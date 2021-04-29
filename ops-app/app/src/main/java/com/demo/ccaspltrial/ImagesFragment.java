package com.demo.ccaspltrial;

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
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.demo.ccaspltrial.Utility.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.Calendar;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ImagesFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ImagesFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ImagesFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    //parameters required for this fragment
    ConstraintLayout selfie_layout, siteImage_layout, challan_layout, attendance_layout;
    ImageView selfie, siteImage, challan, attendance;
    ProgressBar imgfrag_progress;


    private OnFragmentInteractionListener mListener;

    public ImagesFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ImagesFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ImagesFragment newInstance(String param1, String param2) {
        ImagesFragment fragment = new ImagesFragment();
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
        return inflater.inflate(R.layout.fragment_images, container, false);
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


    //working of the views in the fragment

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        selfie_layout=(ConstraintLayout)view.findViewById(R.id.selfie_layout);
        siteImage_layout=(ConstraintLayout)view.findViewById(R.id.siteImage_layout);
        challan_layout=(ConstraintLayout)view.findViewById(R.id.challan_layout);
        attendance_layout=(ConstraintLayout)view.findViewById(R.id.attendance_layout);
        selfie=(ImageView)view.findViewById(R.id.selfie);
        siteImage=(ImageView)view.findViewById(R.id.siteImage);
        challan=(ImageView)view.findViewById(R.id.challan);
        attendance=(ImageView)view.findViewById(R.id.attendance);
        imgfrag_progress=(ProgressBar)view.findViewById(R.id.imgfrag_progress);

        //initialising sharedpreferences
        Utils.loginPreferences=getActivity().getSharedPreferences(Utils.loginPref, Context.MODE_PRIVATE);
        Utils.appPreferences=getActivity().getSharedPreferences(Utils.appPref, Context.MODE_PRIVATE);
        Utils.appEditor=Utils.appPreferences.edit();

        //setting image on selfie_layout
        if(!Utils.loginPreferences.getString("emp_id", "").equalsIgnoreCase("")
                && Utils.isNetworkAvailable(getContext()))
        {
            new LatestSelfieTask().execute(Utils.latestselfieimageURL);
        }


        //setting image on siteImage_layout
        if(!Utils.loginPreferences.getString("site_id", "").equalsIgnoreCase("")
                && Utils.isNetworkAvailable(getContext()))
        {
            new LatestSiteImageTask().execute(Utils.latestsiteimageURL);
        }


        //setting image on challan_layout
        if(!Utils.loginPreferences.getString("site_id", "").equalsIgnoreCase("")
                && Utils.isNetworkAvailable(getContext()))
        {
            new LatestChallanTask().execute(Utils.latestdeliverychallanimageURL);
        }


        //setting image on attendance_layout
        if(!Utils.loginPreferences.getString("site_id", "").equalsIgnoreCase("")
                && Utils.isNetworkAvailable(getContext()))
        {
            new LatestAttendanceTask().execute(Utils.latestattendanceimageURL);
        }


        if(Utils.loginPreferences.getString("site_id", "").equalsIgnoreCase(""))
        {
            Toast.makeText(getContext(), "No site assigned", Toast.LENGTH_LONG).show();
        }
        else
        if(!Utils.isNetworkAvailable(getContext()))
        {
            Toast.makeText(getContext(), "No internet", Toast.LENGTH_SHORT).show();
        }

        //adding action to selfie_layout click
        selfie_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Utils.selfieFlag=1;
                Utils.siteFlag=0;
                Utils.challanFlag=0;
                Utils.attendanceFlag=0;
                Utils.appEditor.putString("ImageType", "Selfie").commit();

                //opening gallery activity
                startActivity(new Intent(getContext(), ImageGallery.class));
                getActivity().finish();
            }
        });


        //adding action to siteImage_layout click
        siteImage_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Utils.selfieFlag=0;
                Utils.siteFlag=1;
                Utils.challanFlag=0;
                Utils.attendanceFlag=0;
                Utils.appEditor.putString("ImageType", "Site").commit();

                //Getting current date
                Calendar calendar=Calendar.getInstance();
                final int year1=calendar.get(Calendar.YEAR);
                final int month1=calendar.get(Calendar.MONTH);
                final int day1=calendar.get(Calendar.DAY_OF_MONTH);

                //opening datepickerdialog
                DatePickerDialog pickerDialog=new DatePickerDialog(getContext(), new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {

                        String date=selectDate(year, month, dayOfMonth);

                        Intent imageIntent=new Intent(getContext(), ImageGallery.class);

                        //sending selected date
                        imageIntent.putExtra("SelectedDate", date);

                        //opening gallery activity
                        startActivity(imageIntent);
                        getActivity().finish();
                    }
                }, year1, month1, day1);
                pickerDialog.show();
            }
        });


        //adding action to challan_layout
        challan_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Utils.selfieFlag=0;
                Utils.siteFlag=0;
                Utils.challanFlag=1;
                Utils.attendanceFlag=0;
                Utils.appEditor.putString("ImageType", "Challan").commit();

                //opening gallery activity
                startActivity(new Intent(getContext(), ImageGallery.class));
                getActivity().finish();
            }
        });


        //adding action to attendance_layout click
        attendance_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Utils.selfieFlag=0;
                Utils.siteFlag=0;
                Utils.challanFlag=0;
                Utils.attendanceFlag=1;
                Utils.appEditor.putString("ImageType", "Attendance").commit();

                //opening gallery activity
                startActivity(new Intent(getContext(), ImageGallery.class));
                getActivity().finish();
            }
        });


    }

    //method to convert selected date to String
    public String selectDate(int year, int month, int dayOfMonth)
    {
        String yy, mm, dd, selectedDate;

        yy=""+year;
        month=month+1;
        if(month<10)
        {
            mm="0"+month;
        }
        else
        {
            mm=""+month;
        }
        if(dayOfMonth<10)
        {
            dd="0"+dayOfMonth;
        }
        else
        {
            dd=""+dayOfMonth;
        }

        selectedDate=dd+"-"+mm+"-"+yy;

        return selectedDate;
    }


    //asynctask to retrieve latest site image url
    private class LatestSelfieTask extends AsyncTask<String, Void, Void>
    {
        String content="";
        String error=null;
        String data="";
        String msg="";
        String status="";
        String employee_id="";
        String imageUrl="";
        Bitmap selfieBitmap =null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            imgfrag_progress.setVisibility(View.VISIBLE);

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

            //retrieving data from webservice
            try
            {
                JSONObject jsonResponse=new JSONObject(content);
                status=jsonResponse.getString("status");
                msg=jsonResponse.getString("msg");

                if(status.equalsIgnoreCase("1"))
                {
                    imageUrl=jsonResponse.getString("url");
                    Utils.appEditor.putString("LastSelfieURL", imageUrl).commit();
                    URL url = new URL(imageUrl);
                    selfieBitmap=BitmapFactory.decodeStream(url.openConnection().getInputStream());
                }
            }
            catch (JSONException je)
            {
                je.printStackTrace();
            }
            catch (IOException ioe)
            {
                ioe.printStackTrace();
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
                if(status.equalsIgnoreCase("1") && selfieBitmap !=null)
                {
                    selfie.setImageBitmap(selfieBitmap);
                    HomePage.emp_profileImg.setImageBitmap(selfieBitmap);
                }
                else
                if(status.equalsIgnoreCase("0"))
                {
                    Toast.makeText(getContext(), msg, Toast.LENGTH_LONG).show();
                }
            }//outer else closes

            imgfrag_progress.setVisibility(View.GONE);
        }

    }//asynctask closes


    //asynctask to retrieve latest site image url
    private class LatestSiteImageTask extends AsyncTask<String, Void, Void>
    {
        String content="";
        String error=null;
        String data="";
        String msg="";
        String status="";
        String site_id="";
        String imageUrl="";
        Bitmap siteImageBitmap=null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            site_id=Utils.loginPreferences.getString("site_id", "");
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

            //retrieving data from webservice
            try
            {
                JSONObject jsonResponse=new JSONObject(content);
                status=jsonResponse.getString("status");
                msg=jsonResponse.getString("msg");

                if(status.equalsIgnoreCase("1"))
                {
                    imageUrl=jsonResponse.getString("url");
                    Utils.appEditor.putString("LastSiteImageURL", imageUrl).commit();
                    URL url = new URL(imageUrl);
                    siteImageBitmap=BitmapFactory.decodeStream(url.openConnection().getInputStream());
                }
            }
            catch (JSONException je)
            {
                je.printStackTrace();
            }
            catch (IOException ioe)
            {
                ioe.printStackTrace();
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
                if(status.equalsIgnoreCase("1") && siteImageBitmap!=null)
                {
                    siteImage.setImageBitmap(siteImageBitmap);
                }
                else
                if(status.equalsIgnoreCase("0"))
                {
                    Toast.makeText(getContext(), msg, Toast.LENGTH_LONG).show();
                }
            }//outer else closes
        }

    }//asynctask closes


    //asynctask to retrieve latest site image url
    private class LatestChallanTask extends AsyncTask<String, Void, Void>
    {
        String content="";
        String error=null;
        String data="";
        String msg="";
        String status="";
        String site_id="";
        String imageUrl="";
        Bitmap challanBitmap =null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();


            site_id=Utils.loginPreferences.getString("site_id", "");
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

            //retrieving data from webservice
            try
            {
                JSONObject jsonResponse=new JSONObject(content);
                status=jsonResponse.getString("status");
                msg=jsonResponse.getString("msg");

                if(status.equalsIgnoreCase("1"))
                {
                    imageUrl=jsonResponse.getString("url");
                    Utils.appEditor.putString("LastChallanURL", imageUrl).commit();
                    URL url = new URL(imageUrl);
                    challanBitmap =BitmapFactory.decodeStream(url.openConnection().getInputStream());
                }
            }
            catch (JSONException je)
            {
                je.printStackTrace();
            }
            catch (IOException ioe)
            {
                ioe.printStackTrace();
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
                if(status.equalsIgnoreCase("1") && challanBitmap !=null)
                {
                    challan.setImageBitmap(challanBitmap);
                }
                else
                if(status.equalsIgnoreCase("0"))
                {
                    Toast.makeText(getContext(), msg, Toast.LENGTH_LONG).show();
                }
            }//outer else closes
        }

    }//asynctask closes


    //asynctask to retrieve latest site image url
    private class LatestAttendanceTask extends AsyncTask<String, Void, Void>
    {
        String content="";
        String error=null;
        String data="";
        String msg="";
        String status="";
        String site_id="";
        String imageUrl="";
        Bitmap attendanceBitmap=null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            site_id=Utils.loginPreferences.getString("site_id", "");
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

            //retrieving data from webservice
            try
            {
                JSONObject jsonResponse=new JSONObject(content);
                status=jsonResponse.getString("status");
                msg=jsonResponse.getString("msg");

                if(status.equalsIgnoreCase("1"))
                {
                    imageUrl=jsonResponse.getString("url");
                    Utils.appEditor.putString("LastAttendanceURL", imageUrl).commit();
                    URL url = new URL(imageUrl);
                    attendanceBitmap=BitmapFactory.decodeStream(url.openConnection().getInputStream());
                }
            }
            catch (JSONException je)
            {
                je.printStackTrace();
            }
            catch (IOException ioe)
            {
                ioe.printStackTrace();
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
                if(status.equalsIgnoreCase("1") && attendanceBitmap!=null)
                {
                    attendance.setImageBitmap(attendanceBitmap);
                }
                else
                if(status.equalsIgnoreCase("0"))
                {
                    Toast.makeText(getContext(), msg, Toast.LENGTH_LONG).show();
                }
            }//outer else closes
        }

    }//asynctask closes


}
