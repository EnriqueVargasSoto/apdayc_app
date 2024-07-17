package com.expediodigital.ventas360.model;

import android.view.View;

import java.util.ArrayList;

/**
 * Created by Kevin Robinson Meza Hinostroza on noviembre 2017.
 * Expedio Digital
 * Kevin.Meza@expediodigital.com
 */

public class EncuestaDetallePreguntaModel {
    public static final String TIPO_RESPUESTA_UNICA = "U";
    public static final String TIPO_RESPUESTA_MULTIPLE = "M";
    public static final String TIPO_RESPUESTA_LIBRE = "L";
    public static final String TIPO_RESPUESTA_FOTO = "F";
    public static final int ENCUESTA_REQUERIDA = 1;
    public static final int ENCUESTA_NO_REQUERIDA = 0;

    /*private int idEncuesta;
    private int idEncuestaDetalle;*/
    private int idPregunta;
    private String pregunta;
    private int ordenPregunta;
    private String tipoRespuesta;
    private int requerido;
    private View view;

    private ArrayList<EncuestaAlternativaModel> listaAlternativas = new ArrayList<>();

    public int getIdPregunta() {
        return idPregunta;
    }

    public void setIdPregunta(int idPregunta) {
        this.idPregunta = idPregunta;
    }

    public String getPregunta() {
        return pregunta;
    }

    public void setPregunta(String pregunta) {
        this.pregunta = pregunta;
    }

    public String getTipoRespuesta() {
        return tipoRespuesta;
    }

    public void setTipoRespuesta(String tipoRespuesta) {
        this.tipoRespuesta = tipoRespuesta;
    }

    public int getOrdenPregunta() {
        return ordenPregunta;
    }

    public void setOrdenPregunta(int ordenPregunta) {
        this.ordenPregunta = ordenPregunta;
    }

    public ArrayList<EncuestaAlternativaModel> getListaAlternativas() {
        return listaAlternativas;
    }

    public void setListaAlternativas(ArrayList<EncuestaAlternativaModel> listaAlternativas) {
        this.listaAlternativas = listaAlternativas;
    }

    public int getRequerido() {
        return requerido;
    }

    public void setRequerido(int requerido) {
        this.requerido = requerido;
    }

    public View getView() {
        return view;
    }

    public void setView(View view) {
        this.view = view;
    }
}
