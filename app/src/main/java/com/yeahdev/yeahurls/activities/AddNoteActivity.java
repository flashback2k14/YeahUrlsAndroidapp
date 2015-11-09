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
import android.widget.Toast;

import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;

import com.yeahdev.yeahurls.R;
import com.yeahdev.yeahurls.model.NoteItem;
import com.yeahdev.yeahurls.model.UserCreds;
import com.yeahdev.yeahurls.util.SharedPreferencesHelper;
import com.yeahdev.yeahurls.util.UserHelper;
import com.yeahdev.yeahurls.util.Utilities;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AddNoteActivity extends AppCompatActivity implements View.OnClickListener {
    private Intent intent;

    private EditText etTitle, etKeywords, etNotes;
    private ImageButton btnClearTitle, btnClearKeywords, btnClearNotes;
    private Button btnCancel, btnSave, btnUpdate;

    private UserCreds userCreds;
    private int id;
    private String objId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_note);

        this.setFinishOnTouchOutside(false);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        userCreds = SharedPreferencesHelper.getUserCredsFromPreferences(preferences);
        intent = getIntent();

        if (UserHelper.userStillLoggedIn(userCreds.getExpireDate())) {
            initComponents();
            initButtonListener();
            getSharingData();
        } else {
            Utilities.buildToast(this, "User is not logged in!", Toast.LENGTH_LONG);
            startActivity(new Intent(AddNoteActivity.this, MainActivity.class));
            finish();
        }
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
        btnUpdate = (Button) findViewById(R.id.btnUpdateNote);
    }

    private void initButtonListener() {
        btnClearTitle.setOnClickListener(this);
        btnClearKeywords.setOnClickListener(this);
        btnClearNotes.setOnClickListener(this);
        btnCancel.setOnClickListener(this);
        btnSave.setOnClickListener(this);
        btnUpdate.setOnClickListener(this);
    }

    private void getSharingData() {
        if (intent != null) {
            String action = intent.getAction();
            if (action != null) {
                if (action.equals(Intent.ACTION_VIEW)) {
                    Uri data = intent.getData();
                    if (data != null) {
                        try {
                            URL url = new URL(data.getScheme(), data.getHost(), data.getPath());
                            etNotes.setText(url.toString());
                        } catch (MalformedURLException e) {
                            Utilities.buildToast(this, "Get URL failed!", Toast.LENGTH_LONG);
                        }
                    }
                }
                if (action.equals(Intent.ACTION_SEND)) {
                    String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
                    if (sharedText != null) {
                        etNotes.setText(sharedText);
                    }
                }
                btnUpdate.setVisibility(View.GONE);
                btnSave.setVisibility(View.VISIBLE);
            } else {
                Bundle bundle = intent.getExtras();
                if (bundle != null) {
                    id = bundle.getInt("Id");
                    objId = bundle.getString("ObjId");
                    etTitle.setText(bundle.getString("Title"));
                    etKeywords.setText(bundle.getString("Keywords"));
                    etNotes.setText(bundle.getString("Note"));

                    btnSave.setVisibility(View.GONE);
                    btnUpdate.setVisibility(View.VISIBLE);
                }
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
            case R.id.btnUpdateNote:
                if (updateNote()) {
                    AddNoteActivity.this.finish();
                }
            default:
                break;
        }
    }

    private boolean saveNote() {
        if (UserHelper.userStillLoggedIn(userCreds.getExpireDate())) {
            try {
                Firebase ref = new Firebase("https://yeah-url-extension.firebaseio.com/" + userCreds.getUserId() + "/notescollector");
                Firebase noteRef = ref.push();

                List<NoteItem> noteItemList = new ArrayList<>();
                NoteItem noteItem = new NoteItem();
                noteItem.setId(1);
                noteItem.setTimestamp(Math.round(new Date().getTime() / 1000.0));
                noteItem.setTitle(etTitle.getText().toString());
                noteItem.setKeywords(etKeywords.getText().toString());
                noteItem.setValue(etNotes.getText().toString());
                noteItem.setObjId(noteRef.getKey());
                noteItemList.add(noteItem);

                noteRef.setValue(noteItemList, new Firebase.CompletionListener() {
                    @Override
                    public void onComplete(FirebaseError firebaseError, Firebase firebase) {
                        if (firebaseError != null) {
                            Utilities.buildToast(AddNoteActivity.this, "Note saving failed: " + firebaseError.getMessage(), Toast.LENGTH_LONG);
                        } else {
                            Utilities.buildToast(AddNoteActivity.this, "Note saved successfully!", Toast.LENGTH_LONG);
                        }
                    }
                });
            } catch (Exception e) {
                Utilities.buildToast(this, "Add Note failed! Error: " + e.getMessage(), Toast.LENGTH_LONG);
                return false;
            }
        } else {
            Utilities.buildToast(this, "User is not logged in!", Toast.LENGTH_LONG);
            startActivity(new Intent(AddNoteActivity.this, MainActivity.class));
            return true;
        }
        return true;
    }

    private boolean updateNote() {
        if (UserHelper.userStillLoggedIn(userCreds.getExpireDate())) {
            try {
                Firebase ref = new Firebase("https://yeah-url-extension.firebaseio.com/" + userCreds.getUserId() + "/notescollector/" + objId + "/" + id);

                Map<String, Object> updateNote = new HashMap<>(3);
                updateNote.put("title", etTitle.getText().toString());
                updateNote.put("keywords", etKeywords.getText().toString());
                updateNote.put("value", etNotes.getText().toString());

                ref.updateChildren(updateNote, new Firebase.CompletionListener() {
                    @Override
                    public void onComplete(FirebaseError firebaseError, Firebase firebase) {
                        if (firebaseError != null) {
                            Utilities.buildToast(AddNoteActivity.this, "Note updating failed: " + firebaseError.getMessage(), Toast.LENGTH_LONG);
                        } else {
                            Utilities.buildToast(AddNoteActivity.this, "Note updated successfully!", Toast.LENGTH_LONG);
                        }
                    }
                });
            } catch (Exception e) {
                Utilities.buildToast(this, "Update Note failed! Error: " + e.getMessage(), Toast.LENGTH_LONG);
                return false;
            }
        } else {
            Utilities.buildToast(this, "User is not logged in!", Toast.LENGTH_LONG);
            startActivity(new Intent(AddNoteActivity.this, MainActivity.class));
        }
        return true;
    }
}
