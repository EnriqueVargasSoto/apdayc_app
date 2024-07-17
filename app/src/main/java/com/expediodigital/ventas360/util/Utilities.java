package com.expediodigital.ventas360.util;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.AudioManager;
import android.media.ExifInterface;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import static android.content.Context.CONNECTIVITY_SERVICE;

/**
 * Created by devendra on 4/7/16.
 */
public class Utilities implements AppConstants {
    private ConnectivityManager cm;
    static Context context;
    private static Utilities singleton = null;
    public static String input = "yyyy-MM-dd HH:mm:ss";
    public static String input1 = "yyyy-MM-dd";
    public static String input2 = "HH:mm";
    public static String outPut = "yyyy:MM:dd";
//    public static String outPut = "MMMM dd, yyyy @ hh:mm a";
    public static String outPut1 = "MMM dd, yyyy";
    public static String outPut2 = "yyyy:MM:dd:HH:mm";

    public static String inputForAcc = "dd MM yyyy hh:mm";
    public static String outPutForAcc = "MMM dd, yyyy";
    public static String outPutForedit = "dd-MM-yyyy";
    public static final String KEY_request_id = "request_id";

    /* A private Constructor prevents any other
     * class from instantiating.
     */
    private Utilities() {
    }

    public static String formatDateShow(String inputDate) {
        String date = "";
        SimpleDateFormat format = new SimpleDateFormat(input1);
        try {
            Date newDate = format.parse(inputDate);
            format = new SimpleDateFormat(outPut1);
            date = format.format(newDate);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return date;
    }

    public static String formateDateShow5(String inputDate) {
        String date = "";
        SimpleDateFormat format = new SimpleDateFormat(input1);
        try {
            Date newDate = format.parse(inputDate);
            format = new SimpleDateFormat(outPut1);
            date = format.format(newDate);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return date;
    }

    public static String formateDateShow6(String inputDate) {
        String date = "";
        SimpleDateFormat format = new SimpleDateFormat(input2);
        try {
            Date newDate = format.parse(inputDate);
            format = new SimpleDateFormat(outPut2);
            date = format.format(newDate);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return date;
    }

    /* Static 'instance' method */
    public static Utilities getInstance(Context mContext) {
        context = mContext;
        if (singleton == null)
            singleton = new Utilities();
        return singleton;
    }

    /**
     * Method for checking network availability
     */
    public boolean isNetworkAvailable(String s) {
        try {
            cm = (ConnectivityManager) context
                    .getSystemService(CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = cm.getActiveNetworkInfo();
            // if no network is available networkInfo will be null
            // otherwise check if we are connected
            if (networkInfo != null && networkInfo.isConnected())
                return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Check if the connection is fast
     *
     * @param type
     * @param subType
     * @return
     */

    public boolean isConnectionFast(int type, int subType) {
        if (type == ConnectivityManager.TYPE_WIFI) {
            Log.i(getClass().getName(), "Wifi State");
            return true;
        } else if (type == ConnectivityManager.TYPE_MOBILE) {
            switch (subType) {
                case TelephonyManager.NETWORK_TYPE_1xRTT:
                    Log.i(getClass().getName(), "50 - 100 kbps");
                    return true; // ~ 50-100 kbps
                case TelephonyManager.NETWORK_TYPE_CDMA:
                    Log.i(getClass().getName(), "14 - 64 kbps");
                    return true; // ~ 14-64 kbps
                case TelephonyManager.NETWORK_TYPE_EDGE:
                    Log.i(getClass().getName(), "50 - 100 kbps");
                    return true; // ~ 50-100 kbps
                case TelephonyManager.NETWORK_TYPE_EVDO_0:
                    Log.i(getClass().getName(), "400 - 1000 kbps");
                    return true; // ~ 400-1000 kbps
                case TelephonyManager.NETWORK_TYPE_EVDO_A:
                    Log.i(getClass().getName(), "600 - 1400 kbps");
                    return true; // ~ 600-1400 kbps
                case TelephonyManager.NETWORK_TYPE_GPRS:
                    Log.i(getClass().getName(), " 100 kbps");
                    return false; // ~ 100 kbps
                case TelephonyManager.NETWORK_TYPE_HSDPA:
                    Log.i(getClass().getName(), "2 - 14 Mbps");
                    return true; // ~ 2-14 Mbps
                case TelephonyManager.NETWORK_TYPE_HSPA:
                    Log.i(getClass().getName(), "700 - 1700 kbps");
                    return true; // ~ 700-1700 kbps
                case TelephonyManager.NETWORK_TYPE_HSUPA:
                    Log.i(getClass().getName(), "1 - 23 Mbps");
                    return true; // ~ 1-23 Mbps
                case TelephonyManager.NETWORK_TYPE_UMTS:
                    Log.i(getClass().getName(), "400 - 7000 kbps");
                    return true; // ~ 400-7000 kbps
            /*
             * Above API level 7, make sure to set android:targetSdkVersion
             * to appropriate level to use these
             */
                case TelephonyManager.NETWORK_TYPE_EHRPD: // API level 11
                    Log.i(getClass().getName(), "1 - 2 Mbps");
                    return true; // ~ 1-2 Mbps
                case TelephonyManager.NETWORK_TYPE_EVDO_B: // API level 9
                    Log.i(getClass().getName(), "5 Mbps");
                    return true; // ~ 5 Mbps
                case TelephonyManager.NETWORK_TYPE_HSPAP: // API level 13
                    Log.i(getClass().getName(), "10 - 20 Mbps");
                    return true; // ~ 10-20 Mbps
                case TelephonyManager.NETWORK_TYPE_IDEN: // API level 8
                    Log.i(getClass().getName(), "25 kbps");
                    return false; // ~25 kbps
                case TelephonyManager.NETWORK_TYPE_LTE: // API level 11
                    Log.i(getClass().getName(), "10+ Mbps");
                    return true; // ~ 10+ Mbps
                // Unknown
                case TelephonyManager.NETWORK_TYPE_UNKNOWN:
                default:
                    return false;
            }
        } else {
            return false;
        }
    }


    /**
     * Method for checking network availability
     */
    public boolean isNetworkAvailable() {
        try {
            cm = (ConnectivityManager) context
                    .getSystemService(CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = cm.getActiveNetworkInfo();

            // if no network is available networkInfo will be null
            // otherwise check if we are connected
            if (networkInfo != null && networkInfo.isConnected()) {
                if (isConnectionFast(networkInfo.getType(), networkInfo.getSubtype())) {
                    return true;
                } else {
                    // dialogOK(context,"","Your connection is too low", "OK", false);
                    return false;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Method for getting device unique number i.e IMEI
     */
//    public String getIMEIorDeviceId() {
//        if (isAndroidEmulator()) return "ANDROID_EMULATOR";
//        String imei = "";
//        try {
//            TelephonyManager tm = (TelephonyManager) context
//                    .getSystemService(Context.TELEPHONY_SERVICE);
//            imei = tm.getDeviceId();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return imei;
//    }
    public String getIMEIorDeviceId() {
        String deviceId = "";
        if (isEmulator()) return "ANDROID_EMULATOR";
        try {
            final TelephonyManager mTelephony = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        /*    if (mTelephony.getDeviceId() != null) {
                deviceId = mTelephony.getDeviceId();
            } else {*/
            deviceId = Settings.Secure.getString(
                    context.getContentResolver(),
                    Settings.Secure.ANDROID_ID);
            //  }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return deviceId;
    }

    public static boolean isEmulator() {
        return Build.FINGERPRINT.startsWith("generic")
                || Build.FINGERPRINT.startsWith("unknown")
                || Build.MODEL.contains("google_sdk")
                || Build.MODEL.contains("Emulator")
                || Build.MODEL.contains("Android SDK built for x86")
                || Build.MANUFACTURER.contains("Genymotion")
                || (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic"))
                || "google_sdk".equals(Build.PRODUCT)
                || Build.PRODUCT.equals("sdk")
                || Build.PRODUCT.contains("_sdk")
                || Build.PRODUCT.contains("sdk_");
    }

    /**
     * Method for getting application version code
     */
    public String getAppVersion() {
        String appVersion = "";
        try {
            appVersion = context.getPackageManager().getPackageInfo(
                    context.getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return appVersion;
    }

    public void exit() {
        try {
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_HOME);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
            ((Activity) context).finish();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Method for Checking application running on emulator or real device
     */
    public boolean isAndroidEmulator() {
        String product = Build.PRODUCT;
        boolean isEmulator = false;
        if (product != null) {
            isEmulator = product.equals("sdk") || product.contains("_sdk")
                    || product.contains("sdk_");
        }
        return isEmulator;
    }

    /**
     * method for email validation
     */
    public boolean checkEmail(String email) {
        try {
            return EMAIL_ADDRESS_PATTERN.matcher(email).matches();
        } catch (NullPointerException exception) {
            return false;
        }
    }


    /**
     * method for Password validation
     */
    public boolean checkPassword(String password) {
        try {
            return PASSWORD_PATTERN.matcher(password).matches();
        } catch (NullPointerException exception) {
            return false;
        }
    }


    /**
     * method for mobile_app number validation
     */
    public boolean checkMobile(String mobile) {
        try {
            mobile = mobile.replaceAll("[^0-9]", "");
            if (MOBILE_NUMBER_PATTERN.matcher(mobile).matches())
                return true;
            else
                return false;
        } catch (Exception exception) {
            return false;
        }
    }

    /**
     * method for user name validation
     */
    public boolean checkUsername(String username) {

        try {
            // username = username.replaceAll("", "");
            if (USER_NAME_PATTERN.matcher(username).matches())
                return true;
            else
                return false;
        } catch (Exception exception) {
            return false;
        }
    }

    MediaPlayer mMediaPlayer;
    AudioManager am;

    public void setSound(int sound) {
        try {
            // Log.i(getClass().getName(), "setSound................");
            int maxVol;
            am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
            maxVol = am.getStreamVolume(AudioManager.STREAM_MUSIC);
            am.setStreamVolume(AudioManager.STREAM_MUSIC, maxVol, 0);
            if (mMediaPlayer == null) {
                mMediaPlayer = new MediaPlayer();
                mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                mMediaPlayer.setVolume(maxVol, maxVol);
                mMediaPlayer = MediaPlayer.create(context, sound);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void playSound(int sound) {
        try {
            setSound(sound);
            if (mMediaPlayer != null) {
                if (am.getRingerMode() == AudioManager.RINGER_MODE_NORMAL) {
                    mMediaPlayer.start();
                }
            }
            // Log.i(getClass().getName(), "playSound.................");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

//    public void vibrate() {
//        try {
//            // Get instance of Vibrator from current Context
//            am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
//            if (am.getRingerMode() == AudioManager.RINGER_MODE_VIBRATE
//                    || am.getRingerMode() == AudioManager.RINGER_MODE_NORMAL) {
//                Vibrator v = (Vibrator) context
//                        .getSystemService(Context.VIBRATOR_SERVICE);
//                v.vibrate(1000);
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }


    /**
     * This method used to create new file if not exist .
     */
    public File getNewFile(String directoryName, String imageName) {
        String root = Environment.getExternalStorageDirectory()
                + directoryName;
        File file;
        if (isSDCARDMounted()) {
            new File(root).mkdirs();
            file = new File(root, imageName);
        } else {
            file = new File(context.getFilesDir(), imageName);
        }
        return file;
    }

    public boolean isSDCARDMounted() {
        String status = Environment.getExternalStorageState();
        if (status.equals(Environment.MEDIA_MOUNTED))
            return true;
        return false;
    }

    public String getAbsolutePath(Uri uri) {
        String[] projection = {MediaStore.MediaColumns.DATA};
        // Cursor cursor = ((Activity) context).managedQuery(uri, projection,
        // null, null, null);
        Cursor cursor = context.getContentResolver().query(uri, projection,
                null, null, null);
        if (cursor != null) {
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } else
            return null;
    }

    public void deletePicture() {
        try {
            String root = Environment.getExternalStorageDirectory().toString();
            new File(root + IMAGE_DIRECTORY_CROP).mkdirs();
            File f = new File(root + IMAGE_DIRECTORY_CROP);
            File[] files = f.listFiles();
            for (int i = 0; i < files.length; i++) {
                if (files[i].isFile())
                    files[i].delete();
            }
            f.delete();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public String getAbsolutePath(Context context, Uri uri) {
        String[] projection = {MediaStore.MediaColumns.DATA};
        // Cursor cursor = ((Activity) context).managedQuery(uri, projection,
        // null, null, null);
        Cursor cursor = context.getContentResolver().query(uri, projection,
                null, null, null);
        if (cursor != null) {
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
            cursor.moveToFirst();
            String path = cursor.getString(column_index);
            cursor.close();
            return path;
        } else
            return null;
    }


    public void dialogOK(final Context context, String title, String message,
                         String btnText, final boolean isFinish) {
        // https://www.google.com/design/spec/components/dialogs.html#dialogs-specs
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        alertDialogBuilder.setTitle("");
        alertDialogBuilder.setMessage(Html.fromHtml(message));
//        Typeface typeface = Typeface.createFromAsset(context.getAssets(), "Nirmala.ttf");
//        alertDialogBuilder.setView(new TextView(context));
        alertDialogBuilder.setCancelable(false);
        alertDialogBuilder.setPositiveButton(btnText, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (isFinish)
                    ((Activity) context).finish();
            }
        });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();

    }

    public void dialogOK2(final Context context, String title, String message,
                          String btnText, final boolean isFinish) {
        // https://www.google.com/design/spec/components/dialogs.html#dialogs-specs
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        alertDialogBuilder.setTitle("");
        alertDialogBuilder.setMessage(message);
//        alertDialogBuilder.setView(new TextView(context));
        alertDialogBuilder.setCancelable(false);
        alertDialogBuilder.setPositiveButton(btnText, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (isFinish)
                    ((Activity) context).finish();
            }
        });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();

    }


    public static void hideKeyboard(View view) {
        InputMethodManager inputManager = (InputMethodManager) context
                .getSystemService(Context.INPUT_METHOD_SERVICE);

        // check if no view has focus:
//        View v = ((Activity) context).getCurrentFocus();
//        if (v == null)
//            return;

        inputManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }


    public String getFormatedAmountString(Float amt) {
        DecimalFormat decimalFormat = new DecimalFormat("#");
        decimalFormat.setMinimumFractionDigits(2);
        String value = decimalFormat.format(amt);
        String str = "" + value.charAt(0);
        if (str.equals(".")) {
            value = "0" + value;
        }
        return value;
    }


    public static String formateDateShow(String inputDate) {
        String date = "";
        SimpleDateFormat format = new SimpleDateFormat(input);
        try {
            Date newDate = format.parse(inputDate);
            format = new SimpleDateFormat(outPut);
            date = format.format(newDate);
        } catch (Exception e) {

        }
        return date;
    }

    public static String formateDateShow1(String inputDate) {
        String date = "";
        SimpleDateFormat format = new SimpleDateFormat(input);
        try {
            Date newDate = format.parse(inputDate);
            format = new SimpleDateFormat(outPut1);
            date = format.format(newDate);
        } catch (Exception e) {

        }
        return date;
    }



    public static String formateDateShowAcc(String inputDate) {
        String date = "";
        Log.d("Input Date --",inputDate);
        SimpleDateFormat format = new SimpleDateFormat(inputForAcc);
        try {
            Date newDate = format.parse(inputDate);
            format = new SimpleDateFormat(input);
            date = format.format(newDate);
            Log.d("OutPut Date --",date);
        } catch (Exception e) {

        }
        return date;
    }


    public static Bitmap decodeFile(File f, int REQUIRED_WIDTH,
                                    int REQUIRED_HEIGHT) {
        try {
            ExifInterface exif = new ExifInterface(f.getPath());
            int orientation = exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL);
            // Log.i("exif.getAttribute(ExifInterface.TAG_ORIENTATION)",
            // exif.getAttribute(ExifInterface.TAG_ORIENTATION));
            int angle = 0;

            if (orientation == ExifInterface.ORIENTATION_ROTATE_90) {
                angle = 90;
            } else if (orientation == ExifInterface.ORIENTATION_ROTATE_180) {
                angle = 180;
            } else if (orientation == ExifInterface.ORIENTATION_ROTATE_270) {
                angle = 270;
            }
            // Log.i("path & orientation & angle", f.getPath() + " & "
            // + orientation + " & " + angle);
            Matrix mat = new Matrix();
            mat.postRotate(angle);
            // decode image size
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(new FileInputStream(f), null, o);
            // Find the correct scale value. It should be the power of 2.
            int REQUIRED_SIZE = 100; // 70
            int width_tmp = o.outWidth, height_tmp = o.outHeight;
            int scale = 1;
            // Log.i("W*H Before.....................", scale + " " + width_tmp
            // + "*" + height_tmp);
            if (width_tmp > height_tmp) {
                REQUIRED_SIZE = REQUIRED_HEIGHT;
                REQUIRED_HEIGHT = REQUIRED_WIDTH;
                REQUIRED_WIDTH = REQUIRED_SIZE;
            }
            while (true) {
                if (width_tmp / 2 < REQUIRED_WIDTH
                        && height_tmp / 2 < REQUIRED_HEIGHT)
                    break;
                width_tmp /= 2;
                height_tmp /= 2;
                scale *= 2;
            }
            // Log.i("W*H After.....................", scale + " " + width_tmp
            // + "*" + height_tmp);
            // decode with inSampleSize
            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize = scale;
            o2.inPurgeable = true;
            // return BitmapFactory.decodeStream(new FileInputStream(f), null,
            // o2);
            Bitmap correctBmp = BitmapFactory.decodeStream(new FileInputStream(
                    f), null, o2);
            correctBmp = Bitmap.createBitmap(correctBmp, 0, 0,
                    correctBmp.getWidth(), correctBmp.getHeight(), mat, true);
            return correctBmp;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    public static String getFilePath(Bitmap bitmap, Context context, String path) {
        //  File cacheDir;
        File file;

        try {
//            if (android.os.Environment.getExternalStorageState().equals(
//                    android.os.Environment.MEDIA_MOUNTED))
//                cacheDir = new File(
//                        android.os.Environment.getExternalStorageDirectory(),
//                        "NIGHTLIVE/Media/Images");
//            else
//                cacheDir = context.getCacheDir();
//            if (!cacheDir.exists())
//                cacheDir.mkdirs();

            if (bitmap != null) {
                String FILE_NAME = "dnd_" + new Date().getTime() + ".jpg";
                file = new File(path);

                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
                FileOutputStream fo;

                fo = new FileOutputStream(file);
                fo.write(bytes.toByteArray());
                fo.close();

                return file.getAbsolutePath();
            }

        } catch (Exception e1) {
            e1.printStackTrace();
            Log.v("", "getMessage " + e1.getMessage());

        } catch (Error e1) {
            e1.printStackTrace();
            Log.v("", "getMessage " + e1.getMessage());
        }
        /*
         * if (myDir.isDirectory()) { String[] children = myDir.list(); for (int
		 * i = 0; i < children.length; i++) { new File(myDir,
		 * children[i]).delete(); } }
		 */
        return "";
    }

    public boolean isEmailValid(String email) {
        return email.matches(emailPattern);
    }

    public boolean isPasswordValid(String password) {
        return password.length() >= 3;
    }

    public boolean isMobileNoValid(String MobileeNo) {
        return MobileeNo.length() == 10;
    }

    public boolean isCountrycodeValid(String CountryyCode) {
        return CountryyCode.length() == 2;
    }


    public String getMessage(JSONObject jsonObject) {
        String message = "";

        try {
            JSONObject jsonObject1 = jsonObject.getJSONObject("result");
            Iterator iterator = jsonObject1.keys();

            while (iterator.hasNext()) {
                String keys = iterator.next() + "";
                JSONArray jsonArray = jsonObject1.getJSONArray(keys + "");
                for (int i = 0; i < jsonArray.length(); i++) {
                    message += jsonArray.getString(i) + "\n";
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return message.trim();
    }


//    public void dialogAlert(final Context context, String title, String message, final AlertResponseListener alrertResponseListener) {
//
//
//        final Dialog dialogExit = new Dialog(context);
//        Window window = dialogExit.getWindow();
//        dialogExit.setCanceledOnTouchOutside(false);
//        dialogExit.setCancelable(false);
//        dialogExit.requestWindowFeature(Window.FEATURE_NO_TITLE);
//        dialogExit.setContentView(R.layout.dialog_box_confirm);
//        window.setType(WindowManager.LayoutParams.FIRST_SUB_WINDOW);
//        window.setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
//        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
//        TextView tvMessage = (TextView) window.findViewById(R.id.tv_message);
//        tvMessage.setText("" + message);
//        tvMessage.setMovementMethod(new ScrollingMovementMethod());
//        TextView tvTitle = (TextView) window.findViewById(R.id.tv_title);
//        tvTitle.setText(title);
//        TextView tvYes = (TextView) window.findViewById(R.id.tv_yes);
//        tvYes.setText(context.getResources().getString(R.string.yes));
//        TextView tvNo = (TextView) window.findViewById(R.id.tv_no);
//        tvNo.setText(context.getResources().getString(R.string.no));
//        tvYes.setOnClickListener(new View.OnClickListener() {
//            public void onClick(View v) {
//                dialogExit.dismiss();
//                // finish();
//                alrertResponseListener.isOk(true);
//
//            }
//        });
//        tvNo.setOnClickListener(new View.OnClickListener() {
//            public void onClick(View v) {
//                dialogExit.dismiss();
//                alrertResponseListener.isOk(false);
//            }
//        });
//        if (dialogExit != null && !dialogExit.isShowing()) {
//            dialogExit.show();
//        }
//
//
//    }

    public boolean isPincodeValid(String PinCode) {
        return PinCode.length() == 6;
    }


    public String getUpperCaseFirstChar(String str) {

        String[] strArray = str.split(" ");
        StringBuilder builder = new StringBuilder();
        for (String s : strArray) {
            String cap = s.substring(0, 1).toUpperCase() + s.substring(1);
            builder.append(cap + " ");
        }
        return builder.toString();
    }

    public String getDeviceInfo() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        int version = Build.VERSION.SDK_INT;
        String versionRelease = Build.VERSION.RELEASE;

        Log.e("MyActivity", "manufacturer - " + manufacturer
                + " \n model - " + model
                + " \n version - " + version
                + " \n versionRelease - " + versionRelease
        );

        String info = "manufacturer - " + manufacturer
                + " \n model - " + model
                + " \n version - " + version
                + " \n versionRelease - " + versionRelease;

        return info;

    }

    public void checkError(String response) {
        List phone = new ArrayList<>();
        JSONObject result = null;
        try {
            Log.i(getClass().getName(), "data res 2 " + response.toString());
            JSONObject jsonObject = new JSONObject(response);
            result = jsonObject.getJSONObject("result");
            if (result.has("mobile_number")) {
                JSONArray jsonArray = result.optJSONArray("mobile_number");
                for (int i = 0; i < jsonArray.length(); i++) {
                    phone.add(jsonArray.get(0).toString());
                }
            }
            if (result.has("email")) {
                JSONArray jsonArray = result.optJSONArray("email");
                for (int i = 0; i < jsonArray.length(); i++) {
                    phone.add(jsonArray.get(0).toString());
                }
            }
            Log.d("Log", phone.get(0).toString() + "-----------");
            dialogOK(context, "", phone.get(0).toString(), "Aceptar", false);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public byte[] getImageByte(String imagePath) {
        byte[] b = null;
        try {
            Bitmap bm = BitmapFactory.decodeFile(imagePath);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bm.compress(Bitmap.CompressFormat.JPEG, 100, baos); //bm is the bitmap object
            b = baos.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return b;
    }

//    public String getFormatedAmount(long amount){
//        return NumberFormat.getNumberInstance(Locale.US).format(amount);
//    }

    public int getSpinnerValue(ArrayList<HashMap<String, String>> hashMapArrayList, String name){
        int position = 0;
        if (name != null  && !name.equals("")){
        for (int i = 0; i < hashMapArrayList.size(); i++){
            if (name.replaceAll("_", " ").equalsIgnoreCase(hashMapArrayList.get(i).get(PN_VALUE).replaceAll("_", " "))){
                position = i;
                break;
            }
        }
        }
        return position;
    }
}

