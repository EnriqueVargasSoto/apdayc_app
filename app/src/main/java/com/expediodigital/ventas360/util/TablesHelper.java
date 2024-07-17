package com.expediodigital.ventas360.util;

/**
 * Created by Kevin Robinson Meza Hinostroza on julio 2017.
 * Expedio Digital
 * Meza.Hinostroza.Robin@gmail.com
 */

public class TablesHelper {

    public static final class PedidoCabecera {
        public static final String Table = "PedidoCabecera";

        public static final String PKeyName = "numeroPedido";
        public static final String FKCliente = "idCliente";
        public static final String FKVendedor = "idVendedor";
        public static final String FechaPedido = "fechaPedido";
        public static final String FechaEntrega = "fechaEntrega";
        public static final String FKFormaPago = "idFormaPago";
        public static final String Observacion = "observacion";
        public static final String PesoTotal = "pesoTotal";
        public static final String ImporteTotal = "importeTotal";
        public static final String FKMotivoNoVenta = "idMotivoNoVenta";
        public static final String Estado = "estado";
        public static final String Flag = "flag";
        public static final String SerieDocumento = "serieDocumento";
        public static final String NumeroDocumento = "numeroDocumento";
        public static final String Latitud = "latitud";
        public static final String Longitud = "longitud";
        public static final String LatitudDocumento = "latitudDocumento";
        public static final String LongitudDocumento = "longitudDocumento";
        public static final String PorcentajeBateria = "porcentajeBateria";
        public static final String HoraFin = "horaFin";
        public static final String FechaModificacion = "fechaModificacion";
        public static final String PedidoEntregado = "pedidoEntregado";
        public static final String FechaEntregado = "fechaEntregado";
    }

    public static final class PedidoDetalle {
        public static final String Table = "PedidoDetalle";

        public static final String PKeyPedido = "numeroPedido";
        public static final String PKeyProducto = "idProducto";
        public static final String FKPoliticaPrecio = "idPoliticaPrecio";
        public static final String TipoProducto = "tipoProducto";
        public static final String PrecioBruto = "precioBruto";
        public static final String Cantidad = "cantidad";
        public static final String PrecioNeto = "precioNeto";
        public static final String FKUnidadMedida = "idUnidadMedida";
        public static final String PesoNeto = "pesoNeto";
        public static final String Item = "item";
        public static final String SinStock = "sinStock";
        public static final String Percepcion = "percepcion";
        public static final String ISC = "ISC";
        public static final String Malla = "malla";
        public static final String EstadoDetalle = "estadoDetalle";
        /*
        * Métodos SOAP
        */
    }

    public static final class Cliente {
        public static final String Table = "Cliente";

        public static final String PKeyName = "idCliente";
        public static final String RucDni = "rucDni";
        public static final String RazonSocial = "razonSocial";
        public static final String Correo = "correo";
        public static final String Direccion = "direccion";
        public static final String DireccionFiscal = "direccionFiscal";
        public static final String FKModulo = "idModulo";
        public static final String Latitud = "latitud";
        public static final String Longitud = "longitud";
        public static final String Orden = "orden";
        public static final String FKSegmento = "idSegmento";
        public static final String FKCluster = "idCluster";
        public static final String LimiteCredito = "limiteCredito";
        public static final String FKSubGiro = "idSubGiro";
        public static final String FKGiro = "idGiro";
        public static final String FKCanalVentas = "idCanalVentas";
        public static final String FKOcasionConsumo = "idOcasionConsumo";
        public static final String AfectoPercepcion = "afectoPercepcion";
        public static final String NroExhibidores = "nroExhibidores";
        public static final String NroPuertasFrio = "nroPuertasFrio";
        /**
         * Métodos SOAP
         */
        public static final String Sincronizar = "obtenerClientesxVendedor_json";
        public static final String EnviarEncuesta = "UploadFile";
    }

    public static final class Producto {
        public static final String Table = "Producto";

        public static final String PKeyName = "idProducto";
        public static final String Descripcion = "descripcion";
        public static final String FKLinea = "idLinea";
        public static final String FKFamilia = "idFamilia";
        public static final String Peso = "peso";
        public static final String FKProveedor = "idProveedor";
        public static final String FKProductoERP = "idProductoERP";
        public static final String DescripcionERP = "descripcionERP";
        public static final String TipoProducto = "tipoProducto";
        public static final String FKMarca = "idMarca";
        public static final String PorcentajePercepcion = "porcentajePercepcion";
        public static final String PorcentajeISC = "porcentajeISC";
        /**
         * Métodos SOAP
         */
        public static final String Sincronizar = "obtenerProductosxVendedor_json";
        public static final String ObtenerStockLinea = "obtenerStockxProducto_json";
    }


