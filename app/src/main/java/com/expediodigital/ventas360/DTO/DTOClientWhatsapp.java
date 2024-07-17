package com.expediodigital.ventas360.DTO;

/**
 * Created by Kevin Robinson Meza Hinostroza on julio 2017.
 * Expedio Digital
 * Kevin.Meza@expediodigital.com
 */

public class DTOClientWhatsapp {
    private String idEmpresa;
    private String idSucursal;
    private String idCliente;
    private String whathsapp;
    private String codigociudad;
    private String telefonofijo;
    private String email;
    private String fechaRegistro;

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

    public String getIdCliente() {
        return idCliente;
    }

    public void setIdCliente(String idCliente) {
        this.idCliente = idCliente;
    }

    public String getWhathsapp() {
        return whathsapp;
    }

    public void setWhathsapp(String whathsapp) {
        this.whathsapp = whathsapp;
    }

    public String getCodigociudad() {
        return codigociudad;
    }

    public void setCodigociudad(String codigociudad) {
        this.codigociudad = codigociudad;
    }

    public String getTelefonofijo() {
        return telefonofijo;
    }

    public void setTelefonofijo(String telefonofijo) {
        this.telefonofijo = telefonofijo;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(String fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }
}
