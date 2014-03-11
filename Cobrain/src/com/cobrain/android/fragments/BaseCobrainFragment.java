package com.cobrain.android.fragments;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.AsyncTask.Status;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ListView;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragment;
import com.cobrain.android.controllers.Cobrain.CobrainController;
import com.cobrain.android.controllers.Cobrain.CobrainView;
import com.cobrain.android.utils.LoaderUtils;

public class BaseCobrainFragment extends SherlockFragment implements OnClickListener, CobrainView {
	public CobrainController controller;
	public LoaderUtils loaderUtils = new LoaderUtils();
	ActionBar actionBar;
	View abHide;
	StateSaver state = new StateSaver();
	boolean silentMode = true;
	HashMap<String, AsyncTask> asyncTasks = new HashMap<String, AsyncTask>();
	Field childFragmentManagerField;
	boolean detached;
	public boolean autoUpdate = true;
	public BaseCobrainFragment parent;

	public BaseCobrainFragment() {
		try {
	        childFragmentManagerField = Fragment.class.getDeclaredField("mChildFragmentManager");
	        childFragmentManagerField.setAccessible(true);
	    }catch (Exception e){
	    	e.printStackTrace();
	    }
	}

	public AsyncTask addAsyncTask(String key, AsyncTask asyncTask) {
		asyncTasks.put(key, asyncTask);
		return asyncTask;
	}
	public void cancelAsyncTask(String key) {
		AsyncTask task = asyncTasks.remove(key);
		if (task != null) {
			if (task.getStatus() != Status.FINISHED) {
				task.cancel(true);
			}
		}
	}

	public class StateSaver {
		Bundle savedState = new Bundle();
		
		public int getInt(String key, int def) {
			if (!savedState.containsKey(key)) return def;
			int i = savedState.getInt(key);
			savedState.remove(key);
			return i;
		}

		public String getString(String key, String def) {
			if (!savedState.containsKey(key)) return def;
			String i = savedState.getString(key);
			savedState.remove(key);
			return i;
		}
		
		public boolean getBoolean(String key, boolean def) {
			if (!savedState.containsKey(key)) return def;
			boolean i = savedState.getBoolean(key);
			savedState.remove(key);
			return i;
		}

		public void putInt(String key, int val) {
			savedState.putInt(key, val);
		}

		public void putString(String key, String val) {
			savedState.putString(key, val);
		}
		
		public void putBoolean(String key, boolean val) {
			savedState.putBoolean(key, val);
		}

		public Bundle getBundle() {
			return savedState;
		}

		public void restore(ListView list, String key) {
			int index = getInt(key + ".index", 0);
			int top = getInt(key + ".top", 0);
			if (list != null) {
				if (list.getCount() > 0)
					list.setSelectionFromTop(index, top);
			}
		}
		public void save(ListView list, String key) {
			int index = list.getFirstVisiblePosition();
			View v = list.getChildAt(0);
			int top = (v == null) ? 0 : v.getTop();
			putInt(key + ".index", index);
			putInt(key + ".top", top);
		}
	}

	private Runnable updateRunnable = new Runnable() {
		public void run() {
			update();
		}
	};
	
	public int getMenuItemId() {
		return -1;
	}
	
	Handler handler;

	public Handler getHandler() {
		if (handler == null) handler = new Handler();
		return handler;
	}
	
	public void update() {
		boolean updated = onUpdate();
		if (!updated) {
			//TODO: if not updated try again in 2 seconds
			getHandler().postDelayed(updateRunnable, 2 * 1000);
		}
	}

	protected boolean onUpdate() {
		return true;
	}

	@Override
	public void onAttach(Activity activity) {
		controller = (CobrainController) activity;
		controller.showOptionsMenu(true);
		detached = false;
		actionBar = controller.getSupportActionBar();
		abHide = new View(activity.getApplicationContext());
		controller.dispatchOnFragmentAttached(this);
		int menuItemId = getMenuItemId();
		if (menuItemId != -1) {
			controller.setMenuItemSelected(String.valueOf(menuItemId));
		}
		super.onAttach(activity);
	}

