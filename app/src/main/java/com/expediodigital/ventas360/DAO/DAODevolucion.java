package com.expediodigital.ventas360.DAO;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.expediodigital.ventas360.DTO.DTODevolucion;
import com.expediodigital.ventas360.DTO.DTODevolucionDetalle;
import com.expediodigital.ventas360.Ventas360App;
import com.expediodigital.ventas360.model.DevolucionCabeceraModel;
import com.expediodigital.ventas360.model.DevolucionDetalleModel;
import com.expediodigital.ventas360.util.DataBaseHelper;
import com.expediodigital.ventas360.util.TablesHelper;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by Kevin Robinson Meza Hinostroza on julio 2017.
 * Expedio Digital
 * Meza.Hinostroza.Robin@gmail.com
 */

public class DAODevolucion {
    public static final String TAG = "DAODevolucion";
    DataBaseHelper dataBaseHelper;
    Context context;

    public DAODevolucion(Context context) {
        dataBaseHelper = DataBaseHelper.getInstance(context);
        this.context = context;
    }

    public DevolucionCabeceraModel getDevolucionCabecera(String numeroGuia) {
        String rawQuery =
                "SELECT numeroGuia,idVendedor,fechaDevolucion,flag FROM "+TablesHelper.DevolucionCabecera.Table+" WHERE numeroGuia='"+numeroGuia+"'";

        SQLiteDatabase db = dataBaseHelper.getReadableDatabase();
        Cursor cur = db.rawQuery(rawQuery, null);
        DevolucionCabeceraModel devolucionCabecera = null;
        cur.moveToFirst();

        while (!cur.isAfterLast()) {
            devolucionCabecera = new DevolucionCabeceraModel();
            devolucionCabecera.setNumeroGuia(cur.getString(0));
            devolucionCabecera.setIdVendedor(cur.getString(1));
            devolucionCabecera.setFechaDevolucion(cur.getString(2));
            devolucionCabecera.setFlag(cur.getString(3));
            cur.moveToNext();
        }
        cur.close();
        return devolucionCabecera;
    }

    public void guardarDevolucionTemporal(String numeroGuia, ArrayList<DevolucionDetalleModel> detalleDevolucion, String idVendedor) {
        try {
            SQLiteDatabase db = dataBaseHelper.getWritableDatabase();
            //Se eliminan la devulucion pendiente de la Guia, ya que se generará otro nuevo.
            String where = TablesHelper.DevolucionCabecera.PKeyName +"=? AND "+TablesHelper.DevolucionCabecera.Flag+"=?";
            String[] args = { numeroGuia, DevolucionCabeceraModel.FLAG_PENDIENTE };
            db.delete(TablesHelper.DevolucionCabecera.Table, where, args);
            db.delete(TablesHelper.DevolucionDetalle.Table, where, args);

            ContentValues Nreg = new ContentValues();
            Nreg.put(TablesHelper.DevolucionCabecera.PKeyName,  numeroGuia);
            Nreg.put(TablesHelper.DevolucionCabecera.FKVendedor, idVendedor);
            Nreg.put(TablesHelper.DevolucionCabecera.FechaDevolucion, "");
            Nreg.put(TablesHelper.DevolucionCabecera.Flag, DevolucionCabeceraModel.FLAG_PENDIENTE);

            db.insert(TablesHelper.DevolucionCabecera.Table, null, Nreg);
            Log.i(TAG, "guardarDevolucionTemporal: Cabecera insertada");

            for (DevolucionDetalleModel devolucionDetalleModel: detalleDevolucion){
                ContentValues Nreg2 = new ContentValues();
                Nreg2.put(TablesHelper.DevolucionDetalle.PKeyName,  numeroGuia);
                Nreg2.put(TablesHelper.DevolucionDetalle.FKProducto, devolucionDetalleModel.getIdProducto());
                Nreg2.put(TablesHelper.DevolucionDetalle.FKUnidadMayor, devolucionDetalleModel.getIdUnidadMedidaMayor());
                Nreg2.put(TablesHelper.DevolucionDetalle.CantidadUnidadMayor, devolucionDetalleModel.getStockDevolucionUnidadMayor());
                Nreg2.put(TablesHelper.DevolucionDetalle.FKUnidadMenor, devolucionDetalleModel.getIdUnidadMedidaMenor());
                Nreg2.put(TablesHelper.DevolucionDetalle.CantidadUnidadMenor, devolucionDetalleModel.getStockDevolucionUnidadMenor());
                Nreg2.put(TablesHelper.DevolucionDetalle.Flag, DevolucionCabeceraModel.FLAG_PENDIENTE);

                db.insert(TablesHelper.DevolucionDetalle.Table, null, Nreg2);
            }
            Log.i(TAG, "guardarDevolucionTemporal: Detalle insertada");
        } catch (Exception e) {
            Log.e(TAG, "guardarDevolucionTemporal: Error al insertar registro");
            e.printStackTrace();
        }
    }

