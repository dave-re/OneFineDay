package com.toda.onefineday.views;

import android.app.Activity;
import android.graphics.Bitmap;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.toda.onefineday.R;
import com.toda.onefineday.async.AsyncPostExecute;
import com.toda.onefineday.async.BitmapWorkerTask;
import com.toda.onefineday.async.MiniBitmapWorker;
import com.toda.onefineday.models.Picture;
import com.toda.onefineday.models.PictureGroup;

/**
 * Created by fpgeek on 2014. 5. 25..
 */
public class MiniPictureAdapter extends ArrayAdapter<Picture> {

    private static final String CACHE_NAME = "Mini";
    private Activity mActivity;
    private PictureGroup mPictureGroup;
    private static ImageListLoader mImageListLoader;

    public MiniPictureAdapter(Activity activity, PictureGroup pictureGroup, ImageListLoader imageListLoader) {
        super(activity, R.layout.picture_mini_item, pictureGroup);

        mActivity = activity;
        mPictureGroup = pictureGroup;
        mImageListLoader = imageListLoader;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;

        if (convertView == null) {
            convertView = mActivity.getLayoutInflater().inflate(R.layout.picture_mini_item, null);

            viewHolder = new ViewHolder();
            viewHolder.pictureImageView = (ImageView)convertView.findViewById(R.id.picture);
            viewHolder.videoIconImageView = (ImageView)convertView.findViewById(R.id.video_icon);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder)convertView.getTag();
        }

        Picture picture = mPictureGroup.get(position);

        if (picture.getType() == Picture.TYPE_IMAGE) {
            viewHolder.videoIconImageView.setVisibility(View.GONE);
        } else {
            viewHolder.videoIconImageView.setVisibility(View.VISIBLE);
        }

        BitmapWorkerTask bitmapWorkerTask = new MiniBitmapWorker(picture, viewHolder.pictureImageView, position, CACHE_NAME, mActivity.getContentResolver());
        mImageListLoader.loadBitmap(picture, null, viewHolder.pictureImageView, bitmapWorkerTask, CACHE_NAME);
        return convertView;
    }

    private class ThumbnailPostExecure implements AsyncPostExecute<Bitmap> {

        private ImageView mImageView;

        public ThumbnailPostExecure(ImageView imageView) {
            mImageView = imageView;
        }

        @Override
        public void onPostExecute(Bitmap t) {
            if (t != null) {
                mImageView.setImageBitmap(t);
            }
        }
    }

    private static class ViewHolder {
        public ImageView pictureImageView;
        public ImageView videoIconImageView;
    }
}
