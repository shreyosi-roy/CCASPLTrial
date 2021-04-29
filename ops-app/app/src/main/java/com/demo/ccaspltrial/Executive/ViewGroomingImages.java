package com.demo.ccaspltrial.Executive;

import android.app.AlertDialog;
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
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.demo.ccaspltrial.LocalDatabase.ExecutiveDatabaseHelper;
import com.demo.ccaspltrial.R;
import com.demo.ccaspltrial.Utility.ImageModel;
import com.demo.ccaspltrial.Utility.ExeEmployeeModel;
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
import java.util.Collections;

public class ViewGroomingImages extends AppCompatActivity {

    GridView groomingGridView;
    SearchableSpinner employeeSpinner;
    ProgressBar grooming_progress;
    ArrayList<String> employee;
    ArrayList<ImageModel> imageList;
    ConstraintLayout gi_layout1, gi_layout2;
    ImageView gi_fullImage;
    String selectedEmployeeId="";
    GroomingAdapter adapter;
    Toolbar toolbar;

    ArrayList<ExeEmployeeModel> employeeList;

    Context context;

    ExecutiveDatabaseHelper myExeDB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_grooming_images);

        groomingGridView=(GridView)findViewById(R.id.groomingGrid);
        employeeSpinner=(SearchableSpinner) findViewById(R.id.selectEmployee);
        gi_layout1=(ConstraintLayout)findViewById(R.id.gi_layout1);
        gi_layout2=(ConstraintLayout)findViewById(R.id.gi_layout2);
        gi_fullImage=(ImageView)findViewById(R.id.gi_fullImage);
        toolbar=(Toolbar)findViewById(R.id.toolbar);
        grooming_progress=(ProgressBar)findViewById(R.id.grooming_progress);

        setSupportActionBar(toolbar);

        toolbar.setTitle("View Grooming Images");

        context=this;

        //initialising sharedpreferences
        Utils.loginPreferences=getSharedPreferences(Utils.loginPref, Context.MODE_PRIVATE);
        Utils.appPreferences=getSharedPreferences(Utils.appPref, MODE_PRIVATE);
        Utils.appEditor=Utils.appPreferences.edit();

        myExeDB =new ExecutiveDatabaseHelper(context, Utils.appPreferences.getInt("ExecutiveDBVersion", 1));

        //setting up reliever list
        if(!Utils.loginPreferences.getString("executive_id", "").equalsIgnoreCase("")
                && Utils.isNetworkAvailable(context))
        {
            new RetrieveExeEmployeesTask().execute(Utils.exeemployeeretrievalURL);
        }
        else
        if(!Utils.isNetworkAvailable(context))
        {
            Toast.makeText(context, "No internet", Toast.LENGTH_SHORT).show();
        }


        //adding action to spinner item selection
        employeeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                String selectedEmp=employee.get(position);
                selectedEmployeeId=employeeList.get(position).getEmpId();

                //setting up images
                groomingGridView.setVisibility(View.VISIBLE);

                //displaying images
                if(!selectedEmployeeId.equalsIgnoreCase("") && Utils.isNetworkAvailable(context))
                {
                    new GroomingImagesTask().execute(Utils.imagelistviewURL);
                }
                else
                if(!Utils.isNetworkAvailable(context))
                {
                    Toast.makeText(context, "No internet", Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

                groomingGridView.setVisibility(View.GONE);
            }
        });
    }


    //method to setup employee spinner using data retrieved from asynctask RetrieveExeEmployeeTask
    public void setUpEmployeeSpinner(ArrayList<ExeEmployeeModel> list)
    {
        employee=new ArrayList<String>();

        for(ExeEmployeeModel obj : list)
        {
            employee.add(obj.getEmpCode() + " - " + obj.getEmpName());
        }

        //setting up spinner
        ArrayAdapter<String> employeeAdapter=new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, employee);
        employeeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        employeeSpinner.setAdapter(employeeAdapter);
    }


    @Override
    public void onBackPressed() {
        if(gi_layout2.getVisibility() == View.VISIBLE)
        {
            gi_layout2.setVisibility(View.GONE);
            gi_layout1.setVisibility(View.VISIBLE);
        }
        else
        {
            startActivity(new Intent(ViewGroomingImages.this, ExecutiveHome.class));
            finish();
        }
    }

    //viewholder class for GroomingAdapter
    static class ViewHolder
    {
        ImageView image;
        TextView date;
    }

    //adapter class for gridview
    class GroomingAdapter extends BaseAdapter
    {
        ArrayList<ImageModel> imagelist;
        LayoutInflater inflater;
        Context context;

        public GroomingAdapter(){}

        public GroomingAdapter(Context context, ArrayList<ImageModel> list)
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

                    gi_layout1.setVisibility(View.GONE);
                    gi_layout2.setVisibility(View.VISIBLE);

                    gi_fullImage.setImageBitmap(imagelist.get(position).getImageBitmap());
                }
            });

            return convertView;
        }
    }

    //asynctask for retrieving employees (regular and reliever) under an executive
    private class RetrieveExeEmployeesTask extends AsyncTask<String, Void, Void>
    {
        String content="";
        String error=null;
        String data="";
        String msg="";
        String status="";
        String executive_id ="";
        int localInsertEmpCount=0;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            grooming_progress.setVisibility(View.VISIBLE);

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

            String emp_id="", emp_code="", emp_name="", desig_id="", desig_name="", sub_regular_empid="";


            //showing error message if error has occurred
            if(error!=null)
            {
                if(Utils.appPreferences.getString("LocalExeEmployeePresent", "No").equalsIgnoreCase("Yes"))
                {
                    employeeList=new ArrayList<ExeEmployeeModel>();

                    //loading data from executive local database
                    employeeList= myExeDB.getExeEmployees();

                    setUpEmployeeSpinner(employeeList);
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

                employeeList=new ArrayList<ExeEmployeeModel>();

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
                        JSONArray arrayObj=obj2.getJSONArray("executive-employee");
                        int arrLength=arrayObj.length();

                        for(int i=0; i<arrLength; i++)
                        {
                            JSONObject empObj=arrayObj.getJSONObject(i);

                            emp_id=empObj.getString("employee_id");
                            emp_code=empObj.getString("employee_code");
                            emp_name=empObj.getString("employee_name");
                            desig_id=empObj.getString("designation_id");
                            desig_name=empObj.getString("designation_name");
                            //set sub_regular_emp_id when received-----not needed here

                            //adding values to ExeEmployeeModel object
                            ExeEmployeeModel newEmployee=new ExeEmployeeModel();
                            newEmployee.setEmpId(emp_id);
                            newEmployee.setEmpCode(emp_code);
                            newEmployee.setEmpName(emp_name);
                            newEmployee.setDesigId(desig_id);
                            newEmployee.setDesigName(desig_name);
                            if(desig_id.equalsIgnoreCase("1")) {
                                newEmployee.setEmp_type("0");
                                newEmployee.setSub_regular_empId("0");
                            }
                            else
                            if(desig_id.equalsIgnoreCase("2")) {
                                newEmployee.setEmp_type("1");
                                newEmployee.setSub_regular_empId(sub_regular_empid);
                            }
                            else
                            {
                                continue;
                            }

                            //adding object to list
                            employeeList.add(newEmployee);

                            //adding data to executive local databse
                            if(Utils.appPreferences.getString("LocalExeEmployeePresent", "No").equalsIgnoreCase("No"))
                            {
                                boolean checkInserted= myExeDB.insertExeEmployee(emp_id, emp_code, emp_name, newEmployee.getEmp_type(), newEmployee.getSub_regular_empId());

                                if(checkInserted)
                                {
                                    localInsertEmpCount++;
                                }
                            }//adding to executive db closes
                        }
                    }
                }
                catch (JSONException je)
                {
                    je.printStackTrace();
                }

                if(status.equalsIgnoreCase("1"))
                {
                    setUpEmployeeSpinner(employeeList);

                    //checking whether data is successfully loaded into local database
                    if(Utils.appPreferences.getString("LocalExeEmployeePresent", "No").equalsIgnoreCase("No"))
                    {
                        if(localInsertEmpCount== employeeList.size())
                        {
                            Utils.appEditor.putString("LocalExeEmployeePresent", "Yes").commit();
                        }
                        else
                        {
                            Utils.appEditor.putString("LocalExeEmployeePresent", "No").commit();
                        }
                    }
                }
                else
                {
                    Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
                }
            }//outer else closes

            grooming_progress.setVisibility(View.GONE);
        }
    }//asynctask closes


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

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            grooming_progress.setVisibility(View.VISIBLE);

            emp_id=selectedEmployeeId;
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
                        System.out.println("Image: "+ imageObj);
                        imageUrl=imageObj.getString("url");
