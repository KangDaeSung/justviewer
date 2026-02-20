package com.kds3393.just.viewer.View;

import android.animation.LayoutTransition;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.common.utils.FileUtils;
import com.common.utils.ResManager;
import com.kds3393.just.viewer.Activity.FavoriteList;
import com.kds3393.just.viewer.Animation.LayoutScaleAnimation;
import com.kds3393.just.viewer.Browser.BrowserView;
import com.kds3393.just.viewer.Browser.BrowserView.OnSelectListener;
import com.kds3393.just.viewer.Config.HelpActivity;
import com.kds3393.just.viewer.Config.KConfig;
import com.kds3393.just.viewer.Config.SettingActivity;
import com.kds3393.just.viewer.Config.SharedPrefHelper;
import com.kds3393.just.viewer.Image.ImageViewerActivity;
import com.kds3393.just.viewer.Movie.MoviePlayerActivity;
import com.kds3393.just.viewer.Music.MusicListView;
import com.kds3393.just.viewer.Music.MusicLyricsView;
import com.kds3393.just.viewer.Music.MusicPlayerPanelView;
import com.kds3393.just.viewer.Music.MusicService;
import com.kds3393.just.viewer.R;
import com.kds3393.just.viewer.Text.TextViewerActivity;
import com.kds3393.just.viewer.View.MenuView.OnIconClickListener;
import com.kds3393.just.viewer.provider.DBMgr;
import com.kds3393.just.viewer.provider.FavoriteData;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;

public class MainView extends FrameLayout implements OnClickListener {
	private static final String TAG = "MainView";
	
	private MenuView mMenuView;
	private LinearLayout mBrowserParent;
	protected LinearLayout mFavoriteLayout;
	protected FavoriteList mFavoriteList;
	protected TextView mFavoriteEmptyText;
	
	protected BrowserView mBrowser;
	
	protected RelativeLayout mFunctionLayout;
	
	protected int mSelectedViewId = KConfig.TYPE_FAVORITE_ALL;
	protected String mSelectedFolderPath = "";
	private DBMgr mDBMgr;
	
	public MainView(Context context) {
		super(context);
		mDBMgr = DBMgr.getInstance();
		initAnimation();
		// last data
		mSelectedViewId = SharedPrefHelper.getLastView(context);
		mSelectedFolderPath = SharedPrefHelper.getLastPath(context);

		inflate(context, R.layout.v_main, this);

		mBrowserParent = ((LinearLayout) findViewById(R.id.main_sub));
		LayoutTransition transition = mBrowserParent.getLayoutTransition();
		transition.enableTransitionType(LayoutTransition.CHANGING);
		transition.setDuration(300);
        transition.setAnimator(LayoutTransition.APPEARING,null);

		createMenuView();
		createFavoriteView();
		createBrowserView();
		createBottomMenuView();
		createMusicPanel();
		
		mBrowser.setType(mSelectedViewId);
		mBrowser.setPath(mSelectedFolderPath);
		
		this.postDelayed(new Runnable() {
			@Override
			public void run() {
				if (mSelectedViewId != KConfig.TYPE_FAVORITE_ALL) {
					mSelectedFolderPath = loadRootPath(mSelectedViewId);
					mPathTextView.setText(mBrowser.getFolderName());
					mMenuView.setSelection(mSelectedViewId);
				}
				changeBrowser(mSelectedViewId, true);
			}
		}, 100);
	}
	
	public void onResume() {
		mBrowser.reload();
		mMusicPanel.onResume();
	}
	
	public void onStop() {
		SharedPrefHelper.setLastView(getContext(), mSelectedViewId);
		mMusicPanel.clear();
	}
	
	public boolean onBackPressed() {
		if (mMusicPanel.isShow()) {
			mMusicPanel.setSwitchMusicPanel(false);
			return false;
		} else if (mSelectedViewId != KConfig.TYPE_FAVORITE_ALL) {
			mMenuView.deSelection();
			changeViewType(KConfig.TYPE_FAVORITE_ALL);
			return false;
		}
		return true;
	}
	
	public void setMusicService(MusicService service) {
		mMusicPanel.setMusicService(service);
		mFavoriteList.setMusicService(service);
	}
	
	protected LayoutScaleAnimation mFavoriteAnimation;
	protected LayoutScaleAnimation mBrowserAnimation;
	protected Animation mFadeInAnimation;
	protected Animation mFadeOutAnimation;
	protected Animation mMoveAnimation;
	protected int mAniDruation = 600;
	
