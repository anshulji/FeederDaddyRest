package com.dev.fd.feederdaddyrest;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;

import com.dev.fd.feederdaddyrest.ViewHolder.AddOnListViewHolder;
import com.dev.fd.feederdaddyrest.ViewHolder.DeleteBannerViewHolder;
import com.dev.fd.feederdaddyrest.model.AddOnsModel;
import com.dev.fd.feederdaddyrest.model.RestaurantBanner;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

public class DeleteBannerActivity extends AppCompatActivity {

    ImageView imggoback;

    RecyclerView recycler_deletebanner;
    RecyclerView.LayoutManager layoutManager;


    FirebaseRecyclerAdapter<RestaurantBanner,DeleteBannerViewHolder> banneradapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delete_banner);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }


        imggoback = findViewById(R.id.imggoback);
        imggoback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        recycler_deletebanner = findViewById(R.id.recycler_deletebanner);
        layoutManager = new LinearLayoutManager(this);
        recycler_deletebanner.setLayoutManager(layoutManager);

        SharedPreferences sharedPreferences  =getSharedPreferences("MyData", Context.MODE_PRIVATE);
        String city = sharedPreferences.getString("city","N/A");
        String restid = sharedPreferences.getString("restaurantid","N/A");

        final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference(city).child("RestaurantBanner").child(restid);

        banneradapter = new FirebaseRecyclerAdapter<RestaurantBanner, DeleteBannerViewHolder>(RestaurantBanner.class, R.layout.deletebanneritem_layout, DeleteBannerViewHolder.class,
                databaseReference
        ) {
            @Override
            protected void onDataChanged() {
                super.onDataChanged();
                recycler_deletebanner.setAdapter(banneradapter);
            }

            @Override
            protected void populateViewHolder(final DeleteBannerViewHolder viewHolder, final RestaurantBanner model, final int position) {

                viewHolder.txtbannername.setText(model.getName());
                Picasso.with(getBaseContext()).load(model.getImage()).fit().centerCrop().into(viewHolder.imgbanner);

                viewHolder.imgdeletebanner.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        databaseReference.child(banneradapter.getRef(position).getKey()).setValue(null);
                    }
                });
            }
        };



    }
}
