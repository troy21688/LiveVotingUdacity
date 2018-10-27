package com.troychuinard.livevotingudacity.Fragment;

import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.squareup.picasso.Picasso;
import com.troychuinard.livevotingudacity.Model.Poll;
import com.troychuinard.livevotingudacity.MyService;
import com.troychuinard.livevotingudacity.PollHostActivity;
import com.troychuinard.livevotingudacity.PollWidgetProvider;
import com.troychuinard.livevotingudacity.R;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link FeedFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link FeedFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FeedFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";


    private static final String POLL_QUESTION = "POLL_QUESTION";
    private static final String POLL_IMAGE_URL = "POLL_IMAGE_URL";
    private static final String FILTER_ACTION = "UPDATE_WIDGET";
    private static final String POLL_ID = "POLL_ID";
    private static final String RECYCLERVIEW_STATE = "Recyclerview_State";

    private Parcelable mRecyclerViewState;
    private static Bundle mRecyclerViewBundle;

    private MyReceiver myReceiver;


    @BindView(R.id.list)
    RecyclerView mRecyclerview;

    private SharedPreferences mPrefs;
    private SharedPreferences.Editor editor;

    private DatabaseReference mBaseRef;
    private DatabaseReference mPollsRef;

    private LinearLayoutManager mLayoutManager;

    private FirebaseRecyclerAdapter<Poll, PollHolder> mFireAdapter;


    private OnFragmentInteractionListener mListener;

    public FeedFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment FeedFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static FeedFragment newInstance(String param1, String param2) {
        FeedFragment fragment = new FeedFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_poll_feed, container, false);
        ButterKnife.bind(this, v);


        mPrefs = getActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        editor = mPrefs.edit();
        if (savedInstanceState != null) {
            mRecyclerViewState = savedInstanceState.getParcelable(RECYCLERVIEW_STATE);
        }


        mRecyclerview.getItemAnimator().setChangeDuration(0);
        mLayoutManager = new LinearLayoutManager(getContext());
        mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mLayoutManager.setReverseLayout(true);
        mLayoutManager.setStackFromEnd(true);
        mRecyclerview.setLayoutManager(mLayoutManager);

        setReceiver();

        return v;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

//    @Override
//    public void onSaveInstanceState(@NonNull Bundle outState) {
//        super.onSaveInstanceState(outState);
//        mRecyclerViewState = mLayoutManager.onSaveInstanceState();
//        outState.putParcelable(RECYCLERVIEW_STATE, mRecyclerViewState);
//    }

//
//
//    @Override
//    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
//        super.onViewStateRestored(savedInstanceState);
//        if (savedInstanceState != null){
//            mRecyclerViewState = savedInstanceState.getParcelable(RECYCLERVIEW_STATE);
//        }
//    }


    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        mRecyclerViewState = mLayoutManager.onSaveInstanceState();
        outState.putParcelable(RECYCLERVIEW_STATE, mRecyclerViewState);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null){
            mRecyclerViewState = savedInstanceState.getParcelable(RECYCLERVIEW_STATE);
        }
    }


    @Override
    public void onResume() {
        super.onResume();
        if (mRecyclerViewState != null){
            mLayoutManager.onRestoreInstanceState(mRecyclerViewState);
        }
    }



    @Override
    public void onStart() {
        super.onStart();

        Query query = FirebaseDatabase.getInstance()
                .getReference()
                .child("Polls");

        FirebaseRecyclerOptions<Poll> options = new FirebaseRecyclerOptions.Builder<Poll>()
                .setQuery(query, Poll.class)
                .build();


        mFireAdapter = new FirebaseRecyclerAdapter<Poll, PollHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final PollHolder holder, int position, @NonNull Poll model) {

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

                //TODO: Cannot understand how to utilize IntentService to update widget
                //TODO: Followed this tutorial: https://www.journaldev.com/20735/android-intentservice-broadcastreceiver
                Intent intent = new Intent(getContext(), MyService.class);
                intent.putExtra(POLL_QUESTION, model.getQuestion());
                intent.putExtra(POLL_IMAGE_URL, model.getImage_URL());

                getActivity().startService(intent);

                holder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        String pollID = mFireAdapter.getRef(holder.getAdapterPosition()).getKey();

                        if (getActivity().findViewById(R.id.two_pane_constraint_layout) != null & getActivity().getApplication().getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                            FragmentManager fm = getActivity().getSupportFragmentManager();
                            PollFragment pollFragment = PollFragment.newInstance(pollID);
                            fm.beginTransaction()
                                    .replace(R.id.poll_fragment, pollFragment)
                                    .commit();
                        } else {

                            Intent i = new Intent(getActivity().getApplicationContext(), PollHostActivity.class);
                            i.putExtra(POLL_ID, pollID);
                            startActivity(i);

                        }
                    }
                });

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

    private void setReceiver() {
        myReceiver = new MyReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(FILTER_ACTION);
//        LocalBroadcastManager.getInstance(this).registerReceiver(myReceiver, intentFilter);
    }

    private void scrollToPosition() {
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


    private class MyReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String poll_Question = intent.getStringExtra("broadcastMessage");

            //TODO: Update Widget - used answer from https://stackoverflow.com/questions/3455123/programmatically-update-widget-from-activity-service-receiver
            int[] ids = AppWidgetManager.getInstance(getContext()).getAppWidgetIds(new ComponentName(getContext(), PollWidgetProvider.class));
            PollWidgetProvider pollWidgetProvider = new PollWidgetProvider();
            pollWidgetProvider.onUpdate(getContext(), AppWidgetManager.getInstance(getContext()), ids);
        }
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
