-- 1. Users (1 ADMIN, 2 ORGANIZER, 5 CUSTOMER)
-- Password for all users is: password123 (BCrypt hash)
INSERT INTO users (email, password_hash, display_name, role, enabled) VALUES
                                                                          ('admin@kimep.kz', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xD0m1bC.iJjE5B3u', 'System Admin', 'ADMIN', true),
                                                                          ('org1@kimep.kz', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xD0m1bC.iJjE5B3u', 'Almaty Concerts Org', 'ORGANIZER', true),
                                                                          ('org2@kimep.kz', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xD0m1bC.iJjE5B3u', 'Tech Conf Global', 'ORGANIZER', true),
                                                                          ('user1@kimep.kz', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xD0m1bC.iJjE5B3u', 'Alice Smith', 'CUSTOMER', true),
                                                                          ('user2@kimep.kz', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xD0m1bC.iJjE5B3u', 'Bob Jones', 'CUSTOMER', true),
                                                                          ('user3@kimep.kz', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xD0m1bC.iJjE5B3u', 'Charlie Brown', 'CUSTOMER', true),
                                                                          ('user4@kimep.kz', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xD0m1bC.iJjE5B3u', 'Diana Prince', 'CUSTOMER', true),
                                                                          ('user5@kimep.kz', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xD0m1bC.iJjE5B3u', 'Evan Wright', 'CUSTOMER', true);

-- 2. Venues (3 Venues)
INSERT INTO venues (name, address_line, city, timezone, total_capacity) VALUES
                                                                            ('Astana Arena Stadium', 'Turan Ave 48', 'Astana', 'Asia/Almaty', 1000),
                                                                            ('Republic Palace', 'Dostyk Ave 56', 'Almaty', 'Asia/Almaty', 600),
                                                                            ('KIMEP Great Hall', 'Abai Ave 4', 'Almaty', 'Asia/Almaty', 400);

-- 3. Sections (2 per Venue)
INSERT INTO sections (venue_id, name, row_count, seats_per_row, price_tier) VALUES
                                                                                (1, 'VIP Sector A', 10, 20, 'VIP'),       -- 200 seats
                                                                                (1, 'Main Arena B', 20, 40, 'STANDARD'),  -- 800 seats -> Total Venue 1 = 1,000 seats
                                                                                (2, 'Orchestra Hall', 15, 20, 'VIP'),     -- 300 seats
                                                                                (2, 'Balcony Tier', 15, 20, 'STANDARD'),  -- 300 seats -> Total Venue 2 = 600 seats
                                                                                (3, 'Main Hall Ground', 10, 25, 'REGULAR'),-- 250 seats
                                                                                (3, 'Gallery Tier', 5, 30, 'ECONOMY');   -- 150 seats -> Total Venue 3 = 400 seats

-- 4. Bulk Generate Exactly 2,000 Seats across all sections
INSERT INTO seats (section_id, row_label, seat_number, accessible)
SELECT
    s.id AS section_id,
    'Row ' || r.n AS row_label,
    st.n AS seat_number,
    (st.n % 10 = 0) AS accessible
FROM sections s
         CROSS JOIN LATERAL generate_series(1, s.row_count) AS r(n)
         CROSS JOIN LATERAL generate_series(1, s.seats_per_row) AS st(n);

-- 5. Events (10 Events across statuses: PAST, ON SALE, SOLD OUT, DRAFT)
INSERT INTO events (title, description, starts_at, doors_open_at, status, sales_open_at, sales_close_at, venue_id, organizer_id) VALUES
                                                                                                                                     ('Central Asia Tech Fest 2026', 'Largest tech conference in Almaty', CURRENT_TIMESTAMP + INTERVAL '10 days', CURRENT_TIMESTAMP + INTERVAL '10 days' - INTERVAL '2 hours', 'PUBLISHED', CURRENT_TIMESTAMP - INTERVAL '5 days', CURRENT_TIMESTAMP + INTERVAL '9 days', 3, 3),
                                                                                                                                     ('Dimash Qudaibergen Concert', 'Live Symphony Performance', CURRENT_TIMESTAMP + INTERVAL '30 days', CURRENT_TIMESTAMP + INTERVAL '30 days' - INTERVAL '3 hours', 'PUBLISHED', CURRENT_TIMESTAMP - INTERVAL '10 days', CURRENT_TIMESTAMP + INTERVAL '29 days', 1, 2),
                                                                                                                                     ('KIMEP Alumni Gala 2026', 'Annual homecoming event', CURRENT_TIMESTAMP + INTERVAL '15 days', CURRENT_TIMESTAMP + INTERVAL '15 days' - INTERVAL '1 hour', 'PUBLISHED', CURRENT_TIMESTAMP - INTERVAL '2 days', CURRENT_TIMESTAMP + INTERVAL '14 days', 3, 2),
                                                                                                                                     ('Astana Jazz Night', 'Evening of international jazz', CURRENT_TIMESTAMP - INTERVAL '5 days', CURRENT_TIMESTAMP - INTERVAL '5 days' - INTERVAL '1 hour', 'COMPLETED', CURRENT_TIMESTAMP - INTERVAL '20 days', CURRENT_TIMESTAMP - INTERVAL '6 days', 2, 2),
                                                                                                                                     ('Rock Symphony 2026', 'Orchestral rock covers', CURRENT_TIMESTAMP + INTERVAL '45 days', CURRENT_TIMESTAMP + INTERVAL '45 days' - INTERVAL '2 hours', 'PUBLISHED', CURRENT_TIMESTAMP - INTERVAL '1 day', CURRENT_TIMESTAMP + INTERVAL '44 days', 2, 2),
                                                                                                                                     ('Almaty Marathon Expo', 'Exhibition & runner pack pickup', CURRENT_TIMESTAMP + INTERVAL '60 days', CURRENT_TIMESTAMP + INTERVAL '60 days' - INTERVAL '1 hour', 'DRAFT', CURRENT_TIMESTAMP + INTERVAL '10 days', CURRENT_TIMESTAMP + INTERVAL '59 days', 3, 3),
                                                                                                                                     ('Cybersecurity Summit 2026', 'Infosec conference', CURRENT_TIMESTAMP + INTERVAL '20 days', CURRENT_TIMESTAMP + INTERVAL '20 days' - INTERVAL '1 hour', 'PUBLISHED', CURRENT_TIMESTAMP - INTERVAL '3 days', CURRENT_TIMESTAMP + INTERVAL '19 days', 3, 3),
                                                                                                                                     ('Standup Comedy Special', 'Live comedy show', CURRENT_TIMESTAMP - INTERVAL '15 days', CURRENT_TIMESTAMP - INTERVAL '15 days' - INTERVAL '1 hour', 'COMPLETED', CURRENT_TIMESTAMP - INTERVAL '30 days', CURRENT_TIMESTAMP - INTERVAL '16 days', 2, 2),
                                                                                                                                     ('Spring Music Festival', 'Open air festival', CURRENT_TIMESTAMP + INTERVAL '90 days', CURRENT_TIMESTAMP + INTERVAL '90 days' - INTERVAL '2 hours', 'DRAFT', CURRENT_TIMESTAMP + INTERVAL '15 days', CURRENT_TIMESTAMP + INTERVAL '89 days', 1, 2),
                                                                                                                                     ('Exclusive Chamber Concert', 'Sold out VIP concert', CURRENT_TIMESTAMP + INTERVAL '5 days', CURRENT_TIMESTAMP + INTERVAL '5 days' - INTERVAL '1 hour', 'PUBLISHED', CURRENT_TIMESTAMP - INTERVAL '15 days', CURRENT_TIMESTAMP + INTERVAL '4 days', 2, 2);

-- 6. Pricing for Event Sections
INSERT INTO event_section_prices (event_id, section_id, price, currency) VALUES
                                                                             (1, 5, 15000.00, 'KZT'), (1, 6, 8000.00, 'KZT'),
                                                                             (2, 1, 50000.00, 'KZT'), (2, 2, 25000.00, 'KZT'),
                                                                             (3, 5, 20000.00, 'KZT'), (3, 6, 10000.00, 'KZT'),
                                                                             (4, 3, 30000.00, 'KZT'), (4, 4, 15000.00, 'KZT'),
                                                                             (5, 3, 35000.00, 'KZT'), (5, 4, 18000.00, 'KZT'),
                                                                             (7, 5, 25000.00, 'KZT'), (7, 6, 12000.00, 'KZT'),
                                                                             (8, 3, 12000.00, 'KZT'), (8, 4, 7000.00, 'KZT'),
                                                                             (10, 3, 40000.00, 'KZT'), (10, 4, 20000.00, 'KZT');

-- 7. Seed 15 Bookings
INSERT INTO bookings (booking_reference, event_id, user_id, status, subtotal, fees, total, currency, created_at, version) VALUES
                                                                                                                              ('BK-1001', 1, 4, 'CONFIRMED', 30000.00, 1500.00, 31500.00, 'KZT', CURRENT_TIMESTAMP - INTERVAL '2 days', 0),
                                                                                                                              ('BK-1002', 1, 5, 'CONFIRMED', 15000.00, 750.00, 15750.00, 'KZT', CURRENT_TIMESTAMP - INTERVAL '2 days', 0),
                                                                                                                              ('BK-1003', 2, 6, 'CONFIRMED', 100000.00, 5000.00, 105000.00, 'KZT', CURRENT_TIMESTAMP - INTERVAL '1 day', 0),
                                                                                                                              ('BK-1004', 2, 7, 'CONFIRMED', 50000.00, 2500.00, 52500.00, 'KZT', CURRENT_TIMESTAMP - INTERVAL '1 day', 0),
                                                                                                                              ('BK-1005', 3, 8, 'CONFIRMED', 20000.00, 1000.00, 21000.00, 'KZT', CURRENT_TIMESTAMP - INTERVAL '12 hours', 0),
                                                                                                                              ('BK-1006', 4, 4, 'CONFIRMED', 30000.00, 1500.00, 31500.00, 'KZT', CURRENT_TIMESTAMP - INTERVAL '10 days', 0),
                                                                                                                              ('BK-1007', 5, 5, 'PENDING', 35000.00, 1750.00, 36750.00, 'KZT', CURRENT_TIMESTAMP - INTERVAL '5 minutes', 0),
                                                                                                                              ('BK-1008', 5, 6, 'CONFIRMED', 18000.00, 900.00, 18900.00, 'KZT', CURRENT_TIMESTAMP - INTERVAL '3 hours', 0),
                                                                                                                              ('BK-1009', 7, 7, 'CANCELLED', 25000.00, 1250.00, 26250.00, 'KZT', CURRENT_TIMESTAMP - INTERVAL '1 day', 0),
                                                                                                                              ('BK-1010', 7, 8, 'CONFIRMED', 12000.00, 600.00, 12600.00, 'KZT', CURRENT_TIMESTAMP - INTERVAL '6 hours', 0),
                                                                                                                              ('BK-1011', 8, 4, 'CONFIRMED', 12000.00, 600.00, 12600.00, 'KZT', CURRENT_TIMESTAMP - INTERVAL '20 days', 0),
                                                                                                                              ('BK-1012', 10, 5, 'CONFIRMED', 80000.00, 4000.00, 84000.00, 'KZT', CURRENT_TIMESTAMP - INTERVAL '2 days', 0),
                                                                                                                              ('BK-1013', 10, 6, 'CONFIRMED', 40000.00, 2000.00, 42000.00, 'KZT', CURRENT_TIMESTAMP - INTERVAL '1 day', 0),
                                                                                                                              ('BK-1014', 10, 7, 'CONFIRMED', 20000.00, 1000.00, 21000.00, 'KZT', CURRENT_TIMESTAMP - INTERVAL '18 hours', 0),
                                                                                                                              ('BK-1015', 10, 8, 'CONFIRMED', 20000.00, 1000.00, 21000.00, 'KZT', CURRENT_TIMESTAMP - INTERVAL '4 hours', 0);

-- 8. Seed Tickets for Bookings
INSERT INTO tickets (ticket_code, booking_id, seat_id, event_id, status, price_paid) VALUES
                                                                                         ('TCK-1001-A', 1, 1501, 1, 'ISSUED', 15000.00),
                                                                                         ('TCK-1001-B', 1, 1502, 1, 'ISSUED', 15000.00),
                                                                                         ('TCK-1002-A', 2, 1503, 1, 'ISSUED', 15000.00),
                                                                                         ('TCK-1003-A', 3, 1, 2, 'ISSUED', 50000.00),
                                                                                         ('TCK-1003-B', 3, 2, 2, 'ISSUED', 50000.00),
                                                                                         ('TCK-1004-A', 4, 3, 2, 'ISSUED', 50000.00),
                                                                                         ('TCK-1005-A', 5, 1504, 3, 'ISSUED', 20000.00),
                                                                                         ('TCK-1006-A', 6, 1001, 4, 'CHECKED_IN', 30000.00),
                                                                                         ('TCK-1008-A', 8, 1301, 5, 'ISSUED', 18000.00),
                                                                                         ('TCK-1010-A', 10, 1751, 7, 'ISSUED', 12000.00),
                                                                                         ('TCK-1011-A', 11, 1002, 8, 'CHECKED_IN', 12000.00),
                                                                                         ('TCK-1012-A', 12, 1003, 10, 'ISSUED', 40000.00),
                                                                                         ('TCK-1012-B', 12, 1004, 10, 'ISSUED', 40000.00),
                                                                                         ('TCK-1013-A', 13, 1005, 10, 'ISSUED', 40000.00),
                                                                                         ('TCK-1014-A', 14, 1302, 10, 'ISSUED', 20000.00),
                                                                                         ('TCK-1015-A', 15, 1303, 10, 'ISSUED', 20000.00);