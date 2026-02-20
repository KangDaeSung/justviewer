package com.kds3393.just.viewer.Text;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.common.utils.Size;
import com.common.utils.debug.CLog;
import com.kds3393.just.viewer.Config.SettingTextViewer;
import com.kds3393.just.viewer.Image.ImageDownloader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class VTextListView extends ListView implements TextSettingInterface {
	private static final String TAG = "VTextScrollView";
	
	private String mContentPath;
	public ArrayList<String> mFilePaths;
	public int mBookIndex;
	
	private TextAdapter mAdapter;
	
	private int mTextColor = Color.WHITE;
	private float mTextSize = 15;
	private float mTextSpacing = 1;
	
	private Typeface mFont;
	
	private int mScrollDelay = 500;

    public VTextListView(Context context) {
        super(context);
        initList(context);
    }

    public VTextListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initList(context);
    }

    public VTextListView(Context context,String path) {
        super(context);
        mContentPath = path;
        initList(context);
    }

    public void setContentPath(String path) {
        mContentPath = path;
    }

    private void initList(Context context) {
		setFadingEdgeLength(0);
		setCacheColorHint(0);
		setSelector(android.R.color.transparent);
		setDivider(null);
        setFastScrollEnabled(true);
		SettingTextViewer.initTextFont(context);
		mTextColor = SettingTextViewer.sColors[SettingTextViewer.getTextColor(context)][1];
		mTextSize = SettingTextViewer.getTextSizeValue(context);
		mTextSpacing = SettingTextViewer.getTextGapValue(context);
		mFont = SettingTextViewer.getTextFontTypeface(context);
		mAdapter = new TextAdapter(context);
		this.setAdapter(mAdapter);
	}
	
	private boolean mIsSingleSetting = false;
	public void isSingleSetting() {
		mIsSingleSetting = true;
		setBackgroundColor(SettingTextViewer.sColors[SettingTextViewer.getTextColor(getContext())][0]);
	}
	@Override
	public void setTextFont(int index) {
		mFont = SettingTextViewer.sFonts.get(index);
		int max = getChildCount();
		for(int i=0;i<max;i++) {
			View view = getChildAt(i);
			if (view instanceof TextView) {
				((TextView)view).setTypeface(mFont);
			}
		}
	}
	
	@Override
	public void setTextColor(int index) {
		if (mIsSingleSetting)
			setBackgroundColor(SettingTextViewer.sColors[index][0]);
		mTextColor = SettingTextViewer.sColors[index][1];
		int max = getChildCount();
		for(int i=0;i<max;i++) {
			View view = getChildAt(i);
			if (view instanceof TextView) {
				((TextView)view).setTextColor(mTextColor);
			}
		}
	}

	@Override
	public void setTextSize(int index) {
		mTextSize = SettingTextViewer.sSizes[index];
		int max = getChildCount();
		for(int i=0;i<max;i++) {
			View view = getChildAt(i);
			if (view instanceof TextView) {
				((TextView)view).setTextSize(mTextSize);
			}
		}
	}
	
	@Override
	public void setLineSpacing(int index) {
		mTextSpacing = SettingTextViewer.sGaps[index];
		int max = getChildCount();
		for(int i=0;i<max;i++) {
			View view = getChildAt(i);
			if (view instanceof TextView) {
				((TextView)view).setLineSpacing(7, mTextSpacing);
			}
		}
	}
	
	public void setScrollSpeed(int speed) {
		mScrollDelay = speed;
	}
	
	public int getScrollSpeed() {
		return mScrollDelay;
	}
	
	public void setContentFileList(ArrayList<String> file) {
		if (file.size() > 1) {
			mFilePaths = file;
			mBookIndex = mFilePaths.indexOf(ImageDownloader.sZipPath);
		} else
			mFilePaths = null;
	}
	
	public void movePrev() {
		this.smoothScrollBy((int) -(Size.DisplayHeight *0.95), 1);
	}
	
	public void moveNext() {
		this.smoothScrollBy((int)(Size.DisplayHeight *0.95), 1);
	}
	
	public void moveScrollUp() {
		this.smoothScrollBy((int) -(Size.DisplayHeight *0.95), mScrollDelay);
	}
	
	public void moveScrollDown() {
		this.smoothScrollBy((int)(Size.DisplayHeight *0.95), mScrollDelay);
	}

	public void clearAdapter() {
        mAdapter.clear();
    }

	public void setBook(ArrayList<String> texts) {
		mTextArray = texts;
		mAdapter.addAll(mTextArray);
	}

	private int mMaxBufferLine = 1;
	private ArrayList<String> mTextArray = new ArrayList<String>();

	private ContentReadFileAsyncTask mReadBookAsyncTask = new ContentReadFileAsyncTask();
	public void runInitTask() {
		mReadBookAsyncTask.execute(true);
	}
	
	public void runSettingText() {
		mTextArray.add(" 가나다라마바사 ");
		mTextArray.add(" ABCDEFG ");
		mTextArray.add(" 1234567890 ");
		mAdapter.addAll(mTextArray);
	}
	
	protected class ContentReadFileAsyncTask extends AsyncTask<Boolean, Object, Boolean>{
		@Override
		protected Boolean doInBackground(Boolean... params) {
            File file = new File(mContentPath);
            if (file.exists() && file.isFile() && file.canRead()) {
                try {
                    BufferedReader in = new BufferedReader(new InputStreamReader(
                            new FileInputStream(file),"MS949"));
                    String s;
                    StringBuilder strBuf = new StringBuilder();
                    while ((s = in.readLine()) != null) {
                        s = s.replace("&nbsp;", "");
                        strBuf.append(s);
                        mTextArray.add(strBuf.toString());
                        strBuf.setLength(0);
                    }
                    in.close();
                    return true;
                } catch (Exception e) {
                    CLog.e(TAG, e);
                }
            }
			//initConfig();
			return true; 
		}
		
		@Override
		protected void onPostExecute(Boolean isReadText) {
			mAdapter.addAll(mTextArray);
		}
	}
	
	private static final int FIRST_TEXT = 0;
	private static final int UPDATE_TEXT = 1;
	private Handler mUpdateHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case FIRST_TEXT:
            	break;
            case UPDATE_TEXT:
            	break;
            }
        }
    };

    protected int mHideModeSelectedLine = -1;
	public class TextAdapter extends ArrayAdapter<String> {
	    private static final String TAG = "TextAdapter";
	    
	    public TextAdapter(Context context) {
	    	super(context,0);
	    }
        
	    @Override
	    public View getView(int position, View convertView, ViewGroup parent) {
	    	return getContent(position,convertView,parent);
	    }
	    
	    
	    private View getContent(int position, View convertView, ViewGroup parent) {
	    	TextView listMain = (TextView) convertView;
	    	
	    	String text = null;
	    	text = (String) getItem(position);
	    	
    		if (listMain == null) {
    			listMain = new TextView(getContext());
    			
    		} 
			listMain.setTextSize(mTextSize);
			listMain.setTextColor(mTextColor);
			listMain.setLineSpacing(7, mTextSpacing);
    		listMain.setText(text);
    		listMain.setTypeface(mFont);
            if (mHideModeSelectedLine > 0 && position == mHideModeSelectedLine) {
                listMain.setBackgroundColor(Color.parseColor("#29abe2"));
            } else {
                listMain.setBackgroundColor(Color.TRANSPARENT);
            }

	        return listMain;
	    }
	}
}
