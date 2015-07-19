package com.yeahdev.yeahurls.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.yeahdev.yeahurls.R;
import com.yeahdev.yeahurls.model.NoteItem;
import com.yeahdev.yeahurls.model.UrlItem;
import com.yeahdev.yeahurls.model.UserCreds;
import com.yeahdev.yeahurls.util.SharedPreferencesHelper;

import org.w3c.dom.Text;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AddUrlActivity extends AppCompatActivity implements View.OnClickListener {

    private Intent intent;

    private EditText etKeywordsUrl;
    private TextView tvUrl;
    private ImageButton btnClearKeywordsUrl;
    private Button btnCancel, btnSave;

    private UserCreds userCreds;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_url);

        this.setFinishOnTouchOutside(false);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        userCreds = SharedPreferencesHelper.getUserCredsFromPreferences(preferences);
        intent = getIntent();

        initComponents();
        initButtonListener();
        getSharingData();
    }

    private void initComponents() {
        etKeywordsUrl = (EditText) findViewById(R.id.etKeywordsUrl);
        tvUrl = (TextView) findViewById(R.id.tvUrl);
        btnClearKeywordsUrl = (ImageButton) findViewById(R.id.btnClearKeywordsUrl);
        btnCancel = (Button) findViewById(R.id.btnCancelNote);
        btnSave = (Button) findViewById(R.id.btnSaveNote);
    }

    private void initButtonListener() {
        btnClearKeywordsUrl.setOnClickListener(this);
        btnCancel.setOnClickListener(this);
        btnSave.setOnClickListener(this);
    }

    private void getSharingData() {
        String action = intent.getAction();
        if (action.equals(Intent.ACTION_VIEW)) {
            Uri data = intent.getData();
            if (data != null) {
                try {
                    URL url = new URL(data.getScheme(), data.getHost(), data.getPath());
                    tvUrl.setText(url.toString());
                } catch (MalformedURLException e) {
                    Log.d("Yeah!Urls", "AddUrlActivity: Error: " + e.getMessage());
                }
            } else {
                Log.d("Yeah!Urls", "AddUrlActivity: Error: No Data!");
            }
        }

        if (action.equals(Intent.ACTION_SEND)) {
            String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
            if (sharedText != null) {
                tvUrl.setText(sharedText);
            }else {
                Log.d("Yeah!Urls", "AddUrlActivity: Error: No sharedText!");
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnClearKeywordsUrl:
                etKeywordsUrl.setText("");
                break;
            case R.id.btnCancelNote:
                AddUrlActivity.this.finish();
                break;
            case R.id.btnSaveNote:
                if (saveNote()) {
                    AddUrlActivity.this.finish();
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
            Firebase ref = new Firebase("https://yeah-url-extension.firebaseio.com/" + userCreds.getUserId() + "/urlcollector");
            Firebase noteRef = ref.push();

            List<UrlItem> urlItemList = new ArrayList<>();
            UrlItem urlItem = new UrlItem();
            urlItem.setId(1);
            urlItem.setTimestamp(String.valueOf(currentTimestamp));
            urlItem.setTime(new SimpleDateFormat("HH:mm:ss", Locale.GERMANY).format(System.currentTimeMillis()));
            urlItem.setDate(new SimpleDateFormat("dd-MM-yyyy", Locale.GERMANY).format(System.currentTimeMillis()));
            urlItem.setKeywords(etKeywordsUrl.getText().toString());
            urlItem.setValue(tvUrl.getText().toString());
            urlItem.setObjId(noteRef.getKey());
            urlItemList.add(urlItem);

            noteRef.setValue(urlItemList, new Firebase.CompletionListener() {
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
