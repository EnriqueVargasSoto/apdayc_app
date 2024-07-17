package com.expediodigital.ventas360.view;

import android.content.DialogInterface;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.expediodigital.ventas360.DAO.DAOCliente;
import com.expediodigital.ventas360.DAO.DAOConfiguracion;
import com.expediodigital.ventas360.R;
import com.expediodigital.ventas360.Ventas360App;
import com.expediodigital.ventas360.model.ClienteRegistro;
import com.expediodigital.ventas360.util.Util;

import java.util.ArrayList;
import java.util.HashMap;

public class NuevoClienteActivity extends AppCompatActivity {
    private final String TAG =getClass().getName();
    private final int ACCION_NUEVO_CLIENTE = 1;
    private final int ACCION_EDITAR_CLIENTE = 2;
    private TextInputLayout til_nombres, til_apellidoPaterno, til_apellidoMaterno, til_rucDni, til_telefono, til_direccion, til_distrito, til_modulo;
    private TextInputEditText edt_nombres, edt_apellidoPaterno, edt_apellidoMaterno, edt_rucDni, edt_telefono, edt_direccion, edt_distrito, edt_modulo;
    private Spinner spn_subGiro, spn_ruta;
    private DAOCliente daoCliente;
    private DAOConfiguracion daoConfiguracion;

    private ArrayList<HashMap<String,String>> listaSubGiros;
    private ArrayList<String> listaRutas;
    private int ACCION_CLIENTE;

