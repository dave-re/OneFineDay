package com.toda.happyday.views;

import android.app.ActionBar;
import android.app.ListFragment;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.internal.ac;
import com.toda.happyday.R;
import com.toda.happyday.async.OneDayBitmapWorkerTask;
import com.toda.happyday.models.Picture;
import com.toda.happyday.models.db.DailyInfo;
import com.toda.happyday.models.db.DailyInfoDbHelper;
import com.toda.happyday.models.PictureGroup;
import com.toda.happyday.utils.TextViewUtil;

import java.io.File;

public class OneDayActivity extends FragmentActivity {

    private final static int REQUEST_CODE_TO_WRITE_ACTIVITY = 10;
    public final static int RESULT_CODE_FROM_ONE_DAY_ACTIVITY = 11;
    public final static String INTENT_EXTRA_NAME = "updatePictureGroup";
    private static PlaceholderFragment placeholderFragment = null;
    private static Integer[] STICKER_IMAGE_IDS = {
            R.drawable.sticker_1, R.drawable.sticker_2,
            R.drawable.sticker_3, R.drawable.sticker_4,
            R.drawable.sticker_11,
            R.drawable.sticker_13, R.drawable.sticker_6,
            R.drawable.sticker_7, R.drawable.sticker_8,
            R.drawable.sticker_10
    };
    private static final int STICKER_COUNT_PER_SCREEN = 10;
    private DailyInfoDbHelper dbHelper = null;
    private static ImageListLoader mImageListLoader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_daily);

        dbHelper = new DailyInfoDbHelper(this);
        mImageListLoader = new ImageListLoader(this);

        if (savedInstanceState == null) {
            placeholderFragment = new PlaceholderFragment();
            getFragmentManager().beginTransaction()
                    .add(R.id.container, placeholderFragment)
                    .commit();
        }
    }

    private void initActionBar() {
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowCustomEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);

            RelativeLayout relativeLayout = (RelativeLayout)getLayoutInflater().inflate(R.layout.important_in_actionbar, null, false);
            actionBar.setCustomView(relativeLayout);
        }
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        intent.putExtra(INTENT_EXTRA_NAME, (Parcelable)placeholderFragment.getPictureGroup());
        setResult(RESULT_CODE_FROM_ONE_DAY_ACTIVITY, intent);
        super.onBackPressed();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.daily, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case R.id.action_sticker:
                toggelStickerView();
                return true;
            case R.id.action_edit:
                openEditView();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void toggelStickerView() {
        if (this.placeholderFragment.getStickerViewPager().getVisibility() == View.VISIBLE) {
            closeStickerView();
        } else {
            openStickerView();
        }
    }

    private void openStickerView() {
        this.placeholderFragment.getStickerViewPager().setVisibility(View.VISIBLE);
    }

    private void closeStickerView() {
        this.placeholderFragment.getStickerViewPager().setVisibility(View.GONE);
    }

    private void openEditView() {
        Intent intent = new Intent(this, WriteActivity.class);
        intent.putExtra(getString(R.string.EXTRA_DAILY_GROUP_ID), this.placeholderFragment.getPictureGroup().getId());
        intent.putExtra(getString(R.string.EXTRA_DAIRY_TEXT), this.placeholderFragment.getPictureGroup().getDairyText());
        startActivityForResult(intent, REQUEST_CODE_TO_WRITE_ACTIVITY);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_TO_WRITE_ACTIVITY && resultCode == WriteActivity.RESULT_CODE_FROM_WRITE_ACTIVITY) {
            String dairyText = data.getStringExtra(WriteActivity.INTENT_EXTRA_UPDATE_DAIRY_TEXT_NAME);
            placeholderFragment.getPictureGroup().setDairyText(dairyText);
            TextViewUtil.setText(placeholderFragment.getDiaryTextView(), dairyText);
        }
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public class PlaceholderFragment extends ListFragment {

        private PictureGroup mPictureGroup;
        private View mHeaderView;

        private ViewPager mStickerViewPager = null;
        private StickerCollectionPagerAdapter mStickerCollectionPagerAdapter = null;

        private TextView mDairyTextView = null;

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_daily, container, false);

            Parcelable parcelable = getActivity().getIntent().getParcelableExtra( getString(R.string.extra_daily_data_array) );
            mPictureGroup = (PictureGroup)parcelable;

            ActionBar actionBar = getActionBar();
            if (actionBar != null) {
                actionBar.setTitle(mPictureGroup.getMainPicture().getDateText());
                if (mPictureGroup.hasSticker()) {
                    actionBar.setIcon(mPictureGroup.getSticker());
                }
            }

            mHeaderView = inflater.inflate(R.layout.daily_header, null, false);

            mDairyTextView = (TextView) mHeaderView.findViewById(R.id.dairy_text);
            TextViewUtil.setText(mDairyTextView, mPictureGroup.getDairyText());

            mStickerViewPager = (ViewPager)rootView.findViewById(R.id.sticker_view_pager);
            mStickerCollectionPagerAdapter = new StickerCollectionPagerAdapter( ((FragmentActivity)getActivity()).getSupportFragmentManager() );
            mStickerViewPager.setAdapter(mStickerCollectionPagerAdapter);

            return rootView;
        }

        AdapterView.OnItemClickListener itemClickListener = new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                int index = (i - 1);
                if (0 <= index && index < mPictureGroup.size()) {
                    Picture picture = mPictureGroup.get(i-1);

                    if (picture != null) {
                        Intent intent = new Intent(Intent.ACTION_VIEW);

                        if (picture.getType() == Picture.TYPE_IMAGE) {
                            Uri uri = Uri.fromFile(new File(picture.getFilePath()));
                            intent.setDataAndType(uri, "image/*");
                        } else {
                            Uri uri = Uri.parse(picture.getFilePath());
                            intent.setDataAndType(uri, "video/*");
                        }
                        startActivity(intent);
                    }
                }


            }
        };

        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);

            if (mHeaderView != null) {
                getListView().addHeaderView(mHeaderView);
            }

            getListView().setOnItemClickListener(itemClickListener);

            OneDayAdapter listAdapter = new OneDayAdapter(getActivity(), mPictureGroup, mImageListLoader);
            setListAdapter(listAdapter);
        }

        @Override
        public void onStart() {
            super.onStart();
        }

        @Override
        public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
            super.onCreateOptionsMenu(menu, inflater);
            inflater.inflate(R.menu.daily, menu);
        }

        public PictureGroup getPictureGroup() {
            return mPictureGroup;
        }

        public ViewPager getStickerViewPager() {
            return mStickerViewPager;
        }

        public TextView getDiaryTextView() {
            return mDairyTextView;
        }
    }

    public class StickerCollectionPagerAdapter extends FragmentStatePagerAdapter {

        public StickerCollectionPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int i) {
            Fragment fragment = new StickerImageFragment();
            Bundle args = new Bundle();
            args.putInt(StickerImageFragment.ARG_OBJECT, i);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public int getCount() {
            return (STICKER_IMAGE_IDS.length / (STICKER_COUNT_PER_SCREEN + 1)) + 1;
        }

//        @Override
//        public CharSequence getPageTitle(int position) {
//            return super.getPageTitle(position);
//        }
    }

    public class StickerImageFragment extends Fragment implements AdapterView.OnItemClickListener {

        public static final String ARG_OBJECT = "StickerImageObject";
        private int imageIndex = 0;

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

            GridView gridView = (GridView)inflater.inflate(R.layout.fragment_sticker, container, false);
            Bundle args = getArguments();
            imageIndex = args.getInt(ARG_OBJECT);
            gridView.setAdapter(new ImageAdapter(this.getActivity(), imageIndex));
            gridView.setOnItemClickListener(this);
            return gridView;
        }

        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            final int sticker = STICKER_IMAGE_IDS[i + (imageIndex * STICKER_COUNT_PER_SCREEN)];
            if (sticker == R.drawable.sticker_10) {
                new SaveStickerTask(placeholderFragment.getPictureGroup().getId()).execute(0);
            } else {
                new SaveStickerTask(placeholderFragment.getPictureGroup().getId()).execute(sticker);
            }
        }
    }

    private class SaveStickerTask extends AsyncTask<Integer, Void, Boolean> {

        private long rowId;
        private int stickerImageId;

        public SaveStickerTask(long rowId) {
            this.rowId = rowId;
        }

        @Override
        protected Boolean doInBackground(Integer... integers) {
            stickerImageId = integers[0];
            SQLiteDatabase db = dbHelper.getWritableDatabase();

            ContentValues values = new ContentValues();
            values.put(DailyInfo.DailyEntry.COLUMN_NAME_STICKER, stickerImageId);

            String selection = DailyInfo.DailyEntry._ID + " = ?";
            String[] selectionArgs = { String.valueOf(rowId) };

            assert db != null;
            int count = db.update(
                    DailyInfo.DailyEntry.TABLE_NAME,
                    values,
                    selection,
                    selectionArgs
            );
            db.close();

            return count == 1;
        }

        @Override
        protected void onPostExecute(Boolean updateSuccess) {
            super.onPostExecute(updateSuccess);

            if (updateSuccess) {
                placeholderFragment.getPictureGroup().setSticker(stickerImageId);
                ActionBar actionBar = getActionBar();
                if (actionBar != null) {
                    if (stickerImageId == 0) {
                        actionBar.setDisplayShowHomeEnabled(false);
                    } else {
                        actionBar.setDisplayShowHomeEnabled(true);
                        actionBar.setIcon(stickerImageId);
                    }
                }
                closeStickerView();
            }
        }
    }

    public static class ImageAdapter extends BaseAdapter {
        private Context context;
        private int index;

        public ImageAdapter(Context c, int index) {
            context = c;
            this.index = index;
        }

        @Override
        public int getCount() {
            if (STICKER_COUNT_PER_SCREEN * (index + 1) <= STICKER_IMAGE_IDS.length) {
                return STICKER_COUNT_PER_SCREEN;
            } else {
                return (STICKER_IMAGE_IDS.length % STICKER_COUNT_PER_SCREEN);
            }
        }

        @Override
        public Object getItem(int i) {
            return null;
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup viewGroup) {

            final int imageIndex = position + (index * STICKER_COUNT_PER_SCREEN);
            if (STICKER_IMAGE_IDS.length <= imageIndex) {
                return null;
            }

            ImageView imageView;
            if (convertView == null) {
                imageView = new ImageView(context);
                imageView.setLayoutParams(new GridView.LayoutParams(85, 85));
                imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                imageView.setPadding(8, 8, 8, 8);
            } else {
                imageView = (ImageView) convertView;
            }

            imageView.setImageResource( STICKER_IMAGE_IDS[position + (index * STICKER_COUNT_PER_SCREEN)] );
            imageView.setVisibility(View.VISIBLE);
            return imageView;
        }
    }
}