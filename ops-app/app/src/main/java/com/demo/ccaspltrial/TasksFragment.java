package com.demo.ccaspltrial;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.demo.ccaspltrial.LocalDatabase.RegularDatabaseHelper;
import com.demo.ccaspltrial.LocalDatabase.RelieverDatabaseHelper;
import com.demo.ccaspltrial.Utility.CertificationModel;
import com.demo.ccaspltrial.Utility.QuestionModel;
import com.demo.ccaspltrial.Utility.TasksModel2;
import com.demo.ccaspltrial.Utility.TrainingModel;
import com.demo.ccaspltrial.Utility.Utils;

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

import static android.content.Context.MODE_PRIVATE;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link TasksFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link TasksFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TasksFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;
    
    //parameters required for this fragment
    TextView date;
    Button submit_tasks, extraWork;
    ListView taskListView;
    ProgressBar taskfrag_progress;
    ConstraintLayout regular_layout, reliever_layout, currentDate_layout, otherDate_layout;
    
    ArrayList<TasksModel2> taskList;
    ArrayList<TasksModel2> selectedTasks;
    static String extraWorkDoneRegular="", extraWorkDoneReliever="";
    TaskAdapter2 taskAdapter;
    TaskAdapter3 taskAdapter3;

    String regular_currentDate ="", reliever_currentDate="";

    ProgressDialog pDialog;

    int regularVersionCount, relieverVersionCount;

    RegularDatabaseHelper myRegularDB;

    RelieverDatabaseHelper myRelieverDB;

    ArrayList<CertificationModel> certList;

    public TasksFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment TasksFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static TasksFragment newInstance(String param1, String param2) {
        TasksFragment fragment = new TasksFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_tasks, container, false);
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android TrainingVideos lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        regular_layout=(ConstraintLayout)view.findViewById(R.id.regular_layout);
        reliever_layout=(ConstraintLayout)view.findViewById(R.id.reliever_layout);
        taskfrag_progress=(ProgressBar)view.findViewById(R.id.taskfrag_progress);

        //initialising sharedpreferences
        Utils.appPreferences=getActivity().getSharedPreferences(Utils.appPref, MODE_PRIVATE);
        Utils.appEditor=Utils.appPreferences.edit();
        Utils.pagePreferences=getActivity().getSharedPreferences(Utils.pagePref, MODE_PRIVATE);
        Utils.pageEditor=Utils.pagePreferences.edit();
        Utils.loginPreferences=getActivity().getSharedPreferences(Utils.loginPref, MODE_PRIVATE);
        Utils.loginEditor=Utils.loginPreferences.edit();


        //retrieving saved extra work
        extraWorkDoneRegular=Utils.appPreferences.getString("ExtraWorkDoneRegular", "");
        extraWorkDoneReliever=Utils.appPreferences.getString("ExtraWorkDoneReliever", "");


        //retrieving requested training questions, if any
        String lastscreen=Utils.pagePreferences.getString("LastScreenForQuestions", "");

        if((lastscreen.equalsIgnoreCase("LoginScreen") || lastscreen.equalsIgnoreCase("SplashScreen"))
                && !Utils.loginPreferences.getString("emp_id", "").equalsIgnoreCase("")
                && Utils.isNetworkAvailable(getContext()))
        {
            new TrainingQuestionsTask().execute(Utils.trainingquestionsURL);

            Utils.pageEditor.putString("LastScreenForQuestions", "").commit();
        }
        else
        if(!Utils.isNetworkAvailable(getContext()))
        {
            Toast.makeText(getContext(), "No internet", Toast.LENGTH_SHORT).show();

            Utils.pageEditor.putString("LastScreen", "").commit();
        }


        //hiding date selection option if login type is reliever
        if(Utils.loginPreferences.getString("LoginType", "None").equalsIgnoreCase("Reliever"))
        {
            regular_layout.setVisibility(View.GONE);
            reliever_layout.setVisibility(View.VISIBLE);
            setUpRelieverLayout(view);

        }
        else
        if(Utils.loginPreferences.getString("LoginType", "None").equalsIgnoreCase("Regular"))
        {
            reliever_layout.setVisibility(View.GONE);
            regular_layout.setVisibility(View.VISIBLE);
            setUpRegularLayout(view);
        }

        //adding action to submit button
        submit_tasks.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //adding extra work done if any
                if(Utils.loginPreferences.getString("LoginType", "None").equalsIgnoreCase("Regular"))
                {
                    if(!extraWorkDoneRegular.equalsIgnoreCase(""))
                    {
                        TasksModel2 obj=new TasksModel2();
                        obj.setTaskId("0");
                        obj.setTaskName("Extra Work");

                        selectedTasks.add(obj);
                    }

                    if(!Utils.loginPreferences.getString("emp_id", "").equalsIgnoreCase("")
                            && !Utils.loginPreferences.getString("site_id", "").equalsIgnoreCase("")
                            && selectedTasks.size() > 0 && Utils.isNetworkAvailable(getContext()))
                    {
                        new WorkSubmissionTask().execute(Utils.tasksubmissionURL);
                    }
                    else
                    if(selectedTasks.size() <= 0)
                    {
                        Toast.makeText(getContext(), "No tasks selected or extra work done", Toast.LENGTH_LONG).show();
                    }
                    else
                    if(Utils.loginPreferences.getString("site_id", "").equalsIgnoreCase(""))
                    {
                        Toast.makeText(getContext(), "No site assigned", Toast.LENGTH_LONG).show();
                    }
                    else
                    if(!Utils.isNetworkAvailable(getContext()))
                    {
                        Toast.makeText(getContext(), "No internet", Toast.LENGTH_SHORT).show();
                    }
                }
                else
                if(Utils.loginPreferences.getString("LoginType", "None").equalsIgnoreCase("Reliever"))
                {
                    if(!extraWorkDoneReliever.equalsIgnoreCase(""))
                    {
                        TasksModel2 obj=new TasksModel2();
                        obj.setTaskId("0");
                        obj.setTaskName("Extra Work");

                        selectedTasks.add(obj);
                    }


                    if(!Utils.loginPreferences.getString("emp_id", "").equalsIgnoreCase("")
                            && !Utils.loginPreferences.getString("site_id", "").equalsIgnoreCase("")
                            && !Utils.loginPreferences.getString("RelieverSubEmployeeId", "").equalsIgnoreCase("")
                            && selectedTasks.size() > 0 && Utils.isNetworkAvailable(getContext()))
                    {
                        new WorkSubmissionTask().execute(Utils.tasksubmissionURL);
                    }
                    else
                    if(selectedTasks.size() <= 0)
                    {
                        Toast.makeText(getContext(), "No tasks selected or extra work done", Toast.LENGTH_LONG).show();
                    }
                    else
                    if(Utils.loginPreferences.getString("site_id", "").equalsIgnoreCase(""))
                    {
                        Toast.makeText(getContext(), "No site assigned", Toast.LENGTH_LONG).show();
                    }
                    else
                    if(Utils.loginPreferences.getString("RelieverSubEmployeeId", "").equalsIgnoreCase(""))
                    {
                        Toast.makeText(getContext(), "No employee substituted", Toast.LENGTH_LONG).show();
                    }
                    else
                    if(!Utils.isNetworkAvailable(getContext()))
                    {
                        Toast.makeText(getContext(), "No internet", Toast.LENGTH_SHORT).show();
                    }
                }



            }
        });

        //adding action to extra work button
        extraWork.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //opening extra work fragment
                FragmentManager fm=getFragmentManager();
                FragmentTransaction transaction=fm.beginTransaction();
                if(Utils.loginPreferences.getString("LoginType", "None").equalsIgnoreCase("Regular"))
                {
                    transaction.add(R.id.tfr_extraWork_frag, new ExtraWorkFragment());
                }
                else
                if(Utils.loginPreferences.getString("LoginType", "None").equalsIgnoreCase("Reliever"))
                {
                    transaction.add(R.id.tfr_extraWork_frag2, new ExtraWorkFragment());
                }
                transaction.commit();
            }
        });



    }

    //setting up layout for regular employee login
    public void setUpRegularLayout(View view)
    {
        date=(TextView)view.findViewById(R.id.date);
        submit_tasks=(Button)view.findViewById(R.id.submit_tasks);
        extraWork=(Button) view.findViewById(R.id.extraWork);
        taskListView=(ListView)view.findViewById(R.id.taskListView);
        currentDate_layout=(ConstraintLayout)view.findViewById(R.id.currentDate_layout);
        otherDate_layout=(ConstraintLayout)view.findViewById(R.id.otherDate_layout);

        taskListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

        //Getting current date
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

        regular_currentDate =dd+"-"+mm+"-"+yy;

        //saving current date for checking first login of day if not already saved
        if(!Utils.appPreferences.getString("RegularCurrentDate", "")
                .equalsIgnoreCase(regular_currentDate))
        {
            Utils.appEditor.putString("RegularCurrentDate", regular_currentDate).commit();

            //creating or upgrading local database and incrementing version
            regularVersionCount=Utils.appPreferences.getInt("RegularDBVersion", 1);
            myRegularDB=new RegularDatabaseHelper(getContext(), regularVersionCount);
            regularVersionCount++;
            Utils.appEditor.putInt("RegularDBVersion", regularVersionCount).commit();

            //re-initialising sharedpreferences data
            Utils.appEditor.putString("RegularLocalTasksPresent", "No");
            Utils.appEditor.putString("RegularLocalMatPresent", "No");
            Utils.appEditor.putString("RegularLocalSelfiePresent", "No");
            Utils.appEditor.putString("RegularLocalChallanPresent", "No");
            Utils.appEditor.putString("RegularLocalAttendancePresent", "No");
            Utils.appEditor.putString("RegularLocalCertificationPresent", "No");
            Utils.appEditor.commit();

            //re-initialising extraWorkDoneRegular to "" for the day
            extraWorkDoneRegular="";
            Utils.appEditor.putString("ExtraWorkDoneRegular", "").commit();
        }
        else
        {
            myRegularDB=new RegularDatabaseHelper(getContext(), Utils.appPreferences.getInt("RegularDBVersion", 1));
        }


        if(Utils.pagePreferences.getString("LastScreen", "").equalsIgnoreCase("SOP"))
        {
            String selDate=Utils.appPreferences.getString("TaskListDate", "");

            //setting previously set date to textview
            date.setText(selDate);

            //calling api if required data is not null
            if(!Utils.loginPreferences.getString("site_id", "").equalsIgnoreCase("")
                    && !Utils.appPreferences.getString("TaskListDate", "").equalsIgnoreCase("")
                    && !Utils.loginPreferences.getString("emp_id", "").equalsIgnoreCase("")
                    && Utils.isNetworkAvailable(getContext()))
            {
                //loading tasklist
                new ViewTaskslistTask().execute(Utils.tasklistviewURL,
                        Utils.loginPreferences.getString("emp_id", ""));
            }
            else
            if(Utils.loginPreferences.getString("site_id", "").equalsIgnoreCase(""))
            {
                Toast.makeText(getContext(), "No site assigned", Toast.LENGTH_LONG).show();
            }
            else
            if(!Utils.isNetworkAvailable(getContext()))
            {
                Toast.makeText(getContext(), "No internet", Toast.LENGTH_SHORT).show();
            }


            Utils.pageEditor.putString("LastScreen", "").commit();
        }
        else
        {
            Utils.appEditor.putString("TaskListDate", regular_currentDate).commit();

            //setting current date to textview
            date.setText(regular_currentDate);

            //calling api if required data is not null
            if(!Utils.loginPreferences.getString("site_id", "").equalsIgnoreCase("")
                    && !Utils.appPreferences.getString("TaskListDate", "").equalsIgnoreCase("")
                    && !Utils.loginPreferences.getString("emp_id", "").equalsIgnoreCase("")
                    && Utils.isNetworkAvailable(getContext()))
            {
                //loading tasklist
                new ViewTaskslistTask().execute(Utils.tasklistviewURL,
                        Utils.loginPreferences.getString("emp_id", ""));
            }
            else
            if(Utils.loginPreferences.getString("site_id", "").equalsIgnoreCase(""))
            {
                Toast.makeText(getContext(), "No site assigned", Toast.LENGTH_LONG).show();
            }
            else
            if(!Utils.isNetworkAvailable(getContext()))
            {
                Toast.makeText(getContext(), "No internet", Toast.LENGTH_SHORT).show();
            }

        }

        //opening datepicker dialog on clicking date textview
        date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                DatePickerDialog pickDate=new DatePickerDialog(getContext(), new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {

                        String selDate=showDate(year, month, dayOfMonth);

                        //calling api if required data is not null
                        if(!Utils.loginPreferences.getString("site_id", "").equalsIgnoreCase("")
                                && !Utils.appPreferences.getString("TaskListDate", "").equalsIgnoreCase("")
                                && !Utils.loginPreferences.getString("emp_id", "").equalsIgnoreCase("")
                                && Utils.isNetworkAvailable(getContext()))
                        {
                            //loading tasklist
                            new ViewTaskslistTask().execute(Utils.tasklistviewURL,
                                    Utils.loginPreferences.getString("emp_id", ""));
                        }
                        else
                        if(Utils.loginPreferences.getString("site_id", "").equalsIgnoreCase(""))
                        {
                            Toast.makeText(getContext(), "No site assigned", Toast.LENGTH_LONG).show();
                        }
                        else
                        if(!Utils.isNetworkAvailable(getContext()))
                        {
                            Toast.makeText(getContext(), "No internet", Toast.LENGTH_SHORT).show();
                        }


                    }
                }, year, month, day);
                pickDate.show();


            }
        });


        //loading certifications details into local db if not present
        if(!Utils.loginPreferences.getString("emp_id", "").equalsIgnoreCase("")
                && Utils.isNetworkAvailable(getContext()))
        {
            new ViewCertificationsTask().execute(Utils.certificationviewURL);
        }
        else
        if(!Utils.isNetworkAvailable(getContext()))
        {
            Toast.makeText(getContext(), "No internet", Toast.LENGTH_SHORT).show();
        }
    }//setUpRegularLayout closes

    //setting up layout for reliever login
    public void setUpRelieverLayout(View view)
    {
        submit_tasks=(Button)view.findViewById(R.id.submit_tasks);
        extraWork=(Button) view.findViewById(R.id.extraWork);
        taskListView=(ListView)view.findViewById(R.id.taskListView2);
        currentDate_layout=(ConstraintLayout)view.findViewById(R.id.currentDate_layout2);

        taskListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

        //Getting current date
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

        reliever_currentDate=dd+"-"+mm+"-"+yy;


        //saving current date for checking first login of day if not already saved
        if(!Utils.appPreferences.getString("RelieverCurrentDate", "").equalsIgnoreCase(reliever_currentDate))
        {
            Utils.appEditor.putString("RelieverCurrentDate", reliever_currentDate).commit();

            //creating or upgrading local database and incrementing version
            relieverVersionCount=Utils.appPreferences.getInt("RelieverDBVersion", 1);
            myRelieverDB=new RelieverDatabaseHelper(getContext(), relieverVersionCount);
            relieverVersionCount++;
            Utils.appEditor.putInt("RelieverDBVersion", relieverVersionCount);

            //re-initialising sharedpreferences data
            Utils.appEditor.putString("RelieverLocalTasksPresent", "No");
            Utils.appEditor.putString("RelieverLocalMatPresent", "No");
            Utils.appEditor.putString("RelieverLocalSelfiePresent", "No");
            Utils.appEditor.putString("RelieverLocalChallanPresent", "No");
            Utils.appEditor.putString("RelieverLocalAttendancePresent", "No");
            Utils.appEditor.putString("RelieverLocalCertificationPresent", "No");
            Utils.appEditor.commit();

            //re-initialising extraWorkDoneReliever to "" for the day
            extraWorkDoneReliever="";
            Utils.appEditor.putString("ExtraWorkDoneReliever", "").commit();
        }
        else
        {
            myRelieverDB=new RelieverDatabaseHelper(getContext(), Utils.appPreferences.getInt("RelieverDBVersion", 1));
        }

        Utils.appEditor.putString("TaskListDate", reliever_currentDate).commit();

        //checking if site has changed from before
        if(!Utils.loginPreferences.getString("site_id", "")
                .equalsIgnoreCase(Utils.appPreferences.getString("RelieverSiteId", "")))
        {
            Utils.appEditor.putString("RelieverSiteId", Utils.loginPreferences.getString("site_id", "")).commit();

            //checking if site has changed on the same day
            if(reliever_currentDate.equalsIgnoreCase(Utils.appPreferences.getString("RelieverCurrentDate", ""))
                    && !Utils.loginPreferences.getString("site_id", "").equalsIgnoreCase(""))
            {
                //upgrading local database and incrementing version
                relieverVersionCount=Utils.appPreferences.getInt("RelieverDBVersion", 1);
                myRelieverDB=new RelieverDatabaseHelper(getContext(), relieverVersionCount);
                relieverVersionCount++;
                Utils.appEditor.putInt("RelieverDBVersion", relieverVersionCount);

                //re-initialising sharedpreferences data
                Utils.appEditor.putString("RelieverLocalTasksPresent", "No");
                Utils.appEditor.putString("RelieverLocalMatPresent", "No");
                Utils.appEditor.putString("RelieverLocalSelfiePresent", "No");
                Utils.appEditor.putString("RelieverLocalChallanPresent", "No");
                Utils.appEditor.putString("RelieverLocalAttendancePresent", "No");
                Utils.appEditor.putString("RelieverLocalCertificationPresent", "No");
                Utils.appEditor.commit();

                //re-initialising extraWorkDoneReliever to "" for the day
                extraWorkDoneReliever="";
                Utils.appEditor.putString("ExtraWorkDoneReliever", "").commit();
            }//innermost for closes
        }

        //setting up tasklist if any
        if(!Utils.loginPreferences.getString("site_id", "").equalsIgnoreCase("")
                && !Utils.appPreferences.getString("TaskListDate", "").equalsIgnoreCase("")
                && !Utils.loginPreferences.getString("RelieverSubEmployeeId", "").equalsIgnoreCase("")
                && Utils.isNetworkAvailable(getContext()))
        {
            //loading tasklist
            new ViewTaskslistTask().execute(Utils.tasklistviewURL,
                    Utils.loginPreferences.getString("RelieverSubEmployeeId", ""));
        }
        else
        if(Utils.loginPreferences.getString("site_id", "").equalsIgnoreCase(""))
        {
            Toast.makeText(getContext(), "No site assigned", Toast.LENGTH_LONG).show();
        }
        else
        if(!Utils.isNetworkAvailable(getContext()))
        {
            Toast.makeText(getContext(), "No internet", Toast.LENGTH_SHORT).show();
        }


        Utils.pageEditor.putString("LastScreen", "").commit();

        //loading certifications details into local db if not present
        if(!Utils.loginPreferences.getString("emp_id", "").equalsIgnoreCase("")
                && Utils.isNetworkAvailable(getContext()))
        {
            new ViewCertificationsTask().execute(Utils.certificationviewURL);
        }
        else
        if(!Utils.isNetworkAvailable(getContext()))
        {
            Toast.makeText(getContext(), "No internet", Toast.LENGTH_SHORT).show();
        }

    }//setUpRelieverLayout closes

    //method for showing date
    public String showDate(int yy, int mm, int dd)
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

        String selectedDate=day+"-"+month+"-"+year;
        Utils.appEditor.putString("TaskListDate", selectedDate).commit();

        //setting date to textview
        date.setText(selectedDate);

        return selectedDate;
    }//showDate closes


    //viewholder class for TaskAdapter2
    static class ViewHolder2
    {
        CheckBox checkBox;
        TextView task_name;
        TextView task_time;
        ImageView info;
    }

    //adapter class for tasklistview
    class TaskAdapter2 extends BaseAdapter
    {
        Context context;
        ArrayList<TasksModel2> tasklist;
        LayoutInflater inflater;
        Activity mActivity;

        public TaskAdapter2(Context context, Activity mActivity, ArrayList<TasksModel2> list)
        {
            this.mActivity=mActivity;
            this.context=context;
            tasklist=list;
            inflater=LayoutInflater.from(context);
            selectedTasks=new ArrayList<TasksModel2>();
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
        public View getView(final int position, View convertView, ViewGroup parent) {

            final ViewHolder2 holder;

            if(convertView==null)
            {
                holder=new ViewHolder2();

                convertView=inflater.inflate(R.layout.tasks_listitem2, null);

                holder.checkBox=(CheckBox)convertView.findViewById(R.id.task_checkbox);
                holder.task_name=(TextView)convertView.findViewById(R.id.task_name);
                holder.task_time=(TextView)convertView.findViewById(R.id.task_time);
                holder.info=(ImageView)convertView.findViewById(R.id.taskInfo);

                convertView.setTag(holder);
            }
            else
            {
                holder=(ViewHolder2)convertView.getTag();
            }

            //adding task item name to the item
            holder.task_name.setText(tasklist.get(position).getTaskName());

            //setting task time to item
            holder.task_time.setText(tasklist.get(position).getTaskTime());

            //setting checked or unchecked status of task-----for maintaining check type of item in list
            if(selectedTasks.contains(tasklist.get(position)))
            {
                holder.checkBox.setChecked(true);
            }
            else
            {
                holder.checkBox.setChecked(false);
            }

            //adding action to the info (sop) icon
            holder.info.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Intent i=new Intent(mActivity, Sop.class);

                    i.putExtra("sop_id", tasklist.get(position).getSopId());
                    i.putExtra("sop_name", tasklist.get(position).getSopName());
                    i.putExtra("sop_url", tasklist.get(position).getSopDescript());

                    startActivity(i);
                    mActivity.finish();
                }
            });

            //adding action to click on checkbox
            holder.checkBox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if(holder.checkBox.isChecked())
                    {
                        if(!selectedTasks.contains(tasklist.get(position)))
                        {
                            selectedTasks.add(tasklist.get(position));
                        }
                    }
                    else
                    if(!holder.checkBox.isChecked())
                    {
                        if(selectedTasks.contains(tasklist.get(position)))
                        {
                            selectedTasks.remove(tasklist.get(position));
                        }
                    }
                }
            });


            return convertView;
        }
    }//TaskAdapter2 closes

    //viewholder class for TaskAdapter3
    static class ViewHolder3
    {
        TextView task_name, task_time;
        ImageView info;
    }

    //adapter class for tasklistview for dates other than current date
    class TaskAdapter3 extends BaseAdapter
    {
        Context context;
        ArrayList<TasksModel2> tasklist;
        LayoutInflater inflater;
        Activity mActivity;

        public TaskAdapter3(Context context, Activity mActivity, ArrayList<TasksModel2> list)
        {
            this.mActivity=mActivity;
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
        public View getView(final int position, View convertView, ViewGroup parent) {

            ViewHolder3 holder;

            if(convertView==null)
            {
                holder=new ViewHolder3();

                convertView=inflater.inflate(R.layout.tasks_listitem3, null);

                holder.task_name=(TextView)convertView.findViewById(R.id.task_name2);
                holder.task_time=(TextView)convertView.findViewById(R.id.task_time2);
                holder.info=(ImageView)convertView.findViewById(R.id.taskInfo2);

                convertView.setTag(holder);
            }
            else
            {
                holder=(ViewHolder3)convertView.getTag();
            }

            //adding task item name to the item
            holder.task_name.setText(tasklist.get(position).getTaskName());

            //setting task time to item
            holder.task_time.setText(tasklist.get(position).getTaskTime());

            //adding action to the info (sop) icon
            holder.info.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Intent i=new Intent(mActivity, Sop.class);

                    i.putExtra("sop_id", tasklist.get(position).getSopId());
                    i.putExtra("sop_name", tasklist.get(position).getSopName());
                    i.putExtra("sop_url", tasklist.get(position).getSopDescript());

                    startActivity(i);
                    mActivity.finish();
                }
            });


            return convertView;
        }
    }//TaskAdapter3 closes


    //async task to obtain task list and sop list details
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
        int localRegularInsertTaskCount=0, localRelieverInsertTaskCount=0;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            taskfrag_progress.setVisibility(View.VISIBLE);

            date_selected=Utils.appPreferences.getString("TaskListDate", "");
            site_id=Utils.loginPreferences.getString("site_id", "");

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
                if(Utils.loginPreferences.getString("LoginType", "None").equalsIgnoreCase("Regular")
                        && Utils.appPreferences.getString("RegularLocalTasksPresent", "No").equalsIgnoreCase("Yes")
                        && Utils.appPreferences.getString("RegularCurrentDate", "").equalsIgnoreCase(date_selected))
                {
                    taskList=new ArrayList<TasksModel2>();

                    //loading data from regular local database
                    taskList=myRegularDB.getTasks();

                    //loading tasklist
                    loadTaskList(date_selected);
                }
                else
                if(Utils.loginPreferences.getString("LoginType", "None").equalsIgnoreCase("Reliever")
                        && Utils.appPreferences.getString("RelieverLocalTasksPresent", "No").equalsIgnoreCase("Yes"))
                {
                    taskList=new ArrayList<TasksModel2>();

                    //loading data from reliever local database
                    taskList=myRelieverDB.getTasks();

                    //loading tasklist
                    loadTaskList(date_selected);
                }
                else
                {
                    AlertDialog.Builder aDialog=new AlertDialog.Builder(getContext());
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
                taskList=new ArrayList<TasksModel2>();

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
                        String task_id="", task_name="", task_time="", sop_id="", sop_name="", sop_desc="";

                        JSONObject obj2=jsonResponse.getJSONObject(1);
                        JSONArray list=obj2.getJSONArray("tasklist");
                        int arrLength=list.length();

                        for(int i=0; i<arrLength; i++)
                        {
                            JSONObject listObj=list.getJSONObject(i);

                            task_id=listObj.getString("task_id");
                            task_name=listObj.getString("task_name");
                            task_time=listObj.getString("task_time");
                            sop_id=listObj.getString("sop_id");
                            sop_name=listObj.getString("sop_name");
                            sop_desc=listObj.getString("sop_description");

                            //saving task data in model class
                            TasksModel2 newTask=new TasksModel2();
                            newTask.setTaskId(task_id);
                            newTask.setTaskName(task_name);
                            newTask.setTaskTime(task_time);
                            newTask.setSopId(sop_id);

                            //checking if sop name is null
                            if(sop_name.equalsIgnoreCase("null"))
                            {
                                newTask.setSopName(task_name);
                            }
                            else
                            {
                                newTask.setSopName(sop_name);
                            }

                            //checking if sop description (url) is null
                            if(sop_desc.equalsIgnoreCase("null"))
                            {
                                newTask.setSopDescript("");
                            }
                            else
                            {
                                newTask.setSopDescript(sop_desc);
                            }

                            //adding task object to arraylist
                            taskList.add(newTask);

                            //adding data to regular local database
                            if(Utils.loginPreferences.getString("LoginType", "None").equalsIgnoreCase("Regular")
                                    && Utils.appPreferences.getString("RegularCurrentDate", "").equalsIgnoreCase(date_selected)
                                    && Utils.appPreferences.getString("RegularLocalTasksPresent", "No").equalsIgnoreCase("No"))
                            {
                                boolean checkInserted=myRegularDB.insertTask(task_id, task_name,
                                        task_time, sop_id, sop_name, sop_desc);

                                if(checkInserted==true)
                                {
                                    localRegularInsertTaskCount++;
                                }

                            }//adding to regular local db closes
                            else
                            //adding data to reliever local database
                            if(Utils.loginPreferences.getString("LoginType", "None").equalsIgnoreCase("Reliever")
                                    && Utils.appPreferences.getString("RelieverLocalTasksPresent", "No").equalsIgnoreCase("No"))
                            {
                                boolean checkInserted=myRelieverDB.insertTask(task_id, task_name,
                                        task_time, sop_id, sop_name, sop_desc);

                                if(checkInserted==true)
                                {
                                    localRelieverInsertTaskCount++;
                                }

                            }//adding to reliever local db closes

                        } //for closes


                    }


                }
                catch (JSONException je)
                {
                    je.printStackTrace();
                }

                //loading tasklists if status is 1
                if(status.equalsIgnoreCase("1"))
                {
                    loadTaskList(date_selected);

                    //checking whether data is successfully entered into local database
                    if(Utils.loginPreferences.getString("LoginType", "None").equalsIgnoreCase("Regular")
                            && Utils.appPreferences.getString("RegularCurrentDate", "").equalsIgnoreCase(date_selected)
                            && Utils.appPreferences.getString("RegularLocalTasksPresent", "No").equalsIgnoreCase("No"))
                    {
                        if(localRegularInsertTaskCount == taskList.size())
                        {
                            Utils.appEditor.putString("RegularLocalTasksPresent", "Yes").commit();
                        }
                        else
                        {
                            Utils.appEditor.putString("RegularLocalTasksPresent", "No").commit();

                            if(Utils.loginPreferences.getString("LoginType", "None").equalsIgnoreCase("Regular"))
                            {
                                myRegularDB.deleteTasks();
                            }

                        }
                    }
                    else
                    if(Utils.loginPreferences.getString("LoginType", "None").equalsIgnoreCase("Reliever")
                            && Utils.appPreferences.getString("RelieverLocalTasksPresent", "No").equalsIgnoreCase("No"))
                    {
                        if(localRelieverInsertTaskCount == taskList.size())
                        {
                            Utils.appEditor.putString("RelieverLocalTasksPresent", "Yes").commit();
                        }
                        else
                        {
                            Utils.appEditor.putString("RelieverLocalTasksPresent", "No").commit();

                            if(Utils.loginPreferences.getString("LoginType", "None").equalsIgnoreCase("Reliever"))
                            {
                                myRelieverDB.deleteTasks();
                            }
                        }
                    }


                }
                else
                if(status.equalsIgnoreCase("0"))
                {
                    Toast.makeText(getContext(), msg, Toast.LENGTH_LONG).show();
                }

            }//outer else closes

            taskfrag_progress.setVisibility(View.GONE);
        }

        //method to load tasklist for listview
        public void loadTaskList(String dt)
        {
            //for regular login
            if(Utils.loginPreferences.getString("LoginType", "None").equalsIgnoreCase("Regular"))
            {
                if(dt.equalsIgnoreCase(regular_currentDate))
                {
                    //showing buttons and currentDate_layout for regular layout
                    otherDate_layout.setVisibility(View.GONE);
                    currentDate_layout.setVisibility(View.VISIBLE);
                    extraWork.setVisibility(View.VISIBLE);
                    submit_tasks.setVisibility(View.VISIBLE);

                    //loading list
                    taskAdapter=new TaskAdapter2(getContext(), getActivity(), taskList);
                    taskListView.setAdapter(taskAdapter);
                }
                else
                {
                    //hiding buttons and otherDate_layout
                    currentDate_layout.setVisibility(View.GONE);
                    otherDate_layout.setVisibility(View.VISIBLE);
                    extraWork.setVisibility(View.GONE);
                    submit_tasks.setVisibility(View.GONE);

                    //loading list
                    taskAdapter3=new TaskAdapter3(getContext(), getActivity(), taskList);
                    taskListView.setAdapter(taskAdapter3);
                }
            }
            else
            //for reliever login
            if(Utils.loginPreferences.getString("LoginType", "None").equalsIgnoreCase("Reliever"))
            {
                //loading list
                taskAdapter=new TaskAdapter2(getContext(), getActivity(), taskList);
                taskListView.setAdapter(taskAdapter);
            }

        } //loadTaskList() closes
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
        String employee_id="";
        String reliever_id="";
        ArrayList<TasksModel2> workDone;
        int progressStep=0;
        int count=0;
        String finalMsg="";

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            TasksFragment.this.pDialog=new ProgressDialog(getContext());
            TasksFragment.this.pDialog.setTitle("Sending request...");
            TasksFragment.this.pDialog.setCancelable(false);
            TasksFragment.this.pDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            TasksFragment.this.pDialog.setMax(100);
            TasksFragment.this.pDialog.setProgress(0);
            TasksFragment.this.pDialog.show();

            site_id=Utils.loginPreferences.getString("site_id", "");

            if(Utils.loginPreferences.getString("LoginType", "None").equalsIgnoreCase("Regular"))
            {
                employee_id=Utils.loginPreferences.getString("emp_id", "");
                reliever_id="0";
            }
            else
            if(Utils.loginPreferences.getString("LoginType", "None").equalsIgnoreCase("Reliever"))
            {
                employee_id=Utils.loginPreferences.getString("RelieverSubEmployeeId", "");
                reliever_id=Utils.loginPreferences.getString("emp_id", "");
            }

            workDone=selectedTasks;

            if(workDone.size() > 0)
            {
                progressStep=100/workDone.size();
            }

        }


        @Override
        protected Void doInBackground(String... strings) {

            for(TasksModel2 obj:workDone)
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
                            + URLEncoder.encode("employee_id", "UTF-8") + "="
                            + URLEncoder.encode(employee_id, "UTF-8") + "&"
                            + URLEncoder.encode("remarks", "UTF-8") + "=";

                    if(task_id.equalsIgnoreCase("0"))
                    {
                        if(Utils.loginPreferences.getString("LoginType", "None").equalsIgnoreCase("Regular"))
                        {
                            data+=URLEncoder.encode(extraWorkDoneRegular, "UTF-8") + "&"
                                    + URLEncoder.encode("reliever_id", "UTF-8") + "=";
                        }
                        else
                        if(Utils.loginPreferences.getString("LoginType", "None").equalsIgnoreCase("Reliever"))
                        {
                            data+=URLEncoder.encode(extraWorkDoneReliever, "UTF-8") + "&"
                                    + URLEncoder.encode("reliever_id", "UTF-8") + "=";
                        }
                    }
                    else
                    {
                        data+=URLEncoder.encode("", "UTF-8") + "&"
                                + URLEncoder.encode("reliever_id", "UTF-8") + "=";
                    }

                    data+=URLEncoder.encode(reliever_id, "UTF-8") + "&"
                            + URLEncoder.encode("temp_reliever_id", "UTF-8") + "="
                            + URLEncoder.encode("0", "UTF-8");
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
                    finalMsg+="Error at "+obj.getTaskName()+"\n";
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

            TasksFragment.this.pDialog.setProgress(values[0]);
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            //closing progress dialog
            TasksFragment.this.pDialog.dismiss();

            if(error!=null)
            {
                AlertDialog.Builder aDialog=new AlertDialog.Builder(getContext());
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
                Toast.makeText(getContext(), finalMsg, Toast.LENGTH_LONG).show();
            }
            else
            {
                Toast.makeText(getContext(), "Tasks submitted", Toast.LENGTH_LONG).show();

                //re-initialising extra work done and selectedTasks list
                if(Utils.loginPreferences.getString("LoginType", "None").equalsIgnoreCase("Regular"))
                {
                    extraWorkDoneRegular="";
                    Utils.appEditor.putString("ExtraWorkDoneRegular", "").commit();
                }
                else
                if(Utils.loginPreferences.getString("LoginType", "None").equalsIgnoreCase("Reliever"))
                {
                    extraWorkDoneReliever="";
                    Utils.appEditor.putString("ExtraWorkDoneReliever", "").commit();
                }

                selectedTasks=new ArrayList<TasksModel2>();

            }

        }
    }//asynctask closes


    //asynctask to display certifications list of the employee
    private class ViewCertificationsTask extends AsyncTask<String, Integer, Void>
    {
        String content="";
        String error=null;
        String data="";
        String msg="";
        String status="";
        String employee_id="";
        int localRegularInsertCertCount=0, localRelieverInsertCertCount=0;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            employee_id= Utils.loginPreferences.getString("emp_id", "");
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

            String training_id="", training_name="";

            if(error == null)
            {
                certList=new ArrayList<CertificationModel>();

                try
                {
                    //retrieving data from webservice
                    JSONArray jsonResponse=new JSONArray(content);
                    JSONObject obj1=jsonResponse.getJSONObject(0);
                    status=obj1.getString("status");
                    msg=obj1.getString("msg");

                    if(status.equalsIgnoreCase("1"))
                    {
                        JSONObject obj2=jsonResponse.getJSONObject(1);
                        JSONArray arrayObj=obj2.getJSONArray("certificates");
                        int arrLength=arrayObj.length();

                        for(int i=0; i<arrLength; i++)
                        {
                            JSONObject certObj=arrayObj.getJSONObject(i);
                            training_id=certObj.getString("traning_id");
                            training_name=certObj.getString("traning_name");

                            //adding data to new object
                            CertificationModel newCert=new CertificationModel();
                            newCert.setCertId(training_id);
                            newCert.setCertName(training_name);

                            //adding object to list
                            certList.add(newCert);

                            //adding data to regular local database
                            if(Utils.loginPreferences.getString("LoginType", "None").equalsIgnoreCase("Regular")
                                    && Utils.appPreferences.getString("RegularLocalCertificationPresent", "No").equalsIgnoreCase("No"))
                            {
                                boolean checkInserted=myRegularDB.insertCertification(training_id, training_name);

                                if(checkInserted==true)
                                {
                                    localRegularInsertCertCount++;
                                }

                            }//adding to regular local db closes
                            else
                                //adding data to reliever local database
                            if(Utils.loginPreferences.getString("LoginType", "None").equalsIgnoreCase("Reliever")
                                    && Utils.appPreferences.getString("RelieverLocalCertificationPresent", "No").equalsIgnoreCase("No"))
                            {
                                boolean checkInserted=myRelieverDB.insertCertification(training_id, training_name);

                                if(checkInserted==true)
                                {
                                    localRelieverInsertCertCount++;
                                }

                            }//adding to reliever local db closes
                        }//for closes
                    }

                }//try closes
                catch (JSONException je)
                {
                    je.printStackTrace();
                }

                if(status.equalsIgnoreCase("1"))
                {
                    //checking whether data is successfully entered into local database
                    if(Utils.loginPreferences.getString("LoginType", "None").equalsIgnoreCase("Regular")
                            && Utils.appPreferences.getString("RegularLocalCertificationPresent", "No").equalsIgnoreCase("No"))
                    {
                        if(localRegularInsertCertCount == certList.size())
                        {
                            Utils.appEditor.putString("RegularLocalCertificationPresent", "Yes").commit();
                        }
                        else
                        {
                            Utils.appEditor.putString("RegularLocalCertificationPresent", "No").commit();

                            if(Utils.loginPreferences.getString("LoginType", "None").equalsIgnoreCase("Regular"))
                            {
                                myRegularDB.deleteCertifications();
                            }
                        }
                    }
                    else
                    if(Utils.loginPreferences.getString("LoginType", "None").equalsIgnoreCase("Reliever")
                            && Utils.appPreferences.getString("RelieverLocalCertificationPresent", "No").equalsIgnoreCase("No"))
                    {
                        if(localRelieverInsertCertCount == certList.size())
                        {
                            Utils.appEditor.putString("RelieverLocalCertificationPresent", "Yes").commit();
                        }
                        else
                        {
                            Utils.appEditor.putString("RelieverLocalCertificationPresent", "No").commit();

                            if(Utils.loginPreferences.getString("LoginType", "None").equalsIgnoreCase("Reliever"))
                            {
                                myRelieverDB.deleteCertifications();
                            }
                        }
                    }

                }
            }//outer else closes
        }
    }//asynctask closes


    //asynctask to retrieve training questions if any training was requested
    private class TrainingQuestionsTask extends AsyncTask<String, Void, Void>
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

            taskfrag_progress.setVisibility(View.VISIBLE);

            employee_id=Utils.loginPreferences.getString("emp_id", "");
        }


        @Override
        protected Void doInBackground(String... strings) {

            try
            {
                data=URLEncoder.encode("employee_id", "UTF-8") + "="
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

            String training_id="", training_name="", question_id="", question="", answer1="", answer2="",
                    answer3="", answer4="", correct_answer="", video_title="";

            if(error!=null)
            {
                AlertDialog.Builder aDialog=new AlertDialog.Builder(getContext());
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
                Utils.requestedTrainingList=new ArrayList<TrainingModel>();

                Utils.questionList=new ArrayList<QuestionModel>();

                //retrieving data from webservice
                try
                {
                    JSONArray jsonResponse=new JSONArray(content);
                    JSONObject obj1=jsonResponse.getJSONObject(0);
                    status=obj1.getString("status");
                    msg=obj1.getString("msg");

                    if(status.equalsIgnoreCase("1"))
                    {
                        JSONArray arrayObj=jsonResponse.getJSONArray(1);
                        int arrLength1=arrayObj.length();

                        for(int i=0; i<arrLength1; i++)
                        {
                            JSONObject trainingObj=arrayObj.getJSONObject(i);
                            training_id=trainingObj.getString("training_id");
                            training_name=trainingObj.getString("traning_name");
                            video_title=trainingObj.getString("video_title");


                            JSONArray questionArray=trainingObj.getJSONArray("question_list");
                            int arrLength2=questionArray.length();

                            //adding training details to object
                            TrainingModel newTraining=new TrainingModel();
                            newTraining.setTrainingId(training_id);
                            newTraining.setTrainingName(training_name);
                            newTraining.setTrainingVideo(video_title);
                            newTraining.setTotalQuestions(arrLength2);

                            //adding object to list
                            Utils.requestedTrainingList.add(newTraining);


                            for(int j=0; j<arrLength2; j++)
                            {
                                JSONObject questionObj=questionArray.getJSONObject(j);
                                question_id=questionObj.getString("question_id");
                                question=questionObj.getString("question");
                                answer1=questionObj.getString("answer1");
                                answer2=questionObj.getString("answer2");
                                answer3=questionObj.getString("answer3");
                                answer4=questionObj.getString("answer4");
                                correct_answer=questionObj.getString("correct_answer");

                                //adding data to object
                                QuestionModel newQuestion=new QuestionModel();
                                newQuestion.setTrainingId(training_id);
                                newQuestion.setQuestionId(question_id);
                                newQuestion.setQuestion(question);
                                newQuestion.setAnswer1(answer1);
                                newQuestion.setAnswer2(answer2);
                                newQuestion.setAnswer3(answer3);
                                newQuestion.setAnswer4(answer4);
                                newQuestion.setCorrect_answer(correct_answer);

                                //adding object to list
                                Utils.questionList.add(newQuestion);

                            }//inner for closes

                            //re-initialising training details
                            training_id="";
                            training_name="";

                        }//outer for closes
                    }
                }//try closes
                catch (JSONException je)
                {
                    je.printStackTrace();
                }

                if(status.equalsIgnoreCase("1"))
                {
                    if(!Utils.requestedTrainingList.isEmpty())
                    {
                        FragmentManager fm=getFragmentManager();
                        FragmentTransaction transaction=fm.beginTransaction();
                        transaction.add(R.id.question_frag, new TrainingQuestionFragment());
                        transaction.commit();
                    }
                }
                else
                if(status.equalsIgnoreCase("2"))
                {
                    //add code
                }
                else
                if(status.equalsIgnoreCase("0"))
                {
                    //add code
                }
            }//outer else closes

            taskfrag_progress.setVisibility(View.GONE);
        }
    }//asynctask closes

}
