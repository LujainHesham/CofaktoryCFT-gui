package com.cofaktory.footprint.util;

/**
 * This is a simple static session manager to hold current login info
 * with improved session state management and debugging
 */
public class SessionManager {
    // Default values explicitly set for clarity
    private static int currentBranchId = 0;
    private static int currentUserId = 0;
    private static String currentBranchName = null;
    private static boolean isAuthenticated = false;

    public static int getCurrentBranchId() { return currentBranchId; }
    public static int getCurrentUserId() { return currentUserId; }
    public static String getCurrentBranchName() { return currentBranchName; }
    public static boolean isAuthenticated() { return isAuthenticated; }

    public static void setSession(int branchId, int userId, String branchName) {
        // Validate inputs
        if (branchId <= 0 || userId <= 0) {
            System.err.println("WARNING: Attempted to set session with invalid IDs: " +
                    "BranchID=" + branchId + ", UserID=" + userId);
            return;
        }

        currentBranchId = branchId;
        currentUserId = userId;
        currentBranchName = branchName;
        isAuthenticated = true;

        // Debug output to verify session was set
        logSessionState("After login");
    }

    public static void clearSession() {
        // Reset user identification
        currentUserId = 0;

        // Reset branch information
        currentBranchId = 0;
        currentBranchName = null;

        // Reset authentication state
        isAuthenticated = false;
    }

    // Helper method for debugging
    public static void logSessionState(String location) {
        System.out.println("=== SESSION STATE AT: " + location + " ===");
        System.out.println("User ID: " + getCurrentUserId());
        System.out.println("Branch ID: " + getCurrentBranchId());
        System.out.println("Branch Name: " + getCurrentBranchName());
        System.out.println("Is Authenticated: " + isAuthenticated());
        System.out.println("=====================================");
    }
}