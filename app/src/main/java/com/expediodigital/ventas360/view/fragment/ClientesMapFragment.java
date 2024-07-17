package com.expediodigital.ventas360.view.fragment;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import android.os.Bundle;
import androidx.core.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.expediodigital.ventas360.DAO.DAOCliente;
import com.expediodigital.ventas360.R;
import com.expediodigital.ventas360.model.ClienteModel;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;

public class ClientesMapFragment extends Fragment implements OnMapReadyCallback {
    static final String TAG = "ClientesMapFragment";
    private final int REQUEST_PERMISOS_LOCALIZACION = 1;
    private GoogleMap mMap;
    DAOCliente daoCliente;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_clientes_mapa, container, false);
        setHasOptionsMenu(true);
        daoCliente = new DAOCliente(getActivity());
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        SupportMapFragment supportMapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.mapa_clientes);
        supportMapFragment.getMapAsync(this);
        //SupportMapFragment mapFragment = (SupportMapFragment) getActivity().getSupportFragmentManager().findFragmentById(R.id.mapa_clientes);
        //mapFragment.getMapAsync(this);
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);

        mMap.getUiSettings().setMapToolbarEnabled(true);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setTiltGesturesEnabled(true);
        //mMap.setPadding(0,0,0,30);

        mMap.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener() {
            @Override
            public void onMyLocationChange(Location location) {
                LatLng loc = new LatLng(location.getLatitude(), location.getLongitude());
                //currentLocation = location;
                /*
                if (primeraPosicion == true){
                    //mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(loc, 14));
                    //mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(loc, 13));

                    CameraPosition cameraPosition = new CameraPosition.Builder()
                            .target(loc)      // Sets the center of the map to Mountain View
                            .zoom(14)                   // Sets the zoom
                            //.bearing(90)                // Sets the orientation of the camera to east
                            .tilt(30)                   // Sets the tilt of the camera to 30 degrees
                            .build();                   // Creates a CameraPosition from the builder
                    mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                    //primeraPosicion=false;
                }
                */
                //mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(loc, 14));
            }
        });

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                marker.showInfoWindow();

                return true;
            }
        });

        mMap.setOnInfoWindowLongClickListener(new GoogleMap.OnInfoWindowLongClickListener() {
            @Override
            public void onInfoWindowLongClick(Marker marker) {
                //alertOptions(marker.getPosition(), marker.getTitle(), marker.getSnippet());
            }
        });

        new async_cargarClientes().execute();

        // Add a marker in Sydney and move the camera
        LatLng Lima = new LatLng(-12.043355, -77.042864);
        //mMap.addMarker(new MarkerOptions().position(Lima).title("Marker in Lima"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(Lima));

        // Construct a CameraPosition focusing on Mountain View and animate the camera to that position.
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(Lima)      // Sets the center of the map to Mountain View
                .zoom(7)                   // Sets the zoom
                //.bearing(90)                // Sets the orientation of the camera to east
                .tilt(45)                   // Sets the tilt of the camera to 30 degrees
                .build();                   // Creates a CameraPosition from the builder
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

        if (Build.VERSION.SDK_INT >= 23) {
            if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                mMap.setMyLocationEnabled(true);
            } else {
                //FragmentCompat.requestPermissions(permissionsList, RequestCode); Para Fragments
                requestPermissions(new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_PERMISOS_LOCALIZACION);
            }
        } else {
            mMap.setMyLocationEnabled(true);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.d(TAG,"onRequestPermissionsResult");
        if (requestCode == REQUEST_PERMISOS_LOCALIZACION) {
            Log.d(TAG,"REQUEST_PERMISOS_LOCALIZACION");
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG,"NO HAY AMBOS PERMISOS");
                    return;
                }
                mMap.setMyLocationEnabled(true);
                Log.d(TAG,"SE COLOCÓ EL BOTON MY LOCATION");
            }
        }
    }

    class async_cargarClientes extends AsyncTask<Void,Void,ArrayList<ClienteModel>>{
        ProgressDialog pDialog;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(getActivity());
            pDialog.setCancelable(false);
            pDialog.setIndeterminate(true);
            pDialog.setMessage("Cargando...");
            pDialog.show();
        }

        @Override
        protected ArrayList<ClienteModel> doInBackground(Void... voids) {
            return daoCliente.getClientesOrdenados();
        }

        @Override
        protected void onPostExecute(ArrayList<ClienteModel> result) {
            super.onPostExecute(result);

            for (ClienteModel cliente: result){
                if (cliente.getLatitud() != 0 && cliente.getLongitud() != 0){
                    LatLng poistion = new LatLng(cliente.getLatitud(), cliente.getLongitud());
                    mMap.addMarker(new MarkerOptions().position(poistion).title(cliente.getRazonSocial()));
                }
            }

            pDialog.dismiss();
        }
    }

}
