package com.expediodigital.ventas360.DAO;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.expediodigital.ventas360.DTO.DTOServicio;
import com.expediodigital.ventas360.model.FormaPagoModel;
import com.expediodigital.ventas360.model.GuiaModel;
import com.expediodigital.ventas360.model.VendedorModel;
import com.expediodigital.ventas360.util.DataBaseHelper;
import com.expediodigital.ventas360.util.TablesHelper;
import com.expediodigital.ventas360.util.Util;
import com.github.mikephil.charting.utils.Utils;
import com.google.android.gms.maps.model.LatLng;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;

/**
 * Created by Kevin Robinson Meza Hinostroza on julio 2017.
 * Expedio Digital
 * Meza.Hinostroza.Robin@gmail.com
 */

public class DAOConfiguracion {
    public static final String TAG = "DAOConfiguracion";
    DataBaseHelper dataBaseHelper;
    Context mContext;

    public DAOConfiguracion(Context context) {
        dataBaseHelper = DataBaseHelper.getInstance(context);
        mContext = context;
    }

    public VendedorModel getVendedorUsuario(String user, String pass, String ruc) {
        String rawQuery =
                "SELECT v.* " +
                "FROM "+ TablesHelper.Usuario.Table+" u "+
                "INNER JOIN "+ TablesHelper.Vendedor.Table+ " v ON u.idUsuario = v.idUsuario "+
                "INNER JOIN "+ TablesHelper.Empresa.Table + " e ON u.idEmpresa = e.idEmpresa "+
                "WHERE e.ruc = '"+ruc+"' AND u.usuario like '"+ user + "' AND u.clave ='" + pass + "' AND v.idEmpresa = e.idEmpresa";

        SQLiteDatabase db = dataBaseHelper.getReadableDatabase();
        Log.d(TAG,rawQuery);
        Cursor cur = db.rawQuery(rawQuery, null);
        VendedorModel vendedorModel = null;
        cur.moveToFirst();

        while (!cur.isAfterLast()) {
            vendedorModel = new VendedorModel();
            vendedorModel.setIdEmpresa(cur.getString(0));
            vendedorModel.setIdSucursal(cur.getString(1));
            vendedorModel.setIdVendedor(cur.getString(2));
            vendedorModel.setIdusuario(cur.getString(3));
            vendedorModel.setNombre(cur.getString(4));
            vendedorModel.setTipo(cur.getString(5));
            vendedorModel.setSerie(cur.getString(6));
            vendedorModel.setIdRuta(cur.getString(7));
            if (cur.getString(8) == null){
                vendedorModel.setIdAlmacen("");
            }else{
                vendedorModel.setIdAlmacen(cur.getString(8));
            }

            if (cur.getString(9) == null){
                vendedorModel.setModoVenta("");
            }else{
                vendedorModel.setModoVenta(cur.getString(9));
            }

            cur.moveToNext();
        }
        cur.close();
        return vendedorModel;
    }

    public ArrayList<FormaPagoModel> getCondicionVenta(){
        String rawQuery;
        rawQuery = "SELECT * from "+TablesHelper.FormaPago.Table;
        SQLiteDatabase db = dataBaseHelper.getReadableDatabase();
        Cursor cur = db.rawQuery(rawQuery, null);

        ArrayList<FormaPagoModel> lista = new ArrayList<>();

        cur.moveToFirst();
        while (!cur.isAfterLast()) {
            FormaPagoModel formaPago = new FormaPagoModel();
            formaPago.setIdFormaPago(cur.getString(0));
            formaPago.setDescripcion(cur.getString(1));
            lista.add(formaPago);
            cur.moveToNext();
        }
        cur.close();
        return lista;
    }

    public String getCondicionVentaDescripcion(String idCondicionVenta){
        String descripcion = "";
        String rawQuery;
        rawQuery = "SELECT * from "+TablesHelper.FormaPago.Table + " WHERE "+TablesHelper.FormaPago.PKName + " = "+idCondicionVenta;
        SQLiteDatabase db = dataBaseHelper.getReadableDatabase();
        Cursor cur = db.rawQuery(rawQuery, null);

        cur.moveToFirst();
        while (!cur.isAfterLast()) {
            descripcion = cur.getString(1);
            cur.moveToNext();
        }
        cur.close();
        return descripcion;
    }

