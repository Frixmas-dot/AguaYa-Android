package com.frix.aguaya.repositories;

import android.content.ContentResolver;
import android.net.Uri;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.frix.aguaya.controllers.ApiService;
import com.frix.aguaya.controllers.RetrofitClient;
import com.frix.aguaya.models.EstadisticasModel;
import com.frix.aguaya.models.ReportModel;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ReporteRepository {
    private final ApiService apiService;
    private final MutableLiveData<Boolean> cargando = new MutableLiveData<>(false);
    private final MutableLiveData<String> error = new MutableLiveData<>();

    public ReporteRepository() {
        this.apiService = RetrofitClient.getApi();
    }

    public LiveData<Boolean> getCargando() {
        return cargando;
    }

    public LiveData<String> getError() {
        return error;
    }

    public LiveData<List<ReportModel>> getReportes(String tipo, String estado) {
        MutableLiveData<List<ReportModel>> data = new MutableLiveData<>();
        cargando.setValue(true);
        apiService.getReportes(tipo, estado, null, null).enqueue(new Callback<List<ReportModel>>() {
            @Override
            public void onResponse(Call<List<ReportModel>> call, Response<List<ReportModel>> response) {
                cargando.setValue(false);
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    com.frix.aguaya.utils.OfflineCacheManager.saveReportes(null, response.body());
                    com.frix.aguaya.utils.OfflineCacheManager.setOfflineFallbackActive(false);
                    data.setValue(response.body());
                } else {
                    com.frix.aguaya.utils.OfflineCacheManager.setOfflineFallbackActive(true);
                    List<ReportModel> cached = com.frix.aguaya.utils.OfflineCacheManager.getReportes(null);
                    data.setValue(cached);
                }
            }

            @Override
            public void onFailure(Call<List<ReportModel>> call, Throwable t) {
                cargando.setValue(false);
                error.setValue(t.getMessage());
                com.frix.aguaya.utils.OfflineCacheManager.setOfflineFallbackActive(true);
                List<ReportModel> cached = com.frix.aguaya.utils.OfflineCacheManager.getReportes(null);
                data.setValue(cached);
            }
        });
        return data;
    }

    public LiveData<ReportModel> crearReporte(String barrio, String tipoId, String desc, String urgencia, String direccion, Uri foto, ContentResolver contentResolver) {
        return crearReporte(barrio, tipoId, desc, urgencia, direccion, foto, contentResolver, null, null);
    }

    public LiveData<ReportModel> crearReporte(String barrio, String tipoId, String desc, String urgencia, String direccion, Uri foto, ContentResolver contentResolver, Double lat, Double lng) {
        MutableLiveData<ReportModel> data = new MutableLiveData<>();
        cargando.setValue(true);
        
        MultipartBody.Builder builder = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("barrio", barrio)
                .addFormDataPart("tipoId", tipoId)
                .addFormDataPart("desc", desc)
                .addFormDataPart("urgencia", urgencia != null ? urgencia : "media")
                .addFormDataPart("direccion", direccion != null ? direccion : "");

        if (lat != null && lng != null) {
            builder.addFormDataPart("lat", String.valueOf(lat));
            builder.addFormDataPart("lng", String.valueOf(lng));
        }
        
        if (foto != null && contentResolver != null) {
            try {
                InputStream inputStream = contentResolver.openInputStream(foto);
                if (inputStream != null) {
                    ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                    int nRead;
                    byte[] d = new byte[16384];
                    while ((nRead = inputStream.read(d, 0, d.length)) != -1) {
                        buffer.write(d, 0, nRead);
                    }
                    byte[] bytes = buffer.toByteArray();
                    RequestBody reqFile = RequestBody.create(MediaType.parse("image/*"), bytes);
                    builder.addFormDataPart("foto", "imagen.jpg", reqFile);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        apiService.crearReporte(builder.build()).enqueue(new Callback<ReportModel>() {
            @Override
            public void onResponse(Call<ReportModel> call, Response<ReportModel> response) {
                cargando.setValue(false);
                if (response.isSuccessful() && response.body() != null) {
                    data.setValue(response.body());
                } else {
                    String errorMsg = "Error al crear reporte";
                    try {
                        if (response.errorBody() != null) {
                            errorMsg += ": " + response.errorBody().string();
                        } else {
                            errorMsg += " (HTTP " + response.code() + ")";
                        }
                    } catch (Exception ignored) {}
                    error.setValue(errorMsg);
                }
            }
            @Override
            public void onFailure(Call<ReportModel> call, Throwable t) {
                cargando.setValue(false);
                error.setValue(t.getMessage());
            }
        });
        return data;
    }

    public LiveData<Boolean> cambiarEstado(int id, String estado) {
        MutableLiveData<Boolean> success = new MutableLiveData<>();
        cargando.setValue(true);
        Map<String, String> body = new HashMap<>();
        body.put("estado", estado);
        apiService.cambiarEstado(id, body).enqueue(new Callback<ReportModel>() {
            @Override
            public void onResponse(Call<ReportModel> call, Response<ReportModel> response) {
                cargando.setValue(false);
                success.setValue(response.isSuccessful());
                if (!response.isSuccessful()) error.setValue("Error al cambiar estado");
            }
            @Override
            public void onFailure(Call<ReportModel> call, Throwable t) {
                cargando.setValue(false);
                error.setValue(t.getMessage());
                success.setValue(false);
            }
        });
        return success;
    }

    public LiveData<Boolean> eliminarReporte(int id) {
        MutableLiveData<Boolean> success = new MutableLiveData<>();
        cargando.setValue(true);
        apiService.eliminarReporte(id).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                cargando.setValue(false);
                success.setValue(response.isSuccessful());
                if (!response.isSuccessful()) error.setValue("Error al eliminar reporte");
            }
            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                cargando.setValue(false);
                error.setValue(t.getMessage());
                success.setValue(false);
            }
        });
        return success;
    }

    public LiveData<Boolean> confirmarReporte(int id) {
        MutableLiveData<Boolean> success = new MutableLiveData<>();
        cargando.setValue(true);
        apiService.confirmarReporte(id).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                cargando.setValue(false);
                success.setValue(response.isSuccessful());
                if (!response.isSuccessful()) error.setValue("Error al confirmar reporte");
            }
            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                cargando.setValue(false);
                error.setValue(t.getMessage());
                success.setValue(false);
            }
        });
        return success;
    }

    public LiveData<EstadisticasModel> getEstadisticas() {
        MutableLiveData<EstadisticasModel> data = new MutableLiveData<>();
        cargando.setValue(true);
        apiService.getEstadisticas().enqueue(new Callback<EstadisticasModel>() {
            @Override
            public void onResponse(Call<EstadisticasModel> call, Response<EstadisticasModel> response) {
                cargando.setValue(false);
                if (response.isSuccessful() && response.body() != null) {
                    com.frix.aguaya.utils.OfflineCacheManager.saveEstadisticas(null, response.body());
                    data.setValue(response.body());
                } else {
                    data.setValue(com.frix.aguaya.utils.OfflineCacheManager.getEstadisticas(null));
                }
            }
            @Override
            public void onFailure(Call<EstadisticasModel> call, Throwable t) {
                cargando.setValue(false);
                error.setValue(t.getMessage());
                data.setValue(com.frix.aguaya.utils.OfflineCacheManager.getEstadisticas(null));
            }
        });
        return data;
    }
}
