package com.expediodigital.ventas360.view;

import android.graphics.Color;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.expediodigital.ventas360.R;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;

import java.util.ArrayList;

public class TestActivity extends AppCompatActivity {
    final String TAG = getClass().getName();
    private PieChart piechart_visitados;
    private PieChart piechart_efectivos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        piechart_visitados = (PieChart) findViewById(R.id.piechart_visitados);
        piechart_efectivos = (PieChart) findViewById(R.id.piechart_efectivos);
        //piechart_visitados.setUsePercentValues(true);
        //piechart_efectivos.setUsePercentValues(true);
        Description description = new Description();
        description.setText("Clientes efectivos");
        piechart_efectivos.setDescription(description);
        piechart_efectivos.setRotationEnabled(true);
        //pieChart.setUsePercentValues(true);
        //pieChart.setHoleColor(Color.BLUE);
        //pieChart.setCenterTextColor(Color.BLACK);
        piechart_efectivos.setHoleRadius(25f);
        piechart_efectivos.setTransparentCircleAlpha(0);
        piechart_efectivos.setCenterText("Super Cool Chart");
        piechart_efectivos.setCenterTextSize(10);
        //pieChart.setDrawEntryLabels(true);
        //pieChart.setEntryLabelTextSize(20);
        //More options just check out the documentation!
        cargarEfectivos();
    }

    private void cargarEfectivos() {
        Log.d(TAG,"cargarEfectivos");
        Log.d(TAG, "addDataSet started");
        float[] yData = {25.3f, 10.6f, 66.76f, 44.32f, 46.01f, 16.89f, 23.9f};
        String[] xData = {"Mitch", "Jessica" , "Mohammad" , "Kelsey", "Sam", "Robert", "Ashley"};
        ArrayList<PieEntry> yEntrys = new ArrayList<>();
        ArrayList<String> xEntrys = new ArrayList<>();

        for(int i = 0; i < yData.length; i++){
            yEntrys.add(new PieEntry(yData[i] , i));
        }

        for(int i = 1; i < xData.length; i++){
            xEntrys.add(xData[i]);
        }

        //create the data set
        PieDataSet pieDataSet = new PieDataSet(yEntrys, "Employee Sales");
        pieDataSet.setSliceSpace(2);
        pieDataSet.setValueTextSize(12);

        //add colors to dataset
        ArrayList<Integer> colors = new ArrayList<>();
        colors.add(Color.GRAY);
        colors.add(Color.BLUE);
        colors.add(Color.RED);
        colors.add(Color.GREEN);
        colors.add(Color.CYAN);
        colors.add(Color.YELLOW);
        colors.add(Color.MAGENTA);

        pieDataSet.setColors(colors);

        //add legend to chart
        Legend legend = piechart_efectivos.getLegend();
        legend.setForm(Legend.LegendForm.CIRCLE);
        legend.setPosition(Legend.LegendPosition.LEFT_OF_CHART);

        //create pie data object
        PieData pieData = new PieData(pieDataSet);
        piechart_efectivos.setData(pieData);
        piechart_efectivos.invalidate();

    }

}
