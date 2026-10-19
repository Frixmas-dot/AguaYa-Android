package com.frix.aguaya.ui;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.frix.aguaya.R;
import com.frix.aguaya.databinding.DialogReportDetailBinding;
import com.frix.aguaya.models.ReportModel;
import com.frix.aguaya.repositories.ReporteRepository;
import com.frix.aguaya.utils.ShareHelper;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.Locale;

public class ReportDetailBottomSheet extends BottomSheetDialogFragment {

    public static final String ARG_REPORTE = "arg_reporte";

    public interface OnReportUpdatedListener {
        void onReportUpdated(ReportModel updatedReport);
    }

    private DialogReportDetailBinding binding;
    private ReportModel reporte;
    private ReporteRepository repository;
    private OnReportUpdatedListener updateListener;
    private boolean yaConfirmado = false;

    public static ReportDetailBottomSheet newInstance(ReportModel reporte) {
        ReportDetailBottomSheet fragment = new ReportDetailBottomSheet();
        Bundle args = new Bundle();
        args.putSerializable(ARG_REPORTE, reporte);
        fragment.setArguments(args);
        return fragment;
    }

    public void setOnReportUpdatedListener(OnReportUpdatedListener listener) {
        this.updateListener = listener;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = DialogReportDetailBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        repository = new ReporteRepository();

        if (getArguments() != null) {
            reporte = (ReportModel) getArguments().getSerializable(ARG_REPORTE);
        }

        if (reporte == null) {
            dismiss();
            return;
        }

        setupViews();
        setupListeners();
    }

    private void setupViews() {
        // Barrio y título
        String barrio = reporte.getBarrio() != null ? reporte.getBarrio() : "Santa Marta";
        binding.tvDetalleBarrio.setText(barrio);

        // Descripción
        String desc = reporte.getDescripcion() != null && !reporte.getDescripcion().isEmpty()
                ? reporte.getDescripcion()
                : "Sin descripción adicional proporcionada.";
        binding.tvDetalleDescripcion.setText(desc);

        // Badge Tipo
        String tipo = reporte.getTipo() != null ? reporte.getTipo() : "Reporte";
        binding.tvDetalleTipo.setText(tipo);
        int tipoColor = getTipoColor(tipo);
        binding.llDetalleTipoBadge.setBackgroundTintList(ColorStateList.valueOf(tipoColor));
        binding.ivDetalleTipo.setImageResource(getTipoIcon(tipo));

        // Badge Urgencia
        String urgencia = reporte.getUrgencia() != null ? reporte.getUrgencia() : "media";
        binding.tvDetalleUrgencia.setText("Urgencia " + capitalize(urgencia));
        int urgenciaColor = getUrgenciaColor(urgencia);
        binding.llDetalleUrgenciaBadge.setBackgroundTintList(ColorStateList.valueOf(urgenciaColor));

        // Badge Estado
        String estado = reporte.getEstado() != null ? reporte.getEstado() : "pendiente";
        binding.tvDetalleEstado.setText(capitalize(estado.replace("_", " ")));
        int estadoColor = getEstadoColor(estado);
        binding.llDetalleEstadoBadge.setBackgroundTintList(ColorStateList.valueOf(estadoColor));

        // Timestamps
        String tiempoRelativo = reporte.getTiempoRelativo() != null ? reporte.getTiempoRelativo() : "Reciente";
        binding.tvDetalleTiempoRelativo.setText(tiempoRelativo);

        String fechaExacta = reporte.getFechaFormateada();
        if (fechaExacta != null && !fechaExacta.isEmpty()) {
            binding.tvDetalleFechaExacta.setText(fechaExacta);
            binding.tvDetalleSeparadorFecha.setVisibility(View.VISIBLE);
            binding.tvDetalleFechaExacta.setVisibility(View.VISIBLE);
        } else {
            binding.tvDetalleSeparadorFecha.setVisibility(View.GONE);
            binding.tvDetalleFechaExacta.setVisibility(View.GONE);
        }

        // Coordenadas GPS
        if (reporte.getLat() != 0.0 || reporte.getLng() != 0.0) {
            binding.llDetalleCoordenadas.setVisibility(View.VISIBLE);
            binding.tvDetalleCoordenadas.setText(String.format(Locale.getDefault(), "GPS: %.4f° N, %.4f° O", reporte.getLat(), reporte.getLng()));
        } else {
            binding.llDetalleCoordenadas.setVisibility(View.GONE);
        }

        // Foto
        if (reporte.getFotoUrl() != null && !reporte.getFotoUrl().trim().isEmpty()) {
            binding.cardDetalleFoto.setVisibility(View.VISIBLE);
            Glide.with(this)
                    .load(reporte.getFotoUrl())
                    .placeholder(R.drawable.bg_photo_dropzone)
                    .centerCrop()
                    .into(binding.ivDetalleFoto);
        } else {
            binding.cardDetalleFoto.setVisibility(View.GONE);
        }

        // Apoyo comunitario
        actualizarTextoApoyo(reporte.getConfirmaciones());
    }

