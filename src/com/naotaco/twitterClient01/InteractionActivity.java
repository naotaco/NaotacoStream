package com.naotaco.twitterClient01;

import java.util.ArrayList;

import com.naotaco.twitter.auth.TwitterAuthFactory;

import twitter4j.Paging;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.StatusAdapter;
import twitter4j.Twitter;
import twitter4j.TwitterException;

import android.app.Activity;
import android.app.ListActivity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

/**
 * 
 * @author nao
 * 
 * 
 */
public class InteractionActivity extends Activity {

	private ArrayList<Interaction> interactionList = null;
	private ArrayList<Status> tmpTwList = null;
	// ListAdapter
	private InteractionAdapter adapter = null;

	Handler mHandler = new Handler();

	private ListView listView;
	private ResponseList<Status> rtList = null;
	private ProgressBar progressBar;
	Twitter tw = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.interaction_layout);

		TwitterAuthFactory taf = TwitterAuthFactory.getInstance();
		if (taf.isLoggedIn() == false) {
			Log.w("maz", "not logged in....");

		}
		tw = taf.getTwitter();
		listView = (ListView) findViewById(R.id.interactionList);
		progressBar = (ProgressBar) findViewById(R.id.progressBar1);
		progressBar.setVisibility(View.GONE);
		interactionList = new ArrayList<Interaction>();
		tmpTwList = new ArrayList<Status>();

		adapter = new InteractionAdapter(this, R.layout.interaction,
				interactionList);

		new Thread(new Runnable() {

			public void run() {

				mHandler.post(new Runnable() {
					public void run() {
						progressBar.setVisibility(View.VISIBLE);
					}
				});

				try {

					rtList = tw.getRetweetsOfMe();
					long lastRtTweet = rtList.get(rtList.size() - 1).getId();
					int page = 1;
					do {
						// tmpTwList.addAll(tw.getUserTimeline(new
						// Paging(page)));
						for (Status s : tw
								.getUserTimeline(new Paging(page, 40))) {
							Log.v("maz", s.getText() + " : " + s.isFavorited());
							if (s.isFavorited()) {
								tmpTwList.add(s);
							}
						}

						Log.v("maz",
								"page: " + page + " total " + tmpTwList.size());
						page++;
						if (page > 5){
							break;
						}
					} while (tmpTwList.size() == 0
							|| tmpTwList.get(tmpTwList.size() - 1).getId() > lastRtTweet);
				} catch (TwitterException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				if (rtList != null && tmpTwList != null) {

					mHandler.post(new Runnable() {
						public void run() {
							progressBar.setVisibility(View.GONE);
							Log.v("maz", "rtList size: " + rtList.size());
							Log.v("maz", "twtempList size: " + tmpTwList.size());
							for (Status s : rtList) {
								Log.v("maz", "list: " + s.getText());
								interactionList.add(new Interaction(s));
							}
							listView.setAdapter(adapter);
						}
					});
				}else{
					Log.e("maz", "list is null. API limit?");
					mHandler.post(new Runnable() {
						public void run() {
							Toast.makeText(getApplicationContext(), "error while getting TimeLines (API limit?)", Toast.LENGTH_SHORT).show();
							finish();
						}
					});
				}
			}
		}).start();

	}

}
