package com.frix.aguaya.utils;

import android.content.Context;
import android.content.Intent;
import com.frix.aguaya.models.ReportModel;

public class ShareHelper {

    public static void compartirReporte(Context context, ReportModel reporte) {
        if (context == null || reporte == null) return;

        String barrio = reporte.getBarrio() != null ? reporte.getBarrio() : "Santa Marta";
        String tipo = reporte.getTipo() != null ? reporte.getTipo() : "Reporte de agua";
        String desc = reporte.getDescripcion() != null ? reporte.getDescripcion() : "";
        String tiempo = reporte.getTiempoRelativo() != null && !reporte.getTiempoRelativo().isEmpty()
                ? reporte.getTiempoRelativo()
                : "Reciente";
        
        String urgencia = reporte.getUrgencia() != null ? reporte.getUrgencia().toUpperCase() : "MEDIA";
        String emojiUrgencia = "alta".equalsIgnoreCase(urgencia) ? "🔴" : ("baja".equalsIgnoreCase(urgencia) ? "🟢" : "🟠");

        String estado = reporte.getEstado() != null ? reporte.getEstado() : "pendiente";
        String estadoLabel = "resuelto".equalsIgnoreCase(estado) ? "✅ Resuelto"
                : ("en_proceso".equalsIgnoreCase(estado) ? "🟠 En proceso" : "⏳ Pendiente");

        String mensaje = "🚨 *Alerta Comunitaria — AguaYa Santa Marta* 🌊\n\n" +
                "📍 *Barrio:* " + barrio + "\n" +
                "⚠️ *Problema:* " + tipo + "\n" +
                emojiUrgencia + " *Urgencia:* " + urgencia + "\n" +
                "📌 *Estado:* " + estadoLabel + "\n" +
                "🕒 *Publicado:* " + tiempo + "\n" +
                (desc.isEmpty() ? "" : "📝 *Detalles:* " + desc + "\n") +
                "\n📲 *¡Únete y apoya este reporte en la app comunitaria AguaYa!*\n" +
                "El agua es un derecho. Exijámoslo juntos. 💧";

        Intent sendIntent = new Intent(Intent.ACTION_SEND);
        sendIntent.setType("text/plain");
        sendIntent.putExtra(Intent.EXTRA_SUBJECT, "Reporte AguaYa — " + barrio);
        sendIntent.putExtra(Intent.EXTRA_TEXT, mensaje);

        Intent chooser = Intent.createChooser(sendIntent, "Compartir alerta en WhatsApp o redes:");
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(chooser);
    }
}
