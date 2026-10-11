package com.frix.aguaya.ui;

import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.frix.aguaya.R;
import com.frix.aguaya.databinding.FragmentReportarBinding;
import com.frix.aguaya.viewmodels.ReportarViewModel;
import com.google.android.material.snackbar.Snackbar;

public class ReportarFragment extends Fragment {

    private FragmentReportarBinding binding;
    private ReportarViewModel viewModel;
    
    private final ActivityResultLauncher<String> pickImage = 
        registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
            if (uri != null) {
                viewModel.fotoUri.setValue(uri);
                mostrarFotoPreview(uri);
            }
        });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentReportarBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        viewModel = new ViewModelProvider(this).get(ReportarViewModel.class);
        
        // Handle pre-selected argument
        if (getArguments() != null && getArguments().containsKey("tipo_preseleccionado")) {
            String tipo = getArguments().getString("tipo_preseleccionado");
            seleccionarTipo(tipo);
        }

        setupListeners();
        observeViewModel();
    }

    private static final String[] BARRIOS_SANTA_MARTA = {
        "17 de Diciembre", "20 de Julio", "Alcudia", "Bavaria",
        "Bello Horizonte", "Bolivariana", "Bonda", "Boulevard de las Rosas",
        "Cantilito", "Centro Histórico", "Chimila", "Ciudad Equidad",
        "Cristo Rey", "El Cundí", "El Mayor", "El Parque", "El Pradito",
        "El Rodadero", "Gaira", "Garagoa", "Jardín", "La Concepción",
        "La Esperanza", "La Paz", "Las Américas", "Los Almendros",
        "Los Ángeles", "Los Olivos", "Mamatoco", "Manzanares", "María Eugenia",
        "Minuto de Dios", "Nacho Vives", "Obrero", "Once de Noviembre",
        "Pastrana", "Pescaíto", "Playa Salguero", "Pozos Colorados",
        "San Fernando", "San Francisco", "San Jorge", "San Martín", "San Pablo",
        "Santa Ana", "Santa Cruz", "Santa Fe", "Taganga", "Taminaca",
        "Timayui", "Urbanización Riascos", "Villa Bella", "Villa Concha",
        "Villa del Carmen", "Villa del Río", "Vista Hermosa"
    };

    private void setupBarrioAutocomplete() {
        android.widget.ArrayAdapter<String> adapter = new android.widget.ArrayAdapter<>(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            BARRIOS_SANTA_MARTA
        );
        binding.etBarrio.setAdapter(adapter);
    }

    private void setupListeners() {
        setupBarrioAutocomplete();
        // Tipo Selectors
        binding.cardTipoSinAgua.setOnClickListener(v -> seleccionarTipo("Sin agua"));
        binding.cardTipoBajaPresion.setOnClickListener(v -> seleccionarTipo("Baja presión"));
        binding.cardTipoAguaSucia.setOnClickListener(v -> seleccionarTipo("Agua sucia"));
        binding.cardTipoFuga.setOnClickListener(v -> seleccionarTipo("Fuga"));

        // Urgencia Selectors
        binding.btnUrgenciaBaja.setOnClickListener(v -> seleccionarUrgencia("baja"));
        binding.btnUrgenciaMedia.setOnClickListener(v -> seleccionarUrgencia("media"));
        binding.btnUrgenciaAlta.setOnClickListener(v -> seleccionarUrgencia("alta"));
        
        // Default Urgencia
        if (viewModel.urgencia.getValue() == null) {
            seleccionarUrgencia("media");
        }

        // Photo Picker
        binding.photoDropzone.setOnClickListener(v -> pickImage.launch("image/*"));
        binding.btnRemoverFoto.setOnClickListener(v -> {
            viewModel.fotoUri.setValue(null);
            ocultarFotoPreview();
        });

        // Submit Button
        binding.btnEnviar.setOnClickListener(v -> enviarReporte());
        
        // Validation Listeners
        binding.etBarrio.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                binding.tilBarrio.setError(null);
            }
        });
        
        binding.etDescripcion.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                binding.tilDescripcion.setError(null);
            }
        });
    }

    private void seleccionarTipo(String tipo) {
        viewModel.tipoSeleccionado.setValue(tipo);
        
        // Reset all
        binding.cardTipoSinAgua.setAlpha(0.5f);
        binding.cardTipoBajaPresion.setAlpha(0.5f);
        binding.cardTipoAguaSucia.setAlpha(0.5f);
        binding.cardTipoFuga.setAlpha(0.5f);
        
        // Select one
        switch (tipo) {
            case "Sin agua": binding.cardTipoSinAgua.setAlpha(1.0f); break;
            case "Baja presión": binding.cardTipoBajaPresion.setAlpha(1.0f); break;
            case "Agua sucia": binding.cardTipoAguaSucia.setAlpha(1.0f); break;
            case "Fuga": binding.cardTipoFuga.setAlpha(1.0f); break;
        }
    }

    private void seleccionarUrgencia(String urgencia) {
        viewModel.urgencia.setValue(urgencia);
        
        binding.btnUrgenciaBaja.setAlpha(0.5f);
        binding.btnUrgenciaMedia.setAlpha(0.5f);
        binding.btnUrgenciaAlta.setAlpha(0.5f);
        
        switch (urgencia) {
            case "baja": binding.btnUrgenciaBaja.setAlpha(1.0f); break;
            case "media": binding.btnUrgenciaMedia.setAlpha(1.0f); break;
            case "alta": binding.btnUrgenciaAlta.setAlpha(1.0f); break;
        }
    }

    private void mostrarFotoPreview(Uri uri) {
        binding.ivFotoPreview.setImageURI(uri);
        binding.ivFotoPreview.setVisibility(View.VISIBLE);
        binding.btnRemoverFoto.setVisibility(View.VISIBLE);
    }

    private void ocultarFotoPreview() {
        binding.ivFotoPreview.setImageURI(null);
        binding.ivFotoPreview.setVisibility(View.GONE);
        binding.btnRemoverFoto.setVisibility(View.GONE);
    }

    private void enviarReporte() {
        boolean valid = true;
        
        String barrio = binding.etBarrio.getText() != null ? binding.etBarrio.getText().toString().trim() : "";
        String direccion = binding.etDireccion.getText() != null ? binding.etDireccion.getText().toString().trim() : "";
        String descripcion = binding.etDescripcion.getText() != null ? binding.etDescripcion.getText().toString().trim() : "";
        
        if (barrio.isEmpty()) {
            binding.tilBarrio.setError("El barrio es requerido");
            valid = false;
        }
        
        if (descripcion.length() < 10) {
            binding.tilDescripcion.setError("La descripción debe tener al menos 10 caracteres");
            valid = false;
        }
        
        String tipoValue = viewModel.tipoSeleccionado.getValue();
        if (tipoValue == null || tipoValue.isEmpty()) {
            Snackbar.make(binding.getRoot(), "Selecciona el tipo de problema", Snackbar.LENGTH_SHORT).show();
            valid = false;
        }
        
        if (valid) {
            viewModel.barrio.setValue(barrio);
            viewModel.direccion.setValue(direccion);
            viewModel.descripcion.setValue(descripcion);
            viewModel.enviarReporte(requireContext().getContentResolver());
        }
    }

    private void observeViewModel() {
        viewModel.getEnviando().observe(getViewLifecycleOwner(), cargando -> {
            if (cargando != null) {
                binding.progressReportar.setVisibility(cargando ? View.VISIBLE : View.GONE);
                binding.btnEnviar.setEnabled(!cargando);
                binding.formContainer.setAlpha(cargando ? 0.5f : 1.0f);
            }
        });

        viewModel.getReporteCreado().observe(getViewLifecycleOwner(), reporte -> {
            if (reporte != null) {
                mostrarExito();
                viewModel.resetFormulario();
            }
        });

        viewModel.getError().observe(getViewLifecycleOwner(), error -> {
            if (error != null) {
                Snackbar.make(binding.getRoot(), error, Snackbar.LENGTH_LONG).show();
            }
        });
    }

    private void mostrarExito() {
        binding.successOverlay.setVisibility(View.VISIBLE);
        
        binding.btnVerMapa.setOnClickListener(v -> 
            Navigation.findNavController(requireView()).navigate(R.id.mapaFragment)
        );
        
        binding.btnHacerOtro.setOnClickListener(v -> {
            binding.successOverlay.setVisibility(View.GONE);
            resetForm();
        });
    }

    private void resetForm() {
        binding.etBarrio.setText("");
        binding.etDireccion.setText("");
        binding.etDescripcion.setText("");
        viewModel.fotoUri.setValue(null);
        ocultarFotoPreview();
        seleccionarUrgencia("media");
        // Deseleccionar tipo
        viewModel.tipoSeleccionado.setValue(null);
        binding.cardTipoSinAgua.setAlpha(0.5f);
        binding.cardTipoBajaPresion.setAlpha(0.5f);
        binding.cardTipoAguaSucia.setAlpha(0.5f);
        binding.cardTipoFuga.setAlpha(0.5f);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
    
    private abstract class SimpleTextWatcher implements TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        @Override
        public void afterTextChanged(Editable s) {}
    }
}
