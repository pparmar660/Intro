package com.intro.utils;

import org.json.JSONObject;

import java.util.ArrayList;

/**
 * This class is used to list variables and constants which are required to show
 * messages and other text.
 * <p/>
 * By Ankit Kumar
 */
public final class GlobalVariables {

    public static enum SERVICE_TAG {
        LOGIN, REGISTER
    }

    public static final String IMAGE_DIRECTORY_NAME = "!ntro Album";
    public static Boolean finish = false;
    public static final String key = "RjJdQlChIxfKFDhzWuGnqxnJkDcPEv44";
    public static final String ServerUrl = "https://introapp.azure-mobile.net/";
    public static final String FirebaseUrl = "https://introapp.firebaseio.com/chat/";
    public static final String SENDER_ID = "1069191468093";
    public static final String HubName = "introapphub";
    public static final String HubListenConnectionString = "Endpoint=sb://introapphub-ns.servicebus.windows.net/;SharedAccessKeyName=DefaultListenSharedAccessSignature;SharedAccessKey=zSMHhi0kju/lhiyzrAV1cNrqmnCfqwV62ZycZ8wluTk=";
    //"https://introapp.firebaseIO.com/";


    public static final String Image_URL = "https://introapp.blob.core.windows.net/introappimages/";

    public static String title = "!ntro";
    public static String loading = "Loading...";
    public static String login = "login";
    public static String signup = "signup";

    public static String dropped_breacrumb = "Dropped Breadcrumb";
    public static String breadcrumb_popup = "Your profile will now be visible near this location!";
    public static String oh_snap = "Oh Snap!";
    public static String intro = "!ntro";
    public static String delete_user = "Delete user?";
    public static String event_expired = "Your event  has expired";
    public static String user_already_exist = "Username already exists";
    public static String event_timer_alart = "Hey, uhh... your event is going to expire in 5 minutes! Tap the 'Add Time' button to add 30 minutes to your event!";
    public static String no_account_found = "No account found";
    public static String invalid_login = "It looks like you entered an incorrect username or password.";
    /*public static String set_profile_error = "Please complete your profile - upload (or take) 3 pics and fill out your Name, Age and Gender.";*/
    public static String set_profile_error = "Oh Snap! It looks like you haven't finished filling out your profile! All you need is your first name, age, and gender to continue.";
    /*public static String to_contact = "ryan@introapp.net";*/
    public static String to_contact = "support@go-intro.com";
    public static String to_body = "Ryan, let me tell you about how I feel about !ntro";
    public static String network_error = "Network Not Found";
    public static String request_not_done = "Connection to server failed.";
    public static String user_mane_error = "Please enter your email address.";
    public static String password_error = "Please enter your password.";
    public static String password_count_error = "Invalid password (least 7 chars required)";
    public static String ValidEmail = " Please enter a valid  email address.";
    public static String pwd_not_equal = "Passwords do not match.";
    public static String repassBlank = "Please re-enter password.";
    public static String age_error = "Age must be greater than 18";
    public static String Pause = "Pause";
    public static String Resume = "Resume";
    public static String queueActivity = "queueActivity";
    public static String chatActivity = "chatActivity";
    public static String ChatResume = "ChatResume";
    public static String ChatPause = "Chat Pause";
    public static String ChatId = "Chat Id";
    public static String blockUser = "Are you sure you want to block ";
    public static String only3words = "Only 3 words allowed";
    public static String twitterLogin = "Twitter";
    public static String settingActivity = "Setting Activity";
    public static String comeFrom = "Come From";
    public static boolean profileView = false;
    public static boolean customLogin = false;
    public static String reportMsg = "Is this profile inappropriate?";
    public static String inappropriateMsg = "This profile has been reported and we will take action. Thank you for helping to keep our content appropriate for all users!";
    public static String matchAdapter = "match adapter";
    public static String breadcrumbAdapter = "breadcrumb adapter";
    public static String favAdapter = "fav adapter";

    public static String first_time_profile = "first time profile";
    public static String first_time_profile_both_breadcrumb = "first time profile both breadcrumb";
    public static String first_time_chat = "first time chat";
    public static String first_time_match = "first time match";

    public static ArrayList<String> randomImagesList = new ArrayList<String>();
    public static ArrayList<JSONObject> bothArrayList;
}
