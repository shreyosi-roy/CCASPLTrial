package com.demo.ccaspltrial.Utility;

public class TasksModel {

    public String taskName;
    public int taskCounter;

    public TasksModel(){}

    public TasksModel(String name, int count)
    {
        taskName=name;
        taskCounter=count;
    }

    public void setTaskName(String name)
    {
        taskName=name;
    }

    public void setTaskCounter()
    {
        taskCounter++;
    }

    public String getTaskName()
    {
        return taskName;
    }

    public int getTaskCounter()
    {
        return taskCounter;
    }
}
