package iub.trendbot.graph;

import java.util.ArrayList;

/**
 * Created by shaoshing on 4/15/14.
 */
public class Graph {

    private String neo4jURL;

    public Graph(String neo4jURL){
        this.neo4jURL = neo4jURL;
    }

    public ArrayList<Page> getIncomingPages(String pageTitle){
        ArrayList<Page> pages = new ArrayList<Page>();

        return pages;
    }

    public ArrayList<Page> getOutgoingPages(String pageTitle){
        ArrayList<Page> pages = new ArrayList<Page>();

        return pages;
    }

    public ArrayList<Page> getMainCategoryPages(String pageTitle){
        ArrayList<Page> pages = new ArrayList<Page>();

        return pages;
    }

    public ArrayList<Page> getSubCategoryPages(String pageTitle){
        ArrayList<Page> pages = new ArrayList<Page>();

        return pages;
    }


    private Client client;
    private Client _client(){
        if(this.client == null){
            this.client = new Client(neo4jURL);
        }
        return this.client;
    }
}
