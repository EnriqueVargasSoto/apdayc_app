package com.expediodigital.ventas360.DAO;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.expediodigital.ventas360.Ventas360App;
import com.expediodigital.ventas360.model.ClienteCoordenadasModel;
import com.expediodigital.ventas360.model.ClienteModel;
import com.expediodigital.ventas360.model.ClienteRegistro;
import com.expediodigital.ventas360.model.DireccionClienteModel;
import com.expediodigital.ventas360.model.DirectionApiResponse;
import com.expediodigital.ventas360.model.EncuestaDetalleModel;
import com.expediodigital.ventas360.model.FormaPagoModel;
import com.expediodigital.ventas360.model.HojaRutaIndicadorModel;
import com.expediodigital.ventas360.model.HojaRutaMarcasModel;
import com.expediodigital.ventas360.model.JSONModel;
import com.expediodigital.ventas360.model.MarcaModel;
import com.expediodigital.ventas360.model.PedidoCabeceraModel;
import com.expediodigital.ventas360.model.PoliticaPrecioModel;
import com.expediodigital.ventas360.model.RutaXModuloModel;
import com.expediodigital.ventas360.model.RutaXPersonaModel;
import com.expediodigital.ventas360.model.VendedorModel;
import com.expediodigital.ventas360.util.DataBaseHelper;
import com.expediodigital.ventas360.util.TablesHelper;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Kevin Robinson Meza Hinostroza on julio 2017.
 * Expedio Digital
 * Meza.Hinostroza.Robin@gmail.com
 */

public class DAOCliente {
    public static final String TAG = "DAOCliente";
    DataBaseHelper dataBaseHelper;

    public DAOCliente(Context context) {
        dataBaseHelper = DataBaseHelper.getInstance(context);
    }

    public ArrayList<ClienteModel> getClientes() {

        String rawQuery = "SELECT idCliente,razonSocial,rucDni,ifnull(orden,0) FROM cliente ORDER BY orden";

        SQLiteDatabase db = dataBaseHelper.getReadableDatabase();
        Cursor cur = db.rawQuery(rawQuery, null);
        ArrayList<ClienteModel> lista = new ArrayList<>();
        cur.moveToFirst();

        while (!cur.isAfterLast()) {
            ClienteModel clienteModel = new ClienteModel();
            clienteModel.setIdCliente(cur.getString(0));
            clienteModel.setRazonSocial(cur.getString(1));
            clienteModel.setRucDni(cur.getString(2));
            clienteModel.setOrden(cur.getInt(3));

            lista.add(clienteModel);
            cur.moveToNext();
        }
        cur.close();
        return lista;
    }

    public ClienteModel getCliente(String idCliente) {
        ClienteModel clienteModel = null;
        String rawQuery = "SELECT idCliente,razonSocial,rucDni,ifnull(orden,0) FROM "+TablesHelper.Cliente.Table+" WHERE "+TablesHelper.Cliente.PKeyName+" = '"+idCliente+"'";

        SQLiteDatabase db = dataBaseHelper.getReadableDatabase();
        Cursor cur = db.rawQuery(rawQuery, null);
        ArrayList<ClienteModel> lista = new ArrayList<>();
        cur.moveToFirst();

        while (!cur.isAfterLast()) {
            clienteModel = new ClienteModel();
            clienteModel.setIdCliente(cur.getString(0));
            clienteModel.setRazonSocial(cur.getString(1));
            clienteModel.setRucDni(cur.getString(2));
            clienteModel.setOrden(cur.getInt(3));
            cur.moveToNext();
        }
        cur.close();
        return clienteModel;
    }

    public String getDireccionCliente(String idCliente) {
        String direccion = "";
        SQLiteDatabase db = dataBaseHelper.getWritableDatabase();
        try {
            String rawQuery =
                    "SELECT " +TablesHelper.Cliente.Direccion+" "+
                    "FROM "+ TablesHelper.Cliente.Table + " " +
                    "WHERE "+TablesHelper.Cliente.PKeyName+" = '"+ idCliente +"' ";
            Log.v(TAG,rawQuery);

            Cursor cur = db.rawQuery(rawQuery, null);

            cur.moveToFirst();

            while (!cur.isAfterLast()) {
                direccion = cur.getString(0);
                cur.moveToNext();
            }
            cur.close();
        } catch (Exception e) {
            Log.e(TAG, "getDireccionCliente: Error al obtener registros");
            e.printStackTrace();
        }
        return direccion;
    }

