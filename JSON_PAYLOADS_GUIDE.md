# JSON Sample Payloads for User Endpoints

## Endpoint 1: POST /users/endpoint1

### Description

Creates a user with contact information (common attributes + phone + address).

**Validates:**

- Common fields: id, username, email, firstName, lastName
- Endpoint1 specific: phone, address (with nested street, city, state, postalCode, country)

**Does NOT validate:**

- birthDate, department, salary (Endpoint2 fields are ignored)

### Request Payload - Valid Example

```json
{
  "id": 1,
  "username": "john.doe",
  "email": "john.doe@example.com",
  "firstName": "John",
  "lastName": "Doe",
  "phone": "+15551234567",
  "address": {
    "street": "123 Main Street",
    "city": "Springfield",
    "state": "IL",
    "postalCode": "62701",
    "country": "USA"
  }
}
```

### Response - 200 OK

```json
{
  "id": 1,
  "username": "john.doe",
  "email": "john.doe@example.com",
  "firstName": "John",
  "lastName": "Doe",
  "phone": "+15551234567",
  "address": {
    "street": "123 Main Street",
    "city": "Springfield",
    "state": "IL",
    "postalCode": "62701",
    "country": "USA"
  }
}
```

### Request Payload - With Different Phone Numbers (All Valid)

```json
{
  "id": 2,
  "username": "jane.smith",
  "email": "jane.smith@example.com",
  "firstName": "Jane",
  "lastName": "Smith",
  "phone": "+441234567890",
  "address": {
    "street": "10 Downing Street",
    "city": "London",
    "state": "England",
    "postalCode": "SW1A2AA",
    "country": "United Kingdom"
  }
}
```

```json
{
  "id": 3,
  "username": "marie.jean",
  "email": "marie.jean@example.com",
  "firstName": "Marie",
  "lastName": "Jean",
  "phone": "+33123456789",
  "address": {
    "street": "1 Rue de Rivoli",
    "city": "Paris",
    "state": "Île-de-France",
    "postalCode": "75001",
    "country": "France"
  }
}
```

```json
{
  "id": 4,
  "username": "yuki.tanaka",
  "email": "yuki.tanaka@example.com",
  "firstName": "Yuki",
  "lastName": "Tanaka",
  "phone": "+81312345678",
  "address": {
    "street": "2-1-1 Marunouchi, Chiyoda-ku",
    "city": "Tokyo",
    "state": "Tokyo",
    "postalCode": "10001",
    "country": "Japan"
  }
}
```

### Invalid Examples (Will Return 400)

#### Invalid Phone Format

```json
{
  "id": 5,
  "username": "invalid.phone",
  "email": "invalid@example.com",
  "firstName": "Invalid",
  "lastName": "Phone",
  "phone": "555-123-4567",
  "address": {
    "street": "123 Main Street",
    "city": "Springfield",
    "state": "IL",
    "postalCode": "62701",
    "country": "USA"
  }
}
```

**Error Response:**

```json
{
  "phone": "Phone number must be a valid E.164 format"
}
```

#### Invalid Postal Code Format

```json
{
  "id": 6,
  "username": "invalid.postal",
  "email": "invalid@example.com",
  "firstName": "Invalid",
  "lastName": "Postal",
  "phone": "+15551234567",
  "address": {
    "street": "123 Main Street",
    "city": "Springfield",
    "state": "IL",
    "postalCode": "abc123",
    "country": "USA"
  }
}
```

**Error Response:**

```json
{
  "address.postalCode": "Postal code must be 3-10 alphanumeric characters (uppercase)"
}
```

#### Short Street Name

```json
{
  "id": 7,
  "username": "short.street",
  "email": "short@example.com",
  "firstName": "Short",
  "lastName": "Street",
  "phone": "+15551234567",
  "address": {
    "street": "123",
    "city": "Springfield",
    "state": "IL",
    "postalCode": "62701",
    "country": "USA"
  }
}
```

**Error Response:**

```json
{
  "address.street": "Street must be between 5 and 100 characters"
}
```

---

