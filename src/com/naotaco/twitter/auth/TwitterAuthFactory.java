package com.naotaco.twitter.auth;

import android.content.Context;
import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;

public class TwitterAuthFactory {
		
	// singleton
	private static TwitterAuthFactory instance = new TwitterAuthFactory();

	private TwitterAuthFactory() {}
	
	public static TwitterAuthFactory getInstance(){
		return instance;
	}
	
	private boolean isLoggedIn = false;
	private Twitter twitter = null;
	
	public boolean login(String consumerKey, String consumerSecret,
			String accessToken, String accessSecret){
		
		twitter = new TwitterFactory().getInstance();
		twitter.setOAuthConsumer(consumerKey, consumerSecret);
		twitter.setOAuthAccessToken(new AccessToken(accessToken, accessSecret));
		
		isLoggedIn = true;
		
		return true;
	}

	public boolean isLoggedIn() {
		return isLoggedIn;
	}

	public Twitter getTwitter() {
		return twitter;
	}

}
