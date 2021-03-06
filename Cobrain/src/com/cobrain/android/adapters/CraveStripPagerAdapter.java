package com.cobrain.android.adapters;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.Activity;
import android.text.Html;
import android.text.Spannable;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.cobrain.android.MiniFragment;
import com.cobrain.android.R;
import com.cobrain.android.controllers.CraveStrip;
import com.cobrain.android.fragments.CraveStripsFragment;
import com.cobrain.android.minifragments.CraveStripFragment;
import com.cobrain.android.model.Sku;

public class CraveStripPagerAdapter<T> extends MiniFragmentPagerAdapter {
	
	final int COUNT = 3;
	final float WIDTH = 1f / COUNT;

	protected int page = 1;
	protected int perPage;
	protected int countOnThisPage;
	protected int count;
	protected List<Sku> recommendations;
	protected CraveStripsFragment<T> parentFragment;
	private boolean destroyAll;
	protected T results;
	protected HashMap<Integer, MiniFragment> fragments = new HashMap<Integer, MiniFragment>();
	Activity activity;
	protected int stripType;
	protected CraveStrip<T> strip;

	public CraveStripPagerAdapter(Activity activity, CraveStrip<T> strip, CraveStripsFragment<T> cravesFragment) {
		super(activity);
		parentFragment = cravesFragment;
		setCraveStrip(strip);
	}

	public void clear() {
		destroyAll = true;
		notifyDataSetChanged();
		destroyAll = false;
	}

	public boolean setCraveStrip(CraveStrip<T> strip) {
		boolean changed = false;
		
		if (this.strip != strip) {
			this.strip = strip;
			changed = true;
		}
		if (this.stripType != strip.type) {
			this.stripType = strip.type;
			changed = true;
		}
		
		if (changed) {
			notifyDataSetChanged();
		}
		return changed;
	}
	public int getStripType() {
		return this.stripType;
	}
	
	public void setAdapter(CraveStripPagerListAdapter<T> a) {
		a.setPagerAdapter(this);
	}
	
	public void dispose() {
		if (recommendations != null) {
			recommendations.clear();
			recommendations = null;
		}
		clear();
		results = null;
		parentFragment = null;
		activity = null;
	}
	
	void loadSkus(List<Sku> s) {
		//page = r.getPage();
		//perPage = r.getPerPage();
		//countOnThisPage = r.getCount();
		//count = r.getTotal();
		//countOnThisPage = Math.min(countOnThisPage, count);
		page = 1;
		perPage = 100;
		countOnThisPage = count = s.size();
	
		List<Sku> products = s;
		int position = (page - 1) * perPage;
	
		if (recommendations == null) 
			recommendations = new ArrayList<Sku>(position + products.size());
	
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
	
	public void load(T r) {
		results = r;
		clear();
		if (r != null) {
			loadSkus( getSkus(r) );
		}
		else {
			page = 1;
			count = 0;
			if (recommendations != null) {
				recommendations.clear();
				recommendations = null;
			}
		}
		notifyDataSetChanged();
	}

	protected void onLoadSkus(T r) {}

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
	public MiniFragment getItem(int position) {

		CraveStripFragment<T> csf = new CraveStripFragment<T>(getActivity(),  parentFragment);

		if (recommendations == null) 
			return csf;
		
		if (recommendations.size() <= position)
			return csf;
		
		csf.setRecommendation(results, recommendations.get(position));
		
		fragments.put(position, csf);

		return csf;
	}	
	
	@Override
	public float getPageWidth(int position) {
		return WIDTH;
	}

	protected int getTotalCraves(T results) {
		return 0;
	}
	
	public void updateTitle(int position) {
		if (results != null) {
			int totalCraves = getTotalCraves(results);
			
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
	public int getCount() {
		//return count;

		int cnt = 0;

		if (recommendations != null) {
			cnt += ((page-1) * perPage) + countOnThisPage;

			cnt = Math.max(recommendations.size(), cnt);
			cnt = Math.min(cnt, count);
			if (cnt < 0) cnt = 0;
		}

		return cnt;
	}

	public MiniFragment getPage(int position) {
		return fragments.get(position);
	}

	public List<Sku> getRecommendations() {
		return recommendations;
	}
	
	public Sku getRecommendation(int position) {
		return recommendations.get(position);
	}

	public T getParentObject() {
		return results;
	}

	protected List<Sku> getSkus(T r) {
		return null;
	}

}