package com.expediodigital.ventas360.model;

/**
 * Created by Kevin Robinson Meza Hinostroza on octubre 2017.
 * Expedio Digital
 * Kevin.Meza@expediodigital.com
 */

public class JSONModel {
    public static final String ID_JSON_RUTAS = "JSONRutas";
    /**
     * Constante utilizado en MapFragment, el cual indica que no hay algun index de ruta que se haya quedado en Preferencias, es decir que no se seleccione alguna ruta por defecto al cargar el mapa
     */
    public static final int SIN_RUTA_SELECCIONADA = -1;
    private String idJSON;
    private String JSON;

    public String getIdJSON() {
        return idJSON;
    }

    public void setIdJSON(String idJSON) {
        this.idJSON = idJSON;
    }

    public String getJSON() {
        return JSON;
    }

    public void setJSON(String JSON) {
        this.JSON = JSON;
    }
}
