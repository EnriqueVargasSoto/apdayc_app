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

import com.expediodigital.ventas360.DAO.DAOConfiguracion;
import com.expediodigital.ventas360.DAO.DAOPedido;
import com.expediodigital.ventas360.R;
import com.expediodigital.ventas360.Ventas360App;
import com.expediodigital.ventas360.util.Util;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;

public class EstadisticaResumenClienteFragment extends Fragment {
    public final String TAG = getClass().getName();
    private DAOPedido daoPedido;
    private DAOConfiguracion daoConfiguracion;
    private TableLayout table_segmentos;
    private DecimalFormat formateador;
    private TextView tv_totalProgramados,tv_totalEfectivos,/*tv_totalPaquetes,*/tv_totalSoles,/*tv_totalDropPaquetes,*/tv_totalDropSoles;
    private int totalProgramados,totalEfectivos,totalPaquetes;
    private double totalSoles,totalDropPaquetes,totalDropSoles;

    LinearLayout.LayoutParams paramsWEIGHT = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT,.5f);
    ViewGroup.LayoutParams paramsSMALL;
    ViewGroup.LayoutParams paramsMEDIUM;
    ViewGroup.LayoutParams paramsLARGE;

    Ventas360App ventas360App;

    public EstadisticaResumenClienteFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_estadistica_resumen_cliente, container, false);
        ventas360App = (Ventas360App) getActivity().getApplicationContext();
        daoPedido = new DAOPedido(getActivity());
        daoConfiguracion = new DAOConfiguracion(getActivity());

        table_segmentos     = view.findViewById(R.id.table_segmentos);
        tv_totalProgramados = view.findViewById(R.id.tv_totalProgramados);
        tv_totalEfectivos   = view.findViewById(R.id.tv_totalEfectivos);
//        tv_totalPaquetes    = view.findViewById(R.id.tv_totalPaquetes);
        tv_totalSoles       = view.findViewById(R.id.tv_totalSoles);
//        tv_totalDropPaquetes= view.findViewById(R.id.tv_totalDropPaquetes);
        tv_totalDropSoles   = view.findViewById(R.id.tv_totalDropSoles);

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
        totalProgramados = 0; totalEfectivos=0; totalPaquetes=0; totalSoles=0; totalDropPaquetes=0; totalDropSoles=0;
        String estadoVendedor = daoConfiguracion.getEstadoVendedor(ventas360App.getIdEmpresa(),ventas360App.getIdSucursal(),ventas360App.getIdVendedor());
        ArrayList<HashMap<String,Object>> lista = daoPedido.getResumenVentaSegmento(ventas360App.getModoVenta(),estadoVendedor);
        int i = 0;
        for (HashMap<String,Object> item : lista){
            double dropPaquetes = 0;
            double dropSoles = 0;
            if ((int)item.get("efectivos") != 0){
                dropPaquetes = Util.redondearDouble((int)item.get("unidadMayor")/((int)item.get("efectivos")*1.0d));
                dropSoles = Util.redondearDouble((double)item.get("importe")/(int)item.get("efectivos"));
            }
            item.put("dropPaquetes",dropPaquetes);
            item.put("dropSoles",dropSoles);

            if (i == lista.size()-1)
                agregarMarcaRow(item, true);
            else
                agregarMarcaRow(item, false);

            totalProgramados    += (int)item.get("programados");
            totalEfectivos      += (int)item.get("efectivos");
//            totalPaquetes       += (int)item.get("unidadMayor");
            totalSoles          += (double)item.get("importe");
//            totalDropPaquetes   += (double)item.get("dropPaquetes");
            totalDropSoles      += (double)item.get("dropSoles");
            i++;
        }
        tv_totalProgramados.setText(String.valueOf(totalProgramados));
        tv_totalEfectivos.setText(String.valueOf(totalEfectivos));
//        tv_totalPaquetes.setText(String.valueOf(totalPaquetes));
        tv_totalSoles.setText("S/. "+formateador.format((totalSoles)));
