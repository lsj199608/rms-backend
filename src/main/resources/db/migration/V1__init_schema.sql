CREATE TABLE IF NOT EXISTS test (
  id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  code       VARCHAR(50)  NOT NULL UNIQUE, -- PHARMACY, HOSPITAL ...
  name       VARCHAR(100) NOT NULL,        -- 약국, 병원 ...
  created_at TIMESTAMPTZ  NOT NULL DEFAULT now()
);

INSERT INTO test (code, name)
VALUES
  ('PHARMACY', '약국'),
  ('HOSPITAL', '병원'),
  ('CLINIC',   '의원'),
  ('CAFE',     '카페'),
  ('PUBLIC',   '공공시설')
ON CONFLICT (code) DO NOTHING;