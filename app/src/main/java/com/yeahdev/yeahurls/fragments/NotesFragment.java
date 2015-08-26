package com.yeahdev.yeahurls.fragments;


import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.yeahdev.yeahurls.R;
import com.yeahdev.yeahurls.activities.AddNoteActivity;
import com.yeahdev.yeahurls.adapter.OverviewNotesRvAdapter;
import com.yeahdev.yeahurls.interfaces.ICommunicationAdapter;
import com.yeahdev.yeahurls.model.NoteItem;
import com.yeahdev.yeahurls.model.UserCreds;
import com.yeahdev.yeahurls.util.UserHelper;
import com.yeahdev.yeahurls.util.Utilities;

import java.util.ArrayList;

public class NotesFragment extends Fragment implements ICommunicationAdapter {
    private ProgressDialog progressDialog;
    private FloatingActionButton fab;
    private RecyclerView rvNotes;

    private OverviewNotesRvAdapter overviewNotesRvAdapter;
    private ArrayList<NoteItem> itemArrayList;

    public NotesFragment() { itemArrayList = new ArrayList<>(); }
    public static NotesFragment newInstance() { return new NotesFragment(); }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_notes, container, false);
        setHasOptionsMenu(true);

        progressDialog = ProgressDialog.show(getActivity(), "Loading", "Get Notes from Firebase...", false, true);
        itemArrayList.clear();

        fab = (FloatingActionButton) v.findViewById(R.id.fabAddNote);

        String userId = this.getArguments().getString("userId", "");
        long expireDate = this.getArguments().getLong("expireDate", 0);

        if (!"".equals(userId) || expireDate != 0) {
            UserCreds userCreds = UserHelper.createUserCredsObject(userId, expireDate);

            rvNotes = (RecyclerView) v.findViewById(R.id.rvNotes);
            rvNotes.setLayoutManager(new LinearLayoutManager(getActivity()));
            rvNotes.setItemAnimator(new DefaultItemAnimator());

            overviewNotesRvAdapter = new OverviewNotesRvAdapter(getActivity(), userCreds, this);
            rvNotes.setAdapter(overviewNotesRvAdapter);

            if (UserHelper.userStillLoggedIn(expireDate)) {
                loadNotesDataFromFirebase(userCreds.getUserId());
            }

            rvNotes.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                    super.onScrollStateChanged(recyclerView, newState);

                    if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                        fab.hide();
                    } else {
                        fab.show();
                    }
                }
            });
        } else {
            Utilities.buildSnackbar(getActivity(), "No valid User available!");
            progressDialog.dismiss();
        }

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), AddNoteActivity.class));
            }
        });

        return v;
    }

    /**
     * Method to Load Data from Firebase
     * @param userId User Id
     */
    private void loadNotesDataFromFirebase(String userId) {
        try {
            Firebase ref = new Firebase("https://yeah-url-extension.firebaseio.com/" + userId + "/notescollector");
            ref.addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot snapshot, String s) {
                    String objId = snapshot.getKey();
                    for (DataSnapshot child : snapshot.getChildren()) {
                        try {
                            NoteItem noteItem = createNoteItem(objId, child);
                            if (noteItem != null) {
                                itemArrayList.add(noteItem);
                                overviewNotesRvAdapter.addItem(itemArrayList.size() - 1, noteItem);
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
                            NoteItem noteItem = createNoteItem(objId, child);
                            if (noteItem != null) {
                                overviewNotesRvAdapter.searchAndReplaceItem(noteItem);
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
     * Helper Method to generated NoteItem from Firebase DataSnapshot child
     * @param objId Object Id
     * @param child DataSnapshot
     * @return NoteItem
     */
    private NoteItem createNoteItem(String objId, DataSnapshot child) {
        NoteItem noteItem = null;
        try {
            noteItem = child.getValue(NoteItem.class);
            if (noteItem.getObjId() == null) {
                noteItem.setObjId(objId);
            }
        } catch(Exception e) {
            Utilities.buildToast(getActivity(), "createNoteItem failed! " + e.getMessage(), Toast.LENGTH_LONG);
        }
        return noteItem;
    }

    /**
     * Method to Remove all Adapter Elements
     */
    public void removeAllItemsFromRv() {
        try {
            overviewNotesRvAdapter.removeAllItems();
        } catch (Exception e) {
            Utilities.buildToast(getActivity(), "removeAllItemsFromRv failed! Error: " + e.getMessage(), Toast.LENGTH_LONG);
        }
    }

    /**
     * Method to Filter Adapter Elements that matching the Query
     * @param noteCollection old Elements List
     * @param query Search Query
     * @return new Elements List
     */
    private ArrayList<NoteItem> filter(ArrayList<NoteItem> noteCollection, String query) {
        query = query.toLowerCase();

        if (TextUtils.isEmpty(query)) {
            return noteCollection;
        } else {
            final ArrayList<NoteItem> filteredModelList = new ArrayList<>();
            for (NoteItem item : noteCollection) {
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

    /**
     * Method to Add Element to Temp Element List
     * @param position Position in ArrayList
     * @param item Item Object
     */
    @Override
    public void getAddedItemPosition(int position, Object item) {
        NoteItem noteItem = (NoteItem) item;
        if (!itemArrayList.contains(noteItem)) {
            itemArrayList.add(position, noteItem);
        }
    }

    /**
     * Method to Remove Element from Temp Element List
     * @param position Position in ArrayList
     */
    @Override
    public void getRemovedItemPosition(int position) {
        itemArrayList.remove(position);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        final MenuItem menuItem = menu.findItem(R.id.action_search);
        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(menuItem);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                final ArrayList<NoteItem> filteredModelList = filter(itemArrayList, query);
                overviewNotesRvAdapter.animateTo(filteredModelList);
                rvNotes.scrollToPosition(0);
                return true;
            }
        });
    }
}
