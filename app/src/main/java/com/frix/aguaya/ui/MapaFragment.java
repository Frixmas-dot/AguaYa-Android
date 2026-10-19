package com.frix.aguaya.ui;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.frix.aguaya.R;
import com.frix.aguaya.adapters.ReportCardAdapter;
import com.frix.aguaya.databinding.FragmentMapaBinding;
import com.frix.aguaya.models.ReportModel;
import com.frix.aguaya.viewmodels.MapaViewModel;
import com.google.android.material.bottomsheet.BottomSheetBehavior;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.Marker;

import java.util.List;

public class MapaFragment extends Fragment {

    private FragmentMapaBinding binding;
    private MapaViewModel viewModel;
    private ReportCardAdapter adapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Configuration.getInstance().setUserAgentValue(requireContext().getPackageName());
        Configuration.getInstance().load(requireContext(), requireContext().getSharedPreferences("osmdroid", Context.MODE_PRIVATE));
        binding = FragmentMapaBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(MapaViewModel.class);

        setupMap();
        setupBottomSheet();
        setupFilters();
        setupObservers();

        viewModel.cargarReportes();
        viewModel.cargarClima();
    }

    private void setupMap() {
        binding.mapView.setTileSource(TileSourceFactory.MAPNIK);
        binding.mapView.getController().setZoom(13.0);
        binding.mapView.getController().setCenter(new GeoPoint(11.2408, -74.199));
        binding.mapView.setMultiTouchControls(true);
    }

    private void setupBottomSheet() {
        adapter = new ReportCardAdapter(requireContext(), new ReportCardAdapter.OnReportClickListener() {
            @Override
            public void onReportClick(ReportModel reporte) {
                mostrarDetalleReporte(reporte);
            }

            @Override
            public void onConfirmarClick(ReportModel reporte, int position) {
                new com.frix.aguaya.repositories.ReporteRepository().confirmarReporte(reporte.getId())
                        .observe(getViewLifecycleOwner(), success -> {
                            if (Boolean.TRUE.equals(success)) {
                                reporte.setConfirmaciones(reporte.getConfirmaciones() + 1);
                                adapter.notifyItemChanged(position);
                            }
                        });
            }
        });
        
        binding.rvReportesMapa.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvReportesMapa.setAdapter(adapter);

        BottomSheetBehavior<View> behavior = BottomSheetBehavior.from(binding.bottomSheet);
        binding.btnCarrotanquesMapa.setOnClickListener(v -> {
            RutasCarrotanquesDialog.newInstance().show(getChildFragmentManager(), "RutasCarrotanques");
        });
        behavior.setPeekHeight(600);
    }

    private boolean modoSemaforoActivo = false;

    private void setupFilters() {
        binding.chipGroupFiltros.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) return;
            int checkedId = checkedIds.get(0);

            if (checkedId == R.id.chipSemaforo) {
                modoSemaforoActivo = true;
                viewModel.setFiltro(null);
                List<ReportModel> current = viewModel.getReportes().getValue();
                if (current != null) {
                    renderizarSemaforo(current);
                }
                return;
            }

            modoSemaforoActivo = false;
            String filtro = null;
            if (checkedId == R.id.chipSinAgua) filtro = "Sin agua";
            else if (checkedId == R.id.chipBajaPresion) filtro = "Baja presión";
            else if (checkedId == R.id.chipAguaSucia) filtro = "Agua sucia";
            else if (checkedId == R.id.chipFuga) filtro = "Fuga";
            
            viewModel.setFiltro(filtro);
        });
    }

    private void setupObservers() {
        viewModel.getReportes().observe(getViewLifecycleOwner(), reportes -> {
            if (reportes != null) {
                binding.tvReportCount.setText(String.valueOf(reportes.size()));
                adapter.setReportes(reportes);
                if (modoSemaforoActivo) {
                    renderizarSemaforo(reportes);
                } else {
                    updateMapMarkers(reportes);
                }
                boolean offline = com.frix.aguaya.utils.OfflineCacheManager.isOfflineFallbackActive();
                binding.bannerModoOffline.setVisibility(offline ? View.VISIBLE : View.GONE);
            }
        });

        viewModel.getClima().observe(getViewLifecycleOwner(), weather -> {
            if (weather != null) {
                binding.weatherBanner.setVisibility(View.VISIBLE);
                binding.weatherBanner.setWeatherData(weather);
            } else {
                binding.weatherBanner.setVisibility(View.GONE);
            }
        });

        viewModel.getCargando().observe(getViewLifecycleOwner(), cargando -> {
            if (cargando != null) {
                binding.progressMapa.setVisibility(cargando ? View.VISIBLE : View.GONE);
            }
        });
    }

    private void updateMapMarkers(List<ReportModel> reportes) {
        binding.mapView.getOverlays().clear();
        for (ReportModel reporte : reportes) {
            if (reporte.getLat() == 0.0 && reporte.getLng() == 0.0) continue;
            Marker marker = new Marker(binding.mapView);
            marker.setPosition(new GeoPoint(reporte.getLat(), reporte.getLng()));
            marker.setTitle(reporte.getBarrio());
            marker.setSnippet(reporte.getTipo() + " - " + reporte.getDescripcion());
            marker.setIcon(createMarkerIcon(reporte.getTipo()));
            marker.setOnMarkerClickListener((m, mapView) -> {
                mostrarDetalleReporte(reporte);
                return true;
            });
            binding.mapView.getOverlays().add(marker);
        }
        binding.mapView.invalidate();
    }

    private void mostrarDetalleReporte(ReportModel reporte) {
        if (reporte == null || !isAdded()) return;
        ReportDetailBottomSheet sheet = ReportDetailBottomSheet.newInstance(reporte);
        sheet.setOnReportUpdatedListener(updatedReport -> {
            if (adapter != null) {
                adapter.notifyDataSetChanged();
            }
        });
        sheet.show(getChildFragmentManager(), "ReportDetailBottomSheet");
    }

    private void renderizarSemaforo(List<ReportModel> reportes) {
        binding.mapView.getOverlays().clear();
        if (reportes == null || reportes.isEmpty()) {
            binding.mapView.invalidate();
            return;
        }

        // Agrupar por barrio para contar incidencias y calcular centroide
        java.util.Map<String, List<ReportModel>> porBarrio = new java.util.HashMap<>();
        for (ReportModel r : reportes) {
            String barrio = r.getBarrio() != null ? r.getBarrio() : "Desconocido";
            if (!porBarrio.containsKey(barrio)) {
                porBarrio.put(barrio, new java.util.ArrayList<>());
            }
            porBarrio.get(barrio).add(r);
        }

        for (java.util.Map.Entry<String, List<ReportModel>> entry : porBarrio.entrySet()) {
            String barrio = entry.getKey();
            List<ReportModel> lista = entry.getValue();
            int count = lista.size();

            double sumLat = 0, sumLng = 0;
            int validCoords = 0;
            for (ReportModel r : lista) {
                if (r.getLat() != 0.0 && r.getLng() != 0.0) {
                    sumLat += r.getLat();
                    sumLng += r.getLng();
                    validCoords++;
                }
            }

            if (validCoords == 0) continue;
            double centerLat = sumLat / validCoords;
            double centerLng = sumLng / validCoords;
            GeoPoint centerPoint = new GeoPoint(centerLat, centerLng);

            int fillColor;
            int strokeColor;
            String nivelAlerta;
            double radiusMeters;

            if (count >= 3) {
                fillColor = Color.argb(65, 239, 68, 68);
                strokeColor = Color.parseColor("#EF4444");
                nivelAlerta = "🚨 ALERTA ROJA (Zona Crítica)";
                radiusMeters = 850.0;
            } else if (count == 2) {
                fillColor = Color.argb(65, 249, 115, 22);
                strokeColor = Color.parseColor("#F97316");
                nivelAlerta = "⚠️ ALERTA NARANJA (Moderada)";
                radiusMeters = 650.0;
            } else {
                fillColor = Color.argb(45, 14, 165, 233);
                strokeColor = Color.parseColor("#0EA5E9");
                nivelAlerta = "🟢 ALERTA LEVE (Seguimiento)";
                radiusMeters = 450.0;
            }

            org.osmdroid.views.overlay.Polygon circle = new org.osmdroid.views.overlay.Polygon(binding.mapView);
            circle.setPoints(org.osmdroid.views.overlay.Polygon.pointsAsCircle(centerPoint, radiusMeters));
            circle.setFillColor(fillColor);
            circle.setStrokeColor(strokeColor);
            circle.setStrokeWidth(3.5f);
            circle.setTitle(nivelAlerta + ": " + barrio);
            circle.setSnippet(count + " reportes acumulados");
            binding.mapView.getOverlays().add(circle);

            Marker marker = new Marker(binding.mapView);
            marker.setPosition(centerPoint);
            marker.setTitle(nivelAlerta);
            marker.setSnippet(barrio + " — " + count + " incidencias registradas");
            marker.setIcon(createMarkerIcon(count >= 3 ? "Fuga" : (count == 2 ? "Baja presión" : "Sin agua")));
            marker.setOnMarkerClickListener((m, mapView) -> {
                mostrarDetalleReporte(lista.get(0));
                return true;
            });
            binding.mapView.getOverlays().add(marker);
        }

        binding.mapView.invalidate();
    }

    private ShapeDrawable createMarkerIcon(String tipo) {
        ShapeDrawable oval = new ShapeDrawable(new OvalShape());
        oval.setIntrinsicHeight(40);
        oval.setIntrinsicWidth(40);
        int color;
        switch (tipo != null ? tipo : "") {
            case "Sin agua": color = Color.parseColor("#0ea5e9"); break;
            case "Baja presión": color = Color.parseColor("#f97316"); break;
            case "Agua sucia": color = Color.parseColor("#a16207"); break;
            case "Fuga": color = Color.parseColor("#ef4444"); break;
            default: color = Color.parseColor("#0c4a6e"); break;
        }
        oval.getPaint().setColor(color);
        oval.getPaint().setStyle(Paint.Style.FILL);
        oval.getPaint().setAntiAlias(true);
        return oval;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (binding != null) binding.mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (binding != null) binding.mapView.onPause();
    }
}