    public static final class Kardex{
        public static final String Table = "Kardex";

        public static final String FKProducto = "idProducto";
        public static final String stockInicial = "stockInicial";
        public static final String stockPedido = "stockPedido";
        public static final String stockDespachado = "stockDespachado";
        /**
         * Métodos SOAP
         */
        public static final String Sincronizar = "obtenerKardexVendedor_json";
    }

    public static final class Empresa {
        public static final String Table = "Empresa";
        public static final String PKName = "idEmpresa";
        public static final String Ruc = "ruc";
        public static final String RazonSocial = "razonSocial";
        /**
         * Métodos SOAP
         */
        public static final String Sincronizar = "obtenerEmpresas_json";
    }

    public static final class Usuario {
        public static final String Table = "Usuario";

        public static final String PKeyName = "idUsuario";
        public static final String Usuario = "usuario";
        public static final String Clave = "clave";
        public static final String FKEmpresa = "idEmpresa";
        public static final String FKSucursal = "idSucursal";
        /**
         * Métodos SOAP
         */
        public static final String Sincronizar = "obtenerUsuarios_json";
    }

    public static final class Vendedor {
        public static final String Table = "Vendedor";

        public static final String PKeyName = "idVendedor";
        public static final String FKEmpresa = "idEmpresa";
        public static final String FKSucursal = "idSucursal";
        public static final String FKUsuario = "idUsuario";
        public static final String Nombre = "nombre";
        public static final String Tipo = "tipo";
        public static final String Serie = "serie";
        public static final String FKRuta = "idRuta";
        public static final String FKAlmacen = "idAlmacen";
        public static final String ModoVenta = "modoVenta";
        public static final String Estado = "estado";
        /**
         * Métodos SOAP
         */
        public static final String Sincronizar = "obtenerVendedores_json";
        public static final String CerrarVentas = "cerrarVentasxVendedor";
        public static final String ActualizarFechaSincronizacion = "actualizarFechaSincronizacion";
    }

    public static final class ObjPedido{

        public static final String Table = "ObjPedido";
        /*
        * Métodos SOAP
        */
        public static final String Sincronizar = "obtenerObjpedidosxVendedor_json";
        public static final String ActualizarObjPedido = "actualizarObjpedidoxVendedor_json";
        public static final String ObtenerEstado = "obtenerEstadoPedido";
        public static final String FacturarPedido = "facturarObjpedidoxVendedor_json";
        public static final String ActualizarEntregaPedidos = "actualizarEntregaPedidos";
        public static final String ActualizarFormaPago = "actualizarFormaPago";
        public static final String ObtenerDocumentoGenerado = "obtenerDocumentoGenerado_json";
    }

    public static final class Servicio {
        public static final String Table = "Servicio";
        public static final String PKName = "idServicio";
        public static final String Url = "url";
        public static final String Tipo = "tipo";
        /*
        * Métodos SOAP
        */
        public static final String Sincronizar = "obtenerServiciosxSucursal_json";
    }

    public static final class Configuracion {
        public static final String Table = "Configuracion";
        public static final String PKName = "identificador";
        public static final String Descripcion = "descripcion";

        public static final String Configuracion_correoSoporte = "correoSoporte";
        public static final String Fecha = "fecha";
        public static final String MaximoPedido = "maximoPedido";
        public static final String UnidadVentaMayor = "unidadVentaMayor";
        public static final String IdPoliticaMinorista = "idPoliticaMinorista";
        public static final String PreventaEnLinea = "preventaEnLinea";
        public static final String PorcentajeIGV = "porcentajeIGV";
        public static final String Direccion = "direccion";
        public static final String IdClienteGeneral = "idClienteGeneral";
        public static final String LimitePercepcion = "limitePercepcion";
        public static final String AfectoPercepcion = "afectoPercepcion";
        public static final String UrlTracking = "urlTracking";
        public static final String InfoVendedorCliente = "infoVendedorCliente";
        /*
        * Métodos SOAP
        */
        public static final String Sincronizar = "obtenerConfiguracionesxVendedor_json";
    }

    public static final class MotivoNoVenta {
        public static final String Table = "MotivoNoVenta";

        public static final String PKName = "idMotivoNoVenta";
        public static final String Descripcion = "descripcion";
        /*
        * Métodos SOAP
        */
        public static final String Sincronizar = "obtenerMotivosNoVentaxEmpresa_json";
    }

    public static final class PromocionDetalle {
        public static final String Table = "PromocionDetalle";

