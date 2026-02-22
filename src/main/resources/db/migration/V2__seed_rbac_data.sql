INSERT INTO roles(role_name)
VALUES
  ('ROLE_ADMIN'),
  ('ROLE_USER')
ON CONFLICT (role_name) DO NOTHING;

INSERT INTO permissions(perm_name)
VALUES
  ('USER_READ'),
  ('USER_WRITE'),
  ('MENU_READ')
ON CONFLICT (perm_name) DO NOTHING;

INSERT INTO users(username, password_hash, enabled)
VALUES
  (
    'admin',
    '$2y$10$86pfR8fgb2HSZFuZvJeJaOv1laKuZEL8kxjMeDgFNY/ozT6utm47m',
    TRUE
  ),
  (
    'user',
    '$2y$10$.woiqRO0xrI.uyI6.ngKCeN15agUASlqIW5TCnYuM/2f5vibug9ba',
    TRUE
  )
ON CONFLICT (username) DO UPDATE
SET password_hash = EXCLUDED.password_hash, enabled = TRUE;

INSERT INTO role_permissions(role_id, permission_id)
SELECT r.id, p.id
FROM roles r
CROSS JOIN permissions p
WHERE (r.role_name = 'ROLE_ADMIN')
   OR (r.role_name = 'ROLE_USER' AND p.perm_name = 'USER_READ')
ON CONFLICT (role_id, permission_id) DO NOTHING;

INSERT INTO user_roles(user_id, role_id)
SELECT u.id, r.id
FROM users u
JOIN roles r ON (
    (u.username = 'admin' AND r.role_name = 'ROLE_ADMIN')
    OR
    (u.username = 'user' AND r.role_name = 'ROLE_USER')
)
ON CONFLICT (user_id, role_id) DO NOTHING;
