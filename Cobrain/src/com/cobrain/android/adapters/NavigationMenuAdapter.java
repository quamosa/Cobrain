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
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class NavigationMenuAdapter extends ArrayAdapter<NavigationMenuItem> {

	private ListView parent;
	private int layoutId = R.layout.list_item_navigation;

	public NavigationMenuAdapter(Context context,
			List<NavigationMenuItem> menuItems) {
		super(context, 0, menuItems);
	}

	public void setLayoutId(int layoutId) {
		this.layoutId = layoutId;
	}
	
	@Override
	public long getItemId(int position) {
		return getItem(position).id;
	}

	@Override
	public boolean isEnabled(int position) {
		return getItemId(position) >= 0;
	}

	OnTouchListener listener = new OnTouchListener() {

		@Override
		public boolean onTouch(View v, MotionEvent event) {

			if(event.getAction() == MotionEvent.ACTION_DOWN && v.isEnabled()) {
	            v.setPressed(true);
	            //v.setSelected(true);
			}

	        if(event.getAction() == MotionEvent.ACTION_UP) {
	            //v.setSelected(false);
	            v.setPressed(false);
	            int position = parent.getPositionForView(v);
	            long id = getItemId(position);
	            parent.performItemClick(v, position, id);
	        }

			return true;
		}
		
	};

	private OnStateChangedListener statelistener = new OnStateChangedListener() {
		
		@Override
		public void onStateChanged(View v, int state, boolean enabled) {
			if (state == StateFullView.STATE_PRESSED || state == StateFullView.STATE_CHECKED) {
				ImageView iv = (ImageView) v.findViewById(R.id.navigation_menu_icon);
	            if (enabled) 
	            	iv.setColorFilter(0xffffffff, PorterDuff.Mode.SRC_IN); //your color here
	            else
		            iv.setColorFilter(null);
			}
		}
	};

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		StateFullView v = (StateFullView) convertView;
		
		if (this.parent == null) this.parent = (ListView) parent;
		
		if (v == null) {
			v = (StateFullView) View.inflate(getContext(), layoutId, null);
			v.setOnTouchListener(listener);
			v.setOnStateChangeListener(statelistener);
			//v.setBackground(getContext().getResources().getDrawable(R.drawable.navigation_menu_selector));
		}
		
		ImageView iv = (ImageView) v.findViewById(R.id.navigation_menu_icon);
		NavigationMenuItem nmi = getItem(position);
		iv.setImageDrawable(nmi.icon);
		
		TextView tv = (TextView) v.findViewById(R.id.navigation_menu_caption);
		tv.setText(nmi.caption);
		
		return v;
	}

}
