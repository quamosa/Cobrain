package com.cobrain.android.service.web;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

import org.apache.http.conn.ConnectTimeoutException;
import android.content.Context;
import android.net.http.HttpResponseCache;
import android.os.AsyncTask;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

public class WebRequest extends AsyncTask<Void, Void, Integer> {
	private static final int GET = 0;
	private static final int POST = 1;
	private static final int PUT = 2;
	private static final int DELETE = 3;
	private static final String TAG = "WebRequest";

	String url;
	HashMap<String, String> formFields;
	HashMap<String, String> headerFields;
	private int action = GET;
	private String response;
	private Map<String, List<String>> headers;
    OnResponseListener responseListener;
    private String body;
    private String contentType;
	private int timeout = 10 * 1000; //default time out will be 10 secs...
    
    //504 Gateway Timeout
    
    public interface OnResponseListener {
    	public void onResponse(int responseCode, String response, HashMap<String, String> headers);
		public void onResponseInBackground(int responseCode, String response, HashMap<String, String> headers);    	
    }

    static {
        HttpURLConnection.setFollowRedirects(true);
    }
    
    public static void enableCache(Context c, long size) {
    	final long httpCacheSize = (size == 0) ? (10 * 1024 * 1024) : size; // 10 MiB
        final File httpCacheDir = new File(c.getCacheDir(), "http");

        try {
        	HttpResponseCache.install(httpCacheDir, httpCacheSize);
            //Class.forName("android.net.http.HttpResponseCache")
            //    .getMethod("install", File.class, long.class)
            //    .invoke(null, httpCacheDir, httpCacheSize);
            Log.v(TAG,"cache set up");
        } catch (Exception httpResponseCacheNotAvailable) {
            Log.v(TAG, "Failed to set up HttpResponseCache");
        }    	
    }

