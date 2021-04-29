package com.demo.ccaspltrial;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.demo.ccaspltrial.Utility.Utils;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link MaterialDetailsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MaterialDetailsFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    //parameters for this fragment
    TextView matcode_tv, matname_tv, matunit_tv; //, matrate_tv
    Button matdetails_ok;

    public MaterialDetailsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment MaterialDetailsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static MaterialDetailsFragment newInstance(String param1, String param2) {
        MaterialDetailsFragment fragment = new MaterialDetailsFragment();
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
        return inflater.inflate(R.layout.fragment_material_details, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        matcode_tv=(TextView)view.findViewById(R.id.matcode_details);
        matname_tv=(TextView)view.findViewById(R.id.matname_details);
        matunit_tv=(TextView)view.findViewById(R.id.matunit_details);
//        matrate_tv=(TextView)view.findViewById(R.id.matrate_details);
        matdetails_ok=(Button)view.findViewById(R.id.matdetails_ok);

        //initialising sharedpreferences
        Utils.pagePreferences=getActivity().getSharedPreferences(Utils.pagePref, Context.MODE_PRIVATE);
        Utils.pageEditor=Utils.pagePreferences.edit();

        //call to method to set material details
        if(Utils.pagePreferences.getString("MaterialCallingScreen", "")
                .equalsIgnoreCase("MaterialsTab"))
        {
            //call method to set material details in Materials tab
            setMaterialDetails1();
        }
        else
        if(Utils.pagePreferences.getString("MaterialCallingScreen", "")
                .equalsIgnoreCase("MaterialRequest"))
        {
            //call method to set material details in Material Request screen
            setMaterialDetails2();
        }

        //adding action to the ok button
        matdetails_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //setting sharedpreferences to default value
                Utils.pageEditor.putString("MaterialCallingScreen", "").commit();

                getFragmentManager().beginTransaction().remove(MaterialDetailsFragment.this).commit();
            }
        });
    }

    //method to set material details in Materials tab
    public void setMaterialDetails1()
    {
        //setting material details
        matcode_tv.setText(MaterialsFragment.materialList.get(MaterialsFragment.pos).getMaterialCode());
        matname_tv.setText(MaterialsFragment.materialList.get(MaterialsFragment.pos).getMaterialName());
        matunit_tv.setText(MaterialsFragment.materialList.get(MaterialsFragment.pos).getUnit_quant());

//        double rate=0.00;
//        try
//        {
//            rate=Double.parseDouble(MaterialsFragment.materialList.get(MaterialsFragment.pos).getMaterialRate().trim());
//        }
//        catch (NumberFormatException nfe)
//        {
//            nfe.printStackTrace();
//        }
//        matrate_tv.setText("Rs "+String.format("%.2f", rate));

    }//setMaterialDetails1 closes

    //method to set material details in Material Request screen
    public void setMaterialDetails2()
    {
        //setting material details
        matcode_tv.setText(MaterialsRequired.materialList.get(MaterialsRequired.pos).getMaterialCode());
        matname_tv.setText(MaterialsRequired.materialList.get(MaterialsRequired.pos).getMaterialName());
        matunit_tv.setText(MaterialsRequired.materialList.get(MaterialsRequired.pos).getUnit_quant());

//        double rate=0.00;
//        try
//        {
//            rate=Double.parseDouble(MaterialsRequired.materialList.get(MaterialsRequired.pos).getMaterialRate().trim());
//        }
//        catch (NumberFormatException nfe)
//        {
//            nfe.printStackTrace();
//        }
//
//        matrate_tv.setText("Rs "+String.format("%.2f",rate));

    }//setMaterialDetails2 closes
}
