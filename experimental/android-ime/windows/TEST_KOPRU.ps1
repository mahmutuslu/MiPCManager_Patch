param(
  [string]$Adb = 'C:\platform-tools\adb.exe',
  [Parameter(Mandatory=$true)][string]$Token
)
$ErrorActionPreference='Stop'
if (!(Test-Path $Adb)) { throw "ADB bulunamadi: $Adb" }
Write-Host "ADB baglanti kontrolu:"
& $Adb devices
Write-Host ""
Write-Host "Yalnizca bir adet Turkce g gonderiliyor. Telefonunuzdaki bos notun yazi alani aktif olmali."
$letter = [char]0x011F
& $Adb shell am broadcast -a com.example.xiaomikeyboardbridge.SEND -n com.example.xiaomikeyboardbridge/.TextReceiver --es token $Token --es char "$letter"
Write-Host ""
Write-Host "100=Android commitText kabul etti; 22=IME aktif degil; 23=aktif yazi baglantisi yok; 21=anahtar yanlis; 26=harf ulasmadi."
Write-Host "Lutfen 'Broadcast completed' satirini ve notta gorunen sonucu bildirin. Anahtarinizi paylasmayin."
