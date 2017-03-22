package uk.co.frips.sample.login.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.Nullable;

import uk.co.frips.sample.login.data.entity.Credentials;

public class CredentialStoreImpl implements CredentialStore {
    private static final String MyPREFERENCES = "MyPREFERENCES";
    private static final String EMAIL = "email";
    private static final String PASSWORD = "password";
    private final SharedPreferences mSharedPreferences;

    public CredentialStoreImpl(Context context) {
        mSharedPreferences = context.getApplicationContext().getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
    }

    @Override
    @Nullable
    public Credentials getCredentials() {
        String email = mSharedPreferences.getString(EMAIL, null);
        String password = mSharedPreferences.getString(PASSWORD, null);
        if (password == null || email == null ) {
            return null;
        } else {
            return new Credentials(email, password);
        }
    }

    @Override
    public void saveCredentials(Credentials credentials) {
        String email = null;
        String password = null;
        if (credentials != null) {
            email = credentials.getEmail();
            password = credentials.getPassword();
        }
        mSharedPreferences.edit()
                .putString(EMAIL, email)
                .putString(PASSWORD, password)
                .apply();
    }
}
