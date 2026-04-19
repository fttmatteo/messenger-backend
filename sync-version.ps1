param (
    [Parameter(Mandatory=$true)]
    [string]$NewVersion
)

# 1. Actualizar Maven (pom.xml)
Write-Host "Actualizando pom.xml a la version $NewVersion..." -ForegroundColor Cyan
Set-Location messenger
& .\mvnw.cmd versions:set "-DnewVersion=$NewVersion" "-DgenerateBackupPoms=false"
if ($LASTEXITCODE -ne 0) {
    Write-Error "Error al actualizar la version de Maven."
    Set-Location ..
    exit $LASTEXITCODE
}
Set-Location ..

# 2. Actualizar Documentación (READMEs)
Write-Host "Actualizando archivos de documentacion..." -ForegroundColor Cyan

$DocsToUpdate = @("README.md", "README.en.md")

foreach ($File in $DocsToUpdate) {
    if (Test-Path $File) {
        $Content = Get-Content $File
        $NewContent = $Content | ForEach-Object {
            # Coincidir con badges de versión en README: <img src="...Version-1.1.0-blue.svg" alt="Version">
            if ($_ -match "<img src=`".*Version-([\d\.]+(-SNAPSHOT)?)-blue\.svg`".*alt=`"Version`".*>") {
                $_ -replace "Version-[\d\.]+(-SNAPSHOT)?", "Version-$NewVersion"
            }
            else {
                $_
            }
        }
        $NewContent | Set-Content $File
    }
}

Write-Host "Version actualizada con exito a $NewVersion en todos los archivos centralizados." -ForegroundColor Green
Write-Host "Nota: La version de Swagger/OpenAPI se actualizara automaticamente en la proxima compilacion." -ForegroundColor Yellow
