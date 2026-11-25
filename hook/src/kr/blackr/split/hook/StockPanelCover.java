package kr.blackr.split.hook;

import android.graphics.Color;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.FrameLayout;

/** Reversible opaque cover in the existing stock panel window. */
public final class StockPanelCover {
    public interface Listener { void close(); }
    private ViewGroup parent;
    private Cover cover;

    /** Must be called on the window's UI thread; content must not already have a parent. */
    public boolean attach(ViewGroup root, View content, Listener listener) {
        if (parent != null || root == null || content == null || listener == null
                || content.getParent() != null || root.getWidth() <= 0 || root.getHeight() <= 0) return false;
        for (int i = 0; i < root.getChildCount(); i++) {
            if (root.getChildAt(i) instanceof Cover) return false;
        }
        Cover next = new Cover(root, listener);
        try {
            next.addView(content, new FrameLayout.LayoutParams(-1, -1));
            root.addView(next, new ViewGroup.LayoutParams(-1, -1));
            parent = root;
            cover = next;
            return true;
        } catch (RuntimeException e) {
            if (next.getParent() == root) root.removeView(next);
            if (content.getParent() == next) next.removeView(content);
            return false;
        }
    }

    public boolean attached() {
        return parent != null && cover != null && cover.getParent() == parent;
    }

    /** Removes only our views, preserving all stock children and their state. */
    public void release() {
        ViewGroup old = parent;
        Cover view = cover;
        parent = null;
        cover = null;
        if (view != null) {
            view.cancelGesture();
            if (old != null && view.getParent() == old) { old.requestDisallowInterceptTouchEvent(false); old.removeView(view); }
            view.removeAllViews();
        }
    }

    private static final class Cover extends FrameLayout {
        private final Listener listener;
        private float startX, startY;
        private int pointer = -1;
        private boolean closing;
        Cover(ViewGroup root, Listener listener) {
            super(root.getContext());
            this.listener = listener;
            setBackgroundColor(Color.rgb(22, 33, 42));
            setClickable(true);
        }
        void cancelGesture() { pointer = -1; closing = false; }
        @Override public boolean dispatchTouchEvent(MotionEvent event) {
            int action = event.getActionMasked();
            ViewParent parent = getParent();
            // The stock sliding container may otherwise take MOVE before this overlay sees it.
            if (action == MotionEvent.ACTION_DOWN && parent != null) parent.requestDisallowInterceptTouchEvent(true);
            boolean handled = super.dispatchTouchEvent(event);
            if ((action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) && parent != null)
                parent.requestDisallowInterceptTouchEvent(false);
            return handled;
        }
        @Override public boolean onInterceptTouchEvent(MotionEvent event) {
            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    pointer = event.getPointerId(0); startX = event.getX(); startY = event.getY(); closing = false;
                    return false;
                case MotionEvent.ACTION_POINTER_DOWN:
                case MotionEvent.ACTION_CANCEL:
                    cancelGesture(); return false;
                case MotionEvent.ACTION_MOVE:
                    int index = event.findPointerIndex(pointer);
                    if (index < 0) return false;
                    float dx = event.getX(index) - startX, dy = event.getY(index) - startY;
                    if (dx >= 48 && dx > Math.abs(dy) * 1.5f) { closing = true; return true; }
                    return false;
                case MotionEvent.ACTION_UP:
                    cancelGesture(); return false;
                default: return false;
            }
        }
        @Override public boolean onTouchEvent(MotionEvent event) {
            if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
                pointer = event.getPointerId(0); startX = event.getX(); startY = event.getY(); closing = false;
                return true;
            }
            if (event.getActionMasked() == MotionEvent.ACTION_MOVE && !closing) {
                int index = event.findPointerIndex(pointer);
                if (index >= 0) {
                    float dx = event.getX(index) - startX, dy = event.getY(index) - startY;
                    closing = dx >= 48 && dx > Math.abs(dy) * 1.5f;
                }
            } else if (event.getActionMasked() == MotionEvent.ACTION_UP) {
                boolean close = closing;
                cancelGesture();
                if (close) listener.close();
            } else if (event.getActionMasked() == MotionEvent.ACTION_POINTER_DOWN
                    || event.getActionMasked() == MotionEvent.ACTION_CANCEL) cancelGesture();
            return true;
        }
        @Override protected void onDetachedFromWindow() {
            ViewParent parent = getParent();
            if (parent != null) parent.requestDisallowInterceptTouchEvent(false);
            cancelGesture(); super.onDetachedFromWindow();
        }
    }
}
