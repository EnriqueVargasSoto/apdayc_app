package com.expediodigital.ventas360.model;

/**
 * Created by Kevin Robinson Meza Hinostroza on noviembre 2017.
 * Expedio Digital
 * Kevin.Meza@expediodigital.com
 */

public class EncuestaAlternativaModel {
    private int idAlternativa;
    private String descripcion;
    private int orden;

    public int getIdAlternativa() {
        return idAlternativa;
    }

    public void setIdAlternativa(int idAlternativa) {
        this.idAlternativa = idAlternativa;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public int getOrden() {
        return orden;
    }

    public void setOrden(int orden) {
        this.orden = orden;
    }
}
