-- Users
INSERT INTO users (id, username, email, password) VALUES ('11111111-1111-1111-1111-111111111111', 'Alice', 'alice@example.com', 'hashed_password');
INSERT INTO users (id, username, email, password) VALUES ('22222222-2222-2222-2222-222222222222', 'Bob', 'bob@example.com', 'hashed_password');

-- Homes
INSERT INTO homes (id, name) VALUES ('33333333-3333-3333-3333-333333333333', 'Maison Verte');

-- Home Members
INSERT INTO home_members (home_id, user_id, role) VALUES ('33333333-3333-3333-3333-333333333333', '11111111-1111-1111-1111-111111111111', 'OWNER');
INSERT INTO home_members (home_id, user_id, role) VALUES ('33333333-3333-3333-3333-333333333333', '22222222-2222-2222-2222-222222222222', 'GUEST');

-- Rooms
INSERT INTO rooms (id, name, home_id) VALUES ('44444444-4444-4444-4444-444444444441', 'Salon', '33333333-3333-3333-3333-333333333333');
INSERT INTO rooms (id, name, home_id) VALUES ('44444444-4444-4444-4444-444444444442', 'Cuisine', '33333333-3333-3333-3333-333333333333');
INSERT INTO rooms (id, name, home_id) VALUES ('44444444-4444-4444-4444-444444444443', 'Chambre', '33333333-3333-3333-3333-333333333333');

-- Plants
INSERT INTO plants (id, name, species, watering_frequency, last_watered_date, room_id) VALUES ('55555555-5555-5555-5555-555555555501', 'Ficus', 'Ficus Benjamina', 7, '2023-10-25', '44444444-4444-4444-4444-444444444441');
INSERT INTO plants (id, name, species, watering_frequency, last_watered_date, room_id) VALUES ('55555555-5555-5555-5555-555555555502', 'Monstera', 'Monstera Deliciosa', 10, '2023-10-20', '44444444-4444-4444-4444-444444444441');
INSERT INTO plants (id, name, species, watering_frequency, last_watered_date, room_id) VALUES ('55555555-5555-5555-5555-555555555503', 'Pilea', 'Pilea Peperomioides', 5, '2023-10-26', '44444444-4444-4444-4444-444444444441');
INSERT INTO plants (id, name, species, watering_frequency, last_watered_date, room_id) VALUES ('55555555-5555-5555-5555-555555555504', 'Basilic', 'Ocimum basilicum', 2, '2023-10-27', '44444444-4444-4444-4444-444444444442');
INSERT INTO plants (id, name, species, watering_frequency, last_watered_date, room_id) VALUES ('55555555-5555-5555-5555-555555555505', 'Menthe', 'Mentha', 3, '2023-10-26', '44444444-4444-4444-4444-444444444442');
INSERT INTO plants (id, name, species, watering_frequency, last_watered_date, room_id) VALUES ('55555555-5555-5555-5555-555555555506', 'Cactus', 'Cactaceae', 30, '2023-09-15', '44444444-4444-4444-4444-444444444443');
INSERT INTO plants (id, name, species, watering_frequency, last_watered_date, room_id) VALUES ('55555555-5555-5555-5555-555555555507', 'Aloe Vera', 'Aloe Barbadensis', 15, '2023-10-10', '44444444-4444-4444-4444-444444444443');
INSERT INTO plants (id, name, species, watering_frequency, last_watered_date, room_id) VALUES ('55555555-5555-5555-5555-555555555508', 'Orchidée', 'Phalaenopsis', 10, '2023-10-18', '44444444-4444-4444-4444-444444444441');
INSERT INTO plants (id, name, species, watering_frequency, last_watered_date, room_id) VALUES ('55555555-5555-5555-5555-555555555509', 'Sansevieria', 'Sansevieria Trifasciata', 20, '2023-10-05', '44444444-4444-4444-4444-444444444443');
INSERT INTO plants (id, name, species, watering_frequency, last_watered_date, room_id) VALUES ('55555555-5555-5555-5555-555555555510', 'Calathea', 'Calathea Orbifolia', 4, '2023-10-24', '44444444-4444-4444-4444-444444444441');
INSERT INTO plants (id, name, species, watering_frequency, last_watered_date, room_id) VALUES ('55555555-5555-5555-5555-555555555511', 'Yucca', 'Yucca Elephantipes', 14, '2023-10-12', '44444444-4444-4444-4444-444444444441');
