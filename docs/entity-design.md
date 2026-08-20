# 엔티티 설계

## 테이블 구성

User, ChatRoom, ChatMessage, ChatParticipant, Friend 5개.
ChatParticipant는 방-회원 N:M을, Friend는 회원-회원 N:M을 푸는 매핑 테이블이다.

@ManyToMany는 쓰지 않았다. 매핑 테이블에 담아야 할 속성이 있었기 때문이다.
ChatParticipant에는 안읽음 카운트 계산용 lastReadMessageId와 참여 시각이,
Friend에는 요청 상태(PENDING/ACCEPTED)가 필요했다.

## 연관관계

처음에는 roomId, senderId처럼 순수 Long FK로 잡았다가 @ManyToOne 매핑으로 바꿨다.
Long FK는 조회할 때마다 조인 쿼리를 직접 짜야 하고 객체 그래프 탐색이 안 된다.

단방향(@ManyToOne, LAZY)
- ChatMessage → ChatRoom, User(sender)
- ChatParticipant → ChatRoom, User
- Friend → User(user), User(friend)

전부 지연 로딩으로 뒀다. 즉시 로딩이면 메시지 하나 조회할 때 방과 발신자까지
매번 따라 나온다.

양방향(@OneToMany)
- ChatRoom → participants, messages : cascade ALL + orphanRemoval.
  방이 삭제되면 참여자와 메시지도 함께 사라져야 한다.
- User → participants : cascade 없음.
  탈퇴한 회원의 데이터를 지울지 남길지 아직 정하지 않아서 붙이지 않았다.

양방향을 걸지 않은 곳
- User → ChatMessage : 메시지는 수십만 건까지 늘어난다. 컬렉션으로 들고 있으면
  전체 로딩 위험이 있어 필요할 때 리포지토리로 조회한다.
- User → Friend : 자기참조라 mappedBy 대상이 user, friend 두 갈래로 나뉜다.
  컬렉션도 두 개가 되어 복잡해지기만 한다.

## 상속 매핑

ERD의 12테이블을 모두 엔티티로 옮기면서 상속 매핑을 적용했다.

ChatMessage를 @Inheritance(strategy = InheritanceType.JOINED)로 두고
type을 @DiscriminatorColumn으로 삼아, TextMessage / EmojiMessage / MediaMessage가
@PrimaryKeyJoinColumn(name = "message_id")로 부모를 상속한다.
종류마다 필요한 컬럼이 겹치지 않아서, 한 테이블에 몰면 대부분이 빈 값이 되기 때문이다.

답장은 ChatMessage.parentMessage가 자기 자신을 @ManyToOne으로 참조해서 푼다.
없으면 null이 일반 메시지다.

OpenChatRoom은 @MapsId로 chat_room_id를 PK이자 FK로 쓴다.
오픈채팅에만 있는 방장/정원/입장코드를 chat_rooms에 두면 일반 방에서 전부 빈 값이 된다.

### 상속 매핑에서 실제로 걸린 것

조회 결과를 DTO로 바꿀 때 instanceof로 자식 타입을 분기했는데, 답장 대상으로 먼저
참조된 메시지는 ChatMessage 프록시로 로딩되어 있어서 instanceof가 전부 빗나갔다.
프록시는 자식이 아니라 부모를 상속하기 때문이다.
Hibernate.unproxy()로 벗겨낸 뒤 분기하도록 고쳤다.

발신자 닉네임을 응답에 넣느라 LAZY인 sender를 건드리면서 메시지 수만큼
users 조회가 더 나갔다(N+1). ChatMessageRepository에 @EntityGraph(attributePaths = {"sender"})를
붙여 한 번에 가져오도록 했다.

## 남은 것

ChatParticipant.joinedAt이 BaseEntity.createdAt과 겹친다.
재입장 시각을 따로 기록할 게 아니면 하나로 합치는 게 맞다.

메시지 전송/채팅방 생성 API가 아직 없어서, 상속 매핑은 조회 경로로만 검증했다.
쓰기 경로(어떤 자식 타입으로 저장할지 분기)는 API를 만들 때 함께 봐야 한다.