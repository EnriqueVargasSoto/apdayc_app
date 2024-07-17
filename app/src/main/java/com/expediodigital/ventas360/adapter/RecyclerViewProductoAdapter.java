package com.expediodigital.ventas360.adapter;

import android.content.Context;
import android.content.Intent;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.expediodigital.ventas360.R;
import com.expediodigital.ventas360.model.ProductoKardex;
import com.expediodigital.ventas360.view.DetalleProductoActivity;
import com.expediodigital.ventas360.view.fragment.ProductosFragment;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ASUS on 11/07/2017.
 */

public class RecyclerViewProductoAdapter extends RecyclerView.Adapter<RecyclerViewProductoAdapter.ProductoViewHolder> {
    public static final String TAG = "RViewProductoAdapter";
    private ArrayList<ProductoKardex> listaProducto;
    private int resource;
    private Context mContext;
    private ProductosFragment productoFragment;

    public RecyclerViewProductoAdapter(ArrayList<ProductoKardex> listaProducto, int resource, ProductosFragment fragment) {
        this.listaProducto = listaProducto;
        this.productoFragment = fragment;
        this.resource = resource;
    }

    @Override
    public RecyclerViewProductoAdapter.ProductoViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(resource, parent, false);
        mContext = parent.getContext();

        return new RecyclerViewProductoAdapter.ProductoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerViewProductoAdapter.ProductoViewHolder holder, int position) {
        final ProductoKardex model = listaProducto.get(position);

        holder.tv_codProducto.setText(model.getIdProducto());
        holder.tv_descripcion.setText(model.getDescripcion());

        //String und = " UND";
        holder.tv_stockInicial.setText("Inicial: " + model.getStockInicial() );
        holder.tv_stockPedido.setText("Pedidos: " + model.getStockPedido() );
        holder.tv_stockDespachado.setText("Factura: " + model.getStockDespachado() );
        holder.tv_stockDisponible.setText("Dispon: " + model.getStockDisponibleGeneral() );

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent detalle_producto = new Intent(mContext, DetalleProductoActivity.class);
                detalle_producto.putExtra("codigo", model.getIdProducto());
                productoFragment.startActivity(detalle_producto);
            }
        });

    }

    public void setFilter(List<ProductoKardex> listaProducto){
        this.listaProducto = new ArrayList<>();
        this.listaProducto.addAll(listaProducto);
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() { return listaProducto.size();    }

    public class ProductoViewHolder extends RecyclerView.ViewHolder {

        private TextView tv_codProducto;
        private TextView tv_descripcion;

        TextView tv_stockInicial;
        TextView tv_stockPedido;
        TextView tv_stockDespachado;
        TextView tv_stockDisponible;

        public ProductoViewHolder(View itemView) {
            super(itemView);
            tv_codProducto = itemView.findViewById(R.id.tv_codProducto);
            tv_descripcion = itemView.findViewById(R.id.tv_descripcion);

            tv_stockInicial = itemView.findViewById(R.id.tv_stockInicial);
            tv_stockPedido = itemView.findViewById(R.id.tv_stockPedido);
            tv_stockDespachado = itemView.findViewById(R.id.tv_stockDespachado);
            tv_stockDisponible = itemView.findViewById(R.id.tv_stockDisponible);

        }
    }
}
