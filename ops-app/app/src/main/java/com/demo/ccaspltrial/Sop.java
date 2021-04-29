package com.demo.ccaspltrial;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;
import android.widget.Toast;

import com.demo.ccaspltrial.Utility.Utils;
import com.github.barteksc.pdfviewer.PDFView;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class Sop extends AppCompatActivity {

    Toolbar toolbar;
    PDFView sop_pdfview;
    TextView sopName, sop_empty;

    String sop_id, sop_name, sop_url;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_sop);

        sopName=(TextView)findViewById(R.id.sop_name);
        sop_pdfview=(PDFView)findViewById(R.id.sop_pdfview);
        sop_empty=(TextView)findViewById(R.id.sop_empty);
        toolbar=(Toolbar)findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);
        toolbar.setTitle("SOP");

        Utils.pagePreferences=getSharedPreferences(Utils.pagePref, MODE_PRIVATE);
        Utils.pageEditor=Utils.pagePreferences.edit();
        Utils.pageEditor.putString("LastScreen", "SOP").commit();

        Intent i=getIntent();
        Bundle b=i.getExtras();

        sop_id=b.getString("sop_id");
        sop_name=b.getString("sop_name");
        sop_url=b.getString("sop_url");

        sopName.setText(sop_name);

        if(!sop_url.equalsIgnoreCase(""))
        {
            new RetrievePDFTask().execute(sop_url);
        }
        else
        {
            sop_empty.setVisibility(View.VISIBLE);
        }

    }

    @Override
    public void onBackPressed() {

        startActivity(new Intent(Sop.this, HomePage.class));
        finish();
    }

    //retrieving pdf from url
    private class RetrievePDFTask extends AsyncTask<String, Void, InputStream>
    {
        String error=null;

        @Override
        protected InputStream doInBackground(String... strings) {

            InputStream iStream=null;
            try
            {
                URL url=new URL(strings[0]);
                HttpURLConnection conn=(HttpURLConnection)url.openConnection();
                if(conn.getResponseCode() == 200)
                {
                    iStream=new BufferedInputStream(conn.getInputStream());
                }
            }
            catch (IOException e)
            {
                error=e.getMessage();
                return null;
            }

            return iStream;
        }

        @Override
        protected void onPostExecute(InputStream inputStream) {

            if(error != null)
            {
                Toast.makeText(Sop.this, "error= "+error, Toast.LENGTH_SHORT).show();
            }
            else
            {
                sop_pdfview.fromStream(inputStream).load();
            }
        }
    }
}
