package com.frix.aguaya.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.frix.aguaya.R;
import com.frix.aguaya.models.ReportModel;
import java.util.ArrayList;
import java.util.List;

public class ReportCardAdapter extends RecyclerView.Adapter<ReportCardAdapter.ViewHolder> {
    private final List<ReportModel> reportes = new ArrayList<>();
    private final Context context;
    private final OnReportClickListener listener;

    public interface OnReportClickListener {
        void onReportClick(ReportModel report);
        void onConfirmarClick(ReportModel report, int position);
    }

    public ReportCardAdapter(Context context, OnReportClickListener listener) {
        this.context = context;
        this.listener = listener;
    }

    public void setReportes(List<ReportModel> nuevosReportes) {
        DiffUtil.DiffResult result = DiffUtil.calculateDiff(new DiffUtil.Callback() {
            @Override
            public int getOldListSize() { return reportes.size(); }
            @Override
            public int getNewListSize() { return nuevosReportes.size(); }
            @Override
            public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                return reportes.get(oldItemPosition).getId() == nuevosReportes.get(newItemPosition).getId();
            }
            @Override
            public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                return reportes.get(oldItemPosition).equals(nuevosReportes.get(newItemPosition));
            }
        });
        reportes.clear();
        reportes.addAll(nuevosReportes);
        result.dispatchUpdatesTo(this);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_report_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ReportModel reporte = reportes.get(position);
        
        holder.txtBarrio.setText(reporte.getBarrio());
        holder.txtDescripcion.setText(reporte.getDescripcion());
        holder.txtTiempo.setText(reporte.getTiempoRelativo() != null ? reporte.getTiempoRelativo() : "");
        holder.txtTipo.setText(reporte.getTipo());
        
        String estado = reporte.getEstado() != null ? reporte.getEstado() : "";
        if (!estado.isEmpty()) {
            holder.txtEstado.setText(estado.substring(0, 1).toUpperCase() + estado.substring(1).replace("_", " "));
        }
        
        holder.txtConfirmaciones.setText(String.valueOf(reporte.getConfirmaciones()));

        // Colores y badges
        holder.llTipoBadge.setBackgroundColor(getTipoColor(reporte.getTipo()));
        holder.llEstadoBadge.setBackgroundColor(getEstadoColor(reporte.getEstado()));
        holder.icTipo.setImageResource(getTipoIcon(reporte.getTipo()));

        if (reporte.getFotoUrl() != null && !reporte.getFotoUrl().isEmpty()) {
            holder.imgFoto.setVisibility(View.VISIBLE);
            Glide.with(context).load(reporte.getFotoUrl()).centerCrop().into(holder.imgFoto);
        } else {
            holder.imgFoto.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onReportClick(reporte);
        });

        holder.btnConfirmar.setOnClickListener(v -> {
            if (listener != null) listener.onConfirmarClick(reporte, position);
        });

        if (holder.btnCompartir != null) {
            holder.btnCompartir.setOnClickListener(v -> {
                com.frix.aguaya.utils.ShareHelper.compartirReporte(context, reporte);
            });
        }
    }

    @Override
    public int getItemCount() {
        return reportes.size();
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
    
    private int getEstadoColor(String estado) {
        if (estado == null) return Color.parseColor("#64748b");
        switch (estado) {
            case "pendiente": return Color.parseColor("#ef4444");
            case "en_proceso": return Color.parseColor("#f97316");
            case "resuelto": return Color.parseColor("#22c55e");
            default: return Color.parseColor("#64748b");
        }
    }

    private int getTipoIcon(String tipo) {
        if (tipo == null) return android.R.drawable.ic_menu_info_details;
        switch (tipo) {
            case "Sin agua": return android.R.drawable.ic_delete;
            case "Baja presión": return android.R.drawable.ic_menu_sort_by_size;
            case "Agua sucia": return android.R.drawable.ic_menu_view;
            case "Fuga": return android.R.drawable.ic_menu_share;
            default: return android.R.drawable.ic_menu_info_details;
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtBarrio, txtDescripcion, txtTiempo, txtTipo, txtEstado, txtConfirmaciones;
        ImageView imgFoto, icTipo;
        LinearLayout llTipoBadge, llEstadoBadge;
        View btnConfirmar, btnCompartir;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtBarrio = itemView.findViewById(R.id.txtBarrio);
            txtDescripcion = itemView.findViewById(R.id.txtDescripcion);
            txtTiempo = itemView.findViewById(R.id.txtTiempo);
            txtTipo = itemView.findViewById(R.id.txtTipo);
            txtEstado = itemView.findViewById(R.id.txtEstado);
            txtConfirmaciones = itemView.findViewById(R.id.txtConfirmaciones);
            imgFoto = itemView.findViewById(R.id.imgFoto);
            btnCompartir = itemView.findViewById(R.id.btnCompartir);
            icTipo = itemView.findViewById(R.id.icTipo);
            llTipoBadge = itemView.findViewById(R.id.llTipoBadge);
            llEstadoBadge = itemView.findViewById(R.id.llEstadoBadge);
            btnConfirmar = itemView.findViewById(R.id.btnConfirmar);
        }
    }
}
