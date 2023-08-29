package com.example.aqrsgmap;

import static android.icu.util.MeasureUnit.DOT;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Dash;
import com.google.android.gms.maps.model.Dot;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Gap;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PatternItem;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.maps.android.PolyUtil;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback,
        GoogleMap.OnPolygonClickListener {

    private GoogleMap mMap;
    Button reportBtn;
    private static final int PATTERN_GAP_LENGTH_PX = 20;
    private static final PatternItem DOT = new Dot();
    private static final PatternItem GAP = new Gap(PATTERN_GAP_LENGTH_PX);
    private static final int COLOR_BLACK_ARGB = 0xff000000;

    private LocationManager locationManager;
    private Location location = null;

    private String POST = "POST";
    private String GET = "GET";

    private String url = Constants.url;

    FloatingActionButton view_FAB;
    FloatingActionButton pin_curr;
    public JSONObject result_final = null;
    public ArrayList<PolygonOptions> fence_list = new ArrayList<PolygonOptions>();
    public ArrayList<ArrayList<LatLng>> coords_list = new ArrayList<ArrayList<LatLng>>();
    public ArrayList<Integer> count_list = new ArrayList<Integer>();
    public final boolean[] fence_view = {false};

    private LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            notify_on_entry(location, coords_list);

//            locationManager.removeUpdates(locationListener);
//            Toast.makeText(MainActivity.this, location.toString(), Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {

        }

        @Override
        public void onProviderEnabled(String s) {

        }

        @Override
        public void onProviderDisabled(String s) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        reportBtn = (Button) findViewById(R.id.report_btn);
        view_FAB = (FloatingActionButton) findViewById(R.id.view);
        pin_curr = (FloatingActionButton) findViewById(R.id.pointer_curr);

        view_FAB.setVisibility(View.GONE);

        view_FAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!fence_view[0]) {
                    view_FAB.setImageDrawable(getResources().getDrawable(R.drawable.visibility_off48px));

                    fence_view[0] = true;
                    if (result_final != null) {
                        for (int i = 0; i < fence_list.size(); i++){
                            Polygon temp_fence = mMap.addPolygon(fence_list.get(i));

                            ArrayList<String> tag_fence = new ArrayList<String>();
                            tag_fence.add("prone");
                            tag_fence.add(String.valueOf(i));

                            temp_fence.setTag(tag_fence);
                            stylePolygon(temp_fence);
                        }
                    }
                }
                else{
                    fence_view[0] = false;
                    view_FAB.setImageDrawable(getResources().getDrawable(R.drawable.visibility48px));

                    mMap.clear();
                    mMap.addMarker(new MarkerOptions()
                            .position(new LatLng(location.getLatitude(), location.getLongitude()))
                            .title("Current location" + location.toString()));
//                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 12.0f));
                }
            }
        });

        view_FAB.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if (!fence_view[0]) {
                    view_FAB.setImageDrawable(getResources().getDrawable(R.drawable.visibility_off48px));

                    fence_view[0] = true;
                    if (result_final != null) {
                        for (int i = 0; i < fence_list.size(); i++){
                            Polygon temp_fence = mMap.addPolygon(fence_list.get(i));

                            ArrayList<String> tag_fence = new ArrayList<String>();
                            tag_fence.add("prone");
                            tag_fence.add(String.valueOf(i));

                            temp_fence.setTag(tag_fence);
                            stylePolygon(temp_fence);
                        }
                    }
                }

                LatLng nearest_fence_coord = nearest_coord_fence(location, coords_list);
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(nearest_fence_coord, 12.0f));

                return true;
            }
        });

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            boolean isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            boolean isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            if (!isGPSEnabled && !isNetworkEnabled) {
                Toast.makeText(getApplicationContext(), getText(R.string.provider_failed), Toast.LENGTH_LONG).show();
            } else {
                location = null;
                if (isGPSEnabled) {
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5, 500, locationListener);
                    location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                }
                if (isNetworkEnabled) {
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5, 500, locationListener);
                    location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                }
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 12);
            }
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 13);
            }
        }

        reportBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LatLng curr_loc = new LatLng(location.getLatitude(), location.getLongitude());
//                PolyUtil.containsLocation(dubbo, polygon1.getPoints(), false);

