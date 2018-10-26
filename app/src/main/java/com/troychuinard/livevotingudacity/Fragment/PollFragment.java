package com.troychuinard.livevotingudacity.Fragment;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.CheckBox;
import android.widget.ImageView;
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
import com.google.firebase.database.Query;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.troychuinard.livevotingudacity.HomeActivity;
import com.troychuinard.livevotingudacity.Model.MyDataValueFormatter;
import com.troychuinard.livevotingudacity.R;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link PollFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link PollFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PollFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final String VOTERS = "Voters";
    private static final String ID = "ID";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    @BindView(R.id.comment_label_counter)
    TextView mCommentCounter;
    @BindView(R.id.comments_label_icon)
    ImageView mCommentsLabelIcon;
    @BindView(R.id.poll_image_fragment)
    ImageView mPollImage;

    private FirebaseAuth mAuth;

    private DatabaseReference mBaseRef;
    private DatabaseReference mPollsRef;
    private DatabaseReference mSelectedPollRef;
    private DatabaseReference mUsersRef;

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
    private String pollID;


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
    private int mPollIndex;
    private ProgressBar mProgressBar;
    private CheckBox mFollowingCheck;
    private Context mContext;
    private boolean hasVoted;


    private OnFragmentInteractionListener mListener;

    public PollFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment PollFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static PollFragment newInstance(String pollID) {
        PollFragment fragment = new PollFragment();
        Bundle args = new Bundle();
        args.putString("POLL_ID", pollID);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        pollID = args.getString("POLL_ID");
        mAuth = FirebaseAuth.getInstance();
        mUserID = mAuth.getCurrentUser().getUid();
        mBaseRef = FirebaseDatabase.getInstance().getReference();
        mPollsRef = mBaseRef.child(POLL_LABEL);
        mSelectedPollRef = mPollsRef.child(pollID);
        mUsersRef = mBaseRef.child(USERS_LABEL);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        final View v = inflater.inflate(R.layout.fragment_poll, container, false);
        ButterKnife.bind(v);





        mTotalVoteCounter = (TextView) v.findViewById(R.id.total_vote_counter);
        mCommentCounter = (TextView) v.findViewById(R.id.comment_label_counter);
        mDisplayName = (TextView) v.findViewById(R.id.creator_id);

        mProgressBar = (ProgressBar) v.findViewById(R.id.progress_bar_white);

        mPollQuestion = (TextView) v.findViewById(R.id.poll_question);
        mPollQuestion.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.poll_question_text_size));
        mPollQuestionRadioGroup = (RadioGroup) v.findViewById(R.id.poll_question_group);
        mFollowingCheck = (CheckBox) v.findViewById(R.id.following_check);

        mParams = new RadioGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        mPollResults = (HorizontalBarChart) v.findViewById(R.id.poll_results_chart);
        mPollResults.setBackgroundColor(getResources().getColor(R.color.white));
        mPollResults.setNoDataTextDescription(getResources().getString(R.string.no_results_description));

        Query q = mSelectedPollRef.child(VOTERS);
        q.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot x: dataSnapshot.getChildren()){
                    if (x.getValue().equals(mUserID)){
                        Log.v("TEST", x.getValue().toString());
                        mPollResults.setVisibility(View.VISIBLE);
                        mPollQuestionRadioGroup.setVisibility(View.INVISIBLE);

                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });



        mSelectedPollRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                Log.v("KEY", dataSnapshot.getKey());

                Log.v("POLL_ID", "The poll ID is " + pollID);

                Log.v("TAG", dataSnapshot.toString());

                for (DataSnapshot d : dataSnapshot.getChildren()) {
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
                        .into((ImageView) v.findViewById(R.id.poll_image_fragment));

                //Load vote count
                Long totalVoteCount = (Long) dataSnapshot.child(VOTE_COUNT_LABEL).getValue();
                mTotalVoteCounter.setText(getResources().getString(R.string.total_vote_counter) + " " + totalVoteCount);
                mTotalVoteCounter.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.vote_count_label_text_size_lower_right));
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
                Toast.makeText(getContext(), R.string.vote_submitted_label, Toast.LENGTH_LONG).show();

                int checkedRadioButtonID = group.getCheckedRadioButtonId();
                Log.e("checkedIDCHECK", "the checkedID is " + checkedRadioButtonID);
                String selectedRadioButtonPollAnswer = mPollAnswerArrayList.get(checkedRadioButtonID).getText().toString();
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                String UID = user.getUid();
                //TODO: Add user reference in Firebase and update
