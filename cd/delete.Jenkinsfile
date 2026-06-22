// Sub-project D — delete / teardown job (Project 02 req #5).
//
// Tears down what the developer-build job (sub-project C) deployed. Flexible modes:
//   * RELEASES non-empty  -> `helm uninstall` exactly those releases in NAMESPACE.
//   * RELEASES empty + DELETE_NAMESPACE=true -> `kubectl delete namespace` (nukes everything).
//   * RELEASES empty + DELETE_NAMESPACE=false -> uninstall ALL helm releases in NAMESPACE.
//
// The job description links back to the developer-build job (set DEVELOPER_BUILD_JOB_URL or
// edit the job's description in Jenkins) to satisfy the "hyperlink to the create job" requirement.
//
// Agent requirements: `helm`, `kubectl`, and a kubeconfig pointing at the Minikube cluster.

pipeline {
    agent any

    parameters {
        string(name: 'NAMESPACE', defaultValue: 'yas', description: 'Namespace to tear down in')
        string(name: 'RELEASES',  defaultValue: '',    description: 'Space/comma separated helm releases to uninstall. Empty = all releases in NAMESPACE.')
        booleanParam(name: 'DELETE_NAMESPACE', defaultValue: false, description: 'If RELEASES is empty: delete the whole namespace instead of per-release uninstall.')
        string(name: 'DEVELOPER_BUILD_JOB_URL', defaultValue: '', description: 'URL of the developer-build job (shown in build description).')
    }

    stages {
        stage('Describe & link') {
            steps {
                script {
                    def link = params.DEVELOPER_BUILD_JOB_URL?.trim()
                    currentBuild.description = link
                        ? "Teardown of <a href=\"${link}\">developer-build</a> in ns ${params.NAMESPACE}"
                        : "Teardown in ns ${params.NAMESPACE} (set DEVELOPER_BUILD_JOB_URL to link the create job)"
                }
            }
        }

        stage('Teardown') {
            steps {
                script {
                    def ns = params.NAMESPACE
                    def releases = params.RELEASES?.trim()?.replaceAll(',', ' ')?.split(/\s+/)?.findAll { it } ?: []

                    if (releases) {
                        echo "Uninstalling releases in ns ${ns}: ${releases.join(', ')}"
                        for (r in releases) {
                            sh "helm uninstall ${r} --namespace ${ns} --ignore-not-found || true"
                        }
                    } else if (params.DELETE_NAMESPACE) {
                        echo "Deleting entire namespace ${ns}"
                        sh "kubectl delete namespace ${ns} --ignore-not-found --wait=false"
                    } else {
                        echo "Uninstalling ALL helm releases in ns ${ns}"
                        def all = sh(
                            script: "helm list -n ${ns} -q",
                            returnStdout: true
                        ).trim()
                        if (!all) {
                            echo "No helm releases found in ns ${ns}."
                        } else {
                            for (r in all.split('\n')) {
                                sh "helm uninstall ${r} --namespace ${ns} --ignore-not-found || true"
                            }
                        }
                    }
                }
            }
        }

        stage('Verify') {
            steps {
                script {
                    sh "helm list -n ${params.NAMESPACE} || true"
                    sh "kubectl get pods -n ${params.NAMESPACE} 2>/dev/null || echo 'namespace gone'"
                }
            }
        }
    }
}
