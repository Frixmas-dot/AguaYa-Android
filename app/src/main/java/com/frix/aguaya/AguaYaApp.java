package com.frix.aguaya;

import android.app.Application;

public class AguaYaApp extends Application {
    private static AguaYaApp instance;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
    }

    public static AguaYaApp getInstance() {
        return instance;
    }
}
