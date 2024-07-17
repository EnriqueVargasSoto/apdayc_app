package com.expediodigital.ventas360.util;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.HandlerThread;
import android.util.Log;
import android.util.TimingLogger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;
import org.xmlpull.v1.XmlPullParserException;

import com.expediodigital.ventas360.DAO.DAOPedido;
import com.expediodigital.ventas360.DAO.DAOProducto;
import com.expediodigital.ventas360.Ventas360App;
import com.expediodigital.ventas360.model.DocumentoGeneradoModel;
import com.expediodigital.ventas360.model.PedidoCabeceraModel;
import com.google.gson.Gson;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.concurrent.TimeoutException;
import java.util.logging.Handler;

import okio.Timeout;

/**
 * Created by Kevin Robinson Meza Hinostroza on julio 2017.
 * Expedio Digital
 * Meza.Hinostroza.Robin@gmail.com
 */

public class SoapManager {
    private static final String TAG = "SoapManager";
    private static final String NAMESPACE = "http://tempuri.org/";
    private final int TIEMPO_ESPERA = 1000 * 90;//60 Segundos

    Ventas360App ventas360App;
    private Context context;
    private DataBaseHelper helper;
    private String urlWebService;
    private String idServicio;
    private String vendedor;
    private String idEmpresa;
    private String idSucursal;
    private String idAlmacen;
    private String numeroGuia;

    public SoapManager(Context context) {
        this.context = context;
        helper = DataBaseHelper.getInstance(this.context);
        ventas360App = (Ventas360App) context.getApplicationContext();

        urlWebService   = "http://apps.atiendo.pe/ventas360Test/Service360.asmx";//http://66.70.227.161:1200/ventas360Test/Service360.asmx";//ventas360App.getUrlWebService();//Obtiene la direccion del servicio web
        idServicio      = ventas360App.getIdServicio();//Obtiene el ID de los datos para la conexión con el BD
        vendedor        = ventas360App.getIdVendedor();
        idEmpresa       = ventas360App.getIdEmpresa();
        idSucursal      = ventas360App.getIdSucursal();
        idAlmacen       = ventas360App.getIdAlmacen();
        numeroGuia      = ventas360App.getNumeroGuia();
    }

