package com.disnak.puas.quiz;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class ActivitySoalIsian extends AppCompatActivity {

    int urutanPertanyaan = 0;
    List<Soal> listSoal;
    JSONArray soal = null;
    private ProgressDialog pDialog;
    SessionManager session;
    String namaTampil, nipTampil;

    @BindView(R.id.soal)
    TextView soalIsi;
    @BindView(R.id.textArea)
    EditText textArea;
    @BindView(R.id.nextIsi)
    Button nextIsi;
    @BindView(R.id.textNama)
    TextView tmpNip;
    @BindView(R.id.textNip)
    TextView tmpNama;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_soal_isian);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        listSoal = new ArrayList<Soal>();
        ButterKnife.bind(this);
        session = new SessionManager(getApplicationContext());
        Toast.makeText(getApplicationContext(), "User Login Status: " + session.isLoggedIn(), Toast.LENGTH_LONG).show();

        session.checkLogin();
        HashMap<String, String> user = session.getUserDetails();

        namaTampil = user.get(SessionManager.KEY_NAME);
        nipTampil = user.get(SessionManager.KEY_NIP);

        tmpNama.setText(Html.fromHtml(namaTampil));
        tmpNip.setText(Html.fromHtml(nipTampil));
        //Handle error saat tidak terkoneksi ke internet
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

        if (networkInfo != null && networkInfo.isConnected()) {
            new getSoal().execute();
        } else {
            dialogError();
        }

    }

    private void dialogError() {
        final AlertDialog.Builder errorDialog = new AlertDialog.Builder(this);
        errorDialog.setTitle("Koneksi Error");
        errorDialog.setMessage("Cek koneksi internet anda");
        errorDialog.setNeutralButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int i) {
                dialog.dismiss();
                ActivitySoalIsian.this.finish();
            }
        }).show();
    }

    private class getSoal extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // showing progress dialog
            pDialog = new ProgressDialog(ActivitySoalIsian.this);
            pDialog.setMessage("Mohon Tunggu");
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            // Creating service handler class instance
            ServiceHandler sh = new ServiceHandler();
            // making a request to url and getting response
            String jsonStr = sh.makeServiceCall(Config.url_soal_isian, ServiceHandler.GET);
            Log.d("Response: ", ">" + jsonStr);
            if (jsonStr != null) {
                try {
                    JSONObject jsonObj = new JSONObject(jsonStr);
                    // Getting JSON Array node
                    soal = jsonObj.getJSONArray(Config.TAG_DAFTAR);
                    Soal s = null;
                    //looping through All Contacts
                    for (int i = 0; i < soal.length(); i++) {
                        JSONObject c = soal.getJSONObject(i);
                        s = new Soal();

                        String id = c.getString(Config.TAG_ID);
                        String soal = c.getString(Config.TAG_SOAL);

                        s.setId(id);
                        s.setSoal(soal);

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
            if (pDialog.isShowing())
                pDialog.dismiss();
            setUpSoal();
        }
    }

    private void setUpSoal() {
        tunjukkanPertanyaan(0);
    }

    private void tunjukkanPertanyaan(int urutan_soal_soal) {
        try {
            Soal soal = new Soal();
            soal = listSoal.get(urutan_soal_soal);

            String soalnya = soal.getSoal();
            soalIsi.setText(soalnya);

            //pasangLabelDanNomorUrut();
        } catch (Exception e) {
            Log.e(this.getClass().toString(), e.getMessage(), e.getCause());
        }
    }


    @OnClick(R.id.nextIsi)
    void ButtonNext() {
        // text area harus clear isinya saat di lanjut

        NEXTISI();
        textArea.getText().clear();
        urutanPertanyaan++;
        tunjukkanPertanyaan(urutanPertanyaan);
        if (urutanPertanyaan == (listSoal.size())) {
            AlertDialog();
        }
    }

    private void NEXTISI() {
        //Membuat progess dialog
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);
        pDialog.setMessage("Loading...");
        pDialog.show();

        //Penyimpanan data yang akan di inputkan dalam database.
        String soal = soalIsi.getText().toString();
        String alasan = textArea.getText().toString();
        String nip = nipTampil;
        String username = namaTampil;


        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Config.SERVER_API)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        RegisterAPI api = retrofit.create(RegisterAPI.class);
        Call<Value> call = api.NEXTISI(soal, alasan, nip, username);
        call.enqueue(new Callback<Value>() {
            @Override
            public void onResponse(Call<Value> call, Response<Value> response) {
                String value = response.body().getValue();
                String message = response.body().getMessage();
                pDialog.dismiss();
                if (value.equals("1")) {
                    Toast.makeText(ActivitySoalIsian.this, message, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(ActivitySoalIsian.this, message, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<Value> call, Throwable t) {
                t.printStackTrace();
                pDialog.dismiss();
                Toast.makeText(ActivitySoalIsian.this, "Jaringan Error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void AlertDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Tutup Aplikasi?")
                .setCancelable(false).setPositiveButton("Ya",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,
                                        int id) {
                        ActivitySoalIsian.this.finish();
                    }
                }).setNegativeButton("Tidak",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,
                                        int id) {
                        dialog.cancel();
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
