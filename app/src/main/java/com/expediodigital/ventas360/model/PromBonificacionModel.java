package com.expediodigital.ventas360.model;

public class PromBonificacionModel {

    private int idPromocion;
    private String descripcion;
    private long fecini;
    private long fecfin;
    private String condicion;
    private String estado;
    private int orden;
    private String mecanica;
    private String malla;

    //grupo asociado
    private int idGrupo;

    //detalle de promocion
    private int idRango;
    private String unidad;
    private String desde;
    private String hasta;
    private float porcada;

    public PromBonificacionModel() {
        this.idPromocion = 0;
        this.descripcion = "";
        this.fecini = 0;
        this.fecfin = 0;
        this.condicion = "";
        this.estado = "";
        this.orden = 0;
        this.mecanica = "";
        this.malla = "";
        this.idGrupo = 0;
        this.idRango = 0;
        this.unidad = "";
        this.desde = "";
        this.hasta = "";
        this.porcada = 0;
    }

    public PromBonificacionModel(int idPromocion, String descripcion, long fecini, long fecfin, String condicion, String estado, int orden, String mecanica, String malla) {
        this.idPromocion = idPromocion;
        this.descripcion = descripcion;
        this.fecini = fecini;
        this.fecfin = fecfin;
        this.condicion = condicion;
        this.estado = estado;
        this.orden = orden;
        this.mecanica = mecanica;
        this.malla = malla;
    }

    public int getIdPromocion() {
        return idPromocion;
    }

    public void setIdPromocion(int idPromocion) {
        this.idPromocion = idPromocion;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public long getFecini() {
        return fecini;
    }

    public void setFecini(long fecini) {
        this.fecini = fecini;
    }

    public long getFecfin() {
        return fecfin;
    }

    public void setFecfin(long fecfin) {
        this.fecfin = fecfin;
    }

    public String getCondicion() {
        return condicion;
    }

    public void setCondicion(String condicion) {
        this.condicion = condicion;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public int getOrden() {
        return orden;
    }

    public void setOrden(int orden) {
        this.orden = orden;
    }

    public String getMecanica() {
        return mecanica;
    }

    public void setMecanica(String mecanica) {
        this.mecanica = mecanica;
    }

    public String getMalla() {
        return malla;
    }

    public void setMalla(String malla) {
        this.malla = malla;
    }

    public int getIdGrupo() {
        return idGrupo;
    }

    public void setIdGrupo(int idGrupo) {
        this.idGrupo = idGrupo;
    }

    public int getIdRango() {
        return idRango;
    }

    public void setIdRango(int idRango) {
        this.idRango = idRango;
    }

    public String getUnidad() {
        return unidad;
    }

    public void setUnidad(String unidad) {
        this.unidad = unidad;
    }

    public String getDesde() {
        return desde;
    }

    public void setDesde(String desde) {
        this.desde = desde;
    }

    public String getHasta() {
        return hasta;
    }

    public void setHasta(String hasta) {
        this.hasta = hasta;
    }

    public float getPorcada() {
        return porcada;
    }

    public void setPorcada(float porcada) {
        this.porcada = porcada;
    }
}
