# 채팅 서비스 REST API 명세서

실시간 채팅 서비스 백엔드의 REST API 및 WebSocket(STOMP) 명세서입니다.

## 기술 스택

| 구분 | 내용 |
| --- | --- |
| Framework | Spring Boot 3.5.16 |
| Language | Java 21 |
| Persistence | Spring Data JPA, MySQL |
| Security | Spring Security (JWT) |
| Realtime | WebSocket (STOMP) |

## 인증 방식

- JWT(JSON Web Token) 기반 인증을 사용합니다.
- 로그인 성공 시 Access Token을 발급합니다.
- 인증이 필요한 API는 `Authorization: Bearer <token>` 헤더를 반드시 포함해야 합니다.
- 토큰이 없거나 유효하지 않은 경우 `401 Unauthorized`를 반환합니다.

---

## 공통 사항

### 공통 요청 헤더

| 헤더 | 필수 | 설명 |
| --- | --- | --- |
| `Content-Type` | 조건부 | 요청 body가 있는 경우 `application/json` 지정 |
| `Authorization` | 조건부 | 인증이 필요한 API에서 `Bearer <token>` 형식으로 지정 |

### 공통 응답 상태 코드

| 상태 코드 | 설명 |
| --- | --- |
| `200 OK` | 요청 성공 |
| `201 Created` | 리소스 생성 성공 |
| `204 No Content` | 성공했으나 응답 본문 없음 |
| `400 Bad Request` | 잘못된 요청 (유효성 검증 실패 등) |
| `401 Unauthorized` | 인증 실패 (토큰 없음/만료/무효) |
| `403 Forbidden` | 권한 없음 |
| `404 Not Found` | 리소스를 찾을 수 없음 |
| `409 Conflict` | 리소스 중복 (이메일 중복, 중복 요청 등) |

### 공통 에러 응답 형식

```json
{
  "timestamp": "2026-07-09T12:34:56",
  "status": 404,
  "error": "Not Found",
  "message": "해당 유저를 찾을 수 없습니다.",
  "path": "/api/users/999"
}
```

---

## 1. 회원 (Users)

### 1.1 회원가입

| 항목 | 내용 |
| --- | --- |
| Method + URL | `POST /api/auth/signup` |
| 설명 | 신규 회원을 등록합니다. |
| 인증 필요 | ❌ |

**요청 body**

```json
{
  "email": "hong@example.com",
  "password": "password1234",
  "nickname": "홍길동"
}
```

**응답 상태 코드**

| 상태 코드 | 설명 |
| --- | --- |
| `201 Created` | 회원가입 성공 |
| `400 Bad Request` | 유효성 검증 실패 (이메일 형식, 비밀번호 규칙 등) |
| `409 Conflict` | 이미 존재하는 이메일 |

**응답 body (201)**

```json
{
  "id": 1,
  "email": "hong@example.com",
  "nickname": "홍길동",
  "status": "ACTIVE",
  "createdAt": "2026-07-09T12:34:56"
}
```

---

### 1.2 로그인

| 항목 | 내용 |
| --- | --- |
| Method + URL | `POST /api/auth/login` |
| 설명 | 이메일/비밀번호로 로그인하고 JWT 토큰을 발급받습니다. |
| 인증 필요 | ❌ |

**요청 body**

```json
{
  "email": "hong@example.com",
  "password": "password1234"
}
```

**응답 상태 코드**

| 상태 코드 | 설명 |
| --- | --- |
| `200 OK` | 로그인 성공 |
| `400 Bad Request` | 필수 값 누락 |
| `401 Unauthorized` | 이메일 또는 비밀번호 불일치 |

**응답 body (200)**

```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "tokenType": "Bearer",
  "expiresIn": 3600,
  "user": {
    "id": 1,
    "email": "hong@example.com",
    "nickname": "홍길동"
  }
}
```

---

### 1.3 로그아웃

| 항목 | 내용 |
| --- | --- |
| Method + URL | `POST /api/auth/logout` |
| 설명 | 현재 토큰을 무효화(서버 측 블랙리스트 처리)합니다. |
| 인증 필요 | ✅ |

**요청 body**

없음

**응답 상태 코드**

| 상태 코드 | 설명 |
| --- | --- |
| `204 No Content` | 로그아웃 성공 |
| `401 Unauthorized` | 인증 실패 |

**응답 body**

없음

---

### 1.4 내 프로필 조회

| 항목 | 내용 |
| --- | --- |
| Method + URL | `GET /api/users/me` |
| 설명 | 로그인한 사용자의 프로필 정보를 조회합니다. |
| 인증 필요 | ✅ |

