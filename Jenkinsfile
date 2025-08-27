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
          // Build package fast (skip tests here)
          sh '''mvn clean -B -V verify -Dmaven.test.skip=true'''
        }
      }
      post {
        success {
          archiveArtifacts artifacts: 'target/*.war', fingerprint: true
        }
      }
    }

    // --- Unit tests: use embedded/in-memory DB (no external MySQL) ---
    stage ('Unit Tests') {
      when { not { buildingTag() } }
      tools {
        maven 'maven3'
        jdk 'Java8'
      }
      steps {
        sh '''
          set -eux
          # Run ONLY unit tests with embedded DB config
          mvn -B -V -Denv=unittest -DskipITs=true clean test

          # Static analysis and unit coverage report
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

    // --- Integration tests: let the POM (docker-maven-plugin) start/stop MySQL ---
    stage('Integration Tests') {
      when { not { buildingTag() } }
      tools {
        maven 'maven3'
        jdk 'Java8'
      }
      steps {
        sh '''
          set -eux
          # Run ONLY integration tests; the POM starts mysql:5.7 and maps a host port
          mvn -B -V -Denv=jenkins -DskipUTs=true verify
        '''
      }
      post {
        always {
          junit 'target/failsafe-reports/*.xml'
          // IT and merged coverage HTML reports (allowMissing in case of failures)
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
      cleanWs(cleanWhenAborted: true, cleanWhenFailure: true, cleanWhenNotBuilt: true, cleanWhenSuccess: true, cleanWhenUnstable: true, deleteDirs: true)

      script {
        def url = "${env.BUILD_URL}/display/redirect"
        def status = currentBuild.currentResult
        def subject = "${status}: Job '${env.JOB_NAME} [${env.BUILD_NUMBER}]'"
        def summary = "${subject} (${url})"
        def details = """<h1>${env.JOB_NAME} - Build #${env.BUILD_NUMBER} - ${status}</h1>
                         <p>Check console output at <a href="${url}">${env.JOB_BASE_NAME} - #${env.BUILD_NUMBER}</a></p>
                      """

        def color = '#FFFF00'
        if (status == 'SUCCESS') {
          color = '#00FF00'
        } else if (status == 'FAILURE') {
          color = '#FF0000'
        }

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
