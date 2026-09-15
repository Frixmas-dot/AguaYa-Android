package com.frix.aguaya.repositories;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.frix.aguaya.controllers.RetrofitClient;
import com.frix.aguaya.controllers.WeatherApiService;
import com.frix.aguaya.models.WeatherModel;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class WeatherRepository {
    private final WeatherApiService weatherApiService;
    private final MutableLiveData<Boolean> cargando = new MutableLiveData<>(false);
    private final MutableLiveData<String> error = new MutableLiveData<>();

    public WeatherRepository() {
        this.weatherApiService = RetrofitClient.getWeatherApi();
    }

    public LiveData<Boolean> getCargando() {
        return cargando;
    }

    public LiveData<String> getError() {
        return error;
    }

    public LiveData<WeatherModel> getWeather() {
        MutableLiveData<WeatherModel> data = new MutableLiveData<>();
        cargando.setValue(true);
        
        // Coordenadas fijas para Santa Marta, Colombia según requerimientos
        double lat = 11.2408;
        double lon = -74.199;
        
        weatherApiService.getWeather(lat, lon, true, "temperature_2m,relative_humidity_2m,wind_speed_10m,precipitation,weather_code")
                .enqueue(new Callback<WeatherModel>() {
            @Override
            public void onResponse(Call<WeatherModel> call, Response<WeatherModel> response) {
                cargando.setValue(false);
                if (response.isSuccessful() && response.body() != null) {
                    data.setValue(response.body());
                } else {
                    error.setValue("Error al obtener el clima");
                }
            }

            @Override
            public void onFailure(Call<WeatherModel> call, Throwable t) {
                cargando.setValue(false);
                error.setValue(t.getMessage());
            }
        });
        return data;
    }
}
