package com.frix.aguaya.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.text.InputType;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Toast;

import java.net.URI;

public class ServerConfig {
    private static final String PREFS_NAME = "aguaya_server_config";
    private static final String KEY_SERVER_URL = "server_base_url";
    public static final String DEFAULT_URL = "http://192.168.20.229:3001/";

    public static String getServerUrl(Context context) {
        if (context == null) return DEFAULT_URL;
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getString(KEY_SERVER_URL, DEFAULT_URL);
    }

    public static void setServerUrl(Context context, String url) {
        if (context == null || url == null) return;
        String clean = url.trim();
        if (!clean.startsWith("http://") && !clean.startsWith("https://")) {
            clean = "http://" + clean;
        }

        try {
            URI uri = URI.create(clean);
            String host = uri.getHost();
            int port = uri.getPort();
            if (port == -1 && host != null && (host.matches("\\d+\\.\\d+\\.\\d+\\.\\d+") || host.equals("localhost"))) {
                clean = uri.getScheme() + "://" + host + ":3001";
                if (uri.getPath() != null) {
                    clean += uri.getPath();
                }
            }
        } catch (Exception ignored) {}

        if (!clean.endsWith("/")) {
            clean = clean + "/";
        }

        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString(KEY_SERVER_URL, clean).apply();
    }

    public static void resetToDefault(Context context) {
        if (context == null) return;
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString(KEY_SERVER_URL, DEFAULT_URL).apply();
    }

    public static void showServerConfigDialog(Context context, Runnable onConfigSaved) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("⚙️ Servidor Backend");
        builder.setMessage("Ingresa la IP o URL del Ubuntu Server (Backend de AguaYa):\n\nEjemplo: 192.168.43.50 o 192.168.43.50:3001");

        final EditText input = new EditText(context);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_URI);
        input.setText(getServerUrl(context));
        input.setHint("http://192.168.x.x:3001/");
        input.setSelectAllOnFocus(true);

        FrameLayout container = new FrameLayout(context);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        int margin = dpToPx(context, 20);
        params.leftMargin = margin;
        params.rightMargin = margin;
        input.setLayoutParams(params);
        container.addView(input);
        builder.setView(container);

        builder.setPositiveButton("Guardar y Conectar", (dialog, which) -> {
            String newUrl = input.getText().toString().trim();
            if (!newUrl.isEmpty()) {
                setServerUrl(context, newUrl);
                Toast.makeText(context, "✅ Servidor actualizado a:\n" + getServerUrl(context), Toast.LENGTH_LONG).show();
                if (onConfigSaved != null) {
                    onConfigSaved.run();
                }
            }
        });

        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.cancel());

        builder.setNeutralButton("Restablecer", (dialog, which) -> {
            resetToDefault(context);
            Toast.makeText(context, "🔄 Restaurado por defecto:\n" + DEFAULT_URL, Toast.LENGTH_SHORT).show();
            if (onConfigSaved != null) {
                onConfigSaved.run();
            }
        });

        builder.show();
    }

    private static int dpToPx(Context context, int dp) {
        return (int) (dp * context.getResources().getDisplayMetrics().density);
    }
}
