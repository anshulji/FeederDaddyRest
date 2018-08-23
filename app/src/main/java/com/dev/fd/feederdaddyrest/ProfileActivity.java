package com.dev.fd.feederdaddyrest;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.media.Image;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.dev.fd.feederdaddyrest.Common.Common;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import info.hoang8f.widget.FButton;

public class ProfileActivity extends AppCompatActivity {

    private MaterialEditText edtrestaurantname,edtrestaurantaddress,edtphonenumber;

    RadioGroup rghideshow,rgvegnonveg;

    Place shippingAddress;
    PlaceAutocompleteFragment edtAddress;
    ImageView imggoback,imgrestimg,imgeditrestimg;



    private FButton  btnupdate;
    private Button btnopentime,btnclosetime;
    private CheckBox checkBox;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference,databaseReference1,restref;
    private String addressassigned="",location="";
    private int btntype = 0, tm = 1, flag = 0, fg = 0;
    String phoneNumber, phoneno, username,city;

    private static final String TAG = "ProfileActivity";
    private static final int ERROR_DIALOG_REQUEST = 9001;

    private static final String FINE_LOCATION = android.Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COURSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;

    private boolean mLocationPermissionsGranted=false;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;

    private FusedLocationProviderClient mFusedLocationproviderClient;
    private double latitude=0,longitude=0;
    private Geocoder geocoder;
    protected LocationManager locationManager;
    private String address;
    private boolean isCheckBoxTicked=true;


    Uri saveUri=null;
    private  final int PICK_IMAGE_REQUEST =71;

    FirebaseStorage storage;
    StorageReference storageReference;

    String downloadurl;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

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
        checkBox = findViewById(R.id.checkbox);
        btnupdate = findViewById(R.id.btnupdate);
        edtphonenumber = findViewById(R.id.edtphonenumber);
        edtrestaurantname = findViewById(R.id.edtrestaurantname);
        edtrestaurantaddress = findViewById(R.id.edtrestaurantaddress);
        rghideshow = findViewById(R.id.radioGrouphideshow);
        rgvegnonveg = findViewById(R.id.radioGroupVegNonVeg);
        btnopentime = findViewById(R.id.btnopentime);
        btnclosetime = findViewById(R.id.btnclosetime);


