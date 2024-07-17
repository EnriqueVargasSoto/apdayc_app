package com.expediodigital.ventas360.view;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Path;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import androidx.annotation.Nullable;

import com.expediodigital.ventas360.Ventas360App;
import com.expediodigital.ventas360.quickaction.ActionItem;
import com.expediodigital.ventas360.quickaction.QuickAction;
import com.expediodigital.ventas360.service.GPSTracker;
import com.expediodigital.ventas360.util.APIClient;
import com.expediodigital.ventas360.util.APIInterface;
import com.expediodigital.ventas360.util.ImageJavierDTO;
import com.expediodigital.ventas360.util.PermissionUtil;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.MenuItemCompat;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;

import android.provider.Settings;
import android.text.Html;
import android.text.TextUtils;
import android.transition.Slide;
import android.transition.Transition;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.expediodigital.ventas360.DAO.DAOCliente;
import com.expediodigital.ventas360.DAO.DAOEncuesta;
import com.expediodigital.ventas360.R;
import com.expediodigital.ventas360.adapter.RecyclerViewEncuestaClienteAdapter;
import com.expediodigital.ventas360.model.ClienteModel;
import com.expediodigital.ventas360.model.EncuestaDetalleModel;
import com.expediodigital.ventas360.model.EncuestaDetallePreguntaModel;
import com.expediodigital.ventas360.model.EncuestaRespuestaDetalleModel;
import com.expediodigital.ventas360.model.EncuestaRespuestaModel;
import com.expediodigital.ventas360.service.UploadPhotoService;
import com.expediodigital.ventas360.service.UploadResultReceiver;
import com.expediodigital.ventas360.util.BitmapConverter;
import com.expediodigital.ventas360.util.MenuItemCustomListener;
import com.expediodigital.ventas360.util.SoapManager;
import com.expediodigital.ventas360.util.TablesHelper;
import com.expediodigital.ventas360.util.Util;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.xmlpull.v1.XmlPullParserException;

import java.io.File;
import java.net.SocketTimeoutException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Kevin Robinson Meza Hinostroza on julio 2017.
 * Expedio Digital
 * Kevin.Meza@expediodigital.com
 */

public class EncuestasClientesActivity extends AppCompatActivity implements SearchView.OnQueryTextListener, UploadResultReceiver.AppReceiver {
    public final String TAG = this.getClass().getName();
    public static final String ACTION_SHOW_DIALOG = "action_show_dialog";
    public static final int REQUEST_CODE_ENCUESTA_CLIENTE = 1;
    public static final int QUICK_ITEM_COMPLETAR_ENCUESTA = 11;
    public static final int QUICK_ITEM_SUBIR_FOTO = 12;
    private static final int REQUEST_CODE_UBICACION = 33;
    private static final int REQUEST_PERMISOS_UBICACION = 54;

    private RecyclerView recycler_clientes;
    private FloatingActionButton fab_obtenerEncuestas;
    private TextView tv_cantidadTotal;
    private TextView tv_cantidadEncuestados;
    private TextView tv_cantidadPendientes;
    private SearchView searchView;
    private RecyclerViewEncuestaClienteAdapter adapter;
    private ArrayList<ClienteModel> listaCliente = new ArrayList<>();
    private ArrayList<ClienteModel> listaClientesPendientes = new ArrayList<>();
    private EncuestaDetalleModel encuestaDetalleModel;
    private DAOCliente daoCliente;
    private DAOEncuesta daoEncuesta;
    private SoapManager soapManager;
    ClienteModel mCurrentClient;

    private UploadResultReceiver resultReceiver;
    String[] PERMISSIONS = {Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};
    private final int PERMISSIONS_REQUEST_READ_CONTACTS = 4125;

    int idEncuesta = 0;
    int idEncuestaDetalle = 0;
    String descripcionEncuesta = "";
    String tipoEncuesta = "";
    int numeroEncuestasPendientes = 0;
    boolean isSeaching = false;
    String textSearching = "";

