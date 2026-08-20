# 미션 기록 — 조사 정리

8주차 미션 4·5번(❗필수❗ 항목 포함)과 7~9주차 핵심 키워드 조사.
가능한 한 이 프로젝트의 실제 코드를 근거로 정리했다.

---

# 1. RestControllerAdvice의 장점, 없으면 무엇이 불편한가 ❗필수❗

## 무엇인가

`@RestControllerAdvice` = `@ControllerAdvice` + `@ResponseBody`.
여러 컨트롤러에서 발생한 예외를 **한 곳에서 가로채** 처리하고, 그 결과를
JSON 본문으로 직렬화해 응답한다.

내부적으로는 Spring MVC의 `HandlerExceptionResolver` 체인 중
`ExceptionHandlerExceptionResolver`가 `@ExceptionHandler` 메서드를 찾아 호출하는 구조다.
컨트롤러 안의 `@ExceptionHandler`는 그 컨트롤러에만 적용되지만,
`@RestControllerAdvice` 안의 것은 **전역**으로 적용된다.

이 프로젝트에서는 `ExceptionAdvice`가 그 역할을 한다.

```java
@Slf4j
@RestControllerAdvice(annotations = {RestController.class})
public class ExceptionAdvice extends ResponseEntityExceptionHandler {

    @ExceptionHandler(value = GeneralException.class)
    public ResponseEntity<Object> onThrowException(GeneralException e, HttpServletRequest request) {
        ErrorReasonDTO reason = e.getErrorReasonHttpStatus();
        return handleExceptionInternal(e, reason, null, request);
    }
    ...
}
```

## 장점

**1) 예외 처리 코드가 비즈니스 로직에서 완전히 분리된다 (횡단 관심사 분리)**

서비스는 "이건 잘못된 상황이다"라고 **던지기만** 하면 된다.
어떤 HTTP 상태로, 어떤 JSON 모양으로 나갈지는 전혀 신경 쓰지 않는다.

```java
// service - 상황만 표현한다
User user = userRepository.findById(userId)
        .orElseThrow(() -> new UserHandler(ErrorStatus.USER_NOT_FOUND));
```

컨트롤러에도 try-catch가 한 줄도 없다.

**2) 응답 형식이 강제로 통일된다**

모든 에러가 `ExceptionAdvice`라는 하나의 출구를 지나므로, 거기서 `ApiResponse.onFailure()`로
감싸는 코드를 **한 번만** 쓰면 전체 API의 에러 응답 형식이 같아진다.
새 API를 아무리 추가해도 형식이 어긋날 수가 없다.

**3) 에러 정책을 한 파일에서 바꿀 수 있다**

"검증 실패 시 필드별 사유를 `result`에 담자"는 정책을 바꾸고 싶으면
`handleMethodArgumentNotValid` 한 곳만 고치면 된다. 컨트롤러 수와 무관하다.

실제로 이 프로젝트에서 "커스텀 어노테이션이 심은 `ErrorStatus` 이름을 도메인 에러로 복원"하는
로직을 나중에 추가했는데, **컨트롤러는 한 줄도 건드리지 않았다.**

**4) 프레임워크가 던지는 예외까지 잡을 수 있다**

`MethodArgumentNotValidException`(@Valid 실패), `HttpMessageNotReadableException`(JSON 파싱 실패),
`MethodArgumentTypeMismatchException`(타입 불일치) 등은 **컨트롤러 메서드가 실행되기도 전에**
터진다. 컨트롤러 안의 try-catch로는 잡을 방법이 아예 없다.
`ResponseEntityExceptionHandler`를 상속하면 이런 표준 예외들을 오버라이드해서 처리할 수 있다.

**5) 예외 → HTTP 상태 매핑이 한눈에 보인다**

`ErrorStatus` enum + `ExceptionAdvice` 두 파일만 보면 이 API가 어떤 에러를 어떤 코드로
내려주는지 전부 파악된다. 신규 투입자에게도, 프론트 개발자에게도 문서 역할을 한다.

## 없으면 무엇이 불편한가

