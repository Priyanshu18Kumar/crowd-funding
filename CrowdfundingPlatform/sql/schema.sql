-- ============================================================
--  Crowdfunding Platform - database schema (MySQL)
-- ------------------------------------------------------------
--  Run this ONCE to create the database and tables, and to add
--  the default admin account.
--
--  From a terminal:
--      mysql -u root -p < sql/schema.sql
--
--  Or just paste it into MySQL Workbench and run it.
-- ============================================================

CREATE DATABASE IF NOT EXISTS crowdfund;
USE crowdfund;

-- Drop in the right order (children before parents)
DROP TABLE IF EXISTS campaign_updates;
DROP TABLE IF EXISTS contributions;
DROP TABLE IF EXISTS campaigns;
DROP TABLE IF EXISTS activity_log;
DROP TABLE IF EXISTS settings;
DROP TABLE IF EXISTS users;

CREATE TABLE users (
    id       INT AUTO_INCREMENT PRIMARY KEY,
    name     VARCHAR(100) NOT NULL,
    email    VARCHAR(150) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    role     VARCHAR(20)  NOT NULL
);

CREATE TABLE campaigns (
    id            INT AUTO_INCREMENT PRIMARY KEY,
    title         VARCHAR(150) NOT NULL,
    description   VARCHAR(500),
    goal_amount   DOUBLE NOT NULL,
    raised_amount DOUBLE NOT NULL DEFAULT 0,
    creator_email VARCHAR(150) NOT NULL,
    status        VARCHAR(20)  NOT NULL DEFAULT 'PENDING'
);

CREATE TABLE contributions (
    id                INT AUTO_INCREMENT PRIMARY KEY,
    campaign_id       INT NOT NULL,
    contributor_email VARCHAR(150) NOT NULL,
    amount            DOUBLE NOT NULL,
    contrib_date      VARCHAR(20) NOT NULL,
    CONSTRAINT fk_contribution_campaign
        FOREIGN KEY (campaign_id) REFERENCES campaigns(id)
);

CREATE TABLE campaign_updates (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    campaign_id INT NOT NULL,
    message     VARCHAR(500) NOT NULL,
    CONSTRAINT fk_update_campaign
        FOREIGN KEY (campaign_id) REFERENCES campaigns(id)
);

CREATE TABLE settings (
    setting_key   VARCHAR(100) PRIMARY KEY,
    setting_value VARCHAR(200)
);

CREATE TABLE activity_log (
    id      INT AUTO_INCREMENT PRIMARY KEY,
    message VARCHAR(300) NOT NULL
);

-- Default admin account so you can log in immediately
INSERT INTO users (name, email, password, role) VALUES
    ('Platform Admin', 'admin@crowd.com', 'admin123', 'Admin');

-- Default system settings
INSERT INTO settings (setting_key, setting_value) VALUES
    ('Allow New Campaigns', 'yes'),
    ('Currency', 'INR'),
    ('Platform Name', 'CrowdFund');
