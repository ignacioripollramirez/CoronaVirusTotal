package es.upm.coronavirustotal;

import android.app.ProgressDialog;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.SystemClock;

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

public class Scan extends AsyncTask<AsyncTask_parameters, Void, String>
{

    private Exception exception;
    public AsyncResponse delegate = null;
    File file_path_global = null;
    String file_path_string_global = null;
    String api_key = null;
    private ProgressDialog pd;

    public Scan(MainActivity activity) {
        pd = new ProgressDialog(activity);
    }

    @Override
    protected void onPreExecute() {
        pd.setMessage("Scanning...");
        pd.show();
    }

    protected String doInBackground(AsyncTask_parameters... async_parameters) {
        
        JSONObject jsonreader = null;
        URL url_scan = async_parameters[0].url_scan;
        File file_path = async_parameters[0].file_path;
        String file_path_string = async_parameters[0].file_path_string;
        file_path_global = file_path;
        file_path_string_global = file_path_string;
        Context context = async_parameters[0].context;
        api_key = async_parameters[0].api_key;




        DatabaseHelper admin = new DatabaseHelper(context);
        SQLiteDatabase base_de_datos = admin.getReadableDatabase();
        if(base_de_datos!=null){
            Cursor fila = base_de_datos.rawQuery("Select * from T_ANTIVIRUS where file_name='"+file_path_string+"'",null);
            if (fila.getCount() != 0) {
                if (pd.isShowing()) {
                    pd.dismiss();
                }
                this.cancel(true);
            }
            fila.close();
            base_de_datos.close();
        }



        HttpURLConnection connectionPost = null;
        try {


            connectionPost = (HttpURLConnection) url_scan.openConnection();
            connectionPost.setRequestMethod("POST");
            connectionPost.setDoInput(true);
            connectionPost.setDoOutput(true);

            List<AbstractMap.SimpleEntry> params = new ArrayList<AbstractMap.SimpleEntry>();
            params.add(new AbstractMap.SimpleEntry("apikey", api_key));
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
            jsonreader = new JSONObject(bufferreader.toString());

            is.close();

        } catch (IOException | JSONException e) {
            e.printStackTrace();
            if (pd.isShowing()) {
                pd.dismiss();
            }
        }
        try {
            connectionPost.connect();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            connectionPost.disconnect();
            try {
                return jsonreader.getString("md5");
            } catch (JSONException e) {
                e.printStackTrace();
                return null;
            }
        }

    }

    @Override
    protected void onPostExecute(String result) {

        if (pd.isShowing()) {
            pd.dismiss();
        }

        try {
            delegate.Scan_Finish(result, file_path_global, file_path_string_global);
        } catch (MalformedURLException e) {
            e.printStackTrace();
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
            result.append(URLEncoder.encode(pair.getValue().toString(), "UTF-8"));
        }

        return result.toString();
    }


}