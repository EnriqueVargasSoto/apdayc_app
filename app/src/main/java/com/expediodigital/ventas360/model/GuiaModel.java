package com.expediodigital.ventas360.model;

/**
 * Created by Monica Toribio Rojas on julio 2017.
 * Expedio Digital
 * monica.toribio.rojas@gmail.com
 */

public class GuiaModel {
    public static final String ESTADO_OPERANDO = "O";
    public static final String ESTADO_CERRADO = "C";

    private String numeroGuia;
    private String idAlmacen;
    private String fechaCarga;
    private String fechaCierre;
    private int productoDisponible;
    private String estado;

    public String getNumeroguia() {
        return numeroGuia;
    }

    public void setNumeroguia(String numeroGuia) {
        this.numeroGuia = numeroGuia;
    }

    public String getFechaCarga() {
        return fechaCarga;
    }

    public void setFechaCarga(String fechaCarga) {
        this.fechaCarga = fechaCarga;
    }

    public String getFechaCierre() {
        return fechaCierre;
    }

    public void setFechaCierre(String fechaCierre) {
        this.fechaCierre = fechaCierre;
    }

    public int getProductoDisponible() {
        return productoDisponible;
    }

    public void setProductoDisponible(int productoDisponible) {
        this.productoDisponible = productoDisponible;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getIdAlmacen() {
        return idAlmacen;
    }

    public void setIdAlmacen(String idAlmacen) {
        this.idAlmacen = idAlmacen;
    }
}
