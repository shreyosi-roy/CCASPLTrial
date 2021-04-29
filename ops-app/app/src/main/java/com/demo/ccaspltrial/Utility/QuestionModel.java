package com.demo.ccaspltrial.Utility;

public class QuestionModel {

    public String trainingId="";
    public String questionId="";
    public String question="";
    public String answer1="";
    public String answer2="";
    public String answer3="";
    public String answer4="";
    public String correct_answer="";

    public QuestionModel(){}

    public QuestionModel(String trainingId, String questionId, String question, String answer1, String answer2, String answer3, String answer4, String correct_answer)
    {
        this.trainingId=trainingId;
        this.questionId=questionId;
        this.question=question;
        this.answer1=answer1;
        this.answer2=answer2;
        this.answer3=answer3;
        this.answer4=answer4;
        this.correct_answer=correct_answer;
    }

    public String getTrainingId() {
        return trainingId;
    }

    public void setTrainingId(String trainingId) {
        this.trainingId = trainingId;
    }

    public void setQuestionId(String questionId) {
        this.questionId = questionId;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public void setAnswer1(String answer1) {
        this.answer1 = answer1;
    }

    public void setAnswer2(String answer2) {
        this.answer2 = answer2;
    }

    public void setAnswer3(String answer3) {
        this.answer3 = answer3;
    }

    public void setAnswer4(String answer4) {
        this.answer4 = answer4;
    }

    public void setCorrect_answer(String correct_answer) {
        this.correct_answer = correct_answer;
    }

    public String getQuestionId() {
        return questionId;
    }

    public String getQuestion() {
        return question;
    }

    public String getAnswer1() {
        return answer1;
    }

    public String getAnswer2() {
        return answer2;
    }

    public String getAnswer3() {
        return answer3;
    }

    public String getAnswer4() {
        return answer4;
    }

    public String getCorrect_answer() {
        return correct_answer;
    }
}
