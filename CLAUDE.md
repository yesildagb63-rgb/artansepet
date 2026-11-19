# Artan Sepet

Too Good To Go'nun Türkiye versiyonu: işletmeler (fırın, pastane, kafe, restoran, market, manav, otel) gün sonu kalan ürünlerini indirimli **sürpriz paket** olarak satar, müşteriler rezerve edip öder ve belirli saat aralığında teslim alır.

Bu proje bir **Design Patterns dersi** projesidir. Asıl amaç tasarım desenlerini doğru yerde, gerekçesiyle kullanmak. Kod okunabilir ve savunulabilir olmalı: her pattern için "hangi problemi çözüyor, neden burada" sorusuna cevap verebilmeliyiz.

## Teknik kararlar

- Java 21, Maven (Maven Wrapper ile), JUnit 5
- **Framework yok** (Spring vb. kullanılmıyor). Pattern'ları kendimiz yazıyoruz.
- Arayüz: konsol menüsü (zaman kalırsa JavaFX)
- Veri: bellekte (`DataStore` singleton + `InMemory*Repository`). Gerçek veritabanı yok.
- Ödeme: gerçek API yok, sahte `IyzicoClient` / `PaytrGateway` sınıfları
- CI: GitHub Actions, her push'ta `./mvnw -B test`
- Paket kökü: `com.artansepet`

## Pattern haritası (8 desenin hepsi tamamlandı)

| Pattern | Nerede | Çözdüğü problem |
|---|---|---|
| Builder | `model.SurpriseBag.Builder` (iç sınıf) | Paketin çok sayıda opsiyonel alanı var; tüm kontroller `build()`'de tek yerde |
| Factory Method | `creation.BagFactory` + 7 alt fabrika | İşletme türüne göre varsayılan paket ayarları; yeni tür = yeni sınıf |
| Singleton | `repository.DataStore` (Holder yöntemi) | Uygulama genelinde tek veri deposu |
| State | `state.OrderState` + 6 durum sınıfı | Sipariş durumuna göre izinli işlemler değişiyor, if/else yığınını önlüyor |
| Strategy | `pricing.PricingStrategy` (3), `pricing.BagSortStrategy` (3) | Değiştirilebilir indirim kuralları ve arama sıralaması |
| Observer | `notification.BagSubject` (`Business`), `BagObserver` (`Customer`) | Takip edilen işletme yeni paket ekleyince takipçilere bildirim |
| Adapter | `payment.IyzicoAdapter`, `payment.PaytrAdapter` | Farklı ödeme API'lerini tek `PaymentProcessor` arayüzüne bağlamak |
| Facade | `service.OrderFacade` | Stok + teslim kodu + ödeme + geri alma + bildirimi tek çağrıda toplamak |

Repository arayüzleri (`UserRepository`, `BagRepository`, `OrderRepository`) testleri kolaylaştırmak ve depolamayı değiştirilebilir tutmak için var. `BagService` ve `ExpirationService` yeni desen değil, mevcut desenleri birbirine bağlayan servisler.

## Tasarımdan bilinçli sapmalar (raporda anlatılacak)

- Builder ayrı sınıf değil, `SurpriseBag`'in içinde: private constructor'a erişebilmesi için.
- İşletme türü 3'ten 7'ye çıktı (pastane, kafe, manav, otel eklendi). Kasap/balıkçı gıda güvenliği nedeniyle kapsam dışı.
- Ödeme State'ten ayrıldı: `Order.markPaid(transactionId)` sadece geçişi yapar, ödemeyi `OrderFacade` alır. State, Adapter'a bağımlı değil.
- Durum metotları bir sonraki durumu döndürür (`state = state.cancel(this)`); `Order`'da dışarıya açık `setState` yok.
- `NotificationService` yazılmadı; bildirimler `Customer.addNotification` ile tutuluyor.

## Paket yapısı

