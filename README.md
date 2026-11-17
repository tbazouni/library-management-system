# Library Management System

A Spring Boot REST API for managing books, authors, borrowers, and borrowing transactions.

## Requirements

- Java 21
- PostgreSQL
- IntelliJ IDEA (recommended)
- Postman (for API testing)

## Database setup

Create a PostgreSQL database named:

```sql
CREATE DATABASE library_management_system;
```

The current `src/main/resources/application.properties` expects:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/library_management_system
spring.datasource.username=postgres
spring.datasource.password=postgres
spring.jpa.hibernate.ddl-auto=update
server.port=8080
```

Change the username and password to match your local PostgreSQL configuration. Do not commit real database credentials.

## Run the application

1. Start PostgreSQL.
2. Open the project root in IntelliJ IDEA.
3. Make sure the Gradle project is loaded.
4. Check the database connection settings in `application.properties`.
5. Run `LibraryManagementSystemApplication`.
6. The API listens at `http://localhost:8080`.

Alternatively, on Windows PowerShell, run:

```powershell
.\gradlew.bat bootRun
```

Run tests with:

```powershell
.\gradlew.bat test
```

## API endpoints

All endpoints use the base URL `http://localhost:8080`.

### Authors

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/authors` | Create an author |
| GET | `/api/authors` | Get all authors |
| GET | `/api/authors/{id}` | Get an author by ID |
| PUT | `/api/authors/{id}` | Update an author |
| DELETE | `/api/authors/{id}` | Delete an author |
| GET | `/api/authors/{id}/books` | Get books written by an author |

Example create request:

```json
{
  "name": "Mary Shelley",
  "biography": "English novelist."
}
```

### Books

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/books` | Create a book |
| GET | `/api/books` | Get all books |
| GET | `/api/books/{id}` | Get a book by ID |
| PUT | `/api/books/{id}` | Update a book |
| DELETE | `/api/books/{id}` | Delete a book |
| GET | `/api/books/search/title?title={title}` | Search books by title |
| GET | `/api/books/search/category?category={category}` | Filter books by category |
| GET | `/api/books/search/author/{authorId}` | Get books by author ID |

Example create request (create the author first):

```json
{
  "title": "Frankenstein",
  "isbn": "9780486282114",
  "category": "FICTION",
  "authorId": 1
}
```

Supported categories:

`FICTION`, `NON_FICTION`, `SCIENCE`, `HISTORY`, `TECHNOLOGY`, `BIOGRAPHY`, `FANTASY`, `MYSTERY`.

### Borrowers

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/borrowers` | Create a borrower |
| GET | `/api/borrowers` | Get all borrowers |
| GET | `/api/borrowers/{id}` | Get a borrower by ID |
| PUT | `/api/borrowers/{id}` | Update a borrower |
| DELETE | `/api/borrowers/{id}` | Delete a borrower |

Example create request:

```json
{
  "name": "Alex Morgan",
  "email": "alex@example.com",
  "phoneNumber": "+96170123456"
}
```

### Borrowing transactions

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

A successful borrowing changes the book's availability to `false` and creates a transaction with status `BORROWED`. Returning the transaction changes its status to `RETURNED`, records the return date, and makes the book available again.

## Postman testing

Import `Library_Management_System_Requirement_Tests.postman_collection.json` into Postman.

Set `baseUrl` to `http://localhost:8080`, then run the folders in order:

1. Authors
2. Books
3. Borrowers
4. Borrowing Transactions
5. Delete Operations

The collection stores created IDs in collection variables. Some negative tests are intended to reveal incorrect error status codes until exception handling is corrected. Use a clean database or change the sample ISBNs and email addresses before repeating the collection.

## Project structure

The source code is organized by feature (`author`, `book`, `borrower`, and `borrowing`). Each feature contains its entity, controller, service, repository, and DTOs where applicable. Shared exception classes and the global exception handler are in the `exception` package.

## Implementation notes

- Book categories are represented by the `BookCategory` enum.
- Spring Data JPA repositories provide persistence and derived query methods.
- Request and response DTOs are used for API data exchange.
- Jakarta Bean Validation annotations validate request data.
- SLF4J logging is used in service operations.
- PostgreSQL is used as the database.
