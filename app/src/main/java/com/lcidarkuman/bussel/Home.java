package com.lcidarkuman.bussel;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.provider.Settings;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;
import com.jakewharton.processphoenix.ProcessPhoenix;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

public class Home extends AppCompatActivity {
    TextView helloText;
    ListView reportListView;
    private ProgressDialog progressDialog;
    private SendGetRequest backGroundProcess;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */

    final ArrayList<HashMap<String, String>> busselReportList = new ArrayList<HashMap<String, String>>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        getSupportActionBar().setTitle("Bussel Reports");

        reportListView = (ListView) findViewById(R.id.reports_list);

        backGroundProcess = new SendGetRequest();
        backGroundProcess.execute(CredentialsWrapper.code, CredentialsWrapper.password);

        reportListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Start an alpha animation for clicked item
                Animation animation1 = new AlphaAnimation(0.3f, 1.0f);
                animation1.setDuration(200);
                view.startAnimation(animation1);
                HashMap<String, String> report = new HashMap<String, String>();
                report = busselReportList.get(position);
                Toast.makeText(getApplicationContext(), report.get("topic"), Toast.LENGTH_SHORT).show();
            }
        });

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.new_bussel_report);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(), CreateReport.class);
                startActivity(i);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_home, menu);
        return true;
    }

    @Override
    public void onDestroy() {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
        backGroundProcess.cancel(true);
        super.onDestroy();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_refresh_data) {
            backGroundProcess = new SendGetRequest();
            backGroundProcess.execute(CredentialsWrapper.code, CredentialsWrapper.password);
            return true;
        }

        if (id == R.id.action_update_location) {
            LocationManager lm = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
            boolean gps_enabled = false;

            try {
                gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
            } catch (Exception ex) {
            }


            if (!gps_enabled) {

                new AlertDialog.Builder(this)
                        .setMessage("GPS is disabled. Please enable it.")
                        .setCancelable(false)
                        .setPositiveButton("Enable GPS", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // TODO Auto-generated method stub
                                Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                startActivity(myIntent);
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                                paramDialogInterface.dismiss();
                            }
                        })
                        .show();
            } else {
                progressDialog = new ProgressDialog(Home.this);
                progressDialog.setIndeterminate(true);
                progressDialog.setMessage("Acquiring Location...");
                progressDialog.setCancelable(false);
                progressDialog.show();

                final LocationManager locationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);

                final LocationListener locationListener = new LocationListener() {

                    @Override
                    public void onStatusChanged(String provider, int status, Bundle extras) {
                        // TODO Auto-generated method stub

                    }

                    @Override
                    public void onProviderEnabled(String provider) {
                        // TODO Auto-generated method stub

                    }

                    @Override
                    public void onProviderDisabled(String provider) {
                        // TODO Auto-generated method stub

                    }

                    @Override
                    public void onLocationChanged(Location location) {
                        // TODO Auto-generated method stub
                        if (location != null && location.getAccuracy()<=5) {
                            GpsCoordinate gps = new GpsCoordinate();
                            gps.latitude = location.getLatitude();
                            gps.longitude = location.getLongitude();
                            Log.d("GPS", "Location Acquired");
                            new SendGPSLocation().execute(gps);

                            if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                                // TODO: Consider calling
                                return;
                            }
                            locationManager.removeUpdates(this);
                        }

                    }
                };

                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    //return TODO;
                }
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);

            }

            return true;
        }

        if (id == R.id.action_change_password) {
            Intent i = new Intent(getApplicationContext(), PasswordChange.class);
            startActivity(i);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setMessage("Are you sure you want to exit?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Home.this.finish();
                    }
                })
                .setNegativeButton("No", null)
                .show();
    }

    private class SendGetRequest extends AsyncTask<String, Void, ServerResultWrapper> {


        protected void onPreExecute(){

            progressDialog = new ProgressDialog(Home.this);
            progressDialog.setIndeterminate(true);
            progressDialog.setMessage("Fetching Reports...");
            progressDialog.setCancelable(false);
            progressDialog.show();

        }

        protected ServerResultWrapper doInBackground(String... params) {
            ServerResultWrapper serverResult = new ServerResultWrapper();

            try {

                URL url = new URL("http://lcidarkuman.herokuapp.com/bussels/reports_for_bussel/all/?code="
                        +params[0] + "&password="+params[1]); // here is your URL path

                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(15000 /* milliseconds */);
                conn.setConnectTimeout(15000 /* milliseconds */);
                conn.setRequestMethod("GET");

                int responseCode=conn.getResponseCode();

                if(responseCode==HttpURLConnection.HTTP_OK) {

                    BufferedReader in = new BufferedReader(new
                            InputStreamReader(
                            conn.getInputStream()));

                    StringBuffer sb = new StringBuffer("");
                    String line = "";

                    while ((line = in.readLine()) != null) {

                        sb.append(line);
                        break;
                    }

                    in.close();
                    serverResult.resultCode = responseCode;
                    serverResult.resultString = sb.toString();
                    return serverResult;
                }
                else if(responseCode==HttpURLConnection.HTTP_UNAUTHORIZED) {
                    serverResult.resultCode = responseCode;
                    serverResult.resultString = "Login Failed";
                    return serverResult;
                }

                else {
                    serverResult.resultCode = responseCode;
                    serverResult.resultString = "Unknown Error";
                    return serverResult;
                }

            }
            catch(Exception e){
                serverResult.resultCode =-1;
                serverResult.resultString = e.getMessage();
                return serverResult;
            }

        }

        @Override
        protected void onPostExecute(ServerResultWrapper result) {
            progressDialog.dismiss();
            if(result.resultCode==-1) {
                Toast.makeText(getApplicationContext(), result.resultString,
                        Toast.LENGTH_LONG).show();
            }
            else if(result.resultCode==401) {
                Toast.makeText(getApplicationContext(), result.resultString,
                        Toast.LENGTH_LONG).show();
            }
            else if(result.resultCode==200) {
                try {
                    busselReportList.clear();
                    JSONArray resultArray = new JSONArray(result.resultString);
                    for (int i = 0; i < resultArray.length(); i++) {
                        JSONObject report = resultArray.getJSONObject(i);
                        HashMap<String, String> busselReport = new HashMap<String, String>();
                        busselReport.put("id", report.getString("pk"));
                        busselReport.put("topic", report.getString("topic"));
                        busselReport.put("b_attendance", report.getString("b_attendance"));
                        busselReport.put("date", report.getString("date"));
                        busselReportList.add(busselReport);
                    }

                    ListAdapter adapter = new SimpleAdapter(
                            Home.this, busselReportList,
                            R.layout.list_item, new String[]{"topic", "date", "b_attendance"},
                            new int[]{R.id.report_topic,
                                    R.id.report_date,
                            R.id.attendance});

                    reportListView.setAdapter(adapter);
                } catch (JSONException e) {

                }

            }
        }
    }
    private class ServerResultWrapper {
        int resultCode;
        String resultString;
    }

    // background class to send GPS location
    private class SendGPSLocation extends AsyncTask<GpsCoordinate, Void, Integer> {


        @Override
        protected void onPreExecute(){
            Log.d("Debugging", "onPreExecute");
            progressDialog.setMessage("Bussel location acquired. \n " +
                    "Sending data to server...");
        }


        @Override
        protected Integer doInBackground(GpsCoordinate... params) {
            Log.d("Debugging", "doInBackground");
            try {

                URL url = new URL("http://lcidarkuman.herokuapp.com/bussels/update/location/?code="
                        + CredentialsWrapper.code + "&lon=" + params[0].longitude + "&lat=" + params[0].latitude);

                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(15000 /* milliseconds */);
                conn.setConnectTimeout(15000 /* milliseconds */);
                conn.setRequestMethod("GET");

                int responseCode = conn.getResponseCode();
                return responseCode;

            }

            catch (Exception e) {
                e.printStackTrace();
                return -1;
            }
        }

        @Override
        protected void onPostExecute(Integer responseCode){
            progressDialog.dismiss();
            Log.d("Debugging", "onPostExecute: "+ responseCode+ ":" + HttpURLConnection.HTTP_OK);
            if (responseCode.intValue() == HttpURLConnection.HTTP_OK) {
                Toast.makeText(Home.this, "Location updated.", Toast.LENGTH_LONG).show();
            }

            else if(responseCode.intValue()==-1){
                Toast.makeText(Home.this, "Could not connect to server", Toast.LENGTH_LONG).show();
            }

            else {
                Toast.makeText(Home.this, "Could not update location", Toast.LENGTH_LONG).show();
            }

        }
    }

    private class GpsCoordinate {
        double longitude;
        double latitude;
    }

}
