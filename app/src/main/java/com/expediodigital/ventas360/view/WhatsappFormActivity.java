package com.expediodigital.ventas360.view;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.expediodigital.ventas360.R;
import com.expediodigital.ventas360.util.APIClient;
import com.expediodigital.ventas360.util.APIInterface;
import com.expediodigital.ventas360.util.DataBaseHelper;
import com.expediodigital.ventas360.util.SoapManager;
import com.expediodigital.ventas360.util.TablesHelper;
import com.expediodigital.ventas360.util.Util;
import com.google.android.material.textfield.TextInputEditText;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class WhatsappFormActivity extends AppCompatActivity {

    TextInputEditText editEmail, editTelefono, editCodCiudad, editFijo;
    String numero, email, ciudad, fijo, idClient;

    SoapManager soap_manager;
    ProgressDialog progressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_whatsapp_form);
        editEmail = findViewById(R.id.editNombre);
        editTelefono = findViewById(R.id.editTelefono);
        editCodCiudad = findViewById(R.id.editCiudad);
        editFijo = findViewById(R.id.editTelefonoFijo);
        soap_manager = new SoapManager(getApplicationContext());

        final ImageButton btnWhatsapp = findViewById(R.id.btnWhatsapp);
        btnWhatsapp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!editTelefono.getText().toString().isEmpty() && editTelefono.getText().toString().length() == 9)
                {
                    String codeCountry = "+51";
                    String url = "https://api.whatsapp.com/send?phone="+codeCountry+editTelefono.getText().toString();
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setData(Uri.parse(url));
                    startActivity(i);
                }
                else{
                    editTelefono.setError("Indique el número del cliente para contactarnos por whatsapp");
                }
            }
        });

        ImageView imgBack = findViewById(R.id.imgBack);
        imgBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        editTelefono.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if(!editTelefono.getText().toString().isEmpty() && editTelefono.getText().toString().length() == 9)
                {
                    btnWhatsapp.setImageDrawable(getResources().getDrawable(R.drawable.ic_whatsapp));
                }
                else{
                    btnWhatsapp.setImageDrawable(getResources().getDrawable(R.drawable.ic_whatsapp_off));
                }
            }
        });

        Bundle bundle = getIntent().getExtras();
        if(bundle!=null){
            idClient = bundle.getString("idClient");
            email = bundle.getString("email","");
            numero = bundle.getString("whatsapp","");
            ciudad = bundle.getString("ciudad","");
            fijo = bundle.getString("fijo","");

            editTelefono.setText(numero);
            editEmail.setText(email);
            editCodCiudad.setText(ciudad);
            editFijo.setText(fijo);
        }

        Button btnGuardar = findViewById(R.id.btnGuardar);
        btnGuardar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(editEmail.getText().toString().isEmpty()){
                    editEmail.setError("Ingrese el nombre");
                    return;
                }

                if(editTelefono.getText().toString().isEmpty()){
                    editTelefono.setError("Ingrese el numero telefónico");
                    return;
                }

                if(editCodCiudad.getText().toString().isEmpty()){
                    editCodCiudad.setError("Ingrese el codigo de la ciudad");
                    return;
                }

                if(editFijo.getText().toString().isEmpty()){
                    editFijo.setError("Ingrese el numero del telefono fijo");
                    return;
                }

                send_server();
            }
        });
    }

    private void send_server()
    {
        (new AsyncSend()).execute("");
    }

    class AsyncSend extends AsyncTask<String, String, String> {
        String mensajeSincronizacion = "Sincronizando....";
        DataBaseHelper dataBaseHelper;

        protected void onPreExecute() {
            progressDialog = new ProgressDialog(WhatsappFormActivity.this);
            progressDialog.setTitle("");
            progressDialog.setMessage(mensajeSincronizacion);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            progressDialog.setIndeterminate(false);
            progressDialog.setCancelable(false);
            progressDialog.show();

            dataBaseHelper = DataBaseHelper.getInstance(WhatsappFormActivity.this);
        }

        @Override
        protected String doInBackground(String... strings) {
            try {
                if (Util.isConnectingToInternet()) {
                    int rpta = soap_manager.registroWhatsappJSON(TablesHelper.ClienteWathsapp.Update, TablesHelper.ClienteWathsapp.Table, idClient, editTelefono.getText().toString(),
                            editCodCiudad.getText().toString(), editFijo.getText().toString(), editEmail.getText().toString());

                    if(rpta == 1){
                        insertOnTable();
                        return "1";
                    }
                    else{
                        return "Error al registrar los datos de contacto";
                    }
                }
                else {
                    return "NoConnectedToInternet";
                }
            }
            catch (NoSuchMethodException ex) {
                ex.printStackTrace();
                return "NoSuchMethodException";
            } catch (SocketTimeoutException ex) {
                ex.printStackTrace();
                return "SocketTimeoutException";
            } catch (IOException ex) {
                ex.printStackTrace();
                return ex.getMessage();
            } catch (Exception e) {
                e.printStackTrace();
                return e.getMessage();
            }
        }

        void insertOnTable() {

            String rawQuery = "SELECT COUNT(*) FROM " + TablesHelper.ClienteWathsapp.Table + " WHERE " + TablesHelper.ClienteWathsapp.idCliente + " = ?";
            String[] args = { idClient };
            SQLiteDatabase db = dataBaseHelper.getReadableDatabase();
            Cursor cur = db.rawQuery(rawQuery, args);
            Util.LogCursorInfo(cur, WhatsappFormActivity.this);
            cur.moveToFirst();
            db.close();

            if(cur.getInt(0)>0)
            {
                SQLiteDatabase db5 = dataBaseHelper.getWritableDatabase();
                String where = TablesHelper.ClienteWathsapp.idCliente + " = ?";
                ContentValues updateValues = new ContentValues();
                updateValues.put("codigociudad", editCodCiudad.getText().toString());
                updateValues.put("email", editEmail.getText().toString());
                updateValues.put("telefonofijo", editFijo.getText().toString());
                updateValues.put("whathsapp", editTelefono.getText().toString());
                db5.update(TablesHelper.ClienteWathsapp.Table, updateValues, where, args );

                String rawQuery3 = "SELECT * FROM " + TablesHelper.ClienteWathsapp.Table + " WHERE " + TablesHelper.ClienteWathsapp.idCliente + " = ?";
                SQLiteDatabase db3 = dataBaseHelper.getReadableDatabase();
                Cursor cur3 = db3.rawQuery(rawQuery3, args);
                Util.LogCursorInfo(cur3, WhatsappFormActivity.this);
                cur3.moveToFirst();
                db3.close();
            }
            else{
                Date currentTime = Calendar.getInstance().getTime();
                String str_fecha = DateFormat.format("yyyy-MM-dd HH:mm:ss", currentTime).toString();
                String rawQuery2 = "INSERT INTO " + TablesHelper.ClienteWathsapp.Table +
                        " ('idCliente','codigociudad','email','fechaRegistro','telefonofijo','whathsapp') VALUES ( " +
                        "'"+ idClient +"', "+"'"+ editCodCiudad.getText().toString() +"', "+"'"+ editEmail.getText().toString() +"', "+"'"+ str_fecha +"', "+
                        "'"+ editFijo.getText().toString() +"', "+"'"+ editTelefono.getText().toString() +"' )";

                SQLiteDatabase db2 = dataBaseHelper.getWritableDatabase();
                db2.execSQL(rawQuery2);
                db2.close();
            }
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            progressDialog.dismiss();

            if(s.equals("1")){
                AlertDialog.Builder builder = new AlertDialog.Builder(WhatsappFormActivity.this);
                builder.setTitle("¡Felicidades!");
                builder.setMessage("El numero de whatsapp se guardó con éxito");
                builder.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        setResult(RESULT_OK);
                        finish();
                    }
                });
                builder.show();
            }
            else{
                AlertDialog.Builder builder = new AlertDialog.Builder(WhatsappFormActivity.this);
                builder.setTitle("¡Error?");
                builder.setMessage(s);
                builder.setPositiveButton("Entendido", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });
                builder.show();
            }
        }
    }
}