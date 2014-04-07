package com.cobrain.android.model;

import java.util.List;

public class Opinion {
	String _id;
	String updated_at;
	List<String> reasons;
	String signal;
    String operation;
    String sentiment;
	
	public String getId() {
		return _id;
	}

	public String updatedAt() {
		return updated_at;
	}
	
	public boolean is(String signal) {
		return (signal != null) && (
                signal.equals(this.signal) ||
                signal.equals(this.sentiment) ||
                signal.equals(this.operation));
	}

    public String getSentiment() {
        return sentiment;
    }

    public String getOperation() {
        return operation;
    }

	public String getSignal() {
		return signal;
	}
}
