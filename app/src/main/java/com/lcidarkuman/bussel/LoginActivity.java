package com.lcidarkuman.bussel;

/**
 * Created by argon on 12/23/16.
 */

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
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
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.HttpsURLConnection;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";

    EditText _busselCodeText;
    EditText _passwordText;
    Button _loginButton;

    String busselCode;
    String busselPassword;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        _busselCodeText = (EditText)findViewById(R.id.bussel_code_input) ;
        _passwordText = (EditText)findViewById(R.id.bussel_password_input);
        _loginButton = (Button)findViewById(R.id.btn_login);

        _loginButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                login();
            }
        });
    }

    public void login() {
        Log.d(TAG, "Login");

        if (!validate()) {
            onLoginFailed();
            return;
        }
        // TODO: Implement your own authentication logic here.

        busselCode = _busselCodeText.getText().toString();
        busselPassword = _passwordText.getText().toString();

        new SendLoginRequest().execute(busselCode, busselPassword);

    }

    @Override
    public void onBackPressed() {
        // disable going back to the MainActivity
        moveTaskToBack(true);
    }

    public void onLoginSuccess() {
        _loginButton.setEnabled(true);
        Log.d(TAG, "Login Successful");
        CredentialsWrapper.code = busselCode;
        CredentialsWrapper.password = busselPassword;
        Intent i = new Intent(getApplicationContext(), Home.class);
        startActivity(i);
        finish();
    }

    public void onLoginFailed() {
        Toast.makeText(getBaseContext(), "Login failed", Toast.LENGTH_LONG).show();

        _loginButton.setEnabled(true);
    }

    public boolean validate() {
        boolean valid = true;

        String code = _busselCodeText.getText().toString();
        String password = _passwordText.getText().toString();

        final String CODE_PATTERN = "^[A-Fa-f0-9]{4}-[A-Fa-f0-9]{4}$";
        Pattern pattern = Pattern.compile(CODE_PATTERN);
        Matcher matcher = pattern.matcher(code);

        if (code.isEmpty() || !matcher.matches()) {
            _busselCodeText.setError("enter a valid bussel code");
            valid = false;
        } else {
            _busselCodeText.setError(null);
        }

        if (password.isEmpty() || password.length() < 4) {
            _passwordText.setError("between 4 and 10 alphanumeric characters");
            valid = false;
        } else {
            _passwordText.setError(null);
        }

        return valid;
    }

    private class SendLoginRequest extends AsyncTask<String, Void, Integer> {

        private ProgressDialog progressDialog;

        protected void onPreExecute(){

            progressDialog = new ProgressDialog(LoginActivity.this);
            progressDialog.setIndeterminate(true);
            progressDialog.setMessage("Authenticating...");
            progressDialog.setCancelable(false);
            progressDialog.show();

        }

        protected Integer doInBackground(String... params) {

            try {

                URL url = new URL("http://lcidarkuman.herokuapp.com/bussels/authenticate/?code="
                        +params[0] + "&password="+params[1]); // here is your URL path

                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(15000 /* milliseconds */);
                conn.setConnectTimeout(15000 /* milliseconds */);
                conn.setRequestMethod("GET");

                int responseCode=conn.getResponseCode();

                return responseCode;

            }
            catch(Exception e){
                return -1;
            }

        }

        @Override
        protected void onPostExecute(Integer result) {
            progressDialog.dismiss();
            if(result==-1) {
                Toast.makeText(getApplicationContext(), "Unable to connect to server",
                        Toast.LENGTH_LONG).show();
            }
            else if(result==401) {
                Toast.makeText(getApplicationContext(), "Wrong bussel code or password",
                        Toast.LENGTH_LONG).show();
            }

            else if(result==500) {
                Toast.makeText(getApplicationContext(), "The server experienced a problem. Please try again",
                        Toast.LENGTH_LONG).show();
            }
            else if(result==200) {
                onLoginSuccess();
            }

            else  {
                Toast.makeText(getApplicationContext(), "Unexpected Error!",
                        Toast.LENGTH_LONG).show();
            }

        }
    }

}



