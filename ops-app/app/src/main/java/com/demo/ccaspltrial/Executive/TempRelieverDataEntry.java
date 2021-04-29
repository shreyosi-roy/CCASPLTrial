package com.demo.ccaspltrial.Executive;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.demo.ccaspltrial.R;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link TempRelieverDataEntry.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link TempRelieverDataEntry#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TempRelieverDataEntry extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    //parameters for this fragment
    EditText name, phno, altno, city, state, country, addr, email;
    Button submit, cancel;

    public static String tempreliever_name="", tempreliever_phno="", tempreliever_altno="",
            tempreliever_city="", tempreliever_state="", tempreliever_country="", tempreliever_addr="",
            tempreliever_email="";

    private OnFragmentInteractionListener mListener;

    public TempRelieverDataEntry() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment TempRelieverDataEntry.
     */
    // TODO: Rename and change types and number of parameters
    public static TempRelieverDataEntry newInstance(String param1, String param2) {
        TempRelieverDataEntry fragment = new TempRelieverDataEntry();
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
        return inflater.inflate(R.layout.fragment_temp_reliever_data_entry, container, false);
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
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    //adding action to submit button
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        name=(EditText)view.findViewById(R.id.tempreliever_name);
        phno=(EditText)view.findViewById(R.id.tempreliever_phno);
        altno=(EditText)view.findViewById(R.id.tempreliever_altno);
        city=(EditText)view.findViewById(R.id.tempreliever_city);
        state=(EditText)view.findViewById(R.id.tempreliever_state);
        country=(EditText)view.findViewById(R.id.tempreliever_country);
        addr=(EditText)view.findViewById(R.id.tempreliever_addr);
        email=(EditText)view.findViewById(R.id.tempreliever_email);
        submit=(Button) view.findViewById(R.id.tempreliever_submit);
        cancel=(Button)view.findViewById(R.id.tempreliever_cancel);

        //hiding assign reliever button
        ExecutiveHome.assign.setVisibility(View.GONE);

        //adding action cancel button
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ExecutiveHome.assign.setVisibility(View.VISIBLE);

                getFragmentManager().beginTransaction().remove(TempRelieverDataEntry.this).commit();
            }
        });

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                tempreliever_name=name.getText().toString();
                tempreliever_phno=phno.getText().toString();
                tempreliever_altno=altno.getText().toString();
                tempreliever_city=city.getText().toString();
                tempreliever_state=state.getText().toString();
                tempreliever_country=country.getText().toString();
                tempreliever_addr=addr.getText().toString();
                tempreliever_email=email.getText().toString();

                if(tempreliever_name.equalsIgnoreCase(""))
                {
                    name.setError("Enter name");
                    name.requestFocus();
                }
                else
                if(tempreliever_phno.equalsIgnoreCase(""))
                {
                    phno.setError("Enter phone no.");
                    phno.requestFocus();
                }
                else
                if(tempreliever_city.equalsIgnoreCase(""))
                {
                    city.setError("Enter city");
                    city.requestFocus();
                }
                else
                if(tempreliever_state.equalsIgnoreCase(""))
                {
                    state.setError("Enter state");
                    state.requestFocus();
                }
                else
                if(tempreliever_country.equalsIgnoreCase(""))
                {
                    country.setError("Enter country");
                    country.requestFocus();
                }
                else
                if(tempreliever_addr.equalsIgnoreCase(""))
                {
                    addr.setError("Enter present address");
                    addr.requestFocus();
                }
                else
                {
                    Toast.makeText(getContext(), "Temporary reliever data entered", Toast.LENGTH_SHORT).show();

                    //showing assign reliever button
                    ExecutiveHome.assign.setVisibility(View.VISIBLE);

                    getFragmentManager().beginTransaction().remove(TempRelieverDataEntry.this).commit();
                }
            }
        });
    }
}
