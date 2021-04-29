package com.demo.ccaspltrial.Executive;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.demo.ccaspltrial.LocalDatabase.ExecutiveDatabaseHelper;
import com.demo.ccaspltrial.Login;
import com.demo.ccaspltrial.R;
import com.demo.ccaspltrial.Utility.ExeEmployeeModel;
import com.demo.ccaspltrial.Utility.ExeSiteModel;
import com.demo.ccaspltrial.Utility.Utils;
import com.toptoche.searchablespinnerlibrary.SearchableSpinner;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Calendar;

public class ExecutiveHome extends AppCompatActivity implements TempRelieverDataEntry.OnFragmentInteractionListener
{
    NavigationView nav_View;
    DrawerLayout drawerLayout;
    ProgressBar exehome_progress;
    ActionBarDrawerToggle drawerToggle;
    TextView uname;
    Toolbar toolbar;
    ImageView exeProfileImg;

    SearchableSpinner relieverSpinner, siteSpinner, empSpinner;
    public static Button assign;
    ArrayList<String> reliever, site, regularEmp;
    TextView assigndate;
    ProgressDialog pDialog;
    String selectedDate="", selectedSiteId="", selectedRelieverId="", selectedRegularEmpId="";

    ArrayList<ExeEmployeeModel> relieverList;
    ArrayList<ExeSiteModel> siteList;
    ArrayList<ExeEmployeeModel> regularEmpList;
    ArrayList<ExeEmployeeModel> employeeList; //for loading all employee data into local database

    String exe_currentDate ="", reliever_type_flag="";

    int exeVersionCount;

