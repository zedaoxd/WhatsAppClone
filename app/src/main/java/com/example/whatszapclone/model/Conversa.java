package com.example.whatszapclone.model;

import com.example.whatszapclone.config.FirebaseSettings;
import com.google.firebase.database.DatabaseReference;

public class Conversa {
    private String idSender;
    private String idRecipient;
    private String lastMessage;
    private Usuario userExibition;
    private String isGroup;
    private Group group;

    public Conversa() {
        this.setIsGroup("false");
    }

    public void save(){
        DatabaseReference databaseReference = FirebaseSettings.getDatabaseReference();
        DatabaseReference conversationRef = databaseReference.child("conversation");

        conversationRef.child(getIdSender())
                .child(getIdRecipient())
                .setValue(this);
    }

    public String getIsGroup() {
        return isGroup;
    }

    public void setIsGroup(String isGroup) {
        this.isGroup = isGroup;
    }

    public Group getGroup() {
        return group;
    }

    public void setGroup(Group group) {
        this.group = group;
    }

    public String getIdSender() {
        return idSender;
    }

    public void setIdSender(String idSender) {
        this.idSender = idSender;
    }

    public String getIdRecipient() {
        return idRecipient;
    }

    public void setIdRecipient(String idRecipient) {
        this.idRecipient = idRecipient;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public Usuario getUserExibition() {
        return userExibition;
    }

    public void setUserExibition(Usuario userExibition) {
        this.userExibition = userExibition;
    }
}
