package com.expediodigital.ventas360.service;

import android.app.ActivityManager;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Process;
import android.os.ResultReceiver;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import android.util.Log;

import com.expediodigital.ventas360.DAO.DAOEncuesta;
import com.expediodigital.ventas360.R;
import com.expediodigital.ventas360.util.SoapManager;
import com.expediodigital.ventas360.view.EncuestasClientesActivity;

import java.util.Iterator;
import java.util.List;


/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions and extra parameters.
 */
public class UploadPhotoService extends IntentService {
    final String TAG = getClass().getName();
    public static final int STATUS_RUNNING = 0;
    public static final int STATUS_FINISHED = 1;
    public static final int STATUS_ERROR = 2;

    private static int NOTIFY_ID=1337;
    private static int FOREGROUND_ID=1338;
    public static boolean isRunningTask = false;

    private int idEncuesta, idEncuestaDetalle;
    private String descripcionEncuesta, tipoEncuesta;

    DAOEncuesta daoEncuesta;
    SoapManager soapManager;
    NotificationCompat.Builder foregroundBuilder;
    NotificationManager mNotificationManager;
    String CHANNEL_ID;

    public UploadPhotoService() {
        super(UploadPhotoService.class.getName());
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        /*
         * Step 1: We pass the ResultReceiver from the activity to the intent service via intent.
         *  */
        ResultReceiver receiver = intent.getParcelableExtra("receiver");

        idEncuesta = intent.getIntExtra("idEncuesta",0);
        idEncuestaDetalle = intent.getIntExtra("idEncuestaDetalle",0);
        descripcionEncuesta = intent.getStringExtra("descripcionEncuesta");
        tipoEncuesta = intent.getStringExtra("tipoEncuesta");


        CHANNEL_ID = getString(R.string.channel_carga_id);

        if (!isRunningTask){
            isRunningTask = true;
            startForeground(FOREGROUND_ID, buildForegroundNotification());

            try {
                for (int i=0; i<5; i++){
                    Thread.sleep(1000*3);
                    //Actualizar notificacion
                    foregroundBuilder.setContentText("Enviando "+descripcionEncuesta+" "+ i+"/"+5);
                    foregroundBuilder.setPriority(NotificationCompat.PRIORITY_LOW);
                    mNotificationManager.notify(FOREGROUND_ID, foregroundBuilder.build());
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }


            //TODO: process background task here!
            /*daoEncuesta = new DAOEncuesta(getApplicationContext());
            soapManager = new SoapManager(getApplicationContext());
            ArrayList<EncuestaRespuestaModel> lista = daoEncuesta.getEncuestaPendientesEnvio(intent.getIntExtra("idEncuesta",0),intent.getIntExtra("idEncuestaDetalle",0));
            Log.i(TAG,"lista de pendientes: "+lista.size());
            for (EncuestaRespuestaModel encuestaRespuestaModel : lista){
                if (Util.isConnectingToRed(getApplicationContext()) && Util.isConnectingToInternet()) {
                    try {
                        //Desde este activity se debe enviar toda la encuesta con las fotos, obtener la foto en cadena antes de enviarlo a la webservice
                        for (EncuestaRespuestaDetalleModel respuestaDetalleModel : encuestaRespuestaModel.getDetalle()) {
                            if (respuestaDetalleModel.getTipoRespuesta().equals(EncuestaDetallePreguntaModel.TIPO_RESPUESTA_FOTO)) {
                                Log.d(TAG, "getFotoURL:" + respuestaDetalleModel.getFotoURL());
                                String base64 = BitmapConverter.convertirImagenString(respuestaDetalleModel.getFotoURL());
                                respuestaDetalleModel.setStringFoto(base64);
                            }
                        }
                        *//*La encuesta debe enviarse con el flag E, porque si se envía con P o I, la webservice mantendrá esos flag.
                        Y lo que se quiere ahora es que si se ingrese la encuesta completa y deje finalmente el flag en E*//*
                        encuestaRespuestaModel.setFlag(EncuestaRespuestaModel.FLAG_ENVIADO);

                        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
                        Log.i(TAG, gson.toJson(encuestaRespuestaModel));
                        String cadena = gson.toJson(encuestaRespuestaModel);
                        String respuesta = soapManager.enviarEncuesta(TablesHelper.EncuestaRespuestaCabecera.ActualizarEncuesta, cadena);
                        String flag = daoEncuesta.actualizarFlagEncuesta(encuestaRespuestaModel.getIdEncuesta(), encuestaRespuestaModel.getIdEncuestaDetalle(), encuestaRespuestaModel.getIdCliente(), respuesta);
                        Log.i(TAG,"new flag = "+flag);
                    } catch (NullPointerException e){
                        e.printStackTrace();
                        Log.e(TAG,"Archivo no encontrado");
                        //return ERROR_FOTO_NO_ENCONTRADA;
                    } catch (XmlPullParserException e){
                        e.printStackTrace();
                        //return SIN_CONEXION;
                    } catch (SocketTimeoutException e){
                        e.printStackTrace();
                        Log.e(TAG,"ERROR DE CONEXION SOCKET");
                        //return SIN_CONEXION;
                    } catch (Exception e) {
                        e.printStackTrace();
                        //return e.getMessage();
                    }
                }else {
                    //return SIN_CONEXION;
                    Log.e(TAG,"SIN CONEXION !");
                }
            }*/

            /*
             * Step 2: Now background service is processed,
             * we can pass the status of the service back to the activity using the resultReceiver
             *  */

            raiseNotification(true);

            Bundle bundle = new Bundle();
            bundle.putString("data","Se terminaron de enviar las fotos!!");
            receiver.send(STATUS_FINISHED, bundle);
            /*SE COMUNICA CON LA ACTIVIDAD CORRECTAMENTE, PROBAR CON MAS DATA Y LUEGO DESTRUYENDO LA ACTIVIDAD (NO DEBERÍA MOSTRAR ERROR)
            FINALMENTE MOSTRAR TOAST CORRECTO Y NOTIFICACION DE QUE SE REALIZÓ CON EXITO O UNA NOTIFICACION DE ERRO EN CASO NO SE HAYA CUMPLIDO*/
            isRunningTask = false;
            stopForeground(true);
            stopSelf();
        }else{
            Log.w(TAG,"isRunningTask actually");

        }


    }

    private Notification buildForegroundNotification() {
        /*
        * Desde android O se introdujo los canales para notificaciones a fin de que el usaurio pueda gestionar dichos canales (por niveles como solo sonido,
         * no mostrar en pantalla, solo vibrar, entre otros). Esto ayuda a que contrario a como pasaba antes de android O, el usuario cambiaba la configuración
         * de las notificaciones y esto afectaba a todas notificaciones de la app o incluso de otras app.
         * Una app puede tener varios canales para notificar y el usuario podrá gestionar cada uno de estos canales independientemente. De esta manera
         * por ejemplo, puede que le baje el nivel de importancia al canal de "Carga de imagenes" pero le de importancia alta al canal de "Chat" o "Descarga"
         * todos estos de la misma app.
        * */
        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //mNotificationManager.deleteNotificationChannel(CHANNEL_ID);
            NotificationChannel notificationChannel = mNotificationManager.getNotificationChannel(CHANNEL_ID);
            if (notificationChannel == null){
                CharSequence name = getString(R.string.channel_carga_name); //Nombre del canal que el usuario verá desde el teléfono
                int importance = NotificationManager.IMPORTANCE_LOW;
                NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID, name, importance);
                mNotificationManager.createNotificationChannel(mChannel);
            }
        }

