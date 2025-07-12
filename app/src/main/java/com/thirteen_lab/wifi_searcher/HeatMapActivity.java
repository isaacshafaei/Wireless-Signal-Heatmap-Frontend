package com.thirteen_lab.wifi_searcher;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.thirteen_lab.wifi_searcher.config.Constants;
import com.thirteen_lab.wifi_searcher.utls.heat_map.*;
import com.thirteen_lab.wifi_searcher.utls.heat_map.GridOverlayView;

import java.util.ArrayList;
import java.util.List;
import android.net.Uri;
import androidx.annotation.Nullable;
import com.google.gson.Gson;
import android.graphics.Bitmap;
import java.io.ByteArrayOutputStream;
import android.util.Base64;
import org.json.JSONObject;
import org.json.JSONException;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.Request;
import com.android.volley.toolbox.Volley;
import android.os.Build;
import android.Manifest;
import android.content.pm.PackageManager;
import java.io.File;
import android.os.Environment;
import android.media.MediaScannerConnection;
import java.io.IOException;
import java.io.FileOutputStream;
import java.io.IOException;



public class HeatMapActivity extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener,
        AdapterView.OnItemSelectedListener {

    private static final String TAG = HeatMapActivity.class.getSimpleName();
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 1000;

    private Location mLastLocation;
    private GoogleApiClient mGoogleApiClient;
    private boolean mRequestingLocationUpdates = false;
    private boolean measurementStarted = false;  // <-- Track if measurement started
    private LocationRequest mLocationRequest;

    private WifiDetails wifiDetails;
    private WifiManager wifiManager;

    private Spinner wifiNetworksSpinner;
    private ViewGroup gridViewFrameLayout;
    private com.thirteen_lab.wifi_searcher.utls.heat_map.GridView gridView;
    private HeatmapOverlayView heatmapOverlayView;

    private ArrayAdapter<WifiNetwork> wifiNetworksDataAdapter;
    private final MainData mainData = new MainData();
    private boolean overlayEnabled = false;

    private static final int PICK_IMAGE_REQUEST = 1;
    private ImageView floorPlanImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_heat_map);
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            }
        }


        floorPlanImage = findViewById(R.id.floorPlanImage); // Use the class-level variable

        floorPlanImage.setOnClickListener(v -> {
            gridView.setVisibility(View.VISIBLE);
        });

        // Optional: Add a button to pick the image
        Button pickImageButton = findViewById(R.id.pickImageButton);
        pickImageButton.setOnClickListener(v -> openImagePicker());

        Button saveButton = findViewById(R.id.saveButton);
        saveButton.setOnClickListener(v -> captureAndSaveHeatmap());


        gridView = findViewById(R.id.gridView);
        wifiNetworksSpinner = findViewById(R.id.wifiNetworksSpinner);
        gridViewFrameLayout = findViewById(R.id.gridViewFrameLayout);
        heatmapOverlayView = findViewById(R.id.heatmapOverlay);

        wifiNetworksSpinner.setOnItemSelectedListener(this);
        wifiNetworksDataAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, new ArrayList<>());
        wifiNetworksDataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        wifiNetworksSpinner.setAdapter(wifiNetworksDataAdapter);


        // Important: Set pixel size of grid AFTER layout has been drawn.
        floorPlanImage.post(() -> {
            int widthPx = floorPlanImage.getWidth();
            int heightPx = floorPlanImage.getHeight();
            if (mainData.getGridInfo() != null) {
                mainData.getGridInfo().setPixelSize(widthPx, heightPx);
                Log.d(TAG, "GridInfo pixel size set: " + widthPx + "x" + heightPx);
            }
        });

        // Setup Google API client and location request
        if (checkPlayServices()) {
            buildGoogleApiClient();
            createLocationRequest();
        }

        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        wifiDetails = new ViewModelProvider(this).get(WifiDetails.class);
        wifiDetails.scanWifi(wifiManager);

        gridView.setMainData(mainData);

        Button scanButton = findViewById(R.id.scanButton);
        scanButton.setOnClickListener(v -> {
            CellPosition tappedCell = heatmapOverlayView.getTappedCell();
            if (tappedCell == null) {
                Toast.makeText(this, "Please tap on the map first", Toast.LENGTH_SHORT).show();
                return;
            }

            WifiNetwork selectedNetwork = (WifiNetwork) wifiNetworksSpinner.getSelectedItem();
            if (selectedNetwork == null) {
                Toast.makeText(this, "Select a WiFi network", Toast.LENGTH_SHORT).show();
                return;
            }

            SignalGrid signalGrid = mainData.getSignalGrids().get(selectedNetwork);
            GridInfo gridInfo = mainData.getGridInfo();

            if (signalGrid == null || gridInfo == null) {
                Toast.makeText(this, "Missing data", Toast.LENGTH_SHORT).show();
                return;
            }
            int signalLevel = getCurrentSignalLevel();
            signalGrid.addSignal(tappedCell, signalLevel);
            overlayEnabled = true;
            heatmapOverlayView.setData(gridInfo, signalGrid.getCells());
            measurementStarted = true;

            Toast.makeText(this, "Signal added at tapped location", Toast.LENGTH_SHORT).show();
        });

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.heatMapWifi);
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.accessPoints:
                    startActivity(new Intent(getApplicationContext(), AccessPointsActivity.class));
                    overridePendingTransition(0, 0);
                    return true;
                case R.id.profile:
                    startActivity(new Intent(getApplicationContext(), ProfileActivity.class));
                    overridePendingTransition(0, 0);
                    return true;
                case R.id.heatMapWifi:
                    return true;
            }
            return false;
        });



        measurementStarted = false;

