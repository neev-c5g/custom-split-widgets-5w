package kr.blackr.split.core;
import java.util.*;
public final class WidgetRegistryTest {
 static void check(boolean ok){if(!ok)throw new AssertionError();}
 public static void main(String[] args){
  List<WidgetRegistry.Widget> installed=Arrays.asList(new WidgetRegistry.Widget("taximeter","미터기"),new WidgetRegistry.Widget("clock","시계"),new WidgetRegistry.Widget("media","미디어"));
  WidgetRegistry r=new WidgetRegistry(installed,Arrays.asList("missing","media","media","taximeter"),new HashSet<String>(Arrays.asList("taximeter","missing")));
  check(r.savedOrder().equals(Arrays.asList("media","taximeter","clock")));check(r.active().size()==1&&r.active().get(0).id.equals("taximeter"));
  r.setEnabled("clock",true);r.move("clock",0);check(r.active().get(0).id.equals("clock"));r.setEnabled("taximeter",false);check(r.active().size()==1);
  boolean failed=false;try{r.setEnabled("missing",true);}catch(IllegalArgumentException expected){failed=true;}check(failed);
  failed=false;try{new WidgetRegistry(Arrays.asList(installed.get(0),installed.get(0)),null,null);}catch(IllegalArgumentException expected){failed=true;}check(failed);
  System.out.println("WidgetRegistryTest passed");
 }
}
