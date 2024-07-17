package com.expediodigital.ventas360.view;

import android.content.Intent;
import android.graphics.Color;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.view.ViewCompat;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.expediodigital.ventas360.DAO.DAOCliente;
import com.expediodigital.ventas360.DAO.DAOConfiguracion;
import com.expediodigital.ventas360.DAO.DAOPedido;
import com.expediodigital.ventas360.R;
import com.expediodigital.ventas360.Ventas360App;
import com.expediodigital.ventas360.model.HRVendedorModel;
import com.expediodigital.ventas360.util.Util;
import com.expediodigital.ventas360.view.fragment.ClientesListaFragment;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;

public class InfoVendedorActivity extends AppCompatActivity {
    final String TAG = getClass().getName();
    private TextView tv_vendedor,tv_cuotaSolesA,tv_cuotaSolesM,tv_cuotaSolesAA,/*tv_cuotaPaquetesA,tv_cuotaPaquetesM,tv_cuotaPaquetesAA,*/tv_ventaSolesA,tv_ventaSolesM,tv_ventaSolesAA,
            /*tv_ventaPaquetesA,tv_ventaPaquetesM,tv_ventaPaquetesAA,*/tv_avance,tv_necesidadSoles,/*tv_necesidadPaquetes,*/tv_ventaDiaSoles,/*tv_ventaDiaPaquetes,*/
            tv_coberturaSimpleA,tv_coberturaSimpleM,tv_coberturaSimpleAA,tv_coberturaMultipleA,tv_coberturaMultipleM,tv_coberturaMultipleAA,tv_hitRateA,tv_hitRateM,tv_hitRateAA,
            tv_reternerP,tv_reternerV,tv_reternerPor,tv_capturarP,tv_capturarV,tv_capturarPor,tv_desarrollarP,tv_desarrollarV,tv_desarrollarPor,tv_transaccionalP,tv_transaccionalV,tv_transaccionalPor,
            tv_otrosP,tv_otrosV,tv_otrosPor,tv_totalClientesP,tv_totalClientesV,tv_totalClientesPor,tv_nroExhibidores,tv_nroPuertasFrio;
    private PieChart piechart;
    private Button btn_siguiente;
    private DAOPedido daoPedido;
    private DAOCliente daoCliente;
    private DAOConfiguracion daoConfiguracion;
    private DecimalFormat decimalFormat;
    Ventas360App ventas360App;
    private ClientesListaFragment fragment;
    private String estadoVendedor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info_vendedor);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Util.actualizarToolBar(getString(R.string.informacion_vendedor), true, this);

        ventas360App = (Ventas360App) getApplicationContext();
        daoPedido = new DAOPedido(getApplicationContext());
        daoCliente = new DAOCliente(getApplicationContext());
        daoConfiguracion = new DAOConfiguracion(getApplicationContext());
        decimalFormat = Util.formateador();
        //region Controles
        tv_vendedor = findViewById(R.id.tv_vendedor);
        tv_cuotaSolesA = findViewById(R.id.tv_cuotaSolesA);
        tv_cuotaSolesM = findViewById(R.id.tv_cuotaSolesM);
        tv_cuotaSolesAA = findViewById(R.id.tv_cuotaSolesAA);
//        tv_cuotaPaquetesA = findViewById(R.id.tv_cuotaPaquetesA);
//        tv_cuotaPaquetesM = findViewById(R.id.tv_cuotaPaquetesM);
//        tv_cuotaPaquetesAA = findViewById(R.id.tv_cuotaPaquetesAA);
        tv_ventaSolesA = findViewById(R.id.tv_ventaSolesA);
        tv_ventaSolesM = findViewById(R.id.tv_ventaSolesM);
        tv_ventaSolesAA = findViewById(R.id.tv_ventaSolesAA);
//        tv_ventaPaquetesA = findViewById(R.id.tv_ventaPaquetesA);
//        tv_ventaPaquetesM = findViewById(R.id.tv_ventaPaquetesM);
//        tv_ventaPaquetesAA = findViewById(R.id.tv_ventaPaquetesAA);
        tv_avance = findViewById(R.id.tv_avance);
        tv_necesidadSoles = findViewById(R.id.tv_necesidadSoles);
//        tv_necesidadPaquetes = findViewById(R.id.tv_necesidadPaquetes);
        tv_ventaDiaSoles = findViewById(R.id.tv_ventaDiaSoles);
