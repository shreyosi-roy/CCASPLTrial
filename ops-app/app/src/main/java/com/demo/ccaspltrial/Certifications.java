package com.demo.ccaspltrial;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.demo.ccaspltrial.LocalDatabase.RegularDatabaseHelper;
import com.demo.ccaspltrial.LocalDatabase.RelieverDatabaseHelper;
import com.demo.ccaspltrial.Utility.CertificationModel;
import com.demo.ccaspltrial.Utility.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;

public class Certifications extends AppCompatActivity {

    Toolbar toolbar;
    ListView cert_listview;
    Button required;
    ProgressBar certifications_progress;

    CertificationAdapter adapter;

    Context context;

    ArrayList<CertificationModel> certList;

    RegularDatabaseHelper myRegularDB;
    RelieverDatabaseHelper myRelieverDB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_certifications);

        toolbar=(Toolbar)findViewById(R.id.toolbar);
        cert_listview=(ListView)findViewById(R.id.cert_listview);
        required=(Button)findViewById(R.id.submit_extraTraining);
        certifications_progress=(ProgressBar)findViewById(R.id.certifications_progress);

        setSupportActionBar(toolbar);
        toolbar.setTitle("Certifications");

        context=this;

        //initialising sharedpreferences
        Utils.loginPreferences=getSharedPreferences(Utils.loginPref, MODE_PRIVATE);
        Utils.appPreferences=getSharedPreferences(Utils.appPref, MODE_PRIVATE);
        Utils.appEditor=Utils.appPreferences.edit();

        if(Utils.loginPreferences.getString("LoginType", "None").equalsIgnoreCase("Regular"))
        {
            myRegularDB=new RegularDatabaseHelper(context, Utils.appPreferences.getInt("RegularDBVersion", 1));
        }
        else
        if(Utils.loginPreferences.getString("LoginType", "None").equalsIgnoreCase("Reliever"))
        {
            myRelieverDB=new RelieverDatabaseHelper(context, Utils.appPreferences.getInt("RelieverDBVersion", 1));
        }

        //retrieving and displaying certifications of employee
        if(!Utils.loginPreferences.getString("emp_id", "").equalsIgnoreCase("")
                && Utils.isNetworkAvailable(context))
        {
            new ViewCertificationsTask().execute(Utils.certificationviewURL);
        }
        else
        if(!Utils.isNetworkAvailable(context))
        {
            Toast.makeText(context, "No internet", Toast.LENGTH_SHORT).show();
        }

        //adding action to require extra button
        required.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                startActivity(new Intent(Certifications.this, RequestTraining.class));
                finish();

            }
        });
    }

    @Override
    public void onBackPressed() {

        startActivity(new Intent(Certifications.this, HomePage.class));
        finish();

    }


    //viewholder class for CertificationAdapter
    static class ViewHolder
    {
        TextView slno, cert_name;
    }

    //adapter class for listview
    class CertificationAdapter extends BaseAdapter
    {
        ArrayList<CertificationModel> certlist;
        Context context;
        LayoutInflater inflater;

        public CertificationAdapter(Context context, ArrayList<CertificationModel> list)
        {
            this.context=context;
            certlist=list;
            inflater=LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            return certlist.size();
        }

        @Override
        public Object getItem(int position) {
            return certlist.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            ViewHolder holder;

            if(convertView==null)
            {
                convertView=inflater.inflate(R.layout.certification_item, null );

                holder=new ViewHolder();

                holder.slno=(TextView)convertView.findViewById(R.id.slno);
                holder.cert_name=(TextView)convertView.findViewById(R.id.cert_name);

                convertView.setTag(holder);
            }
            else
            {
                holder=(ViewHolder)convertView.getTag();
            }

            //setting name to item
            holder.cert_name.setText(certlist.get(position).getCertName());

            //setting slno to item
            holder.slno.setText(""+(position+1));

            return convertView;
        }
    }


    //asynctask to display certifications list of the employee
    private class ViewCertificationsTask extends AsyncTask<String, Integer, Void>
    {
        String content="";
        String error=null;
        String data="";
        String msg="";
        String status="";
        String employee_id="";
        int localRegularInsertCertCount=0, localRelieverInsertCertCount=0;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            certifications_progress.setVisibility(View.VISIBLE);

            employee_id= Utils.loginPreferences.getString("emp_id", "");
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

            String training_id="", training_name="";


            //showing error message if error has occurred
            if(error!=null)
            {
                if(Utils.loginPreferences.getString("LoginType", "None").equalsIgnoreCase("Regular")
                        && Utils.appPreferences.getString("RegularLocalCertificationPresent", "No").equalsIgnoreCase("Yes"))
                {
                    certList=new ArrayList<CertificationModel>();

                    //loading data from regular local database
                    certList=myRegularDB.getCertifications();

                    //setting up listview
                    adapter=new CertificationAdapter(context, certList);
                    cert_listview.setAdapter(adapter);
                }
                else
                if(Utils.loginPreferences.getString("LoginType", "None").equalsIgnoreCase("Reliever")
                        && Utils.appPreferences.getString("RelieverLocalCertificationPresent", "No").equalsIgnoreCase("Yes"))
                {
                    certList=new ArrayList<CertificationModel>();

                    //loading data from reliever local database
                    certList=myRelieverDB.getCertifications();

                    //setting up listview
                    adapter=new CertificationAdapter(context, certList);
                    cert_listview.setAdapter(adapter);
                }
                else
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
            }
            else
            {
                certList=new ArrayList<CertificationModel>();

                try
                {
                    //retrieving data from webservice
                    JSONArray jsonResponse=new JSONArray(content);
                    JSONObject obj1=jsonResponse.getJSONObject(0);
                    status=obj1.getString("status");
                    msg=obj1.getString("msg");

                    if(status.equalsIgnoreCase("1"))
                    {
                        JSONObject obj2=jsonResponse.getJSONObject(1);
                        JSONArray arrayObj=obj2.getJSONArray("certificates");
                        int arrLength=arrayObj.length();

                        for(int i=0; i<arrLength; i++)
                        {
                            JSONObject certObj=arrayObj.getJSONObject(i);
                            training_id=certObj.getString("traning_id");
                            training_name=certObj.getString("traning_name");

                            //adding data to new object
                            CertificationModel newCert=new CertificationModel();
                            newCert.setCertId(training_id);
                            newCert.setCertName(training_name);

                            //adding object to list
                            certList.add(newCert);

                        }//for closes
                    }

                }//try closes
                catch (JSONException je)
                {
                    je.printStackTrace();
                }

                if(status.equalsIgnoreCase("1"))
                {
                    //setting up listview
                    adapter=new CertificationAdapter(context, certList);
                    cert_listview.setAdapter(adapter);
                }
                else
                if(status.equalsIgnoreCase("0"))
                {
                    Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
                }
            }//outer else closes

            certifications_progress.setVisibility(View.GONE);
        }
    }//asynctask closes
}
