package com.expediodigital.ventas360.model;

/**
 * Created by Kevin Robinson Meza Hinostroza on agosto 2017.
 * Expedio Digital
 * Kevin.Meza@expediodigital.com
 */

public class DevolucionCabeceraModel {
    public static String FLAG_PENDIENTE = "P";
    public static String FLAG_ENVIADO = "E";

    private String numeroGuia;
    private String idVendedor;
    private String fechaDevolucion;
    private String flag;

    public String getNumeroGuia() {
        return numeroGuia;
    }

    public void setNumeroGuia(String numeroGuia) {
        this.numeroGuia = numeroGuia;
    }

    public String getIdVendedor() {
        return idVendedor;
    }

    public void setIdVendedor(String idVendedor) {
        this.idVendedor = idVendedor;
    }

    public String getFechaDevolucion() {
        return fechaDevolucion;
    }

    public void setFechaDevolucion(String fechaDevolucion) {
        this.fechaDevolucion = fechaDevolucion;
    }

    public String getFlag() {
        return flag;
    }

    public void setFlag(String flag) {
        this.flag = flag;
    }
}
