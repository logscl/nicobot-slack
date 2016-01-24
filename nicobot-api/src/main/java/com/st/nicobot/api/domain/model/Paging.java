package com.st.nicobot.api.domain.model;

/**
 * @author Julien
 *
 */
public class Paging {
	
	private int start;
	
	private int limit;
	
	private int total;

	public Paging() {	}

	public int getStart() {
		return start;
	}

	public int getLimit() {
		return limit;
	}

	public int getTotal() {
		return total;
	}
	
}
