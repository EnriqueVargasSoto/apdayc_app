package com.expediodigital.ventas360.model;

/**
 * Created by Kevin Robinson Meza Hinostroza on julio 2017.
 * Expedio Digital
 * Meza.Hinostroza.Robin@gmail.com
 */

public class PoliticaPrecioModel {
    private String idPoliticaPrecio;
    private String descripcion;
    private double precioManejo;
    private double precioContenido;
    private String unidad;
    private int cantidadMinima;
    private String idUnidadManejo;

    public String getIdPoliticaPrecio() {
        return idPoliticaPrecio;
    }

    public void setIdPoliticaPrecio(String idPoliticaPrecio) {
        this.idPoliticaPrecio = idPoliticaPrecio;
    }

    public String getDescripcion() {       return descripcion;    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public double getPrecioManejo() {
        return precioManejo;
    }

    public void setPrecioManejo(double precioManejo) {
        this.precioManejo = precioManejo;
    }

    public double getPrecioContenido() {
        return precioContenido;
    }

    public void setPrecioContenido(double precioContenido) {
        this.precioContenido = precioContenido;
    }

    public String getUnidad() {
        return unidad;
    }

    public void setUnidad(String unidad) {
        this.unidad = unidad;
    }

    public int getCantidadMinima() {
        return cantidadMinima;
    }

    public void setCantidadMinima(int cantidadMinima) {
        this.cantidadMinima = cantidadMinima;
    }

    public String getIdUnidadManejo() {
        return idUnidadManejo;
    }

    public void setIdUnidadManejo(String idUnidadManejo) {
        this.idUnidadManejo = idUnidadManejo;
    }
}
