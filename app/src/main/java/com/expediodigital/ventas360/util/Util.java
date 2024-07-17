package com.expediodigital.ventas360.util;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.expediodigital.ventas360.R;
import com.expediodigital.ventas360.Ventas360App;
import com.expediodigital.ventas360.service.UploadPhotoService;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.channels.FileChannel;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by Kevin Robinson Meza Hinostroza on julio 2017.
 * Expedio Digital
 * Kevin.Meza@expediodigital.com
 */

public class Util {
    public static final String TAG = "Util";
    public static int DECIMALES_REDONDEO = 4;

    public static void actualizarToolBar(String title, boolean upButton, Activity activity){
        ((AppCompatActivity) activity).getSupportActionBar().setTitle(title);
        ((AppCompatActivity) activity).getSupportActionBar().setDisplayHomeAsUpEnabled(upButton);
    }

    public static void actualizarToolBar(String title, boolean upButton, Activity activity,@DrawableRes int drawable){
        ((AppCompatActivity) activity).getSupportActionBar().setTitle(title);
        ((AppCompatActivity) activity).getSupportActionBar().setDisplayHomeAsUpEnabled(upButton);
        ((AppCompatActivity) activity).getSupportActionBar().setHomeAsUpIndicator(drawable);
    }

    public static void actualizarToolBar(String title, String subtitle, boolean upButton, Activity activity){
        ((AppCompatActivity) activity).getSupportActionBar().setTitle(title);
        ((AppCompatActivity) activity).getSupportActionBar().setSubtitle(subtitle);
        ((AppCompatActivity) activity).getSupportActionBar().setDisplayHomeAsUpEnabled(upButton);
    }

    public static void actualizarToolBar(String title, String subtitle, boolean upButton, Activity activity, @DrawableRes int drawable){
        ((AppCompatActivity) activity).getSupportActionBar().setTitle(title);
        ((AppCompatActivity) activity).getSupportActionBar().setSubtitle(subtitle);
        ((AppCompatActivity) activity).getSupportActionBar().setDisplayHomeAsUpEnabled(upButton);
        ((AppCompatActivity) activity).getSupportActionBar().setHomeAsUpIndicator(drawable);
    }

    public static DecimalFormat formateador(){
        DecimalFormatSymbols simbolos = new DecimalFormatSymbols();
        simbolos.setDecimalSeparator('.');
        simbolos.setGroupingSeparator(',');

        DecimalFormat formateador = new DecimalFormat("###,##0.00", simbolos);

        return formateador;
    }

    @NonNull
    public static Double redondearDouble(double val){
        String r = val+"";
        BigDecimal big = new BigDecimal(r);
        big = big.setScale(DECIMALES_REDONDEO, RoundingMode.HALF_UP);
        return big.doubleValue();
    }
    @NonNull
    public static Double redondearDouble(double val,int decimalesRedondeo){
        String r = val+"";
        BigDecimal big = new BigDecimal(r);
        big = big.setScale(decimalesRedondeo, RoundingMode.HALF_UP);
        return big.doubleValue();
    }

    @NonNull
    public static int redondearInt(double val,int decimalesRedondeo){
        String r = val+"";
        BigDecimal big = new BigDecimal(r);
        big = big.setScale(decimalesRedondeo, RoundingMode.HALF_UP);
        return big.intValue();
    }

    /**
     * Verifica si está conectado a alguna RED, sólo si está conectado a alguna retorna true, falso de lo contrario
     * @param context
     * @return
     */
    public static boolean isConnectingToRed(Context context){
        ConnectivityManager connectivity = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        Log.i("isConnectingToRed","comprobando...");
        if (connectivity != null) {
            NetworkInfo info = connectivity.getActiveNetworkInfo();
            if (info != null) {
                Log.i("isConnectingToRed",info.isConnectedOrConnecting()+"");
                return info.isConnectedOrConnecting();
            }
        }
        return false;
    }

