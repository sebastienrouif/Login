package uk.co.frips.sample.login.base;

import android.support.annotation.LayoutRes;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;

import butterknife.ButterKnife;

public abstract class BaseActivity extends AppCompatActivity {

    @Override
    public void setContentView(@LayoutRes int layoutResID) {
        super.setContentView(layoutResID);
        injectViews();
    }

    @Override
    public void setContentView(View view) {
        super.setContentView(view);
        injectViews();
    }


    @Override
    public void setContentView(View view, ViewGroup.LayoutParams params) {
        super.setContentView(view, params);
        injectViews();
    }

    protected void injectViews() {
        ButterKnife.bind(this);
    }
}
