package com.disnak.puas.quiz;


import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.disnak.puas.R;
import com.disnak.puas.adapter.ServiceHandler;
import com.disnak.puas.adapter.Soal;
import com.disnak.puas.config.Config;
import com.disnak.puas.login.SessionManager;
import com.disnak.puas.model.RegisterAPI;
import com.disnak.puas.model.Value;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ActivitySoalMultiple extends AppCompatActivity {
    private RadioButton radioSexButton;

    int urutanPertanyaan = 0;
    List<Soal> listSoal;
    JSONArray soal = null;
    private ProgressDialog pDialog;
    SessionManager session;
    String nama, nip;


    @BindView(R.id.textViewNama)

    TextView txtNama;
    @BindView(R.id.textViewSoal)
    TextView txtSoal;
    @BindView(R.id.buttonNext)
    Button btnNext;
    @BindView(R.id.radioGroup1)
    RadioGroup rg;
    @BindView(R.id.radio0)
    RadioButton rb1;
    @BindView(R.id.radio1)
    RadioButton rb2;
    @BindView(R.id.radio2)
    RadioButton rb3;
    @BindView(R.id.radio3)
    RadioButton rb4;
    @BindView(R.id.txtNama)
    TextView tampilNama;
    @BindView(R.id.txtNip)
    TextView tampilNip;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        listSoal = new ArrayList<Soal>();

        ButterKnife.bind(this);
        session = new SessionManager(getApplicationContext());
        Toast.makeText(getApplicationContext(), "User Login Status: " + session.isLoggedIn(), Toast.LENGTH_LONG).show();

        session.checkLogin();
        HashMap<String, String> user = session.getUserDetails();

        nama = user.get(SessionManager.KEY_NAME);
        nip = user.get(SessionManager.KEY_NIP);

        tampilNama.setText(Html.fromHtml(nama));
        tampilNip.setText(Html.fromHtml(nip));


        //action
        btnNext.setOnClickListener(klikBerikut);

        //handle error saat tidak terkoneksi ke internet
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

        if (networkInfo != null && networkInfo.isConnected()) {
            new GetSoal().execute();
        } else {
            dialog_error();
        }

    }

    private void dialog_error() {
        final AlertDialog.Builder errorDialog = new AlertDialog.Builder(this);
        errorDialog.setTitle("Koneksi Error");
        errorDialog.setMessage("Anda tidak terhubung ke internet");
        errorDialog.setNeutralButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
                ActivitySoalMultiple.this.finish();
            }
        }).show();
    }


    private class GetSoal extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Showing progress dialog
            pDialog = new ProgressDialog(ActivitySoalMultiple.this);
            pDialog.setMessage("Loading...");
            pDialog.setCancelable(false);
            pDialog.show();

        }

        @Override
        protected Void doInBackground(Void... arg0) {
            // Creating service handler class instance
            ServiceHandler sh = new ServiceHandler();
            // Making a request to url and getting response
            String jsonStr = sh.makeServiceCall(Config.url_soal_view, ServiceHandler.GET);
            //   Log.d("response ", response);
            Log.d("Response: ", "> " + jsonStr);
            if (jsonStr != null) {
                try {
                    JSONObject jsonObj = new JSONObject(jsonStr);
                    // Getting JSON Array node
                    soal = jsonObj.getJSONArray(Config.TAG_DAFTAR);
                    Soal s = null;
                    // looping through All Contacts
                    for (int i = 0; i < soal.length(); i++) {
                        JSONObject c = soal.getJSONObject(i);
                        s = new Soal();

                        String id = c.getString(Config.TAG_ID);
                        String soal = c.getString(Config.TAG_SOAL);
                        String a = c.getString(Config.TAG_A);
                        String b = c.getString(Config.TAG_B);
                        String cc = c.getString(Config.TAG_C);
                        String d = c.getString(Config.TAG_D);

                        s.setId(id);
                        s.setSoal(soal);
                        s.setA(a);
                        s.setB(b);
                        s.setC(cc);
                        s.setD(d);
                        listSoal.add(s);

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                Log.e("ServiceHandler", "Couldn't get any data from the url");
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            // Dismiss the progress dialog
            if (pDialog.isShowing())
                pDialog.dismiss();

            setUpSoal();
        }
    }

    private void setUpSoal() {
        //MENG-ACAK SOAL
        // Collections.shuffle(listSoal);
        tunjukanPertanyaan(0);
    }

    private void tunjukanPertanyaan(int urutan_soal_soal) {


        try {
            rg.clearCheck();
            Soal soal = new Soal();
            soal = listSoal.get(urutan_soal_soal);

            String soalnya = soal.getSoal();
            txtSoal.setText(soalnya);
            rg.check(-1);
            rb1.setTextColor(Color.WHITE);
            rb2.setTextColor(Color.WHITE);
            rb3.setTextColor(Color.WHITE);
            rb4.setTextColor(Color.WHITE);

            rb1.setText(soal.getA());
            rb2.setText(soal.getB());
            rb3.setText(soal.getC());
            rb4.setText(soal.getD());


            pasangLabelDanNomorUrut();

        } catch (Exception e) {
            Log.e(this.getClass().toString(), e.getMessage(), e.getCause());
        }
    }


    private OnClickListener klikBerikut = new OnClickListener() {
        public void onClick(View v) {

            if ((rb1.isChecked() == false) && (rb2.isChecked() == false) && (rb3.isChecked() == false) && (rb4.isChecked() == false)) {
                Toast.makeText(getBaseContext(), "Pilih jawaban dulu", Toast.LENGTH_SHORT).show();
            } else {
                NEXT();
                urutanPertanyaan++;

                tunjukanPertanyaan(urutanPertanyaan);
                if (urutanPertanyaan == (listSoal.size())) {
                    AlertDialog();
                }
            }

        }
    };


    private void pasangLabelDanNomorUrut() {
        // txtNo.setText("No. " + (urutanPertanyaan + 1)+ " dari "
        //    + listSoal.size() + " soal");
    }


    private void NEXT() {
        //Membuat progess dialog
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);
        pDialog.setMessage("Loading...");
        //pDialog.show();

        //Mencari id radio button
        int selectedId = rg.getCheckedRadioButtonId();
        radioSexButton = (RadioButton) findViewById(selectedId);
        String jawaban = radioSexButton.getText().toString();
        String soalInsert = txtSoal.getText().toString();

        String username = nama;
        String nipUser = nip;
        String volume = "1";


        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Config.SERVER_API)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        RegisterAPI api = retrofit.create(RegisterAPI.class);
        Call<Value> call = api.btnBerikut(soalInsert, jawaban, volume, nipUser, username);
        call.enqueue(new Callback<Value>() {
            @Override
            public void onResponse(Call<Value> call, Response<Value> response) {
                String value = response.body().getValue();
                String message = response.body().getMessage();
                pDialog.dismiss();
                if (value.equals("1")) {
                    //Toast.makeText(ActivitySoalMultiple.this, message, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(ActivitySoalMultiple.this, message, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<Value> call, Throwable t) {
                t.printStackTrace();
                pDialog.dismiss();
                Toast.makeText(ActivitySoalMultiple.this, "Jaringan Error", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void AlertDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("LANJUTKAN")
                .setCancelable(false).setPositiveButton("OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,
                                        int id) {
                        Intent next = new Intent(ActivitySoalMultiple.this, ActivitySoalIsian.class);
                        startActivity(next);
                    }
                }).show();
    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Selesaikan pertanyaan dulu")
                .setCancelable(false).setPositiveButton("OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,
                                        int id) {
                        dialog.cancel();
                    }
                }).show();
    }
}
