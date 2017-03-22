package uk.co.frips.sample.login.widget;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

public class MultiView extends FrameLayout {

    static class SavedState extends BaseSavedState {
        int activeViewIndex;
        int activeViewId;

        SavedState(Parcelable superState) {
            super(superState);
        }

        private SavedState(Parcel in) {
            super(in);
            activeViewIndex = in.readInt();
            activeViewId = in.readInt();
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeInt(activeViewIndex);
            out.writeInt(activeViewId);
        }

        public static final Creator<SavedState> CREATOR =
                new Creator<SavedState>() {
                    public SavedState createFromParcel(Parcel in) {
                        return new SavedState(in);
                    }

                    public SavedState[] newArray(int size) {
                        return new SavedState[size];
                    }
                };
    }

    private int mActiveViewIndex = -1;
    private int mActiveViewId = -1;

    public MultiView(Context context) {
        super(context);
        setSaveEnabled(true);
    }

    public MultiView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setSaveEnabled(true);
    }

    public void setActiveView(int id) {
        mActiveViewId = id;
        mActiveViewIndex = findChildIndexById(id);

        setActiveView();
    }

    private int findChildIndexById(int id) {
        for(int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            if(child.getId() == id) {
                return i;
            }
        }

        return -1;
    }

    public boolean isActiveView(int id) {
        return id == mActiveViewId;
    }

    protected void setActiveView() {
        for (int i = 0; i < getChildCount(); i++) {
            View view = getChildAt(i);

            if (mActiveViewIndex == i) {
                view.setVisibility(View.VISIBLE);
            } else {
                view.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        SavedState state = new SavedState(superState);

        state.activeViewIndex = mActiveViewIndex;
        state.activeViewId = mActiveViewId;

        return state;
    }

    @Override
    public void onRestoreInstanceState(Parcelable p) {
        SavedState state = (SavedState) p;

        super.onRestoreInstanceState(state.getSuperState());

        if (mActiveViewIndex == -1) {
            mActiveViewIndex = state.activeViewIndex;
            mActiveViewId = state.activeViewId;
        }

        setActiveView();
    }

}
