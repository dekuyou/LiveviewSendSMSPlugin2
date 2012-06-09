/*
 * Copyright (c) 2010 Sony Ericsson
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * 
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package jp.ddo.dekuyou.liveview.plugins.sendsms2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.preference.PreferenceScreen;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.telephony.SmsManager;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.Log;

import com.sonyericsson.extras.liveview.plugins.AbstractPluginService;
import com.sonyericsson.extras.liveview.plugins.PluginConstants;
import com.sonyericsson.extras.liveview.plugins.PluginUtils;

public class SendSMSPluginService extends AbstractPluginService {

	List<Map<KEY, String>> smsList = new ArrayList<Map<KEY, String>>();
	int msgNo = 1;
	int phoneNo = 0;
	MODE mode = MODE.ID;

	// Our handler.
	private Handler mHandler = null;

	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
		// Create handler.
		if (mHandler == null) {
			mHandler = new Handler();
		}

	}

	// private EditText mUserText;
	//    
	// private ArrayAdapter<String> mAdapter;
	//    
	// private ArrayList<String> mStrings = new ArrayList<String>();

	@Override
	public void onCreate() {
		super.onCreate();



	}


	@Override
	public void onDestroy() {
		super.onDestroy();

		stopWork();
	}

	/**
	 * Plugin is sandbox.
	 */
	protected boolean isSandboxPlugin() {
		return true;
	}

	/**
	 * Must be implemented. Starts plugin work, if any.
	 */
	protected void startWork() {

		msgNo = 1;
		phoneNo = 0;
		mode = MODE.ID;
		smsList = new ArrayList<Map<KEY, String>>();

		
		if(mSharedPreferences.getBoolean("contact0",false)){
			
			setFixContact();
			setSmsContact();
		}else{
			setSmsContact();
			setFixContact();
			
		}

		sendBitmap();

	}
	
	private void setFixContact(){
		
		for (int i = 1; i <= 3; i++) {

			if (!"".equals(mSharedPreferences.getString("contact"
					+ String.valueOf(i), ""))) {
				
				Map<KEY, String> map = new HashMap<KEY, String>();

				map.put(KEY.PHONE,  mSharedPreferences.getString("contact"
						+ String.valueOf(i), ""));
				map.put(KEY.NAME,  mSharedPreferences.getString("contact"
						+ String.valueOf(i), ""));
				
				smsList.add(map);
			}

		}
		
	}

	private void setSmsContact() {
		Uri uriSms = Uri.parse("content://sms/inbox");
		// Uri uriSms = Uri.parse("content://mms-sms/inbox");

		String columns[] = new String[] { "distinct address" };
		Cursor c = getContentResolver()
				.query(uriSms, columns, null, null, null);

		c.moveToFirst();
		CharSequence[] list = new CharSequence[c.getCount()];

		for (int i = 0; i < list.length; i++) {

			String[] proj = new String[] { Phone._ID, Phone.DISPLAY_NAME,
					Phone.NUMBER };

			Uri _uri = Uri.withAppendedPath(Phone.CONTENT_FILTER_URI, Uri
					.encode(c.getString(0)));

			Cursor _cursor = getContentResolver().query(_uri, proj, null, null,
					null);
			Map<KEY, String> map = new HashMap<KEY, String>();

			if (_cursor.getCount() > 0) {
				_cursor.moveToFirst();

				map.put(KEY.PHONE, c.getString(0));
				map.put(KEY.NAME, _cursor.getString(1));

			} else {
				map.put(KEY.PHONE, c.getString(0));
				map.put(KEY.NAME, c.getString(0));
			}

			_cursor.close();

			smsList.add(map);
			c.moveToNext();
		}

		c.close();
	}

	private void sendBitmap() {

		String msg = "";

		switch (mode) {
		case ID:
			msg = getName();
			break;
		case MSG:
			msg = mMsgList.get(msgNo - 1);
			break;

		default:
			break;
		}

		PluginUtils.sendTextBitmap(mLiveViewAdapter, mPluginId, msg, 128, 12);

	}

	/**
	 * Must be implemented. Stops plugin work, if any.
	 */
	protected void stopWork() {

	}

	/**
	 * Must be implemented.
	 * 
	 * PluginService has done connection and registering to the LiveView
	 * Service.
	 * 
	 * If needed, do additional actions here, e.g. starting any worker that is
	 * needed.
	 */
	protected void onServiceConnectedExtended(ComponentName className,
			IBinder service) {

	}

	/**
	 * Must be implemented.
	 * 
	 * PluginService has done disconnection from LiveView and service has been
	 * stopped.
	 * 
	 * Do any additional actions here.
	 */
	protected void onServiceDisconnectedExtended(ComponentName className) {

	}

	/**
	 * Must be implemented.
	 * 
	 * PluginService has checked if plugin has been enabled/disabled.
	 * 
	 * The shared preferences has been changed. Take actions needed.
	 */
	protected void onSharedPreferenceChangedExtended(SharedPreferences prefs,
			String key) {

	}

	protected void startPlugin() {
		Log.d(PluginConstants.LOG_TAG, "startPlugin");
		startWork();
	}

	protected void stopPlugin() {
		Log.d(PluginConstants.LOG_TAG, "stopPlugin");
		stopWork();
	}

	protected void button(String buttonType, boolean doublepress,
			boolean longpress) {
		Log.d(PluginConstants.LOG_TAG, "button - type " + buttonType
				+ ", doublepress " + doublepress + ", longpress " + longpress);

		if (buttonType.equalsIgnoreCase(PluginConstants.BUTTON_UP)) {

			switch (mode) {
			case ID:
				if (phoneNo == 0) {
					phoneNo = smsList.size() - 1;
				} else {
					phoneNo -= 1;
				}

				sendBitmap();

				break;
			case MSG:
				if (msgNo == 1) {
					msgNo = mMsgList.size();
				} else {
					msgNo -= 1;
				}

				sendBitmap();

				break;
			default:
				break;
			}

		} else if (buttonType.equalsIgnoreCase(PluginConstants.BUTTON_DOWN)) {

			switch (mode) {
			case ID:
				if (phoneNo == smsList.size() - 1) {
					phoneNo = 0;
				} else {
					phoneNo += 1;
				}

				sendBitmap();
				break;
			case MSG:
				if (msgNo == mMsgList.size()) {
					msgNo = 1;
				} else {
					msgNo += 1;
				}

				sendBitmap();

				break;
			default:
				break;
			}

		} else if (buttonType.equalsIgnoreCase(PluginConstants.BUTTON_RIGHT)) {
			msgNo = 1;
			mode = MODE.MSG;
			mMsgList = new ArrayList<String>();

			for (int i = 1; i <= 15; i++) {

				if (!"".equals(mSharedPreferences.getString("Msg"
						+ String.valueOf(i), ""))) {

					mMsgList.add(String.valueOf(i)
							+ ":"
							+ mSharedPreferences.getString("Msg"
									+ String.valueOf(i), ""));
				}

			}
			if (mMsgList.size() == 0) {
				mMsgList.add("0:SendSMS");
			}

			sendBitmap();

			doDraw();

		} else if (buttonType.equalsIgnoreCase(PluginConstants.BUTTON_LEFT)) {
			mode = MODE.ID;

			mLiveViewAdapter.clearDisplay(mPluginId);

			sendBitmap();

		} else if (buttonType.equalsIgnoreCase(PluginConstants.BUTTON_SELECT)) {
			switch (mode) {
			case MSG:

				PluginUtils.sendTextBitmap(mLiveViewAdapter, mPluginId,
						"Sending....", 128, 12);

				sendSMS();

				sendBitmap();
				break;

			default:
				break;
			}

		}
	}

	List<String> mMsgList = null;

	private void doDraw() {
		// TODO Auto-generated method stub

		mHandler.postDelayed(new Runnable() {
			public void run() { //
				try {

					Bitmap bitmap = null;
					try {
						bitmap = Bitmap.createBitmap(128, 12,
								Bitmap.Config.RGB_565);
					} catch (IllegalArgumentException e) {
						return;
					}

					Canvas canvas = new Canvas(bitmap);

					// Set the text properties in the canvas
					TextPaint textPaint = new TextPaint();
					textPaint.setTextSize(12);
					textPaint.setColor(Color.WHITE);

					// Create the text layout and draw it to the canvas
					Layout textLayout = new StaticLayout(getName(), textPaint,
							128, Layout.Alignment.ALIGN_NORMAL, 1, 1, false);
					textLayout.draw(canvas);

					mLiveViewAdapter
							.sendImageAsBitmap(mPluginId, 0, 20, bitmap);

					bitmap.recycle();
				} catch (Exception e) {
					Log.e(PluginConstants.LOG_TAG, "Failed to drowBitmap .");
				}

			}
		}, 10);

	}

	private void sendSMS() {
		// TODO Auto-generated method stub

		mHandler.postDelayed(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				SmsManager smsManager = SmsManager.getDefault();

				smsManager.sendTextMessage(getPhoneNo(), null, mMsgList.get(
						msgNo - 1).substring(
						mMsgList.get(msgNo - 1).indexOf(":") + 1), null, null);

				ContentValues values = new ContentValues();
				values.put("address", getPhoneNo());
				values.put("body", mMsgList.get(msgNo - 1).substring(
						mMsgList.get(msgNo - 1).indexOf(":") + 1));
				getContentResolver().insert(Uri.parse("content://sms/sent"),
						values);

			}
		}, 1000L);

	}

	private String getName() {

		Map<KEY, String> tmpMap = smsList.get(phoneNo);

		return tmpMap.get(KEY.NAME);
	}

	private String getPhoneNo() {

		Map<KEY, String> tmpMap = smsList.get(phoneNo);

		return tmpMap.get(KEY.PHONE);
	}

	@Override
	protected void screenMode(int mode) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void displayCaps(int displayWidthPx, int displayHeigthPx) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void onUnregistered() throws RemoteException {
		// TODO Auto-generated method stub

	}

	@Override
	protected void openInPhone(String openInPhoneAction) {
		// TODO Auto-generated method stub

	}

}

enum MODE {
	ID, MSG;
}

enum KEY {
	PHONE, NAME;
}
