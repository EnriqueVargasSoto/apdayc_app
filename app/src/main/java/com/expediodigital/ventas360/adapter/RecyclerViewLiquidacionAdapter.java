package com.expediodigital.ventas360.adapter;

import android.app.Activity;

import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.expediodigital.ventas360.R;
import com.expediodigital.ventas360.model.LiquidacionProductoModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Kevin Robinson Meza Hinostroza on agosto 2017.
 * Expedio Digital
 * Kevin.Meza@expediodigital.com
 */

public class RecyclerViewLiquidacionAdapter extends RecyclerView.Adapter<RecyclerViewLiquidacionAdapter.ProductoViewHolder> {
    public static final String TAG = "RecyclerViewLiquidacionAdapter";
    private ArrayList<LiquidacionProductoModel> listaProductos;
    private Activity activity;

    public RecyclerViewLiquidacionAdapter(ArrayList<LiquidacionProductoModel> listaProductos, Activity activity) {
        this.listaProductos = listaProductos;
        this.activity = activity;
    }

    @Override
    public RecyclerViewLiquidacionAdapter.ProductoViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_liquidacion_producto, parent, false);
        return new RecyclerViewLiquidacionAdapter.ProductoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerViewLiquidacionAdapter.ProductoViewHolder holder, final int position) {
        final LiquidacionProductoModel model = listaProductos.get(position);

        holder.tv_idProducto.setText(model.getIdProducto());
        holder.tv_descripcion.setText(model.getDescripcion());

        holder.tv_stockGuia.setText("En Guia: " +model.getStockGuia());
        holder.tv_stockVenta.setText("En Venta: " +model.getStockVenta());
        holder.tv_stockDisponible.setText("Devolución: " +model.getStockDevolucion());
        holder.tv_diferencia.setText("Diferencia: " +model.getDiferencia());

        holder.checkBox.setOnCheckedChangeListener(null);//Por alguna razón es necesario para que se mantengan los estados de los checkbox cuando se reciclen
        holder.checkBox.setChecked(model.isSelected());
        holder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                listaProductos.get(position).setSelected(b);
                Log.d(TAG,"onCheckedChanged "+b);
            }
        });

    }

    public void setFilter(List<LiquidacionProductoModel> listaProductos){
        this.listaProductos = new ArrayList<>();
        this.listaProductos.addAll(listaProductos);
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() { return listaProductos.size();    }

    public class ProductoViewHolder extends RecyclerView.ViewHolder {

        private TextView tv_idProducto;
        private TextView tv_descripcion;

        TextView tv_stockGuia;
        TextView tv_stockVenta;
        TextView tv_stockDisponible;
        TextView tv_diferencia;
        CheckBox checkBox;

        public ProductoViewHolder(View itemView) {
            super(itemView);
            tv_idProducto = itemView.findViewById(R.id.tv_idProducto);
            tv_descripcion = itemView.findViewById(R.id.tv_descripcion);

            tv_stockGuia = itemView.findViewById(R.id.tv_stockGuia);
            tv_stockVenta = itemView.findViewById(R.id.tv_stockVenta);
            tv_stockDisponible = itemView.findViewById(R.id.tv_stockDisponible);
            tv_diferencia = itemView.findViewById(R.id.tv_diferencia);
            checkBox = itemView.findViewById(R.id.checkbox);
        }
    }
}
