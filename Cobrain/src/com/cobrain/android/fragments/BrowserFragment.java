package com.cobrain.android.fragments;

import com.cobrain.android.R;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.RelativeLayout;

public class BrowserFragment extends BaseCobrainFragment {
	public static final String TAG = "BrowserFragment";

	WebView webView;
	RelativeLayout webViewParent;
	ImageView back;
	ImageView forward;
	ImageView refresh;
	String url;
	String merchant;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
		Bundle savedInstanceState) {

		merchant = getArguments().getString("merchant");
		url = getArguments().getString("url");
		
		View v = inflater.inflate(R.layout.main_webview_frame, null);

		loaderUtils.initialize((ViewGroup)v);

		webView = (WebView) v.findViewById(R.id.web_view);
		back = (ImageView) v.findViewById(R.id.back);
		webViewParent = (RelativeLayout) v.findViewById(R.id.web_view_parent);
		forward = (ImageView) v.findViewById(R.id.forward);
		refresh = (ImageView) v.findViewById(R.id.refresh);
		back.setOnClickListener(this);
		forward.setOnClickListener(this);
		refresh.setOnClickListener(this);

		webView.setWebViewClient(new WebViewClient() {
			@Override
			public void onPageStarted(WebView view, String url, Bitmap favicon) {
				//if (view.getVisibility() == View.VISIBLE)
					//loaderUtils.showLoading(null);
				super.onPageStarted(view, url, favicon);
			}
			@Override
			public void onPageFinished(WebView view, String url) {
				String clear = (String) view.getTag();
				if (clear != null && clear.equals("clearHistory")) {
					view.setTag(null);
					view.clearHistory();
				}
				loaderUtils.dismissLoading();
				loaderUtils.show(webViewParent);
				super.onPageFinished(view, url);
			}
		});
		webView.getSettings().setJavaScriptEnabled(true);
		webView.getSettings().setBuiltInZoomControls(true);
		webView.setInitialScale(100);
	    webView.getSettings().setLoadWithOverviewMode(true);
        webView.getSettings().setUseWideViewPort(true);

		setTitle("Cobrain");
		
		return v;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
	}

	public void update() {
		//String host = Uri.parse(url).getHost();
		if (merchant != null) 
			loaderUtils.showLoading("Sending you to " + merchant);
		else
			loaderUtils.showLoading(null);
		webView.setTag("clearHistory");
		webView.loadUrl(url);
	}

	@Override
	public void onDestroyView() {
		webView.stopLoading();
		webView.setWebViewClient(null);
		webView = null;
		webViewParent = null;
		
		back.setOnClickListener(null);
		forward.setOnClickListener(null);
		refresh.setOnClickListener(null);
		back = null;
		forward = null;
		refresh = null;
		
		merchant = null;
		url = null;

		super.onDestroyView();
	}

	@Override
	public void onClick(View v) {
		super.onClick(v);
		
		switch(v.getId()) {
		case R.id.refresh:
			webView.reload();
			break;
		case R.id.back:
			webView.goBack();
			break;
		case R.id.forward:
			webView.goForward();
			break;
		}
	}

}