    public ArrayList<ClienteModel> getClientesOrdenados() {
        ArrayList<ClienteModel> lista = new ArrayList<>();

        String rawQuery =
                "SELECT razonSocial,rucDni,orden, c.idCliente,ifnull(direccion,''),ifnull((SELECT estado FROM "+TablesHelper.PedidoCabecera.Table+" WHERE idCliente=c.idCliente),''),c.latitud,c.longitud,ifnull(limiteCredito,0),ifnull(cc.flag,''),ifnull(cw.whathsapp,''),ifnull(cb.motivo,'') " +
                "FROM "+TablesHelper.Cliente.Table+" c " +
                "LEFT JOIN "+TablesHelper.ClienteCoordenadas.Table + " cc ON c.idCliente=cc.idCliente "+
                "LEFT JOIN "+TablesHelper.ClienteWathsapp.Table + " cw ON c.idCliente=cw.idCliente "+
                "LEFT JOIN "+TablesHelper.ClienteBaja.Table + " cb ON c.idCliente=cb.idCliente "+
                "WHERE c.latitud <> 0 AND c.longitud <> 0 ORDER BY orden ";
                //"WHERE latitud like '-12%' AND longitud like '-77%' and latitud <> 0 and longitud <> 0 ORDER BY orden  limit 50 ";
                //"WHERE latitud <> 0 AND longitud <> 0 AND latitud not like '-12%' AND longitud not like '-77%' ORDER BY orden";
        SQLiteDatabase db = dataBaseHelper.getReadableDatabase();
        Cursor cur = db.rawQuery(rawQuery, null);

        cur.moveToFirst();
        int order = 0;
        while (!cur.isAfterLast()) {
            order++;
            ClienteModel clienteModel = new ClienteModel();
            clienteModel.setRazonSocial(cur.getString(0));
            clienteModel.setRucDni(cur.getString(1));
            //clienteModel.setOrden(cur.getInt(2));
            clienteModel.setOrden(order);
            clienteModel.setIdCliente(cur.getString(3));
            clienteModel.setDireccion(cur.getString(4));

            if (cur.getString(5).equals("G") || cur.getString(5).equals("F")){
                clienteModel.setEstadoPedido(ClienteModel.ESTADO_PEDIDO_VISITADO);
            }else if (cur.getString(5).equals("A")){
                clienteModel.setEstadoPedido(ClienteModel.ESTADO_PEDIDO_ANULADO);
            }else{
                clienteModel.setEstadoPedido(ClienteModel.ESTADO_PEDIDO_PENDIENTE);
            }

            clienteModel.setLatitud(cur.getDouble(6));
            clienteModel.setLongitud(cur.getDouble(7));
            //clienteModel.setLatitud(cur.getDouble(7));//INVERTIDO PARA PROBAR YA QUE EN LA BD ESTA AL REVES
            //clienteModel.setLongitud(cur.getDouble(6));//INVERTIDO PARA PROBAR YA QUE EN LA BD ESTA AL REVES
            clienteModel.setLimiteCredito(cur.getDouble(8));
            clienteModel.setFlagLocalizacion(cur.getString(9));
            clienteModel.setWhatsapp(cur.getString(10));

            if(cur.getString(11).equals("")){
                lista.add(clienteModel);
            }
            cur.moveToNext();
        }
        cur.close();
        return lista;
    }

    public ArrayList<ClienteModel> getClientesOrdenadosSinUbicacion(int orden) {
        ArrayList<ClienteModel> lista = new ArrayList<>();

        String rawQuery =
                "SELECT razonSocial,rucDni,orden, c.idCliente,ifnull(direccion,''),ifnull((SELECT estado FROM "+TablesHelper.PedidoCabecera.Table+" WHERE idCliente=c.idCliente),''),c.latitud,c.longitud,ifnull(limiteCredito,0),ifnull(cc.flag,''),ifnull(cw.whathsapp,''),ifnull(cb.motivo,'') " +
                "FROM "+TablesHelper.Cliente.Table+" c " +
                "LEFT JOIN "+TablesHelper.ClienteCoordenadas.Table + " cc ON c.idCliente=cc.idCliente "+
                "LEFT JOIN "+TablesHelper.ClienteWathsapp.Table + " cw ON c.idCliente=cw.idCliente "+
                "LEFT JOIN "+TablesHelper.ClienteBaja.Table + " cb ON c.idCliente=cb.idCliente "+
                "WHERE c.latitud = 0 AND c.longitud = 0 ORDER BY orden";
        SQLiteDatabase db = dataBaseHelper.getReadableDatabase();
        Cursor cur = db.rawQuery(rawQuery, null);

        cur.moveToFirst();
        int order = orden;
        while (!cur.isAfterLast()) {
            order++;
            ClienteModel clienteModel = new ClienteModel();
            clienteModel.setRazonSocial(cur.getString(0));
            clienteModel.setRucDni(cur.getString(1));
            //clienteModel.setOrden(cur.getInt(2));
            clienteModel.setOrden(order);
            clienteModel.setIdCliente(cur.getString(3));
            clienteModel.setDireccion(cur.getString(4));

            if (cur.getString(5).equals("G") || cur.getString(5).equals("F")){
                clienteModel.setEstadoPedido(ClienteModel.ESTADO_PEDIDO_VISITADO);
            }else if (cur.getString(5).equals("A")){
                clienteModel.setEstadoPedido(ClienteModel.ESTADO_PEDIDO_ANULADO);
            }else{
                clienteModel.setEstadoPedido(ClienteModel.ESTADO_PEDIDO_PENDIENTE);
            }

            //clienteModel.setLatitud(cur.getDouble(6));
            //clienteModel.setLongitud(cur.getDouble(7));
            clienteModel.setLatitud(cur.getDouble(7));//INVERTIDO PARA PROBAR YA QUE EN LA BD ESTA AL REVES
            clienteModel.setLongitud(cur.getDouble(6));

            clienteModel.setLimiteCredito(cur.getDouble(8));
            clienteModel.setFlagLocalizacion(cur.getString(9));
            clienteModel.setWhatsapp(cur.getString(10));

            if(cur.getString(11).equals("")){
                lista.add(clienteModel);
            }

            cur.moveToNext();
        }
        cur.close();
        return lista;
    }

    /**
     * @param modoVenta
     * @param estadoVendedor
     * @return Numero de clientes programados (getClientesOrdenados + getClientesOrdenadosSinUbicacion). Se tiene que identidficar, cuándo es un pedido programado y cuando es nuevo.
     */

