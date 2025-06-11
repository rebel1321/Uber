# /api/user/register

**Description**  
Creates a new user in the system.

**Method**  
POST

**Request Body**

```json
{
  "fullName": {
    "firstName": "required, string, at least 3 characters",
    "lastName": "optional, string, at least 3 characters"
  },
  "email": "required, valid email address",
  "password": "required, string, at least 6 characters"
}
```

**Responses**

- **201**: User created successfully, returns JSON with `token` and `user`
- **400**: Validation error, returns JSON with `{ errors: [...] }`


## Example Successful Response (201)
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJfaWQiOiI2ODQ2YmViN2ZlMzk4ZGE2ZTQ4OTEwZjgiLCJpYXQiOjE3NDk0NjY4MDgsImV4cCI6MTc0OTQ3MDQwOH0.WJ11gROSTsAqkFpws1JtKDaaAZlE2zD3ju8shrphSTs",
    "user": {
        "fullName": {
            "firstName": "Divyansh",
            "lastName": "Gupta"
        },
        "email": "divyansh.gupta@example.com",
        "password": "$2b$10$sSZ11fGMqnE8LW3GlPN0wOEkV2u.gHW/cjYHRW8lqcUkIfZdKSRjO",
  }
}
```

## Example Error Response (400)
```json
{
  "errors": [
    {
      "msg": "Password must be at least 6 characters long",
      "param": "password",
      "location": "body"
    }
  ]
}
```

## /api/user/login

**Description**  
Logs in an existing user.

**Method**  
POST

**Request Body**
```json
{
  "email": "required, valid email address",
  "password": "required, string, at least 6 characters"
}
```

**Responses**  
- **200**: User logged in successfully, returns JSON with `token` and `user`  
- **400**: Validation error, returns JSON with `{ errors: [...] }`  
- **401**: Invalid email or password  

### Example Successful Response (200)
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.abc123",
  "user": {
    "_id": "6486bec9fe398da6e48910f8",
    "fullName": {
      "firstName": "Divyansh",
      "lastName": "Gupta"
    },
    "email": "divyansh.gupta@example.com"
  }
}
```

### Example Validation Error Response (400)
```json
{
  "errors": [
    {
      "msg": "Invalid email address",
      "param": "email",
      "location": "body"
    }
  ]
}
```

### Example Invalid Credentials Response (401)
```json
{
  "message": "Invalid email or password"
}
```

## /api/user/profile

**Description**  
Fetches the authenticated user's profile (requires authentication).

**Method**  
GET

**Headers**  
- `Authorization`: Bearer <token>

**Responses**  
- **200**: Returns the authenticated user's profile  
- **401**: Unauthorized if no valid token is provided  

### Example Successful Response (200)

```json
{
  "user": {
    "_id": "6486bec9fe398da6e48910f8",
    "fullName": {
      "firstName": "Divyansh",
      "lastName": "Gupta"
    },
    "email": "divyansh.gupta@example.com"
  },
  "message": "User profile fetched successfully"
}
```

### Example Unauthorized Response (401)
```json
{
  "message": "Unauthorized"
}
```

## /api/user/logout

**Description**  
Logs out the authenticated user (requires authentication).

**Method**  
GET

**Headers**  
- `Authorization`: Bearer <token>

**Responses**  
- **200**: User logged out successfully  
- **401**: Unauthorized if no valid token is provided  

### Example Successful Response (200)

```json
{
  "message": "User logged out successfully"
}
```

### Example Unauthorized Response (401)
```json
{
  "message": "Unauthorized"
}
```

## /api/captain/register

**Description**  
Registers a new captain.

**Method**  
POST

**Request Body**
```json
{
  "email": "required, valid email address",
  "password": "required, string, at least 6 characters",
  "fullName": {
    "firstName": "required, string, at least 3 characters",
    "lastName": "optional, string"
  },
  "vehicle": {
    "color": "required, string, at least 3 characters",
    "plate": "required, string, at least 3 characters",
    "capacity": "required, at least 1 character",
    "vehicleType": "required, must be one of: car, motorcycle, auto"
  }
}
```

**Responses**  
- **201**: Captain registered successfully, returns JSON with captain data  
- **400**: Validation error, returns JSON with `{ errors: [...] }`  

### Example Successful Response (201)
```json
{
  "token": "captainAuthToken",
  "captain": {
    "_id": "64b12e6902ab98ca235f52f1",
    "fullName": {
      "firstName": "Jane",
      "lastName": "Doe"
    },
    "email": "jane.doe@example.com",
    "vehicle": {
      "color": "Blue",
      "model": "Sedan",
      "plate": "ABC123",
      "capacity": "4",
      "vehicleType": "car"
    }
  }
}
```

### Example Error Response (400)
```json
{
  "errors": [
    {
      "msg": "Invalid email address",
      "param": "email",
      "location": "body"
    }
  ]
}
```
