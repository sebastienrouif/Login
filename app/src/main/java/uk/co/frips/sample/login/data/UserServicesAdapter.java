package uk.co.frips.sample.login.data;

import android.util.Log;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import rx.subjects.BehaviorSubject;
import uk.co.frips.sample.login.data.entity.Avatar;
import uk.co.frips.sample.login.data.entity.Credentials;
import uk.co.frips.sample.login.data.entity.SessionResponse;
import uk.co.frips.sample.login.data.entity.User;

public class UserServicesAdapter {
    private static UserServicesAdapter INSTANCE;

    private static final String TAG = UserServicesAdapter.class.getSimpleName();
    private static final String ROOT_URL = "https://farepilottest.herokuapp.com/";
    private final UserService mUserService;
    private final BehaviorSubject<SessionResponse> mSessionResponseBehaviorSubject;
    private final BehaviorSubject<User> mUserBehaviorSubject;
    private Credentials mCredentials;
    private SessionResponse mSessionResponse;
    private User mUser;
    private CredentialStore mCredentialStore;

    public static UserServicesAdapter getInstance(CredentialStore credentialStore) {
        if (INSTANCE == null) {
            INSTANCE = new UserServicesAdapter(credentialStore);
        }
        return INSTANCE;
    }


    UserServicesAdapter(CredentialStore credentialStore) {
        mCredentialStore = credentialStore;
        Interceptor authenticationInterceptor = new Interceptor() {

            @Override
            public okhttp3.Response intercept(Chain chain) throws IOException {
                Request originalRequest = chain.request();
                Request.Builder builder = originalRequest.newBuilder();
                Log.d(TAG, "sessionToken : " + getSessionToken());
                if (getSessionToken() != null) {
                    builder.header("Authorization:Bearer ", getSessionToken()).
                            method(originalRequest.method(), originalRequest.body());
                }
                okhttp3.Response response = chain.proceed(builder.build());
                /*
                implies that the token has expired
                or was never initialized
                 */
                if (response.code() == 401) {
                    Log.d(TAG, "Token Expired");
                    getAuthenticationToken();
                    builder = originalRequest.newBuilder().header("Authorization:Bearer ", getSessionToken()).
                            method(originalRequest.method(), originalRequest.body());
                    response = chain.proceed(builder.build());
                }
                return response;
            }
        };
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(authenticationInterceptor)
                .addInterceptor(loggingInterceptor)
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .baseUrl(ROOT_URL)
                .build();

        mUserService = retrofit.create(UserService.class);
        SessionResponse sessionResponse = null;
        mSessionResponseBehaviorSubject = BehaviorSubject.create(sessionResponse);
        User user = null;
        mUserBehaviorSubject = BehaviorSubject.create(user);
        mCredentials = credentialStore.getCredentials();
    }


    public void updateCredentials(Credentials credentials) {
        mCredentialStore.saveCredentials(credentials);
        mCredentials = credentials;
    }

    public Credentials getCredentials() {
        return mCredentials;
    }

    public void updateSession(SessionResponse sessionResponse) {
        mSessionResponse = sessionResponse;
        mSessionResponseBehaviorSubject.onNext(sessionResponse);
    }

    public Observable<SessionResponse> getSessionObservable() {
        return mSessionResponseBehaviorSubject;
    }

    public void updateUser(User user) {
        mUser = user;
        mUserBehaviorSubject.onNext(mUser);
    }

    public void updateUserAvatar(Avatar avatar) {
        mUser.setAvatar(avatar.getAvatar());
        mUserBehaviorSubject.onNext(mUser);
    }

    public Observable<User> getUserObservable() {
        return mUserBehaviorSubject;
    }

    private String getSessionToken() {
        if (mSessionResponse == null) return null;
        return mSessionResponse.getToken();
    }

    public Observable<Void> login(final Credentials credentials) {
        Observable<SessionResponse> responseObservable = mUserService.login(credentials);

        Observable<Void> voidObservable = responseObservable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(new Func1<SessionResponse, Void>() {
                    @Override
                    public Void call(SessionResponse sessionResponse) {
                        updateCredentials(credentials);
                        updateSession(sessionResponse);
                        return null;
                    }
                });
        return voidObservable;
    }

    public Observable<Void> getUser(String userId) {
        Observable<User> responseObservable = mUserService.getUser(userId);
        Observable<Void> voidObservable = responseObservable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(new Func1<User, Void>() {
                    @Override
                    public Void call(User user) {
                        updateUser(user);
                        return null;
                    }
                });
        return voidObservable;
    }

    public Observable<Void> updateAvatar(String base64) {
        Observable<Avatar> responseObservable = mUserService.postAvatar(mSessionResponse.getUserid(), new Avatar(base64));
        Observable<Void> voidObservable = responseObservable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(new Func1<Avatar, Void>() {
                    @Override
                    public Void call(Avatar avatar) {
                        updateUserAvatar(avatar);
                        Log.d(TAG, "avatar updated");
                        return null;
                    }
                });
        return voidObservable;
    }

    private void getAuthenticationToken() {
        Call<SessionResponse> authRequestCall = mUserService.loginSync(mCredentials);
        Response<SessionResponse> response = null;
        try {
            response = authRequestCall.execute();
            if (response.isSuccessful()) {
                updateSession(response.body());
            }
        } catch (IOException e) {
            Log.e(TAG, "Exception occurred due to ", e);
        }
    }
}
