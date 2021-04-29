package com.demo.ccaspltrial;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.demo.ccaspltrial.Utility.MaterialDeliModel;
import com.demo.ccaspltrial.Utility.MaterialsModel;
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

import static java.security.AccessController.getContext;

public class MaterialDelivered extends AppCompatActivity {

    Toolbar toolbar;
    ListView matdel_listview;
    TextView matdel_display_month_year;
    ProgressBar matdel_progress;
    Button submit_month;
    String month_year_value;


    static ArrayList<MaterialDeliModel> materialList;
    MaterialDelivered.MatdeliAdapter adapter;


    ArrayList<MaterialDeliModel> deliveredMaterial;

    ProgressDialog pDialog;

    Context context;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_material_delivered);
        toolbar=(Toolbar)findViewById(R.id.toolbar);
        matdel_listview=(ListView)findViewById(R.id.matdel_listview);
        matdel_progress=(ProgressBar)findViewById(R.id.matdel_progress);
        submit_month=(Button)findViewById(R.id.matdel_month_year_select);
        matdel_display_month_year=(TextView)findViewById(R.id.matdel_display_month_year);
        setSupportActionBar(toolbar);
        toolbar.setTitle("Materials Delivered");
        context=this;
        deliveredMaterial=new ArrayList<MaterialDeliModel>();

        materialList=new ArrayList<MaterialDeliModel>();


        //set default month-year
        matdel_display_month_year.setText(mon_yr());

        //select month-year

        matdel_display_month_year.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int mYear, mMonth, mDay, mHour, mMinute;

                final Calendar c = Calendar.getInstance();
                mYear = c.get(Calendar.YEAR);
                mMonth = c.get(Calendar.MONTH);
                mDay = c.get(Calendar.DAY_OF_MONTH);
                final String arr_month_name[]={"Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec"};

                DatePickerDialog monthDatePickerDialog = new DatePickerDialog(context,
                        AlertDialog.THEME_HOLO_LIGHT, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        matdel_display_month_year.setText(arr_month_name[month] + "/" + year);
                        month_year_value=String.valueOf((month + 1))+'/'+String.valueOf(year);


                    }
                }, mYear, mMonth, mDay){
                    @Override
                    protected void onCreate(Bundle savedInstanceState) {
                        super.onCreate(savedInstanceState);
                        getDatePicker().findViewById(getResources().getIdentifier("day","id","android")).setVisibility(View.GONE);
                    }
                };
                monthDatePickerDialog.setTitle("select_month");
                monthDatePickerDialog.show();


            }
        });

        submit_month.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Toast.makeText(context, "submit button clicked", Toast.LENGTH_SHORT).show();
                materialList.clear();
                new MaterialDeliveredTask().execute(Utils.materialdeliveredURL);

            }
        });


    }

    private String mon_yr() {

        //get current month and year.
        Calendar calendar = Calendar.getInstance();

        int thisYear = calendar.get(Calendar.YEAR);
        //Log.d(TAG, "# thisYear : " + thisYear);

        int thisMonth = calendar.get(Calendar.MONTH);//Month count start from 0
        // Log.d(TAG, "@ thisMonth : " + thisMonth);

        String arr_month_name[]={"Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec"};

        String month_year = arr_month_name[thisMonth]+"/"+String.valueOf(thisYear);
        month_year_value=String.valueOf(thisMonth+1)+'/'+String.valueOf(thisYear);

        return month_year;
    }


