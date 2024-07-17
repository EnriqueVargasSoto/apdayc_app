package com.expediodigital.ventas360.service;


import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;

/**
 * Created by Kevin Robin Meza Hinostroza on 28/09/2018.
 * kevin.meza@expediodigital.com
 */
public class UploadResultReceiver extends ResultReceiver {

    /*
     * Step 1: The AppReceiver is just a custom interface class we created.
     * This interface is implemented by the activity
     */
    private AppReceiver appReceiver;

    /**
     * Create a new ResultReceive to receive results.  Your
     * {@link #onReceiveResult} method will be called from the thread running
     * <var>handler</var> if given, or from an arbitrary thread if null.
     *
     * @param handler
     */

    public UploadResultReceiver(Handler handler, AppReceiver receiver) {
        super(handler);
        appReceiver = receiver;
    }

    @Override
    protected void onReceiveResult(int resultCode, Bundle resultData) {
        if (appReceiver != null) {
            /*
             * Step 2: We pass the resulting data from the service to the activity
             * using the AppReceiver interface
             */
            appReceiver.onReceiveResult(resultCode, resultData);
        }
    }

    public AppReceiver getAppReceiver() {
        return appReceiver;
    }

    public void setAppReceiver(AppReceiver appReceiver) {
        this.appReceiver = appReceiver;
    }



    public interface AppReceiver {
        public void onReceiveResult(int resultCode, Bundle resultData);
    }
}
