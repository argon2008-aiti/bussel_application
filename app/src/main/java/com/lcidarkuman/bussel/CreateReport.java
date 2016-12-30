package com.lcidarkuman.bussel;

import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Calendar;
import java.util.Iterator;

import javax.net.ssl.HttpsURLConnection;

public class CreateReport extends AppCompatActivity {

    EditText busselTopic;
    EditText busselStartTime;
    EditText busselEndTime;
    EditText busselAttendance;
    EditText busselFirstTimers;
    EditText busselSoulsWon;
    EditText busselOffertory;
    Button submitButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_report);

        ActionBar myActionBar = getSupportActionBar();
        myActionBar.setDisplayHomeAsUpEnabled(true);
        myActionBar.setTitle("Create Report");

        busselTopic = (EditText) findViewById(R.id.bussel_topic);
        busselAttendance = (EditText) findViewById(R.id.bussel_attendance);
        busselStartTime = (EditText) findViewById(R.id.bussel_start_time);
        busselEndTime = (EditText) findViewById(R.id.bussel_end_time);
        busselFirstTimers = (EditText) findViewById(R.id.bussel_first_timers);
        busselOffertory = (EditText) findViewById(R.id.bussel_offertory);
        busselSoulsWon = (EditText) findViewById(R.id.bussel_souls_won);

        submitButton = (Button) findViewById(R.id.btn_submit_report);

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitReport();
            }
        });

        busselStartTime.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (v.getId()==R.id.bussel_start_time) {
                    Calendar mcurrentTime = Calendar.getInstance();
                    int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
                    int minute = mcurrentTime.get(Calendar.MINUTE);
                    TimePickerDialog mTimePicker;
                    mTimePicker = new TimePickerDialog(CreateReport.this, new TimePickerDialog.OnTimeSetListener() {
                        @Override
                        public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                            busselStartTime.setText(selectedHour + ":" + selectedMinute);
                        }
                    }, hour, minute, true);//Yes 24 hour time
                    mTimePicker.setTitle("Select Time");
                    mTimePicker.show();
                }
            }
        });

        busselEndTime.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                if(v.getId()==R.id.bussel_end_time) {
                    Calendar mcurrentTime = Calendar.getInstance();
                    int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
                    int minute = mcurrentTime.get(Calendar.MINUTE);
                    TimePickerDialog mTimePicker;
                    mTimePicker = new TimePickerDialog(CreateReport.this, new TimePickerDialog.OnTimeSetListener() {
                        @Override
                        public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                            busselEndTime.setText(selectedHour + ":" + selectedMinute);
                        }
                    }, hour, minute, true);//Yes 24 hour time
                    mTimePicker.setTitle("Select Time");
                    mTimePicker.show();
                }
            }
        });

    }

    public boolean validateEmpty(EditText edit){
        if(edit.getText().toString().length()==0) {
            edit.setError("This field is required");
            return false;
        }
        else {
            return true;
        }
    }

    public boolean submitReport(){
        /* Validate each textinput in turn when the submit button is cliked*/
        if (!validateEmpty(busselTopic)||!validateEmpty(busselStartTime)
                ||!validateEmpty(busselEndTime)||!validateEmpty(busselAttendance)
                ||!validateEmpty(busselFirstTimers)||!validateEmpty(busselSoulsWon)
                ||!validateEmpty(busselOffertory)) {

            return false;
        }

        /* Validation passed submit inputs using post*/
        else {
             new SendPostRequest().execute(busselTopic.getText().toString(),
                     busselStartTime.getText().toString(),
                     busselEndTime.getText().toString(),
                     busselAttendance.getText().toString(),
                     busselFirstTimers.getText().toString(),
                     busselSoulsWon.getText().toString(),
                     busselOffertory.getText().toString(),
                     "2016-12-28");
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        if(item.getItemId()==android.R.id.home) {
            finish();
        }
        return true;

    }
    public class SendPostRequest extends AsyncTask<String, Void, String> {

        private ProgressDialog progressDialog;

        protected void onPreExecute(){
            progressDialog = new ProgressDialog(CreateReport.this);
            progressDialog.setIndeterminate(true);
            progressDialog.setMessage("Submitting Report...");
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        protected String doInBackground(String... arg0) {

            try {

                URL url = new URL("http://lcidarkuman.herokuapp.com/bussels/report/save/"); // here is your URL path

                //URL url = new URL("http://requestb.in/1laqj5m1"); // here is your URL path


                JSONObject postDataParams = new JSONObject();
                postDataParams.put("code", CredentialsWrapper.code);
                postDataParams.put("password", CredentialsWrapper.password);

                postDataParams.put("topic", arg0[0]);
                postDataParams.put("time_started", arg0[1]);
                postDataParams.put("time_ended", arg0[2]);
                postDataParams.put("bussel_attendance", arg0[3]);
                postDataParams.put("num_first_timers", arg0[4]);
                postDataParams.put("num_souls_won", arg0[5]);
                postDataParams.put("offertory", arg0[6]);
                postDataParams.put("date", arg0[7]);

                Log.e("params",postDataParams.toString());

                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(15000 /* milliseconds */);
                conn.setConnectTimeout(15000 /* milliseconds */);
                conn.setRequestMethod("POST");
                conn.setDoInput(true);
                conn.setDoOutput(true);

                OutputStream os = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(os, "UTF-8"));
                writer.write(getPostDataString(postDataParams));

                writer.flush();
                writer.close();
                os.close();

                int responseCode=conn.getResponseCode();

                if (responseCode == HttpsURLConnection.HTTP_OK) {

                    String resultString = "Report Saved Successfully";
                    return resultString;

                }
                else if(responseCode == HttpURLConnection.HTTP_CONFLICT){
                    return new String("A report has already been submitted today");
                }
                else {
                    return new String("Unable to save bussel report");
                }
            }
            catch(Exception e){
                return new String("Exception: " + e.getMessage());
            }

        }

        @Override
        protected void onPostExecute(String result) {
            Toast.makeText(getApplicationContext(), result,
                    Toast.LENGTH_LONG).show();
            progressDialog.dismiss();
            if(result.contains("Saved Successfully")){
                Intent i = new Intent(getApplicationContext(), Home.class);
                startActivity(i);
                finish();
            }
        }
    }

    public String getPostDataString(JSONObject params) throws Exception {

        StringBuilder result = new StringBuilder();
        boolean first = true;

        Iterator<String> itr = params.keys();

        while(itr.hasNext()){

            String key= itr.next();
            Object value = params.get(key);

            if (first)
                first = false;
            else
                result.append("&");

            result.append(URLEncoder.encode(key, "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(value.toString(), "UTF-8"));

        }
        return result.toString();
    }
}
