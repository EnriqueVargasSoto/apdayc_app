package com.expediodigital.ventas360.model;

/**
 * Created by Kevin Robin Meza Hinostroza on 3/05/2018.
 * Expedio Digital
 * kevin.meza@expediodigital.com
 */

public class ClienteRegistro {
    private String idClienteTemp;
    private String nombres;
    private String apellidoPaterno;
    private String apellidoMaterno;
    private String rucDni;
    private String telefono;
    private String direccion;
    private String distrito;
    private String idSubGiro;
    private String idRuta;
    private String idModulo;

    private String idVendedor;
    private String latitud;
    private String longitud;
    private String fechaRegistro;

    public String getIdClienteTemp() {
        return idClienteTemp;
    }

    public void setIdClienteTemp(String idClienteTemp) {
        this.idClienteTemp = idClienteTemp;
    }

    public String getNombres() {
        return nombres;
    }

    public void setNombres(String nombres) {
        this.nombres = nombres;
    }

    public String getApellidoPaterno() {
        return apellidoPaterno;
    }

    public void setApellidoPaterno(String apellidoPaterno) {
        this.apellidoPaterno = apellidoPaterno;
    }

    public String getApellidoMaterno() {
        return apellidoMaterno;
    }

    public void setApellidoMaterno(String apellidoMaterno) {
        this.apellidoMaterno = apellidoMaterno;
    }

    public String getRucDni() {
        return rucDni;
    }

    public void setRucDni(String rucDni) {
        this.rucDni = rucDni;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getDistrito() {
        return distrito;
    }

    public void setDistrito(String distrito) {
        this.distrito = distrito;
    }

    public String getIdSubGiro() {
        return idSubGiro;
    }

    public void setIdSubGiro(String idSubGiro) {
        this.idSubGiro = idSubGiro;
    }

    public String getIdRuta() {
        return idRuta;
    }

    public void setIdRuta(String idRuta) {
        this.idRuta = idRuta;
    }

    public String getIdModulo() {
        return idModulo;
    }

    public void setIdModulo(String idModulo) {
        this.idModulo = idModulo;
    }

    public String getIdVendedor() {
        return idVendedor;
    }

    public void setIdVendedor(String idVendedor) {
        this.idVendedor = idVendedor;
    }

    public String getLatitud() {
        return latitud;
    }

    public void setLatitud(String latitud) {
        this.latitud = latitud;
    }

    public String getLongitud() {
        return longitud;
    }

    public void setLongitud(String longitud) {
        this.longitud = longitud;
    }

    public String getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(String fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }
}
