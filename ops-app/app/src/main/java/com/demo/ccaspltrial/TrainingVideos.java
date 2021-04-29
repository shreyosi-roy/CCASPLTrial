package com.demo.ccaspltrial;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.demo.ccaspltrial.Utility.TrainingModel;
import com.demo.ccaspltrial.Utility.Utils;
import com.demo.ccaspltrial.Utility.VideoAdapter;
import com.demo.ccaspltrial.Utility.VideoModel;

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

public class TrainingVideos extends AppCompatActivity {

    RecyclerView training_recyclerview;
    Toolbar toolbar;
    EditText search_input;
    ArrayList<VideoModel> video_list, searchedList;
    VideoAdapter videoAdapter;
    String search_text="";

    Context context;

    ProgressDialog pDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_training);

        training_recyclerview=(RecyclerView) findViewById(R.id.training_recyclerview);
        search_input=(EditText)findViewById(R.id.search_input);
        toolbar=(Toolbar)findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);
        toolbar.setTitle("Training Videos");

        context=this;

        //initialising sharedpreferences
        Utils.loginPreferences=getSharedPreferences(Utils.loginPref, MODE_PRIVATE);
        Utils.appPreferences=getSharedPreferences(Utils.appPref, MODE_PRIVATE);
        Utils.appEditor=Utils.appPreferences.edit();
        Utils.pagePreferences=getSharedPreferences(Utils.pagePref, MODE_PRIVATE);
        Utils.pageEditor=Utils.pagePreferences.edit();

        training_recyclerview.setHasFixedSize(true);
        training_recyclerview.setLayoutManager(new LinearLayoutManager(this));

        if(Utils.pagePreferences.getString("LastScreen", "").equalsIgnoreCase("TrainingList"))
        {
            //retrieving videos of requested trainings
            if(!RequestTraining.requiredTraining.isEmpty()
                    && !Utils.loginPreferences.getString("emp_id", "").equalsIgnoreCase("")
                    && Utils.isNetworkAvailable(context))
            {
                new RetrieveVideosTask().execute(Utils.retrievevideoURL);
            }
            else
            if(RequestTraining.requiredTraining.isEmpty())
            {
                Toast.makeText(context, "No trainings selected", Toast.LENGTH_SHORT).show();
            }
            else
            if(!Utils.isNetworkAvailable(context))
            {
                Toast.makeText(context, "No internet", Toast.LENGTH_SHORT).show();
            }
        }
        else
        if(Utils.pagePreferences.getString("LastScreen", "").equalsIgnoreCase("HomeScreen"))
        {
            //retrieving videos of requested trainings
            if(!Utils.loginPreferences.getString("emp_id", "").equalsIgnoreCase("")
                    && Utils.isNetworkAvailable(context))
            {
                new RetrieveVideos2Task().execute(Utils.retrievevideo2URL);
            }
            else
            if(!Utils.isNetworkAvailable(context))
            {
                Toast.makeText(context, "No internet", Toast.LENGTH_SHORT).show();
            }
        }

        //adding search functionality
        search_input.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                search_text=search_input.getText().toString();

                if(search_text.equalsIgnoreCase(""))
                {
                    //calling adapter and setting training list with all items
                    videoAdapter=new VideoAdapter(context, video_list);
                    training_recyclerview.setAdapter(videoAdapter);
                }
                else
                if(video_list.isEmpty())
                {
                    Toast.makeText(context, "No videos to search", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    searchedList=new ArrayList<VideoModel>();

                    for(int i=0; i<video_list.size(); i++)
                    {
                        if(video_list.get(i).getVideoTitle().toLowerCase().contains(search_text.toLowerCase()))
                        {
                            searchedList.add(video_list.get(i));
                        }
                    }

                    //calling adapter and setting training list with searched items
                    videoAdapter=new VideoAdapter(context, searchedList);
                    training_recyclerview.setAdapter(videoAdapter);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });


    }

    @Override
    public void onBackPressed() {

        if(Utils.pagePreferences.getString("LastScreen", "").equalsIgnoreCase("TrainingList"))
        {
            Utils.pageEditor.putString("LastScreen", "").commit();

            startActivity(new Intent(TrainingVideos.this, RequestTraining.class));
            finish();
        }
        else
        if(Utils.pagePreferences.getString("LastScreen", "").equalsIgnoreCase("HomeScreen"))
        {
            Utils.pageEditor.putString("LastScreen", "").commit();

            startActivity(new Intent(TrainingVideos.this, HomePage.class));
            finish();
        }
    }


    //asynctask to get video links
    private class RetrieveVideosTask extends AsyncTask<String, Integer, Void>
    {
        String content="";
        String error=null;
        String data="";
        String msg="";
        String status="";
        String employee_id="";
        String training_id="";
        ArrayList<TrainingModel> requestedList;
        int progressStep=0;
        int count=0;
        String finalMsg="";

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            TrainingVideos.this.pDialog=new ProgressDialog(context);
            TrainingVideos.this.pDialog.setTitle("Loading...");
            TrainingVideos.this.pDialog.setCancelable(false);
            TrainingVideos.this.pDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            TrainingVideos.this.pDialog.setMax(100);
            TrainingVideos.this.pDialog.setProgress(0);
            TrainingVideos.this.pDialog.show();

            employee_id=Utils.loginPreferences.getString("emp_id", "");

            requestedList=RequestTraining.requiredTraining;

            if(!requestedList.isEmpty())
            {
                progressStep=100/requestedList.size();
            }
        }


        @Override
        protected Void doInBackground(String... strings) {

            video_list=new ArrayList<VideoModel>();

            for(TrainingModel obj : requestedList)
            {
                String content="";
                String error=null;
                String data="";
                String msg="";
                String status="";
                String video_title="", video_url="";

                training_id=obj.getTrainingId();

                try
                {
                    data= URLEncoder.encode("employee_id", "UTF-8") + "="
                            + URLEncoder.encode(employee_id, "UTF-8") + "&"
                            + URLEncoder.encode("training_id", "UTF-8") + "="
                            + URLEncoder.encode(training_id, "UTF-8");
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

                if(error != null)
                    break;

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
                        video_title=obj2.getString("video_title");
                        video_url=obj2.getString("video_url");
                    }
                }
                catch (JSONException je)
                {
                    je.printStackTrace();
                    finalMsg=je.getMessage();
                }

                if(status.equalsIgnoreCase("1"))
                {
                    //adding data to object
                    VideoModel newVideo=new VideoModel();
                    newVideo.setVideoTitle(video_title);
                    newVideo.setVideoUrl(Utils.videoUrlFirstPart + video_url + Utils.videoUrlLastPart);

                    //adding object to list
                    video_list.add(newVideo);

                    count+=progressStep;
                    publishProgress(count);
                }
                else
                if(status.equalsIgnoreCase("0"))
                {
                    finalMsg+=" " + obj.getTrainingName() + " " + msg + "\n";
                }
            }//for closes


            return null;
        }


        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);

            TrainingVideos.this.pDialog.setProgress(values[0]);
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            //closing progressdialog
            TrainingVideos.this.pDialog.dismiss();

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
            if(!finalMsg.equalsIgnoreCase(""))
            {
                Toast.makeText(context, finalMsg, Toast.LENGTH_LONG).show();
            }
            else
            {
                //calling adapter and setting training list
                videoAdapter=new VideoAdapter(context, video_list);
                training_recyclerview.setAdapter(videoAdapter);
            }
        }
    }//asynctask closes


    //asynctask to get video links
    private class RetrieveVideos2Task extends AsyncTask<String, Void, Void>
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

            TrainingVideos.this.pDialog=new ProgressDialog(context);
            TrainingVideos.this.pDialog.setTitle("Loading...");
            TrainingVideos.this.pDialog.setCancelable(false);
            TrainingVideos.this.pDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            TrainingVideos.this.pDialog.show();

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

            String video_title="", video_url="";

            //closing progressdialog
            TrainingVideos.this.pDialog.dismiss();

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
            else {
                video_list = new ArrayList<VideoModel>();

                //retrieving data from webservice
                try {
                    JSONArray jsonResponse = new JSONArray(content);
                    JSONObject obj1 = jsonResponse.getJSONObject(0);
                    status = obj1.getString("status");
                    msg = obj1.getString("msg");

                    if (status.equalsIgnoreCase("1")) {
                        JSONObject obj2 = jsonResponse.getJSONObject(1);
                        JSONArray arrayObj = obj2.getJSONArray("video_list");
                        int arrLength = arrayObj.length();

                        for (int i = 0; i < arrLength; i++) {
                            JSONObject videoObj = arrayObj.getJSONObject(i);
                            video_title = videoObj.getString("video_title");
                            video_url = videoObj.getString("video_url");

                            //adding data to object
                            VideoModel newVideo = new VideoModel();
                            newVideo.setVideoTitle(video_title);
                            newVideo.setVideoUrl(Utils.videoUrlFirstPart + video_url + Utils.videoUrlLastPart);

                            //adding object to list
                            video_list.add(newVideo);
                        }//for closes
                    }
                }//try closes
                catch (JSONException je)
                {
                    je.printStackTrace();
                }

                if (status.equalsIgnoreCase("1"))
                {
                    //calling adapter and setting training list
                    videoAdapter = new VideoAdapter(context, video_list);
                    training_recyclerview.setAdapter(videoAdapter);
                }
                else
                if (status.equalsIgnoreCase("0"))
                {
                    Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
                }
            }
        }
    }//asynctask closes

}
