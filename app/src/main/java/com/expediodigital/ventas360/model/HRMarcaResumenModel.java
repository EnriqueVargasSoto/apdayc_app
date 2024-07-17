package com.expediodigital.ventas360.model;

/**
 * Created by Kevin Robin Meza Hinostroza on 11/12/2018.
 * kevin.meza@expediodigital.com
 */
public class HRMarcaResumenModel {
    private int ejercicio;
    private int periodo;
    private String idCliente;
    private int cantidad;

    public int getEjercicio() {
        return ejercicio;
    }

    public void setEjercicio(int ejercicio) {
        this.ejercicio = ejercicio;
    }

    public int getPeriodo() {
        return periodo;
    }

    public void setPeriodo(int periodo) {
        this.periodo = periodo;
    }

    public String getIdCliente() {
        return idCliente;
    }

    public void setIdCliente(String idCliente) {
        this.idCliente = idCliente;
    }

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }
}
