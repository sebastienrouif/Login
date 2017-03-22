package uk.co.frips.sample.login;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;

import okhttp3.MediaType;
import okhttp3.ResponseBody;
import okio.BufferedSource;
import retrofit2.Response;
import retrofit2.adapter.rxjava.HttpException;
import rx.Observable;
import uk.co.frips.sample.login.data.CredentialStore;
import uk.co.frips.sample.login.data.UserServicesAdapter;
import uk.co.frips.sample.login.data.entity.Credentials;
import uk.co.frips.sample.login.data.entity.SessionResponse;
import uk.co.frips.sample.login.data.entity.User;
import uk.co.frips.sample.login.schedulers.ImmediateSchedulerProvider;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21)
public class UserPresenterTest {
    public static final String USER_ID = "userId";
    public static final String AVATAR = "avatar";
    public static final String PASSWORD = "password";
    public static final String TOKEN = "token";

    @Mock private UserContract.View mView;
    @Mock private UserServicesAdapter mUserServicesAdapter;
    @Mock private CredentialStore mCredentialStore;
    private UserPresenter mUserPresenter;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        ImmediateSchedulerProvider schedulerProvider = new ImmediateSchedulerProvider();

        mUserPresenter = new UserPresenter(mView, mUserServicesAdapter, mCredentialStore, schedulerProvider);
    }

    @Test
    public void subscribe_without_session() throws Exception {
        //arrange
        SessionResponse sessionResponse = null;
        when(mUserServicesAdapter.getSessionObservable()).thenReturn(Observable.just(sessionResponse));

        //act
        mUserPresenter.subscribe();

        //assert
        verify(mView).goToLogin();
    }

    @Test
    public void subscribe_with_session_shows_user_details() throws Exception {
        //arrange
        SessionResponse sessionResponse = new SessionResponse(USER_ID, TOKEN);
        when(mUserServicesAdapter.getSessionObservable()).thenReturn(Observable.just(sessionResponse));
        Credentials credentials = new Credentials(USER_ID, PASSWORD);
        when(mUserServicesAdapter.getCredentials()).thenReturn(credentials);
        User user = new User(USER_ID, AVATAR);
        when(mUserServicesAdapter.getUserObservable()).thenReturn(Observable.just(user));

        //act
        mUserPresenter.subscribe();

        //assert
        verify(mView).setEmail(USER_ID);
        verify(mView).setPassword(PASSWORD);
        verify(mView).showAvatarProgress(true);
        verify(mView).showUserDetails();
    }

    @Test
    public void subscribe_with_session_shows_avatar() throws Exception {
        //arrange
        SessionResponse sessionResponse = new SessionResponse(USER_ID, TOKEN);
        when(mUserServicesAdapter.getSessionObservable()).thenReturn(Observable.just(sessionResponse));
        Credentials credentials = new Credentials(USER_ID, PASSWORD);
        when(mUserServicesAdapter.getCredentials()).thenReturn(credentials);
        User user = new User(USER_ID, AVATAR);
        when(mUserServicesAdapter.getUserObservable()).thenReturn(Observable.just(user));

        //act
        mUserPresenter.subscribe();

        //assert
        verify(mView).setAvatar(AVATAR);
        verify(mView).showAvatarProgress(false);
    }

    @Test
    public void subscribe_with_session_does_not_show_avatar_if_null() throws Exception {
        //arrange
        SessionResponse sessionResponse = new SessionResponse(USER_ID, TOKEN);
        when(mUserServicesAdapter.getSessionObservable()).thenReturn(Observable.just(sessionResponse));
        Credentials credentials = new Credentials(USER_ID, PASSWORD);
        when(mUserServicesAdapter.getCredentials()).thenReturn(credentials);
        User user = new User(USER_ID, null);
        when(mUserServicesAdapter.getUserObservable()).thenReturn(Observable.just(user));

        //act
        mUserPresenter.subscribe();

        //assert
        verify(mView, never()).setAvatar(anyString());
        verify(mView).showAvatarProgress(false);
    }


    @Test
    public void subscribe_with_session_fetches_user_if_null() throws Exception {
        //arrange
        SessionResponse sessionResponse = new SessionResponse(USER_ID, TOKEN);
        when(mUserServicesAdapter.getSessionObservable()).thenReturn(Observable.just(sessionResponse));
        Credentials credentials = new Credentials(USER_ID, PASSWORD);
        when(mUserServicesAdapter.getCredentials()).thenReturn(credentials);
        User user = null;
        when(mUserServicesAdapter.getUserObservable()).thenReturn(Observable.just(user));

        when(mUserServicesAdapter.getUser(anyString())).thenReturn(Observable.<Void>just(null));

        //act
        mUserPresenter.subscribe();

        //assert
        verify(mView, never()).setAvatar(anyString());
        verify(mView).showAvatarProgress(true);
    }


    @Test
    public void onAvatarUpdated_shows_progress() throws Exception {
        //arrange
        when(mUserServicesAdapter.updateAvatar(anyString())).thenReturn(Observable.<Void>just(null));

        //act
        mUserPresenter.onAvatarUpdated(anyString());

        //assert
        verify(mView, never()).setAvatar(anyString());
        verify(mView).showAvatarProgress(true);
    }

    @Test
    public void onAvatarUpdated_with_credential_error_goes_to_login() throws Exception {
        //arrange
        when(mUserServicesAdapter.updateAvatar(anyString())).thenReturn(Observable.<Void>error(createHttpError(401)));

        //act
        mUserPresenter.onAvatarUpdated(anyString());

        //assert
        verify(mUserServicesAdapter).updateCredentials(null);
        verify(mView).goToLogin();
    }

    @Test
    public void onAvatarUpdated_with_any_does_nothing() throws Exception {
        //arrange
        when(mUserServicesAdapter.updateAvatar(anyString())).thenReturn(Observable.<Void>error(createHttpError(404)));

        //act
        mUserPresenter.onAvatarUpdated(anyString());

        //assert
        verify(mUserServicesAdapter, never()).updateCredentials(null);
        verify(mView, never()).goToLogin();
    }

    private HttpException createHttpError(int code) {
        Response<Object> error = Response.error(code, new ResponseBody() {
            @Override
            public MediaType contentType() {
                return null;
            }

            @Override
            public long contentLength() {
                return 0;
            }

            @Override
            public BufferedSource source() {
                return null;
            }
        });
        return new HttpException(error);
    }

}