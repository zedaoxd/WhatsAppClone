package com.example.whatszapclone.fragment;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import com.example.whatszapclone.R;
import com.example.whatszapclone.activity.ChatActivity;
import com.example.whatszapclone.adapter.AdapterConversations;
import com.example.whatszapclone.config.FirebaseSettings;
import com.example.whatszapclone.databinding.FragmentContactsBinding;
import com.example.whatszapclone.databinding.FragmentConversationsBinding;
import com.example.whatszapclone.model.Conversa;
import com.example.whatszapclone.model.Usuario;
import com.example.whatszapclone.utils.RecyclerItemClickListener;
import com.example.whatszapclone.utils.UserFirebase;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class ConversationsFragment extends Fragment {

    private FragmentConversationsBinding binding;
    private List<Conversa> conversaList = new ArrayList<>();
    private AdapterConversations adapterConversations;
    private DatabaseReference reference;
    private DatabaseReference conversationRef;
    private ChildEventListener childEventListenerConversation;

    public ConversationsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentConversationsBinding.inflate(inflater, container, false);
        View v = binding.getRoot();

        initialSettings();
        settingsRecyclerViewConversations();

        return v;
    }

    @Override
    public void onStart() {
        super.onStart();
        recoveryConversationFromFirebase();
    }

    @Override
    public void onStop() {
        super.onStop();
        conversationRef.removeEventListener(childEventListenerConversation);
    }

    public void searchConversation(String text){
        List<Conversa> conversaListSearch = new ArrayList<>();
        for (Conversa conversa : conversaList){

            String nameUser;
            if (conversa.getUserExibition() != null) {
                nameUser = conversa.getUserExibition().getNome().toLowerCase();
            } else {
                nameUser = conversa.getGroup().getName().toLowerCase();
            }
            String lastMessage = conversa.getLastMessage().toLowerCase();
            if (nameUser.contains(text) || lastMessage.contains(text)){
                conversaListSearch.add(conversa);
            }
        }
        setListFromAdapter(conversaListSearch);
    }

    public void reloadConversations(){
       setListFromAdapter(conversaList);
    }

    private void setListFromAdapter(List<Conversa> list){
        adapterConversations = new AdapterConversations(list, getActivity());
        binding.RecyclerViewConversations.setAdapter(adapterConversations);
        adapterConversations.notifyDataSetChanged();
    }

    private void initialSettings(){
        reference = FirebaseSettings.getDatabaseReference();
        String idCurrentUser = UserFirebase.getUserIdentifier();
        conversationRef = reference.child("conversation").child(idCurrentUser);
    }

    private void settingsRecyclerViewConversations(){
        adapterConversations = new AdapterConversations(conversaList, getActivity());

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
        binding.RecyclerViewConversations.setLayoutManager(layoutManager);
        binding.RecyclerViewConversations.setHasFixedSize(true);
        binding.RecyclerViewConversations.setAdapter(adapterConversations);

        eventClickRecyclerViewConversation();
    }

    private void eventClickRecyclerViewConversation(){
        binding.RecyclerViewConversations.addOnItemTouchListener(
                new RecyclerItemClickListener(
                        getActivity(),
                        binding.RecyclerViewConversations,
                        new RecyclerItemClickListener.OnItemClickListener() {
                            @Override
                            public void onItemClick(View view, int position) {

                                List<Conversa> newListConversation = adapterConversations.getConversas();
                                Conversa selectedConversation = newListConversation.get(position);

                                Intent i = new Intent(getActivity(), ChatActivity.class);
                                if (selectedConversation.getIsGroup().equals("true")) {
                                    i.putExtra("group", selectedConversation.getGroup());
                                } else {
                                    i.putExtra("key_user", selectedConversation.getUserExibition());
                                }
                                startActivity(i);

                            }
                            @Override
                            public void onLongItemClick(View view, int position) {
                            }
                            @Override
                            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                            }
                        }
                )
        );
    }

    private void recoveryConversationFromFirebase(){
        conversaList.clear();
        childEventListenerConversation = conversationRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Conversa conversa = snapshot.getValue(Conversa.class);
                conversaList.add(conversa);
                adapterConversations.notifyDataSetChanged();
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
}