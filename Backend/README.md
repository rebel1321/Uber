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
