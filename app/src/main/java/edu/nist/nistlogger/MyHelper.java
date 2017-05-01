package edu.nist.nistlogger;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

/**
 * Created by SiD on 4/7/2017.
 */
public class MyHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME ="NISTLogger";
    private static final int DATABASE_VERSION =1;
    Context context;


    public MyHelper(Context context) {
//Super(context, databaseName, CustomCursor, Database_Version);
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
//Toast.makeText(context, "Constructor Is Called ", Toast.LENGTH_LONG).show();

    }


    @Override
    public void onCreate(SQLiteDatabase db)
    {
        String tab1="CREATE TABLE numberLog (num varchar(15), call_date varchar(10), call_time varchar(5))";
        String tab2="CREATE TABLE numbers (id INTEGER PRIMARY KEY AUTOINCREMENT, num varchar(15), call_date varchar(10), hold varchar(3), picked varchar(3), unique(num, call_date))";
        try
        {
            db.execSQL(tab1);
            db.execSQL(tab2);
        }
        catch (SQLException e)
        {
            Toast.makeText(context, e+"", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int arg1, int arg2) {
        //Toast.makeText(context, "OnUpgrade Called", Toast.LENGTH_LONG).show();
        String tab1="DROP TABLE IF EXIST numbers";
        String tab2="DROP TABLE IF EXIST numbers";
        try {
            db.execSQL(tab1);
            db.execSQL(tab2);
            onCreate(db);
        }
        catch (SQLException e)
        {
            Toast.makeText(context, e+"", Toast.LENGTH_LONG).show();
        }

    }

}

