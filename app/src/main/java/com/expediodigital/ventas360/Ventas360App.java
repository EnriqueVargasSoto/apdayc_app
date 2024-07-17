package com.expediodigital.ventas360;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import androidx.appcompat.app.AppCompatDelegate;

/**
 * Created by Kevin Robinson Meza Hinostroza on julio 2017.
 * Expedio Digital
 * Meza.Hinostroza.Robin@gmail.com
 */

public class Ventas360App extends Application {
    public static final String TAG = "ventas360App";
    //public static final String URL_WEBSERVICE_BASE = "http://104.154.193.68:1200/ventas360/service360.asmx";
    //public static final String URL_WEBSERVICE_BASE = "http://35.184.215.96:1200/ventas360/service360.asmx";
        //public static final String URL_WEBSERVICE_BASE = "http://170.239.100.137/Ventas360Test/service360.asmx";

    public static final String URL_WEBSERVICE_BASE = "http://apps.atiendo.pe/apdaycService/Service360.asmx";//"http://66.70.227.161:1200/ventas360Test/Service360.asmx";//"http://142.44.251.174/ventas360Test/Service360.asmx";//"http://66.70.227.161:1200/ventas360Test/Service360.asmx";// "http://149.56.94.200:1200/ventas360/service360.asmx";

    final String PREFERENCIAS_RUC_EMPRESA = "rucEmpresa";
    final String PREFERENCIAS_USUARIO = "usuario";
    final String PREFERENCIAS_PASSWORD = "password";
    final String PREFERENCIAS_ID_VENDEDOR = "idVendedor";
    final String PREFERENCIAS_NOMBRE_VENDEDOR = "nombreVendedor";
    final String PREFERENCIAS_SERIE_VENDEDOR = "serieVendedor";
    final String PREFERENCIAS_ID_ALMACEN = "idAlmacen";
    final String PREFERENCIAS_NUMERO_GUIA = "numeroGuia";
    final String PREFERENCIAS_TIPO_VENDEDOR = "tipoVendedor";
    final String PREFERENCIAS_MODO_VENTA = "modoVenta";
    final String PREFERENCIAS_SOLO_PRODUCTOS_DISPONIBLES = "soloProductosDisponibles";
    final String PREFERENCIAS_ID_EMPRESA = "idEmpresa";
    final String PREFERENCIAS_ID_SUCURSAL = "idSucursal";
    final String PREFERENCIAS_INDEX_RUTA_MAPA = "indexRutaMapa";
    final String PREFERENCIAS_MARCAR_PEDIDOS_ENTREGADOS = "marcarPedidosEntregados";
    final String PREFERENCIAS_DEVICE_ADDRESS = "deviceAddress";
    final String PREFERENCIAS_SESION_ACTIVA = "sesionActiva";

    final String PREFERENCIAS_DATABASE_ID_SERVIDOR = "idServicio";
    final String PREFERENCIAS_DATABASE_URL_WEBSERVICE = "urlWebService";

    /* Lista de Preferencias que podrán ser cambiadas desde Configuración */
    final String SETTINGS_VALIDAR_STOCK = "settings_validarStock";
    final String SETTINGS_STOCK_EN_LINEA = "settings_stockEnLinea";
    final String SETTINGS_PRODUCTOS_SIN_PRECIO = "settings_productoSinPrecio";
    final String SETTINGS_PREVENTA_EN_LINEA = "settings_preventaEnLinea";
    final String SETTINGS_BONIFIACIONES = "settings_bonificaciones";
    /* ---------------------------------------------------------------------*/

    private String rucEmpresa;
    private String usuario;
    private String password;
    private String idVendedor;
    private String nombreVendedor;
    private String serieVendedor;
    private String tipoVendedor;
    private String modoVenta;
    private String idAlmacen;
    private String numeroGuia;
    private boolean soloProductosDisponibles;
    private int indexRutaMapa;
    private boolean marcarPedidosEntregados;
    private String deviceAddress;
    private String idEmpresa;
    private String idSucursal;
    private Boolean sesionActiva;

    private String idServicio;
    private String urlWebService;

    private boolean settings_validarStock;
    private boolean settings_stockEnLinea;
    private boolean settings_productoSinPrecio;
    private boolean settings_preventaEnLinea;
    private boolean settings_bonificaciones;

    SharedPreferences preferencias;
    SharedPreferences.Editor editor;

    /*IMPORTANTE para que se pueda cargar vectores en versiones anteriores*/
    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        preferencias = getSharedPreferences("PreferenciasApp", Context.MODE_PRIVATE);

