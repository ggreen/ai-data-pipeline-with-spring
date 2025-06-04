Run Rabbit

```shell
docker run -it --name rabbitmq   --rm  -p 5672:5672 -p 15672:15672  rabbitmq:4.1.0-management 
```


Run Postgres

```shell
```shell
docker run --name postgresql --network data-pipeline --rm  -e POSTGRESQL_USERNAME=postgres -e ALLOW_EMPTY_PASSWORD=true -e POSTGRESQL_DATABASE=postgres -p 5432:5432 bitnami/postgresql:latest 
```

Run PostgresML

```shell
docker run --rm --name postgresml \
    -it \
    --network data-pipeline  \
    -v postgresml_data:/var/lib/postgresql \
    -p 6432:5432 \
    -p 8000:8000 \
    ghcr.io/postgresml/postgresml:2.10.0 \
    sudo -u postgresml psql -d postgresml
```


```shell
docker run --name psql -it --rm \
--network data-pipeline \
    bitnami/postgresql:latest psql -h postgresml  -U postgres -d postgresml
```


```shell
create  schema  if not exists customer ;

create table customer.customer_similarity(
    customer_id text NOT NULL,
    email_similarity text NOT NULL,
    score numeric NOT NULL,
 PRIMARY KEY (customer_id,email_similarity)
);
```

---------------------------


Start Http


```shell
java -jar runtime/http-source-rabbit-5.0.1.jar --http.supplier.pathPattern=customers --server.port=8095 --spring.cloud.stream.bindings.output.destination=customers.similarities.input
```


```shell
java -jar applications/processors/postgres-embedding-similarity-processor/target/postgres-embedding-similarity-processor-0.0.1-SNAPSHOT.jar --spring.datasource.username=postgres --spring.datasource.url="jdbc:postgresql://localhost:6432/postgresml" --spring.datasource.driverClassName=org.postgresql.Driver --spring.cloud.stream.bindings.input.destination=customers.similarities.input --spring.cloud.stream.bindings.output.destination=customers.similarities.output --embedding.similarity.processor.topK=3 --embedding.similarity.processor.similarityThreshold="0.90" --embedding.similarity.processor.documentTextFieldNames="email,phone,zip,state,city,address,lastName,firstName" --spring.datasource.hikari.max-lifetime=600000 --spring.cloud.stream.bindings.input.group=postgres-query-processor
```



Distance 0

```sql
SELECT '[1, 0, 0]' <=> '[1, 0, 0]' AS cosine_distance;
```

Non Zero Distance
```sql
SELECT '[1, 1, 0]' <=> '[1, 0, 0]' AS cosine_distance;
```


Deploy

```properties
app.http.path-pattern=customers
app.http.server.port=8095

app.postgres-embedding-similarity.spring.datasource.username=postgres
app.postgres-embedding-similarity.spring.datasource.url="jdbc:postgresql://localhost:6432/postgresml"
app.postgres-embedding-similarity.spring.datasource.driverClassName=org.postgresql.Driver
app.postgres-embedding-similarity.spring.config.import=optional:file:///Users/Projects/solutions/ai-ml/dev/ai-data-pipeline-with-scdf-showcase/applications/processors/postgres-query-processor/src/main/resources/text-summarization.yml
app.postgres-embedding-similarity.spring.datasource.hikari.max-lifetime=600000


app.postgres.spring.datasource.username=postgres
app.postgres.spring.datasource.url="jdbc:postgresql://localhost:6432/postgresml"
app.postgres.spring.config.import=optional:file:///Users/Projects/solutions/ai-ml/dev/ai-data-pipeline-with-scdf-showcase/applications/sinks/postgres-sink/src/main/resources/postgres-sentiment-analysis.yml
app.postgres.spring.datasource.driverClassName=org.postgresql.Driver
app.postgres.spring.datasource.hikari.max-lifetime=600000
```


```shell
curl -X 'POST' \
  'http://localhost:8095/customers' \
  -H 'accept: */*' \
  -H 'Content-Type: application/json' \
  -d '{
  "firstName" : "Josiah",
  "lastName" : "Imani",
  "email" : "email@email",
  "phone" : "555-555-5555",
  "address" : "12 Straight St",
  "city" : "gold",
  "state" : "ny",
  "zip": "55555"
}'
```


In psql

```sql
select * from customer.feedback;

```


```json
               {
                  "id" : "email@email",
                  "firstName" : "Josiah",
                  "lastName" : "Imani",
                  "email" : "email@email",
                  "phone" : "555-555-5555",
                  "address" : "12 Straight St",
                  "city" : "gold",
                  "state" : "ny",
                  "zip": "55555"
                }
```

```json
               {
                  "id" : "duplicate1@email",
                  "firstName" : "Josiah",
                  "lastName" : "Imani",
                  "email" : "duplicate1@email",
                  "phone" : "555-555-5555",
                  "address" : "12 Straight St",
                  "city" : "gold",
                  "state" : "ny",
                  "zip": "55555"
                }
```


Not working


```sql
[SELECT *, embedding <=> ? AS distance FROM public.vector_store WHERE embedding <=> ? < ?  AND metadata::jsonb @@ '$.id != "email@email"'::jsonpath  ORDER BY distance LIMIT ? ]

```


working

```sql
SELECT *, embedding <=> ? AS distance FROM public.vector_store WHERE embedding <=> ? < ?  ORDER BY distance LIMIT ?
```