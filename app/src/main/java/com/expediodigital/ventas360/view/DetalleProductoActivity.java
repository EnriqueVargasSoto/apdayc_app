package com.expediodigital.ventas360.view;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;

import android.view.MenuItem;
import android.widget.TextView;

import com.expediodigital.ventas360.DAO.DAOProducto;
import com.expediodigital.ventas360.R;
import com.expediodigital.ventas360.adapter.RecyclerViewPoliticaPrecioDetalleAdapter;
import com.expediodigital.ventas360.model.PoliticaPrecioModel;
import com.expediodigital.ventas360.model.ProductoModel;
import com.expediodigital.ventas360.util.Util;

import java.util.ArrayList;

/**
 * Created by ASUS on 11/07/2017.
 */

public class DetalleProductoActivity extends AppCompatActivity {

    DAOProducto daoProducto;
    TextView tv_codigo, tv_descripcion, tv_linea, tv_familia, tv_peso;
    ProductoModel detalleproducto;
    String codigoProducto;

    // politica de precios
    ArrayList<PoliticaPrecioModel> politicapreciodetalle;
    RecyclerView recycler_politica;
    RecyclerViewPoliticaPrecioDetalleAdapter politicaPrecioDetalleAdapter;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_producto);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Util.actualizarToolBar("Detalle del producto",true,this);

        daoProducto = new DAOProducto(this.getApplicationContext());
        tv_codigo = (TextView) this.findViewById(R.id.tv_codigo);
        tv_descripcion = (TextView) this.findViewById(R.id.tv_descripcion);
        tv_linea = (TextView) this.findViewById(R.id.tv_linea);
        tv_familia = (TextView) this.findViewById(R.id.tv_familia);
        tv_peso = (TextView) this.findViewById(R.id.tv_peso);

        recycler_politica = (RecyclerView) this.findViewById(R.id.recycler_politica_precios);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {

            codigoProducto = bundle.getString("codigo");

            detalleproducto = daoProducto.getDetalleProducto(codigoProducto);
            tv_codigo.setText(" Codigo : "+detalleproducto.getIdProducto());
            tv_descripcion.setText(detalleproducto.getDescripcion());
            tv_linea.setText(" Linea : "+detalleproducto.getIdLinea());
            tv_familia.setText(" Familia : "+detalleproducto.getIdFamilia());
            tv_peso.setText(" Peso : "+detalleproducto.getPeso());
        }

        // se da valor a la lista
        politicapreciodetalle = daoProducto.obtenerpoliticapreciodetalleXproducto(codigoProducto);
        politicaPrecioDetalleAdapter = new RecyclerViewPoliticaPrecioDetalleAdapter(politicapreciodetalle, R.layout.cardview_politica_precios);

        //lineas de separacion
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this.getApplicationContext());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);

        //Es importante crear un LinearLayoutManager e indicar la orientación
        recycler_politica.setLayoutManager(linearLayoutManager);
        recycler_politica.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false));
        recycler_politica.addItemDecoration(new DividerItemDecoration(this,DividerItemDecoration.VERTICAL));

        recycler_politica.setAdapter(politicaPrecioDetalleAdapter);
    }

    //codigo para dar funcionalidad al boton retoceder
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home);{
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

}
