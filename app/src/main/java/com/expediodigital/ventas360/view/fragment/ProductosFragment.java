package com.expediodigital.ventas360.view.fragment;

import android.app.ProgressDialog;
import android.content.pm.ActivityInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.fragment.app.Fragment;
import androidx.core.view.MenuItemCompat;
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
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import com.expediodigital.ventas360.DAO.DAOProducto;
import com.expediodigital.ventas360.R;
import com.expediodigital.ventas360.Ventas360App;
import com.expediodigital.ventas360.adapter.RecyclerViewProductoAdapter;
import com.expediodigital.ventas360.model.ProductoKardex;
import com.expediodigital.ventas360.util.SoapManager;
import com.expediodigital.ventas360.util.TablesHelper;
import com.expediodigital.ventas360.util.Util;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Kevin Robinson Meza Hinostroza on julio 2017.
 * Expedio Digital
 * Kevin.Meza@expediodigital.com
 */

public class ProductosFragment extends Fragment implements SearchView.OnQueryTextListener  {
    public static final String TAG = "ProductoFragment";
    RecyclerView recycler_producto;
    FloatingActionButton fab_obtenerProductos;
    DAOProducto daoProducto;
    RecyclerViewProductoAdapter adapter;
    private Switch switch_soloDisponibles;
    ProgressDialog progressDialog;
    SoapManager soap_manager;
    boolean soloDisponibles = false;

    ArrayList<ProductoKardex> listaProducto = new ArrayList<>();
    SearchView searchView;
    TextView tv_cantidadTotal;
    Ventas360App ventas360App;

    public ProductosFragment() {
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        View view = inflater.inflate(R.layout.fragment_productos, container, false);
        setHasOptionsMenu(true);
        Util.actualizarToolBar("Productos",false,getActivity());

        ventas360App = (Ventas360App) getActivity().getApplicationContext();
        daoProducto = new DAOProducto(getActivity());
        soap_manager = new SoapManager(getContext());

        switch_soloDisponibles = view.findViewById(R.id.switch_soloDisponibles);
        recycler_producto = view.findViewById(R.id.recycler_producto);
        fab_obtenerProductos = view.findViewById(R.id.fab_obtenerProductos);
        tv_cantidadTotal = view.findViewById(R.id.tv_cantidadTotal);
        //data para el fragment - clientes en cardview

        adapter = new RecyclerViewProductoAdapter(listaProducto, R.layout.item_producto, this);

        switch_soloDisponibles.setOnCheckedChangeListener (new CompoundButton.OnCheckedChangeListener(){
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                ventas360App.setSoloProductosDisponibles(isChecked);
                soloDisponibles = isChecked;
                refreshLista();
            }
        });

        recycler_producto.addItemDecoration(new DividerItemDecoration(getActivity(),DividerItemDecoration.VERTICAL));
        recycler_producto.setAdapter(adapter);

        fab_obtenerProductos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new ProductosFragment.async_obtenerStockActualizado().execute();
            }
        });

        soloDisponibles = ventas360App.getSoloProductosDisponibles();
        switch_soloDisponibles.setChecked(soloDisponibles);

        refreshLista();
        return view;
    }

    public void refreshLista(){
        listaProducto.clear();
        listaProducto.addAll(daoProducto.getListaProductos(soloDisponibles));
        adapter.notifyDataSetChanged();

        int numeroProductos = listaProducto.size();
        tv_cantidadTotal.setText(String.valueOf(numeroProductos));
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
        List<ProductoKardex> filteredModelList = filtrar(listaProducto, newText);
        adapter.setFilter(filteredModelList);
    }
    private List<ProductoKardex> filtrar(ArrayList<ProductoKardex> listaProducto, String query) {
        query = query.toLowerCase();
        final List<ProductoKardex> listaFiltrada = new ArrayList<>();
        for (ProductoKardex producto : listaProducto) {

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

    class async_obtenerStockActualizado extends AsyncTask<String, Void, String> {
        protected void onPreExecute() {
            progressDialog = new ProgressDialog(getActivity());
            progressDialog.setTitle("");
            progressDialog.setMessage("Obteniendo productos...");
            progressDialog.setIndeterminate(true);
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        protected String doInBackground(String... strings) {
            try {
                soap_manager.obtenerRegistrosxVendedorJSON( TablesHelper.Kardex.Sincronizar, TablesHelper.Kardex.Table);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return "asyncSincronizacion Ok";
        }

        protected void onPostExecute(String result) {
            progressDialog.dismiss();
            refreshLista();
            Log.d( TAG, "onPostExecute "+ result);
        }

    }



}
