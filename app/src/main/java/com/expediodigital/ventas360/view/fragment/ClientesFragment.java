package com.expediodigital.ventas360.view.fragment;


import android.os.Bundle;
import androidx.annotation.NonNull;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.fragment.app.FragmentTransaction;
import androidx.core.view.MenuItemCompat;
import androidx.appcompat.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.expediodigital.ventas360.R;
import com.expediodigital.ventas360.util.Util;

public class ClientesFragment extends Fragment implements SearchView.OnQueryTextListener {
    public static final String TAG = "ClientesFragment";

    BottomNavigationView bottomNavigationView;
    private ClientesListaFragment clientesListaFragment;
    private ClientesMapFragment clientesMapaFragment;

    SearchView searchView;

    public ClientesFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_clientes, container, false);
        setHasOptionsMenu(true);
        Util.actualizarToolBar(getString(R.string.menu_clientes),false,getActivity());

        clientesListaFragment = new ClientesListaFragment();
        clientesMapaFragment = new ClientesMapFragment();

        bottomNavigationView = (BottomNavigationView) view.findViewById(R.id.bottomBar);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                switch (id) {
                    case R.id.tab_clientes_lista:
                        bottomNavigationView.setBackgroundResource(R.color.colorPrimary);
                        replaceFragment(clientesListaFragment);
                        break;
                    case R.id.tab_clientes_mapa:
                        bottomNavigationView.setBackgroundResource(R.color.teal_500);
                        replaceFragment(clientesMapaFragment);
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
            fragmentTransaction.replace(R.id.clientes_container, fragment);
            fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
            fragmentTransaction.commit();
        }
    }
    private void setInitialFragment() {
        FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
        fragmentTransaction.add(R.id.clientes_container, clientesListaFragment);
        fragmentTransaction.commit();
    }

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_clientes, menu);

        MenuItem itemSearch = menu.findItem(R.id.action_search);
        searchView = (SearchView) MenuItemCompat.getActionView(itemSearch);
        searchView.setOnQueryTextListener(this);
    }

    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    public boolean onQueryTextChange(String newText) {
        clientesListaFragment.filtrarCliente(newText);
        return true;
    }

    private class SectionsPagerAdapter extends FragmentPagerAdapter {
        //indican la posición de la página o tab, siempre debe iniciar en cero
        final int PAGE_LISTA = 0;
        final int PAGE_MAPA = 1;

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case PAGE_LISTA:
                    return clientesListaFragment;
                case PAGE_MAPA:
                    return clientesMapaFragment;
                default:
                    return clientesListaFragment;
            }
            // getItem es llamada para instanciar el fragment de la página dada.
        }

        @Override
        public int getCount() {
            // Mostrar 2 páginas en total.
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case PAGE_LISTA:
                    return "Lista Clientes";
                case PAGE_MAPA:
                    return "Mapa";
            }
            return null;
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.wtf(TAG,"onPause()");
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(TAG,"onPause()");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG,"onDestroy()");
    }
}
