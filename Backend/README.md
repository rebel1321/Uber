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

### Example Successful Response (201)
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "user": {
    "fullName": {
      "firstName": "Divyansh",
      "lastName": "Gupta"
    },
    "email": "divyansh.gupta@example.com",
    "password": "$2b$10$..."
  }
}
```

### Example Error Response (400)
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

---

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

---

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

---

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

---

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

---

## /api/captain/login

**Description**  
Logs in an existing captain.

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
- **200**: Captain logged in successfully, returns JSON with `token` and `captain`  
- **400**: Validation error, returns JSON with `{ errors: [...] }`

### Example Successful Response (200)
```json
{
  "token": "captainAuthToken552",
  "captain": {
    "_id": "64b1ec6892cab85f235f52f2",
    "fullName": {
      "firstName": "Alice",
      "lastName": "Smith"
    },
    "email": "alice.smith@example.com",
    "vehicle": {
      "color": "Red",
      "plate": "XYZ789",
      "capacity": "2",
      "vehicleType": "motorcycle"
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

---

## /api/captain/profile

**Description**  
Fetches the authenticated captain's profile (requires authentication).

**Method**  
GET

**Headers**  
- `Authorization`: Bearer <token>

**Responses**  
- **200**: Returns the authenticated captain's profile  
- **401**: Unauthorized if no valid token is provided  

### Example Successful Response (200)
```json
{
  "captain": {
    "_id": "64b1ec6892cab85f235f52f2",
    "fullName": {
      "firstName": "Alice",
      "lastName": "Smith"
    },
    "email": "alice.smith@example.com",
    "vehicle": {
      "color": "Red",
      "plate": "XYZ789",
      "capacity": "2",
      "vehicleType": "moto"
    }
  }
}
```

### Example Unauthorized Response (401)
```json
{
  "message": "Unauthorized"
}
```

---

## /api/captain/logout

**Description**  
Logs out the authenticated captain (requires authentication).

**Method**  
GET

**Headers**  
- `Authorization`: Bearer <token>

**Responses**  
- **200**: Captain logged out successfully  
- **401**: Unauthorized if no valid token is provided  

### Example Successful Response (200)
```json
{
  "message": "Logged out successfully"
}
```

### Example Unauthorized Response (401)
```json
{
  "message": "Unauthorized"
}
```
MAP ROUTES
---

## /api/maps/get-coordinates

**Description**  
Get latitude and longitude for a given address.

**Method**  
GET

**Headers**  
- `Authorization`: Bearer <token>

**Query Parameters**  
- `address`: (required, string, min 3 characters) The address to geocode.

**Responses**  
- **200**: Returns coordinates and formatted address  
- **400**: Validation error  
- **404**: Coordinates not found

### Example Successful Response (200)
```json
{
  "latitude": 28.6328,
  "longitude": 77.2197,
  "formatted_address": "Connaught Place, New Delhi, Delhi, India"
}
```

### Example Error Response (400)
```json
{
  "errors": [
    {
      "msg": "Invalid value",
      "param": "address",
      "location": "query"
    }
  ]
}
```

---

## /api/maps/get-distance-time

**Description**  
Get distance and estimated time between two addresses.

**Method**  
GET

**Headers**  
- `Authorization`: Bearer <token>

**Query Parameters**  
- `origin`: (required, string, min 3 characters) Origin address  
- `destination`: (required, string, min 3 characters) Destination address

**Responses**  
- **200**: Returns distance and time data  
- **400**: Validation error or missing parameters  
- **404**: Distance and time not found

### Example Successful Response (200)
```json
{
  "distance": {
    "text": "12.3 km",
    "value": 12345
  },
  "duration": {
    "text": "25 mins",
    "value": 1500
  }
}
```

### Example Error Response (400)
```json
{
  "message": "Origin and destination are required"
}
```

---

## /api/maps/get-suggestions

**Description**  
Get autocomplete suggestions for a location input.

**Method**  
GET

**Headers**  
- `Authorization`: Bearer <token>

**Query Parameters**  
- `input`: (required, string, min 3 characters) The partial address or place name

**Responses**  
- **200**: Returns an array of suggestion objects  
- **400**: Validation error  
- **404**: Suggestions not found

### Example Successful Response (200)
```json
[
  {
    "description": "Connaught Place, New Delhi, Delhi, India",
    "place_id": "ChIJL_P_CXMEDTkRw0ZdG-0GVvw"
  },
  {
    "description": "Connaught Circus, New Delhi, Delhi, India",
    "place_id": "ChIJL_P_CXMEDTkRw0ZdG-0GVvx"
  }
]
```

### Example Error Response (400)
```json
{
  "errors": [
    {
      "msg": "Invalid value",
      "param": "input",
      "location": "query"
    }
  ]
}
```

---

## /api/rides/create

**Description**  
Create a new ride request for a user.

**Method**  
POST

**Headers**  
- `Authorization`: Bearer <token>

**Request Body**  
```json
{
  "pickup": "required, string, at least 3 characters",
  "destination": "required, string, at least 3 characters",
  "vehicleType": "required, one of: auto, car, moto"
}
```

**Responses**  
- **201**: Ride created successfully, returns ride details  
- **400**: Validation error, returns JSON with `{ errors: [...] }`  
- **500**: Failed to create ride

### Example Successful Response (201)
```json
{
  "_id": "65a1b2c3d4e5f6a7b8c9d0e1",
  "user": "6486bec9fe398da6e48910f8",
  "pickup": "Connaught Place, New Delhi",
  "destination": "India Gate, New Delhi",
  "fare": 120,
  "otp": "123456",
  "status": "pending",
  "__v": 0
}
```

### Example Error Response (400)
```json
{
  "errors": [
    {
      "msg": "Invalid pickup location",
      "param": "pickup",
      "location": "body"
    }
  ]
}
```

### Example Error Response (500)
```json
{
  "error": "Failed to create ride request"
}
```
