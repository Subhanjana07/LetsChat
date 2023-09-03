package com.company.letschat;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileAcitivty extends AppCompatActivity {

    CircleImageView imageViewUpdateProfile;
    TextInputEditText editTextUpdateUserName;
    Button buttonUpdate;

    FirebaseDatabase database;
    DatabaseReference databaseReference;
    FirebaseAuth auth;
    FirebaseUser user;
    FirebaseStorage firebaseStorage;
    StorageReference storageReference;
    Boolean imageControl = false;
    private static final int CAMERA_PERMISSION_CODE = 101;
    private static final int IMAGE_CAPTURE_REQUEST_CODE = 102;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_acitivty);

        database = FirebaseDatabase.getInstance();
        databaseReference = database.getReference();
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        firebaseStorage = FirebaseStorage.getInstance();
        storageReference = firebaseStorage.getReference();

        imageViewUpdateProfile = findViewById(R.id.imageViewProfilePicture);
        editTextUpdateUserName = findViewById(R.id.editTextUpdateUserName);
        buttonUpdate = findViewById(R.id.buttonUpdateProfile);

        getInfo();

        buttonUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                updateProfileInfo();
            }
        });

        if (ContextCompat.checkSelfPermission(this,android.Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE);
        }

        imageViewUpdateProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                showImageSourceDialog();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == IMAGE_CAPTURE_REQUEST_CODE && resultCode == RESULT_OK) {
            if (data != null && data.getExtras() != null) {
                // Get the captured image
                Bitmap bitmap = (Bitmap) data.getExtras().get("data");

                // Display the image in the ImageView
                imageViewUpdateProfile.setImageBitmap(bitmap);
                imageControl = true;
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            } else {
                Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }


    private final ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    if (result.getData() != null) {
                        Uri selectedImageUri = result.getData().getData();
                        Log.d("ImageUri", selectedImageUri.toString());

                        // Use Picasso to load and display the selected image
                        Picasso.get().load(selectedImageUri).into(imageViewUpdateProfile);
                        imageControl = true;
                    }
                }
            }
    );

    public void showImageSourceDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose Image Source");
        builder.setItems(new CharSequence[]{"Gallery", "Camera"}, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:
                        // Open the gallery to pick an image
                        Intent galleryIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        imagePickerLauncher.launch(galleryIntent);
                        break;
                    case 1:
                        // Open the camera to capture a new image
                        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        startActivityForResult(intent, IMAGE_CAPTURE_REQUEST_CODE);
                        break;
                }
            }
        });
        builder.show();
    }


    public void getInfo(){
        databaseReference.child("Users").child(user.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                String userName = snapshot.child("userName").getValue(String.class);
                if (snapshot.child("image").exists()) {
                    String image = snapshot.child("image").getValue(String.class);
                    Picasso.get().load(image).into(imageViewUpdateProfile);
                } else {
                    imageViewUpdateProfile.setImageResource(R.drawable.baseline_account_circle_24);
                }
                editTextUpdateUserName.setText(userName);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

                Toast.makeText(ProfileAcitivty.this, "There was a problem", Toast.LENGTH_SHORT).show();
                Log.e("error","Error Message : " +error);

            }
        });
    }

    private void uploadImageToFirebaseStorage(Bitmap bitmap) {
        // Generate a unique filename for the image
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "profile_image_" + timeStamp + ".jpg";

        // Create a reference to the Firebase Storage location where you want to store the image
        StorageReference imageRef = storageReference.child("images/" + imageFileName);

        // Convert the Bitmap to a byte array (you can choose a different format if needed)
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] imageData = baos.toByteArray();

        // Upload the byte array to Firebase Storage
        UploadTask uploadTask = imageRef.putBytes(imageData);
        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // Image uploaded successfully
                Toast.makeText(ProfileAcitivty.this, "Image uploaded to Firebase Storage", Toast.LENGTH_SHORT).show();

                // Get the download URL of the uploaded image
                imageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        String downloadUrl = uri.toString();
                        databaseReference.child("Users").child(auth.getUid()).child("image").setValue(downloadUrl).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {

                                Toast.makeText(ProfileAcitivty.this, "Write to Database is successfull", Toast.LENGTH_SHORT).show();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(ProfileAcitivty.this, "Write to Database is not successfull", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });
            }

        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle the failure to upload the image
                Toast.makeText(ProfileAcitivty.this, "Image upload failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void updateProfileInfo(){
        String updatedUserName = editTextUpdateUserName.getText().toString();
        databaseReference.child("Users").child(user.getUid()).child("userName").setValue(updatedUserName);
        if(imageControl) {
            imageViewUpdateProfile.setDrawingCacheEnabled(true);
            imageViewUpdateProfile.buildDrawingCache();
            Bitmap bitmap = ((BitmapDrawable) imageViewUpdateProfile.getDrawable()).getBitmap();

            // Upload the image to Firestore
            uploadImageToFirebaseStorage(bitmap);
        }

        Intent intent = new Intent(ProfileAcitivty.this, MainActivity.class);
        intent.putExtra("userName", updatedUserName);
        startActivity(intent);
        finish();
    }
}