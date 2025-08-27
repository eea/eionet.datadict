pipeline {
  agent {
    node { label "docker-host" }
  }

  environment {
    GIT_NAME = "eionet.datadict"
    SONARQUBE_TAGS = "dd.eionet.europa.eu"
    registry = "eeacms/datadict"
    dockerImage = ''
    tagName = ''
    datadictTemplate = "templates/datadict"
  }

  stages {
    stage('Project Build') {
      tools {
        maven 'maven3'
        jdk 'Java8'
      }
      steps {
        withCredentials([string(credentialsId: 'jenkins-maven-token', variable: 'GITHUB_TOKEN')]) {
          sh '''mkdir -p ~/.m2'''
          sh '''sed "s/TOKEN/$GITHUB_TOKEN/" m2.settings.tpl.xml > ~/.m2/settings.xml'''
          // Fast build: do NOT touch integration phases, do NOT let POM's docker-maven-plugin run.
          sh '''mvn -B -V -DskipTests=true -Ddocker.skip=true -DskipDocker=true clean package'''
        }
      }
      post {
        success {
          archiveArtifacts artifacts: 'target/*.war', fingerprint: true
        }
      }
    }

    // Start a disposable MySQL for the whole UT+IT window (random published port)
    stage('Start Test DB') {
      when { not { buildingTag() } }
      steps {
        sh '''#!/usr/bin/env bash
set -euo pipefail

docker rm -f it-mysql >/dev/null 2>&1 || true
docker run -d --name it-mysql -P \
  -e MYSQL_ROOT_PASSWORD=test \
  -e MYSQL_DATABASE=app \
  -e MYSQL_USER=app \
  -e MYSQL_PASSWORD=app \
  mysql:5.7 >/dev/null

# --- wait until Docker publishes a host port for 3306/tcp ---
HOST_PORT=""
for i in $(seq 1 60); do
  MAP="$(docker port it-mysql 3306/tcp 2>/dev/null || true)"
  if [ -n "$MAP" ]; then
    HOST_PORT="$(echo "$MAP" | sed -E 's/.*:([0-9]+)$/\\1/' | tail -n1)"
  fi
  [ -n "$HOST_PORT" ] && break
  sleep 1
done
if [ -z "$HOST_PORT" ]; then
  echo "ERROR: could not determine published port for 3306/tcp"
  docker ps -a --filter "name=it-mysql" || true
  docker logs --tail=200 it-mysql || true
  exit 1
fi
echo "$HOST_PORT" > mysql_host_port.txt

# --- resolve a reachable host IP from the agent and verify TCP ---
pick_host() {
  local c1="" c2="" c3="172.17.0.1" c4="127.0.0.1"
  if command -v ip >/dev/null 2>&1; then
    c1="$(ip -4 route get 1.1.1.1 2>/dev/null | awk '{print $7; exit}')" || true
  fi
  c2="$(hostname -I 2>/dev/null | awk '{print $1}')" || true
  for h in "$c1" "$c2" "$c3" "$c4"; do
    [ -n "$h" ] || continue
    (exec 3<>/dev/tcp/"$h"/"$HOST_PORT") >/dev/null 2>&1 && { echo "$h"; return 0; }
  done
  return 1
}
HOST_IP="$(pick_host)" || {
  echo "ERROR: cannot reach MySQL on any candidate host for port ${HOST_PORT}"
  docker logs --tail=200 it-mysql || true
  exit 1
}
echo "$HOST_IP" > mysql_host_ip.txt
echo "MySQL published on ${HOST_IP}:${HOST_PORT}"

# --- wait until server is ready (logs OR steady TCP) ---
deadline=$((SECONDS+300))
ok_tcp=0
while (( SECONDS < deadline )); do
  if docker logs it-mysql 2>&1 | grep -qi "ready for connections"; then
    echo "MySQL READY (logs)"; break
  fi
  if (exec 3<>/dev/tcp/"$HOST_IP"/"$HOST_PORT") >/dev/null 2>&1; then
    ok_tcp=$((ok_tcp+1))
  else
    ok_tcp=0
  fi
  if (( ok_tcp >= 3 )); then
    echo "MySQL READY (tcp)"; break
  fi
  sleep 2
done
if (( SECONDS >= deadline )); then
  echo "MySQL didn't become ready within 300s"
  docker ps -a --filter "name=it-mysql" || true
  docker logs --tail=200 it-mysql || true
  exit 1
fi
'''
      }
    }

    // UNIT TESTS: use embedded/in-memory DB (no external MySQL needed)
    stage ('Unit Tests') {
      when { not { buildingTag() } }
      tools {
        maven 'maven3'
        jdk 'Java8'
      }
      steps {
        sh '''#!/usr/bin/env bash
set -eux
mvn -B -V -Denv=unittest -DskipITs=true clean test
mvn -B -V -Denv=unittest -DskipITs=true pmd:pmd pmd:cpd spotbugs:spotbugs checkstyle:checkstyle jacoco:report
'''
      }
      post {
        always {
          junit 'target/surefire-reports/*.xml'
          recordCoverage(tools: [[parser: 'JACOCO']],
            id: 'jacoco', name: 'JaCoCo Coverage',
            sourceCodeRetention: 'EVERY_BUILD',
            ignoreParsingErrors: true,
            qualityGates: [
              [threshold: 5.0, metric: 'LINE', baseline: 'PROJECT', unstable: true],
              [threshold: 5.0, metric: 'BRANCH', baseline: 'PROJECT', unstable: true]
            ])
          publishHTML target:[
            allowMissing: false,
            alwaysLinkToLastBuild: false,
            keepAll: true,
            reportDir: 'target/site/jacoco-unit-cov-report',
            reportFiles: 'index.html',
            reportName: "Detailed UNIT Coverage Report"
          ]
        }
      }
    }

    // INTEGRATION TESTS: reuse the same MySQL and pass the properties your tests expect.
    // Also skip any docker-maven-plugin DB to avoid a second MySQL from the POM.
    stage('Integration Tests') {
      when { not { buildingTag() } }
      tools {
        maven 'maven3'
        jdk 'Java8'
      }
      steps {
        sh '''#!/usr/bin/env bash
set -euo pipefail

HOST_IP="$(cat mysql_host_ip.txt)"
HOST_PORT="$(cat mysql_host_port.txt)"
IT_JDBC_URL="jdbc:mysql://${HOST_IP}:${HOST_PORT}/app?useUnicode=true&characterEncoding=UTF-8&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"

echo "Using IT DB: ${IT_JDBC_URL}"
export SPRING_DATASOURCE_URL="${IT_JDBC_URL}"
export SPRING_DATASOURCE_USERNAME="app"
export SPRING_DATASOURCE_PASSWORD="app"

# Only integration tests; skip UTs; prevent docker-maven-plugin from spawning another DB
mvn -B -V -Denv=jenkins -DskipUTs=true -Ddocker.skip=true -DskipDocker=true \
  -Dtest.mysql.url="${IT_JDBC_URL}" \
  -Dtest.mysql.user=app \
  -Dtest.mysql.username=app \
  -Dtest.mysql.password=app \
  verify
'''
      }
      post {
        always {
          junit 'target/failsafe-reports/*.xml'
          publishHTML target:[
            allowMissing: true,
            alwaysLinkToLastBuild: false,
            keepAll: true,
            reportDir: 'target/site/jacoco-it-cov-report',
            reportFiles: 'index.html',
            reportName: "Detailed IT Coverage Report"
          ]
          publishHTML target:[
            allowMissing: true,
            alwaysLinkToLastBuild: false,
            keepAll: true,
            reportDir: 'target/site/jacoco-merged-cov-report',
            reportFiles: 'index.html',
            reportName: "Detailed Merged Coverage Report"
          ]
        }
      }
    }

    stage ('Sonarqube') {
      when { not { buildingTag() } }
      tools {
        maven 'maven3'
        jdk 'Java17'
      }
      steps {
        script {
          withSonarQubeEnv('Sonarqube') {
            sh '''mvn sonar:sonar -Dsonar.java.source=11 -Dsonar.sources=src/main/java/ -Dsonar.test.exclusions=**/src/test/** -Dsonar.coverage.exclusions=**/src/test/** -Dsonar.java.checkstyle.reportPaths=target/checkstyle-result.xml -Dsonar.java.pmd.reportPaths=target/pmd.xml -Dsonar.coverage.jacoco.xmlReportPaths=target/site/jacoco-unit-cov-report/jacoco.xml,target/site/jacoco-it-cov-report/jacoco.xml -Dsonar.java.spotbugs.reportPaths=target/spotbugsXml.xml -Dsonar.host.url=${SONAR_HOST_URL} -Dsonar.token=${SONAR_AUTH_TOKEN} -Dsonar.java.binaries=target/classes -Dsonar.projectKey=${GIT_NAME}-${GIT_BRANCH} -Dsonar.projectName=${GIT_NAME}-${GIT_BRANCH} '''
            sh '''try=2; while [ $try -gt 0 ]; do curl -s -XPOST -u "${SONAR_AUTH_TOKEN}:" "${SONAR_HOST_URL}api/project_tags/set?project=${GIT_NAME}-${BRANCH_NAME}&tags=${SONARQUBE_TAGS},${BRANCH_NAME}" > set_tags_result; if [ $(grep -ic error set_tags_result ) -eq 0 ]; then try=0; else cat set_tags_result; echo "... Will retry"; sleep 60; try=$(( $try - 1 )); fi; done'''
          }
        }
      }
    }

    stage ('Docker build and push') {
      when {
        environment name: 'CHANGE_ID', value: ''
      }
      steps {
        script {
          if (env.BRANCH_NAME == 'master') {
            tagName = 'latest'
          } else {
            tagName = "$BRANCH_NAME"
          }
          def date = sh(returnStdout: true, script: 'echo $(date "+%Y-%m-%dT%H%M")').trim()
          dockerImage = docker.build("$registry:$tagName", "--no-cache .")
          docker.withRegistry('', 'eeajenkins') {
            dockerImage.push()
            dockerImage.push(date)
          }
        }
      }
      post {
        always {
          sh "docker rmi $registry:$tagName | docker images $registry:$tagName"
        }
      }
    }

    stage('Release') {
      when { buildingTag() }
      steps {
        node(label: 'docker') {
          withCredentials([string(credentialsId: 'eea-jenkins-token', variable: 'GITHUB_TOKEN'),
                           usernamePassword(credentialsId: 'jekinsdockerhub', usernameVariable: 'DOCKERHUB_USER', passwordVariable: 'DOCKERHUB_PASS')]) {
            sh '''docker pull eeacms/gitflow; docker run -i --rm --name="$BUILD_TAG-release"  -e GIT_BRANCH="$BRANCH_NAME" -e GIT_NAME="$GIT_NAME" -e DOCKERHUB_REPO="$registry" -e GIT_TOKEN="$GITHUB_TOKEN" -e DOCKERHUB_USER="$DOCKERHUB_USER" -e DOCKERHUB_PASS="$DOCKERHUB_PASS"  -e RANCHER_CATALOG_PATHS="$datadictTemplate" -e GITFLOW_BEHAVIOR="RUN_ON_TAG" eeacms/gitflow'''
          }
        }
      }
    }
  }

  post {
    always {
      // Clean DB container at the very end (safe if missing)
      sh 'docker rm -f it-mysql >/dev/null 2>&1 || true'

      cleanWs(cleanWhenAborted: true, cleanWhenFailure: true, cleanWhenNotBuilt: true, cleanWhenSuccess: true, cleanWhenUnstable: true, deleteDirs: true)

      script {
        def url = "${env.BUILD_URL}/display/redirect"
        def status = currentBuild.currentResult
        def subject = "${status}: Job '${env.JOB_NAME} [${env.BUILD_NUMBER}]'"
        def details = """<h1>${env.JOB_NAME} - Build #${env.BUILD_NUMBER} - ${status}</h1>
                         <p>Check console output at <a href="${url}">${env.JOB_BASE_NAME} - #${env.BUILD_NUMBER}</a></p>
                      """
        withCredentials([string(credentialsId: 'eworx-email-list', variable: 'EMAIL_LIST')]) {
          emailext(
            to: "$EMAIL_LIST",
            subject: '$DEFAULT_SUBJECT',
            body: details,
            attachLog: true,
            compressLog: true,
          )
        }
      }
    }
  }
}
