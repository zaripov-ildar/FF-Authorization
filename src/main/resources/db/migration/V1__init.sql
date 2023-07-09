CREATE TABLE IF NOT EXISTS _role
(
    id         serial primary key,
    title      varchar(50),
    created_at timestamp DEFAULT current_timestamp,
    updated_at timestamp DEFAULT current_timestamp

);
CREATE TABLE IF NOT EXISTS auth_user
(
    id         serial primary key,
    email      varchar(50),
    password   varchar(100),
    created_at timestamp DEFAULT current_timestamp,
    updated_at timestamp DEFAULT current_timestamp
);

CREATE TABLE IF NOT EXISTS user_role
(
    user_id bigint REFERENCES auth_user (id),
    role_id bigint REFERENCES _role (id)
);

INSERT INTO _role(title)
VALUES ('CLIENT'),
        ('RESTAURANT');