//        FrameLayout frameLayoutGrid = findViewById(R.id.gridViewFrameLayout);
//        GridOverlayView gridViewSqure = new GridOverlayView(this);
//
//        frameLayoutGrid.addView(gridViewSqure);
    }

    private void captureAndSaveHeatmap() {
        FrameLayout heatmapLayout = findViewById(R.id.gridViewFrameLayout);
        heatmapLayout.setDrawingCacheEnabled(true);
        Bitmap bitmap = Bitmap.createBitmap(heatmapLayout.getDrawingCache());
        heatmapLayout.setDrawingCacheEnabled(false);

        // Save to Pictures directory
        String filename = "heatmap_" + System.currentTimeMillis() + ".png";
        File directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File file = new File(directory, filename);

        try {
            FileOutputStream out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.flush();
            out.close();

            // Make it appear in Gallery
            MediaScannerConnection.scanFile(this,
                    new String[]{file.getAbsolutePath()},
                    new String[]{"image/png"},
                    null);

            Toast.makeText(this, "Heatmap saved to gallery", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to save heatmap", Toast.LENGTH_SHORT).show();
        }
    }


    private void saveHeatmapData() {
        WifiNetwork selectedNetwork = (WifiNetwork) wifiNetworksSpinner.getSelectedItem();
        if (selectedNetwork == null || floorPlanImage.getDrawable() == null) {
            Toast.makeText(this, "Select a network and floor plan first", Toast.LENGTH_SHORT).show();
            return;
        }

        SignalGrid signalGrid = mainData.getSignalGrids().get(selectedNetwork);
        GridInfo gridInfo = mainData.getGridInfo();

        if (signalGrid == null || gridInfo == null) {
            Toast.makeText(this, "Heatmap data missing", Toast.LENGTH_SHORT).show();
            return;
        }

        // Serialize using Gson
        Gson gson = new Gson();
        String gridJson = gson.toJson(gridInfo);
        String signalJson = gson.toJson(signalGrid);

        // Convert image to Base64
        floorPlanImage.setDrawingCacheEnabled(true);
        Bitmap bitmap = Bitmap.createBitmap(floorPlanImage.getDrawingCache());
        floorPlanImage.setDrawingCacheEnabled(false);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        String imageBase64 = Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);

        // Send to backend (pseudo call)
        sendHeatmapToServer(selectedNetwork.getSsid(), gridJson, signalJson, imageBase64);
    }

    private void sendHeatmapToServer(String ssid, String gridInfoJson, String signalGridJson, String imageBase64) {
        String url = Constants.Base_URL+"/heatmap/save"; // replace with your real URL

        JSONObject payload = new JSONObject();
        try {
            payload.put("user_id", 1);  // replace with real user ID
            payload.put("ssid", ssid);
            payload.put("grid_info", new JSONObject(gridInfoJson));
            payload.put("signal_grid", new JSONObject(signalGridJson));
            payload.put("floor_plan_image", imageBase64);
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                url,
                payload,
                response -> Toast.makeText(this, "Heatmap saved!", Toast.LENGTH_SHORT).show(),
                error -> Toast.makeText(this, "Save failed: " + error.getMessage(), Toast.LENGTH_LONG).show()
        );

        Volley.newRequestQueue(this).add(request);
    }


    private String getConnectedSSID(Context context) {
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        if (wifiManager != null && wifiManager.getConnectionInfo() != null) {
            String ssid = wifiManager.getConnectionInfo().getSSID();
            if (ssid != null && ssid.length() > 1 && ssid.startsWith("\"") && ssid.endsWith("\"")) {
                ssid = ssid.substring(1, ssid.length() - 1); // Remove quotes
            }
            return ssid;
        }
        return null;
    }


    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();
            floorPlanImage.setImageURI(imageUri);
        }
    }


    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        WifiNetwork wifiNetwork = (WifiNetwork) parent.getItemAtPosition(position);
        SignalGrid grid = mainData.getSignalGrids().get(wifiNetwork);

