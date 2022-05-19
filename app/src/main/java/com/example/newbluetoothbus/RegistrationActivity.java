package com.example.newbluetoothbus;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.example.newbluetoothbus.Models.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


public class RegistrationActivity extends AppCompatActivity {
    private Button BtnReg, BtnAuth, BtnEWA;
    private FirebaseAuth auth;
    private FirebaseDatabase db;
    private DatabaseReference users;
    private DatabaseReference mDataBase;
    private ConstraintLayout root;
    private String USER_KEY = "User";
    FirebaseUser cUser;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.registrationactivity);
        BtnReg = findViewById(R.id.BtnReg);
        BtnAuth = findViewById(R.id.BtnAuth);
        root = findViewById(R.id.root_element);
        final Animation animAlpha = AnimationUtils.loadAnimation(this, R.anim.alpha);
        db = FirebaseDatabase.getInstance("https://newbluetoothbus-default-rtdb.firebaseio.com");
        users = db.getReference(USER_KEY);
        mDataBase = FirebaseDatabase.getInstance("https://newbluetoothbus-default-rtdb.firebaseio.com").getReference(USER_KEY);
        auth = FirebaseAuth.getInstance();

        BtnReg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                view.startAnimation(animAlpha);
                showRegisterWindow();
            }
        });
        BtnAuth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                view.startAnimation(animAlpha);
                showSignInWindow();
            }
        });
    }
    @Override
    protected void onStart(){
        super.onStart();
        cUser = auth.getCurrentUser();
        if(cUser != null)
        {
            startActivity(new Intent(RegistrationActivity.this, MainActivity.class));
            finish();
        }
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
                            return;
                        }
                        if (TextUtils.isEmpty(password_auth.getText().toString())) {
                            showToast("Введите пароль");
                            return;
                        } else if (password_auth.getText().toString().length() < 5) {
                            showToast("Пароль должен содержать 5 или более символов");
                            return;
                        }
                        if (!TextUtils.isEmpty(email_auth.getText().toString()) && !TextUtils.isEmpty(password_auth.getText().toString())) {
                            auth.signInWithEmailAndPassword(email_auth.getText().toString(), password_auth.getText().toString())
                                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                        @Override
                                        public void onComplete(@NonNull Task<AuthResult> task) {
                                            if (task.isSuccessful()) {
                                                showToast("Успех!");
                                                cUser = auth.getCurrentUser();
                                                startActivity(new Intent(RegistrationActivity.this, MainActivity.class));
                                                finish();
                                            }
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    showToast("Ошибка" + e.getMessage());
                                }
                            });
                        }
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
                    return;
                }
                if(TextUtils.isEmpty(email.getText().toString())){
                    showToast("Введите вашу электронную почту");
                    return;
                }
                if(TextUtils.isEmpty(password.getText().toString())){
                    showToast("Введите пароль");
                    return;
                }else if(password.getText().toString().length() < 5){
                    showToast("Пароль может состоять минимум из 5 символов");
                    return;
                }
                if(!TextUtils.isEmpty(email.getText().toString()) && !TextUtils.isEmpty(password.getText().toString())){
                    auth.createUserWithEmailAndPassword(email.getText().toString(),password.getText().toString())
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if(task.isSuccessful()){
                                        User newUser = new User();
                                        newUser.setEmail(email.getText().toString());
                                        newUser.setName(name.getText().toString());
                                        newUser.setPassword(password.getText().toString());
                                        showToast("Пользователь добавлен");
                                        mDataBase.child(String.valueOf(auth.getUid())).setValue(newUser);
                                        startActivity(new Intent(RegistrationActivity.this, MainActivity.class));
                                        finish();
                                    }
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            showToast("Ошибка: " + e.getMessage());
                        }
                    });

                }
            }
        });

        dialog.show();

    }
    private void showToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
    @Override
    public void onBackPressed() {
        // super.onBackPressed();
        openQuitDialog();
    }

    private void openQuitDialog() {
        AlertDialog.Builder quitDialog = new AlertDialog.Builder(
                RegistrationActivity.this);
        quitDialog.setTitle("Вы уверены, что хотите выйти?");

        quitDialog.setPositiveButton("Да", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });

        quitDialog.setNegativeButton("Нет", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // TODO Auto-generated method stub
            }
        });

        quitDialog.show();
    }

}