
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
                bat '"C:\\Users\\B.HEMA\\Downloads\\apache-maven-3.9.16-bin\\apache-maven-3.9.16\\bin\\mvn.cmd" clean test package'
            }
        }

        stage('Deploy Locally') {
            steps {
                powershell '''
                    $java = 'C:\\Program Files\\Java\\jdk-25\\bin\\java.exe'
                    $jar = Join-Path (Get-Location) 'target/student-management.jar'
                    $stdout = Join-Path (Get-Location) 'target/app-output.log'
                    $stderr = Join-Path (Get-Location) 'target/app-error.log'

                    if (-not (Test-Path $jar)) {
                        throw "JAR file not found: $jar"
                    }

                    $app = Start-Process -FilePath $java `
                        -ArgumentList ('-jar "' + $jar + '"') `
                        -WorkingDirectory (Get-Location) `
                        -RedirectStandardOutput $stdout `
                        -RedirectStandardError $stderr `
                        -PassThru

                    Write-Host "Started application process: $($app.Id)"

                    $deployed = $false

                    for ($i = 0; $i -lt 30; $i++) {
                        Start-Sleep -Seconds 2
                        $app.Refresh()

                        if ($app.HasExited) {
                            Write-Host "Application exited. Startup logs:"
                            if (Test-Path $stdout) { Get-Content $stdout -Tail 30 }
                            if (Test-Path $stderr) { Get-Content $stderr -Tail 30 }
                            throw "Application process stopped unexpectedly."
                        }

                        try {
                            $response = Invoke-WebRequest 'http://localhost:8081/students' -UseBasicParsing -TimeoutSec 3
                            if ($response.StatusCode -eq 200) {
                                $deployed = $true
                                break
                            }
                        }
                        catch {
                            # Application may still be starting.
                        }
                    }

                    if (-not $deployed) {
                        if (Test-Path $stdout) { Get-Content $stdout -Tail 30 }
                        if (Test-Path $stderr) { Get-Content $stderr -Tail 30 }
                        throw "Application did not become available on port 8081."
                    }

                    Write-Host "Deployment verified. Application is running at http://localhost:8081/students"
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
