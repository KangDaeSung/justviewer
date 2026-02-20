package com.kds3393.just.viewer.Browser;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.common.utils.FileUtils;
import com.common.utils.Size;
import com.kds3393.just.viewer.R;
import com.kds3393.just.viewer.Config.KConfig;
import com.kds3393.just.viewer.Utils.FileExtract;
import com.kds3393.just.viewer.Utils.SubStringComparator;

public class BrowserView extends ListView{
	private static final String TAG = "BrowserView";
	
	private String rootPath;
	private ArrayList<ZipArchiveEntry> mZipEntrys;
	private FileAdapter mAdapter;
	private int mListType = KConfig.TYPE_FAVORITE;

	private OnSelectListener mSelectListener = null;
	public interface OnSelectListener {
		public void onSelect(String path);
	};
	public void setOnSelectListener(OnSelectListener listnener) {
		mSelectListener = listnener;
	}
	
	private OnFileCheckedListener mCheckedListener = null;
	public interface OnFileCheckedListener {
		public void onChecked(int checkPos, int checkCount);
	};
	
	public void setOnFileCheckedListener(OnFileCheckedListener listnener) {
		mCheckedListener = listnener;
	}
	
	public BrowserView(Context context) {
		super(context);
		mAdapter = new FileAdapter(context);
        this.setAdapter(mAdapter);
	}
	
