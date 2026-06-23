# Sub-project A — CI: Docker build & push to Docker Hub

**Date:** 2026-06-21
**Parent:** DevOps Project 02 (CD system for YAS)
**Status:** Design approved, pending spec review

## Context

Project 02 builds the CD half of the YAS DevOps coursework (target: full 10 points,
including both advanced parts). The existing `Jenkinsfile` (from Project 01) is a
multibranch CI pipeline that, for each changed service, runs gitleaks, builds & tests
with Maven, runs SonarQube, Snyk, and enforces 70% JaCoCo line coverage. **It does not
build or push container images.**

Sub-project A closes that gap: it adds container image build + push to the CI pipeline,
which is the prerequisite for every K8s deployment step (B, C, E) since those pull
images by tag from a registry.

This is the first of six decomposed sub-projects:

| # | Sub-project | Scope |
|---|---|---|
| **A** | CI docker build+push (this spec) | core |
| B | Minikube bootstrap + deploy subset YAS via Helm (NodePort) | core |
| C | Jenkins `developer_build` job (per-service branch param) | core |
| D | Jenkins delete/teardown job | core |
| E | ArgoCD GitOps dev + staging | advanced |
| F | Istio + Kiali (mTLS, authz, retry) | advanced |

## Requirement mapping

Satisfies Project 02 requirement **#3**: "Phần CI, với mỗi branch của user tạo, sau khi
user commit code thay đổi, bạn phải build ra một image với tag là commit id cuối cùng của
branch đó, và push image đó lên Docker Hub." Also produces the `main`/`latest` tag that
requirement #4 (`developer_build`) uses as the default image for non-selected services.

## Decisions

- **Registry:** Docker Hub (per requirement; ghcr.io rejected to avoid losing points).
- **Image naming:** `<dockerhub-user>/yas-<service>`, one public repo per service
  (auto-created on first push; public repos are unlimited on the free tier).
- **Commit tag:** short 7-char SHA, e.g. `yas-tax:76e7078`.
- **Trigger scope:** every branch (per requirement #3), reusing the existing
  `SERVICES_TO_BUILD` path-filter so only changed services are built/pushed.
- **`main` branch:** additionally tag and push `main` and `latest`.
- **Service scope:** backend Java services only (the existing `allServices` list).
  Frontend (Node) images are out of scope.

## Prerequisites (manual, done by user)

1. Create a Docker Hub account (free).
2. Create a Docker Hub **access token** (Account Settings → Security → New Access Token,
   read/write scope).
3. In Jenkins, create a **Username with password** credential `dockerhub-creds`
   (username = Docker Hub user, password = access token).
4. Decide the Docker Hub username; it becomes the `DOCKERHUB_USER` pipeline variable.

## Design

### Pipeline changes (`Jenkinsfile`)

1. **Environment / login.** Add `DOCKERHUB_USER` (env or Jenkins global var) and wrap the
   build stage with `withCredentials([usernamePassword(credentialsId: 'dockerhub-creds',
   usernameVariable: 'DH_USER', passwordVariable: 'DH_PASS')])`, running `docker login -u
   $DH_USER -p $DH_PASS` once per build (before the parallel service loop).

2. **Per-service image build & push.** Extend `buildService(serviceName)` (currently runs
   `mvn install`, `mvn test jacoco:report`, sonar). After `mvn install` the service jar
   exists in `<service>/target/`, which the service `Dockerfile` copies
   (`COPY target/<svc>*.jar app.jar`). Add at the end of `buildService`:

   ```
   def shortSha = env.GIT_COMMIT.take(7)
   def repo = "${DOCKERHUB_USER}/yas-${serviceName}"
   sh "docker build -t ${repo}:${shortSha} ./${serviceName}"
   sh "docker push ${repo}:${shortSha}"
   if (env.BRANCH_NAME == 'main') {
       sh "docker tag ${repo}:${shortSha} ${repo}:latest"
       sh "docker tag ${repo}:${shortSha} ${repo}:main"
       sh "docker push ${repo}:latest"
       sh "docker push ${repo}:main"
   }
   ```

   Build context is the service directory; the jar is already present from the prior
   `mvn install` step, so no multi-stage rebuild is needed.

3. **Reuse path filter.** No change to `SERVICES_TO_BUILD` logic — only changed services
   reach `buildService`, so only they are pushed. `common-library` is a library (no
   Dockerfile / runnable jar) and is excluded from image build.

4. **Ordering.** Image build/push runs after tests pass within `buildService`, so a failing
   test prevents a bad image from being published.

### Components & boundaries

- **`buildService` (Jenkinsfile function):** single responsibility per service — build,
  test, scan, **now also containerize & push**. Inputs: service name, `GIT_COMMIT`,
  `BRANCH_NAME`, Docker Hub creds. Output: pushed image tag(s). Testable by running the
  pipeline on one branch and inspecting Docker Hub.
- **Docker Hub:** external artifact store. Interface = image coordinates
  `<user>/yas-<service>:<tag>`.
- **Jenkins agent:** must have Docker CLI + daemon access (DinD or mounted
  `/var/run/docker.sock`). This is an environment prerequisite to verify before coding.

### Error handling

- Docker daemon unavailable → fail fast with clear message (check `docker info` in
  Initialize stage).
- Push auth failure → surfaces from `docker push` non-zero exit; build fails.
- A failing `mvn test` aborts before build/push (no broken image published).

## Verification

1. Push a commit to a non-main test branch touching one service (e.g. `tax`).
2. Confirm Jenkins builds only that service and pushes `yas-tax:<sha>` to Docker Hub.
3. Confirm the tag exists on Docker Hub and `docker pull <user>/yas-tax:<sha>` works;
   `docker run` the image starts the service.
4. Merge to `main`; confirm `latest` and `main` tags also appear.

## Out of scope

- K8s deployment of the images (sub-project B).
- Frontend (Node) service images.
- Registry cleanup / retention policy.
- Multi-arch builds.
