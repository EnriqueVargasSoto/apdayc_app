package com.expediodigital.ventas360.view.fragment;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.core.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.expediodigital.ventas360.DAO.DAOCliente;
import com.expediodigital.ventas360.DAO.DAOConfiguracion;
import com.expediodigital.ventas360.R;
import com.expediodigital.ventas360.Ventas360App;
import com.expediodigital.ventas360.model.ClienteModel;
import com.expediodigital.ventas360.model.DirectionApiModel;
import com.expediodigital.ventas360.model.DirectionApiResponse;
import com.expediodigital.ventas360.util.BitmapConverter;
import com.expediodigital.ventas360.util.DirectionsJSONParser;
import com.expediodigital.ventas360.util.Util;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.gson.Gson;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MapFragment extends Fragment implements OnMapReadyCallback {
    static final String TAG = "ClientesMapFragment";
    private static final String DIRECTION_URL_API = "https://maps.googleapis.com/maps/api/directions/json?";
    private static final String GOOGLE_API_KEY = "AIzaSyBV2tCZkHMxJFMXLyxW5nqWHUnExuwp9VE";
    private static final int LIMITE_WAYPOINTS = 10;

    private final int REQUEST_PERMISOS_LOCALIZACION = 1;
    private ImageButton btn_anteriorRuta, btn_siguienteRuta;
    private GoogleMap mMap;
    private boolean showMarkerEnumerado = false;
    TextView tv_infoRuta;
    TextView tv_cantidadTotal;
    TextView tv_cantidadVisitados;
    TextView tv_cantidadPendientes;
    ArrayList<ClienteModel> listaClientes = new ArrayList<>();
    ProgressDialog progressDialog;
    DAOCliente daoCliente;
    DAOConfiguracion daoConfiguracion;
    Ventas360App ventas360App;

    Location currentLocation;
    Polyline polylineRuta;
    boolean primeraPosicion = true;
    List<DirectionApiResponse> listaRespuestasGoogleMaps = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        View view = inflater.inflate(R.layout.fragment_mapa, container, false);
        setHasOptionsMenu(true);
        Util.actualizarToolBar(getString(R.string.menu_mapa),false,getActivity());
        ventas360App = (Ventas360App) getActivity().getApplicationContext();
        daoCliente = new DAOCliente(getActivity());
        daoConfiguracion = new DAOConfiguracion(getActivity());
        btn_anteriorRuta = view.findViewById(R.id.btn_anteriorRuta);
        btn_siguienteRuta = view.findViewById(R.id.btn_siguienteRuta);
        tv_infoRuta = view.findViewById(R.id.tv_infoRuta);
        tv_cantidadTotal = view.findViewById(R.id.tv_cantidadTotal);
        tv_cantidadVisitados = view.findViewById(R.id.tv_cantidadVisitados);
        tv_cantidadPendientes = view.findViewById(R.id.tv_cantidadPendientes);

        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setCancelable(false);
        progressDialog.setIndeterminate(true);

        btn_anteriorRuta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int index = ventas360App.getIndexRutaMapa();
                index--;
                if (index >= 0){
                    //CADA QUE SE PULSE EN ALGUN BOTON SE DEBE SELECCIONAR EL PROXIMO O ANTERIOR BLOQUE DE CLIENTES MARKERS LLAMANDO A showGeoMarkers()
                    ventas360App.setIndexRutaMapa(index);
                    int posicionFin = (LIMITE_WAYPOINTS * (index + 1)) + index;
                    int posicionInicio = posicionFin - LIMITE_WAYPOINTS;
                    showGeoMarkers(true,posicionInicio,posicionFin);
                    showInfoRuta(posicionInicio,posicionFin);

                    pintarRutaRespuestaGoogleMaps();
                }else{
                    //Toast.makeText(getActivity(),"No hay mas rutas disponibles",Toast.LENGTH_SHORT).show();
                }
            }
        });

        btn_siguienteRuta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int index = ventas360App.getIndexRutaMapa();
                index++;
                //Si el proximo index es menor al tamaño de la lista se mueve el index, de lo contrario significa que se llegó al límite de la lista de rutas.
                if (index < listaRespuestasGoogleMaps.size()){
                    ventas360App.setIndexRutaMapa(index);

                    int posicionFin = (LIMITE_WAYPOINTS * (index + 1)) + index;
                    int posicionInicio = posicionFin - LIMITE_WAYPOINTS;
                    showGeoMarkers(true,posicionInicio,posicionFin);
                    showInfoRuta(posicionInicio,posicionFin);

                    pintarRutaRespuestaGoogleMaps();
                }else{
                    Toast.makeText(getActivity(),"No hay mas rutas disponibles",Toast.LENGTH_SHORT).show();
                }
            }
        });

        listaRespuestasGoogleMaps = daoCliente.getJSONRutas();

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //SupportMapFragment mapFragment = (SupportMapFragment) getActivity().getSupportFragmentManager().findFragmentById(R.id.mapa_clientes);
        SupportMapFragment supportMapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.mapa_clientes);
        supportMapFragment.getMapAsync(this);

        if (ventas360App.getIndexRutaMapa() != -1){
            //Si es distinto de -1 quiere decir que en algun momento ya se ordenó los clientes y debe mostrarse los markers enumerados al abrir el mapa
            showMarkerEnumerado = true;
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        mMap.getUiSettings().setMapToolbarEnabled(true);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setTiltGesturesEnabled(true);
        mMap.setPadding(0,0,0,30);

        mMap.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener() {
            @Override
            public void onMyLocationChange(Location location) {
                LatLng loc = new LatLng(location.getLatitude(), location.getLongitude());
                currentLocation = location;

                if (primeraPosicion == true){
                    //mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(loc, 14));
                    //mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(loc, 13));

                    CameraPosition cameraPosition = new CameraPosition.Builder()
                            .target(loc)      // Sets the center of the map to Mountain View
                            .zoom(14)                   // Sets the zoom
                            //.bearing(90)                // Sets the orientation of the camera to east
                            .tilt(30)                   // Sets the tilt of the camera to 30 degrees
                            .build();                   // Creates a CameraPosition from the builder
                    //mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                    //primeraPosicion=false;
                }
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
                alertOptions(marker.getPosition(), marker.getTitle(), marker.getSnippet());
            }
        });

        // Add a marker in Sydney and move the camera
        LatLng Lima = new LatLng(-12.043355, -77.042864);
        //mMap.addMarker(new MarkerOptions().position(Lima).title("Marker in Lima"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(Lima));

        // Construct a CameraPosition focusing on Mountain View and animate the camera to that position.
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(Lima)      // Sets the center of the map to Mountain View
                .zoom(7)                   // Sets the zoom
                //.bearing(90)                // Sets the orientation of the camera to east
                //.tilt(45)                   // Sets the tilt of the camera to 30 degrees
                .build();                   // Creates a CameraPosition from the builder
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

        if (Build.VERSION.SDK_INT >= 23) {
            if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                mMap.setMyLocationEnabled(true);
            } else {
                //FragmentCompat.requestPermissions(permissionsList, RequestCode); Para Fragments
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_PERMISOS_LOCALIZACION);
            }
        } else {
            mMap.setMyLocationEnabled(true);
        }

        /*Es muy importante indicar que index irá cambiando y cada que se sincronice clientes, se iniciará en -1.
        Para que no se quede guardado en preferencias un index que la nueva lista no tenga, ademas que no se debe
        seleccionar los markers ni pintar la ruta de algo que el usuario no ha visto con anterioridad*/
        int index = ventas360App.getIndexRutaMapa();
        int posicionFin = 0;
        int posicionInicio = 0;
        if (index != -1){
            /*Si el index es distinto a -1 es porque no ha mostrado alguna ruta anteriormente, es ese caso se debe mostrar los markers
            que se tenía seleccionado, esto se realiza dentro del AsyncTask async_cargarClientes() pasándole
            las posiciones de inicio y fin de los markers a seleccionar. También dentro del AsyncTask se muestra la ruta correspondiente*/
            posicionFin = (LIMITE_WAYPOINTS * (index + 1)) + index;
            posicionInicio = posicionFin - LIMITE_WAYPOINTS;
        }

        new async_cargarClientes().execute(posicionInicio,posicionFin);
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

    private class async_cargarClientes extends AsyncTask<Integer,Void,ArrayList<ClienteModel>>{
        ProgressDialog pDialog;
        int inicioSeleccionado=0, finSeleccionado=0;
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
        protected ArrayList<ClienteModel> doInBackground(Integer... integers) {
            inicioSeleccionado = integers[0];
            finSeleccionado = integers[1];
            return daoCliente.getClientesOrdenados();
        }

        @Override
        protected void onPostExecute(ArrayList<ClienteModel> result) {
            super.onPostExecute(result);

            listaClientes.clear();
            listaClientes.addAll(result);

            int numeroClientes = result.size();
            int numeroVisitados = daoCliente.getNumeroClientesVisitados(ventas360App.getModoVenta(), daoConfiguracion.getEstadoVendedor(ventas360App.getIdEmpresa(),ventas360App.getIdSucursal(),ventas360App.getIdVendedor()));
            int numeroPendientes = numeroClientes - numeroVisitados;

            showGeoMarkers(showMarkerEnumerado,inicioSeleccionado,finSeleccionado);
            showInfoRuta(inicioSeleccionado,finSeleccionado);

            if (showMarkerEnumerado){
                pintarRutaRespuestaGoogleMaps();
            }

            tv_cantidadTotal.setText(String.valueOf(numeroClientes));
            tv_cantidadVisitados.setText(String.valueOf(numeroVisitados));
            tv_cantidadPendientes.setText(String.valueOf(numeroPendientes));
            pDialog.dismiss();
        }
    }

    /**
     * @param markerEnumerados Indica si los markers tendrán en número de orden
     * @param inicioSeleccionado Indica la posición desde donde se deben mostrar markers seleccionados (un color mas fuerte)
     * @param finSeleccionado Indica a posición hasta donde se deben mostrar markers seleccionados (un color mas fuerte)
     */
    private void showGeoMarkers(boolean markerEnumerados, int inicioSeleccionado, int finSeleccionado) {
        mMap.clear();

        for (int i = 0; i < listaClientes.size(); i++){
            ClienteModel cliente = listaClientes.get(i);

            if (markerEnumerados){
                if (cliente.getLatitud() != 0 && cliente.getLongitud() != 0){
                    Bitmap bitmap;

                    if (cliente.getEstadoPedido() == ClienteModel.ESTADO_PEDIDO_VISITADO){
                        if (inicioSeleccionado <= i && i <= finSeleccionado) {
                            bitmap = BitmapConverter.writeTextOnDrawable(getActivity(), R.drawable.icon_geomarker_green_number_selected, String.valueOf(cliente.getOrden()));
                        }else{
                            bitmap = BitmapConverter.writeTextOnDrawable(getActivity(), R.drawable.icon_geomarker_green_number, String.valueOf(cliente.getOrden()));
                        }
                        mMap.addMarker(new MarkerOptions()
                                .position(new LatLng(cliente.getLatitud(),cliente.getLongitud()))
                                .title(cliente.getRazonSocial()).snippet(cliente.getDireccion())
                                .icon(BitmapDescriptorFactory.fromBitmap(bitmap)));
                    }else if (cliente.getEstadoPedido() == ClienteModel.ESTADO_PEDIDO_ANULADO){
                        if (inicioSeleccionado <= i && i <= finSeleccionado) {
                            bitmap = BitmapConverter.writeTextOnDrawable(getActivity(), R.drawable.icon_geomarker_red_number_selected, String.valueOf(cliente.getOrden()));
                        }else{
                            bitmap = BitmapConverter.writeTextOnDrawable(getActivity(), R.drawable.icon_geomarker_red_number, String.valueOf(cliente.getOrden()));
                        }
                        mMap.addMarker(new MarkerOptions()
                                .position(new LatLng(cliente.getLatitud(),cliente.getLongitud()))
                                .title(cliente.getRazonSocial()).snippet(cliente.getDireccion())
                                .icon(BitmapDescriptorFactory.fromBitmap(bitmap)));
                    }else{
                        if (inicioSeleccionado <= i && i <= finSeleccionado) {
                            bitmap = BitmapConverter.writeTextOnDrawable(getActivity(), R.drawable.icon_geomarker_grey_number_selected, String.valueOf(cliente.getOrden()));
                        }else{
                            bitmap = BitmapConverter.writeTextOnDrawable(getActivity(), R.drawable.icon_geomarker_grey_number, String.valueOf(cliente.getOrden()));
                        }
                        mMap.addMarker(new MarkerOptions()
                                .position(new LatLng(cliente.getLatitud(),cliente.getLongitud()))
                                .title(cliente.getRazonSocial()).snippet(cliente.getDireccion())
                                .icon(BitmapDescriptorFactory.fromBitmap(bitmap)));

                    }
                }
            }else{
                if (cliente.getLatitud() != 0 && cliente.getLongitud() != 0){
                    if (cliente.getEstadoPedido() == ClienteModel.ESTADO_PEDIDO_VISITADO){
                        mMap.addMarker(new MarkerOptions()
                                .position(new LatLng(cliente.getLatitud(),cliente.getLongitud()))
                                .title(cliente.getRazonSocial()).snippet(cliente.getDireccion())
                                .icon(BitmapDescriptorFactory.fromBitmap(BitmapConverter.getBitmap(getActivity(), R.drawable.icon_geomarker_green))));
                    }else if (cliente.getEstadoPedido() == ClienteModel.ESTADO_PEDIDO_ANULADO){
                        mMap.addMarker(new MarkerOptions()
                                .position(new LatLng(cliente.getLatitud(),cliente.getLongitud()))
                                .title(cliente.getRazonSocial()).snippet(cliente.getDireccion())
                                .icon(BitmapDescriptorFactory.fromBitmap(BitmapConverter.getBitmap(getActivity(), R.drawable.icon_geomarker_red))));
                        //.icon(BitmapDescriptorFactory.fromResource(R.drawable.rojo_reducido)));
                    }else{
                        mMap.addMarker(new MarkerOptions()
                            .position(new LatLng(cliente.getLatitud(),cliente.getLongitud()))
                            .title(cliente.getRazonSocial()).snippet(cliente.getDireccion())
                            .icon(BitmapDescriptorFactory.fromBitmap(BitmapConverter.getBitmap(getActivity(), R.drawable.icon_geomarker_grey))));
                    }
                }

            }

        }
    }

    /**
     * @param inicioSeleccionado Muestra desde donde se está resaltando a los markers
     * @param finSeleccionado Muestra hasta donde se está resaltando a los markers
     */
    void showInfoRuta(int inicioSeleccionado, int finSeleccionado){
        if (inicioSeleccionado == 0 && finSeleccionado == 0){
            tv_infoRuta.setVisibility(View.GONE);
        }else {
            if (ventas360App.getIndexRutaMapa() != -1){
                tv_infoRuta.setVisibility(View.VISIBLE);
                tv_infoRuta.setText("Ruta "+(ventas360App.getIndexRutaMapa()+1)+"/"+listaRespuestasGoogleMaps.size()+" (Del "+(inicioSeleccionado+1)+" al " +(finSeleccionado+1)+")");
            }else{
                tv_infoRuta.setVisibility(View.GONE);
            }
        }
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_fragment_mapa, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id){
            case R.id.menu_mapa_ruta:
                cargarRutas();
                break;

        }
        return super.onOptionsItemSelected(item);
    }

    private void showProgress(boolean show) {
        if (show) {
            progressDialog.show();
            progressDialog.setContentView(R.layout.progress_bar);
        }else
            progressDialog.dismiss();
    }

    private void cargarRutas() {
        showProgress(true);
        listaRespuestasGoogleMaps.clear();
        getDataFromGoogleMaps(0);
    }



    public void alertOptions(final LatLng puntoDestino, final String nombreCliente,final String direccion){
        new AlertDialog.Builder(getActivity())
                .setTitle("Qué desea hacer?")
                .setMessage("Cliente: "+nombreCliente)
                .setNegativeButton("Cancelar",null)
                .setPositiveButton("Tomar pedido",
                        new DialogInterface.OnClickListener() {
                            @TargetApi(11)
                            public void onClick(DialogInterface dialog, int id) {
                                Toast.makeText(getActivity(), "Detalle ", Toast.LENGTH_LONG).show();
                                dialog.cancel();
                            }
                        }).show();
    }

    private String getDirectionsUrl(LatLng puntoOrigen, LatLng puntoDestino, List<LatLng> puntosParada) {
        //String str_mode = "mode=walking"; //POR DEFECTO SIEMPRE TIENE DRIVING
        //Origen de la ruta
        String str_origin = "origin=" + puntoOrigen.latitude + "," + puntoOrigen.longitude;
        //Destino de la ruta
        String str_dest = "destination=" + puntoDestino.latitude + "," + puntoDestino.longitude;

        //Puntos de parada
        String waypoints = "";
        for(int i=0;i<puntosParada.size();i++){
            LatLng point  = puntosParada.get(i);
            if(i==0)//waypoints = "&waypoints=optimize:true|";
                waypoints = "&waypoints=optimize:true|";//waypoints = "&waypoints=";
            waypoints += point.latitude + "," + point.longitude + "|";
        }
        if (!waypoints.isEmpty())
            waypoints = waypoints.substring(0,waypoints.length()-1);

        return DIRECTION_URL_API + str_origin + "&" + str_dest + waypoints + "&key="+GOOGLE_API_KEY;
    }

    int indexBloque = 0;
    public void getDataFromGoogleMaps(int indexAnterior){
        LatLng puntoOrigen = null;
        List<LatLng> puntosParada = new ArrayList<>();
        LatLng puntoDestino = null;

        if (indexAnterior == 0){
            puntoOrigen = new LatLng(-11.9991929,-77.01347351);
            //puntoOrigen = new LatLng(-11.10072537,-77.61053324);
            if(puntoOrigen == null) {
                if (currentLocation != null)
                    puntoOrigen = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
            }
            if (puntoOrigen != null)
                mMap.addMarker(new MarkerOptions().position(puntoOrigen).title("Punto de origen"));
        }else{
            //Se obtiene el index donde se quedó, tomando ese último item como el punto de origen para el nuevo bloque
            puntoOrigen = new LatLng(listaClientes.get(indexAnterior).getLatitud(),listaClientes.get(indexAnterior).getLongitud());
            indexAnterior += 1;//Se incrementa en 1 para que el for que toma los waypoints no tome el punto actual (punto origen), sino tome desde el siguiente en la lista
        }
        int contadorWaypoints = 0;
        //El for funciona aun cuando se haya llegado al límite de la lista y no se pueda alcanzar el bloque de 23, pero si se sabe que se llegó al final de la lista.
        for (int i = indexAnterior ; i<listaClientes.size(); i++){//El for iniciará desde indexAnterior +1 porque el indexAnterior ya fué tomado como punto origen
            if (contadorWaypoints == LIMITE_WAYPOINTS || i == listaClientes.size()-1){//Si ya se agregaron 23 punto o se llegó al final de la lista
                puntoDestino = new LatLng(listaClientes.get(i).getLatitud(),listaClientes.get(i).getLongitud());
                Log.d(TAG,"puntoDestino "+i+" agregado -> "+listaClientes.get(i).getRazonSocial()+" anterior orden "+listaClientes.get(i).getOrden());
                indexBloque = i;
                break;
            }else{
                LatLng puntoCliente = new LatLng(listaClientes.get(i).getLatitud(),listaClientes.get(i).getLongitud());
                puntosParada.add(puntoCliente);
                contadorWaypoints ++;
                Log.d(TAG,"waypoint "+i+" agregado -> "+listaClientes.get(i).getRazonSocial()+" anterior orden "+listaClientes.get(i).getOrden());
            }
        }

        final String directionsURL = getDirectionsUrl(puntoOrigen,puntoDestino,puntosParada);//Obtiene el url para hacer la consulta a google

        new AsyncTask<Void, Void, String>() {

            @Override
            protected String doInBackground(Void... voids) {
                String data = "";
                try{
                    data = downloadUrl(directionsURL);
                    Log.v(TAG,directionsURL);
                }catch(Exception e){
                    Log.d("Background Task",e.toString());
                }
                return data;
            }

            @Override
            protected void onPostExecute(String result) {
                super.onPostExecute(result);

                JSONObject jObject;
                DirectionApiResponse directionApiResponse = null;
                try{
                    jObject = new JSONObject(result);
                    DirectionsJSONParser parser = new DirectionsJSONParser();
                    directionApiResponse = parser.parse(jObject);
                    Log.v(TAG,""+ new Gson().toJson(directionApiResponse));
                }catch(Exception e){
                    e.printStackTrace();
                }
                listaRespuestasGoogleMaps.add(directionApiResponse);

                if (indexBloque < listaClientes.size() -1 ){
                    Log.d(TAG,"lanzando de nuevo...");
                    getDataFromGoogleMaps(indexBloque);
                }else{
                    Log.d(TAG,"Terminando obtencion de data from google!");
                    actualizarOrdenClientes();
                }
            }
        }.execute();
    }

    private void actualizarOrdenClientes() {
        largeLog(TAG,new Gson().toJson(listaRespuestasGoogleMaps));

        int ultimoIndex = 0;

        for (DirectionApiResponse directionApiResponse : listaRespuestasGoogleMaps){
            Log.w(TAG,"ultimoIndex al empezar "+ultimoIndex);
            PolylineOptions lineOptions = new PolylineOptions();
            lineOptions.width(7);
            lineOptions.color(Color.MAGENTA);

            int distanceGeneral = 0;
            int durationGeneral = 0;
            if (directionApiResponse!=null){
                if(directionApiResponse.getListaPuntos().isEmpty()){
                    Toast.makeText(getActivity(), "No Points", Toast.LENGTH_SHORT).show();
                    return;
                }

                for(int i=0;i<directionApiResponse.getListaPuntos().size();i++){
                    DirectionApiModel directionApiModel = directionApiResponse.getListaPuntos().get(i);

                    distanceGeneral += directionApiModel.getDistance();
                    durationGeneral += directionApiModel.getDuration();

                    lineOptions.addAll(directionApiModel.getSteps());
                }
                //mMap.addPolyline(lineOptions);
                //---------------------------------------------------------------------------------------------------

                //Si es cero, es el inicio de la lista de waypoints, el punto origen es el del almacén mas no este, por lo que no debería tomarse como punto origen e ir directamente como waypoint
                /*if (ultimoIndex > 0){
                    //Se obtiene el punto origen
                    daoCliente.actualizarOrdenCliente(listaClientes.get(ultimoIndex).getIdCliente(), ultimoIndex +1 );//se toma el ultimo index como siguiente numero
                    ultimoIndex ++;
                }*/

                //Se recorren los waypoints
                for(int i=0;i<directionApiResponse.getListaOrden().size();i++){
                    int ordenFromGoogle = directionApiResponse.getListaOrden().get(i);
                    int ordenCliente = ultimoIndex + ordenFromGoogle;//Se le suma el index ya que se debe continuar con la numeracion anterior

                    daoCliente.actualizarOrdenCliente(listaClientes.get(ultimoIndex + i).getIdCliente(), ordenCliente +1 );
                }
                ultimoIndex += directionApiResponse.getListaOrden().size();//Se guarda el ultimo index que se utilizó +1 (index con el cual se empezará la proxima vuelta del for)

                //Se obtiene el punto destino
                daoCliente.actualizarOrdenCliente(listaClientes.get(ultimoIndex).getIdCliente(), ultimoIndex +1 );//se toma el ultimo index como siguiente numero
                ultimoIndex ++;
            }
        }
        showProgress(false);
        showMarkerEnumerado = true;
        ventas360App.setIndexRutaMapa(0);

        new async_cargarClientes().execute(0,LIMITE_WAYPOINTS);//Como se acaba de ordenar el index de la ruta a mostrar será cero por lo tanto se deben seleccionar los primeros markers
        //Almacenar las respuestas en la base de datos
        daoCliente.guardarJSONRutas(new Gson().toJson(listaRespuestasGoogleMaps));
    }

    public static void largeLog(String tag, String content) {
        if (content.length() > 4000) {
            Log.v(tag, content.substring(0, 4000));
            largeLog("", content.substring(4000));
        } else {
            Log.v(tag, content);
        }
    }

    //VERIFICAR EL JSON SIN ORDENAR Y LUEGO ORDENANDO, COMPARARLOS PARA VER SI VARÍA LOS STEPS LAS RUTAS POR DONDE SE DEBE PASAR O ES QUE LUEGO DE OBTENER SE DEBE ORDENAR EN BASE AL ARRAY DE ORDEN QUE RETORNA
    private void pintarRutaRespuestaGoogleMaps() {
        if (polylineRuta != null)
            polylineRuta.remove();
        if (!listaRespuestasGoogleMaps.isEmpty()){
            if ( ventas360App.getIndexRutaMapa() >= listaRespuestasGoogleMaps.size()){
                //Si el index que se tiene guardado excede a la cantidad de rutas respuesta, se reinicia el index para que se muestre la primera ruta de la lista
                ventas360App.setIndexRutaMapa(0);
            }

            DirectionApiResponse directionApiResponse = listaRespuestasGoogleMaps.get(ventas360App.getIndexRutaMapa());

            ArrayList<LatLng> points = null;
            PolylineOptions lineOptions = new PolylineOptions();
            lineOptions.width(5);

            //lineOptions.color(Color.MAGENTA);

            int distanceGeneral = 0;
            int durationGeneral = 0;
            if (directionApiResponse!=null){
                if(directionApiResponse.getListaPuntos().isEmpty()){
                    Toast.makeText(getActivity(), "No Points", Toast.LENGTH_SHORT).show();
                    return;
                }

                for(int i=0;i<directionApiResponse.getListaPuntos().size();i++){
                    points = new ArrayList<LatLng>();
                    DirectionApiModel directionApiModel = directionApiResponse.getListaPuntos().get(i);

                    distanceGeneral += directionApiModel.getDistance();
                    durationGeneral += directionApiModel.getDuration();

                    Random rnd = new Random();
                    int color = Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));
                    PolylineOptions lineOptions2 = new PolylineOptions();
                    lineOptions2.width(2);
                    lineOptions2.color(color);
                    //lineOptions2.color(Color.MAGENTA);
                    lineOptions2.addAll(directionApiModel.getSteps());
                    polylineRuta = mMap.addPolyline(lineOptions2);

                    /*Bitmap bitmap = BitmapConverter.writeTextOnDrawable(getActivity(), R.drawable.icon_geomarker_red_number_selected, "A-"+(i+1));
                    Bitmap bitmap2 = BitmapConverter.writeTextOnDrawable(getActivity(), R.drawable.icon_geomarker_red_number_selected, "B-"+(i+1));
                    mMap.addMarker(new MarkerOptions()
                            .position(directionApiModel.getStartLocation())
                            .title("A-"+(i+1))
                            .icon(BitmapDescriptorFactory.fromBitmap(bitmap)));
                    mMap.addMarker(new MarkerOptions()
                            .position(directionApiModel.getEndLocation())
                            .title("B-"+(i+1))
                            .icon(BitmapDescriptorFactory.fromBitmap(bitmap2)));*/

                    if (i<directionApiResponse.getListaPuntos().size()){
                        int index = ventas360App.getIndexRutaMapa();
                        int posicionFin = (LIMITE_WAYPOINTS * (index + 1)) + index;
                        int posicionInicio = posicionFin - LIMITE_WAYPOINTS;

                        /*Bitmap bitmap = BitmapConverter.writeTextOnDrawable(getActivity(), R.drawable.icon_geomarker_red_number_selected,listaClientes.get(posicionInicio+i).getOrden()+"" );
                        mMap.addMarker(new MarkerOptions()
                                .position(new LatLng(listaClientes.get(posicionInicio+i).getLatitud(),listaClientes.get(posicionInicio+i).getLongitud()))
                                .title("A-"+(i+1))
                                .icon(BitmapDescriptorFactory.fromBitmap(bitmap)));*/

                        ArrayList<LatLng> points2 = new ArrayList<LatLng>();
                        points2.add(directionApiModel.getEndLocation());
                        points2.add(new LatLng(listaClientes.get(posicionInicio+i).getLatitud(),listaClientes.get(posicionInicio+i).getLongitud()));//Punto del cliente

                        PolylineOptions lineOptions3 = new PolylineOptions();
                        lineOptions3.addAll(points2);
                        lineOptions3.width(2);
                        lineOptions3.color(Color.GRAY);

                        mMap.addPolyline(lineOptions3);
                    }


                    if (i == 0)
                        mMap.addMarker(new MarkerOptions().position(directionApiModel.getStartLocation()).title("Punto de inicio"));
                    if (i == directionApiResponse.getListaPuntos().size() -1) {
                        //mMap.addMarker(new MarkerOptions().position(directionApiModel.getEndLocation()).title("Punto final"));
                    }

                }
            }
            //tvDistanceDuration.setText("Distancia:"+distance + ", Tiempo:"+(duration.replace("hour", "h")));
            //polylineRuta = mMap.addPolyline(lineOptions);
        }
    }


    private String downloadUrl(String strUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(strUrl);

            // Creating an http connection to communicate with url
            urlConnection = (HttpURLConnection) url.openConnection();

            // Connecting to url
            urlConnection.connect();

            // Reading data from url
            iStream = urlConnection.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));
            StringBuffer sb = new StringBuffer();
            String line = "";
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            data = sb.toString();
            br.close();
        } catch (Exception e) {
            Log.d("Exception while downloading url", e.toString());
        } finally {
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }



}
