package com.yeahdev.yeahurls.fragments;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
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
import android.widget.Toast;

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


public class OverviewFragment extends Fragment implements ICommunicationAdapter {
    private ProgressDialog progressDialog;
    private RecyclerView rvOverview;

    private OverviewRvAdapter overviewRvAdapter;
    private FloatingActionButton fabScrollOverviewUpDown;
    private ArrayList<UrlItem> itemArrayList;

    public OverviewFragment() { itemArrayList = new ArrayList<>(); }

    public static OverviewFragment newInstance() { return new OverviewFragment(); }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_overview, container, false);
        setHasOptionsMenu(true);

        progressDialog = ProgressDialog.show(getActivity(), "Loading", "Get Url Collection from Firebase...", false, true);
        itemArrayList.clear();

        fabScrollOverviewUpDown = (FloatingActionButton) v.findViewById(R.id.fabScrollUrlUpDown);

        String userId = this.getArguments().getString("userId", "");
        long expireDate = this.getArguments().getLong("expireDate", 0);

        if (!"".equals(userId) || expireDate != 0) {
            UserCreds userCreds = UserHelper.createUserCredsObject(userId, expireDate);

            rvOverview = (RecyclerView) v.findViewById(R.id.rvOverview);
            rvOverview.setLayoutManager(new LinearLayoutManager(getActivity()));
            rvOverview.setItemAnimator(new DefaultItemAnimator());

            overviewRvAdapter = new OverviewRvAdapter(getActivity(), userCreds, this);
            rvOverview.setAdapter(overviewRvAdapter);

            if (UserHelper.userStillLoggedIn(expireDate)) {
                loadUrlDataFromFirebase(userCreds.getUserId());
            }

            rvOverview.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(RecyclerView recyclerView, int newState) {

                    if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                        fabScrollOverviewUpDown.hide();
                    } else {
                        fabScrollOverviewUpDown.show();
                    }

                    super.onScrollStateChanged(recyclerView, newState);
                }
            });

        } else {
            Utilities.buildSnackbar(getActivity(), "No valid User available!");
            progressDialog.dismiss();
        }

        fabScrollOverviewUpDown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (overviewRvAdapter.isScrolledToBottom()) {
                    rvOverview.scrollToPosition(0);
                    overviewRvAdapter.setScrolledToBottom(false);
                } else {
                    rvOverview.scrollToPosition(overviewRvAdapter.getItemCount() - 1);
                    overviewRvAdapter.setScrolledToBottom(true);
                }
            }
        });

        return v;
    }

    /**
     * Method to Load Data from Firebase
     * @param userId User Id
     */
    private void loadUrlDataFromFirebase(String userId) {
        try {
            Firebase ref = new Firebase("https://yeah-url-extension.firebaseio.com/" + userId + "/urlcollector");
            ref.addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot snapshot, String s) {
                    String objId = snapshot.getKey();
                    for (DataSnapshot child : snapshot.getChildren()) {
                        try {
                            UrlItem urlItem = createUrlItem(objId, child);
                            if (urlItem != null) {
                                itemArrayList.add(urlItem);
                                overviewRvAdapter.addItem(itemArrayList.size() - 1, urlItem);
                            }
                        } catch (Exception e) {
                            Utilities.buildToast(getActivity(), "onChildAdded failed! Error: " + e.getMessage(), Toast.LENGTH_LONG);
                        }
                    }
                    progressDialog.dismiss();
                }

                @Override
                public void onChildChanged(DataSnapshot snapshot, String s) {
                    String objId = snapshot.getKey();
                    for (DataSnapshot child : snapshot.getChildren()) {
                        try {
                            UrlItem urlItem = createUrlItem(objId, child);
                            if (urlItem != null) {
                                overviewRvAdapter.searchAndReplaceItem(urlItem);
                            }
                        } catch (Exception e) {
                            Utilities.buildToast(getActivity(), "onChildChanged failed! Error: " + e.getMessage(), Toast.LENGTH_LONG);
                        }
                    }
                }

                @Override
                public void onChildRemoved(DataSnapshot snapshot) {
                }

                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                }

                @Override
                public void onCancelled(FirebaseError firebaseError) {
                    Utilities.buildToast(getActivity(), "Error Code:" + firebaseError.getCode() + ", Msg: " + firebaseError.getMessage(), Toast.LENGTH_LONG);
                }
            });
        } catch (Exception e) {
            Utilities.buildToast(getActivity(), "addChildEventListener failed! Error: " + e.getMessage(), Toast.LENGTH_LONG);
        }
    }

    /**
     * Helper Method to generated UrlItem from Firebase DataSnapshot child
     * @param objId Object Id
     * @param child DataSnapshot
     * @return UrlItem
     */
    private UrlItem createUrlItem(String objId, DataSnapshot child) {
        UrlItem urlItem = null;
        try {
            urlItem = child.getValue(UrlItem.class);
            if (urlItem.getObjId() == null) {
                urlItem.setObjId(objId);
            }
        } catch(Exception e) {
            Utilities.buildToast(getActivity(), "createUrlItem failed! " + e.getMessage(), Toast.LENGTH_LONG);
        }
        return urlItem;
    }

    /**
     * Method to Remove all Adapter Elements
     */
    public void removeAllItemsFromRv() {
        try {
            overviewRvAdapter.removeAllItems();
        } catch (Exception e) {
            Utilities.buildToast(getActivity(), "removeAllItemsFromRv failed! Error: " + e.getMessage(), Toast.LENGTH_LONG);
        }
    }

    /**
     * Method to Filter Adapter Elements that matching the Query
     * @param urlCollection old Elements List
     * @param query Search Query
     * @return new Elements List
     */
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
                    continue;
                }
                final String textKey = item.getKeywords().toLowerCase().trim();
                if (textKey.contains(query)) {
                    filteredModelList.add(item);
                }
            }
            return filteredModelList;
        }
    }

    /**
     * Method to Add Element to Temp Element List
     * @param position Position in ArrayList
     * @param item Item Object
     */
    @Override
    public void getAddedItemPosition(int position, Object item) {
        UrlItem urlItem = (UrlItem) item;
        if (!itemArrayList.contains(urlItem)) {
            itemArrayList.add(position, urlItem);
        }
    }

    /**
     * Method to Remove Element from Temp Element List
     * @param position Position in ArrayList
     */
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
