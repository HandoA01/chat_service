package com.study.chat.websocket;

import static org.assertj.core.api.Assertions.assertThat;

import com.study.chat.web.dto.ChatMessageRequestDTO;
import com.study.chat.domain.enums.MessageType;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;

/**
 * 명세서 5장(WebSocket STOMP) 검증.
 * 실제 서버를 띄우고 CONNECT → SUBSCRIBE → SEND → 브로드캐스트 수신까지 확인한다.
 *
 * 수신 payload는 Map으로 받는다. 응답 DTO에는 setter가 없어(불변에 가깝게 두려고)
 * Jackson이 역직렬화할 수 없는데, 그건 테스트 사정이지 DTO에 setter를 열 이유는 아니기 때문이다.
 * 실제 클라이언트(JS)는 JSON을 그대로 읽으므로 Map 검증이 실제 사용과 더 가깝다.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ChatStompIntegrationTest {

    @LocalServerPort
    int port;

    private WebSocketStompClient stompClient;
    private String token;
    private final BlockingQueue<Map<String, Object>> serverErrors = new LinkedBlockingQueue<>();

    @BeforeEach
    void setUp() {
        stompClient = new WebSocketStompClient(new StandardWebSocketClient());
        stompClient.setMessageConverter(new MappingJackson2MessageConverter());
        token = login("hong@example.com", "password1234");
        serverErrors.clear();
    }

    @Test
    @DisplayName("텍스트 메시지를 발행하면 구독자에게 브로드캐스트된다")
    void sendTextMessage() throws Exception {
        StompSession session = connect(token);
        BlockingQueue<Map<String, Object>> received = subscribe(session, 100L);

        ChatMessageRequestDTO.SendDTO payload = new ChatMessageRequestDTO.SendDTO();
        payload.setContent("안녕하세요! 회의 준비 되셨나요?");
        session.send("/pub/chat-rooms/100/messages", payload);

        Map<String, Object> message = received.poll(5, TimeUnit.SECONDS);

        assertNoServerError();
        assertThat(message).isNotNull();
        assertThat(message.get("type")).isEqualTo("TEXT");
        assertThat(message.get("content")).isEqualTo("안녕하세요! 회의 준비 되셨나요?");
        assertThat(message.get("roomId")).isEqualTo(100);
        assertThat(message.get("senderNickname")).isEqualTo("홍길동");
        // 발신자는 서버가 세션에서 꺼낸다 (payload에 담지 않았는데도 채워져야 한다)
        assertThat(message.get("senderId")).isEqualTo(1);
    }

    @Test
    @DisplayName("답장을 보내면 parentMessageId가 함께 내려온다")
    void sendReply() throws Exception {
        StompSession session = connect(token);
        BlockingQueue<Map<String, Object>> received = subscribe(session, 100L);

        ChatMessageRequestDTO.SendDTO payload = new ChatMessageRequestDTO.SendDTO();
        payload.setContent("네 준비됐습니다");
        payload.setParentMessageId(5001L);
        session.send("/pub/chat-rooms/100/messages", payload);

        Map<String, Object> message = received.poll(5, TimeUnit.SECONDS);

        assertNoServerError();
        assertThat(message).isNotNull();
        assertThat(message.get("parentMessageId")).isEqualTo(5001);
    }

    @Test
    @DisplayName("이모티콘 메시지는 EMOJI 자식 엔티티로 저장되고 이모티콘 정보가 내려온다")
    void sendEmojiMessage() throws Exception {
        StompSession session = connect(token);
        BlockingQueue<Map<String, Object>> received = subscribe(session, 100L);

        ChatMessageRequestDTO.SendDTO payload = new ChatMessageRequestDTO.SendDTO();
        payload.setType(MessageType.EMOJI);
        payload.setEmoticonId(20L);
        session.send("/pub/chat-rooms/100/messages", payload);

        Map<String, Object> message = received.poll(5, TimeUnit.SECONDS);

        assertNoServerError();
        assertThat(message).isNotNull();
        assertThat(message.get("type")).isEqualTo("EMOJI");
        assertThat(message.get("emoticonId")).isEqualTo(20);
        // 종류가 다르면 채워지는 필드도 달라야 한다 (조인 상속)
        assertThat(message).doesNotContainKey("content");
    }

    @Test
    @DisplayName("미디어 메시지는 MEDIA 자식 엔티티로 저장된다")
    void sendMediaMessage() throws Exception {
        StompSession session = connect(token);
        BlockingQueue<Map<String, Object>> received = subscribe(session, 100L);

        ChatMessageRequestDTO.SendDTO payload = new ChatMessageRequestDTO.SendDTO();
        payload.setType(MessageType.MEDIA);
        payload.setFileUrl("https://cdn.example.com/files/a.png");
        payload.setFileType(com.study.chat.domain.enums.FileType.IMAGE);
        session.send("/pub/chat-rooms/100/messages", payload);

        Map<String, Object> message = received.poll(5, TimeUnit.SECONDS);

        assertNoServerError();
        assertThat(message).isNotNull();
        assertThat(message.get("type")).isEqualTo("MEDIA");
        assertThat(message.get("fileType")).isEqualTo("IMAGE");
        assertThat(message).doesNotContainKey("content");
    }

    @Test
    @DisplayName("본문 없는 텍스트 메시지는 발신자에게만 에러가 돌아가고 브로드캐스트되지 않는다")
    void sendTextWithoutContent() throws Exception {
        StompSession session = connect(token);
        BlockingQueue<Map<String, Object>> received = subscribe(session, 100L);

        ChatMessageRequestDTO.SendDTO payload = new ChatMessageRequestDTO.SendDTO();
        payload.setContent("   ");
        session.send("/pub/chat-rooms/100/messages", payload);

        Map<String, Object> error = serverErrors.poll(5, TimeUnit.SECONDS);

        assertThat(error).isNotNull();
        assertThat(error.get("code")).isEqualTo("MESSAGE4004");
        assertThat(error.get("isSuccess")).isEqualTo(false);
        // 실패한 메시지가 구독자에게 퍼지면 안 된다
        assertThat(received.poll(1, TimeUnit.SECONDS)).isNull();
    }

    @Test
    @DisplayName("참여하지 않은 채팅방에는 메시지를 보낼 수 없다")
    void sendToRoomNotJoined() throws Exception {
        String otherToken = login("lee@example.com", "password1234");
        StompSession session = connect(otherToken);
        subscribe(session, 100L);

        ChatMessageRequestDTO.SendDTO payload = new ChatMessageRequestDTO.SendDTO();
        payload.setContent("낄 수 있나요?");
        session.send("/pub/chat-rooms/100/messages", payload);

        Map<String, Object> error = serverErrors.poll(5, TimeUnit.SECONDS);

        assertThat(error).isNotNull();
        assertThat(error.get("code")).isEqualTo("ROOM4002");
    }

    @Test
    @DisplayName("토큰 없이 CONNECT하면 연결이 거부된다")
    void connectWithoutToken() {
        StompHeaders headers = new StompHeaders();
        try {
            stompClient.connectAsync(url(), new WebSocketHttpHeaders(), headers,
                    new StompSessionHandlerAdapter() {
                    }).get(5, TimeUnit.SECONDS);
            org.junit.jupiter.api.Assertions.fail("인증 없이 연결이 성공하면 안 된다");
        } catch (Exception expected) {
            assertThat(expected).isNotNull();
        }
    }

    // --- helpers ---

    private String url() {
        return "ws://localhost:" + port + "/ws-stomp";
    }

    @SuppressWarnings("unchecked")
    private String login(String email, String password) {
        RestTemplate restTemplate = new RestTemplateBuilder().build();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        String body = "{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}";

        Map<String, Object> response = restTemplate.postForObject(
                "http://localhost:" + port + "/api/auth/login",
                new HttpEntity<>(body, headers), Map.class);

        Map<String, Object> result = (Map<String, Object>) response.get("result");
        return (String) result.get("accessToken");
    }

    private StompSession connect(String jwt) throws Exception {
        StompHeaders connectHeaders = new StompHeaders();
        connectHeaders.add("Authorization", "Bearer " + jwt);

        return stompClient.connectAsync(url(), new WebSocketHttpHeaders(), connectHeaders,
                new StompSessionHandlerAdapter() {
                }).get(5, TimeUnit.SECONDS);
    }

    @SuppressWarnings("unchecked")
    private BlockingQueue<Map<String, Object>> subscribe(StompSession session, Long roomId)
            throws InterruptedException {
        BlockingQueue<Map<String, Object>> queue = new LinkedBlockingQueue<>();

        session.subscribe("/sub/chat-rooms/" + roomId, new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return Map.class;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                queue.add((Map<String, Object>) payload);
            }
        });

        // 서버가 에러를 개인 큐로 돌려주므로 같이 구독해 실패 원인을 드러낸다.
        session.subscribe("/user/sub/errors", new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return Map.class;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                serverErrors.add((Map<String, Object>) payload);
            }
        });

        // SUBSCRIBE 프레임이 서버에 등록되기 전에 SEND가 처리되면 브로드캐스트를 놓친다.
        Thread.sleep(300);
        return queue;
    }

    private void assertNoServerError() {
        assertThat(serverErrors).as("서버가 에러를 반환했다").isEmpty();
    }
}
