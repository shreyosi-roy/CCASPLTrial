package com.demo.ccaspltrial.Executive;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.DatePicker;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.demo.ccaspltrial.LocalDatabase.ExecutiveDatabaseHelper;
import com.demo.ccaspltrial.R;
import com.demo.ccaspltrial.Utility.ExeSiteModel;
import com.demo.ccaspltrial.Utility.ImageModel;
import com.demo.ccaspltrial.Utility.Utils;
import com.toptoche.searchablespinnerlibrary.SearchableSpinner;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;

public class ViewSiteImages extends AppCompatActivity {

    GridView siteGridView;
    SearchableSpinner siteSpinner;
    ArrayList<ImageModel> imageList;
    ProgressBar site_progress;
    TextView date;
    ArrayList<String> site;
    ConstraintLayout si_layout1, si_layout2;
    ImageView si_fullImage;
    Toolbar toolbar;
    SiteAdapter adapter;

    String selectedDate="", selectedSiteId="";

    ArrayList<ExeSiteModel> siteList;

    ProgressDialog pDialog;

    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_site_images);

        siteGridView=(GridView)findViewById(R.id.siteGrid);
        siteSpinner=(SearchableSpinner)findViewById(R.id.selectSite);
        date=(TextView)findViewById(R.id.si_date);
        si_layout1=(ConstraintLayout)findViewById(R.id.si_layout1);
        si_layout2=(ConstraintLayout)findViewById(R.id.si_layout2);
        si_fullImage=(ImageView)findViewById(R.id.si_fullImage);
        toolbar=(Toolbar)findViewById(R.id.toolbar);
        site_progress=(ProgressBar)findViewById(R.id.site_progress);

        setSupportActionBar(toolbar);

        toolbar.setTitle("View Site Images");

        //initialising sharedpreferences
        Utils.loginPreferences=getSharedPreferences(Utils.loginPref, Context.MODE_PRIVATE);

        context=this;

        //setting up site spinner
        if(!Utils.loginPreferences.getString("executive_id", "").equalsIgnoreCase("")
                && Utils.isNetworkAvailable(context))
        {
            new RetrieveExeSiteTask().execute(Utils.exesiteretrievalURL);
        }
        else
        if(!Utils.isNetworkAvailable(context))
        {
            Toast.makeText(context, "No internet", Toast.LENGTH_SHORT).show();
        }

        //adding action to spinner item selection
        siteSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                String selectedEmp=site.get(position);
                selectedSiteId=siteList.get(position).getSiteId();

                //setting up images if date is selected
                if(date.getText().toString().equalsIgnoreCase(""))
                {
                    Toast.makeText(context, "Select date!", Toast.LENGTH_LONG).show();
                }
                else
                {
                    //setting up images
                    siteGridView.setVisibility(View.VISIBLE);

                    //displaying images
                    if(!selectedSiteId.equalsIgnoreCase("") && !selectedDate.equalsIgnoreCase("")
                            && Utils.isNetworkAvailable(context))
                    {
                        new SiteImagesTask().execute(Utils.imagelistviewURL);
                    }
                    else
                    if(!Utils.isNetworkAvailable(context))
                    {
                        Toast.makeText(context, "No internet", Toast.LENGTH_SHORT).show();
                    }
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

                siteGridView.setVisibility(View.GONE);
                selectedSiteId="";
            }
        });


        //adding action to date textview
        date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Getting current date
                Calendar calendar=Calendar.getInstance();
                final int year1=calendar.get(Calendar.YEAR);
                final int month1=calendar.get(Calendar.MONTH);
                final int day1=calendar.get(Calendar.DAY_OF_MONTH);

                //opening datepickerdialog
                DatePickerDialog pickerDialog=new DatePickerDialog(context, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {

                        showDate(year, month, dayOfMonth);

                        //add code to change list on date change after this
                        if(selectedSiteId.equalsIgnoreCase(""))
                        {
                            Toast.makeText(context, "Select site!", Toast.LENGTH_LONG).show();
                        }
                        else
                        {
                            //displaying images according to date when site is already selected
                            siteGridView.setVisibility(View.VISIBLE);

                            if(!selectedSiteId.equalsIgnoreCase("") && !selectedDate.equalsIgnoreCase("")
                                    && Utils.isNetworkAvailable(context))
                            {
                                new SiteImagesTask().execute(Utils.imagelistviewURL);
                            }
                            else
                            if(!Utils.isNetworkAvailable(context))
                            {
                                Toast.makeText(context, "No internet", Toast.LENGTH_SHORT).show();
                            }

                        }

                    }
                }, year1, month1, day1);
                pickerDialog.show();

            }
        });

    }


    //method for showing date
    public void showDate(int yy, int mm, int dd)
    {
        String year, month, day;

        year=""+yy;
        mm=mm+1;
        if(mm<10)
        {
            month="0"+mm;
        }
        else
        {
            month=""+mm;
        }
        if(dd<10)
        {
            day="0"+dd;
        }
        else
        {
            day=""+dd;
        }

        selectedDate=day+"-"+month+"-"+year;

        //setting date to textview
        date.setText(selectedDate);
    }

    //method to setup site spinner using data from asynctask RetrieveExeSiteTask
    public void setUpSiteSpinner(ArrayList<ExeSiteModel> list)
    {
        site=new ArrayList<String>();

        for (ExeSiteModel obj : list)
        {
            site.add(obj.getSiteCode() + " - " + obj.getSiteName());
        }

        //setting up spinner
        ArrayAdapter<String> siteAdapter=new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, site);
        siteAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        siteSpinner.setAdapter(siteAdapter);
    }


    @Override
    public void onBackPressed() {
        if(si_layout2.getVisibility() == View.VISIBLE)
        {
            si_layout2.setVisibility(View.GONE);
            si_layout1.setVisibility(View.VISIBLE);
        }
        else
        {
            startActivity(new Intent(ViewSiteImages.this, ExecutiveHome.class));
            finish();
        }
    }

    //viewholder class for SiteAdapter
    static class ViewHolder
    {
        ImageView image;
        TextView date;
    }

    //adapter class for gridview
    class SiteAdapter extends BaseAdapter
    {
        ArrayList<ImageModel> imagelist;
        LayoutInflater inflater;
        Context context;

        public SiteAdapter(){}

        public SiteAdapter(Context context, ArrayList<ImageModel> list)
        {
            this.context=context;
            imagelist=list;
            inflater=LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            return imagelist.size();
        }

        @Override
        public Object getItem(int position) {
            return imagelist.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {

            ViewHolder holder;

            if(convertView==null)
            {
                holder=new ViewHolder();

                convertView=inflater.inflate(R.layout.gallery_item, null);

                holder.date=(TextView)convertView.findViewById(R.id.grid_date);
                holder.image=(ImageView)convertView.findViewById(R.id.grid_image);

                convertView.setTag(holder);
            }
            else
            {
                holder=(ViewHolder)convertView.getTag();
            }

            //setting timestamp to item
            holder.date.setText(imagelist.get(position).getTimeStamp());


            //setting image to item
            holder.image.setImageBitmap(imagelist.get(position).getImageBitmap());


            //adding action to item click
            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    si_layout1.setVisibility(View.GONE);
                    si_layout2.setVisibility(View.VISIBLE);

                    si_fullImage.setImageBitmap(imagelist.get(position).getImageBitmap());
                }
            });

            return convertView;
        }
    }


    //asynctask for retrieving sites under an executive
    private class RetrieveExeSiteTask extends AsyncTask<String, Void, Void>
    {
        String content="";
        String error=null;
        String data="";
        String msg="";
        String status="";
        String executive_id ="";

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            site_progress.setVisibility(View.VISIBLE);

            executive_id = Utils.loginPreferences.getString("executive_id", "");

        }


        @Override
        protected Void doInBackground(String... strings) {

            try
            {
                data= URLEncoder.encode("executive_id", "UTF-8") + "="
                        + URLEncoder.encode(executive_id, "UTF-8");
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

            String site_id="", site_code="", site_name="";


            //showing error message if error has occurred
            if(error!=null)
            {
                if(Utils.appPreferences.getString("LocalExeSitePresent", "No").equalsIgnoreCase("Yes"))
                {
                    siteList=new ArrayList<ExeSiteModel>();

                    //loading data from executive local database
                    ExecutiveDatabaseHelper myExeDB=new ExecutiveDatabaseHelper(context, Utils.appPreferences.getInt("ExecutiveDBVersion", 1));
                    siteList=myExeDB.getExeSites();

                    setUpSiteSpinner(siteList);
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

                siteList=new ArrayList<ExeSiteModel>();

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
                        JSONArray arrayObj=obj2.getJSONArray("executive-site");
                        int arrLength=arrayObj.length();

                        for(int i=0; i<arrLength; i++)
                        {
                            JSONObject siteObj=arrayObj.getJSONObject(i);

                            site_id=siteObj.getString("site_id");
                            site_code=siteObj.getString("site_code");
                            site_name=siteObj.getString("site_name");

                            //adding values to ExeSiteModel object
                            ExeSiteModel newSite=new ExeSiteModel();
                            newSite.setSiteId(site_id);
                            newSite.setSiteCode(site_code);
                            newSite.setSiteName(site_name);

                            //adding object to list
                            siteList.add(newSite);
                        }
                    }
                }
                catch (JSONException je)
                {
                    je.printStackTrace();
                }

                if(status.equalsIgnoreCase("1"))
                {
                    setUpSiteSpinner(siteList);
                }
                else
                {
                    Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
                }
            } //outer else closes

            site_progress.setVisibility(View.GONE);
        }
    }//asynctask closes


    //asynctask for retrieving site images
    private class SiteImagesTask extends AsyncTask<String, Void, Void>
    {
        String content="";
        String error=null;
        String data="";
        String msg="";
        String status="";
        String doc_id="3";
        String site_id="";
        String date="";
        String imageUrl="", imageTimeStamp="";

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            site_progress.setVisibility(View.VISIBLE);

            site_id=selectedSiteId;
            date=selectedDate;
        }



        @Override
        protected Void doInBackground(String... strings) {

            try
            {
                data=URLEncoder.encode("site_id", "UTF-8") + "="
                        + URLEncoder.encode(site_id, "UTF-8") + "&"
                        + URLEncoder.encode("doc_id", "UTF-8") + "="
                        + URLEncoder.encode(doc_id, "UTF-8") + "&"
                        + URLEncoder.encode("date", "UTF-8") + "="
                        + URLEncoder.encode(date, "UTF-8");
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


            imageList=new ArrayList<ImageModel>();

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
                    JSONArray imageArray=obj2.getJSONArray("image");
                    int arrLength=imageArray.length();

                    for(int i=0; i<arrLength; i++)
                    {
                        JSONObject imageObj=imageArray.getJSONObject(i);
                        imageUrl=imageObj.getString("url");
//                        imageUrl="http://demo.brandconfiance.com/operation_app_ccaspl/image/image.png";
                        imageTimeStamp=imageObj.getString("timestamp");

                        URL url = new URL(imageUrl);
                        Bitmap image = BitmapFactory.decodeStream(url.openConnection().getInputStream());

                        //compressing image
                        ByteArrayOutputStream out = new ByteArrayOutputStream();
                        image.compress(Bitmap.CompressFormat.JPEG, 85, out);
                        Bitmap compressedImg= BitmapFactory.decodeStream(new ByteArrayInputStream(out.toByteArray()));

                        //saving data in ImageModel object
                        ImageModel newImage=new ImageModel();
                        newImage.setImageId(i);
                        newImage.setImageURL(imageUrl);
                        newImage.setImageBitmap(compressedImg);
                        newImage.setTimeStamp(extractTime(imageTimeStamp));

                        //adding object to imageList
                        imageList.add(newImage);
                    }
                }
            }
            catch (JSONException je)
            {
                je.printStackTrace();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }


            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);


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
            else {

                if (status.equalsIgnoreCase("1"))
                {
                    //reversing the image list to get the latest image first
                    Collections.reverse(imageList);

                    //setting up gridview
                    adapter = new SiteAdapter(context, imageList);
                    siteGridView.setAdapter(adapter);
                }
                else
                if (status.equalsIgnoreCase("0"))
                {
                    //setting up gridview
                    adapter = new SiteAdapter(context, imageList);
                    siteGridView.setAdapter(adapter);

                    Toast.makeText(context, msg, Toast.LENGTH_LONG).show();

                }
            }

            site_progress.setVisibility(View.GONE);
        }


        public String extractTime(String timeStamp)
        {
            int spaceindex=timeStamp.indexOf(' ');
            int colonindex=timeStamp.lastIndexOf(':');

            String time=timeStamp.substring(spaceindex, colonindex);

            return time;
        }
    }//asynctask closes
}
