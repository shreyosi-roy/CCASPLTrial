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
import com.demo.ccaspltrial.Utility.Utils;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ExeExtraWorkFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ExeExtraWorkFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ExeExtraWorkFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    //parameters required for this fragment
    EditText extra_work;
    Button ok, cancel;

    private OnFragmentInteractionListener mListener;

    public ExeExtraWorkFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ExeExtraWorkFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ExeExtraWorkFragment newInstance(String param1, String param2) {
        ExeExtraWorkFragment fragment = new ExeExtraWorkFragment();
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
        return inflater.inflate(R.layout.fragment_exe_extra_work, container, false);
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

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        extra_work=(EditText)view.findViewById(R.id.exeExtraWork_input);
        ok=(Button)view.findViewById(R.id.exeEwfrag_ok);
        cancel=(Button)view.findViewById(R.id.exeEwfrag_cancel);

        //hiding submit and extra work buttons
        TasksForOthers.submit.setVisibility(View.GONE);
        TasksForOthers.extra_work.setVisibility(View.GONE);

        //initialising sharedpreferences
        Utils.appPreferences=getActivity().getSharedPreferences(Utils.appPref, Context.MODE_PRIVATE);
        Utils.appEditor=Utils.appPreferences.edit();

        //adding action to cancel button
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //showing submit and extra work buttons
                TasksForOthers.submit.setVisibility(View.VISIBLE);
                TasksForOthers.extra_work.setVisibility(View.VISIBLE);

                getFragmentManager().beginTransaction().remove(ExeExtraWorkFragment.this).commit();
            }
        });

        //adding action to okay button
        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String work=extra_work.getText().toString();

                if(work.equalsIgnoreCase(""))
                {
                    extra_work.setError("Enter extra work or cancel to exit");
                    extra_work.requestFocus();
                }
                else
                {
                    TasksForOthers.extraWorkDone+=work+"\n";
                    Utils.appEditor.putString("ExtraWorkDoneForOthers", TasksForOthers.extraWorkDone).commit();

                    Toast.makeText(getContext(), "Extra work added", Toast.LENGTH_SHORT).show();

                    //showing submit and extra work buttons
                    TasksForOthers.submit.setVisibility(View.VISIBLE);
                    TasksForOthers.extra_work.setVisibility(View.VISIBLE);

                    getFragmentManager().beginTransaction().remove(ExeExtraWorkFragment.this).commit();
                }
            }
        });
    }
}
