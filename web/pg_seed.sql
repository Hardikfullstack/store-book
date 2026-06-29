BEGIN;

CREATE TABLE IF NOT EXISTS "public"."user" (
  "id" text NOT NULL,
  "created_at" integer NOT NULL,
  "phone_number" text NULL,
  "role" text NOT NULL DEFAULT 'owner',
  PRIMARY KEY ("id")
);

-- Seed User: G7RjDGMQJuZ4GrcDaZIwcEpQWjx2 (Phone: +917777777777)
INSERT INTO "public"."user" (id, phone_number, created_at, role) VALUES ('G7RjDGMQJuZ4GrcDaZIwcEpQWjx2', '+917777777777', 1781760497, 'owner') ON CONFLICT (id) DO NOTHING;
INSERT INTO "public"."item" (id, store_id, name, quantity, unit, buy_price, sell_price, low_stock_threshold, category, is_deleted, updated_at) VALUES ('09672abc-4610-47c6-b762-2c44771cbbf0', 'G7RjDGMQJuZ4GrcDaZIwcEpQWjx2', 'Seeded Milk 1L', 50, 'litre', 45, 55, 10, 'Dairy', false, 1781760497) ON CONFLICT DO NOTHING;
INSERT INTO "public"."item" (id, store_id, name, quantity, unit, buy_price, sell_price, low_stock_threshold, category, is_deleted, updated_at) VALUES ('029665b0-7f8f-459d-94c9-759a015548da', 'G7RjDGMQJuZ4GrcDaZIwcEpQWjx2', 'Seeded Bread', 20, 'pcs', 30, 40, 5, 'Grocery', false, 1781760497) ON CONFLICT DO NOTHING;

-- Seed User: jTlDSRAtZJbh1OLPFF67yrI5O8h2 (Phone: +916666666666)
INSERT INTO "public"."user" (id, phone_number, created_at, role) VALUES ('jTlDSRAtZJbh1OLPFF67yrI5O8h2', '+916666666666', 1781699960, 'owner') ON CONFLICT (id) DO NOTHING;
INSERT INTO "public"."item" (id, store_id, name, quantity, unit, buy_price, sell_price, low_stock_threshold, category, is_deleted, updated_at) VALUES ('0d920ced-5e15-4845-9f5d-6eec68414b30', 'jTlDSRAtZJbh1OLPFF67yrI5O8h2', 'Seeded Milk 1L', 50, 'litre', 45, 55, 10, 'Dairy', false, 1781699960) ON CONFLICT DO NOTHING;
INSERT INTO "public"."item" (id, store_id, name, quantity, unit, buy_price, sell_price, low_stock_threshold, category, is_deleted, updated_at) VALUES ('0b91ab76-2075-42d9-b24e-5bec1270f0d9', 'jTlDSRAtZJbh1OLPFF67yrI5O8h2', 'Seeded Bread', 20, 'pcs', 30, 40, 5, 'Grocery', false, 1781699960) ON CONFLICT DO NOTHING;

-- Seed User: p1aMDfZT2NOTpkhoLtoDv5uUi2p2 (Phone: +919999999999)
INSERT INTO "public"."user" (id, phone_number, created_at, role) VALUES ('p1aMDfZT2NOTpkhoLtoDv5uUi2p2', '+919999999999', 1781700014, 'owner') ON CONFLICT (id) DO NOTHING;
INSERT INTO "public"."item" (id, store_id, name, quantity, unit, buy_price, sell_price, low_stock_threshold, category, is_deleted, updated_at) VALUES ('496f4b08-745b-40cd-9c15-f2e4716b50d5', 'p1aMDfZT2NOTpkhoLtoDv5uUi2p2', 'Seeded Milk 1L', 50, 'litre', 45, 55, 10, 'Dairy', false, 1781700014) ON CONFLICT DO NOTHING;
INSERT INTO "public"."item" (id, store_id, name, quantity, unit, buy_price, sell_price, low_stock_threshold, category, is_deleted, updated_at) VALUES ('791c1c79-b5ac-43fe-9f7f-a2e8fad8fc64', 'p1aMDfZT2NOTpkhoLtoDv5uUi2p2', 'Seeded Bread', 20, 'pcs', 30, 40, 5, 'Grocery', false, 1781700014) ON CONFLICT DO NOTHING;

-- Seed User: xDNSxAwvpIRKZeXo6x2MlSHtWxn2 (Phone: +918888888888)
INSERT INTO "public"."user" (id, phone_number, created_at, role) VALUES ('xDNSxAwvpIRKZeXo6x2MlSHtWxn2', '+918888888888', 1781859448, 'owner') ON CONFLICT (id) DO NOTHING;
INSERT INTO "public"."item" (id, store_id, name, quantity, unit, buy_price, sell_price, low_stock_threshold, category, is_deleted, updated_at) VALUES ('7600907d-b5de-471b-81db-6d5ace6a5be7', 'xDNSxAwvpIRKZeXo6x2MlSHtWxn2', 'Seeded Milk 1L', 50, 'litre', 45, 55, 10, 'Dairy', false, 1781859448) ON CONFLICT DO NOTHING;
INSERT INTO "public"."item" (id, store_id, name, quantity, unit, buy_price, sell_price, low_stock_threshold, category, is_deleted, updated_at) VALUES ('aad81da4-4b1c-457d-bc64-f491d69ad255', 'xDNSxAwvpIRKZeXo6x2MlSHtWxn2', 'Seeded Bread', 20, 'pcs', 30, 40, 5, 'Grocery', false, 1781859448) ON CONFLICT DO NOTHING;

COMMIT;
