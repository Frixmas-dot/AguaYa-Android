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
                // Not needed for map
            }

            @Override
            public void onConfirmarClick(ReportModel reporte, int id) {
                // Handle confirm action if supported in this view
            }
        });
        
        binding.rvReportesMapa.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvReportesMapa.setAdapter(adapter);

        BottomSheetBehavior<View> behavior = BottomSheetBehavior.from(binding.bottomSheet);
        behavior.setPeekHeight(600);
    }

    private void setupFilters() {
        binding.chipGroupFiltros.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) return;
            int checkedId = checkedIds.get(0);
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
                updateMapMarkers(reportes);
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
            Marker marker = new Marker(binding.mapView);
            marker.setPosition(new GeoPoint(reporte.getLat(), reporte.getLng()));
            marker.setTitle(reporte.getBarrio());
            marker.setSnippet(reporte.getTipo() + " - " + reporte.getDescripcion());
            marker.setIcon(createMarkerIcon(reporte.getTipo()));
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
