package org.neo4j.example.unmanagedextension;

import org.apache.commons.configuration.Configuration;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.event.PropertyEntry;
import org.neo4j.graphdb.event.TransactionData;
import org.neo4j.graphdb.event.TransactionEventHandler;
import org.neo4j.helpers.collection.IteratorUtil;
import org.neo4j.server.plugins.Injectable;
import org.neo4j.server.plugins.PluginLifecycle;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class SampleTransactionEventHandler implements TransactionEventHandler<Object>, PluginLifecycle, Injectable<SampleTransactionEventHandler> {
    private GraphDatabaseService graphDb;

    @Override
    public Collection<Injectable<?>> start(GraphDatabaseService graphDatabaseService, Configuration configuration) {
        this.graphDb = graphDatabaseService;
        graphDb.registerTransactionEventHandler(this);
        return Collections.<Injectable<?>>singleton(this);
    }

    @Override
    public void stop() {
        graphDb.unregisterTransactionEventHandler(this);
    }

    @Override
    public Object beforeCommit(TransactionData data) throws Exception {
        Set<Node> nodes = new HashSet<Node>();
        nodes.addAll(IteratorUtil.asCollection(data.createdNodes()));
        for (PropertyEntry<Node> nodePropertyEntry : data.assignedNodeProperties()) {
            nodes.add(nodePropertyEntry.entity());
        }
        long time = System.currentTimeMillis();
        for (Node node : nodes) {
            node.setProperty("lastModified", time);
        }
        return null;
    }

    @Override
    public void afterCommit(TransactionData data, Object state) {
    }

    @Override
    public void afterRollback(TransactionData data, Object state) {
    }

    @Override
    public SampleTransactionEventHandler getValue() {
        return this;
    }

    @Override
    public Class<SampleTransactionEventHandler> getType() {
        return SampleTransactionEventHandler.class;
    }
}
