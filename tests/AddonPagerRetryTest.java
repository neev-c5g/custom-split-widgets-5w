package kr.blackr.split.addons;
import android.content.ComponentName;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.ServiceConnection;
import android.widget.FrameLayout;
import java.util.Collections;
import java.util.concurrent.TimeUnit;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowLooper;
import static org.junit.Assert.*;
@RunWith(RobolectricTestRunner.class)
@Config(sdk=19,manifest=Config.NONE)
public class AddonPagerRetryTest {
    @Test public void retriesFailedProviderAndStopsWhenPanelCloses(){
        Context base=RuntimeEnvironment.getApplication();final int[] attempts={0};
        Context ctx=new ContextWrapper(base){@Override public boolean bindService(Intent i,ServiceConnection c,int flags){attempts[0]++;return false;}};
        AddonPager p=new AddonPager(ctx,new FrameLayout(ctx),new AddonPager.Listener(){public void onPage(int a,int b,String c){}public void onStatus(String s){}});
        p.setPages(Collections.singletonList(new AddonDiscovery.Addon(new ComponentName("test.package","test.package.Service"),"demo","Demo")));
        p.setVisible(true);assertEquals(1,attempts[0]);ShadowLooper.idleMainLooper(2100,TimeUnit.MILLISECONDS);assertEquals(2,attempts[0]);
        p.setVisible(false);ShadowLooper.idleMainLooper(60,TimeUnit.SECONDS);assertEquals(2,attempts[0]);p.close();
    }
}
