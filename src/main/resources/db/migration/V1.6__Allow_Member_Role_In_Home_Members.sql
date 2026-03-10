ALTER TABLE home_members
DROP CONSTRAINT IF EXISTS home_members_role_check;

ALTER TABLE home_members
ADD CONSTRAINT home_members_role_check
CHECK (role IN ('GUEST', 'MEMBER', 'ADMIN'));
