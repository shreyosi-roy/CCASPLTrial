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
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.demo.ccaspltrial.LocalDatabase.RegularDatabaseHelper;
import com.demo.ccaspltrial.LocalDatabase.RelieverDatabaseHelper;
import com.demo.ccaspltrial.Utility.TrainingModel;
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

public class RequestTraining extends AppCompatActivity {

    Toolbar toolbar;
    ListView reqTraining_listview;
    Button request;
    ProgressBar reqtraining_progress;

    ProgressDialog pDialog;

    Context context;

    RegularDatabaseHelper myRegularDB;
    RelieverDatabaseHelper myRelieverDB;

    TrainingAdapter adapter;

    ArrayList<TrainingModel> trainingList;

    public static ArrayList<TrainingModel> requiredTraining;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request_training);

        toolbar=(Toolbar)findViewById(R.id.toolbar);
        request=(Button)findViewById(R.id.request);
        reqTraining_listview=(ListView)findViewById(R.id.reqTraining_listview);
        reqtraining_progress=(ProgressBar)findViewById(R.id.reqtraining_progress);

        setSupportActionBar(toolbar);
        toolbar.setTitle("Training List");

        context=this;

        //initialising sharedpreferences
        Utils.loginPreferences=getSharedPreferences(Utils.loginPref, MODE_PRIVATE);
        Utils.appPreferences=getSharedPreferences(Utils.appPref, MODE_PRIVATE);
        Utils.appEditor=Utils.appPreferences.edit();
        Utils.pagePreferences=getSharedPreferences(Utils.pagePref, MODE_PRIVATE);
        Utils.pageEditor=Utils.pagePreferences.edit();

        if(Utils.loginPreferences.getString("LoginType", "None").equalsIgnoreCase("Regular"))
        {
            myRegularDB=new RegularDatabaseHelper(context, Utils.appPreferences.getInt("RegularDBVersion", 1));
        }
        else
        if(Utils.loginPreferences.getString("LoginType", "None").equalsIgnoreCase("Reliever"))
        {
            myRelieverDB=new RelieverDatabaseHelper(context, Utils.appPreferences.getInt("RelieverDBVersion", 1));
        }


        //setting up training listview
        if(!Utils.loginPreferences.getString("emp_id", "").equalsIgnoreCase("")
                && Utils.isNetworkAvailable(context))
        {
            new TrainingTask().execute(Utils.viewtraininglistURL);
        }
        else
        if(!Utils.isNetworkAvailable(context))
        {
            Toast.makeText(context, "No internet", Toast.LENGTH_SHORT).show();
        }

        //adding action to request button
        request.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(!requiredTraining.isEmpty())
                {
                    Utils.pageEditor.putString("LastScreen", "TrainingList").commit();
                    startActivity(new Intent(RequestTraining.this, TrainingVideos.class));
                    finish();
                }
                else
                {
                    Toast.makeText(context, "Select trainings to view", Toast.LENGTH_LONG).show();
                }
            }
        });


    }


    @Override
    public void onBackPressed() {

        startActivity(new Intent(RequestTraining.this, Certifications.class));
        finish();
    }


    //viewholder for TrainingAdapter
    static class ViewHolder
    {
        TextView training_name;
        CheckBox training_checkbox;
    }

    //adapter class for listview
    class TrainingAdapter extends BaseAdapter
    {
        ArrayList<TrainingModel> training_list;
        Context context;
        LayoutInflater inflater;

        public TrainingAdapter(Context context, ArrayList<TrainingModel> list)
        {
            training_list=list;
            this.context=context;
            inflater=LayoutInflater.from(context);
            requiredTraining=new ArrayList<TrainingModel>();
        }

        @Override
        public int getCount() {
            return training_list.size();
        }

        @Override
        public Object getItem(int position) {
            return training_list.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {

            final ViewHolder holder;

            if(convertView==null)
            {
                holder=new ViewHolder();

                convertView=inflater.inflate(R.layout.required_training_item, null);

                holder.training_checkbox=(CheckBox)convertView.findViewById(R.id.training_checkbox);
                holder.training_name=(TextView)convertView.findViewById(R.id.training_name);

                convertView.setTag(holder);
            }
            else
            {
                holder=(ViewHolder)convertView.getTag();
            }

            //setting training name to item
            holder.training_name.setText(training_list.get(position).getTrainingName());

            //setting checked or unchecked status of training-----for maintaining check type of item in list
            if(requiredTraining.contains(training_list.get(position)))
            {
                holder.training_checkbox.setChecked(true);
            }
            else
            {
                holder.training_checkbox.setChecked(false);
            }

            //adding action to checkbox selection and un-selection
            holder.training_checkbox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    //checking whether checkbox is checked
                    if(!holder.training_checkbox.isChecked())
                    {
                        //removing item from required list, if present
                        if(requiredTraining.contains(training_list.get(position)))
                        {
                            requiredTraining.remove(training_list.get(position));
                        }
                    }
                    else
                    {
                        if(!requiredTraining.contains(training_list.get(position)))
                        {
                            requiredTraining.add(training_list.get(position));
                        }
                    }
                }
            });

            return convertView;
        }
    }//adapter class closes


    //asynctask to retrieve other trainings available
    private class TrainingTask extends AsyncTask<String, Void, Void>
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

            reqtraining_progress.setVisibility(View.VISIBLE);

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

            String training_id="", training_name="";

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
                trainingList=new ArrayList<TrainingModel>();

                //retrieving data from webservice
                try
                {
                    JSONArray jsonResponse=new JSONArray(content);
                    JSONObject obj1=jsonResponse.getJSONObject(0);
                    status=obj1.getString("status");
                    msg=obj1.getString("msg");

                    if(status.equalsIgnoreCase("1"))
                    {
                        JSONObject obj2=jsonResponse.getJSONObject(1);
                        JSONArray arrayObj=obj2.getJSONArray("training_list");
                        int arrLength=arrayObj.length();

                        for(int i=0; i<arrLength; i++)
                        {
                            JSONObject trainingObj=arrayObj.getJSONObject(i);

                            training_id=trainingObj.getString("training_id");
                            training_name=trainingObj.getString("training_name");

                            //adding data to object
                            TrainingModel newTraining=new TrainingModel();
                            newTraining.setTrainingId(training_id);
                            newTraining.setTrainingName(training_name);

                            //adding object to list
                            trainingList.add(newTraining);
                        }//for closes
                    }
                }//try closes
                catch (JSONException je)
                {
                    je.printStackTrace();
                }

                if(status.equalsIgnoreCase("1"))
                {
                    //setting up training list
                    adapter=new TrainingAdapter(context, trainingList);
                    reqTraining_listview.setAdapter(adapter);
                }
                else
                if(status.equalsIgnoreCase("0"))
                {
                    Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
                }
            }//outer else closes

            reqtraining_progress.setVisibility(View.GONE);
        }
    }//asynctask closes

}
