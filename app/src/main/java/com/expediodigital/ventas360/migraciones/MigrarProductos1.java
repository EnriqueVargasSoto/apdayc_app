package com.expediodigital.ventas360.migraciones;

import android.app.ProgressDialog;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import androidx.appcompat.app.AlertDialog;
import android.util.Log;

import com.expediodigital.ventas360.Ventas360App;
import com.expediodigital.ventas360.util.DataBaseHelper;
import com.expediodigital.ventas360.util.SoapManager;
import com.expediodigital.ventas360.util.TablesHelper;
import com.expediodigital.ventas360.util.Util;
import com.expediodigital.ventas360.view.ConfiguracionActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import java.io.IOException;
import java.net.SocketTimeoutException;

public class MigrarProductos1 {

    Context mContexto;
    SoapManager soap_manager;
    DataBaseHelper mDataBaseHelper;

    public MigrarProductos1(DataBaseHelper dataBaseHelper, Context contexto)
    {

        mContexto = contexto;
        mDataBaseHelper = dataBaseHelper;
        soap_manager = new SoapManager(contexto);

    }

    public void moverTabla()
    {

        String rawQuery = "ALTER TABLE \"Producto\" RENAME TO \"ProductoOld\"";
        SQLiteDatabase db = mDataBaseHelper.getReadableDatabase();
        db.execSQL(rawQuery);

        String rawQuery1 = "CREATE TABLE IF NOT EXISTS \"Producto\" (\"idProducto\" TEXT NOT NULL, \"descripcion\" TEXT,\"idLinea\" TEXT, \"idFamilia\" TEXT, \"peso\" REAL, \"idProveedor\" TEXT, \"idProductoERP\" TEXT, \"descripcionERP\" TEXT, \"tipoProducto\" TEXT, \"idMarca\" TEXT, \"porcentajePercepcion\" real, \"porcentajeISC\" real, \"estadoDetalle\" INT, PRIMARY KEY(\"idProducto\") )";
        db.execSQL(rawQuery1);

        String rawQuery2 = "INSERT INTO Producto(idProducto, descripcion, idLinea, idFamilia, peso, idProveedor, idProductoERP, descripcionERP, tipoProducto, idMarca, porcentajePercepcion, porcentajeISC)\n" +
                "SELECT idProducto, descripcion, idLinea, idFamilia, peso, idProveedor, idProductoERP, descripcionERP, tipoProducto, idMarca, porcentajePercepcion, porcentajeISC\n" +
                "FROM ProductoOld";
        db.execSQL(rawQuery2);

    }

    public void dropPpp()
    {
        SQLiteDatabase db = mDataBaseHelper.getReadableDatabase();

        String rawQuery1 = "ALTER TABLE \"PoliticaPrecioxProducto\" RENAME TO \"PoliticaPrecioxProductoOld\"";
        db.execSQL(rawQuery1);

        String rawQuery2 = "CREATE TABLE IF NOT EXISTS \"PoliticaPrecioxProducto\" (\n" +
                "\t\"idEmpresa\"\tTEXT(15) NOT NULL,\n" +
                "\t\"idPolitica\"\tTEXT NOT NULL,\n" +
                "\t\"idProducto\"\tTEXT NOT NULL,\n" +
                "\t\"idUnidadManejo\"\tTEXT NOT NULL,\n" +
                "\t\"idUnidadContenido\"\tTEXT NOT NULL,\n" +
                "\t\"precioManejo\"\tREAL NOT NULL,\n" +
                "\t\"precioContenido\"\tREAL NOT NULL,\n" +
                "\tPRIMARY KEY(\"idEmpresa\",\"idPolitica\",\"idProducto\",\"idUnidadManejo\")\n" +
                ")";
        db.execSQL(rawQuery2);
    }

    public void dropPedidoDetalle()
    {
        SQLiteDatabase db = mDataBaseHelper.getReadableDatabase();

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
                TablesHelper.PedidoDetalle.SinStock + " INT, " +
                TablesHelper.PedidoDetalle.Percepcion + " REAL NOT NULL, " +
                TablesHelper.PedidoDetalle.ISC + " REAL NOT NULL, " +
                TablesHelper.PedidoDetalle.Malla + " TEXT(20), " +
                TablesHelper.PedidoDetalle.EstadoDetalle + " TEXT(2) ) ";
        db.execSQL(rawQuery0);
    }


