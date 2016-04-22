package com.example.johnpconsidine.blemap;

import android.Manifest;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.johnpconsidine.transmit.Loc;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;

public class MapActivity extends AppCompatActivity {
    private static final String TAG = MapActivity.class.getSimpleName();
    private GoogleMap mMap;
    Button bleButton;
    Button networkButton;
    MyReceiver receiver;
    private MapView mMapView;

    /***** Ble Scan code for test ******/
    /***************** start *********************/

    private BluetoothLeScanner mBluetoothLeScanner;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothLeAdvertiser mBluetoothLeAdvertiser;
    private void startScan() {

        //the filter settings
        ScanFilter ResultsFilter = new ScanFilter.Builder()
                //.setDeviceAddress(string)
                //.setDeviceName(string)
                //.setManufacturerData()
                //.setServiceData()
                //.setServiceUuid(Loc.LOC_SERVICE)
                .build();

        ArrayList<ScanFilter> filters = new ArrayList<ScanFilter>();
        filters.add(ResultsFilter);

        //scan settings
        ScanSettings settings = new ScanSettings.Builder()
                //.setCallbackType() //int
                //.setMatchMode(ScanSettings.MATCH_MODE_AGGRESSIVE) //AGGRESSIVE, STICKY  //require API 23
                //.setNumOfMatches(ScanSettings.MATCH_NUM_MAX_ADVERTISEMENT) //ONE, FEW, MAX  //require API 23
                //.setReportDelay(0) //0: no delay; >0: queue up
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY) //LOW_POWER, BALANCED, LOW_LATENCY
                .build();

        if(mBluetoothLeScanner != null) {
            mBluetoothLeScanner.startScan(filters, settings, mScanCallback);
        }
    }

    private void stopScan() {
        mBluetoothLeScanner.stopScan(mScanCallback);
    }

    @Override
    protected void onResume() {
        super.onResume();
        //make sure bluetooth
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    1);

        }
    }

    //Get the scan callback and process results
    private ScanCallback mScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            Log.d(TAG, "onScanResult");
            processResult(result);
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            Log.d(TAG, "onBatchScanResults: "+results.size()+" results");
            for (ScanResult result : results) {
                processResult(result);
            }
        }

        @Override
        public void onScanFailed(int errorCode) {
            Log.w(TAG, "LE Scan Failed: "+errorCode);
        }

        private void processResult(ScanResult result) {

            //Log.i(TAG, String.valueOf(System.currentTimeMillis())+result);
            //LocRes locres = new LocRes(result.getScanRecord());
            In_Marker in_marker = new In_Marker(result.getScanRecord());
            Log.v(TAG, "lat is " + in_marker.getLatitude() + " long is " + in_marker.getLongitude());
            mMap.addMarker(new MarkerOptions()
                    .position(new LatLng((double) in_marker.getLatitude(), (double) in_marker.getLongitude()))
                    .title("user_id is: " + in_marker.getUser_id() + " event type is: " + in_marker.getEvent_type()));

        }
    };
    /***** Ble Scan code for test **/
    /***************** end *********************/


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        //initialize braod cast receiver
        IntentFilter filter = new IntentFilter(MyReceiver.ACTION_RESP);
        filter.addCategory(Intent.CATEGORY_DEFAULT);
        receiver = new MyReceiver();
        registerReceiver(receiver, filter);

        /************* start ble scan, always on ****************/

        BluetoothManager manager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
        mBluetoothAdapter = manager.getAdapter();
        mBluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();
        mBluetoothLeAdvertiser = mBluetoothAdapter.getBluetoothLeAdvertiser();

        // it's also okay to put startScan here !!
        //startScan();
        /************** start ble scan, always on ***************/




        //initialize buttons
        bleButton = (Button) findViewById(R.id.bleButton);
        networkButton = (Button) findViewById(R.id.networkButton);

        ((SupportMapFragment) getSupportFragmentManager().findFragmentById(
                R.id.mapview)).getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                mMap = googleMap;
                CameraPosition cameraPosition = new CameraPosition.Builder()
                        //USe south bends coordinates since most of the pins are here
                        .target(new LatLng(41.6764, -86.2520))      // Sets the center of the map to location user
                        .zoom(10)
                        .build();
                mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                    @Override
                    public void onMapClick(LatLng latLng) {
                        mMap.addMarker(new MarkerOptions()
                        .position(latLng));

                        //advertise
                        byte[] Lat = FloatToArray((float)latLng.latitude);
                        byte[] Long = FloatToArray((float) latLng.longitude);

                        byte event_type = 0x7f;
                        String user_id = "78dsYj2a!_8jwe";// 14 bytes
                        
                        byte[] event_type_byte = new byte[1];
                        event_type_byte [0] = event_type;
                        byte[] user_id_byte = user_id.getBytes();

                        byte[] Loc_Ad = ArrayConcat(Lat, Long);
                        byte[] User_Ad = ArrayConcat(event_type_byte, user_id_byte);
                        byte[] Basic_toSend = ArrayConcat(Loc_Ad, User_Ad);

                        Log.v(TAG, "loc in byte " + Loc_Ad);
                        restartAdvertising(Basic_toSend);


                    }
                });

            }
        });


        networkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AlertDialog.Builder decideMode = new AlertDialog.Builder(MapActivity.this);
                decideMode.setTitle("Data Type");
                decideMode.setMessage("How would you like to receive locations?");
                decideMode.setNegativeButton("Via Network", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(MapActivity.this, TransmitIntentService.class);
                        startService(intent);
                        Toast.makeText(MapActivity.this, "Network pins received", Toast.LENGTH_SHORT).show();
                    }
                });
                decideMode.setPositiveButton("Via BLE", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.v(TAG, "SCANNING");
                        stopScan();
                        startScan();
                    }
                });
                AlertDialog alert = decideMode.create();
                alert.show();



            }
        });


        /**************************** the click button will trigger the start of ble Scan *************************************/
        bleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopScan();
                //create dialog button with inputs:
                AlertDialog.Builder builder = new AlertDialog.Builder(MapActivity.this);
                builder.setTitle("New Pin");
                builder.setMessage("Enter your location");
                //set edit texts on the dialog
                LinearLayout layout = new LinearLayout(MapActivity.this);
                final EditText latinput = new EditText(MapActivity.this);
                latinput.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL);
                latinput.setHint("Latitude");
                final EditText loninput = new EditText(MapActivity.this);
                loninput.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL);
                loninput.setHint("Longitude");
                layout.addView(latinput);
                layout.addView(loninput);

                builder.setView(layout);

                builder.setPositiveButton("Enter", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.v(TAG, loninput.getText().toString() + latinput.getText().toString());
                    }
                });
                AlertDialog alert = builder.create();
                alert.show();






                Log.v(TAG, "lat is " + "41.6764" + "long is "+ "-86.2520");
                byte[] Lat = FloatToArray(40.23f);
                byte[] Long = FloatToArray(-86.2520f);
                byte[] Loc_Ad = ArrayConcat(Lat, Long);
                Log.v(TAG, "loc in byte " + Loc_Ad);

                restartAdvertising(Loc_Ad);
               //creating Dialog with Text
            }
        });