        public static final String PKeyName = "idPromocion";
        public static final String Promocion = "promocion";
        public static final String TipoPromocion = "tipoPromocion";
        public static final String Item = "item";
        public static final String TotalAgrupado = "totalAgrupado";
        public static final String Agrupado = "agrupado";
        public static final String Entrada = "entrada";
        public static final String TipoCondicion = "tipoCondicion";
        public static final String MontoCondicion = "montoCondicion";
        public static final String CantidadCondicion = "cantidadCondicion";
        public static final String Salida = "salida";
        public static final String CantidadBonificada = "cantidadBonificada";
        public static final String MontoLimite = "montoLimite";
        public static final String CantidadLimite = "cantidadLimite";
        public static final String MaximaBonificacion = "maximaBonificacion";
        public static final String Acumulado = "acumulado";
        public static final String PorCliente = "porCliente";
        public static final String PorVendedor = "porVendedor";
        public static final String PorPoliticaPrecio = "porPoliticaPrecio";
        public static final String EvaluarEnUnidadMayor = "evaluarEnUnidadMayor";
        public static final String MultiplicarPorCompra = "multiplicarPorCompra";
        public static final String FechaInicio = "fechaInicio";
        /*
        * Métodos SOAP
        */
        public static final String Sincronizar = "obtenerPromocionesxSucursal_json";
    }

    public static final class PromocionxCliente {
        public static final String Table = "PromocionxCliente";

        public static final String PKeyPromocion = "idPromocion";
        public static final String PKeyCliente = "idCliente";
        /*
        * Métodos SOAP
        */
        public static final String Sincronizar = "obtenerPromocionClientexSucursal_json";
    }

    public static final class PromocionxPoliticaPrecio {
        public static final String Table = "PromocionxPoliticaPrecio";

        public static final String PKeyPromocion = "idPromocion";
        public static final String PKeyPoliticaPrecio = "idPoliticaPrecio";
        /*
        * Métodos SOAP
        */
        public static final String Sincronizar = "obtenerPromocionPoliticaPrecioxSucursal_json";
    }

    public static final class PromocionxVendedor {
        public static final String Table = "PromocionxVendedor";

        public static final String PKeyPromocion = "idPromocion";
        public static final String PKeyVendedor = "idVendedor";
        /*
        * Métodos SOAP
        */
        public static final String Sincronizar = "obtenerPromocionVendedorxSucursal_json";
    }

    public static final class Linea {
        public static final String Table = "Linea";
        public static final String PKName = "idLinea";
        public static final String Descripcion = "descripcion";
        /*
        * Métodos SOAP
        */
        public static final String Sincronizar = "obtenerLineasxEmpresa_json";
    }

    public static final class FormaPago {
        public static final String Table = "FormaPago";
        public static final String PKName = "idFormaPago";
        public static final String Descripcion = "descripcion";
        /*
        * Métodos SOAP
        */
        public static final String Sincronizar = "obtenerFormasPagoxEmpresa_json";
    }

    public static final class UnidadMedida {
        public static final String Table = "UnidadMedida";
        public static final String PKName = "idUnidadMedida";
        public static final String Descripcion = "descripcion";
        /*
        * Métodos SOAP
        */
        public static final String Sincronizar = "obtenerUnidadesMedidasxEmpresa_json";
    }

    public static final class Familia {
        public static final String Table = "Familia";
        public static final String PKName = "idFamilia";
        public static final String Descripcion = "descripcion";
        /*
        * Métodos SOAP
        */
        public static final String Sincronizar = "obtenerFamiliasxEmpresa_json";
    }

    public static final class Proveedor {
        public static final String Table = "Proveedor";
        public static final String PKName = "idProveedor";
        public static final String RazonSocial = "razonSocial";
        /*
        * Métodos SOAP
        */
        public static final String Sincronizar = "obtenerProveedoresxEmpresa_json";
    }

    public static final class Guia {
        public static final String Table = "Guia";
        public static final String PKName = "numeroGuia";
        public static final String FechaCarga = "fechaCarga";
        public static final String FechaCierre = "fechaCierre";
        public static final String Estado = "estado";
        /*
        * Métodos SOAP
        */
        public static final String Sincronizar = "obtenerGuiasxVendedor_json";
        public static final String CerrarGuia = "cerrarGuiaxTransportista";
        public static final String AbrirGuia = "abrirGuiaxTransportista";
    }

    public static final class PoliticaPrecio {
        public static final String Table = "PoliticaPrecio";
        public static final String PKName = "idPoliticaPrecio";
        public static final String Descripcion = "descripcion";
        public static final String CantidadMinima = "cantidadMinima";
        /*
        * Métodos SOAP
        */
        public static final String Sincronizar = "obtenerPoliticasPrecioxSucursal_json";
    }


