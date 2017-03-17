package com.gongzetao.loop.activity;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVFile;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVQuery;
import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.FindCallback;
import com.avos.avoscloud.SaveCallback;
import com.avos.avoscloud.im.v2.AVIMClient;
import com.avos.avoscloud.im.v2.AVIMConversation;
import com.avos.avoscloud.im.v2.AVIMException;
import com.avos.avoscloud.im.v2.AVIMMessage;
import com.avos.avoscloud.im.v2.AVIMMessageHandler;
import com.avos.avoscloud.im.v2.AVIMMessageManager;
import com.avos.avoscloud.im.v2.callback.AVIMClientCallback;
import com.avos.avoscloud.im.v2.callback.AVIMConversationCallback;
import com.avos.avoscloud.im.v2.callback.AVIMConversationCreatedCallback;
import com.avos.avoscloud.im.v2.messages.AVIMTextMessage;
import com.bumptech.glide.Glide;
import com.gongzetao.loop.R;
import com.gongzetao.loop.application.MyApplication;
import com.gongzetao.loop.base.BaseActivity;
import com.gongzetao.loop.bean.ChatMessage;
import com.gongzetao.loop.bean.ChatMessageType;
import com.gongzetao.loop.bean.User;
import com.gongzetao.loop.facedutils.SpanableStringUtils;
import com.gongzetao.loop.utils.DataUtils;
import com.gongzetao.loop.utils.DateBaseUtils;
import com.gongzetao.loop.utils.EnterActivityUtils;
import com.gongzetao.loop.utils.MediumUtils;
import com.gongzetao.loop.utils.ToastUtils;
import com.gongzetao.loop.view.CircleImageView;
import com.gongzetao.loop.view.RefreshListView;
import com.gongzetao.loop.view.photoBrowser.ui.PhotoViewActivity;
import com.lidroid.xutils.BitmapUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.finalteam.galleryfinal.GalleryFinal;
import cn.finalteam.galleryfinal.model.PhotoInfo;

/**
 * Created by baixinping on 2016/8/11.
 */
public class ChatActivity extends BaseActivity {


    ChatMessageHandler chatMessageHandler;

    //点击朋友创建对话时需要将朋友信息传入
    public static final String FRIEND_INFO = "user";

    public static final int remindFriends = 0;

    MyApplication app;
    EditText et_messageFrame;
    RefreshListView lv_showMessage;
    TextView tv_name;
    ImageView iv_add;
    TextView tv_send;
    LinearLayout activityRootView;


    Activity context = this;
    MyAdapter adapter;
    List<ChatMessage> messages;

    AVUser user;
    String str_user;

    String friendAccountNumber;
    String friendName;
    String friendPhoto;

    BitmapUtils bitmaputils;

    List<String> pictureList;
    List<PhotoInfo> photoInfos;
    int currentPage = 0;

    MyApplication.ReceiverMessageSucceedListener receiverMessageSucceedListener;

    GalleryFinal.OnHanlderResultCallback onHanlderResultCallback = new GalleryFinal.OnHanlderResultCallback() {
        @Override
        public void onHanlderSuccess(int reqeustCode, List<PhotoInfo> resultList) {
            pictureList = new ArrayList();
            photoInfos = resultList;
            for (int i = 0; i < resultList.size(); i++) {
                pictureList.add(resultList.get(i).getPhotoPath());
                //上传图片
                sendImage(resultList.get(i).getPhotoPath());
                //发送图片
                sendMessage(resultList.get(i).getPhotoPath(), ChatMessageType.MESSAGE_TYPE_TO_IMAGE, ChatMessageType.STR_TYPE_IMAGE);
            }
        }

        @Override
        public void onHanlderFailure(int requestCode, String errorMsg) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //获取application
        app = (MyApplication) getApplication();
        bitmaputils = new BitmapUtils(this);
        //注册消息监听
        chatMessageHandler = new ChatMessageHandler();
        AVIMMessageManager.registerMessageHandler(AVIMMessage.class, chatMessageHandler);

        receiverMessageSucceedListener = new MyApplication.ReceiverMessageSucceedListener() {
            @Override
            public void receiverMessageSucceed(AVUser user) {
                //获取消息id
                String fromAccountNumber = user.getEmail();
                //如果当前接收到的消息是当前朋友发的，则更新listview
                if (user.getEmail().equals(fromAccountNumber)) {
                    updateListView();
                }
            }
        };
        app.setReceiverMessageSucceedListener(receiverMessageSucceedListener);
        setContentView(R.layout.activity_chat);
        initUI();
        initData();
    }

