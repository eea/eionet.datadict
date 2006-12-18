package eionet.meta;

import java.util.*;
import eionet.util.QueryString;
/**
 * A Class class.
 * <P>
 * @author Enriko Käsper
 */
public class DDhistory {

  private static String START_PAGE="index.jsp";
  private Vector history; //stores all the loaded urls
  private String backUrl = null;  //back button should use this url
  private String currentUrl = null;  //this url can be used to reload the page
  private int historySize=0;
  /**
   * Constructor
   */
  public DDhistory() {
      history = new Vector();
  }
  public void loadPage(String url){
      addUrl(url);
  }
  public String getBackUrl(){
      return backUrl;
  }
  public String getCurrentUrl(){
      return currentUrl;
  }
  public String getSize(){
      return Integer.toString(historySize);
  }
  public int getCurrentIndex(){
      return historySize-1;
  }
  public String toString(){
      return history.toString();
  }
  public String gotoLastMatching(String url){
      String s[] = {url};
      return gotoLastMatching(s);
  }
  public String gotoLastMatching(String[] url){
      gotoMatching(url, true);
      return currentUrl;
  }
  public String gotoLastNotMatching(String url){
      String s[] = {url};
      return gotoLastNotMatching(s);
  }
  public String gotoLastNotMatching(String[] url){
      gotoMatching(url, false);
      return currentUrl;
  }
  public String getLastMatching(String url){
      String s[] = {url};
      return getLastMatching(s);
  }
  public String getLastMatching(String[] url){
      return  getMatching(url, true);
  }
  public String getLastNotMatching(String url){
      String s[] = {url};
      return getLastNotMatching(s);
  }
  public String getLastNotMatching(String[] url){
      return getMatching(url, false);
  }
  public void remove(int index){
      removeUrl(index);
  }
  /* This method sets the loaded url as currentUrl, if it is not the same as last url in history
      If loaded url is the same as history(i-1), then the user clicked the back button.
  */
  private void addUrl(String url){

      if (historySize==0){
          history.add(url);
          historySize=1;
          currentUrl=url;
          backUrl="";
      }
      else{
          QueryString qs = new QueryString(url);
          if (qs.equals(currentUrl)) return;
          if (qs.equals(backUrl)){  //move back
              history.remove(historySize-1);
              currentUrl=backUrl;
              historySize--;
              backUrl = historySize>1 ? (String)history.get(historySize-2):"";
          }
          else if (!getLastMatching(url).equals(START_PAGE) && getLastMatching(url)!=null){
              gotoLastMatching(url);
          }
          else{ //move forward
              history.add(url);
              backUrl=currentUrl;
              currentUrl=url;
              historySize++;
          }

      }
  }
  /* finds last matching url or not matching from history and sets it as currentUrl
      If there are not any matches, then it cleans the history and currentUrl is start page
  */
  private void gotoMatching(String[] find_url, boolean matching){
      boolean found=false;
      if (historySize>0){
        for (int i=history.size();i>0;i--){
            currentUrl = (String)history.get(i-1);
            historySize=i;
            backUrl = historySize>1 ? (String)history.get(historySize-2):"";
            found=false;
            for(int j=0;j<find_url.length;j++){
                if (currentUrl.indexOf(find_url[j])>-1){
                  if (matching) return;
                  found=true;
                }
            }
            if (!matching && !found)
                return;
            history.remove(i-1);
        }
      }
      historySize=0;
      backUrl = "";
      currentUrl = START_PAGE;
  }
  /* finds last matching url or not matching from history
      If there are not any matches, then it returns start page
  */
  private String getMatching(String[] find_url, boolean matching){
      boolean found=false;
      String url="";

      if (historySize>0){
        for (int i=history.size();i>0;i--){
            url = (String)history.get(i-1);
            found=false;
            for(int j=0;j<find_url.length;j++){
                if (url.indexOf(find_url[j])>-1){
                  if (matching) return url;
                  found=true;
                }
            }
            if (!matching && !found)
                return url;
        }
      }
      return START_PAGE;
  }
  private void removeUrl(int index){
      if (historySize>index && index>=0){
          history.remove(index);
          currentUrl=backUrl;
          historySize--;
          backUrl = historySize>1 ? (String)history.get(historySize-2):"";
      }
  }
}

