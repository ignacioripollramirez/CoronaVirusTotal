package es.upm.coronavirustotal;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class DB_Antivirus
{

    private DatabaseHelper dbHelper;

    private SQLiteDatabase database;

    public final static String ANTIVIRUS_TABLE="T_ANTIVIRUS"; // name of table

    public final static String FILE_ID="_id_file"; // id value for antivirus
    public final static String ANTIVIRUS_ID="_id_antivirus_name"; // id value for antivirus
    public final static String ANTIVIRUS_RESULT="result";  // result of antivirus
    public final static String FILE_NAME="file_name";  // name of antivirus


    /**
     *
     * @param context
     */
    public DB_Antivirus(Context context){
        dbHelper = new DatabaseHelper(context);
        database = dbHelper.getWritableDatabase();
        //database.execSQL("DROP TABLE IF EXISTS T_ANTIVIRUS");
        //dbHelper.onCreate(database);
    }


    public long createRecords(String id_file, String id_antivirus_name, int result, String name){
        ContentValues values = new ContentValues();
        values.put(FILE_ID, id_file);
        values.put(ANTIVIRUS_ID, id_antivirus_name);
        values.put(ANTIVIRUS_RESULT, result);
        values.put(FILE_NAME, name);
        Log.d("database", "database" + values);
        return database.insert(ANTIVIRUS_TABLE, null, values);
    }

    public Cursor selectRecords() {
        String[] cols = new String[] {FILE_ID, ANTIVIRUS_ID, ANTIVIRUS_RESULT, FILE_NAME};
        Cursor mCursor = database.query(false, ANTIVIRUS_TABLE, cols,null, null, null, null, null, null);
        if (!mCursor.moveToFirst()) {
            return null;
        }

        while (!mCursor.isAfterLast()) {
            Log.d("database","database' = " + mCursor.getString(3));
            mCursor.moveToNext();
        }
        mCursor.moveToFirst();

        return mCursor;
    }
}
