package com.expediodigital.ventas360.util;

public class GrupoPromocionBon {
    int idGrupo;
    int idPromocion;

    public int getIdGrupo() {
        return idGrupo;
    }

    public void setIdGrupo(int idGrupo) {
        this.idGrupo = idGrupo;
    }

    public int getIdPromocion() {
        return idPromocion;
    }

    public void setIdPromocion(int idPromocion) {
        this.idPromocion = idPromocion;
    }

    public GrupoPromocionBon(int idGrupo, int idPromocion) {
        this.idGrupo = idGrupo;
        this.idPromocion = idPromocion;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GrupoPromocionBon other = (GrupoPromocionBon) o;
        if(getIdGrupo() == other.getIdGrupo() && getIdPromocion() == other.getIdPromocion()){
            return true;
        }
        else{
            return false;
        }
    }
    @Override
    public int hashCode() {
        int result = idGrupo*10000000 + idPromocion;
        return result;
    }


}