	private void initAnimation() {
		mFavoriteAnimation = new LayoutScaleAnimation(getContext());
		mBrowserAnimation = new LayoutScaleAnimation(getContext());
		
		mFadeInAnimation = new AlphaAnimation(0.0f, 1.0f);
		mFadeOutAnimation = new AlphaAnimation(1.0f, 0.0f);
		mFadeInAnimation.setDuration(mAniDruation);
		mFadeOutAnimation.setDuration(mAniDruation);
	}
	
	private boolean isSwitchingView() {
		if (mFavoriteAnimation.mScroller != null && mFavoriteAnimation.mScroller.isFinished() && mBrowserAnimation.mScroller != null
				&& mBrowserAnimation.mScroller.isFinished())
			return false;
		return true;
	}
	
	private void changeViewType(int viewType) {
		changeBrowser(viewType, false);
		mSelectedViewId = viewType;
		if (viewType != KConfig.TYPE_FAVORITE_ALL) {
			mBrowser.setType(mSelectedViewId);
			mBrowser.setPath(mSelectedFolderPath);
			mPathTextView.setText(mBrowser.getFolderName());
		}
		mFavoriteList.unCheckAllItem();
		mMusicPlay.setVisibility(View.INVISIBLE);
		mAddFavorite.setVisibility(View.GONE);
		mDeleteBtn.setVisibility(View.INVISIBLE);
	}
	
