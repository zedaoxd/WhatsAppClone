package com.example.whatszapclone.activity;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.example.whatszapclone.R;
import com.example.whatszapclone.config.FirebaseSettings;
import com.example.whatszapclone.databinding.ActivitySettingsBinding;

import com.example.whatszapclone.model.Usuario;
import com.example.whatszapclone.utils.Permissions;
import com.example.whatszapclone.utils.UserFirebase;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;

import android.view.View;
import android.widget.Toast;


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Objects;

public class SettingsActivity extends AppCompatActivity {

    private ActivitySettingsBinding binding;
    private final String[] requiredPermissions = new String[]{
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA,
    };
    //private ActivityResultLauncher<Intent> activityResultLauncher;
    private static final int CAMERA = 100;
    private static final int PHOTO = 200;
    private String userIdentifier;
    private FirebaseUser currentUser;
    private Usuario loggedUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        binding = ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        loggedUser = UserFirebase.getDataUser();
        loadDataUser();
        binding.progressBarPhoto.setVisibility(View.GONE);
        /*
        activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {

            }
        });
         */
        Permissions.validatePermission(requiredPermissions, this, 1);

        toolbarSettings();
        clickButtonCamera();
        clickButtonGallery();
        clickButtonSaveNameUser();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        for (int result : grantResults){
            if (result == PackageManager.PERMISSION_DENIED){
                alertValidationPermission();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK){
            binding.progressBarPhoto.setVisibility(View.VISIBLE);
            Bitmap image = null;
            if (requestCode == PHOTO){
                try {
                    Uri uri = data.getData();
                    image = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else if (requestCode == CAMERA){
                image = (Bitmap) data.getExtras().get("data");
            }

            if (image != null){
                binding.circleImage.setImageBitmap(image);
                saveImageFirebase(image);
            }
        }
    }

    private void clickButtonSaveNameUser(){
        binding.imageViewSaveName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name = binding.PersonName.getText().toString();
                if (UserFirebase.updateUserName(name)){
                    loggedUser.setNome(name);
                    loggedUser.update();
                    toastMessage("Nome atualizado");
                } else {
                    toastMessage("Erro ao atualizar nome");
                }
            }
        });
    }

    private void loadDataUser(){
        userIdentifier = UserFirebase.getUserIdentifier();
        currentUser = UserFirebase.getCurrentUser();
        Uri uri = currentUser.getPhotoUrl();
        if (uri != null){
            Glide.with(SettingsActivity.this)
                    .load(uri)
                    .into(binding.circleImage);
        } else {
            binding.circleImage.setImageResource(R.drawable.padrao);
        }
        binding.PersonName.setText(currentUser.getDisplayName());
    }

    private void saveImageFirebase(@NonNull Bitmap image){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int imageQuality = 75;
        image.compress(Bitmap.CompressFormat.JPEG, imageQuality, baos);
        byte[] imageData = baos.toByteArray();

        StorageReference storageReference = FirebaseSettings.getFirebaseStorage();
        final StorageReference imageRef = storageReference.child("image").child("perfil").child(userIdentifier).child("perfil.jpg");

        UploadTask uploadTask = imageRef.putBytes(imageData);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                toastMessage("Erro ao fazer upload da imagem");
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                toastMessage("Sucesso ao fazer upload da imagem");
                imageRef.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        Uri uri = task.getResult();
                        updateUserPhoto(uri);
                    }
                });
            }
        });
    }

    private void updateUserPhoto(Uri uri){
        if (UserFirebase.updateUserPhoto(uri)){
            binding.progressBarPhoto.setVisibility(View.GONE);
            loggedUser.setFoto(uri.toString());
            loggedUser.update();
            toastMessage("Foto atualizada");
        }

    }

    private void clickButtonGallery(){
        binding.buttonPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                if (intent.resolveActivity(getPackageManager()) != null){
                    startActivityForResult(intent, PHOTO);
                    //activityResultLauncher.launch(intent);
                }
            }
        });
    }

    private void clickButtonCamera(){
        binding.buttonCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (intent.resolveActivity(getPackageManager()) != null){
                    startActivityForResult(intent, CAMERA);
                    //activityResultLauncher.launch(intent);
                }
            }
        });
    }

    private void alertValidationPermission(){
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle(R.string.title_alert_permission_denied);
        alert.setMessage(R.string.message_permission_denied);
        alert.setCancelable(false);
        alert.setPositiveButton(R.string.alert_button_confirm, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                finish();
            }
        }).create().show();
    }

    private void toolbarSettings(){
        binding.include.toolbarMain.setTitle(R.string.title_settings);
        setSupportActionBar(binding.include.toolbarMain);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

    }

    private void toastMessage(String message){
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
    }
}