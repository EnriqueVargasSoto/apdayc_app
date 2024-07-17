package com.expediodigital.ventas360.model;

/**
 * Created by Kevin Robinson Meza Hinostroza on agosto 2017.
 * Expedio Digital
 * Kevin.Meza@expediodigital.com
 */

public class LiquidacionProductoModel {
    private String numeroDocumento;
    private String idProducto;
    private String descripcion;
    private String factorConversion;
    private String stockGuia;
    private String stockVenta;
    private String stockDevolucion;
    private String diferencia;

    private boolean selected = false;

    public String getNumeroDocumento() {
        return numeroDocumento;
    }

    public void setNumeroDocumento(String numeroDocumento) {
        this.numeroDocumento = numeroDocumento;
    }

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

    public String getFactorConversion() {
        return factorConversion;
    }

    public void setFactorConversion(String factorConversion) {
        this.factorConversion = factorConversion;
    }

    public String getStockGuia() {
        return stockGuia;
    }

    public void setStockGuia(String stockGuia) {
        this.stockGuia = stockGuia;
    }

    public String getStockVenta() {
        return stockVenta;
    }

    public void setStockVenta(String stockVenta) {
        this.stockVenta = stockVenta;
    }

    public String getStockDevolucion() {
        return stockDevolucion;
    }

    public void setStockDevolucion(String stockDevolucion) {
        this.stockDevolucion = stockDevolucion;
    }

    public String getDiferencia() {
        return diferencia;
    }

    public void setDiferencia(String diferencia) {
        this.diferencia = diferencia;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }
}