    //Variables para el dialogo de encuestas pendientes
    RecyclerViewEncuestaClienteAdapter adapterPendientes;
    TextView tv_mensaje;
    ProgressBar progressBar;
    String textoMensaje="";
    AlertDialog dialogPendientes;
    Button btn_negative;
    Button btn_positive;
    boolean isServiceRunning = false;
    Ventas360App ventas360App;
    GPSTracker gpsTracker;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Transition transition = new Slide(Gravity.RIGHT);//Transition transition =  new Explode();
            transition.setDuration(150);//Cuánto se demoran en llegar hasta este activity
            getWindow().setEnterTransition(transition);
        }
        setContentView(R.layout.activity_clientes_encuestas);
        ventas360App = (Ventas360App) getApplicationContext();

        Bundle bundle = getIntent().getExtras();

        idEncuesta = bundle.getInt("idEncuesta",0);
        idEncuestaDetalle = bundle.getInt("idEncuestaDetalle",0);
        descripcionEncuesta = bundle.getString("descripcionEncuesta","Encuesta");
        tipoEncuesta = bundle.getString("tipoEncuesta","");

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Util.actualizarToolBar(descripcionEncuesta,tipoEncuesta,true,this);

        soapManager = new SoapManager(getApplicationContext());
        daoCliente = new DAOCliente(getApplicationContext());
        daoEncuesta = new DAOEncuesta(getApplicationContext());

        encuestaDetalleModel = daoEncuesta.getEncuestaDetalle(idEncuesta,idEncuestaDetalle);

        recycler_clientes       = (RecyclerView) findViewById(R.id.recycler_clientes);
        fab_obtenerEncuestas    = (FloatingActionButton) findViewById(R.id.fab_obtenerEncuestas);
        tv_cantidadTotal        = (TextView) findViewById(R.id.tv_cantidadTotal);
        tv_cantidadEncuestados  = (TextView) findViewById(R.id.tv_cantidadEncuestados);
        tv_cantidadPendientes   = (TextView) findViewById(R.id.tv_cantidadPendientes);

        /*fab_obtenerEncuestas.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Generar backup antes de la sincronizacion
                //Util.backupdDatabase();
                //
            }
        });*/

        // creo el adapter y lo enlazo con el cardview
        adapter = new RecyclerViewEncuestaClienteAdapter(listaCliente, this, idEncuesta,idEncuestaDetalle,descripcionEncuesta, tipoEncuesta, true);
        recycler_clientes.addItemDecoration(new DividerItemDecoration(getApplicationContext(),DividerItemDecoration.VERTICAL));
        recycler_clientes.setAdapter(adapter);
        refreshLista();

        String action = getIntent().getAction();
        if(action != null && action.equals(ACTION_SHOW_DIALOG)){
            showEncuestasPendientesDialog();
        }

        createActions();
        IniciarLocalizador();
    }

    private void IniciarLocalizador() {
        gpsTracker = new GPSTracker(this);

        if (gpsTracker.isGPSEnabled()){
            if (Build.VERSION.SDK_INT >= 23){
                if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED
                        || ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_PERMISOS_UBICACION);
                }else
                    gpsTracker.getLocations();
            }else
                gpsTracker.getLocations();
        }else {
            showDialogoUbicacion();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        gpsTracker.stopUsingGPS();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        gpsTracker.stopUsingGPS();
    }

    @Override
    public void onResume() {
        super.onResume();
        gpsTracker.getLocations();
    }

    private void showDialogoUbicacion() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle("Ubicación");
        alertDialog.setMessage("Es necesario que active la ubicación del teléfono en precisión alta");
        alertDialog.setCancelable(false);

        alertDialog.setPositiveButton("ACEPTAR", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivityForResult(intent,REQUEST_CODE_UBICACION);
            }
        });
        alertDialog.show();
    }

    void createActions()
    {
        QuickAction quickAction = new QuickAction(this);
        ActionItem quickItemEncuesta = new ActionItem(QUICK_ITEM_COMPLETAR_ENCUESTA, "Tomar encuesta", ContextCompat.getDrawable(this,R.drawable.encuesta));
        ActionItem quickItemFoto = new ActionItem(QUICK_ITEM_SUBIR_FOTO, "Enviar foto",ContextCompat.getDrawable(this,R.drawable.foto2));

        quickAction.addActionItem(quickItemEncuesta);
        //quickAction.addActionItem(quickItemFoto);

        quickAction.setOnActionItemClickListener(new QuickAction.OnActionItemClickListener() {
            @Override
            public void onItemClick(QuickAction source, int pos, int actionId) {
                quickItemAccion(actionId);
            }
        });

        adapter.setQuickAction(quickAction);
    }

    private void quickItemAccion(int actionId) {
        ClienteModel model = adapter.getItemSelectedByQuickAction();
        mCurrentClient = model;
        switch (actionId){
            case QUICK_ITEM_COMPLETAR_ENCUESTA:
                Intent intent = new Intent(this, EncuestaClienteActivity.class);
                intent.putExtra("idCliente",model.getIdCliente());
                intent.putExtra("razonSocial",model.getRazonSocial());
                intent.putExtra("descripcionEncuesta",descripcionEncuesta);
                intent.putExtra("tipoEncuesta",tipoEncuesta);
                intent.putExtra("idEncuesta",idEncuesta);
                intent.putExtra("idEncuestaDetalle",idEncuestaDetalle);
                startActivityForResult(intent, EncuestasClientesActivity.REQUEST_CODE_ENCUESTA_CLIENTE);

                break;
            case QUICK_ITEM_SUBIR_FOTO:
                //Subir la foto
                if (hasPermissions(this, PERMISSIONS)) {
                    Intent intent1 = new Intent(this, CameraActivity.class);
                    startActivityForResult(intent1, 123);
                } else {
                    ActivityCompat.requestPermissions(EncuestasClientesActivity.this, PERMISSIONS, PERMISSIONS_REQUEST_READ_CONTACTS);
                }

                break;
            default:
                break;
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[],
                                           int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_READ_CONTACTS: {
                if (PermissionUtil.verifyPermissions(grantResults)) {
                    Intent intent1 = new Intent(EncuestasClientesActivity.this, CameraActivity.class);
                    startActivityForResult(intent1, 123);
                } else {
                    Toast.makeText(this, "Permiso requerido", Toast.LENGTH_SHORT).show();
                }
                break;
            }
            case REQUEST_PERMISOS_UBICACION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    gpsTracker.getLocations();
                } else {
                    Toast.makeText(this, "No se otorgaron permisos de ubicación", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    void sendImageToServer(String imagePath) {

        final ProgressDialog mProgressDialog;
        mProgressDialog = ProgressDialog.show(this, null, null);
        mProgressDialog.setContentView(R.layout.progress_loader);
        mProgressDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        mProgressDialog.setCancelable(false);

        Map<String, RequestBody> partMap = new HashMap<>();
        RequestBody tipo_operacion = RequestBody.create(MultipartBody.FORM, "0");
        RequestBody vendedor_id = RequestBody.create(MultipartBody.FORM, ventas360App.getIdVendedor());
        RequestBody empresa_id = RequestBody.create(MultipartBody.FORM, ventas360App.getIdEmpresa());
        RequestBody sucursal_id = RequestBody.create(MultipartBody.FORM, ventas360App.getIdSucursal());
        RequestBody client_id = RequestBody.create(MultipartBody.FORM, mCurrentClient.getIdCliente());

        double m_lat = 0;
        double m_lng = 0;
        if(gpsTracker != null){
            gpsTracker.getLocations();
            m_lat = gpsTracker.getLatitude();
            m_lng = gpsTracker.getLongitude();
        }
        RequestBody latitude = RequestBody.create(MultipartBody.FORM, String.format("%f",m_lat));
        RequestBody longitude = RequestBody.create(MultipartBody.FORM, String.format("%f",m_lng));
        partMap.put("tipo_operacion",tipo_operacion);
        partMap.put("client_id",client_id);
        partMap.put("empresa_id",empresa_id);
        partMap.put("sucursal_id",sucursal_id);
        partMap.put("vendedor_id",vendedor_id);
        partMap.put("latitude",latitude);
        partMap.put("longitude",longitude);

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
                                AlertDialog.Builder builderSingle = new AlertDialog.Builder(EncuestasClientesActivity.this);
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
                            //Toast.makeText(EncuestasClientesActivity.this,
                            //        "Error al procesar la imagen, intente mas tarde", Toast.LENGTH_SHORT).show();
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

    /**
     * Se ejecuta cuando la actividad es relanzada, pero al ser singleTop solo se notifica que se le está pasando un nuevo intent
     * @param intent
     */
    @Override
    protected void onNewIntent(Intent intent) {

        super.onNewIntent(intent);
        String action = intent.getAction();
        if (action != null && action.equals(ACTION_SHOW_DIALOG)) {
            showEncuestasPendientesDialog();
        }
        Log.w(TAG, "onNewIntent !!!!" + action);
    }

    /*
     * Step 1: Register the intent service in the activity
     * */
    private void registerService() {
        Intent intent = new Intent(getApplicationContext(), UploadPhotoService.class);

        /*
         * Step 2: We pass the ResultReceiver via the intent to the intent service
         * */
        resultReceiver = new UploadResultReceiver(new Handler(), this);
        intent.putExtra("receiver", resultReceiver);
        intent.putExtra("idEncuesta",idEncuesta);
        intent.putExtra("idEncuestaDetalle",idEncuestaDetalle);
        intent.putExtra("descripcionEncuesta",descripcionEncuesta);
        startService(intent);
    }


    public void refreshLista(){
        listaClientesPendientes.clear();
        listaCliente.clear();
        if (encuestaDetalleModel != null){
            listaCliente.addAll(daoCliente.getClientesEncuesta(encuestaDetalleModel));
            adapter.notifyDataSetChanged();
        }

        int numeroClientes = listaCliente.size();
        int numeroEncuestados = 0;
        numeroEncuestasPendientes = 0;
        for (ClienteModel clienteModel: listaCliente) {
            if (clienteModel.tieneEncuesta())
                numeroEncuestados ++;
            if (clienteModel.getFlagEncuesta().equals(EncuestaRespuestaModel.FLAG_PENDIENTE) || clienteModel.getFlagEncuesta().equals(EncuestaRespuestaModel.FLAG_INCOMPLETO)){
                listaClientesPendientes.add(clienteModel);
                numeroEncuestasPendientes ++;
            }
        }
        //adapterPendientes.notifyDataSetChanged();
        int numeroPendientes = numeroClientes - numeroEncuestados;

        tv_cantidadTotal.setText(String.valueOf(numeroClientes));
        tv_cantidadEncuestados.setText(String.valueOf(numeroEncuestados));
        tv_cantidadPendientes.setText(String.valueOf(numeroPendientes));

        invalidateOptionsMenu();
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.menu_activity_encuestas, menu);

        final View menuItemPendientes = menu.findItem(R.id.menu_encuestas_pendientes).getActionView();
        TextView tv_contador = (TextView) menuItemPendientes.findViewById(R.id.tv_contador);

        new MenuItemCustomListener(menuItemPendientes, "Pendientes") {
            @Override
            public void onClick(View v) {
                showEncuestasPendientesDialog();
            }
        }.actualizarTextView(tv_contador,numeroEncuestasPendientes);

        MenuItem itemSearch = menu.findItem(R.id.action_search);
        searchView = (SearchView) MenuItemCompat.getActionView(itemSearch);
        searchView.setOnQueryTextListener(this);
        searchView.setOnSearchClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isSeaching = true;
                menu.findItem(R.id.menu_encuestas_pendientes).setVisible(false);
                searchView.requestFocus();
            }
        });
        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                isSeaching = false;
                if (numeroEncuestasPendientes>0)
                    menu.findItem(R.id.menu_encuestas_pendientes).setVisible(true);
                return false;
            }
        });
        /*Cada vez que se ejecuta el método refreshLista, el menú se refresa, recreando el searchView. En este proceso se pierde el estado que tenía el serchView (Si estaba abierto o si tenia un texto)
        * Lo que se hace es guardar el estado junto al texto en variables y al momento de recrear el searchView, actualizar a como estaba antes de recrear.*/
        if (isSeaching){
            searchView.setIconified(false);
            searchView.setQuery(textSearching,false);
        }

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (numeroEncuestasPendientes == 0){
            menu.findItem(R.id.menu_encuestas_pendientes).setVisible(false);
        }else{
            menu.findItem(R.id.menu_encuestas_pendientes).setVisible(true);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.d(TAG,"SE ESTA EJECUTANDO EL MENU DEL FRAGMENT");
        int id = item.getItemId();
        switch (id){
            case android.R.id.home:
                onBackPressed();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.w(TAG,"onStop");
        /*
         * Step 4: don't forget to clear receiver in order to avoid leaks.
         * */
        if(resultReceiver != null) {
            resultReceiver.setAppReceiver(null);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_ENCUESTA_CLIENTE) {
            if (resultCode == RESULT_OK) {
                refreshLista();
            }
        }
        else if(requestCode == REQUEST_CODE_UBICACION) {
            if (gpsTracker.isGPSEnabled()) {
                if (Build.VERSION.SDK_INT >= 23) {
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                            || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_PERMISOS_UBICACION);
                    } else
                        gpsTracker.getLocations();
                } else
                    gpsTracker.getLocations();
            } else {
                showDialogoUbicacion();
            }
        }
        if (resultCode == 2 && requestCode == 123) {
            if (data.hasExtra("image")) {
                if (!data.getStringExtra("image").equals("")) {

                    String imagePath = data.getStringExtra("image");
                    sendImageToServer(imagePath);
                }
            }
        }
    }

    /*Metodo para mostrar la lista de pendientes*/
    public void showEncuestasPendientesDialog(){
        final AlertDialog.Builder builder = new AlertDialog.Builder(EncuestasClientesActivity.this);
        builder.setTitle("Lista de encuestas pendientes ("+numeroEncuestasPendientes+")");

        View pop = getLayoutInflater().inflate(R.layout.dialog_encuestas_pendientes,null); //LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        builder.setView(pop);

        RecyclerView recyclerPendientes = pop.findViewById(R.id.recycler_lista);
        tv_mensaje = pop.findViewById(R.id.tv_mensaje);
        progressBar = pop.findViewById(R.id.progressBar);

        adapterPendientes = new RecyclerViewEncuestaClienteAdapter(listaClientesPendientes, this, idEncuesta, idEncuestaDetalle, descripcionEncuesta, tipoEncuesta, false);
        recyclerPendientes.setAdapter(adapterPendientes);
        recyclerPendientes.addItemDecoration(new DividerItemDecoration(this,DividerItemDecoration.VERTICAL));
        //recyclerPendientes.setPadding(0,25,0,0);
        /*Seteamos los botones en null ya que crearemos un boton personalizado, que no cierre automaticamente el dialogo luego de presionarlo
        Esto para que se pueda verificar que si los campos no esta llenados, muestre una indicación y el usuario no asuma que ya se registró un item*/
        if (Util.isServiceRunning(getApplicationContext())) {
            isServiceRunning = true;
        }else{
            isServiceRunning = false;
            builder.setPositiveButton("ENVIAR AL SERVIDOR",null);
        }
        builder.setNegativeButton("CANCELAR",null);

        dialogPendientes = builder.create();
        dialogPendientes.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(final DialogInterface dialogInterface) {
                btn_positive = dialogPendientes.getButton(AlertDialog.BUTTON_POSITIVE);
                btn_negative = dialogPendientes.getButton(AlertDialog.BUTTON_NEGATIVE);
                /*Setear los valores determinados al abrir el dialogo*/
                btn_positive.setVisibility(View.VISIBLE);
                btn_negative.setVisibility(View.VISIBLE);
                tv_mensaje.setVisibility(View.GONE);

                if (isServiceRunning)
                    progressBar.setVisibility(View.VISIBLE);
                else
                    progressBar.setVisibility(View.GONE);

                btn_positive.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (!listaClientesPendientes.isEmpty()){
                            dialogPendientes.setCancelable(false);
                            textoMensaje = "<font>Enviando...</font>";
                            tv_mensaje.setText(Html.fromHtml(textoMensaje));
                            tv_mensaje.setVisibility(View.VISIBLE);
                            progressBar.setVisibility(View.VISIBLE);
                            btn_positive.setVisibility(View.INVISIBLE);
                            btn_negative.setVisibility(View.INVISIBLE);
                            enviarEncuesta(listaClientesPendientes);
                        }else{
                            Toast.makeText(EncuestasClientesActivity.this, "No hay encuestas pendientes", Toast.LENGTH_SHORT).show();
                        }
                        /*if (!Util.isServiceRunning(getApplicationContext()))
                            registerService();
                        Toast.makeText(EncuestasClientesActivity.this, "Enviando encuestas", Toast.LENGTH_SHORT).show();
                        dialogPendientes.dismiss();*/
                    }
                });

                btn_negative.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialogPendientes.dismiss();
                    }
                });
            }
        });
        dialogPendientes.show();
    }

    public void enviarEncuesta(final ArrayList<ClienteModel> listaPendientes){
        if (!listaPendientes.isEmpty()){
            final EncuestaRespuestaModel encuestaRespuestaModel = daoEncuesta.getEncuestaRespuesta(idEncuesta,idEncuestaDetalle,listaPendientes.get(0).getIdCliente());

            new AsyncTask<Void, Void, String>() {
                final String ERROR_FOTO_NO_ENCONTRADA = "NO_FOTO";
                final String SIN_CONEXION = "SIN_CONEXION";
                protected void onPreExecute() {}
                protected String doInBackground(Void... params) {
                    if (Util.isConnectingToRed(getApplicationContext()) && Util.isConnectingToInternet()) {
                        try {
                            /*Desde este activity se debe enviar toda la encuesta con las fotos, obtener la foto en cadena antes de enviarlo a la webservice*/
                            for (EncuestaRespuestaDetalleModel respuestaDetalleModel : encuestaRespuestaModel.getDetalle()) {
                                if (respuestaDetalleModel.getTipoRespuesta().equals(EncuestaDetallePreguntaModel.TIPO_RESPUESTA_FOTO)) {
                                    Log.d(TAG, "getFotoURL:" + respuestaDetalleModel.getFotoURL());
                                    File profileImageFile = new File(respuestaDetalleModel.getFotoURL());
                                    if(!profileImageFile.exists())
                                    {
                                        Log.d(TAG, respuestaDetalleModel.getFotoURL() + " no existe");
                                        continue;
                                    }

//                                    String base64 = BitmapConverter.convertirImagenString(respuestaDetalleModel.getFotoURL());
//                                    respuestaDetalleModel.setStringFoto(base64);

                                    //enviar a nuestros servidores la foto
                                    sendImage(respuestaDetalleModel.getFotoURL(), respuestaDetalleModel.getLatitud(), respuestaDetalleModel.getLongitud());
                                }
                            }
                        /*La encuesta debe enviarse con el flag E, porque si se envía con P o I, la webservice mantendrá esos flag.
                        Y lo que se quiere ahora es que si se ingrese la encuesta completa y deje finalmente el flag en E*/
                            encuestaRespuestaModel.setFlag(EncuestaRespuestaModel.FLAG_ENVIADO);

                            Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
                            Log.i(TAG, gson.toJson(encuestaRespuestaModel));
                            String cadena = gson.toJson(encuestaRespuestaModel);
                            String respuesta = soapManager.enviarEncuesta(TablesHelper.EncuestaRespuestaCabecera.ActualizarEncuesta, cadena);
                            return daoEncuesta.actualizarFlagEncuesta(idEncuesta, idEncuestaDetalle, encuestaRespuestaModel.getIdCliente(), respuesta);
                        } catch (NullPointerException e){
                            e.printStackTrace();
                            Log.e(TAG,"Archivo no encontrado");
                            return ERROR_FOTO_NO_ENCONTRADA;
                        } catch (XmlPullParserException e){
                            e.printStackTrace();
                            return SIN_CONEXION;
                        } catch (SocketTimeoutException e){
                            e.printStackTrace();
                            Log.e(TAG,"ERROR DE CONEXION SOCKET");
                            return SIN_CONEXION;
                        } catch (Exception e) {
                            e.printStackTrace();
                            return e.getMessage();
                        }
                    }else {
                        return SIN_CONEXION;
                    }

                }
                protected void onPostExecute(String result) {
                    switch (result) {
                        case SIN_CONEXION:
                            textoMensaje += "<br><font color=\"red\">Encuesta para "+encuestaRespuestaModel.getIdCliente()+" No se pudo conectar.</font>";
                            break;
                        case EncuestaRespuestaModel.FLAG_ENVIADO:
                            textoMensaje += "<br><font color=\"green\">Encuesta para "+encuestaRespuestaModel.getIdCliente()+" enviado.</font>";
                            break;
                        case EncuestaRespuestaModel.FLAG_PENDIENTE:
                            textoMensaje += "<br><font color=\"#FFA500\">Encuesta para "+encuestaRespuestaModel.getIdCliente()+" se mantuvo pendiente.</font>";
                            break;
                        case EncuestaRespuestaModel.FLAG_INCOMPLETO:
                            textoMensaje += "<br><font color=\"#FFA500\">Encuesta para "+encuestaRespuestaModel.getIdCliente()+" incompleto.</font>";
                            break;
                        case ERROR_FOTO_NO_ENCONTRADA:
                            textoMensaje += "<br><font color=\"red\">Encuesta para "+encuestaRespuestaModel.getIdCliente()+" imágenes no encontradas.</font>";
                            break;
                        default:
                            textoMensaje += "<br><font color=\"red\">Encuesta para "+encuestaRespuestaModel.getIdCliente()+" reintentar.</font>";
                            break;
                    }
                    tv_mensaje.setText(Html.fromHtml(textoMensaje));
                    if (!result.equals(SIN_CONEXION)){
                        ArrayList<ClienteModel> listaRestante = new ArrayList<>(listaPendientes);
                        listaRestante.remove(0);
                        refreshLista();//Actualiza la lista principal y la de pendientes
                        adapterPendientes.notifyDataSetChanged();//notifica a la vista que la lista de pendientes ha cambiado, a fin de actualizarlo
                        enviarEncuesta(listaRestante);
                    }else{
                        progressBar.setVisibility(View.GONE);
                        dialogPendientes.setCancelable(true);
                        btn_negative.setVisibility(View.VISIBLE);
                        btn_positive.setVisibility(View.VISIBLE);
                    }

                }


                void sendImage(String imagePath, double _latitude, double _longitude) {
                    int index = imagePath.lastIndexOf("\\");
                    String fileName = imagePath.substring(index + 1);
                    String[] arr_data = fileName.split("_");
                    String idCliente = arr_data[2];

                    Map<String, RequestBody> partMap = new HashMap<>();
                    RequestBody tipo_operacion = RequestBody.create(MultipartBody.FORM, "1");
                    RequestBody vendedor_id = RequestBody.create(MultipartBody.FORM, ventas360App.getIdVendedor());
                    RequestBody empresa_id = RequestBody.create(MultipartBody.FORM, ventas360App.getIdEmpresa());
                    RequestBody sucursal_id = RequestBody.create(MultipartBody.FORM, ventas360App.getIdSucursal());
                    RequestBody client_id = RequestBody.create(MultipartBody.FORM, idCliente);
                    RequestBody latitude = RequestBody.create(MultipartBody.FORM, String.format("%f",_latitude));
                    RequestBody longitude = RequestBody.create(MultipartBody.FORM, String.format("%f",_longitude));
                    partMap.put("tipo_operacion",tipo_operacion);
                    partMap.put("client_id",client_id);
                    partMap.put("empresa_id",empresa_id);
                    partMap.put("sucursal_id",sucursal_id);
                    partMap.put("vendedor_id",vendedor_id);
                    partMap.put("latitude",latitude);
                    partMap.put("longitude",longitude);
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
                                    if (response.isSuccessful()) {
                                        Log.d("VentasApp360","¡Imagen enviado con exito!");
                                    } else {
                                        Log.d("VentasApp360","Error al procesar la imagen, intente mas tarde");
                                    }
                                }

                                @Override
                                public void onFailure(Call<ImageJavierDTO> call, Throwable t) {
                                    Log.e(TAG, t.toString());
                                }
                            });

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }


                }


            }.execute();
        } else{
            //textoMensaje = "<font>Enviando...</font>";
            //tv_mensaje.setText(Html.fromHtml(textoMensaje));
            //tv_mensaje.setVisibility(View.GONE);
            progressBar.setVisibility(View.GONE);
            dialogPendientes.setCancelable(true);
            btn_negative.setVisibility(View.VISIBLE);
            btn_positive.setVisibility(View.VISIBLE);
        }

    }


    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        filtrarCliente(newText);
        textSearching = newText;
        return true;
    }

    public void filtrarCliente(String newText) {
        List<ClienteModel> filteredModelList = filtrar(listaCliente, newText);
        adapter.setFilter(filteredModelList);
    }

    private List<ClienteModel> filtrar(ArrayList<ClienteModel> listaClientes, String query) {
        query = query.toLowerCase();
        final List<ClienteModel> listaFiltrada = new ArrayList<>();
        for (ClienteModel cliente : listaClientes) {

            //Si se busca por codigo
            if (TextUtils.isDigitsOnly(query)) {
                String codigo = cliente.getIdCliente();
                if (codigo.contains(query)) {
                    listaFiltrada.add(cliente);
                }
            } else {
                //De lo contrario se filtra por el nombre
                String descripcion = cliente.getRazonSocial().toLowerCase();
                if (descripcion.contains(query.toLowerCase().trim())) {
                    listaFiltrada.add(cliente);
                }
            }
        }
        return listaFiltrada;
    }

    @Override
    public void onReceiveResult(int resultCode, Bundle resultData) {
        Log.i(TAG,"resultCode: "+resultCode);
        Log.i(TAG,"resultData: "+resultData.getString("data"));
        Toast.makeText(this, ""+resultData.getString("data"), Toast.LENGTH_SHORT).show();
        refreshLista();
        //Util.actualizarToolBar("Encuesta actualizada",tipoEncuesta,true,this);
        if (dialogPendientes!=null && dialogPendientes.isShowing()) {
            dialogPendientes.dismiss();
        }
        showEncuestasPendientesDialog();
    }



}


//UploadFile
//ahora solo me envias dos parametros la imagen en base64Binary y su nombre sin .jpg