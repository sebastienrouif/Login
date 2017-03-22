package uk.co.frips.sample.login;

import uk.co.frips.sample.login.base.BasePresenter;
import uk.co.frips.sample.login.base.BaseView;

/**
 * This specifies the contract between the view and the presenter.
 */
public interface LoginContract {

    interface View extends BaseView<Presenter> {

        void showForm();

        void showProgress();

        void showEmailError();

        void showPasswordError();

        void clearErrors();

        void goToUserDetails();
    }

    interface Presenter extends BasePresenter {

        void onCredentialsSubmitted(String login, String password);

    }
}
