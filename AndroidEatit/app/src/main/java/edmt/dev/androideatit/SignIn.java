package edmt.dev.androideatit;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rengwuxian.materialedittext.MaterialEditText;

import edmt.dev.androideatit.Common.Common;
import edmt.dev.androideatit.Model.User;

public class SignIn extends AppCompatActivity {
    EditText edtPhone,edtPassword;
    Button btnSignIn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        edtPassword=(MaterialEditText)findViewById(R.id.edtPassword);
        edtPhone=(MaterialEditText)findViewById(R.id.edtPhone);
        btnSignIn=(Button)findViewById(R.id.btnSignIn);


        //Init FIrebase
        FirebaseApp.initializeApp(this);
        final FirebaseDatabase database=FirebaseDatabase.getInstance();
        final DatabaseReference table_user=database.getReference("User");

        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final ProgressDialog mDIalog= new ProgressDialog(SignIn.this);
                mDIalog.setMessage("Please waiting");
                mDIalog.show();
                table_user.addValueEventListener(new ValueEventListener(){

                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        //Check if User doesn't exist
                        if (dataSnapshot.child(edtPhone.getText().toString()).exists()) {
                            //Get  User info
                            mDIalog.dismiss();
                            User user = dataSnapshot.child(edtPhone.getText().toString()).getValue(User.class);
                            user.setPhone(edtPhone.getText().toString());//Set phone
                            if (user.getPassword().equals(edtPassword.getText().toString())) {
                                Intent homeIntent=new Intent(SignIn.this,Home.class);
                                Common.currentUser=user;
                                startActivity(homeIntent);
                                finish();
                            }
                            else {
                                Toast.makeText(SignIn.this, "Wrong Password", Toast.LENGTH_SHORT).show();
                            }
                        }
                        else{
                            mDIalog.dismiss();
                            Toast.makeText(SignIn.this, "User Doesn't exist", Toast.LENGTH_SHORT).show();
                        }
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError){

                    }
                });
            }
        });
    }
}
