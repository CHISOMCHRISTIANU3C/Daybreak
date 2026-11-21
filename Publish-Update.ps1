param([Parameter(Mandatory=$true)][string]$Version)
$ErrorActionPreference='Stop'
if($Version -notmatch '^\d+\.\d+\.\d+$'){throw 'Use a version such as 0.6.0.'}
$repo='CHISOMCHRISTIANU3C/Daybreak'
$project=$PSScriptRoot
$apk=Join-Path $project 'app/build/outputs/apk/debug/app-debug.apk'
if(!(Test-Path -LiteralPath $apk)){throw 'Build the APK before publishing.'}
$build=Get-Content -LiteralPath (Join-Path $project 'app/build.gradle') -Raw
$code=[int]([regex]::Match($build,'versionCode\s+(\d+)').Groups[1].Value)
$configured=[regex]::Match($build,"versionName\s+'([^']+)'").Groups[1].Value
if($configured -ne $Version){throw 'Version does not match app/build.gradle.'}
$login=(& gh api user --jq .login).Trim()
if($login -ne 'CHISOMCHRISTIANU3C'){throw 'Authenticate gh as the Daybreak repository owner before publishing.'}
$staging=Join-Path $project 'build/update-channel'
New-Item -ItemType Directory -Force -Path $staging | Out-Null
$manifest=@{packageName='com.daybreak.clock';versionCode=$code;versionName=$Version;minSdk=30;size=(Get-Item -LiteralPath $apk).Length;sha256=(Get-FileHash -LiteralPath $apk -Algorithm SHA256).Hash.ToLowerInvariant();apkUrl="https://github.com/$repo/releases/download/v$Version/Daybreak.apk";notes="Daybreak $Version"}
$manifestPath=Join-Path $staging 'update.json'
$manifest | ConvertTo-Json | Set-Content -LiteralPath $manifestPath -Encoding utf8
$upload=Join-Path $staging 'Daybreak.apk';Copy-Item -LiteralPath $apk -Destination $upload -Force
& gh release create "v$Version" $upload $manifestPath --repo $repo --title "Daybreak $Version" --notes "Daybreak Android update $Version" --latest
if($LASTEXITCODE -ne 0){throw 'Release upload failed; the update channel was not changed.'}
$content=[Convert]::ToBase64String([IO.File]::ReadAllBytes($manifestPath))
$body=@{message="Publish Daybreak $Version update";content=$content}
$existing=(& gh api "repos/$repo/contents/updates/update.json" --jq .sha 2>$null)
if($LASTEXITCODE -eq 0){$body.sha=$existing.Trim()}
$bodyPath=Join-Path $staging 'manifest-put.json';$body | ConvertTo-Json | Set-Content -LiteralPath $bodyPath -Encoding utf8
& gh api "repos/$repo/contents/updates/update.json" --method PUT --input $bodyPath --silent
if($LASTEXITCODE -ne 0){throw 'Release uploaded, but the channel manifest could not be updated.'}
Write-Output "Published Daybreak $Version. Users can check for updates in the app."
