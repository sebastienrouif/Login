package uk.co.frips.sample.login;

import android.text.TextUtils;

import rx.functions.Action1;
import rx.subscriptions.CompositeSubscription;
import uk.co.frips.sample.login.data.CredentialStore;
import uk.co.frips.sample.login.data.UserServicesAdapter;
import uk.co.frips.sample.login.data.entity.Credentials;
import uk.co.frips.sample.login.data.entity.SessionResponse;
import uk.co.frips.sample.login.schedulers.BaseSchedulerProvider;

public class LoginPresenter implements LoginContract.Presenter {
    private final static String TAG = LoginPresenter.class.getSimpleName();

    private final CompositeSubscription mSubscriptions;
    private final LoginContract.View mView;
    private CredentialStore mCredentialStore;
    private final BaseSchedulerProvider mSchedulerProvider;
    private final UserServicesAdapter mUserServicesAdapter;

    public LoginPresenter(LoginContract.View view,
                          UserServicesAdapter userServicesAdapter,
                          CredentialStore credentialStore,
                          BaseSchedulerProvider schedulerProvider) {
        mView = view;
        mCredentialStore = credentialStore;
        mSchedulerProvider = schedulerProvider;
        mSubscriptions = new CompositeSubscription();
        mUserServicesAdapter = userServicesAdapter;
        view.setPresenter(this);
    }

    @Override
    public void subscribe() {
        mSubscriptions.add(mUserServicesAdapter.getSessionObservable().subscribe(new Action1<SessionResponse>() {
            @Override
            public void call(SessionResponse sessionResponse) {
                if (sessionResponse == null) {
                    mView.showForm();
                } else {
                    mView.goToUserDetails();
                }
            }
        }));

        Credentials credentials = mCredentialStore.getCredentials();
        if (credentials != null) {
            login(credentials);
        }
    }

    @Override
    public void unsubscribe() {
        mSubscriptions.clear();
    }

    @Override
    public void onCredentialsSubmitted(String email, String password) {
        //TODO create better validation
        if (TextUtils.isEmpty(email)) {
            mView.showEmailError();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            mView.showPasswordError();
            return;
        }

        login(new Credentials(email, password));
    }

    private void login(Credentials credentials) {

        mView.showProgress();
        mSubscriptions.add(mUserServicesAdapter.login(credentials)
                .subscribeOn(mSchedulerProvider.io())
                .observeOn(mSchedulerProvider.ui())
                .subscribe(new Action1<Void>() {
                    @Override
                    public void call(Void aVoid) {

                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        //TODO create more precise error
                        mView.showEmailError();
                        mView.showPasswordError();
                        mView.showForm();
                    }
                }));

    }


}
