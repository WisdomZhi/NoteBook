package cn.edu.gdqy.notebook;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class MyDatabaseHelper extends SQLiteOpenHelper {
    private final static String DATABASE_NAME = "Notebook.db";
    public final static String TABLE_NOTEBOOK = "notebook";
    private final static int VERSION = 1;
    private final static String CREATE_TABLE_NOTEBOOK =
            "CREATE TABLE " + TABLE_NOTEBOOK +
            "(" +
                "id INTEGER PRIMARY KEY," +
                "date TEXT," +
                "content TEXT" +
            ");";

    public MyDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(CREATE_TABLE_NOTEBOOK);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE " + TABLE_NOTEBOOK);
        onCreate(sqLiteDatabase);
    }
}
