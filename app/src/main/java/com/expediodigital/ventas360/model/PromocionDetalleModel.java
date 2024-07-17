package com.expediodigital.ventas360.model;

/**
 * Created by Kevin Robinson Meza Hinostroza on julio 2017.
 * Expedio Digital
 * Kevin.Meza@expediodigital.com
 */

public class PromocionDetalleModel {
    public static final String TIPO_PROMOCION_CANTIDAD = "C";
    public static final String TIPO_PROMOCION_MONTO = "D";

    public static final int TIPO_CONDICION_MAYOR_IGUAL = 1;
    public static final int TIPO_CONDICION_POR_CADA = 3;

    public static final int TIPO_ACUMULADO_PURO_COMPUESTO = 1;
    public static final int TIPO_ACUMULADO_MULTIPLE = 2;

    private int idPromocion;
    private String promocion;
    private String tipoPromocion;
    private int item;
    private int totalAgrupado;
    private int agrupado;
    private String entrada;
    private int tipoCondicion;
    private double montoCondicion;
    private int cantidadCondicion;
    private String salida;
    private int cantidadBonificada;

    private double montoLimite;
    /*Es el tope de productos de entrada (monto o cantidad), si se pasa de ese tope, ya no se aplica la promocion.
    Esto debe ser usado principalmente para poner rangos de promociones donde se sabe que si se superó habrá otra promocion que continúe con el rango.
    Y si una promocion no tiene rangos, no debería indicarse una cantidad en este campo*/
    private int cantidadLimite;

    private int maximaBonificacion;//Es la maxima cantidad de productos que se bonificará en la promocion. (Para poner un límite de productos a bonificar y no se lleven todo)
    private int acumulado;

    private int porCliente;
    private int porVendedor;
    private int porPoliticaPrecio;

    private int evaluarEnUnidadMayor;
    private String fechaInicio;/*Es la fecha desde cuando se generan las promociones, si un pedido ha sido generado sin promociones debe permanecer así siempre que su fecha sea menor a la fechaInicio de la promoción */

    public int getIdPromocion() {
        return idPromocion;
    }

    public void setIdPromocion(int idPromocion) {
        this.idPromocion = idPromocion;
    }

    public String getPromocion() {
        return promocion;
    }

    public void setPromocion(String promocion) {
        this.promocion = promocion;
    }

    public String getTipoPromocion() {
        return tipoPromocion;
    }

    public void setTipoPromocion(String tipoPromocion) {
        this.tipoPromocion = tipoPromocion;
    }

    public int getItem() {
        return item;
    }

    public void setItem(int item) {
        this.item = item;
    }

    public int getTotalAgrupado() {
        return totalAgrupado;
    }

    public void setTotalAgrupado(int totalAgrupado) {
        this.totalAgrupado = totalAgrupado;
    }

    public int getAgrupado() {
        return agrupado;
    }

    public void setAgrupado(int agrupado) {
        this.agrupado = agrupado;
    }

    public String getEntrada() {
        return entrada;
    }

    public void setEntrada(String entrada) {
        this.entrada = entrada;
    }

    public int getTipoCondicion() {
        return tipoCondicion;
    }

    public void setTipoCondicion(int tipoCondicion) {
        this.tipoCondicion = tipoCondicion;
    }

    public double getMontoCondicion() {
        return montoCondicion;
    }

    public void setMontoCondicion(double montoCondicion) {
        this.montoCondicion = montoCondicion;
    }

    public int getCantidadCondicion() {
        return cantidadCondicion;
    }

    public void setCantidadCondicion(int cantidadCondicion) {
        this.cantidadCondicion = cantidadCondicion;
    }

    public String getSalida() {
        return salida;
    }

    public void setSalida(String salida) {
        this.salida = salida;
    }

    public int getCantidadBonificada() {
        return cantidadBonificada;
    }

    public void setCantidadBonificada(int cantidadBonificada) {
        this.cantidadBonificada = cantidadBonificada;
    }

    public double getMontoLimite() {
        return montoLimite;
    }

    public void setMontoLimite(double montoLimite) {
        this.montoLimite = montoLimite;
    }

    public int getCantidadLimite() {
        return cantidadLimite;
    }

    public void setCantidadLimite(int cantidadLimite) {
        this.cantidadLimite = cantidadLimite;
    }

    public int getMaximaBonificacion() {
        return maximaBonificacion;
    }

    public void setMaximaBonificacion(int maximaBonificacion) {
        this.maximaBonificacion = maximaBonificacion;
    }

    public int getAcumulado() {
        return acumulado;
    }

    public void setAcumulado(int acumulado) {
        this.acumulado = acumulado;
    }

    public int getPorCliente() {
        return porCliente;
    }

    public void setPorCliente(int porCliente) {
        this.porCliente = porCliente;
    }

    public int getPorVendedor() {
        return porVendedor;
    }

    public void setPorVendedor(int porVendedor) {
        this.porVendedor = porVendedor;
    }

    public int getPorPoliticaPrecio() {
        return porPoliticaPrecio;
    }

    public void setPorPoliticaPrecio(int porPoliticaPrecio) {
        this.porPoliticaPrecio = porPoliticaPrecio;
    }

    public int getEvaluarEnUnidadMayor() {
        return evaluarEnUnidadMayor;
    }

    public void setEvaluarEnUnidadMayor(int evaluarEnUnidadMayor) {
        this.evaluarEnUnidadMayor = evaluarEnUnidadMayor;
    }

    public String getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(String fechaInicio) {
        this.fechaInicio = fechaInicio;
    }
}
