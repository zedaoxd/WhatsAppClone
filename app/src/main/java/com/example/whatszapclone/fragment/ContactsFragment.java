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
import com.example.whatszapclone.activity.GroupActivity;
import com.example.whatszapclone.adapter.AdapterContacts;
import com.example.whatszapclone.adapter.AdapterConversations;
import com.example.whatszapclone.config.FirebaseSettings;
import com.example.whatszapclone.databinding.FragmentContactsBinding;
import com.example.whatszapclone.model.Conversa;
import com.example.whatszapclone.model.Usuario;
import com.example.whatszapclone.utils.RecyclerItemClickListener;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class ContactsFragment extends Fragment {

    private FragmentContactsBinding binding;
    private List<Usuario> listContacts = new ArrayList<>();
    private DatabaseReference reference;
    private AdapterContacts adapterContacts;
    private ValueEventListener valueEventListenerContacts;
    private FirebaseUser currentUser;

    public ContactsFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentContactsBinding.inflate(inflater, container, false);
        View v = binding.getRoot();
        //View view = inflater.inflate(R.layout.fragment_contacts, container, false);
        reference = FirebaseSettings.getDatabaseReference().child("usuarios");
        currentUser = FirebaseSettings.getFirebaseAuth().getCurrentUser();

        recyclerViewSettings();
        clickRecyclerView();
        itemGroup();

        return v;
    }

    @Override
    public void onStart() {
        super.onStart();
        retrieveUser();
    }

    @Override
    public void onStop() {
        super.onStop();
        reference.removeEventListener(valueEventListenerContacts);
    }

    public void searchContacts(String text){
        List<Usuario> contactsListSearch = new ArrayList<>();
        for (Usuario user : listContacts){
            String name = user.getNome().toLowerCase();
            if (name.contains(text)){
                contactsListSearch.add(user);
            }
        }
        setListFromAdapter(contactsListSearch);
    }

    public void reloadContacts(){
        setListFromAdapter(listContacts);
    }

    private void setListFromAdapter(List<Usuario> list){
        adapterContacts = new AdapterContacts(list, getActivity());
        binding.RecylerViewContacts.setAdapter(adapterContacts);
        adapterContacts.notifyDataSetChanged();
    }

    private void itemGroup(){
        Usuario itemGroup = new Usuario();
        itemGroup.setNome("Novo grupo");
        itemGroup.setEmail("");
        listContacts.add(itemGroup);
    }

    private void recyclerViewSettings(){
        // adapter
        adapterContacts = new AdapterContacts(listContacts, getActivity());

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
        binding.RecylerViewContacts.setLayoutManager(layoutManager);
        binding.RecylerViewContacts.setHasFixedSize(true);
        binding.RecylerViewContacts.setAdapter(adapterContacts);
    }

    private void clickRecyclerView(){
        binding.RecylerViewContacts.addOnItemTouchListener(new RecyclerItemClickListener(
                getActivity(),
                binding.RecylerViewContacts,
                new RecyclerItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        List<Usuario> newListUser = adapterContacts.getListContacts();
                        Usuario selectedUser = newListUser.get(position);

                        Intent i;
                        if (selectedUser.getEmail().isEmpty()){
                            i = new Intent(getActivity(), GroupActivity.class);
                        } else {
                            i = new Intent(getActivity(), ChatActivity.class);
                            i.putExtra("key_user", selectedUser);
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
        ));
    }

    private void retrieveUser(){
        valueEventListenerContacts = reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                clearContactsList();
                for (DataSnapshot data : snapshot.getChildren()){
                    Usuario user = data.getValue(Usuario.class);

                    if (!Objects.equals(currentUser.getEmail(), user.getEmail())){
                        listContacts.add(user);
                    }
                }
                adapterContacts.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void clearContactsList(){
        listContacts.clear();
        itemGroup();
    }
}