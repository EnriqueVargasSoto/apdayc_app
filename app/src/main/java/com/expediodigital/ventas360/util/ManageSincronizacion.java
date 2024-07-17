package com.expediodigital.ventas360.util;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.AsyncTask;

import com.expediodigital.ventas360.view.MenuPrincipalActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.expediodigital.ventas360.DAO.DAOConfiguracion;
import com.expediodigital.ventas360.DTO.DTOServicio;
import com.expediodigital.ventas360.view.IntroActivity;
import com.expediodigital.ventas360.LoginActivity;
import com.expediodigital.ventas360.R;
import com.expediodigital.ventas360.Ventas360App;
import com.expediodigital.ventas360.migraciones.MigrarProductos1;
import com.expediodigital.ventas360.model.GuiaModel;
import com.expediodigital.ventas360.model.JSONModel;
import com.expediodigital.ventas360.model.VendedorModel;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Arrays;

public class ManageSincronizacion {

    public static final String TAG = "ManageSincronizacion";
    public static final int ORIGEN_LOGIN = 0;
    public static final int ORIGEN_MENU = 1;
    private int ORIGEN;
    Context context;
    ProgressBar progressBar;
    LinearLayout layPrgBar;
    SoapManager soap_manager;
    DAOConfiguracion daoConfiguracion;
    Ventas360App ventas360App;
    ArrayList<Integer> listaTablas = new ArrayList<>(Arrays.asList(0, 1, 2, 3, 4));
    String[] tablas = new String[]{"Clientes", "Ventas", "Productos", "Configuración", "Bonificaciones"};
    boolean usuarioValido = true;
    boolean accionUsuario = true;//Antes de cada setSelection del spinner indicar si es una accion del usuario para mostrar un mensaje

    ArrayList<String> arrayServidores = new ArrayList<>();
    private ArrayList<DTOServicio> listaServicios;
    String vendedor, idServicioActual;
    FloatingActionButton fabSincronizar;
    ProgressDialog mProgressDialog;
    ListenerSincronizacion mListener;

    public ManageSincronizacion(Context context, ProgressBar prgBar, int ORIGEN, FloatingActionButton fabSincronizar, LinearLayout layPrgBar, int tabla,
                                ProgressDialog progressDialog, ListenerSincronizacion listener) {
        this.context = context;
        this.progressBar = prgBar;
        this.ORIGEN = ORIGEN;
        soap_manager = new SoapManager(context);
        daoConfiguracion = new DAOConfiguracion(context);
        ventas360App = (Ventas360App) context.getApplicationContext();

        vendedor        = ventas360App.getIdVendedor();
        idServicioActual  = ventas360App.getIdServicio();

        this.mListener = listener;
        this.mProgressDialog = progressDialog;
        this.fabSincronizar = fabSincronizar;
        this.layPrgBar = layPrgBar;
        //para separar tipos de apk (desarrollo y produccion)
        idServicioActual = ventas360App.getIdServicio();
        cargarServicios();

        //valores por defecto
        ventas360App.setSettings_validarStock(false);
        ventas360App.setSettings_bonificaciones(true);
        ventas360App.setSettings_productoSinPrecio(false);


        if (ORIGEN == ORIGEN_MENU) {
            if (progressBar != null) {
                progressBar.setVisibility(View.VISIBLE);
            }
            listaTablas = new ArrayList<>();
            if(tabla==-1){
                listaTablas = new ArrayList<>(Arrays.asList(0, 1, 2, 3, 4));
            }else{
                listaTablas.add(tabla);
            }
            tablas = new String[]{"Clientes", "Ventas", "Productos", "Configuración", "Bonificaciones"};
        } else {
            tablas = new String[]{"Usuarios", "Servicios"};
            listaTablas = new ArrayList<>();
            if(tabla==-1){
                listaTablas = new ArrayList<>(Arrays.asList(0, 1));
            }else{
                listaTablas.add(tabla);
            }
        }

    }

    public void iniciarSincronizacion() {
        asyncSincronizacion as = new asyncSincronizacion();
        as.execute();
    }

    public void notificarDisponibleSincr() {
        fabSincronizar.setBackgroundTintList(ColorStateList.valueOf(Color.YELLOW));
        fabSincronizar.setEnabled(true);
        Toast.makeText(context, "La informacion a cambiado, sincronice por favor", Toast.LENGTH_SHORT).show();
    }

