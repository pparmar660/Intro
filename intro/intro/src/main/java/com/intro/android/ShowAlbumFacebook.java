package com.intro.android;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

import com.appsee.Appsee;
import com.facebook.HttpMethod;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.intro.adapter.CustomGridViewAdapter;
import com.intro.model.AlbumItem;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.TextView;

/**
 * This class is for the show facebook albums with existing login
 *
 * By Ankit Kumar
 */

public class ShowAlbumFacebook extends Activity {

	private GridView gridView;
	private String response;
	private ImageButton back_arrow;
    private TextView noImages;
	private CustomGridViewAdapter customGridAdapter;
	private ArrayList<AlbumItem> gridArray;
	private SharedPreferences mPrefs;
	private String PREFS = "MyPrefs";
	private String facebookID = "", accessToken = "";
	private Context context;
    private boolean customLogin,singleClick = true;
	private static final int GET_REQUEST = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		context = this;
		setContentView(R.layout.activity_showalbum);
        Appsee.start(getString(R.string.appsee_apikey));
		gridView = (GridView) findViewById(R.id.gridView1);
		back_arrow = (ImageButton) findViewById(R.id.back_arrow);
        noImages = (TextView) findViewById(R.id.noImages);
		gridArray = new ArrayList<AlbumItem>();
        try {
            accessToken = getIntent().getStringExtra("accessToken");
            response = getIntent().getStringExtra("response");
			JSONObject json = new JSONObject(response);
			JSONArray jarray = json.getJSONArray("data");
            if(jarray == null || jarray.length()==0){
                noImages.setVisibility(View.VISIBLE);
            }else {
                noImages.setVisibility(View.GONE);
			for (int i = 0; i < jarray.length(); i++) {
                JSONObject oneAlbum = jarray.getJSONObject(i);
                AlbumItem item = new AlbumItem(oneAlbum.getString("id"),
                        oneAlbum.getString("name"), oneAlbum.getString("link"));
                gridArray.add(item);
            }
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		customGridAdapter = new CustomGridViewAdapter(this, R.layout.row_grid,gridArray,accessToken);
		gridView.setAdapter(customGridAdapter);
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
//                if(customLogin) {
//                Session session = Session.getActiveSession();
//                if (session != null) {
//                    if (!session.isClosed()) {
//                        Session.getActiveSession().closeAndClearTokenInformation();
//                    }
//                }
//                Session.setActiveSession(null);
//                }
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
                    System.out.println("accessToken1 : "+accessToken);
                    Session session = Session.getActiveSession();
                    if (!session.isOpened()) {
                        session = Session.openActiveSessionFromCache(context);
                    }
                    new Request(session, "/"+ ((AlbumItem) gridView.getAdapter().getItem(position))
                            .getId() + "/photos", null, HttpMethod.GET,new Request.Callback() {
                                public void onCompleted(Response response) {
                                    try {
                                        JSONObject jObj = response.getGraphObject().getInnerJSONObject();
                                        Intent next = new Intent(context,ShowPhotosFacebook.class);
                                        next.putExtra("response", jObj.toString());
                                       // next.putExtra("customLogin", customLogin);
                                        next.putExtra("accessToken",accessToken);
                                        Log.v("Response", "Photos=" + jObj.toString());
                                        startActivityForResult(next, GET_REQUEST);
                                        overridePendingTransition(0, R.anim.exit_slide_right);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            }).executeAsync();
                }
			}
		});
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
		case GET_REQUEST:
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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
//        if(customLogin) {
//            Session session = Session.getActiveSession();
//            if (session != null) {
//                if (!session.isClosed()) {
//                    Session.getActiveSession().closeAndClearTokenInformation();
//                }
//            }
//            Session.setActiveSession(null);
//        }
        finish();
        overridePendingTransition(0, R.anim.exit_slide_left);
    }
}
