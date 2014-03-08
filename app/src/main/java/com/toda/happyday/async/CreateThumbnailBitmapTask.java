package com.toda.happyday.async;

import android.content.ContentResolver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.AsyncTask;
import android.provider.MediaStore;

import com.google.android.gms.internal.bi;
import com.toda.happyday.models.Picture;

/**
 * Created by fpgeek on 2014. 2. 16..
 */
public class CreateThumbnailBitmapTask extends AsyncTask<Void, Void, Bitmap> {

    private ContentResolver mContentResolver;
    private Picture mPicture;
    private AsyncPostExecute<Bitmap> mAsyncPostExecute = null;

    public CreateThumbnailBitmapTask(ContentResolver contentResolver, Picture picture, AsyncPostExecute<Bitmap> asyncPostExecute) {
        mContentResolver = contentResolver;
        mPicture = picture;
        mAsyncPostExecute = asyncPostExecute;
    }

    @Override
    protected Bitmap doInBackground(Void... voids) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 4;
        Bitmap thumbnailBitmap = MediaStore.Images.Thumbnails.getThumbnail(mContentResolver, mPicture.getId(), MediaStore.Images.Thumbnails.MINI_KIND, options);

        if (mPicture.getDegrees() == 90) {
            Matrix mx = new Matrix();
            mx.postRotate(mPicture.getDegrees());
            return Bitmap.createBitmap(thumbnailBitmap, 0, 0, thumbnailBitmap.getWidth(), thumbnailBitmap.getHeight(), mx ,true);
        }
        return thumbnailBitmap;
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        mPicture.setThumbnailBitmap(bitmap);
        if (mAsyncPostExecute != null) {
            mAsyncPostExecute.onPostExecute(bitmap);
        }
    }
}
