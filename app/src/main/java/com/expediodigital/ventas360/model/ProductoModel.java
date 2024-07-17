package com.expediodigital.ventas360.model;

/**
 * Created by Kevin Robinson Meza Hinostroza on julio 2017.
 * Expedio Digital
 * Meza.Hinostroza.Robin@gmail.com
 */

public class ProductoModel {
    public static final String TIPO_VENTA = "V";
    public static final String TIPO_BONIFICACION = "B";
    public static final String TIPO_PUBLICIDAD = "P";
    public static final String TIPO_SERVICIO = "S";

    private String idProducto;
    private String descripcion;
    private String idLinea;
    private String idFamilia;
    private double peso;
    private String idProveedor;
    private int stockInicial;
    private int stockPedidos;
    private int stockDespachado;
    private int stockDisponible;
    private double precioMenor;
    private double precioMayor;
    private String tipoProducto;
    private String idPoliticaPrecio;
    private String idUnidadManejo;
    private String contenido;
    private int prom_grupo_unidades;

    public static String getTipoVenta() {
        return TIPO_VENTA;
    }

    public static String getTipoBonificacion() {
        return TIPO_BONIFICACION;
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

    public double getPeso() {
        return peso;
    }

    public void setPeso(double peso) {
        this.peso = peso;
    }

    public String getIdProveedor() {
        return idProveedor;
    }

    public void setIdProveedor(String idProveedor) {
        this.idProveedor = idProveedor;
    }

    public int getStockDisponible() {
        return stockDisponible;
    }

    public void setStockDisponible(int stockDisponible) {
        this.stockDisponible = stockDisponible;
    }

    public double getPrecioMenor() {
        return precioMenor;
    }

    public void setPrecioMenor(double precioMenor) {
        this.precioMenor = precioMenor;
    }

    public double getPrecioMayor() {
        return precioMayor;
    }

    public void setPrecioMayor(double precioMayor) {
        this.precioMayor = precioMayor;
    }

    public int getStockInicial() {
        return stockInicial;
    }

    public void setStockInicial(int stockInicial) {
        this.stockInicial = stockInicial;
    }

    public int getStockPedidos() {
        return stockPedidos;
    }

    public void setStockPedidos(int stockPedidos) {
        this.stockPedidos = stockPedidos;
    }

    public int getStockDespachado() {
        return stockDespachado;
    }

    public void setStockDespachado(int stockDespachado) {
        this.stockDespachado = stockDespachado;
    }

    public String getTipoProducto() {
        return tipoProducto;
    }

    public void setTipoProducto(String tipoProducto) {
        this.tipoProducto = tipoProducto;
    }

    public String getIdPoliticaPrecio() {
        return idPoliticaPrecio;
    }

    public void setIdPoliticaPrecio(String idPoliticaPrecio) {
        this.idPoliticaPrecio = idPoliticaPrecio;
    }

    public String getIdUnidadManejo() {
        return idUnidadManejo;
    }

    public void setIdUnidadManejo(String idUnidadManejo) {
        this.idUnidadManejo = idUnidadManejo;
    }

    public String getContenido() {
        return contenido;
    }

    public void setContenido(String contenido) {
        this.contenido = contenido;
    }

    public int getProm_grupo_unidades() {
        return prom_grupo_unidades;
    }

    public void setProm_grupo_unidades(int prom_grupo_unidades) {
        this.prom_grupo_unidades = prom_grupo_unidades;
    }
}
