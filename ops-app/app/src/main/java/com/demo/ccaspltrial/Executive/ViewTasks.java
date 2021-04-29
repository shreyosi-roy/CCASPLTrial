package com.demo.ccaspltrial.Executive;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.demo.ccaspltrial.LocalDatabase.ExecutiveDatabaseHelper;
import com.demo.ccaspltrial.R;
import com.demo.ccaspltrial.Utility.ExeEmployeeModel;
import com.demo.ccaspltrial.Utility.ExeSiteModel;
import com.demo.ccaspltrial.Utility.ExeTasksModel;
import com.demo.ccaspltrial.Utility.Utils;
import com.toptoche.searchablespinnerlibrary.SearchableSpinner;

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
import java.util.Calendar;

public class ViewTasks extends AppCompatActivity implements ViewExtraFragment.OnFragmentInteractionListener{

    Toolbar toolbar;
    ConstraintLayout table_header_layout;
    SearchableSpinner siteSpinner, empSpinner;
    ProgressBar viewtasks_progress;
    TextView date;
    ListView viewTasks_listview;
    Button viewExtra;

    Context context;

    String selectedDate="", selectedSiteId="", selectedEmpId="", selectedEmpType="";
    ArrayList<String> site, employee;
    ArrayList<ExeSiteModel> siteList;
    ArrayList<ExeEmployeeModel> empList;
    ArrayList<ExeTasksModel> taskList;

    ExeTaskAdapter adapter;

    ProgressDialog pDialog;

    public static String extra_work_done="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_tasks);

        toolbar=(Toolbar)findViewById(R.id.toolbar);
        table_header_layout=(ConstraintLayout)findViewById(R.id.vt_header);
        siteSpinner=(SearchableSpinner)findViewById(R.id.vt_siteSpinner);
        date=(TextView)findViewById(R.id.vt_date);
        empSpinner=(SearchableSpinner)findViewById(R.id.vt_empSpinner);
        viewTasks_listview=(ListView)findViewById(R.id.vt_listview);
        viewExtra=(Button)findViewById(R.id.view_extra);
        viewtasks_progress=(ProgressBar)findViewById(R.id.viewtasks_progress);

        setSupportActionBar(toolbar);

        toolbar.setTitle("View Tasks");

        context=this;

        //initialising sharedpreferences
        Utils.loginPreferences=getSharedPreferences(Utils.loginPref, MODE_PRIVATE);
        Utils.appPreferences=getSharedPreferences(Utils.appPref, MODE_PRIVATE);
        Utils.appEditor=Utils.appPreferences.edit();


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

                        extra_work_done="";
                        table_header_layout.setVisibility(View.GONE);
                        viewTasks_listview.setVisibility(View.GONE);
                        viewExtra.setVisibility(View.GONE);
                        selectedEmpId="";
                        selectedEmpType="";

