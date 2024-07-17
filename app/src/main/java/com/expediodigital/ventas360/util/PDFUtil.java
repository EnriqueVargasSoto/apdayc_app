package com.expediodigital.ventas360.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Environment;
import android.util.Base64;
import android.view.inputmethod.InputContentInfo;
import android.widget.Toast;


import com.expediodigital.ventas360.R;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * Created by Kevin Robinson Meza Hinostroza on febrero 2018.
 * Expedio Digital
 * Kevin.Meza@expediodigital.com
 */

public class PDFUtil {

    public static void generarPDF_fromBase64String(Context context, String base64String, String nombrePDF){
        File storageDir = Environment.getExternalStoragePublicDirectory(context.getResources().getString(R.string.Ventas360App_PDF));
        if (!storageDir.exists())
            storageDir.mkdirs();
        File pdfFile = new File(storageDir, nombrePDF);

        /*Document document = new Document(PageSize.A4);

        try {
            PdfWriter pdfWriter = PdfWriter.getInstance(document, new FileOutputStream(nombrePDF));
            document.open();
            document.addCreator("Expedio Digital");
            document.close();
        } catch (DocumentException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }*/

        byte[] pdfAsBytes = Base64.decode(base64String, Base64.DEFAULT);
        FileOutputStream os;
        try {
            os = new FileOutputStream(pdfFile, false);
            os.write(pdfAsBytes);
            os.flush();
            os.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    /*Que pasa si el archivo está abierto y quiero reemplazar, probar*/

    public static void abrirPDF(Context context, String nombreArchivo){
        String rutaCompleta = Environment.getExternalStoragePublicDirectory(context.getResources().getString(R.string.Ventas360App_PDF)).getAbsolutePath()+File.separator+nombreArchivo ;
        File file = new File(rutaCompleta);
        if (file.exists()){
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(Uri.fromFile(file),"application/pdf");
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            try {
                PackageManager pm = context.getPackageManager();
                List<ResolveInfo> activities = pm.queryIntentActivities(intent, 0);
                if (activities.size() > 0) {
                    context.startActivity(intent);
                }else{
                    Toast.makeText(context,"No se encontró un visor de PDF en el sistema ", Toast.LENGTH_LONG).show();
                }
            }catch (Exception e){
                Toast.makeText(context,"No tiene aplicaciones para abrir PDF", Toast.LENGTH_LONG).show();
            }
            /*Intent intent = new Intent(Intent.ACTION_VIEW,Uri.parse(rutaCompleta));
            intent.setData(Uri.fromFile(file));
            intent.setType("application/pdf");
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            PackageManager pm = context.getPackageManager();
            List<ResolveInfo> activities = pm.queryIntentActivities(intent, PackageManager.MATCH_ALL); --> 0
            if (activities.size() > 0) {
                context.startActivity(intent);
            }
            else
            {
                Toast.makeText(context,"No se encontró un visor de PDF en el sistema ", Toast.LENGTH_LONG).show();
            }*/
        }else{
            Toast.makeText(context,"El archivo PDF no existe! ", Toast.LENGTH_LONG).show();
        }
    }
}
