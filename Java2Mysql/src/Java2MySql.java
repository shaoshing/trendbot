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

/**
 * Created by Bhavik on 3/12/14.
 */

 
public class Java2MySql
{
    public static void main(String[] args) 
    {
          String url = "jdbc:mysql://localhost:3306/";
          String dbName = "trendbot";
          String driver = "com.mysql.jdbc.Driver";
          String userName = "root";
          String password = "root";
          int index=0;
          String date;
          Hashtable<String, Integer> trend = new Hashtable<String,Integer>();
          
          try 
          {
        	  
        	  BufferedReader reader = new BufferedReader(new FileReader("C:\\Users\\Bhavik\\Desktop\\Pagename.txt"));
              String line = null;
              while ((line = reader.readLine()) != null)
              {
	              if(!trend.containsKey(line))
	              {
	            	  trend.put(line, 0);
	              }
                  
              }
                      
                  Class.forName(driver).newInstance();
            	  Connection conn = DriverManager.getConnection(url+dbName,userName,password);

              
                  for (int j=16;j<=31;j++)
                  {
                	  
                	  for (String key : trend.keySet()) 
                	  {
                    	  PreparedStatement stmt = conn.prepareStatement("select pagetitle, sum(pageviewcount) as sum from pagecount_"+j+"_jan where pagetitle = ?");
                    	  stmt.setString(1, key);
                    	  ResultSet rs = stmt.executeQuery();
                		  while(rs.next())
                    	  {
                			 String title = rs.getString("pagetitle");
                			 int count = rs.getInt("sum");
                			 PreparedStatement stmt1 = conn.prepareStatement("insert into final_pagecount1 values(?,?,?)");
                             stmt1.setString(1, key);
                             stmt1.setInt(2, count);
                             stmt1.setString(3, "2014-01-"+j);
                             stmt1.executeUpdate();
                		  }
                      }
              		  System.out.println("Done for jan : " + j);
                	  
                  }
                  
                  conn.close();
              
          }
          catch (Exception e) 
          {
          	e.printStackTrace();
          }
    }
}