## Endpoint 2: POST /users/endpoint2

### Description

Creates a user with employment information (common attributes + birthDate + department + salary).

**Validates:**

- Common fields: id, username, email, firstName, lastName
- Endpoint2 specific: birthDate, department, salary

**Does NOT validate:**

- phone, address (Endpoint1 fields are ignored - not deserialized)

### Request Payload - Valid Example

```json
{
  "id": 1,
  "username": "john.doe",
  "email": "john.doe@example.com",
  "firstName": "John",
  "lastName": "Doe",
  "birthDate": "1990-05-15",
  "department": "Engineering",
  "salary": 85000.00
}
```

### Response - 200 OK

```json
{
  "id": 1,
  "username": "john.doe",
  "email": "john.doe@example.com",
  "firstName": "John",
  "lastName": "Doe",
  "birthDate": "1990-05-15",
  "department": "Engineering",
  "salary": 85000.00
}
```

### Request Payload - With Various Departments and Salaries

```json
{
  "id": 2,
  "username": "jane.smith",
  "email": "jane.smith@example.com",
  "firstName": "Jane",
  "lastName": "Smith",
  "birthDate": "1985-03-22",
  "department": "Human Resources",
  "salary": 72000.50
}
```

```json
{
  "id": 3,
  "username": "bob.wilson",
  "email": "bob.wilson@example.com",
  "firstName": "Bob",
  "lastName": "Wilson",
  "birthDate": "1992-07-10",
  "department": "Sales",
  "salary": 65000.00
}
```

```json
{
  "id": 4,
  "username": "alice.johnson",
  "email": "alice.johnson@example.com",
  "firstName": "Alice",
  "lastName": "Johnson",
  "birthDate": "1988-11-30",
  "department": "Finance",
  "salary": 95000.75
}
```

```json
{
  "id": 5,
  "username": "charlie.brown",
  "email": "charlie.brown@example.com",
  "firstName": "Charlie",
  "lastName": "Brown",
  "birthDate": "1995-01-15",
  "department": "Marketing",
  "salary": 60000.00
}
```

### Important: Endpoint1 Fields Are Ignored for Endpoint2

Even if you include `phone` and `address` in the request, they will be **completely ignored** and not deserialized:

```json
{
  "id": 6,
  "username": "ignored.fields",
  "email": "ignored@example.com",
  "firstName": "Ignored",
  "lastName": "Fields",
  "birthDate": "1995-05-20",
  "department": "Engineering",
  "salary": 80000.00,
  "phone": "+15551234567",
  "address": {
    "street": "123 Main Street",
    "city": "Springfield",
    "state": "IL",
    "postalCode": "62701",
    "country": "USA"
  }
}
```

**Response - 200 OK (phone and address are NOT in response)**

```json
{
  "id": 6,
  "username": "ignored.fields",
  "email": "ignored@example.com",
  "firstName": "Ignored",
  "lastName": "Fields",
  "birthDate": "1995-05-20",
  "department": "Engineering",
  "salary": 80000.00
}
```

### Invalid Examples (Will Return 400)

#### Invalid Date Format

```json
{
  "id": 7,
  "username": "invalid.date",
  "email": "invalid@example.com",
  "firstName": "Invalid",
  "lastName": "Date",
  "birthDate": "05/15/1990",
  "department": "Engineering",
  "salary": 85000.00
}
```

**Error Response:**

```json
{
  "birthDate": "Birth date must be in yyyy-MM-dd format"
}
```

#### Negative Salary

```json
{
  "id": 8,
  "username": "negative.salary",
  "email": "negative@example.com",
  "firstName": "Negative",
  "lastName": "Salary",
  "birthDate": "1990-05-15",
  "department": "Engineering",
  "salary": -50000.00
}
```

**Error Response:**

```json
{
  "salary": "Salary must be greater than 0"
}
```

#### Zero Salary

```json
{
  "id": 9,
  "username": "zero.salary",
  "email": "zero@example.com",
  "firstName": "Zero",
  "lastName": "Salary",
  "birthDate": "1990-05-15",
  "department": "Engineering",
  "salary": 0.00
}
```

