package kr.blackr.split.probe;

import android.app.Instrumentation;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.RemoteViews;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/** Headless integration probe; does not start a trip or change panel settings. */
public final class MeterProbe extends Instrumentation {
    private final CountDownLatch done = new CountDownLatch(1);
    private volatile RemoteViews frame;
    private volatile String error;
    private HandlerThread replies;
    private ServiceConnection connection;
    private Messenger provider;
    @Override public void onCreate(Bundle args) { super.onCreate(args); start(); }
    @Override public void onStart() {
        final Context host=getTargetContext();boolean bound=false;
        try {
            replies=new HandlerThread("meter-probe");replies.start();
            final Messenger receiver=new Messenger(new Handler(replies.getLooper()) {
                @Override public void handleMessage(Message m) {
                    Bundle b=m.getData();
                    if(m.what==3 && b!=null && "probe-instance".equals(b.getString("instance")) && b.getInt("version",-1)==1) {
                        b.setClassLoader(RemoteViews.class.getClassLoader());frame=b.getParcelable("remote_views");done.countDown();
                    }
                }
            });
            connection=new ServiceConnection() {
                public void onServiceConnected(ComponentName name,IBinder binder) {
                    provider=new Messenger(binder);Message m=Message.obtain(null,1);m.replyTo=receiver;Bundle b=new Bundle();b.putString("instance","probe-instance");b.putInt("version",1);m.setData(b);
                    try { provider.send(m); } catch(RemoteException e) { error="send failed";done.countDown(); }
                }
                public void onServiceDisconnected(ComponentName name) { error="disconnected";done.countDown(); }
            };
            Intent intent=new Intent("kr.blackr.customsplit.action.WIDGET_PROVIDER");intent.setComponent(new ComponentName("dev.ev6.taximeter.prototype","dev.ev6.taximeter.prototype.SplitMeterProvider"));
            bound=host.bindService(intent,connection,Context.BIND_AUTO_CREATE);
            if (!bound) throw new IllegalStateException("bind returned false");
            if (!done.await(8,TimeUnit.SECONDS)) throw new IllegalStateException("frame timeout");
            if (error!=null) throw new IllegalStateException(error);
            if (frame==null || !"dev.ev6.taximeter.prototype".equals(frame.getPackage())) throw new IllegalStateException("invalid frame package");
            final Throwable[] failure={null};
            runOnMainSync(new Runnable(){public void run(){try{View view=frame.apply(host,new FrameLayout(host));if(view==null)throw new IllegalStateException("empty view");}catch(Throwable t){failure[0]=t;}}});
            if(failure[0]!=null)throw new IllegalStateException("render failed: "+failure[0].toString()+" cause="+failure[0].getCause(),failure[0]);
            Bundle result=new Bundle();result.putString("result","meter provider bind, frame and render passed");finish(0,result);
        } catch(Exception e) { Bundle result=new Bundle();result.putString("error",e.toString());finish(1,result); }
        finally {
            if(provider!=null){Message m=Message.obtain(null,2);Bundle b=new Bundle();b.putString("instance","probe-instance");m.setData(b);try{provider.send(m);}catch(RemoteException ignored){}}
            if(bound)host.unbindService(connection);
            if(replies!=null)replies.quit();
        }
    }
}
