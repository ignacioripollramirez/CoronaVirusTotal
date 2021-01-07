package es.upm.coronavirustotal;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.FileObserver;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.concurrent.ExecutionException;

//Request libraries


public class MainActivity extends AppCompatActivity implements AsyncResponse {


    String md5_hash = null;
    JSONObject json_response = null;
    Context context = null;
    DB_Antivirus database_antivirus = null;
    DirectoryObserver downloads_observer = new DirectoryObserver(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString());

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        downloads_observer.startWatching();
/*
        //Cogemos la API key, si no existe llevamos al usuario a la activity LOGIN
        SharedPreferences preferencia = getSharedPreferences("MiPreferencia", Context.MODE_PRIVATE);
        //API KEY DEL USUARIO
        String key = preferencia.getString("api_key","0");
        //Si el usuario ya introdujo una KEY no le llevamos a LOGIN (saltamos el if)
        if(key.equals("0")) {
            Intent myIntent = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(myIntent);
        }

        //Borro la api key anterior en cada ejecucion para las PRUEBAS
        SharedPreferences.Editor editor = preferencia.edit();
        editor.remove("api_key");
        editor.commit();
*/

        File descargas = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        String[] ficherosEnDescargas = descargas.list();
        File[] ficherosEnDescargas_File = descargas.listFiles();

        notification("Virus has been detected on file"+"dario.pdf","dario.pdf");
        //eicar(this,"virus.txt","X5O!P%@AP[4\\PZX54(P^)7CC)7}$EICAR-STANDARD-ANTIVIRUS-TEST-FILE!$H+H*");
        //Log.d("eicar","X5O!P%@AP[4\\PZX54(P^)7CC)7}$EICAR-STANDARD-ANTIVIRUS-TEST-FILE!$H+H*");


        context = getApplicationContext();
        database_antivirus = new DB_Antivirus(context);
        downloads_observer.context = context;
        downloads_observer.database_antivirus = database_antivirus;

        try {
            for (int i = 0; i < ficherosEnDescargas.length; i++) {

                AsyncTask_parameters params = new AsyncTask_parameters(
                        new URL("https://www.virustotal.com/vtapi/v2/file/scan"),
                        new URL("https://www.virustotal.com/vtapi/v2/file/report?apikey=2abf2d86fc5ffb6e31404851bdd50f519d9fc4a3aba4263e0b034c69b7d4c1d1&resource="),
                        ficherosEnDescargas_File[i],
                        null,
                        ficherosEnDescargas[i]);

                Scan ScanTask = new Scan();
                ScanTask.delegate = this;
                ScanTask.execute(params);
            }

//            Log.d("database","database' = " + database_antivirus.selectRecords().getString(3));

        } catch (IOException e) {
            e.printStackTrace();
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

        Notification notification = new NotificationCompat.Builder(this,"1")
                .setCategory(Notification.CATEGORY_PROMO)
                .setContentTitle("Alert, Virus detected!")
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

    @Override
    protected void onStart() {
        super.onStart();
        Intent passDataIntent = new Intent(this, AntiVirusService.class);
        startService(passDataIntent);
    }

    @Override
    public void Scan_Finish(String output, File file, String file_path_string) throws MalformedURLException {
        md5_hash = output;
        File file_path = file;
        Request RequestTask = new Request();
        RequestTask.delegate = this;
        AsyncTask_parameters params = new AsyncTask_parameters(
                new URL("https://www.virustotal.com/vtapi/v2/file/scan"),
                new URL("https://www.virustotal.com/vtapi/v2/file/report?apikey=2abf2d86fc5ffb6e31404851bdd50f519d9fc4a3aba4263e0b034c69b7d4c1d1&resource="),
                file_path,
                md5_hash,
                file_path_string);

        RequestTask.execute(params);
    }
    @Override
    public void Request_Finish(JSONObject output, File file, String file_path_string) throws JSONException {
        json_response = output;
        File file_path = file;


        if (json_response != null){
            //Log.d("json","json' = " + json_response.getJSONObject("scans"));

            Iterator<String> keys_scans = json_response.getJSONObject("scans").keys();
            boolean any_detected = false;

            while(keys_scans.hasNext()) {
                String key_scans = keys_scans.next();
                Log.d("json","json' = " + key_scans + " -> " + json_response.getJSONObject("scans").getJSONObject(key_scans).getString("detected"));
                //if (json_response.getJSONObject("scans").get(key_scans) instanceof JSONObject)
                if (json_response.getJSONObject("scans").getJSONObject(key_scans).getString("detected") == "true"){
                    database_antivirus.createRecords(new get_MD5_hash().calculateMD5(file_path), key_scans, 1, file_path_string);
                    any_detected = true;
                }
            }

            if (!any_detected){
                database_antivirus.createRecords(new get_MD5_hash().calculateMD5(file_path), "Todos", 0, file_path_string);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        File descargas = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        String[] ficherosEnDescargas = descargas.list();
        File[] ficherosEnDescargas_File = descargas.listFiles();

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
    }

    /*public void eicar(Context context, String sFileName, String sBody) {
        try {
            File root = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            if (!root.exists()) {
                root.mkdirs();
            }
            File gpxfile = new File(root, sFileName);
            FileWriter writer = new FileWriter(gpxfile);
            writer.append(sBody);
            writer.flush();
            writer.close();
            Toast.makeText(context, "Saved", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }*/
}



