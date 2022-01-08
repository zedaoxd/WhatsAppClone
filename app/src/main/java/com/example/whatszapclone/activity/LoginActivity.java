package com.example.whatszapclone.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.example.whatszapclone.R;
import com.example.whatszapclone.config.FirebaseSettings;
import com.example.whatszapclone.databinding.ActivityLoginBinding;
import com.example.whatszapclone.model.Usuario;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;

import java.util.Objects;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = FirebaseSettings.getFirebaseAuth();

        clickTextCadastro();
        clickButtonLogin();
    }

    @Override
    protected void onStart() {
        super.onStart();

        isThereAnyUser();
    }

    private void isThereAnyUser(){
        FirebaseUser user = auth.getCurrentUser();
        if (user != null){
            openScreen(MainActivity.class);
        }
    }

    private void clickTextCadastro(){
        binding.idTextCadastrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openScreen(CadastroActivity.class);
            }
        });
    }

    private void loginUser(Usuario user){
        auth.signInWithEmailAndPassword(user.getEmail(), user.getSenha())
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()){
                            toastMessage("logado com sucesso");
                            openScreen(MainActivity.class);
                        } else {
                            String message;
                            try {
                                throw Objects.requireNonNull(task.getException());
                            } catch (FirebaseAuthInvalidUserException e){
                                message = "Usuário não cadastrado";
                            } catch (FirebaseAuthInvalidCredentialsException e){
                                message = "E-mail ou senha incorretos";
                            } catch (Exception e){
                                message = "Erro ao logar usuario";
                            }
                            toastMessage(message);
                        }
                    }
                });
    }

    private void clickButtonLogin(){
        binding.buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = binding.emailLogin.getText().toString();
                String senha = binding.senhaLogin.getText().toString();
                if (emptyFields(email, senha)){
                    toastMessage("um ou mais campos vazios");
                    return;
                }
                Usuario user = new Usuario();
                user.setEmail(email);
                user.setSenha(senha);
                loginUser(user);
            }
        });
    }

    private void openScreen(Class screen){
        Intent intent = new Intent(getApplicationContext(), screen);
        startActivity(intent);
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