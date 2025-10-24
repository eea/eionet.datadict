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
      tools { maven 'maven3'; jdk 'Java8' }
      steps {
        withCredentials([string(credentialsId: 'jenkins-maven-token', variable: 'GITHUB_TOKEN')]) {
          sh '''#!/usr/bin/env bash
set -eux
mkdir -p ~/.m2
sed "s/TOKEN/$GITHUB_TOKEN/" m2.settings.tpl.xml > ~/.m2/settings.xml

export logFilePath="$WORKSPACE/build-logs"
export queryLogRetainAll=false
export queryLogRetentionDays=30d
mkdir -p "$logFilePath"

# Fast packaging (no tests here)
mvn -B -V -Dmaven.test.skip=true clean package
'''
        }
      }
      post {
        success { archiveArtifacts artifacts: 'target/*.war', fingerprint: true }
      }
    }

    // --- UNIT TESTS: embedded/in-memory; no external DB ---
    stage('Unit Tests') {
      when { not { buildingTag() } }
      tools { maven 'maven3'; jdk 'Java8' }
      steps {
        sh '''#!/usr/bin/env bash
set -eux
# Optional: log4j2 env to avoid noisy init in tests
export logFilePath="$WORKSPACE/unit-logs"
export queryLogRetainAll=false
export queryLogRetentionDays=14d
mkdir -p "$logFilePath"

mvn -B -V -Denv=unittest -DskipITs=true clean test
mvn -B -V -Denv=unittest -DskipITs=true pmd:pmd pmd:cpd spotbugs:spotbugs checkstyle:checkstyle jacoco:report
'''
      }
      post {
        always {
          junit 'target/surefire-reports/*.xml'
          recordCoverage(
            tools: [[parser: 'JACOCO']],
            id: 'jacoco', name: 'JaCoCo Coverage',
            sourceCodeRetention: 'EVERY_BUILD',
            ignoreParsingErrors: true,
            qualityGates: [
              [threshold: 5.0, metric: 'LINE', baseline: 'PROJECT', unstable: true],
              [threshold: 5.0, metric: 'BRANCH', baseline: 'PROJECT', unstable: true]
            ]
          )
          publishHTML target:[
            allowMissing: false, alwaysLinkToLastBuild: false, keepAll: true,
            reportDir: 'target/site/jacoco-unit-cov-report', reportFiles: 'index.html',
            reportName: "Detailed UNIT Coverage Report"
          ]
        }
      }
    }

    // --- START DB FOR INTEGRATION TESTS (dynamic host port) ---
    stage('Start IT DB') {
      when { not { buildingTag() } }
      steps {
        sh '''#!/usr/bin/env bash
set -Eeuo pipefail

# Clean previous container if any
docker rm -f it-mysql >/dev/null 2>&1 || true

MYSQL_IMAGE="mysql:5.7"
docker pull "$MYSQL_IMAGE" >/dev/null

# Start MySQL 5.7 on default bridge network
docker run -d --name it-mysql \
  -e MYSQL_ROOT_PASSWORD=12345 \
  -e MYSQL_ROOT_HOST=% \
  -e MYSQL_DATABASE=datadict \
  -e MYSQL_USER=app \
  -e MYSQL_PASSWORD=app \
  -e MYSQL_ALLOW_EMPTY_PASSWORD=no \
  "$MYSQL_IMAGE" >/dev/null

# Wait until the server is ready, max 300s
deadline=$((SECONDS+300))
while (( SECONDS < deadline )); do
  if docker logs it-mysql 2>&1 | grep -qi "ready for connections"; then
    echo "MySQL READY (logs)"
    break
  fi
  if docker exec it-mysql mysqladmin ping -h127.0.0.1 -p12345 --silent >/dev/null 2>&1; then
    echo "MySQL READY (mysqladmin)"
    break
  fi
  sleep 2
done
if (( SECONDS >= deadline )); then
  echo "MySQL didn't become ready within 300s"
  docker ps -a --filter "name=it-mysql" || true
  docker logs --tail=200 it-mysql || true
  exit 1
fi

HOST_IP=$(docker inspect -f '{{range.NetworkSettings.Networks}}{{.IPAddress}}{{end}}' it-mysql)
HOST_PORT="3306"
NETWORK_NAME=$(docker inspect -f '{{range $name, $conf := .NetworkSettings.Networks}}{{$name}}{{end}}' it-mysql)

echo "Waiting for application user availability"
ready_app=0
for i in $(seq 1 60); do
  if docker exec it-mysql mysql -h127.0.0.1 -uroot -p12345 -e 'SELECT 1' >/dev/null 2>&1; then
    if docker exec it-mysql mysql -h127.0.0.1 -uapp -papp -Ddatadict -e 'SELECT 1' >/dev/null 2>&1; then
      ready_app=1
      break
    fi
  fi
  sleep 2
done
if [ "$ready_app" -ne 1 ]; then
  echo "ERROR: MySQL application user is not ready after waiting"
  docker logs --tail=200 it-mysql || true
  exit 1
fi

echo "Verifying connectivity from Jenkins via client container"
if ! docker run --rm --network "$NETWORK_NAME" mysql:5.7 mysql -h "$HOST_IP" -P "$HOST_PORT" -uapp -papp -e 'SELECT 1' >/dev/null 2>&1; then
  echo "ERROR: Jenkins client container cannot reach MySQL ${HOST_IP}:${HOST_PORT}"
  docker logs --tail=200 it-mysql || true
  exit 1
fi

# Persist connection details for the next stage
echo "$HOST_IP" > .itdb_host
echo "$HOST_PORT" > .itdb_port
echo "$NETWORK_NAME" > .itdb_network
echo "MySQL for IT on ${HOST_IP}:${HOST_PORT}"
'''
      }
    }

    // --- INTEGRATION TESTS: connect to the DB started above ---
    stage('Integration Tests') {
      when { not { buildingTag() } }
      tools { maven 'maven3'; jdk 'Java8' }
      steps {
        sh '''#!/usr/bin/env bash
set -eux

HOST_IP="$(cat .itdb_host)"
HOST_PORT="$(cat .itdb_port)"
NETWORK_NAME="$(cat .itdb_network)"
IT_JDBC_URL="jdbc:mysql://${HOST_IP}:${HOST_PORT}/datadict?useUnicode=true&characterEncoding=UTF-8&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&sessionVariables=sql_mode=''"
MYSQL_SYS_URL="jdbc:mysql://${HOST_IP}:${HOST_PORT}/mysql?useUnicode=true&characterEncoding=UTF-8&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"
echo "Using IT DB: ${IT_JDBC_URL}"
echo "Verifying MySQL connectivity from Jenkins agent"
docker run --rm --network "$NETWORK_NAME" mysql:5.7 mysql -h "${HOST_IP}" -P "${HOST_PORT}" -uapp -papp -e 'SELECT 1' >/dev/null 2>&1 || {
  echo "ERROR: Jenkins agent cannot reach MySQL ${HOST_IP}:${HOST_PORT}"
  docker logs --tail=200 it-mysql || true
  exit 1
}

# Some tests read Spring DS envs; others use test.mysql.* placeholders
export SPRING_DATASOURCE_URL="${IT_JDBC_URL}"
export SPRING_DATASOURCE_USERNAME="app"
export SPRING_DATASOURCE_PASSWORD="app"

# Log4j2 env for IT (its config uses ${env:...})
export logFilePath="$WORKSPACE/it-logs"
export queryLogRetainAll=false
export queryLogRetentionDays=14d
mkdir -p "$logFilePath"

# IMPORTANT:
# - Run ONLY ITs here (skip UTs).
# - Disable any docker-maven-plugin/database in the POM to avoid a second DB.
# - Provide explicit test.mysql.* properties expected by the Spring test context.
mvn -B -V -Denv=jenkins -DskipUTs=true -Ddocker.skip=true -DskipDocker=true -P '!docker' \
  -Ddocker.host.address="${HOST_IP}" \
  -Dmysql.port="${HOST_PORT}" \
  -Dtest.mysql.url="${MYSQL_SYS_URL}" \
  -Dtest.mysql.usr=root \
  -Dtest.mysql.username=root \
  -Dtest.mysql.password=12345 \
  -Dtest.mysql.psw=12345 \
  -Dtest.db.jdbcurl="${IT_JDBC_URL}" \
  -Dtest.db.user=app \
  -Dtest.db.password=app \
  -Ddocker.it.skip=true \
  verify
'''
      }
      post {
        always {
          junit 'target/failsafe-reports/*.xml'
          publishHTML target:[
            allowMissing: true, alwaysLinkToLastBuild: false, keepAll: true,
            reportDir: 'target/site/jacoco-it-cov-report', reportFiles: 'index.html',
            reportName: "Detailed IT Coverage Report"
          ]
          publishHTML target:[
            allowMissing: true, alwaysLinkToLastBuild: false, keepAll: true,
            reportDir: 'target/site/jacoco-merged-cov-report', reportFiles: 'index.html',
            reportName: "Detailed Merged Coverage Report"
          ]
          // Clean disposable DB after IT
          sh 'docker rm -f it-mysql || true'
        }
      }
    }

    stage ('Sonarqube') {
      when { not { buildingTag() } }
      tools { maven 'maven3'; jdk 'Java17' }
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
      when { environment name: 'CHANGE_ID', value: '' }
      steps {
        script {
          if (env.BRANCH_NAME == 'master') { tagName = 'latest' } else { tagName = "$BRANCH_NAME" }
          def date = sh(returnStdout: true, script: 'echo $(date "+%Y-%m-%dT%H%M")').trim()
          dockerImage = docker.build("$registry:$tagName", "--no-cache .")
          docker.withRegistry('', 'eeajenkins') {
            dockerImage.push()
            dockerImage.push(date)
          }
        }
      }
      post {
        always { sh "docker rmi $registry:$tagName | docker images $registry:$tagName" }
      }
    }

    stage('Release') {
      when { buildingTag() }
      steps {
        node(label: 'docker') {
          withCredentials([
            string(credentialsId: 'eea-jenkins-token', variable: 'GITHUB_TOKEN'),
            usernamePassword(credentialsId: 'jekinsdockerhub', usernameVariable: 'DOCKERHUB_USER', passwordVariable: 'DOCKERHUB_PASS')
          ]) {
            sh '''docker pull eeacms/gitflow; docker run -i --rm --name="$BUILD_TAG-release" \
              -e GIT_BRANCH="$BRANCH_NAME" -e GIT_NAME="$GIT_NAME" \
              -e DOCKERHUB_REPO="$registry" -e GIT_TOKEN="$GITHUB_TOKEN" \
              -e DOCKERHUB_USER="$DOCKERHUB_USER" -e DOCKERHUB_PASS="$DOCKERHUB_PASS" \
              -e RANCHER_CATALOG_PATHS="$datadictTemplate" -e GITFLOW_BEHAVIOR="RUN_ON_TAG" eeacms/gitflow'''
          }
        }
      }
    }
  }

  post {
    always {
      // Safety cleanup in case IT post didn't run
      sh 'docker rm -f it-mysql >/dev/null 2>&1 || true'

      cleanWs(cleanWhenAborted: true, cleanWhenFailure: true, cleanWhenNotBuilt: true,
              cleanWhenSuccess: true, cleanWhenUnstable: true, deleteDirs: true)

      script {
        def url = "${env.BUILD_URL}/display/redirect"
        def status = currentBuild.currentResult
        def details = """<h1>${env.JOB_NAME} - Build #${env.BUILD_NUMBER} - ${status}</h1>
                         <p>Check console output at <a href="${url}">${env.JOB_BASE_NAME} - #${env.BUILD_NUMBER}</a></p>"""
        withCredentials([string(credentialsId: 'eworx-email-list', variable: 'EMAIL_LIST')]) {
          emailext(to: "$EMAIL_LIST", subject: '$DEFAULT_SUBJECT', body: details, attachLog: true, compressLog: true)
        }
      }
    }
  }
}
