package com.expediodigital.ventas360.model;

import java.io.Serializable;

public class CantidadBotella implements Serializable {
    String marca;
    int cantidad;

    public String getMarca() {
        return marca;
    }

    public void setMarca(String marca) {
        this.marca = marca;
    }

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }
}
