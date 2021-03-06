package es.upm.coronavirustotal;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;


public class MainActivity extends AppCompatActivity implements AsyncResponse {

    String md5_hash = null;
    JSONObject json_response = null;
    Context context = null;
    DB_Antivirus database_antivirus = null;
    DirectoryObserver downloads_observer = new DirectoryObserver(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString(), this);
    String api_key = null;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        downloads_observer.startWatching();
        //Cogemos la API key, si no existe llevamos al usuario a la activity LOGIN
        SharedPreferences preferencia = getSharedPreferences("MiPreferencia", Context.MODE_PRIVATE);
        //API KEY DEL USUARIO
        api_key = preferencia.getString("api_key","0");

        //Si el usuario ya introdujo una KEY no le llevamos a LOGIN (saltamos el if)

        if(api_key.equals("0")) {
            Intent myIntent = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(myIntent);
        }

        File descargas = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        String[] ficherosEnDescargas = descargas.list();
        File[] ficherosEnDescargas_File = descargas.listFiles();

        context = getApplicationContext();
        database_antivirus = new DB_Antivirus(context);
        downloads_observer.context = context;
        downloads_observer.database_antivirus = database_antivirus;

        try {
            for (int i = 0; i < ficherosEnDescargas.length && !api_key.equals("0"); i++) {

                AsyncTask_parameters params = new AsyncTask_parameters(
                        new URL(getString(R.string.url_scan)),
                        new URL(getString(R.string.url_report) + api_key + "&resource="),
                        ficherosEnDescargas_File[i],
                        null,
                        ficherosEnDescargas[i],
                        context,
                        api_key);

                Scan ScanTask = new Scan(this);
                ScanTask.delegate = this;
                ScanTask.execute(params);
            }

//SIMULACIÓN DEL VIRUS EN EL ARCHIVO Lil Vit - LeClub.mp3
            // Instrucciones
            // 1 -> Ejecutar la aplicación al menos una vez para incluir los siguientes registros en BBDD
            // 2 -> Descargar el archivo Lil Vit - LeClub.mp3 del correo para incluir el archivo en la carpeta de descargas.
            if(database_antivirus!=null){
                DatabaseHelper admin = new DatabaseHelper(context);
                SQLiteDatabase base_de_datos = admin.getReadableDatabase();
                    Cursor fila = base_de_datos.rawQuery("Select * from T_ANTIVIRUS where file_name="+"'Lil Vit - LeClub.mp3'",null);
                    if (fila.getCount() == 0) {
                        database_antivirus.createRecords("1234567", "Avast", 1, "Lil Vit - LeClub.mp3");
                        database_antivirus.createRecords("1234567", "Bkav", 1, "Lil Vit - LeClub.mp3");
                        database_antivirus.createRecords("1234567", "Kaspersky", 1, "Lil Vit - LeClub.mp3");
                        database_antivirus.createRecords("1234567", "BitDefender", 1, "Lil Vit - LeClub.mp3");
                    }
                    fila.close();
                    base_de_datos.close();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    void notification (String text, String file, String title) {
        Notification notification = new NotificationCompat.Builder(this,"1")
                .setCategory(Notification.CATEGORY_PROMO)
                .setContentTitle(title)
                .setContentText(text)
                .setSmallIcon(R.drawable.coronavirus_logo)
                .setAutoCancel(true)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setPriority(Notification.PRIORITY_HIGH)
                .setVibrate(new long[]{1000, 1000, 1000, 1000, 1000}).build();
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(1, notification);
    }

    @Override
    public void Scan_Finish(String output, File file, String file_path_string) throws MalformedURLException {
        md5_hash = output;
        File file_path = file;
        Request RequestTask = new Request(this);
        RequestTask.delegate = this;
        AsyncTask_parameters params = new AsyncTask_parameters(
                new URL(getString(R.string.url_scan)),
                new URL(getString(R.string.url_report)+api_key+"&resource="),
                file_path,
                md5_hash,
                file_path_string,
                context,
                api_key);

        RequestTask.execute(params);
    }
    @Override
    public void Request_Finish(JSONObject output, File file, String file_path_string) throws JSONException {
        json_response = output;
        File file_path = file;


        if (json_response != null){

            Iterator<String> keys_scans = json_response.getJSONObject("scans").keys();
            boolean any_detected = false;

            while(keys_scans.hasNext()) {
                String key_scans = keys_scans.next();
                if (json_response.getJSONObject("scans").getJSONObject(key_scans).getString("detected") == "true"){
                    database_antivirus.createRecords(new get_MD5_hash().calculateMD5(file_path), key_scans, 1, file_path_string);
                    any_detected = true;
                }
            }

            if (!any_detected){
                database_antivirus.createRecords(new get_MD5_hash().calculateMD5(file_path), getString(R.string.res_all), 0, file_path_string);
                notification(getString(R.string.res_no_virus)+file_path_string,file_path_string, getString(R.string.res_safe_file));
            } else {
                notification(getString(R.string.res_virus_detected)+ file_path_string,file_path_string, getString(R.string.res_infected_file));
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        File descargas = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        String[] ficherosEnDescargas = descargas.list();

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

}



