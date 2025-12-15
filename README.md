# Spring Network Service

REST API project developed in Java using Spring Boot 3 and MongoDB to manage and hydrate complex network relationships between business contracts.

The application discovers and validates hierarchical relationships stored in MongoDB, derives complete connection graphs using depth-first (DFS) and breadth-first (BFS) traversal algorithms, and generates enriched network structures through decoration and validation layers.

It supports large-scale asynchronous network hydration to Amazon S3 via multipart uploads, with dynamic batching, error tracking, and concurrency control using semaphores and CompletableFutures. Network hydration can be triggered via REST endpoints or automatically through a scheduled task protected by distributed locks, ensuring reliable and scalable execution across environments.

## Requirements

- Java 21
- Spring Boot 3.x.x
- Apache Maven 3.8.6

## Libraries

- [spring-common-parent](https://github.com/erebelo/spring-common-parent): Manages the Spring Boot version and provide common configurations for plugins and formatting.

## Configuring Maven for GitHub Dependencies

To pull the `spring-common-parent` dependency, follow these steps:

1. **Generate a Personal Access Token**:

   Go to your GitHub account -> **Settings** -> **Developer settings** -> **Personal access tokens** -> **Tokens (classic)** -> **Generate new token (classic)**:

   - Fill out the **Note** field: `Pull packages`.
   - Set the scope:
     - `read:packages` (to download packages)
   - Click **Generate token**.

2. **Set Up Maven Authentication**:

   In your local Maven `settings.xml`, define the GitHub repository authentication using the following structure:

   ```xml
   <servers>
     <server>
       <id>github-spring-common-parent</id>
       <username>USERNAME</username>
       <password>TOKEN</password>
     </server>
   </servers>
   ```

   **NOTE**: Replace `USERNAME` with your GitHub username and `TOKEN` with the personal access token you just generated.

## Run App

- Complete the required [AWS Setup](#aws-setup) and [DB Setup](#db-setup) steps.
- Set the following environment variables: `AWS_REGION`, `AWS_ACCESS_KEY_ID`, and `AWS_SECRET_ACCESS_KEY`.
- Run the `SpringNetworkServiceApplication` class as Java Application.

## Collection

[Project Collection](https://github.com/erebelo/spring-network-service/tree/main/collection)

## AWS Setup

[IAM and S3 Setup](https://github.com/erebelo/spring-network-service/blob/main/docs/aws/aws-setup.md)

## DB Setup

Run the script [database.js](https://github.com/erebelo/spring-network-service/blob/main/docs/data/database.js) to create the database, collections, and indexes. Once it has completed, import all of the following [.csv](https://github.com/erebelo/spring-network-service/tree/main/docs/data) files:

- network_db.contracts.csv
- network_db.non_selling_relationships.csv
- network_db.organizations.csv
- network_db.relationships.csv

## Diagram

[Role-Based Business Network Representations](https://github.com/erebelo/spring-network-service/blob/main/docs/data/networks.png)
