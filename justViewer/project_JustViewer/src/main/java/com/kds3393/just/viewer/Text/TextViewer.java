package com.kds3393.just.viewer.Text;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.TranslateAnimation;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.common.utils.Size;
import com.common.utils.debug.CLog;
import com.kds3393.just.viewer.Config.SettingTextViewer;
import com.kds3393.just.viewer.R;
import com.kds3393.just.viewer.Utils.CUtils;
import com.kds3393.just.viewer.provider.DBItemData;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class TextViewer extends RelativeLayout implements View.OnClickListener,TextSettingInterface {
	private static final String TAG = "TextViewer";
	private static int NONE = 0;
	private static int PREV = 1;
	private static int NEXT = 2;

    @BindView(R.id.textviewer) VTextListView textviewer;
    @BindView(R.id.textviewer_hide_btn) TextView textviewer_hide_btn;
    @BindView(R.id.textviewer_hide_cancel_btn) TextView textviewer_hide_cancel_btn;
    @BindView(R.id.textviewer_captureView) ImageView textviewer_captureView;

    private String mContentPath;
	
	private TranslateAnimation mAniLeftIn;
	private TranslateAnimation mAniLeftOut;
	private TranslateAnimation mAniRightIn;
	private TranslateAnimation mAniRightOut;
	private int mAniDuration = 500;
	private int mPageDirectionMode = SettingTextViewer.DIRECTION_VERTICAL;

    private OnItemClickListener mOnItemClickListener = null;

    private DBItemData mTextData;

    private boolean mIsHideMode = false;    //이미 본 구간을 안보이도록 설정하는 모드

    private int mHidePosition = -1;
    private View mPrevHideCheckView = null;

    private TextViewerActivity mActivity;
    public void setActivity(TextViewerActivity act) {
        mActivity = act;
    }
    public TextViewer(Context context,String path) {
        super(context);
        init(path);
    }

    public TextViewer(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(null);
    }

    public TextViewer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(null);
    }


    private void init(String path) {
        inflate(getContext(), R.layout.v_text_viewer, this);
        ButterKnife.bind(this);

		mContentPath = path;
		initAnimation();
		mPageDirectionMode = SettingTextViewer.getPageDirection(getContext());

		setBackgroundColor(SettingTextViewer.sColors[SettingTextViewer.getTextColor(getContext())][0]);

        textviewer.setOnScrollListener(new OnScrollListener(){
			private int mChangeBook = NONE;
			private boolean isFling = false;
			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,int visibleItemCount, int totalItemCount) {}
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				if (scrollState == OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
					if (textviewer.getLastVisiblePosition() == textArray.size() - 1) {
						mChangeBook = NEXT;
					} else if (textviewer.getFirstVisiblePosition() == 0){
						mChangeBook = PREV;
					}
				} else if (mChangeBook != NONE && scrollState == OnScrollListener.SCROLL_STATE_FLING){
					isFling = true;
				} else if (mChangeBook != NONE && isFling && scrollState == OnScrollListener.SCROLL_STATE_IDLE) {
					if (mChangeBook == NEXT && textviewer.getLastVisiblePosition() == textArray.size() - 1) {
						changeBook(true);
					} else if (mChangeBook == PREV && textviewer.getFirstVisiblePosition() == 0){
						changeBook(false);
					} 
					isFling = false;
					mChangeBook = NONE;
				}
			}
		});

        textviewer_hide_btn.setOnClickListener(this);
        textviewer_hide_cancel_btn.setOnClickListener(this);
	}
	
	public void setContentPath(String path) {
		mContentPath = path;
	}
	
	private void initAnimation() {
		mAniLeftIn = new TranslateAnimation(Animation.RELATIVE_TO_SELF,-1.0f,
				Animation.RELATIVE_TO_SELF,0.0f,
				Animation.RELATIVE_TO_SELF,0.0f,
				Animation.RELATIVE_TO_SELF,0.0f);
		mAniLeftIn.setDuration(mAniDuration);
		mAniLeftIn.setFillEnabled(true);
		mAniLeftIn.setAnimationListener(mAnimationListener);
		mAniLeftOut = new TranslateAnimation(Animation.RELATIVE_TO_SELF,0.0f,
				Animation.RELATIVE_TO_SELF,-1.0f,
				Animation.RELATIVE_TO_SELF,0.0f,
				Animation.RELATIVE_TO_SELF,0.0f);
		mAniLeftOut.setDuration(mAniDuration);
		mAniLeftOut.setFillEnabled(true);
		mAniLeftOut.setFillAfter(true);
		mAniLeftOut.setAnimationListener(mAnimationListener);
		mAniRightIn = new TranslateAnimation(Animation.RELATIVE_TO_SELF,1.0f,
				Animation.RELATIVE_TO_SELF,0.0f,
				Animation.RELATIVE_TO_SELF,0.0f,
				Animation.RELATIVE_TO_SELF,0.0f);
		mAniRightIn.setDuration(mAniDuration);
		mAniRightIn.setFillEnabled(true);
		mAniRightOut = new TranslateAnimation(Animation.RELATIVE_TO_SELF,0.0f,
				Animation.RELATIVE_TO_SELF,1.0f,
				Animation.RELATIVE_TO_SELF,0.0f,
				Animation.RELATIVE_TO_SELF,0.0f);
		mAniRightOut.setDuration(mAniDuration);
		mAniRightOut.setFillEnabled(true);
		mAniRightOut.setFillAfter(true);
		mAniRightOut.setAnimationListener(mAnimationListener);
	}

	private AnimationListener mAnimationListener = new AnimationListener(){
		@Override public void onAnimationRepeat(Animation arg0) {}
		@Override public void onAnimationStart(Animation arg0) {}
		@Override
		public void onAnimationEnd(Animation arg0) {
            textviewer_captureView.setVisibility(View.GONE);
		}
	};

	public void setTextData(DBItemData data) {
		mTextData = data;
	}
	
	private ContentReadFileAsyncTask mReadBookAsyncTask = null;
	public void runInitTask() {
        if (mReadBookAsyncTask != null) {
            mReadBookAsyncTask.mIsCancel = true;
            textArray.clear();
        }
        mReadBookAsyncTask = new ContentReadFileAsyncTask();
		mReadBookAsyncTask.execute(getDBHideLine());
	}
	
	public int getFirstVisiblePosition() {
		return textviewer.getFirstVisiblePosition();
	}
	
	public void setScrollSpeed(int speed) {
        textviewer.setScrollSpeed(speed);
	}

	public void setOnItemClickListener(OnItemClickListener l) {
        mOnItemClickListener = l;
        textviewer.setOnItemClickListener(l);
	}
	
	public void smoothScrollBy(int distance, int duration) {
        textviewer.smoothScrollBy(distance, duration);
	}
	
	public void movePrev() {
		if (textviewer.getFirstVisiblePosition() == 0) {
			changeBook(false);
		} else {
			if (mPageDirectionMode == SettingTextViewer.DIRECTION_VERTICAL) {
				moveScrollUp();
			} else {
				captureView();
                textviewer_captureView.startAnimation(mAniRightOut);
                textviewer_captureView.setVisibility(View.VISIBLE);

                textviewer.startAnimation(mAniLeftIn);
				this.smoothScrollBy(-Size.DisplayHeight, 1);
			}
		}
	}
	
	public void moveNext() {
		Log.e(TAG,"KDS3393_last = " + textviewer.getLastVisiblePosition() + " last line = " + textArray.size());
		if (textviewer.getLastVisiblePosition() == textArray.size() - 1) {
			changeBook(true);
		} else {
			if (mPageDirectionMode == SettingTextViewer.DIRECTION_VERTICAL) {
				moveScrollDown();
			} else {
				captureView();
                textviewer_captureView.startAnimation(mAniLeftOut);
                textviewer_captureView.setVisibility(View.VISIBLE);

				textviewer.startAnimation(mAniRightIn);
				this.smoothScrollBy(Size.DisplayHeight, 1);
			}
		}
	}

	public void setHideEditMode(boolean isHide) {
        mIsHideMode = isHide;
        if (mIsHideMode) {
            textviewer.setOnItemClickListener(new OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    int pos = position;

                    RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
                    params.leftMargin = view.getRight() - textviewer_hide_btn.getWidth();
                    int top = ((view.getBottom() - view.getTop()) - textviewer_hide_btn.getHeight()) / 2;
                    top += view.getTop();
                    params.topMargin = top;
                    textviewer_hide_btn.setLayoutParams(params);

                    if (mHidePosition == pos) {
                        return;
                    }
                    mHidePosition = pos;
                    textviewer.mHideModeSelectedLine = position;
                    view.setBackgroundColor(Color.parseColor("#29abe2"));
                    if (mPrevHideCheckView != null) {
                        mPrevHideCheckView.setBackgroundColor(Color.TRANSPARENT);
                    }
                    mPrevHideCheckView = view;

                    textviewer_hide_btn.setVisibility(View.VISIBLE);

                }
            });
            textviewer_hide_cancel_btn.setVisibility(View.VISIBLE);
        } else {
            textviewer.setOnItemClickListener(mOnItemClickListener);
            textviewer_hide_btn.setVisibility(View.GONE);
            textviewer_hide_cancel_btn.setVisibility(View.GONE);
            textviewer.mHideModeSelectedLine = -1;
            mHidePosition = -1;
            if (mPrevHideCheckView != null) {
                mPrevHideCheckView.setBackgroundColor(Color.TRANSPARENT);
            }
            mPrevHideCheckView = null;
        }
    }

    public boolean isHideMode() {
        return mIsHideMode;
    }

    @Override
    public void onClick(View v) {
        if (v == textviewer_hide_btn) {
            if (mIsHideMode && mHidePosition > 0) {
                int offset = getDBHideLine()>0?-1:0;
                setDBHideLine(getDBHideLine() + mHidePosition + offset);
                mTextData.mPageNum = 0;
                synchronized (textArray) {
                    List<String> list = textArray.subList(mHidePosition,textArray.size());
                    textArray = new ArrayList<>();
                    textArray.add("● ● ●");
                    textArray.addAll(list);
                    textviewer.clearAdapter();
                    textviewer.setBook(textArray);
                    textviewer.setSelection(mTextData.mPageNum);
                    setHideEditMode(false);
                }
            }
        } else if (v == textviewer_hide_cancel_btn) {
            int hidePos = getDBHideLine();
            setDBHideLine(0);
            setHideEditMode(false);
            mActivity.setShowNaviBar();

            if (hidePos > 0) {
                int pos = getFirstVisiblePosition();
                mTextData.mPageNum = hidePos + pos;
                runInitTask();
            }
        }
    }

    private void setDBHideLine(int pos) {
        mTextData.mZoomStandardHeight = pos;
    }

    private int getDBHideLine() {
        return mTextData.mZoomStandardHeight;
    }

	private void changeBook(boolean isNext) {
		if (isNext) {
			Toast.makeText(getContext(), "마지막 입니다.",Toast.LENGTH_SHORT).show();
		} else {
			Toast.makeText(getContext(), "처음 입니다.",Toast.LENGTH_SHORT).show();
		}
	}
	
	private void captureView() {
		this.buildDrawingCache();
		Bitmap bitmap = this.getDrawingCache();
        textviewer_captureView.setBackground(new BitmapDrawable(bitmap));
	}

	public void moveScrollUp() {
		textviewer.moveScrollUp();
	}
	
	public void moveScrollDown() {
		textviewer.moveScrollDown();
	}

	private ArrayList<String> textArray = new ArrayList<String>();

    protected class ContentReadFileAsyncTask extends AsyncTask<Integer, Object, Boolean>{
        public boolean mIsCancel = false;
		@Override
		protected Boolean doInBackground(Integer... params) {
            int hideLine = params[0];
			File file = new File(mContentPath);
			if (file.exists() && file.isFile() && file.canRead()) {

				try {
					String charset = CUtils.detectEncoding(file);
					BufferedReader in = new BufferedReader(new InputStreamReader(
							new FileInputStream(file),charset));
					String s;
					StringBuilder strBuf = new StringBuilder();
                    int count = 0;
                    if (hideLine > 0) {
                        textArray.add("● ● ●");
                    }
					while ((s = in.readLine()) != null) {
                        if (hideLine > count) {
                            count++;
                            continue;
                        }
						s = s.replace("&nbsp;", "");
						strBuf.append(s);
						textArray.add(strBuf.toString());
						strBuf.setLength(0);
					}
					in.close();
                    return true;
				} catch (Exception e) {
					CLog.e(TAG, e);
				}
			}
			return true; 
		}
		
		@Override
		protected void onPostExecute(Boolean isReadText) {
            if (!mIsCancel) {
                textviewer.clearAdapter();
                textviewer.setBook(textArray);
                textviewer.setSelection(mTextData.mPageNum);
            }
		}
	}

	@Override
	public void setTextFont(int index) {
		textviewer.setTextFont(index);

	}

	@Override
	public void setTextColor(int index) {
		setBackgroundColor(SettingTextViewer.sColors[index][0]);
		textviewer.setTextColor(index);
	}

	@Override
	public void setTextSize(int index) {
		textviewer.setTextSize(index);

	}

	@Override
	public void setLineSpacing(int index) {
		textviewer.setLineSpacing(index);

	}
}