    ExecutiveDatabaseHelper myExeDB;

    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_executive_home);

        nav_View=(NavigationView)findViewById(R.id.exe_nav_View);
        drawerLayout=(DrawerLayout)findViewById(R.id.exe_drawerLayout);
        toolbar=(Toolbar)findViewById(R.id.toolbar);
        relieverSpinner=(SearchableSpinner)findViewById(R.id.exe_home_relieverSpinner);
        siteSpinner=(SearchableSpinner)findViewById(R.id.exe_home_siteSpinner);
        empSpinner=(SearchableSpinner)findViewById(R.id.exe_home_empSpinner);
        assign=(Button)findViewById(R.id.assign);
        assigndate=(TextView)findViewById(R.id.assigndate);
        exehome_progress=(ProgressBar)findViewById(R.id.exehome_progress);

        setSupportActionBar(toolbar);

        toolbar.setTitle("CCASPL Executive");

        context=this;

        //setting up the hamburger icon for the navigation drawer
        drawerToggle=new ActionBarDrawerToggle(this, drawerLayout, R.string.drawer_open, R.string.drawer_close);
        drawerLayout.addDrawerListener(drawerToggle);

        //making the hamburger icon visible on the toolbar
        final ActionBar actionBar=getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeAsUpIndicator(R.drawable.hamburger_icon);
        drawerToggle.syncState();

        //calling method to request required permissions
        Utils.requestRequiredPermissions(this, this);

        //initialising sharedpreferences
        Utils.loginPreferences=getSharedPreferences(Utils.loginPref, Context.MODE_PRIVATE);
        Utils.loginEditor=Utils.loginPreferences.edit();
        Utils.appPreferences=getSharedPreferences(Utils.appPref, MODE_PRIVATE);
        Utils.appEditor=Utils.appPreferences.edit();

        myExeDB =new ExecutiveDatabaseHelper(context, Utils.appPreferences.getInt("ExecutiveDBVersion", 1));

        //setting username to navigation drawer header
        View navHeader=nav_View.getHeaderView(0);
        uname=(TextView)navHeader.findViewById(R.id.exe_uname);
        exeProfileImg=(ImageView)navHeader.findViewById(R.id.exe_header_image);
        uname.setText(Utils.loginPreferences.getString("executive_name", ""));

        //setting profile image
        new Thread(new Runnable() {
            @Override
            public void run() {

                if(!Utils.loginPreferences.getString("executive_profileimg", "").equalsIgnoreCase(""))
                {
                    try {
                        URL url = new URL(Utils.loginPreferences.getString("executive_profileimg", ""));
                        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                        connection.setDoInput(true);
                        connection.connect();
                        InputStream input = connection.getInputStream();
                        Bitmap profileBitmap = BitmapFactory.decodeStream(input);

                        //compressing image
                        ByteArrayOutputStream out = new ByteArrayOutputStream();
                        profileBitmap.compress(Bitmap.CompressFormat.JPEG, 75, out);
                        final Bitmap compressedImg= BitmapFactory.decodeStream(new ByteArrayInputStream(out.toByteArray()));

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                exeProfileImg.setImageBitmap(compressedImg);
                            }
                        });
                    }
                    catch (IOException ioe)
                    {
                        ioe.printStackTrace();
                    }
                }//if closes

            }
        }).start();

        //setting onSelectItemListeners for navigation menu items
        nav_View.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {

                int itemId=menuItem.getItemId();

                //adding action to menu item selection
                switch (itemId)
                {
                    case R.id.exe_profile:
                        startActivity(new Intent(ExecutiveHome.this, ExecutiveProfile.class));
                        drawerLayout.closeDrawers();
                        finish();
                        break;

                    case R.id.exe_tasks_done:
                        startActivity(new Intent(ExecutiveHome.this, ViewTasks.class));
                        drawerLayout.closeDrawers();
                        finish();
                        break;

                    case R.id.grooming_images:
                        startActivity(new Intent(ExecutiveHome.this, ViewGroomingImages.class));
                        drawerLayout.closeDrawers();
                        finish();
                        break;

                    case R.id.site_images:
                        startActivity(new Intent(ExecutiveHome.this, ViewSiteImages.class));
                        drawerLayout.closeDrawers();
                        finish();
                        break;

                    case R.id.tasksforothers:
                        startActivity(new Intent(ExecutiveHome.this, TasksForOthers.class));
                        drawerLayout.closeDrawers();
                        finish();
                        break;

                    case R.id.exe_sync:
                        exeVersionCount=Utils.appPreferences.getInt("ExecutiveDBVersion", 1);
                        exeVersionCount++;
                        Utils.appEditor.putInt("ExecutiveDBVersion", exeVersionCount).commit();
                        myExeDB =new ExecutiveDatabaseHelper(context, exeVersionCount);

                        //re-initialising sharedpreferences data
                        Utils.appEditor.putString("LocalExeSitePresent", "No");
                        Utils.appEditor.putString("LocalExeRelieverPresent", "No");
                        Utils.appEditor.putString("LocalExeEmployeePresent", "No");
                        Utils.appEditor.commit();

                        Toast.makeText(context, "Data sync complete", Toast.LENGTH_LONG).show();
                        drawerLayout.closeDrawers();
                        break;

                    case R.id.exe_logout:
                        Utils.loginEditor.clear();
                        Utils.loginEditor.putString("loggedIn", "No");
                        Utils.loginEditor.commit();
                        startActivity(new Intent(ExecutiveHome.this, Login.class));
                        drawerLayout.closeDrawers();
                        finish();
                        break;
                }

                return true;
            }
        });


        //getting current date
        Calendar calendar=Calendar.getInstance();
        final int year=calendar.get(Calendar.YEAR);
        final int month=calendar.get(Calendar.MONTH);
        final int day=calendar.get(Calendar.DAY_OF_MONTH);

        String yy, mm, dd;
        yy=""+year;

        if((month+1)<10)
        {
            mm="0"+(int)(month+1);
        }
        else
        {
            mm=""+(int)(month+1);
        }

        if(day<10)
        {
            dd="0"+day;
        }
        else
        {
            dd=""+day;
        }

        exe_currentDate =dd+"-"+mm+"-"+yy;

        //setting up reliever list
        if(!Utils.loginPreferences.getString("executive_id", "").equalsIgnoreCase("")
                && Utils.isNetworkAvailable(context))
        {
            new RetrieveExeRelieversTask().execute(Utils.exeemployeeretrievalURL);
        }

        //setting up site spinner
        if(!Utils.loginPreferences.getString("executive_id", "").equalsIgnoreCase("")
                && Utils.isNetworkAvailable(context))
        {
            new RetrieveExeSiteTask().execute(Utils.exesiteretrievalURL);
        }

        if(!Utils.isNetworkAvailable(context))
        {
            Toast.makeText(context, "No internet", Toast.LENGTH_SHORT).show();
        }

        assigndate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                DatePickerDialog pickDialog=new DatePickerDialog(context, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {

                        showDate(year, month, dayOfMonth);

                        //add action on clicking assign

                    }
                }, year, month, day);
                pickDialog.show();
            }
        });

        //adding action to reliever selection
        relieverSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                String selectedEmp=reliever.get(position);

                if(position == 0)
                {
                    selectedRelieverId="-1";
                    reliever_type_flag="2"; //for temporary reliever

                    FragmentManager fm=getSupportFragmentManager();
                    FragmentTransaction transaction=fm.beginTransaction();
                    transaction.add(R.id.temp_reliever_data_frag, new TempRelieverDataEntry());
                    transaction.commit();
                }
                else
                {
                    selectedRelieverId=relieverList.get(position-1).getEmpId();
                    reliever_type_flag="1"; //for reliever
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

                selectedRelieverId="";
                reliever_type_flag="";
            }
        });


        //adding action to site selection
        siteSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                String selectedSite=site.get(position);
                selectedSiteId=siteList.get(position).getSiteId();

                //setting up regular employee spinner
                empSpinner.setVisibility(View.VISIBLE);
                Toast.makeText(context, "Select employee to be substituted", Toast.LENGTH_LONG).show();

                if(!selectedSiteId.equalsIgnoreCase("") && Utils.isNetworkAvailable(context))
                {
                    new SiteEmployeeTask().execute(Utils.employeesofsiteURL);
                }
                else
                if(!Utils.isNetworkAvailable(context))
                {
                    Toast.makeText(context, "No internet", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

                empSpinner.setVisibility(View.GONE);
                selectedSiteId="";
            }
        });


        //adding action to regular employee selection
        empSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                String selectedRegularEmp=regularEmp.get(position);
                selectedRegularEmpId=regularEmpList.get(position).getEmpId();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

                selectedRegularEmpId="";
            }
        });


        //adding action assign button
        assign.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //checking whether reliever is selected
                if(selectedRelieverId.equalsIgnoreCase(""))
                {
                    Toast.makeText(context, "Select reliever to assign!", Toast.LENGTH_LONG).show();
                }
                else
                    //checking whether site is selected
                if(selectedSiteId.equalsIgnoreCase(""))
                {
                    Toast.makeText(context, "Select site to assign!", Toast.LENGTH_LONG).show();
                }
                else
                        //checking whether date is selected
                if(selectedDate.equalsIgnoreCase(""))
                {
                    Toast.makeText(context, "Select date of assignment!", Toast.LENGTH_LONG).show();
                }
                else
                //checking if regularemployee to be substituted is selected
                if(selectedRegularEmpId.equalsIgnoreCase(""))
                {
                    Toast.makeText(context, "Select employee to be substituted!", Toast.LENGTH_LONG).show();
                }
                else
                {
                    if(selectedRelieverId.equalsIgnoreCase("-1"))
                    {
                        if(!TempRelieverDataEntry.tempreliever_name.equalsIgnoreCase("")
                                && Utils.isNetworkAvailable(context))
                        {
                            //entering temp reliever details in database
                            new TempRelieverDetailsTask().execute(Utils.temprelieverdataentryURL);
                        }
                        else
                        if(TempRelieverDataEntry.tempreliever_name.equalsIgnoreCase(""))
                        {
                            selectedRelieverId="-1";
                            reliever_type_flag="2"; //for temporary reliever

                            FragmentManager fm=getSupportFragmentManager();
                            FragmentTransaction transaction=fm.beginTransaction();
                            transaction.add(R.id.temp_reliever_data_frag, new TempRelieverDataEntry());
                            transaction.commit();
                        }
                    }
                    else
                    //assigning reliever to site for the selected date
                    if(!selectedDate.equalsIgnoreCase("")
                            && !selectedRelieverId.equalsIgnoreCase("")
                            && !selectedSiteId.equalsIgnoreCase("")
                            && !selectedRegularEmpId.equalsIgnoreCase("")
                            && !reliever_type_flag.equalsIgnoreCase("")
                            && Utils.isNetworkAvailable(context))
                    {
                        new RelieverAssignTask().execute(Utils.relieverassignURL);
                    }
                    else
                    if(!Utils.isNetworkAvailable(context))
                    {
                        Toast.makeText(context, "No internet", Toast.LENGTH_SHORT).show();
                    }

                }
            }
        });

        //loading all employee details into local db if not present
        if(Utils.appPreferences.getString("LocalExeEmployeesPresent", "No").equalsIgnoreCase("No")
                && !Utils.loginPreferences.getString("executive_id", "").equalsIgnoreCase("")
                && Utils.isNetworkAvailable(context))
        {
            new RetrieveExeEmployeesTask().execute(Utils.exeemployeeretrievalURL);
        }
        else
        if(!Utils.isNetworkAvailable(context))
        {
            Toast.makeText(context, "No internet", Toast.LENGTH_SHORT).show();
        }

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
        assigndate.setText(selectedDate);

    }


    //method to setup reliever spinner using data from asynctask RetrieveExeEmployeeTask
    public void setUpRelieverSpinner(ArrayList<ExeEmployeeModel> list)
    {
        reliever=new ArrayList<String>();

        //adding temporary reliever as the first entry in the reliever dropdown
        reliever.add("Temporary reliever");

        for(ExeEmployeeModel obj : list)
        {
            reliever.add(obj.getEmpCode()+" - "+obj.getEmpName());
        }

        //setting up reliever spinner
        ArrayAdapter<String> relieverAdapter=new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, reliever);
        relieverAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        relieverSpinner.setAdapter(relieverAdapter);
    }

    //method to setup site spinner using data from asynctask RetrieveExeSiteTask
    public void setUpSiteSpinner(ArrayList<ExeSiteModel> list)
    {
        site=new ArrayList<String>();

        for (ExeSiteModel obj : list)
        {
            site.add(obj.getSiteCode() + " - " + obj.getSiteName());
        }

        //setting up site spinner
        ArrayAdapter<String> siteAdapter=new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, site);
        siteAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        siteSpinner.setAdapter(siteAdapter);
    }


    //method to setup regular employee spinner using data from asynctask SiteEmployeeTask
    public void setUpRegularEmpSpinner(ArrayList<ExeEmployeeModel> list)
    {
        regularEmp=new ArrayList<String>();

        for (ExeEmployeeModel obj : list)
        {
            regularEmp.add(obj.getEmpCode() + " - " + obj.getEmpName());
        }

        //setting up site spinner
        ArrayAdapter<String> regularEmpAdapter=new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, regularEmp);
        regularEmpAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        empSpinner.setAdapter(regularEmpAdapter);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(drawerToggle.onOptionsItemSelected(item))
        {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onBackPressed() {

        finish();
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }


    //asynctask for retrieving reliever under a executive
    private class RetrieveExeRelieversTask extends AsyncTask<String, Void, Void>
    {
        String content="";
        String error=null;
        String data="";
        String msg="";
        String status="";
        String executive_id ="";
        int localInsertRelieverCount=0;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            exehome_progress.setVisibility(View.VISIBLE);

            executive_id =Utils.loginPreferences.getString("executive_id", "");

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
                if(Utils.appPreferences.getString("LocalExeRelieverPresent", "No").equalsIgnoreCase("Yes"))
                {
                    relieverList=new ArrayList<ExeEmployeeModel>();

                    //loading data from executive local database
                    relieverList= myExeDB.getExeRelievers();

                    setUpRelieverSpinner(relieverList);
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

                relieverList=new ArrayList<ExeEmployeeModel>();

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
                            desig_id=empObj.getString("designation_id");

                            if(desig_id.equalsIgnoreCase("2"))
                            {
                                emp_id=empObj.getString("employee_id");
                                emp_code=empObj.getString("employee_code");
                                emp_name=empObj.getString("employee_name");
                                desig_name=empObj.getString("designation_name");
                                //set sub_regular_emp_id when received----not needed here

                                //adding values to ExeEmployeeModel object
                                ExeEmployeeModel newEmployee=new ExeEmployeeModel();
                                newEmployee.setEmpId(emp_id);
                                newEmployee.setEmpCode(emp_code);
                                newEmployee.setEmpName(emp_name);
                                newEmployee.setDesigId(desig_id);
                                newEmployee.setDesigName(desig_name);
                                newEmployee.setEmp_type("1");
                                newEmployee.setSub_regular_empId(sub_regular_empid);

                                //adding object to list
                                relieverList.add(newEmployee);

                                //adding data to executive local databse
                                if(Utils.appPreferences.getString("LocalExeRelieverPresent", "No").equalsIgnoreCase("No"))
                                {
                                    boolean checkInserted= myExeDB.insertExeReliever(emp_id, emp_code, emp_name, sub_regular_empid);

                                    if(checkInserted)
                                    {
                                        localInsertRelieverCount++;
                                    }
                                }//adding to executive db closes

                            }


                        }//for closes
                    }
                }//try closes
                catch (JSONException je)
                {
                    je.printStackTrace();
                }

                if(status.equalsIgnoreCase("1"))
                {
                    setUpRelieverSpinner(relieverList);

                    //checking whether data is successfully loaded into local database
                    if(Utils.appPreferences.getString("LocalExeRelieverPresent", "No").equalsIgnoreCase("No"))
                    {
                        if(localInsertRelieverCount== relieverList.size())
                        {
                            Utils.appEditor.putString("LocalExeRelieverPresent", "Yes").commit();
                        }
                        else
                        {
                            Utils.appEditor.putString("LocalExeRelieverPresent", "No").commit();

                            myExeDB.deleteExeRelievers();
                        }
                    }
                }
                else
                if(status.equalsIgnoreCase("0"))
                {
                    Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
                }
            } //outer else closes

            exehome_progress.setVisibility(View.GONE);
        }
    }//asynctask closes


    //asynctask for retrieving sites under an executive
    private class RetrieveExeSiteTask extends AsyncTask<String, Void, Void>
    {
        String content="";
        String error=null;
        String data="";
        String msg="";
        String status="";
        String executive_id ="";
        int localInsertSiteCount=0;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

//            ExecutiveHome.this.pDialog=new ProgressDialog(context);
//            ExecutiveHome.this.pDialog.setTitle("Loading...");
//            ExecutiveHome.this.pDialog.setCancelable(false);
//            ExecutiveHome.this.pDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
//            ExecutiveHome.this.pDialog.show();

            executive_id =Utils.loginPreferences.getString("executive_id", "");

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

            //closing progressdialog
//            ExecutiveHome.this.pDialog.dismiss();

            //showing error message if error has occurred
            if(error!=null)
            {
                if(Utils.appPreferences.getString("LocalExeSitePresent", "No").equalsIgnoreCase("Yes"))
                {
                    siteList=new ArrayList<ExeSiteModel>();

                    //loading data from executive local database
                    siteList= myExeDB.getExeSites();

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

                            //adding data to executive local databse
                            if(Utils.appPreferences.getString("LocalExeSitePresent", "No").equalsIgnoreCase("No"))
                            {
                                boolean checkInserted= myExeDB.insertExeSite(site_id, site_code, site_name);

                                if(checkInserted)
                                {
                                    localInsertSiteCount++;
                                }
                            }//adding to executive db closes
                        }//for closes
                    }
                }//try closes
                catch (JSONException je)
                {
                    je.printStackTrace();
                }

                if(status.equalsIgnoreCase("1"))
                {
                    setUpSiteSpinner(siteList);

                    //checking whether data is successfully loaded into local database
                    if(Utils.appPreferences.getString("LocalExeSitePresent", "No").equalsIgnoreCase("No"))
                    {
                        if(localInsertSiteCount == siteList.size())
                        {
                            Utils.appEditor.putString("LocalExeSitePresent", "Yes").commit();
                        }
                        else
                        {
                            Utils.appEditor.putString("LocalExeSitePresent", "No").commit();

                            myExeDB.deleteExeSites();
                        }
                    }
                }
                else
                if(status.equalsIgnoreCase("0"))
                {
                    Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
                }
            }//outer else closes
        }
    }//asynctask closes


    //asynctask to retrieve regular employees at a site
    private class SiteEmployeeTask extends AsyncTask<String, Void, Void>
    {
        String content="";
        String error=null;
        String data="";
        String msg="";
        String status="";
        String site_id="";

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            exehome_progress.setVisibility(View.VISIBLE);

            site_id=selectedSiteId;
        }


        @Override
        protected Void doInBackground(String... strings) {

            try
            {
                data=URLEncoder.encode("site_id", "UTF-8") + "="
                        + URLEncoder.encode(site_id, "UTF-8");
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

            String emp_id="", emp_code="", emp_name="";

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
                regularEmpList=new ArrayList<ExeEmployeeModel>();

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
                        JSONArray arrayObj=obj2.getJSONArray("employelist");
                        int arrLength=arrayObj.length();

                        for(int i=0; i<arrLength; i++)
                        {
                            JSONObject empObj=arrayObj.getJSONObject(i);
                            emp_id=empObj.getString("employee_id");
                            emp_code=empObj.getString("employee_code");
                            emp_name=empObj.getString("employee_name");

                            //saving data in ExeEmployeeModel object
                            ExeEmployeeModel newEmp=new ExeEmployeeModel();
                            newEmp.setEmpId(emp_id);
                            newEmp.setEmpCode(emp_code);
                            newEmp.setEmpName(emp_name);
                            newEmp.setEmp_type("0");
                            newEmp.setSub_regular_empId("0");

                            //adding object to arraylist
                            regularEmpList.add(newEmp);
                        }
                    }
                }
                catch (JSONException je)
                {
                    je.printStackTrace();
                }

                if(status.equalsIgnoreCase("1"))
                {
                    setUpRegularEmpSpinner(regularEmpList);
                }
                else
                if(status.equalsIgnoreCase("0"))
                {
                    Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
                }
            }//outer else closes

            exehome_progress.setVisibility(View.GONE);
        }
    }//asynctask closes


    //asynctask for assigning reliever to a site
    private class RelieverAssignTask extends AsyncTask<String, Void, Void>
    {
        String content="";
        String error=null;
        String data="";
        String msg="";
        String status="";
        String reliever_id="";
        String regularEmp_id="";
        String site_id="";
        String date="";
        String reliever_flag="";

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            exehome_progress.setVisibility(View.VISIBLE);

            site_id=selectedSiteId;
            date=selectedDate;
            regularEmp_id=selectedRegularEmpId;
            reliever_flag=reliever_type_flag;

        }


        @Override
        protected Void doInBackground(String... strings) {

            if(reliever_flag.equalsIgnoreCase("1"))
            {
                reliever_id=selectedRelieverId;
            }
            else
            if(reliever_flag.equalsIgnoreCase("2"))
            {
                reliever_id=strings[1];
            }

            try
            {
                data=URLEncoder.encode("date_of_assignment", "UTF-8") + "="
                        + URLEncoder.encode(date, "UTF-8") + "&"
                        + URLEncoder.encode("employee_id", "UTF-8") + "="
                        + URLEncoder.encode(regularEmp_id, "UTF-8") + "&"
                        + URLEncoder.encode("site_id", "UTF-8") + "="
                        + URLEncoder.encode(site_id, "UTF-8") + "&"
                        + URLEncoder.encode("reliever_id", "UTF-8") + "="
                        + URLEncoder.encode(reliever_id, "UTf-8") + "&"
                        + URLEncoder.encode("reliever_type_flag", "UTF-8") + "="
                        + URLEncoder.encode(reliever_flag, "UTF-8");
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
                //retrieving data from webservice
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

                if(status.equalsIgnoreCase("1"))
                {
                    Toast.makeText(context, "Reliever assigned!", Toast.LENGTH_LONG).show();
                    reliever_type_flag="";

                }
                else
                if(status.equalsIgnoreCase("0"))
                {
                    Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
                }
            }//outer else closes

            exehome_progress.setVisibility(View.GONE);
        }
    }//asynctask closes


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

            //closing progressdialog
