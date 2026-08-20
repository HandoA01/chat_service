# 트러블 슈팅

8~9주차 실습(공통 응답 규격, 전역 에러 핸들러, @Valid, 조인 상속 매핑)을 진행하면서
실제로 겪은 문제들. **이슈 - 문제 - 해결** 순서로 정리했다.

---

## No.1 — 실패 응답인데 `isSuccess`가 `true`로 내려옴

**`이슈`**

👉 에러 핸들러를 다 붙이고 예외를 던져봤는데, 응답 body의 `isSuccess`가 `true`였다.
HTTP 상태 코드는 404가 맞게 나가는데 body만 성공이라고 말하고 있었다.

**`문제`**

워크북의 `ApiResponse` 예시 코드를 그대로 옮겼는데, 실패 응답을 만드는 메서드 안에서
`isSuccess` 자리에 `true`가 들어가 있었다.

```java
public static <T> ApiResponse<T> onFailure(String code, String message, T data){
    return new ApiResponse<>(true, code, message, data);  // ← 실패인데 true
}
```

프론트가 `isSuccess`만 보고 분기하면 에러를 성공으로 처리하게 된다.
응답 통일의 목적 자체를 깨는 버그였다.

**`해결`**

`false`로 수정. 자료의 오타로 판단했고, 워크북 본문도 뒤쪽에서 "오타 수정도 합시다"라고
언급하고 있어서 근거가 맞다고 봤다.

---

## No.2 — `@Valid` 실패 응답에 enum 이름이 그대로 노출됨

**`이슈`**

👉 커스텀 어노테이션 `@NotDuplicateEmail`을 만들고 중복 이메일로 회원가입을 시도했더니
응답이 이렇게 나왔다.

```json
{
  "isSuccess": false,
  "code": "COMMON400",
  "message": "잘못된 요청입니다.",
  "result": { "email": "USER_EMAIL_DUPLICATED" }
}
```

`USER_EMAIL_DUPLICATED`라는 **자바 enum 상수 이름이 그대로 클라이언트에 노출**됐고,
`code`도 `USER4002`가 아니라 `COMMON400`으로 뭉개졌다. HTTP 상태도 409가 아닌 400.

**`문제`**

Validator는 `context.buildConstraintViolationWithTemplate(ErrorStatus.XXX.toString())`으로
**enum 이름을 메시지에 심어둔다.** 핸들러가 그걸 `ErrorStatus`로 되돌려줘야 의미가 생긴다.

그런데 되돌리는 코드가 `ConstraintViolationException` 핸들러에만 있었다.
`@Valid`가 붙은 `@RequestBody` 검증은 이 예외가 아니라
`MethodArgumentNotValidException`으로 올라오는데, 그쪽 핸들러는 필드 에러를 그냥
`Map<필드, 메시지>`로 만들어 `_BAD_REQUEST`에 담아버리고 있었다.

즉 **커스텀 어노테이션이 `@Valid` 경로로 들어오면 복원이 안 되는 구멍**이 있었다.

**`해결`**

`handleMethodArgumentNotValid`에서 필드 에러 메시지 중 `ErrorStatus`의 상수 이름으로
해석되는 게 있으면 그 도메인 에러로 응답하도록 고쳤다.

```java
Optional<ErrorStatus> domainError = errors.values().stream()
        .map(this::toErrorStatus)      // valueOf 실패하면 Optional.empty()
        .flatMap(Optional::stream)
        .findFirst();

if (domainError.isPresent()) {
    return handleExceptionInternalConstraint(e, domainError.get(), HttpHeaders.EMPTY, request);
}
// 해당 없으면 기존대로 필드별 메시지 맵을 그대로 내려준다
```

`valueOf`는 매칭 실패 시 `IllegalArgumentException`을 던지므로 감싸서 `Optional`로 바꿨다.
`@NotBlank`, `@Email` 같은 표준 어노테이션의 사람 말 메시지는 그대로 필드 맵으로 남는다.

결과:

