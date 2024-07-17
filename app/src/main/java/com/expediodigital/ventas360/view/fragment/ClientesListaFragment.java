package com.expediodigital.ventas360.view.fragment;


import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;

import android.provider.Settings;
import androidx.annotation.DrawableRes;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.core.content.ContextCompat;
import androidx.core.view.MenuItemCompat;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.SearchView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.expediodigital.ventas360.DAO.DAOCliente;

import com.expediodigital.ventas360.DAO.DAOConfiguracion;
import com.expediodigital.ventas360.DAO.DAOEncuesta;
import com.expediodigital.ventas360.DAO.DAOPedido;
import com.expediodigital.ventas360.DTO.DTOMotivoNoVenta;
import com.expediodigital.ventas360.DTO.DTOPedido;
import com.expediodigital.ventas360.R;
import com.expediodigital.ventas360.Ventas360App;
import com.expediodigital.ventas360.adapter.RecyclerViewClienteAdapter;
import com.expediodigital.ventas360.interfaces.Darbaja_listener;
import com.expediodigital.ventas360.model.ClienteCoordenadasModel;
import com.expediodigital.ventas360.model.ClienteModel;
import com.expediodigital.ventas360.model.EncuestaDetalleModel;
import com.expediodigital.ventas360.model.JSONModel;
import com.expediodigital.ventas360.model.PedidoCabeceraModel;
import com.expediodigital.ventas360.model.VendedorModel;
import com.expediodigital.ventas360.modulos.DarBajaController;
import com.expediodigital.ventas360.modulos.WhatsappController;
import com.expediodigital.ventas360.quickaction.ActionItem;
import com.expediodigital.ventas360.quickaction.QuickAction;
import com.expediodigital.ventas360.service.GPSTracker;
import com.expediodigital.ventas360.util.MenuItemCustomListener;
import com.expediodigital.ventas360.util.SoapManager;
import com.expediodigital.ventas360.util.TablesHelper;
import com.expediodigital.ventas360.util.Util;
import com.expediodigital.ventas360.view.DetalleClienteActivity;
import com.expediodigital.ventas360.view.EncuestaClienteDialogActivity;
import com.expediodigital.ventas360.view.InfoVendedorActivity;
import com.expediodigital.ventas360.view.NuevoClienteActivity;
import com.expediodigital.ventas360.view.PedidoActivity;
import com.expediodigital.ventas360.view.WhatsappFormActivity;
import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.google.gson.Gson;
import com.google.gson.JsonParseException;

import org.xmlpull.v1.XmlPullParserException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static android.app.Activity.RESULT_OK;

/**
 * A simple {@link Fragment} subclass.
 */
public class ClientesListaFragment extends Fragment implements SearchView.OnQueryTextListener {
    public static final String TAG = "ClientesListaFragment";
    public static final int QUICK_ITEM_VER_FICHA = 1;
    public static final int QUICK_ITEM_TOMAR_PEDIDO = 2;
    public static final int QUICK_ITEM_NO_VENTA = 3;
    public static final int QUICK_GEOLOCALIZAR = 4;
    public static final int QUICK_ITEM_WHATSAPP = 5;
    public static final int QUICK_ITEM_BAJAUSER = 6;

    private static final int REQUEST_CODE_NUEVO_CLIENTE = 1;
    private static final int REQUEST_CODE_TOMAR_PEDIDO = 2;
    private static final int REQUEST_PREMISOS_UBICACION = 3;
    private static final int REQUEST_CODE_UBICACION = 4;
    private static final int REQUEST_ENCUESTA = 5;
    private static final int REQUEST_TOMAR_ENCUESTA = 6;


    private FloatingActionMenu fab_menu;
    private FloatingActionButton fab_nuevoCliente, fab_obtenerClientes;
    private boolean ubicacionClienteLista = false;
    private int contadorUbicacion = 0;


    RecyclerView recycler_clientes;
    DAOCliente daoCliente;
    DAOPedido daoPedido;
    static DAOConfiguracion daoConfiguracion;
    static DAOEncuesta daoEncuesta;
    SoapManager soapManager;
    RecyclerViewClienteAdapter adapter;

    ArrayList<ClienteModel> listaCliente = new ArrayList<>();
    ArrayList<ClienteModel> listaCoordenadasPendientes = new ArrayList<>();
    SearchView searchView;
    //declarar variable
    TextView tv_cantidadTotal;
    TextView tv_cantidadVisitados;
    TextView tv_cantidadPendientes;

    static Ventas360App ventas360App;
    String idVendedor;
    private int numeroCoordenadasPendientes = 0;

    GPSTracker gpsTracker;
    private static String idClienteGeneral = "";
    private static String razonSocialGeneral = "";
    Gson gson;

    static ClientesListaFragment fragment;
    WhatsappController whatsappController;
    DarBajaController darBajaController;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        View view = inflater.inflate(R.layout.fragment_clientes_lista, container, false);
        setHasOptionsMenu(true);
        Util.actualizarToolBar(getString(R.string.menu_clientes),false,getActivity());
        fragment = this;
        ventas360App = (Ventas360App) getActivity().getApplicationContext();
        daoCliente = new DAOCliente(getActivity());
        daoPedido = new DAOPedido(getActivity());
        daoConfiguracion = new DAOConfiguracion(getActivity());
        daoEncuesta = new DAOEncuesta(getActivity());
        soapManager = new SoapManager(getActivity());

