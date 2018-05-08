package com.disnak.puas.login;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONObject;

import android.os.AsyncTask;
import android.app.ProgressDialog;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.disnak.puas.R;
import com.disnak.puas.config.Config;
import com.disnak.puas.quiz.ActivitySoalMultiple;

public class Login extends AppCompatActivity {
    Button login;
    Intent a;
    EditText nama, nip;
    String url, success;
    SessionManager session;
    ProgressDialog pDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        session = new SessionManager(getApplicationContext());
        //Toast.makeText(getApplicationContext(),"User Login Status: " + session.isLoggedIn(), Toast.LENGTH_LONG).show();

        login = (Button) findViewById(R.id.login);
        nama = (EditText) findViewById(R.id.txtNama);
        nip = (EditText) findViewById(R.id.password);

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                url = "http://192.168.1.19/puas/login.php?" + "nip="
                        + nip.getText().toString() + "&nama="
                        + nama.getText().toString();
                if (nama.getText().toString().trim().length() > 0
                        && nip.getText().toString().trim().length() > 0) {
                    new Masuk().execute();
                } else {
                    Toast.makeText(getApplicationContext(), "Nama/Nip masih kosong!", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    public class Masuk extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            pDialog = new ProgressDialog(Login.this);
            pDialog.setMessage("Loading . . .");
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected String doInBackground(String... arg0) {
            JSONParser jParser = new JSONParser();
            JSONObject json = jParser.getJSONFromUrl(url);
            try {
                success = json.getString("success");
                Log.e("error", "nilai sukses" + success);
                JSONArray hasil = json.getJSONArray("login");
                if (success.equals("1")) {
                    for (int i = 0; i < hasil.length(); i++) {
                        JSONObject c = hasil.getJSONObject(i);

                        String nama = c.getString("nama").trim();
                        String nip = c.getString("nip").trim();
                        session.createLoginSession(nama, nip);
                        Log.e("Ok", "ambil data");

                    }
                } else {
                    Log.e("error", "tidak bisa ambil data 0");
                }

            } catch (Exception e) {
                Log.e("error", "tidak bisa ambil data 1");
            }


            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            pDialog.dismiss();
            if(success.equals("1")){
                a = new Intent(Login.this, ActivitySoalMultiple.class);
                startActivity(a);
                finish();
            }else {
                Toast.makeText(getApplicationContext(), "Pateppak nyama ben nippe bro...!",Toast.LENGTH_SHORT ).show();
            }
        }
    }

}
