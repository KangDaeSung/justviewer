package com.kds3393.just.viewer.Activity;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Paint;
import android.media.MediaPlayer;
import android.support.v4.widget.CursorAdapter;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.common.utils.FileUtils;
import com.common.utils.Size;
import com.common.utils.ViewMaker;
import com.kds3393.just.viewer.R;
import com.kds3393.just.viewer.Activity.FavoriteList.FavoriteAdapter.ViewHolder;
import com.kds3393.just.viewer.Config.KConfig;
import com.kds3393.just.viewer.Music.Mp3Id3Parser;
import com.kds3393.just.viewer.Music.MusicService;
import com.kds3393.just.viewer.Music.MusicService.OnMediaPlayerListener;
import com.kds3393.just.viewer.Utils.SubStringComparator;
import com.kds3393.just.viewer.provider.FavoriteData;
import com.kds3393.just.viewer.provider.Provider;

public class FavoriteList extends ListView {
	private static final String TAG = "FavoriteList";
	
	private MusicService mService;
	
	private String rootPath;
	private FavoriteAdapter mAdapter;
	private int mListType = KConfig.TYPE_FAVORITE;

	private OnFileCheckedListener mCheckedListener = null;
	public interface OnFileCheckedListener {
		public void onChecked(int checkPos, int checkCount);
	};
	
	public void setOnFileCheckedListener(OnFileCheckedListener listnener) {
		mCheckedListener = listnener;
	}
	
	public FavoriteList(Context context) {
		super(context);
		Cursor cursor = context.getContentResolver().query(Provider.IMAGE_FAVORITE_URI, FavoriteData.LIST_PROJECTION, null , null, Provider.IMAGE_FAVORITE_TYPE + " COLLATE LOCALIZED ASC");
		mAdapter = new FavoriteAdapter(context,cursor);
        this.setAdapter(mAdapter);
	}
	
	public FavoriteList(Context context, AttributeSet attrs) {
		super(context, attrs);
		Cursor cursor = context.getContentResolver().query(Provider.IMAGE_FAVORITE_URI, FavoriteData.LIST_PROJECTION, null , null, Provider.IMAGE_FAVORITE_TYPE + " COLLATE LOCALIZED ASC");
		mAdapter = new FavoriteAdapter(context,cursor);
        this.setAdapter(mAdapter);
	}
	
	public void setMusicService(MusicService service) {
		mService = service;
		mService.setOnMediaPlayerListener(this, new OnMediaPlayerListener(){
			@Override public void onStartCommanded(ArrayList<String> array) {}
			@Override public void onPrepared(MediaPlayer mediaplsyer, Mp3Id3Parser data) {}
			@Override public void onSleepTimer(boolean isRun, String timeString) {}
			@Override
			public void onCompletion(MediaPlayer mediaplsyer) {
				for (int i=0;i<FavoriteList.this.getChildCount();i++) {
					View v = FavoriteList.this.getChildAt(i);
					if (v.getTag() instanceof ViewHolder) {
						ViewHolder holder = (ViewHolder) v.getTag();
						if (holder.mFunctionButton != null && holder.mFunctionButton.getVisibility() == View.VISIBLE)
							holder.mFunctionButton.setBackgroundResource(R.drawable.h_media_play);
					}
				}
			}
			
			@Override
			public void onPlay(MediaPlayer mediaplsyer, String playPath) {
				for (int i=0;i<FavoriteList.this.getChildCount();i++) {
					View v = FavoriteList.this.getChildAt(i);
					if (v.getTag() instanceof ViewHolder) {
						ViewHolder holder = (ViewHolder) v.getTag();
						if (holder.mFunctionButton != null && holder.mFunctionButton.getVisibility() == View.VISIBLE) {
							String path = (String) holder.mFunctionButton.getTag();
							if (path.equalsIgnoreCase(playPath)) {
								holder.mFunctionButton.setBackgroundResource(R.drawable.h_media_pause);
							}
						}
					}
				}
			}
			@Override
			public void onPause(MediaPlayer mediaplsyer, String playPath) {
				for (int i=0;i<FavoriteList.this.getChildCount();i++) {
					View v = FavoriteList.this.getChildAt(i);
					if (v.getTag() instanceof ViewHolder) {
						ViewHolder holder = (ViewHolder) v.getTag();
						if (holder.mFunctionButton != null && holder.mFunctionButton.getVisibility() == View.VISIBLE) {
							String path = (String) holder.mFunctionButton.getTag();
							if (path.equalsIgnoreCase(playPath)) {
								holder.mFunctionButton.setBackgroundResource(R.drawable.h_media_play);
							}
						}
					}
				}
			}

			@Override
			public void onDeleteMusicFile(String filePath) {
				String DeleteFolder = new File(filePath).getParent();
				Cursor c;
				String fPath;
				for (int i=0;i<mAdapter.getCount();i++) {
					c = (Cursor) mAdapter.getItem(i);
					fPath = c.getString(FavoriteData.INDEX_PATH);
					if (fPath.equalsIgnoreCase(DeleteFolder)){
						mAdapter.mPlayPath = fPath;
						mAdapter.notifyDataSetChanged();
						break;
					}
				}
				
				
				
			}
		});
	}
	
