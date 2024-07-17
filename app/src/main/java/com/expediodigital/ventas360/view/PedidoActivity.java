package com.expediodigital.ventas360.view;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.BatteryManager;
import android.os.Build;
import android.provider.Settings;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;

import com.expediodigital.ventas360.DTO.DTOPedidoDetalle;
import com.expediodigital.ventas360.model.UnidadMedidaModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import androidx.appcompat.widget.Toolbar;

import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import com.expediodigital.ventas360.DAO.DAOBonificacion;
import com.expediodigital.ventas360.DAO.DAOCliente;
import com.expediodigital.ventas360.DAO.DAOConfiguracion;
import com.expediodigital.ventas360.DAO.DAOEncuesta;
import com.expediodigital.ventas360.DAO.DAOGrupoPromocion;
import com.expediodigital.ventas360.DAO.DAOPedido;
import com.expediodigital.ventas360.DAO.DAOProducto;
import com.expediodigital.ventas360.DAO.DAOPromo3F;
import com.expediodigital.ventas360.DAO.DAOPromocion;
import com.expediodigital.ventas360.DTO.DTOPedido;
import com.expediodigital.ventas360.R;
import com.expediodigital.ventas360.Ventas360App;
import com.expediodigital.ventas360.adapter.AutoCompleteClienteAdapter2;
import com.expediodigital.ventas360.model.ClienteModel;
import com.expediodigital.ventas360.model.EncuestaDetalleModel;
import com.expediodigital.ventas360.model.FormaPagoModel;
import com.expediodigital.ventas360.model.MarcaModel;
import com.expediodigital.ventas360.model.PedidoCabeceraModel;
import com.expediodigital.ventas360.model.PedidoDetalleModel;
import com.expediodigital.ventas360.model.PoliticaPrecioModel;
import com.expediodigital.ventas360.model.ProductoModel;
import com.expediodigital.ventas360.model.PromBonificacionModel;
import com.expediodigital.ventas360.model.PromocionDetalleModel;
import com.expediodigital.ventas360.model.RutaXModuloModel;
import com.expediodigital.ventas360.model.VendedorModel;
import com.expediodigital.ventas360.service.GPSTracker;
import com.expediodigital.ventas360.util.GrupoPromocionBon;
import com.expediodigital.ventas360.util.PedidoDetalleProductoModel;
import com.expediodigital.ventas360.util.SoapManager;
import com.expediodigital.ventas360.util.TablesHelper;
import com.expediodigital.ventas360.util.Util;
import com.expediodigital.ventas360.view.fragment.PedidoCabeceraFragment;
import com.expediodigital.ventas360.view.fragment.PedidoDetalleFragment;
import com.google.gson.Gson;
import com.google.gson.JsonParseException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class PedidoActivity extends AppCompatActivity {
    public static final String TAG = "PedidoActivity";
    public static final int ACCION_NUEVO_PEDIDO = 1;
    public static final int ACCION_EDITAR_PEDIDO = 2;
    public static final int ACCION_VER_PEDIDO = 3;

    public static final int ORIGEN_PEDIDOS = 1;
    public static final int ORIGEN_CLIENTES = 2;

    public static final int ACCION_AGREGAR_PRODUCTO = 1;
    public static final int ACCION_MODIFICAR_PRODUCTO = 2;

    public int ACCION_PEDIDO = ACCION_NUEVO_PEDIDO;
    private int REQUEST_CODE_AGREGAR = 1;
    private int REQUEST_CODE_UBICACION = 2;
    private int REQUEST_PREMISOS_UBICACION = 3;
    private int REQUEST_ENCUESTA = 4;
    private int ORIGEN;

    private boolean cabeceraGuardada = false;
    private SectionsPagerAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;
    private PedidoCabeceraFragment pedidoCabeceraFragment;
    private PedidoDetalleFragment pedidoDetalleFragment;
    private String idCliente = "";
    private double porcentajeIGV = 0;
    private double saldoCredito = 0;
    boolean isAfectoPercepcion = false;

    AutoCompleteTextView autocomplete_busqueda;
    private DAOCliente daoCliente;
    private DAOPedido daoPedido;
    private DAOPromocion daoPromocion;
    private DAOBonificacion daoBonificacion;
    private DAOProducto daoProducto;
    private DAOConfiguracion daoConfiguracion;
    private DAOEncuesta daoEncuesta;
    FloatingActionButton fab_agregarProducto;
    String numeroPedido;//solo tiene valor cuando se visualiza o modifica el pedido, para un pedido nuevo que se está realizando, obtener el numero de pedido de "getNumeroPedidoFromFragment()"

    GPSTracker gpsTracker;
    Ventas360App ventas360App;

    //datos de productos que se bonifican pero que al momento de hacer el pedido no hay stock para entregar;
    ArrayList<PedidoDetalleModel> mListaProductosNoBonifica;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pedido);

        ventas360App = (Ventas360App) getApplicationContext();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Util.actualizarToolBar("", true, this, R.drawable.ic_action_close);

        sincronizarBonificaciones();

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            ORIGEN = bundle.getInt("origen");
            ACCION_PEDIDO = bundle.getInt("accion");
            if (ACCION_PEDIDO == ACCION_VER_PEDIDO || ACCION_PEDIDO == ACCION_EDITAR_PEDIDO) {
                idCliente = bundle.getString("idCliente");
                numeroPedido = bundle.getString("numeroPedido");
            }
        }

        autocomplete_busqueda = (AutoCompleteTextView) findViewById(R.id.autocomplete_busqueda);
        autocomplete_busqueda.setHint("Buscar Cliente");

        daoCliente = new DAOCliente(getApplicationContext());
        daoPedido = new DAOPedido(getApplicationContext());
        daoPromocion = new DAOPromocion(getApplicationContext());
        daoProducto = new DAOProducto(getApplicationContext());
        daoConfiguracion = new DAOConfiguracion(getApplicationContext());
        daoEncuesta = new DAOEncuesta(getApplicationContext());
        daoBonificacion = new DAOBonificacion(getApplicationContext());

        pedidoCabeceraFragment = new PedidoCabeceraFragment();
        pedidoDetalleFragment = new PedidoDetalleFragment();

        /*AutoCompleteClienteAdapter2 adapter = new AutoCompleteClienteAdapter2(getApplicationContext(), daoCliente.getClientes());
        autocomplete_busqueda.setThreshold(1);
        autocomplete_busqueda.setAdapter(adapter);*/
        new async_cargarClientes().execute();

        autocomplete_busqueda.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ClienteModel clienteModel = (ClienteModel) parent.getItemAtPosition(position);
                autocomplete_busqueda.setText(clienteModel.getRazonSocial());
                idCliente = clienteModel.getIdCliente();
                saldoCredito = daoCliente.getSaldoCredito(idCliente, getNumeroPedidoFromFragment());
                isAfectoPercepcion = daoCliente.isAfectoPercepcion(idCliente);
                //Una vez se obtenga el codigo del cliente se tiene que mandar ese codigo a los fragment hijos y estos puedan usarlo
                pedidoCabeceraFragment.setIdCliente(idCliente);//Ahora el fragment tiene el codigo del cliente
                pedidoCabeceraFragment.edt_numeroPedido.requestFocus();
            }
        });

        autocomplete_busqueda.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                idCliente = "";
                saldoCredito = 0;
                isAfectoPercepcion = false;
                autocomplete_busqueda.setText("");
                pedidoCabeceraFragment.setIdCliente("");
                return true;
            }
        });

        porcentajeIGV = daoConfiguracion.getPorcentajeIGV();

        /*Una vez cargados los componentes, se debe pasar tanto el codigo del cliente como el numero del pedido
        hacia los fragments que componen toda la vista del pedido, sin embargo los fragments son los que deben obtener
        los campos ya que no sabemos el momento exacto cuando terminan de crearse cada uno*/

        if (ACCION_PEDIDO == ACCION_VER_PEDIDO || ACCION_PEDIDO == ACCION_EDITAR_PEDIDO) {
            pedidoDetalleFragment.setIdCliente(idCliente);
            saldoCredito = daoCliente.getSaldoCredito(idCliente, numeroPedido);
            isAfectoPercepcion = daoCliente.isAfectoPercepcion(idCliente);

            autocomplete_busqueda.setText(bundle.getString("nombreCliente"));
            autocomplete_busqueda.setEnabled(false);
            autocomplete_busqueda.dismissDropDown();

            if (ACCION_PEDIDO == ACCION_VER_PEDIDO) {
                Util.actualizarToolBar("", true, this, R.drawable.ic_action_back);
            }
        } else {
            if (ORIGEN == ORIGEN_CLIENTES) {
                idCliente = bundle.getString("idCliente");
                saldoCredito = daoCliente.getSaldoCredito(idCliente, numeroPedido);
                isAfectoPercepcion = daoCliente.isAfectoPercepcion(idCliente);

                autocomplete_busqueda.setText(bundle.getString("nombreCliente"));
                autocomplete_busqueda.setInputType(InputType.TYPE_NULL);//Indicamos que el AutoCompleteTextView no sea editable
                //Una vez se obtenga el codigo del cliente se tiene que mandar ese codigo a los fragment hijos y estos puedan usarlo
                //pedidoCabeceraFragment.setCodigoCliente(codigoCliente);//Ahora el fragment tiene el codigo del cliente
                autocomplete_busqueda.dismissDropDown();
                autocomplete_busqueda.clearFocus();
            }
        }

        //Una vez se obtenga la ACCION del pedido y los datos necesarios. Crear el adapter que retornará un fragment por cada sección del activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Preparar el ViewPager con las secciones del adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

        int nivelBaterial = getEstadoBateria();
        Log.d(TAG, "NIVEL DE BATERIA:" + nivelBaterial);

        if (ventas360App.getTipoVendedor().equals(VendedorModel.TIPO_PUNTO_VENTA)) {
            Util.cerrarTeclado(PedidoActivity.this, autocomplete_busqueda);
        }
        IniciarLocalizador();
    }

    class async_cargarClientes extends AsyncTask<Void, Void, ArrayList<ClienteModel>> {
        //ProgressDialog pDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            /*pDialog = new ProgressDialog(PedidoActivity.this);
            pDialog.setCancelable(false);
            pDialog.setIndeterminate(true);
            pDialog.setMessage("Cargando...");
            pDialog.show();*/
        }

        @Override
        protected ArrayList<ClienteModel> doInBackground(Void... strings) {
            return daoCliente.getClientes();
        }

        @Override
        protected void onPostExecute(ArrayList<ClienteModel> listaClientes) {
            super.onPostExecute(listaClientes);
            //pDialog.dismiss();
            AutoCompleteClienteAdapter2 adapter = new AutoCompleteClienteAdapter2(getApplicationContext(), listaClientes);
            autocomplete_busqueda.setThreshold(1);
            autocomplete_busqueda.setAdapter(adapter);

            if (ventas360App.getTipoVendedor().equals(VendedorModel.TIPO_PUNTO_VENTA)) {
                Util.cerrarTeclado(PedidoActivity.this, autocomplete_busqueda);
            }
        }
    }


    /**
     * Este método es llamado desde el PedidoCabeceraFragment una vez que se obtiene el numero del pedido, parámetro necesario para obtener el saldo de crédito. En caso sea MODIFICAR O VER PEDIDO ya se tiene el numero de pedido, por lo tanto también se tiene el saldoCredito
     *
     * @param saldoCredito
     */
    public void setSaldoCredito(double saldoCredito) {
        this.saldoCredito = saldoCredito;
    }

    private void IniciarLocalizador() {
        gpsTracker = new GPSTracker(this);

        if (gpsTracker.isGPSEnabled()) {
            if (Build.VERSION.SDK_INT >= 23) {
                if (ContextCompat.checkSelfPermission(PedidoActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                        || ContextCompat.checkSelfPermission(PedidoActivity.this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(PedidoActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_PREMISOS_UBICACION);
                } else
                    gpsTracker.getLocations();
            } else
                gpsTracker.getLocations();
        } else {
            showDialogoUbicacion();
        }
    }

    private void showDialogoUbicacion() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(PedidoActivity.this);
        alertDialog.setTitle("Ubicación");
        alertDialog.setMessage("Es necesario que active la ubicación del teléfono en precisión alta");
        alertDialog.setCancelable(false);

        alertDialog.setPositiveButton("ACEPTAR", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivityForResult(intent, REQUEST_CODE_UBICACION);
            }
        });
        alertDialog.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_activity_pedido, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (ACCION_PEDIDO == ACCION_VER_PEDIDO) {
            menu.findItem(R.id.menu_pedido_guardar).setVisible(false);
        } else {

        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.menu_pedido_guardar:
                enviarPedido(false);
                break;
            case android.R.id.home:
                if (ACCION_PEDIDO == ACCION_VER_PEDIDO) {
                    finish();
                } else {
                    if (cabeceraGuardada) {
                        //Si la cabecera está guardada (se modificó algún campo o el detalle) se muestra la confirmación

                        //Si se está editando y la cabecera se ha guardado, no hay forma que llegue hasta aqui. Ya que no se mostrará el boton "cerrar".
                        //De todas formas se comprueba que no se esté editando para evitar mostrar la confirmación y se vaya sin guardar (necesariamente debe guardar)
                        if (ACCION_PEDIDO != ACCION_EDITAR_PEDIDO) {
                            DialogoConfirmacion();
                        }
                    } else {
                        /*Si la cabecera no está guardada, ya sea cuando se esté registrando o editando,
                          se verifica si se tiene un cliente seleccionado para confirmar, porque puede que
                          de casualidad se haya presionado "atras" o "cerrar"
                         */
                        if (!idCliente.isEmpty()) {
                            DialogoConfirmacion();
                        } else {
                            finish();
                        }
                    }
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void enviarPedido(boolean anularPedido) {
        //Como la cabecera ya está guardada, se actualizará los cambios
        boolean seGuardoCorrectamente = GuardarPedido(anularPedido);

        if (seGuardoCorrectamente) {
            if (daoPedido.verificarPedidoTieneDetalle(getNumeroPedidoFromFragment())) {
                EnviarPedido(getNumeroPedidoFromFragment());
            } else {
                Snackbar.make(findViewById(android.R.id.content), "No puede guardar un pedido sin detalle", Snackbar.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (ACCION_PEDIDO == ACCION_VER_PEDIDO) {
            super.onBackPressed();
        } else {
            if (cabeceraGuardada) {
                //Si la cabecera está guardada (se modificó algún campo o el detalle) se muestra la confirmación

                //Si se está editando y la cabecera se ha guardado, la unica forma de llegar aqui es a través del boton "atras". Ya que no se mostrará el boton "cerrar".
                //Se comprueba que no se esté editando para evitar mostrar la confirmación y se vaya sin guardar (necesariamente debe guardar)
                if (ACCION_PEDIDO != ACCION_EDITAR_PEDIDO) {
                    DialogoConfirmacion();
                }
                //Si se está editando la unica forma de salir debe ser guardando el pedido, "atras" no debe funcionar en esa situación.
            } else {
                /*Si la cabecera no está guardada, ya sea cuando se esté registrando o editando,
                  se verifica si se tiene un cliente seleccionado para confirmar, porque puede que
                  de casualidad se haya presionado "atras" o "cerrar"
                 */
                if (!idCliente.isEmpty()) {
                    DialogoConfirmacion();
                } else {
                    super.onBackPressed();
                }
            }
        }
    }

    private void DialogoConfirmacion() {
        AlertDialog.Builder builder = new AlertDialog.Builder(PedidoActivity.this);
        if (ACCION_PEDIDO == ACCION_NUEVO_PEDIDO) {
            builder.setTitle("Descartar pedido");
            builder.setMessage("Se perderán los datos del pedido");
        } else {
            builder.setTitle("Descartar cambios");
            builder.setMessage("Se perderán los cambios en el pedido");
        }
        builder.setNegativeButton("CANCELAR", null);
        builder.setPositiveButton("DESCARTAR", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if (ACCION_PEDIDO == ACCION_NUEVO_PEDIDO) {
                    daoPedido.eliminarPedido(getNumeroPedidoFromFragment());
                }
                finish();
            }
        });
        builder.show();

    }

    private void EnviarPedido(final String numeroPedido) {
        //validarPercepcion();//Validar Percepciones, cuando el vendedor es TIPO_PUNTO_VENTA, se mantienen las percepciones si es que el monto total del pedido supera los 100 soles, de lo contrario se quita a todos.
        noBonificacionesYPedidoPendiente();
        boolean politicaValida = validarPoliticasPrecio();
        //CalcularBonificaciones();

        if (!isCreditoValido()) {
            showDialogCreditoInsuficiente(saldoCredito, false);
            return;
        }

        if (politicaValida) {
            //Si es que no se valida no se envia el pedido, ya que se enviará dede el metodo validarPoliticasPrecio()
            new AsyncTask<Void, Void, String>() {
                final String ENVIADO = "E";
                final String INCOMPLETO = "I";
                final String PENDIENTE = "P";
                final String TRANSFERIDO = "T";
                final String JSONEXCEPTION = "jsonException";
                final String SIN_CONEXION = "SinConexion";
                final String OTRO_ERROR = "error";

                ProgressDialog pDialog;
                String cadenaResultado = "";

                @Override
                protected void onPreExecute() {
                    super.onPreExecute();
                    pDialog = new ProgressDialog(PedidoActivity.this);
                    pDialog.setCancelable(false);
                    pDialog.setIndeterminate(true);
                    pDialog.setMessage("Enviando pedido...");
                    pDialog.show();
                }

                @Override
                protected String doInBackground(Void... strings) {
                    //evalua bonificaciones
                    ArrayList<PedidoDetalleModel> listaBonificar = calcularBonificaciones3();
                    Log.d(TAG,"esta es la parte de bonificacion");
                    Log.d(TAG,listaBonificar.toArray().toString());
                    Log.d(TAG,"+++++++++++++++++++++++");
                    //GuardarPedido();//EL pedido ya está guardado
                    SoapManager soapManager = new SoapManager(getApplicationContext());
                    Gson gson = new Gson();
                    if (Util.isConnectingToRed(getApplicationContext())) {
                        try {
                            evalStockProductosBonificados(listaBonificar, soapManager);

                            while (gpsTracker.getLatitude() == 0.0 && gpsTracker.getLongitude() == 0.0) {
                                Log.d(TAG, "latitud y longitud 0.0");//Mantener el hilo trabajando hasta que se tome alguna posición
                            }
                            daoPedido.actualizarLatLongPedido(numeroPedido, gpsTracker.getLatitude(), gpsTracker.getLongitude());

                            if (ACCION_PEDIDO == ACCION_NUEVO_PEDIDO) {
                                String horaFin = Util.getHoraTelefonoString();
                                daoPedido.actualizarHoraFinPedido(numeroPedido, horaFin);
                            } else if (ACCION_PEDIDO == ACCION_EDITAR_PEDIDO) {
                                String horaModificacion = Util.getFechaHoraTelefonoString();
                                daoPedido.actualizarFechaModificacionPedido(numeroPedido, horaModificacion);
                            }

                            ArrayList<DTOPedido> pedidoEnviar = daoPedido.getDTOPedidoCompleto(numeroPedido);
                            String cadena = gson.toJson(pedidoEnviar);
                            cadenaResultado = soapManager.enviarPendientes(TablesHelper.ObjPedido.ActualizarObjPedido, cadena);

                            //actualizar kardex
                            daoPedido.actualizarKardex(cadenaResultado);

                            return daoPedido.actualizarFlagPedidos(cadenaResultado);//Retorna el flag resultado
                            //return "I";
                        } catch (JsonParseException ex) {
                            ex.printStackTrace();
                            return JSONEXCEPTION;
                        } catch (Exception e) {
                            e.printStackTrace();
                            return OTRO_ERROR;
                        }
                    } else {
                        return SIN_CONEXION;
                    }
                }

                @Override
                protected void onPostExecute(String respuesta) {
                    super.onPostExecute(respuesta);

                    //lista de productos que no bonifica
                    if (mListaProductosNoBonifica != null && mListaProductosNoBonifica.size() > 0) {
                        if (ventas360App.getTipoVendedor().equals("T")) { //autoventa
                            //como no genera bonificacion, no muestra despues
                        } else {
                            for (PedidoDetalleModel productoB : mListaProductosNoBonifica) {
                                Log.d(TAG,"producto bonificacion");
                                Log.d(TAG,productoB.toString());
                                Log.d(TAG,"++++++++++ fin +++++++");
                                if (productoB.getCantidad() == 0) {
                                    continue;
                                }
                                if (ventas360App.getSettings_preventaEnLinea()) { //preventa en linea
                                    productoB.setSinStock(1);
                                    //no genera bonificacion y mensaje se aplica luego de enviado el pedido
                                    daoPedido.agregarItemPedidoDetalle(productoB);
                                } else { //solo preventa
                                    //como genera bonificacion muestra normalmente
                                    continue;
                                }

                            }
                        }
                    }

                    //Actualizar la lista detalle del pedido resultante
                    pedidoDetalleFragment.mostrarListaProductos();

                    pDialog.dismiss();
                    switch (respuesta) {
                        case ENVIADO:
                            showDialogoPostEnvio("Envío satisfactorio", "El pedido fue ingresado al servidor", R.drawable.ic_dialog_check);
                            break;
                        case INCOMPLETO:
                            showDialogoPostEnvio("Atención", "No se pudieron guardar todos los datos", R.drawable.ic_dialog_alert);
                            break;
                        case PENDIENTE:
                            showDialogoPostEnvio("Atención", "El servidor no pudo ingresar el pedido", R.drawable.ic_dialog_error);
                            break;
                        case TRANSFERIDO:
                            showDialogoPostEnvio("Atención", "El pedido ya se encuentra en proceso de facturación \nComuníquese con el administrador", R.drawable.ic_dialog_block);
                            break;
                        case SIN_CONEXION:
                            showDialogoPostEnvio("Sin conexión", "Es probable que no tenga acceso a INTERNET, El pedido se guardó localmente", R.drawable.ic_dialog_error);
                            break;
                        case JSONEXCEPTION:
                            showDialogoPostEnvio("Atención", "El pedido fue enviado pero no se pudo verificar\nConsulte con el administrador", R.drawable.ic_dialog_alert);
                            break;
                        case OTRO_ERROR:
                            showDialogoPostEnvio("Error", "No se pudo enviar el pedido, se guardó localmente", R.drawable.ic_dialog_error);
                            break;
                        default:
                            if (ventas360App.getModoVenta().equals(VendedorModel.MODO_PREVENTA) && !ventas360App.getSettings_preventaEnLinea()) {
                                showDialogoPostEnvio("Error", "" + respuesta, R.drawable.ic_dialog_error);
                            } else {
                                final ArrayList<PedidoDetalleModel> listaProductosSinStock = getListaDetalleFromJSON(cadenaResultado);
                                for (PedidoDetalleModel productoSinStock : listaProductosSinStock) {

                                }
                                //actualizar en la base de datos indicando que no se comprara la bonificacion
                                //en cantidad se pone cero y al momento de mostrarlo dira que no hay stock

                                //en el web service poner a cero o quitar el producto si es unicamente bonificacion

                                //Si es Autoventa no dejar salir del pedido hasta enviar correctamente o descartar
                                AlertDialog.Builder builder = new AlertDialog.Builder(PedidoActivity.this);
                                builder.setTitle("Atención");
                                builder.setMessage("" + respuesta);
                                builder.setIcon(R.drawable.ic_dialog_error);
                                builder.setCancelable(false);
                                builder.setPositiveButton("ACEPTAR", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        for (PedidoDetalleModel productoSinStock : listaProductosSinStock) {
                                            daoPedido.actualizarFlagSinStock(numeroPedido, productoSinStock.getIdProducto(), 1);
                                            pedidoDetalleFragment.mostrarListaProductos();
                                        }
                                    }
                                });
                                builder.show();
                            }
                            break;
                    }

                }
            }.execute();
        }
    }

    private void tomarEncuesta(EncuestaDetalleModel encuestaDetalleModel) {

        if (encuestaDetalleModel != null) {
            Intent intent = new Intent(this, EncuestaClienteDialogActivity.class);

            intent.putExtra("idCliente", idCliente);
            intent.putExtra("razonSocial", autocomplete_busqueda.getText().toString());
            intent.putExtra("clientesObligatorios", encuestaDetalleModel.getClientesObligatorios() == 1); //Determina si se puede cerrar u obviar la encuesta
            intent.putExtra("descripcionEncuesta", encuestaDetalleModel.getDescripcionEncuesta());
            intent.putExtra("idTipoEncuesta", encuestaDetalleModel.getIdTipoEncuesta());
            intent.putExtra("tipoEncuesta", encuestaDetalleModel.getTipoEncuesta());
            intent.putExtra("idEncuesta", encuestaDetalleModel.getIdEncuesta());
            intent.putExtra("idEncuestaDetalle", encuestaDetalleModel.getIdEncuestaDetalle());

            startActivityForResult(intent, REQUEST_ENCUESTA);
        } else {
            Toast.makeText(this, "No se encontró alguna encuesta", Toast.LENGTH_SHORT).show();
        }
    }

    private ArrayList<PedidoDetalleModel> getListaDetalleFromJSON(String json) {
        ArrayList<PedidoDetalleModel> lista = new ArrayList<>();
        //Gson gson = new Gson();
        //Type listType = new TypeToken<ArrayList<PedidoDetalleModel>>() {}.getType();
        //lista = gson.fromJson(json,listType);
        try {
            JSONArray jsonArray = new JSONArray(json);

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonData = jsonArray.getJSONObject(i);

                JSONArray jsonArrayDetalle = jsonData.getJSONArray("detalles");
                for (int j = 0; j < jsonArrayDetalle.length(); j++) {
                    JSONObject jsonDataDetalle = jsonArrayDetalle.getJSONObject(j);
                    PedidoDetalleModel producto = new PedidoDetalleModel();
                    producto.setIdProducto(jsonDataDetalle.getString(TablesHelper.PedidoDetalle.PKeyProducto));
                    producto.setCantidad(jsonDataDetalle.getInt(TablesHelper.PedidoDetalle.Cantidad));
                    lista.add(producto);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.w(TAG, "getListaDetalleFromJSON: " + new Gson().toJson(lista));
        Log.w(TAG, "HAY " + lista.size() + " PRODUCTOS SIN STOCK");
        return lista;
    }


    private void showDialogoPostEnvio(String titulo, String mensaje, @DrawableRes int icon) {
        AlertDialog.Builder builder = new AlertDialog.Builder(PedidoActivity.this);
        builder.setTitle(titulo);
        builder.setMessage(mensaje);
        builder.setIcon(icon);
        builder.setCancelable(false);
        builder.setPositiveButton("ACEPTAR", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {


                if (ventas360App.getTipoVendedor().equals(VendedorModel.TIPO_MERCADEO)) {
                    EncuestaDetalleModel encuestaDetalleModel = daoEncuesta.getEncuestaDetalle(TablesHelper.EncuestaTipo.TIPO_TRADE);
                    if (encuestaDetalleModel != null) {
                        tomarEncuesta(encuestaDetalleModel);
                    } else {
                        Intent intent = new Intent(getApplicationContext(), DetallePedidoActivity.class);
                        intent.putExtra("numeroPedido", getNumeroPedidoFromFragment());
                        intent.putExtra("idCliente", idCliente);
                        intent.putExtra("nombreCliente", autocomplete_busqueda.getText().toString());
                        startActivity(intent);
                        setResult(RESULT_OK);
                        finish();
                    }
                } else {
                    EncuestaDetalleModel encuestaDetalleModel = daoEncuesta.getEncuestaPostPedido();
                    if (encuestaDetalleModel != null) {
                        tomarEncuesta(encuestaDetalleModel);
                    } else {
                        Intent intent = new Intent(getApplicationContext(), DetallePedidoActivity.class);
                        intent.putExtra("numeroPedido", getNumeroPedidoFromFragment());
                        intent.putExtra("idCliente", idCliente);
                        intent.putExtra("nombreCliente", autocomplete_busqueda.getText().toString());
                        startActivity(intent);
                        setResult(RESULT_OK);
                        finish();
                    }
                }

            }
        });
        builder.show();
    }

    private boolean isCreditoValido() {
        if (pedidoCabeceraFragment.getPedido().getIdFormaPago().equals(FormaPagoModel.ID_FORMA_PAGO_CREDITO)) {
            double resto = saldoCredito - pedidoDetalleFragment.getMontoTotal();
            return (resto >= 0);
        } else
            return true;
    }

    private void validarPercepcion() {
        /*FUNCIONALIDAD POR CONFIRMAR
        if (ventas360App.getModoVenta().equals(VendedorModel.TIPO_PUNTO_VENTA)){
            double limiteParaPercepcion = daoConfiguracion.getLimitePercepcion();
            double importePedido = pedidoDetalleFragment.getMontoTotal();

            if (importePedido >= limiteParaPercepcion){
                //Se mantiene la percepcion de los productos
            }else{
                //Si no se alcanza el limite, se debe quitar la percepcion de los productos
                ArrayList<PedidoDetalleModel> listaProductos = daoPedido.getListaProductoPedido(getNumeroPedidoFromFragment());//se obtiene la lista de productos del pedido
                for (PedidoDetalleModel producto: listaProductos) {
                    double nuevoPrecioBruto = daoProducto.getPrecioProducto(producto.getIdProducto(),politicaPrecioPorDefecto.getIdPoliticaPrecio(),producto.getIdUnidadMedida());//Validar cuando retorne cero
                    double nuevoPrecioNeto = nuevoPrecioBruto * producto.getCantidad();
                    Log.e(TAG,"nuevoPrecioBruto: "+nuevoPrecioBruto);
                    Log.e(TAG,"nuevoPrecioNeto: "+nuevoPrecioNeto);
                    daoPedido.actualizarPoliticaPreciosProductoPedido(getNumeroPedidoFromFragment(),producto.getIdProducto(),producto.getTipoProducto(),politicaPrecioPorDefecto.getIdPoliticaPrecio(),nuevoPrecioBruto,nuevoPrecioNeto);

                    if (nuevoPrecioBruto == 0){
                        existenPrecioCero = true;//Guarda el flag para indicar luego que hay productos sin precio
                    }
                }
            }
        }*/
    }

    private boolean validarPoliticasPrecio() {
        boolean politicaValida = true;

        boolean existenPrecioCero = false;
        int cantidadPaquetesPedido = daoPedido.getCantidadPaquetes(getNumeroPedidoFromFragment());
        ArrayList<PedidoDetalleModel> listaProductos = daoPedido.getListaProductoPedido(getNumeroPedidoFromFragment());//se obtiene la lista de productos del pedido
        PoliticaPrecioModel politicaPrecioCliente = daoCliente.getPoliticaPrecio(idCliente);

        if (cantidadPaquetesPedido == 0 && listaProductos.size() > 0) {
            //Existen productos pero ninguno es TIPO_VENTA, en este caso el pedido es válido pero no se debe validar Politicas de precio.
            politicaValida = true;
        } else {
            Log.v(TAG, "politicaPrecioCliente.getCantidadMinima(): " + politicaPrecioCliente.getCantidadMinima());
            /*Si la cantidad de paquetes no alcanza la cantidad minima para mantener la politica del cliente, se tiene que cambiar todos los precios de los productos*/
            if (cantidadPaquetesPedido < politicaPrecioCliente.getCantidadMinima()) {

                PoliticaPrecioModel politicaPrecioPorDefecto = daoProducto.getPoliticaPrecioPorDefecto();// se obtiene la politica de precio por defecto (Sucursal)

                if (politicaPrecioPorDefecto != null) {
                    for (PedidoDetalleModel producto : listaProductos) {
                        if (producto.getTipoProducto().equals(ProductoModel.TIPO_VENTA)) {//Solo para los productos TIPO_VENTA se debe cambiar la politica de precio
                            double nuevoPrecioBruto = daoProducto.getPrecioProducto(producto.getIdProducto(), politicaPrecioPorDefecto.getIdPoliticaPrecio(), producto.getIdUnidadMedida());//Validar cuando retorne cero
                            double nuevoPrecioNeto = nuevoPrecioBruto * producto.getCantidad();
                            Log.e(TAG, "nuevoPrecioBruto: " + nuevoPrecioBruto);
                            Log.e(TAG, "nuevoPrecioNeto: " + nuevoPrecioNeto);
                            daoPedido.actualizarPoliticaPreciosProductoPedido(getNumeroPedidoFromFragment(), producto.getIdProducto(), producto.getTipoProducto(), politicaPrecioPorDefecto.getIdPoliticaPrecio(), nuevoPrecioBruto, nuevoPrecioNeto);

                            if (nuevoPrecioBruto == 0) {
                                existenPrecioCero = true;//Guarda el flag para indicar luego que hay productos sin precio
                            }
                        }
                    }
                    pedidoDetalleFragment.mostrarListaProductos();//Se actualiza la vista IU de detalle del pedido y se actualiza los totales en la cabecera del pedido
                    showDialogCambioPolitica(politicaPrecioCliente.getDescripcion(), politicaPrecioPorDefecto.getDescripcion(), cantidadPaquetesPedido, politicaPrecioCliente.getCantidadMinima(), existenPrecioCero, true);

                } else {
                    showDialogCambioPolitica(politicaPrecioCliente.getDescripcion(), "base", cantidadPaquetesPedido, politicaPrecioCliente.getCantidadMinima(), existenPrecioCero, false);
                }
                politicaValida = false;
            } else {
                //la cantidadPaquetesPedido si alcanzó la cantidad minima para mantener la politica del cliente, por lo tanto es válido
                politicaValida = true;
            }
        }
        return politicaValida;
    }

    private void CalcularBonificaciones() {
        Gson gson = new Gson();
        String idVendedor = ventas360App.getIdVendedor();
        String numeroPedido = getNumeroPedidoFromFragment(); //Obtenecemos el numeroPedido
        ArrayList<PedidoDetalleModel> pedidoDetalle = daoPedido.getListaProductoPedido(numeroPedido);//Obtenemos todo el detalle del pedido

        ArrayList<HashMap<String, Object>> listaFinalCantidadesBonificadas = new ArrayList<>();
        ArrayList<PromocionDetalleModel> listaFinalPromocionesGeneradas = new ArrayList<>();
        for (PedidoDetalleModel pedidoDetalleOriginal : pedidoDetalle) {
            /*Esta lista guardará todas las bonificaciones obtenidas por el producto entrada, para al final tomar seleccionar las idóneos, dándole prioridad al que sea PorCliente*/
            ArrayList<HashMap<String, Object>> listaBonificacionesDelProducto = new ArrayList<>();
            ArrayList<PromocionDetalleModel> listaPromocionesGeneradasDelProducto = new ArrayList<>();

            /*Aqui si se obtiene mas de una promocion para el producto, tal vez con un distinct de entrada en PromocionDetalle, se puede quitar aquí o sería mejor quitar cuando se genere la bonificacion ya que a lo mejor uno no cumpla su condición y no necesite quitarlo*/
            ArrayList<PromocionDetalleModel> promocionesProducto = daoPromocion.getPromocionesProducto(pedidoDetalleOriginal, idCliente, idVendedor, getNumeroPedidoFromFragment());
            for (PromocionDetalleModel itemPromocion : promocionesProducto) {
                int cantidadBonificada = 0;
                //Si se altera algo en el pedidoDetalleOriginal, es mejor crear aqui un nuevo objeto con sus datos
                Log.e(TAG, "itemPromocion.getAcumulado() ==> " + itemPromocion.getAcumulado());
                /* ----------------- ACUMULADOS -------------------*/
                if (itemPromocion.getAcumulado() == PromocionDetalleModel.TIPO_ACUMULADO_PURO_COMPUESTO) {
                    if (daoPromocion.isPromocionAcumuladoPuro(itemPromocion)) {
                        cantidadBonificada = getCantidadBonificada_AcumuladoPuro(itemPromocion, pedidoDetalleOriginal, numeroPedido);
                    } else {
                        //Agregar logica para promociones acumulados Compuestos (AGRUPADO + ACUMULADOS)
                        //cantidadBonificada = getCantidadBonificada_ obtenerCantidadBonificacionAcumulado_Combinado(itemPromocion, pedidoDetalleOriginal, numeroPedido);
                    }
                } else if (itemPromocion.getAcumulado() == PromocionDetalleModel.TIPO_ACUMULADO_MULTIPLE) {
                    cantidadBonificada = getCantidadBonificada_AcumuladoMultiple(itemPromocion, pedidoDetalleOriginal, numeroPedido);
                } else {
                    /* ------------- AGRUPADOS ----------------- */
                    /* 1.- Verificar si estan agrupados "Si tienen el mismo codigo agrupado" & ,"Si tienen codigos distintos se  corta la agrupación" ó
                     * 2.- Verificar tipoPromocion [C,M] ->Cantidad,Monto
                     * 3.- Verificar por condición [1,3] -> mayor o igual, por cada	 */
                    cantidadBonificada = getCantidadBonificada_Agrupados(itemPromocion, pedidoDetalleOriginal, numeroPedido);
                }

                if (cantidadBonificada > 0) {
                    Log.i(TAG, "cantidadBonificada > 0 " + cantidadBonificada + " de la promocion " + itemPromocion.getIdPromocion() + " agregando a listaPromocionesGeneradasDelProducto");
                    /*HAY UN BUG, AL VALIDAR UNA PROMOCION  ACUMULADO, PASA QUE SE REQUIERE NECESARIAMENTE UNA TABLA AUXILIAR DONDE REGITRAR LAS CANTIDADES QUE YA SE USARON,
                     * SINO EN EL SIGUIENTE ITEM DEL DETALLE SE TOMARÁ LAS CANTIDADES ANTERIORES Y SE SUMARÁ A LA NUEVA, REPIDIENDOSE. ESTO ESTA MAL.
                     * ADEMAS NO SE TOMARÁ LOS PRODUCTOS RESTANTES DE LA PROMOCION (Esto no deberia ser un problema ya que se indicó que un producto solo tendrá una promocion y no varias)
                     * Y SE PUEDE SOLUCIONAR CON DICHA TABLA
                     * OTRA OPCIÓN ES CADA QUE INICIE UN NUEVO itemPromocion VERIFICAR SI ES UNA PROMOCION ACUMULADA, SI ES ASI
                     * SE BUSCA LA PROMOCION Y SU CANTIDAD GENERADA PARA QUITARLO DE LA LISTA DE PROMOCIONES QUE YA SE DIERON
                     * Y LUEGO SE ELIMINA, YA QUE SE VOLVERÁ A EVALUAR ESA PROMOCINO Y TOMARÁ LAS CANTIDADES ANTERIORES JUNTO A LA NUEVA
                     *
                     * Y LA SOLUCION POR LA QUE SE OPTÓ ES ELIMINAR LAS PROMOCIONES REPETIDAS DE LA MISMA idPromocion,itemProducto,cantidad,idProducto
                     * ASI NO IMPORTA SI SE SIGUE TOMANDO LAS CANTIDADES YA USADAS PARA ACUMULAR, AL FINAL SE TOMA LA CANTIDAD MAYOR Y ESA ES LA QUE SE BONIFICA :)*/

                    /* Quitar las promociones generadas anteriormente con el mismo idPromocion y que sean acumulados*/
                    listaPromocionesGeneradasDelProducto.add(itemPromocion);
                    HashMap<String, Object> map = new HashMap<>();
                    map.put("idPromocion", itemPromocion.getIdPromocion());
                    map.put("itemPromocion", itemPromocion.getItem());
                    map.put("cantidad", cantidadBonificada);
                    map.put("idProducto", itemPromocion.getSalida());
                    map.put("itemPedido", pedidoDetalleOriginal.getItem());//este campo no debería ser tomando en cuenta por el HashSet(eliminar repetidos) ya que con este campo todos los valores se vuelven diferencites, cuando en realidad no lo es. Sin embargo no se puede quitar este campo porque es importante para calcular despues, es por eso que se eliminan los repetidos con un for comparando solo los campos que se requieren
                    listaBonificacionesDelProducto.add(map);
                }
            }

            /*Validamos si las bonificaciones generados por el producto son mas de uno, a fin de darle prioridad a las que son desgnadas al cliente.*/

            if (!listaBonificacionesDelProducto.isEmpty()) {
                if (listaBonificacionesDelProducto.size() > 1) {
                    Log.i(TAG, "listaBonificacionesDelProducto hay mas de una bonificacion generada por el producto " + pedidoDetalleOriginal.getIdProducto());
                    ArrayList<HashMap<String, Object>> tempListaCantidadesBonificadas = new ArrayList<>();//Lista temporal para poder analizar
                    ArrayList<PromocionDetalleModel> tempListaPromocionesGeneradas = new ArrayList<>();//Lista temporal para poder analizar

                    for (int i = 0; i < listaPromocionesGeneradasDelProducto.size(); i++) {
                        if (listaPromocionesGeneradasDelProducto.get(i).getPorCliente() == 1) {
                            tempListaCantidadesBonificadas.add(listaBonificacionesDelProducto.get(i));
                            tempListaPromocionesGeneradas.add(listaPromocionesGeneradasDelProducto.get(i));
                            Log.i(TAG, "agregando " + gson.toJson(listaBonificacionesDelProducto.get(i)) + " como promocion PorCliente");
                        }
                    }

                    //Verificar si la lista de listaCantidadesBonificadas está vacía(si no hay promociones PorCliente), basta que exista una promocion PorCliente, ya no se tomarán en cuenta las bonificaciones generadas de forma general
                    if (tempListaCantidadesBonificadas.isEmpty()) {//Si la lista temporal está vacía, entonces significa que no hay ProCliente y se toma todas las bonificaciones generadas
                        Log.i(TAG, "No hay promociones filtrados PorCliente=1, agregando todas las bonificaciones generadas...");
                        tempListaCantidadesBonificadas.addAll(listaBonificacionesDelProducto);
                        tempListaPromocionesGeneradas.addAll(listaPromocionesGeneradasDelProducto);
                    }

                    //Luego de obtener la lista temporal de las cantidades bonificadas ya sea PorCliente o todas las generales, se agrega la lista temporal a la lista final
                    listaFinalCantidadesBonificadas.addAll(tempListaCantidadesBonificadas);
                    listaFinalPromocionesGeneradas.addAll(tempListaPromocionesGeneradas);
                } else {
                    //Si la lista solo tiene un elemento, no hay nada que analizar, asi que lo tomamos
                    listaFinalCantidadesBonificadas.addAll(listaBonificacionesDelProducto);
                    listaFinalPromocionesGeneradas.addAll(listaPromocionesGeneradasDelProducto);
                }
            }
        }

        Log.e(TAG, "---------------------------------");
        Log.d(TAG, "lista PromocionesGeneradas:\n" + gson.toJson(listaFinalPromocionesGeneradas));
        Log.e(TAG, "---------------------------------");
        Log.d(TAG, "lista CantidadesBonificadas Inicial:\n" + gson.toJson(listaFinalCantidadesBonificadas));
        Log.e(TAG, "---------------------------------");
        /*Log.d(TAG, "lista PromocionCompuesta:\n"	+ gson.toJson(listaPromocionCompuesta));
        Log.e("", "---------------------------------");
        Log.d(TAG, "lista CantidadesUsadas:\n"	+ gson.toJson(listaCantidadesUsadas));
        Log.e("", "---------------------------------");
        Log.d(TAG, "lista MontosUsados:\n"	+ gson.toJson(listaMontosUsados));
        Log.e("", "---------------------------------");*/
        //
        /*Elminar repetidos con HashSet (todos los atributos del objeto son tomados en cuenta, es decir que todos los atributos del objeto deben ser necesariamente diferentes)*/
        /*HashSet hashSet = new HashSet();//Creamos un objeto HashSet
        hashSet.addAll(listaCantidadesBonificadas);//Lo cargamos con los valores de un array, esto hace que se quiten los repetidos
        listaCantidadesBonificadas.clear();//Limpiamos la lista
        listaCantidadesBonificadas.addAll(hashSet);//Agregamos los elementos sin repetir
        Log.d(TAG, "lista CantidadesBonificadas Sin Repetir:\n"		+ gson.toJson(listaCantidadesBonificadas));*/

        /*Elminar repetidos con For, no se puede usar el HashSet porque no queremos tomar en cuenta el atributo itemPedido al momento de eliminar repetidos*/
        ArrayList<HashMap<String, Object>> newListaCantidadesBonificadas = new ArrayList<>();
        for (HashMap<String, Object> item : listaFinalCantidadesBonificadas) {
            boolean isInList = false;
            for (HashMap<String, Object> itemUnico : newListaCantidadesBonificadas) {
                if ((int) item.get("idPromocion") == (int) itemUnico.get("idPromocion") &&
                        (int) item.get("itemPromocion") == (int) itemUnico.get("itemPromocion") &&
                        (int) item.get("cantidad") == (int) itemUnico.get("cantidad") &&
                        item.get("idProducto").toString().equals(itemUnico.get("idProducto").toString())) {
                    //Si el item es totalmente igual(por los campos igualados) a alguno de los items dentro de este for, entonces se cambia el flag
                    isInList = true;
                }
            }
            if (!isInList)
                newListaCantidadesBonificadas.add(item);
        }
        listaFinalCantidadesBonificadas.clear();
        listaFinalCantidadesBonificadas.clear();//Limpiamos la lista
        listaFinalCantidadesBonificadas.addAll(newListaCantidadesBonificadas);//Agregamos los elementos sin repetir
        Log.d(TAG, "lista CantidadesBonificadas Sin Repetir:\n" + gson.toJson(listaFinalCantidadesBonificadas));


        /*Con la lista sin repetidos, recien se puede validar cada uno de los elementos
         La lista podría contener varias promociones con el mismo ID y con cantidades bonificadas distintas,
         esto solo pasa con los acumulados ya que se puede cumplir su condicion varias veces.
         Para solucionar esto analizamos todos los las bonificaciones a fin de determinar solo el mayor del cantidad
         bonificada por cada promocion, es decir que si hay varias cantidades por acumulado, solo se tomará y será agregado
         a la lista de productos a bonificar en el pedido finalmente
        */

        ArrayList<PedidoDetalleModel> listaProductosBonificados = new ArrayList<>();
        String productosNulos = "";

        //Primer for es sólo para actualizar las cantidades luego de ser multiplicadas por la compra (en caso se requiera)
        for (int i = 0; i < listaFinalCantidadesBonificadas.size(); i++) {
            int idPromocionx = (int) listaFinalCantidadesBonificadas.get(i).get("idPromocion");
            String itemPromocionx = (String) listaFinalCantidadesBonificadas.get(i).get("itemPromocion");
            int cantidadx = (int) listaFinalCantidadesBonificadas.get(i).get("cantidad");
            int itemPedidox = (int) listaFinalCantidadesBonificadas.get(i).get("itemPedido");
            String idProductox = listaFinalCantidadesBonificadas.get(i).get("idProducto").toString();
            boolean esElMayor = true;

            //Antes de agregar la cantidad al pedido, verificar si la promociones debe ser multiplicado por la cantidad de compra total del pedido (UnidadMayor)
            //Esto se debe hacer antes de obtener el mayor también, ya que aquí es donde habrá diferencias entre las promociones escalables, al final el mayor de ellos deberá registrarse en el pedido
            if (daoPromocion.isPromocionMultiplicadoPorCompra(idPromocionx, itemPromocionx)) {
                int cantidadProductosPromo = daoPedido.getCantidadProductosDePromocion(numeroPedido, idPromocionx, itemPromocionx, itemPedidox);
                Log.w(TAG, "multiplicando " + cantidadx + " x " + cantidadProductosPromo + " productos en el pedido...");
                cantidadx = cantidadx * cantidadProductosPromo;
                listaFinalCantidadesBonificadas.get(i).put("cantidad", cantidadx);
            }
        }
        Log.d(TAG, "lista CantidadesBonificadas Sin Repetir luego de update cantidadx:\n" + gson.toJson(listaFinalCantidadesBonificadas));

        //Segundo for ya es para obtener la bonificación mayor (con las cantidades ya multiplicadas), para este momento no deben existir bonificaciones repetidas(mismas cantidades, esto pasa con acumulados), de lo contrario no se bonificará nada
        for (int i = 0; i < listaFinalCantidadesBonificadas.size(); i++) {
            int idPromocionx = (int) listaFinalCantidadesBonificadas.get(i).get("idPromocion");
            int itemPromocionx = (int) listaFinalCantidadesBonificadas.get(i).get("itemPromocion");
            int cantidadx = (int) listaFinalCantidadesBonificadas.get(i).get("cantidad");
            int itemPedidox = (int) listaFinalCantidadesBonificadas.get(i).get("itemPedido");
            String idProductox = listaFinalCantidadesBonificadas.get(i).get("idProducto").toString();
            boolean esElMayor = true;

            /*Por promocion solo debe haber una condicion resultante, asi hayan muchos items dentro de la promoción, los items deben ser usados para promocinoes escalables,
            donde al final se obtenga solo uno o tambien sirve para hacer multilpes bonificaciones.
            La comprobación de la bonificación mayor se hace sin tomar en cuenta el item, para asi descargar los items con bonificaciones menores, siempre y cuando se trate del mismo producto bonificado
            Si los productos que se van a boinficar son distintos, entonces no es una promocion escalable, sino una promocion con bonificacion multiple*/
            for (int j = 0; j < listaFinalCantidadesBonificadas.size(); j++) {
                if (j != i) {
                    /*todo Si son de la misma promocion, y con la misma salida quiere decir que son escalables y se debe elegir el mayor.*/
                    if (idPromocionx == (int) listaFinalCantidadesBonificadas.get(j).get("idPromocion") && idProductox.equals(listaFinalCantidadesBonificadas.get(j).get("idProducto").toString())) {
                        Log.w(TAG, "if " + cantidadx + " > " + (int) listaFinalCantidadesBonificadas.get(j).get("cantidad"));
                        if (cantidadx > (int) listaFinalCantidadesBonificadas.get(j).get("cantidad")) {
                            Log.w(TAG, "true");
                            esElMayor = true;
                        } else {
                            Log.w(TAG, "false");
                            esElMayor = false;
                            //Basta que no sea mayor que alguno para descartarlo como el Mayor
                            break;//El break rompe el for
                        }
                    }
                    /*todo Si no son de la misma salida, quiere decir que son varias bonificaciones, será tomado como mayor y será agregado a lista de productos bonificados.*/
                }
            }

            if (esElMayor) {

                Log.e(TAG, "El producto " + idProductox + " es el mayor en la posicion " + i + " con cantidad " + cantidadx);
                ProductoModel productoModel = daoProducto.getProducto(idProductox);
                if (productoModel != null) {
                    PedidoDetalleModel pedidoDetalleBonificacion = new PedidoDetalleModel();
                    pedidoDetalleBonificacion.setNumeroPedido(numeroPedido);
                    pedidoDetalleBonificacion.setIdProducto(idProductox);
                    pedidoDetalleBonificacion.setPrecioBruto(0.0);
                    pedidoDetalleBonificacion.setPrecioNeto(0.0);
                    pedidoDetalleBonificacion.setCantidad(cantidadx);
                    pedidoDetalleBonificacion.setPesoNeto(productoModel.getPeso() * cantidadx);
                    //pedidoDetalleBonificacion.setIdUnidadMedida(productoModel.getIdUnidadMenor());
                    pedidoDetalleBonificacion.setIdPoliticaPrecio("0");
                    pedidoDetalleBonificacion.setPercepcion(0.0);
                    pedidoDetalleBonificacion.setISC(0.0);
                    pedidoDetalleBonificacion.setTipoProducto(ProductoModel.TIPO_BONIFICACION);//Producto tipo [V]venta [B]bonificacion
                    listaProductosBonificados.add(pedidoDetalleBonificacion);
                } else {
                    //Se concatenan los codigos de los productos bonificados que no se encuentren. Para mostrar un mensaje después
                    productosNulos += idProductox + ",";
                }
            }

        }

        //Si la cadena no está vacía quiere decir quer si hubo productos nulos y se debe mostrar el mensaje
        if (!productosNulos.isEmpty()) {
            productosNulos = productosNulos.substring(0, productosNulos.length() - 1);//Quitamos el ultimo coma ","
            String mensaje = "No se encontraron los productos (" + productosNulos + ") para bonificar";
            Toast.makeText(getApplicationContext(), mensaje, Toast.LENGTH_LONG).show();
        }

        //Registramos todos los productos bonificados al pedido
        for (PedidoDetalleModel productoB : listaProductosBonificados) {
            int cantidadRegistrada = productoEstaRegistrado(productoB.getIdProducto(), productoB.getTipoProducto(), productoB.getMalla(), productoB.getIdUnidadMedida());
            if (cantidadRegistrada != -1) {
                //Si retorna distinto a -1 quiere decir que si está registrado, se debe tomar su cantidad y sumarle a la nueva cantidad
                productoB.setCantidad(productoB.getCantidad() + cantidadRegistrada);
                daoPedido.modificarItemDetallePedido(productoB);
            } else {
                daoPedido.agregarItemPedidoDetalle(productoB);
            }
        }

        //Actualizar la lista detalle del pedido
        pedidoDetalleFragment.mostrarListaProductos();
    }

    private int getCantidadBonificada_Agrupados(PromocionDetalleModel itemPromocion, PedidoDetalleModel itemDetalle, String numeroPedido) {
        Log.v(TAG, "getCantidadBonificada_Agrupados pedido ->" + numeroPedido);
        /* 1.- Verificar campo agrupado
         * -Si tienen el mismo codigo"-> &
         * -Si tienen codigos distintos"-> ó
         */
        ArrayList<PromocionDetalleModel> listaAgrupados = daoPromocion.getListaAgrupados(itemPromocion.getIdPromocion(), itemPromocion.getItem(), itemPromocion.getAgrupado());
        /* 2.- Verificar tipo [C,M] = Cantidad,Monto
         * 3.- Verificar por condición [1,2,3] = mayor o igual, menor o igual, por cada*/
        int cantidadBonificacion = 0;
        if (listaAgrupados.size() > 1) {
            /*------------------------------------- PRODUCTOS AGRUPADOS & ---------------------------------------*/
            Log.d(TAG, "Es de tipo &");
            Log.v(TAG, "getCantidadBonificada_Agrupados:▬▬▬▬▬▬▬▬ Analisis del detalle ▬▬▬▬▬▬▬");
            ArrayList<PedidoDetalleModel> listaDetallePedido = daoPedido.getListaProductoPedidoEvaluar(numeroPedido, itemDetalle.getItem());
            int contador = 0;

            /* Array de las cantidades obtenidas por los item agrupado individualmente */
            ArrayList<Integer> cantidadesIndividual = new ArrayList<Integer>();

            /* Verificar si cada registro con el mismo agrupado cumple con su condicion */
            for (int i = 0; i < listaAgrupados.size(); i++) {
                PromocionDetalleModel auxItemPromo = listaAgrupados.get(i);
                Log.v(TAG, "listaAgrupados(" + i + ")  " + auxItemPromo.getEntrada());

                for (PedidoDetalleModel auxItemDetalle : listaDetallePedido) {
                    /* Verifica si el item promocion esta en el detalle y si cumple con la condicion*/
                    if (auxItemPromo.getEntrada().equals(auxItemDetalle.getIdProducto())) {
                        int cantidadEvaluar = 0;
                        if (itemPromocion.getEvaluarEnUnidadMayor() == 1) {//Se acumula con cantidad mayor
                            cantidadEvaluar = getCantidadUnidadMayor(auxItemDetalle.getIdProducto(), auxItemDetalle.getIdUnidadMedida(), auxItemDetalle.getCantidad());
                        } else {//Se acumula con cantidad menor
                            cantidadEvaluar = getCantidadUnidadMenor(auxItemDetalle.getIdProducto(), auxItemDetalle.getIdUnidadMedida(), auxItemDetalle.getCantidad());
                        }

                        int auxCantidadBonif = verificarTipoCondicion(auxItemPromo, cantidadEvaluar, auxItemDetalle.getPrecioNeto());
                        Log.e(TAG, "getCantidadBonificada_Agrupados: cantidad obtenido por item " + auxItemDetalle.getIdProducto() + " -> " + auxCantidadBonif);
                        cantidadesIndividual.add(auxCantidadBonif);

                        if (auxCantidadBonif > 0) {
                            contador++;
                        }
                    }
                }
            }

            /* Si todos las promociones cumplieron su condicion, se procede a obtener la bonificacion*/
            if (listaAgrupados.size() == contador) {
                /* Si todas las cantidades obtenidas son iguales elegir cualquiera
                 * Si no son iguales escoger la cantidad minima */
                int cantidadBonifMenor = cantidadesIndividual.get(0);
                int cantidadBonifTemp = cantidadesIndividual.get(0);

                for (int x = 0; x < cantidadesIndividual.size(); x++) {
                    if (cantidadBonifTemp != cantidadesIndividual.get(x)) {
                        /*Si no son iguales*/
                        if (cantidadesIndividual.get(x) < cantidadBonifMenor) {
                            /*Si la cantidad es la menor hasta ahora se guarda como tal*/
                            cantidadBonifMenor = cantidadesIndividual.get(x);
                            //Es la cantidad menor bonificado mas no utilizado
                        }
                    }
                }
                cantidadBonificacion = cantidadBonifMenor;
                Log.i(TAG, "Todos las promociones cumplieron con su condicion, cantidad bonificacion -> " + cantidadBonificacion);
            } else {
                Log.e(TAG, "No se cumplieron todas las condiciones listaAgrupados: " + listaAgrupados.size() + "  contador: " + contador);
            }
            Log.v(TAG, "getCantidadBonificada_Agrupados:▬▬▬▬▬▬▬▬ ▬▬▬▬▬▬▬▬▬▬▬▬ ▬▬▬▬▬▬▬");
            return cantidadBonificacion;

        } else {
            /*------------------------------------- PRODUCTOS NO AGRUPADOS Ó ---------------------------------------*/
            Log.d(TAG, "Es de tipo Ó");
            int cantidadEvaluar = 0;
            if (itemPromocion.getEvaluarEnUnidadMayor() == 1) {//Se acumula con cantidad mayor
                cantidadEvaluar = getCantidadUnidadMayor(itemDetalle.getIdProducto(), itemDetalle.getIdUnidadMedida(), itemDetalle.getCantidad());
            } else {//Se acumula con cantidad menor
                cantidadEvaluar = getCantidadUnidadMenor(itemDetalle.getIdProducto(), itemDetalle.getIdUnidadMedida(), itemDetalle.getCantidad());
            }

            cantidadBonificacion = verificarTipoCondicion(itemPromocion, cantidadEvaluar, itemDetalle.getPrecioNeto());
            Log.i(TAG, "La Promocion del tipo O cumplió con su condición, cantidad bonificacion -> " + cantidadBonificacion);
            return cantidadBonificacion;
        }

    }

    private int getCantidadBonificada_AcumuladoPuro(PromocionDetalleModel itemPromocion, PedidoDetalleModel itemDetalle, String numeroPedido) {
        int cantidadBonificacion = 0;
        Log.v(TAG, "getCantidadBonificada_AcumuladoPuro pedido ->" + numeroPedido);
        Log.v(TAG, "▬▬▬▬▬▬▬▬ Analisis del detalle ▬▬▬▬▬▬▬");

        ArrayList<PedidoDetalleModel> listaDetallePedido = daoPedido.getListaProductoPedidoEvaluar(numeroPedido, itemDetalle.getItem());
        ArrayList<PromocionDetalleModel> listaAcumulado = daoPromocion.getListaAcumulados(itemPromocion.getIdPromocion(), itemPromocion.getItem());
        int cantidadAcumulada = 0;
        double montoAcumulado = 0.0;

        for (PedidoDetalleModel pedidoDetalleModel : listaDetallePedido) {
            for (PromocionDetalleModel promocionDetalleModel : listaAcumulado) {
                if (pedidoDetalleModel.getIdProducto().equals(promocionDetalleModel.getEntrada())) {
                    int cantidadEvaluar = 0;
                    if (promocionDetalleModel.getEvaluarEnUnidadMayor() == 1) {//Se acumula con cantidad mayor
                        cantidadEvaluar = getCantidadUnidadMayor(pedidoDetalleModel.getIdProducto(), pedidoDetalleModel.getIdUnidadMedida(), pedidoDetalleModel.getCantidad());
                    } else {//Se acumula con cantidad menor
                        cantidadEvaluar = getCantidadUnidadMenor(pedidoDetalleModel.getIdProducto(), pedidoDetalleModel.getIdUnidadMedida(), pedidoDetalleModel.getCantidad());
                    }

                    cantidadAcumulada += cantidadEvaluar;
                    montoAcumulado += pedidoDetalleModel.getPrecioNeto();
                }
            }
        }

        if (itemPromocion.getTipoPromocion().equals(PromocionDetalleModel.TIPO_PROMOCION_CANTIDAD)) {
            cantidadBonificacion = verificarTipoCondicionxCantidad_acumulados(itemPromocion, cantidadAcumulada);
        } else {
            cantidadBonificacion = verificarTipoCondicionxMonto_acumulados(itemPromocion, montoAcumulado);
        }
        Log.d(TAG, "Cantidad bonificacion obtenido por AcumuladoPuro -> " + cantidadBonificacion);
        return cantidadBonificacion;
    }

    private int getCantidadBonificada_AcumuladoMultiple(PromocionDetalleModel itemPromocion, PedidoDetalleModel itemDetalle, String numeroPedido) {
        int cantidadBonificacion = 0;
        Log.v(TAG, "getCantidadBonificada_AcumuladoMultiple pedido ->" + numeroPedido);
        Log.v(TAG, "▬▬▬▬▬▬▬▬ Analisis del detalle ▬▬▬▬▬▬▬");

        ArrayList<PedidoDetalleModel> listaDetallePedido = daoPedido.getListaProductoPedidoEvaluar(numeroPedido, itemDetalle.getItem());
        ArrayList<Integer> listaNumerosAgrupados = daoPromocion.getListaAgrupados(itemPromocion.getIdPromocion(), itemPromocion.getItem());
        ArrayList<HashMap<String, Object>> listaGruposCumplidos = new ArrayList<>();

        for (int numeroAgrupado : listaNumerosAgrupados) {
            int cantidadBonificacionxGrupo = 0;
            ArrayList<PromocionDetalleModel> listaAcumuladoxGrupo = daoPromocion.getListaAcumuladosMultiple(itemPromocion.getIdPromocion(), itemPromocion.getItem(), numeroAgrupado);

            int cantidadAcumulada = 0;
            double montoAcumulado = 0.0;

            for (PedidoDetalleModel pedidoDetalleModel : listaDetallePedido) {
                for (PromocionDetalleModel promocionDetalleModel : listaAcumuladoxGrupo) {
                    if (pedidoDetalleModel.getIdProducto().equals(promocionDetalleModel.getEntrada())) {
                        int cantidadEvaluar = 0;
                        if (promocionDetalleModel.getEvaluarEnUnidadMayor() == 1) {//Se acumula con cantidad mayor
                            cantidadEvaluar = getCantidadUnidadMayor(pedidoDetalleModel.getIdProducto(), pedidoDetalleModel.getIdUnidadMedida(), pedidoDetalleModel.getCantidad());
                        } else {//Se acumula con cantidad menor
                            cantidadEvaluar = getCantidadUnidadMenor(pedidoDetalleModel.getIdProducto(), pedidoDetalleModel.getIdUnidadMedida(), pedidoDetalleModel.getCantidad());
                        }

                        cantidadAcumulada += cantidadEvaluar;
                        montoAcumulado += pedidoDetalleModel.getPrecioNeto();
                    }
                }
            }

            /*La validaciones para tipoPromocion y tipoCondicion se deben hacer con el itemPromocion de la lista de agrupados actual,
            mas no con el itemPromocion que llega como parametro ya que ese es del producto del detalle que se está analizando
            de la listaAcumuladoxGrupo todos los items deben tener la mismas condiciones y tipo, es decir que tomando el primer elemento se puede analizar*/
            if (listaAcumuladoxGrupo.get(0).getTipoPromocion().equals(PromocionDetalleModel.TIPO_PROMOCION_CANTIDAD)) {
                cantidadBonificacionxGrupo = verificarTipoCondicionxCantidad_acumulados(listaAcumuladoxGrupo.get(0), cantidadAcumulada);
            } else {
                cantidadBonificacionxGrupo = verificarTipoCondicionxMonto_acumulados(listaAcumuladoxGrupo.get(0), montoAcumulado);
            }
            Log.v(TAG, "Cantidad bonificacion obtenido por AcumuladoMultiple -> agrupado " + numeroAgrupado + ": " + cantidadBonificacionxGrupo);

            if (cantidadBonificacionxGrupo > 0) {
                HashMap<String, Object> grupoCumplido = new HashMap<>();
                grupoCumplido.put("agrupado", numeroAgrupado);
                grupoCumplido.put("cantidadBonificacion", cantidadBonificacionxGrupo);
                grupoCumplido.put("itemPromocion", listaAcumuladoxGrupo.get(0));
                listaGruposCumplidos.add(grupoCumplido);
            }
        }

        if (listaNumerosAgrupados.size() == listaGruposCumplidos.size()) {
            //Si todos los grupos cumplieron sus condiciones se procede a calcular la cantidad a bonificar
            //El primero de la lista es quien manda, es quien tiene la primera validación a cumplirse, debe ser tipo condicion "mayor o igual que"

            PromocionDetalleModel primerItemPromocionGrupo = (PromocionDetalleModel) listaGruposCumplidos.get(0).get("itemPromocion");//Obtenemos los datos de condicion y tipo del grupo
            if (primerItemPromocionGrupo.getTipoCondicion() == PromocionDetalleModel.TIPO_CONDICION_MAYOR_IGUAL) {
                //Si el primero es TIPO_CONDICION_MAYOR_IGUAL y la lista de agrupados es igual a la lista de grupos cumplidos, quiere decir que todos cumplieron con sus condiciones, por lo que se bonifica
                //Ya se verificó que el primero de la lista sea TIPO_CONDICION_MAYOR_IGUAL, ahora se debe verificar el resto de los grupos

                //Si el resto de agrupados tiene la condición por cada, entonces se obtiene el menor monto bonificado (sin tomar al primero grupo de la lista)
                if (sonTodosCondicionPorCada(listaGruposCumplidos)) {
                    int cantidadBonifMenor = (int) listaGruposCumplidos.get(0).get("cantidadBonificacion");
                    int cantidadBonifTem = (int) listaGruposCumplidos.get(0).get("cantidadBonificacion");
                    ;

                    if (listaGruposCumplidos.size() > 1) {
                        for (int x = 1; x < listaGruposCumplidos.size(); x++) {//No se toma en cuenta al primero de la lista iniciando en x=1
                            int cantidadx = (int) listaGruposCumplidos.get(x).get("cantidadBonificacion");

                            if (cantidadBonifTem != cantidadx) {
                                /*Si no son iguales*/
                                if (cantidadx < cantidadBonifMenor) {
                                    /*Si la cantidad es la menor hasta ahora se guarda como tal*/
                                    cantidadBonifMenor = cantidadx;
                                }
                            }
                        }
                    }
                    Log.w(TAG, "sonTodosCondicionPorCada, retornando la cantidad menor bonificada: " + cantidadBonifMenor);
                    cantidadBonificacion = cantidadBonifMenor;//Al final retornar la cantidad menor que se puede bonificar combinando los "por cada"
                } else {
                    //Si el resto de agrupados tiene la condición mayor o igual , entonces se obtiene cualquier monto ya que todos tendrán el mismo por ser mayor o igual no varía.
                    cantidadBonificacion = (int) listaGruposCumplidos.get(0).get("cantidadBonificacion");
                    Log.v(TAG, "sonTodosCondicionMayorIgual, retornando la cantidad bonificada: " + cantidadBonificacion);
                }
            } else {
                cantidadBonificacion = 0;//Se retorna cero porque no hay mecánica para cuando el primer grupo sea por cada
                Log.w(TAG, "primerItemPromocionGrupo no es TIPO_CONDICION_MAYOR_IGUAL, retornando: " + cantidadBonificacion);
            }
        }
        Log.w(TAG, "getCantidadBonificada_AcumuladoMultiple, retornando al final: " + cantidadBonificacion);
        return cantidadBonificacion;
    }

    private boolean sonTodosCondicionPorCada(ArrayList<HashMap<String, Object>> listaGruposCumplidos) {
        boolean flag = true;
        for (HashMap<String, Object> grupoCumplido : listaGruposCumplidos) {
            PromocionDetalleModel itemPromocionGrupo = (PromocionDetalleModel) grupoCumplido.get("itemPromocion");
            if (itemPromocionGrupo.getTipoCondicion() == PromocionDetalleModel.TIPO_CONDICION_MAYOR_IGUAL) {
                flag = false;
            }
        }
        return flag;
    }

    private int verificarTipoCondicionxCantidad_acumulados(PromocionDetalleModel itemPromocion, int cantidadEntrada) {
        boolean promocionValida = false;
        int cantidad = 0;
        Log.w(TAG, "---------------------- verificarTipoCondicion Acumulado----------------------");
        //Primero verificamos si la promocion tiene limite
        if (itemPromocion.getCantidadLimite() > 0) {
            //Si tiene limite, verificamos que estemos dentro del límite (Ejemplo: maximo 100. Debemos tener menos de 100, ya habrá otra promo que empiece xCada 100)
            if (cantidadEntrada < itemPromocion.getCantidadLimite()) {
                promocionValida = true;
            }
        } else {
            promocionValida = true;
        }

        if (promocionValida) {
            if (cantidadEntrada >= itemPromocion.getCantidadCondicion()) {
                if (itemPromocion.getTipoCondicion() == PromocionDetalleModel.TIPO_CONDICION_MAYOR_IGUAL) {
                    cantidad = itemPromocion.getCantidadBonificada();
                } else if (itemPromocion.getTipoCondicion() == PromocionDetalleModel.TIPO_CONDICION_POR_CADA) {
                    cantidad = (cantidadEntrada / itemPromocion.getCantidadCondicion()) * itemPromocion.getCantidadBonificada();
                    Log.d(TAG, "cantidad maxima para bonificar:" + itemPromocion.getMaximaBonificacion());
                    if (itemPromocion.getMaximaBonificacion() > 0) {
                        if (cantidad > itemPromocion.getMaximaBonificacion()) {
                            cantidad = itemPromocion.getMaximaBonificacion();
                        }
                    }
                }
            }
        } else {
            Log.d(TAG, "La promocion no es valida porque la cantidad(" + cantidadEntrada + ") supera el Limite de " + itemPromocion.getCantidadLimite());
        }
        Log.d(TAG, "verificarTipoCondicionxCantidad_acumulados: cantidadBonificacion:" + cantidad);
        return cantidad;
    }

    private int verificarTipoCondicionxMonto_acumulados(PromocionDetalleModel itemPromocion, double montoEntrada) {
        boolean promocionValida = false;
        int cantidad = 0;
        Log.w(TAG, "---------------------- verificarTipoCondicion Acumulado----------------------");
        //Primero verificamos si la promocion tiene limite
        if (itemPromocion.getMontoLimite() > 0) {
            //Si tiene limite, verificamos que estemos dentro del límite (Ejemplo: maximo 100. Debemos tener menos de 100, ya habrá otra promo que empiece xCada 100)
            if (montoEntrada < itemPromocion.getMontoLimite()) {
                promocionValida = true;
            }
        } else {
            promocionValida = true;
        }

        if (promocionValida) {
            if (montoEntrada >= itemPromocion.getMontoCondicion()) {
                if (itemPromocion.getTipoCondicion() == PromocionDetalleModel.TIPO_CONDICION_MAYOR_IGUAL) {
                    cantidad = itemPromocion.getCantidadBonificada();
                } else if (itemPromocion.getTipoCondicion() == PromocionDetalleModel.TIPO_CONDICION_POR_CADA) {
                    cantidad = ((int) (montoEntrada / itemPromocion.getMontoCondicion())) * itemPromocion.getCantidadBonificada();
                    Log.d(TAG, "cantidad maxima para bonificar:" + itemPromocion.getMaximaBonificacion());
                    if (itemPromocion.getMaximaBonificacion() > 0) {
                        if (cantidad > itemPromocion.getMaximaBonificacion()) {
                            cantidad = itemPromocion.getMaximaBonificacion();
                        }
                    }
                }
            }
        } else {
            Log.d(TAG, "La promocion no es valida porque el monto(" + montoEntrada + ") supera el Limite de " + itemPromocion.getMontoLimite());
        }
        Log.d(TAG, "verificarTipoCondicionxMonto_acumulados: cantidadBonificacion:" + cantidad);
        return cantidad;
    }

    private int verificarTipoCondicion(PromocionDetalleModel itemPromocion, int cantidadEntrada, double montoEntrada) {
        boolean promocionValida = false;
        int cantidad = 0;
        Log.w(TAG, "---------------------- verificarTipoCondicion----------------------");


        if (itemPromocion.getTipoPromocion().equals(PromocionDetalleModel.TIPO_PROMOCION_CANTIDAD)) {
            //Primero verificamos si la promocion tiene limite
            if (itemPromocion.getCantidadLimite() > 0) {
                //Si tiene limite, verificamos que estemos dentro del límite (Ejemplo: maximo 100. Debemos tener menos de 100, ya habrá otra promo que empiece xCada 100)
                if (cantidadEntrada < itemPromocion.getCantidadLimite()) {
                    promocionValida = true;
                }
            } else {
                promocionValida = true;
            }

            if (promocionValida) {
                Log.d(TAG, "verificarTipoCondicion: itemPromocion.getTipoPromocion() -> C");
                Log.d(TAG, "verificarTipoCondicion: itemDetalle.getCantidad()-> " + cantidadEntrada);
                Log.d(TAG, "verificarTipoCondicion: itemPromocion.getCantidadCondicion()-> " + itemPromocion.getCantidadCondicion());

                if (cantidadEntrada >= itemPromocion.getCantidadCondicion()) {
                    if (itemPromocion.getTipoCondicion() == PromocionDetalleModel.TIPO_CONDICION_MAYOR_IGUAL) {
                        cantidad = itemPromocion.getCantidadBonificada();
                    } else if (itemPromocion.getTipoCondicion() == PromocionDetalleModel.TIPO_CONDICION_POR_CADA) {
                        cantidad = (cantidadEntrada / itemPromocion.getCantidadCondicion()) * itemPromocion.getCantidadBonificada();
                        Log.d(TAG, "cantidad maxima para bonificar:" + itemPromocion.getMaximaBonificacion());
                        if (itemPromocion.getMaximaBonificacion() > 0) {
                            if (cantidad > itemPromocion.getMaximaBonificacion()) {
                                cantidad = itemPromocion.getMaximaBonificacion();
                            }
                        }
                    }
                }
            } else {
                Log.d(TAG, "La promocion no es valida porque la cantidad(" + cantidadEntrada + ") supera el Limite de " + itemPromocion.getCantidadLimite());
            }
        } else {
            //Primero verificamos si la promocion tiene limite
            if (itemPromocion.getMontoLimite() > 0) {
                //Si tiene limite, verificamos que estemos dentro del límite (Ejemplo: maximo 100. Debemos tener menos de 100, ya habrá otra promo que empiece xCada 100)
                if (montoEntrada < itemPromocion.getMontoLimite()) {
                    promocionValida = true;
                }
            } else {
                promocionValida = true;
            }

            if (promocionValida) {
                Log.d(TAG, "verificarTipoCondicion: itemPromocion.getTipoPromocion() -> C");
                Log.d(TAG, "verificarTipoCondicion: itemDetalle.getPrecioNeto()-> " + montoEntrada);
                Log.d(TAG, "verificarTipoCondicion: itemPromocion.getMontoCondicion()-> " + itemPromocion.getMontoCondicion());

                if (montoEntrada >= itemPromocion.getMontoCondicion()) {
                    if (itemPromocion.getTipoCondicion() == PromocionDetalleModel.TIPO_CONDICION_MAYOR_IGUAL) {
                        cantidad = itemPromocion.getCantidadBonificada();
                    } else if (itemPromocion.getTipoCondicion() == PromocionDetalleModel.TIPO_CONDICION_POR_CADA) {
                        cantidad = ((int) (montoEntrada / itemPromocion.getMontoCondicion())) * itemPromocion.getCantidadBonificada();
                        Log.d(TAG, "cantidad maxima para bonificar:" + itemPromocion.getMaximaBonificacion());
                        if (itemPromocion.getMaximaBonificacion() > 0) {
                            if (cantidad > itemPromocion.getMaximaBonificacion()) {
                                cantidad = itemPromocion.getMaximaBonificacion();
                            }
                        }
                    }
                }
            } else {
                Log.d(TAG, "La promocion no es valida porque el monto(" + montoEntrada + ") supera el Limite de " + itemPromocion.getMontoLimite());
            }
        }
        Log.w(TAG, "--------------------------  -------------------------------");
        return cantidad;
    }

    private int getCantidadUnidadMenor(String idProducto, String idUnidadMedida, int cantidad) {
        int esMinima = daoProducto.isUnidadMinima(idProducto, idUnidadMedida);
        if (esMinima == 1 || esMinima == -1) {
            return cantidad;
        } else {
            int factorConversion = daoProducto.getFactorConversion(idProducto);
            return cantidad * factorConversion;
        }
    }

    private int getCantidadUnidadMayor(String idProducto, String idUnidadMedida, int cantidad) {
        int esMinima = daoProducto.isUnidadMinima(idProducto, idUnidadMedida);
        if (esMinima == 0 || esMinima == -1) {
            return cantidad;
        } else {
            int factorConversion = daoProducto.getFactorConversion(idProducto);
            return cantidad / factorConversion;
        }
    }

    private void showDialogCreditoInsuficiente(double saldoCredito, boolean mensajePorProducto) {
        DecimalFormat formateador = Util.formateador();
        ;

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(PedidoActivity.this);
        alertDialog.setIcon(R.drawable.ic_dialog_alert);
        alertDialog.setTitle("Se superó crédito disponible (S/. " + formateador.format(saldoCredito) + ")");
        if (mensajePorProducto)
            alertDialog.setMessage("El producto no ha sido agregado o modificado");
        else
            alertDialog.setMessage("Para guardar el pedido como crédito, debe reducir el importe total del pedido");
        alertDialog.setCancelable(false);

        alertDialog.setPositiveButton("ACEPTAR", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        alertDialog.show();
    }

    private void showDialogCambioPolitica(String politicaCliente, String politicaPorDefecto, int cantidadPaquetes, int cantidadMinima, boolean existePrecioCero, boolean existePoliticaPorDefecto) {
        Log.i(TAG, "showDialogCambioPolitica");
        int icon = R.drawable.ic_dialog_alert;

        String mensaje = "La cantidad de paquetes del pedido (" + cantidadPaquetes + ") no alcanza a la cantidad minima (" + cantidadMinima + ") para matener la politica " + politicaCliente;

        if (existePoliticaPorDefecto)
            mensaje += "\nTodos los precios han sido cambiados por la política " + politicaPorDefecto;
        else {
            mensaje += "\nSin embargo nos precios no se han podido cambiar a la politica " + politicaPorDefecto + ". Comuníquese con el administrador";
            icon = R.drawable.ic_dialog_error;
        }

        if (existePrecioCero) {
            mensaje += "\n\n¡ Existen productos sin precio, verificar cuáles son e informar inmediatamente !";
            icon = R.drawable.ic_dialog_error;
        }

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(PedidoActivity.this);
        alertDialog.setTitle("No se mantuvo la politica del cliente");
        alertDialog.setIcon(icon);
        alertDialog.setMessage(mensaje);
        alertDialog.setCancelable(false);

        alertDialog.setPositiveButton("ACEPTAR", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                new AsyncTask<Void, Void, String>() {
                    final String ENVIADO = "E";
                    final String INCOMPLETO = "I";
                    final String PENDIENTE = "P";
                    final String TRANSFERIDO = "T";
                    final String JSONEXCEPTION = "jsonException";
                    final String SIN_CONEXION = "SinConexion";
                    final String OTRO_ERROR = "error";

                    ProgressDialog pDialog;
                    String cadenaResultado = "";

                    @Override
                    protected void onPreExecute() {
                        super.onPreExecute();
                        pDialog = new ProgressDialog(PedidoActivity.this);
                        pDialog.setCancelable(false);
                        pDialog.setIndeterminate(true);
                        pDialog.setMessage("Enviando pedido...");
                        pDialog.show();
                    }

                    @Override
                    protected String doInBackground(Void... strings) {
                        //GuardarPedido();//EL pedido ya está guardado
                        SoapManager soapManager = new SoapManager(getApplicationContext());
                        Gson gson = new Gson();
                        if (Util.isConnectingToRed(getApplicationContext())) {
                            try {
                                while (gpsTracker.getLatitude() == 0.0 && gpsTracker.getLongitude() == 0.0) {
                                    Log.d(TAG, "latitud y longitud 0.0");//Mantener el hilo trabajando hasta que se tome alguna posición
                                }
                                daoPedido.actualizarLatLongPedido(numeroPedido, gpsTracker.getLatitude(), gpsTracker.getLongitude());

                                if (ACCION_PEDIDO == ACCION_NUEVO_PEDIDO) {
                                    String horaFin = Util.getHoraTelefonoString();
                                    daoPedido.actualizarHoraFinPedido(numeroPedido, horaFin);
                                } else if (ACCION_PEDIDO == ACCION_EDITAR_PEDIDO) {
                                    String horaModificacion = Util.getFechaHoraTelefonoString();
                                    daoPedido.actualizarFechaModificacionPedido(numeroPedido, horaModificacion);
                                }

                                ArrayList<DTOPedido> pedidoEnviar = daoPedido.getDTOPedidoCompleto(numeroPedido);
                                String cadena = gson.toJson(pedidoEnviar);
                                cadenaResultado = soapManager.enviarPendientes(TablesHelper.ObjPedido.ActualizarObjPedido, cadena);

                                return daoPedido.actualizarFlagPedidos(cadenaResultado);//Retorna el flag resultado
                                //return "I";
                            } catch (JsonParseException ex) {
                                ex.printStackTrace();
                                return JSONEXCEPTION;
                            } catch (XmlPullParserException e) {
                                e.printStackTrace();
                                return SIN_CONEXION;
                            } catch (Exception e) {
                                e.printStackTrace();
                                return OTRO_ERROR;
                            }
                        } else {
                            return SIN_CONEXION;
                        }
                    }

                    @Override
                    protected void onPostExecute(String respuesta) {
                        super.onPostExecute(respuesta);
                        pDialog.dismiss();
                        switch (respuesta) {
                            case ENVIADO:
                                showDialogoPostEnvio("Envío satisfactorio", "El pedido fue ingresado al servidor", R.drawable.ic_dialog_check);
                                break;
                            case INCOMPLETO:
                                showDialogoPostEnvio("Atención", "No se pudieron guardar todos los datos", R.drawable.ic_dialog_alert);
                                break;
                            case PENDIENTE:
                                showDialogoPostEnvio("Atención", "El servidor no pudo ingresar el pedido", R.drawable.ic_dialog_error);
                                break;
                            case TRANSFERIDO:
                                showDialogoPostEnvio("Atención", "El pedido ya se encuentra en proceso de facturación \nComuníquese con el administrador", R.drawable.ic_dialog_block);
                                break;
                            case SIN_CONEXION:
                                showDialogoPostEnvio("Sin conexión", "Es probable que no tenga acceso a INTERNET, El pedido se guardó localmente", R.drawable.ic_dialog_error);
                                break;
                            case JSONEXCEPTION:
                                showDialogoPostEnvio("Atención", "El pedido fue enviado pero no se pudo verificar\nConsulte con el administrador", R.drawable.ic_dialog_alert);
                                break;
                            case OTRO_ERROR:
                                showDialogoPostEnvio("Error", "No se pudo enviar el pedido, se guardó localmente", R.drawable.ic_dialog_error);
                                break;
                            default:
                                if (ventas360App.getModoVenta().equals(VendedorModel.MODO_PREVENTA) && !ventas360App.getSettings_preventaEnLinea()) {
                                    showDialogoPostEnvio("Error", "" + respuesta, R.drawable.ic_dialog_error);
                                } else {
                                    //Si es Autoventa no dejar salir del pedido hasta enviar correctamente o descartar
                                    AlertDialog.Builder builder = new AlertDialog.Builder(PedidoActivity.this);
                                    builder.setTitle("Atención");
                                    builder.setMessage("" + respuesta);
                                    builder.setIcon(R.drawable.ic_dialog_error);
                                    builder.setCancelable(false);
                                    builder.setPositiveButton("ACEPTAR", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            ArrayList<PedidoDetalleModel> listaProductosSinStock = getListaDetalleFromJSON(cadenaResultado);
                                            for (PedidoDetalleModel productoSinStock : listaProductosSinStock) {
                                                daoPedido.actualizarFlagSinStock(numeroPedido, productoSinStock.getIdProducto(), 1);
                                                pedidoDetalleFragment.mostrarListaProductos();
                                            }
                                        }
                                    });
                                    builder.show();
                                }
                                break;
                        }
                    }
                }.execute();
            }
        });
        alertDialog.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_AGREGAR) {
            if (resultCode == RESULT_OK) {
                int accion = data.getIntExtra("accion", ACCION_AGREGAR_PRODUCTO);
                String idProducto = data.getStringExtra("idProducto");
                String descripcion = data.getStringExtra("descripcion");
                double precioBruto = Double.parseDouble(data.getStringExtra("precio"));
                int cantidad = Integer.parseInt(data.getStringExtra("cantidad"));
                double precioNeto = Util.redondearDouble(precioBruto * cantidad);
                String idUnidadMedida = data.getStringExtra("idUnidadMedida");
                String descripcionUnidadMedida = data.getStringExtra("descripcionUnidadMedida");
                String idPoliticaPrecio = data.getStringExtra("idPoliticaPrecio");
                double peso = data.getDoubleExtra("peso", 0.0);
                double pesoNeto = Util.redondearDouble(peso * cantidad);
                String tipoProducto = data.getStringExtra("tipoProducto");
                int factorConversion = data.getIntExtra("factorConversion", 0);

                double porcentajePercepcion = data.getDoubleExtra("porcentajePercepcion", 0.0);
                double porcentajeISC = data.getDoubleExtra("porcentajeISC", 0.0);

                double percepcion = 0.0;
                //La percepción se genera cuando el pedido es Facturado (Boleta o Factura) //Cálculo de Percepcion
                /*if (daoCliente.isAfectoPercepcion(idCliente) && (tipoProducto.equals(ProductoModel.TIPO_VENTA) || tipoProducto.equals(ProductoModel.TIPO_SERVICIO))){
                    //La percepción se aplica al precio con IGV
                    percepcion = Util.redondearDouble(precioNeto * porcentajePercepcion);
                    //Log.i(TAG,"setPercepcion: "+percepcion);
                }*/

                //Cálculo de ISC
                /*El cálculo del ISC se realiza al precio base (sin IGV). Como el precio ya tiene el IGV, primero debemos disgregar*/
                double precioNetoSinIGV = precioNeto / (1 + porcentajeIGV);
                Log.i(TAG, "precioNetoSinIGV: " + precioNetoSinIGV);
                double precioNetoSinISC = precioNetoSinIGV / (1 + porcentajeISC);
                Log.i(TAG, "precioNetoSinISC: " + precioNetoSinISC);
                double isc = Util.redondearDouble(precioNetoSinISC * porcentajeISC);
                Log.i(TAG, "ISC: " + isc);

                PedidoDetalleModel pedidoDetalleModel = new PedidoDetalleModel();
                pedidoDetalleModel.setNumeroPedido(getNumeroPedidoFromFragment());
                pedidoDetalleModel.setIdProducto(idProducto);
                pedidoDetalleModel.setPrecioBruto(precioBruto);
                pedidoDetalleModel.setPrecioNeto(precioNeto);
                pedidoDetalleModel.setCantidad(cantidad);
                pedidoDetalleModel.setPesoNeto(pesoNeto);
                pedidoDetalleModel.setIdUnidadMedida(idUnidadMedida);
                pedidoDetalleModel.setIdPoliticaPrecio(idPoliticaPrecio);
                pedidoDetalleModel.setTipoProducto(tipoProducto);//Producto tipo [V]venta [B]bonificacion
                pedidoDetalleModel.setSinStock(0);//Siempre que se agregue o modifique el producto se asumirá que tiene stock por que ya ha sido validado y porque luego se volverá a validar
                pedidoDetalleModel.setPercepcion(percepcion);
                pedidoDetalleModel.setISC(isc);
                pedidoDetalleModel.setDescripcion(descripcion);
                pedidoDetalleModel.setDescripcionUnidadMedida(descripcionUnidadMedida);
                pedidoDetalleModel.setFactorConversion(factorConversion);

                boolean creditoValido = true;

                //Validar si el tipo fue para agregar o para modificar
                if (accion == ACCION_AGREGAR_PRODUCTO) {
                    //Validar si el producto ya se encuentra registrado
                    if (productoEstaRegistrado(idProducto, pedidoDetalleModel.getTipoProducto(), pedidoDetalleModel.getMalla(), pedidoDetalleModel.getIdUnidadMedida()) != -1) {
                        Snackbar.make(findViewById(android.R.id.content), "El producto ya está registrado", Snackbar.LENGTH_LONG).show();
                    } else {
                        //Validar el crédito
                        //Log.w(TAG,"SALDO DE CREDITO: "+saldoCredito);
                        if (saldoCredito > 0) {//Si el saldoCredito es > 0, entonces si se debe verificar el credito
                            if (pedidoCabeceraFragment.getPedido().getIdFormaPago().equals(FormaPagoModel.ID_FORMA_PAGO_CREDITO)) {//Si el pedido se está realizando al crédito
                                double importePedido = pedidoDetalleFragment.getMontoTotal();
                                //Log.w(TAG,"IMPORTE PEDIDO:"+importePedido+" PRECIONETO:"+precioNeto+" SALDOCREDITO:"+saldoCredito);
                                if ((importePedido + precioNeto) > saldoCredito) {
                                    creditoValido = false;
                                    showDialogCreditoInsuficiente(saldoCredito, true);
                                }
                            }
                        }

                        if (creditoValido) {
                            noBonificacionesYPedidoPendiente();//Antes de guardar y mostrar se deben quitar las bonificaciones y actualizar el pedido como pendiente
                            daoPedido.agregarItemPedidoDetalle(pedidoDetalleModel);
                            pedidoDetalleFragment.mostrarListaProductos();
                        }

                    }
                } else {//MODIFICAR PRODUCTO

                    //Validar el crédito
                    if (saldoCredito > 0) {//Si el saldoCredito es > 0, entonces si se debe verificar el credito
                        if (pedidoCabeceraFragment.getPedido().getIdFormaPago().equals(FormaPagoModel.ID_FORMA_PAGO_CREDITO)) {//Si el pedido se está realizando al crédito
                            double importePedido = pedidoDetalleFragment.getMontoTotal();
                            double precioNetoAnterior = daoPedido.getPrecioNetoProductoPedido(getNumeroPedidoFromFragment(), idProducto);
                            Log.w(TAG, "IMPORTE PEDIDO:" + importePedido + "PRECIOANTERIOR:" + precioNetoAnterior + " PRECIONETO:" + precioNeto + " SALDOCREDITO:" + saldoCredito);
                            if ((importePedido - precioNetoAnterior + precioNeto) > saldoCredito) {
                                creditoValido = false;
                                showDialogCreditoInsuficiente(saldoCredito, true);
                            }
                        }
                    }

                    if (creditoValido) {
                        noBonificacionesYPedidoPendiente();//Antes de guardar y mostrar se deben quitar las bonificaciones y actualizar el pedido como pendiente
                        daoPedido.modificarItemDetallePedido(pedidoDetalleModel);
                        pedidoDetalleFragment.mostrarListaProductos();
                    }
                }

            }
        } else if (requestCode == REQUEST_CODE_UBICACION) {
            Log.e(TAG, "REQUEST_CODE_UBICACION -> " + resultCode);
            //No se valida si es RESULT_OK porque no tenemos control de la actividad Settings que se ha lanzado
            if (gpsTracker.isGPSEnabled()) {
                if (Build.VERSION.SDK_INT >= 23) {
                    if (ContextCompat.checkSelfPermission(PedidoActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                            || ContextCompat.checkSelfPermission(PedidoActivity.this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(PedidoActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_PREMISOS_UBICACION);
                    } else
                        gpsTracker.getLocations();
                } else
                    gpsTracker.getLocations();
            } else {
                showDialogoUbicacion();
            }

        } else if (requestCode == REQUEST_ENCUESTA) {
            if (resultCode == RESULT_OK) {
                setResult(RESULT_OK);
                finish();
            } else {
                //En caso se descarte la encuesta (solo se podrá descartar en caso no sea una encuesta obligatoria)
                setResult(RESULT_OK);
                finish();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_PREMISOS_UBICACION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                gpsTracker.getLocations();
            } else {
                Toast.makeText(this, "No se otorgaron permisos de ubicación", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        gpsTracker.stopUsingGPS();
    }

    boolean GuardarPedido(boolean anularPedido) {
        //En aso de ser PUNTO_VENTA auto seleccionar el cliente general
        if (idCliente.equals("") && ventas360App.getTipoVendedor().equals(VendedorModel.TIPO_PUNTO_VENTA)) {
            //Si no se ha escogido un cliente, se debe seleccionar el cliente por defecto indicado en la sucursal
            String idClienteGeneral = daoConfiguracion.getIdClienteGeneral();
            if (!idClienteGeneral.equals("")) {
                ClienteModel clienteModel = daoCliente.getCliente(idClienteGeneral);
                if (clienteModel != null) {
                    autocomplete_busqueda.setText(clienteModel.getRazonSocial());
                    autocomplete_busqueda.setEnabled(false);
                    autocomplete_busqueda.dismissDropDown();

                    idCliente = clienteModel.getIdCliente();
                    saldoCredito = daoCliente.getSaldoCredito(idCliente, getNumeroPedidoFromFragment());
                    isAfectoPercepcion = daoCliente.isAfectoPercepcion(idCliente);
                    //Una vez se obtenga el codigo del cliente se tiene que mandar ese codigo a los fragment hijos y estos puedan usarlo
                    pedidoCabeceraFragment.setIdCliente(idCliente);//Ahora el fragment tiene el codigo del cliente
                } else {
                    Toast.makeText(getApplicationContext(), "Cliente general no establecido", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(getApplicationContext(), "Cliente general no establecido", Toast.LENGTH_SHORT).show();
            }
        }

        //Validar cliente
        if (idCliente.equals("")) {
            autocomplete_busqueda.setError("Seleccionar cliente");
            autocomplete_busqueda.requestFocus();
            return false;
        } else {
            //Validar los campos de la cabecera
            if (pedidoCabeceraFragment.validarCampos()) {
                PedidoCabeceraModel pedidoModel = pedidoCabeceraFragment.getPedido();
                pedidoModel.setLatitud(gpsTracker.getLatitude());
                pedidoModel.setLongitud(gpsTracker.getLongitude());
                pedidoModel.setPorcentajeBateria(getEstadoBateria());
                if (anularPedido) {
                    pedidoModel.setEstado(PedidoCabeceraModel.ESTADO_ANULADO);
                    pedidoModel.setIdMotivoNoVenta(PedidoCabeceraModel.ID_MOTIVO_NO_COMPRA_DEFAULT);
                }

                //Si la cabecera ya está guardada, se actualiza los datos (puede que se hayan modificado mientras se ingresaban los productos)
                if (cabeceraGuardada) {
                    daoPedido.actualizarPedidoCabecera(pedidoModel);
                } else {
                    //Si no esta guardada, determinar si es un pedido nuevo o se está modificando uno
                    if (ACCION_PEDIDO == ACCION_NUEVO_PEDIDO) {
                        daoPedido.guardarPedidoCabecera(pedidoModel);
                    } else if (ACCION_PEDIDO == ACCION_EDITAR_PEDIDO) {
                        daoPedido.actualizarPedidoCabecera(pedidoModel);
                    }
                }
                cabeceraGuardada = true;
                //Si cabeceraGuardada cambia su estado a true, quiere decir que ha sufrido cambios. Por lo tanto validamos si se está modificando el pedido
                //De esa forma quitamos la opción para descartar el pedido mediante el boton cerrar o el botón atrás y obligamos al usuario a guardar los cambios
                //Enviandolo al servidor o guardando localmente
                if (ACCION_PEDIDO == ACCION_EDITAR_PEDIDO) {
                    noPermitirCerrar();
                }

            } else {
                mViewPager.setCurrentItem(mSectionsPagerAdapter.PAGE_PEDIDO);
                return false;
            }
        }
        return true;
    }

    private void noBonificacionesYPedidoPendiente() {
        Log.i(TAG, "Bonificaciones removidas y pedido actualizado como pendiente, por seguridad");
        //Tambien se quitan las bonificaciones, no se quita antes porque puede que salgan sin guardar, por lo tanto no se generarían de nuevo las bonificaciones
        daoPedido.eliminarBonificaciones(getNumeroPedidoFromFragment());
        //Aqui ya se elimina siempre las bonificaciones antes de guardar.
        //Cada que se modifique algo cambiar a pendiente para que se envie nuevamente si es que se cierra la app inesperadamente (Y no se quede con flag Enviado)
        daoPedido.actualizarFlagPedido(getNumeroPedidoFromFragment(), PedidoCabeceraModel.FLAG_PENDIENTE);
    }

    public void noPermitirCerrar() {
        Util.actualizarToolBar("", false, this);
        cabeceraGuardada = true; //Para que el onBack valide y no deje retroceder

    }

    public void setFab_agregarProducto(FloatingActionButton fab_agregarProducto) {
        this.fab_agregarProducto = fab_agregarProducto;
        this.fab_agregarProducto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                agregarProducto();
            }
        });
    }

    public void agregarProducto() {
        Log.d(TAG, "agregarProducto()");
        //La primera vez en agregar un producto se debe guardar la cebecera,
        // si no esta guardada se valida y se guarda para luego agregar el producto
        if (!cabeceraGuardada) {
            GuardarPedido(false);
            Log.d(TAG, "cabecera guardada");
        }

        //Si la cabecera ahora si está guardada
        if (cabeceraGuardada) {
            Log.d(TAG, "cabecera guardada, validando campos");
            if (pedidoCabeceraFragment.validarCampos()) {
                //REQUEST_CODE_AGREGAR identificará desde donde(Activity) se está retornando cuando se ejecute el método onActivityResult
                Intent intent = new Intent(PedidoActivity.this, AgregarProductoActivity.class);
                //PRODUCTO_AGREGAR(buscar producto) o PRODUCTO_MODIFICAR(cargar un producto para modificar)
                intent.putExtra("accion", ACCION_AGREGAR_PRODUCTO);
                intent.putExtra("numeroPedido", getNumeroPedidoFromFragment());
                intent.putExtra("idCliente", idCliente);
                startActivityForResult(intent, REQUEST_CODE_AGREGAR);
            }
        }
    }

    public void modificarProducto(String idProducto, String descripcion, double precioBruto) {
        //La primera vez en agregar un producto se debe guardar la cebecera,
        // si no esta guardada se valida y se guarda para luego agregar el producto
        if (!cabeceraGuardada) {
            GuardarPedido(false);
        }

        if (cabeceraGuardada) {
            if (pedidoCabeceraFragment.validarCampos()) {
                //REQUEST_CODE_AGREGAR identificará desde donde(Activity) se está retornando cuando se ejecute el método onActivityResult
                Intent intent = new Intent(PedidoActivity.this, AgregarProductoActivity.class);
                //PRODUCTO_AGREGAR(buscar producto) o PRODUCTO_MODIFICAR(cargar un producto para modificar)
                intent.putExtra("accion", ACCION_MODIFICAR_PRODUCTO);
                intent.putExtra("idCliente", idCliente);
                intent.putExtra("numeroPedido", getNumeroPedidoFromFragment());
                intent.putExtra("idProducto", idProducto);
                intent.putExtra("descripcion", descripcion);
                intent.putExtra("precioBruto", precioBruto);
                startActivityForResult(intent, REQUEST_CODE_AGREGAR);
            }
        }
    }

    private void calcularBonificaciones2() {
        Gson gson = new Gson();
        String idVendedor = ventas360App.getIdVendedor();
        String numeroPedido = getNumeroPedidoFromFragment(); //Obtenecemos el numeroPedido
        ArrayList<PedidoDetalleModel> pedidoDetalle = daoPedido.getListaProductoPedido(numeroPedido);//Obtenemos todo el detalle del pedido

        ArrayList<HashMap<String, Object>> listaFinalCantidadesBonificadas = new ArrayList<>();
        ArrayList<PromBonificacionModel> listaFinalPromocionesGeneradas = new ArrayList<>();
        for (PedidoDetalleModel pedidoDetalleOriginal : pedidoDetalle) {
            //Esta lista guardará todas las bonificaciones obtenidas por el producto entrada, para al final tomar seleccionar las idóneos, dándole prioridad al que sea PorCliente*/
            ArrayList<HashMap<String, Object>> listaBonificacionesDelProducto = new ArrayList<>();
            ArrayList<PromBonificacionModel> listaPromocionesGeneradasDelProducto = new ArrayList<>();
//                Aqui si se obtiene mas de una promocion para el producto, tal vez con un distinct de entrada en PromocionDetalle, se puede quitar aquí
//                o sería mejor quitar cuando se genere la bonificacion ya que a lo mejor uno no cumpla su condición y no necesite quitarlo
            ArrayList<PromBonificacionModel> promocionesValidas = daoBonificacion.getPromocionesValidas(pedidoDetalleOriginal, idCliente, idVendedor, numeroPedido);
            for (PromBonificacionModel itemPromocion : promocionesValidas) {

                //Si se altera algo en el pedidoDetalleOriginal, es mejor crear aqui un nuevo objeto con sus datos
                Log.e(TAG, "itemPromocion.getAcumulado() ==> " + itemPromocion.getDescripcion());

                /* ----------------- ACUMULADOS -------------------*/
                //para cada grupo, obtenemos la lista de productos y validamos
                ArrayList<ProductoModel> productosPromocion = daoBonificacion.getProductosPromocion(itemPromocion.getIdGrupo());//Obtenemos todo el detalle del pedido

                for (ProductoModel prod : productosPromocion) {
                    if (prod.getIdProducto().equals(pedidoDetalleOriginal.getIdProducto())) {
                        listaPromocionesGeneradasDelProducto.add(itemPromocion);

                        //obtenidos las promociones que aplican al producto, se evalua si cumple las condiciones y la cantidad a bonificar|
                        int cantidadBonificada = 0;

                        float desde = Float.valueOf(itemPromocion.getDesde());
                        float hasta = Float.valueOf(itemPromocion.getHasta());
                        float porcada = itemPromocion.getPorcada();
                        int rango = itemPromocion.getIdRango();

                        String promUnidad = itemPromocion.getUnidad();
                        //BONIFICACION por unidad, en todos la premiacion es en unidades
                        if (promUnidad.equals("UND")) {
                            //la comparacion debe ser por unidades, pedidoDetalleOriginal transformando a und
                            int compra_en_und = pedidoDetalleOriginal.getCantidad();
                            if (!pedidoDetalleOriginal.getIdUnidadMedida().equals("UND")) {
                                int contenido_prod = Integer.parseInt(prod.getContenido());
                                compra_en_und = pedidoDetalleOriginal.getCantidad() * contenido_prod;
                            }

                            if (compra_en_und >= desde) {
                                cantidadBonificada = (int) (compra_en_und / porcada)/**(int)rango*/;
                            } else {
                                cantidadBonificada = 0;
                            }
                        } else if (promUnidad.equals("PQT")) {
                            //la comparacion debe ser por unidad de manejo, pedidoDetalleOriginal transformando a unidad de manejo
                            int compra_en_pqt = pedidoDetalleOriginal.getCantidad();
                            if (pedidoDetalleOriginal.getIdUnidadMedida().equals("UND")) {
                                int contenido_prod = Integer.parseInt(prod.getContenido());
                                compra_en_pqt = pedidoDetalleOriginal.getCantidad() / contenido_prod;
                            }

                            if (compra_en_pqt >= desde) {
                                cantidadBonificada = (int) (compra_en_pqt / porcada)/**(int)rango*/;
                            } else {
                                cantidadBonificada = 0;
                            }
                        } else if (promUnidad.equals("IMP")) {
                            //la comparacion debe ser por cantidad en soles
                            double compra_en_soles = pedidoDetalleOriginal.getPrecioNeto();

                            if (compra_en_soles >= (double) desde) {
                                cantidadBonificada = (int) (compra_en_soles / (double) porcada)/**(int)rango*/;
                            } else {
                                cantidadBonificada = 0;
                            }
                        } else if (promUnidad.equals("KGM")) {
                            //la comparacion debe ser por cantidad en peso
                            double compra_en_peso = pedidoDetalleOriginal.getPesoNeto();

                            if (compra_en_peso >= (double) desde) {
                                cantidadBonificada = (int) (compra_en_peso / (double) porcada)/**(int)rango*/;
                            } else {
                                cantidadBonificada = 0;
                            }
                        } else {
                            continue;
                        }


                        if (cantidadBonificada > 0) {
                            Log.i(TAG, "cantidadBonificada > 0 " + cantidadBonificada + " de la promocion " + itemPromocion.getIdPromocion() + " agregando a listaPromocionesGeneradasDelProducto");
                        }

                        HashMap<String, Object> map = new HashMap<>();
                        map.put("idPromocion", itemPromocion.getIdPromocion());
                        map.put("itemPromocion", itemPromocion.getUnidad());
                        map.put("cantidad", cantidadBonificada);
                        map.put("idProducto", prod.getIdProducto());
                        map.put("malla", itemPromocion.getMalla());
                        map.put("itemPedido", pedidoDetalleOriginal.getItem());//este campo no debería ser tomando en cuenta por el HashSet(eliminar repetidos) ya que con este campo todos los valores se vuelven diferencites, cuando en realidad no lo es. Sin embargo no se puede quitar este campo porque es importante para calcular despues, es por eso que se eliminan los repetidos con un for comparando solo los campos que se requieren
                        listaBonificacionesDelProducto.add(map);
                    }
                }
            }


            /*Validamos si las bonificaciones generados por el producto son mas de uno, a fin de darle prioridad a las que son desgnadas al cliente.*/
            if (!listaBonificacionesDelProducto.isEmpty()) {
                if (listaBonificacionesDelProducto.size() > 1) {
                    Log.i(TAG, "listaBonificacionesDelProducto hay mas de una bonificacion generada por el producto " + pedidoDetalleOriginal.getIdProducto());
                    ArrayList<HashMap<String, Object>> tempListaCantidadesBonificadas = new ArrayList<>();//Lista temporal para poder analizar
                    ArrayList<PromBonificacionModel> tempListaPromocionesGeneradas = new ArrayList<>();//Lista temporal para poder analizar

                    for (int i = 0; i < listaPromocionesGeneradasDelProducto.size(); i++) {
                        //if (listaPromocionesGeneradasDelProducto.get(i).getPorCliente() == 1){
                        tempListaCantidadesBonificadas.add(listaBonificacionesDelProducto.get(i));
                        tempListaPromocionesGeneradas.add(listaPromocionesGeneradasDelProducto.get(i));
                        Log.i(TAG, "agregando " + gson.toJson(listaBonificacionesDelProducto.get(i)) + " como promocion PorCliente");
                        //}
                    }

                    //Verificar si la lista de listaCantidadesBonificadas está vacía(si no hay promociones PorCliente), basta que exista una promocion PorCliente, ya no se tomarán en cuenta las bonificaciones generadas de forma general
                    if (tempListaCantidadesBonificadas.isEmpty()) {//Si la lista temporal está vacía, entonces significa que no hay ProCliente y se toma todas las bonificaciones generadas
                        Log.i(TAG, "No hay promociones filtrados PorCliente=1, agregando todas las bonificaciones generadas...");
                        tempListaCantidadesBonificadas.addAll(listaBonificacionesDelProducto);
                        tempListaPromocionesGeneradas.addAll(listaPromocionesGeneradasDelProducto);
                    }

                    //Luego de obtener la lista temporal de las cantidades bonificadas ya sea PorCliente o todas las generales, se agrega la lista temporal a la lista final
                    listaFinalCantidadesBonificadas.addAll(tempListaCantidadesBonificadas);
                    listaFinalPromocionesGeneradas.addAll(tempListaPromocionesGeneradas);
                } else {
                    //Si la lista solo tiene un elemento, no hay nada que analizar, asi que lo tomamos
                    listaFinalCantidadesBonificadas.addAll(listaBonificacionesDelProducto);
                    listaFinalPromocionesGeneradas.addAll(listaPromocionesGeneradasDelProducto);
                }
            }


        }


        /*Elminar repetidos con For, no se puede usar el HashSet porque no queremos tomar en cuenta el atributo itemPedido al momento de eliminar repetidos*/
        ArrayList<HashMap<String, Object>> newListaCantidadesBonificadas = new ArrayList<>();
        for (HashMap<String, Object> item : listaFinalCantidadesBonificadas) {
            boolean isInList = false;
            for (HashMap<String, Object> itemUnico : newListaCantidadesBonificadas) {
                if ((int) item.get("idPromocion") == (int) itemUnico.get("idPromocion") &&
                        item.get("itemPromocion").equals(itemUnico.get("itemPromocion")) &&
                        (int) item.get("cantidad") == (int) itemUnico.get("cantidad") &&
                        item.get("idProducto").toString().equals(itemUnico.get("idProducto").toString())) {
                    //Si el item es totalmente igual(por los campos igualados) a alguno de los items dentro de este for, entonces se cambia el flag
                    isInList = true;
                }
            }
            if (!isInList)
                newListaCantidadesBonificadas.add(item);
        }
        listaFinalCantidadesBonificadas.clear();//Limpiamos la lista
        listaFinalCantidadesBonificadas.addAll(newListaCantidadesBonificadas);//Agregamos los elementos sin repetir
        Log.d(TAG, "lista CantidadesBonificadas Sin Repetir:\n" + gson.toJson(listaFinalCantidadesBonificadas));


        ArrayList<PedidoDetalleModel> listaProductosBonificados = new ArrayList<>();
        String productosNulos = "";

        //Primer for es sólo para actualizar las cantidades luego de ser multiplicadas por la compra (en caso se requiera)
        String malla = "";
        for (int i = 0; i < listaFinalCantidadesBonificadas.size(); i++) {
            int idPromocionx = (int) listaFinalCantidadesBonificadas.get(i).get("idPromocion");
            int cantidadx = (int) listaFinalCantidadesBonificadas.get(i).get("cantidad");
            int itemPedidox = (int) listaFinalCantidadesBonificadas.get(i).get("itemPedido");
            String itemPromocion = (String) listaFinalCantidadesBonificadas.get(i).get("itemPromocion");
            String idProductox = listaFinalCantidadesBonificadas.get(i).get("idProducto").toString();
            malla = listaFinalCantidadesBonificadas.get(i).get("malla").toString();
            boolean esElMayor = true;

            for (int j = 0; j < listaFinalCantidadesBonificadas.size(); j++) {
                if (j != i) {
                    /*todo Si son de la misma promocion, y con la misma salida quiere decir que son escalables y se debe elegir el mayor.*/
                    if (idPromocionx == (int) listaFinalCantidadesBonificadas.get(j).get("idPromocion") && idProductox.equals(listaFinalCantidadesBonificadas.get(j).get("idProducto").toString())) {
                        Log.w(TAG, "if " + cantidadx + " > " + (int) listaFinalCantidadesBonificadas.get(j).get("cantidad"));
                        if (cantidadx > (int) listaFinalCantidadesBonificadas.get(j).get("cantidad")) {
                            Log.w(TAG, "true");
                            esElMayor = true;
                            malla = listaFinalCantidadesBonificadas.get(j).get("malla").toString();
                        } else {
                            Log.w(TAG, "false");
                            esElMayor = false;
                            //Basta que no sea mayor que alguno para descartarlo como el Mayor
                            break;//El break rompe el for
                        }
                    }
                    /*todo Si no son de la misma salida, quiere decir que son varias bonificaciones, será tomado como mayor y será agregado a lista de productos bonificados.*/
                }
            }

            if (esElMayor) {

                Log.e(TAG, "El producto " + idProductox + " es el mayor en la posicion " + i + " con cantidad " + cantidadx);
                ProductoModel productoModel = daoProducto.getProducto(idProductox);
                if (productoModel != null) {
                    PedidoDetalleModel pedidoDetalleBonificacion = new PedidoDetalleModel();
                    pedidoDetalleBonificacion.setNumeroPedido(numeroPedido);
                    pedidoDetalleBonificacion.setIdProducto(idProductox);
                    pedidoDetalleBonificacion.setPrecioBruto(0.0);
                    pedidoDetalleBonificacion.setPrecioNeto(0.0);
                    pedidoDetalleBonificacion.setCantidad(cantidadx);
                    pedidoDetalleBonificacion.setPesoNeto(productoModel.getPeso() * cantidadx);
                    pedidoDetalleBonificacion.setIdUnidadMedida(itemPromocion);
                    pedidoDetalleBonificacion.setIdPoliticaPrecio("0");
                    pedidoDetalleBonificacion.setPercepcion(0.0);
                    pedidoDetalleBonificacion.setISC(0.0);
                    pedidoDetalleBonificacion.setMalla(malla);
                    pedidoDetalleBonificacion.setTipoProducto(ProductoModel.TIPO_BONIFICACION);//Producto tipo [V]venta [B]bonificacion
                    pedidoDetalleBonificacion.setIdPromocion(idPromocionx);
                    listaProductosBonificados.add(pedidoDetalleBonificacion);
                } else {
                    //Se concatenan los codigos de los productos bonificados que no se encuentren. Para mostrar un mensaje después
                    productosNulos += idProductox + ",";
                }
            }

        }
        Log.d(TAG, "lista CantidadesBonificadas Sin Repetir luego de update cantidadx:\n" + gson.toJson(listaFinalCantidadesBonificadas));

        //ejecutamos las acciones
        ArrayList<PedidoDetalleModel> listaProductosBonificados2 = new ArrayList<>();
        for (PedidoDetalleModel productoB : listaProductosBonificados) {
            ArrayList<PedidoDetalleModel> arr = daoBonificacion.getAcciones(productoB);
            for (PedidoDetalleModel pdm : arr) {
                if (productoB.getCantidad() != 0) {
                    listaProductosBonificados2.add(pdm);
                }
            }
        }

        //Registramos todos los productos bonificados al pedido
        for (PedidoDetalleModel productoB : listaProductosBonificados2) {
            if (productoB.getCantidad() == 0) {
                continue;
            }
            int cantidadRegistrada = productoEstaRegistrado(productoB.getIdProducto(), productoB.getTipoProducto(), productoB.getMalla(), productoB.getIdUnidadMedida());
            if (cantidadRegistrada != -1) {
                //Si retorna distinto a -1 quiere decir que si está registrado, se debe tomar su cantidad y sumarle a la nueva cantidad
                productoB.setCantidad(productoB.getCantidad() + cantidadRegistrada);
                //daoPedido.modificarItemDetallePedido(productoB);
                daoPedido.modificarItemDetallePedido2(productoB);
            } else {
                daoPedido.agregarItemPedidoDetalle(productoB);
            }
        }

        ArrayList<PedidoDetalleModel> despues = pedidoDetalleFragment.getListaProductos();

        //Actualizar la lista detalle del pedido
        pedidoDetalleFragment.mostrarListaProductos();


    }

    private ArrayList<PedidoDetalleModel> calcularBonificaciones3() {

        String idVendedor = ventas360App.getIdVendedor();
        String numeroPedido = getNumeroPedidoFromFragment(); //Obtenecemos el numeroPedido

        //cuando se edita pedido las bonificaciones previas no se analizan
        ArrayList<PedidoDetalleModel> lista = daoPedido.getListaProductoPedido(numeroPedido);//Obtenemos todo el detalle del pedido
        ArrayList<PedidoDetalleModel> pedidoDetalle = new ArrayList<>();
        for (PedidoDetalleModel item : lista) {
            if (item.getTipoProducto().equals("B")) {
                continue;
            }
            pedidoDetalle.add(item);
        }

        //PoliticaPrecioModel dw = daoCliente.getPoliticaPrecio(idCliente);
        //ArrayList<RutaXModuloModel> e3 = daoCliente.getRutaXCliente(idCliente);
        //MarcaModel vf = daoCliente.getMarca("741");


        HashMap<GrupoPromocionBon, ArrayList<PedidoDetalleProductoModel>> listaBonificaciones = new HashMap<GrupoPromocionBon, ArrayList<PedidoDetalleProductoModel>>();
        ArrayList<PromBonificacionModel> promocionesValidas = daoBonificacion.getPromocionesValidas2(idCliente, idVendedor, numeroPedido);
        for (PromBonificacionModel itemPromocion : promocionesValidas) {
            ArrayList<ProductoModel> productosPromocion = daoBonificacion.getProductosPromocion(itemPromocion.getIdGrupo());//Obtenemos todo el detalle del pedido
            for (ProductoModel prod : productosPromocion) {
                for (PedidoDetalleModel pedidoDetalleOriginal : pedidoDetalle) {
                    if (prod.getIdProducto().equals(pedidoDetalleOriginal.getIdProducto())) {
                        if (!itemPromocion.getCondicion().isEmpty()) {
                            //comparar lineas
                            String mm = "0";
                            String[] politicas = null;
                            int i1 = -1;
                            if (itemPromocion.getCondicion().contains("LISTA=")) {
                                mm = itemPromocion.getCondicion().substring(6, itemPromocion.getCondicion().length());
                                PoliticaPrecioModel politicaPrecioCliente = daoCliente.getPoliticaPrecio(idCliente);
                                i1 = Integer.valueOf(politicaPrecioCliente.getIdPoliticaPrecio());
                            } else if (itemPromocion.getCondicion().contains("LISTA IN")) {
                                mm = "";
                                String tempList = itemPromocion.getCondicion().substring(10, itemPromocion.getCondicion().length() - 1);
                                tempList = tempList.replace("'", "");
                                politicas = tempList.split(",");
                                PoliticaPrecioModel politicaPrecioCliente = daoCliente.getPoliticaPrecio(idCliente);
                                i1 = Integer.valueOf(politicaPrecioCliente.getIdPoliticaPrecio());
                            } else if (itemPromocion.getCondicion().contains("RUTA=")) {
                                mm = itemPromocion.getCondicion().substring(5, itemPromocion.getCondicion().length());
                                ArrayList<RutaXModuloModel> rutas = daoCliente.getRutaXCliente(idCliente);
                                for (RutaXModuloModel ruta : rutas) {
                                    i1 = Integer.valueOf(ruta.getIdRuta());
                                    break;
                                }
                            } else if (itemPromocion.getCondicion().contains("MARCA=")) {
                                mm = itemPromocion.getCondicion().substring(6, itemPromocion.getCondicion().length());
                                MarcaModel marcaModel = daoCliente.getMarca(mm);
                                if (marcaModel == null) {
                                    continue;
                                }
                                i1 = Integer.valueOf(marcaModel.getIdMarca());
                            }

                            if (politicas != null && politicas.length > 0) {
                                //si i1 no esta en el array
                                boolean esta = false;
                                for (int jk = 0; jk < politicas.length; jk++) {
                                    int i2 = 0;
                                    try {
                                        i2 = Integer.valueOf(politicas[jk]);
                                    } catch (NumberFormatException nfe) {
                                        continue;
                                    }
                                    if (i1 == i2) {
                                        esta = true;
                                        break;
                                    }
                                }
                                if (!esta) {
                                    continue;
                                }
                            } else {
                                int i2 = 0;
                                try {
                                    i2 = Integer.valueOf(mm);
                                    ;
                                } catch (NumberFormatException nfe) {
                                    continue;
                                }
                                if (i1 != i2) {
                                    continue;
                                }
                            }

                        }
                        GrupoPromocionBon temp = new GrupoPromocionBon(itemPromocion.getIdGrupo(), itemPromocion.getIdPromocion());
                        if (listaBonificaciones.get(temp) == null) {
                            listaBonificaciones.put(temp, new ArrayList<PedidoDetalleProductoModel>());
                        }
                        PedidoDetalleProductoModel pdpm = new PedidoDetalleProductoModel(prod, pedidoDetalleOriginal);
                        if (!listaBonificaciones.get(temp).contains(pdpm)) {
                            listaBonificaciones.get(temp).add(pdpm);
                        }
                    }
                }
            }
        }


        Log.d(TAG, "Numero de promociones aplicables: " + listaBonificaciones.size());
        ArrayList<PedidoDetalleModel> listaProductosBonificados2 = new ArrayList<>();
        for (Map.Entry<GrupoPromocionBon, ArrayList<PedidoDetalleProductoModel>> entry : listaBonificaciones.entrySet()) //se asume que a cada promocion hay un grupo unico
        {
            GrupoPromocionBon gpb = entry.getKey();
            ArrayList<PedidoDetalleProductoModel> arr = entry.getValue();
            int idPromocion = gpb.getIdPromocion();
            Log.d(TAG, "Analizando promocion: " + gpb.getIdPromocion() + " grupo= " + gpb.getIdGrupo() + "#prod en grupo= " + arr.size());

            //verifica para el caso de multiples promociones
            PromBonificacionModel promocion = null;
            for (PromBonificacionModel itemPromocion : promocionesValidas) {
                if (itemPromocion.getIdPromocion() == idPromocion) {
                    promocion = itemPromocion;
                    break;
                }
            }
            if (promocion == null) continue;

            float desde = Float.valueOf(promocion.getDesde());
            float hasta = Float.valueOf(promocion.getHasta());
            float porcada = promocion.getPorcada();
            int rango = promocion.getIdRango();
            Log.d(TAG, "Promocion: " + gpb.getIdPromocion() + " desde= " + desde + " hasta= " + hasta + " por cada=" + porcada + "rango= " + rango);

            //multiples grupos
            ArrayList<DAOPromo3F> grupos3F = daoBonificacion.getCondicionPromocionxGrupo(idPromocion);
            Log.d(TAG, "Promocion: " + gpb.getIdPromocion() + " numero de grupos3F= " + grupos3F.size());
            int cantidadBonificada = 0;
            if (grupos3F.size() > 1) {
                //al menos un producto de cada grupo debe comprarse
                boolean eall = true;
                for (DAOPromo3F item : grupos3F) {
                    //la condicional desde se suma para generar el id, se asume que la unidad es la misma en cada promo3f grupo
                    if (item == null) {
                        continue;
                    }

                    boolean al_menos_un_prod = true;
                    ArrayList<DAOGrupoPromocion> grupos2F = daoBonificacion.getMinimosPromocionxGrupo(item.getIDGRUPO());
                    for (DAOGrupoPromocion item0 : grupos2F) {
                        boolean esta_incluido = false;
                        for (PedidoDetalleModel pedidoDetalleOriginal : pedidoDetalle) {
                            if (Integer.valueOf(pedidoDetalleOriginal.getIdProducto()) == item0.getARTICULO()) {
                                esta_incluido = true;
                                break;
                            }
                        }
                        if (item0.getUNIDADES() == 0) { // no es necesario que todos del grupo se compren, al menos uno
                            if (esta_incluido) {
                                al_menos_un_prod = esta_incluido;
                                break;
                            } else {
                                al_menos_un_prod = false;
                            }
                        } else {
                            al_menos_un_prod = al_menos_un_prod && esta_incluido;
                        }
                    }
                    eall = eall && al_menos_un_prod;

                }
                if (!eall) {
                    //no bonifica
                    continue;
                }

                //posiblemente bonifica, filtrar si no superan el minimo
                ArrayList<Integer> weight_groupbon = new ArrayList<>();
                for (DAOPromo3F item : grupos3F) {
                    int subCantidadBonificada = getCantidadBonificarGeneradaxProducto(item.getIDGRUPO(), item.getUNIDAD(), item.getDESDE(), item.PORCADA, arr, item.getHASTA());
                    if (subCantidadBonificada == 0) //no supero el minimo local en el grupo, no bonifica
                    {
                        cantidadBonificada = 0;
                        weight_groupbon.clear();
                        break;
                    }
                    weight_groupbon.add(subCantidadBonificada);
                }
                if (weight_groupbon.size() > 0) {
                    for (int value : weight_groupbon) {
                        cantidadBonificada += value;
                    }
                    /*
                    //minimo
                    int min = 100000000;
                    for(int value: weight_groupbon){
                        if(value < min){
                            min = value;
                        }
                    }
                    cantidadBonificada = min;*/
                }
            } else {
                cantidadBonificada = getCantidadBonificarGeneradaxProducto(promocion.getIdGrupo(), promocion.getUnidad(), desde, porcada, arr, hasta);
            }

            if (cantidadBonificada == 0) {
                continue;
            }

            if (cantidadBonificada > 0) {
                Log.i(TAG, "cantidadBonificada > 0 " + cantidadBonificada + " de la promocion " + promocion.getIdPromocion() + " agregando a listaPromocionesGeneradasDelProducto");
            }

            //ejecutamos las acciones
            setProductosBonifica(listaProductosBonificados2, promocion, cantidadBonificada, numeroPedido);
        }

        return listaProductosBonificados2;
    }

    void setProductosBonifica(ArrayList<PedidoDetalleModel> listaProductosBonificados2, PromBonificacionModel promocion, int cantidadBonificada, String numeroPedido) {
        ArrayList<PedidoDetalleModel> arr2 = daoBonificacion.getAcciones2(promocion.getIdPromocion());
        for (PedidoDetalleModel pdm : arr2) {
            if (cantidadBonificada != 0) {
                int cantidad_total = cantidadBonificada * pdm.getCantidad();

                pdm.setNumeroPedido(numeroPedido);
                pdm.setPrecioBruto(0.0);
                pdm.setPrecioNeto(0.0);
                pdm.setIdPoliticaPrecio("0");
                pdm.setPercepcion(0.0);
                pdm.setISC(0.0);
                pdm.setMalla(promocion.getMalla());
                pdm.setTipoProducto(ProductoModel.TIPO_BONIFICACION);//Producto tipo [V]venta [B]bonificacion
                pdm.setIdPromocion(promocion.getIdPromocion());
                //convirtiendo und a unidad de manejo si es que sobre pasa
                int factorConversion = daoProducto.getFactorConversion(pdm.getIdProducto());
                //evaluando si es que sobra
                int np = (cantidad_total / factorConversion) * factorConversion;
                int remain = cantidad_total - (cantidad_total / factorConversion) * factorConversion;
                if (remain > 0 && np != 0) {
                    PedidoDetalleModel pdm2 = new PedidoDetalleModel();
                    pdm2.setIdProducto(pdm.getIdProducto());
                    pdm2.setNumeroPedido(numeroPedido);
                    pdm2.setPrecioBruto(0.0);
                    pdm2.setPrecioNeto(0.0);
                    pdm2.setIdPoliticaPrecio("0");
                    pdm2.setPercepcion(0.0);
                    pdm2.setISC(0.0);
                    pdm2.setMalla(promocion.getMalla());
                    pdm2.setTipoProducto(ProductoModel.TIPO_BONIFICACION);//Producto tipo [V]venta [B]bonificacion
                    pdm2.setIdPromocion(promocion.getIdPromocion());
                    pdm2.setCantidad(remain);
                    pdm2.setIdUnidadMedida("UND");
                    listaProductosBonificados2.add(pdm2);
                }

                if (cantidad_total / factorConversion > 0) {
                    String um = daoProducto.getUnidadManejo(pdm.getIdProducto());
                    int cm = cantidad_total / factorConversion;
                    pdm.setCantidad(cm);
                    pdm.setIdUnidadMedida(um);
                } else {
                    pdm.setCantidad(cantidad_total);
                }

                listaProductosBonificados2.add(pdm);
            }
        }
    }

    /**
     * Esta funcion evalua el stock de los productos a bonificar, si hay stock agregar a las lista de productos a enviar al servidor
     * si no hay stock, segun el tipo de cuenta notifica al usuario sobre el estado de la bonificacion de un producto
     *
     * @param listaPosibleProductosBonifica
     * @param soapManager
     */
    void evalStockProductosBonificados(ArrayList<PedidoDetalleModel> listaPosibleProductosBonifica, SoapManager soapManager) {
        ArrayList<PedidoDetalleModel> listaProductosBonificar = new ArrayList<>();
        mListaProductosNoBonifica = new ArrayList<>();

        //para cada producto bonificado obtiene el kardex
        String fkProducto = TablesHelper.Kardex.FKProducto;
        String stockInicial = TablesHelper.Kardex.stockInicial;
        String stockPedido = TablesHelper.Kardex.stockPedido;
        String stockDespachado = TablesHelper.Kardex.stockDespachado;
        Log.d(TAG, "mira le producto");

        Log.d(TAG, listaProductosBonificar.toString());


        Log.d(TAG, "------- fin ---------");
        //para cada producto que bonifica
        for (PedidoDetalleModel pdm : listaPosibleProductosBonifica) {
            JSONArray stocks = null;
            try {
                ArrayList<PedidoDetalleModel> pedidoEnviar = pedidoDetalleFragment.getListaProductos();

                stocks = soapManager.obtenerStockProductox2(TablesHelper.Producto.ObtenerStockLinea, pdm.getIdProducto(), numeroPedido);
                for (int i = 0; i < stocks.length(); i++) {
                    JSONObject stock = stocks.getJSONObject(i);
                    String idProd = stock.getString(fkProducto).trim();
                    int sInicial = Integer.parseInt(stock.getString(stockInicial).trim());
                    int sPedido = Integer.parseInt(stock.getString(stockPedido).trim());
                    int sDespachado = Integer.parseInt(stock.getString(stockDespachado).trim());

                    if (idProd.equals(pdm.getIdProducto())) {
                        int disponible = sInicial - sPedido - sDespachado; //en UND
                        //obtenermos la cantidad que pide del producto, puede ser multiples veces
                        int pide = getCantidadPedir(pdm, pedidoEnviar);

                        if (pide <= disponible) {
                            //bonifica
                            listaProductosBonificar.add(pdm);
                        } else {
                            //no bonifica
                            mListaProductosNoBonifica.add(pdm);
                        }
                        break;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


        //Registramos todos los productos bonificados al pedido
        for (PedidoDetalleModel productoB : listaProductosBonificar) {
            if (productoB.getCantidad() == 0) {
                continue;
            }
            int cantidadRegistrada = productoEstaRegistrado(productoB.getIdProducto(), productoB.getTipoProducto(), productoB.getMalla(), productoB.getIdUnidadMedida());
            if (cantidadRegistrada != -1) {
                //Si retorna distinto a -1 quiere decir que si está registrado, se debe tomar su cantidad y sumarle a la nueva cantidad
                //en el caso en que dos productos comprados generen la misma bonificacion

                //evaluar kardex
                productoB.setCantidad(productoB.getCantidad() + cantidadRegistrada);
                daoPedido.modificarItemDetallePedido2(productoB);
            } else {

                //evaluar kardex
                daoPedido.agregarItemPedidoDetalle(productoB);
            }
        }

        //lista de productos que no bonifica
        if (mListaProductosNoBonifica != null && mListaProductosNoBonifica.size() > 0) {
            if (ventas360App.getTipoVendedor().equals("T")) { //autoventa
                //no genera bonificacion (no envia el dato al servidor) y no muestra producto
            } else {
                for (PedidoDetalleModel productoB : mListaProductosNoBonifica) {
                    if (productoB.getCantidad() == 0) {
                        continue;
                    }
                    int cantidadRegistrada = productoEstaRegistrado(productoB.getIdProducto(), productoB.getTipoProducto(), productoB.getMalla(), productoB.getIdUnidadMedida());
                    if (cantidadRegistrada != -1) {
                        //Si retorna distinto a -1 quiere decir que si está registrado, se debe tomar su cantidad y sumarle a la nueva cantidad
                        //en el caso en que dos productos comprados generen la misma bonificacion

                        if (ventas360App.getSettings_preventaEnLinea()) { //preventa en linea
                            //no genera bonificacion y mensaje se aplica luego de enviado el pedido
                            continue;
                        } else { //solo preventa
                            //genera bonificacion (envia el dato al servidor) y normal
                            productoB.setCantidad(productoB.getCantidad() + cantidadRegistrada);
                            daoPedido.modificarItemDetallePedido2(productoB);
                        }
                    } else {

                        if (ventas360App.getSettings_preventaEnLinea()) { //preventa en linea
                            //genera bonificacion (envia el dato al servidor) y mensaje sin inventario
                            //no genera bonificacion y mensaje se aplica luego de enviado el pedido
                            continue;
                        } else { //solo preventa
                            //genera bonificacion (envia el dato al servidor) y normal
                            daoPedido.agregarItemPedidoDetalle(productoB);
                        }

                    }
                }
            }
        }


    }

    int getCantidadPedir(PedidoDetalleModel pdm, ArrayList<PedidoDetalleModel> pedidoEnviar) {
        int pide = 0;
        if (pdm.getIdUnidadMedida().equals("UND")) {
            pide = pdm.getCantidad();
        } else {
            pide = pdm.getCantidad() * pdm.getFactorConversion();
        }

        for (PedidoDetalleModel item : pedidoEnviar) {
            if (item.getIdProducto().equals(pdm.getIdProducto())) {
                if (item.getIdUnidadMedida().equals("UND")) {
                    pide += item.getCantidad();
                } else {
                    pide += item.getCantidad() * item.getFactorConversion();
                }
                break;
            }
        }


        return pide;
    }

    int getCantidadBonificarGeneradaxProducto(int idGrupo, String promUnidad, float desde, float porcada, ArrayList<PedidoDetalleProductoModel> arr, float hasta) {

        //si no estan todos los productos del grupo con unidad diferente a null, bonifica cero
        ArrayList<DAOGrupoPromocion> grupos2F = daoBonificacion.getMinimosPromocionxGrupo(idGrupo);
        Log.d(TAG, "idGrupo: " + idGrupo + " numero de grupos2F= " + grupos2F.size());
        boolean existen = true;
        for (DAOGrupoPromocion item : grupos2F) {
            if (item.getUNIDADES() == 0) {
                continue;
            }
            boolean coincide = false;
            for (PedidoDetalleProductoModel pdpm : arr) {
                ProductoModel prod = pdpm.getProductoModel();
                if (Integer.valueOf(prod.getIdProducto()) == item.getARTICULO()) {
                    coincide = true;
                    break;
                }
            }
            existen = coincide && existen;
        }

        Log.d(TAG, "idGrupo: " + idGrupo + " existen (concide idproducto pedido con alguno de idgrupo)= " + existen);
        if (!existen) {
            //no bonifica
            return 0;
        }

        ArrayList<Integer> caso_especial = new ArrayList<>();
        double compra = getCompra(caso_especial, promUnidad, arr);
        Log.d(TAG, "getCantidadBonificarGeneradaxProducto: compra=" + compra);
        //obtenidos las promociones que aplican al producto, se evalua si cumple las condiciones y la cantidad a bonificar|
        int cantidadBonificada = 0;
        //BONIFICACION por unidad, en todos la premiacion es en unidades
        if (caso_especial.size() > 0) {
            cantidadBonificada = (int) compra;
        } else {
            cantidadBonificada = getNormalBonificacionxProducto(promUnidad, compra, desde, porcada, hasta);
        }

        Log.d(TAG, "getCantidadBonificarGeneradaxProducto: cantidadBonificada=" + cantidadBonificada);
        return cantidadBonificada;
    }

    //Realiza la validacion de las promociones (compara desde y hasta) cantidad de promociones a brindar

    private int getNormalBonificacionxProducto(String promUnidad, double compra, double desde, double porcada, double hasta) {
        Log.d(TAG, "getNormalBonificacionxProducto: promUnidad=" + promUnidad + " compra= " + compra + " desde= " + desde);
        int cantidadBonificada = 0;
        if (promUnidad.equals("UND")) {
            if (hasta == 0) {
                //Escenario 1 (validar solo desde)
                if (compra >= (double) desde) {
                    cantidadBonificada = (int) (compra / (double) porcada)/**(int)rango*/;
                } else {
                    cantidadBonificada = 0;
                }
            } else {
                //Escenario 2 (validar desde y hasta)
                if (compra >= (double) desde & compra <= (double) hasta) {
                    cantidadBonificada = (int) (compra / (double) porcada)/**(int)rango*/;
                } else {
                    cantidadBonificada = 0;
                }
            }
        } else if (promUnidad.equals("PQT")) {
            if (hasta == 0) {
                //Escenario 1 (validar solo desde)
                if (compra >= (double) desde) {
                    cantidadBonificada = (int) (compra / (double) porcada)/**(int)rango*/;
                } else {
                    cantidadBonificada = 0;
                }
            } else {
                //Escenario 2 (validar desde y hasta)
                if (compra >= (double) desde & compra <= (double) hasta) {
                    cantidadBonificada = (int) (compra / (double) porcada)/**(int)rango*/;
                } else {
                    cantidadBonificada = 0;
                }
            }
        } else if (promUnidad.equals("IMP")) {
            if (hasta == 0) {
                //Escenario 1 (validar solo desde)
                if (compra >= (double) desde) {
                    cantidadBonificada = (int) (compra / (double) porcada)/**(int)rango*/;
                } else {
                    cantidadBonificada = 0;
                }
            } else {
                //Escenario 2 (validar desde y hasta)
                if (compra >= (double) desde & compra <= (double) hasta) {
                    cantidadBonificada = (int) (compra / (double) porcada)/**(int)rango*/;
                } else {
                    cantidadBonificada = 0;
                }
            }
        } else if (promUnidad.equals("KGM")) {

            if (hasta == 0) {
                //Escenario 1 (validar solo desde)
                if (compra >= (double) desde) {
                    cantidadBonificada = (int) (compra / (double) porcada)/**(int)rango*/;
                } else {
                    cantidadBonificada = 0;
                }
            } else {
                //Escenario 2 (validar desde y hasta)
                if (compra >= (double) desde & compra <= (double) hasta) {
                    cantidadBonificada = (int) (compra / (double) porcada)/**(int)rango*/;
                } else {
                    cantidadBonificada = 0;
                }
            }
        } else {
            cantidadBonificada = 0;
        }
        return cantidadBonificada;
    }


    private double getCompra(ArrayList<Integer> caso_especial, String promUnidad, ArrayList<PedidoDetalleProductoModel> arr) {
        double compra = 0;
        for (PedidoDetalleProductoModel pdpm : arr) {
            PedidoDetalleModel pedidoDetalleOriginal = pdpm.getDetalleOriginal();
            ProductoModel prod = pdpm.getProductoModel();
            if (promUnidad.equals("UND")) {
                //la comparacion debe ser por unidades, pedidoDetalleOriginal transformando a und
                int compra_en_und = pedidoDetalleOriginal.getCantidad();
                if (!pedidoDetalleOriginal.getIdUnidadMedida().equals("UND")) {
                    int contenido_prod = Integer.parseInt(prod.getContenido());
                    compra_en_und = pedidoDetalleOriginal.getCantidad() * contenido_prod;
                }

                if (prod.getProm_grupo_unidades() > 0) {
                    if (compra_en_und < prod.getProm_grupo_unidades()) {
                        //no bonifica
                        compra = 0;
                        break;
                    } else {
                        caso_especial.add(compra_en_und / prod.getProm_grupo_unidades());
                    }
                } else {
                    compra += compra_en_und;
                }
            } else if (promUnidad.equals("PQT")) {
                //la comparacion debe ser por unidad de manejo, pedidoDetalleOriginal transformando a unidad de manejo
                int compra_en_pqt = pedidoDetalleOriginal.getCantidad();
                if (pedidoDetalleOriginal.getIdUnidadMedida().equals("UND")) {
                    int contenido_prod = Integer.parseInt(prod.getContenido());
                    compra_en_pqt = pedidoDetalleOriginal.getCantidad() / contenido_prod;
                }

                if (prod.getProm_grupo_unidades() > 0) {
                    if (compra_en_pqt < prod.getProm_grupo_unidades()) {
                        //no bonifica
                        compra = 0;
                        break;
                    } else {
                        caso_especial.add(compra_en_pqt / prod.getProm_grupo_unidades());
                    }
                } else {
                    compra += compra_en_pqt;
                }
            } else if (promUnidad.equals("IMP")) {
                //la comparacion debe ser por cantidad en soles
                if (prod.getProm_grupo_unidades() > 0) {
                    if (pedidoDetalleOriginal.getPrecioNeto() < prod.getProm_grupo_unidades()) {
                        //no bonifica
                        compra = 0;
                        break;
                    } else {
                        caso_especial.add((int) (pedidoDetalleOriginal.getPrecioNeto() / prod.getProm_grupo_unidades()));
                    }
                } else {
                    compra += pedidoDetalleOriginal.getPrecioNeto();
                }
            } else if (promUnidad.equals("KGM")) {
                //la comparacion debe ser por cantidad en peso
                if (prod.getProm_grupo_unidades() > 0) {
                    if (pedidoDetalleOriginal.getPesoNeto() < prod.getProm_grupo_unidades()) {
                        //no bonifica
                        compra = 0;
                        break;
                    } else {
                        caso_especial.add((int) (pedidoDetalleOriginal.getPesoNeto() / prod.getProm_grupo_unidades()));
                    }
                } else {
                    compra += pedidoDetalleOriginal.getPesoNeto();
                }
            }
        }

        if (caso_especial.size() > 0) {
            //minimo
            int min = 100000000;
            for (int value : caso_especial) {
                if (value < min) {
                    min = value;
                }
            }
            compra = min;
        }

        return compra;
    }

    public String getNumeroPedidoFromFragment() {
        return pedidoCabeceraFragment.getNumeroPedido();
    }

    public String getIdClienteFromActivity() {
        return this.idCliente;
    }

    public String getNumeroPedidoFromActivity() {
        return this.numeroPedido;
    }

    public int getACCION_PEDIDO() {
        return ACCION_PEDIDO;
    }

    /**
     * @param idProducto
     * @param tipoProducto
     * @return Retorna la cantidad del producto, <p>Retorna -1 Si el producto no está registrado</p>
     */
    int productoEstaRegistrado(String idProducto, String tipoProducto, String malla, String idUnidadMedida) {
        ArrayList<PedidoDetalleModel> listaProductos = pedidoDetalleFragment.getListaProductos();
        for (PedidoDetalleModel producto : listaProductos) {
            if ((producto.getIdProducto().equals(idProducto) && producto.getTipoProducto().equals(tipoProducto))
                    && (producto.getMalla().equals(malla))
                    && (producto.getIdUnidadMedida().equals(idUnidadMedida))
            ) {
                return producto.getCantidad();
            }
        }
        return -1;
    }

    public int getEstadoBateria() {
        try {
            IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
            Intent bateryStatus = registerReceiver(null, ifilter);

            int level = bateryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            int scale = bateryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
            int porcentaje = (100 * level) / scale;
            return porcentaje;
        } catch (Exception ex) {
            ex.printStackTrace();
            return -1;
        }
    }

    private class SectionsPagerAdapter extends FragmentPagerAdapter {
        //indican la posición de la página o tab, siempre debe iniciar en cero
        final int PAGE_PEDIDO = 0;
        final int PAGE_PRODUCTOS = 1;

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case PAGE_PEDIDO:
                    return pedidoCabeceraFragment;
                case PAGE_PRODUCTOS:
                    return pedidoDetalleFragment;
                default:
                    return pedidoCabeceraFragment;
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
                case PAGE_PEDIDO:
                    return "Gestion";
                case PAGE_PRODUCTOS:
                    return "Productos";
            }
            return null;
        }
    }

    void sincronizarBonificaciones() {
        new DownloadBonificaciones().execute();
    }

    private class DownloadBonificaciones extends AsyncTask<String, Void, Boolean> {
        protected Boolean doInBackground(String... urls) {

            //descargar la version actualizada de bonificaciones
            try {
                SoapManager soap_manager = new SoapManager(getApplicationContext());
                soap_manager.obtenerBonificaionesJSON(TablesHelper.MGRUP1F.Sincronizar, TablesHelper.MGRUP1F.Table);
                soap_manager.obtenerBonificaionesJSON(TablesHelper.MGRUP2F.Sincronizar, TablesHelper.MGRUP2F.Table);
                soap_manager.obtenerBonificaionesJSON(TablesHelper.MPROMO1F.Sincronizar, TablesHelper.MPROMO1F.Table);
                soap_manager.obtenerBonificaionesJSON(TablesHelper.MPROMO2F.Sincronizar, TablesHelper.MPROMO2F.Table);
                soap_manager.obtenerBonificaionesJSON(TablesHelper.MPROMO3F.Sincronizar, TablesHelper.MPROMO3F.Table);
                soap_manager.obtenerBonificaionesJSON(TablesHelper.MPROMO4F.Sincronizar, TablesHelper.MPROMO4F.Table);
                soap_manager.obtenerBonificaionesJSON(TablesHelper.MPROMO5F.Sincronizar, TablesHelper.MPROMO5F.Table);
                soap_manager.obtenerBonificaionesJSON(TablesHelper.MPROMO6F.Sincronizar, TablesHelper.MPROMO6F.Table);
            } catch (NoSuchMethodException ex) {
                ex.printStackTrace();
                return false;
            } catch (SocketTimeoutException ex) {
                ex.printStackTrace();
                return false;
            } catch (IOException ex) {
                ex.printStackTrace();
                return false;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
            return true;
        }

        protected void onPostExecute(boolean result) {
            if (result) {
                // Toast.makeText(PedidoActivity.this, "Sincronizado bonificaciones",Toast.LENGTH_SHORT).show();
            } else {
                //Toast.makeText(PedidoActivity.this, "No se pudo sincronizar bonificaciones, Exception",Toast.LENGTH_SHORT);
            }
        }


    }
}
