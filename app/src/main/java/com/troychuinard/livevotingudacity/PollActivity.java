package com.troychuinard.livevotingudacity;

import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
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
import android.util.TypedValue;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;


import com.github.mikephil.charting.charts.HorizontalBarChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.troychuinard.livevotingudacity.Model.MyDataValueFormatter;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;

public class PollActivity extends AppCompatActivity  {

    private Toolbar toolbar;

    @BindView(R.id.comment_label_counter)
    TextView mCommentCounter;
    @BindView(R.id.comments_label_icon)
    ImageView mCommentsLabelIcon;
    private FirebaseAuth mAuth;


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
//    private FirebaseAuth mAuth;

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
        mIntentFromTouch = getIntent();
        final String pollIndex = mIntentFromTouch.getStringExtra("POLL_ID");

        mAuth = FirebaseAuth.getInstance();
        mUserID = mAuth.getCurrentUser().getUid();
        mBaseRef = FirebaseDatabase.getInstance().getReference();
        mPollsRef = mBaseRef.child(POLL_LABEL);
        mSelectedPollRef = mPollsRef.child(pollIndex);
        mUsersRef = mBaseRef.child(USERS_LABEL);

        //Initialize Poll Results Bar Chart and set to Invisible
        mPollResults = (HorizontalBarChart) findViewById(R.id.poll_results_chart);
        mPollResults.setBackgroundColor(getResources().getColor(R.color.white));
        mPollResults.setNoDataTextDescription(getResources().getString(R.string.no_results_description));
        mPollResults.setVisibility(View.INVISIBLE);

        mTotalVoteCounter = (TextView) findViewById(R.id.total_vote_counter);
        mCommentCounter = (TextView) findViewById(R.id.comment_label_counter);
        mDisplayName = (TextView) findViewById(R.id.creator_id);

        mProgressBar = (ProgressBar) findViewById(R.id.progress_bar_white);

        mPollQuestion = (TextView) findViewById(R.id.poll_question);
        mPollQuestion.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.poll_question_text_size));
        mPollQuestionRadioGroup = (RadioGroup) findViewById(R.id.poll_question_group);
        mFollowingCheck = (CheckBox) findViewById(R.id.following_check);

        mParams = new RadioGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);


        mSelectedPollRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                Log.v("KEY", dataSnapshot.getKey());

                Log.v("POLL_ID", "The poll ID is " + pollID);

                Log.v("TAG", dataSnapshot.toString());

                for (DataSnapshot d : dataSnapshot.getChildren()){
                    Log.v("TAG", d.toString());
                }

                //add question
                String pollQuestion = String.valueOf(dataSnapshot.child(QUESTION_LABEL).getValue());
                Log.v("TAG", "THE POLL QUESTION IS " + pollQuestion);
                mPollQuestion.setText(pollQuestion);
                mPollQuestion.setTypeface(null, Typeface.BOLD);

                mPollCreatorDisplayName = String.valueOf(dataSnapshot.child(DISPLAY_NAME_LABEL).getValue());
                mPollCreatorID = String.valueOf(dataSnapshot.child(USER_ID_LABEL).getValue());
                mDisplayName.setText(mPollCreatorDisplayName);
                mDisplayName.setTypeface(null, Typeface.BOLD);


                //add image
                String pollImageURL = (String) dataSnapshot.child(IMAGE_URL).getValue();
                Log.v("TAG", "THE POLL IMAGE URL IS" + pollImageURL);
                Picasso.get()
                        .load(pollImageURL)
                        .fit()
                        .placeholder(R.drawable.loading_spinner_white)
                        .into((ImageView)findViewById(R.id.poll_image));

                //Load vote count
                Long totalVoteCount = (Long) dataSnapshot.child(VOTE_COUNT_LABEL).getValue();
                mTotalVoteCounter.setText(getResources().getString(R.string.total_vote_counter) + " " + totalVoteCount);
                mTotalVoteCounter.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.vote_count_label_text_size_lower_right));
                mCommentCounter.setText("52");
                mCommentCounter.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.vote_count_label_text_size_lower_right));

                //create radio buttons
                int pollAnswerCount = (int) dataSnapshot.child(ANSWERS_LABEL).getChildrenCount();
                addRadioButtonsWithFirebaseAnswers(pollAnswerCount, dataSnapshot);

                //create initial bar graph results
//                assignInitialBarGraphResults(dataSnapshot);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mPollQuestionRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {

                disableVoteButtons(group);
                Toast.makeText(getApplicationContext(),R.string.vote_submitted_label, Toast.LENGTH_LONG).show();

                int checkedRadioButtonID = group.getCheckedRadioButtonId();
                Log.e("checkedIDCHECK", "the checkedID is " + checkedRadioButtonID);
                String selectedRadioButtonPollAnswer = mPollAnswerArrayList.get(checkedRadioButtonID).getText().toString();
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                String UID = user.getUid();
                //TODO: Add user reference in Firebase and update
