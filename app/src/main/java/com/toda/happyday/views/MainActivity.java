package com.toda.happyday.views;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.toda.happyday.R;
import com.toda.happyday.async.AsyncPostExecute;
import com.toda.happyday.models.PictureGroup;
import com.toda.happyday.presenters.PictureGroupPresenter;
import com.toda.happyday.presenters.PictureGroupsLoadListener;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case R.id.action_settings:
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment implements PictureGroupsLoadListener {

        private PictureGroupPresenter mPictureGroupPresenter;

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            mPictureGroupPresenter = new PictureGroupPresenter(getActivity());
            mPictureGroupPresenter.loadPictureGroups(new Date().getTime(), this);

            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            return rootView;
        }

        @Override
        public void onLoad(List<PictureGroup> loadedPictureGroups, boolean isLoadComplete) {
            Intent intent = new Intent(getActivity(), PictureGroupActivity.class);
            intent.putParcelableArrayListExtra(getActivity().getString(R.string.EXTRA_PICTURE_GROUP_LIST), new ArrayList<PictureGroup>(loadedPictureGroups));
            intent.putExtra(getActivity().getString(R.string.EXTRA_LAST_LOAD_TIME), mPictureGroupPresenter.getLastLoadedDateValue());
            intent.putExtra(getActivity().getString(R.string.EXTRA_IS_LOAD_COMPLETE), isLoadComplete);
            getActivity().startActivity(intent);
        }
    }
}
