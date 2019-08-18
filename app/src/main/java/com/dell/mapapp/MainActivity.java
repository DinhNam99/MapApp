package com.dell.mapapp;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;

import uk.co.deanwild.materialshowcaseview.MaterialShowcaseSequence;
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseView;
import uk.co.deanwild.materialshowcaseview.ShowcaseConfig;

public class MainActivity extends ActivityBase implements LocationListener {

    GoogleMap map;
    EditText edSearch;
    TextView tvLocation;
    Button locate;
    ImageView done,changeMap1,changeMap2;
    RelativeLayout layoutLocation;
    RelativeLayout layoutSearch;

    LatLng myLocal = null;
    LatLng searchLocal = null;

    LatLng pk = new LatLng(20.988779, 105.792433);
    LatLng hn = new LatLng(10.780252, 106.660185);

    private static final String MYTAG = "MYTAG";
    public static final int REQUEST_ID_ACCESS_COURSE_FINE_LOCATION = 100;
    private static final String SHOWCASE_ID = "Sequence Showcase";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        innit();


    }
    private void innit(){
        edSearch = findViewById(R.id.edSearch);
        layoutLocation = findViewById(R.id.layoutLoction);
        layoutSearch = findViewById(R.id.layoutseach);
        tvLocation = findViewById(R.id.tvLocation);
        done = findViewById(R.id.cancel);
        locate = findViewById(R.id.myLocal);
        changeMap1 = findViewById(R.id.changeMap1);
        changeMap2 = findViewById(R.id.changeMap2);
        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                layoutLocation.setVisibility(View.INVISIBLE);
                edSearch.setText("");
                map.clear();
                showMyLocation();
            }
        });
        edSearch.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if(actionId == EditorInfo.IME_ACTION_SEARCH){
                    layoutLocation.setVisibility(View.VISIBLE);
                    searchAddress(edSearch.getText()+"");
                    searchLocal = getSearchLocation(edSearch.getText()+"");
                    tvLocation.setText(edSearch.getText() + "");
                }
                return false;
            }
        });

        showDialogLoading();
        final SupportMapFragment mapFragment
                = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);

        mapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(final GoogleMap googleMap) {
                map = googleMap;
                map.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
                    @Override
                    public void onMapLoaded() {
                        dismissDialog();
                        map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                        map.getUiSettings().setZoomControlsEnabled(true);
                        map.getUiSettings().setMyLocationButtonEnabled(false);
                        askPermissionsAndShowMyLocation();
                        tutorialApp(400);
                    }
                });


                if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                    return;
                }
                map.setMyLocationEnabled(true);

            }
        });
        //change type map
        changeMap1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                map.setMapType(GoogleMap.MAP_TYPE_HYBRID);
            }
        });
        changeMap2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
            }
        });

        locate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMyLocation();
                //tutorialApp(500);
            }
        });


    }
    private void tutorialApp(int millis){
        ShowcaseConfig config = new ShowcaseConfig();
        config.setDelay(millis);

        MaterialShowcaseSequence sequence = new MaterialShowcaseSequence(MainActivity.this, SHOWCASE_ID);

        sequence.setConfig(config);

        sequence.addSequenceItem(locate, "Show your location", "Next");

        sequence.addSequenceItem(
                new MaterialShowcaseView.Builder(MainActivity.this)
                        .setTarget(layoutSearch)
                        .setDismissText("OK")
                        .setContentText("Find location")
                        .withRectangleShape(true)
                        .build()
        );
        sequence.start();
    }
    private void askPermissionsAndShowMyLocation() {

        if (Build.VERSION.SDK_INT >= 23) {
            int accessCoarsePermission
                    = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);
            int accessFinePermission
                    = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);


            if (accessCoarsePermission != PackageManager.PERMISSION_GRANTED
                    || accessFinePermission != PackageManager.PERMISSION_GRANTED) {

                String[] permissions = new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION};

                ActivityCompat.requestPermissions(this, permissions,
                        REQUEST_ID_ACCESS_COURSE_FINE_LOCATION);

                return;
            }
        }
        this.showMyLocation();
    }
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        //
        switch (requestCode) {
            case REQUEST_ID_ACCESS_COURSE_FINE_LOCATION: {

                if (grantResults.length > 1
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED
                        && grantResults[1] == PackageManager.PERMISSION_GRANTED) {

                    Toast.makeText(this, "Permission granted!", Toast.LENGTH_LONG).show();

                    this.showMyLocation();
                }
                else {
                    Toast.makeText(this, "Permission denied!", Toast.LENGTH_LONG).show();
                }
                break;
            }
        }
    }
    private String getEnabledLocationProvider() {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        Criteria criteria = new Criteria();

        String bestProvider = locationManager.getBestProvider(criteria, true);

        boolean enabled = locationManager.isProviderEnabled(bestProvider);

        if (!enabled) {
            Toast.makeText(this, "No location provider enabled!", Toast.LENGTH_LONG).show();
            Log.i(MYTAG, "No location provider enabled!");
            return null;
        }
        return bestProvider;
    }

    private void showMyLocation() {
        LatLng latLng = getMyLoacation();
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(latLng)
                .zoom(20)
                .bearing(60)
                .tilt(40)
                .build();
        map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

        // add Marker for Map:
        MarkerOptions option = new MarkerOptions();
        option.title("My Location");
        option.snippet("....");
        option.position(latLng);
        option.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
        Marker currentMarker = map.addMarker(option);
        Log.e("MAKER",currentMarker.getPosition().toString());
        currentMarker.showInfoWindow();
    }

    private LatLng getMyLoacation(){
        LatLng latLng = null;
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        String locationProvider = this.getEnabledLocationProvider();

        if (locationProvider == null) {
            return null;
        }

        final long MIN_TIME_BW_UPDATES = 1000;

        final float MIN_DISTANCE_CHANGE_FOR_UPDATES = 1;

        Location myLocation = null;
        try {
            locationManager.requestLocationUpdates(
                    locationProvider,
                    MIN_TIME_BW_UPDATES,
                    MIN_DISTANCE_CHANGE_FOR_UPDATES, (LocationListener) this);

            myLocation = locationManager
                    .getLastKnownLocation(locationProvider);
        }

        catch (SecurityException e) {
            Toast.makeText(this, "Show My Location Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            Log.e(MYTAG, "Show My Location Error:" + e.getMessage());
            e.printStackTrace();
            return null;
        }

        if (myLocation != null) {

            latLng = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());
            return latLng;
        } else {
            Toast.makeText(this, "Location not found!", Toast.LENGTH_LONG).show();
            Log.i(MYTAG, "Location not found");
            return null;
        }
    }
    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    //Search address
    private void searchAddress(String location){
        map.clear();
        LatLng latLng = getSearchLocation(location);
        if(latLng!=null){
            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(latLng)
                    .zoom(15)
                    .bearing(90)
                    .tilt(40)
                    .build();
            map.addMarker(new MarkerOptions().position(latLng).title(location));
            map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

        }else{
            Toast.makeText(MainActivity.this,"Please enter your query!!!",Toast.LENGTH_SHORT).show();
            layoutLocation.setVisibility(View.INVISIBLE);
        }
    }

    private LatLng getSearchLocation(String location){
        List<Address> addressList = null;
        LatLng latLng = null;

        if (location != null || !location.equals("")) {
            Geocoder geocoder = new Geocoder(this);
            try {
                addressList = geocoder.getFromLocationName(location, 1);

            } catch (IOException e) {
                e.printStackTrace();
            }
            if(addressList!=null) {
                Address address = addressList.get(0);
                latLng = new LatLng(address.getLatitude(), address.getLongitude());
                return latLng;

            }else{
                return  null;
            }
        }else{
            Toast.makeText(MainActivity.this,"Please enter your query!!!",Toast.LENGTH_SHORT).show();
            layoutLocation.setVisibility(View.INVISIBLE);
            return null;
        }
    }


}