package com.yeahdev.yeahurls.fragments;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.support.v4.app.Fragment;

import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;

import com.yeahdev.yeahurls.R;
import com.yeahdev.yeahurls.adapter.OverviewRvAdapter;
import com.yeahdev.yeahurls.interfaces.ICommunicationAdapter;
import com.yeahdev.yeahurls.model.UrlItem;
import com.yeahdev.yeahurls.model.UserCreds;
import com.yeahdev.yeahurls.util.UserHelper;
import com.yeahdev.yeahurls.util.Utilities;

import java.util.ArrayList;
import java.util.HashMap;


public class OverviewFragment extends Fragment implements ICommunicationAdapter {
    private ProgressDialog progressDialog;
    private RecyclerView rvOverview;

    private OverviewRvAdapter overviewRvAdapter;
    private ArrayList<UrlItem> itemArrayList;

    public OverviewFragment() { itemArrayList = new ArrayList<>(); }

    public static OverviewFragment newInstance() {
        return new OverviewFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_overview, container, false);
        setHasOptionsMenu(true);

        progressDialog = ProgressDialog.show(getActivity(), "Loading", "Get Data from Firebase...", false);
        itemArrayList.clear();

        String userId = this.getArguments().getString("userId", "");
        long expireDate = this.getArguments().getLong("expireDate", 0);
        UserCreds userCreds = UserHelper.createUserCredsObject(userId, expireDate);

        rvOverview = (RecyclerView) v.findViewById(R.id.rvOverview);
        rvOverview.setLayoutManager(new LinearLayoutManager(getActivity()));
        rvOverview.setItemAnimator(new DefaultItemAnimator());

        overviewRvAdapter = new OverviewRvAdapter(getActivity(), userCreds, this);
        rvOverview.setAdapter(overviewRvAdapter);

        if (expireDate > 0) {
            loadDataFromFirebase(userCreds.getUserId());
        }

        return v;
    }

    private void loadDataFromFirebase(String userId) {
        try {
            Firebase ref = new Firebase("https://yeah-url-extension.firebaseio.com/" + userId + "/urlcollector");
            ref.addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot snapshot, String s) {
                    String objId = snapshot.getKey();

                    for (DataSnapshot child : snapshot.getChildren()) {
                        try {
                            UrlItem urlItem = createUrlItem(objId, child);
                            itemArrayList.add(urlItem);
                            overviewRvAdapter.addItem(itemArrayList.size() - 1, urlItem);
                        } catch (Exception e) {
                            Utilities.buildSnackbar(getActivity(), "onChildAdded failed! Error: " + e.getMessage());
                        }
                    }

                    progressDialog.dismiss();
                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                }

                @Override
                public void onChildRemoved(DataSnapshot snapshot) {
                    /**
                     * Too slow!!!
                     */
                        /*String objId = snapshot.getKey();

                        for (DataSnapshot child : snapshot.getChildren()) {
                            try {
                                UrlItem urlItem = createUrlItem(objId, child);
                                Utilities.buildSnackbar(getActivity(), "ID: " + urlItem.getId() + ", Keywords: " + urlItem.getKeywords());
                            } catch (Exception e) {
                                Utilities.buildSnackbar(getActivity(), "onChildRemoved failed! Error: " + e.getMessage());
                            }
                        }*/
                }

                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                }

                @Override
                public void onCancelled(FirebaseError firebaseError) {
                    Utilities.buildSnackbar(getActivity(), "Error Code:" + firebaseError.getCode() + ", Msg: " + firebaseError.getMessage());
                }
            });
        } catch (Exception e) {
            Utilities.buildSnackbar(getActivity(), "addChildEventListener failed! Error: " + e.getMessage());
        }
    }

    private UrlItem createUrlItem(String objId, DataSnapshot child) {
        UrlItem urlItem = new UrlItem();

        HashMap<String, UrlItem> map = (HashMap<String, UrlItem>) child.getValue();
        urlItem.setDate(String.valueOf(map.get("date")));
        urlItem.setTimestamp(String.valueOf(map.get("timestamp")));
        urlItem.setValue(String.valueOf(map.get("value")));
        urlItem.setTime(String.valueOf(map.get("time")));
        urlItem.setId(Integer.parseInt(String.valueOf(map.get("id"))));
        urlItem.setKeywords(String.valueOf(map.get("keywords")));
        urlItem.setObjId(objId);

        return urlItem;
    }

    public void removeAllItemsFromRv() {
        try {
            overviewRvAdapter.removeAllItems();
        } catch (Exception e) {
            Utilities.buildSnackbar(getActivity(), "removeAllItemsFromRv failed! Error: " + e.getMessage());
        }
    }

    private ArrayList<UrlItem> filter(ArrayList<UrlItem> urlCollection, String query) {
        query = query.toLowerCase();

        if (TextUtils.isEmpty(query)) {
            return urlCollection;
        } else {
            final ArrayList<UrlItem> filteredModelList = new ArrayList<>();
            for (UrlItem item : urlCollection) {
                final String text = item.getValue().toLowerCase().trim();
                if (text.contains(query)) {
                    filteredModelList.add(item);
                }
                final String textKey = item.getKeywords().toLowerCase().trim();
                if (textKey.contains(query)) {
                    filteredModelList.add(item);
                }
            }
            return filteredModelList;
        }
    }

    @Override
    public void getRemovedItemPosition(int position) {
        itemArrayList.remove(position);
        overviewRvAdapter.notifyDataSetChanged();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        final MenuItem item = menu.findItem(R.id.action_search);
        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                final ArrayList<UrlItem> filteredModelList = filter(itemArrayList, query);
                overviewRvAdapter.animateTo(filteredModelList);
                rvOverview.scrollToPosition(0);
                return true;
            }
        });
    }

}
