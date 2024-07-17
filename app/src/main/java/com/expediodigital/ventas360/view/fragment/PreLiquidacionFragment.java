package com.expediodigital.ventas360.view.fragment;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.annotation.DrawableRes;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.fragment.app.Fragment;
import androidx.core.view.MenuItemCompat;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.SearchView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.expediodigital.ventas360.DAO.DAOConfiguracion;
import com.expediodigital.ventas360.DAO.DAOPedido;
import com.expediodigital.ventas360.DAO.DAOProducto;
import com.expediodigital.ventas360.R;
import com.expediodigital.ventas360.adapter.RecyclerViewLiquidacionAdapter;
import com.expediodigital.ventas360.model.GuiaModel;
import com.expediodigital.ventas360.model.LiquidacionProductoModel;
import com.expediodigital.ventas360.model.PedidoCabeceraModel;
import com.expediodigital.ventas360.util.SoapManager;
import com.expediodigital.ventas360.util.TablesHelper;
import com.expediodigital.ventas360.util.Util;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Kevin Robinson Meza Hinostroza on agosto 2017.
 * Expedio Digital
 * Kevin.Meza@expediodigital.com
 */

public class PreLiquidacionFragment extends Fragment implements SearchView.OnQueryTextListener {
    public static final String TAG = "ProductoFragment";
    RecyclerView recycler_producto;
    FloatingActionButton fab_obtenerLiquidacion;
    RecyclerViewLiquidacionAdapter adapter;
    DAOPedido daoPedido;
    DAOProducto daoProducto;
    DAOConfiguracion daoConfiguracion;
    SoapManager soap_manager;
    DecimalFormat formateador;

    ArrayList<LiquidacionProductoModel> listaProducto = new ArrayList<>();
    SearchView searchView;
    TextView tv_numeroGuia,tv_cantidadTotal,tv_montoTotal;

    GuiaModel guiaModel;

    public PreLiquidacionFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_pre_liquidacion, container, false);
        setHasOptionsMenu(true);
        Util.actualizarToolBar("Pre Liquidación",false,getActivity());

        daoConfiguracion = new DAOConfiguracion(getActivity());
        daoProducto = new DAOProducto(getActivity());
        daoPedido = new DAOPedido(getActivity());
        soap_manager = new SoapManager(getContext());

        recycler_producto = view.findViewById(R.id.recycler_producto);
        fab_obtenerLiquidacion = view.findViewById(R.id.fab_obtenerLiquidacion);
        tv_numeroGuia = view.findViewById(R.id.tv_numeroGuia);
        tv_cantidadTotal = view.findViewById(R.id.tv_cantidadTotal);
        tv_montoTotal = view.findViewById(R.id.tv_montoTotal);

        adapter = new RecyclerViewLiquidacionAdapter(listaProducto, getActivity());

        recycler_producto.addItemDecoration(new DividerItemDecoration(getActivity(),DividerItemDecoration.VERTICAL));
        recycler_producto.setAdapter(adapter);
        /*
        fab_devoluciones.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fab_menu.close(true);


            }
        });
        */

        fab_obtenerLiquidacion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new async_obtenerLiquidacion().execute();
            }
        });

        formateador = Util.formateador();

        refreshLista();
        return view;
    }

    public void refreshLista(){
        listaProducto.clear();
        listaProducto.addAll(daoProducto.getLiquidacionGuia());
        adapter.notifyDataSetChanged();

        /*Se obtiene el número de guía de los productos (todos tienen el mismo, asi que solo se obtiene del primero),
        esto para ver en caso el numero de guía que se tiene en el Dispositivo es distinto al número de guía
        con el que llegó la liquidación(Tal vez se abrió una nueva guía)*/
        if (!listaProducto.isEmpty()){
            tv_numeroGuia.setText("Número de Guía: "+listaProducto.get(0).getNumeroDocumento());
        }
        int numeroProductos = listaProducto.size();
        tv_cantidadTotal.setText(String.valueOf(numeroProductos));


        ArrayList<PedidoCabeceraModel> listaPedidosCabecera = daoPedido.getPedidosCabecera();
        int numeroPedidos = listaPedidosCabecera.size();
        double importeTotal = 0;
        double pesoTotal = 0;

        for (PedidoCabeceraModel pedido : listaPedidosCabecera){
            if (!pedido.getEstado().equals("A")){
                importeTotal += pedido.getImporteTotal();
                pesoTotal += pedido.getPesoTotal();
            }
        }
        tv_montoTotal.setText("S/. "+formateador.format(importeTotal));
    }

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_productos, menu);

        MenuItem itemSearch = menu.findItem(R.id.action_search);
        searchView = (SearchView) MenuItemCompat.getActionView(itemSearch);
        searchView.setOnQueryTextListener(this);
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        filtrarProducto(newText);
        return true;
    }

    public void filtrarProducto(String newText) {
        List<LiquidacionProductoModel> filteredModelList = filtrar(listaProducto, newText);
        adapter.setFilter(filteredModelList);
    }
    private List<LiquidacionProductoModel> filtrar(ArrayList<LiquidacionProductoModel> listaProducto, String query) {
        query = query.toLowerCase();
        final List<LiquidacionProductoModel> listaFiltrada = new ArrayList<>();
        for (LiquidacionProductoModel producto : listaProducto) {

            //Si se busca por codigo
            if (TextUtils.isDigitsOnly(query)) {
                String codigo = producto.getIdProducto();
                if (codigo.contains(query)) {
                    listaFiltrada.add(producto);
                }
            } else {
                //De lo contrario se filtra por el nombre
                String descripcion = producto.getDescripcion().toLowerCase();
                if (descripcion.contains(query)) {
                    listaFiltrada.add(producto);
                }
            }
        }
        return listaFiltrada;
    }

    class async_obtenerLiquidacion extends AsyncTask<String, Void, String> {
        ProgressDialog progressDialog;
        String errorMensaje = "";
        protected void onPreExecute() {
            progressDialog = new ProgressDialog(getActivity());
            progressDialog.setTitle("");
            progressDialog.setMessage("Obteniendo liquidación...");
            progressDialog.setIndeterminate(true);
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        protected String doInBackground(String... strings) {
            String response = "";
            if (Util.isConnectingToRed(getActivity())) {
                try {
                    soap_manager.obtenerRegistrosxVendedorJSON( TablesHelper.Liquidacion.Sincronizar, TablesHelper.Liquidacion.Table);
                    response = "OK";
                } catch (Exception e) {
                    e.printStackTrace();
                    errorMensaje = e.getMessage();
                    response = "Error";
                }
            } else {
                // Sin conexion al Servidor
                response = "NoInternet";
            }
            return response;
        }

        protected void onPostExecute(String result) {
            progressDialog.dismiss();
            if (result.equals("NoInternet")){
                showDialogo("Sin conexión","Por el momento no es posible completar la acción, verifique su conexión", R.drawable.ic_dialog_error);
            }else if (result.equals("Error")){
                showDialogo("Ocurrió un error",""+errorMensaje, R.drawable.ic_dialog_error);
            }else {
                showDialogo("Sincronización correcta","Se obtuvo la liquidación satisfactoriamente", R.drawable.ic_dialog_check);
            }
        }
    }

    private void showDialogo(String titulo, String mensaje,@DrawableRes int icon) {
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

}
