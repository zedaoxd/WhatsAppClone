package com.example.whatszapclone.activity;

import android.content.Intent;
import android.os.Bundle;

import com.example.whatszapclone.adapter.AdapterContacts;
import com.example.whatszapclone.adapter.SelectedGroupAdapter;
import com.example.whatszapclone.config.FirebaseSettings;
import com.example.whatszapclone.databinding.ActivityGroupBinding;
import com.example.whatszapclone.model.Usuario;
import com.example.whatszapclone.utils.RecyclerItemClickListener;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.view.View;
import android.widget.AdapterView;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.whatszapclone.R;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class GroupActivity extends AppCompatActivity {
    private ActivityGroupBinding binding;
    private AdapterContacts adapter;
    private SelectedGroupAdapter selectedGroupAdapter;
    private List<Usuario> usuarioList = new ArrayList<>();
    private List<Usuario> usuarioListSelected = new ArrayList<>();
    private ValueEventListener valueEventListenerMembers;
    private DatabaseReference usersRef;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityGroupBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        settingsToolbar();

        usersRef = FirebaseSettings.getDatabaseReference().child("usuarios");
        currentUser = FirebaseSettings.getFirebaseAuth().getCurrentUser();

        fab();
        settingsRecyclerViewMembers();
        settingsRecyclerViewSelectedMembers();
    }

    @Override
    protected void onStart() {
        super.onStart();
        retrieveUser();
    }

    @Override
    protected void onStop() {
        super.onStop();
        usersRef.removeEventListener(valueEventListenerMembers);
    }

    private void settingsToolbar(){
        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void updateMembersToolbar(){
        int totalSelected = usuarioListSelected.size();
        int totalGlobal = usuarioList.size() + totalSelected;

        binding.toolbar.setTitle("Novo grupo");
        binding.toolbar.setSubtitle(totalSelected + " de " + totalGlobal + " selecionados");
    }

    private void retrieveUser(){
        valueEventListenerMembers = usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot data : snapshot.getChildren()){
                    Usuario user = data.getValue(Usuario.class);
                    if (!Objects.equals(currentUser.getEmail(), user.getEmail())){
                        usuarioList.add(user);
                    }
                }
                adapter.notifyDataSetChanged();
                updateMembersToolbar();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void settingsRecyclerViewSelectedMembers(){
        selectedGroupAdapter = new SelectedGroupAdapter(usuarioListSelected, getApplicationContext());

        RecyclerView.LayoutManager layoutManagerH = new LinearLayoutManager(
                getApplicationContext(),
                LinearLayoutManager.HORIZONTAL,
                false
        );

        binding.include.recyclerViewSelectedMembers.setLayoutManager(layoutManagerH);
        binding.include.recyclerViewSelectedMembers.setHasFixedSize(true);
        binding.include.recyclerViewSelectedMembers.setAdapter(selectedGroupAdapter);

        clickEventRecyclerViewSelectedMembers();
    }

    private void clickEventRecyclerViewSelectedMembers(){
        binding.include.recyclerViewSelectedMembers.addOnItemTouchListener(
                new RecyclerItemClickListener(
                        getApplicationContext(),
                        binding.include.recyclerViewSelectedMembers,
                        new RecyclerItemClickListener.OnItemClickListener() {
                            @Override
                            public void onItemClick(View view, int position) {
                                Usuario selectedUser = usuarioListSelected.get(position);
                                usuarioListSelected.remove(selectedUser);
                                selectedGroupAdapter.notifyDataSetChanged();
                                usuarioList.add(selectedUser);
                                adapter.notifyDataSetChanged();

                                updateMembersToolbar();
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

    private void settingsRecyclerViewMembers(){
        adapter = new AdapterContacts(usuarioList, getApplicationContext());

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        binding.include.recyclerViewMembers.setLayoutManager(layoutManager);
        binding.include.recyclerViewMembers.setHasFixedSize(true);
        binding.include.recyclerViewMembers.setAdapter(adapter);

        clickEventRecyclerViewMembers();
    }

    private void clickEventRecyclerViewMembers(){
        binding.include.recyclerViewMembers.addOnItemTouchListener(
                new RecyclerItemClickListener(
                        getApplicationContext(),
                        binding.include.recyclerViewMembers,
                        new RecyclerItemClickListener.OnItemClickListener() {
                            @Override
                            public void onItemClick(View view, int position) {
                                Usuario selectedUser = usuarioList.get(position);
                                usuarioList.remove(selectedUser);
                                adapter.notifyDataSetChanged();
                                usuarioListSelected.add(selectedUser);
                                selectedGroupAdapter.notifyDataSetChanged();

                                updateMembersToolbar();
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

    private void fab(){
        binding.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(GroupActivity.this, GroupRegistrationActivity.class);
                i.putExtra("members", (Serializable) usuarioListSelected);
                startActivity(i);
            }
        });
    }
}