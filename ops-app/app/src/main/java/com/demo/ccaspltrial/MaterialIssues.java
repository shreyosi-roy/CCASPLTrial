package com.demo.ccaspltrial;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import com.demo.ccaspltrial.Utility.Utils;

import org.json.JSONArray;
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

public class MaterialIssues extends AppCompatActivity {

    Toolbar toolbar;
    Spinner issuetype_spinner;
    Button matissue_submit;
    EditText matname_et, matremarks_et;
    ImageView damagematerial;
    ProgressBar matissues_progress;
    Bitmap image;
    String imagePath;

    String issueTypeList[]={"Select type", "Wrong material", "Extra material"};

    int issueTypeFlag=0;

    Context context;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_material_issues);

        toolbar=(Toolbar)findViewById(R.id.toolbar);
        issuetype_spinner=(Spinner)findViewById(R.id.issuetype_spinner);
        matissue_submit=(Button)findViewById(R.id.matissues_submit);
        matname_et=(EditText)findViewById(R.id.matissues_name_et);
        matremarks_et=(EditText)findViewById(R.id.matissues_remarks_et);
        matissues_progress=(ProgressBar)findViewById(R.id.matissues_progress);
        damagematerial=(ImageView)findViewById(R.id.damagematerial);

        setSupportActionBar(toolbar);

        //setting toolbar title
        toolbar.setTitle("Material Issues");

        context=getApplicationContext();

        //initialising sharedpreferences
        Utils.loginPreferences=getSharedPreferences(Utils.loginPref, MODE_PRIVATE);

        //setting up issue type spinner
        ArrayAdapter<String> arrayAdapter=new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, issueTypeList);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        issuetype_spinner.setAdapter(arrayAdapter);

        damagematerial.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
                {
                    Utils.requestRequiredPermissions(context, (Activity) context);
                }

                else {
                    Intent picIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(picIntent, Utils.REQUEST_SELFIE_PHOTO);

                }

            }
        });



        //adding action to submit button
        matissue_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //call method for action of submit button
                submitIssueInfo();
            }
        });

        //adding action to issue type selection
        issuetype_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {

                switch (position)
                {
                    case 0:
                        issueTypeFlag=0;
                        break;
                    case 1:
                        issueTypeFlag=1;
                        break;
                    case 2:
                        issueTypeFlag=2;
                        break;
                    default:
                        issueTypeFlag=0;
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

                issueTypeFlag=0;
            }
        });
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==Utils.REQUEST_SELFIE_PHOTO && resultCode==Activity.RESULT_OK)
        {
            Bundle extras=data.getExtras();
            image=(Bitmap)extras.get("data");

            // Toast.makeText(context, (CharSequence) data, Toast.LENGTH_SHORT).show();
            damagematerial.setImageBitmap(image);

            saveImage();


        }
    }

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
            f.mkdir();
        }

        //creating unique file name using datetime stamp
        DateFormat dateFormat=new SimpleDateFormat("yyyyMMdd_HHmmss");
        Date currentDate=new Date();
        String timeStamp=dateFormat.format(currentDate);


        //creating subdirectory if it doesn't exist
        File file=new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)+"/CCASPL/Damage_Image");
        if(!file.exists())
        {
            file.mkdir();
        }

        //unique file name
        fileName="CCASPL/Damage_Image/Damage"+timeStamp+".jpg";

        //getting image path
        imagePath=getImagePath(image);

        //uploading image to database
        if(!imagePath.equalsIgnoreCase("")
                && Utils.isNetworkAvailable(context))
        {
            //new SaveExeProfileImage.ExeProfileImgUploadTask().execute(Utils.exeprofileimguploadURL, imagePath);

            //Toast.makeText(context, "Done", Toast.LENGTH_SHORT).show();
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

    //method to get encoded image path
    public String getImagePath(Bitmap imageBitmap)
    {
        ByteArrayOutputStream bytes=new ByteArrayOutputStream();
        imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        byte []byteArray=bytes.toByteArray();
        String imagePaths= Base64.encodeToString(byteArray, Base64.DEFAULT);
        return imagePaths;
    }//getImagePath closes


    //method for action of submit button
    public void submitIssueInfo()
    {
        String matname="", matremarks="", issuetype="";

        matname=matname_et.getText().toString().trim();
        matremarks=matremarks_et.getText().toString().trim();

        if(issueTypeFlag == 0)
        {
            Toast.makeText(context, "Select issue type!", Toast.LENGTH_SHORT).show();
        }
        else
        if (matname.equalsIgnoreCase(""))
        {
            matname_et.setError("Enter material name(s)");
            matname_et.requestFocus();
        }
        else
        {
            if(issueTypeFlag == 1)
            {
                issuetype="Wrong material";
            }
            else
            if(issueTypeFlag == 2)
            {
                issuetype="Extra material";
            }

            //uploading material issue
            new MaterialIssueTask().execute(Utils.materialissuesURL, issuetype, matname, matremarks,imagePath);

        }
    }//submitIssueInfo closes

    @Override
    public void onBackPressed() {

        startActivity(new Intent(MaterialIssues.this, HomePage.class));
        finish();
    }

    //asynctask to send material issues
    private class MaterialIssueTask extends AsyncTask<String, Void, Void>
    {
        String content="";
        String error=null;
        String data="";
        String msg="";
        String status="";
        String site_id="";
        String emp_id="";

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            matissues_progress.setVisibility(View.VISIBLE);

            emp_id=Utils.loginPreferences.getString("emp_id", "");
            site_id=Utils.loginPreferences.getString("site_id", "");
        }

        @Override
        protected Void doInBackground(String... strings) {

            try
            {
                data= URLEncoder.encode("site_id", "UTF-8") + "="
                        + URLEncoder.encode(site_id, "UTF-8") + "&"
                        + URLEncoder.encode("employee_id", "UTF-8") + "="
                        + URLEncoder.encode(emp_id, "UTF-8") + "&"
                        + URLEncoder.encode("issue_type", "UTF-8") + "="
                        + URLEncoder.encode(strings[1], "UTF-8") + "&"
                        + URLEncoder.encode("material_names", "UTF-8") + "="
                        + URLEncoder.encode(strings[2], "UTF-8") + "&"
                        + URLEncoder.encode("remarks", "UTF-8") + "="
                        + URLEncoder.encode(strings[3], "UTF-8")
                        +"&"
                        + URLEncoder.encode("damageimage", "UTF-8") + "="
                        + URLEncoder.encode(strings[4], "UTF-8");

                //Log.d("URLDATA", "doInBackground:"+data);
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

//            Toast.makeText(context, data, Toast.LENGTH_SHORT).show();
//            System.out.print(data);
            super.onPostExecute(aVoid);

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
                //obtaining data from webservice
                try
                {
                    JSONArray jsonResponse=new JSONArray(content);
                    JSONObject obj1=jsonResponse.getJSONObject(0);
                    status=obj1.getString("status");
                    msg=obj1.getString("msg");
                }
                catch (JSONException je)
                {
                    je.printStackTrace();
                }

                if(status.equalsIgnoreCase("1"))
                {
                    Toast.makeText(context, "Issue raised!", Toast.LENGTH_SHORT).show();
                }
                else
                if(status.equalsIgnoreCase("0"))
                {
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
                }

            }//outer else closes

            //re-initialising screen
            matname_et.setText("");
            matremarks_et.setText("");
            issuetype_spinner.setSelection(0);
            damagematerial.setImageBitmap(null);


            matissues_progress.setVisibility(View.GONE);
        }
    }//asynctask closes

}
