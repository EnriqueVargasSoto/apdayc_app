package com.expediodigital.ventas360.model;

/**
 * Created by Kevin Robinson Meza Hinostroza on julio 2017.
 * Expedio Digital
 * Meza.Hinostroza.Robin@gmail.com
 */

public class VendedorModel {
    public static final String TIPO_VENDEDOR = "V";
    public static final String TIPO_TRANSPORTISTA = "T";
    public static final String TIPO_PUNTO_VENTA = "P";
    public static final String TIPO_MERCADEO = "M";
    public static final String MODO_AUTOVENTA = "autoventa";
    public static final String MODO_PREVENTA = "preventa";
    public static final String MODO_PREVENTA_ENLINEA = "preventaEnLinea";
    public static final String ESTADO_OPERATIVO = "O";
    public static final String ESTADO_CERRADO = "C";
    public static final String ESTADO_DESPACHO = "D";

    private String idEmpresa;
    private String idSucursal;
    private String idVendedor;
    private String idusuario;
    private String nombre;
    private String tipo;
    private String serie;
    private String idRuta;
    private String idAlmacen;
    private String modoVenta;
    private String estado;

    public String getIdVendedor() {
        return idVendedor;
    }

    public void setIdVendedor(String idVendedor) {
        this.idVendedor = idVendedor;
    }

    public String getIdusuario() {
        return idusuario;
    }

    public void setIdusuario(String idusuario) {
        this.idusuario = idusuario;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getSerie() {
        return serie;
    }

    public void setSerie(String serie) {
        this.serie = serie;
    }

    public String getIdAlmacen() {
        return idAlmacen;
    }

    public void setIdAlmacen(String idAlmacen) {
        this.idAlmacen = idAlmacen;
    }

    public String getIdRuta() {
        return idRuta;
    }

    public void setIdRuta(String idRuta) {
        this.idRuta = idRuta;
    }

    public String getIdEmpresa() {
        return idEmpresa;
    }

    public void setIdEmpresa(String idEmpresa) {
        this.idEmpresa = idEmpresa;
    }

    public String getIdSucursal() {
        return idSucursal;
    }

    public void setIdSucursal(String idSucursal) {
        this.idSucursal = idSucursal;
    }

    public String getModoVenta() {
        return modoVenta;
    }

    public void setModoVenta(String modoVenta) {
        this.modoVenta = modoVenta;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
}