    public static void flushCache() {
    	HttpResponseCache cache = HttpResponseCache.getInstalled();
    	cache.flush();
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

	public WebRequest setTimeout(int timeout) {
		this.timeout  = timeout;
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
		HttpURLConnection conn = null;
		int code = 0;

        try {
	        URL urlPath = new URL(url);
	        
	        conn = (HttpURLConnection) urlPath.openConnection();
	        conn.setRequestMethod("POST");
	        conn.setDoInput(true);
	        conn.setDoOutput(true);
        	setHeaders(conn, headerFieldsToHeaders());

        	if (timeout > 0) {
	            conn.setConnectTimeout(timeout); 
	            conn.setReadTimeout(timeout); 
        	}

	        setFormFields(conn);
	        
	        //conn.connect();
        	InputStream s = getInputStream(conn);
			this.response = streamToString(s);
			this.headers = conn.getHeaderFields();
			code = conn.getResponseCode();
			
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (conn != null) conn.disconnect();
		}		

	    return code;
	}

	InputStream getInputStream(HttpURLConnection conn) {
		InputStream s = null;     
        try {
	       s = conn.getInputStream();
	    }
	    catch(IOException exception) {           
	       s = conn.getErrorStream();
	    }
        return s;
	}
	
	private int doGet() {
		HttpURLConnection conn = null;
		int code = 0;
		
	   	try {
	        URL urlPath = new URL(url);

	        conn = (HttpURLConnection) urlPath.openConnection();
	        //conn.setRequestMethod("GET");
	        conn.setUseCaches(true);
	        conn.setDoInput(true);
        	setHeaders(conn, headerFieldsToHeaders());
        	
        	if (timeout > 0) {
	            conn.setConnectTimeout(timeout); 
	            conn.setReadTimeout(timeout); 
        	}

        	conn.connect();
        	InputStream s = getInputStream(conn);
			this.response = streamToString(s);
			this.headers = conn.getHeaderFields();
			
			code = conn.getResponseCode();
		
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (ConnectTimeoutException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (conn != null) conn.disconnect();
		}
		
		return code;
	}

	private int doDelete() {
		HttpURLConnection conn = null;
		int code = 0;

        try {
        	
	        URL urlPath = new URL(url);

	        conn = (HttpURLConnection) urlPath.openConnection();
	        conn.setRequestMethod("DELETE");
	        conn.setDoInput(true);
        	setHeaders(conn, headerFieldsToHeaders());
        	
        	if (timeout > 0) {
	            conn.setConnectTimeout(timeout); 
	            conn.setReadTimeout(timeout); 
        	}

        	conn.connect();
        	InputStream s = getInputStream(conn);
			this.response = streamToString(s);
			this.headers = conn.getHeaderFields();
			
			code = conn.getResponseCode();
			
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (conn != null) conn.disconnect();
		}
		
		return code;
	}

	private int doPut() {
		HttpURLConnection conn = null;
		int code = 0;

        try {
	        URL urlPath = new URL(url);

	        conn = (HttpURLConnection) urlPath.openConnection();
	        conn.setRequestMethod("PUT");
	        conn.setDoInput(true);
	        conn.setDoOutput(true);
        	setHeaders(conn, headerFieldsToHeaders());

        	if (timeout > 0) {
	            conn.setConnectTimeout(timeout); 
	            conn.setReadTimeout(timeout); 
        	}

        	setFormFields(conn);
        	
        	conn.connect();
        	InputStream s = getInputStream(conn);
			this.response = streamToString(s);
			this.headers = conn.getHeaderFields();
			code = conn.getResponseCode();
			
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (conn != null) conn.disconnect();
		}		
		
		return code;
	}

	void setFormFields(HttpURLConnection conn) {
		String body = this.body;
		
		if (formFields != null) {
			conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

			try {

				for (String key : formFields.keySet()) {
		        	if (body == null) body = "";
		        	if (body.length() > 0) body += "&";
		        	//body += key + "=" + formFields.get(key);
		        	String value = formFields.get(key);
		        	body += URLEncoder.encode(key, "UTF-8") + "=" + URLEncoder.encode(value, "UTF-8");
		        }
			
				//body = URLEncoder.encode(body, "UTF-8");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}

        if (body != null && body.length() > 0) {
        	OutputStream os = null;

			try {
				byte[] bytes = body.getBytes("UTF-8");
				int contentLength = bytes.length;
				conn.setFixedLengthStreamingMode(contentLength);
	            conn.setRequestProperty("Content-Length", Integer.toString(contentLength));
				os = conn.getOutputStream();
	        	os.write(bytes);
	        	os.flush();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				if (os != null)
					try {
						os.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
			}
        }
		
	}

	String streamToString(InputStream is) {
		StringBuilder builder = new StringBuilder();

		try {
			if (is != null) {
	        	BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
				for (String line = null; (line = reader.readLine()) != null; ) {
				    builder.append(line).append("\n");
				}
				reader.close();
				is.close();
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return builder.toString();
	}

	Map<String, List<String>> headerFieldsToHeaders() {
		Map<String, List<String>> headers = new HashMap<String, List<String>>();

        if (contentType != null) {
        	ArrayList<String> s = new ArrayList<String>();
        	s.add(contentType);
        	headers.put("Content-Type", s);
        }

        if (headerFields != null) {
			for (String key : headerFields.keySet()) {
	        	ArrayList<String> s = new ArrayList<String>();
	        	s.add(headerFields.get(key));
	        	headers.put(key, s);
			}
		}

    	ArrayList<String> s = new ArrayList<String>();
    	s.add("android");
    	s.add(Build.VERSION.RELEASE);
    	s.add("phone");
    	headers.put("x-cobrain-client", s);
    	
		return headers;
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

	public void onResponse(int responseCode, String response, HashMap<String, String> headers) {}

	public void setFormField(String field, String value) {
		if (formFields == null) formFields = new HashMap<String, String>();
		formFields.put(field, value);
	}

	private void setHeaders(HttpURLConnection conn, Map<String, List<String>> headerFieldsToHeaders) {
		for (String key : headerFieldsToHeaders.keySet()) {
			List<String> value = headerFieldsToHeaders.get(key);
			String[] values = value.toArray(new String[value.size()]);
			String val = TextUtils.join(", " , values);
			conn.setRequestProperty(key, val);
		}
	}
	
	public HashMap<String, String> getHeaders() {
		HashMap<String, String> map = new HashMap<String, String>();
		if (headers != null) {
			for (String key : headers.keySet()) {
				List<String> value = headers.get(key);
				String[] values = value.toArray(new String[value.size()]);
				String val = TextUtils.join("," , values);
				map.put(key, val);
			}
		}
		return map;
	}

}
