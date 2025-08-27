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
          // Fast build: skip tests here
          sh '''mvn -B -V -Dmaven.test.skip=true clean package'''
        }
      }
      post {
        success {
          archiveArtifacts artifacts: 'target/*.war', fingerprint: true
        }
      }
    }

    // Unit tests use embedded/in-memory config. No external DB here.
    stage ('Unit Tests') {
      when { not { buildingTag() } }
      tools {
        maven 'maven3'
        jdk 'Java8'
      }
      steps {
        sh '''
          set -eux
          mvn -B -V -Denv=unittest -DskipITs=true clean test
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

    // Integration tests: let the POM (docker-maven-plugin) start/stop MySQL.
    stage('Integration Tests') {
      when { not { buildingTag() } }
      tools {
        maven 'maven3'
        jdk 'Java8'
      }
      steps {
        // Optional Log4j2 safety knobs to avoid NPE 'age' during init; harmless if unused.
        withEnv([
          "MAVEN_OPTS=${env.MAVEN_OPTS} -DqueryLogRetentionDays=14 -DqueryLogRetainAll=false -DlogFilePath=${env.WORKSPACE}/it-logs"
        ]) {
          sh '''
            set -eux
            mkdir -p "$WORKSPACE/it-logs"
            # Run ONLY ITs; do NOT skip Docker here (plugin must bring up MySQL & wire props).
            mvn -B -V -Denv=jenkins -DskipUTs=true verify
          '''
        }
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
            sh '''docker pull eeacms/gitflow; docker run -i --rm --name="$BUILD_TAG-release"  -e GIT_BRANCH="$BRANCH_NAME" -e GIT_NAME="$GIT_NAME" -e_
