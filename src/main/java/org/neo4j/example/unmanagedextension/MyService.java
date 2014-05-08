package org.neo4j.example.unmanagedextension;

import org.codehaus.jackson.map.ObjectMapper;
import org.neo4j.cypher.javacompat.ExecutionEngine;
import org.neo4j.cypher.javacompat.ExecutionResult;
import org.neo4j.graphdb.*;
import org.neo4j.helpers.collection.IteratorUtil;
import org.neo4j.server.database.CypherExecutor;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Path("/service")
public class MyService {

    enum Labels implements Label {
        Person
    }

    enum RelTypes implements RelationshipType {
        KNOWS
    }

    @GET
    @Path("/helloworld")
    public String helloWorld() {
        return "Hello World!";
    }

    @GET
    @Path("/friendsCypher/{name}")
    public Response getFriendsCypher(@PathParam("name") String name, @Context CypherExecutor cypherExecutor) throws IOException {
        ExecutionEngine executionEngine = cypherExecutor.getExecutionEngine();
        ExecutionResult result = executionEngine.execute("MATCH (p:Person)-[:KNOWS]-(friend) WHERE p.name = {n} RETURN friend.name",
                Collections.<String, Object>singletonMap("n", name));
        List<String> friendNames = new ArrayList<String>();
        for (Map<String, Object> item : result) {
            friendNames.add((String) item.get("friend.name"));
        }
        ObjectMapper objectMapper = new ObjectMapper();
        return Response.ok().entity(objectMapper.writeValueAsString(friendNames)).build();
    }

    @GET
    @Path("/friendsJava/{name}")
    public Response getFriendsJava(@PathParam("name") String name, @Context GraphDatabaseService db) throws IOException {

        List<String> friendNames = new ArrayList<>();

        try (Transaction tx = db.beginTx()) {
            Node person = IteratorUtil.single(db.findNodesByLabelAndProperty(Labels.Person, "name", name));

            for (Relationship knowsRel : person.getRelationships(RelTypes.KNOWS, Direction.BOTH)) {
                Node friend = knowsRel.getOtherNode(person);
                friendNames.add((String) friend.getProperty("name"));
            }
            tx.success();
        }

        ObjectMapper objectMapper = new ObjectMapper();
        return Response.ok().entity(objectMapper.writeValueAsString(friendNames)).build();
    }
}
