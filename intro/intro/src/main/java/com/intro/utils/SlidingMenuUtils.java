package com.intro.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.intro.android.GetReadyActivity;
import com.intro.android.ProfileActivity;
import com.intro.android.QueueUserActivity;
import com.intro.android.R;
import com.intro.android.SetProfileActivity;
import com.intro.android.SettingsActivity;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;

public final class SlidingMenuUtils {
	public static SharedPreferences userData;
	
	public static void WhereToGo(final Context context, int arg2) {
		if (arg2 == 3) {
			Intent inteni = new Intent(context, ProfileActivity.class);
			inteni.putExtra("showMatchesMenu",true);
			context.startActivity(inteni);
			((Activity) context).overridePendingTransition(0,
					R.anim.exit_slide_right);
		//	slidingMenuRight.toggle();
		} else if (arg2 == 4) {
			Intent inteni = new Intent(context, ProfileActivity.class);
			context.startActivity(inteni);
			((Activity) context).overridePendingTransition(0,
					R.anim.exit_slide_right);
		} else if (arg2 == 5) {
            userData = context.getSharedPreferences("UserData", 0);
			if (!userData.getString("expires2", "").equalsIgnoreCase("")
					&& !userData.getString("expires2", "").equalsIgnoreCase("null")
					&& !userData.getString("expires2", "").contains("-")) {
				Intent inteni = new Intent(context, QueueUserActivity.class);
				context.startActivity(inteni);
				((Activity) context).overridePendingTransition(0, R.anim.exit_slide_right);
			} else {
				Intent inteni = new Intent(context, GetReadyActivity.class);
				context.startActivity(inteni);
				((Activity) context).overridePendingTransition(0, R.anim.exit_slide_right);
			}
			
		} else if (arg2 == 6) {
			Intent inteni = new Intent(context, SettingsActivity.class);
			context.startActivity(inteni);
			((Activity) context).overridePendingTransition(0,
					R.anim.exit_slide_right);
		} else if (arg2 == 7) {
			try {
				// Share my app
				Intent i = new Intent(Intent.ACTION_SEND);
				i.setType("text/plain");
				i.putExtra(Intent.EXTRA_SUBJECT, GlobalVariables.title);
				String sAux = "\nLet me recommend you this application\n\n";
				sAux = sAux + "https://play.google.com/store/apps/details?id="
						+ context.getPackageName() + "\n\n";
				i.putExtra(Intent.EXTRA_TEXT, sAux);
				context.startActivity(Intent.createChooser(i, "choose one"));
			} catch (Exception e) { // e.toString();
			}
		}

	}

}