        //init storage
        storage=FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        btnopentime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimePickerDialog mTimePicker;
                mTimePicker = new TimePickerDialog(ProfileActivity.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                        String shour, sminute;
                        shour = String.valueOf(selectedHour);
                        sminute = String.valueOf(selectedMinute);
                        if (selectedHour == 0)
                            shour = "00";
                        if (selectedMinute == 0)
                            sminute = "00";
                        if(shour.length()==1)
                        {
                            shour="0"+shour;
                        }
                        if(sminute.length()==1)
                        {
                            sminute="0"+sminute;
                        }

                        btnopentime.setText(shour + ":" + sminute);
                    }
                }, 0, 0, true);//Yes 24 hour time
                mTimePicker.setTitle("Select Restaurant's Opening Time");
                mTimePicker.show();
            }
        });
        btnclosetime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimePickerDialog mTimePicker;
                mTimePicker = new TimePickerDialog(ProfileActivity.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                        String shour, sminute;
                        shour = String.valueOf(selectedHour);
                        sminute = String.valueOf(selectedMinute);
                        if (selectedHour == 0)
                            shour = "00";
                        if (selectedMinute == 0)
                            sminute = "00";
                        if(shour.length()==1)
                        {
                            shour="0"+shour;
                        }
                        if(sminute.length()==1)
                        {
                            sminute="0"+sminute;
                        }
                        btnclosetime.setText(shour + ":" + sminute);
                    }
                }, 0, 0, true);//Yes 24 hour time
                mTimePicker.setTitle("Select Restaurant's Closing Time");
                mTimePicker.show();
            }
        });


        edtAddress = (PlaceAutocompleteFragment) getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);
        //hide search icon before fragment
        edtAddress.getView().findViewById(R.id.place_autocomplete_search_button).setVisibility(View.GONE);
        //set hint for autocomplete edit text
        ((EditText)edtAddress.getView().findViewById(R.id.place_autocomplete_search_input)).setHint("Enter your Location");
        //set text size
        ((EditText)edtAddress.getView().findViewById(R.id.place_autocomplete_search_input)).setTextSize(18);

        // get address from autocomplete
        edtAddress.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                shippingAddress = place;
                addressassigned="";
            }
            @Override
            public void onError(Status status) {
                Log.e("Error",status.getStatusMessage() );
            }
        });

        geocoder = new Geocoder(this, Locale.getDefault());
        locationManager = (LocationManager) this.getSystemService(LOCATION_SERVICE);

        //get old data from shared preferences and set on edit text views
        SharedPreferences sharedPreferences = getSharedPreferences("MyData", Context.MODE_PRIVATE);
        String strphone = sharedPreferences.getString("phone","N/A");
        String strusername = sharedPreferences.getString("name","N/A");
        String strrestaurantname = sharedPreferences.getString("restaurantname","N/A");
        final String strrestaurantid = sharedPreferences.getString("restaurantid","N/A");
        String strrestaurantaddress = sharedPreferences.getString("restaurantaddress","N/A");
        String strlocation = sharedPreferences.getString("location","N/A");
        String strlatitude = sharedPreferences.getString("latitude","N/A");
        String strlongitude = sharedPreferences.getString("longitude","N/A");
        String stropenclosetime = sharedPreferences.getString("openclosetime","N/A");
        String strveg = sharedPreferences.getString("veg","0");
        String strnonveg = sharedPreferences.getString("nonveg","0");
        String strhideshow = sharedPreferences.getString("hideshow","N/A");
         city = sharedPreferences.getString("city","N/A");


        imgrestimg= findViewById(R.id.imgrestimg);
        imgeditrestimg = findViewById(R.id.imgeditrestimg);

        firebaseDatabase = FirebaseDatabase.getInstance();
        restref = firebaseDatabase.getReference(city).child("Restaurant").child(strrestaurantid).child("image");

        restref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.getValue()==null)
                {
                    restref.setValue("null");
                }
                else
                {
                    Picasso.with(getBaseContext()).load(dataSnapshot.getValue().toString()).into(imgrestimg);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        imgeditrestimg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseImage();
            }
        });

        if(strveg.equals("1") && strnonveg.equals("1"))
        {
            RadioButton radioButton = findViewById(R.id.radiovegnon);
            radioButton.setChecked(true);
        }
        else if(strveg.equals("1"))
        {
            RadioButton radioButton = findViewById(R.id.radioveg);
            radioButton.setChecked(true);
        }
        else
        {
            RadioButton radioButton = findViewById(R.id.radiononveg);
            radioButton.setChecked(true);
        }

        if(strhideshow.equals("3"))
        {
            RadioButton radioButton = findViewById(R.id.radiohide);
            radioButton.setChecked(true);
        }
        else
        {
            RadioButton radioButton = findViewById(R.id.radioshow);
            radioButton.setChecked(true);
        }

        edtrestaurantname.setText(strrestaurantname);
        edtphonenumber.setText(strphone);

        if(!stropenclosetime.equals("N/A")) {
            btnopentime.setText(stropenclosetime.substring(0, 5));
            btnclosetime.setText(stropenclosetime.substring(6, 11));
        }
        edtrestaurantaddress.setText(strrestaurantaddress);
        edtAddress.setText(strlocation);
        phoneno = strphone;
        username = strusername;
        addressassigned=strlocation;
        edtAddress.setText(strlocation);
        if(!strlatitude.equals("N/A")) {
            latitude = Double.valueOf(strlatitude);
            longitude = Double.valueOf(strlongitude);
        }
        btnupdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (edtrestaurantname.getText().toString().equals(""))
                    Toast.makeText(ProfileActivity.this, "Please enter Restaurant Name", Toast.LENGTH_SHORT).show();
                else if (btnopentime.getText().toString().equals("SET") || btnclosetime.getText().toString().equals("SET"))
                    Toast.makeText(ProfileActivity.this, "Please set Open-Close time", Toast.LENGTH_SHORT).show();
                else if (edtphonenumber.getText().toString().equals(""))
                    Toast.makeText(ProfileActivity.this, "Please enter Phone Number", Toast.LENGTH_SHORT).show();
                else if(edtrestaurantaddress.getText().toString().equals(""))
                {
                    Toast.makeText(ProfileActivity.this, "Please Enter Restaurant Address", Toast.LENGTH_SHORT).show();
                }
                else if (shippingAddress==null && addressassigned.equals(""))
                    Toast.makeText(ProfileActivity.this, "Please set Restaurant Location", Toast.LENGTH_SHORT).show();
                else {

                    if(addressassigned.equals("")) {
                        address = edtrestaurantaddress.getText().toString();
                        LatLng ll=shippingAddress.getLatLng();
                        latitude=ll.latitude;
                        longitude=ll.longitude;
                        location = shippingAddress.getAddress().toString();

                    }
                    else {
                        address = edtrestaurantaddress.getText().toString();
                        location = addressassigned;
                    }
                    Toast.makeText(ProfileActivity.this, "Updating...", Toast.LENGTH_SHORT).show();

                    String isbakery="3";

                    int selectedid = rghideshow.getCheckedRadioButtonId();
                    if(selectedid==R.id.radiohide)
                    {
                        isbakery="3";
                    }
                    else
                    {
                        String restauranttype = Common.getRestauranttype(getBaseContext());
                        if(restauranttype.equals("restaurant"))
                            isbakery="0";
                        else if (restauranttype.equals("bakery"))
                            isbakery="1";
                        else
                            isbakery="3";

                    }
                    selectedid = rgvegnonveg.getCheckedRadioButtonId();
                    String veg="0",nonveg="0";
                    if(selectedid==R.id.radioveg)
                    {
                        veg="1"; nonveg="0";
                    }
                    else if(selectedid == R.id.radiononveg)
                    {
                        veg="0"; nonveg="1";
                    }
                    else
                    {
                        veg="1"; nonveg="1";
                    }

                    databaseReference = firebaseDatabase.getReference(city).child("Restaurant").child(strrestaurantid);
                    databaseReference.child("name").setValue(edtrestaurantname.getText().toString());
                    databaseReference.child("opentime").setValue(btnopentime.getText().toString()+"-"+btnclosetime.getText().toString());
                    databaseReference.child("areaname").setValue(address);
                    databaseReference.child("latitude").setValue(String.valueOf(latitude));
                    databaseReference.child("longitude").setValue(String.valueOf(longitude));
                    databaseReference.child("veg").setValue(veg);
                    databaseReference.child("nonveg").setValue(nonveg);
                    databaseReference.child("isbakery").setValue(isbakery);
                    databaseReference.child("restaurantphone").setValue(phoneno);
                    databaseReference.child("id").setValue(strrestaurantid);
                    databaseReference.child("isopen").setValue(Common.getIsopen(getBaseContext()));
                    if(saveUri!=null){
                        uploadImage();
                    }
                    databaseReference.child("rating").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if(dataSnapshot.getValue()==null) {
                                databaseReference.child("rating").setValue("5.0");
                                databaseReference.child("totalrates").setValue("0");
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });



                    databaseReference1 = firebaseDatabase.getReference("RestaurantList").child(strrestaurantid);
                    databaseReference1.child("phone").setValue(edtphonenumber.getText().toString());
                    databaseReference1.child("restaurantname").setValue(edtrestaurantname.getText().toString());

                    SharedPreferences sharedPreferences = getSharedPreferences("MyData", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("phone", phoneno);
                    if(latitude!=0)
                        editor.putString("latitude",String.valueOf(latitude));
                    if(longitude!=0)
                        editor.putString("longitude",String.valueOf(longitude));
                    editor.putString("restaurantaddress",address);
                    editor.putString("openclosetime",btnopentime.getText().toString()+"-"+btnclosetime.getText().toString());
                    if(!addressassigned.equals(""))
                     editor.putString("location",location);
                    else


                    editor.putString("restaurantname", edtrestaurantname.getText().toString());
                    editor.putString("veg",veg);
                    editor.putString("nonveg",nonveg);
                    editor.putString("hideshow",isbakery);

                    //editor.putString("address",address);
                    editor.commit();

                    //getFragmentManager().beginTransaction().remove(getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment))
                    //      .commit();

                    Toast.makeText(ProfileActivity.this, "Updated Successfully !", Toast.LENGTH_SHORT).show();
                    Bundle bundle = getIntent().getExtras();
                    if (bundle != null) {
                        String strgoback = bundle.getString("comeback");
                        if(strgoback.equals("yes"))
                        {Intent returnIntent = new Intent();
                            returnIntent.putExtra("result","1");
                            setResult(Activity.RESULT_OK,returnIntent);
                        }
                        //databaseReference = firebaseDatabase.getReference("category").child(category);
                    }
                }
            }
        });

        checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!checkBox.isChecked()) {
                    edtAddress.setText("");
                    addressassigned="";
                    // currentAddtv.setText("");
                } else {

                    if(mLocationPermissionsGranted) {
                        boolean isGPSEnabled = false;
                        isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
                        if(isGPSEnabled) {
                            getDevicelocation();
                        }
                        else
                        {enableGPS();
                            getDevicelocation();
                        }
                    }
                    else
                    { getLocationPermission();
                        enableGPS();
                        getDevicelocation();
                    }

                    if(latitude==0)
                        checkBox.setChecked(false);

                }
            }
        });


    }
    private void getDevicelocation(){
        Log.d(TAG, "getDevicelocation: getting the device's current location");

        mFusedLocationproviderClient = LocationServices.getFusedLocationProviderClient(this);

        try{
            if(mLocationPermissionsGranted){
                Task location = mFusedLocationproviderClient.getLastLocation();
                location.addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if(task.isSuccessful()){
                            Log.d(TAG, "onComplete: Found Location");
                            Location currentLocation = (Location) task.getResult();

                            if(currentLocation!=null) {
                                latitude = currentLocation.getLatitude();
                                longitude = currentLocation.getLongitude();


                                // Toast.makeText(SignUpActivity.this ,"address:"+latitude+" "+longitude,Toast.LENGTH_SHORT).show();

                                List<Address> addresses = new ArrayList<>();
                                try {
                                    addresses = geocoder.getFromLocation(latitude, longitude, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }

                                String address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
                                String city = addresses.get(0).getLocality();
                                String state = addresses.get(0).getAdminArea();
                                String country = addresses.get(0).getCountryName();
                                String postalCode = addresses.get(0).getPostalCode();
                                String knownName = addresses.get(0).getFeatureName();
                                for (int i = 0; i < address.length(); i++) {
                                    if (address.substring(i, i + 5).equals("India")) {
                                        address = address.substring(0, i + 5);
                                        break;
                                    }
                                }
                                edtAddress.setText(address);
                                addressassigned=address;
                                //currentAddtv.setText(address);
                                checkBox.setChecked(true);
                            }


                        }else{
                            Log.d(TAG, "onComplete: Current location is null");
                            Toast.makeText(ProfileActivity.this, "Unable to detect your location !", Toast.LENGTH_SHORT).show();
                        }

                    }
                });
            }

        }catch (SecurityException e){
            Log.e(TAG, "getDevicelocation: security exception"+ e.getMessage() );

        }
    }

    private void getLocationPermission() {
        String permissions[] = {FINE_LOCATION,COURSE_LOCATION};

        if(ContextCompat.checkSelfPermission(this.getApplicationContext(),FINE_LOCATION)== PackageManager.PERMISSION_GRANTED)
        {
            if(ContextCompat.checkSelfPermission(this.getApplicationContext(),COURSE_LOCATION)==PackageManager.PERMISSION_GRANTED)
            {
                mLocationPermissionsGranted=true;
            }else {
                ActivityCompat.requestPermissions(this,permissions,LOCATION_PERMISSION_REQUEST_CODE);
            }
        }
        else {
            ActivityCompat.requestPermissions(this,permissions,LOCATION_PERMISSION_REQUEST_CODE);
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        mLocationPermissionsGranted=false;

        switch (requestCode){
            case LOCATION_PERMISSION_REQUEST_CODE:{
                if(grantResults.length>0)
                { for(int i=0;i<grantResults.length;i++)
                {
                    if(grantResults[i]!=PackageManager.PERMISSION_GRANTED){
                        mLocationPermissionsGranted=false;
                        return;
                    }
                }
                    mLocationPermissionsGranted=true;
                }
            }
        }
    }

    //-----------------------------------------location------------------------------//

    private void init() {
    }

    public boolean isServicesOK(){
        Log.d(TAG, "isServicesOK: chicking google services version");
        int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(ProfileActivity.this);
        if(available== ConnectionResult.SUCCESS)
        {  //everything is fine and user can make map request
            Log.d(TAG, "isServicesOK: google play services is working");
            return true;
        }
        else if(GoogleApiAvailability.getInstance().isUserResolvableError(available))
        {//an error occured but we can resolve it
            Log.d(TAG, "isServicesOK: an error occured but we can fix it");
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(ProfileActivity.this,available,ERROR_DIALOG_REQUEST);
            dialog.show();
        }
        else
        {
            Toast.makeText(this, "You Can't make map requests !", Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    private void enableGPS() {

        boolean isGPSEnabled = false;
        isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if(!isGPSEnabled) {
            final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(ProfileActivity.this,R.style.MyDialogTheme);
            alertDialogBuilder.setTitle("Turn On GPS");
            alertDialogBuilder.setMessage("Turn on your GPS, to detect your location.");
            alertDialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(intent);
                }
            });
            alertDialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.cancel();
                }
            });
            alertDialogBuilder.show();
        }
    }

    @Override
    public void onBackPressed() {
       finish();
    }

    private void chooseImage() {
        Intent intent = new Intent(Intent.ACTION_PICK,android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent,PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == PICK_IMAGE_REQUEST && resultCode==RESULT_OK && data!=null && data.getData()!=null){
            saveUri = data.getData();

            imgrestimg.setImageURI(saveUri);
        }
    }

    private void uploadImage() {

        if(saveUri!=null)
        {
            final ProgressDialog mDialog = new ProgressDialog(this);
            mDialog.setMessage("Uploading...");
            mDialog.show();

            String imageName = UUID.randomUUID().toString();
            final StorageReference imagefolder = storageReference.child("images/restimages/"+city+"/"+imageName);
            imagefolder.putFile(saveUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            mDialog.dismiss();
                            Toast.makeText(ProfileActivity.this, "Uploaded !", Toast.LENGTH_SHORT).show();
                            imagefolder.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    downloadurl = uri.toString();

                                    restref.setValue(uri.toString());

                                }
                            });

                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    mDialog.dismiss();
                    Toast.makeText(ProfileActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();

                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                    double progress = (100.0 * taskSnapshot.getBytesTransferred()/taskSnapshot.getTotalByteCount());
                    String progressstr =  String.format("%.0f",progress);

                    mDialog.setMessage("Uploaded "+progressstr+"%");
                }
            });
        }
    }


}
