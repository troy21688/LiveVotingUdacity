package com.troychuinard.livevotingudacity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.internal.view.SupportMenuItem;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatRadioButton;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.support.v7.widget.SearchView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.squareup.picasso.Picasso;
import com.troychuinard.livevotingudacity.Model.Flicker;
import com.troychuinard.livevotingudacity.Model.GridSpacingItemDecoration;
import com.troychuinard.livevotingudacity.Model.Photo;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Query;

public class NewImageActivity extends AppCompatActivity {

    @Nullable
    @BindView(R.id.flickr_image_results_view)
    RecyclerView mImageResults;
    @Nullable
    @BindView(R.id.progressBar)
    ProgressBar mProgressBar;

    private RecyclerView.Adapter mImageResultsAdapter;
    private ArrayList<Photo> mPhotoArray;
    private ArrayList<String> mPhotoURLS;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_image);
        ButterKnife.bind(this);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        }

        GridLayoutManager glm = new GridLayoutManager(this, 2);
        glm.setOrientation(LinearLayoutManager.VERTICAL);
        mImageResults.setLayoutManager(glm);
        mImageResults.setItemAnimator(new DefaultItemAnimator());
        mImageResultsAdapter = new MyAdapter(mPhotoURLS);
        mImageResults.setAdapter(mImageResultsAdapter);
        int spanCount = 2; // 3 columns
        int spacing = 25; // 50px
        boolean includeEdge = false;
        mImageResults.addItemDecoration(new GridSpacingItemDecoration(spanCount, spacing, includeEdge));
        mPhotoURLS = new ArrayList<>();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_search, menu);
        // Retrieve the SearchView and plug it into SearchManager

        SupportMenuItem searchItem = (SupportMenuItem) menu.findItem(R.id.search);

        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                mProgressBar.setVisibility(View.VISIBLE);
                View view = getCurrentFocus();
                if (view != null) {
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }
                //the urls to populate the Recyclerview; URLs dumped into Picasso
                mPhotoURLS.clear();
                mImageResultsAdapter.notifyDataSetChanged();
                Toast.makeText(getApplicationContext(), query, Toast.LENGTH_LONG).show();
                HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
                interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
                OkHttpClient client = new OkHttpClient.Builder().addInterceptor(interceptor).build();
                Retrofit retrofit = new Retrofit.Builder()
                        .baseUrl("https://api.flickr.com/services/rest/")
                        .client(client)
                        .addConverterFactory(GsonConverterFactory.create())
                        .build();


                ApiInterface apiInterface = retrofit.create(ApiInterface.class);
                Call<Flicker> call = apiInterface.getImages(query);
                call.enqueue(new Callback<Flicker>() {
                    @Override
                    public void onResponse(Call<Flicker> call, Response<Flicker> response) {
                        Log.v("RESPONSE_CALLED", "ON_RESPONSE_CALLED");
                        String didItWork = String.valueOf(response.isSuccessful());
                        Log.v("SUCCESS?", didItWork);
                        Log.v("RESPONSE_CODE", String.valueOf(response.code()));
                        Flicker testResponse = response.body();
                        Log.v("RESPONSE_BODY", "response:" + testResponse);
                        String total = response.body().getPhotos().getTotal().toString();
                        Log.v("Total", total);
                        List<Photo> photoResults = response.body().getPhotos().getPhoto();
                        int numberOfPages = response.body().getPhotos().getPages();
                        for (int i = 0; i < numberOfPages; i++) {
                            for (Photo photo : photoResults) {
                                if (photo.getUrl_m() != null) {
                                    String photoURL = photo.getUrl_m();
//                                    Log.v("PHOTO_URL:", photoURL);
                                    mPhotoURLS.add(photoURL);
                                    mImageResultsAdapter.notifyDataSetChanged();
                                }
                            }


                        }

                    }

                    @Override
                    public void onFailure(Call<Flicker> call, Throwable t) {

                    }
                });
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        return true;
    }


    public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {


        // Provide a reference to the views for each data item
        // Complex data items may need more than one view per item, and
        // you provide access to all the views for a data item in a view holder
        public class ViewHolder extends RecyclerView.ViewHolder {
            // each data item is just a string in this case
            protected ImageView mResultImage;


            public ViewHolder(View v) {
                super(v);
                mResultImage = (ImageView) v.findViewById(R.id.flickr_individual_image);


            }
        }

        // Provide a suitable constructor (depends on the kind of dataset)
        public MyAdapter(ArrayList<String> mDataSet) {
            mPhotoURLS = mDataSet;
        }


        // Create new views (invoked by the layout manager)
        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent,
                                             int viewType) {
            // create a new view
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.grid_item, parent, false);
            // set the view's size, margins, paddings and layout parameters
            return new ViewHolder(v);
        }

        // Replace the contents of a view (invoked by the layout manager)

        //The OutOfBoundsException is pointing here
        @Override
        public void onBindViewHolder(ViewHolder holder, final int position) {
            Log.v("ON_BIND", "ON_BINDVIEWHOLDER CALLED");
            final String urlForPhoto = mPhotoURLS.get(position);
            if (mProgressBar.getVisibility() == View.VISIBLE) {
                mProgressBar.setVisibility(View.INVISIBLE);
            }
            Picasso.get()
                    .load(urlForPhoto)
                    .placeholder(R.drawable.progress_animation)
                    .into(holder.mResultImage);

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Toast.makeText(getApplicationContext(), mPhotoURLS.get(position), Toast.LENGTH_LONG).show();
                    AlertDialog.Builder builder = new AlertDialog.Builder(NewImageActivity.this, R.style.AlertDialogCustom);
                    builder.setTitle(getResources().getString(R.string.app_label));
                    builder.setMessage(getResources().getString(R.string.add_photo_question));
                    builder.setPositiveButton((getResources().getString(R.string.yes)), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            Intent returnIntent = new Intent();
                            returnIntent.putExtra("result", urlForPhoto);
                            setResult(NewImageActivity.RESULT_OK,returnIntent);
                            finish();
                        }
                    });
                    builder.setNegativeButton((getResources().getString(R.string.no)), null);
                    builder.show();
                }
            });

        }

        // Return the size of your dataset (invoked by the layout manager)
        @Override
        public int getItemCount() {
            return mPhotoURLS.size();
        }
    }

    public void clearData() {
        int size = this.mPhotoURLS.size();
        if (size > 0) {
            for (int i = 0; i < size; i++) {
                this.mPhotoURLS.remove(0);
            }

            mImageResultsAdapter.notifyItemRangeRemoved(0, size);
        }
    }

    public interface ApiInterface {

        @GET("?method=flickr.photos.search&api_key=1c448390199c03a6f2d436c40defd90e&license=4&format=json&nojsoncallback=1&extras=url_m")
        Call<Flicker> getImages(@Query("text") String query);


    }

}