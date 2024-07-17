package com.expediodigital.ventas360.adapter;

import android.app.Activity;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.expediodigital.ventas360.DAO.DAOPedido;
import com.expediodigital.ventas360.R;
import com.expediodigital.ventas360.model.ClienteCoordenadasModel;
import com.expediodigital.ventas360.model.ClienteModel;
import com.expediodigital.ventas360.quickaction.QuickAction;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by ASUS on 07/07/2017.
 */

public class RecyclerViewClienteAdapter extends RecyclerView.Adapter<RecyclerViewClienteAdapter.ClienteViewHolder> {
    public static final String TAG = "RViewClienteAdapter";
    private ArrayList<ClienteModel> listaCliente;
    private Activity activity;
    QuickAction quickAction;
    QuickAction quickAction2;
    private int positionSelectedQuickAction;
    private View viewSelectedQuickAction;
    DAOPedido daoPedido;
    
    public RecyclerViewClienteAdapter(ArrayList<ClienteModel> listaCliente, Activity activity) {
        this.listaCliente = listaCliente;
        this.activity = activity;
        daoPedido = new DAOPedido(activity);
    }



    @Override
    public RecyclerViewClienteAdapter.ClienteViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_cliente, parent, false);
        return new RecyclerViewClienteAdapter.ClienteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final RecyclerViewClienteAdapter.ClienteViewHolder holder, final int position) {
        final ClienteModel model = listaCliente.get(position);

        holder.tv_nomcliente.setText(model.getRazonSocial());
        holder.tv_codigoCliente.setText("Código: "+model.getIdCliente());
        if (model.getRucDni().length() == 11){
            holder.tv_rucdni.setText("RUC: "+model.getRucDni());
        }else{
            holder.tv_rucdni.setText("DNI: "+model.getRucDni());
        }

        holder.tv_direccion.setText(model.getDireccion());
        holder.tv_orden.setText(model.getOrden()+"");

        if (model.getEstadoPedido() == ClienteModel.ESTADO_PEDIDO_VISITADO){
            holder.img_icon.setFillColorResource(R.color.green_500);
        }else if (model.getEstadoPedido() == ClienteModel.ESTADO_PEDIDO_ANULADO){
            holder.img_icon.setFillColorResource(R.color.red_400);
        }else{
            holder.img_icon.setFillColorResource(R.color.blue_grey_500);
        }

        if (model.getFlagLocalizacion().isEmpty())
            holder.img_location.setImageResource(R.drawable.icon_location_off);
        else if (model.getFlagLocalizacion().equals(ClienteCoordenadasModel.FLAG_ENVIADO))
            holder.img_location.setImageResource(R.drawable.icon_location_green);
        else if (model.getFlagLocalizacion().equals(ClienteCoordenadasModel.FLAG_PENDIENTE))
            holder.img_location.setImageResource(R.drawable.icon_location_grey);

        if(model.getWhatsapp().isEmpty()){
            holder.img_whatsapp.setImageResource(R.drawable.ic_whatsapp_off);
        }
        else{
            holder.img_whatsapp.setImageResource(R.drawable.ic_whatsapp);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (daoPedido.tienePedido(model.getIdCliente())){
                    if (quickAction2 != null){
                        quickAction2.show(v);
                    }
                }else{
                    if (quickAction != null){
                        quickAction.show(v);
                    }
                }
                positionSelectedQuickAction = position;
                viewSelectedQuickAction = holder.img_icon;
            }
        });

    }

    public void setQuickAction(QuickAction quickAction){
        this.quickAction = quickAction;
    }
    public void setQuickAction2(QuickAction quickAction2){
        this.quickAction2 = quickAction2;
    }
    public ClienteModel getItemSelectedByQuickAction(){
        return listaCliente.get(positionSelectedQuickAction);
    }

    public View getViewItemSelectedByQuickAction(){
        return viewSelectedQuickAction;
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
        private TextView tv_orden;
        private CircleImageView img_icon;
        private ImageView img_location, img_whatsapp;

        public ClienteViewHolder(View itemView) {
            super(itemView);
            tv_nomcliente = itemView.findViewById(R.id.tv_nomcliente);
            tv_codigoCliente = itemView.findViewById(R.id.tv_codigoCliente);
            tv_rucdni = itemView.findViewById(R.id.tv_rucdni);
            tv_direccion = itemView.findViewById(R.id.tv_direccion);
            tv_orden = itemView.findViewById(R.id.tv_orden);
            img_icon = itemView.findViewById(R.id.img_icon);
            img_location = itemView.findViewById(R.id.img_location);
            img_whatsapp = itemView.findViewById(R.id.img_whatsapp);
        }
    }
}
