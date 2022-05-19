package com.example.newbluetoothbus;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
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

import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


public class MainActivity extends AppCompatActivity {

    private static final int ACCESS_COARSE_LOCATION_REQUEST = 2;
    private ImageButton bScan;
    private boolean deviceFound;
    private BluetoothManager bluetoothManager;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothLeScanner bluetoothLeScanner;
    private static final int PERMISSION_REQUEST_FINE_LOCATION = 2;
    private String a;
    private static final int REQUEST_ENABLE_BT = 0;
    private FirebaseAuth auth;
    private FirebaseUser cUser;
    TextView ConnectionsWindow;
    Button BPay;
    int x = 0;
    ImageButton BRefresh, BtnBT, BtnMap, BtnCard;
    private FirebaseAnalytics mFirebaseAnalytics;
    BluetoothAdapter mBlueAdapter;

    DatabaseReference rootRef = FirebaseDatabase.getInstance("https://newbluetoothbus-default-rtdb.firebaseio.com/").getReference();
    DatabaseReference myRef = rootRef.child("Bus");
    DatabaseReference myRef2 = rootRef.child("Check");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        auth = FirebaseAuth.getInstance();
        cUser = auth.getCurrentUser();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final Animation animAlpha = AnimationUtils.loadAnimation(this, R.anim.alpha);
        ScanSettings scanSettings = new ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                .setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES)
                .setMatchMode(ScanSettings.MATCH_MODE_AGGRESSIVE)
                .setNumOfMatches(ScanSettings.MATCH_NUM_ONE_ADVERTISEMENT)
                .setReportDelay(0L)
                .build();


        mBlueAdapter = BluetoothAdapter.getDefaultAdapter();
        bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();

        ConnectionsWindow = findViewById(R.id.Paired);
        //ConnectionsWindow.setText(getResources().getString(R.string.busSearch));
        BPay = findViewById(R.id.Pay);
        BRefresh = findViewById(R.id.Refresh);
        BtnBT = findViewById(R.id.BtnBT);
        BtnMap = findViewById(R.id.BtnMap);
        BtnCard = findViewById(R.id.BtnCard);

        if (ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSION_REQUEST_FINE_LOCATION);
        }
        bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();

        BtnCard.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, CardActivity.class);
                startActivity(intent);
            }
        });

        BtnMap.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, MapActivity.class);
                startActivity(intent);
            }
        });
        // Текущее время
        Date currentDate = new Date();
        // Форматирование времени как "день.месяц.год"
        DateFormat dateFormat = new SimpleDateFormat("dd:MM:yyyy", Locale.getDefault());
        String dateText = dateFormat.format(currentDate);
        // Форматирование времени как "часы:минуты:секунды"
        DateFormat timeFormat = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
        String timeText = timeFormat.format(currentDate);

        BPay.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mBlueAdapter.isEnabled()) {
                    ValueEventListener eventListener = new ValueEventListener() {
                        @SuppressLint("MissingPermission")
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            for (DataSnapshot ds : dataSnapshot.getChildren()) {
                                String bnumber = ds.getKey();
                                Object bmac = ds.getValue();
                                String bmacstring = String.valueOf(bmac);
                                if (ConnectionsWindow.getText().toString().contains(bmacstring)) {
                                    if (cUser == auth.getCurrentUser()) {
                                        rootRef.child("Check")
                                                .child(String.valueOf(auth.getUid()))
                                                .child(bnumber)
                                                .child("Чек от " +dateText +", " +timeText )
                                                .child("cost")
                                                .setValue("33");

                                        bluetoothLeScanner.stopScan(scanCallback);
                                        ConnectionsWindow.setTextSize(24);
                                        ConnectionsWindow.setText("Маршрут");
                                        openPayBuilder();
                                    }
                                }
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                        }
                    };
                    myRef.addListenerForSingleValueEvent(eventListener);
                }
            }
        });

        BRefresh.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("MissingPermission")
            @Override
            public void onClick(View v) {
                v.startAnimation(animAlpha);
                if (mBlueAdapter.isEnabled()) {
                    bluetoothLeScanner = mBlueAdapter.getBluetoothLeScanner();
                    bluetoothLeScanner.startScan(scanCallback);
                    if(!deviceFound){
                        ConnectionsWindow.setTextSize(24);
                        ConnectionsWindow.setText("Маршрут");
                    }
                } else {
                    showToast("Включите Bluetooth");
                }
            }

        });

        BtnBT.setOnClickListener(new OnClickListener() {
            @SuppressLint("MissingPermission")
            @Override
            public void onClick(View view) {
                if (!mBlueAdapter.isEnabled()) {
                    showToast("Включаем Bluetooth...");
                    Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(intent, REQUEST_ENABLE_BT);
                    BtnBT.setImageResource(R.drawable.ic_bluetoothoff);
                } else {
                    if (mBlueAdapter.isEnabled()) {
                        mBlueAdapter.disable();
                        BtnBT.setImageResource(R.drawable.ic_bluetoothoff);
                        showToast("Выключаем Bluetooth");
                        showToast("Bluetooth выключен");
                        BtnBT.setImageResource(R.drawable.ic_bluetoothon);
                    }
                }
            }
        });
    }

    ScanCallback scanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            final BluetoothDevice device = result.getDevice();
            final String s = device.getAddress();
            final int rssi = result.getRssi();
            ValueEventListener eventListener = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                        String bnumber = ds.getKey();
                        Object bmac = ds.getValue();
                        String bmacstring = String.valueOf(bmac);
                        //showToast("" + s);
                        if (s.equals(bmacstring) && rssi < 100) {
                            ConnectionsWindow.setTextSize(18);
                            ConnectionsWindow.setText("Номер маршрута: " + bnumber + "\n" + "маршрут: " + bmacstring);
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            };
            myRef.addListenerForSingleValueEvent(eventListener);
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, 0, 0, "Чеки");
        menu.add(0, 1, 1, "Выйти с аккаунта");
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == 0) {
            Intent intent = new Intent(this, CheckActivity.class);
            startActivity(intent);
        }
        if (item.getItemId() == 1) {
            openQuitFADialog();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
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

    private void openQuitFADialog() {
        AlertDialog.Builder quitFADialog = new AlertDialog.Builder(
                MainActivity.this);
        quitFADialog.setTitle("Вы уверены, что хотите выйти с аккаунта?");
        quitFADialog.setPositiveButton("Да", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                startActivity(new Intent(MainActivity.this, RegistrationActivity.class));
                auth.signOut();
                finish();
            }
        });
        quitFADialog.setNegativeButton("Нет", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });

        quitFADialog.show();
    }

    private void openPayBuilder() {
        AlertDialog.Builder paymentDialog = new AlertDialog.Builder(
                MainActivity.this);
        paymentDialog.setTitle("Оплата");
        paymentDialog.setMessage("Оплата произошла успешно.");
        paymentDialog.setPositiveButton("Ок", new DialogInterface.OnClickListener() {
            @SuppressLint("MissingPermission")
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                startActivity(getIntent());
                finish();
                overridePendingTransition(0, 0);
            }
        });
        paymentDialog.setNegativeButton("Показать чек", new DialogInterface.OnClickListener() {
            @SuppressLint("MissingPermission")
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                startActivity(new Intent(MainActivity.this, CheckActivity.class));
            }
        });
        paymentDialog.show();
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

    @SuppressLint("MissingPermission")
    protected void onStart() {
        super.onStart();
        if (mBlueAdapter.isEnabled()) {
            bluetoothLeScanner = mBlueAdapter.getBluetoothLeScanner();
            bluetoothLeScanner.startScan(scanCallback);
            deviceFound = false;
        } else {
            showToast("Включите Bluetooth");
        }
    }

    @SuppressLint("MissingPermission")
    protected void onDestroy() {
        super.onDestroy();
        //mBlueAdapter.disable();
    }
}