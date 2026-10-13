package com.frix.aguaya.ui;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.frix.aguaya.adapters.BarrioStatsAdapter;
import com.frix.aguaya.databinding.FragmentEstadisticasBinding;
import com.frix.aguaya.models.EstadisticasModel;
import com.frix.aguaya.viewmodels.EstadisticasViewModel;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.PercentFormatter;

import java.util.ArrayList;
import java.util.List;

public class EstadisticasFragment extends Fragment {

    private FragmentEstadisticasBinding binding;
    private EstadisticasViewModel viewModel;
    private BarrioStatsAdapter adapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentEstadisticasBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(EstadisticasViewModel.class);

        adapter = new BarrioStatsAdapter(requireContext());
        binding.rvBarrios.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvBarrios.setAdapter(adapter);

        binding.swipeRefreshStats.setOnRefreshListener(() -> viewModel.cargarEstadisticas());

        setupCharts();
        setupObservers();

        viewModel.cargarEstadisticas();
    }

    private void setupCharts() {
        binding.chartTipos.getDescription().setEnabled(false);
        binding.chartTipos.getLegend().setEnabled(false);
        binding.chartTipos.getAxisRight().setEnabled(false);
        binding.chartTipos.getAxisLeft().setAxisMinimum(0f);
        binding.chartTipos.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        binding.chartTipos.getXAxis().setDrawGridLines(false);
        binding.chartTipos.setDrawValueAboveBar(true);

        binding.chartResolucion.getDescription().setEnabled(false);
        binding.chartResolucion.setDrawHoleEnabled(true);
        binding.chartResolucion.setHoleRadius(60f);
        binding.chartResolucion.setTransparentCircleRadius(65f);
        binding.chartResolucion.setCenterTextSize(20f);
        binding.chartResolucion.getLegend().setEnabled(false);
        binding.chartResolucion.setUsePercentValues(true);
    }

    private void setupObservers() {
        viewModel.getEstadisticas().observe(getViewLifecycleOwner(), stats -> {
            if (stats == null) return;
            
            binding.tvKpiReportes.setText(String.valueOf(stats.getTotalReportes()));
            binding.tvKpiBarrios.setText(String.valueOf(stats.getBarriosUnicos()));
            binding.tvKpiResueltos.setText(String.format("%.1f%%", stats.getPctResueltos()));
            binding.tvKpiTiempo.setText("2.4 días");
            
            updateBarChart(stats.getPorTipo());
            updatePieChart(stats);
            adapter.setBarrios(stats.getPorBarrio());
        });

        viewModel.getCargando().observe(getViewLifecycleOwner(), cargando -> {
            binding.swipeRefreshStats.setRefreshing(cargando);
            binding.progressEstadisticas.setVisibility(cargando && !binding.swipeRefreshStats.isRefreshing() ? View.VISIBLE : View.GONE);
        });
    }

    private void updateBarChart(List<EstadisticasModel.TipoCount> porTipo) {
        if (porTipo == null || porTipo.isEmpty()) return;

        ArrayList<BarEntry> entries = new ArrayList<>();
        ArrayList<String> labels = new ArrayList<>();
        ArrayList<Integer> colors = new ArrayList<>();

        for (int i = 0; i < porTipo.size(); i++) {
            EstadisticasModel.TipoCount stat = porTipo.get(i);
            entries.add(new BarEntry(i, stat.getCantidad()));
            labels.add(stat.getTipo());
            
            int color;
            switch (stat.getTipo() != null ? stat.getTipo() : "") {
                case "Sin agua": color = Color.parseColor("#0ea5e9"); break;
                case "Baja presión": color = Color.parseColor("#f97316"); break;
                case "Agua sucia": color = Color.parseColor("#a16207"); break;
                case "Fuga": color = Color.parseColor("#ef4444"); break;
                default: color = Color.parseColor("#0c4a6e"); break;
            }
            colors.add(color);
        }

        BarDataSet dataSet = new BarDataSet(entries, "Tipos");
        dataSet.setColors(colors);
        dataSet.setValueTextSize(12f);

        BarData data = new BarData(dataSet);
        data.setBarWidth(0.6f);

        binding.chartTipos.setData(data);
        binding.chartTipos.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));
        binding.chartTipos.getXAxis().setLabelCount(labels.size());
        binding.chartTipos.invalidate();
    }

    private void updatePieChart(EstadisticasModel stats) {
        ArrayList<PieEntry> entries = new ArrayList<>();
        ArrayList<Integer> colors = new ArrayList<>();

        if (stats.getPctResueltos() > 0) {
            entries.add(new PieEntry((float) stats.getPctResueltos(), "Resueltos"));
            colors.add(Color.parseColor("#22c55e"));
        }
        if (stats.getPctEnProceso() > 0) {
            entries.add(new PieEntry((float) stats.getPctEnProceso(), "En proceso"));
            colors.add(Color.parseColor("#f97316"));
        }
        if (stats.getPctPendientes() > 0) {
            entries.add(new PieEntry((float) stats.getPctPendientes(), "Pendientes"));
            colors.add(Color.parseColor("#ef4444"));
        }

        if (entries.isEmpty()) {
            entries.add(new PieEntry(100f, "Sin datos"));
            colors.add(Color.parseColor("#94a3b8"));
        }

        PieDataSet dataSet = new PieDataSet(entries, "Estados");
        dataSet.setColors(colors);
        dataSet.setValueTextSize(14f);
        dataSet.setValueTextColor(Color.WHITE);

        PieData data = new PieData(dataSet);
        data.setValueFormatter(new PercentFormatter(binding.chartResolucion));

        binding.chartResolucion.setData(data);
        binding.chartResolucion.setCenterText(String.format("%.1f%%", stats.getPctResueltos()));
        binding.chartResolucion.invalidate();
    }
}
