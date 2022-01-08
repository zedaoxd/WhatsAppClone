package com.example.whatszapclone.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;

import com.bumptech.glide.Glide;
import com.example.whatszapclone.adapter.AdapterMessages;
import com.example.whatszapclone.config.FirebaseSettings;
import com.example.whatszapclone.databinding.ActivityChatBinding;
import com.example.whatszapclone.model.Conversa;
import com.example.whatszapclone.model.Group;
import com.example.whatszapclone.model.Message;
import com.example.whatszapclone.model.Usuario;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.whatszapclone.R;
import com.example.whatszapclone.utils.Base64Custom;
import com.example.whatszapclone.utils.UserFirebase;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ChatActivity extends AppCompatActivity {

    private ActivityChatBinding binding;
    private Usuario recipientUser;
    private Usuario senderUser;
    private List<Message> messageList = new ArrayList<>();
    private DatabaseReference reference;
    private DatabaseReference messagesRef;
    private StorageReference storageReference;
    private ChildEventListener childEventListenerMessages;
    private AdapterMessages adapterMessages;
    private ActivityResultLauncher<Intent> activityResultLauncher;
    private Group group;

    //user identifiers
    private String idUserSender;
    private String idUserRecipient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.toolbar.setTitle("");
        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {

            }
        });
        storageReference = FirebaseSettings.getFirebaseStorage();

        setUserSender();
        setInitialsParametersRecipient();
        adapterMessages = new AdapterMessages(messageList, getApplicationContext());
        settingsRecyclerViewMessages();

        reference = FirebaseSettings.getDatabaseReference();
        messagesRef = reference.child("mensagens")
                .child(idUserRecipient)
                .child(idUserSender);

        clickFabSendMessage();
        clickImageSendMessage();
    }

    @Override
    protected void onStart() {
        super.onStart();
        retrieveMessageFromFirebase();
    }

    @Override
    protected void onStop() {
        super.onStop();
        messagesRef.removeEventListener(childEventListenerMessages);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK){
            try {
                Bitmap image = (Bitmap) data.getExtras().get("data");
                if (image != null){
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    int imageQuality = 75;
                    image.compress(Bitmap.CompressFormat.JPEG, imageQuality, baos);
                    byte[] imageData = baos.toByteArray();

                    String nameImage = UUID.randomUUID().toString();

                    final StorageReference imageRef = storageReference.child("image")
                            .child("photo")
                            .child(idUserSender)
                            .child(nameImage);

                    UploadTask uploadTask = imageRef.putBytes(imageData);
                    uploadTask.addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d("error", "error when trying to upload the image");
                        }
                    }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            imageRef.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                                @Override
                                public void onComplete(@NonNull Task<Uri> task) {

                                    Uri url = task.getResult();
                                    Message message = new Message();
                                    if (recipientUser != null) {
                                        message.setUserId(idUserSender);
                                        message.setMessage("image.jpg");
                                        message.setImage(url.toString());
                                        saveMessageToFirebase(idUserSender, idUserRecipient, message);
                                    } else {
                                        for (Usuario member : group.getUsuarioList()) {
                                            String idSenderGroup = Base64Custom.encodeBase64(member.getEmail());
                                            String idCurrentUser = UserFirebase.getUserIdentifier();

                                            message.setUserId(idCurrentUser);
                                            message.setMessage("image.jpeg");
                                            message.setName(senderUser.getNome());
                                            message.setImage(url.toString());
                                            saveMessageToFirebase(idSenderGroup, idUserRecipient, message);
                                            saveConversation(idSenderGroup, idUserRecipient, recipientUser, message, true);
                                        }
                                    }
                                }
                            });
                        }
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void settingsRecyclerViewMessages(){
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        binding.include.recyclerViewMessages.setLayoutManager(layoutManager);
        binding.include.recyclerViewMessages.setHasFixedSize(true);
        binding.include.recyclerViewMessages.setAdapter(adapterMessages);
    }

    private void setUserSender(){
        idUserSender = UserFirebase.getUserIdentifier();
        senderUser = UserFirebase.getDataUser();
    }

    private void setInitialsParametersRecipient(){
        Bundle bundle = getIntent().getExtras();
        if (bundle != null){
            if (bundle.containsKey("group")){
                group = (Group) bundle.getSerializable("group");
                idUserRecipient = group.getId();
                binding.textViewNameChat.setText(group.getName());

                String photo = group.getPhoto();
                if (photo != null) {
                    Uri url = Uri.parse(photo);
                    Glide.with(ChatActivity.this)
                            .load(url)
                            .into(binding.circleImagePhotoChat);
                } else {
                    binding.circleImagePhotoChat.setImageResource(R.drawable.padrao);
                }
            } else {
                recipientUser = (Usuario) bundle.getSerializable("key_user");
                binding.textViewNameChat.setText(recipientUser.getNome());

                String pathImage = recipientUser.getFoto();
                if (pathImage != null){
                    Uri url = Uri.parse(pathImage);
                    Glide.with(ChatActivity.this)
                            .load(url)
                            .into(binding.circleImagePhotoChat);
                } else {
                    binding.circleImagePhotoChat.setImageResource(R.drawable.padrao);
                }
                idUserRecipient = Base64Custom.encodeBase64(recipientUser.getEmail());
            }
        }
    }

    private void clickImageSendMessage(){
        binding.include.imageCameraSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (intent.resolveActivity(getPackageManager()) != null){
                    activityResultLauncher.launch(intent);
                }
            }
        });
    }

    private void clickFabSendMessage(){
        binding.include.fabSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String message = binding.include.textMessageChat.getText().toString();
                if (!message.isEmpty()){

                    if (recipientUser != null) {
                        Message m = new Message();
                        m.setUserId(idUserSender);
                        m.setMessage(message);
                        // save message
                        saveMessageToFirebase(idUserSender, idUserRecipient, m);

                        // save conversation to Sender
                        saveConversation(idUserSender, idUserRecipient, recipientUser, m, false);

                        // save conversation to Recipient
                        saveConversation(idUserRecipient, idUserSender, senderUser, m, false);
                    } else {
                        for (Usuario member : group.getUsuarioList()) {
                            String idSenderGroup = Base64Custom.encodeBase64(member.getEmail());
                            String idCurrentUser = UserFirebase.getUserIdentifier();

                            Message m = new Message();
                            m.setUserId(idCurrentUser);
                            m.setMessage(message);
                            m.setName(senderUser.getNome());
                            saveMessageToFirebase(idSenderGroup, idUserRecipient, m);
                            saveConversation(idSenderGroup, idUserRecipient, recipientUser, m, true);
                        }
                    }
                } else {
                    toastMessage("Digite uma mensagem para enviar");
                }
            }
        });
    }

    private void saveConversation(String idSender, String idRecipient, Usuario displayUser, Message message, boolean isGroup){

        Conversa conversationSender = new Conversa();
        conversationSender.setIdSender(idSender);
        conversationSender.setIdRecipient(idRecipient);
        conversationSender.setLastMessage(message.getMessage());
        if (isGroup) {
            conversationSender.setGroup(group);
            conversationSender.setIsGroup("true");
        } else {
            //conversationSender.setUserExibition(recipientUser);
            conversationSender.setUserExibition(displayUser);
            conversationSender.setIsGroup("false");
        }
        conversationSender.save();
    }

    private void saveMessageToFirebase(String idUserSender, String idUserRecipient, Message message){
        DatabaseReference reference = FirebaseSettings.getDatabaseReference();
        DatabaseReference messagesRef = reference.child("mensagens");

        messagesRef.child(idUserSender)
                .child(idUserRecipient)
                .push()
                .setValue(message);

        messagesRef.child(idUserRecipient)
                .child(idUserSender)
                .push()
                .setValue(message);

        binding.include.textMessageChat.setText("");
    }

    private void retrieveMessageFromFirebase(){
        messageList.clear();
        childEventListenerMessages = messagesRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Message message = snapshot.getValue(Message.class);
                messageList.add(message);
                adapterMessages.notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void toastMessage(String message){
        Toast.makeText(ChatActivity.this, message, Toast.LENGTH_LONG).show();
    }
}