                        if(siteSpinner.getVisibility()==View.GONE)
                        {
                            selectedSiteId="";

                            //setting up site spinner
                            siteSpinner.setVisibility(View.VISIBLE);

                            if(!Utils.loginPreferences.getString("executive_id", "").equalsIgnoreCase("")
                                    && Utils.isNetworkAvailable(context))
                            {
                                new RetrieveExeSiteTask().execute(Utils.exesiteretrievalURL);
                                Toast.makeText(context, "Select site", Toast.LENGTH_SHORT).show();
                            }
                            else
                            if(!Utils.isNetworkAvailable(context))
                            {
                                Toast.makeText(context, "No internet", Toast.LENGTH_SHORT).show();
                            }
                        }
                        else
                        {
                            if(selectedEmpId.equalsIgnoreCase(""))
                            {
                                Toast.makeText(context, "Select employee", Toast.LENGTH_SHORT).show();
                            }

                            //call api to set up employee spinner
                            if(!selectedSiteId.equalsIgnoreCase("") && Utils.isNetworkAvailable(context))
                            {
                                new SiteEmployeeTask().execute(Utils.employeesofsiteURL);
                            }
                            else
                            if(!Utils.isNetworkAvailable(context))
                            {
                                Toast.makeText(context, "No internet", Toast.LENGTH_SHORT).show();
                            }
                        }//outer else closes

                    }
                }, year1, month1, day1);
                pickerDialog.show();

            }
        });


        //adding action to site spinner item selection
        siteSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                String selectedSite=site.get(position);
                selectedSiteId=siteList.get(position).getSiteId();

                Toast.makeText(context, "Select employee", Toast.LENGTH_LONG).show();

                extra_work_done="";

                //call api to set up employee spinner
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

                table_header_layout.setVisibility(View.GONE);
                empSpinner.setVisibility(View.GONE);
                viewTasks_listview.setVisibility(View.GONE);
                selectedSiteId="";
                selectedEmpId="";
                extra_work_done="";
                viewExtra.setVisibility(View.GONE);
                selectedEmpType="";
            }
        });


        //adding action to employee spinner item selection
        empSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                String selectedEmp=employee.get(position);
                selectedEmpId=empList.get(position).getEmpId();
                selectedEmpType=empList.get(position).getEmp_type();

                extra_work_done="";

                //setting up images if date is selected
                if(date.getText().toString().equalsIgnoreCase(""))
                {
                    Toast.makeText(context, "Select date!", Toast.LENGTH_LONG).show();
                }
                else
                {
                    //setting up tasks list
                    table_header_layout.setVisibility(View.VISIBLE);
                    viewTasks_listview.setVisibility(View.VISIBLE);

                    //call api to display tasklist
                    if(!selectedDate.equalsIgnoreCase("")
                            && !selectedSiteId.equalsIgnoreCase("")
                            && !selectedEmpId.equalsIgnoreCase("")
                            && !selectedEmpType.equalsIgnoreCase("")
                            && Utils.isNetworkAvailable(context))
                    {
                        new ExeWorkDoneTask().execute(Utils.exetaskdonelistURL);
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

                table_header_layout.setVisibility(View.GONE);
                viewTasks_listview.setVisibility(View.GONE);
                selectedEmpId="";
                viewExtra.setVisibility(View.GONE);
                extra_work_done="";
            }
        });


        //adding action to voewExtra button
        viewExtra.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                FragmentManager fm=getSupportFragmentManager();
                FragmentTransaction transaction=fm.beginTransaction();
                transaction.add(R.id.viewExtra_frag, new ViewExtraFragment());
                transaction.commit();
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


    //method to set up employee spinner after retrieving employee data from asynctask
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
        empSpinner.setAdapter(employeeAdapter);
    }


    @Override
    public void onBackPressed() {
        startActivity(new Intent(ViewTasks.this, ExecutiveHome.class));
        finish();
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }


    static class ViewHolder
    {
        TextView task_name, task_time;
    }

    class ExeTaskAdapter extends BaseAdapter
    {
        ArrayList<ExeTasksModel> tasklist;
        LayoutInflater inflater;
        Context context;

        public ExeTaskAdapter(Context context, ArrayList<ExeTasksModel> list)
        {
            this.context=context;
            tasklist=list;
            inflater=LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            return tasklist.size();
        }

        @Override
        public Object getItem(int position) {
            return tasklist.get(position);
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
                holder=new ViewHolder();

                convertView=inflater.inflate(R.layout.exe_tasklist_item, null);

                holder.task_name=(TextView)convertView.findViewById(R.id.task_name);
                holder.task_time=(TextView)convertView.findViewById(R.id.task_time);

                convertView.setTag(holder);
            }
            else
            {
                holder=(ViewHolder)convertView.getTag();
            }

            //setting task name to item
            holder.task_name.setText(tasklist.get(position).getTaskName());

            //setting task time to item
            holder.task_time.setText(tasklist.get(position).getTaskTime());


            return convertView;
        }
    }



    //asynctask for retrieving sites under a executive
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

            viewtasks_progress.setVisibility(View.VISIBLE);

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
                if(status.equalsIgnoreCase("0"))
                {
                    Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
                }
            } //outer else closes

            viewtasks_progress.setVisibility(View.GONE);
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

            viewtasks_progress.setVisibility(View.VISIBLE);

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
                empList=new ArrayList<ExeEmployeeModel>();

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
                            empList.add(newEmp);
                        }
                    }
                }
                catch (JSONException je)
                {
                    je.printStackTrace();
                }

                if(status.equalsIgnoreCase("1"))
                {
                    if(!selectedSiteId.equalsIgnoreCase("")
                            && !selectedDate.equalsIgnoreCase("")
                            && Utils.isNetworkAvailable(context))
                    {
                        new SiteRelieversTask().execute(Utils.relieversitemappedURL);
                    }
                    else
                    if(!Utils.isNetworkAvailable(context))
                    {
                        Toast.makeText(ViewTasks.this, "No internet", Toast.LENGTH_SHORT).show();
                    }
                }
                else
                if(status.equalsIgnoreCase("0"))
                {
//                    Toast.makeText(context, msg, Toast.LENGTH_LONG).show();

                    if(!selectedSiteId.equalsIgnoreCase("")
                            && !selectedDate.equalsIgnoreCase("")
                            && Utils.isNetworkAvailable(context))
                    {
                        new SiteRelieversTask().execute(Utils.relieversitemappedURL);
                    }
                    else
                    if(!Utils.isNetworkAvailable(context))
                    {
                        Toast.makeText(ViewTasks.this, "No internet", Toast.LENGTH_SHORT).show();
                    }
                }
                else
                {
                    if(!selectedSiteId.equalsIgnoreCase("")
                            && !selectedDate.equalsIgnoreCase("")
                            && Utils.isNetworkAvailable(context))
                    {
                        new SiteRelieversTask().execute(Utils.relieversitemappedURL);
                    }
                    else
                    if(!Utils.isNetworkAvailable(context))
                    {
                        Toast.makeText(ViewTasks.this, "No internet", Toast.LENGTH_SHORT).show();
                    }
                }
            }//outer else closes

            viewtasks_progress.setVisibility(View.GONE);
        }
    }//asynctask closes


    //asynctask for retrieving reliever under an executive
    private class SiteRelieversTask extends AsyncTask<String, Void, Void>
    {
        String content="";
        String error=null;
        String data="";
        String msg="";
        String status="";
        String site_id="";
        String date="";

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            viewtasks_progress.setVisibility(View.VISIBLE);

            site_id=selectedSiteId;
            date=selectedDate;

        }


        @Override
        protected Void doInBackground(String... strings) {

            try
            {
                data= URLEncoder.encode("date", "UTF-8") + "="
                        + URLEncoder.encode(date, "UTF-8") + "&"
                        + URLEncoder.encode("site_id", "UTF-8") + "="
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

            String reliever_id="", reliever_code="", reliever_name="", sub_regular_empid="";

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
                    JSONArray jsonResponse=new JSONArray(content);
                    JSONObject obj1=jsonResponse.getJSONObject(0);
                    status=obj1.getString("status");
                    msg=obj1.getString("msg");

                    if(status.equalsIgnoreCase("1"))
                    {
                        JSONObject obj2=jsonResponse.getJSONObject(1);
                        JSONArray arrayObj=obj2.getJSONArray("details");
                        int arrLength=arrayObj.length();

                        for(int i=0; i<arrLength; i++)
                        {
                            JSONObject empObj=arrayObj.getJSONObject(i);

                            reliever_id=empObj.getString("reliever_id");
                            reliever_code=empObj.getString("reliever_code");
                            reliever_name=empObj.getString("reliever_name");
                            sub_regular_empid=empObj.getString("employee_id");

                            //adding values to ExeEmployeeModel object
                            ExeEmployeeModel newEmployee=new ExeEmployeeModel();
                            newEmployee.setEmpId(reliever_id);
                            newEmployee.setEmpCode(reliever_code);
                            newEmployee.setEmpName(reliever_name);
                            newEmployee.setEmp_type("1");
                            newEmployee.setSub_regular_empId(sub_regular_empid);

                            //adding object to list
                            empList.add(newEmployee);
                        }
                    }
                }
                catch (JSONException je)
                {
                    je.printStackTrace();
                }

                if(status.equalsIgnoreCase("1"))
                {
                    if(!selectedSiteId.equalsIgnoreCase("")
                            && !selectedDate.equalsIgnoreCase("")
                            && Utils.isNetworkAvailable(context))
                    {
                        new SiteTempRelieversTask().execute(Utils.temprelieversitemappedURL);
                    }
                    else
                    if(!Utils.isNetworkAvailable(context))
                    {
                        Toast.makeText(ViewTasks.this, "No internet", Toast.LENGTH_SHORT).show();
                    }
                }
                else
                if(status.equalsIgnoreCase("0"))
                {
//                    Toast.makeText(context, msg, Toast.LENGTH_LONG).show();

                    if(!selectedSiteId.equalsIgnoreCase("")
                            && !selectedDate.equalsIgnoreCase("")
                            && Utils.isNetworkAvailable(context))
                    {
                        new SiteTempRelieversTask().execute(Utils.temprelieversitemappedURL);
                    }
                    else
                    if(!Utils.isNetworkAvailable(context))
                    {
                        Toast.makeText(ViewTasks.this, "No internet", Toast.LENGTH_SHORT).show();
                    }
                }
                else
                {
                    if(!selectedSiteId.equalsIgnoreCase("")
                            && !selectedDate.equalsIgnoreCase("")
                            && Utils.isNetworkAvailable(context))
                    {
                        new SiteTempRelieversTask().execute(Utils.temprelieversitemappedURL);
                    }
                    else
                    if(!Utils.isNetworkAvailable(context))
                    {
                        Toast.makeText(ViewTasks.this, "No internet", Toast.LENGTH_SHORT).show();
                    }
                }
            } //outer else closes

            viewtasks_progress.setVisibility(View.GONE);
        }
    }//asynctask closes


    //asynctask for retrieving reliever under an executive
    private class SiteTempRelieversTask extends AsyncTask<String, Void, Void>
    {
        String content="";
        String error=null;
        String data="";
        String msg="";
        String status="";
        String site_id="";
        String date="";

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            viewtasks_progress.setVisibility(View.VISIBLE);

            site_id=selectedSiteId;
            date=selectedDate;

        }


        @Override
        protected Void doInBackground(String... strings) {

            try
            {
                data= URLEncoder.encode("date", "UTF-8") + "="
                        + URLEncoder.encode(date, "UTF-8") + "&"
                        + URLEncoder.encode("site_id", "UTF-8") + "="
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

            String tempreliever_id="", tempreliever_name="", sub_regular_empid="";


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
                    JSONArray jsonResponse=new JSONArray(content);
                    JSONObject obj1=jsonResponse.getJSONObject(0);
                    status=obj1.getString("status");
                    msg=obj1.getString("msg");

                    if(status.equalsIgnoreCase("1"))
                    {
                        JSONObject obj2=jsonResponse.getJSONObject(1);
                        JSONArray arrayObj=obj2.getJSONArray("details");
                        int arrLength=arrayObj.length();

                        for(int i=0; i<arrLength; i++)
                        {
                            JSONObject empObj=arrayObj.getJSONObject(i);

                            tempreliever_id=empObj.getString("temp_reliever_id");
                            tempreliever_name=empObj.getString("reliever_name");
                            sub_regular_empid=empObj.getString("employee_id");

                            //adding values to ExeEmployeeModel object
                            ExeEmployeeModel newEmployee=new ExeEmployeeModel();
                            newEmployee.setEmpId(tempreliever_id);
                            newEmployee.setEmpCode("Temporary");
                            newEmployee.setEmpName(tempreliever_name);
                            newEmployee.setEmp_type("2");
                            newEmployee.setSub_regular_empId(sub_regular_empid);

                            //adding object to list
                            empList.add(newEmployee);
                        }//for closes
                    }
                }//try closes
                catch (JSONException je)
                {
                    je.printStackTrace();
                }

                if(status.equalsIgnoreCase("1"))
                {
                    //making employee spinner visible
                    empSpinner.setVisibility(View.VISIBLE);
                    setUpEmployeeSpinner(empList);
                }
                else
                if(status.equalsIgnoreCase("0"))
                {
//                    Toast.makeText(context, msg, Toast.LENGTH_LONG).show();

                    //making employee spinner visible
                    empSpinner.setVisibility(View.VISIBLE);
                    setUpEmployeeSpinner(empList);
                }
                else
                {
                    //making employee spinner visible
                    empSpinner.setVisibility(View.VISIBLE);
                    setUpEmployeeSpinner(empList);
                }
            } //outer else closes

            viewtasks_progress.setVisibility(View.GONE);
        }
    }//asynctask closes


    //asynctask to retrieve task list for the selected employee of the site selected of the particular executive
    private class ExeWorkDoneTask extends AsyncTask<String, Void, Void>
    {
        String content="";
        String error=null;
        String data="";
        String msg="";
        String status="";
        String site_id="";
        String date="";
        String emp_id="";
        String emp_type_flag="";

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            viewtasks_progress.setVisibility(View.VISIBLE);

            date=selectedDate;
            site_id=selectedSiteId;
            emp_id=selectedEmpId;
            emp_type_flag=selectedEmpType;
        }


        @Override
        protected Void doInBackground(String... strings) {

            try
            {
                data=URLEncoder.encode("site_id", "UTF-8") + "="
                        + URLEncoder.encode(site_id, "UTF-8") + "&"
                        + URLEncoder.encode("date_selected", "UTF-8") + "="
                        + URLEncoder.encode(date, "UTF-8") + "&"
                        + URLEncoder.encode("employee_id", "UTF-8") + "="
                        + URLEncoder.encode(emp_id, "UTF-8") + "&"
                        + URLEncoder.encode("reliever_type_flag", "UTF-8") + "="
                        + URLEncoder.encode(emp_type_flag, "UTF-8");
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

            String task_id="", task_name="", task_time="";



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
                taskList=new ArrayList<ExeTasksModel>();

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
                        JSONArray arrObj1=obj2.getJSONArray("taskdonelist");
                        int arrLength1=arrObj1.length();

                        for(int i=0; i<arrLength1; i++)
                        {
                            JSONObject taskObj=arrObj1.getJSONObject(i);
                            task_id=taskObj.getString("task_id");
                            task_name=taskObj.getString("task_name");
                            task_time=taskObj.getString("task_time");

                            if(task_id.equalsIgnoreCase("0"))
                                continue;

                            //saving data to ExeTasksModel object
                            ExeTasksModel newTask=new ExeTasksModel();
                            newTask.setTaskId(task_id);
                            newTask.setTaskName(task_name);
                            newTask.setTaskTime(task_time);

                            //adding object to task list
                            taskList.add(newTask);
                        } //for closes

                        JSONObject obj3=jsonResponse.getJSONObject(2);
                        JSONArray arrObj2=obj3.getJSONArray("extrawork");
                        int arrLength2=arrObj2.length();

                        //checking whether there is any extra work or not
                        if(arrLength2 == 0)
                        {
                            Utils.appEditor.putString("ExtraWorkPresent", "No").commit();
                        }
                        else
                        {
                           for(int i=0; i<arrLength2; i++)
                           {
                               Utils.appEditor.putString("ExtraWorkPresent", "Yes").commit();
                               extra_work_done+=arrObj2.getString(i)+"\n";
                           }
                        }

                    }
                }
                catch (JSONException je)
                {
                    je.printStackTrace();
                }

                if(status.equalsIgnoreCase("1"))
                {
                    //setting up listview
                    adapter=new ExeTaskAdapter(context, taskList);
                    viewTasks_listview.setAdapter(adapter);

                    viewExtra.setVisibility(View.VISIBLE);
                }
                else
                if(status.equalsIgnoreCase("0"))
                {
                    //setting up listview
                    adapter=new ExeTaskAdapter(context, taskList);
                    viewTasks_listview.setAdapter(adapter);
                    table_header_layout.setVisibility(View.GONE);

                    viewExtra.setVisibility(View.GONE);

                    Utils.appEditor.putString("ExtraWorkPresent", "No").commit();
                    extra_work_done="";

                    Toast.makeText(ViewTasks.this, msg, Toast.LENGTH_LONG).show();
                }
                else
                {
                    //setting up listview
                    adapter=new ExeTaskAdapter(context, taskList);
                    viewTasks_listview.setAdapter(adapter);
                    table_header_layout.setVisibility(View.GONE);

                    viewExtra.setVisibility(View.GONE);

                    Utils.appEditor.putString("ExtraWorkPresent", "No").commit();
                    extra_work_done="";

                    Toast.makeText(ViewTasks.this, "No data found", Toast.LENGTH_LONG).show();
                }
            }//outer else closes

            viewtasks_progress.setVisibility(View.GONE);
        }
    }//asynctask closes
}
