package com.demo.ccaspltrial.Utility;

public class TrainingModel {

    public String trainingId="";
    public String trainingName="";
    public String trainingVideo="";
    public int totalQuestions=0;
    public int correctAnswers=0;
    public double correctPercentage=0;

    public TrainingModel(){}

    public TrainingModel(String trainingId, String trainingName)
    {
        this.trainingId=trainingId;
        this.trainingName=trainingName;
    }

    public void setTrainingId(String trainingId) {
        this.trainingId = trainingId;
    }

    public void setTrainingName(String trainingName) {
        this.trainingName = trainingName;
    }

    public String getTrainingId() {
        return trainingId;
    }

    public String getTrainingName() {
        return trainingName;
    }

    public String getTrainingVideo() {
        return trainingVideo;
    }

    public void setTrainingVideo(String trainingVideo) {
        this.trainingVideo = trainingVideo;
    }

    public int getTotalQuestions() {
        return totalQuestions;
    }

    public void setTotalQuestions(int totalQuestions) {
        this.totalQuestions = totalQuestions;
    }

    public int getCorrectAnswers() {
        return correctAnswers;
    }

    public void setCorrectAnswers(int correctAnswers) {
        this.correctAnswers = correctAnswers;
    }

    public double getCorrectPercentage() {
        return correctPercentage;
    }

    public void setCorrectPercentage(double correctPercentage) {
        this.correctPercentage = correctPercentage;
    }
}
