package com.expediodigital.ventas360.view.fragment;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.annotation.DrawableRes;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.fragment.app.Fragment;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.expediodigital.ventas360.DAO.DAOConfiguracion;
import com.expediodigital.ventas360.DAO.DAODevolucion;
import com.expediodigital.ventas360.DAO.DAOProducto;
import com.expediodigital.ventas360.DTO.DTODevolucion;
import com.expediodigital.ventas360.R;
import com.expediodigital.ventas360.Ventas360App;
import com.expediodigital.ventas360.adapter.RecyclerViewDevolucionAdapter;
import com.expediodigital.ventas360.model.DevolucionCabeceraModel;
import com.expediodigital.ventas360.model.DevolucionDetalleModel;
import com.expediodigital.ventas360.model.GuiaModel;
import com.expediodigital.ventas360.model.PedidoCabeceraModel;
import com.expediodigital.ventas360.util.SoapManager;
import com.expediodigital.ventas360.util.TablesHelper;
import com.expediodigital.ventas360.util.Util;
import com.google.gson.Gson;
import com.google.gson.JsonParseException;

import java.util.ArrayList;

/**
 * Created by Kevin Robinson Meza Hinostroza on agosto 2017.
 * Expedio Digital
 * Kevin.Meza@expediodigital.com
 */

public class DevolucionesFragment extends Fragment {
    public static final String TAG = "DevolucionesFragment";
    RecyclerView recycler_producto;
    FloatingActionButton fab_enviarDevoluciones;
    DAOProducto daoProducto;
    DAOConfiguracion daoConfiguracion;
    DAODevolucion daoDevolucion;
    GuiaModel guiaModel;

    RecyclerViewDevolucionAdapter adapter;
    ProgressDialog progressDialog;
    SoapManager soap_manager;

    ArrayList<DevolucionDetalleModel> listaProducto = new ArrayList<>();
    TextView tv_numeroGuia,tv_estadoDevolucion, tv_cantidadTotal;
    Ventas360App ventas360App;

