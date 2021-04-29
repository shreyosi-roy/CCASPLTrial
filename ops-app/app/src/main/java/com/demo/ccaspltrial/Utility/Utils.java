package com.demo.ccaspltrial.Utility;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import com.demo.ccaspltrial.HomePage;

import java.util.ArrayList;

public class Utils {

    //webservice base URL
    public static final String baseURL="http://payroll.ccaspl.in/demo/operation_app_ccaspl/";

    //sharedpreferences variables----login details
    public static SharedPreferences loginPreferences;
    public static SharedPreferences.Editor loginEditor;
    public static final String loginPref="LoginPreferences";

    //sharedpreferences variables----page details
    public static SharedPreferences pagePreferences;
    public static SharedPreferences.Editor pageEditor;
    public static final String pagePref="PagePreferences";

    //sharedpreferences variables----other app details
    public static SharedPreferences appPreferences;
    public static SharedPreferences.Editor appEditor;
    public static final String appPref="ApplicationPreferences";

    //flags for image type selected
    public static int selfieFlag=0;
    public static int siteFlag=0;
    public static int challanFlag=0;
    public static int attendanceFlag=0;

    //permission request code
    public static final int REQUEST_PERMISSIONS=1;

    //intent request codes
    public static final int REQUEST_SELFIE_PHOTO=1;
    public static final int REQUEST_SITE_PHOTO=2;
    public static final int REQUEST_CHALLAN_PHOTO=3;
    public static final int REQUEST_ATTENDANCE_PHOTO=4;
    public static final int REQUEST_EXEPROFILE_PHOTO=5;

    //webservice api urls
//    public static final String loginURL=baseURL + "login.php?"; //[OLD]regular employee login api url
    public static final String loginURL=baseURL + "login_new.php?"; //[NEW]regular employee login api url
//    public static final String relieverloginURL=baseURL+"relieverlogin.php?"; //[OLD]reliever login api url
    public static final String relieverloginURL=baseURL+"relieverlogin_new.php?"; //[NEW]reliever login api url
    public static final String executiveloginURL =baseURL + "executivelogin.php?"; //executive login api url
    public static final String relieversiteURL=baseURL + "relieversite.php?"; //retrieve reliever site api url
    public static final String tasklistviewURL=baseURL + "task.php?"; //tasklist viewing api url
    public static final String imageUploadURL=baseURL + "imageupload.php?"; //image upload api url
    public static final String materiallistviewURL=baseURL + "materialbalance.php?"; //materiallist viewing api url
    public static final String imagelistviewURL=baseURL + "imageview.php?"; //image viewing api
    public static final String materialbalanceupdateURL=baseURL + "materialupdate.php?"; //material balance updating api
    public static final String materialrequiredURL=baseURL + "materialrequested.php?";  //material required update api
    public static final String certificationviewURL=baseURL + "certification.php?"; //certifications viewing api
    public static final String tasksubmissionURL=baseURL + "tasksubmission.php?";  //task submission api
    public static final String exeemployeeretrievalURL =baseURL + "executive_employee.php?"; //api to retrieve employees under an executive
    public static final String exesiteretrievalURL =baseURL + "executive_site.php?"; //api to retrieve sites under an executive
    public static final String relieverassignURL=baseURL + "assign_reliever.php?"; //api to assign reliever to selected site for selected date
    public static final String employeesofsiteURL=baseURL + "employee_site.php?"; //api to retrieve employees of a site
    public static final String exetaskdonelistURL =baseURL + "taskdone.php?"; //tasks done list api
    public static final String latestsiteimageURL=baseURL + "latest_site_image.php?"; //api to retrieve latest site image
    public static final String latestselfieimageURL=baseURL + "latest_selfie_image.php?"; //api to retrieve latest selfie image
    public static final String latestattendanceimageURL=baseURL + "latest_attendance_image.php?"; //api to retrieve latest attendance image
    public static final String latestdeliverychallanimageURL=baseURL + "latest_delivery_challan.php?"; //api to retrieve latest delivery challan image
    public static final String viewtraininglistURL=baseURL + "uncertified_traning.php?"; //api to retrieve list of other trainings
    public static final String retrievevideoURL=baseURL + "training_video.php?"; //api to retrieve videos
    public static final String updatepassedtrainingURL=baseURL + "training_status.php?"; //api to update status of requested trainings passed
    public static final String trainingquestionsURL=baseURL + "question.php?"; //api to retrieve training questions, if any
    public static final String retrievevideo2URL=baseURL + "requested_videos.php?"; //api to retrieve videos of already requested trainings
    public static final String temprelieverdataentryURL=baseURL + "add_temp_reliever_details.php?"; //api to enter details of temporary reliever
    public static final String temprelieversitemappedURL=baseURL + "temp_site_reliever_mapped.php?"; //api to get temporary relievers mapped to a site on a date
    public static final String relieversitemappedURL=baseURL + "site_reliever_mapped.php?"; //relievers mapped to a site on a date
    public static final String exeprofileimguploadURL=baseURL + "executive_profile_image.php?"; //api to upload new executive profile image
    public static final String materialissuesURL=baseURL + "material_issue.php?"; //api to upload material issues
    public static final String materialdeliveredURL=baseURL + "material_requested.php?"; //api to upload material issues
    public static final String materialdeliconfURL=baseURL + "material_delivered.php?"; //api to upload material issues



    //url parts for video links (youtube embed code)
    public static final String videoUrlFirstPart="<iframe width=\"100%\" height=\"100%\" src=\""; //first part of the url
    public static final String videoUrlLastPart="\" frameborder=\"0\" allow=\"accelerometer; autoplay; encrypted-media; gyroscope; picture-in-picture\" allowfullscreen></iframe>"; //last part of video url

    //arraylists for training questions
    public static ArrayList<TrainingModel> requestedTrainingList;
    public static ArrayList<QuestionModel> questionList;

    //function to request required permissions
    public static void requestRequiredPermissions(Context context, Activity mActivity)
    {
        ArrayList<String> permissions=new ArrayList<String>();

        //checking whether required permissions are granted
        if(ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
        {
            permissions.add(Manifest.permission.CAMERA);
        }
        if(ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
        {
            permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        }
        if(ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
        {
            permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if(ContextCompat.checkSelfPermission(context, Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED)
        {
            permissions.add(Manifest.permission.INTERNET);
        }
        if(ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            permissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if(ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        }

        //requesting permissions that are not granted
        if(!permissions.isEmpty())
        {
            ActivityCompat.requestPermissions(mActivity,
                    permissions.toArray(new String[permissions.size()]), REQUEST_PERMISSIONS);
        }
    }


    //function to check whether internet is connected
    public static boolean isNetworkAvailable(Context context)
    {
        ConnectivityManager connectivityManager=(ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork=connectivityManager.getActiveNetworkInfo();

        if(activeNetwork != null && activeNetwork.isConnected())
            return true;
        else
            return false;
    }
}