    /**
     * @return true si hay conexión a internet y false de lo contrario. Es verificado haciendo ping a Google
     */
    public static Boolean isConnectingToInternet() {
        try {
            HttpURLConnection urlc = (HttpURLConnection) (new URL("https://www.facebook.com").openConnection());
            urlc.setRequestProperty("User-Agent", "Test");
            urlc.setRequestProperty("Connection", "close");
            urlc.setConnectTimeout(3000);
            urlc.connect();
            Log.i(TAG,"isConnectingToInternet "+(urlc.getResponseCode() == 200));
            //urlc.disconnect();
            return (urlc.getResponseCode() == 200);
        } catch (IOException e) {
            Log.e(TAG, "Error checking internet connection", e);
        };
        return false;
        /*
        try {
            String comando = "ping -c 1 www.google.com";
            return (Runtime.getRuntime().exec (comando).waitFor() == 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
        */
    }

    /**
     * @return true si hay conexión a internet y false de lo contrario. Es verificado haciendo ping a Google
     */
    public static Boolean isConnectingToServer(Context context) {
        try {
            Ventas360App ventas360App = (Ventas360App) context;
            HttpURLConnection urlc = (HttpURLConnection) (new URL(ventas360App.getUrlWebService()).openConnection());
            Log.i(TAG,"isConnectingTo "+ventas360App.getUrlWebService());
            urlc.setRequestProperty("User-Agent", "Test");
            urlc.setRequestProperty("Connection", "close");
            urlc.setConnectTimeout(3000);
            urlc.connect();
            Log.i(TAG,"isConnectingToInternet servers "+(urlc.getResponseCode() == 200));
            return (urlc.getResponseCode() == 200);
        } catch (IOException e) {
            Log.e(TAG, "Error checking internet connection", e);
        }
        return false;
        /*
        try {
            String comando = "ping -c 1 www.google.com";
            return (Runtime.getRuntime().exec (comando).waitFor() == 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
        */
    }

    /**
     * @return Retorna la fecha actual en formato "01/01/2017"
     */
    public static String getFechaTelefonoString(){
        String fechaActual = "";
        Calendar calendar = Calendar.getInstance(TimeZone.getDefault());
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        if (day<10){
            fechaActual += "0" + day;
        }else{
            fechaActual += "" + day;
        }

        if (month<10){
            fechaActual += "/0" + month;
        }else {
            fechaActual += "/" + month;
        }
        fechaActual += "/"+year;

        return fechaActual;
    }
    /**
     * @return Retorna la fecha actual en formato "01/01/2017 00:00:00"
     */
    public static String getFechaHoraTelefonoString(){
        String fechaActual = "";
        Calendar calendar = Calendar.getInstance(TimeZone.getDefault());
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        int second = calendar.get(Calendar.SECOND);

        if (day<10){
            fechaActual += "0" + day;
        }else{
            fechaActual += "" + day;
        }

        if (month<10){
            fechaActual += "/0" + month;
        }else {
            fechaActual += "/" + month;
        }

        fechaActual += "/"+year;

        if (hour<10){
            fechaActual += " 0"+hour;
        }else{
            fechaActual += " "+hour;
        }

        if (minute<10){
            fechaActual += ":" + "0"+minute;
        }else{
            fechaActual += ":" + minute;
        }

        if (second<10){
            fechaActual += ":" + "0"+second;
        }else{
            fechaActual += ":" + second;
        }

        return fechaActual;

    }

    /**
     * @return Retorna la fecha actual en formato "01/01/2017 00:00:00"
     */
    public static String getFechaHoraTelefonoString_formatoSql(){
        String fechaActual = "";
        Calendar calendar = Calendar.getInstance(TimeZone.getDefault());
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        int second = calendar.get(Calendar.SECOND);

        fechaActual += year;

        if (month<10){
            fechaActual += "-0" + month;
        }else {
            fechaActual += "-" + month;
        }

        if (day<10){
            fechaActual += "-0" + day;
        }else{
            fechaActual += "-" + day;
        }

        if (hour<10){
            fechaActual += " 0"+hour;
        }else{
            fechaActual += " "+hour;
        }

        if (minute<10){
            fechaActual += ":" + "0"+minute;
        }else{
            fechaActual += ":" + minute;
        }

        if (second<10){
            fechaActual += ":" + "0"+second;
        }else{
            fechaActual += ":" + second;
        }

        return fechaActual;

    }