**1) 모든 컨트롤러 메서드가 try-catch로 뒤덮인다**

```java
// ExceptionAdvice가 없다면...
@PostMapping("/signup")
public ResponseEntity<?> join(@RequestBody UserRequestDTO.JoinDTO request) {
    try {
        User user = userCommandService.joinUser(request);
        return ResponseEntity.status(201).body(ApiResponse.of(SuccessStatus.USER_JOINED, ...));
    } catch (EmailDuplicatedException e) {
        return ResponseEntity.status(409).body(ApiResponse.onFailure("USER4002", "이미 사용 중인 이메일입니다.", null));
    } catch (UserNotFoundException e) {
        return ResponseEntity.status(404).body(ApiResponse.onFailure("USER4001", "해당 회원을 찾을 수 없습니다.", null));
    } catch (Exception e) {
        return ResponseEntity.status(500).body(ApiResponse.onFailure("COMMON500", "서버 에러", null));
    }
}
```

핵심 로직은 한 줄인데 예외 처리가 열 줄이다. **API가 20개면 이게 20번 반복된다.**

**2) 복붙 과정에서 응답 형식이 갈라진다**

어떤 API는 `{"error": "..."}`, 어떤 API는 `{"message": "..."}`, 어떤 API는 상태 코드만 다르게…
워크북이 지적한 *"하나의 API의 응답이 성공/실패에서 양식이 다름"* 문제가 그대로 재현된다.
프론트는 API마다 다르게 파싱해야 한다.

**3) 정책 변경 비용이 API 개수에 비례한다**

에러 코드 체계를 한 번 바꾸면 모든 컨트롤러를 순회해야 한다. 하나라도 놓치면 그 API만
옛 형식으로 나가고, 이런 건 테스트로도 잘 안 잡힌다.

**4) 프레임워크 예외는 아예 손을 못 댄다**

`@Valid` 실패 시 Spring 기본 응답이 그대로 나간다.

```json
{ "timestamp": "...", "status": 400, "error": "Bad Request", "path": "/api/auth/signup" }
```

우리 `ApiResponse` 형식과 완전히 다르고, `isSuccess` 필드도 없다.
프론트 입장에서 "왜 이 API만 응답 모양이 다르지?"가 된다.

**5) 예외를 삼키는 실수가 쉬워진다**

`catch (Exception e) { return null; }` 같은 코드가 여기저기 생기면
장애 원인을 추적할 수 없게 된다. 전역 핸들러가 있으면 로깅 지점도 한 곳으로 모인다.

## 정리

`@RestControllerAdvice`가 하는 일은 **"예외 처리"라는 횡단 관심사를 비즈니스 로직에서
떼어내 한 곳으로 모으는 것**이다. 없어도 동작은 하지만, 중복 · 불일치 · 변경 비용이
API 개수만큼 늘어난다.

---

# 2. Spring의 의존성 주입(DI)

## 무엇인가

객체가 **필요한 다른 객체(의존성)를 스스로 만들지 않고, 외부에서 받아 쓰는 것.**

```java
// DI가 없다면 - 서비스가 리포지토리 생성 방법까지 알아야 한다
public class UserCommandServiceImpl {
    private final UserRepository userRepository = new UserRepositoryImpl(); // 강한 결합
}

// DI - 누가 넣어주는지 모르고, 알 필요도 없다
@Service
@RequiredArgsConstructor
public class UserCommandServiceImpl implements UserCommandService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
}
```

`@RequiredArgsConstructor`가 `final` 필드를 받는 생성자를 만들어주고,
Spring이 그 생성자를 호출하면서 컨테이너에 등록된 빈을 넣어준다.

## 왜 하는가

**1) 결합도를 낮춘다**

`UserCommandServiceImpl`은 `UserRepository`라는 **인터페이스**만 안다.
구현이 JPA든 MyBatis든 서비스 코드는 바뀌지 않는다.

**2) 테스트가 쉬워진다**

생성자로 받으니 테스트에서 가짜 객체를 그냥 넣으면 된다. DB 없이 서비스 로직만 검증할 수 있다.