    public void crearTablas()
    {

        SQLiteDatabase db = mDataBaseHelper.getReadableDatabase();

        String rawQuery0 = "CREATE TABLE IF NOT EXISTS \"UnidadMedidaxProducto\" (\n" +
                "\t\"idEmpresa\"\tTEXT(15) NOT NULL,\n" +
                "\t\"idProducto\" TEXT NOT NULL,\n" +
                "\t\"idUnidadManejo\" TEXT NOT NULL,\n" +
                "\t\"idUnidadContable\"\tTEXT NOT NULL,\n" +
                "\t\"contenido\"\tTEXT,\n" +
                "\tPRIMARY KEY(\"idEmpresa\",\"idProducto\",\"idUnidadManejo\")\n" +
                ")";
        db.execSQL(rawQuery0);

        String rawQuery2 = "CREATE TABLE IF NOT EXISTS \"PoliticaPrecioxProducto\" (\n" +
                "\t\"idEmpresa\"\tTEXT(15) NOT NULL,\n" +
                "\t\"idPolitica\"\tTEXT NOT NULL,\n" +
                "\t\"idProducto\"\tTEXT NOT NULL,\n" +
                "\t\"idUnidadManejo\"\tTEXT NOT NULL,\n" +
                "\t\"idUnidadContenido\"\tTEXT NOT NULL,\n" +
                "\t\"precioManejo\"\tREAL NOT NULL,\n" +
                "\t\"precioContenido\"\tREAL NOT NULL,\n" +
                "\tPRIMARY KEY(\"idEmpresa\",\"idPolitica\",\"idProducto\",\"idUnidadManejo\")\n" +
                ")";
        db.execSQL(rawQuery2);

    }

    public void downloadInfoTablas(Context contexto)
    {
        new asynDownloadNewProducts().execute("", "");
    }

