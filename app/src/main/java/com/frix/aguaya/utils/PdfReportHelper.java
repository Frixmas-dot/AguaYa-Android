package com.frix.aguaya.utils;

import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import com.frix.aguaya.models.EstadisticasModel;
import com.frix.aguaya.models.ReportModel;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PdfReportHelper {

    public static void exportarInformePdf(Context context, List<ReportModel> reportes, EstadisticasModel stats) {
        if (context == null) return;

        try {
            PdfDocument document = new PdfDocument();
            // Tamaño estándar A4: 595 x 842 puntos
            int pageWidth = 595;
            int pageHeight = 842;

            PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create();
            PdfDocument.Page page = document.startPage(pageInfo);
            Canvas canvas = page.getCanvas();

            Paint paint = new Paint();
            paint.setAntiAlias(true);

            // 1. Encabezado principal (Navy #0c4a6e)
            paint.setColor(Color.parseColor("#0c4a6e"));
            canvas.drawRect(0, 0, pageWidth, 110, paint);

            // Decoración curva o acento en cyan
            paint.setColor(Color.parseColor("#0ea5e9"));
            canvas.drawRect(0, 105, pageWidth, 110, paint);

            // Título
            paint.setColor(Color.WHITE);
            paint.setTextSize(18);
            paint.setFakeBoldText(true);
            canvas.drawText("AGUAYA — INFORME EJECUTIVO DE CRISIS HÍDRICA", 30, 45, paint);

            paint.setTextSize(12);
            paint.setFakeBoldText(false);
            paint.setColor(Color.parseColor("#e0f2fe"));
            canvas.drawText("Monitoreo ciudadano y veeduría de agua potable en Santa Marta, Colombia", 30, 68, paint);

            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy - hh:mm a", new Locale("es", "CO"));
            String fechaEmision = "Generado: " + sdf.format(new Date());
            paint.setTextSize(10);
            canvas.drawText(fechaEmision, 30, 88, paint);

            // 2. Tarjetas de Resumen KPI
            int yPos = 135;
            paint.setColor(Color.parseColor("#1e293b"));
            paint.setTextSize(14);
            paint.setFakeBoldText(true);
            canvas.drawText("Indicadores Clave de Impacto Comunitario", 30, yPos, paint);

            yPos += 15;
            int cardW = 125;
            int cardH = 55;
            int margin = 10;
            int startX = 30;

            int total = (stats != null) ? stats.getTotalReportes() : (reportes != null ? reportes.size() : 0);
            int barrios = (stats != null) ? stats.getBarriosUnicos() : 0;
            double resueltos = (stats != null) ? stats.getPctResueltos() : 0.0;
            double pendientes = (stats != null) ? stats.getPctPendientes() : 0.0;

            dibujarKpiCard(canvas, paint, startX, yPos, cardW, cardH, "Total Reportes", String.valueOf(total), "#0c4a6e");
            dibujarKpiCard(canvas, paint, startX + cardW + margin, yPos, cardW, cardH, "Barrios Afectados", String.valueOf(barrios), "#0ea5e9");
            dibujarKpiCard(canvas, paint, startX + (cardW + margin) * 2, yPos, cardW, cardH, "Resueltos (%)", String.format(Locale.getDefault(), "%.1f%%", resueltos), "#22c55e");
            dibujarKpiCard(canvas, paint, startX + (cardW + margin) * 3, yPos, cardW, cardH, "Pendientes (%)", String.format(Locale.getDefault(), "%.1f%%", pendientes), "#ef4444");

            // 3. Desglose por tipo de problema
            yPos += cardH + 30;
            paint.setColor(Color.parseColor("#1e293b"));
            paint.setTextSize(14);
            paint.setFakeBoldText(true);
            canvas.drawText("Distribución de Problemáticas Reportadas", 30, yPos, paint);

            yPos += 18;
            if (stats != null && stats.getPorTipo() != null) {
                paint.setTextSize(10);
                paint.setFakeBoldText(false);
                int colX = 30;
                for (EstadisticasModel.TipoCount tc : stats.getPorTipo()) {
                    paint.setColor(Color.parseColor("#f1f5f9"));
                    canvas.drawRoundRect(new RectF(colX, yPos, colX + 125, yPos + 24), 6, 6, paint);

                    paint.setColor(Color.parseColor("#0f172a"));
                    paint.setFakeBoldText(true);
                    canvas.drawText(tc.getTipo() + ": " + tc.getCantidad(), colX + 8, yPos + 16, paint);
                    colX += 135;
                    if (colX > 480) break;
                }
            }

            // 4. Tabla de Zonas Más Críticas
            yPos += 45;
            paint.setColor(Color.parseColor("#1e293b"));
            paint.setTextSize(14);
            paint.setFakeBoldText(true);
            canvas.drawText("Barrios con Mayor Alerta en Santa Marta", 30, yPos, paint);

            yPos += 15;
            paint.setColor(Color.parseColor("#0c4a6e"));
            canvas.drawRect(30, yPos, pageWidth - 30, yPos + 22, paint);

            paint.setColor(Color.WHITE);
            paint.setTextSize(10);
            paint.setFakeBoldText(true);
            canvas.drawText("Barrio", 40, yPos + 15, paint);
            canvas.drawText("Incidencias", 220, yPos + 15, paint);
            canvas.drawText("Falla Predominante", 340, yPos + 15, paint);
            canvas.drawText("Nivel de Riesgo", 460, yPos + 15, paint);

            yPos += 22;
            if (stats != null && stats.getPorBarrio() != null) {
                paint.setTextSize(9);
                paint.setFakeBoldText(false);
                int count = 0;
                for (EstadisticasModel.BarrioCount bc : stats.getPorBarrio()) {
                    paint.setColor(count % 2 == 0 ? Color.parseColor("#f8fafc") : Color.WHITE);
                    canvas.drawRect(30, yPos, pageWidth - 30, yPos + 20, paint);

                    paint.setColor(Color.parseColor("#1e293b"));
                    canvas.drawText(bc.getBarrio(), 40, yPos + 14, paint);
                    canvas.drawText(String.valueOf(bc.getCantidad()) + " reportes", 220, yPos + 14, paint);
                    canvas.drawText(bc.getTipoPrincipal() != null ? bc.getTipoPrincipal() : "Sin agua", 340, yPos + 14, paint);

                    paint.setColor(Color.parseColor("#ef4444"));
                    paint.setFakeBoldText(true);
                    canvas.drawText(count == 0 ? "CRÍTICO" : "ALTO", 460, yPos + 14, paint);
                    paint.setFakeBoldText(false);

                    yPos += 20;
                    count++;
                    if (count >= 5) break;
                }
            }

            // 5. Tabla de Reportes Ciudadanos Recientes
            yPos += 25;
            paint.setColor(Color.parseColor("#1e293b"));
            paint.setTextSize(14);
            paint.setFakeBoldText(true);
            canvas.drawText("Muestra de Reportes Comunitarios Recientes", 30, yPos, paint);

            yPos += 15;
            paint.setColor(Color.parseColor("#334155"));
            canvas.drawRect(30, yPos, pageWidth - 30, yPos + 22, paint);

            paint.setColor(Color.WHITE);
            paint.setTextSize(10);
            paint.setFakeBoldText(true);
            canvas.drawText("ID / Barrio", 40, yPos + 15, paint);
            canvas.drawText("Tipo de Problema", 170, yPos + 15, paint);
            canvas.drawText("Urgencia", 300, yPos + 15, paint);
            canvas.drawText("Estado", 400, yPos + 15, paint);
            canvas.drawText("Apoyos", 490, yPos + 15, paint);

            yPos += 22;
            if (reportes != null) {
                paint.setTextSize(9);
                paint.setFakeBoldText(false);
                int limit = Math.min(reportes.size(), 7);
                for (int i = 0; i < limit; i++) {
                    ReportModel r = reportes.get(i);
                    paint.setColor(i % 2 == 0 ? Color.parseColor("#f8fafc") : Color.WHITE);
                    canvas.drawRect(30, yPos, pageWidth - 30, yPos + 20, paint);

                    paint.setColor(Color.parseColor("#1e293b"));
                    canvas.drawText("#" + r.getId() + " - " + r.getBarrio(), 40, yPos + 14, paint);
                    canvas.drawText(r.getTipo(), 170, yPos + 14, paint);
                    canvas.drawText(r.getUrgencia() != null ? r.getUrgencia().toUpperCase() : "MEDIA", 300, yPos + 14, paint);
                    canvas.drawText(r.getEstado() != null ? r.getEstado().replace("_", " ") : "pendiente", 400, yPos + 14, paint);
                    canvas.drawText("+" + r.getConfirmaciones(), 490, yPos + 14, paint);

                    yPos += 20;
                }
            }

            // 6. Pie de página institucional y legal
            paint.setColor(Color.parseColor("#94a3b8"));
            canvas.drawLine(30, pageHeight - 50, pageWidth - 30, pageHeight - 50, paint);

            paint.setColor(Color.parseColor("#64748b"));
            paint.setTextSize(8);
            paint.setFakeBoldText(false);
            canvas.drawText("AguaYa Santa Marta • Plataforma comunitaria de auditoría social y veeduría hídrica.", 30, pageHeight - 36, paint);
            canvas.drawText("Documento emitido conforme a datos en tiempo real de la ciudadanía. El agua es un derecho.", 30, pageHeight - 24, paint);

            document.finishPage(page);

            // Guardar archivo PDF en caché
            File cacheDir = new File(context.getExternalCacheDir(), "informes");
            if (!cacheDir.exists()) cacheDir.mkdirs();

            File pdfFile = new File(cacheDir, "Informe_AguaYa_SantaMarta.pdf");
            FileOutputStream fos = new FileOutputStream(pdfFile);
            document.writeTo(fos);
            document.close();
            fos.close();

            // Compartir / Abrir con FileProvider
            Uri uri = FileProvider.getUriForFile(context, context.getPackageName() + ".fileprovider", pdfFile);
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("application/pdf");
            shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Informe Ejecutivo AguaYa Santa Marta");
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            Intent chooser = Intent.createChooser(shareIntent, "Abrir o compartir informe PDF:");
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(chooser);

            Toast.makeText(context, "Informe PDF generado exitosamente", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, "Error al generar informe PDF: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private static void dibujarKpiCard(Canvas canvas, Paint paint, int x, int y, int w, int h, String titulo, String valor, String colorHex) {
        paint.setColor(Color.parseColor("#f8fafc"));
        canvas.drawRoundRect(new RectF(x, y, x + w, y + h), 8, 8, paint);

        // Barra decorativa izquierda
        paint.setColor(Color.parseColor(colorHex));
        canvas.drawRoundRect(new RectF(x, y, x + 4, y + h), 4, 4, paint);

        paint.setColor(Color.parseColor("#64748b"));
        paint.setTextSize(9);
        paint.setFakeBoldText(false);
        canvas.drawText(titulo, x + 10, y + 20, paint);

        paint.setColor(Color.parseColor(colorHex));
        paint.setTextSize(16);
        paint.setFakeBoldText(true);
        canvas.drawText(valor, x + 10, y + 42, paint);
    }
}
