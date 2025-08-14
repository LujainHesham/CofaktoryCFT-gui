-- ========== DATABASE CREATION ==========
-- CREATE DATABASE carbon_footprint_tracker;
-- 2. Create simple user (no host restriction)
-- CREATE USER 'coffee_user'@'%' IDENTIFIED BY '1234';

-- 3. Grant basic privileges
-- GRANT ALL ON carbon_footprint_tracker.* TO 'coffee_user'@'%';
USE carbon_footprint_tracker;

-- ========== 1. CORE TABLES ==========
CREATE TABLE City (
    CityID INT PRIMARY KEY AUTO_INCREMENT,
    CityName VARCHAR(50) UNIQUE NOT NULL
);

CREATE TABLE PlanStatus (
    StatusID INT PRIMARY KEY AUTO_INCREMENT,
    StatusName VARCHAR(50) UNIQUE NOT NULL
);

-- ========== 2. BRANCH/USER TABLES ==========
CREATE TABLE Branch (
    BranchID INT PRIMARY KEY AUTO_INCREMENT,
    CityID INT NOT NULL,
    Location VARCHAR(100) NOT NULL,
    NumberOfEmployees INT NOT NULL CHECK (NumberOfEmployees > 0),
    FOREIGN KEY (CityID) REFERENCES City(CityID) ON DELETE RESTRICT
);

CREATE TABLE User (
    UserID INT PRIMARY KEY AUTO_INCREMENT,
    BranchID INT NULL,
    UserName VARCHAR(100) NOT NULL,
    UserRole ENUM('BranchUser', 'OPManager', 'CIO', 'CEO') NOT NULL,
    UserEmail VARCHAR(100) UNIQUE NOT NULL,
    Password VARCHAR(255) NOT NULL,
    Salt VARCHAR(255) NOT NULL,
    ForcePasswordChange BOOLEAN DEFAULT FALSE NOT NULL,
    FOREIGN KEY (BranchID) REFERENCES Branch(BranchID) ON DELETE SET NULL
);

-- ========== 3. AUDIT/NOTIFICATION TABLES ==========
CREATE TABLE AuditLogging (
    LogID INT PRIMARY KEY AUTO_INCREMENT,
    UserID INT NOT NULL,
    Action VARCHAR(20) NOT NULL,
    TableName VARCHAR(50) NOT NULL,
    RecordID INT NULL,
    Timestamp DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (UserID) REFERENCES User(UserID) ON DELETE CASCADE
);

CREATE TABLE Notification (
    NotificationID INT PRIMARY KEY AUTO_INCREMENT,
    UserID INT NOT NULL,
    Message TEXT NOT NULL,
    Timestamp DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    IsRead BOOLEAN NOT NULL DEFAULT FALSE,
    FOREIGN KEY (UserID) REFERENCES User(UserID) ON DELETE CASCADE
);

-- ========== 4. BRANCH-SPECIFIC TABLES ==========
CREATE TABLE CoffeeDistribution (
    DistributionID INT PRIMARY KEY AUTO_INCREMENT,
    BranchID INT NOT NULL,
    UserID INT NULL,
    VehicleType ENUM('Minivan', 'Pickup Truck') NOT NULL,
    NumberOfVehicles INT NOT NULL CHECK (NumberOfVehicles > 0),
    DistancePerVehicle_KM DECIMAL(10,2) NOT NULL CHECK (DistancePerVehicle_KM > 0),
    TotalDistance_KM DECIMAL(10,2) AS (NumberOfVehicles * DistancePerVehicle_KM) STORED,
    FuelEfficiency DECIMAL(10,2) AS (
        CASE 
            WHEN VehicleType = 'Minivan' THEN 10.00
            WHEN VehicleType = 'Pickup Truck' THEN 15.00
            ELSE NULL
        END
    ) STORED,
    V_CarbonEmissions_Kg DECIMAL(10,2) AS (
        ROUND((TotalDistance_KM / FuelEfficiency) * 2.68, 2)
    ) STORED,
    ActivityDate DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (BranchID) REFERENCES Branch(BranchID) ON DELETE CASCADE,
    FOREIGN KEY (UserID) REFERENCES User(UserID) ON DELETE SET NULL
);

CREATE TABLE CoffeePackaging (
    PackagingID INT PRIMARY KEY AUTO_INCREMENT,
    BranchID INT NOT NULL,
    UserID INT NULL,
    PackagingWaste_KG DECIMAL(10,2) NOT NULL CHECK (PackagingWaste_KG >= 0),
    Pa_CarbonEmissions_KG DECIMAL(10,2) AS (
        ROUND(PackagingWaste_KG * 6, 2)
    ) STORED,
    ActivityDate DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (BranchID) REFERENCES Branch(BranchID) ON DELETE CASCADE,
    FOREIGN KEY (UserID) REFERENCES User(UserID) ON DELETE SET NULL
);

