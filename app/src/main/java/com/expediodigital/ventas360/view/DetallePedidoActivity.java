package com.expediodigital.ventas360.view;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteException;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;

import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.expediodigital.ventas360.DAO.DAOCliente;
import com.expediodigital.ventas360.DAO.DAOConfiguracion;
import com.expediodigital.ventas360.DAO.DAOEncuesta;
import com.expediodigital.ventas360.DAO.DAOPedido;
import com.expediodigital.ventas360.DAO.DAOProducto;
import com.expediodigital.ventas360.DTO.DTOMotivoNoVenta;
import com.expediodigital.ventas360.DTO.DTOPedido;
import com.expediodigital.ventas360.R;
import com.expediodigital.ventas360.Ventas360App;
import com.expediodigital.ventas360.adapter.RecyclerViewProductoPedidoAdapter;
import com.expediodigital.ventas360.model.DocumentoGeneradoModel;
import com.expediodigital.ventas360.model.EncuestaDetalleModel;
import com.expediodigital.ventas360.model.FormaPagoModel;
import com.expediodigital.ventas360.model.PedidoCabeceraModel;
import com.expediodigital.ventas360.model.PedidoDetalleModel;
import com.expediodigital.ventas360.model.ProductoModel;
import com.expediodigital.ventas360.model.VendedorModel;
import com.expediodigital.ventas360.service.GPSTracker;
import com.expediodigital.ventas360.util.NumberToLetterConverter;
import com.expediodigital.ventas360.util.PDFUtil;
import com.expediodigital.ventas360.util.SoapManager;
import com.expediodigital.ventas360.util.TablesHelper;
import com.expediodigital.ventas360.util.Util;
import com.google.gson.Gson;
import com.google.gson.JsonParseException;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class DetallePedidoActivity extends AppCompatActivity {
    public static final String TAG = "DetallePedidoActivity";
    private static final String LINE = "-------------------------------------\n";

    private final int REQUEST_CODE_MODIFICAR_PEDIDO = 2;
    private final int REQUEST_CODE_UBICACION = 3;
    private int REQUEST_ENCUESTA = 4;
    private final int REQUEST_PREMISOS_UBICACION = 1;
    private Ventas360App ventas360App;

    TableRow row_documento;
    TextView tv_razonSocial, tv_numeroPedido, tv_flag, tv_serieNumeroDocumento, tv_estado, tv_motivoNoVenta, tv_importeTotal, /*tv_pesoTotal,*/ tv_direccionEntrega, tv_direccionFiscal, tv_fechaEntrega, tv_fechaPedido, tv_formaPago, tv_observación;
    RecyclerView recycler_productosPedido;

    RecyclerViewProductoPedidoAdapter adapter;
    ArrayList<PedidoDetalleModel> listaProductos = new ArrayList<>();
    PedidoCabeceraModel pedidoCabeceraModel;

    DAOPedido daoPedido;
    DAOConfiguracion daoConfiguracion;
    DAOCliente daoCliente;
    DAOProducto daoProducto;
    DAOEncuesta daoEncuesta;
    SoapManager soapManager;
    DecimalFormat formateador;
    String numeroPedido = "", idCliente = "", nombreCliente = "", flag = "";

    GPSTracker gpsTracker;
    Timer contadorSegundos = new Timer();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalle_pedido);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Util.actualizarToolBar("Detalle de gestion", true, this);
        ventas360App = (Ventas360App) getApplicationContext();
        formateador = Util.formateador();

        daoPedido = new DAOPedido(getApplicationContext());
        daoConfiguracion = new DAOConfiguracion(getApplicationContext());
        daoCliente = new DAOCliente(getApplicationContext());
        daoProducto = new DAOProducto(getApplicationContext());
        daoEncuesta = new DAOEncuesta(getApplicationContext());
        soapManager = new SoapManager(getApplicationContext());

        row_documento = (TableRow) findViewById(R.id.row_documento);
        tv_razonSocial = (TextView) findViewById(R.id.tv_razonSocial);
        tv_numeroPedido = (TextView) findViewById(R.id.tv_numeroPedido);
        tv_flag = (TextView) findViewById(R.id.tv_flag);
        tv_serieNumeroDocumento = (TextView) findViewById(R.id.tv_serieNumeroDocumento);
        tv_estado = (TextView) findViewById(R.id.tv_estado);
        tv_motivoNoVenta = (TextView) findViewById(R.id.tv_motivoNoVenta);
        tv_importeTotal = (TextView) findViewById(R.id.tv_importeTotal);
//        tv_pesoTotal = (TextView) findViewById(R.id.tv_pesoTotal);
        tv_direccionEntrega = (TextView) findViewById(R.id.tv_direccionEntrega);
        tv_direccionFiscal = (TextView) findViewById(R.id.tv_direccionFiscal);
        tv_fechaEntrega = (TextView) findViewById(R.id.tv_fechaEntrega);
        tv_fechaPedido = (TextView) findViewById(R.id.tv_fechaPedido);
        tv_formaPago = (TextView) findViewById(R.id.tv_formaPago);
        tv_observación = (TextView) findViewById(R.id.tv_observación);
        recycler_productosPedido = (RecyclerView) findViewById(R.id.recycler_productosPedido);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            numeroPedido = bundle.getString("numeroPedido");
            idCliente = bundle.getString("idCliente");
            nombreCliente = bundle.getString("nombreCliente");
            adapter = new RecyclerViewProductoPedidoAdapter(listaProductos, DetallePedidoActivity.this, false);
            recycler_productosPedido.setAdapter(adapter);
            recycler_productosPedido.addItemDecoration(new DividerItemDecoration(DetallePedidoActivity.this, DividerItemDecoration.VERTICAL));

            cargarPedido();
        }
        IniciarLocalizador();
    }

    private void IniciarLocalizador() {
        gpsTracker = new GPSTracker(this);

        if (gpsTracker.isGPSEnabled()) {
            if (Build.VERSION.SDK_INT >= 23) {
                if (ContextCompat.checkSelfPermission(DetallePedidoActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                        || ContextCompat.checkSelfPermission(DetallePedidoActivity.this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(DetallePedidoActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_PREMISOS_UBICACION);
                } else
                    gpsTracker.getLocations();
            } else
                gpsTracker.getLocations();
        } else {
            showDialogoUbicacion();
        }
    }

    private void showDialogoUbicacion() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(DetallePedidoActivity.this);
        alertDialog.setTitle("Ubicación");
        alertDialog.setMessage("Es necesario que active la ubicación del teléfono en precisión alta");
        alertDialog.setCancelable(false);

        alertDialog.setPositiveButton("ACEPTAR", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivityForResult(intent, REQUEST_CODE_UBICACION);
            }
        });
        alertDialog.show();
    }

    private void cargarPedido() {
        //Log.i(TAG,"CARGAR PEDIDO !");
        try {
            pedidoCabeceraModel = daoPedido.getPedidoCabecera(numeroPedido);
            tv_razonSocial.setText(idCliente + " " + pedidoCabeceraModel.getNombreCliente());
            String numeropedidoSimple = pedidoCabeceraModel.getNumeroPedido().substring(6, pedidoCabeceraModel.getNumeroPedido().length());
            tv_numeroPedido.setText(numeropedidoSimple);

            flag = pedidoCabeceraModel.getFlag();
            if (flag.equals(PedidoCabeceraModel.FLAG_ENVIADO)) {
                tv_flag.setText("(Enviado)");
                tv_flag.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.green_500));
            } else {
                tv_flag.setText("(Pendiente)");
                tv_flag.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.red_400));
            }

            if (pedidoCabeceraModel.getSerieDocumento().isEmpty() && pedidoCabeceraModel.getNumeroDocumento().isEmpty()) {
                row_documento.setVisibility(View.GONE);
            } else {
                row_documento.setVisibility(View.VISIBLE);
                tv_serieNumeroDocumento.setText(pedidoCabeceraModel.getSerieDocumento() + " - " + pedidoCabeceraModel.getNumeroDocumento());
            }

            if (pedidoCabeceraModel.getEstado().equals(PedidoCabeceraModel.ESTADO_ANULADO)) {
                tv_estado.setText("Anulado");
                tv_estado.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.red_400));
            } else if (pedidoCabeceraModel.getEstado().equals(PedidoCabeceraModel.ESTADO_FACTURADO)) {
                tv_estado.setText("Facturado");
                tv_estado.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.purple_500));
            } else if (pedidoCabeceraModel.getEstado().equals(PedidoCabeceraModel.ESTADO_GENERADO)) {
                tv_estado.setText("Generado");
                tv_estado.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.grey_700));
            }
            tv_motivoNoVenta.setText(pedidoCabeceraModel.getMotivoNoVenta());
            tv_direccionEntrega.setText(pedidoCabeceraModel.getDireccion());
            tv_direccionFiscal.setText(pedidoCabeceraModel.getDireccionFiscal());
            tv_fechaEntrega.setText(pedidoCabeceraModel.getFechaEntrega());
            tv_fechaPedido.setText(pedidoCabeceraModel.getFechaPedido());
            tv_formaPago.setText(pedidoCabeceraModel.getFormaPago());
            tv_observación.setText(pedidoCabeceraModel.getObservacion());

            tv_importeTotal.setText("S/. " + formateador.format(pedidoCabeceraModel.getImporteTotal()));
