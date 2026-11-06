# Xiaomi 6.0.2.19 Türkçe Q IME köprüsü (deneysel)
Bu Android APK, Xiaomi'nin sistem APK'sına veya MiScreenShare.exe dosyasına dokunmaz. Windows tarafındaki yardımcı program yalnızca Türkçe özel harfleri Android InputConnection.commitText ile göndermeyi dener. **Telefon üzerinde test edilmedi, çalışması garanti değildir.**
## Derleme
GitHub Actions içindeki **Build Android IME APK** iş akışı Android SDK 35 kullanır ve **debug APK** üretir. İndirilen artifact ZIP içerisindeki app-debug.apk test amaçlıdır, üretim imzalı uygulama değildir.
## Kurulum
APK'yı yalnızca güvendiğiniz cihazda test edin. Android'de uygulamayı açın, giriş yöntemini etkinleştirip seçin. Windows yardımcı betiğine uygulamada görünen anahtarı verin. Test sonrası eski giriş yöntemini seçin.
## Riskler
Bu bir IME'dir ve kullanıcı girişlerine erişme yetkisi vardır. Özellikle parola ve kişisel metinlerde kullanmayın. Windows köprüsü ADB üzerinden çalışır ve gecikme/sıralama sorunları yaşayabilir. Yalnızca boş test notunda deneyin. Herhangi bir başarı doğrulanmamıştır.
