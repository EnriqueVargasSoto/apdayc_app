package com.expediodigital.ventas360.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Kevin Robinson Meza Hinostroza on octubre 2017.
 * Expedio Digital
 * Kevin.Meza@expediodigital.com
 */

public class DirectionApiResponse {
    List<DirectionApiModel> listaPuntos = new ArrayList<>();
    List<Integer> listaOrden = new ArrayList<>();

    public List<DirectionApiModel> getListaPuntos() {
        return listaPuntos;
    }

    public void setListaPuntos(List<DirectionApiModel> listaPuntos) {
        this.listaPuntos = listaPuntos;
    }

    public List<Integer> getListaOrden() {
        return listaOrden;
    }

    public void setListaOrden(List<Integer> listaOrden) {
        this.listaOrden = listaOrden;
    }
}
