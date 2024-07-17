package com.expediodigital.ventas360.view;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;

import androidx.annotation.NonNull;

import com.expediodigital.ventas360.model.CantidadBotella;
import com.expediodigital.ventas360.model.ResultBotella;
import com.expediodigital.ventas360.util.ImageJavierDTO;
import com.expediodigital.ventas360.util.ListenerSincronizacion;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.expediodigital.ventas360.DAO.DAOConfiguracion;
import com.expediodigital.ventas360.R;
import com.expediodigital.ventas360.Ventas360App;
import com.expediodigital.ventas360.model.VendedorModel;
import com.expediodigital.ventas360.util.APIClient;
import com.expediodigital.ventas360.util.APIInterface;
import com.expediodigital.ventas360.util.AppConstants;
import com.expediodigital.ventas360.util.ImageBotellasDTO;
import com.expediodigital.ventas360.util.ManageSincronizacion;
import com.expediodigital.ventas360.util.PermissionUtil;
import com.expediodigital.ventas360.view.fragment.CierreDeVentafragment;
import com.expediodigital.ventas360.view.fragment.ClientesListaFragment;
import com.expediodigital.ventas360.view.fragment.DevolucionesFragment;
import com.expediodigital.ventas360.view.fragment.EncuestasFragment;
import com.expediodigital.ventas360.view.fragment.EstadisticasFragment;
import com.expediodigital.ventas360.view.fragment.MapFragment;
import com.expediodigital.ventas360.view.fragment.PedidosFragment;
import com.expediodigital.ventas360.view.fragment.PreLiquidacionFragment;
import com.expediodigital.ventas360.view.fragment.ProductosFragment;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MenuPrincipalActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, AppConstants, ListenerSincronizacion {
    public static final String TAG = "MenuPrincipalActivity";
    private static final int REQUEST_CODE_SINCRONIZAR = 0;
    private int ITEM_MENU_ID_SELECTED = -1;
    private boolean refreshedByResult = false;
    Ventas360App ventas360App;
    NavigationView navigationView;
    DrawerLayout drawer;
    TextView tv_vendedor, tv_tipoVendedor, tv_sucursal, tv_almacen, tv_numeroGuia, tv_fecha, tv_ruta;
    LinearLayout linear_background;
    DAOConfiguracion daoConfiguracion;
    private final int PERMISSIONS_REQUEST_READ_CONTACTS = 1234;
    String[] PERMISSIONS = {Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};

    ManageSincronizacion ms;
    ProgressBar prgBar;
    LinearLayout layPrgBar;

    FloatingActionButton fabSincronizar;
    float dX;
    float dY;
    int lastAction;
    int currFrame=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_principal);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ventas360App = (Ventas360App) getApplicationContext();
        daoConfiguracion = new DAOConfiguracion(getApplicationContext());
        fabSincronizar = findViewById(R.id.fabSincronizar);
        fabSincronizar.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                switch (event.getActionMasked()) {
                    case MotionEvent.ACTION_DOWN:
                        dX = view.getX() - event.getRawX();
                        dY = view.getY() - event.getRawY();
                        lastAction = MotionEvent.ACTION_DOWN;
                        break;

                    case MotionEvent.ACTION_MOVE:
                        view.setY(event.getRawY() + dY);
                        view.setX(event.getRawX() + dX);
                        lastAction = MotionEvent.ACTION_MOVE;
                        break;

                    case MotionEvent.ACTION_UP:
                        if (lastAction == MotionEvent.ACTION_DOWN)
                            fabSincronizar.setEnabled(false);
                        ManageSincronizacion ms = new ManageSincronizacion(MenuPrincipalActivity.this, prgBar, ManageSincronizacion.ORIGEN_MENU, fabSincronizar, layPrgBar,
                                -1, null, MenuPrincipalActivity.this);
                        ms.iniciarSincronizacion();
                        break;

                    default:
                        return false;
                }
                return true;
            }
        });

        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        View hView = navigationView.getHeaderView(0); //Se debe obtener la vista del Header
        tv_fecha = hView.findViewById(R.id.tv_fecha);
        tv_vendedor = hView.findViewById(R.id.tv_vendedor);
        tv_tipoVendedor = hView.findViewById(R.id.tv_tipoVendedor);
        tv_sucursal = hView.findViewById(R.id.tv_sucursal);
        tv_almacen = hView.findViewById(R.id.tv_almacen);
        tv_ruta = hView.findViewById(R.id.tv_ruta);
        tv_numeroGuia = hView.findViewById(R.id.tv_numeroGuia);
        linear_background = hView.findViewById(R.id.linear_background);

        cargarDatosVendedorConfiguracion();

        drawer.openDrawer(GravityCompat.START);

        obtenerAlmacenVendedor();

        hideItem();

        //Util.popupAskBonificaciones(this);
        prgBar = findViewById(R.id.simpleProgressBar);
        layPrgBar = findViewById(R.id.layPrgBar);
        fabSincronizar.setEnabled(false);

        ManageSincronizacion ms0 = new ManageSincronizacion(this, prgBar, ManageSincronizacion.ORIGEN_MENU, fabSincronizar, layPrgBar, -1, null, this);
        ms0.iniciarSincronizacion();
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        //getMenuInflater().inflate(R.menu.menu_bowel, menu);
//        return super.onCreateOptionsMenu(menu);
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        int item_id = item.getItemId();
//        switch (item_id) {
//            case R.id.botellas:
//                if (hasPermissions(this, PERMISSIONS)) {
//                    Intent intent1 = new Intent(this, CameraActivity.class);
//                    startActivityForResult(intent1, 123);
//                } else {
//                    ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSIONS_REQUEST_READ_CONTACTS);
//                }
//                break;
//        }
//
//        return super.onOptionsItemSelected(item);
//    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //Se guarda el item que se seleccionó, para que no se pierda cuando se rote la pantalla y se pueda refrescar el fragment correspondiente
        outState.putInt("ITEM_MENU_ID_SELECTED", ITEM_MENU_ID_SELECTED);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        //Se carga el item que se seleccionó antes de rotar la pantalla
        try {
            ITEM_MENU_ID_SELECTED = savedInstanceState.getInt("ITEM_MENU_ID_SELECTED");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void obtenerAlmacenVendedor() {
        String idAlmacen = ventas360App.getIdAlmacen();
        if (idAlmacen.isEmpty()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setIcon(R.drawable.ic_dialog_alert);
            builder.setTitle("Importante");
            builder.setMessage("Este vendedor no tiene asignado ningún almacén, es necesario para realizar pedidos o buscar productos.\ncomuníquese con el administrador");
            builder.setCancelable(false);
            builder.setPositiveButton("ACEPTAR", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                }
            });
            builder.show();
        } else {
            ventas360App.setIdAlmacen(idAlmacen);
            verificarServicios();
        }
    }

    private void verificarServicios() {
        if (ventas360App.getIdServicio().isEmpty()) {
            //Si no se tiene un servicio para la aplicación se debe seleccionar uno
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setIcon(R.drawable.ic_dialog_alert);
            builder.setTitle("Importante");
            builder.setMessage("No se ha establecido un servicio para la aplicación, verificar que el seleccionado sea el correcto.\nEn caso no hayan servicios disponibles, salir y sincronizar antes de iniciar sesión ");
            builder.setCancelable(false);
            builder.setPositiveButton("ACEPTAR", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent configuracion = new Intent(MenuPrincipalActivity.this, ConfiguracionActivity.class);
                    configuracion.putExtra("origen", ConfiguracionActivity.ORIGEN_MENU);
                    startActivityForResult(configuracion, REQUEST_CODE_SINCRONIZAR);
                }
            });
            builder.setNegativeButton("SALIR", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    //Sale al LoginActivity, desde donde puede volver a sincronizar configuraciones
                    finish();
                }
            });
            builder.show();
        }
    }

    public boolean hasPermissions(Context context, String... permissions) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null && permissions != null) {
            for (String permission : permissions) {
                if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[],
                                           int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_READ_CONTACTS: {
                if (PermissionUtil.verifyPermissions(grantResults)) {
                    Intent intent1 = new Intent(MenuPrincipalActivity.this, CameraActivity.class);
                    startActivityForResult(intent1, 123);
                } else {
                    Toast.makeText(this, "Permiso requerido", Toast.LENGTH_SHORT).show();
                }
                break;
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult");
        if (requestCode == REQUEST_CODE_SINCRONIZAR) {
            cargarDatosVendedorConfiguracion();
            verificarServicios();
            refreshFragment();
            refreshedByResult = true;
        } else if (resultCode == 2 && requestCode == 123) {
            if (data.hasExtra("image")) {
                if (!data.getStringExtra("image").equals("")) {

                    String imagePath = data.getStringExtra("image");
                    sendImageToServer(imagePath);
                    processImage(imagePath);
                }
            }
        }
    }

    private void cargarDatosVendedorConfiguracion() {
        String nombreVendedor = ventas360App.getNombreVendedor();
        String idSucursal = ventas360App.getIdSucursal();
        String idAlmacen = ventas360App.getIdAlmacen();
        String numeroGuia = ventas360App.getNumeroGuia();
        String fecha = daoConfiguracion.getFechaString();
        String ruta = daoConfiguracion.getRutaVendedor(ventas360App.getIdEmpresa(),ventas360App.getIdSucursal(),ventas360App.getIdVendedor());

        tv_vendedor.setText(nombreVendedor.toUpperCase());
        tv_fecha.setText("Fecha: " + fecha);
        tv_sucursal.setText("Sucursal: " + idSucursal);
        tv_almacen.setText("Almacén: " + idAlmacen);
        tv_ruta.setText("RUTA: " + ruta);

        if (numeroGuia.isEmpty()) {
            tv_numeroGuia.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.red_400));
            tv_numeroGuia.setText("Sin Guía disponible");
        } else {
            tv_numeroGuia.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.white));
            tv_numeroGuia.setText("");
