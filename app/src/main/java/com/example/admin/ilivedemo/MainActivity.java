package com.example.admin.ilivedemo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.tencent.av.sdk.AVAudioCtrl;
import com.tencent.av.sdk.AVRoomMulti;
import com.tencent.ilivesdk.ILiveCallBack;
import com.tencent.ilivesdk.ILiveConstants;
import com.tencent.ilivesdk.ILiveSDK;
import com.tencent.ilivesdk.core.ILiveLoginManager;
import com.tencent.ilivesdk.core.ILiveRoomManager;
import com.tencent.ilivesdk.core.ILiveRoomOption;
import com.tencent.ilivesdk.view.AVRootView;
import com.tencent.livesdk.ILVLiveManager;
import com.tencent.livesdk.ILVLiveRoomOption;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, ILiveRoomOption.onRoomDisconnectListener {

    private static final String TAG = "-----" + MainActivity.class.getSimpleName() + "-----";
    private boolean isShowLogin = true;
    private RelativeLayout login;
    private RelativeLayout vadio;
    private int roomId = -1;
    private AVRootView av_view;
    private boolean isLogou = true;
    private EditText input_join_roomid;
    private EditText input_link_room;
    private boolean switchchoice = false;
    private LinearLayout choice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.initView();
    }

    private void initView() {
        this.login = (RelativeLayout) this.findViewById(R.id.login);
        this.vadio = (RelativeLayout) this.findViewById(R.id.vadio);
        this.findViewById(R.id.btn_login).setOnClickListener(this);
        this.findViewById(R.id.create_room).setOnClickListener(this);
        this.findViewById(R.id.join_room).setOnClickListener(this);
        this.findViewById(R.id.quit_room).setOnClickListener(this);
        this.findViewById(R.id.accept).setOnClickListener(this);
        this.findViewById(R.id.refuseLink).setOnClickListener(this);
        this.findViewById(R.id.across).setOnClickListener(this);
        this.findViewById(R.id.switchchoice).setOnClickListener(this);
        input_join_roomid = (EditText) this.findViewById(R.id.input_join_roomid);
        input_link_room = (EditText) this.findViewById(R.id.input_link_room);
        choice = (LinearLayout) this.findViewById(R.id.choice);
        this.av_view = (AVRootView) this.findViewById(R.id.av_view);
        this.showLogin();
    }

    private void showLogin() {
        if (isShowLogin) {
            login.setVisibility(View.VISIBLE);
            vadio.setVisibility(View.GONE);
        }
    }

    private void showVadio() {
        if (!isShowLogin) {
            vadio.setVisibility(View.VISIBLE);
            login.setVisibility(View.GONE);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            //登录
            case R.id.btn_login:
                this.login();
                break;
            //创建房间
            case R.id.create_room:
                this.createRoom();
                break;
            //加入房间
            case R.id.join_room:
                this.joinRoom();
                break;
            //退出房间
            case R.id.quit_room:
                this.quitRoom();
                break;
            //跨房连麦请求
            case R.id.across:
                this.linkRoomRequest();
                break;
            //允许跨房连麦
            case R.id.accept:
                this.acceptLinkRoom();
                break;
            //拒绝跨房连麦
            case R.id.refuseLink:
                this.refuseLinkRoom();
                break;
            case R.id.switchchoice:
                this.switchChoice();
            default:
                break;
        }
    }

    private void switchChoice() {
        this.switchchoice = !this.switchchoice;
        if(this.switchchoice){
            this.choice.setVisibility(View.VISIBLE);
        }else{
            this.choice.setVisibility(View.GONE);
        }
    }

    private void login() {
        if (this.isLogou) {
            ILiveLoginManager.getInstance().iLiveLogin(Constants.LIVE_ID, Constants.SIGN, new ILiveCallBack() {
                @Override
                public void onSuccess(Object data) {
                    Log.d(TAG, "iLiveLogin onSuccess : " + data.toString());
                    isShowLogin = false;
                    showVadio();
                }

                @Override
                public void onError(String module, int errCode, String errMsg) {
                    Log.d(TAG, "iLiveLogin onError ：" + errMsg + "\terrcode:" + errCode);
                    isShowLogin = true;
                    showLogin();
                }
            });
        } else {
            Log.d(TAG, "没有退出");
        }
    }

    private void logout() {
        ILiveLoginManager.getInstance().iLiveLogout(new ILiveCallBack() {
            @Override
            public void onSuccess(Object data) {
                Log.i(TAG, "IMLogout succ !");
                isLogou = true;
            }

            @Override
            public void onError(String module, int errCode, String errMsg) {
                Log.e(TAG, "IMLogout fail ：" + module + "|" + errCode + " msg " + errMsg);
                isLogou = false;
            }
        });
    }

    /**
     * 創建房間
     */
    private void createRoom() {
        this.createRoomId();
        Log.d(TAG, "roomid : " + this.roomId);
        ILVLiveRoomOption option = new ILVLiveRoomOption(Constants.LIVE_ID)
                .roomDisconnectListener(this)
                .videoMode(ILiveConstants.VIDEOMODE_BSUPPORT)
                .controlRole(Constants.HD_ROLE)
                .autoFocus(true)
                .authBits(AVRoomMulti.AUTH_BITS_DEFAULT)
                .videoRecvMode(AVRoomMulti.VIDEO_RECV_MODE_SEMI_AUTO_RECV_CAMERA_VIDEO);

        ILVLiveManager.getInstance().createRoom(this.roomId, option, new ILiveCallBack() {
            @Override
            public void onSuccess(Object data) {
                Log.d(TAG, "createRoom onSuccess : " + data.toString());
                //注册一个音频回调为变声用
                ILiveSDK.getInstance().getAvAudioCtrl().registAudioDataCallbackWithByteBuffer(AVAudioCtrl.AudioDataSourceType.AUDIO_DATA_SOURCE_VOICEDISPOSE, new AVAudioCtrl.RegistAudioDataCompleteCallbackWithByteBuffer() {
                    @Override
                    public int onComplete(AVAudioCtrl.AudioFrameWithByteBuffer audioFrameWithByteBuffer, int i) {
                        return 0;
                    }
                });
                ILVLiveManager.getInstance().setAvVideoView(av_view);
            }

            @Override
            public void onError(String module, int errCode, String errMsg) {
                Log.d(TAG, "createRoom onError ：" + errMsg + "\terrcode:" + errCode);
            }
        });
    }

    private void createRoomId() {
        this.roomId = (int) ((Math.random() * 9 + 1) * 100000);
    }

    /**
     * 退出房间
     */
    private void quitRoom() {
        ILVLiveManager.getInstance().quitRoom(new ILiveCallBack() {
            @Override
            public void onSuccess(Object data) {
                Log.d(TAG, "quitRoom onSuccess : " + data.toString());
            }

            @Override
            public void onError(String module, int errCode, String errMsg) {
                Log.d(TAG, "quitRoom onError ：" + errMsg + "\terrcode:" + errCode);
            }
        });
    }

    /**
     * 加入房间
     */
    private void joinRoom() {
        int roomUserId = -1;
        int roomId = Integer.valueOf(this.input_join_roomid.getText().toString());
        if (roomId == -1) {
            return;
        }
        roomUserId = roomId;
//        ILVLiveRoomOption memberOption = new ILVLiveRoomOption("abc")
//                .autoCamera(false)
//                .controlRole(Constants.HD_ROLE)
//                .authBits(AVRoomMulti.AUTH_BITS_JOIN_ROOM | AVRoomMulti.AUTH_BITS_RECV_AUDIO |
//                        AVRoomMulti.AUTH_BITS_RECV_CAMERA_VIDEO | AVRoomMulti.AUTH_BITS_RECV_SCREEN_VIDEO)
//                .videoRecvMode(AVRoomMulti.VIDEO_RECV_MODE_SEMI_AUTO_RECV_CAMERA_VIDEO)
//                .autoMic(false);

        ILVLiveRoomOption memberOption = new ILVLiveRoomOption("abc")
                .autoCamera(false)
                .roomDisconnectListener(this)
                .videoMode(ILiveConstants.VIDEOMODE_BSUPPORT)
                .controlRole(Constants.LD_ROLE)
                .authBits(AVRoomMulti.AUTH_BITS_JOIN_ROOM | AVRoomMulti.AUTH_BITS_RECV_AUDIO | AVRoomMulti.AUTH_BITS_RECV_CAMERA_VIDEO | AVRoomMulti.AUTH_BITS_RECV_SCREEN_VIDEO)
                .videoRecvMode(AVRoomMulti.VIDEO_RECV_MODE_SEMI_AUTO_RECV_CAMERA_VIDEO)
                .autoMic(false);
        int ret = ILVLiveManager.getInstance().joinRoom(roomUserId, memberOption, new ILiveCallBack() {
            @Override
            public void onSuccess(Object data) {
                Log.d(TAG,"joinRoom onSuccess" + data.toString());
            }

            @Override
            public void onError(String module, int errCode, String errMsg) {
                Log.d(TAG, "linkRoomRequest onError ：" + errMsg + "\terrcode:" + errCode);
            }
        });
        Log.d(TAG,"ret" + ret);
        if(ILiveConstants.ERR_ALREADY_IN_ROOM == ret){
            ILiveRoomManager.getInstance().quitRoom(new ILiveCallBack() {
                @Override
                public void onSuccess(Object data) {
                    Log.d(TAG,"quitRoom onSuccess" + data.toString());
                }

                @Override
                public void onError(String module, int errCode, String errMsg) {
                    Log.d(TAG, "quitRoom onError ：" + errMsg + "\terrcode:" + errCode);
                }
            });
        }
    }

    /**
     * 跨房连麦
     */
    private void linkRoomRequest() {
        String roomUserid = input_link_room.getText().toString();
        if (null == roomUserid && "" == roomUserid ) {
            return;
        }
        ILVLiveManager.getInstance().linkRoomRequest(roomUserid, new ILiveCallBack() {
            @Override
            public void onSuccess(Object data) {
                Log.d(TAG, "linkRoomRequest onSuccess : " + data.toString());
            }

            @Override
            public void onError(String module, int errCode, String errMsg) {
                Log.d(TAG, "linkRoomRequest onError ：" + errMsg + "\terrcode:" + errCode);
            }
        });
    }

    /**
     * 接受连麦
     */
    private void acceptLinkRoom() {
        String roomUserid = input_link_room.getText().toString();
        if (null == roomUserid && "" == roomUserid ) {
            return;
        }
        ILVLiveManager.getInstance().acceptLinkRoom(roomUserid, new ILiveCallBack() {
            @Override
            public void onSuccess(Object data) {
                Log.d(TAG, "acceptLinkRoom onSuccess : " + data.toString());
            }

            @Override
            public void onError(String module, int errCode, String errMsg) {
                Log.d(TAG, "acceptLinkRoom onError ：" + errMsg + "\terrcode:" + errCode);
            }
        });
    }

    /**
     * 拒绝连麦
     */
    private void refuseLinkRoom() {
        String roomUserid = input_link_room.getText().toString();
        if (null == roomUserid && "" == roomUserid ) {
            return;
        }
        ILVLiveManager.getInstance().refuseLinkRoom(roomUserid, new ILiveCallBack() {
            @Override
            public void onSuccess(Object data) {
                Log.d(TAG, "refuseLinkRoom onSuccess : " + data.toString());
            }

            @Override
            public void onError(String module, int errCode, String errMsg) {
                Log.d(TAG, "refuseLinkRoom onError ：" + errMsg + "\terrcode:" + errCode);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        ILiveRoomManager.getInstance().onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        ILiveRoomManager.getInstance().onPause();
    }

    @Override
    protected void onDestroy() {
        this.logout();
        super.onDestroy();
    }

    /**
     * 异常退出房间的回调
     *
     * @param errCode
     * @param errMsg
     */
    @Override
    public void onRoomDisconnect(int errCode, String errMsg) {
        Log.d(TAG, "onRoomDisconnect onError ：" + errMsg + "\terrcode:" + errCode);
    }
}
