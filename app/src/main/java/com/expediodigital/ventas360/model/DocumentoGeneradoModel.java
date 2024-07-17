package com.expediodigital.ventas360.model;

/**
 * Created by Kevin Robinson Meza Hinostroza on marzo 2018.
 * Expedio Digital
 * Kevin.Meza@expediodigital.com
 */

public class DocumentoGeneradoModel {
    private String numeroPedido;
    private String numeroGuia;
    private String serieDocumento;
    private String numeroDocumento;
    private String PDFBase64;
    private String error;

    public DocumentoGeneradoModel() {
        this.numeroPedido = "";
        this.numeroGuia = "";
        this.serieDocumento = "";
        this.numeroDocumento = "";
        this.PDFBase64 = "";
        this.error = "";
    }

    public String getNumeroPedido() {
        return numeroPedido;
    }

    public void setNumeroPedido(String numeroPedido) {
        this.numeroPedido = numeroPedido;
    }

    public String getNumeroGuia() {
        return numeroGuia;
    }

    public void setNumeroGuia(String numeroGuia) {
        this.numeroGuia = numeroGuia;
    }

    public String getSerieDocumento() {
        return serieDocumento;
    }

    public void setSerieDocumento(String serieDocumento) {
        this.serieDocumento = serieDocumento;
    }

    public String getNumeroDocumento() {
        return numeroDocumento;
    }

    public void setNumeroDocumento(String numeroDocumento) {
        this.numeroDocumento = numeroDocumento;
    }

    public String getPDFBase64() {
        return PDFBase64;
    }

    public void setPDFBase64(String PDFBase64) {
        this.PDFBase64 = PDFBase64;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
}
