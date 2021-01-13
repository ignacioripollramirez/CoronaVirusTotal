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
    MainActivity main_activity;
    String pathString_global;
    String api_key;


    public DirectoryObserver(String path, MainActivity activity) {
        super(path);
        main_activity = activity;
    }

    @Override
    public void onEvent(int event, String pathString) {

        api_key = main_activity.getSharedPreferences("MiPreferencia", Context.MODE_PRIVATE).getString("api_key","0");

        event &= FileObserver.ALL_EVENTS;
        switch (event) {

            case FileObserver.CREATE:

                pathString_global = pathString;
                try {
                    AsyncTask_parameters params = null;
                    params = new AsyncTask_parameters(
                            new URL(context.getString(R.string.url_scan)),
                            new URL(context.getString(R.string.url_report)+api_key+"&resource="),
                            getLastModified(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString()),
                            null,
                            pathString,
                            context,
                            api_key);
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }


                main_activity.runOnUiThread(new Runnable() {
                    public void run() {
                        Scan ScanTask = new Scan(main_activity);
                        ScanTask.delegate = main_activity;
                        AsyncTask_parameters params = null;
                        try {
                            params = new AsyncTask_parameters(
                                    new URL(context.getString(R.string.url_scan)),
                                    new URL(context.getString(R.string.url_report) + api_key + "&resource="),
                                    getLastModified(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString()),
                                    null,
                                    pathString_global,
                                    context,
                                    api_key);
                        } catch (MalformedURLException e) {
                            e.printStackTrace();
                        }
                        ScanTask.execute(params);
                    }
                });

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
        Request RequestTask = new Request(main_activity);
        RequestTask.delegate = this;
        AsyncTask_parameters params = new AsyncTask_parameters(
                new URL(context.getString(R.string.url_scan)),
                new URL(context.getString(R.string.url_report)+api_key+"&resource="),
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
                database_antivirus.createRecords(new get_MD5_hash().calculateMD5(file_path), "Todos", 0, file_path_string);
            }
        }
    }

}