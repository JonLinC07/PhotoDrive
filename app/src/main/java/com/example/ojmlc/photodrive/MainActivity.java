package com.example.ojmlc.photodrive;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaScannerConnection;
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
    private String currentPhotoPath, nameImage;
    private StorageReference storageRef;
    private static final String PRINCIPAL_FOLDER = "UniDrive";
    private static final String IMAGES_FOLDER = "Imagenes";
    private static final String IMAGES_DIRECTORY = PRINCIPAL_FOLDER + IMAGES_FOLDER;
    private static final int CAMERA_REQUEST = 25;
    File imageFile;
    Bitmap bmPhoto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle("Uni Drive");

        storageRef = FirebaseStorage.getInstance().getReference();
        opnCamera = findViewById(R.id.btnCamera);
        sndPhoto = findViewById(R.id.btnEnviar);
        photo = findViewById(R.id.IMPhoto);

        opnCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                File nFile = new File(Environment.getExternalStorageDirectory(), IMAGES_DIRECTORY);
                boolean created = nFile.exists();

                if (created == false) {
                    created = nFile.mkdirs();
                } else if (created == true) {
                    //Long timeStamp = System.currentTimeMillis()/1000;
                    String nameImage = new SimpleDateFormat("ddMMyyyy_HHmmSS").format(new Date()) + ".jpg";
                    currentPhotoPath = Environment.getExternalStorageDirectory() + File.separator + IMAGES_DIRECTORY
                            + File.separator + nameImage;
                    imageFile = new File(currentPhotoPath);

                    Intent photoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    Uri fileUri = FileProvider.getUriForFile(MainActivity.this, BuildConfig.APPLICATION_ID, imageFile);
                    photoIntent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
                    startActivityForResult(photoIntent, CAMERA_REQUEST);
                }
            }
        });

        sndPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri file = Uri.fromFile(new File(currentPhotoPath));
                //CREAR METODO PARA SUBIR IMAGENES CON DIFERENTE NOMBRE
                StorageReference imageRef = storageRef.child("images/nameImage");

                imageRef.putFile(file)
                        .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                // Get a URL to the uploaded content
                                //Uri downloadUrl = taskSnapshot.getDownloadUrl();
                                Toast.makeText(MainActivity.this, "Foto enviada correctamente", Toast.LENGTH_SHORT).show();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception exception) {
                                // Handle unsuccessful uploads
                                // ...
                                Toast.makeText(MainActivity.this, "Error al subir la foto", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        MediaScannerConnection.scanFile(this, new String[]{currentPhotoPath}, null,
                new MediaScannerConnection.OnScanCompletedListener() {
                    @Override
                    public void onScanCompleted(String path, Uri uri) {
                        Log.i("Path", "" + currentPhotoPath);
                    }
                });

        bmPhoto = BitmapFactory.decodeFile(currentPhotoPath);
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
