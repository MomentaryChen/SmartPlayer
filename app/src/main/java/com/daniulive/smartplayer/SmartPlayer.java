/*
 * SmartPlayer.java
 * SmartPlayer
 * 
 * Github: https://github.com/daniulive/SmarterStreaming
 * 
 * Created by DaniuLive on 2015/09/26.
 * Copyright © 2014~2018 DaniuLive. All rights reserved.
 */

package com.daniulive.smartplayer;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.eventhandle.NTSmartEventCallbackV2;
import com.eventhandle.NTSmartEventID;
import com.videoengine.NTExternalRender;
import com.videoengine.NTRenderer;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;

//import java.io.FileOutputStream;
//import java.io.IOException;
//import android.graphics.YuvImage;  
//import android.graphics.ImageFormat;

public class SmartPlayer extends Activity {

	private SurfaceView sSurfaceView = null;
	String host = "192.168.43.207";
	int port = 6666;
	Socket socket = null;

	private long playerHandle = 0;
    private final String palyerUrl = "rtmp://192.168.43.31/rtmp/live";
	private static final int PORTRAIT = 1; // 竖屏
	private static final int LANDSCAPE = 2; // 横屏
	private static final String TAG = "SmartPlayer";

	private SmartPlayerJniV2 libPlayer = null;

	private int currentOrigentation = PORTRAIT;

	private String playbackUrl = null;

	private boolean isMute = false;

	private boolean isHardwareDecoder = false;

	private int playBuffer = 200; // 默认200ms

	private boolean isLowLatency = false; // 超低延时，默认不开启

	private boolean isFastStartup = true; // 是否秒开, 默认true

	private int rotate_degrees = 0;

	private boolean switchUrlFlag = false;

	private String switchURL = "rtmp://live.hkstv.hk.lxdns.com/live/hks";

	private String imageSavePath;

	private String recDir = "/sdcard/daniulive/playrec"; // for recorder path

	private boolean isPlaying = false;
	private boolean isRecording = false;

	// Button btnPopInputText;
	Button btnPopInputUrl;
	Button btnMute;
	Button btnStartStopPlayback;
	Button btnStartStopRecorder;
	Button btnRecoderMgr;
	Button btnHardwareDecoder;
	Button btnCaptureImage;
	Button btnFastStartup;
	Button btnSetPlayBuffer;
	Button btnLowLatency;
	Button btnRotation;
	Button btnFood;
	Button btnSwitchUrl;
	TextView txtCopyright;
	TextView txtQQQun;

	LinearLayout lLayout = null;
	FrameLayout fFrameLayout = null;
	private Context myContext;

	static {
		System.loadLibrary("SmartPlayer");
	}
	private void invisibilityAllButton(){
        btnPopInputUrl.setVisibility(View.INVISIBLE);
        btnMute.setVisibility(View.VISIBLE);
        //btnStartStopPlayback.setVisibility(View.INVISIBLE);
        //btnStartStopRecorder.setVisibility(View.INVISIBLE);
        btnRecoderMgr.setVisibility(View.INVISIBLE);
        btnHardwareDecoder.setVisibility(View.INVISIBLE);
        //btnCaptureImage.setVisibility(View.INVISIBLE);
        btnFastStartup.setVisibility(View.INVISIBLE);
        btnSetPlayBuffer.setVisibility(View.INVISIBLE);
        btnLowLatency.setVisibility(View.INVISIBLE);
        btnRotation.setVisibility(View.INVISIBLE);
        btnFood.setVisibility(View.VISIBLE);
    }

    private void visibilityAllButton(){
        btnPopInputUrl.setVisibility(View.INVISIBLE);
        btnMute.setVisibility(View.VISIBLE);
        //btnStartStopPlayback.setVisibility(View.VISIBLE);
        //btnStartStopRecorder.setVisibility(View.VISIBLE);
        btnRecoderMgr.setVisibility(View.INVISIBLE);
        btnHardwareDecoder.setVisibility(View.INVISIBLE);
        btnCaptureImage.setVisibility(View.INVISIBLE);
        btnFastStartup.setVisibility(View.INVISIBLE);
        btnSetPlayBuffer.setVisibility(View.INVISIBLE);
        btnLowLatency.setVisibility(View.INVISIBLE);
        btnRotation.setVisibility(View.INVISIBLE);
        btnFood.setVisibility(View.INVISIBLE);
    }

	@Override
	protected void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		Log.i(TAG, "Run into OnCreate++");

		libPlayer = new SmartPlayerJniV2();
		myContext = this.getApplicationContext();

		// 设置快照路径(具体路径可自行设置)
		File storageDir = getOwnCacheDirectory(myContext, "daniuimage");// 创建保存的路径
		imageSavePath = storageDir.getPath();
		Log.i(TAG, "快照存储路径: " + imageSavePath);

		boolean bViewCreated = CreateView();

