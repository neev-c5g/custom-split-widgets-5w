package dev.ev6.taximeter.prototype;
import android.app.Activity;
import android.provider.Settings;
import android.view.View;
import android.widget.FrameLayout;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowLooper;
import static org.junit.Assert.*;
@RunWith(RobolectricTestRunner.class)
@Config(sdk=19,manifest=Config.NONE)
public class SplitViewportTest {
 @Test public void resizesForOpenPopupAndRestoresFullWidth(){
    Activity a=Robolectric.buildActivity(Activity.class).create().get();FrameLayout root=new FrameLayout(a);View child=new View(a);root.addView(child,new FrameLayout.LayoutParams(-1,-1));
    Settings.System.putInt(a.getContentResolver(),"split_screen_status",1);SplitViewport v=new SplitViewport(a,child);
    assertEquals(Math.max(1,a.getResources().getDisplayMetrics().widthPixels*2/3-8),child.getLayoutParams().width);
    Settings.System.putInt(a.getContentResolver(),"split_screen_status",0);ShadowLooper.idleMainLooper();assertEquals(-1,child.getLayoutParams().width);v.close();
 }
}
