package com.expediodigital.ventas360.view;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import androidx.annotation.NonNull;
import com.google.android.material.textfield.TextInputEditText;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.expediodigital.ventas360.DAO.DAOConfiguracion;
import com.expediodigital.ventas360.R;
import com.expediodigital.ventas360.Ventas360App;
import com.expediodigital.ventas360.util.GmailSender;
import com.expediodigital.ventas360.util.Util;


import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Kevin Robinson Meza Hinostroza on julio 2017.
 * Expedio Digital
 * Kevin.Meza@expediodigital.com
 */

public class BackupActivity extends AppCompatActivity {
    private final static String TAG = "BackupActivity";
    private final int REQUEST_PERMISOS_ALMACENAMIENTO = 1;
    private final String CORREO_SOPORTE = "soporte@expediodigital.com";
    private final String CORREO_FROM = "api.expediodigital@gmail.com";
    private final String PASS_FROM = "[]@[paswordx]";//{{passwordisnull}}

    Button btn_generarBackup,btn_cargarBackup, btn_enviarBackup;

    TextView tv_ruta,tv_folder;
    TextInputEditText edt_descripcion;
    LinearLayout linear_arriba;
    ListView dialog_ListView;

    String defaultRuta = "";
    String nombreVendedor = "", idVendedor = "", nombreEmpresa = "", rucEmpresa = "", correo = CORREO_SOPORTE;
    Ventas360App ventas360App;
    DAOConfiguracion daoConfiguracion;

