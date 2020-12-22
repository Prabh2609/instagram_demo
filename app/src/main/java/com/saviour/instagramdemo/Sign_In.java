package com.saviour.instagramdemo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.media.SoundPool;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;

public class Sign_In extends AppCompatActivity implements View.OnKeyListener {
    private TextInputEditText email,password,name;
    private TextView login;
    private MaterialButton signIn,proceed;
    private ImageView logo;
    private LinearLayout background;

    private FirebaseAuth mAuth;
    private FirebaseFirestore mFireStore;
     
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign__in);

        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        name = findViewById(R.id.name);
        signIn = findViewById(R.id.btn);
        login = findViewById(R.id.signUp);
        logo = findViewById(R.id.logo);
        background = findViewById(R.id.background_layout);
        proceed = findViewById(R.id.proceed);

        logo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                turnOffKeyboard();
            }
        });
        background.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                turnOffKeyboard();
            }
        });

        mAuth = FirebaseAuth.getInstance();
        mFireStore = FirebaseFirestore.getInstance();

        password.setOnKeyListener(this);
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Object tag = view.getTag();
                if ("SignUp".equals(tag)) {
                    signIn.setText(String.valueOf(tag));
                    signIn.setTag(tag);
                    view.setTag("Login");
                    login.setText("or Login");
                } else if ("Login".equals(tag)) {
                    signIn.setText(String.valueOf(tag));
                    signIn.setTag(tag);
                    view.setTag("SignUp");
                    login.setText("or SignUp");
                }
            }
        });
        signIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loginClicked();
            }
        });

    }

    private void turnOffKeyboard(){
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),0);
    }

    private void loginClicked(){
        boolean error = false;

        if (TextUtils.isEmpty(email.getText())){
            email.setError("Can't be Empty");
            error = true;
        }else if (!isEmailValid(String.valueOf(email.getText()))){
            email.setError("Invalid Email");
            error = true;
        }

        if(TextUtils.isEmpty(password.getText())){
            password.setError("Can't be Empty");
            error = true;
        }else if (password.getText().length()<8){
            password.setError("Length is too small");
            error = true;
        }
        if (!error) {
            if (signIn.getTag().equals("SignUp")) {
                createAccount(String.valueOf(email.getText()), String.valueOf(password.getText()));
            } else {
                signIn(String.valueOf(email.getText()), String.valueOf(password.getText()));
            }
        }
    }

    private boolean isEmailValid(String email){
        return Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private void createAccount(String email,String password){
        mAuth.createUserWithEmailAndPassword(email,password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()){
                            populateDatabase();
                        }else{
                            if (task.getException() instanceof FirebaseAuthUserCollisionException){
                                Toast.makeText(Sign_In.this,"Account already existed",Toast.LENGTH_SHORT).show();
                            }else
                                Toast.makeText(Sign_In.this,"Failed with : "+task.getException().getMessage(),Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
    private void populateDatabase(){
        email.setVisibility(View.GONE);
        password.setVisibility(View.GONE);
        name.setVisibility(View.VISIBLE);
        login.setVisibility(View.GONE);
        signIn.setVisibility(View.GONE);
        proceed.setVisibility(View.VISIBLE);

        proceed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                HashMap<String,Object> user = new HashMap<>();
                user.put("Name",String.valueOf(name.getText()));
                user.put("Image","default");
                user.put("Id",mAuth.getCurrentUser().getUid());

                mFireStore.collection("User").add(user)
                        .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentReference> task) {
                                if (task.isSuccessful()){
                                    startActivity(new Intent(Sign_In.this,MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                                }else {
                                    Toast.makeText(Sign_In.this,"Failed with : "+ task.getException().getMessage(),Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });
    }
    private void signIn(String email,String password){
        mAuth.signInWithEmailAndPassword(email,password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()){
                            Toast.makeText(Sign_In.this,"Sign in successful",Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(Sign_In.this,MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                        }else {
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException){
                                Toast.makeText(Sign_In.this,"Invalid Email or Password",Toast.LENGTH_SHORT).show();
                            }else
                                Toast.makeText(Sign_In.this,"Sign in failed with : "+task.getException().getMessage(),Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
    }

    @Override
    public boolean onKey(View view, int i, KeyEvent keyEvent) {
        if (i==keyEvent.KEYCODE_ENTER && keyEvent.getAction() == KeyEvent.ACTION_DOWN){
            loginClicked();
            return true;

        }
        return false;
    }
}