package kr.blackr.split.hook;

import android.content.Context;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
@Config(sdk=19, manifest=Config.NONE)
public class StockWindowCoverTest {
    private static void touch(View v, int action, float x, float y) {
        MotionEvent e = MotionEvent.obtain(0, 1, action, x, y, 0);
        v.dispatchTouchEvent(e); e.recycle();
    }
    @Test public void coverLeavesStockViewsIntactAndRestoresInput() {
        Context c = RuntimeEnvironment.getApplication();
        FrameLayout root = new FrameLayout(c); final int[] clicks = {0};
        View stock = new View(c) { @Override public boolean onTouchEvent(MotionEvent event) { if (event.getActionMasked() == MotionEvent.ACTION_UP) clicks[0]++; return true; } }; stock.setBackgroundColor(0xff112233);
        root.addView(stock, new FrameLayout.LayoutParams(644, 720));
        root.layout(0,0,644,720);
        StockPanelCover cover = new StockPanelCover(); final int[] closed = {0};
        View custom = new View(c); custom.setClickable(true);
        assertTrue(cover.attach(root,custom,new StockPanelCover.Listener(){public void close(){closed[0]++;}}));
        root.measure(View.MeasureSpec.makeMeasureSpec(644,View.MeasureSpec.EXACTLY),View.MeasureSpec.makeMeasureSpec(720,View.MeasureSpec.EXACTLY));
        root.layout(0,0,644,720);
        touch(root, MotionEvent.ACTION_DOWN,100,100);touch(root,MotionEvent.ACTION_UP,100,100);
        assertEquals(0,clicks[0]);
        touch(root,MotionEvent.ACTION_DOWN,100,100);touch(root,MotionEvent.ACTION_MOVE,180,100);touch(root,MotionEvent.ACTION_UP,180,100);
        assertEquals(1,closed[0]);
        cover.release(); assertEquals(1,root.getChildCount());assertSame(stock,root.getChildAt(0));assertNull(custom.getParent());
        touch(root,MotionEvent.ACTION_DOWN,100,100);touch(root,MotionEvent.ACTION_UP,100,100);
        assertEquals(1,clicks[0]);
    }
    @Test public void handleLeaseRejectsUnexpectedShapeAndRestoresChildren() {
        Context c=RuntimeEnvironment.getApplication();FrameLayout root=new FrameLayout(c);root.layout(0,0,80,192);
        StockHandleLease lease=new StockHandleLease(); final int[] opens={0};
        StockHandleLease.Listener listener=new StockHandleLease.Listener(){public boolean isOpen(){return false;}public void open(){opens[0]++;}public void close(){}};
        assertFalse(lease.attach(root,listener));
        ImageButton first=new ImageButton(c),second=new ImageButton(c);root.addView(first);root.addView(second);
        assertTrue(lease.attach(root,listener));assertFalse(new StockHandleLease().attach(root,listener));
        root.measure(View.MeasureSpec.makeMeasureSpec(80,View.MeasureSpec.EXACTLY),View.MeasureSpec.makeMeasureSpec(192,View.MeasureSpec.EXACTLY));root.layout(0,0,80,192);
        touch(root,MotionEvent.ACTION_DOWN,60,60);touch(root,MotionEvent.ACTION_UP,20,60);assertEquals(1,opens[0]);
        lease.release();assertEquals(2,root.getChildCount());assertSame(first,root.getChildAt(0));assertSame(second,root.getChildAt(1));
    }
    @Test public void coverKeepsCloseSwipeWhenStockContainerInterceptsMoves() {
        Context c=RuntimeEnvironment.getApplication();final int[] stolen={0},closed={0};
        FrameLayout root=new FrameLayout(c){@Override public boolean onInterceptTouchEvent(MotionEvent event){
            if(event.getActionMasked()==MotionEvent.ACTION_MOVE){stolen[0]++;return true;}return false;
        }};
        View stock=new View(c);root.addView(stock,new FrameLayout.LayoutParams(-1,-1));root.layout(0,0,644,720);
        View custom=new View(c);custom.setClickable(true);StockPanelCover cover=new StockPanelCover();
        assertTrue(cover.attach(root,custom,new StockPanelCover.Listener(){public void close(){closed[0]++;}}));
        root.measure(View.MeasureSpec.makeMeasureSpec(644,View.MeasureSpec.EXACTLY),View.MeasureSpec.makeMeasureSpec(720,View.MeasureSpec.EXACTLY));root.layout(0,0,644,720);
        touch(root,MotionEvent.ACTION_DOWN,100,100);touch(root,MotionEvent.ACTION_MOVE,180,100);touch(root,MotionEvent.ACTION_UP,180,100);
        assertEquals(0,stolen[0]);assertEquals(1,closed[0]);cover.release();
    }

    @Test public void tapsReachAddonAndVerticalSwipeDoesNotClose() {
        Context c=RuntimeEnvironment.getApplication();final int[] ups={0},closed={0};
        FrameLayout root=new FrameLayout(c);root.addView(new View(c));root.layout(0,0,644,720);
        View custom=new View(c){@Override public boolean onTouchEvent(MotionEvent event){if(event.getActionMasked()==MotionEvent.ACTION_UP)ups[0]++;return true;}};
        StockPanelCover cover=new StockPanelCover();assertTrue(cover.attach(root,custom,new StockPanelCover.Listener(){public void close(){closed[0]++;}}));
        root.measure(View.MeasureSpec.makeMeasureSpec(644,View.MeasureSpec.EXACTLY),View.MeasureSpec.makeMeasureSpec(720,View.MeasureSpec.EXACTLY));root.layout(0,0,644,720);
        touch(root,MotionEvent.ACTION_DOWN,100,100);touch(root,MotionEvent.ACTION_UP,100,100);assertEquals(1,ups[0]);
        touch(root,MotionEvent.ACTION_DOWN,100,100);touch(root,MotionEvent.ACTION_MOVE,105,210);touch(root,MotionEvent.ACTION_UP,105,210);
        assertEquals(2,ups[0]);assertEquals(0,closed[0]);cover.release();
    }

}