//        tv_totalDropPaquetes.setText(String.valueOf(Util.redondearDouble(totalDropPaquetes)));
        tv_totalDropSoles.setText("S/. "+formateador.format(Util.redondearDouble(totalDropSoles)));

    }

    private int dpToInt(@DimenRes int dimenRes){
        //return (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, (int) getResources().getDimension(dimenRes)/3, getResources().getDisplayMetrics());
        return (int) (getResources().getDimension(dimenRes) / getResources().getDisplayMetrics().density);
    }

    private void agregarMarcaRow(HashMap<String,Object> item, boolean isLastRow){
        LinearLayout tableRow = new LinearLayout(getActivity());

        TextView textViewSegmento = new TextView(getActivity());
        textViewSegmento.setText(item.get("segmento").toString());
        textViewSegmento.setTextColor(ContextCompat.getColor(getActivity(),R.color.grey_800));
        textViewSegmento.setGravity(Gravity.CENTER);
        textViewSegmento.setPadding(dpToInt(R.dimen.padding_item_row_layout),dpToInt(R.dimen.padding_item_row_layout),dpToInt(R.dimen.padding_item_row_layout),dpToInt(R.dimen.padding_item_row_layout));
        textViewSegmento.setBackgroundResource(R.drawable.table_cell_bg);
        textViewSegmento.setLayoutParams(paramsLARGE);

        TextView textProgramados = new TextView(getActivity());
        textProgramados.setText(item.get("programados").toString());
        textProgramados.setGravity(Gravity.CENTER);
        textProgramados.setPadding(dpToInt(R.dimen.padding_item_row_layout),dpToInt(R.dimen.padding_item_row_layout),dpToInt(R.dimen.padding_item_row_layout),dpToInt(R.dimen.padding_item_row_layout));
        textProgramados.setBackgroundResource(R.drawable.table_cell_bg);
        textProgramados.setLayoutParams(paramsMEDIUM);

        TextView textEfectivos = new TextView(getActivity());
        textEfectivos.setText(item.get("efectivos").toString());
        textEfectivos.setGravity(Gravity.CENTER);
        textEfectivos.setPadding(dpToInt(R.dimen.padding_item_row_layout),dpToInt(R.dimen.padding_item_row_layout),dpToInt(R.dimen.padding_item_row_layout),dpToInt(R.dimen.padding_item_row_layout));
        textEfectivos.setBackgroundResource(R.drawable.table_cell_bg);
        textEfectivos.setLayoutParams(paramsSMALL);

//        TextView textUnidadMayor = new TextView(getActivity());
//        textUnidadMayor.setText(item.get("unidadMayor").toString());
//        textUnidadMayor.setGravity(Gravity.CENTER);
//        textUnidadMayor.setPadding(dpToInt(R.dimen.padding_item_row_layout),dpToInt(R.dimen.padding_item_row_layout),dpToInt(R.dimen.padding_item_row_layout),dpToInt(R.dimen.padding_item_row_layout));
//        textUnidadMayor.setBackgroundResource(R.drawable.table_cell_bg);
//        textUnidadMayor.setLayoutParams(paramsSMALL);

        TextView textImporte = new TextView(getActivity());
        textImporte.setText("S/. "+formateador.format((double)item.get("importe")));
        textImporte.setGravity(Gravity.CENTER_VERTICAL | Gravity.RIGHT);
        textImporte.setPadding(dpToInt(R.dimen.padding_item_row_layout),dpToInt(R.dimen.padding_item_row_layout),dpToInt(R.dimen.padding_item_row_layout),dpToInt(R.dimen.padding_item_row_layout));
        textImporte.setBackgroundResource(R.drawable.table_cell_bg);
        textImporte.setLayoutParams(paramsMEDIUM);

//        TextView textDropPaquetes = new TextView(getActivity());
//        textDropPaquetes.setText(item.get("dropPaquetes").toString());
//        textDropPaquetes.setGravity(Gravity.CENTER);
//        textDropPaquetes.setPadding(dpToInt(R.dimen.padding_item_row_layout),dpToInt(R.dimen.padding_item_row_layout),dpToInt(R.dimen.padding_item_row_layout),dpToInt(R.dimen.padding_item_row_layout));
//        textDropPaquetes.setBackgroundResource(R.drawable.table_cell_bg);
//        textDropPaquetes.setLayoutParams(paramsSMALL);

        TextView textDropSoles = new TextView(getActivity());
        textDropSoles.setText("S/. "+formateador.format((double)item.get("dropSoles")));
        textDropSoles.setGravity(Gravity.CENTER_VERTICAL | Gravity.RIGHT);
        textDropSoles.setPadding(dpToInt(R.dimen.padding_item_row_layout),dpToInt(R.dimen.padding_item_row_layout),dpToInt(R.dimen.padding_item_row_layout),dpToInt(R.dimen.padding_item_row_layout));
        textDropSoles.setBackgroundColor(ContextCompat.getColor(getActivity(),R.color.white));
        textDropSoles.setLayoutParams(paramsMEDIUM);

        tableRow.addView(textViewSegmento);
        tableRow.addView(textProgramados);
        tableRow.addView(textEfectivos);
//        tableRow.addView(textUnidadMayor);
        tableRow.addView(textImporte);
//        tableRow.addView(textDropPaquetes);
        tableRow.addView(textDropSoles);

        tableRow.setPadding(1,1,1,1);
        if (isLastRow)
            tableRow.setBackgroundResource(R.drawable.table_row_last_bg_teal);
        else
            tableRow.setBackgroundResource(R.drawable.table_row_bg_teal);
        table_segmentos.addView(tableRow,new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
    }
}
