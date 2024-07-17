package com.expediodigital.ventas360.DAO;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.expediodigital.ventas360.model.PedidoDetalleModel;
import com.expediodigital.ventas360.model.PromocionDetalleModel;
import com.expediodigital.ventas360.util.DataBaseHelper;
import com.expediodigital.ventas360.util.TablesHelper;

import java.util.ArrayList;

/**
 * Created by Kevin Robinson Meza Hinostroza on julio 2017.
 * Expedio Digital
 * Kevin.Meza@expediodigital.com
 */

public class DAOPromocion {
    public static final String TAG = "DAOPromocionDetalle";
    DataBaseHelper dataBaseHelper;

    public DAOPromocion(Context context) {
        dataBaseHelper = DataBaseHelper.getInstance(context);
    }

    public ArrayList<PromocionDetalleModel> getPromocionesProducto(PedidoDetalleModel detalle, String idCliente, String idVendedor, String numeroPedido) {
        ArrayList<PromocionDetalleModel> lista = new ArrayList<>();
        try {
            String rawQuery;
            rawQuery = "SELECT * FROM "+TablesHelper.PromocionDetalle.Table + " p "
                    +"WHERE "+TablesHelper.PromocionDetalle.Entrada+" = '"+detalle.getIdProducto()+"' "
                    +"AND ('"+idCliente+"' IN (SELECT idCliente FROM "+TablesHelper.PromocionxCliente.Table+" WHERE idPromocion = p.idPromocion) OR p.porCliente = 0)"
                    +"AND ('"+idVendedor+"' IN (SELECT idVendedor FROM "+TablesHelper.PromocionxVendedor.Table+" WHERE idPromocion = p.idPromocion) OR p.porVendedor = 0)"
                    +"AND ('"+detalle.getIdPoliticaPrecio()+"' IN (SELECT idPoliticaPrecio FROM "+TablesHelper.PromocionxPoliticaPrecio.Table+" WHERE idPromocion = p.idPromocion) OR p.porPoliticaPrecio = 0) "
                    +"AND ((SELECT substr(fechaPedido,7,4)||'-'||substr(fechaPedido,4,2)||'-'||substr(fechaPedido,1,2) FROM PedidoCabecera WHERE numeroPedido='"+numeroPedido+"') >= "+TablesHelper.PromocionDetalle.FechaInicio+") "//Se valida que no se obtengan promociones recientes para pedidos pasados
                    +"ORDER BY "+TablesHelper.PromocionDetalle.PorCliente+" DESC "; //Para poner en primer lugar la promocion porCliente y darle prioridad
                    //+"LIMIT 1";//Para restringir una sola promocion del producto antes de evaluar.
            //Esto limita las promociones, previamente se quita las demas promociones sin evaluar si se cumplirán o no. Puede que la que se ha escogido no cumpla con las condiciones
            //mientras que una que se quitó si lo cumpla, sin embargo no se podrá saber. La solución que se dió es analizar luego de obtener la bonificacion por cada producto
            Log.d(TAG,rawQuery);
            SQLiteDatabase db = dataBaseHelper.getReadableDatabase();
            Cursor cur = db.rawQuery(rawQuery, null);

            if (cur.moveToFirst()) {
                do {
                    PromocionDetalleModel item = new PromocionDetalleModel();
                    item.setIdPromocion(cur.getInt(0));
                    item.setPromocion(cur.getString(1));
                    item.setTipoPromocion(cur.getString(2));
                    item.setItem(cur.getInt(3));
                    item.setTotalAgrupado(cur.getInt(4));
                    item.setAgrupado(cur.getInt(5));
                    item.setEntrada(cur.getString(6));
                    item.setTipoCondicion(cur.getInt(7));
                    item.setMontoCondicion(cur.getDouble(8));
                    item.setCantidadCondicion(cur.getInt(9));
                    item.setSalida(cur.getString(10));
                    item.setCantidadBonificada(cur.getInt(11));
                    item.setMontoLimite(cur.getDouble(12));
                    item.setCantidadLimite(cur.getInt(13));
                    item.setMaximaBonificacion(cur.getInt(14));
                    item.setAcumulado(cur.getInt(15));
                    item.setPorCliente(cur.getInt(16));
                    item.setPorVendedor(cur.getInt(17));
                    item.setPorPoliticaPrecio(cur.getInt(18));
                    item.setEvaluarEnUnidadMayor(cur.getInt(19));

                    lista.add(item);
                    Log.d(TAG, "getPromocionesProducto: promocion "+item.getIdPromocion());
                } while (cur.moveToNext());
            }
            cur.close();
        }catch (Exception e){
            e.printStackTrace();
        }
        return lista;
    }

