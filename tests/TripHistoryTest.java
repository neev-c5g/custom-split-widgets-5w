package dev.ev6.taximeter.prototype;
import android.content.Context;
import java.util.List;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import static org.junit.Assert.*;
@RunWith(RobolectricTestRunner.class)
@Config(sdk=19,manifest=Config.NONE)
public class TripHistoryTest {
    Context context;TripHistory db;
    @Before public void setUp(){context=RuntimeEnvironment.getApplication();context.deleteDatabase("trips.db");db=new TripHistory(context);}
    @After public void tearDown(){db.close();context.deleteDatabase("trips.db");}
    private TripHistory.Trip trip(int n){TripHistory.Trip t=new TripHistory.Trip();t.key="trip-"+n;t.region="서울";t.startedAt=1000+n;t.endedAt=2000+n;t.fare=4800+n;t.base=4800;t.distance=123.5+n;t.elapsed=30000;t.slow=1000;t.complete=true;return t;}
    @Test public void savesOnceAndPagesNewestFirst(){
        for(int i=0;i<55;i++)assertTrue(db.add(trip(i)));assertFalse(db.add(trip(0)));
        List<TripHistory.Trip> first=db.page(0);assertEquals(50,first.size());assertEquals("trip-54",first.get(0).key);assertEquals(4854,first.get(0).fare,0);
        List<TripHistory.Trip> next=db.page(first.get(49).id);assertEquals(5,next.size());assertEquals("trip-0",next.get(4).key);assertTrue(db.page(next.get(4).id).isEmpty());
    }
    @Test public void rejectsInvalidTotals(){TripHistory.Trip t=trip(1);t.slow=31000;try{db.add(t);fail("invalid slow time");}catch(IllegalArgumentException expected){}}
}
