package com.expediodigital.ventas360.model;

/**
 * Created by Kevin Robinson Meza Hinostroza on julio 2017.
 * Expedio Digital
 * Kevin.Meza@expediodigital.com
 */

public class DevolucionDetalleModel {
    private String numeroGuia;
    private String idProducto;
    private String descripcion;
    private int factorConversion;

    private int stockDevolucion;
    private int stockDevolucionUnidadMayor;
    private int stockDevolucionUnidadMenor;
    private String idUnidadMedidaMayor;
    private String idUnidadMedidaMenor;
    private String unidadMedidaMayor;
    private String unidadMedidaMenor;

    private int modificado = 0;
    private String flag;

    public String getIdProducto() {
        return idProducto;
    }

    public void setIdProducto(String idProducto) {
        this.idProducto = idProducto;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public int getFactorConversion() {
        return factorConversion;
    }

    public void setFactorConversion(int factorConversion) {
        this.factorConversion = factorConversion;
    }

    public int getStockDevolucion() {
        return stockDevolucion;
    }

    public void setStockDevolucion(int stockDevolucion) {
        this.stockDevolucion = stockDevolucion;
    }

    public int getStockDevolucionUnidadMayor() {
        return stockDevolucionUnidadMayor;
    }

    public void setStockDevolucionUnidadMayor(int stockDevolucionUnidadMayor) {
        this.stockDevolucionUnidadMayor = stockDevolucionUnidadMayor;
    }

    public int getStockDevolucionUnidadMenor() {
        return stockDevolucionUnidadMenor;
    }

    public void setStockDevolucionUnidadMenor(int stockDevolucionUnidadMenor) {
        this.stockDevolucionUnidadMenor = stockDevolucionUnidadMenor;
    }

    public int getModificado() {
        return modificado;
    }

    public void setModificado(int modificado) {
        this.modificado = modificado;
    }

    public String getUnidadMedidaMayor() {
        return unidadMedidaMayor;
    }

    public void setUnidadMedidaMayor(String unidadMedidaMayor) {
        this.unidadMedidaMayor = unidadMedidaMayor;
    }

    public String getUnidadMedidaMenor() {
        return unidadMedidaMenor;
    }

    public void setUnidadMedidaMenor(String unidadMedidaMenor) {
        this.unidadMedidaMenor = unidadMedidaMenor;
    }

    public String getIdUnidadMedidaMayor() {
        return idUnidadMedidaMayor;
    }

    public void setIdUnidadMedidaMayor(String idUnidadMedidaMayor) {
        this.idUnidadMedidaMayor = idUnidadMedidaMayor;
    }

    public String getIdUnidadMedidaMenor() {
        return idUnidadMedidaMenor;
    }

    public void setIdUnidadMedidaMenor(String idUnidadMedidaMenor) {
        this.idUnidadMedidaMenor = idUnidadMedidaMenor;
    }

    public String getNumeroGuia() {
        return numeroGuia;
    }

    public void setNumeroGuia(String numeroGuia) {
        this.numeroGuia = numeroGuia;
    }

    public String getFlag() {
        return flag;
    }

    public void setFlag(String flag) {
        this.flag = flag;
    }
}
