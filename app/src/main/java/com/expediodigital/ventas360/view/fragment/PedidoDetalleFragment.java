package com.expediodigital.ventas360.view.fragment;


import android.content.DialogInterface;
import android.os.Bundle;
import androidx.annotation.Nullable;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.fragment.app.Fragment;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.expediodigital.ventas360.DAO.DAOPedido;
import com.expediodigital.ventas360.R;
import com.expediodigital.ventas360.adapter.RecyclerViewProductoPedidoAdapter;
import com.expediodigital.ventas360.model.PedidoCabeceraModel;
import com.expediodigital.ventas360.model.PedidoDetalleModel;
import com.expediodigital.ventas360.model.ProductoModel;
import com.expediodigital.ventas360.util.Util;
import com.expediodigital.ventas360.view.PedidoActivity;

import java.text.DecimalFormat;
import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 */
public class PedidoDetalleFragment extends Fragment {
    public static final String TAG = "PedidoDetalleFragment";
    FloatingActionButton fab_agregarProducto;
    ArrayList<PedidoDetalleModel> listaProductos = new ArrayList<>();
    String idCliente;
    String numeroPedido;
    private DAOPedido daoPedido;
    TextView tv_montoTotal,tv_pesoTotal,tv_subTotal,tv_descuento,tv_cantidadProductos;
    DecimalFormat formateador;
    RecyclerViewProductoPedidoAdapter adapter;
    RecyclerView recycler_productosPedido;
    private int ACCION_PEDIDO;
    private double montoTotal = 0;

