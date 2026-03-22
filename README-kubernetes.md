# Plant API

Backend API for the household plant management application, built with Quarkus.

## Tech stack

- Java 21
- Quarkus
- PostgreSQL
- Flyway
- Docker
- Kubernetes (kind)
- Helm
- Task

---

## Local development

### Run PostgreSQL with Docker Compose

```bash
docker compose up -d db
```

### Run the backend in dev mode

```bash
./mvnw quarkus:dev
```

The API will be available on:

- `http://localhost:8080`

OpenAPI documentation is available on:

- `http://localhost:8080/q/openapi`

---

## Docker

### Build the application

```bash
./mvnw clean package -DskipTests
```

### Build the Docker image

```bash
docker build -f src/main/docker/Dockerfile.jvm -t plant-api:latest .
```

---

## Kubernetes local deployment

This project can be deployed locally on a Kubernetes cluster using:

- **kind** for the cluster
- **Helm** for package management
- **Bitnami PostgreSQL** for the database
- **Taskfile** for automation

### Prerequisites

Make sure the following tools are installed:

- Docker
- kind
- kubectl
- Helm
- Task

You can verify them with:

```bash
kind --version
kubectl version --client
helm version
task --version
docker --version
```

---

## Deployment workflow

### 1. Create the kind cluster

```bash
task create-kind
```

### 2. Install PostgreSQL in Kubernetes

```bash
task install-db
```

### 3. Build the backend image

```bash
task build-image-jvm
```

### 4. Load the image into kind

```bash
task load-image-kind
```

### 5. Deploy the backend with Helm

```bash
task deploy
```

### 6. Check the deployment status

```bash
task status
```

Or manually:

```bash
kubectl get pods -n plant
kubectl get svc -n plant
```

### 7. Check backend logs

```bash
task logs
```

### 8. Access the API locally

Start port-forwarding:

```bash
task port-forward
```

Then access the API at:

- `http://localhost:8080`

OpenAPI specification:

- `http://localhost:8080/q/openapi`

---

## One-command bootstrap

You can also run the full setup with:

```bash
task bootstrap
```

This will:

- create the kind cluster
- install PostgreSQL
- build the backend image
- load the image into kind
- deploy the backend
- show cluster status

---

## Available Task commands

### Cluster management

```bash
task create-kind
task delete-kind
```

### Namespace and database

```bash
task create-namespace
task install-db
task uninstall-db
```

### Image build and deployment

```bash
task build-image-jvm
task load-image-kind
task deploy
task redeploy
task undeploy
```

### Debugging

```bash
task pods
task services
task logs
task port-forward
task status
```

### Cleanup

```bash
task reset
```

---

## Kubernetes resources

### Namespace

The deployment uses the namespace:

- `plant`

### PostgreSQL release

The PostgreSQL Helm release name is:

- `plant-db`

The PostgreSQL service inside the cluster is:

- `plant-db-postgresql`

### Backend release

The backend Helm release name is:

- `plant-api`

The backend service inside the cluster is:

- `plant-api`

---

## Cleanup

### Remove the backend and database releases

```bash
task reset
```

### Delete the kind cluster

```bash
task delete-kind
```

---

## Notes

- Production datasource configuration is injected through environment variables.
- PostgreSQL credentials are passed through Helm values and Kubernetes resources.
- Flyway migrations run automatically on backend startup.
- The local Kubernetes setup is intended for development and demonstration purposes.

---

## Project structure

Relevant files for the Kubernetes deployment:

```text
Taskfile.yaml
helm/
  plant-api/
    Chart.yaml
    values.yaml
    templates/
      _helpers.tpl
      configmap.yaml
      secret.yaml
      service.yaml
      deployment.yaml
src/main/resources/application.properties
```
