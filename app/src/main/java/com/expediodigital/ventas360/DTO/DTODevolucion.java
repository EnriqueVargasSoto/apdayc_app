package com.expediodigital.ventas360.DTO;

import java.util.ArrayList;

/**
 * Created by Kevin Robinson Meza Hinostroza on agosto 2017.
 * Expedio Digital
 * Kevin.Meza@expediodigital.com
 */

public class DTODevolucion {
    private String idEmpresa;
    private String idSucursal;
    private String numeroGuia;
    private String idVendedor;
    private String fechaDevolucion;
    private String flag;

    private ArrayList<DTODevolucionDetalle> detalles;

    public String getIdEmpresa() {
        return idEmpresa;
    }

    public void setIdEmpresa(String idEmpresa) {
        this.idEmpresa = idEmpresa;
    }

    public String getIdSucursal() {
        return idSucursal;
    }

    public void setIdSucursal(String idSucursal) {
        this.idSucursal = idSucursal;
    }

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

    public ArrayList<DTODevolucionDetalle> getDetalles() {
        return detalles;
    }

    public void setDetalles(ArrayList<DTODevolucionDetalle> detalles) {
        this.detalles = detalles;
    }
}