    private void initEmotion() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                SpanableStringUtils.getInstace().getFileText(app);
            }
        }).start();
    }

    private void initData() {
//       initEmotion();

        str_user = getIntent().getStringExtra(FRIEND_INFO);
        try {
            //获取最新用户信息
            user = (AVUser) AVObject.parseAVObject(str_user);
            AVQuery<AVUser> query = new AVQuery("_User");
            query.whereEqualTo(User.str_mail, user.getEmail());
            query.findInBackground(new FindCallback<AVUser>() {
                @Override
                public void done(List<AVUser> list, AVException e) {
                    if (list != null) {
                        user = list.get(0);
                        initBaseInfo();
                    }

                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

        //点击好友进入聊天时，若内存中没有加载好友聊天记录，先获取好友的聊天信息
        if (MyApplication.chatMessages.get(user.getEmail()) == null) {
            //从数据库中获取好友的聊天信息。
            List<ChatMessage> messages = new DateBaseUtils(app).getOneChatMessage(user.toString(), 10, 0);
            MyApplication.chatMessages.put(user.toString(), messages);
        }



        //将好友的未读消息清除
        app.removeUnReadChatMessage(user);
    }

    private void initBaseInfo() {
        initChatMessage();
        //获取朋友的基本信息
        friendAccountNumber = (String) user.get(User.str_mail);
        friendName = (String) user.get(User.str_accountName);
        friendPhoto = (String) user.get(User.str_photo);

        tv_name.setText(friendName);
        //得到最新聊天记录
        messages = app.getChatMessages(user);
        if (messages == null)
            messages = new ArrayList();
        adapter = new MyAdapter();
        lv_showMessage.setAdapter(adapter);
        lv_showMessage.smoothScrollToPosition(messages.size());
        lv_showMessage.setSelection(messages.size());
    }


    private void initChatMessage() {
        //点击好友进入聊天时，若内存中没有加载好友聊天记录，先获取好友的聊天信息
        if (MyApplication.chatMessages.get(user.getEmail()) == null) {
            //从数据库中获取好友的聊天信息。
            List<ChatMessage> messages = new DateBaseUtils(app).getOneChatMessage(user.toString(), 10, 0);
            MyApplication.chatMessages.put(user.getEmail(), messages);
        }
    }

    private void initUI() {
        activityRootView = (LinearLayout) findViewById(R.id.chat_ll_root);
        tv_name = (TextView) findViewById(R.id.chat_tv_name);
        et_messageFrame = (EditText) findViewById(R.id.et_message_frame);
        lv_showMessage = (RefreshListView) findViewById(R.id.chat_lv_show_message);
        iv_add = (ImageView) findViewById(R.id.chat_iv_icon_add);
        tv_send = (TextView) findViewById(R.id.chat_tv_send);
        iv_add.setVisibility(View.VISIBLE);
        tv_send.setVisibility(View.INVISIBLE);
        et_messageFrame.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String content = et_messageFrame.getText().toString().trim();
                if ("".equals(content) || content == null) {
                    //没有内容
                    iv_add.setVisibility(View.VISIBLE);
                    tv_send.setVisibility(View.INVISIBLE);
                } else {
                    iv_add.setVisibility(View.INVISIBLE);
                    tv_send.setVisibility(View.VISIBLE);
                }
                if ("@".equals(s.subSequence(start, start + count).toString())) {
                    Intent intent = new Intent(ChatActivity.this, RemindFriendsActivity.class);
                    startActivityForResult(intent, remindFriends);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        //监听软键盘的显示和隐藏,必须在获取数据之后进行监听，否则会出现数据空指针异常
        activityRootView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                Rect rect = new Rect();
                activityRootView.getWindowVisibleDisplayFrame(rect);
                int rootInvisibleHeight = activityRootView.getRootView().getHeight() - rect.bottom;
                if (rootInvisibleHeight <= 100) {
//                    if (messages != null)
//                        lv_showMessage.setSelection(messages.size());
                } else {
                    if (messages != null)
                        lv_showMessage.setSelection(messages.size());
                }
            }
        });
        lv_showMessage.setPermitLoading(false);
        lv_showMessage.setOnRefreshListener(new RefreshListView.OnRefreshListener() {
            @TargetApi(Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void pullRefresh() {
                currentPage += 1;
                //获取当页数据
                List<ChatMessage> currentMessages = new DateBaseUtils(app).getOneChatMessage(user.toString(), 10 * (currentPage + 1), currentPage * 10);
                messages.addAll(0, currentMessages);
                MyApplication.chatMessages.put(user.getEmail(), messages);
                adapter.notifyDataSetChanged();
                lv_showMessage.completeRefresh();
                if (currentMessages.size() > 0)
                    lv_showMessage.setSelectionFromTop(10, 0);
            }

            @Override
            public void loading() {

            }
        });
    }

    public void onclick(View view) {
        switch (view.getId()) {
            case R.id.chat_iv_icon_last:
                setResult(RESULT_OK);
                finish();
                break;
            case R.id.chat_iv_icon_persion:
                EnterActivityUtils.enterFriendMainPager(this, user);
                break;
            case R.id.chat_iv_icon_add:
                MediumUtils.openPhoto(this, onHanlderResultCallback, photoInfos, 9);
                break;
            case R.id.iv_icon_emoticon:
                break;
            case R.id.chat_tv_send:
                String message = et_messageFrame.getText().toString();
                et_messageFrame.setText("");
                if ("".equals(message) || message == null) {
                    return;
                }
                sendMessage(message, ChatMessageType.MESSAGE_TYPE_TO_TEXT, ChatMessageType.STR_TYPE_TEXT);
                break;
        }
    }


    /**
     * 发送消息
     *
     * @param text
     */
    public void sendMessage(final String text, final int type, final String strType) {
        // Tom 用自己的信息作为clientId，获取AVIMClient对象实例
        final String myInfo = MyApplication.me.toString();
        AVIMClient tom = AVIMClient.getInstance(MyApplication.me.getEmail());
        // 与服务器连接
        tom.open(new AVIMClientCallback() {
            @Override
            public void done(AVIMClient client, AVIMException e) {
                if (e == null) {
                    // 创建与朋友之间的对话
                    client.createConversation(Arrays.asList(friendAccountNumber), "me & you", null,
                            new AVIMConversationCreatedCallback() {
                                @Override
                                public void done(AVIMConversation conversation, AVIMException e) {
                                    if (e == null) {
                                        AVIMTextMessage msg = new AVIMTextMessage();
                                        msg.setText(text);
                                        Map<String, Object> map = new HashMap<String, Object>();
                                        map.put(ChatMessageType.STR_TYPE, strType);
                                        map.put(ChatMessageType.STR_USER_INFO, myInfo);
                                        msg.setAttrs(map);
                                        // 发送消息
                                        conversation.sendMessage(msg, new AVIMConversationCallback() {
                                            @Override
                                            public void done(AVIMException e) {
                                                if (e == null) {
                                                    //发送成功
                                                    //创建message
                                                    ChatMessage message = new ChatMessage(type,
                                                            System.currentTimeMillis(), text);
                                                    //message保存到当前消息对应的集合中
                                                    app.addChatMessage(user, message);
                                                    //更新listview
                                                    updateListView();
                                                    //将信息保存到数据库中
                                                    DateBaseUtils.saveChatMessageInDataBase(user, message);
                                                    //保存最近聊天人信息到缓存中
                                                    app.saveRecentlyContact(user);
                                                }
                                            }
                                        });
                                    }
                                }
                            });
                }
            }
        });
    }

    /**
     * 发送图片
     *
     * @param photoPath
     */
    private void sendImage(String photoPath) {
        AVFile file = null;
        try {
            File localFile = new File(photoPath);
            file = AVFile.withFile(localFile.getName(), localFile);
        } catch (FileNotFoundException e) {
            ToastUtils.showToast(this, "图片资源错误，请重新选择");
            return;
        }
        if (file != null) {
            final AVFile finalFile1 = file;
            file.saveInBackground(new SaveCallback() {
                @Override
                public void done(AVException e) {
                    //说明照片发送成功
//                    sendMessage(finalFile1.getUrl(), ChatMessageType.MESSAGE_TYPE_TO_IMAGE, ChatMessageType.STR_TYPE_IMAGE);
                }
            });
        }
    }

    private void updateListView() {
        //更新listView是根据当前页面朋友的账号进行更新
        messages = ((MyApplication) context.getApplication()).getChatMessages(user);
        adapter.notifyDataSetChanged();
        lv_showMessage.smoothScrollToPosition(messages.size());
    }


    /**
     * 接收文字消息：
     * 用户在此界面时会受到来自所有人的消息，先进性判断，再分类保存。
     */
    public class ChatMessageHandler extends AVIMMessageHandler {
        //接收到消息后的处理逻辑
        @Override
        public void onMessage(AVIMMessage message, AVIMConversation conversation, AVIMClient client) {
            if (message instanceof AVIMTextMessage) {

                //获取消息id
                String fromAccountNumber = message.getFrom();
                //如果当前接收到的消息是当前朋友发的，则更新listview
                if (user.getEmail().equals(fromAccountNumber)) {
                    updateListView();
                }

            }
        }

        public void onMessageReceipt(AVIMMessage message, AVIMConversation conversation, AVIMClient client) {
        }
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK
                && event.getRepeatCount() == 0) {
            setResult(RESULT_OK);
            finish();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        AVIMMessageManager.unregisterMessageHandler(AVIMMessage.class, chatMessageHandler);
    }

    class MyAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            if (messages == null)
                messages = new ArrayList<ChatMessage>();
            return messages.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public int getItemViewType(int position) {
            if (messages.get(position).getType() == ChatMessageType.MESSAGE_TYPE_TO_TEXT) {
                return 0;
            } else if (messages.get(position).getType() == ChatMessageType.MESSAGE_TYPE_FROM_TEXT) {
                return 1;
            } else if (messages.get(position).getType() == ChatMessageType.MESSAGE_TYPE_TO_IMAGE) {
                return 2;
            } else if (messages.get(position).getType() == ChatMessageType.MESSAGE_TYPE_FROM_IMAGE) {
                return 3;
            } else {
                return -1;
            }
        }

        @Override
        public int getViewTypeCount() {
            return ChatMessageType.MESSAGE_TYPE;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            int viewType = getItemViewType(position);
            TextViewHolder textHolder;

            ImageViewHolder imageHolder;

            if (viewType == 0) {
                if (convertView == null) {
                    textHolder = new TextViewHolder();
                    convertView = View.inflate(context, R.layout.chat_message_text_to, null);
                    textHolder.tv_time = (TextView) convertView.findViewById(R.id.chat_message_text_to_time);
                    textHolder.iv_photo = (CircleImageView) convertView.findViewById(R.id.chat_message_text_to_photo);
                    textHolder.tv_content = (TextView) convertView.findViewById(R.id.chat_message_text_to_content);
                    convertView.setTag(textHolder);
                } else {
                    textHolder = (TextViewHolder) convertView.getTag();
                }
                setTextTime(position, textHolder.tv_time);
                SpannableString spannableString = SpanableStringUtils.getInstace().getExpressionString(context, messages.get(position).getContent());
                textHolder.tv_content.setText(spannableString);
                String photo = (String) MyApplication.me.get(User.str_photo);
                if (photo != null && !"".equals(photo))
                    bitmaputils.display(textHolder.iv_photo, photo);
                else
                    textHolder.iv_photo.setImageResource(R.drawable.persion_default_photo);
                textHolder.iv_photo.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(ChatActivity.this, FriendMainPagerActivity.class);
                        intent.putExtra(FriendMainPagerActivity.ser_user_info, MyApplication.me.toString());
                        ChatActivity.this.startActivity(intent);
                    }
                });
            } else if (viewType == 1) {
                if (convertView == null) {
                    textHolder = new TextViewHolder();
                    convertView = View.inflate(context, R.layout.chat_message_text_from, null);
                    textHolder.tv_time = (TextView) convertView.findViewById(R.id.chat_message_text_from_time);
                    textHolder.iv_photo = (CircleImageView) convertView.findViewById(R.id.chat_message_text_from_photo);
                    textHolder.tv_content = (TextView) convertView.findViewById(R.id.chat_message_text_from_content);
                    convertView.setTag(textHolder);
                } else {
                    textHolder = (TextViewHolder) convertView.getTag();
                }
                setTextTime(position, textHolder.tv_time);
                SpannableString spannableString = SpanableStringUtils.getInstace().getExpressionString(context, messages.get(position).getContent());
                textHolder.tv_content.setText(spannableString);
                if (friendPhoto != null && !"".equals(friendPhoto))
                    bitmaputils.display(textHolder.iv_photo, friendPhoto);
                else
                    textHolder.iv_photo.setImageResource(R.drawable.persion_default_photo);
                textHolder.iv_photo.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(ChatActivity.this, FriendMainPagerActivity.class);
                        intent.putExtra(FriendMainPagerActivity.ser_user_info, user.toString());
                        ChatActivity.this.startActivity(intent);
                    }
                });
            } else if (viewType == 2) {
                if (convertView == null) {
                    imageHolder = new ImageViewHolder();
                    convertView = View.inflate(context, R.layout.chat_message_image_to, null);
                    imageHolder.tv_time = (TextView) convertView.findViewById(R.id.chat_message_image_to_time);
                    imageHolder.iv_photo = (CircleImageView) convertView.findViewById(R.id.chat_message_image_to_photo);
                    imageHolder.iv_image = (ImageView) convertView.findViewById(R.id.chat_message_image_to_content);
                    convertView.setTag(imageHolder);
                } else {
                    imageHolder = (ImageViewHolder) convertView.getTag();
                }
                setTextTime(position, imageHolder.tv_time);
                String photo = (String) MyApplication.me.get(User.str_photo);
                imageHolder.iv_image.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ArrayList<String> photo = new ArrayList<String>();
                        photo.add(messages.get(position).getContent());
                        Intent intent = new Intent(ChatActivity.this, PhotoViewActivity.class);
                        intent.putExtra(PhotoViewActivity.position, 0);
                        intent.putStringArrayListExtra(PhotoViewActivity.URL_LIST, photo);
                        ChatActivity.this.startActivity(intent);
                    }
                });
                if (photo != null && !"".equals(photo))
                    bitmaputils.display(imageHolder.iv_photo, photo);
                else
                    imageHolder.iv_photo.setImageResource(R.drawable.persion_default_photo);
                Glide.with(ChatActivity.this).load(messages.get(position).getContent()).into(imageHolder.iv_image);
                imageHolder.iv_photo.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(ChatActivity.this, FriendMainPagerActivity.class);
                        intent.putExtra(FriendMainPagerActivity.ser_user_info, MyApplication.me.toString());
                        ChatActivity.this.startActivity(intent);
                    }
                });
            } else if (viewType == 3) {
                if (convertView == null) {
                    imageHolder = new ImageViewHolder();
                    convertView = View.inflate(context, R.layout.chat_message_image_from, null);
                    imageHolder.tv_time = (TextView) convertView.findViewById(R.id.chat_message_image_from_time);
                    imageHolder.iv_photo = (CircleImageView) convertView.findViewById(R.id.chat_message_image_from_photo);
                    imageHolder.iv_image = (ImageView) convertView.findViewById(R.id.chat_message_image_from_content);
                    convertView.setTag(imageHolder);

                } else {
                    imageHolder = (ImageViewHolder) convertView.getTag();
                }
                setTextTime(position, imageHolder.tv_time);
                imageHolder.iv_image.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ArrayList<String> photo = new ArrayList<String>();
                        photo.add(messages.get(position).getContent());
                        Intent intent = new Intent(ChatActivity.this, PhotoViewActivity.class);
                        intent.putExtra(PhotoViewActivity.position, 0);
                        intent.putStringArrayListExtra(PhotoViewActivity.URL_LIST, photo);
                        ChatActivity.this.startActivity(intent);
                    }
                });
                if (friendPhoto != null && !"".equals(friendPhoto))
                    bitmaputils.display(imageHolder.iv_photo, friendPhoto);
                else
                    imageHolder.iv_photo.setImageResource(R.drawable.persion_default_photo);
                Glide.with(ChatActivity.this).load(messages.get(position).getContent()).into(imageHolder.iv_image);
                imageHolder.iv_photo.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(ChatActivity.this, FriendMainPagerActivity.class);
                        intent.putExtra(FriendMainPagerActivity.ser_user_info, user.toString());
                        ChatActivity.this.startActivity(intent);
                    }
                });

            }
