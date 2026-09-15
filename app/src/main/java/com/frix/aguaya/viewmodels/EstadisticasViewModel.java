package com.frix.aguaya.viewmodels;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;

import com.frix.aguaya.models.EstadisticasModel;
import com.frix.aguaya.repositories.ReporteRepository;

public class EstadisticasViewModel extends AndroidViewModel {
    private final ReporteRepository repository;
    private final MediatorLiveData<EstadisticasModel> estadisticas;
    private LiveData<EstadisticasModel> estadisticasSource;

    public EstadisticasViewModel(@NonNull Application application) {
        super(application);
        repository = new ReporteRepository();
        estadisticas = new MediatorLiveData<>();
    }

    public LiveData<EstadisticasModel> getEstadisticas() {
        return estadisticas;
    }

    public LiveData<Boolean> getCargando() {
        return repository.getCargando();
    }

    public void cargarEstadisticas() {
        if (estadisticasSource != null) {
            estadisticas.removeSource(estadisticasSource);
        }
        estadisticasSource = repository.getEstadisticas();
        if (estadisticasSource != null) {
            estadisticas.addSource(estadisticasSource, estadisticas::setValue);
        }
    }
}
