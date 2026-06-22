// Sub-project C — "developer_build" job (Project 02 req #4).
//
// Parameterized pipeline: one branch param per deployable backend service, default `main`.
// For every service whose param is NOT `main`, resolve that branch's latest commit SHA
// (7 chars) and `helm upgrade` the service to image thuonghong/yas-<svc>:<sha>. Services
// left at `main` are not touched — they keep whatever the baseline deploy (sub-project B)
// installed. This lets a developer push a feature branch, have CI build & push its image,
// then deploy just that service into the running cluster to test it end to end.
//
// Agent requirements: docker not needed; needs `helm`, `kubectl`, `yq`, `git` and a
// kubeconfig pointing at the Minikube cluster (the YAS `yas` namespace from sub-project B).

pipeline {
    agent any

    parameters {
        string(name: 'cart',           defaultValue: 'main', description: 'Branch to deploy for cart (main = leave baseline)')
        string(name: 'customer',       defaultValue: 'main', description: 'Branch to deploy for customer')
        string(name: 'inventory',      defaultValue: 'main', description: 'Branch to deploy for inventory')
        string(name: 'media',          defaultValue: 'main', description: 'Branch to deploy for media')
        string(name: 'order',          defaultValue: 'main', description: 'Branch to deploy for order')
        string(name: 'product',        defaultValue: 'main', description: 'Branch to deploy for product')
        string(name: 'search',         defaultValue: 'main', description: 'Branch to deploy for search')
        string(name: 'tax',            defaultValue: 'main', description: 'Branch to deploy for tax')
        string(name: 'storefront-bff', defaultValue: 'main', description: 'Branch to deploy for storefront-bff')
        string(name: 'backoffice-bff', defaultValue: 'main', description: 'Branch to deploy for backoffice-bff')
        string(name: 'NAMESPACE',      defaultValue: 'yas',  description: 'Target namespace (shares the infra deployed in sub-project B)')
        string(name: 'DOMAIN',         defaultValue: 'yas.local.com', description: 'Ingress base domain')
    }

    environment {
        DOCKERHUB_USER = 'thuonghong'
        REPO_URL       = 'https://github.com/ThuongHong/yas.git'
        // Backend services that have a branch param (must match the parameters above).
        SERVICES = 'cart,customer,inventory,media,order,product,search,tax,storefront-bff,backoffice-bff'
    }

    stages {
        stage('Checkout') {
            steps { checkout scm }
        }

        stage('Resolve & Deploy selected services') {
            steps {
                script {
                    def services = env.SERVICES.split(',')
                    def deployed = []

                    for (svc in services) {
                        def branch = params[svc]
                        if (!branch || branch == 'main') {
                            echo "-- ${svc}: main -> leave baseline (skip)"
                            continue
                        }

                        // Resolve the branch's latest commit SHA (short 7) — matches the CI tag.
                        def fullSha = sh(
                            script: "git ls-remote ${REPO_URL} refs/heads/${branch} | cut -f1",
                            returnStdout: true
                        ).trim()
                        if (!fullSha) {
                            error "Branch '${branch}' not found on ${REPO_URL} for service '${svc}'"
                        }
                        def tag = fullSha.take(7)

                        deployServiceImage(svc, tag)
                        deployed << "${svc} -> ${branch} (${tag})"
                    }

                    if (deployed.isEmpty()) {
                        echo 'No service selected (all params = main). Nothing deployed.'
                    } else {
                        echo "Deployed:\n  " + deployed.join('\n  ')
                    }
                    env.DEPLOYED_SUMMARY = deployed.join(', ')
                }
            }
        }

        stage('Print access URLs') {
            steps {
                script {
                    def ip = sh(script: 'minikube ip 2>/dev/null || echo "<minikube-ip>"', returnStdout: true).trim()
                    echo """
==================== Developer build deployed ====================
Updated services : ${env.DEPLOYED_SUMMARY ?: '(none)'}
Namespace        : ${params.NAMESPACE}

Access (add to /etc/hosts: '${ip} storefront.${params.DOMAIN} backoffice.${params.DOMAIN} api.${params.DOMAIN}'):
  Storefront : http://storefront.${params.DOMAIN}
  Backoffice : http://backoffice.${params.DOMAIN}
  Swagger    : http://api.${params.DOMAIN}/swagger-ui
==================================================================
"""
                }
            }
        }
    }
}

// Deploy one service with the developer's image (thuonghong/yas-<svc>:<tag>) into the
// running cluster, preserving the RAM caps / extra config used by sub-project B.
def deployServiceImage(String svc, String tag) {
    def repo = "${DOCKERHUB_USER}/yas-${svc}"
    echo "-- ${svc}: deploying image ${repo}:${tag}"

    sh "helm dependency build k8s/charts/${svc}"

    // bffs need their own override file (UI_HOST + prod profile + ingress host);
    // plain backends use the shared limits file and the api.* ingress host.
    def valuesFile
    def ingressHost
    if (svc == 'storefront-bff') {
        valuesFile = 'k8s/deploy/storefront-bff-limits.yaml'; ingressHost = "storefront.${params.DOMAIN}"
    } else if (svc == 'backoffice-bff') {
        valuesFile = 'k8s/deploy/backoffice-bff-limits.yaml'; ingressHost = "backoffice.${params.DOMAIN}"
    } else {
        valuesFile = 'k8s/deploy/subset-limits.yaml'; ingressHost = "api.${params.DOMAIN}"
    }

    sh """
        helm upgrade --install ${svc} k8s/charts/${svc} \
          --namespace ${params.NAMESPACE} --create-namespace \
          -f ${valuesFile} \
          --set backend.serviceMonitor.enabled=false \
          --set backend.image.repository=${repo} \
          --set backend.image.tag=${tag} \
          --set backend.ingress.host=${ingressHost} \
          --wait --timeout 180s
    """
}
