package com.yeahdev.yeahurls.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;

import com.yeahdev.yeahurls.R;
import com.yeahdev.yeahurls.model.NoteItem;
import com.yeahdev.yeahurls.model.UserCreds;
import com.yeahdev.yeahurls.util.SharedPreferencesHelper;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;


public class AddNoteActivity extends AppCompatActivity implements View.OnClickListener {
    private Intent intent;

    private EditText etTitle, etKeywords, etNotes;
    private ImageButton btnClearTitle, btnClearKeywords, btnClearNotes;
    private Button btnCancel, btnSave;

    private UserCreds userCreds;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_note);

        this.setFinishOnTouchOutside(false);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        userCreds = SharedPreferencesHelper.getUserCredsFromPreferences(preferences);
        intent = getIntent();

        initComponents();
        initButtonListener();
        getSharingData();
    }

    private void initComponents() {
        etTitle = (EditText) findViewById(R.id.etTitle);
        etKeywords = (EditText) findViewById(R.id.etKeywords);
        etNotes = (EditText) findViewById(R.id.etNotes);

        btnClearTitle = (ImageButton) findViewById(R.id.btnClearTitle);
        btnClearKeywords = (ImageButton) findViewById(R.id.btnClearKeywords);
        btnClearNotes = (ImageButton) findViewById(R.id.btnClearNote);

        btnCancel = (Button) findViewById(R.id.btnCancelNote);
        btnSave = (Button) findViewById(R.id.btnSaveNote);
    }

    private void initButtonListener() {
        btnClearTitle.setOnClickListener(this);
        btnClearKeywords.setOnClickListener(this);
        btnClearNotes.setOnClickListener(this);
        btnCancel.setOnClickListener(this);
        btnSave.setOnClickListener(this);
    }

    private void getSharingData() {
        String action = intent.getAction();
        if (action == null) return;

        if (action.equals(Intent.ACTION_VIEW)) {
            Uri data = intent.getData();
            if (data != null) {
                try {
                    URL url = new URL(data.getScheme(), data.getHost(), data.getPath());
                    etNotes.setText(url.toString());
                } catch (MalformedURLException e) {
                    Log.d("Yeah!Urls", "AddNoteActivity: Error: " + e.getMessage());
                }
            } else {
                Log.d("Yeah!Urls", "AddNoteActivity: Error: No Data!");
            }
        }

        if (action.equals(Intent.ACTION_SEND)) {
            String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
            if (sharedText != null) {
                etNotes.setText(sharedText);
            }else {
                Log.d("Yeah!Urls", "AddNoteActivity: Error: No sharedText!");
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnClearTitle:
                etTitle.setText("");
                break;
            case R.id.btnClearKeywords:
                etKeywords.setText("");
                break;
            case R.id.btnClearNote:
                etNotes.setText("");
                break;
            case R.id.btnCancelNote:
                AddNoteActivity.this.finish();
                break;
            case R.id.btnSaveNote:
                if (saveNote()) {
                    AddNoteActivity.this.finish();
                }
                break;
            default:
                break;
        }
    }

    private boolean saveNote() {
        long currentTimestamp = System.currentTimeMillis() / 1000;
        long expireDate = userCreds.getExpireDate();

        if ((expireDate == 0) && (expireDate == currentTimestamp)) {
            return false;
        }

        try {
            Firebase ref = new Firebase("https://yeah-url-extension.firebaseio.com/" + userCreds.getUserId() + "/notescollector");
            Firebase noteRef = ref.push();

            List<NoteItem> noteItemList = new ArrayList<>();
            NoteItem noteItem = new NoteItem();
            noteItem.setId(1);
            noteItem.setTimestamp(currentTimestamp);
            noteItem.setTitle(etTitle.getText().toString());
            noteItem.setKeywords(etKeywords.getText().toString());
            noteItem.setValue(etNotes.getText().toString());
            noteItem.setObjId(noteRef.getKey());
            noteItemList.add(noteItem);

            noteRef.setValue(noteItemList, new Firebase.CompletionListener() {
                @Override
                public void onComplete(FirebaseError firebaseError, Firebase firebase) {
                    if (firebaseError != null) {
                        Log.d("Yeah!Urls", "Data could not be saved. " + firebaseError.getMessage());
                    } else {
                        Log.d("Yeah!Urls", "Data saved successfully.");
                    }
                }
            });

        } catch (Exception e) {
            Log.d("Yeah!Urls", "addChildEventListener failed! Error: " + e.getMessage());
            return false;
        }

        return true;
    }
}
