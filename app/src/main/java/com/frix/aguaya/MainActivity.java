package com.frix.aguaya;

import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.frix.aguaya.controllers.ReportController;
import com.frix.aguaya.models.ReportModel;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private TextView tvVistaReportes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        tvVistaReportes = findViewById(R.id.tvVistaReportes);
        cargarReportes();
    }

    private void cargarReportes() {
        ReportController.getApi().obtenerReportes().enqueue(new Callback<List<ReportModel>>() {
            @Override
            public void onResponse(Call<List<ReportModel>> call, Response<List<ReportModel>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<ReportModel> reportes = response.body();
                    if (reportes.isEmpty()) {
                        tvVistaReportes.setText("No hay reportes disponibles.");
                    } else {
                        StringBuilder vistaHtml= new StringBuilder();
                        for (ReportModel reporte : reportes) {
                            vistaHtml.append("📍 ").append(reporte.getBarrio()).append(" (").append(reporte.getTipo()).append(")\n")
                                    .append("📝 ").append(reporte.getDescripcion()).append("\n")
                                    .append("📌 Estado: ").append(reporte.getEstado()).append("\n\n");
                        }
                        tvVistaReportes.setText(vistaHtml.toString());
                    }
                } else {
                    tvVistaReportes.setText("Error en la respuesta del servidor (Código " + response.code() + ")");
                }
            }

            @Override
            public void onFailure(Call<List<ReportModel>> call, Throwable t) {
                tvVistaReportes.setText("Error al conectar con el servidor:\n" + t.getMessage());
            }
        });
    }
}
