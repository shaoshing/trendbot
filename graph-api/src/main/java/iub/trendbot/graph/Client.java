package iub.trendbot.graph;

/**
 * Created by shaoshing on 4/15/14.
 */


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
    // Input Example:
    //    {
    //        "query" : "MATCH (x {name: {startName}})-[r]-(friend) WHERE friend.name = {name} RETURN TYPE(r)",
    //            "params" : {
    //        "startName" : "I",
    //                "name" : "you"
    //          }
    //    }
    //
    // Output Example 1:
    //    {
    //        "columns" : [ "page.id", "page.title" ],
    //        "data" : [ [ 23329612, "Terry_Bradshaw_(baseball)" ], [ 60089, "Terry_Bradshaw" ] ]
    //    }
    //
    // Output Example 2:
    //    {
    //        "columns" : [ "page" ],
    //        "data" : [ [ {
    //        "outgoing_relationships" : "http://localhost:7474/db/data/node/1107/relationships/out",
    //                "labels" : "http://localhost:7474/db/data/node/1107/labels",
    //                "data" : {
    //            "id" : 23329612,
    //                    "title" : "Terry_Bradshaw_(baseball)",
    //                    "level" : 1
    //        },
    //        "all_typed_relationships" : "http://localhost:7474/db/data/node/1107/relationships/all/{-list|&|types}",
    //                "traverse" : "http://localhost:7474/db/data/node/1107/traverse/{returnType}",
    //                "self" : "http://localhost:7474/db/data/node/1107",
    //                "property" : "http://localhost:7474/db/data/node/1107/properties/{key}",
    //                "outgoing_typed_relationships" : "http://localhost:7474/db/data/node/1107/relationships/out/{-list|&|types}",
    //                "properties" : "http://localhost:7474/db/data/node/1107/properties",
    //                "incoming_relationships" : "http://localhost:7474/db/data/node/1107/relationships/in",
    //                "extensions" : {
    //        },
    //        "create_relationship" : "http://localhost:7474/db/data/node/1107/relationships",
    //                "paged_traverse" : "http://localhost:7474/db/data/node/1107/paged/traverse/{returnType}{?pageSize,leaseTime}",
    //                "all_relationships" : "http://localhost:7474/db/data/node/1107/relationships/all",
    //                "incoming_typed_relationships" : "http://localhost:7474/db/data/node/1107/relationships/in/{-list|&|types}"
    //    } ], [ {
    //        "outgoing_relationships" : "http://localhost:7474/db/data/node/1106/relationships/out",
    //                "labels" : "http://localhost:7474/db/data/node/1106/labels",
    //                "id" : 60089,
    //                "data" : {
    public JSONObject query(String cypherQuery){
        WebResource resource = com.sun.jersey.api.client.Client.create().resource( this.neo4jServerUri+"cypher" );
        String query = Client.escape(cypherQuery);
        ClientResponse neo4jResponse = resource.accept( "application/json" ).type( "application/json" )
                .entity( "{\"query\" : \""+query+"\", \"params\" : {}}" )
                .post( ClientResponse.class );

        String cypherResult = neo4jResponse.getEntity( String.class );
        neo4jResponse.close();

        JSONObject result = (JSONObject)JSONValue.parse(cypherResult);
        return result;
    }

    static public String escape(String str){
        return JSONObject.escape(str);
    }
}
