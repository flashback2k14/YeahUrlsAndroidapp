package com.yeahdev.yeahurls;

import android.content.DialogInterface;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.view.Menu;
import android.view.View;
import android.view.MenuItem;
import android.content.res.Configuration;
import android.content.SharedPreferences;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.*;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.design.widget.NavigationView;
import android.support.design.widget.FloatingActionButton;
import android.widget.TextView;

import com.firebase.client.Firebase;
import com.yeahdev.yeahurls.fragments.LoginFragment;
import com.yeahdev.yeahurls.fragments.NotesFragment;
import com.yeahdev.yeahurls.fragments.OverviewFragment;
import com.yeahdev.yeahurls.interfaces.ICommunication;
import com.yeahdev.yeahurls.model.User;
import com.yeahdev.yeahurls.model.UserCreds;
import com.yeahdev.yeahurls.util.SharedPreferencesHelper;
import com.yeahdev.yeahurls.util.UserHelper;
import com.yeahdev.yeahurls.util.Utilities;

public class MainActivity extends AppCompatActivity implements ICommunication {
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle drawerToggle;
    private FloatingActionButton fabAdd;
    private TextView tvHeaderEmail;

    private LoginFragment loginFragment;
    private OverviewFragment overviewFragment;
    private NotesFragment notesFragment;

    private UserCreds fireBaseUserCreds;
    private User fireBaseUser;

    private SharedPreferences preferences;

    private static final String LOGIN_FRAGMENT = "LOGIN_FRAGMENT";
    private static final String OVERVIEW_FRAGMENT = "OVERVIEW_FRAGMENT";
    private static final String NOTES_FRAGMENT = "NOTES_FRAGMENT";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Firebase.setAndroidContext(this);

        setDrawerLayout();
        setNavigationView();
        setupToolbar();
        setupFab();

