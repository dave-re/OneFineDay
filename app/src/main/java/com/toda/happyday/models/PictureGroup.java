package com.toda.happyday.models;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Parcel;
import android.os.Parcelable;

import com.toda.happyday.async.AsyncPostExecute;
import com.toda.happyday.models.db.DailyInfo;
import com.toda.happyday.models.db.DailyInfoDbHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by fpgeek on 2014. 1. 19..
 */
public class PictureGroup extends ArrayList<Picture> implements Parcelable {

    private long id;
    private String dairyText = "";
    private int sticker;
    private boolean isFavorite;
    private String locationText = null;

    private final static String[] DB_PROJECTION = {
            DailyInfo.DailyEntry._ID,
            DailyInfo.DailyEntry.COLUMN_NAME_DIARY_TEXT,
            DailyInfo.DailyEntry.COLUMN_NAME_STICKER,
            DailyInfo.DailyEntry.COLUMN_NAME_FAVORITE
    };

    private Random random = new Random();
    private int mSelectedIndex = 0;

    public PictureGroup() {
    }

    public PictureGroup(Parcel parcel) {
        readFromParcel(parcel);
    }

    public PictureGroup(int size) {
        super(size);
    }

    public void selectMainPicture() {
        mSelectedIndex = selectRandomIndex(this.size());
    }

//    public int getHeight() {
//        int height = 0;
//        for (Picture picture : this) {
//            height += picture.getHeight();
//        }
//        return height;
//    }

    public Picture getMainPicture() {
        return this.get(mSelectedIndex);
    }

    private int selectRandomIndex(int size) {
        if (size == 1) {
            return 0;
        }

        return random.nextInt(size - 1);
    }

    public void changeProperties(PictureGroup pictureGroup) {
        setDairyText(pictureGroup.getDairyText());
        setSticker(pictureGroup.getSticker());
        setFavorite(pictureGroup.isFavorite());
        setLocationText(pictureGroup.getLocationText());
    }

    public static void all(DailyInfoDbHelper dbHelper, AsyncPostExecute<List<PictureGroup>> asyncPostExecute) {
        new GetAllPictureGroupsTask(dbHelper.getReadableDatabase(), asyncPostExecute).execute();
    }

    public static void remove(DailyInfoDbHelper dbHelper, long id) {
        new RemovePictureGroupTask(dbHelper).execute(id);
    }

    private static class RemovePictureGroupTask extends AsyncTask<Long, Void, Boolean> {

        private DailyInfoDbHelper mDbHelper;

        public RemovePictureGroupTask(DailyInfoDbHelper dbHelper) {
            mDbHelper = dbHelper;
        }

        @Override
        protected Boolean doInBackground(Long... longs) {
            final long id = longs[0];

            SQLiteDatabase db = mDbHelper.getWritableDatabase();
            if (db == null) { return null; }

            String selection = DailyInfo.DailyEntry._ID + " = ?";
            String[] selectionArgs = { String.valueOf(id) };

            int count = db.delete(
                    DailyInfo.DailyEntry.TABLE_NAME,
                    selection,
                    selectionArgs
            );
//            db.close();

            return count == 1;
        }
    }

    private static class GetAllPictureGroupsTask extends AsyncTask<Void, Void, List<PictureGroup>> {

        private SQLiteDatabase mDb;
        private AsyncPostExecute<List<PictureGroup>> mAsyncPostExecute;

        public GetAllPictureGroupsTask(SQLiteDatabase db, AsyncPostExecute<List<PictureGroup>> asyncPostExecute) {
            mDb = db;
            mAsyncPostExecute = asyncPostExecute;
        }

        @Override
        protected List<PictureGroup> doInBackground(Void... voids) {
            if (mDb == null) { return null; }

            Cursor cursor = getCursor(mDb, null, null, DailyInfo.DailyEntry._ID + " ASC");
            List<PictureGroup> pictureGroupList = new ArrayList<PictureGroup>(cursor.getCount());
            while(cursor.moveToNext()) {
                PictureGroup pictureGroup = createPictureGroup(cursor);
                pictureGroupList.add(pictureGroup);
            }
            cursor.close();
            return pictureGroupList;
        }

        @Override
        protected void onPostExecute(List<PictureGroup> pictureGroupList) {
            mAsyncPostExecute.onPostExecute(pictureGroupList);
        }
    }

    public static PictureGroup get(SQLiteDatabase db, final long id) {
        if (db == null) { return null; }

        String selection = DailyInfo.DailyEntry._ID + " = ?";
        String[] selectionArgs = { String.valueOf(id) };

        Cursor cursor = getCursor(db, selection, selectionArgs, DailyInfo.DailyEntry.COLUMN_NAME_FAVORITE + " DESC");
        cursor.moveToNext();
        PictureGroup pictureGroup = createPictureGroup(cursor);
        cursor.close();
        return pictureGroup;
    }

