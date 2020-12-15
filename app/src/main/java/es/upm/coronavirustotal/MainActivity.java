package es.upm.coronavirustotal;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

//Request libraries
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;


public class MainActivity extends AppCompatActivity {



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try {
            new Request().execute(new URL("https://www.virustotal.com/vtapi/v2/file/report?apikey=2abf2d86fc5ffb6e31404851bdd50f519d9fc4a3aba4263e0b034c69b7d4c1d1&resource=6eb2cc5f1df2fd8d801c9b72758224b2"));
        } catch (IOException e) {
            e.printStackTrace();
        }



        File descargas = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        String [] ficherosEnDescargas = descargas.list();
        Log.d("fichero","archivos en la carpeta de 'Downloads' = " + ficherosEnDescargas.length);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,ficherosEnDescargas);
        ListView listView = (ListView) findViewById(R.id.files_list);
        listView.setAdapter(adapter);

    }


    class Request extends AsyncTask<URL, Void, String> {

        private Exception exception;

        protected String doInBackground(URL... urls) {

            final TextView text = (TextView) findViewById(R.id.request);

            HttpURLConnection connection = null;
            try {
                connection = (HttpURLConnection) urls[0].openConnection();
            } catch (IOException e) {
                e.printStackTrace();
            }
            //connection.setRequestMethod("GET");
            try {
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
                    Log.d("Response: ", "> " + line);   //here u ll get whole response...... :-)

                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        text.setText(buffer.toString());
                    }
                });




/*
                final Scanner scanner = new Scanner(in);
                scanner.useDelimiter("\\A");

                boolean hasInput = scanner.hasNext();
                if (hasInput) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            text.setText(buffer.toString());
                        }
                    });

                    return scanner.next();
                } else {
                    return null;
                }


 */

            } catch (IOException e) {
                e.printStackTrace();
                return null;
            } finally {
                connection.disconnect();
                return null;
            }


        }

    }

}


