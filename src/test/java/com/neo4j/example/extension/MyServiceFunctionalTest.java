package com.neo4j.example.extension;

import com.sun.jersey.api.client.Client;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Test;
import org.neo4j.graphdb.*;
import org.neo4j.harness.ServerControls;
import org.neo4j.harness.TestServerBuilders;
import org.neo4j.server.rest.JaxRsResponse;
import org.neo4j.server.rest.RestRequest;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class MyServiceFunctionalTest {

    public static final Client CLIENT = Client.create();
    public static final String MOUNT_POINT = "/ext";
    private ObjectMapper objectMapper = new ObjectMapper();

    private static final RelationshipType KNOWS = RelationshipType.withName("KNOWS");

    private ServerControls newTestDb() {
        return TestServerBuilders
                .newInProcessBuilder()
                .withExtension(MOUNT_POINT, MyService.class)
                .newServer();
    }

    @Test
    public void shouldReturnFriends() throws IOException {
        try ( ServerControls server = newTestDb()) {
            populateDb(server.graph());

            RestRequest restRequest = new RestRequest(server.httpURI().resolve(MOUNT_POINT), CLIENT);
            JaxRsResponse response = restRequest.get("service/friendsCypher/B");
            System.out.println(response.getEntity());

            List list = objectMapper.readValue(response.getEntity(), List.class);
            assertEquals(new HashSet<>(Arrays.asList("A", "C")), new HashSet<>(list));
        }
    }

    private void populateDb(GraphDatabaseService db) {

        try (Transaction tx = db.beginTx())
        {
            Node personA = createPerson(db, "A");
            Node personB = createPerson(db, "B");
            Node personC = createPerson(db, "C");
            Node personD = createPerson(db, "D");
            personA.createRelationshipTo(personB, KNOWS);
            personB.createRelationshipTo(personC, KNOWS);
            personC.createRelationshipTo(personD, KNOWS);
            tx.success();
        }
    }

    private Node createPerson(GraphDatabaseService db, String name) {
        Node node = db.createNode(MyService.Labels.Person);
        node.setProperty("name", name);
        return node;
    }


}
