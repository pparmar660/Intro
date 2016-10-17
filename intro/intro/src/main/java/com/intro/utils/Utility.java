package com.intro.utils;

import java.io.File;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Pattern;

import com.intro.android.R;
import com.intro.customviews.CustomAlartDialog;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.Signature;
import android.graphics.Color;
import android.os.Build;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.util.Base64;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * This class contains the utility methods that are common to use and there name
 * would give an idea about the working of methods.
 * 
 * By Ankit Kumar
 */
public final class Utility {

	public static int height = 0;
	public static int getAge(Date dateOfBirth) {

	    Calendar today = Calendar.getInstance();
	    Calendar birthDate = Calendar.getInstance();

	    int age = 0;

	    birthDate.setTime(dateOfBirth);
	    if (birthDate.after(today)) {
	        throw new IllegalArgumentException("Can't be born in the future");
	    }

	    age = today.get(Calendar.YEAR) - birthDate.get(Calendar.YEAR);

	    // If birth date is greater than todays date (after 2 days adjustment of leap year) then decrement age one year   
	    if ( (birthDate.get(Calendar.DAY_OF_YEAR) - today.get(Calendar.DAY_OF_YEAR) > 3) ||
	            (birthDate.get(Calendar.MONTH) > today.get(Calendar.MONTH ))){
	        age--;

	     // If birth date and todays date are of same month and birth day of month is greater than todays day of month then decrement age
	    }else if ((birthDate.get(Calendar.MONTH) == today.get(Calendar.MONTH )) &&
	              (birthDate.get(Calendar.DAY_OF_MONTH) > today.get(Calendar.DAY_OF_MONTH ))){
	        age--;
	    }

	    return age;
	}
	public static void getListViewSize(ListView myListView) {
		try {
			ListAdapter myListAdapter = myListView.getAdapter();
			if (myListAdapter == null) {
				// do nothing return null
				return;
			}
			// set listAdapter in loop for getting final size
			int totalHeight = 0;

			for (int size = 0; size < myListAdapter.getCount(); size++) {
				View listItem = myListAdapter.getView(size, null, myListView);
				if (listItem instanceof ViewGroup) {
					listItem.setLayoutParams(new LinearLayout.LayoutParams(
							LinearLayout.LayoutParams.WRAP_CONTENT,
							LinearLayout.LayoutParams.WRAP_CONTENT));
				}
				listItem.measure(0, 0);
				totalHeight += listItem.getMeasuredHeight();
				height = listItem.getMeasuredHeight();
				Log.e("height 3", "" + listItem.getMeasuredHeight());
			}
			// setting listview item in adapter
			ViewGroup.LayoutParams params = myListView.getLayoutParams();
			params.height = (int) ((totalHeight) * (1) + (myListView
					.getDividerHeight() * (myListAdapter.getCount()))) + 10;
			myListView.setLayoutParams(params);
			// print height of adapter on log
			Log.e("3 of listItem:", String.valueOf(totalHeight));

			// DashBoard1.taskitem.getLayoutParams().height=totalListHeight;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void getListViewSize2(ListView myListView) {
		try {
			ListAdapter myListAdapter = myListView.getAdapter();
			if (myListAdapter == null) {
				// do nothing return null
				return;
			}
			// set listAdapter in loop for getting final size
			int totalHeight = 0;
			for (int size = 0; size < myListAdapter.getCount(); size++) {
				View listItem = myListAdapter.getView(size, null, myListView);
				if (listItem instanceof ViewGroup) {
					listItem.setLayoutParams(new LinearLayout.LayoutParams(
							LinearLayout.LayoutParams.WRAP_CONTENT,
							LinearLayout.LayoutParams.WRAP_CONTENT));
				}
				listItem.measure(0, 0);
				totalHeight += listItem.getMeasuredHeight();
				height = listItem.getMeasuredHeight();
				Log.e("height 3", "" + listItem.getMeasuredHeight());
			}
			// setting listview item in adapter
			ViewGroup.LayoutParams params = myListView.getLayoutParams();
			params.height = (int) ((totalHeight) * (1) + (myListView
					.getDividerHeight() * (myListAdapter.getCount())));
			myListView.setLayoutParams(params);
			// print height of adapter on log
			Log.e("3 of listItem:", String.valueOf(totalHeight));

			// DashBoard1.taskitem.getLayoutParams().height=totalListHeight;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void showSoftKey(Context context, EditText et_location) {
		try {
			InputMethodManager imm = (InputMethodManager) context
					.getApplicationContext().getSystemService(
							Context.INPUT_METHOD_SERVICE);

			et_location.requestFocus();
			imm.showSoftInput(et_location, 0);
		} catch (Exception ex) {

			ex.printStackTrace();
		}
	}

	public static void hideSoftKey(Context context) {
		try {
			InputMethodManager inputMethodManager = (InputMethodManager) context
					.getSystemService(Context.INPUT_METHOD_SERVICE);
			inputMethodManager.hideSoftInputFromWindow(((Activity) context)
					.getCurrentFocus().getWindowToken(), 0);
		} catch (Exception ex) {

			ex.printStackTrace();
		}
	}

	public final static Pattern EMAIL_ADDRESS_PATTERN = Pattern
			.compile("^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
		+ "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$");
					//"[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+"

	public static boolean checkEmail(String email) {
		return EMAIL_ADDRESS_PATTERN.matcher(email).matches();
	}

	public static void validationByToastMessage(Context context,
			String validation_message) {
		try {
			Toast toast = Toast.makeText(context, validation_message,
					Toast.LENGTH_SHORT);
			toast.setGravity(Gravity.CENTER, 0, 0);
			toast.getView().setBackgroundColor(Color.BLACK);
			toast.show();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return;
	}

	public static boolean checkName(String about) {
		// UserName Validation Pattern String
		final Pattern USER_NAME_PATTERN = Pattern.compile("^[a-zA-Z ]+$");
		if (USER_NAME_PATTERN.matcher(about).matches()) {
			return true;
		}
		return false;
	}

	public final static Pattern ZIP_ADDRESS_PATTERN = Pattern
			.compile("\\d{5}(-\\d{4})?");

	public static boolean checkZip(String zip) {
		return ZIP_ADDRESS_PATTERN.matcher(zip).matches();
	}

	public final static Pattern MOBILE_PATTERN = Pattern
			.compile("\\d{3}-\\d{3}-\\d{4}");

	public static boolean checkMobile(String zip) {
		return MOBILE_PATTERN.matcher(zip).matches();
	}

	public static void keyHash(Context mContext) {
		try {
			PackageInfo info = mContext.getPackageManager().getPackageInfo(
					mContext.getPackageName(), PackageManager.GET_SIGNATURES);
			for (Signature signature : info.signatures) {
				MessageDigest md = MessageDigest.getInstance("SHA");
				md.update(signature.toByteArray());
				// Toast.makeText(mContext,Base64.encodeToString(md.digest(),
				// Base64.DEFAULT) , 50000).show();
				// Log.d("KeyHash:", Base64.encodeToString(md.digest(),
				// Base64.DEFAULT));
				System.out.println("KeyHash:"
						+ Base64.encodeToString(md.digest(), Base64.DEFAULT));
			}
		} catch (NameNotFoundException e) {

		} catch (NoSuchAlgorithmException e) {

		}
	}

	public static String getSavePath() {
		File mediaStorageDir = new File(
				Environment
						.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
				GlobalVariables.IMAGE_DIRECTORY_NAME);
		if (!mediaStorageDir.exists()) {
			if (!mediaStorageDir.mkdirs()) {
				return null;
			}
		}
		return mediaStorageDir.getAbsolutePath();
	}

	public static File getOutputMediaFile() {
		String directory = getSavePath();
		// Create a media file name
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss",
				Locale.ENGLISH).format(new Date());
		File mediaFile = new File(directory + File.separator + "IMG_"
				+ timeStamp + ".jpg");
		return mediaFile;
	}
	
	public static String decimalFormatUptoTwoDigits(double value){
    	DecimalFormat format = new DecimalFormat("#0.00");
    	return format.format(value);
    }

    public static boolean hasPermissions(Context context, String... permissions) {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

}