CREATE TABLE CoffeeProduction (
    ProductionID INT PRIMARY KEY AUTO_INCREMENT,
    BranchID INT NOT NULL,
    UserID INT NULL,
    Supplier VARCHAR(100) NOT NULL,
    CoffeeType ENUM('Arabica Beans', 'Robusta Beans', 'Organic Beans') NOT NULL,
    ProductType ENUM('Ground', 'Whole Bean', 'Instant') NOT NULL,
    ProductionQuantitiesOfCoffee_KG DECIMAL(10,2) NOT NULL CHECK (ProductionQuantitiesOfCoffee_KG > 0),
    Pr_CarbonEmissions_KG DECIMAL(10,2) AS (
        ROUND(ProductionQuantitiesOfCoffee_KG * 6.4, 2)
    ) STORED,
    ActivityDate DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (BranchID) REFERENCES Branch(BranchID) ON DELETE CASCADE,
    FOREIGN KEY (UserID) REFERENCES User(UserID) ON DELETE SET NULL
);

CREATE TABLE ReductionStrategy (
    ReductionID INT PRIMARY KEY AUTO_INCREMENT,
    BranchID INT NOT NULL,
    UserID INT NULL,
    ReductionStrategy TEXT NOT NULL,
    StatusID INT NOT NULL,
    ImplementationCosts DECIMAL(12,2) NOT NULL CHECK (ImplementationCosts >= 0),
    ProjectedAnnualProfits DECIMAL(12,2) AS (
        ROUND(ImplementationCosts * 0.2, 2)
    ) STORED,
    ActivityDate DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (BranchID) REFERENCES Branch(BranchID) ON DELETE CASCADE,
    FOREIGN KEY (UserID) REFERENCES User(UserID) ON DELETE SET NULL,
    FOREIGN KEY (StatusID) REFERENCES PlanStatus(StatusID)
);

-- ========== 5. INDEXES ==========
-- Removed UQ_BranchUser to avoid duplicate issues, use if you want exactly 1 user per role per branch
-- CREATE UNIQUE INDEX UQ_BranchUser ON User(BranchID, UserRole); 

CREATE INDEX IDX_CoffeeProduction_Branch ON CoffeeProduction(BranchID);
CREATE INDEX IDX_CoffeePackaging_Branch ON CoffeePackaging(BranchID);
CREATE INDEX IDX_CoffeeDistribution_Branch ON CoffeeDistribution(BranchID);
CREATE INDEX IDX_ReductionStrategy_Branch ON ReductionStrategy(BranchID);
CREATE INDEX IDX_AuditLogging_User_Time ON AuditLogging(UserID, Timestamp);
CREATE INDEX IDX_Notification_User_Time ON Notification(UserID, Timestamp);
CREATE INDEX IDX_ReductionStrategy_Status ON ReductionStrategy(StatusID);
CREATE INDEX IDX_Branch_Reports ON CoffeeProduction(BranchID, ProductionQuantitiesOfCoffee_KG);
CREATE INDEX IDX_User_Email ON User(UserEmail);
CREATE INDEX IDX_Branch_City ON Branch(CityID);


-- Insert Cities
INSERT INTO City (CityName) VALUES
('Alex'),
('Aswan'),
('Cairo'),
('Suez');

-- Insert Plan Status
INSERT INTO PlanStatus (StatusName) VALUES
('Draft'),
('InProgress'),
('Complete');

-- Insert Branches
INSERT INTO Branch (CityID, Location, NumberOfEmployees) VALUES
-- Cairo branches
(3, 'Maadi', 35),
(3, 'Nasr City', 38),
(3, 'New Cairo', 40),
(3, 'Badr City', 32),

-- Aswan branches
(2, 'New Aswan', 24),
(2, 'Edfu', 20),
(2, 'Tushka', 20),
(2, 'Nag el Kurkur', 22),

-- Alex branches
(1, 'El Laban', 28),
(1, 'Borg El Arab', 25),
(1, 'Al Amreya', 29),
(1, 'El Agamy', 27),

-- Suez branches
(4, 'Al Arbaeen', 15),
(4, 'Al Adabeya', 18),
(4, 'Faisal District', 18),
(4, 'El Ganayen', 20);

