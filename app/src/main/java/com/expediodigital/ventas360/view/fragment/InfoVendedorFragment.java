package com.expediodigital.ventas360.view.fragment;


import android.graphics.Color;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.expediodigital.ventas360.DAO.DAOCliente;
import com.expediodigital.ventas360.DAO.DAOConfiguracion;
import com.expediodigital.ventas360.DAO.DAOPedido;
import com.expediodigital.ventas360.R;
import com.expediodigital.ventas360.Ventas360App;
import com.expediodigital.ventas360.model.HRVendedorModel;
import com.expediodigital.ventas360.util.Util;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

/**
 * A simple {@link Fragment} subclass.
 */
public class InfoVendedorFragment extends Fragment {
    final String TAG = getClass().getName();
    private TextView tv_vendedor,tv_cuotaSolesA,tv_cuotaSolesM,tv_cuotaSolesAA,/*tv_cuotaPaquetesA,tv_cuotaPaquetesM,tv_cuotaPaquetesAA,*/tv_ventaSolesA,tv_ventaSolesM,tv_ventaSolesAA,
            /*tv_ventaPaquetesA,tv_ventaPaquetesM,tv_ventaPaquetesAA,*/tv_avance,tv_necesidadSoles,/*tv_necesidadPaquetes,*/tv_ventaDiaSoles,/*tv_ventaDiaPaquetes,*/
            tv_coberturaSimpleA,tv_coberturaSimpleM,tv_coberturaSimpleAA,tv_coberturaMultipleA,tv_coberturaMultipleM,tv_coberturaMultipleAA,tv_hitRateA,tv_hitRateM,tv_hitRateAA,
            tv_reternerP,tv_reternerV,tv_reternerPor,tv_capturarP,tv_capturarV,tv_capturarPor,tv_desarrollarP,tv_desarrollarV,tv_desarrollarPor,tv_transaccionalP,tv_transaccionalV,tv_transaccionalPor,
            tv_otrosP,tv_otrosV,tv_otrosPor,tv_totalClientesP,tv_totalClientesV,tv_totalClientesPor/*,tv_nroExhibidores,tv_nroPuertasFrio*/;
    private PieChart piechart;    
    private DAOPedido daoPedido;
    private DAOCliente daoCliente;
    private DAOConfiguracion daoConfiguracion;
    private DecimalFormat decimalFormat;
    Ventas360App ventas360App;    
    private String estadoVendedor;

    public InfoVendedorFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_info_vendedor, container, false);

        ventas360App = (Ventas360App) getActivity().getApplicationContext();
        daoPedido = new DAOPedido(getActivity().getApplicationContext());
        daoCliente = new DAOCliente(getActivity().getApplicationContext());
        daoConfiguracion = new DAOConfiguracion(getActivity().getApplicationContext());
        decimalFormat = Util.formateador();
        //region Controles
        tv_vendedor = view.findViewById(R.id.tv_vendedor);
        tv_cuotaSolesA = view.findViewById(R.id.tv_cuotaSolesA);
        tv_cuotaSolesM = view.findViewById(R.id.tv_cuotaSolesM);
        tv_cuotaSolesAA = view.findViewById(R.id.tv_cuotaSolesAA);
//        tv_cuotaPaquetesA = view.findViewById(R.id.tv_cuotaPaquetesA);
//        tv_cuotaPaquetesM = view.findViewById(R.id.tv_cuotaPaquetesM);
//        tv_cuotaPaquetesAA = view.findViewById(R.id.tv_cuotaPaquetesAA);
        tv_ventaSolesA = view.findViewById(R.id.tv_ventaSolesA);
        tv_ventaSolesM = view.findViewById(R.id.tv_ventaSolesM);
        tv_ventaSolesAA = view.findViewById(R.id.tv_ventaSolesAA);
//        tv_ventaPaquetesA = view.findViewById(R.id.tv_ventaPaquetesA);
//        tv_ventaPaquetesM = view.findViewById(R.id.tv_ventaPaquetesM);
//        tv_ventaPaquetesAA = view.findViewById(R.id.tv_ventaPaquetesAA);
        tv_avance = view.findViewById(R.id.tv_avance);
        tv_necesidadSoles = view.findViewById(R.id.tv_necesidadSoles);
