pipeline {
    agent any

    stages {
        stage('Checkout') {
            steps {
                echo 'Source code is checked out from the configured repository.'
                checkout scm
            }
        }

        stage('Build and Test') {
            steps {
                bat 'mvn -B clean test package'
            }
        }

        stage('Deploy Locally') {
            steps {
                bat '''
                powershell -NoProfile -ExecutionPolicy Bypass -Command ^
                  "$port = 8081; ^
                   $connections = Get-NetTCPConnection -LocalPort $port -State Listen -ErrorAction SilentlyContinue; ^
                   foreach ($connection in $connections) { Stop-Process -Id $connection.OwningProcess -Force -ErrorAction SilentlyContinue }; ^
                   Start-Sleep -Seconds 2; ^
                   $jar = Join-Path (Get-Location) 'target/student-management.jar'; ^
                   Start-Process -FilePath 'java' -ArgumentList @('-jar', ('"' + $jar + '"')) -WorkingDirectory (Get-Location) -WindowStyle Hidden; ^
                   Start-Sleep -Seconds 12; ^
                   try { Invoke-WebRequest 'http://localhost:8081/students' -UseBasicParsing | Out-Null; Write-Host 'Deployment successful.' } ^
                   catch { Write-Error 'Application did not respond on port 8081.'; exit 1 }"
                '''
            }
        }
    }

    post {
        success {
            echo 'Student Management System deployed successfully at http://localhost:8081'
        }
        failure {
            echo 'Pipeline failed. Check the Console Output for details.'
        }
    }
}
