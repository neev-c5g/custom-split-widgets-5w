package kr.blackr.split;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import kr.blackr.split.addons.AddonDiscovery;
import kr.blackr.split.addons.AddonPager;
import kr.blackr.split.addons.PageSwipeFrame;

/** Screen-sized test host; it does not create or replace a vehicle overlay. */
public final class PanelPreviewActivity extends Activity {
    private AddonPager pager;
    @Override public void onCreate(Bundle state){
        super.onCreate(state);
        LinearLayout root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setBackgroundColor(0xff16212a);root.setPadding(24,16,24,16);
        final TextView title=new TextView(this),status=new TextView(this);title.setTextColor(-1);title.setTextSize(22);status.setTextColor(0xffffca80);status.setTextSize(16);title.setGravity(Gravity.CENTER);status.setGravity(Gravity.CENTER);
        root.addView(title,new LinearLayout.LayoutParams(-1,48));root.addView(status,new LinearLayout.LayoutParams(-1,40));
        PageSwipeFrame gesture=new PageSwipeFrame(this);FrameLayout host=new FrameLayout(this);gesture.addView(host,new FrameLayout.LayoutParams(-1,-1));root.addView(gesture,new LinearLayout.LayoutParams(-1,0,1));setContentView(root);
        pager=new AddonPager(this,host,new AddonPager.Listener(){public void onPage(int current,int count,String name){title.setText(count==0?"패널 미리보기":name+"  "+current+" / "+count);}public void onStatus(String text){status.setText(text);}});
        gesture.setOnPage(new PageSwipeFrame.OnPage(){public void onMove(int delta){pager.move(delta);}});
        SharedPreferences p=getSharedPreferences("split-widgets",MODE_PRIVATE);Set<String> enabled=p.getStringSet("enabled",new HashSet<String>());
        List<String> order=Arrays.asList(p.getString("order","").split(","));List<AddonDiscovery.Addon> found=AddonDiscovery.find(this),selected=new ArrayList<AddonDiscovery.Addon>();
        for(String key:order)for(AddonDiscovery.Addon a:found)if(enabled.contains(key)&&a.key().equals(key))selected.add(a);
        pager.setPages(selected);
    }
    @Override protected void onStart(){super.onStart();pager.setVisible(true);}
    @Override protected void onStop(){pager.setVisible(false);super.onStop();}
    @Override protected void onDestroy(){pager.close();super.onDestroy();}
}