```java
UserCommandService service = new UserCommandServiceImpl(mockRepository, mockEncoder);
```

`new`로 직접 만들었다면 실제 DB를 붙이지 않고는 테스트가 불가능하다.

**3) 객체 생명주기를 프레임워크가 관리한다**

싱글톤 관리, 초기화 순서, 소멸 처리를 Spring이 담당한다.

## 주입 방식 3가지

| 방식 | 형태 | 평가 |
| --- | --- | --- |
| **생성자 주입** | `private final X x;` + 생성자 | ✅ **권장** |
| 필드 주입 | `@Autowired private X x;` | ❌ 비권장 |
| 수정자 주입 | `@Autowired public void setX(X x)` | 선택적 의존성에만 |

**생성자 주입을 쓰는 이유:**

- **불변성** — `final`을 붙일 수 있어 주입 후 바뀌지 않는다
- **필수 의존성 보장** — 의존성 없이는 객체 생성 자체가 안 된다. 필드 주입은 객체는 만들어지고 나중에 NPE가 난다
- **순환 참조를 시작 시점에 발견** — A↔B가 서로를 필요로 하면 애플리케이션이 아예 뜨지 않는다. 필드 주입은 런타임에 터진다
- **테스트 용이** — Spring 컨테이너 없이 `new`로 조립 가능
- 생성자가 하나면 `@Autowired`도 생략 가능하다

이 프로젝트는 모든 서비스/컨트롤러/Validator가 `@RequiredArgsConstructor` 기반 생성자 주입이다.

## IoC(제어의 역전)와의 관계

**IoC** = 객체의 생성과 흐름 제어권이 **개발자 코드에서 프레임워크로 넘어간 것**.
**DI** = IoC를 구현하는 구체적인 방법 중 하나.

즉 DI는 IoC의 부분집합이다.

## IoC 컨테이너

빈(bean)의 생성 · 의존성 연결 · 생명주기를 관리하는 Spring의 핵심.

- `BeanFactory` — 기본 컨테이너. 빈 등록/조회/관리
- `ApplicationContext` — `BeanFactory` 확장. 국제화, 이벤트, AOP 등 추가

**빈 등록 방법**: `@Component`(및 `@Service`, `@Repository`, `@Controller`, `@Configuration`)
컴포넌트 스캔, 또는 `@Configuration` + `@Bean` 수동 등록.

이 프로젝트의 `SecurityConfig`가 후자다.

```java
@Bean
public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
}
```

`PasswordEncoder`는 우리가 만든 클래스가 아니라 Spring Security가 제공하는 것이라
`@Component`를 붙일 수 없다. 그래서 `@Bean`으로 직접 등록한다.

**빈 스코프**: 기본은 `singleton`(컨테이너당 1개). `prototype`, `request`, `session` 등도 있다.
싱글톤이므로 **빈에 상태를 두면 안 된다**(동시 요청이 공유하게 된다).

---

# 3. Lombok

컴파일 시점에 애노테이션 프로세서로 **보일러플레이트 코드를 생성**해주는 라이브러리.
`.java` 소스는 그대로고 `.class`에 메서드가 추가된다.

## 이 프로젝트에서 쓰는 것

| 애노테이션 | 역할 | 사용처 |
| --- | --- | --- |
| `@Getter` | getter 생성 | 엔티티, DTO |
| `@Builder` | 빌더 패턴 생성 | 엔티티, DTO |
| `@SuperBuilder` | **상속 계층**에서 부모 필드까지 빌더로 | `ChatMessage` 계층 |
| `@NoArgsConstructor(access = PROTECTED)` | 기본 생성자 | 엔티티 |
| `@AllArgsConstructor` | 전체 필드 생성자 | DTO |
| `@RequiredArgsConstructor` | `final` 필드 생성자 → **생성자 주입** | 서비스, 컨트롤러 |
| `@Slf4j` | `log` 필드 생성 | `ExceptionAdvice` |

## 주의할 점 (직접 겪은 것 포함)

**1) 엔티티에 `@Setter`를 붙이지 않는다**

