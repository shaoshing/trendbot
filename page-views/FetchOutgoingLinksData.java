import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import iub.trendbot.graph.Graph;
import iub.trendbot.graph.Page;

import java.util.ArrayList;

/**
 * Created by Bhavik on 4/23/14.
 * Modified by Sayali on 4/27/14
 */
public class PageCount {
	private static void printPages(ArrayList<Page> pages){
        for(Page page : pages){
            System.out.printf("%d, %s\n", page.id, page.title);
        }
    }
	
	private static void printPagesAndCategories(ArrayList<Page> pages){
        for(Page page : pages){
            System.out.printf("%d, %s -> %d, %s\n", page.id, page.title, page.category_id, page.category_title);
        }
    }
	public static void pgcount(Connection conn, Date date, String title,String Keyword, String type,String monthstr,Integer subtable, String linebleh) throws SQLException, IOException
	{

		PreparedStatement stmt = conn.prepareStatement("select * from pagecount_"+monthstr+"_"+subtable+" where pagetitle = ? and datetime between DATE_SUB(?,INTERVAL 1 DAY) and DATE_ADD(?,INTERVAL 2 DAY)");
		stmt.setString(1, title);
		stmt.setDate(2, date);
		stmt.setDate(3, date);
  	    ResultSet rs = stmt.executeQuery();
  	    int count = 0, hr=0;

  	    int pageviewcount;
  	    String pagetitle;
  	    
  	    if(rs.next())
  	    {
  	    	do
  	    	{
  	    	pagetitle = rs.getString("pagetitle");
  	    	pageviewcount = rs.getInt("pageviewcount");
  	    	java.sql.Timestamp datetime = rs.getTimestamp("datetime");
  	    
  	    	PreparedStatement stmt1 = conn.prepareStatement("insert into trends1 values(?,?,?,?,?,?,?)");
  	    	stmt1.setString(1,linebleh);
            stmt1.setString(2, Keyword);
            stmt1.setString(3, pagetitle);
            stmt1.setDate(4,date);
            stmt1.setString(5,type);
            stmt1.setInt(6,pageviewcount);
            stmt1.setTimestamp(7,datetime);
            
            stmt1.executeUpdate();
  	    	} while (rs.next());
  	    }
  	    else
  	    {
  	    	PreparedStatement stmt2 = conn.prepareStatement("insert into trends1 values(?,?,?,?,?,0,null)");
  	    	stmt2.setString(1,linebleh);
            stmt2.setString(2, Keyword);
            stmt2.setString(3, title);
            stmt2.setDate(4,date);
            stmt2.setString(5,type);
            
            stmt2.executeUpdate();

  	    }
  	    	
	}
	public static void main(String[] args) 
    {
		  BufferedWriter br;
		  StringBuilder sb = new StringBuilder();
          String url = "jdbc:mysql://localhost:3306/";
          String dbName = "trendbot";
          String driver = "com.mysql.jdbc.Driver";
          String userName = "root";
          String password = "sayali123";
          Graph graph = new Graph ("http://localhost:7474/db/data/");
          ArrayList<Page> pages, incoming, outgoing, innout, lvl1, lvl2, lvl3, lvl12, lvl23, lvl123;
          String filename;
          Date date;
          boolean i=true;
          try 
          {
        	  
        	  BufferedReader reader = new BufferedReader(new FileReader("E:\\Trendbot\\regressionGT.txt"));
              String line = null, linebleh;
              Class.forName(driver).newInstance();
        	  Connection conn = DriverManager.getConnection(url+dbName,userName,password);
        	  System.out.println("Start of the program: ");
        	  
        	  while((line = reader.readLine())!=null)
        	  {
        		  linebleh = line.replace(' ', '_');
        		  System.out.println("LINE:"+ line);
     			  
        		  int maxcount = 0,subtable = 0;
        		  String month;
        		  String monthstr=null;
        		  
        		  for (int x=0; x<2; x++)
        		  {
        			  if (x == 0)
        			  {
        				  month = "jan";
        			  }
        			  else
        			  {
        				  month = "feb";  
        			  }
        			  
        				  for (int y=1; y<3; y++)
        				  {
        					  PreparedStatement peak = conn.prepareStatement("select max(pageviewcount) as maximum from pagecount_"+month+"_"+y+" where pagetitle=?"); 
        					  peak.setString(1, linebleh);
        					  ResultSet pk= peak.executeQuery();
        					  if (pk.next())
        					  {
        					  	  int max = pk.getInt("maximum");
        					  	  if (max > maxcount)
        					  	  {
        					  		  maxcount = max;
        					  		  monthstr = month;
        					  		  subtable = y;        	     				  
        					  	  }
        					  }  
        				  }
        		  }
        		  
        		  if(monthstr != null)
        		  {
        			  String type;
        			  PreparedStatement stmt = conn.prepareStatement("select datetime from pagecount_"+monthstr+"_"+subtable+" where pagetitle=? and pageviewcount="+maxcount+"");
        			  stmt.setString(1, linebleh);
        			  ResultSet rs = stmt.executeQuery();
        			  if(rs.next())
        			  {
     			 		  date = rs.getDate("datetime");
        				  pages = graph.searchPages(line);
       	   				  
        				  /********************************* Outgoing Pages *********************************/
        				  for(Page page: pages)
        				  {
        					  	type = "outgoing";
	              	         	outgoing = graph.getOutgoingPages(page.title);
	              	         	for(Page out: outgoing)
	              	         	{
	              	         		pgcount(conn, date, out.title,page.title, type, monthstr, subtable,linebleh);
	              	         	}
        				  } 
        				  
        				  /********************************* Incoming Pages *********************************
        				  for(Page page: pages)
        				  {
        					  	type = "incoming";
	              	         	incoming = graph.getIncomingPages(page.title);
	              	         	for(Page in: incoming)
	              	         	{
	              	         		pgcount(conn, date, in.title,page.title, type, monthstr, subtable,linebleh);
	              	         	}
        				  } 
        				  
        				  /********************************* InOut Pages *********************************
        				  
        				 /* for(Page page: pages)
        				  {
        					  	type = "innout";
        					  	innout = graph.getIncomingPages(page.title);
        		            	innout.addAll(graph.getOutgoingPages(page.title));
	              	         	for(Page io: innout)
	              	         	{
	              	         		pgcount(conn, date, io.title,page.title, type, monthstr, subtable,linebleh);
	              	         	}
        				  }
        				  */

        	              
        	              /********************************* lvl1 Pages *********************************

        	              for(Page page: pages)
        	              {
        	            	  type = "lvl1";
        	            	  lvl1 = graph.getCategoryPages(page.title, 1);
        	            	  for(Page l1: lvl1)
        	            	  {
        	            		  pgcount(conn, date, l1.title,page.title, type, monthstr, subtable,linebleh);
        	            	  }	
        	              }
        	              
        	              /********************************* lvl2 Pages *********************************

        	              for(Page page: pages)
        	              {
        	            	  type = "lvl2";

        	            	  lvl2 = graph.getCategoryPages(page.title, 2);
        	            	  for(Page l2: lvl2)
        	            	  {
        	            		  pgcount(conn, date, l2.title,page.title, type, monthstr, subtable,linebleh);
        	            	  }	
        	            	  i = false;
        	              }
        	              i = true;
        	              
        	              /********************************* lvl3 Pages *********************************

        	              for(Page page: pages)
        	              {
        	            	  type = "lvl3";
        	            	  lvl3 = graph.getCategoryPages(page.title, 3);
        	            	  for(Page l3: lvl3)
        	            	  {
        	            		  pgcount(conn, date, l3.title,page.title, type, monthstr, subtable,linebleh);
        	            	  }	
        	              }
                      
        	              /********************************* lv12 Pages *********************************/

        	            /*  for(Page page: pages)
        	              {
        	            	  type = "lvl12";
        	            	  lvl12 = graph.getCategoryPages(page.title, 1);
        	            	  lvl12.addAll(graph.getCategoryPages(page.title, 2));
        	            	  for(Page l12: lvl12)
        	            	  {
        	            		  pgcount(conn, date, l12.title,page.title, type, monthstr, subtable,linebleh);
        	            	  }	
        	              }
        	              */
        	              /********************************* lv23 Pages *********************************/
        	              /*
        	              for(Page page: pages)
        	              {
        	            	  type = "lvl23";
        	            	  lvl23 = graph.getCategoryPages(page.title, 2);
        	            	  lvl23.addAll(graph.getCategoryPages(page.title, 3));
        	            	  for(Page l23: lvl23)
        	            	  {
        	            		  pgcount(conn, date, l23.title,page.title, type, monthstr, subtable,linebleh);
        	            	  }	
        	              }
                      	*/
        	              /********************************* lvl123 Pages *********************************/
        	              /*
        	              for(Page page: pages)
        	              {
        	            	  type = "lvl123";
        	            	  lvl123 = graph.getCategoryPages(page.title, 1);
        	            	  lvl123.addAll(graph.getCategoryPages(page.title, 2));
        	            	  lvl123.addAll(graph.getCategoryPages(page.title, 3));
        	            	  for(Page l123: lvl123)
        	            	  {
        	            		  pgcount(conn, date, l123.title,page.title, type, monthstr, subtable,linebleh);
        	            	  }	
        	              }
        	              */
        			  }
        		  }
            	  System.out.println("Done For"+linebleh);

        	  }
        	  System.out.println("Done");
	          reader.close();
              conn.close();
          }
          catch(Exception e)
          {
        	  e.printStackTrace();
          }
    }
}
