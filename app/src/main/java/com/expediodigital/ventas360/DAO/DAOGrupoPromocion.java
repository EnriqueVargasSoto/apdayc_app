package com.expediodigital.ventas360.DAO;

public class DAOGrupoPromocion {
    public int IDGRUPO;
    public int ARTICULO;
    public int MANDATORIO;
    public int UNIDADES;
    public String MALLA;

    public int getIDGRUPO() {
        return IDGRUPO;
    }

    public void setIDGRUPO(int IDGRUPO) {
        this.IDGRUPO = IDGRUPO;
    }

    public int getARTICULO() {
        return ARTICULO;
    }

    public void setARTICULO(int ARTICULO) {
        this.ARTICULO = ARTICULO;
    }

    public int getMANDATORIO() {
        return MANDATORIO;
    }

    public void setMANDATORIO(int MANDATORIO) {
        this.MANDATORIO = MANDATORIO;
    }

    public int getUNIDADES() {
        return UNIDADES;
    }

    public void setUNIDADES(int UNIDADES) {
        this.UNIDADES = UNIDADES;
    }

    public String getMALLA() {
        return MALLA;
    }

    public void setMALLA(String MALLA) {
        this.MALLA = MALLA;
    }
}
