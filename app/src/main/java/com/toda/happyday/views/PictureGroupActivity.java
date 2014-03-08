package com.toda.happyday.views;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.etsy.android.grid.StaggeredGridView;
import com.google.android.gms.internal.ac;
import com.toda.happyday.R;
import com.toda.happyday.async.AsyncPostExecute;
import com.toda.happyday.models.Picture;
import com.toda.happyday.models.PictureGroup;
import com.toda.happyday.presenters.PictureGroupPresenter;
import com.toda.happyday.presenters.PictureGroupsLoadListener;
import com.toda.happyday.utils.TextViewUtil;

import java.util.List;

public class PictureGroupActivity extends Activity {

    private final static int REQUEST_CODE_TO_ONE_DAY_ACTIVITY = 1;

    private List<PictureGroup> mPictureGroups;
    private StaggeredGridView mGridView;
    private PictureGroupAdapter mPictureGroupAdapter;

    private PictureGroup mShouldUpdatePictureGroup = null;
    private View mShouldUpdateView = null;
    private static ImageListLoader mImageListLoader;

    private long mLastLoadTime;
    private PictureGroupPresenter mPictureGroupPresenter = null;
    private View mLoadingBarView = null;

    private Context mThis;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.picture_group_list);
        mThis = this;

        initActionBar();

        List<PictureGroup> pictureGroups = getIntent().getParcelableArrayListExtra(getString(R.string.EXTRA_PICTURE_GROUP_LIST));
        mPictureGroups = pictureGroups;
        mLastLoadTime = getIntent().getLongExtra(getString(R.string.EXTRA_LAST_LOAD_TIME), 0);

        mImageListLoader = new ImageListLoader(this);

        mGridView = (StaggeredGridView)findViewById(R.id.grid_view);
        mGridView.setOnItemClickListener(itemClickListener);
        boolean isLoadComplete = getIntent().getBooleanExtra(getString(R.string.EXTRA_IS_LOAD_COMPLETE), false);
        if (!isLoadComplete) {
            mLoadingBarView = getLayoutInflater().inflate(R.layout.loading_progress, null);
            mGridView.addFooterView(mLoadingBarView);
        }

        mPictureGroupAdapter = new PictureGroupAdapter(this, pictureGroups, mImageListLoader, new AsyncPostExecute<Integer>() {

            @Override
            public void onPostExecute(Integer index) {
                long lastLoadTime = mLastLoadTime;
                if (mPictureGroupPresenter != null) {
                    lastLoadTime = mPictureGroupPresenter.getLastLoadedDateValue();
                }
                mPictureGroupPresenter = new PictureGroupPresenter(mThis);
                mPictureGroupPresenter.loadPictureGroups(lastLoadTime, mLoadCompleteCallback);
            }
        });

        mGridView.setAdapter(mPictureGroupAdapter);
    }

    private PictureGroupsLoadListener mLoadCompleteCallback = new PictureGroupsLoadListener() {

        @Override
        public void onLoad(List<PictureGroup> loadedPictureGroups, boolean isLoadComplete) {
            if (isLoadComplete && mLoadingBarView != null) {
                mLoadingBarView.setVisibility(View.GONE);
            } else {
                mPictureGroupAdapter.addAll(loadedPictureGroups);
                mPictureGroupAdapter.notifyDataSetChanged();
            }
        }
    };

    private void initActionBar() {
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            int titleId = getResources().getIdentifier("action_bar_title", "id", "android");
            if (titleId == 0) { return; }

            TextView titleTextView = (TextView) findViewById(titleId);
            if (titleTextView == null) { return; }

            Typeface typeface = Typeface.createFromAsset(getAssets(), "fonts/NanumPen.ttf");
            if (typeface == null) { return; }

            titleTextView.setTypeface(typeface);
        }
    }

    AdapterView.OnItemClickListener itemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

            PictureGroup pictureGroup = mPictureGroups.get(i);
            if (pictureGroup != null) {
                mShouldUpdatePictureGroup = pictureGroup;
                mShouldUpdateView = view;
                Intent intent = new Intent(view.getContext(), OneDayActivity.class);
                intent.putExtra(getString(R.string.extra_daily_data_array), (Parcelable)pictureGroup);
                startActivityForResult(intent, REQUEST_CODE_TO_ONE_DAY_ACTIVITY);
            }
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_TO_ONE_DAY_ACTIVITY && resultCode == OneDayActivity.RESULT_CODE_FROM_ONE_DAY_ACTIVITY) {
            PictureGroup updatedPictureGroup = data.getParcelableExtra(OneDayActivity.INTENT_EXTRA_NAME);
            updateItemView(updatedPictureGroup);
        }
    }

    private void updateItemView(PictureGroup pictureGroup) {
        if (mShouldUpdatePictureGroup != null) {
            mShouldUpdatePictureGroup.changeProperties(pictureGroup);
        }

        if (mShouldUpdateView != null) {
            final int sticker = pictureGroup.getSticker();
            ImageView stickerImageView = (ImageView)mShouldUpdateView.findViewById(R.id.sticker_thumb);
            stickerImageView.setImageResource(sticker);

            final String dairyText = pictureGroup.getDairyText();
            TextView diaryTextView = (TextView)mShouldUpdateView.findViewById(R.id.dairy_text);
            diaryTextView.setText(dairyText);
            TextViewUtil.setText(diaryTextView, dairyText);
        }
    }
}
