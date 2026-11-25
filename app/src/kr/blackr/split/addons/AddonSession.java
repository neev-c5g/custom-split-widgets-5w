package kr.blackr.split.addons;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.RemoteViews;
import java.util.UUID;
import kr.blackr.split.sdk.WidgetProtocol;

/** One explicitly selected, visible add-on. All methods run on the main thread. */
public final class AddonSession {
    public interface Listener { void onStatus(String message); }
    private final Context context;private final AddonDiscovery.Addon addon;private final FrameLayout host;private final Listener listener;
    private final Handler ui=new Handler(Looper.getMainLooper());private final String token=UUID.randomUUID().toString();
    private Messenger provider;private boolean bound,closed,received;private int layoutId=-1;
    private final Runnable timeout=new Runnable(){public void run(){if(!closed&&!received){listener.onStatus("위젯 응답 없음");close();}}};
    private final Messenger replies=new Messenger(new Handler(Looper.getMainLooper()){@Override public void handleMessage(Message msg){
        if(closed)return;
        try{
            Bundle data=msg.getData();if(data==null||!token.equals(data.getString(WidgetProtocol.KEY_INSTANCE))||data.getInt(WidgetProtocol.KEY_VERSION,-1)!=WidgetProtocol.VERSION)return;
            if(msg.what==WidgetProtocol.FAILURE){listener.onStatus("위젯 오류");close();return;}
            if(msg.what!=WidgetProtocol.FRAME)return;
            data.setClassLoader(RemoteViews.class.getClassLoader());
            Object payload=data.getParcelable(WidgetProtocol.KEY_REMOTE_VIEWS);
            if(!(payload instanceof RemoteViews)){listener.onStatus("잘못된 위젯 화면");close();return;}
            RemoteViews views=(RemoteViews)payload;
            if(!addon.component.getPackageName().equals(views.getPackage())){listener.onStatus("위젯 제공자 불일치");close();return;}
            if(host.getChildCount()==1 && layoutId==views.getLayoutId()) views.reapply(context,host.getChildAt(0));
            else { View result=views.apply(context,host);host.removeAllViews();host.addView(result,new FrameLayout.LayoutParams(-1,-1));layoutId=views.getLayoutId(); }
            received=true;ui.removeCallbacks(timeout);listener.onStatus("");
        }catch(RuntimeException e){listener.onStatus("위젯 표시 오류");close();}
    }});
    private final ServiceConnection connection=new ServiceConnection(){
        @Override public void onServiceConnected(ComponentName name,IBinder binder){
            if(closed||!addon.component.equals(name)){close();return;}
            provider=new Messenger(binder);Message msg=Message.obtain(null,WidgetProtocol.SUBSCRIBE);msg.replyTo=replies;
            Bundle data=new Bundle();data.putString(WidgetProtocol.KEY_INSTANCE,token);data.putInt(WidgetProtocol.KEY_VERSION,WidgetProtocol.VERSION);msg.setData(data);
            try{provider.send(msg);}catch(RemoteException e){listener.onStatus("위젯 연결 실패");close();}
        }
        @Override public void onServiceDisconnected(ComponentName name){if(!closed){listener.onStatus("위젯 연결 끊김");close();}}
    };
    public AddonSession(Context context,AddonDiscovery.Addon addon,FrameLayout host,Listener listener){
        if(context==null||addon==null||host==null||listener==null)throw new IllegalArgumentException();
        this.context=context;this.addon=addon;this.host=host;this.listener=listener;
    }
    /** Only call after the user has enabled this exact component and widget ID. */
    public boolean start(){
        if(closed||bound)throw new IllegalStateException();
        Intent intent=new Intent(WidgetProtocol.SERVICE_ACTION).setComponent(addon.component);
        try{bound=context.bindService(intent,connection,Context.BIND_AUTO_CREATE);}catch(RuntimeException e){bound=false;}
        if(bound)ui.postDelayed(timeout,3000);else listener.onStatus("위젯을 열 수 없습니다");
        return bound;
    }
    public void close(){
        if(closed)return;closed=true;ui.removeCallbacks(timeout);
        if(provider!=null){Message msg=Message.obtain(null,WidgetProtocol.UNSUBSCRIBE);Bundle data=new Bundle();data.putString(WidgetProtocol.KEY_INSTANCE,token);msg.setData(data);try{provider.send(msg);}catch(RemoteException ignored){}}
        if(bound){try{context.unbindService(connection);}catch(IllegalArgumentException ignored){}bound=false;}
        host.removeAllViews();provider=null;
    }
}
