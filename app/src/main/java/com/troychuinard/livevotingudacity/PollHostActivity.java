package com.troychuinard.livevotingudacity;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.troychuinard.livevotingudacity.Fragment.FeedFragment;
import com.troychuinard.livevotingudacity.Fragment.PollFragment;
import com.troychuinard.livevotingudacity.Model.Poll;

import butterknife.ButterKnife;

public class PollHostActivity extends AppCompatActivity implements PollFragment.OnFragmentInteractionListener {

    private static final String POLL_ID = "POLL_ID";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_poll_host);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setSupportActionBar(toolbar);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            overridePendingTransition(R.anim.fadein, R.anim.fadeout);

            // Apply activity transition
        } else {
            // Swap without transition
        }
        ButterKnife.bind(this);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Intent i = getIntent();
        String pollID = i.getStringExtra(POLL_ID);


        if (savedInstanceState == null){
            PollFragment pollFragment = PollFragment.newInstance(pollID);
            FragmentManager fm = getSupportFragmentManager();
            fm.beginTransaction()
                    .add(R.id.poll_fragment, pollFragment)
                    .commit();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }
}
