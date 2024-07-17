package com.expediodigital.ventas360.model;

/**
 * Created by Kevin Robinson Meza Hinostroza on julio 2017.
 * Expedio Digital
 * Meza.Hinostroza.Robin@gmail.com
 */

public class UnidadMedidaModel {
    private String idUnidadManejo;
    private String descripcion;
    private String contenido;
    private String idUnidadContable;
    private String descripcionContable;

    public String getIdUnidadManejo() {
        return idUnidadManejo;
    }

    public void setIdUnidadManejo(String idUnidadManejo) {
        this.idUnidadManejo = idUnidadManejo;
    }

    public String getIdUnidadContable() {
        return idUnidadContable;
    }

    public void setIdUnidadContable(String idUnidadContable) {
        this.idUnidadContable = idUnidadContable;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getContenido() {
        return contenido;
    }

    public void setContenido(String contenido) {
        this.contenido = contenido;
    }

    public String getDescripcionContable() {
        return descripcionContable;
    }

    public void setDescripcionContable(String descripcionContable) {
        this.descripcionContable = descripcionContable;
    }
}
