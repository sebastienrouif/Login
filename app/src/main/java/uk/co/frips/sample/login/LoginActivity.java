package uk.co.frips.sample.login;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;

import butterknife.BindView;
import butterknife.OnClick;
import uk.co.frips.sample.login.base.BaseActivity;
import uk.co.frips.sample.login.data.CredentialStoreImpl;
import uk.co.frips.sample.login.data.UserServicesAdapter;
import uk.co.frips.sample.login.schedulers.SchedulerProvider;
import uk.co.frips.sample.login.widget.MultiView;

import static com.google.common.base.Preconditions.checkNotNull;

public class LoginActivity extends BaseActivity implements LoginContract.View {

    @BindView(R.id.login_multi_view) MultiView mMultiView;
    @BindView(R.id.email) AutoCompleteTextView mEmailView;
    @BindView(R.id.password) EditText mPasswordView;

    private LoginContract.Presenter mPresenter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        CredentialStoreImpl credentialStore = new CredentialStoreImpl(this);
        new LoginPresenter(this,
                UserServicesAdapter.getInstance(credentialStore),
                credentialStore,
                SchedulerProvider.getInstance());
    }

    @Override
    public void setPresenter(LoginContract.Presenter presenter) {
        mPresenter = checkNotNull(presenter);
    }

    @Override
    public void showForm() {
        mMultiView.setActiveView(R.id.login_form);
    }

    @Override
    public void showProgress() {
        mMultiView.setActiveView(R.id.login_progress);
    }

    @Override
    public void showEmailError() {
        mEmailView.setError(getString(R.string.error_field_required));
    }

    @Override
    public void showPasswordError() {
        mPasswordView.setError(getString(R.string.error_field_required));
    }

    @Override
    public void clearErrors() {
        mPasswordView.setError(null);
        mEmailView.setError(null);
    }

    @OnClick(R.id.email_sign_in_button)
    public void onLoginClicked() {
        mPresenter.onCredentialsSubmitted(mEmailView.getText().toString(), mPasswordView.getText().toString());
    }
    @Override
    public void goToUserDetails() {
        startActivity(new Intent(this, UserActivity.class));
        finish();
    }

    @Override
    public void onStart() {
        super.onStart();
        mPresenter.subscribe();
    }

    @Override
    public void onStop() {
        mPresenter.unsubscribe();
        super.onStop();
    }
}