        rucEmpresa = preferencias.getString(PREFERENCIAS_RUC_EMPRESA,"");
        usuario = preferencias.getString(PREFERENCIAS_USUARIO,"");
        password = preferencias.getString(PREFERENCIAS_PASSWORD,"");
        idVendedor = preferencias.getString(PREFERENCIAS_ID_VENDEDOR,"");
        nombreVendedor = preferencias.getString(PREFERENCIAS_NOMBRE_VENDEDOR,"");
        idEmpresa = preferencias.getString(PREFERENCIAS_ID_EMPRESA,"");
        idSucursal = preferencias.getString(PREFERENCIAS_ID_SUCURSAL,"");

        idServicio = preferencias.getString(PREFERENCIAS_DATABASE_ID_SERVIDOR,"");
        urlWebService = preferencias.getString(PREFERENCIAS_DATABASE_URL_WEBSERVICE,URL_WEBSERVICE_BASE);

        serieVendedor = preferencias.getString(PREFERENCIAS_SERIE_VENDEDOR,"");
        idAlmacen = preferencias.getString(PREFERENCIAS_ID_ALMACEN,"");
        numeroGuia = preferencias.getString(PREFERENCIAS_NUMERO_GUIA,"");
        tipoVendedor = preferencias.getString(PREFERENCIAS_TIPO_VENDEDOR,"");
        modoVenta = preferencias.getString(PREFERENCIAS_MODO_VENTA,"");
        soloProductosDisponibles = preferencias.getBoolean(PREFERENCIAS_SOLO_PRODUCTOS_DISPONIBLES,false);
        indexRutaMapa = preferencias.getInt(PREFERENCIAS_INDEX_RUTA_MAPA,-1);
        marcarPedidosEntregados = preferencias.getBoolean(PREFERENCIAS_MARCAR_PEDIDOS_ENTREGADOS,false);
        deviceAddress = preferencias.getString(PREFERENCIAS_DEVICE_ADDRESS,"");
        sesionActiva = preferencias.getBoolean(PREFERENCIAS_SESION_ACTIVA,false);

