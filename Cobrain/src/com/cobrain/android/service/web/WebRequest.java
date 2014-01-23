package com.cobrain.android.service.web;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import android.os.AsyncTask;

public class WebRequest extends AsyncTask<Void, Void, Integer> {
	private static final int GET = 0;
	private static final int POST = 1;
	private static final int PUT = 2;
	private static final int DELETE = 3;

	String url;
	HashMap<String, String> formFields;
	HashMap<String, String> headerFields;
	private int action = GET;
	private String response;
	private Header[] headers;
    static HttpClient httpclient = new DefaultHttpClient();
    OnResponseListener responseListener;
    private String body;
    private String contentType;
    
    //504 Gateway Timeout
    
    public interface OnResponseListener {
    	public void onResponse(int responseCode, String response, HashMap<String, String> headers);
		public void onResponseInBackground(int responseCode, String response, HashMap<String, String> headers);    	
    }
      
	public WebRequest post(String url) {
		this.url = url;
		this.action = POST;
		return this;
	}
	public WebRequest get(String url) {
		this.url = url;
		this.action = GET;
		return this;
	}
	public WebRequest put(String url) {
		this.url = url;
		this.action = PUT;
		return this;
	}
	public WebRequest delete(String url) {
		this.url = url;
		this.action = DELETE;
		return this;
	}
	
    public WebRequest setFormFields(HashMap<String, String> formFields) {
		this.formFields = formFields;
    	return this;
    }
    public WebRequest setHeaders(HashMap<String, String> headers) {
		this.headerFields = headers;
    	return this;
    }
	public WebRequest setContentType(String type) {
		contentType = type;
		return this;
	}
    
    public WebRequest setOnResponseListener(OnResponseListener listener) {
    	responseListener = listener;
    	return this;
    }

	public WebRequest setBody(String query) {
		body = query;
		return this;
	}

	public String getResponse() {
		return response;
	}

	public int go() {
		return go(false);
	}
	public int go(boolean async) {
		int responseCode = 0;
		
		if (async) {
			execute();
		}
		else {
			switch(action) {
			case GET: 
				responseCode = doGet();
				break;
			case POST:
				responseCode = doPost();
				break;
			case PUT:
				responseCode = doPut();
				break;
			case DELETE:
				responseCode = doDelete();
			}
		}
		
		return responseCode;
	}
	
	public int doPost() {
	    HttpPost httppost = new HttpPost(url);

	    try {
	        httppost.setHeaders(headerFieldsToHeaders());

	        setFormFields(httppost);
	    	
	        if (body != null && body.length() > 0) {
	        	StringEntity se = new StringEntity(body);
	        	if (contentType != null) se.setContentType(contentType);
	        	httppost.setEntity(se);
	        }

	        HttpResponse response = httpclient.execute(httppost);

			this.headers = response.getAllHeaders();

	        this.response = streamToString( response.getEntity().getContent() );

	        return response.getStatusLine().getStatusCode();
	        
	    } catch (ClientProtocolException e) {
	    	e.printStackTrace();
	    } catch (IOException e) {
	    	e.printStackTrace();
	    }
	    
	    return 0;
	}
	
	private int doGet() {
	   	HttpUriRequest request = new HttpGet(url);

	   	try {
        	request.setHeaders(headerFieldsToHeaders());
        	HttpResponse response =  httpclient.execute(request);

        	InputStream s = response.getEntity().getContent();
			this.response = streamToString(s);
			this.headers = response.getAllHeaders();
			
			return response.getStatusLine().getStatusCode();
			
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return 0;
	}

	private int doDelete() {
	   	HttpUriRequest request = new HttpDelete(url);

        try {
        	request.setHeaders(headerFieldsToHeaders());
        	HttpResponse response =  httpclient.execute(request);

        	InputStream s = response.getEntity().getContent();
			this.response = streamToString(s);
			this.headers = response.getAllHeaders();
			
			return response.getStatusLine().getStatusCode();
			
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return 0;
	}

	private int doPut() {
	   	HttpPut request = new HttpPut(url);

        try {
        	setFormFields(request);
        	
        	request.setHeaders(headerFieldsToHeaders());
        	
	        if (body != null && body.length() > 0) {
	        	StringEntity se = new StringEntity(body);
	        	if (contentType != null) se.setContentType(contentType);
	        	request.setEntity(se);
	        }
        	
        	HttpResponse response =  httpclient.execute(request);

        	InputStream s = response.getEntity().getContent();
			this.response = streamToString(s);
			this.headers = response.getAllHeaders();
			
			return response.getStatusLine().getStatusCode();
	    				
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return 0;
	}

	void setFormFields(Object request) {
		if (formFields == null) return;
		
        ArrayList<BasicNameValuePair> nameValuePairs = new ArrayList<BasicNameValuePair>();

        for (String key : formFields.keySet()) {
	        nameValuePairs.add(new BasicNameValuePair(key, formFields.get(key) ));
        }

        try {

        	UrlEncodedFormEntity entity = new UrlEncodedFormEntity(nameValuePairs);
        
        	if (request instanceof HttpPost)
        		((HttpPost) request).setEntity(entity);
        	else if (request instanceof HttpPut) 
        		((HttpPut) request).setEntity(entity);
        	
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}

	String streamToString(InputStream is) {
		StringBuilder builder = new StringBuilder();

		try {
        	BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
			for (String line = null; (line = reader.readLine()) != null; ) {
			    builder.append(line).append("\n");
			}
			reader.close();
			is.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return builder.toString();
	}

	Header[] headerFieldsToHeaders() {
		ArrayList<Header> h = new ArrayList<Header>();

        if (contentType != null) {
			h.add(new BasicHeader("Content-Type", contentType));
        }

        if (headerFields != null) {
			for (String key : headerFields.keySet()) {
				BasicHeader hd = new BasicHeader(key, headerFields.get(key));
				h.add(hd);
			}
		}

		return h.toArray(new Header[h.size()]);
	}
	
	@Override
	protected Integer doInBackground(Void... params) {
		int responseCode = go();
		
		if (responseListener != null) {
			HashMap<String, String> map = getHeaders();
			responseListener.onResponseInBackground(responseCode, response, map);
		}
		
		return responseCode;		
	}

	@Override
	protected void onPostExecute(Integer result) {
		HashMap<String, String> map = getHeaders();
		if (responseListener != null) {
			responseListener.onResponse(result, response, map);
		}
		onResponse(result, response, map);
	}
	
	public HashMap<String, String> getHeaders() {
		HashMap<String, String> map = new HashMap<String, String>();
		if (headers != null) {
			for (int i = 0; i < headers.length; i++) {
				map.put(headers[i].getName(), headers[i].getValue());
			}
		}
		return map;
	}
	
	public void onResponse(int responseCode, String response, HashMap<String, String> headers) {}

}
