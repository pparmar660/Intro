package com.intro.android;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.Toast;

import com.appsee.Appsee;
import com.edmodo.cropper.CropImageView;
import com.intro.utils.Utility;

/**
 * This class is for the crop the image after selecting from gallery, camera or facebook.
 * By Ankit Kumar and Mirza Faisal
 */


public class CropperActivity extends Activity {
    private Bitmap finalBitmap;

    private CropImageView cropImageView;
    private String imagePath, photoStr;
    private TextView save, reset, cancel;
    private SharedPreferences imagePaths;
    private Context context;
    private Bitmap bMap;
    private File pictureFile;
    private static String[] PERMISSIONS = {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.ACCESS_FINE_LOCATION};
    private static final int REQUEST_ID_PERMISSIONS = 1;

    @SuppressLint("NewApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cropper);
        Appsee.start(getString(R.string.appsee_apikey));
        context = this;
        cropImageView = (CropImageView) findViewById(R.id.cropImageView);
        save = (TextView) findViewById(R.id.save);
        reset = (TextView) findViewById(R.id.reset);
        cancel = (TextView) findViewById(R.id.cancel);

        if(!Utility.hasPermissions(CropperActivity.this, PERMISSIONS)){
            ActivityCompat.requestPermissions(CropperActivity.this, PERMISSIONS, REQUEST_ID_PERMISSIONS);
        }else{
            setImage();
        }

        addlistener();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {

        switch (requestCode){
            case REQUEST_ID_PERMISSIONS:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    setImage();
                }
                break;
        }
    }

    public void setImage(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            cropImageView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }
        cropImageView.setFixedAspectRatio(true);
        try {
            pictureFile = (File) getIntent().getExtras().get("URI");
            photoStr = getIntent().getStringExtra("imagerotate");
            System.out.println("File : " + pictureFile);
            bMap = decodeFile(pictureFile);
            cropImageView.setImageBitmap(bMap);
            cropImageView.setGuidelines(0);
        } catch (Exception e1) {
            e1.printStackTrace();
            try {
                imagePaths = getSharedPreferences("imagePaths", MODE_PRIVATE);
                imagePath = imagePaths.getString("imagePath", "");
                System.out.println("imagePath" + imagePath);
                finalBitmap = BitmapFactory.decodeFile(imagePath);
                cropImageView.setImageBitmap(finalBitmap);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    private Bitmap decodeFile(File f) {
        try {
            // Decode image size
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            FileInputStream stream1 = new FileInputStream(f);
            BitmapFactory.decodeStream(stream1, null, o);
            stream1.close();

            final int REQUIRED_SIZE = 512;
            int width_tmp = o.outWidth, height_tmp = o.outHeight;
            int scale = 0;
            while (true) {
                if (width_tmp / 2 < REQUIRED_SIZE || height_tmp / 2 < REQUIRED_SIZE)
                    break;
                width_tmp /= 2;
                height_tmp /= 2;
                scale++;
            }
            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize = scale;
            FileInputStream stream2 = new FileInputStream(f);
            Bitmap bitmap = BitmapFactory.decodeStream(stream2, null, o2);
            stream2.close();

            ExifInterface exif = new ExifInterface(f.getAbsolutePath());
            int orientation = exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION, 1);
            Log.e("ExifInteface .........", "rotation =" + orientation);
            Matrix matrix = new Matrix();
            if (orientation != 0) {
                switch (orientation) {
                    case ExifInterface.ORIENTATION_NORMAL:
                        return bitmap;
                    case ExifInterface.ORIENTATION_FLIP_HORIZONTAL:
                        matrix.setScale(-1, 1);
                        bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
                        break;
                    case ExifInterface.ORIENTATION_ROTATE_180:
                        matrix.setRotate(180);
                        bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
                        break;
                    case ExifInterface.ORIENTATION_FLIP_VERTICAL:
                        matrix.setRotate(180);
                        matrix.postScale(-1, 1);
                        bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
                        break;
                    case ExifInterface.ORIENTATION_TRANSPOSE:
                        matrix.setRotate(90);
                        matrix.postScale(-1, 1);
                        bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
                        break;
                    case ExifInterface.ORIENTATION_ROTATE_90:
                        matrix.setRotate(90);
                        bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
                        break;
                    case ExifInterface.ORIENTATION_TRANSVERSE:
                        matrix.setRotate(-90);
                        matrix.postScale(-1, 1);
                        bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
                        break;
                    case ExifInterface.ORIENTATION_ROTATE_270:
                        matrix.setRotate(-90);
                        bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
                        break;
                    default:
                        return bitmap;
                }
            } else {
                if (photoStr.equalsIgnoreCase("Camera")) {
                    matrix.postRotate(0);
                    bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
                }
            }
            return bitmap;
        } catch (FileNotFoundException e) {
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void addlistener() {
        save.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                try {
                    Bitmap bitmap = cropImageView.getCroppedImage();
                    String path = getImagePath(CropperActivity.this, bitmap);
                    Intent data = new Intent();
                    data.putExtra("bitmap", path);
                    setResult(RESULT_OK, data);
                    finish();
                }catch (Exception e){
                    e.printStackTrace();
                    Toast.makeText(context, "Some problem occurs, please upload image again", Toast.LENGTH_LONG).show();
                }
            }
        });
        reset.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                try {
                    pictureFile = (File) getIntent().getExtras().get("URI");
                    photoStr = getIntent().getStringExtra("imagerotate");
                    System.out.println("File : " + pictureFile);
                    bMap = decodeFile(pictureFile);
                    cropImageView.setImageBitmap(bMap);
                    cropImageView.setGuidelines(0);
                } catch (Exception e1) {
                    e1.printStackTrace();
                    try {
                        imagePaths = getSharedPreferences("imagePaths", MODE_PRIVATE);
                        imagePath = imagePaths.getString("imagePath", "");
                        System.out.println("imagePath" + imagePath);
                        finalBitmap = BitmapFactory.decodeFile(imagePath);
                        cropImageView.setImageBitmap(finalBitmap);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        cancel.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent returnIntent = new Intent();
                setResult(RESULT_CANCELED, returnIntent);
                finish();
                overridePendingTransition(0, R.anim.exit_slide_left);
            }
        });
    }

    private String getImagePath(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 60, bytes);
        try {
            String imageURL = MediaStore.Images.Media.insertImage(
                    inContext.getContentResolver(), inImage, "!ntro", "cropped image");
            return imageURL;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void onBackPressed() {
        Intent returnIntent = new Intent();
        setResult(RESULT_CANCELED, returnIntent);
        finish();
        overridePendingTransition(0, R.anim.exit_slide_left);
    }
}