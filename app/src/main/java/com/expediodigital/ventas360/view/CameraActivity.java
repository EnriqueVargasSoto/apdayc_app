package com.expediodigital.ventas360.view;

import android.Manifest;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;

import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;


import com.expediodigital.ventas360.R;
import com.expediodigital.ventas360.util.AppConstants;
import com.expediodigital.ventas360.util.AppSession;
import com.expediodigital.ventas360.util.Utilities;
import com.soundcloud.android.crop.Crop;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;


public class CameraActivity extends Activity implements AppConstants {
    private Intent intent;
    private Bitmap bitmap;
        private Utilities utilities;
    private Context context;
    private AppSession appSession;
    private File photoFile;
    private String picturePath = "", image = "", cropPicturePath = "";
    private Uri cameraUri = null;

    private static final int REQUEST = 112;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_image_chooser);
        this.setFinishOnTouchOutside(true);
        context = this;
        utilities = Utilities.getInstance(context);
        appSession = new AppSession(context);

        if (Build.VERSION.SDK_INT >= 23) {
            String[] PERMISSIONS = {android.Manifest.permission.READ_EXTERNAL_STORAGE,
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.CAMERA};
            if (!hasPermissions(this, PERMISSIONS)) {
                ActivityCompat.requestPermissions(this, PERMISSIONS, REQUEST );
            } else {
                //do here
                Log.i("Ventas360APp","Si tiene permisos el contador de botellas");
            }
        } else {
            //do here
            Log.i("Ventas360APp","La version de android no requiere permisos para el contador de botellas");
        }


            dailogImageChooser(context, "Choose Image");

    }

    private static boolean hasPermissions(Context context, String... permissions) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    public void dailogImageChooser(final Context context, String header) {

        TextView tvHeader = (TextView) findViewById(R.id.tv_header);
        TextView tvGallery = (TextView) findViewById(R.id.tv_gallery);
        TextView tvCamera = (TextView) findViewById(R.id.tv_camera);
        appSession = new AppSession(context);
        tvHeader.setText(header);
        tvCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    // Ensure that there's a camera activity to handle the intent
                    if (intent.resolveActivity(context.getPackageManager()) != null) {
                        // Create the File where the photo should go
                        photoFile = null;
                        try {
                            photoFile = createImageFile();
                        } catch (IOException ex) {
                            // Error occurred while creating the File
                            ex.printStackTrace();
                            return;
                        }
                        // Continue only if the File was successfully created
                        if (photoFile != null) {

                            cameraUri = FileProvider.getUriForFile(context,
                                    getApplicationContext().getPackageName() + ".provider", photoFile);


                            appSession.setImageUri(cameraUri);
                            intent.putExtra(MediaStore.EXTRA_OUTPUT, cameraUri);
                            startActivityForResult(intent, CAMERA);
                        }
                    }
                } else {

                    Crop.pickImage(CameraActivity.this);
                    /*
                    intent = new Intent();
                    intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    String fileName = "IMAGE_" + System.currentTimeMillis() + ".jpg";
                    cameraUri = Uri.fromFile(getNewFile(IMAGE_DIRECTORY, fileName));
                    appSession.setImageUri(cameraUri);
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, cameraUri);
                    intent.putExtra("return-data", true);
                    startActivityForResult(intent, CAMERA);*/
                }
            }
        });
        tvGallery.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                intent = new Intent(Intent.ACTION_PICK,
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(Intent.createChooser(intent, ""),
                        GALLERY);
            }
        });

    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        return image;
    }


    private Uri getTempUri() {
        return Uri.fromFile(getTempFile());
    }

    private File getTempFile() {
        String imageName = "CROP_" + System.currentTimeMillis() + ".jpg";
        File tempFile = getNewFile(IMAGE_DIRECTORY_CROP, imageName);
        cropPicturePath = tempFile.getPath();
        appSession = new AppSession(context);
        appSession.setCropImagePath(tempFile.getPath());
        return tempFile;
    }

    private void performCrop(Uri picUri) {

        try {
            Intent cropIntent = new Intent("com.android.camera.action.CROP");
            cropIntent.setDataAndType(picUri, "image/*");
            cropIntent.putExtra("crop", "true");
            cropIntent.putExtra(MediaStore.EXTRA_OUTPUT, getTempUri());
            cropIntent.putExtra("outputFormat",
                    Bitmap.CompressFormat.JPEG.toString());

            cropIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            cropIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

            startActivityForResult(cropIntent, CROP);
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
            Toast.makeText(context,
                    "cortar", Toast.LENGTH_SHORT)
                    .show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context,
                    "cortar", Toast.LENGTH_SHORT)
                    .show();
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        appSession = new AppSession(context);
        if (requestCode != ACTIVITY_RESULT && resultCode != RESULT_OK) {
            Intent intentMessage = new Intent();
            // put the message in Intent
            intentMessage.putExtra("image", image);
            // Set The Result in Intent
            setResult(0, intentMessage);
            // finish The activity
            finish();

        }
        if (requestCode == CROP && resultCode == RESULT_OK) {
            try {
                if (cropPicturePath == null || cropPicturePath.equals("")
                        || !new File(cropPicturePath).isFile())
                    cropPicturePath = appSession.getCropImagePath();

                if (cropPicturePath == null || cropPicturePath.equals("")
                        || !new File(cropPicturePath).isFile())
                    cropPicturePath = picturePath;

                if (cropPicturePath == null || cropPicturePath.equals("")
                        || !new File(cropPicturePath).isFile())
                    cropPicturePath = appSession.getImagePath();

                if (cropPicturePath != null && !cropPicturePath.equals("")
                        && new File(cropPicturePath).isFile()) {
                    if (bitmap != null)
                        bitmap.recycle();

//                    bitmap = new Compressor(this).compressToBitmap(new File(cropPicturePath));

                    bitmap = decodeFile(new File(cropPicturePath),
                            640, 640);
                    cropPicturePath = getFilePath(bitmap, context, cropPicturePath);

                    image = cropPicturePath;
                    Intent intentMessage = new Intent();
                    // put the message in Intent

                    intentMessage.putExtra("image", image);

                    // Set The Result in Intent

                    setResult(2, intentMessage);

                    // finish The activity
                    finish();
                    } else {
                    Toast.makeText(context,
                            "cortar",
                            Toast.LENGTH_LONG).show();
                    Intent intentMessage = new Intent();

                    // put the message in Intent
                    intentMessage.putExtra("image", image);
                    // Set The Result in Intent
                    setResult(0, intentMessage);
                    // finish The activity
                    finish();
                }
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(context,
                        "cortar",
                        Toast.LENGTH_LONG).show();
                Toast.makeText(context,
                        "cortar",
                        Toast.LENGTH_LONG).show();
                Intent intentMessage = new Intent();

                // put the message in Intent
                intentMessage.putExtra("image", image);
                // Set The Result in Intent
                setResult(0, intentMessage);
                // finish The activity
                finish();
            }
        } else if (resultCode != Activity.RESULT_CANCELED) {
            if (requestCode == GALLERY) {
                try {
                    Uri uriImage = data.getData();
                    if (uriImage != null) {
                        picturePath = getAbsolutePath(uriImage);
                        if (picturePath == null || picturePath.equals(""))
                            picturePath = uriImage.getPath();
                        appSession.setImagePath(picturePath);
                        Cursor cursor = context
                                .getContentResolver()
                                .query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                                        new String[]{MediaStore.Images.Media._ID},
                                        MediaStore.Images.Media.DATA + "=? ",
                                        new String[]{picturePath}, null);
                        if (cursor != null && cursor.moveToFirst()) {
                            int id = cursor.getInt(cursor.getColumnIndex(MediaStore.MediaColumns._ID));
                            uriImage = Uri.parse("content://media/external/images/media/" + id);
                        }
                        performCrop(uriImage);
                    } else {
                        Toast.makeText(context,
                                "Error al seleccionar foto",
                                Toast.LENGTH_LONG).show();
                        Toast.makeText(context,
                                "cortar",
                                Toast.LENGTH_LONG).show();
                        Intent intentMessage = new Intent();

                        // put the message in Intent
                        intentMessage.putExtra("image", image);
                        // Set The Result in Intent
                        setResult(0, intentMessage);
                        // finish The activity
                        finish();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(context,
                            "Error al seleccionar foto",
                            Toast.LENGTH_LONG).show();
                    Toast.makeText(context,
                            "cortar",
                            Toast.LENGTH_LONG).show();
                    Intent intentMessage = new Intent();

                    // put the message in Intent
                    intentMessage.putExtra("image", image);
                    // Set The Result in Intent
                    setResult(0, intentMessage);
                    // finish The activity
                    finish();
                }
            } else if (requestCode == CAMERA) {
                try {

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//                    Uri uri = Uri.parse(photoFile.getAbsolutePath());
                        picturePath = photoFile.getAbsolutePath();
                        appSession.setImagePath(picturePath);
                        cropPicturePath = picturePath;
                        Log.i(getClass().getName(), "Nougat Path >>>>>>>"+ cropPicturePath);

                       /* Intent intentMessage = new Intent();

                        // put the message in Intent
                        intentMessage.putExtra("image", cropPicturePath);
                        // Set The Result in Intent
                        setResult(2, intentMessage);
                        // finish The activity
                        finish();*/

                        Cursor cursor = context
                                .getContentResolver()
                                .query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                                        new String[]{MediaStore.Images.Media._ID},
                                        MediaStore.Images.Media.DATA + "=? ",
                                        new String[]{picturePath}, null);
                        if (cursor != null && cursor.moveToFirst()) {
                            int id = cursor
                                    .getInt(cursor
                                            .getColumnIndex(MediaStore.MediaColumns._ID));
                            cameraUri = Uri
                                    .parse("content://media/external/images/media/"
                                            + id);
                        }
                        performCrop(cameraUri);
                    } else {

                        if (cameraUri == null)
                            cameraUri = appSession.getImageUri();
                        if (cameraUri != null) {
                            picturePath = getAbsolutePath(cameraUri);
                            if (picturePath == null || picturePath.equals(""))
                                picturePath = cameraUri.getPath();
                            appSession.setImagePath(picturePath);

                            Log.i(getClass().getName(), "Simple Path >>>>>>>"+ picturePath);
                            Cursor cursor = context
                                    .getContentResolver()
                                    .query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                                            new String[]{MediaStore.Images.Media._ID},
                                            MediaStore.Images.Media.DATA + "=? ",
                                            new String[]{picturePath}, null);
                            if (cursor != null && cursor.moveToFirst()) {
                                int id = cursor
                                        .getInt(cursor
                                                .getColumnIndex(MediaStore.MediaColumns._ID));
                                cameraUri = Uri
                                        .parse("content://media/external/images/media/"
                                                + id);
                            }
                            performCrop(cameraUri);
                        } else {
                            Toast.makeText(context,
                                    "Error",
                                    Toast.LENGTH_LONG).show();
                            Toast.makeText(context,
                                    "cortar",
                                    Toast.LENGTH_LONG).show();
                            Intent intentMessage = new Intent();

                            // put the message in Intent
                            intentMessage.putExtra("image", image);
                            // Set The Result in Intent
                            setResult(0, intentMessage);
                            // finish The activity
                            finish();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(context,
                            "Error",
                            Toast.LENGTH_LONG).show();
                    Toast.makeText(context,
                            "cortar",
                            Toast.LENGTH_LONG).show();
                    Intent intentMessage = new Intent();

                    // put the message in Intent
                    intentMessage.putExtra("image", image);
                    // Set The Result in Intent
                    setResult(0, intentMessage);
                    // finish The activity
                    finish();
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Intent intentMessage = new Intent();
        // put the message in Intent
        intentMessage.putExtra("image", image);
        // Set The Result in Intent
        setResult(0, intentMessage);
        // finish The activity
        finish();
        }

    /**
     * This method used to create new file if not exist .
     */
    public File getNewFile(String directoryName, String imageName) {

        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        String root = storageDir + directoryName;
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
        Cursor cursor = context.getContentResolver().query(uri, projection,
                null, null, null);
        if (cursor != null) {
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } else
            return null;
    }

    public Bitmap decodeFile(File f, int REQUIRED_WIDTH,
                             int REQUIRED_HEIGHT) {
        try {
            ExifInterface exif = new ExifInterface(f.getPath());
            int orientation = exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL);
            int angle = 0;

            if (orientation == ExifInterface.ORIENTATION_ROTATE_90) {
                angle = 90;
            } else if (orientation == ExifInterface.ORIENTATION_ROTATE_180) {
                angle = 180;
            } else if (orientation == ExifInterface.ORIENTATION_ROTATE_270) {
                angle = 270;
            }

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

            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize = scale;
            o2.inPurgeable = true;
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

    public String getFilePath(Bitmap bitmap, Context context, String path) {
        //  File cacheDir;
        File file;

        try {

            if (bitmap != null) {
                file = new File(path);

                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 70, bytes);
                FileOutputStream fo;

                fo = new FileOutputStream(file);
                fo.write(bytes.toByteArray());
                fo.close();

                return file.getAbsolutePath();
            }

        } catch (Exception e1) {
            e1.printStackTrace();

        } catch (Error e1) {
            e1.printStackTrace();
        }

        return "";
    }

}
