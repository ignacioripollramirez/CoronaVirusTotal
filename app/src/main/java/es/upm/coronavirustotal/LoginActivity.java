package es.upm.coronavirustotal;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import org.w3c.dom.Text;

public class LoginActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        final SharedPreferences preferencia =
                getSharedPreferences("MiPreferencia", Context.MODE_PRIVATE);
        String key = preferencia.getString("api-key","0");

        //Si el usuario ya introdujo una key
        if(!key.equals("0")) {
            launchAPP();
        }
        Button accept = findViewById(R.id.accept_button);
        accept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences.Editor editor = preferencia.edit();
                EditText api_key = findViewById(R.id.api_key);
                editor.putString("api_key", api_key.getText().toString());
                Log.d("Key","Clave guardada con exito");
                editor.commit();
                launchAPP();
            }
        });
    }

    private void launchAPP(){
        Intent myIntent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(myIntent);
    }
}