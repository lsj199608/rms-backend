CREATE TABLE IF NOT EXISTS chat_rooms (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(120) NOT NULL UNIQUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS chat_room_members (
    room_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uk_chat_room_members_room_id_user_id UNIQUE (room_id, user_id),
    CONSTRAINT fk_chat_room_members_room FOREIGN KEY (room_id) REFERENCES chat_rooms (id) ON DELETE CASCADE,
    CONSTRAINT fk_chat_room_members_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS chat_messages (
    id BIGSERIAL PRIMARY KEY,
    room_id BIGINT NOT NULL,
    sender_id BIGINT NOT NULL,
    content TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT fk_chat_messages_room FOREIGN KEY (room_id) REFERENCES chat_rooms (id) ON DELETE CASCADE,
    CONSTRAINT fk_chat_messages_sender FOREIGN KEY (sender_id) REFERENCES users (id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_chat_messages_room_created_at
    ON chat_messages (room_id, created_at);
CREATE INDEX IF NOT EXISTS idx_chat_room_members_user
    ON chat_room_members (user_id);

INSERT INTO chat_rooms(name)
VALUES ('전체채팅')
ON CONFLICT (name) DO NOTHING;

INSERT INTO chat_room_members(room_id, user_id)
SELECT r.id, u.id
FROM chat_rooms r
JOIN users u ON u.username IN ('admin', 'user')
WHERE r.name = '전체채팅'
ON CONFLICT (room_id, user_id) DO NOTHING;