    public static final class UnidadMedidaxProducto{
        public static final String Table = "UnidadMedidaxProducto";

        public static final String FKEmpresa = "idEmpresa";
        public static final String FKProducto = "idProducto";
        public static final String FKUnidadManejo = "idUnidadManejo";
        public static final String FKUnidadContable = "idUnidadContable";
        public static final String Contenido = "contenido";

        /**
         * Métodos SOAP
         */
        public static final String Sincronizar = "obtenerUnidadMedidaxProducto_json";
        public static final String Sincronizar2 = "obtenerUnidadMedidaxProductoxVendedor_json";
    }

    public static final class PoliticaPrecioxProducto{
        public static final String Table = "PoliticaPrecioxProducto";

        public static final String FKEmpresa = "idEmpresa";
        public static final String FKPolitica = "idPolitica";
        public static final String FKProducto = "idProducto";
        public static final String FKUnidadManejo = "idUnidadManejo";
        public static final String FKUnidadContenido = "idUnidadContenido";
        public static final String PrecioManejo = "precioManejo";
        public static final String PrecioContenido = "precioContenido";
        /**
         * Métodos SOAP
         */
        public static final String Sincronizar = "obtenerPoliticaPrecioxProducto_json";
        public static final String Sincronizar2 = "obtenerPoliticasPrecioxProductoxVendedor_json";
    }
    /*
    public static final class PoliticaPrecioxProducto {
        public static final String Table = "PoliticaPrecioxProducto";
        public static final String FKPoliticaPrecio = "idPoliticaPrecio";
        public static final String FKProducto = "idProducto";
        public static final String PrecioMenor = "precioMenor";
        public static final String PrecioMayor = "precioMayor";


        public static final String Sincronizar = "obtenerPoliticasPrecioxProductoxVendedor_json";
    }*/

    public static final class PoliticaPrecioxCliente {
        public static final String Table = "PoliticaPrecioxCliente";
        public static final String FKPoliticaPrecio = "idPoliticaPrecio";
        public static final String FKCliente = "idCliente";
        /*
        * Métodos SOAP
        */
        public static final String Sincronizar = "obtenerPoliticasPrecioxClientexVendedor_json";
    }

    public static final class Liquidacion {
        public static final String Table = "Liquidacion";
        public static final String NumeroDocumento = "numeroDocumento";
        public static final String PKName = "idProducto";
        public static final String Descripcion = "descripcion";
        public static final String FactorConversion = "factorConversion";
        public static final String StockGuia = "stockGuia";
        public static final String StockVenta = "stockVenta";
        public static final String StockDevolucion = "stockDevolucion";
        public static final String Diferencia = "diferencia";
        /*
        * Métodos SOAP
        */
        public static final String Sincronizar = "obtenerLiquidacionGuiaxTransportista_json";
    }

    public static final class DevolucionCabecera {
        public static final String Table = "DevolucionCabecera";
        public static final String PKeyName = "numeroGuia";
        public static final String FKVendedor = "idVendedor";
        public static final String FechaDevolucion = "fechaDevolucion";
        public static final String Flag = "flag";
    }

    public static final class DevolucionDetalle {
        public static final String Table = "DevolucionDetalle";
        public static final String PKeyName = "numeroGuia";
        public static final String FKProducto = "idProducto";
        public static final String FKUnidadMayor = "idUnidadMayor";
        public static final String CantidadUnidadMayor = "cantidadUnidadMayor";
        public static final String FKUnidadMenor = "idUnidadMenor";
        public static final String CantidadUnidadMenor = "cantidadUnidadMenor";
        public static final String Modificado = "modificado";
        public static final String Flag = "flag";
    }

    public static final class ObjDevolucion{

        public static final String Table = "ObjDevolucion";
        /*
        * Métodos SOAP
        */
        public static final String Sincronizar = "obtenerObjdevolucionxVendedor_json";
        public static final String ActualizarObjDevolucion = "actualizarObjdevolucionxVendedor_json";
    }

    public static final class JSON{

        public static final String Table = "JSON";
        public static final String PKeyName = "idJSON";
        public static final String JSON = "JSON";
        /*
        * Métodos SOAP
        */
        public static final String Sincronizar = "obtenerJSONxVendedor_json";
    }

    public static final class Encuesta {
        public static final String Table = "Encuesta";
        public static final String PKeyName = "idEncuesta";
        public static final String Descripcion = "descripcion";
        public static final String FKTipoEncuesta = "idTipoEncuesta";
        /*Métodos SOAP*/
        public static final String Sincronizar = "obtenerEncuestaxVendedor_json";
    }

