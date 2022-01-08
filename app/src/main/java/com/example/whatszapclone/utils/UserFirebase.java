package com.example.whatszapclone.utils;

import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.whatszapclone.config.FirebaseSettings;
import com.example.whatszapclone.model.Usuario;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

public class UserFirebase {

    public static String getUserIdentifier(){
        FirebaseAuth user = FirebaseSettings.getFirebaseAuth();
        String email = user.getCurrentUser().getEmail();
        return Base64Custom.encodeBase64(email);
    }

    public static FirebaseUser getCurrentUser(){
        FirebaseAuth user = FirebaseSettings.getFirebaseAuth();
        return user.getCurrentUser();
    }

    public static boolean updateUserPhoto(Uri uri){
        try {
            FirebaseUser user = getCurrentUser();
            UserProfileChangeRequest userProfileChangeRequest = new UserProfileChangeRequest.Builder().setPhotoUri(uri).build();
            user.updateProfile(userProfileChangeRequest).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (!task.isSuccessful()){
                        Log.d("Perfil", "Erro ao atualizar foto de perfil");
                    }
                }
            });
            return true;
        } catch (Exception e){
            return false;
        }
    }

    public static boolean updateUserName(String name){
        try {
            FirebaseUser user = getCurrentUser();
            UserProfileChangeRequest userProfileChangeRequest = new UserProfileChangeRequest.Builder()
                    .setDisplayName(name)
                    .build();
            user.updateProfile(userProfileChangeRequest).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (!task.isSuccessful()){
                        Log.d("Perfil", "Erro ao atualizar nome de perfil");
                    }
                }
            });
            return true;
        } catch (Exception e){
            return false;
        }
    }

    @NonNull
    public static Usuario getDataUser(){
        FirebaseUser userFirebase = getCurrentUser();
        Usuario dataUser = new Usuario();
        dataUser.setEmail(userFirebase.getEmail());
        dataUser.setNome(userFirebase.getDisplayName());
        if (userFirebase.getPhotoUrl() == null){
            dataUser.setFoto("");
        } else {
            dataUser.setFoto(userFirebase.getPhotoUrl().toString());
        }
        return dataUser;
    }
}
