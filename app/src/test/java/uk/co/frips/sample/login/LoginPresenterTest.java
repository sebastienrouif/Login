package uk.co.frips.sample.login;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;

import rx.Observable;
import uk.co.frips.sample.login.data.CredentialStore;
import uk.co.frips.sample.login.data.UserServicesAdapter;
import uk.co.frips.sample.login.data.entity.Credentials;
import uk.co.frips.sample.login.data.entity.SessionResponse;
import uk.co.frips.sample.login.schedulers.ImmediateSchedulerProvider;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21)
public class LoginPresenterTest {
    public static final String USER_ID = "userId";
    public static final String PASSWORD = "password";

    @Mock private LoginContract.View mLoginView;
    @Mock private UserServicesAdapter mUserServicesAdapter;
    @Mock private CredentialStore mCredentialStore;
    private LoginPresenter mLoginPresenter;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        ImmediateSchedulerProvider schedulerProvider = new ImmediateSchedulerProvider();

        mLoginPresenter = new LoginPresenter(mLoginView, mUserServicesAdapter, mCredentialStore, schedulerProvider);

    }

    @Test
    public void subscribe_without_session_details() throws Exception {
        //arrange
        SessionResponse sessionResponse = null;
        when(mUserServicesAdapter.getSessionObservable()).thenReturn(Observable.just(sessionResponse));

        //act
        mLoginPresenter.subscribe();

        //assert
        verify(mLoginView).showForm();
    }

    @Test
    public void subscribe_with_session_details() throws Exception {
        //arrange
        SessionResponse sessionResponse = new SessionResponse(USER_ID, PASSWORD);
        when(mUserServicesAdapter.getSessionObservable()).thenReturn(Observable.just(sessionResponse));

        //act
        mLoginPresenter.subscribe();

        //assert
        verify(mLoginView).goToUserDetails();
    }

    @Test
    public void subscribe_with_credentials() throws Exception {
        //arrange
        SessionResponse sessionResponse = null;
        Credentials credentials = new Credentials(USER_ID, PASSWORD);
        when(mCredentialStore.getCredentials()).thenReturn(credentials);
        when(mUserServicesAdapter.getSessionObservable()).thenReturn(Observable.just(sessionResponse));
        when(mUserServicesAdapter.login(any(Credentials.class))).thenReturn(Observable.<Void>just(null));

        //act
        mLoginPresenter.subscribe();

        //assert
        verify(mLoginView).showProgress();
        verify(mUserServicesAdapter).login(credentials);
    }

    @Test
    public void subscribe_with_credentials_and_api_error() throws Exception {
        //arrange
        SessionResponse sessionResponse = null;
        Credentials credentials = new Credentials(USER_ID, PASSWORD);
        when(mCredentialStore.getCredentials()).thenReturn(credentials);
        when(mUserServicesAdapter.getSessionObservable()).thenReturn(Observable.just(sessionResponse));
        when(mUserServicesAdapter.login(any(Credentials.class))).thenReturn(Observable.<Void>error(new RuntimeException("an error")));

        //act
        mLoginPresenter.subscribe();

        //assert
        verify(mLoginView).showEmailError();
        verify(mLoginView).showPasswordError();
        verify(mLoginView, atLeastOnce()).showForm();
    }

    @Test
    public void onCredentialsSubmitted_with_wrong_password_and_email() throws Exception {
        //arrange

        //act
        mLoginPresenter.onCredentialsSubmitted(null, null);

        //assert
        verify(mLoginView, never()).showProgress();
        verify(mLoginView).showEmailError();
        verify(mLoginView, never()).showPasswordError();
    }

    @Test
    public void onCredentialsSubmitted_with_wrong_password() throws Exception {
        //arrange

        //act
        mLoginPresenter.onCredentialsSubmitted(USER_ID, null);

        //assert
        verify(mLoginView, never()).showProgress();
        verify(mLoginView, never()).showEmailError();
        verify(mLoginView).showPasswordError();
    }

    @Test
    public void onCredentialsSubmitted_with_wrong_email() throws Exception {
        //arrange

        //act
        mLoginPresenter.onCredentialsSubmitted(null, null);

        //assert
        verify(mLoginView, never()).showProgress();
        verify(mLoginView).showEmailError();
        verify(mLoginView, never()).showPasswordError();
    }

    @Test
    public void onCredentialsSubmitted_with_correct_credentials() throws Exception {
        //arrange
        when(mUserServicesAdapter.login(any(Credentials.class))).thenReturn(Observable.<Void>just(null));

        //act
        mLoginPresenter.onCredentialsSubmitted(USER_ID, PASSWORD);

        //assert
        ArgumentCaptor<Credentials> captor = ArgumentCaptor.forClass(Credentials.class);
        verify(mLoginView).showProgress();
        verify(mUserServicesAdapter).login(captor.capture());
        assertEquals(USER_ID, captor.getValue().getEmail());
        assertEquals(PASSWORD, captor.getValue().getPassword());
    }

    @Test
    public void sonCredentialsSubmitted_with_correct_credentials_and_api_error() throws Exception {
        //arrange
        when(mUserServicesAdapter.login(any(Credentials.class))).thenReturn(Observable.<Void>error(new RuntimeException("an error")));

        //act
        mLoginPresenter.onCredentialsSubmitted(USER_ID, PASSWORD);

        //assert
        verify(mLoginView).showEmailError();
        verify(mLoginView).showPasswordError();
        verify(mLoginView).showForm();
    }
}