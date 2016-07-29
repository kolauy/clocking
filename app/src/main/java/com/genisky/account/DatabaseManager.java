package com.genisky.account;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.telephony.TelephonyManager;

import java.util.ArrayList;
import java.util.List;

public class DatabaseManager implements AutoCloseable {
    private final SQLiteDatabase _database;
    private final Context _context;
    private DatabaseHelper _helper;

    public DatabaseManager(Context context){
        _context = context;
        _helper = new DatabaseHelper(context);
        _database = _helper.getWritableDatabase();
    }

    public boolean isAccountExist() {
        try (Cursor cursor = _database.rawQuery("select imei from account", new String[0])) {
            int count = cursor.getCount();
            if (count != 1)
                return false;
            cursor.moveToFirst();
            String imei = cursor.getString(cursor.getColumnIndex("imei"));
            if(imei == null){
                return false;
            }
            String current = ((TelephonyManager) _context.getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId();
            return imei.equals(current);
        }
    }

    public AuthenticationRequest getAuthenticationRequest() {
        try (Cursor cursor = _database.query("account", new String[]{"phone", "password", "imei"}, "", new String[0], "", "", "")) {
            cursor.moveToFirst();
            AuthenticationRequest info = new AuthenticationRequest();
            info.phone = cursor.getString(cursor.getColumnIndex("phone"));
            info.password = cursor.getString(cursor.getColumnIndex("password"));
            info.imei = cursor.getString(cursor.getColumnIndex("imei"));
            return info;
        }
    }

    public AuthenticationResponse getAuthenticationResult() {
        try (Cursor cursor = _database.query("account", new String[]{"id", "services", "token"}, "", new String[0], "", "", "")) {
            cursor.moveToFirst();
            AuthenticationResponse info = new AuthenticationResponse();
            info.id = cursor.getInt(cursor.getColumnIndex("id"));
            info.services = cursor.getString(cursor.getColumnIndex("services"));
            info.token = cursor.getString(cursor.getColumnIndex("token"));
            return info;
        }
    }

    public void close()
    {
        _helper.close();
    }

    public void saveAccount(AuthenticationRequest request, AuthenticationResponse response) {
        _database.delete("account", null, null);
        ContentValues values = new ContentValues();
        values.put("id", response.id);
        values.put("services", response.services);
        values.put("token", response.token);
        values.put("phone", request.phone);
        values.put("password", request.password);
        values.put("imei", request.imei);
        _database.insert("account", null, values);
    }

    public void saveUnreadMessages(ClockingMessage[] messages) {
        for (ClockingMessage message : messages) {
            try (Cursor cursor = _database.rawQuery("select id from message where id = " + message.id, new String[0])) {
                int count = cursor.getCount();
                if (count != 0)
                    continue;
            }
            ContentValues values = new ContentValues();
            values.put("id", message.id);
            values.put("datetime", message.datetime);
            values.put("title", message.title);
            values.put("content", message.content);
            values.put("read", message.read ? 1 : 0);
            _database.insert("message", null, values);
        }
    }

    public ClockingMessage[] getMessages() {
        List<ClockingMessage> messages = new ArrayList<>();
        try (Cursor cursor = _database.query("message", new String[]{"id", "datetime", "title", "content", "read"}, "", new String[0], "", "", "")) {
            while (cursor.moveToNext()){
                ClockingMessage info = new ClockingMessage();
                info.id = cursor.getInt(cursor.getColumnIndex("id"));
                info.datetime = cursor.getString(cursor.getColumnIndex("datetime"));
                info.title = cursor.getString(cursor.getColumnIndex("title"));
                info.content = cursor.getString(cursor.getColumnIndex("content"));
                info.read = cursor.getShort(cursor.getColumnIndex("read")) == 1;
                messages.add(info);
            }
        }
        ClockingMessage[] temp = new ClockingMessage[messages.size()];
        for(int i = 0; i < messages.size(); ++i)
            temp[i] = messages.get(i);
        return temp;
    }

    public void deleteMessages(int[] ids) {
        String where = "";
        for(int i = 0; i < ids.length; ++i){
            where += "id = " + ids[i];
            if (i != ids.length - 1)
                where += " or ";
        }
        _database.delete("message", where, null);
    }
}

