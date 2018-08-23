package com.dev.fd.feederdaddyrest;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.dev.fd.feederdaddyrest.ViewHolder.BillAdapter;
import com.dev.fd.feederdaddyrest.model.Order;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class ViewBillActivity extends AppCompatActivity {


    ImageView imggoback;
    TextView txtuseraddress,txttotal;

    RecyclerView rvbill;

    String requestnumber;

    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;

    List<Order> orderlist  = new ArrayList<>();
    BillAdapter adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_bill);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }

        if(getIntent()!=null)
        {
            requestnumber = getIntent().getStringExtra("viewbill");
        }

        SharedPreferences sharedPreferences = getSharedPreferences("MyData", Context.MODE_PRIVATE);
        String city = sharedPreferences.getString("city","N/A");

        //init firebase
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference("CurrentRequests").child(requestnumber);

        rvbill = findViewById(R.id.recycler_bill);
        rvbill.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager= new LinearLayoutManager(this);
        rvbill.setLayoutManager(layoutManager);

        imggoback = findViewById(R.id.imggoback);
        txtuseraddress = findViewById(R.id.txtaddress);
        txttotal =findViewById(R.id.txttotalamount);

        imggoback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                txtuseraddress.setText(dataSnapshot.child("customeraddress").getValue().toString());

                int totalamount  = Integer.parseInt(dataSnapshot.child("totalamount").getValue().toString())-Integer.parseInt(dataSnapshot.child("deliverycharge").getValue().toString());

                txttotal.setText("₹"+totalamount);

                for(DataSnapshot postSnapshot : dataSnapshot.child("foods").getChildren())
                {
                    orderlist.add(postSnapshot.getValue(Order.class));
                }

                adapter = new BillAdapter(orderlist,ViewBillActivity.this);
                rvbill.setAdapter(adapter);

            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

    }
}
