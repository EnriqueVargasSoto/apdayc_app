package com.expediodigital.ventas360.model;

/**
 * Created by Kevin Robinson Meza Hinostroza on julio 2017.
 * Expedio Digital
 * Meza.Hinostroza.Robin@gmail.com
 */

public class ClienteModel {
    public static final int ESTADO_PEDIDO_PENDIENTE = 0;
    public static final int ESTADO_PEDIDO_VISITADO = 1;
    public static final int ESTADO_PEDIDO_ANULADO = 2;

    private String idCliente;
    private String rucDni;
    private String razonSocial;
    private String correo;
    private String idSubGiro;
    private String subGiro;
    private String idModulo;
    private String idRuta;
    private int orden;
    private String direccion;
    private String direccionFiscal;
    private int estadoPedido;
    private double latitud;
    private double longitud;
    private String idSegmento;
    private String idCluster;
    private double limiteCredito;
    private int nroExhibidores;
    private int nroPuertasFrio;

    private boolean tieneEncuesta;
    private String flagEncuesta;
    private String flagLocalizacion;
    private String whatsapp;

    public String getIdCliente() {
        return idCliente;
    }

    public void setIdCliente(String idCliente) {
        this.idCliente = idCliente;
    }

    public String getRucDni() {
        return rucDni;
    }

    public void setRucDni(String rucDni) {
        this.rucDni = rucDni;
    }

    public String getRazonSocial() {
        return razonSocial;
    }

    public void setRazonSocial(String razonSocial) {
        this.razonSocial = razonSocial;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getIdSubGiro() {
        return idSubGiro;
    }

    public void setIdSubGiro(String idSubGiro) {
        this.idSubGiro = idSubGiro;
    }

    public String getSubGiro() {
        return subGiro;
    }

    public void setSubGiro(String subGiro) {
        this.subGiro = subGiro;
    }

    public String getIdModulo() {
        return idModulo;
    }

    public void setIdModulo(String idModulo) {
        this.idModulo = idModulo;
    }

    public String getIdRuta() {
        return idRuta;
    }

    public void setIdRuta(String idRuta) {
        this.idRuta = idRuta;
    }

    public int getOrden() {
        return orden;
    }

    public void setOrden(int orden) {
        this.orden = orden;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getDireccionFiscal() {
        return direccionFiscal;
    }

    public void setDireccionFiscal(String direccionFiscal) {
        this.direccionFiscal = direccionFiscal;
    }

    public int getEstadoPedido() {
        return estadoPedido;
    }

    public void setEstadoPedido(int estadoPedido) {
        this.estadoPedido = estadoPedido;
    }

    public double getLatitud() {
        return latitud;
    }

    public void setLatitud(double latitud) {
        this.latitud = latitud;
    }

    public double getLongitud() {
        return longitud;
    }

    public void setLongitud(double longitud) {
        this.longitud = longitud;
    }

    public String getIdSegmento() {
        return idSegmento;
    }

    public void setIdSegmento(String idSegmento) {
        this.idSegmento = idSegmento;
    }

    public String getIdCluster() {
        return idCluster;
    }

    public void setIdCluster(String idCluster) {
        this.idCluster = idCluster;
    }

    public double getLimiteCredito() {
        return limiteCredito;
    }

    public void setLimiteCredito(double limiteCredito) {
        this.limiteCredito = limiteCredito;
    }


    public boolean tieneEncuesta() {
        return tieneEncuesta;
    }

    public void setTieneEncuesta(boolean tieneEncuesta) {
        this.tieneEncuesta = tieneEncuesta;
    }

    public String getFlagEncuesta() {
        return flagEncuesta;
    }

    public void setFlagEncuesta(String flagEncuesta) {
        this.flagEncuesta = flagEncuesta;
    }

    public int getNroExhibidores() {
        return nroExhibidores;
    }

    public void setNroExhibidores(int nroExhibidores) {
        this.nroExhibidores = nroExhibidores;
    }

    public int getNroPuertasFrio() {
        return nroPuertasFrio;
    }

    public void setNroPuertasFrio(int nroPuertasFrio) {
        this.nroPuertasFrio = nroPuertasFrio;
    }

    public String getFlagLocalizacion() {
        return flagLocalizacion;
    }

    public void setFlagLocalizacion(String flagLocalizacion) {
        this.flagLocalizacion = flagLocalizacion;
    }

    public String getWhatsapp() {
        return whatsapp;
    }

    public void setWhatsapp(String whatsapp) {
        this.whatsapp = whatsapp;
    }
}
