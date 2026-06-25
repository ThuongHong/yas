# Runbook tái lập demo + chụp screenshot — Đồ án 2 (CD)

Tài liệu này ghi lại **toàn bộ** các bước để dựng lại hệ thống và chụp từng screenshot trong báo cáo
`docs/[DevOps] Lab 2.docx`. Mọi lệnh chạy từ thư mục gốc repo trừ khi ghi chú khác.

> Bối cảnh: 1 laptop, ~15GB RAM, Minikube single-node (docker driver). Vì RAM hạn chế, **không**
> chạy đồng thời full subset + ArgoCD + Istio. Demo theo **3 phase**, tear down giữa các phase.
> Docker Hub user = `thuonghong`. Domain YAS trỏ về node qua `/etc/hosts`.

---

## 0. Chuẩn bị một lần

### 0.1 Cluster + hạ tầng
```bash
minikube start --nodes 1 --memory 11000 --disk-size 40000 --cpus 4
minikube addons enable ingress
cd k8s/deploy
./setup-cluster-min.sh      # postgres, kafka(strimzi 0.45.2), elasticsearch(ECK), keycloak, redis
```

### 0.2 /etc/hosts trên máy chụp (cần sudo)
```
192.168.49.2  storefront.yas.local.com backoffice.yas.local.com api.yas.local.com identity.yas.local.com
```
`192.168.49.2` = `minikube ip`.

