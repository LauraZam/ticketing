-- Identity Tables
CREATE TABLE users (
                       id BIGSERIAL PRIMARY KEY,
                       email VARCHAR(255) NOT NULL UNIQUE,
                       password_hash VARCHAR(255) NOT NULL,
                       display_name VARCHAR(255) NOT NULL,
                       role VARCHAR(50) NOT NULL,
                       enabled BOOLEAN NOT NULL DEFAULT TRUE,
                       created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE refresh_tokens (
                                id BIGSERIAL PRIMARY KEY,
                                token VARCHAR(255) NOT NULL UNIQUE,
                                user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                                expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
                                revoked BOOLEAN NOT NULL DEFAULT FALSE
);

-- Catalog Tables
CREATE TABLE venues (
                        id BIGSERIAL PRIMARY KEY,
                        name VARCHAR(255) NOT NULL,
                        address_line VARCHAR(255) NOT NULL,
                        city VARCHAR(100) NOT NULL,
                        timezone VARCHAR(50) NOT NULL,
                        total_capacity INT NOT NULL
);

CREATE TABLE sections (
                          id BIGSERIAL PRIMARY KEY,
                          venue_id BIGINT NOT NULL REFERENCES venues(id) ON DELETE CASCADE,
                          name VARCHAR(100) NOT NULL,
                          row_count INT NOT NULL,
                          seats_per_row INT NOT NULL,
                          price_tier VARCHAR(50) NOT NULL
);

CREATE TABLE seats (
                       id BIGSERIAL PRIMARY KEY,
                       section_id BIGINT NOT NULL REFERENCES sections(id) ON DELETE CASCADE,
                       row_label VARCHAR(10) NOT NULL,
                       seat_number INT NOT NULL,
                       accessible BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE events (
                        id BIGSERIAL PRIMARY KEY,
                        title VARCHAR(255) NOT NULL,
                        description TEXT,
                        starts_at TIMESTAMP WITH TIME ZONE NOT NULL,
                        doors_open_at TIMESTAMP WITH TIME ZONE NOT NULL,
                        status VARCHAR(50) NOT NULL,
                        sales_open_at TIMESTAMP WITH TIME ZONE NOT NULL,
                        sales_close_at TIMESTAMP WITH TIME ZONE NOT NULL,
                        created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
                        venue_id BIGINT NOT NULL REFERENCES venues(id),
                        organizer_id BIGINT NOT NULL REFERENCES users(id)
);

CREATE TABLE event_section_prices (
                                      id BIGSERIAL PRIMARY KEY,
                                      event_id BIGINT NOT NULL REFERENCES events(id) ON DELETE CASCADE,
                                      section_id BIGINT NOT NULL REFERENCES sections(id) ON DELETE CASCADE,
                                      price NUMERIC(10, 2) NOT NULL,
                                      currency VARCHAR(3) NOT NULL DEFAULT 'KZT'
);

-- Booking Tables
CREATE TABLE seat_holds (
                            id BIGSERIAL PRIMARY KEY,
                            hold_reference VARCHAR(64) NOT NULL UNIQUE,
                            event_id BIGINT NOT NULL REFERENCES events(id),
                            user_id BIGINT NOT NULL REFERENCES users(id),
                            expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
                            status VARCHAR(50) NOT NULL,
                            created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE seat_hold_seats (
                                 hold_id BIGINT NOT NULL REFERENCES seat_holds(id) ON DELETE CASCADE,
                                 seat_id BIGINT NOT NULL REFERENCES seats(id),
                                 PRIMARY KEY (hold_id, seat_id)
);

CREATE TABLE bookings (
                          id BIGSERIAL PRIMARY KEY,
                          booking_reference VARCHAR(64) NOT NULL UNIQUE,
                          event_id BIGINT NOT NULL REFERENCES events(id),
                          user_id BIGINT NOT NULL REFERENCES users(id),
                          status VARCHAR(50) NOT NULL,
                          subtotal NUMERIC(10, 2) NOT NULL,
                          fees NUMERIC(10, 2) NOT NULL,
                          total NUMERIC(10, 2) NOT NULL,
                          currency VARCHAR(3) NOT NULL DEFAULT 'KZT',
                          created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
                          confirmed_at TIMESTAMP WITH TIME ZONE,
                          cancelled_at TIMESTAMP WITH TIME ZONE,
                          version BIGINT NOT NULL DEFAULT 0
);

CREATE TABLE tickets (
                         id BIGSERIAL PRIMARY KEY,
                         ticket_code VARCHAR(64) NOT NULL UNIQUE,
                         booking_id BIGINT NOT NULL REFERENCES bookings(id) ON DELETE CASCADE,
                         seat_id BIGINT NOT NULL REFERENCES seats(id),
                         event_id BIGINT NOT NULL REFERENCES events(id),
                         status VARCHAR(50) NOT NULL,
                         issued_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
                         checked_in_at TIMESTAMP WITH TIME ZONE,
                         price_paid NUMERIC(10, 2) NOT NULL,
                         CONSTRAINT uq_ticket_event_seat UNIQUE (event_id, seat_id)
);

-- Payment Table
CREATE TABLE payments (
                          id BIGSERIAL PRIMARY KEY,
                          booking_id BIGINT NOT NULL UNIQUE REFERENCES bookings(id) ON DELETE CASCADE,
                          transaction_id VARCHAR(100),
                          method VARCHAR(50) NOT NULL,
                          status VARCHAR(50) NOT NULL,
                          amount NUMERIC(10, 2) NOT NULL,
                          currency VARCHAR(3) NOT NULL DEFAULT 'KZT',
                          failure_code VARCHAR(100),
                          paid_at TIMESTAMP WITH TIME ZONE
);

-- Messaging & Audit Tables
CREATE TABLE outbox_messages (
                                 id BIGSERIAL PRIMARY KEY,
                                 aggregate_type VARCHAR(100) NOT NULL,
                                 aggregate_id VARCHAR(100) NOT NULL,
                                 event_type VARCHAR(100) NOT NULL,
                                 payload TEXT NOT NULL,
                                 created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                 published_at TIMESTAMP WITH TIME ZONE
);

CREATE TABLE processed_messages (
                                    id BIGSERIAL PRIMARY KEY,
                                    message_id VARCHAR(255) NOT NULL UNIQUE,
                                    consumed_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE audit_entries (
                               id BIGSERIAL PRIMARY KEY,
                               actor_id BIGINT,
                               action VARCHAR(100) NOT NULL,
                               entity_type VARCHAR(100) NOT NULL,
                               entity_id VARCHAR(100) NOT NULL,
                               created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Essential Performance Indexes
CREATE INDEX idx_events_status_starts ON events(status, starts_at);
CREATE INDEX idx_events_venue ON events(venue_id);
CREATE INDEX idx_seats_section ON seats(section_id);
CREATE INDEX idx_tickets_booking ON tickets(booking_id);
CREATE INDEX idx_bookings_user ON bookings(user_id);
CREATE INDEX idx_seat_holds_user ON seat_holds(user_id);