package com.naotaco.twitterClient01;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class tweetListAdapter extends ArrayAdapter<Object> {

	private LayoutInflater layoutInflater = null;
	private ArrayList<Status> tweetList = null;
	private ArrayList<View> viewList;
	private ImageButton favBtn;
	Handler mHandler = new Handler();
	private Twitter tw = null;
	
	public boolean setTw(Twitter t){
		tw = t;
		return true;
	}
	
	

	public tweetListAdapter(Context context, int textViewResourceId,
			ArrayList<Status> items) {

		super(context, textViewResourceId);
		this.tweetList = new ArrayList<Status>();
		viewList = new ArrayList<View>();
		// this.elements = items;
		this.layoutInflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		favBtn = new ImageButton(context);
		
	}

	public int getCount() {
		// TODO Auto-generated method stub
		return viewList.size();
	}

	public void add(final Status e) {
		tweetList.add(0, e);
		// inflater.xml����1�s���̃��C�A�E�g�𐶐�
		View convertView = layoutInflater.inflate(R.layout.tweet, null);

		// �eid�̍��ڂɒl���Z�b�g
		final ImageView inflaterIcon = (ImageView) convertView
				.findViewById(R.id.icon);
		TextView inflaterName = (TextView) convertView
				.findViewById(R.id.screen_name);
		TextView inflaterStatus = (TextView) convertView
				.findViewById(R.id.status);
		
		// inflaterIcon.setImageBitmap(elements.get(position).getUser().getProfileImageURL());
		new Thread(new Runnable() {
			
			public void run() {
				final Bitmap b = getBitmapFromURL(e.getUser().getProfileImageURL());
				mHandler.post(new Runnable() {
					public void run() {
						inflaterIcon.setImageBitmap(b);
					}
				});
			}
		}).start();
		inflaterIcon.setImageResource(R.drawable.ic_launcher);

		inflaterName.setText(e.getUser().getScreenName());
		inflaterStatus.setText(e.getText());
		


		viewList.add(0, convertView);
	}
	

	public View getItem(int num) {
		// TODO Auto-generated method stub
		return viewList.get(num);
	}
	
	public Status getStatus(int pos){
		return tweetList.get(pos);
	}

	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return 0;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		convertView = viewList.get(position);
		final Status s = tweetList.get(position);
			
		final ImageButton favBtn = (ImageButton) convertView.findViewById(R.id.fav);
		favBtn.setFocusable(false);

        favBtn.setOnClickListener(new OnClickListener() {
            public void onClick(final View v) {
            	
            	Log.v("naotaco", "+fav: " + s.getText());
            	
            	
        		new Thread(new Runnable() {
        			
        			public void run() {
        				try {
							tw.createFavorite(s.getId());
							
	        				mHandler.post(new Runnable() {
	        					public void run() {
	        						favBtn.setImageResource(R.drawable.fav_true);
	        					}
	        				});
	        				
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
        			}
        		}).start();            	
            }
        });

		return convertView;

	}
	

	public static Bitmap getBitmapFromURL(URL url) {
		Log.v("naotaco", "getBitmap");
		try {
			HttpURLConnection connection = (HttpURLConnection) url
					.openConnection();
			connection.setDoInput(true);
			connection.connect();
			InputStream input = connection.getInputStream();
			Bitmap myBitmap = BitmapFactory.decodeStream(input);
			return myBitmap;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}


}
