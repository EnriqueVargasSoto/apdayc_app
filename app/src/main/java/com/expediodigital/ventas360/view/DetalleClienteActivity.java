package com.expediodigital.ventas360.view;

import android.os.Bundle;
import com.google.android.material.tabs.TabLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.MenuItem;

import com.expediodigital.ventas360.R;
import com.expediodigital.ventas360.util.Util;
import com.expediodigital.ventas360.view.fragment.DatosClienteFragment;
import com.expediodigital.ventas360.view.fragment.InfoClienteFragment;

/**
 * Created by Kevin Robinson Meza Hinostroza on julio 2017.
 * Expedio Digital
 * Kevin.Meza@expediodigital.com
 */

public class DetalleClienteActivity extends AppCompatActivity {
    private SectionsPagerAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;
    private DatosClienteFragment datosClienteFragment;
    //private TrackingClienteFragment trackingClienteFragment;
    private InfoClienteFragment infoClienteFragment;
    private String idCliente = "";
    private String razonSocial = "";

    public String getIdCliente() {
        return idCliente;
    }
    public String getRazonSocial() {
        return razonSocial;
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalle_cliente);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null){
            idCliente = bundle.getString("idCliente","");
            razonSocial = bundle.getString("razonSocial","");
        }

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Util.actualizarToolBar(razonSocial,true,this);

        datosClienteFragment = new DatosClienteFragment();
        //trackingClienteFragment = new TrackingClienteFragment();
        infoClienteFragment = new InfoClienteFragment();

        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        mViewPager = findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);


    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home){
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    private class SectionsPagerAdapter extends FragmentPagerAdapter {
        //indican la posición de la página o tab, siempre debe iniciar en cero
        final int PAGE_TRACKING = 0;
        final int PAGE_DATOS = 1;


        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case PAGE_TRACKING:
                    return infoClienteFragment;
                case PAGE_DATOS:
                    return datosClienteFragment;
                default:
                    return infoClienteFragment;
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
                case PAGE_TRACKING:
                    return "Tracking";
                case PAGE_DATOS:
                    return "Datos";
            }
            return null;
        }
    }

}
