package com.naotaco.twitterClient01;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class InteractionAdapter extends ArrayAdapter<Interaction> {

	private ArrayList<Interaction> interactions;
	private LayoutInflater inflater;

	public InteractionAdapter(Context context, int textViewResourceId,
			ArrayList<Interaction> items) {

		super(context, textViewResourceId, items);
		this.interactions = items;
		this.inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// ビューを受け取る
		View view = convertView;
		if (view == null) {
			// 受け取ったビューがnullなら新しくビューを生成
			view = inflater.inflate(R.layout.interaction, null);
		}
		// 表示すべきデータの取得
		Interaction item = interactions.get(position);
		if (item != null) {
			//tweet
			TextView text = (TextView) view.findViewById(R.id.text);
			
			// fav
			TextView favCount = (TextView) view.findViewById(R.id.fav);
			
			// rt
			TextView rtCount = (TextView) view.findViewById(R.id.rt);

			if (text != null) {
				text.setText(item.getStatus().getText());
			}else{
				text.setText("null text");
			}
			
			if (favCount != null && rtCount != null){
				// favCount.setText("");
				rtCount.setText(item.getStatus().getRetweetCount() + " RTs: ");
			}

		}
		return view;
	}

}
