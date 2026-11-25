package dev.ev6.taximeter.prototype;
import android.widget.Spinner;
import android.view.View;
import android.view.ViewGroup;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.shadows.ShadowLooper;
import static org.junit.Assert.*;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.shadows.ShadowApplication;
@RunWith(RobolectricTestRunner.class)
@Config(sdk=19, manifest=Config.NONE)
public class MeterActivityTest {
    private Spinner spinner(View v){if(v instanceof Spinner)return(Spinner)v;if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++){Spinner s=spinner(g.getChildAt(i));if(s!=null)return s;}}return null;}
    @Test public void changingRegionPersistsAndIsLockedDuringTrip(){
        ActivityController<MeterActivity> controller=Robolectric.buildActivity(MeterActivity.class).create().start().resume();MeterActivity activity=controller.get();
        Spinner s=spinner(activity.getWindow().getDecorView());assertNotNull(s);assertEquals(FareProfiles.ALL.length+1,s.getCount());
        ShadowApplication app=Shadow.extract(RuntimeEnvironment.getApplication());app.clearStartedServices();
        s.setSelection(FareProfiles.indexOf("gunsan"));ShadowLooper.idleMainLooper();assertEquals("gunsan",activity.getSharedPreferences("meter",0).getString("fare_profile",null));
        assertNotNull(app.peekNextStartedService());assertEquals(MeterService.REFRESH,app.peekNextStartedService().getAction());
        activity.getSharedPreferences("meter",0).edit().putBoolean("running",true).commit();ShadowLooper.idleMainLooper();assertFalse(s.isEnabled());
        controller.pause().stop().destroy();
    }
}
