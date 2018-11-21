package com.example.ojmlc.photodrive;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
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
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    File imageFile;
    Bitmap bmPhoto;
    ImageView photo;
    EditText userDescription;
    Button opnCamera, sndPhoto;
    private LocationManager gpsManager;
    private StorageReference storageRef;
    private double latitud;
    private double longitud;
    private static String currentPhotoPath, nameImage, downloadUrl;
    private static boolean sendOrNot, gpsOrNot;
    private static final String PRINCIPAL_FOLDER = "UniDrive";
    private static final String IMAGES_FOLDER = "Imagenes";
    private static final String IMAGES_DIRECTORY = PRINCIPAL_FOLDER + IMAGES_FOLDER;
    private static final int GPS_LOCATION = 62;
    private static final int CAMERA_REQUEST = 25;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle("Uni Drive");

        sendOrNot = false;
        storageRef = FirebaseStorage.getInstance().getReference();
        opnCamera = findViewById(R.id.btnCamera);
        sndPhoto = findViewById(R.id.btnEnviar);
        photo = findViewById(R.id.IMPhoto);
        userDescription = findViewById(R.id.txtDescrption);

        opnCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                        && ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION,}, GPS_LOCATION);
                } else {
                    locationStart();
                }

                File nFile = new File(Environment.getExternalStorageDirectory(), IMAGES_DIRECTORY);
                boolean created = nFile.exists();

                if (created == false) {
                    created = nFile.mkdirs();
                } else if (created == true) {
                    //Long timeStamp = System.currentTimeMillis()/1000;
                    nameImage = new SimpleDateFormat("ddMMyyyy_HHmmSS").format(new Date()) + ".jpg";
                    currentPhotoPath = Environment.getExternalStorageDirectory() + File.separator + IMAGES_DIRECTORY
                            + File.separator + nameImage;
                    imageFile = new File(currentPhotoPath);

                    Intent photoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    Uri fileUri = FileProvider.getUriForFile(MainActivity.this, BuildConfig.APPLICATION_ID, imageFile);
                    photoIntent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
                    startActivityForResult(photoIntent, CAMERA_REQUEST);
                    photo.setRotation(90);
                    sendOrNot = true;
                }
            }
        });

        sndPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (sendOrNot) {
                    Uri file = Uri.fromFile(new File(currentPhotoPath));
                    final StorageReference imageRef = storageRef.child("UniParking/Photo_" + nameImage);
                    //UploadTask uploadTask = imageRef.putFile(file);

                    imageRef.putFile(file)
                            .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                    // Get a URL to the uploaded content
                                    //Uri downloadUrl = taskSnapshot.getDownloadUrl();
                                    Toast.makeText(MainActivity.this, "Foto enviada correctamente", Toast.LENGTH_SHORT).show();
                                    Task<Uri> downloadTask = taskSnapshot.getMetadata().getReference().getDownloadUrl();


                                    downloadTask.addOnSuccessListener(new OnSuccessListener<Uri>() {
                                        @Override
                                        public void onSuccess(Uri uri) {
                                            downloadUrl = imageRef.getDownloadUrl().toString();
                                            userDescription.setText(downloadUrl);
                                        }
                                    });
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            // Handle unsuccessful uploads
                            // ...
                            Toast.makeText(MainActivity.this, "Error al subir la foto", Toast.LENGTH_SHORT).show();
                        }
                    });
                    sendOrNot = false;

                } else {
                    Toast.makeText(MainActivity.this, "No se ah tomado una foto aun", Toast.LENGTH_SHORT).show();
                }


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

    private void locationStart() {
        gpsManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        Localizator localizer = new Localizator();
        localizer.setMainActivity(this);
        gpsOrNot = gpsManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

        if (!gpsOrNot) {
            Intent settingIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(settingIntent);
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION,}, GPS_LOCATION);
            return;
        }

        gpsManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, localizer);
        gpsManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, localizer);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == GPS_LOCATION) {
            locationStart();
            return;
        }
    }

    /*
    public void setLocation(Location location) {
        if (location.getLongitude() != 0.0 && location.getLatitude() != 0.0) {
            try {
                Geocoder geocoder = new Geocoder(this, Locale.getDefault());
                List<Address> list = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);

                if (!list.isEmpty()) {
                    Address address = list.get(0);
                    ubication = "Direccion: " + address.getAddressLine(0);
                }
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }

        }
    }
    */

    public class Localizator implements LocationListener {
        MainActivity ma;

        public MainActivity getMainActivity() {
            return ma;
        }

        public void setMainActivity(MainActivity main) {
            this.ma = main;
        }

        @Override
        public void onLocationChanged(Location location) {
            latitud = location.getLatitude();
            longitud = location.getLongitude();
            //viewLongitud.setText(String.valueOf(longitud));
            //this.ma.setLocation(location);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            switch (status) {
                case LocationProvider.AVAILABLE:
                    Log.d("debug", "LocationProvider.AVAILABLE");
                    break;
                case LocationProvider.OUT_OF_SERVICE:
                    Log.d("debug", "LocationProvider.OUT_OF_SERVICE");
                    break;
                case LocationProvider.TEMPORARILY_UNAVAILABLE:
                    Log.d("debug", "LocationProvider.TEMPORARILY_UNAVAILABLE");
                    break;
            }
        }

        @Override
        public void onProviderEnabled(String provider) {
            Toast.makeText(ma, "GPS activado", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onProviderDisabled(String provider) {
            Toast.makeText(ma, "Favor de activar el GPS", Toast.LENGTH_SHORT).show();
        }
    }
}