아무 데서나 상태를 바꿀 수 있으면 어디서 변경됐는지 추적이 불가능해진다.
변경은 의미 있는 메서드(`updateNickname()` 등)로만 열어야 한다.

**2) 엔티티에 `@Data`를 쓰지 않는다**

`@Data`는 `@Setter` + `@EqualsAndHashCode` + `@ToString`을 포함한다.
연관관계가 있는 엔티티에서 `@ToString`은 **양방향 참조를 타고 무한 재귀**에 빠지고,
`@EqualsAndHashCode`는 지연 로딩 프록시와 충돌한다.

**3) `@NoArgsConstructor(access = PROTECTED)`를 쓰는 이유**

JPA 스펙상 엔티티는 기본 생성자가 필요하다(프록시 생성 때문에).
하지만 `public`으로 열어두면 아무나 빈 엔티티를 만들 수 있으므로 `PROTECTED`로 최소 공개한다.

**4) `@Builder` + 컬렉션 필드는 `@Builder.Default` 필수**

안 붙이면 빌더로 만들 때 필드 초기화식이 무시되고 `null`이 들어간다.

```java
@OneToMany(mappedBy = "room", cascade = CascadeType.ALL, orphanRemoval = true)
@Builder.Default
private List<ChatMessage> messages = new ArrayList<>();
```

**5) `@SuperBuilder`는 계층 전체에 필요하다 (실제로 겪음)**

`ChatMessage`에만 붙였더니 `cannot find symbol: class BaseEntityBuilder`로 컴파일이 깨졌다.
부모인 `BaseEntity`에도 붙여야 하고, 그러면 암묵적 기본 생성자가 사라지므로
`@NoArgsConstructor`도 같이 붙여야 한다. → 트러블슈팅 No.4 참고

---

# 4. Java의 Exception 종류

## 전체 계층

```
Throwable
├── Error                        복구 불가. 잡지 않는다
│   ├── OutOfMemoryError
│   └── StackOverflowError
└── Exception
    ├── (Checked Exception)      컴파일러가 처리를 강제
    │   ├── IOException
    │   ├── SQLException
    │   └── ClassNotFoundException
    └── RuntimeException         (Unchecked) 처리 강제 없음
        ├── NullPointerException
        ├── IllegalArgumentException
        ├── IndexOutOfBoundsException
        └── ClassCastException
```

## Checked vs Unchecked

| | Checked | Unchecked (RuntimeException) |
| --- | --- | --- |
| 처리 강제 | ✅ try-catch 또는 throws 필수 | ❌ 선택 |
| 검사 시점 | 컴파일 타임 | 런타임 |
| 성격 | 외부 요인, 복구 가능 | 프로그래밍 실수, 예측 가능 |
| **스프링 트랜잭션** | **롤백 안 됨** (기본) | **롤백 됨** |
| 예시 | 파일 없음, 네트워크 끊김 | null 접근, 잘못된 인자 |

## 커스텀 예외를 `RuntimeException`으로 만드는 이유

이 프로젝트의 `GeneralException`은 `RuntimeException`을 상속한다.

```java
@Getter
@AllArgsConstructor
public class GeneralException extends RuntimeException {
    private final BaseErrorCode code;
    ...
}
```

**1) `@Transactional`이 기본적으로 롤백해준다**

Spring은 기본 설정에서 **unchecked 예외에만 롤백**한다.
checked로 만들면 예외가 터져도 트랜잭션이 커밋되어 데이터가 반쯤 저장될 수 있다.
(`@Transactional(rollbackFor = Exception.class)`로 바꿀 수는 있지만 매번 명시해야 한다)

**2) 메서드 시그니처가 오염되지 않는다**

checked라면 서비스 → 컨트롤러로 올라오는 모든 메서드에 `throws`를 달아야 한다.
전역 핸들러가 잡을 예외인데 중간 계층이 전부 알아야 하는 건 불필요한 결합이다.

**3) 어차피 `@RestControllerAdvice`가 잡는다**