	@Override
	public void onError(CharSequence message) {
		if (loaderUtils != null) loaderUtils.dismiss();
		if (!silentMode) if (controller != null) controller.showErrorDialog(message);
	}
	
	public LoaderUtils getLoaderUtils() {
		return loaderUtils;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		if (autoUpdate)
			update();

		super.onActivityCreated(savedInstanceState);
	}

	@Override
	public void setTitle(CharSequence title) {
		if (parent != null) parent.setTitle(this, title);
		else controller.setTitle(title);
	}

	public void setTitle(BaseCobrainFragment child, CharSequence title) {
		controller.setTitle(title);
	}
	public void setSubTitle(BaseCobrainFragment child, CharSequence title) {
		controller.setSubTitle(title);
	}

	@Override
	public void setSubTitle(CharSequence title) {
		if (parent != null) parent.setSubTitle(this, title);
		else controller.setSubTitle(title);
	}
	@Override
	public CobrainController getCobrainController() {
		return controller;
	}
	
	void hideActionBar() {
		//wasActionBarShown = actionBar.isShowing();
/*		actionBar.setCustomView(abHide);
		actionBar.setDisplayShowTitleEnabled(false);
		actionBar.setDisplayShowCustomEnabled(true);
		actionBar.setDisplayShowHomeEnabled(false);
		actionBar.setDisplayHomeAsUpEnabled(false);
		controller.showOptionsMenu(false);
*/
		actionBar.hide();
	}
	
	void restoreActionBar() {
		//actionBar.setDisplayShowCustomEnabled(false);
		//if (wasActionBarShown) actionBar.show();
	}
	
	@Override
	public void onPause() {
		super.onPause();
		
		for (@SuppressWarnings("rawtypes") AsyncTask task : asyncTasks.values()) {
			if (task.getStatus() != Status.FINISHED) {
				task.cancel(true);
			}
		}
		asyncTasks.clear();
	}
	
	@Override
	public void onDetach() {
		detached = true;
		handler = null;
		if (actionBar.getCustomView() == abHide) actionBar.setCustomView(null);
		controller.dispatchOnFragmentDetached(this);
		abHide = null;
		controller = null;
		actionBar = null;
		childFragmentManagerField = null;
		parent = null;
		
		super.onDetach();
	}

	List<Fragment> getChildFragments() {

		//try to get rid of random "No Activity" error that seems to occur when using viewpagers!
        // dont want to create childfragmentmanager instances if we arent attaching any children to them!
		if (childFragmentManagerField == null) return null;
		
		try {
            FragmentManager fm = (FragmentManager) childFragmentManagerField.get(this);
            if (fm != null) return fm.getFragments();
        }catch (Exception e){
        	e.printStackTrace();
        }
		
		return null;
	}
	
	@Override
	public void onClick(View v) {
	}

	@Override
	public void onSlidingMenuOpened() {
	}

	@Override
	public void onSlidingMenuClosed() {
	}
	
	@Override
	public void setSilentMode(boolean silent) {
		silentMode = silent;
	}

	public void onFragmentDetached(BaseCobrainFragment f) {}
	public void onFragmentAttached(BaseCobrainFragment f) {}
	
	public void dispatchOnFragmentDetached(BaseCobrainFragment f) {
		onFragmentDetached(f);
		
		List<Fragment> fragments = getChildFragments();
		if (fragments != null) {
			for (Fragment fragment : fragments) {
				if (f != fragment && fragment instanceof BaseCobrainFragment) {
					BaseCobrainFragment bf = (BaseCobrainFragment)fragment;
					bf.dispatchOnFragmentDetached(f);
				}
			}
		}
	}
	public void dispatchOnFragmentAttached(BaseCobrainFragment f) {
		onFragmentAttached(f);

		List<Fragment> fragments = getChildFragments();
		if (fragments != null) {
			for (Fragment fragment : fragments) {
				if (f != fragment && fragment instanceof BaseCobrainFragment) {
					BaseCobrainFragment bf = (BaseCobrainFragment)fragment;
					bf.dispatchOnFragmentAttached(f);
				}
			}
		}
	}

	@Override
	public boolean getSilentMode() {
		return silentMode;
	}

	@Override
	public boolean onBackPressed() {
		return false;
	}

}
