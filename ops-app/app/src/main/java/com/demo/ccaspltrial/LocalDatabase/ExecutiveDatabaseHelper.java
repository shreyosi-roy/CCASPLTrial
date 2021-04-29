package com.demo.ccaspltrial.LocalDatabase;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.demo.ccaspltrial.Utility.ExeEmployeeModel;
import com.demo.ccaspltrial.Utility.ExeSiteModel;

import java.util.ArrayList;

public class ExecutiveDatabaseHelper extends SQLiteOpenHelper {

    //database name
    public static final String DB_NAME="CCASPL_OPERATIONS_APP_EXECUTIVE.db";

    //table names
    public static final String EXE_SITES_TABLE ="Executive_sites_table"; //table to store sites under an executive
    public static final String EXE_RELIEVERS_TABLE ="Executive_relievers_table"; //table to store relievers under an executive
    public static final String EXE_EMPLOYEES_TABLE ="Executive_employees_table"; //table to store employees under an executive

    //columns for Executive_sites_table
    public static final String COL_SITE_ID="site_id"; //stores site id
    public static final String COL_SITE_CODE="site_code"; //stores site code
    public static final String COL_SITE_NAME="site_name"; //stores site name

    //columns for Executive_relievers_table
    public static final String COL_RELIEVER_ID="reliever_id"; //stores reliever id
    public static final String COL_RELIEVER_CODE="reliever_code"; //stores reliever code
    public static final String COL_RELIEVER_NAME="reliever_name"; //stores reliever name
    public static final String COL_TYPE_RELIEVER="emp_type"; //stores the type of employee
    public static final String COL_SUB_REGULAR_EMPID="sub_regular_empId"; //stores the regular employee id who is being substituted

    //columns for Executive_employees_table
    public static final String COL_EMP_ID="emp_id";
    public static final String COL_EMP_CODE="emp_code";
    public static final String COL_EMP_NAME="emp_name";
    public static final String COL_EMP_TYPE="emp_type"; //stores the type of employee
    public static final String COL_SUB_REGULAR_EMP_ID="sub_regular_empId"; //stores the regular employee id who is being substituted

    //queries to create the above tables
    //to create Executive_sites_table
    public static final String createExeSitesTable ="CREATE TABLE " + EXE_SITES_TABLE + "\n" +
            "( " + COL_SITE_ID + " INTEGER PRIMARY KEY, \n"
            + COL_SITE_CODE + " TEXT, \n"
            + COL_SITE_NAME + " TEXT, \n"
            + COL_SUB_REGULAR_EMPID + "TEXT )";

    //to create Executive_relievers_table
    public static final String createExeRelieversTable ="CREATE TABLE " + EXE_RELIEVERS_TABLE + "\n" +
            "( " + COL_RELIEVER_ID + " INTEGER PRIMARY KEY, \n"
            + COL_RELIEVER_CODE + " TEXT, \n"
            + COL_RELIEVER_NAME + " TEXT, \n"
            + COL_TYPE_RELIEVER + " TEXT, \n"
            + COL_SUB_REGULAR_EMP_ID + "TEXT )";

    //to create Executive_employees_table
    public static final String createExeEmployeesTable ="CREATE TABLE " + EXE_EMPLOYEES_TABLE + "\n" +
            "( " + COL_EMP_ID + " INTEGER PRIMARY KEY, \n"
            + COL_EMP_CODE + " TEXT, \n"
            + COL_EMP_NAME + " TEXT, \n"
            + COL_EMP_TYPE + " TEXT )";

