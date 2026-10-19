# Artan Sepet

Too Good To Go'nun Türkiye versiyonu: işletmeler (fırın, restoran, market) gün sonu kalan ürünlerini indirimli **sürpriz paket** olarak satar, müşteriler rezerve edip öder ve belirli saat aralığında teslim alır.

Bu proje bir **Design Patterns dersi** projesidir. Asıl amaç tasarım desenlerini doğru yerde, gerekçesiyle kullanmak. Kod okunabilir ve savunulabilir olmalı: her pattern için "hangi problemi çözüyor, neden burada" sorusuna cevap verebilmeliyiz.

## Teknik kararlar

- Java 21, Maven, JUnit 5
- **Framework yok** (Spring vb. kullanılmıyor). Pattern'ları kendimiz yazıyoruz.
- Arayüz: konsol menüsü (zaman kalırsa JavaFX)
- Veri: bellekte (`DataStore` singleton + `InMemory*Repository`). Gerçek veritabanı yok.
- Ödeme: gerçek API yok, sahte `IyzicoClient` / `PaytrGateway` sınıfları
- CI: GitHub Actions, her push'ta `mvn test`
- Paket kökü: `com.artansepet`

## Pattern haritası

| Pattern | Nerede | Çözdüğü problem |
|---|---|---|
| Builder | `creation.SurpriseBagBuilder` | Paketin çok sayıda opsiyonel alanı var (etiketler, saat aralığı, fiyat stratejisi) |
| Factory Method | `creation.BagFactory` + `Bakery/Restaurant/MarketBagFactory` | İşletme türüne göre varsayılan paket ayarları |
| State | `state.OrderState` + 6 durum sınıfı | Sipariş durumuna göre izinli işlemler değişiyor, if/else yığınını önlüyor |
| Strategy | `pricing.PricingStrategy`, `pricing.BagSortStrategy` | Değiştirilebilir indirim kuralları ve arama sıralaması |
| Observer | `notification.BagSubject` (`Business`), `BagObserver` (`Customer`) | Takip edilen işletme yeni paket ekleyince takipçilere bildirim |
| Adapter | `payment.IyzicoAdapter`, `payment.PaytrAdapter` | Farklı ödeme API'lerini tek `PaymentProcessor` arayüzüne bağlamak |
| Facade | `service.OrderFacade` | Stok + kod üretimi + ödeme + bildirimi tek çağrıda toplamak |
| Singleton | `repository.DataStore` | Uygulama genelinde tek veri deposu |

Repository arayüzleri (`UserRepository`, `BagRepository`, `OrderRepository`) testleri kolaylaştırmak ve depolamayı değiştirilebilir tutmak için var.

## Paket yapısı

```
src/main/java/com/artansepet/
  model/          User, Customer, Business, SurpriseBag, Order, Location, enum'lar
  creation/       Builder + Factory Method
  state/          OrderState ve durumlar
  pricing/        Fiyat ve sıralama stratejileri
  notification/   Observer arayüzleri, NotificationService
  payment/        PaymentProcessor, adapter'lar, sahte 3. parti istemciler
  repository/     DataStore, repository arayüzleri ve InMemory uygulamaları
  service/        OrderFacade, BagService, ExpirationService, PickupCodeGenerator
  exception/      ArtanSepetException ve alt sınıfları
  ui/             Konsol menüsü
src/test/java/com/artansepet/   paket yapısının aynısı
docs/           PlantUML diyagramları (class, use case, sipariş durumları)
```

## İş kuralları

- **Sipariş durumları:** Reserved → Paid → ReadyForPickup → PickedUp. Reserved/Paid → Cancelled. Reserved/Paid/ReadyForPickup → Expired. Ayrıntı: `docs/order-states.puml`.
- Geçersiz bir geçiş `InvalidStateTransitionException` fırlatır. `AbstractOrderState` varsayılan olarak her metotta bunu fırlatır, alt sınıflar sadece izinli geçişleri ezer.
- **Teslim kodu:** Rezervasyonda 6 haneli kod üretilir, işletme teslimde doğrular. Yanlış kod → `InvalidPickupCodeException`, durum değişmez.
- **Süre dolma:** `ExpirationService.expireOverdueOrders(now)` teslim saati geçmiş açık siparişleri `Expired` yapar. Zaman her zaman parametre olarak verilir (`LocalDateTime now` veya `Clock`), test edilebilirlik için `LocalDateTime.now()` doğrudan iş kodunda çağrılmaz.
- **Stok çakışması:** `SurpriseBag.reserveOne()` ve `releaseOne()` `synchronized`. Stok 0 iken rezervasyon `BagSoldOutException` fırlatır. Bunun için eşzamanlı bir test yazılacak.
- İptal veya süre dolmada stok geri bırakılır, ödenmişse iade (`refund`) yapılır.
- Para tutarları `BigDecimal`.

## Kapsam

- **Var:** kayıt/giriş, paket listeleme ve sıralama, takip + bildirim, rezervasyon, ödeme, iptal, teslim kodu, süre dolma, işletmenin paket eklemesi ve sipariş yönetimi
- **Opsiyonel (zaman kalırsa):** puan verme, etki istatistikleri (`ImpactStatsService`)
- **Yok:** gerçek harita, gerçek ödeme, admin paneli, veritabanı

## Takvim (teslim: Aralık sonu, hedef bitiş: ~20 Aralık)

- **Ekim:** kapsam, use case + sınıf diyagramı, repo ve Maven kurulumu ← şu an buradayız
- **1–15 Kasım:** modeller, Builder, Factory Method, Singleton, State, Strategy
- **16–30 Kasım:** Observer, Facade, Adapter, entegrasyon ve testler
- **1–20 Aralık:** konsol arayüzü, uçtan uca testler, rapor ve sunum
- **21–31 Aralık:** tampon (aynı dönemde MLOps projesi de teslim)

## Çalışma şekli

- Her pattern'a başlamadan önce "neden burada bu pattern" kısaca tartışılır, sonra kod yazılır. Kodu toptan üretmek yerine adım adım ilerle, ekip üyeleri sunumda savunabilmeli.
- Her yeni sınıf için JUnit testi yazılır. Pattern davranışı testle gösterilmeli (örneğin State için geçersiz geçiş testi).
- Sınıf yapısı değişirse `docs/class-diagram.puml` da güncellenir.
- Komutlar: `mvn compile`, `mvn test`
