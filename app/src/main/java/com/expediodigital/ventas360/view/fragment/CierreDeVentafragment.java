package com.expediodigital.ventas360.view.fragment;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SearchView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.expediodigital.ventas360.DAO.DAOCliente;
import com.expediodigital.ventas360.DAO.DAOConfiguracion;
import com.expediodigital.ventas360.DAO.DAOPedido;
import com.expediodigital.ventas360.DAO.DAOProducto;
import com.expediodigital.ventas360.R;
import com.expediodigital.ventas360.Ventas360App;
import com.expediodigital.ventas360.model.GuiaModel;
import com.expediodigital.ventas360.model.VendedorModel;
import com.expediodigital.ventas360.util.SoapManager;
import com.expediodigital.ventas360.util.TablesHelper;
import com.expediodigital.ventas360.util.Util;

import java.text.DecimalFormat;

/**
 * Created by Monica Toribio Rojas on julio 2017.
 * Expedio Digital
 * monica.toribio.rojas@gmail.com
 */

public class CierreDeVentafragment extends Fragment implements SearchView.OnQueryTextListener  {
    public static final String TAG = "CierreDeVentafragment";
    DAOProducto daoProducto;
    DAOConfiguracion daoConfiguracion;
    DAOCliente daoCliente;
    DAOPedido daoPedido;
    GuiaModel guiaModel;
    Ventas360App ventas360App;

    SoapManager soap_manager;
    ProgressDialog progressDialog;

    //declarar variable
    TableRow row_guia;
    TextView tv_numeroGuia;
    TextView label_tipoVendedor,tv_nombreVendedor;
    TextView tv_fechaAp;
    TextView tv_pDisponble;
    TextView tv_mensaje;
    TextView tv_clientesProgramados, tv_clientesVisitados, tv_cantidadUnidadMayor, tv_cantidadUnidadMenor, tv_importeTotalVentas;
    Button btn_cerraVenta;

    String mensaje;
    DecimalFormat formateador;

    //data para el fragment - clientes en cardview

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        View view = inflater.inflate(R.layout.cierredeventa_fragment, container, false);
        setHasOptionsMenu(true);

        Util.actualizarToolBar("Cierre de Venta",false,getActivity());
        ventas360App    = (Ventas360App) getActivity().getApplicationContext();

        daoProducto = new DAOProducto(getActivity());
        daoConfiguracion = new DAOConfiguracion(getActivity());
        daoCliente = new DAOCliente(getActivity());
        daoPedido = new DAOPedido(getActivity());

        row_guia                = view.findViewById(R.id.row_guia);
        tv_numeroGuia           = view.findViewById(R.id.tv_numeroGuia);
        tv_fechaAp              = view.findViewById(R.id.tv_fechaAp);
        tv_pDisponble           = view.findViewById(R.id.tv_pDisponble);
        label_tipoVendedor      = view.findViewById(R.id.label_tipoVendedor);
        tv_nombreVendedor       = view.findViewById(R.id.tv_nombreVendedor);
        tv_mensaje              = view.findViewById(R.id.tv_mensaje);
        tv_clientesProgramados  = view.findViewById(R.id.tv_clientesProgramados);
        tv_clientesVisitados    = view.findViewById(R.id.tv_clientesVisitados);
//        tv_cantidadUnidadMayor  = view.findViewById(R.id.tv_cantidadUnidadMayor);
//        tv_cantidadUnidadMenor  = view.findViewById(R.id.tv_cantidadUnidadMenor);
        tv_importeTotalVentas   = view.findViewById(R.id.tv_importeTotalVentas);
        btn_cerraVenta          = view.findViewById(R.id.btn_cerraVentas);

        soap_manager    = new SoapManager(getActivity());