```json
{ "isSuccess": false, "code": "USER4002", "message": "이미 사용 중인 이메일입니다." }   // 409
```

---

## No.3 — `code`는 201인데 실제 HTTP 상태는 200

**`이슈`**

👉 회원가입 성공 응답의 `code`가 `USER201`인데, 실제 응답 상태 코드는 `200 OK`였다.
명세서에는 `201 Created`로 적어뒀는데 서로 안 맞았다.

**`문제`**

컨트롤러가 `ApiResponse` 객체만 반환하고 있었다. `ApiResponse` 안의 `code`는 우리가 정한
문자열일 뿐이고, **HTTP 상태 코드를 바꾸는 힘이 없다.** 지정하지 않으면 Spring은 200을 쓴다.

워크북 예제는 성공 API가 전부 조회라 200이 맞아서 이 문제가 드러나지 않았다.

**`해결`**

생성 API에 `@ResponseStatus(HttpStatus.CREATED)`를 붙였다.

```java
@ResponseStatus(HttpStatus.CREATED)
@PostMapping("/signup")
public ApiResponse<UserResponseDTO.JoinResultDTO> join(@RequestBody @Valid ...) { ... }
```

`ResponseEntity`로 감싸는 방법도 있지만, 모든 컨트롤러의 반환 타입이 `ApiResponse<T>`로
통일돼 있는 게 이 프로젝트의 규칙이라 어노테이션 쪽을 택했다.

---

## No.4 — `@SuperBuilder`를 붙였더니 `BaseEntityBuilder`를 못 찾음

**`이슈`**

👉 메시지를 조인 상속으로 바꾸면서 자식 엔티티가 부모 필드(`room`, `sender`)까지
빌더로 받게 하려고 `@SuperBuilder`를 붙였더니 컴파일이 깨졌다.

```
ChatMessage.java:31: error: cannot find symbol
@SuperBuilder
^
  symbol:   class BaseEntityBuilder
  location: class BaseEntity
```

**`문제`**

`@SuperBuilder`는 **상속 계층 전체가 `@SuperBuilder`여야** 동작한다.
자식이 부모의 빌더(`BaseEntityBuilder`)를 이어받는 구조라서, 부모에 없으면 참조할 게 없다.
`ChatMessage`의 부모는 `BaseEntity`인데 거기엔 `@Builder`조차 없었다.

**`해결`**

`BaseEntity`에 `@SuperBuilder`를 추가했다. 다만 이것만 붙이면 두 번째 문제가 생긴다.
`@SuperBuilder`가 빌더용 생성자를 만들면서 **암묵적 기본 생성자가 사라지고**,
`@NoArgsConstructor(access = PROTECTED)`로 `super()`를 호출하던 다른 엔티티들이 전부 깨진다.

그래서 `@NoArgsConstructor(access = AccessLevel.PROTECTED)`도 같이 붙였다.

```java
@Getter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity { ... }
```

기존 엔티티들은 `@Builder`를 그대로 써도 문제없다. `BaseEntity`의 `createdAt`/`updatedAt`은
어차피 JPA Auditing이 채우는 값이라 빌더로 받을 일이 없다.

---

## No.5 — 조인 상속 후 특정 메시지만 `type`과 `content`가 비어서 내려옴

**`이슈`**

👉 메시지를 `@Inheritance(JOINED)`로 바꾸고 채팅 내역을 조회했더니,
5개 중 **딱 한 건만** 종류별 필드가 통째로 비어 있었다.

```
 5005 MEDIA   -     https://.../study.png
 5004 EMOJI   -     https://.../20.png
 5003 TEXT    5002  18시입니다
 5002 None    -     (비어있음!)     ← 얘만
 5001 TEXT    -     안녕하세요
```

DB에는 `text_messages`에 5002의 `content`가 멀쩡히 들어 있었다.

**`문제`**

컨버터가 `instanceof`로 자식 타입을 분기하고 있었다.

```java
if (message instanceof TextMessage textMessage) { ... }
```