-- Insert Users with Salt
INSERT INTO User (
    BranchID, UserName, UserRole, UserEmail, Password, Salt, ForcePasswordChange
) VALUES
(1, 'Amr Azzam', 'BranchUser', 'amr.azzam@gmail.com', 'AZZ89754', 'salt1', FALSE),
(2, 'Mehad Mohamed', 'BranchUser', 'mehad.mohamed@gmail.com', 'ADD-5648', 'salt2', FALSE),
(3, 'Ahmed Maher', 'BranchUser', 'ahmed.maher@gmail.com', 'AH.M1236', 'salt3', FALSE),
(4, 'Shawkat Eldin', 'BranchUser', 'shawkat.eldin@gmail.com', 'DIN/89745', 'salt4', FALSE),
(5, 'Lujain Hesham', 'BranchUser', 'lujain.hesham@gmail.com', 'LOJ*9874', 'salt5', FALSE),
(6, 'Nagi Eid', 'BranchUser', 'nagi.eid@gmail.com', 'NAG/8974', 'salt6', FALSE),
(7, 'Baraa Ahmed', 'BranchUser', 'baraa.ahmed@gmail.com', 'BAR54789', 'salt7', FALSE),
(8, 'Abdallah Amr', 'BranchUser', 'abdallah.amr@gmail.com', 'ABDA**987', 'salt8', FALSE),
(9, 'Essam Elghandour', 'BranchUser', 'essam.elghandour@gmail.com', 'EGH**987', 'salt9', FALSE),
(10, 'Yehia Homaimy', 'BranchUser', 'yehia.homaimy@gmail.com', 'EGH7-098', 'salt10', FALSE),
(11, 'Mohab Elkandil', 'BranchUser', 'mohab.elkandil@gmail.com', 'moh-0987', 'salt11', FALSE),
(12, 'Nussaiba Khaled', 'BranchUser', 'nussaiba.khaled@gmail.com', 'NKH/8974', 'salt12', FALSE),
(13, 'Farouk Mahmoud', 'BranchUser', 'farouk.mahmoud@gmail.com', 'FKM-0987', 'salt13', FALSE),
(14, 'Karen Adel', 'BranchUser', 'karen.adel@gmail.com', 'krn**879', 'salt14', FALSE),
(15, 'Sarah Ahmed', 'BranchUser', 'sarah.ahmed@gmail.com', 'SAR//897', 'salt15', FALSE),
(16, 'Nada Ibrahim', 'BranchUser', 'nada.ibrahim@gmail.com', 'NAD-1234', 'salt16', FALSE),
(NULL, 'Ahmed Samir', 'OPManager', 'ahmed.samir@gmail.com', 'OPM*8974', 'salt17', FALSE),
(NULL, 'Amr Daba', 'CIO', 'amr.daba@gmail.com', 'CIO**789', 'salt18', FALSE),
(NULL, 'Osama Hanafy', 'CEO', 'osama.hanafy@gmail.com', 'CEO-1598', 'salt19', FALSE);



-- Insert Coffee Distribution
INSERT INTO CoffeeDistribution (BranchID, UserID, VehicleType, NumberOfVehicles, DistancePerVehicle_KM) VALUES
-- Cairo
(1, 1, 'Minivan', 5, 150.0),   
(2, 2, 'Pickup Truck', 4, 120.0),  
(3, 3, 'Minivan', 6, 130.0),    
(4, 4, 'Pickup Truck', 5, 140.0), 

-- Aswan
(5, 5, 'Pickup Truck', 3, 180.0), 
(6, 6, 'Minivan', 4, 160.0),      
(7, 7, 'Pickup Truck', 2, 200.0), 
(8, 8, 'Minivan', 3, 170.0),     

-- Alex
(9, 9, 'Minivan', 4, 100.0),     
(10, 10, 'Pickup Truck', 3, 110.0), 
(11, 11, 'Minivan', 3, 90.0),     
(12, 12, 'Pickup Truck', 2, 120.0), 

-- Suez
(13, 13, 'Pickup Truck', 2, 80.0),  
(14, 14, 'Minivan', 3, 70.0),      
(15, 15, 'Pickup Truck', 1, 90.0),  
(16, 16, 'Minivan', 2, 60.0);

-- Insert Coffee Production
INSERT INTO CoffeeProduction (BranchID, UserID, Supplier, CoffeeType, ProductType, ProductionQuantitiesOfCoffee_KG) VALUES
-- Cairo
(1, 1, 'Egyptian Coffee Co.', 'Arabica Beans', 'Ground', 120.0),
(2, 2, 'Cairo Roasters', 'Robusta Beans', 'Whole Bean', 110.5),
(3, 3, 'Nile Valley Beans', 'Arabica Beans', 'Instant', 150.2),
(4, 4, 'Delta Coffee', 'Organic Beans', 'Ground', 130.7),

-- Aswan
(5, 5, 'Aswan Farmers Co-op', 'Arabica Beans', 'Whole Bean', 95.0),
(6, 6, 'Upper Egypt Roasters', 'Robusta Beans', 'Ground', 88.4),
(7, 7, 'Nubian Gold', 'Organic Beans', 'Instant', 92.1),
(8, 8, 'Red Sea Coffee', 'Arabica Beans', 'Whole Bean', 90.8),

