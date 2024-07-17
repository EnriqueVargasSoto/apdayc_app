package com.expediodigital.ventas360.model;

/**
 * Created by Kevin Robin Meza Hinostroza on 14/10/2018.
 * kevin.meza@expediodigital.com
 */
public class HojaRutaIndicadorModel {
    private int ejercicio;
    private int periodo;
    private String idCliente;
    private String tipoCobertura;
    private int programado = 0;
    private int transcurrido = 0;
    private int liquidado = 0;
    private double hitRate;
    private double venAnoAnterior;
    private double venMesAnterior;
    private double avanceMesActual;
    private double proyectado;
    private double avanceAnual;
    private double avanceMes;
    private double CUOTAGTM;
    private String SEGMENTO;
    private int EXHIBIDORES;
    private int NROPTAFRIOGTM;
    private double coberturaMultiple;


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

    public String getTipoCobertura() {
        return tipoCobertura;
    }

    public void setTipoCobertura(String tipoCobertura) {
        this.tipoCobertura = tipoCobertura;
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

    public double getVenAnoAnterior() {
        return venAnoAnterior;
    }

    public void setVenAnoAnterior(double venAnoAnterior) {
        this.venAnoAnterior = venAnoAnterior;
    }

    public double getVenMesAnterior() {
        return venMesAnterior;
    }

    public void setVenMesAnterior(double venMesAnterior) {
        this.venMesAnterior = venMesAnterior;
    }

    public double getAvanceMesActual() {
        return avanceMesActual;
    }

    public void setAvanceMesActual(double avanceMesActual) {
        this.avanceMesActual = avanceMesActual;
    }

    public double getProyectado() {
        return proyectado;
    }

    public void setProyectado(double proyectado) {
        this.proyectado = proyectado;
    }

    public double getAvanceAnual() {
        return avanceAnual;
    }

    public void setAvanceAnual(double avanceAnual) {
        this.avanceAnual = avanceAnual;
    }

    public double getAvanceMes() {
        return avanceMes;
    }

    public void setAvanceMes(double avanceMes) {
        this.avanceMes = avanceMes;
    }

    public double getCUOTAGTM() {
        return CUOTAGTM;
    }

    public void setCUOTAGTM(double CUOTAGTM) {
        this.CUOTAGTM = CUOTAGTM;
    }

    public String getSEGMENTO() {
        return SEGMENTO;
    }

    public void setSEGMENTO(String SEGMENTO) {
        this.SEGMENTO = SEGMENTO;
    }

    public int getEXHIBIDORES() {
        return EXHIBIDORES;
    }

    public void setEXHIBIDORES(int EXHIBIDORES) {
        this.EXHIBIDORES = EXHIBIDORES;
    }

    public int getNROPTAFRIOGTM() {
        return NROPTAFRIOGTM;
    }

    public void setNROPTAFRIOGTM(int NROPTAFRIOGTM) {
        this.NROPTAFRIOGTM = NROPTAFRIOGTM;
    }

    public double getCoberturaMultiple() {
        return coberturaMultiple;
    }

    public void setCoberturaMultiple(double coberturaMultiple) {
        this.coberturaMultiple = coberturaMultiple;
    }
}