    public static final class EncuestaDetalle {
        public static final String Table = "EncuestaDetalle";
        public static final String PKeyName = "idEncuesta";
        public static final String PKEncuestaDetalle = "idEncuestaDetalle";
        public static final String FechaInicio = "fechaInicio";
        public static final String FechaFin = "fechaFin";
        public static final String ClientesObligatorios = "clientesObligatorios";
        public static final String ClientesAnonimos = "clientesAnonimos";
        public static final String EncuestasMinimas = "encuestasMinimas";
        public static final String FotosMinimas = "fotosMinimas";
        public static final String MaximoIntentosCliente = "maximoIntentosCliente";
        public static final String FiltroOcasion = "filtroOcasion";
        public static final String FiltroCanalVentas = "filtroCanalVentas";
        public static final String FiltroGiro = "filtroGiro";
        public static final String FiltroSubGiro = "filtroSubGiro";
        //public static final String PorCargo = "porCargo"; este filtro se aplicará en el servidor y no será necesario sincronizarlo
        public static final String PorCliente = "porCliente";
        public static final String PorSegmento = "porSegmento";
        //public static final String PorVendedor = "porVendedor"; este filtro se aplicará en el servidor y no será necesario sincronizarlo
        //public static final String Estado = "estado"; este filtro se aplicará en el servidor y no será necesario sincronizarlo
        /*Métodos SOAP*/
        public static final String Sincronizar = "obtenerEncuestaDetallexVendedor_json";
    }

    public static final class EncuestaDetallePregunta {
        public static final String Table = "EncuestaDetallePregunta";
        public static final String PKeyName = "idEncuesta";
        public static final String PKEncuestaDetalle = "idEncuestaDetalle";
        public static final String PKPregunta = "idPregunta";
        public static final String Pregunta = "pregunta";
        public static final String Orden = "orden";
        public static final String FKTipoRespuesta = "idTipoRespuesta";
        public static final String Requerido = "requerido";
        public static final String CantidadAlternativas = "cantidadAlternativas";
        /*Métodos SOAP*/
        public static final String Sincronizar = "obtenerEncuestaDetallePreguntaxVendedor_json";
    }

    public static final class EncuestaAlternativaPregunta {
        public static final String Table = "EncuestaAlternativaPregunta";
        public static final String PKeyName = "idEncuesta";
        public static final String PKEncuestaDetalle = "idEncuestaDetalle";
        public static final String PKPregunta = "idPregunta";
        public static final String PKAlternativa = "idAlternativa";
        public static final String Alternativa = "alternativa";
        public static final String Orden = "orden";
        /* Métodos SOAP*/
        public static final String Sincronizar = "obtenerEncuestaAlternativaPreguntaxVendedor_json";
    }

    public static final class EncuestaDetallexCliente {
        public static final String Table = "EncuestaDetallexCliente";
        public static final String PKeyName = "idEncuesta";
        public static final String PKEncuestaDetalle = "idEncuestaDetalle";
        public static final String PKCliente = "idCliente";
        /* Métodos SOAP*/
        public static final String Sincronizar = "obtenerEncuestaDetallexClientexVendedor_json";
    }

    public static final class EncuestaDetallexSegmento {
        public static final String Table = "EncuestaDetallexSegmento";
        public static final String PKeyName = "idEncuesta";
        public static final String PKEncuestaDetalle = "idEncuestaDetalle";
        public static final String PKSegmentoCliente = "idSegmentoCliente";
        /* Métodos SOAP*/
        public static final String Sincronizar = "obtenerEncuestaDetallexSegmentoxVendedor_json";
    }

    public static final class EncuestaTipo {
        public static final String Table = "EncuestaTipo";
        public static final String PKeyName = "idTipoEncuesta";
        public static final String Descripcion = "descripcion";
        /* Métodos SOAP*/
        public static final String Sincronizar = "obtenerEncuestaTipoxEmpresa_json";
        public static final String TIPO_TRADE = "T";
    }

    public static final class EncuestaRespuestaCabecera {
        public static final String Table = "EncuestaRespuestaCabecera";
        public static final String PKEncuesta = "idEncuesta";
        public static final String PKEncuestaDetalle = "idEncuestaDetalle";
        public static final String PKCliente = "idCliente";
        public static final String FKVendedor = "idVendedor";
        public static final String Fecha = "fecha";
        public static final String Flag = "flag";
        /* Métodos SOAP*/
        public static final String Sincronizar = "obtenerEncuestaRespuestaCabeceraxVendedor_json";
        public static final String ActualizarEncuesta = "actualizarEncuestaRespuestaxVendedor_json";
    }