        foregroundBuilder = new NotificationCompat.Builder(this, CHANNEL_ID);

        foregroundBuilder.setOngoing(true)
                .setContentTitle(getString(R.string.envio_encuesta))
                .setContentText("Enviando "+descripcionEncuesta)
                .setSmallIcon(android.R.drawable.stat_sys_upload)
                .setColor(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary))
                .setTicker(getString(R.string.iniciando_carga));
        foregroundBuilder.setPriority(NotificationCompat.PRIORITY_DEFAULT);

        Intent cancelIntent = new Intent(this, CancelUploadReceiver.class);
        PendingIntent pendingIntentCancel = PendingIntent.getBroadcast(this, 0, cancelIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        //PendingIntent pendingIntentCancel = PendingIntent.getService(this, 0, cancelIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        foregroundBuilder.addAction(android.R.drawable.ic_menu_close_clear_cancel, getString(R.string.cancelar_envio), pendingIntentCancel);//cada action normalmente esta asociado a un intent

        foregroundBuilder.setProgress(100, 0, true);

        return(foregroundBuilder.build());
    }

    private void raiseNotification(boolean complete) {

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID);
        builder.setAutoCancel(true).setWhen(System.currentTimeMillis());

        if (complete) {
            builder.setContentTitle(getString(R.string.envio_encuesta))
                    .setContentText(descripcionEncuesta + " " + getString(R.string.completado))
                    .setSmallIcon(android.R.drawable.stat_sys_upload_done)
                    .setColor(ContextCompat.getColor(getApplicationContext(), R.color.green_A700))
                    .setTicker(getString(R.string.envio_completado));

            Intent startIntent = new Intent(this, StartEncuestaReceiver.class);
            startIntent.putExtra("idEncuesta",idEncuesta);
            startIntent.putExtra("idEncuestaDetalle",idEncuestaDetalle);
            startIntent.putExtra("descripcionEncuesta",descripcionEncuesta);
            startIntent.putExtra("tipoEncuesta",tipoEncuesta);
            startIntent.setAction(EncuestasClientesActivity.ACTION_SHOW_DIALOG);
            //android:launchMode="singleTop" agregar al manifest para que no se lance otra instancia de la actividad en caso ya exista una

            PendingIntent contentIntent = PendingIntent.getBroadcast(this, 0, startIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            builder.setContentIntent(contentIntent);

        }
        else {
            builder.setContentTitle(getString(R.string.envio_imcompleto))
                    .setContentText(descripcionEncuesta + " no se pudo enviar completamente, puede verificar en la app")
                    .setSmallIcon(android.R.drawable.stat_notify_error)
                    .setColor(ContextCompat.getColor(getApplicationContext(), R.color.amber_A400))
                    .setTicker(getString(R.string.envio_imcompleto))
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(descripcionEncuesta + " no se pudo enviar completamente, puede verificar en la app"));

        }
        mNotificationManager.notify(NOTIFY_ID, builder.build());
    }

    @Override
    public void onDestroy() {
        Log.i("TAG Service","onDestroy");
        stopForeground(true);
        stopSelf();
        killProccess();

        super.onDestroy();
    }

    private void killProccess() {
        ActivityManager am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> runningAppProcesses = am.getRunningAppProcesses();

        Iterator<ActivityManager.RunningAppProcessInfo> iter = runningAppProcesses.iterator();

        while(iter.hasNext()){
            ActivityManager.RunningAppProcessInfo next = iter.next();

            String pricessName = getPackageName() + ":uploadService";

            if(next.processName.equals(pricessName)){
                Process.killProcess(next.pid);
                break;
            }
        }
    }



    @Override
    public void onTaskRemoved(Intent rootIntent) {
        Log.i("TAG Service","onTaskRemoved");
    }
}