//        tv_necesidadPaquetes = view.findViewById(R.id.tv_necesidadPaquetes);
        tv_ventaDiaSoles = view.findViewById(R.id.tv_ventaDiaSoles);
//        tv_ventaDiaPaquetes = view.findViewById(R.id.tv_ventaDiaPaquetes);
        tv_coberturaSimpleA = view.findViewById(R.id.tv_coberturaSimpleA);
        tv_coberturaSimpleM = view.findViewById(R.id.tv_coberturaSimpleM);
        tv_coberturaSimpleAA = view.findViewById(R.id.tv_coberturaSimpleAA);
//        tv_coberturaMultipleA = view.findViewById(R.id.tv_coberturaMultipleA);
//        tv_coberturaMultipleM = view.findViewById(R.id.tv_coberturaMultipleM);
//        tv_coberturaMultipleAA = view.findViewById(R.id.tv_coberturaMultipleAA);
        tv_hitRateA = view.findViewById(R.id.tv_hitRateA);
        tv_hitRateM = view.findViewById(R.id.tv_hitRateM);
        tv_hitRateAA = view.findViewById(R.id.tv_hitRateAA);
        tv_reternerP = view.findViewById(R.id.tv_reternerP);
        tv_reternerV = view.findViewById(R.id.tv_reternerV);
        tv_reternerPor = view.findViewById(R.id.tv_reternerPor);
        tv_capturarP = view.findViewById(R.id.tv_capturarP);
        tv_capturarV = view.findViewById(R.id.tv_capturarV);
        tv_capturarPor = view.findViewById(R.id.tv_capturarPor);
        tv_desarrollarP = view.findViewById(R.id.tv_desarrollarP);
        tv_desarrollarV = view.findViewById(R.id.tv_desarrollarV);
        tv_desarrollarPor = view.findViewById(R.id.tv_desarrollarPor);
        tv_transaccionalP = view.findViewById(R.id.tv_transaccionalP);
        tv_transaccionalV = view.findViewById(R.id.tv_transaccionalV);
        tv_transaccionalPor = view.findViewById(R.id.tv_transaccionalPor);
        tv_otrosP = view.findViewById(R.id.tv_otrosP);
        tv_otrosV = view.findViewById(R.id.tv_otrosV);
        tv_otrosPor = view.findViewById(R.id.tv_otrosPor);
        tv_totalClientesP = view.findViewById(R.id.tv_totalClientesP);
        tv_totalClientesV = view.findViewById(R.id.tv_totalClientesV);
        tv_totalClientesPor = view.findViewById(R.id.tv_totalClientesPor);
