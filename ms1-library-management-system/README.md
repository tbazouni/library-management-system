# Library Management System

A Spring Boot REST API for managing books, authors, borrowers, and borrowing transactions. This project was developed in two stages as part of the Java Developer Code Challenge.

## Project Overview

The application provides REST APIs for library operations, using Spring Boot, Java, PostgreSQL, and Spring Data JPA. It includes CRUD functionality, DTO-based data exchange, validation, logging, and borrowing/returning operations.

## Project Milestones

### Part 1 – Library Management System

The original implementation includes:

- CRUD operations for books, authors, and borrowers.
- Borrowing and returning books.
- Book search by title, category, and author.
- DTOs for request and response data.
- Entity relationships using Spring Data JPA.
- Request validation.
- Logging and centralized exception handling.
- PostgreSQL persistence.

### Part 2 – Project Extension

Part 2 extends the original project with:

- Open Library API integration to retrieve author information by ISBN when creating a book.
- DTO-level validation for book and borrower requests.
- ISBN uniqueness checks and database constraints.
- Configurable maximum active borrowings per borrower.
- Additional Postman tests for the extension requirements.

## Technologies

- Java 21
- Spring Boot
- Spring Web
- Spring Data JPA
- PostgreSQL
- Gradle
- Lombok
- Jakarta Bean Validation
- SLF4J Logging
- Open Library API
- Postman

## Requirements

- Java 21
- PostgreSQL
- IntelliJ IDEA (recommended)
- Postman (for API testing)

## Database Setup

Create a PostgreSQL database:

```sql
CREATE DATABASE library_management_system;
```

Configure your local database connection with `DB_URL`, `DB_USERNAME`, and `DB_PASSWORD` environment variables before starting MS1.

Example:

```powershell
$env:DB_URL = "jdbc:postgresql://localhost:5432/library_management_system"
$env:DB_USERNAME = "postgres"
$env:DB_PASSWORD = "your-local-password"
```

Keep database credentials in environment variables; do not commit local or production passwords.

## Running the Application

1. Start PostgreSQL.
2. Create the `library_management_system` database.
3. Open the project in IntelliJ IDEA.
4. Set `DB_URL`, `DB_USERNAME`, and `DB_PASSWORD` in the run environment.
5. Run `LibraryManagementSystemApplication`.

The API is available at:

`http://localhost:8080`

From the repository root, run the application from Windows PowerShell:

```powershell
.\gradlew.bat :ms1-library-management-system:bootRun
```

## Running Tests

Run MS1 tests from the repository root:

```powershell
.\gradlew.bat :ms1-library-management-system:test
```

## API Endpoints

Base URL:

`http://localhost:8080`

### Authors

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/authors` | Create an author |
| GET | `/api/authors` | Get all authors |
| GET | `/api/authors/{id}` | Get an author by ID |
| PUT | `/api/authors/{id}` | Update an author |
| DELETE | `/api/authors/{id}` | Delete an author |
| GET | `/api/authors/{id}/books` | Get books written by an author |

### Books

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/books` | Create a book |
| GET | `/api/books` | Get all books |
| GET | `/api/books/{id}` | Get a book by ID |
| PUT | `/api/books/{id}` | Update a book |
| DELETE | `/api/books/{id}` | Delete a book |
| GET | `/api/books/search/title?title={title}` | Search books by title |
| GET | `/api/books/search/category?category={category}` | Search books by category |
| GET | `/api/books/search/author/{authorId}` | Get books by author |

### Create a Book – Part 2

When creating a book in Part 2, the client provides the title, ISBN, and category. The application uses the ISBN to retrieve author information from Open Library.

Example request:

```json
{
  "title": "Nineteen Eighty-Four",
  "isbn": "9780451526538",
  "category": "FICTION"
}
```

The author is retrieved from Open Library rather than supplied as an `authorId` in the creation request.

Supported book categories:

`FICTION`, `NON_FICTION`, `SCIENCE`, `HISTORY`, `TECHNOLOGY`, `BIOGRAPHY`, `FANTASY`, `MYSTERY`.