//                for (int i = 0; i < fence_list.size(); i++){
//                    mMap.addPolygon(fence_list.get(i));
//                }

                Intent intent = new Intent(MainActivity.this, ReportActivity.class);
                intent.putExtra("lat", location.getLatitude());
                intent.putExtra("long", location.getLongitude());
                startActivity(intent);
            }
        });

        pin_curr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 12.0f));
            }
        });

//        sendRequest(GET, "/request", null, null);
//        try {
//            Toast.makeText(this, getJSONObjectFromURL(url + "/request").toString(), Toast.LENGTH_LONG).show();
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        } catch (JSONException e) {
//            throw new RuntimeException(e);
//        }
    }

    private LatLng nearest_coord_fence(Location curr_location,
                                       ArrayList<ArrayList<LatLng>> list_fence_coords){
        LatLng ans_coord = new LatLng(0.0, 0.0);

        LatLng curr_location_lat_lng = new LatLng(curr_location.getLatitude(), curr_location.getLongitude());
        double distance = find_dist(ans_coord, curr_location_lat_lng);

        for (int fence_index = 0; fence_index < list_fence_coords.size(); fence_index++){
            ArrayList<LatLng> fence_coords_temp = list_fence_coords.get(fence_index);

            for (int fence_coords_temp_index = 0; fence_coords_temp_index < fence_coords_temp.size(); fence_coords_temp_index++){
                if (find_dist(curr_location_lat_lng, fence_coords_temp.get(fence_coords_temp_index)) < distance){
                    ans_coord = fence_coords_temp.get(fence_coords_temp_index);
                }
            }
        }

        return ans_coord;
    }

    public void notify_on_entry(Location location, ArrayList<ArrayList<LatLng>> fence_list){
        boolean flag = false;

        for (int i = 0; i < fence_list.size(); i++){
            if (!flag) {
                ArrayList<LatLng> temp_coords = fence_list.get(i);

                if (PolyUtil.containsLocation(new LatLng(location.getLatitude(), location.getLongitude()), temp_coords, true)){
//                    Toast.makeText(this, "Location Changed", Toast.LENGTH_SHORT).show();

                    String title = "Alert from AKSA!!";
                    String subject = "Alert! Entering accident-prone region.";
                    String body = "You have just entered an accident-prone region. Remain alert";

                    NotificationManager notif = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
                    Notification notify = new Notification.Builder
                            (getApplicationContext()).setContentTitle(title).setContentText(body).
                            setContentTitle(subject).setSmallIcon(R.drawable.ic_launcher_foreground).build();

                    notify.flags |= Notification.FLAG_AUTO_CANCEL;
                    notif.notify(0, notify);

                    flag = true;
                }
            }
        }
    }

    private double find_dist(LatLng coord1, LatLng coord2){
        double theta = coord1.longitude - coord2.longitude;
        double dist = Math.sin(deg2rad(coord1.latitude))
                * Math.sin(deg2rad(coord2.latitude))
                + Math.cos(deg2rad(coord1.latitude))
                * Math.cos(deg2rad(coord2.latitude))
                * Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515;

        return dist;
    }

    private double deg2rad(double deg) {
        return (deg * Math.PI / 180.0); }

    private double rad2deg(double rad) {
        return (rad * 180.0 / Math.PI); }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        if (location != null) {
            mMap.addMarker(new MarkerOptions()
                    .position(new LatLng(location.getLatitude(), location.getLongitude()))
                    .title("Current location" + location.toString()));
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 12.0f));
//                    Toast.makeText(MainActivity.this, location.getLatitude() + ", " + location.getLongitude(), Toast.LENGTH_LONG).show();
        }

//        // Add a marker in Sydney and move the camera
//        LatLng sydney = new LatLng(-34, 151);
//        LatLng dubbo = new LatLng(-32.263322, 148.614253);
//        mMap.addMarker(new MarkerOptions()
//                .position(sydney)
//                .title("Marker in Sydney"));
//        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));

