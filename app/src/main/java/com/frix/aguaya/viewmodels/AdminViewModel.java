package com.frix.aguaya.viewmodels;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.frix.aguaya.models.ReportModel;
import com.frix.aguaya.repositories.ReporteRepository;

import java.util.List;

public class AdminViewModel extends AndroidViewModel {
    private final ReporteRepository repository;
    private final MediatorLiveData<List<ReportModel>> reportes;
    private LiveData<List<ReportModel>> reportesSource;
    
    private final MutableLiveData<Boolean> autenticado;
    public final MutableLiveData<String> filtroEstado;
    private final MutableLiveData<String> mensaje;
    
    private final SharedPreferences prefs;

    public AdminViewModel(@NonNull Application application) {
        super(application);
        repository = new ReporteRepository();
        reportes = new MediatorLiveData<>();
        filtroEstado = new MutableLiveData<>(null);
        mensaje = new MutableLiveData<>(null);
        
        prefs = application.getSharedPreferences("admin_prefs", Context.MODE_PRIVATE);
        autenticado = new MutableLiveData<>(prefs.getBoolean("is_admin", false));
    }

    public LiveData<List<ReportModel>> getReportes() { return reportes; }
    public LiveData<Boolean> getCargando() { return repository.getCargando(); }
    public LiveData<Boolean> getAutenticado() { return autenticado; }
    public LiveData<String> getMensaje() { return mensaje; }

    public boolean login(String password) {
        if ("08042003".equals(password)) {
            prefs.edit().putBoolean("is_admin", true).apply();
            autenticado.setValue(true);
            return true;
        }
        return false;
    }

    public void logout() {
        prefs.edit().putBoolean("is_admin", false).apply();
        autenticado.setValue(false);
    }

    public void cargarReportes() {
        if (reportesSource != null) {
            reportes.removeSource(reportesSource);
        }
        String estado = filtroEstado.getValue();
        reportesSource = repository.getReportes(null, estado);
        if (reportesSource != null) {
            reportes.addSource(reportesSource, reportes::setValue);
        }
    }

    public void cambiarEstado(int id, String nuevoEstado) {
        LiveData<Boolean> request = repository.cambiarEstado(id, nuevoEstado);
        if (request != null) {
            request.observeForever(new Observer<Boolean>() {
                @Override
                public void onChanged(Boolean success) {
                    if (success != null && success) {
                        mensaje.setValue("Estado actualizado: " + nuevoEstado);
                        cargarReportes();
                    }
                    request.removeObserver(this);
                }
            });
        }
    }

    public void eliminarReporte(int id) {
        LiveData<Boolean> request = repository.eliminarReporte(id);
        if (request != null) {
            request.observeForever(new Observer<Boolean>() {
                @Override
                public void onChanged(Boolean success) {
                    if (success != null && success) {
                        mensaje.setValue("Reporte eliminado");
                        cargarReportes();
                    }
                    request.removeObserver(this);
                }
            });
        }
    }
}