//            convertView.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    ArrayList<String> photo = new ArrayList<String>();
//                    photo.add(messages.get(position).getContent());
//                    Intent intent = new Intent(ChatActivity.this, PhotoViewActivity.class);
//                    intent.putExtra(PhotoViewActivity.position, 0);
//                    intent.putStringArrayListExtra(PhotoViewActivity.URL_LIST, photo);
//                    ChatActivity.this.startActivity(intent);
//                }
//            });
            return convertView;
        }
    }
    private void setTextTime(int position, TextView tv_time) {

        Date date = new Date(messages.get(position).getTime());
        boolean b;
        try{
            b = date.getTime()-messages.get(position-1).getTime()<80000;
        }catch(ArrayIndexOutOfBoundsException e){
            b = false;
        }
        if(b)
            tv_time.setVisibility(View.GONE);
        else {
            if (DataUtils.isRecent(date) == DataUtils.TODAY)
                tv_time.setText(DataUtils.getTimeText(date));
            else if (DataUtils.isRecent(date) == DataUtils.YESTERDAY)
                tv_time.setText("昨天" + DataUtils.getTimeText(date));
            else if (DataUtils.isRecent(date) == DataUtils.TDBY)
                tv_time.setText("前天" + DataUtils.getTimeText(date));
            else if (DataUtils.isWeeks(date))
                tv_time.setText(DataUtils.getWeekTimeText(date));
            else if (DataUtils.isRecent(date) == DataUtils.THIS_YEAR)
                tv_time.setText(DataUtils.getDataTimeTextNoYS(date));
            else
                tv_time.setText(DataUtils.getDataTimeTextNoS(date));
        }
    }

    static class TextViewHolder {
        public TextView tv_time;
        public CircleImageView iv_photo;
        public TextView tv_content;
    }

    static class ImageViewHolder {
        public TextView tv_time;
        public CircleImageView iv_photo;
        public ImageView iv_image;
    }

    int posEnd = 0;
    int posStart = 0;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case remindFriends:
                if (resultCode == RESULT_OK) {
                    String userStr = data.getStringExtra(User.user);
                    AVObject user = null;
                    try {
                        user = AVObject.parseAVObject(userStr);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    String userName = (String) user.get(User.str_accountName);
                    et_messageFrame.getText().delete(et_messageFrame.getSelectionStart() - 1, et_messageFrame.getSelectionStart());
                    posStart = et_messageFrame.getSelectionStart();
                    et_messageFrame.append(SpanableStringUtils.getInstace().getExpressionString(context, "@" + userName));
                    posEnd = et_messageFrame.getSelectionStart();
                    et_messageFrame.removeTextChangedListener(new TextWatcher() {
                        @Override
                        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                        }

                        @Override
                        public void onTextChanged(CharSequence s, int start, int before, int count) {
                            if (et_messageFrame.getSelectionStart() == posEnd)
                                et_messageFrame.setSelection(posStart, posEnd);
                        }

                        @Override
                        public void afterTextChanged(Editable s) {

                        }
                    });
                }
        }
    }


}