그런데 **5002는 5003의 답장 대상**이다. `ChatMessage.parentMessage`가 LAZY라서,
5003을 로딩할 때 Hibernate가 5002를 **`ChatMessage` 타입의 프록시**로 먼저 만들어
영속성 컨텍스트에 등록한다. 이후 같은 트랜잭션에서 5002 행을 조회해도
동일성 보장 때문에 **이미 등록된 프록시가 반환**된다.

프록시 클래스는 `TextMessage`가 아니라 **`ChatMessage`를 상속**한다.
그래서 `instanceof TextMessage`가 false가 되고, 모든 분기를 빠져나가
아무 필드도 안 채워진 DTO가 만들어졌다.

정렬이 최신순이라 5003(답장)이 5002보다 먼저 처리된 것도 조건이 맞아떨어진 이유였다.

**`해결`**

분기 전에 프록시를 벗겨냈다.

```java
public static MessagePreviewDTO toMessagePreviewDTO(ChatMessage rawMessage) {
    // 답장 대상으로 먼저 참조된 메시지는 ChatMessage 프록시로 로딩되어 있을 수 있다.
    // 프록시는 자식 타입이 아니라 부모 타입을 상속하므로, 벗겨내지 않으면 instanceof가 전부 빗나간다.
    ChatMessage message = (ChatMessage) Hibernate.unproxy(rawMessage);
    ...
}
```

**배운 것**: 상속 매핑 + LAZY 연관관계 환경에서 `instanceof`나 `getClass()` 비교는 위험하다.
`equals()`를 직접 구현할 때 `getClass()`를 쓰면 안 되고 `instanceof`를 쓰라는 조언도 같은 뿌리다.

---

## No.6 — 메시지 목록 조회 한 번에 users 조회가 메시지 수만큼 더 나감 (N+1)

**`이슈`**

👉 `show-sql`을 켜고 채팅 내역 조회 API를 한 번 호출했더니,
메시지 목록 쿼리 1번 + count 1번 외에 `users` 단건 조회가 여러 번 반복해서 찍혔다.

```sql
select ... from users u1_0 where u1_0.id=?
select ... from users u1_0 where u1_0.id=?
select ... from users u1_0 where u1_0.id=?
```

**`문제`**

응답 DTO에 `senderNickname`이 있어서 컨버터가 `message.getSender().getNickname()`을 호출한다.
`sender`는 `@ManyToOne(fetch = FetchType.LAZY)`라 그 시점에 프록시가 초기화되면서
**메시지 한 건당 users 쿼리 한 방**이 나간다. 전형적인 N+1이다.

메시지 20건이면 쿼리가 22번, 100건이면 102번이 된다.
LAZY 자체는 옳은 선택이었고(메시지 하나 조회할 때마다 방/발신자까지 딸려오면 안 되므로),
**필요한 순간에 같이 가져오지 않은 것**이 문제였다.

**`해결`**

리포지토리 메서드에 `@EntityGraph`를 붙여 `sender`를 함께 조회하도록 했다.

```java
@EntityGraph(attributePaths = {"sender"})
Page<ChatMessage> findAllByRoom(ChatRoom room, Pageable pageable);
```

적용 후 같은 요청에서 users 단건 조회 **0회**. `fetch join`을 쓰는 방법도 있지만
`Page` 반환 시 count 쿼리를 따로 지정해야 해서, 페이징과 잘 맞는 `@EntityGraph`를 택했다.

---

## No.7 — 커밋에 이전 버전 내용이 들어감 (환경 문제)

**`이슈`**

👉 파일을 수정하고 커밋했는데, 커밋된 내용이 수정 전 버전이었다.
어떤 파일은 문장 중간에서 잘려 있기도 했다.

**`문제`**

디스크 사용량이 99%(여유 3.3GB)였고, Gradle도
`resourceHashesCache.bin is corrupt. Discarding.` 경고를 냈다.
편집한 파일이 저장 후 이전 내용으로 되돌아가는 현상이 반복됐고,
그 상태에서 `git add`가 실행되어 옛 내용이 스테이징됐다.

