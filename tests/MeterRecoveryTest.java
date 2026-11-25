package dev.ev6.taximeter.prototype;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.HandlerThread;
import java.lang.reflect.Field;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.android.controller.ServiceController;
import org.robolectric.annotation.Config;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.shadows.ShadowLooper;
import static org.junit.Assert.*;
@RunWith(RobolectricTestRunner.class)
@Config(sdk=19,manifest=Config.NONE)
public class MeterRecoveryTest {
    private void restore(MeterService service)throws Exception{Field f=MeterService.class.getDeclaredField("thread");f.setAccessible(true);HandlerThread thread=(HandlerThread)f.get(service);((ShadowLooper)Shadow.extract(thread.getLooper())).idle();}
    @Test public void interruptedTripSavedOnceAndNotPretendedComplete()throws Exception{
        Context c=RuntimeEnvironment.getApplication();c.deleteDatabase("trips.db");SharedPreferences p=c.getSharedPreferences("meter",0);p.edit().clear().putBoolean("present",true).putBoolean("running",true).putBoolean("complete",true).putString("trip_key","recover-1").putLong("trip_started_at",123456).putLong("elapsed",30000).putLong("slow",1000).putLong("fare",4900).putLong("distance",Double.doubleToRawLongBits(200.0)).commit();
        ServiceController<MeterService> first=Robolectric.buildService(MeterService.class).create();restore(first.get());first.destroy();
        TripHistory db=new TripHistory(c);List<TripHistory.Trip> rows=db.page(0);assertEquals(1,rows.size());assertTrue(rows.get(0).interrupted);assertFalse(rows.get(0).complete);assertEquals(0,rows.get(0).endedAt);assertEquals(4900,rows.get(0).fare);assertFalse(p.getBoolean("running",true));db.close();
        ServiceController<MeterService> second=Robolectric.buildService(MeterService.class).create();restore(second.get());second.destroy();db=new TripHistory(c);assertEquals(1,db.page(0).size());assertTrue(db.page(0).get(0).interrupted);db.close();c.deleteDatabase("trips.db");p.edit().clear().commit();
    }
    @Test public void priorVersionResultImportedWithoutInventingTime()throws Exception{
        Context c=RuntimeEnvironment.getApplication();c.deleteDatabase("trips.db");SharedPreferences p=c.getSharedPreferences("meter",0);p.edit().clear().putBoolean("present",true).putBoolean("complete",true).putLong("elapsed",1000).putLong("slow",0).putLong("fare",4800).putLong("distance",Double.doubleToRawLongBits(2.0)).commit();
        ServiceController<MeterService> service=Robolectric.buildService(MeterService.class).create();restore(service.get());service.destroy();TripHistory db=new TripHistory(c);List<TripHistory.Trip> rows=db.page(0);assertEquals(1,rows.size());assertEquals("legacy-last",rows.get(0).key);assertEquals(0,rows.get(0).startedAt);assertEquals(0,rows.get(0).endedAt);db.close();c.deleteDatabase("trips.db");p.edit().clear().commit();
    }
    @Test public void completedTripIsSavedOnceWithLiveTotals()throws Exception{
        Context c=RuntimeEnvironment.getApplication();c.deleteDatabase("trips.db");SharedPreferences p=c.getSharedPreferences("meter",0);p.edit().clear().putString("fare_profile","gunsan").commit();
        ServiceController<MeterService> controller=Robolectric.buildService(MeterService.class).create();MeterService service=controller.get();
        service.setSpeedSourceForTest(new MeterService.SpeedSource(){public double readKmh(){return 36.0;}});restore(service);
        Field f=MeterService.class.getDeclaredField("thread");f.setAccessible(true);ShadowLooper worker=(ShadowLooper)Shadow.extract(((HandlerThread)f.get(service)).getLooper());
        service.onStartCommand(new android.content.Intent().setAction(MeterService.START),0,1);worker.idle();assertTrue(p.getBoolean("running",false));String key=p.getString("trip_key","");assertFalse(key.isEmpty());
        worker.idleFor(5,java.util.concurrent.TimeUnit.SECONDS);service.onStartCommand(new android.content.Intent().setAction(MeterService.STOP),0,2);worker.idle();assertFalse(p.getBoolean("running",true));
        TripHistory db=new TripHistory(c);List<TripHistory.Trip> rows=db.page(0);assertEquals(1,rows.size());assertEquals(key,rows.get(0).key);assertEquals("전북",rows.get(0).region);assertTrue(rows.get(0).complete);assertTrue(rows.get(0).distance>0);assertTrue(rows.get(0).endedAt>=rows.get(0).startedAt);db.close();
        service.onStartCommand(new android.content.Intent().setAction(MeterService.STOP),0,3);worker.idle();db=new TripHistory(c);assertEquals(1,db.page(0).size());db.close();controller.destroy();c.deleteDatabase("trips.db");p.edit().clear().commit();
    }

    @Test public void corruptSavedTripBlocksNewTripInsteadOfOverwriting()throws Exception{
        Context c=RuntimeEnvironment.getApplication();c.deleteDatabase("trips.db");SharedPreferences p=c.getSharedPreferences("meter",0);
        p.edit().clear().putBoolean("present",true).putBoolean("running",false).putLong("elapsed",-1).putLong("fare",5300).commit();
        ServiceController<MeterService> controller=Robolectric.buildService(MeterService.class).create();MeterService service=controller.get();final int[] reads={0};
        service.setSpeedSourceForTest(new MeterService.SpeedSource(){public double readKmh(){reads[0]++;return 0;}});restore(service);
        assertTrue(p.getBoolean("history_error",false));service.onStartCommand(new android.content.Intent().setAction(MeterService.START),0,1);restore(service);
        assertEquals(0,reads[0]);assertEquals(-1,p.getLong("elapsed",0));assertEquals(5300,p.getLong("fare",0));controller.destroy();c.deleteDatabase("trips.db");p.edit().clear().commit();
    }

}
