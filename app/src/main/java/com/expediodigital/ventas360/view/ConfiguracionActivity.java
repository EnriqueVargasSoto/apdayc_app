package com.expediodigital.ventas360.view;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.NonNull;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

import com.expediodigital.ventas360.DAO.DAOConfiguracion;
import com.expediodigital.ventas360.DTO.DTOServicio;
import com.expediodigital.ventas360.R;
import com.expediodigital.ventas360.Ventas360App;
import com.expediodigital.ventas360.migraciones.MigrarProductos1;
import com.expediodigital.ventas360.model.GuiaModel;
import com.expediodigital.ventas360.model.JSONModel;
import com.expediodigital.ventas360.model.VendedorModel;
import com.expediodigital.ventas360.util.DataBaseHelper;
import com.expediodigital.ventas360.util.SoapManager;
import com.expediodigital.ventas360.util.TablesHelper;
import com.expediodigital.ventas360.util.Util;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by Kevin Robinson Meza Hinostroza on julio 2017.
 * Expedio Digital
 * Kevin.Meza@expediodigital.com
 */

public class ConfiguracionActivity  extends AppCompatActivity {
    public static final String TAG = "ConfiguracionActivity";
    private final int REQUEST_PERMISOS_ALMACENAMIENTO = 1;
    public static final int ORIGEN_LOGIN = 0;
    public static final int ORIGEN_MENU = 1;

    private int ORIGEN;
    ProgressDialog progressDialog;
    SoapManager soap_manager;
    String vendedor, idServicioActual;
    DAOConfiguracion daoConfiguracion;
    Ventas360App ventas360App;
    private ArrayList<DTOServicio> listaServicios;
    ArrayList<String> arrayServidores  = new ArrayList<>();
    private Spinner spinnerServidores;
    ArrayList<Integer> listaTablas = new ArrayList<>(Arrays.asList(0, 1, 2, 3, 4));
    String[] tablas = new String[]{"Clientes", "Ventas", "Productos", "Configuración", "Bonificaciones"};
    final boolean[] checkedTablas = new boolean[]{true, true, true, true, true};

    Switch switch_validarStock,switch_productosSinPrecio,switch_bonificaciones;
    Button btn_backup;

    int posicionServicioSelected = 0;
    boolean accionUsuario = true;//Antes de cada setSelection del spinner indicar si es una accion del usuario para mostrar un mensaje
    boolean usuarioValido = true;

