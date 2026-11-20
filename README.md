# Library Management System - Part 3

This Gradle multi-project repository contains two independently runnable Spring Boot services:

- **MS1 - Library Management System**: REST API and PostgreSQL persistence. A successful borrow calls MS2 using Spring Cloud OpenFeign after the database transaction commits.
- **MS2 - Email Notification Service**: validates `POST /send-email` requests and sends messages through Spring Email.

## Project layout

```text
.
|-- ms1-library-management-system/       # Library API, tests, and MS1 Postman collections
|-- ms2-email-notification-service/       # Email API, tests, and Part 3 Postman collection
|-- gradle/                               # Shared Gradle wrapper
|-- build.gradle                          # Root build, test, and clean tasks
|-- settings.gradle                       # MS1/MS2 multi-project setup
```

Open the repository root (`library-management-system-part3`) in IntelliJ so it imports both services. Do not open only an individual service folder.

## Run Part 3 locally

### 1. Start PostgreSQL

Create the database if it does not already exist:

```sql
CREATE DATABASE library_management_system;
```

Configure MS1's connection in the PowerShell session used to start the service. The application reads these environment variables and keeps credentials out of source control:

```powershell
$env:DB_URL = "jdbc:postgresql://localhost:5432/library_management_system"
$env:DB_USERNAME = "postgres"
$env:DB_PASSWORD = "your-local-password"
```

### 2. Start MS2 first

Set the SMTP values for a test inbox or SMTP sandbox, then start MS2:

```powershell
$env:SMTP_HOST = "your-smtp-host"
$env:SMTP_PORT = "587"
$env:SMTP_USERNAME = "your-smtp-username"
$env:SMTP_PASSWORD = "your-smtp-password"
$env:SMTP_AUTH = "true"
$env:SMTP_STARTTLS_ENABLED = "true"
$env:SMTP_STARTTLS_REQUIRED = "true"
$env:MAIL_FROM = "library@example.com"
.\gradlew.bat :ms2-email-notification-service:bootRun
```

MS2 listens on port `8081`. Use the same PowerShell session for its SMTP variables. IntelliJ users can run `EmailNotificationServiceApplication.java` with the same environment variables in the run configuration.

### 3. Start MS1

In another terminal, set the database variables above and start MS1:

```powershell
.\gradlew.bat :ms1-library-management-system:bootRun
```

MS1 listens on port `8080` and calls `http://localhost:8081` by default. Set `EMAIL_SERVICE_URL` if MS2 uses another address. IntelliJ users can run `LibraryManagementSystemApplication.java` with `DB_URL`, `DB_USERNAME`, and `DB_PASSWORD` configured.

### 4. Import and run the Part 3 Postman collection

Import [Part3-Email-Notification.postman_collection.json](ms2-email-notification-service/postman/Part3-Email-Notification.postman_collection.json) into Postman. Set the collection variable `testEmail` to an inbox you control. The default `testIsbn` is an Open Library ISBN; change it if that ISBN already exists in your database.

Run requests `01` through `03` to test MS2 delivery and validation. Then run `05` through `09` in order to create a borrower and book, borrow it through MS1, verify the transaction, and return the book. Confirm the successful borrow message in the MS2 logs and your SMTP test inbox:

```text
Book <your book title> borrowed successfully
```

Request `04` is for the SMTP outage case: restart MS2 with SMTP pointed at an unavailable host/port, then send it; MS2 should return `503`. For the MS1 outage case, run request `10` only after request `09`, with MS2 stopped. The borrow should still return `201`; MS1 logs the delivery failure because the borrowing transaction has already committed. Restart MS2 afterward.

MS1 currently logs failed notifications but does not persist or retry them. An outbox/retry worker would be needed for guaranteed redelivery.

## Integration tests

Run both services' tests from the repository root:

```powershell
.\gradlew.bat test
```

The MS1 integration test starts the real MS1 HTTP application, performs a borrow, and verifies that Feign sends the notification body over HTTP. MS2 integration tests send HTTP requests to a running embedded MS2 instance and verify delivery into a local SMTP test inbox, validation errors, and the `503` response when SMTP is unavailable. These tests use a local test inbox; they do not verify delivery through your external mail provider.

Run one service's tests with `.\gradlew.bat :ms1-library-management-system:test` or `.\gradlew.bat :ms2-email-notification-service:test`.

## Clean generated files

Run `.\gradlew.bat clean` to remove build outputs from both services. Gradle caches and build directories are ignored by Git. To also remove local Gradle caches, close IntelliJ and delete the root and service `.gradle` directories; Gradle recreates them on the next build.
