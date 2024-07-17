package com.expediodigital.ventas360.view;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.ColorDrawable;
import android.media.ExifInterface;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.provider.Settings;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.appcompat.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.expediodigital.ventas360.DAO.DAOEncuesta;
import com.expediodigital.ventas360.R;
import com.expediodigital.ventas360.Ventas360App;
import com.expediodigital.ventas360.model.EncuestaAlternativaModel;
import com.expediodigital.ventas360.model.EncuestaDetallePreguntaModel;
import com.expediodigital.ventas360.model.EncuestaRespuestaDetalleModel;
import com.expediodigital.ventas360.model.EncuestaRespuestaModel;
import com.expediodigital.ventas360.service.GPSTracker;
import com.expediodigital.ventas360.util.APIClient;
import com.expediodigital.ventas360.util.APIInterface;
import com.expediodigital.ventas360.util.BitmapConverter;
import com.expediodigital.ventas360.util.ImageJavierDTO;
import com.expediodigital.ventas360.util.PermissionUtil;
import com.expediodigital.ventas360.util.SoapManager;
import com.expediodigital.ventas360.util.TablesHelper;
import com.expediodigital.ventas360.util.Util;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.xmlpull.v1.XmlPullParserException;

import java.io.File;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Kevin Robinson Meza Hinostroza on enero 2018.
 * Expedio Digital
 * Kevin.Meza@expediodigital.com
 */

public class EncuestaClienteActivity extends AppCompatActivity {
    public final String TAG = this.getClass().getName();

    private static final int REQUEST_CODE_CAMARA = 1;
    private static final int REQUEST_CODE_GALERIA = 2;
    private static final int REQUEST_CODE_UBICACION = 3;

    private static final int REQUEST_PERMISOS_CAMARA = 4;
    private static final int REQUEST_PERMISOS_UBICACION = 5;

    private final int LIMITE_ALTERNATIVAS_PARA_CHECK = 5;
    private List<EncuestaDetallePreguntaModel> listaPreguntas;
    private EncuestaRespuestaModel encuestaRespuestaModel;
    private List<EncuestaRespuestaDetalleModel> listaRespuestasDetalle;
    private int idPreguntaSeleccionada;
    private String idCliente;
    private String razonSocial;
    private String descripcionEncuesta;
    private String tipoEncuesta;
    private int idEncuesta;
    private int idEncuestaDetalle;
    private TextView tv_cliente,tv_descripcionEncuesta;

    LinearLayout linearEncuesta;
    ImageView imgViewSeleccionado;
    DAOEncuesta daoEncuesta;
    SoapManager soapManager;
    Ventas360App ventas360App;
    private String fotoPathTemp = "";
    private String fotoNameTemp = "";

