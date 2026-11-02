# 🏨 Hotel Booking System (Microservices Architecture)

Распределенное микросервисное приложение для бронирования номеров в отелях. 
Проект демонстрирует применение паттерна Saga с компенсацией, Circuit Breaker, JWT-аутентификацию, API Gateway маршрутизацию и динамическое обнаружение сервисов.

---

## 🏗 Архитектурная схема

```mermaid
graph TD
    Client[Клиент / Postman / Swagger] -->|HTTP + JWT Token| GW[API Gateway :8080]
    
    GW -->|Маршрутизация lb://| BS[Booking Service :8082]
    GW -->|Маршрутизация lb://| HS[Hotel Service :8081]
    
    BS -.->|OpenFeign + CircuitBreaker + Retry| HS
    
    BS & HS -->|Регистрация экземпляров| EU[(Eureka Server :8761)]
    
    BS -->|JPA / Hibernate| DB1[(H2 In-Memory Booking DB)]
    HS -->|JPA / Hibernate| DB2[(H2 In-Memory Hotel DB)]
    
    subgraph "Безопасность"
    GW -.->|Валидация JWT и проброс заголовков| BS
    GW -.->|Валидация JWT и проброс заголовков| HS
    end
🛠 Технологический стек
Java: 17
Framework: Spring Boot 3.2.5, Spring Cloud 2023.0.1
Безопасность: Spring Security, JWT (io.jsonwebtoken 0.12.5)
Данные: Spring Data JPA, Hibernate, H2 Database (in-memory)
Микросервисы: Eureka Server, Spring Cloud Gateway, OpenFeign
Отказоустойчивость: Resilience4j (Circuit Breaker, Retry)
Утилиты: Lombok, MapStruct, SpringDoc OpenAPI (Swagger)

🚀 Как запустить проект
Предварительные требования
JDK 17 или выше
Maven 3.9+
Важно: Проект настроен с флагом компилятора <parameters>true</parameters> для корректной работы OpenFeign.
Пошаговый запуск
Рекомендуется запускать сервисы в следующем порядке (каждый в отдельном терминале или вкладке IDE):
Сборка проекта (в корневой папке):
bash
   mvn clean install -DskipTests
Eureka Server (порт 8761):
bash
   cd eureka-server
   mvn spring-boot:run
Hotel Service (порт 8081):
bash
   cd hotel-service
   mvn spring-boot:run
Booking Service (порт 8082):
bash
   cd booking-service
   mvn spring-boot:run
API Gateway (порт 8080):
bash
   cd api-gateway
   mvn spring-boot:run
После запуска все сервисы автоматически зарегистрируются в Eureka. Проверьте: http://localhost:8761

🔑 Предзаполненные данные

При старте Booking Service создает тестовых пользователей:
Admin: username: admin, password: admin123, role: ADMIN
User: username: user, password: user123, role: USER
При старте Hotel Service создаются 2 отеля и 8 номеров с разным уровнем загруженности (times_booked).
📡 Основные API Endpoints

Все запросы (кроме регистрации/авторизации) требуют заголовок: Authorization: Bearer <ваш_токен>
Аутентификация (Booking Service)
POST /api/user/register — Регистрация нового пользователя
POST /api/user/auth — Авторизация и получение JWT токена
Бронирования (Booking Service)
POST /api/bookings — Создание бронирования (запускает Saga)
GET /api/bookings — История бронирований текущего пользователя (с пагинацией)
DELETE /api/bookings/{id} — Отмена бронирования
Отели и номера (Hotel Service)
GET /api/hotels — Список всех отелей
GET /api/rooms — Список всех доступных номеров
GET /api/rooms/recommend — Рекомендуемые номера (отсортированы по возрастанию times_booked для равномерной загрузки)
Swagger документация доступна по адресам:
Hotel Service: http://localhost:8081/swagger-ui.html
Booking Service: http://localhost:8082/swagger-ui.html

🧪 Тестирование
Для запуска модульных и интеграционных тестов выполните в корне проекта:
bash
mvn clean test
Тесты покрывают сценарии успешного бронирования, срабатывания Circuit Breaker, компенсации (отката) транзакции при сбое и проверки прав доступа (Role-based access control).

📚 Architecture Decision Records (ADR)
ADR-1: Выбор JWT для аутентификации в микросервисах
Контекст: Требуется stateless-аутентификация, при которой каждый микросервис может независимо проверять права пользователя без обращения к центральной базе сессий.
Решение: Использован JWT (HS256). API Gateway валидирует токен и прокидывает X-User-Id и X-User-Role во внутренние сервисы. Сами сервисы также имеют фильтр валидации JWT для защиты от прямых вызовов в обход шлюза.
Преимущества: Отсутствие состояния (stateless), высокая производительность, простота масштабирования.

ADR-2: Паттерн Saga с компенсацией вместо 2PC
Контекст: Создание бронирования требует атомарности между двумя разными базами данных (Booking и Hotel). Распределенные транзакции (2PC) плохо масштабируются и блокируют ресурсы.
Решение: Реализована хореографическая Saga:
Booking Service создает запись со статусом PENDING.
Вызывает Hotel Service для блокировки номера.
При успехе статус меняется на CONFIRMED.
При ошибке или таймауте срабатывает компенсация: статус меняется на CANCELLED, и вызывается эндпойнт releaseRoom для снятия блокировки.
Идемпотентность: Достигается за счет передачи уникального requestId, который кэшируется в ConcurrentHashMap на стороне Hotel Service, предотвращая двойные списания.

ADR-3: Алгоритм равномерного распределения загрузки номеров
Контекст: Необходимо избежать ситуации, когда одни номера простаивают, а другие изнашиваются из-за постоянного бронирования.
Решение: Введена метрика times_booked. Эндпойнт /api/rooms/recommend возвращает свободные номера, отсортированные по возрастанию times_booked (при равенстве — по id).
Преимущества: Простая и эффективная эвристика, не требующая сложных статистических моделей, но гарантирующая равномерную эксплуатацию фонда.

⚠️ Устранение неполадок (Troubleshooting)
Ошибка Feign "Name for argument not specified": Убедитесь, что в корневом pom.xml в maven-compiler-plugin установлен параметр <parameters>true</parameters>, и выполните mvn clean compile.
Проблемы с кодировкой: Все Java-файлы сохранены в кодировке UTF-8 без BOM.