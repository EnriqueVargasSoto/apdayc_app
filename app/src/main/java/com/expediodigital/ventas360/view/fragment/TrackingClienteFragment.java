package com.expediodigital.ventas360.view.fragment;


import android.graphics.Color;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.core.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.expediodigital.ventas360.DAO.DAOCliente;
import com.expediodigital.ventas360.R;
import com.expediodigital.ventas360.model.HojaRutaIndicadorModel;
import com.expediodigital.ventas360.model.HojaRutaMarcasModel;
import com.expediodigital.ventas360.util.Util;
import com.expediodigital.ventas360.view.DetalleClienteActivity;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.text.DecimalFormat;
import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 */
public class TrackingClienteFragment extends Fragment {
    private TextView tv_tipoCobertura,tv_hitRate,tv_avanceAnual,tv_avanceMes,tv_programado,tv_transcurrido,tv_liquidado
            ,tv_venAnoAnterior,tv_venMesAnterior,tv_avanceMesActual,tv_proyectado
            ,tv_coberturaMultiple,tv_cuotaGTM,tv_segmento,tv_numeroExhibidores,tv_numeroPuertasFrios;
    private PieChart piechart_marcas;
    private DAOCliente daoCliente;
    private String idCliente = "";

    public TrackingClienteFragment() {}


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tracking_cliente, container, false);

        tv_tipoCobertura    = view.findViewById(R.id.tv_tipoCobertura);
        tv_programado       = view.findViewById(R.id.tv_programado);
        tv_transcurrido     = view.findViewById(R.id.tv_transcurrido);
        tv_liquidado        = view.findViewById(R.id.tv_liquidado);
        tv_hitRate          = view.findViewById(R.id.tv_hitRate);

        tv_venAnoAnterior   = view.findViewById(R.id.tv_venAnoAnterior);
        tv_venMesAnterior   = view.findViewById(R.id.tv_venMesAnterior);
        tv_avanceMesActual  = view.findViewById(R.id.tv_avanceMesActual);
        tv_proyectado       = view.findViewById(R.id.tv_proyectado);
        tv_avanceAnual      = view.findViewById(R.id.tv_avanceAnual);
        tv_avanceMes        = view.findViewById(R.id.tv_avanceMes);

        tv_coberturaMultiple     = view.findViewById(R.id.tv_coberturaMultiple);
        tv_cuotaGTM             = view.findViewById(R.id.tv_cuotaGTM);
        tv_segmento             = view.findViewById(R.id.tv_segmento);
        tv_numeroExhibidores    = view.findViewById(R.id.tv_numeroExhibidores);
        tv_numeroPuertasFrios   = view.findViewById(R.id.tv_numeroPuertasFrios);

        piechart_marcas     = view.findViewById(R.id.piechart_marcas);

        setUpCharts();

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        daoCliente = new DAOCliente(getActivity());
        DecimalFormat decimalFormat = Util.formateador();

        try{
            idCliente = ((DetalleClienteActivity)getActivity()).getIdCliente();
            HojaRutaIndicadorModel indicadorModel = daoCliente.getHojaRutaIndicador(idCliente);

            tv_tipoCobertura.setText(indicadorModel.getTipoCobertura());
            tv_programado.setText(String.valueOf(indicadorModel.getProgramado()));
            tv_transcurrido.setText(String.valueOf(indicadorModel.getTranscurrido()));
            tv_liquidado.setText(String.valueOf(indicadorModel.getLiquidado()));
            tv_hitRate.setText((indicadorModel.getHitRate() * 100 )+"%");

            tv_venAnoAnterior.setText("S/. "+decimalFormat.format(indicadorModel.getVenAnoAnterior()));
            tv_venMesAnterior.setText("S/. "+decimalFormat.format(indicadorModel.getVenMesAnterior()));
            tv_avanceMesActual.setText("S/. "+decimalFormat.format(indicadorModel.getAvanceMesActual()));
            tv_proyectado.setText("S/. "+decimalFormat.format(indicadorModel.getProyectado()));
            tv_avanceAnual.setText(Util.redondearDouble(indicadorModel.getAvanceAnual() * 100)+"%");
            tv_avanceMes.setText(Util.redondearDouble(indicadorModel.getAvanceMes() * 100)+"%");

            tv_coberturaMultiple.setText(decimalFormat.format(indicadorModel.getCoberturaMultiple()) + "%");
            tv_cuotaGTM.setText("S/. "+decimalFormat.format(indicadorModel.getCUOTAGTM()));
            tv_segmento.setText(indicadorModel.getSEGMENTO());
            tv_numeroExhibidores.setText(String.valueOf(indicadorModel.getEXHIBIDORES()));
            tv_numeroPuertasFrios.setText(String.valueOf(indicadorModel.getNROPTAFRIOGTM()));


            if (indicadorModel.getLiquidado() == 0)
                tv_liquidado.setTextColor(ContextCompat.getColor(getActivity(),R.color.red_400));
        }catch (Exception e){
            e.printStackTrace();
        }

        cargarMarcas();
    }

    private void setUpCharts(){
        piechart_marcas.setDescription(null);
        piechart_marcas.setRotationEnabled(true);
        piechart_marcas.setUsePercentValues(true);
        piechart_marcas.setHoleRadius(16f);
        piechart_marcas.setTransparentCircleRadius(24f);
        piechart_marcas.setTransparentCircleAlpha(150);

        Legend legend = piechart_marcas.getLegend();
        legend.setForm(Legend.LegendForm.CIRCLE);
        legend.setOrientation(Legend.LegendOrientation.VERTICAL);
        legend.setPosition(Legend.LegendPosition.RIGHT_OF_CHART_INSIDE);
    }

    private void cargarMarcas() {
        try{
            ArrayList<HojaRutaMarcasModel> listaMarcas = daoCliente.getHojaRutaMarcas(idCliente);

            String[] xData = new String[listaMarcas.size()];
            float[] yData = new float[listaMarcas.size()];

            for (int i = 0; i<listaMarcas.size(); i++){
                xData[i] = listaMarcas.get(i).getMarca()+"("+listaMarcas.get(i).getCantidadPaquetes()+")";
                yData[i] = listaMarcas.get(i).getCantidadPaquetes();
            }

            ArrayList<PieEntry> yEntrys = new ArrayList<>();

            for(int i = 0; i < yData.length; i++){
                yEntrys.add(new PieEntry(yData[i] , xData[i]));
            }
            //create the data set
            PieDataSet pieDataSet = new PieDataSet(yEntrys,"");
            pieDataSet.setSliceSpace(2);
            pieDataSet.setValueTextSize(12);

            pieDataSet.setColors(ColorTemplate.MATERIAL_COLORS);

            //create pie data object
            PieData data = new PieData(pieDataSet);
            data.setValueFormatter(new PercentFormatter());
            data.setValueTextSize(18f);
            data.setValueTextColor(Color.WHITE);

            if (listaMarcas.isEmpty())
                piechart_marcas.setNoDataText("No hay data disponible");
            else
                piechart_marcas.setData(data);

            piechart_marcas.invalidate();

        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
