package com.expediodigital.ventas360.view.fragment;


import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import androidx.annotation.Nullable;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import androidx.fragment.app.Fragment;
import androidx.appcompat.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.Spinner;
import android.widget.Toast;

import com.expediodigital.ventas360.DAO.DAOCliente;
import com.expediodigital.ventas360.DAO.DAOConfiguracion;
import com.expediodigital.ventas360.DAO.DAOPedido;
import com.expediodigital.ventas360.R;
import com.expediodigital.ventas360.Ventas360App;
import com.expediodigital.ventas360.model.FormaPagoModel;
import com.expediodigital.ventas360.model.PedidoCabeceraModel;
import com.expediodigital.ventas360.util.Util;
import com.expediodigital.ventas360.view.PedidoActivity;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;

/**
 * A simple {@link Fragment} subclass.
 */
public class PedidoCabeceraFragment extends Fragment {
    public static final String TAG = "PedidoCabeceraFragment";
    public TextInputEditText edt_numeroPedido,edt_direccion,edt_fechaEntrega,edt_limiteCredito,edt_observaciones;
    private TextInputLayout text_input_limiteCredito;
    DecimalFormat formateador;
    Spinner spn_formaPago;

    DAOConfiguracion daoConfiguracion;
    DAOPedido daoPedido;
    DAOCliente daoCliente;

    ArrayList<FormaPagoModel> listaFormaPago;
    //String fechaActual, diaActual, mesActual, anioActual;
    String idCliente = "";
    String idVendedor = "";
    String serieVendedor = "";
    String numeroPedido;
    String horaInicio;
    double saldoCredito = 0;
    private int ACCION_PEDIDO;
    Ventas360App ventas360App;

