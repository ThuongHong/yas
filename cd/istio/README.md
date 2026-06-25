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

**Retry (tax)** — `attempts: 3` in the VirtualService means Envoy adds up to 3 retries
(4 upstream attempts total) on a 5xx. To produce a deterministic 5xx for evidence,
temporarily route the `tax` host to a fault source (`httpbin /status/503`), then count
the upstream attempts for a single client call:
```sh
# deploy a deterministic 5xx source
kubectl apply -n yas -f https://raw.githubusercontent.com/istio/istio/release-1.24/samples/httpbin/httpbin.yaml
# temporarily point the tax-retry VirtualService route at httpbin (keep the retries block)
kubectl patch virtualservice tax-retry -n yas --type=merge -p \
  '{"spec":{"http":[{"route":[{"destination":{"host":"httpbin","port":{"number":8000}}}],"retries":{"attempts":3,"perTryTimeout":"2s","retryOn":"5xx,gateway-error,connect-failure,reset"}}]}}'

# ONE client call -> Envoy retries -> httpbin sees 4 attempts (1 + 3 retries)
kubectl exec -n yas deploy/order -c order -- wget -qO- -S http://tax:8090/status/503
kubectl logs -n yas deploy/httpbin -c httpbin --since=5s | grep -c 'GET /status/503'   # -> 4
# client sidecar logs response_flags=URX (retry limit exceeded)
kubectl logs -n yas deploy/order -c istio-proxy --since=6s | grep status/503

# restore the real route + clean up
kubectl apply -f cd/istio/virtualservice-tax-retry.yaml
kubectl delete -n yas -f https://raw.githubusercontent.com/istio/istio/release-1.24/samples/httpbin/httpbin.yaml
```

Screenshot the Kiali topology (padlocks + traffic) for the report.