// Always clear before updating, to reset previous heatmap and tap
        heatmapOverlayView.clearData();

        if (measurementStarted && grid != null && mainData.getGridInfo() != null && !grid.getCells().isEmpty()) {
            heatmapOverlayView.setData(mainData.getGridInfo(), grid.getCells());
            Log.d(TAG, "Heatmap updated for selected network: " + wifiNetwork.getSsid());
        }


        gridView.update(wifiNetwork, null);
    }


    @Override
    public void onNothingSelected(AdapterView<?> parent) {}

    @Override
    protected void onStart() {
        super.onStart();
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkPlayServices();
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            startLocationUpdates();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopLocationUpdates();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    private int getCurrentSignalLevel() {
        List<ScanResult> scanResults = wifiManager.getScanResults();
        WifiNetwork selected = (WifiNetwork) wifiNetworksSpinner.getSelectedItem();
        if (selected == null) return -100;

        for (ScanResult result : scanResults) {
            if (result.SSID.equals(selected.getSsid())) {
                int level = result.level;
                int normalized = Math.max(0, Math.min(100, 2 * (level + 100))); // -100 dBm → 0, -50 dBm → 100
                return normalized;
            }
        }

        return -100;
    }



    /**
     * This method is triggered on every location update.
     * It performs measurements and updates heatmap accordingly.
     */
    private void doMeasurement() {
        if (mLastLocation == null) {
            Log.w(TAG, "Location not available yet for measurement.");
            return;
        }

        // Start measurement only once when location and wifi scans are ready
        if (!measurementStarted) {
            mainData.startMeasurement(mLastLocation);
            measurementStarted = true;
            Log.d(TAG, "Measurement started at location: " + mLastLocation);
        }

        List<ScanResult> scanResults = wifiDetails.getScanResults();
        List<WifiNetwork> discoveredNetworks = new ArrayList<>();

        boolean added = mainData.addMeasurement(mLastLocation, scanResults, discoveredNetworks);
        if (added) {
            updateWifiNetworksSpinner(discoveredNetworks);
            String selectedSSID = getConnectedSSID(this);
            if (selectedSSID != null) {
                for (int i = 0; i < wifiNetworksDataAdapter.getCount(); i++) {
                    WifiNetwork network = wifiNetworksDataAdapter.getItem(i);
                    if (network != null && selectedSSID.equals(network.getSsid())) {
                        wifiNetworksSpinner.setSelection(i);
                        Log.d("HeatmapActivity", "Selected WiFi in spinner: " + selectedSSID);
                        break;
                    }
                }
            }
            WifiNetwork selectedNetwork = (WifiNetwork) wifiNetworksSpinner.getSelectedItem();
            updateHeatmap(selectedNetwork);
//            gridView.update(selectedNetwork, null);
            Log.d(TAG, "Measurement added and heatmap updated.");
        } else {
            Log.w(TAG, "Measurement not added (possibly duplicate data).");
        }
    }

    private void updateHeatmap(WifiNetwork network) {
        if (network == null) return;
        SignalGrid grid = mainData.getSignalGrids().get(network);
        if (grid == null) return;
        heatmapOverlayView.setData(mainData.getGridInfo(), grid.getCells());
    }

    private void displayLocation() {
        try {
            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        } catch (SecurityException e) {
            e.printStackTrace();
        }

    }

    private void updateWifiNetworksSpinner(List<WifiNetwork> discoveredNetworks) {
        if (discoveredNetworks.isEmpty()) return;

        boolean newNetworksAdded = false;
        for (WifiNetwork network : discoveredNetworks) {
            if (wifiNetworksDataAdapter.getPosition(network) != -1) {
                continue; // Already in adapter
            }
            wifiNetworksDataAdapter.add(network);
            newNetworksAdded = true;
        }

        if (newNetworksAdded) {
            runOnUiThread(() -> wifiNetworksDataAdapter.notifyDataSetChanged());
        }
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    private void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    protected void startLocationUpdates() {
        try {
            LocationServices.FusedLocationApi.requestLocationUpdates(
                    mGoogleApiClient, mLocationRequest, this);
            mRequestingLocationUpdates = true;
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(
                mGoogleApiClient, this);
        mRequestingLocationUpdates = false;
    }

    @Override
    public void onConnected(Bundle bundle) {
        displayLocation();
        startLocationUpdates();
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.w(TAG, "GoogleApiClient connection suspended");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.e(TAG, "GoogleApiClient connection failed: " + connectionResult.getErrorMessage());
    }

    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;
        displayLocation();
        wifiDetails.scanWifi(wifiManager);
        doMeasurement();
    }

    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this, PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Log.i(TAG, "This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }
}
