package com.cobrain.android.views;

import java.util.ArrayList;
import java.util.List;

import com.cobrain.android.R;

import android.content.Context;
import android.database.DataSetObserver;
import android.graphics.Rect;
import android.support.v4.view.PagerAdapter;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.Scroller;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TabHost.TabContentFactory;
import android.widget.TabWidget;
import android.widget.TextView;

public class RepeatingTabHost extends HorizontalScrollView {
	private TabHost tabHost;
	private PagerAdapter adapter;
	private Scroller mScroller;
	private boolean init;
	Rect r = new Rect();
	private int currentTab;
	
	public RepeatingTabHost(Context context) {
		super(context);
	}

	public RepeatingTabHost(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public RepeatingTabHost(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}
	
	void setup() {
		if (tabHost == null) {
			mScroller = new Scroller(getContext());
			tabHost = (TabHost) getRootView().findViewById(android.R.id.tabhost);
			tabHost.setup();
		}
	}
	
	public void setOnTabChangedListener(OnTabChangeListener listener) {
		final OnTabChangeListener _listener = listener;
		setup();
		tabHost.setOnTabChangedListener(new OnTabChangeListener() {
			@Override
			public void onTabChanged(String tabId) {
				int i = Integer.parseInt(tabId);
				selectTab(i);
				_listener.onTabChanged(tabId);
			}
		});
	}
	
	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
	
		switch(ev.getAction()) {
		case MotionEvent.ACTION_DOWN:
			currentTab = tabHost.getCurrentTab();
			break;
			
		case MotionEvent.ACTION_UP:
			postDelayed(selectTabRunnable.set(ev.getX(), ev.getY()), 1000/30);
		}
		
		return super.onInterceptTouchEvent(ev);
	}

	SelectTabRunnable selectTabRunnable = new SelectTabRunnable();
	public class SelectTabRunnable implements Runnable {
		float x, y;
		
		public SelectTabRunnable set(float x, float y) {
			this.x = x;
			this.y = y;
			return this;
		}

		@Override
		public void run() {
			if (tabHost.getCurrentTab() == currentTab) {
				//if we didnt change selections with this click, then lets check to see if we clicked the current tab
				//if so lets trigger a selectTab to update its position
				tabHost.getTabWidget().getChildTabViewAt(currentTab).getHitRect(r);
				r.offset(-getScrollX(), -getScrollY());
				if (r.contains((int)x, (int)y)) {
					selectTab(currentTab);
				}
			}
		}
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		// TODO Auto-generated method stub
		return super.onTouchEvent(ev);
	}

	ArrayList<Integer> selectedTabs = new ArrayList<Integer>();
	
	void selectTab(int i) {
		View v;
		
		for (Integer tab : selectedTabs) {
			v = tabHost.getTabWidget().getChildAt(tab);
			v.setSelected(false);
		}
		selectedTabs.clear();
		
		int count = getTabWidget().getTabCount();
		if (i == 0) i = count / 2;
		v = tabHost.getTabWidget().getChildAt(i);
		v.getHitRect(r);
		
		if (!v.isSelected()) {
			v.setSelected(true);
			selectedTabs.add(i);
		}
		
		int halfCount = count / 2;
		if ((i += halfCount) >= count) i -= count;
		if (i < count) {
			v = tabHost.getTabWidget().getChildAt(i);
			if (!v.isSelected()) {
				v.setSelected(true);
				selectedTabs.add(i);
			}
		}

		HorizontalScrollView scroll = (HorizontalScrollView) tabHost.getTabWidget().getParent();
		int x = r.centerX() - scroll.getWidth() / 2;
		scroll.setScrollX(x);
	}
	
	public TabWidget getTabWidget() {
		return tabHost.getTabWidget();
	}
	public void setAdapter(PagerAdapter adapter) {
		setup();
		this.adapter = adapter;
		adapter.registerDataSetObserver(tabHostObserver);
	}

	private DataSetObserver tabHostObserver = new DataSetObserver() {

		@Override
		public void onChanged() {
			buildTabs();
			super.onChanged();
		}
	};
	TabContentFactory tabFactory = new TabContentFactory() {
		
		@Override
		public View createTabContent(String tag) {
			return new View(getContext());
		}
	};