    void guardarInformacion(JSONArray jsonstring)
    {
        try {
            mDataBaseHelper.actualizarProducto(jsonstring);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void verificarInformacionGuardada()
    {

        String table = TablesHelper.Producto.Table;


    }


    class asynDownloadNewProducts extends AsyncTask<String, String, String> {

        String mensajeSincronizacion = "Sincronizando....";
        ProgressDialog progressDialog;
        private static final String TAG = "SoapManager";
        private static final String NAMESPACE = "http://tempuri.org/";
        private final int TIEMPO_ESPERA = 1000 * 90;//60 Segundos

        protected void onPreExecute() {
            progressDialog = new ProgressDialog(mContexto);
            progressDialog.setTitle("");
            progressDialog.setMessage(mensajeSincronizacion);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            progressDialog.setIndeterminate(false);
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        protected String doInBackground(String... params) {
            try {
                if (Util.isConnectingToInternet()){

                    if (Util.isConnectingToInternet()) {
                        obtenerRegistrosxVendedorJSON(TablesHelper.Producto.Sincronizar, TablesHelper.Producto.Table);
                    }
                    else{
                        Log.e(ConfiguracionActivity.TAG,"NoConnectedToInternet");
                        return "NoConnectedToInternet";
                    }

                }
            } catch (NoSuchMethodException ex) {
                ex.printStackTrace();
                return "NoSuchMethodException";
            } catch (SocketTimeoutException  ex) {
                ex.printStackTrace();
                return "SocketTimeoutException";
            } catch (IOException ex) {
                ex.printStackTrace();
                return ex.getMessage();
            } catch (Exception e) {
                e.printStackTrace();
                return e.getMessage();
            }

            return "asyncSincronizacion Ok";
        }

        protected void onProgressUpdate(String... progress) {
            progressDialog.setMessage(mensajeSincronizacion);
            progressDialog.setProgress(Integer.parseInt(progress[0]));
        }

        protected void onPostExecute(String result) {
            progressDialog.dismiss();
            Log.d( ConfiguracionActivity.TAG, "onPostExecute "+ result);
            /*
            validarStockProductos();

            //Mostrar las tablas en su valor por defecto
            if (ORIGEN == ORIGEN_MENU){
                listaTablas = new ArrayList<>(Arrays.asList(0, 1, 2, 3));
                tablas = new String[]{"Clientes", "Vendedores", "Productos", "Configuración"};
            }else{
                tablas = new String[]{"Usuarios","Servicios"};
                listaTablas = new ArrayList<>(Arrays.asList(0, 1));
            }

            //Volver a cargar los servicios para ver los cambios
            cargarServicios();

            if (result.equals("asyncSincronizacion Ok")) {
                AlertDialog.Builder alerta = new AlertDialog.Builder(ConfiguracionActivity.this);
                alerta.setTitle("Sincronización correcta");
                alerta.setMessage("Se sincronizó correctamente");
                alerta.setIcon(R.drawable.ic_dialog_check);
                alerta.setCancelable(false);
                alerta.setPositiveButton("ACEPTAR", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (!usuarioValido)
                            verificarUsuario();
                        else
                            verificarGuias();
                    }
                });
                alerta.show();
            } else if(result.equals("NoConnectedToInternet")) {
                AlertDialog.Builder alerta = new AlertDialog.Builder(ConfiguracionActivity.this);
                alerta.setTitle("Sin conexión");
                alerta.setMessage("No se pudo acceder a Internet, compruebe su conexión e inténtelo nuevamente.");
                alerta.setIcon(R.drawable.ic_dialog_error);
                alerta.setCancelable(false);
                alerta.setPositiveButton("ACEPTAR", new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        verificarGuias();
                    }
                });
                alerta.show();
            }else if(result.equals("SocketTimeoutException")) {
                AlertDialog.Builder alerta = new AlertDialog.Builder(ConfiguracionActivity.this);
                alerta.setTitle("No se pudo sincronizar");
                alerta.setMessage("Se superó el tiempo de espera en la conexión, por favor intente sincronizar nuevamente");
                alerta.setIcon(R.drawable.ic_dialog_error);
                alerta.setCancelable(false);
                alerta.setPositiveButton("ACEPTAR", new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        verificarGuias();
                    }
                });
                alerta.show();
            } else {
                AlertDialog.Builder alerta = new AlertDialog.Builder(ConfiguracionActivity.this);
                alerta.setTitle("Sincronización incorrecta");
                alerta.setMessage("Algunas tablas no se sincronizaron correctamente. Sincronice nuevamente\n"+"\""+result+"\"");
                alerta.setIcon(R.drawable.ic_dialog_error);
                alerta.setCancelable(false);
                alerta.setPositiveButton("ACEPTAR", new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        verificarGuias();
                    }
                });
                alerta.show();
            }*/
        }


        public void obtenerRegistrosxVendedorJSON( String method, String table) throws Exception {

            Ventas360App ventas360App;
            String urlWebService;
            String idServicio;
            String vendedor;
            String idEmpresa;
            String idSucursal;

            ventas360App = (Ventas360App) mContexto.getApplicationContext();
            urlWebService   = ventas360App.getUrlWebService();//Obtiene la direccion del servicio web
            idServicio      = ventas360App.getIdServicio();//Obtiene el ID de los datos para la conexión con el BD
            vendedor        = "2302";
            idEmpresa       = "0010";
            idSucursal      = "01";

            String METHOD_NAME = method;

            if(METHOD_NAME != null && urlWebService != null){
                String SOAP_ACTION = NAMESPACE + METHOD_NAME;
                Log.d(ConfiguracionActivity.TAG,"idEmpresa:"+idEmpresa);
                Log.d(ConfiguracionActivity.TAG,"idSucursal:"+idSucursal);
                Log.d(ConfiguracionActivity.TAG,"idVendedor:"+vendedor);
                Log.d(ConfiguracionActivity.TAG,"server:"+idServicio);
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
                    Log.i( ConfiguracionActivity.TAG, table + ": Respuesta en "  + (System.currentTimeMillis() - beforeCall) + "miliseg");

                    SoapPrimitive result = (SoapPrimitive) soapEnvelope.getResponse();
                    JSONArray jsonstring = new JSONArray(result.toString());

                    Log.i(ConfiguracionActivity.TAG, table + ": " + jsonstring.length() + " registros");
                    Log.v(ConfiguracionActivity.TAG, jsonstring.toString());


                    //Buscamos el metodo en la clase DataBaseHelper para insertar el json retornado
                    if(table.equals("ObjPedido")){
                        Log.i(ConfiguracionActivity.TAG,result.toString());
                        guardarInformacion(jsonstring);
                    }else {
                        guardarInformacion(jsonstring);
                    }

                    Log.i(ConfiguracionActivity.TAG, table + " SINCRONIZADO");
                } /*catch (NoSuchMethodException e) {
                    Log.e(ConfiguracionActivity.TAG, "NO SINCRONIZADO : No existe el metodo actualizar"+ table +" en DataBaseHelper");
                    e.printStackTrace();
                    throw new NoSuchMethodException();//atrapa la excepcion pero la vuelve a lanzar (para que la la actividad la vuelva a detectar)
                }*/ catch (SocketTimeoutException e) {
                    //Error relacionado a la webservice
                    Log.e(ConfiguracionActivity.TAG, table + " SOCKETTIMEOUT EXCEPTION :" + e.getMessage());
                    e.printStackTrace();
                    throw new SocketTimeoutException();
                } catch (IOException e) {
                    //Error relacionado a la webservice
                    Log.e(ConfiguracionActivity.TAG, table + " IO EXCEPTION:" + e.getMessage());
                    //ex.printStackTrace();
                    throw new IOException(e);
                } catch (JSONException e) {
                    //Error relacionado a la webservice
                    Log.e(ConfiguracionActivity.TAG, table + " JSON EXCEPTION:" + e.getMessage());
                    //ex.printStackTrace();
                    throw new RuntimeException(e);
                } catch (Exception e) {
                    //Error relacionado a la webservice
                    if (e.getCause() != null) {
                        Log.e(ConfiguracionActivity.TAG, table + " GENERAL EXCEPTION CAUSE:" + e.getCause().getMessage());
                        throw new Exception(e.getCause());
                    }else {
                        Log.e(ConfiguracionActivity.TAG, table + " GENERAL EXCEPTION:" + e.getMessage());
                        throw new Exception(e);
                    }
                }

            }else
                Log.e(ConfiguracionActivity.TAG, "No se encontro el metodo Soap para  sincronizar la tabla "+table+" en Util.getMetodoSoap");
        }



    }



}