    public static final class EncuestaRespuestaDetalle {
        public static final String Table = "EncuestaRespuestaDetalle";
        public static final String PKEncuesta = "idEncuesta";
        public static final String PKEncuestaDetalle = "idEncuestaDetalle";
        public static final String PKCliente = "idCliente";
        public static final String PKPregunta = "idPregunta";
        public static final String PKAlternativas = "idAlternativas";
        public static final String Descripcion = "descripcion";
        public static final String TipoRespuesta = "tipoRespuesta";
        public static final String Latitud = "latitud";
        public static final String Longitud = "longitud";
        public static final String FotoURL = "fotoURL";
        /* Métodos SOAP*/
        public static final String Sincronizar = "obtenerEncuestaRespuestaDetallexVendedor_json";
    }

    public static final class SubGiro {
        public static final String Table = "SubGiro";
        public static final String PKeyName = "idSubGiro";
        public static final String Descripcion = "descripcion";
        /*
        * Métodos SOAP
        */
        public static final String Sincronizar = "obtenerSubGiroxEmpresa_json";
    }

    public static final class Marca {
        public static final String Table = "Marca";
        public static final String PKeyName = "idMarca";
        public static final String Descripcion = "descripcion";
        /*
        * Métodos SOAP
        */
        public static final String Sincronizar = "obtenerMarcasxEmpresa_json";
    }

    public static final class Segmento {
        public static final String Table = "Segmento";
        public static final String PKeyName = "idSegmento";
        public static final String Descripcion = "descripcion";
        /*
        * Métodos SOAP
        */
        public static final String Sincronizar = "obtenerSegmentosxEmpresa_json";
    }

    public static final class AvanceCuota {
        public static final String Table = "AvanceCuota";
        public static final String PKeyName = "vendedor";
        public static final String Nombre = "nombre";
        public static final String TotalPaquetes = "totalPaquetes";
        public static final String CuotaDia = "cuotaDia";
        public static final String CajasFaltantes = "cajasFaltantes";
        public static final String Status = "status";
        /*
        * Métodos SOAP
        */
        public static final String Sincronizar = "obtenerReporteAvanceCuota_json";
    }

    public static final class ClienteRegistro {
        public static final String Table = "ClienteRegistro";
        public static final String PKName = "idClienteTemp";
        public static final String Nombres = "nombres";
        public static final String ApellidoPaterno = "apellidoPaterno";
        public static final String ApellidoMaterno = "apellidoMaterno";
        public static final String RucDni = "rucDni";
        public static final String Telefono = "telefono";
        public static final String Direccion = "direccion";
        public static final String Distrito = "distrito";
        public static final String FKSubGiro = "idSubGiro";
        public static final String FKRuta = "idRuta";
        public static final String FKModulo = "idModulo";
        public static final String FKVendedor = "idVendedor";
        public static final String Latitud = "latitud";
        public static final String Longitud = "longitud";
        public static final String FechaRegistro = "fechaRegistro";

        /*
        * Métodos SOAP
        */
        public static final String Sincronizar = "obtenerClienteCoordenadasxVendedor_json";
        public static final String ActualizarCoordenadas = "actualizarClienteCoordenadasxVendedor_json";
    }

    public static final class ClienteCoordenadas {
        public static final String Table = "ClienteCoordenadas";
        public static final String PKeyName = "idCliente";
        public static final String Latitud = "latitud";
        public static final String Longitud = "longitud";
        public static final String Flag = "flag";
        /*
        * Métodos SOAP
        */
        public static final String Sincronizar = "obtenerClienteCoordenadasxVendedor_json";
        public static final String ActualizarCoordenadas = "actualizarClienteCoordenadasxVendedor_json";
    }

    public static final class HojaRutaIndicador {
        public static final String Table = "HojaRutaIndicador";
        public static final String ejercicio = "ejercicio";
        public static final String periodo = "periodo";
        public static final String idCliente = "idCliente";
        public static final String tipoCobertura = "tipoCobertura";
        public static final String programado = "programado";
        public static final String transcurrido = "transcurrido";
        public static final String liquidado = "liquidado";
        public static final String hitRate = "hitRate";
        public static final String venAnoAnterior = "venAnoAnterior";
        public static final String venMesAnterior = "venMesAnterior";
        public static final String avanceMesActual = "avanceMesActual";
        public static final String proyectado = "proyectado";
        public static final String avanceAnual = "avanceAnual";
        public static final String avanceMes = "avanceMes";
        public static final String CUOTAGTM = "CUOTAGTM";
        public static final String SEGMENTO = "SEGMENTO";
        public static final String EXHIBIDORES = "EXHIBIDORES";
        public static final String NROPTAFRIOGTM = "NROPTAFRIOGTM";
        public static final String coberturaMultiple = "coberturaMultiple";
        /*
         * Métodos SOAP
         */
        public static final String Sincronizar = "obtenerHRIndicadorxVendedor_json";
    }

