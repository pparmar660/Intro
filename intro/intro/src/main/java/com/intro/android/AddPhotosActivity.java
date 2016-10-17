package com.intro.android;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.appsee.Appsee;
import com.facebook.HttpMethod;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.model.GraphUser;
import com.facebook.widget.LoginButton;
import com.intro.customviews.CustomAlartDialogTwoBtn;
import com.intro.customviews.CustomDialog;
import com.intro.utils.GlobalVariables;
import com.intro.utils.GlobalVariables.SERVICE_TAG;
import com.intro.utils.Utility;
import com.intro.webservice.BaseActivity;
import com.intro.webservice.JSONListener;


/**
 * AddPhotosActivity is screen that show three option for choosing images from gellary, camera and facebook.
 * With facebook it check current account is of facebook or not if account is of facebook so then fetch images from facebook
 * if other account then login to facebook to fetch images
 * By Ankit Kumar & Mirza Faisal
 */

public class AddPhotosActivity extends BaseActivity {
    private LinearLayout photos, cameras, facebook;
    private Context context;
    protected static final String TAG = "AddPhotosActivity";
    private Uri mImageUri;
    private static final int REQUEST_GALLERY = 1;
    private static final int REQUEST_KITKAT_GALLERY = 2;
    private static final int REQUEST_CAMERA = 3;
    private static final int REQUEST_CROPPER = 0;
    private ImageButton back_arrow;
    private SharedPreferences imagePaths;
    private SharedPreferences userData;
    private String facebookID = "", accessToken = "";
    private static final int GET_REQUEST = 4;
    public static Bitmap imageBitmap;
    private LoginButton facebookButton;
    private UiLifecycleHelper uiHelper;
    private Session.StatusCallback statusCallback = new SessionStatusCallback();
    File photo = null;
    private boolean customLogin = false;
    private static String[] CAMERA_PERMISSIONS = {Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};
    private static String[] GALLERY_PERMISSIONS = {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};
    private static final int REQUEST_ID_GALLERY_PERMISSIONS = 6;
    private static final int REQUEST_ID_CAMERA_PERMISSIONS = 5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        uiHelper = new UiLifecycleHelper(this, statusCallback);
        uiHelper.onCreate(savedInstanceState);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_addphotos);
        Appsee.start(getString(R.string.appsee_apikey));
        context = this;
        photos = (LinearLayout) findViewById(R.id.photos);
        cameras = (LinearLayout) findViewById(R.id.cameras);
        facebook = (LinearLayout) findViewById(R.id.facebook);
        back_arrow = (ImageButton) findViewById(R.id.back_arrow);
        facebookButton = (LoginButton) findViewById(R.id.facebook_button);
        facebookButton.setReadPermissions(Arrays.asList("email",
                "user_birthday", "public_profile", "user_photos"));
        imagePaths = getSharedPreferences("imagePaths", MODE_PRIVATE);
        addlistener();
        /*if (!Utility.hasPermissions(this, PERMISSIONS)) {
            ActivityCompat.requestPermissions(AddPhotosActivity.this, PERMISSIONS, REQUEST_ID_MULTIPLE_PERMISSIONS);
        } else {
            addlistener();
        }*/
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {

        switch (requestCode){
            case REQUEST_ID_GALLERY_PERMISSIONS:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (Build.VERSION.SDK_INT < 19) {
                        Intent intent = new Intent();
                        intent.setType("image/*");
                        intent.setAction(Intent.ACTION_GET_CONTENT);
                        startActivityForResult(Intent.createChooser(intent, "Select Picture"), REQUEST_GALLERY);
                        overridePendingTransition(0, R.anim.exit_slide_right);
                    } else {
                        Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        startActivityForResult(intent, REQUEST_KITKAT_GALLERY);
                        overridePendingTransition(0, R.anim.exit_slide_right);
                    }
                }
                break;
            case REQUEST_ID_CAMERA_PERMISSIONS:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    try {
                        // place where to store camera taken picture
                        photo = createTemporaryFile("picture", ".jpg");
                        photo.delete();
                        mImageUri = Uri.fromFile(photo);
                    } catch (Exception e) {
                    }
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, mImageUri);
                    startActivityForResult(intent, REQUEST_CAMERA);
                    overridePendingTransition(0, R.anim.exit_slide_right);
                }
        }

    }

    private void addlistener() {
        back_arrow.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                finish();
                overridePendingTransition(0, R.anim.exit_slide_left);
            }
        });

        facebook.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                userData = getSharedPreferences("UserData", MODE_PRIVATE);
                facebookID = userData.getString("facebookid", "");
                accessToken = userData.getString("accessToken", "");
                if (facebookID.equalsIgnoreCase("")) {
                    final CustomAlartDialogTwoBtn alart = new CustomAlartDialogTwoBtn(context,
                            GlobalVariables.oh_snap, "Login to facebook to access facebook images", "Cancel", "Ok");
                    alart.show();
                    CustomAlartDialogTwoBtn.okBtn
                            .setOnClickListener(new OnClickListener() {

                                @Override
                                public void onClick(View v) {
                                    alart.dismiss();
                                    progressDia = CustomDialog.show(context, GlobalVariables.loading);
                                    facebookButton.performClick();
                                    GlobalVariables.customLogin = true;
                                }
                            });
                } else {
                    progressDia = CustomDialog.show(context, GlobalVariables.loading);
                    getFBImages(facebookID, accessToken);
                }
            }

        });
        photos.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                if (!Utility.hasPermissions(AddPhotosActivity.this, GALLERY_PERMISSIONS)) {
                    ActivityCompat.requestPermissions(AddPhotosActivity.this, GALLERY_PERMISSIONS, REQUEST_ID_GALLERY_PERMISSIONS);
                } else{
                    if (Build.VERSION.SDK_INT < 19) {
                        Intent intent = new Intent();
                        intent.setType("image/*");
                        intent.setAction(Intent.ACTION_GET_CONTENT);
                        startActivityForResult(Intent.createChooser(intent, "Select Picture"), REQUEST_GALLERY);
                        overridePendingTransition(0, R.anim.exit_slide_right);
                    } else {
                        Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        startActivityForResult(intent, REQUEST_KITKAT_GALLERY);
                        overridePendingTransition(0, R.anim.exit_slide_right);
                    }
                }

            }
        });
        cameras.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (!Utility.hasPermissions(AddPhotosActivity.this, CAMERA_PERMISSIONS)) {
                    ActivityCompat.requestPermissions(AddPhotosActivity.this, CAMERA_PERMISSIONS, REQUEST_ID_CAMERA_PERMISSIONS);
                } else {
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    try {
                        // place where to store camera taken picture
                        photo = createTemporaryFile("picture", ".jpg");
                        photo.delete();
                        mImageUri = Uri.fromFile(photo);
                    } catch (Exception e) {
                    }
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, mImageUri);
                    startActivityForResult(intent, REQUEST_CAMERA);
                    overridePendingTransition(0, R.anim.exit_slide_right);
                }
            }
        });
    }

    private class SessionStatusCallback implements Session.StatusCallback {

        @Override
        public void call(Session session, SessionState state, Exception exception) {
            Log.e("FB", "Logged statusCallback...");
            if (session.isOpened() && session != null) {
                onSessionStateChange(session, state, exception);
            }
        }
    }

    // same for every class For log In
    JSONListener getResponse = new JSONListener() {

        @Override
        public void onRemoteCallComplete(JSONObject jsonFromNet, SERVICE_TAG tag) {
            if (jsonFromNet != null) {
                switch (tag) {
                    case LOGIN:
                        System.out.println("jsonFromNet" + jsonFromNet.toString());
                        break;
                }
            }
        }
    };

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        uiHelper.onSaveInstanceState(outState);
    }

    private void onSessionStateChange(final Session session, SessionState state, Exception exception) {
        if (state.isOpened()) {
            Log.e("FB", "Logged in...");
            Request.newMeRequest(session, new Request.GraphUserCallback() {
                @Override
                public void onCompleted(GraphUser user, Response response) {
                    Log.i("FB", "onCompleted");
                    if (user != null) {
                        session.getAccessToken();
                        facebookID = user.getId();
                        accessToken = session.getAccessToken();
                        SharedPreferences.Editor editor = userData.edit();
                        editor.putString("accessToken", accessToken);
                        editor.putString("facebookid", facebookID);
                        editor.commit();
                        getFBImages(facebookID, accessToken);
                    }
                }
            }).executeAsync();
        } else if (state.isClosed()) {
            Log.e("FB", "Logged out...");
        }
    }

    public void getFBImages(String fbID, final String accessToken) {
        Session session = Session.getActiveSession();
        if (session.isClosed()) {
            session = Session.openActiveSessionFromCache(context);
        }
        System.out.println(fbID + "fbID");
        new Request(session, "/" + fbID + "/albums", null,
                HttpMethod.GET, new Request.Callback() {
            public void onCompleted(Response response) {
                try {
                    JSONObject jObj = response.getGraphObject().getInnerJSONObject();
                    if (progressDia != null && progressDia.isShowing())
                        progressDia.dismiss();
                    Intent next = new Intent(context, ShowAlbumFacebook.class);
                    next.putExtra("response", jObj.toString());
                    // next.putExtra("customLogin",customLogin);
                    next.putExtra("accessToken", accessToken);
                    Log.v("Response", "Albums=" + jObj.toString());
                    startActivityForResult(next, GET_REQUEST);
                    overridePendingTransition(0, R.anim.exit_slide_right);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).executeAsync();
    }

    private File createTemporaryFile(String part, String ext) throws Exception {
        File tempDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        if (!tempDir.exists()) {
            tempDir.mkdir();
        }
        return File.createTempFile(part, ext, tempDir);
    }

    private String getRealPathFromURI(Uri contentURI) {
        String result;
        Cursor cursor = getContentResolver().query(contentURI, null, null, null, null);
        if (cursor == null) {
            result = contentURI.getPath();
        } else {
            cursor.moveToFirst();
            int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            result = cursor.getString(idx);
            cursor.close();
        }
        return result;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        uiHelper.onActivityResult(requestCode, resultCode, data);
        Uri selectedImageUri = null;
        switch (requestCode) {
            case REQUEST_GALLERY:
                if (data == null || data.getData() == null)
                    return;
                selectedImageUri = data.getData();
                photo = new File(getRealPathFromURI(selectedImageUri));
                Intent intent = new Intent(context, CropperActivity.class);
                intent.putExtra("URI", photo);
                intent.putExtra("imagerotate", "Gallery");
                startActivityForResult(intent, REQUEST_CROPPER);
                overridePendingTransition(0, R.anim.exit_slide_right);
                break;
            case REQUEST_KITKAT_GALLERY:
                if (data == null || data.getData() == null)
                    return;
                selectedImageUri = data.getData();
                photo = new File(getRealPathFromURI(selectedImageUri));
                imagePaths.getString("imagePath", "");
                Intent intent1 = new Intent(context, CropperActivity.class);
                intent1.putExtra("URI", photo);
                intent1.putExtra("imagerotate", "Gallery");
                startActivityForResult(intent1, REQUEST_CROPPER);
                overridePendingTransition(0, R.anim.exit_slide_right);
                break;
            case REQUEST_CAMERA:
                try {
                    if (resultCode == RESULT_CANCELED) {
                        return;
                    }
                    Intent intent11 = new Intent(context, CropperActivity.class);
                    intent11.putExtra("URI", photo);
                    intent11.putExtra("imagerotate", "Camera");
                    startActivityForResult(intent11, REQUEST_CROPPER);
                    overridePendingTransition(0, R.anim.exit_slide_right);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case GET_REQUEST:
                if (resultCode == RESULT_CANCELED) {
                    return;
                } else {
                    setResult(RESULT_OK, data);
                    finish();
                    overridePendingTransition(0, R.anim.exit_slide_left);
                }
                break;
            case REQUEST_CROPPER:
                if (resultCode == RESULT_CANCELED) {
                    return;
                } else {
                    Log.v("AddPhotosActivity", "OnActivityResult");
                    setResult(RESULT_OK, data);
                    finish();
                    overridePendingTransition(0, R.anim.exit_slide_left);
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onBackPressed() {
        finish();
        overridePendingTransition(0, R.anim.exit_slide_left);
    }

    @Override
    public void onResume() {
        super.onResume();
        uiHelper.onResume();
        if (progressDia != null && progressDia.isShowing())
            progressDia.dismiss();
    }

    @Override
    public void onPause() {
        super.onPause();
        uiHelper.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        uiHelper.onDestroy();
    }
}