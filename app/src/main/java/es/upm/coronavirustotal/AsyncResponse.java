package es.upm.coronavirustotal;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.net.MalformedURLException;

public interface AsyncResponse {
    void Scan_Finish(String output, File file, String file_path_string) throws MalformedURLException;
    void Request_Finish(JSONObject output, File file, String file_path_string) throws JSONException;
}