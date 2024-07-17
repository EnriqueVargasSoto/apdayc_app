package com.expediodigital.ventas360.view.fragment;


import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
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
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.expediodigital.ventas360.DAO.DAOConfiguracion;
import com.expediodigital.ventas360.DAO.DAOEncuesta;
import com.expediodigital.ventas360.DAO.DAOPedido;
import com.expediodigital.ventas360.DTO.DTOMotivoNoVenta;
import com.expediodigital.ventas360.DTO.DTOPedido;
import com.expediodigital.ventas360.R;
import com.expediodigital.ventas360.Ventas360App;
import com.expediodigital.ventas360.adapter.RecyclerViewPedidoAdapter;
import com.expediodigital.ventas360.model.EncuestaDetalleModel;
import com.expediodigital.ventas360.model.PedidoCabeceraModel;
import com.expediodigital.ventas360.model.VendedorModel;
import com.expediodigital.ventas360.quickaction.ActionItem;
import com.expediodigital.ventas360.quickaction.QuickAction;
import com.expediodigital.ventas360.service.GPSTracker;
import com.expediodigital.ventas360.util.MenuItemCustomListener;
import com.expediodigital.ventas360.util.SoapManager;
import com.expediodigital.ventas360.util.TablesHelper;
import com.expediodigital.ventas360.util.Util;
import com.expediodigital.ventas360.view.DetallePedidoActivity;
import com.expediodigital.ventas360.view.EncuestaClienteDialogActivity;
import com.expediodigital.ventas360.view.PedidoActivity;
import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.google.gson.Gson;
import com.google.gson.JsonParseException;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class PedidosFragment extends Fragment implements SearchView.OnQueryTextListener {
    public static final String TAG = "PedidosFragment";
    public static PedidosFragment pedidosFragmentContext;
    private static final int QUICK_ITEM_VER_DETALLE = 0;
    private static final int QUICK_ITEM_MODIFICAR = 1;
    private static final int QUICK_ITEM_ANULAR = 2;
    private static final int QUICK_ITEM_OBSERVACION = 3;
    private static final int QUICK_ITEM_FACTURAR = 4;

    private static final int REQUEST_NUEVO_PEDIDO = 1;
    private static final int REQUEST_MODIFICAR_PEDIDO = 2;
    private static final int REQUEST_DETALLE_PEDIDO = 3;
    private static final int REQUEST_CODE_UBICACION = 4;
    private static final int REQUEST_PREMISOS_UBICACION = 5;
    private static final int REQUEST_ENCUESTA = 6;

    ProgressDialog progressDialog;
    SoapManager soap_manager;

    RecyclerView recycler_pedidos;
    RecyclerViewPedidoAdapter adapter;
    DAOPedido daoPedido;
    DAOConfiguracion daoConfiguracion;
    DAOEncuesta daoEncuesta;
    SoapManager soapManager;
    ArrayList<PedidoCabeceraModel> listaPedidosCabecera = new ArrayList<>();
    ArrayList<PedidoCabeceraModel> listaPedidosPendientes = new ArrayList<>();
    int numeroPedidosPendientes = 0;

    private FloatingActionMenu fab_menu;
    private FloatingActionButton fab_nuevoPedido, fab_obtenerPedidos;

    TextView tv_pedidosEfectivos,tv_pedidosAnulados,tv_cantidadVenta,tv_montoTotal;

    DecimalFormat formateador;
    Ventas360App ventas360App;

    GPSTracker gpsTracker;
    boolean mostrarcheckEntregado = false;
    QuickAction quickAction;
    SearchView searchView;
    boolean isSeaching = false;
    String textSearching = "";

    private String idClienteGeneral = "";
    private String razonSocialGeneral = "";

    public PedidosFragment() {}


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        View view = inflater.inflate(R.layout.fragment_pedidos, container, false);
        setHasOptionsMenu(true);
        Util.actualizarToolBar(getString(R.string.menu_Pedidos),false,getActivity());

        soap_manager    = new SoapManager(getActivity());
        ventas360App = (Ventas360App) getActivity().getApplicationContext();

        soapManager = new SoapManager(getActivity());
        daoPedido = new DAOPedido(getActivity().getApplicationContext());
        daoConfiguracion = new DAOConfiguracion(getActivity().getApplicationContext());
        daoEncuesta = new DAOEncuesta(getActivity());
        mostrarcheckEntregado = ventas360App.getMarcarPedidosEntregados();
        adapter = new RecyclerViewPedidoAdapter(listaPedidosCabecera,mostrarcheckEntregado,PedidosFragment.this);
        recycler_pedidos = view.findViewById(R.id.recycler_pedidos);
        recycler_pedidos.setAdapter(adapter);
        recycler_pedidos.addItemDecoration(new DividerItemDecoration(getActivity(),DividerItemDecoration.VERTICAL));
        recycler_pedidos.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE)
                    fab_menu.showMenuButton(true); //.show(true);
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (dy > 0 || dy < 0 && fab_menu.isShown())
                    fab_menu.hideMenuButton(true); //.hide(true);
            }
        });

        fab_menu = view.findViewById(R.id.fab_menu);
        fab_menu.setClosedOnTouchOutside(true);
        fab_menu.getMenuIconView().setImageDrawable(ContextCompat.getDrawable(getActivity(),R.drawable.ic_action_agregar));

        fab_nuevoPedido = view.findViewById(R.id.fab_nuevoPedido);
        fab_obtenerPedidos = view.findViewById(R.id.fab_obtenerPedidos);

        fab_nuevoPedido.setImageDrawable(ContextCompat.getDrawable(getActivity(),R.drawable.ic_action_agregar_white));
        fab_obtenerPedidos.setImageDrawable(ContextCompat.getDrawable(getActivity(),R.drawable.ic_action_sincronizar));

        fab_nuevoPedido.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*Accion comentada, para que no se pueda realizar pedidos desde este fragment. El objetivo es hacer que se tome el pedido
                desde la lista de clientes y de esa forma se pueda tomar encuestas obligatorias*/
                /*
                fab_menu.close(true);
                if (ventas360App.getModoVenta().equals(VendedorModel.MODO_PREVENTA)){
                    if (daoConfiguracion.getEstadoVendedor(ventas360App.getIdEmpresa(),ventas360App.getIdSucursal(),ventas360App.getIdVendedor()).equals(VendedorModel.ESTADO_OPERATIVO)){
                        Intent intent = new Intent(getActivity(), PedidoActivity.class);
                        intent.putExtra("origen",PedidoActivity.ORIGEN_PEDIDOS);
                        intent.putExtra("accion",PedidoActivity.ACCION_NUEVO_PEDIDO);
                        startActivityForResult(intent, REQUEST_NUEVO_PEDIDO);
                    }else{
                        Toast.makeText(getActivity(),"No puede ingresar pedidos luego del cierre de ventas",Toast.LENGTH_SHORT).show();
                    }
                }else {
                    Intent intent = new Intent(getActivity(), PedidoActivity.class);
                    intent.putExtra("origen",PedidoActivity.ORIGEN_PEDIDOS);
                    intent.putExtra("accion",PedidoActivity.ACCION_NUEVO_PEDIDO);
                    startActivityForResult(intent, REQUEST_NUEVO_PEDIDO);
                }
                */
            }
        });

        fab_obtenerPedidos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fab_menu.close(true);
                // Generar backup antes de la sincronizacion
                Util.backupdDatabase(getActivity());
                new async_obtenerPedidos().execute();
            }
        });

        if( !ventas360App.getTipoVendedor().equals(VendedorModel.TIPO_TRANSPORTISTA)){
            //fab_obtenerPedidos.setVisibility(View.GONE);//No usar INVISIBLE porque en la librería si el boton ha sido usado se queda VISIBLE
            fab_menu.setVisibility(View.GONE);
        }

        tv_pedidosEfectivos = view.findViewById(R.id.tv_pedidosEfectivos);
        tv_pedidosAnulados = view.findViewById(R.id.tv_pedidosAnulados);
        tv_cantidadVenta = view.findViewById(R.id.tv_cantidadVenta);
        tv_montoTotal = view.findViewById(R.id.tv_montoTotal);
        formateador = Util.formateador();
        refreshLista();
        IniciarLocalizador();
        return view;
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
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        quickAction = new QuickAction(getActivity());
        ActionItem quickItemDetalle = new ActionItem(QUICK_ITEM_VER_DETALLE, "Ver detalle", ContextCompat.getDrawable(getActivity(),R.drawable.icon_document_detalle));
        ActionItem quickItemModificar = new ActionItem(QUICK_ITEM_MODIFICAR, "Modificar",ContextCompat.getDrawable(getActivity(),R.drawable.icon_document_editar));
        ActionItem quickItemAnular = new ActionItem(QUICK_ITEM_ANULAR, "Anular",ContextCompat.getDrawable(getActivity(),R.drawable.icon_document_anular));
        ActionItem quickItemObservacion = new ActionItem(QUICK_ITEM_OBSERVACION, "Observación",ContextCompat.getDrawable(getActivity(),R.drawable.icon_document_observacion));

        quickAction.addActionItem(quickItemDetalle);
        quickAction.addActionItem(quickItemModificar);
        quickAction.addActionItem(quickItemAnular);
        quickAction.addActionItem(quickItemObservacion);

        quickAction.setOnActionItemClickListener(new QuickAction.OnActionItemClickListener() {
            @Override
            public void onItemClick(QuickAction source, int pos, int actionId) {
                PedidoCabeceraModel cabeceralModel = adapter.getItemSelectedByQuickAction();
                idClienteGeneral = cabeceralModel.getIdCliente();
                razonSocialGeneral = cabeceralModel.getNombreCliente();
                Intent intent;
                switch (actionId){
                    case QUICK_ITEM_VER_DETALLE:
                        /*
                        intent = new Intent(getActivity(), PedidoActivity.class);
                        intent.putExtra("origen",PedidoActivity.ORIGEN_PEDIDOS);
                        intent.putExtra("accion",PedidoActivity.ACCION_VER_PEDIDO);
                        intent.putExtra("idCliente",cabeceralModel.getIdCliente());
                        intent.putExtra("numeroPedido",cabeceralModel.getNumeroPedido());
                        intent.putExtra("nombreCliente",cabeceralModel.getNombreCliente());
                        */
                        intent = new Intent(getActivity(), DetallePedidoActivity.class);
                        intent.putExtra("numeroPedido",cabeceralModel.getNumeroPedido());
                        intent.putExtra("idCliente",cabeceralModel.getIdCliente());
                        intent.putExtra("nombreCliente",cabeceralModel.getNombreCliente());
                        startActivityForResult(intent, REQUEST_DETALLE_PEDIDO);
                        break;
                    case QUICK_ITEM_MODIFICAR:
                        if (cabeceralModel.getPedidoEntregado() == 0) {
                            String codigoMotivoNoVenta = daoPedido.getIdMotivoNoVentaPedido(cabeceralModel.getNumeroPedido());
                            if (!codigoMotivoNoVenta.equals("0")) {
                                new async_modificarPedido().execute(cabeceralModel.getNumeroPedido(), async_modificarPedido.MODIFICAR_NO_VENTA + "", "", "",cabeceralModel.getEstado());
                            } else {
                                new async_modificarPedido().execute(cabeceralModel.getNumeroPedido(), async_modificarPedido.MODIFICAR_PEDIDO + "", cabeceralModel.getIdCliente(), cabeceralModel.getNombreCliente(),cabeceralModel.getEstado());
                            }
                        }else
                            Toast.makeText(getActivity(),"El pedido ya ha sido entregado",Toast.LENGTH_SHORT).show();
                        break;
                    case QUICK_ITEM_ANULAR:
                        if (cabeceralModel.getPedidoEntregado() == 0)
                            new async_modificarPedido().execute(cabeceralModel.getNumeroPedido(), async_modificarPedido.MODIFICAR_NO_VENTA,"","",cabeceralModel.getEstado());
                        else
                            Toast.makeText(getActivity(),"El pedido ya ha sido entregado",Toast.LENGTH_SHORT).show();
                        break;
                    case QUICK_ITEM_OBSERVACION:
                        if (cabeceralModel.getPedidoEntregado() == 0)
                            new async_modificarPedido().execute(cabeceralModel.getNumeroPedido(), async_modificarPedido.MODIFICAR_OBSERVACION,"","",cabeceralModel.getEstado());
                        else
                            Toast.makeText(getActivity(),"El pedido ya ha sido entregado",Toast.LENGTH_SHORT).show();
                        break;
                    default:
                        break;
                }
            }
        });
        quickAction.setOnDismissListener(new QuickAction.OnDismissListener() {
            @Override
            public void onDismiss() {

            }
        });
        adapter.setQuickAction(quickAction);
    }

    public void refreshLista(){
        Log.d(TAG,"REFRESHING LISTA");
        listaPedidosCabecera.clear();
        listaPedidosCabecera.addAll(daoPedido.getPedidosCabecera());
        adapter.notifyDataSetChanged();
        //Si se ha aplicado un filtro (searchView) la lista que está en el adapter está limitada al filtro, por las que aqui se actualice la referencia. No se le vuelve a pasar la lista completa
        //La lista del adapter se modifica cada que se filtra, en esa medida no habría necesidad de estar pasando la lista nuevamente. A no ser que se quiera refrescar todalmente la vista y que se muestre toda la data forzandolo

        int pedidosEfectivos = 0;
        int pedidosAnulados = 0;
        double importeTotal = 0;

        numeroPedidosPendientes = 0;
        listaPedidosPendientes.clear();
        for (PedidoCabeceraModel pedido : listaPedidosCabecera){
            if (pedido.getEstado().equals(PedidoCabeceraModel.ESTADO_ANULADO)){
                pedidosAnulados ++;
            }else{
                pedidosEfectivos ++;
                importeTotal += pedido.getImporteTotal();
            }

            if (pedido.getFlag().equals("P")){
                listaPedidosPendientes.add(pedido);
                numeroPedidosPendientes ++;
            }
        }

        int cantidadPaquetes = daoPedido.getCantidadPaquetesTotal();
        int cantidadUnidades = daoPedido.getCantidadUnidadesTotal();

        getActivity().invalidateOptionsMenu();

        tv_pedidosEfectivos.setText(String.valueOf(pedidosEfectivos));
        tv_pedidosAnulados.setText(String.valueOf(pedidosAnulados));
        tv_montoTotal.setText("S/. "+formateador.format(importeTotal));
        tv_cantidadVenta.setText(cantidadPaquetes + "." + cantidadUnidades);
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        //super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG,"onActivityResult requestCode:"+requestCode+" resultCode:"+resultCode);

        if (requestCode != REQUEST_CODE_UBICACION){
            //De cualquier respuesta se actualiza la lista
            if (resultCode == getActivity().RESULT_OK){
                refreshLista();
            }
        }else{
            //Si la respuesta es de configuracion ubicacion no se actualiza la lista (para no mover el recyclerView)
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
        }
    }

    class async_modificarPedido extends AsyncTask<String,Void,String>{
        final static String MODIFICAR_PEDIDO = "0";
        final static String MODIFICAR_NO_VENTA = "1";
        final static String MODIFICAR_OBSERVACION = "2";
        String numeroPedido;
        String tipo;
        String idCliente;
        String nombreCliente;
        String estado;
        ProgressDialog pDialog;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(getActivity());
            pDialog.setMessage("Verificando...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected String doInBackground(String... strings) {
            numeroPedido = strings[0];
            tipo = strings[1];
            idCliente = strings[2];
            nombreCliente = strings[3];
            estado = strings[4];

            String response = "";
            /*Para preventa no es necesario que se obtenga el estado del pedido online, ya que hay usuarios que no tienen acceso a internet,
             * ademas de que en preventa solo un usuario tiene acceso a su propio pedido a diferencia de autoventa donde el transportista
             * puede modificar el pedido de los vendedores*/
            if (ventas360App.getModoVenta().equals(VendedorModel.MODO_PREVENTA) && !ventas360App.getSettings_preventaEnLinea()) {
                response = estado;
            } else if (ventas360App.getTipoVendedor().equals(VendedorModel.TIPO_MERCADEO)){
                response = estado;
            }else {
                if (Util.isConnectingToRed(getActivity())) {
                    try {
                        response = soapManager.obtenerEstadoPedido(TablesHelper.ObjPedido.ObtenerEstado,numeroPedido);
                        if (response.length()>1){
                            response = "Error";
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        Log.e(TAG,e.getMessage()+"");
                        response = "Error";
                    }
                } else {
                    // Sin conexion al Servidor
                    response = "NoInternet";
                }
            }
            return response;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            pDialog.dismiss();
            if (result.equals("NoInternet")) {
                // Sin conexion al Servidor, no se pudo verificar asi que no se continua con la accion seleccionada
                AlertDialog.Builder alerta = new AlertDialog.Builder(getActivity());
                alerta.setIcon(R.drawable.ic_dialog_error);
                alerta.setTitle("Sin conexión");
                alerta.setMessage("Por el momento no es posible completar la acción, verifique su conexión");
                alerta.setCancelable(false);
                alerta.setPositiveButton("ACEPTAR", null);
                alerta.show();
            } else if (result.equals("Error")) {
                // Error en el envio, no se pudo verificar asi que no se continua con la accion seleccionada
                AlertDialog.Builder alerta = new AlertDialog.Builder(getActivity());
                alerta.setIcon(R.drawable.ic_dialog_error);
                alerta.setTitle("No se pudo verificar");
                alerta.setMessage("No se pudo obtener el estado del pedido, no es posible completar la acción");
                alerta.setCancelable(false);
                alerta.setPositiveButton("ACEPTAR", null);
                alerta.show();

            }else if (result.equals("N")){
                // Error en el envio, no se pudo verificar asi que no se continua con la accion seleccionada
                AlertDialog.Builder alerta = new AlertDialog.Builder(getActivity());
                alerta.setIcon(R.drawable.ic_dialog_error);
                alerta.setTitle("No se pudo verificar");
                alerta.setMessage("No se pudo obtener el estado del pedido, ya se hizo el cierre de ventas");
                alerta.setCancelable(false);
                alerta.setPositiveButton("ACEPTAR", null);
                alerta.show();

            }else {
                if (result.equals(PedidoCabeceraModel.ESTADO_FACTURADO)){
                    if (tipo.equals(MODIFICAR_NO_VENTA)){
                        //Se puede anular un pedido ya facturado, para quitar la factura y obtener el pedido
                        AlertDialog.Builder alerta = new AlertDialog.Builder(getActivity());
                        alerta.setIcon(R.drawable.ic_dialog_block);
                        alerta.setTitle("Atención");
                        alerta.setMessage("El pedido se encuentra facturado, ¿Desea anular la factura?");
                        alerta.setCancelable(false);
                        alerta.setPositiveButton("ANULAR FACTURA", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                daoPedido.actualizarPedidoGenerado(numeroPedido);
                                EnviarPedido(numeroPedido, false);
                            }
                        });
                        alerta.setNegativeButton("CANCELAR", null);
                        alerta.show();
                        refreshLista();//Para que la lista muestre el pedido como Facturado o transferido

                    }else{
                        //El pedido ya ha sido transferido y no se puede modificar
                        AlertDialog.Builder alerta = new AlertDialog.Builder(getActivity());
                        alerta.setIcon(R.drawable.ic_dialog_block);
                        alerta.setTitle("Atención");
                        alerta.setMessage("El pedido ha sido facturado y no se puede modificar\nComuníquese con el administrador");
                        alerta.setCancelable(false);
                        alerta.setPositiveButton("ACEPTAR", null);
                        alerta.show();
                        refreshLista();//Para que la lista muestre el pedido como Facturado o transferido
                    }
                }else{
                    if (tipo.equals(MODIFICAR_PEDIDO)){
                        //Si el pedido esta anulado entonces ya no puede ser modificado
                        if (result.equals(PedidoCabeceraModel.ESTADO_ANULADO)){
                            Toast.makeText(getActivity(),"El pedido se encuentra anulado actualmente",Toast.LENGTH_SHORT).show();
                            refreshLista();//Para que la lista muestre el pedido como anulado
                            showDialogoNoVenta(numeroPedido);
                        }else{
                            Intent intent = new Intent(getActivity(), PedidoActivity.class);
                            intent.putExtra("origen",PedidoActivity.ORIGEN_PEDIDOS);
                            intent.putExtra("accion",PedidoActivity.ACCION_EDITAR_PEDIDO);
                            intent.putExtra("idCliente",idCliente);
                            intent.putExtra("numeroPedido",numeroPedido);
                            intent.putExtra("nombreCliente",nombreCliente);
                            startActivityForResult(intent, REQUEST_MODIFICAR_PEDIDO);
                        }
                    }else if (tipo.equals(MODIFICAR_NO_VENTA)){
                        showDialogoNoVenta(numeroPedido);
                    }else if (tipo.equals(MODIFICAR_OBSERVACION)){
                        showDialogoObservacion(numeroPedido);
                    }
                }
            }
        }
    }

    int noVentaSelected = -1;
    public void showDialogoNoVenta(final String numeroPedido) {
        noVentaSelected = -1;
        String idNoVentaPedido = daoPedido.getIdMotivoNoVentaPedido(numeroPedido);
        AlertDialog.Builder alerta = new AlertDialog.Builder(getActivity());
        alerta.setIcon(R.drawable.ic_dialog_block);
        alerta.setTitle("Motivo de no Venta");

        final ArrayList<DTOMotivoNoVenta> listaMotivos = daoPedido.getMotivoNoVenta();
        List<CharSequence> arrayList = new ArrayList<>();
        for (int i=0; i<listaMotivos.size(); i++){
            arrayList.add(listaMotivos.get(i).getDescripcion());
            if (idNoVentaPedido.equals(listaMotivos.get(i).getIdMotivoNoVenta())){
                noVentaSelected = i;
            }
            //Si no se tiene un motivo anteriormente seleccionado, se selecciona el motivo por defecto.
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

                guardarMotivoNoVenta(numeroPedido,dtoMotivoNoVenta.getIdMotivoNoVenta());
            }
        });
        alerta.setNegativeButton("CANCELAR", null);
        alerta.show();
    }

    private void EnviarPedido(final String numeroPedido, final boolean accionComercial) {

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
                Gson gson = new Gson();
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
                        showDialogoPostEnvio("Envío sactisfactorio","El pedido fue ingresado al servidor", R.drawable.ic_dialog_check, accionComercial);
                        break;
                    case INCOMPLETO:
                        showDialogoPostEnvio("Atención","No se pudieron guardar todos los datos", R.drawable.ic_dialog_alert, accionComercial);
                        break;
                    case PENDIENTE:
                        showDialogoPostEnvio("Atención","El servidor no pudo ingresar el pedido", R.drawable.ic_dialog_error, accionComercial);
                        break;
                    case TRANSFERIDO:
                        showDialogoPostEnvio("Atención","El pedido ya se encuentra en proceso de facturación \nComuníquese con el administrador", R.drawable.ic_dialog_block, accionComercial);
                        break;
                    case SIN_CONEXION:
                        showDialogoPostEnvio("Sin conexión","Es probable que no tenga acceso a INTERNET, El pedido se guardó localmente", R.drawable.ic_dialog_error, accionComercial);
                        break;
                    case JSONEXCEPTION:
                        showDialogoPostEnvio("Atención","El pedido fue enviado pero no se pudo verificar\nConsulte con el administrador", R.drawable.ic_dialog_alert, accionComercial);
                        break;
                    case OTRO_ERROR:
                        showDialogoPostEnvio("Error","No se pudo enviar el pedido, se guardó localmente", R.drawable.ic_dialog_error, accionComercial);
                        break;
                    default:
                        showDialogoPostEnvio("Error","No se pudo enviar el pedido, se guardó localmente", R.drawable.ic_dialog_error, accionComercial);
                        break;
                }
            }
        }.execute();
    }

    private void showDialogoPostEnvio(String titulo, String mensaje,@DrawableRes int icon, final boolean accionComercial) {
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

    private void showDialogoPostEnvioLista(String titulo, String mensaje,@DrawableRes int icon) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(titulo);
        builder.setMessage(mensaje);
        builder.setIcon(icon);
        builder.setCancelable(false);
        builder.setPositiveButton("ACEPTAR", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog,int which) {
                refreshLista();
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
            intent.putExtra("idTipoEncuesta",encuestaDetalleModel.getIdTipoEncuesta());
            intent.putExtra("tipoEncuesta",encuestaDetalleModel.getTipoEncuesta());
            intent.putExtra("idEncuesta",encuestaDetalleModel.getIdEncuesta());
            intent.putExtra("idEncuestaDetalle",encuestaDetalleModel.getIdEncuestaDetalle());

            startActivityForResult(intent, REQUEST_ENCUESTA);
        }else{
            Toast.makeText(getActivity(), "No se encontró alguna encuesta", Toast.LENGTH_SHORT).show();
        }
    }

    public void showDialogoObservacion(final String numeroPedido) {
        String observacionPedido = daoPedido.getObservacionPedido(numeroPedido);
        AlertDialog.Builder alerta = new AlertDialog.Builder(getActivity());
        alerta.setIcon(R.drawable.ic_dialog_alert);
        alerta.setTitle("Observación del Pedido");
        final EditText editText = new EditText(getActivity());
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        editText.setLayoutParams(layoutParams);
        layoutParams.setMargins(10,10,10,10);

        editText.setLayoutParams(layoutParams);
        editText.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        editText.setMinLines(4);

        editText.setHint("Ingrese una observación");
        editText.setText(observacionPedido);
        alerta.setView(editText);

        alerta.setPositiveButton("ACEPTAR", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                final String observacion = editText.getText().toString();
                guardarObservacionPedido(numeroPedido,observacion);
                EnviarPedido(numeroPedido, false);

            }
        });
        alerta.setNegativeButton("CANCELAR", null);
        alerta.show();
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

    public void guardarMotivoNoVenta(final String numeroPedido, final String idMotivoNoVenta) {
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
                pedido.setEstado(PedidoCabeceraModel.ESTADO_ANULADO);//Anulado
                pedido.setFlag(PedidoCabeceraModel.FLAG_PENDIENTE);//Pendiente
                pedido.setIdMotivoNoVenta(idMotivoNoVenta);
                pedido.setLatitud(gpsTracker.getLatitude());
                pedido.setLongitud(gpsTracker.getLongitude());

                daoPedido.actualizarMotivoNoVentaPedido(pedido);
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                pDialog.dismiss();

                EnviarPedido(numeroPedido, false);
            }
        }.execute();
    }

    public void guardarObservacionPedido(String numeroPedido,String observacion) {
        PedidoCabeceraModel pedido = new PedidoCabeceraModel();
        pedido.setNumeroPedido(numeroPedido);
        pedido.setFlag(PedidoCabeceraModel.FLAG_PENDIENTE);//Pendiente
        pedido.setObservacion(observacion);

        daoPedido.actualizarObservacionPedido(pedido);
    }

    @Override
    public void onCreateOptionsMenu(final Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_fragment_pedidos, menu);

        final View menuItemPendientes = menu.findItem(R.id.menu_pedidos_pendientes).getActionView();
        TextView tv_contador = (TextView) menuItemPendientes.findViewById(R.id.tv_contador);

        new MenuItemCustomListener(menuItemPendientes, "Pendientes") {
            @Override
            public void onClick(View v) {
                showPedidosPendientesDialog();
            }
        }.actualizarTextView(tv_contador,numeroPedidosPendientes);

        MenuItem itemSearch = menu.findItem(R.id.action_search);
        searchView = (SearchView) MenuItemCompat.getActionView(itemSearch);
        searchView.setOnQueryTextListener(this);
        searchView.setOnSearchClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isSeaching = true;
                menu.findItem(R.id.menu_pedidos_pendientes).setVisible(false);
                searchView.requestFocus();
            }
        });
        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                isSeaching = false;
                if (numeroPedidosPendientes>0)
                    menu.findItem(R.id.menu_pedidos_pendientes).setVisible(true);
                return false;
            }
        });
        /*Cada vez que se ejecuta el método refreshLista, el menú se refresa, recreando el searchView. En este proceso se pierde el estado que tenía el serchView (Si estaba abierto o si tenia un texto)
        * Lo que se hace es guardar el estado junto al texto en variables y al momento de recrear el searchView, actualizar a como estaba antes de recrear.*/
        if (isSeaching){
            searchView.setIconified(false);
            searchView.setQuery(textSearching,false);
        }
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        if (numeroPedidosPendientes == 0){
            menu.findItem(R.id.menu_pedidos_pendientes).setVisible(false);
        }else{
            menu.findItem(R.id.menu_pedidos_pendientes).setVisible(true);
        }

        if (ventas360App.getModoVenta().equals(VendedorModel.MODO_AUTOVENTA)){
            if (mostrarcheckEntregado) {
                menu.findItem(R.id.menu_pedidos_marcar_entregados).setVisible(false);
                menu.findItem(R.id.menu_pedidos_no_marcar_entregados).setVisible(true);
            }else{
                menu.findItem(R.id.menu_pedidos_marcar_entregados).setVisible(true);
                menu.findItem(R.id.menu_pedidos_no_marcar_entregados).setVisible(false);
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id){
            case R.id.menu_pedidos_marcar_entregados:
                mostrarcheckEntregado = true;
                ventas360App.setMarcarPedidosEntregados(true);
                getActivity().invalidateOptionsMenu();
                //listaPedidosCabecera.clear();
                //listaPedidosCabecera.addAll(daoPedido.getPedidosCabecera());
                adapter = new RecyclerViewPedidoAdapter(listaPedidosCabecera,mostrarcheckEntregado,PedidosFragment.this);
                adapter.setQuickAction(quickAction);
                recycler_pedidos.setAdapter(adapter);

                break;
            case  R.id.menu_pedidos_no_marcar_entregados:
                mostrarcheckEntregado = false;
                ventas360App.setMarcarPedidosEntregados(false);
                getActivity().invalidateOptionsMenu();
                adapter = new RecyclerViewPedidoAdapter(listaPedidosCabecera,mostrarcheckEntregado,PedidosFragment.this);
                adapter.setQuickAction(quickAction);
                recycler_pedidos.setAdapter(adapter);

                break;
        }
        return super.onOptionsItemSelected(item);
    }

    /*Metodo para mostrar la lista de pendientes*/
    public void showPedidosPendientesDialog(){
        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View alertLayout = inflater.inflate(R.layout.dialog_recyclerview,null);
        final RecyclerView recyclerPendientes = alertLayout.findViewById(R.id.recycler_lista);

        RecyclerViewPedidoAdapter adapter = new RecyclerViewPedidoAdapter(listaPedidosPendientes, false,PedidosFragment.this);
        recyclerPendientes.setAdapter(adapter);
        recyclerPendientes.addItemDecoration(new DividerItemDecoration(getActivity(),DividerItemDecoration.VERTICAL));
        recyclerPendientes.setPadding(0,25,0,0);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Lista de pedidos pendientes ("+numeroPedidosPendientes+")");
        builder.setView(alertLayout);
        builder.setPositiveButton("ENVIAR AL SERVIDOR", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                EnviarPedidosPendientes();
            }
        });
        builder.setNegativeButton("CANCELAR",null);
        builder.show();
    }

    private void EnviarPedidosPendientes() {

        new AsyncTask<Void,Void,String>(){
            final String ENVIADO = "E";
            final String INCOMPLETO = "I";
            final String PENDIENTE = "P";
            final String TRANSFERIDO = "T";
            final String JSONEXCEPTION = "jsonException";
            final String SIN_CONEXION = "SinConexion";
            final String OTRO_ERROR = "error";
            final String SIN_PENDIENTES = "sinPendientes";

            ProgressDialog pDialog;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                pDialog = new ProgressDialog(getActivity());
                pDialog.setCancelable(false);
                pDialog.setIndeterminate(true);
                pDialog.setMessage("Enviando pendientes...");
                pDialog.show();
            }

            @Override
            protected String doInBackground(Void... strings) {
                Gson gson = new Gson();
                if (Util.isConnectingToRed(getActivity())) {
                    try {
                        ArrayList<DTOPedido> pedidoEnviar = daoPedido.getDTOPedidosPendientes();
                        if (pedidoEnviar.isEmpty()){
                            return SIN_PENDIENTES;
                        }else{
                            String cadena = gson.toJson(pedidoEnviar);
                            String cadenaResultado = soapManager.enviarPendientes(TablesHelper.ObjPedido.ActualizarObjPedido,cadena);
                            return daoPedido.actualizarFlagPedidos(cadenaResultado);
                        }
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
                        showDialogoPostEnvioLista("Envío sactisfactorio","Los pedidos fueron ingresados al servidor", R.drawable.ic_dialog_check);
                        break;
                    case INCOMPLETO:
                        showDialogoPostEnvioLista("Atención","No se pudieron guardar todos los datos", R.drawable.ic_dialog_alert);
                        break;
                    case PENDIENTE:
                        showDialogoPostEnvioLista("Atención","El servidor no pudo ingresar los pedidos", R.drawable.ic_dialog_error);
                        break;
                    case TRANSFERIDO:
                        showDialogoPostEnvioLista("Atención","Algunos pedidos se encuentran en proceso de facturación \nComuníquese con el administrador", R.drawable.ic_dialog_block);
                        break;
                    case SIN_CONEXION:
                        showDialogoPostEnvioLista("Sin conexión","Es probable que no tenga acceso a INTERNET, El pedido se guardó localmente", R.drawable.ic_dialog_error);
                        break;
                    case JSONEXCEPTION:
                        showDialogoPostEnvioLista("Atención","Los pedidos fueron enviados pero no se pudo verificar\nConsulte con el administrador", R.drawable.ic_dialog_alert);
                        break;
                    case OTRO_ERROR:
                        showDialogoPostEnvioLista("No se pudo enviar","Verifique que tenga acceso a internet e inténtelo nuevamente. Si el problema persiste, comuníquese con el administrador", R.drawable.ic_dialog_error);
                        break;
                    case SIN_PENDIENTES:
                        Toast.makeText(getActivity(), "No hay pedidos pendientes", Toast.LENGTH_SHORT).show();
                        break;
                    default:
                        showDialogoPostEnvioLista("Error","No se pudo enviar el pedido, se guardó localmente", R.drawable.ic_dialog_error);
                        break;
                }
            }
        }.execute();
    }

    public void EnviarEntregaPedido(final String numeroPedido, final boolean checked, final int position){
        new AsyncTask<Void,Void,String>(){
            final String ENVIADO = "E";
            final String INCOMPLETO = "I";
            final String PENDIENTE = "P";
            final String TRANSFERIDO = "T";
            final String JSONEXCEPTION = "jsonException";
            final String SIN_CONEXION = "SinConexion";
            final String OTRO_ERROR = "error";
            final String SIN_PENDIENTES = "sinPendientes";

            ProgressDialog pDialog;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                pDialog = new ProgressDialog(getActivity());
                pDialog.setCancelable(false);
                pDialog.setIndeterminate(true);
                pDialog.setMessage("Enviando entrega de pedido...");
                pDialog.show();
            }

            @Override
            protected String doInBackground(Void... strings) {
                Gson gson = new Gson();
                if (Util.isConnectingToRed(getActivity())) {
                    try {
                        String flag = "";
                        ArrayList<HashMap<String,Object>> pedidoEnviar = daoPedido.getPedidosEntregados(numeroPedido);
                        if (pedidoEnviar.isEmpty()){
                            return SIN_PENDIENTES;
                        }else{
                            String cadena = gson.toJson(pedidoEnviar);
                            String cadenaResultado = soapManager.actualizarEntregaPedidos(TablesHelper.ObjPedido.ActualizarEntregaPedidos,cadena);
                            flag = daoPedido.obtenerFlagEnvio(cadenaResultado);
                            return flag;
                        }
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

                if( !respuesta.equals(PedidoCabeceraModel.FLAG_ENVIADO)){
                    //Si el flag que se retorna no es el de Enviado, quiere decir que no se ha actualizado el registro del pedido como entregado, y se debe regresar el campo pedidoEntregado al valor anterior
                    daoPedido.actualizarPedidoEntregado(numeroPedido,!checked,"");//Se actualiza el registro con el valor del checked cambiado(el valor que tenía inicialmente)
                    View view = recycler_pedidos.findViewHolderForAdapterPosition(position).itemView;
                    CheckBox checkBox = view.findViewById(R.id.check_entregado);
                    checkBox.setOnCheckedChangeListener(null);//Importante para que no se ejecute el listener
                    checkBox.setChecked(!checked);
                }

                switch (respuesta){
                    case ENVIADO:
                        showDialogoPostEnvioLista("Envío sactisfactorio","Se actualizaron los datos del pedido", R.drawable.ic_dialog_check);
                        break;
                    case INCOMPLETO:
                        showDialogoPostEnvioLista("Atención","No se pudieron guardar todos los datos", R.drawable.ic_dialog_alert);
                        break;
                    case PENDIENTE:
                        showDialogoPostEnvioLista("Atención","No se pudo actualizar el pedido, intente nuevamente", R.drawable.ic_dialog_error);
                        break;
                    case TRANSFERIDO:
                        showDialogoPostEnvioLista("Atención","Algunos pedidos se encuentran en proceso de facturación \nComuníquese con el administrador", R.drawable.ic_dialog_block);
                        break;
                    case SIN_CONEXION:
                        showDialogoPostEnvioLista("Sin conexión","Es probable que no tenga acceso a INTERNET, compruebe su conexión", R.drawable.ic_dialog_error);
                        break;
                    case JSONEXCEPTION:
                        showDialogoPostEnvioLista("Atención","El pedido fue enviado pero no se pudo verificar\nConsulte con el administrador", R.drawable.ic_dialog_alert);
                        break;
                    case OTRO_ERROR:
                        showDialogoPostEnvioLista("No se pudo enviar","Verifique que tenga acceso a internet e inténtelo nuevamente. Si el problema persiste, comuníquese con el administrador", R.drawable.ic_dialog_error);
                        break;
                    case SIN_PENDIENTES:
                        Toast.makeText(getActivity(), "No hay pedidos pendientes", Toast.LENGTH_SHORT).show();
                        break;
                    default:
                        showDialogoPostEnvioLista("Atención !",""+respuesta, R.drawable.ic_dialog_error);
                        break;
                }
            }
        }.execute();
    }

    class async_obtenerPedidos extends AsyncTask<Void,Void,String>{
        ProgressDialog pDialog;
        String errorMensaje = "";
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(getActivity());
            pDialog.setMessage("Obteniendo pedidos...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected String doInBackground(Void... strings) {
            String response = "";
            if (Util.isConnectingToRed(getActivity())) {
                try {
                    soapManager.obtenerRegistrosxVendedorJSON(TablesHelper.ObjPedido.Sincronizar, TablesHelper.ObjPedido.Table);
                    response = "OK";
                } catch (Exception e) {
                    e.printStackTrace();
                    errorMensaje = e.getMessage();
                    Log.e(TAG,e.getMessage()+"");
                    response = "Error";
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
                showDialogoPostEnvioLista("Sin conexión","or el momento no es posible completar la acción, verifique su conexión", R.drawable.ic_dialog_error);
            }else if (result.equals("Error")){
                showDialogoPostEnvioLista("Ocurrió un error",""+errorMensaje, R.drawable.ic_dialog_error);
            }else {
                showDialogoPostEnvioLista("Sincronización correcta","Se obtuvieron los pedidos satisfactoriamente", R.drawable.ic_dialog_check);
            }
        }
    }


    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    public boolean onQueryTextChange(String newText) {
        filtrarCliente(newText);
        textSearching = newText;
        return true;
    }

    public void filtrarCliente(String newText) {
        List<PedidoCabeceraModel> filteredModelList = filtrar(listaPedidosCabecera, newText);
        adapter.setFilter(filteredModelList);
    }

    private List<PedidoCabeceraModel> filtrar(ArrayList<PedidoCabeceraModel> listaPedidos, String query) {
        query = query.toLowerCase();
        final List<PedidoCabeceraModel> listaFiltrada = new ArrayList<>();
        for (PedidoCabeceraModel pedido : listaPedidos) {

            //Si se busca por codigo
            if (TextUtils.isDigitsOnly(query)) {
                String codigo = pedido.getIdCliente();
                if (codigo.contains(query)) {
                    listaFiltrada.add(pedido);
                }
            } else {
                //De lo contrario se filtra por el nombre
                String descripcion = pedido.getNombreCliente().toLowerCase();
                if (descripcion.contains(query.toLowerCase().trim())) {
                    listaFiltrada.add(pedido);
                }
            }
        }
        return listaFiltrada;
    }

}