//            ExecutiveHome.this.pDialog.dismiss();

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
                            //set sub_regular_emp_id when received----not needed here

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
                                newEmployee.setSub_regular_empId(selectedRegularEmpId);
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

                            myExeDB.deleteExeEmployees();
                        }
                    }
                }
            }//outer else closes
        }
    }//asynctask closes


    //asynctask to enter temporary reliever details into database
    class TempRelieverDetailsTask extends AsyncTask<String, Void, Void>
    {
        String content="";
        String error=null;
        String data="";
        String msg="";
        String status="";

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            exehome_progress.setVisibility(View.VISIBLE);

        }


        @Override
        protected Void doInBackground(String... strings) {

            try
            {
                data=URLEncoder.encode("name", "UTF-8") + "="
                        + URLEncoder.encode(TempRelieverDataEntry.tempreliever_name, "UTF-8") + "&"
                        + URLEncoder.encode("number", "UTF-8") + "="
                        + URLEncoder.encode(TempRelieverDataEntry.tempreliever_phno, "UTF-8") + "&"
                        + URLEncoder.encode("alternative_number", "UTF-8") + "="
                        + URLEncoder.encode(TempRelieverDataEntry.tempreliever_altno, "UTF-8") + "&"
                        + URLEncoder.encode("city", "UTF-8") + "="
                        + URLEncoder.encode(TempRelieverDataEntry.tempreliever_city, "Utf-8") + "&"
                        + URLEncoder.encode("state", "UTF-8") + "="
                        + URLEncoder.encode(TempRelieverDataEntry.tempreliever_state, "UTF-8") + "&"
                        + URLEncoder.encode("country", "UTF-8") + "="
                        + URLEncoder.encode(TempRelieverDataEntry.tempreliever_country, "UTF-8") + "&"
                        + URLEncoder.encode("address", "Utf-8") + "="
                        + URLEncoder.encode(TempRelieverDataEntry.tempreliever_addr, "UTF-8") + "&"
                        + URLEncoder.encode("email", "UTF-8") + "="
                        + URLEncoder.encode(TempRelieverDataEntry.tempreliever_email, "UTF-8");
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

            String temp_reliever_id="", temp_reliever_name="";

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
                //retrieving data from webservice
                try
                {
                    JSONObject jsonRsponse=new JSONObject(content);
                    status=jsonRsponse.getString("status");
                    msg=jsonRsponse.getString("msg");

                    if(status.equalsIgnoreCase("1"))
                    {
                        temp_reliever_id=jsonRsponse.getString("id");
                        temp_reliever_name=jsonRsponse.getString("name");
                    }
                }
                catch (JSONException je)
                {
                    je.printStackTrace();
                }

                if(status.equalsIgnoreCase("1"))
                {
                    //re-initialising temp reliever data entered
                    TempRelieverDataEntry.tempreliever_name="";
                    TempRelieverDataEntry.tempreliever_phno="";
                    TempRelieverDataEntry.tempreliever_altno="";
                    TempRelieverDataEntry.tempreliever_city="";
                    TempRelieverDataEntry.tempreliever_state="";
                    TempRelieverDataEntry.tempreliever_country="";
                    TempRelieverDataEntry.tempreliever_email="";
                    TempRelieverDataEntry.tempreliever_addr="";

                    //assigning reliever to site for the selected date
                    if(!selectedDate.equalsIgnoreCase("")
                            && !temp_reliever_id.equalsIgnoreCase("")
                            && !selectedSiteId.equalsIgnoreCase("")
                            && !selectedRegularEmpId.equalsIgnoreCase("")
                            && !reliever_type_flag.equalsIgnoreCase("")
                            && Utils.isNetworkAvailable(context))
                    {
                        new RelieverAssignTask().execute(Utils.relieverassignURL, temp_reliever_id);
                    }
                    else
                    if(!Utils.isNetworkAvailable(context))
                    {
                        Toast.makeText(context, "No internet", Toast.LENGTH_SHORT).show();
                    }
                }
                else
                if(status.equalsIgnoreCase("0"))
                {
                    Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
                }
            }//outer else closes

            exehome_progress.setVisibility(View.GONE);
        }
    }//asynctask closes
}
