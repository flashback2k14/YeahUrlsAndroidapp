package com.yeahdev.yeahurls.fragments;


import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.yeahdev.yeahurls.R;
import com.yeahdev.yeahurls.util.Utilities;

public class NotesFragment extends Fragment {

    public NotesFragment() {}
    public static NotesFragment newInstance() { return new NotesFragment(); }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_notes, container, false);

        FloatingActionButton fabAddNote = (FloatingActionButton) v.findViewById(R.id.fabAddNote);
        fabAddNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utilities.buildSnackbar(getActivity(), "ToDo: Add Note");
            }
        });

        return v;
    }
}
