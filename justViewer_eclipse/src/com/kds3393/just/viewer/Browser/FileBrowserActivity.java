package com.kds3393.just.viewer.Browser;

import java.io.File;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.common.utils.FileUtils;
import com.common.utils.LayoutUtils;
import com.common.utils.Size;
import com.common.utils.ViewMaker;
import com.kds3393.just.viewer.ParentActivity;
import com.kds3393.just.viewer.Browser.BrowserView.OnFileCheckedListener;
import com.kds3393.just.viewer.Browser.BrowserView.OnSelectListener;
import com.kds3393.just.viewer.Config.KConfig;
import com.kds3393.just.viewer.Config.SharedPrefHelper;
import com.kds3393.just.viewer.Image.ImageViewerActivity;
import com.kds3393.just.viewer.Movie.MoviePlayerActivity;

public class FileBrowserActivity extends ParentActivity {
	private static final String TAG = "ImageViewer";
	
	public static final String EXTRA_BROWSER_PATH = "extra_browser_path";
	public static final String EXTRA_BROWSER_PATH_ARRAY = "extra_browser_path_array";
	
	public static final String EXTRA_NEXT_VIEW = "extra_next_view";
	
	public String mRootPath;
	public int mViewType;
	
	private TextView mPathTextView;
	private Button mDeleteBtn;
	private BrowserView mBrowser;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getIntentData();
        setContentView(createMainView());
    }
    
    private void getIntentData() {
    	mRootPath = getIntent().getStringExtra(EXTRA_BROWSER_PATH);
    	mViewType = getIntent().getIntExtra(EXTRA_NEXT_VIEW,KConfig.TYPE_FAVORITE);
    	
    	if (TextUtils.isEmpty(mRootPath))
    		mRootPath = KConfig.PATH_SDCARD_ROOT;
    	
    }
    
    private LinearLayout createMainView() {
    	LinearLayout main = new LinearLayout(this);
    	main.setOrientation(LinearLayout.VERTICAL);
    	main.setGravity(Gravity.CENTER_HORIZONTAL);
    	main.setWeightSum(4);
    		
    		createNavi(main);
    		createBrowser(main);
    		if (mViewType == KConfig.TYPE_BROWSER ||
    				mViewType == KConfig.TYPE_FAVORITE) {
    			createFolderSelectBtn(main);
    		}
		return main;
    }
    
    private void createNavi(LinearLayout main) {
    	LinearLayout topNavi = new LinearLayout(this);
		main.addView(topNavi);
		LayoutUtils.setLinearLayoutParams(topNavi, LayoutParams.MATCH_PARENT, (int) (Size.DisplayHeight * 0.07),0,0,0,(int) (Size.DisplayHeight * 0.01));
		
			mPathTextView = ViewMaker.TextViewMaker(this, topNavi, "", (int) (Size.DisplayWidth * 0.7), LayoutParams.MATCH_PARENT, 0, 0);
			mPathTextView.setBackgroundColor(Color.BLUE);
			mPathTextView.setGravity(Gravity.CENTER_VERTICAL);
			mPathTextView.setTextSize((float) (22 / ((0.5 * Size.Density) + 0.5)));
			
			LinearLayout RightLayout = new LinearLayout(this);
			topNavi.addView(RightLayout);
			LayoutUtils.setLinearLayoutParams(RightLayout, LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT);
				
				mDeleteBtn = ViewMaker.ButtonMaker(this, RightLayout, (int) (Size.DisplayWidth * 0.3), LayoutParams.MATCH_PARENT, 0, 0);
				mDeleteBtn.setText("Del");
				mDeleteBtn.setVisibility(View.INVISIBLE);
				mDeleteBtn.setOnClickListener(new OnClickListener(){
					@Override
					public void onClick(View arg0) {
						boolean[] checked = mBrowser.getCheckedConfrim();
						for (int i = 0;i<checked.length;i++) {
							if (checked[i]) {
								String filePath = (String) mBrowser.getAdapter().getItem(i);
								File file = new File(filePath);
								FileUtils.deleteFile(file.getAbsolutePath());
							}
						}
						mBrowser.reload();
					}
				});
    }
    private void createBrowser(LinearLayout main) {
    	float proportion = 0.92f;
    	if (mViewType == KConfig.TYPE_BROWSER ||
				mViewType == KConfig.TYPE_FAVORITE)
    		proportion = 0.82f;
    	mBrowser = new BrowserView(this);
		mBrowser.setType(mViewType);
		main.addView(mBrowser);
		LayoutUtils.setLinearLayoutParams(mBrowser, LayoutParams.MATCH_PARENT, (int) (Size.DisplayHeight * proportion));
		mBrowser.setPath(mRootPath);
		mPathTextView.setText(mBrowser.getFolderName());
		mBrowser.setOnItemClickListener(new OnItemClickListener(){
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				String path = (String) parent.getItemAtPosition(position);
				File file = new File(path);
				if (file.getName().equals("..")) {
					mBrowser.moveParentPath();
					mDeleteBtn.setVisibility(View.INVISIBLE);
				} else if (file.isDirectory()) {
					mBrowser.setPath(file.getPath());
					saveRootPath(mBrowser.getPath());
					mDeleteBtn.setVisibility(View.INVISIBLE);
				} else if (file.isFile()) {
					if ((mViewType == KConfig.TYPE_BROWSER ||
		    				mViewType == KConfig.TYPE_FAVORITE) && FileUtils.getExtension(path).equalsIgnoreCase("zip")) {
						mBrowser.setPath(file.getPath());
					} else {
						startViewer(file);
					}
				}
				mPathTextView.setText(mBrowser.getFolderName());
			}
		});
		
		mBrowser.setOnSelectListener(new OnSelectListener(){
			@Override
			public void onSelect(String path) {
				
			}
		});
		
		
		mBrowser.setOnFileCheckedListener(new OnFileCheckedListener(){
			@Override
			public void onChecked(int checkPos, int checkCount) {
				if (checkCount > 0)
					mDeleteBtn.setVisibility(View.VISIBLE);
				else
					mDeleteBtn.setVisibility(View.INVISIBLE);
			}
		});
    }
    private void createFolderSelectBtn(LinearLayout main) {
    	Button selectBtn = ViewMaker.ButtonMaker(this, main, LayoutParams.MATCH_PARENT, (int) (Size.DisplayHeight * 0.1),0,0);
		selectBtn.setText("현재 폴더 선택");
		selectBtn.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				SharedPrefHelper.setImageRootPath(FileBrowserActivity.this, mBrowser.getPath());
				Intent intent = null;
				switch(mViewType) {
				case KConfig.TYPE_IMAGE:
					intent = new Intent(FileBrowserActivity.this, ImageViewerActivity.class);
					break;
				}
				if (intent != null) {
					finish();
					startActivity(intent);
				} else
					finish();
			}
		});
    }
    
    private void saveRootPath(String path) {
    	switch(mViewType) {
    	case KConfig.TYPE_BROWSER:
    		SharedPrefHelper.setLastPath(FileBrowserActivity.this, path);
    		break;
		case KConfig.TYPE_IMAGE:
			SharedPrefHelper.setImageRootPath(FileBrowserActivity.this, path);
			break;
		}
    }
    
    private void startViewer(File file) {
    	String extension = FileUtils.getExtension(file.getName());
    	
    	if (extension.equalsIgnoreCase("zip")) {
    		Intent intent = new Intent(FileBrowserActivity.this, ImageViewerActivity.class);
    		intent.putExtra(FileBrowserActivity.EXTRA_BROWSER_PATH, file.getAbsolutePath());
    		startActivity(intent);
    	} else if (extension.equalsIgnoreCase("avi") ||
	    		extension.equalsIgnoreCase("mp4") ||
	    		extension.equalsIgnoreCase("wmv") ||
	    		extension.equalsIgnoreCase("mkv")) {
    		Intent intent = new Intent(FileBrowserActivity.this, MoviePlayerActivity.class);
    		intent.putExtra(FileBrowserActivity.EXTRA_BROWSER_PATH, file.getAbsolutePath());
    		startActivity(intent);
    	} else if (extension.equalsIgnoreCase("txt") ||
	    		extension.equalsIgnoreCase("mp3")) {
    		
    	}
    	
    	return;
    		
    }
}
