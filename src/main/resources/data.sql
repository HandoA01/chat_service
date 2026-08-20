-- 로컬 개발/검증용 시드 데이터 (H2 인메모리, 매 기동 시 재생성)
-- 시드 회원 3명의 비밀번호는 모두 password1234 (BCrypt 인코딩된 값)

INSERT INTO users (id, email, password, nickname, status, created_at, updated_at) VALUES
  (1, 'hong@example.com', '$2a$10$N1YDQSWhBbS7L9ksIdDh3e6LijF2GLcROX3R2gkobVfOn5GPq06i6', '홍길동', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  (2, 'kim@example.com',  '$2a$10$N1YDQSWhBbS7L9ksIdDh3e6LijF2GLcROX3R2gkobVfOn5GPq06i6', '김철수', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  (3, 'lee@example.com',  '$2a$10$N1YDQSWhBbS7L9ksIdDh3e6LijF2GLcROX3R2gkobVfOn5GPq06i6', '이영희', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO chat_rooms (id, title, type, created_at, updated_at) VALUES
  (100, '스프링 스터디', 'GROUP', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  (101, '자바 오픈채팅', 'OPEN',  CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 오픈채팅방에만 존재하는 자식 행 (chat_room_id가 PK이자 FK)
INSERT INTO open_chat_rooms (chat_room_id, owner_id, description, max_member_count, created_at, updated_at) VALUES
  (101, 1, '자바 이야기 나누는 방입니다', 100, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO chat_participants (id, room_id, user_id, joined_at, created_at, updated_at) VALUES
  (1000, 100, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  (1001, 100, 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO emoticon_packs (id, name, publisher, created_at, updated_at) VALUES
  (10, '춘식이 이모티콘', '카카오', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO emoticons (id, pack_id, name, image_url, created_at, updated_at) VALUES
  (20, 10, '춘식이 웃음', 'https://cdn.example.com/emoticons/20.png', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO user_emoticons (id, user_id, pack_id, created_at, updated_at) VALUES
  (30, 1, 10, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 메시지 부모 행 (type이 discriminator, parent_message_id는 답장 대상)
INSERT INTO chat_messages (id, type, room_id, sender_id, parent_message_id, created_at, updated_at) VALUES
  (5001, 'TEXT',  100, 1, NULL, DATEADD('MINUTE', -5, CURRENT_TIMESTAMP), CURRENT_TIMESTAMP),
  (5002, 'TEXT',  100, 2, NULL, DATEADD('MINUTE', -4, CURRENT_TIMESTAMP), CURRENT_TIMESTAMP),
  (5003, 'TEXT',  100, 1, 5002, DATEADD('MINUTE', -3, CURRENT_TIMESTAMP), CURRENT_TIMESTAMP),
  (5004, 'EMOJI', 100, 2, NULL, DATEADD('MINUTE', -2, CURRENT_TIMESTAMP), CURRENT_TIMESTAMP),
  (5005, 'MEDIA', 100, 1, NULL, DATEADD('MINUTE', -1, CURRENT_TIMESTAMP), CURRENT_TIMESTAMP);

-- 종류별 자식 행 (message_id가 PK이자 FK)
INSERT INTO text_messages (message_id, content) VALUES
  (5001, '안녕하세요'),
  (5002, '오늘 스터디 몇시죠?'),
  (5003, '18시입니다');   -- 5002에 대한 답장

INSERT INTO emoji_messages (message_id, emoticon_id) VALUES
  (5004, 20);

INSERT INTO media_messages (message_id, file_url, file_type, file_size, thumbnail_url) VALUES
  (5005, 'https://cdn.example.com/files/study.png', 'IMAGE', 20480, 'https://cdn.example.com/files/study_thumb.png');
