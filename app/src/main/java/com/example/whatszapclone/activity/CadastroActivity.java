package com.example.whatszapclone.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.whatszapclone.R;
import com.example.whatszapclone.config.FirebaseSettings;
import com.example.whatszapclone.databinding.ActivityCadastroBinding;
import com.example.whatszapclone.model.Usuario;
import com.example.whatszapclone.utils.Base64Custom;
import com.example.whatszapclone.utils.UserFirebase;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Objects;

public class CadastroActivity extends AppCompatActivity {

    private ActivityCadastroBinding binding;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro);

        binding = ActivityCadastroBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.progressBar.setVisibility(View.GONE);

        clickButtonCadastrar();
    }

    private void clickButtonCadastrar(){
        binding.buttonCadastrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                binding.progressBar.setVisibility(View.VISIBLE);
                String nome = binding.textNome.getText().toString();
                String email = binding.textEmail.getText().toString();
                String senha = binding.textSenha.getText().toString();

                if (emptyFields(nome, email, senha)){
                    toastMessage("Existem um ou mais campos vazios");
                    return;
                }
                Usuario user = new Usuario(nome, email, senha);
                saveUserFirebase(user);
            }
        });
    }

    private void saveUserFirebase(Usuario user){
        auth = FirebaseSettings.getFirebaseAuth();
        auth.createUserWithEmailAndPassword(user.getEmail(), user.getSenha())
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()){
                            saveDataFirebase(user);
                            UserFirebase.updateUserName(user.getNome());
                            toastMessage("Usuário cadastrado com sucesso");
                            finish();
                        } else {
                            String excecao;
                            try {
                                throw task.getException();
                            } catch (FirebaseAuthWeakPasswordException e){
                                excecao = "Digite uma senha mais forte!";
                            } catch (FirebaseAuthInvalidCredentialsException e) {
                                excecao = "Por favor, digite um e-mail válido";
                            } catch (FirebaseAuthUserCollisionException e){
                                excecao = "Esta conta já existe no banco de dados";
                            } catch (Exception e){
                                excecao = "Erro ao cadastrar usuário";
                            }
                            toastMessage("Erro: " + excecao);
                        }
                    }
                });
    }

    private void saveDataFirebase(Usuario user){
        try {
            String userIdentifier = Base64Custom.encodeBase64(user.getEmail());
            user.setId(userIdentifier);
            DatabaseReference reference = FirebaseSettings.getDatabaseReference();
            DatabaseReference users = reference.child("usuarios").child(userIdentifier);
            users.setValue(user);
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    private boolean emptyFields(String... texts){
        for(String text : texts){
            if (text.isEmpty()){
                return true;
            }
        }
        return false;
    }

    private void toastMessage(String message){
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
    }
}