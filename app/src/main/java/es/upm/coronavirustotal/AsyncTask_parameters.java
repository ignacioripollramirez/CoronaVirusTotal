package es.upm.coronavirustotal;

import java.io.File;
import java.net.URL;

public class AsyncTask_parameters
{
    URL url_scan;
    URL url_retrieve_report;
    File file_path;
    String md5_hash;
    String file_path_string;

    AsyncTask_parameters(URL url_scan, URL url_retrieve_report, File file_path, String md5_hash, String file_path_string) {
        this.url_scan = url_scan;
        this.url_retrieve_report = url_retrieve_report;
        this.file_path = file_path;
        this.md5_hash = md5_hash;
        this.file_path_string = file_path_string;
    }
}