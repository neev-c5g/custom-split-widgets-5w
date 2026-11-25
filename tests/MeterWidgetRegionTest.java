package dev.ev6.taximeter.prototype;
import android.content.Context;
import android.widget.RemoteViews;
import java.lang.reflect.Field;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import taximeter.TaxiMeter;
import static org.junit.Assert.*;
@RunWith(RobolectricTestRunner.class)
@Config(sdk=19,manifest=Config.NONE)
public class MeterWidgetRegionTest {
    private String text(RemoteViews view,int id)throws Exception{
        Field actions=RemoteViews.class.getDeclaredField("mActions");actions.setAccessible(true);
        for(Object action:(List<?>)actions.get(view)){
            Class<?> type=action.getClass();try{
                Field viewId=type.getSuperclass().getDeclaredField("viewId");viewId.setAccessible(true);
                Field method=type.getDeclaredField("methodName");method.setAccessible(true);
                Field value=type.getDeclaredField("value");value.setAccessible(true);
                if(viewId.getInt(action)==id&&"setText".equals(method.get(action)))return String.valueOf(value.get(action));
            }catch(NoSuchFieldException ignored){}
        }return null;
    }
    @Test public void changedRegionShownAlongsidePriorTripWithoutRelabelingFare()throws Exception{
        Context c=RuntimeEnvironment.getApplication();c.getSharedPreferences("meter",0).edit().clear().putString("fare_profile","gunsan").putString("fare_trip_profile","seoul").putLong("fare_trip_base",4800).commit();
        TaxiMeter.Snapshot previous=TaxiMeter.Snapshot.restored(true,1000,1000,4800,0);
        RemoteViews v=MeterWidget.makeViews(c,previous,true,false);
        assertEquals("전북 · 다음 운행",text(v,R.id.meter_status));assertTrue(text(v,R.id.meter_breakdown).contains("최근 운행: 서울 중형"));
    }
}
