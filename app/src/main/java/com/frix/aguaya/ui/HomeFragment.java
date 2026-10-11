package com.frix.aguaya.ui;

import android.animation.ValueAnimator;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.frix.aguaya.R;
import com.frix.aguaya.databinding.FragmentHomeBinding;
import com.frix.aguaya.viewmodels.HomeViewModel;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private HomeViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        viewModel = new ViewModelProvider(this).get(HomeViewModel.class);
        
        setupClickListeners(view);
        
        binding.swipeRefreshHome.setColorSchemeResources(R.color.agua_primary);
        binding.swipeRefreshHome.setOnRefreshListener(() -> viewModel.cargarEstadisticas());

        observeViewModel();
        
        viewModel.cargarEstadisticas();
    }

    private void setupClickListeners(View view) {
        View.OnClickListener cardClickListener = v -> {
            String tipo = "";
            int id = v.getId();
            if (id == R.id.cardSinAgua) {
                tipo = "Sin agua";
            } else if (id == R.id.cardBajaPresion) {
                tipo = "Baja presión";
            } else if (id == R.id.cardAguaSucia) {
                tipo = "Agua sucia";
            } else if (id == R.id.cardFuga) {
                tipo = "Fuga";
            }
            
            Bundle bundle = new Bundle();
            bundle.putString("tipo_preseleccionado", tipo);
            Navigation.findNavController(view).navigate(R.id.reportarFragment, bundle);
        };

        binding.cardSinAgua.setOnClickListener(cardClickListener);
        binding.cardBajaPresion.setOnClickListener(cardClickListener);
        binding.cardAguaSucia.setOnClickListener(cardClickListener);
        binding.cardFuga.setOnClickListener(cardClickListener);

        binding.btnHacerReporte.setOnClickListener(v -> 
            Navigation.findNavController(v).navigate(R.id.reportarFragment)
        );

        binding.btnVerMapa.setOnClickListener(v -> 
            Navigation.findNavController(v).navigate(R.id.mapaFragment)
        );
        
        binding.btnBottomHacerReporte.setOnClickListener(v -> 
            Navigation.findNavController(v).navigate(R.id.reportarFragment)
        );
    }

    private void observeViewModel() {
        viewModel.getCargando().observe(getViewLifecycleOwner(), cargando -> {
            boolean isLoading = cargando != null && cargando;
            binding.swipeRefreshHome.setRefreshing(isLoading);
            binding.progressHome.setVisibility(isLoading && !binding.swipeRefreshHome.isRefreshing() ? View.VISIBLE : View.GONE);
        });

        viewModel.getEstadisticas().observe(getViewLifecycleOwner(), stats -> {
            if (stats != null) {
                animateCounter(binding.tvCounterReportes, stats.getTotalReportes(), "");
                animateCounter(binding.tvCounterBarrios, stats.getBarriosUnicos(), "");
                animateCounter(binding.tvCounterVerificados, (int) stats.getPctResueltos(), "%");
            }
        });
    }

    private void animateCounter(android.widget.TextView textView, int targetValue, String suffix) {
        ValueAnimator animator = ValueAnimator.ofInt(0, targetValue);
        animator.setDuration(1500);
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        animator.addUpdateListener(animation -> {
            int animatedValue = (int) animation.getAnimatedValue();
            textView.setText(animatedValue + suffix);
        });
        animator.start();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