    public ExecutiveDatabaseHelper(Context context, int versionCount) {
        super(context, DB_NAME, null, versionCount);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL(createExeSitesTable);
        db.execSQL(createExeRelieversTable);
        db.execSQL(createExeEmployeesTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        db.execSQL("DROP TABLE IF EXISTS " + EXE_SITES_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + EXE_RELIEVERS_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + EXE_EMPLOYEES_TABLE);

        onCreate(db);
    }


    //method to insert site into Executive_sites_table
    public boolean insertExeSite(String site_id, String site_code, String site_name)
    {
        int siteId=-1;

        try
        {
            Integer.parseInt(site_id);
        }
        catch (NumberFormatException nfe)
        {
            nfe.printStackTrace();
        }

        SQLiteDatabase myDB=this.getWritableDatabase();

        ContentValues values=new ContentValues();
        values.put(COL_SITE_ID, siteId);
        values.put(COL_SITE_CODE, site_code);
        values.put(COL_SITE_NAME, site_name);

        long result=myDB.insert(EXE_SITES_TABLE, null, values);

        if(result == -1)
            return false;
        else
            return true;
    }


    //method to retrieve sites from Executive_sites_table
    public ArrayList<ExeSiteModel> getExeSites()
    {
        ArrayList<ExeSiteModel> list=new ArrayList<ExeSiteModel>();

        SQLiteDatabase myDB=this.getReadableDatabase();

        Cursor result=myDB.query(EXE_SITES_TABLE, new String[]{COL_SITE_ID, COL_SITE_CODE, COL_SITE_NAME},
                null, null, null, null, null);

        //adding to list if data is returned
        if(result.getCount() != 0)
        {
            while(result.moveToNext())
            {
                //adding data to object
                ExeSiteModel newSite=new ExeSiteModel();
                newSite.setSiteId(result.getString(0));
                newSite.setSiteCode(result.getString(1));
                newSite.setSiteName(result.getString(2));

                //adding object to list
                list.add(newSite);

            }//while closes
        }//if closes

        return list;
    }


    public void deleteExeSites()
    {
        SQLiteDatabase myDB=this.getWritableDatabase();

        myDB.delete(EXE_SITES_TABLE, null, null);
    }


    //method to insert reliever into Executive_relievers_table
    public boolean insertExeReliever(String reliever_id, String reliever_code, String reliever_name, String subRegularEmpId)
    {
        int relieverId=-1;

        try
        {
            relieverId=Integer.parseInt(reliever_id);
        }
        catch (NumberFormatException nfe)
        {
            nfe.printStackTrace();
        }

        SQLiteDatabase myDB=this.getWritableDatabase();

        ContentValues values=new ContentValues();
        values.put(COL_RELIEVER_ID, relieverId);
        values.put(COL_RELIEVER_CODE, reliever_code);
        values.put(COL_RELIEVER_NAME, reliever_name);
        values.put(COL_TYPE_RELIEVER, "1");
        values.put(COL_SUB_REGULAR_EMPID, subRegularEmpId);

        long result=myDB.insert(EXE_RELIEVERS_TABLE, null, values);

        if(result == -1)
            return false;
        else
            return true;
    }


    //method to retrieve relievers from Executive_relievers_table
    public ArrayList<ExeEmployeeModel> getExeRelievers()
    {
        ArrayList<ExeEmployeeModel> list=new ArrayList<ExeEmployeeModel>();

        SQLiteDatabase myDB=this.getReadableDatabase();

        Cursor result=myDB.query(EXE_RELIEVERS_TABLE, new String[]{COL_RELIEVER_ID,
                COL_RELIEVER_CODE, COL_RELIEVER_NAME, COL_TYPE_RELIEVER, COL_SUB_REGULAR_EMPID},
                null, null, null, null, null);

        //adding to list if data is returned
        if(result.getCount() != 0)
        {
            while (result.moveToNext())
            {
                //adding data to object
                ExeEmployeeModel newReliever=new ExeEmployeeModel();
                newReliever.setEmpId(result.getString(0));
                newReliever.setEmpCode(result.getString(1));
                newReliever.setEmpName(result.getString(2));
                newReliever.setDesigId("2");
                newReliever.setDesigName("Reliever");
                newReliever.setEmp_type("1");
                newReliever.setSub_regular_empId(result.getString(4));

                //adding object to list
                list.add(newReliever);

            }//while closes
        }//if closes

        return list;
    }


    public void deleteExeRelievers()
    {
        SQLiteDatabase myDB=this.getWritableDatabase();

        myDB.delete(EXE_RELIEVERS_TABLE, null, null);
    }


    //method to insert employee into Executive_employees_table
    public boolean insertExeEmployee(String emp_id, String emp_code, String emp_name, String emp_type, String subRegularEmpId)
    {
        int empId=-1;

        try
        {
            empId=Integer.parseInt(emp_id);
        }
        catch (NumberFormatException nfe)
        {
            nfe.printStackTrace();
        }

        SQLiteDatabase myDB=this.getWritableDatabase();

        ContentValues values=new ContentValues();
        values.put(COL_EMP_ID, empId);
        values.put(COL_EMP_CODE, emp_code);
        values.put(COL_EMP_NAME, emp_name);
        values.put(COL_EMP_TYPE, emp_type);
        values.put(COL_SUB_REGULAR_EMP_ID, subRegularEmpId);

        long result=myDB.insert(EXE_EMPLOYEES_TABLE, null, values);

        if(result == -1)
            return false;
        else
            return true;
    }


    //method to get employees from Executive_employees_table
    public ArrayList<ExeEmployeeModel> getExeEmployees()
    {
        ArrayList<ExeEmployeeModel> list=new ArrayList<ExeEmployeeModel>();

        SQLiteDatabase myDB=this.getReadableDatabase();

        Cursor result=myDB.query(EXE_EMPLOYEES_TABLE, new String[]{COL_EMP_ID, COL_EMP_CODE, COL_EMP_NAME, COL_EMP_TYPE, COL_SUB_REGULAR_EMP_ID},
                null, null, null, null, null);

        //adding to list if data is returned
        if(result.getCount() != 0)
        {
            while (result.moveToNext())
            {
                //adding data to object
                ExeEmployeeModel newEmp=new ExeEmployeeModel();
                newEmp.setEmpId(result.getString(0));
                newEmp.setEmpCode(result.getString(1));
                newEmp.setEmpName(result.getString(2));
                newEmp.setEmp_type(result.getString(3));
                newEmp.setSub_regular_empId(result.getString(4));

                //adding object to list
                list.add(newEmp);

            }//while closes
        }//if closes


        return list;
    }


    public void deleteExeEmployees()
    {
        SQLiteDatabase myDB=this.getWritableDatabase();

        myDB.delete(EXE_EMPLOYEES_TABLE, null, null);
    }
}