**요청 body**

없음

**응답 상태 코드**

| 상태 코드 | 설명 |
| --- | --- |
| `200 OK` | 조회 성공 |
| `401 Unauthorized` | 인증 실패 |

**응답 body (200)**

```json
{
  "id": 1,
  "email": "hong@example.com",
  "nickname": "홍길동",
  "profileImageUrl": "https://cdn.example.com/profile/1.png",
  "statusMessage": "안녕하세요",
  "status": "ACTIVE",
  "createdAt": "2026-07-09T12:34:56",
  "updatedAt": "2026-07-09T12:34:56"
}
```

---

### 1.5 내 프로필 수정

| 항목 | 내용 |
| --- | --- |
| Method + URL | `PATCH /api/users/me` |
| 설명 | 닉네임, 프로필 이미지, 상태 메시지를 수정합니다. (부분 수정) |
| 인증 필요 | ✅ |

**요청 body**

```json
{
  "nickname": "새로운닉네임",
  "profileImageUrl": "https://cdn.example.com/profile/1_new.png",
  "statusMessage": "오늘도 화이팅"
}
```

**응답 상태 코드**

| 상태 코드 | 설명 |
| --- | --- |
| `200 OK` | 수정 성공 |
| `400 Bad Request` | 유효성 검증 실패 |
| `401 Unauthorized` | 인증 실패 |

**응답 body (200)**

```json
{
  "id": 1,
  "email": "hong@example.com",
  "nickname": "새로운닉네임",
  "profileImageUrl": "https://cdn.example.com/profile/1_new.png",
  "statusMessage": "오늘도 화이팅",
  "status": "ACTIVE",
  "updatedAt": "2026-07-09T13:00:00"
}
```

---

### 1.6 유저 검색

| 항목 | 내용 |
| --- | --- |
| Method + URL | `GET /api/users/search?keyword={keyword}` |
| 설명 | 닉네임 또는 이메일로 유저를 검색합니다. |
| 인증 필요 | ✅ |

**요청 파라미터**

| 파라미터 | 타입 | 필수 | 설명 |
| --- | --- | --- | --- |
| `keyword` | string | ✅ | 검색어 (닉네임 또는 이메일) |

**응답 상태 코드**

| 상태 코드 | 설명 |
| --- | --- |
| `200 OK` | 검색 성공 (결과가 없어도 빈 배열 반환) |
| `400 Bad Request` | 검색어 누락 |
| `401 Unauthorized` | 인증 실패 |

**응답 body (200)**

```json
[
  {
    "id": 2,
    "email": "kim@example.com",
    "nickname": "김철수",
    "profileImageUrl": "https://cdn.example.com/profile/2.png",
    "statusMessage": "반갑습니다"
  }
]
```

---

## 2. 친구 (Friends)

### 2.1 친구 요청

| 항목 | 내용 |
| --- | --- |
| Method + URL | `POST /api/friends/requests` |
| 설명 | 특정 유저에게 친구 요청을 보냅니다. (status=PENDING 생성) |
| 인증 필요 | ✅ |

**요청 body**

```json
{
  "friendId": 2
}
```

**응답 상태 코드**

| 상태 코드 | 설명 |
| --- | --- |
| `201 Created` | 친구 요청 성공 |
| `400 Bad Request` | 자기 자신에게 요청 등 잘못된 요청 |
| `401 Unauthorized` | 인증 실패 |
| `404 Not Found` | 대상 유저 없음 |
| `409 Conflict` | 이미 친구이거나 이미 요청이 존재함 |

**응답 body (201)**

```json
{
  "id": 10,
  "userId": 1,
  "friendId": 2,
  "status": "PENDING",
  "createdAt": "2026-07-09T13:10:00"
}
```

---

### 2.2 친구 요청 수락

| 항목 | 내용 |
| --- | --- |
| Method + URL | `PATCH /api/friends/requests/{requestId}/accept` |
| 설명 | 받은 친구 요청을 수락합니다. (status=PENDING → ACCEPTED) |
| 인증 필요 | ✅ |

**요청 파라미터**

| 파라미터 | 타입 | 필수 | 설명 |
| --- | --- | --- | --- |
| `requestId` | long | ✅ | 친구 요청 ID (friends.id) |

**요청 body**

없음

**응답 상태 코드**

| 상태 코드 | 설명 |
| --- | --- |
| `200 OK` | 수락 성공 |
| `401 Unauthorized` | 인증 실패 |
| `403 Forbidden` | 본인이 받은 요청이 아님 |
| `404 Not Found` | 친구 요청 없음 |
| `409 Conflict` | 이미 수락된 요청 |