//                mUserRef.child(UID).child("Poll_Votes").child(pollID).child("Vote_Selection").setValue(selectedRadioButtonPollAnswer);
//                mUserRef.child(UID).child("Poll_Votes").child(pollID).child("Has_Voted").setValue("True");


                //Update Firebase poll total vote count
                increasePollTotalVoteCounter(checkedRadioButtonID);
                //Update Firebase poll individual answer vote count
                increasePollAnswerVoteCounter(checkedRadioButtonID);

                //TODO: add animation between between vote click and chart display
                mPollQuestionRadioGroup.setVisibility(View.INVISIBLE);
                mPollResults.setVisibility(View.VISIBLE);
            }

        });


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
    protected void onStart() {
        super.onStart();valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                int numberOfPollAnswersAtIndexBelowDate = (int) dataSnapshot.child(ANSWERS_LABEL).getChildrenCount();
                updatePollResultAnswersDynamically(numberOfPollAnswersAtIndexBelowDate, dataSnapshot);

                //Simply displaying, no need for mutable transaction
                int voteCountTotal = 0;
                ArrayList<Long> pollAnswersTotals = new ArrayList<Long>();
                for (int x = 0; x < numberOfPollAnswersAtIndexBelowDate; x++) {
                    pollAnswersTotals.add(x, (Long) dataSnapshot.child(ANSWERS_LABEL).child(String.valueOf(x + 1)).child(VOTE_COUNT_LABEL).getValue());
                    voteCountTotal += pollAnswersTotals.get(x);
                }

                DecimalFormat formatter = new DecimalFormat("#,###,###");
                String totalFormattedVotes = formatter.format(voteCountTotal);
                mTotalVoteCounter.setText("Votes: " + String.valueOf(totalFormattedVotes));
//                mRefAtPollIndexImmediatelyBelowDate.child(VOTE_COUNT_LABEL).setValue(voteCountTotal);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        mSelectedPollRef.addValueEventListener(valueEventListener);
    }


    @Override
    protected void onStop() {
        super.onStop();
    }

    private void disableVoteButtons(RadioGroup group) {
        //disable voting
        for (int i = 0; i < group.getChildCount(); i++) {
            group.getChildAt(i).setEnabled(false);
        }
    }

    private void increasePollAnswerVoteCounter(int checkedRadioButtonID) {
        mSelectedPollRef.child(ANSWERS_LABEL).child(String.valueOf(checkedRadioButtonID + 1)).child(VOTE_COUNT_LABEL).runTransaction(new Transaction.Handler() {
            @Override

            public Transaction.Result doTransaction(MutableData mutableData) {
                mutableData.setValue((Long) mutableData.getValue() + 1);
                return Transaction.success(mutableData);

            }

            @Override
            public void onComplete(DatabaseError firebaseError, boolean b, DataSnapshot dataSnapshot) {
                if (firebaseError != null) {
                    Toast.makeText(getApplicationContext(), firebaseError.getMessage(), Toast.LENGTH_LONG).show();

                }
            }

        });
    }

    private void increasePollTotalVoteCounter(int checkedRadioButtonID) {
        mSelectedPollRef.child("vote_count").runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                mutableData.setValue((Long) mutableData.getValue() + 1);
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {
                if (databaseError != null){
                    Toast.makeText(getApplicationContext(), databaseError.getMessage(), Toast.LENGTH_LONG).show();
                }

            }
        });
    }

    private void addRadioButtonsWithFirebaseAnswers(int numberOfAnswers, DataSnapshot dataSnapshot) {
        mPollAnswerArrayList = new ArrayList<RadioButton>();
        for (int i = (numberOfAnswers - 1); i >= 0; i--) {
            Log.e("Number of Answers", "The number of answers is " + numberOfAnswers);
            int indexCreated = ((numberOfAnswers - 1) - i);
            mPollAnswerArrayList.add((indexCreated), new RadioButton((getApplicationContext())));
            mPollAnswerArrayList.get(indexCreated).setId(indexCreated);
            String firebaseChild = String.valueOf(indexCreated + 1);
            //TODO:Sould be getValue instead of getKey?
            mPollAnswerArrayList.get(indexCreated).setText(((String) dataSnapshot.child(ANSWERS_LABEL).child(firebaseChild).child("answer").getValue()));
            mPollAnswerArrayList.get(indexCreated).setTextColor(getResources().getColor(R.color.black));
            mPollAnswerArrayList.get(indexCreated).setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.radio_button_answer_text_size));

            //TODO: Determne which type of RadioButton to use for consistency; rendering on different API levels
            if (Build.VERSION.SDK_INT >= 21) {
                mPollAnswerArrayList.get(indexCreated).setButtonTintMode(PorterDuff.Mode.DARKEN);
            } else {
                mPollAnswerArrayList.get(indexCreated).setButtonDrawable(R.drawable.black_ring);
            }

            mParams.setMargins((int) getResources().getDimension(R.dimen.radio_question_margin_left), 0, 0, (int) getResources().getDimension(R.dimen.radio_question_margin_bottom));
            mPollQuestionRadioGroup.addView(mPollAnswerArrayList.get(indexCreated), mParams);
        }
    }

    private void updatePollResultAnswersDynamically(int numberOfAnswers, DataSnapshot dataSnapshot) {

//        mPollResults.clearValues();

        pollResultChartValues = new ArrayList<BarEntry>();

        for (int i = 0; i < numberOfAnswers; i++) {
            Long chartLongResultvalue = (Long) dataSnapshot.child(ANSWERS_LABEL).child(String.valueOf(numberOfAnswers - i)).child(VOTE_COUNT_LABEL).getValue();
            float chartFloatResultvalue = chartLongResultvalue.floatValue();
            pollResultChartValues.add(new BarEntry(chartFloatResultvalue, i));
        }

        data = new BarDataSet(pollResultChartValues, "Poll Results");

//        data.setColors(new int[]{getResources().getColor(R.color.black)});
        //TODO: Check attachment to Activity; when adding a color, the getResources.getColor states
        //TODO: that the fragment was detached from the activity; potentially add this method to onCreateView() to avoid;
        ArrayList<Integer> barColors = new ArrayList<>();
        barColors.add(ContextCompat.getColor(getApplicationContext(), R.color.bar_one));
        barColors.add(ContextCompat.getColor(getApplicationContext(), R.color.bar_two));
        barColors.add(ContextCompat.getColor(getApplicationContext(), R.color.bar_three));
        barColors.add(ContextCompat.getColor(getApplicationContext(), R.color.bar_four));
        barColors.add(ContextCompat.getColor(getApplicationContext(), R.color.bar_five));
        data.setColors(barColors);

        data.setAxisDependency(YAxis.AxisDependency.LEFT);
        MyDataValueFormatter f = new MyDataValueFormatter();
        data.setValueFormatter(f);


        //xAxis is a inverted yAxis since the graph is horizontal
        XAxis xAxis = mPollResults.getXAxis();
        xAxis.setDrawGridLines(false);
        xAxis.setDrawAxisLine(false);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        if ((getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_XLARGE) {
            xAxis.setTextSize(20);
        } else if ((getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_LARGE) {
            xAxis.setTextSize(16);
        }

        //yAxis is an inverted xAxis since the graph is horizontal
        YAxis yAxisLeft = mPollResults.getAxisLeft();
        yAxisLeft.setDrawGridLines(false);
        yAxisLeft.setEnabled(false);
        YAxis yAxisRight = mPollResults.getAxisRight();
        yAxisRight.setDrawGridLines(false);
        yAxisRight.setEnabled(false);


        Legend legend = mPollResults.getLegend();
        legend.setEnabled(false);


        //TODO: This figure needs to be dynamic and needs to adjust based on the number of users in the application
        //TODO: Or does it? Right now, it scales without it

        dataSets = new ArrayList<IBarDataSet>();

        dataSets.add(data);


        //Poll Answer Options get added here
        ArrayList<String> yVals = new ArrayList<String>();
        for (int x = 0; x < numberOfAnswers; x++) {
            String pollResult = (String) dataSnapshot.child(ANSWERS_LABEL).child(String.valueOf((numberOfAnswers) - x)).child("answer").getValue();
            yVals.add(x, pollResult);
        }

        BarData testBarData = new BarData(yVals, dataSets);
        //TODO: Fix all text sizes using this method
        if ((getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_XLARGE) {
            testBarData.setValueTextSize(22);
        } else if ((getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_LARGE) {
            testBarData.setValueTextSize(14);
        } else if ((getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_NORMAL) {
            testBarData.setValueTextSize(12);
        } else if ((getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_SMALL) {
            testBarData.setValueTextSize(12);
        }

        mPollResults.getXAxis().setLabelRotationAngle(-15);
        mPollResults.getXAxis().setSpaceBetweenLabels(5);
        mPollResults.setTouchEnabled(false);
        mPollResults.setPinchZoom(false);
        mPollResults.setData(testBarData);
        data.notifyDataSetChanged();
        mPollResults.notifyDataSetChanged();
        mPollResults.invalidate();
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