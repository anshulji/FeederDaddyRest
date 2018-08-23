package com.dev.fd.feederdaddyrest;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.dev.fd.feederdaddyrest.Common.Common;
import com.dev.fd.feederdaddyrest.Remote.APIService;
import com.dev.fd.feederdaddyrest.Service.MyAlarm;
import com.dev.fd.feederdaddyrest.ViewHolder.OrderViewHolder;
import com.dev.fd.feederdaddyrest.model.MyResponse;
import com.dev.fd.feederdaddyrest.model.Notification;
import com.dev.fd.feederdaddyrest.model.Order;
import com.dev.fd.feederdaddyrest.model.Request;
import com.dev.fd.feederdaddyrest.model.Sender;
import com.dev.fd.feederdaddyrest.model.Token;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.Calendar;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class Orders extends AppCompatActivity {

    public RecyclerView recyclerView;
    public  RecyclerView.LayoutManager layoutManager;
    private ImageView imggoback;
    private TextView tbtxt,txttotalamount;
    private SwitchCompat switchbtn;

    private static final int REQUEST_PHONE_CALL = 1;
    private boolean isadvord=false;

    private String phone,restaurantidstr;

    private FirebaseDatabase database;
    private DatabaseReference requests,restref;
    private FirebaseRecyclerAdapter<Request, OrderViewHolder> adapter;

    private long totalamnt=0;

    APIService mService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_orders);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }

        //init service
        mService = Common.getFCMClient();

        //firebase init
        database = FirebaseDatabase.getInstance();
        requests = database.getReference("CurrentRequests");
        restref = database.getReference(Common.getCity(this)).child("Restaurant").child(Common.getRestaurantId(this)).child("isopen");

        recyclerView = findViewById(R.id.listOrders);
        imggoback = findViewById(R.id.imggoback);
        txttotalamount = findViewById(R.id.txttotalamount);

        imggoback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        tbtxt = findViewById(R.id.tbtxt);
        switchbtn = findViewById(R.id.switchbtn);


        SharedPreferences sharedPreferences = getSharedPreferences("MyData", Context.MODE_PRIVATE);
        phone = sharedPreferences.getString("phone","N/A");
        restaurantidstr = sharedPreferences.getString("restaurantid","N/A");

        //Toast.makeText(this, ""+Common.currentuser.getPhone(), Toast.LENGTH_SHORT).show();
        if(getIntent().getStringExtra("currentorder")==null)
        {
            tbtxt.setText("Current Orders");
            txttotalamount.setVisibility(View.GONE);
            loadOrders(phone);
        }
        else if(getIntent().getStringExtra("currentorder").equals("1"))
        {//Toast.makeText(this, ""+Common.currentuser.getPhone(), Toast.LENGTH_SHORT).show();
            tbtxt.setText("Current Orders");
            txttotalamount.setVisibility(View.GONE);
            loadOrders(phone);
        }
        else if( getIntent().getStringExtra("currentorder").equals("0"))
        {   tbtxt.setText("Past Orders");
            loadPastOrders();
        }

        //set switchbtn
        restref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.getValue().toString().equals("1"))
                    switchbtn.setChecked(true);
                else switchbtn.setChecked(false);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        switchbtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked)
                {
                    restref.setValue("1");
                    SharedPreferences sharedPreferences = getSharedPreferences("MyData",Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("isopen","1");
                    editor.commit();

                }
                else {
                    SharedPreferences sharedPreferences = getSharedPreferences("MyData",Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("isopen","0");
                    editor.commit();
                    restref.setValue("0");
                }
            }
        });

    }

    private void loadPastOrders() {

        requests.orderByChild("restaurantid_po").equalTo(restaurantidstr+"YES").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot postSnapshot : dataSnapshot.getChildren())
                {
                    totalamnt+=Integer.parseInt(postSnapshot.child("totalamount").getValue().toString())-Integer.parseInt(postSnapshot.child("deliverycharge").getValue().toString());
                }
                txttotalamount.setText("Total Amount : ₹"+totalamnt);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        adapter = new FirebaseRecyclerAdapter<Request, OrderViewHolder>(
                Request.class,
                R.layout.order_item_layout,
                OrderViewHolder.class,
                requests.orderByChild("restaurantid_po").equalTo(restaurantidstr+"YES")
        ) {

            @Override
            protected void onDataChanged() {
                super.onDataChanged();
                recyclerView.setAdapter(adapter);
            }

            @Override
            protected void populateViewHolder(final OrderViewHolder viewHolder, final Request model, final int position) {

                String orderidstr = adapter.getRef(position).getKey();
                orderidstr = "#"+orderidstr.substring(1,orderidstr.length());
                viewHolder.txtorderid.setText(orderidstr);
                viewHolder.txtordertime.setText(Common.getDate(Long.parseLong(model.getTimeinms())));

                if(model.getDidrestaurantaccepted().equals("1"))
                {
                    viewHolder.btncancel.setVisibility(View.GONE);
                    viewHolder.btnaccept.setVisibility(View.VISIBLE);
                    viewHolder.btnaccept.setText("ACCEPTED");
                    viewHolder.btnaccept.setEnabled(false);
                }
                else if(model.getDidrestaurantaccepted().equals("-1"))
                {
                    viewHolder.btnaccept.setVisibility(View.GONE);
                    viewHolder.btncancel.setVisibility(View.VISIBLE);
                    viewHolder.btncancel.setText("Cancelled");
                    viewHolder.btncancel.setEnabled(false);
                }
                else
                {
                    viewHolder.btnaccept.setVisibility(View.VISIBLE);
                    viewHolder.btncancel.setVisibility(View.VISIBLE);
                    viewHolder.btncancel.setEnabled(true);
                    viewHolder.btnaccept.setEnabled(true);
                    viewHolder.btncancel.setText("Cancel");
                    viewHolder.btnaccept.setText("Accept");
                }

                viewHolder.txtdeliveryboyname.setText(model.getDeliveryboyname());

                String otp = model.getTimeinms();
                otp = otp.substring(otp.length()-8,otp.length()-4);
                viewHolder.txtotp.setText("OTP : "+otp);

                int totalamount =Integer.parseInt(model.getTotalamount())-Integer.parseInt(model.getDeliverycharge());
                viewHolder.txttotalamount.setText("Total: ₹"+totalamount);
/*                totalamnt +=totalamount;
                txttotalamount.setText("Total Amount : "+totalamnt);*/


                viewHolder.btnaccept.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        DatabaseReference orderstatusref = database.getReference("CurrentRequests")
                                .child(adapter.getRef(position).getKey());
                        orderstatusref.child("didrestaurantaccepted").setValue("1");
                        viewHolder.btncancel.setVisibility(View.GONE);
                        viewHolder.btnaccept.setText("ACCEPTED");
                        viewHolder.btnaccept.setEnabled(false);
                        //orderstatusref.child("orderstatusmessage").setValue("Your food is cooking now...");
                    }
                });

                viewHolder.btncancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        DatabaseReference orderstatusref = database.getReference("CurrentRequests")
                                .child(adapter.getRef(position).getKey());
                        orderstatusref.child("didrestaurantaccepted").setValue("-1");
                        viewHolder.btnaccept.setVisibility(View.GONE);
                        viewHolder.btncancel.setText("Cancelled");
                        viewHolder.btncancel.setEnabled(false);
                    }
                });

                viewHolder.imgcalldeliveryboy.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + model.getDeliveryboyphone()));
                        if (ContextCompat.checkSelfPermission(Orders.this, android.Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                            ActivityCompat.requestPermissions(Orders.this, new String[]{android.Manifest.permission.CALL_PHONE},REQUEST_PHONE_CALL);
                        }
                        else
                        {
                            startActivity(intent);
                        }
                    }

                });

                viewHolder.txtviewbill.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent Viewbill = new Intent(Orders.this, ViewBillActivity.class);
                        Viewbill.putExtra("viewbill",adapter.getRef(position).getKey());
                        startActivity(Viewbill);
                    }
                });
            }
        };
    }



    private void loadOrders(final String phone) {


        adapter = new FirebaseRecyclerAdapter<Request, OrderViewHolder>(
                Request.class,
                R.layout.order_item_layout,
                OrderViewHolder.class,
                requests.orderByChild("restaurantid_co").equalTo(restaurantidstr+"YES")
        ) {

            @Override
            protected void onDataChanged() {
                super.onDataChanged();
                recyclerView.setAdapter(adapter);
            }

            @Override
            protected void populateViewHolder(final OrderViewHolder viewHolder, final Request model, final int position) {

                if(!model.getOrderreceivetime().equals("ASAP")) {
                    viewHolder.rlort.setVisibility(View.VISIBLE);
                    viewHolder.txtordereceivetime.setText("Order to be delivered at "+model.getOrderreceivetime()+" today.");
                }
                else viewHolder.rlort.setVisibility(View.GONE);

                String orderidstr = adapter.getRef(position).getKey();
                orderidstr = "#"+orderidstr.substring(1,orderidstr.length());
                viewHolder.txtorderid.setText(orderidstr);
                viewHolder.txtordertime.setText(Common.getDate(Long.parseLong(model.getTimeinms())));

                if(model.getDidrestaurantaccepted().equals("1"))
                {
                    viewHolder.btncancel.setVisibility(View.GONE);
                    viewHolder.btnaccept.setVisibility(View.VISIBLE);
                    viewHolder.btnaccept.setText("ACCEPTED");
                    viewHolder.btnaccept.setEnabled(false);
                }
                else if(model.getDidrestaurantaccepted().equals("-1"))
                {
                    viewHolder.btnaccept.setVisibility(View.GONE);
                    viewHolder.btncancel.setVisibility(View.VISIBLE);
                    viewHolder.btncancel.setText("Cancelled");
                    viewHolder.btncancel.setEnabled(false);
                }
                else
                {
                    viewHolder.btnaccept.setVisibility(View.VISIBLE);
                    viewHolder.btncancel.setVisibility(View.VISIBLE);
                    viewHolder.btncancel.setEnabled(true);
                    viewHolder.btnaccept.setEnabled(true);
                    viewHolder.btncancel.setText("Cancel");
                    viewHolder.btnaccept.setText("Accept");
                }

                viewHolder.txtdeliveryboyname.setText(model.getDeliveryboyname());

                String otp = model.getTimeinms();
                otp = otp.substring(otp.length()-8,otp.length()-4);
                viewHolder.txtotp.setText("OTP : "+otp);

                int totalamount =Integer.parseInt(model.getTotalamount())-Integer.parseInt(model.getDeliverycharge());
                viewHolder.txttotalamount.setText("Total: ₹"+totalamount);

                viewHolder.btnaccept.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        if(model.getOrderstatus().equals("11"))
                            isadvord=true;
                        else
                            isadvord=false;


                        DatabaseReference orderstatusref = database.getReference("CurrentRequests")
                                .child(adapter.getRef(position).getKey());
                        orderstatusref.child("didrestaurantaccepted").setValue("1");
                        orderstatusref.child("orderstatus").setValue("3");
                        if(model.getOrderreceivetime().equals("ASAP"))
                        orderstatusref.child("orderstatusmessage").setValue("Your food is getting prepared...");
                        else
                            orderstatusref.child("orderstatusmessage").setValue("Your food will be prepared & delivered on requested time...");

                        orderstatusref.child("adminstatus").setValue("3");
                        orderstatusref.child("city_zone_status").setValue(model.getCity()+model.getZone()+"3");

                        if(!isadvord) {
                            sendAcceptedOrderStatusToCustomer(adapter.getRef(position).getKey(), model.getCustomerphone());
                            sendAcceptedOrderStatusToDeliveryBoy(adapter.getRef(position).getKey(), model.getDeliveryboyphone());
                        }
                        else
                        {
                            sendAdvAcceptedOrderStatusToCustomer(adapter.getRef(position).getKey(), model.getCustomerphone());
                            sendAdvAcceptedOrderStatusToDeliveryBoy(adapter.getRef(position).getKey(), model.getDeliveryboyphone());
                        }

                        viewHolder.btncancel.setVisibility(View.GONE);
                        viewHolder.btnaccept.setText("ACCEPTED");
                        viewHolder.btnaccept.setEnabled(false);

                        //set 45min timer before order delivery?

                        Calendar calendar = Calendar.getInstance();
                        long time;

                        //getting the alarm manager
                        AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);


                        //creating a new intent specifying the broadcast receiver
                        Intent inte = new Intent(Orders.this, MyAlarm.class);
                        inte.putExtra("currentrequest",adapter.getRef(position).getKey());

                        //creating a pending intent using the intent
                        PendingIntent pi = PendingIntent.getBroadcast(Orders.this, 0, inte, 0);

                        //setting the repeating alarm that will be fired

                        //We need a calendar object to get the specified time in millis
                        //as the alarm manager method takes time in millis to setup the alarm
                        int hr,min;
                        hr =  Integer.parseInt(model.getOrderreceivetime().substring(0,2));
                        min = Integer.parseInt(model.getOrderreceivetime().substring(3,5));

                        if(model.getRestaurantid().equals("1")) hr+=12;

                        if(min>=45)
                        {
                            min-=45;
                        }
                        else
                        {
                            if(hr==0) {
                                hr = 23;
                            }
                            else
                            {
                              hr-=1;
                            }
                            min = 60 - (45-min);
                        }

                        calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH),
                              hr,min,0);
                        time = calendar.getTimeInMillis();
                        am.set(AlarmManager.RTC, time, pi);
                        if(!model.getRestaurantid().equals("1")) {
                            sendAdvancedOrderAlarmToDeliveryBoy(adapter.getRef(position).getKey(), model.getDeliveryboyphone(), model.getOrderreceivetime());
                            sendAdvancedOrderAlarmToGuestAdmin(adapter.getRef(position).getKey() , model.getOrderreceivetime(),model.getCity(),model.getZone());
                        }
                        else
                        {if(hr==19) {
                            sendAdvancedOrderAlarmToDeliveryBoy(adapter.getRef(position).getKey(), model.getDeliveryboyphone(), "19:00");
                            sendAdvancedOrderAlarmToGuestAdmin(adapter.getRef(position).getKey() , "19:00",model.getCity(),model.getZone());
                        }
                        else {
                            sendAdvancedOrderAlarmToDeliveryBoy(adapter.getRef(position).getKey(), model.getDeliveryboyphone(), "20:00");
                            sendAdvancedOrderAlarmToGuestAdmin(adapter.getRef(position).getKey() , "20:00",model.getCity(),model.getZone());
                        }
                        }


                    }
                });

                viewHolder.btncancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final DatabaseReference orderstatusref = database.getReference("CurrentRequests")
                                .child(adapter.getRef(position).getKey());

                        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(Orders.this);
                        alertDialogBuilder.setTitle("Are You Sure?");
                        alertDialogBuilder.setMessage("Order will get permanently cancelled !");
                        final EditText input = new EditText(Orders.this);
                        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT);
                        input.setLayoutParams(lp);
                        input.setHint("Enter reason for cancelling this order...");
                        alertDialogBuilder.setView(input);

                        alertDialogBuilder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                                if(!input.getText().equals(null)){

                                    orderstatusref.child("didrestaurantaccepted").setValue("-1");
                                    orderstatusref.child("orderstatus").setValue("-1");
                                    orderstatusref.child("orderstatusmessage").setValue("Order Cancelled : "+input.getText().toString());

                                    orderstatusref.child("adminstatus").setValue("-1");
                                    orderstatusref.child("restaurantid_co").setValue(model.getRestaurantid()+"NO");
                                    orderstatusref.child("restaurabtid_po").setValue(model.getRestaurantid()+"YES");
                                    orderstatusref.child("city_zone_status").setValue(model.getCity()+model.getZone()+"-1");

                                    viewHolder.btnaccept.setVisibility(View.GONE);
                                    viewHolder.btncancel.setText("Cancelled");
                                    viewHolder.btncancel.setEnabled(false);

                                    sendCancelledOrderStatusToUser(adapter.getRef(position).getKey(),model.getCustomerphone(),input.getText().toString());

                                }
                                else
                                    Toast.makeText(Orders.this, "Please enter reason to cancel order!", Toast.LENGTH_SHORT).show();
                            }
                        });
                        alertDialogBuilder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        });
                        alertDialogBuilder.show();

                    }
                });

                viewHolder.imgcalldeliveryboy.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + model.getDeliveryboyphone()));
                        if (ContextCompat.checkSelfPermission(Orders.this, android.Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                            ActivityCompat.requestPermissions(Orders.this, new String[]{android.Manifest.permission.CALL_PHONE},REQUEST_PHONE_CALL);
                        }
                        else
                        {
                            startActivity(intent);
                        }
                    }

                });

                viewHolder.txtviewbill.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent Viewbill = new Intent(Orders.this, ViewBillActivity.class);
                        Viewbill.putExtra("viewbill",adapter.getRef(position).getKey());
                        startActivity(Viewbill);
                    }
                });
            }
        };
    }

    private void sendAdvancedOrderAlarmToGuestAdmin(final String key, final String orderreceivetime, String city, String zone) {
        DatabaseReference tokens = database.getReference("Tokens");
        Query data =tokens.orderByChild("city_zone_isserver").equalTo(city+zone+"1"); // get all node with server token is true
        data.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot postSnapshot : dataSnapshot.getChildren())
                {
                    Token serverToken  = postSnapshot.getValue(Token.class);

                    //create raw play load to send
                    Notification notification = new Notification(orderreceivetime,"Order #"+key.substring(1,key.length())+": 45 min remaining in order #"+key.substring(1,key.length())+" delivery!","ORDERS");
                    Sender content = new Sender(serverToken.getToken(),notification);

                    mService.sendNotification(content)
                            .enqueue(new Callback<MyResponse>() {
                                @Override
                                public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {

                                    // if(response.code()==200){
                                    if (response.body().success == 1) {
                                        Toast.makeText(Orders.this, "Thankyou Order Placed !", Toast.LENGTH_SHORT).show();
                                        finish();
                                        //    }
                                    }else {
                                        Toast.makeText(Orders.this, "Failed !!!", Toast.LENGTH_SHORT).show();
                                    }

                                }

                                @Override
                                public void onFailure(Call<MyResponse> call, Throwable t) {
                                    Log.e("ERROR", t.getMessage());
                                }
                            });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void sendAdvAcceptedOrderStatusToDeliveryBoy(final String key, String deliveryboyphone) {

        DatabaseReference tokens = database.getReference("Tokens");
        tokens.orderByKey().equalTo(deliveryboyphone)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for(DataSnapshot postSnapshot  : dataSnapshot.getChildren())
                        {
                            Token token = postSnapshot.getValue(Token.class);

                            //make raw payload
                            Notification notification = new Notification("Advance Order accepted by restaurant","Order #"+key.substring(1,key.length())+" is accepted by restaurant !","ORDERS");
                            Sender content = new Sender(token.getToken(),notification);

                            mService.sendNotification(content)
                                    .enqueue(new Callback<MyResponse>() {
                                        @Override
                                        public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {
                                            if(response.body().success==1)
                                            {
                                                //do nothing
                                            }
                                            else
                                            {
                                                Toast.makeText(Orders.this, "Order was updated but failed to send notification", Toast.LENGTH_SHORT).show();
                                            }
                                        }

                                        @Override
                                        public void onFailure(Call<MyResponse> call, Throwable t) {
                                            Log.e("ERROR", t.getMessage());
                                        }
                                    });

                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });

    }

    private void sendAdvAcceptedOrderStatusToCustomer(final String key, String customerphone) {

        DatabaseReference tokens = database.getReference("Tokens");
        tokens.orderByKey().equalTo(customerphone)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for(DataSnapshot postSnapshot  : dataSnapshot.getChildren())
                        {
                            Token token = postSnapshot.getValue(Token.class);

                            //make raw payload
                            Notification notification = new Notification("Your Order accepted by restaurant!","Your Order #"+key.substring(1,key.length())+" is confirmed !","ORDERS");
                            Sender content = new Sender(token.getToken(),notification);

                            mService.sendNotification(content)
                                    .enqueue(new Callback<MyResponse>() {
                                        @Override
                                        public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {
                                            if(response.body().success==1)
                                                Toast.makeText(Orders.this, "Order accepted successfully!", Toast.LENGTH_SHORT).show();
                                            else
                                            {
                                                Toast.makeText(Orders.this, "Order was updated but failed to send notification", Toast.LENGTH_SHORT).show();
                                            }
                                        }

                                        @Override
                                        public void onFailure(Call<MyResponse> call, Throwable t) {
                                            Log.e("ERROR", t.getMessage());
                                        }
                                    });

                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    private void sendAdvancedOrderAlarmToDeliveryBoy(final String key, String deliveryboyphone, final String ort) {
        DatabaseReference tokens = database.getReference("Tokens");
        tokens.orderByKey().equalTo(deliveryboyphone)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for(DataSnapshot postSnapshot  : dataSnapshot.getChildren())
                        {
                            Token token = postSnapshot.getValue(Token.class);

                            //make raw payload
                            Notification notification = new Notification(ort,"45 min remaining in order #"+key.substring(1,key.length())+" delivery!","ORDERS");
                            Sender content = new Sender(token.getToken(),notification);

                            mService.sendNotification(content)
                                    .enqueue(new Callback<MyResponse>() {
                                        @Override
                                        public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {
                                            if(response.body().success==1)
                                            {
                                                //do nothing
                                            }
                                            else
                                            {
                                                Toast.makeText(Orders.this, "Order was updated but failed to send notification", Toast.LENGTH_SHORT).show();
                                            }
                                        }

                                        @Override
                                        public void onFailure(Call<MyResponse> call, Throwable t) {
                                            Log.e("ERROR", t.getMessage());
                                        }
                                    });

                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });

    }

    private void sendAcceptedOrderStatusToDeliveryBoy(final String key, String deliveryboyphone) {

        DatabaseReference tokens = database.getReference("Tokens");
        tokens.orderByKey().equalTo(deliveryboyphone)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for(DataSnapshot postSnapshot  : dataSnapshot.getChildren())
                        {
                            Token token = postSnapshot.getValue(Token.class);

                            //make raw payload
                            Notification notification = new Notification("Order accepted by restaurant","Order #"+key.substring(1,key.length())+" is cooking now !","ORDERS");
                            Sender content = new Sender(token.getToken(),notification);

                            mService.sendNotification(content)
                                    .enqueue(new Callback<MyResponse>() {
                                        @Override
                                        public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {
                                            if(response.body().success==1)
                                            {
                                                //do nothing
                                            }
                                            else
                                            {
                                                Toast.makeText(Orders.this, "Order was updated but failed to send notification", Toast.LENGTH_SHORT).show();
                                            }
                                        }

                                        @Override
                                        public void onFailure(Call<MyResponse> call, Throwable t) {
                                            Log.e("ERROR", t.getMessage());
                                        }
                                    });

                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });

    }

    private void sendAcceptedOrderStatusToCustomer(final String key, String customerphone) {


        DatabaseReference tokens = database.getReference("Tokens");
        tokens.orderByKey().equalTo(customerphone)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for(DataSnapshot postSnapshot  : dataSnapshot.getChildren())
                        {
                            Token token = postSnapshot.getValue(Token.class);

                            //make raw payload
                            Notification notification = new Notification("Your Order accepted by restaurant","Your Order #"+key.substring(1,key.length())+" is cooking now !","ORDERS");
                            Sender content = new Sender(token.getToken(),notification);

                            mService.sendNotification(content)
                                    .enqueue(new Callback<MyResponse>() {
                                        @Override
                                        public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {
                                            if(response.body().success==1)
                                                Toast.makeText(Orders.this, "Order accepted successfully!", Toast.LENGTH_SHORT).show();
                                            else
                                            {
                                                Toast.makeText(Orders.this, "Order was updated but failed to send notification", Toast.LENGTH_SHORT).show();
                                            }
                                        }

                                        @Override
                                        public void onFailure(Call<MyResponse> call, Throwable t) {
                                            Log.e("ERROR", t.getMessage());
                                        }
                                    });

                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

    }

    private void sendCancelledOrderStatusToUser(final String key, String customerphone, final String reasoncancelled) {

        DatabaseReference tokens = database.getReference("Tokens");
        tokens.orderByKey().equalTo(customerphone)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for(DataSnapshot postSnapshot  : dataSnapshot.getChildren())
                        {
                            Token token = postSnapshot.getValue(Token.class);

                            //make raw payload
                            Notification notification = new Notification(reasoncancelled,"Your order #"+key.substring(1,key.length())+" is cancelled!","ORDERS");
                            Sender content = new Sender(token.getToken(),notification);

                            mService.sendNotification(content)
                                    .enqueue(new Callback<MyResponse>() {
                                        @Override
                                        public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {
                                            if(response.body().success==1)
                                                Toast.makeText(Orders.this, "Order Cancelled Successfully!", Toast.LENGTH_SHORT).show();
                                            else
                                            {
                                                Toast.makeText(Orders.this, "Order was updated but failed to send notification", Toast.LENGTH_SHORT).show();
                                            }
                                        }

                                        @Override
                                        public void onFailure(Call<MyResponse> call, Throwable t) {
                                            Log.e("ERROR", t.getMessage());
                                        }
                                    });

                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }


}
