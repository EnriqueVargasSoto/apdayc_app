package com.expediodigital.ventas360.adapter;

import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.expediodigital.ventas360.R;
import com.expediodigital.ventas360.model.PoliticaPrecioModel;

import java.util.ArrayList;

/**
 * Created by Monica Toribio Rojas on julio 2017.
 * Expedio Digital
 * monica.toribio.rojas@gmail.com
 */

public class RecyclerViewPoliticaPrecioDetalleAdapter extends RecyclerView.Adapter<RecyclerViewPoliticaPrecioDetalleAdapter.PoliticaPrecioDetalleViewHolder> {
    public static final String TAG = "RViewPPDetAdapter";
    private ArrayList<PoliticaPrecioModel> listaPPreciodetalle;
    private int resource;

    public RecyclerViewPoliticaPrecioDetalleAdapter(ArrayList<PoliticaPrecioModel> listaPPreciodetalle, int resource) {
        this.listaPPreciodetalle = listaPPreciodetalle;
        Log.i(TAG," lista  "+ this.listaPPreciodetalle.size());
        this.resource = resource;
    }

    @Override
    public RecyclerViewPoliticaPrecioDetalleAdapter.PoliticaPrecioDetalleViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(resource, parent, false);

        return new RecyclerViewPoliticaPrecioDetalleAdapter.PoliticaPrecioDetalleViewHolder(view);
    }


    @Override
    public void onBindViewHolder(RecyclerViewPoliticaPrecioDetalleAdapter.PoliticaPrecioDetalleViewHolder holder, int position) {
        final PoliticaPrecioModel model = listaPPreciodetalle.get(position);

        Log.i(TAG," nombrepolitica  "+model.getDescripcion());

        holder.tv_desc_politica.setText(model.getDescripcion());
        holder.tv_precio_unitario.setText(model.getUnidad());
        holder.tv_precioUnidad.setText(model.getPrecioContenido()+"");
        holder.tv_precio_mayor.setText(model.getUnidad());
        holder.tv_precioMayor.setText(model.getPrecioManejo()+"");

    }

    @Override
    public int getItemCount() {     return listaPPreciodetalle.size();     }

    public class PoliticaPrecioDetalleViewHolder extends RecyclerView.ViewHolder {

        private TextView tv_desc_politica;
        private TextView tv_precio_unitario;
        private TextView tv_precioUnidad;
        private TextView tv_precio_mayor;
        private TextView tv_precioMayor;

        public PoliticaPrecioDetalleViewHolder(View itemView) {
            super(itemView);
            tv_desc_politica = itemView.findViewById(R.id.tv_desc_politica);
            tv_precio_unitario = itemView.findViewById(R.id.tv_precio_unitario);
            tv_precioUnidad = itemView.findViewById(R.id.tv_precioUnidad);
            tv_precio_mayor = itemView.findViewById(R.id.tv_precio_mayor);
            tv_precioMayor = itemView.findViewById(R.id.tv_precioMayor);
        }
    }
}
