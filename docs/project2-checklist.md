# Project 2 (CD) — Checklist tiến độ

> Memory chung khi làm Project 2 với Claude. Mục tiêu: **10đ** (core 6đ + 2 nâng cao 4đ).
> Cập nhật `[ ]` → `[x]` khi xong. Branch làm việc: `feat/project2-cd`.

## Bối cảnh / env facts
- Máy: 1 laptop CachyOS, 16GB RAM → chạy **subset service** (xem dưới).
- Minikube: 2 node đã tạo sẵn.
- Registry: **Docker Hub**, user = `thuonghong`, image = `thuonghong/yas-<service>`.
- Image tag: short 7-char SHA mọi branch; branch `main` thêm `main`+`latest`.
- Mỗi backend service có Dockerfile copy `target/*.jar` (cần `mvn install` trước `docker build`, không jib).
- Helm charts có sẵn: `k8s/charts/`. Deploy infra scripts: `k8s/deploy/`.
- Frontend (Node) + infra → cân nhắc pull upstream `ghcr.io/nashtech-garage/yas-*`, không build-own.

## Subset service chạy demo (14, sampledata chạy 1 lần rồi tắt)
product, cart, order, customer, inventory, tax, media, search,
storefront-bff, storefront-ui, backoffice-bff, backoffice-ui, swagger-ui, sampledata.

---

## A — CI build & push Docker Hub  (core, req #3)  ✅ DONE
- [x] Sửa `Jenkinsfile`: env `DOCKERHUB_USER`, docker login, `buildAndPushImage` (skip common-library)
- [x] Docker Hub: tạo account + access token (R/W)
- [x] Jenkins cred `dockerhub-creds` (Username+password = user/token)
- [x] Jenkins agent có docker daemon (`docker info` OK)
- [x] Commit + push diff Jenkinsfile
- [x] **Verify:** commit nhánh test → tag SHA push OK (`thuonghong/yas-tax:144b4a0` pull được)
- [ ] Verify: merge main → tag `main`+`latest` xuất hiện (check khi merge PR đầu)

## B — Minikube deploy subset (ingress)  (core, req #1,#2)  ✅ DONE
- Chốt: **single-node** (lab cho phép 1M+1W *hoặc* Minikube/bất kỳ). 2-node×12G=24G không vừa host 15G.
- Chốt: **ingress + /etc/hosts** (giữ Keycloak SSO). Cluster `--memory 11000 --disk 40000 --cpus 4`.
- Chốt: baseline dùng **upstream ghcr `:latest`** (chart default). C mới swap image của ta.
- Scripts tạo trong `k8s/deploy/`: `setup-cluster-min.sh` (postgres+kafka+ES, strimzi PIN 0.45.2), `deploy-yas-subset.sh` (+yas-configuration first, +ui-alias-services), `subset-limits.yaml` (heap/RAM cap backends), `storefront-bff-limits.yaml`/`backoffice-bff-limits.yaml` (giữ UI_HOST+prod profile), `ui-alias-services.yaml`.
- [x] Recreate cluster single-node + ingress addon
- [x] Infra up: postgres, kafka(+zk+connect), elasticsearch(green), keycloak, redis. yq cài `~/.local/bin`.
- [x] Deploy yas-configuration (configmaps+secrets) TRƯỚC apps — bắt buộc
- [x] Deploy 13 svc + UIs (heap cap -Xmx320m; kibana xoá, redis replica=0 để tiết kiệm RAM)
- [x] CoreDNS patch: YAS domains → ingress ClusterIP 10.100.215.248 (bff resolve identity.yas.local.com)
- [x] Fix gateway routes: chart dùng legacy `spring.cloud.gateway.routes`, image `:latest` đọc `server.webflux.routes` → re-path trong yas-configuration values
- [x] **Verify:** storefront `/`→200, backoffice `/`→302(login), `/api/product`+`/api/tax` api-docs→200, swagger up
- [x] sampledata deployed (Running; seed qua API on-demand, chưa trigger)
- [x] **search FIXED:** ES8.8.1 vs ES client 9.x (Spring Boot 4.0.2) → bump ES 9.0.4 trong chart. search 1/1, tạo index `product`.
- [ ] **USER:** thêm `/etc/hosts`: `192.168.49.2 identity.yas.local.com backoffice.yas.local.com storefront.yas.local.com api.yas.local.com` để mở browser
- [ ] **CÒN:** chưa commit scripts/chart fixes + checklist (Jenkinsfile A đã push)