//        tv_nroExhibidores = view.findViewById(R.id.tv_nroExhibidores);
//        tv_nroPuertasFrio = view.findViewById(R.id.tv_nroPuertasFrio);

        piechart = view.findViewById(R.id.piechart);
        //endregion
        
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        cargarInfo();
    }

    private void cargarInfo() {
        tv_vendedor.setText(ventas360App.getIdVendedor() + " - " + ventas360App.getNombreVendedor());

        ArrayList<HRVendedorModel> lista = daoPedido.getHojaRutaVendedor();

        String fechaString = daoConfiguracion.getFechaString();
        Calendar calendarOriginal = Util.convertirStringFecha_aCalendar(fechaString);
        Calendar calendarAnioAnterior = Util.convertirStringFecha_aCalendar(fechaString);
        Calendar calendarMesAnterior = Util.convertirStringFecha_aCalendar(fechaString);
        calendarAnioAnterior.add(Calendar.YEAR,-1);
        calendarMesAnterior.add(Calendar.MONTH,-1);

        Log.i(TAG,"Mes Actual:"+ calendarOriginal.get(Calendar.YEAR) + "-"+(calendarOriginal.get(Calendar.MONTH)+1));
        Log.i(TAG,"Mes Anterior:"+ calendarMesAnterior.get(Calendar.YEAR) + "-"+(calendarMesAnterior.get(Calendar.MONTH)+1));
        Log.i(TAG,"Año Anterior:"+ calendarAnioAnterior.get(Calendar.YEAR) + "-"+(calendarAnioAnterior.get(Calendar.MONTH)+1));

        for (HRVendedorModel model : lista){
            if (model.getEjercicio() == calendarOriginal.get(Calendar.YEAR) && model.getPeriodo() == (calendarOriginal.get(Calendar.MONTH)+1)){
                //Actual
                Log.d(TAG,"ACTUAL "+ model.getPeriodo());
                tv_cuotaSolesA.setText(decimalFormat.format(model.getCuotaSoles()));
//                tv_cuotaPaquetesA.setText(String.valueOf(model.getCuotaPaquetes()));
                tv_ventaSolesA.setText(decimalFormat.format(model.getVentaSoles()));
//                tv_ventaPaquetesA.setText(String.valueOf(model.getVentaPaquetes()));
                tv_avance.setText(model.getAvance() + "%");

                tv_necesidadSoles.setText(decimalFormat.format(model.getNecesidadDiaSoles()));
//                tv_necesidadPaquetes.setText(String.valueOf(model.getNecesidadDiaPaquetes()));

                tv_coberturaSimpleA.setText("");
                tv_coberturaMultipleA.setText(Util.redondearInt(model.getCoberturaMultiple(),0) + "%");
                tv_hitRateA.setText(Util.redondearInt(model.getHitRate(),0) + "%");
            }else if(model.getEjercicio() == calendarMesAnterior.get(Calendar.YEAR) && model.getPeriodo() == (calendarMesAnterior.get(Calendar.MONTH)+1)){
                //Mes anterior M-1
                Log.d(TAG,"M-1 "+ model.getPeriodo());
                tv_cuotaSolesM.setText(decimalFormat.format(model.getCuotaSoles()));
//                tv_cuotaPaquetesM.setText(String.valueOf(model.getCuotaPaquetes()));
                tv_ventaSolesM.setText(decimalFormat.format(model.getVentaSoles()));
//                tv_ventaPaquetesM.setText(String.valueOf(model.getVentaPaquetes()));

                tv_coberturaSimpleM.setText("");
                tv_coberturaMultipleM.setText(Util.redondearInt(model.getCoberturaMultiple(),0) + "%");
                tv_hitRateM.setText(Util.redondearInt(model.getHitRate(),0) + "%");
            }else if(model.getEjercicio() == calendarAnioAnterior.get(Calendar.YEAR) && model.getPeriodo() == (calendarAnioAnterior.get(Calendar.MONTH)+1)){
                //Año anterior AA
                Log.d(TAG,"AA "+ model.getEjercicio()+" "+ model.getPeriodo());
                tv_cuotaSolesAA.setText(decimalFormat.format(model.getCuotaSoles()));
//                tv_cuotaPaquetesAA.setText(String.valueOf(model.getCuotaPaquetes()));
                tv_ventaSolesAA.setText(decimalFormat.format(model.getVentaSoles()));
//                tv_ventaPaquetesAA.setText(String.valueOf(model.getVentaPaquetes()));

                tv_coberturaSimpleAA.setText("");
                tv_coberturaMultipleAA.setText(Util.redondearInt(model.getCoberturaMultiple(),0) + "%");
                tv_hitRateAA.setText(Util.redondearInt(model.getHitRate(),0) + "%");
            }
        }

        HashMap<String,Object> resumenDia = daoPedido.getResumenVenta();
        if (resumenDia != null){
            tv_ventaDiaSoles.setText(decimalFormat.format((double)resumenDia.get("importeTotal")));
//            tv_ventaDiaPaquetes.setText(resumenDia.get("cantidad").toString());
        }

        estadoVendedor = daoConfiguracion.getEstadoVendedor(ventas360App.getIdEmpresa(),ventas360App.getIdSucursal(),ventas360App.getIdVendedor());
        ArrayList<HashMap<String,Object>> listaSegmento = daoPedido.getClientesxSegmento(ventas360App.getModoVenta(),estadoVendedor, calendarOriginal.get(Calendar.YEAR), (calendarOriginal.get(Calendar.MONTH)+1));
        int totalClientes = 0, totalVendidos = 0;
        int totalPorcentaje = 0;
        int totalExhibidores = 0, totalPuertasFrio = 0;


        for (HashMap<String,Object> segmento : listaSegmento){

            switch (segmento.get("idSegmento").toString()){
                case "001":
                    //Log.w(TAG,"001 "+);
                    tv_capturarP.setText(segmento.get("programados").toString());
                    tv_capturarV.setText(segmento.get("efectivos").toString());
                    tv_capturarPor.setText(segmento.get("porcentaje").toString() + "%");
                    break;
                case "002":
                    tv_desarrollarP.setText(segmento.get("programados").toString());
                    tv_desarrollarV.setText(segmento.get("efectivos").toString());
                    tv_desarrollarPor.setText(segmento.get("porcentaje").toString() + "%");
                    break;
                case "003":
                    tv_reternerP.setText(segmento.get("programados").toString());
                    tv_reternerV.setText(segmento.get("efectivos").toString());
                    tv_reternerPor.setText(segmento.get("porcentaje").toString() + "%");
                    break;
                case "004":
                    tv_transaccionalP.setText(segmento.get("programados").toString());
                    tv_transaccionalV.setText(segmento.get("efectivos").toString());
                    tv_transaccionalPor.setText(segmento.get("porcentaje").toString() + "%");
                    break;
                default:
                    tv_otrosP.setText(segmento.get("programados").toString());
                    tv_otrosV.setText(segmento.get("efectivos").toString());
                    tv_otrosPor.setText(segmento.get("porcentaje").toString() + "%");
                    break;
            }

            totalClientes += (int)segmento.get("programados");
            //Log.d(TAG,"efectivos:"+(int)segmento.get("efectivos"));
            totalVendidos += (int)segmento.get("efectivos");
            totalExhibidores += (int)segmento.get("nroExhibidores");
            totalPuertasFrio += (int)segmento.get("nroPuertasFrio");
        }
        totalPorcentaje = Util.redondearInt(totalVendidos * 100.00 / totalClientes,0);

        tv_totalClientesP.setText(String.valueOf(totalClientes));
        tv_totalClientesV.setText(String.valueOf(totalVendidos));
        tv_totalClientesPor.setText(totalPorcentaje + "%");
//        tv_nroExhibidores.setText(String.valueOf(totalExhibidores));
//        tv_nroPuertasFrio.setText(String.valueOf(totalPuertasFrio));

        setUpCharts();
        cargarMarcasDia();
    }

    private void setUpCharts(){
        piechart.setDescription(null);
        piechart.setRotationEnabled(true);
        piechart.setUsePercentValues(true);
        piechart.setHoleRadius(16f);
        piechart.setTransparentCircleRadius(24f);
        piechart.setTransparentCircleAlpha(150);

        Legend legend = piechart.getLegend();
        legend.setForm(Legend.LegendForm.CIRCLE);
        legend.setOrientation(Legend.LegendOrientation.VERTICAL);
        legend.setWordWrapEnabled(true);
        legend.setPosition(Legend.LegendPosition.RIGHT_OF_CHART_CENTER);

    }

    private void cargarMarcasDia() {
        try{
            ArrayList<HashMap<String,Object>> listaMarcas = daoPedido.getVentaMarcasDia();

            String[] xData = new String[listaMarcas.size()];
            float[] yData = new float[listaMarcas.size()];

            for (int i = 0; i<listaMarcas.size(); i++){
                xData[i] = listaMarcas.get(i).get("descripcion").toString()+"("+listaMarcas.get(i).get("cantidad")+")";
                yData[i] = (int)listaMarcas.get(i).get("cantidad");
            }

            ArrayList<PieEntry> yEntrys = new ArrayList<>();

            for(int i = 0; i < yData.length; i++){
                yEntrys.add(new PieEntry(yData[i] , xData[i]));
            }
            //create the data set
            PieDataSet pieDataSet = new PieDataSet(yEntrys,"");
            pieDataSet.setSliceSpace(2);
            pieDataSet.setValueTextSize(12);

            //Es importante tener la cantidad de colores suficiente, de lo contrario no se mostrarán todos en la leyenda
            ArrayList<Integer> colors = new ArrayList<>();

            double cont = yEntrys.size()*1.0/ColorTemplate.MATERIAL_COLORS.length;
            if((cont%1) !=0)
                cont = cont + 1;

            for (int i=0; i<(int)cont; i++){
                for (int c : ColorTemplate.MATERIAL_COLORS)
                    colors.add(c);
            }
            pieDataSet.setColors(colors);
            //create pie data object
            PieData data = new PieData(pieDataSet);
            data.setValueFormatter(new PercentFormatter());
            data.setValueTextSize(18f);
            data.setValueTextColor(Color.WHITE);

            if (listaMarcas.isEmpty())
                piechart.setNoDataText("No hay data disponible");
            else
                piechart.setData(data);

            piechart.invalidate();

        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
