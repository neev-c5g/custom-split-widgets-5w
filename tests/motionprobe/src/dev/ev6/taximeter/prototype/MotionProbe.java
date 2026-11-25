package dev.ev6.taximeter.prototype;
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

/** Exercises moving frames in isolation without changing the real speed or trip state. */
public final class MotionProbe extends Instrumentation {
    private final CountDownLatch connected=new CountDownLatch(1),frames=new CountDownLatch(3);
    private Messenger provider;private volatile RemoteViews latest;private HandlerThread thread;
    private ServiceConnection connection;
    @Override public void onCreate(Bundle args){super.onCreate(args);start();}
    @Override public void onStart(){
        final Context c=getTargetContext();boolean bound=false;String outcome="";
        try{
            thread=new HandlerThread("motion-probe");thread.start();
            final Messenger client=new Messenger(new Handler(thread.getLooper()){@Override public void handleMessage(Message m){
                Bundle b=m.getData();if(m.what==3&&b!=null&&"motion-probe".equals(b.getString("instance"))){b.setClassLoader(RemoteViews.class.getClassLoader());latest=b.getParcelable("remote_views");frames.countDown();}
            }});
            connection=new ServiceConnection(){public void onServiceConnected(ComponentName n,IBinder b){provider=new Messenger(b);Message m=Message.obtain(null,1);m.replyTo=client;Bundle d=new Bundle();d.putString("instance","motion-probe");d.putInt("version",1);m.setData(d);try{provider.send(m);}catch(RemoteException ignored){}connected.countDown();}public void onServiceDisconnected(ComponentName n){}};
            bound=c.bindService(new Intent(c,SplitMeterProvider.class),connection,Context.BIND_AUTO_CREATE);if(!bound||!connected.await(5,TimeUnit.SECONDS))throw new IllegalStateException("bind failed");
            Thread.sleep(1000);SplitMeterProvider.publish(MeterWidget.makeViews(c,null,false,false),true,30f);
            if(!frames.await(5,TimeUnit.SECONDS)||latest==null)throw new IllegalStateException("animated frame timeout");
            final Throwable[] error={null};runOnMainSync(new Runnable(){public void run(){try{FrameLayout host=new FrameLayout(c);View v=latest.apply(c,host);host.addView(v);latest.reapply(c,v);}catch(Throwable t){error[0]=t;}}});
            if(error[0]!=null)throw new IllegalStateException("frame rendering: "+error[0]);
            java.io.FileOutputStream sample=c.openFileOutput("horse-probe.png",0);HorsePainter.frame(0).compress(android.graphics.Bitmap.CompressFormat.PNG,100,sample);sample.close();
            outcome="moving frames and reapply passed";
        }catch(Exception e){outcome="ERROR: "+e.toString();}
        finally{if(provider!=null){Message m=Message.obtain(null,2);Bundle b=new Bundle();b.putString("instance","motion-probe");m.setData(b);try{provider.send(m);}catch(RemoteException ignored){}}if(bound)c.unbindService(connection);if(thread!=null)thread.quit();}
        Bundle result=new Bundle();result.putString("result",outcome);finish(outcome.startsWith("ERROR")?1:0,result);
    }
}
