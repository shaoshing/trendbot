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
 */
public class Page_Count {
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
		  BufferedWriter br = null;
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
        	  
        	  BufferedReader reader = new BufferedReader(new FileReader("C:\\Users\\Bhavik\\Desktop\\GoogleTrend.txt"));
              String line = null, linebleh;
              Class.forName(driver).newInstance();
        	  Connection conn = DriverManager.getConnection(url+dbName,userName,password);
        	  System.out.println("aaya");
        	  
        	  while((line = reader.readLine())!=null)
        	  {
        		  line = line.substring(1);
        		  System.out.println("LINE:"+ line);
     			  //PreparedStatement stmt = conn.prepareStatement("select datetime from pagecount_feb_2 where pagetitle=? and pageviewcount=(select max(pageviewcount) from pagecount_feb_2 where pagetitle=?)");
	        	  //stmt.setString(1, linebleh);
	        	  //stmt.setString(2, linebleh);
     			  //ResultSet rs = stmt.executeQuery();
     			  //rs.next();
        		  //date = rs.getDate("datetime");
        		  //System.out.println("Date:" + date);//"2014-02-17";//reader.readLine();
	        	  pages = graph.searchPages(line);
	        	  //printPages(pages);
	        	  System.out.println("incoming pages");
	              for(Page page: pages)//while ((line = reader.readLine()) != null)
	              {
	              	  filename = "C:\\Users\\Bhavik\\Desktop\\Proj\\incoming.txt";
	              	  if(i)
	            	  {
		            	  br = new BufferedWriter(new FileWriter(filename, true));
		            	  br.write(line+"--->");
		            	  br.newLine();
	            	  }
	              	
	            	  incoming = graph.getIncomingPages(page.title);
	            	  //System.out.println("Count: "+incoming.size());
	            	  for(Page in: incoming)
	            	  {
	            		  br.write(in.title);
	            		  br.newLine();
	            	  }
	            	  i = false;
	            	  
	              }
	              br.close();
	              i = true;
	              System.out.println("outgoing");
	              for(Page page: pages)//while ((line = reader.readLine()) != null)
	              {
	            	  filename = "C:\\Users\\Bhavik\\Desktop\\Proj\\outgoing.txt";
	            	  if(i)
	            	  {
		            	  br = new BufferedWriter(new FileWriter(filename, true));
		            	  br.write(line+"--->");
		            	  br.newLine();
		            	  //br.close();
	            	  }
	            	  
	            	  outgoing = graph.getOutgoingPages(page.title);
	            	  //System.out.println("Count: "+outgoing.size());
	            	  //printPages(outgoing);
	            	  for(Page out: outgoing)
	            	  {
	            		  br.write(out.title);
	            		  br.newLine();
	            	  }
	            	  i = false;
	              }
	              br.close();
	              i = true;
	              System.out.println("innout");
	              for(Page page: pages)//while ((line = reader.readLine()) != null)
	              {
	            	  filename = "C:\\Users\\Bhavik\\Desktop\\Proj\\innout.txt";
	            	  if(i)
	            	  {
		            	  br = new BufferedWriter(new FileWriter(filename, true));
		            	  br.write(line+"--->");
		            	  br.newLine();
		            	  //br.close();
	            	  }
	            	  br.newLine();
	            	  innout = graph.getIncomingPages(page.title);
	            	  innout.addAll(graph.getOutgoingPages(page.title));
	            	  //printPages(innout);
	            	  for(Page io: innout)
	            	  {
	            		  br.write(io.title);
	            		  br.newLine();
	            	  }	
	            	  i = false;
	            	 
	              }
	              br.close();
	              i = true;
	              System.out.println("lv2");
	              for(Page page: pages)//while ((line = reader.readLine()) != null)
	              {
	            	  filename = "C:\\Users\\Bhavik\\Desktop\\Proj\\lvl2.txt";
	            	  if(i)
	            	  {
		            	  br = new BufferedWriter(new FileWriter(filename, true));
		            	  br.write(line+"--->");
		            	  br.newLine();
		            	 //br.close();
	            	  }
	            	  
	            	  lvl2 = graph.getCategoryPages(page.title, 2);
	            	  //printPagesAndCategories(lvl2);
	            	  for(Page l2: lvl2)
	            	  {
	            		  br.write(l2.title);	  
	            		  br.newLine();
	            	  }	
	            	  i = false;
	              }
	              br.close();
	              i = true;
	              System.out.println("lvl3");
	              for(Page page: pages)//while ((line = reader.readLine()) != null)
	              {
	            	  filename = "C:\\Users\\Bhavik\\Desktop\\Proj\\lvl3.txt";
	            	  if(i)
	            	  {
		            	  br = new BufferedWriter(new FileWriter(filename, true));
		            	  br.write(line+"--->");
		            	  br.newLine();
		            	  //br.close();
	            	  }
	            	  
	            	  lvl3 = graph.getCategoryPages(page.title, 3);
	            	  //printPagesAndCategories(lvl3);
	            	  for(Page l3: lvl3)
	            	  {
	            		  br.write(l3.title);	    
	            		  br.newLine();
	            	  }	
	            	  i = false;
	              }
	              br.close();
	              i = true;
	              System.out.println("lv23");
	              for(Page page: pages)//while ((line = reader.readLine()) != null)
	              {
	            	  filename = "C:\\Users\\Bhavik\\Desktop\\Proj\\lvl23.txt";
	            	  if(i)
	            	  {
		            	  br = new BufferedWriter(new FileWriter(filename, true));
		            	  br.write(line+"--->");
		            	  br.newLine();
	            	  }
            		  
	            	  lvl23 = graph.getCategoryPages(page.title, 2);
	            	  lvl23.addAll(graph.getCategoryPages(page.title, 3));
	            	  //printPagesAndCategories(lvl23);
	            	  for(Page l23: lvl23)
	            	  {
	            		  br.write(l23.title);	 
	            		  br.newLine();
	            	  }	
	            	  i = false;
	              }
	              br.close();
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
