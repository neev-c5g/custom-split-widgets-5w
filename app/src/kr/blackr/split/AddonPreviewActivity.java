package kr.blackr.split;

import android.app.Activity;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.util.HashSet;
import kr.blackr.split.addons.AddonDiscovery;
import kr.blackr.split.addons.AddonSession;

/** Explicit preview only; binds no provider unless the exact item is enabled. */
public final class AddonPreviewActivity extends Activity {
    public static final String EXTRA_KEY="widget_key";
    private AddonSession session;
    private TextView status;
    private FrameLayout host;
    private String key;
    @Override public void onCreate(Bundle state){
        super.onCreate(state);
        key=getIntent().getStringExtra(EXTRA_KEY);
        LinearLayout root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setPadding(24,24,24,24);root.setBackgroundColor(0xff16212a);
        status=new TextView(this);status.setTextColor(0xffffffff);status.setTextSize(20);status.setGravity(Gravity.CENTER);status.setText("연결 중");root.addView(status,new LinearLayout.LayoutParams(-1,60));
        host=new FrameLayout(this);root.addView(host,new LinearLayout.LayoutParams(-1,0,1));setContentView(root);
    }
    @Override protected void onStart(){
        super.onStart();
        if(key==null||!getSharedPreferences("split-widgets",MODE_PRIVATE).getStringSet("enabled",new HashSet<String>()).contains(key)){status.setText("활성화된 위젯을 선택해 주세요");return;}
        AddonDiscovery.Addon selected=null;for(AddonDiscovery.Addon a:AddonDiscovery.find(this))if(key.equals(a.key())){selected=a;break;}
        if(selected==null){status.setText("위젯을 찾을 수 없습니다");return;}
        status.setText("연결 중");session=new AddonSession(this,selected,host,new AddonSession.Listener(){public void onStatus(String message){status.setText(message);}});session.start();
    }
    @Override protected void onStop(){if(session!=null){session.close();session=null;}super.onStop();}
}