    File root;
    File curFolder;
    File seleccionado;
    private List<String> fileList = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_backup);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Util.actualizarToolBar("Backups",true,this);

        ventas360App = (Ventas360App) getApplicationContext();
        daoConfiguracion = new DAOConfiguracion(getApplicationContext());

        btn_generarBackup = (Button) findViewById(R.id.btn_generarBackup);
        btn_cargarBackup = (Button) findViewById(R.id.btn_cargarBackup);
        btn_enviarBackup = (Button) findViewById(R.id.btn_enviarBackup);
        tv_ruta = (TextView) findViewById(R.id.tv_ruta);
        edt_descripcion = (TextInputEditText) findViewById(R.id.edt_descripcion);

        defaultRuta = tv_ruta.getText().toString();

        correo = daoConfiguracion.getCorreoSoporte();
        if (correo.isEmpty()){
            correo = CORREO_SOPORTE;
        }

        idVendedor = ventas360App.getIdVendedor();
        nombreVendedor = ventas360App.getNombreVendedor();
        rucEmpresa = ventas360App.getRucEmpresa();
        nombreEmpresa = daoConfiguracion.getNombreEmpresa(rucEmpresa);

        btn_generarBackup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (Build.VERSION.SDK_INT >= 23) {
                    if (ContextCompat.checkSelfPermission(BackupActivity.this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED
                            || ContextCompat.checkSelfPermission(BackupActivity.this, android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                        //FragmentCompat.requestPermissions(permissionsList, RequestCode); Para Fragments
                        ActivityCompat.requestPermissions(BackupActivity.this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE, android.Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_PERMISOS_ALMACENAMIENTO);
                    } else {
                        if (Util.backupdDatabase(getApplicationContext())){
                            Toast.makeText(getApplicationContext(), "Backup generado", Toast.LENGTH_SHORT).show();
                        }else{
                            Toast.makeText(getApplicationContext(), "No se pudo generar backup", Toast.LENGTH_SHORT).show();
                        }
                    }
                } else {
                    if (Util.backupdDatabase(getApplicationContext())){
                        Toast.makeText(getApplicationContext(), "Backup generado", Toast.LENGTH_SHORT).show();
                    }else{
                        Toast.makeText(getApplicationContext(), "No se pudo generar backup", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        btn_cargarBackup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //showDialog(0);
                showDialogCargar();
            }
        });

        btn_enviarBackup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (defaultRuta.equals(tv_ruta.getText().toString())) {
                    Toast.makeText(getApplicationContext(), "Adjunte un archivo (backup)", Toast.LENGTH_SHORT).show();
                }else{
                    EnviarBackUp_correo();
                }
            }
        });

        root = new File(Environment.getExternalStorageDirectory().getAbsolutePath());
        curFolder = new File(Environment.getExternalStorageDirectory().getAbsolutePath()+File.separator+getResources().getString(R.string.Ventas360App_backups));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_PERMISOS_ALMACENAMIENTO){
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (Util.backupdDatabase(getApplicationContext())){
                    Toast.makeText(getApplicationContext(), "Backup generado", Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(getApplicationContext(), "No se pudo generar backup", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "No se otorgaron permisos de almacenamiento para realizar el Backup", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void showDialogCargar() {
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View dialog = inflater.inflate(R.layout.dialog_seleccionar_archivo,null);

        final AlertDialog builder = new AlertDialog.Builder(BackupActivity.this).create();
        builder.setView(dialog);
        builder.setCancelable(true);

        tv_folder = dialog.findViewById(R.id.tv_folder);
        linear_arriba = dialog.findViewById(R.id.linear_arriba);

        linear_arriba.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ListDir(curFolder.getParentFile());
            }
        });

        // Prepare ListView in dialog
        dialog_ListView = (ListView) dialog.findViewById(R.id.lv_hijos);
        dialog_ListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                File selected = new File(fileList.get(position));
                if (selected.isDirectory()) {
                    ListDir(selected);
                } else {
                    builder.dismiss();
                    //Bitmap bm = BitmapFactory.decodeFile(selected.getAbsolutePath());
                    //image.setImageBitmap(bm);
                    seleccionado = selected;
                    tv_ruta.setText(Environment.getExternalStorageDirectory().getAbsolutePath()+File.separator+getResources().getString(R.string.Ventas360App_backups)+File.separator+seleccionado.toString()+"");
                    Toast.makeText(getApplicationContext(), "Se cargó el backup correctamente", Toast.LENGTH_SHORT).show();
                }

            }
        });
        ListDir(curFolder);
        builder.show();
    }

    protected void EnviarBackUp_correo() {
        final String emailFrom = CORREO_FROM;
        final String password = PASS_FROM;
        final String emailTo = correo;
        final String aliasSender = nombreVendedor+ " ("+idVendedor+")";

        final String subject = "Envio de BackUp Ventas360App-"+nombreEmpresa+":"+nombreVendedor+ " ("+idVendedor+")";
        final String body =
                "Empresa: "+nombreEmpresa+ " | "+rucEmpresa+
                        "\nnombre Vendedor: "+nombreVendedor+
                        "\n(ID)Código: "+idVendedor+
                        "\nBackup adjunto: "+tv_ruta.getText().toString()+
                        "\n\nInconveniente:"+
                        "\n"+edt_descripcion.getText().toString();

        Log.d(TAG, body+"\ncorreo: "+emailTo);
        final String rutaBackup = tv_ruta.getText().toString();

        new AsyncTask<Void, Void, Void>() {
            ProgressDialog progressDialog = new ProgressDialog(BackupActivity.this);
            boolean flagEnviado = false;

            @Override
            protected void onPreExecute()
            {
                progressDialog.setMessage("Enviando correo...");
                progressDialog.setIndeterminate(true);
                progressDialog.show();
            }

            @Override
            protected Void doInBackground(Void... params)
            {
                try {
                    GmailSender email = new GmailSender(emailFrom, password);
                    email.addAttachment(rutaBackup, "BackUp");
                    email.sendMail(subject,body,emailFrom,aliasSender,emailTo);
                    flagEnviado = true;
                } catch (Exception e) {
                    Log.e(TAG + " Mail","Ocurrió un problema al enviar email, con los datos: ");
                    flagEnviado = false;
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void res)
            {
                progressDialog.dismiss();
                if (flagEnviado) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(BackupActivity.this);
                    builder.setTitle("Correo enviado");
                    builder.setIcon(R.drawable.ic_dialog_check);
                    builder.setMessage("El correo fue enviado correctamente, comuníquese con el administrador para que sea revisado");
                    builder.setPositiveButton("ACEPTAR", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            tv_ruta.setText("");
                            edt_descripcion.setText("");
                            //finish();
                        }
                    });
                    builder.show();
                }else{
                    AlertDialog.Builder builder = new AlertDialog.Builder(BackupActivity.this);
                    builder.setTitle("Correo no enviado");
                    builder.setIcon(R.drawable.ic_dialog_error);
                    builder.setMessage("Ocurrió un problema al enviar el correo");
                    builder.setPositiveButton("ACEPTAR", null);
                    builder.show();
                }

            }
        }.execute();

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home){
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    /*
    @Override
    protected Dialog onCreateDialog(int id) {
        Dialog dialog = null;

        switch (id) {
            case 0:
                dialog = new Dialog(BackupActivity.this);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setContentView(R.layout.dialog_seleccionar_archivo);
                dialog.setCancelable(true);
                dialog.setCanceledOnTouchOutside(true);

                tv_folder = (TextView) dialog.findViewById(R.id.tv_folder);
                linear_arriba = (LinearLayout) dialog.findViewById(R.id.linear_arriba);

                linear_arriba.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ListDir(curFolder.getParentFile());
                    }
                });

                // Prepare ListView in dialog
                dialog_ListView = (ListView) dialog.findViewById(R.id.lv_hijos);
                dialog_ListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view,
                                            int position, long id) {

                        File selected = new File(fileList.get(position));
                        if (selected.isDirectory()) {
                            ListDir(selected);
                        } else {
                            dismissDialog(0);

                            //Bitmap bm = BitmapFactory.decodeFile(selected.getAbsolutePath());
                            //image.setImageBitmap(bm);
                            seleccionado = selected;
                            tv_ruta.setText(seleccionado.toString()+"");
                            Toast.makeText(getApplicationContext(),"Backup cargado",Toast.LENGTH_SHORT).show();
                        }

                    }
                });

                break;
        }

        return dialog;
    }

    @Override
    protected void onPrepareDialog(int id, Dialog dialog, Bundle bundle) {
        // TODO Auto-generated method stub
        super.onPrepareDialog(id, dialog, bundle);

        switch (id) {
            case 0:
                ListDir(curFolder);
                break;
        }

    }
    */


    void ListDir(File f) {
        try{

            if (f.equals(root)) {
                linear_arriba.setEnabled(false);
            } else {
                linear_arriba.setEnabled(true);
            }

            curFolder = f;
            tv_folder.setText(f.getPath());

            File[] files = f.listFiles();
            fileList.clear();
            for (File file : files) {

                if (file.isDirectory()) {
                    fileList.add(file.getPath());
                } else {
                    Uri selectedUri = Uri.fromFile(file);
                    String fileExtension = MimeTypeMap.getFileExtensionFromUrl(selectedUri.toString());
                    if (fileExtension.equalsIgnoreCase("db")) {
                        fileList.add(file.getName());
                    }
                }
            }

            ArrayAdapter<String> directoryList = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1, fileList);
            dialog_ListView.setAdapter(directoryList);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    void deleteFiles(){
        Uri uri = null;
        String[] projection = new String[]{
                MediaStore.Files.FileColumns._ID,
                MediaStore.Files.FileColumns.DATA,
                MediaStore.Files.FileColumns.DATE_ADDED,
                MediaStore.Files.FileColumns.MIME_TYPE
        };
        Uri uri_data = Uri.parse("content://"+Environment.getExternalStorageDirectory().getAbsolutePath()+File.separator+getResources().getString(R.string.Ventas360App_backups));
        Cursor cursor = getContentResolver()
                .query(uri_data, projection, null,
                        null, MediaStore.Images.ImageColumns.DATE_TAKEN + " DESC");

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                if(true) {
                    String imageLocation = cursor.getString(1);
                    uri = Uri.parse(imageLocation);
                } else {
                    cursor.moveToNext();
                    String imageLocation = cursor.getString(1);
                    uri = Uri.parse(imageLocation);
                }
            }
            cursor.close();
        }
        //return uri;
    }
}
