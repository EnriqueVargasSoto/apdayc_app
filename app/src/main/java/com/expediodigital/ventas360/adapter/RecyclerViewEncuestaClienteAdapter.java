package com.expediodigital.ventas360.adapter;

import android.app.Activity;
import android.content.Intent;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.expediodigital.ventas360.R;
import com.expediodigital.ventas360.model.ClienteModel;
import com.expediodigital.ventas360.model.EncuestaRespuestaModel;
import com.expediodigital.ventas360.quickaction.QuickAction;
import com.expediodigital.ventas360.view.EncuestaClienteActivity;
import com.expediodigital.ventas360.view.EncuestasClientesActivity;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Kevin Robinson Meza Hinostroza on marzo 2018.
 * Expedio Digital
 * Kevin.Meza@expediodigital.com
 */

public class RecyclerViewEncuestaClienteAdapter extends RecyclerView.Adapter<RecyclerViewEncuestaClienteAdapter.ClienteViewHolder> {
    public static final String TAG = "RecyclerViewEncuestaClienteAdapter";
    private ArrayList<ClienteModel> listaCliente;
    private Activity activity;
    private int idEncuesta;
    private int idEncuestaDetalle;
    private String descripcionEncuesta;
    private String tipoEncuesta;
    private boolean clickListener;
    QuickAction quickAction;
    private int positionSelectedQuickAction;

    public RecyclerViewEncuestaClienteAdapter(ArrayList<ClienteModel> listaCliente, Activity activity, int idEncuesta, int idEncuestaDetalle, String descripcionEncuesta, String tipoEncuesta, boolean clickListener) {
        this.listaCliente = listaCliente;
        this.activity = activity;
        this.idEncuesta = idEncuesta;
        this.idEncuestaDetalle = idEncuestaDetalle;
        this.descripcionEncuesta = descripcionEncuesta;
        this.tipoEncuesta = tipoEncuesta;
        this.clickListener = clickListener;
    }

    @Override
    public RecyclerViewEncuestaClienteAdapter.ClienteViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_cliente_encuesta, parent, false);
        return new RecyclerViewEncuestaClienteAdapter.ClienteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerViewEncuestaClienteAdapter.ClienteViewHolder holder, final int position) {
        final ClienteModel model = listaCliente.get(position);

        holder.tv_nomcliente.setText(model.getRazonSocial());
        holder.tv_codigoCliente.setText("Código: "+model.getIdCliente());
        if (model.getRucDni().length() == 11){
            holder.tv_rucdni.setText("RUC: "+model.getRucDni());
        }else{
            holder.tv_rucdni.setText("DNI: "+model.getRucDni());
        }

        holder.tv_direccion.setText(model.getDireccion());

        if (model.tieneEncuesta()){
            holder.img_icon.setFillColorResource(R.color.green_500);
        }else{
            holder.img_icon.setFillColorResource(R.color.blue_grey_500);
        }

        switch (model.getFlagEncuesta()){
            case EncuestaRespuestaModel.FLAG_ENVIADO:
                holder.tv_flag.setText("Enviado");
                holder.tv_flag.setTextColor(ContextCompat.getColor(activity,R.color.green_500));
                break;
            case EncuestaRespuestaModel.FLAG_PENDIENTE:
                holder.tv_flag.setText("Pendiente");
                holder.tv_flag.setTextColor(ContextCompat.getColor(activity,R.color.red_400));
                break;
            case EncuestaRespuestaModel.FLAG_INCOMPLETO:
                holder.tv_flag.setText("Pendiente");
                holder.tv_flag.setTextColor(ContextCompat.getColor(activity,R.color.yellow_800));
                break;
            default:
                holder.tv_flag.setText("");
        }

        if (clickListener){
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (quickAction != null){
                        quickAction.show(v);
                    }
                    positionSelectedQuickAction = position;
                }
            });
        }else{
            holder.itemView.setOnClickListener(null);
        }


    }

    public void setQuickAction(QuickAction quickAction){
        this.quickAction = quickAction;
    }

    public ClienteModel getItemSelectedByQuickAction(){
        return listaCliente.get(positionSelectedQuickAction);
    }

    @Override
    public int getItemCount() { return listaCliente.size();  }

    public void setFilter(List<ClienteModel> listaClienteFiltrada) {
        this.listaCliente = new ArrayList<>();
        this.listaCliente.addAll(listaClienteFiltrada);
        notifyDataSetChanged();
    }

    public class ClienteViewHolder extends RecyclerView.ViewHolder {
        private TextView tv_nomcliente;
        private TextView tv_codigoCliente;
        private TextView tv_rucdni;
        private TextView tv_direccion;
        private TextView tv_flag;
        private CircleImageView img_icon;

        public ClienteViewHolder(View itemView) {
            super(itemView);
            tv_nomcliente = itemView.findViewById(R.id.tv_nomcliente);
            tv_codigoCliente = itemView.findViewById(R.id.tv_codigoCliente);
            tv_rucdni = itemView.findViewById(R.id.tv_rucdni);
            tv_direccion = itemView.findViewById(R.id.tv_direccion);
            tv_flag = itemView.findViewById(R.id.tv_flag);
            img_icon = itemView.findViewById(R.id.img_icon);
        }
    }
}
