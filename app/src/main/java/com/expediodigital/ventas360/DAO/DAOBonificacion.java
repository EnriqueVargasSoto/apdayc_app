package com.expediodigital.ventas360.DAO;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.expediodigital.ventas360.model.PedidoDetalleModel;
import com.expediodigital.ventas360.model.ProductoModel;
import com.expediodigital.ventas360.model.PromBonificacionModel;
import com.expediodigital.ventas360.model.PromocionDetalleModel;
import com.expediodigital.ventas360.util.DataBaseHelper;
import com.expediodigital.ventas360.util.TablesHelper;
import com.expediodigital.ventas360.util.Util;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class DAOBonificacion {
    public static final String TAG = "DAOBonificacionDetalle";
    DataBaseHelper dataBaseHelper;

    public DAOBonificacion(Context context) {
        dataBaseHelper = DataBaseHelper.getInstance(context);
    }

    public ArrayList<PromBonificacionModel> getPromocionesValidas(PedidoDetalleModel detalle, String idCliente, String idVendedor, String numeroPedido) {
        ArrayList<PromBonificacionModel> lista = new ArrayList<>();
        try {
            //obtener lista de promociones vigentes
            Date currentTime = Calendar.getInstance().getTime();
            long timestamp = currentTime.getTime();
            String rawQuery;
            rawQuery = "SELECT p.IDPROMOCION, p.DESCRIPCION, p.FECINI, p.FECFIN, p.CONDICION, p.ESTADO, p.ORDEN, p.MECANICA, p.MALLA, "
                    + "mp2.IDGRUPO, mp3.IDRANGO, mp3.UNIDAD, mp3.DESDE, mp3.HASTA, mp3.PORCADA "
                    + " FROM "+ TablesHelper.MPROMO1F.Table + " p "
                    +"INNER JOIN " + TablesHelper.MPROMO2F.Table + " mp2 " + " ON mp2.IDPROMOCION = p.IDPROMOCION "
                    +"INNER JOIN " + TablesHelper.MPROMO3F.Table + " mp3 " + " ON mp3.IDPROMOCION = p.IDPROMOCION "
                    //+"WHERE "+TablesHelper.MPROMO1F.fecini+" < '"+String.valueOf(timestamp)+"' "
                    //+"AND "+TablesHelper.MPROMO1F.fecfin+" > '"+String.valueOf(timestamp)+"' "
                    +"WHERE "+TablesHelper.MPROMO1F.estado+" = 'A' " //TODO verificar con valores que ingresa el cliente
                    +"ORDER BY "+TablesHelper.MPROMO1F.fecfin+" DESC "; //Para poner en primer lugar la promocion porCliente y darle prioridad
            Log.d(TAG,rawQuery);
            SQLiteDatabase db = dataBaseHelper.getReadableDatabase();
            Cursor cur = db.rawQuery(rawQuery, null);

            if (cur.moveToFirst()) {
                do {
                    PromBonificacionModel item = new PromBonificacionModel();
                    item.setIdPromocion(cur.getInt(0));
                    item.setDescripcion(cur.getString(1));
                    item.setFecini(cur.getLong(2));
                    item.setFecfin(cur.getLong(3));
                    item.setCondicion(cur.getString(4));
                    item.setEstado(cur.getString(5));
                    item.setOrden(cur.getInt(6));
                    item.setMecanica(cur.getString(7));
                    item.setMalla(cur.getString(8));

                    item.setIdGrupo(cur.getInt(9));

                    item.setIdRango(cur.getInt(10));
                    item.setUnidad(cur.getString(11));
                    item.setDesde(cur.getString(12));
                    item.setHasta(cur.getString(13));
                    item.setPorcada(cur.getFloat(14));

                    lista.add(item);

                } while (cur.moveToNext());
            }


            cur.close();
        }catch (Exception e){
            e.printStackTrace();
        }
        return lista;
    }


    public ArrayList<ProductoModel> getProductosPromocion(int idGrupo) {
        ArrayList<ProductoModel> lista = new ArrayList<>();
        try {
            //obtener lista de productos en un grupo dado
            String rawQuery;
            rawQuery = "SELECT p.descripcion, p.idLinea, p.idFamilia, p.peso, p.idProveedor, p.tipoProducto, "
                    + " g.ARTICULO, ump.idUnidadManejo, ump.contenido, g.UNIDADES "
                    + " FROM "+ TablesHelper.MGRUP2F.Table + " g "
                    +"INNER JOIN " + TablesHelper.Producto.Table + " p " + " ON p.idProducto = g.ARTICULO "
                    +"INNER JOIN " + TablesHelper.UnidadMedidaxProducto.Table + " ump " + " ON ump.idProducto = g.ARTICULO "
                    +"WHERE "+TablesHelper.MGRUP2F.FKGrup2f+" = '"+String.valueOf(idGrupo)+"' "; //Para poner en primer lugar la promocion porCliente y darle prioridad
            Log.d(TAG,rawQuery);
            SQLiteDatabase db = dataBaseHelper.getReadableDatabase();
            Cursor cur = db.rawQuery(rawQuery, null);

            if (cur.moveToFirst()) {
                do {
                    ProductoModel item = new ProductoModel();
                    item.setDescripcion(cur.getString(0));
                    item.setIdLinea(cur.getString(1));
                    item.setIdFamilia(cur.getString(2));
                    item.setPeso(cur.getLong(3));
                    item.setIdProveedor(cur.getString(4));
                    item.setTipoProducto(cur.getString(5));
                    item.setIdProducto(cur.getString(6));
                    item.setIdUnidadManejo(cur.getString(7));
                    item.setContenido(cur.getString(8));
                    item.setProm_grupo_unidades(cur.getInt(9));

                    lista.add(item);
                } while (cur.moveToNext());
            }

            cur.close();
        }catch (Exception e){
            e.printStackTrace();
        }
        return lista;
    }

    public ArrayList<PedidoDetalleModel> getAcciones(PedidoDetalleModel pedidoDetalle) {

        ArrayList<PedidoDetalleModel> out = new ArrayList<>();
        try {
            //obtener lista de productos en un grupo dado
            String rawQuery;
            rawQuery = "SELECT p4.ARTICULO, p4.UNIDAD, p4.DESCRIPCION "
                    + " FROM "+ TablesHelper.MPROMO5F.Table + " p5 "
                    +" INNER JOIN " + TablesHelper.MPROMO4F.Table + " p4 " + " ON p4.IDACCION = p5.IDACCION "
                    +" WHERE "+TablesHelper.MPROMO5F.FKPromo+" = '"+String.valueOf(pedidoDetalle.getIdPromocion())+"' "; //Para poner en primer lugar la promocion porCliente y darle prioridad
            Log.d(TAG,rawQuery);
            SQLiteDatabase db = dataBaseHelper.getReadableDatabase();
            Cursor cur = db.rawQuery(rawQuery, null);
            Util.LogCursorInfo(cur, null);

            if(cur.getCount() == 0){
                return out;
            }

            if (cur.moveToFirst()) {
                do {
                    PedidoDetalleModel pdm = pedidoDetalle;
                    pdm.setIdProducto( cur.getString(0) );
                    pdm.setIdUnidadMedida("UND");
                    pdm.setDescripcionUnidadMedida("UNIDAD");
                    pdm.setCantidad( pdm.getCantidad() * cur.getInt(1) );
                    pdm.setIdProducto(cur.getString(0));
                    pdm.setDescripcion(cur.getString(2));
                    out.add(pdm);
                } while (cur.moveToNext());
            }

            cur.close();
        }catch (Exception e){
            e.printStackTrace();
        }
        return out;
    }

    public ArrayList<PedidoDetalleModel> getAcciones2(int idPromocion) {

        ArrayList<PedidoDetalleModel> out = new ArrayList<>();
        try {
            //obtener lista de productos en un grupo dado
            String rawQuery;
            rawQuery = "SELECT p4.ARTICULO, p4.UNIDAD, p4.DESCRIPCION, p.peso, P4.IDACCION "
                    + " FROM "+ TablesHelper.MPROMO5F.Table + " p5 "
                    +" INNER JOIN " + TablesHelper.MPROMO4F.Table + " p4 " + " ON p4.IDACCION = p5.IDACCION "
                    +" INNER JOIN " + TablesHelper.Producto.Table + " p " + " ON p.idProducto = p4.ARTICULO "
                    +" WHERE "+TablesHelper.MPROMO5F.FKPromo+" = '"+String.valueOf(idPromocion)+"' "; //Para poner en primer lugar la promocion porCliente y darle prioridad
            Log.d(TAG,rawQuery);
            SQLiteDatabase db = dataBaseHelper.getReadableDatabase();
            Cursor cur = db.rawQuery(rawQuery, null);
            Util.LogCursorInfo(cur, null);

            if(cur.getCount() == 0){
                return out;
            }

            if (cur.moveToFirst()) {
                do {
                    PedidoDetalleModel pdm = new PedidoDetalleModel();
                    pdm.setIdProducto( cur.getString(0) );
                    pdm.setIdUnidadMedida("UND");
                    pdm.setDescripcionUnidadMedida("UNIDAD");
                    pdm.setCantidad( cur.getInt(1) );
                    pdm.setDescripcion(cur.getString(2));
                    pdm.setPesoNeto(cur.getDouble(3));
                    out.add(pdm);
                } while (cur.moveToNext());
            }

            cur.close();
        }catch (Exception e){
            e.printStackTrace();
        }
        return out;
    }

    public ArrayList<PromBonificacionModel> getPromocionesValidas2(String idCliente, String idVendedor, String numeroPedido) {
        ArrayList<PromBonificacionModel> lista = new ArrayList<>();
        try {
            //obtener lista de promociones vigentes
            Date currentTime = Calendar.getInstance().getTime();
            long timestamp = currentTime.getTime();
            String rawQuery;
            rawQuery = "SELECT p.IDPROMOCION, p.DESCRIPCION, p.FECINI, p.FECFIN, p.CONDICION, p.ESTADO, p.ORDEN, p.MECANICA, p.MALLA, "
                    + "mp2.IDGRUPO, mp3.IDRANGO, mp3.UNIDAD, mp3.DESDE, mp3.HASTA, mp3.PORCADA, p.CONDICION "
                    + " FROM "+ TablesHelper.MPROMO1F.Table + " p "
                    +"INNER JOIN " + TablesHelper.MPROMO2F.Table + " mp2 " + " ON mp2.IDPROMOCION = p.IDPROMOCION "
                    +"INNER JOIN " + TablesHelper.MPROMO3F.Table + " mp3 " + " ON mp3.IDPROMOCION = p.IDPROMOCION "
                    //+"WHERE "+TablesHelper.MPROMO1F.fecini+" < '"+String.valueOf(timestamp)+"' "
                    //+"AND "+TablesHelper.MPROMO1F.fecfin+" > '"+String.valueOf(timestamp)+"' "
                    +"WHERE "+TablesHelper.MPROMO1F.estado+" = 'A' " //TODO verificar con valores que ingresa el cliente
                    +"ORDER BY "+TablesHelper.MPROMO1F.fecfin+" DESC "; //Para poner en primer lugar la promocion porCliente y darle prioridad
            Log.d(TAG,rawQuery);
            SQLiteDatabase db = dataBaseHelper.getReadableDatabase();
            Cursor cur = db.rawQuery(rawQuery, null);
            Util.LogCursorInfo(cur, null);
            Log.d(TAG, "este es el resultado: +++++++");
            Log.d(TAG, cur.toString());
            if (cur.moveToFirst()) {
                do {
                    PromBonificacionModel item = new PromBonificacionModel();
                    item.setIdPromocion(cur.getInt(0));
                    item.setDescripcion(cur.getString(1));
                    item.setFecini(cur.getLong(2));
                    item.setFecfin(cur.getLong(3));
                    item.setCondicion(cur.getString(4));
                    item.setEstado(cur.getString(5));
                    item.setOrden(cur.getInt(6));
                    item.setMecanica(cur.getString(7));
                    item.setMalla(cur.getString(8));

                    item.setIdGrupo(cur.getInt(9));

                    item.setIdRango(cur.getInt(10));
                    item.setUnidad(cur.getString(11));
                    item.setDesde(cur.getString(12));
                    item.setHasta(cur.getString(13));
                    item.setPorcada(cur.getFloat(14));
                    item.setCondicion(cur.getString(15));

                    lista.add(item);

                } while (cur.moveToNext());
            }


            cur.close();
        }catch (Exception e){
            e.printStackTrace();
        }
        return lista;
    }


    public DAOGrupoPromocion getDetalleGrupo(int idGrupo) {

        DAOGrupoPromocion out = null;
        try {
            //obtener lista de productos en un grupo dado
            String rawQuery;
            rawQuery = "SELECT g2.IDGRUPO, g2.ARTICULO, g2.MANDATORIO, g2.UNIDADES, g2.MALLA "
                    + " FROM "+ TablesHelper.MGRUP2F.Table + " g2 "
                    +" WHERE "+TablesHelper.MGRUP2F.FKGrup2f+" = '"+String.valueOf(idGrupo)+"' "; //Para poner en primer lugar la promocion porCliente y darle prioridad
            Log.d(TAG,rawQuery);
            SQLiteDatabase db = dataBaseHelper.getReadableDatabase();
            Cursor cur = db.rawQuery(rawQuery, null);
            Util.LogCursorInfo(cur, null);

            if(cur.getCount() == 0){
                return out;
            }

            if (cur.moveToFirst()) {
                do {
                    out = new DAOGrupoPromocion();
                    out.setIDGRUPO( cur.getInt(0) );
                    out.setARTICULO( cur.getInt(1) );
                    out.setMANDATORIO( cur.getInt(2) );
                    out.setUNIDADES( cur.getInt(3) );
                    out.setMALLA( cur.getString(4) );
                } while (cur.moveToNext());
            }

            cur.close();
        }catch (Exception e){
            e.printStackTrace();
        }
        return out;
    }


    public ArrayList<DAOPromo3F> getCondicionPromocionxGrupo(int idPromocion) {

        ArrayList<DAOPromo3F> out = new ArrayList<>();
        try {
            //obtener lista de productos en un grupo dado
            String rawQuery;
            rawQuery = "SELECT g2.IDGRUPO, g2.IDRANGO, g2.UNIDAD, g2.DESDE, g2.HASTA, g2.PORCADA "
                    + " FROM "+ TablesHelper.MPROMO3F.Table + " g2 "
                    +" WHERE "+TablesHelper.MPROMO3F.FKPromo+" = '"+String.valueOf(idPromocion)+"' "; //Para poner en primer lugar la promocion porCliente y darle prioridad
            Log.d(TAG,rawQuery);
            SQLiteDatabase db = dataBaseHelper.getReadableDatabase();
            Cursor cur = db.rawQuery(rawQuery, null);
            Util.LogCursorInfo(cur, null);

            if(cur.getCount() == 0){
                return out;
            }

            if (cur.moveToFirst()) {
                do {
                    DAOPromo3F item = new DAOPromo3F();
                    item.setIDGRUPO( cur.getInt(0) );
                    item.setIDRANGO( cur.getInt(1) );
                    item.setUNIDAD( cur.getString(2) );
                    item.setDESDE( cur.getFloat(3) );
                    item.setHASTA( cur.getFloat(4) );
                    item.setPORCADA( cur.getInt(5) );
                    out.add(item);
                } while (cur.moveToNext());
            }

            cur.close();
        }catch (Exception e){
            e.printStackTrace();
        }
        return out;
    }

    public ArrayList<DAOGrupoPromocion> getMinimosPromocionxGrupo(int idGrupo) {

        ArrayList<DAOGrupoPromocion> out = new ArrayList<>();
        try {
            //obtener lista de productos en un grupo dado
            String rawQuery;
            rawQuery = "SELECT g2.IDGRUPO, g2.ARTICULO, g2.MANDATORIO, g2.UNIDADES, g2.MALLA "
                    + " FROM "+ TablesHelper.MGRUP2F.Table + " g2 "
                    +" WHERE "+TablesHelper.MGRUP2F.FKGrup2f+" = '"+String.valueOf(idGrupo)+"' "; //Para poner en primer lugar la promocion porCliente y darle prioridad
            Log.d(TAG,rawQuery);
            SQLiteDatabase db = dataBaseHelper.getReadableDatabase();
            Cursor cur = db.rawQuery(rawQuery, null);
            Util.LogCursorInfo(cur, null);

            if(cur.getCount() == 0){
                return out;
            }

            if (cur.moveToFirst()) {
                do {
                    DAOGrupoPromocion item = new DAOGrupoPromocion();
                    item.setIDGRUPO( cur.getInt(0) );
                    item.setARTICULO( cur.getInt(1) );
                    item.setMANDATORIO( cur.getInt(2) );
                    item.setUNIDADES( cur.getInt(3) );
                    item.setMALLA( cur.getString(4) );
                    out.add(item);
                } while (cur.moveToNext());
            }

            cur.close();
        }catch (Exception e){
            e.printStackTrace();
        }
        return out;
    }

}
