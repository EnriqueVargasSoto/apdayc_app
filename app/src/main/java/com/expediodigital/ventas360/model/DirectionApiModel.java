package com.expediodigital.ventas360.model;

import com.google.android.gms.maps.model.LatLng;

import java.util.List;

/**
 * Created by Kevin Robinson Meza Hinostroza on septiembre 2017.
 * Expedio Digital
 * Kevin.Meza@expediodigital.com
 */

/**
 * Esta clase contiene los atributos de un solo punto, el recorrido a mostrar será una lista de DirectionApiModel
 */
public class DirectionApiModel {
    private String idCliente;
    private int distance;//metros
    private int duration;//segundos
    private List<LatLng> steps;//pasos para llegar al siguiente punto
    private LatLng startLocation;//Punto de inicio
    private LatLng endLocation;//Punto de fin
    private int orden;//Orden del punto

    public String getIdCliente() {
        return idCliente;
    }

    public void setIdCliente(String idCliente) {
        this.idCliente = idCliente;
    }

    public int getDistance() {
        return distance;
    }

    public void setDistance(int distance) {
        this.distance = distance;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public List<LatLng> getSteps() {
        return steps;
    }

    public void setSteps(List<LatLng> steps) {
        this.steps = steps;
    }

    public LatLng getStartLocation() {
        return startLocation;
    }

    public void setStartLocation(LatLng startLocation) {
        this.startLocation = startLocation;
    }

    public LatLng getEndLocation() {
        return endLocation;
    }

    public void setEndLocation(LatLng endLocation) {
        this.endLocation = endLocation;
    }

    public int getOrden() {
        return orden;
    }

    public void setOrden(int orden) {
        this.orden = orden;
    }
}
