package uk.co.frips.sample.login.base;

import android.os.Bundle;
import android.support.annotation.StringRes;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import butterknife.ButterKnife;

public abstract class BaseFragment  extends Fragment {

    abstract protected int getLayoutResourceId();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // For Calligraphy to work: get the activity layout inflater because the support library has "broken" the fragment layout inflater
        View rootView = getActivity().getLayoutInflater().inflate(getLayoutResourceId(), container, false);

        injectViews(rootView);

        postCreateView(rootView);

        return rootView;
    }

    protected void injectViews(View rootView) {
        ButterKnife.bind(this, rootView);
    }

    protected void postCreateView(View rootView) {
    }

    @StringRes
    public abstract int getTitle();
}