		if (bViewCreated) {
			inflateLayout(LinearLayout.VERTICAL);
		}
	}

	/*
	 * For smartplayer demo app, the url is based on: baseURL + inputID For
	 * example: baseURL: rtmp://player.daniulive.com:1935/hls/stream inputID:
	 * 123456 playbackUrl: rtmp://player.daniulive.com:1935/hls/stream123456
	 */
	private void GenerateURL(String id) {
		if (id == null)
			return;

		if (id.equals("hks")) {
			playbackUrl = "rtmp://live.hkstv.hk.lxdns.com/live/hks";
			return;
		}

		btnStartStopPlayback.setEnabled(true);
		String baseURL = "rtmp://player.daniulive.com:1935/hls/stream";

		playbackUrl = baseURL + id;
	}

	private void SaveInputUrl(String url) {
		playbackUrl = "";

		if (url == null)
			return;

		if (url.equals("hks")) {
			btnStartStopPlayback.setEnabled(true);
			playbackUrl = "rtmp://live.hkstv.hk.lxdns.com/live/hks";

			Log.i(TAG, "Input url:" + playbackUrl);

			return;
		}

		// rtmp:/
		if (url.length() < 8) {
			Log.e(TAG, "Input full url error:" + url);
			return;
		}

		if (!url.startsWith("rtmp://") && !url.startsWith("rtsp://")) {
			Log.e(TAG, "Input full url error:" + url);
			return;
		}

		btnStartStopPlayback.setEnabled(true);
		playbackUrl = url;

		Log.i(TAG, "Input full url:" + url);
	}

	private void SaveInputPlayBuffer(String bufferText) {
		try {
			Integer intValue;
			intValue = Integer.valueOf(bufferText);

			playBuffer = intValue;

			Log.i(TAG, "Input play buffer:" + playBuffer);

		} catch (NumberFormatException e) {
			Log.i(TAG, "Input play buffer convert exception");

			e.printStackTrace();
		}
	}

	/* Popup InputID dialog */
	private void PopDialog() {
		final EditText inputID = new EditText(this);
		inputID.setFocusable(true);

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(
				"如 rtmp://player.daniulive.com:1935/hls/stream123456,请输入123456")
				.setView(inputID).setNegativeButton("取消", null);
		builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface dialog, int which) {
				String strID = inputID.getText().toString();
				GenerateURL(strID);
			}
		});
		builder.show();
	}

	private void PopFullUrlDialog() {
		final EditText inputUrlTxt = new EditText(this);
		inputUrlTxt.setFocusable(true);
		inputUrlTxt.setText("rtsp://184.72.239.149/vod/mp4://BigBuckBunny_175k.mov");

		AlertDialog.Builder builderUrl = new AlertDialog.Builder(this);
		builderUrl
				.setTitle("如 rtmp://player.daniulive.com:1935/hls/stream123456")
				.setView(inputUrlTxt).setNegativeButton("取消", null);
		builderUrl.setPositiveButton("確認",
				new DialogInterface.OnClickListener() {

					public void onClick(DialogInterface dialog, int which) {
						String fullUrl = inputUrlTxt.getText().toString();
						SaveInputUrl(fullUrl);
					}
				});
		builderUrl.show();
	}

	private void PopSettingBufferDialog() {
		final EditText inputBuferTxt = new EditText(this);
		inputBuferTxt.setFocusable(true);

		String str = "";
		str += playBuffer;

		inputBuferTxt.setText(str);

		AlertDialog.Builder builderBuffer = new AlertDialog.Builder(this);

		builderBuffer.setTitle("设置播放缓冲(毫秒),默认200ms").setView(inputBuferTxt)
				.setNegativeButton("取消", null);

		builderBuffer.setPositiveButton("确认",
				new DialogInterface.OnClickListener() {

					public void onClick(DialogInterface dialog, int which) {
						String bufferText = inputBuferTxt.getText().toString();
						SaveInputPlayBuffer(bufferText);
					}
				});

		builderBuffer.show();
	}

	/* Generate basic layout */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void inflateLayout(int orientation) {
		if (null == lLayout)
			lLayout = new LinearLayout(this);

		addContentView(lLayout, new LayoutParams(
				LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT));

		lLayout.setOrientation(orientation);

		fFrameLayout = new FrameLayout(this);

		LayoutParams lp = new LinearLayout.LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, 1.0f);
		fFrameLayout.setLayoutParams(lp);
		Log.i(TAG, "++inflateLayout..");

		sSurfaceView.setLayoutParams(new LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

		fFrameLayout.addView(sSurfaceView, 0);

		RelativeLayout outLinearLayout = new RelativeLayout(this);
		outLinearLayout.setLayoutParams(new LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.FILL_PARENT));

		LinearLayout lLinearLayout = new LinearLayout(this);
		lLinearLayout.setOrientation(LinearLayout.VERTICAL);
		lLinearLayout.setLayoutParams(new LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));

		LinearLayout copyRightLinearLayout = new LinearLayout(this);
		copyRightLinearLayout.setOrientation(LinearLayout.HORIZONTAL);

        RelativeLayout.LayoutParams rl = new RelativeLayout.LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		rl.topMargin = getWindowManager().getDefaultDisplay().getHeight() - 270;
		rl.leftMargin = getWindowManager().getDefaultDisplay().getWidth()*4/5;
		copyRightLinearLayout.setLayoutParams(rl);

		btnFood = new Button(this);
        btnFood.setText("餵食");
        btnFood.setLayoutParams(new LinearLayout.LayoutParams(
                LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        copyRightLinearLayout.addView(btnFood);

        btnFood.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View view) {
                Toast.makeText(SmartPlayer.this,"已餵食",Toast.LENGTH_SHORT).show();
                new SendCode("111").start();
            }
        });
		/*
		txtCopyright = new TextView(this);
		txtCopyright.setLayoutParams(new LinearLayout.LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		txtCopyright
				.setText("Copyright 2014~2016 www.daniulive.com v1.0.16.0326");
		copyRightLinearLayout.addView(txtCopyright, 0);
		/*
		txtQQQun = new TextView(this);
		txtQQQun.setLayoutParams(new LinearLayout.LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		txtQQQun.setText("QQ群:294891451,  499687479");
		copyRightLinearLayout.addView(txtQQQun, 1);*/

		/* PopInput button */
		/*
		 * btnPopInputText = new Button(this);
		 * btnPopInputText.setText("输入urlID");
		 * btnPopInputText.setLayoutParams(new
		 * LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		 * lLinearLayout.addView(btnPopInputText, 0);
		 */
        btnMute = new Button(this);

        if (!isMute) {
            btnMute.setText("静音 ");
        } else {
            btnMute.setText("取消靜音");
        }

        btnStartStopPlayback = new Button(this);
        btnStartStopPlayback.setText("开始播放 ");
        btnStartStopPlayback.setLayoutParams(new LayoutParams(
                LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        lLinearLayout.addView(btnStartStopPlayback);

        /* Start/stop recorder stream button */
        LinearLayout recorderLinearLayout = new LinearLayout(this);
        btnStartStopRecorder = new Button(this);
        btnStartStopRecorder.setText("開始錄影");
        btnStartStopRecorder.setLayoutParams(new LayoutParams(
                LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        recorderLinearLayout.addView(btnStartStopRecorder);

        btnRecoderMgr = new Button(this);
        btnRecoderMgr.setText("錄影管理 ");
        btnRecoderMgr.setLayoutParams(new LayoutParams(
                LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        recorderLinearLayout.addView(btnRecoderMgr);
        lLinearLayout.addView(recorderLinearLayout);


        btnRecoderMgr.setOnClickListener(new ButtonRecorderMangerListener());
        outLinearLayout.addView(lLinearLayout, 0);
        outLinearLayout.addView(copyRightLinearLayout, 1);
        fFrameLayout.addView(outLinearLayout, 1);

        lLayout.addView(fFrameLayout, 0);

        btnMute.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT));
        lLinearLayout.addView(btnMute);

		btnPopInputUrl = new Button(this);
		btnPopInputUrl.setText("輸入url");
		btnPopInputUrl.setLayoutParams(new LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		lLinearLayout.addView(btnPopInputUrl);

		/* mute button */
		/* switch url button */
		/*
		btnSwitchUrl = new Button(this);

		if (!switchUrlFlag) {
			btnSwitchUrl.setText("切换url1");
		} else {
			btnSwitchUrl.setText("切换url2");
		}

		btnSwitchUrl.setLayoutParams(new LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		lLinearLayout.addView(btnSwitchUrl);*/

		/* hardware decoder button */
		btnHardwareDecoder = new Button(this);

		if (!isHardwareDecoder) {
			btnHardwareDecoder.setText("目前軟解碼");
		} else {
			btnHardwareDecoder.setText("目前硬解碼");
		}

		LinearLayout LinearLayoutImage = new LinearLayout(this);
		LinearLayoutImage.setOrientation(LinearLayout.HORIZONTAL);
		LinearLayoutImage.setLayoutParams(new LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));

		/* capture image button */
		btnCaptureImage = new Button(this);

		btnCaptureImage.setText("拍照");
		btnCaptureImage.setLayoutParams(new LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		LinearLayoutImage.addView(btnCaptureImage);

		btnRotation = new Button(this);

		if (0 == rotate_degrees) {
			btnRotation.setText("旋轉90度");
		} else if (90 == rotate_degrees) {
			btnRotation.setText("旋轉180度");
		} else if (180 == rotate_degrees) {
			btnRotation.setText("旋轉270度");
		} else if (270 == rotate_degrees) {
			btnRotation.setText("不旋轉");
		}

		btnRotation.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT));
		LinearLayoutImage.addView(btnRotation);

		lLinearLayout.addView(LinearLayoutImage);

		btnHardwareDecoder.setLayoutParams(new LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		lLinearLayout.addView(btnHardwareDecoder);

		// buffer setting++

		LinearLayout bufferLinearLayout = new LinearLayout(this);
		bufferLinearLayout.setOrientation(LinearLayout.HORIZONTAL);
		bufferLinearLayout.setLayoutParams(new LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));

		btnSetPlayBuffer = new Button(this);
		btnSetPlayBuffer.setText("設置緩衝");
		btnSetPlayBuffer.setLayoutParams(new LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		bufferLinearLayout.addView(btnSetPlayBuffer);

		btnLowLatency = new Button(this);

		if (isLowLatency) {
			btnLowLatency.setText("正常延时");
		} else {
			btnLowLatency.setText("超低延时");
		}

		btnLowLatency.setLayoutParams(new LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		bufferLinearLayout.addView(btnLowLatency);
		btnFastStartup = new Button(this);

		if (isFastStartup) {
			btnFastStartup.setText("停用秒開");
		} else {
			btnFastStartup.setText("啟用秒開");
		}

		btnFastStartup.setLayoutParams(new LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		bufferLinearLayout.addView(btnFastStartup);

		lLinearLayout.addView(bufferLinearLayout);

		// buffer setting--

		/* Start playback stream button */


		if (isPlaying || isRecording) {
			btnPopInputUrl.setEnabled(false);
			btnHardwareDecoder.setEnabled(false);

			btnSetPlayBuffer.setEnabled(false);
			btnLowLatency.setEnabled(false);
			btnFastStartup.setEnabled(false);
			btnRecoderMgr.setEnabled(false);
		} else {
			btnPopInputUrl.setEnabled(true);
			btnHardwareDecoder.setEnabled(true);

			btnSetPlayBuffer.setEnabled(true);
			btnLowLatency.setEnabled(true);
			btnFastStartup.setEnabled(true);
			btnRecoderMgr.setEnabled(true);
		}

		if (isPlaying) {
			btnStartStopPlayback.setText("停止播放 ");

		} else {
			btnStartStopPlayback.setText("開始撥放 ");
		}

		if (isRecording) {
			btnStartStopRecorder.setText("停止錄影");
            Toast.makeText(this,"錄影停止",Toast.LENGTH_SHORT);
		} else {
			btnStartStopRecorder.setText("開始錄影");
            Toast.makeText(this,"錄影開始",Toast.LENGTH_SHORT);
		}

		/* PopInput button listener */

		/*
		 * btnPopInputText.setOnClickListener(new Button.OnClickListener() {
		 *
		 * public void onClick(View v) { Log.i(TAG,
		 * "Run into input playback ID++");
		 *
		 * PopDialog();
		 *
		 * Log.i(TAG, "Run out from input playback ID--"); } });
		 */

		btnPopInputUrl.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {

				//PopFullUrlDialog();
			}
		});

		btnMute.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				isMute = !isMute;

				if (isMute) {
					btnMute.setText("取消静音");
				} else {
					btnMute.setText("静音");
				}

				if (playerHandle != 0) {
					libPlayer.SmartPlayerSetMute(playerHandle, isMute ? 1 : 0);
				}
			}
		});

		/*btnSwitchUrl.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				switchUrlFlag = !switchUrlFlag;

				if (switchUrlFlag) {
					btnSwitchUrl.setText("切换url2");

					switchURL = "rtmp://live.hkstv.hk.lxdns.com/live/hks"; //
					// 实际以可切换url为准
					//switchURL = "rtmp://player.daniulive.com:1935/hls/stream2";
				} else {
					btnSwitchUrl.setText("切换url1");
					switchURL = playbackUrl;
				}

				if (playerHandle != 0) {
					libPlayer.SmartPlayerSwitchPlaybackUrl(playerHandle,
							switchURL);
				}
			}
		});*/

		btnHardwareDecoder.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				isHardwareDecoder = !isHardwareDecoder;

				if (isHardwareDecoder) {
					btnHardwareDecoder.setText("目前軟解碼");
				} else {
					btnHardwareDecoder.setText("目前硬解碼");
				}

			}
		});

		btnCaptureImage.setOnClickListener(new OnClickListener() {
			@SuppressLint("SimpleDateFormat")
			public void onClick(View v) {

				String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss")
						.format(new Date());
				String imageFileName = "dn_" + timeStamp; // 创建以时间命名的文件名称

				String imagePath = imageSavePath + "/" + imageFileName + ".png";

				Log.i(TAG, "imagePath:" + imagePath);

				libPlayer.SmartPlayerSaveCurImage(playerHandle, imagePath);
			}
		});

		btnRotation.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {

				rotate_degrees += 90;
				rotate_degrees = rotate_degrees % 360;

				if (0 == rotate_degrees) {
					btnRotation.setText("旋轉90度");
				} else if (90 == rotate_degrees) {
					btnRotation.setText("旋轉180度");
				} else if (180 == rotate_degrees) {
					btnRotation.setText("旋轉270度");
				} else if (270 == rotate_degrees) {
					btnRotation.setText("不旋轉");
				}

				if (playerHandle != 0) {
					libPlayer.SmartPlayerSetRotation(playerHandle,
							rotate_degrees);
				}
			}
		});

		btnSetPlayBuffer.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				PopSettingBufferDialog();
			}
		});

		btnLowLatency.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				isLowLatency = !isLowLatency;

				if (isLowLatency) {
					playBuffer = 0;
					Log.i(TAG, "low latency mode, set playBuffer to 0");
					btnLowLatency.setText("正常延时");
				} else {
					btnLowLatency.setText("超低延时");
				}
			}
		});

		btnFastStartup.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				isFastStartup = !isFastStartup;

				if (isFastStartup) {
					btnFastStartup.setText("停用秒开");
				} else {
					btnFastStartup.setText("開啟秒开");
				}
			}
		});

		btnStartStopRecorder.setOnClickListener(new OnClickListener() {

			// @Override
			public void onClick(View v) {

				if (isRecording) {

					int iRet = libPlayer.SmartPlayerStopRecorder(playerHandle);

					if (iRet != 0) {
						Log.e(TAG, "SmartPlayerStopRecorder strem failed..");
						return;
					}

					if (!isPlaying) {
						btnPopInputUrl.setEnabled(true);
						btnSetPlayBuffer.setEnabled(true);
						btnFastStartup.setEnabled(true);
						btnRecoderMgr.setEnabled(true);

						libPlayer.SmartPlayerClose(playerHandle);
						playerHandle = 0;
					}

					btnStartStopRecorder.setText(" 開始錄影");

					isRecording = false;

					return;
				} else {
					Log.i(TAG, "onClick start recorder..");

					if (!isPlaying) {
						InitAndSetConfig();
					}

					ConfigRecorderFuntion();

					int startRet = libPlayer.SmartPlayerStartRecorder(playerHandle);

					if (startRet != 0) {
						Log.e(TAG, "Failed to start recorder.");
						return;
					}

					btnPopInputUrl.setEnabled(false);
					btnSetPlayBuffer.setEnabled(false);
					btnFastStartup.setEnabled(false);
					btnRecoderMgr.setEnabled(false);

					isRecording = true;
					btnStartStopRecorder.setText("停止錄影");
				}
			}
		});

		btnStartStopPlayback.setOnClickListener(new OnClickListener() {

			// @Override
			public void onClick(View v) {

				if (isPlaying) {
					Log.i(TAG, "Stop playback stream++");

					int iRet = libPlayer.SmartPlayerStopPlay(playerHandle);
					if (iRet != 0) {
						Log.e(TAG, "SmartPlayerStopPlay strem failed..");
						return;
					}
                    visibilityAllButton();
					btnHardwareDecoder.setEnabled(true);
					btnLowLatency.setEnabled(true);

					if (!isRecording) {
						btnPopInputUrl.setEnabled(true);
						btnSetPlayBuffer.setEnabled(true);
						btnFastStartup.setEnabled(true);

						btnRecoderMgr.setEnabled(true);
						libPlayer.SmartPlayerClose(playerHandle);
						playerHandle = 0;
					}

					isPlaying = false;
					btnStartStopPlayback.setText("開始播放 ");
					Log.i(TAG, "Stop playback stream--");
				} else {
					Log.i(TAG, "Start playback stream++");
                    invisibilityAllButton();
					if (!isRecording) {
						InitAndSetConfig();
					}

					// 如果第二个参数设置为null，则播放纯音频
					libPlayer.SmartPlayerSetSurface(playerHandle, sSurfaceView);
					// External Render test
					// libPlayer.SmartPlayerSetExternalRender(playerHandle, new
					// RGBAExternalRender());
					// libPlayer.SmartPlayerSetExternalRender(playerHandle, new
					// I420ExternalRender());

					libPlayer.SmartPlayerSetAudioOutputType(playerHandle, 0);

					if (isMute) {
						libPlayer.SmartPlayerSetMute(playerHandle, isMute ? 1
								: 0);
					}

					if (isHardwareDecoder) {
						Log.i(TAG, "check isHardwareDecoder: "
								+ isHardwareDecoder);

						int hwChecking = libPlayer
								.SetSmartPlayerVideoHWDecoder(playerHandle,
										isHardwareDecoder ? 1 : 0);

						Log.i(TAG, "[daniulive] hwChecking: " + hwChecking);
					}

					libPlayer.SmartPlayerSetLowLatencyMode(playerHandle, isLowLatency ? 1
							: 0);

					libPlayer.SmartPlayerSetRotation(playerHandle, rotate_degrees);

					int iPlaybackRet = libPlayer
							.SmartPlayerStartPlay(playerHandle);

					if (iPlaybackRet != 0) {
						Log.e(TAG, "StartPlayback strem failed..");
						return;
					}

					btnStartStopPlayback.setText("停止播放 ");

					btnPopInputUrl.setEnabled(false);
					btnHardwareDecoder.setEnabled(false);
					btnSetPlayBuffer.setEnabled(false);
					btnLowLatency.setEnabled(false);
					btnFastStartup.setEnabled(false);
					btnRecoderMgr.setEnabled(false);

					isPlaying = true;
					Log.i(TAG, "Start playback stream--");
				}
			}
		});
        visibilityAllButton();
        SaveInputUrl(palyerUrl);
	}

	public static final String bytesToHexString(byte[] buffer) {
		StringBuffer sb = new StringBuffer(buffer.length);
		String temp;

		for (int i = 0; i < buffer.length; ++i) {
			temp = Integer.toHexString(0xff & buffer[i]);
			if (temp.length() < 2)
				sb.append(0);

			sb.append(temp);
		}

		return sb.toString();
	}

	class RGBAExternalRender implements NTExternalRender {
		// public static final int NT_FRAME_FORMAT_RGBA = 1;
		// public static final int NT_FRAME_FORMAT_ABGR = 2;
		// public static final int NT_FRAME_FORMAT_I420 = 3;

		private int width_ = 0;
		private int height_ = 0;
		private int row_bytes_ = 0;
		private ByteBuffer rgba_buffer_ = null;

		@Override
		public int getNTFrameFormat() {
			Log.i(TAG, "RGBAExternalRender::getNTFrameFormat return "
					+ NT_FRAME_FORMAT_RGBA);
			return NT_FRAME_FORMAT_RGBA;
		}

		@Override
		public void onNTFrameSizeChanged(int width, int height) {
			width_ = width;
			height_ = height;

			row_bytes_ = width_ * 4;

			Log.i(TAG, "RGBAExternalRender::onNTFrameSizeChanged width_:"
					+ width_ + " height_:" + height_);

			rgba_buffer_ = ByteBuffer.allocateDirect(row_bytes_ * height_);
		}

		@Override
		public ByteBuffer getNTPlaneByteBuffer(int index) {
			if (index == 0) {
				return rgba_buffer_;
			} else {
				Log.e(TAG,
						"RGBAExternalRender::getNTPlaneByteBuffer index error:"
								+ index);
				return null;
			}
		}

		@Override
		public int getNTPlanePerRowBytes(int index) {
			if (index == 0) {
				return row_bytes_;
			} else {
				Log.e(TAG,
						"RGBAExternalRender::getNTPlanePerRowBytes index error:"
								+ index);
				return 0;
			}
		}

		public void onNTRenderFrame(int width, int height, long timestamp) {
			if (rgba_buffer_ == null)
				return;

			rgba_buffer_.rewind();

			// copy buffer

			// test
			// byte[] test_buffer = new byte[16];
			// rgba_buffer_.get(test_buffer);

			Log.i(TAG, "RGBAExternalRender:onNTRenderFrame w=" + width + " h="
					+ height + " timestamp=" + timestamp);

			// Log.i(TAG, "RGBAExternalRender:onNTRenderFrame rgba:" +
			// bytesToHexString(test_buffer));
		}
	}

	class I420ExternalRender implements NTExternalRender {
		// public static final int NT_FRAME_FORMAT_RGBA = 1;
		// public static final int NT_FRAME_FORMAT_ABGR = 2;
		// public static final int NT_FRAME_FORMAT_I420 = 3;

		private int width_ = 0;
		private int height_ = 0;

		private int y_row_bytes_ = 0;
		private int u_row_bytes_ = 0;
		private int v_row_bytes_ = 0;

		private ByteBuffer y_buffer_ = null;
		private ByteBuffer u_buffer_ = null;
		private ByteBuffer v_buffer_ = null;

		@Override
		public int getNTFrameFormat() {
			Log.i(TAG, "I420ExternalRender::getNTFrameFormat return "
					+ NT_FRAME_FORMAT_I420);
			return NT_FRAME_FORMAT_I420;
		}

		@Override
		public void onNTFrameSizeChanged(int width, int height) {
			width_ = width;
			height_ = height;

			y_row_bytes_ = (width_ + 15) & (~15);
			u_row_bytes_ = ((width_ + 1) / 2 + 15) & (~15);
			v_row_bytes_ = ((width_ + 1) / 2 + 15) & (~15);

			y_buffer_ = ByteBuffer.allocateDirect(y_row_bytes_ * height_);
			u_buffer_ = ByteBuffer.allocateDirect(u_row_bytes_
					* ((height_ + 1) / 2));
			v_buffer_ = ByteBuffer.allocateDirect(v_row_bytes_
					* ((height_ + 1) / 2));

			Log.i(TAG, "I420ExternalRender::onNTFrameSizeChanged width_="
					+ width_ + " height_=" + height_ + " y_row_bytes_="
					+ y_row_bytes_ + " u_row_bytes_=" + u_row_bytes_
					+ " v_row_bytes_=" + v_row_bytes_);
		}

		@Override
		public ByteBuffer getNTPlaneByteBuffer(int index) {
			if (index == 0) {
				return y_buffer_;
			} else if (index == 1) {
				return u_buffer_;
			} else if (index == 2) {
				return v_buffer_;
			} else {
				Log.e(TAG, "I420ExternalRender::getNTPlaneByteBuffer index error:" + index);
				return null;
			}
		}

		@Override
		public int getNTPlanePerRowBytes(int index) {
			if (index == 0) {
				return y_row_bytes_;
			} else if (index == 1) {
				return u_row_bytes_;
			} else if (index == 2) {
				return v_row_bytes_;
			} else {
				Log.e(TAG, "I420ExternalRender::getNTPlanePerRowBytes index error:" + index);
				return 0;
			}
		}

    	public void onNTRenderFrame(int width, int height, long timestamp)
    	{
    		if ( y_buffer_ == null )
    			return;

    		if ( u_buffer_ == null )
    			return;

    		if ( v_buffer_ == null )
    			return;


    		y_buffer_.rewind();

    		u_buffer_.rewind();

    		v_buffer_.rewind();

    		/*
    		if ( !is_saved_image )
    		{
    			is_saved_image = true;

    			int y_len = y_row_bytes_*height_;

    			int u_len = u_row_bytes_*((height_+1)/2);
    			int v_len = v_row_bytes_*((height_+1)/2);

    			int data_len = y_len + (y_row_bytes_*((height_+1)/2));

    			byte[] nv21_data = new byte[data_len];

    			byte[] u_data = new byte[u_len];
    			byte[] v_data = new byte[v_len];

    			y_buffer_.get(nv21_data, 0, y_len);
    			u_buffer_.get(u_data, 0, u_len);
    			v_buffer_.get(v_data, 0, v_len);

    			int[] strides = new int[2];
    			strides[0] = y_row_bytes_;
    			strides[1] = y_row_bytes_;


    			int loop_row_c = ((height_+1)/2);
    			int loop_c = ((width_+1)/2);

    			int dst_row = y_len;
    			int src_v_row = 0;
    			int src_u_row = 0;

    			for ( int i = 0; i < loop_row_c; ++i)
    			{
    				int dst_pos = dst_row;

    				for ( int j = 0; j <loop_c; ++j )
    				{
    					nv21_data[dst_pos++] = v_data[src_v_row + j];
    					nv21_data[dst_pos++] = u_data[src_u_row + j];
    				}

    				dst_row   += y_row_bytes_;
    				src_v_row += v_row_bytes_;
    				src_u_row += u_row_bytes_;
    			}

    			String imagePath = "/sdcard" + "/" + "testonv21" + ".jpeg";

    			Log.e(TAG, "I420ExternalRender::begin test save iamge++ image_path:" + imagePath);

    			try
    			{
    				File file = new File(imagePath);

        			FileOutputStream image_os = new FileOutputStream(file);

        			YuvImage image = new YuvImage(nv21_data, ImageFormat.NV21, width_, height_, strides);

        			image.compressToJpeg(new android.graphics.Rect(0, 0, width_, height_), 50, image_os);

        			image_os.flush();
        			image_os.close();
    			}
    			catch(IOException e)
    			{
    				e.printStackTrace();
    			}

    			Log.e(TAG, "I420ExternalRender::begin test save iamge--");
    		}

    		*/


    		 Log.i(TAG, "I420ExternalRender::onNTRenderFrame w=" + width + " h=" + height + " timestamp=" + timestamp);

    		 // copy buffer

    		// test
    		// byte[] test_buffer = new byte[16];
    		// y_buffer_.get(test_buffer);

    		// Log.i(TAG, "I420ExternalRender::onNTRenderFrame y data:" + bytesToHexString(test_buffer));

    		// u_buffer_.get(test_buffer);
    		// Log.i(TAG, "I420ExternalRender::onNTRenderFrame u data:" + bytesToHexString(test_buffer));

    		// v_buffer_.get(test_buffer);
    		// Log.i(TAG, "I420ExternalRender::onNTRenderFrame v data:" + bytesToHexString(test_buffer));
    	}
    }

	class EventHandeV2 implements NTSmartEventCallbackV2 {
		@Override
		public void onNTSmartEventCallbackV2(long handle, int id, long param1,
				long param2, String param3, String param4, Object param5) {

			//Log.i(TAG, "EventHandeV2: handle=" + handle + " id:" + id);

			switch (id) {
			case NTSmartEventID.EVENT_DANIULIVE_ERC_PLAYER_STARTED:
				Log.i(TAG, "開始。。");
				break;
			case NTSmartEventID.EVENT_DANIULIVE_ERC_PLAYER_CONNECTING:
				Log.i(TAG, "連接中。。");
				break;
			case NTSmartEventID.EVENT_DANIULIVE_ERC_PLAYER_CONNECTION_FAILED:
				Log.i(TAG, "連接失敗。。");
				break;
			case NTSmartEventID.EVENT_DANIULIVE_ERC_PLAYER_CONNECTED:
				Log.i(TAG, "連接成功。。");
				break;
			case NTSmartEventID.EVENT_DANIULIVE_ERC_PLAYER_DISCONNECTED:
				Log.i(TAG, "連接中斷。。");
				break;
			case NTSmartEventID.EVENT_DANIULIVE_ERC_PLAYER_STOP:
				Log.i(TAG, "停止播放。。");
				break;
			case NTSmartEventID.EVENT_DANIULIVE_ERC_PLAYER_RESOLUTION_INFO:
				Log.i(TAG, "分辨率信息: width: " + param1 + ", height: " + param2);
				break;
			case NTSmartEventID.EVENT_DANIULIVE_ERC_PLAYER_NO_MEDIADATA_RECEIVED:
				Log.i(TAG, "收不到媒體數據，可能是url錯誤。。");
				break;
			case NTSmartEventID.EVENT_DANIULIVE_ERC_PLAYER_SWITCH_URL:
				Log.i(TAG, "切换播放URL。。");
				break;
			case NTSmartEventID.EVENT_DANIULIVE_ERC_PLAYER_CAPTURE_IMAGE:
				Log.i(TAG, "快照: " + param1 + " 路径：" + param3);

				if (param1 == 0) {
					Log.i(TAG, "截取快照成功。.");
				} else {
					Log.i(TAG, "截取快照失敗。.");
				}
				break;

			case NTSmartEventID.EVENT_DANIULIVE_ERC_PLAYER_RECORDER_START_NEW_FILE:
           	 	Log.i(TAG, "[record]開始一个新的录像文件 : " + param3);
                break;
            case NTSmartEventID.EVENT_DANIULIVE_ERC_PLAYER_ONE_RECORDER_FILE_FINISHED:
           	 Log.i(TAG, "[record]已生成一个錄影文件 : " + param3);
                break;

			case NTSmartEventID.EVENT_DANIULIVE_ERC_PLAYER_START_BUFFERING:
				Log.i(TAG, "Start_Buffering");
				break;

			case NTSmartEventID.EVENT_DANIULIVE_ERC_PLAYER_BUFFERING:
				Log.i(TAG, "Buffering:" + param1 + "%");
				break;

			case NTSmartEventID.EVENT_DANIULIVE_ERC_PLAYER_STOP_BUFFERING:
				Log.i(TAG, "Stop_Buffering");
				break;

			case NTSmartEventID.EVENT_DANIULIVE_ERC_PLAYER_DOWNLOAD_SPEED:
				Log.i(TAG, "download_speed:" + param1 + "Byte/s" + ", "
						+ (param1 * 8 / 1000) + "kbps" + ", " + (param1 / 1024)
						+ "KB/s");
				break;
			}
		}
	}

	/* Create rendering */
	private boolean CreateView() {

		if (sSurfaceView == null) {
			/*
			 * useOpenGLES2: If with true: Check if system supports openGLES, if
			 * supported, it will choose openGLES. If with false: it will set
			 * with default surfaceView;
			 */
			sSurfaceView = NTRenderer.CreateRenderer(this, false);

		}

		if (sSurfaceView == null) {
			Log.i(TAG, "Create render failed..");
			return false;
		}

		return true;
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);

		Log.i(TAG, "Run into onConfigurationChanged++");

		if (null != fFrameLayout) {
			fFrameLayout.removeAllViews();
			fFrameLayout = null;
		}

		if (null != lLayout) {
			lLayout.removeAllViews();
			lLayout = null;
		}

		if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
			Log.i(TAG, "onConfigurationChanged, with LANDSCAPE。。");

			inflateLayout(LinearLayout.HORIZONTAL);

			currentOrigentation = LANDSCAPE;
		} else {
			Log.i(TAG, "onConfigurationChanged, with PORTRAIT。。");

			inflateLayout(LinearLayout.VERTICAL);

			currentOrigentation = PORTRAIT;
		}

		if (!isPlaying)
			return;

		libPlayer.SmartPlayerSetOrientation(playerHandle, currentOrigentation);

		Log.i(TAG, "Run out of onConfigurationChanged--");
	}

    @Override
    protected void onResume() {
    	Log.i(TAG, "Run into activity onResume++");

    	if(isPlaying && playerHandle != 0)
    	{
    		libPlayer.SmartPlayerSetOrientation(playerHandle, currentOrigentation);
    	}

        super.onResume();
    }

	@Override
	protected void onDestroy() {
		Log.i(TAG, "Run into activity destory++");
		if (playerHandle != 0) {
			if (isPlaying) {
				libPlayer.SmartPlayerStopPlay(playerHandle);
			}

			if (isRecording) {
				libPlayer.SmartPlayerStopRecorder(playerHandle);
			}

			libPlayer.SmartPlayerClose(playerHandle);
			playerHandle = 0;
		}
		super.onDestroy();
		new SendCode("10").start();
	}

	/**
	 * 根据目录创建文件夹
	 *
	 * @param context
	 * @param cacheDir
	 * @return
	 */
	public static File getOwnCacheDirectory(Context context, String cacheDir) {
		File appCacheDir = null;
		// 判断sd卡正常挂载并且拥有权限的时候创建文件
		if (Environment.MEDIA_MOUNTED.equals(Environment
				.getExternalStorageState())
				&& hasExternalStoragePermission(context)) {
			appCacheDir = new File(Environment.getExternalStorageDirectory(),
					cacheDir);
			Log.i(TAG, "appCacheDir: " + appCacheDir);
		}
		if (appCacheDir == null || !appCacheDir.exists()
				&& !appCacheDir.mkdirs()) {
			appCacheDir = context.getCacheDir();
		}
		return appCacheDir;
	}

	/**
	 * 检查是否有权限
	 *
	 * @param context
	 * @return
	 */
	private static boolean hasExternalStoragePermission(Context context) {
		int perm = context
				.checkCallingOrSelfPermission("android.permission.WRITE_EXTERNAL_STORAGE");
		return perm == 0;
	}

	// Configure recorder related function.
	@SuppressLint("NewApi")
	void ConfigRecorderFuntion() {
		if (libPlayer != null) {
			if (recDir != null && !recDir.isEmpty()) {
				int ret = libPlayer.SmartPlayerCreateFileDirectory(recDir);
				if (0 == ret) {
					if (0 != libPlayer.SmartPlayerSetRecorderDirectory(
							playerHandle, recDir)) {
						Log.e(TAG, "Set recoder dir failed , path:" + recDir);
						return;
					}

					if (0 != libPlayer.SmartPlayerSetRecorderFileMaxSize(
							playerHandle, 200)) {
						Log.e(TAG,
								"SmartPublisherSetRecoderFileMaxSize failed.");
						return;
					}

				} else {
					Log.e(TAG, "Create recoder dir failed, path:" + recDir);
				}
			}
		}
	}

	class ButtonRecorderMangerListener implements OnClickListener {
		public void onClick(View v) {
			if (isPlaying || isRecording) {
				return;
			}

			Intent intent = new Intent();
			intent.setClass(SmartPlayer.this, RecorderManager.class);
			intent.putExtra("RecoderDir", recDir);
			startActivity(intent);
		}
	}

	private void InitAndSetConfig() {
		playerHandle = libPlayer.SmartPlayerOpen(myContext);

		if (playerHandle == 0) {
			Log.e(TAG, "surfaceHandle with nil..");
			return;
		}

		libPlayer.SetSmartPlayerEventCallbackV2(playerHandle,
				new EventHandeV2());

		libPlayer.SmartPlayerSetBuffer(playerHandle, playBuffer);

		// set report download speed
		// libPlayer.SmartPlayerSetReportDownloadSpeed(playerHandle, 1, 5);

		libPlayer.SmartPlayerSetFastStartup(playerHandle, isFastStartup ? 1 : 0);

		libPlayer.SmartPlayerSaveImageFlag(playerHandle, 1);

		// It only used when playback RTSP stream..
		// libPlayer.SmartPlayerSetRTSPTcpMode(playerHandle, 1);

		//playbackUrl = "rtmp://live.hkstv.hk.lxdns.com/live/hks";

		//playbackUrl = "rtmp://player.daniulive.com:1935/hls/stream1";

		// playbackUrl =
		// "rtsp://218.204.223.237:554/live/1/67A7572844E51A64/f68g2mj7wjua3la7";

		// playbackUrl =
		// "rtsp://rtsp-v3-spbtv.msk.spbtv.com/spbtv_v3_1/214_110.sdp";

		if (playbackUrl == null) {
			Log.e(TAG, "playback URL with NULL...");
			return;
		}
		libPlayer.SmartPlayerSetUrl(playerHandle, playbackUrl);
	}

}