호출부에서 개별적으로 복구할 예외가 아니다. "이 상황은 클라이언트 에러다"를 표현하고
전역 핸들러가 응답으로 변환하는 구조라, 처리 강제가 오히려 방해가 된다.

## 이 프로젝트의 예외 설계

```
RuntimeException
└── GeneralException                    BaseErrorCode를 들고 다닌다
    ├── UserHandler                     회원 도메인
    ├── FriendHandler                   친구 도메인
    ├── ChatRoomHandler                 채팅방 도메인
    └── ChatMessageHandler              메시지 도메인
```

**도메인별로 Handler를 나눈 이유**: 예외 클래스 이름만 봐도 어느 영역의 문제인지 드러나고,
나중에 "회원 관련 예외만 별도 로깅" 같은 요구가 생겼을 때
`@ExceptionHandler(UserHandler.class)`로 분리하기 쉽다.

**에러 정보는 예외 클래스가 아니라 `ErrorStatus` enum이 들고 있다.**
상황마다 예외 클래스를 새로 만들면 클래스가 수십 개로 늘어난다.
enum 상수 하나만 추가하면 되도록 설계했다.

---

# 5. @Valid

## 무엇인가

Bean Validation(JSR-380) 표준. 객체의 필드에 붙은 제약 조건을 검증하도록 지시한다.
Spring Boot에서는 `spring-boot-starter-validation`(Hibernate Validator)이 구현체다.

## 왜 쓰는가 — 단일 책임 원칙

검증 로직이 서비스에 있으면 서비스가 "비즈니스 처리"와 "입력값 검사" 두 가지 책임을 진다.

```java
// 검증이 서비스에 섞인 경우
public User joinUser(JoinDTO request) {
    if (request.getEmail() == null || request.getEmail().isBlank()) throw ...;
    if (!request.getEmail().contains("@")) throw ...;
    if (request.getPassword().length() < 8) throw ...;
    // ↑ 진짜 로직은 아래 한 줄인데 검증이 절반
    return userRepository.save(...);
}
```

`@Valid`로 옮기면 **DTO가 자기 형식을 스스로 정의**하고, 서비스는 비즈니스에만 집중한다.
제약 조건이 DTO에 선언적으로 드러나서 문서 역할도 한다.

```java
public static class JoinDTO {
    @NotBlank(message = "이메일은 필수입니다.")
    @Email(message = "이메일 형식이 올바르지 않습니다.")
    @NotDuplicateEmail
    private String email;

    @NotBlank @Size(min = 8, max = 20, message = "비밀번호는 8자 이상 20자 이하여야 합니다.")
    private String password;
}
```

## 표준 어노테이션

| 어노테이션 | 검증 내용 | 적용 타입 |
| --- | --- | --- |
| `@NotNull` | null이 아님 | 모든 타입 |
| `@NotEmpty` | null 아니고 길이/크기 > 0 | String, Collection |
| `@NotBlank` | null 아니고 **공백 제외** 길이 > 0 | String |
| `@Size(min, max)` | 길이/크기 범위 | String, Collection |
| `@Min` / `@Max` | 숫자 범위 | 숫자 |
| `@Email` | 이메일 형식 | String |
| `@Pattern(regexp)` | 정규식 | String |

`@NotEmpty`와 `@NotBlank` 차이: `" "`(공백만 있는 문자열)는 `@NotEmpty`는 통과, `@NotBlank`는 실패.
사용자 입력에는 보통 `@NotBlank`가 맞다.

## @Valid vs @Validated

| | `@Valid` | `@Validated` |
| --- | --- | --- |
| 출처 | Java 표준 (jakarta.validation) | Spring |
| 위치 | 파라미터, 필드 | **클래스**, 파라미터 |
| 그룹 검증 | ❌ | ✅ |
| 발생 예외 | `MethodArgumentNotValidException` | `ConstraintViolationException` |

**둘 다 필요하다.**

- `@RequestBody` 객체 검증 → 파라미터에 `@Valid`
- `@RequestParam` / `@PathVariable` 같은 **단일 값** 검증 → 클래스에 `@Validated`