    class asyncSincronizacion extends AsyncTask<String, String, String> {
        String mensajeSincronizacion = "Sincronizando....";

        protected void onPreExecute() {
            if (progressBar != null) {
                progressBar.setProgress(0);
                progressBar.setVisibility(View.VISIBLE);
            }
            if (fabSincronizar != null) {
                fabSincronizar.setBackgroundTintList(ColorStateList.valueOf(Color.GRAY));
                fabSincronizar.setEnabled(false);
            }
            if (layPrgBar != null) {
                layPrgBar.setVisibility(View.VISIBLE);
            }
        }

        protected String doInBackground(String... params) {

            daoConfiguracion.testPedidoDetalle(context);
            try {
                if (Util.isConnectingToInternet()) {
                    if (ORIGEN == ORIGEN_MENU) {
                        //PRIMERO SINCRONIZAR CONFIGURACIONES ya que ahi está la guía, y si esta cambia se debe actualizar para que desde ya se obtenga todos los datos en base a esta nueva guia
                        /* Configuracion */
                        if (listaTablas.contains(3)) {
                            Log.v(TAG, "------------------------- Sincronizando Configuraciones -------------------------");
                            mensajeSincronizacion = "Sincronizando configuraciones...";
                            publishProgress("1");
                            soap_manager.obtenerRegistrosxSucursalJSON(TablesHelper.Servicio.Sincronizar, TablesHelper.Servicio.Table);
                            publishProgress("3");

                            soap_manager.obtenerRegistrosxVendedorJSON(TablesHelper.Configuracion.Sincronizar, TablesHelper.Configuracion.Table);
                            boolean isPreventaEnLinea = daoConfiguracion.isPreventaEnLinea();
                            ventas360App.setSettings_preventaEnLinea(isPreventaEnLinea);

                            publishProgress("5");
                            soap_manager.obtenerRegistrosJSON(TablesHelper.Empresa.Sincronizar, TablesHelper.Empresa.Table);
                            publishProgress("7");
                            soap_manager.obtenerRegistrosxEmpresaJSON(TablesHelper.MotivoNoVenta.Sincronizar, TablesHelper.MotivoNoVenta.Table);
                            publishProgress("8");
                            soap_manager.obtenerRegistrosxSucursalJSON(TablesHelper.FormaPago.Sincronizar, TablesHelper.FormaPago.Table);
                            if (ventas360App.getModoVenta().equals(VendedorModel.MODO_AUTOVENTA) || isPreventaEnLinea) {
                                publishProgress("9");
                                soap_manager.obtenerRegistrosxVendedorJSON(TablesHelper.Guia.Sincronizar, TablesHelper.Guia.Table);
                            }
                            publishProgress("10");
                        }
                        /* Clientes */
                        if (listaTablas.contains(0)) {
                            Log.v(TAG, "------------------------- Sincronizando Clientes -------------------------");
                            mensajeSincronizacion = "Sincronizando clientes...";
                            publishProgress("11");
                            soap_manager.obtenerRegistrosxVendedorJSON(TablesHelper.Cliente.Sincronizar, TablesHelper.Cliente.Table);
                            ventas360App.setIndexRutaMapa(JSONModel.SIN_RUTA_SELECCIONADA);
                            publishProgress("15");
                            soap_manager.obtenerRegistrosxEmpresaJSON(TablesHelper.Segmento.Sincronizar, TablesHelper.Segmento.Table);
                            publishProgress("18");
                            //soap_manager.obtenerRegistrosxEmpresaJSON(TablesHelper.SubGiro.Sincronizar, TablesHelper.SubGiro.Table);
                            publishProgress("20");
                            soap_manager.obtenerRegistrosxVendedorJSON(TablesHelper.Encuesta.Sincronizar, TablesHelper.Encuesta.Table);
                            publishProgress("22");
                            soap_manager.obtenerRegistrosxVendedorJSON(TablesHelper.EncuestaDetalle.Sincronizar, TablesHelper.EncuestaDetalle.Table);
                            publishProgress("24");
                            soap_manager.obtenerRegistrosxVendedorJSON(TablesHelper.EncuestaDetallePregunta.Sincronizar, TablesHelper.EncuestaDetallePregunta.Table);
                            publishProgress("26");
                            soap_manager.obtenerRegistrosxVendedorJSON(TablesHelper.EncuestaAlternativaPregunta.Sincronizar, TablesHelper.EncuestaAlternativaPregunta.Table);
                            publishProgress("27");
                            soap_manager.obtenerRegistrosxVendedorJSON(TablesHelper.EncuestaDetallexCliente.Sincronizar, TablesHelper.EncuestaDetallexCliente.Table);
                            publishProgress("28");
                            soap_manager.obtenerRegistrosxVendedorJSON(TablesHelper.EncuestaDetallexSegmento.Sincronizar, TablesHelper.EncuestaDetallexSegmento.Table);
                            publishProgress("29");
                            soap_manager.obtenerRegistrosxEmpresaJSON(TablesHelper.EncuestaTipo.Sincronizar, TablesHelper.EncuestaTipo.Table);
                            publishProgress("32");//Iniciar siempre sincronizando EncuestaRespuestaDetalle para que se pueda eliminar correctamente
                            soap_manager.obtenerRegistrosxVendedorJSON(TablesHelper.EncuestaRespuestaDetalle.Sincronizar, TablesHelper.EncuestaRespuestaDetalle.Table);
                            publishProgress("35");
                            soap_manager.obtenerRegistrosxVendedorJSON(TablesHelper.EncuestaRespuestaCabecera.Sincronizar, TablesHelper.EncuestaRespuestaCabecera.Table);
                            publishProgress("37");
                            soap_manager.obtenerRegistrosxVendedorJSON(TablesHelper.ClienteCoordenadas.Sincronizar, TablesHelper.ClienteCoordenadas.Table);
                            publishProgress("38");
                            soap_manager.obtenerWhathsappsJSON(TablesHelper.ClienteWathsapp.Sincronizar, TablesHelper.ClienteWathsapp.Table);
                            publishProgress("39");
                            soap_manager.obtenerClientesBajaJSON(TablesHelper.ClienteBaja.Sincronizar, TablesHelper.ClienteBaja.Table);
                            publishProgress("40");
                            soap_manager.obtenerMotivosBajaJSON(TablesHelper.MotivoBaja.Sincronizar, TablesHelper.MotivoBaja.Table);
                            publishProgress("41");
                            daoConfiguracion.testPedidoDetalle(context);
                        }
                        /* Vendedores */
                        if (listaTablas.contains(1)) {
                            Log.v(TAG, "------------------------- Sincronizando Ventas -------------------------");
                            mensajeSincronizacion = "Sincronizando ventas...";
                            publishProgress("43");
                            soap_manager.obtenerRegistrosJSON(TablesHelper.Usuario.Sincronizar, TablesHelper.Usuario.Table);
                            publishProgress("45");
                            soap_manager.obtenerRegistrosJSON(TablesHelper.Vendedor.Sincronizar, TablesHelper.Vendedor.Table);
                            ResetearDatosVendedor();//Resetear los datos del vendedor en la clase Application
                            publishProgress("47");

                            daoConfiguracion.testPedidoDetalle(context);
                            if (!soap_manager.checkColumnasTablaPedidoDetalle()) {
                                iniciarMigracionPedidoDetalle();
                            }
                            daoConfiguracion.testPedidoDetalle(context);
                            soap_manager.obtenerRegistrosxVendedorJSON(TablesHelper.ObjPedido.Sincronizar, TablesHelper.ObjPedido.Table);
                            daoConfiguracion.testPedidoDetalle(context);
                            publishProgress("49");
                            if (ventas360App.getModoVenta().equals(VendedorModel.MODO_AUTOVENTA) || ventas360App.getSettings_preventaEnLinea() || ventas360App.getTipoVendedor().equals(VendedorModel.TIPO_PUNTO_VENTA)) {
                                soap_manager.obtenerRegistrosxSucursalJSON(TablesHelper.PromocionDetalle.Sincronizar, TablesHelper.PromocionDetalle.Table);
                                publishProgress("51");
                                soap_manager.obtenerRegistrosxSucursalJSON(TablesHelper.PromocionxCliente.Sincronizar, TablesHelper.PromocionxCliente.Table);
                                publishProgress("54");
                                soap_manager.obtenerRegistrosxSucursalJSON(TablesHelper.PromocionxPoliticaPrecio.Sincronizar, TablesHelper.PromocionxPoliticaPrecio.Table);
                                publishProgress("56");
                                soap_manager.obtenerRegistrosxSucursalJSON(TablesHelper.PromocionxVendedor.Sincronizar, TablesHelper.PromocionxVendedor.Table);
                            }
                            publishProgress("58");
                            soap_manager.obtenerRegistrosxVendedorJSON(TablesHelper.ObjDevolucion.Sincronizar, TablesHelper.ObjDevolucion.Table);
                            publishProgress("60");
                            soap_manager.obtenerRegistrosxVendedorJSON(TablesHelper.AvanceCuota.Sincronizar, TablesHelper.AvanceCuota.Table);
                            publishProgress("63");
                            soap_manager.obtenerRegistrosxVendedorJSON(TablesHelper.HojaRutaIndicador.Sincronizar, TablesHelper.HojaRutaIndicador.Table);
                            publishProgress("65");
                            soap_manager.obtenerRegistrosxVendedorJSON(TablesHelper.HojaRutaMarcas.Sincronizar, TablesHelper.HojaRutaMarcas.Table);
                            publishProgress("67");
                            soap_manager.obtenerRutasJSON(TablesHelper.ModuloxRuta.Sincronizar, TablesHelper.ModuloxRuta.Table);

                            soap_manager.obtenerRegistrosxVendedorJSON(TablesHelper.HRCliente.Sincronizar, TablesHelper.HRCliente.table);
                            publishProgress("69");
                            soap_manager.obtenerRegistrosxVendedorJSON(TablesHelper.HRVendedor.Sincronizar, TablesHelper.HRVendedor.table);
                            publishProgress("72");
                            soap_manager.obtenerRegistrosxVendedorJSON(TablesHelper.HRMarcaResumen.Sincronizar, TablesHelper.HRMarcaResumen.table);
                            publishProgress("74");
                            daoConfiguracion.testPedidoDetalle(context);
                        }
                        /* Producto */
                        if (listaTablas.contains(2)) {
                            Log.v(TAG, "------------------------- Sincronizando Productos -------------------------");
                            mensajeSincronizacion = "Sincronizando productos...";
                            publishProgress("76");

                            //caso de que la columnas anteriores aun existen
                            if (soap_manager.checkColumnasTablaProductos()) {
                                iniciarMigracion();
                            }

                            soap_manager.obtenerRegistrosxVendedorJSON(TablesHelper.Producto.Sincronizar, TablesHelper.Producto.Table);
                            if (ventas360App.getModoVenta().equals(VendedorModel.MODO_AUTOVENTA) || ventas360App.getSettings_preventaEnLinea()) {
                                publishProgress("78");
                                soap_manager.obtenerRegistrosxVendedorJSON(TablesHelper.Kardex.Sincronizar, TablesHelper.Kardex.Table);
                            }
                            if (ventas360App.getModoVenta().equals(VendedorModel.MODO_AUTOVENTA)) {
                                publishProgress("81");
                                soap_manager.obtenerRegistrosxVendedorJSON(TablesHelper.Liquidacion.Sincronizar, TablesHelper.Liquidacion.Table);
                            }
                            publishProgress("82");
                            soap_manager.obtenerRegistrosxEmpresaJSON(TablesHelper.Proveedor.Sincronizar, TablesHelper.Proveedor.Table);
                            publishProgress("84");
                            soap_manager.obtenerRegistrosxEmpresaJSON(TablesHelper.Linea.Sincronizar, TablesHelper.Linea.Table);
                            publishProgress("87");
                            soap_manager.obtenerRegistrosxEmpresaJSON(TablesHelper.Familia.Sincronizar, TablesHelper.Familia.Table);
                            publishProgress("88");
                            soap_manager.obtenerRegistrosxEmpresaJSON(TablesHelper.UnidadMedida.Sincronizar, TablesHelper.UnidadMedida.Table);
                            publishProgress("89");
                            soap_manager.obtenerRegistrosxSucursalJSON(TablesHelper.PoliticaPrecio.Sincronizar, TablesHelper.PoliticaPrecio.Table);
                            publishProgress("91");
                            if (!soap_manager.checkColumnasPoliticaPrecioxProducto()) {
                                iniciarMigracionPpp();
                            }
                            soap_manager.obtenerRegistrosxVendedorJSON(TablesHelper.PoliticaPrecioxProducto.Sincronizar2, TablesHelper.PoliticaPrecioxProducto.Table);
                            publishProgress("94");
                            soap_manager.obtenerRegistrosxVendedorJSON(TablesHelper.UnidadMedidaxProducto.Sincronizar2, TablesHelper.UnidadMedidaxProducto.Table);
                            publishProgress("96");
                            soap_manager.obtenerRegistrosxVendedorJSON(TablesHelper.PoliticaPrecioxCliente.Sincronizar, TablesHelper.PoliticaPrecioxCliente.Table);
                            publishProgress("98");
                            soap_manager.obtenerRegistrosxEmpresaJSON(TablesHelper.Marca.Sincronizar, TablesHelper.Marca.Table);
                            publishProgress("100");
                            daoConfiguracion.testPedidoDetalle(context);
                        }
                        /* Bonificaciones */
//                        if (listaTablas.contains(4)) {
//                            Log.v(TAG, "------------------------- Sincronizando de Bonificaciones -------------------------");
//                            mensajeSincronizacion = "Sincronizando bonificaciones...";
//                            publishProgress("69");
//                            soap_manager.obtenerBonificaionesJSON(TablesHelper.MGRUP1F.Sincronizar, TablesHelper.MGRUP1F.Table);
//                            publishProgress("71");
//                            soap_manager.obtenerBonificaionesJSON(TablesHelper.MGRUP2F.Sincronizar, TablesHelper.MGRUP2F.Table);
//                            publishProgress("73");
//                            soap_manager.obtenerBonificaionesJSON(TablesHelper.MPROMO1F.Sincronizar, TablesHelper.MPROMO1F.Table);
//                            publishProgress("75");
//                            soap_manager.obtenerBonificaionesJSON(TablesHelper.MPROMO2F.Sincronizar, TablesHelper.MPROMO2F.Table);
//                            publishProgress("77");
//                            soap_manager.obtenerBonificaionesJSON(TablesHelper.MPROMO3F.Sincronizar, TablesHelper.MPROMO3F.Table);
//                            publishProgress("79");
//                            soap_manager.obtenerBonificaionesJSON(TablesHelper.MPROMO4F.Sincronizar, TablesHelper.MPROMO4F.Table);
//                            publishProgress("81");
//                            soap_manager.obtenerBonificaionesJSON(TablesHelper.MPROMO5F.Sincronizar, TablesHelper.MPROMO5F.Table);
//                            publishProgress("82");
//                            soap_manager.obtenerBonificaionesJSON(TablesHelper.MPROMO6F.Sincronizar, TablesHelper.MPROMO6F.Table);
//                            publishProgress("84");
//                        }
                        soap_manager.actualizarFechaSincronizacion(TablesHelper.Vendedor.ActualizarFechaSincronizacion);
                        daoConfiguracion.testPedidoDetalle(context);
                    } else {
                        /* Usuarios vendedores */
                        if (listaTablas.contains(0)) {
                            Log.v(TAG, "------------------------- Sincronizando Vendedor Usuario -------------------------");
                            mensajeSincronizacion = "Sincronizando usuarios...";
                            publishProgress("1");
                            soap_manager.obtenerRegistrosJSON(TablesHelper.Usuario.Sincronizar, TablesHelper.Usuario.Table);
                            publishProgress("15");
                            soap_manager.obtenerRegistrosJSON(TablesHelper.Vendedor.Sincronizar, TablesHelper.Vendedor.Table);
                            publishProgress("30");
                        }
                        /* Configuracion */
                        if (listaTablas.contains(1)) {
                            Log.v(TAG, "------------------------- Sincronizando Configuraciones y Servicios -------------------------");
                            mensajeSincronizacion = "Sincronizando configuraciones...";
                            publishProgress("40");
                            soap_manager.obtenerRegistrosJSON(TablesHelper.Empresa.Sincronizar, TablesHelper.Empresa.Table);
                            publishProgress("55");
                            soap_manager.obtenerRegistrosxSucursalJSON(TablesHelper.Servicio.Sincronizar, TablesHelper.Servicio.Table);
                            publishProgress("70");

                            soap_manager.obtenerRegistrosxVendedorJSON(TablesHelper.Configuracion.Sincronizar, TablesHelper.Configuracion.Table);
                            boolean isPreventaEnLinea = daoConfiguracion.isPreventaEnLinea();
                            ventas360App.setSettings_preventaEnLinea(isPreventaEnLinea);

                            if (ventas360App.getModoVenta().equals(VendedorModel.MODO_AUTOVENTA) || isPreventaEnLinea) {
                                publishProgress("85");
                                soap_manager.obtenerRegistrosxVendedorJSON(TablesHelper.Guia.Sincronizar, TablesHelper.Guia.Table);
                            }
                            publishProgress("100");
                        }
                    }
                } else {
                    Log.e(TAG, "NoConnectedToInternet");
                    return "NoConnectedToInternet";
                }
            } catch (NoSuchMethodException ex) {
                ex.printStackTrace();
                return "NoSuchMethodException";
            } catch (SocketTimeoutException ex) {
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
            //progressDialog.setMessage(mensajeSincronizacion);
            //progressDialog.setProgress(Integer.parseInt(progress[0]));
            if (progressBar != null) {
                progressBar.setProgress(Integer.parseInt(progress[0]));
            }
        }

        protected void onPostExecute(String result) {

            if(mListener != null){
                mListener.updateData();
            }

            if (mProgressDialog != null && mProgressDialog.isShowing()) {
                mProgressDialog.dismiss();

                verificarGuias();

                Intent intent = new Intent(context.getApplicationContext(), MenuPrincipalActivity.class);
                context.startActivity(intent);
                return;
            }

            if (fabSincronizar != null) {
                fabSincronizar.setEnabled(true);
                fabSincronizar.setBackgroundTintList(ColorStateList.valueOf(context.getResources().getColor(R.color.blue_200)));
            }

            if (progressBar != null) {
                progressBar.setVisibility(View.GONE);
            }
            if (layPrgBar != null) {
                layPrgBar.setVisibility(View.GONE);
            }
            Log.d(TAG, "onPostExecute " + result);
            validarStockProductos();


            if (result.equals("asyncSincronizacion Ok")) {


                //Mostrar las tablas en su valor por defecto
                if (ORIGEN == ORIGEN_MENU) {
                    if (progressBar != null) {
                        progressBar.setVisibility(View.GONE);

                    }
                } else {
                    if(listaTablas.contains(1)) {
                        Intent intent = new Intent(context, LoginActivity.class);
                        context.startActivity(intent);
                        ((IntroActivity) context).finish();
                    }
                    else {

                        AlertDialog.Builder alerta = new AlertDialog.Builder(context);
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
                    }
                }
            } else if (result.equals("NoConnectedToInternet")) {
                AlertDialog.Builder alerta = new AlertDialog.Builder(context);
                alerta.setTitle("Sin conexión");
                alerta.setMessage("No se pudo acceder a Internet, compruebe su conexión e inténtelo nuevamente.");
                alerta.setIcon(R.drawable.ic_dialog_error);
                alerta.setCancelable(false);
                alerta.setPositiveButton("ACEPTAR", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        verificarGuias();
                    }
                });
                alerta.show();
            } else if (result.equals("SocketTimeoutException")) {
                AlertDialog.Builder alerta = new AlertDialog.Builder(context);
                alerta.setTitle("No se pudo sincronizar");
                alerta.setMessage("Se superó el tiempo de espera en la conexión, por favor intente sincronizar nuevamente");
                alerta.setIcon(R.drawable.ic_dialog_error);
                alerta.setCancelable(false);
                alerta.setPositiveButton("ACEPTAR", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        verificarGuias();
                    }
                });
                alerta.show();
            } else {
                AlertDialog.Builder alerta = new AlertDialog.Builder(context);
                alerta.setTitle("Sincronización incorrecta");
                alerta.setMessage("Algunas tablas no se sincronizaron correctamente. Sincronice nuevamente\n" + "\"" + result + "\"");
                alerta.setIcon(R.drawable.ic_dialog_error);
                alerta.setCancelable(false);
                alerta.setPositiveButton("ACEPTAR", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        verificarGuias();
                    }
                });
                alerta.show();
            }
        }
    }

    public void verificarGuias() {
        if (ventas360App.getModoVenta().equals(VendedorModel.MODO_PREVENTA) && !ventas360App.getSettings_preventaEnLinea()) {

            ventas360App.setNumeroGuia(VendedorModel.MODO_PREVENTA);

        } else {
            if (ORIGEN == ORIGEN_MENU) {
                ArrayList<GuiaModel> listaGuias = daoConfiguracion.getGuiasOperativas();
                String mensaje = "";
                int icon = 0;

                if (listaGuias.size() == 1) {
                    ventas360App.setNumeroGuia(listaGuias.get(0).getNumeroguia());
                } else {
                    if (listaGuias.isEmpty()) {
                        if (ventas360App.getSettings_preventaEnLinea())
                            mensaje = "No hay guías de preventa en línea disponibles, comuníquese con el administrador";
                        else
                            mensaje = "No hay guías disponibles, comuníquese con el administrador";

                        icon = R.drawable.ic_dialog_error;
                        ventas360App.setNumeroGuia("");
                    } else {
                        mensaje = "Se encontró mas de una Guia abierta al mismo tiempo, este caso no debe ocurrir y generará problemas.\nComuníquese con el administrador inmediatamente";
                        icon = R.drawable.ic_dialog_alert;
                        ventas360App.setNumeroGuia(listaGuias.get(0).getNumeroguia());//Establecemos la primera guia como la actual
                    }

                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setIcon(icon);
                    builder.setTitle("Importante");
                    builder.setMessage(mensaje);
                    builder.setCancelable(false);
                    builder.setPositiveButton("ACEPTAR", null);
                    builder.show();
                }
            }
        }
    }

    public void cargarServicios() {
        arrayServidores = new ArrayList<>();
        listaServicios = daoConfiguracion.getServicios();

        int itemASeleccionar = 0;

        if (listaServicios.isEmpty()) {
            arrayServidores.add("No hay servicios disponibles");
        } else {
            for (int i = 0; i < listaServicios.size(); i++) {
                String descripcion = listaServicios.get(i).getUrl() + "";

                String tipoServicio = listaServicios.get(i).getTipo();

                try {
                    String url = listaServicios.get(i).getUrl();
                    String[] arrayIP = url.substring(7).split("\\.");
                    if (tipoServicio.equals(DTOServicio.TIPO_PRODUCCION)) {
                        descripcion = "[Producción] " + arrayIP[0] + "..............." + arrayIP[3].substring(0, 3) + "...";
                    } else if (tipoServicio.equals(DTOServicio.TIPO_DESARROLLO)) {
                        descripcion = "[Desarrollo] " + arrayIP[0] + "..............." + arrayIP[3].substring(0, 3) + "...";
                    } else {
                        descripcion = "[Servicio] " + arrayIP[0] + "..............." + arrayIP[3].substring(0, 3) + "...";
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                arrayServidores.add(descripcion);

                //Buscar el idServicio previamente guardado para seleccionarlo despues
                if (!idServicioActual.isEmpty()) {
                    if (listaServicios.get(i).getIdServicio().equals(idServicioActual)) {
                        itemASeleccionar = i;
                    }
                }
            }
        }

        //ArrayAdapter<String> adapterServicios = new ArrayAdapter<String>(context,R.layout.spinner_adapter,arrayServidores);
        //spinnerServidores.setAdapter(adapterServicios);
//        setIdServicio(0); //servidor de produccion
        setIdServicio(1); //servidor de desarrollo
//        setIdServicio(5); //servidor local
    }

    private void setIdServicio(int itemASeleccionar) {

        if (listaServicios.isEmpty()) {
            idServicioActual = "";
            ventas360App.setIdServicio("");
            ventas360App.setUrlWebService("");
        } else {
            accionUsuario = false;//Indicamos que setSelection no será una accion del usuario para evitar el dialogo de confirmación
            //spinnerServidores.setSelection(itemASeleccionar);
            idServicioActual = listaServicios.get(itemASeleccionar).getIdServicio();
            ventas360App.setIdServicio(listaServicios.get(itemASeleccionar).getIdServicio());
            ventas360App.setUrlWebService(listaServicios.get(itemASeleccionar).getUrl());
            soap_manager = new SoapManager(context);//Recargamos SoapManager para que se actualicen los datos de la clase application
            Log.w(TAG, "Estableciendo idServicioActual:" + idServicioActual + " Como servicio inicial");
        }
    }

    private void validarStockProductos() {
        //Validacion por modo de venta
        if (ventas360App.getModoVenta().equals(VendedorModel.MODO_PREVENTA) && !ventas360App.getSettings_preventaEnLinea()) {
            //switch_validarStock.setChecked(false);//No se valida stock por defecto
            ventas360App.setSettings_validarStock(false);
        } else if (ventas360App.getTipoVendedor().equals(VendedorModel.TIPO_MERCADEO)) {
            //switch_validarStock.setChecked(false);//Los mercaderistas no validan stock
            ventas360App.setSettings_validarStock(false);
        } else {//Si es autoventa no se puede cambiar nada
            //switch_validarStock.setChecked(true);//Se valida stock por defecto
            ventas360App.setSettings_validarStock(true);
        }
    }

    private void ResetearDatosVendedor() {
        String user = ventas360App.getUsuario();
        String password = ventas360App.getPassword();
        String ruc = ventas360App.getRucEmpresa();

        VendedorModel vendedorModel = daoConfiguracion.getVendedorUsuario(user, password, ruc);

        if (vendedorModel != null) {
            usuarioValido = true;
            //Si se ingresa con otro vendedor, se debe limpiar la base de datos
            if (!(ventas360App.getIdVendedor().equals(vendedorModel.getIdVendedor()) && ventas360App.getIdSucursal().equals(vendedorModel.getIdSucursal()) && ventas360App.getIdEmpresa().equals(vendedorModel.getIdEmpresa()))) {
                daoConfiguracion.limpiarTablas();
            }

            ventas360App.setRucEmpresa(ruc);
            ventas360App.setUsuario(user);
            ventas360App.setIdEmpresa(vendedorModel.getIdEmpresa());
            ventas360App.setIdSucursal(vendedorModel.getIdSucursal());

            ventas360App.setIdVendedor(vendedorModel.getIdVendedor());
            ventas360App.setNombreVendedor(vendedorModel.getNombre());
            ventas360App.setSerieVendedor(vendedorModel.getSerie());
            ventas360App.setTipoVendedor(vendedorModel.getTipo());
            ventas360App.setIdAlmacen(vendedorModel.getIdAlmacen());
            ventas360App.setModoVenta(vendedorModel.getModoVenta());
            ventas360App.setMarcarPedidosEntregados(false);
        } else {
            usuarioValido = false;
        }
    }

    public void iniciarMigracion() {
        DataBaseHelper helper = DataBaseHelper.getInstance(context);
        MigrarProductos1 migrarProductos1 = new MigrarProductos1(helper, context);
        migrarProductos1.moverTabla();
        migrarProductos1.crearTablas();
        //migrarProductos1.downloadInfoTablas(this);
        //migrarProductos1.verificarInformacionGuardada();
    }

    public void iniciarMigracionPedidoDetalle() {
        DataBaseHelper helper = DataBaseHelper.getInstance(context);
        MigrarProductos1 migrarProductos1 = new MigrarProductos1(helper, context);
        migrarProductos1.dropPedidoDetalle();
    }

    public void iniciarMigracionPpp() {
        DataBaseHelper helper = DataBaseHelper.getInstance(context);
        MigrarProductos1 migrarProductos1 = new MigrarProductos1(helper, context);
        migrarProductos1.dropPpp();
    }

    public void verificarUsuario() {
        if (!usuarioValido) {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setIcon(R.drawable.ic_dialog_error);
            builder.setTitle("Usuario no válido");
            builder.setMessage("El usuario con el que ha iniciado sesión no es válido. Por favor, salga de la aplicación y vuelva a iniciar sesión");
            builder.setCancelable(false);
            builder.setPositiveButton("ACEPTAR", null);
            builder.show();
        }
    }


}
