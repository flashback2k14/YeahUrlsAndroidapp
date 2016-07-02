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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
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
import java.util.Collections;


public class OverviewFragment extends Fragment implements ICommunicationAdapter {
    private ProgressDialog progressDialog;
    private RecyclerView rvOverview;
    private LinearLayout llOverviewKeywords;
    private Spinner spKeywords;
    private FloatingActionButton fabClearSpinner;
    private FloatingActionButton fabScrollOverviewUpDown;

    private ArrayAdapter<String> aaKeywords;
    private OverviewRvAdapter overviewRvAdapter;

    private ArrayList<UrlItem> itemArrayList;
    private ArrayList<String> itemArrayListKeywords;

    public OverviewFragment() {
        itemArrayList = new ArrayList<>();
        itemArrayListKeywords = new ArrayList<>();
    }

    public static OverviewFragment newInstance() { return new OverviewFragment(); }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_overview, container, false);
        setHasOptionsMenu(true);

        progressDialog = ProgressDialog.show(getActivity(), "Loading", "Get Url Collection from Firebase...", false, true);
        itemArrayList.clear();
        itemArrayListKeywords.clear();

        llOverviewKeywords = (LinearLayout) v.findViewById(R.id.llOverviewKeywords);
        fabClearSpinner = (FloatingActionButton) v.findViewById(R.id.fabClearSpinner);
        fabScrollOverviewUpDown = (FloatingActionButton) v.findViewById(R.id.fabScrollUrlUpDown);

        String userId = this.getArguments().getString("userId", "");
        long expireDate = this.getArguments().getLong("expireDate", 0);

        if (!"".equals(userId) || expireDate != 0) {
            UserCreds userCreds = UserHelper.createUserCredsObject(userId, expireDate);

            setupSpinner(v);
            setupRecyclerView(v, userCreds);

            if (UserHelper.userStillLoggedIn(expireDate)) {
                itemArrayListKeywords.add("");
                loadUrlDataFromFirebase(userCreds.getUserId());
            }

            setupListener();

        } else {
            Utilities.buildSnackbar(getActivity(), "No valid User available!");
            progressDialog.dismiss();
        }

        return v;
    }

    /**
     * Setup Spinner
     * @param v View
     */
    private void setupSpinner(View v) {
        spKeywords = (Spinner) v.findViewById(R.id.spKeywords);
        spKeywords.setPrompt("Select Keyword");
        aaKeywords = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, itemArrayListKeywords);
        aaKeywords.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spKeywords.setAdapter(aaKeywords);
    }

    /**
     * Setup RecyclerView
     * @param v View
     * @param userCreds UserCreds
     */
    private void setupRecyclerView(View v, UserCreds userCreds) {
        rvOverview = (RecyclerView) v.findViewById(R.id.rvOverview);
        rvOverview.setLayoutManager(new LinearLayoutManager(getActivity()));
        rvOverview.setItemAnimator(new DefaultItemAnimator());

        overviewRvAdapter = new OverviewRvAdapter(getActivity(), userCreds, this);
        rvOverview.setAdapter(overviewRvAdapter);
    }

    /**
     * Setup Listener
     */
    private void setupListener() {
        spKeywords.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position > 0) {
                    performFilterAction(itemArrayListKeywords.get(position));
                } else {
                    performFilterAction("");
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });

        fabClearSpinner.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performFilterAction("");
                spKeywords.setSelection(0);
            }
        });

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
                                if (!itemArrayListKeywords.contains(urlItem.getKeywords())) {
                                    itemArrayListKeywords.add(urlItem.getKeywords());
                                    Collections.sort(itemArrayListKeywords);
                                    aaKeywords.notifyDataSetChanged();
                                }
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
        // add items from fragment menu
        inflater.inflate(R.menu.menu_fragments, menu);
        // hide settings
        menu.findItem(R.id.action_settings).setVisible(false);
        // setup search
        final MenuItem item = menu.findItem(R.id.action_search);
        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                performFilterAction(query);
                return true;
            }
        });
        // call parent method
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // handle visibility of keywords spinner
        if (item.getItemId() == R.id.action_show_keywords) {
            if (llOverviewKeywords.getVisibility() == View.GONE) {
                llOverviewKeywords.setVisibility(View.VISIBLE);
            } else {
                llOverviewKeywords.setVisibility(View.GONE);
            }
        }
        // call parent method
        return super.onOptionsItemSelected(item);
    }

    /**
     * Perform Filter Action
     * @param query Search Query
     */
    private void performFilterAction(String query) {
        final ArrayList<UrlItem> filteredModelList = filter(itemArrayList, query);
        overviewRvAdapter.animateTo(filteredModelList);
        rvOverview.scrollToPosition(0);
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
}
