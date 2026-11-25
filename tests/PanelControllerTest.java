package kr.blackr.split.core;
import java.util.*;
public final class PanelControllerTest {
 static void check(boolean x){if(!x)throw new AssertionError();}
 public static void main(String[] args){
  PanelController p=new PanelController(24,1.5f);p.setPages(Arrays.asList("meter","clock","media"));
  check(!p.down(100,100,true));p.setEnabled(true);check(!p.down(100,100,false));
  check(p.down(100,100,true));check(p.up(95,100)==PanelController.Effect.NONE&&!p.open());
  p.down(100,100,true);check(p.up(60,100)==PanelController.Effect.OPENED&&p.open());
  p.down(100,100,false);check(p.up(100,60)==PanelController.Effect.PAGE_CHANGED&&p.selected().equals("clock"));
  p.setPages(Arrays.asList("clock","meter"));check(p.selected().equals("clock")&&p.index()==0);
  p.down(100,100,false);check(p.up(100,145)==PanelController.Effect.NONE&&p.index()==0);
  p.down(100,100,false);p.cancel();check(p.up(150,100)==PanelController.Effect.NONE&&p.open());
  p.down(100,100,false);check(p.up(145,100)==PanelController.Effect.CLOSED&&!p.open());
  p.down(100,100,true);p.up(60,100);p.setEnabled(false);check(!p.open()&&!p.enabled());
  p.setEnabled(true);p.setPages(Collections.<String>emptyList());check(!p.down(1,1,true)&&p.selected()==null);
  boolean fail=false;try{p.setPages(Arrays.asList("x","x"));}catch(IllegalArgumentException expected){fail=true;}check(fail);
  System.out.println("PanelControllerTest passed");
 }
}
