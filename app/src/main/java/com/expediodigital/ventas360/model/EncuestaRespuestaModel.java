package com.expediodigital.ventas360.model;

import android.graphics.Bitmap;
import android.view.View;

import com.google.gson.annotations.Expose;

import java.util.List;

/**
 * Created by Kevin Robinson Meza Hinostroza on noviembre 2017.
 * Expedio Digital
 * Kevin.Meza@expediodigital.com
 */

public class EncuestaRespuestaModel {
    public static final String FLAG_ENVIADO = "E";
    public static final String FLAG_PENDIENTE = "P";
    public static final String FLAG_INCOMPLETO = "I";

    @Expose private int idEncuesta;
    @Expose private int idEncuestaDetalle;
    @Expose private String idCliente;
    @Expose private String idVendedor;
    @Expose private String fecha;
    @Expose private String flag;
    @Expose private List<EncuestaRespuestaDetalleModel> detalle;

    public int getIdEncuesta() {
        return idEncuesta;
    }

    public void setIdEncuesta(int idEncuesta) {
        this.idEncuesta = idEncuesta;
    }

    public int getIdEncuestaDetalle() {
        return idEncuestaDetalle;
    }

    public void setIdEncuestaDetalle(int idEncuestaDetalle) {
        this.idEncuestaDetalle = idEncuestaDetalle;
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

    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    public String getFlag() {
        return flag;
    }

    public void setFlag(String flag) {
        this.flag = flag;
    }

    public List<EncuestaRespuestaDetalleModel> getDetalle() {
        return detalle;
    }

    public void setDetalle(List<EncuestaRespuestaDetalleModel> detalle) {
        this.detalle = detalle;
    }
}
