package com.example.newbluetoothbus;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


public class MainActivity extends AppCompatActivity {

    private ImageButton bScan;
    private boolean deviceFound;
    private BluetoothManager bluetoothManager;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothLeScanner bluetoothLeScanner;
    private static final int PERMISSION_REQUEST_FINE_LOCATION = 2;
    private String a;
    private static final int REQUEST_ENABLE_BT = 0;

    TextView ConnectionsWindow;
    Button BPay;
    ImageButton BRefresh;
    private FirebaseAnalytics mFirebaseAnalytics;
    BluetoothAdapter mBlueAdapter;
    DatabaseReference rootRef = FirebaseDatabase.getInstance("https://newbluetoothbus-default-rtdb.firebaseio.com/").getReference();
    DatabaseReference myRef = rootRef.child("World");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mBlueAdapter = BluetoothAdapter.getDefaultAdapter();
        bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();

        ConnectionsWindow = findViewById(R.id.Paired);
        //ConnectionsWindow.setText(getResources().getString(R.string.busSearch));
        BPay = findViewById(R.id.Pay);
        BRefresh = findViewById(R.id.Refresh);

        if (ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSION_REQUEST_FINE_LOCATION);
        }
        bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();

        BPay.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ConnectionsWindow.getText() == getString(R.string.busSearch)) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setTitle("Ошибка");
                    builder.setMessage("Маршрут не найден.");
                    builder.setPositiveButton("OK", null);
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
                myRef.setValue("Hello");

                myRef.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DataSnapshot> task) {
                        if (!task.isSuccessful()) {
                            //Log.e("firebase", "Error getting data", task.getException());
                            showToast("oshibka");
                        }
                        else {
                            ConnectionsWindow.setText(String.valueOf(task.getResult().getValue()));
                            showToast("firebase" + " " + String.valueOf(task.getResult().getValue()));
                        }
                    }
                });
            }
        });

        BRefresh.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mBlueAdapter.isEnabled()) {
                    bluetoothLeScanner = mBlueAdapter.getBluetoothLeScanner();
                    //bluetoothLeScanner.startScan(scanCallback);
                    deviceFound = false;
                } else {
                    showToast("Включите Bluetooth");
                }
            }

        });
    }

    @Override
    public void onBackPressed() {
        // super.onBackPressed();
        openQuitDialog();
    }

    private void openQuitDialog() {
        AlertDialog.Builder quitDialog = new AlertDialog.Builder(
                MainActivity.this);
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

    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        switch (requestCode) {
            case REQUEST_ENABLE_BT:
                if (resultCode == RESULT_OK) {
                    showToast("Bluetooth включен");
                } else {
                    showToast("Не удалось включить Bluetooth");
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void showToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    protected void onStart() {
        super.onStart();
        if (mBlueAdapter.isEnabled()) {
            bluetoothLeScanner = mBlueAdapter.getBluetoothLeScanner();
            //bluetoothLeScanner.startScan(scanCallback);
            deviceFound = false;
        } else {
            showToast("Включите Bluetooth");
        }
    }
}