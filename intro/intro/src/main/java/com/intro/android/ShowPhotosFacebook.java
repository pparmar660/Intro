package com.intro.android;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.TextView;

import com.appsee.Appsee;
import com.intro.adapter.CustomGridViewAdapter;
import com.intro.model.AlbumItem;

/**
 * This class is for show facebook images according to albums with existing login
 *
 * By Ankit Kumar
 */

public class ShowPhotosFacebook extends Activity {

    private GridView gridView;
    private String response;
    private ImageButton back_arrow;
    private TextView noImages;
    private CustomGridViewAdapter customGridAdapter;
    private ArrayList<AlbumItem> gridArray;
    private SharedPreferences mPrefs;
    private String PREFS = "MyPrefs";
    static final int REQUEST_CROPPER = 0;
    private Context context;
    public String image, timeStamp, fileName, accessToken;
    private boolean customLogin, singleClick = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        context = this;
        setContentView(R.layout.activity_showalbum);
        Appsee.start(getString(R.string.appsee_apikey));
        gridView = (GridView) findViewById(R.id.gridView1);
        back_arrow = (ImageButton) findViewById(R.id.back_arrow);
        noImages = (TextView) findViewById(R.id.noImages);
        gridArray = new ArrayList<AlbumItem>();
        TextView text = (TextView) findViewById(R.id.header_title);
        text.setText("Choose an image");
        try {
            accessToken = getIntent().getStringExtra("accessToken");
            response = getIntent().getStringExtra("response");
            JSONObject json = new JSONObject(response);
            JSONArray jdataarray = json.getJSONArray("data");
            System.out.println("data" + jdataarray.toString());
            if(jdataarray == null || jdataarray.length()==0){
                noImages.setVisibility(View.VISIBLE);
            }else {
                noImages.setVisibility(View.GONE);
                for (int i = 0; i < jdataarray.length(); i++) {
                    image = jdataarray.getJSONObject(i).getString("source");
                    AlbumItem Item = new AlbumItem("INTRO", "", image);
                    gridArray.add(Item);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        customGridAdapter = new CustomGridViewAdapter(this, R.layout.row_grid,gridArray,accessToken);
        gridView.setAdapter(customGridAdapter);
        mPrefs = getSharedPreferences("imagePaths", MODE_PRIVATE);
        addlistener();
    }

    @Override
    protected void onResume() {
        super.onResume();
        singleClick = true;
    }

    private void addlistener() {
        back_arrow.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                finish();
                overridePendingTransition(0, R.anim.exit_slide_left);
            }
        });
        gridView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                if(singleClick) {
                    singleClick = false;
                    AlbumItem item = null;
                    String url;
                    url = ((AlbumItem) gridView.getAdapter().getItem(position)).getLink();
                    new DownloadImage().execute(url);
                }
            }
        });
    }

    public class DownloadImage extends AsyncTask<String, Void, Uri> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.ENGLISH).format(new Date());
            fileName = Environment.getExternalStorageDirectory().getAbsolutePath()
                    + File.separator+ "Pictures"+ File.separator+ "IntroFBImages"
                    + File.separator+ "FbImage" + timeStamp + ".jpg";
        }

        @Override
        protected Uri doInBackground(String... URL) {
            int count;
            try {
                URL url = new URL((String) URL[0]);
                URLConnection conexion = url.openConnection();
                conexion.connect();
                String targetFileName = "FbImage" + timeStamp + ".jpg";// Change name andsubname
                int lenghtOfFile = conexion.getContentLength();
                String PATH = Environment.getExternalStorageDirectory()+ "/Pictures/IntroFBImages/";
                File folder = new File(PATH);
                if (!folder.exists()) {
                    folder.mkdir();// If there is no folder it will be created.
                }
                InputStream input = new BufferedInputStream(url.openStream());
                OutputStream output = new FileOutputStream(PATH+ targetFileName);
                byte data[] = new byte[1024];
                long total = 0;
                while ((count = input.read(data)) != -1) {
                    total += count;
                    output.write(data, 0, count);
                }
                output.flush();
                output.close();
                input.close();
            } catch (Exception e) {}
            return null;
        }

        @Override
        protected void onPostExecute(Uri result) {
            Intent intent1 = new Intent(context, CropperActivity.class);
            intent1.putExtra("URI", new File(fileName));
            intent1.putExtra("imagerotate", "Facebook");
          //  intent1.putExtra("customLogin", customLogin);
            startActivityForResult(intent1, REQUEST_CROPPER);
            overridePendingTransition(0, R.anim.exit_slide_right);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_CROPPER:
                if (resultCode == RESULT_CANCELED) {
                    return;
                } else {
                    setResult(RESULT_OK, data);
                    finish();
                    overridePendingTransition(0, R.anim.exit_slide_left);
                }
                break;
            default:
                break;
        }
    }

}