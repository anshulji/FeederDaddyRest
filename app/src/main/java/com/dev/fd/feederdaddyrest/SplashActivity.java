package com.dev.fd.feederdaddyrest;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.dev.fd.feederdaddyrest.Common.Common;
import com.dev.fd.feederdaddyrest.model.User;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class SplashActivity extends AppCompatActivity {

    private static final String APP_VERSION = "1.0";
    private String app_version,phone;

    //waiting duration
    private  int SPLASH_DISPLAY_LENGTH = 500;
    RelativeLayout logo,signuplayout;
    Button btnsendotp;
    EditText edtusername,edtphone,edtpassword;
    private FirebaseAuth mAuth;

    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;


    //Handler handler = new Handler();
    /*Runnable runnable = new Runnable() {
        @Override
        public void run() {
            logo.setVisibility(View.VISIBLE);
        }
    };*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        logo = findViewById(R.id.fdlogo);
        signuplayout=findViewById(R.id.signup_layout);
        edtphone = findViewById(R.id.edtphone);
        edtpassword = findViewById(R.id.edtpassword);
        edtusername = findViewById(R.id.edtusername);
        btnsendotp = findViewById(R.id.btnsendotp);

        /* New Handler to start the Menus-Activity
         * and close this Splash-Screen after some seconds.*/
        mAuth=FirebaseAuth.getInstance();
        SharedPreferences sharedPreferences = getSharedPreferences("MyData", Context.MODE_PRIVATE);
        app_version = sharedPreferences.getString("appversion","N/A");

        if(!app_version.equals(APP_VERSION))
        {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("appversion",APP_VERSION);
            editor.putString("phone", "N/A");
            editor.commit();
        }

        phone = sharedPreferences.getString("phone","N/A");

        if(!phone.equals("N/A"))
        {
            SPLASH_DISPLAY_LENGTH=0;
        }

        //handler.postDelayed(runnable,1000);

        new Handler().postDelayed(new Runnable(){
            @Override
            public void run() {

                if(phone.equals("N/A"))
                { signuplayout.setVisibility(View.VISIBLE);
                }
                else {

                /* Create an Intent that will start the Menus-Activity. */
                    if(!Common.isConnectedToInternet(getBaseContext()))
                    {
                        Toast.makeText(SplashActivity.this, "Please Check Your Internet Connection !", Toast.LENGTH_LONG).show();
                    }

                    Intent mainIntent = new Intent(SplashActivity.this,MenuActivity.class);
                startActivity(mainIntent);
                finish();

                }
            }
        }, SPLASH_DISPLAY_LENGTH);

        btnsendotp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(edtusername.getText().toString().equals(""))
                {
                    Toast.makeText(SplashActivity.this, "Please Enter Username !", Toast.LENGTH_SHORT).show();
                }
                else if(edtphone.getText().toString().length()!=10)
                {
                    Toast.makeText(SplashActivity.this, "Please Enter Valid 10 digit Phone No.", Toast.LENGTH_SHORT).show();
                }
                else if(edtpassword.getText().equals(""))
                {
                    Toast.makeText(SplashActivity.this, "Please Enter Password !", Toast.LENGTH_SHORT).show();
                }
                else {
                    Toast.makeText(SplashActivity.this, "Please Wait... ", Toast.LENGTH_SHORT).show();
                    firebaseDatabase = FirebaseDatabase.getInstance();
                    databaseReference = firebaseDatabase.getReference("RestaurantList");

                    databaseReference.orderByChild("username").equalTo(edtusername.getText().toString()).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {


                            User user = null;
                            
                            for(DataSnapshot postSnapshot :dataSnapshot.getChildren()) {

                                 user = postSnapshot.getValue(User.class);
                            }
                            if(user==null)
                            {
                                Toast.makeText(SplashActivity.this, "Username Doesn't exist !", Toast.LENGTH_SHORT).show();
                            }
                            else if(!user.getPassword().equals(edtpassword.getText().toString()))
                            {
                                Toast.makeText(SplashActivity.this, "Incorrect Password !", Toast.LENGTH_SHORT).show();
                            }
                            else
                            {
                                String restaurantid= user.getRestaurantid();
                                String city  = user.getCity();

                                //SharedPreferences sharedPreferences = getSharedPreferences("MyData", Context.MODE_PRIVATE);
                                //SharedPreferences.Editor editor = sharedPreferences.edit();
                                //editor.putString("restaurantid",restaurantid);
                                //editor.commit();
                                Intent intent = new Intent(SplashActivity.this, SignUpActivity.class);
                                intent.putExtra("username", edtusername.getText().toString());
                                intent.putExtra("phone", edtphone.getText().toString());
                                intent.putExtra("restaurantid",restaurantid);
                                intent.putExtra("password",edtpassword.getText().toString());
                                intent.putExtra("restaurantname",user.getRestaurantname());
                                intent.putExtra("city",user.getCity());
                                intent.putExtra("restauranttype",user.getRestauranttype());
                                startActivity(intent);
                                finish();
                            }

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });




                }
            }
        });


    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if(currentUser==null)
        {
        }
    }
}
