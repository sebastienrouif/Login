package uk.co.frips.sample.login;

import uk.co.frips.sample.login.base.BasePresenter;
import uk.co.frips.sample.login.base.BaseView;

/**
 * This specifies the contract between the view and the presenter.
 */
public interface UserContract {

    interface View extends BaseView<Presenter> {

        void showUserDetails();

        void showProgress();

        void showAvatarProgress(boolean progress);

        void setEmail(String email);

        void setPassword(String password);

        void setAvatar(String base64Image);

        void goToLogin();
    }

    interface Presenter extends BasePresenter {

        void onAvatarClicked();

        void onAvatarUpdated(String base64);

    }
}
