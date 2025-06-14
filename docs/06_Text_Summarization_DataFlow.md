Run Postgres

```shell
docker run --name postgresql --network data-pipelines --rm  -e POSTGRESQL_USERNAME=postgres -e ALLOW_EMPTY_PASSWORD=true -e POSTGRESQL_DATABASE=postgres -p 5432:5432 bitnami/postgresql:latest 
```

Run PostgresML

```shell
docker run --rm --name postgresml \
    -it \
    --network data-pipelines  \
    -v postgresml_data:/var/lib/postgresql \
    -p 6432:5432 \
    -p 8000:8000 \
    ghcr.io/postgresml/postgresml:2.10.0 \
    sudo -u postgresml psql -d postgresml
```


```shell
docker run --name psql -it --rm \
--network data-pipelines \
    bitnami/postgresql:latest psql -h postgresml  -U postgres -d postgresml
```


```sql

create schema if not exists customer;

create table customer.feedback(
feed_id text NOT NULL,
email text NOT NULL,
user_feedback text NOT NULL,
summary text NOT NULL,
 PRIMARY KEY (feed_id)
);
```


---------------------------


```sql
SELECT pg_typeof(pgml.transform( task => '{ "task": "summarization", "model": "google/pegasus-xsum"}'::JSONB, inputs => array[ 'Paris is the capital and most populous city of France, with an estimated population of 2,175,601 residents as of 2018, in an area of more than 105 square kilometres (41 square miles). The City of Paris is the centre and seat of government of the region and province of Île-de-France, or Paris Region, which has an estimated population of 12,174,880, or about 18 percent of the population of France as of 2017.'])::json->0->>'summary_text') as summary_text;


SELECT pgml.transform( task => '{ "task": "summarization", "model": "google/pegasus-xsum"}'::JSONB, inputs => array[ 'Paris is the capital and most populous city of France, with an estimated population of 2,175,601 residents as of 2018, in an area of more than 105 square kilometres (41 square miles). The City of Paris is the centre and seat of government of the region and province of Île-de-France, or Paris Region, which has an estimated population of 12,174,880, or about 18 percent of the population of France as of 2017.'])::json->0->>'summary_text' as summary_text;
```

```shell
http-text-summary=http | postgres-query | postgres
```


Start Http


```shell
java -jar runtime/http-source-rabbit-5.0.1.jar --http.supplier.pathPattern=feedback --server.port=8093 --spring.cloud.stream.bindings.output.destination=customers.input.feedback
```




Start Processor

```shell
java -jar applications/processors/postgres-query-processor/target/postgres-query-processor-0.0.1-SNAPSHOT.jar --spring.datasource.username=postgres --spring.datasource.url="jdbc:postgresql://localhost:6432/postgresml" --spring.datasource.driverClassName=org.postgresql.Driver --spring.cloud.stream.bindings.input.destination=customers.input.feedback --spring.cloud.stream.bindings.output.destination=customers.output.feedback --spring.config.import=optional:file:///Users/Projects/solutions/ai-ml/dev/ai-data-pipeline-with-spring-showcase/applications/processors/postgres-query-processor/src/main/resources/text-summarization.yml --spring.datasource.hikari.max-lifetime=600000 --spring.cloud.stream.bindings.input.group=postgres-query-processor
```

Start Sink


```shell
java -jar applications/sinks/postgres-sink/target/postgres-sink-0.0.1-SNAPSHOT.jar --spring.datasource.username=postgres --spring.datasource.driverClassName=org.postgresql.Driver --spring.datasource.url="jdbc:postgresql://localhost/postgres"  --spring.cloud.stream.bindings.input.destination=customers.output.feedback --spring.config.import=optional:file:///Users/Projects/solutions/ai-ml/dev/ai-data-pipeline-with-spring-showcase/applications/sinks/postgres-sink/src/main/resources/postgres-text-summarization.yml --spring.cloud.stream.bindings.input.group=postgres-sink
```



```shell
curl -X 'POST' \
  'http://localhost:8093/feedback' \
  -H 'accept: */*' \
  -H 'Content-Type: application/json' \
  -d '{
  "id" : "F001",
  "email" : "jmatthews@email",
  "feedback" : "Paris is the capital and most populous city of France, with an estimated population of 2,175,601 residents as of 2018, in an area of more than 105 square kilometres (41 square miles). The City of Paris is the centre and seat of government of the region and province of Île-de-France, or Paris Region, which has an estimated population of 12,174,880, or about 18 percent of the population of France as of 2017."
}'
```


In psql

```sql
select * from customer.feedback;

```