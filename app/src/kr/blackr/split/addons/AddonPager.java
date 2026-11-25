package kr.blackr.split.addons;

import android.content.Context;
import android.view.View;
import android.os.Handler;
import android.os.Looper;
import android.widget.FrameLayout;
import java.util.ArrayList;
import java.util.List;

/** Owns only the selected add-on session; safe to release when the panel closes. */
public final class AddonPager {
    public interface Listener { void onPage(int current,int count,String title); void onStatus(String message); }
    private final Context context; private final FrameLayout host; private final Listener listener;
    private final List<AddonDiscovery.Addon> pages=new ArrayList<AddonDiscovery.Addon>();
    private int index=-1; private AddonSession session; private boolean visible;
    private final Handler ui=new Handler(Looper.getMainLooper());
    private Runnable retry;private int generation,retries;
    public AddonPager(Context context,FrameLayout host,Listener listener){
        if(context==null||host==null||listener==null)throw new IllegalArgumentException();
        this.context=context;this.host=host;this.listener=listener;
    }
    public void setPages(List<AddonDiscovery.Addon> requested){
        if(requested==null)throw new IllegalArgumentException();
        String selected=index>=0&&index<pages.size()?pages.get(index).key():null;
        ArrayList<AddonDiscovery.Addon> next=new ArrayList<AddonDiscovery.Addon>();
        for(AddonDiscovery.Addon a:requested){if(a==null)throw new IllegalArgumentException();for(AddonDiscovery.Addon b:next)if(b.key().equals(a.key()))throw new IllegalArgumentException("duplicate addon");next.add(a);}
        closeSession();retries=0;pages.clear();pages.addAll(next);index=-1;
        for(int i=0;i<pages.size();i++)if(pages.get(i).key().equals(selected)){index=i;break;}
        if(index<0&&!pages.isEmpty())index=0;
        if(visible)show();
    }
    public void setVisible(boolean visible){if(this.visible==visible)return;this.visible=visible;if(visible){retries=0;show();}else closeSession();}
    public boolean move(int delta){
        if(!visible||index<0||delta==0)return false;int next=index+(delta<0?-1:1);
        if(next<0||next>=pages.size())return false;closeSession();retries=0;index=next;show();return true;
    }
    private void show(){
        closeSession();
        if(index<0){listener.onPage(0,0,"");listener.onStatus("활성화한 외부 위젯이 없습니다");return;}
        AddonDiscovery.Addon a=pages.get(index);listener.onPage(index+1,pages.size(),a.title);listener.onStatus("연결 중");
        final int attempt=generation;
        session=new AddonSession(context,a,host,new AddonSession.Listener(){public void onStatus(String value){
            if(!visible||attempt!=generation)return;
            listener.onStatus(value);
            if(value.isEmpty()){retries=0;cancelRetry();}
            else if(!"연결 중".equals(value))scheduleRetry(attempt);
        }});session.start();
    }
    private void scheduleRetry(final int attempt){
        if(retry!=null)return;
        long delay=Math.min(30000L,2000L << Math.min(retries++,4));
        retry=new Runnable(){public void run(){retry=null;if(visible&&attempt==generation)show();}};
        ui.postDelayed(retry,delay);
    }
    private void cancelRetry(){if(retry!=null){ui.removeCallbacks(retry);retry=null;}}
    private void closeSession(){generation++;cancelRetry();if(session!=null){session.close();session=null;}else host.removeAllViews();}
    public void close(){visible=false;closeSession();}
}
