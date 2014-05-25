package com.toda.onefineday.views;

import android.app.Activity;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

import com.toda.onefineday.R;
import com.toda.onefineday.models.PictureGroup;

public class MiniPicturesActivity extends Activity {

    private PictureGroup mPictureGroup;
    private ImageListLoader mImageListLoader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mini_items);

        Parcelable parcelable = getIntent().getParcelableExtra(getString(R.string.extra_daily_data_array));
        mPictureGroup = (PictureGroup)parcelable;

        GridView gridView = (GridView) findViewById(R.id.picture_gridview);
        mImageListLoader = new ImageListLoader(this);
        MiniPictureAdapter listAdapter = new MiniPictureAdapter(this, mPictureGroup, mImageListLoader);
        gridView.setAdapter(listAdapter);

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(MiniPicturesActivity.this, "" + position, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.share_pictures, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
