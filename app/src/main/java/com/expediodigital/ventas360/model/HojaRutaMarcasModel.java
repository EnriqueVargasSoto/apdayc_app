package com.expediodigital.ventas360.model;

/**
 * Created by Kevin Robin Meza Hinostroza on 14/10/2018.
 * kevin.meza@expediodigital.com
 */
public class HojaRutaMarcasModel {
    private int ejercicio;
    private int periodo;
    private String idCliente;
    private String marca;
    private int cantidadPaquetes = 0;

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

    public String getMarca() {
        return marca;
    }

    public void setMarca(String marca) {
        this.marca = marca;
    }

    public int getCantidadPaquetes() {
        return cantidadPaquetes;
    }

    public void setCantidadPaquetes(int cantidadPaquetes) {
        this.cantidadPaquetes = cantidadPaquetes;
    }
}
