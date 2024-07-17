package com.expediodigital.ventas360.model;

/**
 * Created by Kevin Robinson Meza Hinostroza on julio 2017.
 * Expedio Digital
 * Meza.Hinostroza.Robin@gmail.com
 */

public class PedidoDetalleModel {
    private String numeroPedido;
    private String idProducto;
    private String idPoliticaPrecio;
    private String tipoProducto;
    private double precioBruto;
    private int cantidad;
    private double precioNeto;
    private  String idUnidadMedida;
    private double pesoNeto;

    private String descripcion;
    private String descripcionUnidadMedida;
    private int factorConversion;
    private int item;
    private int sinStock;
    private double percepcion;
    private double ISC;
    private String malla = "";
    private int idPromocion;
    private String estadoDetalle = "1";

    public String getEstadoDetalle() {
        return estadoDetalle;
    }

    public void setEstadoDetalle(String estadoDetalle) {
        this.estadoDetalle = estadoDetalle;
    }

    public String getNumeroPedido() {
        return numeroPedido;
    }

    public void setNumeroPedido(String numeroPedido) {
        this.numeroPedido = numeroPedido;
    }

    public String getIdProducto() {
        return idProducto;
    }

    public void setIdProducto(String idProducto) {
        this.idProducto = idProducto;
    }

    public String getIdPoliticaPrecio() {
        return idPoliticaPrecio;
    }

    public void setIdPoliticaPrecio(String idPoliticaPrecio) {
        this.idPoliticaPrecio = idPoliticaPrecio;
    }

    public String getTipoProducto() {
        return tipoProducto;
    }

    public void setTipoProducto(String tipoProducto) {
        this.tipoProducto = tipoProducto;
    }

    public double getPrecioBruto() {
        return precioBruto;
    }

    public void setPrecioBruto(double precioBruto) {
        this.precioBruto = precioBruto;
    }

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }

    public double getPrecioNeto() {
        return precioNeto;
    }

    public void setPrecioNeto(double precioNeto) {
        this.precioNeto = precioNeto;
    }

    public String getIdUnidadMedida() {
        return idUnidadMedida;
    }

    public void setIdUnidadMedida(String idUnidadMedida) {
        this.idUnidadMedida = idUnidadMedida;
    }

    public double getPesoNeto() {
        return pesoNeto;
    }

    public void setPesoNeto(double pesoNeto) {
        this.pesoNeto = pesoNeto;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcionUnidadMedida() {
        return descripcionUnidadMedida;
    }

    public void setDescripcionUnidadMedida(String descripcionUnidadMedida) {
        this.descripcionUnidadMedida = descripcionUnidadMedida;
    }

    public int getFactorConversion() {
        return factorConversion;
    }

    public void setFactorConversion(int factorConversion) {
        this.factorConversion = factorConversion;
    }

    public int getItem() {
        return item;
    }

    public void setItem(int item) {
        this.item = item;
    }

    public int getSinStock() {
        return sinStock;
    }

    public void setSinStock(int sinStock) {
        this.sinStock = sinStock;
    }

    public double getPercepcion() {
        return percepcion;
    }

    public void setPercepcion(double percepcion) {
        this.percepcion = percepcion;
    }

    public double getISC() {
        return ISC;
    }

    public void setISC(double ISC) {
        this.ISC = ISC;
    }

    public String getMalla() {
        return malla;
    }

    public void setMalla(String malla) {
        this.malla = malla;
    }

    public int getIdPromocion() {
        return idPromocion;
    }

    public void setIdPromocion(int idPromocion) {
        this.idPromocion = idPromocion;
    }
}
