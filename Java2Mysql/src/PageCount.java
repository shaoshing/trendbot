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
 * Created by Bhavik on 4/25/14.
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
	public static void pgcount(Connection conn, Date date, String title, String filename) throws SQLException, IOException
	{
		BufferedWriter br = new BufferedWriter(new FileWriter(filename, true));
		StringBuilder sb = new StringBuilder();
		PreparedStatement stmt = conn.prepareStatement("select DISTINCT * from pagecount_feb_2 where pagetitle = ? COLLATE utf8_bin and datetime between DATE_SUB(?,INTERVAL 1 DAY) and DATE_ADD(?,INTERVAL 2 DAY)");
		stmt.setString(1, title);
		stmt.setDate(2, date);
		stmt.setDate(3, date);
  	    ResultSet rs = stmt.executeQuery();
  	    int count = 0, hr=0;
  	    sb.append("\""+title+"\",");
  	    while(rs.next())
  	    {
  	    	//System.out.println("Hours: " + rs.getTime("datetime").getHours()+ "\t" + (hr%24));
  	    	/*while(rs != null && rs.getTime("datetime").getHours() != (hr%24))
  	    	{
  	    		//System.out.println("Appending 0");
  	    		sb.append("0,");
  	    		hr++;
  	    	}*/
  	    	count+=rs.getInt("pageviewcount");
  	    	sb.append(rs.getInt("pageviewcount"));
  	    	sb.append(",");
  	    	hr++;
  	    }
  	    sb.append("Total:"+count);
  	    br.write(sb.toString());
  	  br.write("\n");
  	  br.close();
	}
	public static void main(String[] args) 
    {
		  BufferedWriter br;
		  StringBuilder sb = new StringBuilder();
          String url = "jdbc:mysql://localhost:3306/";
          String dbName = "trendbot";
          String driver = "com.mysql.jdbc.Driver";
          String userName = "root";
          String password = "root";
          Graph graph = new Graph("http://localhost:7474/db/data/");
          ArrayList<Page> pages, incoming, outgoing, innout, lvl1, lvl2, lvl3, lvl12, lvl23, lvl123;
          String filename;
          Date date;
          boolean i=true;
          try 
          {
        	  
        	  BufferedReader reader = new BufferedReader(new FileReader("C:\\Users\\Bhavik\\Desktop\\GT.txt"));
              String line = null, linebleh;
              Class.forName(driver).newInstance();
        	  Connection conn = DriverManager.getConnection(url+dbName,userName,password);
        	  System.out.println("aaya");
        	  
        	  while((line = reader.readLine())!=null)
        	  {
        		  linebleh = line.replace(' ', '_');
        		  System.out.println("LINE:"+ line);
     			  PreparedStatement stmt = conn.prepareStatement("select datetime from pagecount_feb_2 where pagetitle=? and pageviewcount=(select max(pageviewcount) from pagecount_feb_2 where pagetitle=?)");
	        	  stmt.setString(1, linebleh);
	        	  stmt.setString(2, linebleh);
     			  ResultSet rs = stmt.executeQuery();
     			  rs.next();
        		  date = rs.getDate("datetime");
        		  //System.out.println("Date:" + date);//"2014-02-17";//reader.readLine();
	        	  pages = graph.searchPages(line);
	        	  //printPages(pages);
	        	  System.out.println("incoming pages");
	              for(Page page: pages)//while ((line = reader.readLine()) != null)
	              {
	              	  filename = "C:\\Users\\Bhavik\\Desktop\\Proj\\incoming.csv";
	              	  if(i)
	            	  {
		            	  br = new BufferedWriter(new FileWriter(filename, true));
		            	  br.write(line+"--->");
		            	  br.write("\n");
		            	  br.close();
	            	  }
	            	  incoming = graph.getIncomingPages(page.title);
	            	  //System.out.println("Count: "+incoming.size());
	            	  for(Page in: incoming)
	            	  {
	            		  pgcount(conn, date, in.title, filename);
	            	  }
	            	  i = false;
	              }
	              i = true;
	              System.out.println("outgoing");
	              for(Page page: pages)//while ((line = reader.readLine()) != null)
	              {
	            	  filename = "C:\\Users\\Bhavik\\Desktop\\Proj\\outgoing.csv";
	            	  if(i)
	            	  {
		            	  br = new BufferedWriter(new FileWriter(filename, true));
		            	  br.write(line+"--->");
		            	  br.write("\n");
		            	  br.close();
	            	  }
	            	  outgoing = graph.getOutgoingPages(page.title);
	            	  //System.out.println("Count: "+outgoing.size());
	            	  //printPages(outgoing);
	            	  for(Page out: outgoing)
	            	  {
	            		  pgcount(conn, date, out.title, filename);
	            	  }
	            	  i = false;
	              }
	              i = true;
	              System.out.println("innout");
	              for(Page page: pages)//while ((line = reader.readLine()) != null)
	              {
	            	  filename = "C:\\Users\\Bhavik\\Desktop\\Proj\\innout.csv";
	            	  if(i)
	            	  {
		            	  br = new BufferedWriter(new FileWriter(filename, true));
		            	  br.write(line+"--->");
		            	  br.write("\n");
		            	  br.close();
	            	  }
	            	  innout = graph.getIncomingPages(page.title);
	            	  innout.addAll(graph.getOutgoingPages(page.title));
	            	  //printPages(innout);
	            	  for(Page io: innout)
	            	  {
	            		  pgcount(conn, date, io.title, filename);
	            	  }	
	            	  i = false;
	              }
	              i = true;
	              System.out.println("lvl1");
	              for(Page page: pages)//while ((line = reader.readLine()) != null)
	              {
	            	  filename = "C:\\Users\\Bhavik\\Desktop\\Proj\\lvl1.csv";
	            	  if(i)
	            	  {
		            	  br = new BufferedWriter(new FileWriter(filename, true));
		            	  br.write(line+"--->");
		            	  br.write("\n");
		            	  br.close();
	            	  }
	            	  lvl1 = graph.getCategoryPages(page.title, 1);
	            	  //printPagesAndCategories(lvl1);
	            	  for(Page l1: lvl1)
	            	  {
	            		  pgcount(conn, date, l1.title, filename);
	            	  }	
	            	  i = false;
	              }
	              i = true;
	              System.out.println("lv2");
	              for(Page page: pages)//while ((line = reader.readLine()) != null)
	              {
	            	  filename = "C:\\Users\\Bhavik\\Desktop\\Proj\\lvl2.csv";
	            	  if(i)
	            	  {
		            	  br = new BufferedWriter(new FileWriter(filename, true));
		            	  br.write(line+"--->");
		            	  br.write("\n");
		            	  br.close();
	            	  }
	            	  lvl2 = graph.getCategoryPages(page.title, 2);
	            	  //printPagesAndCategories(lvl2);
	            	  for(Page l2: lvl2)
	            	  {
	            		  pgcount(conn, date, l2.title, filename);
	            	  }	
	            	  i = false;
	              }
	              i = true;
	              System.out.println("lvl3");
	              for(Page page: pages)//while ((line = reader.readLine()) != null)
	              {
	            	  filename = "C:\\Users\\Bhavik\\Desktop\\Proj\\lvl3.csv";
	            	  if(i)
	            	  {
		            	  br = new BufferedWriter(new FileWriter(filename, true));
		            	  br.write(line+"--->");
		            	  br.write("\n");
		            	  br.close();
	            	  }
	            	  lvl3 = graph.getCategoryPages(page.title, 3);
	            	  //printPagesAndCategories(lvl3);
	            	  for(Page l3: lvl3)
	            	  {
	            		  pgcount(conn, date, l3.title, filename);
	            	  }	
	            	  i = false;
	              }
	              i = true;
	              System.out.println("lvl12");
	              for(Page page: pages)//while ((line = reader.readLine()) != null)
	              {
	            	  filename = "C:\\Users\\Bhavik\\Desktop\\Proj\\lvl12.csv";
	            	  if(i)
	            	  {
		            	  br = new BufferedWriter(new FileWriter(filename, true));
		            	  br.write(line+"--->");
		            	  br.write("\n");
		            	  br.close();
	            	  }
	            	  lvl12 = graph.getCategoryPages(page.title, 1);
	            	  lvl12.addAll(graph.getCategoryPages(page.title, 2));
	            	  //printPagesAndCategories(lvl12);
	            	  for(Page l12: lvl12)
	            	  {
	            		  pgcount(conn, date, l12.title, filename);
	            	  }	
	            	  i = false;
	              }
	              i = true;
	              System.out.println("lv23");
	              for(Page page: pages)//while ((line = reader.readLine()) != null)
	              {
	            	  filename = "C:\\Users\\Bhavik\\Desktop\\Proj\\lvl23.csv";
	            	  if(i)
	            	  {
		            	  br = new BufferedWriter(new FileWriter(filename, true));
		            	  br.write(line+"--->");
		            	  br.write("\n");
		            	  br.close();
	            	  }
	            	  lvl23 = graph.getCategoryPages(page.title, 2);
	            	  lvl23.addAll(graph.getCategoryPages(page.title, 3));
	            	  //printPagesAndCategories(lvl23);
	            	  for(Page l23: lvl23)
	            	  {
	            		  pgcount(conn, date, l23.title, filename);
	            	  }	
	            	  i = false;
	              }
	              i = true;
	              System.out.println("lvl123");
	              for(Page page: pages)//while ((line = reader.readLine()) != null)
	              {
	            	  filename = "C:\\Users\\Bhavik\\Desktop\\Proj\\lvl123.csv";
	            	  if(i)
	            	  {
		            	  br = new BufferedWriter(new FileWriter(filename, true));
		            	  br.write(line+"--->");
		            	  br.write("\n");
		            	  br.close();
	            	  }
	            	  lvl123 = graph.getCategoryPages(page.title, 1);
	            	  //printPagesAndCategories(lvl123);
	            	  System.out.println();
	            	  lvl123.addAll(graph.getCategoryPages(page.title, 2));
	            	  //printPagesAndCategories(lvl123);
	            	  //System.out.println();
	            	  lvl123.addAll(graph.getCategoryPages(page.title, 3));
	            	  //printPagesAndCategories(lvl123);
	            	  for(Page l123: lvl123)
	            	  {
	            		  pgcount(conn, date, l123.title, filename);
	            	  }	
	            	  i = false;
	              }
	              i = true;
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
