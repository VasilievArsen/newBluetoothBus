package com.example.newbluetoothbus;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class CheckActivity extends AppCompatActivity {
    TextView checkView;
    private FirebaseAuth auth;
    private FirebaseUser cUser;

    DatabaseReference rootRef = FirebaseDatabase.getInstance("https://newbluetoothbus-default-rtdb.firebaseio.com/").getReference();
    DatabaseReference RefUser = rootRef.child("User");
    DatabaseReference RefCheck = rootRef.child("Check");
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check);
        auth = FirebaseAuth.getInstance();
        cUser = auth.getCurrentUser();
        checkView = findViewById(R.id.checkView);

            ValueEventListener eventListener = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                        String dname = ds.getKey();
                        Object fbmac = ds.getValue();
                        String fbmacstring = String.valueOf(fbmac);
                        if(String.valueOf(auth.getUid()).equals(dname)){
                            checkView.setText(fbmacstring);
                        }else{
                            checkView.setText(dname + " " +String.valueOf(auth.getUid()));
                        }
                    }
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            };
            RefCheck.addListenerForSingleValueEvent(eventListener);
    }
    private void showToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}