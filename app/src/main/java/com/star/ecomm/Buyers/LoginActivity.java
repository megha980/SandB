package com.star.ecomm.Buyers;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;

import android.content.Intent;
import android.os.Bundle;

import android.text.TextUtils;
import android.view.View;
import android.widget.Button;

import com.rey.material.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.star.ecomm.Admin.AdminHomeActivity;
import com.star.ecomm.Sellers.SellerProductCategoryActivity;
import com.star.ecomm.Model.Users;
import com.star.ecomm.Prevalent.Prevalent;
import com.star.ecomm.R;

import io.paperdb.Paper;

public class LoginActivity extends AppCompatActivity {
    private EditText InputPhoneNumber, InputPassword;
    private Button LoginButton;
    private ProgressDialog loadingBar;
    private TextView AdminLink, NotAdminLink, ForgetPasswordLink;

    private String parentDbName = "Users";//for users not for admin
    private CheckBox chkBoxRememberMe;   //import com.rey.material.widget.CheckBox;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        LoginButton = (Button) findViewById(R.id.login_btn);
        InputPassword = (EditText) findViewById(R.id.login_password_input);
        InputPhoneNumber = (EditText) findViewById(R.id.login_phone_number_input);
        AdminLink = (TextView) findViewById(R.id.admin_panel_link);
        NotAdminLink = (TextView) findViewById(R.id.not_admin_panel_link);
        ForgetPasswordLink = findViewById(R.id.forget_password_link);
        loadingBar = new ProgressDialog(this);
        chkBoxRememberMe=(CheckBox)findViewById(R.id.remember_me_chkb);


        Paper.init(this); //for remember me checkbox


        LoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                LoginUser();
            }
        });


        ForgetPasswordLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                Intent intent = new Intent(LoginActivity.this, ResetPasswordActivity.class);
                intent.putExtra("check", "login");
                startActivity(intent);
            }
        });
        AdminLink.setOnClickListener(new View.OnClickListener() {  //admin login
            @Override
            public void onClick(View view)
            {
                LoginButton.setText("Login Admin"); //button text set as "login Admin"
                AdminLink.setVisibility(View.INVISIBLE);  //admin login button will be invisible
                NotAdminLink.setVisibility(View.VISIBLE);  //user login button will be visible
                parentDbName = "Admins";  //change parentDbName from Users to Admins node in firebase and add data in Admins node
            }
        });

        NotAdminLink.setOnClickListener(new View.OnClickListener() {     //user login
            @Override
            public void onClick(View view)
            {
                LoginButton.setText("Login"); //button text set as "login" for users
                AdminLink.setVisibility(View.VISIBLE); //admin login visible
                NotAdminLink.setVisibility(View.INVISIBLE); //user login invisible
                parentDbName = "Users"; //  add data in Users node
            }
        });


    }
    private void LoginUser()
    {
        //taking phone no. and password in string variables
        String phone = InputPhoneNumber.getText().toString();
        String password = InputPassword.getText().toString();

        if (TextUtils.isEmpty(phone))  //check empty or not
        {
            Toast.makeText(this, "Please write your phone number...", Toast.LENGTH_SHORT).show();
        }
        else if (TextUtils.isEmpty(password))
        {
            Toast.makeText(this, "Please write your password...", Toast.LENGTH_SHORT).show();
        }
        else
        {
            loadingBar.setTitle("Login Account"); //loading a progress bar
            loadingBar.setMessage("Please wait, while we are checking the credentials.");
            loadingBar.setCanceledOnTouchOutside(false);
            loadingBar.show();


            AllowAccessToAccount(phone, password);
        }
    }

    private void AllowAccessToAccount(final String phone, final String password) {
        if(chkBoxRememberMe.isChecked())
        {
            // writing different phone nad password key for different users using Prevalent class
            Paper.book().write(Prevalent.UserPhoneKey, phone);
            Paper.book().write(Prevalent.UserPasswordKey, password);
        }

        final DatabaseReference RootRef; //Root node reference in firebase
        RootRef = FirebaseDatabase.getInstance().getReference(); //getting firebase instance and reference

        RootRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                if (dataSnapshot.child(parentDbName).child(phone).exists()) //checking users phone number exists or not
                {
                    Users usersData = dataSnapshot.child(parentDbName).child(phone).getValue(Users.class);  //taking all user data by referencing phone number of particular user.

                    if (usersData.getPhone().equals(phone)) // checking entered phone no. equal to firebase phone no or not
                    {
                        if (usersData.getPassword().equals(password)) // checking same for password
                        {
                            if (parentDbName.equals("Admins")) //checking login by admin
                            {
                                Toast.makeText(LoginActivity.this, "Welcome Admin, you are logged in Successfully...", Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss();

                                Intent intent = new Intent(LoginActivity.this, AdminHomeActivity.class); // from login to admin home activity
                                startActivity(intent);
                            }
                            else if (parentDbName.equals("Users")) //login by user
                            {
                                Toast.makeText(LoginActivity.this, "logged in Successfully...", Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss();

                                Intent intent = new Intent(LoginActivity.this, HomeActivity.class); //login to home activity
                                Prevalent.currentOnlineUser = usersData; //taking user data for current user
                                startActivity(intent);
                            }
                        }
                        else
                        {
                            loadingBar.dismiss();
                            Toast.makeText(LoginActivity.this, "Password is incorrect.", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
                else
                {
                    Toast.makeText(LoginActivity.this, "Account with this " + phone + " number do not exists.", Toast.LENGTH_SHORT).show();
                    loadingBar.dismiss();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}