//        tv_ventaDiaPaquetes = findViewById(R.id.tv_ventaDiaPaquetes);
        tv_coberturaSimpleA = findViewById(R.id.tv_coberturaSimpleA);
        tv_coberturaSimpleM = findViewById(R.id.tv_coberturaSimpleM);
        tv_coberturaSimpleAA = findViewById(R.id.tv_coberturaSimpleAA);
        tv_coberturaMultipleA = findViewById(R.id.tv_coberturaMultipleA);
        tv_coberturaMultipleM = findViewById(R.id.tv_coberturaMultipleM);
        tv_coberturaMultipleAA = findViewById(R.id.tv_coberturaMultipleAA);
        tv_hitRateA = findViewById(R.id.tv_hitRateA);
        tv_hitRateM = findViewById(R.id.tv_hitRateM);
        tv_hitRateAA = findViewById(R.id.tv_hitRateAA);
        tv_reternerP = findViewById(R.id.tv_reternerP);
        tv_reternerV = findViewById(R.id.tv_reternerV);
        tv_reternerPor = findViewById(R.id.tv_reternerPor);
        tv_capturarP = findViewById(R.id.tv_capturarP);
        tv_capturarV = findViewById(R.id.tv_capturarV);
        tv_capturarPor = findViewById(R.id.tv_capturarPor);
        tv_desarrollarP = findViewById(R.id.tv_desarrollarP);
        tv_desarrollarV = findViewById(R.id.tv_desarrollarV);
        tv_desarrollarPor = findViewById(R.id.tv_desarrollarPor);
        tv_transaccionalP = findViewById(R.id.tv_transaccionalP);
        tv_transaccionalV = findViewById(R.id.tv_transaccionalV);
        tv_transaccionalPor = findViewById(R.id.tv_transaccionalPor);
        tv_otrosP = findViewById(R.id.tv_otrosP);
        tv_otrosV = findViewById(R.id.tv_otrosV);
        tv_otrosPor = findViewById(R.id.tv_otrosPor);
        tv_totalClientesP = findViewById(R.id.tv_totalClientesP);
        tv_totalClientesV = findViewById(R.id.tv_totalClientesV);
        tv_totalClientesPor = findViewById(R.id.tv_totalClientesPor);
        tv_nroExhibidores = findViewById(R.id.tv_nroExhibidores);
        tv_nroPuertasFrio = findViewById(R.id.tv_nroPuertasFrio);

        piechart = findViewById(R.id.piechart);
        btn_siguiente = findViewById(R.id.btn_siguiente);
        //endregion

        btn_siguiente.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btn_siguiente.setEnabled(false);
                Intent intent = new Intent(InfoVendedorActivity.this, InfoClienteActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

                Bundle bundle = getIntent().getExtras();
                if (bundle != null){
                    int origen = bundle.getInt("origen");
                    int accion = bundle.getInt("accion");
                    String idCliente = bundle.getString("idCliente");
                    String nombreCliente = bundle.getString("nombreCliente");

                    intent.putExtra("origen",origen);
                    intent.putExtra("accion",accion);
                    intent.putExtra("idCliente",idCliente);
                    intent.putExtra("nombreCliente",nombreCliente);
                }

                startActivity(intent);
                overridePendingTransition(R.anim.slide_right_to_left_enter, R.anim.slide_right_to_left_exit);//animación con la que entra la nueva actividad y animación con la que sale esta actividad
                finish();
            }
        });

        cargarInfo();
    }

    @Override
    protected void onResume() {
        super.onResume();
        btn_siguiente.setEnabled(true);
    }

    private void cargarInfo() {
        tv_vendedor.setText(ventas360App.getIdVendedor() + " - " + ventas360App.getNombreVendedor());

        ArrayList<HRVendedorModel> lista = daoPedido.getHojaRutaVendedor();
        String split[] = daoConfiguracion.getFechaString().split("/");
        int anio = Integer.parseInt(split[2]);
        int mes = Integer.parseInt(split[1]);

        for (HRVendedorModel model : lista){
            if (model.getEjercicio() == anio){
                if (model.getPeriodo() == mes){
                    //Actual
                    Log.d(TAG,"ACTUAL "+ model.getPeriodo());
                    tv_cuotaSolesA.setText(decimalFormat.format(model.getCuotaSoles()));
//                    tv_cuotaPaquetesA.setText(String.valueOf(model.getCuotaPaquetes()));
                    tv_ventaSolesA.setText(decimalFormat.format(model.getVentaSoles()));
//                    tv_ventaPaquetesA.setText(String.valueOf(model.getVentaPaquetes()));
                    tv_avance.setText(model.getAvance() + "%");

                    tv_necesidadSoles.setText("S/. "+decimalFormat.format(model.getNecesidadDiaSoles()));
//                    tv_necesidadPaquetes.setText(String.valueOf(model.getNecesidadDiaPaquetes()));

                    tv_coberturaSimpleA.setText("");
                    tv_coberturaMultipleA.setText(Util.redondearInt(model.getCoberturaMultiple(),0) + "%");
                    tv_hitRateA.setText(Util.redondearInt(model.getHitRate(),0) + "%");
                }else if(model.getPeriodo() == (mes -1)){
                    //Mes anterior M-1
                    Log.d(TAG,"M-1 "+ model.getPeriodo());
                    tv_cuotaSolesM.setText(decimalFormat.format(model.getCuotaSoles()));
//                    tv_cuotaPaquetesM.setText(String.valueOf(model.getCuotaPaquetes()));
                    tv_ventaSolesM.setText(decimalFormat.format(model.getVentaSoles()));
//                    tv_ventaPaquetesM.setText(String.valueOf(model.getVentaPaquetes()));

                    tv_coberturaSimpleM.setText("");
                    tv_coberturaMultipleM.setText(Util.redondearInt(model.getCoberturaMultiple(),0) + "%");
                    tv_hitRateM.setText(Util.redondearInt(model.getHitRate(),0) + "%");
                }
            }else if(model.getEjercicio() == (anio -1)){
                if (model.getPeriodo() == mes){
                    //Año anterior AA
                    Log.d(TAG,"AA "+ model.getEjercicio()+" "+ model.getPeriodo());
                    tv_cuotaSolesAA.setText(decimalFormat.format(model.getCuotaSoles()));
//                    tv_cuotaPaquetesAA.setText(String.valueOf(model.getCuotaPaquetes()));
                    tv_ventaSolesAA.setText(decimalFormat.format(model.getVentaSoles()));
//                    tv_ventaPaquetesAA.setText(String.valueOf(model.getVentaPaquetes()));

                    tv_coberturaSimpleAA.setText("");
                    tv_coberturaMultipleAA.setText(Util.redondearInt(model.getCoberturaMultiple(),0) + "%");
                    tv_hitRateAA.setText(Util.redondearInt(model.getHitRate(),0) + "%");
                }
            }
        }

        HashMap<String,Object> resumenDia = daoPedido.getResumenVenta();
        if (resumenDia != null){
            tv_ventaDiaSoles.setText("S/. "+decimalFormat.format((double)resumenDia.get("importeTotal")));
//            tv_ventaDiaPaquetes.setText(resumenDia.get("cantidad").toString());
        }

        estadoVendedor = daoConfiguracion.getEstadoVendedor(ventas360App.getIdEmpresa(),ventas360App.getIdSucursal(),ventas360App.getIdVendedor());
        ArrayList<HashMap<String,Object>> listaSegmento = daoPedido.getClientesxSegmento(ventas360App.getModoVenta(),estadoVendedor, anio, mes);
        int totalClientes = 0, totalVendidos = 0;
        int totalPorcentaje = 0;
        int totalExhibidores = 0, totalPuertasFrio = 0;


        for (HashMap<String,Object> segmento : listaSegmento){

            switch (segmento.get("idSegmento").toString()){
                case "001":
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
        tv_nroExhibidores.setText(String.valueOf(totalExhibidores));
        tv_nroPuertasFrio.setText(String.valueOf(totalPuertasFrio));

        /*setUpCharts();
        cargarEfectivos();*/
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
        legend.setPosition(Legend.LegendPosition.RIGHT_OF_CHART_INSIDE);
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
            piechart.setNoDataText("No hay data disponible");
        else
            piechart.setData(data);
        piechart.invalidate();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home){
            finish();
        }
        return true;
    }

    void setFragment(ClientesListaFragment fragment){
        this.fragment = fragment;
    }
}