    public boolean isPromocionAcumuladoPuro(PromocionDetalleModel itemPromocion){
        boolean flag = true;

        String rawQuery =
                "SELECT MAX("+TablesHelper.PromocionDetalle.TotalAgrupado+") from "+ TablesHelper.PromocionDetalle.Table+" "+
                "WHERE "+TablesHelper.PromocionDetalle.PKeyName+" = "+itemPromocion.getIdPromocion()+" " +
                "AND item = "+itemPromocion.getItem();

        Log.d(TAG, "isPromocionAcumuladoPuro:"+rawQuery);
        SQLiteDatabase db = dataBaseHelper.getReadableDatabase();
        Cursor cur = db.rawQuery(rawQuery, null);

        cur.moveToFirst();
        int totalAgrupado = 0;
        while (!cur.isAfterLast()) {
            totalAgrupado = cur.getInt(0);
            cur.moveToNext();
        }
        cur.close();

        if (totalAgrupado > 1 ) {
            flag = false;
        }
        Log.d(TAG,"isPromocionAcumuladoPuro:"+flag);

        return flag;
    }

    public ArrayList<PromocionDetalleModel> getListaAcumulados(int idPromocion, int itemPromocion) {
        ArrayList<PromocionDetalleModel> lista = new ArrayList<>();
        try {
            String rawQuery;
            rawQuery =
                    "SELECT * FROM "+TablesHelper.PromocionDetalle.Table + " "
                    +"WHERE "+TablesHelper.PromocionDetalle.PKeyName+" = "+idPromocion+" "
                    +"AND "+TablesHelper.PromocionDetalle.Item+" = "+itemPromocion+" "
                    +"AND "+TablesHelper.PromocionDetalle.Acumulado+" = "+PromocionDetalleModel.TIPO_ACUMULADO_PURO_COMPUESTO;
            Log.d(TAG,rawQuery);
            SQLiteDatabase db = dataBaseHelper.getReadableDatabase();
            Cursor cur = db.rawQuery(rawQuery, null);

            if (cur.moveToFirst()) {
                do {
                    PromocionDetalleModel item = new PromocionDetalleModel();
                    item.setIdPromocion(cur.getInt(0));
                    item.setPromocion(cur.getString(1));
                    item.setTipoPromocion(cur.getString(2));
                    item.setItem(cur.getInt(3));
                    item.setTotalAgrupado(cur.getInt(4));
                    item.setAgrupado(cur.getInt(5));
                    item.setEntrada(cur.getString(6));
                    item.setTipoCondicion(cur.getInt(7));
                    item.setMontoCondicion(cur.getDouble(8));
                    item.setCantidadCondicion(cur.getInt(9));
                    item.setSalida(cur.getString(10));
                    item.setCantidadBonificada(cur.getInt(11));
                    item.setMontoLimite(cur.getDouble(12));
                    item.setCantidadLimite(cur.getInt(13));
                    item.setMaximaBonificacion(cur.getInt(14));
                    item.setAcumulado(cur.getInt(15));
                    item.setPorCliente(cur.getInt(16));
                    item.setPorVendedor(cur.getInt(17));
                    item.setPorPoliticaPrecio(cur.getInt(18));
                    item.setEvaluarEnUnidadMayor(cur.getInt(19));

                    lista.add(item);
                    Log.d(TAG, "getListaAcumulados: promocion "+item.getIdPromocion()+" entrada:"+item.getEntrada());
                } while (cur.moveToNext());
            }
            cur.close();
        }catch (Exception e){
            e.printStackTrace();
        }
        return lista;
    }

