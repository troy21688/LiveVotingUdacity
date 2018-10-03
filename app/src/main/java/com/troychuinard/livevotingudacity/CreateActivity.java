package com.troychuinard.livevotingudacity;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.troychuinard.livevotingudacity.Model.Poll;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.google.gson.internal.bind.TypeAdapters.UUID;

public class CreateActivity extends AppCompatActivity {

    private DatabaseReference mBaseRef;
    private DatabaseReference mUserRef;
    private DatabaseReference mPollsRef;

    private FirebaseStorage mStorage;
    private StorageReference mStorageRef;
    private StorageReference mFileRef;

    @BindView(R.id.add_image_button)
    FloatingActionButton mAddImageButton;
    @BindView(R.id.add_answers_button)
    ImageView mAddAnswersButton;
    @BindView(R.id.preview_image)
    ImageView mImagePreview;
    @BindView(R.id.create_poll_question_editText)
    EditText mCreatePollQuestion;
    @BindView(R.id.create_poll_answer_counter_TextView)
    TextView mCreatePollAnswerCounter;
    @BindView(R.id.create_poll_questions_answer_layout)
    ViewGroup mEditTextAnswerLayout;
    @BindView(R.id.submit_poll_FAB)
    FloatingActionButton mSubmitPollCreation;

    private int mNumberOfPollAnswersCreatedByUser;

    private String resultImageURL;
    private ArrayList<String> mPollAnswers;

