package com.yeahdev.yeahurls.fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.yeahdev.yeahurls.R;

public class HomeFragment extends Fragment {

    public HomeFragment() {}
    public static HomeFragment newInstance() { return new HomeFragment(); }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(false);
        return inflater.inflate(R.layout.fragment_home, container, false);
    }
}
