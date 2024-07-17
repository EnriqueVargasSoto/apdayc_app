package com.expediodigital.ventas360.DTO;

import java.util.ArrayList;

/**
 * Created by Kevin Robinson Meza Hinostroza on julio 2017.
 * Expedio Digital
 * Kevin.Meza@expediodigital.com
 */

public class DTOPedido {
    private String idEmpresa;
    private String idSucursal;
    private String idAlmacen;
    private String numeroGuia;
    private String numeroPedido;
    private String idCliente;
    private String idVendedor;
    private String fechaPedido;
    private String fechaEntrega;
    private String idFormaPago;
    private String observacion;
    private double pesoTotal;
    private double importeTotal;
    private String idMotivoNoVenta;
    private String estado;
    private String flag;
    private double latitud;
    private double longitud;
    private double latitudDocumento;
    private double longitudDocumento;
    private int porcentajeBateria;
    private String horaFin;
    private String horaModificacion;
    private int pedidoEntregado;
    private String fechaEntregado;
    private String fechaModificacion;

    public String getFechaModificado() {
        return fechaModificacion;
    }

    public void setFechaModificado(String fechaModificado) {
        this.fechaModificacion = fechaModificado;
    }

    public String getHoraFin() {
        return horaFin;
    }

    public void setHoraFin(String horaFin) {
        this.horaFin = horaFin;
    }

    public String getHoraModificacion() {
        return horaModificacion;
    }

    public void setHoraModificacion(String horaModificacion) {
        this.horaModificacion = horaModificacion;
    }

    private ArrayList<DTOPedidoDetalle> detalles;

    public String getNumeroPedido() {
        return numeroPedido;
    }

    public void setNumeroPedido(String numeroPedido) {
        this.numeroPedido = numeroPedido;
    }

    public String getIdCliente() {
        return idCliente;
    }

    public void setIdCliente(String idCliente) {
        this.idCliente = idCliente;
    }

    public String getIdVendedor() {
        return idVendedor;
    }

    public void setIdVendedor(String idVendedor) {
        this.idVendedor = idVendedor;
    }

    public String getFechaPedido() {
        return fechaPedido;
    }

    public void setFechaPedido(String fechaPedido) {
        this.fechaPedido = fechaPedido;
    }

    public String getFechaEntrega() {
        return fechaEntrega;
    }

    public void setFechaEntrega(String fechaEntrega) {
        this.fechaEntrega = fechaEntrega;
    }

    public String getIdFormaPago() {
        return idFormaPago;
    }

    public void setIdFormaPago(String idFormaPago) {
        this.idFormaPago = idFormaPago;
    }

    public String getObservacion() {
        return observacion;
    }

    public void setObservacion(String observacion) {
        this.observacion = observacion;
    }

    public double getPesoTotal() {
        return pesoTotal;
    }

    public void setPesoTotal(double pesoTotal) {
        this.pesoTotal = pesoTotal;
    }

    public double getImporteTotal() {
        return importeTotal;
    }

    public void setImporteTotal(double importeTotal) {
        this.importeTotal = importeTotal;
    }

    public String getIdMotivoNoVenta() {
        return idMotivoNoVenta;
    }

    public void setIdMotivoNoVenta(String idMotivoNoVenta) {
        this.idMotivoNoVenta = idMotivoNoVenta;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getFlag() {
        return flag;
    }

    public void setFlag(String flag) {
        this.flag = flag;
    }

    public ArrayList<DTOPedidoDetalle> getDetalles() {
        return detalles;
    }

    public void setDetalles(ArrayList<DTOPedidoDetalle> detalles) {
        this.detalles = detalles;
    }

    public String getIdEmpresa() {
        return idEmpresa;
    }

    public void setIdEmpresa(String idEmpresa) {
        this.idEmpresa = idEmpresa;
    }

    public String getIdSucursal() {
        return idSucursal;
    }

    public void setIdSucursal(String idSucursal) {
        this.idSucursal = idSucursal;
    }

    public String getIdAlmacen() {
        return idAlmacen;
    }

    public void setIdAlmacen(String idAlmacen) {
        this.idAlmacen = idAlmacen;
    }

    public String getNumeroGuia() {
        return numeroGuia;
    }

    public void setNumeroGuia(String numeroGuia) {
        this.numeroGuia = numeroGuia;
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

    public double getLatitudDocumento() {
        return latitudDocumento;
    }

    public void setLatitudDocumento(double latitudDocumento) {
        this.latitudDocumento = latitudDocumento;
    }

    public double getLongitudDocumento() {
        return longitudDocumento;
    }

    public void setLongitudDocumento(double longitudDocumento) {
        this.longitudDocumento = longitudDocumento;
    }

    public int getPorcentajeBateria() {
        return porcentajeBateria;
    }

    public void setPorcentajeBateria(int porcentajeBateria) {
        this.porcentajeBateria = porcentajeBateria;
    }

    public int getPedidoEntregado() {
        return pedidoEntregado;
    }

    public void setPedidoEntregado(int pedidoEntregado) {
        this.pedidoEntregado = pedidoEntregado;
    }

    public String getFechaEntregado() {
        return fechaEntregado;
    }

    public void setFechaEntregado(String fechaEntregado) {
        this.fechaEntregado = fechaEntregado;
    }

}