## C — Jenkins `developer_build` job  (core, req #4)  ✅ CODE DONE
- Code: `cd/developer-build.Jenkinsfile`. Param branch/service (default main); branch≠main → resolve SHA (`git ls-remote`) → helm override `image.repository=thuonghong/yas-<svc>` tag=<sha>; others giữ baseline. In URLs cuối job.
- [x] Validated: helm override deploy `thuonghong/yas-tax:144b4a0` → pod Running (đã revert baseline)
- [ ] **Runtime:** tạo Jenkins Pipeline job trỏ tới `cd/developer-build.Jenkinsfile`; agent cần helm/kubectl/yq/git + kubeconfig
- [ ] **Verify demo:** chạy `tax=<branch>` → chỉ tax dùng image branch đó

## D — Jenkins delete job  (core, req #5)  ✅ CODE DONE
- Code: `cd/delete.Jenkinsfile`. Param NAMESPACE/RELEASES/DELETE_NAMESPACE; link tới C qua DEVELOPER_BUILD_JOB_URL (build description).
- [ ] **Runtime:** tạo job + set DEVELOPER_BUILD_JOB_URL; **Verify:** chạy → release C bị xóa

## E — ArgoCD dev + staging  (nâng cao 2đ, thay req #6)  ✅ CODE DONE
- Code: `cd/argocd/` (appproject, dev-apps main+auto→ns dev, staging-apps tag v1.0.0+manual→ns staging, README). Subset: yas-configuration+product+storefront-bff+storefront-ui, dùng chung infra qua FQDN. CI tag-build đã thêm vào Jenkinsfile (`thuonghong/yas-<svc>:vX.Y.Z`).
- [x] **Runtime DONE:** ArgoCD cài (ns argocd, apply --server-side vì CRD lớn), apply appproject+dev+staging.
- [x] **Verified live:** 4 dev apps **Synced/Healthy**, ns `dev` pods 1/1 (product, storefront-bff, storefront-ui, yas-configuration) — auto-sync từ main. UI: `kubectl port-forward svc/argocd-server -n argocd 8080:443`, admin/(argocd-initial-admin-secret).
- [ ] **staging:** apps Unknown — cần `git tag v1.0.0 && push` → CI build `thuonghong:v1.0.0` → sync. Deliverable demo.

## F — Istio + Kiali (mTLS, authz, retry)  (nâng cao 2đ)  ✅ CODE DONE
- Code: `cd/istio/` (peer-authentication STRICT ns yas, authorization-policy product allow search-only, virtualservice tax retry×3, README). Authz demo dùng SA-based curl pod → không cần search app chạy.
- [x] **Runtime DONE:** istioctl 1.24.3 demo, ns yas injected, sidecars 2/2, 3 policies applied. Scale down backoffice-bff/ui+swagger+sampledata+media để lấy RAM (restore sau).
- [x] **Verified live:** mTLS STRICT; search→product:8090 **200** (via Envoy), cart→product **403 RBAC**; tax route numRetries=3 retryOn 5xx.
- [ ] **Deliverable:** screenshot Kiali topology — `istioctl dashboard kiali` (Graph → ns yas, padlock=mTLS).

---

## Thứ tự: A → B → C → D → (E ∥ F)
## Nộp: báo cáo screenshot từng bước, file `<MSSV...>.docx`, nhóm 4 SV
