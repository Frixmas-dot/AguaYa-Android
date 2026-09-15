package com.frix.aguaya.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.frix.aguaya.R;
import com.frix.aguaya.adapters.AdminReportAdapter;
import com.frix.aguaya.databinding.FragmentAdminBinding;
import com.frix.aguaya.viewmodels.AdminViewModel;
import com.google.android.material.snackbar.Snackbar;

public class AdminFragment extends Fragment {

    private FragmentAdminBinding binding;
    private AdminViewModel viewModel;
    private AdminReportAdapter adapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentAdminBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(AdminViewModel.class);

        setupLogin();
        setupAdminPanel();
        setupObservers();
    }

    private void setupLogin() {
        binding.btnAdminLogin.setOnClickListener(v -> {
            String password = binding.etAdminPassword.getText() != null ? binding.etAdminPassword.getText().toString() : "";
            if (!viewModel.login(password)) {
                binding.tvLoginError.setVisibility(View.VISIBLE);
            } else {
                binding.tvLoginError.setVisibility(View.GONE);
                binding.etAdminPassword.setText("");
            }
        });

        binding.btnLogout.setOnClickListener(v -> viewModel.logout());
    }

    private void setupAdminPanel() {
        adapter = new AdminReportAdapter(requireContext(), new AdminReportAdapter.OnAdminActionListener() {
            @Override
            public void onCambiarEstado(int id, String estado) {
                viewModel.cambiarEstado(id, estado);
            }

            @Override
            public void onEliminar(int id) {
                new AlertDialog.Builder(requireContext())
                    .setTitle("Eliminar reporte")
                    .setMessage("¿Estás seguro de que deseas eliminar este reporte?")
                    .setPositiveButton("Eliminar", (dialog, which) -> viewModel.eliminarReporte(id))
                    .setNegativeButton("Cancelar", null)
                    .show();
            }
        });

        binding.rvAdminReportes.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvAdminReportes.setAdapter(adapter);

        binding.swipeRefreshAdmin.setOnRefreshListener(() -> viewModel.cargarReportes());

        binding.chipGroupEstado.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) return;
            int checkedId = checkedIds.get(0);
            String filtro = null;
            if (checkedId == R.id.chipPendientes) filtro = "pendiente";
            else if (checkedId == R.id.chipEnProceso) filtro = "en_proceso";
            else if (checkedId == R.id.chipResueltos) filtro = "resuelto";
            
            viewModel.filtroEstado.setValue(filtro);
            viewModel.cargarReportes();
        });
    }

    private void setupObservers() {
        viewModel.getAutenticado().observe(getViewLifecycleOwner(), auth -> {
            if (auth != null && auth) {
                binding.loginContainer.setVisibility(View.GONE);
                binding.adminPanel.setVisibility(View.VISIBLE);
                viewModel.cargarReportes();
            } else {
                binding.loginContainer.setVisibility(View.VISIBLE);
                binding.adminPanel.setVisibility(View.GONE);
            }
        });

        viewModel.getReportes().observe(getViewLifecycleOwner(), reportes -> {
            if (reportes != null) {
                adapter.setReportes(reportes);
                binding.emptyStateAdmin.setVisibility(reportes.isEmpty() ? View.VISIBLE : View.GONE);
            }
        });

        viewModel.getCargando().observe(getViewLifecycleOwner(), cargando -> {
            binding.swipeRefreshAdmin.setRefreshing(cargando);
            binding.progressAdmin.setVisibility(cargando && !binding.swipeRefreshAdmin.isRefreshing() ? View.VISIBLE : View.GONE);
        });

        viewModel.getMensaje().observe(getViewLifecycleOwner(), mensaje -> {
            if (mensaje != null && !mensaje.isEmpty()) {
                Snackbar.make(binding.getRoot(), mensaje, Snackbar.LENGTH_SHORT).show();
            }
        });
    }
}