    public DevolucionesFragment() {
    }
    /*AL ABRIR QUE SE SINCRONICE AUTOMÁTICAMENTE LAS TABLAS DE DEVOLUCION Y KARDEX*/
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_devoluciones, container, false);
        setHasOptionsMenu(true);
        Util.actualizarToolBar("Devoluciones",false,getActivity());

        ventas360App = (Ventas360App) getActivity().getApplicationContext();
        daoProducto = new DAOProducto(getActivity());
        daoConfiguracion = new DAOConfiguracion(getActivity());
        daoDevolucion = new DAODevolucion(getActivity());
        soap_manager = new SoapManager(getContext());

        recycler_producto = view.findViewById(R.id.recycler_producto);
        tv_numeroGuia = view.findViewById(R.id.tv_numeroGuia);
        tv_estadoDevolucion = view.findViewById(R.id.tv_estadoDevolucion);
        tv_cantidadTotal = view.findViewById(R.id.tv_cantidadTotal);

        adapter = new RecyclerViewDevolucionAdapter(listaProducto, this);

        recycler_producto.addItemDecoration(new DividerItemDecoration(getActivity(),DividerItemDecoration.VERTICAL));
        recycler_producto.setAdapter(adapter);

        fab_enviarDevoluciones = view.findViewById(R.id.fab_enviarDevoluciones);

        fab_enviarDevoluciones.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setIcon(R.drawable.ic_dialog_alert);
                builder.setTitle("¿Enviar devoluciones?");
                builder.setMessage("La lista de devoluciones para la guía ("+guiaModel.getNumeroguia()+") se enviará al servidor y en caso exista una lista, se reemplazará completamente por esta.");
                builder.setCancelable(false);
                builder.setPositiveButton("ENVIAR", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        EnviarDevoluciones(guiaModel.getNumeroguia());
                    }
                });
                builder.setNegativeButton("CANCELAR",null);
                builder.show();
            }
        });

        new async_obtenerStockActualizado().execute();
        return view;
    }

    private void verificarGuia() {

        ArrayList<GuiaModel> listaGuias =  daoConfiguracion.getGuias();
        if (!listaGuias.isEmpty()){
            guiaModel = listaGuias.get(0);
            tv_numeroGuia.setText("Número de Guía: "+guiaModel.getNumeroguia());
            if (guiaModel.getEstado().equals(GuiaModel.ESTADO_OPERANDO)){
                DevolucionCabeceraModel devolucionCabecera = daoDevolucion.getDevolucionCabecera(guiaModel.getNumeroguia());
                /*La devolucion que se registrará reemplazará a la anterior, si es que se tenía uno (Flag Enviado). Solo habrá una devolución por Guia*/
                if (devolucionCabecera != null){
                    if (devolucionCabecera.getFlag().equals(DevolucionCabeceraModel.FLAG_ENVIADO)){
                        tv_estadoDevolucion.setText("Esta Guía tiene una devolución registrada. Si procesa una nueva, se reemplazará la anterior completamente");
                        tv_estadoDevolucion.setTextColor(ContextCompat.getColor(getActivity(),R.color.amber_500));
                    }
                }else{
                    tv_estadoDevolucion.setText("Esta Guía no presenta ninguna devolución actualmente");
                    tv_estadoDevolucion.setTextColor(ContextCompat.getColor(getActivity(), androidx.appcompat.R.color.abc_secondary_text_material_light));
                }

                ArrayList<DevolucionDetalleModel> productosDevolucionKardex = daoProducto.getProductosDevolucionKardex();
                daoDevolucion.guardarDevolucionTemporal(guiaModel.getNumeroguia(),productosDevolucionKardex,ventas360App.getIdVendedor());
                //Los productos a mostrar en la vista son los que se obtienen desde el Kardex.
                refreshLista();
            }else{
                //No se puede hacer devoluciones con la guia cerrada
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setIcon(R.drawable.ic_dialog_alert);
                builder.setTitle("Guía cerrada");
                builder.setMessage("La Guía "+guiaModel.getNumeroguia()+" está cerrada. No se puede registrar devoluciones con la guía cerrada");
                builder.setCancelable(false);
                builder.setPositiveButton("ACEPTAR", null);
                builder.setNeutralButton("ABRIR GUÍA", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        new asyncAbrirGuia().execute();
                    }
                });
                builder.show();
            }
        }else{
            //No hay guias disponibles
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setIcon(R.drawable.ic_dialog_error);
            builder.setTitle("No hay guias disponibles");
            builder.setMessage("No se encontró ninguna guía, comuníquese con el Administrador");
            builder.setCancelable(false);
            builder.setPositiveButton("ACEPTAR", null);
            builder.show();
        }

    }

    public void refreshLista(){
        listaProducto.clear();
        listaProducto.addAll(daoDevolucion.getListaProductoDevolucion(guiaModel.getNumeroguia()));
        adapter.notifyDataSetChanged();

        int numeroProductos = listaProducto.size();
        tv_cantidadTotal.setText(String.valueOf(numeroProductos));
    }

    public void editarCantidad(){
        final DevolucionDetalleModel productoDevolucion = adapter.getItemSelected();

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Modificar cantidad devolución");
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View pop = inflater.inflate(R.layout.dialog_editar_devolucion,null);
        builder.setView(pop);

        TextView tv_producto = (TextView) pop.findViewById(R.id.tv_producto);
        TextView tv_unidadMedidaMayor = (TextView) pop.findViewById(R.id.tv_unidadMedidaMayor);
        TextView tv_unidadMedidaMenor = (TextView) pop.findViewById(R.id.tv_unidadMedidaMenor);
        final EditText edt_cantidadUnidadMayor = (EditText) pop.findViewById(R.id.edt_cantidadUnidadMayor);
        final EditText edt_cantidadUnidadMenor = (EditText) pop.findViewById(R.id.edt_cantidadUnidadMenor);

        tv_producto.setText(productoDevolucion.getDescripcion());
        tv_unidadMedidaMayor.setText(productoDevolucion.getUnidadMedidaMayor());
        tv_unidadMedidaMenor.setText(productoDevolucion.getUnidadMedidaMenor());
        edt_cantidadUnidadMayor.setText(String.valueOf(productoDevolucion.getStockDevolucionUnidadMayor()));
        edt_cantidadUnidadMenor.setText(String.valueOf(productoDevolucion.getStockDevolucionUnidadMenor()));

        builder.setPositiveButton("ACEPTAR", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int x) {
                int cantidadUnidadMayor = 0;
                if (!edt_cantidadUnidadMayor.getText().toString().isEmpty()){
                    cantidadUnidadMayor = Integer.parseInt(edt_cantidadUnidadMayor.getText().toString());
                }

                int cantidadUnidadMenor = 0;
                if (!edt_cantidadUnidadMenor.getText().toString().isEmpty()){
                    cantidadUnidadMenor = Integer.parseInt(edt_cantidadUnidadMenor.getText().toString());
                }
                DevolucionDetalleModel item = new DevolucionDetalleModel();
                item.setNumeroGuia(guiaModel.getNumeroguia());
                item.setIdProducto(productoDevolucion.getIdProducto());
                item.setIdUnidadMedidaMayor(productoDevolucion.getIdUnidadMedidaMayor());
                item.setStockDevolucionUnidadMayor(cantidadUnidadMayor);
                item.setIdUnidadMedidaMenor(productoDevolucion.getIdUnidadMedidaMenor());
                item.setStockDevolucionUnidadMenor(cantidadUnidadMenor);
                item.setModificado(1);
                item.setFlag(PedidoCabeceraModel.FLAG_PENDIENTE);

                daoDevolucion.modificarItemDetalleDevolucion(item);

                refreshLista();
            }
        });
        builder.setNegativeButton("CANCELAR", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
        builder.create().show();
    }

    class async_obtenerDevoluciones extends AsyncTask<String, Void, Boolean> {
        protected void onPreExecute() {
            progressDialog = new ProgressDialog(getActivity());
            progressDialog.setTitle("");
            progressDialog.setMessage("Obteniendo productos...");
            progressDialog.setIndeterminate(true);
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        protected Boolean doInBackground(String... strings) {
            try {
                soap_manager.obtenerRegistrosxVendedorJSON( TablesHelper.ObjDevolucion.Sincronizar, TablesHelper.ObjDevolucion.Table);
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }

        protected void onPostExecute(Boolean result) {
            progressDialog.dismiss();
            verificarGuia();
            if (result){
                Toast.makeText(getActivity(),"Devoluciones actualizadas",Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(getActivity(),"No se pudo obtener las devoluciones",Toast.LENGTH_SHORT).show();
            }
        }

    }

    class async_obtenerStockActualizado extends AsyncTask<String, Void, Boolean> {
        protected void onPreExecute() {
            progressDialog = new ProgressDialog(getActivity());
            progressDialog.setTitle("");
            progressDialog.setMessage("Obteniendo productos...");
            progressDialog.setIndeterminate(true);
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        protected Boolean doInBackground(String... strings) {
            try {
                soap_manager.obtenerRegistrosxVendedorJSON( TablesHelper.Kardex.Sincronizar, TablesHelper.Kardex.Table);
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }

        protected void onPostExecute(Boolean result) {
            progressDialog.dismiss();
            if (result){
                Toast.makeText(getActivity(),"Kardex actualizado",Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(getActivity(),"No se pudo obtener kardex",Toast.LENGTH_SHORT).show();
            }
            new async_obtenerDevoluciones().execute();
        }

    }

    class asyncAbrirGuia extends AsyncTask<String, String, String> {
        ProgressDialog progressDialog;
        String respuesta="";
        String mensaje="";
        @DrawableRes int icon;

        protected void onPreExecute() {
            progressDialog = new ProgressDialog(getActivity());
            progressDialog.setTitle("");
            progressDialog.setMessage("Abriendo Guía...");
            progressDialog.setIndeterminate(true);
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        protected String doInBackground(String... params) {

            try {
                respuesta = soap_manager.enviarAperturaVenta(TablesHelper.Guia.AbrirGuia);
                Log.e(TAG,""+respuesta);

            } catch (Exception e) {
                e.printStackTrace();
                return e.getMessage();
            }
            return "asyncCerrarGuia Ok";
        }

        protected void onPostExecute(String result) {
            progressDialog.dismiss();
            Log.d(TAG, "onPostExecute " + result);

            if(respuesta.equals("0")){
                mensaje = "La Guia ya se encuentra operativa";
                icon = R.drawable.ic_dialog_alert;
            }else if(respuesta.equals(GuiaModel.ESTADO_OPERANDO)){
                daoProducto.actualizarEstadoGuia(guiaModel.getNumeroguia(), "O");
                mensaje = "La Guía ahora se encuentra operativa";
                icon = R.drawable.ic_dialog_check;
            }else if(respuesta.equals("T")) {
                mensaje = "La Guía ya ha sido transferida al Sistema Principal y no puede ser modificada, comuníquese con el Administrador.";
                icon = R.drawable.ic_dialog_block;
            }else {
                mensaje = "No se pudo abrir la Guía, inténtelo nuevamente.";
                icon = R.drawable.ic_dialog_error;
            }

            if (respuesta.isEmpty()) {
                AlertDialog.Builder alerta = new AlertDialog.Builder(getActivity());
                alerta.setTitle("No se pudo abrir la Guía");
                alerta.setMessage(mensaje + "\n\"" + result + "\"");
                alerta.setIcon(R.drawable.ic_dialog_error);
                alerta.setCancelable(false);
                alerta.setPositiveButton("ACEPTAR", null);
                alerta.show();
            } else {
                AlertDialog.Builder alerta = new AlertDialog.Builder(getActivity());
                alerta.setTitle("Importante");
                alerta.setMessage(mensaje);
                alerta.setIcon(icon);
                alerta.setCancelable(false);
                alerta.setPositiveButton("ACEPTAR", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        verificarGuia();
                    }
                });
                alerta.show();
            }
        }
    }

    private void EnviarDevoluciones(final String numeroGuia) {

        new AsyncTask<Void,Void,String>(){
            final String ENVIADO = "E";
            final String INCOMPLETO = "I";
            final String PENDIENTE = "P";
            final String JSONEXCEPTION = "jsonException";
            final String SIN_CONEXION = "SinConexion";
            final String OTRO_ERROR = "error";

            ProgressDialog pDialog;
            String cadenaResultado = "";

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                pDialog = new ProgressDialog(getActivity());
                pDialog.setCancelable(false);
                pDialog.setIndeterminate(true);
                pDialog.setMessage("Enviando devoluciones...");
                pDialog.show();
            }

            @Override
            protected String doInBackground(Void... strings) {
                //GuardarPedido();//EL pedido ya está guardado
                SoapManager soapManager = new SoapManager(getActivity());
                Gson gson = new Gson();
                if (Util.isConnectingToRed(getActivity())) {
                    try {
                        daoDevolucion.actualizarFechaDevolucion(numeroGuia,daoConfiguracion.getFechaHoraString());
                        ArrayList<DTODevolucion> devolucion = daoDevolucion.getDTODevolucionCompleto(numeroGuia);
                        String cadena = gson.toJson(devolucion);
                        cadenaResultado = soapManager.enviarPendientes(TablesHelper.ObjDevolucion.ActualizarObjDevolucion,cadena);

                        return daoDevolucion.actualizarFlagDevolucion(cadenaResultado);//Retorna el flag resultado
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
                        showDialogoPostEnvio("Envío satisfactorio","La devolucion fue ingresada al servidor", R.drawable.ic_dialog_check);
                        break;
                    case INCOMPLETO:
                        showDialogoPostEnvio("Atención","No se pudieron guardar todos los datos", R.drawable.ic_dialog_alert);
                        break;
                    case PENDIENTE:
                        showDialogoPostEnvio("Atención","El servidor no pudo ingresar la devolucion", R.drawable.ic_dialog_error);
                        break;
                    case SIN_CONEXION:
                        showDialogoPostEnvio("Sin conexión","Es probable que no tenga acceso a INTERNET, vuelva a intentarlo", R.drawable.ic_dialog_error);
                        break;
                    case JSONEXCEPTION:
                        showDialogoPostEnvio("Atención","La devolución fue enviada pero no se pudo verificar \nConsulte con el administrador", R.drawable.ic_dialog_alert);
                        break;
                    case OTRO_ERROR:
                        showDialogoPostEnvio("Error","No se pudo enviar la devolución, inténtelo nuevamente", R.drawable.ic_dialog_error);
                        break;
                }
            }
        }.execute();
    }
    private void showDialogoPostEnvio(String titulo, String mensaje,@DrawableRes int icon) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(titulo);
        builder.setMessage(mensaje);
        builder.setIcon(icon);
        builder.setCancelable(false);
        builder.setPositiveButton("ACEPTAR", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog,int which) {
                verificarGuia();
            }
        });
        builder.show();
    }

}
