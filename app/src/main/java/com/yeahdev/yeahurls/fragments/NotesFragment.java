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

import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.yeahdev.yeahurls.R;
import com.yeahdev.yeahurls.activities.AddNoteActivity;
import com.yeahdev.yeahurls.adapter.OverviewNotesRvAdapter;
import com.yeahdev.yeahurls.adapter.OverviewRvAdapter;
import com.yeahdev.yeahurls.interfaces.ICommunicationAdapter;
import com.yeahdev.yeahurls.model.NoteItem;
import com.yeahdev.yeahurls.model.UrlItem;
import com.yeahdev.yeahurls.model.UserCreds;
import com.yeahdev.yeahurls.util.UserHelper;
import com.yeahdev.yeahurls.util.Utilities;

import java.util.ArrayList;
import java.util.HashMap;

public class NotesFragment extends Fragment implements ICommunicationAdapter {
    private ProgressDialog progressDialog;
    private RecyclerView rvNotes;

    private OverviewNotesRvAdapter overviewNotesRvAdapter;
    private ArrayList<NoteItem> itemArrayList;

    public NotesFragment() { itemArrayList = new ArrayList<>(); }
    public static NotesFragment newInstance() { return new NotesFragment(); }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_notes, container, false);
        setHasOptionsMenu(true);

        progressDialog = ProgressDialog.show(getActivity(), "Loading", "Get Notes from Firebase...", false, true);
        itemArrayList.clear();

        String userId = this.getArguments().getString("userId", "");
        long expireDate = this.getArguments().getLong("expireDate", 0);
        long currentTimestamp = System.currentTimeMillis() / 1000;

        UserCreds userCreds = UserHelper.createUserCredsObject(userId, expireDate);

        rvNotes = (RecyclerView) v.findViewById(R.id.rvNotes);
        rvNotes.setLayoutManager(new LinearLayoutManager(getActivity()));
        rvNotes.setItemAnimator(new DefaultItemAnimator());

        overviewNotesRvAdapter = new OverviewNotesRvAdapter(getActivity(), userCreds, this);
        rvNotes.setAdapter(overviewNotesRvAdapter);

        if ((expireDate > 0) && (expireDate != currentTimestamp)) {
            loadNotesDataFromFirebase(userCreds.getUserId());
        }

        FloatingActionButton fab = (FloatingActionButton) v.findViewById(R.id.fabAddNote);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), AddNoteActivity.class));
            }
        });

        return v;
    }

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
                            itemArrayList.add(noteItem);
                            overviewNotesRvAdapter.addItem(itemArrayList.size() - 1, noteItem);
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

    private NoteItem createNoteItem(String objId, DataSnapshot child) {
        NoteItem noteItem = new NoteItem();

        HashMap<String, NoteItem> map = (HashMap<String, NoteItem>) child.getValue();
        noteItem.setTimestamp(Long.parseLong(String.valueOf(map.get("timestamp"))));
        noteItem.setValue(String.valueOf(map.get("value")));
        noteItem.setId(Integer.parseInt(String.valueOf(map.get("id"))));
        noteItem.setKeywords(String.valueOf(map.get("keywords")));
        noteItem.setTitle(String.valueOf(map.get("title")));

        if(map.get("objId") != null) {
            noteItem.setObjId(String.valueOf(map.get("objId")));
        } else {
            noteItem.setObjId(objId);
        }

        return noteItem;
    }

    public void removeAllItemsFromRv() {
        try {
            overviewNotesRvAdapter.removeAllItems();
        } catch (Exception e) {
            Utilities.buildSnackbar(getActivity(), "removeAllItemsFromRv failed! Error: " + e.getMessage());
        }
    }

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

    @Override
    public void getRemovedItemPosition(int position) {
        itemArrayList.remove(position);
        overviewNotesRvAdapter.notifyDataSetChanged();
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
                final ArrayList<NoteItem> filteredModelList = filter(itemArrayList, query);
                overviewNotesRvAdapter.animateTo(filteredModelList);
                rvNotes.scrollToPosition(0);
                return true;
            }
        });
    }
}
