package uk.co.frips.sample.login.data;

import android.support.annotation.Nullable;

import uk.co.frips.sample.login.data.entity.Credentials;

public interface CredentialStore {
    @Nullable
    Credentials getCredentials();

    void saveCredentials(Credentials credentials);
}
