package com.naotaco.twitterClient01;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Locale;

import com.naotaco.twitter.auth.TwitterAuthFactory;

import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.UserStreamAdapter;
import twitter4j.auth.AccessToken;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.util.Log;
import android.view.Menu;
import android.widget.Toast;

public class StreamService extends Service implements OnInitListener {

	private Twitter tw = null;
	private TwitterStream twitterStream;
	private MyUserStreamAdapter streamAdapter;

	private ArrayList<Status> newStatus, newStatusTmp;
	private String[] egoKeyWords;
	private boolean toRead;

	public static final int NOTIFICATION_ID = 1306;

	// for TTS
	private TextToSpeech mTts;
	Menu menu;
	boolean isReading = false;

	// サービスに接続するためのBinder
	public class MyServiceLocalBinder extends Binder {
		// サービスの取得
		StreamService getService() {
			return StreamService.this;
		}
	}

	// Binderの生成
	private final IBinder mBinder = new MyServiceLocalBinder();

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return mBinder;
	}

	@Override
	public void onCreate() {
		// Toast.makeText(this, "create service", Toast.LENGTH_SHORT).show();

		String auth[] = AuthInfoManager.getInstance().getAuthInfoArray();

		if (auth == null) {
			Toast.makeText(this, "invalid auth twitter token",
					Toast.LENGTH_SHORT).show();

		}

		String consumerKey = auth[0];
		String consumerSecret = auth[1];
		String accessToken = auth[2];
		String accessSecret = auth[3];

		TwitterAuthFactory taf = TwitterAuthFactory.getInstance();

		if (taf.isLoggedIn() == false) {
			taf.login(consumerKey, consumerSecret, accessToken, accessSecret);
		}

		tw = taf.getTwitter();

		ConfigurationBuilder builder = new ConfigurationBuilder();
		{
			builder.setOAuthConsumerKey(consumerKey);
			builder.setOAuthConsumerSecret(consumerSecret);
			builder.setOAuthAccessToken(accessToken);
			builder.setOAuthAccessTokenSecret(accessSecret);
			builder.setUserStreamBaseURL("https://userstream.twitter.com/2/");

		}

		// 1. TwitterStreamFactory ���C���X�^���X������
		Configuration conf = builder.build();
		TwitterStreamFactory twitterStreamFactory = new TwitterStreamFactory(
				conf);
		// 2. TwitterStream ���C���X�^���X������
		twitterStream = twitterStreamFactory.getInstance();

		streamAdapter = new MyUserStreamAdapter();
		{
			// 4. TwitterStream �� UserStreamListener
			// �����������C���X�^���X��ݒ肷��
			twitterStream.addListener(streamAdapter);
			twitterStream.user();
		}

		newStatus = new ArrayList<Status>();
		newStatusTmp = new ArrayList<Status>();

		egoKeyWords = getResources().getStringArray(R.array.egoKeyWords);

		for (String s : egoKeyWords) {
			Log.v("naotaco", s);
		}

		this.putNotice(this);

		// TTS
		mTts = new TextToSpeech(this, this // TextToSpeech.OnInitListener
		);

	}

	// 3. UserStream ��M���ɉ�������iUserStreamListener�j���X�i�[����������
	class MyUserStreamAdapter extends UserStreamAdapter {

		// �V�����c�C�[�g�i�X�e�[�^�X�j���擾����x�ɌĂяo�����
		@Override
		public void onStatus(Status status) {
			super.onStatus(status);
			newStatus.add(status);
			toRead = false;

			for (String ego : egoKeyWords) {
				if (status.getText().matches(".*" + ego + ".*")) {
					notifyUpdate(status);
					toRead = true;
				}
			}

			if (isReading && (status.getText().charAt(0) != '@' || toRead)) {
				readText(status.getUser().getScreenName() + " "
						+ status.getText());
			}

			Log.v("naotaco", status.getText());

		}
	}

	@Override
	public void onStart(Intent intent, int StartId) {
		intent.getStringExtra("Message");
		// Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
		Thread t = new Thread() {
			@Override
			public void run() {

			}
		};
		t.start();
	}

	@Override
	public boolean onUnbind(Intent intent) {
		Log.i("naotaco", "onUnbind");
		Toast.makeText(this, "Userstream shutdown", Toast.LENGTH_SHORT).show();
		twitterStream.shutdown();
		super.onUnbind(intent);
		stopSelf();
		removeNotice(this);
		return true;
	}

	@Override
	public void onDestroy() {
		Toast.makeText(this, "destroy service", Toast.LENGTH_SHORT).show();
		stopSelf();
		twitterStream.shutdown();
	}

	public ArrayList<Status> getLastStatus() {

		return newStatus;
	}

	public void clearTweetList() {
		newStatus.clear();
	}

	public String getTestStringHoge() {
		return "hoge";

	}

	public Twitter getTw() {
		return tw;

	}

	private static Notification createNotification(Context context) {
		Notification notification = new Notification(R.drawable.naotacostream, // ステータスに置くスモールアイコン
				"サービスを開始しました", // アイコン横のツールテキスト無し
				System.currentTimeMillis() // システム時刻
		);
		// PendingIntent.getActivity(context, requestCode, intent, flags)
		PendingIntent pi = PendingIntent.getActivity(context, 0, // requestCode
				new Intent(context, NaotacoStream.class), 0);
		notification.setLatestEventInfo(context,
				context.getString(R.string.app_name),
				context.getString(R.string.notification_text), pi);
		// Set Notification To StatusBar
		// ※これがポイント
		// フラグを設定しないと通知領域に表示されてしまう
		// 通知領域に表示するのが目的なら flags の設定は不要になる
		notification.flags = notification.flags | Notification.FLAG_NO_CLEAR // クリアボタンを表示しない
																				// ※ユーザがクリアできない
				| Notification.FLAG_ONGOING_EVENT; // 継続的イベント領域に表示 ※「実行中」領域
		notification.number = 0;
		return notification;

	}

	public static void putNotice(Context context) {
		NotificationManager nm = (NotificationManager) context
				.getSystemService(Context.NOTIFICATION_SERVICE);
		Notification notification = createNotification(context);
		nm.notify(StreamService.NOTIFICATION_ID, notification);
	}

	// 2. ステータスバーから削除
	// 2-1. アプリの起動時に呼び出す
	public static void removeNotice(Context context) {
		NotificationManager nm = (NotificationManager) context
				.getSystemService(Context.NOTIFICATION_SERVICE);
		nm.cancel(NOTIFICATION_ID);
	}

	private void notifyUpdate(Status s) {
		// NotificationManager
		NotificationManager nm = (NotificationManager) this
				.getSystemService(Context.NOTIFICATION_SERVICE);
		// Notification
		Notification notification = new Notification(R.drawable.fav_true,
				s.getText(), System.currentTimeMillis());
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0, // requestCode
				new Intent(this, NaotacoStream.class), 0);
		notification.setLatestEventInfo(this, s.getText(), s.getUser()
				.getScreenName(), contentIntent);
		// �ʒm�𔭍s
		nm.notify(0, notification);
	}

	public void onInit(int status) {
		// TODO Auto-generated method stub
		// status can be either TextToSpeech.SUCCESS or TextToSpeech.ERROR.
		if (status == TextToSpeech.SUCCESS) {
			// Set preferred language to US english.
			// Note that a language may not be available, and the result will
			// indicate this.
			// int result = mTts.setLanguage(Locale.US);
			int result = mTts.setLanguage(Locale.JAPAN);
			// Try this someday for some interesting results.
			// int result mTts.setLanguage(Locale.FRANCE);
			if (result == TextToSpeech.LANG_MISSING_DATA
					|| result == TextToSpeech.LANG_NOT_SUPPORTED) {
				// Lanuage data is missing or the language is not supported.

			} else {
				// Check the documentation for other possible result codes.
				// For example, the language may be available for the locale,
				// but not for the specified country and variant.

				// The TTS engine has been successfully initialized.
				// Allow the user to press the button for the app to speak
				// again.

			}
		} else {
			// Initialization failed.
			// Log.e(TAG, "Could not initialize TextToSpeech.");
		}
	}

	private void readText(String s) {

		mTts.speak(s,
		// TextToSpeech.QUEUE_FLUSH, // Drop all pending entries in the playback
		// queue.
				TextToSpeech.QUEUE_ADD, null);
	}

	public boolean toggleTTS() {
		isReading = !isReading;

		return isReading;
	}

}
