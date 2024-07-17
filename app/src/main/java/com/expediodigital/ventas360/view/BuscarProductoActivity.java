package com.expediodigital.ventas360.view;

import androidx.core.view.MenuItemCompat;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;

import com.expediodigital.ventas360.DAO.DAOCliente;
import com.expediodigital.ventas360.DAO.DAOProducto;
import com.expediodigital.ventas360.R;
import com.expediodigital.ventas360.adapter.RecyclerViewBuscarProductoAdapter;
import com.expediodigital.ventas360.model.ProductoModel;
import com.expediodigital.ventas360.util.Util;

import java.util.ArrayList;
import java.util.List;

public class BuscarProductoActivity extends AppCompatActivity implements SearchView.OnQueryTextListener {
    public static final String TAG = "BuscarProductoActivity";

    SearchView searchView;
    RecyclerView recycler_productos;
    RecyclerViewBuscarProductoAdapter adapter;
    DAOProducto daoProducto;
    DAOCliente daoCliente;
    ArrayList<ProductoModel> listaProductos = new ArrayList<>();
    String idCliente = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_buscar_producto);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Util.actualizarToolBar("Buscar Producto",true,this);

        if (getIntent().getExtras() != null)
            idCliente = getIntent().getExtras().getString("idCliente","");

        daoProducto = new DAOProducto(getApplicationContext());
        daoCliente = new DAOCliente(getApplicationContext());

        recycler_productos = (RecyclerView) findViewById(R.id.recycler_productos);

        String idPoliticaCliente = daoCliente.getIdPoliticaPrecio(idCliente);
        listaProductos = daoProducto.getProductos(idPoliticaCliente);
        adapter = new RecyclerViewBuscarProductoAdapter(listaProductos,this);
        recycler_productos.setAdapter(adapter);
        recycler_productos.addItemDecoration(new DividerItemDecoration(this,DividerItemDecoration.VERTICAL));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_search, menu);
        final MenuItem itemSearch = menu.findItem(R.id.action_search);
        searchView = (SearchView) MenuItemCompat.getActionView(itemSearch);
        searchView.setOnQueryTextListener(this);

        searchView.setQueryHint("Buscar producto...");

        //Indica que la vista no debe estar en modo Icono, es decir que se muestre abierto
        searchView.setIconified(false);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id){
            case android.R.id.home:
                setResult(RESULT_CANCELED);
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        final List<ProductoModel> filteredModelList = filtrar(listaProductos, newText);
        adapter.setFilter(filteredModelList); //Método creado en el adaptador
        return false;
    }

    private List<ProductoModel> filtrar(List<ProductoModel> listaProductos, String query) {
        query = query.toLowerCase();
        final List<ProductoModel> listaFiltrada = new ArrayList<>();
        for (ProductoModel producto : listaProductos) {
            if (TextUtils.isDigitsOnly(query)){
                final String codigo = producto.getIdProducto().toLowerCase();
                if (codigo.contains(query)) {
                    listaFiltrada.add(producto);
                }
            } else {
                final String descripcion = producto.getDescripcion().toLowerCase();
                if (descripcion.contains(query)) {
                    listaFiltrada.add(producto);
                }
            }

        }
        return listaFiltrada;
    }
}
