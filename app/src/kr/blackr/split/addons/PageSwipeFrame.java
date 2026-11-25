package kr.blackr.split.addons;

import android.content.Context;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.widget.FrameLayout;

/** Intercepts only decisive vertical swipes; taps remain with the add-on's controls. */
public final class PageSwipeFrame extends FrameLayout {
    public interface OnPage { void onMove(int delta); }
    private final float slop; private OnPage listener;private float x,y;private int pointer=-1;private boolean intercept;
    public PageSwipeFrame(Context c){super(c);slop=Math.max(24f,ViewConfiguration.get(c).getScaledTouchSlop()*2f);}
    public void setOnPage(OnPage listener){this.listener=listener;}
    @Override public boolean onInterceptTouchEvent(MotionEvent e){
        switch(e.getActionMasked()){
            case MotionEvent.ACTION_DOWN: pointer=e.getPointerId(0);x=e.getX();y=e.getY();intercept=false;return false;
            case MotionEvent.ACTION_POINTER_DOWN: case MotionEvent.ACTION_CANCEL: pointer=-1;intercept=false;return false;
            case MotionEvent.ACTION_MOVE:
                int i=e.findPointerIndex(pointer);if(i<0)return false;
                float dx=e.getX(i)-x,dy=e.getY(i)-y;
                if(Math.abs(dy)>=slop&&Math.abs(dy)>Math.abs(dx)*1.5f){intercept=true;return true;}return false;
            case MotionEvent.ACTION_UP: pointer=-1;return false;
            default:return false;
        }
    }
    @Override public boolean onTouchEvent(MotionEvent e){
        if(e.getActionMasked()==MotionEvent.ACTION_DOWN){pointer=e.getPointerId(0);x=e.getX();y=e.getY();intercept=false;return true;}
        if(e.getActionMasked()==MotionEvent.ACTION_MOVE&&!intercept){int i=e.findPointerIndex(pointer);if(i>=0){float dx=e.getX(i)-x,dy=e.getY(i)-y;intercept=Math.abs(dy)>=slop&&Math.abs(dy)>Math.abs(dx)*1.5f;}return true;}
        if(!intercept){if(e.getActionMasked()==MotionEvent.ACTION_UP||e.getActionMasked()==MotionEvent.ACTION_CANCEL)pointer=-1;return true;}
        if(e.getActionMasked()==MotionEvent.ACTION_UP){int i=e.findPointerIndex(pointer);if(i>=0&&listener!=null){float dy=e.getY(i)-y;if(Math.abs(dy)>=slop)listener.onMove(dy<0?1:-1);}pointer=-1;intercept=false;return true;}
        if(e.getActionMasked()==MotionEvent.ACTION_CANCEL||e.getActionMasked()==MotionEvent.ACTION_POINTER_DOWN){pointer=-1;intercept=false;return true;}
        return true;
    }
}
