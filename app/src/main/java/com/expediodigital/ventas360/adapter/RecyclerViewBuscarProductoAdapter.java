package com.expediodigital.ventas360.adapter;

import android.app.Activity;
import android.content.Intent;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.expediodigital.ventas360.R;
import com.expediodigital.ventas360.model.ProductoModel;
import com.expediodigital.ventas360.util.Util;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Kevin Robinson Meza Hinostroza on julio 2017.
 * Expedio Digital
 * Kevin.Meza@expediodigital.com
 */

public class RecyclerViewBuscarProductoAdapter extends RecyclerView.Adapter<RecyclerViewBuscarProductoAdapter.ProductoViewHolder> {
    private ArrayList<ProductoModel> lista;
    private Activity activity;
    DecimalFormat formateador;

    public RecyclerViewBuscarProductoAdapter(ArrayList<ProductoModel> lista, Activity activity){
        this.lista = lista;
        this.activity = activity;

        this.formateador = Util.formateador();
    }

    @Override
    public ProductoViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_buscar_producto, parent, false);
        return new ProductoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ProductoViewHolder holder, int position) {
        final ProductoModel productoModel = lista.get(position);

        holder.tv_idProducto.setText(productoModel.getIdProducto());
        holder.tv_descripcion.setText(productoModel.getDescripcion());
        holder.tv_precioMenor.setText("S/. "+formateador.format(productoModel.getPrecioMenor()));
        holder.tv_precioMayor.setText("S/. "+formateador.format(productoModel.getPrecioMayor()));
        //holder.tv_unidadMedidaMenor.setText(productoModel.getIdUnidadManejo());
        holder.tv_unidadMedidaMenor.setText("UNIDAD");
        holder.tv_unidadMedidaMayor.setText(productoModel.getIdUnidadManejo());

        //if (productoModel.getIdUnidadMenor().equals(productoModel.getIdUnidadMayor())){
            //holder.tv_unidadMedidaMayor.setVisibility(View.INVISIBLE);
            //holder.tv_precioMayor.setVisibility(View.INVISIBLE);
        //}else{
//            holder.tv_unidadMedidaMenor.setVisibility(View.INVISIBLE);
//            holder.tv_precioMenor.setVisibility(View.INVISIBLE);
//            holder.tv_unidadMedidaMayor.setVisibility(View.INVISIBLE);
//            holder.tv_precioMayor.setVisibility(View.INVISIBLE);
        //}

        if (productoModel.getTipoProducto().equals(ProductoModel.TIPO_SERVICIO)){
            holder.frame_icon.setBackgroundColor(ContextCompat.getColor(activity,R.color.grey_200));
            holder.img_icon.setImageResource(R.drawable.icon_service);
        }else{
            holder.frame_icon.setBackgroundColor(ContextCompat.getColor(activity,R.color.colorAccent));
            holder.img_icon.setImageResource(R.drawable.icon_paquete);
        }

        holder.tv_descripcion.setTextColor(ContextCompat.getColor(activity, androidx.appcompat.R.color.abc_secondary_text_material_light));
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.putExtra("idProducto",productoModel.getIdProducto());
                intent.putExtra("descripcion",productoModel.getDescripcion());
                //intent.putExtra("factorConversion",productoModel.getFactorConversion());
                intent.putExtra("peso",productoModel.getPeso());
                intent.putExtra("tipoProducto",productoModel.getTipoProducto());
                activity.setResult(Activity.RESULT_OK,intent);
                activity.finish();
            }
        });

        /*
        if ( !productoModel.getIdPoliticaPrecio().equals("")) {
            if (productoModel.getTipoProducto().equals(ProductoModel.TIPO_SERVICIO)){
                holder.frame_icon.setBackgroundColor(ContextCompat.getColor(activity,R.color.grey_200));
                holder.img_icon.setImageResource(R.drawable.icon_service);
            }else{
                holder.frame_icon.setBackgroundColor(ContextCompat.getColor(activity,R.color.colorAccent));
                holder.img_icon.setImageResource(R.drawable.icon_paquete);
            }

            holder.tv_descripcion.setTextColor(ContextCompat.getColor(activity,android.support.v7.appcompat.R.color.abc_secondary_text_material_light));
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent();
                    intent.putExtra("idProducto",productoModel.getIdProducto());
                    intent.putExtra("descripcion",productoModel.getDescripcion());
                    //intent.putExtra("factorConversion",productoModel.getFactorConversion());
                    intent.putExtra("peso",productoModel.getPeso());
                    intent.putExtra("tipoProducto",productoModel.getTipoProducto());
                    activity.setResult(Activity.RESULT_OK,intent);
                    activity.finish();
                }
            });
        }else {
            if (productoModel.getTipoProducto().equals(ProductoModel.TIPO_SERVICIO)){
                holder.frame_icon.setBackgroundColor(ContextCompat.getColor(activity,R.color.grey_200));
                holder.img_icon.setImageResource(R.drawable.icon_service_grey);
            }else{
                holder.frame_icon.setBackgroundColor(ContextCompat.getColor(activity,R.color.grey_200));
                holder.img_icon.setImageResource(R.drawable.icon_paquete_grey);
            }
            holder.tv_descripcion.setTextColor(ContextCompat.getColor(activity,R.color.grey_500));
            holder.itemView.setOnClickListener(null);
        }*/
    }

    @Override
    public int getItemCount() {
        return lista.size();
    }

    public void setFilter(List<ProductoModel> listaFiltrada) {
        this.lista = new ArrayList<>();
        this.lista.addAll(listaFiltrada);
        notifyDataSetChanged();
    }

    public class ProductoViewHolder extends RecyclerView.ViewHolder{
        private FrameLayout frame_icon;
        private ImageView img_icon;
        private TextView tv_idProducto;
        private TextView tv_descripcion;
        private TextView tv_unidadMedidaMenor;
        private TextView tv_unidadMedidaMayor;
        private TextView tv_precioMenor;
        private TextView tv_precioMayor;

        public ProductoViewHolder(View itemView) {
            super(itemView);
            frame_icon = itemView.findViewById(R.id.frame_icon);
            img_icon = itemView.findViewById(R.id.img_icon);
            tv_idProducto = itemView.findViewById(R.id.tv_idProducto);
            tv_descripcion = itemView.findViewById(R.id.tv_descripcion);
            tv_unidadMedidaMenor = itemView.findViewById(R.id.tv_unidadMedidaMenor);
            tv_unidadMedidaMayor = itemView.findViewById(R.id.tv_unidadMedidaMayor);
            tv_precioMenor = itemView.findViewById(R.id.tv_precioMenor);
            tv_precioMayor = itemView.findViewById(R.id.tv_precioMayor);

        }
    }
}
