package com.toda.onefineday.models;

import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.MediaStore;
import com.toda.onefineday.R;
import com.toda.onefineday.async.AsyncPostExecute;
import com.toda.onefineday.utils.BitmapUtils;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

//import wseemann.media.FFmpegMediaMetadataRetriever;

/**
 * Created by fpgeek on 2013. 12. 8..
 */
public class Picture implements Parcelable {

    private long id;
    private Date date;
    private double longitude;
    private double latitude;
    private String location = null;
    private String imagePath;
    private Bitmap thumbnailBitmap = null; // 다른 Activity에 전달할 때는 의도적으로 제외했음.
    private int width;
    private int height;
    private int mDegrees = 0;
    private int type = TYPE_IMAGE;

    public static final int TYPE_IMAGE = 0;
    public static final int TYPE_VIDEO = 1;

    private final static Uri DB_IMAGE_URI = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
    private final static Uri DB_VIDEO_URI = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;

    private final static String[] DB_IMAGE_PROJECTION = {
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DATA,
            MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
            MediaStore.Images.Media.DATE_TAKEN,
            MediaStore.Images.Media.LATITUDE,
            MediaStore.Images.Media.LONGITUDE,
            MediaStore.Images.Media.ORIENTATION,
            MediaStore.Images.Media.IS_PRIVATE
    };
    private final static String[] DB_VIDEO_PROJECTION = {
            MediaStore.Video.Media._ID,
            MediaStore.Video.Media.DATA,
            MediaStore.Video.Media.BUCKET_DISPLAY_NAME,
            MediaStore.Video.Media.DATE_TAKEN,
            MediaStore.Video.Media.WIDTH,
            MediaStore.Video.Media.HEIGHT,
            MediaStore.Video.Media.LATITUDE,
            MediaStore.Video.Media.LONGITUDE,
            MediaStore.Video.Media.IS_PRIVATE
    };

    private final static String DB_IMAGE_DATE_ORDER = MediaStore.Images.Media.DATE_TAKEN + " DESC";
    private final static String DB_VIDEO_DATE_ORDER = MediaStore.Video.VideoColumns.DATE_TAKEN + " DESC";

    private final static String MONTH_ENG_LIST[] = {
        "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
    };

    public Picture() {
    }

    public Picture(Parcel parcel) {
        readFromParcel(parcel);
    }

    public static void all(Context context, int loadCount, long lastLoadDateValue, AsyncPostExecute<List<Picture>> asyncPostExecute) {
        new GetAllPicturesTask(context, lastLoadDateValue, asyncPostExecute).execute(loadCount);
    }

    public static void updateLocation(Context context, long pictureId, String location) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(context.getString(R.string.preference_picture_to_location_key), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(String.valueOf(pictureId), location);
        editor.commit();
    }

