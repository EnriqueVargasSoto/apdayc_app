package com.expediodigital.ventas360.model;

import android.os.Parcelable;

import java.io.Serializable;

public class Botella implements Serializable {
    String marca;
    String certeza;

    public String getMarca() {
        return marca;
    }

    public void setMarca(String marca) {
        this.marca = marca;
    }

    public String getCerteza() {
        return certeza;
    }

    public void setCerteza(String certeza) {
        this.certeza = certeza;
    }
}
