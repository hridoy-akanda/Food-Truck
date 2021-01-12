package edmt.dev.androideatitserver;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.squareup.picasso.Picasso;

import java.util.UUID;

import edmt.dev.androideatitserver.Common.Common;
import edmt.dev.androideatitserver.Interface.ItemClickListener;
import edmt.dev.androideatitserver.Model.Category;
import edmt.dev.androideatitserver.Model.Food;
import edmt.dev.androideatitserver.ViewHolder.FoodViewHolder;
import info.hoang8f.widget.FButton;

/**
 * Created by Minhaz on 2/25/2018.
 */

public class FoodList extends AppCompatActivity{

    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;

    RelativeLayout rootLayout;


    FloatingActionButton fab;
    FirebaseDatabase db;
    DatabaseReference foodList;
    FirebaseStorage storage;
    StorageReference storageReference;

    String categoryId="";
    FirebaseRecyclerAdapter<Food,FoodViewHolder>adapter;

    //Add New Food
    MaterialEditText edtName,edtDescription, edtPrice, edtDiscount;
    FButton btnSelect,btnUpload;

    Food newFood;
    Uri saveUri;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_list);

        Log.e("acticity","food list");

        //Firebase
        db=FirebaseDatabase.getInstance();
        foodList=db.getReference("Food");
        storage=FirebaseStorage.getInstance();
        storageReference=storage.getReference();

        //init
        recyclerView=(RecyclerView)findViewById(R.id.recycler_food);
        recyclerView.setHasFixedSize(true);
        layoutManager=new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        rootLayout =(RelativeLayout)findViewById(R.id.rootLayout);

        fab=(FloatingActionButton)findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Code later
                showAddFoodDialog();
            }
        });

        if(getIntent()!=null)
            categoryId=getIntent().getStringExtra("CategoryId");
        if(!categoryId.isEmpty())
            loadListFood(categoryId);
    }

    private void showAddFoodDialog() {

        //Copy code from home activity
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(FoodList.this);
        alertDialog.setTitle("Add new Food");
        alertDialog.setMessage("Please Fill Full Information");

        LayoutInflater inflater = this.getLayoutInflater();
        View add_menu_layout= inflater.inflate(R.layout.add_new_food_layout,null);

        edtName = add_menu_layout.findViewById(R.id.edtName);
        edtDescription = add_menu_layout.findViewById(R.id.edtDescription);
        edtPrice = add_menu_layout.findViewById(R.id.edtPrice);
        edtDiscount = add_menu_layout.findViewById(R.id.edtDiscount);

        btnSelect = add_menu_layout.findViewById(R.id.btnSelect);
        btnUpload = add_menu_layout.findViewById(R.id.btnUpload);

        //Event for button
        btnSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseImage(); //Copy from Home activity
            }
        });
        btnUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadImage(); //Copy from Home activity
            }
        });


        alertDialog.setView(add_menu_layout);
        alertDialog.setIcon(R.drawable.ic_shopping_cart_black_24dp);

        //set button
       /* alertDialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            //public DialogInterface dialogInterface;

            @Override
            public void onClick(DialogInterface dialogInterface, int which) {
                dialogInterface.dismiss();
                //Here create new category
                if(newFood!=null){
                    foodList.push().setValue(newFood);
                    Snackbar.make(rootLayout,"new category"+newFood.getName()+"was added",Snackbar.LENGTH_SHORT )
                            .show();
                }
            }
        });
        alertDialog.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            //public DialogInterface dialogInterface;

            @Override
            public void onClick(DialogInterface dialogInterface, int which) {
                dialogInterface.dismiss();
            }
        });*/
        alertDialog.show();
    }

    private void uploadImage() {
        if (saveUri != null) {
            final ProgressDialog mDialog = new ProgressDialog(this);
            mDialog.setMessage("Uploading");
            mDialog.show();

            String imageName = UUID.randomUUID().toString();
            final StorageReference imageFolder = storageReference.child("images/" + imageName);
            imageFolder.putFile(saveUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            mDialog.dismiss();
                            Toast.makeText(FoodList.this, "Uploading..!", Toast.LENGTH_SHORT).show();
                            imageFolder.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    //set valuye for new category if image uploaded and we can get download link
                                    newFood = new Food();
                                    newFood.setName(edtName.getText().toString());
                                    newFood.setDescription(edtDescription.getText().toString());
                                    newFood.setPrice(edtPrice.getText().toString());
                                    newFood.setDiscount(edtDiscount.getText().toString());
                                    newFood.setMenuId(categoryId);
                                    newFood.setImage(uri.toString());

                                    if(newFood!=null){
                                        foodList.push().setValue(newFood);
                                        Snackbar.make(rootLayout,"new category"+newFood.getName()+"was added",Snackbar.LENGTH_SHORT )
                                                .show();
                                    }
                                }

                            });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            mDialog.dismiss();
                            Toast.makeText(FoodList.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            double progress = (100.0* taskSnapshot.getBytesTransferred()/taskSnapshot.getTotalByteCount());
                            mDialog.setMessage(("Uploaded"+progress+"%"));
                        }
                    });

        }

    }
    private void chooseImage() {
        Intent intent =new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent,"Select Picture"), Common.PICK_IMAGE_REQUEST);
    }

    private void loadListFood(final String categoryId)
    {



        adapter= new FirebaseRecyclerAdapter<Food, FoodViewHolder>(
                Food.class,
                R.layout.food_item,
                FoodViewHolder.class,
                foodList.orderByChild("menuId").equalTo(categoryId)


        ) {


            @Override
            protected void populateViewHolder(FoodViewHolder viewHolder, Food model, int position) {
                Log.e("FoodList","Loading For "+categoryId+" total food "+adapter.getItemCount());
                viewHolder.food_name.setText(model.getName());
                Picasso.with(getBaseContext())
                        .load(model.getImage())
                        .into(viewHolder.food_image);
                viewHolder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onClick(View view, int position, boolean islongClick) {
                        //Code later

                    }
                });
            }
        };
        Log.e("FoodList","Loading For "+categoryId+" total food "+adapter.getItemCount());
        adapter.notifyDataSetChanged();
        recyclerView.setAdapter(adapter);
    }


    //Press ctrl+O

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==Common.PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null)
        {
            saveUri = data.getData();
            btnSelect.setText("Image Selected!");
        }

    }


    //Press ctrl+O

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if(item.getTitle().equals(Common.UPDATE)){
            showUpdateFoodDialog(adapter.getRef(item.getOrder()).getKey(),adapter.getItem(item.getOrder()));

        }else if(item.getTitle().equals(Common.DELETE)){
            deleteFood(adapter.getRef(item.getOrder()).getKey());
        }
        return super.onContextItemSelected(item);
    }

    private void deleteFood(String key) {
        foodList.child(key).removeValue();
    }

    private void showUpdateFoodDialog(final String key, final Food item) {

        //Copy code from ShowCreateFood
        //Copy code from home activity
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(FoodList.this);
        alertDialog.setTitle("Edit Food");
        alertDialog.setMessage("Please Fill Full Information");

        LayoutInflater inflater = this.getLayoutInflater();
        View add_menu_layout= inflater.inflate(R.layout.add_new_food_layout,null);

        edtName = add_menu_layout.findViewById(R.id.edtName);
        edtDescription = add_menu_layout.findViewById(R.id.edtDescription);
        edtPrice = add_menu_layout.findViewById(R.id.edtPrice);
        edtDiscount = add_menu_layout.findViewById(R.id.edtDiscount);

        //Set Default value for view
        edtName.setText(item.getName());
        edtDiscount.setText(item.getDiscount());
        edtPrice.setText(item.getPrice());
        edtDescription.setText(item.getDescription());

        btnSelect = add_menu_layout.findViewById(R.id.btnSelect);
        btnUpload = add_menu_layout.findViewById(R.id.btnUpload);

        //Event for button
        btnSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseImage(); //Copy from Home activity
            }
        });
        btnUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeImage(item); //Copy from Home activity
            }
        });


        alertDialog.setView(add_menu_layout);
        alertDialog.setIcon(R.drawable.ic_shopping_cart_black_24dp);

        //set button
        /*alertDialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            //public DialogInterface dialogInterface;

            @Override
            public void onClick(DialogInterface dialogInterface, int which) {
                dialogInterface.dismiss();
                //Here create new category

                    //Update Information
                    item.setName(edtName.getText().toString());
                    item.setPrice(edtPrice.getText().toString());
                    item.setDiscount(edtDiscount.getText().toString());
                    item.setDescription(edtDescription.getText().toString());

                    foodList.child(key).setValue(item); //Fix here


                    Snackbar.make(rootLayout,"Food"+item.getName()+"was edited",Snackbar.LENGTH_SHORT )
                            .show();

            }
        });*/
        /*alertDialog.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            //public DialogInterface dialogInterface;

            @Override
            public void onClick(DialogInterface dialogInterface, int which) {
                dialogInterface.dismiss();
            }
        });*/
        alertDialog.show();
    }

    private void changeImage(final Food item) {
        if (saveUri != null) {
            final ProgressDialog mDialog = new ProgressDialog(this);
            mDialog.setMessage("Uploaded");
            mDialog.show();

            String imageName = UUID.randomUUID().toString();
            final StorageReference imageFolder = storageReference.child("images/" + imageName);
            imageFolder.putFile(saveUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            mDialog.dismiss();
                            Toast.makeText(FoodList.this, "Upload Complete", Toast.LENGTH_SHORT).show();
                            imageFolder.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    //set valuye for new category if image uploaded and we can get download link
                                    item.setImage(uri.toString());

                                }

                            });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            mDialog.dismiss();
                            Toast.makeText(FoodList.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            double progress = (100.0* taskSnapshot.getBytesTransferred()/taskSnapshot.getTotalByteCount());
                            mDialog.setMessage(("Uploading"+progress+"%"));
                        }
                    });

        }

    }
}
