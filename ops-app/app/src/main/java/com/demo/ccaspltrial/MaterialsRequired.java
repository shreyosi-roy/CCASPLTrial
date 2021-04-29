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

import com.demo.ccaspltrial.LocalDatabase.RegularDatabaseHelper;
import com.demo.ccaspltrial.LocalDatabase.RelieverDatabaseHelper;
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
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class MaterialsRequired extends AppCompatActivity {

    Toolbar toolbar;
    Button required_nonbudgeted, required_budgeted,matreq_month_year_select;
    ListView matreq_listview;
    TextView totalAmtTV,matreq_display_month_year;
    ProgressBar matreq_progress;
    String month_year_value;

    static ArrayList<MaterialsModel> materialList;
    MatreqAdapter adapter;

    ArrayList<MaterialsModel> requiredMaterial;

    ProgressDialog pDialog;

    Context context;

    double total=0.00, totalDisplay=0.00;

    public static int pos=-1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_materials_required);

        toolbar=(Toolbar)findViewById(R.id.toolbar);
        required_nonbudgeted=(Button)findViewById(R.id.matreq_required_nonbudgeted);
        required_budgeted=(Button)findViewById(R.id.matreq_required_budgeted);
        matreq_listview=(ListView)findViewById(R.id.matreq_listview);
        matreq_progress=(ProgressBar)findViewById(R.id.matreq_progress);
        totalAmtTV=(TextView)findViewById(R.id.matreq_totalamount);
        matreq_display_month_year=(TextView)findViewById(R.id.matreq_display_month_year);
        matreq_month_year_select=(Button)findViewById(R.id.matreq_month_year_select);

        setSupportActionBar(toolbar);

        //setting toolbar title
        toolbar.setTitle("Materials Required");

        matreq_listview.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

        context=this;

        requiredMaterial=new ArrayList<MaterialsModel>();

        //initialising sharedpreferences
        Utils.loginPreferences=getSharedPreferences(Utils.loginPref, MODE_PRIVATE);
        Utils.pagePreferences=getSharedPreferences(Utils.pagePref, MODE_PRIVATE);
        Utils.pageEditor=Utils.pagePreferences.edit();

        totalAmtTV.setText("Rs 0.00");
        total=0.00;
        totalDisplay=0.00;


        //set default month-year
        matreq_display_month_year.setText(mon_yr());

        //select month-year

        matreq_month_year_select.setOnClickListener(new View.OnClickListener() {
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
                                                                    matreq_display_month_year.setText(arr_month_name[month] + "/" + year);
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





        //retrieving material list data and setting up list
        if(!Utils.loginPreferences.getString("site_id", "").equalsIgnoreCase("")
                && Utils.isNetworkAvailable(context))
        {
            new ViewMaterialListTask().execute(Utils.materiallistviewURL);
        }
        else
        if(Utils.loginPreferences.getString("site_id", "").equalsIgnoreCase(""))
        {
            Toast.makeText(context, "No site assigned", Toast.LENGTH_LONG).show();
        }
        else
        if(!Utils.isNetworkAvailable(context))
        {
            Toast.makeText(context, "No internet", Toast.LENGTH_SHORT).show();
        }

        //adding action to required_nonbudgeted button
        required_nonbudgeted.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //call to method to send list of required items
                requestMaterials(0); //budgetFlag=0 for non budgeted total
               // matreq_display_month_year.setText(mon_yr());

            }
        });

        //adding action to required_budgeted button
        required_budgeted.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //call to method to send list of required items
                requestMaterials(1); //budgetFlag=1 for budgeted total
                //matreq_display_month_year.setText(mon_yr());

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

    //method to calculate total amount
    public double calculateTotalRequestedAmount()
    {
        total=0.00;

        for(MaterialsModel obj : materialList)
        {
            double rate=0.00, amt=0.00;
            try
            {
                rate=Double.parseDouble(obj.getMaterialRate().trim());

            }
            catch (NumberFormatException nfe)
            {
                nfe.printStackTrace();
            }

            int quant=obj.getMatRequireQuantity();

            amt=(double)(rate*quant);

            total+=amt;
        }

        return total;

    }//calculateTotalRequestedAmount closes

    //method to check total for budgeted requests
    public boolean checkRequestedTotal()
    {
        double requestedTotal = calculateTotalRequestedAmount();

        double budget=0.0;
        try
        {
            budget=Double.parseDouble(Utils.appPreferences.getString("SiteBudget", "").trim());
        }
        catch (NumberFormatException nfe)
        {
            nfe.printStackTrace();
        }

        //checking if requested total is greater than budget
        if(requestedTotal > budget)
        {
            return false; //returning true if total is greater than budget
        }

        return true; //returning true if total is not greater than budget
    }//checkRequestedTotal closes

    //method to send list of required items
    public void requestMaterials(int budgetFlag)
    {
        String type="";
        if(budgetFlag == 1){
          type ="bud";
        }
        else{
            type="act";
        }
        //showing message if total requested amount is greater than budget for budgeted requests
        if(budgetFlag==1 && !checkRequestedTotal())
        {
            Toast.makeText(context, "Requested total amount is higher than budget!", Toast.LENGTH_SHORT).show();
            return;
        }

        if(requiredMaterial.size() > 0 && Utils.isNetworkAvailable(context) &&
                !Utils.loginPreferences.getString("site_id", "").equalsIgnoreCase(""))
        {
            new MaterialRequestedTask().execute(Utils.materialrequiredURL,type);
        }
        else
        if(requiredMaterial.size() <= 0)
        {
            Toast.makeText(context, "Select materials which are required", Toast.LENGTH_LONG).show();
        }
        else
        if(Utils.loginPreferences.getString("site_id", "").equalsIgnoreCase(""))
        {
            Toast.makeText(context, "No site assigned", Toast.LENGTH_LONG).show();
        }
        else
        if(!Utils.isNetworkAvailable(context))
        {
            Toast.makeText(context, "No internet", Toast.LENGTH_SHORT).show();
        }
    }//requestMaterials closes

    //method to calculate and display total amount
    public void displayTotalRequestedAmount(ArrayList<MaterialsModel> listofmaterials)
    {
        totalDisplay=0.00;

        for(MaterialsModel obj : listofmaterials)
        {
            double rate=0.00, amt=0.00;
            try
            {
                rate=Double.parseDouble(obj.getMaterialRate().trim());

            }
            catch (NumberFormatException nfe)
            {
                nfe.printStackTrace();
            }

            if(obj.getMaterialChecked() == 1)
            {
                int quant=obj.getMatRequireQuantity();

                amt=(double)(rate*quant);

                totalDisplay+=amt;
            }

        }

        Log.e("mattot-final:",""+totalDisplay );
        totalAmtTV.setText("Rs "+String.format("%.2f", totalDisplay));

    }//displayTotalRequestedAmount closes

    @Override
    public void onBackPressed() {

        startActivity(new Intent(MaterialsRequired.this, HomePage.class));
        finish();
    }


    //viewholder class for MatreqAdapter
    static class ViewHolder
    {
        CheckBox matreq_checkbox;
        TextView material_name;
        EditText matreq_quantity;
    }

    //adapter class for listview
    class MatreqAdapter extends BaseAdapter
    {
        ArrayList<MaterialsModel> material_list;
        Context context;
        LayoutInflater inflater;

        public MatreqAdapter(){}

        public MatreqAdapter(Context context, ArrayList<MaterialsModel> list)
        {
            this.context=context;
            material_list=list;
            inflater=LayoutInflater.from(context);
            requiredMaterial=new ArrayList<MaterialsModel>();
        }

        @Override
        public int getCount() {
            return material_list.size();
        }

        @Override
        public Object getItem(int position) {
            return material_list.get(position);
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

                convertView=inflater.inflate(R.layout.matreq_listitem, null);

                holder.matreq_checkbox=(CheckBox)convertView.findViewById(R.id.matreq_checkbox);
                holder.material_name=(TextView)convertView.findViewById(R.id.matreq_material_name);
                holder.matreq_quantity=(EditText)convertView.findViewById(R.id.matreq_quantity);

                convertView.setTag(holder);
            }
            else
            {
                holder=(ViewHolder)convertView.getTag();
            }

            holder.matreq_quantity.setInputType(InputType.TYPE_CLASS_NUMBER);

            //setting name of material in item-----for maintaining check type of item in list
            holder.material_name.setText(material_list.get(position).getMaterialName());

            //setting checked or unchecked status of material-----for maintaining check type of item in list
            int matCheckedFlag=0;
            for(MaterialsModel obj : requiredMaterial)
            {
                if(obj.getMaterialCode().equals(material_list.get(position).getMaterialCode()))
                {
                    int reqpos=requiredMaterial.indexOf(obj);
                    holder.matreq_checkbox.setChecked(true);
                    holder.matreq_quantity.setEnabled(true);
                    holder.matreq_quantity.setText(requiredMaterial.get(reqpos).getMatRequireQuantity()+"");
                    matCheckedFlag=1;
                    break;
                }
            }//for closes

            if(matCheckedFlag == 0)
            {
                holder.matreq_checkbox.setChecked(false);
                holder.matreq_quantity.setText("0");
                holder.matreq_quantity.setEnabled(false);
            }


            //adding action to click on material name
            holder.material_name.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    pos=position;

                    Utils.pageEditor.putString("MaterialCallingScreen", "MaterialRequest").commit();

                    FragmentManager fm=getSupportFragmentManager();
                    FragmentTransaction transaction=fm.beginTransaction();
                    transaction.add(R.id.matreq_details_frag, new MaterialDetailsFragment());
                    transaction.commit();
                }
            });

            //adding action to clicking of checkbox
            holder.matreq_checkbox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if(!holder.matreq_checkbox.isChecked())
                    {
                        //removing from required list if present
                        if(requiredMaterial.contains(material_list.get(position)))
                        {
                            requiredMaterial.remove(material_list.get(position));
                        }

                        holder.matreq_quantity.setText("0");
                        holder.matreq_quantity.setEnabled(false);

                        material_list.get(position).setMaterialChecked(0);

                        material_list.get(position).setMatRequireQuantity(0);

                        //calling method to calculate and display total amount for requested materials
                        displayTotalRequestedAmount(material_list);
                    }
                    else
                    if(holder.matreq_checkbox.isChecked())
                    {
                         if(requiredMaterial.contains(material_list.get(position)))
                         {
                            int reqpos=requiredMaterial.indexOf(material_list.get(position));
                            holder.matreq_quantity.setEnabled(true);
                            holder.matreq_quantity.setText(requiredMaterial.get(reqpos).getMatRequireQuantity()+"");
                         }
                         else
                         {
                             holder.matreq_quantity.setText("1");
                             holder.matreq_quantity.setEnabled(true);
                             material_list.get(position).setMatRequireQuantity(1);

                             material_list.get(position).setMaterialChecked(1);

                             //adding to required list
                             requiredMaterial.add(material_list.get(position));
                         }

                        //calling method to calculate and display total amount for requested materials
                        displayTotalRequestedAmount(material_list);

                    }

