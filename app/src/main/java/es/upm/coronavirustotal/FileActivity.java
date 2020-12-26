package es.upm.coronavirustotal;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.TextView;

public class FileActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file);
        Intent intent = getIntent();
        String value = intent.getStringExtra("nombre del fichero");
        TextView file_name = findViewById(R.id.file_name);
        file_name.setText(value);

        String[] antivirus_results = {"Windows","false","Karsperky","true","Panda","false","Eset","false"};
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,antivirus_results);
        GridView gridView = findViewById(R.id.analysis_result);
        gridView.setAdapter(adapter);
    }
}