package com.saviour.instagramdemo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.menu.MenuAdapter;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.common.base.MoreObjects;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.saviour.instagramdemo.Adapter.UserAdapter;
import com.saviour.instagramdemo.Model.User;

import java.util.HashMap;
import java.util.HashSet;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private UserAdapter adapter;

    private FirebaseAuth mAuth;
    private FirebaseFirestore mFireStore;

    private final int STORAGE_REQ_CODE = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        mFireStore = FirebaseFirestore.getInstance();
        recyclerView = findViewById(R.id.recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        if (mAuth.getCurrentUser() == null){
            startActivity(new Intent(MainActivity.this,Sign_In.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
            finish();
        }else{
            Query query = mFireStore.collection("User").whereNotEqualTo("Id",mAuth.getCurrentUser().getUid());
            FirestoreRecyclerOptions<User> options = new FirestoreRecyclerOptions.Builder<User>()
                    .setQuery(query,User.class)
                    .build();
            adapter = new UserAdapter(options,MainActivity.this);
            recyclerView.setAdapter(adapter);
        }



    }

    @Override
    protected void onStart() {
        super.onStart();
        adapter.startListening();
        if (mAuth.getCurrentUser() == null){
            startActivity(new Intent(MainActivity.this,Sign_In.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
            finish();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        adapter.stopListening();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.sign_out,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.sign_out:
                mAuth.signOut();
                startActivity(new Intent(MainActivity.this,Sign_In.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                finish();
                return true;
            case R.id.delete:
                mAuth.getCurrentUser().delete();
                startActivity(new Intent(MainActivity.this,Sign_In.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                finish();
                return true;
            case R.id.share:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                        requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},STORAGE_REQ_CODE);
                    }else {
                        getPhoto();
                    }
                }
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void getPhoto() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent,STORAGE_REQ_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == STORAGE_REQ_CODE){
            if (grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                getPhoto();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Uri SelectedImage = data.getData();
        if (requestCode == STORAGE_REQ_CODE && resultCode ==RESULT_OK && data != null){
            try{
                StorageReference storageReference = FirebaseStorage.getInstance().getReference(mAuth.getCurrentUser().getUid()).child(SelectedImage.getLastPathSegment());
                storageReference.putFile(SelectedImage).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if (task.isSuccessful()){
                            FirebaseDatabase database = FirebaseDatabase.getInstance();
                            DatabaseReference myRef = database.getReference("Images");

                            task.getResult().getMetadata().getReference().getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                                @Override
                                public void onComplete(@NonNull Task<Uri> task) {
                                    if (task.isSuccessful()){
                                        Toast.makeText(getApplicationContext(),String.valueOf(task.getResult()),Toast.LENGTH_SHORT).show();
                                        HashMap<String,Object> url = new HashMap<>();
                                        url.put("url",String.valueOf(task.getResult()));
                                        url.put("createdBy",String.valueOf(mAuth.getCurrentUser().getUid()));

                                        myRef.push().setValue(url);
                                    }else{
                                        Toast.makeText(getApplicationContext(),"Failed with : "+task.getException().getMessage(),Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                            Toast.makeText(getApplicationContext(),String.valueOf(task.getResult().getMetadata().getReference().getDownloadUrl()),Toast.LENGTH_SHORT).show();
                        }else {
                            Toast.makeText(getApplicationContext(),"FAILED UPLOAD WITH :"+task.getException().getMessage(),Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }
}