    public static String getHoraTelefonoString(){
        Calendar calendar = Calendar.getInstance(TimeZone.getDefault());

        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        int second = calendar.get(Calendar.SECOND);

        String horaString = "";
        if (hour<10){
            horaString += "0"+hour;
        }else{
            horaString += hour;
        }

        if (minute<10){
            horaString += ":" + "0"+minute;
        }else{
            horaString += ":" + minute;
        }

        if (second<10){
            horaString += ":" + "0"+second;
        }else{
            horaString += ":" + second;
        }
        Log.d(TAG,"getHoraString:"+horaString);
        return horaString;
    }

    /**
     * @param dateString Fecha con el formato "dd/MM/yyyy"
     * @return Retorna la fecha en un objeto Calendar
     */
    public static Calendar convertirStringFecha_aCalendar(String dateString) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        Calendar calendar = Calendar.getInstance();
        try {
            calendar.setTime((Date) dateFormat.parse(dateString));
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return calendar;
    }

    /**
     * @param dateString Fecha con el formato "dd/MM/yyyy HH:mm:ss"
     *                   <p>(<b>Importante</b> el formato de hora "HH:mm:ss" es para 24horas, es decir que la 1 de la tarde sería asi "13:00:00" )</p>
     * @return Retorna la fecha en un objeto Calendar
     */
    public static Calendar convertirStringFechaHora_aCalendar(String dateString) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss"); //Si se quiere trabajar con un formato de 12horas (am-pm) se cambia el "HH" por "hh"
        Calendar calendar = Calendar.getInstance();
        try {
            calendar.setTime((Date) dateFormat.parse(dateString));
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return calendar;
    }

    public static void abrirTeclado(Context context, View view){
        view.requestFocus(); //Asegurar que editText tiene focus
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
    }