//        ADVERTISING CODE



    } /*end on create */
    public void startAdvertising(byte[] data_out){ //the input is byte[] array, and any double should be transformed
        if (mBluetoothLeAdvertiser == null) return;

        AdvertiseSettings settings = new AdvertiseSettings.Builder()
                .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY) //3 modes: LOW_POWER, BALANCED, LOW_LATENCY
                .setConnectable(false)
                .setTimeout(0)
                .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_MEDIUM) // ULTRA_LOW, LOW, MEDIUM, HIGH
                .build();

        AdvertiseData data = new AdvertiseData.Builder()
                .setIncludeDeviceName(false)
                .setIncludeTxPowerLevel(false)
                .addServiceUuid(Loc.LOC_SERVICE)
                .addServiceData(Loc.LOC_SERVICE,data_out)
                .build();

        mBluetoothLeAdvertiser.startAdvertising(settings, data, mAdvertiseCallback);
    }

//    ADVERTISING CALLBACK
private AdvertiseCallback mAdvertiseCallback = new AdvertiseCallback() {
        @Override
        public void onStartSuccess(AdvertiseSettings settingsInEffect) {
            Log.i(TAG, "LE Advertise Started.");
        }

        @Override
        public void onStartFailure(int errorCode) {
            Log.w(TAG, "LE Advertise Failed: " + errorCode);
        }
    };
    public void stopAdvertising() {
        if (mBluetoothLeAdvertiser == null) return;

        mBluetoothLeAdvertiser.stopAdvertising(mAdvertiseCallback);
    }

    public void restartAdvertising(byte[] data_out) {
        stopAdvertising();
        startAdvertising(data_out);
    }
//    Array operators
    public static byte[] FloatToArray(float Value) {
        int accum = Float.floatToRawIntBits(Value);
        byte[] byteRet = new byte[4];
        byteRet[0] = (byte)(accum & 0xFF);
        byteRet[1] = (byte)((accum>>8) & 0xFF);
        byteRet[2] = (byte)((accum >> 16) & 0xFF);
        byteRet[3] = (byte)((accum>> 24) & 0xFF);
        return byteRet;
    }

    //concatenate two byte[] array of latitude and byte[] array of longitude.
    public static byte[] ArrayConcat(byte[] Lat, byte[] Long){
        byte[] ret = new byte[Lat.length + Long.length];

        System.arraycopy(Lat, 0, ret, 0, Lat.length);
        System.arraycopy(Long, 0, ret, Lat.length, Long.length);
        return ret;
    }
    public class MyReceiver extends BroadcastReceiver {

        public static final String ACTION_RESP =
                "com.example.johnpconsidine.blemap.MESSAGE_PROCESSED";


        @Override
        public void onReceive(Context context, Intent intent) {
            for (Loc location : Application.getmPlaces()) {
                Log.v(TAG, "the lat is " + location.getLatitude());
                mMap.addMarker(new MarkerOptions()
                        .position(new LatLng((double) location.getLatitude(), (double) location.getLongitude()))
                        .title("no notes"));
                // Log.v(TAG, "The lat is " + location.getParseGeoPoint(Utils.PLACE_OBJECT_LOCATION).getLatitude());
            }

        }
    }





//    ADVERTISING CODE

}
