package com.expediodigital.ventas360.model;

/**
 * Created by Kevin Robinson Meza Hinostroza on julio 2017.
 * Expedio Digital
 * Kevin.Meza@expediodigital.com
 */

public class ProductoKardex {
    private String idProducto;
    private String descripcion;
    private String idLinea;
    private String idFamilia;
    private int factorConversion;

    private int stockInicial;
    private int stockPedido;
    private int stockDespachado;
    private int stockDisponibleGeneral;
    private int stockDisponibleUnidadMayor;
    private int stockDisponibleUnidadMenor;

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

    public String getIdLinea() {
        return idLinea;
    }

    public void setIdLinea(String idLinea) {
        this.idLinea = idLinea;
    }

    public String getIdFamilia() {
        return idFamilia;
    }

    public void setIdFamilia(String idFamilia) {
        this.idFamilia = idFamilia;
    }

    public int getStockInicial() {
        return stockInicial;
    }

    public void setStockInicial(int stockInicial) {
        this.stockInicial = stockInicial;
    }

    public int getStockPedido() {
        return stockPedido;
    }

    public void setStockPedido(int stockPedido) {
        this.stockPedido = stockPedido;
    }

    public int getStockDespachado() {
        return stockDespachado;
    }

    public void setStockDespachado(int stockDespachado) {
        this.stockDespachado = stockDespachado;
    }

    public int getStockDisponibleUnidadMayor() {
        return stockDisponibleUnidadMayor;
    }

    public void setStockDisponibleUnidadMayor(int stockDisponibleUnidadMayor) {
        this.stockDisponibleUnidadMayor = stockDisponibleUnidadMayor;
    }

    public int getStockDisponibleUnidadMenor() {
        return stockDisponibleUnidadMenor;
    }

    public void setStockDisponibleUnidadMenor(int stockDisponibleUnidadMenor) {
        this.stockDisponibleUnidadMenor = stockDisponibleUnidadMenor;
    }

    public int getStockDisponibleGeneral() {
        return stockDisponibleGeneral;
    }

    public void setStockDisponibleGeneral(int stockDisponibleGeneral) {
        this.stockDisponibleGeneral = stockDisponibleGeneral;
    }

    public int getFactorConversion() {
        return factorConversion;
    }

    public void setFactorConversion(int factorConversion) {
        this.factorConversion = factorConversion;
    }
}
