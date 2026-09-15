package com.frix.aguaya.models;

import com.google.gson.annotations.SerializedName;

public class WeatherModel {
    private Current current;

    public Current getCurrent() {
        return current;
    }

    public void setCurrent(Current current) {
        this.current = current;
    }

    public static class Current {
        @SerializedName("temperature_2m")
        private double temperature2m;
        
        @SerializedName("relative_humidity_2m")
        private int relativeHumidity2m;
        
        @SerializedName("wind_speed_10m")
        private double windSpeed10m;
        
        private double precipitation;
        
        @SerializedName("weather_code")
        private int weatherCode;

        public double getTemperature2m() { return temperature2m; }
        public void setTemperature2m(double temperature2m) { this.temperature2m = temperature2m; }

        public int getRelativeHumidity2m() { return relativeHumidity2m; }
        public void setRelativeHumidity2m(int relativeHumidity2m) { this.relativeHumidity2m = relativeHumidity2m; }

        public double getWindSpeed10m() { return windSpeed10m; }
        public void setWindSpeed10m(double windSpeed10m) { this.windSpeed10m = windSpeed10m; }

        public double getPrecipitation() { return precipitation; }
        public void setPrecipitation(double precipitation) { this.precipitation = precipitation; }

        public int getWeatherCode() { return weatherCode; }
        public void setWeatherCode(int weatherCode) { this.weatherCode = weatherCode; }
    }
}
