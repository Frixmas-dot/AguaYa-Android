package com.frix.aguaya.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.frix.aguaya.R;
import com.frix.aguaya.adapters.RutaCarrotanqueAdapter;
import com.frix.aguaya.databinding.DialogRutasCarrotanquesBinding;
import com.frix.aguaya.models.RutaCarrotanqueModel;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.ArrayList;
import java.util.List;

public class RutasCarrotanquesDialog extends BottomSheetDialogFragment {

    private DialogRutasCarrotanquesBinding binding;
    private RutaCarrotanqueAdapter adapter;
    private final List<RutaCarrotanqueModel> todasLasRutas = new ArrayList<>();

    public static RutasCarrotanquesDialog newInstance() {
        return new RutasCarrotanquesDialog();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = DialogRutasCarrotanquesBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        adapter = new RutaCarrotanqueAdapter(requireContext());
        binding.rvRutasCarrotanques.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvRutasCarrotanques.setAdapter(adapter);

        binding.btnCerrarRutas.setOnClickListener(v -> dismiss());

        cargarRutasDemo();
        setupFiltros();
    }

    private void setupFiltros() {
        binding.chipGroupFiltroRutas.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) return;
            int id = checkedIds.get(0);
            if (id == R.id.chipRutaEnCamino) {
                filtrarPorEstado("en_camino");
            } else if (id == R.id.chipRutaProgramadas) {
                filtrarPorEstado("programada");
            } else if (id == R.id.chipRutaCompletadas) {
                filtrarPorEstado("completada");
            } else {
                adapter.setRutas(todasLasRutas);
            }
        });
    }

    private void filtrarPorEstado(String estadoDeseado) {
        List<RutaCarrotanqueModel> filtradas = new ArrayList<>();
        for (RutaCarrotanqueModel r : todasLasRutas) {
            if (estadoDeseado.equalsIgnoreCase(r.getEstado())) {
                filtradas.add(r);
            }
        }
        adapter.setRutas(filtradas);
    }

    private void cargarRutasDemo() {
        todasLasRutas.clear();

        todasLasRutas.add(new RutaCarrotanqueModel(1, "Bastidas", "Manzanas 12 a 16",
                "08:30 AM – 11:30 AM", "12.000 Litros", "en_camino",
                "Frente a la Cancha Comunitaria",
                "Prioridad a familias con adultos mayores y colegios.",
                "Carrotanque Cisterna Essmar Móvil #04"));

        todasLasRutas.add(new RutaCarrotanqueModel(2, "Pescaíto", "Sector Calle 7 con Cra 2",
                "09:00 AM – 12:00 PM", "10.000 Litros", "en_camino",
                "Puesto de salud La Castellana",
                "Entrega organizada por turnos con líderes de la JAC.",
                "Alcaldía Distrital Móvil #02"));

        todasLasRutas.add(new RutaCarrotanqueModel(3, "Taganga", "Sector Playas y Parte Alta",
                "02:00 PM – 05:30 PM", "15.000 Litros", "programada",
                "Mirador de entrada a Taganga",
                "Apoyo especial para pescadores artesanales y comedores.",
                "Cruz Roja Seccional Magdalena #01"));

        todasLasRutas.add(new RutaCarrotanqueModel(4, "Mamatoco", "Sector Quinta de San Pedro",
                "03:30 PM – 06:30 PM", "10.000 Litros", "programada",
                "Parque central de Mamatoco",
                "Abastecimiento directo a tanques comunitarios y escuela.",
                "Essmar Móvil Cisterna #01"));

        todasLasRutas.add(new RutaCarrotanqueModel(5, "San Fernando", "Parte Alta (Manzanas 3 a 7)",
                "04:00 PM – 07:00 PM", "12.000 Litros", "programada",
                "Tanque elevado comunal de San Fernando",
                "Llevar recipientes plásticos limpios con tapa.",
                "Essmar Móvil Cisterna #06"));

        todasLasRutas.add(new RutaCarrotanqueModel(6, "Gaira", "Sector Las Palmeras",
                "06:00 AM – 08:30 AM", "8.000 Litros", "completada",
                "Colegio Rodrigo de Bastidas",
                "Distribución finalizada satisfactoriamente.",
                "Cisterna Solidaria #03"));

        adapter.setRutas(todasLasRutas);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