        btn_cerraVenta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cerrarVentas();
            }
        });

        formateador = Util.formateador();

        try{
            if (daoConfiguracion.getGuiasOperativas().size()>1){
                tv_mensaje.setText("Se ha encontrado mas de una Guia abierta al mismo tiempo");
            }
        }catch (Exception e){
            e.printStackTrace();
        }

        tv_nombreVendedor.setText(ventas360App.getNombreVendedor());

        if (ventas360App.getModoVenta().equals(VendedorModel.MODO_PREVENTA)){
            label_tipoVendedor.setText("Jefe de Unidad de Negocio: ");
//            label_tipoVendedor.setText("Vendedor");
            row_guia.setVisibility(View.GONE);
            btn_cerraVenta.setText("Cerrar ventas");
            tv_fechaAp.setText(daoConfiguracion.getFechaString());
            btn_cerraVenta.setEnabled(true);
        }

        guiaModel = daoProducto.obtenerDetalleguia();
        if(guiaModel!= null){
            tv_numeroGuia.setText(guiaModel.getNumeroguia());
            tv_fechaAp.setText(guiaModel.getFechaCarga());
            tv_pDisponble.setText(guiaModel.getProductoDisponible()+"");
            btn_cerraVenta.setEnabled(true);
        }

        cargarResumenVentas();
        return view;
    }

    public void cargarResumenVentas() {
        int cantidadClientesOrdenados = daoCliente.getClientesOrdenados().size();
        int cantidadClientesSinUbicacion = daoCliente.getClientesOrdenadosSinUbicacion(cantidadClientesOrdenados).size();
        int cantidadClientesProgramados = cantidadClientesOrdenados + cantidadClientesSinUbicacion;
        int cantidadClientesVisitados = daoCliente.getNumeroClientesVisitados(ventas360App.getModoVenta(), daoConfiguracion.getEstadoVendedor(ventas360App.getIdEmpresa(),ventas360App.getIdSucursal(),ventas360App.getIdVendedor()));

        int cantidadUnidadMayor = daoPedido.getCantidadPaquetesTotal();
        int cantidadUnidadMenor = daoPedido.getCantidadUnidadesTotal();
        double importeTotalVentas = daoPedido.getImporteTotalVentas();
        tv_clientesProgramados.setText(String.valueOf(cantidadClientesProgramados));
        tv_clientesVisitados.setText(String.valueOf(cantidadClientesVisitados));
//        tv_cantidadUnidadMayor.setText(String.valueOf(cantidadUnidadMayor));
//        tv_cantidadUnidadMenor.setText(String.valueOf(cantidadUnidadMenor));
        tv_importeTotalVentas.setText("S/. "+formateador.format(importeTotalVentas));
    }

    private void cerrarVentas() {
        if (ventas360App.getTipoVendedor().equals(VendedorModel.TIPO_TRANSPORTISTA)){
            AlertDialog.Builder alerta = new AlertDialog.Builder(getActivity());
            alerta.setTitle("¿Desea cerrar la Guía?");
            alerta.setMessage("Las ventas se cerrarán y ningún vendedor podrá enviar nuevos pedidos");
            //alerta.setIcon(R.drawable.ic_dialog_alert);
            alerta.setCancelable(false);
            alerta.setPositiveButton("ACEPTAR", new DialogInterface.OnClickListener(){
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (guiaModel!=null){
                        if(guiaModel.getEstado().equals("O")){
                            Boolean respuesta = daoProducto.actualizarEstadoGuia(guiaModel.getNumeroguia(), "P");
                            Log.i(TAG,""+respuesta );
                        }

                        new asyncCerrarGuia().execute("", "");
                    }
                }
            });
            alerta.setNegativeButton("CANCELAR", null);
            alerta.show();
        }else {
            if(daoConfiguracion.getEstadoVendedor(ventas360App.getIdEmpresa(),ventas360App.getIdSucursal(),ventas360App.getIdVendedor()).equals("O")){
                AlertDialog.Builder alerta = new AlertDialog.Builder(getActivity());
                alerta.setTitle("¿Desea cerrar las ventas?");
                alerta.setMessage("Las ventas se cerrarán y ya no podrá enviar nuevos pedidos");
                //alerta.setIcon(R.drawable.ic_dialog_alert);
                alerta.setCancelable(false);
                alerta.setPositiveButton("ACEPTAR", new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        new asyncCerrarVentas().execute("", "");
                    }
                });
                alerta.setNegativeButton("CANCELAR", null);
                alerta.show();
            }else{
                Toast.makeText(getActivity(),"Las ventas se encuentran cerradas",Toast.LENGTH_SHORT).show();
            }
        }

    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        return false;
    }


    class asyncCerrarGuia extends AsyncTask<String, String, String> {
        String respuesta;

        protected void onPreExecute() {
            progressDialog = new ProgressDialog(getActivity());
            progressDialog.setTitle("");
            progressDialog.setMessage("Sincronizando....");
            progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            progressDialog.setIndeterminate(false);
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        protected String doInBackground(String... params) {

            try {
              respuesta = soap_manager.enviarCierreVenta(TablesHelper.Guia.CerrarGuia);
              Log.e(TAG,""+respuesta);

            } catch (Exception e) {
                e.printStackTrace();
                return e.getMessage();
            }
            return "asyncCerrarGuia Ok";
        }

        protected void onProgressUpdate(String... progress) {
            progressDialog.setProgress(Integer.parseInt(progress[0]));
        }

        protected void onPostExecute(String result) {
            progressDialog.dismiss();
            Log.d(TAG, "onPostExecute " + result);

            if(respuesta.equals("0")){
                mensaje = "La Guia ya se encuentra cerrada";

            }else if(respuesta.equals("C")){
                daoProducto.actualizarEstadoGuia(guiaModel.getNumeroguia(), "C");
                mensaje = "La Guia se cerró satisfactoriamente";

            }else {
                mensaje = "No se pudo cerrar la Guía, inténtelo nuevamente.";
            }

            if (result.equals("asyncCerrarGuia Ok")) {
                AlertDialog.Builder alerta = new AlertDialog.Builder(getActivity());
                alerta.setTitle("Guia Cerrada");
                alerta.setMessage(mensaje);
                alerta.setIcon(R.drawable.ic_dialog_check);
                alerta.setCancelable(false);
                alerta.setPositiveButton("ACEPTAR", null);
                alerta.show();
            } else {
                AlertDialog.Builder alerta = new AlertDialog.Builder(getActivity());
                alerta.setTitle("No se pudo cerrar la Guía");
                alerta.setMessage(mensaje + "\"" + result + "\"");
                alerta.setIcon(R.drawable.ic_dialog_error);
                alerta.setCancelable(false);
                alerta.setPositiveButton("ACEPTAR", null);
                alerta.show();
            }
        }
    }

    class asyncCerrarVentas extends AsyncTask<String, String, String> {
        String respuesta;

        protected void onPreExecute() {
            progressDialog = new ProgressDialog(getActivity());
            progressDialog.setMessage("Cerrando ventas....");
            progressDialog.setIndeterminate(true);
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        protected String doInBackground(String... params) {
            try {
                respuesta = soap_manager.enviarCierreVenta(TablesHelper.Vendedor.CerrarVentas);
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
                mensaje = "Las ventas ya se encuentran cerradas";

            }else if(respuesta.equals("C")){
                daoConfiguracion.actualizarEstadoVendedor(ventas360App.getIdEmpresa(),ventas360App.getIdSucursal(),ventas360App.getIdVendedor(), "C");
                mensaje = "Las ventas fueron cerradas satisfactoriamente";
            }else {
                mensaje = "No se pudo cerrar las ventas, inténtelo nuevamente.";
            }

            if (result.equals("asyncCerrarGuia Ok")) {
                AlertDialog.Builder alerta = new AlertDialog.Builder(getActivity());
                alerta.setTitle("Ventas Cerradas");
                alerta.setMessage(mensaje);
                alerta.setIcon(R.drawable.ic_dialog_check);
                alerta.setCancelable(false);
                alerta.setPositiveButton("ACEPTAR", null);
                alerta.show();
            } else {
                AlertDialog.Builder alerta = new AlertDialog.Builder(getActivity());
                alerta.setTitle("No se pudo cerrar las ventas");
                alerta.setMessage(mensaje + "\"" + result + "\"");
                alerta.setIcon(R.drawable.ic_dialog_error);
                alerta.setCancelable(false);
                alerta.setPositiveButton("ACEPTAR", null);
                alerta.show();
            }
        }
    }
}
