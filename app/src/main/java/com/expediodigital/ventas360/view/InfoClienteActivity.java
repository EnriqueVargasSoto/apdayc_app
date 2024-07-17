package com.expediodigital.ventas360.view;

import android.content.Intent;
import android.graphics.Color;
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
import com.expediodigital.ventas360.model.HRClienteModel;
import com.expediodigital.ventas360.model.HRMarcaResumenModel;
import com.expediodigital.ventas360.model.HojaRutaMarcasModel;
import com.expediodigital.ventas360.util.Util;
import com.expediodigital.ventas360.view.fragment.ClientesListaFragment;
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

public class InfoClienteActivity extends AppCompatActivity {
    final String TAG = getClass().getName();
    private TextView tv_cliente, tv_programadoA,tv_programadoM,tv_programadoAA,tv_transcurridoA,tv_transcurridoM,tv_transcurridoAA,tv_liquidadoA,tv_liquidadoM,tv_liquidadoAA,
            tv_hitRateA,tv_hitRateM,tv_hitRateAA,tv_marcasA,tv_marcasM,tv_marcasAA,tv_coberturaMultipleA,tv_coberturaMultipleM,tv_coberturaMultipleAA,tv_cuotaSolesA,tv_cuotaSolesM,tv_cuotaSolesAA,
            tv_cuotaPaquetesA,tv_cuotaPaquetesM,tv_cuotaPaquetesAA,tv_ventaSolesA,tv_ventaSolesM,tv_ventaSolesAA,tv_ventaPaquetesA,tv_ventaPaquetesM,tv_ventaPaquetesAA,tv_avance,
            tv_necesidadSoles,tv_necesidadPaquetes,tv_segmento,tv_nroExhibidores,tv_nroPuertasFrio;
    private PieChart piechart;
    private Button btn_siguiente;
    private DAOPedido daoPedido;
    private DAOCliente daoCliente;
    private DAOConfiguracion daoConfiguracion;
    private DecimalFormat decimalFormat;

    int origen;
    int accion;
    String idCliente;
    String nombreCliente;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info_cliente);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Util.actualizarToolBar(getString(R.string.informacion_cliente), true, this);
        daoPedido = new DAOPedido(getApplicationContext());
        daoCliente = new DAOCliente(getApplicationContext());
        daoConfiguracion = new DAOConfiguracion(getApplicationContext());
        decimalFormat = Util.formateador();

        Bundle bundle = getIntent().getExtras();
        if (bundle != null){
            origen = bundle.getInt("origen");
            accion = bundle.getInt("accion");
            idCliente = bundle.getString("idCliente");
            nombreCliente = bundle.getString("nombreCliente");
        }

        //region Controles
        tv_cliente = findViewById(R.id.tv_cliente);
        tv_programadoA = findViewById(R.id.tv_programadoA);
        tv_programadoM = findViewById(R.id.tv_programadoM);
        tv_programadoAA = findViewById(R.id.tv_programadoAA);
        tv_transcurridoA = findViewById(R.id.tv_transcurridoA);
        tv_transcurridoM = findViewById(R.id.tv_transcurridoM);
        tv_transcurridoAA = findViewById(R.id.tv_transcurridoAA);
        tv_liquidadoA = findViewById(R.id.tv_liquidadoA);
        tv_liquidadoM = findViewById(R.id.tv_liquidadoM);
        tv_liquidadoAA = findViewById(R.id.tv_liquidadoAA);
        tv_hitRateA = findViewById(R.id.tv_hitRateA);
        tv_hitRateM = findViewById(R.id.tv_hitRateM);
        tv_hitRateAA = findViewById(R.id.tv_hitRateAA);
