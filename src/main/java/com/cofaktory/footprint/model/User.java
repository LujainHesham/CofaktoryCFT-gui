package com.cofaktory.footprint.model;

public class User {
    private int userId;
    private Integer branchId; // Nullable for non-BranchUser roles
    private String userName;
    private String userRole;
    private String userEmail;
    private String password;
    private String salt;
    private boolean forcePasswordChange;

    public User(int userId, Integer branchId, String userName, String userRole, String userEmail, String password, String salt) {
        this.userId = userId;
        this.branchId = branchId;
        this.userName = userName;
        this.userRole = userRole;
        this.userEmail = userEmail;
        this.password = password;
        this.salt = salt;
        this.forcePasswordChange = false;
    }

    public User(int userId, Integer branchId, String userName, String userRole, String userEmail, String password, String salt, boolean forcePasswordChange) {
        this.userId = userId;
        this.branchId = branchId;
        this.userName = userName;
        this.userRole = userRole;
        this.userEmail = userEmail;
        this.password = password;
        this.salt = salt;
        this.forcePasswordChange = forcePasswordChange;
    }

    public User() {}

    // Getters and Setters
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public Integer getBranchId() { return branchId; }
    public void setBranchId(Integer branchId) { this.branchId = branchId; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getUserRole() { return userRole; }
    public void setUserRole(String userRole) { this.userRole = userRole; }

    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getSalt() { return salt; }
    public void setSalt(String salt) { this.salt = salt; }

    public boolean isForcePasswordChange() { return forcePasswordChange; }
    public void setForcePasswordChange(boolean forcePasswordChange) { this.forcePasswordChange = forcePasswordChange; }
}