        settings_validarStock = preferencias.getBoolean(SETTINGS_VALIDAR_STOCK,true);
        settings_stockEnLinea = preferencias.getBoolean(SETTINGS_STOCK_EN_LINEA,true);
        settings_productoSinPrecio = preferencias.getBoolean(SETTINGS_PRODUCTOS_SIN_PRECIO,false);
        settings_preventaEnLinea = preferencias.getBoolean(SETTINGS_PREVENTA_EN_LINEA,false);
        settings_bonificaciones = preferencias.getBoolean(SETTINGS_BONIFIACIONES,false);
    }

    public String getIdVendedor() {
        return idVendedor;
    }

    public void setIdVendedor(String idVendedor) {
        this.idVendedor = idVendedor;
        saveData(PREFERENCIAS_ID_VENDEDOR,idVendedor);
    }

    public String getNombreVendedor() {
        return nombreVendedor;
    }

    public void setNombreVendedor(String nombreVendedor) {
        this.nombreVendedor = nombreVendedor;
        saveData(PREFERENCIAS_NOMBRE_VENDEDOR,nombreVendedor);
    }

    public String getIdServicio() {
        return idServicio;
    }

    public void setIdServicio(String idServicio) {
        this.idServicio = idServicio;
        saveData(PREFERENCIAS_DATABASE_ID_SERVIDOR,idServicio);
    }

    public String getUrlWebService() {
        return urlWebService;
    }

    public void setUrlWebService(String urlWebService) {
        this.urlWebService = urlWebService;
        saveData(PREFERENCIAS_DATABASE_URL_WEBSERVICE,urlWebService);
    }

    public String getSerieVendedor() {
        return serieVendedor;
    }

    public void setSerieVendedor(String serieVendedor) {
        this.serieVendedor = serieVendedor;
        saveData(PREFERENCIAS_SERIE_VENDEDOR,serieVendedor);
    }

    public String getIdAlmacen() {
        return idAlmacen;
    }

    public void setIdAlmacen(String idAlmacen) {
        this.idAlmacen = idAlmacen;
        saveData(PREFERENCIAS_ID_ALMACEN,idAlmacen);
    }

    public String getTipoVendedor() {
        return tipoVendedor;
    }

    public void setTipoVendedor(String tipoVendedor) {
        this.tipoVendedor = tipoVendedor;
        saveData(PREFERENCIAS_TIPO_VENDEDOR,tipoVendedor);
    }

    public boolean getSoloProductosDisponibles() {
        return soloProductosDisponibles;
    }

    public void setSoloProductosDisponibles(boolean soloProductosDisponibles) {
        this.soloProductosDisponibles = soloProductosDisponibles;
        saveData(PREFERENCIAS_SOLO_PRODUCTOS_DISPONIBLES,soloProductosDisponibles);
    }

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
        saveData(PREFERENCIAS_USUARIO,usuario);
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
        saveData(PREFERENCIAS_PASSWORD,password);
    }

    public String getRucEmpresa() {
        return rucEmpresa;
    }

    public void setRucEmpresa(String rucEmpresa) {
        this.rucEmpresa = rucEmpresa;
        saveData(PREFERENCIAS_RUC_EMPRESA,rucEmpresa);
    }

    public String getIdEmpresa() {
        return idEmpresa;
    }

    public void setIdEmpresa(String idEmpresa) {
        this.idEmpresa = idEmpresa;
        saveData(PREFERENCIAS_ID_EMPRESA,idEmpresa);
    }

    public String getIdSucursal() {
        return idSucursal;
    }

    public void setIdSucursal(String idSucursal) {
        this.idSucursal = idSucursal;
        saveData(PREFERENCIAS_ID_SUCURSAL,idSucursal);
    }

    public String getNumeroGuia() {
        return numeroGuia;
    }

    public void setNumeroGuia(String numeroGuia) {
        this.numeroGuia = numeroGuia;
        saveData(PREFERENCIAS_NUMERO_GUIA,numeroGuia);
    }
    public String getModoVenta() {
        return modoVenta;
    }

    public void setModoVenta(String modoVenta) {
        this.modoVenta = modoVenta;
        saveData(PREFERENCIAS_MODO_VENTA,modoVenta);
    }

    public int getIndexRutaMapa() {
        return indexRutaMapa;
    }

    public void setIndexRutaMapa(int indexRutaMapa) {
        this.indexRutaMapa = indexRutaMapa;
        saveData(PREFERENCIAS_INDEX_RUTA_MAPA,indexRutaMapa);
    }

    public boolean getMarcarPedidosEntregados() {
        return marcarPedidosEntregados;
    }

    public void setMarcarPedidosEntregados(boolean marcarPedidosEntregados) {
        this.marcarPedidosEntregados = marcarPedidosEntregados;
        saveData(PREFERENCIAS_MARCAR_PEDIDOS_ENTREGADOS,marcarPedidosEntregados);
    }

    public String getDeviceAddress() {
        return deviceAddress;
    }

    public void setDeviceAddress(String deviceAddress) {
        this.deviceAddress = deviceAddress;
        saveData(PREFERENCIAS_DEVICE_ADDRESS,deviceAddress);
    }

    public Boolean getSesionActiva() {
        return sesionActiva;
    }

    public void setSesionActiva(Boolean sesionActiva) {
        this.sesionActiva = sesionActiva;
        saveData(PREFERENCIAS_SESION_ACTIVA,sesionActiva);
    }

    /*------------------------------------------------------------------------------------------------*/
    public boolean getSettings_validarStock() {
        return settings_validarStock;
    }

    public void setSettings_validarStock(boolean settings_validarStock) {
        this.settings_validarStock = settings_validarStock;
        saveData(SETTINGS_VALIDAR_STOCK,settings_validarStock);
    }

    public boolean getSettings_stockEnLinea() {
        return settings_stockEnLinea;
    }

    public void setSettings_stockEnLinea(boolean settings_stockEnLinea) {
        this.settings_stockEnLinea = settings_stockEnLinea;
        saveData(SETTINGS_STOCK_EN_LINEA,settings_stockEnLinea);
    }

    public boolean getSettings_productoSinPrecio() {
        return settings_productoSinPrecio;
    }

    public void setSettings_productoSinPrecio(boolean settings_productoSinPrecio) {
        this.settings_productoSinPrecio = settings_productoSinPrecio;
        saveData(SETTINGS_PRODUCTOS_SIN_PRECIO,settings_productoSinPrecio);
    }

    public boolean getSettings_preventaEnLinea() {
        return settings_preventaEnLinea;
    }

    public void setSettings_preventaEnLinea(boolean settings_preventaEnLinea) {
        this.settings_preventaEnLinea = settings_preventaEnLinea;
        saveData(SETTINGS_PREVENTA_EN_LINEA,settings_preventaEnLinea);
    }

    public boolean getSettings_bonificaciones() {
        return settings_bonificaciones;
    }

    public void setSettings_bonificaciones(boolean settings_bonificaciones) {
        this.settings_bonificaciones = settings_bonificaciones;
        saveData(SETTINGS_BONIFIACIONES,settings_bonificaciones);
    }
    /*------------------------------------------------------------------------------------------------*/

    public void saveData(String KEY, String value){
        editor = preferencias.edit();
        editor.putString(KEY, value);
        editor.commit();
    }

    public void saveData(String KEY, int value){
        editor = preferencias.edit();
        editor.putInt(KEY, value);
        editor.commit();
    }

    public void saveData(String KEY, long value){
        editor = preferencias.edit();
        editor.putLong(KEY, value);
        editor.commit();
    }

    public void saveData(String KEY, boolean value){
        editor = preferencias.edit();
        editor.putBoolean(KEY, value);
        editor.commit();
    }



}
