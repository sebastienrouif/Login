package uk.co.frips.sample.login;

import android.util.Log;

import retrofit2.adapter.rxjava.HttpException;
import rx.functions.Action1;
import rx.subscriptions.CompositeSubscription;
import uk.co.frips.sample.login.data.CredentialStore;
import uk.co.frips.sample.login.data.UserServicesAdapter;
import uk.co.frips.sample.login.data.entity.SessionResponse;
import uk.co.frips.sample.login.data.entity.User;
import uk.co.frips.sample.login.schedulers.BaseSchedulerProvider;

public class UserPresenter implements UserContract.Presenter {
    private static final String TAG = UserPresenter.class.getSimpleName();
    public static final int UNAUTHORISED = 401;

    private final CompositeSubscription mSubscriptions;
    private final UserContract.View mView;
    private final CredentialStore mCredentialStore;
    private final BaseSchedulerProvider mSchedulerProvider;
    private final UserServicesAdapter mUserServicesAdapter;

    public UserPresenter(UserContract.View view,
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
                    Log.d(TAG, "sessionResponse null");
                    mView.goToLogin();
                } else {
                    Log.d(TAG, "sessionResponse " + sessionResponse.toString());
                    mView.setEmail(mUserServicesAdapter.getCredentials().getEmail());
                    mView.setPassword(mUserServicesAdapter.getCredentials().getPassword());
                    mView.showAvatarProgress(true);
                    mView.showUserDetails();
                    subscribeForUserDetails(sessionResponse.getUserid());
                }
            }
        }));


    }

    private void subscribeForUserDetails(final String userId) {
        mSubscriptions.add(mUserServicesAdapter.getUserObservable().subscribe(new Action1<User>() {
            @Override
            public void call(User user) {
                if (user == null) {
                    Log.d(TAG, "subscribeForUserDetails null");
                    getUserDetails(userId);
                } else {
                    Log.d(TAG, "subscribeForUserDetails " + user.toString());
                    if (user.getAvatar() != null) {
                        mView.setAvatar(user.getAvatar());
                    }
                    mView.showAvatarProgress(false);
                }
            }
        }));
    }

    private void getUserDetails(String userId) {
        mSubscriptions.add(mUserServicesAdapter.getUser(userId)
                .subscribeOn(mSchedulerProvider.io())
                .observeOn(mSchedulerProvider.ui())
                .subscribe(new Action1<Void>() {
                    @Override
                    public void call(Void aVoid) {

                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        handleNetworkException(throwable);
                    }
                }));
    }

    @Override
    public void unsubscribe() {
        mSubscriptions.clear();
    }

    @Override
    public void onAvatarClicked() {

    }

    @Override
    public void onAvatarUpdated(String base64) {
        mView.showAvatarProgress(true);
        mSubscriptions.add(mUserServicesAdapter.updateAvatar(base64)
                .subscribeOn(mSchedulerProvider.io())
                .observeOn(mSchedulerProvider.ui())
                .subscribe(new Action1<Void>() {
                    @Override
                    public void call(Void aVoid) {
                        Log.d(TAG, "avatar updated");
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        handleNetworkException(throwable);
                        Log.d(TAG, "avatar error");
                    }
                }));
    }

    private void handleNetworkException(Throwable throwable) {
        if (throwable instanceof HttpException) {
            HttpException exception = (HttpException) throwable;
            if (exception.code() == UNAUTHORISED) {
                mUserServicesAdapter.updateCredentials(null);
                mView.goToLogin();
            } else {
                Log.d(TAG, "HttpException" + throwable.toString());
            }
        } else {
            Log.d(TAG, "handleNetworkException" + throwable.toString());
        }
    }
}