이 프로젝트의 컨트롤러는 클래스에 `@Validated`, `@RequestBody`에 `@Valid`를 둘 다 붙인다.

```java
@RestController
@Validated                                     // @RequestParam 검증용
public class ChatRoomRestController {
    @GetMapping("/{roomId}/messages")
    public ApiResponse<...> getMessages(
            @PathVariable Long roomId,
            @CheckPage @RequestParam(defaultValue = "1") Integer page) { ... }
}
```

## 커스텀 어노테이션 만들기

DB 조회가 필요한 검증(존재 여부, 중복 여부)은 표준 어노테이션으로 못 한다.
`@Constraint` + `ConstraintValidator`로 직접 만든다.

```java
@Documented
@Constraint(validatedBy = EmailDuplicateValidator.class)
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface NotDuplicateEmail {
    String message() default "USER_EMAIL_DUPLICATED";   // ErrorStatus enum 이름
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
```

- `@Documented` — javadoc에 표시
- `@Constraint(validatedBy = ...)` — 실제 검증 클래스 지정
- `@Target` — 붙일 수 있는 위치
- `@Retention(RUNTIME)` — 런타임에 리플렉션으로 읽어야 하므로 필수
- `groups`, `payload` — Bean Validation 스펙이 요구하는 필수 멤버

```java
@Component
@RequiredArgsConstructor
public class EmailDuplicateValidator implements ConstraintValidator<NotDuplicateEmail, String> {

    private final UserQueryService userQueryService;   // ← Repository가 아니라 Service

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) return true;   // null 여부는 @NotBlank의 책임

        boolean isValid = !userQueryService.isEmailDuplicated(value);
        if (!isValid) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                    ErrorStatus.USER_EMAIL_DUPLICATED.toString()).addConstraintViolation();
        }
        return isValid;
    }
}
```

**Validator가 Repository가 아니라 Service를 주입받는 이유**(워크북이 미션으로 남긴 개선점):
영속성 계층에 접근하는 통로를 **service 하나로 유지**하기 위해서다.
Validator가 Repository를 직접 보면 계층 구조가 무너지고,
"이 조회에 트랜잭션/캐싱이 걸려 있나?"를 두 군데서 관리해야 한다.

**`isValid`에서 null을 통과시키는 이유**: 책임 분리.
"값이 있는가"는 `@NotNull`/`@NotBlank`가, "그 값이 유효한가"는 커스텀 어노테이션이 맡는다.
둘 다 실패하면 에러 메시지가 두 개 겹쳐 나온다.

## 검증 실패 시 흐름

```
1. @ExistUser / @NotDuplicateEmail 등이 붙은 필드 검증
2. Validator.isValid()가 false 반환
3. ConstraintViolationException 발생
4. @Valid가 붙어 있으므로 곧바로 전달되지 않고,
   @Valid가 검증 실패를 감지해 MethodArgumentNotValidException으로 감싼다
5. ExceptionAdvice.handleMethodArgumentNotValid()가 받는다
```

`@Valid` **없이** 커스텀 어노테이션만 붙은 경우(`@RequestParam` 등)는
4~5단계를 건너뛰고 `ConstraintViolationException`이 바로 전달된다.
그래서 `ExceptionAdvice`에 두 경로의 핸들러가 모두 있어야 한다.

## 실패 응답과 에러 핸들러의 연동 (직접 겪은 문제)

Validator는 메시지에 **`ErrorStatus`의 enum 이름**을 심는다.
이걸 핸들러가 되돌려주지 않으면 `USER_EMAIL_DUPLICATED` 같은 자바 상수 이름이
그대로 클라이언트에 노출된다.

`ConstraintViolationException` 경로에는 복원 로직이 있었지만
`MethodArgumentNotValidException` 경로에는 없어서, `@Valid`를 타고 들어온 커스텀 어노테이션
실패가 전부 `COMMON400`으로 뭉개졌다. → 트러블슈팅 No.2 참고

```java
// 해결: 필드 에러 메시지 중 ErrorStatus로 해석되는 게 있으면 그 도메인 에러로 응답
Optional<ErrorStatus> domainError = errors.values().stream()
        .map(this::toErrorStatus)
        .flatMap(Optional::stream)
        .findFirst();
```

