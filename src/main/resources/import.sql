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
INSERT INTO rooms (id, name, home_id) VALUES (2, 'Cuisine', 1);
INSERT INTO rooms (id, name, home_id) VALUES (3, 'Chambre', 1);
ALTER SEQUENCE rooms_id_seq RESTART WITH 4;

-- Plants
INSERT INTO plants (id, name, species, watering_frequency, last_watered_date, room_id) VALUES (1, 'Ficus', 'Ficus Benjamina', 7, '2023-10-25', 1);
INSERT INTO plants (id, name, species, watering_frequency, last_watered_date, room_id) VALUES (2, 'Monstera', 'Monstera Deliciosa', 10, '2023-10-20', 1);
INSERT INTO plants (id, name, species, watering_frequency, last_watered_date, room_id) VALUES (3, 'Pilea', 'Pilea Peperomioides', 5, '2023-10-26', 1);
INSERT INTO plants (id, name, species, watering_frequency, last_watered_date, room_id) VALUES (4, 'Basilic', 'Ocimum basilicum', 2, '2023-10-27', 2);
INSERT INTO plants (id, name, species, watering_frequency, last_watered_date, room_id) VALUES (5, 'Menthe', 'Mentha', 3, '2023-10-26', 2);
INSERT INTO plants (id, name, species, watering_frequency, last_watered_date, room_id) VALUES (6, 'Cactus', 'Cactaceae', 30, '2023-09-15', 3);
INSERT INTO plants (id, name, species, watering_frequency, last_watered_date, room_id) VALUES (7, 'Aloe Vera', 'Aloe Barbadensis', 15, '2023-10-10', 3);
INSERT INTO plants (id, name, species, watering_frequency, last_watered_date, room_id) VALUES (8, 'Orchidée', 'Phalaenopsis', 10, '2023-10-18', 1);
INSERT INTO plants (id, name, species, watering_frequency, last_watered_date, room_id) VALUES (9, 'Sansevieria', 'Sansevieria Trifasciata', 20, '2023-10-05', 3);
INSERT INTO plants (id, name, species, watering_frequency, last_watered_date, room_id) VALUES (10, 'Calathea', 'Calathea Orbifolia', 4, '2023-10-24', 1);
INSERT INTO plants (id, name, species, watering_frequency, last_watered_date, room_id) VALUES (11, 'Yucca', 'Yucca Elephantipes', 14, '2023-10-12', 1);

ALTER SEQUENCE plants_id_seq RESTART WITH 12;
