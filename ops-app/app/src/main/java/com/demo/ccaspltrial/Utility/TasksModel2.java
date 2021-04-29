package com.demo.ccaspltrial.Utility;

public class TasksModel2 {

    public String taskName="";
    public String taskId="";
    public String taskTime="";
    public String sopId="";
    public String sopName="";
    public String sopDescript="";

    public TasksModel2(){}

    public TasksModel2(String id, String name)
    {
        taskId=id;
        taskName=name;
    }

    public void setTaskId(String taskId)
    {
        this.taskId=taskId;
    }

    public String getTaskId()
    {
        return taskId;
    }

    public void setTaskName(String taskName)
    {
        this.taskName=taskName;
    }

    public String getTaskName()
    {
        return taskName;
    }

    public void setTaskTime(String taskTime)
    {
        this.taskTime=taskTime;
    }

    public String getTaskTime()
    {
        return taskTime;
    }

    public void setSopId(String sopId)
    {
        this.sopId=sopId;
    }

    public String getSopId()
    {
        return sopId;
    }

    public void setSopName(String sopName)
    {
        this.sopName=sopName;
    }

    public String getSopName()
    {
        return sopName;
    }

    public void setSopDescript(String sopDescript)
    {
        this.sopDescript=sopDescript;
    }

    public String getSopDescript()
    {
        return sopDescript;
    }

}