    private Ventas360App ventas360App;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nuevo_cliente);

        ventas360App = (Ventas360App) getApplicationContext();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Util.actualizarToolBar("Nuevo Cliente",true,this, R.drawable.ic_action_close);

        daoCliente = new DAOCliente(getApplicationContext());
        daoConfiguracion = new DAOConfiguracion(getApplicationContext());

        til_nombres         = (TextInputLayout) findViewById(R.id.til_nombres);
        til_apellidoPaterno = (TextInputLayout) findViewById(R.id.til_apellidoPaterno);
        til_apellidoMaterno = (TextInputLayout) findViewById(R.id.til_apellidoMaterno);
        til_rucDni          = (TextInputLayout) findViewById(R.id.til_rucDni);
        til_telefono        = (TextInputLayout) findViewById(R.id.til_telefono);
        til_direccion       = (TextInputLayout) findViewById(R.id.til_direccion);
        til_distrito        = (TextInputLayout) findViewById(R.id.til_distrito);
        til_modulo          = (TextInputLayout) findViewById(R.id.til_modulo);

        edt_nombres         = (TextInputEditText) findViewById(R.id.edt_nombres);
        edt_apellidoPaterno = (TextInputEditText) findViewById(R.id.edt_apellidoPaterno);
        edt_apellidoMaterno = (TextInputEditText) findViewById(R.id.edt_apellidoMaterno);
        edt_rucDni          = (TextInputEditText) findViewById(R.id.edt_rucDni);
        edt_telefono        = (TextInputEditText) findViewById(R.id.edt_telefono);
        edt_direccion       = (TextInputEditText) findViewById(R.id.edt_direccion);
        edt_distrito        = (TextInputEditText) findViewById(R.id.edt_distrito);
        edt_modulo          = (TextInputEditText) findViewById(R.id.edt_modulo);
        spn_subGiro         = (Spinner) findViewById(R.id.spn_subGiro);
        spn_ruta            = (Spinner) findViewById(R.id.spn_ruta);

        cargarFormulario();
    }

    private void cargarFormulario() {
        listaSubGiros = daoCliente.getSubGiros();
        ArrayList<String> arraySubGiro = new ArrayList<>();
        for (HashMap<String,String> item : listaSubGiros){
            arraySubGiro.add(item.get("descripcion"));
        }
        ArrayAdapter adapter = new ArrayAdapter<String>(this,R.layout.my_spinner_item,arraySubGiro);
        spn_subGiro.setAdapter(adapter);

        listaRutas = daoConfiguracion.getRutasVendedor(ventas360App.getIdEmpresa(), ventas360App.getIdSucursal(), ventas360App.getIdVendedor());
        ArrayAdapter adapterR = new ArrayAdapter<String>(this,R.layout.my_spinner_item,listaRutas);
        spn_ruta.setAdapter(adapterR);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_check ,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id){
            case R.id.menu_aceptar:
                if (validarFormulario()){
                    registrarCliente();
                }
                break;
            case android.R.id.home:
                DialogoConfirmacion();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void registrarCliente() {
        String newIdClienteTemp = "";
        String maxIdClienteTemp = daoCliente.getMaximoIdClienteTemp(ventas360App.getIdVendedor());
        if (maxIdClienteTemp.equals("")){
            newIdClienteTemp = ventas360App.getIdVendedor() + "1";
        }else{
            newIdClienteTemp = ventas360App.getIdVendedor() +
                    (Integer.parseInt(maxIdClienteTemp.substring(ventas360App.getIdVendedor().length(),maxIdClienteTemp.length())) + 1);
        }
        Log.i(TAG,"generado newIdClienteTemp: "+newIdClienteTemp);

        ClienteRegistro clienteRegistro = new ClienteRegistro();
        clienteRegistro.setIdClienteTemp(newIdClienteTemp);
        clienteRegistro.setNombres(edt_nombres.getText().toString().trim());
        clienteRegistro.setApellidoPaterno(edt_apellidoPaterno.getText().toString().trim());
        clienteRegistro.setApellidoMaterno(edt_apellidoMaterno.getText().toString().trim());
        clienteRegistro.setRucDni(edt_rucDni.getText().toString().trim());
        clienteRegistro.setTelefono(edt_telefono.getText().toString().trim());
        clienteRegistro.setDireccion(edt_direccion.getText().toString().trim());
        clienteRegistro.setDistrito(edt_distrito.getText().toString().trim());
        clienteRegistro.setIdSubGiro(listaSubGiros.get(spn_subGiro.getSelectedItemPosition()).get("idSubGiro"));
        clienteRegistro.setIdRuta(listaRutas.get(spn_ruta.getSelectedItemPosition()));
        clienteRegistro.setIdModulo(edt_modulo.getText().toString().trim());

        clienteRegistro.setIdVendedor(ventas360App.getIdVendedor());
        clienteRegistro.setFechaRegistro(Util.getFechaHoraTelefonoString_formatoSql());
        daoCliente.registrarCliente(clienteRegistro);

    }

    @Override
    public void onBackPressed() {
        DialogoConfirmacion();
    }

    private boolean validarFormulario() {
        boolean flag = true;
        til_nombres.setError(null);
        til_apellidoPaterno.setError(null);
        til_apellidoMaterno.setError(null);
        til_rucDni.setError(null);
        til_telefono.setError(null);
        til_direccion.setError(null);
        til_distrito.setError(null);
        til_modulo.setError(null);

        if (edt_nombres.getText().toString().trim().isEmpty()) {
            til_nombres.setError("Campo requerido");
            flag = false;
        }
        if (edt_apellidoPaterno.getText().toString().trim().isEmpty()) {
            til_apellidoPaterno.setError("Campo requerido");
            flag = false;
        }
        if (edt_apellidoMaterno.getText().toString().trim().isEmpty()) {
            til_apellidoMaterno.setError("Campo requerido");
            flag = false;
        }
        if (edt_rucDni.getText().toString().isEmpty()) {
            til_rucDni.setError("Campo requerido");
            flag = false;
        }else{
            if (edt_rucDni.getText().toString().length() != 8 && edt_rucDni.getText().toString().length() != 11) {
                til_rucDni.setError("Ingrese un RUC o DNI válido");
                flag = false;
            }
        }

        if (edt_telefono.getText().toString().isEmpty()) {
            til_telefono.setError("Campo requerido");
            flag = false;
        }
        if (edt_direccion.getText().toString().trim().isEmpty()) {
            til_direccion.setError("Campo requerido");
            flag = false;
        }
        if (edt_distrito.getText().toString().trim().isEmpty()) {
            til_distrito.setError("Campo requerido");
            flag = false;
        }
        if (spn_subGiro.getSelectedItemPosition() == AdapterView.INVALID_POSITION) {
            Toast.makeText(this, "Sub giro del negocio requerido", Toast.LENGTH_SHORT).show();
            flag = false;
        }
        if (spn_ruta.getSelectedItemPosition() == AdapterView.INVALID_POSITION) {
            Toast.makeText(this, "Ruta requerida", Toast.LENGTH_SHORT).show();
            flag = false;
        }
        if (edt_modulo.getText().toString().isEmpty()){
            til_modulo.setError("Campo requerido");
            flag = false;
        }

        return flag;
    }

    private void DialogoConfirmacion() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        if (ACCION_CLIENTE == ACCION_NUEVO_CLIENTE){
            builder.setTitle("Descartar cliente");
            builder.setMessage("No se guardarán los datos del cliente");
        }else{
            builder.setTitle("Descartar cambios");
            builder.setMessage("Se perderán los cambios en el cliente");
        }
        builder.setNegativeButton("CANCELAR", null);
        builder.setPositiveButton("DESCARTAR", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog,int which) {
                if (ACCION_CLIENTE == ACCION_NUEVO_CLIENTE){

                }
                finish();
            }
        });
        builder.show();

    }
}
