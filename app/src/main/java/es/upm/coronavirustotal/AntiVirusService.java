package es.upm.coronavirustotal;

import android.app.IntentService;
import android.content.Intent;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Toast;

public class AntiVirusService extends IntentService {
    public AntiVirusService() {
        super("AntiVirusService");
    }

    public void onCreate() {
        super.onCreate();
        Log.d("Servicio", "Servicio creado");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
// TODO: ESCRIBIR AQUI EL CODIGO QUE QUEREMOS QUE EJECUTE EL SERVICIO

    }

}
