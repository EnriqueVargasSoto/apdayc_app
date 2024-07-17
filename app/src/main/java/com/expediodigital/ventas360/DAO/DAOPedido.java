package com.expediodigital.ventas360.DAO;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.expediodigital.ventas360.DTO.DTOMotivoNoVenta;
import com.expediodigital.ventas360.DTO.DTOPedido;
import com.expediodigital.ventas360.DTO.DTOPedidoDetalle;
import com.expediodigital.ventas360.Ventas360App;
import com.expediodigital.ventas360.migraciones.MigrarPedidoDetalle1;
import com.expediodigital.ventas360.model.HRClienteModel;
import com.expediodigital.ventas360.model.HRMarcaResumenModel;
import com.expediodigital.ventas360.model.HRVendedorModel;
import com.expediodigital.ventas360.model.PedidoCabeceraModel;
import com.expediodigital.ventas360.model.PedidoDetalleModel;
import com.expediodigital.ventas360.model.ProductoModel;
import com.expediodigital.ventas360.model.VendedorModel;
import com.expediodigital.ventas360.util.DataBaseHelper;
import com.expediodigital.ventas360.util.TablesHelper;
import com.expediodigital.ventas360.util.Util;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

/**
 * Created by Kevin Robinson Meza Hinostroza on julio 2017.
 * Expedio Digital
 * Meza.Hinostroza.Robin@gmail.com
 */

public class DAOPedido {
    public static final String TAG = "DAOPedido";
    DataBaseHelper dataBaseHelper;
    Context context;

    public DAOPedido(Context context) {
        dataBaseHelper = DataBaseHelper.getInstance(context);
        this.context = context;
    }

    public ArrayList<PedidoCabeceraModel> getPedidosCabecera() {
        String rawQuery =
                "SELECT pc.numeroPedido,pc.importeTotal,pc.fechaPedido,pc.idFormaPago,ifnull(fp.descripcion,''),c.razonSocial,pc.estado," +
                "ifnull(pc.observacion,''),pc.idMotivoNoVenta,mnv.descripcion,pc.pesoTotal,pc.flag," +
                "pc.idCliente,pc.pedidoEntregado "+
                "FROM "+ TablesHelper.PedidoCabecera.Table +" pc " +
                "INNER JOIN "+ TablesHelper.Cliente.Table +" c ON pc.idCliente = c.idCliente " +
                "LEFT JOIN "+ TablesHelper.FormaPago.Table +" fp ON fp.idFormaPago = pc.idFormaPago " +
                "LEFT JOIN "+ TablesHelper.MotivoNoVenta.Table +" mnv ON mnv.idMotivoNoVenta = pc.idMotivoNoVenta " +
                "ORDER BY pc.numeroPedido DESC";

        SQLiteDatabase db = dataBaseHelper.getReadableDatabase();
        Cursor cur = db.rawQuery(rawQuery, null);
        ArrayList<PedidoCabeceraModel> lista = new ArrayList<>();
        cur.moveToFirst();

        while (!cur.isAfterLast()) {
            PedidoCabeceraModel model = new PedidoCabeceraModel();
            model.setNumeroPedido(cur.getString(0));
            model.setImporteTotal(cur.getDouble(1));
            model.setFechaPedido(cur.getString(2));
            model.setIdFormaPago(cur.getString(3));
            model.setFormaPago(cur.getString(4));
            model.setNombreCliente(cur.getString(5));
            model.setEstado(cur.getString(6));
            model.setObservacion(cur.getString(7));
            model.setIdMotivoNoVenta(cur.getString(8));
            model.setMotivoNoVenta(cur.getString(9));
            model.setPesoTotal(cur.getDouble(10));
            model.setFlag(cur.getString(11));

            model.setIdCliente(cur.getString(12));
            model.setPedidoEntregado(cur.getInt(13));

            lista.add(model);
            cur.moveToNext();
        }
        cur.close();
        return lista;
    }

    public String getMaximoNumeroPedido(String idVendedor) {
        String rawQuery;
        rawQuery = "select max(numeroPedido) from "+TablesHelper.PedidoCabecera.Table+" where idVendedor = '"+ idVendedor + "'";

        SQLiteDatabase db = dataBaseHelper.getReadableDatabase();
        Cursor cur = db.rawQuery(rawQuery, null);
        String num_oc = "";

        cur.moveToFirst();
        if (cur.moveToFirst()) {
            do {
                num_oc = cur.getString(0);
            } while (cur.moveToNext());

        }

        if (num_oc == null || num_oc.trim().length() == 0) {
            num_oc = "";
        }
        cur.close();

        DAOConfiguracion daoConfiguracion = new DAOConfiguracion(context);
        String numeroConfiguracion =  daoConfiguracion.getMaximoPedido();

        if (num_oc.isEmpty()){
            //Si no se obtiene el maximo numero de los pedidos, verificar si se tiene alguno en la tabla configuracion. Ya que si es de una Guia anterior, se debe continuar
            return  numeroConfiguracion;
        }else{
            if (numeroConfiguracion.isEmpty()){
                return num_oc;
            }else{//Comparación para obtener el mayor. Siempre la seríe debe ser numérico (no contener letras, sino se tendría que hacer un substring de solo el numero)
                if (Long.parseLong(numeroConfiguracion) > Long.parseLong(num_oc)){
                    return numeroConfiguracion;
                }else{
                    return num_oc;
                }
            }
        }
    }

    public PedidoCabeceraModel getPedidoCabecera(String numeroPedido) {
        String rawQuery =
                "SELECT pc.numeroPedido,c.direccion,pc.importeTotal,pc.fechaPedido,pc.fechaEntrega," +
                        "pc.idFormaPago,ifnull(fp.descripcion,''),c.razonSocial,pc.estado," +
                        "ifnull(pc.observacion,''),pc.idMotivoNoVenta,pc.pesoTotal,pc.flag," +
                        "pc.idCliente, " +
                        "c.direccionFiscal, ifnull(mnv.descripcion,'')," +
                        "ifnull(pc.serieDocumento,''), ifnull(pc.numeroDocumento,''), pc.idVendedor, ifnull(pc.pedidoEntregado,0) "+
                        ",c.rucDni " +
                        "FROM "+TablesHelper.PedidoCabecera.Table+" pc " +
                        "INNER JOIN "+TablesHelper.Cliente.Table+" c ON pc.idCliente = c.idCliente " +
                        "LEFT JOIN "+TablesHelper.FormaPago.Table+" fp ON fp.idFormaPago = pc.idFormaPago " +
                        "LEFT JOIN "+TablesHelper.MotivoNoVenta.Table+" mnv ON pc.idMotivoNoVenta = mnv.idMotivoNoVenta " +
                        "WHERE "+TablesHelper.PedidoCabecera.PKeyName+" = '" + numeroPedido + "' " +
                        "ORDER BY 1 DESC";

        SQLiteDatabase db = dataBaseHelper.getReadableDatabase();
        Cursor cur = db.rawQuery(rawQuery, null);
        PedidoCabeceraModel model = null;
        cur.moveToFirst();
        Log.d(TAG,rawQuery);
        while (!cur.isAfterLast()) {
            model = new PedidoCabeceraModel();
            model.setNumeroPedido(cur.getString(0));
            model.setDireccion(cur.getString(1));
            model.setImporteTotal(cur.getDouble(2));
            model.setFechaPedido(cur.getString(3));
            model.setFechaEntrega(cur.getString(4));
            model.setIdFormaPago(cur.getString(5));
            model.setFormaPago(cur.getString(6));
            model.setNombreCliente(cur.getString(7));
            model.setEstado(cur.getString(8));
            model.setObservacion(cur.getString(9));
            model.setIdMotivoNoVenta(cur.getString(10));
            model.setPesoTotal(cur.getDouble(11));
            model.setFlag(cur.getString(12));
            model.setIdCliente(cur.getString(13));
            model.setDireccionFiscal(cur.getString(14));
            model.setMotivoNoVenta(cur.getString(15));
            model.setSerieDocumento(cur.getString(16));
            model.setNumeroDocumento(cur.getString(17));
            model.setIdVendedor(cur.getString(18));
            model.setPedidoEntregado(cur.getInt(19));
            model.setRucDni(cur.getString(20));

            cur.moveToNext();
        }
        cur.close();
        return model;
    }

