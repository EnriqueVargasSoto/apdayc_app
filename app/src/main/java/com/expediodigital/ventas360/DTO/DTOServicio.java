package com.expediodigital.ventas360.DTO;

/**
 * Created by Monica Toribio Rojas on julio 2017.
 * Expedio Digital
 * monica.toribio.rojas@gmail.com
 */

public class DTOServicio {
    public static final String TIPO_DESARROLLO = "D";
    public static final String TIPO_PRODUCCION = "P";
    private String idServicio;
    private String url;
    private String tipo;

    public DTOServicio(){}

    public String getIdServicio() {
        return idServicio;
    }

    public void setIdServicio(String idServicio) {
        this.idServicio = idServicio;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }
}