	protected void buildTabs() {
		tabHost.clearAllTabs();
		_build();  
		_build();
		//tabHost.setCurrentTab(tabHost.getTabWidget().getTabCount() / 2);
	}
	private void _build() {
		for (int i = 0; i < adapter.getCount(); i++) {
			CharSequence title = adapter.getPageTitle(i);
			
			View v = 
				LayoutInflater.from(getContext()).inflate(R.layout.tabs_home_tab_indicator_holo, 
						tabHost.getTabWidget(), false);

			//TextView v = new TextView(getContext());
			//v.setText(title);
			
			TextView tv = (TextView) v.findViewById(android.R.id.title);
			tv.setText(title);

            /*final LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
            		ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.MATCH_PARENT);
            lp.setMargins(0, 0, 0, 0);
            v.setLayoutParams(lp);
            */
            
			//v.setPadding(60, 30, 60, 30);
			//v.setTextColor(Color.WHITE);
			//getTabWidget().setBackgroundColor(Color.WHITE);
			
			TabHost.TabSpec spec = tabHost.newTabSpec(String.valueOf(i))
		                .setIndicator(v)
		                .setContent(tabFactory);
		        tabHost.addTab(spec);
		}
	}
	
	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		super.onLayout(changed, l, t, r, b);
		if (!init) {
			init = true;
			if (tabHost.getCurrentTab() == 0) {
				tabHost.setCurrentTab( tabHost.getTabWidget().getTabCount() / 2 );
			}
		}
	}

	@Override
    public void fling(int velocityX) {
        if (getChildCount() > 0) {
            mScroller.fling(getScrollX(), getScrollY(), velocityX, 0, Integer.MIN_VALUE,
                    Integer.MAX_VALUE, 0, 0);

            final boolean movingRight = velocityX > 0;

            View currentFocused = findFocus();
            View newFocused = findFocusableViewInMyBounds(movingRight,
                    mScroller.getFinalX(), currentFocused);

            if (newFocused == null) {
                newFocused = this;
            }

            if (newFocused != currentFocused) {
                newFocused.requestFocus(movingRight ? View.FOCUS_RIGHT : View.FOCUS_LEFT);
            }

            postInvalidateOnAnimation();
        }
    }
	
    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {
            // This is called at drawing time by ViewGroup.  We don't want to
            // re-show the scrollbars at this point, which scrollTo will do,
            // so we replicate most of scrollTo here.
            //
            //         It's a little odd to call onScrollChanged from inside the drawing.
            //
            //         It is, except when you remember that computeScroll() is used to
            //         animate scrolling. So unless we want to defer the onScrollChanged()
            //         until the end of the animated scrolling, we don't really have a
            //         choice here.
            //
            //         I agree.  The alternative, which I think would be worse, is to post
            //         something and tell the subclasses later.  This is bad because there
            //         will be a window where mScrollX/Y is different from what the app
            //         thinks it is.
            //
            int oldX = getScrollX();
            int oldY = getScrollY();
            int x = mScroller.getCurrX();
            int y = mScroller.getCurrY();

            if (oldX != x || oldY != y) {

                //overScrollBy(x - oldX, y - oldY, oldX, oldY, range, 0,
                //        mOverflingDistance, 0, false);
                scrollBy(x - oldX, y - oldY);
            	
                //onScrollChanged(mScrollX, mScrollY, oldX, oldY);
            }

            if (!awakenScrollBars()) {
                postInvalidateOnAnimation();
            }
        }
        else super.computeScroll();
    }	
    
    int getChildWidth() {
    	TabWidget tw = tabHost.getTabWidget();
    	int count = tw.getChildCount();
    	if (count == 0) {
    		return 0;
    	}
		int w = tw.getChildTabViewAt( count / 2 ).getLeft();
		
		return w;
    }
        
    @Override
	protected void onScrollChanged(int l, int t, int oldl, int oldt) {
		int w = getChildWidth();
		boolean ok = false;

		if (w != 0) {
			while (l > w) {
				l -= w;
				ok = true;
			}
			
			while (l <= 0) {
				l += w;
				ok = true;
			}
		}
    	
		if (ok) scrollTo(l, t);
		else super.onScrollChanged(l, t, oldl, oldt);
	}

	@Override
	public void scrollBy(int x, int y) {
    	x += getScrollX();
    	y += getScrollY();

		int w = getChildWidth();
		
		while (x > w) {
			x -= w;
		}
		while (x <= 0) {
			x += w;
		}

        scrollTo(x, y);
	}

    @Override
    protected boolean overScrollBy(int deltaX, int deltaY,
            int scrollX, int scrollY,
            int scrollRangeX, int scrollRangeY,
            int maxOverScrollX, int maxOverScrollY,
            boolean isTouchEvent) {

        int newScrollX = scrollX + deltaX;
        int newScrollY = scrollY + deltaY;

        onOverScrolled(newScrollX, newScrollY, false, false);

        return false;
    }

	/**
     * <p>
     * Finds the next focusable component that fits in this View's bounds
     * (excluding fading edges) pretending that this View's left is located at
     * the parameter left.
     * </p>
     *
     * @param leftFocus          look for a candidate is the one at the left of the bounds
     *                           if leftFocus is true, or at the right of the bounds if leftFocus
     *                           is false
     * @param left               the left offset of the bounds in which a focusable must be
     *                           found (the fading edge is assumed to start at this position)
     * @param preferredFocusable the View that has highest priority and will be
     *                           returned if it is within my bounds (null is valid)
     * @return the next focusable component in the bounds or null if none can be found
     */
    private View findFocusableViewInMyBounds(final boolean leftFocus,
            final int left, View preferredFocusable) {
        /*
         * The fading edge's transparent side should be considered for focus
         * since it's mostly visible, so we divide the actual fading edge length
         * by 2.
         */
        final int fadingEdgeLength = getHorizontalFadingEdgeLength() / 2;
        final int leftWithoutFadingEdge = left + fadingEdgeLength;
        final int rightWithoutFadingEdge = left + getWidth() - fadingEdgeLength;

        if ((preferredFocusable != null)
                && (preferredFocusable.getLeft() < rightWithoutFadingEdge)
                && (preferredFocusable.getRight() > leftWithoutFadingEdge)) {
            return preferredFocusable;
        }

        return findFocusableViewInBounds(leftFocus, leftWithoutFadingEdge,
                rightWithoutFadingEdge);
    }

    /**
     * <p>
     * Finds the next focusable component that fits in the specified bounds.
     * </p>
     *
     * @param leftFocus look for a candidate is the one at the left of the bounds
     *                  if leftFocus is true, or at the right of the bounds if
     *                  leftFocus is false
     * @param left      the left offset of the bounds in which a focusable must be
     *                  found
     * @param right     the right offset of the bounds in which a focusable must
     *                  be found
     * @return the next focusable component in the bounds or null if none can
     *         be found
     */
    private View findFocusableViewInBounds(boolean leftFocus, int left, int right) {

        List<View> focusables = getFocusables(View.FOCUS_FORWARD);
        View focusCandidate = null;

        /*
         * A fully contained focusable is one where its left is below the bound's
         * left, and its right is above the bound's right. A partially
         * contained focusable is one where some part of it is within the
         * bounds, but it also has some part that is not within bounds.  A fully contained
         * focusable is preferred to a partially contained focusable.
         */
        boolean foundFullyContainedFocusable = false;

        int count = focusables.size();
        for (int i = 0; i < count; i++) {
            View view = focusables.get(i);
            int viewLeft = view.getLeft();
            int viewRight = view.getRight();

            if (left < viewRight && viewLeft < right) {
                /*
                 * the focusable is in the target area, it is a candidate for
                 * focusing
                 */

                final boolean viewIsFullyContained = (left < viewLeft) &&
                        (viewRight < right);

                if (focusCandidate == null) {
                    /* No candidate, take this one */
                    focusCandidate = view;
                    foundFullyContainedFocusable = viewIsFullyContained;
                } else {
                    final boolean viewIsCloserToBoundary =
                            (leftFocus && viewLeft < focusCandidate.getLeft()) ||
                                    (!leftFocus && viewRight > focusCandidate.getRight());

                    if (foundFullyContainedFocusable) {
                        if (viewIsFullyContained && viewIsCloserToBoundary) {
                            /*
                             * We're dealing with only fully contained views, so
                             * it has to be closer to the boundary to beat our
                             * candidate
                             */
                            focusCandidate = view;
                        }
                    } else {
                        if (viewIsFullyContained) {
                            /* Any fully contained view beats a partially contained view */
                            focusCandidate = view;
                            foundFullyContainedFocusable = true;
                        } else if (viewIsCloserToBoundary) {
                            /*
                             * Partially contained view beats another partially
                             * contained view if it's closer
                             */
                            focusCandidate = view;
                        }
                    }
                }
            }
        }

        return focusCandidate;
    }

	public void setCurrentTab(int position) {
		tabHost.setCurrentTab(position);
	}	
}