    //TODO temporal
    FloatingActionButton fabChangeTableProduct;
    //---

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configuracion);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Util.actualizarToolBar("Configuración",true,this);

        FloatingActionButton myFab  = (FloatingActionButton) findViewById(R.id.fabSincronizar);
        spinnerServidores = (Spinner) findViewById(R.id.spinnerServicios);

        soap_manager    = new SoapManager(getApplicationContext());
        daoConfiguracion     = new DAOConfiguracion(getApplicationContext());
        ventas360App    = (Ventas360App) getApplicationContext();

        vendedor        = ventas360App.getIdVendedor();
        idServicioActual  = ventas360App.getIdServicio();

        //Log.e(TAG, "Servicio :" +idServicioActual);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null){
            ORIGEN = bundle.getInt("origen");
            if(ORIGEN == ORIGEN_LOGIN){
                tablas = new String[]{"Usuarios","Servicios"};
                listaTablas = new ArrayList<>(Arrays.asList(0, 1));
            }
        }

        switch_validarStock = (Switch) findViewById(R.id.switch_validarStock);
        switch_productosSinPrecio = (Switch) findViewById(R.id.switch_productosSinPrecio);
        switch_bonificaciones = (Switch) findViewById(R.id.switch_bonificaciones);
        btn_backup = (Button) findViewById(R.id.btn_backup);

        spinnerServidores.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(final AdapterView<?> parent, View view, final int position, long id) {

                if (!listaServicios.isEmpty()){
                    final DTOServicio servicioSelected = listaServicios.get(position);

                    if (accionUsuario){
                        AlertDialog.Builder builder = new AlertDialog.Builder(ConfiguracionActivity.this);
                        builder.setTitle("Cambiar de servidor");
                        builder.setMessage("Se cambiará el servidor desde donde sincroniza y envía los datos");
                        builder.setIcon(R.drawable.ic_dialog_alert);
                        builder.setCancelable(false);
                        builder.setPositiveButton("ACEPTAR", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int which) {
                                posicionServicioSelected = position;
                                //Dar valores a campos del web service
                                idServicioActual = servicioSelected.getIdServicio();
                                ventas360App.setIdServicio(servicioSelected.getIdServicio());
                                ventas360App.setUrlWebService(servicioSelected.getUrl());

                                Toast.makeText(getApplicationContext(), "Se guardó el servicio seleccionado ID("+idServicioActual+")", Toast.LENGTH_SHORT).show();
                                soap_manager = new SoapManager(getApplicationContext());//Recargamos SoapManager para que se actualicen los datos de la clase application

                            }
                        });
                        builder.setNegativeButton("CANCELAR", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Se mueve el spinner a su posicion original
                                accionUsuario = false;
                                spinnerServidores.setSelection(posicionServicioSelected);
                            }
                        });
                        builder.show();
                    }else {
                        posicionServicioSelected = position;
                        //Dar valores a campos del web service
                        idServicioActual = servicioSelected.getIdServicio();
                        ventas360App.setIdServicio(servicioSelected.getIdServicio());
                        ventas360App.setUrlWebService(servicioSelected.getUrl());
                    }
                    accionUsuario = true;//Se indica que la proxima accion será del usuario a menos que se indique lo contrrario antes de mandar un setSelection
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });


        myFab.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                boolean mostrarTablas = true;
                //Si no hay servicios, sincronizar
                if (idServicioActual.isEmpty()){
                    //Si no hay servicios y está fuera de la aplicación solo mostrar Servicios para sincronizar
                    if (ORIGEN == ORIGEN_LOGIN){
                        tablas = new String[]{"Servicios"};
                        listaTablas = new ArrayList<>(Arrays.asList(1));
                    }else {
                        //Si no hay servicios y se encuentra dentro de la aplicación, indicar que sincronice Servicios desde la ventana de login
                        Toast.makeText(getApplicationContext(),"No hay servicios disponibles, sincronice antes de iniciar sesión",Toast.LENGTH_SHORT).show();
                        mostrarTablas = false;
                    }
                }

                if (mostrarTablas){
                    AlertDialog.Builder builder = new AlertDialog.Builder(ConfiguracionActivity.this);
                    builder.setMultiChoiceItems(tablas, new boolean[]{true, true, true, true, true}, new DialogInterface.OnMultiChoiceClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int which, boolean isChecked) {
                            if (isChecked)
                                listaTablas.add(which);
                            else if (listaTablas.contains(which))
                                listaTablas.remove(Integer.valueOf(which));
                        }
                    });

                    // Se especifica que el diálogo no se puede cancelar
                    builder.setCancelable(false);
                    builder.setTitle("Sincronizar");

                    // Establece el click listener del botón positivo
                    builder.setPositiveButton("Sincronizar", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // Generar backup antes de la sincronizacion
                            if (Build.VERSION.SDK_INT >= 23) {
                                if (ContextCompat.checkSelfPermission(ConfiguracionActivity.this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED
                                        || ContextCompat.checkSelfPermission(ConfiguracionActivity.this, android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                                    //FragmentCompat.requestPermissions(permissionsList, RequestCode); Para Fragments
                                    ActivityCompat.requestPermissions(ConfiguracionActivity.this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE, android.Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_PERMISOS_ALMACENAMIENTO);
                                } else {
                                    if (Util.backupdDatabase(getApplicationContext())){}
                                    else Toast.makeText(getApplicationContext(), "No se pudo generar backup", Toast.LENGTH_SHORT).show();

                                    new asyncSincronizacion().execute("", "");
                                }
                            } else {
                                if (Util.backupdDatabase(getApplicationContext())){}
                                else Toast.makeText(getApplicationContext(), "No se pudo generar backup", Toast.LENGTH_SHORT).show();
                                new asyncSincronizacion().execute("", "");
                            }
                        }
                    });

                    // Set the neutral/cancel button click listener
                    builder.setNeutralButton("cancelar", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    });

                    AlertDialog dialog = builder.create();
                    dialog.show();
                }

            }
        });

        switch_productosSinPrecio.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                ventas360App.setSettings_productoSinPrecio(checked);
            }
        });

        switch_validarStock.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                ventas360App.setSettings_validarStock(checked);
            }
        });

        switch_bonificaciones.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                ventas360App.setSettings_bonificaciones(checked);
            }
        });

        switch_productosSinPrecio.setChecked(ventas360App.getSettings_productoSinPrecio());
        switch_validarStock.setChecked(ventas360App.getSettings_validarStock());
        switch_bonificaciones.setChecked(ventas360App.getSettings_bonificaciones());

        switch_productosSinPrecio.setEnabled(false);
        switch_validarStock.setEnabled(false);
        switch_bonificaciones.setEnabled(true);

        validarStockProductos();

        btn_backup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent_backUp = new Intent(ConfiguracionActivity.this,BackupActivity.class);
                startActivity(intent_backUp);
            }
        });

        cargarServicios();


    }

    private void validarStockProductos() {
        //Validacion por modo de venta
        if (ventas360App.getModoVenta().equals(VendedorModel.MODO_PREVENTA) && !ventas360App.getSettings_preventaEnLinea()){
            switch_validarStock.setChecked(false);//No se valida stock por defecto
        }else if(ventas360App.getTipoVendedor().equals(VendedorModel.TIPO_MERCADEO)){
            switch_validarStock.setChecked(false);//Los mercaderistas no validan stock
        }else {//Si es autoventa no se puede cambiar nada
            switch_validarStock.setChecked(true);//Se valida stock por defecto
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_PERMISOS_ALMACENAMIENTO){
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (Util.backupdDatabase(getApplicationContext())){}
                else Toast.makeText(getApplicationContext(), "No se pudo generar backup", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "No se otorgaron permisos de almacenamiento para realizar el Backup", Toast.LENGTH_SHORT).show();
            }

            new asyncSincronizacion().execute("", "");
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void cargarServicios(){
        arrayServidores = new ArrayList<>();
        listaServicios = daoConfiguracion.getServicios();

        int itemASeleccionar = 0;

        if (listaServicios.isEmpty()){
            arrayServidores.add("No hay servicios disponibles");
        }else{
            for (int i=0 ; i < listaServicios.size(); i++) {
                String descripcion = listaServicios.get(i).getUrl()+"";

                String tipoServicio = listaServicios.get(i).getTipo();

                try{
                    String url = listaServicios.get(i).getUrl();
                    String[] arrayIP = url.substring(7).split("\\.");
                    if (tipoServicio.equals(DTOServicio.TIPO_PRODUCCION)){
                        descripcion = "[Producción] "+arrayIP[0]+"..............."+arrayIP[3].substring(0,3)+"...";
                    }else if (tipoServicio.equals(DTOServicio.TIPO_DESARROLLO)){
                        descripcion = "[Desarrollo] "+arrayIP[0]+"..............."+arrayIP[3].substring(0,3)+"...";
                    }else{
                        descripcion = "[Servicio] "+arrayIP[0]+"..............."+arrayIP[3].substring(0,3)+"...";
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
                arrayServidores.add(descripcion);

                //Buscar el idServicio previamente guardado para seleccionarlo despues
                if (!idServicioActual.isEmpty()){
                    if (listaServicios.get(i).getIdServicio().equals(idServicioActual)){
                        itemASeleccionar = i;
                    }
                }
            }
        }

        ArrayAdapter<String> adapterServicios = new ArrayAdapter<String>(this,R.layout.spinner_adapter,arrayServidores);
        spinnerServidores.setAdapter(adapterServicios);

        if (listaServicios.isEmpty()){
            idServicioActual = "";
            ventas360App.setIdServicio("");
            ventas360App.setUrlWebService("");
        }else{
            accionUsuario = false;//Indicamos que setSelection no será una accion del usuario para evitar el dialogo de confirmación
            spinnerServidores.setSelection(itemASeleccionar);
            idServicioActual = listaServicios.get(itemASeleccionar).getIdServicio();
            ventas360App.setIdServicio(listaServicios.get(itemASeleccionar).getIdServicio());
            ventas360App.setUrlWebService(listaServicios.get(itemASeleccionar).getUrl());
            soap_manager = new SoapManager(getApplicationContext());//Recargamos SoapManager para que se actualicen los datos de la clase application
            Log.w(TAG,"Estableciendo idServicioActual:"+idServicioActual+" Como servicio inicial");
        }
    }

    class asyncSincronizacion extends AsyncTask<String, String, String> {
        String mensajeSincronizacion = "Sincronizando....";
        protected void onPreExecute() {
            progressDialog = new ProgressDialog(ConfiguracionActivity.this);
            progressDialog.setTitle("");
            progressDialog.setMessage(mensajeSincronizacion);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            progressDialog.setIndeterminate(false);
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        protected String doInBackground(String... params) {
            try {
                if (Util.isConnectingToInternet()){
                    if (ORIGEN == ORIGEN_MENU) {
                        //PRIMERO SINCRONIZAR CONFIGURACIONES ya que ahi está la guía, y si esta cambia se debe actualizar para que desde ya se obtenga todos los datos en base a esta nueva guia
                    /* Configuracion */
                        if (listaTablas.contains(3)) {
                            Log.v(TAG, "------------------------- Sincronizando Configuraciones -------------------------");
                            mensajeSincronizacion = "Sincronizando configuraciones...";
                            publishProgress("1");
                            soap_manager.obtenerRegistrosxSucursalJSON(TablesHelper.Servicio.Sincronizar, TablesHelper.Servicio.Table);
                            publishProgress("30");

                            soap_manager.obtenerRegistrosxVendedorJSON(TablesHelper.Configuracion.Sincronizar, TablesHelper.Configuracion.Table);
                            boolean isPreventaEnLinea = daoConfiguracion.isPreventaEnLinea();
                            ventas360App.setSettings_preventaEnLinea(isPreventaEnLinea);

                            publishProgress("50");
                            soap_manager.obtenerRegistrosJSON(TablesHelper.Empresa.Sincronizar, TablesHelper.Empresa.Table);
                            publishProgress("70");
                            soap_manager.obtenerRegistrosxEmpresaJSON(TablesHelper.MotivoNoVenta.Sincronizar, TablesHelper.MotivoNoVenta.Table);
                            publishProgress("80");
                            soap_manager.obtenerRegistrosxSucursalJSON(TablesHelper.FormaPago.Sincronizar, TablesHelper.FormaPago.Table);
                            if (ventas360App.getModoVenta().equals(VendedorModel.MODO_AUTOVENTA) || isPreventaEnLinea) {
                                publishProgress("90");
                                soap_manager.obtenerRegistrosxVendedorJSON(TablesHelper.Guia.Sincronizar, TablesHelper.Guia.Table);
                            }
                            publishProgress("100");
                        }
                    /* Clientes */
                        if (listaTablas.contains(0)) {
                            Log.v(TAG, "------------------------- Sincronizando Clientes -------------------------");
                            mensajeSincronizacion = "Sincronizando clientes...";
                            publishProgress("1");
                            soap_manager.obtenerRegistrosxVendedorJSON(TablesHelper.Cliente.Sincronizar, TablesHelper.Cliente.Table);
                            ventas360App.setIndexRutaMapa(JSONModel.SIN_RUTA_SELECCIONADA);
                            publishProgress("5");
                            soap_manager.obtenerRegistrosxEmpresaJSON(TablesHelper.Segmento.Sincronizar, TablesHelper.Segmento.Table);
                            publishProgress("8");
                            //soap_manager.obtenerRegistrosxEmpresaJSON(TablesHelper.SubGiro.Sincronizar, TablesHelper.SubGiro.Table);
                            publishProgress("10");
                            soap_manager.obtenerRegistrosxVendedorJSON(TablesHelper.Encuesta.Sincronizar, TablesHelper.Encuesta.Table);
                            publishProgress("15");
                            soap_manager.obtenerRegistrosxVendedorJSON(TablesHelper.EncuestaDetalle.Sincronizar, TablesHelper.EncuestaDetalle.Table);
                            publishProgress("25");
                            soap_manager.obtenerRegistrosxVendedorJSON(TablesHelper.EncuestaDetallePregunta.Sincronizar, TablesHelper.EncuestaDetallePregunta.Table);
                            publishProgress("30");
                            soap_manager.obtenerRegistrosxVendedorJSON(TablesHelper.EncuestaAlternativaPregunta.Sincronizar, TablesHelper.EncuestaAlternativaPregunta.Table);
                            publishProgress("40");
                            soap_manager.obtenerRegistrosxVendedorJSON(TablesHelper.EncuestaDetallexCliente.Sincronizar, TablesHelper.EncuestaDetallexCliente.Table);
                            publishProgress("50");
                            soap_manager.obtenerRegistrosxVendedorJSON(TablesHelper.EncuestaDetallexSegmento.Sincronizar, TablesHelper.EncuestaDetallexSegmento.Table);
                            publishProgress("60");
                            soap_manager.obtenerRegistrosxEmpresaJSON(TablesHelper.EncuestaTipo.Sincronizar, TablesHelper.EncuestaTipo.Table);
                            publishProgress("70");//Iniciar siempre sincronizando EncuestaRespuestaDetalle para que se pueda eliminar correctamente
                            soap_manager.obtenerRegistrosxVendedorJSON(TablesHelper.EncuestaRespuestaDetalle.Sincronizar, TablesHelper.EncuestaRespuestaDetalle.Table);
                            publishProgress("80");
                            soap_manager.obtenerRegistrosxVendedorJSON(TablesHelper.EncuestaRespuestaCabecera.Sincronizar, TablesHelper.EncuestaRespuestaCabecera.Table);
                            publishProgress("90");
                            soap_manager.obtenerRegistrosxVendedorJSON(TablesHelper.ClienteCoordenadas.Sincronizar, TablesHelper.ClienteCoordenadas.Table);
                            publishProgress("100");
                        }
                    /* Vendedores */
                        if (listaTablas.contains(1)) {
                            Log.v(TAG, "------------------------- Sincronizando Ventas -------------------------");
                            mensajeSincronizacion = "Sincronizando ventas...";
                            publishProgress("1");
                            soap_manager.obtenerRegistrosJSON(TablesHelper.Usuario.Sincronizar, TablesHelper.Usuario.Table);
                            publishProgress("10");
                            soap_manager.obtenerRegistrosJSON(TablesHelper.Vendedor.Sincronizar, TablesHelper.Vendedor.Table);
                            ResetearDatosVendedor();//Resetear los datos del vendedor en la clase Application
                            publishProgress("15");
                            soap_manager.obtenerRegistrosxVendedorJSON(TablesHelper.ObjPedido.Sincronizar, TablesHelper.ObjPedido.Table);
                            publishProgress("20");
                            if (ventas360App.getModoVenta().equals(VendedorModel.MODO_AUTOVENTA) || ventas360App.getSettings_preventaEnLinea() || ventas360App.getTipoVendedor().equals(VendedorModel.TIPO_PUNTO_VENTA)) {
                                soap_manager.obtenerRegistrosxSucursalJSON(TablesHelper.PromocionDetalle.Sincronizar, TablesHelper.PromocionDetalle.Table);
                                publishProgress("22");
                                soap_manager.obtenerRegistrosxSucursalJSON(TablesHelper.PromocionxCliente.Sincronizar, TablesHelper.PromocionxCliente.Table);
                                publishProgress("28");
                                soap_manager.obtenerRegistrosxSucursalJSON(TablesHelper.PromocionxPoliticaPrecio.Sincronizar, TablesHelper.PromocionxPoliticaPrecio.Table);
                                publishProgress("30");
                                soap_manager.obtenerRegistrosxSucursalJSON(TablesHelper.PromocionxVendedor.Sincronizar, TablesHelper.PromocionxVendedor.Table);
                            }
                            publishProgress("40");
                            soap_manager.obtenerRegistrosxVendedorJSON(TablesHelper.ObjDevolucion.Sincronizar, TablesHelper.ObjDevolucion.Table);
                            publishProgress("45");
                            soap_manager.obtenerRegistrosxVendedorJSON(TablesHelper.AvanceCuota.Sincronizar, TablesHelper.AvanceCuota.Table);
                            publishProgress("50");
                            soap_manager.obtenerRegistrosxVendedorJSON(TablesHelper.HojaRutaIndicador.Sincronizar, TablesHelper.HojaRutaIndicador.Table);
                            publishProgress("55");
                            soap_manager.obtenerRegistrosxVendedorJSON(TablesHelper.HojaRutaMarcas.Sincronizar, TablesHelper.HojaRutaMarcas.Table);
                            publishProgress("60");
                            soap_manager.obtenerRutasJSON(TablesHelper.ModuloxRuta.Sincronizar, TablesHelper.ModuloxRuta.Table);

                            soap_manager.obtenerRegistrosxVendedorJSON(TablesHelper.HRCliente.Sincronizar, TablesHelper.HRCliente.table);
                            publishProgress("65");
                            soap_manager.obtenerRegistrosxVendedorJSON(TablesHelper.HRVendedor.Sincronizar, TablesHelper.HRVendedor.table);
                            publishProgress("70");
                            soap_manager.obtenerRegistrosxVendedorJSON(TablesHelper.HRMarcaResumen.Sincronizar, TablesHelper.HRMarcaResumen.table);
                            publishProgress("100");
                        }
                    /* Producto */
                        if (listaTablas.contains(2)) {
                            Log.v(TAG, "------------------------- Sincronizando Productos -------------------------");
                            mensajeSincronizacion = "Sincronizando productos...";
                            publishProgress("1");

                            //caso de que la columnas anteriores aun existen
                            if(soap_manager.checkColumnasTablaProductos()){
                                iniciarMigracion();
                            }

                            soap_manager.obtenerRegistrosxVendedorJSON(TablesHelper.Producto.Sincronizar, TablesHelper.Producto.Table);
                            if (ventas360App.getModoVenta().equals(VendedorModel.MODO_AUTOVENTA) || ventas360App.getSettings_preventaEnLinea()) {
                                publishProgress("30");
                                soap_manager.obtenerRegistrosxVendedorJSON(TablesHelper.Kardex.Sincronizar, TablesHelper.Kardex.Table);
                            }
                            if (ventas360App.getModoVenta().equals(VendedorModel.MODO_AUTOVENTA)) {
                                publishProgress("40");
                                soap_manager.obtenerRegistrosxVendedorJSON(TablesHelper.Liquidacion.Sincronizar, TablesHelper.Liquidacion.Table);
                            }
                            publishProgress("50");
                            soap_manager.obtenerRegistrosxEmpresaJSON(TablesHelper.Proveedor.Sincronizar, TablesHelper.Proveedor.Table);
                            publishProgress("60");
                            soap_manager.obtenerRegistrosxEmpresaJSON(TablesHelper.Linea.Sincronizar, TablesHelper.Linea.Table);
                            publishProgress("70");
                            soap_manager.obtenerRegistrosxEmpresaJSON(TablesHelper.Familia.Sincronizar, TablesHelper.Familia.Table);
                            publishProgress("80");
                            soap_manager.obtenerRegistrosxEmpresaJSON(TablesHelper.UnidadMedida.Sincronizar, TablesHelper.UnidadMedida.Table);
                            publishProgress("85");
                            soap_manager.obtenerRegistrosxSucursalJSON(TablesHelper.PoliticaPrecio.Sincronizar, TablesHelper.PoliticaPrecio.Table);
                            publishProgress("90");
                            if(!soap_manager.checkColumnasPoliticaPrecioxProducto()){
                                iniciarMigracionPpp();
                            }
                            soap_manager.obtenerRegistrosxVendedorJSON(TablesHelper.PoliticaPrecioxProducto.Sincronizar2, TablesHelper.PoliticaPrecioxProducto.Table);
                            publishProgress("93");
                            soap_manager.obtenerRegistrosxVendedorJSON(TablesHelper.UnidadMedidaxProducto.Sincronizar2, TablesHelper.UnidadMedidaxProducto.Table);
                            publishProgress("96");
                            soap_manager.obtenerRegistrosxVendedorJSON(TablesHelper.PoliticaPrecioxCliente.Sincronizar, TablesHelper.PoliticaPrecioxCliente.Table);
                            publishProgress("98");
                            soap_manager.obtenerRegistrosxEmpresaJSON(TablesHelper.Marca.Sincronizar, TablesHelper.Marca.Table);
                            publishProgress("100");
                        }
                        /* Bonificaciones */
                        if (listaTablas.contains(4)) {
                            Log.v(TAG, "------------------------- Sincronizando de Bonificaciones -------------------------");
                            mensajeSincronizacion = "Sincronizando bonificaciones...";
                            publishProgress("1");
                            soap_manager.obtenerBonificaionesJSON(TablesHelper.MGRUP1F.Sincronizar, TablesHelper.MGRUP1F.Table);
                            publishProgress("12");
                            soap_manager.obtenerBonificaionesJSON(TablesHelper.MGRUP2F.Sincronizar, TablesHelper.MGRUP2F.Table);
                            publishProgress("25");
                            soap_manager.obtenerBonificaionesJSON(TablesHelper.MPROMO1F.Sincronizar, TablesHelper.MPROMO1F.Table);
                            publishProgress("38");
                            soap_manager.obtenerBonificaionesJSON(TablesHelper.MPROMO2F.Sincronizar, TablesHelper.MPROMO2F.Table);
                            publishProgress("51");
                            soap_manager.obtenerBonificaionesJSON(TablesHelper.MPROMO3F.Sincronizar, TablesHelper.MPROMO3F.Table);
                            publishProgress("63");
                            soap_manager.obtenerBonificaionesJSON(TablesHelper.MPROMO4F.Sincronizar, TablesHelper.MPROMO4F.Table);
                            publishProgress("77");
                            soap_manager.obtenerBonificaionesJSON(TablesHelper.MPROMO5F.Sincronizar, TablesHelper.MPROMO5F.Table);
                            publishProgress("90");
                            soap_manager.obtenerBonificaionesJSON(TablesHelper.MPROMO6F.Sincronizar, TablesHelper.MPROMO6F.Table);
                            publishProgress("100");
                        }
                        soap_manager.actualizarFechaSincronizacion(TablesHelper.Vendedor.ActualizarFechaSincronizacion);
                    } else {
                    /* Usuarios vendedores */
                        if (listaTablas.contains(0)) {
                            Log.v(TAG, "------------------------- Sincronizando Vendedor Usuario -------------------------");
                            mensajeSincronizacion = "Sincronizando usuarios...";
                            publishProgress("1");
                            soap_manager.obtenerRegistrosJSON(TablesHelper.Usuario.Sincronizar, TablesHelper.Usuario.Table);
                            publishProgress("50");
                            soap_manager.obtenerRegistrosJSON(TablesHelper.Vendedor.Sincronizar, TablesHelper.Vendedor.Table);
                            publishProgress("100");
                        }
                    /* Configuracion */
                        if (listaTablas.contains(1)) {
                            Log.v(TAG, "------------------------- Sincronizando Configuraciones y Servicios -------------------------");
                            mensajeSincronizacion = "Sincronizando configuraciones...";
                            publishProgress("1");
                            soap_manager.obtenerRegistrosJSON(TablesHelper.Empresa.Sincronizar, TablesHelper.Empresa.Table);
                            publishProgress("30");
                            soap_manager.obtenerRegistrosxSucursalJSON(TablesHelper.Servicio.Sincronizar, TablesHelper.Servicio.Table);
                            publishProgress("50");

                            soap_manager.obtenerRegistrosxVendedorJSON(TablesHelper.Configuracion.Sincronizar, TablesHelper.Configuracion.Table);
                            boolean isPreventaEnLinea = daoConfiguracion.isPreventaEnLinea();
                            ventas360App.setSettings_preventaEnLinea(isPreventaEnLinea);

                            if (ventas360App.getModoVenta().equals(VendedorModel.MODO_AUTOVENTA) || isPreventaEnLinea) {
                                publishProgress("60");
                                soap_manager.obtenerRegistrosxVendedorJSON(TablesHelper.Guia.Sincronizar, TablesHelper.Guia.Table);
                            }
                            publishProgress("100");
                        }
                    }
                } else{
                    Log.e(TAG,"NoConnectedToInternet");
                    return "NoConnectedToInternet";
                }
            } catch (NoSuchMethodException ex) {
                ex.printStackTrace();
                return "NoSuchMethodException";
            } catch (SocketTimeoutException  ex) {
                ex.printStackTrace();
                return "SocketTimeoutException";
            } catch (IOException ex) {
                ex.printStackTrace();
                return ex.getMessage();
            } catch (Exception e) {
                e.printStackTrace();
                return e.getMessage();
            }

            return "asyncSincronizacion Ok";
        }

        protected void onProgressUpdate(String... progress) {
            progressDialog.setMessage(mensajeSincronizacion);
            progressDialog.setProgress(Integer.parseInt(progress[0]));
        }

        protected void onPostExecute(String result) {
            progressDialog.dismiss();
            Log.d( TAG, "onPostExecute "+ result);
            validarStockProductos();

            //Mostrar las tablas en su valor por defecto
            if (ORIGEN == ORIGEN_MENU){
                listaTablas = new ArrayList<>(Arrays.asList(0, 1, 2, 3, 4));
                tablas = new String[]{"Clientes", "Ventas", "Productos", "Configuración", "Bonificaciones"};
            }else{
                tablas = new String[]{"Usuarios","Servicios"};
                listaTablas = new ArrayList<>(Arrays.asList(0, 1));
            }

            //Volver a cargar los servicios para ver los cambios
            cargarServicios();

            if (result.equals("asyncSincronizacion Ok")) {
                AlertDialog.Builder alerta = new AlertDialog.Builder(ConfiguracionActivity.this);
                alerta.setTitle("Sincronización correcta");
                alerta.setMessage("Se sincronizó correctamente");
                alerta.setIcon(R.drawable.ic_dialog_check);
                alerta.setCancelable(false);
                alerta.setPositiveButton("ACEPTAR", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (!usuarioValido)
                            verificarUsuario();
                        else
                            verificarGuias();
                    }
                });
                alerta.show();
            } else if(result.equals("NoConnectedToInternet")) {
                AlertDialog.Builder alerta = new AlertDialog.Builder(ConfiguracionActivity.this);
                alerta.setTitle("Sin conexión");
                alerta.setMessage("No se pudo acceder a Internet, compruebe su conexión e inténtelo nuevamente.");
                alerta.setIcon(R.drawable.ic_dialog_error);
                alerta.setCancelable(false);
                alerta.setPositiveButton("ACEPTAR", new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        verificarGuias();
                    }
                });
                alerta.show();
            }else if(result.equals("SocketTimeoutException")) {
                AlertDialog.Builder alerta = new AlertDialog.Builder(ConfiguracionActivity.this);
                alerta.setTitle("No se pudo sincronizar");
                alerta.setMessage("Se superó el tiempo de espera en la conexión, por favor intente sincronizar nuevamente");
                alerta.setIcon(R.drawable.ic_dialog_error);
                alerta.setCancelable(false);
                alerta.setPositiveButton("ACEPTAR", new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        verificarGuias();
                    }
                });
                alerta.show();
            } else {
                AlertDialog.Builder alerta = new AlertDialog.Builder(ConfiguracionActivity.this);
                alerta.setTitle("Sincronización incorrecta");
                alerta.setMessage("Algunas tablas no se sincronizaron correctamente. Sincronice nuevamente\n"+"\""+result+"\"");
                alerta.setIcon(R.drawable.ic_dialog_error);
                alerta.setCancelable(false);
                alerta.setPositiveButton("ACEPTAR", new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        verificarGuias();
                    }
                });
                alerta.show();
            }
        }
    }

    private void ResetearDatosVendedor() {
        String user = ventas360App.getUsuario();
        String password = ventas360App.getPassword();
        String ruc = ventas360App.getRucEmpresa();

        VendedorModel vendedorModel = daoConfiguracion.getVendedorUsuario(user,password,ruc);

        if (vendedorModel != null){
            usuarioValido = true;
            //Si se ingresa con otro vendedor, se debe limpiar la base de datos
            if(!( ventas360App.getIdVendedor().equals(vendedorModel.getIdVendedor()) && ventas360App.getIdSucursal().equals(vendedorModel.getIdSucursal()) && ventas360App.getIdEmpresa().equals(vendedorModel.getIdEmpresa()) )){
                daoConfiguracion.limpiarTablas();
            }

            ventas360App.setRucEmpresa(ruc);
            ventas360App.setUsuario(user);
            ventas360App.setIdEmpresa(vendedorModel.getIdEmpresa());
            ventas360App.setIdSucursal(vendedorModel.getIdSucursal());

            ventas360App.setIdVendedor(vendedorModel.getIdVendedor());
            ventas360App.setNombreVendedor(vendedorModel.getNombre());
            ventas360App.setSerieVendedor(vendedorModel.getSerie());
            ventas360App.setTipoVendedor(vendedorModel.getTipo());
            ventas360App.setIdAlmacen(vendedorModel.getIdAlmacen());
            ventas360App.setModoVenta(vendedorModel.getModoVenta());
            ventas360App.setMarcarPedidosEntregados(false);
        } else {
            usuarioValido = false;
        }
    }

    public void verificarGuias(){
        if (ventas360App.getModoVenta().equals(VendedorModel.MODO_PREVENTA) && !ventas360App.getSettings_preventaEnLinea()){

            ventas360App.setNumeroGuia(VendedorModel.MODO_PREVENTA);

        }else{
            if (ORIGEN == ORIGEN_MENU){
                ArrayList<GuiaModel> listaGuias =  daoConfiguracion.getGuiasOperativas();
                String mensaje = "";
                int icon = 0;

                if (listaGuias.size() == 1){
                    ventas360App.setNumeroGuia(listaGuias.get(0).getNumeroguia());
                }else{
                    if (listaGuias.isEmpty()){
                        if (ventas360App.getSettings_preventaEnLinea())
                            mensaje = "No hay guías de preventa en línea disponibles, comuníquese con el administrador";
                        else
                            mensaje = "No hay guías disponibles, comuníquese con el administrador";

                        icon = R.drawable.ic_dialog_error;
                        ventas360App.setNumeroGuia("");
                    }else{
                        mensaje = "Se encontró mas de una Guia abierta al mismo tiempo, este caso no debe ocurrir y generará problemas.\nComuníquese con el administrador inmediatamente";
                        icon = R.drawable.ic_dialog_alert;
                        ventas360App.setNumeroGuia(listaGuias.get(0).getNumeroguia());//Establecemos la primera guia como la actual
                    }

                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setIcon(icon);
                    builder.setTitle("Importante");
                    builder.setMessage(mensaje);
                    builder.setCancelable(false);
                    builder.setPositiveButton("ACEPTAR", null);
                    builder.show();
                }
            }
        }
    }

    public void verificarUsuario(){
        if (!usuarioValido){
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setIcon(R.drawable.ic_dialog_error);
            builder.setTitle("Usuario no válido");
            builder.setMessage("El usuario con el que ha iniciado sesión no es válido. Por favor, salga de la aplicación y vuelva a iniciar sesión");
            builder.setCancelable(false);
            builder.setPositiveButton("ACEPTAR", null);
            builder.show();
        }
    }


    public void iniciarMigracion()
    {
        DataBaseHelper helper = DataBaseHelper.getInstance(this);
        MigrarProductos1 migrarProductos1 = new MigrarProductos1(helper,this);
        migrarProductos1.moverTabla();
        migrarProductos1.crearTablas();
        //migrarProductos1.downloadInfoTablas(this);
        //migrarProductos1.verificarInformacionGuardada();
    }

    public void iniciarMigracionPpp()
    {
        DataBaseHelper helper = DataBaseHelper.getInstance(this);
        MigrarProductos1 migrarProductos1 = new MigrarProductos1(helper,this);
        migrarProductos1.dropPpp();
    }
}