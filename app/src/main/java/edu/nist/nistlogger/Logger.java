package edu.nist.nistlogger;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class Logger extends AppCompatActivity {

    MyHelper mh;
    Button datepick;
    EditText dateset;
    Calendar myCalendar;

    @Override
    public void onBackPressed() {
        finishAffinity();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logger);

        datepick = (Button) findViewById(R.id.datePicker);
        dateset = (EditText) findViewById(R.id.dateText);

        //Granting Runtime Permissions For Marshmallow
        int PERMISSION_ALL = 1;
        String[] PERMISSIONS = {Manifest.permission.READ_CONTACTS, Manifest.permission.WRITE_CONTACTS, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_CALL_LOG, Manifest.permission.READ_PHONE_STATE, Manifest.permission.CALL_PHONE};

        if(!hasPermissions(this, PERMISSIONS)){
            ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);
        }
        myCalendar = Calendar.getInstance();
        Date cDate = new Date();
        dateset.setText(new SimpleDateFormat("yyyy-MM-dd").format(cDate));
        fetch(dateset.getText().toString());

        final DatePickerDialog.OnDateSetListener cdate = new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                // TODO Auto-generated method stub
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, monthOfYear);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                updateDate();
            }

        };
        datepick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DatePickerDialog(Logger.this, cdate, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });
    }



    private void updateDate() {

        String myFormat = "yyyy-MM-dd"; //In which you need put here
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
        dateset.setText(sdf.format(myCalendar.getTime()));
        try {
            fetch(sdf.format(myCalendar.getTime()));
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), "Something went wrong !", Toast.LENGTH_LONG).show();
        }
    }

    //Checking for permissions when the Android Version is Greater than M
    public static boolean hasPermissions(Context context, String... permissions) {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    //Exporting The File to the SD Card
    private void exportDB() {

        File dbFile = getDatabasePath("NISTLogger.db");
        MyHelper dbhelper = new MyHelper(getApplicationContext());
        File exportDir = new File(Environment.getExternalStorageDirectory(), "");
        if (!exportDir.exists()) {
            exportDir.mkdirs();
        }

        // Saving the file !
        File file = new File(exportDir, "log-"+new SimpleDateFormat("yyyy-MM-dd").format(new Date())+".csv");
        try {
            file.createNewFile();
            CSVWriter csvWrite = new CSVWriter(new FileWriter(file));
            SQLiteDatabase db = dbhelper.getReadableDatabase();
            Cursor curCSV = db.rawQuery("SELECT num, call_date, picked from numbers where call_date = '"+dateset.getText().toString()+"'", null);
            csvWrite.writeNext(curCSV.getColumnNames());
            while (curCSV.moveToNext()) {
                //Which column you want to exprort
                String num = curCSV.getString(0).substring(3);
                //Toast.makeText(getApplicationContext(),num , Toast.LENGTH_SHORT).show();
                String arrStr[] = {num, curCSV.getString(1), curCSV.getString(2)};
                csvWrite.writeNext(arrStr);
            }
            csvWrite.close();
            curCSV.close();
            Toast.makeText(getApplicationContext(), "Successfully Exported ! ", Toast.LENGTH_SHORT).show();
        } catch (Exception sqlEx) {
            Toast.makeText(getApplicationContext(), "Something went wrong !", Toast.LENGTH_SHORT).show();
            Log.e("Logger", sqlEx.getMessage(), sqlEx);
        }
    }

    private int getCount(String number, String date) {
        mh = new MyHelper(Logger.this);
        String countQuery = "SELECT  num FROM numberLog where call_date = '" + date +"' and num = '"+ number +"'";
        SQLiteDatabase db = mh.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        int cnt = cursor.getCount();
        cursor.close();
        return cnt;
    }
    private void fetch(String idate) {
        mh = new MyHelper(Logger.this);
        ListView list = (ListView) findViewById(R.id.listView);
        final ArrayList<MyData> datalist = new ArrayList<MyData>();
        //Retrive The Data from the database !
        try{
            SQLiteDatabase SQLiteDb = mh.getWritableDatabase();
            String[] col1 = {"id","num", "picked", "call_date"};
            Cursor cu1 = SQLiteDb.query("numbers", col1, "call_date=?", new String[]{dateset.getText().toString()}, null, null, null);
            cu1.moveToFirst();
            // Toast.makeText(getApplicationContext(), cu1.getCount() + "", Toast.LENGTH_LONG).show();
            do {
                MyData datafetcher = new MyData();
                datafetcher.setid(cu1.getInt(0));
                datafetcher.setNumber(cu1.getString(1) + "");
                datafetcher.setDate("Has Called you " +getCount(cu1.getString(1),dateset.getText().toString())+ " time(s) today !");
                datafetcher.setPicked(cu1.getString(2) + "");
                datalist.add(datafetcher);
            } while (cu1.moveToNext());
            cu1.close();
            SQLiteDb.close();
            MyBaseAdapter datafetchadapter = new MyBaseAdapter(Logger.this, datalist);
            list.setAdapter(datafetchadapter);
            list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {

                    mh = new MyHelper(Logger.this);
                    SQLiteDatabase SQLiteDb = mh.getWritableDatabase();

                    //Showing Dialog
                    final Dialog d = new Dialog(Logger.this);
                    d.setContentView(R.layout.view_calls);
                    ListView temp = (ListView) d.findViewById(R.id.calllist);
                    Button call = (Button) d.findViewById(R.id.callbutton);

                    final ArrayList<MyData> templist = new ArrayList<MyData>();
                    //Retrive The Data from the database !
                    try {
                        String query = "Select num, call_date, call_time from numberLog where num = '"+datalist.get(position).getNumber()+"' and call_date = '"+dateset.getText().toString()+"'";
                        Cursor cu1 = SQLiteDb.rawQuery(query,null);
                        cu1.moveToFirst();
                        // Toast.makeText(getApplicationContext(), cu1.getCount() + "", Toast.LENGTH_LONG).show();
                        do {
                            MyData datafetcher = new MyData();
                            datafetcher.setNumber(cu1.getString(0) + "");
                            datafetcher.setDate("On : "+cu1.getString(1) + ", At " + cu1.getString(2) +" Hrs.");
                            datafetcher.setPicked("");
                            templist.add(datafetcher);
                        } while (cu1.moveToNext());
                        cu1.close();
                        SQLiteDb.close();
                        MyBaseAdapter tempfetchadapter = new MyBaseAdapter(Logger.this, templist);
                        temp.setAdapter(tempfetchadapter);
                    }
                    catch (Exception e)
                    {

                    }
                    final int pos = position;
                    call.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mh = new MyHelper(Logger.this);
                            SQLiteDatabase SQLiteDb = mh.getWritableDatabase();
                            String query = "update numbers set hold = 'yes' where id = "+datalist.get(pos).getid();
                            SQLiteDb.execSQL(query);
                            SQLiteDb.close();
                            String uri = "tel:" + datalist.get(pos).getNumber();
                            //Toast.makeText(Logger.this,)
                            d.hide();
                            Intent intent = new Intent(Intent.ACTION_CALL);
                            intent.setData(Uri.parse(uri));
                            if (ActivityCompat.checkSelfPermission(Logger.this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                                // TODO: Consider calling
                                //    ActivityCompat#requestPermissions
                                // here to request the missing permissions, and then overriding
                                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                //                                          int[] grantResults)
                                // to handle the case where the user grants the permission. See the documentation
                                // for ActivityCompat#requestPermissions for more details.
                                return;
                            }
                            startActivity(intent);
                            finishAffinity();
                        }
                    });
                    d.show();

                        // later Part !

                }
            });
        }
        catch (Exception e){}
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu); //y
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.export:
                exportDB();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

}
