package com.example.newbluetoothbus;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.LocationManager;
import android.net.Uri;
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

import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


public class MainActivity extends AppCompatActivity {
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 2;
    private ImageButton bScan;
    private boolean deviceFound = true;
    private BluetoothManager bluetoothManager;
    private BluetoothAdapter bluetoothAdapter;
    private LocationManager location;
    private BluetoothLeScanner bluetoothLeScanner;
    private String s;
    private static final int REQUEST_ENABLE_BT = 0;
    private FirebaseAuth auth;
    private FirebaseUser cUser;
    private FirebaseStorage storage;
    private StorageReference storageReference;
    TextView ConnectionsWindow;
    Button BPay;
    int x = 0;
    ImageButton BRefresh, BtnBT, BtnMap, BtnCard;
    private FirebaseAnalytics mFirebaseAnalytics;
    BluetoothAdapter mBlueAdapter;

    DatabaseReference rootRef = FirebaseDatabase.getInstance
            ("https://newbluetoothbus-default-rtdb.firebaseio.com/").getReference();
    DatabaseReference myRef = rootRef.child("Bus");
    DatabaseReference UserRef = rootRef.child("User");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        auth = FirebaseAuth.getInstance();
        cUser = auth.getCurrentUser();
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final Animation animAlpha = AnimationUtils.loadAnimation(this, R.anim.alpha);
        mBlueAdapter = BluetoothAdapter.getDefaultAdapter();
        bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();

        ConnectionsWindow = findViewById(R.id.Paired);
        BPay = findViewById(R.id.Pay);
        BRefresh = findViewById(R.id.Refresh);
        BtnBT = findViewById(R.id.BtnBT);
        BtnMap = findViewById(R.id.BtnMap);
        BtnCard = findViewById(R.id.BtnCard);



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
                                Object busn = ds.child("busnum").getValue();
                                if(s.equals(bnumber)) {
                                    if (cUser == auth.getCurrentUser()) {
                                        rootRef.child("Check")
                                                    .child(String.valueOf(auth.getUid()))
                                                    .child(String.valueOf(busn))
                                                    .child("Чек от " + dateText + ", " + timeText)
                                                    .child("cost")
                                                    .setValue("33");
                                            ConnectionsWindow.setTextSize(24);
                                            ConnectionsWindow.setText("Маршрут");
                                            bluetoothLeScanner.stopScan(scanCallback);
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
            s = device.getAddress();
            final int rssi = result.getRssi();
            ValueEventListener eventListener = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                        String bnumber = ds.getKey();
                        Object bmac = ds.getValue();
                        String bmacstring = String.valueOf(bmac);

                        Object busn = ds.child("busnum").getValue();
                        Object busph = ds.child("phone").getValue();
                        if(!deviceFound) {
                            ConnectionsWindow.setTextSize(24);
                            ConnectionsWindow.setText("Маршрут");
                            }else{
                                if(s.equals(String.valueOf(bnumber))) {
                                    ConnectionsWindow.setTextSize(18);
                                    ConnectionsWindow.setText("Номер маршрута: " + busn + "\n"
                                            + "Телефон для оплаты: " + "\n" + busph);
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
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, 0, 0, "Запросить историю чеков");
        menu.add(0, 1, 1, "Выйти с аккаунта");
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == 0) {
            rootRef.child("User")
                    .child(String.valueOf(cUser.getUid()))
                    .child("historyRequest")
                    .setValue("1");
            showToast("Чеки были высланы на почту");
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
                ValueEventListener eventListener = new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Object check = dataSnapshot.child(cUser.getUid()).child("last_check").getValue();
                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(String.valueOf(check)));
                        startActivity(browserIntent);
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                };
                UserRef.addListenerForSingleValueEvent(eventListener);
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
        } else {
            showToast("Включите Bluetooth");
        }
    }
}