package com.star.ecomm.Sellers;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import com.star.ecomm.R;

import java.util.HashMap;

public class SellerRegistrationActivity extends AppCompatActivity {

    private Button sellerLoginButton;
    private EditText nameInput,phoneInput,emailInput,passwordInput,addressInput;
    private Button registerButton;
    private FirebaseAuth mAuth;
    private ProgressDialog loadingBar;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seller_registration);
        sellerLoginButton=(Button)findViewById(R.id.seller_already_have_account_btn);
        nameInput=(EditText) findViewById(R.id.seller_name);
        phoneInput=(EditText) findViewById(R.id.seller_phone);
        emailInput=(EditText) findViewById(R.id.seller_email);
        passwordInput=(EditText) findViewById(R.id.seller_password);
        addressInput=(EditText) findViewById(R.id.seller_address);
        registerButton=(Button)findViewById(R.id.seller_register_btn);
        mAuth= FirebaseAuth.getInstance();
        loadingBar = new ProgressDialog(this);


        sellerLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SellerRegistrationActivity.this, SellerLoginActivity.class);
                startActivity(intent);
            }
        });
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerSeller();
            }
        });
    }

    private void registerSeller() {
        final String name=nameInput.getText().toString();
        final String phone=phoneInput.getText().toString();
        final String email=emailInput.getText().toString();
        final String password=passwordInput.getText().toString();
        final String address=addressInput.getText().toString();


        if (!name.equals("") && !phone.equals("") && !email.equals("") && !password.equals("") && !address.equals("")){

            loadingBar.setTitle("Creating Seller Account");
            loadingBar.setMessage("Please wait, while we are checking the credentials.");
            loadingBar.setCanceledOnTouchOutside(false);
            loadingBar.show();
            mAuth.createUserWithEmailAndPassword(email,password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()){
                                final DatabaseReference rootRef;
                                rootRef=FirebaseDatabase.getInstance().getReference();
                                String sid=mAuth.getCurrentUser().getUid();
                                HashMap<String,Object> sellerMap=new HashMap<>();
                                sellerMap.put("sid",sid);
                                sellerMap.put("phone",phone);
                                sellerMap.put("email",email);
                                sellerMap.put("address",address);
                                sellerMap.put("name",name);
                                rootRef.child("Sellers").child(sid).updateChildren(sellerMap)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                loadingBar.dismiss();
                                                Toast.makeText(SellerRegistrationActivity.this,"You are registered successfully",Toast.LENGTH_LONG).show();

                                                Intent intent = new Intent(SellerRegistrationActivity.this, SellerHomeActivity.class);
                                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                startActivity(intent);
                                                finish();

                                            }
                                        });

                            }

                        }
                    });

        }
        else {
            Toast.makeText(SellerRegistrationActivity.this,"Please complete the registration form",Toast.LENGTH_LONG).show();

        }

    }
}