**응답 body (200)**

```json
{
  "id": 10,
  "userId": 2,
  "friendId": 1,
  "status": "ACCEPTED",
  "createdAt": "2026-07-09T13:10:00"
}
```

---

### 2.3 친구 목록 조회

| 항목 | 내용 |
| --- | --- |
| Method + URL | `GET /api/friends` |
| 설명 | 수락된(ACCEPTED) 친구 목록을 조회합니다. |
| 인증 필요 | ✅ |

**요청 파라미터**

| 파라미터 | 타입 | 필수 | 설명 |
| --- | --- | --- | --- |
| `status` | string | ❌ | 필터링 (`ACCEPTED` 기본값, `PENDING` 지정 시 대기 중 요청 조회) |

**응답 상태 코드**

| 상태 코드 | 설명 |
| --- | --- |
| `200 OK` | 조회 성공 |
| `401 Unauthorized` | 인증 실패 |

**응답 body (200)**

```json
[
  {
    "friendshipId": 10,
    "user": {
      "id": 2,
      "nickname": "김철수",
      "profileImageUrl": "https://cdn.example.com/profile/2.png",
      "statusMessage": "반갑습니다"
    },
    "status": "ACCEPTED",
    "createdAt": "2026-07-09T13:10:00"
  }
]
```

---

## 3. 채팅방 (Chat Rooms)

### 3.1 채팅방 생성

| 항목 | 내용 |
| --- | --- |
| Method + URL | `POST /api/chat-rooms` |
| 설명 | 1:1(SINGLE) 또는 그룹(GROUP) 채팅방을 생성합니다. 생성자는 자동으로 참여자에 포함됩니다. |
| 인증 필요 | ✅ |

**요청 body (그룹 채팅)**

```json
{
  "type": "GROUP",
  "title": "프로젝트 팀방",
  "participantIds": [2, 3, 4]
}
```

**요청 body (1:1 채팅)**

```json
{
  "type": "SINGLE",
  "participantIds": [2]
}
```

> `SINGLE` 타입은 `title`이 생략 가능하며, `participantIds`는 상대방 1명만 지정합니다.
> 이미 두 유저 간 1:1 방이 존재하면 새로 생성하지 않고 기존 방을 반환할 수 있습니다.

**응답 상태 코드**

| 상태 코드 | 설명 |
| --- | --- |
| `201 Created` | 채팅방 생성 성공 |
| `400 Bad Request` | 잘못된 요청 (participantIds 누락, type 오류 등) |
| `401 Unauthorized` | 인증 실패 |
| `404 Not Found` | 참여자로 지정한 유저 없음 |
| `409 Conflict` | 이미 존재하는 1:1 채팅방 (기존 방 반환 정책 시) |

**응답 body (201)**

```json
{
  "id": 100,
  "title": "프로젝트 팀방",
  "type": "GROUP",
  "participants": [
    { "userId": 1, "nickname": "홍길동" },
    { "userId": 2, "nickname": "김철수" },
    { "userId": 3, "nickname": "이영희" },
    { "userId": 4, "nickname": "박민수" }
  ],
  "createdAt": "2026-07-09T14:00:00"
}
```

---

### 3.2 내 채팅방 목록 조회

| 항목 | 내용 |
| --- | --- |
| Method + URL | `GET /api/chat-rooms` |
| 설명 | 로그인한 사용자가 참여 중인 채팅방 목록을 조회합니다. 최근 메시지와 읽지 않은 메시지 수를 포함합니다. |
| 인증 필요 | ✅ |

**응답 상태 코드**

| 상태 코드 | 설명 |
| --- | --- |
| `200 OK` | 조회 성공 |
| `401 Unauthorized` | 인증 실패 |

**응답 body (200)**

```json
[
  {
    "id": 100,
    "title": "프로젝트 팀방",
    "type": "GROUP",
    "participantCount": 4,
    "lastMessage": {
      "content": "회의는 3시에 시작합니다.",
      "senderId": 2,
      "createdAt": "2026-07-09T14:20:00"
    },
    "unreadCount": 3
  }
]
```

---

### 3.3 채팅방 나가기

| 항목 | 내용 |
| --- | --- |
| Method + URL | `DELETE /api/chat-rooms/{roomId}/participants/me` |
| 설명 | 채팅방에서 나갑니다. (chat_participants에서 본인 제거) |
| 인증 필요 | ✅ |

**요청 파라미터**

