#!/bin/bash
# Deploy the 14-service demo subset to namespace yas (Project 2, 16GB Minikube).
# Baseline uses upstream ghcr :latest images (chart defaults) — no dependency on our
# Docker Hub :main tags. Sub-project C overrides image per developer branch.
# serviceMonitor disabled on backends (no Prometheus operator CRD in trimmed infra).
# Run from k8s/deploy/. Needs: helm, yq.
# set -x only (not -e): `read -rd ''` returns nonzero at EOF by design.
set -x

read -rd '' DOMAIN < <(yq -r '.domain' ./cluster-config.yaml)

# Shared config + credentials all services mount (configmaps, secrets). MUST be first.
helm dependency build ../charts/yas-configuration
helm upgrade --install yas-configuration ../charts/yas-configuration \
 --namespace yas --create-namespace

# common backend flags: disable ServiceMonitor (CRD absent) + apply RAM caps
NOSM="--set backend.serviceMonitor.enabled=false -f ./subset-limits.yaml"

# --- BFFs + UIs (front edge) ---
helm dependency build ../charts/backoffice-bff
helm upgrade --install backoffice-bff ../charts/backoffice-bff \
 --namespace yas --create-namespace -f ./backoffice-bff-limits.yaml \
 --set backend.ingress.host="backoffice.$DOMAIN"

helm dependency build ../charts/backoffice-ui
helm upgrade --install backoffice-ui ../charts/backoffice-ui \
 --namespace yas --create-namespace
sleep 30

helm dependency build ../charts/storefront-bff
helm upgrade --install storefront-bff ../charts/storefront-bff \
 --namespace yas --create-namespace -f ./storefront-bff-limits.yaml \
 --set backend.ingress.host="storefront.$DOMAIN"

helm dependency build ../charts/storefront-ui
helm upgrade --install storefront-ui ../charts/storefront-ui \
 --namespace yas --create-namespace
sleep 30

# bff prod-profile routes UI to service names storefront-nextjs/backoffice-nextjs,
# but UI charts create storefront-ui/backoffice-ui. Alias services bridge the gap.
kubectl apply -f ./ui-alias-services.yaml

helm upgrade --install swagger-ui ../charts/swagger-ui \
 --namespace yas --create-namespace \
 --set ingress.host="api.$DOMAIN"
sleep 20

# --- core backends (subset only) ---
for chart in cart customer inventory media order product search tax ; do
    helm dependency build ../charts/"$chart"
    helm upgrade --install "$chart" ../charts/"$chart" \
     --namespace yas --create-namespace $NOSM \
     --set backend.ingress.host="api.$DOMAIN"
    sleep 45
done