//            tv_numeroGuia.setText("Número de guía: " + numeroGuia);
        }

        String tipoVendedor = ventas360App.getTipoVendedor();
        if (tipoVendedor.equals(VendedorModel.TIPO_TRANSPORTISTA)) {
            linear_background.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.background_transportista));
            tv_tipoVendedor.setText("TRANSPORTISTA");
        } else if (tipoVendedor.equals(VendedorModel.TIPO_PUNTO_VENTA)) {
            linear_background.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.background_vendedor));
            tv_tipoVendedor.setText("PUNTO DE VENTA");
        } else if (tipoVendedor.equals(VendedorModel.TIPO_MERCADEO)) {
            linear_background.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.background_vendedor));
            tv_tipoVendedor.setText("MERCADEO");
        } else {
            linear_background.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.background_vendedor));
            tv_tipoVendedor.setText("JEFE DE UNIDAD DE NEGOCIO");
//            tv_tipoVendedor.setText("VENDEDOR");
        }
    }

    private void hideItem() {
        navigationView = (NavigationView) findViewById(R.id.nav_view);
        Menu nav_Menu = navigationView.getMenu();
        if (ventas360App.getTipoVendedor().equals(VendedorModel.TIPO_VENDEDOR)) {
            if (ventas360App.getModoVenta().equals(VendedorModel.MODO_AUTOVENTA)) {
                nav_Menu.findItem(R.id.nav_cierreVenta).setVisible(false);
            } else if (ventas360App.getModoVenta().equals(VendedorModel.MODO_PREVENTA)) {
                nav_Menu.findItem(R.id.nav_preLiquidacion).setVisible(false);
                nav_Menu.findItem(R.id.nav_devoluciones).setVisible(false);
            }
        } else if (ventas360App.getTipoVendedor().equals(VendedorModel.TIPO_PUNTO_VENTA)) {
            //cuando el vendedor es TIPO_PUNTO_VENTA solo puede ser preventa
            nav_Menu.findItem(R.id.nav_preLiquidacion).setVisible(false);
            nav_Menu.findItem(R.id.nav_devoluciones).setVisible(false);
        } else if (ventas360App.getTipoVendedor().equals(VendedorModel.TIPO_MERCADEO)) {
            nav_Menu.findItem(R.id.nav_preLiquidacion).setVisible(false);
            nav_Menu.findItem(R.id.nav_devoluciones).setVisible(false);
        }
        nav_Menu.findItem(R.id.nav_mapa).setVisible(false);
        //nav_Menu.findItem(R.id.nav_encuesta).setVisible(false);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
       /* if (id != R.id.nav_configuracion && id!= R.id.nav_salir){
            ITEM_MENU_ID_SELECTED = id;
        }*/

        switch (id) {
            case R.id.nav_clientes:
                currFrame = 1;
                addFragment(new ClientesListaFragment());
                break;
            case R.id.nav_encuesta:
                currFrame = 2;
                addFragment(new EncuestasFragment());
                break;
            case R.id.nav_producto:
                currFrame = 3;
                addFragment(new ProductosFragment());
                break;
            case R.id.nav_tracking:
                openPage();
                break;
            case R.id.nav_pedidos:
                currFrame = 4;
                addFragment(new PedidosFragment());
                break;
            case R.id.nav_estadisticas:
                currFrame = 5;
                addFragment(new EstadisticasFragment());
                break;
            case R.id.nav_mapa:
                currFrame = 6;
                addFragment(new MapFragment());
                break;
            case R.id.nav_preLiquidacion:
                currFrame = 7;
                addFragment(new PreLiquidacionFragment());
                break;
            case R.id.nav_devoluciones:
                currFrame = 8;
                addFragment(new DevolucionesFragment());
                break;
            case R.id.nav_cierreVenta:
                currFrame = 9;
                addFragment(new CierreDeVentafragment());
                break;
         /*   case R.id.nav_configuracion:
                Intent configuracion = new Intent(MenuPrincipalActivity.this,ConfiguracionActivity.class);
                configuracion.putExtra("origen", ConfiguracionActivity.ORIGEN_MENU);
                startActivityForResult(configuracion,REQUEST_CODE_SINCRONIZAR);
                break;*/
            case R.id.nav_salir:
                Salir();
                break;
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);

        return true;
    }

    private void openPage() {
        String urlTracking = "http://apps.atiendo.pe/apdayc/user/0081/03/preventa/";//daoConfiguracion.getURLTracking();
        if (!urlTracking.equals("")) {
            try {
                Intent myIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(urlTracking + ventas360App.getIdVendedor()));
                startActivity(myIntent);
            } catch (ActivityNotFoundException e) {
                Toast.makeText(this, "No se encontraron aplicaciones."
                        + " Por favor instale algún navegador", Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }
        } else {
            Toast.makeText(MenuPrincipalActivity.this, "No se configuró la página de consulta", Toast.LENGTH_SHORT).show();
        }
    }

    public void addFragment(Fragment fragment) {
        if (fragment != null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.container, fragment, "MY_FRAGMENT")
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                    //.addToBackStack(null)
                    .commit();
        }
    }

    public void refreshFragment() {
        Log.e(TAG, "REFRESCANDO FRAGMENTS");
        switch (ITEM_MENU_ID_SELECTED) {
            case R.id.nav_clientes:
                currFrame = 1;
                addFragment(new ClientesListaFragment());
                break;
            case R.id.nav_encuesta:
                currFrame = 2;
                addFragment(new EncuestasFragment());
                break;
            case R.id.nav_producto:
                currFrame = 3;
                addFragment(new ProductosFragment());
                break;
            case R.id.nav_tracking:
                break;
            case R.id.nav_pedidos:
                currFrame = 4;
                addFragment(new PedidosFragment());
                break;
            case R.id.nav_estadisticas:
                currFrame = 5;
                addFragment(new EstadisticasFragment());
                break;
            case R.id.nav_mapa:
                currFrame = 6;
                addFragment(new MapFragment());
                break;
            case R.id.nav_cierreVenta:
                currFrame = 9;
                addFragment(new CierreDeVentafragment());
                break;
          /*  case R.id.nav_configuracion:
                break;*/
            case R.id.nav_salir:
                break;
        }
        refreshedByResult = false;
    }

    void Salir() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("¿Salir de la aplicación?");
        builder.setPositiveButton("SALIR", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ventas360App.setSesionActiva(false);
                MenuPrincipalActivity.super.onBackPressed();
            }
        });
        builder.setNegativeButton("CANCELAR", null);
        builder.show();
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            drawer.openDrawer(GravityCompat.START);
        }
    }


    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();
        Log.i(TAG, "onResumeFragments");
        //Se refresca el fragment seleccionado cuando se resuma el activity (ya sea por estar minimizado o por haber rotado la pantalla). Se refresca siempre y cuando no se haya acabado de refrescar en el OnActivityResult, sino se hará doble
        if (!refreshedByResult) {
            Log.i(TAG, "Refrescando por resume");
            if (ITEM_MENU_ID_SELECTED == R.id.nav_estadisticas)
                refreshFragment();
        }
        refreshedByResult = false;//se devuelve al estado original, para que
    }

    void sendImageToServer(String imagePath) {

        final ProgressDialog mProgressDialog;
        mProgressDialog = ProgressDialog.show(this, null, null);
        mProgressDialog.setContentView(R.layout.progress_loader);
        mProgressDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        mProgressDialog.setCancelable(false);

        Map<String, RequestBody> partMap = new HashMap<>();
        RequestBody client_id = RequestBody.create(MultipartBody.FORM, ventas360App.getIdVendedor());
        partMap.put("client_id",client_id);

        if (!imagePath.equals("")) {
            try {
                File profileImageFile = new File(imagePath);
                RequestBody propertyImage = RequestBody.create(MediaType.parse("image/*"), profileImageFile);
                MultipartBody.Part profileImage = MultipartBody.Part.createFormData("fileToUpload", profileImageFile.getName(), propertyImage);

                APIInterface apiInterface = APIClient.getClient();
                Call<ImageJavierDTO> call = apiInterface.sendImage(partMap, profileImage);

                call.enqueue(new Callback<ImageJavierDTO>() {
                    @Override
                    public void onResponse(Call<ImageJavierDTO> call, Response<ImageJavierDTO> response) {
                        if (mProgressDialog != null && mProgressDialog.isShowing())
                            mProgressDialog.dismiss();
                        if (response.isSuccessful()) {
                            try {
                                 //nothing
                                AlertDialog.Builder builderSingle = new AlertDialog.Builder(MenuPrincipalActivity.this);
                                builderSingle.setIcon(R.drawable.ic_launcher);
                                builderSingle.setTitle("");
                                builderSingle.setMessage("¡Imagen enviado con exito!");
                                builderSingle.setNeutralButton("Entendido", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                    }
                                });
                                builderSingle.show();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } else {
                            Toast.makeText(MenuPrincipalActivity.this,
                                    "Error al procesar la imagen, intente mas tarde", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ImageJavierDTO> call, Throwable t) {
                        if (mProgressDialog != null && mProgressDialog.isShowing())
                            mProgressDialog.dismiss();
                        Log.e(TAG, t.toString());
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
                if (mProgressDialog != null && mProgressDialog.isShowing())
                    mProgressDialog.dismiss();
            }
        }


    }

    void processImage(String imagePath)
    {
        if(imagePath.isEmpty())
        {
            return;
        }

        Map<String, RequestBody> partMap = new HashMap<>();

        File profileImageFile = new File(imagePath);
        RequestBody propertyImage = RequestBody.create(MediaType.parse("image/*"), profileImageFile);
        MultipartBody.Part profileImage = MultipartBody.Part.createFormData("fileToUpload", profileImageFile.getName(), propertyImage);

        APIInterface apiInterface = APIClient.getClientTemp();
        Call<ImageBotellasDTO> call2 = apiInterface.callProcessImage(partMap, profileImage);
        call2.enqueue(new Callback<ImageBotellasDTO>() {
            @Override
            public void onResponse(Call<ImageBotellasDTO> call, Response<ImageBotellasDTO> response) {
                if (response.isSuccessful()) {
//                    try {
//                        ResultBotella rb = response.body().getData();
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
                } else {
//                    Toast.makeText(MenuPrincipalActivity.this,
//                            "Error al procesar la imagen, intente mas tarde", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ImageBotellasDTO> call, Throwable t) {
//                Log.e(TAG, t.toString());
            }
        });
    }

    @Override
    public void updateData() {

        switch (currFrame)
        {
            case 1:
                ClientesListaFragment myFragment1 = (ClientesListaFragment)getSupportFragmentManager().findFragmentByTag("MY_FRAGMENT");
                if (myFragment1 != null && myFragment1.isVisible()) {
                    myFragment1.refreshLista();
                }
                break;
            case 2:
                EncuestasFragment myFragment2 = (EncuestasFragment)getSupportFragmentManager().findFragmentByTag("MY_FRAGMENT");
                if (myFragment2 != null && myFragment2.isVisible()) {
                    myFragment2.refreshLista();
                }
                break;
            case 3:
                ProductosFragment myFragment3 = (ProductosFragment)getSupportFragmentManager().findFragmentByTag("MY_FRAGMENT");
                if (myFragment3 != null && myFragment3.isVisible()) {
                    myFragment3.refreshLista();
                }
                break;
            case 4:
                PedidosFragment myFragment5 = (PedidosFragment)getSupportFragmentManager().findFragmentByTag("MY_FRAGMENT");
                if (myFragment5 != null && myFragment5.isVisible()) {
                    myFragment5.refreshLista();
                }
                break;
            case 5:
                EstadisticasFragment myFragment6 = (EstadisticasFragment)getSupportFragmentManager().findFragmentByTag("MY_FRAGMENT");
                if (myFragment6 != null && myFragment6.isVisible()) {
                    //myFragment6.refreshLista();
                }
                break;
            case 6:
                MapFragment myFragment7 = (MapFragment)getSupportFragmentManager().findFragmentByTag("MY_FRAGMENT");
                if (myFragment7 != null && myFragment7.isVisible()) {
                    // add your code here
                }
                break;
            case 7:
                PreLiquidacionFragment myFragment8 = (PreLiquidacionFragment)getSupportFragmentManager().findFragmentByTag("MY_FRAGMENT");
                if (myFragment8 != null && myFragment8.isVisible()) {
                    myFragment8.refreshLista();
                }
                break;
            case 8:
                DevolucionesFragment myFragment9 = (DevolucionesFragment)getSupportFragmentManager().findFragmentByTag("MY_FRAGMENT");
                if (myFragment9 != null && myFragment9.isVisible()) {
                    myFragment9.refreshLista();
                }
                break;
            case 9:
                CierreDeVentafragment myFragment10 = (CierreDeVentafragment)getSupportFragmentManager().findFragmentByTag("MY_FRAGMENT");
                if (myFragment10 != null && myFragment10.isVisible()) {
                    myFragment10.cargarResumenVentas();
                }
                break;
        }
    }

    void showResultado(final ResultBotella rb)
    {
        AlertDialog.Builder builderSingle = new AlertDialog.Builder(MenuPrincipalActivity.this);
        builderSingle.setIcon(R.drawable.ic_launcher);
        builderSingle.setTitle("Resultado del analisis");

        String[] items = new String[rb.getDetalle().getResumen().size()];
        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(MenuPrincipalActivity.this, android.R.layout.select_dialog_item);
        int i=0;
        for (CantidadBotella it:rb.getDetalle().getResumen()) {
            arrayAdapter.add(it.getMarca() + ":  " + String.valueOf(it.getCantidad()));
            items[i] = it.getMarca() + ":  " + String.valueOf(it.getCantidad());
            i += 1;
        }
        builderSingle.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });

        builderSingle.setNeutralButton("imagen Resultado", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(rb.getResultado()));
                startActivity(browserIntent);
            }
        });

        builderSingle.setNegativeButton("Finalizar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builderSingle.show();
    }
}
