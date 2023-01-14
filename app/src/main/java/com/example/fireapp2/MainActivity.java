package com.example.fireapp2;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private Button chooseButton, saveButton,displayButton;
    private ImageView imageView;
    private EditText imageNameEditText;
    private ProgressBar progressBar;
    private Uri imageUri;
    DatabaseReference databaseReference;
    StorageReference storageReference ;
    StorageTask uploadTask;

    private  static final int IMAGE_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        databaseReference = FirebaseDatabase.getInstance().getReference("Upload");
        storageReference = FirebaseStorage.getInstance().getReference("Upload");

        chooseButton = findViewById(R.id.chooseImageButton);
        saveButton = findViewById(R.id.saveImageButtonId);
        displayButton = findViewById(R.id.displayImageButtonId);
        progressBar = findViewById(R.id.progressBarId);

        imageView = findViewById(R.id.imageViewId);
        imageNameEditText = findViewById(R.id.imageNameEditTextId);


        saveButton.setOnClickListener(this);
        chooseButton.setOnClickListener(this);
        displayButton.setOnClickListener(this);



    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){

            case R.id.chooseImageButton:
                openFileChooser();
                break;
            case R.id.saveImageButtonId:
                if (uploadTask != null && uploadTask.isInProgress()) {
                    Toast.makeText(this, "Uploading in progress", Toast.LENGTH_SHORT).show();
                } else
                {saveData();
                 }

                break;
            case R.id.displayImageButtonId:

                Intent intent = new Intent(MainActivity.this,ImageActivity.class);
                startActivity(intent);
                break;

        }
    }

    private void saveData() {
        String imageName = imageNameEditText.getText().toString().trim();
        if (imageName.isEmpty()) {
            imageNameEditText.setError("Enter the image name");
            imageNameEditText.requestFocus();
            return;
        }

        StorageReference ref =
                storageReference.child(System.currentTimeMillis()+"."+getFileExtension(imageUri));


        ref.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Toast.makeText(MainActivity.this, "Image is stored successfully",
                        Toast.LENGTH_SHORT).show();
                Task<Uri> uriTask =taskSnapshot.getStorage().getDownloadUrl();

                while (!uriTask.isSuccessful());

                Uri downloadUrl = uriTask.getResult();

                Upload upload = new Upload(imageName, downloadUrl.toString());
                String uploadId = databaseReference.push().getKey();

                databaseReference.child(uploadId).setValue(upload);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(MainActivity.this, "Image is not stored successfully",
                        Toast.LENGTH_SHORT).show();
            }
        });
                


    }

    void openFileChooser() {

        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent,IMAGE_REQUEST);
    }

    public String getFileExtension(Uri imageUri){
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(imageUri));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == IMAGE_REQUEST && resultCode==RESULT_OK && data!=null && data.getData()!=null) {
            imageUri = data.getData();
            Picasso.with(this).load(imageUri).into(imageView);
        }
    }
}