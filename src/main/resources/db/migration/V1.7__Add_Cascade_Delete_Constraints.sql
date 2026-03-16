-- Add ON DELETE CASCADE to all foreign key constraints so that deleting a
-- parent row automatically removes all dependent child rows.

-- rooms → homes
ALTER TABLE rooms DROP CONSTRAINT fk_rooms_home_id;
ALTER TABLE rooms ADD CONSTRAINT fk_rooms_home_id
    FOREIGN KEY (home_id) REFERENCES homes(id) ON DELETE CASCADE;

-- plants → rooms
ALTER TABLE plants DROP CONSTRAINT fk_plants_room_id;
ALTER TABLE plants ADD CONSTRAINT fk_plants_room_id
    FOREIGN KEY (room_id) REFERENCES rooms(id) ON DELETE CASCADE;

-- care_logs → plants
ALTER TABLE care_logs DROP CONSTRAINT fk_care_logs_plant_id;
ALTER TABLE care_logs ADD CONSTRAINT fk_care_logs_plant_id
    FOREIGN KEY (plant_id) REFERENCES plants(id) ON DELETE CASCADE;

-- care_logs → users
ALTER TABLE care_logs DROP CONSTRAINT fk_care_logs_user_id;
ALTER TABLE care_logs ADD CONSTRAINT fk_care_logs_user_id
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;

-- home_members → homes
ALTER TABLE home_members DROP CONSTRAINT fk_home_members_home_id;
ALTER TABLE home_members ADD CONSTRAINT fk_home_members_home_id
    FOREIGN KEY (home_id) REFERENCES homes(id) ON DELETE CASCADE;

-- home_members → users
ALTER TABLE home_members DROP CONSTRAINT fk_home_members_user_id;
ALTER TABLE home_members ADD CONSTRAINT fk_home_members_user_id
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;

-- refresh_tokens → users already has ON DELETE CASCADE (added in V1.4), no change needed.
