package com.expediodigital.ventas360.model;

/**
 * Created by Kevin Robin Meza Hinostroza on 13/04/2018.
 * Expedio Digital
 * kevin.meza@expediodigital.com
 */

public class ClienteCoordenadasModel {
    public static final String FLAG_PENDIENTE = "P";
    public static final String FLAG_ENVIADO = "E";
    private String idCliente;
    private String latitud;
    private String longitud;
    private String flag;

    public String getIdCliente() {
        return idCliente;
    }

    public void setIdCliente(String idCliente) {
        this.idCliente = idCliente;
    }

    public String getLatitud() {
        return latitud;
    }

    public void setLatitud(String latitud) {
        this.latitud = latitud;
    }

    public String getLongitud() {
        return longitud;
    }

    public void setLongitud(String longitud) {
        this.longitud = longitud;
    }

    public String getFlag() {
        return flag;
    }

    public void setFlag(String flag) {
        this.flag = flag;
    }
}