    public int getNumeroClientesProgramados(String modoVenta, String estadoVendedor) {
        int cantidad =0;
        String rawQuery = "SELECT count(distinct c.idCliente) FROM Cliente c";
        if (modoVenta.equals(VendedorModel.MODO_AUTOVENTA) && estadoVendedor.equals(VendedorModel.ESTADO_DESPACHO)){
            //El cuanto es despacho, por defecto los pedidos cargarán generados al inicio del dia. Y se cuenta como visitado cuando el vendedor toma una acción, ya sea anulando el pedido o facturandolo (despachando)
            rawQuery = "SELECT count(c.idCliente) FROM Cliente c INNER JOIN PedidoCabecera pc ON c.idCliente =  pc.idCliente";
        }

        SQLiteDatabase db = dataBaseHelper.getReadableDatabase();
        Cursor cur = db.rawQuery(rawQuery, null);

        cur.moveToFirst();
        while (!cur.isAfterLast()) {
            cantidad = cur.getInt(0);
            cur.moveToNext();
        }
        cur.close();
        Log.d(TAG,"getNumeroClientesProgramados:"+cantidad);
        return cantidad;
    }

    public int getNumeroClientesVisitados(String modoVenta, String estadoVendedor) {
        int cantidad =0;
        String rawQuery = "SELECT count(distinct c.idCliente) FROM Cliente c INNER JOIN PedidoCabecera pc ON c.idCliente =  pc.idCliente";
        if (modoVenta.equals(VendedorModel.MODO_AUTOVENTA) && estadoVendedor.equals(VendedorModel.ESTADO_DESPACHO)){
            //El cuanto es despacho, por defecto los pedidos cargarán generados al inicio del dia. Y se cuenta como visitado cuando el vendedor toma una acción, ya sea anulando el pedido o facturandolo (despachando). es decir cuando cambie su estado inicial (GENERADO)
            rawQuery = "SELECT count(distinct c.idCliente) FROM Cliente c INNER JOIN PedidoCabecera pc ON c.idCliente =  pc.idCliente WHERE pc.estado <>'"+PedidoCabeceraModel.ESTADO_GENERADO+"'";
        }

        SQLiteDatabase db = dataBaseHelper.getReadableDatabase();
        Cursor cur = db.rawQuery(rawQuery, null);

        cur.moveToFirst();
        while (!cur.isAfterLast()) {
            cantidad = cur.getInt(0);
            cur.moveToNext();
        }
        cur.close();
        return cantidad;
    }

    public int getNumeroClientesEfectivos(String modoVenta, String estadoVendedor) {
        int cantidad =0;
        String rawQuery = "SELECT count(distinct c.idCliente) FROM Cliente c INNER JOIN PedidoCabecera pc ON c.idCliente =  pc.idCliente WHERE pc.estado <>'"+PedidoCabeceraModel.ESTADO_ANULADO+"'";
        if (modoVenta.equals(VendedorModel.MODO_AUTOVENTA) && estadoVendedor.equals(VendedorModel.ESTADO_DESPACHO)){
            //El cuanto es despacho, por defecto los pedidos cargarán generados al inicio del dia. Y se cuenta como efectivo cuando el vendedor logra facturarlo (despacharlo)
            rawQuery = "SELECT count(distinct c.idCliente) FROM Cliente c INNER JOIN PedidoCabecera pc ON c.idCliente =  pc.idCliente WHERE pc.estado ='"+PedidoCabeceraModel.ESTADO_FACTURADO+"'";
        }

        SQLiteDatabase db = dataBaseHelper.getReadableDatabase();
        Cursor cur = db.rawQuery(rawQuery, null);

        cur.moveToFirst();
        while (!cur.isAfterLast()) {
            cantidad = cur.getInt(0);
            cur.moveToNext();
        }
        cur.close();
        return cantidad;
    }

    public ClienteModel getDetalleCliente(String idcliente){
        ClienteModel dcliente = new ClienteModel ();
        String rawQuery =
                "SELECT " +
                "c.idCliente, c.razonSocial, c.rucDni,ifnull(c.idSubGiro,''), ifnull(sg.descripcion,''), ifnull(c.orden,0)," +
                "ifnull(c.correo,''), ifnull(c.direccion,''), ifnull(c.direccionFiscal,''), " +
                "ifnull(c.idRuta,''), ifnull(c.idModulo,''),ifnull(c.idSegmento,''), " +
                "ifnull(c.idCluster,''),ifnull(c.limiteCredito,0)" +
                ",ifnull(c.nroExhibidores,0),ifnull(c.nroPuertasFrio,0) " +
                "FROM "+TablesHelper.Cliente.Table+" c "+
                "LEFT JOIN "+TablesHelper.SubGiro.Table+" sg ON c."+TablesHelper.Cliente.FKSubGiro+"=sg."+TablesHelper.SubGiro.PKeyName+" "+
                "WHERE c."+TablesHelper.Cliente.PKeyName+" = '"+idcliente+"'";

        SQLiteDatabase db = dataBaseHelper.getReadableDatabase();
        Cursor cur = db.rawQuery(rawQuery, null);

        cur.moveToFirst();

        if (cur.moveToFirst()) {
            do {
                dcliente.setIdCliente(cur.getString(0));
                dcliente.setRazonSocial(cur.getString(1));
                dcliente.setRucDni(cur.getString(2));
                dcliente.setIdSubGiro(cur.getString(3));
                dcliente.setSubGiro(cur.getString(4));
                dcliente.setOrden(cur.getInt(5));

                dcliente.setCorreo(cur.getString(6));
                dcliente.setDireccion(cur.getString(7));
                dcliente.setDireccionFiscal(cur.getString(8));

                dcliente.setIdRuta(cur.getString(9));
                dcliente.setIdModulo(cur.getString(10));
                dcliente.setIdSegmento(cur.getString(11));

                dcliente.setIdCluster(cur.getString(12));
                dcliente.setLimiteCredito(cur.getDouble(13));

                dcliente.setNroExhibidores(cur.getInt(14));
                dcliente.setNroPuertasFrio(cur.getInt(15));

                cur.moveToNext();
            }
            while (cur.moveToNext());
        }
        cur.close();
        return dcliente;
    }

