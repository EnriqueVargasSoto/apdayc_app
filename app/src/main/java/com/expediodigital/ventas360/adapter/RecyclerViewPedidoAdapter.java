package com.expediodigital.ventas360.adapter;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import com.expediodigital.ventas360.DAO.DAOPedido;
import com.expediodigital.ventas360.R;
import com.expediodigital.ventas360.model.PedidoCabeceraModel;
import com.expediodigital.ventas360.quickaction.QuickAction;
import com.expediodigital.ventas360.util.Util;
import com.expediodigital.ventas360.view.fragment.PedidosFragment;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Kevin Robinson Meza Hinostroza on julio 2017.
 * Expedio Digital
 * Meza.Hinostroza.Robin@gmail.com
 */

public class RecyclerViewPedidoAdapter extends RecyclerView.Adapter<RecyclerViewPedidoAdapter.PedidoViewHolder> {
    private final String TAG = getClass().getName();
    private ArrayList<PedidoCabeceraModel> lista;
    private PedidosFragment fragment;
    DecimalFormat formateador;
    QuickAction quickAction;
    int positionSelectedQuickAction;
    boolean marcarEntregados;
    DAOPedido daoPedido;

    public RecyclerViewPedidoAdapter(ArrayList<PedidoCabeceraModel> lista, boolean marcarEntregados, PedidosFragment fragment){
        this.lista = lista;
        this.marcarEntregados = marcarEntregados;
        this.fragment = fragment;
        this.formateador = Util.formateador();
        this.daoPedido = new DAOPedido(fragment.getActivity());
    }

