package com.expediodigital.ventas360.model;

/**
 * Created by Kevin Robin Meza Hinostroza on 11/12/2018.
 * kevin.meza@expediodigital.com
 */
public class HRClienteModel {
    private int ejercicio;
    private int periodo;
    private String idCliente;
    private int programado;
    private int transcurrido;
    private int liquidado;
    private double hitRate;
    private double coberturaMultiple;
    private double cuotaSoles;
    private double cuotaPaquetes;
    private double ventaSoles;
    private double ventaPaquetes;
    private int diasLaborados;
    private int diasLaborales;
    private String segmento;
    private int nroExhibidores;
    private int nroPuertasFrio;
    private double avance;
    private double necesidadDiaSoles;
    private double necesidadDiaPaquetes;

    public int getEjercicio() {
        return ejercicio;
    }

    public void setEjercicio(int ejercicio) {
        this.ejercicio = ejercicio;
    }

    public int getPeriodo() {
        return periodo;
    }

    public void setPeriodo(int periodo) {
        this.periodo = periodo;
    }

    public String getIdCliente() {
        return idCliente;
    }

    public void setIdCliente(String idCliente) {
        this.idCliente = idCliente;
    }

    public int getProgramado() {
        return programado;
    }

    public void setProgramado(int programado) {
        this.programado = programado;
    }

    public int getTranscurrido() {
        return transcurrido;
    }

    public void setTranscurrido(int transcurrido) {
        this.transcurrido = transcurrido;
    }

    public int getLiquidado() {
        return liquidado;
    }

    public void setLiquidado(int liquidado) {
        this.liquidado = liquidado;
    }

    public double getHitRate() {
        return hitRate;
    }

    public void setHitRate(double hitRate) {
        this.hitRate = hitRate;
    }

    public double getCoberturaMultiple() {
        return coberturaMultiple;
    }

    public void setCoberturaMultiple(double coberturaMultiple) {
        this.coberturaMultiple = coberturaMultiple;
    }

    public double getCuotaSoles() {
        return cuotaSoles;
    }

    public void setCuotaSoles(double cuotaSoles) {
        this.cuotaSoles = cuotaSoles;
    }

    public double getCuotaPaquetes() {
        return cuotaPaquetes;
    }

    public void setCuotaPaquetes(double cuotaPaquetes) {
        this.cuotaPaquetes = cuotaPaquetes;
    }

    public double getVentaSoles() {
        return ventaSoles;
    }

    public void setVentaSoles(double ventaSoles) {
        this.ventaSoles = ventaSoles;
    }

    public double getVentaPaquetes() {
        return ventaPaquetes;
    }

    public void setVentaPaquetes(double ventaPaquetes) {
        this.ventaPaquetes = ventaPaquetes;
    }

    public int getDiasLaborados() {
        return diasLaborados;
    }

    public void setDiasLaborados(int diasLaborados) {
        this.diasLaborados = diasLaborados;
    }

    public int getDiasLaborales() {
        return diasLaborales;
    }

    public void setDiasLaborales(int diasLaborales) {
        this.diasLaborales = diasLaborales;
    }

    public String getSegmento() {
        return segmento;
    }

    public void setSegmento(String segmento) {
        this.segmento = segmento;
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

    public double getAvance() {
        return avance;
    }

    public void setAvance(double avance) {
        this.avance = avance;
    }

    public double getNecesidadDiaSoles() {
        return necesidadDiaSoles;
    }

    public void setNecesidadDiaSoles(double necesidadDiaSoles) {
        this.necesidadDiaSoles = necesidadDiaSoles;
    }

    public double getNecesidadDiaPaquetes() {
        return necesidadDiaPaquetes;
    }

    public void setNecesidadDiaPaquetes(double necesidadDiaPaquetes) {
        this.necesidadDiaPaquetes = necesidadDiaPaquetes;
    }
}
