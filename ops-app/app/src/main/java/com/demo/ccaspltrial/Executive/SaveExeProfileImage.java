package com.demo.ccaspltrial.Executive;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.demo.ccaspltrial.R;
import com.demo.ccaspltrial.SaveImage;
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

public class SaveExeProfileImage extends AppCompatActivity {

    Button save, clickAnother;
    ImageView showImage;
    Bitmap image;
    ProgressBar saveimg_progress;
    String type;
    Toolbar toolbar;
    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_save_exe_profile_image);

        save=(Button)findViewById(R.id.exe_save);
        clickAnother=(Button)findViewById(R.id.exe_clickPic);
        showImage=(ImageView)findViewById(R.id.exe_showImage);
        toolbar=(Toolbar)findViewById(R.id.toolbar);
        saveimg_progress=(ProgressBar)findViewById(R.id.exe_saveimg_progress);

        setSupportActionBar(toolbar);

        context=getApplicationContext();

        //initialising sharedpreferences
        Utils.loginPreferences=getSharedPreferences(Utils.loginPref, MODE_PRIVATE);
        Utils.loginEditor=Utils.loginPreferences.edit();

        //setting title on toolbar
        toolbar.setTitle("Profile Image Preview");

        //getting image
        Intent i=getIntent();
        Bundle b=i.getExtras();
        image=(Bitmap)b.get("data");

        //setting received image to imageview
        showImage.setImageBitmap(image);

        //adding action to click another button to click another photo
        clickAnother.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //call method to click another image
                clickAnotherImage();
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

    //method to get encoded image path
    public String getImagePath(Bitmap imageBitmap)
    {
        ByteArrayOutputStream bytes=new ByteArrayOutputStream();
        imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);

        byte []byteArray=bytes.toByteArray();

        String imagePath= Base64.encodeToString(byteArray, Base64.DEFAULT);

        return imagePath;
    }//getImagePath closes

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


        //creating subdirectory if it doesn't exist
        File file=new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)+"/CCASPL/Executive_Image");
        if(!file.exists())
        {
            file.mkdir();
        }

        //unique file name
        String executive_id=Utils.loginPreferences.getString("executive_id", "");
        fileName="CCASPL/Executive_Image/Executive_"+executive_id+timeStamp+".jpg";

        //getting image path
        String imagePath=getImagePath(image);

        //uploading image to database
        if(!executive_id.equalsIgnoreCase("") && !imagePath.equalsIgnoreCase("")
                && Utils.isNetworkAvailable(context))
        {
            new ExeProfileImgUploadTask().execute(Utils.exeprofileimguploadURL, imagePath);
        }
        else
        if(!Utils.isNetworkAvailable(context))
        {
            Toast.makeText(context, "No internet", Toast.LENGTH_SHORT).show();
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

    }//saveImage closes

    //method to click another image
    public void clickAnotherImage()
    {
        Intent picIntent=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        if(picIntent.resolveActivity(getPackageManager()) != null)
        {
            startActivityForResult(picIntent, Utils.REQUEST_EXEPROFILE_PHOTO);
        }
        else
        {
            Toast.makeText(context, "No camera available", Toast.LENGTH_SHORT).show();
        }
    }//clickAnotherImage closes

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        if(requestCode==Utils.REQUEST_EXEPROFILE_PHOTO && resultCode==RESULT_OK)
        {
            Bundle extras=data.getExtras();
//            type="ExecutiveProfileImg";
            image=(Bitmap)extras.get("data");
            showImage.setImageBitmap(image);
        }
    }

    @Override
    public void onBackPressed() {

        startActivity(new Intent(SaveExeProfileImage.this, ExecutiveProfile.class));
        finish();
    }

    //asynctask to upload executive profile image
    private class ExeProfileImgUploadTask extends AsyncTask<String, Void, Void>
    {
        String content="";
        String error=null;
        String data="";
        String msg="";
        String status="";
        String exe_id="";

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            saveimg_progress.setVisibility(View.VISIBLE);

            exe_id=Utils.loginPreferences.getString("executive_id", "");
        }

        @Override
        protected Void doInBackground(String... strings) {

            try
            {
                data= URLEncoder.encode("executive_id", "UTF-8") + "="
                        + URLEncoder.encode(exe_id, "UTF-8") + "&"
                        + URLEncoder.encode("url", "UTF-8") + "="
                        + URLEncoder.encode(strings[1], "UTF-8");
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

            String exeImgUrl="";

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

                    if(status.equalsIgnoreCase("1"))
                    {
                        exeImgUrl=jsonResponse.getString("image_url");
                    }
                }
                catch (JSONException je)
                {
                    je.printStackTrace();
                }

                //checking if image upload is successful
                if(status.equalsIgnoreCase("1"))
                {
                    Utils.loginEditor.putString("executive_profileimg", exeImgUrl).commit();
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