```
src/main/java/com/artansepet/
  model/          User, Customer, Business, SurpriseBag (+Builder), Order, Location, enum'lar
  creation/       BagFactory ve 7 alt fabrika
  state/          OrderState, AbstractOrderState ve 6 durum
  pricing/        Fiyat ve sıralama stratejileri
  notification/   BagSubject, BagObserver
  payment/        PaymentProcessor, PaymentResult, adapter'lar, sahte 3. parti istemciler
  repository/     DataStore, repository arayüzleri ve InMemory uygulamaları
  service/        OrderFacade, BagService, ExpirationService, PickupCodeGenerator
  exception/      ArtanSepetException ve alt sınıfları
  ui/             Konsol menüsü (yapılacak)
src/test/java/com/artansepet/   paket yapısının aynısı
docs/           PlantUML diyagramları (class, use case, sipariş durumları)
```

**Dikkat:** main ve test altında aynı isimli klasörler var. Uygulama kodu `src/main`, test kodu `src/test` altına gider; yeni dosya oluştururken editörün yol çubuğunu kontrol et.

## İş kuralları

- **Sipariş durumları:** Reserved → Paid → ReadyForPickup → PickedUp. Reserved/Paid → Cancelled. Reserved/Paid/ReadyForPickup → Expired. ReadyForPickup iptal edilemez. Ayrıntı: `docs/order-states.puml`.
- Geçersiz bir geçiş `InvalidStateTransitionException` fırlatır. `AbstractOrderState` varsayılan olarak her metotta bunu fırlatır, alt sınıflar sadece izinli geçişleri ezer.
- **Teslim kodu:** Rezervasyonda 6 haneli kod üretilir (`SecureRandom`), işletme teslimde doğrular. Yanlış kod → `InvalidPickupCodeException`, durum değişmez.
- **Sipariş fiyatı** rezervasyon anında sabitlenir; sonradan indirim artsa bile değişmez.
- **Süre dolma:** `ExpirationService.expireOverdueOrders(now)` teslim saati geçmiş açık siparişleri `Expired` yapar. Zaman her zaman parametre olarak verilir; `LocalDateTime.now()` iş kodunda çağrılmaz.
- **Stok çakışması:** `SurpriseBag.reserveOne()` ve `releaseOne()` `synchronized`. Stok 0 iken rezervasyon `BagSoldOutException` fırlatır. 100 thread'li eşzamanlılık testi var.
- İptal veya süre dolmada stok geri bırakılır. Ödenmiş sipariş iptalinde `OrderFacade` önce durumu değiştirir, sonra iade yapar. Ödeme başarısızsa sipariş iptal edilip stok geri verilir.
- Para tutarları `BigDecimal`. E-posta küçük harfe `Locale.ROOT` ile çevrilir (Türkçe `I` → `ı` hatası yaşandı ve düzeltildi).

## Kapsam

- **Var:** paket yayınlama ve takip bildirimi, paket arama ve sıralama, rezervasyon, ödeme, iptal ve iade, teslim kodu, süre dolma
- **Yapılacak:** konsol arayüzü (kayıt/giriş, müşteri ve işletme menüleri), demo verisi
- **Opsiyonel (zaman kalırsa):** puan verme, etki istatistikleri, iyzico sandbox, JaCoCo kapsam raporu
- **Yok:** gerçek harita, gerçek ödeme, admin paneli, veritabanı

## Takvim (teslim: Aralık sonu, hedef bitiş: ~20 Aralık)

- **Ekim:** kapsam, UML, repo ve Maven kurulumu ✅
- **Kasım:** 8 desen, `BagService`, `ExpirationService`, 129 test ✅
- **1–10 Aralık:** konsol arayüzü ve demo verisi ← şu an buradayız
- **10–20 Aralık:** rapor ve sunum
- **21–31 Aralık:** tampon (aynı dönemde MLOps projesi de teslim)

## Çalışma şekli

- Her yeni parça için önce "neden" kısaca tartışılır, sonra kod yazılır. Kodu toptan üretmek yerine adım adım ilerle, ekip üyeleri sunumda savunabilmeli.
- Her yeni sınıf için JUnit testi yazılır. Pattern davranışı testle gösterilmeli.
- Sınıf yapısı değişirse `docs/class-diagram.puml` da güncellenir.
- Komutlar (Windows): `.\mvnw.cmd compile`, `.\mvnw.cmd test`
