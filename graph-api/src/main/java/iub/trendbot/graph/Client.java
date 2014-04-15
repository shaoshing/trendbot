package iub.trendbot.graph;

/**
 * Created by shaoshing on 4/15/14.
 */


import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;


public class Client {
    private String neo4jServerUri;

    public Client(String neo4jServerUri){
        this.neo4jServerUri = neo4jServerUri;
    }

    // NEO4J REST API: http://docs.neo4j.org/chunked/stable/rest-api-cypher.html
    public JSONObject query(String cypherQuery){
        WebResource resource = Client.create().resource( this.serverUri+"cypher" );
        String query = JSONObject.escape(cypherQuery);
        ClientResponse neo4jResponse = resource.accept( "application/json" ).type( "application/json" )
                .entity( "{\"query\" : \""+query+"\", \"params\" : {}}" )
                .post( ClientResponse.class );

        String cypherResult = neo4jResponse.getEntity( String.class );
        neo4jResponse.close();

        JSONObject result = (JSONObject)JSONValue.parse(cypherResult);
        return result;
    }
}
