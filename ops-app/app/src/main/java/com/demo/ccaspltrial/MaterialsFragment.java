package com.demo.ccaspltrial;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
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
import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link MaterialsFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link MaterialsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MaterialsFragment extends Fragment implements
        SubtractInputFragment.OnFragmentInteractionListener,
        AddInputFragment.OnFragmentInteractionListener{
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    //parameters required for this fragment
    ListView materialListView;
    Button save;

    static ArrayList<MaterialsModel> materialList;
    static MaterialAdapter materialAdapter;

    static ArrayList<MaterialsModel> changedData;

    public static int pos=-1;

    ProgressBar matfrag_progress;
    ProgressDialog pDialog;

    RegularDatabaseHelper myRegularDB;
    RelieverDatabaseHelper myRelieverDB;

    private OnFragmentInteractionListener mListener;

    public MaterialsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment MaterialsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static MaterialsFragment newInstance(String param1, String param2) {
        MaterialsFragment fragment = new MaterialsFragment();
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
        return inflater.inflate(R.layout.fragment_materials, container, false);
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

    @Override
    public void onFragmentInteraction(Uri uri) {

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

        materialListView=(ListView)view.findViewById(R.id.materialListView);
        save=(Button)view.findViewById(R.id.mfr_save);
        matfrag_progress=(ProgressBar)view.findViewById(R.id.matfrag_progress);

        materialListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

        //initialising sharedpreferences
        Utils.loginPreferences=getActivity().getSharedPreferences(Utils.loginPref, Context.MODE_PRIVATE);
        Utils.appPreferences=getActivity().getSharedPreferences(Utils.appPref, Context.MODE_PRIVATE);
        Utils.appEditor=Utils.appPreferences.edit();
        Utils.pagePreferences=getActivity().getSharedPreferences(Utils.pagePref, Context.MODE_PRIVATE);
        Utils.pageEditor=Utils.pagePreferences.edit();

        //initialising local database object
        if(Utils.loginPreferences.getString("LoginType", "None").equalsIgnoreCase("Regular"))
        {
            myRegularDB=new RegularDatabaseHelper(getContext(),
                    Utils.appPreferences.getInt("RegularDBVersion", 1));
        }
        else
        if(Utils.loginPreferences.getString("LoginType", "None").equalsIgnoreCase("Reliever"))
        {
            myRelieverDB=new RelieverDatabaseHelper(getContext(),
                    Utils.appPreferences.getInt("RelieverDBVersion", 1));
        }

        changedData=new ArrayList<MaterialsModel>();

        //retrieving material list data and loading list
        if(!Utils.loginPreferences.getString("site_id", "").equalsIgnoreCase("")
                && Utils.isNetworkAvailable(getContext()))
        {
            new ViewMaterialListTask().execute(Utils.materiallistviewURL);
        }
        else
        if(Utils.loginPreferences.getString("site_id", "").equalsIgnoreCase(""))
        {
            Toast.makeText(getContext(), "No site assigned", Toast.LENGTH_LONG).show();

            save.setVisibility(View.GONE);
        }
        else
        if(!Utils.isNetworkAvailable(getContext()))
        {
            Toast.makeText(getContext(), "No internet", Toast.LENGTH_SHORT).show();
        }


        //adding functionality to save button
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(changedData.size()!=0 && Utils.isNetworkAvailable(getContext()) &&
                        !Utils.loginPreferences.getString("site_id", "").equalsIgnoreCase(""))
                {
                    new UpdateMaterialBalanceTask().execute(Utils.materialbalanceupdateURL);
                }
                else
                if(changedData.size() != 0)
                {
                    Toast.makeText(getContext(), "No data changed", Toast.LENGTH_LONG).show();
                }
                else
                if(Utils.loginPreferences.getString("site_id", "").equalsIgnoreCase(""))
                {
                    Toast.makeText(getContext(), "No site is assigned", Toast.LENGTH_LONG).show();
                }
                else
                if(!Utils.isNetworkAvailable(getContext()))
                {
                    Toast.makeText(getContext(), "No internet", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    //viewholder class for MaterialAdapter
    static  class ViewHolder
    {
        TextView material_name, quantity;
        ImageView plus, minus;
    }

    //adapter class for materiallistview
    class MaterialAdapter extends BaseAdapter
    {
        Activity mActivity;
        Context context;
        LayoutInflater inflater;
        ArrayList<MaterialsModel> material_list;

        public MaterialAdapter(Context context, Activity mActivity, ArrayList<MaterialsModel> list)
        {
            this.context=context;
            this.mActivity=mActivity;
            material_list=list;
            inflater=LayoutInflater.from(context);
            changedData=new ArrayList<MaterialsModel>();
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

            ViewHolder holder;

            if(convertView==null)
            {
                convertView=inflater.inflate(R.layout.materials_listitem, null);
                holder=new ViewHolder();

                holder.material_name=(TextView)convertView.findViewById(R.id.material_name);
                holder.quantity=(TextView)convertView.findViewById(R.id.quantity);
                holder.minus=(ImageView)convertView.findViewById(R.id.subtractQuantity);
                holder.plus=(ImageView)convertView.findViewById(R.id.addQuantity);

                convertView.setTag(holder);
            }
            else
            {
                holder=(ViewHolder)convertView.getTag();
            }


            //adding material name to the item
//            holder.material_name.setText(material_list.get(position).getMaterialName() + " - "
//                    + material_list.get(position).getUnit_quant());
            holder.material_name.setText(material_list.get(position).getMaterialName());

            //adding material quantity to the item
            holder.quantity.setText(""+material_list.get(position).getMaterialQuantity());

            //adding action to click on material name
            holder.material_name.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    pos=position;

                    Utils.pageEditor.putString("MaterialCallingScreen", "MaterialsTab").commit();

                    FragmentManager fm=getFragmentManager();
                    FragmentTransaction transaction=fm.beginTransaction();
                    transaction.add(R.id.material_details_frag, new MaterialDetailsFragment());
                    transaction.commit();
                }
            });

            //adding action to minus
            holder.minus.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    //call method to subtract quantity by 1
                    subtractMaterialQuantity(position);

                    //code for fragment
//                    pos=position;
//                    FragmentManager fm=getFragmentManager();
//                    FragmentTransaction transaction=fm.beginTransaction();
//                    transaction.add(R.id.input_quantity_frag, new SubtractInputFragment());
//                    transaction.commit();


                }
            });

            //adding action to plus sign
            holder.plus.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    //call method to add quantity by 1
                    addMaterialQuantity(position);

                    //code for fragment
//                    pos=position;
//                    FragmentManager fm=getFragmentManager();
//                    FragmentTransaction transaction=fm.beginTransaction();
//                    transaction.add(R.id.input_quantity_frag, new AddInputFragment());
//                    transaction.commit();
                }
            });

            return convertView;
        }

        //method to add quantity by 1
        public void addMaterialQuantity(int posn)
        {
            int currentQuant=material_list.get(posn).getMaterialQuantity();

            int newQuant=currentQuant+1;

            //removing item if it is already in changed data list (to be added again later step with updated value)
            if(changedData.contains(material_list.get(posn)))
            {
                changedData.remove(material_list.get(posn));
            }

            material_list.get(posn).setMaterialQuantity(newQuant);

            //adding item to changed data list
            changedData.add(material_list.get(posn));

            materialAdapter.notifyDataSetChanged();
        }//addMaterialQuantity closes

        //method to subtract quantity by 1
        public void subtractMaterialQuantity(int posn)
        {
            int currentQuant=material_list.get(posn).getMaterialQuantity();

            int newQuant=0;

            if(currentQuant > 0)
            {
                newQuant=currentQuant-1;

                //removing item if it is already in changed data list (to be added again later step with updated value)
                if(changedData.contains(material_list.get(posn)))
                {
                    changedData.remove(material_list.get(posn));
                }

                material_list.get(posn).setMaterialQuantity(newQuant);

                //adding item to changed data list
                changedData.add(material_list.get(posn));
            }
            else
            {
                Toast.makeText(mActivity, "Cannot subtract any more", Toast.LENGTH_SHORT).show();
            }

            materialAdapter.notifyDataSetChanged();

        }//subtractMaterialQuantity closes


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
        int localRegularInsertMatCount=0, localRelieverInsertMatCount=0;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            matfrag_progress.setVisibility(View.VISIBLE);

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
                    materialList=myRegularDB.getMaterials();

                    //calling adapter for setting material list
                    materialAdapter=new MaterialAdapter(getContext(), getActivity(), materialList);
                    materialListView.setAdapter(materialAdapter);
                }
                else
                if(Utils.loginPreferences.getString("LoginType", "None").equalsIgnoreCase("Reliever")
                        && Utils.appPreferences.getString("RelieverLocalMatPresent", "No").equalsIgnoreCase("Yes"))
                {
                    materialList=new ArrayList<MaterialsModel>();

                    //loading data from reliever local database
                    materialList=myRelieverDB.getMaterials();

                    //calling adapter for setting material list
                    materialAdapter=new MaterialAdapter(getContext(), getActivity(), materialList);
                    materialListView.setAdapter(materialAdapter);
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

                            //adding data to regular local database
                            if(Utils.loginPreferences.getString("LoginType", "None").equalsIgnoreCase("Regular")
                                    && Utils.appPreferences.getString("RegularLocalMatPresent", "No").equalsIgnoreCase("No"))
                            {
                                boolean checkInserted=myRegularDB.insertMaterial(material_id, material_code,
                                        material_name, unit_quant, material_rate, balance_amt,
                                        "0", "0");

                                if(checkInserted == true)
                                {
                                    localRegularInsertMatCount++;
                                }
                            }//adding to regular local db closes
                            else
                            //adding data to reliever local database
                            if(Utils.loginPreferences.getString("LoginType", "None").equalsIgnoreCase("Reliever")
                                    && Utils.appPreferences.getString("RelieverLocalMatPresent", "No").equalsIgnoreCase("No"))
                            {
                                boolean checkInserted=myRelieverDB.insertMaterial(material_id, material_code,
                                        material_name, unit_quant, material_rate, balance_amt,
                                        "0", "0");

                                if(checkInserted == true)
                                {
                                    localRelieverInsertMatCount++;
                                }
                            }//adding to reliever local db closes

                        }//for closes

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

                    //calling adapter for setting material list
                    materialAdapter=new MaterialAdapter(getContext(), getActivity(), materialList);
                    materialListView.setAdapter(materialAdapter);

                    //checking whether data is successfully entered into local database
                    if(Utils.loginPreferences.getString("LoginType", "None").equalsIgnoreCase("Regular")
                            && Utils.appPreferences.getString("RegularLocalMatPresent", "No").equalsIgnoreCase("No"))
                    {
                        if(localRegularInsertMatCount == materialList.size())
                        {
                            Utils.appEditor.putString("RegularLocalMatPresent", "Yes").commit();
                        }
                        else
                        {
                            Utils.appEditor.putString("RegularLocalMatPresent", "No").commit();

                            if(Utils.loginPreferences.getString("LoginType", "None").equalsIgnoreCase("Regular"))
                            {
                                myRegularDB.deleteMaterials();
                            }

                        }
                    }
                    else
                    if(Utils.loginPreferences.getString("LoginType", "None").equalsIgnoreCase("Reliever")
                            && Utils.appPreferences.getString("RelieverLocalMatPresent", "No").equalsIgnoreCase("No"))
                    {
                        if(localRelieverInsertMatCount == materialList.size())
                        {
                            Utils.appEditor.putString("RelieverLocalMatPresent", "Yes").commit();
                        }
                        else
                        {
                            Utils.appEditor.putString("RelieverLocalMatPresent", "No").commit();

                            if(Utils.loginPreferences.getString("LoginType", "None").equalsIgnoreCase("Reliever"))
                            {
                                myRelieverDB.deleteMaterials();
                            }

                        }
                    }
                }
                else
                if(status.equalsIgnoreCase("0"))
                {
                    Toast.makeText(getContext(), msg, Toast.LENGTH_LONG).show();
                }
            }

            matfrag_progress.setVisibility(View.GONE);
        }

    }//asynctaask closes


    //asynctask to save and update updated material balance values
    private class UpdateMaterialBalanceTask extends AsyncTask<String, Integer, Void>
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

            MaterialsFragment.this.pDialog=new ProgressDialog(getContext());
            MaterialsFragment.this.pDialog.setTitle("Uploading...");
            MaterialsFragment.this.pDialog.setCancelable(false);
            MaterialsFragment.this.pDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            MaterialsFragment.this.pDialog.setMax(100);
            MaterialsFragment.this.pDialog.setProgress(0);
            MaterialsFragment.this.pDialog.show();

            site_id=Utils.loginPreferences.getString("site_id", "");

            updateData=changedData;

            if(updateData.size() > 0)
            {
                progressStep=100/updateData.size();
            }
        }


        @Override
        protected Void doInBackground(String... strings) {

           for(MaterialsModel obj:updateData)
           {
               content="";
               error=null;
               data="";
               msg="";
               status="";

               String mat_id=obj.getMaterialId();
               String mat_balance=""+obj.getMaterialQuantity();

               try
               {
                   data=URLEncoder.encode("site_id", "UTF-8") + "="
                           + URLEncoder.encode(site_id, "UTF-8") + "&"
                           + URLEncoder.encode("material_id", "UTF-8") + "="
                           + URLEncoder.encode(mat_id, "UTF-8") + "&"
                           + URLEncoder.encode("balance", "UTF-8") + "="
                           + URLEncoder.encode(mat_balance, "UTF-8");
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
                   if(Utils.loginPreferences.getString("LoginType", "None").equalsIgnoreCase("Regular")
                           && Utils.appPreferences.getString("RegularLocalMatPresent", "No").equalsIgnoreCase("Yes"))
                   {
                       boolean checkUpdated=myRegularDB.updateMaterialBalance(mat_id, obj.getMaterialQuantity());

                       if(checkUpdated ==false)
                       {
                           Utils.appEditor.putString("RegularLocalMatPresent", "No").commit();
                       }
                   }
                   else
                   if(Utils.loginPreferences.getString("LoginType", "None").equalsIgnoreCase("Reliever")
                           && Utils.appPreferences.getString("RelieverLocalMatPresent", "No").equalsIgnoreCase("Yes"))
                   {
                       boolean checkUpdated=myRelieverDB.updateMaterialBalance(mat_id, obj.getMaterialQuantity());

                       if(checkUpdated ==false)
                       {
                           Utils.appEditor.putString("RelieverLocalMatPresent", "No").commit();
                       }
                   }

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

            MaterialsFragment.this.pDialog.setProgress(values[0]);

        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            //closing progress dialog
            MaterialsFragment.this.pDialog.dismiss();

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
            if(!finalMsg.equalsIgnoreCase(""))
            {
                Toast.makeText(getContext(), finalMsg, Toast.LENGTH_LONG).show();
            }
            else
            {
                Toast.makeText(getContext(), "Materials updated", Toast.LENGTH_LONG).show();

                //reinitialising changedData list
                changedData=new ArrayList<MaterialsModel>();

                //calling adapter for setting material list
                materialAdapter=new MaterialAdapter(getContext(), getActivity(), materialList);
                materialListView.setAdapter(materialAdapter);
            }


        }
    }//asynctask closes

}
