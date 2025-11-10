DELETE FROM users;

INSERT INTO users (id, username, password, firstname, lastname, role, enabled)
VALUES
(1, 'qwe', '$2a$10$vJYLtGm05n0151bgf/bhPOLwg9QLltzPJH7l/1d1nNJGPwIeEbEt.', 'test', 'test', 'USER', true),
(2, 'admin', '$2a$10$iBjWG38Mv2Q8lHZA42S9EOMP9hsD5OU0588b9qBnXzZT4SwCdv5RO', 'admin', 'admin', 'ADMIN', true);