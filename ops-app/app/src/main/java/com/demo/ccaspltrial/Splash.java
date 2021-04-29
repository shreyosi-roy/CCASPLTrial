package com.demo.ccaspltrial;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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

public class Splash extends AppCompatActivity {

    private static int SPLASH_TIME_OUT=3000;

    ProgressDialog pDialog;

    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_splash);

        context=this;

        //initialising sharedpreferences
        Utils.loginPreferences=getSharedPreferences(Utils.loginPref, MODE_PRIVATE);
        Utils.loginEditor=Utils.loginPreferences.edit();
        Utils.pagePreferences=getSharedPreferences(Utils.pagePref, MODE_PRIVATE);
        Utils.pageEditor=Utils.pagePreferences.edit();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                //checking whether already logged in
                Utils.loginPreferences=getSharedPreferences(Utils.loginPref, MODE_PRIVATE);
                if(Utils.loginPreferences.getString("loggedIn", "No").equalsIgnoreCase("Yes"))
                {
                    //checking login type
                    if(Utils.loginPreferences.getString("LoginType", "None")
                            .equalsIgnoreCase("Regular"))
                    {
                        Utils.pageEditor.putString("LastScreenForQuestions", "SplashScreen").commit();

                        Intent intent=new Intent(Splash.this, HomePage.class);
                        startActivity(intent);
                        finish();

                    }
                    else
                    if(Utils.loginPreferences.getString("LoginType", "None")
                            .equalsIgnoreCase("Reliever"))
                    {
                        Utils.pageEditor.putString("LastScreenForQuestions", "SplashScreen").commit();

                        if(!Utils.loginPreferences.getString("emp_id", "").equalsIgnoreCase("")
                                && Utils.isNetworkAvailable(Splash.this))
                        {
                            //getting site and substituted employee of reliever
                            new GetRelieverSiteTask().execute(Utils.relieversiteURL);
                        }
                        else
                        {
                            Toast.makeText(Splash.this, "No internet", Toast.LENGTH_SHORT).show();

                            Intent intent=new Intent(Splash.this, HomePage.class);
                            startActivity(intent);
                            finish();
                        }
                    }
                    else
                    if(Utils.loginPreferences.getString("LoginType", "None")
                            .equalsIgnoreCase("Executive"))
                    {
                        Intent intent=new Intent(Splash.this, ExecutiveHome.class);
                        startActivity(intent);
                        finish();
                    }

                }
                else
                {
                    Intent intent=new Intent(Splash.this, Login.class);
                    startActivity(intent);
                    finish();
                }

            }
        }, SPLASH_TIME_OUT);

    }


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

//            Splash.this.pDialog=new ProgressDialog(Splash.this);
//            Splash.this.pDialog.setTitle("Loading...");
//            Splash.this.pDialog.setCancelable(false);
//            Splash.this.pDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
//            Splash.this.pDialog.show();

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

            //closing progressdialog
//            Splash.this.pDialog.dismiss();

            //showing error message if error has occurred
            if(error!=null)
            {
                AlertDialog.Builder aDialog=new AlertDialog.Builder(Splash.this);
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
                    if(site_id.equalsIgnoreCase("null"))
                    {
                        Utils.loginEditor.putString("site_id", "");
                    }
                    else
                    {
                        Utils.loginEditor.putString("site_id", site_id);
                    }
                    if(site_code.equalsIgnoreCase("null"))
                    {
                        Utils.loginEditor.putString("site_code", "");
                    }
                    else
                    {
                        Utils.loginEditor.putString("site_code", site_code);
                    }
                    if(site_name.equalsIgnoreCase("null"))
                    {
                        Utils.loginEditor.putString("site_name", "");
                    }
                    else
                    {
                        Utils.loginEditor.putString("site_name", site_name);
                    }
                    if(site_add.equalsIgnoreCase("null"))
                    {
                        Utils.loginEditor.putString("site_add", "");
                    }
                    else
                    {
                        Utils.loginEditor.putString("site_add", site_add);
                    }
                    if(sub_employee_id.equalsIgnoreCase("null"))
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
                    Toast.makeText(Splash.this, msg, Toast.LENGTH_LONG).show();
                }

                //opening home page
                Intent intent=new Intent(Splash.this, HomePage.class);
                startActivity(intent);
                finish();

            }//outer else closes
        }
    }//asynctask closes

}
