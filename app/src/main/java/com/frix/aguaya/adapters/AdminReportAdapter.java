package com.frix.aguaya.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.frix.aguaya.R;
import com.frix.aguaya.models.ReportModel;
import java.util.ArrayList;
import java.util.List;

public class AdminReportAdapter extends RecyclerView.Adapter<AdminReportAdapter.ViewHolder> {
    private final List<ReportModel> reportes = new ArrayList<>();
    private final Context context;
    private final OnAdminActionListener listener;

    public interface OnAdminActionListener {
        void onCambiarEstado(int id, String estado);
        void onEliminar(int id);
    }

    public AdminReportAdapter(Context context, OnAdminActionListener listener) {
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
        View view = LayoutInflater.from(context).inflate(R.layout.item_admin_report, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ReportModel reporte = reportes.get(position);
        
        holder.txtId.setText("ID: " + reporte.getId());
        holder.txtBarrio.setText(reporte.getBarrio());
        holder.txtDireccion.setText("Dirección: " + (reporte.getDireccion() != null ? reporte.getDireccion() : "N/A"));
        holder.txtTipo.setText(reporte.getTipo());
        holder.txtDescripcion.setText(reporte.getDescripcion());
        holder.txtUrgencia.setText("Urgencia: " + reporte.getUrgencia());
        holder.txtConfirmaciones.setText("Apoyos: " + reporte.getConfirmaciones());
        holder.txtFecha.setText(reporte.getCreatedAt());
        holder.txtEstado.setText(reporte.getEstado() != null ? reporte.getEstado().toUpperCase() : "");

        holder.txtEstado.setTextColor(getEstadoColor(reporte.getEstado()));
        holder.txtUrgencia.setTextColor(getUrgenciaColor(reporte.getUrgencia()));

        if ("resuelto".equals(reporte.getEstado())) {
            holder.itemView.setAlpha(0.6f);
        } else {
            holder.itemView.setAlpha(1.0f);
        }

        if (reporte.getFotoUrl() != null && !reporte.getFotoUrl().isEmpty()) {
            holder.imgFoto.setVisibility(View.VISIBLE);
            Glide.with(context).load(reporte.getFotoUrl()).into(holder.imgFoto);
        } else {
            holder.imgFoto.setVisibility(View.GONE);
        }

        holder.btnEnProceso.setOnClickListener(v -> {
            if (listener != null) listener.onCambiarEstado(reporte.getId(), "en_proceso");
        });
        
        holder.btnResuelto.setOnClickListener(v -> {
            if (listener != null) listener.onCambiarEstado(reporte.getId(), "resuelto");
        });
        
        holder.btnPendiente.setOnClickListener(v -> {
            if (listener != null) listener.onCambiarEstado(reporte.getId(), "pendiente");
        });
        
        holder.btnEliminar.setOnClickListener(v -> {
            if (listener != null) listener.onEliminar(reporte.getId());
        });
    }

    @Override
    public int getItemCount() {
        return reportes.size();
    }

    private int getEstadoColor(String estado) {
        if (estado == null) return Color.BLACK;
        switch (estado) {
            case "pendiente": return Color.parseColor("#ef4444");
            case "en_proceso": return Color.parseColor("#f97316");
            case "resuelto": return Color.parseColor("#22c55e");
            default: return Color.BLACK;
        }
    }
    
    private int getUrgenciaColor(String urgencia) {
        if (urgencia == null) return Color.BLACK;
        switch (urgencia) {
            case "baja": return Color.parseColor("#22c55e");
            case "media": return Color.parseColor("#f97316");
            case "alta": return Color.parseColor("#ef4444");
            default: return Color.BLACK;
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtId, txtBarrio, txtDireccion, txtTipo, txtDescripcion, txtUrgencia, txtConfirmaciones, txtFecha, txtEstado;
        ImageView imgFoto;
        Button btnEnProceso, btnResuelto, btnPendiente, btnEliminar;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtId = itemView.findViewById(R.id.txtAdminId);
            txtBarrio = itemView.findViewById(R.id.txtAdminBarrio);
            txtDireccion = itemView.findViewById(R.id.txtAdminDireccion);
            txtTipo = itemView.findViewById(R.id.txtAdminTipo);
            txtDescripcion = itemView.findViewById(R.id.txtAdminDescripcion);
            txtUrgencia = itemView.findViewById(R.id.txtAdminUrgencia);
            txtConfirmaciones = itemView.findViewById(R.id.txtAdminConfirmaciones);
            txtFecha = itemView.findViewById(R.id.txtAdminFecha);
            txtEstado = itemView.findViewById(R.id.txtAdminEstado);
            imgFoto = itemView.findViewById(R.id.imgAdminFoto);
            btnEnProceso = itemView.findViewById(R.id.btnAdminEnProceso);
            btnResuelto = itemView.findViewById(R.id.btnAdminResuelto);
            btnPendiente = itemView.findViewById(R.id.btnAdminPendiente);
            btnEliminar = itemView.findViewById(R.id.btnAdminEliminar);
        }
    }
}
