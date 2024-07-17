package com.expediodigital.ventas360.view;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Handler;
import android.widget.LinearLayout;
import android.widget.ProgressBar;


import com.expediodigital.ventas360.LoginActivity;
import com.expediodigital.ventas360.R;
import com.expediodigital.ventas360.util.ManageSincronizacion;
import com.expediodigital.ventas360.util.Utilities;

public class IntroActivity extends AppCompatActivity {

    ProgressBar prgBar;
    LinearLayout layPrgBar;
    int currprg = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);

        prgBar = findViewById(R.id.simpleProgressBar);
        layPrgBar = findViewById(R.id.layPrgBar);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Utilities utilities = Utilities.getInstance(this);
        if(utilities.isNetworkAvailable())
        {
            ManageSincronizacion ms = new ManageSincronizacion(this, prgBar, ManageSincronizacion.ORIGEN_LOGIN, null, layPrgBar,-1,null,null);
            ms.iniciarSincronizacion();
        }
        else{
            Handler handler = new Handler();
            handler.postDelayed(runnableanim, 15);
        }
    }


    Runnable runnableanim = new Runnable() {
        @Override
        public void run() {

            currprg += 1;
            prgBar.setProgress(currprg);

            if (currprg < 100) {

                Handler handler = new Handler();
                handler.postDelayed(runnableanim, 15);
            } else {
                Intent intent = new Intent(IntroActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }

        }
    };


}
