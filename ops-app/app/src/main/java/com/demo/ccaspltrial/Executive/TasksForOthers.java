package com.demo.ccaspltrial.Executive;

import android.app.AlertDialog;
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
import android.widget.CheckBox;
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

public class TasksForOthers extends AppCompatActivity implements ExeExtraWorkFragment.OnFragmentInteractionListener {

    SearchableSpinner siteSpinner, empSpinner;
    ListView taskListview;
    ProgressBar tasksforothers_progress;
    public static Button extra_work, submit;
    Toolbar toolbar;
    ConstraintLayout heading_layout;

    Context context;

    ArrayList<ExeSiteModel> siteList;
    ArrayList<ExeEmployeeModel> empList;
    ArrayList<ExeTasksModel> taskList, selectedTasks;
    ArrayList<String> site, employee;

    ProgressDialog pDialog;

    String selectedSiteId="", selectedEmpId="", exeCurrentDate="", selectedEmpType="", selectedSubEmpId="";

    ExeTaskForOthersAdapter adapter;

    public static String extraWorkDone="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tasks_for_others);

        toolbar=(Toolbar)findViewById(R.id.toolbar);
        siteSpinner=(SearchableSpinner)findViewById(R.id.taskforothers_siteSpinner);
        empSpinner=(SearchableSpinner)findViewById(R.id.taskforothers_empSpinner);
        taskListview=(ListView)findViewById(R.id.taskforothers_listview);
        extra_work=(Button)findViewById(R.id.taskforothers_extrawork);
        submit=(Button)findViewById(R.id.tasksforothers_submit);
        heading_layout=(ConstraintLayout)findViewById(R.id.tasksforothers_headingLayout);
        tasksforothers_progress=(ProgressBar)findViewById(R.id.tasksforothers_progress);

        context=this;

        setSupportActionBar(toolbar);

        toolbar.setTitle("Tasks of Employees");

        //initialising sharedpreferences
        Utils.loginPreferences=getSharedPreferences(Utils.loginPref, MODE_PRIVATE);
        Utils.loginEditor=Utils.loginPreferences.edit();

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

        //adding action to site selection
        siteSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                String selectedSite=site.get(position);
                selectedSiteId=siteList.get(position).getSiteId();

                extraWorkDone="";

                //calculating current date
                getCurrentDate();

                //setting up employee spinner
                empSpinner.setVisibility(View.VISIBLE);
                Toast.makeText(context, "Select employee", Toast.LENGTH_LONG).show();

