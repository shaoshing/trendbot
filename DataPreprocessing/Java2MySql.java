/* Authors : Bhavik Bhuta
 *           Sayali Warule
 */

import java.sql.Connection;

import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Hashtable; 
import java.awt.List;
import java.io.BufferedReader;
import java.io.FileReader;

 
public class Java2MySql
{
    public static void main(String[] args) 
    {
          String url = "jdbc:mysql://localhost:3306/";
          String dbName = "trendbot";
          String driver = "com.mysql.jdbc.Driver";
          String userName = "root";
          String password = "sayali123";
          int index=0;
          String date;
          Hashtable<String, Integer> trend = new Hashtable<String,Integer>();
          
          try 
          {
        	  
        	  BufferedReader reader = new BufferedReader(new FileReader("C:\\Users\\User\\Desktop\\GoogleTrendRanking.txt"));
              String line = null;
              while ((line = reader.readLine()) != null)
              {
                  
                  if (!line.matches("Complete"))
                  {
                      index = line.indexOf("#");
                      if (index==0)
                      {
                          date = line.substring(1,9);
                          //month = line.substring(5,7);
                          //day = line.substring(7,9);
                          //System.out.println("Date is -------------- : " + date);
                          
                      }
                      else
                      {
                          String[] lines = line.split(": ");
                          String trend_str = lines[1];
                          trend_str = trend_str.replaceAll(" ","_");
                          if(!trend.containsKey(trend_str))
                          {
                        	  trend.put(trend_str, 0);
                          }
                          
                      }
                  }
              }
                      
                  Class.forName(driver).newInstance();
            	  Connection conn = DriverManager.getConnection(url+dbName,userName,password);

              
                  for (int j=1;j<=28;j++)
                  {
                	  //if (!(j==4 || j==6 ||j==7 || j==9 || j==10 || j==11 ||j==12))
                	  //{
                	  for (String key : trend.keySet()) 
                	  {
                    	  PreparedStatement stmt = conn.prepareStatement("select pagetitle, sum(pageviewcount) as sum from pagecount_"+j+"_feb where pagetitle = ?");
                    	  stmt.setString(1, key);
                    	  ResultSet rs = stmt.executeQuery();
                		  while(rs.next())
                    	  {
                			 String title = rs.getString("pagetitle");
                			 int count = rs.getInt("sum");
                			 PreparedStatement stmt1 = conn.prepareStatement("insert into final_pagecount values(?,?,?)");
                             stmt1.setString(1, key);
                             stmt1.setInt(2, count);
                             if(j<10)
                            	 stmt1.setString(3, "2014-02-0"+j);
                             else
                            	 stmt1.setString(3, "2014-02-"+j);
                             stmt1.executeUpdate();
                		  }
                      }
              		  System.out.println("Done for feb : " + j);
                	  //}
                	  
                  }
                  
                  conn.close();
              
          }
          catch (Exception e) 
          {
          	e.printStackTrace();
          }
    }
}