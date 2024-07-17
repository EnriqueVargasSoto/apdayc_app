package com.expediodigital.ventas360.adapter;

import android.app.Activity;
import androidx.annotation.NonNull;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

import com.expediodigital.ventas360.R;
import com.expediodigital.ventas360.model.PoliticaPrecioModel;
import com.expediodigital.ventas360.util.Util;

import java.text.DecimalFormat;
import java.util.ArrayList;

/**
 * Created by Kevin Robinson Meza Hinostroza on julio 2017.
 * Expedio Digital
 * Meza.Hinostroza.Robin@gmail.com
 */

public class SpinnerPoliticaPrecioAdapter extends BaseAdapter implements SpinnerAdapter {
    private Activity activity;
    private ArrayList<PoliticaPrecioModel> lista;
    LayoutInflater inflater;
    DecimalFormat formateador;

    public SpinnerPoliticaPrecioAdapter(@NonNull Activity activity, @NonNull ArrayList<PoliticaPrecioModel> lista) {
        this.activity = activity;
        this.lista = lista;
        this.inflater = LayoutInflater.from(activity);
        this.formateador = Util.formateador();
    }

    @Override
    public int getCount() {
        return lista.size();
    }

    @Override
    public PoliticaPrecioModel getItem(int position) {
        return lista.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public String getIdPolitica(int position){
        return lista.get(position).getIdPoliticaPrecio();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        TextView txt = new TextView(activity);
        txt.setPadding(16, 10, 16, 10);
        txt.setTextSize(18);
        txt.setGravity(Gravity.CENTER_VERTICAL);
        txt.setText(lista.get(position).getDescripcion());
        return  txt;

    }

    class ViewHolder{
        TextView tv_descripcion;
        TextView tv_precioUnidad;
        TextView tv_precioMayor;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        DecimalFormat formateador = Util.formateador();

        if(convertView==null){
            convertView = inflater.inflate(R.layout.item_politica_precio, parent, false);
            holder = new ViewHolder();

            holder.tv_descripcion = convertView.findViewById(R.id.tv_descripcion);
            holder.tv_precioUnidad = convertView.findViewById(R.id.tv_precioUnidad);
            holder.tv_precioMayor = convertView.findViewById(R.id.tv_precioMayor);
            convertView.setTag(holder);
        }else {
            holder = (ViewHolder) convertView.getTag();
        }

        PoliticaPrecioModel politicaPrecio = lista.get(position);

        holder.tv_descripcion.setText(politicaPrecio.getDescripcion());
        holder.tv_precioMayor.setText("S/. "+formateador.format(politicaPrecio.getPrecioManejo()));
        holder.tv_precioUnidad.setText("S/. "+formateador.format(politicaPrecio.getPrecioContenido()));

        return convertView;
    }
}
