package com.example.ojmlc.photodrive;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    Button opnCamera, sndPhoto;
    ImageView photo;
    String currentPhotoPath;
    private StorageReference storageRef;
    //int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 1;
    //int MY_PERMISSIONS_REQUEST_CAMERA = 1;
    //Uri file = Uri.fromFile(new File("/")); //path/to/images/rivers.jpg
    //StorageReference imageRef = storageRef.child("images");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle("Uni Drive");
        /*
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CAMERA}, MY_PERMISSIONS_REQUEST_CAMERA);
        }

                if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_READ_CONTACTS);
        }
        */

        storageRef = FirebaseStorage.getInstance().getReference();
        opnCamera = findViewById(R.id.btnCamera);
        sndPhoto = findViewById(R.id.btnEnviar);
        photo = findViewById(R.id.IMPhoto);

        opnCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                Intent photoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(photoIntent, 0);


            }
        });

        sndPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                photo.buildDrawingCache();
                Bitmap bm = photo.getDrawingCache();
                OutputStream fos = null;
                Uri uri;

                try {
                    currentPhotoPath = Environment.getExternalStorageDirectory() + "/" + "UniDrive/";
                    File photoDir = new File(currentPhotoPath);

                    if (!photoDir.exists()) {
                        photoDir.mkdir();
                    }

                    uri = Uri.fromFile(photoDir);
                    fos = new FileOutputStream(photoDir);
                } catch (Exception e) {
                    Log.e("ERROR!", e.getMessage());
                }

                try {
                    bm.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                    fos.flush();
                    fos.close();
                } catch (Exception e) {
                    Log.e("ERROR!", e.getMessage());
                }

                String fullName = currentPhotoPath + "myLog";
                File file = new File(fullName);


                /*photo.setDrawingCacheEnabled(true);
                photo.buildDrawingCache();
                Bitmap bm = ((BitmapDrawable)photo.getDrawable()).getBitmap();
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bm.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                byte[] data = baos.toByteArray();
                UploadTask uploadTask = storageRef.putBytes(data);

                uploadTask.addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(MainActivity.this, "Foto subida correctamente", Toast.LENGTH_SHORT).show();
                    }
                }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        taskSnapshot.getMetadata();
                        Toast.makeText(MainActivity.this, "Foto subida correctamente", Toast.LENGTH_SHORT).show();
                    }
                });*/
            }
        });

        /*imageRef.putFile(file)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        // Get a URL to the uploaded content
                        Uri downloadUrl = taskSnapshot.getDownloadUrl();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // Handle unsuccessful uploads
                        // ...
                    }
                });*/
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Bitmap bmPhoto = (Bitmap)data.getExtras().get("data");
        photo.setImageBitmap(bmPhoto);

    }

    private String createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("ddMMyyyy_HHmmSS").format(new Date());
        String imageName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(imageName, ".jpg", storageDir);

        currentPhotoPath = image.getAbsolutePath();
        return currentPhotoPath;
    }
}
