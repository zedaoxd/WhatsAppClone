package com.example.whatszapclone.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.example.whatszapclone.R;
import com.example.whatszapclone.config.FirebaseSettings;
import com.example.whatszapclone.databinding.ActivityMainBinding;
import com.example.whatszapclone.fragment.ContactsFragment;
import com.example.whatszapclone.fragment.ConversationsFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.miguelcatalan.materialsearchview.MaterialSearchView;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItemAdapter;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItems;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private FragmentPagerItemAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        toolbarSettings();
        tabSettings();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        searchMaterial(menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.menu_exit:
                singOutUser();
                finish();
                break;
            case R.id.menu_settings:
                openScreen(SettingsActivity.class);
                break;
            case R.id.menu_search:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void searchMaterial(@NonNull Menu menu){
        MenuItem item = menu.findItem(R.id.menu_search);
        binding.include.materialSearchMain.setMenuItem(item);

        binding.include.materialSearchMain.setOnQueryTextListener(new MaterialSearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {

                switch (binding.viewPager.getCurrentItem()) {
                    case 0:
                        ConversationsFragment conversationsFragment = (ConversationsFragment) adapter.getPage(0);
                        if (newText != null && !newText.isEmpty()){
                            conversationsFragment.searchConversation(newText.toLowerCase());
                        } else {
                            conversationsFragment.reloadConversations();
                        }
                        break;
                    case 1:
                        ContactsFragment contactsFragment = (ContactsFragment) adapter.getPage(1);
                        if (newText != null && !newText.isEmpty()){
                            contactsFragment.searchContacts(newText.toLowerCase());
                        } else {
                            contactsFragment.reloadContacts();
                        }
                        break;
                }
                return true;
            }
        });

        binding.include.materialSearchMain.setOnSearchViewListener(new MaterialSearchView.SearchViewListener() {
            @Override
            public void onSearchViewShown() {

            }

            @Override
            public void onSearchViewClosed() {
                ConversationsFragment fragment = (ConversationsFragment) adapter.getPage(0);
                fragment.reloadConversations();
            }
        });
    }

    private void tabSettings(){
        adapter = new FragmentPagerItemAdapter(
                getSupportFragmentManager(),
                FragmentPagerItems.with(this)
                        .add(R.string.aba_conversas, ConversationsFragment.class)
                        .add(R.string.aba_contatos, ContactsFragment.class)
                        .create()
        );

        binding.viewPager.setAdapter(adapter);
        binding.viewPagerTab.setViewPager(binding.viewPager);
    }

    private void singOutUser(){
        try {
            FirebaseAuth auth = FirebaseSettings.getFirebaseAuth();
            auth.signOut();
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    private void openScreen(Class screen){
        Intent intent = new Intent(getApplicationContext(), screen);
        startActivity(intent);
    }

    private void toolbarSettings(){
        binding.include.toolbarMain.setTitle(R.string.app_name);
        setSupportActionBar(binding.include.toolbarMain);
    }
}