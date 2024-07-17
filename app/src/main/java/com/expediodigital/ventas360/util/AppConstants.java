package com.expediodigital.ventas360.util;

import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.regex.Pattern;


public interface AppConstants {
    /**
     * Application(Project) id on Google api(Developer) Console
     */
    //String GOOGLE_APP_ID = "4079867638";
    /**
     * Messages for user interaction
     */
    String SHOW_ERROR = "-1", HIDE_ERROR = "-1", SUCCESS_1 = "1", SUCCESS_0 = "0", SUCCESS_TRUE = "true", SUCCESS_UNKNOWN = "Whoops! Unknown sucess value";
    String WORK_IN_PROGRESS = "WORK IN PROGRESS";
    String UNEXPEXTED_ERROR = "Whoops! Something is happen unexpectedly. Please try again.";
    String UNEXPECTED_RESPONSE = "Whoops! Something is happen unexpectedly. Response is not in proper format.";
    String PARSING_ERROR = "Whoops! Something is happen unexpectedly. Exception in data parsing.";
    String EXCEPTION = "Whoops! Something is happen unexpectedly. Exception in data processing.";
    int DARK = 1;
    int LIGHT = 2;
    int PAGE_SIZE = 10;
    int paginationCount = 10;
    String[] arrSeats = {"Good", "Satisfactory", "Needs Cleaning", "Needs Replacement"};
    String[] arrMultiple = {"Good", "Satisfactory", "Needs Replacement"};
    String[] arrAc = {"Good", "Satisfactory", "Needs Repair"};
    String[] arrYesNo = {"Yes", "No"};


    /**
     * HTTP request method types
     */
    int POST_TYPE = 1, GET_TYPE = 2, PUT_TYPE = 3, DELETE_TYPE = 4;

    /*Time format for later ride*/


    String BLANK_DATE = "0000-00-00 00:00:00";

    String BLANK_SPACE = " ";


    String ENGLISH = "en";
    String SPANISH = "es";

    String BASE_URL = "http://208.117.85.123/TrackingPackage/index.php/ApiController/";
    String IMAGE_URL = "http://208.117.85.123/TrackingPackage/image/";
    String APP_TOKEN = "COURIERAPP-30062016";


//    String BASE_URL = "http://logistic.blindsandbeyond.be/admin/index.php/ApiController/";
//    String IMAGE_URL = "http://logistic.blindsandbeyond.be/admin/image/";

    String CUSTOMER = "1";
    String DRIVER = "2";

    String PN_EMAIL = "email";
    String PN_PASSWORD = "password";
    String PN_DEVICE_TYPE = "device_type";
    String PN_APP_TOKEN = "code";
    String PN_DEVICE_TOKEN = "device_token";


    //    public static final String emailPattern = "\"^([a-zA-Z0-9._-]+)@{1}(([a-zA-Z0-9_-]{1,67})|([a-zA-Z0-9-]+\\\\.[a-zA-Z0-9-]{1,67}))\\\\.(([a-zA-Z0-9]{2,6})(\\\\.[a-zA-Z0-9]{2,6})?)$\"";
    public static final String mobilePattern = "^[0-9]{8,14}$";
    String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";

    /**
     * Image Storage Path
     */
    String IMAGE_DIRECTORY = "/DCIM/PICTURES";
    String IMAGE_DIRECTORY_CROP = "/DCIM/CROP_PICTURES";
    String VIDEO_DIRECTORY = "/DCIM/VIDEOS";


    //    * Constant for Intent calling
//    */
    int ACTIVITY_RESULT = 1001, ACTIVITY_FINISH = 1002,
            GALLERY = 111, CAMERA = 112, CROP = 113;

    /**
     * Validation ragular expression
     */
    Pattern EMAIL_ADDRESS_PATTERN = Pattern
            .compile("^([a-zA-Z0-9._-]+)@{1}(([a-zA-Z0-9_-]{1,67})|([a-zA-Z0-9-]+\\.[a-zA-Z0-9-]{1,67}))\\.(([a-zA-Z0-9]{2,6})(\\.[a-zA-Z0-9]{2,6})?)$");
    Pattern MOBILE_NUMBER_PATTERN = Pattern.compile("^[0-9]{8,14}$");
    Pattern USER_NAME_PATTERN = Pattern.compile("^([a-zA-Z0-9._-]){6,20}$");
    Pattern PASSWORD_PATTERN = Pattern.compile("^(?=.*?[A-Z])(?=.*?[a-z])(?=.*?[0-9]).{8,}$");
    String PERCENT = "%";
    String FLAT = "flat";

    String DEVICE_TYPE = "android";
    String PN_LANGUAGE = "Lang";
    public static final int SECOND_MILLIS = 1000;
    public static final int MINUTE_MILLIS = 60 * SECOND_MILLIS;
    public static final int HOUR_MILLIS = 60 * MINUTE_MILLIS;
    public static final int DAY_MILLIS = 24 * HOUR_MILLIS;


    public enum Months {
        JAN, FEB, MAR, APR, MAY, JUN, JUL, AUG, SEP, OCT, NOV, DEC
    }


    /**
     * Variable For Condition Check
     */


    /**************************
     * SERVICE NAME START***************************************
     * /**
     * Methods for request on remote server
     */

    String PN_NAME = "name";
    String PN_VALUE = "value";
    String PN_AUTHORIZATION = "Authorization";

    /*For Merging*/
    int HOME = 0, DASHBOARD = 1, MY_ORDERS = 2, MY_ACCOUNT = 3, NOTIFICATION_MENU = 4, SETTINGS = 5, ADDRESS_BOOK = 7, REWARD = 8,
            APP_INFO = 9, SUPPORT_MENU = 10, FAQ_MENU = 11, SHARE_APP = 12, APP_SETTING = 13,
            TERMS_AND_CONDITIONS = 14, LOGOUT = 15, SHOPING_LIST = 16, ACCOUNT_DETAILS = 17,
            BY_CREDIT = 18, SYNCH_DATA = 19, SHOP_BY_CATEGORY = 20, MY_ORDERS_other = 21, LATEST_Product = 22;

    /**************************
     * SERVICE NAME START***************************************
     * /**
     * Methods for request on remote server
     */
    String SETTINGS_SERVICE_NAME_STORE = "store/settings";
    String DASHBOARD_SERVICE_NAME = "store/dashboard_list";
    String PROFILE_INFO = "store/profile_info";

    /* SimpleDateFormat */
    SimpleDateFormat YYYY_MM_DD_HH_MM_SS = new SimpleDateFormat(
            "yyyy-MM-dd hh:mm:ss", Locale.getDefault());

    SimpleDateFormat MMM_DD_YYYY_HH_MM_AM_PM = new SimpleDateFormat(
            "MMMdd yyyy,hh:mm a", Locale.getDefault());
    SimpleDateFormat DD_MMM_YYYY = new SimpleDateFormat(
            "dd MMM yyyy", Locale.getDefault());
}