                if(!selectedSiteId.equalsIgnoreCase("") && Utils.isNetworkAvailable(context))
                {
                    new SiteEmployeeTask().execute(Utils.employeesofsiteURL);
                }
                else
                if(!Utils.isNetworkAvailable(context))
                {
                    Toast.makeText(context, "No internet", Toast.LENGTH_SHORT).show();

                    empSpinner.setVisibility(View.GONE);
                }
                else
                if(selectedSiteId.equalsIgnoreCase(""))
                {
                    Toast.makeText(context, "Please select site", Toast.LENGTH_SHORT).show();

                    empSpinner.setVisibility(View.GONE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

                extraWorkDone="";
                selectedSiteId="";
                empSpinner.setVisibility(View.GONE);
                heading_layout.setVisibility(View.GONE);
                taskListview.setVisibility(View.GONE);
                extra_work.setVisibility(View.GONE);
                submit.setVisibility(View.GONE);
            }
        });

        //adding action to employee spinner
        empSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                String selectedEmployee=employee.get(position);
                selectedEmpId=empList.get(position).getEmpId();
                selectedEmpType=empList.get(position).getEmp_type();
                selectedSubEmpId=empList.get(position).getSub_regular_empId();

                extraWorkDone="";

                if(!selectedSiteId.equalsIgnoreCase("")
                        && !selectedEmpId.equalsIgnoreCase("")
                        && !selectedSubEmpId.equalsIgnoreCase("")
                        && !exeCurrentDate.equalsIgnoreCase("")
                        && Utils.isNetworkAvailable(context))
                {
                    //send selectedEmpid if it is a regular employee and sub_regular_emp_id if it is a reliever or temporary reliever
                    if(selectedEmpType.equalsIgnoreCase("0"))
                    {
                        new ViewTaskslistTask().execute(Utils.tasklistviewURL, selectedEmpId);
                    }
                    else
                    if(selectedEmpType.equalsIgnoreCase("1")
                            || selectedEmpType.equalsIgnoreCase("2"))
                    {
                        new ViewTaskslistTask().execute(Utils.tasklistviewURL, selectedSubEmpId);
                    }
                }
                else
                if(selectedEmpId.equalsIgnoreCase(""))
                {
                    Toast.makeText(context, "Select employee", Toast.LENGTH_SHORT).show();

                    heading_layout.setVisibility(View.GONE);
                    taskListview.setVisibility(View.GONE);
                    extra_work.setVisibility(View.GONE);
                    submit.setVisibility(View.GONE);
                    selectedEmpType="";
                }
                else
                if(selectedSiteId.equalsIgnoreCase(""))
                {
                    Toast.makeText(context, "Select site", Toast.LENGTH_SHORT).show();

                    heading_layout.setVisibility(View.GONE);
                    taskListview.setVisibility(View.GONE);
                    extra_work.setVisibility(View.GONE);
                    submit.setVisibility(View.GONE);
                }
                else
                if(!Utils.isNetworkAvailable(context))
                {
                    Toast.makeText(context, "No internet", Toast.LENGTH_SHORT).show();

                    heading_layout.setVisibility(View.GONE);
                    taskListview.setVisibility(View.GONE);
                    extra_work.setVisibility(View.GONE);
                    submit.setVisibility(View.GONE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

                selectedSubEmpId="";
                selectedEmpType="";
                extraWorkDone="";
                selectedEmpId="";
                heading_layout.setVisibility(View.GONE);
                taskListview.setVisibility(View.GONE);
                extra_work.setVisibility(View.GONE);
                submit.setVisibility(View.GONE);
            }
        });

        //adding action to extra work button
        extra_work.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                FragmentManager fm=getSupportFragmentManager();
                FragmentTransaction transaction=fm.beginTransaction();
                transaction.add(R.id.exeExtraWorkFrag, new ExeExtraWorkFragment());
                transaction.commit();

            }
        });


        //adding action to submit button
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(!extraWorkDone.equalsIgnoreCase(""))
                {
                    ExeTasksModel obj=new ExeTasksModel();
                    obj.setTaskId("0");
                    obj.setTaskName("Extra Work");

                    selectedTasks.add(obj);
                }

                if(!selectedSiteId.equalsIgnoreCase("") && !selectedEmpId.equalsIgnoreCase("")
                        && !selectedSubEmpId.equalsIgnoreCase("") && selectedTasks.size() > 0
                        && Utils.isNetworkAvailable(context))
                {
                    new WorkSubmissionTask().execute(Utils.tasksubmissionURL);
                }
                else
                if(selectedSiteId.equalsIgnoreCase(""))
                {
                    Toast.makeText(context, "Select site!", Toast.LENGTH_SHORT).show();
                }
                else
                if(selectedEmpId.equalsIgnoreCase(""))
                {
                    Toast.makeText(context, "Select employee!", Toast.LENGTH_SHORT).show();
                }
                else
                if(selectedTasks.size() <= 0)
                {
                    Toast.makeText(context, "No tasks selected or extra work given", Toast.LENGTH_SHORT).show();
                }
                if(!Utils.isNetworkAvailable(context))
                {
                    Toast.makeText(context, "No internet", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    //method to calculate current date
    public void getCurrentDate()
    {
        //Getting current date
        Calendar calendar=Calendar.getInstance();
        int year=calendar.get(Calendar.YEAR);
        int month=calendar.get(Calendar.MONTH);
        int day=calendar.get(Calendar.DAY_OF_MONTH);

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

        exeCurrentDate=dd+"-"+mm+"-"+yy;
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

    //method to set up employee spinner
    public void setUpEmployeeSpinner(ArrayList<ExeEmployeeModel> list)
    {
        employee=new ArrayList<String>();

        for (ExeEmployeeModel obj : list)
        {
            employee.add(obj.getEmpCode() + " - " + obj.getEmpName());
        }

        //setting up site spinner
        ArrayAdapter<String> regularEmpAdapter=new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, employee);
        regularEmpAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        empSpinner.setAdapter(regularEmpAdapter);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        startActivity(new Intent(TasksForOthers.this, ExecutiveHome.class));
        finish();
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }


    //viewholder class for taskadapter
    static class ViewHolder
    {
        CheckBox checkBox;
        TextView task_name, task_time;
    }

    //adapter class for tasklist
    class ExeTaskForOthersAdapter extends BaseAdapter
    {
        ArrayList<ExeTasksModel> task_list;
        Context context;
        LayoutInflater inflater;

        public ExeTaskForOthersAdapter(){}

        public ExeTaskForOthersAdapter(Context context, ArrayList<ExeTasksModel> list)
        {
            this.context=context;
            task_list=list;
            inflater=LayoutInflater.from(context);
            selectedTasks=new ArrayList<ExeTasksModel>();
        }

        @Override
        public int getCount() {
            return task_list.size();
        }

        @Override
        public Object getItem(int position) {
            return task_list.get(position);
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

                convertView=inflater.inflate(R.layout.exe_tasklist_item2, null);

                holder.checkBox=(CheckBox)convertView.findViewById(R.id.taskforothers_checkbox);
                holder.task_name=(TextView)convertView.findViewById(R.id.taskforothers_taskname);
                holder.task_time=(TextView)convertView.findViewById(R.id.taskforothers_tasktime);

                convertView.setTag(holder);
            }
            else
            {
                holder=(ViewHolder)convertView.getTag();
            }

            //setting name to tasklist
            holder.task_name.setText(task_list.get(position).getTaskName());

            //setting task time to tasklist
            holder.task_time.setText(task_list.get(position).getTaskTime());

            //setting checked or unchecked status of task
            if(selectedTasks.contains(task_list.get(position)))
            {
                holder.checkBox.setChecked(true);
            }
            else
            {
                holder.checkBox.setChecked(false);
            }

            //adding action to checkbox
            holder.checkBox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if(holder.checkBox.isChecked())
                    {
                        //adding to selected tasks list if not present
                        if(!selectedTasks.contains(task_list.get(position)))
                        {
                            selectedTasks.add(task_list.get(position));
                        }
                    }
                    else
                    if(!holder.checkBox.isChecked())
                    {
                        //removing from selected tasks list if present
                        if(selectedTasks.contains(task_list.get(position)))
                        {
                            selectedTasks.remove(task_list.get(position));
                        }
                    }
                }
            });


            return convertView;
        }
    }//adapter closes


    //asynctask to retrieve sites under an executive
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

            tasksforothers_progress.setVisibility(View.VISIBLE);

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

            tasksforothers_progress.setVisibility(View.GONE);
        }
    }//asynctask closes

    //asynctask to get employees at a site
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

            tasksforothers_progress.setVisibility(View.VISIBLE);

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
                            newEmp.setSub_regular_empId("0"); //sub_regular_employee_id is 0 for regular employees

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
                    if(!exeCurrentDate.equalsIgnoreCase("")
                            && !selectedSiteId.equalsIgnoreCase("")
                            && Utils.isNetworkAvailable(context))
                    {
                        new SiteRelieversTask().execute(Utils.relieversitemappedURL);
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
//                    Toast.makeText(context, msg, Toast.LENGTH_LONG).show();

                    if(!exeCurrentDate.equalsIgnoreCase("")
                            && !selectedSiteId.equalsIgnoreCase("")
                            && Utils.isNetworkAvailable(context))
                    {
                        new SiteRelieversTask().execute(Utils.relieversitemappedURL);
                    }
                    else
                    if(!Utils.isNetworkAvailable(context))
                    {
                        Toast.makeText(context, "No internet", Toast.LENGTH_SHORT).show();
                    }
                }
                else
                {
                    if(!exeCurrentDate.equalsIgnoreCase("")
                            && !selectedSiteId.equalsIgnoreCase("")
                            && Utils.isNetworkAvailable(context))
                    {
                        new SiteRelieversTask().execute(Utils.relieversitemappedURL);
                    }
                    else
                    if(!Utils.isNetworkAvailable(context))
                    {
                        Toast.makeText(context, "No internet", Toast.LENGTH_SHORT).show();
                    }
                }
            }//outer else closes

            tasksforothers_progress.setVisibility(View.GONE);
        }
    }//asynctask closes


    //asynctask for retrieving relievers mapped to the site on that date
    private class SiteRelieversTask extends AsyncTask<String, Void, Void>
    {
        String content="";
        String error=null;
        String data="";
        String msg="";
        String status="";
        String site_id ="";
        String date="";

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            tasksforothers_progress.setVisibility(View.VISIBLE);

            site_id=selectedSiteId;
            date=exeCurrentDate;

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

                        }//for closes
                    }
                }//try closes
                catch (JSONException je)
                {
                    je.printStackTrace();
                }

                if(status.equalsIgnoreCase("1"))
                {
                    if(!exeCurrentDate.equalsIgnoreCase("")
                            && !selectedSiteId.equalsIgnoreCase("")
                            && Utils.isNetworkAvailable(context))
                    {
                        new SiteTempRelieversTask().execute(Utils.temprelieversitemappedURL);
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
//                    Toast.makeText(context, msg, Toast.LENGTH_LONG).show();

                    if(!exeCurrentDate.equalsIgnoreCase("")
                            && !selectedSiteId.equalsIgnoreCase("")
                            && Utils.isNetworkAvailable(context))
                    {
                        new SiteTempRelieversTask().execute(Utils.temprelieversitemappedURL);
                    }
                    else
                    if(!Utils.isNetworkAvailable(context))
                    {
                        Toast.makeText(context, "No internet", Toast.LENGTH_SHORT).show();
                    }
                }
                else
                {
                    if(!exeCurrentDate.equalsIgnoreCase("")
                            && !selectedSiteId.equalsIgnoreCase("")
                            && Utils.isNetworkAvailable(context))
                    {
                        new SiteTempRelieversTask().execute(Utils.temprelieversitemappedURL);
                    }
                    else
                    if(!Utils.isNetworkAvailable(context))
                    {
                        Toast.makeText(context, "No internet", Toast.LENGTH_SHORT).show();
                    }
                }
            }//outer else closes

            tasksforothers_progress.setVisibility(View.GONE);
        }
    }//asynctask closes


    //asynctask for retrieving relievers mapped to the site on that date
    private class SiteTempRelieversTask extends AsyncTask<String, Void, Void>
    {
        String content="";
        String error=null;
        String data="";
        String msg="";
        String status="";
        String site_id ="";
        String date="";

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            tasksforothers_progress.setVisibility(View.VISIBLE);

            site_id=selectedSiteId;
            date=exeCurrentDate;

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
                    setUpEmployeeSpinner(empList);
                }
                else
                if(status.equalsIgnoreCase("0"))
                {
                    Toast.makeText(context, msg, Toast.LENGTH_LONG).show();

                    setUpEmployeeSpinner(empList);
                }
                else
                {
                    setUpEmployeeSpinner(empList);
                }
            }//outer else closes

            tasksforothers_progress.setVisibility(View.GONE);
        }
    }//asynctask closes


    //async task to obtain task list
    private class ViewTaskslistTask extends AsyncTask<String, Void, Void>
    {
        String content="";
        String error=null;
        String data="";
        String msg="";
        String status="";
        String date_selected="";
        String site_id="";
        String employee_id="";

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            tasksforothers_progress.setVisibility(View.VISIBLE);

            date_selected=exeCurrentDate;
            site_id=selectedSiteId;

        }

        @Override
        protected Void doInBackground(String... strings) {

            employee_id=strings[1];

            //encoding data to passed as parameters
            try
            {
                data= URLEncoder.encode("site_id", "UTF-8") + "="
                        + URLEncoder.encode(site_id, "UTF-8") + "&"
                        + URLEncoder.encode("date_selected", "UTF-8") + "="
                        + URLEncoder.encode(date_selected, "UTF-8") + "&"
                        + URLEncoder.encode("employee_id", "UTF-8") + "="
                        + URLEncoder.encode(employee_id, "UTF-8");
            }
            catch (UnsupportedEncodingException uee)
            {
                uee.printStackTrace();
            }

            BufferedReader br=null;

            //getting data from webservice
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
                taskList=new ArrayList<ExeTasksModel>();

                //retrieving data from webservice
                try
                {
                    JSONArray jsonResponse=new JSONArray(content);
                    JSONObject obj1=jsonResponse.getJSONObject(0);
                    status=obj1.getString("status");
                    msg=obj1.getString("msg");

                    //checking if data retrieval is successful
                    if(status.equalsIgnoreCase("1"))
                    {
                        String task_id="", task_name="", task_time="";

                        JSONObject obj2=jsonResponse.getJSONObject(1);
                        JSONArray list=obj2.getJSONArray("tasklist");
                        int arrLength=list.length();

                        for(int i=0; i<arrLength; i++)
                        {
                            JSONObject listObj=list.getJSONObject(i);

                            task_id=listObj.getString("task_id");
                            task_name=listObj.getString("task_name");
                            task_time=listObj.getString("task_time");

                            //saving task data in model class
                            ExeTasksModel newTask=new ExeTasksModel();
                            newTask.setTaskId(task_id);
                            newTask.setTaskName(task_name);
                            newTask.setTaskTime(task_time);

                            //adding task object to arraylist
                            taskList.add(newTask);

                        } //for closes
                    }
                }//try closes
                catch (JSONException je)
                {
                    je.printStackTrace();
                }

                //loading tasklists if status is 1
                if(status.equalsIgnoreCase("1"))
                {
                    heading_layout.setVisibility(View.VISIBLE);
                    taskListview.setVisibility(View.VISIBLE);
                    extra_work.setVisibility(View.VISIBLE);
                    submit.setVisibility(View.VISIBLE);

                    //setting up listview
                    adapter=new ExeTaskForOthersAdapter(context, taskList);
                    taskListview.setAdapter(adapter);
                }
                else
                if(status.equalsIgnoreCase("0"))
                {
                    heading_layout.setVisibility(View.GONE);
                    taskListview.setVisibility(View.GONE);
                    extra_work.setVisibility(View.GONE);
                    submit.setVisibility(View.GONE);

                    Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
                }

            }//outer else closes

            tasksforothers_progress.setVisibility(View.GONE);
        }
    }//asynctask closes


    //asynctask for submitting tasks
    private class WorkSubmissionTask extends AsyncTask<String, Integer, Void>
    {
        String content="";
        String error=null;
        String data="";
        String msg="";
        String status="";
        String site_id="";
        ArrayList<ExeTasksModel> workDone;
        int progressStep=0;
        int count=0;
        String finalMsg="";

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            TasksForOthers.this.pDialog=new ProgressDialog(context);
            TasksForOthers.this.pDialog.setTitle("Sending request...");
            TasksForOthers.this.pDialog.setCancelable(false);
            TasksForOthers.this.pDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            TasksForOthers.this.pDialog.setMax(100);
            TasksForOthers.this.pDialog.setProgress(0);
            TasksForOthers.this.pDialog.show();

            site_id=selectedSiteId;

            workDone=selectedTasks;

            if(workDone.size() > 0)
            {
                progressStep=100/workDone.size();
            }

        }


        @Override
        protected Void doInBackground(String... strings) {


            for(ExeTasksModel obj:workDone)
            {
                content="";
                error=null;
                data="";
                msg="";
                status="";

                String task_id=obj.getTaskId();

                try
                {
                    data=URLEncoder.encode("task_id", "UTF-8") + "="
                            + URLEncoder.encode(task_id, "UTF-8") + "&"
                            + URLEncoder.encode("site_id", "UTF-8") + "="
                            + URLEncoder.encode(site_id, "UTF-8") + "&"
                            + URLEncoder.encode("employee_id", "UTF-8") + "=";

                    //adding remarks parameter
                    if(selectedEmpType.equalsIgnoreCase("0"))
                    {
                        data+=URLEncoder.encode(selectedEmpId, "UTF-8") + "&"
                                + URLEncoder.encode("remarks", "UTF-8") + "=";
                    }
                    else
                    if(selectedEmpType.equalsIgnoreCase("1") || selectedEmpType.equalsIgnoreCase("2"))
                    {
                        data+=URLEncoder.encode(selectedSubEmpId, "UTF-8") + "&"
                                + URLEncoder.encode("remarks", "UTF-8") + "=";
                    }

                    //adding reliever_id parameter
                    if(task_id.equalsIgnoreCase("0"))
                    {
                        data+=URLEncoder.encode(extraWorkDone, "UTF-8") + "&"
                                + URLEncoder.encode("reliever_id", "UTF-8") + "=";
                    }
                    else
                    {
                        data+=URLEncoder.encode("", "UTF-8") + "&"
                                + URLEncoder.encode("reliever_id", "UTF-8") + "=";
                    }

                    //adding temp_reliever_id
                    if(selectedEmpType.equalsIgnoreCase("1"))
                    {
                        data+=URLEncoder.encode(selectedEmpId, "UTF-8") + "&"
                                + URLEncoder.encode("temp_reliever_id", "UTF-8") + "=";
                    }
                    else
                    {
                        data+=URLEncoder.encode("0", "UTF-8") + "&"
                                + URLEncoder.encode("temp_reliever_id", "UTF-8") + "=";
                    }

                    if(selectedEmpType.equalsIgnoreCase("2"))
                    {
                        data+=URLEncoder.encode(selectedEmpId, "UTF-8");
                    }
                    else
                    {
                        data+=URLEncoder.encode("0", "UTF-8");
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

                //checking for connection errors
                if(error!=null)
                    break;

                try
                {
                    //retrieving api response
                    JSONObject jsonResponse=new JSONObject(content);
                    status=jsonResponse.getString("status");
                    msg=jsonResponse.getString("msg");
                }
                catch (JSONException je)
                {
                    je.printStackTrace();
                    finalMsg+="Error at "+obj.getTaskName() + "\n";
                }

                if(status.equalsIgnoreCase("1"))
                {
                    count+=progressStep;
                    publishProgress(count);
                }
                else
                if(status.equalsIgnoreCase("0"))
                {
                    finalMsg+=" "+obj.getTaskName()+" "+msg+"\n";

                    //removing extra work, if present, from the end of list of done tasks
                    // (it will be added again on pressing submit button)
                    if(obj.getTaskId().equalsIgnoreCase("0"))
                    {
                        selectedTasks.remove(obj);
                    }
                }
            } //for closes

            return null;
        }


        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);

            TasksForOthers.this.pDialog.setProgress(values[0]);
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            //closing progress dialog
            TasksForOthers.this.pDialog.dismiss();

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

                //removing extra work, if present, from the end of list of done tasks
                // (it will be added again on pressing submit button)
                if(selectedTasks.get(selectedTasks.size()-1).getTaskId().equalsIgnoreCase("0"))
                {
                    selectedTasks.remove(selectedTasks.size()-1);
                }
            }
            else
            if(!finalMsg.equalsIgnoreCase(""))
            {
                Toast.makeText(context, finalMsg, Toast.LENGTH_LONG).show();
            }
            else
            {
                Toast.makeText(context, "Tasks submitted", Toast.LENGTH_LONG).show();

                //re-initialising extra work done and selectedTasks list
                extraWorkDone="";
                Utils.appEditor.putString("ExtraWorkDoneForOthers", "").commit();

                selectedTasks=new ArrayList<ExeTasksModel>();

            }

        }
    }//asynctask closes
}