    public void modificarItemDetalleDevolucion (DevolucionDetalleModel item) {
        String where = TablesHelper.DevolucionDetalle.PKeyName + " = ? AND "+TablesHelper.DevolucionDetalle.FKProducto + " = ? AND "+TablesHelper.DevolucionDetalle.Flag + " = ?";
        String[] args = { item.getNumeroGuia(), item.getIdProducto(), item.getFlag() };

        try {
            SQLiteDatabase db = dataBaseHelper.getWritableDatabase();

            ContentValues reg = new ContentValues();
            reg.put(TablesHelper.DevolucionDetalle.FKUnidadMayor, item.getIdUnidadMedidaMayor());
            reg.put(TablesHelper.DevolucionDetalle.CantidadUnidadMayor, item.getStockDevolucionUnidadMayor());
            reg.put(TablesHelper.DevolucionDetalle.FKUnidadMenor, item.getIdUnidadMedidaMenor());
            reg.put(TablesHelper.DevolucionDetalle.CantidadUnidadMenor, item.getStockDevolucionUnidadMenor());
            reg.put(TablesHelper.DevolucionDetalle.Modificado, item.getModificado());
            db.update(TablesHelper.DevolucionDetalle.Table, reg, where, args);

            Log.i(TAG,"modificarItemDetalleDevolucion: actualizado "+item.getNumeroGuia()+" - "+item.getIdProducto());
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public ArrayList<DevolucionDetalleModel> getListaProductoDevolucion(String numeroGuia) {
        ArrayList<DevolucionDetalleModel> listaProducto =  new ArrayList<>();

        String rawQuery =
                "SELECT dd.numeroGuia, dd.idProducto, p.descripcion, ump.contenido " +
                ",dd.idUnidadMayor, um.descripcion, dd.cantidadUnidadMayor " +
                ",dd.idUnidadMenor, um2.descripcion, dd.cantidadUnidadMenor " +
                ",modificado, flag "+
                "FROM "+ TablesHelper.DevolucionDetalle.Table+" dd " +
                "INNER JOIN "+TablesHelper.Producto.Table+" p ON dd.idProducto=p.idProducto " +
                "INNER JOIN "+TablesHelper.UnidadMedida.Table+" um ON um.idUnidadMedida=ump.idUnidadManejo " +
                "INNER JOIN "+TablesHelper.UnidadMedida.Table+" um2 ON um2.idUnidadMedida=ump.idUnidadContable " +
                "INNER JOIN "+TablesHelper.UnidadMedidaxProducto.Table+" ump ON ump.idProducto=p.idProducto " +
                "WHERE dd.numeroGuia ='"+numeroGuia+"' AND dd.flag='P' " +
                "ORDER BY p.descripcion ";
        //Se consulta en flag Pendiente porque si existe una guia en el servidor estará en flag E. Por lo tanto el que se puede modificar y cambiar es el Pendiente y al final este reemplazará al Enviado tomando su lugar.

        Log.d(TAG,rawQuery);
        SQLiteDatabase db = dataBaseHelper.getReadableDatabase();
        Cursor cur = db.rawQuery(rawQuery, null);
        cur.moveToFirst();

        try {
            while (!cur.isAfterLast()) {
                DevolucionDetalleModel item = new DevolucionDetalleModel();
                item.setNumeroGuia(cur.getString(0));
                item.setIdProducto(cur.getString(1));
                item.setDescripcion(cur.getString(2));
                item.setFactorConversion(cur.getInt(3));

                item.setIdUnidadMedidaMayor(cur.getString(4));
                item.setUnidadMedidaMayor(cur.getString(5));
                item.setStockDevolucionUnidadMayor(cur.getInt(6));

                item.setIdUnidadMedidaMenor(cur.getString(7));
                item.setUnidadMedidaMenor(cur.getString(8));
                item.setStockDevolucionUnidadMenor(cur.getInt(9));

                item.setModificado(cur.getInt(10));
                item.setFlag(cur.getString(11));

                listaProducto.add(item);
                cur.moveToNext();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        cur.close();
        return listaProducto;
    }

    public ArrayList<DTODevolucion> getDTODevolucionCompleto(String numeroGuia) {
        String rawQuery = "SELECT * FROM " + TablesHelper.DevolucionCabecera.Table + " WHERE " + TablesHelper.DevolucionCabecera.PKeyName + " = ? AND flag='"+DevolucionCabeceraModel.FLAG_PENDIENTE+"'";
        String[] args = { numeroGuia };
        SQLiteDatabase db = dataBaseHelper.getReadableDatabase();
        Cursor cur = db.rawQuery(rawQuery, args);

        ArrayList<DTODevolucion> listaDevoluciones = new ArrayList<>();
        cur.moveToFirst();

        Ventas360App ventas360App =  (Ventas360App) context.getApplicationContext();
        String idEmpresa = ventas360App.getIdEmpresa();
        String idSucursal = ventas360App.getIdSucursal();
        //String numeroGuia = ventas360App.getNumeroGuia();

        while (!cur.isAfterLast()) {
            DTODevolucion dtoDevolucion = new DTODevolucion();
            dtoDevolucion.setIdEmpresa(idEmpresa);
            dtoDevolucion.setIdSucursal(idSucursal);

            dtoDevolucion.setNumeroGuia(cur.getString(0));
            dtoDevolucion.setIdVendedor(cur.getString(1));
            dtoDevolucion.setFechaDevolucion(cur.getString(2));
            dtoDevolucion.setFlag(cur.getString(3));

            listaDevoluciones.add(dtoDevolucion);
            cur.moveToNext();
        }
        cur.close();

        for (int i = 0; i < listaDevoluciones.size(); i++) {
            // Seteo del detalle de devolucion por el numero de guía
            ArrayList<DTODevolucionDetalle> detalles = getDTODevolucionDetalle(listaDevoluciones.get(i).getNumeroGuia());
            listaDevoluciones.get(i).setDetalles(detalles);
        }

        return listaDevoluciones;
    }

    public ArrayList<DTODevolucionDetalle> getDTODevolucionDetalle(String numeroGuia) {
        String rawQuery;

        rawQuery = "SELECT * FROM "+ TablesHelper.DevolucionDetalle.Table + " WHERE " + TablesHelper.DevolucionDetalle.PKeyName + " = '" + numeroGuia+ "' AND flag='"+DevolucionCabeceraModel.FLAG_PENDIENTE+"'";

        ArrayList<DTODevolucionDetalle> lista = new ArrayList<>();
        SQLiteDatabase db = dataBaseHelper.getReadableDatabase();

        Cursor cur = db.rawQuery(rawQuery, null);
        cur.moveToFirst();

        if (cur.moveToFirst()) {
            do {
                DTODevolucionDetalle dbdetalle = new DTODevolucionDetalle();
                dbdetalle.setNumeroGuia(cur.getString(0));
                dbdetalle.setIdProducto(cur.getString(1));
                dbdetalle.setIdUnidadMayor(cur.getString(2));
                dbdetalle.setCantidadUnidadMayor(cur.getInt(3));
                dbdetalle.setIdUnidadMenor(cur.getString(4));
                dbdetalle.setCantidadUnidadMenor(cur.getInt(5));

                lista.add(dbdetalle);

            } while (cur.moveToNext());

        }
        cur.close();
        return lista;
    }

    public void actualizarFechaDevolucion (String numeroGuia, String fecha) {
        String where = TablesHelper.DevolucionCabecera.PKeyName + " = ? AND "+TablesHelper.DevolucionDetalle.Flag + " = ? ";
        String[] args = { numeroGuia, DevolucionCabeceraModel.FLAG_PENDIENTE};

        try {
            SQLiteDatabase db = dataBaseHelper.getWritableDatabase();

            ContentValues reg = new ContentValues();
            reg.put(TablesHelper.DevolucionCabecera.FechaDevolucion, fecha);
            db.update(TablesHelper.DevolucionCabecera.Table, reg, where, args);

            Log.i(TAG,"actualizarFechaDevolucion: actualizado "+numeroGuia+" - "+fecha);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public String actualizarFlagDevolucion (String cadenaRespuesta){
        String flag = "";
        try {
            JSONArray jsonArray = new JSONArray(cadenaRespuesta);

            SQLiteDatabase db = dataBaseHelper.getWritableDatabase();
            String where = TablesHelper.DevolucionCabecera.PKeyName + " = ?";


            for (int i = 0; i < jsonArray.length(); i++){
                JSONObject jsonData = jsonArray.getJSONObject(i);

                String numeroGuia = jsonData.getString(TablesHelper.DevolucionCabecera.PKeyName).trim();
                flag = jsonData.getString(TablesHelper.DevolucionCabecera.Flag).trim();

                if (flag.equals(DevolucionCabeceraModel.FLAG_ENVIADO)){
                    //Se eliminan las devoluciones Enviadas(Los que se tenía inicialmente) ya que el que se acaba de enviar será el vigente .
                    String where2 = TablesHelper.DevolucionCabecera.PKeyName +"=? AND "+TablesHelper.DevolucionCabecera.Flag+"=?";
                    String[] args2 = { numeroGuia, DevolucionCabeceraModel.FLAG_ENVIADO };
                    db.delete(TablesHelper.DevolucionCabecera.Table, where2, args2);
                    db.delete(TablesHelper.DevolucionDetalle.Table, where2, args2);

                    ContentValues updateValues = new ContentValues();
                    updateValues.put(TablesHelper.DevolucionCabecera.Flag, flag);
                    String[] args = { numeroGuia };
                    Log.i(TAG, "Actualizar "+TablesHelper.DevolucionCabecera.Table+": Modificando..."+numeroGuia);
                    db.update(TablesHelper.DevolucionCabecera.Table, updateValues, where, args );
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Modificar "+TablesHelper.DevolucionCabecera.Table+": Error al modificar registro");
            e.printStackTrace();
            flag = "error";
        }
        return flag;
    }
}
