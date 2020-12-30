package es.upm.coronavirustotal;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;

public class FileActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file);
        Intent intent = getIntent();
        String value = intent.getStringExtra("nombre del fichero");
        TextView file_name = findViewById(R.id.file_name);
        file_name.setText(value+"\nHash | Antivirus | Result | File Name");

//Acceso a la base de datos para coger los valores
        DatabaseHelper admin = new DatabaseHelper(this);
        SQLiteDatabase base_de_datos = admin.getReadableDatabase();
        String[] antivirus_results = null;
        if(base_de_datos!=null){
            Cursor fila = base_de_datos.rawQuery("Select * from T_ANTIVIRUS where file_name='nachofile.pdf'",null);
            fila.moveToFirst();
            antivirus_results = new String[fila.getCount()*4];
            for (int j=0, i = 0; j<fila.getCount();j++, i=i+4) {
                antivirus_results[i]=fila.getString(0);
                antivirus_results[i+1]=fila.getString(1);
                antivirus_results[i+2]=fila.getString(2);
                antivirus_results[i+3]=fila.getString(3);
                Log.d("campos", fila.getString(0));
                Log.d("campos", fila.getString(1));
                Log.d("campos", fila.getString(2));
                Log.d("campos", fila.getString(3));
                fila.moveToNext();
            }

            fila.close();
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,antivirus_results);
        GridView gridView = findViewById(R.id.analysis_result);
        gridView.setAdapter(adapter);

    }
}