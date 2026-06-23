# Sub-project E — ArgoCD GitOps (dev + staging)

Advanced part (replaces core req #6). ArgoCD continuously syncs YAS into two
environments from this Git repo:

| Env     | Tracks            | Namespace | Sync    | Images                         |
|---------|-------------------|-----------|---------|--------------------------------|
| dev     | branch `main`     | `dev`     | auto    | ghcr `:latest` baseline        |
| staging | git tag `v1.0.0`  | `staging` | manual  | `thuonghong/yas-<svc>:v1.0.0`  |

Both environments run a representative subset (yas-configuration, product,
storefront-bff, storefront-ui) and share the infra deployed in sub-project B
(postgres/kafka/elasticsearch/keycloak/redis namespaces) via cross-namespace
FQDNs — separate per-env infra does not fit in 16GB.

## Install

```sh
kubectl create namespace argocd
kubectl apply -n argocd -f https://raw.githubusercontent.com/argoproj/argo-cd/stable/manifests/install.yaml
kubectl -n argocd rollout status deploy/argocd-server

# Project + applications
kubectl apply -f cd/argocd/appproject.yaml
kubectl apply -f cd/argocd/dev-apps.yaml
kubectl apply -f cd/argocd/staging-apps.yaml
```

UI / login:

```sh
kubectl -n argocd port-forward svc/argocd-server 8080:443
kubectl -n argocd get secret argocd-initial-admin-secret \
  -o jsonpath='{.data.password}' | base64 -d ; echo   # admin password
```

## Release flow (staging)

1. `git tag v1.0.0 && git push origin v1.0.0`
2. CI (multibranch job with the "Discover tags" trait) builds and pushes
   `thuonghong/yas-<svc>:v1.0.0` (see `buildAndPushImage` in the root Jenkinsfile).
3. `kubectl -n argocd app sync staging-*` (or click Sync in the UI) — staging
   pulls the tagged manifests + images.

## /etc/hosts (browser)

```
<minikube ip> storefront.dev.yas.local.com api.dev.yas.local.com
<minikube ip> storefront.staging.yas.local.com api.staging.yas.local.com
```

## Verify

- Push to `main` (a chart/values change) → `dev-*` apps auto-sync (ArgoCD UI shows
  Synced/Healthy), `kubectl get pods -n dev` updated.
- Tag `v1.0.0` + sync → `kubectl get pods -n staging` Running with the v1.0.0 images.
