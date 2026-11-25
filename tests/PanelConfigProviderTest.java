package kr.blackr.split;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.os.Process;
import java.util.Collections;
import java.util.concurrent.TimeUnit;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.shadows.ShadowBinder;
import org.robolectric.shadows.ShadowLooper;
import org.robolectric.shadows.ShadowPackageManager;
import static org.junit.Assert.*;
@RunWith(RobolectricTestRunner.class)
@Config(sdk=19,manifest=Config.NONE)
public class PanelConfigProviderTest {
 @Test public void onlyEnabledAddonCanAskToCloseAndOnlySystemReadsConfig(){
    Context c=RuntimeEnvironment.getApplication();SharedPreferences p=c.getSharedPreferences("split-widgets",0);String key="example.widget/.Service#meter";
    ApplicationInfo ai=new ApplicationInfo();ai.packageName="example.widget";ai.uid=12345;ai.enabled=true;PackageInfo pkg=new PackageInfo();pkg.packageName=ai.packageName;pkg.applicationInfo=ai;
    ShadowPackageManager pm=Shadow.extract(c.getPackageManager());pm.addPackage(pkg);
    PanelConfigProvider provider=Robolectric.buildContentProvider(PanelConfigProvider.class).create().get();
    p.edit().clear().putBoolean("panel-active",true).putString("order",key).putStringSet("enabled",Collections.singleton(key)).commit();
    ShadowBinder.setCallingUid(54321);try{provider.call("request_close",null,null);fail("unselected caller");}catch(SecurityException expected){}
    ShadowBinder.setCallingUid(12345);try{provider.call("snapshot",null,null);fail("private config");}catch(SecurityException expected){}
    ShadowLooper.idleMainLooper(2,TimeUnit.SECONDS);assertNotNull(provider.call("request_close",null,null));
    ShadowBinder.setCallingUid(Process.SYSTEM_UID);assertTrue(provider.call("snapshot",null,null).getLong("close_at")>0);
    p.edit().putBoolean("panel-active",false).commit();ShadowBinder.setCallingUid(12345);try{provider.call("request_close",null,null);fail("disabled panel");}catch(SecurityException expected){}
    ShadowBinder.reset();p.edit().clear().commit();
 }
}
