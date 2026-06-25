# Release v1.1.0

Tagged release of the YAS CD pipeline. The CI builds this service as
`thuonghong/yas-backoffice-bff:v1.1.0` on the `v1.1.0` git tag, which ArgoCD's
`staging-backoffice-bff` Application tracks (see `cd/argocd/staging-apps.yaml`).

backoffice-bff is the release-bearing service for staging because the
product/storefront-bff SonarQube quality gate currently fails.
