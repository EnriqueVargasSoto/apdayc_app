package com.expediodigital.ventas360.DAO;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.expediodigital.ventas360.Ventas360App;
import com.expediodigital.ventas360.model.DevolucionDetalleModel;
import com.expediodigital.ventas360.model.GuiaModel;
import com.expediodigital.ventas360.model.LiquidacionProductoModel;
import com.expediodigital.ventas360.model.ProductoKardex;
import com.expediodigital.ventas360.model.PoliticaPrecioModel;
import com.expediodigital.ventas360.model.ProductoModel;
import com.expediodigital.ventas360.model.UnidadMedidaModel;
import com.expediodigital.ventas360.model.VendedorModel;
import com.expediodigital.ventas360.util.DataBaseHelper;
import com.expediodigital.ventas360.util.TablesHelper;
import com.expediodigital.ventas360.util.Util;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by Kevin Robinson Meza Hinostroza on julio 2017.
 * Expedio Digital
 * Meza.Hinostroza.Robin@gmail.com
 */

public class DAOProducto {
    public static final String TAG = "DAOProducto";
    DataBaseHelper dataBaseHelper;
    Context context;

    public DAOProducto(Context context) {
        dataBaseHelper = DataBaseHelper.getInstance(context);
        this.context = context;
    }

    public double getPeso(String idProducto) {
        String rawQuery =
                "SELECT ifnull("+ TablesHelper.Producto.Peso+",0) "+
                        " FROM "+ TablesHelper.Producto.Table+
                        " WHERE "+TablesHelper.Producto.PKeyName+" ='"+idProducto+"'";

        double peso = 0.0;

        SQLiteDatabase db = dataBaseHelper.getReadableDatabase();
        Cursor cur = db.rawQuery(rawQuery, null);
        cur.moveToFirst();

        while (!cur.isAfterLast()) {
            peso = cur.getDouble(0);
            cur.moveToNext();
        }
        cur.close();
        return peso;
    }

    public int getFactorConversion(String idProducto) {
        String rawQuery =
                "SELECT ifnull("+TablesHelper.UnidadMedidaxProducto.Contenido+",1) "+
                        " FROM "+ TablesHelper.UnidadMedidaxProducto.Table+
                        " WHERE "+TablesHelper.UnidadMedidaxProducto.FKProducto+" ='"+idProducto+"'";
        //Factor de conversion por lo menos debe ser 1, nunca debe ser cero
        int factor = 1;

        SQLiteDatabase db = dataBaseHelper.getReadableDatabase();
        Cursor cur = db.rawQuery(rawQuery, null);
        cur.moveToFirst();

        while (!cur.isAfterLast()) {
            factor = cur.getInt(0);
            cur.moveToNext();
        }
        cur.close();
        return factor;
    }

    public String getUnidadManejo(String idProducto) {
        String rawQuery =
                "SELECT ifnull("+TablesHelper.UnidadMedidaxProducto.FKUnidadManejo+",1) "+
                        " FROM "+ TablesHelper.UnidadMedidaxProducto.Table+
                        " WHERE "+TablesHelper.UnidadMedidaxProducto.FKProducto+" ='"+idProducto+"'";
        //Factor de conversion por lo menos debe ser 1, nunca debe ser cero
        String um = "";

        SQLiteDatabase db = dataBaseHelper.getReadableDatabase();
        Cursor cur = db.rawQuery(rawQuery, null);
        cur.moveToFirst();

        while (!cur.isAfterLast()) {
            um = cur.getString(0);
            cur.moveToNext();
        }
        cur.close();
        return um;
    }

