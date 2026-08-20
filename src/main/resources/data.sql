-- 로컬 개발/검증용 시드 데이터 (H2 인메모리, 매 기동 시 재생성)
INSERT INTO users (id, email, password, nickname, status, created_at, updated_at) VALUES
  (1, 'hong@example.com', '{noop}seed', '홍길동', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  (2, 'kim@example.com',  '{noop}seed', '김철수', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  (3, 'lee@example.com',  '{noop}seed', '이영희', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO chat_rooms (id, title, type, created_at, updated_at) VALUES
  (100, '스프링 스터디', 'GROUP', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO chat_participants (id, room_id, user_id, joined_at, created_at, updated_at) VALUES
  (1000, 100, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  (1001, 100, 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO chat_messages (id, room_id, sender_id, content, created_at, updated_at) VALUES
  (5001, 100, 1, '안녕하세요',           DATEADD('MINUTE', -5, CURRENT_TIMESTAMP), CURRENT_TIMESTAMP),
  (5002, 100, 2, '네 안녕하세요',         DATEADD('MINUTE', -4, CURRENT_TIMESTAMP), CURRENT_TIMESTAMP),
  (5003, 100, 1, '오늘 스터디 몇시죠?',   DATEADD('MINUTE', -3, CURRENT_TIMESTAMP), CURRENT_TIMESTAMP),
  (5004, 100, 2, '18시입니다',            DATEADD('MINUTE', -2, CURRENT_TIMESTAMP), CURRENT_TIMESTAMP),
  (5005, 100, 1, '감사합니다',            DATEADD('MINUTE', -1, CURRENT_TIMESTAMP), CURRENT_TIMESTAMP);
