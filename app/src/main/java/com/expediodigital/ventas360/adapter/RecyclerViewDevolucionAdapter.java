package com.expediodigital.ventas360.adapter;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.expediodigital.ventas360.R;
import com.expediodigital.ventas360.model.DevolucionDetalleModel;
import com.expediodigital.ventas360.view.fragment.DevolucionesFragment;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Kevin Robinson Meza Hinostroza on agosto 2017.
 * Expedio Digital
 * Kevin.Meza@expediodigital.com
 */

public class RecyclerViewDevolucionAdapter extends RecyclerView.Adapter<RecyclerViewDevolucionAdapter.ProductoViewHolder> {
    public static final String TAG = "RecyclerViewDevolucionAdapter";
    private ArrayList<DevolucionDetalleModel> listaProducto;
    private DevolucionesFragment devolucionesFragment;
    private int posicionSelected;

    public RecyclerViewDevolucionAdapter(ArrayList<DevolucionDetalleModel> listaProducto, DevolucionesFragment fragment) {
        this.listaProducto = listaProducto;
        this.devolucionesFragment = fragment;
    }

    @Override
    public RecyclerViewDevolucionAdapter.ProductoViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_producto_devolucion, parent, false);
        return new RecyclerViewDevolucionAdapter.ProductoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerViewDevolucionAdapter.ProductoViewHolder holder, final int position) {
        final DevolucionDetalleModel model = listaProducto.get(position);

        holder.tv_idProducto.setText(model.getIdProducto());
        holder.tv_descripcion.setText(model.getDescripcion());
        holder.tv_factorConversion.setText("Factor conversión: " +model.getFactorConversion());

        holder.tv_stockUnidadMayor.setText(model.getStockDevolucionUnidadMayor()+" "+model.getUnidadMedidaMayor());
        holder.tv_stockUnidadMenor.setText(model.getStockDevolucionUnidadMenor()+" "+model.getUnidadMedidaMenor());

        if (model.getModificado() == 1){
            holder.tv_stockUnidadMayor.setTextColor(ContextCompat.getColor(devolucionesFragment.getActivity(),R.color.colorPrimary));
            holder.tv_stockUnidadMenor.setTextColor(ContextCompat.getColor(devolucionesFragment.getActivity(),R.color.colorPrimary));
        }else{
            holder.tv_stockUnidadMayor.setTextColor(ContextCompat.getColor(devolucionesFragment.getActivity(), androidx.appcompat.R.color.abc_secondary_text_material_light));
            holder.tv_stockUnidadMenor.setTextColor(ContextCompat.getColor(devolucionesFragment.getActivity(), androidx.appcompat.R.color.abc_secondary_text_material_light));
        }

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                posicionSelected = position;
                devolucionesFragment.editarCantidad();
                return true;
            }
        });
    }

    public void setFilter(List<DevolucionDetalleModel> listaProducto){
        this.listaProducto = new ArrayList<>();
        this.listaProducto.addAll(listaProducto);
        notifyDataSetChanged();
    }

    public DevolucionDetalleModel getItemSelected(){
        return listaProducto.get(posicionSelected);
    }

    @Override
    public int getItemCount() { return listaProducto.size();    }

    public class ProductoViewHolder extends RecyclerView.ViewHolder {

        private TextView tv_idProducto;
        private TextView tv_descripcion;
        private TextView tv_factorConversion;
        private TextView tv_stockUnidadMayor;
        private TextView tv_stockUnidadMenor;


        public ProductoViewHolder(View itemView) {
            super(itemView);
            tv_idProducto = itemView.findViewById(R.id.tv_idProducto);
            tv_descripcion = itemView.findViewById(R.id.tv_descripcion);
            tv_factorConversion = itemView.findViewById(R.id.tv_factorConversion);
            tv_stockUnidadMayor = itemView.findViewById(R.id.tv_stockUnidadMayor);
            tv_stockUnidadMenor = itemView.findViewById(R.id.tv_stockUnidadMenor);
        }
    }
}
