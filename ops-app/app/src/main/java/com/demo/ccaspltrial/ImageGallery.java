package com.demo.ccaspltrial;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.constraint.ConstraintLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.demo.ccaspltrial.LocalDatabase.RegularDatabaseHelper;
import com.demo.ccaspltrial.LocalDatabase.RelieverDatabaseHelper;
import com.demo.ccaspltrial.Utility.ImageModel;
import com.demo.ccaspltrial.Utility.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;

public class ImageGallery extends AppCompatActivity {

    Toolbar toolbar;
    Button newImage;
    TextView date;
    GridView gallery_gridview;
    GalleryAdapter adapter;
    ConstraintLayout layout1, layout2;
    ImageView fullImage;
    Activity mActivity;
    ProgressBar gallery_progress;

    Context context;

    ArrayList<ImageModel> imageList;

    String selectedDate="";

    RegularDatabaseHelper myRegularDB;
    RelieverDatabaseHelper myRelieverDB;

    private LocationManager locationManager;

    //Devina edit - for high resolution img
    Uri picUri;
    ContentValues values;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_gallery);

        toolbar=(Toolbar)findViewById(R.id.toolbar);
        newImage=(Button)findViewById(R.id.clickImage);
        date=(TextView)findViewById(R.id.dateSelection);
        gallery_gridview=(GridView)findViewById(R.id.gallery_gridview);
        layout1=(ConstraintLayout)findViewById(R.id.gallery_layout1);
        layout2=(ConstraintLayout)findViewById(R.id.gallery_layout2);
        fullImage=(ImageView)findViewById(R.id.gallery_full_image);
        gallery_progress=(ProgressBar)findViewById(R.id.gallery_progress);

        setSupportActionBar(toolbar);

        context=this;

        //initialising sharedpreferences
        Utils.loginPreferences=getSharedPreferences(Utils.loginPref, MODE_PRIVATE);
        Utils.pagePreferences=getSharedPreferences(Utils.pagePref, MODE_PRIVATE);
        Utils.pageEditor=Utils.pagePreferences.edit();
        Utils.appPreferences=getSharedPreferences(Utils.appPref, MODE_PRIVATE);
        Utils.appEditor=Utils.appPreferences.edit();

        mActivity=this;

        locationManager=(LocationManager)getSystemService(Context.LOCATION_SERVICE);

        if(Utils.loginPreferences.getString("LoginType", "None").equalsIgnoreCase("Regular"))
        {
            myRegularDB=new RegularDatabaseHelper(context, Utils.appPreferences.getInt("RegularDBVersion", 1));
        }
        else
        {
            myRelieverDB=new RelieverDatabaseHelper(context, Utils.appPreferences.getInt("RelieverDBVersion", 1));
        }

        //setting screen title on toolbar and retrieving intent extras if any and loading image gridview
        if(Utils.selfieFlag==1)
        {
            toolbar.setTitle("Selfie Gallery");

            //loading image gridview
            if(!Utils.loginPreferences.getString("emp_id", "").equalsIgnoreCase(""))
            {
                new GroomingImagesTask().execute(Utils.imagelistviewURL);
            }

        }
        else
        if(Utils.siteFlag==1)
        {
            toolbar.setTitle("Site Image Gallery");

            if(Utils.pagePreferences.getString("LastScreen", "").equalsIgnoreCase("SiteImagePreview"))
            {
                selectedDate=Utils.appPreferences.getString("SiteImageDate", "");
            }
            else
            {
                Intent i=getIntent();
                Bundle b=i.getExtras();
                selectedDate=b.getString("SelectedDate");

                Utils.appEditor.putString("SiteImageDate", selectedDate).commit();
            }

            date.setVisibility(View.VISIBLE);
            date.setText(selectedDate);

            //loading image gridview
            if(!Utils.loginPreferences.getString("site_id", "").equalsIgnoreCase("")
                    && !Utils.appPreferences.getString("SiteImageDate", "").equalsIgnoreCase(""))
            {
                new SiteImagesTask().execute(Utils.imagelistviewURL);
            }
            else
            if(Utils.loginPreferences.getString("site_id", "").equalsIgnoreCase(""))
            {
                Toast.makeText(mActivity, "No site assigned", Toast.LENGTH_LONG).show();
            }
        }
        else
        if(Utils.challanFlag==1)
        {
            toolbar.setTitle("Delivery Challan Images");

            //loading gridview
            if(!Utils.loginPreferences.getString("site_id", "").equalsIgnoreCase(""))
            {
                new ChallanImagesTask().execute(Utils.imagelistviewURL);
            }
            else
            {
                Toast.makeText(mActivity, "No site assigned", Toast.LENGTH_LONG).show();
            }
        }
        else
        if(Utils.attendanceFlag==1)
        {
            toolbar.setTitle("Attendance Sheet Images");

            //loading gridview
            if(!Utils.loginPreferences.getString("site_id", "").equalsIgnoreCase(""))
            {
                new AttendanceImagesTask().execute(Utils.imagelistviewURL);
            }
            else
            {
                Toast.makeText(mActivity, "No site assigned", Toast.LENGTH_LONG).show();
            }
        }

        //adding datepicker dialog to date textview
        date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Getting current date
                Calendar calendar=Calendar.getInstance();
                final int year1=calendar.get(Calendar.YEAR);
                final int month1=calendar.get(Calendar.MONTH);
                final int day1=calendar.get(Calendar.DAY_OF_MONTH);

                //opening datepickerdialog
                DatePickerDialog pickerDialog=new DatePickerDialog(ImageGallery.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {

                        showDate(year, month, dayOfMonth);

                        //add code to change list on date change after this
                        if(!Utils.loginPreferences.getString("site_id", "").equalsIgnoreCase("")
                                && !Utils.appPreferences.getString("SiteImageDate", "").equalsIgnoreCase(""))
                        {
                            new SiteImagesTask().execute(Utils.imagelistviewURL);
                        }

                    }
                }, year1, month1, day1);
                pickerDialog.show();

            }
        });


        //adding action to new image button
        newImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(ContextCompat.checkSelfPermission(ImageGallery.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
                {
                    Utils.requestRequiredPermissions(ImageGallery.this, ImageGallery.this);
                }
                else
                {
                    Intent picIntent=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                    //Devina edit - for high resolution img
                    //determine uri of camera image to save
                    values = new ContentValues();
                    values.put(MediaStore.Images.Media.TITLE, "New Picture");
                    values.put(MediaStore.Images.Media.DESCRIPTION, "From your Camera");
                    picUri = getContentResolver().insert(
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                    picIntent.putExtra(MediaStore.EXTRA_OUTPUT, picUri);
                    //Devina edit - for high resolution img ENDS

                    if(Utils.selfieFlag==1)
                    {
                        if((ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
                                && (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED))
                        {
                            Toast.makeText(mActivity, "Location permission is required!", Toast.LENGTH_LONG).show();
                            Utils.requestRequiredPermissions(ImageGallery.this, ImageGallery.this);
                        }
                        else
                        {
                            //allowing to upload picture only if location services are available
                            if(!locationManager.isProviderEnabled(locationManager.GPS_PROVIDER))
                            {
                                AlertDialog.Builder builder=new AlertDialog.Builder(context);
                                builder.setMessage("Enable GPS to continue!")
                                        .setCancelable(false)
                                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {

                                                Intent locationIntent=new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                                dialog.dismiss();
                                                startActivity(locationIntent);
                                            }
                                        })
                                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {

                                                Toast.makeText(context, "Selfie cannot be uploaded without location", Toast.LENGTH_LONG).show();
                                                dialog.dismiss();
                                            }
                                        }).show();
                            }
                            else
                            if(picIntent.resolveActivity(mActivity.getPackageManager()) != null)
                            {
                                startActivityForResult(picIntent, Utils.REQUEST_SELFIE_PHOTO);
                            }
                            else
                            {
                                Toast.makeText(mActivity, "No camera available", Toast.LENGTH_LONG).show();
                            }
                        }

                    }
                    else
                    if(Utils.siteFlag == 1)
                    {
                        if(picIntent.resolveActivity(mActivity.getPackageManager()) != null)
                        {
                            startActivityForResult(picIntent, Utils.REQUEST_SITE_PHOTO);
                        }
                        else
                        {
                            Toast.makeText(mActivity, "No camera available", Toast.LENGTH_SHORT).show();
                        }
                    }
                    else
                    if(Utils.challanFlag == 1)
                    {
                        if(picIntent.resolveActivity(mActivity.getPackageManager()) != null)
                        {
                            startActivityForResult(picIntent, Utils.REQUEST_CHALLAN_PHOTO);
                        }
                        else
                        {
                            Toast.makeText(mActivity, "No camera available", Toast.LENGTH_SHORT).show();
                        }
                    }
                    else
                    if(Utils.attendanceFlag == 1)
                    {
                        if(picIntent.resolveActivity(mActivity.getPackageManager()) != null)
                        {
                            startActivityForResult(picIntent, Utils.REQUEST_ATTENDANCE_PHOTO);
                        }
                        else
                        {
                            Toast.makeText(mActivity, "No camera available", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
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

        Utils.appEditor.putString("SiteImageDate", selectedDate).commit();

        //setting date to textview
        date.setText(selectedDate);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        Intent i=new Intent(ImageGallery.this, SaveImage.class);
        //Devina edit - for high resolution img
        i.putExtra("imagepath", picUri.toString());

        if(requestCode==Utils.REQUEST_SELFIE_PHOTO && resultCode==Activity.RESULT_OK)
        {
            //Devina edit - for high resolution img
            i.putExtra("Type", "Selfie");
            startActivity(i);
            finish();
        }
        else
        if(requestCode==Utils.REQUEST_SITE_PHOTO && resultCode==Activity.RESULT_OK)
        {
            //Devina edit - for high resolution img
            i.putExtra("Type", "SiteImage");
            startActivity(i);
            finish();
        }
        else
        if(requestCode==Utils.REQUEST_CHALLAN_PHOTO && resultCode==Activity.RESULT_OK)
        {
            //Devina edit - for high resolution img
            i.putExtra("Type", "Challan");
            startActivity(i);
            finish();


        }
        else
        if(requestCode==Utils.REQUEST_ATTENDANCE_PHOTO && resultCode==Activity.RESULT_OK)
        {
            //Devina edit - for high resolution img
            i.putExtra("Type", "Attendance");
            startActivity(i);
            finish();
        }

    }

    @Override
    public void onBackPressed() {

        if(layout2.getVisibility() == View.VISIBLE)
        {
            layout2.setVisibility(View.GONE);
            layout1.setVisibility(View.VISIBLE);
        }
        else
        {
            Utils.pageEditor.putString("LastScreen", "ImageGallery");
            Utils.pageEditor.commit();

            startActivity(new Intent(ImageGallery.this, HomePage.class));
            finish();
        }
    }

    //viewholder class for GalleryAdapter
    static class ViewHolder
    {
        ImageView grid_image;
        TextView grid_date;
    }

    //Adapter class for gridview
    class GalleryAdapter extends BaseAdapter
    {
        Context context;
        LayoutInflater inflater;
        ArrayList<ImageModel> imagelist;

        public GalleryAdapter(Context context, ArrayList<ImageModel> list)
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

                holder.grid_date=(TextView)convertView.findViewById(R.id.grid_date);
                holder.grid_image=(ImageView)convertView.findViewById(R.id.grid_image);

                convertView.setTag(holder);
            }
            else
            {
                holder=(ViewHolder)convertView.getTag();
            }


            //setting timestamp to item
            holder.grid_date.setText(imagelist.get(position).getTimeStamp());


            //setting image to item
            holder.grid_image.setImageBitmap(imagelist.get(position).getImageBitmap());


            //adding action to item click
            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    layout1.setVisibility(View.GONE);
                    layout2.setVisibility(View.VISIBLE);

                    fullImage.setImageBitmap(imagelist.get(position).getImageBitmap());
                }
            });

            return convertView;
        }
    }


    //asynctask for retrieving grooming images
    private class GroomingImagesTask extends AsyncTask<String, Void, Void>
    {
        String content="";
        String error=null;
        String data="";
        String msg="";
        String status="";
        String doc_id="1";
        String emp_id="";
        String imageUrl="", imageTimeStamp="";
        int localRegularInsertSelfieCount=0, localRelieverInsertSelfieCount=0;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            gallery_progress.setVisibility(View.VISIBLE);

            emp_id=Utils.loginPreferences.getString("emp_id", "");
        }


        @Override
        protected Void doInBackground(String... strings) {

            try
            {
                data= URLEncoder.encode("employee_id", "UTF-8") + "="
                        + URLEncoder.encode(emp_id, "UTF-8") + "&"
                        + URLEncoder.encode("doc_id", "UTF-8") + "="
                        + URLEncoder.encode(doc_id, "UTF-8");
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

            if(error != null)
            {
                //retrieving data from regular local db
                if(Utils.loginPreferences.getString("LoginType", "None").equalsIgnoreCase("Regular")
                        && Utils.appPreferences.getString("RegularLocalSelfiePresent", "No").equalsIgnoreCase("Yes"))
                {
                    //loading image list from local database
                    Cursor resultImgList=myRegularDB.getImages(doc_id);

                    if(resultImgList.getCount() == 0)
                    {
                        Toast.makeText(mActivity, "No data found", Toast.LENGTH_LONG).show();
                    }
                    else
                    {
                        while (resultImgList.moveToNext())
                        {
                            //adding data to object
                            ImageModel newImage=new ImageModel();
                            newImage.setImageId(resultImgList.getInt(0));
                            newImage.setImageURL(resultImgList.getString(2));
                            newImage.setTimeStamp(resultImgList.getString(3));

                            try
                            {
                                URL url = new URL(newImage.getImageURL());
                                Bitmap image = BitmapFactory.decodeStream(url.openConnection().getInputStream());

                                //compressing image
                                ByteArrayOutputStream out = new ByteArrayOutputStream();
                                image.compress(Bitmap.CompressFormat.JPEG, 85, out);
                                Bitmap compressedImg= BitmapFactory.decodeStream(new ByteArrayInputStream(out.toByteArray()));

                                newImage.setImageBitmap(compressedImg);
                            }
                            catch (IOException ioe)
                            {
                                ioe.printStackTrace();
                            }

                            //adding object to list
                            imageList.add(newImage);
                        }//while closes

                        status="2"; //when data is successfully retrieved from local database
                    }
                }//data from regular local db
                else
                    //retrieving data from reliever local db
                    if(Utils.loginPreferences.getString("LoginType", "None").equalsIgnoreCase("Reliever")
                            && Utils.appPreferences.getString("RelieverLocalSelfiePresent", "No").equalsIgnoreCase("Yes"))
                    {
                        //loading image list from local database
                        Cursor resultImgList=myRelieverDB.getImages(doc_id);

                        if(resultImgList.getCount() == 0)
                        {
                            Toast.makeText(mActivity, "No data found", Toast.LENGTH_LONG).show();
                        }
                        else
                        {
                            while (resultImgList.moveToNext())
                            {
                                //adding data to object
                                ImageModel newImage=new ImageModel();
                                newImage.setImageId(resultImgList.getInt(0));
                                newImage.setImageURL(resultImgList.getString(2));
                                newImage.setTimeStamp(resultImgList.getString(3));

                                try
                                {
                                    URL url = new URL(newImage.getImageURL());
                                    Bitmap image = BitmapFactory.decodeStream(url.openConnection().getInputStream());

                                    //compressing image
                                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                                    image.compress(Bitmap.CompressFormat.JPEG, 85, out);
                                    Bitmap compressedImg= BitmapFactory.decodeStream(new ByteArrayInputStream(out.toByteArray()));

                                    newImage.setImageBitmap(compressedImg);
                                }
                                catch (IOException ioe)
                                {
                                    ioe.printStackTrace();
                                }

                                //adding object to list
                                imageList.add(newImage);
                            }//while closes

                            status="2"; //when data is successfully retrieved from local database
                        }
                    }//data from reliever local db

                Collections.reverse(imageList);
            }
            else
            {
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
                            newImage.setTimeStamp(extractDate(imageTimeStamp));

                            //adding object to imageList
                            imageList.add(newImage);

                            //adding data to regular local database
                            if(Utils.loginPreferences.getString("LoginType", "None").equalsIgnoreCase("Regular")
                                    && Utils.appPreferences.getString("RegularLocalSelfiePresent", "No").equalsIgnoreCase("No"))
                            {
                                boolean checkInserted=myRegularDB.insertImage(i, doc_id, imageUrl, newImage.getTimeStamp());

                                if(checkInserted)
                                {
                                    localRegularInsertSelfieCount++;
                                }
                            }//adding to regular db closes
                            else
                                //adding data to reliever local database
                                if(Utils.loginPreferences.getString("LoginType", "None").equalsIgnoreCase("Reliever")
                                        && Utils.appPreferences.getString("RelieverLocalSelfiePresent", "No").equalsIgnoreCase("No"))
                                {
                                    boolean checkInserted=myRelieverDB.insertImage(i, doc_id, imageUrl, imageTimeStamp);

                                    if(checkInserted)
                                    {
                                        localRelieverInsertSelfieCount++;
                                    }
                                }//adding to reliever db closes
                        }//for closes
                    }

                }//try closes
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
            }


            return null;
        }


        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);


            //showing error message if error has occurred
            if(error!=null && !status.equalsIgnoreCase("2"))
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

                if(status.equalsIgnoreCase("1"))
                {
                    //reversing the image list to get the latest image first
                    Collections.reverse(imageList);

                    //saving url of latest selfie image
                    if(imageList.size() > 0)
                    {
                        String lastURL=imageList.get(0).getImageURL();
                        Utils.appEditor.putString("LastSelfieURL", lastURL).commit();
                    }


                    //setting up gridview
                    adapter=new GalleryAdapter(context, imageList);
                    gallery_gridview.setAdapter(adapter);

                    //checking whether data is successfully entered into local database
                    if(Utils.loginPreferences.getString("LoginType", "None").equalsIgnoreCase("Regular"))
                    {
                        if(localRegularInsertSelfieCount == imageList.size())
                        {
                            Utils.appEditor.putString("RegularLocalSelfiePresent", "Yes").commit();
                        }
                        else
                        {
                            Utils.appEditor.putString("RegularLocalSelfiePresent", "No").commit();

                            if(Utils.loginPreferences.getString("LoginType", "None").equalsIgnoreCase("Regular"))
                            {
                                myRegularDB.deleteImages(""+doc_id);
                            }

                        }
                    }
                    else
                    if(Utils.loginPreferences.getString("LoginType", "None").equalsIgnoreCase("Reliever"))
                    {
                        if(localRelieverInsertSelfieCount == imageList.size())
                        {
                            Utils.appEditor.putString("RelieverLocalSelfiePresent", "Yes").commit();
                        }
                        else
                        {
                            Utils.appEditor.putString("RelieverLocalSelfiePresent", "No").commit();

                            if(Utils.loginPreferences.getString("LoginType", "None").equalsIgnoreCase("Reliever"))
                            {
                                myRelieverDB.deleteImages(""+doc_id);
                            }

                        }
                    }

                }
                else
                if(status.equalsIgnoreCase("0"))
                {
                    //setting up gridview
                    adapter=new GalleryAdapter(context, imageList);
                    gallery_gridview.setAdapter(adapter);

                    Toast.makeText(mActivity, msg, Toast.LENGTH_LONG).show();
                }
                else
                    //when data is retrieved from local databse
                    if(status.equalsIgnoreCase("2"))
                    {
                        //setting up gridview
                        adapter=new GalleryAdapter(context, imageList);
                        gallery_gridview.setAdapter(adapter);
                    }
            }

            gallery_progress.setVisibility(View.GONE);
        }

        //method to extract date from timestamp
        public String extractDate(String timeStamp)
        {
            String date="";

            int indexSpace=timeStamp.indexOf(" ");
            String reverseDate=timeStamp.substring(0, indexSpace);

            int index1=reverseDate.indexOf("-");
            int index2=reverseDate.indexOf("-", index1+1);
            String year=reverseDate.substring(0, index1);
            String month=reverseDate.substring(index1+1, index2);
            String day=reverseDate.substring(index2+1, reverseDate.length());

            date=day+"-"+month+"-"+year;

            return date;
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

            gallery_progress.setVisibility(View.VISIBLE);

            site_id=Utils.loginPreferences.getString("site_id", "");
            date=Utils.appPreferences.getString("SiteImageDate", "");
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

                if (status.equalsIgnoreCase("1")) {
                    //reversing the image list to get the latest image first
                    Collections.reverse(imageList);


                    //setting up gridview
                    adapter = new GalleryAdapter(context, imageList);
                    gallery_gridview.setAdapter(adapter);
                }
                else
                if (status.equalsIgnoreCase("0"))
                {
                    //setting up gridview
                    adapter = new GalleryAdapter(context, imageList);
                    gallery_gridview.setAdapter(adapter);

                    Toast.makeText(mActivity, msg, Toast.LENGTH_LONG).show();

                }
            }

            gallery_progress.setVisibility(View.GONE);
        }


        public String extractTime(String timeStamp)
        {
            int spaceindex=timeStamp.indexOf(' ');
            int colonindex=timeStamp.lastIndexOf(':');

            String time=timeStamp.substring(spaceindex, colonindex);

            return time;
        }
    }//asynctask closes


    //asynctask for retrieving challan images
    private class ChallanImagesTask extends AsyncTask<String, Void, Void>
    {
        String content="";
        String error=null;
        String data="";
        String msg="";
        String status="";
        String doc_id="2";
        String site_id="";
        String imageUrl="", imageTimeStamp="";
        int localRegularChallanCount=0, localRelieverChallanCount=0;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            gallery_progress.setVisibility(View.VISIBLE);

            site_id=Utils.loginPreferences.getString("site_id", "");
        }


        @Override
        protected Void doInBackground(String... strings) {

            try
            {
                data= URLEncoder.encode("site_id", "UTF-8") + "="
                        + URLEncoder.encode(site_id, "UTF-8") + "&"
                        + URLEncoder.encode("doc_id", "UTF-8") + "="
                        + URLEncoder.encode(doc_id, "UTF-8");
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

            if(error != null)
            {
                //retrieving data from regular local db
                if(Utils.loginPreferences.getString("LoginType", "None").equalsIgnoreCase("Regular")
                        && Utils.appPreferences.getString("RegularLocalChallanPresent", "No").equalsIgnoreCase("Yes"))
                {
                    //loading image list from local database
                    Cursor resultImgList=myRegularDB.getImages(doc_id);

                    if(resultImgList.getCount() == 0)
                    {
                        Toast.makeText(mActivity, "No data found", Toast.LENGTH_LONG).show();
                    }
                    else
                    {
                        while (resultImgList.moveToNext())
                        {
                            //adding data to object
                            ImageModel newImage=new ImageModel();
                            newImage.setImageId(resultImgList.getInt(0));
                            newImage.setImageURL(resultImgList.getString(2));
                            newImage.setTimeStamp(resultImgList.getString(3));

                            try
                            {
                                URL url = new URL(newImage.getImageURL());
                                Bitmap image = BitmapFactory.decodeStream(url.openConnection().getInputStream());

                                //compressing image
                                ByteArrayOutputStream out = new ByteArrayOutputStream();
                                image.compress(Bitmap.CompressFormat.JPEG, 85, out);
                                Bitmap compressedImg= BitmapFactory.decodeStream(new ByteArrayInputStream(out.toByteArray()));

                                newImage.setImageBitmap(compressedImg);
                            }
                            catch (IOException ioe)
                            {
                                ioe.printStackTrace();
                            }

                            //adding object to list
                            imageList.add(newImage);
                        }//while closes

                        status="2"; //when data is successfully retrieved from local database
                    }
                }//data from regular local db
                else
                    //retrieving data from reliever local db
                    if(Utils.loginPreferences.getString("LoginType", "None").equalsIgnoreCase("Reliever")
                            && Utils.appPreferences.getString("RelieverLocalChallanPresent", "No").equalsIgnoreCase("Yes"))
                    {
                        //loading image list from local database
                        Cursor resultImgList=myRelieverDB.getImages(doc_id);

                        if(resultImgList.getCount() == 0)
                        {
                            Toast.makeText(mActivity, "No data found", Toast.LENGTH_LONG).show();
                        }
                        else
                        {
                            while (resultImgList.moveToNext())
                            {
                                //adding data to object
                                ImageModel newImage=new ImageModel();
                                newImage.setImageId(resultImgList.getInt(0));
                                newImage.setImageURL(resultImgList.getString(2));
                                newImage.setTimeStamp(resultImgList.getString(3));

                                try
                                {
                                    URL url = new URL(newImage.getImageURL());
                                    Bitmap image = BitmapFactory.decodeStream(url.openConnection().getInputStream());

                                    //compressing image
                                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                                    image.compress(Bitmap.CompressFormat.JPEG, 85, out);
                                    Bitmap compressedImg= BitmapFactory.decodeStream(new ByteArrayInputStream(out.toByteArray()));

                                    newImage.setImageBitmap(compressedImg);
                                }
                                catch (IOException ioe)
                                {
                                    ioe.printStackTrace();
                                }

                                //adding object to list
                                imageList.add(newImage);
                            }//while closes

                            status="2"; //when data is successfully retrieved from local database
                        }
                    }//data from reliever local db

                Collections.reverse(imageList);

            }
            else
            {
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
                            newImage.setTimeStamp(extractDate(imageTimeStamp));

                            //adding object to imageList
                            imageList.add(newImage);

                            //adding data to regular local database
                            if(Utils.loginPreferences.getString("LoginType", "None").equalsIgnoreCase("Regular")
                                    && Utils.appPreferences.getString("RegularLocalChallanPresent", "No").equalsIgnoreCase("No"))
                            {
                                boolean checkInserted=myRegularDB.insertImage(i, doc_id, imageUrl, newImage.getTimeStamp());

                                if(checkInserted)
                                {
                                    localRegularChallanCount++;
                                }
                            }//adding to regular db closes
                            else
                                //adding data to reliever local database
                                if(Utils.loginPreferences.getString("LoginType", "None").equalsIgnoreCase("Reliever")
                                        && Utils.appPreferences.getString("RelieverLocalChallanPresent", "No").equalsIgnoreCase("No"))
                                {
                                    boolean checkInserted=myRelieverDB.insertImage(i, doc_id, imageUrl, imageTimeStamp);

                                    if(checkInserted)
                                    {
                                        localRelieverChallanCount++;
                                    }
                                }//adding to reliever db closes
                        }//for closes
                    }

                }//try closes
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
            }


            return null;
        }


        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);


            //showing error message if error has occurred
            if(error!=null && !status.equalsIgnoreCase("2"))
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

                if(status.equalsIgnoreCase("1"))
                {
                    //reversing the image list to get the latest image first
                    Collections.reverse(imageList);

                    //saving url of latest challan image
                    if(imageList.size() > 0)
                    {
                        String lastURL=imageList.get(0).getImageURL();
                        Utils.appEditor.putString("LastChallanURL", lastURL).commit();
                    }


                    //setting up gridview
                    adapter=new GalleryAdapter(context, imageList);
                    gallery_gridview.setAdapter(adapter);

                    //checking whether data is successfully entered into local database
                    if(Utils.loginPreferences.getString("LoginType", "None").equalsIgnoreCase("Regular"))
                    {
                        if(localRegularChallanCount== imageList.size())
                        {
                            Utils.appEditor.putString("RegularLocalChallanPresent", "Yes").commit();
                        }
                        else
                        {
                            Utils.appEditor.putString("RegularLocalChallanPresent", "No").commit();

                            if(Utils.loginPreferences.getString("LoginType", "None").equalsIgnoreCase("Regular"))
                            {
                                myRegularDB.deleteImages(""+doc_id);
                            }

                        }
                    }
                    else
                    if(Utils.loginPreferences.getString("LoginType", "None").equalsIgnoreCase("Reliever"))
                    {
                        if(localRelieverChallanCount== imageList.size())
                        {
                            Utils.appEditor.putString("RelieverLocalChallanPresent", "Yes").commit();
                        }
                        else
                        {
                            Utils.appEditor.putString("RelieverLocalChallanPresent", "No").commit();

                            if(Utils.loginPreferences.getString("LoginType", "None").equalsIgnoreCase("Reliever"))
                            {
                                myRelieverDB.deleteImages("" + doc_id);
                            }

                        }
                    }
                }
                else
                if(status.equalsIgnoreCase("0"))
                {
                    //setting up gridview
                    adapter=new GalleryAdapter(context, imageList);
                    gallery_gridview.setAdapter(adapter);

                    Toast.makeText(mActivity, msg, Toast.LENGTH_LONG).show();
                }
                else
                    //when data is successfully retrieved from local db
                    if(status.equalsIgnoreCase("2"))
                    {
                        //setting up gridview
                        adapter=new GalleryAdapter(context, imageList);
                        gallery_gridview.setAdapter(adapter);
                    }
            }//outer else closes

            gallery_progress.setVisibility(View.GONE);
        }

        //method to extract date from timestamp
        public String extractDate(String timeStamp)
        {
            String date="";

            int indexSpace=timeStamp.indexOf(" ");
            String reverseDate=timeStamp.substring(0, indexSpace);

            int index1=reverseDate.indexOf("-");
            int index2=reverseDate.indexOf("-", index1+1);
            String year=reverseDate.substring(0, index1);
            String month=reverseDate.substring(index1+1, index2);
            String day=reverseDate.substring(index2+1, reverseDate.length());

            date=day+"-"+month+"-"+year;

            return date;
        }
    }//asynctask closes


    //asynctask for retrieving attendance images
    private class AttendanceImagesTask extends AsyncTask<String, Void, Void>
    {
        String content="";
        String error=null;
        String data="";
        String msg="";
        String status="";
        String doc_id="4";
        String site_id="";
        String imageUrl="", imageTimeStamp="";
        int localRegularInsertAttendCount=0, localRelieverInsertAttendCount=0;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            gallery_progress.setVisibility(View.VISIBLE);

            site_id=Utils.loginPreferences.getString("site_id", "");
        }


        @Override
        protected Void doInBackground(String... strings) {

            try
            {
                data= URLEncoder.encode("site_id", "UTF-8") + "="
                        + URLEncoder.encode(site_id, "UTF-8") + "&"
                        + URLEncoder.encode("doc_id", "UTF-8") + "="
                        + URLEncoder.encode(doc_id, "UTF-8");
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

            if(error != null)
            {
                //retrieving data from regular local db
                if(Utils.loginPreferences.getString("LoginType", "None").equalsIgnoreCase("Regular")
                        && Utils.appPreferences.getString("RegularLocalAttendancePresent", "No").equalsIgnoreCase("Yes"))
                {
                    //loading image list from local database
                    Cursor resultImgList=myRegularDB.getImages(doc_id);

                    if(resultImgList.getCount() == 0)
                    {
                        Toast.makeText(mActivity, "No data found", Toast.LENGTH_LONG).show();
                    }
                    else
                    {
                        while (resultImgList.moveToNext())
                        {
                            //adding data to object
                            ImageModel newImage=new ImageModel();
                            newImage.setImageId(resultImgList.getInt(0));
                            newImage.setImageURL(resultImgList.getString(2));
                            newImage.setTimeStamp(resultImgList.getString(3));

                            try
                            {
                                URL url = new URL(newImage.getImageURL());
                                Bitmap image = BitmapFactory.decodeStream(url.openConnection().getInputStream());

                                //compressing image
                                ByteArrayOutputStream out = new ByteArrayOutputStream();
                                image.compress(Bitmap.CompressFormat.JPEG, 85, out);
                                Bitmap compressedImg= BitmapFactory.decodeStream(new ByteArrayInputStream(out.toByteArray()));

                                newImage.setImageBitmap(compressedImg);
                            }
                            catch (IOException ioe)
                            {
                                ioe.printStackTrace();
                            }

                            //adding object to list
                            imageList.add(newImage);
                        }//while closes

                        status="2"; //when data is successfully retrieved from local database
                    }
                }//data from regular local db
                else
                    //retrieving data from reliever local db
                    if(Utils.loginPreferences.getString("LoginType", "None").equalsIgnoreCase("Reliever")
                            && Utils.appPreferences.getString("RelieverLocalAttendancePresent", "No").equalsIgnoreCase("Yes"))
                    {
                        //loading image list from local database
                        Cursor resultImgList=myRelieverDB.getImages(doc_id);

                        if(resultImgList.getCount() == 0)
                        {
                            Toast.makeText(mActivity, "No data found", Toast.LENGTH_LONG).show();
                        }
                        else
                        {
                            while (resultImgList.moveToNext())
                            {
                                //adding data to object
                                ImageModel newImage=new ImageModel();
                                newImage.setImageId(resultImgList.getInt(0));
                                newImage.setImageURL(resultImgList.getString(2));
                                newImage.setTimeStamp(resultImgList.getString(3));

                                try
                                {
                                    URL url = new URL(newImage.getImageURL());
                                    Bitmap image = BitmapFactory.decodeStream(url.openConnection().getInputStream());

                                    //compressing image
                                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                                    image.compress(Bitmap.CompressFormat.JPEG, 85, out);
                                    Bitmap compressedImg= BitmapFactory.decodeStream(new ByteArrayInputStream(out.toByteArray()));

                                    newImage.setImageBitmap(compressedImg);
                                }
                                catch (IOException ioe)
                                {
                                    ioe.printStackTrace();
                                }

                                //adding object to list
                                imageList.add(newImage);
                            }//while closes

                            status="2"; //when data is successfully retrieved from local database
                        }
                    }//data from reliever local db

                Collections.reverse(imageList);

            }
            else
            {
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
                            newImage.setTimeStamp(extractDate(imageTimeStamp));

                            //adding object to imageList
                            imageList.add(newImage);

                            //adding data to regular local database
                            if(Utils.loginPreferences.getString("LoginType", "None").equalsIgnoreCase("Regular")
                                    && Utils.appPreferences.getString("RegularLocalAttendancePresent", "No").equalsIgnoreCase("No"))
                            {
                                boolean checkInserted=myRegularDB.insertImage(i, doc_id, imageUrl, newImage.getTimeStamp());

                                if(checkInserted)
                                {
                                    localRegularInsertAttendCount++;
                                }
                            }//adding to regular db closes
                            else
                                //adding data to reliever local database
                                if(Utils.loginPreferences.getString("LoginType", "None").equalsIgnoreCase("Reliever")
                                        && Utils.appPreferences.getString("RelieverLocalAttendancePresent", "No").equalsIgnoreCase("No"))
                                {
                                    boolean checkInserted=myRelieverDB.insertImage(i, doc_id, imageUrl, imageTimeStamp);

                                    if(checkInserted)
                                    {
                                        localRelieverInsertAttendCount++;
                                    }
                                }//adding to reliever db closes
                        }//for closes
                    }

                }//try closes
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
            }


            return null;
        }


        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);


            //showing error message if error has occurred
            if(error!=null && !status.equalsIgnoreCase("2"))
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

                if(status.equalsIgnoreCase("1"))
                {
                    //reversing the image list to get the latest image first
                    Collections.reverse(imageList);

                    //saving url of latest attendance sheet image
                    if(imageList.size() > 0)
                    {
                        String lastURL=imageList.get(0).getImageURL();
                        Utils.appEditor.putString("LastAttendanceURL", lastURL).commit();
                    }

                    //setting up gridview
                    adapter=new GalleryAdapter(context, imageList);
                    gallery_gridview.setAdapter(adapter);

                    //checking whether data is successfully entered into local database
                    if(Utils.loginPreferences.getString("LoginType", "None").equalsIgnoreCase("Regular"))
                    {
                        if(localRegularInsertAttendCount== imageList.size())
                        {
                            Utils.appEditor.putString("RegularLocalAttendancePresent", "Yes").commit();
                        }
                        else
                        {
                            Utils.appEditor.putString("RegularLocalAttendancePresent", "No").commit();

                            if(Utils.loginPreferences.getString("LoginType", "None").equalsIgnoreCase("Regular"))
                            {
                                myRegularDB.deleteImages("" + doc_id);
                            }

                        }
                    }
                    else
                    if(Utils.loginPreferences.getString("LoginType", "None").equalsIgnoreCase("Reliever"))
                    {
                        if(localRelieverInsertAttendCount== imageList.size())
                        {
                            Utils.appEditor.putString("RelieverLocalAttendancePresent", "Yes").commit();
                        }
                        else
                        {
                            Utils.appEditor.putString("RelieverLocalAttendancePresent", "No").commit();

                            if(Utils.loginPreferences.getString("LoginType", "None").equalsIgnoreCase("Reliever"))
                            {
                                myRelieverDB.deleteImages("" + doc_id);
                            }

                        }
                    }
                }
                else
                if(status.equalsIgnoreCase("0"))
                {
                    //setting up gridview
                    adapter=new GalleryAdapter(context, imageList);
                    gallery_gridview.setAdapter(adapter);

                    Toast.makeText(mActivity, msg, Toast.LENGTH_LONG).show();
                }
                else
                    //when data is successfully retrieved from local db
                    if(status.equalsIgnoreCase("2"))
                    {
                        //setting up gridview
                        adapter=new GalleryAdapter(context, imageList);
                        gallery_gridview.setAdapter(adapter);
                    }
            }//outer else closes

            gallery_progress.setVisibility(View.GONE);
        }

        //method to extract date from timestamp
        public String extractDate(String timeStamp)
        {
            String timestamp="";
            int mm=0;

            int indexSpace=timeStamp.indexOf(" ");
            String reverseDate=timeStamp.substring(0, indexSpace);

            int index1=reverseDate.indexOf("-");
            int index2=reverseDate.indexOf("-", index1+1);
            String year=reverseDate.substring(0, index1);
            String month=reverseDate.substring(index1+1, index2);

            try
            {
                mm=Integer.parseInt(month);
            }
            catch (NumberFormatException nfe)
            {
                nfe.printStackTrace();
            }

            switch (mm)
            {
                case 1:
                    timestamp="January "+year;
                    break;
                case 2:
                    timestamp="February "+year;
                    break;
                case 3:
                    timestamp="March "+year;
                    break;
                case 4:
                    timestamp="April "+year;
                    break;
                case 5:
                    timestamp="May "+year;
                    break;
                case 6:
                    timestamp="June "+year;
                    break;
                case 7:
                    timestamp="July "+year;
                    break;
                case 8:
                    timestamp="August "+year;
                    break;
                case 9:
                    timestamp="September "+year;
                    break;
                case 10:
                    timestamp="October "+year;
                    break;
                case 11:
                    timestamp="November "+year;
                    break;
                case 12:
                    timestamp="December "+year;
                    break;
                default:
                    timestamp="Month";
                    break;
            }

            return timestamp;
        }
    }//asynctask closes


}
