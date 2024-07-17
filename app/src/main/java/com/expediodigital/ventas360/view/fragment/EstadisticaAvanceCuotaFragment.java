package com.expediodigital.ventas360.view.fragment;

import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.annotation.DimenRes;
import androidx.annotation.DrawableRes;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AlertDialog;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.expediodigital.ventas360.DAO.DAOConfiguracion;
import com.expediodigital.ventas360.R;
import com.expediodigital.ventas360.Ventas360App;
import com.expediodigital.ventas360.util.SoapManager;
import com.expediodigital.ventas360.util.TablesHelper;
import com.expediodigital.ventas360.util.Util;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;

public class EstadisticaAvanceCuotaFragment extends Fragment {
    public final String TAG = getClass().getName();
    private DAOConfiguracion daoConfiguracion;
    private TableLayout table_avance;
    private DecimalFormat formateador;
    private TextView tv_totalAvance,tv_totalCuotaDia,tv_totalFaltantes,tv_promedioAvance;
    private int totalAvance,totalCuotaDia,totalFaltantes,avanceMayor;
    private double promedioAvance;

    LinearLayout.LayoutParams paramsWEIGHT = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT,.5f);
    ViewGroup.LayoutParams paramsSMALL;
    ViewGroup.LayoutParams paramsMEDIUM;
    ViewGroup.LayoutParams paramsLARGE;
    ViewGroup.LayoutParams paramsEXTRA_LARGE;

    Ventas360App ventas360App;
    public EstadisticaAvanceCuotaFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_estadistica_avance_cuota, container, false);

        ventas360App = (Ventas360App) getActivity().getApplicationContext();
        daoConfiguracion = new DAOConfiguracion(getActivity());

        table_avance      = view.findViewById(R.id.table_avance);
        tv_totalAvance    = view.findViewById(R.id.tv_totalAvance);
        tv_totalCuotaDia  = view.findViewById(R.id.tv_totalCuotaDia);
        tv_totalFaltantes = view.findViewById(R.id.tv_totalFaltantes);
        tv_promedioAvance = view.findViewById(R.id.tv_promedioAvance);

        int table_column_size_small = (int) getResources().getDimension(R.dimen.table_column_size_small);
        int table_column_size_medium = (int) getResources().getDimension(R.dimen.table_column_size_medium);
        int table_column_size_large = (int) getResources().getDimension(R.dimen.table_column_size_large);
        int table_column_size_extra_large = (int) getResources().getDimension(R.dimen.table_column_size_extra_large);
        paramsSMALL = new ViewGroup.LayoutParams(table_column_size_small, ViewGroup.LayoutParams.MATCH_PARENT);
        paramsMEDIUM = new ViewGroup.LayoutParams(table_column_size_medium, ViewGroup.LayoutParams.MATCH_PARENT);
        paramsLARGE = new ViewGroup.LayoutParams(table_column_size_large, ViewGroup.LayoutParams.MATCH_PARENT);
        paramsEXTRA_LARGE = new ViewGroup.LayoutParams(table_column_size_extra_large, ViewGroup.LayoutParams.MATCH_PARENT);
        formateador = Util.formateador();

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        new async_obtenerReporte().execute();
    }

    public void refreshLista(){
        ArrayList<HashMap<String, Object>> lista = daoConfiguracion.getReporteAvanceCuota();

        totalAvance=0;totalCuotaDia=0;totalFaltantes=0; promedioAvance=0;avanceMayor=0;
        int numeroVendedores = 0;

        for (HashMap<String,Object> item : lista){
            totalAvance     += (int)item.get("avanceVenta");
            totalCuotaDia   += (int)item.get("cuotaDia");
            totalFaltantes  += (int)item.get("cajasFaltantes");

            if ((int)item.get("avanceVenta") > avanceMayor)
                avanceMayor = (int)item.get("avanceVenta");

            if ((int)item.get("avanceVenta") > 0)
                numeroVendedores ++;
        }

        if (numeroVendedores == 0)
            promedioAvance = 0.0d;
        else
            promedioAvance = Util.redondearDouble(totalAvance / (numeroVendedores * 1.0d));


        tv_totalAvance.setText(String.valueOf(totalAvance));
        tv_totalCuotaDia.setText(String.valueOf(totalCuotaDia));
        tv_totalFaltantes.setText(String.valueOf(totalFaltantes));
        tv_promedioAvance.setText(String.valueOf(promedioAvance));

        int i = 0;
        for (HashMap<String,Object> item : lista){

            if ((int)item.get("avanceVenta") >= promedioAvance && (int)item.get("avanceVenta") > 0){
                if (i == lista.size()-1)
                    agregarRow(item, true, true);
                else
                    agregarRow(item, false, true);
            }else{
                if (i == lista.size()-1)
                    agregarRow(item, true, false);
                else
                    agregarRow(item, false, false);
            }

            i++;
        }
    }

    private int dpToInt(@DimenRes int dimenRes){
        //return (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, (int) getResources().getDimension(dimenRes)/3, getResources().getDisplayMetrics());
        return (int) (getResources().getDimension(dimenRes) / getResources().getDisplayMetrics().density);
    }

    private void agregarRow(HashMap<String,Object> item, boolean isLastRow, boolean focus){
        LinearLayout tableRow = new LinearLayout(getActivity());

        TextView textVendedor = new TextView(getActivity());
        textVendedor.setText(item.get("nombre").toString());
        textVendedor.setTextColor(ContextCompat.getColor(getActivity(),R.color.grey_800));
        textVendedor.setGravity(Gravity.CENTER);
        textVendedor.setPadding(dpToInt(R.dimen.padding_item_row_layout),dpToInt(R.dimen.padding_item_row_layout),dpToInt(R.dimen.padding_item_row_layout),dpToInt(R.dimen.padding_item_row_layout));
        textVendedor.setBackgroundResource(R.drawable.table_cell_bg);
        textVendedor.setLayoutParams(paramsEXTRA_LARGE);


        LinearLayout linearLayout = new LinearLayout(getActivity());
        linearLayout.setOrientation(LinearLayout.HORIZONTAL);
        linearLayout.setGravity(Gravity.CENTER);

        TextView textAvance = new TextView(getActivity());
        textAvance.setText(String.valueOf(item.get("avanceVenta")));
        textAvance.setPadding(dpToInt(R.dimen.padding_item_row_layout),dpToInt(R.dimen.padding_item_row_layout),dpToInt(R.dimen.padding_item_row_layout),dpToInt(R.dimen.padding_item_row_layout));
        textAvance.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        linearLayout.addView(textAvance,new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        if (focus){
            ImageView imageView = new ImageView(getActivity());
            imageView.setImageResource(R.drawable.icon_star_24dp);
            imageView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));

            linearLayout.addView(imageView,new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        }

        linearLayout.setLayoutParams(paramsSMALL);
        linearLayout.setBackgroundResource(R.drawable.table_cell_bg);

        TextView textCuota = new TextView(getActivity());
        textCuota.setText(String.valueOf(item.get("cuotaDia")));
        textCuota.setGravity(Gravity.CENTER);
        textCuota.setPadding(dpToInt(R.dimen.padding_item_row_layout),dpToInt(R.dimen.padding_item_row_layout),dpToInt(R.dimen.padding_item_row_layout),dpToInt(R.dimen.padding_item_row_layout));
        textCuota.setBackgroundResource(R.drawable.table_cell_bg);
        textCuota.setLayoutParams(paramsSMALL);

        TextView textFaltantes = new TextView(getActivity());
        textFaltantes.setText(String.valueOf(item.get("cajasFaltantes")));
        textFaltantes.setGravity(Gravity.CENTER);
        textFaltantes.setPadding(dpToInt(R.dimen.padding_item_row_layout),dpToInt(R.dimen.padding_item_row_layout),dpToInt(R.dimen.padding_item_row_layout),dpToInt(R.dimen.padding_item_row_layout));
        textFaltantes.setBackgroundResource(R.drawable.table_cell_bg);
        textFaltantes.setLayoutParams(paramsSMALL);

        /*Se debe calcular el status del vendedor, el cual indica qué porcentaje va respecto al mayor avance de la lista*/
        double porcentaje = 0;
        if (avanceMayor > 0)
            porcentaje = ((int)item.get("avanceVenta") * 100.00d) / avanceMayor;

        int status =  (Util.redondearDouble(porcentaje,0)).intValue();
        TextView textStatus = new TextView(getActivity());
        textStatus.setText(status + " %");
        textStatus.setGravity(Gravity.CENTER);
        textStatus.setPadding(dpToInt(R.dimen.padding_item_row_layout),dpToInt(R.dimen.padding_item_row_layout),dpToInt(R.dimen.padding_item_row_layout),dpToInt(R.dimen.padding_item_row_layout));
        textStatus.setBackgroundResource(R.drawable.table_cell_bg);
        textStatus.setLayoutParams(paramsSMALL);

        if (status == 100){
            textStatus.setTextColor(ContextCompat.getColor(getActivity(),R.color.green_A700));
        }/*else{
            textStatus.setTextColor(ContextCompat.getColor(getActivity(),R.color.red_400));
        }*/

        tableRow.addView(textVendedor);
        tableRow.addView(linearLayout);
        tableRow.addView(textCuota);
        tableRow.addView(textFaltantes);
        tableRow.addView(textStatus);

        tableRow.setPadding(1,1,1,1);
        if (isLastRow)
            tableRow.setBackgroundResource(R.drawable.table_row_last_bg_indigo);
        else
            tableRow.setBackgroundResource(R.drawable.table_row_bg_indigo);
        table_avance.addView(tableRow,new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
    }

    class async_obtenerReporte extends AsyncTask<Void,Void,String> {
        //ProgressDialog pDialog;
        String errorMensaje = "";
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            /*pDialog = new ProgressDialog(getActivity());
            pDialog.setMessage("Obteniendo reporte...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();*/
        }

        @Override
        protected String doInBackground(Void... strings) {
            String response = "";
            if (Util.isConnectingToRed(getActivity())) {
                if (Util.isConnectingToInternet()){
                    try {
                        SoapManager soapManager = new SoapManager(getActivity());
                        soapManager.obtenerRegistrosxSucursalJSON(TablesHelper.AvanceCuota.Sincronizar, TablesHelper.AvanceCuota.Table);
                        response = "OK";
                    } catch (Exception e) {
                        e.printStackTrace();
                        errorMensaje = e.getMessage();
                        Log.e(TAG,e.getMessage()+"");
                        response = "Error";
                    }
                }else{
                    // Sin conexion al Servidor
                    response = "NoInternet";
                }
            } else {
                // Sin conexion al Servidor
                response = "NoInternet";
            }
            return response;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            //pDialog.dismiss();
            try{
                if (result.equals("NoInternet")){
                    //showDialogoPostEnvio("Sin conexión","Por el momento no es posible completar la acción, verifique su conexión", R.drawable.ic_dialog_error);
                    Toast.makeText(getActivity(),"Verifique su conexión", Toast.LENGTH_SHORT).show();
                }else if (result.equals("Error")){
                    //showDialogoPostEnvio("Ocurrió un error",""+errorMensaje, R.drawable.ic_dialog_error);
                    Toast.makeText(getActivity(),"No se pudo cargar el reporte", Toast.LENGTH_SHORT).show();
                }else{
                    refreshLista();
                }
            }catch (Exception e){
                e.printStackTrace();
            }

        }
    }
    private void showDialogoPostEnvio(String titulo, String mensaje,@DrawableRes int icon) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(titulo);
        builder.setMessage(mensaje);
        builder.setIcon(icon);
        builder.setCancelable(false);
        builder.setPositiveButton("ACEPTAR", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog,int which) {
                refreshLista();
            }
        });
        builder.show();
    }

}
