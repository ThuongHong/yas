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

## B — Minikube deploy subset (ingress)  (core, req #1,#2)  ✅ DONE (search degraded)
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
- [ ] **CÒN:** search CrashLoop (ES8.8.1 + Spring Data ES client incompat trên indices.exists) → scaled 0. PHẢI fix trước F (mesh authz search→product).
- [ ] **USER:** thêm `/etc/hosts`: `192.168.49.2 identity.yas.local.com backoffice.yas.local.com storefront.yas.local.com api.yas.local.com` để mở browser
- [ ] **CÒN:** chưa commit scripts/chart fixes + checklist (Jenkinsfile A đã push)

## C — Jenkins `developer_build` job  (core, req #4)
- [ ] Pipeline job parameterized: 1 string param/service, default `main`
- [ ] Resolve commit SHA cuối của branch param (`git ls-remote`) → `image.tag`
- [ ] `helm upgrade --install` per service với tag override
- [ ] In `domain:NodePort` cuối job cho dev
- [ ] **Verify:** chạy với `tax_branch=dev_tax_service` → chỉ tax dùng image branch đó

## D — Jenkins delete job  (core, req #5)
- [ ] Job param = namespace/release → `helm uninstall` / `kubectl delete ns`
- [ ] Hyperlink trong job description tới C
- [ ] **Verify:** chạy → deployment C bị xóa sạch

## E — ArgoCD dev + staging  (nâng cao 2đ, thay req #6)
- [ ] Cài ArgoCD (`ns argocd`)
- [ ] Manifests env: `k8s/envs/dev/`, `k8s/envs/staging/`
- [ ] App `dev`: track `main`, auto-sync → ns `dev`
- [ ] App `staging`: track tag `v*` → ns `staging`
- [ ] CI build image khi git tag `vX.Y.Z`
- [ ] **Verify:** push main → dev auto-sync; tag v1.0.0 → staging deploy

## F — Istio + Kiali (mTLS, authz, retry)  (nâng cao 2đ)
- [ ] `istioctl install --set profile=demo`; label ns `istio-injection=enabled`; redeploy
- [ ] Kiali + prometheus addon
- [ ] `PeerAuthentication` STRICT (mTLS toàn mesh)
- [ ] `AuthorizationPolicy`: chỉ search→product allow, khác deny
- [ ] `VirtualService` retry 500 cho tax (attempts:3)
- [ ] **Verify + deliverable:** curl pod↔pod (allow/deny + retry log), screenshot Kiali topology, README

---

## Thứ tự: A → B → C → D → (E ∥ F)
## Nộp: báo cáo screenshot từng bước, file `<MSSV...>.docx`, nhóm 4 SV
