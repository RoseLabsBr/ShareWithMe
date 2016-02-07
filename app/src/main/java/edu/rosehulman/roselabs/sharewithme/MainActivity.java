package edu.rosehulman.roselabs.sharewithme;

import android.annotation.TargetApi;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.camera.CropImageIntentBuilder;
import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.Query;
import com.firebase.client.ValueEventListener;
import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import edu.rosehulman.roselabs.sharewithme.BuyAndSell.BuyAndSellFragment;
import edu.rosehulman.roselabs.sharewithme.BuyAndSell.BuySellAdapter;
import edu.rosehulman.roselabs.sharewithme.BuyAndSell.BuySellPost;
import edu.rosehulman.roselabs.sharewithme.Drafts.DraftsBuySellAdapter;
import edu.rosehulman.roselabs.sharewithme.Drafts.DraftsFragment;
import edu.rosehulman.roselabs.sharewithme.Drafts.DraftsRidesAdapter;
import edu.rosehulman.roselabs.sharewithme.Interfaces.CreateCallback;
import edu.rosehulman.roselabs.sharewithme.Interfaces.OnListFragmentInteractionListener;
import edu.rosehulman.roselabs.sharewithme.Profile.ProfileFragment;
import edu.rosehulman.roselabs.sharewithme.Profile.UserProfile;
import edu.rosehulman.roselabs.sharewithme.Rides.RidesAdapter;
import edu.rosehulman.roselabs.sharewithme.Rides.RidesDetailFragment;
import edu.rosehulman.roselabs.sharewithme.Rides.RidesFragment;
import edu.rosehulman.roselabs.sharewithme.Rides.RidesPost;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, OnListFragmentInteractionListener, CreateCallback {

    private BuySellAdapter mBuySellAdapter;
    private RidesAdapter mRidesAdapter;
    private BuyAndSellFragment mBuyAndSellFragment;
    private RidesFragment mRidesFragment;
    private ProfileFragment mProfileFragment;
    private ImageView mImageView;
    private UserProfile mUser;
    private Firebase mFirebase;
    private TextView mProfileNameTextView;
    private DraftsBuySellAdapter mDraftsBuySellAdapter;
    private DraftsRidesAdapter mDraftsRidesAdapter;
    private DraftsFragment mDraftsFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Firebase.setAndroidContext(this);
        mFirebase = new Firebase(Constants.FIREBASE_URL);
        if (mFirebase.getAuth() == null) {
            switchToLogin();
            return;
        }

        setupUser(mFirebase.getAuth().getUid());

        mDraftsFragment = new DraftsFragment();
        mBuyAndSellFragment = new BuyAndSellFragment();
        mRidesFragment = new RidesFragment();
        mProfileFragment = new ProfileFragment();

        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        TextView emailView = (TextView) navigationView.getHeaderView(0).findViewById(R.id.email_text_view);
        emailView.setText(new Firebase(Constants.FIREBASE_URL).getAuth().getUid() + "@rose-hulman.edu");

        mProfileNameTextView = (TextView) navigationView.getHeaderView(0).findViewById(R.id.profile_name_header_text_view);

        mImageView = (ImageView) navigationView.getHeaderView(0).findViewById(R.id.user_picture_image_view);
        mImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchToFragment(mProfileFragment);
                DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
                drawer.closeDrawer(GravityCompat.START);
            }
        });

    }

    private void setupUser(String uid) {
        mUser = new UserProfile();
        mUser.setUserID(uid);

        MyChildEvent myChildEvent = new MyChildEvent();

        Query query = mFirebase.child("users").orderByChild("userID").equalTo(uid);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.hasChildren()) {
                    if (mUser.getKey() == null)
                        mFirebase.child("users").push().setValue(mUser);
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
        query.addChildEventListener(myChildEvent);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_logout) {
            onLogout();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        Fragment switchTo = null;
        switch (item.getItemId()) {
            case R.id.menu_preferences:
                //TODO Implement: switchTo = new PreferencesFragment();
                break;
            case R.id.categories_lost_found:
                //TODO Implement: switchTo = new LostFoundFragment();
                break;
            case R.id.categories_events:
                //TODO Implement: switchTo = new EventsFragment();
                break;
            case R.id.categories_others:
                //TODO Implement: switchTo = new OthersFragment();
                break;
            case R.id.menu_help_and_feedback:
                switchTo = new HelpAndFeedbackFragment();
                break;
            case R.id.menu_drafts:
                switchTo = mDraftsFragment;
                break;
            case R.id.categories_rides:
                switchTo = mRidesFragment;
                break;
            case R.id.categories_buy:
                switchTo = mBuyAndSellFragment;
                break;
            default:
                break;
        }

        if (switchTo != null) {
            switchToFragment(switchTo);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void switchToFragment(Fragment fragment) {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.fragment_container, fragment);
        ft.addToBackStack("fragBack");
        ft.commit();
    }

    private void switchToLogin() {
        Firebase firebase = new Firebase(Constants.FIREBASE_URL);
        firebase.unauth();
        finish();
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(intent);
    }

    public void onLogout() {
        switchToLogin();
    }

    @Override
    public void sendAdapterToMain(BuySellAdapter adapter) {
        mBuySellAdapter = adapter;
    }

    @Override
    public void sendAdapterToMain(RidesAdapter adapter) {
        mRidesAdapter = adapter;
    }

    @Override
    public void sendAdapterToMain(DraftsBuySellAdapter adapter) {
        mDraftsBuySellAdapter = adapter;
    }

    @Override
    public void sendAdapterToMain(DraftsRidesAdapter adapter) {
        mDraftsRidesAdapter = adapter;
    }

    @Override
    public void sendFragmentToInflate(Fragment fragment) {
        switchToFragment(fragment);
    }

    @Override
    public void onCreatePostFinished(BuySellPost post) {
        post.setUserId(new Firebase(Constants.FIREBASE_URL).getAuth().getUid());
        mBuySellAdapter.add(post);
    }

    @Override
    public void onCreatePostFinished(RidesPost post) {
        post.setUserId(new Firebase(Constants.FIREBASE_URL).getAuth().getUid());
        mRidesAdapter.addPost(post);
    }

    @Override
    public void onEditPostFinished(RidesPost post) {
        post.setUserId(new Firebase(Constants.FIREBASE_URL).getAuth().getUid());
        mRidesAdapter.update(post);
        getSupportFragmentManager().popBackStack();
        switchToFragment(new RidesDetailFragment(post));
    }

    @Override
    public void onDraftPostFinished(RidesPost post) {
        //TODO DRAFT
        post.setUserId(new Firebase(Constants.FIREBASE_URL).getAuth().getUid());
        Log.d("THAIS", "Chegou no OnDraftPostFinished " + post.getTitle());
        mRidesAdapter.addDraft(post);
    }

    @Override
    public void onDraftPostFinished(BuySellPost post) {
        post.setUserId(new Firebase(Constants.FIREBASE_URL).getAuth().getUid());
        Log.d("THAIS", "Chegou no OnDraftPostFinished " + post.getTitle());
        mBuySellAdapter.addDraft(post);
    }


    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK) return;

        File croppedImageFile = new File(getFilesDir(), "profile_picture.jpg");

        if (requestCode == Constants.PICK_IMAGE_REQUEST) {
            Uri croppedImage = Uri.fromFile(croppedImageFile);
            CropImageIntentBuilder cropImage = new CropImageIntentBuilder(200, 200, croppedImage);
            cropImage.setOutlineColor(0xFF03A9F4);
            cropImage.setSourceImage(data.getData());

            Intent intent = cropImage.getIntent(this);
            if (Build.VERSION.SDK_INT > 19) {
                final int takeFlags = data.getFlags()
                        & (Intent.FLAG_GRANT_READ_URI_PERMISSION
                        | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

                //noinspection ResourceType
                getContentResolver().takePersistableUriPermission(data.getData(), takeFlags);
            }

            startActivityForResult(intent, Constants.CROP_IMAGE_REQUEST);

        } else if (requestCode == Constants.CROP_IMAGE_REQUEST) {
            Bitmap bitmap = BitmapFactory.decodeFile(croppedImageFile.getAbsolutePath());
            String str = Utils.encodeBitmap(bitmap);
            mUser.setPicture(str);
            Map<String, Object> userPicture = new HashMap<>();
            userPicture.put("picture", str);
            mFirebase.child("users").child(mUser.getKey()).updateChildren(userPicture);
        }
    }

    class MyChildEvent implements ChildEventListener {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            UserProfile user = dataSnapshot.getValue(UserProfile.class);
            mUser.setKey(dataSnapshot.getKey());
            if (user.getPicture() != null) {
                mUser.setPicture(user.getPicture());
                mImageView.setImageBitmap(Utils.decodeStringToBitmap(mUser.getPicture()));
            }
            if (user.getName() != null) mProfileNameTextView.setText(user.getName());
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            UserProfile user = dataSnapshot.getValue(UserProfile.class);
            mImageView.setImageBitmap(Utils.decodeStringToBitmap(mUser.getPicture()));
            if (user.getName() != null) mProfileNameTextView.setText(user.getName());
        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {

        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onCancelled(FirebaseError firebaseError) {

        }
    }
}
