package com.troychuinard.livevotingudacity;

import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;


import com.github.mikephil.charting.charts.HorizontalBarChart;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;

import butterknife.ButterKnife;

public class PollHostActivity extends AppCompatActivity  {

    private Toolbar toolbar;


    //    private ScreenSlidePagerAdapter mPagerAdapter;
    private DateFormat mDateFormat;
    private Date mDate;
    private String mCurrentDateString;
    private ValueEventListener v;
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;
    private boolean isOpen;
    private TabLayout mTabLayout;
    private ViewPager mViewPager;
    private Intent mIntentFromTouch;

    private DatabaseReference mBaseRef;
    private DatabaseReference mPollsRef;
    private DatabaseReference mSelectedPollRef;
    private DatabaseReference mUsersRef;
    private FirebaseAuth mAuth;

    private RadioGroup mPollQuestionRadioGroup;
    private RadioGroup.LayoutParams mParams;
    //static
    private TextView mCommentsLabel;

    private TextView mTotalVoteCounter;
    private TextView mSelectedVote;

    private TextView mYourVotelabel;
    private TextView mDisplayName;
    private int mPagerCurrentPosition;
    private String mUserID;
    private String mUserDisplayName;
    private String mPollCreatorID;
    private String mPollCreatorDisplayName;

    private static final String USERS_LABEL = "Users";
    private static final String USER_ID_LABEL = "user_ID";
    private static final String FOLLOWING_LABEL = "Following";
    private static final String VOTE_COUNT_LABEL = "vote_count";
    private static final String QUESTION_LABEL = "question";
    private static final String DISPLAY_NAME_LABEL = "display_name";
    private static final String ANSWERS_LABEL = "answers";
    private static final String POLL_LABEL = "Polls";
    private static final String IMAGE_URL = "image_URL";

    private TextView mPollQuestion;
    private ArrayList<RadioButton> mPollAnswerArrayList;
    private HorizontalBarChart mPollResults;
    ArrayList<BarEntry> pollResultChartValues;
    private BarDataSet data;
    private ArrayList<IBarDataSet> dataSets;
    private ValueEventListener valueEventListener;
    private String pollID;
    private int mPollIndex;
    private ProgressBar mProgressBar;
    private CheckBox mFollowingCheck;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_poll);
        ButterKnife.bind(this);

        toolbar = (Toolbar) findViewById(R.id.action_tool_bar);
        toolbar.setTitleTextColor(ContextCompat.getColor(getApplicationContext(), R.color.white));

        setSupportActionBar(toolbar);
        setTitle(R.string.app_name);
        Window window = getWindow();
        //TODO: Bugging out on API < 21, figure out how to set status bar color in previous Android versions
//        window.setStatusBarColor(getResources().getColor(R.color.actionRed));
        final String pollIndex = mIntentFromTouch.getStringExtra("POLL_ID");

        mAuth = FirebaseAuth.getInstance();
        mUserID = mAuth.getCurrentUser().getUid();
        mBaseRef = FirebaseDatabase.getInstance().getReference();
        mPollsRef = mBaseRef.child(POLL_LABEL);
        mSelectedPollRef = mPollsRef.child(pollID);
        mUsersRef = mBaseRef.child(USERS_LABEL);


        mIntentFromTouch = getIntent();

        final android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
        }


        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerToggle = new ActionBarDrawerToggle(
                this,                  /* host Activity */
                mDrawerLayout,         /* DrawerLayout object */
                toolbar,  /* nav drawer icon to replace 'Up' caret */
                R.string.drawer_open,  /* "open drawer" description */
                R.string.drawer_close  /* "close drawer" description */
        ) {

            /**
             * Called when a drawer has settled in a completely closed state.
             */
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                isOpen = false;
            }

            /**
             * Called when a drawer has settled in a completely open state.
             */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                isOpen = true;
            }
        };
        mDrawerToggle.setToolbarNavigationClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        mDrawerToggle.setDrawerIndicatorEnabled(false);
        mDrawerLayout.addDrawerListener(mDrawerToggle);

        mDrawerList = (ListView) findViewById(R.id.left_drawer);
        ArrayList<String> drawerTitleArray = new ArrayList<>();
        drawerTitleArray.add(0, "TEST");
        drawerTitleArray.add(1, "TEST 1");
        // Set the adapter for the list view
        mDrawerList.setAdapter(new ArrayAdapter<String>(this,
                R.layout.drawer_list_item, drawerTitleArray));




        // TODO: Checkn if AuthStateListenerNecessary
        //Determine whether necessary to use an AuthStateListener here
//        mUserRef.addAuthStateListener(new Firebase.AuthStateListener() {
//            @Override
//            public void onAuthStateChanged(AuthData authData) {
//                if (authData == null) {
//                    Intent backToSignIn = new Intent(getApplication(), SignupActivity.class);
//                    startActivity(backToSignIn);
//                    finish();
//                }
//            }
//        })




    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        if (item.getItemId() == android.R.id.home){
            super.onBackPressed();
            return true;}
        else
            return mDrawerToggle.onOptionsItemSelected(item) || super.onOptionsItemSelected(item);
    }



    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        //TODO: Be sure to only allow one instance of each activity
        //TODO:Address mDrawerToggle code from StackOverflow to make sure I am correctly implementing the return to previous activity
        // Sync the toggle state after onRestoreInstanceState has occurred.
//        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }


}