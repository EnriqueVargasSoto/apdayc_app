package com.expediodigital.ventas360.util;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.VectorDrawable;
import android.os.Build;
import androidx.annotation.DrawableRes;
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat;
import androidx.core.content.ContextCompat;
import android.util.Base64;
import android.util.Log;

import java.io.ByteArrayOutputStream;

import static android.graphics.Bitmap.Config.ARGB_8888;
/**
 * Created by Kevin Robinson Meza Hinostroza on septiembre 2017.
 * Expedio Digital
 * Kevin.Meza@expediodigital.com
 */

public class BitmapConverter {
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private static Bitmap getBitmap(VectorDrawable vectorDrawable) {
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(),
                vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        vectorDrawable.draw(canvas);
        return bitmap;
    }

    private static Bitmap getBitmap(VectorDrawableCompat vectorDrawable) {
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(),
                vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        vectorDrawable.draw(canvas);
        return bitmap;
    }

    public static Bitmap getBitmap(Context context, @DrawableRes int drawableResId) {
        Drawable drawable = ContextCompat.getDrawable(context, drawableResId);
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        } else if (drawable instanceof VectorDrawableCompat) {
            return getBitmap((VectorDrawableCompat) drawable);
        } else if (drawable instanceof VectorDrawable) {
            return getBitmap((VectorDrawable) drawable);
        } else {
            throw new IllegalArgumentException("Unsupported drawable type");
        }
    }

    public static Bitmap makeBitmap(Context context, @DrawableRes int drawableResId, String text) {
        Resources resources = context.getResources();
        float scale = resources.getDisplayMetrics().density;
        //Bitmap bitmap = BitmapFactory.decodeResource(resources, R.drawable.marker);
        Bitmap bitmap = getBitmap(context, drawableResId);
        bitmap = bitmap.copy(ARGB_8888, true);

        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(Color.RED); // Text color
        paint.setTextSize(14 * scale); // Text size
        paint.setShadowLayer(1f, 0f, 1f, Color.WHITE); // Text shadow
        Rect bounds = new Rect();
        paint.getTextBounds(text, 0, text.length(), bounds);

        int x = bitmap.getWidth() - bounds.width() - 10; // 10 for padding from right
        int y = bounds.height();
        canvas.drawText(text, x, y, paint);

        return  bitmap;
    }

    public static Bitmap writeTextOnDrawable(Context context, @DrawableRes int drawableResId, String text) {

        Bitmap bitmap = getBitmap(context, drawableResId);
        bitmap = bitmap.copy(ARGB_8888, true);

        Typeface tf = Typeface.create("Helvetica", Typeface.BOLD);

        Paint paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.BLACK);
        paint.setTypeface(tf);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTextSize(convertToPixels(context, 10));

        Rect textRect = new Rect();
        paint.getTextBounds(text, 0, text.length(), textRect);

        Canvas canvas = new Canvas(bitmap);

        //If the text is bigger than the canvas , reduce the font size
        if(textRect.width() >= (canvas.getWidth() - 4))     //the padding on either sides is considered as 4, so as to appropriately fit in the text
            paint.setTextSize(convertToPixels(context, 7));        //Scaling needs to be used for different dpi's

        //Calculate the positions
        float xPos = ((canvas.getWidth() / 2)*1.5f) - 2;     //-2 is for regulating the x position offset

        //"- ((paint.descent() + paint.ascent()) / 2)" is the distance from the baseline to the center.
        float yPos = (int) ((canvas.getHeight() / 2 * 1.5f) - ((paint.descent() + paint.ascent()) / 2)) ;

        canvas.drawText(text, xPos, yPos, paint);

        return  bitmap;
    }

    public static int convertToPixels(Context context, int nDP){
        final float conversionScale = context.getResources().getDisplayMetrics().density;

        return (int) ((nDP * conversionScale) + 0.5f) ;
    }

    public static String convertirImagenString(String fotoPath){
        //se puede reescalar antes
        Bitmap bitmapFoto = BitmapFactory.decodeFile(fotoPath);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmapFoto.compress(Bitmap.CompressFormat.JPEG,60, byteArrayOutputStream);
        byte[] imagenBytes = byteArrayOutputStream.toByteArray();
        String imagenString = Base64.encodeToString(imagenBytes, Base64.DEFAULT);
        return imagenString;
    }

    public static String convertirImagenStringReescale(String fotoPath){
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;//Solo los atributos mas no la imagen
        BitmapFactory.decodeFile(fotoPath,options);//Decodifica para obtener la informacion de la imagen
        options.inSampleSize = BitmapConverter.calculateInSampleSize(options, 1500,1500);//Se calcula la partición que deberá tener la imagen

        //En adelante ya se obtiene la imagen en un Bitmap
        options.inJustDecodeBounds = false;//Los atributos con la imagen
        Bitmap bitmapFoto = BitmapFactory.decodeFile(fotoPath,options);


        //Bitmap bitmapFoto = BitmapFactory.decodeFile(fotoPath);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmapFoto.compress(Bitmap.CompressFormat.JPEG,90, byteArrayOutputStream);
        byte[] imagenBytes = byteArrayOutputStream.toByteArray();
        String imagenString = Base64.encodeToString(imagenBytes, Base64.DEFAULT);
        return imagenString;
    }

    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        Log.i("BitmapConverter","height:"+height);
        Log.i("BitmapConverter","width:"+width);
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }
        Log.i("BitmapConverter","inSampleSize:"+inSampleSize);
        return inSampleSize;
    }
}
