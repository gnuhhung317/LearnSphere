-- Create users table for StudyHub user-service
-- Stores user profile data synced from Keycloak
-- Passwords are NOT stored here - managed by Keycloak

CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    keycloak_user_id VARCHAR(255) NOT NULL UNIQUE,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    theme VARCHAR(20) NOT NULL DEFAULT 'AUTO',
    language VARCHAR(20) NOT NULL DEFAULT 'ENGLISH',
    notifications JSONB,
    accessibility JSONB,
    privacy JSONB,
    profile_visibility VARCHAR(50) DEFAULT 'organization',
    status VARCHAR(20) DEFAULT 'active',
    is_verified BOOLEAN DEFAULT FALSE,
    is_active BOOLEAN DEFAULT TRUE,
    last_login_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for performance
CREATE INDEX idx_users_keycloak_user_id ON users(keycloak_user_id);
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_status ON users(status);
CREATE INDEX idx_users_is_active ON users(is_active, is_verified);

-- Add comments for documentation
COMMENT ON TABLE users IS 'User profile data synced from Keycloak. Passwords managed by Keycloak.';
COMMENT ON COLUMN users.keycloak_user_id IS 'Unique identifier from Keycloak, links profile to identity';
COMMENT ON COLUMN users.notifications IS 'User notification preferences as JSON';
COMMENT ON COLUMN users.accessibility IS 'User accessibility settings as JSON';
COMMENT ON COLUMN users.privacy IS 'User privacy settings as JSON';
