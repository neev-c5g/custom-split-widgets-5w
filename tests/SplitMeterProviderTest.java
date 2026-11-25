package dev.ev6.taximeter.prototype;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.widget.RemoteViews;
import kr.blackr.split.sdk.WidgetProtocol;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.shadows.ShadowLooper;
import org.robolectric.annotation.Config;
import org.robolectric.android.controller.ServiceController;
import static org.junit.Assert.*;
import java.util.concurrent.TimeUnit;

@RunWith(RobolectricTestRunner.class)
@Config(sdk=19, manifest=Config.NONE)
public class SplitMeterProviderTest {
    @Test public void sendsFramesOnlyToSubscribedInstance() throws Exception {
        ServiceController<SplitMeterProvider> controller=Robolectric.buildService(SplitMeterProvider.class).create();
        SplitMeterProvider provider=controller.get();
        Messenger request=new Messenger(provider.onBind(new Intent()));
        final int[] count={0};final String[] instance={null};
        Messenger reply=new Messenger(new Handler(){@Override public void handleMessage(Message m){count[0]++;instance[0]=m.getData().getString(WidgetProtocol.KEY_INSTANCE);}});
        Message subscribe=Message.obtain(null,WidgetProtocol.SUBSCRIBE);subscribe.replyTo=reply;
        Bundle data=new Bundle();data.putString(WidgetProtocol.KEY_INSTANCE,"token-a");data.putInt(WidgetProtocol.KEY_VERSION,WidgetProtocol.VERSION);subscribe.setData(data);
        request.send(subscribe);ShadowLooper.idleMainLooper();
        SplitMeterProvider.publish(new RemoteViews("dev.ev6.taximeter.prototype",1),false,0f);ShadowLooper.idleMainLooper();
        assertEquals(1,count[0]);assertEquals("token-a",instance[0]);
        Message unsubscribe=Message.obtain(null,WidgetProtocol.UNSUBSCRIBE);Bundle stop=new Bundle();stop.putString(WidgetProtocol.KEY_INSTANCE,"token-a");unsubscribe.setData(stop);request.send(unsubscribe);
        ShadowLooper.idleMainLooper();
        SplitMeterProvider.publish(new RemoteViews("dev.ev6.taximeter.prototype",1),false,0f);ShadowLooper.idleMainLooper();
        assertEquals(1,count[0]);controller.destroy();
    }
    @Test public void animatedFramesStopAfterUnsubscribe() throws Exception {
        ServiceController<SplitMeterProvider> controller=Robolectric.buildService(SplitMeterProvider.class).create();
        SplitMeterProvider provider=controller.get();Messenger request=new Messenger(provider.onBind(new Intent()));
        final int[] count={0};Messenger reply=new Messenger(new Handler(){@Override public void handleMessage(Message m){count[0]++;}});
        Message sub=Message.obtain(null,WidgetProtocol.SUBSCRIBE);sub.replyTo=reply;Bundle data=new Bundle();data.putString(WidgetProtocol.KEY_INSTANCE,"motion");data.putInt(WidgetProtocol.KEY_VERSION,WidgetProtocol.VERSION);sub.setData(data);request.send(sub);
        ShadowLooper.idleMainLooper();SplitMeterProvider.publish(new RemoteViews("dev.ev6.taximeter.prototype",1),true,30f);
        ShadowLooper.idleMainLooper(500,TimeUnit.MILLISECONDS);assertTrue(count[0]>=2);
        Message unsub=Message.obtain(null,WidgetProtocol.UNSUBSCRIBE);Bundle end=new Bundle();end.putString(WidgetProtocol.KEY_INSTANCE,"motion");unsub.setData(end);request.send(unsub);
        ShadowLooper.idleMainLooper();int stopped=count[0];ShadowLooper.idleMainLooper(500,TimeUnit.MILLISECONDS);assertEquals(stopped,count[0]);controller.destroy();
    }

}