    /*
    * Se envia el nombre de la tabla como parametro para ejecutar dinamicamente el metodo de insertar en la BD
    */
    public void obtenerRegistrosJSON(String method, final String table) throws Exception {
        Log.d(TAG,"idEmpresa:"+idEmpresa);
        Log.d(TAG,"idSucursal:"+idSucursal);
        Log.d(TAG,"idVendedor:"+vendedor);
        Log.d(TAG,"server:"+idServicio);
        String METHOD_NAME = method;

        if(METHOD_NAME != null && urlWebService != null){
            String SOAP_ACTION = NAMESPACE + METHOD_NAME;

            SoapSerializationEnvelope soapEnvelope = new SoapSerializationEnvelope( SoapEnvelope.VER11 );
            SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME);

            HttpTransportSE transport = new HttpTransportSE(urlWebService, TIEMPO_ESPERA);

            if (method.equals(TablesHelper.Usuario.Sincronizar) || method.equals(TablesHelper.Vendedor.Sincronizar) || method.equals(TablesHelper.Empresa.Sincronizar))
                transport = new HttpTransportSE(urlWebService, TIEMPO_ESPERA/2);

            request.addProperty("server", idServicio);
            soapEnvelope.dotNet = true;
            soapEnvelope.setOutputSoapObject(request);

            long beforeCall = System.currentTimeMillis();

            try {
                transport.call(SOAP_ACTION, soapEnvelope);
                Log.i( TAG, table + ": Respuesta en "  + (System.currentTimeMillis() - beforeCall) + "miliseg");

                SoapPrimitive result = (SoapPrimitive) soapEnvelope.getResponse();
                final JSONArray jsonstring = new JSONArray(result.toString());

                Log.i(TAG, table + ": " + jsonstring.length() + " registros");
                long startTime = System.nanoTime();
                //Buscamos el metodo en la clase DataBaseHelper para insertar el json retornado

                final Method helperMethod = helper.getClass().getMethod("actualizar"+table, JSONArray.class );
                Thread thread = new Thread() {
                    public void run() {
                        try {
                            helperMethod.invoke(helper,jsonstring);
                            Log.i(TAG, table + " SINCRONIZADO");
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        } catch (InvocationTargetException e) {
                            e.printStackTrace();
                        }
                    }
                };
                thread.start();
                long stopTime = System.nanoTime();
                long timeSaveAqlite = (stopTime - startTime)/100000;
                System.out.println("Finalizado guardado en " + table + "  " + String.valueOf(timeSaveAqlite) + " mseg");

            } catch (NoSuchMethodException e) {
                Log.e(TAG, "NO SINCRONIZADO : No existe el metodo actualizar"+ table +" en DataBaseHelper");
                e.printStackTrace();
                throw new NoSuchMethodException();//atrapa la excepcion pero la vuelve a lanzar (para que la la actividad la vuelva a detectar)
            } catch (SocketTimeoutException e) {
                //Error relacionado a la webservice
                Log.e(TAG, table + " SOCKETTIMEOUT EXCEPTION :" + e.getMessage());
                e.printStackTrace();
                throw new SocketTimeoutException();
            } catch (IOException e) {
                //Error relacionado a la webservice
                Log.e(TAG, table + " IO EXCEPTION:" + e.getMessage());
                //ex.printStackTrace();
                throw new IOException(e);
            } catch (JSONException e) {
                //Error relacionado a la webservice
                Log.e(TAG, table + " JSON EXCEPTION:" + e.getMessage());
                //ex.printStackTrace();
                throw new RuntimeException(e);
            } catch (Exception e) {
                //Error relacionado a la webservice
                if (e.getCause() != null) {
                    Log.e(TAG, table + " GENERAL EXCEPTION CAUSE:" + e.getCause().getMessage());
                    throw new Exception(e.getCause());
                }else {
                    Log.e(TAG, table + " GENERAL EXCEPTION:" + e.getMessage());
                    throw new Exception(e);
                }
            }

        }else
            Log.e(TAG, "No se encontro el metodo Soap para  sincronizar la tabla "+table+" en Util.getMetodoSoap");
    }


    public void obtenerRegistrosxVendedorJSON(String method, final String table) throws Exception {
        String METHOD_NAME = method;

        if(METHOD_NAME != null && urlWebService != null){
            String SOAP_ACTION = NAMESPACE + METHOD_NAME;
            Log.d(TAG,"idEmpresa:"+idEmpresa);
            Log.d(TAG,"idSucursal:"+idSucursal);
            Log.d(TAG,"idVendedor:"+vendedor);
            Log.d(TAG,"server:"+idServicio);
            SoapSerializationEnvelope soapEnvelope = new SoapSerializationEnvelope( SoapEnvelope.VER11 );
            SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME);

            HttpTransportSE transport = new HttpTransportSE(urlWebService,TIEMPO_ESPERA);
            request.addProperty("idEmpresa", idEmpresa);
            request.addProperty("idSucursal", idSucursal);
            request.addProperty("idVendedor", vendedor);
            request.addProperty("server", idServicio);
            soapEnvelope.dotNet = true;
            soapEnvelope.setOutputSoapObject(request);

            long beforeCall = System.currentTimeMillis();

            try {
                transport.call(SOAP_ACTION, soapEnvelope);
                Log.i( TAG, table + ": Respuesta en "  + (System.currentTimeMillis() - beforeCall) + "miliseg");

                SoapPrimitive result = (SoapPrimitive) soapEnvelope.getResponse();
                final JSONArray jsonstring = new JSONArray(result.toString());

                Log.i(TAG, table + ": " + jsonstring.length() + " registros");
                Log.v(TAG, jsonstring.toString());


                //Buscamos el metodo en la clase DataBaseHelper para insertar el json retornado
                if(table.equals("ObjPedido")){
                    Log.i(TAG,result.toString());
                    final Method helperMethod = helper.getClass().getMethod("actualizar"+table, JSONArray.class , String.class );
                    /*
                    long startTime = System.nanoTime();
                    Thread thread = new Thread() {
                        public void run() {
                            try {
                                helperMethod.invoke(helper,jsonstring,vendedor);
                                Log.i(TAG, table + " SINCRONIZADO");
                            } catch (IllegalAccessException e) {
                                e.printStackTrace();
                            } catch (InvocationTargetException e) {
                                e.printStackTrace();
                            }
                        }
                    };
                    thread.start();
                    long stopTime = System.nanoTime();
                    long timeSaveAqlite = (stopTime - startTime)/100000;
                    System.out.println("Finalizado guardado en " + table + "  " + String.valueOf(timeSaveAqlite) + " mseg");*/
                    helperMethod.invoke(helper,jsonstring,vendedor);
                }else {

                    final Method helperMethod = helper.getClass().getMethod("actualizar"+table, JSONArray.class );
                  /*  long startTime = System.nanoTime();
                    Thread thread = new Thread() {
                        public void run() {
                            try {
                                helperMethod.invoke(helper,jsonstring);
                                Log.i(TAG, table + " SINCRONIZADO");
                            } catch (IllegalAccessException e) {
                                e.printStackTrace();
                            } catch (InvocationTargetException e) {
                                e.printStackTrace();
                            }
                        }
                    };
                    thread.start();
                    long stopTime = System.nanoTime();
                    long timeSaveAqlite = (stopTime - startTime)/100000;
                    System.out.println("Finalizado guardado en " + table + "  " + String.valueOf(timeSaveAqlite) + " mseg");*/
                    helperMethod.invoke(helper,jsonstring);
                }

                Log.i(TAG, table + " SINCRONIZADO");
            } catch (NoSuchMethodException e) {
                Log.e(TAG, "NO SINCRONIZADO : No existe el metodo actualizar"+ table +" en DataBaseHelper");
                e.printStackTrace();
                throw new NoSuchMethodException();//atrapa la excepcion pero la vuelve a lanzar (para que la la actividad la vuelva a detectar)
            } catch (SocketTimeoutException e) {
                //Error relacionado a la webservice
                Log.e(TAG, table + " SOCKETTIMEOUT EXCEPTION :" + e.getMessage());
                e.printStackTrace();
                throw new SocketTimeoutException();
            } catch (IOException e) {
                //Error relacionado a la webservice
                Log.e(TAG, table + " IO EXCEPTION:" + e.getMessage());
                //ex.printStackTrace();
                throw new IOException(e);
            } catch (JSONException e) {
                //Error relacionado a la webservice
                Log.e(TAG, table + " JSON EXCEPTION:" + e.getMessage());
                //ex.printStackTrace();
                throw new RuntimeException(e);
            } catch (Exception e) {
                //Error relacionado a la webservice
                if (e.getCause() != null) {
                    Log.e(TAG, table + " GENERAL EXCEPTION CAUSE:" + e.getCause().getMessage());
                    throw new Exception(e.getCause());
                }else {
                    Log.e(TAG, table + " GENERAL EXCEPTION:" + e.getMessage());
                    throw new Exception(e);
                }
            }

        }else
            Log.e(TAG, "No se encontro el metodo Soap para  sincronizar la tabla "+table+" en Util.getMetodoSoap");
    }

    public void obtenerRegistrosxSucursalJSON(String method, final String table) throws Exception {
        String METHOD_NAME = method;
        Log.d(TAG,"idEmpresa:"+idEmpresa);
        Log.d(TAG,"idSucursal:"+idSucursal);
        Log.d(TAG,"idVendedor:"+vendedor);
        Log.d(TAG,"server:"+idServicio);
        Log.d(TAG,"urlWebService:"+urlWebService);

        if(METHOD_NAME != null && urlWebService != null){
            String SOAP_ACTION = NAMESPACE + METHOD_NAME;

            SoapSerializationEnvelope soapEnvelope = new SoapSerializationEnvelope( SoapEnvelope.VER11 );
            SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME);

            HttpTransportSE transport = new HttpTransportSE(urlWebService,TIEMPO_ESPERA);

            //Si se va a sincronizar Servicios se debe obtener desde la URL base (Direccion de la webservice que otorga la tabla servicios). Para que se controle dsde un servicio remoto
            /*if (method.equals(TablesHelper.Servicio.Sincronizar)){
                transport = new HttpTransportSE(Ventas360App.URL_WEBSERVICE_BASE);
            }*/

            request.addProperty("idEmpresa", idEmpresa);
            request.addProperty("idSucursal", idSucursal);
            request.addProperty("server", idServicio);
            soapEnvelope.dotNet = true;
            soapEnvelope.setOutputSoapObject(request);

            long beforeCall = System.currentTimeMillis();

            try {
                transport.call(SOAP_ACTION, soapEnvelope);
                Log.i( TAG, table + ": Respuesta en "  + (System.currentTimeMillis() - beforeCall) + "miliseg");

                SoapPrimitive result = (SoapPrimitive) soapEnvelope.getResponse();
                final JSONArray jsonstring = new JSONArray(result.toString());

                Log.i(TAG, table + ": " + jsonstring.length() + " registros");
                Log.i(TAG, result.toString());


                //Buscamos el metodo en la clase DataBaseHelper para insertar el json retornado
                if(table.equals("ObjPedido")){
                    final Method helperMethod = helper.getClass().getMethod("actualizar"+table, JSONArray.class , String.class );
                    long startTime = System.nanoTime();
                    Thread thread = new Thread() {
                        public void run() {
                            try {
                                helperMethod.invoke(helper,jsonstring,vendedor);
                                Log.i(TAG, table + " SINCRONIZADO");
                            } catch (IllegalAccessException e) {
                                e.printStackTrace();
                            } catch (InvocationTargetException e) {
                                e.printStackTrace();
                            }
                        }
                    };
                    thread.start();
                    long stopTime = System.nanoTime();
                    long timeSaveAqlite = (stopTime - startTime)/100000;
                    System.out.println("Finalizado guardado en " + table + "  " + String.valueOf(timeSaveAqlite) + " mseg");

//                    helperMethod.invoke(helper,jsonstring,vendedor);
                }else {
                    final Method helperMethod = helper.getClass().getMethod("actualizar"+table, JSONArray.class );
                    long startTime = System.nanoTime();
                    Thread thread = new Thread() {
                        public void run() {
                            try {
                                helperMethod.invoke(helper,jsonstring);
                                Log.i(TAG, table + " SINCRONIZADO");
                            } catch (IllegalAccessException e) {
                                e.printStackTrace();
                            } catch (InvocationTargetException e) {
                                e.printStackTrace();
                            }
                        }
                    };
                    thread.start();
                    long stopTime = System.nanoTime();
                    long timeSaveAqlite = (stopTime - startTime)/100000;
                    System.out.println("Finalizado guardado en " + table + "  " + String.valueOf(timeSaveAqlite) + " mseg");

//                    helperMethod.invoke(helper,jsonstring);
                }

                Log.i(TAG, table + " SINCRONIZADO");

            } catch (NoSuchMethodException e) {
                Log.e(TAG, "NO SINCRONIZADO : No existe el metodo actualizar"+ table +" en DataBaseHelper");
                e.printStackTrace();
                throw new NoSuchMethodException();//atrapa la excepcion pero la vuelve a lanzar (para que la la actividad la vuelva a detectar)
            } catch (SocketTimeoutException e) {
                //Error relacionado a la webservice
                Log.e(TAG, table + " SOCKETTIMEOUT EXCEPTION :" + e.getMessage());
                e.printStackTrace();
                throw new SocketTimeoutException();
            } catch (IOException e) {
                //Error relacionado a la webservice
                Log.e(TAG, table + " IO EXCEPTION:" + e.getMessage());
                //ex.printStackTrace();
                throw new IOException(e);
            } catch (JSONException e) {
                //Error relacionado a la webservice
                Log.e(TAG, table + " JSON EXCEPTION:" + e.getMessage());
                //ex.printStackTrace();
                throw new RuntimeException(e);
            } catch (Exception e) {
                //Error relacionado a la webservice
                if (e.getCause() != null) {
                    Log.e(TAG, table + " GENERAL EXCEPTION CAUSE:" + e.getCause().getMessage());
                    throw new Exception(e.getCause());
                }else {
                    Log.e(TAG, table + " GENERAL EXCEPTION:" + e.getMessage());
                    throw new Exception(e);
                }
            }

        }else
            Log.e(TAG, "No se encontro el metodo Soap para  sincronizar la tabla "+table+" en Util.getMetodoSoap");
    }

    public void obtenerRegistrosxEmpresaJSON(String method, final String table) throws Exception {
        String METHOD_NAME = method;

        if(METHOD_NAME != null && urlWebService != null){
            String SOAP_ACTION = NAMESPACE + METHOD_NAME;

            SoapSerializationEnvelope soapEnvelope = new SoapSerializationEnvelope( SoapEnvelope.VER11 );
            SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME);

            HttpTransportSE transport = new HttpTransportSE(urlWebService,TIEMPO_ESPERA);
            request.addProperty("idEmpresa", idEmpresa);
            request.addProperty("server", idServicio);
            soapEnvelope.dotNet = true;
            soapEnvelope.setOutputSoapObject(request);

            long beforeCall = System.currentTimeMillis();

            try {
                transport.call(SOAP_ACTION, soapEnvelope);
                Log.i( TAG, table + ": Respuesta en "  + (System.currentTimeMillis() - beforeCall) + "miliseg");

                SoapPrimitive result = (SoapPrimitive) soapEnvelope.getResponse();
                final JSONArray jsonstring = new JSONArray(result.toString());

                Log.i(TAG, table + ": " + jsonstring.length() + " registros");
                Log.v(TAG, jsonstring.toString());

                try {
                    //Buscamos el metodo en la clase DataBaseHelper para insertar el json retornado
                    if(table.equals("ObjPedido")){
                        final Method helperMethod = helper.getClass().getMethod("actualizar"+table, JSONArray.class , String.class );
                        long startTime = System.nanoTime();
                        Thread thread = new Thread() {
                            public void run() {
                                try {
                                    helperMethod.invoke(helper,jsonstring,vendedor);
                                    Log.i(TAG, table + " SINCRONIZADO");
                                } catch (IllegalAccessException e) {
                                    e.printStackTrace();
                                } catch (InvocationTargetException e) {
                                    e.printStackTrace();
                                }
                            }
                        };
                        thread.start();
                        long stopTime = System.nanoTime();
                        long timeSaveAqlite = (stopTime - startTime)/100000;
                        System.out.println("Finalizado guardado en " + table + "  " + String.valueOf(timeSaveAqlite) + " mseg");
//                        helperMethod.invoke(helper,jsonstring,vendedor);
                    }else {
                        final Method helperMethod = helper.getClass().getMethod("actualizar"+table, JSONArray.class );
                        long startTime = System.nanoTime();
                        Thread thread = new Thread() {
                            public void run() {
                                try {
                                    helperMethod.invoke(helper,jsonstring);
                                    Log.i(TAG, table + " SINCRONIZADO");
                                } catch (IllegalAccessException e) {
                                    e.printStackTrace();
                                } catch (InvocationTargetException e) {
                                    e.printStackTrace();
                                }
                            }
                        };
                        thread.start();
                        long stopTime = System.nanoTime();
                        long timeSaveAqlite = (stopTime - startTime)/100000;
                        System.out.println("Finalizado guardado en " + table + "  " + String.valueOf(timeSaveAqlite) + " mseg");
//                        helperMethod.invoke(helper,jsonstring);
                    }

                } catch (NoSuchMethodException e) {
                    Log.e(TAG, "NO SINCRONIZADO : No existe el metodo actualizar"+ table +" en DataBaseHelper");
                    e.printStackTrace();
                    throw new Exception(e);

                } catch (Exception e) {
                    Log.e(TAG,table + " NO SINCRONIZADO :"+e.getMessage());
                    e.printStackTrace();
                    throw new Exception(e);
                }

                Log.i(TAG, table + " SINCRONIZADO");

            } catch (Exception e) {
                Log.e(TAG,table + " NO SINCRONIZADO :"+e.getMessage());
                e.printStackTrace();
                throw new Exception(e);
            }

        }else
            Log.e(TAG, "No se encontro el metodo Soap para  sincronizar la tabla "+table+" en Util.getMetodoSoap");
    }

    public String enviarPendientes(String method, String cadena) throws Exception {
        String METHOD_NAME = method;

        if(METHOD_NAME != null){
            String SOAP_ACTION = NAMESPACE + METHOD_NAME;

            Log.i( TAG, method +": " + cadena);

            SoapSerializationEnvelope Soapenvelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
            SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME);

            HttpTransportSE transporte = new HttpTransportSE(urlWebService);
            request.addProperty("cadena", cadena);
            request.addProperty("idEmpresa", idEmpresa);
            request.addProperty("idSucursal", idSucursal);
            request.addProperty("server", idServicio);
            Soapenvelope.dotNet = true;
            Soapenvelope.setOutputSoapObject(request);

            long beforecall = System.currentTimeMillis();

            try {
                transporte.call(SOAP_ACTION, Soapenvelope);
                Log.i(TAG, method + ": Respuesta en " + (System.currentTimeMillis() - beforecall) + "miliseg");

                SoapPrimitive resultado_xml = (SoapPrimitive) Soapenvelope.getResponse();
                String res = resultado_xml.toString();

                Log.i(TAG, "enviarPendientes Respuesta: " + res);

                return res;
            } catch (XmlPullParserException e){
                e.printStackTrace();
                throw new XmlPullParserException(e.getMessage());
            } catch (Exception e) {
                Log.e(TAG, method+ " NO SINCRONIZADO "+e.getMessage());
                e.printStackTrace();
                throw new Exception(e);
            }
        }else{
            Log.e(TAG, method+ " NO EXISTE EN EL WEBSERVICE");
            return "0";
        }
    }


    /* ------------------------------------- Metodos para obtener o consultar cosas particulares ------------------------------------------------*/
    public void obtenerStockProductox(String method, String idProducto, String numeroPedidoActual) throws Exception{
        String METHOD_NAME = method;

        if(METHOD_NAME != null){
            String SOAP_ACTION = NAMESPACE + METHOD_NAME;

            Log.i( TAG, method +": " + idProducto);

            SoapSerializationEnvelope Soapenvelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
            SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME);

            HttpTransportSE transporte = new HttpTransportSE(urlWebService);
            request.addProperty("numeroPedido",numeroPedidoActual);
            request.addProperty("idProducto", idProducto);
            request.addProperty("idAlmacen", idAlmacen);
            request.addProperty("idEmpresa", idEmpresa);
            request.addProperty("idSucursal", idSucursal);
            //request.addProperty("numeroGuia", numeroGuia);
            request.addProperty("server", idServicio);
            Soapenvelope.dotNet = true;
            Soapenvelope.setOutputSoapObject(request);

            long beforecall = System.currentTimeMillis();

            try {
                transporte.call(SOAP_ACTION, Soapenvelope);
                Log.i( TAG, method + ": Respuesta en "  + (System.currentTimeMillis() - beforecall) + "miliseg");

                SoapPrimitive resultado_xml = (SoapPrimitive) Soapenvelope.getResponse();
                JSONArray jsonstring = new JSONArray(resultado_xml.toString());

                Log.i(TAG,"obtenerStockProductox Respuesta: " + resultado_xml.toString());
                DAOProducto daoProducto = new DAOProducto(context);
                daoProducto.actualizarKardexProducto(jsonstring, numeroPedidoActual);
            } catch (Exception e) {
                Log.e(TAG, method+ " NO SINCRONIZADO "+e.getMessage());
                e.printStackTrace();
                throw new Exception(e);
            }
        }else{
            Log.e(TAG, method+ " NO EXISTE EN EL WEBSERVICE");
        }
    }

    public String obtenerEstadoPedido(String method, String numeroPedido) throws Exception{
        String METHOD_NAME = method;
        String estado = "";
        if(METHOD_NAME != null){
            String SOAP_ACTION = NAMESPACE + METHOD_NAME;

            Log.i( TAG, method +": " + numeroPedido);

            SoapSerializationEnvelope Soapenvelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
            SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME);

            HttpTransportSE transporte = new HttpTransportSE(urlWebService);
            request.addProperty("numeroPedido", numeroPedido);
            request.addProperty("idVendedor", vendedor);
            request.addProperty("idEmpresa", idEmpresa);
            request.addProperty("idSucursal", idSucursal);
            request.addProperty("server", idServicio);
            Soapenvelope.dotNet = true;
            Soapenvelope.setOutputSoapObject(request);

            long beforecall = System.currentTimeMillis();

            try {
                transporte.call(SOAP_ACTION, Soapenvelope);
                Log.i( TAG, method + ": Respuesta en "  + (System.currentTimeMillis() - beforecall) + "miliseg");

                SoapPrimitive resultado_xml = (SoapPrimitive) Soapenvelope.getResponse();
                estado = resultado_xml.toString();

                Log.i(TAG,"obtenerEstadoPedido Respuesta: ("+estado+")");

                if (estado.equals(PedidoCabeceraModel.ESTADO_ANULADO) || estado.equals(PedidoCabeceraModel.ESTADO_GENERADO) || estado.equals(PedidoCabeceraModel.ESTADO_FACTURADO)){
                    DAOPedido daoPedido = new DAOPedido(context);
                    daoPedido.actualizarEstadoPedido(numeroPedido,estado);
                }
            } catch (Exception e) {
                Log.e(TAG, method+ " NO SINCRONIZADO "+e.getMessage());
                e.printStackTrace();
                throw new Exception(e);
            }
        }else{
            Log.e(TAG, method+ " NO EXISTE EN EL WEBSERVICE");
        }
        return estado;
    }

    public String enviarCierreVenta(String method) throws Exception {
        String METHOD_NAME = method;

        if(METHOD_NAME != null && urlWebService != null){
            String SOAP_ACTION = NAMESPACE + METHOD_NAME;

            SoapSerializationEnvelope soapEnvelope = new SoapSerializationEnvelope( SoapEnvelope.VER11 );
            SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME);

            HttpTransportSE transport = new HttpTransportSE(urlWebService);
            request.addProperty("server", idServicio);
            request.addProperty("idVendedor", vendedor);
            request.addProperty("idEmpresa", idEmpresa);
            request.addProperty("idSucursal", idSucursal);
            soapEnvelope.dotNet = true;
            soapEnvelope.setOutputSoapObject(request);

            try {
                transport.call(SOAP_ACTION, soapEnvelope);

                SoapPrimitive result = (SoapPrimitive) soapEnvelope.getResponse();
                Log.i(TAG, "enviarCierreVenta:"+method+": "+result.toString());

                return result.toString();

            } catch (Exception e) {
                Log.e(TAG, "enviarCierreVenta:"+method+": "+e.getMessage());
                e.printStackTrace();
                throw new Exception(e);
            }

        }else {
            Log.e(TAG, "No se encontro el metodo Soap en Util.getMetodoSoap");
            return "E : No se encontro el metodo Soap";
        }
    }

    public String enviarAperturaVenta(String method) throws Exception {
        String METHOD_NAME = method;

        if(METHOD_NAME != null && urlWebService != null){
            String SOAP_ACTION = NAMESPACE + METHOD_NAME;

            SoapSerializationEnvelope soapEnvelope = new SoapSerializationEnvelope( SoapEnvelope.VER11 );
            SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME);

            HttpTransportSE transport = new HttpTransportSE(urlWebService);
            request.addProperty("server", idServicio);
            request.addProperty("idVendedor", vendedor);
            request.addProperty("idEmpresa", idEmpresa);
            request.addProperty("idSucursal", idSucursal);
            soapEnvelope.dotNet = true;
            soapEnvelope.setOutputSoapObject(request);

            try {
                transport.call(SOAP_ACTION, soapEnvelope);

                SoapPrimitive result = (SoapPrimitive) soapEnvelope.getResponse();
                Log.i(TAG, "enviarAperturaVenta:"+method+": "+result.toString());

                return result.toString();

            } catch (Exception e) {
                Log.e(TAG, "enviarAperturaVenta:"+method+": "+e.getMessage());
                e.printStackTrace();
                throw new Exception(e);
            }

        }else {
            Log.e(TAG, "No se encontro el metodo Soap en Util.getMetodoSoap");
            return "E : No se encontro el metodo Soap";
        }
    }

    public String facturarPedido(String method, String cadena) throws Exception {
        String METHOD_NAME = method;

        if(METHOD_NAME != null){
            String SOAP_ACTION = NAMESPACE + METHOD_NAME;

            Log.i( TAG, method +": " + cadena);

            SoapSerializationEnvelope Soapenvelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
            SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME);

            HttpTransportSE transporte = new HttpTransportSE(urlWebService);
            request.addProperty("cadena", cadena);
            request.addProperty("idEmpresa", idEmpresa);
            request.addProperty("idSucursal", idSucursal);
            request.addProperty("server", idServicio);
            Soapenvelope.dotNet = true;
            Soapenvelope.setOutputSoapObject(request);

            long beforecall = System.currentTimeMillis();

            try {
                transporte.call(SOAP_ACTION, Soapenvelope);
                Log.i( TAG, method + ": Respuesta en "  + (System.currentTimeMillis() - beforecall) + "miliseg");

                SoapPrimitive resultado_xml = (SoapPrimitive) Soapenvelope.getResponse();
                String res = resultado_xml.toString();

                Log.i(TAG,"facturarPedido Respuesta: " + res);

                return res;

            } catch (Exception e) {
                Log.e(TAG, method+ " NO SINCRONIZADO "+e.getMessage());
                e.printStackTrace();
                throw new Exception(e);
            }
        }else{
            Log.e(TAG, method+ " NO EXISTE EN EL WEBSERVICE");
            return "0";
        }
    }

    public String actualizarFechaSincronizacion(String method) throws Exception {
        String METHOD_NAME = method;

        if(METHOD_NAME != null && urlWebService != null){
            String SOAP_ACTION = NAMESPACE + METHOD_NAME;

            SoapSerializationEnvelope soapEnvelope = new SoapSerializationEnvelope( SoapEnvelope.VER11 );
            SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME);

            HttpTransportSE transport = new HttpTransportSE(urlWebService);
            request.addProperty("server", idServicio);
            request.addProperty("idVendedor", vendedor);
            request.addProperty("idEmpresa", idEmpresa);
            request.addProperty("idSucursal", idSucursal);
            soapEnvelope.dotNet = true;
            soapEnvelope.setOutputSoapObject(request);

            try {
                transport.call(SOAP_ACTION, soapEnvelope);

                SoapPrimitive result = (SoapPrimitive) soapEnvelope.getResponse();
                Log.i(TAG, "actualizarFechaSincronizacion:"+method+": "+result.toString());

                return result.toString();

            } catch (Exception e) {
                Log.e(TAG, "actualizarFechaSincronizacion:"+method+": "+e.getMessage());
                e.printStackTrace();
                throw new Exception(e);
            }

        }else {
            Log.e(TAG, "No se encontro el metodo Soap en Util.getMetodoSoap");
            return "E : No se encontro el metodo Soap";
        }
    }

    public String actualizarEntregaPedidos(String method, String cadena) throws Exception {
        String METHOD_NAME = method;

        if(METHOD_NAME != null){
            String SOAP_ACTION = NAMESPACE + METHOD_NAME;

            Log.i( TAG, method +": " + cadena);

            SoapSerializationEnvelope Soapenvelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
            SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME);

            HttpTransportSE transporte = new HttpTransportSE(urlWebService);
            request.addProperty("idEmpresa", idEmpresa);
            request.addProperty("idSucursal", idSucursal);
            request.addProperty("idVendedor", vendedor);
            request.addProperty("cadena", cadena);
            request.addProperty("server", idServicio);
            Soapenvelope.dotNet = true;
            Soapenvelope.setOutputSoapObject(request);

            long beforecall = System.currentTimeMillis();

            try {
                transporte.call(SOAP_ACTION, Soapenvelope);
                Log.i( TAG, method + ": Respuesta en "  + (System.currentTimeMillis() - beforecall) + "miliseg");

                SoapPrimitive resultado_xml = (SoapPrimitive) Soapenvelope.getResponse();
                String res = resultado_xml.toString();

                Log.i(TAG,"enviarPendientes Respuesta: " + res);

                return res;

            } catch (Exception e) {
                Log.e(TAG, method+ " NO ACTUALIZADO "+e.getMessage());
                e.printStackTrace();
                throw new Exception(e);
            }
        }else{
            Log.e(TAG, method+ " NO EXISTE EN EL WEBSERVICE");
            return "0";
        }
    }

    public String actualizarFormaPago(String method, HashMap<String,Object> pedidoEnviar) throws Exception {
        String METHOD_NAME = method;

        if(METHOD_NAME != null){
            String SOAP_ACTION = NAMESPACE + METHOD_NAME;

            Gson gson = new Gson();
            pedidoEnviar.put("idAlmacen", idAlmacen);
            String cadena = gson.toJson(pedidoEnviar);
            Log.i( TAG, method +": " + cadena);

            SoapSerializationEnvelope Soapenvelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
            SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME);

            HttpTransportSE transporte = new HttpTransportSE(urlWebService);
            request.addProperty("idEmpresa", idEmpresa);
            request.addProperty("idSucursal", idSucursal);
            request.addProperty("cadena", cadena);
            request.addProperty("server", idServicio);
            Soapenvelope.dotNet = true;
            Soapenvelope.setOutputSoapObject(request);

            long beforecall = System.currentTimeMillis();

            try {
                transporte.call(SOAP_ACTION, Soapenvelope);
                Log.i( TAG, method + ": Respuesta en "  + (System.currentTimeMillis() - beforecall) + "miliseg");

                SoapPrimitive resultado_xml = (SoapPrimitive) Soapenvelope.getResponse();
                String res = resultado_xml.toString();

                Log.i(TAG,"actualizarFormaPago Respuesta: " + res);

                return res;

            } catch (Exception e) {
                Log.e(TAG, method+ " NO ACTUALIZADO "+e.getMessage());
                e.printStackTrace();
                throw new Exception(e);
            }
        }else{
            Log.e(TAG, method+ " NO EXISTE EN EL WEBSERVICE");
            return "0";
        }
    }

    public DocumentoGeneradoModel obtenerDocumentoGenerado(String method, String numeroPedido) {
        String METHOD_NAME = method;
        DocumentoGeneradoModel documentoGeneradoModel = null;

        if(METHOD_NAME != null){
            String SOAP_ACTION = NAMESPACE + METHOD_NAME;
            SoapSerializationEnvelope Soapenvelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
            SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME);

            HttpTransportSE transporte = new HttpTransportSE(urlWebService);
            request.addProperty("idEmpresa", idEmpresa);
            request.addProperty("idSucursal", idSucursal);
            request.addProperty("idVendedor", vendedor);
            request.addProperty("numeroPedido", numeroPedido);
            Log.i(TAG,"SEND:"+idEmpresa+","+idSucursal+","+vendedor+","+numeroPedido);

            //request.addProperty("numeroGuia", numeroGuia);
            request.addProperty("server", idServicio);
            Soapenvelope.dotNet = true;
            Soapenvelope.setOutputSoapObject(request);

            long beforecall = System.currentTimeMillis();

            try {
                transporte.call(SOAP_ACTION, Soapenvelope);
                Log.i( TAG, method + ": Respuesta en "  + (System.currentTimeMillis() - beforecall) + "miliseg");

                SoapPrimitive resultado_xml = (SoapPrimitive) Soapenvelope.getResponse();
                JSONObject jsonData = new JSONObject(resultado_xml.toString());

                String xnumeroPedido    =  jsonData.getString("numeroPedido");
                String xnumeroGuia      =  jsonData.getString("numeroGuia");
                String xserieDocumento  =  jsonData.getString("serieDocumento");
                String xnumeroDocumento =  jsonData.getString("numeroDocumento");
                String PDFBase64        =  jsonData.getString("PDFBase64");
                String error            =  jsonData.getString("error");

                documentoGeneradoModel = new DocumentoGeneradoModel();
                documentoGeneradoModel.setNumeroPedido(xnumeroPedido);
                documentoGeneradoModel.setNumeroGuia(xnumeroGuia);
                documentoGeneradoModel.setSerieDocumento(xserieDocumento);
                documentoGeneradoModel.setNumeroDocumento(xnumeroDocumento);
                documentoGeneradoModel.setPDFBase64(PDFBase64);
                documentoGeneradoModel.setError(error);
                Log.i(TAG, "GET:"+xnumeroPedido+","+xnumeroGuia+","+xserieDocumento+","+xnumeroDocumento+","+error);
                return documentoGeneradoModel;
            } catch (Exception e) {
                Log.e(TAG, method+ " NO SINCRONIZADO "+e.getMessage());
                e.printStackTrace();
            }
        }else{
            Log.e(TAG, method+ " NO EXISTE EN EL WEBSERVICE");
        }
        return documentoGeneradoModel;
    }

    public String enviarEncuesta(String method, String cadena) throws Exception {
        String METHOD_NAME = method;

        if(METHOD_NAME != null && urlWebService != null){
            String SOAP_ACTION = NAMESPACE + METHOD_NAME;

            SoapSerializationEnvelope soapEnvelope = new SoapSerializationEnvelope( SoapEnvelope.VER11 );
            SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME);

            HttpTransportSE transport = new HttpTransportSE(urlWebService,TIEMPO_ESPERA);
            request.addProperty("idEmpresa", idEmpresa);
            request.addProperty("idSucursal", idSucursal);
            request.addProperty("cadena", cadena);
            request.addProperty("server", idServicio);
            soapEnvelope.dotNet = true;
            soapEnvelope.setOutputSoapObject(request);


                transport.call(SOAP_ACTION, soapEnvelope);

                SoapPrimitive result = (SoapPrimitive) soapEnvelope.getResponse();
                Log.i(TAG, "enviarEncuesta:" + method + ": " + result.toString());

                return result.toString();


        }else {
            Log.e(TAG, "No se encontro el metodo Soap en Util.getMetodoSoap");
            return "E : No se encontro el metodo Soap";
        }
    }

    public String enviarFoto(String method, String fotoString, String nombreArchivo) throws Exception {
        String METHOD_NAME = method;

        if(METHOD_NAME != null && urlWebService != null){
            String SOAP_ACTION = NAMESPACE + METHOD_NAME;

            SoapSerializationEnvelope soapEnvelope = new SoapSerializationEnvelope( SoapEnvelope.VER11 );
            SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME);

            HttpTransportSE transport = new HttpTransportSE(urlWebService);
            request.addProperty("server", idServicio);
            request.addProperty("idVendedor", vendedor);
            request.addProperty("idEmpresa", idEmpresa);
            request.addProperty("idSucursal", idSucursal);
            request.addProperty("f", fotoString);
            request.addProperty("fileName", nombreArchivo);
            soapEnvelope.dotNet = true;
            soapEnvelope.setOutputSoapObject(request);

            try {
                transport.call(SOAP_ACTION, soapEnvelope);

                SoapPrimitive result = (SoapPrimitive) soapEnvelope.getResponse();
                Log.i(TAG, "enviarEncuesta:"+method+": "+result.toString());

                return result.toString();

            } catch (Exception e) {
                Log.e(TAG, "enviarEncuesta:"+method+": "+e.getMessage());
                e.printStackTrace();
                throw new Exception(e);
            }

        }else {
            Log.e(TAG, "No se encontro el metodo Soap en Util.getMetodoSoap");
            return "E : No se encontro el metodo Soap";
        }
    }

    public boolean checkColumnasTablaProductos() throws Exception {
        boolean out = helper.isFieldExist(TablesHelper.Producto.Table,"idUnidadMenor");
        out = out | helper.isFieldExist(TablesHelper.Producto.Table,"idUnidadMayor");
        out = out | helper.isFieldExist(TablesHelper.Producto.Table,"factorConversion");
        return out;
    }

    public boolean checkColumnasPoliticaPrecioxProducto() throws Exception {
        boolean out = helper.isFieldExist(TablesHelper.PoliticaPrecioxProducto.Table,"precioContenido");
        out = out | helper.isFieldExist(TablesHelper.PoliticaPrecioxProducto.Table,"precioManejo");
        return out;
    }


    public void obtenerRegistrosUnidadesMedidaxProductoJSON(String method, final String table) throws Exception {
        String METHOD_NAME = method;

        if(METHOD_NAME != null && urlWebService != null){
            String SOAP_ACTION = NAMESPACE + METHOD_NAME;
            Log.d(TAG,"idEmpresa:"+idEmpresa);
            Log.d(TAG,"idSucursal:"+idSucursal);
            Log.d(TAG,"idVendedor:"+vendedor);
            Log.d(TAG,"server:"+idServicio);
            SoapSerializationEnvelope soapEnvelope = new SoapSerializationEnvelope( SoapEnvelope.VER11 );
            SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME);

            HttpTransportSE transport = new HttpTransportSE(urlWebService,TIEMPO_ESPERA);
            request.addProperty("idEmpresa", idEmpresa);
            request.addProperty("idSucursal", idSucursal);
            request.addProperty("idVendedor", vendedor);
            request.addProperty("server", idServicio);
            soapEnvelope.dotNet = true;
            soapEnvelope.setOutputSoapObject(request);

            long beforeCall = System.currentTimeMillis();

            try {
                transport.call(SOAP_ACTION, soapEnvelope);
                Log.i( TAG, table + ": Respuesta en "  + (System.currentTimeMillis() - beforeCall) + "miliseg");

                SoapPrimitive result = (SoapPrimitive) soapEnvelope.getResponse();
                final JSONArray jsonstring = new JSONArray(result.toString());

                Log.i(TAG, table + ": " + jsonstring.length() + " registros");
                Log.v(TAG, jsonstring.toString());


                //Buscamos el metodo en la clase DataBaseHelper para insertar el json retornado
                if(table.equals("ObjPedido")){
                    Log.i(TAG,result.toString());
                    final Method helperMethod = helper.getClass().getMethod("actualizar"+table, JSONArray.class , String.class );
                    long startTime = System.nanoTime();
                    Thread thread = new Thread() {
                        public void run() {
                            try {
                                helperMethod.invoke(helper,jsonstring,vendedor);
                                Log.i(TAG, table + " SINCRONIZADO");
                            } catch (IllegalAccessException e) {
                                e.printStackTrace();
                            } catch (InvocationTargetException e) {
                                e.printStackTrace();
                            }
                        }
                    };
                    thread.start();
                    long stopTime = System.nanoTime();
                    long timeSaveAqlite = (stopTime - startTime)/100000;
                    System.out.println("Finalizado guardado en " + table + "  " + String.valueOf(timeSaveAqlite) + " mseg");
//                    helperMethod.invoke(helper,jsonstring,vendedor);
                }else {
                    final Method helperMethod = helper.getClass().getMethod("actualizar"+table, JSONArray.class );
                    long startTime = System.nanoTime();
                    Thread thread = new Thread() {
                        public void run() {
                            try {
                                helperMethod.invoke(helper,jsonstring);
                                Log.i(TAG, table + " SINCRONIZADO");
                            } catch (IllegalAccessException e) {
                                e.printStackTrace();
                            } catch (InvocationTargetException e) {
                                e.printStackTrace();
                            }
                        }
                    };
                    thread.start();
                    long stopTime = System.nanoTime();
                    long timeSaveAqlite = (stopTime - startTime)/100000;
                    System.out.println("Finalizado guardado en " + table + "  " + String.valueOf(timeSaveAqlite) + " mseg");
//                    helperMethod.invoke(helper,jsonstring);
                }

                Log.i(TAG, table + " SINCRONIZADO");
            } catch (NoSuchMethodException e) {
                Log.e(TAG, "NO SINCRONIZADO : No existe el metodo actualizar"+ table +" en DataBaseHelper");
                e.printStackTrace();
                throw new NoSuchMethodException();//atrapa la excepcion pero la vuelve a lanzar (para que la la actividad la vuelva a detectar)
            } catch (SocketTimeoutException e) {
                //Error relacionado a la webservice
                Log.e(TAG, table + " SOCKETTIMEOUT EXCEPTION :" + e.getMessage());
                e.printStackTrace();
                throw new SocketTimeoutException();
            } catch (IOException e) {
                //Error relacionado a la webservice
                Log.e(TAG, table + " IO EXCEPTION:" + e.getMessage());
                //ex.printStackTrace();
                throw new IOException(e);
            } catch (JSONException e) {
                //Error relacionado a la webservice
                Log.e(TAG, table + " JSON EXCEPTION:" + e.getMessage());
                //ex.printStackTrace();
                throw new RuntimeException(e);
            } catch (Exception e) {
                //Error relacionado a la webservice
                if (e.getCause() != null) {
                    Log.e(TAG, table + " GENERAL EXCEPTION CAUSE:" + e.getCause().getMessage());
                    throw new Exception(e.getCause());
                }else {
                    Log.e(TAG, table + " GENERAL EXCEPTION:" + e.getMessage());
                    throw new Exception(e);
                }
            }

        }else
            Log.e(TAG, "No se encontro el metodo Soap para  sincronizar la tabla "+table+" en Util.getMetodoSoap");
    }


    public void obtenerBonificaionesJSON(String method, final String table) throws Exception {
        String METHOD_NAME = method;

        if(METHOD_NAME != null && urlWebService != null){
            String SOAP_ACTION = NAMESPACE + METHOD_NAME;
            Log.d(TAG,"idEmpresa:"+idEmpresa);
            Log.d(TAG,"server:"+idServicio);
            SoapSerializationEnvelope soapEnvelope = new SoapSerializationEnvelope( SoapEnvelope.VER11 );
            SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME);

            HttpTransportSE transport = new HttpTransportSE(urlWebService,TIEMPO_ESPERA);
            request.addProperty("idEmpresa", idEmpresa);
            request.addProperty("server", idServicio);
            switch (method)
            {
                case "obtenerPromBonificacionSucursal_json":
                    request.addProperty("idSucursal", idSucursal);
                    break;
            }
            soapEnvelope.dotNet = true;
            soapEnvelope.setOutputSoapObject(request);

            long beforeCall = System.currentTimeMillis();

            try {
                transport.call(SOAP_ACTION, soapEnvelope);
                Log.i( TAG, table + ": Respuesta en "  + (System.currentTimeMillis() - beforeCall) + "miliseg");

                SoapPrimitive result = (SoapPrimitive) soapEnvelope.getResponse();
                final JSONArray jsonstring = new JSONArray(result.toString());

                Log.i(TAG, table + ": " + jsonstring.length() + " registros");
                Log.v(TAG, jsonstring.toString());


                //Buscamos el metodo en la clase DataBaseHelper para insertar el json retornado
                if(table.equals("ObjPedido")){
                    Log.i(TAG,result.toString());
                    final Method helperMethod = helper.getClass().getMethod("actualizar"+table, JSONArray.class , String.class );
                    long startTime = System.nanoTime();
                    Thread thread = new Thread() {
                        public void run() {
                            try {
                                helperMethod.invoke(helper,jsonstring,vendedor);
                                Log.i(TAG, table + " SINCRONIZADO");
                            } catch (IllegalAccessException e) {
                                e.printStackTrace();
                            } catch (InvocationTargetException e) {
                                e.printStackTrace();
                            }
                        }
                    };
                    thread.start();
                    long stopTime = System.nanoTime();
                    long timeSaveAqlite = (stopTime - startTime)/100000;
                    System.out.println("Finalizado guardado en " + table + "  " + String.valueOf(timeSaveAqlite) + " mseg");
//                    helperMethod.invoke(helper,jsonstring,vendedor);
                }else {
                    final Method helperMethod = helper.getClass().getMethod("actualizar"+table, JSONArray.class );
                    long startTime = System.nanoTime();
                    Thread thread = new Thread() {
                        public void run() {
                            try {
                                helperMethod.invoke(helper,jsonstring);
                                Log.i(TAG, table + " SINCRONIZADO");
                            } catch (IllegalAccessException e) {
                                e.printStackTrace();
                            } catch (InvocationTargetException e) {
                                e.printStackTrace();
                            }
                        }
                    };
                    thread.start();
                    long stopTime = System.nanoTime();
                    long timeSaveAqlite = (stopTime - startTime)/100000;
                    System.out.println("Finalizado guardado en " + table + "  " + String.valueOf(timeSaveAqlite) + " mseg");
//                    helperMethod.invoke(helper,jsonstring);
                }

                Log.i(TAG, table + " SINCRONIZADO");
            } catch (NoSuchMethodException e) {
                Log.e(TAG, "NO SINCRONIZADO : No existe el metodo actualizar"+ table +" en DataBaseHelper");
                e.printStackTrace();
                throw new NoSuchMethodException();//atrapa la excepcion pero la vuelve a lanzar (para que la la actividad la vuelva a detectar)
            } catch (SocketTimeoutException e) {
                //Error relacionado a la webservice
                Log.e(TAG, table + " SOCKETTIMEOUT EXCEPTION :" + e.getMessage());
                e.printStackTrace();
                throw new SocketTimeoutException();
            } catch (IOException e) {
                //Error relacionado a la webservice
                Log.e(TAG, table + " IO EXCEPTION:" + e.getMessage());
                //ex.printStackTrace();
                throw new IOException(e);
            } catch (JSONException e) {
                //Error relacionado a la webservice
                Log.e(TAG, table + " JSON EXCEPTION:" + e.getMessage());
                //ex.printStackTrace();
                throw new RuntimeException(e);
            } catch (Exception e) {
                //Error relacionado a la webservice
                if (e.getCause() != null) {
                    Log.e(TAG, table + " GENERAL EXCEPTION CAUSE:" + e.getCause().getMessage());
                    throw new Exception(e.getCause());
                }else {
                    Log.e(TAG, table + " GENERAL EXCEPTION:" + e.getMessage());
                    throw new Exception(e);
                }
            }

        }else
            Log.e(TAG, "No se encontro el metodo Soap para  sincronizar la tabla "+table+" en Util.getMetodoSoap");
    }

    public void obtenerRutasJSON(String method, final String table) throws Exception {
        String METHOD_NAME = method;

        if(METHOD_NAME != null && urlWebService != null){
            String SOAP_ACTION = NAMESPACE + METHOD_NAME;
            Log.d(TAG,"idEmpresa:"+idEmpresa);
            Log.d(TAG,"idSucursal:"+idSucursal);
            Log.d(TAG,"server:"+idServicio);
            SoapSerializationEnvelope soapEnvelope = new SoapSerializationEnvelope( SoapEnvelope.VER11 );
            SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME);

            HttpTransportSE transport = new HttpTransportSE(urlWebService,TIEMPO_ESPERA);
            request.addProperty("idEmpresa", idEmpresa);
            request.addProperty("idSucursal", idSucursal);
            request.addProperty("server", idServicio);
            soapEnvelope.dotNet = true;
            soapEnvelope.setOutputSoapObject(request);

            long beforeCall = System.currentTimeMillis();

            try {
                transport.call(SOAP_ACTION, soapEnvelope);
                Log.i( TAG, table + ": Respuesta en "  + (System.currentTimeMillis() - beforeCall) + "miliseg");

                SoapPrimitive result = (SoapPrimitive) soapEnvelope.getResponse();
                final JSONArray jsonstring = new JSONArray(result.toString());

                Log.i(TAG, table + ": " + jsonstring.length() + " registros");
                Log.v(TAG, jsonstring.toString());


                //Buscamos el metodo en la clase DataBaseHelper para insertar el json retornado
                if(table.equals("ObjPedido")){
                    Log.i(TAG,result.toString());
                    final Method helperMethod = helper.getClass().getMethod("actualizar"+table, JSONArray.class , String.class );
                    long startTime = System.nanoTime();
                    Thread thread = new Thread() {
                        public void run() {
                            try {
                                helperMethod.invoke(helper,jsonstring,vendedor);
                                Log.i(TAG, table + " SINCRONIZADO");
                            } catch (IllegalAccessException e) {
                                e.printStackTrace();
                            } catch (InvocationTargetException e) {
                                e.printStackTrace();
                            }
                        }
                    };
                    thread.start();
                    long stopTime = System.nanoTime();
                    long timeSaveAqlite = (stopTime - startTime)/100000;
                    System.out.println("Finalizado guardado en " + table + "  " + String.valueOf(timeSaveAqlite) + " mseg");
//                    helperMethod.invoke(helper,jsonstring,vendedor);
                }else {
                    final Method helperMethod = helper.getClass().getMethod("actualizar"+table, JSONArray.class );
                    long startTime = System.nanoTime();
                    Thread thread = new Thread() {
                        public void run() {
                            try {
                                helperMethod.invoke(helper,jsonstring);
                                Log.i(TAG, table + " SINCRONIZADO");
                            } catch (IllegalAccessException e) {
                                e.printStackTrace();
                            } catch (InvocationTargetException e) {
                                e.printStackTrace();
                            }
                        }
                    };
                    thread.start();
                    long stopTime = System.nanoTime();
                    long timeSaveAqlite = (stopTime - startTime)/100000;
                    System.out.println("Finalizado guardado en " + table + "  " + String.valueOf(timeSaveAqlite) + " mseg");
//                    helperMethod.invoke(helper,jsonstring);
                }

                Log.i(TAG, table + " SINCRONIZADO");
            } catch (NoSuchMethodException e) {
                Log.e(TAG, "NO SINCRONIZADO : No existe el metodo actualizar"+ table +" en DataBaseHelper");
                e.printStackTrace();
                throw new NoSuchMethodException();//atrapa la excepcion pero la vuelve a lanzar (para que la la actividad la vuelva a detectar)
            } catch (SocketTimeoutException e) {
                //Error relacionado a la webservice
                Log.e(TAG, table + " SOCKETTIMEOUT EXCEPTION :" + e.getMessage());
                e.printStackTrace();
                throw new SocketTimeoutException();
            } catch (IOException e) {
                //Error relacionado a la webservice
                Log.e(TAG, table + " IO EXCEPTION:" + e.getMessage());
                //ex.printStackTrace();
                throw new IOException(e);
            } catch (JSONException e) {
                //Error relacionado a la webservice
                Log.e(TAG, table + " JSON EXCEPTION:" + e.getMessage());
                //ex.printStackTrace();
                throw new RuntimeException(e);
            } catch (Exception e) {
                //Error relacionado a la webservice
                if (e.getCause() != null) {
                    Log.e(TAG, table + " GENERAL EXCEPTION CAUSE:" + e.getCause().getMessage());
                    throw new Exception(e.getCause());
                }else {
                    Log.e(TAG, table + " GENERAL EXCEPTION:" + e.getMessage());
                    throw new Exception(e);
                }
            }

        }else
            Log.e(TAG, "No se encontro el metodo Soap para  sincronizar la tabla "+table+" en Util.getMetodoSoap");
    }

    public boolean checkColumnasTablaPedidoDetalle() throws Exception {
        boolean out = helper.isFieldExist(TablesHelper.PedidoDetalle.Table,"malla");
        out &= helper.isFieldExist(TablesHelper.PedidoDetalle.Table,"estadoDetalle");
        return out;
    }

    public JSONArray obtenerStockProductox2(String method, String idProducto, String numeroPedidoActual) throws Exception{
        String METHOD_NAME = method;

        if(METHOD_NAME != null){
            String SOAP_ACTION = NAMESPACE + METHOD_NAME;

            Log.i( TAG, method +": " + idProducto);

            SoapSerializationEnvelope Soapenvelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
            SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME);

            HttpTransportSE transporte = new HttpTransportSE(urlWebService);
            request.addProperty("numeroPedido",numeroPedidoActual);
            request.addProperty("idProducto", idProducto);
            request.addProperty("idAlmacen", idAlmacen);
            request.addProperty("idEmpresa", idEmpresa);
            request.addProperty("idSucursal", idSucursal);
            //request.addProperty("numeroGuia", numeroGuia);
            request.addProperty("server", idServicio);
            Soapenvelope.dotNet = true;
            Soapenvelope.setOutputSoapObject(request);

            long beforecall = System.currentTimeMillis();

            try {
                transporte.call(SOAP_ACTION, Soapenvelope);
                Log.i( TAG, method + ": Respuesta en "  + (System.currentTimeMillis() - beforecall) + "miliseg");

                SoapPrimitive resultado_xml = (SoapPrimitive) Soapenvelope.getResponse();
                JSONArray jsonstring = new JSONArray(resultado_xml.toString());
                return  jsonstring;
            } catch (Exception e) {
                Log.e(TAG, method+ " NO SINCRONIZADO "+e.getMessage());
                e.printStackTrace();
                throw new Exception(e);
            }
        }else{
            Log.e(TAG, method+ " NO EXISTE EN EL WEBSERVICE");
        }
        return null;
    }


    /*
     * Envia datos para el registro en la base de datos del whatsapp del cliente
     */
    public int registroWhatsappJSON(String method, final String table, final String idCliente, final String whathsapp, final String codigociudad, final String telefonofijo, final String email) throws Exception {
        Log.d(TAG,"idEmpresa:"+idEmpresa);
        Log.d(TAG,"idSucursal:"+idSucursal);
        Log.d(TAG,"server:"+idServicio);
        Log.d(TAG,"idCliente:"+idCliente);
        Log.d(TAG,"whathsapp:"+whathsapp);
        Log.d(TAG,"codigociudad:"+codigociudad);
        Log.d(TAG,"telefonofijo:"+telefonofijo);
        Log.d(TAG,"email:"+email);
        String METHOD_NAME = method;

        if(METHOD_NAME != null && urlWebService != null){
            String SOAP_ACTION = NAMESPACE + METHOD_NAME;

            SoapSerializationEnvelope soapEnvelope = new SoapSerializationEnvelope( SoapEnvelope.VER11 );
            SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME);

            HttpTransportSE transport = new HttpTransportSE(urlWebService, TIEMPO_ESPERA);

            if (method.equals(TablesHelper.Usuario.Sincronizar) || method.equals(TablesHelper.Vendedor.Sincronizar) || method.equals(TablesHelper.Empresa.Sincronizar))
                transport = new HttpTransportSE(urlWebService, TIEMPO_ESPERA/2);

            request.addProperty("idEmpresa", idEmpresa);
            request.addProperty("idSucursal", idSucursal);
            request.addProperty("idCliente", idCliente);
            request.addProperty("whathsapp", whathsapp);
            request.addProperty("codigo_ciudad", codigociudad);
            request.addProperty("telefono_fijo", telefonofijo);
            request.addProperty("email", email);
            request.addProperty("server", idServicio);
            soapEnvelope.dotNet = true;
            soapEnvelope.setOutputSoapObject(request);

            long beforeCall = System.currentTimeMillis();

            try {
                transport.call(SOAP_ACTION, soapEnvelope);
                Log.i( TAG, table + ": Respuesta en "  + (System.currentTimeMillis() - beforeCall) + "miliseg");

                SoapPrimitive result = (SoapPrimitive) soapEnvelope.getResponse();
                final JSONObject jsonstring = new JSONObject(result.toString());
                long startTime = System.nanoTime();

                //TODO evaluar jsonstring para indicar si el request fue un exito
                return jsonstring.getInt("rpta");

            }  catch (SocketTimeoutException e) {
                //Error relacionado a la webservice
                Log.e(TAG, table + " SOCKETTIMEOUT EXCEPTION :" + e.getMessage());
                e.printStackTrace();
                throw new SocketTimeoutException();
            } catch (IOException e) {
                //Error relacionado a la webservice
                Log.e(TAG, table + " IO EXCEPTION:" + e.getMessage());
                //ex.printStackTrace();
                throw new IOException(e);
            } catch (JSONException e) {
                //Error relacionado a la webservice
                Log.e(TAG, table + " JSON EXCEPTION:" + e.getMessage());
                //ex.printStackTrace();
                throw new RuntimeException(e);
            } catch (Exception e) {
                //Error relacionado a la webservice
                if (e.getCause() != null) {
                    Log.e(TAG, table + " GENERAL EXCEPTION CAUSE:" + e.getCause().getMessage());
                    throw new Exception(e.getCause());
                }else {
                    Log.e(TAG, table + " GENERAL EXCEPTION:" + e.getMessage());
                    throw new Exception(e);
                }
            }

        }else {
            Log.e(TAG, "No se encontro el metodo Soap para  sincronizar la tabla " + table + " en Util.getMetodoSoap");
        }

        return 0;
    }


    /*
     * Obtiene la lista de whatsapps de una empresa y sucursal
     */
    public void obtenerWhathsappsJSON(String method, final String table) throws Exception {
        Log.d(TAG,"idEmpresa:"+idEmpresa);
        Log.d(TAG,"idSucursal:"+idSucursal);
        Log.d(TAG,"server:"+idServicio);
        String METHOD_NAME = method;

        if(METHOD_NAME != null && urlWebService != null){
            String SOAP_ACTION = NAMESPACE + METHOD_NAME;

            SoapSerializationEnvelope soapEnvelope = new SoapSerializationEnvelope( SoapEnvelope.VER11 );
            SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME);

            HttpTransportSE transport = new HttpTransportSE(urlWebService, TIEMPO_ESPERA);

            if (method.equals(TablesHelper.Usuario.Sincronizar) || method.equals(TablesHelper.Vendedor.Sincronizar) || method.equals(TablesHelper.Empresa.Sincronizar))
                transport = new HttpTransportSE(urlWebService, TIEMPO_ESPERA/2);

            request.addProperty("idEmpresa", idEmpresa);
            request.addProperty("idSucursal", idSucursal);
            request.addProperty("server", idServicio);
            soapEnvelope.dotNet = true;
            soapEnvelope.setOutputSoapObject(request);

            long beforeCall = System.currentTimeMillis();

            try {
                transport.call(SOAP_ACTION, soapEnvelope);
                Log.i( TAG, table + ": Respuesta en "  + (System.currentTimeMillis() - beforeCall) + "miliseg");

                SoapPrimitive result = (SoapPrimitive) soapEnvelope.getResponse();
                final JSONArray jsonstring = new JSONArray(result.toString());

                Log.i(TAG, table + ": " + jsonstring.length() + " registros");
                long startTime = System.nanoTime();
                //Buscamos el metodo en la clase DataBaseHelper para insertar el json retornado

                final Method helperMethod = helper.getClass().getMethod("actualizar"+table, JSONArray.class );
                Thread thread = new Thread() {
                    public void run() {
                        try {
                            helperMethod.invoke(helper,jsonstring);
                            Log.i(TAG, table + " SINCRONIZADO");
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        } catch (InvocationTargetException e) {
                            e.printStackTrace();
                        }
                    }
                };
                thread.start();
                long stopTime = System.nanoTime();
                long timeSaveAqlite = (stopTime - startTime)/100000;
                System.out.println("Finalizado guardado en " + table + "  " + String.valueOf(timeSaveAqlite) + " mseg");

            } catch (NoSuchMethodException e) {
                Log.e(TAG, "NO SINCRONIZADO : No existe el metodo actualizar"+ table +" en DataBaseHelper");
                e.printStackTrace();
                throw new NoSuchMethodException();//atrapa la excepcion pero la vuelve a lanzar (para que la la actividad la vuelva a detectar)
            } catch (SocketTimeoutException e) {
                //Error relacionado a la webservice
                Log.e(TAG, table + " SOCKETTIMEOUT EXCEPTION :" + e.getMessage());
                e.printStackTrace();
                throw new SocketTimeoutException();
            } catch (IOException e) {
                //Error relacionado a la webservice
                Log.e(TAG, table + " IO EXCEPTION:" + e.getMessage());
                //ex.printStackTrace();
                throw new IOException(e);
            } catch (JSONException e) {
                //Error relacionado a la webservice
                Log.e(TAG, table + " JSON EXCEPTION:" + e.getMessage());
                //ex.printStackTrace();
                throw new RuntimeException(e);
            } catch (Exception e) {
                //Error relacionado a la webservice
                if (e.getCause() != null) {
                    Log.e(TAG, table + " GENERAL EXCEPTION CAUSE:" + e.getCause().getMessage());
                    throw new Exception(e.getCause());
                }else {
                    Log.e(TAG, table + " GENERAL EXCEPTION:" + e.getMessage());
                    throw new Exception(e);
                }
            }

        }else
            Log.e(TAG, "No se encontro el metodo Soap para  sincronizar la tabla "+table+" en Util.getMetodoSoap");
    }

    /*
     * Envia datos para indicar el motivo de baja del cliente
     */
    public int darBajaClienteJSON(String method, final String table, final String idCliente, final String motivo, final String flag, final String magic) throws Exception {
        Log.d(TAG,"idEmpresa:"+idEmpresa);
        Log.d(TAG,"idSucursal:"+idSucursal);
        Log.d(TAG,"server:"+idServicio);
        Log.d(TAG,"idCliente:"+idCliente);
        Log.d(TAG,"motivo:"+motivo);
        Log.d(TAG,"flag:"+flag);
        Log.d(TAG,"magic:"+magic);
        String METHOD_NAME = method;

        if(METHOD_NAME != null && urlWebService != null){
            String SOAP_ACTION = NAMESPACE + METHOD_NAME;

            SoapSerializationEnvelope soapEnvelope = new SoapSerializationEnvelope( SoapEnvelope.VER11 );
            SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME);

            HttpTransportSE transport = new HttpTransportSE(urlWebService, TIEMPO_ESPERA);

            if (method.equals(TablesHelper.Usuario.Sincronizar) || method.equals(TablesHelper.Vendedor.Sincronizar) || method.equals(TablesHelper.Empresa.Sincronizar))
                transport = new HttpTransportSE(urlWebService, TIEMPO_ESPERA/2);

            request.addProperty("idEmpresa", idEmpresa);
            request.addProperty("idSucursal", idSucursal);
            request.addProperty("idCliente", idCliente);
            request.addProperty("motivo", motivo);
            request.addProperty("flag", flag);
            request.addProperty("magic", magic);
            request.addProperty("server", idServicio);
            soapEnvelope.dotNet = true;
            soapEnvelope.setOutputSoapObject(request);

            long beforeCall = System.currentTimeMillis();

            try {
                transport.call(SOAP_ACTION, soapEnvelope);
                Log.i( TAG, table + ": Respuesta en "  + (System.currentTimeMillis() - beforeCall) + "miliseg");

                SoapPrimitive result = (SoapPrimitive) soapEnvelope.getResponse();
                final JSONArray jsonArray = new JSONArray(result.toString());
                long startTime = System.nanoTime();

                return jsonArray.length();

            }  catch (SocketTimeoutException e) {
                //Error relacionado a la webservice
                Log.e(TAG, table + " SOCKETTIMEOUT EXCEPTION :" + e.getMessage());
                e.printStackTrace();
                throw new SocketTimeoutException();
            } catch (IOException e) {
                //Error relacionado a la webservice
                Log.e(TAG, table + " IO EXCEPTION:" + e.getMessage());
                //ex.printStackTrace();
                throw new IOException(e);
            } catch (JSONException e) {
                //Error relacionado a la webservice
                Log.e(TAG, table + " JSON EXCEPTION:" + e.getMessage());
                //ex.printStackTrace();
                throw new RuntimeException(e);
            } catch (Exception e) {
                //Error relacionado a la webservice
                if (e.getCause() != null) {
                    Log.e(TAG, table + " GENERAL EXCEPTION CAUSE:" + e.getCause().getMessage());
                    throw new Exception(e.getCause());
                }else {
                    Log.e(TAG, table + " GENERAL EXCEPTION:" + e.getMessage());
                    throw new Exception(e);
                }
            }

        }else {
            Log.e(TAG, "No se encontro el metodo Soap para  sincronizar la tabla " + table + " en Util.getMetodoSoap");
        }

        return -1;
    }


    /*
     * Obtiene los datos de los clientes dados de baja
     */
    public void obtenerClientesBajaJSON(String method, final String table) throws Exception {
        Log.d(TAG,"idEmpresa:"+idEmpresa);
        Log.d(TAG,"idSucursal:"+idSucursal);
        Log.d(TAG,"server:"+idServicio);
        String METHOD_NAME = method;

        if(METHOD_NAME != null && urlWebService != null){
            String SOAP_ACTION = NAMESPACE + METHOD_NAME;

            SoapSerializationEnvelope soapEnvelope = new SoapSerializationEnvelope( SoapEnvelope.VER11 );
            SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME);

            HttpTransportSE transport = new HttpTransportSE(urlWebService, TIEMPO_ESPERA);

            if (method.equals(TablesHelper.Usuario.Sincronizar) || method.equals(TablesHelper.Vendedor.Sincronizar) || method.equals(TablesHelper.Empresa.Sincronizar))
                transport = new HttpTransportSE(urlWebService, TIEMPO_ESPERA/2);

            request.addProperty("idEmpresa", idEmpresa);
            request.addProperty("idSucursal", idSucursal);
            request.addProperty("server", idServicio);
            soapEnvelope.dotNet = true;
            soapEnvelope.setOutputSoapObject(request);

            long beforeCall = System.currentTimeMillis();

            try {
                transport.call(SOAP_ACTION, soapEnvelope);
                Log.i( TAG, table + ": Respuesta en "  + (System.currentTimeMillis() - beforeCall) + "miliseg");

                SoapPrimitive result = (SoapPrimitive) soapEnvelope.getResponse();
                final JSONArray jsonstring = new JSONArray(result.toString());

                Log.i(TAG, table + ": " + jsonstring.length() + " registros");
                long startTime = System.nanoTime();
                //Buscamos el metodo en la clase DataBaseHelper para insertar el json retornado

                final Method helperMethod = helper.getClass().getMethod("actualizar"+table, JSONArray.class );
                Thread thread = new Thread() {
                    public void run() {
                        try {
                            helperMethod.invoke(helper,jsonstring);
                            Log.i(TAG, table + " SINCRONIZADO");
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        } catch (InvocationTargetException e) {
                            e.printStackTrace();
                        }
                    }
                };
                thread.start();
                long stopTime = System.nanoTime();
                long timeSaveAqlite = (stopTime - startTime)/100000;
                System.out.println("Finalizado guardado en " + table + "  " + String.valueOf(timeSaveAqlite) + " mseg");

            } catch (NoSuchMethodException e) {
                Log.e(TAG, "NO SINCRONIZADO : No existe el metodo actualizar"+ table +" en DataBaseHelper");
                e.printStackTrace();
                throw new NoSuchMethodException();//atrapa la excepcion pero la vuelve a lanzar (para que la la actividad la vuelva a detectar)
            } catch (SocketTimeoutException e) {
                //Error relacionado a la webservice
                Log.e(TAG, table + " SOCKETTIMEOUT EXCEPTION :" + e.getMessage());
                e.printStackTrace();
                throw new SocketTimeoutException();
            } catch (IOException e) {
                //Error relacionado a la webservice
                Log.e(TAG, table + " IO EXCEPTION:" + e.getMessage());
                //ex.printStackTrace();
                throw new IOException(e);
            } catch (JSONException e) {
                //Error relacionado a la webservice
                Log.e(TAG, table + " JSON EXCEPTION:" + e.getMessage());
                //ex.printStackTrace();
                throw new RuntimeException(e);
            } catch (Exception e) {
                //Error relacionado a la webservice
                if (e.getCause() != null) {
                    Log.e(TAG, table + " GENERAL EXCEPTION CAUSE:" + e.getCause().getMessage());
                    throw new Exception(e.getCause());
                }else {
                    Log.e(TAG, table + " GENERAL EXCEPTION:" + e.getMessage());
                    throw new Exception(e);
                }
            }

        }else
            Log.e(TAG, "No se encontro el metodo Soap para  sincronizar la tabla "+table+" en Util.getMetodoSoap");
    }


    /*
     * Obtiene los datos de los clientes dados de baja
     */
    public void obtenerMotivosBajaJSON(String method, final String table) throws Exception {
        Log.d(TAG,"idEmpresa:"+idEmpresa);
        Log.d(TAG,"server:"+idServicio);
        String METHOD_NAME = method;

        if(METHOD_NAME != null && urlWebService != null){
            String SOAP_ACTION = NAMESPACE + METHOD_NAME;

            SoapSerializationEnvelope soapEnvelope = new SoapSerializationEnvelope( SoapEnvelope.VER11 );
            SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME);

            HttpTransportSE transport = new HttpTransportSE(urlWebService, TIEMPO_ESPERA);

            if (method.equals(TablesHelper.Usuario.Sincronizar) || method.equals(TablesHelper.Vendedor.Sincronizar) || method.equals(TablesHelper.Empresa.Sincronizar))
                transport = new HttpTransportSE(urlWebService, TIEMPO_ESPERA/2);

            request.addProperty("idEmpresa", idEmpresa);
            request.addProperty("server", idServicio);
            soapEnvelope.dotNet = true;
            soapEnvelope.setOutputSoapObject(request);

            long beforeCall = System.currentTimeMillis();

            try {
                transport.call(SOAP_ACTION, soapEnvelope);
                Log.i( TAG, table + ": Respuesta en "  + (System.currentTimeMillis() - beforeCall) + "miliseg");

                SoapPrimitive result = (SoapPrimitive) soapEnvelope.getResponse();
                final JSONArray jsonstring = new JSONArray(result.toString());

                Log.i(TAG, table + ": " + jsonstring.length() + " registros");
                long startTime = System.nanoTime();
                //Buscamos el metodo en la clase DataBaseHelper para insertar el json retornado

                final Method helperMethod = helper.getClass().getMethod("actualizar"+table, JSONArray.class );
                Thread thread = new Thread() {
                    public void run() {
                        try {
                            helperMethod.invoke(helper,jsonstring);
                            Log.i(TAG, table + " SINCRONIZADO");
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        } catch (InvocationTargetException e) {
                            e.printStackTrace();
                        }
                    }
                };
                thread.start();
                long stopTime = System.nanoTime();
                long timeSaveAqlite = (stopTime - startTime)/100000;
                System.out.println("Finalizado guardado en " + table + "  " + String.valueOf(timeSaveAqlite) + " mseg");

            } catch (NoSuchMethodException e) {
                Log.e(TAG, "NO SINCRONIZADO : No existe el metodo actualizar"+ table +" en DataBaseHelper");
                e.printStackTrace();
                throw new NoSuchMethodException();//atrapa la excepcion pero la vuelve a lanzar (para que la la actividad la vuelva a detectar)
            } catch (SocketTimeoutException e) {
                //Error relacionado a la webservice
                Log.e(TAG, table + " SOCKETTIMEOUT EXCEPTION :" + e.getMessage());
                e.printStackTrace();
                throw new SocketTimeoutException();
            } catch (IOException e) {
                //Error relacionado a la webservice
                Log.e(TAG, table + " IO EXCEPTION:" + e.getMessage());
                //ex.printStackTrace();
                throw new IOException(e);
            } catch (JSONException e) {
                //Error relacionado a la webservice
                Log.e(TAG, table + " JSON EXCEPTION:" + e.getMessage());
                //ex.printStackTrace();
                throw new RuntimeException(e);
            } catch (Exception e) {
                //Error relacionado a la webservice
                if (e.getCause() != null) {
                    Log.e(TAG, table + " GENERAL EXCEPTION CAUSE:" + e.getCause().getMessage());
                    throw new Exception(e.getCause());
                }else {
                    Log.e(TAG, table + " GENERAL EXCEPTION:" + e.getMessage());
                    throw new Exception(e);
                }
            }

        }else
            Log.e(TAG, "No se encontro el metodo Soap para  sincronizar la tabla "+table+" en Util.getMetodoSoap");
    }

}
