package com.troychuinard.livevotingudacity;

import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.transition.Explode;
import android.transition.Transition;
import android.transition.TransitionManager;
import android.transition.TransitionSet;
import android.view.View;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.troychuinard.livevotingudacity.Fragment.BlankFragment;
import com.troychuinard.livevotingudacity.Fragment.FeedFragment;
import com.troychuinard.livevotingudacity.Fragment.PollFragment;
import com.troychuinard.livevotingudacity.Model.Poll;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

public class HomeActivity extends AppCompatActivity implements FeedFragment.OnFragmentInteractionListener, PollFragment.OnFragmentInteractionListener, BlankFragment.OnFragmentInteractionListener {

    @BindView(R.id.drawer_layout)
    DrawerLayout mDrawerLayout;
    @BindView(R.id.left_drawer)
    ListView mDrawerList;
    @BindView(R.id.action_tool_bar)
    Toolbar toolbar;
    @BindView(R.id.myFAB)
    FloatingActionButton mFloatingActionAdd;


    private ActionBarDrawerToggle mDrawerToggle;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthlistener;

    private boolean mTwoPane;


    private boolean isOpen;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Set Transition
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            overridePendingTransition(R.anim.fadein, R.anim.fadeout);

            // Apply activity transition
        } else {
            // Swap without transition
        }
        setContentView(R.layout.activity_home);
        ButterKnife.bind(this);
        toolbar = findViewById(R.id.action_tool_bar);
        toolbar.setTitleTextColor(ContextCompat.getColor(getApplicationContext(), R.color.white));
        setSupportActionBar(toolbar);
        setTitle(R.string.app_name);
        Window window = getWindow();
        mTwoPane = false;

        if (savedInstanceState == null){
            FeedFragment feedFragment = new FeedFragment();
            FragmentManager fm = getSupportFragmentManager();
            fm.beginTransaction()
                    .add(R.id.poll_feed_fragment, feedFragment)
                    .commit();

            if (findViewById(R.id.two_pane_constraint_layout) != null & getApplicationContext().getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE){
                mTwoPane = true;
                BlankFragment blankFragment = new BlankFragment();
                fm.beginTransaction()
                        .add(R.id.poll_fragment, blankFragment)
                        .commit();
            }
        }

//        window.setStatusBarColor(getResources().getColor(R.color.actionRed));


        final android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
        }


        mFloatingActionAdd = findViewById(R.id.myFAB);
        mFloatingActionAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent I = new Intent(getApplicationContext(), CreateActivity.class);
                startActivity(I);
            }
        });


        mDrawerToggle = new ActionBarDrawerToggle(
                this,                  /* host Activity */
                mDrawerLayout,         /* DrawerLayout object */
                toolbar,  /* nav drawer icon to replace 'Up' caret */
                R.string.drawer_open,  /* "open drawer" description */
                R.string.drawer_close  /* "close drawer" description */
        ) {

            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                isOpen = false;
            }

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                isOpen = true;
            }

        };

        ArrayList<String> drawerTitleArray = new ArrayList<>();
        drawerTitleArray.add(0, "Home");
        drawerTitleArray.add(1, "Sign Out");
        // Set the adapter for the list view
        mDrawerList.setAdapter(new ArrayAdapter<>(this,
                R.layout.drawer_list_item,
                drawerTitleArray));
        LayoutInflater inflater = getLayoutInflater();
        ViewGroup header = (ViewGroup) inflater.inflate(R.layout.drawer_header, mDrawerList, false);
//        mDrawerList.addHeaderView(header);
        mDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                switch (i) {
                    case 0:
                        Toast.makeText(getApplicationContext(), "CLICKED 0", Toast.LENGTH_SHORT).show();
                        Log.v("DRAWER", "THE DRAWER HAS BEEN CLICKED");
                        if (getCurrentActivity() instanceof HomeActivity) {
                            mDrawerLayout.closeDrawer(Gravity.START);
                        } else {
                            Intent I = new Intent(getApplicationContext(), HomeActivity.class);
                            startActivity(I);
                        }
                        break;
                    case 1:
                        mAuth.signOut();
                }

            }
        });



    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return mDrawerToggle.onOptionsItemSelected(item) || super.onOptionsItemSelected(item);
    }


    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }


    @Override
    protected void onPause() {
        overridePendingTransition(R.anim.fadein, R.anim.fadeout);

//        unregisterReceiver(myReceiver);
        super.onPause();
    }


    public static Activity getCurrentActivity() {
        try {
            Class activityThreadClass = Class.forName("android.app.ActivityThread");
            Object activityThread = activityThreadClass.getMethod("currentActivityThread").invoke(
                    null);
            Field activitiesField = activityThreadClass.getDeclaredField("mActivities");
            activitiesField.setAccessible(true);
            Map activities = (Map) activitiesField.get(activityThread);
            for (Object activityRecord : activities.values()) {
                Class activityRecordClass = activityRecord.getClass();
                Field pausedField = activityRecordClass.getDeclaredField("paused");
                pausedField.setAccessible(true);
                if (!pausedField.getBoolean(activityRecord)) {
                    Field activityField = activityRecordClass.getDeclaredField("activity");
                    activityField.setAccessible(true);
                    Activity activity = (Activity) activityField.get(activityRecord);
                    return activity;
                }
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }
}


