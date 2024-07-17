package com.expediodigital.ventas360.view.fragment;


import android.content.pm.ActivityInfo;
import android.os.Bundle;
import androidx.annotation.NonNull;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.expediodigital.ventas360.R;
import com.expediodigital.ventas360.util.Util;

/**
 * A simple {@link Fragment} subclass.
 */
public class EstadisticasFragment extends Fragment {
    public final String TAG = getClass().getName();

    private BottomNavigationView bottomNavigationView;
    private EstadisticaAvanceCuotaFragment estadisticaAvanceCuotaFragment;
    private EstadisticaGraficoFragment estadisticaGraficoFragment;
    private EstadisticaResumenMarcaFragment estadisticaResumenMarcaFragment;
    private EstadisticaResumenClienteFragment estadisticaResumenClienteFragment;
    private InfoVendedorFragment infoVendedorFragment;

    public EstadisticasFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);

        View view = inflater.inflate(R.layout.fragment_estadisticas, container, false);
        setHasOptionsMenu(true);
        Util.actualizarToolBar(getString(R.string.menu_estadisticas),false,getActivity());

        estadisticaAvanceCuotaFragment = new EstadisticaAvanceCuotaFragment();
        estadisticaGraficoFragment = new EstadisticaGraficoFragment();
        estadisticaResumenMarcaFragment = new EstadisticaResumenMarcaFragment();
        estadisticaResumenClienteFragment = new EstadisticaResumenClienteFragment();
        infoVendedorFragment = new InfoVendedorFragment();

        bottomNavigationView = view.findViewById(R.id.bottomBar);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                switch (id) {
                    case R.id.tab_estadisticaCuota:
                        replaceFragment(estadisticaAvanceCuotaFragment);
                        break;
                    case R.id.tab_infoVendedor:
                        replaceFragment(infoVendedorFragment);
                        break;
                    case R.id.tab_estadisticaGrafico:
                        replaceFragment(estadisticaGraficoFragment);
                        break;
                    case R.id.tab_estadisticaResumenVentaMarca:
                        replaceFragment(estadisticaResumenMarcaFragment);
                        break;
                    case R.id.tab_estadisticaResumenVentaCliente:
                        replaceFragment(estadisticaResumenClienteFragment);
                        break;
                }
                return true;
            }
        });
        setInitialFragment();
        return view;
    }

    private void replaceFragment(Fragment fragment) {
        if (fragment != null){
            FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.estadisticas_container, fragment);
            fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
            fragmentTransaction.commit();
        }
    }
    private void setInitialFragment() {
        FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
        fragmentTransaction.add(R.id.estadisticas_container, estadisticaAvanceCuotaFragment);
        fragmentTransaction.commit();
    }
}
