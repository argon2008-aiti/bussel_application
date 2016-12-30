package com.lcidarkuman.bussel;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.CountDownTimer;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.jakewharton.processphoenix.ProcessPhoenix;

import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Iterator;

import javax.net.ssl.HttpsURLConnection;

public class PasswordChange extends AppCompatActivity {

    EditText oldPassword;
    EditText newPassword;
    EditText newPasswordConfirm;
    Button changePasswordButton;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password_change);

        ActionBar myActionBar = getSupportActionBar();
        myActionBar.setDisplayHomeAsUpEnabled(true);
        myActionBar.setTitle("Change Password");

        oldPassword = (EditText) findViewById(R.id.old_password);
        newPassword = (EditText) findViewById(R.id.new_password);
        newPasswordConfirm = (EditText) findViewById(R.id.new_password_confirm);
        
        changePasswordButton = (Button) findViewById(R.id.btn_change_password);
        
        changePasswordButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                changePassword();
            }
        });


    }

    private boolean changePassword() {
        if(!validateEmpty(oldPassword)
                ||!validateEmpty(newPassword)
                ||!validateEmpty(newPasswordConfirm)){
            return false;
        }
        if(!validateEquality(newPassword, newPasswordConfirm)){
            return false;
        }
        else {
            new SendPostRequest().execute(oldPassword.getText().toString(),
                    newPassword.getText().toString(),
                    newPasswordConfirm.getText().toString());
            return true;

        }
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

    public boolean validateEquality(EditText edit1, EditText edit2) {
        if(edit1.getText().toString().equals(edit2.getText().toString())) {
            return true;
        }
        else {
            edit1.setError("new passwords do not match");
            edit2.setError("new passwords do not match");
            return false;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        if(item.getItemId()==android.R.id.home) {
            finish();
        }
        return true;

    }

    public class SendPostRequest extends AsyncTask<String, Void, String> {

        protected void onPreExecute(){
            progressDialog = new ProgressDialog(PasswordChange.this);
            progressDialog.setIndeterminate(true);
            progressDialog.setMessage("Requesting...");
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        protected String doInBackground(String... arg0) {

            try {

                URL url = new URL("http://lcidarkuman.herokuapp.com/bussels/change/password/"); // here is your URL path

                //URL url = new URL("http://requestb.in/1laqj5m1"); // here is your URL path


                JSONObject postDataParams = new JSONObject();
                postDataParams.put("code", CredentialsWrapper.code);

                postDataParams.put("old_password", arg0[0]);
                postDataParams.put("new_password_one", arg0[1]);
                postDataParams.put("new_password_two", arg0[2]);


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

                    String resultString = "Password Changed Successfully";
                    return resultString;

                }

                else if(responseCode == HttpURLConnection.HTTP_UNAUTHORIZED){
                    return new String("Incorrect Password from change password");
                }

                else {
                    return new String("Unable to change password");
                }
            }
            catch(Exception e){
                return new String("Exception: " + e.getMessage());
            }

        }

        @Override
        protected void onPostExecute(String result) {
            progressDialog.dismiss();
            Toast.makeText(getApplicationContext(), result,
                    Toast.LENGTH_SHORT).show();
            progressDialog.setMessage("Restarting Application");
            progressDialog.show();
            if(result.contains("Changed Successfully")){
                new CountDownTimer(3000, 1000) {
                    public void onFinish() {
                        // restart application to login with new credentials
                        ProcessPhoenix.triggerRebirth(getApplicationContext());
                    }

                    public void onTick(long millisUntilFinished) {
                        // millisUntilFinished    The amount of time until finished.
                    }
                }.start();

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
