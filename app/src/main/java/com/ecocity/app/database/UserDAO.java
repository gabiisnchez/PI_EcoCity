package com.ecocity.app.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.ecocity.app.model.User;

public class UserDAO {
    private SQLiteDatabase database;
    private DbHelper dbHelper;

    public UserDAO(Context context) {
        dbHelper = new DbHelper(context);
    }

    public void open() {
        database = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    public long registerUser(User user) {
        ContentValues values = new ContentValues();
        values.put(DbHelper.COLUMN_NAME, user.getName());
        values.put(DbHelper.COLUMN_EMAIL, user.getEmail());
        values.put(DbHelper.COLUMN_PASSWORD, user.getPassword());

        return database.insert(DbHelper.TABLE_USERS, null, values);
    }

    public User login(String email, String password) {
        Cursor cursor = database.query(DbHelper.TABLE_USERS,
                new String[] { DbHelper.COLUMN_USER_ID, DbHelper.COLUMN_NAME, DbHelper.COLUMN_EMAIL,
                        DbHelper.COLUMN_PASSWORD },
                DbHelper.COLUMN_EMAIL + "=? AND " + DbHelper.COLUMN_PASSWORD + "=?",
                new String[] { email, password },
                null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            User user = new User(
                    cursor.getInt(0),
                    cursor.getString(1),
                    cursor.getString(2),
                    cursor.getString(3));
            cursor.close();
            return user;
        }
        return null;
    }

    public boolean checkEmailExists(String email) {
        Cursor cursor = database.query(DbHelper.TABLE_USERS,
                new String[] { DbHelper.COLUMN_USER_ID },
                DbHelper.COLUMN_EMAIL + "=?",
                new String[] { email },
                null, null, null);

        boolean exists = (cursor.getCount() > 0);
        cursor.close();
        return exists;
    }
}
