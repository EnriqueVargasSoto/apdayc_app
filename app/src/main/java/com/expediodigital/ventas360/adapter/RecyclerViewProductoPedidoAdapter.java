package com.expediodigital.ventas360.adapter;

import android.app.Activity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.expediodigital.ventas360.R;
import com.expediodigital.ventas360.model.PedidoDetalleModel;
import com.expediodigital.ventas360.model.ProductoModel;
import com.expediodigital.ventas360.util.Util;

import java.text.DecimalFormat;
import java.util.ArrayList;

/**
 * Created by Kevin Robinson Meza Hinostroza on julio 2017.
 * Expedio Digital
 * Meza.Hinostroza.Robin@gmail.com
 */

public class RecyclerViewProductoPedidoAdapter extends RecyclerView.Adapter<RecyclerViewProductoPedidoAdapter.ProductoPedidoViewHolder> {
    private ArrayList<PedidoDetalleModel> lista;
    private Activity activity;
    DecimalFormat formateador;
    int positionItemSelected;
    boolean editarItems;

    public RecyclerViewProductoPedidoAdapter(ArrayList<PedidoDetalleModel> lista, Activity activity, boolean editarItems){
        this.lista = lista;
        this.activity = activity;
        this.editarItems = editarItems;

        this.formateador = Util.formateador();
    }

    @Override
    public ProductoPedidoViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_producto_pedido, parent, false);
        return new ProductoPedidoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ProductoPedidoViewHolder holder, final int position) {
        final PedidoDetalleModel producto = lista.get(position);
        holder.tv_codigoProducto.setText(producto.getIdProducto());
        holder.tv_descripcion.setText(producto.getDescripcion());
        holder.tv_precio.setText("S/. "+formateador.format(producto.getPrecioBruto()));

        if(producto.getEstadoDetalle().equals("2") && producto.getTipoProducto().equals("B")){
            holder.tv_cantidad.setText("No hay stock");
            holder.tv_cantidad.setTextColor(ContextCompat.getColor(activity, R.color.red_900));
        }
        else{
            holder.tv_cantidad.setText(""+producto.getCantidad());
        }
        holder.tv_unidadMedida.setText(producto.getDescripcionUnidadMedida());
        holder.tv_subTotal.setText("S/. "+formateador.format(producto.getPrecioNeto()));

        if (producto.getTipoProducto().equals(ProductoModel.TIPO_VENTA) || producto.getTipoProducto().equals(ProductoModel.TIPO_SERVICIO)){
            holder.linear_icon.setBackgroundColor(ContextCompat.getColor(activity,R.color.blue_grey_500));
            holder.img_producto.setImageResource(R.drawable.icon_paquete);
            holder.tv_descripcion.setTextColor(ContextCompat.getColor(activity, androidx.appcompat.R.color.abc_secondary_text_material_light));
        }else if (producto.getTipoProducto().equals(ProductoModel.TIPO_PUBLICIDAD)){
            holder.linear_icon.setBackgroundColor(ContextCompat.getColor(activity,R.color.grey_500));
            holder.img_producto.setImageResource(R.drawable.icon_paquete);
            holder.tv_descripcion.setTextColor(ContextCompat.getColor(activity,R.color.grey_400));
        }else if (producto.getTipoProducto().equals(ProductoModel.TIPO_BONIFICACION)){
            holder.linear_icon.setBackgroundColor(ContextCompat.getColor(activity,R.color.green_A700));
            holder.img_producto.setImageResource(R.drawable.icon_paquetes);
            holder.tv_descripcion.setTextColor(ContextCompat.getColor(activity,R.color.green_A700));
            holder.tv_unidadMedida.setTextColor(ContextCompat.getColor(activity,R.color.green_A700));
            holder.tv_precio.setTextColor(ContextCompat.getColor(activity,R.color.green_A700));
            holder.lbl_subTotal.setVisibility(View.INVISIBLE);
            holder.tv_subTotal.setTextColor(ContextCompat.getColor(activity,R.color.green_A700));
            holder.tv_subTotal.setText("PROMOCION");
            holder.tv_codigoProducto.setTextColor(ContextCompat.getColor(activity,R.color.green_A700));
            holder.tv_malla.setVisibility(View.VISIBLE);
            holder.tv_malla.setTextColor(ContextCompat.getColor(activity,R.color.green_A700));
            holder.tv_malla.setText("MALLA: "+producto.getMalla());
            holder.tv_sinstock.setTextColor(ContextCompat.getColor(activity,R.color.red_500));
            if (producto.getSinStock() == 1){
                holder.tv_sinstock.setText("no hay inventario");
            }

        }

        //Si el producto no tiene stock (validacion antes de enviar al servidor) pintar de rojo
        if (producto.getSinStock()==1){
            holder.tv_cantidad.setTextColor(ContextCompat.getColor(activity,R.color.red_400));
        }else{
            holder.tv_cantidad.setTextColor(ContextCompat.getColor(activity, androidx.appcompat.R.color.abc_secondary_text_material_light));
        }

        //Los productos que son bonificacion no se pueden cambiar ni quitar
        if (editarItems && (!producto.getTipoProducto().equals(ProductoModel.TIPO_BONIFICACION))){
            holder.itemView.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {
                @Override
                public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
                    MenuInflater inflater = activity.getMenuInflater();
                    menu.setHeaderTitle(producto.getDescripcion());
                    inflater.inflate(R.menu.menu_contextual_general,menu);
                    positionItemSelected = position;
                }
            });
        }
    }

    public PedidoDetalleModel getItemSelected(){
        return lista.get(positionItemSelected);
    }

    @Override
    public int getItemCount() {
        return lista.size();
    }

    public class ProductoPedidoViewHolder extends RecyclerView.ViewHolder {
        private LinearLayout linear_icon;
        private ImageView img_producto;
        private TextView tv_codigoProducto;
        private TextView tv_descripcion;
        private TextView tv_unidadMedida;
        private TextView tv_precio;
        private TextView tv_cantidad;
        private TextView tv_subTotal;
        private TextView lbl_subTotal;
        private TextView tv_malla;
        private TextView tv_sinstock;

        public ProductoPedidoViewHolder(View itemView) {
            super(itemView);
            linear_icon = itemView.findViewById(R.id.linear_icon);
            img_producto = itemView.findViewById(R.id.img_producto);
            tv_codigoProducto = itemView.findViewById(R.id.tv_codigoProducto);
            tv_descripcion = itemView.findViewById(R.id.tv_descripcion);
            tv_unidadMedida = itemView.findViewById(R.id.tv_unidadMedida);
            tv_precio = itemView.findViewById(R.id.tv_precio);
            tv_cantidad = itemView.findViewById(R.id.tv_cantidad);
            tv_subTotal = itemView.findViewById(R.id.tv_subTotal);
            lbl_subTotal = itemView.findViewById(R.id.lbl_subTotal);
            tv_malla = itemView.findViewById(R.id.tv_malla);
            tv_sinstock = itemView.findViewById(R.id.tv_sinstock);
        }
    }
}