    public static final class HojaRutaMarcas {
        public static final String Table = "HojaRutaMarcas";
        public static final String ejercicio = "ejercicio";
        public static final String periodo = "periodo";
        public static final String idCliente = "idCliente";
        public static final String marca = "marca";
        public static final String canPaq = "canPaq";
        /*
         * Métodos SOAP
         */
        public static final String Sincronizar = "obtenerHRMarcaxVendedor_json";
    }

    public static final class RutasxPersona {
        public static final String Table = "RutasxPersona";
        public static final String idSucursal = "idSucursal";
        public static final String idPersona = "idPersona";
        public static final String idRuta = "idRuta";
        /*
         * Métodos SOAP
         */
        public static final String Sincronizar = "obtenerRutasxPersona_json";
    }

    public static final class ModuloxRuta {
        public static final String Table = "ModuloxRuta";
        public static final String idSucursal = "idSucursal";
        public static final String idModulo = "idModulo";
        public static final String idRuta = "idRuta";
        /*
         * Métodos SOAP
         */
        public static final String Sincronizar = "obtenerModuloxRuta_json";
    }

    public static final class HRCliente {
        public static final String table = "HRCliente";
        public static final String ejercicio = "ejercicio";
        public static final String periodo = "periodo";
        public static final String idCliente = "idCliente";
        public static final String programado = "programado";
        public static final String transcurrido = "transcurrido";
        public static final String liquidado = "liquidado";
        public static final String hitRate = "hitRate";
        public static final String coberturaMultiple = "coberturaMultiple";
        public static final String cuotaSoles = "cuotaSoles";
        public static final String cuotaPaquetes = "cuotaPaquetes";
        public static final String ventaSoles = "ventaSoles";
        public static final String ventaPaquetes = "ventaPaquetes";
        public static final String diasLaborados = "diasLaborados";
        public static final String diasLaborales = "diasLaborales";
        public static final String segmento = "segmento";
        public static final String nroExhibidores = "nroExhibidores";
        public static final String nroPuertasFrio = "nroPuertasFrio";
        public static final String avance = "avance";
        public static final String necesidadDiaSoles = "necesidadDiaSoles";
        public static final String necesidadDiaPaquetes = "necesidadDiaPaquetes";
        /*
         * Métodos SOAP
         */
        public static final String Sincronizar = "obtenerHRClientexVendedor_json";
    }

    public static final class HRVendedor {
        public static final String table = "HRVendedor";
        public static final String ejercicio = "ejercicio";
        public static final String periodo = "periodo";
        public static final String idVendedor = "idVendedor";
        public static final String cuotaSoles = "cuotaSoles";
        public static final String cuotaPaquetes = "cuotaPaquetes";
        public static final String ventaSoles = "ventaSoles";
        public static final String ventaPaquetes = "ventaPaquetes";
        public static final String diasLaborados = "diasLaborados";
        public static final String diasLaborales = "diasLaborales";
        public static final String coberturaMultiple = "coberturaMultiple";
        public static final String hitRate = "hitRate";
        public static final String avance = "avance";
        public static final String necesidadDiaSoles = "necesidadDiaSoles";
        public static final String necesidadDiaPaquetes = "necesidadDiaPaquetes";
        /*
         * Métodos SOAP
         */
        public static final String Sincronizar = "obtenerHRVendedorxVendedor_json";
    }

    public static final class HRMarcaResumen {
        public static final String table = "HRMarcaResumen";
        public static final String ejercicio = "ejercicio";
        public static final String periodo = "periodo";
        public static final String idCliente = "idCliente";
        public static final String marcas = "marcas";
        /*
         * Métodos SOAP
         */
        public static final String Sincronizar = "obtenerHRMarcaResumen_json";
    }

    public static final class MGRUP1F{
        public static final String Table = "MGRUP1F";

        public static final String FKMGrup1f = "IDGRUPO";
        public static final String descripcion = "DESCRIPCION";
        public static final String tipoGrupo = "TIPOGRUPO";
        /**
         * Métodos SOAP
         */
        public static final String Sincronizar = "obtenerGrupos_json";
    }

    public static final class MGRUP2F{
        public static final String Table = "MGRUP2F";

        public static final String FKGrup2f = "IDGRUPO";
        public static final String articulo = "ARTICULO";
        public static final String mandatorio = "MANDATORIO";
        public static final String unidades = "UNIDADES";
        public static final String malla = "MALLA";
        /**
         * Métodos SOAP
         */
        public static final String Sincronizar = "obtenerGrupoProductosxEmpresa_json";
    }

