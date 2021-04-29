package com.demo.ccaspltrial;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.demo.ccaspltrial.Utility.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class SaveImage extends AppCompatActivity implements LocationListener{

    Button save, clickAnother;
    ImageView showImage;
    Bitmap image;
    ProgressBar saveimg_progress;
    String type;
    Toolbar toolbar;
    Context context;
    ProgressDialog pDialog;


    private LocationManager locationManager;
    Location location;
    double longitude=0.0, latitude=0.0;

    //Devina edit - for high resolution img
    ContentValues values;
    Uri picUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_save_image);

        save=(Button)findViewById(R.id.save);
        clickAnother=(Button)findViewById(R.id.clickPic);
        showImage=(ImageView)findViewById(R.id.showImage);
        toolbar=(Toolbar)findViewById(R.id.toolbar);
        saveimg_progress=(ProgressBar)findViewById(R.id.saveimg_progress);

        setSupportActionBar(toolbar);

        context=this;

        //initialising sharedpreferences
        Utils.loginPreferences=getSharedPreferences(Utils.loginPref, MODE_PRIVATE);
        Utils.pagePreferences=getSharedPreferences(Utils.pagePref, MODE_PRIVATE);
        Utils.pageEditor=Utils.pagePreferences.edit();


        //setting title on toolbar
        if(Utils.selfieFlag==1)
        {
            toolbar.setTitle("Selfie Preview");
        }
        else
        if(Utils.siteFlag==1)
        {
            toolbar.setTitle("Site Image Preview");

            Utils.pageEditor.putString("LastScreen", "SiteImagePreview").commit();
        }
        else
        if(Utils.challanFlag==1)
        {
            toolbar.setTitle("Delivery Challan Preview");
        }
        else
        if(Utils.attendanceFlag==1)
        {
            toolbar.setTitle("Attendance Sheet Preview");
        }

        //getting image
        Intent i=getIntent();
        Bundle b=i.getExtras();

        //getting type of image
        type=b.getString("Type");

        //Devina edit - for high resolution img
        if(!i.getStringExtra("imagepath").equalsIgnoreCase(""))
        {
            Uri fileUri = Uri.parse(i.getStringExtra("imagepath"));

            try {
                image = MediaStore.Images.Media.getBitmap(this.getContentResolver(), fileUri);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        //Devina edit - for high resolution img ENDS


        //setting received image to imageview
        showImage.setImageBitmap(image);

        Intent intent=new Intent("android.intent.action.LOCATION+PUNCH");
        context.sendBroadcast(intent);

        //loading location
        loadlocation();

        //adding action to click another button to click another photo
        clickAnother.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

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

                if(picIntent.resolveActivity(getPackageManager()) != null)
                {
                    if(type.equalsIgnoreCase("Selfie"))
                    {
                        startActivityForResult(picIntent, Utils.REQUEST_SELFIE_PHOTO);
                    }
                    else
                    if(type.equalsIgnoreCase("SiteImage"))
                    {
                        startActivityForResult(picIntent, Utils.REQUEST_SITE_PHOTO);
                    }
                    else
                    if(type.equalsIgnoreCase("Challan"))
                    {
                        startActivityForResult(picIntent, Utils.REQUEST_CHALLAN_PHOTO);
                    }
                    else
                    if(type.equalsIgnoreCase("Attendance"))
                    {
                        startActivityForResult(picIntent, Utils.REQUEST_ATTENDANCE_PHOTO);
                    }
                }
                else
                {
                    Toast.makeText(SaveImage.this, "No camera available", Toast.LENGTH_SHORT).show();
                }
            }
        });

        //adding action to save button to call method to save image
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                saveImage();
            }
        });

    }


    //method to load location services
    private void loadlocation()
    {
        if((ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
                && (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED))
        {
            Toast.makeText(context, "Please grant location permissions", Toast.LENGTH_LONG).show();

            return;
        }

        locationManager=(LocationManager)getSystemService(Context.LOCATION_SERVICE);
        location=locationManager.getLastKnownLocation(locationManager.NETWORK_PROVIDER);
    }



    //method to save image
    public void saveImage()
    {
        //checking if permissions to access storage are granted
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
        {
            Utils.requestRequiredPermissions(this, this);
            return;
        }
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
        {
            Utils.requestRequiredPermissions(this, this);
            return;
        }

        String fileName="";

        //creating main CCASPL directory if it doesn't exist
        //making app's main storage in Pictures directory of external storage
        File f=new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)+"/CCASPL");
        if(!f.exists())
        {
            f.mkdir();
        }

        //creating unique file name using datetime stamp
        DateFormat dateFormat=new SimpleDateFormat("yyyyMMdd_HHmmss");
        Date currentDate=new Date();
        String timeStamp=dateFormat.format(currentDate);

        if(type.equalsIgnoreCase("Selfie"))
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
            {
                //getting location
                onLocationChanged(location);

                //checking if latitude and longitude is 0
                if(latitude == 0 && longitude == 0)
                    return;

                //creating subdirectory if it doesn't exist
                File file=new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)+"/CCASPL/Selfie");
                if(!file.exists())
                {
                    file.mkdir();
                }

                //unique file name
                fileName="CCASPL/Selfie/Selfie_"+timeStamp+".jpg";

                //getting image path
                String imagePath=getImagePath(image);

                //uploading image to database
                if(!Utils.loginPreferences.getString("emp_id", "").equalsIgnoreCase("")
                        && !Utils.loginPreferences.getString("site_id", "").equalsIgnoreCase("")
                        && !imagePath.equalsIgnoreCase("") && (longitude != 0 || latitude != 0)
                        && Utils.isNetworkAvailable(context))
                {
                    new ImageUploadTask().execute(Utils.imageUploadURL, imagePath, "1");

                }
                else
                if(Utils.loginPreferences.getString("site_id", "").equalsIgnoreCase(""))
                {
                    Toast.makeText(context, "No site is assigned", Toast.LENGTH_LONG).show();
                }
                else
                if(!Utils.isNetworkAvailable(context))
                {
                    Toast.makeText(context, "No internet", Toast.LENGTH_SHORT).show();
                }
            }

        }
        else
        if(type.equalsIgnoreCase("SiteImage"))
        {
            //creating subdirectory if it doesn't exist
            File file=new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)+"/CCASPL/Site_Image");
            if(!file.exists())
            {
                file.mkdir();
            }

            //unique file name
            fileName="CCASPL/Site_Image/SiteImage_"+timeStamp+".jpg";

            //getting image path
            String imagePath=getImagePath(image);

            //uploading image to database
            if(!Utils.loginPreferences.getString("emp_id", "").equalsIgnoreCase("")
                    && !Utils.loginPreferences.getString("site_id", "").equalsIgnoreCase("")
                    && !imagePath.equalsIgnoreCase("")
                    && Utils.isNetworkAvailable(context))
            {
                new ImageUploadTask().execute(Utils.imageUploadURL, imagePath, "3");
            }
            else
            if(Utils.loginPreferences.getString("site_id", "").equalsIgnoreCase(""))
            {
                Toast.makeText(context, "No site is assigned", Toast.LENGTH_LONG).show();
            }
            else
            if(!Utils.isNetworkAvailable(context))
            {
                Toast.makeText(context, "No internet", Toast.LENGTH_SHORT).show();
            }
        }
        else
        if(type.equalsIgnoreCase("Challan"))
        {
            //creating subdirectory if it doesn't exist
            File file=new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)+"/CCASPL/Challan");
            if(!file.exists())
            {
                file.mkdir();
            }

            //unique file name
            fileName="CCASPL/Challan/Challan_"+timeStamp+".jpg";

            //getting image path
            String imagePath=getImagePath(image);

            //uploading image to database
            if(!Utils.loginPreferences.getString("emp_id", "").equalsIgnoreCase("")
                    && !Utils.loginPreferences.getString("site_id", "").equalsIgnoreCase("")
                    && !imagePath.equalsIgnoreCase("")
                    && Utils.isNetworkAvailable(context))
            {
                new ImageUploadTask().execute(Utils.imageUploadURL, imagePath, "2");
            }
            else
            if(Utils.loginPreferences.getString("site_id", "").equalsIgnoreCase(""))
            {
                Toast.makeText(context, "No site is assigned", Toast.LENGTH_LONG).show();
            }
            else
            if(!Utils.isNetworkAvailable(context))
            {
                Toast.makeText(context, "No internet", Toast.LENGTH_SHORT).show();
            }

        }
        else
        if(type.equalsIgnoreCase("Attendance"))
        {
            //creating subdirectory if it doesn't exist
            File file=new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)+"/CCASPL/Attendance");
            if(!file.exists())
            {
                file.mkdir();
            }

            //unique file name
            fileName="CCASPL/Attendance/Attendance_"+timeStamp+".jpg";

            //getting image path
            String imagePath=getImagePath(image);

            //uploading image to database
            if(!Utils.loginPreferences.getString("emp_id", "").equalsIgnoreCase("")
                    && !Utils.loginPreferences.getString("site_id", "").equalsIgnoreCase("")
                    && !imagePath.equalsIgnoreCase("")
                    && Utils.isNetworkAvailable(context))
            {
                new ImageUploadTask().execute(Utils.imageUploadURL, imagePath, "4");
            }
            else
            if(Utils.loginPreferences.getString("site_id", "").equalsIgnoreCase(""))
            {
                Toast.makeText(context, "No site is assigned", Toast.LENGTH_LONG).show();
            }
            else
            if(!Utils.isNetworkAvailable(context))
            {
                Toast.makeText(context, "No internet", Toast.LENGTH_SHORT).show();
            }

        }

        //checking whether fileName is created
        if(!fileName.equalsIgnoreCase(""))
        {
            //creating file
            File imageFile=new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), fileName);

            //storing file
            try
            {
                FileOutputStream fos=new FileOutputStream(imageFile);
                image.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                fos.flush();
                fos.close();
                Toast.makeText(this, "Saved", Toast.LENGTH_SHORT).show();
            }
            catch (FileNotFoundException e)
            {
//                Toast.makeText(this, "FileNotFoundException", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
            catch (IOException e)
            {
//                Toast.makeText(this, "IOException", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }



    }

    //method to get encoded image path
    public String getImagePath(Bitmap imageBitmap)
    {
        ByteArrayOutputStream bytes=new ByteArrayOutputStream();
        imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);

        byte []byteArray=bytes.toByteArray();

        String imagePath= Base64.encodeToString(byteArray, Base64.DEFAULT);

        return imagePath;
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        //Devina edit - for high resolution img
        try
        {
            image = MediaStore.Images.Media.getBitmap(this.getContentResolver(), picUri);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        if(requestCode==Utils.REQUEST_SELFIE_PHOTO && resultCode==RESULT_OK)
        {
            //Devina edit - for high resolution img
            type="Selfie";

            showImage.setImageBitmap(image);
        }
        else
        if(requestCode==Utils.REQUEST_SITE_PHOTO && resultCode==RESULT_OK)
        {
            //Devina edit - for high resolution img
            type="SiteImage";

            showImage.setImageBitmap(image);
        }
        else
        if(requestCode==Utils.REQUEST_CHALLAN_PHOTO && resultCode==RESULT_OK)
        {
            //Devina edit - for high resolution img
            type="Challan";

            showImage.setImageBitmap(image);
        }
        else
        if(requestCode==Utils.REQUEST_ATTENDANCE_PHOTO && resultCode==RESULT_OK)
        {
            //Devina edit - for high resolution img
            type="Attendance";

            showImage.setImageBitmap(image);
        }

    }


    @Override
    public void onBackPressed() {

        startActivity(new Intent(SaveImage.this, ImageGallery.class));
        finish();
    }

    @Override
    public void onLocationChanged(Location location) {

        if(location != null)
        {
            latitude=location.getLatitude();
            longitude=location.getLongitude();
        }
        else
        {
            Toast.makeText(context, "Error in tracking location...please try again later", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }


    //asynctask class to upload image to database
    private class ImageUploadTask extends AsyncTask<String, Void, Void>
    {
        String content="";
        String error=null;
        String data="";
        String msg="";
        String status="";
        String emp_id="";
        String site_id="";

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            saveimg_progress.setVisibility(View.VISIBLE);

            emp_id=Utils.loginPreferences.getString("emp_id", "");
            site_id=Utils.loginPreferences.getString("site_id", "");
        }


        @Override
        protected Void doInBackground(String... strings) {

            try
            {
                data= URLEncoder.encode("employee_id", "UTF-8") + "="
                        + URLEncoder.encode(emp_id, "UTF-8") + "&"
                        + URLEncoder.encode("url", "UTF-8") + "="
                        + URLEncoder.encode(strings[1], "UTF-8") + "&"
                        + URLEncoder.encode("doc_type_id", "UTF-8") + "="
                        + URLEncoder.encode(strings[2], "UTF-8") + "&"
                        + URLEncoder.encode("site_id", "UTF-8") + "="
                        + URLEncoder.encode(site_id, "UTF-8");

                //sending latitude and longitude for selfie
                if(Utils.appPreferences.getString("ImageType", "").equalsIgnoreCase("Selfie"))
                {
                    data+="&" + URLEncoder.encode("latitude", "UTF-8") + "="
                            + URLEncoder.encode(""+latitude, "UTF-8") + "&"
                            + URLEncoder.encode("longitude", "UTF-8") + "="
                            + URLEncoder.encode(""+longitude, "UTF-8");
                }
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
                try
                {
                    JSONObject jsonResponse=new JSONObject(content);
                    status=jsonResponse.getString("status");
                    msg=jsonResponse.getString("msg");
                }
                catch (JSONException je)
                {
                    je.printStackTrace();
                }

                //checking if image upload is successful
                if(status.equalsIgnoreCase("1"))
                {
                    Toast.makeText(context, "Image uploaded", Toast.LENGTH_LONG).show();
                }
                else
                if(status.equalsIgnoreCase("0"))
                {
                    Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
                }
            }//outer else closes

            saveimg_progress.setVisibility(View.GONE);

        }
    }//asynctask closes
}
