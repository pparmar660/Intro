package com.intro.android;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Random;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.appsee.Appsee;
import com.facebook.Session;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.intro.customviews.CustomAlartDialog;
import com.intro.customviews.CustomDialog;
import com.intro.customviews.CustomEditText;
import com.intro.customviews.CustomTextView;
import com.intro.imageUtils.ImageLoader;
import com.intro.network.ConnectionDetector;
import com.intro.utils.GlobalVariables;
import com.intro.utils.Utility;
import com.intro.webservice.BaseActivity;
import com.microsoft.windowsazure.mobileservices.ApiJsonOperationCallback;
import com.microsoft.windowsazure.mobileservices.http.ServiceFilterResponse;
import com.microsoft.windowsazure.mobileservices.table.TableJsonOperationCallback;

/**
 * This class is for set user profile with name/age/aboutme description
 * Images are uploaded one by one when it set on imageview
 * By Ankit Kumar and Mirza Faisal
 */

public class SetProfileActivity extends BaseActivity {

    private Context context;
    private ConnectionDetector cd;
    private CustomEditText name, age, aboutMe;
    private ImageView imageView1, imageView2, imageView3, imageview1cros, imageview2cros, imageview3cros;
    private ImageButton back_arrow;
    private CustomTextView BtnSavepro, cancel;
    private CheckBox male, female;
    private ImageLoader imgL;
    private SharedPreferences userData;
    private String image1Path = "", image2Path = "", image3Path = "", imageName, imageUpload, imageUrl = "null", userID = "",
            gender, maleStr = "1", femaleStr = "1", def = "0", comeFrom, sasUrl;
    private Uri image1, image2, image3;
    private int ImageName;
    private boolean image1Up = false, image2Up = false, image3Up = false;
    private ArrayList<Integer> maleArray = new ArrayList<>();
    private ArrayList<Integer> femaleArray = new ArrayList<>();
    Drawable drawable = null;
    boolean data = false;
    private static String[] STORAGE_PERMISSIONS = {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};
    private static final int REQUEST_ID_STORAGE_PERMISSIONS = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setprofile);
        Appsee.start(getString(R.string.appsee_apikey));
        context = this;
        cd = new ConnectionDetector(getApplicationContext());
        BtnSavepro = (CustomTextView) findViewById(R.id.save_pro);
        cancel = (CustomTextView) findViewById(R.id.cancel);
        name = (CustomEditText) findViewById(R.id.name);
        age = (CustomEditText) findViewById(R.id.age);
        aboutMe = (CustomEditText) findViewById(R.id.aboutMe);
        imageView1 = (ImageView) findViewById(R.id.imageview1);
        imageView2 = (ImageView) findViewById(R.id.imageview2);
        imageView3 = (ImageView) findViewById(R.id.imageview3);
        imageview1cros = (ImageView) findViewById(R.id.imageview1cros);
        imageview2cros = (ImageView) findViewById(R.id.imageview2cros);
        imageview3cros = (ImageView) findViewById(R.id.imageview3cros);
        back_arrow = (ImageButton) findViewById(R.id.back_arrow);
        male = (CheckBox) findViewById(R.id.male);
        female = (CheckBox) findViewById(R.id.female);
        userData = getSharedPreferences("UserData", MODE_PRIVATE);
        Typeface font = Typeface.createFromAsset(getAssets(), "fonts/boton.ttf");
        aboutMe.setTypeface(font);

        comeFrom = getIntent().getStringExtra(GlobalVariables.comeFrom);
        userID = userData.getString("userid", "");
        name.setText(userData.getString("name", ""));
        age.setText(userData.getString("age", ""));
        aboutMe.setText(userData.getString("bio", ""));
        if (userData.getString("gender", "").equals("1")) {
            male.setChecked(true);
            gender = "1";
        } else {
            if (userData.getString("gender", "").equals("0")) {
                female.setChecked(true);
                gender = "0";
            }
        }

        if (getIntent().getBooleanExtra("cancel", false)) {
            cancel.setVisibility(View.VISIBLE);
        } else {
            cancel.setVisibility(View.GONE);
        }
        maleStr = userData.getString("wantsMales", "1");
        femaleStr = userData.getString("wantsFemales", "1");
        if(!Utility.hasPermissions(SetProfileActivity.this, STORAGE_PERMISSIONS)){
            ActivityCompat.requestPermissions(SetProfileActivity.this, STORAGE_PERMISSIONS, REQUEST_ID_STORAGE_PERMISSIONS);
        }else {
            try {
                imgL = new ImageLoader(context);
                if (userData.getString("Image1Uploaded", "").equalsIgnoreCase("1")) {
                    imgL.DisplayImage(GlobalVariables.Image_URL + userData.getString("userid", "") + "-1", imageView1);
                    imageview1cros.setVisibility(View.VISIBLE);
                }
                if (userData.getString("Image2Uploaded", "").equalsIgnoreCase("1")) {
                    imgL.DisplayImage(GlobalVariables.Image_URL + userData.getString("userid", "") + "-2", imageView2);
                    imageview2cros.setVisibility(View.VISIBLE);
                }
                if (userData.getString("Image3Uploaded", "").equalsIgnoreCase("1")) {
                    imgL.DisplayImage(GlobalVariables.Image_URL + userData.getString("userid", "") + "-3", imageView3);
                    imageview3cros.setVisibility(View.VISIBLE);
                }

                Boolean signUp = getIntent().getBooleanExtra("signUp", false);
                if (signUp || userData.getString("twitter", "").equalsIgnoreCase(GlobalVariables.twitterLogin)) {
                    imageview1cros.setVisibility(View.GONE);
                    imageview3cros.setVisibility(View.GONE);
                    imageview2cros.setVisibility(View.GONE);
                    image1Path = "AG";
                    image2Path = "AG";
                    image3Path = "AG";
                } else {
                    image1Path = GlobalVariables.Image_URL + userData.getString("userid", "") + "-1";
                    image2Path = GlobalVariables.Image_URL + userData.getString("userid", "") + "-2";
                    image3Path = GlobalVariables.Image_URL + userData.getString("userid", "") + "-3";
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        aboutMe.setImeOptions(EditorInfo.IME_ACTION_DONE);

        addlistener();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {

        switch (requestCode){
            case REQUEST_ID_STORAGE_PERMISSIONS:
                try {
                    imgL = new ImageLoader(context);
                    if (userData.getString("Image1Uploaded", "").equalsIgnoreCase("1")) {
                        imgL.DisplayImage(GlobalVariables.Image_URL + userData.getString("userid", "") + "-1", imageView1);
                        imageview1cros.setVisibility(View.VISIBLE);
                    }
                    if (userData.getString("Image2Uploaded", "").equalsIgnoreCase("1")) {
                        imgL.DisplayImage(GlobalVariables.Image_URL + userData.getString("userid", "") + "-2", imageView2);
                        imageview2cros.setVisibility(View.VISIBLE);
                    }
                    if (userData.getString("Image3Uploaded", "").equalsIgnoreCase("1")) {
                        imgL.DisplayImage(GlobalVariables.Image_URL + userData.getString("userid", "") + "-3", imageView3);
                        imageview3cros.setVisibility(View.VISIBLE);
                    }

                    Boolean signUp = getIntent().getBooleanExtra("signUp", false);
                    if (signUp || userData.getString("twitter", "").equalsIgnoreCase(GlobalVariables.twitterLogin)) {
                        imageview1cros.setVisibility(View.GONE);
                        imageview3cros.setVisibility(View.GONE);
                        imageview2cros.setVisibility(View.GONE);
                        image1Path = "AG";
                        image2Path = "AG";
                        image3Path = "AG";
                    } else {
                        image1Path = GlobalVariables.Image_URL + userData.getString("userid", "") + "-1";
                        image2Path = GlobalVariables.Image_URL + userData.getString("userid", "") + "-2";
                        image3Path = GlobalVariables.Image_URL + userData.getString("userid", "") + "-3";
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
        }

    }

    private void addlistener() {
        cancel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteUser();
            }
        });
        back_arrow.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (comeFrom.equalsIgnoreCase("Setting Activity")) {
                    finish();
                    overridePendingTransition(0, R.anim.exit_slide_left);
                } else {
                    saveData();
                }
            }
        });
        imageView1.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                ImageName = 1;
                imageName = "Image1";
                imageUpload = "Image1Uploaded";
                imageUrl = userData.getString("userid", "") + "-1";
                Intent intent = new Intent(context, AddPhotosActivity.class);
                startActivityForResult(intent, 1);
                overridePendingTransition(0, R.anim.exit_slide_right);
            }
        });
        imageView2.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                ImageName = 2;
                imageName = "Image2";
                imageUpload = "Image2Uploaded";
                imageUrl = userData.getString("userid", "") + "-2";
                Intent intent = new Intent(context, AddPhotosActivity.class);
                startActivityForResult(intent, 2);
                overridePendingTransition(0, R.anim.exit_slide_right);
            }
        });
        imageView3.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                ImageName = 3;
                imageName = "Image3";
                imageUpload = "Image3Uploaded";
                imageUrl = userData.getString("userid", "") + "-3";
                Intent intent = new Intent(context, AddPhotosActivity.class);
                startActivityForResult(intent, 3);
                overridePendingTransition(0, R.anim.exit_slide_right);
            }
        });
        BtnSavepro.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                saveData();
            }

        });

        male.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView,
                                         boolean isChecked) {

                if (isChecked) {
                    gender = "1";
                    female.setChecked(false);
                } else {
                    female.setChecked(true);
                    gender = "0";
                }
            }
        });

        female.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView,
                                         boolean isChecked) {

                if (isChecked) {
                    male.setChecked(false);
                    gender = "0";
                } else {
                    male.setChecked(true);
                    gender = "1";
                }
            }
        });
    }

    public class uploadSingleImage extends AsyncTask<String, Void, Boolean> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }

        @Override
        protected Boolean doInBackground(String... params) {
            String sasUrl = params[0], imageData = params[1];
            boolean imageUploadData = false;
            try {
                if (ImageName == 1) {
                    image1Up = true;
                } else if (ImageName == 2) {
                    image2Up = true;
                } else if (ImageName == 3) {
                    image3Up = true;
                }
                // Get the file data
                File file = new File(imageData);
                if (!file.exists()) {
                    return false;
                }
                String absoluteFilePath = file.getAbsolutePath();
                FileInputStream fis = new FileInputStream(absoluteFilePath);
                int bytesRead = 0;
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                byte[] b = new byte[1024];
                while ((bytesRead = fis.read(b)) != -1) {
                    bos.write(b, 0, bytesRead);
                }
                fis.close();
                byte[] bytes = bos.toByteArray();
                // Post our image data (byte array) to the server
                URL url = new URL(sasUrl);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setDoOutput(true);
                urlConnection.setConnectTimeout(30000);
                urlConnection.setReadTimeout(30000);
                urlConnection.setRequestMethod("PUT");
                urlConnection.addRequestProperty("Content-Type", "image/jpeg");
                urlConnection.setRequestProperty("x-ms-blob-type", "BlockBlob");
                urlConnection.setRequestProperty("Content-Length", "" + bytes.length);
                // Write file data to server
                DataOutputStream wr = new DataOutputStream(urlConnection.getOutputStream());
                wr.write(bytes);
                wr.flush();
                wr.close();
                int response = urlConnection.getResponseCode();
                if (response == 201 && urlConnection.getResponseMessage().equals("Created")) {
                    System.out.println("ImageUpload = " + urlConnection.getResponseMessage());
                    //imgL.ClearImage();
                    SharedPreferences.Editor editor = userData.edit();
                    editor.putString(imageUpload, "1");
                    editor.putString(imageName, imageUrl);
                    editor.commit();

                    imageUploadData = uploadImages();
                    return imageUploadData;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if(progressDia != null && progressDia.isShowing()){
                progressDia.dismiss();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.v("SetProfileActivity", "OnActivityResult");
        progressDia = CustomDialog.show(context, GlobalVariables.loading);
        try {
            switch (requestCode) {
                case 1:
                    if (!data.getStringExtra("bitmap").equalsIgnoreCase("null")) {
                        image1 = Uri.parse(data.getStringExtra("bitmap"));
                        imageView1.setImageURI(image1);
                        image1Path = getRealPathFromURI(context, image1);
                        mAuthService.getSasUrlForNewBlob(userID + "-" + ImageName, "introappimages", new TableJsonOperationCallback() {

                            @Override
                            public void onCompleted(
                                    final JsonObject jsonObject,
                                    Exception exception,
                                    ServiceFilterResponse response) {
                                try {
                                    if (exception == null) {
                                        System.out.println("Image1Result = " + jsonObject.toString());
                                        new Thread(new Runnable() {
                                            public void run() {
                                               new uploadSingleImage().execute(jsonObject.getAsJsonPrimitive("sasUrl").getAsString(), image1Path);
                                            }
                                        }).start();
                                    } else {
                                        System.out.println("Image1Exception= " + exception.toString());
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                        imageview1cros.setVisibility(View.VISIBLE);
                    }
                    break;
                case 2:
                    if (!data.getStringExtra("bitmap").equalsIgnoreCase("null")) {
                        image2 = Uri.parse(data.getStringExtra("bitmap"));
                        imageView2.setImageURI(image2);
                        image2Path = getRealPathFromURI(context, image2);
                        mAuthService.getSasUrlForNewBlob(userID + "-" + ImageName, "introappimages", new TableJsonOperationCallback() {

                            @Override
                            public void onCompleted(
                                    final JsonObject jsonObject,
                                    Exception exception,
                                    ServiceFilterResponse response) {
                                try {
                                    if (exception == null) {
                                        System.out.println("Image2Result = " + jsonObject.toString());
                                        new Thread(new Runnable() {
                                            public void run() {
                                                new uploadSingleImage().execute(jsonObject.getAsJsonPrimitive("sasUrl").getAsString(), image2Path);
                                            }
                                        }).start();
                                    } else {
                                        System.out.println("Image2Exception= " + exception.toString());
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                        imageview2cros.setVisibility(View.VISIBLE);
                    }
                    break;
                case 3:
                    if (!data.getStringExtra("bitmap").equalsIgnoreCase("null")) {
                        image3 = Uri.parse(data.getStringExtra("bitmap"));
                        imageView3.setImageURI(image3);
                        image3Path = getRealPathFromURI(context, image3);
                        mAuthService.getSasUrlForNewBlob(userID + "-" + ImageName, "introappimages", new TableJsonOperationCallback() {

                            @Override
                            public void onCompleted(
                                    final JsonObject jsonObject,
                                    Exception exception,
                                    ServiceFilterResponse response) {
                                try {
                                    if (exception == null) {
                                        System.out.println("Image3Result = " + jsonObject.toString());
                                        new Thread(new Runnable() {
                                            public void run() {
                                                new uploadSingleImage().execute(jsonObject.getAsJsonPrimitive("sasUrl").getAsString(), image3Path);
                                            }
                                        }).start();
                                    } else {
                                        System.out.println("Image3Exception= " + exception.toString());
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                        imageview3cros.setVisibility(View.VISIBLE);
                    }
                    break;
                default:
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
            if(progressDia != null && progressDia.isShowing()){
                progressDia.dismiss();
            }
        }
    }

    public String getRealPathFromURI(Context context, Uri contentUri) {
        Cursor cursor = null;
        try {
            String[] proj = {MediaStore.Images.Media.DATA};
            cursor = context.getContentResolver().query(contentUri, proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private void setUserData() {
        try {
            String firebaseID = userData.getString("firebaseid", "");
            if (firebaseID.equalsIgnoreCase("null")) {
                firebaseID = "";
            }
            final ArrayList<Pair<String, String>> parameters = new ArrayList<Pair<String, String>>();
            parameters.add(new Pair<String, String>("userid", userID));
            parameters.add(new Pair<String, String>("email", userData.getString("email", "")));
            if (aboutMe.getText().toString().contains("'")) {
                parameters.add(new Pair<String, String>("bio", encodeUTF(aboutMe.getText().toString().replaceAll("'", "''"))));
            } else {
                parameters.add(new Pair<String, String>("bio", encodeUTF(aboutMe.getText().toString())));
            }
            parameters.add(new Pair<String, String>("age", age.getText().toString()));
            parameters.add(new Pair<String, String>("deviceid", userData.getString("userid", "").replaceAll("-", "")));
            parameters.add(new Pair<String, String>("gender", gender));
            parameters.add(new Pair<String, String>("name", encodeUTF(name.getText().toString())));
            parameters.add(new Pair<String, String>("birthday", userData.getString("birthday", "")));
            parameters.add(new Pair<String, String>("high", userData.getString("high", "100")));
            parameters.add(new Pair<String, String>("low", userData.getString("low", "18")));
            parameters.add(new Pair<String, String>("wants_males", maleStr));
            parameters.add(new Pair<String, String>("wants_females", femaleStr));
            parameters.add(new Pair<String, String>("firebaseid", firebaseID));
            parameters.add(new Pair<String, String>("image1_uploaded", userData.getString("Image1Uploaded", "0")));
            parameters.add(new Pair<String, String>("image2_uploaded", userData.getString("Image2Uploaded", "0")));
            parameters.add(new Pair<String, String>("image3_uploaded", userData.getString("Image3Uploaded", "0")));
            if (GlobalVariables.customLogin) {
                parameters.add(new Pair<String, String>("facebookid", ""));
            } else {
                parameters.add(new Pair<String, String>("facebookid", userData.getString("facebookid", "")));
            }
            mAuthService.setUserProfile(parameters, new ApiJsonOperationCallback() {
                @Override
                public void onCompleted(JsonElement jsonElement,
                                        Exception error,
                                        ServiceFilterResponse serviceFilterResponse) {
                    if (progressDia != null && progressDia.isShowing())
                        progressDia.dismiss();
                    if (error == null) {
                        System.out.println("ResultSetProfile" + jsonElement.toString());
                        SharedPreferences.Editor preferencesEditor = userData.edit();
                        preferencesEditor.putString("name", name.getText().toString());
                        preferencesEditor.putString("age", age.getText().toString());
                        preferencesEditor.putString("gender", gender);
                        preferencesEditor.putString("bio", aboutMe.getText().toString());
                        preferencesEditor.commit();
                        Intent intent = new Intent(context, ProfileActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        overridePendingTransition(0, R.anim.exit_slide_right);
                        // finish();
                    } else {
                        alart = new CustomAlartDialog(context, GlobalVariables.oh_snap, error.toString());
                        alart.show();
                    }
                }
            });
        } catch (Exception e) {
            runOnUiThread(new Runnable() {
                public void run() {
                    if (progressDia != null && progressDia.isShowing())
                        progressDia.dismiss();
                }
            });
            e.printStackTrace();
        }
    }

    private Boolean uploadImages() {
        try {
            final String urlStr = userID + "-" + ImageName;
            final ArrayList<Pair<String, String>> parameters = new ArrayList<Pair<String, String>>();
            parameters.add(new Pair<String, String>("url", urlStr));
            parameters.add(new Pair<String, String>("userid", userID));
            parameters.add(new Pair<String, String>("def", def));
            parameters.add(new Pair<String, String>("no", ImageName + ""));
            mAuthService.setUserImage(parameters,
                    new ApiJsonOperationCallback() {

                        @Override
                        public void onCompleted(JsonElement jsonElement,
                                                Exception exception,
                                                ServiceFilterResponse response) {
                            if (exception == null) {
                                imgL.ClearSingleImage(GlobalVariables.Image_URL + urlStr);
                                System.out.println("===ImagejsonElement : " + jsonElement.toString());
                                //imgL.ClearImage();
                                data = true;
                            } else {
                                System.out.println("===ImageException : " + exception.toString());
                                alart = new CustomAlartDialog(context, GlobalVariables.oh_snap, exception.toString());
                                alart.show();
                                data = false;
                            }
                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }
        return data;
    }

    public void saveData() {
        if (cd.isConnectingToInternet()) {
            if ((name.getText().toString().trim().length() < 1) || (age.getText().toString().trim().length() < 1)
                    || (!(male.isChecked() || female.isChecked()))) {
                alart = new CustomAlartDialog(context, GlobalVariables.oh_snap, GlobalVariables.set_profile_error);
                alart.show();
            } else {
                if (Integer.valueOf(age.getText().toString()) < 18) {
                    alart = new CustomAlartDialog(context, GlobalVariables.oh_snap, GlobalVariables.age_error);
                    alart.show();
                } else {
                    progressDia = CustomDialog.show(context, GlobalVariables.loading);
                    if (male.isChecked()) {
                        for (int i = 0; i < maleImages.length; i++) {
                            maleArray.add(maleImages[i]);
                        }
                    } else if (female.isChecked()) {
                        for (int i = 0; i < femaleImages.length; i++) {
                            femaleArray.add(femaleImages[i]);
                        }
                    }

                    if (!image1Up && userData.getString("Image1Uploaded", "0").equals("0")) {
                        mAuthService.getSasUrlForNewBlob(userID + "-" + 1, "introappimages", new TableJsonOperationCallback() {

                            @Override
                            public void onCompleted(
                                    final JsonObject jsonObject,
                                    Exception exception,
                                    ServiceFilterResponse response) {
                                try {
                                    if (exception == null) {
                                        System.out.println("Image1Result = " + jsonObject.toString());
                                        ImageName = 1;
                                        new uploadImage().execute(jsonObject.getAsJsonPrimitive("sasUrl").getAsString(), "1");
                                    } else {
                                        System.out.println("Image1Exception= " + exception.toString());
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    } else if (!image2Up && userData.getString("Image2Uploaded", "0").equals("0")) {
                        mAuthService.getSasUrlForNewBlob(userID + "-" + 2, "introappimages", new TableJsonOperationCallback() {

                            @Override
                            public void onCompleted(
                                    final JsonObject jsonObject,
                                    Exception exception,
                                    ServiceFilterResponse response) {
                                try {
                                    if (exception == null) {
                                        System.out.println("Image2Result = " + jsonObject.toString());
                                        ImageName = 2;
                                        new uploadImage().execute(jsonObject.getAsJsonPrimitive("sasUrl").getAsString(), "2");
                                    } else {
                                        System.out.println("Image2Exception= " + exception.toString());
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    } else if (!image3Up && userData.getString("Image3Uploaded", "0").equals("0")) {
                        mAuthService.getSasUrlForNewBlob(userID + "-" + 3, "introappimages", new TableJsonOperationCallback() {

                            @Override
                            public void onCompleted(
                                    final JsonObject jsonObject,
                                    Exception exception,
                                    ServiceFilterResponse response) {
                                try {
                                    if (exception == null) {
                                        System.out.println("Image3Result = " + jsonObject.toString());
                                        ImageName = 3;
                                        new uploadImage().execute(jsonObject.getAsJsonPrimitive("sasUrl").getAsString(), "3");
                                    } else {
                                        System.out.println("Image3Exception= " + exception.toString());
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    } else {
                        setUserData();
                    }
                }
            }
        } else {
            if (progressDia != null && progressDia.isShowing())
                progressDia.dismiss();
            alart = new CustomAlartDialog(context, GlobalVariables.oh_snap, GlobalVariables.network_error);
            alart.show();
        }
    }

    public class uploadImage extends AsyncTask<String, Void, Boolean> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (male.isChecked()) {
                int maleId = new Random().nextInt(maleArray.size());
                drawable = getResources().getDrawable(maleArray.get(maleId));
                maleArray.remove(maleId);
            } else if (female.isChecked()) {
                int femaleId = new Random().nextInt(femaleArray.size());
                drawable = getResources().getDrawable(femaleArray.get(femaleId));
                femaleArray.remove(femaleId);
            }
        }

        @Override
        protected Boolean doInBackground(String... params) {
            String sasUrl = params[0], imageData = params[1];
            boolean imageUploadData = false;
            try {
                Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                byte[] bytes = stream.toByteArray();

                URL url = new URL(sasUrl);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setDoOutput(true);
                urlConnection.setConnectTimeout(30000);
                urlConnection.setReadTimeout(30000);
                urlConnection.setRequestMethod("PUT");
                urlConnection.addRequestProperty("Content-Type", "image/jpeg");
                urlConnection.setRequestProperty("x-ms-blob-type", "BlockBlob");
                urlConnection.setRequestProperty("Content-Length", "" + bytes.length);
                // Write file data to server
                DataOutputStream wr = new DataOutputStream(urlConnection.getOutputStream());
                wr.write(bytes);
                wr.flush();
                wr.close();
                int response = urlConnection.getResponseCode();
                if (response == 201 && urlConnection.getResponseMessage().equals("Created")) {
                    System.out.println("ImageUpload = " + urlConnection.getResponseMessage());
                    //imgL.ClearImage();
                    SharedPreferences.Editor editor = userData.edit();
                    editor.putString(imageUpload, "1");
                    editor.putString(imageName, imageUrl);


                    imageUploadData = uploadImages();
                    if (imageData == "1") {
                        image1Up = true;
                    } else if (imageData == "2") {
                        image2Up = true;
                    } else if (imageData == "3") {
                        image3Up = true;
                    }
                    editor.commit();
                    return imageUploadData;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (!image1Up && userData.getString("Image1Uploaded", "0").equals("0")) {
                mAuthService.getSasUrlForNewBlob(userID + "-" + 1, "introappimages", new TableJsonOperationCallback() {

                    @Override
                    public void onCompleted(
                            final JsonObject jsonObject,
                            Exception exception,
                            ServiceFilterResponse response) {
                        try {
                            if (exception == null) {
                                ImageName = 1;
                                new uploadImage().execute(jsonObject.getAsJsonPrimitive("sasUrl").getAsString(), "1");
                            } else {
                                System.out.println("Image1Exception= " + exception.toString());
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            } else if (!image2Up && userData.getString("Image2Uploaded", "0").equals("0")) {
                mAuthService.getSasUrlForNewBlob(userID + "-" + 2, "introappimages", new TableJsonOperationCallback() {

                    @Override
                    public void onCompleted(
                            final JsonObject jsonObject,
                            Exception exception,
                            ServiceFilterResponse response) {
                        try {
                            if (exception == null) {
                                ImageName = 2;
                                new uploadImage().execute(jsonObject.getAsJsonPrimitive("sasUrl").getAsString(), "2");
                            } else {
                                System.out.println("Image2Exception= " + exception.toString());
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            } else if (!image3Up && userData.getString("Image3Uploaded", "0").equals("0")) {
                mAuthService.getSasUrlForNewBlob(userID + "-" + 3, "introappimages", new TableJsonOperationCallback() {

                    @Override
                    public void onCompleted(
                            final JsonObject jsonObject,
                            Exception exception,
                            ServiceFilterResponse response) {
                        try {
                            if (exception == null) {
                                ImageName = 3;
                                new uploadImage().execute(jsonObject.getAsJsonPrimitive("sasUrl").getAsString(), "3");
                            } else {
                                System.out.println("Image3Exception= " + exception.toString());
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            } else {
                setUserData();
            }
        }
    }

    public void deleteUser() {
        try {
            GlobalVariables.customLogin = false;
            final ArrayList<Pair<String, String>> parameters = new ArrayList<Pair<String, String>>();
            parameters.add(new Pair<String, String>("userid", userData.getString("userid", "")));
            mAuthService.deleteUser(parameters, new ApiJsonOperationCallback() {

                @Override
                public void onCompleted(JsonElement jsonElement, Exception exception, ServiceFilterResponse response) {
                    if (exception == null) {
                        System.out.println("DeleteUserResult = " + jsonElement.toString());
                        mAuthService.logout(false, context);
                    } else {
                        System.out.println("DeleteUserException = " + exception.toString());
                    }
                }
            });
            mAuthService.logout(false, context);
            Session session = Session.getActiveSession();
            if (session != null) {
                if (!session.isClosed()) {
                    session.closeAndClearTokenInformation();
                }
            } else {
                session = new Session(context);
                Session.setActiveSession(session);
                session.closeAndClearTokenInformation();
            }
            Session.setActiveSession(null);
            Intent intent = new Intent(SetProfileActivity.this, LoginActivity.class);
            intent.addCategory(Intent.CATEGORY_HOME);
            intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.addFlags(Intent.FLAG_ACTIVITY_TASK_ON_HOME);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
            overridePendingTransition(0, R.anim.exit_slide_right);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onBackPressed() {
        finish();
        overridePendingTransition(0, R.anim.exit_slide_left);
    }
}
