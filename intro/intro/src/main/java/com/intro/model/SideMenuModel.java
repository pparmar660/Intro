package com.intro.model;

import com.intro.android.R;

public class SideMenuModel {

	private String[] menuItemsArray = new String[] { "", "", "MATCHES",
			"PROFILE", "GO !NTRO", "SETTINGS", "INVITE", "" };

	private int[] slidingMenuIcon = new int[] { R.drawable.ic_launcher,
			R.drawable.ic_launcher, R.drawable.new_chat,
			R.drawable.new_profile, R.drawable.new_play,
			R.drawable.new_setting, R.drawable.new_invite,
			R.drawable.ic_launcher };

	private boolean[] itemsEnable = new boolean[] { false, false, true, true,
			true, true, true, false };
	
	public String[] getMenuItems() {
		return menuItemsArray;
	}

	public int[] getSlidingImageItems() {
		return slidingMenuIcon;
	}

	public boolean getMenuItemEnabled(int position) {
		return itemsEnable[position];
	}

}
