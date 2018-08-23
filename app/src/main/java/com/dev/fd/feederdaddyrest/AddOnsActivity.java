package com.dev.fd.feederdaddyrest;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import com.dev.fd.feederdaddyrest.ViewHolder.AddOnListViewHolder;
import com.dev.fd.feederdaddyrest.ViewHolder.AddOnViewHolder;
import com.dev.fd.feederdaddyrest.model.AddOnsModel;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

import info.hoang8f.widget.FButton;

public class AddOnsActivity extends AppCompatActivity {

    ImageView imggoback;
    EditText edtaddonname, edtaddonprice;
    FButton btnaddaddon;
    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;
    List<AddOnsModel> addonslist = new ArrayList<>();
    FirebaseRecyclerAdapter<AddOnsModel,AddOnListViewHolder> addonadapter;

    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_ons);

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

        edtaddonname = findViewById(R.id.edtaddonname);
        edtaddonprice = findViewById(R.id.edtaddonprice);
        btnaddaddon = findViewById(R.id.btnaddaddon);
        recyclerView = findViewById(R.id.recycler_addonlist);

        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        SharedPreferences sharedPreferences = getSharedPreferences("MyData", Context.MODE_PRIVATE);
        String city = sharedPreferences.getString("city","N/A");
        String restaurantid = sharedPreferences.getString("restaurantid","N/A");

        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference  = firebaseDatabase.getReference(city).child("AddOns").child(restaurantid);

        addonadapter = new FirebaseRecyclerAdapter<AddOnsModel, AddOnListViewHolder>(AddOnsModel.class, R.layout.addondeleteitem_layout, AddOnListViewHolder.class,
                databaseReference
        ) {
            @Override
            protected void onDataChanged() {
                super.onDataChanged();
                recyclerView.setAdapter(addonadapter);
            }

            @Override
            protected void populateViewHolder(final AddOnListViewHolder viewHolder, final AddOnsModel model, final int position) {

                viewHolder.txtaddonitemname.setText(model.getAddonitemname());
                viewHolder.txtaddonitemprice.setText(model.getAddonitemprice());

                viewHolder.imgdelete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        databaseReference.child(addonadapter.getRef(position).getKey()).setValue(null);
                    }
                });
            }
        };

        btnaddaddon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AddOnsModel addOnsModel = new AddOnsModel(edtaddonname.getText().toString(),edtaddonprice.getText().toString());
                databaseReference.push().setValue(addOnsModel);
            }
        });









            }

}
