package com.expediodigital.ventas360.model;

/**
 * Created by Kevin Robinson Meza Hinostroza on julio 2017.
 * Expedio Digital
 * Meza.Hinostroza.Robin@gmail.com
 */

public class PedidoCabeceraModel {
    public static final String ESTADO_ANULADO = "A";
    public static final String ESTADO_GENERADO = "G";
    public static final String ESTADO_FACTURADO = "F";
    public static final String FLAG_PENDIENTE = "P";
    public static final String FLAG_ENVIADO = "E";
    public static final String ID_MOTIVO_NO_COMPRA_DEFAULT = "8";
    //public static String FLAG_TRANSFERIDO = "T";

    private String idEmpresa;
    private String idSucursal;
    private String idAlmacen;
    private String numeroPedido;
    private String idCliente;
    private String idVendedor;
    private String fechaPedido;
    private String fechaEntrega;
    private String idFormaPago;
    private String observacion = "";
    private double pesoTotal = 0;
    private double importeTotal = 0;
    private String idMotivoNoVenta = "0";
    private String estado;
    private String flag;
    private String numeroDocumento = "";
    private String serieDocumento = "";
    private double latitud = 0.0;
    private double longitud = 0.0;
    private double latitudDocumento = 0.0;
    private double longitudDocumento = 0.0;
    private int porcentajeBateria;
    private String horaFin;
    private String horaModificacion = "";
    private int pedidoEntregado = 0;
    private String fechaEntregado = "";

    private String nombreCliente;
    private String formaPago;
    private String motivoNoVenta;
    private String direccion;
    private String direccionFiscal;
    private String rucDni;

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

    public String getNombreCliente() {
        return nombreCliente;
    }

    public void setNombreCliente(String nombreCliente) {
        this.nombreCliente = nombreCliente;
    }

    public String getFormaPago() {
        return formaPago;
    }

    public void setFormaPago(String formaPago) {
        this.formaPago = formaPago;
    }

    public String getFlag() {
        return flag;
    }

    public void setFlag(String flag) {
        this.flag = flag;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getMotivoNoVenta() {
        return motivoNoVenta;
    }

    public void setMotivoNoVenta(String motivoNoVenta) {
        this.motivoNoVenta = motivoNoVenta;
    }

    public String getFechaEntrega() {
        return fechaEntrega;
    }

    public void setFechaEntrega(String fechaEntrega) {
        this.fechaEntrega = fechaEntrega;
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

    public String getNumeroDocumento() {
        return numeroDocumento;
    }

    public void setNumeroDocumento(String numeroDocumento) {
        this.numeroDocumento = numeroDocumento;
    }

    public String getSerieDocumento() {
        return serieDocumento;
    }

    public void setSerieDocumento(String serieDocumento) {
        this.serieDocumento = serieDocumento;
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

    /**
     * @return Devuelve 1 si el pedido se ha entregado al cliente final, luego de ser facturado
     */
    public int getPedidoEntregado() {
        return pedidoEntregado;
    }
    /**
     * @param pedidoEntregado valor 1 para indicar que el pedido ha sido entregado al cliente final luego de ser facturado, valor 0 de lo contrario.
     */
    public void setPedidoEntregado(int pedidoEntregado) {
        this.pedidoEntregado = pedidoEntregado;
    }

    public String getFechaEntregado() {
        return fechaEntregado;
    }

    public void setFechaEntregado(String fechaEntregado) {
        this.fechaEntregado = fechaEntregado;
    }

    public String getRucDni() {
        return rucDni;
    }

    public void setRucDni(String rucDni) {
        this.rucDni = rucDni;
    }
}
