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
        ArrayList<Page> pages = new ArrayList<Page>();

        String cypher = String.format(
                "MATCH (p:Page {title: \"%s\"}) -[:OUTGOING]-> page RETURN page.id, page.title",
                Client.escape(pageTitle));

        JSONObject result = client().query(cypher);
        return convertCypherResultToPages(result);
    }

    public ArrayList<Page> getIncomingPages(String pageTitle){
        ArrayList<Page> pages = new ArrayList<Page>();

        String cypher = String.format(
                "MATCH (p:Page {title: \"%s\"}) <-[:INCOMING]- page RETURN page.id, page.title",
                Client.escape(pageTitle));

        JSONObject result = client().query(cypher);
        return convertCypherResultToPages(result);
    }

    public ArrayList<Page> getMainCategoryPages(String pageTitle){
        //MATCH (p:Page {title: "KGTV"}) <-[:INCLUDES]- (c:Category {MainCategory: TRUE})

        ArrayList<Page> pages = new ArrayList<Page>();

        return pages;
    }

    public HashMap<Category, ArrayList<Page>> getSubCategoryPages(String pageTitle){
        HashMap<Category, ArrayList<Page>> result = new HashMap<Category, ArrayList<Page>>();

        return result;
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
