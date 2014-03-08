package com.toda.happyday.models.db;
import android.provider.BaseColumns;

/**
 * Created by fpgeek on 2014. 1. 19..
 */
public final class DailyInfo {

    public static abstract class DailyEntry implements BaseColumns {
        public static final String TABLE_NAME = "dailyInfo";
        public static final String COLUMN_NAME_DIARY_TEXT = "diarytext";
        public static final String COLUMN_NAME_STICKER = "sticker";
        public static final String COLUMN_NAME_FAVORITE = "favorite";
        public static final String COLUMN_NAME_NULLABLE = "nullable";
    }
}


