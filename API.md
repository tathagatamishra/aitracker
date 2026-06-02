# All the working endpoints

## Auth

- `POST` /api/auth/register

```json
{ 
    "name": "Test", 
    "email": "test@test.com", 
    "password": "pass123" 
}
```

- `POST` /api/auth/login

```json
{ 
    "email": "test@test.com", 
    "password": "pass123" 
}
```

## Organizations

- `POST` /api/organizations

```json
{ 
    "name": "My Company", 
    "ownerUserId": "<userId>" 
}
```

- `GET` /api/organizations/{orgId}

- `GET` /api/organizations/owner/{userId}

## API Keys

- `POST` /api/api-keys

```json
{ 
    "organizationId": "<orgId>", 
    "provider": "openai", 
    "keyType": "admin", 
    "apiKey": "sk-admin-..." 
}
```

- `GET` /api/api-keys/organization/{orgId}

- `GET` /api/api-keys/{apiKeyId}/decrypt

## Analytics

- `GET` /api/analytics/{orgId}/summary?days=30