---

# 6. 영속성 컨텍스트

## 무엇인가

엔티티를 담아두는 **1차 캐시**. `EntityManager`가 관리하며,
`@Transactional` 범위와 생명주기를 같이한다.

## 엔티티 생명주기

| 상태 | 설명 |
| --- | --- |
| **비영속(new)** | `new`로 만든 직후. 컨텍스트와 무관 |
| **영속(managed)** | `persist()` 했거나 조회로 가져온 상태. 컨텍스트가 관리 |
| **준영속(detached)** | 컨텍스트에서 분리됨. 변경 감지 안 됨 |
| **삭제(removed)** | `remove()` 호출 |

## 특징

**1) 1차 캐시** — 같은 트랜잭션에서 같은 ID를 두 번 조회하면 DB에 한 번만 간다

**2) 동일성 보장** — 같은 ID면 `==` 비교가 참이다.
👉 **이 성질 때문에 트러블슈팅 No.5가 발생했다.**
답장 대상으로 프록시가 먼저 등록되면, 같은 ID의 실제 행을 조회해도
동일성을 지키려고 **이미 등록된 프록시를 반환**한다.

**3) 쓰기 지연** — `persist()` 시점이 아니라 flush 시점에 INSERT가 모여서 나간다

**4) 변경 감지(Dirty Checking)** — 조회한 엔티티의 값을 바꾸면
`update()` 호출 없이도 트랜잭션 커밋 시 UPDATE가 나간다.
스냅샷과 현재 상태를 비교하는 방식이다

**5) 지연 로딩** — LAZY 연관관계는 프록시로 두고, 실제 접근 시 초기화한다

## 주의

- **준영속 상태에서는 변경 감지가 안 된다.** 트랜잭션 밖에서 값을 바꿔도 저장되지 않는다
- `LazyInitializationException` — 트랜잭션이 끝난 뒤 프록시를 건드리면 발생한다.
  `spring.jpa.open-in-view`가 기본 `true`라 웹에서는 잘 안 터지지만,
  이건 DB 커넥션을 뷰 렌더링까지 붙잡고 있는 거라 운영에서는 끄는 편이 낫다
  (기동 시 경고 로그로도 안내된다)

---

# 7. 양방향 매핑

## 단방향 / 양방향

DB의 외래키는 방향이 없다. 조인하면 어느 쪽에서든 갈 수 있다.
반면 **객체는 참조 방향이 있어서**, 양쪽에서 서로를 탐색하려면 양쪽에 필드를 둬야 한다.

```java
// ChatRoom → messages 방향
@OneToMany(mappedBy = "room", cascade = CascadeType.ALL, orphanRemoval = true)
private List<ChatMessage> messages = new ArrayList<>();

// ChatMessage → room 방향
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "room_id", nullable = false)
private ChatRoom room;
```

## 연관관계의 주인

양방향이면 참조가 2개인데 외래키는 1개다. **누가 FK를 관리할지** 정해야 한다.

- **주인** = 외래키를 가진 쪽 = `@JoinColumn`을 쓰는 **N 쪽** (`ChatMessage.room`)
- **주인이 아닌 쪽** = `mappedBy`로 주인 필드명을 가리킴 (`ChatRoom.messages`)
- **주인이 아닌 쪽은 읽기 전용이다.** 여기만 바꾸면 DB에 반영되지 않는다

## 이 프로젝트의 양방향 판단

| 관계 | 방향 | 근거 |
| --- | --- | --- |
| ChatRoom ↔ ChatParticipant | 양방향 + cascade ALL | 방이 삭제되면 참여자도 사라져야 함 |
| ChatRoom ↔ ChatMessage | 양방향 + cascade ALL | 방이 삭제되면 메시지도 사라져야 함 |
| ChatRoom ↔ OpenChatRoom | 양방향 1:1 + cascade | 방에 종속된 정보 |
| User → ChatParticipant | 양방향, **cascade 없음** | 탈퇴 회원 데이터를 지울지 미정 |
| **User → ChatMessage** | **단방향만** | 메시지는 수십만 건. 컬렉션으로 들면 전체 로딩 위험 |
| **User → Friend** | **단방향만** | 자기참조라 mappedBy가 user/friend 두 갈래로 갈림 |

