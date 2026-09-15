package com.frix.aguaya.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.FrameLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.frix.aguaya.R;
import com.frix.aguaya.models.WeatherModel;

import java.util.Locale;

public class WeatherBannerView extends FrameLayout {

    private View weatherSkeleton;
    private View weatherData;
    private TextView tvWeatherCondition;
    private TextView tvTemperature;
    private TextView tvHumidity;
    private TextView tvWind;
    private TextView tvPrecipitation;

    public WeatherBannerView(@NonNull Context context) {
        super(context);
        init(context);
    }

    public WeatherBannerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public WeatherBannerView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        LayoutInflater.from(context).inflate(R.layout.layout_weather_banner, this, true);
        weatherSkeleton = findViewById(R.id.weatherSkeleton);
        weatherData = findViewById(R.id.weatherData);
        tvWeatherCondition = findViewById(R.id.tvWeatherCondition);
        tvTemperature = findViewById(R.id.tvTemperature);
        tvHumidity = findViewById(R.id.tvHumidity);
        tvWind = findViewById(R.id.tvWind);
        tvPrecipitation = findViewById(R.id.tvPrecipitation);

        startSkeletonAnimation();
    }

    private void startSkeletonAnimation() {
        AlphaAnimation animation = new AlphaAnimation(0.5f, 1.0f);
        animation.setDuration(800);
        animation.setRepeatCount(Animation.INFINITE);
        animation.setRepeatMode(Animation.REVERSE);
        weatherSkeleton.startAnimation(animation);
    }

    public void setWeatherData(WeatherModel weather) {
        if (weather == null || weather.getCurrent() == null) return;
        
        weatherSkeleton.clearAnimation();
        weatherSkeleton.setVisibility(View.GONE);
        weatherData.setVisibility(View.VISIBLE);

        WeatherModel.Current current = weather.getCurrent();

        tvTemperature.setText(String.format(Locale.getDefault(), "%.1f°C", current.getTemperature2m()));
        tvHumidity.setText(String.format(Locale.getDefault(), "💧 %d%%", current.getRelativeHumidity2m()));
        tvWind.setText(String.format(Locale.getDefault(), "💨 %.1f km/h", current.getWindSpeed10m()));
        tvPrecipitation.setText(String.format(Locale.getDefault(), "🌧 %.1f mm", current.getPrecipitation()));
        
        tvWeatherCondition.setText(getWeatherEmojiAndDescription(current.getWeatherCode()));
    }

    private String getWeatherEmojiAndDescription(int code) {
        if (code == 0) return "☀️ Despejado";
        if (code >= 1 && code <= 3) return "🌤️ Parcialmente nublado";
        if (code == 45 || code == 48) return "🌫️ Niebla";
        if (code >= 51 && code <= 55) return "🌦️ Llovizna";
        if (code >= 61 && code <= 65) return "🌧️ Lluvia";
        if (code >= 71 && code <= 75) return "❄️ Nieve";
        if (code >= 80 && code <= 82) return "🌧️ Aguacero";
        if (code >= 95 && code <= 99) return "⛈️ Tormenta";
        return "☁️ Nublado";
    }
}
