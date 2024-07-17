package com.expediodigital.ventas360.view.fragment;


import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.expediodigital.ventas360.DAO.DAOCliente;
import com.expediodigital.ventas360.DAO.DAOConfiguracion;
import com.expediodigital.ventas360.R;
import com.expediodigital.ventas360.Ventas360App;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 */
public class EstadisticaGraficoFragment extends Fragment {
    final String TAG = getClass().getName();
    private PieChart piechart_visitados;
    private PieChart piechart_efectivos;
    private DAOCliente daoCliente;
    private DAOConfiguracion daoConfiguracion;
    private Ventas360App ventas360App;
    private String estadoVendedor="";

    public EstadisticaGraficoFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_estadistica_grafico, container, false);
        ventas360App = (Ventas360App) getActivity().getApplicationContext();
        daoCliente = new DAOCliente(getActivity());
        daoConfiguracion = new DAOConfiguracion(getActivity());

        Typeface myTypeface = Typeface.createFromAsset(getActivity().getAssets(), "fonts/prime_regular.otf");

        piechart_visitados = view.findViewById(R.id.piechart_visitados);
        piechart_efectivos = view.findViewById(R.id.piechart_efectivos);
        TextView tv_programadosVSvisitados = view.findViewById(R.id.tv_programadosVSvisitados);
        TextView tv_programadosVSefectivos = view.findViewById(R.id.tv_programadosVSefectivos);
        tv_programadosVSvisitados.setTypeface(myTypeface);
        tv_programadosVSefectivos.setTypeface(myTypeface);

        setUpCharts();
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        estadoVendedor = daoConfiguracion.getEstadoVendedor(ventas360App.getIdEmpresa(), ventas360App.getIdSucursal(), ventas360App.getIdVendedor());
        cargarVisitados();
        cargarEfectivos();
    }

    private void setUpCharts(){
        piechart_efectivos.setDescription(null);
        piechart_efectivos.setRotationEnabled(true);
        piechart_efectivos.setUsePercentValues(true);
        piechart_efectivos.setHoleRadius(16f);
        piechart_efectivos.setTransparentCircleRadius(24f);
        piechart_efectivos.setTransparentCircleAlpha(150);

        Legend legend = piechart_efectivos.getLegend();
        legend.setForm(Legend.LegendForm.CIRCLE);
        legend.setOrientation(Legend.LegendOrientation.VERTICAL);
        legend.setPosition(Legend.LegendPosition.RIGHT_OF_CHART_INSIDE);


        piechart_visitados.setDescription(null);
        piechart_visitados.setRotationEnabled(true);
        piechart_visitados.setUsePercentValues(true);
        piechart_visitados.setHoleRadius(16f);
        piechart_visitados.setTransparentCircleRadius(24f);
        piechart_visitados.setTransparentCircleAlpha(150);

        Legend legend2 = piechart_visitados.getLegend();
        legend2.setForm(Legend.LegendForm.CIRCLE);
        legend2.setOrientation(Legend.LegendOrientation.VERTICAL);
        legend2.setPosition(Legend.LegendPosition.RIGHT_OF_CHART_INSIDE);
    }

    private void cargarVisitados() {
        int numeroClientesProgramados = daoCliente.getNumeroClientesProgramados(ventas360App.getModoVenta(), estadoVendedor);
        int numeroClientesVisitados = daoCliente.getNumeroClientesVisitados(ventas360App.getModoVenta(), estadoVendedor);

        String[] xData = {"Programados("+numeroClientesProgramados+")", "Visitados("+numeroClientesVisitados+")"};
        float[] yData = {numeroClientesProgramados - numeroClientesVisitados, numeroClientesVisitados};
        ArrayList<PieEntry> yEntrys = new ArrayList<>();

        for(int i = 0; i < yData.length; i++){
            yEntrys.add(new PieEntry(yData[i] , xData[i]));
        }
        //create the data set
        PieDataSet pieDataSet = new PieDataSet(yEntrys,"");
        pieDataSet.setSliceSpace(2);
        pieDataSet.setValueTextSize(12);

        //add colors to dataset
        ArrayList<Integer> colors = new ArrayList<>();
        colors.add(Color.rgb(216,27,96));
        colors.add(Color.rgb(255,152,0));

        pieDataSet.setColors(colors);

        //create pie data object
        PieData data = new PieData(pieDataSet);
        data.setValueFormatter(new PercentFormatter());
        data.setValueTextSize(18f);
        data.setValueTextColor(Color.WHITE);

        if (numeroClientesProgramados == 0)
            piechart_visitados.setNoDataText("No hay data disponible");
        else
            piechart_visitados.setData(data);

        piechart_visitados.invalidate();
    }

    private void cargarEfectivos() {
        int numeroClientesProgramados = daoCliente.getNumeroClientesProgramados(ventas360App.getModoVenta(), estadoVendedor);
        int numeroClientesEfectivos = daoCliente.getNumeroClientesEfectivos(ventas360App.getModoVenta(), estadoVendedor);

        String[] xData = {"Programados("+numeroClientesProgramados+")", "Efectivos("+numeroClientesEfectivos+")"};
        float[] yData = {numeroClientesProgramados - numeroClientesEfectivos, numeroClientesEfectivos};
        ArrayList<PieEntry> yEntrys = new ArrayList<>();

        for(int i = 0; i < yData.length; i++){
            yEntrys.add(new PieEntry(yData[i] , xData[i]));
        }
        //create the data set
        PieDataSet pieDataSet = new PieDataSet(yEntrys,"");
        pieDataSet.setSliceSpace(2);
        pieDataSet.setValueTextSize(12);

        //add colors to dataset
        ArrayList<Integer> colors = new ArrayList<>();
        colors.add(Color.rgb(241,196,15));
        colors.add(Color.rgb(22,160,133));

        pieDataSet.setColors(colors);

        //create pie data object
        PieData data = new PieData(pieDataSet);
        data.setValueFormatter(new PercentFormatter());
        data.setValueTextSize(18f);
        data.setValueTextColor(Color.WHITE);
        if (numeroClientesProgramados == 0)
            piechart_efectivos.setNoDataText("No hay data disponible");
        else
            piechart_efectivos.setData(data);
        piechart_efectivos.invalidate();
    }
}
