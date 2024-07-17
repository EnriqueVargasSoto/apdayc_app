package com.expediodigital.ventas360.model;

import java.io.Serializable;
import java.util.List;

public class ResultBotellaDetalle implements Serializable {
    List<Botella> objetos;
    String tiempo;
    List<CantidadBotella> resumen;

    public List<Botella> getObjetos() {
        return objetos;
    }

    public void setObjetos(List<Botella> objetos) {
        this.objetos = objetos;
    }

    public String getTiempo() {
        return tiempo;
    }

    public void setTiempo(String tiempo) {
        this.tiempo = tiempo;
    }

    public List<CantidadBotella> getResumen() {
        return resumen;
    }

    public void setResumen(List<CantidadBotella> resumen) {
        this.resumen = resumen;
    }
}
