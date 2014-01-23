package com.cobrain.android.adapters;

import java.util.List;

import com.cobrain.android.R;
import com.cobrain.android.views.StateFullView;
import com.cobrain.android.views.StateFullView.OnStateChangedListener;

import android.content.Context;
import android.graphics.PorterDuff;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class CravesCategoryAdapter extends BaseAdapter {

	private AdapterView parent;
	private List<NavigationMenuItem> items;
	Context context;
	int layoutId =  R.layout.list_item_navigation;
	int captionLayoutId = 0;

	public CravesCategoryAdapter(Context context, List<NavigationMenuItem> items) {
		this.items = items;
		this.context = context;
		//super(context, R.layout.list_item_navigation, R.id.navigation_menu_caption, items);
	}

	public class MyAdapter extends AdapterView {

		public MyAdapter(Context context) {
			super(context);
		}

		@Override
		public Adapter getAdapter() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void setAdapter(Adapter adapter) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public View getSelectedView() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void setSelection(int position) {
			// TODO Auto-generated method stub
			
		}
		
	}
	
	public AdapterView getParent() {
		return parent;
	}
	
	public void setLayoutId(int layoutId) {
		this.layoutId = layoutId;
	}
	public void setCaptionLayoutId(int layoutId) {
		this.captionLayoutId = layoutId;
	}
	
	@Override
	public long getItemId(int position) {
		return items.get(position).id;
		//return getItem(position).id;
	}
	
	public void clear() {
		parent = null;
		items.clear();
	}

	OnTouchListener listenerx = new OnTouchListener() {

		@Override
		public boolean onTouch(View v, MotionEvent event) {

			if(event.getAction() == MotionEvent.ACTION_DOWN && v.isEnabled()) {
	            v.setPressed(true);
	            v.setSelected(true);
			}

	        if(event.getAction() == MotionEvent.ACTION_UP) {
	            v.setSelected(false);
	            v.setPressed(false);
	            int position = (Integer) v.getTag(); //parent.getPositionForView(v);
	            long id = getItemId(position);
	            parent.performItemClick(v, position, id);	            	
	        }

	        return true;
			//return true;
		}
		
	};

	private OnStateChangedListener statelistener = new OnStateChangedListener() {
		
		@Override
		public void onStateChanged(View v, int state, boolean enabled) {
			if (state == StateFullView.STATE_PRESSED) {
				ImageView iv = (ImageView) v.findViewById(R.id.navigation_menu_icon);
	            if (enabled) 
	            	iv.setColorFilter(0xff000000, PorterDuff.Mode.MULTIPLY); //your color here
	            else
		            iv.setColorFilter(null);
			}
		}
	};

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (this.parent == null) {
			if (parent instanceof AdapterView) {
				this.parent = (AdapterView) parent;
			}
			else {
				this.parent = new MyAdapter(context);
			}
		}

		View v = convertView;
		
		if (v == null) {
			if (captionLayoutId != 0) v = View.inflate(context, captionLayoutId, null);
			else v = new TextView(context);
		}

		NavigationMenuItem item = getItem(position);
		((TextView)v).setText(item.label);

		return v;
	}

	@Override
	public boolean isEnabled(int position) {
		return items.get(position).id != 0;
	}

	@Override
	public View getDropDownView(int position, View convertView, ViewGroup parent) {
		StateFullView v = (StateFullView) convertView;
		
		if (this.parent == null) this.parent = (AdapterView) parent;
		
		if (v == null) {
			v = (StateFullView) View.inflate(context, layoutId, null);
			//v.setOnTouchListener(listener);
			v.setOnStateChangeListener(statelistener);
			if (v.getBackground() == null)
				v.setBackground(context.getResources().getDrawable(R.drawable.navigation_menu_selector));
		}
		
		ImageView iv = (ImageView) v.findViewById(R.id.navigation_menu_icon);
		NavigationMenuItem nmi = items.get(position);
		iv.setImageDrawable(nmi.icon);

		TextView tv = (TextView) v.findViewById(R.id.navigation_menu_caption);
		tv.setText(nmi.caption);
		
		v.setTag(position);
		
		return v;
	}
	  
	@Override
	public int getCount() {
		return items.size();
	}

	@Override
	public NavigationMenuItem getItem(int position) {
		return items.get(position);
	}

}