//            tv_pesoTotal.setText(formateador.format(pedidoCabeceraModel.getPesoTotal()) + " Kg.");

            //Una vez se haya cargado los datos del pedido se refresca las opciones del menú, para determinar que opciones deben mostrarse
            invalidateOptionsMenu();

            listaProductos.clear();
            listaProductos.addAll(daoPedido.getListaProductoPedido(numeroPedido));

            adapter.notifyDataSetChanged();
        } catch (SQLiteException e) {
            Toast.makeText(getApplicationContext(), "Ocurrió un error al obtener el pedido", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_activity_detalle_pedido, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        //Todos los vendedores pueden facturar
        if (((Ventas360App) getApplicationContext()).getModoVenta().equals(VendedorModel.MODO_PREVENTA) && !((Ventas360App) getApplicationContext()).getTipoVendedor().equals(VendedorModel.TIPO_PUNTO_VENTA)) {
            //Si es preventa y no es PUNTO_VENTA, entonces no puede facturar. En modo preventa, solo PUNTO_VENTA puede facturar
            menu.findItem(R.id.menu_pedido_detalle_facturar).setVisible(false);
        }

        if (pedidoCabeceraModel != null && !pedidoCabeceraModel.getEstado().equals(PedidoCabeceraModel.ESTADO_FACTURADO)) {
            menu.findItem(R.id.menu_pedido_detalle_forma_pago).setVisible(false);
            menu.findItem(R.id.menu_pedido_detalle_pdf).setVisible(false);
            //menu.findItem(R.id.menu_pedido_detalle_imprimir).setVisible(false);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.menu_pedido_detalle_anular:
                if (pedidoCabeceraModel.getPedidoEntregado() == 0)
                    new async_modificarPedido().execute(numeroPedido, async_modificarPedido.MODIFICAR_NO_VENTA, "", "", pedidoCabeceraModel.getEstado());
                else
                    Toast.makeText(getApplicationContext(), "El pedido ya ha sido entregado", Toast.LENGTH_SHORT).show();
                break;
            case R.id.menu_pedido_detalle_modificar:
                if (pedidoCabeceraModel.getPedidoEntregado() == 0) {
                    String codigoMotivoNoVenta = daoPedido.getIdMotivoNoVentaPedido(numeroPedido);
                    if (!codigoMotivoNoVenta.equals("0")) {
                        new async_modificarPedido().execute(numeroPedido, async_modificarPedido.MODIFICAR_NO_VENTA + "", "", "", pedidoCabeceraModel.getEstado());
                    } else {
                        new async_modificarPedido().execute(numeroPedido, async_modificarPedido.MODIFICAR_PEDIDO + "", idCliente, nombreCliente, pedidoCabeceraModel.getEstado());
                    }
                } else
                    Toast.makeText(getApplicationContext(), "El pedido ya ha sido entregado", Toast.LENGTH_SHORT).show();
                break;
            case R.id.menu_pedido_detalle_forma_pago:
                if (pedidoCabeceraModel.getPedidoEntregado() == 0) {
                    if (pedidoCabeceraModel.getEstado().equals(PedidoCabeceraModel.ESTADO_FACTURADO))
                        AbrirFormasPago();
                } else
                    Toast.makeText(getApplicationContext(), "El pedido ya ha sido entregado", Toast.LENGTH_SHORT).show();
                break;
            case R.id.menu_pedido_detalle_facturar:
                if (pedidoCabeceraModel.getPedidoEntregado() == 0) {
                    if (!flag.equals(PedidoCabeceraModel.FLAG_ENVIADO)) {
                        AlertDialog.Builder alerta = new AlertDialog.Builder(DetallePedidoActivity.this);
                        alerta.setIcon(R.drawable.ic_dialog_alert);
                        alerta.setTitle("No se pudo facturar");
                        alerta.setMessage("Envíe primero el pedido al servidor, luego intente facturar nuevamente");
                        alerta.setCancelable(false);
                        alerta.setPositiveButton("ACEPTAR", null);
                        alerta.show();
                    } else {
                        new async_modificarPedido().execute(numeroPedido, async_modificarPedido.FACTURAR_PEDIDO, "", "", pedidoCabeceraModel.getEstado());
                    }
                } else
                    Toast.makeText(getApplicationContext(), "El pedido ya ha sido entregado", Toast.LENGTH_SHORT).show();
                break;
            case R.id.menu_pedido_detalle_pdf:
                if (pedidoCabeceraModel.getEstado().equals(PedidoCabeceraModel.ESTADO_FACTURADO))
                    obtenerPDF(pedidoCabeceraModel.getSerieDocumento(), pedidoCabeceraModel.getNumeroDocumento());
                break;
//            case R.id.menu_pedido_detalle_imprimir:
//                //if (pedidoCabeceraModel.getEstado().equals(PedidoCabeceraModel.ESTADO_FACTURADO)){
//                Intent intent = new Intent(DetallePedidoActivity.this, ImprimirPedidoActivity.class);
//                intent.putExtra("headerToPrint", getHeaderToPrint());
//                intent.putExtra("bodyToPrint", getBodyToPrint());
//                intent.putExtra("footerToPrint", getFooterToPrint());
//                startActivity(intent);
//                //}
//                break;
            case android.R.id.home:
                setResult(RESULT_OK);
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    //RELLENAR EL NUMERO DE DOCUMENTO CON CEROS A LA IZQUIERDA
    private String getHeaderToPrint() {
        String textToPrint;
        textToPrint = "RUC N° " + ventas360App.getRucEmpresa() + "\n";
        textToPrint += daoConfiguracion.getNombreEmpresa(ventas360App.getRucEmpresa()) + "\n";
        textToPrint += daoConfiguracion.getDireccionSucursal() + "\n\n";

        if (pedidoCabeceraModel.getEstado().equals(PedidoCabeceraModel.ESTADO_FACTURADO)) {
            if (pedidoCabeceraModel.getRucDni().length() == 11) {
                textToPrint += "FACTURA ELECTRONICA\n";
            } else {
                textToPrint += "BOLETA DE VENTA ELECTRONICA\n";
            }
            textToPrint += pedidoCabeceraModel.getSerieDocumento() + "-" + pedidoCabeceraModel.getNumeroDocumento() + "\n\n";
        } else {
            textToPrint += "NOTA DE PEDIDO\n";
            String numeropedidoSimple = numeroPedido.substring(6, numeroPedido.length());
            textToPrint += numeropedidoSimple + "\n\n";
        }

        return textToPrint;
    }

    private String getBodyToPrint() {
        //Log.i(TAG,new Gson().toJson(listaProductos));
        double porcentajeIGV = 0.18;

        double importeTotal = pedidoCabeceraModel.getImporteTotal();
        double subTotalConISC = Util.redondearDouble(importeTotal / (1 + porcentajeIGV));//Es el total sin IGV
        double IGV = Util.redondearDouble(importeTotal - subTotalConISC);
        double ISC = 0.0;
        double percepcion = 0.0;
        double subTotal = 0.0;
        double opGratuita = 0.0;

        String idPoliticaCliente = daoCliente.getIdPoliticaPrecio(idCliente);

        for (int i = 0; i < listaProductos.size(); i++) {
            percepcion += listaProductos.get(i).getPercepcion();
            ISC += listaProductos.get(i).getISC();

            if (listaProductos.get(i).getTipoProducto().equals(ProductoModel.TIPO_BONIFICACION)) {
                double precioProductoBnof = daoProducto.getPrecioProducto(listaProductos.get(i).getIdProducto(), idPoliticaCliente, listaProductos.get(i).getIdUnidadMedida());
                if (precioProductoBnof > 0) {
                    //Si el precio es mayor a 0, es decir si se encontró su politica se acumula el precio
                    opGratuita += precioProductoBnof;
                } else {
                    //Si no se encontró su politica o el precio es 0, entonces se toma el precio de la politicaPorDefecto
                    opGratuita += daoProducto.getPrecioProducto(listaProductos.get(i).getIdProducto(), daoProducto.getIdPoliticaPrecioPorDefecto(), listaProductos.get(i).getIdUnidadMedida());
                }

            }
            //Log.i(TAG,"percepcion + "+listaProductos.get(i).getPercepcion());
        }
        percepcion = Util.redondearDouble(percepcion);
        ISC = Util.redondearDouble(ISC);
        subTotal = subTotalConISC - ISC;
        opGratuita = Util.redondearDouble(opGratuita);

        /*Calcular OpGratuita*/


        /*El limite de caracteres maximo es (28)*/
        String textToPrint;
        textToPrint = "CLIENTE:" + pedidoCabeceraModel.getNombreCliente() + "\n";

        if (pedidoCabeceraModel.getRucDni().length() == 11)
            textToPrint += "RUC: " + pedidoCabeceraModel.getRucDni() + "\n";
        else
            textToPrint += "DNI: " + pedidoCabeceraModel.getRucDni() + "\n";

        textToPrint += "DIRECCION: " + pedidoCabeceraModel.getDireccionFiscal() + "\n";
        String numeropedidoSimple = numeroPedido.substring(6, numeroPedido.length());

        textToPrint += "PEDIDO: " + numeropedidoSimple + " " + "FECHA: " + daoConfiguracion.getFechaString() + "\n";
        textToPrint += "MONEDA: " + "PEN-SOLES" + "\n";
        textToPrint += "COND.PAGO: " + pedidoCabeceraModel.getFormaPago() + "\n";
        /*if (pedidoCabeceraModel.getEstado().equals(PedidoCabeceraModel.ESTADO_FACTURADO))
            textToPrint += "SERIE:  "+Util.alinearTextoIzquierda(pedidoCabeceraModel.getSerieDocumento(),6) +" "+"N.DOC: "+Util.alinearTextoIzquierda(pedidoCabeceraModel.getNumeroDocumento(),10)+"\n";*/
        textToPrint += LINE;
        textToPrint = textToPrint + "CAN.UNI." + "  DESCRIP.       " + "PUNIT.IMPORT\n\n";

        for (int i = 0; i < listaProductos.size(); i++) {
            int ltotal = listaProductos.get(i).getDescripcion().length();
            int numlines_descr = ltotal / 18 + 1;
            int curr_idx = 0;
            while (curr_idx < numlines_descr) {
                if (curr_idx == 0) {
                    textToPrint += Util.alinearTextoDerecha(String.valueOf(listaProductos.get(i).getCantidad()), 3) + " " + Util.alinearTextoDerecha(listaProductos.get(i).getIdUnidadMedida(), 3) + " " + Util.alinearTextoIzquierda(listaProductos.get(i).getDescripcion(), 18) + " " + Util.alinearDecimalDerecha(listaProductos.get(i).getPrecioBruto(), 4, 1) + " " + Util.alinearDecimalDerecha(listaProductos.get(i).getPrecioNeto(), 5, 1) + "\n";
                } else {
                    String substring = listaProductos.get(i).getDescripcion().substring(18 * curr_idx, Math.min(18 * (curr_idx + 1), listaProductos.get(i).getDescripcion().length()));
                    textToPrint += "   " + " " + "   " + " " + substring + " " + "\n";
                }
                curr_idx += 1;
            }
        }
        textToPrint += "\n                  OP.GRAVADAS: " + Util.alinearDecimalDerecha(subTotal, 6, 2) + "\n";
        textToPrint += "                  OP.INAFECTA: " + Util.alinearDecimalDerecha(0.0, 6, 2) + "\n";
        textToPrint += "                 OP.EXONERADA: " + Util.alinearDecimalDerecha(0.0, 6, 2) + "\n";
        textToPrint += "                  OP.GRATUITA: " + Util.alinearDecimalDerecha(opGratuita, 6, 2) + "\n";
        textToPrint += "                 TOTAL DSCTOS: " + Util.alinearDecimalDerecha(0.0, 6, 2) + "\n";
        textToPrint += "                          ISC: " + Util.alinearDecimalDerecha(ISC, 6, 2) + "\n";
        textToPrint += "                          IGV: " + Util.alinearDecimalDerecha(IGV, 6, 2) + "\n";
        textToPrint += "                IMPORTE TOTAL: " + Util.alinearDecimalDerecha(importeTotal, 6, 2) + "\n";
        textToPrint += "                               " + "------\n";
        textToPrint += "                   PERCEPCION: " + Util.alinearDecimalDerecha(percepcion, 6, 2) + "\n";
        textToPrint += "                               " + "======\n";
        textToPrint += "               TOTAL A COBRAR: " + Util.alinearDecimalDerecha(importeTotal + percepcion, 6, 2) + "\n";
        textToPrint += LINE;
        String total = "SON: " +NumberToLetterConverter.convertNumberToLetter(importeTotal + percepcion);
        if(total.length() > 38){ //with 80 : solo 38 caracteres
            String first = total.substring(0, 38);
            String second = total.substring(38);
            textToPrint += first + "\n" + second+ "\n" ;
        }else{
            textToPrint += total + "\n";
        }
        return textToPrint;
    }

    private String getFooterToPrint() {
        String textToPrint = "\nRepresentacion impresa de";
        if (pedidoCabeceraModel.getEstado().equals(PedidoCabeceraModel.ESTADO_FACTURADO)) {
            if (pedidoCabeceraModel.getRucDni().length() == 11) {
                textToPrint += " FACTURA ELECTRONICA.\n";
            } else {
                textToPrint += " BOLETA DE VENTA ELECTRONICA.\n";
            }
        } else {
            textToPrint += " NOTA DE PEDIDO.\n";
        }
        textToPrint += "Autorizado mediante la resolucion Nro.0180050000781/SUNAT";
        return "";
    }

    String idFormaPagoSeleccionado = "";

    private void AbrirFormasPago() {
        DAOCliente daoCliente = new DAOCliente(getApplicationContext());
        DAOConfiguracion daoConfiguracion = new DAOConfiguracion(getApplicationContext());

        double saldoCredito = daoCliente.getSaldoCredito(idCliente, numeroPedido);
        double importePedido = pedidoCabeceraModel.getImporteTotal();
        boolean mostrarCredito = false;

        if ((saldoCredito - importePedido) >= 0)
            mostrarCredito = true;

        final ArrayList<FormaPagoModel> listaFormaPago = daoConfiguracion.getCondicionVenta();
        if (!mostrarCredito) {//Si no se debe mostrar el crédito, se quita de la lista
            for (int i = 0; i < listaFormaPago.size(); i++) {
                if (listaFormaPago.get(i).getIdFormaPago().equals(FormaPagoModel.ID_FORMA_PAGO_CREDITO))
                    listaFormaPago.remove(i);
            }
        }
        //Una vez removido la forma de pago crédito, se generan los items para los radioButton
        String[] items = new String[listaFormaPago.size()];
        int selectedPosition = 0;
        for (int i = 0; i < listaFormaPago.size(); i++) {
            items[i] = listaFormaPago.get(i).getDescripcion();
            if (pedidoCabeceraModel.getIdFormaPago().equals(listaFormaPago.get(i).getIdFormaPago()))
                selectedPosition = i;
        }

        idFormaPagoSeleccionado = pedidoCabeceraModel.getIdFormaPago();
        AlertDialog.Builder builder = new AlertDialog.Builder(DetallePedidoActivity.this);
        builder.setTitle("Formas de pago");
        //list of items
        builder.setSingleChoiceItems(items, selectedPosition,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        idFormaPagoSeleccionado = listaFormaPago.get(which).getIdFormaPago();
                    }
                });
        builder.setPositiveButton("ACTUALIZAR",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ActualizarFormaPago(pedidoCabeceraModel.getNumeroPedido(), idFormaPagoSeleccionado, pedidoCabeceraModel.getIdVendedor());
                    }
                });
        builder.setNegativeButton("CANCELAR", null);

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();//Llama al obBackPressed original, es decir no se ejecuta ya nada de lo que venga a continuacion
        setResult(RESULT_OK);
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult requestCode:" + requestCode + " resultCode:" + resultCode);
        if (requestCode == REQUEST_CODE_MODIFICAR_PEDIDO) {
            if (resultCode == RESULT_OK) {
                //Si se realizó la modificación correctamente, se retorna y refresca la lista de pedidos. Ya que el pedidoActivity al terminar lanza otro DetallePedidoActivity
                setResult(RESULT_OK);
                finish();
            }
        } else if (requestCode == REQUEST_CODE_UBICACION) {
            Log.e(TAG, "REQUEST_CODE_UBICACION -> " + resultCode);
            //No se valida si es RESULT_OK porque no tenemos control de la actividad Settings que se ha lanzado
            if (gpsTracker.isGPSEnabled()) {
                if (Build.VERSION.SDK_INT >= 23) {
                    if (ContextCompat.checkSelfPermission(DetallePedidoActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                            || ContextCompat.checkSelfPermission(DetallePedidoActivity.this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(DetallePedidoActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_PREMISOS_UBICACION);
                    } else
                        gpsTracker.getLocations();
                } else
                    gpsTracker.getLocations();
            } else {
                showDialogoUbicacion();
            }
        } else if (requestCode == REQUEST_ENCUESTA) {
            if (resultCode == RESULT_OK) {
                cargarPedido();
            }
        } else {
            if (resultCode == RESULT_OK) {
                cargarPedido();
            }
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_PREMISOS_UBICACION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                gpsTracker.getLocations();
            } else {
                Toast.makeText(this, "No se otorgaron permisos de ubicación", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        gpsTracker.stopUsingGPS();//Detener el Localizador porque PedidoActivity tendrá su propio localizador
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        gpsTracker.stopUsingGPS();
    }

    @Override
    public void onResume() {
        super.onResume();
        gpsTracker.getLocations();
    }

    class async_modificarPedido extends AsyncTask<String, Void, String> {
        final static String MODIFICAR_PEDIDO = "0";
        final static String MODIFICAR_NO_VENTA = "1";
        final static String MODIFICAR_OBSERVACION = "2";
        final static String FACTURAR_PEDIDO = "3";
        String numeroPedido;
        String tipo;
        String idCliente;
        String nombreCliente;
        String estado;
        ProgressDialog pDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(DetallePedidoActivity.this);
            pDialog.setMessage("Verificando...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected String doInBackground(String... strings) {
            numeroPedido = strings[0];
            tipo = strings[1];
            idCliente = strings[2];
            nombreCliente = strings[3];
            estado = strings[4];

            String response = "";
            /*Para preventa no es necesario que se obtenga el estado del pedido online, ya que hay usuarios que no tienen acceso a internet,
             * ademas de que en preventa solo un usuario tiene acceso a su propio pedido a diferencia de autoventa donde el transportista
             * puede modificar el pedido de los vendedores*/
            if (ventas360App.getModoVenta().equals(VendedorModel.MODO_PREVENTA) && !ventas360App.getSettings_preventaEnLinea()) {
                response = estado;
            } else if (ventas360App.getTipoVendedor().equals(VendedorModel.TIPO_MERCADEO)) {
                response = estado;
            } else {
                if (Util.isConnectingToRed(DetallePedidoActivity.this)) {
                    try {
                        response = soapManager.obtenerEstadoPedido(TablesHelper.ObjPedido.ObtenerEstado, numeroPedido);

                        if (response.length() > 1) {
                            response = "Error";
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        Log.e(TAG, e.getMessage() + "");
                        response = "Error";
                    }
                } else {
                    // Sin conexion al Servidor
                    response = "NoInternet";
                }
            }
            return response;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            pDialog.dismiss();
            if (result.equals("NoInternet")) {
                // Sin conexion al Servidor, no se pudo verificar asi que no se continua con la accion seleccionada
                AlertDialog.Builder alerta = new AlertDialog.Builder(DetallePedidoActivity.this);
                alerta.setIcon(R.drawable.ic_dialog_error);
                alerta.setTitle("Sin conexión");
                alerta.setMessage("Por el momento no es posible completar la acción, verifique su conexión");
                alerta.setCancelable(false);
                alerta.setPositiveButton("ACEPTAR", null);
                alerta.show();
            } else if (result.equals("Error")) {
                // Error en el envio, no se pudo verificar asi que no se continua con la accion seleccionada
                AlertDialog.Builder alerta = new AlertDialog.Builder(DetallePedidoActivity.this);
                alerta.setIcon(R.drawable.ic_dialog_error);
                alerta.setTitle("No se pudo verificar");
                alerta.setMessage("No se pudo obtener el estado del pedido, no es posible completar la acción");
                alerta.setCancelable(false);
                alerta.setPositiveButton("ACEPTAR", null);
                alerta.show();

            } else if (result.equals("N")) {
                // Error en el envio, no se pudo verificar asi que no se continua con la accion seleccionada
                AlertDialog.Builder alerta = new AlertDialog.Builder(DetallePedidoActivity.this);
                alerta.setIcon(R.drawable.ic_dialog_error);
                alerta.setTitle("No se pudo verificar");
                alerta.setMessage("No se pudo obtener el estado del pedido, las guias están cerradas");
                alerta.setCancelable(false);
                alerta.setPositiveButton("ACEPTAR", null);
                alerta.show();
            } else if (result.equals(" ")) {
                // Error en el envio, no se pudo verificar asi que no se continua con la accion seleccionada
                AlertDialog.Builder alerta = new AlertDialog.Builder(DetallePedidoActivity.this);
                alerta.setIcon(R.drawable.ic_dialog_error);
                alerta.setTitle("No se pudo verificar");
                alerta.setMessage("No se pudo obtener el estado del pedido, tal vez no haya sido enviado al servidor");
                alerta.setCancelable(false);
                alerta.setPositiveButton("ACEPTAR", null);
                alerta.show();
            } else {
                if (result.equals(PedidoCabeceraModel.ESTADO_FACTURADO)) {
                    if (tipo.equals(MODIFICAR_NO_VENTA)) {
                        //Se puede anular un pedido ya facturado, para quitar la factura y obtener el pedido

                        AlertDialog.Builder alerta = new AlertDialog.Builder(DetallePedidoActivity.this);
                        alerta.setIcon(R.drawable.ic_dialog_block);
                        alerta.setTitle("Atención");
                        alerta.setMessage("El pedido se encuentra facturado, ¿Desea anular la factura?");
                        alerta.setCancelable(false);
                        alerta.setPositiveButton("ANULAR FACTURA", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                daoPedido.actualizarPedidoGenerado(numeroPedido);
                                EnviarPedido(numeroPedido);
                            }
                        });
                        alerta.setNegativeButton("CANCELAR", null);
                        alerta.show();
                        cargarPedido();

                    } else {
                        //El pedido ya ha sido transferido y no se puede modificar
                        AlertDialog.Builder alerta = new AlertDialog.Builder(DetallePedidoActivity.this);
                        alerta.setIcon(R.drawable.ic_dialog_block);
                        alerta.setTitle("Atención");
                        alerta.setMessage("El pedido ha sido facturado y no se puede modificar\nComuníquese con el administrador");
                        alerta.setCancelable(false);
                        alerta.setPositiveButton("ACEPTAR", null);
                        alerta.show();
                        cargarPedido();
                    }
                } else {
                    if (tipo.equals(MODIFICAR_PEDIDO)) {
                        //Si el pedido esta anulado entonces ya no puede ser modificado
                        if (result.equals(PedidoCabeceraModel.ESTADO_ANULADO)) {
                            Toast.makeText(DetallePedidoActivity.this, "El pedido se encuentra anulado actualmente", Toast.LENGTH_SHORT).show();
                            cargarPedido();
                            showDialogoNoVenta(numeroPedido);
                        } else {
                            Intent intent = new Intent(DetallePedidoActivity.this, PedidoActivity.class);
                            intent.putExtra("origen", PedidoActivity.ORIGEN_PEDIDOS);
                            intent.putExtra("accion", PedidoActivity.ACCION_EDITAR_PEDIDO);
                            intent.putExtra("idCliente", idCliente);
                            intent.putExtra("numeroPedido", numeroPedido);
                            intent.putExtra("nombreCliente", nombreCliente);
                            startActivityForResult(intent, REQUEST_CODE_MODIFICAR_PEDIDO);
                        }
                    } else if (tipo.equals(MODIFICAR_NO_VENTA)) {
                        showDialogoNoVenta(numeroPedido);
                    } else if (tipo.equals(MODIFICAR_OBSERVACION)) {
                        showDialogoObservacion(numeroPedido);
                    } else if (tipo.equals(FACTURAR_PEDIDO)) {
                        if (result.equals(PedidoCabeceraModel.ESTADO_ANULADO)) {
                            Toast.makeText(DetallePedidoActivity.this, "El pedido se encuentra anulado actualmente", Toast.LENGTH_SHORT).show();
                            cargarPedido();
                        } else {
                            FacturarPedido(numeroPedido);
                        }

                    }
                }
            }
        }
    }

    int noVentaSelected = -1;

    public void showDialogoNoVenta(final String numeroPedido) {
        noVentaSelected = -1;
        String idNoVentaPedido = daoPedido.getIdMotivoNoVentaPedido(numeroPedido);
        AlertDialog.Builder alerta = new AlertDialog.Builder(DetallePedidoActivity.this);
        alerta.setIcon(R.drawable.ic_dialog_block);
        alerta.setTitle("Motivo de no Venta");

        final ArrayList<DTOMotivoNoVenta> listaMotivos = daoPedido.getMotivoNoVenta();
        List<CharSequence> arrayList = new ArrayList<>();
        for (int i = 0; i < listaMotivos.size(); i++) {
            arrayList.add(listaMotivos.get(i).getDescripcion());
            if (idNoVentaPedido.equals(listaMotivos.get(i).getIdMotivoNoVenta())) {
                noVentaSelected = i;
            }
            //Si no se tiene un motivo anteriormente seleccionado, se selecciona el motivo por defecto.
            if (noVentaSelected == -1 && listaMotivos.get(i).getIdMotivoNoVenta().equals(PedidoCabeceraModel.ID_MOTIVO_NO_COMPRA_DEFAULT)) {
                noVentaSelected = i;
            }
        }
        String[] array = new String[arrayList.size()];
        arrayList.toArray(array);

        if (noVentaSelected == -1)//Si después de las validaciones no se obtuvo algun motivo no venta por defecto, se selecciona el primero
            noVentaSelected = 0;
        alerta.setSingleChoiceItems(array, noVentaSelected, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                noVentaSelected = which;
            }
        });

        alerta.setCancelable(true);
        alerta.setPositiveButton("ACEPTAR", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                final DTOMotivoNoVenta dtoMotivoNoVenta = listaMotivos.get(noVentaSelected);

                guardarMotivoNoVenta(numeroPedido, dtoMotivoNoVenta.getIdMotivoNoVenta());
                EnviarPedido(numeroPedido);
            }
        });
        alerta.setNegativeButton("CANCELAR", null);
        alerta.show();
    }

    public void guardarMotivoNoVenta(final String numeroPedido, final String idMotivoNoVenta) {
        new AsyncTask<Void, Void, Void>() {
            ProgressDialog pDialog;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                pDialog = new ProgressDialog(DetallePedidoActivity.this);
                pDialog.setCancelable(false);
                pDialog.setIndeterminate(true);
                pDialog.setMessage("Guardando motivo de no venta...");
                pDialog.show();
            }

            @Override
            protected Void doInBackground(Void... strings) {

                while (gpsTracker.getLatitude() == 0.0 && gpsTracker.getLongitude() == 0.0) {
                    Log.d(TAG, "latitud y longitud 0.0");//Mantener el hilo trabajando hasta que se tome alguna posición
                }

                PedidoCabeceraModel pedido = new PedidoCabeceraModel();
                pedido.setNumeroPedido(numeroPedido);
                pedido.setEstado(PedidoCabeceraModel.ESTADO_ANULADO);//Anulado
                pedido.setFlag(PedidoCabeceraModel.FLAG_PENDIENTE);//Pendiente
                pedido.setIdMotivoNoVenta(idMotivoNoVenta);
                pedido.setLatitud(gpsTracker.getLatitude());
                pedido.setLongitud(gpsTracker.getLongitude());

                daoPedido.actualizarMotivoNoVentaPedido(pedido);
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                pDialog.dismiss();
            }
        }.execute();
    }

    public void showDialogoObservacion(final String numeroPedido) {
        String observacionPedido = daoPedido.getObservacionPedido(numeroPedido);
        AlertDialog.Builder alerta = new AlertDialog.Builder(DetallePedidoActivity.this);
        alerta.setIcon(R.drawable.ic_dialog_alert);
        alerta.setTitle("Observación del Pedido");
        final EditText editText = new EditText(DetallePedidoActivity.this);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        editText.setLayoutParams(layoutParams);
        layoutParams.setMargins(10, 10, 10, 10);

        editText.setLayoutParams(layoutParams);
        editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        editText.setMinLines(4);

        editText.setHint("Ingrese una observación");
        editText.setText(observacionPedido);
        alerta.setView(editText);

        alerta.setPositiveButton("ACEPTAR", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                final String observacion = editText.getText().toString();
                guardarObservacionPedido(numeroPedido, observacion);
                EnviarPedido(numeroPedido);

            }
        });
        alerta.setNegativeButton("CANCELAR", null);
        alerta.show();
    }

    public void guardarObservacionPedido(String numeroPedido, String observacion) {
        PedidoCabeceraModel pedido = new PedidoCabeceraModel();
        pedido.setNumeroPedido(numeroPedido);
        pedido.setFlag(PedidoCabeceraModel.FLAG_PENDIENTE);//Pendiente
        pedido.setObservacion(observacion);

        daoPedido.actualizarObservacionPedido(pedido);
    }


    int contador = 0;

    /**
     * Método que ejecuta una acción "run()" cada determinados segundos "1000" = 1 .Lo hace en un hilo propio, al tener un propio hilo debe cancelarse ".cancel()" si no continuará aun cuando la actividad haya sido destruida.
     * No es usado actualmente
     */
    private void callTimer() {
        Log.d(TAG, "Iniciando callTimer");
        contador = 0;
        contadorSegundos = new Timer();
        contadorSegundos.schedule(new TimerTask() {
            @Override
            public void run() {
                Log.d("=============>>>>", "timer triggered contador:" + contador);
                if (contador == 10) {
                    if (gpsTracker.getLatitude() != 0.0 && gpsTracker.getLongitude() != 0.0) {
                        contadorSegundos.cancel();
                    } else {
                        contadorSegundos.cancel();
                        callTimer();
                    }
                }
                contador++;
            }
        }, 0, 1000);
    }

    private void FacturarPedido(final String numeroPedido) {
        Log.d(TAG, "FacturarPedido");

        new AsyncTask<Void, Void, String>() {
            final String FACTURADO = "F";
            final String JSONEXCEPTION = "jsonException";
            final String SIN_CONEXION = "SinConexion";
            final String OTRO_ERROR = "error";

            ProgressDialog pDialog;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                pDialog = new ProgressDialog(DetallePedidoActivity.this);
                pDialog.setCancelable(false);
                pDialog.setIndeterminate(true);
                pDialog.setMessage("Facturando pedido...");
                pDialog.show();
            }

            @Override
            protected String doInBackground(Void... strings) {
                Gson gson = new Gson();
                if (Util.isConnectingToRed(DetallePedidoActivity.this)) {
                    try {

                        Thread.sleep(1000 * 6);//Esperar 6 segundos para dar tiempo a que el GPS tome alguna posición
                        while (gpsTracker.getLatitude() == 0.0 && gpsTracker.getLongitude() == 0.0) {
                            Log.d(TAG, "latitud y longitud 0.0");//Mantener el hilo trabajando hasta que se tome alguna posición
                        }
                        daoPedido.actualizarLatitudLongitudDocumento(numeroPedido, gpsTracker.getLatitude(), gpsTracker.getLongitude());

                        /*Generar Percepciones para los productos*/
                        /* Aplica a todas las Facturas, en caso de Boletas a montos mayores o igual a 100(monto parametro limite)*/
                        if (daoCliente.isAfectoPercepcion(idCliente)) {
                            daoPedido.generarPercepcion(numeroPedido, daoCliente.getRucDniCliente(idCliente), pedidoCabeceraModel.getImporteTotal(), daoConfiguracion.getLimitePercepcion());
                        }
                        /* ------------------------------------- */

                        ArrayList<DTOPedido> pedidoEnviar = daoPedido.getDTOPedidoCompleto(numeroPedido);
                        String cadena = gson.toJson(pedidoEnviar);
                        //Log.i(TAG,cadena);
                        String cadenaResultado = soapManager.facturarPedido(TablesHelper.ObjPedido.FacturarPedido, cadena);
                        return daoPedido.actualizarPedidoFacturado(cadenaResultado);
                        //Se tendría que devolver un JSON con el numeroPedido, estado y serie y numero boleta o factura

                    } catch (JsonParseException ex) {
                        ex.printStackTrace();
                        return JSONEXCEPTION;
                    } catch (Exception e) {
                        e.printStackTrace();
                        return OTRO_ERROR;
                    }
                } else {
                    return SIN_CONEXION;
                }
            }

            @Override
            protected void onPostExecute(String respuesta) {
                super.onPostExecute(respuesta);
                pDialog.dismiss();
                switch (respuesta) {
                    case FACTURADO:
                        showDialogoPostEnvio("Envío satisfactorio", "El pedido fue facturado", R.drawable.ic_dialog_check, false);
                        break;
                    case SIN_CONEXION:
                        showDialogoPostEnvio("Sin conexión", "Es probable que no tenga acceso a INTERNET", R.drawable.ic_dialog_error, false);
                        break;
                    case JSONEXCEPTION:
                        showDialogoPostEnvio("Atención", "El pedido fue enviado pero no se pudo verificar\nConsulte con el administrador", R.drawable.ic_dialog_alert, false);
                        break;
                    case OTRO_ERROR:
                        showDialogoPostEnvio("Error", "No se pudo facturar el pedido, inténtelo nuevamente", R.drawable.ic_dialog_error, false);
                        break;
                    default:
                        showDialogoPostEnvio("Error", "" + respuesta, R.drawable.ic_dialog_error, false);
                        break;
                }
            }
        }.execute();

        Log.d(TAG, "FacturarPedido");
    }

    private void EnviarPedido(final String numeroPedido) {

        new AsyncTask<Void, Void, String>() {
            final String ENVIADO = "E";
            final String INCOMPLETO = "I";
            final String PENDIENTE = "P";
            final String TRANSFERIDO = "T";
            final String JSONEXCEPTION = "jsonException";
            final String SIN_CONEXION = "SinConexion";
            final String OTRO_ERROR = "error";

            ProgressDialog pDialog;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                pDialog = new ProgressDialog(DetallePedidoActivity.this);
                pDialog.setCancelable(false);
                pDialog.setIndeterminate(true);
                pDialog.setMessage("Enviando pedido...");
                pDialog.show();
            }

            @Override
            protected String doInBackground(Void... strings) {
                Gson gson = new Gson();
                if (Util.isConnectingToRed(DetallePedidoActivity.this)) {
                    try {
                        ArrayList<DTOPedido> pedidoEnviar = daoPedido.getDTOPedidoCompleto(numeroPedido);
                        String cadena = gson.toJson(pedidoEnviar);
                        String cadenaResultado = soapManager.enviarPendientes(TablesHelper.ObjPedido.ActualizarObjPedido, cadena);
                        return daoPedido.actualizarFlagPedidos(cadenaResultado);
                    } catch (JsonParseException ex) {
                        ex.printStackTrace();
                        return JSONEXCEPTION;
                    } catch (Exception e) {
                        e.printStackTrace();
                        return OTRO_ERROR;
                    }
                } else {
                    return SIN_CONEXION;
                }
            }

            @Override
            protected void onPostExecute(String respuesta) {
                super.onPostExecute(respuesta);
                pDialog.dismiss();
                switch (respuesta) {
                    case ENVIADO:
                        showDialogoPostEnvio("Envío satisfactorio", "El pedido fue ingresado al servidor", R.drawable.ic_dialog_check, true);
                        break;
                    case INCOMPLETO:
                        showDialogoPostEnvio("Atención", "No se pudieron guardar todos los datos", R.drawable.ic_dialog_alert, true);
                        break;
                    case PENDIENTE:
                        showDialogoPostEnvio("Atención", "El servidor no pudo ingresar el pedido", R.drawable.ic_dialog_error, true);
                        break;
                    case TRANSFERIDO:
                        showDialogoPostEnvio("Atención", "El pedido ya se encuentra en proceso de facturación \nComuníquese con el administrador", R.drawable.ic_dialog_block, true);
                        break;
                    case SIN_CONEXION:
                        showDialogoPostEnvio("Sin conexión", "Es probable que no tenga acceso a INTERNET, El pedido se guardó localmente", R.drawable.ic_dialog_error, true);
                        break;
                    case JSONEXCEPTION:
                        showDialogoPostEnvio("Atención", "El pedido fue enviado pero no se pudo verificar\nConsulte con el administrador", R.drawable.ic_dialog_alert, true);
                        break;
                    case OTRO_ERROR:
                        showDialogoPostEnvio("Error", "No se pudo enviar el pedido, se guardó localmente", R.drawable.ic_dialog_error, true);
                        break;
                    default:
                        showDialogoPostEnvio("Error", "No se pudo enviar el pedido, se guardó localmente", R.drawable.ic_dialog_error, true);
                        break;
                }
            }
        }.execute();
    }

    public void ActualizarFormaPago(final String numeroPedido, final String idFormaPago, final String idVendedor) {
        new AsyncTask<Void, Void, String>() {
            final String ENVIADO = "E";
            final String INCOMPLETO = "I";
            final String PENDIENTE = "P";
            final String TRANSFERIDO = "T";
            final String JSONEXCEPTION = "jsonException";
            final String SIN_CONEXION = "SinConexion";
            final String OTRO_ERROR = "error";
            final String SIN_PENDIENTES = "sinPendientes";

            ProgressDialog pDialog;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                pDialog = new ProgressDialog(DetallePedidoActivity.this);
                pDialog.setCancelable(false);
                pDialog.setIndeterminate(true);
                pDialog.setMessage("Actualizando forma de pago...");
                pDialog.show();
            }

            @Override
            protected String doInBackground(Void... strings) {

                if (Util.isConnectingToRed(DetallePedidoActivity.this)) {
                    try {
                        HashMap<String, Object> pedidoEnviar = new HashMap<>();
                        pedidoEnviar.put("numeroPedido", numeroPedido);
                        pedidoEnviar.put("idVendedor", idVendedor);
                        pedidoEnviar.put("idFormaPago", idFormaPago);
                        //pedidoEnviar.put("idAlmacen", idAlmacen);

                        String cadenaResultado = soapManager.actualizarFormaPago(TablesHelper.ObjPedido.ActualizarFormaPago, pedidoEnviar);
                        if (cadenaResultado.equals(ENVIADO)) {
                            daoPedido.actualizarFormaPago(numeroPedido, idFormaPago);
                        }
                        return cadenaResultado;

                    } catch (JsonParseException ex) {
                        ex.printStackTrace();
                        return JSONEXCEPTION;
                    } catch (Exception e) {
                        e.printStackTrace();
                        return OTRO_ERROR;
                    }
                } else {
                    return SIN_CONEXION;
                }
            }

            @Override
            protected void onPostExecute(String respuesta) {
                super.onPostExecute(respuesta);
                pDialog.dismiss();

                switch (respuesta) {
                    case ENVIADO:
                        showDialogoPostEnvio("Envío sactisfactorio", "Se actualizó la forma de pago del pedido", R.drawable.ic_dialog_check, false);
                        break;
                    case INCOMPLETO:
                        showDialogoPostEnvio("Atención", "No se pudieron guardar todos los datos", R.drawable.ic_dialog_alert, false);
                        break;
                    case PENDIENTE:
                        showDialogoPostEnvio("Atención", "No se pudo actualizar el pedido, intente nuevamente", R.drawable.ic_dialog_error, false);
                        break;
                    case TRANSFERIDO:
                        showDialogoPostEnvio("Atención", "Algunos pedidos se encuentran en proceso de facturación \nComuníquese con el administrador", R.drawable.ic_dialog_block, false);
                        break;
                    case SIN_CONEXION:
                        showDialogoPostEnvio("Sin conexión", "Es probable que no tenga acceso a INTERNET, compruebe su conexión", R.drawable.ic_dialog_error, false);
                        break;
                    case JSONEXCEPTION:
                        showDialogoPostEnvio("Atención", "El pedido fue enviado pero no se pudo verificar\nConsulte con el administrador", R.drawable.ic_dialog_alert, false);
                        break;
                    case OTRO_ERROR:
                        showDialogoPostEnvio("No se pudo enviar", "Verifique que tenga acceso a internet e inténtelo nuevamente. Si el problema persiste, comuníquese con el administrador", R.drawable.ic_dialog_error, false);
                        break;
                    case SIN_PENDIENTES:
                        Toast.makeText(DetallePedidoActivity.this, "No hay pedidos pendientes", Toast.LENGTH_SHORT).show();
                        break;
                    default:
                        showDialogoPostEnvio("Atención !", "" + respuesta, R.drawable.ic_dialog_error, false);
                        break;
                }
            }
        }.execute();
    }

    private void showDialogoPostEnvio(String titulo, String mensaje, @DrawableRes int icon, final boolean accionComercial) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(DetallePedidoActivity.this);
        builder.setTitle(titulo);
        builder.setMessage(mensaje);
        builder.setIcon(icon);
        builder.setCancelable(false);
        builder.setPositiveButton("ACEPTAR", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if (accionComercial) {
                    EncuestaDetalleModel encuestaDetalleModel = daoEncuesta.getEncuestaDetalle(TablesHelper.EncuestaTipo.TIPO_TRADE);

                    if (encuestaDetalleModel != null && ventas360App.getTipoVendedor().equals(VendedorModel.TIPO_MERCADEO)) {
                        tomarEncuesta(encuestaDetalleModel);
                    } else {
                        cargarPedido();
                    }
                } else {
                    cargarPedido();
                }


            }
        });
        builder.show();
    }

    private void tomarEncuesta(EncuestaDetalleModel encuestaDetalleModel) {

        if (encuestaDetalleModel != null) {
            Intent intent = new Intent(this, EncuestaClienteDialogActivity.class);

            intent.putExtra("idCliente", idCliente);
            intent.putExtra("razonSocial", pedidoCabeceraModel.getNombreCliente());
            intent.putExtra("clientesObligatorios", encuestaDetalleModel.getClientesObligatorios() == 1); //Determina si se puede cerrar u obviar la encuesta
            intent.putExtra("descripcionEncuesta", encuestaDetalleModel.getDescripcionEncuesta());
            intent.putExtra("idTipoEncuesta", encuestaDetalleModel.getIdTipoEncuesta());
            intent.putExtra("tipoEncuesta", encuestaDetalleModel.getTipoEncuesta());
            intent.putExtra("idEncuesta", encuestaDetalleModel.getIdEncuesta());
            intent.putExtra("idEncuestaDetalle", encuestaDetalleModel.getIdEncuestaDetalle());

            startActivityForResult(intent, REQUEST_ENCUESTA);
        } else {
            Toast.makeText(this, "No se encontró alguna encuesta", Toast.LENGTH_SHORT).show();
        }
    }

    private void obtenerPDF(String serieDocumento, String numeroDocumento) {
        final String idEmpresa = ((Ventas360App) getApplicationContext()).getIdEmpresa();
        final String idSucursal = ((Ventas360App) getApplicationContext()).getIdSucursal();
        String nombrePDF = idEmpresa + idSucursal + "_" + serieDocumento + numeroDocumento + "_" + numeroPedido + ".pdf";
        //String nombrePDF = idEmpresa+idSucursal+"_"+serieDocumento+numeroDocumento+"_"+"180225001056"+".pdf";
        /*Validar que el PDF ya exista (ya este descargado) antes de obtener de la webservice, si ya existe simplemente debe mostrarse*/

        File storageDir = Environment.getExternalStoragePublicDirectory(getResources().getString(R.string.Ventas360App_PDF));
        if (!storageDir.exists())
            storageDir.mkdirs();
        File pdfFile = new File(storageDir, nombrePDF);
        if (pdfFile.exists()) {
            Toast.makeText(this, "Abriendo PDF...", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "El PDF ya existe");
            PDFUtil.abrirPDF(DetallePedidoActivity.this, nombrePDF);
        } else {
            new AsyncTask<Void, Void, Boolean>() {
                ProgressDialog progressDialog = new ProgressDialog(DetallePedidoActivity.this);
                String nombrePDF = "";
                String error = "";

                @Override
                protected void onPreExecute() {
                    progressDialog.setMessage("Obteniendo documento...");
                    progressDialog.setIndeterminate(true);
                    progressDialog.show();
                }

                @Override
                protected Boolean doInBackground(Void... params) {
                    try {
                        SoapManager soapManager = new SoapManager(getApplicationContext());
                        DocumentoGeneradoModel documentoGeneradoModel = soapManager.obtenerDocumentoGenerado(TablesHelper.ObjPedido.ObtenerDocumentoGenerado, numeroPedido);
                        if (documentoGeneradoModel != null) {
                            Log.d(TAG, "ERROR:" + documentoGeneradoModel.getError() + ":");
                            if (documentoGeneradoModel.getError().equals("")) {
                                Log.i(TAG, "DOCUMENTO MODEL->" + new Gson().toJson(documentoGeneradoModel));
                                nombrePDF = idEmpresa + idSucursal + "_" + documentoGeneradoModel.getSerieDocumento() + documentoGeneradoModel.getNumeroDocumento() + "_" + numeroPedido + ".pdf";
                                Log.i(TAG, "NOMBRE DEL PDF->" + nombrePDF);
                                PDFUtil.generarPDF_fromBase64String(getApplicationContext(), documentoGeneradoModel.getPDFBase64(), nombrePDF);
                                return true;
                            } else {
                                error = documentoGeneradoModel.getError();
                            }
                        } else {
                            Log.e(TAG, "EL DOCUMENTO MODEL ES NULL");
                        }
                    } catch (Exception e) {
                        Log.e("PDF" + " Mail", "Ocurrió un problema al enviar email, con los datos: ");
                        e.printStackTrace();
                        return false;
                    }
                    return false;
                }

                @Override
                protected void onPostExecute(Boolean flagGenerado) {
                    progressDialog.dismiss();
                    if (flagGenerado && !nombrePDF.equals("")) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(DetallePedidoActivity.this);
                        builder.setTitle("Documento obtenido");
                        builder.setIcon(R.drawable.ic_dialog_check);
                        builder.setMessage("El documento fue obtenido y se genereó el PDF");
                        builder.setPositiveButton("ACEPTAR", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                PDFUtil.abrirPDF(DetallePedidoActivity.this, nombrePDF);
                            }
                        });
                        builder.show();
                    } else {
                        String mensajeError = "Ocurrió algun problema al intentar obtener el documento";
                        if (error != "") {
                            mensajeError = error;
                        }
                        AlertDialog.Builder builder = new AlertDialog.Builder(DetallePedidoActivity.this);
                        builder.setTitle("No se pudo obtener el documento");
                        builder.setIcon(R.drawable.ic_dialog_error);
                        builder.setMessage(mensajeError);
                        builder.setPositiveButton("ACEPTAR", null);
                        builder.show();
                    }

                }
            }.execute();
        }
    }
}
