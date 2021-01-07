package es.upm.coronavirustotal;

import android.content.Context;
import android.os.Environment;
import android.os.FileObserver;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;

public class DirectoryObserver extends FileObserver implements AsyncResponse{

    String md5_hash = null;
    JSONObject json_response = null;
    Context context;
    DB_Antivirus database_antivirus;

    public DirectoryObserver(String path) {
        super(path);
    }

    @Override
    public void onEvent(int event, String pathString) {
        event &= FileObserver.ALL_EVENTS;
        switch (event) {
            case FileObserver.DELETE_SELF:
                //do stuff
                break;

            case FileObserver.CREATE:
                Log.d("observer","observer' = " + "Se ha descargado un nuevo archivo");
                Log.d("observer","observer' = " + pathString);
                Log.d("observer","observer' = " + getLastModified(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString()));


                AsyncTask_parameters params = null;
                try {
                    params = new AsyncTask_parameters(
                            new URL("https://www.virustotal.com/vtapi/v2/file/scan"),
                            new URL("https://www.virustotal.com/vtapi/v2/file/report?apikey=2abf2d86fc5ffb6e31404851bdd50f519d9fc4a3aba4263e0b034c69b7d4c1d1&resource="),
                            getLastModified(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString()),
                            null,
                            pathString);
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }

                Scan ScanTask = new Scan();
                ScanTask.delegate = this;
                ScanTask.execute(params);




                break;
            case FileObserver.DELETE:
                Log.d("observer","observer' = " + "Se ha borrado un archivo");
                break;
        }
    }

    public static File getLastModified(String directoryFilePath)
    {
        File directory = new File(directoryFilePath);
        File[] files = directory.listFiles();
        long lastModifiedTime = Long.MIN_VALUE;
        File chosenFile = null;

        if (files != null)
        {
            for (File file : files)
            {
                if (file.lastModified() > lastModifiedTime)
                {
                    chosenFile = file;
                    lastModifiedTime = file.lastModified();
                }
            }
        }

        return chosenFile;
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

}