	public void unCheckAllItem() {
		Iterator<Integer> iterator = isCheckedConfrimHashMap.keySet().iterator();
		while (iterator.hasNext()) {
			Integer key = iterator.next();
			if (key != null) {
				isCheckedConfrimHashMap.put(key, false);
			}
		}
	}
	
	public void reload() {

	}
	public String getFolderName() {
		if (TextUtils.isEmpty(rootPath))
			return "";
		return rootPath.substring(rootPath.lastIndexOf("/"), rootPath.length());
	}
	public String getPath() {
		return rootPath;
	}
	

	
	public void setType(int type) {
		mAdapter.mPlayPath = null;
		if (mListType == type)
			return;
		mListType = type;
		String where = null;
		if (mListType != KConfig.TYPE_FAVORITE) {
			where = Provider.IMAGE_FAVORITE_TYPE + "=\"" + type + "\"";
		}
		Cursor cursor = getContext().getContentResolver().query(Provider.IMAGE_FAVORITE_URI, 
				FavoriteData.LIST_PROJECTION, where , null, Provider.IMAGE_FAVORITE_TYPE + " COLLATE LOCALIZED ASC");
		if (cursor != null)
			mAdapter.changeCursor(cursor);
	}
	
	public HashMap<Integer,Boolean> isCheckedConfrimHashMap = new HashMap<Integer,Boolean>();;
	public HashMap<Integer,Boolean> getCheckedConfrim() {
		return isCheckedConfrimHashMap;
	}
	
	public class FavoriteAdapter extends CursorAdapter {
		private static final String TAG = "BoomarkListAdapter";
	    private int mCheckCount = 0;
	    public String mPlayPath = null;
	    public FavoriteAdapter(Context context, Cursor c) {
			super(context, c);
		}
	    
	    public void finishAddItem() {

	    }
	    
	    @Override
	    protected void onContentChanged() {
	    	isCheckedConfrimHashMap.clear();
	    	mCheckCount = 0;
	        super.onContentChanged();
	    }
	    
	    @Override
	    public void changeCursor(Cursor cursor) {
	        super.changeCursor(cursor);
	    }
	    
	    class ViewHolder {
	    	public CheckBox mOptionChkView = null;
	    	public ImageView mFileImgView = null;
	    	public TextView mTitleView = null;
	    	public TextView mDescTitleView = null;
	    	public Button mFunctionButton = null;
	    }
	    
	    private ViewHolder viewHolder = null;
	    