	protected void changeBrowser(int viewType, boolean isForce) {
		if (!isForce && mSelectedViewId == viewType) {
			return;
		}

		mBrowser.clearAnimation();
		if (viewType == KConfig.TYPE_FAVORITE_ALL) { // Favorite view Show
			mFavoriteList.setType(viewType);
			if (mFavoriteList.getCount() <= 0) {
				mFavoriteEmptyText.setVisibility(View.VISIBLE);
				mFavoriteList.setVisibility(View.GONE);
			} else {
				mFavoriteEmptyText.setVisibility(View.GONE);
				mFavoriteList.setVisibility(View.VISIBLE);
			}
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) mFavoriteLayout.getLayoutParams();
            params.height =LayoutParams.MATCH_PARENT;
            mFavoriteLayout.setLayoutParams(params);
			if (mFavoriteLayout.getParent() != null) {
				mBrowserParent.removeView(mFavoriteLayout);
			}
			((Activity)getContext()).runOnUiThread(new Runnable(){
				@Override
				public void run() {
					mBrowserParent.addView(mFavoriteLayout,0);
				}
			});

		} else { // Favorite view Hide
			mBrowserParent.removeView(mFavoriteLayout);
		}
	}
	
	protected void favoriteShow(int viewType) {
		mFavoriteList.setType(viewType);
		if (mFavoriteLayout.getParent() == null) { // Favorite view Show
			if (mFavoriteList.getCount() <= 0) {
				mFavoriteEmptyText.setVisibility(View.VISIBLE);
				mFavoriteList.setVisibility(View.GONE);
			} else {
				mFavoriteEmptyText.setVisibility(View.GONE);
				mFavoriteList.setVisibility(View.VISIBLE);
			}

            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) mFavoriteLayout.getLayoutParams();
            params.height = mBrowserParent.getHeight() / 3;
            mFavoriteLayout.setLayoutParams(params);
            mBrowserParent.addView(mFavoriteLayout,0);
    	} else { // Favorite view Hide
            mBrowserParent.removeView(mFavoriteLayout);
		}
	}
	
	private void saveRootPath(String path) {
		switch (mSelectedViewId) {
		case KConfig.TYPE_BROWSER:
			SharedPrefHelper.setBrowserRootPath(getContext(), path);
			break;
		case KConfig.TYPE_IMAGE:
			SharedPrefHelper.setImageRootPath(getContext(), path);
			break;
		case KConfig.TYPE_MOVIE:
			SharedPrefHelper.setMovieRootPath(getContext(), path);
			break;
		case KConfig.TYPE_MUSIC:
			SharedPrefHelper.setMusicRootPath(getContext(), path);
			break;
		case KConfig.TYPE_TEXT:
			SharedPrefHelper.setTextRootPath(getContext(), path);
			break;
		}
		SharedPrefHelper.setLastPath(getContext(), path);
	}
	
	private String loadRootPath(int Type) {
		switch (Type) {
		case KConfig.TYPE_BROWSER:
			return SharedPrefHelper.getBrowserRootPath(getContext());
		case KConfig.TYPE_IMAGE:
			return SharedPrefHelper.getImageRootPath(getContext());
		case KConfig.TYPE_MOVIE:
			return SharedPrefHelper.getMovieRootPath(getContext());
		case KConfig.TYPE_MUSIC:
			return SharedPrefHelper.getMusicRootPath(getContext());
		case KConfig.TYPE_TEXT:
			return SharedPrefHelper.getTextRootPath(getContext());
		}
		return null;
	}
	
	private void startViewer(File file) {
		String extension = FileUtils.getExtension(file.getName());
		
		if (extension.equalsIgnoreCase("zip")) {
			Intent intent = new Intent(getContext(), ImageViewerActivity.class);
			intent.putExtra(BrowserView.EXTRA_BROWSER_PATH, file.getAbsolutePath());
			ArrayList<String> filePaths = new ArrayList<String>();
			int count = mBrowser.getAdapter().getCount();
			for (int i = 0; i < count; i++) {
				String path = (String) mBrowser.getAdapter().getItem(i);
				if (path.length() > 4 && FileUtils.getExtension(path).equalsIgnoreCase("zip")) {
					filePaths.add(path);
				}
			}
			intent.putExtra(BrowserView.EXTRA_BROWSER_PATH_ARRAY, filePaths);
			getContext().startActivity(intent);
		} else if (extension.equalsIgnoreCase("avi") || extension.equalsIgnoreCase("mp4") || extension.equalsIgnoreCase("wmv")
				|| extension.equalsIgnoreCase("mkv")) {
			mMusicPanel.setMusicPause();
			Intent intent = new Intent(getContext(), MoviePlayerActivity.class);
			intent.putExtra(BrowserView.EXTRA_BROWSER_PATH, file.getAbsolutePath());
			ArrayList<String> filePaths = new ArrayList<String>();
			Log.e(TAG,"KDS3393_TEST_startActivity delay = " + filePaths);
			int count = mBrowser.getAdapter().getCount();
			for (int i = 0; i < count; i++) {
				String path = (String) mBrowser.getAdapter().getItem(i);
				if (path.length() > 4
						&& (FileUtils.getExtension(path).equalsIgnoreCase("avi") || FileUtils.getExtension(path).equalsIgnoreCase("mp4")
								|| FileUtils.getExtension(path).equalsIgnoreCase("wmv") || FileUtils.getExtension(path).equalsIgnoreCase("mkv"))) {
					filePaths.add(path);
				}
			}
			intent.putExtra(BrowserView.EXTRA_BROWSER_PATH_ARRAY, filePaths);

			getContext().startActivity(intent);
		} else if (extension.equalsIgnoreCase("mp3") || extension.equalsIgnoreCase("flac")) {
			ArrayList<String> playList = new ArrayList<String>();
			playList.add(file.getPath());
			Intent intent = new Intent(getContext(), MusicService.class);
			intent.putExtra(MusicService.EXTRA_MUSIC_FILE_LIST, playList);
			getContext().startService(intent);
		} else if (extension.equalsIgnoreCase("jpg") || extension.equalsIgnoreCase("png")) {
			
		} else if (extension.equalsIgnoreCase("txt") || extension.equalsIgnoreCase("smi") || extension.equalsIgnoreCase("log")) {
			Intent intent = new Intent(getContext(), TextViewerActivity.class);
			intent.putExtra(BrowserView.EXTRA_BROWSER_PATH, file.getAbsolutePath());
			ArrayList<String> filePaths = new ArrayList<String>();
			int count = mBrowser.getAdapter().getCount();
			for (int i = 0; i < count; i++) {
				String path = (String) mBrowser.getAdapter().getItem(i);
				if (path.length() > 4
						&& (FileUtils.getExtension(path).equalsIgnoreCase("txt") || FileUtils.getExtension(path).equalsIgnoreCase("smi") || FileUtils
								.getExtension(path).equalsIgnoreCase("log"))) {
					filePaths.add(path);
				}
			}
			intent.putExtra(BrowserView.EXTRA_BROWSER_PATH_ARRAY, filePaths);
			getContext().startActivity(intent);
		}
		
		return;
		
	}
	
	protected RelativeLayout mTopMenuLayout;
	protected TextView mPathTextView;
	protected Button mSubFavorite;
	
	protected void createMenuView() {
		mTopMenuLayout = (RelativeLayout) findViewById(R.id.top_menu);
		
		mMenuView = (MenuView) findViewById(R.id.menu);
		mMenuView.setIconSize(ResManager.getDimen(R.dimen.top_icon_size));
		mMenuView.setSpacing(ResManager.getDimen(R.dimen.top_icon_gap));
		mMenuView.addIconView(KConfig.TYPE_IMAGE, R.drawable.z_folder_library);
		mMenuView.addIconView(KConfig.TYPE_MOVIE, R.drawable.z_folder_movie);
		mMenuView.addIconView(KConfig.TYPE_TEXT, R.drawable.z_folder_bookmark);
		mMenuView.addIconView(KConfig.TYPE_MUSIC, R.drawable.z_folder_music);
		mMenuView.addIconView(KConfig.TYPE_BROWSER, R.drawable.z_folder_desktop);
		mMenuView.setOnIconClickListener(new OnIconClickListener() {
			@Override
			public void onIconClick(int id, boolean isSelected) {
				if (isSwitchingView())
					return;
				if (isSelected) {
					mPathTextView.startAnimation(mFadeInAnimation);
					mPathTextView.setVisibility(View.VISIBLE);
					mSubFavorite.startAnimation(mFadeInAnimation);
					mSubFavorite.setVisibility(View.VISIBLE);
				} else {
					id = KConfig.TYPE_FAVORITE_ALL;
					mPathTextView.startAnimation(mFadeOutAnimation);
					mPathTextView.setVisibility(View.GONE);
					mSubFavorite.startAnimation(mFadeOutAnimation);
					mSubFavorite.setVisibility(View.GONE);
				}
				
				if (id == KConfig.TYPE_IMAGE) {
					mSelectedFolderPath = SharedPrefHelper.getImageRootPath(getContext());
				} else if (id == KConfig.TYPE_MOVIE) {
					mSelectedFolderPath = SharedPrefHelper.getMovieRootPath(getContext());
				} else if (id == KConfig.TYPE_TEXT) {
					mSelectedFolderPath = SharedPrefHelper.getTextRootPath(getContext());
				} else if (id == KConfig.TYPE_MUSIC) {
					HelpActivity.showHelp(getContext(), HelpActivity.HELP_MUSIC_PANEL_SHOW);
					mSelectedFolderPath = SharedPrefHelper.getMusicRootPath(getContext());
				} else if (id == KConfig.TYPE_BROWSER) {
					mSelectedFolderPath = SharedPrefHelper.getLastPath(getContext());
				}
				
				changeViewType(id);
			}
		});
		
		mPathTextView = (TextView) findViewById(R.id.path_txt);
		mSubFavorite = (Button) findViewById(R.id.favo_btn);
		mSubFavorite.setOnClickListener(this);
	}
	
	protected void createFavoriteView() {
		mFavoriteLayout = (LinearLayout) findViewById(R.id.favo_l);
		mFavoriteList = (FavoriteList) findViewById(R.id.favo_list);
		mFavoriteList.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Cursor cursor = (Cursor) parent.getItemAtPosition(position);
				File file = new File(cursor.getString(FavoriteData.INDEX_PATH));
				if (file.exists() && file.isDirectory()) {
					mSelectedFolderPath = cursor.getString(FavoriteData.INDEX_PATH);
					if (mSelectedViewId == KConfig.TYPE_FAVORITE_ALL) {
						changeViewType(cursor.getInt(FavoriteData.INDEX_TYPE));
					} else {
						mBrowser.setPath(mSelectedFolderPath);
					}
				} else {
					// delete
				}
			}
		});
		
		mFavoriteList.setOnFileCheckedListener(new FavoriteList.OnFileCheckedListener() {
			@Override
			public void onChecked(int checkPos, int checkCount) {
				mAddFavorite.setVisibility(View.GONE);
				if (checkCount > 0) {
					mMusicPlay.setVisibility(View.INVISIBLE);
					mAddFavorite.setVisibility(View.GONE);
					mDeleteBtn.setVisibility(View.VISIBLE);
					mMusicPanel.setSwitchMusicPanel(false);
				} else {
					mDeleteBtn.setVisibility(View.GONE);
				}
			}
		});
		
		mFavoriteEmptyText = (TextView) findViewById(R.id.favo_empty_txt);
		mBrowserParent.removeView(mFavoriteLayout);
	}
	
	protected void createBrowserView() {
		mBrowser = (BrowserView) findViewById(R.id.browser_v);
		mBrowser.setType(mSelectedViewId);
		mBrowser.setPath(mSelectedFolderPath);
		mPathTextView.setText(mBrowser.getFolderName());
		mBrowser.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				String path = (String) parent.getItemAtPosition(position);
				File file = new File(path);
				if (file.getName().equals("..")) {
					mBrowser.moveParentPath();
					saveRootPath(mBrowser.getPath());
				} else if (file.isDirectory()) {
					mBrowser.setPath(file.getPath());
					saveRootPath(mBrowser.getPath());
				} else if (file.isFile()) {
					if (mSelectedViewId == KConfig.TYPE_BROWSER && FileUtils.getExtension(path).equalsIgnoreCase("zip")) {
						mBrowser.setPath(file.getPath());
					} else {
						startViewer(file);
					}
				}
				mPathTextView.setText(mBrowser.getFolderName());
			}
		});
		
		mBrowser.setOnSelectListener(new OnSelectListener() {
			@Override
			public void onSelect(String path) {
				
			}
		});
		
		mBrowser.setOnFileCheckedListener(new BrowserView.OnFileCheckedListener() {
			@Override
			public void onChecked(int checkPos, int checkCount) {
				if (checkCount > 0) {
					boolean isMp3Play = false;
					boolean isFavorite = true;
					boolean[] checked = mBrowser.getCheckedConfrim();
					for (int i = 0; i < checked.length; i++) {
						if (checked[i]) {
							String filePath = (String) mBrowser.getAdapter().getItem(i);
							File file = new File(filePath);
							if (file.isFile()) {
								isFavorite = false;
								if (FileUtils.getExtension(file.getName()).equalsIgnoreCase("mp3") ||
										FileUtils.getExtension(file.getName()).equalsIgnoreCase("flac")) {
									isMp3Play = true;
								}
							}
						}
					}
					
					if (isFavorite) {
						mAddFavorite.setVisibility(View.VISIBLE);
					} else {
						mAddFavorite.setVisibility(View.GONE);
					}
					
					if (isMp3Play) {
						mMusicPlay.setVisibility(View.VISIBLE);
					} else {
						mMusicPlay.setVisibility(View.GONE);
					}
					
					if (mAddFavorite.getVisibility() == View.GONE && mMusicPlay.getVisibility() == View.GONE) {
						mAddFavorite.setVisibility(View.INVISIBLE);
					}
					
					mDeleteBtn.setVisibility(View.VISIBLE);
					mMusicPanel.setSwitchMusicPanel(false);
				} else {
					mAddFavorite.setVisibility(View.INVISIBLE);
					mDeleteBtn.setVisibility(View.GONE);
					mMusicPlay.setVisibility(View.GONE);
				}
			}
		});
	}
	
	protected Button mAddFavorite;
	protected Button mMusicPlay;
	protected Button mDeleteBtn;
	protected Button mSettingBtn;
	protected void createBottomMenuView() {
		mFunctionLayout = (RelativeLayout) findViewById(R.id.function_l);
		mMusicPlay = (Button) findViewById(R.id.main_music_play);
		mAddFavorite = (Button) findViewById(R.id.main_add_favo);
		mDeleteBtn = (Button) findViewById(R.id.main_delete);
		mSettingBtn = (Button) findViewById(R.id.main_setting);
		mMusicPlay.setOnClickListener(this);
		mAddFavorite.setOnClickListener(this);
		mDeleteBtn.setOnClickListener(this);
		mSettingBtn.setOnClickListener(this);
	}
	
	protected MusicPlayerPanelView mMusicPanel;
	protected MusicLyricsView mMusicLyricsPanel;
	protected MusicListView mMusicListPanel;
	
	protected void createMusicPanel() {
		mMusicLyricsPanel = (MusicLyricsView) findViewById(R.id.main_music_lyrics);
		mMusicListPanel = (MusicListView) findViewById(R.id.main_music_list);
		mMusicPanel = (MusicPlayerPanelView) findViewById(R.id.main_music_panel);
		mMusicPanel.setLyricsView(mMusicLyricsPanel);
		mMusicPanel.setMusicList(mMusicListPanel);
	}
	
	@Override
	public void onClick(View v) {
		int id = v.getId();
		if (id == R.id.main_delete) {
			if (mSelectedViewId == KConfig.TYPE_FAVORITE_ALL) {
				int removeCount = 0;
				Iterator<Integer> iterator = mFavoriteList.isCheckedConfrimHashMap.keySet().iterator();
				while (iterator.hasNext()) {
					int key = (int) iterator.next();
					if (mFavoriteList.isCheckedConfrimHashMap.get(key)) {
						mDBMgr.removeFavorite(key);
						removeCount++;
					}
				}
				if (removeCount >= mFavoriteList.getCount()) {
					mFavoriteList.setVisibility(View.GONE);
					mFavoriteEmptyText.setVisibility(View.VISIBLE);
				}
				mDeleteBtn.setVisibility(View.GONE);
				mFavoriteList.reload();
			} else {
				boolean[] checked = mBrowser.getCheckedConfrim();
				int checkCount = 0;
				for (boolean isChecked : checked) {
					if (isChecked)
						checkCount++;
				}
				
				AlertDialog.Builder aDialog = new AlertDialog.Builder(new ContextThemeWrapper(getContext(), android.R.style.Theme_Holo_Dialog));
				aDialog.setTitle("삭제");
				aDialog.setMessage(checkCount + "개의 파일 및 폴더를 삭제하시겠습니까?");
				aDialog.setNegativeButton("취소", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				}).setPositiveButton("확인", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						boolean[] checked = mBrowser.getCheckedConfrim();
                        boolean isDelete = false;
						for (int i = 0; i < checked.length; i++) {
							if (checked[i]) {
								String filePath = (String) mBrowser.getAdapter().getItem(i);
								File file = new File(filePath);
								if (file.exists()) {
									if (file.isFile()) {
										FileUtils.deleteFile(file.getAbsolutePath());
										String extension = FileUtils.getExtension(file.getName());
										if (extension.equalsIgnoreCase("avi") || extension.equalsIgnoreCase("mp4") || extension.equalsIgnoreCase("wmv")
												|| extension.equalsIgnoreCase("mkv")) {
											String smiPath = file.getAbsolutePath().substring(0, file.getAbsolutePath().lastIndexOf(".")) + ".smi";
											FileUtils.deleteFile(smiPath);
										}
									} else if (file.isDirectory()) {
                                        FileUtils.deleteFolder(file.getAbsolutePath());
                                    }
                                    isDelete = true;
								}
							}
						}
                        if (isDelete) {
                            Toast.makeText(getContext(), "파일 또는 폴더가 삭제되었습니다.", Toast.LENGTH_SHORT).show();
                        }
						mBrowser.reload();
					}
				});
				aDialog.show();
			}
		} else if (id == R.id.main_setting) {
			Intent intent = new Intent(getContext(), SettingActivity.class);
			getContext().startActivity(intent);
		} else if (id == R.id.main_add_favo) {
			boolean[] checked = mBrowser.getCheckedConfrim();
			for (int i = 0; i < checked.length; i++) {
				if (checked[i]) {
					String filePath = (String) mBrowser.getAdapter().getItem(i);
					mDBMgr.insertFavorite(new FavoriteData(filePath, mSelectedViewId));
					Toast.makeText(getContext(), "즐겨찾기가 저장되었습니다.", Toast.LENGTH_SHORT).show();
					mFavoriteList.reload();
				}
			}
			mBrowser.clearCheckedConfrim();
		} else if (id == R.id.favo_btn) {
			if (isSwitchingView())
				return;
			favoriteShow(mSelectedViewId);
		} else if (id == R.id.main_music_play) {
			ArrayList<String> playList = new ArrayList<String>();
			boolean[] checked = mBrowser.getCheckedConfrim();
			for (int i = 0; i < checked.length; i++) {
				if (checked[i]) {
					String filePath = (String) mBrowser.getAdapter().getItem(i);
					if (FileUtils.getExtension(filePath).equalsIgnoreCase("mp3") ||
							FileUtils.getExtension(filePath).equalsIgnoreCase("flac"))
						playList.add(filePath);
				}
				checked[i] = false;
			}
			mFavoriteList.reload();
			if (playList.size() > 0) {
				Intent intent = new Intent(getContext(), MusicService.class);
				intent.putExtra(MusicService.EXTRA_MUSIC_FILE_LIST, playList);
				getContext().startService(intent);
			}
			
			mBrowser.clearCheckedConfrim();
		}
	}
}
