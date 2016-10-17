package com.intro.utils;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

/**
 * Created by vinove on 20/7/16.
 */
public class PermissionGranted {


    public static boolean verifyPermissions(int[] grantResults) {
        // At least one result must be checked.
        if (grantResults.length < 1) {
            return false;
        }

        // Verify that each required permission has been granted, otherwise return false.
        for (int result : grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    //check permission for contacts
    public static boolean checkPermissionContacts(Activity activity){
        int result = ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_CONTACTS);
        int result1=ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_CONTACTS);

        if (result == PackageManager.PERMISSION_GRANTED && result1==PackageManager.PERMISSION_GRANTED){

            return true;

        } else {

            return false;

        }
    }


    //check permission for camera
    public static boolean checkPermissionCamera(Activity activity){
        int result = ContextCompat.checkSelfPermission(activity, Manifest.permission.CAMERA);

        if (result == PackageManager.PERMISSION_GRANTED){

            return true;

        } else {

            return false;

        }
    }


    //check permission for location
    public static boolean checkPermissionLocation(Activity activity){
        int result = ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION);
        int result1 = ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION);

        if (result == PackageManager.PERMISSION_GRANTED && result1==PackageManager.PERMISSION_GRANTED){

            return true;

        } else {

            return false;

        }
    }


    //check permission for phone
    public static boolean checkPermissionPhone(Activity activity){
        int result = ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_PHONE_STATE);
        int result1 = ContextCompat.checkSelfPermission(activity, Manifest.permission.CALL_PHONE);

        if (result == PackageManager.PERMISSION_GRANTED && result1==PackageManager.PERMISSION_GRANTED){

            return true;

        } else {

            return false;

        }
    }

    //check permission for Storage
    public static boolean checkPermissionStorage(Activity activity){
        int result = ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE);
        int result1 = ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (result == PackageManager.PERMISSION_GRANTED && result1==PackageManager.PERMISSION_GRANTED){

            return true;

        } else {

            return false;

        }
    }

    //check permission for SMS
    public static boolean checkPermissionSMS(Activity activity){
        int result = ContextCompat.checkSelfPermission(activity, Manifest.permission.SEND_SMS);
        int result1 = ContextCompat.checkSelfPermission(activity, Manifest.permission.RECEIVE_SMS);
        int result2 = ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_SMS);

        if (result == PackageManager.PERMISSION_GRANTED && result1==PackageManager.PERMISSION_GRANTED && result2==PackageManager.PERMISSION_GRANTED){

            return true;

        } else {

            return false;

        }
    }

    //Contacts permissions
    public static boolean requestContactsPermissions(Activity activity) {

        boolean requestPermission;

        if (ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.READ_CONTACTS)
                || ActivityCompat.shouldShowRequestPermissionRationale(activity,
                Manifest.permission.WRITE_CONTACTS)) {

            requestPermission = true;
        } else {
            // Contact permissions have not been granted yet. Request them directly.

            requestPermission = true;
        }
        return requestPermission;
    }

    //camera permissions
    public static boolean requestCameraPermissions(Activity activity) {

        boolean requestPermission;

        if (ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.CAMERA)) {
            requestPermission = true;
        } else {

            requestPermission = true;
        }
        return requestPermission;
    }

    //locations permissions
    public static boolean requestLocationPermissions(Activity activity) {

        boolean requestPermission;

        if (ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.ACCESS_FINE_LOCATION)
                || ActivityCompat.shouldShowRequestPermissionRationale(activity,
                Manifest.permission.ACCESS_COARSE_LOCATION)) {

            requestPermission = true;
        } else {

            requestPermission = true;
        }
        return requestPermission;
    }


    //Storage permissions
    public static boolean requestStoragePermissions(Activity activity) {

        boolean requestPermission;

        if (ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.READ_EXTERNAL_STORAGE)
                || ActivityCompat.shouldShowRequestPermissionRationale(activity,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

            requestPermission = true;
        } else {

            requestPermission = true;
        }
        return requestPermission;
    }


    //SMS permissions
    public static boolean requestSMSPermissions(Activity activity) {

        boolean requestPermission;

        if (ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.SEND_SMS)
                || ActivityCompat.shouldShowRequestPermissionRationale(activity,
                Manifest.permission.RECEIVE_SMS) || ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.READ_SMS)) {

            requestPermission = true;
        } else {

            requestPermission = true;
        }
        return requestPermission;
    }

    //Calender permissions
    public static boolean requestCalenderPermissions(Activity activity) {

        boolean requestPermission;

        if (ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.READ_CALENDAR)
                || ActivityCompat.shouldShowRequestPermissionRationale(activity,
                Manifest.permission.WRITE_CALENDAR)) {

            requestPermission = true;
        } else {

            requestPermission = true;
        }
        return requestPermission;
    }

    //Phone Call permissions
    public static boolean requestPhonePermissions(Activity activity) {

        boolean requestPermission;

        if (ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.READ_PHONE_STATE)
                || ActivityCompat.shouldShowRequestPermissionRationale(activity,
                Manifest.permission.CALL_PHONE)) {

            requestPermission = true;
        } else {

            requestPermission = true;
        }
        return requestPermission;
    }
}
