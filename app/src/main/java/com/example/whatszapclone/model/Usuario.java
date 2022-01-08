package com.example.whatszapclone.model;

import androidx.annotation.NonNull;

import com.example.whatszapclone.config.FirebaseSettings;
import com.example.whatszapclone.utils.UserFirebase;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Exclude;
import com.google.firebase.database.FirebaseDatabase;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class Usuario implements Serializable {
    private String id, nome, email, senha, foto;

    public Usuario() {
    }

    public Usuario(String nome, String email, String senha) {
        this.nome = nome;
        this.email = email;
        this.senha = senha;
    }

    public void update(){
        String userIdentifier = UserFirebase.getUserIdentifier();
        DatabaseReference database = FirebaseSettings.getDatabaseReference();
        DatabaseReference users = database.child("usuarios").child(userIdentifier);
        Map<String, Object> userValues = convertToMap();
        users.updateChildren(userValues);
    }

    @NonNull
    @Exclude
    private Map<String, Object> convertToMap(){
        HashMap<String, Object> userMap = new HashMap<>();
        userMap.put("email", getEmail());
        userMap.put("nome", getNome());
        userMap.put("foto", getFoto());
        return userMap;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Exclude
    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    @Exclude
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFoto() {
        return foto;
    }

    public void setFoto(String foto) {
        this.foto = foto;
    }
}
