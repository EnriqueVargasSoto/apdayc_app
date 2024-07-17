package com.expediodigital.ventas360.model;

import java.io.Serializable;

public class ResultBotella implements Serializable {
    String resultado;
    String entrada;
    ResultBotellaDetalle detalle;

    public String getResultado() {
        return resultado;
    }

    public void setResultado(String resultado) {
        this.resultado = resultado;
    }

    public String getEntrada() {
        return entrada;
    }

    public void setEntrada(String entrada) {
        this.entrada = entrada;
    }

    public ResultBotellaDetalle getDetalle() {
        return detalle;
    }

    public void setDetalle(ResultBotellaDetalle detalle) {
        this.detalle = detalle;
    }
}
