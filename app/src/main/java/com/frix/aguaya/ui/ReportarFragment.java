package com.frix.aguaya.ui;

import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavOptions;
import androidx.navigation.Navigation;
import com.google.android.material.bottomnavigation.BottomNavigationView;

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

    private final ActivityResultLauncher<String[]> requestLocationPermission =
        registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
            Boolean fineLocation = result.getOrDefault(android.Manifest.permission.ACCESS_FINE_LOCATION, false);
            Boolean coarseLocation = result.getOrDefault(android.Manifest.permission.ACCESS_COARSE_LOCATION, false);
            if ((fineLocation != null && fineLocation) || (coarseLocation != null && coarseLocation)) {
                obtenerUbicacionActual();
            } else {
                Snackbar.make(binding.getRoot(), "Permiso de ubicación denegado", Snackbar.LENGTH_SHORT).show();
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

        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (binding != null && binding.successOverlay.getVisibility() == View.VISIBLE) {
                    binding.successOverlay.setVisibility(View.GONE);
                    resetForm();
                    viewModel.resetFormulario();
                    BottomNavigationView bottomNav = requireActivity().findViewById(R.id.bottom_nav);
                    if (bottomNav != null) {
                        bottomNav.setSelectedItemId(R.id.homeFragment);
                    }
                } else {
                    setEnabled(false);
                    requireActivity().getOnBackPressedDispatcher().onBackPressed();
                }
            }
        });
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

        // GPS Listener
        binding.btnUsarUbicacion.setOnClickListener(v -> iniciarCapturaUbicacion());

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
        
        int strokeDefault = android.graphics.Color.parseColor("#E2E8F0");
        int bgDefault = android.graphics.Color.WHITE;
        
        // Reset all
        binding.cardTipoSinAgua.setStrokeColor(strokeDefault);
        binding.cardTipoSinAgua.setCardBackgroundColor(bgDefault);
        binding.cardTipoSinAgua.setStrokeWidth(dpToPx(1));
        binding.cardTipoSinAgua.setCardElevation(dpToPx(1));

        binding.cardTipoBajaPresion.setStrokeColor(strokeDefault);
        binding.cardTipoBajaPresion.setCardBackgroundColor(bgDefault);
        binding.cardTipoBajaPresion.setStrokeWidth(dpToPx(1));
        binding.cardTipoBajaPresion.setCardElevation(dpToPx(1));

        binding.cardTipoAguaSucia.setStrokeColor(strokeDefault);
        binding.cardTipoAguaSucia.setCardBackgroundColor(bgDefault);
        binding.cardTipoAguaSucia.setStrokeWidth(dpToPx(1));
        binding.cardTipoAguaSucia.setCardElevation(dpToPx(1));

        binding.cardTipoFuga.setStrokeColor(strokeDefault);
        binding.cardTipoFuga.setCardBackgroundColor(bgDefault);
        binding.cardTipoFuga.setStrokeWidth(dpToPx(1));
        binding.cardTipoFuga.setCardElevation(dpToPx(1));

        // Highlight selected
        if ("Sin agua".equals(tipo)) {
            binding.cardTipoSinAgua.setStrokeColor(android.graphics.Color.parseColor("#0EA5E9"));
            binding.cardTipoSinAgua.setCardBackgroundColor(android.graphics.Color.parseColor("#F0F9FF"));
            binding.cardTipoSinAgua.setStrokeWidth(dpToPx(2));
            binding.cardTipoSinAgua.setCardElevation(dpToPx(4));
        } else if ("Baja presión".equals(tipo)) {
            binding.cardTipoBajaPresion.setStrokeColor(android.graphics.Color.parseColor("#F97316"));
            binding.cardTipoBajaPresion.setCardBackgroundColor(android.graphics.Color.parseColor("#FFF7ED"));
            binding.cardTipoBajaPresion.setStrokeWidth(dpToPx(2));
            binding.cardTipoBajaPresion.setCardElevation(dpToPx(4));
        } else if ("Agua sucia".equals(tipo)) {
            binding.cardTipoAguaSucia.setStrokeColor(android.graphics.Color.parseColor("#A16207"));
            binding.cardTipoAguaSucia.setCardBackgroundColor(android.graphics.Color.parseColor("#FEFCE8"));
            binding.cardTipoAguaSucia.setStrokeWidth(dpToPx(2));
            binding.cardTipoAguaSucia.setCardElevation(dpToPx(4));
        } else if ("Fuga".equals(tipo)) {
            binding.cardTipoFuga.setStrokeColor(android.graphics.Color.parseColor("#EF4444"));
            binding.cardTipoFuga.setCardBackgroundColor(android.graphics.Color.parseColor("#FEF2F2"));
            binding.cardTipoFuga.setStrokeWidth(dpToPx(2));
            binding.cardTipoFuga.setCardElevation(dpToPx(4));
        }
    }

    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }

    private void seleccionarUrgencia(String urgencia) {
        viewModel.urgencia.setValue(urgencia);
        
        int strokeDefault = android.graphics.Color.parseColor("#E2E8F0");
        int bgDefault = android.graphics.Color.WHITE;
        int textDefault = android.graphics.Color.parseColor("#64748B");

        // Reset all
        binding.btnUrgenciaBaja.setStrokeColor(android.content.res.ColorStateList.valueOf(strokeDefault));
        binding.btnUrgenciaBaja.setBackgroundColor(bgDefault);
        binding.btnUrgenciaBaja.setTextColor(textDefault);

        binding.btnUrgenciaMedia.setStrokeColor(android.content.res.ColorStateList.valueOf(strokeDefault));
        binding.btnUrgenciaMedia.setBackgroundColor(bgDefault);
        binding.btnUrgenciaMedia.setTextColor(textDefault);

        binding.btnUrgenciaAlta.setStrokeColor(android.content.res.ColorStateList.valueOf(strokeDefault));
        binding.btnUrgenciaAlta.setBackgroundColor(bgDefault);
        binding.btnUrgenciaAlta.setTextColor(textDefault);

        // Highlight selected
        if ("baja".equals(urgencia)) {
            binding.btnUrgenciaBaja.setStrokeColor(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#22C55E")));
            binding.btnUrgenciaBaja.setBackgroundColor(android.graphics.Color.parseColor("#DCFCE7"));
            binding.btnUrgenciaBaja.setTextColor(android.graphics.Color.parseColor("#15803D"));
        } else if ("media".equals(urgencia)) {
            binding.btnUrgenciaMedia.setStrokeColor(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#F97316")));
            binding.btnUrgenciaMedia.setBackgroundColor(android.graphics.Color.parseColor("#FFEDD5"));
            binding.btnUrgenciaMedia.setTextColor(android.graphics.Color.parseColor("#C2410C"));
        } else if ("alta".equals(urgencia)) {
            binding.btnUrgenciaAlta.setStrokeColor(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#EF4444")));
            binding.btnUrgenciaAlta.setBackgroundColor(android.graphics.Color.parseColor("#FEE2E2"));
            binding.btnUrgenciaAlta.setTextColor(android.graphics.Color.parseColor("#B91C1C"));
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
        
        binding.btnVerMapa.setOnClickListener(v -> {
            binding.successOverlay.setVisibility(View.GONE);
            resetForm();
            viewModel.resetFormulario();
            BottomNavigationView bottomNav = requireActivity().findViewById(R.id.bottom_nav);
            if (bottomNav != null) {
                bottomNav.setSelectedItemId(R.id.mapaFragment);
            } else {
                NavOptions navOptions = new NavOptions.Builder()
                        .setLaunchSingleTop(true)
                        .setPopUpTo(R.id.homeFragment, false)
                        .build();
                Navigation.findNavController(requireView()).navigate(R.id.mapaFragment, null, navOptions);
            }
        });
        
        binding.btnHacerOtro.setOnClickListener(v -> {
            binding.successOverlay.setVisibility(View.GONE);
            resetForm();
            viewModel.resetFormulario();
        });
    }

    private void resetForm() {
        binding.etBarrio.setText("");
        binding.etDireccion.setText("");
        binding.etDescripcion.setText("");
        viewModel.fotoUri.setValue(null);
        viewModel.latitud.setValue(null);
        viewModel.longitud.setValue(null);
        ocultarFotoPreview();
        seleccionarUrgencia("media");
        seleccionarTipo(null);

        // Reset GPS UI
        binding.progressGps.setVisibility(View.GONE);
        binding.ivGpsCheck.setVisibility(View.GONE);
        binding.tvGpsTitle.setText("📍 Usar mi ubicación actual (GPS)");
        binding.tvGpsSubtitle.setText("Toca para autocompletar barrio y coordenadas");
        binding.cardGpsLocation.setStrokeColor(android.graphics.Color.parseColor("#0EA5E9"));
    }

    private void iniciarCapturaUbicacion() {
        if (androidx.core.content.ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) == android.content.pm.PackageManager.PERMISSION_GRANTED ||
            androidx.core.content.ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
            obtenerUbicacionActual();
        } else {
            requestLocationPermission.launch(new String[]{
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            });
        }
    }

    @android.annotation.SuppressLint("MissingPermission")
    private void obtenerUbicacionActual() {
        binding.progressGps.setVisibility(View.VISIBLE);
        binding.ivGpsCheck.setVisibility(View.GONE);
        binding.tvGpsSubtitle.setText("Detectando señal GPS...");

        android.location.LocationManager locationManager = 
            (android.location.LocationManager) requireContext().getSystemService(android.content.Context.LOCATION_SERVICE);

        if (locationManager == null) {
            binding.progressGps.setVisibility(View.GONE);
            binding.tvGpsSubtitle.setText("Servicio de ubicación no disponible");
            return;
        }

        boolean gpsEnabled = locationManager.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER);
        boolean networkEnabled = locationManager.isProviderEnabled(android.location.LocationManager.NETWORK_PROVIDER);

        if (!gpsEnabled && !networkEnabled) {
            binding.progressGps.setVisibility(View.GONE);
            binding.tvGpsSubtitle.setText("Por favor activa el GPS en los ajustes del celular");
            Snackbar.make(binding.getRoot(), "Por favor activa el GPS en tu celular", Snackbar.LENGTH_LONG).show();
            return;
        }

        android.location.Location bestLocation = null;
        if (gpsEnabled) {
            bestLocation = locationManager.getLastKnownLocation(android.location.LocationManager.GPS_PROVIDER);
        }
        if (bestLocation == null && networkEnabled) {
            bestLocation = locationManager.getLastKnownLocation(android.location.LocationManager.NETWORK_PROVIDER);
        }

        if (bestLocation != null && (System.currentTimeMillis() - bestLocation.getTime()) < 120000) {
            aplicarUbicacion(bestLocation);
            return;
        }

        String provider = networkEnabled ? android.location.LocationManager.NETWORK_PROVIDER : android.location.LocationManager.GPS_PROVIDER;
        try {
            locationManager.requestSingleUpdate(provider, new android.location.LocationListener() {
                @Override
                public void onLocationChanged(@NonNull android.location.Location location) {
                    if (isAdded()) {
                        aplicarUbicacion(location);
                    }
                }
                @Override public void onStatusChanged(String provider, int status, Bundle extras) {}
                @Override public void onProviderEnabled(@NonNull String provider) {}
                @Override public void onProviderDisabled(@NonNull String provider) {}
            }, android.os.Looper.getMainLooper());
        } catch (Exception e) {
            if (bestLocation != null) {
                aplicarUbicacion(bestLocation);
            } else {
                binding.progressGps.setVisibility(View.GONE);
                binding.tvGpsSubtitle.setText("No se pudo obtener la posición actual");
            }
        }
    }

    private void aplicarUbicacion(android.location.Location location) {
        double lat = location.getLatitude();
        double lng = location.getLongitude();
        viewModel.latitud.setValue(lat);
        viewModel.longitud.setValue(lng);

        binding.progressGps.setVisibility(View.GONE);
        binding.ivGpsCheck.setVisibility(View.VISIBLE);
        binding.tvGpsTitle.setText("📍 Ubicación detectada");
        binding.tvGpsSubtitle.setText(String.format(java.util.Locale.getDefault(), "Lat: %.5f, Lng: %.5f", lat, lng));
        binding.cardGpsLocation.setStrokeColor(android.graphics.Color.parseColor("#22C55E"));

        sugerirBarrioPorCoordenadas(lat, lng);
    }

    private static final Object[][] BARRIOS_COORDENADAS = {
        {"Centro Histórico", 11.2442, -74.2117},
        {"Pescaíto", 11.2505, -74.2098},
        {"Mamatoco", 11.2356, -74.1706},
        {"El Rodadero", 11.2056, -74.2267},
        {"Gaira", 11.1931, -74.2197},
        {"Bavaria", 11.2389, -74.2014},
        {"Taganga", 11.2667, -74.1833},
        {"Bonda", 11.2333, -74.1167},
        {"Bello Horizonte", 11.1578, -74.2322},
        {"Pozos Colorados", 11.1714, -74.2306},
        {"Ciudad Equidad", 11.2289, -74.1458},
        {"El Parque", 11.2222, -74.1625},
        {"Los Almendros", 11.2467, -74.1950},
        {"Jardín", 11.2333, -74.1917},
        {"María Eugenia", 11.2250, -74.2083},
        {"Pastrana", 11.2200, -74.2050},
        {"San Jorge", 11.2520, -74.1980},
        {"San Martín", 11.2480, -74.2050},
        {"Nacho Vives", 11.2560, -74.1920},
        {"17 de Diciembre", 11.2410, -74.1750},
        {"20 de Julio", 11.2390, -74.1810},
        {"Timayui", 11.2310, -74.1350},
        {"Cantilito", 11.2280, -74.1290},
        {"Chimila", 11.2580, -74.1880},
        {"La Paz", 11.1780, -74.2150},
        {"Cristo Rey", 11.1820, -74.2180},
        {"Playa Salguero", 11.1980, -74.2290},
        {"Minuto de Dios", 11.2260, -74.1980},
        {"Taminaca", 11.2430, -74.2030},
        {"El Cundí", 11.2410, -74.2060},
        {"El Mayor", 11.2360, -74.2040},
        {"San Fernando", 11.2510, -74.1950},
        {"San Francisco", 11.2490, -74.2000},
        {"Santa Fe", 11.2340, -74.1800},
        {"Bolivariana", 11.2320, -74.1850},
        {"La Concepción", 11.2250, -74.1720},
        {"Villa del Carmen", 11.2280, -74.1680},
        {"Villa Bella", 11.2300, -74.1650},
        {"Garagoa", 11.2270, -74.1500}
    };

    private void sugerirBarrioPorCoordenadas(double lat, double lng) {
        String barrioMasCercano = null;
        double minDistance = Double.MAX_VALUE;

        for (Object[] b : BARRIOS_COORDENADAS) {
            String nombre = (String) b[0];
            double bLat = (double) b[1];
            double bLng = (double) b[2];
            double dist = Math.hypot(lat - bLat, lng - bLng);
            if (dist < minDistance) {
                minDistance = dist;
                barrioMasCercano = nombre;
            }
        }

        if (barrioMasCercano != null && minDistance < 0.08) {
            String barrioActual = binding.etBarrio.getText() != null ? binding.etBarrio.getText().toString().trim() : "";
            if (barrioActual.isEmpty()) {
                binding.etBarrio.setText(barrioMasCercano, false);
                binding.tilBarrio.setError(null);
                Snackbar.make(binding.getRoot(), "📍 Barrio autocompletado por GPS: " + barrioMasCercano, Snackbar.LENGTH_SHORT).show();
            }
        }
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