    public ArrayList<PromocionDetalleModel> getListaAgrupados(int idPromocion, int itemPromocion, int agrupado) {
        ArrayList<PromocionDetalleModel> lista = new ArrayList<>();
        try {
            String rawQuery;
            rawQuery =
                    "SELECT * FROM "+TablesHelper.PromocionDetalle.Table + " "
                            +"WHERE "+TablesHelper.PromocionDetalle.PKeyName+" = "+idPromocion+" "
                            +"AND "+TablesHelper.PromocionDetalle.Item+" = "+itemPromocion+" "
                            +"AND "+TablesHelper.PromocionDetalle.Agrupado+" = "+agrupado;
            Log.d(TAG,rawQuery);
            SQLiteDatabase db = dataBaseHelper.getReadableDatabase();
            Cursor cur = db.rawQuery(rawQuery, null);

            if (cur.moveToFirst()) {
                do {
                    PromocionDetalleModel item = new PromocionDetalleModel();
                    item.setIdPromocion(cur.getInt(0));
                    item.setPromocion(cur.getString(1));
                    item.setTipoPromocion(cur.getString(2));
                    item.setItem(cur.getInt(3));
                    item.setTotalAgrupado(cur.getInt(4));
                    item.setAgrupado(cur.getInt(5));
                    item.setEntrada(cur.getString(6));
                    item.setTipoCondicion(cur.getInt(7));
                    item.setMontoCondicion(cur.getDouble(8));
                    item.setCantidadCondicion(cur.getInt(9));
                    item.setSalida(cur.getString(10));
                    item.setCantidadBonificada(cur.getInt(11));
                    item.setMontoLimite(cur.getDouble(12));
                    item.setCantidadLimite(cur.getInt(13));
                    item.setMaximaBonificacion(cur.getInt(14));
                    item.setAcumulado(cur.getInt(15));
                    item.setPorCliente(cur.getInt(16));
                    item.setPorVendedor(cur.getInt(17));
                    item.setPorPoliticaPrecio(cur.getInt(18));
                    item.setEvaluarEnUnidadMayor(cur.getInt(19));

                    lista.add(item);
                    Log.d(TAG, "getListaAgrupados: promocion "+item.getIdPromocion()+" entrada:"+item.getEntrada());
                } while (cur.moveToNext());
            }
            cur.close();
        }catch (Exception e){
            e.printStackTrace();
        }
        return lista;
    }

    public ArrayList<Integer> getListaAgrupados(int idPromocion, int itemPromocion) {
        ArrayList<Integer> lista = new ArrayList<>();
        try {
            String rawQuery;
            rawQuery =
                    "SELECT DISTINCT "+TablesHelper.PromocionDetalle.Agrupado+" FROM "+TablesHelper.PromocionDetalle.Table + " "
                            +"WHERE "+TablesHelper.PromocionDetalle.PKeyName+" = "+idPromocion+" "
                            +"AND "+TablesHelper.PromocionDetalle.Item+" = "+itemPromocion+" "
                            +"AND "+TablesHelper.PromocionDetalle.Acumulado+" = "+PromocionDetalleModel.TIPO_ACUMULADO_MULTIPLE;
            Log.d(TAG,rawQuery);
            SQLiteDatabase db = dataBaseHelper.getReadableDatabase();
            Cursor cur = db.rawQuery(rawQuery, null);

            if (cur.moveToFirst()) {
                do {
                    lista.add(cur.getInt(0));
                    Log.d(TAG, "getListaAgrupados: promocion "+idPromocion+" agrupado:"+cur.getInt(0));
                } while (cur.moveToNext());
            }
            cur.close();
        }catch (Exception e){
            e.printStackTrace();
        }
        return lista;
    }

