package com.demo.ccaspltrial;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.demo.ccaspltrial.Utility.Utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class HomePage extends AppCompatActivity implements
        TasksFragment.OnFragmentInteractionListener,
        ImagesFragment.OnFragmentInteractionListener,
        MaterialsFragment.OnFragmentInteractionListener,
        SubtractInputFragment.OnFragmentInteractionListener,
        AddInputFragment.OnFragmentInteractionListener,
        ExtraWorkFragment.OnFragmentInteractionListener,
        TrainingQuestionFragment.OnFragmentInteractionListener{

    TabLayout tabLayout;
    ViewPager viewPager;
    NavigationView nav_View;
    static ImageView emp_profileImg;
    DrawerLayout drawerLayout;
    ActionBarDrawerToggle drawerToggle;
    Toolbar toolbar;
    TextView uname;
    String username="";
    ProgressDialog pDialog;
    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);

        tabLayout=(TabLayout)findViewById(R.id.tabLayout);
        viewPager=(ViewPager)findViewById(R.id.viewPager);
        nav_View=(NavigationView)findViewById(R.id.nav_View);
        drawerLayout=(DrawerLayout)findViewById(R.id.drawerLayout);
        toolbar=(Toolbar)findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);

        context=this;

        //setting up the hamburger icon for the navigation drawer
        drawerToggle=new ActionBarDrawerToggle(this, drawerLayout, R.string.drawer_open, R.string.drawer_close);
        drawerLayout.addDrawerListener(drawerToggle);

        //making the hamburger icon visible on the toolbar
        final ActionBar actionBar=getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeAsUpIndicator(R.drawable.hamburger_icon);
        drawerToggle.syncState();

        //Adding tabs to the tab layout
        tabLayout.addTab(tabLayout.newTab().setText("Tasks"));
        tabLayout.addTab(tabLayout.newTab().setText("Images"));
        tabLayout.addTab(tabLayout.newTab().setText("Material Stock"));

        //Adding fragments to the tabs
        TabAdapter tabAdapter=new TabAdapter(this, getSupportFragmentManager(), tabLayout.getTabCount());
        viewPager.setAdapter(tabAdapter);

        //calling method to request required permissions
        Utils.requestRequiredPermissions(this, this);

        //initialising login sharedpreferences
        Utils.loginPreferences=getSharedPreferences(Utils.loginPref, MODE_PRIVATE);
        Utils.loginEditor=Utils.loginPreferences.edit();

        //setting username to navigation drawer header
        View navHeader=nav_View.getHeaderView(0);
        uname=(TextView)navHeader.findViewById(R.id.uname);
        emp_profileImg=(ImageView)navHeader.findViewById(R.id.header_image);
        uname.setText(Utils.loginPreferences.getString("emp_name", ""));

        //setting profile image
        new Thread(new Runnable() {
            @Override
            public void run() {

                if(!Utils.loginPreferences.getString("profileImage", "").equalsIgnoreCase(""))
                {
                    try {
                        URL url = new URL(Utils.loginPreferences.getString("profileImage", ""));
                        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                        connection.setDoInput(true);
                        connection.connect();
                        InputStream input = connection.getInputStream();
                        Bitmap profileBitmap = BitmapFactory.decodeStream(input);

                        //compressing image
                        ByteArrayOutputStream out = new ByteArrayOutputStream();
                        profileBitmap.compress(Bitmap.CompressFormat.JPEG, 75, out);
                        final Bitmap compressedImg= BitmapFactory.decodeStream(new ByteArrayInputStream(out.toByteArray()));

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                emp_profileImg.setImageBitmap(compressedImg);
                            }
                        });
                    }
                    catch (IOException ioe)
                    {
                        ioe.printStackTrace();
                    }
                }//if closes

            }
        }).start();

        //listener for when pages/ tabs are changed
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));

        //checking if last screen was Image Gallery
        Utils.pagePreferences=getSharedPreferences(Utils.pagePref, MODE_PRIVATE);
        if(Utils.pagePreferences.getString("LastScreen", "").equalsIgnoreCase("ImageGallery"))
        {
            //setting selection of tab to images tab
            tabLayout.setScrollPosition(1, 0f, true);
            viewPager.setCurrentItem(1);

            Utils.pageEditor=Utils.pagePreferences.edit();
            Utils.pageEditor.putString("LastScreen", "").commit();
        }

        //listener for selection of tabs
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {

                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        //setting onSelectItemListeners for navigation menu items
        nav_View.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {

                int itemId=menuItem.getItemId();

                //adding action to navigation menu item selection
                switch (itemId)
                {
                    case R.id.profile:
                        startActivity(new Intent(HomePage.this, Profile.class));
                        drawerLayout.closeDrawers();
                        finish();
                        break;

                    case R.id.materialRequired:
                        startActivity(new Intent(HomePage.this, MaterialsRequired.class));
                        drawerLayout.closeDrawers();
                        finish();
                        break;


                    case R.id.materialDelivered:
                        startActivity(new Intent(HomePage.this, MaterialDelivered.class));
                        drawerLayout.closeDrawers();
                        finish();
                        break;

                    case R.id.materialIssues:
                        startActivity(new Intent(HomePage.this, MaterialIssues.class));
                        drawerLayout.closeDrawers();
                        finish();
                        break;

                    case R.id.video:
                        Utils.pageEditor.putString("LastScreen", "HomeScreen").commit();
                        startActivity(new Intent(HomePage.this, TrainingVideos.class));
                        drawerLayout.closeDrawers();
                        finish();
                        break;

                    case R.id.tracker:
                        startActivity(new Intent(HomePage.this, Certifications.class));
                        drawerLayout.closeDrawers();
                        finish();
                        break;


                    case R.id.logout:
                        Utils.loginEditor.clear();
                        Utils.loginEditor.putString("loggedIn", "No");
                        Utils.loginEditor.commit();
                        startActivity(new Intent(HomePage.this, Login.class));
                        drawerLayout.closeDrawers();
                        finish();
                        break;
                }

                return true;
            }
        });


    }



    @Override
    public void onFragmentInteraction(Uri uri) {

    }


    //Adapter class for viewPager
    private class TabAdapter extends FragmentPagerAdapter
    {
        Context context;
        int tabCount;

        //Constructor
        public TabAdapter(Context context, FragmentManager fm, int tabCount)
        {
            super(fm);
            this.context=context;
            this.tabCount=tabCount;
        }

        @Override
        public Fragment getItem(int i) {
            switch (i)
            {
                case 0:
                    //Opening tasks fragment
                    TasksFragment tfr=new TasksFragment();
                    return tfr;

                case 1:
                    //Opening images fragment
                    ImagesFragment ifr=new ImagesFragment();
                    return ifr;

                case 2:
                    //Opening materials fragment
                    MaterialsFragment mfr=new MaterialsFragment();
                    return mfr;

                default:
                    return null;
            }
        }

        @Override
        public int getCount() {
            return tabCount;
        }
    } //Adapter class closes

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(drawerToggle.onOptionsItemSelected(item))
        {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onBackPressed() {

        finish();
    }


}
