package com.dev.fd.feederdaddyrest;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.Toast;

import com.dev.fd.feederdaddyrest.Common.Common;
import com.dev.fd.feederdaddyrest.Database.Database;
import com.dev.fd.feederdaddyrest.ViewHolder.AddOnViewHolder;
import com.dev.fd.feederdaddyrest.model.AddOnsModel;
import com.dev.fd.feederdaddyrest.model.Food;
import com.dev.fd.feederdaddyrest.model.Order;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AddNewFoodActivity extends AppCompatActivity {

    EditText etfoodname,etfooddescription,etquartername,ethalfname,etfullname,etquarterprice,etquarterdiscount,ethalfprice,ethalfdiscount,etfullprice,etfulldiscount;
    RadioButton rbveg,rbnonveg;
    Button btnselectfoodimage, btnsave;
    CheckBox cbquarter,cbhalf,cbfull;
    RecyclerView recycler_addons;
    RecyclerView.LayoutManager layoutManager;

    //add on rv adapter
    FirebaseRecyclerAdapter<AddOnsModel,AddOnViewHolder> addonadapter;

    //new food class obect
    Food newFood;

    //get intent strings
    String city,MenuId,RestaurantId,FoodId,totalfooditems,update,rating="5.0",totalrates="0";

    //string of quarter price =null
    String qp="null",hp="null",fp="null";

    //Firebase
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference,databaseReferenceaddonslist,databaseReferenceprevdata;
    FirebaseStorage storage;
    StorageReference storageReference;

    //addons list to store addons to be uploaded
    List<AddOnsModel> addOnsList = new ArrayList<>();

    private Uri mImageUri,mCropImageUri;
    private String downloadurl;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_food);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }


        ImageView imggoback = findViewById(R.id.imggoback);
        imggoback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        //set find view by id
        etfoodname = findViewById(R.id.etfoodname);
        etfooddescription = findViewById(R.id.etfooddescription);

        etquartername = findViewById(R.id.etquartername);
        ethalfname = findViewById(R.id.ethalfname);
        etfullname = findViewById(R.id.etfullname);

        etquarterprice = findViewById(R.id.etquarterprice);
        etquarterdiscount = findViewById(R.id.etquarterdiscount);
        ethalfprice = findViewById(R.id.ethalfprice);
        ethalfdiscount = findViewById(R.id.ethalfdiscount);
        etfullprice = findViewById(R.id.etfullprice);
        etfulldiscount = findViewById(R.id.etfulldiscount);
        cbquarter = findViewById(R.id.cbquarterprice);
        cbhalf =findViewById(R.id.cbhalfprice);
        cbfull = findViewById(R.id.cbfullprice);
        rbveg = findViewById(R.id.radioveg);
        rbnonveg = findViewById(R.id.radiononveg);
        btnselectfoodimage = findViewById(R.id.btnselectfoodimage);
        btnsave = findViewById(R.id.btnsave);
        recycler_addons = findViewById(R.id.recycler_addons);

        //setup recyclerview
        layoutManager =new LinearLayoutManager(this);
        recycler_addons.setLayoutManager(layoutManager);

        //get intents
        if(getIntent()!=null)
        {
            city = getIntent().getStringExtra("city");
            MenuId = getIntent().getStringExtra("menuid");
            RestaurantId = getIntent().getStringExtra("restaurantid");
            totalfooditems = getIntent().getStringExtra("totalitems");
            update = getIntent().getStringExtra("update");

        }

        //init firebase
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference(city).child("Foods").child(RestaurantId).child(MenuId);
        databaseReferenceaddonslist =  firebaseDatabase.getReference(city).child("AddOns").child(RestaurantId);
        storage=FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        if(update.equals("1"))
        {
            loadPreviousData();
        }
        //setup addons list
        if(!update.equals("1")) {
            //set checkbox and corresponding edt disabled
            setCheckBoxEdt(false, false, true);
            setAddOnsList();
        }

        //set select image btn listener
        btnselectfoodimage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFileChooser();
            }
        });

        btnsave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(etfoodname.getText().toString().equals(""))
                    Toast.makeText(AddNewFoodActivity.this, "Please enter food name!", Toast.LENGTH_SHORT).show();
                else if(etfooddescription.getText().toString().equals(""))
                    Toast.makeText(AddNewFoodActivity.this, "Please enter food description!", Toast.LENGTH_SHORT).show();
                else if(etfullname.getText().toString().equals(""))
                    Toast.makeText(AddNewFoodActivity.this, "Please enter size0 name!", Toast.LENGTH_SHORT).show();
                else if(etfullprice.getText().toString().equals(""))
                    Toast.makeText(AddNewFoodActivity.this, "Please enter size0 price!", Toast.LENGTH_SHORT).show();
                else if(etfulldiscount.getText().toString().equals(""))
                    Toast.makeText(AddNewFoodActivity.this, "Please set size0 discount!", Toast.LENGTH_SHORT).show();
                else if(cbhalf.isChecked() && ethalfname.getText().toString().equals(""))
                    Toast.makeText(AddNewFoodActivity.this, "Please enter size2 name!", Toast.LENGTH_SHORT).show();
                else if(cbhalf.isChecked() && ethalfprice.getText().toString().equals(""))
                    Toast.makeText(AddNewFoodActivity.this, "Please enter size2 price!", Toast.LENGTH_SHORT).show();
                else if(cbhalf.isChecked() && ethalfdiscount.getText().toString().equals(""))
                    Toast.makeText(AddNewFoodActivity.this, "Please set size2 discount!", Toast.LENGTH_SHORT).show();
                else if(cbquarter.isChecked() && etquartername.getText().toString().equals(""))
                    Toast.makeText(AddNewFoodActivity.this, "Please enter size1 name!", Toast.LENGTH_SHORT).show();
                else if(cbquarter.isChecked() && etquarterprice.getText().toString().equals(""))
                    Toast.makeText(AddNewFoodActivity.this, "Please enter size1 price!", Toast.LENGTH_SHORT).show();
                else if(cbquarter.isChecked() && etquarterdiscount.getText().toString().equals(""))
                    Toast.makeText(AddNewFoodActivity.this, "Please set size1 discount!", Toast.LENGTH_SHORT).show();
                else
                uploadData();
            }
        });

        cbquarter.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) setCheckBoxEdt(true,cbhalf.isChecked(),cbfull.isChecked());
                else setCheckBoxEdt(false,cbhalf.isChecked(),cbfull.isChecked());
            }
        });
        cbhalf.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) setCheckBoxEdt(cbquarter.isChecked(),true,cbfull.isChecked());
                else setCheckBoxEdt(cbquarter.isChecked(),false,cbfull.isChecked());
            }
        });
        cbfull.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) setCheckBoxEdt(cbquarter.isChecked(),cbhalf.isChecked(),true);
                else setCheckBoxEdt(cbquarter.isChecked(),cbhalf.isChecked(),false);
            }
        });

    }

    private void loadPreviousData() {

        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReferenceprevdata = firebaseDatabase.getReference(city).child("Foods").child(RestaurantId).child(MenuId).child(totalfooditems);
        databaseReferenceprevdata.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                etfoodname.setText(dataSnapshot.child("name").getValue().toString());
                etfooddescription.setText(dataSnapshot.child("description").getValue().toString());
                if(dataSnapshot.child("veg").getValue().toString().equals("1"))
                    rbveg.setChecked(true);
                else rbnonveg.setChecked(true);

                if(!dataSnapshot.child("quarterprice").getValue().toString().equals("null"))
                {
                    cbquarter.setChecked(true);
                    if(dataSnapshot.child("quartername").getValue()!=null)
                    etquartername.setText(dataSnapshot.child("quartername").getValue().toString());
                    etquarterprice.setText(dataSnapshot.child("quarterprice").getValue().toString());
                    etquarterdiscount.setText(dataSnapshot.child("quarterdiscount").getValue().toString());
                }
                if(!dataSnapshot.child("halfprice").getValue().toString().equals("null"))
                {
                    cbhalf.setChecked(true);
                    if(dataSnapshot.child("halfname").getValue()!=null)
                    ethalfname.setText(dataSnapshot.child("halfname").getValue().toString());
                    ethalfprice.setText(dataSnapshot.child("halfprice").getValue().toString());
                    ethalfdiscount.setText(dataSnapshot.child("halfdiscount").getValue().toString());
                }
                if(!dataSnapshot.child("fullprice").getValue().toString().equals("null"))
                {
                    cbfull.setChecked(true);
                    if(dataSnapshot.child("fullname").getValue()!=null)
                    etfullname.setText(dataSnapshot.child("fullname").getValue().toString());
                    etfullprice.setText(dataSnapshot.child("fullprice").getValue().toString());
                    etfulldiscount.setText(dataSnapshot.child("fulldiscount").getValue().toString());
                }

                long cnt = dataSnapshot.child("addons").getChildrenCount();
                int i=0;
                for(DataSnapshot postSnapshot : dataSnapshot.child("addons").getChildren())
                {
                    addOnsList.add(postSnapshot.getValue(AddOnsModel.class));
                    i++;
                    if(i==cnt)
                        setAddOnsList();
                }
                if(cnt==0)
                    setAddOnsList();


                rating = dataSnapshot.child("rating").getValue().toString();
                totalrates = dataSnapshot.child("totalrates").getValue().toString();
                if(dataSnapshot.child("image").getValue()!=null)
                downloadurl = dataSnapshot.child("image").getValue().toString();
                else downloadurl="";

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void uploadData() {

        String veg ="1",nonveg="1";

        if(cbquarter.isChecked()) { qp = etquarterprice.getText().toString(); }
        if(cbhalf.isChecked()) { hp = ethalfprice.getText().toString(); }
        if(cbfull.isChecked()) { fp = etfullprice.getText().toString(); }
        if(rbveg.isChecked()) {veg="1"; nonveg="0";}
        else if(rbnonveg.isChecked()) {veg="0"; nonveg="1";}


        newFood = new Food(etfoodname.getText().toString(),downloadurl,etfooddescription.getText().toString()
                            ,qp,hp,fp,etquarterdiscount.getText().toString(),ethalfdiscount.getText().toString()
                            ,etfulldiscount.getText().toString(),veg,nonveg,rating,totalrates);


        databaseReference.child(totalfooditems).setValue(newFood);

        if(cbquarter.isChecked()) { databaseReference.child(totalfooditems).child("quartername").setValue(etquartername.getText().toString()); }
        if(cbhalf.isChecked()) { databaseReference.child(totalfooditems).child("halfname").setValue(ethalfname.getText().toString()); }
        if(cbfull.isChecked()) { databaseReference.child(totalfooditems).child("fullname").setValue(etfullname.getText().toString()); }

        if(addOnsList.size()!=0)
        databaseReference.child(totalfooditems).child("addons").setValue(addOnsList);

        finish();
        Toast.makeText(this, "Uploaded !", Toast.LENGTH_SHORT).show();

    }

    private void openFileChooser() {
        CropImage.startPickImageActivity(this);
    }

    @Override
    @SuppressLint("NewApi")
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
        // handle result of pick image chooser
        if (requestCode == CropImage.PICK_IMAGE_CHOOSER_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            Uri imageUri = CropImage.getPickImageResultUri(this, data);

            // For API >= 23 we need to check specifically that we have permissions to read external storage.
            if (CropImage.isReadExternalStoragePermissionsRequired(this, imageUri)) {
                // request permissions and handle the result in onRequestPermissionsResult()
                mImageUri = imageUri;
                //requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},   CropImage.PICK_IMAGE_PERMISSIONS_REQUEST_CODE);
            } else {
                // no permissions required or already granted, can start crop image activity
                startCropImageActivity(imageUri);
            }
        }


        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                mImageUri = result.getUri();

                Bitmap bmp = null;
                try {
                    bmp = MediaStore.Images.Media.getBitmap(getContentResolver(), mImageUri);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ByteArrayOutputStream baos = new ByteArrayOutputStream();


                //here you can choose quality factor in third parameter(ex. i choosen 25)
                bmp.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                byte[] fileInBytes = baos.toByteArray();

                String encodedImage = Base64.encodeToString(fileInBytes, Base64.DEFAULT);
                SharedPreferences shre = PreferenceManager.getDefaultSharedPreferences(this);
                SharedPreferences.Editor edit = shre.edit();
                edit.putString("image_data",encodedImage);
                edit.commit();

                String imageName = UUID.randomUUID().toString();
                final StorageReference photoref = storageReference.child("images/restaurantfoodimages/"+city+"/"+ Common.getRestaurantId(AddNewFoodActivity.this)+"/"+imageName);

                //here i am uploading
                photoref.putBytes(fileInBytes).addOnSuccessListener(this, new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        // When the image has successfully uploaded, we get its download URL
                        //mProgressBar.setVisibility(ProgressBar.INVISIBLE);


                        photoref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                Uri downloadUrl =uri;
                                downloadurl = downloadUrl.toString();

                                //mDatabaseRef.child(phone).child("photoUrl").setValue(downloadurl);
                            }
                        });

                        Toast.makeText(AddNewFoodActivity.this, "Image Uploaded Successfully !", Toast.LENGTH_SHORT).show();
                        //Uri downloadUrl = taskSnapshot.getDownloadUrl();
                        //String id = chatRoomDataBaseReference.push().getKey();
                        //String time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime());

                        // Set the download URL to the message box, so that the user can send it to the database
                        //FriendlyMessageModel friendlyMessage = new FriendlyMessageModel(id,null, userId, downloadUrl.toString(),time);
                        //chatRoomDataBaseReference.child(id).setValue(friendlyMessage);
                    }
                }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {

                        Toast.makeText(AddNewFoodActivity.this, "Please Wait, Uploading...", Toast.LENGTH_LONG).show();
                    }
                });

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }

    }

    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if (requestCode == CropImage.PICK_IMAGE_PERMISSIONS_REQUEST_CODE) {
            if (mCropImageUri != null && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // required permissions granted, start crop image activity
                startCropImageActivity(mCropImageUri);
            } else {
                Toast.makeText(this, "Cancelling, required permissions are not granted", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void startCropImageActivity(Uri imageUri) {
        CropImage.activity(imageUri).setFixAspectRatio(true).setAspectRatio(1,1)
                .start(this);
    }

    private void setAddOnsList() {
        addonadapter = new FirebaseRecyclerAdapter<AddOnsModel, AddOnViewHolder>(AddOnsModel.class, R.layout.addoninfooditem_layout, AddOnViewHolder.class,
                databaseReferenceaddonslist
        ) {
            @Override
            protected void onDataChanged() {
                super.onDataChanged();
                recycler_addons.setAdapter(addonadapter);
            }
            @Override
            protected void populateViewHolder(final AddOnViewHolder viewHolder, final AddOnsModel model, final int position) {

                if(update.equals("1"))
                {
                    for (int i=0; i<addOnsList.size();i++)
                    {
                        if(addOnsList.get(i).getAddonitemname().equals(model.getAddonitemname()) &&
                                addOnsList.get(i).getAddonitemprice().equals(model.getAddonitemprice()))
                            viewHolder.cbaddonitem.setChecked(true);

                    }

                    /*if(addOnsList.contains(model)) {
                        Toast.makeText(AddNewFoodActivity.this, "Yesss", Toast.LENGTH_SHORT).show();
                        viewHolder.cbaddonitem.setChecked(true);
                    }
                    else
                        Toast.makeText(AddNewFoodActivity.this, "No", Toast.LENGTH_SHORT).show();*/
                }

                viewHolder.txtaddonitemname.setText(model.getAddonitemname());
                viewHolder.txtaddonitemprice.setText("₹" + model.getAddonitemprice());
                //add in list on cb clicked
                viewHolder.cbaddonitem.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if (isChecked) {
                            addOnsList.add(new AddOnsModel(model.getAddonitemname(),model.getAddonitemprice()));
                        }
                        else
                        {
                            for (int i=0; i<addOnsList.size();i++)
                            {
                                if(addOnsList.get(i).getAddonitemname().equals(model.getAddonitemname()) &&
                                        addOnsList.get(i).getAddonitemprice().equals(model.getAddonitemprice()))
                                    addOnsList.remove(i);
                            }
                        }
                    }
                });
            }
        };
    }

    private void setCheckBoxEdt(boolean cbq,boolean cbh, boolean cbf) {
        if(!cbq)
        {
            etquartername.setEnabled(false);
            etquarterprice.setEnabled(false);
            etquarterprice.setText("");
            etquarterdiscount.setEnabled(false);
            etquarterdiscount.setText("");
            qp="null";
        }
        else
        {
            etquartername.setEnabled(true);
            etquarterprice.setEnabled(true);
            etquarterdiscount.setEnabled(true);
        }
        if(!cbh)
        {
            ethalfname.setEnabled(false);
            ethalfprice.setEnabled(false);
            ethalfprice.setText("");
            ethalfdiscount.setEnabled(false);
            ethalfdiscount.setText("");
            hp="null";
        }
        else
        {
            ethalfname.setEnabled(true);
            ethalfprice.setEnabled(true);
            ethalfdiscount.setEnabled(true);
        }
        if(!cbf)
        {
            etfullname.setEnabled(false);
            etfullprice.setEnabled(false);
            etfullprice.setText("");
            etfulldiscount.setEnabled(false);
            etfulldiscount.setText("");
            fp="null";
        }
        else
        {
            etfullname.setEnabled(true);
            etfullprice.setEnabled(true);
            etfulldiscount.setEnabled(true);
        }
    }


}
