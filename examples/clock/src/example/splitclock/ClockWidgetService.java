package example.splitclock;

import android.app.Service;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.widget.RemoteViews;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import kr.blackr.split.sdk.WidgetProtocol;

/** Minimal example: one subscriber, updates only while a host has subscribed. */
public class ClockWidgetService extends Service {
    private final Handler ui=new Handler();
    private Messenger client;private String instance;private int presses;
    private final Messenger incoming=new Messenger(new Handler(){@Override public void handleMessage(Message message){
        Bundle b=message.getData();String token=b==null?null:b.getString(WidgetProtocol.KEY_INSTANCE);
        if(message.what==WidgetProtocol.SUBSCRIBE&&message.replyTo!=null&&token!=null&&b.getInt(WidgetProtocol.KEY_VERSION,-1)==WidgetProtocol.VERSION){
            client=message.replyTo;instance=token;ui.removeCallbacks(tick);ui.post(tick);
        }else if(message.what==WidgetProtocol.UNSUBSCRIBE&&token!=null&&token.equals(instance)){stopUpdates();}
    }});
    private final Runnable tick=new Runnable(){public void run(){
        if(client==null)return;
        RemoteViews view=new RemoteViews(getPackageName(),R.layout.widget_clock);
        view.setTextViewText(R.id.clock,new SimpleDateFormat("HH:mm",Locale.getDefault()).format(new Date())+"\n누름 "+presses);
        Intent click=new Intent(ClockWidgetService.this,ClockWidgetService.this.getClass()).setAction("example.splitclock.TAP");
        view.setOnClickPendingIntent(R.id.clock,PendingIntent.getService(ClockWidgetService.this,0,click,PendingIntent.FLAG_UPDATE_CURRENT));
        Message frame=Message.obtain(null,WidgetProtocol.FRAME);Bundle data=new Bundle();
        data.putInt(WidgetProtocol.KEY_VERSION,WidgetProtocol.VERSION);data.putString(WidgetProtocol.KEY_INSTANCE,instance);
        data.putParcelable(WidgetProtocol.KEY_REMOTE_VIEWS,view);frame.setData(data);
        try{client.send(frame);ui.postDelayed(this,1000);}catch(RemoteException e){stopUpdates();}
    }};
    private void stopUpdates(){ui.removeCallbacks(tick);client=null;instance=null;}
    @Override public int onStartCommand(Intent intent,int flags,int id){if(intent!=null&&"example.splitclock.TAP".equals(intent.getAction()))presses++;stopSelf(id);return START_NOT_STICKY;}
    @Override public IBinder onBind(Intent intent){return incoming.getBinder();}
    @Override public boolean onUnbind(Intent intent){stopUpdates();return false;}
    @Override public void onDestroy(){stopUpdates();super.onDestroy();}
}
