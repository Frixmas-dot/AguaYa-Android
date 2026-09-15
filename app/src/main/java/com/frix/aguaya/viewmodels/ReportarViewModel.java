package com.frix.aguaya.viewmodels;

import android.app.Application;
import android.content.ContentResolver;
import android.net.Uri;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import com.frix.aguaya.models.ReportModel;
import com.frix.aguaya.repositories.ReporteRepository;

public class ReportarViewModel extends AndroidViewModel {
    private final ReporteRepository repository;
    
    public final MutableLiveData<String> barrio = new MutableLiveData<>("");
    public final MutableLiveData<String> tipoSeleccionado = new MutableLiveData<>("");
    public final MutableLiveData<String> descripcion = new MutableLiveData<>("");
    public final MutableLiveData<String> urgencia = new MutableLiveData<>("media");
    public final MutableLiveData<String> direccion = new MutableLiveData<>("");
    public final MutableLiveData<Uri> fotoUri = new MutableLiveData<>(null);
    
    private final MutableLiveData<ReportModel> reporteCreado = new MutableLiveData<>();
    private final MutableLiveData<String> error = new MutableLiveData<>();

    public ReportarViewModel(@NonNull Application application) {
        super(application);
        repository = new ReporteRepository();
    }

    public LiveData<ReportModel> getReporteCreado() { return reporteCreado; }
    public LiveData<Boolean> getEnviando() { return repository.getCargando(); }
    public LiveData<String> getError() { return repository.getError(); }

    public void enviarReporte(ContentResolver contentResolver) {
        String bar = barrio.getValue();
        String tipo = tipoSeleccionado.getValue();
        String desc = descripcion.getValue();
        String dir = direccion.getValue();
        String urg = urgencia.getValue();
        Uri foto = fotoUri.getValue();

        if (bar == null || bar.isEmpty() || tipo == null || tipo.isEmpty() || desc == null || desc.isEmpty()) {
            error.setValue("Por favor complete los campos obligatorios.");
            return;
        }

        LiveData<ReportModel> request = repository.crearReporte(bar, tipo, desc, urg, dir, foto, contentResolver);
        if (request != null) {
            request.observeForever(new Observer<ReportModel>() {
                @Override
                public void onChanged(ReportModel reportModel) {
                    if (reportModel != null) {
                        reporteCreado.setValue(reportModel);
                    }
                    request.removeObserver(this);
                }
            });
        }
    }

    public void resetFormulario() {
        barrio.setValue("");
        tipoSeleccionado.setValue("");
        descripcion.setValue("");
        urgencia.setValue("media");
        direccion.setValue("");
        fotoUri.setValue(null);
        error.setValue(null);
        reporteCreado.setValue(null);
    }
}
