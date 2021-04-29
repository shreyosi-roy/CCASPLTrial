package com.demo.ccaspltrial;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import com.demo.ccaspltrial.Executive.ExecutiveHome;
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

public class Login extends AppCompatActivity {

    Button login;
    EditText username, password;
    String userId="", pw="", logType="";
    Spinner loginType;
    ProgressBar login_progress;
    String type[]={"Select login type", "Regular employee", "Reliever", "Executive"};

    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        login=(Button)findViewById(R.id.login);
        username=(EditText)findViewById(R.id.username);
        password=(EditText)findViewById(R.id.password);
        loginType=(Spinner)findViewById(R.id.loginType);
        login_progress=(ProgressBar)findViewById(R.id.login_progress);

        //requesting required permissions
        Utils.requestRequiredPermissions(this, this);

        context=this;

        Utils.loginPreferences=getSharedPreferences(Utils.loginPref, MODE_PRIVATE);
        Utils.loginEditor=Utils.loginPreferences.edit();
        Utils.appPreferences=getSharedPreferences(Utils.appPref, MODE_PRIVATE);
        Utils.appEditor=Utils.appPreferences.edit();
        Utils.pagePreferences=getSharedPreferences(Utils.pagePref, MODE_PRIVATE);
        Utils.pageEditor=Utils.pagePreferences.edit();

        //setting local DB version count to 0 for first time login
        //for regular emp
        if(Utils.appPreferences.getInt("RegularDBVersion", 1) <= 1)
            Utils.appEditor.putInt("RegularDBVersion", 1).commit();
        //for reliever
        if(Utils.appPreferences.getInt("RelieverDBVersion", 1) <= 1)
            Utils.appEditor.putInt("RelieverDBVersion", 1).commit();
        //for executive
        if(Utils.appPreferences.getInt("ExecutiveDBVersion", 1) <= 1)
            Utils.appEditor.putInt("ExecutiveDBVersion", 1).commit();