    public static PictureGroup create(SQLiteDatabase db) {
        if (db == null) { return null; }

        ContentValues values = new ContentValues();
        values.put(DailyInfo.DailyEntry.COLUMN_NAME_DIARY_TEXT, "");
        values.put(DailyInfo.DailyEntry.COLUMN_NAME_STICKER, 0);
        values.put(DailyInfo.DailyEntry.COLUMN_NAME_FAVORITE, 0);

        final long id = db.insert(
                DailyInfo.DailyEntry.TABLE_NAME,
                DailyInfo.DailyEntry.COLUMN_NAME_NULLABLE,
                values
        );

        if (id == -1) {
            return null;
        }

        PictureGroup newPictureGroup = new PictureGroup();
        newPictureGroup.setId(id);
        return newPictureGroup;
    }

//    public static PictureGroup get_or_create(DailyInfoDbHelper dbHelper, final long id) {
//        PictureGroup pictureGroup = get(dbHelper, id);
//        if (pictureGroup != null) { return pictureGroup; }
//
//        return create(dbHelper);
//    }

    public static boolean update(SQLiteDatabase db, final long id) {
//        SQLiteDatabase db = dbHelper.getWritableDatabase();
        if (db == null) { return false; }

        ContentValues values = new ContentValues();
        values.put(DailyInfo.DailyEntry.COLUMN_NAME_STICKER, id);

        String selection = DailyInfo.DailyEntry._ID + " = ?";
        String[] selectionArgs = { String.valueOf(id) };

        int count = db.update(
                DailyInfo.DailyEntry.TABLE_NAME,
                values,
                selection,
                selectionArgs
        );

        return count == 1;
    }

    private static Cursor getCursor(SQLiteDatabase db, final String selection, final String[] selectionArgs, final String sortOrder) {
        return db.query(
                DailyInfo.DailyEntry.TABLE_NAME,
                DB_PROJECTION,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );
    }

    private static PictureGroup createPictureGroup(Cursor cursor) {
        PictureGroup pictureGroup = new PictureGroup();

        final long id = cursor.getLong(cursor.getColumnIndex(DailyInfo.DailyEntry._ID));
        pictureGroup.setId(id);

        final String diaryText = cursor.getString(cursor.getColumnIndex(DailyInfo.DailyEntry.COLUMN_NAME_DIARY_TEXT));
        pictureGroup.setDairyText(diaryText);

        final int sticker = cursor.getInt(cursor.getColumnIndex(DailyInfo.DailyEntry.COLUMN_NAME_STICKER));
        pictureGroup.setSticker(sticker);

        final int favorite = cursor.getInt(cursor.getColumnIndex(DailyInfo.DailyEntry.COLUMN_NAME_FAVORITE));
        pictureGroup.setFavorite(favorite == 1);

        return pictureGroup;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getDairyText() {
        return dairyText;
    }

    public void setDairyText(String dairyText) {
        this.dairyText = dairyText;
    }

    public int getSticker() {
        return sticker;
    }

    public void setSticker(int sticker) {
        this.sticker = sticker;
    }

    public boolean hasSticker() {
        return this.sticker != 0;
    }

    public boolean isFavorite() {
        return isFavorite;
    }

    public void setFavorite(boolean isFavorite) {
        this.isFavorite = isFavorite;
    }

    public String getLocationText() {
        return locationText;
    }

    public void setLocationText(String locationText) {
        this.locationText = locationText;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<PictureGroup> CREATOR
            = new Creator<PictureGroup>() {
        public PictureGroup createFromParcel(Parcel in) {
            return new PictureGroup(in);
        }

        public PictureGroup[] newArray(int size) {
            return new PictureGroup[size];
        }
    };

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeLong(id);
        parcel.writeString(this.dairyText);
        parcel.writeInt(this.sticker);
        parcel.writeByte((byte) (this.isFavorite ? 1 : 0));
        parcel.writeString(this.locationText);
        parcel.writeInt(mSelectedIndex);
        parcel.writeList(this);
    }

    private void readFromParcel(Parcel parcel) {
        this.id = parcel.readLong();
        this.dairyText = parcel.readString();
        this.sticker = parcel.readInt();
        this.isFavorite = (parcel.readByte() != 0);
        this.locationText = parcel.readString();
        this.mSelectedIndex = parcel.readInt();
        parcel.readList(this, getClass().getClassLoader());
    }
}