    public int getStockProducto(String idProducto) {
        String rawQuery =
                "SELECT (stockInicial - stockPedido - stockDespachado) as stock FROM "+TablesHelper.Kardex.Table+" "+
                "WHERE "+TablesHelper.Kardex.FKProducto+" = '"+idProducto+"'";
        int stock = 0;

        SQLiteDatabase db = dataBaseHelper.getReadableDatabase();
        Cursor cur = db.rawQuery(rawQuery, null);
        Util.LogCursorInfo(cur, context);
        cur.moveToFirst();
        try{
            if (cur.moveToFirst()) {
                do {
                    stock = cur.getInt(0);
                } while (cur.moveToNext());
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        cur.close();
        return stock;
    }

    /**
     * @param idProducto
     * @return Retorna las unidades de medida del Producto. <b>Siempre la unidad Mayor debe ir en primer lugar para hacer los cálculos de "unidad de venta (Solo se venden en unidadMayor)" </b>
     */
    public ArrayList<UnidadMedidaModel> getUnidadMedida(String idProducto) {

        String rawQuery =
                "SELECT " +
                        "ump.idUnidadManejo, um.descripcion, ump.contenido, um2.descripcion " +
                        "FROM "+TablesHelper.Producto.Table+" p " +
                        "INNER JOIN " + TablesHelper.UnidadMedidaxProducto.Table + " ump ON ump.idProducto = p.idProducto  " +
                        "INNER JOIN " + TablesHelper.UnidadMedida.Table + " um ON um.idUnidadMedida = ump.idUnidadManejo " +
                        "INNER JOIN " + TablesHelper.UnidadMedida.Table + " um2 ON um2.idUnidadMedida = 'UND' " +
                        "WHERE p."+TablesHelper.Producto.PKeyName+" = '"+idProducto+"'";

        SQLiteDatabase db = dataBaseHelper.getReadableDatabase();
        Cursor cur = db.rawQuery(rawQuery, null);
        Util.LogCursorInfo(cur, context);
        ArrayList<UnidadMedidaModel> lista = new ArrayList<>();
        cur.moveToFirst();

        //int sz = cur.getCount();
        while (!cur.isAfterLast()) {
            UnidadMedidaModel unidadMedida = new UnidadMedidaModel();
            unidadMedida.setIdUnidadManejo(cur.getString(0));
            unidadMedida.setDescripcion(cur.getString(1));
            unidadMedida.setContenido(cur.getString(2));
            unidadMedida.setIdUnidadContable("UND");
            unidadMedida.setDescripcionContable(cur.getString(3));
            lista.add(unidadMedida);
            cur.moveToNext();
        }

        cur.close();
        return lista;
    }

    public ArrayList<PoliticaPrecioModel> getPoliticaPrecios(String idProducto) {

        SQLiteDatabase db = dataBaseHelper.getReadableDatabase();
        String rawQuery =
                "SELECT pp1.idPoliticaPrecio,pp1.descripcion,ifnull(pp2.precioManejo,0),ifnull(pp2.precioContenido,0),ifnull(pp1.cantidadMinima,1),pp2.idUnidadManejo " +
                "FROM "+TablesHelper.PoliticaPrecio.Table+" pp1 " +
                "INNER JOIN "+TablesHelper.PoliticaPrecioxProducto.Table+" pp2 ON pp1.idPoliticaPrecio = pp2.idPolitica " +
                "WHERE "+TablesHelper.PoliticaPrecioxProducto.FKProducto+" = '"+idProducto+"'";

        Cursor cur = db.rawQuery(rawQuery, null);
        ArrayList<PoliticaPrecioModel> lista = new ArrayList<>();
        Util.LogCursorInfo(cur,context);
        cur.moveToFirst();

        while (!cur.isAfterLast()) {
            PoliticaPrecioModel politicaPrecio = new PoliticaPrecioModel();
            politicaPrecio.setIdPoliticaPrecio(cur.getString(0));
            politicaPrecio.setDescripcion(cur.getString(1));
            politicaPrecio.setPrecioManejo(Util.redondearDouble(cur.getDouble(2)));
            politicaPrecio.setPrecioContenido(Util.redondearDouble(cur.getDouble(3)));
            politicaPrecio.setCantidadMinima(cur.getInt(4));
            politicaPrecio.setIdUnidadManejo(cur.getString(5));
            lista.add(politicaPrecio);
            cur.moveToNext();
        }
        cur.close();
        return lista;
    }

    public PoliticaPrecioModel getPoliticaPrecio(String idProducto, String idPoliticaPrecio) {
        PoliticaPrecioModel politicaPrecio = null;
        String rawQuery =
                "SELECT pp1.idPoliticaPrecio,pp1.descripcion,ifnull(pp2.precioManejo,0),ifnull(pp2.precioContenido,0),ifnull(pp1.cantidadMinima,1) " +
                "FROM "+TablesHelper.PoliticaPrecio.Table+" pp1 " +
                "INNER JOIN "+TablesHelper.PoliticaPrecioxProducto.Table+" pp2 ON pp1.idPoliticaPrecio = pp2.idPolitica " +
                "WHERE "+TablesHelper.PoliticaPrecioxProducto.FKProducto+" = '"+idProducto+"' AND "+TablesHelper.PoliticaPrecioxProducto.FKPolitica+" = '"+idPoliticaPrecio+"'";

        SQLiteDatabase db = dataBaseHelper.getReadableDatabase();
        Cursor cur = db.rawQuery(rawQuery, null);
        ArrayList<PoliticaPrecioModel> lista = new ArrayList<>();
        cur.moveToFirst();

        while (!cur.isAfterLast()) {
            politicaPrecio = new PoliticaPrecioModel();
            politicaPrecio.setIdPoliticaPrecio(cur.getString(0));
            politicaPrecio.setDescripcion(cur.getString(1));
            politicaPrecio.setPrecioManejo(Util.redondearDouble(cur.getDouble(2)));
            politicaPrecio.setPrecioContenido(Util.redondearDouble(cur.getDouble(3)));
            politicaPrecio.setCantidadMinima(cur.getInt(4));
            lista.add(politicaPrecio);
            cur.moveToNext();
        }
        cur.close();
        return politicaPrecio;
    }

    public ArrayList<ProductoModel> getProductos(String idPoliticaCliente) {

        String rawQuery =
                "SELECT p.idProducto,p.descripcion,ifnull(peso,0), ifnull(p.tipoProducto,'V'), ppp.idPolitica, ppp.precioManejo, ppp.precioContenido, um.descripcion " +
                "FROM "+TablesHelper.Producto.Table+" p " +
                "INNER JOIN "+TablesHelper.UnidadMedidaxProducto.Table + " ump ON ump.idProducto = p.idProducto " +
                "INNER JOIN "+TablesHelper.UnidadMedida.Table + " um ON ump.idUnidadManejo = um.idUnidadMedida " +
                "INNER JOIN "+TablesHelper.PoliticaPrecioxProducto.Table + " ppp ON ppp.idProducto = p.idProducto  ";
                //"INNER JOIN "+TablesHelper.PoliticaPrecioxProducto.Table+" pp2 ON p.idProducto = pp2.idProducto AND pp2.idPoliticaPrecio = '"+idPoliticaPorDefecto+"' " +
               // " INNER JOIN "+TablesHelper.UnidadMedida.Table + " um ON p.idUnidadMenor = um.idUnidadMedida " +
               // " INNER JOIN "+TablesHelper.UnidadMedida.Table + " um2 ON p.idUnidadMayor = um2.idUnidadMedida ";

        SQLiteDatabase db = dataBaseHelper.getReadableDatabase();
        Cursor cur = db.rawQuery(rawQuery, null);
        Util.LogCursorInfo(cur,context);
        ArrayList<ProductoModel> lista = new ArrayList<>();
        cur.moveToFirst();

        while (!cur.isAfterLast()) {
            ProductoModel productoModel = new ProductoModel();
            productoModel.setIdProducto(cur.getString(0));
            productoModel.setDescripcion(cur.getString(1));
            productoModel.setPeso(cur.getDouble(2));
            productoModel.setTipoProducto(cur.getString(3));
            productoModel.setIdPoliticaPrecio(cur.getString(4));
            productoModel.setPrecioMayor(cur.getFloat(5));
            productoModel.setPrecioMenor(cur.getFloat(6));
            productoModel.setIdUnidadManejo(cur.getString(7));

            //para ingresar solo el primer producto de cada id
            boolean isAdded = false;
            for (ProductoModel prlista:lista) {
                if(prlista.getIdProducto().equals(productoModel.getIdProducto()))
                {
                    isAdded = true;
                    break;
                }
            }
            if(!isAdded) {
                lista.add(productoModel);
            }
            cur.moveToNext();
        }
        return lista;
    }

    public ArrayList<ProductoKardex>  getListaProductos(Boolean soloDisponibles) {
        ArrayList<ProductoKardex> listaProducto =  new ArrayList<>();

        /*String rawQuery =
                "SELECT p.idProducto, descripcion, stockInicial, stockPedido,stockDespachado,(stockInicial - stockPedido - stockDespachado) as stockDisponible, factorConversion " +
                        "FROM "+TablesHelper.Producto.Table+" p " +
                        "INNER JOIN "+TablesHelper.Kardex.Table+" k ON p.idProducto=k.idProducto ";*/

        String rawQuery =
                "SELECT p.idProducto, descripcion, stockInicial, stockPedido, stockDespachado, (stockInicial - stockPedido - stockDespachado) as stockDisponible " +
                        "FROM "+TablesHelper.Producto.Table+" p " +
                        "INNER JOIN "+TablesHelper.Kardex.Table+" k ON p.idProducto=k.idProducto ";

        if (soloDisponibles){
            rawQuery += "WHERE stockDisponible > 0";
        }

        //En caso sea PREVENTA y preventaEnLinea según configuración, no se toma el stock de los productos
        Ventas360App ventas360App = (Ventas360App) context.getApplicationContext();
        if (ventas360App.getModoVenta().equals(VendedorModel.MODO_PREVENTA) && !ventas360App.getSettings_preventaEnLinea()){
            /*rawQuery =
                    "SELECT p.idProducto, descripcion, 0, 0,0,0 as stockDisponible, factorConversion " +
                            "FROM "+TablesHelper.Producto.Table+" p " ;*/
            rawQuery =
                    "SELECT p.idProducto, descripcion, 0, 0, 0, 0 as stockDisponible " +
                            "FROM "+TablesHelper.Producto.Table+" p " ;
            if (soloDisponibles){
                rawQuery += "WHERE stockDisponible > 0"; //En preventa en teoria no hay ningun disponible, esto ocultaría todos los productos
            }
        }

        Log.d(TAG,rawQuery);
        SQLiteDatabase db = dataBaseHelper.getReadableDatabase();
        Cursor cur = db.rawQuery(rawQuery, null);
        cur.moveToFirst();

        try {
            while (!cur.isAfterLast()) {
                ProductoKardex item = new ProductoKardex();

                item.setIdProducto(cur.getString(0));
                item.setDescripcion(cur.getString(1));
                item.setStockInicial(cur.getInt(2));
                item.setStockPedido(cur.getInt(3));
                item.setStockDespachado(cur.getInt(4));
                item.setStockDisponibleGeneral(cur.getInt(5));

                //factor de conversion
                String rawQuery2 = "SELECT contenido " + "FROM "+TablesHelper.UnidadMedidaxProducto.Table+" p " +"WHERE idProducto = "+ item.getIdProducto() + " LIMIT 1";
                Cursor cur2 = db.rawQuery(rawQuery2, null);
                cur2.moveToFirst();
                if(!cur2.isAfterLast())
                {
                    item.setFactorConversion(cur2.getInt(0));
                }
                else{
                    item.setFactorConversion(1);
                }

                int stockEnUnidadMayor = item.getStockDisponibleGeneral() / item.getFactorConversion();
                int stockRestante = item.getStockDisponibleGeneral() % item.getFactorConversion();

                item.setStockDisponibleUnidadMayor(stockEnUnidadMayor);
                item.setStockDisponibleUnidadMenor(stockRestante);

                listaProducto.add(item);
                cur.moveToNext();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        cur.close();
        return listaProducto;
    }

    public ProductoModel getDetalleProducto(String idProducto) {
        String rawQuery = "SELECT p.descripcion, p.idProducto, l.descripcion, f.descripcion, p.peso " +
                "FROM Producto p INNER JOIN Linea l ON p.idLinea=l.idLinea INNER JOIN Familia f " +
                "ON p.idFamilia=f.idFamilia WHERE p.idProducto = '"+ idProducto +"'";

        ProductoModel detalleProducto = new ProductoModel();

        SQLiteDatabase db = dataBaseHelper.getReadableDatabase();
        Cursor cur = db.rawQuery(rawQuery, null);
        cur.moveToFirst();

        try {
            if (cur.moveToFirst()) {
                do {
                    detalleProducto.setDescripcion(cur.getString(0));
                    detalleProducto.setIdProducto(cur.getString(1));
                    detalleProducto.setIdLinea(cur.getString(2));
                    detalleProducto.setIdFamilia(cur.getString(3));
                    detalleProducto.setPeso(cur.getInt(4));

                } while (cur.moveToNext());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        cur.close();

        return detalleProducto;

    }

    /**
     * @param idProducto
     * @param idUnidadMedida ID de la unidad de medida a evaluar
     * @return <p>1: Si el ID de medida a evaluar es la unidad menor (idUnidadMenor)</p>
     *         <p>0: Si no es la unidad menor </p>
     *         <p>-1: Si es la unidad menor, pero es la unica unidad medida tanto el menor como el mayor son iguales </p>
     */
    public int isUnidadMinima(String idProducto, String idUnidadMedida) {
        int flag = 0;//NO
        String rawQuery =
                "SELECT idUnidadManejo FROM "+ TablesHelper.UnidadMedidaxProducto.Table+
                " WHERE "+TablesHelper.UnidadMedidaxProducto.FKProducto+" ='"+idProducto+"' ";


        SQLiteDatabase db = dataBaseHelper.getReadableDatabase();
        Cursor cur = db.rawQuery(rawQuery, null);
        cur.moveToFirst();
        
        String idUnidadManejo = "";

        while (!cur.isAfterLast()) {
            idUnidadManejo = cur.getString(0);
            cur.moveToNext();
        }

        /*
        if (idUnidadMedida.equals(idUnidadMenor) && idUnidadMedida.equals(idUnidadMayor)){
            flag = -1;//Si es la unidad menor pero tampoco hay unidad mayor
        }else{
            if (idUnidadMedida.equals(idUnidadMenor)){
                flag = 1; //Si es la unidad menor
            }else{
                flag = 0;//No es la unidad menor
            }
        }*/

        if (idUnidadMedida.equals(idUnidadManejo)){
            flag = -1; //Si es la unidad menor
        }else{
            flag = 0;//No es la unidad menor
        }

        cur.close();
        return flag;
    }

    public ProductoModel getProducto(String idProducto) {

        String rawQuery =
                "SELECT p.idProducto,p.descripcion,ifnull(p.peso,0),p.idLinea, p.idFamilia, ump.idUnidadManejo " +
                        "FROM "+TablesHelper.Producto.Table+" p " +
                        "INNER JOIN "+TablesHelper.UnidadMedidaxProducto.Table+" ump ON ump.idProducto = p.idProducto " +
                        "WHERE p.idProducto "+" = '"+idProducto+"'";

        SQLiteDatabase db = dataBaseHelper.getReadableDatabase();
        Cursor cur = db.rawQuery(rawQuery, null);
        ProductoModel productoModel = null;
        cur.moveToFirst();

        while (!cur.isAfterLast()) {
            productoModel = new ProductoModel();
            productoModel.setIdProducto(cur.getString(0));
            productoModel.setDescripcion(cur.getString(1));
            productoModel.setPeso(cur.getDouble(2));
            productoModel.setIdLinea(cur.getString(3));
            productoModel.setIdFamilia(cur.getString(4));
            productoModel.setIdUnidadManejo(cur.getString(5));
            cur.moveToNext();
        }
        cur.close();
        return productoModel;
    }

    public ArrayList<PoliticaPrecioModel> obtenerpoliticapreciodetalleXproducto (String idProducto){
        /*String rawQuery = "SELECT pp.descripcion,(SELECT um.descripcion FROM UnidadMedida um WHERE " +
                "um.idUnidadMedida=p.idUnidadMayor) AS UnidadMayor, ppp.precioMayor, " +
                "(SELECT um.descripcion FROM UnidadMedida um WHERE um.idUnidadMedida=p.idUnidadMenor) AS UnidadMenor, " +
                "ppp.precioMenor FROM PoliticaPrecioxProducto ppp INNER JOIN Producto p ON " +
                "ppp.idProducto=p.idProducto INNER JOIN PoliticaPrecio pp " +
                "ON pp.idPoliticaPrecio=ppp.idPoliticaPrecio WHERE ppp.idProducto= '"+ idProducto +"'";*/

        String rawQuery  =  "SELECT pp.descripcion," +
                            "(SELECT um.descripcion FROM UnidadMedida um WHERE um.idUnidadMedida=ppp.idUnidadManejo) AS UnidadMedida, " +
                            "ppp.precioManejo, " +
                            "ppp.precioContenido " +
                            "FROM PoliticaPrecioxProducto ppp " +
                            "INNER JOIN Producto p ON ppp.idProducto=p.idProducto " +
                            "INNER JOIN PoliticaPrecio pp ON pp.idPoliticaPrecio=ppp.idPolitica " +
                            "WHERE ppp.idProducto= '"+ idProducto +"'";

        SQLiteDatabase db = dataBaseHelper.getReadableDatabase();
        Cursor cur = db.rawQuery(rawQuery, null);
        ArrayList<PoliticaPrecioModel> listapoliticaprecios =  new ArrayList<>();
        cur.moveToFirst();

        try {
           // Log.e(TAG,"entrado wail "+(idProducto));
            while (!cur.isAfterLast()) {

                PoliticaPrecioModel itemPp = new PoliticaPrecioModel();
                itemPp.setDescripcion(cur.getString(0));
                itemPp.setUnidad(cur.getString(1));
                itemPp.setPrecioManejo(cur.getDouble(2));
                itemPp.setPrecioContenido(cur.getDouble(3));

                listapoliticaprecios.add(itemPp);
                cur.moveToNext();

                Log.e(TAG,"si reconoce wail "+(idProducto));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        cur.close();
      //  Log.e(TAG,"entrando a return "+(idProducto));
        return listapoliticaprecios;

    }

    public GuiaModel obtenerDetalleguia(){
        String rawQuery = "SELECT g.numeroGuia, g.fechaCarga, (SELECT count(*) FROM Kardex WHERE " +
                "(stockInicial-(stockPedido+stockDespachado)) > 0) AS ProducDisponible , g.estado FROM Guia g " +
                "WHERE g.estado = 'O' OR g.estado = 'P' ORDER BY g.fechaCarga ASC";
        SQLiteDatabase db = dataBaseHelper.getReadableDatabase();
        Cursor cur = db.rawQuery(rawQuery, null);
        GuiaModel guiaModel = null;
        cur.moveToFirst();

        while (!cur.isAfterLast()) {
            guiaModel = new GuiaModel();
            guiaModel.setNumeroguia(cur.getString(0));
            guiaModel.setFechaCarga(cur.getString(1));
            guiaModel.setProductoDisponible(cur.getInt(2));
            guiaModel.setEstado(cur.getString(3));
            cur.moveToNext();
        }
        cur.close();
        return guiaModel;
    }

    public Boolean actualizarEstadoGuia (String Numeroguia, String Estado){
        try {
            String where = TablesHelper.Guia.PKName + " = ?";
            String[] args = { String.valueOf(Numeroguia)};
            SQLiteDatabase db = dataBaseHelper.getWritableDatabase();

            Log.i(TAG, "where ");

            ContentValues updateValues = new ContentValues();
            DAOConfiguracion daoConfiguracion = new DAOConfiguracion(context);
            if(Estado.equals("C"))
                updateValues.put(TablesHelper.Guia.FechaCierre, daoConfiguracion.getFechaString());

            updateValues.put(TablesHelper.Guia.Estado, Estado);

            Log.i(TAG, ": Modificando...");

            db.update(TablesHelper.Guia.Table, updateValues, where, args );
            //db.close();
            Log.i(TAG, "Actualizar "+Numeroguia+" a "+Estado);
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Modificar ");
            e.printStackTrace();
            return false;
        }
    }

    public void actualizarKardexProducto(JSONArray jArray, String numeroPedidoActual) throws Exception {
        JSONObject jsonData = null;
        ContentValues cv = new ContentValues();
        String table = TablesHelper.Kardex.Table;

        String fkProducto = TablesHelper.Kardex.FKProducto;
        String stockInicial = TablesHelper.Kardex.stockInicial;
        String stockPedido = TablesHelper.Kardex.stockPedido;
        String stockDespachado = TablesHelper.Kardex.stockDespachado;

        SQLiteDatabase db = dataBaseHelper.getWritableDatabase();

        db.beginTransaction();

        try {

            for (int i = 0; i < jArray.length(); i++) {
                jsonData = jArray.getJSONObject(i);

                //Se obtiene las cantidades de los pedidos pendientes
                String idProductojson = jsonData.getString(fkProducto).trim();
                //int cantidadPedidos = getCantidadPedidoProducto(idProductojson, numeroPedidoActual);
                //El stock del producto es igual al stock del Almacén mas los productos que tiene el vendedor en pendientes de envio
                //int stockPedidoCalculado =  Integer.parseInt(jsonData.getString(stockPedido).trim()) + cantidadPedidos;

                cv.put(stockInicial, jsonData.getString(stockInicial).trim());
                cv.put(stockPedido, jsonData.getString(stockPedido).trim()/*stockPedidoCalculado*/);
                cv.put(stockDespachado, jsonData.getString(stockDespachado).trim());

                String whereClause = TablesHelper.Kardex.FKProducto + " = ?";
                String[] whereArgs = {idProductojson};
                db.update(table,cv,whereClause,whereArgs);
            }

            db.setTransactionSuccessful();
            Log.i(TAG, table + ": BD Actualizada");
        } catch (Exception e) {
            Log.e(TAG, table + ":" + e.getMessage());
            e.printStackTrace();
            throw new Exception(e);
        } finally {
            db.endTransaction();
        }
    }

    /**
     * @param idProducto
     * @return Retorna la suma de las cantidades en unidades minimas del producto en todos los pedidos que no hayan sido enviados aún y que no estén anulados. Sin tomar en cuenta el pedido actual que se esté realizando, ya que todavía no se termina.
     */
    public int getCantidadPedidoProducto(String idProducto,String numeroPedido) {
        int cantidadPedido = 0;
        String rawQuery =
                "SELECT  SUM(CASE " +
                        "WHEN ump.idUnidadContable = pd.idUnidadMedida " +
                        "THEN pd.cantidad " +
                        "ELSE pd.cantidad * ump.contenido " +
                        "END) " +
                        "FROM "+TablesHelper.PedidoDetalle.Table+" pd " +
                        "INNER JOIN " + TablesHelper.PedidoCabecera.Table+" pc ON pd.numeroPedido = pc.numeroPedido "+
                        "INNER JOIN " + TablesHelper.UnidadMedidaxProducto.Table+" ump ON ump.idProducto = p.idProducto "+
                        "INNER JOIN " + TablesHelper.Producto.Table+" p ON pd.idProducto = p.idProducto "+
                        "WHERE " +TablesHelper.PedidoCabecera.Estado+" <> 'A' AND "+TablesHelper.PedidoCabecera.Flag+" = 'P' AND pc."+TablesHelper.PedidoCabecera.PKeyName+" <> '"+numeroPedido+"' "+
                        "AND pd."+TablesHelper.Producto.PKeyName+" = '"+idProducto+"'";
        //Log.d(TAG,rawQuery);

        SQLiteDatabase db = dataBaseHelper.getReadableDatabase();
        Cursor cur = db.rawQuery(rawQuery, null);
        cur.moveToFirst();

        while (!cur.isAfterLast()) {
            cantidadPedido = cur.getInt(0);
            cur.moveToNext();
        }
        cur.close();
        Log.d(TAG,"getCantidadPedidoProducto:"+cantidadPedido);
        return cantidadPedido;
    }

    public boolean isUnidadVentaMayor() {
        int valor = 1;
        try{
            String rawQuery =
                    "SELECT * FROM "+TablesHelper.Configuracion.Table+" " +
                            "WHERE "+TablesHelper.Configuracion.PKName + " = '"+TablesHelper.Configuracion.UnidadVentaMayor+"'";

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

        Log.d(TAG,"isUnidadVentaMayor: "+valor);

        if (valor == 0) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * @return Retorna el ID de la politica de precio a seleccionar en caso que el cliente sea Mayorista y no cumpla su condición mínima de cantidad de productos para mantener esa política.
     */
    public String getIdPoliticaPrecioPorDefecto(){
        String idPoliticaPrecio = "";
        String rawQuery = "SELECT * FROM "+TablesHelper.Configuracion.Table+" WHERE "+TablesHelper.Configuracion.PKName+" = '"+TablesHelper.Configuracion.IdPoliticaMinorista+"'";
        //Log.d(TAG,rawQuery);
        SQLiteDatabase db = dataBaseHelper.getReadableDatabase();
        Cursor cur = db.rawQuery(rawQuery, null);

        cur.moveToFirst();

        if (cur.moveToFirst()) {
            do {
                idPoliticaPrecio = cur.getString(1);
                cur.moveToNext();
            }
            while (cur.moveToNext());
        }
        cur.close();
        Log.d(TAG,"getIdPoliticaPrecioPorDefecto: "+idPoliticaPrecio);
        return idPoliticaPrecio;
    }

    public ArrayList<LiquidacionProductoModel>  getLiquidacionGuia() {
        ArrayList<LiquidacionProductoModel> listaProducto =  new ArrayList<>();

        String rawQuery =
                "SELECT numeroDocumento,idProducto,descripcion,factorConversion,stockGuia,stockVenta,stockDevolucion,diferencia " +
                "FROM "+TablesHelper.Liquidacion.Table+" ORDER BY descripcion";

        Log.d(TAG,rawQuery);
        SQLiteDatabase db = dataBaseHelper.getReadableDatabase();
        Cursor cur = db.rawQuery(rawQuery, null);
        cur.moveToFirst();

        try {
            while (!cur.isAfterLast()) {
                LiquidacionProductoModel item = new LiquidacionProductoModel();
                item.setNumeroDocumento(cur.getString(0));
                item.setIdProducto(cur.getString(1));
                item.setDescripcion(cur.getString(2));
                item.setFactorConversion(cur.getString(3));
                item.setStockGuia(cur.getString(4));
                item.setStockVenta(cur.getString(5));
                item.setStockDevolucion(cur.getString(6));
                item.setDiferencia(cur.getString(7));
                listaProducto.add(item);
                cur.moveToNext();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        cur.close();
        return listaProducto;
    }

    public ArrayList<DevolucionDetalleModel> getProductosDevolucionKardex() {
        ArrayList<DevolucionDetalleModel> listaProducto =  new ArrayList<>();

        String rawQuery =
                "SELECT p.idProducto, p.descripcion, ump.contenido, (stockInicial - stockPedido - stockDespachado) as stockDevolucion " +
                        ", ump.idUnidadContable, ump.idUnidadManejo, um.descripcion, um2.descripcion " +
                        "FROM "+TablesHelper.Kardex.Table+" k " +
                        "INNER JOIN "+TablesHelper.Producto.Table+" p ON k.idProducto=p.idProducto " +
                        "INNER JOIN "+TablesHelper.UnidadMedida.Table+" um ON um.idUnidadMedida=ump.idUnidadContable " +
                        "INNER JOIN "+TablesHelper.UnidadMedida.Table+" um2 ON um2.idUnidadMedida=ump.idUnidadManejo " +
                        "INNER JOIN "+TablesHelper.UnidadMedidaxProducto.Table+" ump ON ump.idProducto=p.idProducto " +
                        "WHERE stockDevolucion > 0";

        Log.d(TAG,rawQuery);
        SQLiteDatabase db = dataBaseHelper.getReadableDatabase();
        Cursor cur = db.rawQuery(rawQuery, null);
        cur.moveToFirst();

        try {
            while (!cur.isAfterLast()) {
                DevolucionDetalleModel item = new DevolucionDetalleModel();
                item.setIdProducto(cur.getString(0));
                item.setDescripcion(cur.getString(1));
                item.setFactorConversion(cur.getInt(2));
                item.setStockDevolucion(cur.getInt(3));
                item.setIdUnidadMedidaMenor(cur.getString(4));
                item.setIdUnidadMedidaMayor(cur.getString(5));
                item.setUnidadMedidaMenor(cur.getString(6));
                item.setUnidadMedidaMayor(cur.getString(7));

                int stockDevolucionMayor = item.getStockDevolucion() / item.getFactorConversion();
                int stockDevolucionMenor = item.getStockDevolucion() % item.getFactorConversion();

                item.setStockDevolucionUnidadMayor(stockDevolucionMayor);
                item.setStockDevolucionUnidadMenor(stockDevolucionMenor);

                listaProducto.add(item);
                cur.moveToNext();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        cur.close();
        return listaProducto;
    }

    public String getTipoProducto(String idProducto) {
        String rawQuery =
                "SELECT ifnull("+TablesHelper.Producto.TipoProducto+",'') "+
                        " FROM "+ TablesHelper.Producto.Table+
                        " WHERE "+TablesHelper.Producto.PKeyName+" ='"+idProducto+"'";

        String tipoProducto = "";

        SQLiteDatabase db = dataBaseHelper.getReadableDatabase();
        Cursor cur = db.rawQuery(rawQuery, null);
        cur.moveToFirst();

        while (!cur.isAfterLast()) {
            tipoProducto = cur.getString(0);
            cur.moveToNext();
        }
        cur.close();
        return tipoProducto;
    }

    /**
     * @param idProducto
     * @param idPoliticaPrecio
     * @param idUnidadMedida
     * @return Retorna el precio de la politica en base a la unidad de medida
     */
    public double getPrecioProducto(String idProducto, String idPoliticaPrecio, String idUnidadMedida) {
        double precio = 0;
        String rawQuery = "SELECT "+TablesHelper.PoliticaPrecioxProducto.PrecioContenido+" FROM " + TablesHelper.PoliticaPrecioxProducto.Table+" WHERE "+
                TablesHelper.PoliticaPrecioxProducto.FKUnidadManejo+"='"+ idUnidadMedida+"' AND "+
                TablesHelper.PoliticaPrecioxProducto.FKProducto+"='"+ idProducto+"' AND "+
                TablesHelper.PoliticaPrecioxProducto.FKPolitica+"='"+ idPoliticaPrecio+"'";
        SQLiteDatabase db = dataBaseHelper.getReadableDatabase();
        /*String rawQuery =
                "SELECT (CASE (SELECT "+TablesHelper.Producto.FKUnidadMenor+" FROM "+TablesHelper.Producto.Table+" Where idProducto='"+idProducto+"') " +
                        "WHEN '"+idUnidadMedida+"' THEN "+TablesHelper.PoliticaPrecioxProducto.PrecioMenor+" " +
                        "ELSE "+TablesHelper.PoliticaPrecioxProducto.PrecioMayor+" " +
                        "END) " +
                "FROM "+TablesHelper.PoliticaPrecioxProducto.Table + " " +
                "WHERE "+TablesHelper.PoliticaPrecioxProducto.FKPoliticaPrecio + " = " +idPoliticaPrecio + " AND "+TablesHelper.PoliticaPrecioxProducto.FKProducto + " = '"+idProducto+"'" ;*/
        Log.e(TAG,rawQuery);
        Cursor cur = db.rawQuery(rawQuery, null);
        cur.moveToFirst();

        while (!cur.isAfterLast()) {
            precio = cur.getDouble(0);
            cur.moveToNext();
        }
        cur.close();
        return precio;
    }

    /*
    public double getPrecioProducto(String idProducto, String idPoliticaPrecio, String idUnidadMedida) {
        double precio = 0;
        String rawQuery =
                "SELECT (CASE (SELECT "+TablesHelper.Producto.FKUnidadMenor+" FROM "+TablesHelper.Producto.Table+" Where idProducto='"+idProducto+"') " +
                        "WHEN '"+idUnidadMedida+"' THEN "+TablesHelper.PoliticaPrecioxProducto.PrecioMenor+" " +
                        "ELSE "+TablesHelper.PoliticaPrecioxProducto.PrecioMayor+" " +
                        "END) " +
                        "FROM "+TablesHelper.PoliticaPrecioxProducto.Table + " " +
                        "WHERE "+TablesHelper.PoliticaPrecioxProducto.FKPolitica + " = " +idPoliticaPrecio + " AND "+TablesHelper.PoliticaPrecioxProducto.FKProducto + " = '"+idProducto+"'" ;
        Log.e(TAG,rawQuery);
        SQLiteDatabase db = dataBaseHelper.getReadableDatabase();
        Cursor cur = db.rawQuery(rawQuery, null);
        cur.moveToFirst();

        while (!cur.isAfterLast()) {
            precio = cur.getDouble(0);
            cur.moveToNext();
        }
        cur.close();
        return precio;
    }*/

    public PoliticaPrecioModel getPoliticaPrecioPorDefecto(){
        PoliticaPrecioModel politicaPrecioModel = null;
        String rawQuery =
                "SELECT * FROM PoliticaPrecio " +
                "WHERE idPoliticaPrecio = (SELECT descripcion FROM "+TablesHelper.Configuracion.Table+" WHERE "+TablesHelper.Configuracion.PKName+" = '"+TablesHelper.Configuracion.IdPoliticaMinorista+"')";
        //Log.d(TAG,rawQuery);
        SQLiteDatabase db = dataBaseHelper.getReadableDatabase();
        Cursor cur = db.rawQuery(rawQuery, null);

        cur.moveToFirst();

        if (cur.moveToFirst()) {
            do {
                politicaPrecioModel = new PoliticaPrecioModel();
                politicaPrecioModel.setIdPoliticaPrecio(cur.getString(0));
                politicaPrecioModel.setDescripcion(cur.getString(1));
                politicaPrecioModel.setCantidadMinima(cur.getInt(2));
                cur.moveToNext();
            }
            while (cur.moveToNext());
        }
        cur.close();
        return politicaPrecioModel;
    }

    public double getPorcentajePercepcion(String idProducto) {
        double percepcion = 0;
        String rawQuery =
                "SELECT ifnull("+TablesHelper.Producto.PorcentajePercepcion+", 0) "+
                "FROM "+TablesHelper.Producto.Table + " " +
                "WHERE "+TablesHelper.Producto.PKeyName + " = '" +idProducto +"'";

        SQLiteDatabase db = dataBaseHelper.getReadableDatabase();
        Cursor cur = db.rawQuery(rawQuery, null);
        cur.moveToFirst();

        while (!cur.isAfterLast()) {
            percepcion = cur.getDouble(0);
            cur.moveToNext();
        }
        cur.close();
        return percepcion;
    }

    public double getPorcentajeISC(String idProducto) {
        double isc = 0;
        String rawQuery =
                "SELECT ifnull("+TablesHelper.Producto.PorcentajeISC+", 0) "+
                        "FROM "+TablesHelper.Producto.Table + " " +
                        "WHERE "+TablesHelper.Producto.PKeyName + " = '" +idProducto +"'";

        SQLiteDatabase db = dataBaseHelper.getReadableDatabase();
        Cursor cur = db.rawQuery(rawQuery, null);
        cur.moveToFirst();

        while (!cur.isAfterLast()) {
            isc = cur.getDouble(0);
            cur.moveToNext();
        }
        cur.close();
        Log.i(TAG,"getPorcentajeISC:"+isc);
        return isc;
    }
}
