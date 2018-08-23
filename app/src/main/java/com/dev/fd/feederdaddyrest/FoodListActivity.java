package com.dev.fd.feederdaddyrest;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.dev.fd.feederdaddyrest.Common.Common;
import com.dev.fd.feederdaddyrest.Database.Database;
import com.dev.fd.feederdaddyrest.Interface.ItemClickListener;
import com.dev.fd.feederdaddyrest.ViewHolder.FoodViewHolder;
import com.dev.fd.feederdaddyrest.model.Food;
import com.dev.fd.feederdaddyrest.model.Menu;
import com.dev.fd.feederdaddyrest.model.RestaurantBanner;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.mancj.materialsearchbar.MaterialSearchBar;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import info.hoang8f.widget.FButton;

public class FoodListActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;

    FirebaseDatabase database;
    DatabaseReference foodlist;

    ImageView imggoback;
    ProgressBar progressBar;

    String MenuId="",RestaurantId="";

    TextView text_count;
    FloatingActionButton fab;

    FirebaseRecyclerAdapter<Food,FoodViewHolder> adapter;

    //search food adapter
    FirebaseRecyclerAdapter<Food,FoodViewHolder> searchAdapter;
    List<String> SuggestList = new ArrayList<>();
    MaterialSearchBar materialSearchBar;

    Uri saveUri;
    private  final int PICK_IMAGE_REQUEST =71;

    //add new menu layout
    FButton btnSelect, btnUpload;
    MaterialEditText edtName,edtDescription,edtPrice,edtDiscount;
    RadioGroup radioGroup;
    RadioButton radioButton;

    FirebaseStorage storage;
    StorageReference storageReference;

    Food newFood;
    String totalfooditems="0",city="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_list);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
        imggoback = findViewById(R.id.imggoback);
        imggoback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        progressBar=findViewById(R.id.progressbar);

        SharedPreferences sharedPreferences = getSharedPreferences("MyData", Context.MODE_PRIVATE);
        city = sharedPreferences.getString("city","N/A");


        //init firebase
        database = FirebaseDatabase.getInstance();
        storage=FirebaseStorage.getInstance();
        storageReference = storage.getReference();


        recyclerView = findViewById(R.id.recycler_food);
        //recyclerView.setHasFixedSize(true);
        //layoutManager =new LinearLayoutManager(this);
        //recyclerView.setLayoutManager(layoutManager);
        layoutManager =new GridLayoutManager(this,2);
        //layoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);

        /*RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapter);*/

        LayoutAnimationController controller = AnimationUtils.loadLayoutAnimation(recyclerView.getContext(),
                R.anim.layout_fall_down);
        recyclerView.setLayoutAnimation(controller);

        //getting categoryid passed by intent here
        if(getIntent()!=null) {
            MenuId = getIntent().getStringExtra("MenuId");
            RestaurantId = getIntent().getStringExtra("RestaurantId");
            foodlist = database.getReference(city).child("Foods").child(RestaurantId).child(MenuId);

            //Query query = foodlist.orderByKey().limitToLast(1);
            foodlist.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for(DataSnapshot postSnapshot: dataSnapshot.getChildren())
                    {
                     totalfooditems = postSnapshot.getKey();
                    }
                    //Toast.makeText(FoodListActivity.this, ""+totalfooditems, Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

            //load foof list
            adapter =new FirebaseRecyclerAdapter<Food, FoodViewHolder>(Food.class,R.layout.food_item,FoodViewHolder.class,
                    foodlist // find that foods with menuid==categoryid
            ) {
                @Override
                protected void onDataChanged() {
                    super.onDataChanged();

                    if (progressBar != null) {
                        progressBar.setVisibility(View.GONE);
                    }
                    loadfoodlist();

                }

                @Override
                protected void populateViewHolder(final FoodViewHolder viewHolder, final Food model, int position) {
                    Typeface face = Typeface.createFromAsset(getAssets(),"NABILA.TTF");
                    viewHolder.food_name.setTypeface(face);
                    viewHolder.food_name.setText(model.getName());
                    Picasso.with(getBaseContext()).load(model.getImage()).into(viewHolder.food_image);
                    Double drating = Double.parseDouble(model.getRating());
                    String rat = String.format("%.1f",drating);
                    viewHolder.txtrating.setText(rat);
                    viewHolder.txttotalrates.setText("("+model.getTotalrates()+")");
                    viewHolder.ratingBar.setRating(Float.parseFloat(model.getRating()));

                    foodlist.child(adapter.getRef(position).getKey()).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if(dataSnapshot.child("hidefood").getValue()==null)
                            {
                                viewHolder.food_price.setText("₹ "+model.getFullprice());
                                int color = R.color.transparent;
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                    viewHolder.food_image.setForeground(new ColorDrawable(ContextCompat.getColor(getBaseContext(), color)));
                                }
                            }
                            else
                            {
                                viewHolder.food_price.setText("Out of Stock");

                                int color = R.color.black_filter;
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                    viewHolder.food_image.setForeground(new ColorDrawable(ContextCompat.getColor(getBaseContext(), color)));
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

                    if(model.getVeg().equals("0"))
                    {
                        viewHolder.imgveg.setVisibility(View.GONE);
                    }
                    else if(model.getNonveg().equals("0"))
                    {
                        viewHolder.imgnonveg.setVisibility(View.GONE);
                    }

                    final Food local = model;
                    viewHolder.setItemClickListener(new ItemClickListener() {
                        @Override
                        public void onClick(View view, int position, boolean isLongClick) {
                            // Toast.makeText(FoodList.this, ""+local.getName(), Toast.LENGTH_SHORT).show();

                            //start food details activity
                            Intent fooddetail = new Intent(FoodListActivity.this,FoodDetails.class);
                            fooddetail.putExtra("FoodId",adapter.getRef(position).getKey());
                            fooddetail.putExtra("RestaurantId",RestaurantId);
                            fooddetail.putExtra("MenuId", MenuId);
                            startActivity(fooddetail);
                        }
                    });

                    //totalfooditems = adapter.getRef(position).getKey();

                }
            };

        }
        if(!MenuId.isEmpty() && MenuId!=null){

            //loadfoodlist(MenuId);
        }


        fab =  findViewById(R.id.fab);
        text_count = findViewById(R.id.text_count);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(FoodListActivity.this,AddNewFoodActivity.class);
                intent.putExtra("city",city);
                intent.putExtra("menuid",MenuId);
                intent.putExtra("restaurantid",RestaurantId);
                int ttmi = Integer.parseInt(totalfooditems);
                ttmi+=1;
                totalfooditems = String.valueOf(ttmi);
                intent.putExtra("totalitems",totalfooditems);
                intent.putExtra("update","0");
                startActivity(intent);

                //showDialog();
                //Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG).setAction("Action", null).show();
            }
        });
        int ct = new Database(this).getCountCart();
        String sct = String.valueOf(ct);
        if(ct==0)
            text_count.setVisibility(View.GONE);
        else {
            text_count.setVisibility(View.VISIBLE);
            text_count.setText(sct);
        }


        materialSearchBar = findViewById(R.id.searchBar);
        materialSearchBar.setHint("Enter Food Name");
        loadSuggest();

        materialSearchBar.setLastSuggestions(SuggestList);
        materialSearchBar.setCardViewElevation(0);
        materialSearchBar.addTextChangeListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                List<String> suggest = new ArrayList<>();
                for(String search: SuggestList)
                {
                    if(search.toLowerCase().contains(materialSearchBar.getText().toLowerCase())){
                        suggest.add(search);
                    }
                }
                materialSearchBar.setLastSuggestions(suggest);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        materialSearchBar.setOnSearchActionListener(new MaterialSearchBar.OnSearchActionListener() {
            @Override
            public void onSearchStateChanged(boolean enabled) {
                //when search bar is close
                //restore original adapter
                if(!enabled){
                    recyclerView.setAdapter(adapter);
                }
            }

            @Override
            public void onSearchConfirmed(CharSequence text) {
                //when search finish
                //show result of search adapter
                startSearch(text);
            }

            @Override
            public void onButtonClicked(int buttonCode) {

            }
        });

        final SwipeRefreshLayout pulltorefreshhome = findViewById(R.id.pulltorefreshhome);

        pulltorefreshhome.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                startActivity(getIntent());
                finish();
            }
        });


    }

    private void startSearch(CharSequence text) {


        searchAdapter = new FirebaseRecyclerAdapter<Food, FoodViewHolder>(
                Food.class,
                R.layout.food_item,
                FoodViewHolder.class,
                foodlist.orderByChild("name").equalTo(text.toString())
        ) {

            @Override
            protected void onDataChanged() {
                super.onDataChanged();

                if (progressBar != null) {
                    progressBar.setVisibility(View.GONE);
                }

            }

            @Override
            protected void populateViewHolder(FoodViewHolder viewHolder, Food model, int position) {
                Typeface face = Typeface.createFromAsset(getAssets(),"NABILA.TTF");
                viewHolder.food_name.setTypeface(face);

                viewHolder.food_name.setText(model.getName());
                viewHolder.food_price.setText("₹ "+model.getFullprice());
                Picasso.with(getBaseContext()).load(model.getImage()).into(viewHolder.food_image);

                Double drating = Double.parseDouble(model.getRating());
                String rat = String.format("%.1f",drating);
                viewHolder.txtrating.setText(rat);
                viewHolder.txttotalrates.setText("("+model.getTotalrates()+")");
                viewHolder.ratingBar.setRating(Float.parseFloat(model.getRating()));



                if(model.getVeg().equals("0"))
                {
                    viewHolder.imgveg.setVisibility(View.GONE);
                }
                else if(model.getNonveg().equals("0"))
                {
                    viewHolder.imgnonveg.setVisibility(View.GONE);
                }

                final Food local = model;
                viewHolder.setItemClickListener(new ItemClickListener() {
                    @Override

                    public void onClick(View view, int position, boolean isLongClick) {
                        // Toast.makeText(FoodList.this, ""+local.getName(), Toast.LENGTH_SHORT).show();

                        //start food details activity
                        Intent fooddetail = new Intent(FoodListActivity.this,FoodDetails.class);
                        fooddetail.putExtra("FoodId",adapter.getRef(position).getKey());
                        fooddetail.putExtra("RestaurantId",RestaurantId);
                        fooddetail.putExtra("MenuId", MenuId);
                        startActivity(fooddetail);
                    }
                });
            }
        };
        recyclerView.setAdapter(searchAdapter);

    }



    private void loadSuggest() {
        foodlist.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for(DataSnapshot postSnaphot : dataSnapshot.getChildren())
                        {
                            Food item = postSnaphot.getValue(Food.class);
                            SuggestList.add(item.getName());

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void loadfoodlist() {

        recyclerView.setAdapter(adapter);

        //animation
        recyclerView.getAdapter().notifyDataSetChanged();
        recyclerView.scheduleLayoutAnimation();
    }

    @Override
    protected void onResume() {
        super.onResume();
        int ct = new Database(this).getCountCart();
        String sct = String.valueOf(ct);
        if(ct==0)
            text_count.setVisibility(View.GONE);
        else {
            text_count.setVisibility(View.VISIBLE);
            text_count.setText(sct);
        }
        //animation
        if(recyclerView!=null  && recyclerView.getAdapter()!=null) {
            recyclerView.getAdapter().notifyDataSetChanged();
            recyclerView.scheduleLayoutAnimation();
        }
    }

    private void showDialog() {

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(FoodListActivity.this);
        alertDialog.setTitle("Add New Food");
        //alertDialog.setMessage("Please fill full information");

        LayoutInflater inflater = this.getLayoutInflater();
        View add_new_food_layout = inflater.inflate(R.layout.add_new_food_layout,null);

        radioGroup=add_new_food_layout.findViewById(R.id.radioGroup);
        edtName = add_new_food_layout.findViewById(R.id.edtName);
        edtDiscount = add_new_food_layout.findViewById(R.id.edtDiscount);
        edtPrice = add_new_food_layout.findViewById(R.id.edtPrice);
        edtDescription = add_new_food_layout.findViewById(R.id.edtDescription);
        btnSelect = add_new_food_layout.findViewById(R.id.btnSelect);
        btnUpload =add_new_food_layout.findViewById(R.id.btnUpload);

        btnSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chooseImage();
            }
        });

        btnUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                uploadImage();
            }
        });

        alertDialog.setView(add_new_food_layout);
        alertDialog.setIcon(R.drawable.ic_shopping_cart_black_24dp);

        alertDialog.setPositiveButton("Done", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();

                if(newFood !=null){
                    foodlist.child(totalfooditems).setValue(newFood);
                    //Snackbar.make(drawer,"New Category "+newCategory.getName()+" is added",Snackbar.LENGTH_SHORT).show();
                    Toast.makeText(FoodListActivity.this, "New Food "+ newFood.getName()+" is added successfully !", Toast.LENGTH_SHORT).show();
                }


            }
        });
        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        alertDialog.show();

    }

    private void uploadImage() {

        if(saveUri!=null)
        {
            final ProgressDialog mDialog = new ProgressDialog(this);
            mDialog.setMessage("Uploading...");
            mDialog.show();

            String imageName = UUID.randomUUID().toString();
            final StorageReference imagefolder = storageReference.child("images/"+imageName);
            imagefolder.putFile(saveUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            mDialog.dismiss();
                            Toast.makeText(FoodListActivity.this, "Uploaded !", Toast.LENGTH_SHORT).show();
                            imagefolder.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    //        newFood = new Menu(edtName.getText().toString(),uri.toString());
                                    int ttmi = Integer.parseInt(totalfooditems);
                                    ttmi+=1;
                                    totalfooditems = String.valueOf(ttmi);
                                    int selectedId=radioGroup.getCheckedRadioButtonId();
                                    String veg="0",nonveg="0";
                                    if(selectedId==R.id.radioveg)
                                    {
                                        veg="1"; nonveg="0";
                                    }
                                    else if(selectedId==R.id.radiononveg)
                                    {
                                        veg="0"; nonveg="1";
                                    }

                                    //newFood = new Food(edtName.getText().toString(),uri.toString(),edtDescription.getText().toString(),edtPrice.getText().toString(),veg,nonveg,"5.0","0");
                                }
                            });

                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    mDialog.dismiss();
                    Toast.makeText(FoodListActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();

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

    private void chooseImage() {
        Intent intent = new Intent(Intent.ACTION_PICK,android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        //intent.setType("images/*");
        //intent.setAction(Intent.ACTION_GET_CONTENT);
        //startActivityForResult(Intent.createChooser(intent,"Select Picture"),PICK_IMAGE_REQUEST);
        startActivityForResult(intent,PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == PICK_IMAGE_REQUEST && resultCode==RESULT_OK && data!=null && data.getData()!=null){
            saveUri = data.getData();
            btnSelect.setText("Image Selected !");
            btnUpload.setVisibility(View.VISIBLE);

        }
    }

    @Override
    public boolean onContextItemSelected(final MenuItem item) {
        if(item.getTitle().equals(Common.UPDATE)){
            //showUpdateDialog(adapter.getRef(item.getOrder()).getKey(),adapter.getItem(item.getOrder()));
            Intent intent = new Intent(FoodListActivity.this,AddNewFoodActivity.class);
            intent.putExtra("city",city);
            intent.putExtra("menuid",MenuId);
            intent.putExtra("restaurantid",RestaurantId);
            intent.putExtra("totalitems",adapter.getRef(item.getOrder()).getKey());
            intent.putExtra("update","1");
            startActivity(intent);

        }
        else if(item.getTitle().equals(Common.DELETE))
        {
            foodlist.child(adapter.getRef(item.getOrder()).getKey()).setValue(null);
        }
        else if(item.getTitle().equals(Common.SETASBANNER))
        {
            final DatabaseReference dbbannerref = database.getReference(city).child("RestaurantBanner").child(RestaurantId);

            AlertDialog.Builder alertDialog = new AlertDialog.Builder(FoodListActivity.this);
            alertDialog.setTitle("Set Food as Banner");
            alertDialog.setMessage("Please fill full information");

            LayoutInflater inflater = this.getLayoutInflater();
            View add_new_food_layout = inflater.inflate(R.layout.add_new_banner_layout,null);

            final EditText edtBannerName = add_new_food_layout.findViewById(R.id.edttitleofbanner);

            alertDialog.setView(add_new_food_layout);
            alertDialog.setIcon(R.drawable.ic_shopping_cart_black_24dp);

            alertDialog.setPositiveButton("SET", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    RestaurantBanner restaurantBanner = new RestaurantBanner(RestaurantId,MenuId,adapter.getRef(item.getOrder()).getKey(),edtBannerName.getText().toString(),adapter.getItem(item.getOrder()).getImage());
                    dbbannerref.push().setValue(restaurantBanner);
                    dialogInterface.dismiss();
                    Toast.makeText(FoodListActivity.this, "Banner added successfully!", Toast.LENGTH_SHORT).show();
                }
            });
            alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                }
            });
            alertDialog.show();


        }
        else
        {
            final DatabaseReference dbref = database.getReference(city).child("Foods").child(RestaurantId).child(MenuId).child(adapter.getRef(item.getOrder()).getKey());
            dbref.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(dataSnapshot.child("hidefood").getValue()==null)
                    {
                        dbref.child("hidefood").setValue("1");
                    }
                    else
                        dbref.child("hidefood").setValue(null);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
        return super.onContextItemSelected(item);
    }

    private void showUpdateDialog(final String key, final Food item) {

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(FoodListActivity.this);
        alertDialog.setTitle("Update Food");
        alertDialog.setMessage("Please fill full information");

        LayoutInflater inflater = this.getLayoutInflater();
        View add_new_food_layout = inflater.inflate(R.layout.add_new_food_layout,null);

        edtName = add_new_food_layout.findViewById(R.id.edtName);
        edtDiscount = add_new_food_layout.findViewById(R.id.edtDiscount);
        edtPrice = add_new_food_layout.findViewById(R.id.edtPrice);
        edtDescription = add_new_food_layout.findViewById(R.id.edtDescription);
        btnSelect = add_new_food_layout.findViewById(R.id.btnSelect);
        btnUpload =add_new_food_layout.findViewById(R.id.btnUpload);

        edtName.setText(item.getName());
        edtDescription.setText(item.getDescription());
        edtPrice.setText(item.getFullprice());

        String veg = item.getVeg();
        String nonveg = item.getNonveg();
        if(nonveg.equals("1"))
        {
            radioButton = add_new_food_layout.findViewById(R.id.radiononveg);
            radioButton.setChecked(true);
        }
        else if(veg.equals("1"))
        {
            radioButton = add_new_food_layout.findViewById(R.id.radioveg);
            radioButton.setChecked(true);
        }


        btnSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chooseImage();
            }
        });

        btnUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changeImage(item);
            }
        });

        alertDialog.setView(add_new_food_layout);
        alertDialog.setIcon(R.drawable.ic_shopping_cart_black_24dp);

        alertDialog.setPositiveButton("DONE", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();

                item.setName(edtName.getText().toString());
                item.setDescription(edtDescription.getText().toString());
                //item.setDiscount(edtDiscount.getText().toString());
                //item.setMenuId(CategoryId);
                item.setFullprice(edtPrice.getText().toString());

                int selectedId=radioGroup.getCheckedRadioButtonId();
                String veg="0",nonveg="0";
                if(selectedId==R.id.radioveg)
                {
                    veg="1"; nonveg="0";
                }
                else if(selectedId==R.id.radiononveg)
                {
                    veg="0"; nonveg="1";
                }
                item.setVeg(veg);
                item.setNonveg(nonveg);


                foodlist.child(key).setValue(item);
            }
        });
        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        alertDialog.show();
    }

    private void changeImage(final Food item) {



        if(saveUri!=null)
        {
            final ProgressDialog mDialog = new ProgressDialog(this);
            mDialog.setMessage("Uploading...");
            mDialog.show();

            String imageName = UUID.randomUUID().toString();
            final StorageReference imagefolder = storageReference.child("images/"+imageName);
            imagefolder.putFile(saveUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            mDialog.dismiss();
                            Toast.makeText(FoodListActivity.this, "Uploaded !", Toast.LENGTH_SHORT).show();
                            imagefolder.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    //newCategory = new Category(edtName.getText().toString(),uri.toString());
                                    item.setImage(uri.toString());
                                }
                            });

                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    mDialog.dismiss();
                    Toast.makeText(FoodListActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();

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
