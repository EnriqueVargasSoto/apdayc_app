package com.expediodigital.ventas360.DTO;

import java.io.Serializable;

public class DTOMotivoBaja implements Serializable {
    String idEmpresa;
    String idMotivoBaja;
    String descripcion;

    public String getIdEmpresa() {
        return idEmpresa;
    }

    public void setIdEmpresa(String idEmpresa) {
        this.idEmpresa = idEmpresa;
    }

    public String getIdMotivoBaja() {
        return idMotivoBaja;
    }

    public void setIdMotivoBaja(String idMotivoBaja) {
        this.idMotivoBaja = idMotivoBaja;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
}
