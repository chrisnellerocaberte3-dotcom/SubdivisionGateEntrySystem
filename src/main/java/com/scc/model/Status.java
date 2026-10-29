package com.gateentry.model;

/** Account status. New accounts start as PENDING until an admin approves them. */
public enum Status {
    PENDING,
    ACTIVE,
    REJECTED
}