### Borrowers

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/borrowers` | Create a borrower |
| GET | `/api/borrowers` | Get all borrowers |
| GET | `/api/borrowers/{id}` | Get a borrower by ID |
| PUT | `/api/borrowers/{id}` | Update a borrower |
| DELETE | `/api/borrowers/{id}` | Delete a borrower |

Example request:

```json
{
  "name": "Alex Morgan",
  "email": "alex@example.com",
  "phoneNumber": "+96170123456"
}
```

### Borrowing Transactions

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/borrowings/books/{bookId}` | Borrow a book |
| GET | `/api/borrowings` | Get all borrowing transactions |
| GET | `/api/borrowings/{id}` | Get a transaction by ID |
| PUT | `/api/borrowings/{transactionId}/return` | Return a borrowed book |

Example borrow request:

```json
{
  "borrowerId": 1
}
```

When a book is borrowed, its availability is updated and a borrowing transaction is created with the `BORROWED` status.

When it is returned, the transaction status becomes `RETURNED`, the return date is recorded, and the book becomes available again.

## Part 2 Implementation Details

### 1. Open Library Integration

The application integrates with the Open Library Books API:

- Base URL: `https://openlibrary.org`
- Endpoint: `/api/books`
- ISBN lookup using the `bibkeys` parameter
- Response format: JSON
- Data command: `jscmd=data`

The application retrieves the author's name based on the ISBN provided during book creation. It checks whether the author already exists in the database and reuses the existing author; otherwise, it creates a new author record.

If the API does not provide the required author information, the application handles the failure through its business exception mechanism instead of creating an incomplete book record.

### 2. DTO Validation and Data Integrity

Validation is applied to incoming request DTOs.

**Book requests:**
- Title is required.
- ISBN is required.
- Category is required and must be a valid enum value.
- ISBN uniqueness is checked by the service and enforced by the database constraint.

**Borrower requests:**
- Name is required.
- Email is required and must have a valid email format.
- Phone number is required.

Validation errors are handled through the application's exception-handling mechanism.

### 3. Borrowing Transaction Limit

The maximum number of active borrowings per borrower is configurable in `application.properties`:

```properties
borrower.transaction.limit=5
```

Before processing a borrowing request, the application counts the borrower's active transactions and compares the count with the configured limit.

If the borrower has reached the limit, the borrowing request is rejected. Returned transactions are not counted as active borrowings.

## Postman Testing

The project contains Postman collections for testing the API and the challenge requirements.

### Part 1

Import the original Part 1 requirement-based collection:

`Library_Management_System_Part1.json`

### Part 2

Import the Part 2 collection:

`Library_Management_System_Part2.json`

The Part 2 collection includes tests for CRUD operations, Open Library ISBN-based author retrieval, request validation, borrowing and returning books, and the maximum active borrowing limit.

Set the `baseUrl` collection variable to:

`http://localhost:8080`

Run the requests in their intended order. Some tests use collection variables to store IDs returned by earlier requests. Use a clean database or update the test data when repeating the collection.

## Project Structure

The application is organized by feature:

- `author` – author entity, repository, service, controller, and DTOs.
- `book` – book entity, repository, service, controller, and DTOs.
- `borrower` – borrower entity, repository, service, controller, and DTOs.
- `borrowing` – borrowing transaction logic and related components.
- `integration` – external API integration and Open Library DTOs.
- `exception` – custom exceptions and centralized exception handling.

## Implementation Notes

- Book categories are represented using the `BookCategory` enum.
- Spring Data JPA repositories use derived query methods for database operations.
- DTOs separate API request/response data from database entities.
- Jakarta Bean Validation validates incoming request data.
- SLF4J is used for application logging.
- PostgreSQL stores application data.
- Open Library is used to retrieve author information during book creation in Part 2.
- The borrowing limit is controlled through application configuration.

## Repository History

The project is maintained in one repository to preserve its development history.

- `v1.0-part-1` – Original Library Management System.
- `feature/part-2-open-library` – Part 2 extension development branch.
- `v1.1-part-2` – Intended tag for the completed Part 2 extension after merging.

The final `main` branch contains the combined Library Management System from Part 1 and Part 2.

## MS1 to MS2 notification flow

After a borrowing transaction succeeds, MS1 calls MS2 through Spring Cloud OpenFeign. The request contains the borrower's email and the message `Book [title] borrowed successfully`. MS2 exposes `POST /send-email`; its configuration and run instructions are in the [MS2 guide](../ms2-email-notification-service/README.md).
