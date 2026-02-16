CREATE EXTENSION IF NOT EXISTS "pgcrypto";

ALTER TABLE users ADD COLUMN new_id UUID DEFAULT gen_random_uuid();
ALTER TABLE homes ADD COLUMN new_id UUID DEFAULT gen_random_uuid();

ALTER TABLE rooms ADD COLUMN new_id UUID DEFAULT gen_random_uuid();
ALTER TABLE rooms ADD COLUMN new_home_id UUID;

ALTER TABLE plants ADD COLUMN new_id UUID DEFAULT gen_random_uuid();
ALTER TABLE plants ADD COLUMN new_room_id UUID;

ALTER TABLE care_logs ADD COLUMN new_id UUID DEFAULT gen_random_uuid();
ALTER TABLE care_logs ADD COLUMN new_plant_id UUID;
ALTER TABLE care_logs ADD COLUMN new_user_id UUID;

ALTER TABLE home_members ADD COLUMN new_home_id UUID;
ALTER TABLE home_members ADD COLUMN new_user_id UUID;


UPDATE rooms r SET new_home_id = h.new_id FROM homes h WHERE r.home_id = h.id;

UPDATE plants p SET new_room_id = r.new_id FROM rooms r WHERE p.room_id = r.id;

UPDATE care_logs c SET new_plant_id = p.new_id FROM plants p WHERE c.plant_id = p.id;
UPDATE care_logs c SET new_user_id = u.new_id FROM users u WHERE c.user_id = u.id;
UPDATE home_members hm SET new_home_id = h.new_id FROM homes h WHERE hm.home_id = h.id;
UPDATE home_members hm SET new_user_id = u.new_id FROM users u WHERE hm.user_id = u.id;


ALTER TABLE users DROP CONSTRAINT users_pkey CASCADE;
ALTER TABLE users DROP COLUMN id;
ALTER TABLE users RENAME COLUMN new_id TO id;
ALTER TABLE users ADD PRIMARY KEY (id);

ALTER TABLE homes DROP CONSTRAINT homes_pkey CASCADE;
ALTER TABLE homes DROP COLUMN id;
ALTER TABLE homes RENAME COLUMN new_id TO id;
ALTER TABLE homes ADD PRIMARY KEY (id);

ALTER TABLE rooms DROP CONSTRAINT rooms_pkey CASCADE;
ALTER TABLE rooms DROP COLUMN id;
ALTER TABLE rooms DROP COLUMN home_id;
ALTER TABLE rooms RENAME COLUMN new_id TO id;
ALTER TABLE rooms RENAME COLUMN new_home_id TO home_id;
ALTER TABLE rooms ADD PRIMARY KEY (id);
ALTER TABLE rooms ALTER COLUMN home_id SET NOT NULL;
ALTER TABLE rooms ADD CONSTRAINT fk_rooms_home_id FOREIGN KEY (home_id) REFERENCES homes(id);

ALTER TABLE plants DROP CONSTRAINT plants_pkey CASCADE;
ALTER TABLE plants DROP COLUMN id;
ALTER TABLE plants DROP COLUMN room_id;
ALTER TABLE plants RENAME COLUMN new_id TO id;
ALTER TABLE plants RENAME COLUMN new_room_id TO room_id;
ALTER TABLE plants ADD PRIMARY KEY (id);
ALTER TABLE plants ALTER COLUMN room_id SET NOT NULL;
ALTER TABLE plants ADD CONSTRAINT fk_plants_room_id FOREIGN KEY (room_id) REFERENCES rooms(id);

ALTER TABLE care_logs DROP CONSTRAINT care_logs_pkey CASCADE;
ALTER TABLE care_logs DROP COLUMN id;
ALTER TABLE care_logs DROP COLUMN plant_id;
ALTER TABLE care_logs DROP COLUMN user_id;
ALTER TABLE care_logs RENAME COLUMN new_id TO id;
ALTER TABLE care_logs RENAME COLUMN new_plant_id TO plant_id;
ALTER TABLE care_logs RENAME COLUMN new_user_id TO user_id;
ALTER TABLE care_logs ADD PRIMARY KEY (id);
ALTER TABLE care_logs ALTER COLUMN plant_id SET NOT NULL;
ALTER TABLE care_logs ALTER COLUMN user_id SET NOT NULL;
ALTER TABLE care_logs ADD CONSTRAINT fk_care_logs_plant_id FOREIGN KEY (plant_id) REFERENCES plants(id);
ALTER TABLE care_logs ADD CONSTRAINT fk_care_logs_user_id FOREIGN KEY (user_id) REFERENCES users(id);

ALTER TABLE home_members DROP CONSTRAINT home_members_pkey CASCADE;
ALTER TABLE home_members DROP COLUMN home_id;
ALTER TABLE home_members DROP COLUMN user_id;
ALTER TABLE home_members RENAME COLUMN new_home_id TO home_id;
ALTER TABLE home_members RENAME COLUMN new_user_id TO user_id;
ALTER TABLE home_members ADD PRIMARY KEY (home_id, user_id);
ALTER TABLE home_members ALTER COLUMN home_id SET NOT NULL;
ALTER TABLE home_members ALTER COLUMN user_id SET NOT NULL;
ALTER TABLE home_members ADD CONSTRAINT fk_home_members_home_id FOREIGN KEY (home_id) REFERENCES homes(id);
ALTER TABLE home_members ADD CONSTRAINT fk_home_members_user_id FOREIGN KEY (user_id) REFERENCES users(id);
