# projetplante

## Running with Docker Compose (Recommended)

```sh
docker compose up --build -d
```

The application will be accessible at <http://localhost:8080> and the database on port `5432`.
To stop the application, run:

```sh
docker compose down
```

## Testing the application

```sh
./mvnw clean test
```


## Running the application in dev mode

You can run your application in dev mode that enables live coding using:

```sh
./mvnw quarkus:dev
```

### Generating dev data with Faker

The legacy SQL mock data has been removed from Flyway. In `dev` profile only, you can now generate large fake datasets on demand with:

```sh
curl -X POST http://localhost:8080/api/v1/dev/seed \
  -H "Content-Type: application/json" \
  -d '{
    "replaceExisting": true,
    "userCount": 12,
    "homesPerUser": 2,
    "additionalMembersPerHome": 2,
    "roomsPerHome": 3,
    "plantsPerRoom": 6,
    "logsPerPlant": 4
  }'
```

The response returns the number of generated records plus a small sample of user credentials. All generated users share the password configured by `%dev.plante.dev-seeder.default-user-password` in [src/main/resources/application.properties](src/main/resources/application.properties).

## Packaging and running the application

The application can be packaged using:

```sh
./mvnw package
```

It produces the `quarkus-run.jar` file in the `target/quarkus-app/` directory.
Be aware that it’s not an *uber-jar* as the dependencies are copied into the `target/quarkus-app/lib/` directory.

The application is now runnable using `java -jar target/quarkus-app/quarkus-run.jar`.

If you want to build an *uber-jar*, execute the following command:

```sh
./mvnw package -Dquarkus.package.jar.type=uber-jar
```

The application, packaged as an *uber-jar*, is now runnable using `java -jar target/*-runner.jar`.

## Creating a native executable

You can create a native executable using:

```sh
./mvnw package -Dnative
```

Or, if you don't have GraalVM installed, you can run the native executable build in a container using:

```sh
./mvnw package -Dnative -Dquarkus.native.container-build=true
```

You can then execute your native executable with: `./target/projetplante-1.0-SNAPSHOT-runner`

If you want to learn more about building native executables, please consult <https://quarkus.io/guides/maven-tooling>.

## Related Guides

- REST Jackson ([guide](https://quarkus.io/guides/rest#json-serialisation)): Jackson serialization support for Quarkus
  REST. This extension is not compatible with the quarkus-resteasy extension, or any of the extensions that depend on it

## Provided Code

### REST

Easily start your REST Web Services

[Related guide section...](https://quarkus.io/guides/getting-started-reactive#reactive-jax-rs-resources)