**`해결`**

커밋 전에 **스테이징된 내용과 디스크 내용이 같은지 검증**하는 절차를 넣었다.

```bash
for f in $(git diff --cached --name-only); do
  [ "$(wc -c < $f | tr -d ' ')" = "$(git cat-file -s :$f)" ] || echo "MISMATCH $f"
done
```

불일치가 나오면 파일을 다시 쓰고 재스테이징한다.
근본 원인 쪽은 디스크 정리와, 편집 중인 파일을 붙잡고 있는 에디터 탭을 닫는 것으로 대응.

---

## No.8 — `@RequestParam` 검증 실패가 401로 둔갑함

**`이슈`**

👉 유저 검색 API에 검색어를 비우고 호출했더니, 400이 아니라 **401 Unauthorized**가 나왔다.
토큰은 정상이었고, 같은 토큰으로 다른 API는 잘 되는 상황이었다.

```json
{ "isSuccess": false, "code": "COMMON401", "message": "인증이 필요합니다." }
```

서버 로그에는 인증과 무관한 예외가 찍혀 있었다.

```
ConstraintViolationException: search.keyword: 검색어는 필수입니다.
  at ...ExceptionTranslationFilter.doFilter(...)
```

**`문제`**

`ExceptionAdvice`의 `ConstraintViolationException` 핸들러가 이렇게 돼 있었다.

```java
return handleExceptionInternalConstraint(e, ErrorStatus.valueOf(errorMessage), ...);
```

**모든 위반 메시지가 `ErrorStatus`의 enum 이름이라고 가정**한 코드다.
커스텀 어노테이션(`@CheckPage` 등)은 실제로 enum 이름을 심으니 문제가 없었는데,
표준 어노테이션인 `@NotBlank(message = "검색어는 필수입니다.")`는 사람이 읽는 문구를 담는다.

그 문구로 `valueOf`를 호출하니 **핸들러 안에서 `IllegalArgumentException`이 터졌다.**
예외를 처리하려던 핸들러가 스스로 예외를 던진 것이라, 응답이 만들어지지 못하고
예외가 서블릿 밖 필터 체인까지 올라갔다. 거기서 Spring Security의
`ExceptionTranslationFilter`를 지나며 인증 문제로 오인되어 401로 응답됐다.

**증상(401)과 원인(검증 실패)이 전혀 달라서** 토큰을 계속 의심하느라 시간을 썼다.

**`해결`**

enum으로 해석되는 경우와 아닌 경우를 나눴다.

```java
Optional<ErrorStatus> domainError = toErrorStatus(errorMessage);
if (domainError.isPresent()) {
    return handleExceptionInternalConstraint(e, domainError.get(), HttpHeaders.EMPTY, request);
}

// 표준 어노테이션 메시지는 필드별 맵으로 그대로 내려준다
Map<String, String> errors = new LinkedHashMap<>();
e.getConstraintViolations().forEach(violation ->
        errors.merge(lastNodeOf(violation), violation.getMessage(), ...));
return handleExceptionInternalArgs(e, HttpHeaders.EMPTY, ErrorStatus._BAD_REQUEST, request, errors);
```

`propertyPath`가 `search.keyword`처럼 메서드명까지 포함하므로 마지막 노드만 잘라 필드명으로 썼다.

결과:

```json
{ "isSuccess": false, "code": "COMMON400", "message": "잘못된 요청입니다.",
  "result": { "keyword": "검색어는 필수입니다." } }      // 400
```

**배운 것**: **예외 핸들러 자신이 예외를 던지면 안 된다.**
핸들러가 실패하면 그 뒤에 무슨 응답이 나갈지 통제할 수 없고,
이번처럼 원인과 전혀 다른 상태 코드가 나가 디버깅을 방해한다.
`valueOf`처럼 실패 가능한 변환은 핸들러 안에서 반드시 감싸야 한다.
