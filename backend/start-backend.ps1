$ErrorActionPreference = 'Stop'

Write-Host 'BankEase backend requires Java 17 and Maven.'
java -version

if (-not (Get-Command mvn -ErrorAction SilentlyContinue)) {
    throw 'Maven is not installed or is not on PATH. Install Maven, reopen PowerShell, and run this script again.'
}

Set-Location $PSScriptRoot
mvn clean spring-boot:run
