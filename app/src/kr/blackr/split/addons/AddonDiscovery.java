package kr.blackr.split.addons;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.os.Bundle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import kr.blackr.split.sdk.WidgetProtocol;

/** Discovery is read-only: no add-on is bound or activated here. */
public final class AddonDiscovery {
    public static final class Addon {
        public final ComponentName component; public final String widgetId,title;
        Addon(ComponentName component,String widgetId,String title){this.component=component;this.widgetId=widgetId;this.title=title;}
        /** Component plus provider-defined ID remains distinct across packages. */
        public String key(){return component.flattenToString()+"#"+widgetId;}
    }
    public static List<Addon> find(Context context) {
        PackageManager pm=context.getPackageManager();
        List<ResolveInfo> matches=pm.queryIntentServices(new Intent(WidgetProtocol.SERVICE_ACTION),PackageManager.GET_META_DATA);
        List<Addon> result=new ArrayList<Addon>();HashSet<String> seen=new HashSet<String>();
        if(matches==null)return result;
        for(ResolveInfo match:matches){
            ServiceInfo info=match==null?null:match.serviceInfo;
            if(info==null||!info.enabled||!info.exported||info.applicationInfo==null||!info.applicationInfo.enabled||info.packageName==null||info.name==null)continue;
            Bundle meta=info.metaData;if(meta==null||meta.getInt(WidgetProtocol.META_VERSION,-1)!=WidgetProtocol.VERSION)continue;
            Object rawId=meta.get(WidgetProtocol.META_ID),rawTitle=meta.get(WidgetProtocol.META_TITLE);
            if(!(rawId instanceof String)||!(rawTitle instanceof String))continue;
            String id=((String)rawId).trim(),title=((String)rawTitle).trim();
            if(!id.matches("[a-z][a-z0-9_.-]{0,79}")||title.isEmpty()||title.length()>60)continue;
            Addon addon=new Addon(new ComponentName(info.packageName,info.name),id,title);
            if(!addon.key().matches("[A-Za-z][A-Za-z0-9_.$/#-]{0,239}"))continue;
            if(seen.add(addon.key()))result.add(addon);
        }
        Collections.sort(result,new Comparator<Addon>(){public int compare(Addon a,Addon b){return a.key().compareTo(b.key());}});
        return result;
    }
}
