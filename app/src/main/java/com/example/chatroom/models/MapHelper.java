package com.example.chatroom.models;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Point;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;
import android.view.animation.LinearInterpolator;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.example.chatroom.DirectionsJSONParser;
import com.example.chatroom.MainActivity;
import com.example.chatroom.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MapHelper {

    public static final int REQUEST_LOCATION = 72;

    FusedLocationProviderClient mFusedLocationClient;

    MainActivity activity;

    ArrayList<LatLng> mMarkerPoints = new ArrayList<>();

    LocationManager lm;
    LocationCallback mLocationCallback;
    private Polyline mPolyline;

    public MapHelper(MainActivity activity) {
        this.activity = activity;
        lm = (LocationManager) this.activity.getSystemService(Context.LOCATION_SERVICE);
    }

    public void requestLocationPerms() {
        ActivityCompat.requestPermissions(this.activity, new String[]{
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);
    }

    public boolean hasLocationPerms() {
        return ActivityCompat.checkSelfPermission(this.activity, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this.activity, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    public void stopUpdates() {
        if (mFusedLocationClient != null && mLocationCallback != null) {
            mFusedLocationClient.removeLocationUpdates(mLocationCallback);
        }
    }

    public void animateMarker(GoogleMap mMap, final Marker marker, final LatLng toPosition,
                              final boolean hideMarker) {
        final Handler handler = new Handler();
        final long start = SystemClock.uptimeMillis();
        Projection proj = mMap.getProjection();
        Point startPoint = proj.toScreenLocation(marker.getPosition());
        final LatLng startLatLng = proj.fromScreenLocation(startPoint);
        final long duration = 500;

        final LinearInterpolator interpolator = new LinearInterpolator();

        handler.post(new Runnable() {
            @Override
            public void run() {
                long elapsed = SystemClock.uptimeMillis() - start;
                float t = interpolator.getInterpolation((float) elapsed
                        / duration);
                double lng = t * toPosition.longitude + (1 - t)
                        * startLatLng.longitude;
                double lat = t * toPosition.latitude + (1 - t)
                        * startLatLng.latitude;
                marker.setPosition(new LatLng(lat, lng));
                if (t < 1.0) {
                    handler.postDelayed(this, 16);
                } else {
                    if (hideMarker) {
                        marker.setVisible(false);
                    } else {
                        marker.setVisible(true);
                    }
                }
            }
        });
    }

    public Marker addMarker(GoogleMap mMap, LatLng point) {
        if (mMarkerPoints.size() > 1) {
            mMarkerPoints.clear();
            mMap.clear();
        }

        mMarkerPoints.add(point);

        MarkerOptions options = new MarkerOptions();

        options.position(point);

        if (mMarkerPoints.size() == 1) {
            options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
        } else if (mMarkerPoints.size() == 2) {
            options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
        }

        Marker marker = mMap.addMarker(options);
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(point, 15);
        mMap.animateCamera(cameraUpdate);

        if (mMarkerPoints.size() >= 2) {
            drawRoute(mMap, mMarkerPoints.get(0), mMarkerPoints.get(1));
        }

        return marker;
    }

    @SuppressLint("MissingPermission")
    public boolean getLastLocation(ILastLocation callback) {
        if (callback == null) {
            callback = new ILastLocation() {
                @Override
                public void onFetch(double lat, double longi) {
                }

                @Override
                public void onUpdate(double lat, double longi) {
                }

                @Override
                public boolean stopAfterOneUpdate() {
                    return true;
                }
            };
        }
        if (hasLocationPerms()) {
            if (isLocationEnabled()) {
                mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this.activity);
                ILastLocation finalCallback = callback;
                mFusedLocationClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        Location location = task.getResult();
                        if (location == null) {
                            requestNewLocationData(finalCallback);
                        } else {
                            finalCallback.onFetch(location.getLatitude(), location.getLongitude());
                        }
                    }
                });
                return true;
            } else {
                sendLocOffMessage();
            }
        } else {
            requestLocationPerms();
        }
        return false;
    }

    public void sendLocOffMessage() {
        Toast.makeText(this.activity, "Please turn your location on!", Toast.LENGTH_SHORT).show();
    }

    @SuppressLint("MissingPermission")
    private void requestNewLocationData(ILastLocation callback) {
        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(20);
        mLocationRequest.setFastestInterval(20);
        if (callback.stopAfterOneUpdate())
            mLocationRequest.setNumUpdates(1);
        mLocationCallback = new LocationCallback() {
            public void onLocationResult(LocationResult locationResult) {
                Location location = locationResult.getLastLocation();
                callback.onUpdate(location.getLatitude(), location.getLongitude());
            }
        };
        mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
    }

    public boolean isLocationEnabled() {
        return lm.isProviderEnabled(LocationManager.GPS_PROVIDER) || lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    public void drawRoute(GoogleMap nMap, LatLng mOrigin, LatLng mDestination) {
        String url = getDirectionsUrl(mOrigin, mDestination);
        DownloadTask downloadTask = new DownloadTask(nMap, this.activity);
        downloadTask.execute(url);
    }

    private String getDirectionsUrl(LatLng origin, LatLng dest) {
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;
        String key = "key=" + this.activity.getString(R.string.google_maps_key);
        String parameters = str_origin + "&" + str_dest + "&" + key;
        String output = "json";
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters;
        return url;
    }

    private String downloadUrl(String strUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(strUrl);

            urlConnection = (HttpURLConnection) url.openConnection();

            urlConnection.connect();

            iStream = urlConnection.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

            StringBuffer sb = new StringBuffer();

            String line = "";
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

            data = sb.toString();

            br.close();

        } catch (Exception e) {
            Log.d("Exception on download", e.toString());
        } finally {
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }

    public interface ILastLocation {

        void onFetch(double lat, double longi);

        void onUpdate(double lat, double longi);

        boolean stopAfterOneUpdate();

    }

    private class DownloadTask extends AsyncTask<String, Void, String> {

        GoogleMap nMap;

        MainActivity activity;

        DownloadTask(GoogleMap nMap, MainActivity activity) {
            this.nMap = nMap;
            this.activity = activity;
        }

        @Override
        protected String doInBackground(String... url) {

            String data = "";

            try {
                data = downloadUrl(url[0]);
                Log.d("DownloadTask", "DownloadTask : " + data);
            } catch (Exception e) {
                Log.d("Background Task", e.toString());
            }
            return data;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            ParserTask parserTask = new ParserTask(this.nMap, this.activity);

            parserTask.execute(result);
        }
    }

    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {

        GoogleMap nMap;

        MainActivity activity;

        ParserTask(GoogleMap nMap, MainActivity activity) {
            this.nMap = nMap;
            this.activity = activity;
        }

        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {

            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;

            try {
                jObject = new JSONObject(jsonData[0]);
                DirectionsJSONParser parser = new DirectionsJSONParser();

                routes = parser.parse(jObject);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return routes;
        }

        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {
            ArrayList<LatLng> points;
            PolylineOptions lineOptions = null;

            Log.d("ddd", "onPostExecute: " + result);

            for (int i = 0; i < result.size(); i++) {
                points = new ArrayList<>();
                lineOptions = new PolylineOptions();

                List<HashMap<String, String>> path = result.get(i);

                for (int j = 0; j < path.size(); j++) {
                    HashMap<String, String> point = path.get(j);

                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);

                    points.add(position);
                }

                lineOptions.addAll(points);
                lineOptions.width(8);
                lineOptions.color(Color.RED);
            }

            if (lineOptions != null) {
                if (mPolyline != null) {
                    mPolyline.remove();
                }
                mPolyline = this.nMap.addPolyline(lineOptions);

            } else
                Toast.makeText(this.activity, "No route is found", Toast.LENGTH_LONG).show();
        }
    }


}
