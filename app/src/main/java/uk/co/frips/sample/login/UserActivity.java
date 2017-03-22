package uk.co.frips.sample.login;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import butterknife.BindView;
import butterknife.OnClick;
import uk.co.frips.sample.login.base.BaseActivity;
import uk.co.frips.sample.login.data.CredentialStoreImpl;
import uk.co.frips.sample.login.data.UserServicesAdapter;
import uk.co.frips.sample.login.schedulers.SchedulerProvider;
import uk.co.frips.sample.login.utils.ImageUtils;
import uk.co.frips.sample.login.widget.MultiView;

import static com.google.common.base.Preconditions.checkNotNull;


public class UserActivity extends BaseActivity implements UserContract.View, ImageUtils.ImageAttachmentListener {
    static final int REQUEST_IMAGE_CAPTURE = 1;

    @BindView(R.id.user_multi_view) MultiView mMultiView;
    @BindView(R.id.user_email) EditText mEmailView;
    @BindView(R.id.user_password) EditText mPasswordView;
    @BindView(R.id.user_avatar) ImageView mAvatarView;
    @BindView(R.id.avatar_progress) View mAvatarProgress;

    private UserContract.Presenter mPresenter;
    private ImageUtils mImageUtils;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);
        mImageUtils = new ImageUtils(this);
        CredentialStoreImpl credentialStore = new CredentialStoreImpl(this);
        new UserPresenter(this,
                UserServicesAdapter.getInstance(credentialStore),
                credentialStore,
                SchedulerProvider.getInstance());
    }

    @Override
    public void setPresenter(UserContract.Presenter presenter) {
        mPresenter = checkNotNull(presenter);
    }

    @Override
    public void onStart() {
        Log.d("UserActivity", "onStart");
        super.onStart();
        mPresenter.subscribe();
    }

    @Override
    public void onStop() {
        mPresenter.unsubscribe();
        super.onStop();
    }

    @Override
    public void showUserDetails() {
        mMultiView.setActiveView(R.id.user_details);
    }

    @Override
    public void showProgress() {
        mMultiView.setActiveView(R.id.user_progress);
    }

    public void showAvatarProgress(boolean progress) {
        mAvatarProgress.setVisibility(progress ? View.VISIBLE : View.GONE);
    }

    @Override
    public void setEmail(String email) {
        mEmailView.setText(email);
    }

    @Override
    public void setPassword(String password) {
        mPasswordView.setText(password);
    }

    @Override
    public void setAvatar(String base64Image) {
        byte[] decodedString = Base64.decode(base64Image, Base64.DEFAULT);
        Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
        mAvatarView.setImageBitmap(decodedByte);
    }

    @OnClick(R.id.user_avatar)
    public void onAvatarClicked() {
        mImageUtils.imagepicker(REQUEST_IMAGE_CAPTURE);
        mPresenter.onAvatarClicked();
    }

    @Override
    public void goToLogin() {
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mImageUtils.onActivityResult(requestCode, resultCode, data);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        mImageUtils.request_permission_result(requestCode, permissions, grantResults);
    }

    @Override
    public void image_attachment(int from, String filename, Bitmap bitmap, Uri uri) {
        mAvatarView.setImageBitmap(bitmap);
        mPresenter.onAvatarUpdated(mImageUtils.BitMapToString(bitmap));
    }
}
