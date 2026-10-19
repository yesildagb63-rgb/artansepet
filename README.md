# Artan Sepet

Too Good To Go'nun Türkiye versiyonu: işletmeler (fırın, restoran, market) gün sonu kalan ürünlerini indirimli **sürpriz paket** olarak satar; müşteriler rezerve edip öder ve belirli saat aralığında teslim alır.

Bu bir **Design Patterns dersi** projesidir. Java 21, Maven, JUnit 5; framework kullanılmaz, veri bellekte tutulur, ödeme sahtedir.

## Kullanılan tasarım desenleri

| Pattern | Nerede |
|---|---|
| Builder | `creation.SurpriseBagBuilder` |
| Factory Method | `creation.BagFactory` + `Bakery/Restaurant/MarketBagFactory` |
| State | `state.OrderState` + durum sınıfları |
| Strategy | `pricing.PricingStrategy`, `pricing.BagSortStrategy` |
| Observer | `notification.BagSubject` / `BagObserver` |
| Adapter | `payment.IyzicoAdapter`, `payment.PaytrAdapter` |
| Facade | `service.OrderFacade` |
| Singleton | `repository.DataStore` |

Ayrıntılar için [CLAUDE.md](CLAUDE.md) ve `docs/` altındaki PlantUML diyagramlarına bakın.

## Nasıl çalıştırılır

Gereksinim: JDK 21 veya üstü (Maven kurmaya gerek yok, wrapper indirir).

```
# Windows
mvnw.cmd test
mvnw.cmd compile exec:java -Dexec.mainClass=com.artansepet.Main

# Linux / macOS
./mvnw test
```

Her push'ta GitHub Actions `mvn test` çalıştırır (`.github/workflows/ci.yml`).