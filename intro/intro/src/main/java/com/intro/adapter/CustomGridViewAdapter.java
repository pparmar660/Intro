package com.intro.adapter;

import java.util.ArrayList;

import com.intro.android.R;
import com.intro.imageUtils.ImageLoader;
import com.intro.model.AlbumItem;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * CustomGridViewAdapter this is adapter for set facebook alumbs and images in grid view.
 * By Ankit Kumar
 */

public class CustomGridViewAdapter extends ArrayAdapter<AlbumItem> {
	private Context context;
	private int layoutResourceId;
	private ArrayList<AlbumItem> data = new ArrayList<AlbumItem>();
	private ImageLoader imgLoader;
	String accessToken = "";

	public CustomGridViewAdapter(Context context, int layoutResourceId,
			ArrayList<AlbumItem> data, String accessToken) {
		super(context, layoutResourceId, data);
		this.layoutResourceId = layoutResourceId;
		this.context = context;
		this.data = data;
		this.accessToken = accessToken;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View row = convertView;
		RecordHolder holder = null;

		if (row == null) {
			LayoutInflater inflater = ((Activity) context).getLayoutInflater();
			row = inflater.inflate(layoutResourceId, parent, false);

			holder = new RecordHolder();
			holder.txtTitle = (TextView) row.findViewById(R.id.item_text);
			holder.imageItem = (ImageView) row.findViewById(R.id.item_image);
			row.setTag(holder);
		} else {
			holder = (RecordHolder) row.getTag();
		}

		AlbumItem item = data.get(position);

		holder.txtTitle.setText(item.getName());
		imgLoader = new ImageLoader(context);

		if (item.getId().equals("INTRO")) {
			imgLoader.DisplayImage(item.getLink(), holder.imageItem);
		} else {
			System.out.println("item.getLink()" + "https://graph.facebook.com/"
					+ item.getId() + "/picture?access_token=" + accessToken);
			imgLoader.DisplayImage("https://graph.facebook.com/" + item.getId()
					+ "/picture?access_token=" + accessToken, holder.imageItem);
		}
		return row;
	}

	static class RecordHolder {
		TextView txtTitle;
		ImageView imageItem;
	}
}