//        tv_marcasA = findViewById(R.id.tv_marcasA);
//        tv_marcasM = findViewById(R.id.tv_marcasM);
//        tv_marcasAA = findViewById(R.id.tv_marcasAA);
//        tv_coberturaMultipleA = findViewById(R.id.tv_coberturaMultipleA);
//        tv_coberturaMultipleM = findViewById(R.id.tv_coberturaMultipleM);
//        tv_coberturaMultipleAA = findViewById(R.id.tv_coberturaMultipleAA);
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
//        tv_segmento = findViewById(R.id.tv_segmento);
//        tv_nroExhibidores = findViewById(R.id.tv_nroExhibidores);
//        tv_nroPuertasFrio = findViewById(R.id.tv_nroPuertasFrio);

        piechart = findViewById(R.id.piechart);
        btn_siguiente = findViewById(R.id.btn_siguiente);
        //endregion

        btn_siguiente.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btn_siguiente.setEnabled(false);
                ClientesListaFragment.tomarPedido();
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
        tv_cliente.setText(idCliente + " - " + nombreCliente);
        ArrayList<HRClienteModel> lista = daoPedido.getHojaRutaCliente(idCliente);
        ArrayList<HRMarcaResumenModel> listaMarca = daoPedido.getHRMarcaResumen(idCliente);

        String fechaString = daoConfiguracion.getFechaString();
        Calendar calendarOriginal = Util.convertirStringFecha_aCalendar(fechaString);
        Calendar calendarAnioAnterior = Util.convertirStringFecha_aCalendar(fechaString);
        Calendar calendarMesAnterior = Util.convertirStringFecha_aCalendar(fechaString);
        calendarAnioAnterior.add(Calendar.YEAR,-1);
        calendarMesAnterior.add(Calendar.MONTH,-1);

        Log.i(TAG,"Mes Actual:"+ calendarOriginal.get(Calendar.YEAR) + "-"+(calendarOriginal.get(Calendar.MONTH)+1));
        Log.i(TAG,"Mes Anterior:"+ calendarMesAnterior.get(Calendar.YEAR) + "-"+(calendarMesAnterior.get(Calendar.MONTH)+1));
        Log.i(TAG,"Año Anterior:"+ calendarAnioAnterior.get(Calendar.YEAR) + "-"+(calendarAnioAnterior.get(Calendar.MONTH)+1));

        for (HRClienteModel model : lista){
            if (model.getEjercicio() == calendarOriginal.get(Calendar.YEAR) && model.getPeriodo() == (calendarOriginal.get(Calendar.MONTH)+1)){
                //Actual
                Log.d(TAG,"ACTUAL "+ model.getPeriodo());
                tv_programadoA.setText(String.valueOf(model.getProgramado()));
                tv_transcurridoA.setText(String.valueOf(model.getTranscurrido()));
                tv_liquidadoA.setText(String.valueOf(model.getLiquidado()));
                tv_hitRateA.setText(Util.redondearInt(model.getHitRate(),0) + "%");
                for (int i=0; i<listaMarca.size(); i++){
                    if (listaMarca.get(i).getEjercicio() == calendarOriginal.get(Calendar.YEAR) && listaMarca.get(i).getPeriodo() == (calendarOriginal.get(Calendar.MONTH)+1)){
                        tv_marcasA.setText(String.valueOf(listaMarca.get(i).getCantidad()));//del día
                    }
                }
                tv_coberturaMultipleA.setText(Util.redondearInt(model.getCoberturaMultiple(),0) + "%");

                tv_cuotaSolesA.setText(decimalFormat.format(model.getCuotaSoles()));
                tv_cuotaPaquetesA.setText(String.valueOf(model.getCuotaPaquetes()));
                tv_ventaSolesA.setText(decimalFormat.format(model.getVentaSoles()));
                tv_ventaPaquetesA.setText(String.valueOf(model.getVentaPaquetes()));
                tv_avance.setText(model.getAvance() + "%");

                tv_necesidadSoles.setText(decimalFormat.format(model.getNecesidadDiaSoles()));
                tv_necesidadPaquetes.setText(String.valueOf(model.getNecesidadDiaPaquetes()));
                tv_segmento.setText(model.getSegmento());
                tv_nroExhibidores.setText(String.valueOf(model.getNroExhibidores()));
                tv_nroPuertasFrio.setText(String.valueOf(model.getNroPuertasFrio()));
            }else if(model.getEjercicio() == calendarMesAnterior.get(Calendar.YEAR) && model.getPeriodo() == (calendarMesAnterior.get(Calendar.MONTH)+1)){
                //Mes anterior M-1
                Log.d(TAG,"M-1 "+ model.getPeriodo());
                tv_programadoM.setText(String.valueOf(model.getProgramado()));
                tv_transcurridoM.setText(String.valueOf(model.getTranscurrido()));
                tv_liquidadoM.setText(String.valueOf(model.getLiquidado()));
                tv_hitRateM.setText(Util.redondearInt(model.getHitRate(),0) + "%");
                for (int i=0; i<listaMarca.size(); i++){
                    if(listaMarca.get(i).getEjercicio() == calendarMesAnterior.get(Calendar.YEAR) && listaMarca.get(i).getPeriodo() == (calendarMesAnterior.get(Calendar.MONTH)+1)){
                        tv_marcasM.setText(String.valueOf(listaMarca.get(i).getCantidad()));//del mes anterior
                    }
                }
                tv_coberturaMultipleM.setText(Util.redondearInt(model.getCoberturaMultiple(),0) + "%");

                tv_cuotaSolesM.setText(decimalFormat.format(model.getCuotaSoles()));
                tv_cuotaPaquetesM.setText(String.valueOf(model.getCuotaPaquetes()));
                tv_ventaSolesM.setText(decimalFormat.format(model.getVentaSoles()));
                tv_ventaPaquetesM.setText(String.valueOf(model.getVentaPaquetes()));
            }else if(model.getEjercicio() == calendarAnioAnterior.get(Calendar.YEAR) && model.getPeriodo() == (calendarAnioAnterior.get(Calendar.MONTH)+1)){
                //Año anterior AA
                tv_programadoAA.setText(String.valueOf(model.getProgramado()));
                tv_transcurridoAA.setText(String.valueOf(model.getTranscurrido()));
                tv_liquidadoAA.setText(String.valueOf(model.getLiquidado()));
                tv_hitRateAA.setText(Util.redondearInt(model.getHitRate(),0) + "%");
                for (int i=0; i<listaMarca.size(); i++){
                    if(listaMarca.get(i).getEjercicio() == calendarAnioAnterior.get(Calendar.YEAR) && listaMarca.get(i).getPeriodo() == (calendarAnioAnterior.get(Calendar.MONTH)+1)){
                        tv_marcasAA.setText(String.valueOf(listaMarca.get(i).getCantidad()));//del año anterior
                    }
                }
                tv_coberturaMultipleAA.setText(Util.redondearInt(model.getCoberturaMultiple(),0) + "%");

                tv_cuotaSolesAA.setText(decimalFormat.format(model.getCuotaSoles()));
                tv_cuotaPaquetesAA.setText(String.valueOf(model.getCuotaPaquetes()));
                tv_ventaSolesAA.setText(decimalFormat.format(model.getVentaSoles()));
                tv_ventaPaquetesAA.setText(String.valueOf(model.getVentaPaquetes()));
            }
        }
        setUpCharts();
        cargarMarcas();
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

    private void cargarMarcas() {
        try{
            ArrayList<HojaRutaMarcasModel> listaMarcas = daoCliente.getHojaRutaMarcas(idCliente);
            String split[] = daoConfiguracion.getFechaString().split("/");
            int anio = Integer.parseInt(split[2]);
            int mes = Integer.parseInt(split[1]);
            /*for (HojaRutaMarcasModel model: listaMarcas){
                if (model.getEjercicio() == anio){
                    if (model.getPeriodo() == mes){
                        //Actual
                        Log.d(TAG,"ACTUAL "+ model.getPeriodo());
                        tv_marcas.setText(String.valueOf(model.getNroPuertasFrio()));

                    }else if(model.getPeriodo() == (mes -1)){
                        //Mes anterior M-1
                        tv_marcas.setText(String.valueOf(model.getNroPuertasFrio()));
                    }
                }else if(model.getEjercicio() == (anio -1)){
                    if (model.getPeriodo() == mes){
                        //Año anterior AA
                        tv_marcas.setText(String.valueOf(model.getNroPuertasFrio()));
                    }
                }
            }*/

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
                piechart.setNoDataText("No hay data disponible");
            else
                piechart.setData(data);

            piechart.invalidate();

        }catch (Exception e){
            e.printStackTrace();
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home){
            Intent intent = new Intent(InfoClienteActivity.this, InfoVendedorActivity.class);
            intent.putExtra("origen",origen);
            intent.putExtra("accion",accion);
            intent.putExtra("idCliente",idCliente);
            intent.putExtra("nombreCliente",nombreCliente);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_left_to_right_enter, R.anim.slide_left_to_right_exit);//animación con la que entra la nueva actividad y animación con la que sale esta actividad
            finish();
        }
        return true;
    }
}
