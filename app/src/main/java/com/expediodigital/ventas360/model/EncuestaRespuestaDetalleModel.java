package com.expediodigital.ventas360.model;

import android.graphics.Bitmap;
import android.view.View;

import com.google.gson.annotations.Expose;

/**
 * Created by Kevin Robinson Meza Hinostroza on marzo 2018.
 * Expedio Digital
 * Kevin.Meza@expediodigital.com
 */
//@Expose expone los atributos para ser serializados por Gson (se debe hacer en especial cuando hay atributos como Bitmap,View, etc que no pueden ser serializados en una cadena JSON)
public class EncuestaRespuestaDetalleModel {
    /*@Expose private String idEncuesta;
    @Expose private String idEncuestaDetalle;
    @Expose private String idCliente;*/

    @Expose private int idPregunta;
    @Expose private String idAlternativas;//se puede almacenar 1 id o muchos en caso de ser respuesta multiple (1,2,3...)
    @Expose private String descripcion;//se puede almacenar texto en caso de ser una respuesta abierta
    @Expose private String tipoRespuesta;//se almacena el tipo de respuesta a modo historial
    @Expose private double latitud;
    @Expose private double longitud;
    private String fotoURL;

    @Expose private String stringFoto;

    public int getIdPregunta() {
        return idPregunta;
    }

    public void setIdPregunta(int idPregunta) {
        this.idPregunta = idPregunta;
    }

    public String getIdAlternativas() {
        return idAlternativas;
    }

    public void setIdAlternativas(String idAlternativas) {
        this.idAlternativas = idAlternativas;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getTipoRespuesta() {
        return tipoRespuesta;
    }

    public void setTipoRespuesta(String tipoRespuesta) {
        this.tipoRespuesta = tipoRespuesta;
    }

    public double getLatitud() {
        return latitud;
    }

    public void setLatitud(double latitud) {
        this.latitud = latitud;
    }

    public double getLongitud() {
        return longitud;
    }

    public void setLongitud(double longitud) {
        this.longitud = longitud;
    }

    public String getStringFoto() {
        return stringFoto;
    }

    public void setStringFoto(String stringFoto) {
        this.stringFoto = stringFoto;
    }
    
    public String getFotoURL() {
        return fotoURL;
    }

    public void setFotoURL(String fotoURL) {
        this.fotoURL = fotoURL;
    }
}