    public PedidoCabeceraFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_pedido_cabecera, container, false);
        setHasOptionsMenu(false);//Indica que podrá manipular las opciones del actionBar desde este fragment

        daoConfiguracion = new DAOConfiguracion(getActivity());
        daoPedido = new DAOPedido(getActivity());
        daoCliente = new DAOCliente(getActivity());

        formateador = Util.formateador();

        edt_numeroPedido    = view.findViewById(R.id.edt_numeroPedido);
        edt_fechaEntrega    = view.findViewById(R.id.edt_fechaEntrega);
        edt_observaciones   = view.findViewById(R.id.edt_observaciones);
        edt_direccion       = view.findViewById(R.id.edt_direccion);
        spn_formaPago       = view.findViewById(R.id.spn_formaPago);
        text_input_limiteCredito = view.findViewById(R.id.text_input_limiteCredito);
        edt_limiteCredito   = view.findViewById(R.id.edt_limiteCredito);

        edt_fechaEntrega.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v,boolean hasFocus) {
                //Si la vista ha sido seleccionada mostrar el Picker
                if (hasFocus){
                    Calendar mcurrentTime = Calendar.getInstance();
                    int day = mcurrentTime.get(Calendar.DAY_OF_MONTH);
                    int month = mcurrentTime.get(Calendar.MONTH);
                    int year = mcurrentTime.get(Calendar.YEAR);

                    //Si el campo fecha ya tiene un valor, obtenemos los que ya estan seleccionados
                    if (!edt_fechaEntrega.getText().toString().isEmpty()){
                        try {
                            String fechaArray[] = edt_fechaEntrega.getText().toString().split("/");
                            day = Integer.parseInt(fechaArray[0]);
                            month = (Integer.parseInt(fechaArray[1])-1);
                            year = Integer.parseInt(fechaArray[2]);
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }

                    DatePickerDialog mDatePicker;
                    mDatePicker = new DatePickerDialog(getActivity(),R.style.AlertDialogApp ,new DatePickerDialog.OnDateSetListener() {
                        @Override
                        public void onDateSet(DatePicker view, int selectedYear, int selectedMonthOfYear, int selectedDayOfMonth) {
                            String selectedDay = String.valueOf(selectedDayOfMonth);
                            String selectedMonth =  String.valueOf(selectedMonthOfYear + 1);//Se le suma 1 porque el mes obtenido de la vista tiene un número menos por defecto

                            if (selectedDayOfMonth<10){
                                selectedDay = "0"+selectedDay;
                            }
                            if (selectedMonthOfYear + 1 <10){//Se le suma 1 porque el que nos da la vista tiene un número menos por defecto
                                selectedMonth = "0"+selectedMonth;
                            }
                            edt_fechaEntrega.setText(selectedDay + "/" + selectedMonth + "/" + selectedYear);
                            edt_fechaEntrega.clearFocus();//Quitamos el focus para que pueda volver a ser seleccionado
                        }
                    },year,month,day);

                    /*
                    Al cancelar el Picker, se debe limpiar el focus sobre el TextInputEditText, de lo contrario se quedará abierto (con el hint arriba)
                    y la proxima vez que se seleccione ya no ejecutarpa el evento "OnFocusChange" puesto que ya tiene el focus puesto
                    */
                    mDatePicker.setOnCancelListener(new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog) {
                            edt_fechaEntrega.clearFocus();
                            edt_numeroPedido.requestFocus();
                        }
                    });

                    //mDatePicker.setTitle("Fecha de entrega");
                    mDatePicker.show();
                }
            }
        });
        horaInicio = Util.getHoraTelefonoString();
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //Se obtienen los campos del Activity en este fragment
        this.idCliente = ((PedidoActivity)getActivity()).getIdClienteFromActivity();
        this.numeroPedido = ((PedidoActivity)getActivity()).getNumeroPedidoFromActivity();

        ventas360App = (Ventas360App) getActivity().getApplicationContext();
        idVendedor = ventas360App.getIdVendedor();
        serieVendedor = ventas360App.getSerieVendedor();

        //Cargar la fecha de sincronizacion por defecto
        edt_fechaEntrega.setText(daoConfiguracion.getFechaString());

        listaFormaPago = daoConfiguracion.getCondicionVenta();
        ArrayList<String> arrayFormaPago = new ArrayList<String>();
        for (FormaPagoModel fpago : listaFormaPago) {
            arrayFormaPago.add(fpago.getDescripcion());
        }
        ArrayAdapter adapter = new ArrayAdapter<String>(getActivity(),R.layout.my_spinner_item,arrayFormaPago);
        spn_formaPago.setAdapter(adapter);

        ACCION_PEDIDO = ((PedidoActivity)getActivity()).getACCION_PEDIDO();
        if (ACCION_PEDIDO  == PedidoActivity.ACCION_NUEVO_PEDIDO){
            String numeroMaximo = daoPedido.getMaximoNumeroPedido(idVendedor);
            String fechaActual = daoConfiguracion.getFechaString();

            if (serieVendedor.isEmpty()){
                showAlertError("No se obtuvo la serie del vendedor","Para registrar pedidos se debe tener la serie del vendedor, sincronice la aplicación y vuelva a intentarlo");
            }else{
                this.numeroPedido = Util.calcularSecuencia(numeroMaximo,fechaActual,serieVendedor);
                String numeropedidoSimple = numeroPedido.substring(6,numeroPedido.length());
                edt_numeroPedido.setText(numeropedidoSimple);
                edt_direccion.setText(daoCliente.getDireccionCliente(idCliente));
            }
        }else{
            //Si no debe ser VER_PEDIDO o EDITAR_PEDIDO
            //Los campos ocnumero,cliente ya han sido cargados desde PedidoActivity
            String numeropedidoSimple = numeroPedido.substring(6,numeroPedido.length());
            edt_numeroPedido.setText(numeropedidoSimple);
            cargarPedido();

            /*Si hay campos que condicionen tanto a los precios de los productos como despuesto y otros
            Estos deben ser inhabilitados ya que no se deben cambiar porque ya hay productos que han sido agregados
            y han sido condicionados por ellos */
            inhabilitarCamposCondicionales();

            if (ACCION_PEDIDO == PedidoActivity.ACCION_VER_PEDIDO){
                habilitarCampos(false);
            }else if (ACCION_PEDIDO == PedidoActivity.ACCION_EDITAR_PEDIDO){
                //Por defecto los campos están habilitados no es necesario utilizar habilitarCampos(true);
            }
        }

        saldoCredito = daoCliente.getSaldoCredito(idCliente, numeroPedido);
        setLimiteCredito(saldoCredito);
    }

    private void setLimiteCredito(double saldoCredito){
        if (saldoCredito == 0){
            spn_formaPago.setSelection(0);//Por defecto se selecciona contado
            spn_formaPago.setEnabled(false);
            text_input_limiteCredito.setVisibility(View.GONE);
        }else{
            text_input_limiteCredito.setVisibility(View.VISIBLE);
            edt_limiteCredito.setText("S/. "+formateador.format(saldoCredito));
        }

        ((PedidoActivity)getActivity()).setSaldoCredito(saldoCredito);
    }

    private void inhabilitarCamposCondicionales() {
        //edt_numeroPedido.setEnabled(false);
    }

    private void habilitarCampos(boolean estado) {
        //Estos TextInputEditText al no ser editables funcionan con el evento OnFocusChange
        edt_fechaEntrega.setFocusable(estado);
        //---------------------------------------------------------------------------------
        spn_formaPago.setEnabled(estado);
        edt_observaciones.setEnabled(estado);
    }

    private void cargarPedido() {
        PedidoCabeceraModel pedido = daoPedido.getPedidoCabecera(numeroPedido);
        if (pedido != null){
            edt_fechaEntrega.setText(pedido.getFechaEntrega().trim());

            for (int i = 0; i < listaFormaPago.size(); i++) {
                Log.d(TAG,"Forma de pago:"+listaFormaPago.get(i).getIdFormaPago()+" Pedido:"+pedido.getIdFormaPago());
                if (listaFormaPago.get(i).getIdFormaPago().equals(pedido.getIdFormaPago())) {
                    spn_formaPago.setSelection(i);
                    break;
                }
            }
            edt_observaciones.setText(pedido.getObservacion());
            edt_direccion.setText(daoCliente.getDireccionCliente(idCliente));
        }else{
            showAlertError("No se pudo cargar el pedido","Comuníquiese con el administrador");
        }
    }

    public boolean validarCampos(){
        String ERROR = "Campo requerido";

        if (edt_numeroPedido.getText().length() == 0){
            edt_numeroPedido.setError(ERROR);
            edt_numeroPedido.requestFocus();
            return false;
        }
        if (edt_fechaEntrega.getText().toString().matches("")){
            edt_fechaEntrega.setError(ERROR);
            edt_fechaEntrega.requestFocus();
            return false;
        }
        if (listaFormaPago.size() == 0){
            Toast.makeText(getActivity(),"No se tiene condicion de pago",Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    //Getters and Setters para el fragment, se puede pasar variables, listas, arrays, componentes (EditText, ListView, ...) , etc.
    public PedidoCabeceraModel getPedido(){
        PedidoCabeceraModel pedidoModel = new PedidoCabeceraModel();

        pedidoModel.setIdCliente(idCliente);
        pedidoModel.setNumeroPedido(this.numeroPedido);

        pedidoModel.setFechaEntrega(edt_fechaEntrega.getText().toString());
        pedidoModel.setIdFormaPago(listaFormaPago.get(spn_formaPago.getSelectedItemPosition()).getIdFormaPago());

        if (ACCION_PEDIDO == PedidoActivity.ACCION_NUEVO_PEDIDO) {
            pedidoModel.setFechaPedido(daoConfiguracion.getFechaString() + " " + horaInicio);
        }else if (ACCION_PEDIDO == PedidoActivity.ACCION_EDITAR_PEDIDO){
            Log.e(TAG,"ACTUALIZANDO ACCION EDITAR PEDIDO");
            pedidoModel.setFechaPedido(daoPedido.getFechaPedido(this.numeroPedido));
        }

        pedidoModel.setIdVendedor(idVendedor);
        pedidoModel.setEstado(PedidoCabeceraModel.ESTADO_GENERADO);
        pedidoModel.setFlag(PedidoCabeceraModel.FLAG_PENDIENTE);
        pedidoModel.setObservacion(edt_observaciones.getText().toString());

        return pedidoModel;
    }

    public String getIdCliente() {
        return idCliente;
    }

    /**
     * Este método normalmente se ejecuta cuando se selecciona a un cliente, se pasa el codigo al fragment y se muestra la lista de direcciones del mismo
     * @param idCliente
     */
    public void setIdCliente(String idCliente) {
        this.idCliente = idCliente;
        saldoCredito = daoCliente.getSaldoCredito(idCliente,numeroPedido);
        setLimiteCredito(saldoCredito);
        edt_direccion.setText(daoCliente.getDireccionCliente(idCliente));
    }

    public String getNumeroPedido(){
        return this.numeroPedido;
    }

    void showAlertError(String titulo,String mensaje){
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setIcon(R.drawable.ic_dialog_error);
        builder.setTitle(titulo);
        builder.setMessage(mensaje);
        builder.setCancelable(false);
        builder.setNegativeButton("ACEPTAR", new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int which) {
                getActivity().finish();
            }
        });
        builder.show();
    }
}
