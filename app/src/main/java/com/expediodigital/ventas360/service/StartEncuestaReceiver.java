package com.expediodigital.ventas360.service;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.expediodigital.ventas360.LoginActivity;
import com.expediodigital.ventas360.Ventas360App;
import com.expediodigital.ventas360.view.EncuestasClientesActivity;

public class StartEncuestaReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent newIntent;
        if (((Ventas360App) context.getApplicationContext()).getSesionActiva()){
            Log.i("tartEncuestaReceiver","Intent EncuestasClientesActivity");
            newIntent = new Intent(context, EncuestasClientesActivity.class);

            newIntent.putExtra("idEncuesta",intent.getIntExtra("idEncuesta",0));
            newIntent.putExtra("idEncuestaDetalle",intent.getIntExtra("idEncuestaDetalle",0));
            newIntent.putExtra("descripcionEncuesta",intent.getStringExtra("descripcionEncuesta"));
            newIntent.putExtra("tipoEncuesta",intent.getStringExtra("tipoEncuesta"));
            newIntent.setAction(EncuestasClientesActivity.ACTION_SHOW_DIALOG);

        }else{
            Log.i("tartEncuestaReceiver","Intent LoginActivity");
            newIntent = new Intent(context, LoginActivity.class);
        }
        newIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);//Importante para iniciar la actividad desde un BroadcastReceiver
        context.startActivity(newIntent);
    }
}
