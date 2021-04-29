package com.demo.ccaspltrial;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.demo.ccaspltrial.Utility.TrainingModel;

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link TrainingResultFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TrainingResultFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    //parameters for this fragment
    ListView resultListView;
    Button ok;

    TrainingResultAdapter resultAdapter;

    public TrainingResultFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment TrainingResultFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static TrainingResultFragment newInstance(String param1, String param2) {
        TrainingResultFragment fragment = new TrainingResultFragment();
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
        return inflater.inflate(R.layout.fragment_training_result, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        resultListView=(ListView)view.findViewById(R.id.result_list);
        ok=(Button)view.findViewById(R.id.resultfrag_ok);


        //adding action to ok button
        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                getFragmentManager().beginTransaction().remove(TrainingResultFragment.this).commit();
            }
        });


        //setting up result list
        resultAdapter=new TrainingResultAdapter(getContext(), TrainingQuestionFragment.reqTraining_list);
        resultListView.setAdapter(resultAdapter);
    }

    //viewholder for class TrainingResultAdapter
    static  class ViewHolder
    {
        TextView training_name, training_score, training_video, video_heading;
    }

    //adapter class for training result list
    class TrainingResultAdapter extends BaseAdapter
    {
        Context context;
        ArrayList<TrainingModel> result_list;
        LayoutInflater inflater;

        public TrainingResultAdapter(Context context, ArrayList<TrainingModel> res_list)
        {
            this.context=context;
            result_list=res_list;
            inflater=LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            return result_list.size();
        }

        @Override
        public Object getItem(int i) {
            return result_list.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {

            ViewHolder holder;

            if(view == null)
            {
                holder=new ViewHolder();

                view=inflater.inflate(R.layout.training_result_list, null);

                holder.training_name=(TextView)view.findViewById(R.id.resultlist_trainingname);
                holder.training_score=(TextView)view.findViewById(R.id.resultlist_trainingscore);
                holder.training_video=(TextView)view.findViewById(R.id.resultlist_trainingvideo);
                holder.video_heading=(TextView)view.findViewById(R.id.resultlist_trainingvideo_heading);

                view.setTag(holder);
            }
            else
            {
                holder=(ViewHolder)view.getTag();
            }

            //adding training name
            holder.training_name.setText(result_list.get(i).getTrainingName());

            //adding training score and status
            if(result_list.get(i).getCorrectPercentage() == 100)
            {
                holder.training_score.setText(result_list.get(i).getCorrectPercentage() + "% (Passed)");
            }
            else
            {
                holder.training_score.setText(String.format("%.2f", result_list.get(i).getCorrectPercentage())
                        + "% (Not passed)");
            }

            //adding training video name if not passed
            if(result_list.get(i).getCorrectPercentage() == 100)
            {
                holder.video_heading.setVisibility(View.GONE);
                holder.training_video.setVisibility(View.GONE);
            }
            else
            {
                holder.video_heading.setVisibility(View.VISIBLE);
                holder.training_video.setVisibility(View.VISIBLE);
                holder.training_video.setText(result_list.get(i).getTrainingVideo());
            }


            return view;
        }
    }
}
