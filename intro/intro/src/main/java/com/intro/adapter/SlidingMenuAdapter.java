package com.intro.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.intro.android.R;
import com.intro.model.SideMenuModel;

/**
 * SlidingMenuAdapter this is adapter for is maintain side menu list.
 * By Ankit Kumar
 */

public class SlidingMenuAdapter extends BaseAdapter {

	LayoutInflater inflater;
	Context con;
	String[] data;
	int[] imageIcon;
	SideMenuModel model;
	int position;

	public SlidingMenuAdapter(Context con) {
		this.con = con;
		model = new SideMenuModel();
		data = model.getMenuItems();
		imageIcon = model.getSlidingImageItems();
		inflater = (LayoutInflater) con
				.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
	}

	@Override
	public int getCount() {
		return data.length;
	}

	@Override
	public Object getItem(int position) {
		return data[position];
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public boolean isEnabled(int position) {
		return model.getMenuItemEnabled(position);
	}

	@SuppressLint("NewApi")
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		if (convertView == null) {
			
			holder = new ViewHolder();
			convertView = inflater.inflate(R.layout.side_menu_row, parent,
					false);
			 
			holder.item = (TextView) convertView
					.findViewById(R.id.side_menu_row);
			holder.relative = (RelativeLayout) convertView
					.findViewById(R.id.relative);
			holder.slidingItemsImgView = (ImageView) convertView
					.findViewById(R.id.slidingItemsImgView);
			if (position == 4) {
			      convertView.setBackgroundColor(con.getResources().getColor(R.color.header_background));
			      holder.item.setTextColor(con.getResources().getColor(R.color.white));
			    }
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		holder.item.setText(data[position]);
		if (position < 2 || imageIcon.length - 1 == position) {
			holder.slidingItemsImgView.setVisibility(View.GONE);
		} else {
			holder.slidingItemsImgView.setVisibility(View.VISIBLE);
			holder.slidingItemsImgView.setImageResource(imageIcon[position]);
		}
		return convertView;
	}

	class ViewHolder {
		TextView item;
		RelativeLayout relative;
		ImageView slidingItemsImgView;
	}
}
