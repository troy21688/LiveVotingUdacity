package com.troychuinard.livevotingudacity;

import android.app.IntentService;
import android.content.Intent;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;

public class MyService extends IntentService {

    private static final String POLL_QUESTION = "POLL_QUESTION";
    private static final String POLL_IMAGE_URL = "POLL_IMAGE_URL";

    public MyService() {
        super("MyService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        String pollQuestion = intent.getStringExtra(POLL_QUESTION);
        String pollURL = intent.getStringExtra(POLL_IMAGE_URL);
        SystemClock.sleep(3000);

        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent.putExtra("broadcastMessage",pollQuestion));

    }
}
