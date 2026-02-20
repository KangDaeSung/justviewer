package com.kds3393.just.viewer.View;

import android.content.Context;
import android.graphics.Point;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;

import com.common.utils.LayoutUtils;
import com.common.utils.ResManager;
import com.common.utils.Size;
import com.kds3393.just.viewer.Config.SettingImageViewer;
import com.kds3393.just.viewer.Config.SharedPrefHelper;
import com.kds3393.just.viewer.R;

import static com.kds3393.just.viewer.View.MoveScaleButton.MODE_FUNCTION;

public class PageControlView extends RelativeLayout {
    private static final String TAG = "PageControlView";

    private MoveScaleButton mPrevPageBtn;
    private MoveScaleButton mNextPageBtn;
    private ResizeButton mPrevResizeBtn;
    private ResizeButton mNextResizeBtn;
    private Button mEditModeFinish;

    private boolean mMoveButtonEditMode = false;;

    private OnPageControlListener mOnPageControlListener;
    public interface OnPageControlListener {
        public void onPrev();
        public void onNext();
    }
    public void setOnPageControlListener(OnPageControlListener l) {
        mOnPageControlListener = l;
    }

    public PageControlView(Context context) {
        super(context);
        init(context);
    }

    public PageControlView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        inflate(context, R.layout.v_page_control, this);

        if (!SettingImageViewer.getUsePageMoveBtn(getContext())) {
            setVisibility(View.GONE);
            return;
        }

        Size prevSize = SharedPrefHelper.getImageLeftBtnSize(getContext());
        Point prevPoint = SharedPrefHelper.getImageLeftBtnPoint(getContext());
        mPrevPageBtn = (MoveScaleButton) findViewById(R.id.page_move_prev);
        LayoutUtils.setRelativeLayoutParams(mPrevPageBtn, prevSize.Width, prevSize.Height, prevPoint.x, prevPoint.y, -1);
        mPrevPageBtn.setText("이전 페이지");
        mPrevPageBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                if (mOnPageControlListener != null) {
                    mOnPageControlListener.onPrev();
                }
            }
        });


        Size nextSize = SharedPrefHelper.getImageRightBtnSize(getContext());
        Point nextPoint = SharedPrefHelper.getImageRightBtnPoint(getContext());
        mNextPageBtn = (MoveScaleButton) findViewById(R.id.page_move_next);
        LayoutUtils.setRelativeLayoutParams(mNextPageBtn, nextSize.Width, nextSize.Height, nextPoint.x, nextPoint.y, -1);
        mNextPageBtn.setText("다음 페이지");
        mNextPageBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                if (mOnPageControlListener != null) {
                    mOnPageControlListener.onNext();
                }
            }
        });

        mPrevPageBtn.setBoundView(mNextPageBtn);
        mNextPageBtn.setBoundView(mPrevPageBtn);
        int resizeBtnSize = ResManager.getDimen(R.dimen.imageviewer_page_move_btn_resize_size);
        int x = (int) (prevPoint.x + prevSize.Width) - resizeBtnSize;
        int y = (int) (prevPoint.y + prevSize.Height) - resizeBtnSize;
        mPrevResizeBtn = (ResizeButton) findViewById(R.id.page_move_prev_resize);
        LayoutUtils.setRelativeLayoutParams(mPrevResizeBtn, resizeBtnSize, resizeBtnSize, x, y, -1);
        mPrevResizeBtn.setScaleButton(mPrevPageBtn);
        mPrevResizeBtn.setMaxScaleSize(Size.DisplayWidth, Size.DisplayHeight);

        x = (int) (nextPoint.x + nextSize.Width) - resizeBtnSize;
        y = (int) (nextPoint.y + nextSize.Height) - resizeBtnSize;
        mNextResizeBtn = (ResizeButton) findViewById(R.id.page_move_next_resize);
        LayoutUtils.setRelativeLayoutParams(mNextResizeBtn, resizeBtnSize, resizeBtnSize, x, y, -1);
        mNextResizeBtn.setScaleButton(mNextPageBtn);
        mNextResizeBtn.setMaxScaleSize(Size.DisplayWidth, Size.DisplayHeight);

        mEditModeFinish = (Button) findViewById(R.id.page_edit_close);
        mEditModeFinish.setText("설정 완료");
        mEditModeFinish.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mMoveButtonEditMode = false;
                mPrevPageBtn.setMoveMode(MODE_FUNCTION);
                mNextPageBtn.setMoveMode(MODE_FUNCTION);
                mEditModeFinish.setVisibility(View.INVISIBLE);
                SharedPrefHelper.setImageLeftBtnPoint(getContext(), mPrevPageBtn.getLeft(), mPrevPageBtn.getTop());
                SharedPrefHelper.setImageRightBtnPoint(getContext(), mNextPageBtn.getLeft(), mNextPageBtn.getTop());
                SharedPrefHelper.setImageLeftBtnSize(getContext(), mPrevPageBtn.getWidth(), mPrevPageBtn.getHeight());
                SharedPrefHelper.setImageRightBtnSize(getContext(), mNextPageBtn.getWidth(), mNextPageBtn.getHeight());
            }
        });
    }

    public void setViewer(View viewer) {
        if (!SettingImageViewer.getUsePageMoveBtn(getContext())) {
            return;
        }
        mPrevPageBtn.setFunctionView(viewer, mPrevResizeBtn);
        mNextPageBtn.setFunctionView(viewer, mNextResizeBtn);

        mPrevPageBtn.setMoveMode(MODE_FUNCTION);
        mNextPageBtn.setMoveMode(MODE_FUNCTION);
    }

    public void setMode(int type) {
        if (!SettingImageViewer.getUsePageMoveBtn(getContext())) {
            return;
        }
        mPrevPageBtn.setMoveMode(type);
        mNextPageBtn.setMoveMode(type);
        if (type == MODE_FUNCTION) {
            mPrevResizeBtn.setViewPoint(mPrevPageBtn.getWidth(), mPrevPageBtn.getHeight(), mPrevPageBtn.getLeft(), mPrevPageBtn.getTop());
            mNextResizeBtn.setViewPoint(mNextPageBtn.getWidth(), mNextPageBtn.getHeight(), mNextPageBtn.getLeft(), mNextPageBtn.getTop());
        }
    }
    public void setMoveButtonEditMode(boolean editmode) {
        mMoveButtonEditMode = editmode;
        if (mMoveButtonEditMode) {
            mPrevPageBtn.setMoveMode(MoveScaleButton.MODE_EDIT);
            mNextPageBtn.setMoveMode(MoveScaleButton.MODE_EDIT);
            mEditModeFinish.setVisibility(View.VISIBLE);
        } else {

        }
    }

    public boolean isMoveButtonEditMode() {
        return mMoveButtonEditMode;
    }

    public void setSavePref() {
        if (mPrevPageBtn != null) {
            SharedPrefHelper.setImageLeftBtnPoint(getContext(), mPrevPageBtn.getLeft(),mPrevPageBtn.getTop());
            SharedPrefHelper.setImageRightBtnPoint(getContext(), mNextPageBtn.getLeft(),mNextPageBtn.getTop());
            SharedPrefHelper.setImageLeftBtnSize(getContext(), mPrevPageBtn.getWidth(),mPrevPageBtn.getHeight());
            SharedPrefHelper.setImageRightBtnSize(getContext(), mNextPageBtn.getWidth(),mNextPageBtn.getHeight());
        }
    }
}