    public String getIdPoliticaPrecio(String idcliente){
        String idPoliticaPrecio = "";
        String rawQuery = "SELECT * FROM "+TablesHelper.PoliticaPrecioxCliente.Table+" WHERE "+TablesHelper.PoliticaPrecioxCliente.FKCliente+" = '"+idcliente+"'";
        Log.d(TAG,rawQuery);
        SQLiteDatabase db = dataBaseHelper.getReadableDatabase();
        Cursor cur = db.rawQuery(rawQuery, null);

        cur.moveToFirst();

        if (cur.moveToFirst()) {
            do {
                idPoliticaPrecio = cur.getString(0);
                cur.moveToNext();
            }
            while (cur.moveToNext());
        }
        cur.close();

        return idPoliticaPrecio;
    }

    public Boolean actualizarOrdenCliente (String idCliente, int orden){
        try {
            String where = TablesHelper.Cliente.PKeyName + " = ? ";
            String[] args = { idCliente };
            SQLiteDatabase db = dataBaseHelper.getWritableDatabase();

            ContentValues updateValues = new ContentValues();
            updateValues.put(TablesHelper.Cliente.Orden, orden);

            db.update(TablesHelper.Cliente.Table, updateValues, where, args );
            Log.i(TAG, "Actualizar "+idCliente+" orden "+orden);
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Modificar ");
            e.printStackTrace();
            return false;
        }
    }

    public void guardarJSONRutas(String JSONRutas) {
        try {
            SQLiteDatabase db = dataBaseHelper.getWritableDatabase();

            String subQuery = "SELECT "+TablesHelper.JSON.JSON+" FROM "+TablesHelper.JSON.Table+" WHERE "+TablesHelper.JSON.PKeyName + " = '"+JSONModel.ID_JSON_RUTAS+"'";
            Cursor curAux = db.rawQuery(subQuery, null);
            curAux.moveToFirst();
            String json = null;
            while (!curAux.isAfterLast()) {
                json = curAux.getString(0);
                curAux.moveToNext();
            }
            curAux.close();

            if (json == null){
                ContentValues Nreg = new ContentValues();
                Nreg.put(TablesHelper.JSON.PKeyName, JSONModel.ID_JSON_RUTAS);
                Nreg.put(TablesHelper.JSON.JSON, JSONRutas);

                db.insert(TablesHelper.JSON.Table, null, Nreg);

                Log.i(TAG,"guardarJSONRutas "+JSONModel.ID_JSON_RUTAS);
            }else{
                actualizarJSONRutas(JSONRutas);
            }
        } catch (Exception e) {
            Log.e(TAG, "guardarJSONRutas: Error al insetar registro");
            e.printStackTrace();;
        }
    }

    public Boolean actualizarJSONRutas (String JSONRutas){
        try {
            String where = TablesHelper.JSON.PKeyName + " = ? ";
            String[] args = { JSONModel.ID_JSON_RUTAS };
            SQLiteDatabase db = dataBaseHelper.getWritableDatabase();

            ContentValues updateValues = new ContentValues();
            updateValues.put(TablesHelper.JSON.JSON, JSONRutas);

            db.update(TablesHelper.JSON.Table, updateValues, where, args );
            Log.i(TAG, "actualizarJSONRutas "+JSONModel.ID_JSON_RUTAS);
            return true;
        } catch (Exception e) {
            Log.e(TAG, "actualizarJSONRutas ");
            e.printStackTrace();
            return false;
        }
    }

    public List<DirectionApiResponse> getJSONRutas(){
        List<DirectionApiResponse> lista = new ArrayList<>();
        String json = "";
        String rawQuery = "SELECT "+TablesHelper.JSON.JSON+" FROM "+TablesHelper.JSON.Table+" WHERE "+TablesHelper.JSON.PKeyName + " = '"+JSONModel.ID_JSON_RUTAS+"'";
        Log.d(TAG,rawQuery);
        SQLiteDatabase db = dataBaseHelper.getReadableDatabase();
        Cursor cur = db.rawQuery(rawQuery, null);

        cur.moveToFirst();

        if (cur.moveToFirst()) {
            do {
                json = cur.getString(0);
                cur.moveToNext();
            }
            while (cur.moveToNext());
        }
        cur.close();

        if (!json.isEmpty()){
            lista = new Gson().fromJson(json, new TypeToken<List<DirectionApiResponse>>(){}.getType());
        }

        Log.d(TAG,"getJSONRutas -> "+lista.size());

        return lista;
    }

    public double getLimiteCredito(String idcliente){
        double limiteCredito = 0;
        String rawQuery =
                "SELECT ifnull("+TablesHelper.Cliente.LimiteCredito+",0) "+
                "FROM "+TablesHelper.Cliente.Table+" WHERE "+TablesHelper.Cliente.PKeyName+" = '"+idcliente+"'";

        SQLiteDatabase db = dataBaseHelper.getReadableDatabase();
        Cursor cur = db.rawQuery(rawQuery, null);

        cur.moveToFirst();

        if (cur.moveToFirst()) {
            do {
                limiteCredito = cur.getDouble(0);
                cur.moveToNext();
            }
            while (cur.moveToNext());
        }
        cur.close();

        return limiteCredito;
    }