    @Override
    public PedidoViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_pedido, parent, false);
        return new RecyclerViewPedidoAdapter.PedidoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final PedidoViewHolder holder, final int position) {
        final PedidoCabeceraModel model = lista.get(position);
        String numeropedidoSimple = model.getNumeroPedido().substring(6,model.getNumeroPedido().length());
        holder.tv_numeroPedido.setText(numeropedidoSimple);
        holder.tv_cliente.setText(model.getNombreCliente());
        holder.tv_montoTotal.setText("S/. "+formateador.format(model.getImporteTotal()));
        holder.tv_condicionPago.setText(model.getFormaPago());

        int color = fragment.getActivity().getResources().getColor(R.color.red_400);
        String flagTexto = "Pendiente";

        switch (model.getFlag()) {
            case PedidoCabeceraModel.FLAG_ENVIADO:
                color = fragment.getActivity().getResources().getColor(R.color.green_500);
                flagTexto = "Enviado";
                break;
            case "M"://El servidor también envia M en caso el pedido haya sido modificado
                color = fragment.getActivity().getResources().getColor(R.color.green_500);
                flagTexto = "Enviado";
                break;
            case PedidoCabeceraModel.FLAG_PENDIENTE:
                color = fragment.getActivity().getResources().getColor(R.color.red_400);
                flagTexto = "Pendiente";
                break;
            case "I":
                color = fragment.getActivity().getResources().getColor(R.color.yellow_500);
                flagTexto = "Incompleto";
                break;
            case "T":
                color = fragment.getActivity().getResources().getColor(R.color.purple_200);
                flagTexto = "Transferido";
                break;
            default:
                holder.view_flag.setBackgroundColor(color);
        }

        if (model.getEstado().equals(PedidoCabeceraModel.ESTADO_FACTURADO)){
            holder.tv_flag.setTextColor(fragment.getActivity().getResources().getColor(R.color.purple_500));
            holder.view_flag.setBackgroundColor(fragment.getActivity().getResources().getColor(R.color.purple_500));
            holder.tv_flag.setText("Facturado");
        }else{
            holder.tv_flag.setTextColor(color);
            holder.view_flag.setBackgroundColor(color);
            holder.tv_flag.setText(flagTexto);
        }

        if (model.getEstado().equals(PedidoCabeceraModel.ESTADO_ANULADO)){
            holder.tv_cliente.setTextColor(ContextCompat.getColor(fragment.getActivity(), R.color.red_400));
            holder.tv_motivoNoVenta.setText(model.getMotivoNoVenta());

            holder.tv_condicionPago.setVisibility(View.GONE);
            holder.tv_motivoNoVenta.setVisibility(View.VISIBLE);
        }else{
            holder.tv_cliente.setTextColor(ContextCompat.getColor(fragment.getActivity(), androidx.appcompat.R.color.abc_secondary_text_material_light));
            holder.tv_motivoNoVenta.setText("");
            holder.tv_condicionPago.setVisibility(View.VISIBLE);
            holder.tv_motivoNoVenta.setVisibility(View.GONE);
        }

        holder.check_entregado.setOnCheckedChangeListener(null);//Por alguna razón es necesario para que se mantengan los estados de los checkbox cuando se reciclen, esto debe ir antes de toda interacción en el checkbox

        if (marcarEntregados){
            holder.check_entregado.setVisibility(View.VISIBLE);
            if (model.getPedidoEntregado() == 1)
                holder.check_entregado.setChecked(true);
            else
                holder.check_entregado.setChecked(false);
        }else {
            holder.check_entregado.setVisibility(View.GONE);
        }

        holder.check_entregado.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                if (model.getEstado().equals(PedidoCabeceraModel.ESTADO_FACTURADO)){
                    String fecha = Util.getFechaHoraTelefonoString();

                    if (checked) {
                        lista.get(position).setPedidoEntregado(1);
                    }else {
                        lista.get(position).setPedidoEntregado(0);
                        fecha = "";
                    }
                    daoPedido.actualizarPedidoEntregado(model.getNumeroPedido(), checked, fecha);
                    //Enviar al servicio indicando que ya se entregó este pedido
                    fragment.EnviarEntregaPedido(model.getNumeroPedido(), checked, position);
                }else{
                    Toast.makeText(fragment.getActivity(),"Solo se pueden marcar los facturados",Toast.LENGTH_SHORT).show();
                    holder.check_entregado.setChecked(!checked);
                }
            }
        });

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG,"itemView.setOnClickListener");
                if (quickAction != null){
                    positionSelectedQuickAction = position;
                    quickAction.show(v);
                }
            }
        });
        //holder.setIsRecyclable(false);
    }

    public void setQuickAction(QuickAction quickAction){
        this.quickAction = quickAction;
    }

    public PedidoCabeceraModel getItemSelectedByQuickAction(){
        return lista.get(positionSelectedQuickAction);
    }
    @Override
    public int getItemCount() {
        return lista.size();
    }

    public void setFilter(List<PedidoCabeceraModel> listaPedidosFiltrada) {
        this.lista = new ArrayList<>();
        this.lista.addAll(listaPedidosFiltrada);
        Log.d(TAG,"SETEANDO FILTRO PARA EL ADAPTER !!");
        notifyDataSetChanged();
    }

    public class PedidoViewHolder extends RecyclerView.ViewHolder {
        private TextView tv_numeroPedido;
        private TextView tv_cliente;
        private TextView tv_montoTotal;
        private TextView tv_condicionPago;
        private TextView tv_motivoNoVenta;
        private TextView tv_flag;
        private CheckBox check_entregado;
        private View view_flag;

        public PedidoViewHolder(View itemView) {
            super(itemView);
            tv_numeroPedido = itemView.findViewById(R.id.tv_numeroPedido);
            tv_cliente = itemView.findViewById(R.id.tv_cliente);
            tv_montoTotal = itemView.findViewById(R.id.tv_montoTotal);
            tv_condicionPago = itemView.findViewById(R.id.tv_condicionPago);
            tv_motivoNoVenta = itemView.findViewById(R.id.tv_motivoNoVenta);
            tv_flag = itemView.findViewById(R.id.tv_flag);
            check_entregado = itemView.findViewById(R.id.check_entregado);
            view_flag = itemView.findViewById(R.id.view_flag);
        }

    }
}
