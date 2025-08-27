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
          sh ''' sed "s/TOKEN/$GITHUB_TOKEN/" m2.settings.tpl.xml > ~/.m2/settings.xml '''
          sh '''mvn clean -B -V verify -Dmaven.test.skip=true'''
        }
      }
      post {
        success {
          archiveArtifacts artifacts: 'target/*.war', fingerprint: true
        }
      }
    }

    // ---------- UNIT TESTS: use embedded DB, do NOT start external MySQL ----------
    stage('Unit Tests') {
      when { not { buildingTag() } }
      tools {
        maven 'maven3'
        jdk 'Java8'
      }
      steps {
        sh '''
          set -eux
          # Run unit tests only, using embedded/in-memory DB
          mvn -B -V -Denv=unittest -DskipITs=true clean test

          # Static analysis and reports (same as before)
          mvn -B -V -Denv=unittest -DskipITs=true \
            pmd:pmd pmd:cpd spotbugs:spotbugs checkstyle:checkstyle jacoco:report
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

    // ---------- INTEGRATION TESTS: start MySQL and run ITs separately ----------
    stage('Start IT DB') {
      when { not { buildingTag() } }
      steps {
        sh '''
          set -e
          docker rm -f it-mysql || true
          docker run -d --name it-mysql \
            -e MYSQL_ROOT_PASSWORD=test \
            -e MYSQL_DATABASE=app \
            -e MYSQL_USER=app \
            -e MYSQL_PASSWORD=app \
            -p 3306:3306 \
            mysql:5.7

          # wait for readiness (max 120s)
          SECS=0
          until docker logs it-mysql 2>&1 | grep -qi "ready for connections"; do
            if [ $SECS -ge 120 ]; then
              echo "MySQL not ready in 120s"
              docker logs --tail=200 it-mysql || true
              exit 1
            fi
            sleep 2
            SECS=$((SECS+2))
          done
          echo "MySQL READY"
        '''
      }
    }

    stage('Integration Tests') {
      when { not { buildingTag() } }
      tools {
        maven 'maven3'
        jdk 'Java8'
      }
      environment {
        DB_USER = 'app'
        DB_PASS = 'app'
      }
      steps {
        sh '''
          set -eux

          # Resolve the host that exposes container port 3306 (portable, no extra deps)
          pick_host() {
            if command -v ip >/dev/null 2>&1; then
              C1="$(ip -4 route get 1.1.1.1 2>/dev/null | awk '{print $7; exit}')"
            fi
            C2="$(hostname -I 2>/dev/null | awk '{print $1}')"
            C3="172.17.0.1"