### 0.3 Công cụ chụp (Playwright)
Browser tích hợp một số môi trường bị chặn IP private (`ERR_BLOCKED_BY_CLIENT`). **Playwright launch trực
tiếp** thì tới được cluster LAN. Cài:
```bash
pip install playwright
# dùng chromium đã cache sẵn: export PLAYWRIGHT_BROWSERS_PATH=~/.cache/ms-playwright
```
Các script chụp (`shot.py`, `gshot.sh`, …) ở mục [Phụ lục script](#phụ-lục-script) bên dưới.
`shot.py` chụp **vùng nhìn thấy** (viewport), không `full_page` — tránh ảnh web dài lê thê.

**Figure terminal — Ghostty + zsh (powerlevel10k) thật:** mọi figure terminal là cửa sổ **Ghostty** chạy
**zsh interactive** (load `~/.zshrc` + p10k thật), chụp qua `gshot.sh`. `gshot.sh` mở Ghostty **fullscreen**,
dùng temp `ZDOTDIR` source `~/.zshrc` rồi 1 ZLE hook **tự gõ lệnh** tại prompt p10k → ảnh có prompt p10k +
lệnh + output y như terminal thật. Grab **cả màn hình** (`spectacle -m`): cửa sổ fullscreen luôn topmost nên
ảnh chỉ chứa Ghostty, **không lộ cửa sổ khác**. Tránh `spectacle -a` (active-window) vì phụ thuộc focus →
dễ chụp nhầm app khác (rủi ro riêng tư). Sau grab, `magick -trim` cắt sát + viền lại bằng màu nền:
```bash
bash gshot.sh fig_pods2.png "kubectl get pods -n yas" 16 4   # <out.png> <command> [font-size] [sleep_s]
```

---

## 1. Phase 1 — Full subset (shop demo)

### 1.1 Giải phóng RAM + bỏ Istio khỏi yas (nếu trước đó chạy E/F)
```bash
kubectl scale deploy -n argocd --all --replicas=0
kubectl scale statefulset -n argocd --all --replicas=0
kubectl scale deploy -n istio-system --all --replicas=0
kubectl label ns yas istio-injection- --overwrite
kubectl delete peerauthentication,authorizationpolicy,virtualservice --all -n yas --ignore-not-found
```

### 1.2 Deploy full subset (14 service)
```bash
cd k8s/deploy && ./deploy-yas-subset.sh
kubectl get pods -n yas -w   # chờ tất cả 1/1 Running
```

### 1.3 FIX media (ảnh sản phẩm) — bắt buộc để ảnh hiển thị
Chart media **không** khai báo volume cho `/images` (nơi media lưu file) và ingress **không** có route
`/media`. Sample data cũng chỉ tạo metadata, **không** chứa file ảnh thật. Khắc phục:

**(a) Thêm volume `/images`:**
```bash
kubectl patch deploy media -n yas --type=json -p '[
 {"op":"add","path":"/spec/template/spec/volumes/-","value":{"name":"media-images","emptyDir":{}}},
 {"op":"add","path":"/spec/template/spec/containers/0/volumeMounts/-","value":{"name":"media-images","mountPath":"/images"}}
]'
kubectl rollout status deploy/media -n yas
```

**(b) Thêm ingress route `api.yas.local.com/media` → media:**
```bash
kubectl apply -f - <<'EOF'
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: media-public
  namespace: yas
  annotations:
    nginx.ingress.kubernetes.io/use-regex: "true"
spec:
  ingressClassName: nginx
  rules:
  - host: api.yas.local.com
    http:
      paths:
      - path: /media
        pathType: Prefix
        backend:
          service: { name: media, port: { number: 80 } }
EOF
```

**(c) Wipe + seed lại để DB sạch, rồi nạp file ảnh placeholder:**
```bash
# wipe sample data (chỉ sample/demo data — KHÔNG phải data thật)
PGPW=$(kubectl get secret postgres.postgresql.credentials.postgresql.acid.zalan.do -n postgres -o jsonpath='{.data.password}' | base64 -d)
for db in product media; do
  kubectl exec -n postgres postgresql-0 -- env PGPASSWORD="$PGPW" psql -U postgres -d $db -c \
   "DO \$\$ DECLARE r RECORD; BEGIN FOR r IN (SELECT tablename FROM pg_tables WHERE schemaname='public' AND tablename NOT LIKE 'flyway%') LOOP EXECUTE 'TRUNCATE TABLE public.'||quote_ident(r.tablename)||' RESTART IDENTITY CASCADE'; END LOOP; END \$\$;"
done

# seed lại (tạo product + media metadata)
kubectl exec -n yas deploy/sampledata -- wget -qO- --post-data='{"message":"seed"}' \
  --header='Content-Type: application/json' --timeout=300 \
  http://localhost/sampledata/storefront/sampledata
```

**(d) Nạp file ảnh THẬT vào media** — ảnh sản phẩm gốc đã có sẵn trong repo tại `sampledata/images/sample/...`,
đúng path media records trỏ tới (`/images/sample/...`). Chỉ cần tar nguyên cây vào pod media:
```bash
tar c -C sampledata/images . | kubectl exec -i -n yas deploy/media -- tar x -C /images
# verify (ảnh thật iphone)
curl -s -o /dev/null -w "%{http_code} %{content_type}\n" \
  "http://api.yas.local.com/media/medias/7/file/iphone15_thumbnail.jpg"   # 200 image/jpeg
```
> Ghi chú: `/images` là `emptyDir` (ephemeral). Nếu **restart pod media** thì file mất → chạy lại bước (d).
> Muốn bền: đổi sang PVC. (Bản trước dùng ảnh placeholder `ph.py`; không cần nữa vì repo đã có ảnh thật.)

### 1.4 FIX search (Kafka) — nếu search rỗng / kafka crashloop
Sau khi `minikube stop/start` bẩn, ZooKeeper hay kẹt vòng lặp leader-election → broker không nối được:
```bash
kubectl get pods -n kafka       # kafka-0 CrashLoopBackOff, zookeeper-0 Running nhưng log "LOOKING"
kubectl delete pod kafka-cluster-zookeeper-0 -n kafka     # bầu leader sạch (213ms)
kubectl delete pod kafka-cluster-kafka-0 -n kafka         # broker khởi động lại, hết backoff
kubectl delete pod debezium-connect-cluster-connect-0 -n kafka   # connect phục hồi CDC
kubectl get pods -n kafka       # tất cả Running
```
Sau khi Kafka khỏe + seed data, Debezium CDC đẩy product → search → ES. Kiểm tra:
```bash
PW=$(kubectl get secret elasticsearch-es-elastic-user -n elasticsearch -o jsonpath='{.data.elastic}' | base64 -d)
kubectl exec -n yas deploy/search -c search -- sh -c \
 "wget -qO- --header='Authorization: Basic $(printf 'elastic:%s' "$PW" | base64 | tr -d '\n')' \
  'http://elasticsearch-es-http.elasticsearch:9200/_cat/indices/product?v'"   # docs.count > 0
```

### 1.5 FIX backoffice login — đặt password cho user admin (realm Yas)
Realm import có user `admin` (role ADMIN) nhưng không kèm password. Đặt password:
```bash
SRV="http://keycloak-service.keycloak:80"
AP=$(kubectl get secret keycloak-credentials -n keycloak -o jsonpath='{.data.password}' | base64 -d)
kubectl exec -n keycloak keycloak-0 -- sh -c "
  /opt/keycloak/bin/kcadm.sh config credentials --server $SRV --realm master --user admin --password '$AP' --config /tmp/kc.cfg >/dev/null 2>&1
  UROW=\$(/opt/keycloak/bin/kcadm.sh get users -r Yas -q username=admin --fields id --config /tmp/kc.cfg 2>/dev/null | grep -o '\"id\"[^,]*' | head -1 | cut -d'\"' -f4)
  /opt/keycloak/bin/kcadm.sh set-password -r Yas --userid \$UROW --new-password 'Admin@123' --config /tmp/kc.cfg
"
```
→ đăng nhập backoffice: `admin` / `Admin@123`.

### 1.6b FIX swagger-ui (tài liệu API)
Hai vấn đề: (1) ingress `api.yas.local.com` không route `/{svc}/v3/api-docs`; (2) image swagger-ui v4.16
không render OpenAPI 3.1 (Spring Boot 4 emit 3.1.0). Khắc phục:
```bash
# (1) route api-docs cho từng service (servers url = /{svc}, springdoc path /{svc}/v3/api-docs)
kubectl apply -f - <<'EOF'
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata: { name: apidocs-public, namespace: yas }
spec:
  ingressClassName: nginx
  rules:
  - host: api.yas.local.com
    http:
      paths:
      - {path: /product,   pathType: Prefix, backend: {service: {name: product,   port: {number: 80}}}}
      - {path: /cart,      pathType: Prefix, backend: {service: {name: cart,      port: {number: 80}}}}
      - {path: /order,     pathType: Prefix, backend: {service: {name: order,     port: {number: 80}}}}
      - {path: /customer,  pathType: Prefix, backend: {service: {name: customer,  port: {number: 80}}}}
      - {path: /inventory, pathType: Prefix, backend: {service: {name: inventory, port: {number: 80}}}}
      - {path: /tax,       pathType: Prefix, backend: {service: {name: tax,       port: {number: 80}}}}
      - {path: /search,    pathType: Prefix, backend: {service: {name: search,    port: {number: 80}}}}
EOF
# (media đã có route /media từ bước 1.3b)

# (2) nâng swagger-ui lên v5 (hỗ trợ OpenAPI 3.1)
kubectl set image deploy/swagger-ui -n yas swagger-ui=swaggerapi/swagger-ui:v5.17.14
kubectl rollout status deploy/swagger-ui -n yas
# mở http://api.yas.local.com/swagger-ui/index.html?urls.primaryName=Product -> render OK
```

### 1.6c (tuỳ chọn) Thêm dữ liệu tax-classes
tax-classes đã có sẵn "Value Added Tax (VAT)". Muốn thêm lớp thuế: lấy token admin rồi POST. Lưu ý
`servers.url=/tax` nên endpoint là `/tax/backoffice/tax-classes`. (tax-rates và warehouse cần service
**location** — không có trong subset — nên không tạo được dữ liệu cho 2 mục đó.)
```bash
# token: client backoffice-bff (directAccessGrant), secret lấy qua kcadm
SECRET=$(kubectl exec -n keycloak keycloak-0 -- sh -c "/opt/keycloak/bin/kcadm.sh config credentials --server http://keycloak-service.keycloak:80 --realm master --user admin --password $(kubectl get secret keycloak-credentials -n keycloak -o jsonpath='{.data.password}' | base64 -d) --config /tmp/kc.cfg >/dev/null 2>&1; CID=\$(/opt/keycloak/bin/kcadm.sh get clients -r Yas -q clientId=backoffice-bff --fields id --config /tmp/kc.cfg | grep -o '\"id\"[^,]*' | head -1 | cut -d'\"' -f4); /opt/keycloak/bin/kcadm.sh get clients/\$CID/client-secret -r Yas --config /tmp/kc.cfg | grep -o '\"value\"[^,]*' | cut -d'\"' -f4")
TOK=$(curl -s http://identity.yas.local.com/realms/Yas/protocol/openid-connect/token -d grant_type=password -d client_id=backoffice-bff -d client_secret=$SECRET -d username=admin -d password=Admin@123 | python3 -c "import sys,json;print(json.load(sys.stdin)['access_token'])")
kubectl exec -n yas deploy/storefront-bff -- wget -qO- --header="Authorization: Bearer $TOK" --header="Content-Type: application/json" --post-data='{"name":"Standard Rate"}' http://tax/tax/backoffice/tax-classes
```

### 1.6 Chụp screenshot Phase 1
```bash
export PLAYWRIGHT_BROWSERS_PATH=~/.cache/ms-playwright
python3 shot.py "http://storefront.yas.local.com/" storefront2.png            # Hình storefront
python3 shot.py "http://storefront.yas.local.com/search?keyword=iphone" sf_search.png
python3 shot.py "http://storefront.yas.local.com/products/iphone-15" sf_product.png   # /products/ (số nhiều)
python3 shot.py "http://api.yas.local.com/swagger-ui/index.html?urls.primaryName=Product" swagger2.png
python3 bopages.py            # login + chụp dashboard + products/customers/inventory/tax/orders
# terminal figures (Ghostty + zsh p10k, lệnh chạy LIVE):
bash gshot.sh fig_pods2.png  "kubectl get pods -n yas" 16 4
HC='for s in product cart order customer inventory tax media search storefront-bff backoffice-bff; do st=$(kubectl exec -n yas deploy/product -- wget -qO- http://$s:8090/actuator/health); case $st in *UP*) r=UP;; *) r=DOWN;; esac; printf "%-16s %s\n" $s $r; done'
bash gshot.sh fig_health.png "$HC" 16 10
# job/curl logs đã lưu .txt -> cat trong Ghostty (cp vào ~/.cache/yas-demo trước):
bash gshot.sh fig_jobC.png "cat jobC.txt" 15;  bash gshot.sh fig_jobD.png "cat jobD.txt" 15
bash gshot.sh fig_curl.png "cat curl.txt" 14
```

---

## 2. Phase 2 — ArgoCD (nâng cao)

```bash
# giải phóng RAM: tắt full subset (shop shots đã xong)
kubectl scale deploy -n yas --all --replicas=0
# bật ArgoCD
kubectl scale deploy -n argocd --all --replicas=1
kubectl scale statefulset -n argocd --all --replicas=1
until kubectl get deploy -n argocd argocd-server -o jsonpath='{.status.readyReplicas}' | grep -q 1; do sleep 5; done
kubectl get applications -n argocd     # dev-* Synced/Healthy ; staging-* Unknown (chưa tag v1.0.0)

# chụp UI (port-forward + login admin)
PW=$(kubectl get secret argocd-initial-admin-secret -n argocd -o jsonpath='{.data.password}' | base64 -d)
# script argocd_shot.py: port-forward 8080:443, login admin/$PW, chụp /applications + detail dev-product
python3 argocd_shot.py        # -> argocd_apps.png, argocd_detail.png
```
(Optional) demo staging: `git tag v1.0.0 && git push origin v1.0.0` → CI build `thuonghong/yas-*:v1.0.0`
→ ArgoCD sync app staging.

---

## 3. Phase 3 — Istio + Kiali (nâng cao)

```bash
# giải phóng RAM
kubectl scale deploy -n argocd --all --replicas=0
kubectl scale statefulset -n argocd --all --replicas=0
kubectl scale deploy -n dev --all --replicas=0 2>/dev/null
# bật istio + inject yas
kubectl scale deploy -n istio-system --all --replicas=1
kubectl label ns yas istio-injection=enabled --overwrite
until kubectl get deploy -n istio-system istiod -o jsonpath='{.status.readyReplicas}' | grep -q 1; do sleep 4; done
kubectl apply -f cd/istio/                          # PeerAuthentication STRICT + AuthorizationPolicy + VirtualService
kubectl scale deploy product search cart tax -n yas --replicas=1
kubectl rollout restart deploy product search cart tax -n yas   # inject sidecar (2/2)

# test mTLS + authz (curl trong cluster)
kubectl exec -n yas deploy/search -c search -- wget -qO- -S http://product:8090/actuator/health   # 200 (header x-envoy-...)
kubectl exec -n yas deploy/cart   -c cart   -- wget -qO- -S http://product:8090/actuator/health   # 403 Forbidden (RBAC)

# Kiali topology (auth=anonymous)
for i in $(seq 1 25); do kubectl exec -n yas deploy/search -c search -- wget -qO- http://product:8090/actuator/health >/dev/null 2>&1; done
# script kiali.py: port-forward svc/kiali 20001, chụp graph ns yas -> kiali_graph.png
python3 kiali.py
```
Khôi phục về Phase 1 (bỏ mesh): xem [mục 1.1](#11-giải-phóng-ram--bỏ-istio-khỏi-yas-nếu-trước-đó-chạy-ef).

---

## 4. CI / Jenkins (req #3, #4, #5)

- **Docker Hub tags (Hình 2):** mở `https://hub.docker.com/r/thuonghong/yas-tax/tags` → thấy tag = commit id.
- **Job developer_build (Hình 7, 8):** Jenkins → `yas-developer-build` → Build with Parameters
  (`tax=dev_tax_service`) → log in domain:port NodePort.
- **Job delete (Hình 9, 10):** Jenkins → `yas-delete` (`RELEASES=tax`,
  `DEVELOPER_BUILD_JOB_URL=<url job C>`) → log `release "tax" uninstalled` + build description có hyperlink.

> Jenkins chạy trong container riêng (host network, `/var/jenkins_home`). Hình 7/10 cần chụp trực tiếp
> trên UI Jenkins (không tự động hoá ở đây).

---

## Phụ lục script

Tất cả script chụp đặt cùng thư mục, chạy với `export PLAYWRIGHT_BROWSERS_PATH=~/.cache/ms-playwright`.

### shot.py — chụp một URL (vùng nhìn thấy, không full-page)
```python
from playwright.sync_api import sync_playwright
import sys
url, out = sys.argv[1], sys.argv[2]
vh = int(sys.argv[3]) if len(sys.argv) > 3 else 900
with sync_playwright() as p:
    b = p.chromium.launch(headless=True)
    pg = b.new_page(viewport={"width":1440,"height":vh}, device_scale_factor=2)
    try: pg.goto(url, wait_until="networkidle", timeout=30000)
    except Exception as e: print("goto warn:", e)
    pg.wait_for_timeout(3000)
    pg.screenshot(path=out, full_page=False)   # chỉ viewport
    print("OK", out, "title=", pg.title()); b.close()
```

### gshot.sh — chụp Ghostty + zsh (p10k) THẬT (fullscreen → grab → trim)
```bash
#!/usr/bin/env bash
# usage: gshot.sh <out.png> <command> [fontsize] [sleep_s]
set -e
OUT="$1"; CMD="$2"; FS="${3:-15}"; SLP="${4:-3}"; RAW="${OUT%.png}.raw.png"
WORK="$HOME/.cache/yas-demo"; mkdir -p "$WORK"
# temp ZDOTDIR: load ~/.zshrc (p10k) thật + 1 ZLE hook tự gõ & chạy $DEMO_CMD tại prompt
TMPZ="$(mktemp -d)"
cat > "$TMPZ/.zshrc" <<'EOF'
typeset -g POWERLEVEL9K_INSTANT_PROMPT=quiet
source "${HOME}/.zshrc"
autoload -Uz add-zle-hook-widget
_demo_li() { [[ -n $_DEMO_DONE ]] && return; _DEMO_DONE=1; BUFFER=$DEMO_CMD; zle accept-line }
add-zle-hook-widget line-init _demo_li
EOF
# Ghostty fullscreen luôn topmost -> grab cả màn hình chỉ thấy Ghostty (không lộ app khác)
ghostty --fullscreen=true --font-size="$FS" --window-padding-x=18 --window-padding-y=12 \
  --working-directory="$WORK" -e env ZDOTDIR="$TMPZ" DEMO_CMD="$CMD" zsh -i &
GP=$!; sleep "$SLP"
spectacle -m -b -n -e -S -o "$RAW" -d 200   # -m: monitor hiện tại, -e/-S: bỏ viền+bóng
sleep 1; kill $GP 2>/dev/null || true; rm -rf "$TMPZ"
BG=$(magick "$RAW" -format '%[pixel:p{4,4}]' info:)
magick "$RAW" -bordercolor "$BG" -border 1 -fuzz 4% -trim +repage \
  -bordercolor "$BG" -border 22 "$OUT"; rm -f "$RAW"
echo "OK $OUT"
```
Log job/Istio lưu sẵn `.txt`, cp vào `~/.cache/yas-demo` rồi `cat` trong Ghostty (lệnh pods/health chạy live).

### ph.py — tạo ảnh placeholder sản phẩm (placeholder.jpg + .png)
```python
from playwright.sync_api import sync_playwright
html="<div style='width:600px;height:600px;display:flex;align-items:center;justify-content:center;background:linear-gradient(135deg,#e8eef7,#c9d8ee);font-family:Arial;color:#3a5a8a;font-size:40px;font-weight:700;flex-direction:column'><div style='font-size:90px'>&#128241;</div>YAS Product</div>"
with sync_playwright() as p:
    b=p.chromium.launch(headless=True); pg=b.new_page(viewport={"width":600,"height":600})
    pg.set_content(html); pg.screenshot(path="placeholder.jpg", type="jpeg", quality=85); pg.screenshot(path="placeholder.png"); b.close()
```

### bopages.py — login backoffice + chụp các trang service
```python
from playwright.sync_api import sync_playwright
pages=[("/","backoffice_dash.png"),("/catalog/products","bo_products.png"),("/customers","bo_customers.png"),
       ("/inventory/warehouse-products","bo_inventory.png"),("/tax/tax-rates","bo_tax.png"),("/sales/orders","bo_orders.png")]
with sync_playwright() as p:
    b=p.chromium.launch(headless=True); ctx=b.new_context(viewport={"width":1500,"height":900}, device_scale_factor=2); pg=ctx.new_page()
    pg.goto("http://backoffice.yas.local.com/", wait_until="domcontentloaded", timeout=40000); pg.wait_for_timeout(3000)
    pg.fill("#username","admin"); pg.fill("#password","Admin@123"); pg.click("input[type=submit]"); pg.wait_for_timeout(7000)
    for path,out in pages:
        pg.goto("http://backoffice.yas.local.com"+path, wait_until="domcontentloaded", timeout=30000)
        pg.wait_for_timeout(4500); pg.screenshot(path=out); print("OK", out)
    b.close()
```

### argocd_shot.py — login ArgoCD + chụp apps + detail
```python
from playwright.sync_api import sync_playwright
import os
pw=os.environ["ARGO_PW"]   # = kubectl get secret argocd-initial-admin-secret ... | base64 -d
with sync_playwright() as p:
    b=p.chromium.launch(headless=True, args=["--ignore-certificate-errors"])
    ctx=b.new_context(ignore_https_errors=True, viewport={"width":1500,"height":950}, device_scale_factor=2); pg=ctx.new_page()
    pg.goto("https://localhost:8080/", wait_until="domcontentloaded", timeout=30000)
    pg.wait_for_selector("input[name=username]", timeout=20000)
    pg.fill("input[name=username]","admin"); pg.fill("input[name=password]",pw); pg.click("button:has-text('SIGN IN')"); pg.wait_for_timeout(9000)
    pg.goto("https://localhost:8080/applications", wait_until="domcontentloaded", timeout=30000); pg.wait_for_timeout(6000)
    pg.screenshot(path="argocd_apps.png", full_page=True)
    pg.goto("https://localhost:8080/applications/argocd/dev-product", wait_until="domcontentloaded", timeout=30000); pg.wait_for_timeout(8000)
    pg.screenshot(path="argocd_detail.png"); b.close()
```
Chạy kèm port-forward trong **cùng** lệnh shell (port-forward chạy nền sẽ bị kill khi lệnh kết thúc):
```bash
kubectl port-forward svc/argocd-server -n argocd 8080:443 >pf.log 2>&1 & PF=$!; trap "kill $PF" EXIT
until curl -sk -m3 https://localhost:8080/ >/dev/null 2>&1; do sleep 2; done
ARGO_PW=$(kubectl get secret argocd-initial-admin-secret -n argocd -o jsonpath='{.data.password}' | base64 -d) python3 argocd_shot.py
```

### kiali.py — chụp topology
```python
from playwright.sync_api import sync_playwright
url="http://localhost:20001/kiali/console/graph/namespaces?namespaces=yas&duration=300&refresh=0&layout=dagre&graphType=versionedApp"
with sync_playwright() as p:
    b=p.chromium.launch(headless=True); ctx=b.new_context(viewport={"width":1500,"height":950}, device_scale_factor=2); pg=ctx.new_page()
    pg.goto(url, wait_until="domcontentloaded", timeout=40000); pg.wait_for_timeout(12000)
    pg.screenshot(path="kiali_graph.png"); b.close()
```
Kèm: `kubectl port-forward svc/kiali -n istio-system 20001:20001` (cùng lệnh, trap kill như trên).

---

## Tóm tắt mapping screenshot → file

| Hình | File | Lệnh/script |
|------|------|-------------|
| Kiến trúc | fig_arch.png | arch.py (HTML diagram) |
| Docker Hub commit tag | dockerhub_tax.png | shot.py hub.docker.com |
| Pods 14 svc | fig_pods2.png | gshot.sh < kubectl get pods (Ghostty) |
| Health 14 svc | fig_health.png | gshot.sh < health loop (Ghostty) |
| Storefront | storefront2.png | shot.py (viewport) |
| Search | sf_search.png | shot.py /search?keyword=iphone |
| Product detail | sf_product.png | shot.py /products/iphone-15 |
| Swagger Product | swagger2.png | shot.py swagger-ui |
| Backoffice dashboard + pages | backoffice_dash.png, bo_*.png | bopages.py |
| Job C/D log | fig_jobC.png, fig_jobD.png | gshot.sh < log (Ghostty) |
| ArgoCD | argocd_apps.png, argocd_detail.png | argocd_shot.py |
| Istio curl 200/403 | fig_curl.png | gshot.sh < curl test (Ghostty) |
| Kiali topology | kiali_graph.png | kiali.py |
| Jenkins UI form/hyperlink | (chụp tay) | Jenkins UI |