    public double getSaldoCredito(String idcliente, String numeroPedido){
        double limiteCredito = 0;
        double importePedidos = 0;
        double saldoCredito = 0;
        String idFormaPagoCredito = FormaPagoModel.ID_FORMA_PAGO_CREDITO;

        String rawQuery =
                "SELECT ifnull("+TablesHelper.Cliente.LimiteCredito+",0) " +
                ",(SELECT ifnull(SUM(importeTotal),0) FROM "+TablesHelper.PedidoCabecera.Table+" " +
                "WHERE idCliente='"+idcliente+"' AND idFormaPago= '"+idFormaPagoCredito+"' AND estado <> '"+ PedidoCabeceraModel.ESTADO_ANULADO+"' AND numeroPedido <> '"+numeroPedido+"') "+
                "FROM "+TablesHelper.Cliente.Table+" WHERE "+TablesHelper.Cliente.PKeyName+" = '"+idcliente+"'";
        Log.d(TAG,rawQuery);
        SQLiteDatabase db = dataBaseHelper.getReadableDatabase();
        Cursor cur = db.rawQuery(rawQuery, null);

        cur.moveToFirst();

        if (cur.moveToFirst()) {
            do {
                limiteCredito = cur.getDouble(0);
                importePedidos = cur.getDouble(1);
                cur.moveToNext();
            }
            while (cur.moveToNext());
        }
        cur.close();
        Log.w(TAG,"limiteCredito:"+limiteCredito+" importePedidos:"+importePedidos);

        saldoCredito = limiteCredito-importePedidos;
        if (saldoCredito<0)
            return 0;
        else
            return saldoCredito;
    }