//                        imageUrl="http://demo.brandconfiance.com/operation_app_ccaspl/image/image.png";
                        imageTimeStamp=imageObj.getString("timestamp");

                        URL url = new URL(imageUrl);
                        Bitmap image = BitmapFactory.decodeStream(url.openConnection().getInputStream());

                        //compressing image
                        ByteArrayOutputStream out = new ByteArrayOutputStream();
//                        image.compress(Bitmap.CompressFormat.JPEG, 85, out);
                        Bitmap compressedImg= BitmapFactory.decodeStream(new ByteArrayInputStream(out.toByteArray()));

                        //saving data in ImageModel object
                        ImageModel newImage=new ImageModel();
                        newImage.setImageId(i);
                        newImage.setImageURL(imageUrl);
                        newImage.setImageBitmap(compressedImg);
                        newImage.setTimeStamp(extractDate(imageTimeStamp));

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
            else
            {

                if(status.equalsIgnoreCase("1"))
                {
                    //reversing the image list to get the latest image first
                    Collections.reverse(imageList);

                    //setting up gridview
                    adapter=new GroomingAdapter(context, imageList);
                    groomingGridView.setAdapter(adapter);

                }
                else
                if(status.equalsIgnoreCase("0"))
                {
                    //setting up gridview
                    adapter=new GroomingAdapter(context, imageList);
                    groomingGridView.setAdapter(adapter);

                    Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
                }
            }

            grooming_progress.setVisibility(View.GONE);
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
}