//asynctask start
private class MaterialDeliveredTask extends AsyncTask<String, Void, Void> {
    String error = "";
    String content = "";
    String status = "";
    String msg = "";
    String data = "";
    String site_id="";
    String deli_id="";



    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        //Toast.makeText(context, month_year_value, Toast.LENGTH_SHORT).show();

    }

    @Override
    protected Void doInBackground(String... strings) {
        site_id=Utils.loginPreferences.getString("site_id", "");
       // site_id="S754";
        try {
            data= URLEncoder.encode("site_id", "UTF-8") + "="
                    + URLEncoder.encode(site_id, "UTF-8") + "&"
                    + URLEncoder.encode("requested_month", "UTF-8") + "="
                    + URLEncoder.encode(month_year_value, "UTF-8");


        } catch (UnsupportedEncodingException uee) {
            uee.printStackTrace();
        }

        BufferedReader br = null;
        try {
            URL url = new URL(strings[0]);
            URLConnection conn = url.openConnection();
            conn.setDoOutput(true);
            OutputStreamWriter outWr = new OutputStreamWriter(conn.getOutputStream());
            outWr.write(data);
            outWr.flush();
            br = new BufferedReader(new InputStreamReader(conn.getInputStream()));

            StringBuilder sb = new StringBuilder();
            String line = null;
            Log.d("TAG", "doInBackground() called with: strings = [" + data + "]");

            //reading server response
            while ((line = br.readLine()) != null) {
                sb.append(line + "\n");
            }

            content = sb.toString();
            Log.d("TAG1", "doInBackground() called with: strings = [" + content + "]");

        } catch (Exception e) {
            error = e.getMessage();
        } finally {

            try {
                br.close();
            } catch (Exception e) {
            }
        }

        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);

        String material_deliver_id = "";
        String material_deliver_code = "";
        String material_deliver_name = "";




        deliveredMaterial = new ArrayList<MaterialDeliModel>();

        try {
            JSONArray jsonResponse = new JSONArray(content);
            //System.out.print(jsonResponse);
            JSONObject obj1 = jsonResponse.getJSONObject(0);
            status = obj1.getString("status");
            msg = obj1.getString("msg");

            if (status.equalsIgnoreCase("1")) {
                JSONObject obj2 = jsonResponse.getJSONObject(1);
                JSONArray jsonArray = obj2.getJSONArray("material_req");
                int arrLength = jsonArray.length();
//                Toast.makeText(context, jsonArray.length(), Toast.LENGTH_SHORT).show();

                for (int i = 0; i < arrLength; i++) {
                    JSONObject pendingtaskdata = jsonArray.getJSONObject(i);

                    //Toast.makeText(context, "pendingtaskdata"+pendingtaskdata, Toast.LENGTH_SHORT).show();
                    material_deliver_id = pendingtaskdata.getString("material_delivered_id");
                    material_deliver_code = pendingtaskdata.getString("material_code");
                    material_deliver_name = pendingtaskdata.getString("material_name");


                    //adding data to model class
                    MaterialDeliModel newdeli = new MaterialDeliModel();
                    newdeli.setMaterialDelId(material_deliver_id);
                    newdeli.setMaterialDelCode(material_deliver_code);
                    newdeli.setMaterialDelName(material_deliver_name);


                    //adding material to arraylist
                    materialList.add(newdeli);


                }//for closes

            }

        } catch (JSONException je) {
            je.printStackTrace();
        }

        //loading material list if data retrieval is successful
        if (status.equalsIgnoreCase("1")) {
            //calling adapter for setting material list
            adapter = new MatdeliAdapter(context,materialList);
            matdel_listview.setAdapter(adapter);


        }
        else{
            Toast.makeText(context, "No Material Requested", Toast.LENGTH_SHORT).show();
        }



    }
}
//asynctask closed
static class MyViewHolder{
    TextView code,name;
    TextView delivered;
}

     class MatdeliAdapter extends BaseAdapter {
String deli_id;
        ArrayList<MaterialDeliModel> row;

        public MatdeliAdapter(Context context, ArrayList<MaterialDeliModel> rowData) {
            this.row=rowData;
        }

        @Override
        public int getCount() {
            return row.size();
        }

        @Override
        public Object getItem(int position) {
            return row.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

         @Override
         public int getItemViewType(int position) {
             return position;
         }

         @Override
         public int getViewTypeCount() {
             return getCount();
         }

         @Override
        public View getView(final int position, View convertView, ViewGroup parent) {


          final  MyViewHolder myViewHolder;

            if(convertView == null){
                LayoutInflater inflator=(LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

                convertView=inflator.inflate(R.layout.matderli_listitem,null);
                myViewHolder=new MyViewHolder();

                myViewHolder.code=(TextView)convertView.findViewById(R.id.matdeli_material_code);
                myViewHolder.name=(TextView)convertView.findViewById(R.id.matdeli_material_name);
                myViewHolder.delivered=(TextView)convertView.findViewById(R.id.matdeli_button);


                MaterialDeliModel data_deli=row.get(position);
                myViewHolder.code.setText(data_deli.getMaterialDelCode());
                myViewHolder.name.setText(data_deli.getMaterialDelName());
                myViewHolder.delivered.setText("Delivered");

                deli_id=data_deli.getMaterialDelId();


                convertView.setTag(myViewHolder);


            }
            else{
                myViewHolder=(MyViewHolder)convertView.getTag();
            }

             convertView.setOnClickListener(new View.OnClickListener() {
                 @Override
                 public void onClick(View v) {
                 deli_id=row.get(position).getMaterialDelId();
                     //Toast.makeText(context, deli_id, Toast.LENGTH_SHORT).show();
                     new MaterialDeliveredConfirm().execute(Utils.materialdeliconfURL,deli_id);

                 }
             });
            return convertView;
        }
    }

    private class MaterialDeliveredConfirm extends AsyncTask<String, Void, Void> {
        String error = "";
        String content = "";
        String status = "";
        String msg = "";
        String data = "";
        String x="";


        @Override
        protected void onPreExecute() {
            super.onPreExecute();


        }

        @Override
        protected Void doInBackground(String... strings) {
//        site_id=Utils.loginPreferences.getString("site_id", "");
            x=strings[1];
            try {
                data= URLEncoder.encode("material_delivered_id", "UTF-8") + "="
                        + URLEncoder.encode(x, "UTF-8");


            } catch (UnsupportedEncodingException uee) {
                uee.printStackTrace();
            }

            BufferedReader br = null;
            try {
                URL url = new URL(strings[0]);
                URLConnection conn = url.openConnection();
                conn.setDoOutput(true);
                OutputStreamWriter outWr = new OutputStreamWriter(conn.getOutputStream());
                outWr.write(data);
                outWr.flush();
                br = new BufferedReader(new InputStreamReader(conn.getInputStream()));

                StringBuilder sb = new StringBuilder();
                String line = null;

                //reading server response
                while ((line = br.readLine()) != null) {
                    sb.append(line + "\n");
                }

                content = sb.toString();

            } catch (Exception e) {
                error = e.getMessage();
            } finally {

                try {
                    br.close();
                } catch (Exception e) {
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

           // Toast.makeText(context, x, Toast.LENGTH_SHORT).show();

            materialList.clear();
            new MaterialDeliveredTask().execute(Utils.materialdeliveredURL);





            try {
                JSONArray jsonResponse = new JSONArray(content);
                //System.out.print(jsonResponse);
                JSONObject obj1 = jsonResponse.getJSONObject(0);
                status = obj1.getString("status");
                msg = obj1.getString("msg");



                if (status.equalsIgnoreCase("1")) {

                }

            } catch (JSONException je) {
                je.printStackTrace();
            }




        }
    }


}