    public static void cerrarTeclado(Context context, View view){
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    /**
     * @param fechaString Fecha con el siguiente formato (dia/mes/año). Ejemplo: "01/01/2017"
     * @return Retorna la fecha con el siguiente formato ejemplo: "Domingo 01 de Enero del 2017"
     */
    public static String getFechaExtendida(String fechaString){
        Calendar calendar = Util.convertirStringFecha_aCalendar(fechaString);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd 'de' MMMM 'del' yyyy");
        String currentDate = simpleDateFormat.format(calendar.getTime());

        //Day of Name in full form like,"Saturday", or if you need the first three characters you have to put "EEE" in the date format and your result will be "Sat".
        SimpleDateFormat formatoDia = new SimpleDateFormat("EEEE");
        String dayName = capitalize(formatoDia.format(calendar.getTime()));
        return "" + dayName + " " + currentDate + "";
    }

    /**
     * @return Retorna la fecha actual con el siguiente formato ejemplo: "Domingo 01 de Enero del 2017"
     */
    public static String getFechaExtendida(){
        //Tomamos la hora actual
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("dd 'de' MMMM 'del' yyyy");
        String currentDate = sdf.format(calendar.getTime());

        //Day of Name in full form like,"Saturday", or if you need the first three characters you have to put "EEE" in the date format and your result will be "Sat".
        SimpleDateFormat sdf_ = new SimpleDateFormat("EEEE");
        String dayName = sdf_.format(calendar.getTime());
        return "" + dayName + " " + currentDate + "";
    }

    /**
     * @param texto Texto a darle letra capital
     * @return retorna el texto con la letra capital (Primera letra en mayúscula)
     */
    public static String capitalize(@NonNull String texto) {
        if(texto.length() == 1){ return texto.toUpperCase(); }
        if(texto.length() > 1){ return texto.substring(0,1).toUpperCase() + texto.substring(1); }
        return "";
    }

    /**
     * @param numeroPedidoMaximo El maximo numero de pedido actual
     * @param fechaActual Fecha actual en el formato "01/01/2017"
     * @param serieVendedor Serie del vendedor
     * @return Retorna la secuencia o correlativo del nuevo pedido
     */
    public static String calcularSecuencia(String numeroPedidoMaximo, String fechaActual, String serieVendedor) {
        String [] arrayFecha = fechaActual.split("/");
        String currentDay = arrayFecha[0];
        String currentMonth = arrayFecha[1];
        String currentYear = arrayFecha[2].substring(2,4);
        String currentDate = currentYear+currentMonth+currentDay;

        int secuencia = 1;
        String secueciaCalculada = "";

        if (numeroPedidoMaximo.isEmpty()){
            secueciaCalculada = currentDate+serieVendedor+"00"+secuencia;
            return secueciaCalculada;
        }

        /*--------------------------------------------------------------------*/
        //Se valida que por lo menos tenga 9 digitos (dia mes y año y correlativo)
        if (numeroPedidoMaximo.length() >= 9){
            String cadenaFecha = numeroPedidoMaximo.substring(0,6);
            int year = Integer.parseInt(cadenaFecha.substring(0,2));
            int month = Integer.parseInt(cadenaFecha.substring(2,4));
            int day = Integer.parseInt(cadenaFecha.substring(4,6));
            int sec = Integer.parseInt(numeroPedidoMaximo.substring(numeroPedidoMaximo.length()-3,numeroPedidoMaximo.length()));
            //Verificar cuando sea año nuevo
            if (Integer.parseInt(currentMonth) <= month){
                if (Integer.parseInt(currentDay) > day){
                    secuencia = 1;
                }else{
                    secuencia = sec + 1;
                }
            }else{
                secuencia = 1;
            }

            if (secuencia < 10){
                secueciaCalculada = currentDate+serieVendedor + "00"+secuencia;
            }else if(secuencia < 100){
                secueciaCalculada = currentDate+serieVendedor + "0"+secuencia;
            }else{
                secueciaCalculada = currentDate+serieVendedor + secuencia;
            }
            return secueciaCalculada;

        }else {
            secueciaCalculada = currentDate+serieVendedor+"00"+secuencia;
            return secueciaCalculada;
        }
    }

    public static boolean backupdDatabase(Context context){
        try {
            File sd = Environment.getExternalStorageDirectory();

            File data = Environment.getDataDirectory();
            String packageName  = "com.expediodigital.ventas360";
            String sourceDBName = "ventas360App.db";
            if (sd.canWrite()) {

                String currentDBPath = "data/" + packageName + "/databases/" + sourceDBName;
                File currentDB = new File(data, currentDBPath);

                String timeStamp = new SimpleDateFormat("yyyy-MM-dd-HH-mm").format(new Date());
                String targetDBName = "ventas360AppBK_" + timeStamp + ".db";

                File storageDir = Environment.getExternalStoragePublicDirectory(context.getResources().getString(R.string.Ventas360App_backups));
                if (!storageDir.exists())
                    storageDir.mkdirs();
                File backupDB = new File(storageDir,targetDBName);//Para crear el archivo permanente (crea el archivo con el nombre tal cual, sin agregar nada al final)

                Log.i(TAG,"backupDB=" + backupDB.getAbsolutePath());
                Log.i(TAG,"sourceDB=" + currentDB.getAbsolutePath());

                FileChannel src = new FileInputStream(currentDB).getChannel();
                FileChannel dst = new FileOutputStream(backupDB).getChannel();

                dst.transferFrom(src, 0, src.size());

                src.close();
                dst.close();
                return true;
            }else{
                Log.w(TAG,"backupdDatabase No se puede escribir");
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static String getRealPathFromURI(Context context, Uri contentUri) {
        Cursor cursor = null;
        try {
            String[] proj = { MediaStore.Images.Media.DATA };
            cursor = context.getContentResolver().query(contentUri,  proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public static String alinearTextoDerecha(String texto, int limiteCaracteres){
        if (texto.length() > limiteCaracteres)
            return texto.substring(0,limiteCaracteres);

        String textoAlineado = "";
        int cantidadVacio = limiteCaracteres - texto.length();
        for (int i=0; i < cantidadVacio; i++){
            textoAlineado += " ";
        }
        textoAlineado += texto;
        return textoAlineado;
    }

    public static String alinearTextoIzquierda(String texto, int limiteCaracteres){
        if (texto.length() > limiteCaracteres)
            return texto.substring(0,limiteCaracteres);

        String textoAlineado = texto;
        int cantidadVacio = limiteCaracteres - texto.length();
        for (int i=0; i < cantidadVacio; i++){
            textoAlineado += " ";
        }
        return textoAlineado;
    }

    public static String alinearDecimalDerecha(Double decimal, int limiteCaracteres, int numeroDecimales){
        if (numeroDecimales > 2 || numeroDecimales < 0) numeroDecimales = 2;
        DecimalFormat formateador = formateador();
        String texto = formateador.format(decimal);
        texto = texto.substring(0,texto.length()-(2-numeroDecimales));
        if (texto.length() > limiteCaracteres)
            return texto;

        String textoAlineado = "";
        int cantidadVacio = limiteCaracteres - texto.length();
        for (int i=0; i < cantidadVacio; i++){
            textoAlineado += " ";
        }
        textoAlineado += texto;
        return textoAlineado;
    }

    public static boolean isServiceRunning(Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)){
            if(UploadPhotoService.class.getName().equals(service.service.getClassName())) {
                Log.w(TAG,UploadPhotoService.class.getName()+" is running!");
                return true;
            }
        }
        return false;
    }

    public static void popupAskBonificaciones(Context context){
        final Ventas360App ventas360App = (Ventas360App) context.getApplicationContext();
        String str_mensaje = "";
        if(ventas360App.getSettings_bonificaciones())
        {
            str_mensaje = "¿Desea mantener activado bonificaciones?";
        }
        else{
            str_mensaje = "¿Desea activar bonificaciones?";
        }

        AlertDialog alertDialog = new AlertDialog.Builder(context).create();
        alertDialog.setTitle("Aviso");
        alertDialog.setMessage(str_mensaje);
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "CANCELAR",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "ACTIVAR",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        ventas360App.setSettings_bonificaciones(true);
                        dialog.dismiss();
                    }
                });
        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "DESACTIVAR",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        ventas360App.setSettings_bonificaciones(false);
                        dialog.dismiss();
                    }
                });

        alertDialog.show();
    }
    public static void LogCursorInfo(Cursor c, Context contexto) {
        Log.i(TAG, "*** Cursor Begin *** " + " Results:" +
                c.getCount() + " Columns: " + c.getColumnCount());
        String rowHeaders = "|| ";
        for (int i = 0; i < c.getColumnCount(); i++) {
            rowHeaders = rowHeaders.concat(c.getColumnName(i) + " || ");
        }
        Log.i(TAG, "COLUMNS " + rowHeaders);
        c.moveToFirst();
        while (c.isAfterLast() == false) {
            String rowResults = "|| ";
            for (int i = 0; i < c.getColumnCount(); i++) {
                rowResults = rowResults.concat(c.getString(i) + " || ");
            }
            Log.i(TAG, "Row " + c.getPosition() + ": " + rowResults);
            //Toast.makeText(contexto, rowResults.toString(), Toast.LENGTH_SHORT).show();
            c.moveToNext();
        }
        Log.i(TAG, "*** Cursor End ***");
    }

}
