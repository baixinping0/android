package com.gongzetao.loop.view.sortlistview;

import com.avos.avoscloud.AVUser;

public class SortModel {

	private AVUser user;   //
	private String sortLetters;  //
	
	public AVUser getUser() {
		return user;
	}
	public void setUser(AVUser user) {
		this.user = user;
	}
	public String getSortLetters() {
		return sortLetters;
	}
	public void setSortLetters(String sortLetters) {
		this.sortLetters = sortLetters;
	}
}
