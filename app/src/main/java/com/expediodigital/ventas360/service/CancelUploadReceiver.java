package com.expediodigital.ventas360.service;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

public class CancelUploadReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent service = new Intent();
        service.setComponent(new ComponentName(context, UploadPhotoService.class));

        context.stopService(service);
    }
}
