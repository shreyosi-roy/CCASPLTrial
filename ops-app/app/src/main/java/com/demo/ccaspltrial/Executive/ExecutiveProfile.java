package com.demo.ccaspltrial.Executive;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.demo.ccaspltrial.R;
import com.demo.ccaspltrial.Utility.Utils;

public class ExecutiveProfile extends AppCompatActivity {

    TextView execode_tv, exename_tv, exephno_tv, exealtno_tv, exeemail_tv, exeaddress_tv;
    Button clickImage;
    Toolbar toolbar;

    Context context;
    Activity mActivity;

    String exeCode="", exeName="", exePhno="", exeAltno="", exeEmail="", exeAddress="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_executive_profile);

        execode_tv=(TextView)findViewById(R.id.execode_tv);
        exename_tv=(TextView)findViewById(R.id.exename_tv);
        exephno_tv=(TextView)findViewById(R.id.exephno_tv);
        exealtno_tv=(TextView)findViewById(R.id.exealtno_tv);
        exeemail_tv=(TextView)findViewById(R.id.exeemail_tv);
        exeaddress_tv=(TextView)findViewById(R.id.exeaddress_tv);
        clickImage=(Button)findViewById(R.id.exeimage_btn);
        toolbar=(Toolbar)findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);

        toolbar.setTitle("Profile");

        //initialising sharedpreferences
        Utils.loginPreferences=getSharedPreferences(Utils.loginPref, Context.MODE_PRIVATE);

        context=getApplicationContext();
        mActivity=this;

        //call method to set profile data
        setProfileData();

        //adding action to click profile image button
        clickImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //call method to add action to button
                takeImage();
            }
        });

    }

    //method for action on clicking button
    public void takeImage()
    {
        if(ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
        {
            Utils.requestRequiredPermissions(context, mActivity);
        }
        else
        {
            Intent picIntent=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

            if(picIntent.resolveActivity(mActivity.getPackageManager()) != null)
            {
                startActivityForResult(picIntent, Utils.REQUEST_EXEPROFILE_PHOTO);
            }
            else
            {
                Toast.makeText(mActivity, "No camera available", Toast.LENGTH_SHORT).show();
            }
        }
    }//takeImage closes

    //method to set profile data
    public void setProfileData()
    {
        exeCode=Utils.loginPreferences.getString("executive_code", "");
        exeName=Utils.loginPreferences.getString("executive_name", "");
        exePhno=Utils.loginPreferences.getString("executive_phno", "");
        exeAltno=Utils.loginPreferences.getString("executive_altno", "");
        exeEmail=Utils.loginPreferences.getString("executive_email", "");
        exeAddress=Utils.loginPreferences.getString("executive_address", "");

        execode_tv.setText(exeCode);
        exename_tv.setText(exeName);
        exephno_tv.setText(exePhno);
        exealtno_tv.setText(exeAltno);
        exeemail_tv.setText(exeEmail);
        exeaddress_tv.setText(exeAddress);
    }//setProfileData closes

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        Intent i=new Intent(ExecutiveProfile.this, SaveExeProfileImage.class);

        if(requestCode==Utils.REQUEST_EXEPROFILE_PHOTO && resultCode==Activity.RESULT_OK)
        {
            Bundle extras=data.getExtras();
            Bitmap imageBitmap=(Bitmap)extras.get("data");

            i.putExtras(extras);
//            i.putExtra("Type", "ExeProfileImg");
            startActivity(i);
            finish();
        }
    }

    @Override
    public void onBackPressed() {

        startActivity(new Intent(ExecutiveProfile.this, ExecutiveHome.class));
        finish();
    }
}
