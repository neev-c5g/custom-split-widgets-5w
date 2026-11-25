package kr.blackr.split;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import kr.blackr.split.addons.AddonDiscovery;
import kr.blackr.split.core.WidgetRegistry;

/** Local configuration for the custom stock-panel cover. */
public final class SettingsActivity extends Activity {
    private SharedPreferences preferences;
    private WidgetRegistry registry;
    private LinearLayout items;
    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        preferences=getSharedPreferences("split-widgets",MODE_PRIVATE);
        List<WidgetRegistry.Widget> widgets=new ArrayList<WidgetRegistry.Widget>();
        for(AddonDiscovery.Addon addon:AddonDiscovery.find(this))
            widgets.add(new WidgetRegistry.Widget(addon.key(),addon.title+" · "+addon.component.getPackageName()));
        String saved=preferences.getString("order","");
        List<String> order=saved.isEmpty()?new ArrayList<String>():Arrays.asList(saved.split(","));
        Set<String> enabled=preferences.getStringSet("enabled",new HashSet<String>());
        registry=new WidgetRegistry(widgets,order,enabled);
        LinearLayout page=new LinearLayout(this);page.setOrientation(LinearLayout.VERTICAL);page.setPadding(32,20,32,20);page.setBackgroundColor(0xff16212a);
        TextView title=new TextView(this);title.setText("분할화면 커스텀");title.setTextColor(Color.WHITE);title.setTextSize(30);page.addView(title);
        TextView help=new TextView(this);help.setText("순정 오른쪽 창을 열면 선택한 커스텀 위젯이 표시됩니다.");help.setTextColor(0xffcad7dc);help.setTextSize(18);page.addView(help);
        CheckBox active=new CheckBox(this);active.setText("커스텀 분할 위젯 사용");active.setTextColor(Color.WHITE);active.setTextSize(20);active.setChecked(preferences.getBoolean("panel-active",false));
        active.setOnClickListener(new View.OnClickListener(){public void onClick(View v){preferences.edit().putBoolean("panel-active",((CheckBox)v).isChecked()).apply();}});page.addView(active);
        Button previewPanel=new Button(this);previewPanel.setText("선택한 외부 위젯 미리보기");
        previewPanel.setOnClickListener(new View.OnClickListener(){public void onClick(View v){startActivity(new Intent(SettingsActivity.this,PanelPreviewActivity.class));}});page.addView(previewPanel);
        ScrollView scroll=new ScrollView(this);items=new LinearLayout(this);items.setOrientation(LinearLayout.VERTICAL);scroll.addView(items);page.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));
        setContentView(page);render();
    }
    private void render(){
        items.removeAllViews();final List<WidgetRegistry.Widget> all=registry.all();
        for(int i=0;i<all.size();i++){
            final WidgetRegistry.Widget widget=all.get(i);final int position=i;
            LinearLayout row=new LinearLayout(this);row.setGravity(Gravity.CENTER_VERTICAL);
            CheckBox on=new CheckBox(this);on.setText(widget.title);on.setTextSize(22);on.setTextColor(Color.WHITE);on.setChecked(registry.isEnabled(widget.id));
            row.addView(on,new LinearLayout.LayoutParams(0,80,1));
            on.setOnClickListener(new View.OnClickListener(){public void onClick(View v){registry.setEnabled(widget.id,((CheckBox)v).isChecked());save();}});
            {
                Button preview=new Button(this);preview.setText("미리보기");preview.setEnabled(registry.isEnabled(widget.id));row.addView(preview,new LinearLayout.LayoutParams(180,72));
                preview.setOnClickListener(new View.OnClickListener(){public void onClick(View v){startActivity(new Intent(SettingsActivity.this,AddonPreviewActivity.class).putExtra(AddonPreviewActivity.EXTRA_KEY,widget.id));}});
                on.setOnClickListener(new View.OnClickListener(){public void onClick(View v){registry.setEnabled(widget.id,((CheckBox)v).isChecked());save();render();}});
            }
            Button up=new Button(this);up.setText("↑");up.setEnabled(i>0);row.addView(up,new LinearLayout.LayoutParams(80,72));
            up.setOnClickListener(new View.OnClickListener(){public void onClick(View v){registry.move(widget.id,position-1);save();render();}});
            Button down=new Button(this);down.setText("↓");down.setEnabled(i+1<all.size());row.addView(down,new LinearLayout.LayoutParams(80,72));
            down.setOnClickListener(new View.OnClickListener(){public void onClick(View v){registry.move(widget.id,position+1);save();render();}});
            items.addView(row);
        }
    }
    private void save(){
        StringBuilder order=new StringBuilder();for(String id:registry.savedOrder()){if(order.length()>0)order.append(',');order.append(id);}
        preferences.edit().putString("order",order.toString()).putStringSet("enabled",new HashSet<String>(registry.savedEnabled())).apply();
    }
}