        idVendedor = ventas360App.getIdVendedor();

        //llamo al layout recyclerview
        recycler_clientes = view.findViewById(R.id.recycler_clientes);
        //data para el fragment - clientes en cardview
        tv_cantidadTotal = view.findViewById(R.id.tv_cantidadTotal);
        tv_cantidadVisitados = view.findViewById(R.id.tv_cantidadVisitados);
        tv_cantidadPendientes = view.findViewById(R.id.tv_cantidadPendientes);

        fab_menu = view.findViewById(R.id.fab_menu);
        fab_menu.setClosedOnTouchOutside(true);
        fab_menu.getMenuIconView().setImageDrawable(ContextCompat.getDrawable(getActivity(),R.drawable.ic_action_agregar));

        fab_nuevoCliente = view.findViewById(R.id.fab_nuevoCliente);
        fab_obtenerClientes = view.findViewById(R.id.fab_obtenerClientes);

        fab_nuevoCliente.setImageDrawable(ContextCompat.getDrawable(getActivity(),R.drawable.ic_action_agregar_white));
        fab_obtenerClientes.setImageDrawable(ContextCompat.getDrawable(getActivity(),R.drawable.ic_action_sincronizar));

        gson = new Gson();

        fab_nuevoCliente.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fab_menu.close(true);
                Intent intent = new Intent(getActivity(), NuevoClienteActivity.class);
                startActivityForResult(intent, REQUEST_CODE_NUEVO_CLIENTE);
            }
        });

        fab_obtenerClientes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fab_menu.close(true);
                // Generar backup antes de la sincronizacion
                Util.backupdDatabase(getActivity());
                new async_obtenerClientes().execute();
            }
        });

        fab_nuevoCliente.setVisibility(View.GONE);//No usar INVISIBLE porque en la librería si el boton ha sido usado se queda VISIBLE

        // creo el adapter y lo enlazo con el cardview
        adapter = new RecyclerViewClienteAdapter(listaCliente, getActivity());
        recycler_clientes.addItemDecoration(new DividerItemDecoration(getActivity(),DividerItemDecoration.VERTICAL));
        recycler_clientes.setAdapter(adapter);
        refreshLista();
        IniciarLocalizador();

        whatsappController = new WhatsappController(this);
        darBajaController = new DarBajaController(this);
        darBajaController.setListener(new Darbaja_listener() {
            @Override
            public void actualizarLista() {
                refreshLista();
            }
        });
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        QuickAction quickAction = new QuickAction(getActivity());
        ActionItem quickItemFicha = new ActionItem(QUICK_ITEM_VER_FICHA, "Ver ficha", ContextCompat.getDrawable(getActivity(),R.drawable.icon_id_card));
        ActionItem quickItemPedido = new ActionItem(QUICK_ITEM_TOMAR_PEDIDO, "Tomar gestion",ContextCompat.getDrawable(getActivity(),R.drawable.icon_document_agregar));
        ActionItem quickItemNoVenta = new ActionItem(QUICK_ITEM_NO_VENTA, "No venta",ContextCompat.getDrawable(getActivity(),R.drawable.icon_hold));
        ActionItem quickGeolocalizar = new ActionItem(QUICK_GEOLOCALIZAR, "Localizar",ContextCompat.getDrawable(getActivity(),R.drawable.icon_geolocalizar));
        ActionItem quickItemWhatsapp = new ActionItem(QUICK_ITEM_WHATSAPP, "Whatsapp", ContextCompat.getDrawable(getActivity(),R.drawable.ic_whatsapp));
        ActionItem quickItemBajaUser = new ActionItem(QUICK_ITEM_BAJAUSER, "Dar de baja", ContextCompat.getDrawable(getActivity(),R.drawable.ic_downgrade));

        quickAction.addActionItem(quickItemFicha);
        quickAction.addActionItem(quickItemPedido);
        quickAction.addActionItem(quickItemNoVenta);
        quickAction.addActionItem(quickGeolocalizar);
        quickAction.addActionItem(quickItemWhatsapp);
        quickAction.addActionItem(quickItemBajaUser);

        //quickAction2 no tendrá motivo de no venta, ya que se mostrará cuando el cliente ya tenga pedidos realizados
        QuickAction quickAction2 = new QuickAction(getActivity());
        quickAction2.addActionItem(quickItemFicha);
        quickAction2.addActionItem(quickItemPedido);
        quickAction2.addActionItem(quickGeolocalizar);
        quickAction2.addActionItem(quickItemWhatsapp);
        quickAction2.addActionItem(quickItemBajaUser);


        quickAction.setOnActionItemClickListener(new QuickAction.OnActionItemClickListener() {
            @Override
            public void onItemClick(QuickAction source, int pos, int actionId) {
                quickItemAccion(actionId);
            }
        });
        quickAction2.setOnActionItemClickListener(new QuickAction.OnActionItemClickListener() {
            @Override
            public void onItemClick(QuickAction source, int pos, int actionId) {
                quickItemAccion(actionId);
            }
        });

        adapter.setQuickAction(quickAction);
        adapter.setQuickAction2(quickAction2);
    }

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_clientes, menu);

        final View menuItemPendientes = menu.findItem(R.id.menu_coordenadas_pendientes).getActionView();
        TextView tv_contador = menuItemPendientes.findViewById(R.id.tv_contador);

        new MenuItemCustomListener(menuItemPendientes, "Pendientes") {
            @Override
            public void onClick(View v) {
                showDialogCoordenadasPendientes();
            }
        }.actualizarTextView(tv_contador,numeroCoordenadasPendientes);

        MenuItem itemSearch = menu.findItem(R.id.action_search);
        searchView = (SearchView) MenuItemCompat.getActionView(itemSearch);
        searchView.setOnQueryTextListener(this);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        if (numeroCoordenadasPendientes == 0){
            menu.findItem(R.id.menu_coordenadas_pendientes).setVisible(false);
        }else{
            menu.findItem(R.id.menu_coordenadas_pendientes).setVisible(true);
        }
    }

    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    public boolean onQueryTextChange(String newText) {
        filtrarCliente(newText);
        return true;
    }

    private void IniciarLocalizador() {
        gpsTracker = new GPSTracker(getActivity());

        if (gpsTracker.isGPSEnabled()){
            if (Build.VERSION.SDK_INT >= 23){
                if (ContextCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED
                        || ContextCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_PREMISOS_UBICACION);
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
        gpsTracker.stopUsingGPS();//Detener el Localizador porque PedidoActivity tendrá su propio localizador
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
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
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

    private void quickItemAccion(int actionId) {
        ClienteModel clienteModel = adapter.getItemSelectedByQuickAction();
        idClienteGeneral = clienteModel.getIdCliente();
        razonSocialGeneral = clienteModel.getRazonSocial();
        switch (actionId){
            case QUICK_ITEM_VER_FICHA:
                Intent intent = new Intent(getActivity(), DetalleClienteActivity.class);
                intent.putExtra("idCliente",clienteModel.getIdCliente());
                intent.putExtra("razonSocial",clienteModel.getRazonSocial());
                startActivity(intent);
                /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    View sharedView = adapter.getViewItemSelectedByQuickAction();
                    String transitionName = getString(R.string.transition_cliente);

                    ActivityOptions transitionActivityOptions = ActivityOptions.makeSceneTransitionAnimation(getActivity(), sharedView, transitionName);
                    startActivity(intent, transitionActivityOptions.toBundle());
                }else{
                    startActivity(intent);
                }*/


                break;
            case QUICK_ITEM_TOMAR_PEDIDO:
                //Verificar si la app está configurada para mostrar la info del vendedor y cliente antes de realizar el pedido
                if (daoConfiguracion.isInfoVendedorCliente()){

                    Intent intent1 = new Intent(getActivity(), InfoVendedorActivity.class);
                    intent1.putExtra("origen",PedidoActivity.ORIGEN_CLIENTES);
                    intent1.putExtra("accion",PedidoActivity.ACCION_NUEVO_PEDIDO);
                    intent1.putExtra("idCliente",clienteModel.getIdCliente());
                    intent1.putExtra("nombreCliente",clienteModel.getRazonSocial());
                    startActivity(intent1);

                }else{
                    tomarPedido();
                }
                break;
            case QUICK_ITEM_NO_VENTA:
                showDialogoNoVenta(clienteModel.getIdCliente());
                break;
            case QUICK_GEOLOCALIZAR:
                showDialogGeolocalizar(clienteModel);
                break;
            case QUICK_ITEM_WHATSAPP:
                whatsappController.revisarWhatsapp(clienteModel);
                break;
            case QUICK_ITEM_BAJAUSER:
                darBajaController.showOpcionesMotivo(clienteModel);
                break;
            default:
                break;
        }
    }

    int noVentaSelected = -1;



    public void showDialogoNoVenta(final String idCliente) {
        noVentaSelected = -1;

        AlertDialog.Builder alerta = new AlertDialog.Builder(getActivity());
        alerta.setIcon(R.drawable.ic_dialog_block);
        alerta.setTitle("Motivo de no Venta");

        final ArrayList<DTOMotivoNoVenta> listaMotivos = daoPedido.getMotivoNoVenta();
        List<CharSequence> arrayList = new ArrayList<>();
        for (int i=0; i<listaMotivos.size(); i++){
            arrayList.add(listaMotivos.get(i).getDescripcion());
            //Se selecciona el motivo por defecto.
            if (noVentaSelected == -1 && listaMotivos.get(i).getIdMotivoNoVenta().equals(PedidoCabeceraModel.ID_MOTIVO_NO_COMPRA_DEFAULT)){
                noVentaSelected = i;
            }
        }
        String[] array = new String[arrayList.size()];
        arrayList.toArray(array);

        if (noVentaSelected == -1)//Si después de las validaciones no se obtuvo algun motivo no venta por defecto, se selecciona el primero
            noVentaSelected = 0;
        alerta.setSingleChoiceItems(array, noVentaSelected, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                noVentaSelected = which;
            }
        });

        alerta.setCancelable(true);
        alerta.setPositiveButton("ACEPTAR", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                final DTOMotivoNoVenta dtoMotivoNoVenta = listaMotivos.get(noVentaSelected);

                String numeroMaximo = daoPedido.getMaximoNumeroPedido(idVendedor);
                String fechaActual = daoConfiguracion.getFechaString();
                String serieVendedor = ((Ventas360App) getActivity().getApplicationContext()).getSerieVendedor();

                if (serieVendedor.isEmpty()){
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setIcon(R.drawable.ic_dialog_error);
                    builder.setTitle("No se obtuvo la serie del vendedor");
                    builder.setMessage("Para registrar pedidos se debe tener la serie del vendedor, sincronice la aplicación y vuelva a intentarlo");
                    builder.setCancelable(false);
                    builder.setNegativeButton("ACEPTAR", new DialogInterface.OnClickListener(){
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            getActivity().finish();
                        }
                    });
                    builder.show();
                }else{
                    String numeroPedido = Util.calcularSecuencia(numeroMaximo,fechaActual,serieVendedor);
                    String numeropedidoSimple = numeroPedido.substring(6,numeroPedido.length());
                    guardarMotivoNoVenta(numeroPedido,dtoMotivoNoVenta.getIdMotivoNoVenta(),idCliente);
                    EnviarPedido(numeroPedido);
                }
            }
        });
        alerta.setNegativeButton("CANCELAR", null);
        alerta.show();
    }

    public void guardarMotivoNoVenta(final String numeroPedido, final String idMotivoNoVenta, final String idCliente) {
        new AsyncTask<Void,Void,Void>(){
            ProgressDialog pDialog;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                pDialog = new ProgressDialog(getActivity());
                pDialog.setCancelable(false);
                pDialog.setIndeterminate(true);
                pDialog.setMessage("Guardando motivo de no venta...");
                pDialog.show();
            }

            @Override
            protected Void doInBackground(Void... strings) {

                while (gpsTracker.getLatitude() == 0.0 && gpsTracker.getLongitude() == 0.0){
                    Log.d(TAG,"latitud y longitud 0.0");//Mantener el hilo trabajando hasta que se tome alguna posición
                }

                PedidoCabeceraModel pedido = new PedidoCabeceraModel();
                pedido.setNumeroPedido(numeroPedido);
                pedido.setImporteTotal(0.0);
                pedido.setFechaPedido(daoConfiguracion.getFechaHoraString());
                pedido.setFechaEntrega("");
                pedido.setFormaPago("");
                pedido.setIdCliente(idCliente);
                pedido.setIdVendedor(idVendedor);
                pedido.setEstado(PedidoCabeceraModel.ESTADO_ANULADO);//Anulado
                pedido.setObservacion("");
                pedido.setIdMotivoNoVenta(idMotivoNoVenta);
                pedido.setPesoTotal(0.0);
                pedido.setFlag(PedidoCabeceraModel.FLAG_PENDIENTE);//Pendiente
                pedido.setLatitud(gpsTracker.getLatitude());
                pedido.setLongitud(gpsTracker.getLongitude());

                daoPedido.guardarPedidoCabecera(pedido);
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                pDialog.dismiss();
            }
        }.execute();

    }

    private void EnviarPedido(final String numeroPedido) {

        new AsyncTask<Void,Void,String>(){
            final String ENVIADO = "E";
            final String INCOMPLETO = "I";
            final String PENDIENTE = "P";
            final String TRANSFERIDO = "T";
            final String JSONEXCEPTION = "jsonException";
            final String SIN_CONEXION = "SinConexion";
            final String OTRO_ERROR = "error";

            ProgressDialog pDialog;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                pDialog = new ProgressDialog(getActivity());
                pDialog.setCancelable(false);
                pDialog.setIndeterminate(true);
                pDialog.setMessage("Enviando motivo de no venta...");
                pDialog.show();
            }

            @Override
            protected String doInBackground(Void... strings) {
                if (Util.isConnectingToRed(getActivity())) {
                    try {
                        ArrayList<DTOPedido> pedidoEnviar = daoPedido.getDTOPedidoCompleto(numeroPedido);
                        String cadena = gson.toJson(pedidoEnviar);
                        String cadenaResultado = soapManager.enviarPendientes(TablesHelper.ObjPedido.ActualizarObjPedido,cadena);
                        return daoPedido.actualizarFlagPedidos(cadenaResultado);
                    } catch (JsonParseException ex) {
                        ex.printStackTrace();
                        return JSONEXCEPTION;
                    }catch (Exception e) {
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
                switch (respuesta){
                    case ENVIADO:
                        showDialogoPostEnvio("Envío sactisfactorio","El pedido fue ingresado al servidor", R.drawable.ic_dialog_check, false);
                        break;
                    case INCOMPLETO:
                        showDialogoPostEnvio("Atención","No se pudieron guardar todos los datos", R.drawable.ic_dialog_alert, false);
                        break;
                    case PENDIENTE:
                        showDialogoPostEnvio("Atención","El servidor no pudo ingresar el pedido", R.drawable.ic_dialog_error, false);
                        break;
                    case TRANSFERIDO:
                        showDialogoPostEnvio("Atención","El pedido ya se encuentra en proceso de facturación \nComuníquese con el administrador", R.drawable.ic_dialog_block, false);
                        break;
                    case SIN_CONEXION:
                        showDialogoPostEnvio("Sin conexión","Es probable que no tenga acceso a INTERNET, El pedido se guardó localmente", R.drawable.ic_dialog_error, false);
                        break;
                    case JSONEXCEPTION:
                        showDialogoPostEnvio("Atención","El pedido fue enviado pero no se pudo verificar\nConsulte con el administrador", R.drawable.ic_dialog_alert, false);
                        break;
                    case OTRO_ERROR:
                        showDialogoPostEnvio("Error","No se pudo enviar el pedido, se guardó localmente", R.drawable.ic_dialog_error, false);
                        break;
                    default:
                        showDialogoPostEnvio("Error","No se pudo enviar el pedido, se guardó localmente", R.drawable.ic_dialog_error, false);
                        break;
                }
            }
        }.execute();
    }

    /**
     * @param titulo
     * @param mensaje
     * @param icon
     * @param accionComercial Indica si se realizó algún pedido, no venta u otra acción para realizar una encuesta
     */
    private void showDialogoPostEnvio(String titulo, String mensaje, @DrawableRes int icon, final boolean accionComercial) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(titulo);
        builder.setMessage(mensaje);
        builder.setIcon(icon);
        builder.setCancelable(false);
        builder.setPositiveButton("ACEPTAR", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog,int which) {
                if (accionComercial){
                    if (ventas360App.getTipoVendedor().equals(VendedorModel.TIPO_MERCADEO)){
                        EncuestaDetalleModel encuestaDetalleModel = daoEncuesta.getEncuestaDetalle(TablesHelper.EncuestaTipo.TIPO_TRADE);
                        if (encuestaDetalleModel != null){
                            tomarEncuesta(encuestaDetalleModel);
                        }else{
                            refreshLista();
                        }
                    }else{
                        EncuestaDetalleModel encuestaDetalleModel = daoEncuesta.getEncuestaPostPedido();
                        if (encuestaDetalleModel != null){
                            tomarEncuesta(encuestaDetalleModel);
                        }else{
                            refreshLista();
                        }
                    }
                }else{
                    refreshLista();
                }

            }
        });
        builder.show();
    }

    private void tomarEncuesta(EncuestaDetalleModel encuestaDetalleModel) {

        if (encuestaDetalleModel != null){
            Intent intent = new Intent(getActivity(), EncuestaClienteDialogActivity.class);

            intent.putExtra("idCliente",idClienteGeneral);
            intent.putExtra("razonSocial",razonSocialGeneral);
            intent.putExtra("clientesObligatorios",encuestaDetalleModel.getClientesObligatorios() == 1); //Determina si se puede cerrar u obviar la encuesta
            intent.putExtra("descripcionEncuesta",encuestaDetalleModel.getDescripcionEncuesta());
            intent.putExtra("tipoEncuesta",encuestaDetalleModel.getTipoEncuesta());
            intent.putExtra("idEncuesta",encuestaDetalleModel.getIdEncuesta());
            intent.putExtra("idEncuestaDetalle",encuestaDetalleModel.getIdEncuestaDetalle());

            startActivityForResult(intent, REQUEST_ENCUESTA);
        }else{
            Toast.makeText(getActivity(), "No se encontró alguna encuesta", Toast.LENGTH_SHORT).show();
        }
    }

    public void refreshLista(){
        listaCliente.clear();
        listaCliente.addAll(daoCliente.getClientesOrdenados());
        listaCliente.addAll(daoCliente.getClientesOrdenadosSinUbicacion(listaCliente.size()));
        adapter.notifyDataSetChanged();

        int numeroClientes = listaCliente.size();
        int numeroVisitados = daoCliente.getNumeroClientesVisitados(ventas360App.getModoVenta(), daoConfiguracion.getEstadoVendedor(ventas360App.getIdEmpresa(),ventas360App.getIdSucursal(),ventas360App.getIdVendedor()));
        int numeroPendientes = numeroClientes - numeroVisitados;

        numeroCoordenadasPendientes = 0;
        listaCoordenadasPendientes.clear();
        for (ClienteModel item : listaCliente){
            if (item.getFlagLocalizacion().equals(ClienteCoordenadasModel.FLAG_PENDIENTE)){
                listaCoordenadasPendientes.add(item);
                numeroCoordenadasPendientes ++;
            }
        }

        getActivity().invalidateOptionsMenu();

        tv_cantidadTotal.setText(String.valueOf(numeroClientes));
        tv_cantidadVisitados.setText(String.valueOf(numeroVisitados));
        tv_cantidadPendientes.setText(String.valueOf(numeroPendientes));
    }

    public void showDialogGeolocalizar(final ClienteModel clienteModel){
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        //builder.setTitle("Geolocalizar cliente");
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View pop = inflater.inflate(R.layout.dialog_geolocalizar_cliente,null);
        builder.setView(pop);
        builder.setCancelable(false);

        TextView tv_cliente = pop.findViewById(R.id.tv_cliente);
        final TextView tv_latitud = pop.findViewById(R.id.tv_latitud);
        final TextView tv_longitud = pop.findViewById(R.id.tv_longitud);
        final ImageView img_location = pop.findViewById(R.id.img_location);
        final ProgressBar progressBar = pop.findViewById(R.id.progressBar);
        tv_cliente.setText(clienteModel.getRazonSocial());

        ubicacionClienteLista = false;
        contadorUbicacion = 0;

        //Configurar GPSTracker (indicar que obtenga ubicacion cada que pueda)
        gpsTracker.stopUsingGPS();
        gpsTracker.setMIN_TIME_BW_UPDATES_GPS(0);
        gpsTracker.setMIN_TIME_BW_UPDATES_NETWORK(0);
        gpsTracker.setTIME_TO_FORCE_UPDATE(1000 * 20);
        final double lastLatitude =  gpsTracker.getLatitude();//guardamos la ultima localizacion real, para luego comparar
        final double lastLongitude =  gpsTracker.getLongitude();//guardamos la ultima localizacion real, para luego comparar
        gpsTracker.getLocations();



        //Handler handler = new Handler();
        final Timer timer = new Timer();

        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                //Se usa progressBar como ancla entre el Looper principal(donde estan las vistas) el runnable creado aqui, para que se puedan acceder a las vistas dentro del runnable. Igual se podría usar cualquier vista como ancla
                progressBar.post(new Runnable() {
                    public void run() {
                        try {
                            if (gpsTracker.getLatitude() != 0.0 && gpsTracker.getLongitude() != 0.0) {
                                /*if (gpsTracker.getLastLocation() != null && gpsTracker.getLatitude() != lastLatitude && gpsTracker.getLongitude() != lastLongitude){

                                }*/
                                if (contadorUbicacion >= 8){//Si el timer se ha ejecutado 8 veces(similar a decir que se ha ejecutado por 4 segundos ya que el timer se ejecuta cada medio segundo)
                                    img_location.setVisibility(View.VISIBLE);
                                    progressBar.setVisibility(View.GONE);
                                    ubicacionClienteLista = true;
                                }
                            }
                            contadorUbicacion++;
                            BigDecimal bd_lat = new BigDecimal(Double.toString(gpsTracker.getLatitude()));
                            bd_lat = bd_lat.setScale(9, RoundingMode.HALF_UP);
                            BigDecimal bd_lng = new BigDecimal(Double.toString(gpsTracker.getLongitude()));
                            bd_lng = bd_lng.setScale(9, RoundingMode.HALF_UP);

                            tv_latitud.setText(String.format("%.9f",bd_lat.doubleValue()));
                            tv_longitud.setText(String.format("%.9f",bd_lng.doubleValue()));
                            Log.e("TIMER TASK", "ACTUALIZANDO VISTAS GPS...");
                        } catch (Exception e) {
                            Log.e("error", e.getMessage());
                        }
                    }
                });
            }
        };
        timer.schedule(task, 0, 500);  //ejecutar en intervalo de 0.5 segundos.

        //Seteamos los botones en null ya que crearemos un boton personalizado, que no cierre automaticamente el dialogo luego de presionarlo
        //Esto para que se pueda verificar que si los campos no esta llenados, muestre una indicación y el usuario no asuma que ya se registró un item
        builder.setPositiveButton("ENVIAR UBICACIÓN",null);

        builder.setNegativeButton("CANCELAR", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                timer.cancel();
                //Configurar GPSTracker (indicar que obtenga ubicacion por el intervalo de tiempo definido)
                gpsTracker.stopUsingGPS();
                gpsTracker.setMIN_TIME_BW_UPDATES_GPS(GPSTracker.DEFAULT_MIN_TIME_BW_UPDATES_GPS);
                gpsTracker.setMIN_TIME_BW_UPDATES_NETWORK(GPSTracker.DEFAULT_MIN_TIME_BW_UPDATES_NETWORK);
                gpsTracker.setTIME_TO_FORCE_UPDATE(GPSTracker.DEFAULT_TIME_TO_FORCE_UPDATE);
                gpsTracker.getLocations();
            }
        });

        //Creamos un alert dialog generalizado pero en base al AlertDialog.Builder que tiene nuestra vista
        final AlertDialog dialog = builder.create();
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                Button btn_positive = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
                btn_positive.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (ubicacionClienteLista){
                            timer.cancel();
                            //Configurar GPSTracker (mandar que obtenga ubicacion cada que pueda)
                            gpsTracker.stopUsingGPS();
                            gpsTracker.setMIN_TIME_BW_UPDATES_GPS(GPSTracker.DEFAULT_MIN_TIME_BW_UPDATES_GPS);
                            gpsTracker.setMIN_TIME_BW_UPDATES_NETWORK(GPSTracker.DEFAULT_MIN_TIME_BW_UPDATES_NETWORK);
                            gpsTracker.setTIME_TO_FORCE_UPDATE(GPSTracker.DEFAULT_TIME_TO_FORCE_UPDATE);
                            gpsTracker.getLocations();

                            //Actualizar bd local
                            BigDecimal bd_lat = new BigDecimal(Double.toString(gpsTracker.getLatitude()));
                            bd_lat = bd_lat.setScale(9, RoundingMode.HALF_UP);
                            BigDecimal bd_lng = new BigDecimal(Double.toString(gpsTracker.getLongitude()));
                            bd_lng = bd_lng.setScale(9, RoundingMode.HALF_UP);

                            daoCliente.actualizarCoordenadas(clienteModel.getIdCliente(), bd_lat.doubleValue(), bd_lng.doubleValue());
                            new async_enviarClienteCoordenadas().execute(clienteModel.getIdCliente());

                            //refresh lista y refresh actionBar
                            dialog.dismiss();
                        }
                        else{

                        }
                    }
                });
            }
        });

        dialog.show();
    }

    /*Metodo para mostrar la lista de pendientes*/
    public void showDialogCoordenadasPendientes(){
        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View alertLayout = inflater.inflate(R.layout.dialog_recyclerview,null);
        final RecyclerView recyclerPendientes = alertLayout.findViewById(R.id.recycler_lista);

        RecyclerViewClienteAdapter adapter = new RecyclerViewClienteAdapter(listaCoordenadasPendientes,getActivity());
        recyclerPendientes.setAdapter(adapter);
        recyclerPendientes.addItemDecoration(new DividerItemDecoration(getActivity(),DividerItemDecoration.VERTICAL));
        recyclerPendientes.setPadding(0,25,0,0);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Coordenadas pendientes ("+numeroCoordenadasPendientes+")");
        builder.setView(alertLayout);
        builder.setPositiveButton("ENVIAR AL SERVIDOR", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                new async_enviarClienteCoordenadas().execute();
            }
        });
        builder.setNegativeButton("CANCELAR",null);
        builder.show();
    }

    class async_obtenerClientes extends AsyncTask<Void,Void,String>{
        ProgressDialog pDialog;
        String errorMensaje = "";
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(getActivity());
            pDialog.setMessage("Obteniendo clientes...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected String doInBackground(Void... strings) {
            String response = "";
            if (Util.isConnectingToRed(getActivity())) {
                if (Util.isConnectingToInternet()){
                    try {
                        soapManager.obtenerRegistrosxVendedorJSON(TablesHelper.Cliente.Sincronizar, TablesHelper.Cliente.Table);
                        soapManager.obtenerRegistrosxVendedorJSON(TablesHelper.ClienteCoordenadas.Sincronizar, TablesHelper.ClienteCoordenadas.Table);
                        response = "OK";
                    } catch (Exception e) {
                        e.printStackTrace();
                        errorMensaje = e.getMessage();
                        Log.e(TAG,e.getMessage()+"");
                        response = "Error";
                    }
                }else{
                    // Sin conexion al Servidor
                    response = "NoInternet";
                }
            } else {
                // Sin conexion al Servidor
                response = "NoInternet";
            }
            return response;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            pDialog.dismiss();
            if (result.equals("NoInternet")){
                showDialogoPostEnvio("Sin conexión","Por el momento no es posible completar la acción, verifique su conexión", R.drawable.ic_dialog_error, false);
            }else if (result.equals("Error")){
                showDialogoPostEnvio("Ocurrió un error",""+errorMensaje, R.drawable.ic_dialog_error, false);
                ventas360App.setIndexRutaMapa(JSONModel.SIN_RUTA_SELECCIONADA);
            }else {
                showDialogoPostEnvio("Sincronización correcta","Se obtuvieron los clientes satisfactoriamente", R.drawable.ic_dialog_check, false);
                ventas360App.setIndexRutaMapa(JSONModel.SIN_RUTA_SELECCIONADA);
            }
        }
    }

    class async_enviarClienteCoordenadas extends AsyncTask<String,Void,String>{
        final String ENVIADO = "E";
        final String PENDIENTE = "P";
        final String JSONEXCEPTION = "jsonException";
        final String SIN_CONEXION = "SinConexion";
        final String OTRO_ERROR = "error";

        ProgressDialog pDialog;
        String errorMensaje = "";
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(getActivity());
            pDialog.setMessage("Enviando coordenadas...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected String doInBackground(String... strings) {
            String idCliente = "";
            if (strings.length != 0)
                idCliente = strings[0];
            String response = "";
            if (Util.isConnectingToRed(getActivity()) && Util.isConnectingToInternet()) {
                try {
                    ArrayList<ClienteCoordenadasModel> coordenadas = daoCliente.getClientesCoordenadasPendientes(idCliente);
                    String cadena = gson.toJson(coordenadas);

                    String cadenaResultado = soapManager.enviarPendientes(TablesHelper.ClienteCoordenadas.ActualizarCoordenadas, cadena);
                    return daoCliente.actualizarFlagCoordenadas(cadenaResultado);
                } catch (XmlPullParserException e){
                    e.printStackTrace();
                    response = SIN_CONEXION;
                } catch (Exception e) {
                    e.printStackTrace();
                    errorMensaje = e.getMessage();
                    Log.e(TAG,e.getMessage()+"");
                    response = OTRO_ERROR;
                }
            } else {
                response = SIN_CONEXION;
            }
            return response;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            pDialog.dismiss();
            if (result.equals(ENVIADO)){
                showDialogoPostEnvio("Envío correcto","Se actualizarion las coordenadas", R.drawable.ic_dialog_check, false);
            }else if (result.equals(PENDIENTE)){
                showDialogoPostEnvio("Atención","No se pudo enviar todos los datos, inténtelo nuevamente", R.drawable.ic_dialog_alert, false);
            }else if (result.equals(SIN_CONEXION)){
                showDialogoPostEnvio("Sin conexión","Por el momento no es posible completar la acción, verifique su conexión", R.drawable.ic_dialog_error, false);
            }else if (result.equals(OTRO_ERROR)){
                showDialogoPostEnvio("Atención","No se pudo enviar todos los datos, inténtelo nuevamente\n"+errorMensaje, R.drawable.ic_dialog_error, false);
            }else {
                showDialogoPostEnvio("Ocurrió un problema",""+result, R.drawable.ic_dialog_error, false);
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.v(TAG,"requestCode:"+requestCode+"  resultCode:"+resultCode);
        if (requestCode == REQUEST_CODE_TOMAR_PEDIDO){
            if (resultCode == RESULT_OK){
                refreshLista();
            }
        }else if (requestCode == REQUEST_CODE_UBICACION){
            Log.e(TAG,"REQUEST_CODE_UBICACION -> "+resultCode);
            //No se valida si es RESULT_OK porque no tenemos control de la actividad Settings que se ha lanzado
            if (gpsTracker.isGPSEnabled()){
                if (Build.VERSION.SDK_INT >= 23){
                    if (ContextCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED
                            || ContextCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_PREMISOS_UBICACION);
                    }else
                        gpsTracker.getLocations();
                }else
                    gpsTracker.getLocations();
            }else {
                showDialogoUbicacion();
            }
        } else if (requestCode == REQUEST_ENCUESTA){
            if (resultCode == RESULT_OK){
                refreshLista();
            }
        } else if (requestCode == REQUEST_TOMAR_ENCUESTA){
            if (resultCode == RESULT_OK){
                Intent intent;
                if (ventas360App.getModoVenta().equals(VendedorModel.MODO_PREVENTA)){
                    if (daoConfiguracion.getEstadoVendedor(ventas360App.getIdEmpresa(),ventas360App.getIdSucursal(),ventas360App.getIdVendedor()).equals(VendedorModel.ESTADO_OPERATIVO)){
                        intent = new Intent(getActivity(), PedidoActivity.class);
                        intent.putExtra("origen",PedidoActivity.ORIGEN_CLIENTES);
                        intent.putExtra("accion",PedidoActivity.ACCION_NUEVO_PEDIDO);
                        intent.putExtra("idCliente",idClienteGeneral);
                        intent.putExtra("nombreCliente",razonSocialGeneral);
                        startActivityForResult(intent,REQUEST_CODE_TOMAR_PEDIDO);
                    }else{
                        Toast.makeText(getActivity(),"No puede ingresar pedidos luego del cierre de ventas",Toast.LENGTH_SHORT).show();
                    }
                }else {
                    intent = new Intent(getActivity(), PedidoActivity.class);
                    intent.putExtra("origen",PedidoActivity.ORIGEN_CLIENTES);
                    intent.putExtra("accion",PedidoActivity.ACCION_NUEVO_PEDIDO);
                    intent.putExtra("idCliente",idClienteGeneral);
                    intent.putExtra("nombreCliente",razonSocialGeneral);
                    startActivityForResult(intent,REQUEST_CODE_TOMAR_PEDIDO);
                }
            }else{
                //No se realiza ninguna accion
            }
        } else if (requestCode == 124){ //agregar nuevo o actualizar whatsapp
            if (resultCode == RESULT_OK){
                refreshLista();
            }
        }
    }

    public void filtrarCliente(String newText) {
        List<ClienteModel> filteredModelList = filtrar(listaCliente, newText);
        adapter.setFilter(filteredModelList);
    }


    private List<ClienteModel> filtrar(ArrayList<ClienteModel> listaCliente, String query) {
        query = query.toLowerCase();
        final List<ClienteModel> listaFiltrada = new ArrayList<>();
        for (ClienteModel cliente : listaCliente) {

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

    public static void tomarPedido(){
        Intent intent;
        //Verificar si existe una encuesta pre o post pedido
        EncuestaDetalleModel encuestaDetalleModel = daoEncuesta.getEncuestaPrePedido();
        if (encuestaDetalleModel != null){

            //Realizar encuesta y esperar respuesta para tomar el pedido
            intent = new Intent(fragment.getActivity(), EncuestaClienteDialogActivity.class);
            intent.putExtra("razonSocial",razonSocialGeneral);
            intent.putExtra("idCliente",idClienteGeneral);
            intent.putExtra("descripcionEncuesta",encuestaDetalleModel.getDescripcionEncuesta());
            intent.putExtra("idTipoEncuesta",encuestaDetalleModel.getIdTipoEncuesta());
            intent.putExtra("tipoEncuesta",encuestaDetalleModel.getTipoEncuesta());
            intent.putExtra("idEncuesta",encuestaDetalleModel.getIdEncuesta());
            intent.putExtra("idEncuestaDetalle",encuestaDetalleModel.getIdEncuestaDetalle());
            fragment.startActivityForResult(intent,REQUEST_TOMAR_ENCUESTA);

        }else{
            if (ventas360App.getModoVenta().equals(VendedorModel.MODO_PREVENTA)){
                if (daoConfiguracion.getEstadoVendedor(ventas360App.getIdEmpresa(),ventas360App.getIdSucursal(),ventas360App.getIdVendedor()).equals(VendedorModel.ESTADO_OPERATIVO)){
                    intent = new Intent(fragment.getActivity(), PedidoActivity.class);
                    intent.putExtra("origen",PedidoActivity.ORIGEN_CLIENTES);
                    intent.putExtra("accion",PedidoActivity.ACCION_NUEVO_PEDIDO);
                    intent.putExtra("idCliente",idClienteGeneral);
                    intent.putExtra("nombreCliente",razonSocialGeneral);
                    fragment.startActivityForResult(intent,REQUEST_CODE_TOMAR_PEDIDO);
                }else{
                    Toast.makeText(fragment.getActivity(),"No puede ingresar pedidos luego del cierre de ventas",Toast.LENGTH_SHORT).show();
                }
            }else {
                intent = new Intent(fragment.getActivity(), PedidoActivity.class);
                intent.putExtra("origen",PedidoActivity.ORIGEN_CLIENTES);
                intent.putExtra("accion",PedidoActivity.ACCION_NUEVO_PEDIDO);
                intent.putExtra("idCliente",idClienteGeneral);
                intent.putExtra("nombreCliente",razonSocialGeneral);
                fragment.startActivityForResult(intent,REQUEST_CODE_TOMAR_PEDIDO);
            }
        }
    }

}