    public boolean eliminarItemDetallePedido (String numeroPedido, String idProducto) {
        String where = TablesHelper.PedidoDetalle.PKeyPedido + " = ? AND "+TablesHelper.PedidoDetalle.PKeyProducto + " = ?";
        String[] args = { numeroPedido, idProducto };

        try {
            SQLiteDatabase db = dataBaseHelper.getWritableDatabase();
            db.delete(TablesHelper.PedidoDetalle.Table,where,args);
            Log.i(TAG,"eliminarItemDetallePedido: producto removido");
        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
        return true;
    }

    public ArrayList<PedidoDetalleModel> getListaProductoPedido(String numeroPedido) {


        String rawQuery;

        rawQuery = "SELECT * FROM "+ TablesHelper.PedidoDetalle.Table + " WHERE " + TablesHelper.PedidoDetalle.PKeyPedido + " = '" + numeroPedido+ "'";
        SQLiteDatabase db0 = dataBaseHelper.getReadableDatabase();
        Cursor cur = db0.rawQuery(rawQuery, null);
        Util.LogCursorInfo(cur, context);


        MigrarPedidoDetalle1 mpd1 = new MigrarPedidoDetalle1(dataBaseHelper, context);
        mpd1.checkMalla();
        //String rawQuery;
        rawQuery =
                "SELECT "
                        + "pd.idProducto,"
                        + "p.descripcion,"
                        + "ump.contenido,"
                        + "pd.precioBruto,"
                        + "pd.cantidad,"
                        + "pd.precioNeto,"
                        + "pd.tipoProducto,"
                        + "pd.pesoNeto,"
                        + "pd.idUnidadMedida,"
                        + "pd.idPoliticaPrecio,"
                        + "ifnull(pd.sinStock,0) "
                        + ",ifnull(percepcion, 0) "
                        + ",ifnull(ISC, 0) "
                        + ",ppp.precioManejo "
                        + ",ppp.precioContenido "
                        + ",pd.malla "
                        + "FROM "+TablesHelper.PedidoDetalle.Table+" pd "
                        + "INNER JOIN "+TablesHelper.Producto.Table+" p ON p.idProducto = pd.idProducto "
                        //+ "INNER JOIN "+TablesHelper.UnidadMedida.Table+" um ON pd.idUnidadMedida = um.idUnidadMedida "
                        + "INNER JOIN "+TablesHelper.UnidadMedidaxProducto.Table+" ump ON /*ump.idUnidadManejo = um.idUnidadMedida AND*/ ump.idProducto = p.idProducto "
                        + "INNER JOIN "+TablesHelper.PoliticaPrecioxProducto.Table+" ppp ON /*ppp.precioManejo = pd.precioBruto AND  ppp.idUnidadManejo = um.idUnidadMedida AND*/ ppp.idProducto = p.idProducto "
                        + "WHERE numeroPedido ='" + numeroPedido + "' and pd.cantidad>'0'";

        //Log.d(TAG,"getListaProductoPedido: ___________________________________");
        Log.d(TAG,"getListaProductoPedido: NumeroPedido:"+numeroPedido);
        Log.d(TAG,rawQuery);
        SQLiteDatabase db = dataBaseHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(rawQuery, null);
        Util.LogCursorInfo(cursor, context);
        ArrayList<PedidoDetalleModel> lista = new ArrayList<>();

        if (cursor.moveToFirst()) {
            do {
                PedidoDetalleModel producto = new PedidoDetalleModel();
                producto.setIdProducto(cursor.getString(0));
                producto.setDescripcion(cursor.getString(1));
                producto.setFactorConversion(cursor.getInt(2));
                producto.setPrecioBruto(cursor.getDouble(3));
                producto.setCantidad(cursor.getInt(4));
                producto.setPrecioNeto(cursor.getDouble(5));
                producto.setTipoProducto(cursor.getString(6));
                producto.setPesoNeto(cursor.getDouble(7));
                producto.setIdUnidadMedida(cursor.getString(8));
                producto.setIdPoliticaPrecio(cursor.getString(9));
                producto.setItem(cursor.getInt(10));
                producto.setSinStock(cursor.getInt(10));
                producto.setPercepcion(cursor.getDouble(11));
                producto.setISC(cursor.getDouble(12));

                double p_pmanejo = cursor.getDouble(13);
                double p_pcontenido = cursor.getDouble(14);

                //filtro
                if(p_pmanejo != producto.getPrecioBruto() && p_pcontenido != producto.getPrecioBruto() && producto.getPrecioBruto()!=0){
                    continue;
                }

                if( p_pmanejo == producto.getPrecioBruto()){
                    producto.setDescripcionUnidadMedida(producto.getIdUnidadMedida());
                }else if(producto.getPrecioNeto()==0.0){
                    producto.setDescripcionUnidadMedida(producto.getIdUnidadMedida());
                }
                else{
                    producto.setDescripcionUnidadMedida("Unidades ");
                }
                //Log.v(TAG,"getListaProductoPedido: idProducto:"+producto.getIdProducto());
                if(cursor.getColumnCount()>14) {
                    producto.setMalla(cursor.getString(15));
                }

                boolean isAdded = false;
                for(int i=0; i<lista.size(); i++){
                    if( lista.get(i).getMalla() != null && !lista.get(i).getMalla().isEmpty() ){
                        if(lista.get(i).getIdProducto().equals(producto.getIdProducto())
                                && lista.get(i).getTipoProducto().equals(producto.getTipoProducto())
                                && lista.get(i).getMalla().equals(producto.getMalla())
                                && lista.get(i).getIdUnidadMedida().equals(producto.getIdUnidadMedida())
                        ){
                            isAdded = true;
                            break;
                        }
                    }
                    else{
                        if(lista.get(i).getIdProducto().equals(producto.getIdProducto())
                                && lista.get(i).getTipoProducto().equals(producto.getTipoProducto())
                                && lista.get(i).getIdUnidadMedida().equals(producto.getIdUnidadMedida())
                        ){
                            isAdded = true;
                            break;
                        }
                    }
                }

                if(!isAdded) {
                    lista.add(producto);
                }

            } while (cursor.moveToNext());
        }
        //Log.d(TAG,"getListaProductoPedido: ___________________________________");
        cursor.close();

        return lista;
    }

    /**
     * @param numeroPedido
     * @param item Index o posiciópn del producto en la lista
     * @return Retorna los productos del pedido que sean mayores o iguales al item indicado, utilizado para analizar Promociones
     */
    public ArrayList<PedidoDetalleModel> getListaProductoPedidoEvaluar(String numeroPedido, int item) {

        String rawQuery;

        rawQuery =
                "SELECT "
                        + "pd.idProducto,"
                        + "p.descripcion,"
                        + "p.factorConversion,"
                        + "pd.precioBruto,"
                        + "pd.cantidad,"
                        + "pd.precioNeto,"
                        + "pd.tipoProducto,"
                        + "pd.pesoNeto,"
                        + "pd.idUnidadMedida,"
                        + "pd.idPoliticaPrecio,"
                        + "um.descripcion, "
                        + "pd.item "
                + "FROM "+TablesHelper.PedidoDetalle.Table+" pd "
                + "INNER JOIN "+TablesHelper.Producto.Table+" p ON p.idProducto = pd.idProducto "
                + "INNER JOIN "+TablesHelper.UnidadMedida.Table+" um ON pd.idUnidadMedida = um.idUnidadMedida "
                + "WHERE numeroPedido ='" + numeroPedido + "' AND pd.item <= "+item+" "
                + "ORDER BY item";

        Log.d(TAG,"getListaProductoPedido: ___________________________________");
        Log.d(TAG,"getListaProductoPedido: NumeroPedido:"+numeroPedido);
        SQLiteDatabase db = dataBaseHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(rawQuery, null);
        ArrayList<PedidoDetalleModel> lista = new ArrayList<>();

        if (cursor.moveToFirst()) {
            do {
                PedidoDetalleModel producto = new PedidoDetalleModel();
                producto.setIdProducto(cursor.getString(0));
                producto.setDescripcion(cursor.getString(1));
                producto.setFactorConversion(cursor.getInt(2));
                producto.setPrecioBruto(cursor.getDouble(3));
                producto.setCantidad(cursor.getInt(4));
                producto.setPrecioNeto(cursor.getDouble(5));
                producto.setTipoProducto(cursor.getString(6));
                producto.setPesoNeto(cursor.getDouble(7));
                producto.setIdUnidadMedida(cursor.getString(8));
                producto.setIdPoliticaPrecio(cursor.getString(9));
                producto.setDescripcionUnidadMedida(cursor.getString(10));
                producto.setItem(cursor.getInt(11));
                Log.v(TAG,"getListaProductoPedido: idProducto:"+producto.getIdProducto());
                lista.add(producto);
            } while (cursor.moveToNext());
        }
        Log.d(TAG,"getListaProductoPedido: ___________________________________");
        cursor.close();

        return lista;
    }

    public void actualizarPedidoTotales(double importeTotal, double pesoTotal,String numeroPedido) {

        String where = TablesHelper.PedidoCabecera.PKeyName+" = ?";
        String[] args = { numeroPedido };

        try {

            SQLiteDatabase db = dataBaseHelper.getWritableDatabase();

            ContentValues valor = new ContentValues();
            valor.put(TablesHelper.PedidoCabecera.ImporteTotal, importeTotal);
            valor.put(TablesHelper.PedidoCabecera.PesoTotal, pesoTotal);
            db.update(TablesHelper.PedidoCabecera.Table, valor, where, args);
            Log.i(TAG,"actualizarPedidoTotales: Cabecera actualizada");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public boolean verificarPedidoTieneDetalle(String numeroPedido) {
        String rawQuery = "SELECT * FROM "+TablesHelper.PedidoDetalle.Table+" WHERE "+TablesHelper.PedidoDetalle.PKeyPedido+"= '"+numeroPedido+"'";

        SQLiteDatabase db = dataBaseHelper.getReadableDatabase();
        Cursor cur = db.rawQuery(rawQuery, null);
        if (cur.getCount()>0) {
            cur.close();
            return true;
        }
        cur.close();
        return false;
    }

    public void eliminarPedido(String numeroPedido) {
        String where = TablesHelper.PedidoCabecera.PKeyName+" = ?";
        String[] args = { numeroPedido };

        try {
            SQLiteDatabase db = dataBaseHelper.getWritableDatabase();
            db.delete(TablesHelper.PedidoCabecera.Table, where, args);
            Log.i(TAG, "eliminarPedido: PedidoCabecera eliminado");
            db.delete(TablesHelper.PedidoDetalle.Table, where, args);
            Log.i(TAG, "eliminarPedido: PedidoDetalle eliminado");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void agregarItemPedidoDetalle(PedidoDetalleModel item) {
        SQLiteDatabase db = dataBaseHelper.getWritableDatabase();
        try {

            //actualiza la columna malla
            MigrarPedidoDetalle1 mpd1 = new MigrarPedidoDetalle1(dataBaseHelper, context);
            mpd1.checkMalla();

            String subQuery = "SELECT ifnull(max(item),0) FROM PedidoDetalle WHERE numeroPedido ='"+ item.getNumeroPedido() +"'";
            Cursor curAux = db.rawQuery(subQuery, null);
            curAux.moveToFirst();
            int nro_item=0;
            while (!curAux.isAfterLast()) {
                nro_item = curAux.getInt(0);
                curAux.moveToNext();
            }
            curAux.close();
            nro_item++;
            item.setItem(nro_item);
            //Log.d(TAG,"Agregando producto detalle:");
            //Log.d(TAG,"agregarPedidoDetalle: numeroPedido: "+item.getNumeroPedido());
            //Log.d(TAG,"agregarPedidoDetalle: idProducto: "+item.getIdProducto());

            ContentValues Nreg = new ContentValues();
            Nreg.put(TablesHelper.PedidoDetalle.PKeyPedido, item.getNumeroPedido());
            Nreg.put(TablesHelper.PedidoDetalle.PKeyProducto, item.getIdProducto());
            Nreg.put(TablesHelper.PedidoDetalle.PrecioBruto, item.getPrecioBruto());
            Nreg.put(TablesHelper.PedidoDetalle.PrecioNeto, item.getPrecioNeto());
            Nreg.put(TablesHelper.PedidoDetalle.Cantidad, item.getCantidad());
            Nreg.put(TablesHelper.PedidoDetalle.TipoProducto, item.getTipoProducto());
            Nreg.put(TablesHelper.PedidoDetalle.FKUnidadMedida, item.getIdUnidadMedida());
            Nreg.put(TablesHelper.PedidoDetalle.PesoNeto, item.getPesoNeto());
            Nreg.put(TablesHelper.PedidoDetalle.FKPoliticaPrecio, item.getIdPoliticaPrecio());
            Nreg.put(TablesHelper.PedidoDetalle.Item, item.getItem());//Es necesario una columna para poder ordenar los registros insertados
            Nreg.put(TablesHelper.PedidoDetalle.SinStock, item.getSinStock());
            Nreg.put(TablesHelper.PedidoDetalle.Percepcion, item.getPercepcion());
            Nreg.put(TablesHelper.PedidoDetalle.ISC, item.getISC());
            Nreg.put(TablesHelper.PedidoDetalle.Malla, item.getMalla());

            try {
                db.insertOrThrow(TablesHelper.PedidoDetalle.Table, null, Nreg);
            } catch (Exception e) {
                Log.e(TAG, "agregarPedidoDetalle: Error al insetar registro");
            }finally {
                //db.endTransaction();
            }

            String rq1 = "SELECT * FROM PedidoDetalle WHERE numeroPedido ='"+ item.getNumeroPedido() +"'";
            Cursor cursor = db.rawQuery(rq1, null);
            Log.i(TAG,"agregarPedidoDetalle: Producto insertado "+item.getNumeroPedido()+" - "+item.getIdProducto());
            Util.LogCursorInfo(cursor, context);

        } catch (Exception e) {
            Log.e(TAG, "agregarPedidoDetalle: Error al insetar registro");
            e.printStackTrace();
            if(e.getMessage().contains("idUnidadMenor")){
                renameTableFix1(db);
                agregarItemPedidoDetalle(item);
            }
        }
    }

    private void renameTableFix1(SQLiteDatabase db)
    {

        String rawQuery1 = "DROP TABLE IF EXISTS "+TablesHelper.PedidoDetalle.Table;
        db.execSQL(rawQuery1);

        String rawQuery0 = "CREATE TABLE IF NOT EXISTS "+TablesHelper.PedidoDetalle.Table+" ( " +
                TablesHelper.PedidoDetalle.PKeyPedido + " TEXT(15) NOT NULL, " +
                TablesHelper.PedidoDetalle.PKeyProducto + " TEXT(15) NOT NULL, " +
                TablesHelper.PedidoDetalle.FKPoliticaPrecio + " TEXT(15) NOT NULL, " +
                TablesHelper.PedidoDetalle.TipoProducto + " REAL NOT NULL, " +
                TablesHelper.PedidoDetalle.PrecioBruto + " REAL NOT NULL, " +
                TablesHelper.PedidoDetalle.Cantidad + " INT NOT NULL, " +
                TablesHelper.PedidoDetalle.PrecioNeto + " REAL NOT NULL, " +
                TablesHelper.PedidoDetalle.FKUnidadMedida + " TEXT(5) NOT NULL, " +
                TablesHelper.PedidoDetalle.PesoNeto + " REAL NOT NULL, " +
                TablesHelper.PedidoDetalle.Item + " INT NOT NULL, " +
                TablesHelper.PedidoDetalle.SinStock + " INT NOT NULL, " +
                TablesHelper.PedidoDetalle.Percepcion + " REAL NOT NULL, " +
                TablesHelper.PedidoDetalle.ISC + " REAL NOT NULL, " +
                TablesHelper.PedidoDetalle.Malla + " TEXT(20), " +
                TablesHelper.PedidoDetalle.EstadoDetalle + " TEXT(2) ) ";
        db.execSQL(rawQuery0);
    }

    public void modificarItemDetallePedido (PedidoDetalleModel item) {
        String where = TablesHelper.PedidoDetalle.PKeyPedido + " = ? AND "+TablesHelper.PedidoDetalle.PKeyProducto + " = ? AND "+TablesHelper.PedidoDetalle.TipoProducto + " = ?";
        String[] args = { item.getNumeroPedido(), item.getIdProducto(), item.getTipoProducto() };

        try {
            SQLiteDatabase db = dataBaseHelper.getWritableDatabase();

            ContentValues reg = new ContentValues();
            reg.put(TablesHelper.PedidoDetalle.PrecioBruto,item.getPrecioBruto());
            reg.put(TablesHelper.PedidoDetalle.PrecioNeto, item.getPrecioNeto());
            reg.put(TablesHelper.PedidoDetalle.Cantidad, item.getCantidad());
            reg.put(TablesHelper.PedidoDetalle.FKUnidadMedida, item.getIdUnidadMedida());
            reg.put(TablesHelper.PedidoDetalle.FKPoliticaPrecio, item.getIdPoliticaPrecio());
            reg.put(TablesHelper.PedidoDetalle.PesoNeto,item.getPesoNeto());
            reg.put(TablesHelper.PedidoDetalle.SinStock, item.getSinStock());
            reg.put(TablesHelper.PedidoDetalle.Percepcion, item.getPercepcion());
            reg.put(TablesHelper.PedidoDetalle.ISC, item.getISC());
            db.update(TablesHelper.PedidoDetalle.Table, reg, where, args);

            Log.i(TAG,"modificarItemDetallePedido: actualizado"+item.getNumeroPedido()+" - "+item.getIdProducto());

        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public void actualizarPedidoCabecera(PedidoCabeceraModel cabecera) {
        String where = TablesHelper.PedidoCabecera.PKeyName + " = ?";
        String[] args = { cabecera.getNumeroPedido() };

        try {
            SQLiteDatabase db = dataBaseHelper.getWritableDatabase();

            ContentValues Nreg = new ContentValues();
            Nreg.put(TablesHelper.PedidoCabecera.PKeyName,          cabecera.getNumeroPedido());
            //Nreg.put(TablesHelper.PedidoCabecera.MontoTotal,      cabecera.getMontoTotal()); Este campo se actualiza al modificar el detalle directamente
            Nreg.put(TablesHelper.PedidoCabecera.FechaPedido,       cabecera.getFechaPedido());
            Nreg.put(TablesHelper.PedidoCabecera.FechaEntrega,      cabecera.getFechaEntrega());
            Nreg.put(TablesHelper.PedidoCabecera.FKFormaPago,       cabecera.getIdFormaPago());
            Nreg.put(TablesHelper.PedidoCabecera.FKCliente,         cabecera.getIdCliente());
            Nreg.put(TablesHelper.PedidoCabecera.FKVendedor,        cabecera.getIdVendedor());
            Nreg.put(TablesHelper.PedidoCabecera.Estado,            cabecera.getEstado());
            Nreg.put(TablesHelper.PedidoCabecera.Observacion,       cabecera.getObservacion());
            Nreg.put(TablesHelper.PedidoCabecera.FKMotivoNoVenta,   cabecera.getIdMotivoNoVenta());
            //Nreg.put(TablesHelper.PedidoCabecera.PesoTotal,       cabecera.getPesoTotal()); Este campo se actualiza al modificar el detalle directamente
            Nreg.put(TablesHelper.PedidoCabecera.Flag,              cabecera.getFlag());
            Nreg.put(TablesHelper.PedidoCabecera.Latitud,           cabecera.getLatitud());
            Nreg.put(TablesHelper.PedidoCabecera.Longitud,          cabecera.getLongitud());
            Nreg.put(TablesHelper.PedidoCabecera.PorcentajeBateria, cabecera.getPorcentajeBateria());

            Log.i(TAG, "actualizarPedidoCabecera: Actualizando...");
            db.update(TablesHelper.PedidoCabecera.Table, Nreg, where, args);
            Log.i(TAG, "actualizarPedidoCabecera: Registro actualizado");
        } catch (Exception e) {
            Log.e(TAG, "actualizarPedidoCabecera: Error al actualizar registro");
            e.printStackTrace();
        }
    }

    public void guardarPedidoCabecera(PedidoCabeceraModel cabecera) {
        try {
            SQLiteDatabase db = dataBaseHelper.getWritableDatabase();

            ContentValues Nreg = new ContentValues();
            Nreg.put(TablesHelper.PedidoCabecera.PKeyName,          cabecera.getNumeroPedido());
            Nreg.put(TablesHelper.PedidoCabecera.ImporteTotal,      cabecera.getImporteTotal());
            Nreg.put(TablesHelper.PedidoCabecera.FechaPedido,       cabecera.getFechaPedido());
            Nreg.put(TablesHelper.PedidoCabecera.FechaEntrega,      cabecera.getFechaEntrega());
            Nreg.put(TablesHelper.PedidoCabecera.FKFormaPago,       cabecera.getIdFormaPago());
            Nreg.put(TablesHelper.PedidoCabecera.FKCliente,         cabecera.getIdCliente());
            Nreg.put(TablesHelper.PedidoCabecera.FKVendedor,        cabecera.getIdVendedor());
            Nreg.put(TablesHelper.PedidoCabecera.Estado,            cabecera.getEstado());
            Nreg.put(TablesHelper.PedidoCabecera.Observacion,       cabecera.getObservacion());
            Nreg.put(TablesHelper.PedidoCabecera.FKMotivoNoVenta,   cabecera.getIdMotivoNoVenta());
            Nreg.put(TablesHelper.PedidoCabecera.PesoTotal,         cabecera.getPesoTotal());
            Nreg.put(TablesHelper.PedidoCabecera.Flag,              cabecera.getFlag());
            Nreg.put(TablesHelper.PedidoCabecera.Latitud,           cabecera.getLatitud());
            Nreg.put(TablesHelper.PedidoCabecera.Longitud,          cabecera.getLongitud());
            Nreg.put(TablesHelper.PedidoCabecera.PorcentajeBateria, cabecera.getPorcentajeBateria());

            Log.i(TAG, "GuardarPedidoCabecera: Guardando...");
            db.insertOrThrow(TablesHelper.PedidoCabecera.Table, null, Nreg);
            Log.i(TAG, "GuardarPedidoCabecera: Registro insertado");
        } catch (Exception e) {
            Log.e(TAG, "GuardarPedidoCabecera: Error al insertar registro");
            e.printStackTrace();
        }
    }

    public PedidoDetalleModel getProductoPedido(String numeroPedido,String idProducto) {

        String rawQuery;

        rawQuery =
                "SELECT "
                        + "pd.item,"
                        + "p.descripcion,"
                        + "ump.contenido,"
                        + "pd.idProducto,"
                        + "pd.precioNeto,"
                        + "pd.cantidad,"
                        + "pd.tipoProducto,"
                        + "pd.pesoNeto,"
                        + "pd.idUnidadMedida,"
                        + "pd.precioBruto, "
                        + "pd.idPoliticaPrecio "
                        + ",ppp.precioManejo "
                        + ",ppp.precioContenido "
                        + "FROM "+TablesHelper.PedidoDetalle.Table+" pd "
                        + "INNER JOIN "+TablesHelper.Producto.Table+" p "
                        + "ON p.idProducto = pd.idProducto "
                        //+ "INNER JOIN "+TablesHelper.UnidadMedida.Table+" um ON pd.idUnidadMedida = um.idUnidadMedida "
                        + "INNER JOIN "+TablesHelper.UnidadMedidaxProducto.Table+" ump ON /*pd.idUnidadMedida = ump.idUnidadManejo AND*/ p.idProducto = ump.idProducto "
                        + "INNER JOIN "+TablesHelper.PoliticaPrecioxProducto.Table+" ppp ON /*ppp.precioManejo = pd.precioBruto AND  ppp.idUnidadManejo = um.idUnidadMedida AND*/ ppp.idProducto = p.idProducto "
                        + "WHERE "+TablesHelper.PedidoDetalle.PKeyPedido+"='" + numeroPedido + "' AND pd."+TablesHelper.PedidoDetalle.PKeyProducto+" = '"+ idProducto +"' ";
                        //+ "ORDER BY item";

        Log.d(TAG,"getListaProductoPedido: ___________________________________");
        Log.d(TAG,"getListaProductoPedido: NumeroPedido:"+numeroPedido);
        Log.d(TAG,rawQuery);
        SQLiteDatabase db = dataBaseHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(rawQuery, null);
        Util.LogCursorInfo(cursor, context);
        PedidoDetalleModel producto = null;

        if (cursor.moveToFirst()) {
            do {
                producto = new PedidoDetalleModel();
                producto.setItem(cursor.getInt(0));
                producto.setDescripcion(cursor.getString(1));
                producto.setFactorConversion(cursor.getInt(2));
                producto.setIdProducto(cursor.getString(3));
                producto.setPrecioNeto(cursor.getDouble(4));
                producto.setCantidad(cursor.getInt(5));
                producto.setTipoProducto(cursor.getString(6));
                producto.setPesoNeto(cursor.getDouble(7));
                producto.setIdUnidadMedida(cursor.getString(8));
                producto.setPrecioBruto(cursor.getDouble(9));
                producto.setIdPoliticaPrecio(cursor.getString(10));

                double p_pmanejo = cursor.getDouble(11);
                double p_pcontenido = cursor.getDouble(12);
                //filtro
                if(p_pmanejo != producto.getPrecioBruto() && p_pcontenido != producto.getPrecioBruto() && producto.getPrecioBruto()!=0){
                    continue;
                }

                if( p_pmanejo == producto.getPrecioBruto()){
                    producto.setDescripcionUnidadMedida(producto.getIdUnidadMedida());
                }else if(producto.getPrecioNeto()==0.0){
                    producto.setDescripcionUnidadMedida(producto.getIdUnidadMedida());
                }
                else{
                    producto.setDescripcionUnidadMedida("Unidades ");
                }


                Log.v(TAG,"getProductoPedido: idProducto:"+producto.getIdProducto());
            } while (cursor.moveToNext());
        }
        Log.d(TAG,"getProductoPedido: ___________________________________");
        cursor.close();
        return producto;
    }

    public ArrayList<DTOMotivoNoVenta> getMotivoNoVenta() {

        String rawQuery;
        rawQuery = "SELECT * FROM "+TablesHelper.MotivoNoVenta.Table;

        ArrayList<DTOMotivoNoVenta> motivo_noventa = new ArrayList<>();

        SQLiteDatabase db = dataBaseHelper.getReadableDatabase();
        Cursor cur = db.rawQuery(rawQuery, null);

        cur.moveToFirst();
        if (cur.moveToFirst()) {
            do {
                DTOMotivoNoVenta obj = new DTOMotivoNoVenta();
                obj.setIdMotivoNoVenta(cur.getString(0));
                obj.setDescripcion(cur.getString(1));
                motivo_noventa.add(obj);
            } while (cur.moveToNext());

        }
        cur.close();
        return motivo_noventa;
    }

    public void actualizarMotivoNoVentaPedido (PedidoCabeceraModel pedido) {
        String where = TablesHelper.PedidoCabecera.PKeyName + " = ?";
        String[] args = { pedido.getNumeroPedido() };

        try {
            SQLiteDatabase db = dataBaseHelper.getWritableDatabase();

            ContentValues reg = new ContentValues();
            reg.put(TablesHelper.PedidoCabecera.Flag, pedido.getFlag());
            reg.put(TablesHelper.PedidoCabecera.Estado, pedido.getEstado());
            reg.put(TablesHelper.PedidoCabecera.FKMotivoNoVenta, pedido.getIdMotivoNoVenta());
            reg.put(TablesHelper.PedidoCabecera.Latitud, pedido.getLatitud());
            reg.put(TablesHelper.PedidoCabecera.Longitud, pedido.getLongitud());
            db.update(TablesHelper.PedidoCabecera.Table, reg, where, args);
            Log.i(TAG,"actualizarMotivoNoVentaPedido: actualizado");

        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public void actualizarObservacionPedido (PedidoCabeceraModel pedido) {
        String where = TablesHelper.PedidoCabecera.PKeyName + " = ?";
        String[] args = { pedido.getNumeroPedido() };

        try {
            SQLiteDatabase db = dataBaseHelper.getWritableDatabase();

            ContentValues reg = new ContentValues();
            reg.put(TablesHelper.PedidoCabecera.Flag, pedido.getFlag());
            reg.put(TablesHelper.PedidoCabecera.Observacion, pedido.getObservacion());
            db.update(TablesHelper.PedidoCabecera.Table, reg, where, args);
            Log.i(TAG,"actualizarObservacionPedido: actualizado");

        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public String getIdMotivoNoVentaPedido(String numeroPedido) {
        String id = "0";
        try {
            String rawQuery;
            rawQuery = "SELECT ifnull("+TablesHelper.PedidoCabecera.FKMotivoNoVenta+",'0') FROM "+TablesHelper.PedidoCabecera.Table+" WHERE "+TablesHelper.PedidoCabecera.PKeyName+" = '"+numeroPedido+"'";

            SQLiteDatabase db = dataBaseHelper.getReadableDatabase();
            Cursor cur = db.rawQuery(rawQuery, null);

            cur.moveToFirst();
            if (cur.moveToFirst()) {
                do {
                    id = cur.getString(0);
                } while (cur.moveToNext());
            }
            cur.close();
        }catch (Exception e){
            e.printStackTrace();
        }
        return id;
    }

    public String getObservacionPedido(String numeroPedido) {
        String observacion = "";
        try {
            String rawQuery;
            rawQuery = "SELECT ifnull("+TablesHelper.PedidoCabecera.Observacion+",'0') FROM "+TablesHelper.PedidoCabecera.Table+" WHERE "+TablesHelper.PedidoCabecera.PKeyName+" = '"+numeroPedido+"'";

            SQLiteDatabase db = dataBaseHelper.getReadableDatabase();
            Cursor cur = db.rawQuery(rawQuery, null);

            cur.moveToFirst();
            if (cur.moveToFirst()) {
                do {
                    observacion = cur.getString(0);
                } while (cur.moveToNext());
            }
            cur.close();
        }catch (Exception e){
            e.printStackTrace();
        }
        return observacion;
    }

    public boolean eliminarBonificaciones (String numeroPedido) {
        String where = TablesHelper.PedidoDetalle.PKeyPedido + " = ? AND "+TablesHelper.PedidoDetalle.TipoProducto + " = ?";
        String[] args = { numeroPedido, ProductoModel.TIPO_BONIFICACION};

        try {
            SQLiteDatabase db = dataBaseHelper.getWritableDatabase();
            db.delete(TablesHelper.PedidoDetalle.Table,where,args);
            Log.i(TAG,"eliminarBonificaciones: bonificaciones removidas");
        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
        return true;
    }

    public ArrayList<DTOPedido> getDTOPedidoCompleto(String numeroPedido) throws ParseException {
        String rawQuery = "SELECT * FROM " + TablesHelper.PedidoCabecera.Table + " WHERE " + TablesHelper.PedidoCabecera.PKeyName + " = ?";
        String[] args = { numeroPedido };
        SQLiteDatabase db = dataBaseHelper.getReadableDatabase();
        Cursor cur = db.rawQuery(rawQuery, args);
        Util.LogCursorInfo(cur, context);

        ArrayList<DTOPedido> lista_pedidos = new ArrayList<>();
        cur.moveToFirst();

        Ventas360App ventas360App =  (Ventas360App) context.getApplicationContext();
        String idEmpresa = ventas360App.getIdEmpresa();
        String idSucursal = ventas360App.getIdSucursal();
        String idAlmacen = ventas360App.getIdAlmacen();
        String numeroGuia = ventas360App.getNumeroGuia();

        while (!cur.isAfterLast()) {

            String old_fp = cur.getString(3);
            String old_fe = cur.getString(4);
            String old_hm = cur.getString(20);

            DateFormat df_fp = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss");

            SimpleDateFormat f_fp = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss");
            SimpleDateFormat f_fe = new SimpleDateFormat("dd/MM/yyyy");
            Date date_fp = f_fp.parse(old_fp);
            String new_fe = old_fe;
            if(old_fe != null &&!old_fe.isEmpty()) {
                Date date_fe = f_fe.parse(old_fe);
                new_fe = df_fp.format(date_fe);
            }
            Date date_hm;
            if(old_hm==null || old_hm.isEmpty()){
                date_hm = date_fp;
            }
            else{
                date_hm = f_fp.parse(old_hm);
            }

            String new_fp = df_fp.format(date_fp);
            String new_hm = df_fp.format(date_hm);;

            DTOPedido dbpedido = new DTOPedido();
            dbpedido.setIdEmpresa(idEmpresa);
            dbpedido.setIdSucursal(idSucursal);
            dbpedido.setIdAlmacen(idAlmacen);
            dbpedido.setNumeroGuia(numeroGuia);

            dbpedido.setNumeroPedido(cur.getString(0));
            dbpedido.setIdCliente(cur.getString(1));
            dbpedido.setIdVendedor(cur.getString(2));
//            dbpedido.setFechaPedido(cur.getString(3));//
//            dbpedido.setFechaEntrega(cur.getString(4));//
            dbpedido.setFechaPedido(new_fp);
            dbpedido.setFechaEntrega(new_fe);
            dbpedido.setIdFormaPago(cur.getString(5));
            dbpedido.setObservacion(cur.getString(6));
            dbpedido.setPesoTotal(cur.getDouble(7));
            dbpedido.setImporteTotal(cur.getDouble(8));

            if (cur.getString(9) == null){
                dbpedido.setIdMotivoNoVenta("0");
            }else{
                dbpedido.setIdMotivoNoVenta(cur.getString(9));
            }

            dbpedido.setEstado(cur.getString(10));
            dbpedido.setFlag(cur.getString(11));

            dbpedido.setLatitud(cur.getDouble(14));
            dbpedido.setLongitud(cur.getDouble(15));
            if (cur.getString(16) == null && cur.getString(17) == null){
                dbpedido.setLatitudDocumento(0.0);
                dbpedido.setLongitudDocumento(0.0);
            }else{
                dbpedido.setLatitudDocumento(cur.getDouble(16));
                dbpedido.setLongitudDocumento(cur.getDouble(17));
            }
            dbpedido.setPorcentajeBateria(cur.getInt(18));
            dbpedido.setHoraFin(cur.getString(19));
            //dbpedido.setHoraModificacion(cur.getString(20));//
            dbpedido.setHoraModificacion(new_hm);

            if (cur.getString(21) == null)
                dbpedido.setPedidoEntregado(0);
            else
                dbpedido.setPedidoEntregado(cur.getInt(21));

            if (cur.getString(22) == null)
                dbpedido.setFechaEntregado("");
            else
                dbpedido.setFechaEntregado(cur.getString(22));

            Date dt = new Date();
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss");
            dbpedido.setFechaModificado( sdf.format(dt) );

            lista_pedidos.add(dbpedido);
            cur.moveToNext();
        }
        cur.close();

        for (int i = 0; i < lista_pedidos.size(); i++) {
            // Seteo del detalle del pedido por el oc_numero
            ArrayList<DTOPedidoDetalle> detalles = getDTOPedidoDetalle(lista_pedidos.get(i).getNumeroPedido());
            lista_pedidos.get(i).setDetalles(detalles);
        }

        return lista_pedidos;
    }

    public ArrayList<DTOPedidoDetalle> getDTOPedidoDetalle(String numeroPedido) {



        String rawQuery0 =
                "SELECT "
                        + "pd.idProducto,"
                        + "pd.tipoProducto,"
                        + "pd.idUnidadMedida,"
                        + "pd.idPoliticaPrecio "
                        + "FROM "+TablesHelper.PedidoDetalle.Table+" pd "
                        + "INNER JOIN "+TablesHelper.Producto.Table+" p ON p.idProducto = pd.idProducto "
                        + "INNER JOIN "+TablesHelper.UnidadMedidaxProducto.Table+" ump ON /*ump.idUnidadManejo = um.idUnidadMedida AND*/ ump.idProducto = p.idProducto "
                        + "INNER JOIN "+TablesHelper.PoliticaPrecioxProducto.Table+" ppp ON /*ppp.precioManejo = pd.precioBruto AND  ppp.idUnidadManejo = um.idUnidadMedida AND*/ ppp.idProducto = p.idProducto "
                        + "WHERE numeroPedido ='" + numeroPedido + "' ";


        //Log.d(TAG,"getListaProductoPedido: ___________________________________");
        Log.d(TAG,"getListaProductoPedido: NumeroPedido:"+numeroPedido);
        Log.d(TAG,rawQuery0);
        SQLiteDatabase db = dataBaseHelper.getReadableDatabase();

        //
        String rawQuery1 =
                "SELECT "
                        + " * "
                        + "FROM "+TablesHelper.PedidoDetalle.Table+" pd "
                        + "WHERE numeroPedido ='" + numeroPedido + "' ";
        Cursor cursor1 = db.rawQuery(rawQuery1, null);
        Util.LogCursorInfo(cursor1, context);
        //

        Cursor cursor0 = db.rawQuery(rawQuery0, null);
        Util.LogCursorInfo(cursor0, context);
        ArrayList<PedidoDetalleModel> lista_muestra = new ArrayList<>();
        if (cursor0.moveToFirst()) {
            do {
                PedidoDetalleModel producto = new PedidoDetalleModel();
                producto.setIdProducto(cursor0.getString(0));
                producto.setTipoProducto(cursor0.getString(1));
                producto.setIdUnidadMedida(cursor0.getString(2));
                producto.setIdPoliticaPrecio(cursor0.getString(3));

                boolean isAdded = false;
                for(int i=0; i<lista_muestra.size(); i++){
                    if( lista_muestra.get(i).getMalla() != null && !lista_muestra.get(i).getMalla().isEmpty() ){
                        if(lista_muestra.get(i).getIdProducto().equals(producto.getIdProducto())
                                && lista_muestra.get(i).getTipoProducto().equals(producto.getTipoProducto())
                                && lista_muestra.get(i).getMalla().equals(producto.getMalla())
                                && lista_muestra.get(i).getIdUnidadMedida().equals(producto.getIdUnidadMedida())
                        ){
                            isAdded = true;
                            break;
                        }
                    }
                    else{
                        if(lista_muestra.get(i).getIdProducto().equals(producto.getIdProducto())
                                && lista_muestra.get(i).getTipoProducto().equals(producto.getTipoProducto())
                                && lista_muestra.get(i).getIdUnidadMedida().equals(producto.getIdUnidadMedida())
                        ){
                            isAdded = true;
                            break;
                        }
                    }
                }

                if(!isAdded) {
                    lista_muestra.add(producto);
                }

            } while (cursor0.moveToNext());
        }
        cursor0.close();


        String rawQuery;
        rawQuery = "SELECT * FROM "+ TablesHelper.PedidoDetalle.Table + " WHERE " + TablesHelper.PedidoDetalle.PKeyPedido + " = '" + numeroPedido+ "'";

        ArrayList<DTOPedidoDetalle> lista = new ArrayList<DTOPedidoDetalle>();

        Cursor cur = db.rawQuery(rawQuery, null);
        Util.LogCursorInfo(cur, context);
        cur.moveToFirst();

        if (cur.moveToFirst()) {
            do {
                DTOPedidoDetalle dbdetalle = new DTOPedidoDetalle();
                dbdetalle.setNumeroPedido(cur.getString(0));
                dbdetalle.setIdProducto(cur.getString(1));
                dbdetalle.setIdPoliticaPrecio(cur.getString(2));
                dbdetalle.setTipoProducto(cur.getString(3));
                dbdetalle.setPrecioBruto(cur.getDouble(4));
                dbdetalle.setCantidad(cur.getInt(5));
                dbdetalle.setPrecioNeto(cur.getDouble(6));
                dbdetalle.setIdUnidadMedida(cur.getString(7));
                dbdetalle.setPesoNeto(cur.getDouble(8));
                //dbdetalle.setItem(cur.getString(9));
                //dbdetalle.setSinStock(cur.getInt(10));
                dbdetalle.setPercepcion(cur.getDouble(11));
                dbdetalle.setISC(cur.getDouble(12));
                if(cur.getColumnCount()>13 && cur.getString(13) != null){
                    dbdetalle.setMalla(cur.getString(13));
                }else {
                    dbdetalle.setMalla("");
                }

                if(dbdetalle.getCantidad() == 0)
                {
                    continue;
                }

                boolean isShowed = false;
                for(PedidoDetalleModel pdm: lista_muestra){
                    if(dbdetalle.getIdProducto().equals(pdm.getIdProducto()))
                    {
                        isShowed = true;
                    }
                }

                if(!isShowed)
                {
                    continue;
                }

                boolean isAdded = false;
                for(int i=0; i<lista.size(); i++){
                    if( lista.get(i).getMalla() != null && !lista.get(i).getMalla().isEmpty() ){
                        if(lista.get(i).getIdProducto().equals(dbdetalle.getIdProducto())
                                && lista.get(i).getTipoProducto().equals(dbdetalle.getTipoProducto())
                                && lista.get(i).getMalla().equals(dbdetalle.getMalla())
                                && lista.get(i).getIdUnidadMedida().equals(dbdetalle.getIdUnidadMedida())
                        ){
                            isAdded = true;
                            break;
                        }
                    }
                    else{
                        if(lista.get(i).getIdProducto().equals(dbdetalle.getIdProducto())
                                && lista.get(i).getTipoProducto().equals(dbdetalle.getTipoProducto())
                                && lista.get(i).getIdUnidadMedida().equals(dbdetalle.getIdUnidadMedida())
                        ){
                            isAdded = true;
                            break;
                        }
                    }
                }

                if(!isAdded) {
                    lista.add(dbdetalle);
                }


            } while (cur.moveToNext());

        }
        cur.close();
        return lista;
    }

    public String actualizarFlagPedidos (String cadenaRespuesta){
        String flag = "";
        try {
            JSONArray jsonArray = new JSONArray(cadenaRespuesta);

            SQLiteDatabase db = dataBaseHelper.getWritableDatabase();
            String where = TablesHelper.PedidoCabecera.PKeyName + " = ?";


            for (int i = 0; i < jsonArray.length(); i++){
                JSONObject jsonData = jsonArray.getJSONObject(i);

                String numeroPedido = jsonData.getString(TablesHelper.PedidoCabecera.PKeyName).trim();
                flag = jsonData.getString(TablesHelper.PedidoCabecera.Flag).trim();

                if (flag.equals(PedidoCabeceraModel.FLAG_ENVIADO) || flag.equals(PedidoCabeceraModel.FLAG_PENDIENTE)){
                    ContentValues updateValues = new ContentValues();
                    updateValues.put(TablesHelper.PedidoCabecera.Flag, flag);
                    String[] args = { numeroPedido };

                    Log.i(TAG, "Actualizar "+TablesHelper.PedidoCabecera.Table+": Modificando..."+numeroPedido);
                    db.update(TablesHelper.PedidoCabecera.Table, updateValues, where, args );
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Modificar "+TablesHelper.PedidoCabecera.Table+": Error al modificar registro");
            e.printStackTrace();
            flag = "error";
        }
        return flag;
    }

    public ArrayList<DTOPedido> getDTOPedidosPendientes() {
        String rawQuery =
                "SELECT * FROM " + TablesHelper.PedidoCabecera.Table +
                        " WHERE " + TablesHelper.PedidoCabecera.Flag + " IN ('P','I')";
        SQLiteDatabase db = dataBaseHelper.getReadableDatabase();

        Cursor cur = db.rawQuery(rawQuery, null);
        ArrayList<DTOPedido> lista_pedidos = new ArrayList<>();
        cur.moveToFirst();

        Ventas360App ventas360App =  (Ventas360App) context.getApplicationContext();
        String idEmpresa = ventas360App.getIdEmpresa();
        String idSucursal = ventas360App.getIdSucursal();
        String idAlmacen = ventas360App.getIdAlmacen();;
        String numeroGuia = ventas360App.getNumeroGuia();

        while (!cur.isAfterLast()) {
            DTOPedido dbpedido = new DTOPedido();
            dbpedido.setIdEmpresa(idEmpresa);
            dbpedido.setIdSucursal(idSucursal);
            dbpedido.setIdAlmacen(idAlmacen);
            dbpedido.setNumeroGuia(numeroGuia);

            dbpedido.setNumeroPedido(cur.getString(0));
            dbpedido.setIdCliente(cur.getString(1));
            dbpedido.setIdVendedor(cur.getString(2));
            dbpedido.setFechaPedido(cur.getString(3));
            dbpedido.setFechaEntrega(cur.getString(4));
            dbpedido.setIdFormaPago(cur.getString(5));
            dbpedido.setObservacion(cur.getString(6));
            dbpedido.setPesoTotal(cur.getDouble(7));
            dbpedido.setImporteTotal(cur.getDouble(8));

            if (cur.getString(9) == null){
                dbpedido.setIdMotivoNoVenta("0");
            }else{
                dbpedido.setIdMotivoNoVenta(cur.getString(9));
            }

            dbpedido.setEstado(cur.getString(10));
            dbpedido.setFlag(cur.getString(11));

            dbpedido.setLatitud(cur.getDouble(14));
            dbpedido.setLongitud(cur.getDouble(15));

            if (cur.getString(16) == null && cur.getString(17) == null){
                dbpedido.setLatitudDocumento(0.0);
                dbpedido.setLongitudDocumento(0.0);
            }else{
                dbpedido.setLatitudDocumento(cur.getDouble(16));
                dbpedido.setLongitudDocumento(cur.getDouble(17));
            }
            dbpedido.setPorcentajeBateria(cur.getInt(18));
            dbpedido.setHoraFin(cur.getString(19));
            dbpedido.setHoraModificacion(cur.getString(20));

            if (cur.getString(21) == null)
                dbpedido.setPedidoEntregado(0);
            else
                dbpedido.setPedidoEntregado(cur.getInt(21));

            if (cur.getString(22) == null)
                dbpedido.setFechaEntregado("");
            else
                dbpedido.setFechaEntregado(cur.getString(22));

            lista_pedidos.add(dbpedido);
            cur.moveToNext();
        }
        cur.close();

        for (int i = 0; i < lista_pedidos.size(); i++) {
            // Seteo del detalle del pedido por el oc_numero
            ArrayList<DTOPedidoDetalle> detalles = getDTOPedidoDetalle(lista_pedidos.get(i).getNumeroPedido());
            lista_pedidos.get(i).setDetalles(detalles);
        }

        return lista_pedidos;
    }

    public boolean tienePedido(String idCliente) {
        boolean existe = false;
        String rawQuery;
        rawQuery = "SELECT numeroPedido FROM "+TablesHelper.PedidoCabecera.Table+" WHERE "+TablesHelper.PedidoCabecera.FKCliente+" = '"+idCliente+"'";
        SQLiteDatabase db = dataBaseHelper.getReadableDatabase();
        Cursor cur = db.rawQuery(rawQuery, null);

        cur.moveToFirst();
        while (!cur.isAfterLast()) {
            existe = true;
            cur.moveToNext();
        }
        cur.close();
        return existe;
    }

    public String actualizarEstadoPedido (String numeroPedido, String estado){
        String flag = "";
        try {
            ContentValues updateValues = new ContentValues();
            updateValues.put(TablesHelper.PedidoCabecera.Estado, estado);
            String[] args = { numeroPedido };

            SQLiteDatabase db = dataBaseHelper.getWritableDatabase();
            String where = TablesHelper.PedidoCabecera.PKeyName + " = ?";

            Log.i(TAG, "actualizarEstado:"+numeroPedido+" estado:"+estado);
            db.update(TablesHelper.PedidoCabecera.Table, updateValues, where, args );
        } catch (Exception e) {
            Log.e(TAG, "actualizarEstado "+numeroPedido+" estado:"+estado+" Error al modificar registro");
            e.printStackTrace();
        }
        return flag;
    }

    public String actualizarEstadoPedido (String numeroPedido, String estado, String idMotivoNoVenta){
        String flag = "";
        try {
            ContentValues updateValues = new ContentValues();
            updateValues.put(TablesHelper.PedidoCabecera.Estado, estado);
            updateValues.put(TablesHelper.PedidoCabecera.FKMotivoNoVenta, idMotivoNoVenta);
            String[] args = { numeroPedido };

            SQLiteDatabase db = dataBaseHelper.getWritableDatabase();
            String where = TablesHelper.PedidoCabecera.PKeyName + " = ?";

            Log.i(TAG, "actualizarEstado:"+numeroPedido+" estado:"+estado);
            db.update(TablesHelper.PedidoCabecera.Table, updateValues, where, args );
        } catch (Exception e) {
            Log.e(TAG, "actualizarEstado "+numeroPedido+" estado:"+estado+" Error al modificar registro");
            e.printStackTrace();
        }
        return flag;
    }

    public void actualizarFlagSinStock (String numeroPedido, String idProducto, int flagSinStock) {
        String where = TablesHelper.PedidoDetalle.PKeyPedido + " = ? AND "+TablesHelper.PedidoDetalle.PKeyProducto + " = ? ";
        String[] args = { numeroPedido, idProducto };

        try {
            SQLiteDatabase db = dataBaseHelper.getWritableDatabase();

            ContentValues reg = new ContentValues();
            reg.put(TablesHelper.PedidoDetalle.SinStock,flagSinStock);
            db.update(TablesHelper.PedidoDetalle.Table, reg, where, args);

            Log.i(TAG,"actualizarFlagSinStock: actualizado"+numeroPedido+" - "+idProducto);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public void actualizarFlagPedido (String numeroPedido, String flag){
        try {
            SQLiteDatabase db = dataBaseHelper.getWritableDatabase();
            String where = TablesHelper.PedidoCabecera.PKeyName + " = ?";
            ContentValues updateValues = new ContentValues();
            updateValues.put(TablesHelper.PedidoCabecera.Flag, flag);
            String[] args = { numeroPedido };

            Log.i(TAG, "actualizarFlagPedido:"+numeroPedido+":"+flag);
            db.update(TablesHelper.PedidoCabecera.Table, updateValues, where, args );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String actualizarPedidoFacturado (String cadenaRespuesta){
        String estado = "";
        try {
            JSONArray jsonArray = new JSONArray(cadenaRespuesta);

            SQLiteDatabase db = dataBaseHelper.getWritableDatabase();
            String where = TablesHelper.PedidoCabecera.PKeyName + " = ?";


            for (int i = 0; i < jsonArray.length(); i++){
                JSONObject jsonData = jsonArray.getJSONObject(i);

                String numeroPedido = jsonData.getString(TablesHelper.PedidoCabecera.PKeyName).trim();
                estado = jsonData.getString(TablesHelper.PedidoCabecera.Estado).trim();

                if (estado.equals(PedidoCabeceraModel.ESTADO_FACTURADO)){
                    ContentValues updateValues = new ContentValues();
                    updateValues.put(TablesHelper.PedidoCabecera.Estado, estado);
                    updateValues.put(TablesHelper.PedidoCabecera.SerieDocumento, jsonData.getString(TablesHelper.PedidoCabecera.SerieDocumento).trim());
                    updateValues.put(TablesHelper.PedidoCabecera.NumeroDocumento, jsonData.getString(TablesHelper.PedidoCabecera.NumeroDocumento).trim());
                    String[] args = { numeroPedido };

                    Log.i(TAG, "Actualizar "+TablesHelper.PedidoCabecera.Table+": Modificando..."+numeroPedido);
                    db.update(TablesHelper.PedidoCabecera.Table, updateValues, where, args );
                }else{//Si ocurrió algún error, se mantene el error descrito en el campo estado, y se actualiza el estado del pedido a Generado ya que no ha sido facturado
                    ContentValues updateValues = new ContentValues();
                    updateValues.put(TablesHelper.PedidoCabecera.Estado, PedidoCabeceraModel.ESTADO_GENERADO);
                    String[] args = { numeroPedido };

                    Log.i(TAG, "Actualizar "+TablesHelper.PedidoCabecera.Table+": Modificando..."+numeroPedido);
                    db.update(TablesHelper.PedidoCabecera.Table, updateValues, where, args );

                    /*Si el pedido que se mandó a facturar no retorna con el estado FACTURADO, entonces no se le generó el documento, por tanto no debería mantener la percepcion que se ñe generó con anterioridad*/
                    quitarPercepcion(numeroPedido);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Modificar "+TablesHelper.PedidoCabecera.Table+": Error al modificar registro");
            e.printStackTrace();
            estado = "error";
        }
        return estado;
    }

    public int actualizarPedidoGenerado(String numeroPedido) {
        int actualizado = 0;
        try{
            SQLiteDatabase db = dataBaseHelper.getWritableDatabase();
            String where = TablesHelper.PedidoCabecera.PKeyName + " = ?";

            ContentValues updateValues = new ContentValues();
            updateValues.put(TablesHelper.PedidoCabecera.Estado, PedidoCabeceraModel.ESTADO_GENERADO);
            updateValues.put(TablesHelper.PedidoCabecera.SerieDocumento, "");
            updateValues.put(TablesHelper.PedidoCabecera.NumeroDocumento, "");
            updateValues.put(TablesHelper.PedidoCabecera.Flag, PedidoCabeceraModel.FLAG_PENDIENTE);
            String[] args = { numeroPedido };

            Log.i(TAG, "Actualizar "+TablesHelper.PedidoCabecera.Table+": Modificando..."+numeroPedido);
            actualizado = db.update(TablesHelper.PedidoCabecera.Table, updateValues, where, args );
        }catch (Exception e){
            e.printStackTrace();
        }
        return actualizado;
    }

    public String actualizarLatLongPedido (String numeroPedido, double latitud, double longitud){
        String flag = "";
        try {
            ContentValues updateValues = new ContentValues();
            updateValues.put(TablesHelper.PedidoCabecera.Latitud, latitud);
            updateValues.put(TablesHelper.PedidoCabecera.Longitud, longitud);
            String[] args = { numeroPedido };

            SQLiteDatabase db = dataBaseHelper.getWritableDatabase();
            String where = TablesHelper.PedidoCabecera.PKeyName + " = ?";

            Log.i(TAG, "actualizarLatLongHoraPedido:"+numeroPedido+" "+latitud+","+longitud);
            db.update(TablesHelper.PedidoCabecera.Table, updateValues, where, args );
        } catch (Exception e) {
            Log.i(TAG, "actualizarLatLongHoraPedido:"+numeroPedido+" "+latitud+","+longitud+" Error al actualizar");
            e.printStackTrace();
        }
        return flag;
    }

    public String actualizarLatitudLongitudDocumento (String numeroPedido, double latitudDocumento, double longitudDocumento){
        String flag = "";
        try {
            ContentValues updateValues = new ContentValues();
            updateValues.put(TablesHelper.PedidoCabecera.LatitudDocumento, latitudDocumento);
            updateValues.put(TablesHelper.PedidoCabecera.LongitudDocumento, longitudDocumento);
            String[] args = { numeroPedido };

            SQLiteDatabase db = dataBaseHelper.getWritableDatabase();
            String where = TablesHelper.PedidoCabecera.PKeyName + " = ?";

            Log.i(TAG, "actualizarLatitudLongitudDocumento:"+numeroPedido+" "+latitudDocumento+","+longitudDocumento);
            db.update(TablesHelper.PedidoCabecera.Table, updateValues, where, args );
        } catch (Exception e) {
            Log.i(TAG, "actualizarLatitudLongitudDocumento:"+numeroPedido+" "+latitudDocumento+","+longitudDocumento+" Error al actualizar");
            e.printStackTrace();
        }
        return flag;
    }

    /**
     *
     * @param numeroPedido
     * @param idPromocion
     * @param itemPromocion item correlativo dentro de cada promocion
     * @param itemPedido item del pedido, indica hasta dónde se verificó cuando se obtuvo la promoción. Ayuda a saber hasta dónde se debe obtener la cantidad de productos y trabajar solo con esos, ya que la promoción se dio en ese momento
     * @return Retorna la cantida de productos que sean de unidad mayor en el pedido, pertenecientes a determinada promocion
     */
    public int getCantidadProductosDePromocion(String numeroPedido, int idPromocion, String itemPromocion, int itemPedido) {
        int cantidad = 0;
        String rawQuery =
                "SELECT SUM("+TablesHelper.PedidoDetalle.Cantidad+") " +
                "FROM "+TablesHelper.PedidoDetalle.Table+" pd WHERE "+TablesHelper.PedidoDetalle.PKeyPedido+" = '"+numeroPedido+"' " +
                "AND "+TablesHelper.PedidoDetalle.PKeyProducto+" in (SELECT "+TablesHelper.PromocionDetalle.Entrada+" FROM "+TablesHelper.PromocionDetalle.Table+" WHERE "+TablesHelper.PromocionDetalle.PKeyName+"="+idPromocion+" AND "+TablesHelper.PromocionDetalle.Item+" = "+itemPromocion+") " +
                "AND "+TablesHelper.PedidoDetalle.TipoProducto+" = '"+ProductoModel.TIPO_VENTA+"' " +
                //"AND "+TablesHelper.PedidoDetalle.FKUnidadMedida+" = (SELECT "+TablesHelper.Producto.FKUnidadMayor+" FROM "+TablesHelper.Producto.Table+" WHERE "+TablesHelper.Producto.PKeyName+" = pd."+TablesHelper.PedidoDetalle.PKeyProducto+") "+
                "AND pd."+TablesHelper.PedidoDetalle.Item+" <= "+itemPedido;

        Log.d(TAG,"getCantidadProductosDePromocion\n"+rawQuery);
        SQLiteDatabase db = dataBaseHelper.getReadableDatabase();

        Cursor cur = db.rawQuery(rawQuery, null);
        cur.moveToFirst();

        if (cur.moveToFirst()) {
            do {
                cantidad = cur.getInt(0);
            } while (cur.moveToNext());

        }
        cur.close();
        return cantidad;
    }


    public double getPrecioNetoProductoPedido(String numeroPedido, String idProducto) {
        double precioNeto = 0;
        String rawQuery =
                "SELECT precioNeto "
                + "FROM "+TablesHelper.PedidoDetalle.Table+" "
                + "WHERE numeroPedido ='" + numeroPedido + "' AND idProducto ='"+idProducto+"'";

        SQLiteDatabase db = dataBaseHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(rawQuery, null);

        if (cursor.moveToFirst()) {
            do {
                precioNeto = cursor.getDouble(0);
            } while (cursor.moveToNext());
        }
        cursor.close();

        return precioNeto;
    }

    public String actualizarHoraFinPedido (String numeroPedido, String horaFin){
        String flag = "";
        try {
            ContentValues updateValues = new ContentValues();
            updateValues.put(TablesHelper.PedidoCabecera.HoraFin, horaFin);
            String[] args = { numeroPedido };

            SQLiteDatabase db = dataBaseHelper.getWritableDatabase();
            String where = TablesHelper.PedidoCabecera.PKeyName + " = ?";

            Log.i(TAG, "actualizarHoraFinPedido:"+numeroPedido+" "+horaFin);
            db.update(TablesHelper.PedidoCabecera.Table, updateValues, where, args );
        } catch (Exception e) {
            Log.e(TAG, "actualizarHoraFinPedido: "+numeroPedido+" "+horaFin+" error al actualizar");
            e.printStackTrace();
        }
        return flag;
    }

    public String actualizarFechaModificacionPedido (String numeroPedido, String fechaModificacion){
        String flag = "";
        try {
            ContentValues updateValues = new ContentValues();
            updateValues.put(TablesHelper.PedidoCabecera.FechaModificacion, fechaModificacion);
            String[] args = { numeroPedido };

            SQLiteDatabase db = dataBaseHelper.getWritableDatabase();
            String where = TablesHelper.PedidoCabecera.PKeyName + " = ?";

            Log.i(TAG, "actualizarHoraModificacionPedido:"+numeroPedido+" "+fechaModificacion);
            db.update(TablesHelper.PedidoCabecera.Table, updateValues, where, args );
        } catch (Exception e) {
            Log.e(TAG, "actualizarHoraModificacionPedido: "+numeroPedido+" "+fechaModificacion+" error al actualizar");
            e.printStackTrace();
        }
        return flag;
    }

    public String getFechaPedido(String numeroPedido) {
        String fecha = "";
        String rawQuery =
                "SELECT "+TablesHelper.PedidoCabecera.FechaPedido+" "
                        + "FROM "+TablesHelper.PedidoCabecera.Table+" "
                        + "WHERE numeroPedido ='" + numeroPedido +"'";

        SQLiteDatabase db = dataBaseHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(rawQuery, null);
        Log.e(TAG,rawQuery);
        if (cursor.moveToFirst()) {
            do {
                fecha = cursor.getString(0);
            } while (cursor.moveToNext());
        }
        cursor.close();

        return fecha;
    }

    /**
     * @param numeroPedido
     * @return Retorna la cantidad de paquetes vendidos, solo tomando en cuenta los productos tipo VENTA, mas no los de BONIFICACION, PUBLICIDAD o SERVICIO
     */
    public int getCantidadPaquetes(String numeroPedido) {
        int cantidad = 0;
        String rawQuery =
                "SELECT SUM(cantidad) " +
                        "FROM "+TablesHelper.PedidoDetalle.Table+" " +
                        "WHERE numeroPedido='"+numeroPedido+"' and idUnidadMedida = (SELECT idUnidadMedida FROM Producto where idProducto="+TablesHelper.PedidoDetalle.Table+".idProducto) AND tipoProducto = '"+ProductoModel.TIPO_VENTA+"'";

        SQLiteDatabase db = dataBaseHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(rawQuery, null);
        Log.e(TAG,rawQuery);
        if (cursor.moveToFirst()) {
            do {
                cantidad = cursor.getInt(0);
            } while (cursor.moveToNext());
        }
        cursor.close();

        return cantidad;
    }

    public void actualizarPoliticaPreciosProductoPedido(String numeroPedido, String idProducto, String tipoProducto, String idPoliticaPrecio, double precioBruto, double precioNeto) {
        String where = TablesHelper.PedidoDetalle.PKeyPedido + " = ? AND "+TablesHelper.PedidoDetalle.PKeyProducto + " = ? AND "+TablesHelper.PedidoDetalle.TipoProducto+ " = ?";
        String[] args = { numeroPedido, idProducto, tipoProducto };

        try {
            SQLiteDatabase db = dataBaseHelper.getWritableDatabase();

            ContentValues Nreg = new ContentValues();
            Nreg.put(TablesHelper.PedidoDetalle.FKPoliticaPrecio,   idPoliticaPrecio);
            Nreg.put(TablesHelper.PedidoDetalle.PrecioBruto,        precioBruto);
            Nreg.put(TablesHelper.PedidoDetalle.PrecioNeto,         precioNeto);

            db.update(TablesHelper.PedidoDetalle.Table, Nreg, where, args);
            Log.i(TAG, "actualizarPoliticaPreciosProductoPedido: Registro actualizado");
        } catch (Exception e) {
            Log.e(TAG, "actualizarPoliticaPreciosProductoPedido: Error al actualizar registro");
            e.printStackTrace();
        }
    }

    public void actualizarPedidoEntregado (String numeroPedido, boolean check, String fechaEntregado){
        int entregado = 0;
        if (check)
            entregado = 1;
        try {
            ContentValues updateValues = new ContentValues();
            updateValues.put(TablesHelper.PedidoCabecera.PedidoEntregado, entregado);
            if (!fechaEntregado.equals(""))
                updateValues.put(TablesHelper.PedidoCabecera.FechaEntregado, fechaEntregado);
            String[] args = { numeroPedido };

            SQLiteDatabase db = dataBaseHelper.getWritableDatabase();
            String where = TablesHelper.PedidoCabecera.PKeyName + " = ?";

            Log.i(TAG, "actualizarPedidoEntregado:"+numeroPedido+" "+entregado);
            db.update(TablesHelper.PedidoCabecera.Table, updateValues, where, args );
        } catch (Exception e) {
            Log.e(TAG, "actualizarPedidoEntregado: "+numeroPedido+" "+entregado+" error al actualizar");
            e.printStackTrace();
        }
    }

    public ArrayList<HashMap<String, Object>> getPedidosEntregados(String numeroPedido) {
        String rawQuery = "SELECT numeroPedido,idVendedor,pedidoEntregado,fechaEntregado FROM " + TablesHelper.PedidoCabecera.Table + " WHERE " + TablesHelper.PedidoCabecera.PKeyName + " = ?";
        String[] args = { numeroPedido };
        SQLiteDatabase db = dataBaseHelper.getReadableDatabase();
        Cursor cur = db.rawQuery(rawQuery, args);

        ArrayList<HashMap<String, Object>> lista = new ArrayList<>();
        cur.moveToFirst();

        Ventas360App ventas360App =  (Ventas360App) context.getApplicationContext();
        String idAlmacen = ventas360App.getIdAlmacen();

        while (!cur.isAfterLast()) {
            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("numeroPedido",cur.getString(0));
            hashMap.put("idVendedor",cur.getString(1));
            hashMap.put("idAlmacen",idAlmacen);
            hashMap.put("pedidoEntregado",cur.getString(2));
            hashMap.put("fechaEntregado",cur.getString(3));

            lista.add(hashMap);
            cur.moveToNext();
        }
        cur.close();

        return lista;
    }

    public String obtenerFlagEnvio (String cadenaRespuesta){
        String flag = "";
        try {
            JSONArray jsonArray = new JSONArray(cadenaRespuesta);
            for (int i = 0; i < jsonArray.length(); i++){
                JSONObject jsonData = jsonArray.getJSONObject(i);
                flag = jsonData.getString(TablesHelper.PedidoCabecera.Flag).trim();
            }
        } catch (Exception e) {
            e.printStackTrace();
            flag = "error";
        }
        return flag;
    }

    /**
     * Si la unidade de medida del producto es el mismo tanto para unidad mayor como para unidad menor, entonces el producto no se toma en cuenta, ya que solo tiene una unidad de medida (no tiene factor de conversion y unidad mas grande)
     * @return
     */

    public int getCantidadPaquetesTotal() {
        int cantidad = 0;
        /*String rawQuery =
                "SELECT SUM(cantidad) " +
                "FROM "+TablesHelper.PedidoDetalle.Table+" pd " +
                "INNER JOIN "+TablesHelper.PedidoCabecera.Table+" pc ON pd.numeroPedido = pc.numeroPedido " +
                "INNER JOIN "+TablesHelper.Producto.Table+" p ON pd.idProducto = p.idProducto " +
                "WHERE pc.estado <> 'A' AND pd.tipoProducto = '"+ProductoModel.TIPO_VENTA+"' AND pd.idUnidadMedida=p.idUnidadMayor AND p.idUnidadMenor<>p.idUnidadMayor";*/
        String rawQuery =
                "SELECT SUM(cantidad) " +
                        "FROM "+TablesHelper.PedidoDetalle.Table+" pd " +
                        "INNER JOIN "+TablesHelper.PedidoCabecera.Table+" pc ON pd.numeroPedido = pc.numeroPedido " +
                        "INNER JOIN "+TablesHelper.Producto.Table+" p ON pd.idProducto = p.idProducto " +
                        "INNER JOIN "+TablesHelper.UnidadMedidaxProducto.Table+" ump ON ump.idProducto = p.idProducto " +
                        "WHERE pc.estado <> 'A' AND pd.tipoProducto = '"+ProductoModel.TIPO_VENTA+"' AND pd.idUnidadMedida=ump.idUnidadManejo";

        SQLiteDatabase db = dataBaseHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(rawQuery, null);
        Log.e(TAG,rawQuery);
        if (cursor.moveToFirst()) {
            do {
                cantidad = cursor.getInt(0);
            } while (cursor.moveToNext());
        }
        cursor.close();

        return cantidad;
    }

    /**
     * Se toma en cuenta todos los productos que tengan la unidad de media menor igual a la idUnidadMedida del PedidoDetalle
     * @return
     */
    public int getCantidadUnidadesTotal() {
        int cantidad = 0;
        /*String rawQuery =
                "SELECT SUM(cantidad) " +
                "FROM "+TablesHelper.PedidoDetalle.Table+" pd " +
                "INNER JOIN "+TablesHelper.PedidoCabecera.Table+" pc ON pd.numeroPedido = pc.numeroPedido " +
                "INNER JOIN "+TablesHelper.Producto.Table+" p ON pd.idProducto = p.idProducto " +
                "WHERE pc.estado <> 'A' AND pd.tipoProducto = '"+ProductoModel.TIPO_VENTA+"' AND pd.idUnidadMedida=p.idUnidadMenor";*/
        String rawQuery =
                "SELECT SUM(cantidad) " +
                        "FROM "+TablesHelper.PedidoDetalle.Table+" pd " +
                        "INNER JOIN "+TablesHelper.PedidoCabecera.Table+" pc ON pd.numeroPedido = pc.numeroPedido " +
                        "INNER JOIN "+TablesHelper.Producto.Table+" p ON pd.idProducto = p.idProducto " +
                        "INNER JOIN "+TablesHelper.UnidadMedidaxProducto.Table+" ump ON ump.idProducto = p.idProducto " +
                        "WHERE pc.estado <> 'A' AND pd.tipoProducto = '"+ProductoModel.TIPO_VENTA+"' AND pd.idUnidadMedida=ump.idUnidadContable";

        SQLiteDatabase db = dataBaseHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(rawQuery, null);
        //Log.e(TAG,rawQuery);
        if (cursor.moveToFirst()) {
            do {
                cantidad = cursor.getInt(0);
            } while (cursor.moveToNext());
        }
        cursor.close();

        return cantidad;
    }

    public void actualizarFormaPago (String numeroPedido, String idFormaPago){
        try {
            SQLiteDatabase db = dataBaseHelper.getWritableDatabase();
            String where = TablesHelper.PedidoCabecera.PKeyName + " = ?";
            ContentValues updateValues = new ContentValues();
            updateValues.put(TablesHelper.PedidoCabecera.FKFormaPago, idFormaPago);
            String[] args = { numeroPedido };

            Log.i(TAG, "actualizarFormaPago:"+numeroPedido+":"+idFormaPago);
            db.update(TablesHelper.PedidoCabecera.Table, updateValues, where, args );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public double getImporteTotalVentas(){
        double importe = 0;
        String rawQuery =
                "SELECT SUM("+TablesHelper.PedidoCabecera.ImporteTotal+") "+
                "FROM "+ TablesHelper.PedidoCabecera.Table +" pc " +
                "INNER JOIN "+ TablesHelper.Cliente.Table +" c ON pc.idCliente = c.idCliente " +
                "LEFT JOIN "+ TablesHelper.FormaPago.Table +" fp ON fp.idFormaPago = pc.idFormaPago " +
                "LEFT JOIN "+ TablesHelper.MotivoNoVenta.Table +" mnv ON mnv.idMotivoNoVenta = pc.idMotivoNoVenta " +
                "WHERE pc.estado <> '"+PedidoCabeceraModel.ESTADO_ANULADO+"'";

        SQLiteDatabase db = dataBaseHelper.getReadableDatabase();
        Cursor cur = db.rawQuery(rawQuery, null);
        cur.moveToFirst();

        while (!cur.isAfterLast()) {
            importe = cur.getDouble(0);
            cur.moveToNext();
        }
        cur.close();
        return importe;
    }

    public ArrayList<HashMap<String,Object>> getResumenVentaMarca(){
        ArrayList<HashMap<String,Object>> lista = new ArrayList<>();
        String rawQuery =
                "SELECT " +
                "m.idMarca " +
                ",m.descripcion " +
                ",SUM(CASE WHEN pd.precioBruto=ppp.precioManejo THEN pd.cantidad ELSE 0 END)" +
                ",SUM(CASE WHEN pd.precioBruto=ppp.precioContenido THEN pd.cantidad ELSE 0 END) " +
                ",IFNULL(SUM(pd.precioNeto),0) " +
                ",COUNT(DISTINCT pc.idCliente) " +
                "FROM Marca m " +
                "INNER JOIN Producto p ON m.idMarca=p.idMarca " +
                "INNER JOIN PedidoDetalle pd ON p.idProducto=pd.idProducto " +
                "INNER JOIN PedidoCabecera pc ON pd.numeroPedido=pc.numeroPedido " +
                "INNER JOIN PoliticaPrecioxProducto ppp ON p.idProducto=ppp.idProducto AND pd.idUnidadMedida=ppp.idUnidadManejo " +
                "WHERE pc.estado<>'A' AND pd.tipoProducto='"+ProductoModel.TIPO_VENTA+"' " +
                "GROUP BY m.idMarca,m.descripcion " +

                "UNION " +

                "SELECT " +
                "IFNULL(m.idMarca,'0') " +
                ",IFNULL(m.descripcion,'OTROS') " +
                ",(SELECT IFNULL(SUM(CASE WHEN b.precioBruto=ppp.precioManejo AND ppp.precioManejo<>ppp.precioContenido THEN b.cantidad ELSE 0 END),0 ) " +
                    "FROM PedidoCabecera a " +
                    "INNER JOIN PedidoDetalle b ON a.numeroPedido=b.numeroPedido " +
                    "INNER JOIN Producto c ON b.idProducto=c.idProducto " +
                    "INNER JOIN PoliticaPrecioxProducto ppp ON p.idProducto=ppp.idProducto AND b.idUnidadMedida=ppp.idUnidadManejo " +
                    "LEFT JOIN Marca d ON c.idMarca=d.idMarca " +
                    "WHERE b.tipoProducto='"+ProductoModel.TIPO_VENTA+"' AND d.idMarca IS NULL ) " +
                ",(SELECT IFNULL(SUM(CASE WHEN b.idUnidadMedida=ppp.precioContenido THEN b.cantidad ELSE 0 END),0 ) " +
                    "FROM PedidoCabecera a " +
                    "INNER JOIN PedidoDetalle b ON a.numeroPedido=b.numeroPedido " +
                    "INNER JOIN Producto c ON b.idProducto=c.idProducto " +
                    "INNER JOIN PoliticaPrecioxProducto ppp ON p.idProducto=ppp.idProducto AND b.idUnidadMedida=ppp.idUnidadManejo " +
                    "LEFT JOIN Marca d ON c.idMarca=d.idMarca " +
                    "WHERE b.tipoProducto='"+ProductoModel.TIPO_VENTA+"' AND d.idMarca IS NULL ) " +
                ",(SELECT IFNULL(SUM(b.precioNeto),0 ) " +
                    "FROM PedidoCabecera a  " +
                    "INNER JOIN PedidoDetalle b ON a.numeroPedido=b.numeroPedido " +
                    "INNER JOIN Producto c ON b.idProducto=c.idProducto " +
                    "LEFT JOIN Marca d ON c.idMarca=d.idMarca " +
                    "WHERE b.tipoProducto='"+ProductoModel.TIPO_VENTA+"' AND d.idMarca IS NULL ) " +
                ",(SELECT COUNT(DISTINCT(idCliente)) " +
                    "FROM PedidoCabecera a " +
                    "INNER JOIN PedidoDetalle b ON a.numeroPedido=b.numeroPedido " +
                    "INNER JOIN Producto c ON b.idProducto=c.idProducto " +
                    "LEFT JOIN Marca d ON c.idMarca=d.idMarca " +
                    "WHERE b.tipoProducto='"+ProductoModel.TIPO_VENTA+"' AND d.idMarca IS NULL ) " +
                "FROM Producto p " +
                "LEFT JOIN Marca m ON p.idMarca=m.idMarca " +
                "WHERE m.idMarca IS NULL " +
                "GROUP BY m.idMarca,m.descripcion";

        SQLiteDatabase db = dataBaseHelper.getReadableDatabase();
        Cursor cur = db.rawQuery(rawQuery, null);
        cur.moveToFirst();

        while (!cur.isAfterLast()) {
            if (cur.getInt(2) > 0 || cur.getInt(3) > 0){
                HashMap<String, Object> item = new HashMap<>();
                item.put("idMarca",cur.getString(0));
                item.put("marca",cur.getString(1));
                item.put("unidadMayor",cur.getInt(2));
                item.put("unidadMenor",cur.getInt(3));
                item.put("importe",cur.getDouble(4));
                item.put("numeroClientes",cur.getInt(5));
                lista.add(item);
            }
            cur.moveToNext();
        }
        cur.close();
        return lista;

    }

    public ArrayList<HashMap<String,Object>> getResumenVentaSegmento(String modoVenta, String estadoVendedor){
        ArrayList<HashMap<String,Object>> lista = new ArrayList<>();
        String whereEstado = "estado <> '"+PedidoCabeceraModel.ESTADO_ANULADO+"'";
        if (modoVenta.equals(VendedorModel.MODO_AUTOVENTA) && estadoVendedor.equals(VendedorModel.ESTADO_DESPACHO)){
            whereEstado = "estado = '"+PedidoCabeceraModel.ESTADO_FACTURADO+"'";
        }
        String rawQuery =
                "SELECT " +
                "s.idSegmento" +
                ",s.descripcion" +
                ",(SELECT COUNT(DISTINCT idCliente) FROM Cliente WHERE idSegmento=s.idSegmento)" +
                ",(SELECT COUNT(DISTINCT b.idCliente) FROM PedidoCabecera a INNER JOIN Cliente b ON a.idCliente=b.idCliente WHERE a."+whereEstado+" AND b.idSegmento=s.idSegmento)" +
                ",IFNULL(SUM(CASE WHEN pd.precioBruto=ppp.precioManejo AND ppp.precioContenido<>ppp.precioManejo THEN pd.cantidad ELSE 0 END), 0)" +
                ",IFNULL(SUM(pd.precioNeto), 0) " +
                "FROM Segmento s " +
                "INNER JOIN Cliente c ON s.idSegmento=c.idSegmento " +
                "INNER JOIN PedidoCabecera pc ON c.idCliente=pc.idCliente " +
                "INNER JOIN PedidoDetalle pd ON pc.numeroPedido=pd.numeroPedido " +
                "INNER JOIN Producto p ON pd.idProducto=p.idProducto " +
                "INNER JOIN PoliticaPrecioxProducto ppp ON p.idProducto=ppp.idProducto AND pd.idUnidadMedida=ppp.idUnidadManejo " +
                "WHERE pc.estado<>'A' AND pd.tipoProducto='"+ProductoModel.TIPO_VENTA+"' " +
                "GROUP BY s.idSegmento,s.descripcion " +

                "UNION " +

                "SELECT " +
                "ifnull(s.idSegmento,'0')" +
                ",ifnull(s.descripcion,'OTROS')" +
                ",(SELECT COUNT(DISTINCT idCliente) FROM Cliente a LEFT JOIN Segmento b ON a.idSegmento=b.idSegmento WHERE b.idSegmento is NULL)" +
                ",(SELECT COUNT(DISTINCT b.idCliente) FROM PedidoCabecera a INNER JOIN Cliente b ON a.idCliente=b.idCliente LEFT JOIN Segmento c ON b.idSegmento=c.idSegmento WHERE a."+whereEstado+" AND c.idSegmento is NULL) " +
                ",(SELECT IFNULL(SUM(CASE WHEN b.precioBruto=ppp.precioManejo AND ppp.precioContenido<>ppp.precioManejo THEN b.cantidad ELSE 0 END), 0) " +
                    "FROM PedidoCabecera a " +
                    "INNER JOIN PedidoDetalle b ON a.numeroPedido=b.numeroPedido " +
                    "INNER JOIN Producto c ON b.idProducto=c.idProducto " +
                    "INNER JOIN Cliente d ON a.idCliente=d.idCliente " +
                    "INNER JOIN PoliticaPrecioxProducto ppp ON c.idProducto=ppp.idProducto AND b.idUnidadMedida=ppp.idUnidadManejo " +
                    "LEFT JOIN Segmento e ON d.idSegmento=e.idSegmento " +
                    "WHERE a.estado<>'A' AND b.tipoProducto='"+ProductoModel.TIPO_VENTA+"' AND e.idSegmento is null) " +
                ",(SELECT IFNULL(SUM(a.importeTotal),0) FROM PedidoCabecera a INNER JOIN Cliente b ON a.idCliente=b.idCliente LEFT JOIN Segmento c ON b.idSegmento=c.idSegmento WHERE a.estado<>'A' AND c.idSegmento is null) " +
                "FROM Cliente c " +
                "LEFT JOIN Segmento s ON c.idSegmento=s.idSegmento " +
                "WHERE s.idSegmento IS NULL " +
                "GROUP BY s.idSegmento,s.descripcion";
        SQLiteDatabase db = dataBaseHelper.getReadableDatabase();
        Cursor cur = db.rawQuery(rawQuery, null);
        cur.moveToFirst();

        while (!cur.isAfterLast()) {
            if (cur.getInt(2) > 0){
                HashMap<String, Object> item = new HashMap<>();
                item.put("idSegmento",cur.getString(0));
                item.put("segmento",cur.getString(1));
                item.put("programados",cur.getInt(2));
                item.put("efectivos",cur.getInt(3));
                item.put("unidadMayor",cur.getInt(4));
                item.put("importe",cur.getDouble(5));
                lista.add(item);
            }
            cur.moveToNext();
        }
        cur.close();
        return lista;

    }

    public void generarPercepcion(String numeroPedido, String rucDni, double importePedido, double limiteParaPercepcion){
        DAOProducto daoProducto = new DAOProducto(context);
        if (rucDni.length() == 11 || importePedido >= limiteParaPercepcion){
            /*La percepción solo se aplica a los clientes que tengan ruc(FACTURA) o a las boletas que sean igual o mayor al limitePercepcion(100)*/
            ArrayList<PedidoDetalleModel> listaProductoPedido = getListaProductoPedido(numeroPedido);
            for (PedidoDetalleModel detalleModel : listaProductoPedido){

                double percepcion = 0.0d;

                if (detalleModel.getTipoProducto().equals(ProductoModel.TIPO_VENTA) || detalleModel.getTipoProducto().equals(ProductoModel.TIPO_SERVICIO)){
                    //La percepción se aplica al precio con IGV
                    double porcentajePercepcion = daoProducto.getPorcentajePercepcion(detalleModel.getIdProducto());
                    percepcion = Util.redondearDouble(detalleModel.getPrecioNeto() * porcentajePercepcion);
                }
                Log.i(TAG,detalleModel.getIdProducto()+" setPercepcion: "+percepcion);
                actualizarPercepcion(numeroPedido, detalleModel.getIdProducto(), percepcion);
            }
        }
    }

    public void quitarPercepcion(String numeroPedido){
        ArrayList<PedidoDetalleModel> listaProductoPedido = getListaProductoPedido(numeroPedido);
        for (PedidoDetalleModel detalleModel : listaProductoPedido){
            if (detalleModel.getTipoProducto().equals(ProductoModel.TIPO_VENTA) || detalleModel.getTipoProducto().equals(ProductoModel.TIPO_SERVICIO)){
                actualizarPercepcion(numeroPedido, detalleModel.getIdProducto(), 0.0);
            }
        }
    }

    public void actualizarPercepcion (String numeroPedido, String idProducto, double percepcion){
        try {
            SQLiteDatabase db = dataBaseHelper.getWritableDatabase();
            String where = TablesHelper.PedidoDetalle.PKeyPedido + " = ? AND "+TablesHelper.PedidoDetalle.PKeyProducto + " = ? ";
            ContentValues updateValues = new ContentValues();
            updateValues.put(TablesHelper.PedidoDetalle.Percepcion, percepcion);
            String[] args = { numeroPedido, idProducto };

            Log.i(TAG, "actualizarPercepcion:"+idProducto+":"+percepcion);
            db.update(TablesHelper.PedidoDetalle.Table, updateValues, where, args );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public ArrayList<HRVendedorModel> getHojaRutaVendedor(){
        ArrayList<HRVendedorModel> lista = new ArrayList<>();
        String rawQuery =
                "SELECT * " +
                "FROM "+TablesHelper.HRVendedor.table;



        SQLiteDatabase db = dataBaseHelper.getReadableDatabase();
        Cursor cur = db.rawQuery(rawQuery, null);

        cur.moveToFirst();
        while (!cur.isAfterLast()) {
            HRVendedorModel model = new HRVendedorModel();
            model.setEjercicio(cur.getInt(0));
            model.setPeriodo(cur.getInt(1));
            model.setIdVendedor(cur.getString(2));
            model.setCuotaSoles(cur.getDouble(3));
            model.setCuotaPaquetes(cur.getDouble(4));
            model.setVentaSoles(cur.getDouble(5));
            model.setVentaPaquetes(cur.getInt(6));
            model.setDiasLaborados(cur.getInt(7));
            model.setDiasLaborales(cur.getInt(8));
            model.setCoberturaMultiple(cur.getDouble(9));
            model.setHitRate(cur.getDouble(10));
            model.setAvance(cur.getDouble(11));
            model.setNecesidadDiaSoles(cur.getDouble(12));
            model.setNecesidadDiaPaquetes(cur.getDouble(13));

            lista.add(model);
            cur.moveToNext();
        }

        cur.close();
        return lista;
    }

    public ArrayList<HRClienteModel> getHojaRutaCliente(String idCliente){
        ArrayList<HRClienteModel> lista = new ArrayList<>();
        String rawQuery =
                "SELECT * FROM "+TablesHelper.HRCliente.table + " WHERE "+TablesHelper.HRCliente.idCliente + " = '"+ idCliente +"'";


        SQLiteDatabase db = dataBaseHelper.getReadableDatabase();
        Cursor cur = db.rawQuery(rawQuery, null);

        cur.moveToFirst();
        while (!cur.isAfterLast()) {
            HRClienteModel model = new HRClienteModel();
            model.setEjercicio(cur.getInt(0));
            model.setPeriodo(cur.getInt(1));
            model.setIdCliente(cur.getString(2));
            model.setProgramado(cur.getInt(3));
            model.setTranscurrido(cur.getInt(4));
            model.setLiquidado(cur.getInt(5));
            model.setHitRate(cur.getDouble(6));
            model.setCoberturaMultiple(cur.getDouble(7));
            model.setCuotaSoles(cur.getDouble(8));
            model.setCuotaPaquetes(cur.getDouble(9));
            model.setVentaSoles(cur.getDouble(10));
            model.setVentaPaquetes(cur.getDouble(11));
            model.setDiasLaborados(cur.getInt(12));
            model.setDiasLaborales(cur.getInt(13));
            model.setSegmento(cur.getString(14));
            model.setNroExhibidores(cur.getInt(15));
            model.setNroPuertasFrio(cur.getInt(16));
            model.setAvance(cur.getDouble(17));
            model.setNecesidadDiaSoles(cur.getDouble(18));
            model.setNecesidadDiaPaquetes(cur.getDouble(19));

            lista.add(model);
            cur.moveToNext();
        }

        cur.close();
        return lista;
    }

    public ArrayList<HashMap<String,Object>> getClientesxSegmento(String modoVenta, String estadoVendedor, int ejercicio, int periodo){
        ArrayList<HashMap<String,Object>> lista = new ArrayList<>();
        String whereEstado = "pc.estado <> '"+PedidoCabeceraModel.ESTADO_ANULADO+"'";
        if (modoVenta.equals(VendedorModel.MODO_AUTOVENTA) && estadoVendedor.equals(VendedorModel.ESTADO_DESPACHO)){
            whereEstado = "pc.estado = '"+PedidoCabeceraModel.ESTADO_FACTURADO+"'";
        }

        String rawQuery =
                "SELECT ifnull(q.idSegmento,'0'),ifnull(q.descripcion,'OTROS'),COUNT(q.idCliente),COUNT(CASE WHEN q.pedidos>0 THEN q.pedidos ELSE NULL END),SUM(q.nroExhibidores),SUM(q.nroPuertasFrio) "+
                " FROM("+
                    " SELECT ifnull(s.idSegmento,'0') as idSegmento,ifnull(s.descripcion,'OTROS') as descripcion,c.idCliente,COUNT(pc.numeroPedido) as pedidos,hr.nroExhibidores,hr.nroPuertasFrio " +
                    " FROM "+ TablesHelper.Cliente.Table +" c" +
                    " LEFT JOIN "+ TablesHelper.Segmento.Table +" s ON c.idSegmento = s.idSegmento" +
                    " LEFT JOIN "+ TablesHelper.PedidoCabecera.Table +" pc ON c.idCliente = pc.idCliente AND " + whereEstado +
                    " LEFT JOIN "+ TablesHelper.HRCliente.table +" hr ON c.idCliente = hr.idCliente AND hr.ejercicio="+ejercicio+" AND hr.periodo = "+periodo +
                    " GROUP BY c.idCliente )q "+
                " GROUP BY q.idSegmento";

        Log.d(TAG,rawQuery);
        SQLiteDatabase db = dataBaseHelper.getReadableDatabase();
        Cursor cur = db.rawQuery(rawQuery, null);

        cur.moveToFirst();
        while (!cur.isAfterLast()) {
            HashMap<String, Object> item = new HashMap<>();
            item.put("idSegmento",cur.getString(0));
            item.put("segmento",cur.getString(1));
            item.put("programados",cur.getInt(2));
            item.put("efectivos",cur.getInt(3));
            int porcentaje = cur.getInt(3) * 100 / cur.getInt(2);
            item.put("porcentaje", porcentaje);
            item.put("nroExhibidores", cur.getInt(4));
            item.put("nroPuertasFrio", cur.getInt(5));

            lista.add(item);
            cur.moveToNext();
        }
        cur.close();
        return lista;
    }

    public HashMap<String,Object> getResumenVenta(){
        HashMap<String, Object> item = null;
        /*String rawQuery =
                "SELECT SUM(data.importeTotal), SUM(data.cantidad )" +
                " FROM(" +
                " SELECT pc.importeTotal as importeTotal, SUM(CASE pd.idUnidadMedida WHEN p.idUnidadMayor THEN pd.cantidad ELSE 0 END) as cantidad" +
                " FROM PedidoCabecera pc" +
                " INNER JOIN PedidoDetalle pd ON pc.numeroPedido=pd.numeroPedido" +
                " INNER JOIN Producto p ON pd.idProducto = p.idProducto" +
                " WHERE pc.estado<>'"+PedidoCabeceraModel.ESTADO_ANULADO+"'" +
                " GROUP BY pc.numeroPedido) as data";*/
        String rawQuery =
                "SELECT SUM(data.importeTotal), SUM(data.cantidad )" +
                        " FROM(" +
                        " SELECT pc.importeTotal as importeTotal, SUM(pd.cantidad) as cantidad" +
                        " FROM PedidoCabecera pc" +
                        " INNER JOIN PedidoDetalle pd ON pc.numeroPedido=pd.numeroPedido" +
                        " INNER JOIN Producto p ON pd.idProducto = p.idProducto" +
                        " WHERE pc.estado<>'"+PedidoCabeceraModel.ESTADO_ANULADO+"'" +
                        " GROUP BY pc.numeroPedido) as data";

        SQLiteDatabase db = dataBaseHelper.getReadableDatabase();
        Cursor cur = db.rawQuery(rawQuery, null);
        cur.moveToFirst();

        while (!cur.isAfterLast()) {
            item = new HashMap<>();
            item.put("importeTotal",cur.getDouble(0));
            item.put("cantidad",cur.getInt(1));
            cur.moveToNext();
        }
        cur.close();
        return item;
    }

    public ArrayList<HRMarcaResumenModel> getHRMarcaResumen(String idCliente){
        ArrayList<HRMarcaResumenModel> lista = new ArrayList<>();
        String rawQuery =
                "SELECT * FROM "+TablesHelper.HRMarcaResumen.table + " WHERE "+TablesHelper.HRCliente.idCliente + " = '"+ idCliente +"'";


        SQLiteDatabase db = dataBaseHelper.getReadableDatabase();
        Cursor cur = db.rawQuery(rawQuery, null);

        cur.moveToFirst();
        while (!cur.isAfterLast()) {
            HRMarcaResumenModel model = new HRMarcaResumenModel();
            model.setEjercicio(cur.getInt(0));
            model.setPeriodo(cur.getInt(1));
            model.setIdCliente(cur.getString(2));
            model.setCantidad(cur.getInt(3));

            lista.add(model);
            cur.moveToNext();
        }

        cur.close();
        return lista;
    }

    public ArrayList<HashMap<String,Object>> getVentaMarcasDia(){
        ArrayList<HashMap<String,Object>> lista = new ArrayList<>();

        String rawQuery =
                "SELECT m.idMarca,m.descripcion,SUM(pd.cantidad)" +
                " FROM PedidoDetalle pd" +
                " INNER JOIN PedidoCabecera pc ON pd.numeroPedido=pc.numeroPedido" +
                " INNER JOIN Producto p ON pd.idProducto=p.idProducto" +
                " INNER JOIN Marca m ON p.idMarca=m.idMarca" +
                " WHERE pc.estado <> '"+PedidoCabeceraModel.ESTADO_ANULADO+"' AND pd.idUnidadMedida=p.idUnidadMayor" +
                " GROUP BY m.idMarca,m.descripcion" +
                " ORDER BY 3 DESC LIMIT 5";

        SQLiteDatabase db = dataBaseHelper.getReadableDatabase();
        Cursor cur = db.rawQuery(rawQuery, null);
        cur.moveToFirst();

        while (!cur.isAfterLast()) {
            HashMap<String, Object> item = new HashMap<>();
            item.put("idMarca",cur.getString(0));
            item.put("descripcion",cur.getString(1));
            item.put("cantidad",cur.getInt(2));
            lista.add(item);
            cur.moveToNext();
        }

        rawQuery =
                "SELECT '0','OTROS',SUM(pd.cantidad) " +
                " FROM PedidoDetalle pd" +
                " INNER JOIN PedidoCabecera pc ON pd.numeroPedido=pc.numeroPedido" +
                " INNER JOIN Producto p ON pd.idProducto=p.idProducto" +
                " INNER JOIN Marca m ON p.idMarca=m.idMarca" +
                " WHERE pc.estado <> 'A' AND pd.idUnidadMedida=p.idUnidadMayor" +
                " AND m.idMarca NOT IN (SELECT m.idMarca" +
                " FROM PedidoDetalle pd" +
                " INNER JOIN PedidoCabecera pc ON pd.numeroPedido=pc.numeroPedido" +
                " INNER JOIN Producto p ON pd.idProducto=p.idProducto" +
                " INNER JOIN Marca m ON p.idMarca=m.idMarca" +
                " WHERE pc.estado <> 'A' AND pd.idUnidadMedida=p.idUnidadMayor" +
                " GROUP BY m.idMarca,m.descripcion" +
                " ORDER BY SUM(pd.cantidad) DESC LIMIT 5)";

        cur = db.rawQuery(rawQuery, null);
        cur.moveToFirst();

        while (!cur.isAfterLast()) {
            if (cur.getInt(2) > 0){
                HashMap<String, Object> item = new HashMap<>();
                item.put("idMarca",cur.getString(0));
                item.put("descripcion",cur.getString(1));
                item.put("cantidad",cur.getInt(2));
                lista.add(item);
            }
            cur.moveToNext();
        }
        cur.close();
        return lista;
    }

    public void modificarItemDetallePedido2 (PedidoDetalleModel item) {
        String where = TablesHelper.PedidoDetalle.PKeyPedido + " = ? AND "+TablesHelper.PedidoDetalle.PKeyProducto + " = ? AND "+TablesHelper.PedidoDetalle.TipoProducto + " = ?" ;
        String[] args = { item.getNumeroPedido(), item.getIdProducto(), item.getTipoProducto() };

        try {
            SQLiteDatabase db = dataBaseHelper.getWritableDatabase();

            ContentValues reg = new ContentValues();
            reg.put(TablesHelper.PedidoDetalle.Cantidad, item.getCantidad());
            reg.put(TablesHelper.PedidoDetalle.TipoProducto, item.getTipoProducto());
            db.update(TablesHelper.PedidoDetalle.Table, reg, where, args);

            Log.i(TAG,"modificarItemDetallePedido: actualizado"+item.getNumeroPedido()+" - "+item.getIdProducto());

        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public boolean actualizarKardex (String cadenaRespuesta){

        try {
            JSONArray jsonArray = new JSONArray(cadenaRespuesta);

            for (int i = 0; i < jsonArray.length(); i++){
                JSONObject jsonData = jsonArray.getJSONObject(i);
                //String numeroGuia = jsonData.getString(TablesHelper.PedidoCabecera.PKeyName).trim();
                JSONArray jsonArrayDP_Stock = jsonData.getJSONArray("kardexs");
                for (int j = 0; j < jsonArrayDP_Stock.length(); j++){
                    JSONObject jsonKardex = jsonArrayDP_Stock.getJSONObject(j);
                    int stockInicial = jsonKardex.getInt(TablesHelper.Kardex.stockInicial);
                    int stockPedido = jsonKardex.getInt(TablesHelper.Kardex.stockPedido);
                    int stockDespachado = jsonKardex.getInt(TablesHelper.Kardex.stockDespachado); //comentado por que solo esta en preventa en lina
                    String idProducto = jsonKardex.getString("idProducto");
                    String message = jsonKardex.getString("message");
                    boolean enableAddProducto = jsonKardex.getBoolean("enableAddProducto");

                    if(!enableAddProducto){
                        //TODO notificar al usuario
                        continue;
                    }

                    SQLiteDatabase db = dataBaseHelper.getWritableDatabase();

                    ContentValues valor = new ContentValues();
                    valor.put(TablesHelper.Kardex.stockInicial, stockInicial);
                    valor.put(TablesHelper.Kardex.stockPedido, stockPedido);
                    //valor.put(TablesHelper.Kardex.stockDespachado, stockDespachado);

                    String where = TablesHelper.PedidoDetalle.PKeyProducto + " = ? " ;
                    String[] args = { idProducto };
                    db.update(TablesHelper.Kardex.Table, valor, where, args);

                }
                Log.i(TAG,"actualizarKardex: actualizada");
            }
        } catch (Exception e) {
            Log.e(TAG, "Modificar "+TablesHelper.Kardex.Table+": Error al actualizar kardex");
            e.printStackTrace();
            return false;
        }
        return true;
    }

}
