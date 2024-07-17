package com.expediodigital.ventas360.view.fragment;

import android.os.Bundle;
import androidx.annotation.DimenRes;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.core.content.ContextCompat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TextView;

import com.expediodigital.ventas360.DAO.DAOCliente;
import com.expediodigital.ventas360.DAO.DAOConfiguracion;
import com.expediodigital.ventas360.DAO.DAOPedido;
import com.expediodigital.ventas360.R;
import com.expediodigital.ventas360.Ventas360App;
import com.expediodigital.ventas360.util.Util;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;

public class EstadisticaResumenMarcaFragment extends Fragment {
    public final String TAG = getClass().getName();
    private DAOPedido daoPedido;
    private DAOCliente daoCliente;
    private DAOConfiguracion daoConfiguracion;
    private TableLayout table_marcas;
    private DecimalFormat formateador;
    private TextView /*tv_totalUnidadMayor,tv_totalUnidadMenor,*/tv_totalNumeroClientes,tv_totalImporte,tv_totalEfectividad;
    private int /*totalUnidadMayor,totalUnidadMenor,*/totalNumeroClientes,clientesProgramados;
    private double totalImporte = 0,total_efectividad = 0;

    LinearLayout.LayoutParams paramsWEIGHT = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT,.5f);
    ViewGroup.LayoutParams paramsSMALL;
    ViewGroup.LayoutParams paramsMEDIUM;
    ViewGroup.LayoutParams paramsLARGE;

    Ventas360App ventas360App;
    public EstadisticaResumenMarcaFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_estadistica_resumen_marca, container, false);

        ventas360App = (Ventas360App) getActivity().getApplicationContext();
        daoPedido = new DAOPedido(getActivity());
        daoCliente = new DAOCliente(getActivity());
        daoConfiguracion = new DAOConfiguracion(getActivity());

        table_marcas        = view.findViewById(R.id.table_marcas);
//        tv_totalUnidadMayor = view.findViewById(R.id.tv_totalUnidadMayor);
//        tv_totalUnidadMenor = view.findViewById(R.id.tv_totalUnidadMenor);
        tv_totalNumeroClientes = view.findViewById(R.id.tv_totalNumeroClientes);
        tv_totalImporte     = view.findViewById(R.id.tv_totalImporte);
        tv_totalEfectividad = view.findViewById(R.id.tv_totalEfectividad);

        int table_column_size_small = (int) getResources().getDimension(R.dimen.table_column_size_small);
        int table_column_size_medium = (int) getResources().getDimension(R.dimen.table_column_size_medium);
        int table_column_size_large = (int) getResources().getDimension(R.dimen.table_column_size_large);
        paramsSMALL = new ViewGroup.LayoutParams(table_column_size_small, ViewGroup.LayoutParams.MATCH_PARENT);
        paramsMEDIUM = new ViewGroup.LayoutParams(table_column_size_medium, ViewGroup.LayoutParams.MATCH_PARENT);
        paramsLARGE = new ViewGroup.LayoutParams(table_column_size_large, ViewGroup.LayoutParams.MATCH_PARENT);
        formateador = Util.formateador();
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        String estadoVendedor = daoConfiguracion.getEstadoVendedor(ventas360App.getIdEmpresa(),ventas360App.getIdSucursal(),ventas360App.getIdVendedor());
        clientesProgramados = daoCliente.getNumeroClientesProgramados(ventas360App.getModoVenta(),estadoVendedor);
        ArrayList<HashMap<String,Object>> lista = daoPedido.getResumenVentaMarca();

        /*totalUnidadMayor=0;totalUnidadMenor=0;*/totalNumeroClientes=0;totalImporte=0;total_efectividad=0;
        int i = 0;
        for (HashMap<String,Object> item : lista){
            double efectividad = 0;
            /*if ((int)item.get("numeroClientes") != 0){
                efectividad = clientesProgramados/(int)item.get("numeroClientes");//Preguntar si esta bien
            }*/
            if (clientesProgramados != 0){
                efectividad = (int)item.get("numeroClientes")/(clientesProgramados*1.0d);
                efectividad = Util.redondearDouble(efectividad*100);
                //Log.d(TAG,item.get("numeroClientes")+"/"+clientesProgramados);
            }
            lista.get(i).put("efectividad",efectividad);

            if (i == lista.size()-1)
                agregarMarcaRow(item, true);
            else
                agregarMarcaRow(item, false);

//            totalUnidadMayor    += (int)item.get("unidadMayor");
//            totalUnidadMenor    += (int)item.get("unidadMenor");
            totalImporte        += (double)item.get("importe");
            totalNumeroClientes += (int)item.get("numeroClientes");
            total_efectividad += efectividad;
            i++;
        }
        if (clientesProgramados != 0)
            //total_efectividadtotal_efectividad = total_efectividad*100;//total_efectividad = (totalNumeroClientes/(clientesProgramados*1.0d))*100;