**핵심**: 양방향은 편의 기능일 뿐 필수가 아니다. 필요할 때만 추가하는 게 맞다.

## 주의할 점

**1) 무한 재귀** — 양방향에서 `@ToString`이나 JSON 직렬화가 서로를 계속 타고 들어가
`StackOverflowError`가 난다.
→ 엔티티를 직접 응답하지 말고 **DTO로 변환**한다. 이 프로젝트가 `converter`를 두는 이유다.

**2) 연관관계 편의 메서드** — 양쪽 참조를 동시에 맞춰주는 메서드를 두면 실수를 줄인다

**3) 컬렉션에 cascade를 붙일 때는 소유 관계인지 확인** — 아니면 의도치 않은 삭제가 발생한다

---

# 8. N + 1 문제

## 무엇인가

목록을 조회하는 쿼리 **1번** 뒤에, 각 항목의 연관 엔티티를 채우려고
**N번**의 추가 쿼리가 나가는 현상.

## 실제로 겪은 사례

채팅 내역 조회 API에서 발생했다.

```java
// 메시지 목록 조회 → 쿼리 1번
Page<ChatMessage> messages = chatMessageRepository.findAllByRoom(room, pageable);

// 컨버터에서 발신자 닉네임 접근 → 메시지마다 쿼리 1번씩 (N번)
.senderNickname(message.getSender().getNickname())
```

`sender`가 `@ManyToOne(fetch = FetchType.LAZY)`라 프록시로 있다가,
`getNickname()` 호출 시점에 초기화되면서 `users` 조회가 나간다.

```sql
select ... from chat_messages ... -- 1번
select ... from users where id=?  -- 메시지 1
select ... from users where id=?  -- 메시지 2
select ... from users where id=?  -- 메시지 3
```

메시지 20건이면 쿼리 **22번**(목록 + count + 20), 100건이면 **102번**.

## 왜 생기는가

- **LAZY** — 필요할 때 각각 조회 → N+1
- **EAGER** — JPQL로 조회하면 EAGER여도 연관 엔티티를 따로 가져오므로 **여전히 N+1**.
  게다가 항상 조인되어 불필요한 조회까지 생긴다.
  👉 **EAGER는 해결책이 아니다. 기본은 LAZY로 두고 필요할 때 함께 조회하는 게 정답이다.**

## 해결 방법

**1) `@EntityGraph`** — 이 프로젝트에서 채택

```java
@EntityGraph(attributePaths = {"sender"})
Page<ChatMessage> findAllByRoom(ChatRoom room, Pageable pageable);
```

적용 후 users 단건 조회 **0회**.

**2) fetch join**

```java
@Query("select m from ChatMessage m join fetch m.sender where m.room = :room")
List<ChatMessage> findAllWithSender(@Param("room") ChatRoom room);
```

`Page` 반환 시 count 쿼리를 따로 지정해야 해서, 페이징과 함께 쓸 땐 `@EntityGraph`가 편하다.

**3) `@BatchSize` / `default_batch_fetch_size`**

N번을 `IN` 절로 묶어 `N/배치크기`번으로 줄인다. 컬렉션이 여러 개일 때 유용하다.

## 컬렉션 fetch join의 함정

`@OneToMany`를 fetch join하면 결과 행이 뻥튀기되고,
**페이징이 메모리에서 처리되어**(`HHH000104` 경고) 데이터가 많으면 OOM이 난다.
그래서 컬렉션은 `@BatchSize` 쪽이 안전하다.

## 어떻게 발견하는가

`application.yml`에 아래를 켜두면 SQL이 다 찍혀서 눈으로 확인할 수 있다.

```yaml
spring.jpa.show-sql: true
logging.level.org.hibernate.SQL: debug
```

이번에도 이 로그 덕분에 발견했다.
