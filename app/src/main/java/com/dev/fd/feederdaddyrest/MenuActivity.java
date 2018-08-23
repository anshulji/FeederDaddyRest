package com.dev.fd.feederdaddyrest;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.andremion.counterfab.CounterFab;
import com.daimajia.slider.library.Animations.DescriptionAnimation;
import com.daimajia.slider.library.SliderLayout;
import com.daimajia.slider.library.SliderTypes.BaseSliderView;
import com.daimajia.slider.library.SliderTypes.TextSliderView;
import com.dev.fd.feederdaddyrest.Common.Common;
import com.dev.fd.feederdaddyrest.Database.Database;
import com.dev.fd.feederdaddyrest.Interface.ItemClickListener;
import com.dev.fd.feederdaddyrest.ViewHolder.MenuViewHolder;
import com.dev.fd.feederdaddyrest.model.Menu;
import com.dev.fd.feederdaddyrest.model.RestaurantBanner;
import com.dev.fd.feederdaddyrest.model.Token;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.mancj.materialsearchbar.MaterialSearchBar;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import info.hoang8f.widget.FButton;

public class MenuActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;

    FirebaseDatabase database;
    DatabaseReference menulist,restref;

    String RestaurantId="",city="";

    ImageView imggoback;
    ProgressBar progressBar;

    TextView text_count;

    FirebaseRecyclerAdapter<Menu,MenuViewHolder> adapter;

    //search menu adapter
    FirebaseRecyclerAdapter<Menu,MenuViewHolder> searchAdapter;
    List<String> SuggestList = new ArrayList<>();
    MaterialSearchBar materialSearchBar;

    //slider
    HashMap<String, String> image_list;
    SliderLayout sliderLayout;


    Uri saveUri;
    private  final int PICK_IMAGE_REQUEST =71;

    //add new menu layout
    FButton btnSelect, btnUpload;
    MaterialEditText edtName;
    RadioGroup radioGroup;
     RadioButton radioButton;

    FirebaseStorage storage;
    StorageReference storageReference;

    Menu newMenu;
    String totalmenuitems;

    DrawerLayout drawer;

    private SwitchCompat switchbtn;
    SharedPreferences sharedPreferences;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }

        progressBar = findViewById(R.id.progressbar);
        switchbtn = findViewById(R.id.switchbtn);

        //init firebase
        database = FirebaseDatabase.getInstance();
        restref = database.getReference(Common.getCity(this)).child("Restaurant").child(Common.getRestaurantId(this)).child("isopen");

        //set switchbtn
        restref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.getValue()!=null) {
                    if (dataSnapshot.getValue().toString().equals("1"))
                        switchbtn.setChecked(true);
                    else switchbtn.setChecked(false);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        sharedPreferences = getSharedPreferences("MyData",Context.MODE_PRIVATE);
        final SharedPreferences.Editor editor = sharedPreferences.edit();

        switchbtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked)
                {
                    restref.setValue("1");
                    editor.putString("isopen","1");
                    editor.commit();
                    Toast.makeText(MenuActivity.this, "Restaurant set open to take orders!", Toast.LENGTH_SHORT).show();
                }
                else {
                    editor.putString("isopen","0");
                    editor.commit();
                    restref.setValue("0");
                    Toast.makeText(MenuActivity.this, "Restaurant set closed!", Toast.LENGTH_SHORT).show();
                }
            }
        });


        storage=FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setBackgroundColor(Color.WHITE);

        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                MenuActivity.this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);

        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        //set name for user
        View headerview = navigationView.getHeaderView(0);


        recyclerView = findViewById(R.id.recycler_menulist);
        //recyclerView.setHasFixedSize(true);
        layoutManager =new GridLayoutManager(this,2);
        recyclerView.setLayoutManager(layoutManager);
        LayoutAnimationController controller = AnimationUtils.loadLayoutAnimation(recyclerView.getContext(),
                R.anim.layout_fall_down);
        recyclerView.setLayoutAnimation(controller);


        FloatingActionButton fab = findViewById(R.id.fab);
        text_count = findViewById(R.id.text_count);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDialog();
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





        //getting restaurantid passed by intent here
        if(getIntent()!=null) {

            SharedPreferences sharedPreferences = getSharedPreferences("MyData", Context.MODE_PRIVATE);
            RestaurantId = sharedPreferences.getString("restaurantid","N/A");
            city = sharedPreferences.getString("city","N/A");

            //RestaurantId = getIntent().getStringExtra("RestaurantId");
            menulist = database.getReference(city).child("Menus").child(RestaurantId);
            //Query query = database.getReference("Menus").child(RestaurantId).orderByKey();
            /*query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    totalfooditems = dataSnapshot.getKey();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });*/

            //Query query = foodlist.orderByKey().limitToLast(1);
            menulist.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for(DataSnapshot postSnapshot: dataSnapshot.getChildren())
                    {
                        totalmenuitems = postSnapshot.getKey();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

            //loading list
            adapter =new FirebaseRecyclerAdapter<Menu,MenuViewHolder>(Menu.class,R.layout.menu_item,MenuViewHolder.class,
                    menulist// find that foods with menuid==categoryid
            ) {
                @Override
                protected void onDataChanged() {
                    super.onDataChanged();

                    if (progressBar != null) {
                        progressBar.setVisibility(View.GONE);
                    }

                    loadmenulist(RestaurantId);


                }

                @Override
                protected void populateViewHolder(MenuViewHolder viewHolder, Menu model, int position) {
                    Typeface face = Typeface.createFromAsset(getAssets(),"NABILA.TTF");
                    viewHolder.txtmenuname.setTypeface(face);
                    viewHolder.txtmenuname.setText(model.getName());
                    Picasso.with(getBaseContext()).load(model.getImage()).into(viewHolder.imgmenuitem);
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

                    final Menu local = model;
                    viewHolder.setItemClickListener(new ItemClickListener() {
                        @Override
                        public void onClick(View view, int position, boolean isLongClick) {
                            // Toast.makeText(FoodList.this, ""+local.getName(), Toast.LENGTH_SHORT).show();

                            //start food details activity
                            Intent foodlist = new Intent(MenuActivity.this,FoodListActivity.class);
                            foodlist.putExtra("RestaurantId",RestaurantId);
                            foodlist.putExtra("MenuId",adapter.getRef(position).getKey());
                            startActivity(foodlist);
                        }
                    });

                }
            };

        }
        if(!RestaurantId.isEmpty() && RestaurantId!=null){

        }

        materialSearchBar = findViewById(R.id.searchBar);
        materialSearchBar.setHint("Enter Menu Item Name");
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

        //setup slider
        //need call this function after you init database
        setupSlider();

        updateToken(FirebaseInstanceId.getInstance().getToken());

        final SwipeRefreshLayout pulltorefreshhome = findViewById(R.id.pulltorefreshhome);

        pulltorefreshhome.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                startActivity(getIntent());
                finish();
            }
        });


    }

    private void updateToken(String token) {
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        DatabaseReference tokens = db.getReference("Tokens");

        SharedPreferences sharedPreferences = getSharedPreferences("MyData", Context.MODE_PRIVATE);
        String phone = sharedPreferences.getString("phone","N/A");
        String city = sharedPreferences.getString("city","N/A");

        Token data = new Token(token,city+"0");
        tokens.child(phone).setValue(data);
    }

    private void showDialog() {

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(MenuActivity.this);
        alertDialog.setTitle("Add New Category");
        alertDialog.setMessage("Please fill full information");

        LayoutInflater inflater = this.getLayoutInflater();
        View add_menu_lauout = inflater.inflate(R.layout.add_new_menu_layout,null);

        edtName = add_menu_lauout.findViewById(R.id.edtName);
        radioGroup=add_menu_lauout.findViewById(R.id.radioGroup);
        btnSelect = add_menu_lauout.findViewById(R.id.btnSelect);
        btnUpload =add_menu_lauout.findViewById(R.id.btnUpload);

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

        alertDialog.setView(add_menu_lauout);
        alertDialog.setIcon(R.drawable.ic_shopping_cart_black_24dp);

        alertDialog.setPositiveButton("Done", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();

                if(newMenu!=null){
                    menulist.child(totalmenuitems).setValue(newMenu);
                    //Snackbar.make(drawer,"New Category "+newCategory.getName()+" is added",Snackbar.LENGTH_SHORT).show();
                    Toast.makeText(MenuActivity.this, "New Category "+newMenu.getName()+" is added successfully !", Toast.LENGTH_SHORT).show();
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
            final StorageReference imagefolder = storageReference.child("images/restaurantmenuimages/"+city+"/"+Common.getRestaurantId(MenuActivity.this)+"/"+imageName);
            imagefolder.putFile(saveUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            mDialog.dismiss();
                            Toast.makeText(MenuActivity.this, "Uploaded !", Toast.LENGTH_SHORT).show();
                            imagefolder.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                            //        newFood = new Menu(edtName.getText().toString(),uri.toString());
                                    int ttmi;
                                    if(totalmenuitems==null)
                                        ttmi=0;
                                    else ttmi = Integer.parseInt(totalmenuitems);
                                    ttmi+=1;
                                    totalmenuitems = String.valueOf(ttmi);
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
                                    else
                                    {
                                        veg="1"; nonveg="1";
                                    }

                                    newMenu = new Menu(edtName.getText().toString(),uri.toString(),totalmenuitems,veg,nonveg,"5.0","0");
                                }
                            });

                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    mDialog.dismiss();
                    Toast.makeText(MenuActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();

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
    protected void onStop() {
        super.onStop();
        sliderLayout.stopAutoCycle();
    }

    private void setupSlider() {
        sliderLayout = findViewById(R.id.slidermenu);
        image_list = new HashMap<>();

        DatabaseReference menubannerref = database.getReference(city).child("RestaurantBanner").child(RestaurantId);

        menubannerref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for(DataSnapshot postSnapshot : dataSnapshot.getChildren())
                {
                    RestaurantBanner banner = postSnapshot.getValue(RestaurantBanner.class);

                    // we will concat string name and id
                    image_list.put(banner.getName()+"@#@"+banner.getRestaurantid()+"@#@"+banner.getMenuid()+"@#@"+banner.getFoodid(),banner.getImage());

                }
                for (String key: image_list.keySet())
                {
                    String[] keySplit = key.split("@#@");
                    String nameoffood = keySplit[0];
                    final String restaurantid = keySplit[1];
                    final String menuid = keySplit[2];
                    final String foodid = keySplit[3];

                    //create slider
                    TextSliderView textSliderView =new TextSliderView(getBaseContext());
                    textSliderView.description(nameoffood)
                            .image(image_list.get(key))
                            .setScaleType(BaseSliderView.ScaleType.CenterCrop)
                            .setOnSliderClickListener(new BaseSliderView.OnSliderClickListener() {
                                @Override
                                public void onSliderClick(BaseSliderView slider) {
                                    Intent intent = new Intent(MenuActivity.this,FoodDetails.class);
                                    //we will start food details activity by sending intent extras
                                    intent.putExtra("RestaurantId",restaurantid);
                                    intent.putExtra("MenuId",menuid);
                                    intent.putExtra("FoodId",foodid);
                                    startActivity(intent);

                                }
                            });

                    sliderLayout.addSlider(textSliderView);




                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        //sliderLayout.setPresetTransformer(SliderLayout.Transformer.Background2Foreground);
        sliderLayout.setPresetIndicator(SliderLayout.PresetIndicators.Right_Bottom);
        sliderLayout.setCustomAnimation(new DescriptionAnimation());
        sliderLayout.setDuration(4000);



    }

    private void startSearch(CharSequence text) {

        searchAdapter = new FirebaseRecyclerAdapter<Menu, MenuViewHolder>(
                Menu.class,
                R.layout.menu_item,
                MenuViewHolder.class,
                menulist.orderByChild("name").equalTo(text.toString())
        ) {
            @Override
            protected void onDataChanged() {
                super.onDataChanged();

                if (progressBar != null) {
                    progressBar.setVisibility(View.GONE);
                }

            }

            @Override
            protected void populateViewHolder(MenuViewHolder viewHolder, Menu model, int position) {
                Typeface face = Typeface.createFromAsset(getAssets(),"NABILA.TTF");
                viewHolder.txtmenuname.setTypeface(face);
                viewHolder.txtmenuname.setText(model.getName());
                Picasso.with(getBaseContext()).load(model.getImage()).into(viewHolder.imgmenuitem);
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


                final Menu local = model;
                viewHolder.setItemClickListener(new ItemClickListener() {
                    @Override

                    public void onClick(View view, int position, boolean isLongClick) {
                        // Toast.makeText(FoodList.this, ""+local.getName(), Toast.LENGTH_SHORT).show();

                        //start food details activity
                        Intent foodlist = new Intent(MenuActivity.this,FoodListActivity.class);
                        foodlist.putExtra("RestaurantId",RestaurantId);
                        foodlist.putExtra("MenuId",adapter.getRef(position).getKey());
                        startActivity(foodlist);
                    }
                });
            }
        };
        recyclerView.setAdapter(searchAdapter);


    }



    private void loadSuggest() {
        menulist.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for(DataSnapshot postSnaphot : dataSnapshot.getChildren())
                        {
                            Menu item = postSnaphot.getValue(Menu.class);
                            SuggestList.add(item.getName());

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void loadmenulist(String categoryId) {

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

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if(item.getTitle().equals(Common.UPDATE)){
            showUpdateDialog(adapter.getRef(item.getOrder()).getKey(),adapter.getItem(item.getOrder()));
        }
        else
        {
            menulist.child(adapter.getRef(item.getOrder()).getKey()).setValue(null);
        }



        return super.onContextItemSelected(item);


    }

    private void showUpdateDialog(final String key, final Menu item) {

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(MenuActivity.this);
        alertDialog.setTitle("Update Category");
        alertDialog.setMessage("Please fill full information");

        LayoutInflater inflater = this.getLayoutInflater();
        View add_menu_lauout = inflater.inflate(R.layout.add_new_menu_layout,null);

        edtName = add_menu_lauout.findViewById(R.id.edtName);
        radioGroup=add_menu_lauout.findViewById(R.id.radioGroup);
        btnSelect = add_menu_lauout.findViewById(R.id.btnSelect);
        btnUpload =add_menu_lauout.findViewById(R.id.btnUpload);

        edtName.setText(item.getName());
        String veg = item.getVeg();
        String nonveg = item.getNonveg();
        if(veg.equals("1") && nonveg.equals("1"))
        {
            radioButton = add_menu_lauout.findViewById(R.id.radiovegnon);
            radioButton.setChecked(true);
        }
        else if(veg.equals("1"))
        {
            radioButton = add_menu_lauout.findViewById(R.id.radioveg);
            radioButton.setChecked(true);
        }
        else
        {
            radioButton = add_menu_lauout.findViewById(R.id.radiononveg);
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

        alertDialog.setView(add_menu_lauout);
        alertDialog.setIcon(R.drawable.ic_shopping_cart_black_24dp);

        alertDialog.setPositiveButton("Done", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();

                item.setName(edtName.getText().toString());
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
                else
                {
                    veg="1"; nonveg="1";
                }
                item.setVeg(veg);
                item.setNonveg(nonveg);
                menulist.child(key).setValue(item);
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

    private void changeImage(final Menu item) {



        if(saveUri!=null)
        {
            final ProgressDialog mDialog = new ProgressDialog(this);
            mDialog.setMessage("Uploading...");
            mDialog.show();

            String imageName = UUID.randomUUID().toString();
            final StorageReference imagefolder = storageReference.child("images/restaurantmenuimages/"+city+"/"+Common.getRestaurantId(MenuActivity.this)+"/"+imageName);
            imagefolder.putFile(saveUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            mDialog.dismiss();
                            Toast.makeText(MenuActivity.this, "Uploaded !", Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(MenuActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();

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

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_profile) {
            Intent i = new Intent(MenuActivity.this,ProfileActivity.class);
            startActivity(i);
        } else if (id == R.id.nav_currentorders) {
            Intent i = new Intent(MenuActivity.this,Orders.class);
            i.putExtra("currentorder","1");
            startActivity(i);

        }
        else if (id == R.id.nav_pastorders) {
            Intent i = new Intent(MenuActivity.this,Orders.class);
            i.putExtra("currentorder","0");
            startActivity(i);

        }
        else if (id == R.id.nav_addons) {
            Intent i = new Intent(MenuActivity.this,AddOnsActivity.class);
            startActivity(i);
        }
        else if (id == R.id.nav_deletebanner) {
            Intent i = new Intent(MenuActivity.this,DeleteBannerActivity.class);
            startActivity(i);
        }
        drawer.closeDrawer(GravityCompat.START);

        return true;
    }
}