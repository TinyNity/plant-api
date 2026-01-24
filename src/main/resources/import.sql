-- Users
INSERT INTO users (id, username, email) VALUES (1, 'Alice', 'alice@example.com');
INSERT INTO users (id, username, email) VALUES (2, 'Bob', 'bob@example.com');
ALTER SEQUENCE users_id_seq RESTART WITH 3;

-- Homes
INSERT INTO homes (id, name) VALUES (1, 'Maison Verte');
ALTER SEQUENCE homes_id_seq RESTART WITH 2;

-- Home Members
INSERT INTO home_members (home_id, user_id, role) VALUES (1, 1, 'OWNER');
INSERT INTO home_members (home_id, user_id, role) VALUES (1, 2, 'GUEST');

-- Rooms
INSERT INTO rooms (id, name, home_id) VALUES (1, 'Salon', 1);
ALTER SEQUENCE rooms_id_seq RESTART WITH 2;

-- Plants
INSERT INTO plants (id, name, species, watering_frequency, last_watered_date, room_id) VALUES (1, 'Ficus', 'Ficus Benjamina', 7, '2023-10-25', 1);
ALTER SEQUENCE plants_id_seq RESTART WITH 2;
