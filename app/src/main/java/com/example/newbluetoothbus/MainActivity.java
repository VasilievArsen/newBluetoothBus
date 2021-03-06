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
    BluetoothAdapter mBlueAdapter;
    private LocationManager locationManager;
    public static boolean geolocationEnabled = false;
    private BluetoothLeScanner bluetoothLeScanner;
    private String s;
    private static final int REQUEST_ENABLE_BT = 0;
    private FirebaseAuth auth;
    private FirebaseUser cUser;
    private FirebaseStorage storage;
    private FirebaseAnalytics mFirebaseAnalytics;
    private StorageReference storageReference;
    TextView ConnectionsWindow;
    Button BPay;
    ImageButton BRefresh, BtnBT, BtnMap, BtnCard;

    DatabaseReference rootRef = FirebaseDatabase.getInstance
            ("https://newbluetoothbus-default-rtdb.firebaseio.com/").getReference();
    DatabaseReference myRef = rootRef.child("Bus");
    DatabaseReference UserRef = rootRef.child("User");

    @SuppressLint("MissingPermission")
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

        bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();
        
        if(bluetoothAdapter == null || !bluetoothAdapter.isEnabled()){
            Intent enableBT = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBT, 0);
        }
        
        mBlueAdapter = BluetoothAdapter.getDefaultAdapter();
        bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();

        ConnectionsWindow = findViewById(R.id.Paired);
        BPay = findViewById(R.id.Pay);
        BRefresh = findViewById(R.id.Refresh);
        BtnBT = findViewById(R.id.BtnBT);
        BtnMap = findViewById(R.id.BtnMap);
        BtnCard = findViewById(R.id.BtnCard);

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
        // ?????????????? ??????????
        Date currentDate = new Date();
        // ???????????????????????????? ?????????????? ?????? "????????.??????????.??????"
        DateFormat dateFormat = new SimpleDateFormat("dd:MM:yyyy", Locale.getDefault());
        String dateText = dateFormat.format(currentDate);
        // ???????????????????????????? ?????????????? ?????? "????????:????????????:??????????????"
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
                                if (bnumber != null) {
                                    if ( s != null && s.equals(bnumber) && ConnectionsWindow != null) {
                                        if (cUser == auth.getCurrentUser()) {
                                            rootRef.child("Check")
                                                    .child(String.valueOf(auth.getUid()))
                                                    .child(String.valueOf(busn))
                                                    .child("?????? ???? " + dateText + ", " + timeText)
                                                    .child("33")
                                                    .setValue("cost");
                                            bluetoothLeScanner.stopScan(scanCallback);
                                            openPayBuilder();
                                        }
                                    }
                                }
                            }
                        }
                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                        }
                    };
                    myRef.addListenerForSingleValueEvent(eventListener);
                }else{
                    showToast("???????????????? bluetooth");
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
                    deviceFound = false;
                    if(!deviceFound){
                        ConnectionsWindow.setTextSize(24);
                        ConnectionsWindow.setText("??????????????");
                    }
                } else {
                    ConnectionsWindow.setTextSize(24);
                    ConnectionsWindow.setText("??????????????");
                    showToast("???????????????? Bluetooth");
                }
            }

        });

        BtnBT.setOnClickListener(new OnClickListener() {
            @SuppressLint("MissingPermission")
            @Override
            public void onClick(View view) {
                if (!mBlueAdapter.isEnabled()) {
                    showToast("???????????????? Bluetooth...");
                    Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(intent, REQUEST_ENABLE_BT);
                    BtnBT.setImageResource(R.drawable.ic_bluetoothoff);
                } else {
                    if (mBlueAdapter.isEnabled()) {
                        mBlueAdapter.disable();
                        BtnBT.setImageResource(R.drawable.ic_bluetoothoff);
                        showToast("?????????????????? Bluetooth");
                        showToast("Bluetooth ????????????????");
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
                        Object busn = ds.child("busnum").getValue();
                        Object busph = ds.child("phone").getValue();
                        if(s.equals(String.valueOf(bnumber)) && rssi < 70) {
                            ConnectionsWindow.setTextSize(18);
                            ConnectionsWindow.setText("?????????? ????????????????: " + busn + "\n"
                                    + "?????????????? ?????? ????????????: " + "\n" + busph);
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
        menu.add(0, 0, 0, "?????????????????? ?????????????? ??????????");
        menu.add(0, 1, 1, "?????????? ?? ????????????????");
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == 0) {
            rootRef.child("History_requests")
                    .child(String.valueOf(cUser.getUid()))
                    .setValue("1");
            showToast("???????? ???????? ?????????????? ???? ??????????");
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
        quitDialog.setTitle("???? ??????????????, ?????? ???????????? ???????????");
        quitDialog.setPositiveButton("????", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        quitDialog.setNegativeButton("??????", new DialogInterface.OnClickListener() {
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
        quitFADialog.setTitle("???? ??????????????, ?????? ???????????? ?????????? ?? ?????????????????");
        quitFADialog.setPositiveButton("????", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                startActivity(new Intent(MainActivity.this, RegistrationActivity.class));
                auth.signOut();
                finish();
            }
        });
        quitFADialog.setNegativeButton("??????", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        });
        quitFADialog.show();
    }

    private void openPayBuilder() {
        AlertDialog.Builder paymentDialog = new AlertDialog.Builder(
                MainActivity.this);
        paymentDialog.setTitle("????????????");
        paymentDialog.setMessage("???????????? ?????????????????? ??????????????.");
        paymentDialog.setPositiveButton("????", new DialogInterface.OnClickListener() {
            @SuppressLint("MissingPermission")
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                startActivity(getIntent());
                finish();
                overridePendingTransition(0, 0);
            }
        });

        paymentDialog.setNegativeButton("???????????????? ??????", new DialogInterface.OnClickListener() {
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
                    showToast("Bluetooth ??????????????");
                } else {
                    showToast("???? ?????????????? ???????????????? Bluetooth");
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public boolean isGeoDisabled() {
        LocationManager mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        boolean mIsGPSEnabled = mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean mIsNetworkEnabled = mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        boolean mIsGeoDisabled = !mIsGPSEnabled && !mIsNetworkEnabled;
        return mIsGeoDisabled;
    }
    private void showToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    @SuppressLint("MissingPermission")
    protected void onStart() {
        super.onStart();
        if(!isGeoDisabled()){
            if (mBlueAdapter.isEnabled()) {
                bluetoothLeScanner = mBlueAdapter.getBluetoothLeScanner();
                bluetoothLeScanner.startScan(scanCallback);
            } else {
                showToast("???????????????? Bluetooth");
            }
        }else{
            showToast("????????????????????, ???????????????? ????????????????????");
            startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
        }
    }
}