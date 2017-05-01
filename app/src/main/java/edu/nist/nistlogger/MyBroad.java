package edu.nist.nistlogger;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.provider.CallLog;
import android.provider.ContactsContract;
import android.support.v4.app.ActivityCompat;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by SiD on 4/8/2017.
 */
public class MyBroad extends BroadcastReceiver {
    static boolean ring = false;
    static boolean callReceived = false;
    private static String callerPhoneNumber;
    MyHelper mh;

    // Check for contact in the phonebook
    private String conTest(Context ctx, String number) {
        String res = null;
        try {
            ContentResolver resolver = ctx.getContentResolver();
            Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(number));
            Cursor c = resolver.query(uri, new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME}, null, null, null);

            if (c != null) { // cursor not null means number is found contactsTable
                if (c.moveToFirst()) {   // so now find the contact Name
                    res = c.getString(c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                }
                c.close();
            }
        } catch (Exception ex) {
        /* Ignore */
        }
        return res;
    }

    //Checks if last call was picked or not.
    public Boolean LastCall(Context con, String phoneNumber) {
        StringBuffer sb = new StringBuffer();
        if (ActivityCompat.checkSelfPermission(con, Manifest.permission.READ_CALL_LOG) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            //Toast.makeText(getApplicationContext(),"Not Granted",Toast.LENGTH_LONG).show();
            return true;
        }
        Cursor cur = con.getContentResolver().query(CallLog.Calls.CONTENT_URI, null, null, null, android.provider.CallLog.Calls.DATE + " DESC");

        int number = cur.getColumnIndex(CallLog.Calls.NUMBER);
        int duration = cur.getColumnIndex(CallLog.Calls.DURATION);
        int type = cur.getColumnIndex(CallLog.Calls.TYPE);
        int cnt = 0;
        sb.append("Call Details : \n");
        cur.moveToFirst();
        do {
            String phNumber = cur.getString(number);
            String callDuration = cur.getString(duration);
            String callType = cur.getString(type);
            sb.append("\nPhone Number:" + phNumber);
            if (phNumber.equals(phoneNumber)) {
               // Toast.makeText(con, "Number Matched : " + phoneNumber, Toast.LENGTH_LONG).show();
                int dircode = Integer.parseInt(callType);
                String dir = "";
                switch (dircode) {
                    case CallLog.Calls.OUTGOING_TYPE:
                        dir = "OUTGOING";
                        break;
                    case CallLog.Calls.INCOMING_TYPE:
                        dir = "INCOMING";
                        break;

                    case CallLog.Calls.MISSED_TYPE:
                        dir = "MISSED";
                        break;
                }
                if (Integer.parseInt(callDuration) > 0 && !dir.equals("MISSED")) {
                    //Toast.makeText(con, "Greater Duration : " + callDuration + " @ " + phoneNumber, Toast.LENGTH_LONG).show();
                    return true;
                } else {
                    //Toast.makeText(con, "Lesser Duration : " + callDuration + " @ " + phoneNumber, Toast.LENGTH_LONG).show();
                    return false;
                }
            }
            if (cnt > 10)
                break;
            cnt += 1;
        } while (cur.moveToNext());
        cur.close();
        return false;
    }

    @Override
    public void onReceive(Context mContext, Intent intent) {

        if (intent.getStringExtra(TelephonyManager.EXTRA_STATE).equals(
                TelephonyManager.EXTRA_STATE_IDLE)) {
            try {
                mh = new MyHelper(mContext);
                String incomingNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);
                SQLiteDatabase SQLiteDb = mh.getWritableDatabase();
                String[] col1 = {"id", "num"};
                Cursor cu1 = SQLiteDb.query("numbers", col1, "hold=?", new String[]{"yes"}, null, null, null);
                cu1.moveToFirst();
                int id = cu1.getInt(0);
                String num = cu1.getString(1);
                cu1.close();
                SQLiteDb.close();
               // Toast.makeText(mContext, incomingNumber + " @ " + num, Toast.LENGTH_LONG).show();
                if (num.equals(incomingNumber)) {
                    if (LastCall(mContext, incomingNumber)) {
                        //Toast.makeText(mContext, "Inside True @ id: " + id, Toast.LENGTH_LONG).show();
                        SQLiteDatabase SDB = mh.getWritableDatabase();
                        String query = "update numbers set hold = 'no', picked = 'yes' where id = " + id;
                        SDB.execSQL(query);
                        SDB.close();
                    } else {
                        //Toast.makeText(mContext, "Inside False @ id: " + id, Toast.LENGTH_LONG).show();
                        SQLiteDatabase SDB = mh.getWritableDatabase();
                        String query = "update numbers set hold = 'no', picked = 'no' where id = " + id;
                        SDB.execSQL(query);
                        SDB.close();
                    }
                    Intent i = new Intent(mContext, Logger.class);
                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    mContext.getApplicationContext().startActivity(i);
                }
            } catch (Exception e) {

            }
            //Toast.makeText(mContext,"Ya i am ",Toast.LENGTH_LONG).show();
            // This code will execute when the call is answered or disconnected
        }

        // Get the current Phone State
        String state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);

        TelephonyManager telephony = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);


        telephony.listen(new PhoneStateListener() {
            @Override
            public void onCallStateChanged(int state, String incomingNumber) {
                //super.onCallStateChanged(state, incomingNumber);
                callerPhoneNumber = incomingNumber;
            }
        }, PhoneStateListener.LISTEN_CALL_STATE);

        if (state == null)
            return;

        // If phone state "Rininging"
        if (state.equals(TelephonyManager.EXTRA_STATE_RINGING)) {
            ring = true;
            // Get the Caller's Phone Number
//            Bundle bundle = intent.getExtras();
//            callerPhoneNumber = bundle.getString("incoming_number");
        }


        // If incoming call is received
        if (state.equals(TelephonyManager.EXTRA_STATE_OFFHOOK)) {
            callReceived = true;
        }

        //Toast.makeText(mContext, callReceived + "", Toast.LENGTH_SHORT).show();

        // If phone is Idle
        if (state.equals(TelephonyManager.EXTRA_STATE_IDLE)) {
            // If phone was ringing(ring=true) and not received(callReceived=false) , then it is a missed call
            if (ring == true) {

                if (conTest(mContext, callerPhoneNumber) == null) {

                    //Toast.makeText(mContext, "It was A MISSED CALL from : " + callerPhoneNumber, Toast.LENGTH_LONG).show();

                    //Saving to database
                    Date cDate = new Date();
                    mh = new MyHelper(mContext);
                    try {
                        SQLiteDatabase SQLiteDb = mh.getWritableDatabase();
                        String query = "INSERT OR IGNORE INTO numbers (num,hold,picked,call_date) VALUES('" + callerPhoneNumber + "','yes','no','" + new SimpleDateFormat("yyyy-MM-dd").format(cDate) + "')";
                        SQLiteDb.execSQL(query);
                        query = "UPDATE numbers set hold='no', picked='no' where num = '" + callerPhoneNumber + "'";
                        SQLiteDb.execSQL(query);
                        query = "INSERT INTO numberLog (num,call_date,call_time) VALUES('" + callerPhoneNumber + "','" + new SimpleDateFormat("yyyy-MM-dd").format(cDate) + "','" + new SimpleDateFormat("HH:mm").format(new Date()) + "');";
                        SQLiteDb.execSQL(query);

                        if (callReceived == true) {
                            //Code to check if the call is outgoing, coz we will not record outgoing calls
                            if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.READ_CALL_LOG) != PackageManager.PERMISSION_GRANTED) {
                                // TODO: Consider calling
                                //    ActivityCompat#requestPermissions
                                // here to request the missing permissions, and then overriding
                                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                //                                          int[] grantResults)
                                // to handle the case where the user grants the permission. See the documentation
                                // for ActivityCompat#requestPermissions for more details.
                                return;
                            }
                            Cursor cur = mContext.getContentResolver().query(CallLog.Calls.CONTENT_URI, null, null, null, android.provider.CallLog.Calls.DATE + " DESC");

                            int number = cur.getColumnIndex( CallLog.Calls.NUMBER );
                            int duration = cur.getColumnIndex( CallLog.Calls.DURATION);
                            int type = cur.getColumnIndex(CallLog.Calls.TYPE);
                            int cnt =0;
                            cur.moveToFirst();
                            do {
                                String phNumber = cur.getString( number );
                                String callDuration = cur.getString( duration );
                                String callType = cur.getString(type);
                                if(phNumber.equals(callerPhoneNumber))
                                {
                                   // Toast.makeText(mContext,"Number Matched : "+callerPhoneNumber,Toast.LENGTH_LONG).show();
                                    int dircode = Integer.parseInt(callType);
                                    String dir="";
                                    switch (dircode) {
                                        case CallLog.Calls.OUTGOING_TYPE:
                                            dir = "OUTGOING";
                                            break;
                                        case CallLog.Calls.INCOMING_TYPE:
                                            dir = "INCOMING";
                                            break;

                                        case CallLog.Calls.MISSED_TYPE:
                                            dir = "MISSED";
                                            break;
                                    }
                                    // Checking for not outgoing CAll
                                    if(Integer.parseInt(callDuration) > 0 && !dir.equals("OUTGOING")) {
                                       // Toast.makeText(mContext,"Ringing Stored !",Toast.LENGTH_LONG).show();
                                        query = "INSERT OR IGNORE INTO numbers (num,hold,picked,call_date) VALUES('"+callerPhoneNumber+"','no','yes','"+new SimpleDateFormat("yyyy-MM-dd").format(cDate)+"')";
                                        SQLiteDb.execSQL(query);
                                        query = "UPDATE numbers set hold='no', picked='yes' where num = '"+callerPhoneNumber+"'";
                                        SQLiteDb.execSQL(query);
                                    }
                                }
                                if(cnt>10)
                                    break;
                                cnt +=1;
                            }while ( cur.moveToNext());
                            cur.close();
                        }
                        SQLiteDb.close();
                    }
                    catch (Exception e)
                    {

                    }

                }
            }
        }
    }
}