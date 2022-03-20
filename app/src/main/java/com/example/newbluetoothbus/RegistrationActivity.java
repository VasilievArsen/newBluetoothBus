package com.example.newbluetoothbus;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.example.newbluetoothbus.Models.User;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RegistrationActivity extends AppCompatActivity {
    Button BtnReg, BtnAuth, BtnEWA;
    private FirebaseAuth auth;
    FirebaseDatabase db;
    DatabaseReference users;
    ConstraintLayout root;
    private FirebaseAuth.AuthStateListener mAuthListener;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.registrationactivity);
        BtnReg = findViewById(R.id.BtnReg);
        BtnAuth = findViewById(R.id.BtnAuth);
        BtnEWA = findViewById(R.id.enter_wout_auth);
        root = findViewById(R.id.root_element);

        auth = FirebaseAuth.getInstance();
        db = FirebaseDatabase.getInstance("https://newbluetoothbus-default-rtdb.firebaseio.com/");
        users = db.getReference();

        BtnReg.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                showRegisterWindow();
            }
        });
        BtnAuth.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                showSignInWindow();
            }
        });
        BtnEWA.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(RegistrationActivity.this, MainActivity.class));
            }
        });
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if(user != null){

                }else{

                }
            }
        };
    }

    private void showSignInWindow() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("Авторизация");
        dialog.setMessage("Введите данные для входа");

        LayoutInflater inflater = LayoutInflater.from(this);
        View sign_in_window = inflater.inflate(R.layout.sign_in_window, null);
        dialog.setView(sign_in_window);

        final EditText email_auth = sign_in_window.findViewById(R.id.inputEmail);
        final EditText password_auth = sign_in_window.findViewById(R.id.inputPassword);

        dialog.setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        dialog.setPositiveButton("Вход", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (TextUtils.isEmpty(email_auth.getText().toString())) {
                            showToast("Введите вашу электронную почту");
                            //Snackbar.make(root, "Введите вашу электронную почту", Snackbar.LENGTH_SHORT).show();
                            return;
                        }
                        if (TextUtils.isEmpty(password_auth.getText().toString())) {
                            showToast("Введите пароль");
                            //Snackbar.make(root, "Введите пароль", Snackbar.LENGTH_SHORT).show();
                            return;
                        }else if(password_auth.getText().toString().length() < 5){
                            showToast("Пароль должен содержать 5 или более символов");
                            //Snackbar.make(root, "Пароль должен содержать 5 или более символов", Snackbar.LENGTH_SHORT).show();
                            return;
                        }
                        auth.signInWithEmailAndPassword(email_auth.getText().toString(), password_auth.getText().toString())
                                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                                    @Override
                                    public void onSuccess(AuthResult authResult) {
                                        startActivity(new Intent(RegistrationActivity.this, MainActivity.class));
                                        finish();
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Snackbar.make(root, "Ошибка авторизации" + e.getMessage(), Snackbar.LENGTH_SHORT).show();
                            }
                        });
                    }
                });
            dialog.show();
        }

    private void showRegisterWindow() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("Регистрация");
        dialog.setMessage("Введите данные для регистрации");

        LayoutInflater inflater = LayoutInflater.from(this);
        View register_window = inflater.inflate(R.layout.register_window, null);
        dialog.setView(register_window);

        final EditText name = register_window.findViewById(R.id.inputName);
        final EditText email = register_window.findViewById(R.id.inputEmail);
        final EditText password = register_window.findViewById(R.id.inputPassword);

        dialog.setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        dialog.setPositiveButton("Подтвердить", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if(TextUtils.isEmpty(name.getText().toString())){
                    showToast("Введите ваше имя");
                    //Snackbar.make(root, "Введите ваше имя", Snackbar.LENGTH_SHORT).show();
                    return;
                }
                if(TextUtils.isEmpty(email.getText().toString())){
                    showToast("Введите вашу электронную почту");
                    //Snackbar.make(root, "Введите вашу электронную почту", Snackbar.LENGTH_SHORT).show();
                    return;
                }
                if(TextUtils.isEmpty(password.getText().toString())){
                    showToast("Введите пароль");
                    //Snackbar.make(root, "Введите пароль", Snackbar.LENGTH_SHORT).show();
                    return;
                }else if(password.getText().toString().length() < 5){
                    showToast("Пароль может состоять минимум из 5 символов");
                    //Snackbar.make(root, "Пароль может состоять минимум из 5 символов", Snackbar.LENGTH_SHORT).show();
                    return;
                }
                //Регистрация пользователя
                auth.createUserWithEmailAndPassword(email.getText().toString(), password.getText().toString())
                        .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                            @Override
                            public void onSuccess(AuthResult authResult) {
                                User user = new User();
                                user.setEmail(email.getText().toString());
                                user.setName(name.getText().toString());
                                user.setPassword(password.getText().toString());

                                users.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                        .setValue(user)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {
                                                Snackbar.make(root, "Пользователь добавлен", Snackbar.LENGTH_SHORT).show();
                                            }
                                        });
                            }
                        });

            }
        });

        dialog.show();

    }
    private void showToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}