//                mUserRef.child(UID).child("Poll_Votes").child(pollID).child("Vote_Selection").setValue(selectedRadioButtonPollAnswer);
//                mUserRef.child(UID).child("Poll_Votes").child(pollID).child("Has_Voted").setValue("True");
                HashMap<String, Object> userID = new HashMap<>();
                userID.put(ID, UID);
                mSelectedPollRef.child(VOTERS).updateChildren(userID);

                //Update Firebase poll total vote count
                increasePollTotalVoteCounter(checkedRadioButtonID);
                //Update Firebase poll individual answer vote count
                increasePollAnswerVoteCounter(checkedRadioButtonID);

                //TODO: add animation between between vote click and chart display
                mPollQuestionRadioGroup.setVisibility(View.INVISIBLE);
                mPollResults.setVisibility(View.VISIBLE);
            }

        });
        // Inflate the layout for this fragment
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
    public void onStart() {
        super.onStart();
        valueEventListener = new ValueEventListener() {
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
                mTotalVoteCounter.setText(getResources().getString(R.string.votes) + String.valueOf(totalFormattedVotes));
//                mRefAtPollIndexImmediatelyBelowDate.child(VOTE_COUNT_LABEL).setValue(voteCountTotal);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        mSelectedPollRef.addValueEventListener(valueEventListener);

    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
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
//        barColors.add(R.color.bar_one);
//        barColors.add(R.color.bar_two);
//        barColors.add(R.color.bar_three);
//        barColors.add(R.color.bar_four);
//        barColors.add(R.color.bar_five);

        barColors.add(ContextCompat.getColor(getActivity().getApplicationContext(),R.color.bar_one));
        barColors.add(ContextCompat.getColor(getActivity().getApplicationContext(), R.color.bar_two));
        barColors.add(ContextCompat.getColor(getActivity().getApplicationContext(), R.color.bar_three));
        barColors.add(ContextCompat.getColor(getActivity().getApplicationContext(), R.color.bar_four));
        barColors.add(ContextCompat.getColor(getActivity().getApplicationContext(), R.color.bar_five));
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

    private void addRadioButtonsWithFirebaseAnswers(int numberOfAnswers, DataSnapshot dataSnapshot) {
        mPollAnswerArrayList = new ArrayList<RadioButton>();
        for (int i = (numberOfAnswers - 1); i >= 0; i--) {
            int indexCreated = ((numberOfAnswers - 1) - i);
            mPollAnswerArrayList.add((indexCreated), new RadioButton((getContext())));
            mPollAnswerArrayList.get(indexCreated).setId(indexCreated);
            String firebaseChild = String.valueOf(indexCreated + 1);
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

    private void increasePollTotalVoteCounter(int checkedRadioButtonID) {
        mSelectedPollRef.child("vote_count").runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                mutableData.setValue((Long) mutableData.getValue() + 1);
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {
                if (databaseError != null) {
                    Toast.makeText(getContext(), databaseError.getMessage(), Toast.LENGTH_LONG).show();
                }

            }
        });
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
                    Toast.makeText(getContext(), firebaseError.getMessage(), Toast.LENGTH_LONG).show();

                }
            }

        });
    }

    private void disableVoteButtons(RadioGroup group) {
        //disable voting
        for (int i = 0; i < group.getChildCount(); i++) {
            group.getChildAt(i).setEnabled(false);
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
