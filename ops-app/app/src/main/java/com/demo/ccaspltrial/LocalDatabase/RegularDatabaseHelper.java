package com.demo.ccaspltrial.LocalDatabase;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.demo.ccaspltrial.Utility.CertificationModel;
import com.demo.ccaspltrial.Utility.MaterialsModel;
import com.demo.ccaspltrial.Utility.TasksModel2;

import java.util.ArrayList;

public class RegularDatabaseHelper extends SQLiteOpenHelper {

    //database name
    public static final String DB_NAME="CCASPL_OPERATIONS_APP_REGULAR.db";

    //table names
    public static final String TASK_TABLE="Tasks_table"; //table to store tasks of the day
    public static final String MATERIAL_TABLE="Materials_table"; //table to store tasks of the day
    public static final String IMAGES_TABLE="Images_table"; // table to store images
    public static final String CERTIFICATIONS_TABLE="Certifications_table"; //table to store certifications

    //columns of Tasks_table
    public static final String COL_TASK_ID="task_id"; //stores task id
    public static final String COL_TASK_NAME="task_name"; //stores task name
    public static final String COL_TASK_TIME="task_time"; //stores time of task
    public static final String COL_SOP_ID="sop_id"; //stores sop id for the task
    public static final String COL_SOP_NAME="sop_name"; //stores sop name
    public static final String COL_SOP_DESCRIPTION="sop_description"; //stores sop description

    //columns of Materials_table
    public static final String COL_MATERIAL_ID="material_id"; //stores material id
    public static final String COL_MATERIAL_CODE="material_code"; //stores material code
    public static final String COL_MATERIAL_NAME="material_name"; //stores material name
    public static final String COL_UNIT_QUANTITY="unit_quantity"; //stores unit of quantity of material
    public static final String COL_MATERIAL_RATE="material_rate"; //stores material rate
    public static final String COL_BALANCE_AMOUNT="balance_amount"; //stores balance amount of material at the site
    public static final String COL_REQUIREMENT_STATUS="requirement_status"; //stores the requirement status of the material
    public static final String COL_REQUIRED_AMOUNT="required_amount"; //stores the amount of material required

    //columns of Images_table
    public static final String COL_IMAGE_ID="image_id"; //stores image id
    public static final String COL_IMAGE_TYPE="image_type"; //stores the image type id
    public static final String COL_IMAGE_URL="image_url"; //stores the image blob
    public static final String COL_TIMESTAMP="timestamp"; //stores the time/date/month of the image

    //columns of Certifications_table
    public static final String COL_CERT_ID="cert_id"; //stores certification id
    public static final String COL_CERT_NAME="cert_name"; //stores certification id

    //queries to create the above tables
    //to create Tasks_table
    private final String createTasksTable="CREATE TABLE " + TASK_TABLE + "\n" +
            "( " + COL_TASK_ID + " INTEGER PRIMARY KEY, \n"
            + COL_TASK_NAME + " TEXT, \n"
            + COL_TASK_TIME + " TEXT, \n"
            + COL_SOP_ID + " INTEGER, \n"
            + COL_SOP_NAME + " TEXT, \n"
            + COL_SOP_DESCRIPTION + " TEXT )";

    //to create Materials_table
    private final String createMaterialsTable="CREATE TABLE " + MATERIAL_TABLE + "\n" +
            "( " + COL_MATERIAL_ID + " INTEGER PRIMARY KEY, \n"
            + COL_MATERIAL_CODE + " TEXT, \n"
            + COL_MATERIAL_NAME + " TEXT, \n"
            + COL_UNIT_QUANTITY + " TEXT, \n"
            + COL_MATERIAL_RATE + " TEXT, \n"
            + COL_BALANCE_AMOUNT + " INTEGER, \n"
            + COL_REQUIREMENT_STATUS + " INTEGER DEFAULT 0, \n"
            + COL_REQUIRED_AMOUNT + " INTEGER DEFAULT 0 )";

    //to create Images_table
    private final String createImagesTable="CREATE TABLE " + IMAGES_TABLE + "\n" +
            "( " + COL_IMAGE_ID + " INTEGER PRIMARY KEY, \n"
            + COL_IMAGE_TYPE + " INTEGER, \n"
            + COL_IMAGE_URL + " TEXT, \n"
            + COL_TIMESTAMP + " TEXT )";

    //to create Certifications_table
    private final String createCertificationsTable="CREATE TABLE " + CERTIFICATIONS_TABLE + "\n" +
            "( " + COL_CERT_ID + " INTEGER PRIMARY KEY, \n"
            + COL_CERT_NAME + " TEXT )";