| 파라미터 | 타입 | 필수 | 설명 |
| --- | --- | --- | --- |
| `roomId` | long | ✅ | 채팅방 ID |

**요청 body**

없음

**응답 상태 코드**

| 상태 코드 | 설명 |
| --- | --- |
| `204 No Content` | 나가기 성공 |
| `401 Unauthorized` | 인증 실패 |
| `403 Forbidden` | 해당 방의 참여자가 아님 |
| `404 Not Found` | 채팅방 없음 |

**응답 body**

없음

---

## 4. 메시지 (Messages)

### 4.1 채팅 내역 조회 (페이징)

| 항목 | 내용 |
| --- | --- |
| Method + URL | `GET /api/chat-rooms/{roomId}/messages` |
| 설명 | 특정 채팅방의 메시지 내역을 커서 기반 페이징으로 조회합니다. (최신순) |
| 인증 필요 | ✅ |

**요청 파라미터**

| 파라미터 | 타입 | 필수 | 설명 |
| --- | --- | --- | --- |
| `roomId` | long | ✅ | 채팅방 ID (path) |
| `cursor` | long | ❌ | 이 메시지 ID 이전(더 오래된) 메시지를 조회. 미지정 시 최신부터 |
| `size` | int | ❌ | 페이지 크기 (기본값 20) |

**응답 상태 코드**

| 상태 코드 | 설명 |
| --- | --- |
| `200 OK` | 조회 성공 |
| `401 Unauthorized` | 인증 실패 |
| `403 Forbidden` | 해당 방의 참여자가 아님 |
| `404 Not Found` | 채팅방 없음 |

**응답 body (200)**

```json
{
  "messages": [
    {
      "id": 5021,
      "roomId": 100,
      "senderId": 2,
      "senderNickname": "김철수",
      "content": "회의는 3시에 시작합니다.",
      "createdAt": "2026-07-09T14:20:00"
    },
    {
      "id": 5020,
      "roomId": 100,
      "senderId": 1,
      "senderNickname": "홍길동",
      "content": "네 알겠습니다.",
      "createdAt": "2026-07-09T14:19:30"
    }
  ],
  "nextCursor": 5020,
  "hasNext": true
}
```

---

## 5. WebSocket (STOMP) — 실시간 메시지 송수신

실시간 메시지 송수신은 STOMP 프로토콜 기반 WebSocket으로 처리합니다.

### 5.1 연결 (Connect)

| 항목 | 내용 |
| --- | --- |
| Endpoint | `ws://{host}/ws-stomp` (SockJS 사용 시 `http(s)://{host}/ws-stomp`) |
| 인증 | STOMP `CONNECT` 프레임의 헤더에 `Authorization: Bearer <token>` 포함 |

**CONNECT 프레임 헤더 예시**

```
CONNECT
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
accept-version:1.2
heart-beat:10000,10000
```

### 5.2 구독 (Subscribe)

| 경로 | 설명 |
| --- | --- |
| `/sub/chat-rooms/{roomId}` | 특정 채팅방의 실시간 메시지를 수신 |

**구독 예시**

```
SUBSCRIBE
id:sub-0
destination:/sub/chat-rooms/100
```

### 5.3 발행 (Publish / Send)

| 경로 | 설명 |
| --- | --- |
| `/pub/chat-rooms/{roomId}/messages` | 특정 채팅방으로 메시지를 전송 |

**발행 payload 예시**

```json
{
  "content": "안녕하세요! 회의 준비 되셨나요?"
}
```

> 발신자(senderId)는 서버가 STOMP 세션의 인증 정보(JWT)에서 추출하므로 payload에 포함하지 않습니다.

### 5.4 수신 메시지 payload 예시

구독 중인 클라이언트가 `/sub/chat-rooms/{roomId}`로 수신하는 메시지 형식입니다.

```json
{
  "id": 5022,
  "roomId": 100,
  "senderId": 1,
  "senderNickname": "홍길동",
  "content": "안녕하세요! 회의 준비 되셨나요?",
  "createdAt": "2026-07-09T14:25:00"
}
```

### 5.5 동작 흐름

1. 클라이언트가 `CONNECT` 프레임(+ JWT 헤더)으로 WebSocket 연결
2. `/sub/chat-rooms/{roomId}` 경로를 구독
3. `/pub/chat-rooms/{roomId}/messages`로 메시지 발행
4. 서버가 메시지를 `chat_messages`에 저장
5. 서버가 해당 방을 구독 중인 모든 클라이언트에게 `/sub/chat-rooms/{roomId}`로 브로드캐스트