//                    //calling method to calculate and display total amount for requested materials
//                    displayTotalRequestedAmount();
                }
            });

            holder.matreq_quantity.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {


                }

                @Override
                public void afterTextChanged(Editable s) {

                    int requiredQuant=0;

                    try
                    {
                        requiredQuant=Integer.parseInt(holder.matreq_quantity.getText().toString().trim());
                    }
                    catch (NumberFormatException e)
                    {
                        e.printStackTrace();
                        holder.matreq_quantity.setError("Enter a number that is required");
                        holder.matreq_quantity.requestFocus();
                    }

                    //adding to required list
                    if(requiredMaterial.contains(material_list.get(position)) && requiredQuant>0)
                    {
                        int reqPos=requiredMaterial.indexOf(material_list.get(position));
                        requiredMaterial.get(reqPos).setMatRequireQuantity(requiredQuant);
                    }

                    //setting the value in model class
                    if(requiredQuant > 0)
                    {
                        material_list.get(position).setMatRequireQuantity(requiredQuant);
                    }

                    //calling method to calculate and display total amount for requested materials
                    displayTotalRequestedAmount(material_list);

                }
            });

            return convertView;
        }



    }


    //asynctask for viewing material management list
    private class ViewMaterialListTask extends AsyncTask<String, Void, Void>
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

            matreq_progress.setVisibility(View.VISIBLE);

            site_id=Utils.loginPreferences.getString("site_id", "");

        }


        @Override
        protected Void doInBackground(String... strings) {

            try
            {
                data= URLEncoder.encode("site_id", "UTF-8") + "="
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

            String material_id="", material_code="", material_name="", unit_quant="", material_rate="",
                    balance_amt="", site_budget="";

            //showing error message if error has occurred
            if(error!=null)
            {
                if(Utils.loginPreferences.getString("LoginType", "None").equalsIgnoreCase("Regular")
                        && Utils.appPreferences.getString("RegularLocalMatPresent", "No").equalsIgnoreCase("Yes"))
                {
                    materialList=new ArrayList<MaterialsModel>();

                    //loading data from regular local database
                    RegularDatabaseHelper myRegularDB=new RegularDatabaseHelper(context, Utils.appPreferences.getInt("RegularDBVersion", 1));
                    materialList=myRegularDB.getMaterials();

                    //setting up listview
                    adapter=new MatreqAdapter(context, materialList);
                    matreq_listview.setAdapter(adapter);
                }
                else
                if(Utils.loginPreferences.getString("LoginType", "None").equalsIgnoreCase("Reliever")
                        && Utils.appPreferences.getString("RelieverLocalMatPresent", "No").equalsIgnoreCase("Yes"))
                {
                    materialList=new ArrayList<MaterialsModel>();

                    //loading data from reliever local database
                    RelieverDatabaseHelper myRelieverDB=new RelieverDatabaseHelper(context, Utils.appPreferences.getInt("RelieverDBVersion", 1));
                    materialList=myRelieverDB.getMaterials();

                    //setting up listview
                    adapter=new MatreqAdapter(context, materialList);
                    matreq_listview.setAdapter(adapter);
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
                materialList=new ArrayList<MaterialsModel>();

                try
                {
                    JSONArray jsonResponse=new JSONArray(content);
                    JSONObject obj1=jsonResponse.getJSONObject(0);
                    status=obj1.getString("status");
                    msg=obj1.getString("msg");

                    if(status.equalsIgnoreCase("1"))
                    {
                        site_budget=obj1.getString("max_amount");
                        JSONObject obj2=jsonResponse.getJSONObject(1);
                        JSONArray jsonArray=obj2.getJSONArray("material_balance");
                        int arrLength=jsonArray.length();

                        for (int i=0; i<arrLength; i++)
                        {
                            JSONObject materialItem=jsonArray.getJSONObject(i);

                            material_id=materialItem.getString("material_id");
                            material_code=materialItem.getString("material_code");
                            material_name=materialItem.getString("material_name");
                            unit_quant=materialItem.getString("unit_quantity");
                            material_rate=materialItem.getString("material_rate");
                            balance_amt=materialItem.getString("balance_amount");

                            //adding data to model class
                            MaterialsModel newMaterial=new MaterialsModel();
                            newMaterial.setMaterialId(material_id);
                            newMaterial.setMaterialCode(material_code);
                            newMaterial.setMaterialName(material_name);
                            newMaterial.setUnit_quant(unit_quant);
                            newMaterial.setMaterialRate(material_rate);
                            newMaterial.setMatRequireQuantity(0);
                            try
                            {
                                newMaterial.setMaterialQuantity(Integer.parseInt(balance_amt.trim()));
                            }
                            catch(NumberFormatException nfe)
                            {
                                nfe.printStackTrace();
                            }

                            //adding material to arraylist
                            materialList.add(newMaterial);
                        }

                    }

                }
                catch (JSONException je)
                {
                    je.printStackTrace();
                }

                //loading material list if data retrieval is successful
                if(status.equalsIgnoreCase("1"))
                {
                    if(site_budget.equalsIgnoreCase("")
                            || site_budget.equalsIgnoreCase("null"))
                    {
                        Utils.appEditor.putString("SiteBudget", "0.0").commit();
                    }
                    else
                    {
                        Utils.appEditor.putString("SiteBudget", site_budget).commit();
                    }

                    //setting up listview
                    adapter=new MatreqAdapter(context, materialList);
                    matreq_listview.setAdapter(adapter);
                }
                else
                if(status.equalsIgnoreCase("0"))
                {
                    Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
                }
            }//outer else closes


            matreq_progress.setVisibility(View.GONE);
        }

    }//asynctask closes


    //asynctask to update materials requested
    private class MaterialRequestedTask extends AsyncTask<String, Integer, Void>
    {
        String content="";
        String error=null;
        String data="";
        String msg="";
        String status="";
        String site_id="";
        ArrayList<MaterialsModel> updateData;
        int progressStep=0;
        int count=0;
        String finalMsg="";

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            MaterialsRequired.this.pDialog=new ProgressDialog(context);
            MaterialsRequired.this.pDialog.setTitle("Sending request...");
            MaterialsRequired.this.pDialog.setCancelable(false);
            MaterialsRequired.this.pDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            MaterialsRequired.this.pDialog.setMax(100);
            MaterialsRequired.this.pDialog.setProgress(0);
            MaterialsRequired.this.pDialog.show();

            site_id=Utils.loginPreferences.getString("site_id", "");

            updateData=requiredMaterial;

            if(updateData.size() > 0)
            {
                progressStep=100/updateData.size();
            }
        }


        @Override
        protected Void doInBackground(String... strings) {

            for(MaterialsModel obj:updateData)
            {
                String mat_id=obj.getMaterialId();
                String mat_required=""+obj.getMatRequireQuantity();

                try
                {
                    data=URLEncoder.encode("site_id", "UTF-8") + "="
                            + URLEncoder.encode(site_id, "UTF-8") + "&"
                            + URLEncoder.encode("material_id", "UTF-8") + "="
                            + URLEncoder.encode(mat_id, "UTF-8") + "&"
                            + URLEncoder.encode("requested", "UTF-8") + "="
                            + URLEncoder.encode(mat_required, "UTF-8")+ "&"
                            + URLEncoder.encode("requested_month", "UTF-8") + "="
                            + URLEncoder.encode(month_year_value, "UTF-8") +"&"
                            + URLEncoder.encode("requested_type", "UTF-8") + "="
                            + URLEncoder.encode(strings[1], "UTF-8");

                    Log.d("Material", "doInBackground: "+data);
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
                }

                if(status.equalsIgnoreCase("1"))
                {
                    count+=progressStep;
                    publishProgress(count);
                }
                else
                if(status.equalsIgnoreCase("0"))
                {
                    finalMsg+=" "+obj.getMaterialName()+" "+msg+"\n";
                }
            } //for closes


            return null;
        }


        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);

            MaterialsRequired.this.pDialog.setProgress(values[0]);

        }


        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            //closing progress dialog
            MaterialsRequired.this.pDialog.dismiss();

            if(error!=null)
            {
                AlertDialog.Builder aDialog = new AlertDialog.Builder(context);
                aDialog.setMessage("Error in connecting to server! Please check your network connection.")
                        .setCancelable(true)
                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                dialog.cancel();
                            }
                        });

                AlertDialog alert = aDialog.create();
                alert.show();
            }
            else
            if(!finalMsg.equalsIgnoreCase(""))
            {
                Toast.makeText(context, finalMsg, Toast.LENGTH_LONG).show();
            }
            else
            {
                Toast.makeText(context, "Materials requested", Toast.LENGTH_LONG).show();

                //reinitialising requiredMaterial list
                requiredMaterial=new ArrayList<MaterialsModel>();
            }

            //setting up listview
            adapter=new MatreqAdapter(context, materialList);
            matreq_listview.setAdapter(adapter);

        }


    }//asynctask closes
}