-- Alex
(9, 9, 'Mediterranean Beans', 'Arabica Beans', 'Ground', 75.2),
(10, 10, 'Alexandria Roasters', 'Robusta Beans', 'Instant', 80.5),
(11, 11, 'Port City Coffee', 'Organic Beans', 'Whole Bean', 78.7),
(12, 12, 'Western Delta Co.', 'Arabica Beans', 'Ground', 82.3),

-- Suez
(13, 13, 'Suez Canal Coffee', 'Robusta Beans', 'Whole Bean', 52.4),
(14, 14, 'Gulf Roasters', 'Arabica Beans', 'Instant', 48.6),
(15, 15, 'East Wind Beans', 'Organic Beans', 'Ground', 50.2),
(16, 16, 'Sinai Coffee Co.', 'Arabica Beans', 'Whole Bean', 45.8);

-- Insert Reduction Strategies
INSERT INTO ReductionStrategy (BranchID, UserID, ReductionStrategy, StatusID, ImplementationCosts) VALUES
-- Cairo
(1, 1, 'Implement electric delivery vehicles and solar-powered packaging equipment', 3, 250000.00),
(2, 2, 'Install waste-to-energy system for coffee grounds and biodegradable packaging', 2, 180000.00),
(3, 3, 'Complete facility retrofit with energy-efficient roasting and LED lighting', 3, 300000.00),
(4, 4, 'Advanced water recycling system and carbon capture for roasting process', 1, 220000.00),

-- Aswan
(5, 5, 'Switch to hybrid delivery vehicles and compostable packaging', 2, 120000.00),
(6, 6, 'Install solar panels for 50% of energy needs and improved insulation', 3, 95000.00),
(7, 7, 'Implement coffee chaff recycling program and efficient roasting', 2, 85000.00),
(8, 8, 'Rainwater harvesting system and local biomass packaging', 1, 75000.00),

-- Alex
(9, 9, 'Upgrade to energy-efficient roasting equipment', 3, 80000.00),
(10, 10, 'Switch to reusable transport containers and LED lighting', 2, 65000.00),
(11, 11, 'Implement waste segregation and recycling program', 1, 50000.00),
(12, 12, 'Install variable-speed drives on all motors', 2, 45000.00),

-- Suez
(13, 13, 'Basic packaging optimization and staff training', 3, 30000.00),
(14, 14, 'Implement proper maintenance schedule for equipment', 2, 25000.00),
(15, 15, 'Switch to local suppliers to reduce transport emissions', 1, 35000.00),
(16, 16, 'Basic energy monitoring system installation', 2, 20000.00);

-- Insert Audit Logs with Correct Columns
INSERT INTO AuditLogging (UserID, Action, TableName, RecordID, Timestamp) VALUES
(1, 'CREATE', 'ReductionStrategy', 1, '2025-05-01 08:45:00'),
(2, 'UPDATE', 'CoffeeDistribution', 2, '2025-05-02 10:12:00'),
(3, 'VIEW', 'EmissionsReport', NULL, '2025-05-03 14:30:00'), -- Assuming RecordID can be NULL if not applicable
(4, 'INSERT', 'CoffeeProduction', 4, '2025-05-04 09:50:00'),
(5, 'LOGIN', 'User', 5, '2025-05-05 07:00:00'),
(6, 'LOGOUT', 'User', 6, '2025-05-05 17:00:00'),
(7, 'UPDATE', 'User', 7, '2025-05-06 11:25:00'),
(8, 'UPDATE', 'ReductionStrategy', 8, '2025-05-07 15:10:00'),
(9, 'APPROVE', 'Report', 9, '2025-05-08 16:30:00'),
(10, 'UPDATE', 'User', 10, '2025-05-09 12:45:00');

-- Insert Notifications (after confirming UserIDs 1-19 exist)
INSERT INTO Notification (UserID, Message, Timestamp, IsRead) VALUES
(1, 'Your Reduction Strategy has been approved.', '2025-05-01 09:00:00', 0),
(2, 'Reminder: Update coffee delivery distances.', '2025-05-02 08:00:00', 0),
(3, 'New audit log entry recorded.', '2025-05-03 15:00:00', 1),
(4, 'Coffee production report is due tomorrow.', '2025-05-04 11:15:00', 0),
(5, 'System maintenance scheduled for 2025-05-10.', '2025-05-05 14:30:00', 1),
(6, 'Password changed successfully.', '2025-05-06 11:30:00', 1),
(7, 'Coffee distribution updated successfully.', '2025-05-07 10:20:00', 0),
(8, 'Reminder: Submit your audit report.', '2025-05-08 13:45:00', 0),
(9, 'Plan status changed to "Complete".', '2025-05-09 16:10:00', 1),
(10, 'Welcome to the Carbon Tracker System!', '2025-05-10 09:00:00', 1);