    private void setupListeners() {
        // Cerrar diálogo
        binding.btnCerrarDetalle.setOnClickListener(v -> dismiss());

        // Compartir en WhatsApp / Redes
        binding.btnCompartirDetalle.setOnClickListener(v -> {
            ShareHelper.compartirReporte(requireContext(), reporte);
        });

        // Confirmar / Apoyar reporte
        binding.btnConfirmarDetalle.setOnClickListener(v -> {
            if (yaConfirmado) {
                Toast.makeText(requireContext(), "Ya has apoyado este reporte", Toast.LENGTH_SHORT).show();
                return;
            }

            binding.btnConfirmarDetalle.setEnabled(false);
            repository.confirmarReporte(reporte.getId()).observe(getViewLifecycleOwner(), success -> {
                binding.btnConfirmarDetalle.setEnabled(true);
                if (Boolean.TRUE.equals(success)) {
                    yaConfirmado = true;
                    int nuevasConfirmaciones = reporte.getConfirmaciones() + 1;
                    reporte.setConfirmaciones(nuevasConfirmaciones);
                    actualizarTextoApoyo(nuevasConfirmaciones);

                    binding.btnConfirmarDetalle.setText("¡Apoyado!");
                    binding.btnConfirmarDetalle.setIconResource(R.drawable.ic_check_circle);
                    binding.btnConfirmarDetalle.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#22c55e")));

                    Toast.makeText(requireContext(), "¡Gracias! Tu apoyo visibiliza este problema en Santa Marta.", Toast.LENGTH_SHORT).show();

                    if (updateListener != null) {
                        updateListener.onReportUpdated(reporte);
                    }
                } else {
                    Toast.makeText(requireContext(), "No se pudo registrar tu apoyo. Intenta de nuevo.", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private void actualizarTextoApoyo(int total) {
        if (total <= 0) {
            binding.tvDetalleApoyoComunitario.setText("Sé el primero en confirmar este problema");
        } else if (total == 1) {
            binding.tvDetalleApoyoComunitario.setText("1 vecino de Santa Marta ha confirmado este problema");
        } else {
            binding.tvDetalleApoyoComunitario.setText(total + " vecinos de Santa Marta han confirmado este problema");
        }
    }

    private int getTipoColor(String tipo) {
        if (tipo == null) return ContextCompat.getColor(requireContext(), R.color.agua_primary);
        switch (tipo) {
            case "Sin agua": return ContextCompat.getColor(requireContext(), R.color.tipo_sin_agua);
            case "Baja presión": return ContextCompat.getColor(requireContext(), R.color.tipo_baja_presion);
            case "Agua sucia": return ContextCompat.getColor(requireContext(), R.color.tipo_agua_sucia);
            case "Fuga": return ContextCompat.getColor(requireContext(), R.color.tipo_fuga);
            default: return ContextCompat.getColor(requireContext(), R.color.agua_primary);
        }
    }

    private int getTipoIcon(String tipo) {
        if (tipo == null) return R.drawable.ic_tipo_sin_agua;
        switch (tipo) {
            case "Sin agua": return R.drawable.ic_tipo_sin_agua;
            case "Baja presión": return R.drawable.ic_tipo_baja_presion;
            case "Agua sucia": return R.drawable.ic_tipo_agua_sucia;
            case "Fuga": return R.drawable.ic_tipo_fuga;
            default: return R.drawable.ic_tipo_sin_agua;
        }
    }

    private int getUrgenciaColor(String urgencia) {
        if (urgencia == null) return ContextCompat.getColor(requireContext(), R.color.urgencia_media);
        switch (urgencia.toLowerCase()) {
            case "alta": return ContextCompat.getColor(requireContext(), R.color.urgencia_alta);
            case "baja": return ContextCompat.getColor(requireContext(), R.color.urgencia_baja);
            default: return ContextCompat.getColor(requireContext(), R.color.urgencia_media);
        }
    }

    private int getEstadoColor(String estado) {
        if (estado == null) return ContextCompat.getColor(requireContext(), R.color.estado_pendiente);
        switch (estado.toLowerCase()) {
            case "resuelto": return ContextCompat.getColor(requireContext(), R.color.estado_resuelto);
            case "en_proceso": return ContextCompat.getColor(requireContext(), R.color.estado_en_proceso);
            default: return ContextCompat.getColor(requireContext(), R.color.estado_pendiente);
        }
    }

    private String capitalize(String text) {
        if (text == null || text.isEmpty()) return "";
        return text.substring(0, 1).toUpperCase(Locale.getDefault()) + text.substring(1).toLowerCase(Locale.getDefault());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
