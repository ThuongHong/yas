# Sub-project F — Istio service mesh + Kiali

Advanced part. Adds mutual TLS, a service-to-service authorization policy, and an
HTTP retry policy to the `yas` namespace, observed in Kiali.

## Install

```sh
# 1. Istio (demo profile)
istioctl install --set profile=demo -y

# 2. Enable sidecar injection for yas and restart so pods get the sidecar
kubectl label namespace yas istio-injection=enabled --overwrite
kubectl rollout restart deployment -n yas
kubectl rollout status deployment -n yas --timeout=300s

# 3. Addons: Kiali + Prometheus (+ Grafana/Jaeger)
kubectl apply -f https://raw.githubusercontent.com/istio/istio/release-1.24/samples/addons/prometheus.yaml
kubectl apply -f https://raw.githubusercontent.com/istio/istio/release-1.24/samples/addons/kiali.yaml

# 4. Policies
kubectl apply -f cd/istio/peer-authentication.yaml
kubectl apply -f cd/istio/authorization-policy-product.yaml
kubectl apply -f cd/istio/virtualservice-tax-retry.yaml
```

> RAM: sidecars add ~100Mi/pod. On a 16GB laptop, narrow the subset (scale unused
> backends to 0) before enabling injection if pods stay Pending.

Kiali dashboard: `istioctl dashboard kiali`.

## Verify (deliverables)

**mTLS** — Kiali Graph shows padlock icons on edges; or:
```sh
istioctl authn tls-check $(kubectl get pod -n yas -l app.kubernetes.io/name=product \
  -o jsonpath='{.items[0].metadata.name}').yas product.yas.svc.cluster.local
```

**AuthorizationPolicy (search->product allowed, others denied)** — the policy keys on the
caller's SA identity, so this works even without the search app running, using a pod that
runs under each service account:
```sh
# ALLOWED (search identity) -> HTTP 200
kubectl run t-allow -n yas --rm -it --image=curlimages/curl --overrides='{"spec":{"serviceAccountName":"search"}}' --restart=Never -- \
  curl -s -o /dev/null -w "%{http_code}\n" http://product/actuator/health
# DENIED (cart identity) -> HTTP 403 RBAC: access denied
kubectl run t-deny -n yas --rm -it --image=curlimages/curl --overrides='{"spec":{"serviceAccountName":"cart"}}' --restart=Never -- \
  curl -s -o /dev/null -w "%{http_code}\n" http://product/actuator/health
```

**Retry (tax)** — watch the istio-proxy access log on tax show repeated upstream attempts
when tax returns 5xx; Kiali edge to tax shows the retry/error rate:
```sh
kubectl logs -n yas -l app.kubernetes.io/name=tax -c istio-proxy --tail=20
```

Screenshot the Kiali topology (padlocks + traffic) for the report.
