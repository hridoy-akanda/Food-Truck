package edmt.dev.androideatitserver;

import android.app.ProgressDialog;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApi;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnSuccessListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import edmt.dev.androideatitserver.Common.Common;
import edmt.dev.androideatitserver.Common.DirectionJSONParser;
import edmt.dev.androideatitserver.Remote.IGeoCoordinates;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TrackingOrder extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener

{



    private GoogleMap mMap;
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST=1000;
    private final static int LOCATION_PERMISSION_REQUEST=1001;

    private Location mLastLocation;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;

    private static int UPDATE_INTERVAL=1000;
    private static int FATEST_INTERVAL=5000;
    private static int DISPLACEMENT=10;


    private IGeoCoordinates mService;
    private FusedLocationProviderClient mFusedLocationClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tracking_order);

        mService= Common.getGeoCodeService();
        if(ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION)!= PackageManager.PERMISSION_GRANTED
                ){
            requestRuntimePermission();
        }else{
            if(checkPlayServices()){
                buildGoogleApiClient();
                createLocationRequest();
            }
            mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        }

        displayLocation();

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    private void displayLocation() {
        if(ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION)!= PackageManager.PERMISSION_GRANTED
                ){
            requestRuntimePermission();
        }else{
            //mLastLocation=LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            mFusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location mLastLocation) {
                            // Got last known location. In some rare situations this can be null.
                            if(mLastLocation!=null){
                                double latitude=mLastLocation.getLatitude();
                                double longitude=mLastLocation.getLongitude();
                                //Add Marker
                                LatLng yourLocation=new LatLng(latitude,longitude);
                                mMap.addMarker(new MarkerOptions().position(yourLocation).title("Your Location"));
                                mMap.moveCamera(CameraUpdateFactory.newLatLng(yourLocation));
                                mMap.animateCamera(CameraUpdateFactory.zoomTo(17.0f));

                                //After adding marker for your location, add marker for this order
                                drawRoute(yourLocation,Common.currentRequest.getAddress());

                            }else{
                                //Toast.makeText(TrackingOrder.this, "Couldn't get this location", Toast.LENGTH_SHORT).show();
                                Log.d("DEBUG","Couldn't get the location");
                            }
                        }
                    });

            /*if(mLastLocation!=null){
                double latitude=mLastLocation.getLatitude();
                double longitude=mLastLocation.getLongitude();
                //Add MArker in your location and move the camera
                LatLng yourLocation= new LatLng(latitude, longitude);
                mMap.addMarker(new MarkerOptions().position(yourLocation).title("Your Location"));
                mMap.moveCamera(CameraUpdateFactory.newLatLng(yourLocation));
                mMap.animateCamera(CameraUpdateFactory.zoomTo(17.0f));
            }*/
        }
    }

    private void drawRoute(final LatLng yourLocation, String address) {
        mService.getGeoCode(address).enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                try {
                    JSONObject jsonObject=new JSONObject(response.body().toString());
                    String lat=((JSONArray)jsonObject.get("results"))
                            .getJSONObject(0)
                            .getJSONObject("geometry")
                            .getJSONObject("location")
                            .get("lat").toString();
                    String lng=((JSONArray)jsonObject.get("results"))
                            .getJSONObject(0)
                            .getJSONObject("geometry")
                            .getJSONObject("location")
                            .get("lng").toString();

                    LatLng orderLocation= new LatLng(Double.parseDouble(lat),Double.parseDouble(lng));
                    Bitmap bitmap= BitmapFactory.decodeResource(getResources(),R.drawable.box);

                    bitmap= Common.scaleBitmap(bitmap,70,70);
                    MarkerOptions marker= new MarkerOptions().icon(BitmapDescriptorFactory.fromBitmap(bitmap))
                            .title("Order of "+Common.currentRequest.getPhone())
                            .position(orderLocation);
                    mMap.addMarker(marker);
                    //Draw route
                    mService.getDirections(yourLocation.latitude+","+yourLocation.longitude,
                            orderLocation.latitude+","+orderLocation.longitude)
                            .enqueue(new Callback<String>() {
                                @Override
                                public void onResponse(Call<String> call, Response<String> response) {
                                    new ParserTask().execute(response.body().toString());
                                }

                                @Override
                                public void onFailure(Call<String> call, Throwable t) {

                                }
                            });


                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {

            }
        });
    }

    private void createLocationRequest() {
        mLocationRequest=  new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FATEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setSmallestDisplacement(DISPLACEMENT);
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient= new GoogleApiClient.Builder(this)
        .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build();
        mGoogleApiClient.connect();
    }

    private boolean checkPlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();

        int resultCode= GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GoogleApiAvailability.getInstance().isUserResolvableError(resultCode)) {

                apiAvailability.getErrorDialog(this,resultCode, PLAY_SERVICES_RESOLUTION_REQUEST).show();

            } else {
                Toast.makeText(this, "This device is not supported", Toast.LENGTH_SHORT).show();
                finish();
            }
            return false;
        }
        return true;
    }

    private void requestRuntimePermission() {
        ActivityCompat.requestPermissions(this, new String[]{
            android.Manifest.permission.ACCESS_COARSE_LOCATION,
            android.Manifest.permission.ACCESS_FINE_LOCATION
        },LOCATION_PERMISSION_REQUEST);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults){
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch(requestCode){
            case LOCATION_PERMISSION_REQUEST:
                if(grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                    if(checkPlayServices()){
                        buildGoogleApiClient();
                        createLocationRequest();
                        displayLocation();
                    }
                }
                break;
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

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

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        displayLocation();
        startLocationUpdates();
    }

    private void startLocationUpdates() {

        if(ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION)!= PackageManager.PERMISSION_GRANTED
                ){
            return;
        }
         LocationServices.getFusedLocationProviderClient(this);

    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
    @Override
    public void onLocationChanged(Location location) {
        mLastLocation=location;
        displayLocation();
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkPlayServices();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(mGoogleApiClient!=null)
            mGoogleApiClient.connect();
    }

    private class ParserTask extends AsyncTask<String,Integer,List<List<HashMap<String,String>>>>{
        //Depreciated
        ProgressDialog mDialog=new ProgressDialog(TrackingOrder.this);

        @Override
        protected void onPreExecute(){
            super.onPreExecute();
            mDialog.setMessage("Please waiting...");
            mDialog.show();
        }
        @Override
        protected List<List<HashMap<String,String>>> doInBackground(String... strings) {
            JSONObject jObject;
            List<List<HashMap<String,String>>> routes=null;
            try{
                jObject = new JSONObject(strings[0]);
                DirectionJSONParser parser= new DirectionJSONParser();

                routes=parser.parse(jObject);

            } catch (JSONException e) {
                e.printStackTrace();
            }
            return routes;
        }
        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>>list){
            //super.onPostExecute(lists);
            mDialog.dismiss();
            ArrayList points=null;
            PolylineOptions lineOptions= null;
            for(int i=0;i<list.size();i++){
                points= new ArrayList();
                lineOptions = new PolylineOptions();

                List<HashMap<String,String>> path=list.get(i);
                for(int k=0;k<path.size();k++){
                    HashMap<String,String>point =path.get(k);
                    double lat=Double.parseDouble(point.get("lat"));
                    double lng=Double.parseDouble(point.get("lng"));
                    LatLng position= new LatLng(lat,lng);
                    points.add(position);
                }

                lineOptions.addAll(points);
                lineOptions.width(12);
                lineOptions.color(Color.BLUE);
                lineOptions.geodesic(true);
            }
            mMap.addPolyline(lineOptions);
        }

    }
}