	    @Override
		public void bindView(View view, Context context, Cursor cursor) {
	    	viewHolder = (ViewHolder) view.getTag();
	    	if (mListType == KConfig.TYPE_FAVORITE) {
	    		viewHolder.mOptionChkView.setVisibility(View.VISIBLE);
		    	Boolean checked = isCheckedConfrimHashMap.get(cursor.getInt(FavoriteData.INDEX_ID));
		    	viewHolder.mOptionChkView.setTag(cursor.getInt(FavoriteData.INDEX_ID));
		    	if (checked != null && checked)
		    		viewHolder.mOptionChkView.setChecked(true);
		    	else
		    		viewHolder.mOptionChkView.setChecked(false);
	    		viewHolder.mOptionChkView.setFocusable(false);
	    		viewHolder.mOptionChkView.setOnCheckedChangeListener(new OnCheckedChangeListener(){
					@Override
					public void onCheckedChanged(CompoundButton view, boolean chk) {
						int pos = (Integer) view.getTag();
						isCheckedConfrimHashMap.put(pos, chk);
						if (chk)
							mCheckCount++;
						else
							mCheckCount--;
						if (mCheckCount < 0)
							mCheckCount = 0;
						if (mCheckedListener != null) {
							mCheckedListener.onChecked(pos, mCheckCount);
						}
					}
				});
	    	} else {
	    		viewHolder.mOptionChkView.setVisibility(View.INVISIBLE);
	    	}

    		
    		setFavoriteImage(viewHolder.mFileImgView,cursor.getInt(FavoriteData.INDEX_TYPE));
    		
    		String path = cursor.getString(FavoriteData.INDEX_PATH);
    		path = path.substring(path.lastIndexOf("/") + 1);
    		if (cursor.getInt(FavoriteData.INDEX_TYPE) == KConfig.TYPE_MUSIC) {
    			viewHolder.mDescTitleView.setVisibility(View.VISIBLE);
    			List<File> files = FileUtils.getDirFileList(cursor.getString(FavoriteData.INDEX_PATH));
    			int count = 0;
    			if (files != null) {
	    			for(File file:files) {
	    				if (FileUtils.getExtension(file.getName()).equalsIgnoreCase("mp3")) {
	    					count++;
	    				}
	    			}
    			}
    			viewHolder.mDescTitleView.setText("(" + count + ")");
    			if (count > 0) {
    				viewHolder.mFunctionButton.setTag(cursor.getString(FavoriteData.INDEX_PATH));
    				viewHolder.mFunctionButton.setVisibility(View.VISIBLE);
    				viewHolder.mFunctionButton.setFocusable(false);
    				if (mService != null && mService.isFolderPlaying(cursor.getString(FavoriteData.INDEX_PATH)))
    					viewHolder.mFunctionButton.setBackgroundResource(R.drawable.h_media_pause);
    				else
    					viewHolder.mFunctionButton.setBackgroundResource(R.drawable.h_media_play);
    				
    				if (mPlayPath != null && mPlayPath.equalsIgnoreCase(cursor.getString(FavoriteData.INDEX_PATH))) {
    					viewHolder.mFunctionButton.setBackgroundResource(R.drawable.h_media_pause);
    				}
    						
    				viewHolder.mFunctionButton.setOnClickListener(new OnClickListener(){
						@Override
						public void onClick(View view) {
							String path = (String) view.getTag();
							if (mService.getPlayPath() != null && path.equals(mService.getPlayPath()) &&
								mService.getMediaPlayer() != null && mService.getMediaPlayer().isPlaying()) {
								mService.pause();
								((Button)view).setBackgroundResource(R.drawable.h_media_play);
							} else {
								ArrayList<String> playList = new ArrayList<String>();
								List<File> files = FileUtils.getDirFileList(path);
								for(File file:files) {
									if (file.isFile() && FileUtils.getExtension(file.getName()).equalsIgnoreCase("mp3")) {
										playList.add(file.getPath());
									}
								}
								Collections.sort(playList, new SubStringComparator());
								if (playList.size() > 0) {
									Intent intent = new Intent(getContext(), MusicService.class);
									intent.putExtra(MusicService.EXTRA_MUSIC_FILE_LIST, playList);
									intent.putExtra(MusicService.EXTRA_MUSIC_FOLDER_PATH, path);
									getContext().startService(intent);
								}
								
								for (int i=0;i<FavoriteList.this.getChildCount();i++) {
									View v = FavoriteList.this.getChildAt(i);
									if (v.getTag() instanceof ViewHolder) {
										ViewHolder holder = (ViewHolder) v.getTag();
										if (holder.mFunctionButton != null && holder.mFunctionButton.getVisibility() == View.VISIBLE) {
											holder.mFunctionButton.setBackgroundResource(R.drawable.h_media_play);
										}
									}
								}
								
								((Button)view).setBackgroundResource(R.drawable.h_media_pause);
							}
						}
					});
    			} else {
    				viewHolder.mFunctionButton.setVisibility(View.INVISIBLE);
    			}
    		} else {
    			viewHolder.mDescTitleView.setVisibility(View.GONE);
    			viewHolder.mFunctionButton.setVisibility(View.GONE);
    		}
    		viewHolder.mTitleView.setText(path);
		}

		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {
			viewHolder = new ViewHolder();
			
			LinearLayout listMain = (LinearLayout) inflate(context, R.layout.file_list_item, null);
            listMain.setTag(viewHolder);
            
            viewHolder.mOptionChkView = (CheckBox) listMain.findViewById(R.id.favo_chk);
        	viewHolder.mFileImgView = (ImageView) listMain.findViewById(R.id.file_img);
			viewHolder.mTitleView = (TextView) listMain.findViewById(R.id.file_title);
			viewHolder.mDescTitleView = (TextView) listMain.findViewById(R.id.file_count);
			viewHolder.mFunctionButton = (Button) listMain.findViewById(R.id.file_func);
			return listMain;
		}
	    
	    @Override
	    public View getView(int position, View convertView, ViewGroup parent) {
	        View v;
	        Cursor cursor = getCursor();
	        cursor.moveToFirst();
	        
	        int realPosition = position;
	        if (!cursor.moveToPosition(realPosition)) {
	            throw new IllegalStateException("couldn't move cursor to position " + position);
	        }
	        if (convertView == null) {
	            v = newView(mContext, cursor, parent);
	        } else {
	            v = convertView;
	        }
	        bindView(v, mContext, cursor);
	        return v;
	    }
	    
	    private void setFavoriteImage(ImageView view, int viewType) {
	    	if (viewType == KConfig.TYPE_IMAGE) {
	    		view.setImageResource(R.drawable.z_folder_library_press);
	    	} else if (viewType == KConfig.TYPE_MOVIE) {
	    		view.setImageResource(R.drawable.z_folder_movie_press);
	    	} else if (viewType == KConfig.TYPE_TEXT) {
	    		view.setImageResource(R.drawable.z_folder_bookmark_press);
	    	} else if (viewType == KConfig.TYPE_MUSIC) {
	    		view.setImageResource(R.drawable.z_folder_music_press);
	    	} else {
	    		view.setImageResource(R.drawable.file);
	    	}
	    }
	}
}
