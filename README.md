Sample Neo4j unmanaged extension
================================

This is an unmanaged extension. 

1. Build it: 

        mvn clean package

2. Copy target/unmanaged-extension-template-1.0.jar to the plugins/ directory of your Neo4j server.

3. Configure Neo4j by adding a line to conf/neo4j.conf:

        dbms.unmanaged_extension_classes=com.neo4j.example.extension=/example

4. Start Neo4j server.

5. Query it over HTTP:

        curl http://localhost:7474/example/service/helloworld

