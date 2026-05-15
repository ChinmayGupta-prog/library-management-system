package com.codex.lms.service;

public record DashboardStats(
        long totalBooks,
        long availableBooks,
        long issuedBooks,
        long activeMembers,
        long activeLoans,
        long overdueLoans,
        long categories
) {
}