        setupFragments();
        setupPreferences();
    }

    /**
     * Init Layout
     */
    private void setDrawerLayout() {
        drawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);
        drawerToggle = new ActionBarDrawerToggle(MainActivity.this, drawerLayout, R.string.drawer_open, R.string.drawer_close);
        drawerLayout.setDrawerListener(drawerToggle);
    }

    private void setNavigationView() {
        NavigationView navigationView = (NavigationView) findViewById(R.id.navigation);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {

                switch(menuItem.getItemId()) {
                    case R.id.navigation_item_login:
                        drawerLayout.closeDrawers();
                        showLoginFragment();
                        break;

                    case R.id.navigation_item_overview:
                        drawerLayout.closeDrawers();
                        showOverviewFragment();
                        break;

                    case R.id.navigation_item_note:
                        drawerLayout.closeDrawers();
                        //showNotesFragment();
                        Utilities.buildSnackbar(MainActivity.this, "Not implemented, yet!");
                        break;

                    case R.id.navigation_item_logout:
                        drawerLayout.closeDrawers();
                        logoutFromFirebase();
                        break;

                    default:
                        break;
                }
                return false;
            }
        });
        tvHeaderEmail = (TextView) findViewById(R.id.tvHeaderEmail);
    }

    private void setupToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final ActionBar ab = getSupportActionBar();
        ab.setHomeButtonEnabled(true);
        ab.setDisplayHomeAsUpEnabled(true);
    }

    private void setupFab() {
        fabAdd = (FloatingActionButton) findViewById(R.id.fabAdd);
        fabAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utilities.buildSnackbar(MainActivity.this, "From FAB");
            }
        });
    }

    private void setupFragments() {
        this.loginFragment = LoginFragment.newInstance();
        this.overviewFragment = OverviewFragment.newInstance();
        this.notesFragment = NotesFragment.newInstance();
    }

    private void showLoginFragment() {
        LoginFragment loginFragment = (LoginFragment) getSupportFragmentManager().findFragmentByTag(LOGIN_FRAGMENT);

        if (loginFragment == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.contentFrame, this.loginFragment, LOGIN_FRAGMENT).commit();
            this.fabAdd.setVisibility(View.GONE);
        }
    }

    private void showOverviewFragment() {
        OverviewFragment overviewFragment = (OverviewFragment) getSupportFragmentManager().findFragmentByTag(OVERVIEW_FRAGMENT);

        if (overviewFragment == null) {
            Bundle userData = new Bundle();
            if (this.fireBaseUserCreds != null) {
                userData.putString("userId", this.fireBaseUserCreds.getUserId());
                userData.putLong("expireDate", this.fireBaseUserCreds.getExpireDate());
            }
            this.overviewFragment.setArguments(userData);
            getSupportFragmentManager().beginTransaction().replace(R.id.contentFrame, this.overviewFragment, OVERVIEW_FRAGMENT).commit();

            this.fabAdd.setVisibility(View.GONE);
            //this.fabAdd.setVisibility(View.VISIBLE); //temporary gone
        }
    }

    private void showNotesFragment() {
        NotesFragment notesFragment = (NotesFragment) getSupportFragmentManager().findFragmentByTag(NOTES_FRAGMENT);

        if (notesFragment == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.contentFrame, this.notesFragment, NOTES_FRAGMENT).commit();
            this.fabAdd.setVisibility(View.GONE);
        }
    }

    private void logoutFromFirebase() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle("Logout");
        alertDialogBuilder
            .setMessage("Are you sure to logout?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int id) {

                        Firebase ref = new Firebase("https://yeah-url-extension.firebaseio.com/");
                        ref.unauth();

                        SharedPreferencesHelper.removeUserDataFromPreferences(MainActivity.this.preferences, SharedPreferencesHelper.RemoveType.All);

                        MainActivity.this.fireBaseUserCreds = null;
                        MainActivity.this.fireBaseUser = null;
                        tvHeaderEmail.setText("");

                        OverviewFragment overviewFragment = (OverviewFragment) getSupportFragmentManager().findFragmentByTag(OVERVIEW_FRAGMENT);
                        overviewFragment.removeAllItemsFromRv();

                        showLoginFragment();
                    }
                })
                .setNegativeButton("No",new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int id) {
                        dialog.cancel();
                    }
                });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    private void setupPreferences() {
        preferences = PreferenceManager.getDefaultSharedPreferences(this);

        if (fireBaseUserCreds == null) {
            fireBaseUserCreds = SharedPreferencesHelper.getUserCredsFromPreferences(this.preferences);

            if (UserHelper.userStillLoggedIn(fireBaseUserCreds.getExpireDate())) {
                fireBaseUser = SharedPreferencesHelper.getUserFromPreferences(this.preferences);
                tvHeaderEmail.setText(fireBaseUser.getEmailAddress());
                showOverviewFragment();
            } else {
                showLoginFragment();
            }
        }
    }

    /**
     * Interface Methods
     */
    @Override
    public void passUserFromFirebase(UserCreds userCreds, User user) {
        this.fireBaseUserCreds = userCreds;
        this.fireBaseUser = user;

        SharedPreferencesHelper.setUserToPreferences(this.preferences, this.fireBaseUser);
        SharedPreferencesHelper.setUserCredsToPreferences(this.preferences, this.fireBaseUserCreds);

        tvHeaderEmail.setText(fireBaseUser.getEmailAddress());

        showOverviewFragment();
    }

    /**
     * Default Activity Methods
     */
    @Override
    public void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        drawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        drawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (drawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        switch (item.getItemId()) {
            case android.R.id.home:
                drawerLayout.openDrawer(GravityCompat.START);
                return true;
            case R.id.action_settings:
                Utilities.buildSnackbar(this, "From Settings");
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}

/*
private BubblesManager bubblesManager;

setupBubbles();

private void setupBubbles() {
    bubblesManager = new BubblesManager.Builder(this)
            .setTrashLayout(R.layout.bubble_trash_layout)
            .build();
    bubblesManager.initialize();
}

private void addNewBubble() {
    BubbleLayout bubbleView = (BubbleLayout) LayoutInflater.from(MainActivity.this).inflate(R.layout.bubble_layout, null);
    bubbleView.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            // http://www.android-ios-tutorials.com/android/custom-android-dialog-alertdialog-example/
        }
    });
    bubbleView.setOnBubbleRemoveListener(new BubbleLayout.OnBubbleRemoveListener() {
        @Override
        public void onBubbleRemoved(BubbleLayout bubble) {
        }
    });
    bubblesManager.addBubble(bubbleView, 60, 20);
}

@Override
protected void onDestroy() {
    super.onDestroy();
    bubblesManager.recycle();
}
*/