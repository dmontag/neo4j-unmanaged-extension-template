package org.neo4j.example.unmanagedextension;

import org.codehaus.jackson.map.ObjectMapper;
import org.neo4j.cypher.javacompat.ExecutionEngine;
import org.neo4j.cypher.javacompat.ExecutionResult;
import org.neo4j.graphdb.GraphDatabaseService;

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

    @GET
    @Path("/helloworld")
    public String helloWorld() {
        return "Hello World!";
    }

    @GET
    @Path("/friends/{name}")
    public Response getFriends(@PathParam("name") String name, @Context GraphDatabaseService db) throws IOException {
        ExecutionEngine executionEngine = new ExecutionEngine(db);
        ExecutionResult result = executionEngine.execute("START person=node:people(name={n}) MATCH person-[:KNOWS]-other RETURN other.name",
                Collections.<String, Object>singletonMap("n", name));
        List<String> friends = new ArrayList<String>();
        for (Map<String, Object> item : result) {
            friends.add((String) item.get("other.name"));
        }
        ObjectMapper objectMapper = new ObjectMapper();
        return Response.ok().entity(objectMapper.writeValueAsString(friends)).build();
    }
}
