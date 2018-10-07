package com.troychuinard.livevotingudacity;

import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
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
import com.troychuinard.livevotingudacity.Model.Poll;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

public class HomeActivity extends AppCompatActivity {

    private static final String POLL_QUESTION = "POLL_QUESTION";
    private static final String POLL_IMAGE_URL = "POLL_IMAGE_URL";
    private static final String FILTER_ACTION = "UPDATE_WIDGET";
    @BindView(R.id.drawer_layout)
    DrawerLayout mDrawerLayout;
    @BindView(R.id.left_drawer)
    ListView mDrawerList;
    @BindView(R.id.action_tool_bar)
    Toolbar toolbar;
    @BindView(R.id.list)
    RecyclerView mRecyclerview;
    @BindView(R.id.myFAB)
    FloatingActionButton mFloatingActionAdd;

    private SharedPreferences mPrefs;
    private SharedPreferences.Editor editor;


    private ActionBarDrawerToggle mDrawerToggle;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthlistener;


    private DatabaseReference mBaseRef;
    private DatabaseReference mPollsRef;

    private LinearLayoutManager mLayoutManager;

    private FirebaseRecyclerAdapter<Poll, PollHolder> mFireAdapter;

    private boolean isOpen;

    private MyReceiver myReceiver;


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
        ButterKnife.bind(HomeActivity.this);
        toolbar = findViewById(R.id.action_tool_bar);
        toolbar.setTitleTextColor(ContextCompat.getColor(getApplicationContext(), R.color.white));
        setSupportActionBar(toolbar);
        setTitle(R.string.app_name);
        Window window = getWindow();

        setReceiver();


        mPrefs = getApplicationContext().getSharedPreferences("MyPrefs", MODE_PRIVATE);
        editor = mPrefs.edit();

        mBaseRef = FirebaseDatabase.getInstance().getReference();
        mPollsRef = mBaseRef.child("Polls");
        mAuth = FirebaseAuth.getInstance();
//        window.setStatusBarColor(getResources().getColor(R.color.actionRed));


        final android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
        }
        mRecyclerview.getItemAnimator().setChangeDuration(0);
        mLayoutManager = new LinearLayoutManager(getApplicationContext());
        mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mLayoutManager.setReverseLayout(true);
        mLayoutManager.setStackFromEnd(true);
        mRecyclerview.setLayoutManager(mLayoutManager);

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

//        mPollsRef.limitToLast(1).addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                String x = (String) dataSnapshot.child("question").getValue();
//                Log.v("testing", x);
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError databaseError) {
//
//            }
//        });
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
    protected void onStart() {
        super.onStart();

        mAuthlistener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = mAuth.getCurrentUser();
                if (user == null) {
                    Intent toSignUpActivity = new Intent(getApplicationContext(), SignupActivity.class);
                    startActivity(toSignUpActivity);
                }
            }
        };
        mAuth.addAuthStateListener(mAuthlistener);


        Query query = FirebaseDatabase.getInstance()
                .getReference()
                .child("Polls");

        FirebaseRecyclerOptions<Poll> options = new FirebaseRecyclerOptions.Builder<Poll>()
                .setQuery(query, Poll.class)
                .build();

        mFireAdapter = new FirebaseRecyclerAdapter<Poll, PollHolder>(options) {
            @Override
            protected void onBindViewHolder(final PollHolder holder, int position, Poll model) {

                holder.mPollQuestion.setText(model.getQuestion());
                String voteCount = String.valueOf(model.getVote_count());

                //Update shared prefs with latest question
                editor.putString(POLL_QUESTION, model.getQuestion());
                editor.putString(POLL_IMAGE_URL, model.getImage_URL());
                editor.apply();

                //TODO: Investigate formatting of vote count for thousands
                holder.mVoteCount.setText(voteCount);
                Picasso.get()
                        .load(model.getImage_URL())
                        .fit()
                        .into(holder.mPollImage);
                holder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent toClickedPoll = new Intent(getApplicationContext(), PollActivity.class);
                        toClickedPoll.putExtra("POLL_ID", mFireAdapter.getRef(holder.getAdapterPosition()).getKey());
                        startActivity(toClickedPoll);
                    }
                });

                //TODO: Cannot understand how to utilize IntentService to update widget
                //TODO: Followed this tutorial: https://www.journaldev.com/20735/android-intentservice-broadcastreceiver
                Intent intent = new Intent(HomeActivity.this, MyService.class);
                intent.putExtra(POLL_QUESTION, model.getQuestion());
                intent.putExtra(POLL_IMAGE_URL, model.getImage_URL());
                startService(intent);
            }

            @Override
            public PollHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View v = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.latest_item, parent, false);
                return new PollHolder(v);
            }
        };

        mRecyclerview.setAdapter(mFireAdapter);
        scrollToPosition();
        mFireAdapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mAuth.removeAuthStateListener(mAuthlistener);
        mFireAdapter.stopListening();
    }

    @Override
    protected void onPause() {
        overridePendingTransition(R.anim.fadein, R.anim.fadeout);

//        unregisterReceiver(myReceiver);
        super.onPause();
    }

    public static class PollHolder extends RecyclerView.ViewHolder {

        TextView mPollQuestion;
        TextView mVoteCount;
        ImageView mPollImage;
        View mView;
        String mTag;


        public PollHolder(View itemView) {
            super(itemView);

            mPollQuestion = itemView.findViewById(R.id.latest_item_question);
            mPollImage = itemView.findViewById(R.id.pollThumbNailImage);
            mVoteCount = itemView.findViewById(R.id.latest_item_poll_count);
            this.mView = itemView;
        }

        public void setTag(String tag) {
            this.mTag = tag;
        }

        public View getViewByTag(String tag) {
            return mView;
        }

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

    private void scrollToPosition(){
        mFireAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                int pollCount = mFireAdapter.getItemCount();
                int lastVisiblePosition = mLayoutManager.findLastCompletelyVisibleItemPosition();

                // If the recycler view is initially being loaded or the user is at the bottom of the list, scroll
                // to the bottom of the list to show the newly added message.
                if (lastVisiblePosition == -1 ||
                        (positionStart >= (pollCount - 1) && lastVisiblePosition == (positionStart - 1))) {
                    mRecyclerview.scrollToPosition(positionStart);
                }
            }
        });
    }

    private void setReceiver(){
        myReceiver = new MyReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(FILTER_ACTION);
//        LocalBroadcastManager.getInstance(this).registerReceiver(myReceiver, intentFilter);
    }

    private class MyReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            String poll_Question = intent.getStringExtra("broadcastMessage");

            //TODO: Update Widget - used answer from https://stackoverflow.com/questions/3455123/programmatically-update-widget-from-activity-service-receiver
            int[] ids = AppWidgetManager.getInstance(getApplicationContext()).getAppWidgetIds(new ComponentName(getApplicationContext(), PollWidgetProvider.class));
            PollWidgetProvider pollWidgetProvider = new PollWidgetProvider();
            pollWidgetProvider.onUpdate(getApplicationContext(), AppWidgetManager.getInstance(getApplicationContext()),ids);
        }
    }


}
