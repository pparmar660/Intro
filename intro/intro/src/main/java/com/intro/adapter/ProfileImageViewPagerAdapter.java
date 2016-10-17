package com.intro.adapter;

import java.util.ArrayList;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.AnimationDrawable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.intro.android.R;
import com.intro.customviews.CustomDialog;
import com.intro.imageUtils.ImageLoader;
import com.intro.model.SideMenuModel;
import com.intro.utils.GlobalVariables;
import com.intro.webservice.BaseActivity;

/**
 * ProfileImageViewPagerAdapter this is adapter for set images im viewpager on Profile Screen.
 * By Ankit Kumar
 */

public class ProfileImageViewPagerAdapter extends PagerAdapter {
    // Declare Variables
    Context context;
    LayoutInflater inflater;
    ArrayList<String> imageURL = new ArrayList<String>();


    public ProfileImageViewPagerAdapter(Context context, ArrayList<String> imageURL) {
        this.context = context;
        this.imageURL = imageURL;
    }

    @Override
    public int getCount() {
        return imageURL.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == ((RelativeLayout) object);
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        ImageView imgflag, loader;
        inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View itemView = inflater.inflate(R.layout.viewpager_item, container,
                false);

        imgflag = (ImageView) itemView.findViewById(R.id.profileImageView);
        loader = (ImageView) itemView.findViewById(R.id.profileImageLoader);

        AnimationDrawable spinner = (AnimationDrawable) loader
                .getBackground();
        spinner.start();
        ImageLoader imgL = new ImageLoader(context);
        imgL.DisplayImage(GlobalVariables.Image_URL + imageURL.get(position), imgflag, loader);

        ((ViewPager) container).addView(itemView);

        return itemView;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        ((ViewPager) container).removeView((RelativeLayout) object);
    }

}
