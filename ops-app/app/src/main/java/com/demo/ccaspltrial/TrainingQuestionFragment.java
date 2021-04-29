package com.demo.ccaspltrial;

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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.demo.ccaspltrial.Utility.QuestionModel;
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


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link TrainingQuestionFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link TrainingQuestionFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TrainingQuestionFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    //parameters for this fragment
    TextView training_name, question;
    Button submit;
    RadioButton answer1, answer2, answer3, answer4;

    String selectedAnswer="";

    private OnFragmentInteractionListener mListener;

    public static ArrayList<TrainingModel> reqTraining_list;

    ArrayList <QuestionModel> question_list;

    ArrayList<String> passedTraining;

    int correctStatus, correctCount=0;

    int trainingListPosition=0, questionListPosition=0;

    ProgressDialog pDialog;

    public TrainingQuestionFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment TrainingQuestionFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static TrainingQuestionFragment newInstance(String param1, String param2) {
        TrainingQuestionFragment fragment = new TrainingQuestionFragment();
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
        return inflater.inflate(R.layout.fragment_training_question, container, false);
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

        training_name=(TextView)view.findViewById(R.id.training_title);
        question=(TextView)view.findViewById(R.id.training_question);
        answer1=(RadioButton)view.findViewById(R.id.answer1);
        answer2=(RadioButton)view.findViewById(R.id.answer2);
        answer3=(RadioButton)view.findViewById(R.id.answer3);
        answer4=(RadioButton)view.findViewById(R.id.answer4);
        submit=(Button)view.findViewById(R.id.submit_answer);

        //initialising sharedpreferences
        Utils.appPreferences=getActivity().getSharedPreferences(Utils.appPref, Context.MODE_PRIVATE);
        Utils.appEditor=Utils.appPreferences.edit();

        //setting training list and question list
        reqTraining_list=Utils.requestedTrainingList;
        question_list=Utils.questionList;

        //saving current training id
        Utils.appEditor.putString("CurrentTrainingId", reqTraining_list.get(trainingListPosition).getTrainingId()).commit();

        //initialising status of correct answers of a training
        correctStatus=1;

        //initialising number of correct answers of a training
        correctCount=0;

        //initialising list of passed trainings
        passedTraining=new ArrayList<String>();

        //setting first question
        setTrainingQuestions();

        //adding action to click on radio buttons
        answer1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                getSelectedAnswer(answer1);
            }
        });

        answer2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                getSelectedAnswer(answer2);
            }
        });

        answer3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                getSelectedAnswer(answer3);
            }
        });

        answer4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                getSelectedAnswer(answer4);
            }
        });

        //adding action to submit button
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //call to method to check answer
                checkAnswer();

            }
        });


    }

    //method to check answer
    public void checkAnswer()
    {
        if(!answer1.isChecked() && !answer2.isChecked() && !answer3.isChecked() && !answer4.isChecked())
        {
            Toast.makeText(getContext(), "Please select an option", Toast.LENGTH_SHORT).show();
        }
        else
        {
            //code to check if answer is correct
            if(reqTraining_list.get(trainingListPosition).getTrainingId().trim()
                    .equalsIgnoreCase(Utils.appPreferences.getString("CurrentTrainingId", "")))
            {
                //checking if answer is correct
                if(!selectedAnswer.trim().equalsIgnoreCase(question_list.get(questionListPosition)
                        .getCorrect_answer().trim()))
                {
                    correctStatus=0;
                }
                else
                {
                    correctCount++;
                }

                if(questionListPosition+1==question_list.size() && correctStatus==1)
                {
                    //storing number of correct answers for a training
                    reqTraining_list.get(trainingListPosition).setCorrectAnswers(correctCount);

                    //adding training to passed list if all answers are correct
                    passedTraining.add(Utils.appPreferences.getString("CurrentTrainingId", ""));
                }
                else
                if(questionListPosition+1==question_list.size())
                {
                    //storing number of correct answers for a training
                    reqTraining_list.get(trainingListPosition).setCorrectAnswers(correctCount);
                }
            }
            else
            {
                //storing number of correct answers for a training
                reqTraining_list.get(trainingListPosition-1).setCorrectAnswers(correctCount);

                //adding training to passed list if all answers are correct
                if(correctStatus == 1)
                {
                    passedTraining.add(Utils.appPreferences.getString("CurrentTrainingId", ""));
                }

                //updating saved training id
                Utils.appEditor.putString("CurrentTrainingId", reqTraining_list.get(trainingListPosition).getTrainingId().trim()).commit();

                //re-initialising status of correct answers
                correctStatus=1;

                //re-initialising count of correct answers
                correctCount=0;

                //checking if answer is correct
                if(!selectedAnswer.trim().equalsIgnoreCase(question_list.get(questionListPosition).getCorrect_answer().trim()))
                {
                    correctStatus=0;
                }
                else
                {
                    correctCount++;
                }
            }

            //incrementing question list position
            questionListPosition++;

            //set next question, if any
            setTrainingQuestions();
        }
    }//checkAnswer closes

    //set training name
    public void setTrainingQuestions()
    {
        if(trainingListPosition < reqTraining_list.size() && questionListPosition < question_list.size())
        {
            if(question_list.get(questionListPosition).getTrainingId().trim()
                    .equalsIgnoreCase(reqTraining_list.get(trainingListPosition).getTrainingId().trim()))
            {
                //setting question and answer
                training_name.setText(reqTraining_list.get(trainingListPosition).getTrainingName().trim());
                question.setText(question_list.get(questionListPosition).getQuestion().trim());
                if(question_list.get(questionListPosition).getAnswer1().trim().equalsIgnoreCase("null")
                        || question_list.get(questionListPosition).getAnswer1().trim().equalsIgnoreCase(""))
                {
                    answer1.setVisibility(View.GONE);
                }
                else
                {
                    answer1.setVisibility(View.VISIBLE);
                    answer1.setChecked(false);
                    answer1.setText(question_list.get(questionListPosition).getAnswer1().trim());
                }
                if(question_list.get(questionListPosition).getAnswer2().trim().equalsIgnoreCase("null")
                        || question_list.get(questionListPosition).getAnswer2().trim().equalsIgnoreCase(""))
                {
                    answer2.setVisibility(View.GONE);
                }
                else
                {
                    answer2.setVisibility(View.VISIBLE);
                    answer2.setChecked(false);
                    answer2.setText(question_list.get(questionListPosition).getAnswer2().trim());
                }
                if(question_list.get(questionListPosition).getAnswer3().trim().equalsIgnoreCase("null")
                        || question_list.get(questionListPosition).getAnswer3().trim().equalsIgnoreCase(""))
                {
                    answer3.setVisibility(View.GONE);
                }
                else
                {
                    answer3.setVisibility(View.VISIBLE);
                    answer3.setChecked(false);
                    answer3.setText(question_list.get(questionListPosition).getAnswer3().trim());
                }
                if(question_list.get(questionListPosition).getAnswer4().trim().equalsIgnoreCase("null")
                        || question_list.get(questionListPosition).getAnswer4().trim().equalsIgnoreCase(""))
                {
                    answer4.setVisibility(View.GONE);
                }
                else
                {
                    answer4.setVisibility(View.VISIBLE);
                    answer4.setChecked(false);
                    answer4.setText(question_list.get(questionListPosition).getAnswer4().trim());
                }

            }
            else
            if(!question_list.get(questionListPosition).getTrainingId().trim()
                    .equalsIgnoreCase(reqTraining_list.get(trainingListPosition).getTrainingId().trim()))
            {
                //incrementing training list position
                trainingListPosition++;

                if(trainingListPosition < reqTraining_list.size())
                {
                    //setting question and answer
                    training_name.setText(reqTraining_list.get(trainingListPosition).getTrainingName().trim());
                    question.setText(question_list.get(questionListPosition).getQuestion().trim());
                    if(question_list.get(questionListPosition).getAnswer1().trim().equalsIgnoreCase("null")
                            || question_list.get(questionListPosition).getAnswer1().trim().equalsIgnoreCase(""))
                    {
                        answer1.setVisibility(View.GONE);
                    }
                    else
                    {
                        answer1.setVisibility(View.VISIBLE);
                        answer1.setChecked(false);
                        answer1.setText(question_list.get(questionListPosition).getAnswer1().trim());
                    }
                    if(question_list.get(questionListPosition).getAnswer2().trim().equalsIgnoreCase("null")
                            || question_list.get(questionListPosition).getAnswer2().trim().equalsIgnoreCase(""))
                    {
                        answer2.setVisibility(View.GONE);
                    }
                    else
                    {
                        answer2.setVisibility(View.VISIBLE);
                        answer2.setChecked(false);
                        answer2.setText(question_list.get(questionListPosition).getAnswer2().trim());
                    }
                    if(question_list.get(questionListPosition).getAnswer3().trim().equalsIgnoreCase("null")
                            || question_list.get(questionListPosition).getAnswer3().trim().equalsIgnoreCase(""))
                    {
                        answer3.setVisibility(View.GONE);
                    }
                    else
                    {
                        answer3.setVisibility(View.VISIBLE);
                        answer3.setChecked(false);
                        answer3.setText(question_list.get(questionListPosition).getAnswer3().trim());
                    }
                    if(question_list.get(questionListPosition).getAnswer4().trim().equalsIgnoreCase("null")
                            || question_list.get(questionListPosition).getAnswer4().trim().equalsIgnoreCase(""))
                    {
                        answer4.setVisibility(View.GONE);
                    }
                    else
                    {
                        answer4.setVisibility(View.VISIBLE);
                        answer4.setChecked(false);
                        answer4.setText(question_list.get(questionListPosition).getAnswer4().trim());
                    }
                }
                else
                if(trainingListPosition >= reqTraining_list.size())
                {
                    //call method to calculate result
                    calculateResult();

                    //add code to update training status in database
                    if(!Utils.loginPreferences.getString("emp_id", "").equalsIgnoreCase("")
                            && Utils.isNetworkAvailable(getContext()) && passedTraining.size() > 0)
                    {
                        new UpdateTrainingTask().execute(Utils.updatepassedtrainingURL);
                    }
                    else
                    if(!Utils.isNetworkAvailable(getContext()))
                    {
                        Toast.makeText(getContext(), "No internet", Toast.LENGTH_SHORT).show();

                        getFragmentManager().beginTransaction().remove(TrainingQuestionFragment.this).commit();
                    }
                    else
                    if(passedTraining.size() <= 0)
                    {
                        Toast.makeText(getContext(), "No trainings passed", Toast.LENGTH_SHORT).show();

                        getFragmentManager().beginTransaction().replace(R.id.question_frag,
                                new TrainingResultFragment()).commit();
                    }
                    else
                    {
                        getFragmentManager().beginTransaction().remove(TrainingQuestionFragment.this).commit();
                    }
                }
            }
        }//outer if closes
        else
        if(questionListPosition >= question_list.size())
        {
            //incrementing training list position
            trainingListPosition++;

            //call method to calculate result
            calculateResult();

            //add code to update training status in database
            if(!Utils.loginPreferences.getString("emp_id", "").equalsIgnoreCase("")
                    && Utils.isNetworkAvailable(getContext()) && passedTraining.size() > 0)
            {
                new UpdateTrainingTask().execute(Utils.updatepassedtrainingURL);
            }
            else
            if(!Utils.isNetworkAvailable(getContext()))
            {
                Toast.makeText(getContext(), "No internet", Toast.LENGTH_SHORT).show();

                getFragmentManager().beginTransaction().remove(TrainingQuestionFragment.this).commit();
            }
            else
            if(passedTraining.size() <= 0)
            {
                Toast.makeText(getContext(), "No trainings passed", Toast.LENGTH_SHORT).show();

                getFragmentManager().beginTransaction().replace(R.id.question_frag,
                        new TrainingResultFragment()).commit();
            }
            else
            {
                getFragmentManager().beginTransaction().remove(TrainingQuestionFragment.this).commit();
            }
        }//outer else closes
        else
        if(trainingListPosition >= reqTraining_list.size())
        {
            //call method to calculate result
            calculateResult();

            //add code to update training status in database
            if(!Utils.loginPreferences.getString("emp_id", "").equalsIgnoreCase("")
                    && Utils.isNetworkAvailable(getContext()) && passedTraining.size() > 0)
            {
                new UpdateTrainingTask().execute(Utils.updatepassedtrainingURL);
            }
            else
            if(!Utils.isNetworkAvailable(getContext()))
            {
                Toast.makeText(getContext(), "No internet", Toast.LENGTH_SHORT).show();

                getFragmentManager().beginTransaction().remove(TrainingQuestionFragment.this).commit();
            }
            else
            if(passedTraining.size() <= 0)
            {
                Toast.makeText(getContext(), "No trainings passed", Toast.LENGTH_SHORT).show();

                getFragmentManager().beginTransaction().replace(R.id.question_frag,
                        new TrainingResultFragment()).commit();
            }
            else
            {
                getFragmentManager().beginTransaction().remove(TrainingQuestionFragment.this).commit();
            }
        }//outer else closes
    }//setTrainingQuestions closes

    //method to calculate result
    public void calculateResult()
    {
        for(int i=0; i<reqTraining_list.size(); i++)
        {
            int totQ=0, correctAns=0;
            double correctPercent=0;

            totQ=reqTraining_list.get(i).getTotalQuestions();
            correctAns=reqTraining_list.get(i).getCorrectAnswers();

            if(correctAns == totQ)
            {
                reqTraining_list.get(i).setCorrectPercentage(100);
            }
            else
            {
                correctPercent=(correctAns*100.0)/totQ;
                reqTraining_list.get(i).setCorrectPercentage(correctPercent);
            }
        }
    }//calculateResult closes

    //method for click action of radio buttons
    public void getSelectedAnswer(RadioButton button)
    {
        boolean checked=button.isChecked();

        int itemId=button.getId();

        switch (itemId)
        {
            case R.id.answer1:
                if(checked)
                    selectedAnswer=question_list.get(questionListPosition).getAnswer1().trim();
                break;

            case R.id.answer2:
                if(checked)
                    selectedAnswer=question_list.get(questionListPosition).getAnswer2().trim();
                break;

            case R.id.answer3:
                if(checked)
                    selectedAnswer=question_list.get(questionListPosition).getAnswer3().trim();
                break;

            case R.id.answer4:
                if(checked)
                    selectedAnswer=question_list.get(questionListPosition).getAnswer4().trim();
                break;
        }
    }


    //asynctask to update training status of passed trainings
    private class UpdateTrainingTask extends AsyncTask<String, Integer, Void>
    {
        String content="";
        String error=null;
        String data="";
        String msg="";
        String status="";
        String employee_id="";
        String finalMsg="";
        ArrayList<String> updateTrainings;
        int progressStep=0;
        int count=0;
        int updateErrorCount=0;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            TrainingQuestionFragment.this.pDialog=new ProgressDialog(getContext());
            TrainingQuestionFragment.this.pDialog.setTitle("Uploading...");
            TrainingQuestionFragment.this.pDialog.setCancelable(false);
            TrainingQuestionFragment.this.pDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            TrainingQuestionFragment.this.pDialog.setMax(100);
            TrainingQuestionFragment.this.pDialog.setProgress(0);
            TrainingQuestionFragment.this.pDialog.show();

            employee_id=Utils.loginPreferences.getString("emp_id", "");

            updateTrainings=passedTraining;

            if(updateTrainings.size() > 0)
            {
                progressStep=100/updateTrainings.size();
            }
        }

        @Override
        protected Void doInBackground(String... strings) {

            for(String training_id : updateTrainings)
            {
                content="";
                error=null;
                data="";
                msg="";
                status="";

                try
                {
                    data= URLEncoder.encode("employee_id", "UTF-8") + "="
                            + URLEncoder.encode(employee_id, "UTF-8") + "&"
                            + URLEncoder.encode("training_id", "UTF-8") + "="
                            + URLEncoder.encode(training_id, "UTF-8");
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
                    JSONArray jsonResponse=new JSONArray(content);
                    JSONObject obj1=jsonResponse.getJSONObject(0);
                    status=obj1.getString("status");
                    msg=obj1.getString("msg");
                }
                catch (JSONException je)
                {
                    je.printStackTrace();
                    finalMsg=je.getMessage();
                }

                if(status.equalsIgnoreCase("1"))
                {
                    count+=progressStep;
                    publishProgress(count);
                }
                else
                if(status.equalsIgnoreCase("0"))
                {
                    updateErrorCount++;
                }
            }//for closes

            return null;
        }


        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);

            TrainingQuestionFragment.this.pDialog.setProgress(values[0]);
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            //closing progress dialog
            TrainingQuestionFragment.this.pDialog.dismiss();

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
            if(updateErrorCount > 0)
            {
                Toast.makeText(getContext(), "Passed "+(updateTrainings.size()-updateErrorCount)+" training(s)", Toast.LENGTH_SHORT).show();
                Toast.makeText(getContext(), "Could not update "+updateErrorCount+" training(s)", Toast.LENGTH_LONG).show();
            }
            else
            if(!finalMsg.equalsIgnoreCase(""))
            {
                Toast.makeText(getContext(), "Error "+finalMsg, Toast.LENGTH_LONG).show();
            }
            else
            {
                if(reqTraining_list.size() == updateTrainings.size())
                {
                    Toast.makeText(getContext(), "Passed "+updateTrainings.size()+" training(s)", Toast.LENGTH_LONG).show();
                }
                else
                {
                    Toast.makeText(getContext(), "Passed "+updateTrainings.size()+
                            " training(s)\nDid not pass "+(reqTraining_list.size()-updateTrainings.size())
                            +" training(s)", Toast.LENGTH_LONG).show();
                }

            }

            //opening result fragment
            getFragmentManager().beginTransaction().replace(R.id.question_frag,
                    new TrainingResultFragment()).commit();
        }
    }//asynctask closes
}
