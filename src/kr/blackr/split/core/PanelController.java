package kr.blackr.split.core;

import java.util.ArrayList;
import java.util.List;

/** UI-independent state for a single-pointer split-panel gesture sequence. */
public final class PanelController {
    public enum Effect { NONE, OPENED, CLOSED, PAGE_CHANGED }
    private final float thresholdPx, dominance;
    private List<String> pages=new ArrayList<String>();
    private String selected;
    private boolean enabled,open,tracking,cancelled;
    private float x,y;
    public PanelController(float thresholdPx,float dominance) {
        if (!(thresholdPx>0) || !(dominance>=1) || Float.isInfinite(thresholdPx) || Float.isInfinite(dominance))
            throw new IllegalArgumentException("invalid gesture parameters");
        this.thresholdPx=thresholdPx;this.dominance=dominance;
    }
    /** Disabling always drops the custom panel; a stock restore is the caller's responsibility. */
    public void setEnabled(boolean value) {enabled=value;if(!value){open=false;tracking=false;}}
    public boolean enabled(){return enabled;}
    public boolean open(){return open;}
    public String selected(){return selected;}
    public void setPages(List<String> ids) {
        if(ids==null)throw new IllegalArgumentException("pages");
        ArrayList<String> next=new ArrayList<String>();
        for(String id:ids) {if(id==null||id.isEmpty()||next.contains(id))throw new IllegalArgumentException("invalid page");next.add(id);}
        int old=selected==null?-1:pages.indexOf(selected);
        pages=next;
        if(!pages.contains(selected))selected=pages.isEmpty()?null:pages.get(Math.min(Math.max(old,0),pages.size()-1));
        if(pages.isEmpty()){open=false;tracking=false;}
    }
    public int index(){return selected==null?-1:pages.indexOf(selected);}
    public int count(){return pages.size();}
    public boolean down(float x,float y,boolean onHandle){
        if(!enabled||pages.isEmpty()||(!open&&!onHandle)||!finite(x)||!finite(y))return false;
        this.x=x;this.y=y;tracking=true;cancelled=false;return true;
    }
    public void cancel(){cancelled=true;tracking=false;}
    public Effect up(float x,float y){
        if(!tracking||cancelled||!finite(x)||!finite(y)){tracking=false;return Effect.NONE;}
        tracking=false;float dx=x-this.x,dy=y-this.y;
        if(Math.abs(dx)>=thresholdPx&&Math.abs(dx)>Math.abs(dy)*dominance){
            if(!open&&dx<0){open=true;return Effect.OPENED;}
            if(open&&dx>0){open=false;return Effect.CLOSED;}
        } else if(open&&Math.abs(dy)>=thresholdPx&&Math.abs(dy)>Math.abs(dx)*dominance){
            int old=index(),next=old+(dy<0?1:-1);
            if(next>=0&&next<pages.size()){selected=pages.get(next);return Effect.PAGE_CHANGED;}
        }
        return Effect.NONE;
    }
    private static boolean finite(float n){return !Float.isNaN(n)&&!Float.isInfinite(n);}
}
