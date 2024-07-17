package com.expediodigital.ventas360.util;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.Environment;
import android.util.Log;

import com.expediodigital.ventas360.DAO.DAOConfiguracion;
import com.expediodigital.ventas360.DAO.DAOEncuesta;
import com.expediodigital.ventas360.R;
import com.expediodigital.ventas360.Ventas360App;
import com.expediodigital.ventas360.model.ClienteCoordenadasModel;
import com.expediodigital.ventas360.model.EncuestaDetallePreguntaModel;
import com.expediodigital.ventas360.model.EncuestaRespuestaModel;
import com.expediodigital.ventas360.model.GuiaModel;
import com.expediodigital.ventas360.model.VendedorModel;
import com.readystatesoftware.sqliteasset.SQLiteAssetHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by Kevin Robinson Meza Hinostroza on julio 2017.
 * Expedio Digital
 * Meza.Hinostroza.Robin@gmail.com
 */

public class DataBaseHelper extends SQLiteAssetHelper {
    private static final String DATABASE_NAME = "ventas360App.db";
    private static final int DATABASE_VERSION = 10;
    public static final String TAG = "DataBaseHelper";

    private Context context;
    private static DataBaseHelper dbInstance = null;

    public DataBaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    public static DataBaseHelper getInstance(Context ctx) {
        //Application context no es igual a context de un activity
        // Use the application context, which will ensure that you
        // don't accidentally leak an Activity's context.
        // See this article for more information: http://bit.ly/6LRzfx
        if (dbInstance == null) {
            dbInstance = new DataBaseHelper(ctx.getApplicationContext());
        }
        return dbInstance;
    }

    private DataBaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
        // Delete old database when upgrading
        setForcedUpgrade(DATABASE_VERSION);
    }

    /**
     * Metodos Sincronización -------------------------------------------------------
     */

    public void actualizarEmpresa(JSONArray jArray) {
        JSONObject jsonData = null;
        ContentValues cv = new ContentValues();
        String table = TablesHelper.Empresa.Table;
        String pkName = TablesHelper.Empresa.PKName;
        String ruc = TablesHelper.Empresa.Ruc;
        String razonSocial = TablesHelper.Empresa.RazonSocial;

        getReadableDatabase().delete(table, null, null);
        SQLiteDatabase db = getWritableDatabase();

        db.beginTransaction();

        try {

            for (int i = 0; i < jArray.length(); i++) {
                jsonData = jArray.getJSONObject(i);
                cv.put(pkName, jsonData.getString(pkName).trim());
                cv.put(ruc, jsonData.getString(ruc).trim());
                cv.put(razonSocial, jsonData.getString(razonSocial).trim());

                db.insert(table, null, cv);
            }

            db.setTransactionSuccessful();
            Log.i(TAG, table + ": BD Actualizada");
        } catch (JSONException e) {
            Log.e(TAG, table + ":" + e.getMessage());
            throw new RuntimeException(e);
        } finally {
            db.endTransaction();
        }
    }

    public void actualizarUsuario(JSONArray jArray) {
        JSONObject jsonData = null;
        ContentValues cv = new ContentValues();
        String table = TablesHelper.Usuario.Table;
        String pkName = TablesHelper.Usuario.PKeyName;
        String usuario = TablesHelper.Usuario.Usuario;
        String password = TablesHelper.Usuario.Clave;
        String fkEmpresa = TablesHelper.Usuario.FKEmpresa;
        String fkSucursal = TablesHelper.Usuario.FKSucursal;

        getReadableDatabase().delete(table, null, null);
        SQLiteDatabase db = getWritableDatabase();

        db.beginTransaction();

        try {

            for (int i = 0; i < jArray.length(); i++) {
                jsonData = jArray.getJSONObject(i);
                cv.put(pkName, jsonData.getString(pkName).trim());
                cv.put(usuario, jsonData.getString(usuario).trim());
                cv.put(password, jsonData.getString(password).trim());
                cv.put(fkEmpresa, jsonData.getString(fkEmpresa).trim());
                cv.put(fkSucursal, jsonData.getString(fkSucursal).trim());

                db.insert(table, null, cv);
            }

            db.setTransactionSuccessful();
            Log.i(TAG, table + ": BD Actualizada");
        } catch (JSONException e) {
            Log.e(TAG, table + ":" + e.getMessage());
            throw new RuntimeException(e);
        } finally {
            db.endTransaction();
        }
    }

    public void actualizarVendedor(JSONArray jArray) throws Exception {

        JSONObject jsonData = null;
        ContentValues cv = new ContentValues();
        String table = TablesHelper.Vendedor.Table;
        String pkName = TablesHelper.Vendedor.PKeyName;

        String fkEmpresa = TablesHelper.Vendedor.FKEmpresa;
        String fkSucursal = TablesHelper.Vendedor.FKSucursal;

        String nombre = TablesHelper.Vendedor.Nombre;
        String fkUsuario = TablesHelper.Vendedor.FKUsuario;
        String tipo = TablesHelper.Vendedor.Tipo;
        String serie = TablesHelper.Vendedor.Serie;

        String fkRuta = TablesHelper.Vendedor.FKRuta;
        String fkAlmacen = TablesHelper.Vendedor.FKAlmacen;
        String modoVenta = TablesHelper.Vendedor.ModoVenta;
        String estado = TablesHelper.Vendedor.Estado;

        getReadableDatabase().delete(table, null, null);
        SQLiteDatabase db = getWritableDatabase();

        db.beginTransaction();

        try {

            for (int i = 0; i < jArray.length(); i++) {

                jsonData = jArray.getJSONObject(i);
                cv.put(pkName, jsonData.getString(pkName).trim());
                cv.put(fkEmpresa, jsonData.getString(fkEmpresa).trim());
                cv.put(fkSucursal, jsonData.getString(fkSucursal).trim());
                cv.put(nombre, jsonData.getString(nombre).trim());
                cv.put(fkUsuario, jsonData.getString(fkUsuario).trim());
                cv.put(tipo, jsonData.getString(tipo).trim());
                cv.put(serie, jsonData.getString(serie).trim());

                cv.put(fkRuta, jsonData.getString(fkRuta).trim());
                cv.put(fkAlmacen, jsonData.getString(fkAlmacen).trim());
                cv.put(modoVenta, jsonData.getString(modoVenta).trim());
                if (jsonData.has(estado)) {
                    cv.put(estado, jsonData.getString(estado).trim());
                }
                db.insertOrThrow(table, null, cv);
            }

            db.setTransactionSuccessful();
            Log.i(TAG, table + ": BD Actualizada");
        } catch (JSONException e) {
            Log.e(TAG, table + ":" + e.getMessage());
            throw new RuntimeException(e);
        } finally {
            db.endTransaction();
        }
    }

    public void actualizarCliente(JSONArray jArray) throws Exception {
        JSONObject jsonData = null;
        ContentValues cv = new ContentValues();
        String table = TablesHelper.Cliente.Table;
        String pkName = TablesHelper.Cliente.PKeyName;
        String ruc = TablesHelper.Cliente.RucDni;
        String razonSocial = TablesHelper.Cliente.RazonSocial;
        String correo = TablesHelper.Cliente.Correo;
        String fkModulo = TablesHelper.Cliente.FKModulo;
        String orden = TablesHelper.Cliente.Orden;
        String direccion = TablesHelper.Cliente.Direccion;
        String direccionFiscal = TablesHelper.Cliente.DireccionFiscal;
        String latitud = TablesHelper.Cliente.Latitud;
        String longitud = TablesHelper.Cliente.Longitud;
        String idSegmento = TablesHelper.Cliente.FKSegmento;
        String idCluster = TablesHelper.Cliente.FKCluster;
        String limiteCredito = TablesHelper.Cliente.LimiteCredito;
        String idSubGiro = TablesHelper.Cliente.FKSubGiro;
        String afectoPercepcion = TablesHelper.Cliente.AfectoPercepcion;
        String nroExhibidores = TablesHelper.Cliente.NroExhibidores;
        String nroPuertasFrio = TablesHelper.Cliente.NroPuertasFrio;

        getReadableDatabase().delete(table, null, null);
        SQLiteDatabase db = getWritableDatabase();

        db.beginTransaction();

        try {

            for (int i = 0; i < jArray.length(); i++) {
                jsonData = jArray.getJSONObject(i);
                cv.put(pkName, jsonData.getString(pkName).trim());
                cv.put(ruc, jsonData.getString(ruc).trim());
                cv.put(correo, jsonData.getString(correo).trim());
                cv.put(razonSocial, jsonData.getString(razonSocial).trim());
                cv.put(fkModulo, jsonData.getString(fkModulo).trim());
                cv.put(orden, jsonData.getInt(orden));
                cv.put(direccion, jsonData.getString(direccion).trim());
                cv.put(direccionFiscal, jsonData.getString(direccionFiscal).trim());
                cv.put(latitud, jsonData.getString(latitud).trim());
                cv.put(longitud, jsonData.getString(longitud).trim());
                cv.put(idSegmento, jsonData.getString(idSegmento).trim());
                cv.put(idCluster, jsonData.getString(idCluster).trim());
                cv.put(limiteCredito, jsonData.getDouble(limiteCredito));
                cv.put(idSubGiro, jsonData.getString(idSubGiro).trim());
                cv.put(afectoPercepcion, jsonData.getString(afectoPercepcion).trim());
                cv.put(nroExhibidores, jsonData.getInt(nroExhibidores));
                cv.put(nroPuertasFrio, jsonData.getInt(nroPuertasFrio));

                db.insertOrThrow(table, null, cv);
            }

            db.setTransactionSuccessful();
            Log.i(TAG, table + ": BD Actualizada");
        } catch (JSONException e) {
            Log.e(TAG, table + ":" + e.getMessage());
            throw new RuntimeException(e);
        } finally {
            db.endTransaction();
        }
    }

    public void actualizarClienteCoordenadas(JSONArray jArray) {
        JSONObject jsonData = null;
        ContentValues cv;
        String table = TablesHelper.ClienteCoordenadas.Table;
        String pKeyName = TablesHelper.ClienteCoordenadas.PKeyName;
        String latitud = TablesHelper.ClienteCoordenadas.Latitud;
        String longitud = TablesHelper.ClienteCoordenadas.Longitud;
        String flag = TablesHelper.ClienteCoordenadas.Flag;

        getReadableDatabase().delete(table, TablesHelper.ClienteCoordenadas.Flag + " <> ?", new String[]{ClienteCoordenadasModel.FLAG_PENDIENTE});
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            for (int i = 0; i < jArray.length(); i++) {
                jsonData = jArray.getJSONObject(i);
                cv = new ContentValues();
                cv.put(pKeyName, jsonData.getString(pKeyName));
                cv.put(latitud, jsonData.getString(latitud));
                cv.put(longitud, jsonData.getString(longitud));
                cv.put(flag, jsonData.getString(flag));

                db.insert(table, null, cv);
            }
            db.setTransactionSuccessful();
            Log.i(TAG, table + ": BD Actualizada");
        } catch (JSONException e) {
            Log.e(TAG, table + ":" + e.getMessage());
            throw new RuntimeException(e);
        } finally {
            db.endTransaction();
        }
    }

