
package com.cooeeui.brand.zenlauncher.unittest.category;

import java.util.List;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.SearchViewCompat;
import android.support.v4.widget.SearchViewCompat.OnCloseListenerCompat;
import android.support.v4.widget.SearchViewCompat.OnQueryTextListenerCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.cooeeui.brand.zenlauncher.unittest.R;

public class AppListFragment extends ListFragment
        implements LoaderManager.LoaderCallbacks<List<AppEntry>> {
    public static class AppListAdapter extends ArrayAdapter<AppEntry> {
        private final LayoutInflater mInflater;

        public AppListAdapter(Context context) {
            super(context, android.R.layout.simple_list_item_2);
            mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        public void setData(List<AppEntry> data) {
            clear();
            if (data != null) {
                for (AppEntry appEntry : data) {
                    add(appEntry);
                }
            }
        }

        /**
         * Populate new items in the list.
         */
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view;

            if (convertView == null) {
                view = mInflater.inflate(R.layout.list_item_icon_text, parent, false);
            } else {
                view = convertView;
            }

            AppEntry item = getItem(position);
            ((ImageView) view.findViewById(R.id.icon)).setImageDrawable(item.getIcon());
            ((TextView) view.findViewById(R.id.text)).setText(item.getLabel());

            return view;
        }
    }

    // This is the Adapter being used to display the list's data.
    AppListAdapter mAdapter;

    // If non-null, this is the current filter the user has provided.
    String mCurFilter;

    OnQueryTextListenerCompat mOnQueryTextListenerCompat;
    
    int mCategoryId;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Get category id from bundle.
        mCategoryId = getArguments().getInt("categoryId");

        // Give some text to display if there is no data. In a real
        // application this would come from a resource.
        setEmptyText("No applications");

        // We have a menu item to show in action bar.
        setHasOptionsMenu(true);

        // Create an empty adapter we will use to display the loaded data.
        mAdapter = new AppListAdapter(getActivity());
        setListAdapter(mAdapter);

        // Start out with a progress indicator.
        setListShown(false);

        // Prepare the loader. Either re-connect with an existing one,
        // or start a new one.
        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Place an action bar item for searching.
        MenuItem item = menu.add("Search");
        item.setIcon(android.R.drawable.ic_menu_search);
        MenuItemCompat.setShowAsAction(item, MenuItemCompat.SHOW_AS_ACTION_IF_ROOM
                | MenuItemCompat.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);
        final View searchView = SearchViewCompat.newSearchView(getActivity());
        if (searchView != null) {
            SearchViewCompat.setOnQueryTextListener(searchView,
                    new OnQueryTextListenerCompat() {
                        @Override
                        public boolean onQueryTextChange(String newText) {
                            // Called when the action bar search text has
                            // changed. Since this
                            // is a simple array adapter, we can just have
                            // it do the filtering.
                            mCurFilter = !TextUtils.isEmpty(newText) ? newText : null;
                            mAdapter.getFilter().filter(mCurFilter);
                            return true;
                        }
                    });
            SearchViewCompat.setOnCloseListener(searchView,
                    new OnCloseListenerCompat() {
                        @Override
                        public boolean onClose() {
                            if (!TextUtils.isEmpty(SearchViewCompat.getQuery(searchView))) {
                                SearchViewCompat.setQuery(searchView, null, true);
                            }
                            return true;
                        }

                    });
            MenuItemCompat.setActionView(item, searchView);
        }
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        // Insert desired behavior here.
        Log.i("LoaderCustom", "Item clicked: " + id);
    }

    @Override
    public Loader<List<AppEntry>> onCreateLoader(int id, Bundle args) {
        // This is called when a new Loader needs to be created. This
        // sample only has one Loader with no arguments, so it is simple.
        return new AppListLoader(getActivity(), mCategoryId);
    }

    @Override
    public void onLoadFinished(Loader<List<AppEntry>> loader, List<AppEntry> data) {
        // Set the new data in the adapter.
        mAdapter.setData(data);

        // The list should now be shown.
        if (isResumed()) {
            setListShown(true);
        } else {
            setListShownNoAnimation(true);
        }
    }

    @Override
    public void onLoaderReset(Loader<List<AppEntry>> loader) {
        // Clear the data in the adapter.
        mAdapter.setData(null);
    }
}