    public PedidoDetalleFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_pedido_detalle, container, false);
        setHasOptionsMenu(false);//Indica que podrá manipular las opciones del actionBar desde este fragment

        daoPedido = new DAOPedido(getActivity());
        formateador = Util.formateador();

        tv_subTotal = view.findViewById(R.id.tv_subTotal);
        tv_descuento = view.findViewById(R.id.tv_descuento);
        tv_pesoTotal = view.findViewById(R.id.tv_pesoTotal);
        tv_montoTotal = view.findViewById(R.id.tv_montoTotal);
        tv_cantidadProductos = view.findViewById(R.id.tv_cantidadProductos);
        recycler_productosPedido = view.findViewById(R.id.recycler_productosPedido);

        recycler_productosPedido.setItemAnimator(new DefaultItemAnimator());

        fab_agregarProducto = view.findViewById(R.id.fab_agregarProducto);
        //Mandamos el boton al activity PedidoActivty desde donde se trabajará
        ((PedidoActivity)getActivity()).setFab_agregarProducto(fab_agregarProducto);

        //Obtenermos el ocnumero del activity PedidoActivity y este a su vez lo obtiene de PedidoCabeceraFragment
        this.numeroPedido = ((PedidoActivity)getActivity()).getNumeroPedidoFromFragment();


        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        boolean editarItems = true;
        ACCION_PEDIDO = ((PedidoActivity)getActivity()).getACCION_PEDIDO();
        if (ACCION_PEDIDO  == PedidoActivity.ACCION_NUEVO_PEDIDO){

        }else{
            //Los campos ocnumero,cliente ya han sido cargados desde PedidoActivity
            if (ACCION_PEDIDO == PedidoActivity.ACCION_VER_PEDIDO){
                fab_agregarProducto.setEnabled(false);
                editarItems = false;
            }
        }

        adapter = new RecyclerViewProductoPedidoAdapter(listaProductos,getActivity(),editarItems);
        recycler_productosPedido.setAdapter(adapter);
        recycler_productosPedido.addItemDecoration(new DividerItemDecoration(getActivity(),DividerItemDecoration.VERTICAL));

        /*
        recycler_productosPedido.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (dy > 0 && fab_agregarProducto.getVisibility() == View.VISIBLE) {
                    fab_agregarProducto.hide();
                } else if (dy < 0 && fab_agregarProducto.getVisibility() != View.VISIBLE) {
                    fab_agregarProducto.show();
                }
            }
        });
        */

        mostrarListaProductos();
    }

    public boolean onContextItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_contextual_general_editar:
                ((PedidoActivity)getActivity()).modificarProducto(adapter.getItemSelected().getIdProducto(),adapter.getItemSelected().getDescripcion(),adapter.getItemSelected().getPrecioBruto());
                break;
            case R.id.menu_contextual_general_quitar:
                if (listaProductos.size() <= 1){
                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
                    alertDialog.setTitle("Anular pedido");
                    alertDialog.setMessage("Está removiendo todos los productos del pedido. ¿Desea anular todo el pedido?.\n\nSi no desea anular el pedido, primero agregue otros productos antes de quitar este.");
                    alertDialog.setCancelable(false);

                    alertDialog.setPositiveButton("ANULAR", new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            daoPedido.actualizarEstadoPedido(numeroPedido, PedidoCabeceraModel.ESTADO_ANULADO, PedidoCabeceraModel.ID_MOTIVO_NO_COMPRA_DEFAULT);
                            ((PedidoActivity)getActivity()).enviarPedido(true);
                        }
                    });
                    alertDialog.setNegativeButton("CANCELAR",null);
                    alertDialog.show();
                }else{
                    //daoPedido.modificarItemDetallePedido();
                    if (!daoPedido.eliminarItemDetallePedido(numeroPedido,adapter.getItemSelected().getIdProducto())) {
                        Toast.makeText(getActivity(), "No se pudo quitar el producto", Toast.LENGTH_SHORT).show();
                    }
                    mostrarListaProductos();
                    if (ACCION_PEDIDO == PedidoActivity.ACCION_EDITAR_PEDIDO) { ((PedidoActivity) getActivity()).noPermitirCerrar(); }

                }

                break;
        }
        return super.onContextItemSelected(item);
    }

    public void mostrarListaProductos(){
        this.listaProductos.clear();

        double pesoTotal = 0;
        double subTotal = 0;
        double descuento = 0;
        montoTotal = 0;
        int cantidadProductos = 0;

        ArrayList<PedidoDetalleModel> lista = new ArrayList<>();
        for (PedidoDetalleModel producto : getListaProductos()){
            if(producto.getTipoProducto().equals("B"))
            {
                continue;
            }
            lista.add(producto);
        }

        this.listaProductos.addAll(lista);
        cantidadProductos = listaProductos.size();
        adapter.notifyDataSetChanged();

        for (PedidoDetalleModel producto : listaProductos){
            if(producto.getTipoProducto().equals("B"))
            {
                continue;
            }
            if (!producto.getTipoProducto().equals(ProductoModel.TIPO_PUBLICIDAD)){
                subTotal += producto.getPrecioNeto();
                pesoTotal += producto.getPesoNeto();
            }
        }

        montoTotal = Util.redondearDouble(subTotal - descuento);
        pesoTotal = Util.redondearDouble(pesoTotal);
        daoPedido.actualizarPedidoTotales(montoTotal,pesoTotal,numeroPedido);

        tv_subTotal.setText("S/. "+formateador.format(subTotal));
        tv_descuento.setText("S/. "+formateador.format(descuento));
        tv_pesoTotal.setText("");
//        tv_pesoTotal.setText("Kg. "+formateador.format(pesoTotal));
        tv_montoTotal.setText("S/. "+formateador.format(montoTotal));
        tv_cantidadProductos.setText(""+cantidadProductos);
    }

    public ArrayList<PedidoDetalleModel> getListaProductos() {
        return daoPedido.getListaProductoPedido(numeroPedido);
    }

    public String getIdCliente() {
        return idCliente;
    }

    public void setIdCliente(String idCliente) {
        this.idCliente = idCliente;
    }

    public double getMontoTotal(){
        return montoTotal;
    }
}
