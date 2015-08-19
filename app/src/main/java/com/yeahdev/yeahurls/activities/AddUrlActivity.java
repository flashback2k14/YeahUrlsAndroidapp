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
import android.widget.Toast;

import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.yeahdev.yeahurls.R;
import com.yeahdev.yeahurls.model.NoteItem;
import com.yeahdev.yeahurls.model.UrlItem;
import com.yeahdev.yeahurls.model.User;
import com.yeahdev.yeahurls.model.UserCreds;
import com.yeahdev.yeahurls.util.SharedPreferencesHelper;
import com.yeahdev.yeahurls.util.UserHelper;
import com.yeahdev.yeahurls.util.Utilities;

import org.w3c.dom.Text;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AddUrlActivity extends AppCompatActivity implements View.OnClickListener {

    private Intent intent = null;

    private EditText etKeywordsUrl;
    private TextView tvUrl;
    private ImageButton btnClearKeywordsUrl;
    private Button btnCancel, btnSave, btnUpdate;

    private UserCreds userCreds;
    private int id;
    private String objId;

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

        btnCancel = (Button) findViewById(R.id.btnCancel);
        btnSave = (Button) findViewById(R.id.btnSave);
        btnUpdate = (Button) findViewById(R.id.btnUpdate);
    }

    private void initButtonListener() {
        btnClearKeywordsUrl.setOnClickListener(this);
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
                            tvUrl.setText(url.toString());
                        } catch (MalformedURLException e) {
                            Utilities.buildToast(this, "Get URL failed!", Toast.LENGTH_LONG);
                        }
                    }
                }
                if (action.equals(Intent.ACTION_SEND)) {
                    String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
                    if (sharedText != null) {
                        tvUrl.setText(sharedText);
                    }
                }
                btnUpdate.setVisibility(View.GONE);
                btnSave.setVisibility(View.VISIBLE);
            } else {
                Bundle bundle = intent.getExtras();
                if (bundle != null) {
                    id = bundle.getInt("Id");
                    objId = bundle.getString("ObjId");
                    etKeywordsUrl.setText(bundle.getString("Keywords"));
                    tvUrl.setText(bundle.getString("Url"));

                    btnSave.setVisibility(View.GONE);
                    btnUpdate.setVisibility(View.VISIBLE);
                }
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnClearKeywordsUrl:
                etKeywordsUrl.setText("");
                break;
            case R.id.btnCancel:
                AddUrlActivity.this.finish();
                break;
            case R.id.btnSave:
                if (saveUrl()) {
                    AddUrlActivity.this.finish();
                }
                break;
            case R.id.btnUpdate:
                if (updateUrl()) {
                    AddUrlActivity.this.finish();
                }
            default:
                break;
        }
    }

    private boolean saveUrl() {
        if (UserHelper.userStillLoggedIn(userCreds.getExpireDate())) {
            try {
                Firebase ref = new Firebase("https://yeah-url-extension.firebaseio.com/" + userCreds.getUserId() + "/urlcollector");
                Firebase urlRef = ref.push();

                List<UrlItem> urlItemList = new ArrayList<>(1);
                UrlItem urlItem = new UrlItem();
                urlItem.setId(1);
                urlItem.setTimestamp(String.valueOf(Math.round(new Date().getTime() / 1000.0)));
                urlItem.setTime(new SimpleDateFormat("HH:mm:ss", Locale.GERMANY).format(System.currentTimeMillis()));
                urlItem.setDate(new SimpleDateFormat("dd.MM.yyyy", Locale.GERMANY).format(System.currentTimeMillis()));
                urlItem.setKeywords(etKeywordsUrl.getText().toString());
                urlItem.setValue(tvUrl.getText().toString());
                urlItem.setObjId(urlRef.getKey());
                urlItemList.add(urlItem);

                urlRef.setValue(urlItemList, new Firebase.CompletionListener() {
                    @Override
                    public void onComplete(FirebaseError firebaseError, Firebase firebase) {
                        if (firebaseError != null) {
                            Utilities.buildToast(AddUrlActivity.this, "Url saving failed: " + firebaseError.getMessage(), Toast.LENGTH_LONG);
                        } else {
                            Utilities.buildToast(AddUrlActivity.this, "Url saved successfully!", Toast.LENGTH_LONG);
                        }
                    }
                });
            } catch (Exception e) {
                Utilities.buildToast(this, "Add Url failed! Error: " + e.getMessage(), Toast.LENGTH_LONG);
                return false;
            }
        } else {
            Utilities.buildToast(this, "User is not logged in!", Toast.LENGTH_LONG);
            startActivity(new Intent(AddUrlActivity.this, MainActivity.class));
        }
        return true;
    }

    private boolean updateUrl() {
        if (UserHelper.userStillLoggedIn(userCreds.getExpireDate())) {
            try {
                Firebase ref = new Firebase("https://yeah-url-extension.firebaseio.com/" + userCreds.getUserId() + "/urlcollector/" + objId + "/" + id);

                Map<String, Object> updateNote = new HashMap<>(2);
                updateNote.put("keywords", etKeywordsUrl.getText().toString());
                updateNote.put("value", tvUrl.getText().toString());

                ref.updateChildren(updateNote, new Firebase.CompletionListener() {
                    @Override
                    public void onComplete(FirebaseError firebaseError, Firebase firebase) {
                        if (firebaseError != null) {
                            Utilities.buildToast(AddUrlActivity.this, "Url updating failed: " + firebaseError.getMessage(), Toast.LENGTH_LONG);
                        } else {
                            Utilities.buildToast(AddUrlActivity.this, "Url updated successfully!", Toast.LENGTH_LONG);
                        }
                    }
                });
            } catch (Exception e) {
                Utilities.buildToast(this, "Update Url failed! Error: " + e.getMessage(), Toast.LENGTH_LONG);
                return false;
            }

        } else {
            Utilities.buildToast(this, "User is not logged in!", Toast.LENGTH_LONG);
            startActivity(new Intent(AddUrlActivity.this, MainActivity.class));
        }
        return true;
    }
}
