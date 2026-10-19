package com.frix.aguaya.adapters;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.frix.aguaya.R;
import com.frix.aguaya.models.RutaCarrotanqueModel;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

public class RutaCarrotanqueAdapter extends RecyclerView.Adapter<RutaCarrotanqueAdapter.ViewHolder> {

    private final Context context;
    private final List<RutaCarrotanqueModel> rutas = new ArrayList<>();

    public RutaCarrotanqueAdapter(Context context) {
        this.context = context;
    }

    public void setRutas(List<RutaCarrotanqueModel> nuevasRutas) {
        this.rutas.clear();
        if (nuevasRutas != null) {
            this.rutas.addAll(nuevasRutas);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_ruta_carrotanque, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        RutaCarrotanqueModel ruta = rutas.get(position);

        holder.tvRutaBarrio.setText(ruta.getBarrio() + " — " + ruta.getSector());
        holder.tvRutaVehiculo.setText(ruta.getVehiculo());
        holder.tvRutaHorario.setText("⏰ " + ruta.getHorario());
        holder.tvRutaCapacidad.setText("💧 " + ruta.getCapacidad());
        holder.tvRutaPunto.setText("📍 Punto: " + ruta.getPuntoDistribucion());
        holder.tvRutaObservaciones.setText("ℹ️ " + ruta.getObservaciones());

        // Configuración de Estado y Badge
        String estado = ruta.getEstado() != null ? ruta.getEstado() : "programada";
        if ("en_camino".equalsIgnoreCase(estado)) {
            holder.tvEstadoRuta.setText("🟢 En camino");
            holder.llEstadoRutaBadge.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#22c55e")));
        } else if ("completada".equalsIgnoreCase(estado)) {
            holder.tvEstadoRuta.setText("🔵 Completada");
            holder.llEstadoRutaBadge.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#0284c7")));
        } else {
            holder.tvEstadoRuta.setText("🟡 Programada");
            holder.llEstadoRutaBadge.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#f59e0b")));
        }

        // Compartir en WhatsApp
        holder.btnCompartirRuta.setOnClickListener(v -> compartirRuta(ruta));
    }

    private void compartirRuta(RutaCarrotanqueModel ruta) {
        String mensaje = "🚨 *Aviso de Carrotanque — AguaYa Santa Marta* 💧\n\n" +
                "📍 *Sector:* " + ruta.getBarrio() + " (" + ruta.getSector() + ")\n" +
                "⏰ *Horario estimado:* " + ruta.getHorario() + "\n" +
                "🚚 *Vehículo:* " + ruta.getVehiculo() + "\n" +
                "💧 *Capacidad:* " + ruta.getCapacidad() + "\n" +
                "📌 *Punto de entrega:* " + ruta.getPuntoDistribucion() + "\n" +
                "ℹ️ *Nota:* " + ruta.getObservaciones() + "\n\n" +
                "📲 *¡Comunícale a tus vecinos! Información verificada en AguaYa Santa Marta.*";

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_SUBJECT, "Ruta de Carrotanque — " + ruta.getBarrio());
        intent.putExtra(Intent.EXTRA_TEXT, mensaje);

        Intent chooser = Intent.createChooser(intent, "Avisar a vecinos de la ruta:");
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(chooser);
    }

    @Override
    public int getItemCount() {
        return rutas.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvRutaBarrio, tvRutaVehiculo, tvEstadoRuta, tvRutaHorario, tvRutaCapacidad, tvRutaPunto, tvRutaObservaciones;
        LinearLayout llEstadoRutaBadge;
        MaterialButton btnCompartirRuta;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvRutaBarrio = itemView.findViewById(R.id.tvRutaBarrio);
            tvRutaVehiculo = itemView.findViewById(R.id.tvRutaVehiculo);
            tvEstadoRuta = itemView.findViewById(R.id.tvEstadoRuta);
            tvRutaHorario = itemView.findViewById(R.id.tvRutaHorario);
            tvRutaCapacidad = itemView.findViewById(R.id.tvRutaCapacidad);
            tvRutaPunto = itemView.findViewById(R.id.tvRutaPunto);
            tvRutaObservaciones = itemView.findViewById(R.id.tvRutaObservaciones);
            llEstadoRutaBadge = itemView.findViewById(R.id.llEstadoRutaBadge);
            btnCompartirRuta = itemView.findViewById(R.id.btnCompartirRuta);
        }
    }
}