    public PoliticaPrecioModel getPoliticaPrecio(String idcliente){
        PoliticaPrecioModel politicaPrecioModel = null;

        String rawQuery =
                "SELECT * FROM "+TablesHelper.PoliticaPrecio.Table+" " +
                "WHERE "+TablesHelper.PoliticaPrecio.PKName+" = (SELECT "+TablesHelper.PoliticaPrecioxCliente.FKPoliticaPrecio+" FROM "+TablesHelper.PoliticaPrecioxCliente.Table+" WHERE "+TablesHelper.PoliticaPrecioxCliente.FKCliente+" = '"+idcliente+"')";
        Log.d(TAG,rawQuery);
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

    public ArrayList<ClienteModel> getClientesEncuesta(EncuestaDetalleModel encuestaDetalleModel) {
        ArrayList<ClienteModel> lista = new ArrayList<>();

        String rawQuery =
        "SELECT razonSocial,rucDni,orden, idCliente,ifnull(direccion,'') " +
        ",ifnull((SELECT "+ TablesHelper.EncuestaRespuestaCabecera.Flag +" FROM "+TablesHelper.EncuestaRespuestaCabecera.Table+" WHERE idEncuesta='"+encuestaDetalleModel.getIdEncuesta()+"' AND idEncuestaDetalle='"+encuestaDetalleModel.getIdEncuestaDetalle()+"' AND idCliente=c.idCliente LIMIT 1),'') " +
        "FROM "+TablesHelper.Cliente.Table+" c ";

        if (encuestaDetalleModel.getPorCliente() == 1)
            rawQuery += "AND c.idCliente IN (SELECT idCliente FROM EncuestaDetallexCliente WHERE idEncuesta='"+encuestaDetalleModel.getIdEncuesta()+"' AND idEncuestaDetalle='"+encuestaDetalleModel.getIdEncuestaDetalle()+"') ";
        if (encuestaDetalleModel.getPorSegmento() == 1)
            rawQuery += "AND c.idSegmento IN (SELECT idSegmentoCliente FROM EncuestaDetallexSegmento WHERE idEncuesta='"+encuestaDetalleModel.getIdEncuesta()+"' AND idEncuestaDetalle='"+encuestaDetalleModel.getIdEncuestaDetalle()+"') ";

        if (!encuestaDetalleModel.getFiltroOcasion().equals("0") && !encuestaDetalleModel.getFiltroOcasion().equals("")){
            String filtroOcasion = "";
            String [] arrayfiltro = encuestaDetalleModel.getFiltroOcasion().split(",");
            for (String anArrayfiltro : arrayfiltro) {
                filtroOcasion += "'" + anArrayfiltro + "'";
            }
            rawQuery += "AND "+TablesHelper.Cliente.FKOcasionConsumo+" IN ("+filtroOcasion+") ";
        }
        if (!encuestaDetalleModel.getFiltroCanalVentas().equals("0") && !encuestaDetalleModel.getFiltroCanalVentas().equals("")){
            String filtroCanal = "";
            String [] arrayfiltro = encuestaDetalleModel.getFiltroCanalVentas().split(",");
            for (String anArrayfiltro : arrayfiltro) {
                filtroCanal += "'" + anArrayfiltro + "'";
            }
            rawQuery += "AND "+TablesHelper.Cliente.FKCanalVentas+" IN ("+filtroCanal+") ";
        }
        if (!encuestaDetalleModel.getFiltroGiro().equals("0") && !encuestaDetalleModel.getFiltroGiro().equals("")){
            String filtroGiro = "";
            String [] arrayfiltro = encuestaDetalleModel.getFiltroGiro().split(",");
            for (String anArrayfiltro : arrayfiltro) {
                filtroGiro += "'" + anArrayfiltro + "'";
            }
            rawQuery += "AND "+TablesHelper.Cliente.FKGiro+" IN ("+filtroGiro+") ";
        }

        if (!encuestaDetalleModel.getFiltroSubGiro().equals("0") && !encuestaDetalleModel.getFiltroSubGiro().equals("")){
            String filtroSubGiro = "";
            String [] arrayfiltro = encuestaDetalleModel.getFiltroSubGiro().split(",");
            for (String anArrayfiltro : arrayfiltro) {
                filtroSubGiro += "'" + anArrayfiltro + "'";
            }
            rawQuery += "AND "+TablesHelper.Cliente.FKSubGiro+" IN ("+filtroSubGiro+") ";
        }
        rawQuery += "ORDER BY orden ";

        SQLiteDatabase db = dataBaseHelper.getReadableDatabase();
        Cursor cur = db.rawQuery(rawQuery, null);

        cur.moveToFirst();
        int order = 0;
        while (!cur.isAfterLast()) {
            order++;
            ClienteModel clienteModel = new ClienteModel();
            clienteModel.setRazonSocial(cur.getString(0));
            clienteModel.setRucDni(cur.getString(1));
            //clienteModel.setOrden(cur.getInt(2));
            clienteModel.setOrden(order);
            clienteModel.setIdCliente(cur.getString(3));
            clienteModel.setDireccion(cur.getString(4));
            clienteModel.setFlagEncuesta(cur.getString(5));
            if (!cur.getString(5).equals(""))
                clienteModel.setTieneEncuesta(true);
            else
                clienteModel.setTieneEncuesta(false);

            lista.add(clienteModel);
            cur.moveToNext();
        }
        cur.close();
        return lista;
    }

    public ArrayList<HashMap<String, String>> getSubGiros (){
        ArrayList<HashMap<String, String>> lista = new ArrayList<>();

        String rawQuery = "SELECT * FROM "+TablesHelper.SubGiro.Table;

        SQLiteDatabase db = dataBaseHelper.getReadableDatabase();
        Cursor cur = db.rawQuery(rawQuery, null);

        cur.moveToFirst();

        if (cur.moveToFirst()) {
            do {
                HashMap<String, String> item  = new HashMap<>();
                item.put("idSubGiro",cur.getString(0));
                item.put("descripcion",cur.getString(1));
                lista.add(item);
                cur.moveToNext();
            }
            while (cur.moveToNext());
        }
        cur.close();

        return lista;
    }

    public boolean registrarCliente(ClienteRegistro clienteRegistro) {
        try {
            SQLiteDatabase db = dataBaseHelper.getWritableDatabase();

            ContentValues values = new ContentValues();
            values.put(TablesHelper.ClienteRegistro.PKName, clienteRegistro.getIdClienteTemp());
            values.put(TablesHelper.ClienteRegistro.Nombres, clienteRegistro.getNombres());
            values.put(TablesHelper.ClienteRegistro.ApellidoPaterno, clienteRegistro.getApellidoPaterno());
            values.put(TablesHelper.ClienteRegistro.ApellidoMaterno, clienteRegistro.getApellidoMaterno());
            values.put(TablesHelper.ClienteRegistro.RucDni, clienteRegistro.getRucDni());
            values.put(TablesHelper.ClienteRegistro.Telefono, clienteRegistro.getTelefono());
            values.put(TablesHelper.ClienteRegistro.Direccion, clienteRegistro.getDireccion());
            values.put(TablesHelper.ClienteRegistro.Distrito, clienteRegistro.getDistrito());
            values.put(TablesHelper.ClienteRegistro.FKSubGiro, clienteRegistro.getIdSubGiro());
            values.put(TablesHelper.ClienteRegistro.FKRuta, clienteRegistro.getIdRuta());
            values.put(TablesHelper.ClienteRegistro.FKModulo, clienteRegistro.getIdModulo());
            values.put(TablesHelper.ClienteRegistro.FKVendedor, clienteRegistro.getIdVendedor());
            values.put(TablesHelper.ClienteRegistro.Latitud, clienteRegistro.getLatitud());
            values.put(TablesHelper.ClienteRegistro.Longitud, clienteRegistro.getLongitud());
            values.put(TablesHelper.ClienteRegistro.FechaRegistro, clienteRegistro.getFechaRegistro());

            if(db.insert(TablesHelper.ClienteRegistro.Table, null, values) != -1)
                return true;
            else
                return false;
        } catch (Exception e) {
            Log.e(TAG, "registrarCliente");
            e.printStackTrace();
            return false;
        }
    }

    public String getMaximoIdClienteTemp(String idVendedor) {
        String rawQuery = "select max("+TablesHelper.ClienteRegistro.PKName+") from "+TablesHelper.ClienteRegistro.Table+" where "+TablesHelper.ClienteRegistro.FKVendedor+" = '"+ idVendedor + "'";

        SQLiteDatabase db = dataBaseHelper.getReadableDatabase();
        Cursor cur = db.rawQuery(rawQuery, null);
        String idClienteTemp = "";

        cur.moveToFirst();
        if (cur.moveToFirst()) {
            do {
                idClienteTemp = cur.getString(0);
            } while (cur.moveToNext());

        }

        if (idClienteTemp == null || idClienteTemp.trim().length() == 0) {
            idClienteTemp = "";
        }
        cur.close();
        return  idClienteTemp;
    }

    public boolean isAfectoPercepcion(String idCliente) {
        String rawQuery = "SELECT "+TablesHelper.Cliente.AfectoPercepcion+" FROM "+TablesHelper.Cliente.Table+" WHERE "+TablesHelper.Cliente.PKeyName+" = '"+idCliente+"'";

        SQLiteDatabase db = dataBaseHelper.getReadableDatabase();
        Cursor cur = db.rawQuery(rawQuery, null);
        int afectoPercepcion = 0;
        cur.moveToFirst();
        if (cur.moveToFirst()) {
            do {
                 afectoPercepcion = cur.getInt(0);
            } while (cur.moveToNext());
        }
        cur.close();

        return (afectoPercepcion == 1);
    }

    public void actualizarCoordenadas (String idCliente, double latitud, double longitud) {
        try {
            SQLiteDatabase db = dataBaseHelper.getWritableDatabase();
            String rawQuery = "SELECT * FROM "+TablesHelper.ClienteCoordenadas.Table+" WHERE "+TablesHelper.ClienteCoordenadas.PKeyName+" = '"+idCliente+"'";
            Cursor cur = db.rawQuery(rawQuery, null);
            if (cur.getCount() > 0){
                ContentValues values = new ContentValues();
                values.put(TablesHelper.ClienteCoordenadas.Latitud, latitud);
                values.put(TablesHelper.ClienteCoordenadas.Longitud, longitud);
                values.put(TablesHelper.ClienteCoordenadas.Flag, ClienteCoordenadasModel.FLAG_PENDIENTE);

                db.update(TablesHelper.ClienteCoordenadas.Table, values, TablesHelper.ClienteCoordenadas.PKeyName +" = ? ",new String[]{idCliente});
                Log.i(TAG, "actualizarOrdenCliente: registro actualizado");
            }else{
                ContentValues values = new ContentValues();
                values.put(TablesHelper.ClienteCoordenadas.PKeyName,  idCliente);
                values.put(TablesHelper.ClienteCoordenadas.Latitud, latitud);
                values.put(TablesHelper.ClienteCoordenadas.Longitud, longitud);
                values.put(TablesHelper.ClienteCoordenadas.Flag, ClienteCoordenadasModel.FLAG_PENDIENTE);

                db.insert(TablesHelper.ClienteCoordenadas.Table, null, values);
                Log.i(TAG, "actualizarOrdenCliente: registro insertado");
            }
            cur.close();
        } catch (Exception e) {
            Log.e(TAG, "actualizarOrdenCliente: Error al insertar registro");
            e.printStackTrace();
        }
    }

    public ArrayList<ClienteCoordenadasModel> getClientesCoordenadasPendientes(String idcliente){
        ArrayList<ClienteCoordenadasModel> lista = new ArrayList<>();
        String rawQuery =
                "SELECT idCliente,latitud,longitud,flag " +
                "FROM "+TablesHelper.ClienteCoordenadas.Table + " WHERE "+TablesHelper.ClienteCoordenadas.Flag + " = '"+ClienteCoordenadasModel.FLAG_PENDIENTE+"'";
        if (!idcliente.isEmpty())
            rawQuery += " AND "+TablesHelper.ClienteCoordenadas.PKeyName+" = '"+idcliente+"'";
        Log.d(TAG,rawQuery);

        SQLiteDatabase db = dataBaseHelper.getReadableDatabase();
        Cursor cur = db.rawQuery(rawQuery, null);

        cur.moveToFirst();
        if (cur.moveToFirst()) {
            do {
                ClienteCoordenadasModel clienteCoordenadasModel = new ClienteCoordenadasModel();
                clienteCoordenadasModel.setIdCliente(cur.getString(0));
                clienteCoordenadasModel.setLatitud(cur.getString(1));
                clienteCoordenadasModel.setLongitud(cur.getString(2));
                clienteCoordenadasModel.setFlag(cur.getString(3));
                lista.add(clienteCoordenadasModel);
            }
            while (cur.moveToNext());
        }
        cur.close();
        return lista;
    }

    public String getRucDniCliente(String idCliente) {
        String rucDni = "";
        SQLiteDatabase db = dataBaseHelper.getWritableDatabase();
        try {
            String rawQuery =
                    "SELECT " +TablesHelper.Cliente.RucDni+" "+
                    "FROM "+ TablesHelper.Cliente.Table + " " +
                    "WHERE "+TablesHelper.Cliente.PKeyName+" = '"+ idCliente +"' ";
            Log.v(TAG,rawQuery);

            Cursor cur = db.rawQuery(rawQuery, null);

            cur.moveToFirst();

            while (!cur.isAfterLast()) {
                rucDni = cur.getString(0);
                cur.moveToNext();
            }
            cur.close();
        } catch (Exception e) {
            Log.e(TAG, "getDireccionCliente: Error al obtener registros");
            e.printStackTrace();
        }
        return rucDni;
    }

    public String actualizarFlagCoordenadas (String cadenaRespuesta){
        String flag = "";
        try {
            JSONArray jsonArray = new JSONArray(cadenaRespuesta);

            SQLiteDatabase db = dataBaseHelper.getWritableDatabase();
            String where = TablesHelper.ClienteCoordenadas.PKeyName + " = ?";

            for (int i = 0; i < jsonArray.length(); i++){
                JSONObject jsonData = jsonArray.getJSONObject(i);

                String idCliente = jsonData.getString(TablesHelper.ClienteCoordenadas.PKeyName).trim();
                flag = jsonData.getString(TablesHelper.ClienteCoordenadas.Flag).trim();

                if (flag.equals(PedidoCabeceraModel.FLAG_ENVIADO) || flag.equals(PedidoCabeceraModel.FLAG_PENDIENTE)){
                    ContentValues updateValues = new ContentValues();
                    updateValues.put(TablesHelper.ClienteCoordenadas.Flag, flag);
                    String[] args = { idCliente };

                    Log.i(TAG, "Actualizar "+TablesHelper.ClienteCoordenadas.Table+": Actualizando..."+idCliente);
                    db.update(TablesHelper.ClienteCoordenadas.Table, updateValues, where, args );
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Actualizar "+TablesHelper.ClienteCoordenadas.Table+": Error al Actualizar registro");
            e.printStackTrace();
            flag = "error";
        }
        return flag;
    }

    public HojaRutaIndicadorModel getHojaRutaIndicador(String idcliente){
        HojaRutaIndicadorModel indicadorModel = new HojaRutaIndicadorModel();
        String rawQuery =
                "SELECT * " +
                "FROM "+TablesHelper.HojaRutaIndicador.Table+" "+
                "WHERE "+TablesHelper.HojaRutaIndicador.idCliente+" = '"+idcliente+"'";

        SQLiteDatabase db = dataBaseHelper.getReadableDatabase();
        Cursor cur = db.rawQuery(rawQuery, null);

        cur.moveToFirst();

        while (!cur.isAfterLast()) {
            indicadorModel.setEjercicio(cur.getInt(0));
            indicadorModel.setPeriodo(cur.getInt(1));
            indicadorModel.setIdCliente(cur.getString(2));
            indicadorModel.setTipoCobertura(cur.getString(3));
            indicadorModel.setProgramado(cur.getInt(4));
            indicadorModel.setTranscurrido(cur.getInt(5));
            indicadorModel.setLiquidado(cur.getInt(6));
            indicadorModel.setHitRate(cur.getDouble(7));
            indicadorModel.setVenAnoAnterior(cur.getDouble(8));
            indicadorModel.setVenMesAnterior(cur.getDouble(9));
            indicadorModel.setAvanceMesActual(cur.getDouble(10));
            indicadorModel.setProyectado(cur.getDouble(11));
            indicadorModel.setAvanceAnual(cur.getDouble(12));
            indicadorModel.setAvanceMes(cur.getDouble(13));
            indicadorModel.setCUOTAGTM(cur.getDouble(14));
            indicadorModel.setSEGMENTO(cur.getString(15));
            indicadorModel.setEXHIBIDORES(cur.getInt(16));
            indicadorModel.setNROPTAFRIOGTM(cur.getInt(17));
            indicadorModel.setCoberturaMultiple(cur.getInt(18));
            cur.moveToNext();
        }
        cur.close();

        return indicadorModel;
    }

    public ArrayList<HojaRutaMarcasModel> getHojaRutaMarcas(String idcliente){
        ArrayList<HojaRutaMarcasModel> lista = new ArrayList<>();
        String rawQuery =
                "SELECT * " +
                "FROM "+TablesHelper.HojaRutaMarcas.Table+" "+
                "WHERE "+TablesHelper.HojaRutaMarcas.idCliente+" = '"+idcliente+"' "+
                "ORDER BY "+TablesHelper.HojaRutaMarcas.canPaq + " DESC ";

        SQLiteDatabase db = dataBaseHelper.getReadableDatabase();
        Cursor cur = db.rawQuery(rawQuery, null);

        cur.moveToFirst();

        while (!cur.isAfterLast()) {
            HojaRutaMarcasModel marcaModel = new HojaRutaMarcasModel();
            marcaModel.setEjercicio(cur.getInt(0));
            marcaModel.setPeriodo(cur.getInt(1));
            marcaModel.setIdCliente(cur.getString(2));
            marcaModel.setMarca(cur.getString(3));
            marcaModel.setCantidadPaquetes(cur.getInt(4));

            lista.add(marcaModel);

            cur.moveToNext();
        }
        cur.close();

        return lista;
    }

    public RutaXPersonaModel getRutaXPersona(String idcliente){
        RutaXPersonaModel marcaModel = null;
        String rawQuery =
                "SELECT * " +
                        "FROM "+TablesHelper.RutasxPersona.Table+" "+
                        "WHERE "+TablesHelper.RutasxPersona.idPersona+" = '"+idcliente+"' ";

        SQLiteDatabase db = dataBaseHelper.getReadableDatabase();
        Cursor cur = db.rawQuery(rawQuery, null);

        if (cur.moveToFirst()) {
            do {
                marcaModel = new RutaXPersonaModel();
                marcaModel.setIdPersona(cur.getInt(0));
                marcaModel.setIdRuta(cur.getInt(1));
                cur.moveToNext();
            }
            while (cur.moveToNext());
        }

        cur.close();

        return marcaModel;
    }

    public ArrayList<RutaXModuloModel> getRutaXCliente(String idcliente){
        ArrayList<RutaXModuloModel> lista = new ArrayList<>();
        String rawQuery =
                "SELECT mr.idModulo, mr.idRuta " +
                        "FROM "+TablesHelper.Cliente.Table+" c "+
                        "INNER JOIN ModuloxRuta mr ON mr.idModulo =  c.idModulo "+
                        "WHERE "+TablesHelper.Cliente.PKeyName+" = '"+idcliente+"' ";

        SQLiteDatabase db = dataBaseHelper.getReadableDatabase();
        Cursor cur = db.rawQuery(rawQuery, null);

        if (cur.moveToFirst()) {
            do {
                RutaXModuloModel marcaModel = new RutaXModuloModel();
                marcaModel.setIdModulo(cur.getInt(0));
                marcaModel.setIdRuta(cur.getInt(1));
                lista.add(marcaModel);
                cur.moveToNext();
            }
            while (cur.moveToNext());
        }

        cur.close();

        return lista;
    }

    public MarcaModel getMarca(String idMarca){
        MarcaModel marcaModel = null;
        String rawQuery =
                "SELECT * " +
                        "FROM "+TablesHelper.Marca.Table+" "+
                        "WHERE "+TablesHelper.Marca.PKeyName+" = '"+idMarca+"' ";

        SQLiteDatabase db = dataBaseHelper.getReadableDatabase();
        Cursor cur = db.rawQuery(rawQuery, null);

        if (cur.moveToFirst()) {
            do {
                marcaModel = new MarcaModel();
                marcaModel.setIdMarca(cur.getInt(0));
                marcaModel.setDescripcion(cur.getString(1));
                cur.moveToNext();
            }
            while (cur.moveToNext());
        }

        cur.close();

        return marcaModel;
    }
}
