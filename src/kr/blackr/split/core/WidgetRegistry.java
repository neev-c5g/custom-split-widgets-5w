package kr.blackr.split.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Ordered widget selection, independent of Android and stock split-screen settings. */
public final class WidgetRegistry {
    public static final class Widget {
        public final String id, title;
        public Widget(String id, String title) {
            // Built-in IDs and component/widget keys; commas are reserved for persistence.
            if (id == null || !id.matches("[A-Za-z][A-Za-z0-9_.$/#-]{0,239}") || title == null || title.trim().isEmpty())
                throw new IllegalArgumentException("invalid widget");
            this.id=id; this.title=title.trim();
        }
    }
    private final Map<String,Widget> available=new LinkedHashMap<String,Widget>();
    private final List<String> order=new ArrayList<String>();
    private final Set<String> enabled=new LinkedHashSet<String>();
    public WidgetRegistry(List<Widget> widgets,List<String> savedOrder,Set<String> savedEnabled) {
        if (widgets==null) throw new IllegalArgumentException("widgets");
        for (Widget w:widgets) {
            if(w==null||available.put(w.id,w)!=null)throw new IllegalArgumentException("duplicate widget");
        }
        if(savedOrder!=null) for(String id:savedOrder) if(available.containsKey(id)&&!order.contains(id))order.add(id);
        for(String id:available.keySet())if(!order.contains(id))order.add(id);
        if(savedEnabled!=null)for(String id:savedEnabled)if(available.containsKey(id))enabled.add(id);
    }
    public List<Widget> all() {List<Widget> result=new ArrayList<Widget>();for(String id:order)result.add(available.get(id));return Collections.unmodifiableList(result);}
    public List<Widget> active() {List<Widget> result=new ArrayList<Widget>();for(String id:order)if(enabled.contains(id))result.add(available.get(id));return Collections.unmodifiableList(result);}
    public boolean isEnabled(String id){return enabled.contains(id);}
    public void setEnabled(String id,boolean value){require(id);if(value)enabled.add(id);else enabled.remove(id);}
    public void move(String id,int position){require(id);if(position<0||position>=order.size())throw new IndexOutOfBoundsException();order.remove(id);order.add(position,id);}
    public List<String> savedOrder(){return Collections.unmodifiableList(new ArrayList<String>(order));}
    public Set<String> savedEnabled(){return Collections.unmodifiableSet(new LinkedHashSet<String>(enabled));}
    private void require(String id){if(!available.containsKey(id))throw new IllegalArgumentException("unknown widget");}
}
