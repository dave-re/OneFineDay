package com.toda.happyday.models.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by fpgeek on 2014. 1. 19..
 */
public class DailyInfoDbHelper extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "DailyInfo.db";

    private static final String TEXT_TYPE = " TEXT";
    private static final String INTEGER_TYPE = " INTEGER";
    private static final String BOOLEAN_TYPE = " BOOLEAN";
    private static final String COMMA_SEP = ",";
    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + DailyInfo.DailyEntry.TABLE_NAME + " (" +
                    DailyInfo.DailyEntry._ID + INTEGER_TYPE + " PRIMARY KEY," +
                    DailyInfo.DailyEntry.COLUMN_NAME_DIARY_TEXT + TEXT_TYPE + COMMA_SEP +
                    DailyInfo.DailyEntry.COLUMN_NAME_STICKER + INTEGER_TYPE + COMMA_SEP +
                    DailyInfo.DailyEntry.COLUMN_NAME_FAVORITE + BOOLEAN_TYPE +
                    " )";

    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + DailyInfo.DailyEntry.TABLE_NAME;

    public DailyInfoDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(SQL_CREATE_ENTRIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i2) {
        sqLiteDatabase.execSQL(SQL_DELETE_ENTRIES);
        onCreate(sqLiteDatabase);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }
}