        //setting up arrayadapter for logintype spinner
        ArrayAdapter<String> arrayAdapter=new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, type);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        //setting the arrayadapter to the spinner
        loginType.setAdapter(arrayAdapter);

        //setting itemselected listener on loginType spinner
        loginType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                switch (position)
                {
                    case 0:
                        Utils.loginEditor.putString("LoginType", "None");
                        break;
                    case 1:
                        Utils.loginEditor.putString("LoginType", "Regular");
                        break;
                    case 2:
                        Utils.loginEditor.putString("LoginType", "Reliever");
                        break;
                    case 3:
                        Utils.loginEditor.putString("LoginType", "Executive");
                        break;
                }

                Utils.loginEditor.commit();

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

                Utils.loginEditor.putString("LoginType", "None");
                Utils.loginEditor.commit();
            }
        });

        //adding action to login button
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //getting userid and password from edittexts
                userId=username.getText().toString();
                pw=password.getText().toString();

                logType=Utils.loginPreferences.getString("LoginType", "None");

                //checking whether logintype is selected or not
                if(logType.equalsIgnoreCase("None"))
                {
                    Toast.makeText(context, "Please select login type", Toast.LENGTH_SHORT).show();
                }
                else
                if(logType.equalsIgnoreCase("Executive") && Utils.isNetworkAvailable(context))
                {
                    //calling ExecutiveLoginTask to check if username and password is valid and matches
                    new ExecutiveLoginTask().execute(Utils.executiveloginURL, userId, pw);
                }
                else
                if(logType.equalsIgnoreCase("Regular") && Utils.isNetworkAvailable(context))
                {
                    Utils.pageEditor.putString("LastScreenForQuestions", "LoginScreen").commit();

                    //calling LoginTask to check if username and password is valid and matches
                    new LoginTask().execute(Utils.loginURL, userId, pw);

                }
                else
                if(logType.equalsIgnoreCase("Reliever") && Utils.isNetworkAvailable(context))
                {
                    Utils.pageEditor.putString("LastScreenForQuestions", "LoginScreen").commit();

                    //calling RelieverLoginTask to check if username and password is valid and matches
                    new RelieverLoginTask().execute(Utils.relieverloginURL, userId, pw);
                }
                else
                if(!Utils.isNetworkAvailable(context))
                {
                    Toast.makeText(context, "No internet", Toast.LENGTH_SHORT).show();
                }


            }
        });


    }

    @Override
    public void onBackPressed() {

        finish();
    }


    //asynctask class to verify login details taken from the user
    private class LoginTask extends AsyncTask<String, Void, Void>
    {
        String content="";
        String error=null;
        String data="";
        String msg="";
        String status="";

        @Override
        protected void onPreExecute() {

            login_progress.setVisibility(View.VISIBLE);
        }

        @Override
        protected Void doInBackground(String... strings) {

            try
            {
                //encoding user data to be passed to api url
                data= URLEncoder.encode("username", "UTF-8") + "="
                        + URLEncoder.encode(strings[1], "UTF-8") + "&"
                        + URLEncoder.encode("password", "UTF-8") + "="
                        + URLEncoder.encode(strings[2], "UTF-8");
            }
            catch (UnsupportedEncodingException uee)
            {
                uee.printStackTrace();
            }


            BufferedReader br=null;

            try
            {
                URL url=new URL(strings[0]);
                URLConnection conn=url.openConnection();
                conn.setDoOutput(true);
                OutputStreamWriter outWr=new OutputStreamWriter(conn.getOutputStream());
                outWr.write(data);
                outWr.flush();
                br=new BufferedReader(new InputStreamReader(conn.getInputStream()));

                StringBuilder sb=new StringBuilder();
                String line=null;

                //reading server response
                while((line = br.readLine()) != null)
                {
                    sb.append(line+"\n");
                }

                content=sb.toString();
            }
            catch (Exception e)
            {
                error=e.getMessage();
            }
            finally {

                try
                {
                    br.close();
                }
                catch (Exception e){}
            }


            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {

            //Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();

            String emp_code="", emp_name="", emp_id="", curr_add="", per_add="",
                    phno="", altno="", email="", emp_doj="", gender="", profile_image="", site_id="", site_code="",
                    site_name="", site_add="";

            //showing error message if error has occurred
            if(error!=null)
            {
                AlertDialog.Builder aDialog=new AlertDialog.Builder(context);
                aDialog.setMessage("Error in connecting to server! Please check your network connection.")
                        .setCancelable(true)
                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                dialog.cancel();
                            }
                        });

                AlertDialog alert=aDialog.create();
                alert.show();
            }
            else
            {
                //retrieving data from webservice
                try
                {

                    JSONObject jsonResponse=new JSONObject(content);
                    status=jsonResponse.getString("status");
                    msg=jsonResponse.getString("msg");

                    //accepting login and profile details if login is successful
                    if(status.equalsIgnoreCase("1"))
                    {
                        //retrieving data from api
                        emp_code=jsonResponse.getString("employee_code");
                        emp_name=jsonResponse.getString("employee_name");
                        emp_id=jsonResponse.getString("employee_id");
                        gender=jsonResponse.getString("gender");
                        phno=jsonResponse.getString("phone_number");
                        altno=jsonResponse.getString("alternate_number");
                        email=jsonResponse.getString("email");
                        emp_doj=jsonResponse.getString("employee_doj");
                        profile_image=jsonResponse.getString("profile_image");
                        curr_add=jsonResponse.getString("present_address");
                        per_add=jsonResponse.getString("parmanent_address");
                        site_id=jsonResponse.getString("site_id");
                        site_code=jsonResponse.getString("site_code");
                        site_name=jsonResponse.getString("site_name");
                        site_add=jsonResponse.getString("site_address");
                    }

                }
                catch (JSONException je)
                {
                    je.printStackTrace();
                }

                //checking if login details are valid
                if(status.equalsIgnoreCase("1"))
                {
                    //adding login details to sharedpreferences
                    Utils.loginEditor.putString("loggedIn", "Yes");
                    if(emp_code.equalsIgnoreCase("null")
                            || emp_code.equalsIgnoreCase(""))
                    {
                        Utils.loginEditor.putString("emp_code", "");
                    }
                    else
                    {
                        Utils.loginEditor.putString("emp_code", emp_code);
                    }
                    if(emp_name.equalsIgnoreCase("null")
                            || emp_name.equalsIgnoreCase(""))
                    {
                        Utils.loginEditor.putString("emp_name", "");
                    }
                    else
                    {
                        Utils.loginEditor.putString("emp_name", emp_name);
                    }
                    if(emp_id.equalsIgnoreCase("null")
                            || emp_id.equalsIgnoreCase(""))
                    {
                        Utils.loginEditor.putString("emp_id", "");
                    }
                    else
                    {
                        Utils.loginEditor.putString("emp_id", emp_id);
                    }
                    if(gender.equalsIgnoreCase("null")
                            || gender.equalsIgnoreCase(""))
                    {
                        Utils.loginEditor.putString("gender", "");
                    }
                    else
                    {
                        Utils.loginEditor.putString("gender", gender);
                    }
                    if(phno.equalsIgnoreCase("null")
                            || phno.equalsIgnoreCase(""))
                    {
                        Utils.loginEditor.putString("phoneno", "");
                    }
                    else
                    {
                        Utils.loginEditor.putString("phoneno", phno);
                    }
                    if(altno.equalsIgnoreCase("null")
                            || altno.equalsIgnoreCase(""))
                    {
                        Utils.loginEditor.putString("altno", "");
                    }
                    else
                    {
                        Utils.loginEditor.putString("altno", altno);
                    }
                    if(email.equalsIgnoreCase("null")
                            || email.equalsIgnoreCase(""))
                    {
                        Utils.loginEditor.putString("email", "");
                    }
                    else
                    {
                        Utils.loginEditor.putString("email", email);
                    }
                    if(emp_doj.equalsIgnoreCase("null")
                            || emp_doj.equalsIgnoreCase(""))
                    {
                        Utils.loginEditor.putString("emp_doj", "");
                    }
                    else
                    {
                        Utils.loginEditor.putString("emp_doj", emp_doj);
                    }
                    if(profile_image.equalsIgnoreCase("null")
                            || profile_image.equalsIgnoreCase(""))
                    {
                        Utils.loginEditor.putString("profileImage", "");
                    }
                    else
                    {
                        Utils.loginEditor.putString("profileImage", profile_image);
                    }
                    if(curr_add.equalsIgnoreCase("null")
                            || curr_add.equalsIgnoreCase(""))
                    {
                        Utils.loginEditor.putString("current_add", "");
                    }
                    else
                    {
                        Utils.loginEditor.putString("current_add", curr_add);
                    }
                    if(per_add.equalsIgnoreCase("null")
                            || per_add.equalsIgnoreCase(""))
                    {
                        Utils.loginEditor.putString("permanent_add", "");
                    }
                    else
                    {
                        Utils.loginEditor.putString("permanent_add", per_add);
                    }
                    if(site_id.equalsIgnoreCase("null")
                            || site_id.equalsIgnoreCase(""))
                    {
                        Utils.loginEditor.putString("site_id", "");
                    }
                    else
                    {
                        Utils.loginEditor.putString("site_id", site_id);
                    }
                    if(site_code.equalsIgnoreCase("null")
                            || site_code.equalsIgnoreCase(""))
                    {
                        Utils.loginEditor.putString("site_code", "");
                    }
                    else
                    {
                        Utils.loginEditor.putString("site_code", site_code);
                    }
                    if(site_name.equalsIgnoreCase("null")
                            || site_name.equalsIgnoreCase(""))
                    {
                        Utils.loginEditor.putString("site_name", "");
                    }
                    else
                    {
                        Utils.loginEditor.putString("site_name", site_name);
                    }
                    if(site_add.equalsIgnoreCase("null")
                            || site_add.equalsIgnoreCase(""))
                    {
                        Utils.loginEditor.putString("site_add", "");
                    }
                    else
                    {
                        Utils.loginEditor.putString("site_add", site_add);
                    }
                    Utils.loginEditor.commit();

                    //checking which login type
                    if(logType.equalsIgnoreCase("Regular"))
                    {
                        //going to regular employee screens
                        startActivity(new Intent(Login.this, HomePage.class));
                        finish();
                    }

                }
                else
                if(status.equalsIgnoreCase("0"))
                {
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
                }

            }//outer else closes

            login_progress.setVisibility(View.GONE);
        }
    }//asynctask closes


    //asynctask for executive login
    private class ExecutiveLoginTask extends AsyncTask<String, Void, Void>
    {
        String content="";
        String error=null;
        String data="";
        String msg="";
        String status="";

        @Override
        protected void onPreExecute() {

            login_progress.setVisibility(View.VISIBLE);
        }

        @Override
        protected Void doInBackground(String... strings) {

            try
            {
                //encoding user data to be passed to api url
                data= URLEncoder.encode("username", "UTF-8") + "="
                        + URLEncoder.encode(strings[1], "UTF-8") + "&"
                        + URLEncoder.encode("password", "UTF-8") + "="
                        + URLEncoder.encode(strings[2], "UTF-8");
            }
            catch (UnsupportedEncodingException uee)
            {
                uee.printStackTrace();
            }


            BufferedReader br=null;

            try
            {
                URL url=new URL(strings[0]);
                URLConnection conn=url.openConnection();
                conn.setDoOutput(true);
                OutputStreamWriter outWr=new OutputStreamWriter(conn.getOutputStream());
                outWr.write(data);
                outWr.flush();
                br=new BufferedReader(new InputStreamReader(conn.getInputStream()));

                StringBuilder sb=new StringBuilder();
                String line=null;

                //reading server response
                while((line = br.readLine()) != null)
                {
                    sb.append(line+"\n");
                }

                content=sb.toString();
            }
            catch (Exception e)
            {
                error=e.getMessage();
            }
            finally {

                try
                {
                    br.close();
                }
                catch (Exception e){}
            }


            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {

            String exe_code="", exe_name="", exe_id="", exe_profile="", exe_phno="", exe_altno="",
                    exe_email="", exe_address="";


            //showing error message if error has occurred
            if(error!=null)
            {
                AlertDialog.Builder aDialog=new AlertDialog.Builder(context);
                aDialog.setMessage("Error in connecting to server! Please check your network connection.")
                        .setCancelable(true)
                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                dialog.cancel();
                            }
                        });

                AlertDialog alert=aDialog.create();
                alert.show();
            }
            else
            {
                //retrieving data from webservice
                try
                {

                    JSONObject jsonResponse=new JSONObject(content);
                    status=jsonResponse.getString("status");
                    msg=jsonResponse.getString("msg");

                    //accepting login and profile details if login is successful
                    if(status.equalsIgnoreCase("1"))
                    {
                        //retrieving data from api
                        exe_code=jsonResponse.getString("executive_code");
                        exe_name=jsonResponse.getString("executive_name");
                        exe_id=jsonResponse.getString("executive_id");
                        exe_profile=jsonResponse.getString("profile_image");
                        exe_phno=jsonResponse.getString("phone_number");
                        exe_altno=jsonResponse.getString("alternate_number");
                        exe_email=jsonResponse.getString("email");
                        exe_address=jsonResponse.getString("present_address");
                    }

                }
                catch (JSONException je)
                {
                    je.printStackTrace();
                }

                //checking if login details are valid
                if(status.equalsIgnoreCase("1"))
                {
                    //adding login details to sharedpreferences
                    Utils.loginEditor.putString("loggedIn", "Yes");
                    if(exe_code.equalsIgnoreCase("null")
                            || exe_code.equalsIgnoreCase(""))
                    {
                        Utils.loginEditor.putString("executive_code", "");
                    }
                    else
                    {
                        Utils.loginEditor.putString("executive_code", exe_code);
                    }

                    if(exe_name.equalsIgnoreCase("null")
                            || exe_name.equalsIgnoreCase(""))
                    {
                        Utils.loginEditor.putString("executive_name", "");
                    }
                    else
                    {
                        Utils.loginEditor.putString("executive_name", exe_name);
                    }

                    if(exe_id.equalsIgnoreCase("null")
                            || exe_id.equalsIgnoreCase(""))
                    {
                        Utils.loginEditor.putString("executive_id", "");
                    }
                    else
                    {
                        Utils.loginEditor.putString("executive_id", exe_id);
                    }

                    if(exe_phno.equalsIgnoreCase("null")
                            || exe_phno.equalsIgnoreCase(""))
                    {
                        Utils.loginEditor.putString("executive_phno", "");
                    }
                    else
                    {
                        Utils.loginEditor.putString("executive_phno", exe_phno);
                    }

                    if(exe_altno.equalsIgnoreCase("null")
                            || exe_altno.equalsIgnoreCase(""))
                    {
                        Utils.loginEditor.putString("executive_altno", "");
                    }
                    else
                    {
                        Utils.loginEditor.putString("executive_altno", exe_altno);
                    }

                    if(exe_email.equalsIgnoreCase("null")
                            || exe_email.equalsIgnoreCase(""))
                    {
                        Utils.loginEditor.putString("executive_email", "");
                    }
                    else
                    {
                        Utils.loginEditor.putString("executive_email", exe_email);
                    }

                    if(exe_address.equalsIgnoreCase("null")
                            || exe_address.equalsIgnoreCase(""))
                    {
                        Utils.loginEditor.putString("executive_address", "");
                    }
                    else
                    {
                        Utils.loginEditor.putString("executive_address", exe_address);
                    }

                    if(exe_profile.equalsIgnoreCase("null")
                            || exe_profile.equalsIgnoreCase(""))
                    {
                        Utils.loginEditor.putString("executive_profileimg", "");
                    }
                    else
                    {
                        Utils.loginEditor.putString("executive_profileimg", exe_profile);
                    }

                    Utils.loginEditor.commit();

                    //going to executive screens
                    startActivity(new Intent(Login.this, ExecutiveHome.class));
                    finish();


                }
                else
                if(status.equalsIgnoreCase("0"))
                {
                    Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
                }
            }

            login_progress.setVisibility(View.GONE);
        }
    }//asynctask closes


    //asynctask for reliever login
    private class RelieverLoginTask extends AsyncTask<String, Void, Void>
    {
        String content="";
        String error=null;
        String data="";
        String msg="";
        String status="";

        @Override
        protected void onPreExecute() {

            login_progress.setVisibility(View.VISIBLE);
        }

        @Override
        protected Void doInBackground(String... strings) {

            try
            {
                //encoding user data to be passed to api url
                data= URLEncoder.encode("username", "UTF-8") + "="
                        + URLEncoder.encode(strings[1], "UTF-8") + "&"
                        + URLEncoder.encode("password", "UTF-8") + "="
                        + URLEncoder.encode(strings[2], "UTF-8");
            }
            catch (UnsupportedEncodingException uee)
            {
                uee.printStackTrace();
            }


            BufferedReader br=null;

            try
            {
                URL url=new URL(strings[0]);
                URLConnection conn=url.openConnection();
                conn.setDoOutput(true);
                OutputStreamWriter outWr=new OutputStreamWriter(conn.getOutputStream());
                outWr.write(data);
                outWr.flush();
                br=new BufferedReader(new InputStreamReader(conn.getInputStream()));

                StringBuilder sb=new StringBuilder();
                String line=null;

                //reading server response
                while((line = br.readLine()) != null)
                {
                    sb.append(line+"\n");
                }

                content=sb.toString();
            }
            catch (Exception e)
            {
                error=e.getMessage();
            }
            finally {

                try
                {
                    br.close();
                }
                catch (Exception e){}
            }


            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {

            String rel_code="", rel_name="", rel_id="", curr_add="", per_add="", phno="", altno="",
                    email="", emp_doj="", gender="", profile_image="";


            //showing error message if error has occurred
            if(error!=null)
            {
                AlertDialog.Builder aDialog=new AlertDialog.Builder(context);
                aDialog.setMessage("Error in connecting to server! Please check your network connection.")
                        .setCancelable(true)
                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                dialog.cancel();
                            }
                        });

                AlertDialog alert=aDialog.create();
                alert.show();
            }
            else
            {
                //retrieving data from webservice
                try
                {

                    JSONObject jsonResponse=new JSONObject(content);
                    status=jsonResponse.getString("status");
                    msg=jsonResponse.getString("msg");

                    //accepting login and profile details if login is successful
                    if(status.equalsIgnoreCase("1"))
                    {
                        //retrieving data from api
                        rel_code=jsonResponse.getString("employee_code");
                        rel_name=jsonResponse.getString("employee_name");
                        rel_id=jsonResponse.getString("employee_id");
                        gender=jsonResponse.getString("gender");
                        phno=jsonResponse.getString("phone_number");
                        altno=jsonResponse.getString("alternate_number");
                        email=jsonResponse.getString("email");
                        emp_doj=jsonResponse.getString("employee_doj");
                        curr_add=jsonResponse.getString("present_address");
                        per_add=jsonResponse.getString("parmanent_address");
                        profile_image=jsonResponse.getString("profile_image");
                    }

                }
                catch (JSONException je)
                {
                    je.printStackTrace();
                }

                //checking if login details are valid
                if(status.equalsIgnoreCase("1"))
                {
                    //adding login details to sharedpreferences
                    Utils.loginEditor.putString("loggedIn", "Yes");
                    if(rel_code.equalsIgnoreCase("null")
                            || rel_code.equalsIgnoreCase(""))
                    {
                        Utils.loginEditor.putString("emp_code", "");
                    }
                    else
                    {
                        Utils.loginEditor.putString("emp_code", rel_code);
                    }
                    if(rel_name.equalsIgnoreCase("null")
                            || rel_name.equalsIgnoreCase(""))
                    {
                        Utils.loginEditor.putString("emp_name", "");
                    }
                    else
                    {
                        Utils.loginEditor.putString("emp_name", rel_name);
                    }
                    if(rel_id.equalsIgnoreCase("null")
                            || rel_id.equalsIgnoreCase(""))
                    {
                        Utils.loginEditor.putString("emp_id", "");
                    }
                    else
                    {
                        Utils.loginEditor.putString("emp_id", rel_id);
                    }
                    if(gender.equalsIgnoreCase("null")
                            || gender.equalsIgnoreCase(""))
                    {
                        Utils.loginEditor.putString("gender", "");
                    }
                    else
                    {
                        Utils.loginEditor.putString("gender", gender);
                    }
                    if(phno.equalsIgnoreCase("null")
                            || phno.equalsIgnoreCase(""))
                    {
                        Utils.loginEditor.putString("phoneno", "");
                    }
                    else
                    {
                        Utils.loginEditor.putString("phoneno", phno);
                    }
                    if(altno.equalsIgnoreCase("null")
                            || altno.equalsIgnoreCase(""))
                    {
                        Utils.loginEditor.putString("altno", "");
                    }
                    else
                    {
                        Utils.loginEditor.putString("altno", altno);
                    }
                    if(email.equalsIgnoreCase("null")
                            || email.equalsIgnoreCase(""))
                    {
                        Utils.loginEditor.putString("email", "");
                    }
                    else
                    {
                        Utils.loginEditor.putString("email", email);
                    }
                    if(emp_doj.equalsIgnoreCase("null")
                            || emp_doj.equalsIgnoreCase(""))
                    {
                        Utils.loginEditor.putString("emp_doj", "");
                    }
                    else
                    {
                        Utils.loginEditor.putString("emp_doj", emp_doj);
                    }
                    if(curr_add.equalsIgnoreCase("null")
                            || curr_add.equalsIgnoreCase(""))
                    {
                        Utils.loginEditor.putString("current_add", "");
                    }
                    else
                    {
                        Utils.loginEditor.putString("current_add", curr_add);
                    }
                    if(per_add.equalsIgnoreCase("null")
                            || per_add.equalsIgnoreCase(""))
                    {
                        Utils.loginEditor.putString("permanent_add", "");
                    }
                    else
                    {
                        Utils.loginEditor.putString("permanent_add", per_add);
                    }
                    if(profile_image.equalsIgnoreCase("null")
                            || profile_image.equalsIgnoreCase(""))
                    {
                        Utils.loginEditor.putString("profileImage", "");
                    }
                    else
                    {
                        Utils.loginEditor.putString("profileImage", profile_image);
                    }

                    //for reliever login site mapping
                    Utils.loginEditor.putString("site_id", "");
                    Utils.loginEditor.putString("site_code", "");
                    Utils.loginEditor.putString("site_name", "");
                    Utils.loginEditor.putString("site_add", "");
                    Utils.loginEditor.putString("RelieverSubEmployeeId", "");

                    Utils.loginEditor.commit();

                    if(Utils.isNetworkAvailable(context))
                    {
                        //getting site and substituted employee of reliever
                        new GetRelieverSiteTask().execute(Utils.relieversiteURL);
                    }
                    else
                    {
                        Toast.makeText(context, "No internet", Toast.LENGTH_SHORT).show();
                    }


                }
                else
                if(status.equalsIgnoreCase("0"))
                {
                    Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
                }


            }//outer else closes

            login_progress.setVisibility(View.GONE);
        }
    }//asynctask closes


    //retrieving reliever site mapping
    private class GetRelieverSiteTask extends AsyncTask<String, Void, Void>
    {
        String content="";
        String error=null;
        String data="";
        String msg="";
        String status="";
        String employee_id="";

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            login_progress.setVisibility(View.VISIBLE);

            employee_id=Utils.loginPreferences.getString("emp_id", "");
        }


        @Override
        protected Void doInBackground(String... strings) {

            try
            {
                data= URLEncoder.encode("employee_id", "UTF-8") + "="
                        + URLEncoder.encode(employee_id, "UTF-8");
            }
            catch (UnsupportedEncodingException uee)
            {
                uee.printStackTrace();
            }

            BufferedReader br=null;

            try
            {
                URL url=new URL(strings[0]);
                URLConnection conn=url.openConnection();
                conn.setDoOutput(true);
                OutputStreamWriter outWr=new OutputStreamWriter(conn.getOutputStream());
                outWr.write(data);
                outWr.flush();
                br=new BufferedReader(new InputStreamReader(conn.getInputStream()));

                StringBuilder sb=new StringBuilder();
                String line=null;

                //reading server response
                while((line = br.readLine()) != null)
                {
                    sb.append(line+"\n");
                }

                content=sb.toString();
            }
            catch (Exception e)
            {
                error=e.getMessage();
            }
            finally {

                try
                {
                    br.close();
                }
                catch (Exception e){}
            }


            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            String site_id="", site_code="", site_name="", site_add="", sub_employee_id="";

            //showing error message if error has occurred
            if(error!=null)
            {
                AlertDialog.Builder aDialog=new AlertDialog.Builder(Login.this);
                aDialog.setMessage("Error in connecting to server! Please check your network connection.")
                        .setCancelable(true)
                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                dialog.cancel();
                            }
                        });

                AlertDialog alert=aDialog.create();
                alert.show();
            }
            else
            {
                //retrieving data from webservice
                try
                {
                    JSONObject jsonResponse=new JSONObject(content);
                    status=jsonResponse.getString("status");
                    msg=jsonResponse.getString("msg");

                    if(status.equalsIgnoreCase("1"))
                    {
                        site_id=jsonResponse.getString("site_id");
                        site_code=jsonResponse.getString("site_code");
                        site_name=jsonResponse.getString("site_name");
                        site_add=jsonResponse.getString("site_address");
                        sub_employee_id=jsonResponse.getString("regular_employee_id");
                    }
                }
                catch (JSONException je)
                {
                    je.printStackTrace();
                }

                //saving data to sharedpreferences
                if(status.equalsIgnoreCase("1"))
                {
                    if(site_id.equalsIgnoreCase("null")
                            || site_id.equalsIgnoreCase(""))
                    {
                        Utils.loginEditor.putString("site_id", "");
                    }
                    else
                    {
                        Utils.loginEditor.putString("site_id", site_id);
                    }
                    if(site_code.equalsIgnoreCase("null")
                            || site_code.equalsIgnoreCase(""))
                    {
                        Utils.loginEditor.putString("site_code", "");
                    }
                    else
                    {
                        Utils.loginEditor.putString("site_code", site_code);
                    }
                    if(site_name.equalsIgnoreCase("null")
                            || site_name.equalsIgnoreCase(""))
                    {
                        Utils.loginEditor.putString("site_name", "");
                    }
                    else
                    {
                        Utils.loginEditor.putString("site_name", site_name);
                    }
                    if(site_add.equalsIgnoreCase("null")
                            || site_add.equalsIgnoreCase(""))
                    {
                        Utils.loginEditor.putString("site_add", "");
                    }
                    else
                    {
                        Utils.loginEditor.putString("site_add", site_add);
                    }
                    if(sub_employee_id.equalsIgnoreCase("null")
                            || sub_employee_id.equalsIgnoreCase(""))
                    {
                        Utils.loginEditor.putString("RelieverSubEmployeeId", "");
                    }
                    else
                    {
                        Utils.loginEditor.putString("RelieverSubEmployeeId", sub_employee_id);
                    }

                    Utils.loginEditor.commit();


                }
                else
                if(status.equalsIgnoreCase("0"))
                {
                    Toast.makeText(Login.this, msg, Toast.LENGTH_LONG).show();
                }

                //opening home page
                Intent intent=new Intent(Login.this, HomePage.class);
                startActivity(intent);
                finish();

            }//outer else closes

            login_progress.setVisibility(View.GONE);
        }
    }//asynctask closes
}
