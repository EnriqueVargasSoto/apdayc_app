package com.expediodigital.ventas360.adapter;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.expediodigital.ventas360.R;
import com.expediodigital.ventas360.view.ListaDispositivosActivity;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Kevin Robinson Meza Hinostroza on julio 2017.
 * Expedio Digital
 * Meza.Hinostroza.Robin@gmail.com
 */

public class RecyclerViewDispositivoAdapter extends RecyclerView.Adapter<RecyclerViewDispositivoAdapter.DispositivoViewHolder> {
    private ArrayList<HashMap<String,String>> lista;
    private ListaDispositivosActivity activity;

    public RecyclerViewDispositivoAdapter(ArrayList<HashMap<String,String>> lista, ListaDispositivosActivity activity){
        this.lista = lista;
        this.activity = activity;
    }

    @Override
    public DispositivoViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_dispositivo, parent, false);
        return new DispositivoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(DispositivoViewHolder holder, final int position) {
        final HashMap<String,String> item = lista.get(position);
        holder.tv_dispositivo.setText(item.get("dispositivo"));
        holder.tv_direccion.setText(item.get("direccion"));

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                activity.onClickDispositivo(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return lista.size();
    }

    public class DispositivoViewHolder extends RecyclerView.ViewHolder {
        private TextView tv_dispositivo;
        private TextView tv_direccion;

        public DispositivoViewHolder(View itemView) {
            super(itemView);
            tv_dispositivo = itemView.findViewById(R.id.tv_dispositivo);
            tv_direccion = itemView.findViewById(R.id.tv_direccion);
        }
    }
}
