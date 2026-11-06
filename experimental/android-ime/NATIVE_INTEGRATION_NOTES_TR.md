# Xiaomi 6.0.2.19 — doğrudan Türkçe Q entegrasyonu incelemesi

## Korunacak çalışan referans

Kullanıcı Windows fiziksel Türkçe Q + Xiaomi PC Manager 6.0.2.19 ekran paylaşımı + Android köprü IME + Windows V6.9 ile `ğ ü ş ı i ö ç` ve büyük harflerini, Caps Lock'u, bazı Shift/AltGr noktalamalarını ve normal yazı hızını cihazda doğruladı.

**Bu deneysel kaynaklar silinmemeli veya geriye dönük bozulmamalı.** Yerleşik entegrasyon ayrıca incelenmelidir.

## Yerel ikili dosya incelemesi (6 Kasım 2026)

Orijinal dosya `MiSmartShare/MiScreenShare.exe` (27038296 bayt), SHA256 `25DF4D1EFA6E62FA840C7CF22B70A17F9CB2B18C1AE57F3AE24FEDE03F62FE53`.

- Dosya offset `0xB107F` civarında, `cn` dizgesi dahil dil/bölge koşullarından sonraki `WM_CHAR` karakter işleme bölümü görülüyor.
- Karakter kabulünde `r13w` aralığı `>=0x20`, `!=0x7f` biçiminde kontrol ediliyor.
- Kod `0xFDE9` sabitini tür/mesaj kodu olarak hazırlayıp yardımcı bir işleve iletiyor.
- V4'ün bölge/karakter geçidine yaptığı müdahale ve V5'in keydown/keyup süzgeci cihazda metnin birbirinin yerine geçmesine yol açmıştı. Bu, Android tarafındaki kesin hatanın bulunduğu anlamına gelmiyor.
- V6.9'un başarılı sonucu Android `InputConnection.commitText()` yoluyla elde edildi. Bunun Xiaomi'nin yerleşik `0xFDE9` alıcısıyla birebir aynı işlem olduğu **kanıtlanmadı**.

## Yerleşik entegrasyonun tamamlanması için gerekli kanıt

1. `0xFDE9` mesajının alıcı tarafında hangi IME/input yoluna bağlandığını gerçek Android çağrı zincirinde eşleştir.
2. Mesajın *metin ekleme* mi yoksa *composing metnini güncelleme* mi yaptığına dair çalışma zamanı veya kod kanıtı elde et.
3. Yalnızca somut protokol kanıtından sonra orijinal EXE üzerinde hash kontrollü, yedeklemeli ve geri alınabilir patch hazırla.
4. V6.9 eşdeğerlik testi: Unicode, Shift, Caps Lock, AltGr, kesintisiz yazma, normal tuşlar, kısayollar ve çalışan bildirim sistemi.
5. APK/ADB/PowerShell olmadan gerçek cihazda doğrulanana kadar yerleşik entegrasyonu tamamlanmış olarak sunma.

**Durum: araştırma. Yerleşik yama henüz yok.**
