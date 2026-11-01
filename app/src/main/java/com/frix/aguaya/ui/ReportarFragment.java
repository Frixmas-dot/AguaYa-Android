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
        // Localidad 1: Cultural Tayrona - San Pedro Alejandrino
        "11 de Noviembre", "20 de Octubre", "Bolivariana", "Bonda", "Cantilito",
        "Ciudad Equidad", "Curinca", "El Cisne", "El Parque", "Garagoa",
        "La Concepción", "Los Alpes", "Los Laureles", "Luz del Mundo", "Mamatoco",
        "Nueva Colombia", "Oasis", "Rodrigo Ahumada", "Rosalía", "San Pedro Alejandrino",
        "Santa Ana", "Santa Clara", "Santa Cruz", "Timayui", "Villa Bella",
        "Villa Concha", "Villa Dania", "Villa del Carmen", "Yucal",
        // Localidad 2: Histórica - Rodrigo de Bastidas
        "17 de Diciembre", "20 de Julio", "Acodis", "Alcudia", "Bastidas",
        "Bavaria", "Betania", "Boulevard de las Rosas", "Centro Histórico", "Chimila",
        "Colinas del Pando", "Corea", "El Cundí", "El Mayor", "El Pando",
        "El Pradito", "Jardín", "Las Américas", "Libertador", "Los Almendros",
        "Los Ángeles", "Manzanares", "María Eugenia", "Martinete", "Minuto de Dios",
        "Murallas del Pando", "Nacho Vives", "Obrero", "Olaya Herrera", "Pastrana",
        "Pescaíto", "Primero de Mayo", "San Fernando", "San Francisco", "San Jorge",
        "San José del Pando", "San Martín", "San Pablo", "Santa Fe", "Taganga",
        "Taminaca", "Urbanización Riascos", "Villa del Río",
        // Localidad 3: Turística - Perla del Caribe
        "Aeromar", "Ayapel", "Bello Horizonte", "Cerro Ziruma", "Cristo Rey",
        "Don Jaca", "El Rodadero", "Gaira", "Irotama", "La Paz",
        "Los Lirios", "Nueva Betel", "Playa Salguero", "Plenomar", "Pozos Colorados",
        "Rodadero Reservado", "Salguero Beach", "Totumo", "Vista Hermosa", "Zuana"
    };

    private final java.util.List<Object[]> listaCoordenadas = new java.util.ArrayList<>();

    private void setupBarrioAutocomplete() {
        android.widget.ArrayAdapter<String> adapter = new android.widget.ArrayAdapter<>(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            new java.util.ArrayList<>(java.util.Arrays.asList(BARRIOS_SANTA_MARTA))
        );
        binding.etBarrio.setAdapter(adapter);

        new com.frix.aguaya.repositories.ReporteRepository().getBarrios().observe(getViewLifecycleOwner(), barrios -> {
            if (barrios != null && !barrios.isEmpty()) {
                java.util.List<String> nombres = new java.util.ArrayList<>();
                listaCoordenadas.clear();
                for (com.frix.aguaya.models.BarrioModel b : barrios) {
                    nombres.add(b.getNombre());
                    listaCoordenadas.add(new Object[]{b.getNombre(), b.getLat(), b.getLng()});
                }
                android.widget.ArrayAdapter<String> dynamicAdapter = new android.widget.ArrayAdapter<>(
                    requireContext(),
                    android.R.layout.simple_dropdown_item_1line,
                    nombres
                );
                binding.etBarrio.setAdapter(dynamicAdapter);
            }
        });
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
        // Localidad 1
        {"Mamatoco", 11.2356, -74.1706},
        {"Bonda", 11.2333, -74.1167},
        {"Cantilito", 11.2280, -74.1290},
        {"Timayui", 11.2310, -74.1350},
        {"Ciudad Equidad", 11.2289, -74.1458},
        {"Once de Noviembre", 11.2380, -74.1550},
        {"20 de Octubre", 11.2370, -74.1600},
        {"Garagoa", 11.2270, -74.1500},
        {"El Cisne", 11.2240, -74.1480},
        {"Rodrigo Ahumada", 11.2260, -74.1460},
        {"Bolivariana", 11.2320, -74.1850},
        {"Los Laureles", 11.2290, -74.1750},
        {"Santa Cruz", 11.2310, -74.1730},
        {"Villa del Carmen", 11.2280, -74.1680},
        {"Villa Bella", 11.2300, -74.1650},
        {"Villa Concha", 11.2330, -74.1630},
        {"El Parque", 11.2222, -74.1625},
        {"La Concepción", 11.2250, -74.1720},
        {"Santa Clara", 11.2340, -74.1710},
        {"Villa Dania", 11.2270, -74.1520},
        {"Nueva Colombia", 11.2330, -74.1590},
        {"Los Alpes", 11.2260, -74.1640},
        {"Curinca", 11.2210, -74.1710},
        {"Luz del Mundo", 11.2345, -74.1670},
        {"Yucal", 11.2255, -74.1580},
        {"Oasis", 11.2365, -74.1610},
        {"Rosalía", 11.2325, -74.1660},
        {"San Pedro Alejandrino", 11.2335, -74.1830},
        {"Santa Ana", 11.2340, -74.1810},

        // Localidad 2
        {"Centro Histórico", 11.2442, -74.2117},
        {"Pescaíto", 11.2505, -74.2098},
        {"San Martín", 11.2480, -74.2050},
        {"San Jorge", 11.2520, -74.1980},
        {"San Fernando", 11.2510, -74.1950},
        {"Nacho Vives", 11.2560, -74.1920},
        {"Taganga", 11.2667, -74.1833},
        {"Bastidas", 11.2520, -74.1850},
        {"Chimila", 11.2580, -74.1880},
        {"17 de Diciembre", 11.2410, -74.1750},
        {"20 de Julio", 11.2390, -74.1810},
        {"Los Almendros", 11.2467, -74.1950},
        {"El Cundí", 11.2410, -74.2060},
        {"Bavaria", 11.2389, -74.2014},
        {"El Mayor", 11.2360, -74.2040},
        {"Minuto de Dios", 11.2260, -74.1980},
        {"Taminaca", 11.2430, -74.2030},
        {"Santa Fe", 11.2340, -74.1800},
        {"Obrero", 11.2435, -74.2080},
        {"Olaya Herrera", 11.2450, -74.2070},
        {"Manzanares", 11.2350, -74.2130},
        {"Martinete", 11.2320, -74.2140},
        {"San Pablo", 11.2470, -74.2010},
        {"Betania", 11.2460, -74.1990},
        {"Los Ángeles", 11.2415, -74.2045},
        {"El Pradito", 11.2440, -74.2010},
        {"Alcudia", 11.2405, -74.2005},
        {"Urbanización Riascos", 11.2375, -74.2025},
        {"Jardín", 11.2333, -74.1917},
        {"Libertador", 11.2395, -74.1930},
        {"María Eugenia", 11.2250, -74.2083},
        {"Pastrana", 11.2200, -74.2050},
        {"Primero de Mayo", 11.2220, -74.2070},
        {"Las Américas", 11.2230, -74.2060},
        {"Corea", 11.2240, -74.2090},
        {"San José del Pando", 11.2270, -74.2010},
        {"El Pando", 11.2215, -74.1980},
        {"Murallas del Pando", 11.2205, -74.1960},
        {"Acodis", 11.2195, -74.1970},
        {"Colinas del Pando", 11.2185, -74.1990},
        {"San Francisco", 11.2490, -74.2000},
        {"Villa del Río", 11.2385, -74.1965},
        {"Boulevard de las Rosas", 11.2365, -74.1945},

        // Localidad 3
        {"El Rodadero", 11.2056, -74.2267},
        {"Gaira", 11.1931, -74.2197},
        {"Playa Salguero", 11.1980, -74.2290},
        {"Pozos Colorados", 11.1714, -74.2306},
        {"Bello Horizonte", 11.1578, -74.2322},
        {"La Paz", 11.1780, -74.2150},
        {"Cristo Rey", 11.1820, -74.2180},
        {"Vista Hermosa", 11.1750, -74.2170},
        {"Aeromar", 11.1450, -74.2310},
        {"Don Jaca", 11.1350, -74.2300},
        {"Los Lirios", 11.1850, -74.2160},
        {"Nueva Betel", 11.1810, -74.2190},
        {"Ayapel", 11.1790, -74.2175},
        {"Totumo", 11.1830, -74.2140},
        {"Rodadero Reservado", 11.2090, -74.2240},
        {"Cerro Ziruma", 11.2150, -74.2210},
        {"Salguero Beach", 11.1960, -74.2280},
        {"Plenomar", 11.1680, -74.2315},
        {"Zuana", 11.1520, -74.2325},
        {"Irotama", 11.1500, -74.2330}
    };

    private void sugerirBarrioPorCoordenadas(double lat, double lng) {
        String barrioMasCercano = null;
        double minDistance = Double.MAX_VALUE;

        java.util.List<Object[]> coordenadasParaBuscar = !listaCoordenadas.isEmpty() 
            ? listaCoordenadas 
            : java.util.Arrays.asList(BARRIOS_COORDENADAS);

        for (Object[] b : coordenadasParaBuscar) {
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