/*
    public void actualizarProducto(JSONArray jArray) throws Exception {
        JSONObject jsonData = null;
        ContentValues cv = new ContentValues();
        String table = TablesHelper.Producto.Table;
        String pkName = TablesHelper.Producto.PKeyName;
        String descripcion = TablesHelper.Producto.Descripcion;
        String fkLinea = TablesHelper.Producto.FKLinea;
        String fkFamilia = TablesHelper.Producto.FKFamilia;
        String peso = TablesHelper.Producto.Peso;
        String fkUnidadMenor = TablesHelper.Producto.FKUnidadMenor;
        String fkUnidadMayor = TablesHelper.Producto.FKUnidadMayor;
        String factorConversion = TablesHelper.Producto.FactorConversion;
        String fkProveedor = TablesHelper.Producto.FKProveedor;
        String fkProductoERP = TablesHelper.Producto.FKProductoERP;
        String descripcionERP = TablesHelper.Producto.DescripcionERP;
        String tipoProducto = TablesHelper.Producto.TipoProducto;
        String fkMarca = TablesHelper.Producto.FKMarca;
        String porcentajePercepcion = TablesHelper.Producto.PorcentajePercepcion;
        String porcentajeISC = TablesHelper.Producto.PorcentajeISC;

        getReadableDatabase().delete(table, null, null);
        SQLiteDatabase db = getWritableDatabase();

        db.beginTransaction();

        try {

            for (int i = 0; i < jArray.length(); i++) {
                jsonData = jArray.getJSONObject(i);
                cv.put(pkName, jsonData.getString(pkName).trim());
                cv.put(descripcion, jsonData.getString(descripcion).trim());
                cv.put(fkLinea, jsonData.getString(fkLinea).trim());
                cv.put(fkFamilia, jsonData.getString(fkFamilia).trim());
                cv.put(peso, jsonData.getString(peso).trim());
                cv.put(fkUnidadMenor, jsonData.getString(fkUnidadMenor));
                cv.put(fkUnidadMayor, jsonData.getString(fkUnidadMayor).trim());
                cv.put(factorConversion, jsonData.getInt(factorConversion));
                cv.put(fkProveedor, jsonData.getString(fkProveedor).trim());
                cv.put(fkProductoERP, jsonData.getString(fkProductoERP).trim());
                cv.put(descripcionERP, jsonData.getString(descripcionERP).trim());
                cv.put(tipoProducto, jsonData.getString(tipoProducto).trim());
                cv.put(fkMarca, jsonData.getString(fkMarca).trim());
                cv.put(porcentajePercepcion, jsonData.getString(porcentajePercepcion).trim());
                cv.put(porcentajeISC, jsonData.getString(porcentajeISC).trim());

                db.insertOrThrow(table, null, cv);
            }

            db.setTransactionSuccessful();
            Log.i(TAG, table + ": BD Actualizada");
        } catch (JSONException e) {
            Log.e(TAG, table + ":" + e.getMessage());
            throw new RuntimeException(e);
        } finally {
            db.endTransaction();
        }
    }*/

    public void actualizarKardex(JSONArray jArray) throws Exception {
        JSONObject jsonData = null;
        ContentValues cv = new ContentValues();
        String table = TablesHelper.Kardex.Table;

        String fkProducto = TablesHelper.Kardex.FKProducto;
        String stockInicial = TablesHelper.Kardex.stockInicial;
        String stockPedido = TablesHelper.Kardex.stockPedido;
        String stockDespachado = TablesHelper.Kardex.stockDespachado;

        getReadableDatabase().delete(table, null, null);
        SQLiteDatabase db = getWritableDatabase();

        db.beginTransaction();

        try {

            for (int i = 0; i < jArray.length(); i++) {
                jsonData = jArray.getJSONObject(i);

                cv.put(fkProducto, jsonData.getString(fkProducto).trim());
                cv.put(stockInicial, jsonData.getString(stockInicial).trim());
                cv.put(stockPedido, jsonData.getString(stockPedido).trim());
                cv.put(stockDespachado, jsonData.getString(stockDespachado).trim());

                db.insertOrThrow(table, null, cv);
            }

            db.setTransactionSuccessful();
            Log.i(TAG, table + ": BD Actualizada");
        } catch (Exception e) {
            Log.e(TAG, table + ":" + e.getMessage());
            throw new RuntimeException(e);
        } finally {
            db.endTransaction();
        }
    }

    public void actualizarLiquidacion(JSONArray jArray) throws Exception {
        JSONObject jsonData = null;
        ContentValues cv = new ContentValues();
        String table = TablesHelper.Liquidacion.Table;

        String numeroDocumento = TablesHelper.Liquidacion.NumeroDocumento;
        String pkName = TablesHelper.Liquidacion.PKName;
        String descripcion = TablesHelper.Liquidacion.Descripcion;
        String factorConversion = TablesHelper.Liquidacion.FactorConversion;
        String stockGuia = TablesHelper.Liquidacion.StockGuia;
        String stockVenta = TablesHelper.Liquidacion.StockVenta;
        String stockDevolucion = TablesHelper.Liquidacion.StockDevolucion;
        String diferencia = TablesHelper.Liquidacion.Diferencia;

        getReadableDatabase().delete(table, null, null);
        SQLiteDatabase db = getWritableDatabase();

        db.beginTransaction();

        try {

            for (int i = 0; i < jArray.length(); i++) {
                jsonData = jArray.getJSONObject(i);

                cv.put(numeroDocumento, jsonData.getString(numeroDocumento).trim());
                cv.put(pkName, jsonData.getString(pkName).trim());
                cv.put(descripcion, jsonData.getString(descripcion).trim());
                cv.put(factorConversion, jsonData.getString(factorConversion).trim());
                cv.put(stockGuia, jsonData.getString(stockGuia).trim());
                cv.put(stockVenta, jsonData.getString(stockVenta).trim());
                cv.put(stockDevolucion, jsonData.getString(stockDevolucion).trim());
                cv.put(diferencia, jsonData.getString(diferencia).trim());

                db.insertOrThrow(table, null, cv);
            }

            db.setTransactionSuccessful();
            Log.i(TAG, table + ": BD Actualizada");
        } catch (Exception e) {
            Log.e(TAG, table + ":" + e.getMessage());
            throw new RuntimeException(e);
        } finally {
            db.endTransaction();
        }
    }

    public void actualizarServicio(JSONArray jArray) {
        JSONObject jsonData = null;
        ContentValues cv = new ContentValues();
        String table = TablesHelper.Servicio.Table;
        String pkName = TablesHelper.Servicio.PKName;
        String url = TablesHelper.Servicio.Url;
        String tipo = TablesHelper.Servicio.Tipo;

        getReadableDatabase().delete(table, null, null);
        SQLiteDatabase db = getWritableDatabase();

        db.beginTransaction();

        try {

            for (int i = 0; i < jArray.length(); i++) {
                jsonData = jArray.getJSONObject(i);
                cv.put(pkName, jsonData.getString(pkName).trim());
                cv.put(url, jsonData.getString(url).trim());
                cv.put(tipo, jsonData.getString(tipo).trim());

                db.insert(table, null, cv);
            }

            db.setTransactionSuccessful();
            Log.i(TAG, table + ": BD Actualizada");
        } catch (JSONException e) {
            Log.e(TAG, table + ":" + e.getMessage());
            throw new RuntimeException(e);
        } finally {
            db.endTransaction();
        }
    }

    public void actualizarConfiguracion(JSONArray jArray) {
        JSONObject jsonData = null;
        ContentValues cv = new ContentValues();
        String table = TablesHelper.Configuracion.Table;
        String pkName = TablesHelper.Configuracion.PKName;
        String descripcion = TablesHelper.Configuracion.Descripcion;

        getReadableDatabase().delete(table, null, null);
        SQLiteDatabase db = getWritableDatabase();

        db.beginTransaction();

        try {

            for (int i = 0; i < jArray.length(); i++) {
                jsonData = jArray.getJSONObject(i);
                cv.put(pkName, jsonData.getString(pkName).trim());
                cv.put(descripcion, jsonData.getString(descripcion).trim());

                db.insert(table, null, cv);
            }

            db.setTransactionSuccessful();
            Log.i(TAG, table + ": BD Actualizada");
        } catch (JSONException e) {
            Log.e(TAG, table + ":" + e.getMessage());
            throw new RuntimeException(e);
        } finally {
            db.endTransaction();
        }
    }

    public void actualizarMotivoNoVenta(JSONArray jArray) {
        JSONObject jsonData = null;
        ContentValues cv = new ContentValues();
        String table = TablesHelper.MotivoNoVenta.Table;
        String pkName = TablesHelper.MotivoNoVenta.PKName;
        String descripcion = TablesHelper.MotivoNoVenta.Descripcion;

        getReadableDatabase().delete(table, null, null);
        SQLiteDatabase db = getWritableDatabase();

        db.beginTransaction();

        try {

            for (int i = 0; i < jArray.length(); i++) {
                jsonData = jArray.getJSONObject(i);
                cv.put(pkName, jsonData.getString(pkName).trim());
                cv.put(descripcion, jsonData.getString(descripcion).trim());

                db.insert(table, null, cv);
            }

            db.setTransactionSuccessful();
            Log.i(TAG, table + ": BD Actualizada");
        } catch (JSONException e) {
            Log.e(TAG, table + ":" + e.getMessage());
            throw new RuntimeException(e);
        } finally {
            db.endTransaction();
        }
    }

    public void actualizarLinea(JSONArray jArray) {
        JSONObject jsonData = null;
        ContentValues cv = new ContentValues();
        String table = TablesHelper.Linea.Table;
        String pkName = TablesHelper.Linea.PKName;
        String descripcion = TablesHelper.Linea.Descripcion;

        getReadableDatabase().delete(table, null, null);
        SQLiteDatabase db = getWritableDatabase();

        db.beginTransaction();

        try {

            for (int i = 0; i < jArray.length(); i++) {
                jsonData = jArray.getJSONObject(i);
                cv.put(pkName, jsonData.getString(pkName).trim());
                cv.put(descripcion, jsonData.getString(descripcion).trim());

                db.insert(table, null, cv);
            }

            db.setTransactionSuccessful();
            Log.i(TAG, table + ": BD Actualizada");
        } catch (JSONException e) {
            Log.e(TAG, table + ":" + e.getMessage());
            throw new RuntimeException(e);
        } finally {
            db.endTransaction();

        }
    }

    public void actualizarFormaPago(JSONArray jArray) {
        JSONObject jsonData = null;
        ContentValues cv = new ContentValues();
        String table = TablesHelper.FormaPago.Table;
        String pkName = TablesHelper.FormaPago.PKName;
        String descripcion = TablesHelper.FormaPago.Descripcion;

        getReadableDatabase().delete(table, null, null);
        SQLiteDatabase db = getWritableDatabase();

        db.beginTransaction();

        try {

            for (int i = 0; i < jArray.length(); i++) {
                jsonData = jArray.getJSONObject(i);
                cv.put(pkName, jsonData.getString(pkName).trim());
                cv.put(descripcion, jsonData.getString(descripcion).trim());

                db.insert(table, null, cv);
            }

            db.setTransactionSuccessful();
            Log.i(TAG, table + ": BD Actualizada");
        } catch (JSONException e) {
            Log.e(TAG, table + ":" + e.getMessage());
            throw new RuntimeException(e);
        } finally {
            db.endTransaction();
        }
    }

    public void actualizarUnidadMedida(JSONArray jArray) {
        JSONObject jsonData = null;
        ContentValues cv = new ContentValues();
        String table = TablesHelper.UnidadMedida.Table;
        String pkName = TablesHelper.UnidadMedida.PKName;
        String descripcion = TablesHelper.UnidadMedida.Descripcion;

        getReadableDatabase().delete(table, null, null);
        SQLiteDatabase db = getWritableDatabase();

        db.beginTransaction();

        try {

            for (int i = 0; i < jArray.length(); i++) {
                jsonData = jArray.getJSONObject(i);
                cv.put(pkName, jsonData.getString(pkName).trim());
                cv.put(descripcion, jsonData.getString(descripcion).trim());

                db.insert(table, null, cv);
            }

            db.setTransactionSuccessful();
            Log.i(TAG, table + ": BD Actualizada");
        } catch (JSONException e) {
            Log.e(TAG, table + ":" + e.getMessage());
            throw new RuntimeException(e);
        } finally {
            db.endTransaction();
        }
    }

    public void actualizarFamilia(JSONArray jArray) {
        JSONObject jsonData = null;
        ContentValues cv = new ContentValues();
        String table = TablesHelper.Familia.Table;
        String pkName = TablesHelper.Familia.PKName;
        String descripcion = TablesHelper.Familia.Descripcion;

        getReadableDatabase().delete(table, null, null);
        SQLiteDatabase db = getWritableDatabase();

        db.beginTransaction();

        try {

            for (int i = 0; i < jArray.length(); i++) {
                jsonData = jArray.getJSONObject(i);
                cv.put(pkName, jsonData.getString(pkName).trim());
                cv.put(descripcion, jsonData.getString(descripcion).trim());

                db.insert(table, null, cv);
            }

            db.setTransactionSuccessful();
            Log.i(TAG, table + ": BD Actualizada");
        } catch (JSONException e) {
            Log.e(TAG, table + ":" + e.getMessage());
            throw new RuntimeException(e);
        } finally {
            db.endTransaction();
        }
    }

    public void actualizarProveedor(JSONArray jArray) {
        JSONObject jsonData = null;
        ContentValues cv = new ContentValues();
        String table = TablesHelper.Proveedor.Table;
        String pkName = TablesHelper.Proveedor.PKName;
        String descripcion = TablesHelper.Proveedor.RazonSocial;

        getReadableDatabase().delete(table, null, null);
        SQLiteDatabase db = getWritableDatabase();

        db.beginTransaction();

        try {

            for (int i = 0; i < jArray.length(); i++) {
                jsonData = jArray.getJSONObject(i);
                cv.put(pkName, jsonData.getString(pkName).trim());
                cv.put(descripcion, jsonData.getString(descripcion).trim());

                db.insert(table, null, cv);
            }

            db.setTransactionSuccessful();
            Log.i(TAG, table + ": BD Actualizada");
        } catch (JSONException e) {
            Log.e(TAG, table + ":" + e.getMessage());
            throw new RuntimeException(e);
        } finally {
            db.endTransaction();
        }
    }

    public void actualizarGuia(JSONArray jArray) {
        JSONObject jsonData = null;
        ContentValues cv = new ContentValues();
        String table = TablesHelper.Guia.Table;
        String pkName = TablesHelper.Guia.PKName;
        String fechaCarga = TablesHelper.Guia.FechaCarga;
        String fechaCierre = TablesHelper.Guia.FechaCierre;
        String estado = TablesHelper.Guia.Estado;

        getReadableDatabase().delete(table, null, null);
        SQLiteDatabase db = getWritableDatabase();

        db.beginTransaction();

        try {

            for (int i = 0; i < jArray.length(); i++) {
                jsonData = jArray.getJSONObject(i);
                cv.put(pkName, jsonData.getString(pkName).trim());
                cv.put(fechaCarga, jsonData.getString(fechaCarga).trim());
                cv.put(fechaCierre, jsonData.getString(fechaCierre).trim());
                cv.put(estado, jsonData.getString(estado).trim());

                db.insert(table, null, cv);
            }

            db.setTransactionSuccessful();
            Log.i(TAG, table + ": BD Actualizada");

            //Si la guia es una distinta a la anterior, se deben eliminar todos los pedidos que quedaron pendientes, ya que no pertenecen a este nuevo grupo
            Ventas360App ventas360App = (Ventas360App) context.getApplicationContext();
            DAOConfiguracion daoConfiguracion = new DAOConfiguracion(context);

            ArrayList<GuiaModel> listaGuias = daoConfiguracion.getGuias();
            if (!listaGuias.isEmpty()) {
                String nuevaGuia = listaGuias.get(0).getNumeroguia();
                if (!ventas360App.getNumeroGuia().equals(nuevaGuia)) {
                    Log.e(TAG, "SE OBTUVO UN NUEVO NUMERO DE GUÍA, BORRANDO TODOS LOS PEDIDOS QUE QUEDARON PENDIENTE...");
                    ventas360App.setNumeroGuia(nuevaGuia);//Establecemos la primera guia como la actual
                    eliminarDetallePedidoPendientes();
                    eliminarCabeceraPedidoPendientes();
                }
            }

        } catch (JSONException e) {
            Log.e(TAG, table + ":" + e.getMessage());
            throw new RuntimeException(e);
        } finally {
            db.endTransaction();
        }
    }

    public void actualizarPoliticaPrecio(JSONArray jArray) {
        JSONObject jsonData = null;
        ContentValues cv = new ContentValues();
        String table = TablesHelper.PoliticaPrecio.Table;
        String pkName = TablesHelper.PoliticaPrecio.PKName;
        String descripcion = TablesHelper.PoliticaPrecio.Descripcion;
        String cantidadMinima = TablesHelper.PoliticaPrecio.CantidadMinima;

        getReadableDatabase().delete(table, null, null);
        SQLiteDatabase db = getWritableDatabase();

        db.beginTransaction();

        try {

            for (int i = 0; i < jArray.length(); i++) {
                jsonData = jArray.getJSONObject(i);
                cv.put(pkName, jsonData.getString(pkName).trim());
                cv.put(descripcion, jsonData.getString(descripcion).trim());
                cv.put(cantidadMinima, jsonData.getString(cantidadMinima).trim());

                db.insert(table, null, cv);
            }

            db.setTransactionSuccessful();
            Log.i(TAG, table + ": BD Actualizada");
        } catch (JSONException e) {
            Log.e(TAG, table + ":" + e.getMessage());
            throw new RuntimeException(e);
        } finally {
            db.endTransaction();
        }
    }

    public void actualizarPoliticaPrecioxCliente(JSONArray jArray) {
        JSONObject jsonData = null;
        ContentValues cv = new ContentValues();
        String table = TablesHelper.PoliticaPrecioxCliente.Table;
        String fkPoliticaPrecio = TablesHelper.PoliticaPrecioxCliente.FKPoliticaPrecio;
        String fkCliente = TablesHelper.PoliticaPrecioxCliente.FKCliente;

        getReadableDatabase().delete(table, null, null);
        SQLiteDatabase db = getWritableDatabase();

        db.beginTransaction();

        try {

            for (int i = 0; i < jArray.length(); i++) {
                jsonData = jArray.getJSONObject(i);
                cv.put(fkPoliticaPrecio, jsonData.getString(fkPoliticaPrecio).trim());
                cv.put(fkCliente, jsonData.getString(fkCliente).trim());

                db.insert(table, null, cv);
            }

            db.setTransactionSuccessful();
            Log.i(TAG, table + ": BD Actualizada");
        } catch (JSONException e) {
            Log.e(TAG, table + ":" + e.getMessage());
            throw new RuntimeException(e);
        } finally {
            db.endTransaction();
        }
    }

    public void actualizarMarca(JSONArray jArray) {
        JSONObject jsonData = null;
        ContentValues cv = new ContentValues();
        String table = TablesHelper.Marca.Table;
        String pkName = TablesHelper.Marca.PKeyName;
        String descripcion = TablesHelper.Marca.Descripcion;

        getReadableDatabase().delete(table, null, null);
        SQLiteDatabase db = getWritableDatabase();

        db.beginTransaction();

        try {
            for (int i = 0; i < jArray.length(); i++) {
                jsonData = jArray.getJSONObject(i);
                cv.put(pkName, jsonData.getString(pkName).trim());
                cv.put(descripcion, jsonData.getString(descripcion).trim());

                db.insert(table, null, cv);
            }
            db.setTransactionSuccessful();
            Log.i(TAG, table + ": BD Actualizada");
        } catch (JSONException e) {
            Log.e(TAG, table + ":" + e.getMessage());
            throw new RuntimeException(e);
        } finally {
            db.endTransaction();
        }
    }

    public void actualizarSegmento(JSONArray jArray) {
        JSONObject jsonData = null;
        ContentValues cv = new ContentValues();
        String table = TablesHelper.Segmento.Table;
        String pkName = TablesHelper.Segmento.PKeyName;
        String descripcion = TablesHelper.Segmento.Descripcion;

        getReadableDatabase().delete(table, null, null);
        SQLiteDatabase db = getWritableDatabase();

        db.beginTransaction();

        try {
            for (int i = 0; i < jArray.length(); i++) {
                jsonData = jArray.getJSONObject(i);
                cv.put(pkName, jsonData.getString(pkName).trim());
                cv.put(descripcion, jsonData.getString(descripcion).trim());

                db.insert(table, null, cv);
            }
            db.setTransactionSuccessful();
            Log.i(TAG, table + ": BD Actualizada");
        } catch (JSONException e) {
            Log.e(TAG, table + ":" + e.getMessage());
            throw new RuntimeException(e);
        } finally {
            db.endTransaction();
        }
    }

    public void actualizarSubGiro(JSONArray jArray) {
        JSONObject jsonData = null;
        ContentValues cv = new ContentValues();
        String table = TablesHelper.SubGiro.Table;
        String pkName = TablesHelper.SubGiro.PKeyName;
        String descripcion = TablesHelper.SubGiro.Descripcion;

        getReadableDatabase().delete(table, null, null);
        SQLiteDatabase db = getWritableDatabase();

        db.beginTransaction();

        try {
            for (int i = 0; i < jArray.length(); i++) {
                jsonData = jArray.getJSONObject(i);
                cv.put(pkName, jsonData.getString(pkName).trim());
                cv.put(descripcion, jsonData.getString(descripcion).trim());

                db.insert(table, null, cv);
            }
            db.setTransactionSuccessful();
            Log.i(TAG, table + ": BD Actualizada");
        } catch (JSONException e) {
            Log.e(TAG, table + ":" + e.getMessage());
            throw new RuntimeException(e);
        } finally {
            db.endTransaction();
        }
    }

    public void actualizarHojaRutaIndicador(JSONArray jArray) {
        JSONObject jsonData = null;
        ContentValues cv;
        String table = TablesHelper.HojaRutaIndicador.Table;
        String ejercicio = TablesHelper.HojaRutaIndicador.ejercicio;
        String periodo = TablesHelper.HojaRutaIndicador.periodo;
        String idCliente = TablesHelper.HojaRutaIndicador.idCliente;
        String tipoCobertura = TablesHelper.HojaRutaIndicador.tipoCobertura;
        String programado = TablesHelper.HojaRutaIndicador.programado;
        String transcurrido = TablesHelper.HojaRutaIndicador.transcurrido;
        String liquidado = TablesHelper.HojaRutaIndicador.liquidado;
        String hitRate = TablesHelper.HojaRutaIndicador.hitRate;
        String venAnoAnterior = TablesHelper.HojaRutaIndicador.venAnoAnterior;
        String venMesAnterior = TablesHelper.HojaRutaIndicador.venMesAnterior;
        String avanceMesActual = TablesHelper.HojaRutaIndicador.avanceMesActual;
        String proyectado = TablesHelper.HojaRutaIndicador.proyectado;
        String avanceAnual = TablesHelper.HojaRutaIndicador.avanceAnual;
        String avanceMes = TablesHelper.HojaRutaIndicador.avanceMes;
        String CUOTAGTM = TablesHelper.HojaRutaIndicador.CUOTAGTM;
        String SEGMENTO = TablesHelper.HojaRutaIndicador.SEGMENTO;
        String EXHIBIDORES = TablesHelper.HojaRutaIndicador.EXHIBIDORES;
        String NROPTAFRIOGTM = TablesHelper.HojaRutaIndicador.NROPTAFRIOGTM;
        String coberturaMultiple = TablesHelper.HojaRutaIndicador.coberturaMultiple;

        getReadableDatabase().delete(table, null, null);
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            for (int i = 0; i < jArray.length(); i++) {
                jsonData = jArray.getJSONObject(i);
                cv = new ContentValues();
                cv.put(ejercicio, jsonData.getString(ejercicio));
                cv.put(periodo, jsonData.getString(periodo));
                cv.put(idCliente, jsonData.getString(idCliente));
                cv.put(tipoCobertura, jsonData.getString(tipoCobertura));
                cv.put(programado, jsonData.getString(programado));
                cv.put(transcurrido, jsonData.getString(transcurrido));
                cv.put(liquidado, jsonData.getString(liquidado));
                cv.put(hitRate, jsonData.getString(hitRate));
                cv.put(venAnoAnterior, jsonData.getString(venAnoAnterior));
                cv.put(venMesAnterior, jsonData.getString(venMesAnterior));
                cv.put(avanceMesActual, jsonData.getString(avanceMesActual));
                cv.put(proyectado, jsonData.getString(proyectado));
                cv.put(avanceAnual, jsonData.getString(avanceAnual));
                cv.put(avanceMes, jsonData.getString(avanceMes));
                cv.put(CUOTAGTM, jsonData.getString(CUOTAGTM));
                cv.put(SEGMENTO, jsonData.getString(SEGMENTO));
                cv.put(EXHIBIDORES, jsonData.getString(EXHIBIDORES));
                cv.put(NROPTAFRIOGTM, jsonData.getString(NROPTAFRIOGTM));
                cv.put(coberturaMultiple, jsonData.getString(coberturaMultiple));

                db.insert(table, null, cv);
            }
            db.setTransactionSuccessful();
            Log.i(TAG, table + ": BD Actualizada");
        } catch (JSONException e) {
            Log.e(TAG, table + ":" + e.getMessage());
            throw new RuntimeException(e);
        } finally {
            db.endTransaction();
        }
    }

    public void actualizarHojaRutaMarcas(JSONArray jArray) {
        JSONObject jsonData = null;
        ContentValues cv;
        String table = TablesHelper.HojaRutaMarcas.Table;
        String ejercicio = TablesHelper.HojaRutaMarcas.ejercicio;
        String periodo = TablesHelper.HojaRutaMarcas.periodo;
        String idCliente = TablesHelper.HojaRutaMarcas.idCliente;
        String marca = TablesHelper.HojaRutaMarcas.marca;
        String canPaq = TablesHelper.HojaRutaMarcas.canPaq;

        getReadableDatabase().delete(table, null, null);
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            for (int i = 0; i < jArray.length(); i++) {
                jsonData = jArray.getJSONObject(i);
                cv = new ContentValues();
                cv.put(ejercicio, jsonData.getString(ejercicio));
                cv.put(periodo, jsonData.getString(periodo));
                cv.put(idCliente, jsonData.getString(idCliente));
                cv.put(marca, jsonData.getString(marca));
                cv.put(canPaq, jsonData.getString(canPaq));

                db.insert(table, null, cv);
            }
            db.setTransactionSuccessful();
            Log.i(TAG, table + ": BD Actualizada");
        } catch (JSONException e) {
            Log.e(TAG, table + ":" + e.getMessage());
            throw new RuntimeException(e);
        } finally {
            db.endTransaction();
        }
    }

    public void actualizarHRCliente(JSONArray jArray) {
        JSONObject jsonData = null;
        ContentValues cv;
        String table = TablesHelper.HRCliente.table;
        String ejercicio = TablesHelper.HRCliente.ejercicio;
        String periodo = TablesHelper.HRCliente.periodo;
        String idCliente = TablesHelper.HRCliente.idCliente;
        String programado = TablesHelper.HRCliente.programado;
        String transcurrido = TablesHelper.HRCliente.transcurrido;
        String liquidado = TablesHelper.HRCliente.liquidado;
        String hitRate = TablesHelper.HRCliente.hitRate;
        String coberturaMultiple = TablesHelper.HRCliente.coberturaMultiple;
        String cuotaSoles = TablesHelper.HRCliente.cuotaSoles;
        String cuotaPaquetes = TablesHelper.HRCliente.cuotaPaquetes;
        String ventaSoles = TablesHelper.HRCliente.ventaSoles;
        String ventaPaquetes = TablesHelper.HRCliente.ventaPaquetes;
        String diasLaborados = TablesHelper.HRCliente.diasLaborados;
        String diasLaborales = TablesHelper.HRCliente.diasLaborales;
        String segmento = TablesHelper.HRCliente.segmento;
        String nroExhibidores = TablesHelper.HRCliente.nroExhibidores;
        String nroPuertasFrio = TablesHelper.HRCliente.nroPuertasFrio;
        String avance = TablesHelper.HRCliente.avance;
        String necesidadDiaSoles = TablesHelper.HRCliente.necesidadDiaSoles;
        String necesidadDiaPaquetes = TablesHelper.HRCliente.necesidadDiaPaquetes;

        getReadableDatabase().delete(table, null, null);
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            for (int i = 0; i < jArray.length(); i++) {
                jsonData = jArray.getJSONObject(i);
                cv = new ContentValues();
                cv.put(ejercicio, jsonData.getString(ejercicio));
                cv.put(periodo, jsonData.getString(periodo));
                cv.put(idCliente, jsonData.getString(idCliente));
                cv.put(programado, jsonData.getString(programado));
                cv.put(transcurrido, jsonData.getString(transcurrido));
                cv.put(liquidado, jsonData.getString(liquidado));
                cv.put(hitRate, jsonData.getString(hitRate));
                cv.put(coberturaMultiple, jsonData.getString(coberturaMultiple));
                cv.put(cuotaSoles, jsonData.getString(cuotaSoles));
                cv.put(cuotaPaquetes, jsonData.getString(cuotaPaquetes));
                cv.put(ventaSoles, jsonData.getString(ventaSoles));
                cv.put(ventaPaquetes, jsonData.getString(ventaPaquetes));
                cv.put(diasLaborados, jsonData.getString(diasLaborados));
                cv.put(diasLaborales, jsonData.getString(diasLaborales));
                cv.put(segmento, jsonData.getString(segmento));
                cv.put(nroExhibidores, jsonData.getString(nroExhibidores));
                cv.put(nroPuertasFrio, jsonData.getString(nroPuertasFrio));
                cv.put(avance, jsonData.getString(avance));
                cv.put(necesidadDiaSoles, jsonData.getString(necesidadDiaSoles));
                cv.put(necesidadDiaPaquetes, jsonData.getString(necesidadDiaPaquetes));


                db.insert(table, null, cv);
            }
            db.setTransactionSuccessful();
            Log.i(TAG, table + ": BD Actualizada");
        } catch (JSONException e) {
            Log.e(TAG, table + ":" + e.getMessage());
            throw new RuntimeException(e);
        } finally {
            db.endTransaction();
        }
    }

    public void actualizarHRVendedor(JSONArray jArray) {
        JSONObject jsonData = null;
        ContentValues cv;
        String table = TablesHelper.HRVendedor.table;
        String ejercicio = TablesHelper.HRVendedor.ejercicio;
        String periodo = TablesHelper.HRVendedor.periodo;
        String idVendedor = TablesHelper.HRVendedor.idVendedor;
        String cuotaSoles = TablesHelper.HRVendedor.cuotaSoles;
        String cuotaPaquetes = TablesHelper.HRVendedor.cuotaPaquetes;
        String ventaSoles = TablesHelper.HRVendedor.ventaSoles;
        String ventaPaquetes = TablesHelper.HRVendedor.ventaPaquetes;
        String diasLaborados = TablesHelper.HRVendedor.diasLaborados;
        String diasLaborales = TablesHelper.HRVendedor.diasLaborales;
        String coberturaMultiple = TablesHelper.HRVendedor.coberturaMultiple;
        String hitRate = TablesHelper.HRVendedor.hitRate;
        String avance = TablesHelper.HRVendedor.avance;
        String necesidadDiaSoles = TablesHelper.HRVendedor.necesidadDiaSoles;
        String necesidadDiaPaquetes = TablesHelper.HRVendedor.necesidadDiaPaquetes;

        getReadableDatabase().delete(table, null, null);
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            for (int i = 0; i < jArray.length(); i++) {
                jsonData = jArray.getJSONObject(i);
                cv = new ContentValues();
                cv.put(ejercicio, jsonData.getString(ejercicio));
                cv.put(periodo, jsonData.getString(periodo));
                cv.put(idVendedor, jsonData.getString(idVendedor));
                cv.put(cuotaSoles, jsonData.getString(cuotaSoles));
                cv.put(cuotaPaquetes, jsonData.getString(cuotaPaquetes));
                cv.put(ventaSoles, jsonData.getString(ventaSoles));
                cv.put(ventaPaquetes, jsonData.getString(ventaPaquetes));
                cv.put(diasLaborados, jsonData.getString(diasLaborados));
                cv.put(diasLaborales, jsonData.getString(diasLaborales));
                cv.put(coberturaMultiple, jsonData.getString(coberturaMultiple));
                cv.put(hitRate, jsonData.getString(hitRate));
                cv.put(avance, jsonData.getString(avance));
                cv.put(necesidadDiaSoles, jsonData.getString(necesidadDiaSoles));
                cv.put(necesidadDiaPaquetes, jsonData.getString(necesidadDiaPaquetes));

                db.insert(table, null, cv);
            }
            db.setTransactionSuccessful();
            Log.i(TAG, table + ": BD Actualizada");
        } catch (JSONException e) {
            Log.e(TAG, table + ":" + e.getMessage());
            throw new RuntimeException(e);
        } finally {
            db.endTransaction();
        }
    }

    public void actualizarHRMarcaResumen(JSONArray jArray) {
        JSONObject jsonData = null;
        ContentValues cv;
        String table = TablesHelper.HRMarcaResumen.table;
        String ejercicio = TablesHelper.HRMarcaResumen.ejercicio;
        String periodo = TablesHelper.HRMarcaResumen.periodo;
        String idCliente = TablesHelper.HRMarcaResumen.idCliente;
        String marcas = TablesHelper.HRMarcaResumen.marcas;

        getReadableDatabase().delete(table, null, null);
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            for (int i = 0; i < jArray.length(); i++) {
                jsonData = jArray.getJSONObject(i);
                cv = new ContentValues();
                cv.put(ejercicio, jsonData.getString(ejercicio));
                cv.put(periodo, jsonData.getString(periodo));
                cv.put(idCliente, jsonData.getString(idCliente));
                cv.put(marcas, jsonData.getString(marcas));

                db.insert(table, null, cv);
            }
            db.setTransactionSuccessful();
            Log.i(TAG, table + ": BD Actualizada");
        } catch (JSONException e) {
            Log.e(TAG, table + ":" + e.getMessage());
            throw new RuntimeException(e);
        } finally {
            db.endTransaction();
        }
    }

    // region ACTUALIZAR TABLAS PARA PROMOCIONES
    public void actualizarPromocionxCliente(JSONArray jArray) {
        JSONObject jsonData = null;
        ContentValues cv = new ContentValues();
        String table = TablesHelper.PromocionxCliente.Table;
        String pkPromocion = TablesHelper.PromocionxCliente.PKeyPromocion;
        String pkCliente = TablesHelper.PromocionxCliente.PKeyCliente;

        getReadableDatabase().delete(table, null, null);
        SQLiteDatabase db = getWritableDatabase();

        db.beginTransaction();

        try {

            for (int i = 0; i < jArray.length(); i++) {
                jsonData = jArray.getJSONObject(i);
                cv.put(pkPromocion, jsonData.getString(pkPromocion).trim());
                cv.put(pkCliente, jsonData.getString(pkCliente).trim());

                db.insert(table, null, cv);
            }

            db.setTransactionSuccessful();
            Log.i(TAG, table + ": BD Actualizada");
        } catch (JSONException e) {
            Log.e(TAG, table + ":" + e.getMessage());
            throw new RuntimeException(e);
        } finally {
            db.endTransaction();
        }
    }

    public void actualizarPromocionxPoliticaPrecio(JSONArray jArray) {
        JSONObject jsonData = null;
        ContentValues cv = new ContentValues();
        String table = TablesHelper.PromocionxPoliticaPrecio.Table;
        String pkPromocion = TablesHelper.PromocionxPoliticaPrecio.PKeyPromocion;
        String pkPoliticaPrecio = TablesHelper.PromocionxPoliticaPrecio.PKeyPoliticaPrecio;

        getReadableDatabase().delete(table, null, null);
        SQLiteDatabase db = getWritableDatabase();

        db.beginTransaction();

        try {

            for (int i = 0; i < jArray.length(); i++) {
                jsonData = jArray.getJSONObject(i);
                cv.put(pkPromocion, jsonData.getString(pkPromocion).trim());
                cv.put(pkPoliticaPrecio, jsonData.getString(pkPoliticaPrecio).trim());

                db.insert(table, null, cv);
            }

            db.setTransactionSuccessful();
            Log.i(TAG, table + ": BD Actualizada");
        } catch (JSONException e) {
            Log.e(TAG, table + ":" + e.getMessage());
            throw new RuntimeException(e);
        } finally {
            db.endTransaction();
        }
    }

    public void actualizarPromocionxVendedor(JSONArray jArray) {
        JSONObject jsonData = null;
        ContentValues cv = new ContentValues();
        String table = TablesHelper.PromocionxVendedor.Table;
        String pkPromocion = TablesHelper.PromocionxVendedor.PKeyPromocion;
        String pkVendedor = TablesHelper.PromocionxVendedor.PKeyVendedor;

        getReadableDatabase().delete(table, null, null);
        SQLiteDatabase db = getWritableDatabase();

        db.beginTransaction();

        try {

            for (int i = 0; i < jArray.length(); i++) {
                jsonData = jArray.getJSONObject(i);
                cv.put(pkPromocion, jsonData.getString(pkPromocion).trim());
                cv.put(pkVendedor, jsonData.getString(pkVendedor).trim());

                db.insert(table, null, cv);
            }

            db.setTransactionSuccessful();
            Log.i(TAG, table + ": BD Actualizada");
        } catch (JSONException e) {
            Log.e(TAG, table + ":" + e.getMessage());
            throw new RuntimeException(e);
        } finally {
            db.endTransaction();
        }
    }

    public void actualizarPromocionDetalle(JSONArray jArray) throws JSONException {
        JSONObject jsonData = null;
        ContentValues cv = new ContentValues();
        String table = TablesHelper.PromocionDetalle.Table;

        String pkPromocion = TablesHelper.PromocionDetalle.PKeyName;
        String promocion = TablesHelper.PromocionDetalle.Promocion;
        String tipoPromocion = TablesHelper.PromocionDetalle.TipoPromocion;
        String item = TablesHelper.PromocionDetalle.Item;
        String totalAgrupado = TablesHelper.PromocionDetalle.TotalAgrupado;
        String agrupado = TablesHelper.PromocionDetalle.Agrupado;
        String entrada = TablesHelper.PromocionDetalle.Entrada;
        String tipoCondicion = TablesHelper.PromocionDetalle.TipoCondicion;
        String montoCondicion = TablesHelper.PromocionDetalle.MontoCondicion;
        String cantidadCondicion = TablesHelper.PromocionDetalle.CantidadCondicion;
        String salida = TablesHelper.PromocionDetalle.Salida;
        String cantidadBonificada = TablesHelper.PromocionDetalle.CantidadBonificada;
        String montoLimite = TablesHelper.PromocionDetalle.MontoLimite;
        String cantidadLimite = TablesHelper.PromocionDetalle.CantidadLimite;
        String maximaBonificacion = TablesHelper.PromocionDetalle.MaximaBonificacion;
        String acumulado = TablesHelper.PromocionDetalle.Acumulado;
        String porCliente = TablesHelper.PromocionDetalle.PorCliente;
        String porVendedor = TablesHelper.PromocionDetalle.PorVendedor;
        String porPoliticaPrecio = TablesHelper.PromocionDetalle.PorPoliticaPrecio;
        String evaluarEnUnidadMayor = TablesHelper.PromocionDetalle.EvaluarEnUnidadMayor;
        String multiplicarPorCompra = TablesHelper.PromocionDetalle.MultiplicarPorCompra;
        String fechaInicio = TablesHelper.PromocionDetalle.FechaInicio;

        getReadableDatabase().delete(table, null, null);
        SQLiteDatabase db = getWritableDatabase();

        db.beginTransaction();

        try {

            for (int i = 0; i < jArray.length(); i++) {
                jsonData = jArray.getJSONObject(i);
                cv.put(pkPromocion, jsonData.getString(pkPromocion).trim());
                cv.put(promocion, jsonData.getString(promocion).trim());
                cv.put(tipoPromocion, jsonData.getString(tipoPromocion).trim());
                cv.put(item, jsonData.getString(item).trim());
                cv.put(totalAgrupado, jsonData.getString(totalAgrupado).trim());
                cv.put(agrupado, jsonData.getString(agrupado).trim());
                cv.put(entrada, jsonData.getString(entrada).trim());
                cv.put(tipoCondicion, jsonData.getString(tipoCondicion).trim());
                cv.put(montoCondicion, jsonData.getString(montoCondicion).trim());
                cv.put(cantidadCondicion, jsonData.getString(cantidadCondicion).trim());
                cv.put(salida, jsonData.getString(salida).trim());
                cv.put(cantidadBonificada, jsonData.getString(cantidadBonificada).trim());
                cv.put(montoLimite, jsonData.getString(montoLimite).trim());
                cv.put(cantidadLimite, jsonData.getString(cantidadLimite).trim());
                cv.put(maximaBonificacion, jsonData.getString(maximaBonificacion).trim());
                cv.put(acumulado, jsonData.getString(acumulado).trim());
                cv.put(porCliente, jsonData.getString(porCliente).trim());
                cv.put(porVendedor, jsonData.getString(porVendedor).trim());
                cv.put(porPoliticaPrecio, jsonData.getString(porPoliticaPrecio).trim());
                cv.put(evaluarEnUnidadMayor, jsonData.getString(evaluarEnUnidadMayor).trim());
                cv.put(multiplicarPorCompra, jsonData.getString(multiplicarPorCompra).trim());
                cv.put(fechaInicio, jsonData.getString(fechaInicio).trim());

                db.insert(table, null, cv);
            }

            db.setTransactionSuccessful();
            Log.i(TAG, table + ": BD Actualizada");
        } catch (JSONException e) {
            Log.e(TAG, table + ":" + e.getMessage());
            throw new RuntimeException(e);
        } finally {
            db.endTransaction();
        }
    }
    // endregion

    public void actualizarObjPedido(JSONArray jArray, String idVendedor) throws JSONException, SQLiteException {

        //test
        String rawQuery2;
        rawQuery2 = "SELECT * FROM "+ TablesHelper.PedidoDetalle.Table  ;
        SQLiteDatabase db2 = getReadableDatabase();
        Cursor cur2 = db2.rawQuery(rawQuery2, null);
        Util.LogCursorInfo(cur2, context);
        //-

        JSONObject jsonData = null;
        String table = TablesHelper.ObjPedido.Table;
        String tableDetalle = TablesHelper.PedidoDetalle.Table;
        String tableCabecera = TablesHelper.PedidoCabecera.Table;

        if (eliminarCabeceraPedidoEnviados(idVendedor)) {

            String pkeyName = TablesHelper.PedidoCabecera.PKeyName;
            String fkCliente = TablesHelper.PedidoCabecera.FKCliente;
            String fkVendedor = TablesHelper.PedidoCabecera.FKVendedor;
            String fechaPedido = TablesHelper.PedidoCabecera.FechaPedido;
            String fechaEntrega = TablesHelper.PedidoCabecera.FechaEntrega;
            String fkFormaPago = TablesHelper.PedidoCabecera.FKFormaPago;
            String observacion = TablesHelper.PedidoCabecera.Observacion;
            String pesoTotal = TablesHelper.PedidoCabecera.PesoTotal;
            String importeTotal = TablesHelper.PedidoCabecera.ImporteTotal;
            String fkMotivoNoVenta = TablesHelper.PedidoCabecera.FKMotivoNoVenta;
            String estado = TablesHelper.PedidoCabecera.Estado;
            String flag = TablesHelper.PedidoCabecera.Flag;
            String serieDocumento = TablesHelper.PedidoCabecera.SerieDocumento;
            String numeroDocumento = TablesHelper.PedidoCabecera.NumeroDocumento;

            String pedidoEntregado = TablesHelper.PedidoCabecera.PedidoEntregado;
            String fechaEntregado = TablesHelper.PedidoCabecera.FechaEntregado;

            SQLiteDatabase db = getWritableDatabase();

            db.beginTransaction();

            try {

                String fkeyPedido = TablesHelper.PedidoDetalle.PKeyPedido;
                String fkProducto = TablesHelper.PedidoDetalle.PKeyProducto;
                String fkPoliticaPrecio = TablesHelper.PedidoDetalle.FKPoliticaPrecio;
                String tipoProducto = TablesHelper.PedidoDetalle.TipoProducto;
                String precioBruto = TablesHelper.PedidoDetalle.PrecioBruto;
                String cantidad = TablesHelper.PedidoDetalle.Cantidad;
                String precioNeto = TablesHelper.PedidoDetalle.PrecioNeto;
                String fkUnidadMedida = TablesHelper.PedidoDetalle.FKUnidadMedida;
                String pesoNeto = TablesHelper.PedidoDetalle.PesoNeto;
                String item = TablesHelper.PedidoDetalle.Item;
                String percepcion = TablesHelper.PedidoDetalle.Percepcion;
                String isc = TablesHelper.PedidoDetalle.ISC;
                String malla = TablesHelper.PedidoDetalle.Malla;
                String estadoDetalle = TablesHelper.PedidoDetalle.EstadoDetalle;

                for (int i = 0; i < jArray.length(); i++) {
                    // Pedido Cabecera
                    jsonData = jArray.getJSONObject(i);
                    ContentValues cv = new ContentValues();
                    cv.put(pkeyName, jsonData.getString(pkeyName).trim());
                    cv.put(fkCliente, jsonData.getString(fkCliente).trim());
                    cv.put(fkVendedor, jsonData.getString(fkVendedor).trim());
                    cv.put(fechaPedido, jsonData.getString(fechaPedido).trim());
                    cv.put(fechaEntrega, jsonData.getString(fechaEntrega).trim());
                    cv.put(fkFormaPago, jsonData.getString(fkFormaPago).trim());
                    cv.put(observacion, jsonData.getString(observacion).trim());
                    cv.put(pesoTotal, jsonData.getString(pesoTotal).trim());
                    cv.put(importeTotal, jsonData.getString(importeTotal).trim());
                    cv.put(fkMotivoNoVenta, jsonData.getString(fkMotivoNoVenta).trim());
                    cv.put(estado, jsonData.getString(estado).trim());
                    cv.put(flag, jsonData.getString(flag).trim());
                    cv.put(serieDocumento, jsonData.getString(serieDocumento).trim());
                    cv.put(numeroDocumento, jsonData.getString(numeroDocumento).trim());

                    cv.put(pedidoEntregado, jsonData.getString(pedidoEntregado).trim());
                    cv.put(fechaEntregado, jsonData.getString(fechaEntregado).trim());

                    //validacion para insertar detalle
                    if (db.insert(tableCabecera, null, cv) != -1) {
                        JSONArray json_detalles = new JSONArray(jsonData.getString("detalles"));

                        for (int j = 0; j < json_detalles.length(); j++) {

                            JSONObject jsonDataDetalle = json_detalles.getJSONObject(j);
                            ContentValues cv2 = new ContentValues();

                            cv2.put(fkeyPedido, jsonDataDetalle.getString(fkeyPedido).trim());
                            cv2.put(fkProducto, jsonDataDetalle.getString(fkProducto).trim());
                            cv2.put(fkPoliticaPrecio, jsonDataDetalle.getString(fkPoliticaPrecio).trim());
                            cv2.put(tipoProducto, jsonDataDetalle.getString(tipoProducto).trim());
                            cv2.put(precioBruto, jsonDataDetalle.getString(precioBruto).trim());
                            cv2.put(cantidad, jsonDataDetalle.getString(cantidad).trim());
                            cv2.put(precioNeto, jsonDataDetalle.getString(precioNeto).trim());
                            cv2.put(fkUnidadMedida, jsonDataDetalle.getString(fkUnidadMedida).trim());
                            cv2.put(pesoNeto, jsonDataDetalle.getString(pesoNeto).trim());

                            cv2.put(item, j);
                            cv2.put(percepcion, jsonDataDetalle.getString(percepcion).trim());
                            cv2.put(isc, jsonDataDetalle.getString(isc).trim());

                            String malla2 = "";
                            if(!jsonDataDetalle.getString(malla).trim().equals("null"))
                            {
                                malla2 = jsonDataDetalle.getString(malla).trim();
                            }
                            cv2.put(malla, malla2);
                            cv2.put(estadoDetalle, jsonDataDetalle.getString(estadoDetalle).trim());

                            db.insert(tableDetalle, null, cv2);
                        }
                    } else {
                        Log.d(TAG, jsonData.getString(pkeyName) + "Pedido error y no se insertó su detalle");
                    }

                }

                db.setTransactionSuccessful();

            } catch (JSONException e) {
                Log.i(TAG, table + " - JSON Exception ");
                e.printStackTrace();

            } catch (SQLiteException e) {
                Log.i(TAG, table + " - SQLITE Exception");
                e.printStackTrace();

            } finally {
                db.endTransaction();
            }

        } else {
            Log.i(TAG, table + ": no se ejecutó correctamente el método eliminarCabeceraPedidoEnviados");
        }

    }

    public boolean eliminarDetallePedidoEnviados(String idVendedor) throws SQLiteException {
        String where = "numeroPedido in (select numeroPedido from PedidoCabecera where flag <> ? or idVendedor <> ?)";
        String[] args = {"P", idVendedor};

        String tipoVendedor = ((Ventas360App) context.getApplicationContext()).getTipoVendedor();
        if (tipoVendedor.equals(VendedorModel.TIPO_TRANSPORTISTA)) {
            where = "numeroPedido in (select numeroPedido from PedidoCabecera where flag <> ? )";
            args = new String[]{"P"};
        }


        String table = TablesHelper.PedidoDetalle.Table;

        try {
            SQLiteDatabase db = getWritableDatabase();
            int pr = db.delete(table, where, args);

            Log.i(TAG, table + " eliminados todos menos flag (P)");
            return true;

        } catch (SQLiteException e) {
            Log.i(TAG, table + " SQLITE Exception al eliminar");
            e.printStackTrace();
            return false;
        }

    }

    public boolean eliminarCabeceraPedidoEnviados(String idVendedor) throws SQLiteException {
        String where = "flag <> ? or idVendedor <> ?";
        String[] args = {"P", idVendedor};

        String tipoVendedor = ((Ventas360App) context.getApplicationContext()).getTipoVendedor();
        if (tipoVendedor.equals(VendedorModel.TIPO_TRANSPORTISTA)) {
            where = "flag <> ? ";
            args = new String[]{"P"};
        }

        String table = TablesHelper.PedidoCabecera.Table;

        try {
            SQLiteDatabase db = getWritableDatabase();
            db.delete(table, where, args);

            Log.i(TAG, table + " eliminados todos menos flag (P)");
            return true;

        } catch (SQLiteException e) {
            Log.i(TAG, table + " SQLITE Exception al eliminar");
            e.printStackTrace();
            return false;
        }

    }

    /*Metodos para cuando se detecte un nuevo número de guía*/
    public boolean eliminarDetallePedidoPendientes() {
        String where = "numeroPedido in (select numeroPedido from PedidoCabecera where flag = ?) ";
        String[] args = {"P"};

        String table = TablesHelper.PedidoDetalle.Table;

        try {
            SQLiteDatabase db = getWritableDatabase();
            db.delete(table, where, args);

            Log.i(TAG, table + " Eliminado todos los pendientes por nueva guia");
            return true;

        } catch (SQLiteException e) {
            Log.i(TAG, table + " eliminarDetallePedidoPendientes");
            e.printStackTrace();
            return false;
        }

    }

    public boolean eliminarCabeceraPedidoPendientes() throws SQLiteException {
        String where = "flag = ? ";
        String[] args = {"P"};

        String table = TablesHelper.PedidoCabecera.Table;

        try {
            SQLiteDatabase db = getWritableDatabase();
            db.delete(table, where, args);

            Log.i(TAG, table + " Eliminado todos los pendientes por nueva guia");
            return true;

        } catch (SQLiteException e) {
            Log.i(TAG, table + " eliminarCabeceraPedidoPendientes");
            e.printStackTrace();
            return false;
        }

    }

    public void actualizarObjDevolucion(JSONArray jArray) throws JSONException, SQLiteException {
        JSONObject jsonData = null;
        ContentValues cv = new ContentValues();
        String table = TablesHelper.ObjDevolucion.Table;
        String tableDetalle = TablesHelper.DevolucionDetalle.Table;
        String tableCabecera = TablesHelper.DevolucionCabecera.Table;

        getReadableDatabase().delete(tableDetalle, null, null);
        getReadableDatabase().delete(tableCabecera, null, null);

        String pkeyName = TablesHelper.DevolucionCabecera.PKeyName;
        String fkVendedor = TablesHelper.DevolucionCabecera.FKVendedor;
        String fechaDevolucion = TablesHelper.DevolucionCabecera.FechaDevolucion;
        String flag = TablesHelper.PedidoCabecera.Flag;

        SQLiteDatabase db = getWritableDatabase();

        db.beginTransaction();

        try {

            String fkeyPedido = TablesHelper.DevolucionDetalle.PKeyName;
            String fkProducto = TablesHelper.DevolucionDetalle.FKProducto;
            String fkUnidadMayor = TablesHelper.DevolucionDetalle.FKUnidadMayor;
            String cantidadUnidadMayor = TablesHelper.DevolucionDetalle.CantidadUnidadMayor;
            String fkUnidadMenor = TablesHelper.DevolucionDetalle.FKUnidadMenor;
            String cantidadUnidadMenor = TablesHelper.DevolucionDetalle.CantidadUnidadMenor;

            for (int i = 0; i < jArray.length(); i++) {
                // Devolucion Cabecera
                jsonData = jArray.getJSONObject(i);
                cv.put(pkeyName, jsonData.getString(pkeyName).trim());
                cv.put(fkVendedor, jsonData.getString(fkVendedor).trim());
                cv.put(fechaDevolucion, jsonData.getString(fechaDevolucion).trim());
                cv.put(flag, jsonData.getString(flag).trim());

                //validacion para insertar detalle
                if (db.insert(tableCabecera, null, cv) != -1) {
                    JSONArray json_detalles = new JSONArray(jsonData.getString("detalles"));

                    for (int j = 0; j < json_detalles.length(); j++) {

                        JSONObject jsonDataDetalle = json_detalles.getJSONObject(j);
                        ContentValues cv2 = new ContentValues();

                        cv2.put(fkeyPedido, jsonDataDetalle.getString(fkeyPedido).trim());
                        cv2.put(fkProducto, jsonDataDetalle.getString(fkProducto).trim());
                        cv2.put(fkUnidadMayor, jsonDataDetalle.getString(fkUnidadMayor).trim());
                        cv2.put(cantidadUnidadMayor, jsonDataDetalle.getString(cantidadUnidadMayor).trim());
                        cv2.put(fkUnidadMenor, jsonDataDetalle.getString(fkUnidadMenor).trim());
                        cv2.put(cantidadUnidadMenor, jsonDataDetalle.getString(cantidadUnidadMenor).trim());
                        cv2.put(TablesHelper.DevolucionDetalle.Modificado, 0);//Registrar como si no hubiese tenido modificaciones
                        cv2.put(TablesHelper.DevolucionDetalle.Flag, jsonData.getString(flag).trim());//Registrar con el flag E

                        db.insert(tableDetalle, null, cv2);
                    }
                } else {
                    Log.d(TAG, jsonData.getString(pkeyName) + "Devolución error y no se insertó su detalle");
                }

            }

            db.setTransactionSuccessful();

        } catch (JSONException e) {
            Log.i(TAG, table + " - JSON Exception ");
            e.printStackTrace();

        } catch (SQLiteException e) {
            Log.i(TAG, table + " - SQLITE Exception");
            e.printStackTrace();

        } finally {
            db.endTransaction();
        }

    }

    public void actualizarAvanceCuota(JSONArray jArray) {
        JSONObject jsonData = null;
        ContentValues cv;
        String table = TablesHelper.AvanceCuota.Table;
        String pkAvance = TablesHelper.AvanceCuota.PKeyName;
        String nombre = TablesHelper.AvanceCuota.Nombre;
        String totalPaquetes = TablesHelper.AvanceCuota.TotalPaquetes;
        String cuotaDia = TablesHelper.AvanceCuota.CuotaDia;
        String cajasFaltantes = TablesHelper.AvanceCuota.CajasFaltantes;
        String status = TablesHelper.AvanceCuota.Status;

        getReadableDatabase().delete(table, null, null);
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            for (int i = 0; i < jArray.length(); i++) {
                jsonData = jArray.getJSONObject(i);
                cv = new ContentValues();
                cv.put(pkAvance, jsonData.getString(pkAvance));
                cv.put(nombre, jsonData.getString(nombre));
                cv.put(totalPaquetes, jsonData.getInt(totalPaquetes));
                cv.put(cuotaDia, jsonData.getInt(cuotaDia));
                cv.put(cajasFaltantes, jsonData.getInt(cajasFaltantes));
                cv.put(status, jsonData.getString(status).trim());

                db.insert(table, null, cv);
            }
            db.setTransactionSuccessful();
            Log.i(TAG, table + ": BD Actualizada");
        } catch (JSONException e) {
            Log.e(TAG, table + ":" + e.getMessage());
            throw new RuntimeException(e);
        } finally {
            db.endTransaction();
        }
    }

    /********************************************************** ENCUESTA *****************************************************************/
    // region ACTUALIZAR TABLAS PARA ENCUESTAS
    public void actualizarEncuesta(JSONArray jArray) {
        JSONObject jsonData = null;
        ContentValues cv = new ContentValues();
        String table = TablesHelper.Encuesta.Table;
        String pkEncuesta = TablesHelper.Encuesta.PKeyName;
        String descripcion = TablesHelper.Encuesta.Descripcion;
        String fkTipoEncuesta = TablesHelper.Encuesta.FKTipoEncuesta;

        getReadableDatabase().delete(table, null, null);
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            for (int i = 0; i < jArray.length(); i++) {
                jsonData = jArray.getJSONObject(i);
                cv.put(pkEncuesta, jsonData.getInt(pkEncuesta));
                cv.put(descripcion, jsonData.getString(descripcion).trim());
                cv.put(fkTipoEncuesta, jsonData.getString(fkTipoEncuesta).trim());

                db.insert(table, null, cv);
            }
            db.setTransactionSuccessful();
            Log.i(TAG, table + ": BD Actualizada");
        } catch (JSONException e) {
            Log.e(TAG, table + ":" + e.getMessage());
            throw new RuntimeException(e);
        } finally {
            db.endTransaction();
        }
    }

    public void actualizarEncuestaDetalle(JSONArray jArray) {
        JSONObject jsonData = null;
        ContentValues cv = new ContentValues();
        String table = TablesHelper.EncuestaDetalle.Table;
        String pkEncuesta = TablesHelper.EncuestaDetalle.PKeyName;
        String pkEncuestaDetalle = TablesHelper.EncuestaDetalle.PKEncuestaDetalle;
        String fechaInicio = TablesHelper.EncuestaDetalle.FechaInicio;
        String fechaFin = TablesHelper.EncuestaDetalle.FechaFin;
        String clientesObligatorios = TablesHelper.EncuestaDetalle.ClientesObligatorios;
        String clientesAnonimos = TablesHelper.EncuestaDetalle.ClientesAnonimos;
        String encuestasMinimas = TablesHelper.EncuestaDetalle.EncuestasMinimas;
        String fotosMinimas = TablesHelper.EncuestaDetalle.FotosMinimas;
        String maximoIntentosCliente = TablesHelper.EncuestaDetalle.MaximoIntentosCliente;
        String filtroOcasion = TablesHelper.EncuestaDetalle.FiltroOcasion;
        String filtroCanalVentas = TablesHelper.EncuestaDetalle.FiltroCanalVentas;
        String filtroGiro = TablesHelper.EncuestaDetalle.FiltroGiro;
        String filtroSubGiro = TablesHelper.EncuestaDetalle.FiltroSubGiro;
        String porCliente = TablesHelper.EncuestaDetalle.PorCliente;
        String porSegmento = TablesHelper.EncuestaDetalle.PorSegmento;

        getReadableDatabase().delete(table, null, null);
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            for (int i = 0; i < jArray.length(); i++) {
                jsonData = jArray.getJSONObject(i);
                cv.put(pkEncuesta, jsonData.getInt(pkEncuesta));
                cv.put(pkEncuestaDetalle, jsonData.getInt(pkEncuestaDetalle));
                cv.put(fechaInicio, jsonData.getString(fechaInicio).trim());
                cv.put(fechaFin, jsonData.getString(fechaFin).trim());
                cv.put(clientesObligatorios, jsonData.getString(clientesObligatorios).trim());
                cv.put(clientesAnonimos, jsonData.getString(clientesAnonimos).trim());
                cv.put(encuestasMinimas, jsonData.getString(encuestasMinimas).trim());
                cv.put(fotosMinimas, jsonData.getString(fotosMinimas).trim());
                cv.put(maximoIntentosCliente, jsonData.getString(maximoIntentosCliente).trim());
                cv.put(filtroOcasion, jsonData.getString(filtroOcasion).trim());
                cv.put(filtroCanalVentas, jsonData.getString(filtroCanalVentas).trim());
                cv.put(filtroGiro, jsonData.getString(filtroGiro).trim());
                cv.put(filtroSubGiro, jsonData.getString(filtroSubGiro).trim());
                cv.put(porCliente, jsonData.getString(porCliente).trim());
                cv.put(porSegmento, jsonData.getString(porSegmento).trim());

                db.insert(table, null, cv);
            }
            db.setTransactionSuccessful();
            Log.i(TAG, table + ": BD Actualizada");
        } catch (JSONException e) {
            Log.e(TAG, table + ":" + e.getMessage());
            throw new RuntimeException(e);
        } finally {
            db.endTransaction();
        }
    }

    public void actualizarEncuestaDetallePregunta(JSONArray jArray) {
        JSONObject jsonData = null;
        ContentValues cv = new ContentValues();
        String table = TablesHelper.EncuestaDetallePregunta.Table;
        String pkEncuesta = TablesHelper.EncuestaDetallePregunta.PKeyName;
        String pkEncuestaDetalle = TablesHelper.EncuestaDetallePregunta.PKEncuestaDetalle;
        String pkPregunta = TablesHelper.EncuestaDetallePregunta.PKPregunta;
        String pregunta = TablesHelper.EncuestaDetallePregunta.Pregunta;
        String orden = TablesHelper.EncuestaDetallePregunta.Orden;
        String fkTipoRespuesta = TablesHelper.EncuestaDetallePregunta.FKTipoRespuesta;
        String requerido = TablesHelper.EncuestaDetallePregunta.Requerido;

        getReadableDatabase().delete(table, null, null);
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            for (int i = 0; i < jArray.length(); i++) {
                jsonData = jArray.getJSONObject(i);
                cv.put(pkEncuesta, jsonData.getInt(pkEncuesta));
                cv.put(pkEncuestaDetalle, jsonData.getInt(pkEncuestaDetalle));
                cv.put(pkPregunta, jsonData.getInt(pkPregunta));
                cv.put(pregunta, jsonData.getString(pregunta).trim());
                cv.put(orden, jsonData.getString(orden).trim());
                cv.put(fkTipoRespuesta, jsonData.getString(fkTipoRespuesta).trim());
                cv.put(requerido, jsonData.getString(requerido).trim());

                db.insert(table, null, cv);
            }
            db.setTransactionSuccessful();
            Log.i(TAG, table + ": BD Actualizada");
        } catch (JSONException e) {
            Log.e(TAG, table + ":" + e.getMessage());
            throw new RuntimeException(e);
        } finally {
            db.endTransaction();
        }
    }

    public void actualizarEncuestaAlternativaPregunta(JSONArray jArray) {
        JSONObject jsonData = null;
        ContentValues cv = new ContentValues();
        String table = TablesHelper.EncuestaAlternativaPregunta.Table;
        String pkEncuesta = TablesHelper.EncuestaAlternativaPregunta.PKeyName;
        String pkEncuestaDetalle = TablesHelper.EncuestaAlternativaPregunta.PKEncuestaDetalle;
        String pkPregunta = TablesHelper.EncuestaAlternativaPregunta.PKPregunta;
        String pkAlternativa = TablesHelper.EncuestaAlternativaPregunta.PKAlternativa;
        String alternativa = TablesHelper.EncuestaAlternativaPregunta.Alternativa;
        String orden = TablesHelper.EncuestaAlternativaPregunta.Orden;

        getReadableDatabase().delete(table, null, null);
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            for (int i = 0; i < jArray.length(); i++) {
                jsonData = jArray.getJSONObject(i);
                cv.put(pkEncuesta, jsonData.getInt(pkEncuesta));
                cv.put(pkEncuestaDetalle, jsonData.getInt(pkEncuestaDetalle));
                cv.put(pkPregunta, jsonData.getInt(pkPregunta));
                cv.put(pkAlternativa, jsonData.getInt(pkAlternativa));
                cv.put(alternativa, jsonData.getString(alternativa).trim());
                cv.put(orden, jsonData.getString(orden).trim());

                db.insert(table, null, cv);
            }
            db.setTransactionSuccessful();
            Log.i(TAG, table + ": BD Actualizada");
        } catch (JSONException e) {
            Log.e(TAG, table + ":" + e.getMessage());
            throw new RuntimeException(e);
        } finally {
            db.endTransaction();
        }
    }

    public void actualizarEncuestaDetallexCliente(JSONArray jArray) {
        JSONObject jsonData = null;
        ContentValues cv = new ContentValues();
        String table = TablesHelper.EncuestaDetallexCliente.Table;
        String pkEncuesta = TablesHelper.EncuestaDetallexCliente.PKeyName;
        String pkEncuestaDetalle = TablesHelper.EncuestaDetallexCliente.PKEncuestaDetalle;
        String pkCliente = TablesHelper.EncuestaDetallexCliente.PKCliente;

        getReadableDatabase().delete(table, null, null);
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            for (int i = 0; i < jArray.length(); i++) {
                jsonData = jArray.getJSONObject(i);
                cv.put(pkEncuesta, jsonData.getInt(pkEncuesta));
                cv.put(pkEncuestaDetalle, jsonData.getInt(pkEncuestaDetalle));
                cv.put(pkCliente, jsonData.getString(pkCliente).trim());

                db.insert(table, null, cv);
            }
            db.setTransactionSuccessful();
            Log.i(TAG, table + ": BD Actualizada");
        } catch (JSONException e) {
            Log.e(TAG, table + ":" + e.getMessage());
            throw new RuntimeException(e);
        } finally {
            db.endTransaction();
        }
    }

    public void actualizarEncuestaDetallexSegmento(JSONArray jArray) {
        JSONObject jsonData = null;
        ContentValues cv = new ContentValues();
        String table = TablesHelper.EncuestaDetallexSegmento.Table;
        String pkEncuesta = TablesHelper.EncuestaDetallexSegmento.PKeyName;
        String pkEncuestaDetalle = TablesHelper.EncuestaDetallexSegmento.PKEncuestaDetalle;
        String pkSegmentoCliente = TablesHelper.EncuestaDetallexSegmento.PKSegmentoCliente;

        getReadableDatabase().delete(table, null, null);
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            for (int i = 0; i < jArray.length(); i++) {
                jsonData = jArray.getJSONObject(i);
                cv.put(pkEncuesta, jsonData.getInt(pkEncuesta));
                cv.put(pkEncuestaDetalle, jsonData.getInt(pkEncuestaDetalle));
                cv.put(pkSegmentoCliente, jsonData.getString(pkSegmentoCliente).trim());

                db.insert(table, null, cv);
            }
            db.setTransactionSuccessful();
            Log.i(TAG, table + ": BD Actualizada");
        } catch (JSONException e) {
            Log.e(TAG, table + ":" + e.getMessage());
            throw new RuntimeException(e);
        } finally {
            db.endTransaction();
        }
    }

    public void actualizarEncuestaTipo(JSONArray jArray) {
        JSONObject jsonData = null;
        ContentValues cv = new ContentValues();
        String table = TablesHelper.EncuestaTipo.Table;
        String pkEncuestaTipo = TablesHelper.EncuestaTipo.PKeyName;
        String descripcion = TablesHelper.EncuestaTipo.Descripcion;

        getReadableDatabase().delete(table, null, null);
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            for (int i = 0; i < jArray.length(); i++) {
                jsonData = jArray.getJSONObject(i);
                cv.put(pkEncuestaTipo, jsonData.getString(pkEncuestaTipo));
                cv.put(descripcion, jsonData.getString(descripcion).trim());

                db.insert(table, null, cv);
            }
            db.setTransactionSuccessful();
            Log.i(TAG, table + ": BD Actualizada");
        } catch (JSONException e) {
            Log.e(TAG, table + ":" + e.getMessage());
            throw new RuntimeException(e);
        } finally {
            db.endTransaction();
        }
    }

    public void actualizarEncuestaRespuestaCabecera(JSONArray jArray) {
        JSONObject jsonData = null;
        ContentValues cv;
        String table = TablesHelper.EncuestaRespuestaCabecera.Table;
        String pkEncuesta = TablesHelper.EncuestaRespuestaCabecera.PKEncuesta;
        String pkEncuestaDetalle = TablesHelper.EncuestaRespuestaCabecera.PKEncuestaDetalle;
        String pkCliente = TablesHelper.EncuestaRespuestaCabecera.PKCliente;
        String fkVendedor = TablesHelper.EncuestaRespuestaCabecera.FKVendedor;
        String fecha = TablesHelper.EncuestaRespuestaCabecera.Fecha;
        String flag = TablesHelper.EncuestaRespuestaCabecera.Flag;

        eliminarEncuestaRespuestaCabecera();
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            for (int i = 0; i < jArray.length(); i++) {
                jsonData = jArray.getJSONObject(i);
                cv = new ContentValues();
                cv.put(pkEncuesta, jsonData.getInt(pkEncuesta));
                cv.put(pkEncuestaDetalle, jsonData.getInt(pkEncuestaDetalle));
                cv.put(pkCliente, jsonData.getString(pkCliente).trim());
                cv.put(fkVendedor, jsonData.getString(fkVendedor).trim());
                cv.put(fecha, jsonData.getString(fecha).trim());
                cv.put(flag, jsonData.getString(flag).trim());

                db.insert(table, null, cv);
            }
            db.setTransactionSuccessful();
            Log.i(TAG, table + ": BD Actualizada");
        } catch (JSONException e) {
            Log.e(TAG, table + ":" + e.getMessage());
            throw new RuntimeException(e);
        } finally {
            db.endTransaction();
        }
    }

    public void actualizarEncuestaRespuestaDetalle(JSONArray jArray) {
        JSONObject jsonData = null;
        ContentValues cv;
        String table = TablesHelper.EncuestaRespuestaDetalle.Table;
        String pkEncuesta = TablesHelper.EncuestaRespuestaDetalle.PKEncuesta;
        String pkEncuestaDetalle = TablesHelper.EncuestaRespuestaDetalle.PKEncuestaDetalle;
        String pkCliente = TablesHelper.EncuestaRespuestaDetalle.PKCliente;
        String pkPregunta = TablesHelper.EncuestaRespuestaDetalle.PKPregunta;
        String pkAlternativas = TablesHelper.EncuestaRespuestaDetalle.PKAlternativas;
        String descripcion = TablesHelper.EncuestaRespuestaDetalle.Descripcion;
        String tipoRespuesta = TablesHelper.EncuestaRespuestaDetalle.TipoRespuesta;
        String latitud = TablesHelper.EncuestaRespuestaDetalle.Latitud;
        String longitud = TablesHelper.EncuestaRespuestaDetalle.Longitud;
        String fotoURL = TablesHelper.EncuestaRespuestaDetalle.FotoURL;

        eliminarEncuestaRespuestaDetalle();
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            for (int i = 0; i < jArray.length(); i++) {
                jsonData = jArray.getJSONObject(i);
                cv = new ContentValues();
                cv.put(pkEncuesta, jsonData.getInt(pkEncuesta));
                cv.put(pkEncuestaDetalle, jsonData.getInt(pkEncuestaDetalle));
                cv.put(pkCliente, jsonData.getString(pkCliente).trim());
                cv.put(pkPregunta, jsonData.getInt(pkPregunta));
                if (jsonData.has(pkAlternativas) && !jsonData.isNull(pkAlternativas))
                    cv.put(pkAlternativas, jsonData.getString(pkAlternativas));
                if (jsonData.has(descripcion) && !jsonData.isNull(descripcion))
                    cv.put(descripcion, jsonData.getString(descripcion));
                cv.put(tipoRespuesta, jsonData.getString(tipoRespuesta).trim());
                cv.put(latitud, jsonData.getString(latitud).trim());
                cv.put(longitud, jsonData.getString(longitud).trim());

                if (jsonData.getString(tipoRespuesta).equals(EncuestaDetallePreguntaModel.TIPO_RESPUESTA_FOTO)) {
                    String fotoName = jsonData.getString(descripcion);
                    String URL = Environment.getExternalStoragePublicDirectory(context.getResources().getString(R.string.Ventas360App_Picture)) + File.separator + fotoName;
                    cv.put(fotoURL, URL);
                }

                db.insert(table, null, cv);
            }
            db.setTransactionSuccessful();
            Log.i(TAG, table + ": BD Actualizada");
        } catch (JSONException e) {
            Log.e(TAG, table + ":" + e.getMessage());
            throw new RuntimeException(e);
        } finally {
            db.endTransaction();
        }
    }
    // endregion

    public boolean eliminarEncuestaRespuestaCabecera() throws SQLiteException {
        String where = "flag <> ? ";
        String[] args = {"P"};

        String table = TablesHelper.EncuestaRespuestaCabecera.Table;

        try {
            SQLiteDatabase db = getWritableDatabase();
            db.delete(table, where, args);

            Log.i(TAG, table + " eliminados todos menos flag (P)");
            return true;

        } catch (SQLiteException e) {
            Log.i(TAG, table + " SQLITE Exception al eliminar");
            e.printStackTrace();
            return false;
        }

    }

    public boolean eliminarEncuestaRespuestaDetalle() throws SQLiteException {
        boolean flag = true;
        DAOEncuesta daoEncuesta = new DAOEncuesta(context);
        ArrayList<EncuestaRespuestaModel> lista = daoEncuesta.getEncuestasEnviadas();

        SQLiteDatabase db = getWritableDatabase();
        for (EncuestaRespuestaModel item : lista) {
            String where = "idEncuesta = ? AND idEncuestaDetalle = ? AND idCliente = ?";
            String[] args = {String.valueOf(item.getIdEncuesta()), String.valueOf(item.getIdEncuestaDetalle()), item.getIdCliente()};

            String table = TablesHelper.EncuestaRespuestaDetalle.Table;

            try {
                db.delete(table, where, args);
                Log.i(TAG, table + " eliminados encuestas menos flag (P) " + item.getIdEncuesta() + "," + item.getIdEncuestaDetalle() + "," + item.getIdCliente());
            } catch (SQLiteException e) {
                Log.i(TAG, table + " SQLITE Exception al eliminar");
                e.printStackTrace();
                flag = false;
            }
        }
        return flag;
    }


    public void actualizarProducto(JSONArray jArray) throws Exception {
        JSONObject jsonData = null;
        ContentValues cv = new ContentValues();
        String table = TablesHelper.Producto.Table;
        String pkName = TablesHelper.Producto.PKeyName;
        String descripcion = TablesHelper.Producto.Descripcion;
        String fkLinea = TablesHelper.Producto.FKLinea;
        String fkFamilia = TablesHelper.Producto.FKFamilia;
        String peso = TablesHelper.Producto.Peso;
        String fkProveedor = TablesHelper.Producto.FKProveedor;
        String fkProductoERP = TablesHelper.Producto.FKProductoERP;
        String descripcionERP = TablesHelper.Producto.DescripcionERP;
        String tipoProducto = TablesHelper.Producto.TipoProducto;
        String fkMarca = TablesHelper.Producto.FKMarca;
        String porcentajePercepcion = TablesHelper.Producto.PorcentajePercepcion;
        String porcentajeISC = TablesHelper.Producto.PorcentajeISC;

        getReadableDatabase().delete(table, null, null);
        SQLiteDatabase db = getWritableDatabase();

        db.beginTransaction();

        try {

            for (int i = 0; i < jArray.length(); i++) {
                jsonData = jArray.getJSONObject(i);
                cv.put(pkName, jsonData.getString(pkName).trim());
                cv.put(descripcion, jsonData.getString(descripcion).trim());
                cv.put(fkLinea, jsonData.getString(fkLinea).trim());
                cv.put(fkFamilia, jsonData.getString(fkFamilia).trim());
                cv.put(peso, jsonData.getString(peso).trim());
                cv.put(fkProveedor, jsonData.getString(fkProveedor).trim());
                cv.put(fkProductoERP, jsonData.getString(fkProductoERP).trim());
                cv.put(descripcionERP, jsonData.getString(descripcionERP).trim());
                cv.put(tipoProducto, jsonData.getString(tipoProducto).trim());
                cv.put(fkMarca, jsonData.getString(fkMarca).trim());
                cv.put(porcentajePercepcion, jsonData.getString(porcentajePercepcion).trim());
                cv.put(porcentajeISC, jsonData.getString(porcentajeISC).trim());

                db.insertOrThrow(table, null, cv);
            }

            db.setTransactionSuccessful();
            Log.i(TAG, table + ": BD Actualizada");
        } catch (JSONException e) {
            Log.e(TAG, table + ":" + e.getMessage());
            throw new RuntimeException(e);
        } finally {
            db.endTransaction();
        }
    }

    public boolean isFieldExist(String tableName, String fieldName) {
        boolean isExist = false;
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("PRAGMA table_info(" + tableName + ")", null);
        res.moveToFirst();
        do {
            String currentColumn = res.getString(1);
            if (currentColumn.equals(fieldName)) {
                isExist = true;
            }
        } while (res.moveToNext());
        return isExist;
    }

    public void actualizarUnidadMedidaxProducto(JSONArray jArray) throws Exception {
        JSONObject jsonData = null;
        ContentValues cv = new ContentValues();
        String table = TablesHelper.UnidadMedidaxProducto.Table;
        String idEmpresa = TablesHelper.UnidadMedidaxProducto.FKEmpresa;
        String idProducto = TablesHelper.UnidadMedidaxProducto.FKProducto;
        String idUnidadManejo = TablesHelper.UnidadMedidaxProducto.FKUnidadManejo;
        String idUnidadContable = TablesHelper.UnidadMedidaxProducto.FKUnidadContable;
        String contenido = TablesHelper.UnidadMedidaxProducto.Contenido;

        getReadableDatabase().delete(table, null, null);
        SQLiteDatabase db = getWritableDatabase();

        db.beginTransaction();

        try {

            for (int i = 0; i < jArray.length(); i++) {
                jsonData = jArray.getJSONObject(i);
                cv.put(idEmpresa, jsonData.getString(idEmpresa).trim());
                cv.put(idProducto, jsonData.getString(idProducto).trim());
                cv.put(idUnidadManejo, jsonData.getString(idUnidadManejo).trim());
                cv.put(idUnidadContable, jsonData.getString(idUnidadContable).trim());
                cv.put(contenido, jsonData.getString(contenido).trim());

                db.insertOrThrow(table, null, cv);
            }

            db.setTransactionSuccessful();
            Log.i(TAG, table + ": BD Actualizada");
        } catch (JSONException e) {
            Log.e(TAG, table + ":" + e.getMessage());
            throw new RuntimeException(e);
        } finally {
            db.endTransaction();
        }
    }

    /*
    public void actualizarPoliticaPrecioxProducto(JSONArray jArray) {
        JSONObject jsonData = null;
        ContentValues cv = new ContentValues();
        String table = TablesHelper.PoliticaPrecioxProducto.Table;
        String fkPoliticaPrecio = TablesHelper.PoliticaPrecioxProducto.FKPoliticaPrecio;
        String fkProducto = TablesHelper.PoliticaPrecioxProducto.FKProducto;
        String precioMenor = TablesHelper.PoliticaPrecioxProducto.PrecioMenor;
        String precioMayor = TablesHelper.PoliticaPrecioxProducto.PrecioMayor;

        getReadableDatabase().delete(table, null, null);
        SQLiteDatabase db = getWritableDatabase();

        db.beginTransaction();

        try {

            for (int i = 0; i < jArray.length(); i++) {
                jsonData = jArray.getJSONObject(i);
                cv.put(fkPoliticaPrecio, jsonData.getString(fkPoliticaPrecio).trim());
                cv.put(fkProducto, jsonData.getString(fkProducto).trim());
                cv.put(precioMenor, jsonData.getString(precioMenor).trim());
                cv.put(precioMayor, jsonData.getString(precioMayor).trim());

                db.insert(table, null, cv);
            }

            db.setTransactionSuccessful();
            Log.i(TAG, table + ": BD Actualizada");
        } catch (JSONException e) {
            Log.e(TAG, table + ":" + e.getMessage());
            throw new RuntimeException(e);
        } finally {
            db.endTransaction();
        }
    }
     */

    public void actualizarPoliticaPrecioxProducto(JSONArray jArray) throws Exception {
        JSONObject jsonData = null;
        ContentValues cv = new ContentValues();
        String table = TablesHelper.PoliticaPrecioxProducto.Table;
        String idEmpresa = TablesHelper.PoliticaPrecioxProducto.FKEmpresa;
        String idPolitica = TablesHelper.PoliticaPrecioxProducto.FKPolitica;
        String idProducto = TablesHelper.PoliticaPrecioxProducto.FKProducto;
        String idUnidadManejo = TablesHelper.PoliticaPrecioxProducto.FKUnidadManejo;
        String idUnidadContenido = TablesHelper.PoliticaPrecioxProducto.FKUnidadContenido;
        String precioManejo = TablesHelper.PoliticaPrecioxProducto.PrecioManejo;
        String precioContenido = TablesHelper.PoliticaPrecioxProducto.PrecioContenido;

        getReadableDatabase().delete(table, null, null);
        SQLiteDatabase db = getWritableDatabase();

        db.beginTransaction();

        try {

            for (int i = 0; i < jArray.length(); i++) {
                jsonData = jArray.getJSONObject(i);
                cv.put(idEmpresa, jsonData.getString(idEmpresa).trim());
                cv.put(idPolitica, jsonData.getString(idPolitica).trim());
                cv.put(idProducto, jsonData.getString(idProducto).trim());
                cv.put(idUnidadManejo, jsonData.getString(idUnidadManejo).trim());
                cv.put(idUnidadContenido, jsonData.getString(idUnidadContenido).trim());
                cv.put(precioManejo, jsonData.getString(precioManejo).trim());
                cv.put(precioContenido, jsonData.getString(precioContenido).trim());

                db.insertOrThrow(table, null, cv);
            }

            db.setTransactionSuccessful();
            Log.i(TAG, table + ": BD Actualizada");
        } catch (JSONException e) {
            Log.e(TAG, table + ":" + e.getMessage());
            throw new RuntimeException(e);
        } finally {
            db.endTransaction();
        }
    }

    public void actualizarMGRUP1F(JSONArray jArray) throws Exception {
        JSONObject jsonData = null;
        ContentValues cv = new ContentValues();
        String table = TablesHelper.MGRUP1F.Table;
        String idGrupo = TablesHelper.MGRUP1F.FKMGrup1f;
        String tipoGrupo = TablesHelper.MGRUP1F.tipoGrupo;
        String descripcion = TablesHelper.MGRUP1F.descripcion;

        getReadableDatabase().execSQL("CREATE TABLE IF NOT EXISTS \"MGRUP1F\" (\"IDGRUPO\" INTEGER NOT NULL, \"DESCRIPCION\" TEXT, \"TIPOGRUPO\" INTEGER, PRIMARY KEY(\"IDGRUPO\") )");
        getReadableDatabase().delete(table, null, null);
        SQLiteDatabase db = getWritableDatabase();

        db.beginTransaction();

        try {

            for (int i = 0; i < jArray.length(); i++) {
                jsonData = jArray.getJSONObject(i);
                cv.put(idGrupo, jsonData.getString(idGrupo).trim());
                cv.put(tipoGrupo, jsonData.getString(tipoGrupo).trim());
                cv.put(descripcion, jsonData.getString(descripcion).trim());

                db.insertOrThrow(table, null, cv);
            }

            db.setTransactionSuccessful();
            Log.i(TAG, table + ": BD Actualizada");
        } catch (JSONException e) {
            Log.e(TAG, table + ":" + e.getMessage());
            throw new RuntimeException(e);
        } finally {
            db.endTransaction();
        }
    }

    public void actualizarMGRUP2F(JSONArray jArray) throws Exception {
        JSONObject jsonData = null;
        ContentValues cv = new ContentValues();
        String table = TablesHelper.MGRUP2F.Table;
        String idGrupo = TablesHelper.MGRUP2F.FKGrup2f;
        String articulo = TablesHelper.MGRUP2F.articulo;
        String mandatorio = TablesHelper.MGRUP2F.mandatorio;
        String unidades = TablesHelper.MGRUP2F.unidades;
        String malla = TablesHelper.MGRUP2F.malla;

        getReadableDatabase().execSQL("CREATE TABLE IF NOT EXISTS \"MGRUP2F\" (\"IDGRUPO\" INTEGER NOT NULL, \"ARTICULO\" INTEGER NOT NULL, \"MANDATORIO\" INTEGER, \"UNIDADES\" INTEGER, \"MALLA\" TEXT, PRIMARY KEY(\"IDGRUPO\",\"ARTICULO\") )");
        getReadableDatabase().delete(table, null, null);
        SQLiteDatabase db = getWritableDatabase();

        db.beginTransaction();

        try {

            for (int i = 0; i < jArray.length(); i++) {
                jsonData = jArray.getJSONObject(i);
                cv.put(idGrupo, jsonData.getString(idGrupo).trim());
                cv.put(articulo, jsonData.getString(articulo).trim());
                cv.put(mandatorio, jsonData.getString(mandatorio).trim());
                cv.put(unidades, jsonData.getString(unidades).trim());
                cv.put(malla, jsonData.getString(malla).trim());

                db.insertOrThrow(table, null, cv);
            }

            db.setTransactionSuccessful();
            Log.i(TAG, table + ": BD Actualizada");
        } catch (JSONException e) {
            Log.e(TAG, table + ":" + e.getMessage());
            throw new RuntimeException(e);
        } finally {
            db.endTransaction();
        }
    }

    public void actualizarMPROMO1F(JSONArray jArray) throws Exception {
        JSONObject jsonData = null;
        ContentValues cv = new ContentValues();
        String table = TablesHelper.MPROMO1F.Table;
        String idPromocion = TablesHelper.MPROMO1F.FKPromo;
        String descripcion = TablesHelper.MPROMO1F.descripcion;
        String fecini = TablesHelper.MPROMO1F.fecini;
        String fecfin = TablesHelper.MPROMO1F.fecfin;
        String condicion = TablesHelper.MPROMO1F.condicion;
        String estado = TablesHelper.MPROMO1F.estado;
        String orden = TablesHelper.MPROMO1F.orden;
        String mecanica = TablesHelper.MPROMO1F.mecanica;
        String malla = TablesHelper.MPROMO1F.malla;

        getReadableDatabase().execSQL("CREATE TABLE IF NOT EXISTS \"MPROMO1F\" (\"IDPROMOCION\" INTEGER NOT NULL, \"DESCRIPCION\" TEXT, \"FECINI\" INTEGER, \"FECFIN\" INTEGER, \"CONDICION\" TEXT, \"ESTADO\" TEXT, \"ORDEN\" INTEGER, \"MECANICA\" TEXT, \"MALLA\" TEXT, PRIMARY KEY(\"IDPROMOCION\") )");
        getReadableDatabase().delete(table, null, null);
        SQLiteDatabase db = getWritableDatabase();

        db.beginTransaction();

        try {

            for (int i = 0; i < jArray.length(); i++) {
                jsonData = jArray.getJSONObject(i);
                cv.put(idPromocion, jsonData.getString(idPromocion).trim());
                cv.put(descripcion, jsonData.getString(descripcion).trim());
                cv.put(fecini, jsonData.getString(fecini).trim());
                cv.put(fecfin, jsonData.getString(fecfin).trim());
                cv.put(condicion, jsonData.getString(condicion).trim());
                cv.put(estado, jsonData.getString(estado).trim());
                cv.put(orden, jsonData.getString(orden).trim());
                cv.put(mecanica, jsonData.getString(mecanica).trim());
                cv.put(malla, jsonData.getString(malla).trim());

                db.insertOrThrow(table, null, cv);
            }

            db.setTransactionSuccessful();
            Log.i(TAG, table + ": BD Actualizada");
        } catch (JSONException e) {
            Log.e(TAG, table + ":" + e.getMessage());
            throw new RuntimeException(e);
        } finally {
            db.endTransaction();
        }
    }

    public void actualizarMPROMO2F(JSONArray jArray) throws Exception {
        JSONObject jsonData = null;
        ContentValues cv = new ContentValues();
        String table = TablesHelper.MPROMO2F.Table;
        String idPromocion = TablesHelper.MPROMO2F.FKPromo;
        String idGrupo = TablesHelper.MPROMO2F.idGrupo;

        getReadableDatabase().execSQL("CREATE TABLE IF NOT EXISTS \"MPROMO2F\" (\"IDPROMOCION\" INTEGER NOT NULL, \"IDGRUPO\" INTEGER NOT NULL, PRIMARY KEY(\"IDPROMOCION\",\"IDGRUPO\") )");
        getReadableDatabase().delete(table, null, null);
        SQLiteDatabase db = getWritableDatabase();

        db.beginTransaction();

        try {

            for (int i = 0; i < jArray.length(); i++) {
                jsonData = jArray.getJSONObject(i);
                cv.put(idPromocion, jsonData.getString(idPromocion).trim());
                cv.put(idGrupo, jsonData.getString(idGrupo).trim());

                db.insertOrThrow(table, null, cv);
            }

            db.setTransactionSuccessful();
            Log.i(TAG, table + ": BD Actualizada");
        } catch (JSONException e) {
            Log.e(TAG, table + ":" + e.getMessage());
            throw new RuntimeException(e);
        } finally {
            db.endTransaction();
        }
    }

    public void actualizarMPROMO3F(JSONArray jArray) throws Exception {
        JSONObject jsonData = null;
        ContentValues cv = new ContentValues();
        String table = TablesHelper.MPROMO3F.Table;
        String idPromocion = TablesHelper.MPROMO3F.FKPromo;
        String idGrupo = TablesHelper.MPROMO3F.idGrupo;
        String idRango = TablesHelper.MPROMO3F.idRango;
        String unidad = TablesHelper.MPROMO3F.unidad;
        String desde = TablesHelper.MPROMO3F.desde;
        String hasta = TablesHelper.MPROMO3F.hasta;
        String porcada = TablesHelper.MPROMO3F.porcada;

        getReadableDatabase().execSQL("CREATE TABLE IF NOT EXISTS \"MPROMO3F\" (\"IDPROMOCION\" INTEGER NOT NULL, \"IDGRUPO\" INTEGER NOT NULL, \"IDRANGO\" INTEGER NOT NULL, \"UNIDAD\" TEXT, \"DESDE\" TEXT, \"HASTA\" TEXT, \"PORCADA\" REAL, PRIMARY KEY(\"IDPROMOCION\",\"IDGRUPO\",\"IDRANGO\") )");
        getReadableDatabase().delete(table, null, null);
        SQLiteDatabase db = getWritableDatabase();

        db.beginTransaction();

        try {

            for (int i = 0; i < jArray.length(); i++) {
                jsonData = jArray.getJSONObject(i);
                cv.put(idPromocion, jsonData.getString(idPromocion).trim());
                cv.put(idGrupo, jsonData.getString(idGrupo).trim());
                cv.put(idRango, jsonData.getString(idRango).trim());
                cv.put(unidad, jsonData.getString(unidad).trim());
                cv.put(desde, jsonData.getString(desde).trim());
                cv.put(hasta, jsonData.getString(hasta).trim());
                cv.put(porcada, jsonData.getString(porcada).trim());

                db.insertOrThrow(table, null, cv);
            }

            db.setTransactionSuccessful();
            Log.i(TAG, table + ": BD Actualizada");
        } catch (JSONException e) {
            Log.e(TAG, table + ":" + e.getMessage());
            throw new RuntimeException(e);
        } finally {
            db.endTransaction();
        }
    }

    public void actualizarMPROMO4F(JSONArray jArray) throws Exception {
        JSONObject jsonData = null;
        ContentValues cv = new ContentValues();
        String table = TablesHelper.MPROMO4F.Table;
        String idAccion = TablesHelper.MPROMO4F.FKAccion;
        String descripcion = TablesHelper.MPROMO4F.descripcion;
        String articulo = TablesHelper.MPROMO4F.articulo;
        String unidad = TablesHelper.MPROMO4F.unidad;

        getReadableDatabase().execSQL("CREATE TABLE IF NOT EXISTS \"MPROMO4F\" (\"IDACCION\" INTEGER NOT NULL, \"DESCRIPCION\" TEXT, \"ARTICULO\" INTEGER, \"UNIDAD\" REAL, PRIMARY KEY(\"IDACCION\") )");
        getReadableDatabase().delete(table, null, null);
        SQLiteDatabase db = getWritableDatabase();

        db.beginTransaction();

        try {

            for (int i = 0; i < jArray.length(); i++) {
                jsonData = jArray.getJSONObject(i);
                cv.put(idAccion, jsonData.getString(idAccion).trim());
                cv.put(descripcion, jsonData.getString(descripcion).trim());
                cv.put(articulo, jsonData.getString(articulo).trim());
                cv.put(unidad, jsonData.getString(unidad).trim());

                db.insertOrThrow(table, null, cv);
            }

            db.setTransactionSuccessful();
            Log.i(TAG, table + ": BD Actualizada");
        } catch (JSONException e) {
            Log.e(TAG, table + ":" + e.getMessage());
            throw new RuntimeException(e);
        } finally {
            db.endTransaction();
        }
    }

    public void actualizarMPROMO5F(JSONArray jArray) throws Exception {
        JSONObject jsonData = null;
        ContentValues cv = new ContentValues();
        String table = TablesHelper.MPROMO5F.Table;
        String idPromocion = TablesHelper.MPROMO5F.FKPromo;
        String idAccion = TablesHelper.MPROMO5F.idAccion;
        String mecanica = TablesHelper.MPROMO5F.mecanica;

        getReadableDatabase().execSQL("DROP TABLE IF EXISTS \"MPROMO5F\"");
        getReadableDatabase().execSQL("CREATE TABLE IF NOT EXISTS \"MPROMO5F\" (\"IDPROMOCION\" INTEGER NOT NULL, \"IDACCION\" INTEGER NOT NULL, \"MECANICA\" TEXT)");
        getReadableDatabase().delete(table, null, null);
        SQLiteDatabase db = getWritableDatabase();

        db.beginTransaction();

        try {

            for (int i = 0; i < jArray.length(); i++) {
                jsonData = jArray.getJSONObject(i);
                cv.put(idPromocion, jsonData.getString(idPromocion).trim());
                cv.put(idAccion, jsonData.getString(idAccion).trim());
                cv.put(mecanica, jsonData.getString(mecanica).trim());

                db.insertOrThrow(table, null, cv);
            }

            db.setTransactionSuccessful();
            Log.i(TAG, table + ": BD Actualizada");
        } catch (JSONException e) {
            Log.e(TAG, table + ":" + e.getMessage());
            throw new RuntimeException(e);
        } finally {
            db.endTransaction();
        }
    }

    public void actualizarMPROMO6F(JSONArray jArray) throws Exception {
        JSONObject jsonData = null;
        ContentValues cv = new ContentValues();
        String table = TablesHelper.MPROMO6F.Table;
        String idPromocion = TablesHelper.MPROMO6F.FKPromo;
        String sucursal = TablesHelper.MPROMO6F.sucursal;
        String fdesde = TablesHelper.MPROMO6F.fdesde;
        String fhasta = TablesHelper.MPROMO6F.fhasta;
        String ftermino = TablesHelper.MPROMO6F.ftermino;
        String fecCrea = TablesHelper.MPROMO6F.fecCrea;
        String horCrea = TablesHelper.MPROMO6F.horCrea;
        String usuCrea = TablesHelper.MPROMO6F.usuCrea;
        String fecUltmod = TablesHelper.MPROMO6F.fecUltmod;
        String horUltmod = TablesHelper.MPROMO6F.horUltmod;
        String usUultmod = TablesHelper.MPROMO6F.usUultmod;

        //getReadableDatabase().execSQL("DROP TABLE \"MPROMO6F\"");
        getReadableDatabase().execSQL("CREATE TABLE IF NOT EXISTS \"MPROMO6F\" (\"IDPROMOCION\" INTEGER NOT NULL, \"SUCURSAL\" TEXT , \"FDESDE\" Timestamp , \"FHASTA\" Timestamp , \"FTERMINO\" Timestamp , \"FECCREA\" Timestamp , \"HORCREA\" TEXT, \"USUCREA\" TEXT , \"FECULTMOD\" Timestamp, \"HORULTMOD\" TEXT, \"USUULTMOD\" TEXT , PRIMARY KEY(\"IDPROMOCION\") )");
        getReadableDatabase().delete(table, null, null);
        SQLiteDatabase db = getWritableDatabase();

        db.beginTransaction();

        try {

            for (int i = 0; i < jArray.length(); i++) {
                jsonData = jArray.getJSONObject(i);
                cv.put(sucursal, jsonData.getString(sucursal).trim());
                cv.put(fdesde, jsonData.getString(fdesde).trim());
                cv.put(fhasta, jsonData.getString(fhasta).trim());
                cv.put(ftermino, jsonData.getString(ftermino).trim());
                cv.put(fecCrea, jsonData.getString(fecCrea).trim());
                cv.put(horCrea, jsonData.getString(horCrea).trim());
                cv.put(usuCrea, jsonData.getString(usuCrea).trim());
                cv.put(fecUltmod, jsonData.getString(fecUltmod).trim());
                cv.put(horUltmod, jsonData.getString(horUltmod).trim());
                cv.put(usUultmod, jsonData.getString(usUultmod).trim());

                db.insertOrThrow(table, null, cv);
            }

            db.setTransactionSuccessful();
            Log.i(TAG, table + ": BD Actualizada");
        } catch (JSONException e) {
            Log.e(TAG, table + ":" + e.getMessage());
            throw new RuntimeException(e);
        } finally {
            db.endTransaction();
        }
    }

    public void actualizarRutasxPersona(JSONArray jArray) throws Exception {
        JSONObject jsonData = null;
        ContentValues cv = new ContentValues();
        String table = TablesHelper.RutasxPersona.Table;
        String idPersona = TablesHelper.RutasxPersona.idPersona;
        String idRuta = TablesHelper.RutasxPersona.idRuta;

        getReadableDatabase().execSQL("DROP TABLE IF EXISTS \"RutasxPersona\"");
        getReadableDatabase().execSQL("CREATE TABLE IF NOT EXISTS \"RutasxPersona\" (\"idPersona\" INTEGER NOT NULL, \"idRuta\" INTEGER NOT NULL)");
        getReadableDatabase().delete(table, null, null);
        SQLiteDatabase db = getWritableDatabase();

        db.beginTransaction();

        try {

            for (int i = 0; i < jArray.length(); i++) {
                jsonData = jArray.getJSONObject(i);
                cv.put(idPersona, jsonData.getInt(idPersona));
                cv.put(idRuta, jsonData.getInt(idRuta));

                db.insertOrThrow(table, null, cv);
            }

            db.setTransactionSuccessful();
            Log.i(TAG, table + ": BD Actualizada");
        } catch (JSONException e) {
            Log.e(TAG, table + ":" + e.getMessage());
            throw new RuntimeException(e);
        } finally {
            db.endTransaction();
        }
    }

    public void actualizarModuloxRuta(JSONArray jArray) throws Exception {
        JSONObject jsonData = null;
        ContentValues cv = new ContentValues();
        String table = TablesHelper.ModuloxRuta.Table;
        String idModulo = TablesHelper.ModuloxRuta.idModulo;
        String idRuta = TablesHelper.ModuloxRuta.idRuta;

        getReadableDatabase().execSQL("DROP TABLE IF EXISTS \"ModuloxRuta\"");
        getReadableDatabase().execSQL("CREATE TABLE IF NOT EXISTS \"ModuloxRuta\" (\"idModulo\" INTEGER NOT NULL, \"idRuta\" INTEGER NOT NULL)");
        getReadableDatabase().delete(table, null, null);
        SQLiteDatabase db = getWritableDatabase();

        db.beginTransaction();

        try {

            for (int i = 0; i < jArray.length(); i++) {
                jsonData = jArray.getJSONObject(i);
                cv.put(idModulo, jsonData.getInt(idModulo));
                cv.put(idRuta, jsonData.getInt(idRuta));

                db.insertOrThrow(table, null, cv);
            }

            db.setTransactionSuccessful();
            Log.i(TAG, table + ": BD Actualizada");
        } catch (JSONException e) {
            Log.e(TAG, table + ":" + e.getMessage());
            throw new RuntimeException(e);
        } finally {
            db.endTransaction();
        }
    }

    public void actualizarClienteWathsapp(JSONArray jArray) throws Exception {
        JSONObject jsonData = null;
        ContentValues cv = new ContentValues();
        String table = TablesHelper.ClienteWathsapp.Table;
        String idCliente = TablesHelper.ClienteWathsapp.idCliente;
        String codigociudad = TablesHelper.ClienteWathsapp.codigociudad;
        String email = TablesHelper.ClienteWathsapp.email;
        String fechaRegistro = TablesHelper.ClienteWathsapp.fechaRegistro;
        String telefonofijo = TablesHelper.ClienteWathsapp.telefonofijo;
        String whathsapp = TablesHelper.ClienteWathsapp.whathsapp;

        getReadableDatabase().execSQL("DROP TABLE IF EXISTS \"ClienteWathsapp\"");
        getReadableDatabase().execSQL("CREATE TABLE IF NOT EXISTS \"ClienteWathsapp\" (\"idCliente\" VARCHAR(9) NOT NULL, \"whathsapp\" VARCHAR(9)   NOT NULL, \"codigociudad\" VARCHAR(3)   NOT NULL, \"telefonofijo\" VARCHAR(7)   NOT NULL, \"email\" VARCHAR(50)   NOT NULL, \"fechaRegistro\" DATETIME  NOT NULL)");
        getReadableDatabase().delete(table, null, null);
        SQLiteDatabase db = getWritableDatabase();

        db.beginTransaction();

        try {

            for (int i = 0; i < jArray.length(); i++) {
                jsonData = jArray.getJSONObject(i);
                cv.put(idCliente, jsonData.getString(idCliente));
                cv.put(whathsapp, jsonData.getString(whathsapp));
                cv.put(codigociudad, jsonData.getString(codigociudad));
                cv.put(telefonofijo, jsonData.getString(telefonofijo));
                cv.put(email, jsonData.getString(email));
                cv.put(fechaRegistro, jsonData.getString(fechaRegistro));

                db.insertOrThrow(table, null, cv);
            }

            db.setTransactionSuccessful();
            Log.i(TAG, table + ": BD Actualizada");
        } catch (JSONException e) {
            Log.e(TAG, table + ":" + e.getMessage());
            throw new RuntimeException(e);
        } finally {
            db.endTransaction();
        }
    }

    public void actualizarClienteBaja(JSONArray jArray) throws Exception {
        JSONObject jsonData = null;
        ContentValues cv = new ContentValues();
        String table = TablesHelper.ClienteBaja.Table;
        String idCliente = TablesHelper.ClienteBaja.idCliente;
        String motivo = TablesHelper.ClienteBaja.motivo;
        String flag = TablesHelper.ClienteBaja.flag;
        String created_at = TablesHelper.ClienteBaja.created_at;
        String updated_at = TablesHelper.ClienteBaja.updated_at;
        String magic = TablesHelper.ClienteBaja.magic;

        getReadableDatabase().execSQL("DROP TABLE IF EXISTS \"ClienteBaja\"");
        getReadableDatabase().execSQL("CREATE TABLE IF NOT EXISTS \"ClienteBaja\" (\"idCliente\" VARCHAR(15) NOT NULL, \"motivo\" VARCHAR(5)  NOT NULL, \"flag\" VARCHAR(1)  NOT NULL, \"created_at\" DATETIME  NOT NULL, \"updated_at\" DATETIME NOT NULL, \"magic\" INTEGER  NOT NULL)");
        getReadableDatabase().delete(table, null, null);
        SQLiteDatabase db = getWritableDatabase();

        db.beginTransaction();

        try {

            for (int i = 0; i < jArray.length(); i++) {
                jsonData = jArray.getJSONObject(i);
                cv.put(idCliente, jsonData.getString(idCliente));
                cv.put(motivo, jsonData.getString(motivo));
                cv.put(flag, jsonData.getString(flag));
                cv.put(created_at, jsonData.getString(created_at));
                cv.put(updated_at, jsonData.getString(updated_at));
                cv.put(magic, jsonData.getString(magic));

                db.insertOrThrow(table, null, cv);
            }

            db.setTransactionSuccessful();
            Log.i(TAG, table + ": BD Actualizada");
        } catch (JSONException e) {
            Log.e(TAG, table + ":" + e.getMessage());
            throw new RuntimeException(e);
        } finally {
            db.endTransaction();
        }
    }

    public void actualizarMotivoBaja(JSONArray jArray) throws Exception {
        JSONObject jsonData = null;
        ContentValues cv = new ContentValues();
        String table = TablesHelper.MotivoBaja.Table;
        String idMotivoBaja = TablesHelper.MotivoBaja.idMotivoBaja;
        String descripcion = TablesHelper.MotivoBaja.descripcion;

        getReadableDatabase().execSQL("DROP TABLE IF EXISTS \"MotivoBaja\"");
        getReadableDatabase().execSQL("CREATE TABLE IF NOT EXISTS \"MotivoBaja\" (\"idMotivoBaja\" VARCHAR(5) NOT NULL, \"descripcion\" VARCHAR(25) NOT NULL)");
        getReadableDatabase().delete(table, null, null);
        SQLiteDatabase db = getWritableDatabase();

        db.beginTransaction();

        try {

            for (int i = 0; i < jArray.length(); i++) {
                jsonData = jArray.getJSONObject(i);
                cv.put(idMotivoBaja, jsonData.getString(idMotivoBaja));
                cv.put(descripcion, jsonData.getString(descripcion));

                db.insertOrThrow(table, null, cv);
            }

            db.setTransactionSuccessful();
            Log.i(TAG, table + ": BD Actualizada");
        } catch (JSONException e) {
            Log.e(TAG, table + ":" + e.getMessage());
            throw new RuntimeException(e);
        } finally {
            db.endTransaction();
        }
    }

}
