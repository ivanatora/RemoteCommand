package com.example.ivanatora.remotecommand;

import java.util.Properties;

import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.os.AsyncTask;
import android.app.ProgressDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.Button;
import android.util.Log;
import com.jcraft.jsch.*;
import com.example.ivanatora.remotecommand.ExecTask.*;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "RemoteCommand1";
    public static final String PREFS_NAME = "MyPrefsFile";

    public static Session session = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        String sHost = settings.getString("host", "");
        String sPort = settings.getString("port", "22");
        String sUsername = settings.getString("username", "");
        String sPassword = settings.getString("password", "");

        ((EditText) findViewById(R.id.txtHost)).setText(sHost);
        ((EditText) findViewById(R.id.txtPort)).setText(sPort);
        ((EditText) findViewById(R.id.txtUsername)).setText(sUsername);
        ((EditText) findViewById(R.id.txtPassword)).setText(sPassword);
    }

    public void connect(View view) {
        String sHost = ((EditText) findViewById(R.id.txtHost)).getText().toString().trim();
        String sPort = ((EditText) findViewById(R.id.txtPort)).getText().toString().trim();
        String sUsername = ((EditText) findViewById(R.id.txtUsername)).getText().toString().trim();
        String sPassword = ((EditText) findViewById(R.id.txtPassword)).getText().toString().trim();

        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("host", sHost);
        editor.putString("port", sPort);
        editor.putString("username", sUsername);
        editor.putString("password", sPassword);
        editor.commit();

        Log.v(TAG, "params: " + sHost +":"+sPort+":"+sUsername+":"+sPassword);

        if (sHost.isEmpty() || sPort.isEmpty() || sUsername.isEmpty() || sPassword.isEmpty()) {
            Log.v(TAG, "have empty values");
            Context context = getApplicationContext();
            Toast.makeText(context, "Fill all fields", android.widget.Toast.LENGTH_LONG).show();
            return;
        }

        ConnectTask task = new ConnectTask();
        task.execute(sHost, sPort, sUsername, sPassword);
    }

    public void cmdTurnOffDisplay(View view) {
        Log.v(TAG, "exec cmd 1");
        ExecTask task = new ExecTask();
        task.execute("DISPLAY=:0.0 xset dpms force off");
        Log.v(TAG, "back from exec cmd 1");
    }

    public class ConnectTask extends AsyncTask<String, String, String> {
        ProgressDialog dl;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            // TODO Auto-generated method stub
            dl = ProgressDialog.show(MainActivity.this, "Connecting...", "Trying");
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            // TODO Auto-generated method stub
            if (result.equals("ok")) {
                dl.dismiss();
                Context context = getApplicationContext();
                Toast.makeText(context, "Connected", android.widget.Toast.LENGTH_LONG).show();
                Button btnCmd1 = (Button) findViewById(R.id.btnCommand1);
                btnCmd1.setEnabled(true);
            } else {
                dl.dismiss();
                Context context = getApplicationContext();
                Toast.makeText(context, "Connectivity problem, check WiFi", android.widget.Toast.LENGTH_LONG).show();
                Toast.makeText(context, result, android.widget.Toast.LENGTH_LONG).show();
            }
        }

        @Override
        protected String doInBackground(String... params) {
            NetworkInfo networkInfo;

            // TODO Auto-generated method stub
            if (params.length > 0 && params[0].equals("skip")) {
                return "ok";
            }

            Context context = getApplicationContext();
            ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            networkInfo = connManager.getActiveNetworkInfo();

            try {
                if (networkInfo != null) { // connected to the internet
                    if (networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                        // connected to wifi
                        Log.v(TAG, "connected to wifi");

                    } else if (networkInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
                        Log.v(TAG, "connected to mobile");
                        // connected to the mobile provider's data plan
                        return "Error: connected on mobile network";
                    }
                } else {
                    // not connected to the internet
                    return "nok";
                }
            }
            catch (Exception e){
                Log.v(TAG, e.toString());
                return "NOK";
            }

            Properties config = new Properties();
            config.put("StrictHostKeyChecking", "no");
            config.put("compression.s2c", "zlib,none");
            config.put("compression.c2s", "zlib,none");

            JSch jsch = new JSch();
            Session session = null;
            try {
                session = jsch.getSession(params[2], params[0], Integer.parseInt(params[1]));
                session.setConfig(config);
                session.setPassword(params[3]);
                session.connect();
                session.setServerAliveInterval(1000);

            } catch (Exception e) {
                e.printStackTrace();
            }
            MainActivity.session = session;


            return "ok";
        }

    }
}
