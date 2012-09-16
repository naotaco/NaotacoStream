package com.naotaco.twitterClient01;

import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class DeleteTweetReceiver extends BroadcastReceiver{

	@Override
	public void onReceive(Context context, Intent intent) {
		long statusId = intent.getLongExtra(NaotacoStream.INTENT_PARAM_STATUS_ID, -1);
		if (statusId == -1){
			Toast.makeText(context, "Intent Params error...", Toast.LENGTH_SHORT).show();
			return;
		}
		 
		
		Twitter tw;
		String auth[] = AuthInfoManager.getInstance().getAuthInfoArray();
		
		if (auth == null){
			//まあnullでも知らねえ
		}

		tw = new TwitterFactory().getInstance();
		tw.setOAuthConsumer(auth[0], auth[1]);
		tw.setOAuthAccessToken(new AccessToken(auth[2], auth[3]));
		
		try {
			Status s = tw.destroyStatus(statusId);
			Toast.makeText(context, "destroyed status \"" + s.getText() + "\"", Toast.LENGTH_SHORT).show(); 
		} catch (TwitterException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Toast.makeText(context, "delete Tweet error...", Toast.LENGTH_SHORT).show();
		}
		
	}

}