    public RegularDatabaseHelper(Context context, int versionCount) {
        super(context, DB_NAME, null, versionCount);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL(createTasksTable);
        db.execSQL(createMaterialsTable);
        db.execSQL(createImagesTable);
        db.execSQL(createCertificationsTable);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        db.execSQL("DROP TABLE IF EXISTS " +TASK_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " +MATERIAL_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " +IMAGES_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " +CERTIFICATIONS_TABLE);

        onCreate(db);
    }


    //method to insert task into Tasks_table
    public boolean insertTask(String task_id, String task_name, String task_time, String sop_id, String sop_name, String sop_desc)
    {
        int taskId=-1, sopId=-1;

        try
        {
            taskId=Integer.parseInt(task_id);
            sopId=Integer.parseInt(sop_id);
        }
        catch (NumberFormatException nfe)
        {
            nfe.printStackTrace();
        }

        SQLiteDatabase myDB=this.getWritableDatabase();

        ContentValues values=new ContentValues();
        values.put(COL_TASK_ID, taskId);
        values.put(COL_TASK_NAME, task_name);
        values.put(COL_TASK_TIME, task_time);
        values.put(COL_SOP_ID, sopId);
        values.put(COL_SOP_NAME, sop_name);
        values.put(COL_SOP_DESCRIPTION, sop_desc);

        long result=myDB.insert(TASK_TABLE, null, values);

        if(result == -1)
            return false;
        else
            return true;

    }

    //method to retrieve tasks from Tasks_table
    public ArrayList<TasksModel2> getTasks()
    {
        ArrayList<TasksModel2> list=new ArrayList<TasksModel2>();

        SQLiteDatabase myDB=this.getReadableDatabase();

        Cursor result=myDB.query(TASK_TABLE, new String[]{COL_TASK_ID, COL_TASK_NAME, COL_TASK_TIME,
                COL_SOP_ID, COL_SOP_NAME, COL_SOP_DESCRIPTION}, null, null,
                null, null, null);

        //adding data to list if data has been returned
        if(result.getCount() != 0)
        {
            while (result.moveToNext())
            {
                //adding row data to object
                TasksModel2 newTask=new TasksModel2();
                newTask.setTaskId(result.getString(0));
                newTask.setTaskName(result.getString(1));
                newTask.setTaskTime(result.getString(2));
                newTask.setSopId(result.getString(3));
                newTask.setSopName(result.getString(4));
                newTask.setSopDescript(result.getString(5));

                //adding object to list
                list.add(newTask);

            }//while closes
        }//if closes

        return list;
    }

    public void deleteTasks()
    {
        SQLiteDatabase myDB=this.getWritableDatabase();

        myDB.delete(TASK_TABLE, null, null);
    }


    //method to insert material into Materials_table
    public boolean insertMaterial(String mat_id, String mat_code, String mat_name, String unit_quant,
                                  String mat_rate, String balance_amt, String status, String amt_required)
    {
        int matId=-1, balanceAmt=-1, statusReq=-1, reqAmt=-1;

        try
        {
            matId=Integer.parseInt(mat_id);
            balanceAmt=Integer.parseInt(balance_amt);
            statusReq=Integer.parseInt(status);
            reqAmt=Integer.parseInt(amt_required);
        }
        catch (NumberFormatException nfe)
        {
            nfe.printStackTrace();
        }

        SQLiteDatabase myDB=this.getWritableDatabase();

        ContentValues values=new ContentValues();
        values.put(COL_MATERIAL_ID, matId);
        values.put(COL_MATERIAL_CODE, mat_code);
        values.put(COL_MATERIAL_NAME, mat_name);
        values.put(COL_UNIT_QUANTITY, unit_quant);
        values.put(COL_MATERIAL_RATE, mat_rate);
        values.put(COL_BALANCE_AMOUNT, balanceAmt);
        values.put(COL_REQUIREMENT_STATUS, statusReq);
        values.put(COL_REQUIRED_AMOUNT, reqAmt);

        long result=myDB.insert(MATERIAL_TABLE, null, values);

        if(result == -1)
            return false;
        else
            return true;
    }


    //method to retrieve materials from Materials_table
    public ArrayList<MaterialsModel> getMaterials()
    {
        ArrayList<MaterialsModel> list=new ArrayList<MaterialsModel>();

        SQLiteDatabase myDB=this.getReadableDatabase();

        Cursor result=myDB.query(MATERIAL_TABLE, new String[]{COL_MATERIAL_ID, COL_MATERIAL_CODE,
                        COL_MATERIAL_NAME, COL_UNIT_QUANTITY, COL_MATERIAL_RATE, COL_BALANCE_AMOUNT,
                        COL_REQUIREMENT_STATUS, COL_REQUIRED_AMOUNT}, null, null,
                null, null, null);

        //adding materials to list if data is retrieved
        if(result.getCount() != 0)
        {
            while (result.moveToNext())
            {
                //adding data to object
                MaterialsModel newMaterial=new MaterialsModel();
                newMaterial.setMaterialId(result.getString(0));
                newMaterial.setMaterialCode(result.getString(1));
                newMaterial.setMaterialName(result.getString(2));
                newMaterial.setUnit_quant(result.getString(3));
                newMaterial.setMaterialRate(result.getString(4));
                newMaterial.setMaterialQuantity(result.getInt(5));
                newMaterial.setMatRequireQuantity(result.getInt(6));

                //adding object to list
                list.add(newMaterial);

            }//while closes
        }//if closes


        return list;
    }


    public void deleteMaterials()
    {
        SQLiteDatabase myDB=this.getWritableDatabase();

        myDB.delete(MATERIAL_TABLE, null, null);
    }


    //method to insert image into Images_table
    public boolean insertImage(int image_id, String image_type, String image_url, String timestamp)
    {
        int imageType=-1;

        try
        {
            imageType=Integer.parseInt(image_type);
        }
        catch (NumberFormatException nfe)
        {
            nfe.printStackTrace();
        }

        SQLiteDatabase myDB=this.getWritableDatabase();

        ContentValues values=new ContentValues();
        values.put(COL_IMAGE_ID, image_id);
        values.put(COL_IMAGE_TYPE, imageType);
        values.put(COL_IMAGE_URL, image_url);
        values.put(COL_TIMESTAMP, timestamp);

        long result=myDB.insert(IMAGES_TABLE, null, values);

        if(result == -1)
            return false;
        else
            return true;
    }


    //method to retrieve images from Images_table
    public Cursor getImages(String image_type)
    {
        SQLiteDatabase myDB=this.getReadableDatabase();

        Cursor result=myDB.query(IMAGES_TABLE, new String[]{COL_IMAGE_ID, COL_IMAGE_URL, COL_TIMESTAMP},
                COL_IMAGE_TYPE+"=?", new String[]{image_type}, null, null, null);

        return result;
    }


    public void deleteImages(String imageType)
    {
        SQLiteDatabase myDB=this.getWritableDatabase();

        myDB.delete(IMAGES_TABLE, COL_IMAGE_TYPE + "=?", new String[]{imageType});
    }


    //method to insert certification into Certifications_table
    public boolean insertCertification(String cert_id, String cert_name)
    {
        int certId=-1;

        try
        {
            certId=Integer.parseInt(cert_id);
        }
        catch (NumberFormatException nfe)
        {
            nfe.printStackTrace();
        }

        SQLiteDatabase myDB=this.getWritableDatabase();

        ContentValues values=new ContentValues();
        values.put(COL_CERT_ID, certId);
        values.put(COL_CERT_NAME, cert_name);

        long result=myDB.insert(CERTIFICATIONS_TABLE, null, values);

        if(result == -1)
            return false;
        else
            return true;
    }


    //method to retrieve certifications from Certifications_table
    public ArrayList<CertificationModel> getCertifications()
    {
        ArrayList<CertificationModel> list=new ArrayList<CertificationModel>();

        SQLiteDatabase myDB=this.getReadableDatabase();

        Cursor result=myDB.query(CERTIFICATIONS_TABLE, new String[]{COL_CERT_ID, COL_CERT_NAME},
                null, null, null, null, null);

        //adding certifications to list if data is retrieved
        if(result.getCount() != 0)
        {
            while (result.moveToNext())
            {
                //adding data to object
                CertificationModel newCert=new CertificationModel();
                newCert.setCertId(result.getString(0));
                newCert.setCertName(result.getString(1));

                //adding object to list
                list.add(newCert);

            }//while closes
        }//if closes


        return  list;
    }


    public void deleteCertifications()
    {
        SQLiteDatabase myDB=this.getWritableDatabase();

        myDB.delete(CERTIFICATIONS_TABLE, null, null);
    }


    //method to update material balance data
    public boolean updateMaterialBalance(String mat_id, int balance_amt)
    {
        SQLiteDatabase myDB=this.getWritableDatabase();

        ContentValues values=new ContentValues();
        values.put(COL_BALANCE_AMOUNT, balance_amt);

        int result=myDB.update(MATERIAL_TABLE, values, COL_MATERIAL_ID+"=?",
                new String[]{mat_id});

        if(result == 0)
            return false;
        else
            return true;
    }
}
