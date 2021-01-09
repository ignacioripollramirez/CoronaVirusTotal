package es.upm.coronavirustotal;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.util.Log;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class Request extends AsyncTask<AsyncTask_parameters, Void, JSONObject>
{
    private Exception exception;
    public AsyncResponse delegate = null;
    File file_path_global = null;
    String file_path_string_global = null;
    private ProgressDialog pd;

    public Request(MainActivity activity) {
        pd = new ProgressDialog(activity);
    }

    @Override
    protected void onPreExecute() {
        pd.setMessage("Requesting report...");
        pd.show();
    }

    protected JSONObject doInBackground(AsyncTask_parameters... async_parameters)
    {
        SystemClock.sleep(10000);
        JSONObject jsonreader = null;
        URL url_scan = async_parameters[0].url_scan;
        URL url_retrieve_report = async_parameters[0].url_retrieve_report;
        File file_path = async_parameters[0].file_path;
        String md5_hash = async_parameters[0].md5_hash;
        String file_path_string = async_parameters[0].file_path_string;
        file_path_global = file_path;
        file_path_string_global = file_path_string;

        //final TextView text = (TextView) findViewById(R.id.request);
        final StringBuffer buffer = new StringBuffer();

        HttpURLConnection connection = null;
        try {
            String reportURLString = url_retrieve_report.toString();
            reportURLString = reportURLString.concat(md5_hash);
            URL reportURL = new URL(reportURLString);
            Log.d("json: ", "> " + reportURL);
            connection = (HttpURLConnection) reportURL.openConnection();
        } catch (IOException e) {
            e.printStackTrace();
        }
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
            String line = "";

            while ((line = reader.readLine()) != null) {
                buffer.append(line+"\n");
            }

            /*
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    //text.setText(buffer.toString());
                    Log.d("Response: ", "> " + buffer.toString());   //here u ll get whole response...... :-)
                }
            });
             */

            //Log.d("return","return log' = " + jsonreader.getString("md5"));

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } finally {
            connection.disconnect();
            try {
                return new JSONObject(buffer.toString());
            } catch (JSONException e) {
                e.printStackTrace();
                return null;
            }
        }

    }

    @Override
    protected void onPostExecute(JSONObject result) {

        if (pd.isShowing()) {
            pd.dismiss();
        }

        Log.d("Request result","Request result' = " + result);
        try {
            delegate.Request_Finish(result, file_path_global, file_path_string_global);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
