package com.expediodigital.ventas360.DTO;

/**
 * Created by Kevin Robinson Meza Hinostroza on agosto 2017.
 * Expedio Digital
 * Kevin.Meza@expediodigital.com
 */

public class DTODevolucionDetalle {
    private String idEmpresa;
    private String idSucursal;
    private String numeroGuia;
    private String idProducto;
    private String idUnidadMayor;
    private int cantidadUnidadMayor;
    private String idUnidadMenor;
    private int cantidadUnidadMenor;

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

    public String getNumeroGuia() {
        return numeroGuia;
    }

    public void setNumeroGuia(String numeroGuia) {
        this.numeroGuia = numeroGuia;
    }

    public String getIdProducto() {
        return idProducto;
    }

    public void setIdProducto(String idProducto) {
        this.idProducto = idProducto;
    }

    public String getIdUnidadMayor() {
        return idUnidadMayor;
    }

    public void setIdUnidadMayor(String idUnidadMayor) {
        this.idUnidadMayor = idUnidadMayor;
    }

    public int getCantidadUnidadMayor() {
        return cantidadUnidadMayor;
    }

    public void setCantidadUnidadMayor(int cantidadUnidadMayor) {
        this.cantidadUnidadMayor = cantidadUnidadMayor;
    }

    public String getIdUnidadMenor() {
        return idUnidadMenor;
    }

    public void setIdUnidadMenor(String idUnidadMenor) {
        this.idUnidadMenor = idUnidadMenor;
    }

    public int getCantidadUnidadMenor() {
        return cantidadUnidadMenor;
    }

    public void setCantidadUnidadMenor(int cantidadUnidadMenor) {
        this.cantidadUnidadMenor = cantidadUnidadMenor;
    }
}
