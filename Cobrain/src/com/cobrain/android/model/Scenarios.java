package com.cobrain.android.model;

import java.util.List;

public class Scenarios {

	int count;
	List<ScenarioItem> next;
	List<ScenarioItem> previous;
	List<ScenarioItem> results;
	
	public int getCount() {
		return count;
	}

	public List<ScenarioItem> getNext() {
		return next;
	}

	public List<ScenarioItem> getPrevious() {
		return previous;
	}

	public List<ScenarioItem> getResults() {
		return results;
	}

	public Scenarios() {}

}
