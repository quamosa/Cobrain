package com.cobrain.android.model;

import java.util.List;

public class Opinion {
	String _id;
	String updated_at;
	List<String> reasons;
	String signal;
	
	public String getId() {
		return _id;
	}

	public boolean is(String signal) {
		return this.signal != null && this.signal.equals(signal);
	}

	public void setSignal(String signal) {
		this.signal = signal;
	}
}
