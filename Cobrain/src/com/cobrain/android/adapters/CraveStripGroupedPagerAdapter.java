package com.cobrain.android.adapters;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.text.Html;
import android.text.Spannable;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import com.cobrain.android.MiniFragment;
import com.cobrain.android.R;
import com.cobrain.android.fragments.CraveStripFragment;
import com.cobrain.android.fragments.CravesFragment;
import com.cobrain.android.model.Product;
import com.cobrain.android.model.RecommendationsResults;

public class CraveStripGroupedPagerAdapter extends MiniFragmentPagerAdapter {

	final int COUNT_PER_PAGE = 3;

	private int page = 1;
	private int perPage;
	private int countOnThisPage;
	private int count;
	private List<Product> recommendations;
	private CravesFragment parentFragment;
	private boolean destroyAll;
	private RecommendationsResults results;
	private HashMap<Integer, CraveStripFragment> fragments = new HashMap<Integer, CraveStripFragment>();
	Activity activity;

	public CraveStripGroupedPagerAdapter(Activity activity, CravesFragment cravesFragment) {
		super(activity);
		parentFragment = cravesFragment;
	}

	public void clear() {
		destroyAll = true;
		notifyDataSetChanged();
		destroyAll = false;
	}
	
	public void dispose() {
		if (recommendations != null) {
			recommendations.clear();
			recommendations = null;
		}
		clear();
		results = null;
		parentFragment = null;
	}
	
	public void load(RecommendationsResults r) {
		if (r != null) {
			page = r.getPage();
			perPage = r.getPerPage();
			countOnThisPage = r.getCount();
			count = r.getTotal();
			countOnThisPage = Math.min(countOnThisPage, count);

			List<Product> products = r.getProducts();
			int position = (page - 1) * perPage;

			if (recommendations == null) 
				recommendations = new ArrayList<Product>(position + products.size());

			if (recommendations.size() == 0 && position == 0) {
				recommendations.addAll(products);
			}
			else if (recommendations.size() <= position) {
				while (recommendations.size() < position)
					recommendations.add(null);

				recommendations.addAll(position, products);
			}
			else {
				while (recommendations.size() < countOnThisPage)
					recommendations.add(null);

				for (int i = position, in = 0; in < countOnThisPage; in++) {
					recommendations.set(i++, products.get(in));
				}
			}
		}
		else {
			page = 1;
			count = 0;
			if (recommendations != null) {
				recommendations.clear();
				recommendations = null;
			}
		}
		results = r;
		clear();
	}
	
	public int getMaxPages() {
		double d = Math.ceil(count/(double)perPage);
		return (int)d;
	}

	@Override
	public void destroyItem(ViewGroup container, int position, Object object) {
		fragments.remove(position);
		super.destroyItem(container, position, object);
	}

	
	@Override
	public MiniFragment getSubItem(MiniFragment parent, int position) {
		CraveStripFragment f = new CraveStripFragment(getActivity(),  parentFragment);
		
		f.create((ViewGroup) parent.getView());

		View v = parent.getView();
		if (v instanceof LinearLayout) {
			LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(0, LayoutParams.MATCH_PARENT);
			llp.weight = 1;
			f.getView().setLayoutParams(llp);
		}

		//fragments.put(position, f);

		if (recommendations == null) 
			return f;
		
		if (recommendations.size() <= position)
			return f;
		
		f.setRecommendation(results, recommendations.get(position));
		
		//if (position == 0 && parentFragment.cravePager.getCurrentItem() == position) updateTitle(position);
		
		return f;
	}

	@Override
	public MiniFragment getItem(int position) {
		GroupMiniFragment f = new GroupMiniFragment(getActivity());
		return f;
	}
	
	public class GroupMiniFragment extends MiniFragment {
		public GroupMiniFragment(Activity a) {
			super(a);
		}

		@Override
		public View onCreateView(Bundle inState, LayoutInflater inflater,
				ViewGroup container) {
			LinearLayout ll = new LinearLayout(inflater.getContext());
			ll.setOrientation(LinearLayout.HORIZONTAL);
			ViewPager vp = (ViewPager) container;
			int margin = vp.getPageMargin();
			if (margin < 0) {
				ll.setPadding(-margin/2, 0, -margin/2, 0);
			}
			return ll;
		}
		
	}
	
	/*@Override
	public float getPageWidth(int position) {
		return 0.95f;
	}*/

	public void updateTitle(int position) {
		if (results != null) {
			int totalCraves = results.getTotal();
			
			//final TextView txt = rankForYouLabel;
			String s;
			
			if (recommendations.size() > position && count > position && recommendations.get(position) != null) 
				s = parentFragment.getString(R.string.rank_for_you,
						recommendations.get(position).getRank(), 
						totalCraves
						);
			else
				s = parentFragment.getString(R.string.rank_for_you_empty);

			final TextView txt = parentFragment.getCobrainController().getSubTitleView();
			txt.setVisibility(View.VISIBLE);
			txt.setText(Html.fromHtml(s));
			txt.setMovementMethod(LinkMovementMethod.getInstance());
			
			Spannable buf = (Spannable) txt.getText();
			ForegroundColorSpan[] spans = buf.getSpans(0, txt.length(), ForegroundColorSpan.class);
			int start = buf.getSpanStart(spans[0]);
			int end = buf.getSpanEnd(spans[0]);
			final int linkColor = parentFragment.getResources().getColor(R.color.SeaGreen);
			
			ClickableSpan cs = new ClickableSpan() {
				@Override
				public void onClick(View widget) {
					//parentFragment.showTeachMyCobrain();
				}

				@Override
				public void updateDrawState(TextPaint ds) {
					ds.linkColor = linkColor;
					super.updateDrawState(ds);
					ds.setUnderlineText(false);
					ds.setFakeBoldText(true);
				}
			};
			
			buf.setSpan(cs, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
		}
		
	}
	
	@Override
	public int getItemPosition(Object object) {
		if (destroyAll) return POSITION_NONE;
		return super.getItemPosition(object);
	}
	
	@Override
	public int getCountPerPage() {
		return COUNT_PER_PAGE;
	}

	@Override
	public int getTotalCount() {
		//return count;
		
		if (recommendations == null) return 0;
		
		int cnt = ((page-1) * perPage) + countOnThisPage;

		cnt = Math.max(recommendations.size(), cnt);
		cnt = Math.min(cnt, count);
		if (cnt < 0) cnt = 0;
		
		return cnt;
	}
	
	//public CraveStripFragment getPage(int position) {
	//	return fragments.get(position);
	//}

}