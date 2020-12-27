package es.upm.coronavirustotal;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

//Request libraries
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;


public class MainActivity extends AppCompatActivity {



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        File descargas = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        String [] ficherosEnDescargas = descargas.list();
        //Log.d("fichero","archivos en la carpeta de 'Downloads' = " + ficherosEnDescargas.length);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,ficherosEnDescargas);
        ListView listView = (ListView) findViewById(R.id.files_list);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                // selected item
                String file = ((TextView) view).getText().toString();

                // Launching new Activity on selecting single List Item
                Intent myIntent = new Intent(getApplicationContext(), FileActivity.class);
                // sending data to new activity
                myIntent.putExtra("nombre del fichero", file);
                startActivity(myIntent);
            }
        });
        notification("Virus has been detected on file"+"dario.pdf","dario.pdf");

        try {
            for (int i = 0; i < ficherosEnDescargas.length; i++) {

                AsyncTask_parameters params = new AsyncTask_parameters(
                        new URL("https://www.virustotal.com/vtapi/v2/file/scan"),
                        new URL("https://www.virustotal.com/vtapi/v2/file/report?apikey=2abf2d86fc5ffb6e31404851bdd50f519d9fc4a3aba4263e0b034c69b7d4c1d1&resource="),
                        ficherosEnDescargas[i]);
                new Request().execute(params);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    class Request extends AsyncTask<AsyncTask_parameters, Void, String> {

        private Exception exception;

        protected String doInBackground(AsyncTask_parameters... async_parameters) {

            URL url_scan = async_parameters[0].url_scan;
            URL url_retrieve_report = async_parameters[0].url_retrieve_report;
            String file_path = async_parameters[0].file_path;
            Log.d("file","file' = " + file_path);

            final TextView text = (TextView) findViewById(R.id.request);

            HttpURLConnection connectionPost = null;
            HttpURLConnection connection = null;
            try {
                connectionPost = (HttpURLConnection) url_scan.openConnection();
                connectionPost.setRequestMethod("POST");
                connectionPost.setDoInput(true);
                connectionPost.setDoOutput(true);

                List<AbstractMap.SimpleEntry> params = new ArrayList<AbstractMap.SimpleEntry>();
                params.add(new AbstractMap.SimpleEntry("apikey", "2abf2d86fc5ffb6e31404851bdd50f519d9fc4a3aba4263e0b034c69b7d4c1d1"));
                params.add(new AbstractMap.SimpleEntry("file", file_path));

                OutputStream os = connectionPost.getOutputStream();
                BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(os, "UTF-8"));
                writer.write(getQuery(params));
                writer.flush();
                writer.close();
                os.close();


                StringBuffer bufferreader = new StringBuffer();
                InputStream is = connectionPost.getInputStream();
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(is));
                String line = null;
                while ((line = reader.readLine()) != null)
                    bufferreader.append(line + "\r\n");
                // reading your response
                JSONObject jsonreader = new JSONObject(bufferreader.toString());

                Log.d("json","respuesta de escaneo' = " + jsonreader.getString("md5"));
                is.close();

                String reportURLString = url_retrieve_report.toString();
                reportURLString = reportURLString.concat(jsonreader.getString("md5"));
                URL reportURL = new URL(reportURLString);
                Log.d("url: ", "> " + reportURL);
                connection = (HttpURLConnection) reportURL.openConnection();
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
            //connection.setRequestMethod("GET");
            try {
                connectionPost.connect();
                connection.connect();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {

                InputStream in = null;
                try {
                    in = connection.getInputStream();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                BufferedReader reader = null;
                reader = new BufferedReader(new InputStreamReader(in));
                final StringBuffer buffer = new StringBuffer();
                String line = "";

                while ((line = reader.readLine()) != null) {
                    buffer.append(line+"\n");
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        text.setText(buffer.toString());
                        Log.d("Response: ", "> " + buffer.toString());   //here u ll get whole response...... :-)
                    }
                });


            } catch (IOException e) {
                e.printStackTrace();
                return null;
            } finally {
                connection.disconnect();
                return null;
            }


        }

    }

    private String getQuery(List<AbstractMap.SimpleEntry> params) throws UnsupportedEncodingException
    {
        StringBuilder result = new StringBuilder();
        boolean first = true;

        for (AbstractMap.SimpleEntry pair : params)
        {
            if (first)
                first = false;
            else
                result.append("&");

            result.append(URLEncoder.encode((String) pair.getKey(), "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode((String) pair.getValue(), "UTF-8"));
        }

        return result.toString();
    }

    private static class AsyncTask_parameters {
        URL url_scan;
        URL url_retrieve_report;
        String file_path;

        AsyncTask_parameters(URL url_scan, URL url_retrieve_report, String file_path) {
            this.url_scan = url_scan;
            this.url_retrieve_report = url_retrieve_report;
            this.file_path = file_path;
        }
    }

    void toast_Message (String text){
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }
    void notification (String text, String file) {
        // Launching new Activity on selecting single List Item
        Intent myIntent = new Intent(getApplicationContext(), FileActivity.class);
        // sending data to new activity
        myIntent.putExtra("nombre del fichero", file);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 2, myIntent, 0);

        Notification notification = new NotificationCompat.Builder(this)
                .setCategory(Notification.CATEGORY_PROMO)
                .setContentTitle("Virus detected")
                .setContentText(text)
                .setSmallIcon(R.drawable.coronavirus_logo)
                .setAutoCancel(true)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .addAction(android.R.drawable.ic_menu_view, "See File", contentIntent)
                .setContentIntent(contentIntent)
                .setPriority(Notification.PRIORITY_HIGH)
                .setVibrate(new long[]{1000, 1000, 1000, 1000, 1000}).build();
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(1, notification);
    }


}