    public ArrayList<PromocionDetalleModel> getListaAcumuladosMultiple(int idPromocion, int itemPromocion, int agrupado) {
        ArrayList<PromocionDetalleModel> lista = new ArrayList<>();
        try {
            String rawQuery;
            rawQuery =
                    "SELECT * FROM "+TablesHelper.PromocionDetalle.Table + " "
                            +"WHERE "+TablesHelper.PromocionDetalle.PKeyName+" = "+idPromocion+" "
                            +"AND "+TablesHelper.PromocionDetalle.Item+" = "+itemPromocion+" "
                            +"AND "+TablesHelper.PromocionDetalle.Agrupado+" = "+agrupado+" "
                            +"AND "+TablesHelper.PromocionDetalle.Acumulado+" = "+PromocionDetalleModel.TIPO_ACUMULADO_MULTIPLE;
            Log.d(TAG,rawQuery);
            SQLiteDatabase db = dataBaseHelper.getReadableDatabase();
            Cursor cur = db.rawQuery(rawQuery, null);

            if (cur.moveToFirst()) {
                do {
                    PromocionDetalleModel item = new PromocionDetalleModel();
                    item.setIdPromocion(cur.getInt(0));
                    item.setPromocion(cur.getString(1));
                    item.setTipoPromocion(cur.getString(2));
                    item.setItem(cur.getInt(3));
                    item.setTotalAgrupado(cur.getInt(4));
                    item.setAgrupado(cur.getInt(5));
                    item.setEntrada(cur.getString(6));
                    item.setTipoCondicion(cur.getInt(7));
                    item.setMontoCondicion(cur.getDouble(8));
                    item.setCantidadCondicion(cur.getInt(9));
                    item.setSalida(cur.getString(10));
                    item.setCantidadBonificada(cur.getInt(11));
                    item.setMontoLimite(cur.getDouble(12));
                    item.setCantidadLimite(cur.getInt(13));
                    item.setMaximaBonificacion(cur.getInt(14));
                    item.setAcumulado(cur.getInt(15));
                    item.setPorCliente(cur.getInt(16));
                    item.setPorVendedor(cur.getInt(17));
                    item.setPorPoliticaPrecio(cur.getInt(18));
                    item.setEvaluarEnUnidadMayor(cur.getInt(19));

                    lista.add(item);
                    Log.d(TAG, "getListaAcumuladosMultiple: promocion "+item.getIdPromocion()+" agrupado:"+agrupado+" entrada:"+item.getEntrada());
                } while (cur.moveToNext());
            }
            cur.close();
        }catch (Exception e){
            e.printStackTrace();
        }
        return lista;
    }

    public boolean isPromocionMultiplicadoPorCompra(int idPromocion, String itemPromocion) {
        boolean flag = false;

        String rawQuery =
                "SELECT "+TablesHelper.PromocionDetalle.MultiplicarPorCompra+" FROM "+ TablesHelper.PromocionDetalle.Table+" "+
                "WHERE "+TablesHelper.PromocionDetalle.PKeyName+" = "+idPromocion+" AND "+TablesHelper.PromocionDetalle.Item+" = "+itemPromocion;

        Log.d(TAG, "isPromocionMultiplicadoPorCompra:"+rawQuery);
        SQLiteDatabase db = dataBaseHelper.getReadableDatabase();
        Cursor cur = db.rawQuery(rawQuery, null);

        cur.moveToFirst();
        int multiplica = 0;
        while (!cur.isAfterLast()) {
            multiplica = cur.getInt(0);
            cur.moveToNext();
        }
        cur.close();

        if (multiplica == 1 ) {
            flag = true;
        }
        Log.d(TAG,"isPromocionMultiplicadoPorCompra:"+flag);

        return flag;
    }
}