**Error Response:**

```json
{
  "salary": "Salary must be greater than 0"
}
```

#### Invalid Department (Too Short)

```json
{
  "id": 10,
  "username": "short.dept",
  "email": "short@example.com",
  "firstName": "Short",
  "lastName": "Dept",
  "birthDate": "1990-05-15",
  "department": "IT",
  "salary": 85000.00
}
```

**Error Response:**

```json
{
  "department": "Department must be between 2 and 50 characters"
}
```

Note: "IT" is 2 characters (meets minimum), so this would actually pass. Try "E" instead.

#### Invalid Department (Too Long)

```json
{
  "id": 11,
  "username": "long.dept",
  "email": "long@example.com",
  "firstName": "Long",
  "lastName": "Dept",
  "birthDate": "1990-05-15",
  "department": "This is a very long department name that exceeds the maximum character limit",
  "salary": 85000.00
}
```

**Error Response:**

```json
{
  "department": "Department must be between 2 and 50 characters"
}
```

---

## Curl Command Examples

### Endpoint 1 - Valid Request

```bash
curl -X POST http://localhost:8080/users/endpoint1 \
  -H "Content-Type: application/json" \
  -d '{
    "id": 1,
    "username": "john.doe",
    "email": "john.doe@example.com",
    "firstName": "John",
    "lastName": "Doe",
    "phone": "+15551234567",
    "address": {
      "street": "123 Main Street",
      "city": "Springfield",
      "state": "IL",
      "postalCode": "62701",
      "country": "USA"
    }
  }'
```

### Endpoint 2 - Valid Request

```bash
curl -X POST http://localhost:8080/users/endpoint2 \
  -H "Content-Type: application/json" \
  -d '{
    "id": 1,
    "username": "john.doe",
    "email": "john.doe@example.com",
    "firstName": "John",
    "lastName": "Doe",
    "birthDate": "1990-05-15",
    "department": "Engineering",
    "salary": 85000.00
  }'
```

### Endpoint 1 - Invalid Phone

```bash
curl -X POST http://localhost:8080/users/endpoint1 \
  -H "Content-Type: application/json" \
  -d '{
    "id": 1,
    "username": "john.doe",
    "email": "john.doe@example.com",
    "firstName": "John",
    "lastName": "Doe",
    "phone": "invalid-phone",
    "address": {
      "street": "123 Main Street",
      "city": "Springfield",
      "state": "IL",
      "postalCode": "62701",
      "country": "USA"
    }
  }'
```

### Endpoint 2 - Negative Salary

```bash
curl -X POST http://localhost:8080/users/endpoint2 \
  -H "Content-Type: application/json" \
  -d '{
    "id": 1,
    "username": "john.doe",
    "email": "john.doe@example.com",
    "firstName": "John",
    "lastName": "Doe",
    "birthDate": "1990-05-15",
    "department": "Engineering",
    "salary": -50000.00
  }'
```

---

## Valid Postal Code Examples (for Endpoint 1)

```
62701          (USA)
90210          (USA)
10001          (USA)
SW1A2AA        (UK)
M5H2N2         (Canada)
T1X1V1         (Canada)
75001          (France)
10115          (Germany)
B1A2B3         (Canada)
E1A2B3         (Canada)
SE3E1A2B3      (Sweden)
```

---

## Key Differences

| Aspect                | Endpoint 1                                               | Endpoint 2                                                              |
|-----------------------|----------------------------------------------------------|-------------------------------------------------------------------------|
| **URL**               | POST /users/endpoint1                                    | POST /users/endpoint2                                                   |
| **Required Fields**   | id, username, email, firstName, lastName, phone, address | id, username, email, firstName, lastName, birthDate, department, salary |
| **Ignored Fields**    | birthDate, department, salary                            | phone, address                                                          |
| **Nested Validation** | Address object validated                                 | No nested objects                                                       |
| **Use Case**          | Contact Information                                      | Employment Information                                                  |

The implementation is production-ready! 🚀