    GPSTracker gpsTracker;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_encuesta_cliente);

        razonSocial         = getIntent().getExtras().getString("razonSocial","");
        idCliente           = getIntent().getExtras().getString("idCliente","");
        descripcionEncuesta = getIntent().getExtras().getString("descripcionEncuesta","");
        tipoEncuesta        = getIntent().getExtras().getString("tipoEncuesta","");
        idEncuesta          = getIntent().getExtras().getInt("idEncuesta",0);
        idEncuestaDetalle   = getIntent().getExtras().getInt("idEncuestaDetalle",0);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Util.actualizarToolBar(descripcionEncuesta, tipoEncuesta,true,this, R.drawable.ic_action_close);

        soapManager = new SoapManager(getApplicationContext());
        daoEncuesta = new DAOEncuesta(getApplicationContext());
        ventas360App = (Ventas360App) getApplicationContext();

        tv_cliente = (TextView) findViewById(R.id.tv_cliente);
        tv_descripcionEncuesta = (TextView) findViewById(R.id.tv_descripcionEncuesta);
        linearEncuesta = (LinearLayout) findViewById(R.id.linearEncuesta);

        cargarEncuesta();
        IniciarLocalizador();
    }

    private void cargarEncuesta() {
        tv_cliente.setText(idCliente+" - "+razonSocial);
        tv_descripcionEncuesta.setText(descripcionEncuesta);

        encuestaRespuestaModel = new EncuestaRespuestaModel();
        encuestaRespuestaModel.setIdEncuesta(idEncuesta);
        encuestaRespuestaModel.setIdEncuestaDetalle(idEncuestaDetalle);
        encuestaRespuestaModel.setIdCliente(idCliente);
        encuestaRespuestaModel.setIdVendedor(ventas360App.getIdVendedor());
        encuestaRespuestaModel.setFecha(Util.getFechaHoraTelefonoString_formatoSql());
        encuestaRespuestaModel.setFlag(EncuestaRespuestaModel.FLAG_PENDIENTE);

        listaPreguntas = daoEncuesta.getListaDetallePreguntas(idEncuesta, idEncuestaDetalle);

        linearEncuesta.removeAllViews();
        linearEncuesta.setOrientation(LinearLayout.VERTICAL);

        for (EncuestaDetallePreguntaModel preguntaParseada: listaPreguntas){
            agregarTextView(preguntaParseada.getPregunta(),preguntaParseada.getOrdenPregunta(), preguntaParseada.getRequerido()==1);

            switch (preguntaParseada.getTipoRespuesta()){
                case EncuestaDetallePreguntaModel.TIPO_RESPUESTA_UNICA:
                    if (preguntaParseada.getListaAlternativas().size() > LIMITE_ALTERNATIVAS_PARA_CHECK){
                        preguntaParseada.setView(agregarSpinner(preguntaParseada.getListaAlternativas()));
                    }else {
                        preguntaParseada.setView(agregarRadioButton(preguntaParseada.getListaAlternativas()));
                    }
                    break;
                case EncuestaDetallePreguntaModel.TIPO_RESPUESTA_MULTIPLE:
                    preguntaParseada.setView(agregarCheckBox(preguntaParseada.getListaAlternativas()));
                    break;
                case EncuestaDetallePreguntaModel.TIPO_RESPUESTA_LIBRE:
                    preguntaParseada.setView(agregarEditText());
                    break;
                case EncuestaDetallePreguntaModel.TIPO_RESPUESTA_FOTO:
                    preguntaParseada.setView(agregarImageView(preguntaParseada.getIdPregunta()));
                    break;
            }
        }
    }

    private View agregarImageView(final int idPregunta) {
        final ImageView imageView = new ImageView(getApplicationContext());
        imageView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, getResources().getInteger(R.integer.encuesta_image_size)));
        imageView.setImageResource(R.drawable.ic_photo_camera_grey);
        linearEncuesta.addView(imageView);

        LinearLayout linearLayout = new LinearLayout(getApplicationContext());
        linearLayout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT));
        linearLayout.setOrientation(LinearLayout.HORIZONTAL);
        linearLayout.setGravity(Gravity.CENTER);

        @SuppressLint("RestrictedApi") Button buttonFoto = new Button(new ContextThemeWrapper(getApplicationContext(), R.style.RaisedButtonAccent));
        buttonFoto.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT));
        buttonFoto.setText("Tomar foto");
        buttonFoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                abrirCamara(imageView,idPregunta);
            }
        });
        linearLayout.addView(buttonFoto);

        /*@SuppressLint("RestrictedApi") Button buttonGaleria = new Button(new ContextThemeWrapper(getApplicationContext(), R.style.RaisedButtonWhite));
        buttonGaleria.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT));
        buttonGaleria.setText("Abrir de galería");
        buttonGaleria.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                abrirGaleria(imageView,idPregunta);
            }
        });
        linearLayout.addView(buttonGaleria);*/

        agregarMargenBottom(linearLayout);
        linearEncuesta.addView(linearLayout);
        return imageView;
    }

    private void agregarTextView(String pregunta, int orden, boolean isRequerido) {
        TextView textView = new TextView(EncuestaClienteActivity.this);
        textView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT));
        textView.setTextSize(getResources().getInteger(R.integer.encuesta_pregunta_text_size));
        String requeridoText = "";
        if (isRequerido)
            requeridoText = "<font color=\"red\">*</font>&nbsp;";
        textView.setText(Html.fromHtml("<strong>"+orden+".-&nbsp;&nbsp;"+pregunta+requeridoText+"</strong>"));
        agregarMargenBottom(textView);
        linearEncuesta.addView(textView);
    }

    private View agregarEditText() {
        EditText editText = new EditText(EncuestaClienteActivity.this);
        editText.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT));
        agregarMargenBottom(editText);
        linearEncuesta.addView(editText);
        return editText;
    }

    private View agregarRadioButton(ArrayList<EncuestaAlternativaModel> listaAlternativas){
        Log.d(TAG,"listaAlternativas size:"+listaAlternativas.size());
        RadioGroup radioGroup = new RadioGroup(EncuestaClienteActivity.this);
        radioGroup.setOrientation(RadioGroup.VERTICAL);
        boolean autoselect = true;
        for (EncuestaAlternativaModel alternativa : listaAlternativas){
            RadioButton radioButton = new RadioButton(EncuestaClienteActivity.this);
            radioButton.setText(alternativa.getDescripcion());
            radioButton.setId(alternativa.getIdAlternativa());

            if (autoselect)
                radioButton.setChecked(true);
            else {
                //Seleccionar en caso sea igual al parametro (para cuando se cargue una encuesta ya hecha)
            }
            autoselect = false;
            radioGroup.addView(radioButton);
        }
        radioGroup.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT));
        agregarMargenBottom(radioGroup);
        linearEncuesta.addView(radioGroup);
        return radioGroup;
    }

    private View agregarSpinner(ArrayList<EncuestaAlternativaModel> listaAlternativas){
        Spinner spinner = new Spinner(EncuestaClienteActivity.this, Spinner.MODE_DIALOG);
        ArrayList<String> arrayList = new ArrayList<>();

        for (EncuestaAlternativaModel alternativa: listaAlternativas){
            arrayList.add(alternativa.getDescripcion());
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,R.layout.spinner_item_dialog, arrayList);
        //adapter.setDropDownViewResource(android.R.layout.select_dialog_singlechoice);
        spinner.setAdapter(adapter);

        spinner.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT));
        agregarMargenBottom(spinner);
        linearEncuesta.addView(spinner);
        return spinner;
    }

    private View agregarCheckBox(ArrayList<EncuestaAlternativaModel> listaAlternativas){
        Log.d(TAG,"listaAlternativas size:"+listaAlternativas.size());
        LinearLayout linear = new LinearLayout(EncuestaClienteActivity.this);
        linear.setOrientation(RadioGroup.VERTICAL);
        boolean autoselect = false;
        for (EncuestaAlternativaModel alternativa : listaAlternativas){
            CheckBox checkBox = new CheckBox(EncuestaClienteActivity.this);
            checkBox.setText(alternativa.getDescripcion());
            checkBox.setId(alternativa.getIdAlternativa());

            if (autoselect)
                checkBox.setChecked(true);
            else {
                //Seleccionar en caso sea igual al parametro (para cuando se cargue una encuesta ya hecha)
            }
            autoselect = false;
            linear.addView(checkBox);
        }
        linear.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT));
        agregarMargenBottom(linear);
        linearEncuesta.addView(linear);
        return linear;
    }

    private void agregarMargenBottom(View view){
        LinearLayout.LayoutParams parameter =  (LinearLayout.LayoutParams) view.getLayoutParams();
        parameter.setMargins(parameter.leftMargin, parameter.topMargin, parameter.rightMargin, getResources().getInteger(R.integer.encuesta_view_bottom_margin)); // left, top, right, bottom
        view.setLayoutParams(parameter);
    }

    private void abrirGaleria(ImageView imageView, int idPregunta) {
        imgViewSeleccionado = imageView;
        idPreguntaSeleccionada = idPregunta;
        //Intent intent = new Intent(Intent.ACTION_GET_CONTENT, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/");
        startActivityForResult(intent.createChooser(intent, "Seleccione"), REQUEST_CODE_GALERIA);
    }

    String[] PERMISSIONS = {Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};

    public boolean hasPermissions(Context context, String... permissions) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null && permissions != null) {
            for (String permission : permissions) {
                if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean permisosCamara() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (!hasPermissions(this, PERMISSIONS)) {
                //FragmentCompat.requestPermissions(permissionsList, RequestCode); Para Fragments
                ActivityCompat.requestPermissions(EncuestaClienteActivity.this, PERMISSIONS, REQUEST_PERMISOS_CAMARA);
//                ActivityCompat.requestPermissions(EncuestaClienteActivity.this, new String[]{Manifest.permission.CAMERA}, REQUEST_PERMISOS_CAMARA);
                return false;
            } else {
                return true;
            }
        } else {
            return true;
        }
    }

    private void abrirCamara(ImageView imageView, int idPregunta){
        imgViewSeleccionado = imageView;
        idPreguntaSeleccionada = idPregunta;

        if (permisosCamara()){
            if (getApplicationContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (intent.resolveActivity(getPackageManager()) != null) {//Si tiene camara
                    File fotoFile = null;
                    try {
                        fotoFile = createImageFile();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    if (fotoFile != null) {
                        //Obtiene file_path.xml el cual define la ruta de las fotos, a fin de tener disponible para toda la app. Esto debe estar también declarado en el manifest como <provider>
                        //Uri fotoUri = FileProvider.getUriForFile(getApplicationContext(),getPackageName(),fotoFile);
                        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
                        StrictMode.setVmPolicy(builder.build());
                        Uri fotoUri = Uri.fromFile(fotoFile);
                        intent.putExtra(MediaStore.EXTRA_OUTPUT, fotoUri);
                        startActivityForResult(intent, REQUEST_CODE_CAMARA);
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "Can't use camera from image capture", Toast.LENGTH_SHORT).show();
                }
            }else{
                Toast.makeText(getApplicationContext(), "Device doesn't have feature camera ", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void abrirCamara(){
        if (getApplicationContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if(intent.resolveActivity(getPackageManager()) != null){//Si tiene camara
                File fotoFile = null;
                try{
                    fotoFile = createImageFile();
                }catch (Exception e){
                    e.printStackTrace();
                }

                if (fotoFile != null) {
                    //Obtiene file_path.xml el cual define la ruta de las fotos, a fin de tener disponible para toda la app. Esto debe estar también declarado en el manifest como <provider>
                    //Uri fotoUri = FileProvider.getUriForFile(getApplicationContext(),getPackageName(),fotoFile);
                    StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
                    StrictMode.setVmPolicy(builder.build());
                    Uri fotoUri = Uri.fromFile(fotoFile);
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, fotoUri);
                    startActivityForResult(intent, REQUEST_CODE_CAMARA);
                }
            }else{
                Toast.makeText(getApplicationContext(),"Can't use camera from image capture",Toast.LENGTH_SHORT).show();
            }
        }else{
            Toast.makeText(getApplicationContext(), "Device doesn't have feature camera ", Toast.LENGTH_SHORT).show();
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String fileName = ventas360App.getIdEmpresa()+ventas360App.getIdSucursal()+"_"+ventas360App.getIdVendedor()+"_"+idCliente+"_"+timeStamp;

        //File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File storageDir = Environment.getExternalStoragePublicDirectory(getResources().getString(R.string.Ventas360App_Picture));// presenta problemas cuando un dispositivo usa una app de camara de terceros y no la nativa de android
        if (!storageDir.exists()){
            storageDir.mkdirs();
        }
        //File storageDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath()+File.separator+getResources().getString(R.string.Ventas360App_Picture));

        //File foto = File.createTempFile(fileName,".jpg", storageDir); //Para crear el archivo temporal (en realidad solo agrega un numero aleatorio al final para hacerlo unico)
        Log.i(TAG,"PATH: "+storageDir.getAbsolutePath());
        File foto = new File(storageDir,fileName+".jpg");//Para crear el archivo permanente (crea el archivo con el nombre tal cual, sin agregar nada al final)
        fotoPathTemp = foto.getAbsolutePath();
        fotoNameTemp = fileName+".jpg";
        return foto;
    }

    private void IniciarLocalizador() {
        gpsTracker = new GPSTracker(this);

        if (gpsTracker.isGPSEnabled()){
            if (Build.VERSION.SDK_INT >= 23){
                if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED
                        || ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_PERMISOS_UBICACION);
                }else
                    gpsTracker.getLocations();
            }else
                gpsTracker.getLocations();
        }else {
            showDialogoUbicacion();
        }
    }

    private void showDialogoUbicacion() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle("Ubicación");
        alertDialog.setMessage("Es necesario que active la ubicación del teléfono en precisión alta");
        alertDialog.setCancelable(false);

        alertDialog.setPositiveButton("ACEPTAR", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivityForResult(intent,REQUEST_CODE_UBICACION);
            }
        });
        alertDialog.show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode){
            case REQUEST_PERMISOS_CAMARA:
                if (PermissionUtil.verifyPermissions(grantResults)) {
                    abrirCamara();
                } else {
                    Toast.makeText(this, "No se otorgaron permisos para usar la cámara o guardar en el almacenamiento interno", Toast.LENGTH_SHORT).show();
                }
                break;
            case REQUEST_PERMISOS_UBICACION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    gpsTracker.getLocations();
                } else {
                    Toast.makeText(this, "No se otorgaron permisos de ubicación", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_CODE_CAMARA:
                MediaScannerConnection.scanFile(getApplicationContext(), new String[]{fotoPathTemp}, null, new MediaScannerConnection.MediaScannerConnectionClient() {
                    @Override
                    public void onMediaScannerConnected() {

                    }

                    @Override
                    public void onScanCompleted(String s, Uri uri) {
                        Log.i(TAG, "onScanCompleted PATH " + fotoPathTemp);
                    }
                });

                if (resultCode == RESULT_OK) {
                    if (!fotoPathTemp.equals("")) {
                        BitmapFactory.Options options = new BitmapFactory.Options();
                        options.inJustDecodeBounds = true;//Solo los atributos mas no la imagen
                        BitmapFactory.decodeFile(fotoPathTemp, options);//Decodifica para obtener la informacion de la imagen
                        //options.inSampleSize = 6;//poner la imagen a 1/6 de su tamaño para no tener problemas de memoria
                        options.inSampleSize = BitmapConverter.calculateInSampleSize(options, 500, 500);//Se calcula la partición que deberá tener la imagen

                        //En adelante ya se obtiene la imagen en un Bitmap
                        options.inJustDecodeBounds = false;//Los atributos con la imagen
                        Bitmap bitmap = BitmapFactory.decodeFile(fotoPathTemp, options);
                        try {
                            ExifInterface exif = new ExifInterface(fotoPathTemp);
                            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
                            Log.d("EXIF", "Exif: " + orientation);
                            Matrix matrix = new Matrix();
                            if (orientation == ExifInterface.ORIENTATION_ROTATE_90) {
                                matrix.postRotate(90);
                            } else if (orientation == ExifInterface.ORIENTATION_ROTATE_180) {
                                matrix.postRotate(180);
                            } else if (orientation == ExifInterface.ORIENTATION_ROTATE_270) {
                                matrix.postRotate(270);
                            }
                            bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true); // rotating bitmap
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        imgViewSeleccionado.setImageBitmap(bitmap);

                        //Picasso.with(getApplicationContext()).load("file:"+fotoPathTemp).into(imgViewSeleccionado);//Carga la imagen en caché (no necesario por ahora)
                        imgViewSeleccionado.setTag(fotoPathTemp);

                        for (int i = 0; i < listaPreguntas.size(); i++) {
                            if (listaPreguntas.get(i).getIdPregunta() == idPreguntaSeleccionada) {
                                ((ImageView) listaPreguntas.get(i).getView()).setTag(fotoNameTemp);
                            }
                        }
                    }
                }
                break;
            case REQUEST_CODE_GALERIA:
                if (data != null) {
                    try {
                        Uri uri = data.getData();
                        Bitmap bitmapFoto = MediaStore.Images.Media.getBitmap(getApplicationContext().getContentResolver(), uri);
                        imgViewSeleccionado.setImageBitmap(bitmapFoto);
                        /*String pathName = Util.getRealPathFromURI(getApplicationContext(),uri);
                        Picasso.with(getApplicationContext()).load("file:"+pathName).into(imgViewSeleccionado);//Carga la imagen en caché (no necesario por ahora)*/
                        /*Bitmap bitmapFoto = BitmapFactory.decodeFile(pathName);//otra alternativa para cargar la imagen desde el path completo
                        imgViewSeleccionado.setImageBitmap(bitmapFoto);*/

                        for (int i = 0; i < listaPreguntas.size(); i++) {
                            if (listaPreguntas.get(i).getIdPregunta() == idPreguntaSeleccionada) {
                                ((ImageView) listaPreguntas.get(i).getView()).setTag(fotoNameTemp);
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                break;
            case REQUEST_CODE_UBICACION:
                if (gpsTracker.isGPSEnabled()) {
                    if (Build.VERSION.SDK_INT >= 23) {
                        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                                || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_PERMISOS_UBICACION);
                        } else
                            gpsTracker.getLocations();
                    } else
                        gpsTracker.getLocations();
                } else {
                    showDialogoUbicacion();
                }
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_activity_encuesta_cliente,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id){
            case android.R.id.home:
                DialogoConfirmacion();
                break;
            case R.id.menu_encuesta_enviar:
                EnviarEncuesta();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        DialogoConfirmacion();
    }

    @Override
    public void onPause() {
        super.onPause();
        gpsTracker.stopUsingGPS();
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

    private void DialogoConfirmacion() {
        AlertDialog.Builder builder = new AlertDialog.Builder(EncuestaClienteActivity.this);

        builder.setTitle("Descartar encuesta");
        builder.setMessage("Los datos de esta encuesta no se guardarán");

        builder.setNegativeButton("CANCELAR", null);
        builder.setPositiveButton("DESCARTAR", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog,int which) {
                finish();
            }
        });
        builder.show();

    }

    private void EnviarEncuesta() {
        boolean encuestaValidada = true;
        listaRespuestasDetalle = new ArrayList<>();
        ArrayList<String> listaFotos = new ArrayList<>();
        for (EncuestaDetallePreguntaModel pregunta: listaPreguntas){
            Log.i(TAG,"pregunta:"+pregunta.getTipoRespuesta());
            switch (pregunta.getTipoRespuesta()){
                case EncuestaDetallePreguntaModel.TIPO_RESPUESTA_LIBRE:
                    EditText editText = (EditText) pregunta.getView();
                    String respuestaString = editText.getText().toString();
                    if (respuestaString != null && !respuestaString.isEmpty()){//agregar validacion para cuando es vacio o no respondido
                        EncuestaRespuestaDetalleModel respuesta = new EncuestaRespuestaDetalleModel();
                        respuesta.setIdPregunta(pregunta.getIdPregunta());
                        respuesta.setTipoRespuesta(pregunta.getTipoRespuesta());
                        respuesta.setDescripcion(respuestaString);
                        listaRespuestasDetalle.add(respuesta);
                    }else{
                        if (pregunta.getRequerido() == EncuestaDetallePreguntaModel.ENCUESTA_REQUERIDA)
                            encuestaValidada = false;
                    }
                    break;
                case EncuestaDetallePreguntaModel.TIPO_RESPUESTA_UNICA:
                    if (pregunta.getListaAlternativas().size() > LIMITE_ALTERNATIVAS_PARA_CHECK){
                        Spinner spinner = (Spinner) pregunta.getView();
                        int position = spinner.getSelectedItemPosition();

                        if (position != Spinner.INVALID_POSITION){
                            EncuestaRespuestaDetalleModel respuesta = new EncuestaRespuestaDetalleModel();
                            respuesta.setIdPregunta(pregunta.getIdPregunta());
                            respuesta.setTipoRespuesta(pregunta.getTipoRespuesta());
                            int idAlternativa = pregunta.getListaAlternativas().get(position).getIdAlternativa();
                            respuesta.setIdAlternativas(String.valueOf(idAlternativa));
                            listaRespuestasDetalle.add(respuesta);
                        }else {
                            if (pregunta.getRequerido() == EncuestaDetallePreguntaModel.ENCUESTA_REQUERIDA)
                                encuestaValidada = false;
                        }
                    }else {
                        RadioGroup radioGroup = (RadioGroup) pregunta.getView();
                        int idAlternativa = radioGroup.getCheckedRadioButtonId();//retorna -1 si ninguno esta seleccionado
                        if (idAlternativa != -1){//Si hay alguna alternativa seleccionada, se crea la respuesta
                            EncuestaRespuestaDetalleModel respuesta = new EncuestaRespuestaDetalleModel();
                            respuesta.setIdPregunta(pregunta.getIdPregunta());
                            respuesta.setTipoRespuesta(pregunta.getTipoRespuesta());
                            respuesta.setIdAlternativas(String.valueOf(idAlternativa));
                            listaRespuestasDetalle.add(respuesta);
                        }else{
                            if (pregunta.getRequerido() == EncuestaDetallePreguntaModel.ENCUESTA_REQUERIDA)
                                encuestaValidada = false;
                        }
                    }

                    break;
                case EncuestaDetallePreguntaModel.TIPO_RESPUESTA_MULTIPLE:
                    LinearLayout linear = (LinearLayout) pregunta.getView();
                    int childCount = linear.getChildCount();
                    String alternativasSeleccionadas = "";
                    for (int i = 0; i < childCount; i++) {
                        View element = linear.getChildAt(i);
                        if (element instanceof CheckBox) {
                            CheckBox checkBox = (CheckBox) element;
                            if (checkBox.isChecked())
                                alternativasSeleccionadas += checkBox.getId()+",";
                        }
                    }
                    if (!alternativasSeleccionadas.equals("")) {
                        alternativasSeleccionadas = alternativasSeleccionadas.substring(0, alternativasSeleccionadas.length() - 1);

                        EncuestaRespuestaDetalleModel respuesta = new EncuestaRespuestaDetalleModel();
                        respuesta.setIdPregunta(pregunta.getIdPregunta());
                        respuesta.setTipoRespuesta(pregunta.getTipoRespuesta());
                        respuesta.setIdAlternativas(alternativasSeleccionadas);
                        listaRespuestasDetalle.add(respuesta);
                    }

                    if (pregunta.getRequerido() == EncuestaDetallePreguntaModel.ENCUESTA_REQUERIDA) {
                        if (alternativasSeleccionadas.isEmpty())
                            encuestaValidada = false;
                    }
                    break;
                case EncuestaDetallePreguntaModel.TIPO_RESPUESTA_FOTO:
                    ImageView imageView = (ImageView) pregunta.getView();
                    if (imageView.getTag() != null && imageView.getTag().toString() != null && !imageView.getTag().toString().isEmpty()){
                        String fotoName = imageView.getTag().toString();
                        String fotoURL = Environment.getExternalStoragePublicDirectory(getResources().getString(R.string.Ventas360App_Picture))+File.separator+fotoName;
                        listaFotos.add(fotoURL);
                        EncuestaRespuestaDetalleModel respuesta = new EncuestaRespuestaDetalleModel();
                        respuesta.setIdPregunta(pregunta.getIdPregunta());
                        respuesta.setTipoRespuesta(pregunta.getTipoRespuesta());
                        respuesta.setDescripcion(fotoName);//Aqui almacenaremos el nombre de la imagen
                        respuesta.setFotoURL(fotoURL);

                        String base64 = BitmapConverter.convertirImagenString(fotoURL);
                        respuesta.setStringFoto(base64);

                        /*while (gpsTracker.getLatitude() == 0.0 && gpsTracker.getLongitude() == 0.0){
                            Log.d(TAG,"latitud y longitud 0.0");//Mantener el hilo trabajando hasta que se tome alguna posición
                        }*/
                        respuesta.setLatitud(gpsTracker.getLatitude());
                        respuesta.setLongitud(gpsTracker.getLongitude());
                        listaRespuestasDetalle.add(respuesta);

                        //Si la encuesta tiene una pregunta tipo Foto, esta no será enviada desde este activity, por tanto el flag de la encuesta tiene que indicar Incompleto
                        encuestaRespuestaModel.setFlag(EncuestaRespuestaModel.FLAG_INCOMPLETO);
                    }else{
                        if (pregunta.getRequerido() == EncuestaDetallePreguntaModel.ENCUESTA_REQUERIDA)
                            encuestaValidada = false;
                    }
                    break;
            }

            if (!encuestaValidada)
                break;
        }

        if (!encuestaValidada){
            Toast.makeText(getApplicationContext(),"Complete los campos requeridos",Toast.LENGTH_SHORT).show();
        }else{
            encuestaRespuestaModel.setDetalle(listaRespuestasDetalle);
            daoEncuesta.guardarEncuestaRespuesta(encuestaRespuestaModel);
            new asyncEnviarEncuesta().execute();
        }
    }


    private void showDialogoPostEnvio(String titulo, String mensaje, @DrawableRes int icon, final boolean finish) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(titulo);
        builder.setMessage(mensaje);
        builder.setIcon(icon);
        builder.setCancelable(false);
        builder.setPositiveButton("ACEPTAR", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog,int which) {
                if (finish){
                    setResult(RESULT_OK);
                    finish();
                }
            }
        });
        builder.show();
    }

    class asyncEnviarFotos extends AsyncTask<Void, Void, String> {
        @Override
        protected String doInBackground(Void... voids) {
            return null;
        }
    }

    class asyncEnviarEncuesta extends AsyncTask<Void, Void, String> {
        ProgressDialog progressDialog;

        protected void onPreExecute() {
            progressDialog = new ProgressDialog(EncuestaClienteActivity.this);
            progressDialog.setMessage("Enviando encuesta....");
            progressDialog.setIndeterminate(true);
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        protected String doInBackground(Void... params) {
            if (Util.isConnectingToRed(getApplicationContext())){
                if (Util.isConnectingToServer(getApplicationContext())){
                    try {
                        //Obtener la foto en cadena antes de poder enviarlo a la webservice
                        /*Desde este activity se debe enviar toda la encuesta pero sin fotos, para que no gasten datos, ya desde an actividad anterior se podrá enviar las imagenes pendientes*/
                        /*for (EncuestaRespuestaDetalleModel respuestaDetalleModel: encuestaRespuestaModel.getDetalle()){
                            if (respuestaDetalleModel.getTipoRespuesta().equals(EncuestaDetallePreguntaModel.TIPO_RESPUESTA_FOTO)){
                                String base64 = convertirImagenString(respuestaDetalleModel.getFotoURL());
                                respuestaDetalleModel.setStringFoto(base64);
                            }
                        }*/
                        Gson gson  = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
                       // Log.i(TAG,gson.toJson(encuestaRespuestaModel));
                        String cadena = gson.toJson(encuestaRespuestaModel);
                        String respuesta = soapManager.enviarEncuesta(TablesHelper.EncuestaRespuestaCabecera.ActualizarEncuesta,cadena);
                        return daoEncuesta.actualizarFlagEncuesta(idEncuesta,idEncuestaDetalle,idCliente,respuesta);
                    } catch (XmlPullParserException e){
                        e.printStackTrace();
                        daoEncuesta.actualizarFlagEncuesta(idEncuesta,idEncuestaDetalle,idCliente,EncuestaRespuestaModel.FLAG_PENDIENTE);
                        return "noService";
                    } catch (SocketTimeoutException e) {
                        e.printStackTrace();
                        daoEncuesta.actualizarFlagEncuesta(idEncuesta, idEncuestaDetalle, idCliente, EncuestaRespuestaModel.FLAG_PENDIENTE);
                        return "noService";
                    }catch (Exception e) {
                        e.printStackTrace();
                        daoEncuesta.actualizarFlagEncuesta(idEncuesta,idEncuestaDetalle,idCliente,EncuestaRespuestaModel.FLAG_PENDIENTE);
                        if (e.getCause() != null) {
                            if(e.getCause().getClass().equals(XmlPullParserException.class)){
                                return "noService";
                            }
                        }
                        return e.getMessage();
                    }
                }else{
                    daoEncuesta.actualizarFlagEncuesta(idEncuesta,idEncuestaDetalle,idCliente,EncuestaRespuestaModel.FLAG_PENDIENTE);
                    return "noInternet";
                }
            }else{
                daoEncuesta.actualizarFlagEncuesta(idEncuesta,idEncuestaDetalle,idCliente,EncuestaRespuestaModel.FLAG_PENDIENTE);
                return "noInternet";
            }

        }

        protected void onPostExecute(String result) {
            progressDialog.dismiss();
            switch (result) {
                case EncuestaRespuestaModel.FLAG_ENVIADO:
                    showDialogoPostEnvio("Envío satisfactorio", "La encuesta fué enviada al servidor. Sin embargo las fotos deben ser enviadas aparte", R.drawable.ic_dialog_check,true);
                    break;
                case EncuestaRespuestaModel.FLAG_PENDIENTE:
                    showDialogoPostEnvio("Atención", "No se pudieron enviar los datos, se almacenó localmente", R.drawable.ic_dialog_alert,true);
                    break;
                case EncuestaRespuestaModel.FLAG_INCOMPLETO:
                    showDialogoPostEnvio("Envío parcial", "La encuesta fué enviada al servidor de forma parcial. Se tendrá que enviar de forma completa", R.drawable.ic_dialog_check,true);
                    break;
                case "noInternet":
                    showDialogoPostEnvio("Sin conexión", "Es probable que no tenga acceso a INTERNET, la encuesta se guardó localmente", R.drawable.ic_dialog_error,true);
                    break;
                case "noService":
                    showDialogoPostEnvio("Sin conexión", "No se pudo conectar con el servicio, la encuesta se guardó localmente", R.drawable.ic_dialog_error,true);
                    break;
                default:
                    showDialogoPostEnvio("Atención", "Ocurrió un error: "+result, R.drawable.ic_dialog_error,false);
                    break;
            }
        }
    }
}
