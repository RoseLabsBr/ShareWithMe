package edu.rosehulman.roselabs.sharewithme;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.firebase.client.AuthData;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;

import edu.rosehulman.rosefire.RosefireAuth;

public class LoginActivity extends AppCompatActivity {

    private static final String FIREBASE_URL = "https://sharewithme.firebaseio.com/";
    private EditText mUserEdit;
    private EditText mPasswordEdit;
    private Button mLoginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        if(savedInstanceState == null){
            Firebase.setAndroidContext(this);
        }

        mUserEdit = (EditText)findViewById(R.id.user_edit_text);
        mPasswordEdit = (EditText) findViewById(R.id.password_edit_text);
        mLoginButton = (Button) findViewById(R.id.login_button);

        mLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login();
            }
        });
    }

    protected void login (){
        String email = mUserEdit.getText().toString();
        String password = mPasswordEdit.getText().toString();

        if(email.isEmpty() || password.isEmpty()){
            //show error

        }

        if(!email.endsWith("@rose-hulman.edu")){
            email += "@rose-hulman.edu";
        }

        Firebase firebase = new Firebase(FIREBASE_URL);
        RosefireAuth roseAuth = new RosefireAuth(firebase, getString(R.string.rosefire_token));
        roseAuth.authWithRoseHulman(email, password, new MyAuthResultHandler());

    }

    class MyAuthResultHandler implements Firebase.AuthResultHandler {

        @Override
        public void onAuthenticated(AuthData authData) {
//          switchToPasswordFragment(Constants.FIREBASE_URL + "/users/" + authData.getUid());

            Intent intent = new Intent(LoginActivity.this, MainActivity.class);

            startActivity(intent);

        }

        @Override
        public void onAuthenticationError(FirebaseError firebaseError) {
            Log.e("BILADA", "onAuthentication" + firebaseError.getMessage());
        }
    }

}