    private static String getLocationFromDb(Context context, long pictureId) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(context.getString(R.string.preference_picture_to_location_key), Context.MODE_PRIVATE);
        return sharedPreferences.getString(String.valueOf(pictureId), null);
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public String getMonthText() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return MONTH_ENG_LIST[calendar.get(Calendar.MONTH)];
    }

    public String getLocation() {
        return location;
    }

    public boolean hasValidLocationInfo() {
        return longitude != 0 && latitude != 0;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    private static class GetAllPicturesTask extends AsyncTask<Integer, Void, List<Picture>> {

        private Context mContext;
        private long mLastLoadDateValue;
        private AsyncPostExecute<List<Picture>> mAsyncPostExecute;

        public GetAllPicturesTask(Context context, long lastLoadDateValue, AsyncPostExecute<List<Picture>> asyncPostExecute) {
            mContext = context;
            mLastLoadDateValue = lastLoadDateValue;
            mAsyncPostExecute = asyncPostExecute;
        }

        @Override
        protected List<Picture> doInBackground(Integer... integers) {
            final int loadCount = integers[0];

            List<Picture> pictureList = new ArrayList<Picture>();

            Cursor imageCursor = getAllImageCursor(mContext.getContentResolver(), loadCount, mLastLoadDateValue);
            while(imageCursor.moveToNext()) {
                Picture picture = createImagePicture(mContext, imageCursor);
                pictureList.add(picture);
            }
            imageCursor.close();

            Cursor videoCursor = getAllVideoCursor(mContext.getContentResolver(), loadCount, mLastLoadDateValue);
            while(videoCursor.moveToNext()) {
                Picture picture = createVideoPicture(mContext, videoCursor);
                pictureList.add(picture);
            }
            imageCursor.close();

            return pictureList;
        }

        @Override
        protected void onPostExecute(List<Picture> pictureList) {
            mAsyncPostExecute.onPostExecute(pictureList);
        }
    }

    private static Cursor getAllImageCursor(ContentResolver contentResolver, int loadCount, long lastLoadDateValue) {
        final String selection = MediaStore.Images.Media.DATE_TAKEN + " <= ?";
        final String[] selectionArgs = {String.valueOf(lastLoadDateValue)};
        return getImageCursor(contentResolver, selection, selectionArgs, DB_IMAGE_DATE_ORDER + " limit " + loadCount);
    }

    private static Cursor getAllVideoCursor(ContentResolver contentResolver, int loadCount, long lastLoadDateValue) {
        final String selection = MediaStore.Video.Media.DATE_TAKEN + " <= ?";
        final String[] selectionArgs = {String.valueOf(lastLoadDateValue)};
        return getVideoCursor(contentResolver, selection, selectionArgs, DB_VIDEO_DATE_ORDER + " limit " + loadCount);
    }

    private static Cursor getImageCursor(ContentResolver contentResolver, final String selection, final String[] selectionArgs, final String sortOrder) {
        return contentResolver.query(
                DB_IMAGE_URI,
                DB_IMAGE_PROJECTION,
                selection,
                selectionArgs,
                sortOrder
        );
    }

    private static Cursor getVideoCursor(ContentResolver contentResolver, final String selection, final String[] selectionArgs, final String sortOrder) {
        return contentResolver.query(
                DB_VIDEO_URI,
                DB_VIDEO_PROJECTION,
                selection,
                selectionArgs,
                sortOrder
        );
    }

    private static Picture createImagePicture(Context context, Cursor pictureCursor) {
        Picture picture = new Picture();
        picture.setType(TYPE_IMAGE);
        picture.setId(pictureCursor.getInt(pictureCursor.getColumnIndex(MediaStore.Images.Media._ID)));

        final long takenDateValue = pictureCursor.getLong(pictureCursor.getColumnIndex(MediaStore.Images.Media.DATE_TAKEN));
        picture.setDate(new Date(takenDateValue));

        String filePath = pictureCursor.getString(pictureCursor.getColumnIndex(MediaStore.Images.Media.DATA));
        picture.setFilePath(filePath);

        final double longitudeValue = pictureCursor.getDouble(pictureCursor.getColumnIndex(MediaStore.Images.Media.LONGITUDE));
        picture.setLongitude(longitudeValue);

        final double latitudeValue = pictureCursor.getDouble(pictureCursor.getColumnIndex(MediaStore.Images.Media.LATITUDE));
        picture.setLatitude(latitudeValue);

        final String location = getLocationFromDb(context, picture.getId());
        if (location != null) {
            picture.setLocation(location);
        }

        final int orientation = pictureCursor.getInt(pictureCursor.getColumnIndex(MediaStore.Images.Media.ORIENTATION));
        picture.setDegrees(orientation);

        BitmapFactory.Options bitmapOptions = BitmapUtils.getBitmapOptions(picture.getFilePath());
        picture.setWidth(bitmapOptions.outWidth);
        picture.setHeight(bitmapOptions.outHeight);

        return picture;
    }

    private static Picture createVideoPicture(Context context, Cursor pictureCursor) {
        Picture picture = new Picture();
        picture.setType(TYPE_VIDEO);
        picture.setId(pictureCursor.getInt(pictureCursor.getColumnIndex(MediaStore.Video.Media._ID)));

        final long takenDateValue = pictureCursor.getLong(pictureCursor.getColumnIndex(MediaStore.Video.Media.DATE_TAKEN));
        picture.setDate(new Date(takenDateValue));

        String filePath = pictureCursor.getString(pictureCursor.getColumnIndex(MediaStore.Video.Media.DATA));
        picture.setFilePath(filePath);

        final double longitudeValue = pictureCursor.getDouble(pictureCursor.getColumnIndex(MediaStore.Video.Media.LONGITUDE));
        picture.setLongitude(longitudeValue);

        final double latitudeValue = pictureCursor.getDouble(pictureCursor.getColumnIndex(MediaStore.Video.Media.LATITUDE));
        picture.setLatitude(latitudeValue);

        final String location = getLocationFromDb(context, picture.getId());
        if (location != null) {
            picture.setLocation(location);
        }

        final int width = pictureCursor.getInt(pictureCursor.getColumnIndex(MediaStore.Video.Media.WIDTH));
        picture.setWidth(width);

        final int height = pictureCursor.getInt(pictureCursor.getColumnIndex(MediaStore.Video.Media.HEIGHT));
        picture.setHeight(height);

        {
            MediaMetadataRetriever metaRetriever = new MediaMetadataRetriever();
            try {
                metaRetriever.setDataSource(picture.getFilePath());
                String rotation = metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION);
                if (rotation != null) {
                    picture.setDegrees(Integer.parseInt(rotation));
                }
            } catch(RuntimeException exception) {
            } finally {
                metaRetriever.release();
            }
        }

        return picture;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public String getFullDateText() {
        return DateFormat.getDateInstance(DateFormat.FULL).format(date);
    }

    public String getDateText() {
        return DateFormat.getDateInstance(DateFormat.MEDIUM).format(date);
    }

    public String getDayText() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return String.format("%02d", calendar.get(Calendar.DAY_OF_MONTH));
    }

    public String getTimeText() {
        return DateFormat.getTimeInstance(DateFormat.SHORT).format(date);
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Date getDate() {
        return date;
    }

    public void setFilePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public String getFilePath() {
        return imagePath;
    }

    public Bitmap getThumbnailBitmap() {
        return thumbnailBitmap;
    }

    public void setThumbnailBitmap(Bitmap thumbnailBitmap) {
        this.thumbnailBitmap = thumbnailBitmap;
    }

    public int getDegrees() {
        return mDegrees;
    }

    public void setDegrees(int degrees) {
        this.mDegrees = degrees;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Picture> CREATOR
            = new Creator<Picture>() {
        public Picture createFromParcel(Parcel in) {
            return new Picture(in);
        }

        public Picture[] newArray(int size) {
            return new Picture[size];
        }
    };

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeLong(id);
        parcel.writeLong(date.getTime());
        parcel.writeDouble(longitude);
        parcel.writeDouble(latitude);
        parcel.writeString(location);
        parcel.writeString(imagePath);
        parcel.writeInt(width);
        parcel.writeInt(height);
        parcel.writeInt(mDegrees);
        parcel.writeInt(type);
//        parcel.writeParcelable(thumbnailBitmap, i);
    }

    private void readFromParcel(Parcel parcel) {
        this.id = parcel.readLong();
        this.date = new Date(parcel.readLong());
        this.longitude = parcel.readDouble();
        this.latitude = parcel.readDouble();
        this.location = parcel.readString();
        this.imagePath = parcel.readString();
        this.width = parcel.readInt();
        this.height = parcel.readInt();
        this.mDegrees = parcel.readInt();
        this.type = parcel.readInt();
//        this.thumbnailBitmap = parcel.readParcelable(getClass().getClassLoader());
    }


}
