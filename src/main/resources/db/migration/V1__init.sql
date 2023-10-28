CREATE TABLE IF NOT EXISTS _role (
    id         serial primary key,
    title      varchar(50),
    created_at timestamp DEFAULT current_timestamp,
    updated_at timestamp DEFAULT current_timestamp

);

CREATE TABLE IF NOT EXISTS auth_user (
    id         serial primary key,
    email      varchar(50),
    password   varchar(100),
    created_at timestamp DEFAULT current_timestamp,
    updated_at timestamp DEFAULT current_timestamp
);

CREATE TABLE IF NOT EXISTS user_role (
    user_id bigint REFERENCES auth_user (id),
    role_id bigint REFERENCES _role (id)
);

INSERT INTO _role (title) VALUES
('CLIENT'),
('NUTRITIONIST'),
('RESTAURANT'),
('ADMIN');

INSERT INTO auth_user (email, password) VALUES
('admin@gmail.com', '$2a$12$TK5JzECdm04yRJ1Gtg5RW.ptVT1FMCqLywGBsbw97AEJHxRGwPJXK'),
('revaz@gmail.com', '$2a$12$TK5JzECdm04yRJ1Gtg5RW.ptVT1FMCqLywGBsbw97AEJHxRGwPJXK'),
('dieta@gmail.com', '$2a$12$TK5JzECdm04yRJ1Gtg5RW.ptVT1FMCqLywGBsbw97AEJHxRGwPJXK'),
('pizzery@gmail.com', '$2a$12$TK5JzECdm04yRJ1Gtg5RW.ptVT1FMCqLywGBsbw97AEJHxRGwPJXK'),
('steakhouse@gmail.com', '$2a$12$TK5JzECdm04yRJ1Gtg5RW.ptVT1FMCqLywGBsbw97AEJHxRGwPJXK'),
('burger@gmail.com', '$2a$12$TK5JzECdm04yRJ1Gtg5RW.ptVT1FMCqLywGBsbw97AEJHxRGwPJXK'),
('basil@gmail.com', '$2a$12$TK5JzECdm04yRJ1Gtg5RW.ptVT1FMCqLywGBsbw97AEJHxRGwPJXK'),
('jeanne@gmail.com', '$2a$12$TK5JzECdm04yRJ1Gtg5RW.ptVT1FMCqLywGBsbw97AEJHxRGwPJXK');


INSERT INTO user_role (user_id, role_id) VALUES
(1, 1),
(1, 2),
(1, 3),
(1, 4),
(2, 1),
(2, 2),
(3, 3),
(4, 3),
(5, 3),
(6, 3),
(7, 1),
(8, 1);