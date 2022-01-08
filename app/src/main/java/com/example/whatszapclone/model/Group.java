package com.example.whatszapclone.model;

import com.example.whatszapclone.config.FirebaseSettings;
import com.example.whatszapclone.utils.Base64Custom;
import com.google.firebase.database.DatabaseReference;

import java.io.Serializable;
import java.util.List;

public class Group implements Serializable {
    private String id, name, photo;
    private List<Usuario> usuarioList;

    public Group() {
        DatabaseReference databaseReference = FirebaseSettings.getDatabaseReference();
        DatabaseReference groupRef = databaseReference.child("group");
        this.id = groupRef.push().getKey();
    }

    public void save(){
        DatabaseReference databaseReference = FirebaseSettings.getDatabaseReference();
        DatabaseReference groupRef = databaseReference.child("group");

        groupRef.child(getId()).setValue(this);
        for (Usuario member : getUsuarioList()) {
            String idSender = Base64Custom.encodeBase64(member.getEmail());
            String idRecipient = getId();

            Conversa conversa = new Conversa();
            conversa.setIdSender(idSender);
            conversa.setIdRecipient(idRecipient);
            conversa.setLastMessage("");
            conversa.setIsGroup("true");
            conversa.setGroup(this);

            conversa.save();
        }
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public List<Usuario> getUsuarioList() {
        return usuarioList;
    }

    public void setUsuarioList(List<Usuario> usuarioList) {
        this.usuarioList = usuarioList;
    }
}
