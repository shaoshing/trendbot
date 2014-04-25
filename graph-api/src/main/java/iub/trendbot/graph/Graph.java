package iub.trendbot.graph;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by shaoshing on 4/15/14.
 */
public class Graph {

    private String neo4jURL;

    public Graph(String neo4jURL){
        this.neo4jURL = neo4jURL;
    }

    public ArrayList<Page> searchPages(String keyword){
        String cypher = String.format(
                "Match (k:Keyword {name: \"%s\"}) -[:MATCHES]-> page RETURN page.id, page.title",
                Client.escape(keyword));

        JSONObject result = client().query(cypher);
        return convertCypherResultToPages(result);
    }

    public ArrayList<Page> getOutgoingPages(String pageTitle){
        String cypher = String.format(
                "MATCH (p:Page {title: \"%s\"}) -[:OUTGOING]-> page RETURN page.id, page.title",
                Client.escape(pageTitle));

        JSONObject result = client().query(cypher);
        return convertCypherResultToPages(result);
    }

    public ArrayList<Page> getIncomingPages(String pageTitle){
        String cypher = String.format(
                "MATCH (p:Page {title: \"%s\"}) <-[:INCOMING]- page RETURN page.id, page.title",
                Client.escape(pageTitle));

        JSONObject result = client().query(cypher);
        return convertCypherResultToPages(result);
    }

    public ArrayList<Page> getCategoryPages(String pageTitle, int level){
        String cypher = "";

        if(level == 1){
            cypher = String.format(
                    "MATCH (p:Page {title: \"%s\"}) -[:L1_BELONGS_TO]-> (cc:Category) <-- (pp:Page), " +
                            "cc -[:BELONGS_TO_CATEGORY]-> (c:Category {level: 1}) " +
                            "RETURN pp.id, pp.title, c.id, c.title, c.level",
                    Client.escape(pageTitle));
        }else{
            cypher = String.format(
                    "MATCH (p:Page {title: \"%s\"}) -[:L1_BELONGS_TO]-> (c:Category {level: %d}) <-[:BELONGS_TO]- (pp:Page) " +
                            "RETURN DISTINCT pp.id, pp.title, c.id, c.title, c.level",
                    Client.escape(pageTitle), level);
        }


        JSONObject result = client().query(cypher);
        ArrayList<Page> pages = new ArrayList<Page>();
        JSONArray rows = (JSONArray)result.get("data");
        for(int i = 0; i < rows.size(); i++){
            JSONArray row = (JSONArray)rows.get(i);
            Page page = new Page((Long)row.get(0), (String)row.get(1), (Long)row.get(2), (String)row.get(3), (Long)row.get(4));
            pages.add(page);
        }
        return pages;
    }

    private Client client;
    private Client client(){
        if(this.client == null){
            this.client = new Client(neo4jURL);
        }
        return this.client;
    }

    private ArrayList<Page> convertCypherResultToPages(JSONObject result){
        ArrayList<Page> pages = new ArrayList<Page>();
        JSONArray rows = (JSONArray)result.get("data");
        for(int i = 0; i < rows.size(); i++){
            JSONArray row = (JSONArray)rows.get(i);
            Page page = new Page((Long)row.get(0), (String)row.get(1));
            pages.add(page);
        }
        return pages;
    }
}
