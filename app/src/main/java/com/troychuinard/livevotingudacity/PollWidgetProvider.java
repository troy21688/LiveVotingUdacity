package com.troychuinard.livevotingudacity;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.net.Uri;
import android.util.Log;
import android.widget.RemoteViews;

import static android.content.Context.MODE_PRIVATE;

/**
 * Implementation of App Widget functionality.
 */
public class PollWidgetProvider extends AppWidgetProvider {

    private static final String POLL_QUESTION = "POLL_QUESTION";
    private static final String POLL_IMAGE_URL = "POLL_IMAGE_URL";

    private static String mPollQuestion;
    private static String mPollImage;


    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {

        SharedPreferences mPrefs = context.getSharedPreferences("MyPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = mPrefs.edit();
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.poll_widget_provider);
        Resources res = context.getResources();


        mPollQuestion = mPrefs.getString(POLL_QUESTION, null);
        mPollImage = mPrefs.getString(POLL_IMAGE_URL, null);
        Log.v(mPollQuestion, mPollQuestion);
        Log.v(mPollImage, mPollImage);

        if (mPollQuestion == null){
            views.setTextViewText(R.id.appwidget_text, res.getString(R.string.please_open_application));
        } else {
            views.setTextViewText(R.id.appwidget_text, mPollQuestion);
        }

        if (mPollImage == null){
            views.setImageViewResource(R.id.appwidget_image, R.drawable.fan_polls_logo);

        } else {
            Uri imageURI = Uri.parse(mPollImage);
            views.setImageViewUri(R.id.appwidget_image, imageURI);
        }


        CharSequence widgetText = context.getString(R.string.appwidget_text);


        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }


}

