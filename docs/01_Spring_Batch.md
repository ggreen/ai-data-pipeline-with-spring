```shell
docker network create data-pipelines
```

Start Postgres

```shell
docker run --name postgresql --network data-pipelines --rm  -e POSTGRESQL_USERNAME=postgres -e ALLOW_EMPTY_PASSWORD=true -e POSTGRESQL_DATABASE=postgres -p 5432:5432 bitnami/postgresql:latest 
```

```shell
docker run --name psql -it --rm --network data-pipelines \
bitnami/postgresql:latest psql -h postgresql -U postgres
```



Cleanup previous runs 
```shell
drop  schema customer cascade;
```

Run batch

```shell
java -jar applications/batching/customer-batch/target/customer-batch-0.0.1-SNAPSHOT.jar --source.input.file.csv="file:./applications/batching/customer-batch/src/test/resources/sources/customers.csv"
```


In Psql
```shell
select * from customer.customers;
```