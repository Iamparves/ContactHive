package com.example.contacthive;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import java.util.ArrayList;

public class ContactDatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "contact.db";
    private static final int DATABASE_VERSION = 2;
    private static final String TABLE_NAME = "contact_table";
    private static final String COLUMN_ID = "_id";
    private static final String COLUMN_NAME = "contact_name";
    private static final String COLUMN_PHONE = "contact_phone";
    private static final String COLUMN_EMAIL = "contact_email";
    private static final String COLUMN_ADDRESS = "contact_address";
    private static final String COLUMN_AVATAR = "contact_avatar";

    public ContactDatabaseHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String createTableQuery = "CREATE TABLE " + TABLE_NAME + " (" + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + COLUMN_NAME + " TEXT, " + COLUMN_PHONE + " TEXT, " + COLUMN_EMAIL + " TEXT, " + COLUMN_ADDRESS + " TEXT, " + COLUMN_AVATAR + " TEXT)";
        sqLiteDatabase.execSQL(createTableQuery);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(sqLiteDatabase);
    }

    public boolean addContact(ContactModel contactModel) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put(COLUMN_NAME, contactModel.getName());
        cv.put(COLUMN_PHONE, contactModel.getPhone());
        cv.put(COLUMN_EMAIL, contactModel.getEmail());
        cv.put(COLUMN_ADDRESS, contactModel.getAddress());
        cv.put(COLUMN_AVATAR, contactModel.getAvatar());

        long insert = db.insert(TABLE_NAME, null, cv);
        db.close();

        if(insert == -1) {
            return false;
        } else {
            return true;
        }
    }

    public boolean updateContact(ContactModel contactModel, String row_id) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put(COLUMN_NAME, contactModel.getName());
        cv.put(COLUMN_PHONE, contactModel.getPhone());
        cv.put(COLUMN_EMAIL, contactModel.getEmail());
        cv.put(COLUMN_ADDRESS, contactModel.getAddress());
        cv.put(COLUMN_AVATAR, contactModel.getAvatar());

        int rowsUpdated = db.update(TABLE_NAME, cv, (COLUMN_ID+"=?"), new String[]{row_id});
        db.close();

        if(rowsUpdated > 0) {
            return true;
        } else {
            return false;
        }

    }

    public boolean deleteOneContact(String row_id) {
        SQLiteDatabase db = this.getWritableDatabase();

        int deletedRow = db.delete(TABLE_NAME, (COLUMN_ID+"=?"), new String[]{row_id});
        db.close();

        if(deletedRow == 0) {
            return false;
        } else {
            return true;
        }
    }

    public ArrayList<ContactModel> getAllContact() {
        ArrayList<ContactModel> returnList = new ArrayList<>();

        String query = "SELECT * FROM " + TABLE_NAME;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null);

        if(cursor.moveToFirst()) {
            do {
                int _id = cursor.getInt(0);
                String contact_name = cursor.getString(1);
                String contact_phone = cursor.getString(2);
                String contact_email = cursor.getString(3);
                String contact_address = cursor.getString(4);
                String contact_avatar = cursor.getString(5);

                ContactModel contactItem = new ContactModel(_id, contact_name, contact_phone, contact_email, contact_address, contact_avatar);
                returnList.add(contactItem);
            } while (cursor.moveToNext());
        } else {
            // failure: doesn't add anything to the returnList
        }

        cursor.close();
        db.close();
        return returnList;
    }
}
