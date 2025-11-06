-- Migration: Add used_by column to admin_invites table
-- This migration adds a column to track which user used each invite code

ALTER TABLE admin_invites 
ADD COLUMN used_by BIGINT NULL,
ADD CONSTRAINT fk_admin_invites_used_by 
    FOREIGN KEY (used_by) REFERENCES users(id) ON DELETE SET NULL;

