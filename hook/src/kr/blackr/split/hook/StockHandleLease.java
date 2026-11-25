package kr.blackr.split.hook;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;

/**
 * Temporary input lease inside the stock 80x192 handle window.
 * This does not change stock children, listeners, alpha, settings, or window geometry.
 * A caller must release the lease when custom mode or its panel process disappears.
 */
public final class StockHandleLease {
    public interface Listener { boolean isOpen(); void open(); void close(); }
    private FrameLayout parent;private Handle cover;
    public boolean attach(FrameLayout root,Listener listener){
        if(parent!=null||root==null||listener==null||root.getWidth()!=80||root.getHeight()!=192||root.getChildCount()<2
                ||!(root.getChildAt(0) instanceof ImageButton)||!(root.getChildAt(1) instanceof ImageButton))return false;
        // Avoid stacking two custom leases if the installer is called twice.
        for(int i=0;i<root.getChildCount();i++)if(root.getChildAt(i) instanceof Handle)return false;
        Handle next=new Handle(root.getContext(),listener);
        try{root.addView(next,new FrameLayout.LayoutParams(80,192));parent=root;cover=next;return true;}
        catch(RuntimeException e){if(next.getParent()==root)root.removeView(next);return false;}
    }
    public void release(){
        FrameLayout old=parent;Handle view=cover;parent=null;cover=null;
        if(view!=null){view.cancelGesture();if(old!=null&&view.getParent()==old)old.removeView(view);}
    }
    public boolean attached(){return parent!=null&&cover!=null&&cover.getParent()==parent;}
    private static final class Handle extends View {
        private final Paint paint=new Paint(3);private final Listener listener;
        private int pointer=-1;private float x,y;
        Handle(Context c,Listener listener){super(c);this.listener=listener;setClickable(true);setContentDescription("커스텀 위젯 열기 또는 닫기");}
        void cancelGesture(){pointer=-1;}
        @Override protected void onDraw(Canvas canvas){super.onDraw(canvas);paint.setColor(0xffb5b8ba);float cx=getWidth()*.88f;canvas.drawRoundRect(new RectF(cx-3,getHeight()*.12f,cx+3,getHeight()*.88f),3,3,paint);}
        @Override public boolean onTouchEvent(MotionEvent event){
            switch(event.getActionMasked()){
                case MotionEvent.ACTION_DOWN:pointer=event.getPointerId(0);x=event.getX();y=event.getY();return true;
                case MotionEvent.ACTION_POINTER_DOWN:case MotionEvent.ACTION_CANCEL:cancelGesture();return true;
                case MotionEvent.ACTION_UP:
                    int i=event.findPointerIndex(pointer);
                    if(i>=0){float dx=event.getX(i)-x,dy=event.getY(i)-y;
                        if(Math.abs(dx)>=24&&Math.abs(dx)>Math.abs(dy)*1.5f){if(!listener.isOpen()&&dx<0)listener.open();else if(listener.isOpen()&&dx>0)listener.close();}}
                    cancelGesture();return true;
                default:return true;
            }
        }
        @Override protected void onDetachedFromWindow(){cancelGesture();super.onDetachedFromWindow();}
    }
}
