package com.expediodigital.ventas360;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;

import com.expediodigital.ventas360.util.ManageSincronizacion;
import com.expediodigital.ventas360.util.Utilities;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.expediodigital.ventas360.DAO.DAOConfiguracion;
import com.expediodigital.ventas360.model.VendedorModel;
import com.expediodigital.ventas360.view.ConfiguracionActivity;
import com.expediodigital.ventas360.view.MenuPrincipalActivity;

public class LoginActivity extends AppCompatActivity {
    TextInputEditText edt_ruc,edt_usuario,edt_password;
    FloatingActionButton btn_login;
    DAOConfiguracion daoConfiguracion;
    TextView tv_settings;
    Ventas360App ventas360App;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(ContextCompat.getColor(getApplicationContext(), R.color.cyan_500));
        }

        Typeface myTypeface = Typeface.createFromAsset(getApplicationContext().getAssets(), "fonts/prime_regular.otf");
        TextView tv_company = (TextView) findViewById(R.id.tv_company);
        tv_company.setTypeface(myTypeface);

        daoConfiguracion = new DAOConfiguracion(getApplicationContext());
        ventas360App = (Ventas360App) getApplicationContext();

        edt_ruc = (TextInputEditText) findViewById(R.id.edt_ruc);
        edt_usuario = (TextInputEditText) findViewById(R.id.edt_usuario);
        edt_password = (TextInputEditText) findViewById(R.id.edt_password);
        tv_settings = (TextView) findViewById(R.id.tv_settings);
        btn_login = (FloatingActionButton) findViewById(R.id.fab_login);
        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (validarCampos()){
                    validarUsuario();
                }
            }
        });


        tv_settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent configuracion = new Intent(LoginActivity.this,ConfiguracionActivity.class);
                configuracion.putExtra("origen", ConfiguracionActivity.ORIGEN_LOGIN);
                startActivity(configuracion);
            }
        });

        edt_ruc.setText(ventas360App.getRucEmpresa());
        edt_usuario.setText(ventas360App.getUsuario());
    }

    private boolean validarCampos() {
        if (edt_ruc.getText().toString().isEmpty()){
            Toast.makeText(getApplicationContext(),"Ingrese el ruc de su empresa",Toast.LENGTH_SHORT).show();
            edt_ruc.requestFocus();
            return false;
        }
        if (edt_usuario.getText().toString().isEmpty()){
            edt_usuario.requestFocus();
            return false;
        }
        if (edt_password.getText().toString().isEmpty()){
            edt_password.requestFocus();
            return false;
        }
        return true;
    }

    private void validarUsuario() {
        final String ruc = edt_ruc.getText().toString();
        final String user = edt_usuario.getText().toString();
        final String password = edt_password.getText().toString();

        final VendedorModel vendedorModel = daoConfiguracion.getVendedorUsuario(user,password,ruc);

        if (vendedorModel != null){
            //Si se ingresa con otro vendedor, se debe limpiar la base de datos
            if(!( ventas360App.getIdVendedor().equals(vendedorModel.getIdVendedor()) && ventas360App.getIdSucursal().equals(vendedorModel.getIdSucursal()) && ventas360App.getIdEmpresa().equals(vendedorModel.getIdEmpresa()) )){
                daoConfiguracion.limpiarTablas();
            }

            ventas360App.setRucEmpresa(ruc);
            ventas360App.setUsuario(user);
            ventas360App.setPassword(password);
            ventas360App.setIdEmpresa(vendedorModel.getIdEmpresa());
            ventas360App.setIdSucursal(vendedorModel.getIdSucursal());

            ventas360App.setIdVendedor(vendedorModel.getIdVendedor());
            ventas360App.setNombreVendedor(vendedorModel.getNombre());
            ventas360App.setSerieVendedor(vendedorModel.getSerie());
            ventas360App.setTipoVendedor(vendedorModel.getTipo());
            ventas360App.setIdAlmacen(vendedorModel.getIdAlmacen());
            ventas360App.setModoVenta(vendedorModel.getModoVenta());

            ventas360App.setSesionActiva(true);

            //si tiene internet
            Utilities utilities = Utilities.getInstance(this);
            if(utilities.isNetworkAvailable())
            {
                final ProgressDialog mProgressDialog;
                mProgressDialog = ProgressDialog.show(this, null, null);
                mProgressDialog.setContentView(R.layout.progress_loader);
                mProgressDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                mProgressDialog.setCancelable(false);

                ManageSincronizacion ms0 = new ManageSincronizacion(this, null, ManageSincronizacion.ORIGEN_MENU, null, null, 3, mProgressDialog,null);
                ms0.iniciarSincronizacion();

//                Handler handler = new Handler();
//                Runnable runnable = new Runnable() {
//                    @Override
//                    public void run() {
//                        if (mProgressDialog != null && mProgressDialog.isShowing())
//                            mProgressDialog.dismiss();
//                        Intent intent = new Intent(LoginActivity.this, MenuPrincipalActivity.class);
//                        startActivity(intent);
//                    }
//                };
//                handler.postDelayed(runnable, 1000);
            }
            else{
                Intent intent = new Intent(LoginActivity.this, MenuPrincipalActivity.class);
                startActivity(intent);
            }

        } else {
            Toast.makeText(LoginActivity.this, "Usuario no registrado", Toast.LENGTH_SHORT).show();
        }
    }
}
