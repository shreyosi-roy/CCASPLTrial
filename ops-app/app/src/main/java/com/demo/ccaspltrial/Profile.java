package com.demo.ccaspltrial;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.demo.ccaspltrial.Utility.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

public class Profile extends AppCompatActivity {

    TextView empname, empcode, gender, designation, phoneno, altno, email, doj, sitecode, sitename,
            permanentAdd, currentAdd, siteAdd;
    ConstraintLayout gender_layout, phoneno_layout, altno_layout, email_layout, doj_layout,
            sitecode_layout, sitename_layout, perAdd_layout, currAdd_layout, siteAdd_layout;

    ProgressDialog pDialog;
    Context context;
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_profile);

        empname=(TextView)findViewById(R.id.empname);
        empcode=(TextView)findViewById(R.id.empcode);
        gender=(TextView)findViewById(R.id.gender);
        designation=(TextView)findViewById(R.id.designation);
        phoneno=(TextView)findViewById(R.id.phoneno);
        altno=(TextView)findViewById(R.id.altno);
        email=(TextView)findViewById(R.id.email);
        doj=(TextView)findViewById(R.id.doj);
        sitecode=(TextView)findViewById(R.id.sitecode);
        sitename=(TextView)findViewById(R.id.sitename);
        permanentAdd=(TextView)findViewById(R.id.permanentAdd);
        currentAdd=(TextView)findViewById(R.id.currentAdd);
        siteAdd=(TextView)findViewById(R.id.siteAdd);
        gender_layout=(ConstraintLayout)findViewById(R.id.gender_layout);
        phoneno_layout=(ConstraintLayout)findViewById(R.id.phoneno_layout);
        altno_layout=(ConstraintLayout)findViewById(R.id.altno_layout);
        email_layout=(ConstraintLayout)findViewById(R.id.email_layout);
        doj_layout=(ConstraintLayout)findViewById(R.id.doj_layout);
        sitecode_layout=(ConstraintLayout)findViewById(R.id.sitecode_layout);
        sitename_layout=(ConstraintLayout)findViewById(R.id.sitename_layout);
        perAdd_layout=(ConstraintLayout)findViewById(R.id.perAdd_layout);
        currAdd_layout=(ConstraintLayout)findViewById(R.id.currentAdd_layout);
        siteAdd_layout=(ConstraintLayout)findViewById(R.id.siteAdd_layout);
        toolbar=(Toolbar)findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);
        toolbar.setTitle("Profile");

        context=this;

        //initialising sharedprefrences
        Utils.loginPreferences=getSharedPreferences(Utils.loginPref, MODE_PRIVATE);

        empname.setText(Utils.loginPreferences.getString("emp_name", ""));
        empcode.setText(Utils.loginPreferences.getString("emp_code", ""));
        if(Utils.loginPreferences.getString("gender", "").equalsIgnoreCase(""))
        {
            gender_layout.setVisibility(View.GONE);
        }
        else
        {
            gender.setText(Utils.loginPreferences.getString("gender", ""));
        }
        if(Utils.loginPreferences.getString("phoneno", "").equalsIgnoreCase(""))
        {
            phoneno_layout.setVisibility(View.GONE);
        }
        else
        {
            phoneno.setText(Utils.loginPreferences.getString("phoneno", ""));
        }
        if(Utils.loginPreferences.getString("altno", "").equalsIgnoreCase(""))
        {
            altno_layout.setVisibility(View.GONE);
        }
        else
        {
            altno.setText(Utils.loginPreferences.getString("altno", ""));
        }
        if(Utils.loginPreferences.getString("email", "").equalsIgnoreCase(""))
        {
            email_layout.setVisibility(View.GONE);
        }
        else
        {
            email.setText(Utils.loginPreferences.getString("email", ""));
        }
        if(Utils.loginPreferences.getString("emp_doj", "").equalsIgnoreCase(""))
        {
            doj_layout.setVisibility(View.GONE);
        }
        else
        {
            doj.setText(Utils.loginPreferences.getString("emp_doj", ""));
        }
        if(Utils.loginPreferences.getString("current_add", "").equalsIgnoreCase(""))
        {
            currAdd_layout.setVisibility(View.GONE);
        }
        else
        {
            currentAdd.setText(Utils.loginPreferences.getString("current_add", ""));
        }
        if(Utils.loginPreferences.getString("permanent_add", "").equalsIgnoreCase(""))
        {
            perAdd_layout.setVisibility(View.GONE);
        }
        else
        {
            permanentAdd.setText(Utils.loginPreferences.getString("permanent_add", ""));
        }
        if(Utils.loginPreferences.getString("site_code", "").equalsIgnoreCase(""))
        {
            sitecode_layout.setVisibility(View.GONE);
        }
        else
        {
            sitecode.setText(Utils.loginPreferences.getString("site_code", ""));
        }
        if(Utils.loginPreferences.getString("site_name", "").equalsIgnoreCase(""))
        {
            sitename_layout.setVisibility(View.GONE);
        }
        else
        {
            sitename.setText(Utils.loginPreferences.getString("site_name", ""));
        }
        if(Utils.loginPreferences.getString("site_add", "").equalsIgnoreCase(""))
        {
            siteAdd_layout.setVisibility(View.GONE);
        }
        else
        {
            siteAdd.setText(Utils.loginPreferences.getString("site_add", ""));
        }

        if(Utils.loginPreferences.getString("LoginType", "None").equalsIgnoreCase("Regular"))
        {
            designation.setText("Regular employee");
        }
        else
        {
            designation.setText(Utils.loginPreferences.getString("LoginType", "None"));
        }

        
    }

    @Override
    public void onBackPressed() {

        startActivity(new Intent(Profile.this, HomePage.class));
        finish();
    }



}
