# MS2 - Email Notification Service

MS2 is a standalone Spring Boot service that accepts a notification request and sends it through Spring Email. It runs on port `8081` by default.

## API

`POST /send-email`

```json
{
  "email": "user@example.com",
  "message": "Book The Hobbit borrowed successfully"
}
```

MS1 calls this endpoint after a successful borrow using Spring Cloud OpenFeign. Set `EMAIL_SERVICE_URL` in MS1 to change the MS2 base URL; the default is `http://localhost:8081`.

## Run

From the repository root:

```powershell
.\gradlew.bat :ms2-email-notification-service:bootRun
```

Configure SMTP through environment variables before starting MS2:

```powershell
$env:SMTP_HOST = "smtp.example.com"
$env:SMTP_PORT = "587"
$env:SMTP_USERNAME = "your-smtp-username"
$env:SMTP_PASSWORD = "your-smtp-password"
$env:SMTP_AUTH = "true"
$env:SMTP_STARTTLS_ENABLED = "true"
$env:SMTP_STARTTLS_REQUIRED = "true"
$env:MAIL_FROM = "library@example.com"
```

Defaults use localhost SMTP on port `1025`, which supports local development but will not deliver email unless an SMTP server is available. Keep real mail credentials outside the repository.

## Build and test

From the repository root:

```powershell
.\gradlew.bat :ms2-email-notification-service:build
.\gradlew.bat :ms2-email-notification-service:test
```
