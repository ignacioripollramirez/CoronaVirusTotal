package es.upm.coronavirustotal;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

public class DB_Antivirus
{

    private DatabaseHelper dbHelper;

    private SQLiteDatabase database;

    public final static String ANTIVIRUS_TABLE="T_ANTIVIRUS"; // name of table

    public final static String FILE_ID="_id_file"; // id value for antivirus
    public final static String ANTIVIRUS_ID="_id_antivirus_name"; // id value for antivirus
    public final static String ANTIVIRUS_RESULT="result";  // result of antivirus
    public final static String FILE_NAME="file_name";  // name of antivirus

    public DB_Antivirus(Context context){
        dbHelper = new DatabaseHelper(context);
        database = dbHelper.getWritableDatabase();
    }

    public long createRecords(String id_file, String id_antivirus_name, int result, String name){
        ContentValues values = new ContentValues();
        values.put(FILE_ID, id_file);
        values.put(ANTIVIRUS_ID, id_antivirus_name);
        values.put(ANTIVIRUS_RESULT, result);
        values.put(FILE_NAME, name);
        return database.insert(ANTIVIRUS_TABLE, null, values);
    }

}
