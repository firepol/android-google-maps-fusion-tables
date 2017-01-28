package com.github.firepol.fusionmap;

import android.content.pm.PackageManager;
import android.location.Location;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.fusiontables.Fusiontables;
import com.google.api.services.fusiontables.FusiontablesScopes;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MapsActivity extends FragmentActivity
        implements OnMapReadyCallback, ConnectionCallbacks, OnConnectionFailedListener {

    protected static final String TAG = "MapsActivity";

    private static final LatLng mDefaultLatLng = new LatLng(47.411497, 8.544184);
    private GoogleApiClient mGoogleApiClient;
    private GoogleMap mMap;
    private Location mLastLocation;

    // Google API client stuff
    final HttpTransport transport = AndroidHttp.newCompatibleTransport();
    final JsonFactory jsonFactory = GsonFactory.getDefaultInstance();

    GoogleAccountCredential credential;
    Fusiontables client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        buildGoogleApiClient();

        prepareFusion();
    }


    /**
     * Builds a GoogleApiClient. Uses the addApi() method to request the LocationServices API.
     */
    protected synchronized void buildGoogleApiClient() {
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        }
    }


    protected void prepareFusion() {
        // Normally READONLY should be enough (see credential with one scope), but I checked online a console
        // and I could see a public table only if I would grant both permissions
        List<String> scopes = new ArrayList<>(Arrays.asList(FusiontablesScopes.FUSIONTABLES, FusiontablesScopes.FUSIONTABLES_READONLY));
        credential = GoogleAccountCredential.usingOAuth2(this, scopes);
        //credential = GoogleAccountCredential.usingOAuth2(this, Collections.singleton(FusiontablesScopes.FUSIONTABLES_READONLY));

        // TODO : get account name automatically
        // http://stackoverflow.com/questions/35789071/getting-the-gmail-id-of-the-user-in-android-6-0-marshmallow
        credential.setSelectedAccountName("YOUR_GOOGLE_ACCOUNT");

        // Calendar client
        client = new Fusiontables.Builder(
                transport, jsonFactory, credential).setApplicationName("TestMap/1.0")
                .build();

        try {
            String tableId = "1774o_WcrqSQlepLXlz1kgH_01NpCJ-6OyId9Pm1J";

            Fusiontables.Query.Sql sql = client.query().sql("SELECT FileName,Name,Location FROM " + tableId);
            //sql.execute();
            //java.lang.IllegalStateException: Calling this from your main thread can lead to deadlock

            Fusiontables.Table.Get table = client.table().get(tableId);
            table.setFields("items(FileName,Name,Location)");
            //table.execute();

            // TODO : can't execute like this on main thread as the documentation example "suggests"
            //https://developers.google.com/api-client-library/java/google-api-java-client/android

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean fetchLocation() {

        if (ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling request permission: https://developer.android.com/training/permissions/requesting.html
            return true;
        }

        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

        if (mLastLocation != null) {
            // This makes sense for an app full of POIs everywhere in the world
            // Else it's better to center the app on a specific area
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                    new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()), 12));
        } else {
            Toast.makeText(this, "No location detected. Make sure location is enabled on the device.", Toast.LENGTH_LONG).show();
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                    mDefaultLatLng, 7));
        }

        return false;
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker over Zurich Oerlikon, Switzerland.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker to Zurich Oerlikon and move the camera
        mMap.addMarker(new MarkerOptions().position(mDefaultLatLng).title("Zurich Oerlikon"));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                mDefaultLatLng, 13));

        // TODO: add fusion tables POIs
    }


    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }


    @Override
    protected void onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    /**
     * Runs when a GoogleApiClient object successfully connects.
     */
    @Override
    public void onConnected(Bundle connectionHint) {
        Log.i(TAG, "onConnected");

        fetchLocation();
    }


    @Override
    public void onConnectionFailed(ConnectionResult result) {
        // Refer to the javadoc for ConnectionResult to see what error codes might be returned in
        // onConnectionFailed.
        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = " + result.getErrorCode());
    }


    @Override
    public void onConnectionSuspended(int cause) {
        // The connection to Google Play services was lost for some reason. We call connect() to
        // attempt to re-establish the connection.
        Log.i(TAG, "Connection suspended");
        mGoogleApiClient.connect();
    }
}