    private static final int USE_WEB = 1;
    private static final int TAKE_PICTURE = 2;
    private static final int USE_GALLERY = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create);
        ButterKnife.bind(this);
        mStorage = FirebaseStorage.getInstance();
        mStorageRef = mStorage.getReferenceFromUrl("gs://fan-polls-udacity.appspot.com");


        mBaseRef = FirebaseDatabase.getInstance().getReference();
        mPollsRef = mBaseRef.child("Polls");
        mUserRef = mBaseRef.child("Users");

        mNumberOfPollAnswersCreatedByUser = 2;
        mCreatePollAnswerCounter.setText(String.valueOf(mNumberOfPollAnswersCreatedByUser));
        mPollAnswers = new ArrayList<>();
        for (int i = 0; i < mNumberOfPollAnswersCreatedByUser; i++) {
            createAnswerChoice(i + 1);
        }
        mAddAnswersButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mNumberOfPollAnswersCreatedByUser++;
                if (mNumberOfPollAnswersCreatedByUser > 5) {
                    return;
                }
                createAnswerChoice(mNumberOfPollAnswersCreatedByUser);
                mCreatePollAnswerCounter.setText(String.valueOf(mNumberOfPollAnswersCreatedByUser));
            }
        });

        mSubmitPollCreation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //TODO: Need to check if poll requirements are added, i.e. Question, Answer, ......
                //check if image has been loaded first
                if (resultImageURL == null) {
                    Toast.makeText(getApplicationContext(), getResources().getString(R.id.please_add_image),Toast.LENGTH_LONG).show();
                    return;
                }

                //capture answers
                if (mNumberOfPollAnswersCreatedByUser > 5) {
                    mNumberOfPollAnswersCreatedByUser = 5;
                }
                for (int i = 0; i < mNumberOfPollAnswersCreatedByUser; i++) {
                    EditText editText = (EditText) mEditTextAnswerLayout.findViewWithTag(getResources().getString(R.string.created_answer_editText_id) + String.valueOf(i + 1));
                    String editTextInputForAnswer = String.valueOf(editText.getText());
                    mPollAnswers.add(0, editTextInputForAnswer);
                }

                FirebaseAuth auth = FirebaseAuth.getInstance();
                String displayName = auth.getCurrentUser().getDisplayName();
                String ID = auth.getUid();

                Poll poll = new Poll(mCreatePollQuestion.getText().toString(), resultImageURL, mPollAnswers, 0,ID, displayName);
                Map<String, Object> pollMap = poll.toMap();
                final String key = mBaseRef.child("Polls").push().getKey();
                Map<String, Object> childUpdates = new HashMap<String, Object>();
                childUpdates.put("/Polls/" + key, pollMap);

                mBaseRef.updateChildren(childUpdates);
                Collections.reverse(mPollAnswers);
                for (int i = 0; i < mPollAnswers.size(); i++) {
                    mBaseRef.child("Polls").child(key).child("answers").child(String.valueOf(i + 1)).updateChildren(poll.answerConvert(mPollAnswers, i));
                }

                String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
                Log.v("USER_ID", userID);
                mUserRef.child(userID).updateChildren(childUpdates);


                Intent toHomeActivity = new Intent(CreateActivity.this, HomeActivity.class);
                toHomeActivity.putExtra("viewpager_position", 2);
                startActivity(toHomeActivity);
            }
        });

        mAddImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createAlertDialog();
            }
        });


    }

    //programatically create editText based
    private void createAnswerChoice(int answerNumber) {
        EditText editText = new EditText(getApplicationContext());
        editText.setHint(getResources().getString(R.string.answer_text) + " " + answerNumber);
        editText.setSingleLine(true);
        editText.setImeOptions(EditorInfo.IME_ACTION_DONE);
        editText.setTextColor(getResources().getColor(R.color.black));
        String editTextID = ((getResources().getString(R.string.created_answer_editText_id)) + String.valueOf(answerNumber));
        editText.setTag(editTextID);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        editText.setLayoutParams(layoutParams);
        TextInputLayout newAnswer = new TextInputLayout(CreateActivity.this);
        newAnswer.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        newAnswer.addView(editText, layoutParams);
        mEditTextAnswerLayout.addView(newAnswer);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mAddImageButton.setVisibility(View.INVISIBLE);
        switch (requestCode) {
            case USE_WEB:
                if (resultCode == CreateActivity.RESULT_OK) {
                    resultImageURL = data.getStringExtra("result");
                    Picasso.get()
                            .load(resultImageURL)
                            .into(mImagePreview);
                }
                if (resultCode == CreateActivity.RESULT_CANCELED) {
                    //Write your code if there's no result
                }
                break;
            case TAKE_PICTURE:

                if (resultCode == CreateActivity.RESULT_OK) {

                    Bundle extras = data.getExtras();
                    Bitmap imageBitmap = (Bitmap) extras.get("data");
                    mImagePreview.setDrawingCacheEnabled(true);
                    mImagePreview.buildDrawingCache();
                    mImagePreview.setImageBitmap(imageBitmap);
                    encodeBitmapAndSaveToFirebase(imageBitmap);

                }
                break;
            case USE_GALLERY:
                if (resultCode == CreateActivity.RESULT_OK) {
                    Uri selectedImage = data.getData();
                    //yourSelectedImage = BitmapFactory.decodeStream(imageStream);
                    try {
                        Bitmap yourSelectedImage = decodeUri(selectedImage);
                        encodeBitmapAndSaveToFirebase(yourSelectedImage);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }

                    // Do something with the bitmap


                    // At the end remember to close the cursor or you will end with the RuntimeException!
                }
        }
    }


    private Bitmap decodeUri(Uri selectedImage) throws FileNotFoundException {

        // Decode image size
        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(getContentResolver().openInputStream(selectedImage), null, o);

        // The new size we want to scale to
        final int REQUIRED_SIZE = 250;

        // Find the correct scale value. It should be the power of 2.
        int width_tmp = o.outWidth, height_tmp = o.outHeight;
        int scale = 1;
        while (true) {
            if (width_tmp / 2 < REQUIRED_SIZE
                    || height_tmp / 2 < REQUIRED_SIZE) {
                break;
            }
            width_tmp /= 2;
            height_tmp /= 2;
            scale *= 2;
        }

        // Decode with inSampleSize
        BitmapFactory.Options o2 = new BitmapFactory.Options();
        o2.inSampleSize = scale;
        return BitmapFactory.decodeStream(getContentResolver().openInputStream(selectedImage), null, o2);

    }


    private void createAlertDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.add_image_dialog_title);
        builder.setItems(new CharSequence[]
                        {getResources().getString(R.string.add_image_web), getResources().getString(R.string.add_image_camera), getResources().getString(R.string.add_image_gallery)},
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // The 'which' argument contains the index position
                        // of the selected item
                        switch (which) {
                            case 0:
                                Intent toImageSearch = new Intent(CreateActivity.this, NewImageActivity.class);
                                startActivityForResult(toImageSearch, USE_WEB);
                                break;
                            case 1:
                                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                                if (takePictureIntent.resolveActivity(getApplication().getPackageManager()) != null) {
                                    startActivityForResult(takePictureIntent, TAKE_PICTURE);
                                } else {
                                }
                                break;
                            case 2:
                                Intent i = new Intent();
                                i.setType("image/*");
                                i.setAction(Intent.ACTION_GET_CONTENT);
                                startActivityForResult(Intent.createChooser(i, "Select Picture"), USE_GALLERY);
                                break;
                        }

                    }
                }

        );
        builder.create().

                show();
    }

    private void encodeBitmapAndSaveToFirebase(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();

        String uniqueID = java.util.UUID.randomUUID().toString();
        mFileRef = mStorageRef.child(uniqueID);
        UploadTask uploadTask = mFileRef.putBytes(data);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
                Uri downloadUrl = taskSnapshot.getUploadSessionUri();
                resultImageURL = downloadUrl.toString();
                Picasso.get()
                        .load(resultImageURL)
                        .into(mImagePreview);
            }
        });

    }
}