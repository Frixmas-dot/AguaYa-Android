package com.frix.aguaya.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.frix.aguaya.R;
import com.frix.aguaya.models.EstadisticasModel;
import java.util.ArrayList;
import java.util.List;

public class BarrioStatsAdapter extends RecyclerView.Adapter<BarrioStatsAdapter.ViewHolder> {
    private final List<EstadisticasModel.BarrioCount> barrios = new ArrayList<>();
    private final Context context;
    private int maxCantidad = 1;

    public BarrioStatsAdapter(Context context) {
        this.context = context;
    }

    public void setBarrios(List<EstadisticasModel.BarrioCount> nuevosBarrios) {
        this.barrios.clear();
        if (nuevosBarrios != null) {
            this.barrios.addAll(nuevosBarrios);
            for (EstadisticasModel.BarrioCount b : barrios) {
                if (b.getCantidad() > maxCantidad) {
                    maxCantidad = b.getCantidad();
                }
            }
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_barrio_stat, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        EstadisticasModel.BarrioCount barrio = barrios.get(position);
        
        holder.txtRank.setText(String.valueOf(position + 1));
        holder.txtBarrioName.setText(barrio.getBarrio());
        holder.txtCantidad.setText(String.valueOf(barrio.getCantidad()));
        holder.txtTipoPrincipal.setText(barrio.getTipoPrincipal());
        
        holder.txtTipoPrincipal.setTextColor(getTipoColor(barrio.getTipoPrincipal()));
        
        holder.progressBar.setMax(maxCantidad);
        holder.progressBar.setProgress(barrio.getCantidad());
    }

    @Override
    public int getItemCount() {
        return barrios.size();
    }
    
    private int getTipoColor(String tipo) {
        if (tipo == null) return Color.parseColor("#0c4a6e");
        switch (tipo) {
            case "Sin agua": return Color.parseColor("#0ea5e9");
            case "Baja presión": return Color.parseColor("#f97316");
            case "Agua sucia": return Color.parseColor("#a16207");
            case "Fuga": return Color.parseColor("#ef4444");
            default: return Color.parseColor("#0c4a6e");
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtRank, txtBarrioName, txtCantidad, txtTipoPrincipal;
        ProgressBar progressBar;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtRank = itemView.findViewById(R.id.txtRank);
            txtBarrioName = itemView.findViewById(R.id.txtBarrioName);
            txtCantidad = itemView.findViewById(R.id.txtCantidad);
            txtTipoPrincipal = itemView.findViewById(R.id.txtTipoPrincipal);
            progressBar = itemView.findViewById(R.id.progressStats);
        }
    }
}
