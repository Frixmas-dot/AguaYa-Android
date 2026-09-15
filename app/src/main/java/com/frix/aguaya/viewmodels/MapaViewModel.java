package com.frix.aguaya.viewmodels;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;

import com.frix.aguaya.models.ReportModel;
import com.frix.aguaya.models.WeatherModel;
import com.frix.aguaya.repositories.ReporteRepository;
import com.frix.aguaya.repositories.WeatherRepository;

import java.util.List;

public class MapaViewModel extends AndroidViewModel {
    private final ReporteRepository reporteRepository;
    private final WeatherRepository weatherRepository;
    
    private final MutableLiveData<String> filtroTipo;
    private final MediatorLiveData<List<ReportModel>> reportes;
    private final MediatorLiveData<WeatherModel> clima;
    
    private LiveData<List<ReportModel>> reportesSource;
    private LiveData<WeatherModel> climaSource;

    public MapaViewModel(@NonNull Application application) {
        super(application);
        reporteRepository = new ReporteRepository();
        weatherRepository = new WeatherRepository();
        filtroTipo = new MutableLiveData<>(null);
        reportes = new MediatorLiveData<>();
        clima = new MediatorLiveData<>();
    }

    public LiveData<List<ReportModel>> getReportes() { return reportes; }
    public LiveData<WeatherModel> getClima() { return clima; }
    public LiveData<Boolean> getCargando() { return reporteRepository.getCargando(); }
    public MutableLiveData<String> getFiltroTipo() { return filtroTipo; }

    public void cargarReportes() {
        if (reportesSource != null) {
            reportes.removeSource(reportesSource);
        }
        String tipo = filtroTipo.getValue();
        reportesSource = reporteRepository.getReportes(tipo, null);
        if (reportesSource != null) {
            reportes.addSource(reportesSource, reportes::setValue);
        }
    }

    public void setFiltro(String tipo) {
        filtroTipo.setValue(tipo);
        cargarReportes();
    }

    public void cargarClima() {
        if (climaSource != null) {
            clima.removeSource(climaSource);
        }
        climaSource = weatherRepository.getWeather();
        if (climaSource != null) {
            clima.addSource(climaSource, clima::setValue);
        }
    }
}
