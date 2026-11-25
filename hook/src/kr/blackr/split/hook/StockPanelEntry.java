package kr.blackr.split.hook;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.SystemClock;
import android.provider.Settings;
import android.net.Uri;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import hmgapi.hook.LoadedApp;
import hmgapi.hook.ModContext;
import hmgapi.hook.ModEntry;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import kr.blackr.split.addons.AddonDiscovery;
import kr.blackr.split.addons.AddonPager;
import kr.blackr.split.addons.PageSwipeFrame;

/** Observes the stock panel; it never replaces the stock handle or opens the panel itself. */
public final class StockPanelEntry extends ModEntry {
    private static final Uri CONFIG = Uri.parse("content://kr.blackr.customsplit.config");
    private Handler ui;
    private final StockPanelCover cover = new StockPanelCover();
    private HandlerThread workerThread;
    private Handler worker;
    private Context context;
    private ModContext mod;
    private boolean active;
    private long lastConfig, lastCloseRequest;
    private ArrayList<String> keys = new ArrayList<String>();
    private ViewGroup parent;
    private AddonPager pager;
    private TextView title, status;
    private final Runnable poll = new Runnable() { public void run() {
        Bundle snapshot = null;
        try { snapshot = context.getContentResolver().call(CONFIG, "snapshot", null, null); }
        catch (RuntimeException ignored) { }
        final Bundle result = snapshot;
        ui.post(new Runnable() { public void run() {
            if (result == null) { active = false; keys.clear(); }
            else {
                active = result.getBoolean("active", false);
                ArrayList<String> next = result.getStringArrayList("selected");
                keys = next == null ? new ArrayList<String>() : next;
                long now=SystemClock.elapsedRealtime();lastConfig=now;
                long requested=result.getLong("close_at",0);
                if(active && requested>lastCloseRequest && requested<=now && now-requested<3000){
                    lastCloseRequest=requested;
                    if(Settings.System.getInt(context.getContentResolver(),"split_screen_status",-1)==1)stockClose();
                }
            }
        }});
        if (worker != null) worker.postDelayed(this, 1000);
    }};
    private final Runnable scan = new Runnable() { public void run() {
        try {
            if (mod == null || !mod.enabled() || !active || SystemClock.elapsedRealtime()-lastConfig > 3500) {
                detach();
            } else {
                View root = window("com.mobis.splitscreen.right", 2045);
                int state = Settings.System.getInt(context.getContentResolver(), "split_screen_status", -1);
                int id = root == null ? 0 : root.getResources().getIdentifier("main_view", "id", "com.mobis.splitscreen.right");
                View candidate = id == 0 ? null : root.findViewById(id);
                if (state != 1 || root == null || !root.isShown() || !(candidate instanceof FrameLayout)
                        || !candidate.isShown() || candidate.getWidth() <= 0 || candidate.getHeight() <= 0) detach();
                else {
                    if (candidate != parent || !cover.attached()) { detach(); attach((FrameLayout) candidate); }
                    if (pager != null) updatePages();
                }
            }
        } catch (RuntimeException ignored) { detach(); }
          catch (ReflectiveOperationException ignored) { detach(); }
          catch (LinkageError ignored) { detach(); }
        ui.postDelayed(this, 250);
    }};
    @Override public void appLoaded(ModContext m, LoadedApp app) {
        if (app == null || !"com.mobis.splitscreen.right".equals(app.packageName)) return;
        mod = m; context = app.context;
        if (ui == null) ui = new Handler(Looper.getMainLooper());
        if (workerThread == null) {
            workerThread = new HandlerThread("custom-split-config"); workerThread.start();
            worker = new Handler(workerThread.getLooper()); worker.post(poll);
        }
        ui.removeCallbacks(scan); ui.post(scan);
    }
    @Override public void onEnabled(ModContext m, boolean enabled) {
        if (!enabled && ui != null) ui.post(new Runnable() { public void run() { detach(); }});
    }
    private void attach(FrameLayout target) {
        LinearLayout layout = new LinearLayout(context); layout.setOrientation(LinearLayout.VERTICAL);
        layout.setBackgroundColor(0xff16212a); layout.setPadding(24, 16, 24, 16);
        title = new TextView(context); title.setTextColor(-1); title.setTextSize(22); title.setGravity(Gravity.CENTER);
        status = new TextView(context); status.setTextColor(0xffffca80); status.setTextSize(16); status.setGravity(Gravity.CENTER);
        layout.addView(title, new LinearLayout.LayoutParams(-1, 48)); layout.addView(status, new LinearLayout.LayoutParams(-1, 40));
        PageSwipeFrame gesture = new PageSwipeFrame(context); FrameLayout host = new FrameLayout(context);
        gesture.addView(host, new FrameLayout.LayoutParams(-1, -1)); layout.addView(gesture, new LinearLayout.LayoutParams(-1, 0, 1));
        pager = new AddonPager(context, host, new AddonPager.Listener() {
            public void onPage(int current,int count,String name) { title.setText(count == 0 ? "커스텀 위젯" : name + "  " + current + " / " + count); }
            public void onStatus(String value) { status.setText(value); }
        });
        gesture.setOnPage(new PageSwipeFrame.OnPage() { public void onMove(int delta) { if (pager != null) pager.move(delta); }});
        if (!cover.attach(target, layout, new StockPanelCover.Listener() { public void close() { stockClose(); }})) { pager.close(); pager = null; return; }
        parent = target; updatePages(); pager.setVisible(true);
    }
    private ArrayList<String> shownKeys = new ArrayList<String>();
    private void updatePages() {
        if (pager == null || shownKeys.equals(keys)) return;
        List<AddonDiscovery.Addon> found = AddonDiscovery.find(context), selected = new ArrayList<AddonDiscovery.Addon>();
        for (String key : keys) for (AddonDiscovery.Addon addon : found) if (key.equals(addon.key())) selected.add(addon);
        pager.setPages(selected); shownKeys = new ArrayList<String>(keys);
    }
    private void detach() {
        if (pager != null) { pager.close(); pager = null; }
        cover.release(); parent = null; title = null; status = null; shownKeys.clear();
    }
    private void stockClose() {
        try {
            View root = window("SS_Button_Window", 2026);
            if (!(root instanceof ViewGroup)) return;
            ViewGroup group = (ViewGroup) root;
            for (int i=0;i<Math.min(2,group.getChildCount());i++) {
                View target=group.getChildAt(i);
                if (!(target instanceof android.widget.ImageButton) || !target.isShown()) continue;
                long now=SystemClock.uptimeMillis();float x=target.getWidth()/2f,y=target.getHeight()/2f;
                MotionEvent down=MotionEvent.obtain(now,now,MotionEvent.ACTION_DOWN,x,y,0), up=MotionEvent.obtain(now,now+30,MotionEvent.ACTION_UP,x,y,0);
                try { target.dispatchTouchEvent(down);target.dispatchTouchEvent(up); } finally { down.recycle();up.recycle(); }
                return;
            }
        } catch (ReflectiveOperationException ignored) { } catch (RuntimeException ignored) { }
    }
    private static View window(String title,int type) throws ReflectiveOperationException {
        Class<?> cls=Class.forName("android.view.WindowManagerGlobal");Object global=cls.getMethod("getInstance").invoke(null);
        Field field=cls.getDeclaredField("mViews");field.setAccessible(true);Object views=field.get(global);
        int count=views instanceof List ? ((List<?>)views).size() : Array.getLength(views);
        for(int i=0;i<count;i++) {
            Object item=views instanceof List ? ((List<?>)views).get(i) : Array.get(views,i);
            if (!(item instanceof View)) continue;
            View v=(View)item;ViewGroup.LayoutParams p=v.getLayoutParams();
            if (p instanceof WindowManager.LayoutParams) {
                WindowManager.LayoutParams w=(WindowManager.LayoutParams)p;
                if (title.contentEquals(w.getTitle()) || (type==2045 && w.type==type && v.getWidth()>0)) return v;
            }
        }
        return null;
    }
}
