package com.naotaco.twitterClient01;

import java.util.ArrayList;
import java.util.Calendar;


import twitter4j.Status;
import twitter4j.StatusUpdate;
import twitter4j.Twitter;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

public class NaotacoStream extends Activity implements OnEditorActionListener,
		OnItemClickListener {
	/** Called when the activity is first created. */

	private tweetListAdapter listAdapter;

	Handler handler = new Handler();
	ListView tweets;
	Status newTweet;
	EditText statusInput;

	private StatusUpdate composingStatus;
	private long inReplyToId = -1;

	private StreamService mBoundService = null;
	private boolean mIsBound;

	private Runnable pollTweet;
	private Handler pollHandler = new Handler();

	private AlarmManager alarm;
	private PendingIntent pIntent;
	private Calendar calendar;
	private int deleteInterval;

	public static String INTENT_PARAM_STATUS_ID = "status_id";
	private static final String SETTING_FILE_NAME = "NightDreamSettings";
	private static final String SETTING_INTERVAL_TIME = "settingTime";

	private SharedPreferences preferences;
	private SharedPreferences.Editor preferencesEditor;

	ArrayList<Status> tweetList;
	ArrayList<Status> lastStatusList;

	Twitter tw;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.main);
		tweetList = new ArrayList<Status>();
		listAdapter = new tweetListAdapter(this, R.layout.tweet, tweetList);

		tweets = (ListView) findViewById(R.id.tweets);
		tweets.setAdapter(listAdapter);
		tweets.setOnItemClickListener(this);

		// input
		statusInput = (EditText) findViewById(R.id.input);
		statusInput.setOnEditorActionListener(this);

		// intent to start Service
		Intent intent = new Intent(NaotacoStream.this, StreamService.class);
		startService(intent);

		// set handler to get status from Service

		doBindService();
		lastStatusList = new ArrayList<Status>();

		pollTweet = new Runnable() {
			public void run() {
				if (mIsBound && mBoundService != null) {
					lastStatusList = mBoundService.getLastStatus();
					Log.v("naotaco", String.valueOf(lastStatusList.size()));
					// return only 0...
					if (lastStatusList != null) {
						for (Status s : lastStatusList) {
							addStatus(s);
						}
						mBoundService.clearTweetList();
					}
				}
				pollHandler.postDelayed(pollTweet, 500);
			}
		};
		pollHandler.postDelayed(pollTweet, 100);

		// settings

		preferences = getSharedPreferences(SETTING_FILE_NAME, MODE_PRIVATE);
		deleteInterval = preferences.getInt(SETTING_INTERVAL_TIME, 0);

	}

	@Override
	public void onResume() {
		super.onResume();
		Context context = NaotacoStream.this;
		alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		calendar = Calendar.getInstance();

		if (deleteInterval > 0){
			Toast.makeText(
					getApplicationContext(),
					"Foolish mode: Your update will be deleted in "
							+ deleteInterval + "sec.", Toast.LENGTH_SHORT)
					.show();
		}
	}

	public void addStatus(Status s) {
		listAdapter.add(s);
		listAdapter.notifyDataSetChanged();
	}

	@Override
	protected void onDestroy() {
		Log.d("naotaco", "onDestroy");
		Toast.makeText(this, "userstream shutdown", Toast.LENGTH_SHORT).show();
		// twitterStream.shutdown();

		super.onDestroy();
	}

	@Override
	protected void onNewIntent(Intent intent) {

	}

	public boolean onEditorAction(final TextView v, int actionId, KeyEvent event) {
		if (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {

			new Thread(new Runnable() {
				public void run() {
					composingStatus = new StatusUpdate(v.getText().toString());
					if (inReplyToId > -1) {
						composingStatus.setInReplyToStatusId(inReplyToId);
						inReplyToId = -1;
					}

					try {
						Status sendStatus = tw.updateStatus(composingStatus);

						if (deleteInterval > 0) {
							Intent intent = new Intent(NaotacoStream.this,
									DeleteTweetReceiver.class);
							intent.putExtra(INTENT_PARAM_STATUS_ID,
									sendStatus.getId());
							// 2回目以降も認識させるためにダミーのキーを登録
							intent.setType(String.valueOf(Math.random()));
							pIntent = PendingIntent.getBroadcast(
									NaotacoStream.this, 0, intent,
									PendingIntent.FLAG_UPDATE_CURRENT);
							calendar.setTimeInMillis(System.currentTimeMillis());
							deleteInterval = preferences.getInt(
									SETTING_INTERVAL_TIME, 0);
							calendar.add(Calendar.SECOND, deleteInterval);
							alarm.set(AlarmManager.RTC_WAKEUP,
									calendar.getTimeInMillis(), pIntent);
							
													 
						}
						handler.post(new Runnable() {
							public void run() {
								v.setText("");
							}
						});
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}).start();

		}
		return false;
	}

	public void onItemClick(AdapterView<?> arg0, View arg1, int pos, long id) {
		Log.v("naotaco", String.valueOf(pos));
		Status st = listAdapter.getStatus(pos);
		EditText et = (EditText) findViewById(R.id.input);
		Log.v("naotaco", "itemSelected: " + st.getText());
		et.setText("@" + st.getUser().getScreenName() + " "
				+ et.getText().toString());
		et.setSelection(et.getText().length());
		inReplyToId = st.getId();
	}

	// オプションメニューが最初に呼び出される時に1度だけ呼び出されます
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// メニューアイテムを追加します
		menu.add(Menu.NONE, Menu.FIRST, Menu.NONE, "Toggle TTS");
		menu.add(Menu.NONE, Menu.FIRST + 1, Menu.NONE, "Stop Service");
		menu.add(Menu.NONE, Menu.FIRST + 2, Menu.NONE, "鳥頭設定");
		menu.add(Menu.NONE, Menu.FIRST + 3, Menu.NONE, "my Favstar");

		return super.onCreateOptionsMenu(menu);
	}

	// オプションメニューアイテムが選択された時に呼び出されます
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {

		case Menu.FIRST:
			boolean isReading = mBoundService.toggleTTS();
			if (isReading) {
				Toast.makeText(this, "TTS Started", Toast.LENGTH_SHORT).show();
			} else {
				Toast.makeText(this, "TTS Stopped", Toast.LENGTH_SHORT).show();
			}
			break;

		case Menu.FIRST + 1:
			doUnbindService();
			break;

		case Menu.FIRST + 2:
			new AlertDialog.Builder(this)
					.setTitle(R.string.timeSetting)
					.setItems(R.array.timeSettings,
							new DialogInterface.OnClickListener() {
								public void onClick(
										DialogInterface dialoginterface, int i) {

									preferencesEditor = preferences.edit();

									switch (i) {
									case 0:
										// off
										preferencesEditor.putInt(
												SETTING_INTERVAL_TIME, 0);
										break;
									case 1:
										// 30sec
										preferencesEditor.putInt(
												SETTING_INTERVAL_TIME, 30);
										break;
									case 2:
										preferencesEditor.putInt(
												SETTING_INTERVAL_TIME, 60);
										break;
									case 3:
										preferencesEditor.putInt(
												SETTING_INTERVAL_TIME, 180);
										break;
									case 4:
										preferencesEditor.putInt(
												SETTING_INTERVAL_TIME, 300);
										break;

									default:
										preferencesEditor.putInt(
												SETTING_INTERVAL_TIME, 0);
										break;

									}
									preferencesEditor.commit();

									deleteInterval = preferences.getInt(
											SETTING_INTERVAL_TIME, 0);

									if (deleteInterval > 0){
										Toast.makeText(
												getApplicationContext(),
												"Foolish mode: Your update will be deleted in "
														+ deleteInterval + "sec.", Toast.LENGTH_SHORT)
												.show();
									}
								}
							}).show();
			break;
			
		case Menu.FIRST + 3:
			// open my favstar
			Intent i = new Intent(this, InteractionActivity.class);
			startActivity(i);
			
			
			break;

		}
		return true;
	}

	private ServiceConnection mConnection = new ServiceConnection() {

		public void onServiceConnected(ComponentName className, IBinder service) {

			// サービスとの接続確立時に呼び出される
			Toast.makeText(NaotacoStream.this, "Activity:onServiceConnected",
					Toast.LENGTH_SHORT).show();

			// サービスにはIBinder経由で#getService()してダイレクトにアクセス可能
			mBoundService = ((StreamService.MyServiceLocalBinder) service)
					.getService();

			// 必要であればmBoundServiceを使ってバインドしたサービスへの制御を行う
			Log.v("naotaco", mBoundService.getTestStringHoge());
			// get twitter instance from Service and set to Adapter
			tw = mBoundService.getTw();
			listAdapter.setTw(tw);

		}

		public void onServiceDisconnected(ComponentName className) {
			// サービスとの切断(異常系処理)
			// プロセスのクラッシュなど意図しないサービスの切断が発生した場合に呼ばれる。
			mBoundService = null;
			Toast.makeText(NaotacoStream.this,
					"Activity:onServiceDisconnected", Toast.LENGTH_SHORT)
					.show();
		}
	};

	void doBindService() {
		// サービスとの接続を確立する。明示的にServiceを指定
		// (特定のサービスを指定する必要がある。他のアプリケーションから知ることができない = ローカルサービス)
		bindService(new Intent(NaotacoStream.this, StreamService.class),
				mConnection, Context.BIND_AUTO_CREATE);
		mIsBound = true;
	}

	void doUnbindService() {
		if (mIsBound) {
			// コネクションの解除
			unbindService(mConnection);
			mIsBound = false;
		}
	}

}