package kr.blackr.split;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.ComponentName;
import android.content.pm.PackageManager;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.Process;
import android.os.SystemClock;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/** Configuration for the system panel, plus a close request from an enabled add-on. */
public final class PanelConfigProvider extends ContentProvider {
    public static final String AUTHORITY = "kr.blackr.customsplit.config";
    public static final Uri URI = Uri.parse("content://" + AUTHORITY);
    private static long closeAt;
    @Override public boolean onCreate() { return true; }
    @Override public Bundle call(String method, String arg, Bundle extras) {
        int caller = Binder.getCallingUid();
        SharedPreferences prefs = getContext().getSharedPreferences("split-widgets", 0);
        if ("request_close".equals(method)) {
            if (!prefs.getBoolean("panel-active",false) || !allowedAddon(caller,prefs)) throw new SecurityException("not an enabled widget");
            synchronized (PanelConfigProvider.class) {long now=SystemClock.elapsedRealtime();if(now-closeAt>=1000)closeAt=now;}
            return new Bundle();
        }
        if (caller != Process.SYSTEM_UID && caller != Process.myUid()) throw new SecurityException("system only");
        if (!"snapshot".equals(method)) throw new IllegalArgumentException("unknown method");
        Set<String> enabled = prefs.getStringSet("enabled", new HashSet<String>());
        ArrayList<String> selected = new ArrayList<String>();
        for (String key : prefs.getString("order", "").split(",")) {
            if (enabled.contains(key) && !selected.contains(key)) selected.add(key);
        }
        Bundle result = new Bundle();
        result.putBoolean("active", prefs.getBoolean("panel-active", false));
        result.putStringArrayList("selected", selected);
        synchronized (PanelConfigProvider.class) { result.putLong("close_at",closeAt); }
        return result;
    }
    private boolean allowedAddon(int uid,SharedPreferences prefs) {
        if(uid==Process.SYSTEM_UID||uid==Process.myUid())return true;
        Set<String> enabled=prefs.getStringSet("enabled",new HashSet<String>());
        for(String key:prefs.getString("order","").split(",")){
            if(!enabled.contains(key))continue;int delimiter=key.indexOf('#');if(delimiter<=0)continue;
            ComponentName component=ComponentName.unflattenFromString(key.substring(0,delimiter));if(component==null)continue;
            try { if(getContext().getPackageManager().getApplicationInfo(component.getPackageName(),0).uid==uid)return true; }
            catch(PackageManager.NameNotFoundException ignored) { }
        }
        return false;
    }
    @Override public Cursor query(Uri u,String[] p,String s,String[] a,String o){throw new UnsupportedOperationException();}
    @Override public String getType(Uri u){return null;}
    @Override public Uri insert(Uri u,ContentValues v){throw new UnsupportedOperationException();}
    @Override public int delete(Uri u,String s,String[] a){throw new UnsupportedOperationException();}
    @Override public int update(Uri u,ContentValues v,String s,String[] a){throw new UnsupportedOperationException();}
}
