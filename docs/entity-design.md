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

## 남은 것

ChatParticipant.joinedAt이 BaseEntity.createdAt과 겹친다.
재입장 시각을 따로 기록할 게 아니면 하나로 합치는 게 맞다.

패키지는 아직 domain만 있다. controller/service/repository를 만들 때
계층형으로 갈지 도메인별로 나눌지 정해야 한다.

카카오톡 기준으로 확장한 ERD는 12테이블이고 메시지를 조인 상속으로 설계했다.
JPA에서는 @Inheritance(strategy = InheritanceType.JOINED)로 대응된다.