    public void limpiarTablas(){
        try {
            SQLiteDatabase db = dataBaseHelper.getWritableDatabase();
            db.delete(TablesHelper.Cliente.Table,null,null);
            db.delete(TablesHelper.Producto.Table,null,null);
            db.delete(TablesHelper.Kardex.Table,null,null);
            db.delete(TablesHelper.PoliticaPrecio.Table,null,null);
            db.delete(TablesHelper.PoliticaPrecioxProducto.Table,null,null);
            db.delete(TablesHelper.Guia.Table,null,null);

            db.delete(TablesHelper.PedidoCabecera.Table,null,null);
            db.delete(TablesHelper.PedidoDetalle.Table,null,null);
            db.delete(TablesHelper.PromocionDetalle.Table,null,null);
            db.delete(TablesHelper.PromocionxCliente.Table,null,null);
            db.delete(TablesHelper.PromocionxVendedor.Table,null,null);
            db.delete(TablesHelper.PromocionxPoliticaPrecio.Table,null,null);
            Log.w(TAG,"limpiarTablas: Limpiando tablas...");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public String getCorreoSoporte() {
        String valor="";
        try{
            String rawQuery =
                    "SELECT * FROM "+TablesHelper.Configuracion.Table+" " +
                            "WHERE "+TablesHelper.Configuracion.PKName + " = '"+TablesHelper.Configuracion.Configuracion_correoSoporte+"'";

            SQLiteDatabase db = dataBaseHelper.getReadableDatabase();
            Log.d(TAG,rawQuery);
            Cursor cur = db.rawQuery(rawQuery, null);
            cur.moveToFirst();

            while (!cur.isAfterLast()) {
                valor = cur.getString(1);
                cur.moveToNext();
            }
            cur.close();
        }catch (Exception e){
            e.printStackTrace();
        }
        return valor;
    }

    public String getNombreEmpresa(String ruc) {
        String empresa="";
        try{
            String rawQuery =
                    "SELECT razonSocial FROM "+TablesHelper.Empresa.Table+" " +
                    "WHERE "+TablesHelper.Empresa.Ruc + " = '"+ruc+"'";

            SQLiteDatabase db = dataBaseHelper.getReadableDatabase();
            //Log.d(TAG,rawQuery);
            Cursor cur = db.rawQuery(rawQuery, null);
            cur.moveToFirst();

            while (!cur.isAfterLast()) {
                empresa = cur.getString(0);
                cur.moveToNext();
            }
            cur.close();
        }catch (Exception e){
            e.printStackTrace();
        }
        return empresa;
    }

    public ArrayList<DTOServicio> getServicios() {

        String rawQuery = "SELECT * FROM "+ TablesHelper.Servicio.Table;

        SQLiteDatabase db = dataBaseHelper.getReadableDatabase();
        Cursor cur = db.rawQuery(rawQuery, null);
        ArrayList<DTOServicio> lista = new ArrayList<>();
        cur.moveToFirst();

        while (!cur.isAfterLast()) {
            DTOServicio model = new DTOServicio();

            model.setIdServicio(cur.getString(0));
            model.setUrl(cur.getString(1));
            if (cur.getString(2) == null){
                model.setTipo("");
            }else{
                model.setTipo(cur.getString(2));
            }

            lista.add(model);
            cur.moveToNext();
        }
        cur.close();

        //TODO temporal, servidor de desarrollo de MTMSAC
        DTOServicio dto = new DTOServicio();
        dto.setIdServicio("5");
        dto.setTipo("D");
        dto.setUrl("http://208.117.87.86:92/Service360.asmx");
        lista.add(dto);

        //TODO temporal, servidor de desarrollo Local
        DTOServicio dto2 = new DTOServicio();
        dto2.setIdServicio("6");
        dto2.setTipo("D");
        dto2.setUrl("http://192.168.0.12:8080/Service360.asmx");
        lista.add(dto2);

        return lista;
    }

    public ArrayList<GuiaModel> getGuiasOperativas() {
        ArrayList<GuiaModel> lista = new ArrayList<>();

        try{
            String rawQuery = "SELECT * FROM "+ TablesHelper.Guia.Table+" WHERE "+TablesHelper.Guia.Estado + " = '"+GuiaModel.ESTADO_OPERANDO+"' "+
                    "ORDER BY "+TablesHelper.Guia.FechaCarga+" DESC" ;//Para que se tome la primera guia con fecha mas reciente de carga

            SQLiteDatabase db = dataBaseHelper.getReadableDatabase();
            Cursor cur = db.rawQuery(rawQuery, null);

            cur.moveToFirst();

            while (!cur.isAfterLast()) {
                GuiaModel model = new GuiaModel();

                model.setNumeroguia(cur.getString(0));
                model.setFechaCarga(cur.getString(1));
                if (cur.getString(2) == null){
                    model.setFechaCierre("");
                }else{
                    model.setFechaCierre(cur.getString(2));
                }
                model.setEstado(cur.getString(3));

                lista.add(model);
                cur.moveToNext();
            }
            cur.close();
        }catch (Exception e){
            e.printStackTrace();
        }
        return lista;
    }

    public ArrayList<GuiaModel> getGuias() {
        ArrayList<GuiaModel> lista = new ArrayList<>();

        try{
            String rawQuery = "SELECT * FROM "+ TablesHelper.Guia.Table+" "+
                    "ORDER BY "+TablesHelper.Guia.FechaCarga+" DESC" ;//Para que se tome la primera guia con fecha mas reciente de carga

            SQLiteDatabase db = dataBaseHelper.getReadableDatabase();
            Cursor cur = db.rawQuery(rawQuery, null);

            cur.moveToFirst();

            while (!cur.isAfterLast()) {
                GuiaModel model = new GuiaModel();

                model.setNumeroguia(cur.getString(0));
                model.setFechaCarga(cur.getString(1));
                if (cur.getString(2) == null){
                    model.setFechaCierre("");
                }else{
                    model.setFechaCierre(cur.getString(2));
                }
                model.setEstado(cur.getString(3));

                lista.add(model);
                cur.moveToNext();
            }
            cur.close();
        }catch (Exception e){
            e.printStackTrace();
        }
        return lista;
    }

    public String getFechaHoraString(){
        Calendar calendar = Calendar.getInstance(TimeZone.getDefault());

        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        int second = calendar.get(Calendar.SECOND);

        String fechaServidor = "";
        try{
            String rawQuery = "SELECT * FROM "+TablesHelper.Configuracion.Table+" " +
                    "WHERE "+TablesHelper.Configuracion.PKName + " = '"+TablesHelper.Configuracion.Fecha+"'";

            SQLiteDatabase db = dataBaseHelper.getReadableDatabase();
            Log.d(TAG,rawQuery);
            Cursor cur = db.rawQuery(rawQuery, null);
            cur.moveToFirst();

            while (!cur.isAfterLast()) {
                fechaServidor = cur.getString(1);
                cur.moveToNext();
            }
            cur.close();

            if (hour<10){
                fechaServidor += " 0"+hour;
            }else{
                fechaServidor += " "+hour;
            }

            if (minute<10){
                fechaServidor += ":" + "0"+minute;
            }else{
                fechaServidor += ":" + minute;
            }

            if (second<10){
                fechaServidor += ":" + "0"+second;
            }else{
                fechaServidor += ":" + second;
            }
            Log.d(TAG,"getFechaHoraString:"+fechaServidor);
            return fechaServidor;
        }catch (Exception e){
            e.printStackTrace();

            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH) + 1;
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            String fechaActual = "";

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

    }

    public String getFechaString(){
        String fechaServidor = "";
        try{
            String rawQuery = "SELECT * FROM "+TablesHelper.Configuracion.Table+" " +
                    "WHERE "+TablesHelper.Configuracion.PKName + " = '"+TablesHelper.Configuracion.Fecha+"'";

            SQLiteDatabase db = dataBaseHelper.getReadableDatabase();
            Log.d(TAG,rawQuery);
            Cursor cur = db.rawQuery(rawQuery, null);
            cur.moveToFirst();

            while (!cur.isAfterLast()) {
                fechaServidor = cur.getString(1);
                cur.moveToNext();
            }
            cur.close();

            return fechaServidor;
        }catch (Exception e){
            e.printStackTrace();

            Calendar calendar = Calendar.getInstance(TimeZone.getDefault());
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH) + 1;
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            String fechaActual = "";

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

    }

    public String getMaximoPedido() {
        String valor="";
        try{
            String rawQuery =
                    "SELECT * FROM "+TablesHelper.Configuracion.Table+" " +
                            "WHERE "+TablesHelper.Configuracion.PKName + " = '"+TablesHelper.Configuracion.MaximoPedido+"'";

            SQLiteDatabase db = dataBaseHelper.getReadableDatabase();
            //Log.d(TAG,rawQuery);
            Cursor cur = db.rawQuery(rawQuery, null);
            cur.moveToFirst();

            while (!cur.isAfterLast()) {
                valor = cur.getString(1);
                cur.moveToNext();
            }
            cur.close();
            Log.d(TAG,rawQuery+" :"+valor);
        }catch (Exception e){
            e.printStackTrace();
        }
        return valor;
    }

    public String getEstadoVendedor(String idEmpresa,String idSucursal, String idVendedor) {
        String estado="O";
        try{
            String rawQuery =
                    "SELECT estado FROM "+TablesHelper.Vendedor.Table+" " +
                    "WHERE idEmpresa = '"+idEmpresa+"' AND idSucursal = '"+idSucursal+"' AND idVendedor = '"+idVendedor+"'";

            SQLiteDatabase db = dataBaseHelper.getReadableDatabase();

            Cursor cur = db.rawQuery(rawQuery, null);
            cur.moveToFirst();

            while (!cur.isAfterLast()) {
                estado = cur.getString(0);
                cur.moveToNext();
            }
            cur.close();
            Log.d(TAG,rawQuery+" :"+estado);
        }catch (Exception e){
            e.printStackTrace();
        }
        return estado;
    }

    public Boolean actualizarEstadoVendedor (String idEmpresa, String idSucursal,String idVendedor, String estado){
        try {
            String where = TablesHelper.Vendedor.FKEmpresa + " = ? AND " + TablesHelper.Vendedor.FKSucursal + " = ? AND " + TablesHelper.Vendedor.PKeyName + " = ?";
            String[] args = { idEmpresa, idSucursal, idVendedor };
            SQLiteDatabase db = dataBaseHelper.getWritableDatabase();

            Log.i(TAG, "where ");
            ContentValues updateValues = new ContentValues();
            updateValues.put(TablesHelper.Vendedor.Estado, estado);
            Log.i(TAG, ": Modificando...");

            db.update(TablesHelper.Vendedor.Table, updateValues, where, args );
            //db.close();
            Log.i(TAG, "Actualizar "+idVendedor+" a "+estado);
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Modificar ");
            e.printStackTrace();
            return false;
        }
    }

    public String getRutaVendedor(String idEmpresa,String idSucursal, String idVendedor) {
        String ruta="";
        try{
            String rawQuery =
                    "SELECT "+TablesHelper.Vendedor.FKRuta+" FROM "+TablesHelper.Vendedor.Table+" " +
                            "WHERE idEmpresa = '"+idEmpresa+"' AND idSucursal = '"+idSucursal+"' AND idVendedor = '"+idVendedor+"'";

            SQLiteDatabase db = dataBaseHelper.getReadableDatabase();

            Cursor cur = db.rawQuery(rawQuery, null);
            cur.moveToFirst();

            while (!cur.isAfterLast()) {
                ruta = cur.getString(0);
                cur.moveToNext();
            }
            cur.close();
            Log.d(TAG,rawQuery+" :"+ruta);
        }catch (Exception e){
            e.printStackTrace();
        }
        return ruta;
    }

    public ArrayList<String> getRutasVendedor(String idEmpresa, String idSucursal, String idVendedor) {
        ArrayList<String> lista = new ArrayList<>();
        String rutas = "";
        try{
            String rawQuery =
                    "SELECT "+TablesHelper.Vendedor.FKRuta+" FROM "+TablesHelper.Vendedor.Table+" " +
                    "WHERE idEmpresa = '"+idEmpresa+"' AND idSucursal = '"+idSucursal+"' AND idVendedor = '"+idVendedor+"'";

            SQLiteDatabase db = dataBaseHelper.getReadableDatabase();

            Cursor cur = db.rawQuery(rawQuery, null);
            cur.moveToFirst();

            while (!cur.isAfterLast()) {
                rutas = cur.getString(0);
                cur.moveToNext();
            }
            cur.close();
            Log.d(TAG,rawQuery+" :"+rutas);
            String[] array = rutas.split(",");
            //lista = Arrays.asList(array);
            Collections.addAll(lista, array);

        }catch (Exception e){
            e.printStackTrace();
        }
        return lista;
    }

    public boolean isPreventaEnLinea() {
        int valor = 0;
        try{
            String rawQuery =
                    "SELECT * FROM "+TablesHelper.Configuracion.Table+" " +
                            "WHERE "+TablesHelper.Configuracion.PKName + " = '"+TablesHelper.Configuracion.PreventaEnLinea+"'";

            SQLiteDatabase db = dataBaseHelper.getReadableDatabase();
            //Log.d(TAG,rawQuery);
            Cursor cur = db.rawQuery(rawQuery, null);
            Util.LogCursorInfo(cur, mContext);
            cur.moveToFirst();

            while (!cur.isAfterLast()) {
                valor = cur.getInt(1);
                cur.moveToNext();
            }
            cur.close();
            Log.d(TAG,rawQuery+" :"+valor);
        }catch (Exception e){
            e.printStackTrace();
        }

        Log.d(TAG,"isPreventaEnLinea: "+valor);

        if (valor == 1) {
            return true;
        } else {
            return false;
        }
    }

    public double getPorcentajeIGV() {
        double porcentaje = 0.18;
        try{
            String rawQuery =
                    "SELECT * FROM "+TablesHelper.Configuracion.Table+" " +
                            "WHERE "+TablesHelper.Configuracion.PKName + " = '"+TablesHelper.Configuracion.PorcentajeIGV+"'";

            SQLiteDatabase db = dataBaseHelper.getReadableDatabase();
            //Log.d(TAG,rawQuery);
            Cursor cur = db.rawQuery(rawQuery, null);
            cur.moveToFirst();

            while (!cur.isAfterLast()) {
                porcentaje = cur.getDouble(1);
                cur.moveToNext();
            }
            cur.close();
            Log.d(TAG,rawQuery+" :"+porcentaje);
        }catch (Exception e){
            e.printStackTrace();
        }

        return porcentaje;
    }

    public ArrayList<HashMap<String, Object>> getReporteAvanceCuota() {
        ArrayList<HashMap<String, Object>> lista = new ArrayList<>();
        try{
            String rawQuery = "SELECT * FROM "+TablesHelper.AvanceCuota.Table + " ORDER BY "+TablesHelper.AvanceCuota.TotalPaquetes + " DESC ";

            SQLiteDatabase db = dataBaseHelper.getReadableDatabase();

            Cursor cur = db.rawQuery(rawQuery, null);
            cur.moveToFirst();

            while (!cur.isAfterLast()) {
                HashMap<String, Object> item = new HashMap<>();
                item.put("idVendedor",cur.getString(0));
                item.put("nombre",cur.getString(1));
                item.put("avanceVenta",cur.getInt(2));
                item.put("cuotaDia",cur.getInt(3));
                item.put("cajasFaltantes",cur.getInt(4));
                item.put("status",cur.getString(5));
                lista.add(item);
                cur.moveToNext();
            }
            cur.close();
        }catch (Exception e){
            e.printStackTrace();
        }
        return lista;
    }

    public String getDireccionSucursal(){
        String direccion = "";
        try{
            String rawQuery =
                    "SELECT * FROM "+TablesHelper.Configuracion.Table + " WHERE "+TablesHelper.Configuracion.PKName + " = '"+TablesHelper.Configuracion.Direccion+"'";

            SQLiteDatabase db = dataBaseHelper.getReadableDatabase();

            Cursor cur = db.rawQuery(rawQuery, null);
            cur.moveToFirst();

            while (!cur.isAfterLast()) {
                direccion = cur.getString(1);
                cur.moveToNext();
            }
            cur.close();
        }catch (Exception e){
            e.printStackTrace();
        }
        return direccion;
    }

    public String getIdClienteGeneral(){
        String idCliente = "";
        try{
            String rawQuery =
                    "SELECT * FROM "+TablesHelper.Configuracion.Table + " WHERE "+TablesHelper.Configuracion.PKName + " = '"+TablesHelper.Configuracion.IdClienteGeneral+"'";

            SQLiteDatabase db = dataBaseHelper.getReadableDatabase();

            Cursor cur = db.rawQuery(rawQuery, null);
            cur.moveToFirst();

            while (!cur.isAfterLast()) {
                idCliente = cur.getString(1);
                cur.moveToNext();
            }
            cur.close();
        }catch (Exception e){
            e.printStackTrace();
        }
        return idCliente;
    }

    public double getLimitePercepcion(){
        double limite = 100.00;
        try{
            String rawQuery =
                    "SELECT * FROM "+TablesHelper.Configuracion.Table+" " +
                            "WHERE "+TablesHelper.Configuracion.PKName + " = '"+TablesHelper.Configuracion.LimitePercepcion+"'";

            SQLiteDatabase db = dataBaseHelper.getReadableDatabase();
            //Log.d(TAG,rawQuery);
            Cursor cur = db.rawQuery(rawQuery, null);
            cur.moveToFirst();

            while (!cur.isAfterLast()) {
                limite = cur.getDouble(1);
                cur.moveToNext();
            }
            cur.close();
            Log.d(TAG,rawQuery+" :"+limite);
        }catch (Exception e){
            e.printStackTrace();
        }

        return limite;
    }

    public boolean isAfectoPercepcion(){
        boolean flag = false;
        try{
            String rawQuery =
                    "SELECT * FROM "+TablesHelper.Configuracion.Table+" " +
                    "WHERE "+TablesHelper.Configuracion.PKName + " = '"+TablesHelper.Configuracion.AfectoPercepcion+"'";

            SQLiteDatabase db = dataBaseHelper.getReadableDatabase();
            Cursor cur = db.rawQuery(rawQuery, null);
            cur.moveToFirst();

            while (!cur.isAfterLast()) {
                if (cur.getInt(0) == 1)
                    flag = true;
                cur.moveToNext();
            }
            cur.close();
            Log.d(TAG,rawQuery+" :"+flag);
        }catch (Exception e){
            e.printStackTrace();
        }

        return flag;
    }

    public String getURLTracking(){
        String url = "";
        try{
            String rawQuery =
                    "SELECT * FROM "+TablesHelper.Configuracion.Table + " WHERE "+TablesHelper.Configuracion.PKName + " = '"+TablesHelper.Configuracion.UrlTracking+"'";

            SQLiteDatabase db = dataBaseHelper.getReadableDatabase();

            Cursor cur = db.rawQuery(rawQuery, null);
            cur.moveToFirst();

            while (!cur.isAfterLast()) {
                url = cur.getString(1);
                cur.moveToNext();
            }
            cur.close();
        }catch (Exception e){
            e.printStackTrace();
        }
        return url;
    }

    public boolean isInfoVendedorCliente() {
        int valor = 1;//Por defecto debe mostrar
        try{
            String rawQuery =
                    "SELECT * FROM "+TablesHelper.Configuracion.Table+" " +
                            "WHERE "+TablesHelper.Configuracion.PKName + " = '"+TablesHelper.Configuracion.InfoVendedorCliente+"'";

            SQLiteDatabase db = dataBaseHelper.getReadableDatabase();
            //Log.d(TAG,rawQuery);
            Cursor cur = db.rawQuery(rawQuery, null);
            cur.moveToFirst();

            while (!cur.isAfterLast()) {
                valor = cur.getInt(1);
                cur.moveToNext();
            }
            cur.close();
            Log.d(TAG,rawQuery+" :"+valor);
        }catch (Exception e){
            e.printStackTrace();
        }

        Log.d(TAG,"isPreventaEnLinea: "+valor);

        if (valor == 1) {
            return true;
        } else {
            return false;
        }
    }

    public void testPedidoDetalle(Context context)
    {

        //test
        String rawQuery2;
        rawQuery2 = "SELECT * FROM "+ TablesHelper.PedidoDetalle.Table  ;
        SQLiteDatabase db2 = dataBaseHelper.getReadableDatabase();
        Cursor cur2 = db2.rawQuery(rawQuery2, null);
        Util.LogCursorInfo(cur2, context);
        //-
    }
}