	public BrowserView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mAdapter = new FileAdapter(context);
        this.setAdapter(mAdapter);
	}
	
	public void setPath(String path) {
		File file = new File(path);
		while(!file.exists()) {
			file = new File(file.getParent());
		}
		rootPath = file.getPath();
		if (rootPath.lastIndexOf("zip") > 0) {
			setZipItem(FileExtract.GetZipEntry(path));
		} else {
			setItem(FileUtils.getDirFileList(path));
		}
		
	}
	
	public void unCheckAllItem() {
		for (int i=0;i<isCheckedConfrim.length;i++) {
			isCheckedConfrim[i] = false;
		}
	}
	
	public void reload() {
		setItem(FileUtils.getDirFileList(rootPath));
	}
	public String getFolderName() {
		if (TextUtils.isEmpty(rootPath))
			return "";
		return rootPath.substring(rootPath.lastIndexOf("/"), rootPath.length());
	}
	public String getPath() {
		return rootPath;
	}
	
	public void moveParentPath() {
		if (TextUtils.isEmpty(rootPath) || isRootPath(rootPath)) {
			return;
		}
		
		File root = new File(rootPath);
		if (FileUtils.getExtension(rootPath).equals("zip"))
			setPath(root.getAbsolutePath().substring(0, root.getAbsolutePath().lastIndexOf("/")));
		else
			setPath(root.getParent());
	}
	
	public void setType(int type) {
		mListType = type;
	}
	
	private boolean isRootPath(String path) {
		return "/".equals(rootPath);
	}
	public void setItem(List<File> datas) {
		mAdapter.clear();
		ArrayList<String> directorys = new ArrayList<String>();
		ArrayList<String> files = new ArrayList<String>();
		if (datas != null) {
	        for(File file : datas ) {
	        	if (file.isHidden()) {
	        		continue;
	        	}
	        	if (file.isDirectory()) {
	        		directorys.add(file.getAbsolutePath());
	        	} else {
	        		boolean isAdd = false;
	        		if (mListType == KConfig.TYPE_BROWSER) {
	        			isAdd = true;
	        		} else if (mListType == KConfig.TYPE_IMAGE) {
		        		if (FileUtils.getExtension(file.getName()).equalsIgnoreCase("zip"))
		        			isAdd = true;
	        		} else if (mListType == KConfig.TYPE_MOVIE) {
		        		if (FileUtils.getExtension(file.getName()).equalsIgnoreCase("avi") ||
		        				FileUtils.getExtension(file.getName()).equalsIgnoreCase("mp4") ||
		        				FileUtils.getExtension(file.getName()).equalsIgnoreCase("wmv") ||
		        				FileUtils.getExtension(file.getName()).equalsIgnoreCase("mkv"))
		        			isAdd = true;
	        		} else if (mListType == KConfig.TYPE_MUSIC) {
		        		if (FileUtils.getExtension(file.getName()).equalsIgnoreCase("mp3"))
		        			isAdd = true;
	        		} else if (mListType == KConfig.TYPE_TEXT) {
	        			if (FileUtils.getExtension(file.getName()).equalsIgnoreCase("txt") ||
	        					FileUtils.getExtension(file.getName()).equalsIgnoreCase("smi") ||
	        					FileUtils.getExtension(file.getName()).equalsIgnoreCase("log"))
	        				isAdd = true;
	        		}
	        		if (isAdd) {
	        			files.add(file.getAbsolutePath());
	        		}
	        	}
	        }
		}
		
		if (!isRootPath(rootPath)) {
			mAdapter.add("..");
		}
		Collections.sort(directorys, new SubStringComparator());
		mAdapter.addAll(directorys);
		Collections.sort(files, new SubStringComparator());
		mAdapter.addAll(files);
		mAdapter.finishAddItem();
		mAdapter.notifyDataSetChanged();
	}
	
	public void setZipItem(ArrayList<ZipArchiveEntry> datas) {
		mAdapter.clear();
		mZipEntrys = datas;
		if (!isRootPath(rootPath)) {
			mAdapter.add("..");
		}
		if (mZipEntrys != null) {
			for (ZipArchiveEntry entry:mZipEntrys) {
				mAdapter.add(entry.getName());
			}
		}
		mAdapter.finishAddItem();
		mAdapter.notifyDataSetChanged();
	}
	
	public void setItem(ArrayList<String> datas) {
		mAdapter.clear();
		mAdapter.addAll(datas);
		mAdapter.notifyDataSetChanged();
	}
	
	private boolean[] isCheckedConfrim = null;
	public boolean[] getCheckedConfrim() {
		return isCheckedConfrim;
	}
	
	public void clearCheckedConfrim() {
		for (int i=0;i<isCheckedConfrim.length;i++) {
			isCheckedConfrim[i] = false;
		}
		mAdapter.notifyDataSetChanged();
	}
	
	public class FileAdapter extends ArrayAdapter<String> {
	    private static final String TAG = "BoomarkListAdapter";
	    public FileAdapter(Context context) {
	    	super(context,0);
	    }
	    
	    public void finishAddItem() {
	    	isCheckedConfrim = null;
	    	isCheckedConfrim = new boolean[getCount()];
	    }
        
	    class ViewHolder {
	    	public CheckBox mOptionChkView = null;
	    	public ImageView mFileImgView = null;
	    	public TextView mTitleView = null;
	    	public TextView mDescTitleView = null;
	    }
	    
	    private ViewHolder viewHolder = null;
	    @Override
	    public View getView(int position, View convertView, ViewGroup parent) {
	    	return getContent(position,convertView,parent);
	    }
	    
	    
	    private View getContent(int position, View convertView, ViewGroup parent) {
	    	LinearLayout listMain = (LinearLayout) convertView;
	    	
	    	String file = null;
	    	file = (String) getItem(position);
	    	
    		if (listMain == null) {
                viewHolder = new ViewHolder();
                
    			listMain = (LinearLayout) inflate(getContext(), R.layout.file_list_item, null);
                listMain.setTag(viewHolder);
                
                viewHolder.mOptionChkView = (CheckBox) listMain.findViewById(R.id.favo_chk);
            	viewHolder.mFileImgView = (ImageView) listMain.findViewById(R.id.file_img);
    			viewHolder.mTitleView = (TextView) listMain.findViewById(R.id.file_title);
    			viewHolder.mDescTitleView = (TextView) listMain.findViewById(R.id.file_count);
    			listMain.findViewById(R.id.file_func).setVisibility(View.GONE);
    		} else {
    			viewHolder = (ViewHolder) listMain.getTag();
    		}
    		if (isRootPath(rootPath)) {
    			viewHolder.mOptionChkView.setVisibility(View.INVISIBLE);
	    	} else {
	    		viewHolder.mOptionChkView.setVisibility(View.VISIBLE);
	    	}
    		
    		viewHolder.mOptionChkView.setTag(position);
    		viewHolder.mOptionChkView.setChecked(isCheckedConfrim[position]);
    		viewHolder.mOptionChkView.setFocusable(false);
    		viewHolder.mOptionChkView.setOnCheckedChangeListener(new OnCheckedChangeListener(){
				@Override
				public void onCheckedChanged(CompoundButton view, boolean chk) {
					int checkCount = 0;
					int pos = (Integer) view.getTag();
					isCheckedConfrim[pos] = chk;
					if (mCheckedListener != null) {
						for (boolean check:isCheckedConfrim) {
							if (check)
								checkCount++;
						}
						mCheckedListener.onChecked(pos, checkCount);
					}
				}
			});
    		if (file.equals("..")) {
    			viewHolder.mOptionChkView.setVisibility(View.INVISIBLE);
    			viewHolder.mFileImgView.setImageResource(R.drawable.back);
    			viewHolder.mTitleView.setText(FileUtils.getName(file));
    			setTextInfo(viewHolder.mTitleView,FileUtils.getName(file),viewHolder.mDescTitleView,null);
    			return listMain;
    		} else {
    			viewHolder.mOptionChkView.setVisibility(View.VISIBLE);
    		}
    		
    		viewHolder.mTitleView.setText(FileUtils.getName(file));
    		if ((new File(file)).isDirectory()) {
    			viewHolder.mFileImgView.setImageResource(R.drawable.file_folder);
    		} else {
    			setFileImage(viewHolder.mFileImgView,FileUtils.getExtension(file));
    		}
    		setTextInfo(viewHolder.mTitleView,FileUtils.getName(file),viewHolder.mDescTitleView,null);
	        return listMain;
	    }
	    
	    private void setTextInfo(TextView titleView, String title, TextView descView, String desc) {
	    	if (TextUtils.isEmpty(desc)) {
	    		viewHolder.mDescTitleView.setVisibility(View.GONE);
	    		viewHolder.mTitleView.setGravity(Gravity.CENTER_VERTICAL);
	    	} else {
	    		viewHolder.mDescTitleView.setVisibility(View.VISIBLE);
	    		viewHolder.mTitleView.setGravity(Gravity.TOP);
	    		descView.setText(desc);
	    	}
	    	titleView.setText(title);
	    }
	    private void setFileImage(ImageView view, String fileExtension) {
	    	if (fileExtension.equalsIgnoreCase(".")) {
	    		view.setImageResource(R.drawable.file_music);
	    	} else if (fileExtension.equalsIgnoreCase("zip")) {
	    		view.setImageResource(R.drawable.archive);
	    	} else if (fileExtension.equalsIgnoreCase("mp3")) {
	    		view.setImageResource(R.drawable.file_music);
	    	} else if (fileExtension.equalsIgnoreCase("jpg") ||
	    			fileExtension.equalsIgnoreCase("jpeg") ||
	    			fileExtension.equalsIgnoreCase("gif") ||
	    			fileExtension.equalsIgnoreCase("png")) {
	    		view.setImageResource(R.drawable.file_image);
	    	} else if (fileExtension.equalsIgnoreCase("mp4") ||
	    			fileExtension.equalsIgnoreCase("avi") ||
	    			fileExtension.equalsIgnoreCase("wmv") ||
	    			fileExtension.equalsIgnoreCase("mkv")) {
	    		view.setImageResource(R.drawable.file_movie);
	    	} else if (fileExtension.equalsIgnoreCase("txt") ||
	    			fileExtension.equalsIgnoreCase("smi") ||
	    			fileExtension.equalsIgnoreCase("log")
	    			) {
	    		view.setImageResource(R.drawable.file);
	    	}else {
	    		view.setImageResource(R.drawable.file);
	    	}
	    }
	}
}
