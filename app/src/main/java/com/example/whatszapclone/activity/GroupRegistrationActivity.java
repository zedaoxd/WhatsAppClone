package com.example.whatszapclone.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;

import com.example.whatszapclone.adapter.SelectedGroupAdapter;
import com.example.whatszapclone.config.FirebaseSettings;
import com.example.whatszapclone.databinding.ActivityGroupRegistrationBinding;
import com.example.whatszapclone.model.Group;
import com.example.whatszapclone.model.Usuario;
import com.example.whatszapclone.utils.UserFirebase;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.provider.MediaStore;
import android.view.View;
import android.widget.Toast;


import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

public class GroupRegistrationActivity extends AppCompatActivity {

    private ActivityGroupRegistrationBinding binding;
    private List<Usuario> usuarioListSelected = new ArrayList<>();
    private SelectedGroupAdapter selectedGroupAdapter;
    private ActivityResultLauncher<Intent> activityResultLauncher;
    private final int QUALITY_IMAGE = 75;
    private StorageReference storageReference;
    private Group group;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityGroupRegistrationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        /* Initial Settings */
        group = new Group();
        storageReference = FirebaseSettings.getFirebaseStorage();
        activityResultLauncher = activityResult();
        /*--------------------------*/

        settingsToolbar();
        fab();
        retrieveusers();
        settingsRecyclerView();
        clickEventSetImageGroup();
    }

    private ActivityResultLauncher<Intent> activityResult(){
        return registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                if (result.getResultCode() == RESULT_OK){
                    try {
                        Uri url = result.getData().getData();
                        Bitmap image = MediaStore.Images.Media.getBitmap(getContentResolver(), url);
                        saveImageGroupToFirebase(image);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    private void saveImageGroupToFirebase(Bitmap image){
        if (image != null) {
            binding.include.circleImageGroup.setImageBitmap(image);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            image.compress(Bitmap.CompressFormat.JPEG, QUALITY_IMAGE, baos);
            byte[] dataImage = baos.toByteArray();

            final StorageReference imageRef = storageReference
                    .child("image")
                    .child("group")
                    .child(group.getId() + ".jpeg");

            UploadTask uploadTask = imageRef.putBytes(dataImage);
            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    toasMessage("Erro ao fazer upload da imagem");
                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    toasMessage("imagem salva com sucesso");
                    imageRef.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                        @Override
                        public void onComplete(@NonNull Task<Uri> task) {
                            Uri url = task.getResult();
                            group.setPhoto(url.toString());
                        }
                    });
                }
            });
        }
    }

    private void clickEventSetImageGroup(){
        binding.include.circleImageGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                if (i.resolveActivity(getPackageManager()) != null){
                    activityResultLauncher.launch(i);
                }
            }
        });
    }

    private void settingsRecyclerView(){
        selectedGroupAdapter = new SelectedGroupAdapter(usuarioListSelected, getApplicationContext());

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(
                getApplicationContext(),
                LinearLayoutManager.HORIZONTAL,
                false
        );
        binding.include.recyclerViewMembersGroup.setLayoutManager(layoutManager);
        binding.include.recyclerViewMembersGroup.setHasFixedSize(true);
        binding.include.recyclerViewMembersGroup.setAdapter(selectedGroupAdapter);
    }

    private void settingsToolbar(){
        binding.toolbar.setTitle("Novo grupo");
        binding.toolbar.setSubtitle("Defina um nome");
        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void retrieveusers(){
        if (getIntent().getExtras() != null) {
            List<Usuario> userList = (List<Usuario>) getIntent().getExtras().getSerializable("members");
            usuarioListSelected.addAll(userList);
            binding.include.textTotalParticipantes.setText("Participantes: " + usuarioListSelected.size());
        }
    }

    private void fab(){
        binding.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String nameGroup = binding.include.editTextGroupName.getText().toString();
                usuarioListSelected.add(UserFirebase.getDataUser());

                group.setName(nameGroup);
                group.setUsuarioList(usuarioListSelected);
                group.save();

                Intent i = new Intent(GroupRegistrationActivity.this,  ChatActivity.class);
                i.putExtra("group", group);
                startActivity(i);
            }
        });
    }

    private void toasMessage(String message) {
        Toast.makeText(GroupRegistrationActivity.this, message, Toast.LENGTH_LONG).show();
    }

}