    public static final class MPROMO1F{
        public static final String Table = "MPROMO1F";

        public static final String FKPromo = "IDPROMOCION";
        public static final String descripcion = "DESCRIPCION";
        public static final String fecini = "FECINI";
        public static final String fecfin = "FECFIN";
        public static final String condicion = "CONDICION";
        public static final String estado = "ESTADO";
        public static final String orden = "ORDEN";
        public static final String mecanica = "MECANICA";
        public static final String malla = "MALLA";
        /**
         * Métodos SOAP
         */
        public static final String Sincronizar = "obtenerPromBonificacion_json";
    }

    public static final class MPROMO2F{
        public static final String Table = "MPROMO2F";

        public static final String FKPromo = "IDPROMOCION";
        public static final String idGrupo = "IDGRUPO";
        /**
         * Métodos SOAP
         */
        public static final String Sincronizar = "obtenerPromBonificacionGrupoxEmpresa_json";
    }

    public static final class MPROMO3F{
        public static final String Table = "MPROMO3F";

        public static final String FKPromo = "IDPROMOCION";
        public static final String idGrupo = "IDGRUPO";
        public static final String idRango = "IDRANGO";
        public static final String unidad = "UNIDAD";
        public static final String desde = "DESDE";
        public static final String hasta = "HASTA";
        public static final String porcada = "PORCADA";
        /**
         * Métodos SOAP
         */
        public static final String Sincronizar = "obtenerPromBonificacionGrupoDetallexEmpresa_json";
    }

    public static final class MPROMO4F{
        public static final String Table = "MPROMO4F";

        public static final String FKAccion = "IDACCION";
        public static final String descripcion = "DESCRIPCION";
        public static final String articulo = "ARTICULO";
        public static final String unidad = "UNIDAD";
        /**
         * Métodos SOAP
         */
        public static final String Sincronizar = "obtenerAcciones_json";
    }

    public static final class MPROMO5F{
        public static final String Table = "MPROMO5F";

        public static final String FKPromo = "IDPROMOCION";
        public static final String idAccion = "IDACCION";
        public static final String mecanica = "MECANICA";
        /**
         * Métodos SOAP
         */
        public static final String Sincronizar = "obtenerPromBonificacionAccionxEmpresa_json";
    }

    public static final class MPROMO6F{
        public static final String Table = "MPROMO6F";

        public static final String FKPromo = "IDPROMOCION";
        public static final String sucursal = "SUCURSAL";
        public static final String fdesde = "FDESDE";
        public static final String fhasta = "FHASTA";
        public static final String ftermino = "FTERMINO";
        public static final String fecCrea = "FECCREA";
        public static final String horCrea = "HORCREA";
        public static final String usuCrea = "USUCREA";
        public static final String fecUltmod = "FECULTMOD";
        public static final String horUltmod = "HORULTMOD";
        public static final String usUultmod = "USUULTMOD";
        /**
         * Métodos SOAP
         */
        public static final String Sincronizar = "obtenerPromBonificacionSucursal_json";
    }

    public static final class ClienteWathsapp {
        public static final String Table = "ClienteWathsapp";
        public static final String idEmpresa = "idEmpresa";
        public static final String idSucursal = "idSucursal";
        public static final String idCliente = "idCliente";
        public static final String whathsapp = "whathsapp";
        public static final String codigociudad = "codigociudad";
        public static final String telefonofijo = "telefonofijo";
        public static final String email = "email";
        public static final String fechaRegistro = "fechaRegistro";
        /**
         * Métodos SOAP
         */
        public static final String Sincronizar = "obtenerWhatsapp_json";
        public static final String Update = "updateWhatsapp_json";
    }

    public static final class ClienteBaja {
        public static final String Table = "ClienteBaja";
        public static final String idEmpresa = "idEmpresa";
        public static final String idSucursal = "idSucursal";
        public static final String idCliente = "idCliente";
        public static final String motivo = "motivo";
        public static final String flag = "flag";
        public static final String created_at = "created_at";
        public static final String updated_at = "updated_at";
        public static final String magic = "magic";
        /**
         * Métodos SOAP
         */
        public static final String Sincronizar = "clientebaja_json";
        public static final String Create = "create_clientebaja_json";
    }

    public static final class MotivoBaja {
        public static final String Table = "MotivoBaja";
        public static final String idEmpresa = "idEmpresa";
        public static final String idMotivoBaja = "idMotivoBaja";
        public static final String descripcion = "descripcion";
        /**
         * Métodos SOAP
         */
        public static final String Sincronizar = "motivobaja_json";
    }
}