//        ArrayList<Double> lats = new ArrayList<Double>();
//        ArrayList<Double> longs = new ArrayList<Double>();
//        ArrayList<LatLng> coords1 = new ArrayList<LatLng>();
//
////        lats.add(-27.457);
////        lats.add(-33.852);
////        lats.add(-37.813);
////        lats.add(-34.928);
////
////        longs.add(153.040);
////        longs.add(151.211);
////        longs.add(144.962);
////        longs.add(138.599);
//
//        coords1.add(new LatLng(-27.457, 153.040));
//        coords1.add(new LatLng(-33.852, 151.211));
//        coords1.add(new LatLng(-37.813, 144.962));
//        coords1.add(new LatLng(-34.928, 138.599));
//
//        // Add polygons to indicate areas on the map.
//        Polygon polygon1 = googleMap.addPolygon(new PolygonOptions()
//                .clickable(false)
//                .addAll(coords1));
////        polygon1.setPoints(coords1);
//
//        // Store a data object with the polygon, used here to indicate an arbitrary type.
//        polygon1.setTag("alpha");
//        // Style the polygon.
//        stylePolygon(polygon1);

//        Polygon polygon2 = googleMap.addPolygon(new PolygonOptions()
//                .clickable(false)
//                .add(
//                        new LatLng(-31.673, 128.892),
//                        new LatLng(-31.952, 115.857),
//                        new LatLng(-17.785, 122.258),
//                        new LatLng(-12.4258, 130.7932)));
//        polygon2.setTag("beta");
//        stylePolygon(polygon2);
//
//        Toast.makeText(this, String.valueOf(PolyUtil.containsLocation(dubbo, polygon1.getPoints(), false)), Toast.LENGTH_SHORT).show();

        // Position the map's camera near Alice Springs in the center of Australia,
        // and set the zoom factor so most of Australia shows on the screen.
//        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(-23.684, 133.903), 4));

        // Set listeners for click events.
//        googleMap.setOnPolygonClickListener(this);

//        googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
//            @Override
//            public void onMapClick(@NonNull LatLng latLng) {
//                googleMap.addMarker(new MarkerOptions()
//                        .position(latLng)
//                        .title(latLng.toString()));
//            }
//        });

        new MyTask().execute(1);

//        Toast.makeText(this, "MyTask executed", Toast.LENGTH_SHORT).show();

        mMap.setOnPolygonClickListener(this);