//        tv_totalUnidadMayor.setText(String.valueOf(totalUnidadMayor));
//        tv_totalUnidadMenor.setText(String.valueOf(totalUnidadMenor));
        tv_totalImporte.setText("S/. "+formateador.format((totalImporte)));
        tv_totalNumeroClientes.setText(String.valueOf(totalNumeroClientes));
        tv_totalEfectividad.setText(String.valueOf(Util.redondearDouble(total_efectividad))+" %");

    }

    private int dpToInt(@DimenRes int dimenRes){
        //return (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, (int) getResources().getDimension(dimenRes)/3, getResources().getDisplayMetrics());
        return (int) (getResources().getDimension(dimenRes) / getResources().getDisplayMetrics().density);
    }

    private void agregarMarcaRow(HashMap<String,Object> item, boolean isLastRow){
        LinearLayout tableRow = new LinearLayout(getActivity());

        TextView textViewMarca = new TextView(getActivity());
        textViewMarca.setText(item.get("marca").toString());
        textViewMarca.setTextColor(ContextCompat.getColor(getActivity(),R.color.grey_800));
        textViewMarca.setGravity(Gravity.CENTER);
        textViewMarca.setPadding(dpToInt(R.dimen.padding_item_row_layout),dpToInt(R.dimen.padding_item_row_layout),dpToInt(R.dimen.padding_item_row_layout),dpToInt(R.dimen.padding_item_row_layout));
        textViewMarca.setBackgroundResource(R.drawable.table_cell_bg);
        textViewMarca.setLayoutParams(paramsMEDIUM);

//        LinearLayout linearLayout = new LinearLayout(getActivity());
//        linearLayout.setOrientation(LinearLayout.HORIZONTAL);

//        TextView textViewUnidadMayor = new TextView(getActivity());
//        textViewUnidadMayor.setText(item.get("unidadMayor").toString());
//        textViewUnidadMayor.setGravity(Gravity.CENTER);
//        textViewUnidadMayor.setPadding(dpToInt(R.dimen.padding_item_row_layout),dpToInt(R.dimen.padding_item_row_layout),dpToInt(R.dimen.padding_item_row_layout),dpToInt(R.dimen.padding_item_row_layout));
//        textViewUnidadMayor.setBackgroundResource(R.drawable.table_cell_bg);

//        TextView textViewUnidadMenor = new TextView(getActivity());
//        textViewUnidadMenor.setText(item.get("unidadMenor").toString());
//        textViewUnidadMenor.setGravity(Gravity.CENTER);
//        textViewUnidadMenor.setPadding(dpToInt(R.dimen.padding_item_row_layout),dpToInt(R.dimen.padding_item_row_layout),dpToInt(R.dimen.padding_item_row_layout),dpToInt(R.dimen.padding_item_row_layout));
//        textViewUnidadMenor.setBackgroundResource(R.drawable.table_cell_bg);

//        linearLayout.addView(textViewUnidadMayor,paramsWEIGHT);
//        linearLayout.addView(textViewUnidadMenor,paramsWEIGHT);
//        linearLayout.setLayoutParams(paramsLARGE);

        TextView textImporte = new TextView(getActivity());
        textImporte.setText("S/. "+formateador.format((double)item.get("importe")));
        textImporte.setGravity(Gravity.CENTER_VERTICAL | Gravity.RIGHT);
        textImporte.setPadding(dpToInt(R.dimen.padding_item_row_layout),dpToInt(R.dimen.padding_item_row_layout),dpToInt(R.dimen.padding_item_row_layout),dpToInt(R.dimen.padding_item_row_layout));
        textImporte.setBackgroundResource(R.drawable.table_cell_bg);
        textImporte.setLayoutParams(paramsMEDIUM);

        TextView textClientes = new TextView(getActivity());
        textClientes.setText(item.get("numeroClientes").toString());
        textClientes.setGravity(Gravity.CENTER);
        textClientes.setPadding(dpToInt(R.dimen.padding_item_row_layout),dpToInt(R.dimen.padding_item_row_layout),dpToInt(R.dimen.padding_item_row_layout),dpToInt(R.dimen.padding_item_row_layout));
        textClientes.setBackgroundResource(R.drawable.table_cell_bg);
        textClientes.setLayoutParams(paramsSMALL);

        TextView textEfectividad = new TextView(getActivity());
        textEfectividad.setText(String.valueOf(Util.redondearDouble((double)item.get("efectividad")))+" %");
        textEfectividad.setGravity(Gravity.CENTER);
        textEfectividad.setPadding(dpToInt(R.dimen.padding_item_row_layout),dpToInt(R.dimen.padding_item_row_layout),dpToInt(R.dimen.padding_item_row_layout),dpToInt(R.dimen.padding_item_row_layout));
        textEfectividad.setLayoutParams(paramsSMALL);
        textEfectividad.setBackgroundColor(ContextCompat.getColor(getActivity(),R.color.white));
        //textEfectividad.setLayoutParams(paramsWRAP);//setLayoutParams, indica los parámetros del parentView, no del actual

        tableRow.addView(textViewMarca);
//        tableRow.addView(linearLayout);
        tableRow.addView(textImporte);
        tableRow.addView(textClientes);
        tableRow.addView(textEfectividad);

        tableRow.setPadding(1,1,1,1);
        if (isLastRow)
            tableRow.setBackgroundResource(R.drawable.table_row_last_bg_teal);
        else
            tableRow.setBackgroundResource(R.drawable.table_row_bg_teal);
        table_marcas.addView(tableRow,new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
    }
}
