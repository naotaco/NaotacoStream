package com.naotaco.twitterClient01;

import java.util.ArrayList;

import twitter4j.Status;
import twitter4j.User;

public class Interaction {
	
	private Status status;
	private ArrayList<User> rtUsers;
	private ArrayList<User> favUsers;
	
	public Interaction (Status s){
		setStatus(s);
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	public ArrayList<User> getRtUsers() {
		return rtUsers;
	}

	public void setRtUsers(ArrayList<User> rtUsers) {
		this.rtUsers = rtUsers;
	}

	public ArrayList<User> getFavUsers() {
		return favUsers;
	}

	public void setFavUsers(ArrayList<User> favUsers) {
		this.favUsers = favUsers;
	}
	
	
	

}