//        Toast.makeText(this, "PolygonClickListener attached", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onPolygonClick(Polygon polygon) {
//        int color = polygon.getStrokeColor() ^ 0x00ffffff;
//        polygon.setStrokeColor(color);
//        color = polygon.getFillColor() ^ 0x00ffffff;
//        polygon.setFillColor(color);

        int index_fence = -1;

        ArrayList<String> tag_props;

        // Get the data object stored with the polygon.
        if (polygon.getTag() != null) {
            tag_props = (ArrayList<String>) polygon.getTag();

            index_fence = Integer.parseInt(tag_props.get(1));
        }

        Toast.makeText(this, count_list.get(index_fence - 1) + " accidents here in the past.", Toast.LENGTH_SHORT).show();
    }

    private static final int COLOR_WHITE_ARGB = 0xffffffff;
    private static final int COLOR_DARK_GREEN_ARGB = 0xff388E3C;
    private static final int COLOR_LIGHT_GREEN_ARGB = 0xff81C784;

    private static final int POLYGON_STROKE_WIDTH_PX = 8;
    private static final int PATTERN_DASH_LENGTH_PX = 20;
    private static final PatternItem DASH = new Dash(PATTERN_DASH_LENGTH_PX);

    // Create a stroke pattern of a gap followed by a dash.
    private static final List<PatternItem> PATTERN_POLYGON_ALPHA = Arrays.asList(GAP, DASH);

    // Create a stroke pattern of a dot followed by a gap, a dash, and another gap.
    private static final List<PatternItem> PATTERN_POLYGON_BETA =
            Arrays.asList(DOT, GAP, DASH, GAP);

    /**
     * Styles the polygon, based on type.
     * @param polygon The polygon object that needs styling.
     */
    private void stylePolygon(Polygon polygon) {
        String type = "";
        ArrayList<String> tag_props;

        // Get the data object stored with the polygon.
        if (polygon.getTag() != null) {
            tag_props = (ArrayList<String>) polygon.getTag();
            type = tag_props.get(0);
        }

        List<PatternItem> pattern = null;
        int strokeColor = COLOR_BLACK_ARGB;
        int fillColor = COLOR_WHITE_ARGB;

        switch (type) {
            // If no type is given, allow the API to use the default.
            case "prone":
                // Apply a stroke pattern to render a dashed line, and define colors.
                pattern = PATTERN_POLYGON_ALPHA;
                strokeColor = COLOR_DARK_GREEN_ARGB;
                fillColor = COLOR_LIGHT_GREEN_ARGB;
                break;
//            case "beta":
//                // Apply a stroke pattern to render a line of dots and dashes, and define colors.
//                pattern = PATTERN_POLYGON_BETA;
//                strokeColor = COLOR_DARK_ORANGE_ARGB;
//                fillColor = COLOR_LIGHT_ORANGE_ARGB;
//                break;
        }

        polygon.setStrokePattern(pattern);
        polygon.setStrokeWidth(POLYGON_STROKE_WIDTH_PX);
        polygon.setStrokeColor(strokeColor);
        polygon.setFillColor(fillColor);
    }

    class MyTask extends AsyncTask<Integer, Integer, JSONObject> {
        @Override
        protected JSONObject doInBackground(Integer... params) {
            JSONObject response_String = null;
            for (int count = 0; count <= params[0]; count++) {
                try {
                    Thread.sleep(1000);

                    publishProgress(count);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            try {
                JSONObject response = getJSONObjectFromURL(url + "/request"); // calls method to get JSON object
//                return response.toString();
//                response_String = response.toString();
                response_String = response;

//                System.out.println("Data from server:" + response);
//
//                for (int polygon_count = 0; polygon_count < response.length(); polygon_count++){
//                    ArrayList<LatLng> coords_poly = new ArrayList<LatLng>();
////                    System.out.println("Polygon id " + (polygon_count + 1) + ": " + response.get(String.valueOf(polygon_count + 1)) + " : " + response.getJSONObject(String.valueOf(polygon_count + 1)).length());
//
//                    for (int coord_count = 0; coord_count < response.getJSONObject(String.valueOf(polygon_count + 1)).length(); coord_count++){
////                        System.out.println("coord_count id " + (coord_count + 1) + ": " + response.getJSONObject(String.valueOf(polygon_count + 1)).get("v" + (coord_count + 1)));
//                        coords_poly.add(new LatLng(Double.parseDouble(response.getJSONObject(String.valueOf(polygon_count + 1)).getJSONObject("v" + (coord_count + 1)).get("lat").toString()), Double.parseDouble(response.getJSONObject(String.valueOf(polygon_count + 1)).getJSONObject("v" + (coord_count + 1)).get("long").toString())));
//                    }
//
//                    System.out.println("Polygon id " + (polygon_count + 1) + ": " + coords_poly);
//
//                    Polygon polygon2 = mMap.addPolygon(new PolygonOptions()
//                            .clickable(false)
//                            .addAll(coords_poly));
//                    polygon2.setTag("prone");
//                    stylePolygon(polygon2);
//
////                    for (int coord_count = 0; coord_count < response.get(String.valueOf(polygon_count + 1)).toString().length(); coord_count++) {
////                    }
//                }

            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return response_String;
        }
        @Override
        protected void onPostExecute(JSONObject result) {
//            progressBar.setVisibility(View.GONE);
//            Toast.makeText(MainActivity.this, result, Toast.LENGTH_SHORT).show();

//            ArrayList<LatLng> coords1 = new ArrayList<LatLng>();

//        lats.add(-27.457);
//        lats.add(-33.852);
//        lats.add(-37.813);
//        lats.add(-34.928);
//
//        longs.add(153.040);
//        longs.add(151.211);
//        longs.add(144.962);
//        longs.add(138.599);

//            coords1.add(new LatLng(-27.457, 153.040));
//            coords1.add(new LatLng(-33.852, 151.211));
//            coords1.add(new LatLng(-37.813, 144.962));
//            coords1.add(new LatLng(-34.928, 138.599));
//
//            // Add polygons to indicate areas on the map.
//            Polygon polygon1 = mMap.addPolygon(new PolygonOptions()
//                    .clickable(false)
//                    .addAll(coords1));
////        polygon1.setPoints(coords1);
//
//            // Store a data object with the polygon, used here to indicate an arbitrary type.
//            polygon1.setTag("alpha");
//            // Style the polygon.
//            stylePolygon(polygon1);

            if ((result != null) && (!result.toString().equals("{}"))) {
                result_final = result;

                view_FAB.setVisibility(View.VISIBLE);
                fence_view[0] = true;

                JSONObject result_0;
                JSONObject result_1;

                try {
                    result_0 = result.getJSONObject("0");
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }

                try {
                    result_1 = result.getJSONObject("1");
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }

                System.out.println("Data from server:" + result);

                for (int fence_count = 0; fence_count < result_1.length(); fence_count++){
                    try {
                        count_list.add(Integer.parseInt(result_1.get(String.valueOf(fence_count + 1)).toString()));
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                }

                for (int polygon_count = 0; polygon_count < result_0.length(); polygon_count++) {
                    ArrayList<LatLng> coords_poly = new ArrayList<LatLng>();
                    //                    System.out.println("Polygon id " + (polygon_count + 1) + ": " + response.get(String.valueOf(polygon_count + 1)) + " : " + response.getJSONObject(String.valueOf(polygon_count + 1)).length());

                    try {
                        for (int coord_count = 0; coord_count < result_0.getJSONObject(String.valueOf(polygon_count + 1)).length(); coord_count++) {
                            //                        System.out.println("coord_count id " + (coord_count + 1) + ": " + response.getJSONObject(String.valueOf(polygon_count + 1)).get("v" + (coord_count + 1)));
                            try {
                                coords_poly.add(new LatLng(Double.parseDouble(result_0.getJSONObject(String.valueOf(polygon_count + 1)).getJSONObject("v" + (coord_count + 1)).get("lat").toString()), Double.parseDouble(result_0.getJSONObject(String.valueOf(polygon_count + 1)).getJSONObject("v" + (coord_count + 1)).get("long").toString())));
                            } catch (JSONException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    } catch (JSONException error2) {
                        throw new RuntimeException(error2);
                    }

                    System.out.println("Polygon id " + (polygon_count + 1) + ": " + coords_poly);

                    Polygon polygon2 = mMap.addPolygon(new PolygonOptions()
                            .clickable(true)
                            .addAll(coords_poly));

                    ArrayList<String> tag_fence = new ArrayList<String>();
                    tag_fence.add("prone");
                    tag_fence.add(String.valueOf(polygon_count));

//                    polygon2.setTag(["prone", polygon_count]);
//                    polygon2.setTag(polygon_count);
                    polygon2.setTag(tag_fence);

                    stylePolygon(polygon2);

                    fence_list.add(new PolygonOptions()
                            .clickable(false)
                            .addAll(coords_poly));

                    coords_list.add(coords_poly);
                    //                    for (int coord_count = 0; coord_count < response.get(String.valueOf(polygon_count + 1)).toString().length(); coord_count++) {
                    //                    }
                }
            }
            else {
                Toast.makeText(MainActivity.this, "No data for accident-prone areas found", Toast.LENGTH_SHORT).show();
            }

        }
        @Override
        protected void onPreExecute() {
//            txt.setText("Task Starting...");
        }
        @Override
        protected void onProgressUpdate(Integer... values) {
//            txt.setText("Running..."+ values[0]);
//            progressBar.setProgress(values[0]);
        }
    }

    public static JSONObject getJSONObjectFromURL(String urlString) throws IOException, JSONException {

        HttpURLConnection urlConnection = null;

        URL url = new URL(urlString);

        urlConnection = (HttpURLConnection) url.openConnection();

        urlConnection.setRequestMethod("GET");
        urlConnection.setReadTimeout(10000 /* milliseconds */);
        urlConnection.setConnectTimeout(15000 /* milliseconds */);

        urlConnection.setDoOutput(true);

        urlConnection.connect();

        BufferedReader br=new BufferedReader(new InputStreamReader(url.openStream()));

        char[] buffer = new char[1024];

        String jsonString = new String();

        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) {
            sb.append(line+"\n");
        }
        br.close();

        jsonString = sb.toString();

        System.out.println("JSON: " + jsonString);
        urlConnection.disconnect();

        return new JSONObject(jsonString);
    }
}