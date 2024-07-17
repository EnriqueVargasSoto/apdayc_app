package com.expediodigital.ventas360.model;

/**
 * Created by Kevin Robinson Meza Hinostroza on noviembre 2017.
 * Expedio Digital
 * Kevin.Meza@expediodigital.com
 */

public class EncuestaDetalleModel {
    public static final String TIPO_PRE_PEDIDO = "PRE";
    public static final String TIPO_POST_PEDIDO = "POST";

    private int idEncuesta;
    private int idEncuestaDetalle;
    private String fechaInicio;
    private String fechaFin;
    private int clientesObligatorios;
    private int clientesAnonimos;
    private int encuestasMinimas;
    private int fotosMinimas;
    private int maximoIntentosCliente;
    private String filtroOcasion;
    private String filtroCanalVentas;
    private String filtroGiro;
    private String filtroSubGiro;
    private int porCliente;
    private int porSegmento;

    private String descripcionEncuesta;
    private String tipoEncuesta;
    private String idTipoEncuesta;

    public int getIdEncuesta() {
        return idEncuesta;
    }

    public void setIdEncuesta(int idEncuesta) {
        this.idEncuesta = idEncuesta;
    }

    public int getIdEncuestaDetalle() {
        return idEncuestaDetalle;
    }

    public void setIdEncuestaDetalle(int idEncuestaDetalle) {
        this.idEncuestaDetalle = idEncuestaDetalle;
    }

    public String getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(String fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public String getFechaFin() {
        return fechaFin;
    }

    public void setFechaFin(String fechaFin) {
        this.fechaFin = fechaFin;
    }

    public int getClientesObligatorios() {
        return clientesObligatorios;
    }

    public void setClientesObligatorios(int clientesObligatorios) {
        this.clientesObligatorios = clientesObligatorios;
    }

    public int getClientesAnonimos() {
        return clientesAnonimos;
    }

    public void setClientesAnonimos(int clientesAnonimos) {
        this.clientesAnonimos = clientesAnonimos;
    }

    public int getEncuestasMinimas() {
        return encuestasMinimas;
    }

    public void setEncuestasMinimas(int encuestasMinimas) {
        this.encuestasMinimas = encuestasMinimas;
    }

    public int getFotosMinimas() {
        return fotosMinimas;
    }

    public void setFotosMinimas(int fotosMinimas) {
        this.fotosMinimas = fotosMinimas;
    }

    public int getMaximoIntentosCliente() {
        return maximoIntentosCliente;
    }

    public void setMaximoIntentosCliente(int maximoIntentosCliente) {
        this.maximoIntentosCliente = maximoIntentosCliente;
    }

    public String getFiltroOcasion() {
        return filtroOcasion;
    }

    public void setFiltroOcasion(String filtroOcasion) {
        this.filtroOcasion = filtroOcasion;
    }

    public String getFiltroCanalVentas() {
        return filtroCanalVentas;
    }

    public void setFiltroCanalVentas(String filtroCanalVentas) {
        this.filtroCanalVentas = filtroCanalVentas;
    }

    public String getFiltroGiro() {
        return filtroGiro;
    }

    public void setFiltroGiro(String filtroGiro) {
        this.filtroGiro = filtroGiro;
    }

    public String getFiltroSubGiro() {
        return filtroSubGiro;
    }

    public void setFiltroSubGiro(String filtroSubGiro) {
        this.filtroSubGiro = filtroSubGiro;
    }

    public int getPorCliente() {
        return porCliente;
    }

    public void setPorCliente(int porCliente) {
        this.porCliente = porCliente;
    }

    public int getPorSegmento() {
        return porSegmento;
    }

    public void setPorSegmento(int porSegmento) {
        this.porSegmento = porSegmento;
    }

    public String getDescripcionEncuesta() {
        return descripcionEncuesta;
    }

    public void setDescripcionEncuesta(String descripcionEncuesta) {
        this.descripcionEncuesta = descripcionEncuesta;
    }

    public String getTipoEncuesta() {
        return tipoEncuesta;
    }

    public void setTipoEncuesta(String idTipoEncuesta) {
        this.tipoEncuesta = idTipoEncuesta;
    }

    public String getIdTipoEncuesta() {
        return idTipoEncuesta;
    }

    public void setIdTipoEncuesta(String idTipoEncuesta) {
        this.idTipoEncuesta = idTipoEncuesta;
    }
}
