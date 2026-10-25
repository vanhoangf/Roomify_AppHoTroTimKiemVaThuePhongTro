-- ============================================================================
-- ROOMIFY - FINAL DATABASE SCHEMA (TWO-TIER LOCATION + STRONGER CONSISTENCY)
-- De tai: Ung dung ho tro tim kiem va thue phong tro danh cho sinh vien
-- DBMS: Microsoft SQL Server 2019+
-- Backend: ASP.NET Core Web API + Entity Framework Core + JWT
-- Mobile: Kotlin Android (STUDENT / LANDLORD)
-- Admin: Web rieng (ADMIN)
--
-- Luu y:
--   @RecreateDatabase = 1 se XOA database RoomifyDB cu va tao lai tu dau.
--   Dung cho giai doan phat trien/do an. Neu database da co du lieu that,
--   hay chuyen sang EF Core Migration hoac script ALTER thay vi DROP DATABASE.
-- ============================================================================

USE master;
GO

DECLARE @RecreateDatabase BIT = 1;

IF @RecreateDatabase = 1 AND DB_ID(N'RoomifyDB') IS NOT NULL
BEGIN
    ALTER DATABASE RoomifyDB SET SINGLE_USER WITH ROLLBACK IMMEDIATE;
    DROP DATABASE RoomifyDB;
END;
GO

IF DB_ID(N'RoomifyDB') IS NULL
BEGIN
    CREATE DATABASE RoomifyDB;
END;
GO

USE RoomifyDB;
GO

/* ============================================================================
   1. AUTHENTICATION & USERS
   ============================================================================ */

CREATE TABLE Roles (
    RoleID INT IDENTITY(1,1) PRIMARY KEY,
    RoleCode VARCHAR(30) NOT NULL,
    RoleName NVARCHAR(100) NOT NULL,

    CONSTRAINT UQ_Roles_RoleCode UNIQUE (RoleCode)
);
GO

CREATE TABLE Users (
    UserID BIGINT IDENTITY(1,1) PRIMARY KEY,
    RoleID INT NOT NULL,
    Email VARCHAR(255) NOT NULL,
    PasswordHash VARCHAR(500) NOT NULL,
    FullName NVARCHAR(150) NOT NULL,
    Phone VARCHAR(20) NULL,
    AvatarURL NVARCHAR(1000) NULL,

    Status VARCHAR(20) NOT NULL CONSTRAINT DF_Users_Status DEFAULT 'ACTIVE',
    EmailVerified BIT NOT NULL CONSTRAINT DF_Users_EmailVerified DEFAULT 0,

    -- Thong tin khoa tai khoan de Admin quan ly.
    LockedReason NVARCHAR(500) NULL,
    LockedAt DATETIME2 NULL,
    LockedByUserID BIGINT NULL,

    LastLoginAt DATETIME2 NULL,
    CreatedAt DATETIME2 NOT NULL CONSTRAINT DF_Users_CreatedAt DEFAULT SYSUTCDATETIME(),
    UpdatedAt DATETIME2 NOT NULL CONSTRAINT DF_Users_UpdatedAt DEFAULT SYSUTCDATETIME(),

    CONSTRAINT FK_Users_Roles FOREIGN KEY (RoleID)
        REFERENCES Roles(RoleID),
    CONSTRAINT FK_Users_LockedBy FOREIGN KEY (LockedByUserID)
        REFERENCES Users(UserID),

    CONSTRAINT UQ_Users_Email UNIQUE (Email),
    CONSTRAINT CK_Users_Status CHECK (Status IN ('ACTIVE','LOCKED'))
);
GO

-- Cho phep nhieu User co Phone = NULL, nhung Phone co gia tri thi phai duy nhat.
CREATE UNIQUE INDEX UX_Users_Phone_NotNull
ON Users(Phone)
WHERE Phone IS NOT NULL;
GO

CREATE INDEX IX_Users_RoleID ON Users(RoleID);
CREATE INDEX IX_Users_Status ON Users(Status);
CREATE INDEX IX_Users_CreatedAt ON Users(CreatedAt DESC);
GO

CREATE TABLE RefreshTokens (
    RefreshTokenID BIGINT IDENTITY(1,1) PRIMARY KEY,
    UserID BIGINT NOT NULL,
    TokenHash VARCHAR(500) NOT NULL,
    ExpiresAt DATETIME2 NOT NULL,
    RevokedAt DATETIME2 NULL,
    CreatedAt DATETIME2 NOT NULL CONSTRAINT DF_RefreshTokens_CreatedAt DEFAULT SYSUTCDATETIME(),

    CONSTRAINT FK_RefreshTokens_Users FOREIGN KEY (UserID)
        REFERENCES Users(UserID) ON DELETE CASCADE,
    CONSTRAINT UQ_RefreshTokens_TokenHash UNIQUE (TokenHash)
);
GO

CREATE INDEX IX_RefreshTokens_UserID ON RefreshTokens(UserID);
CREATE INDEX IX_RefreshTokens_ExpiresAt ON RefreshTokens(ExpiresAt);
GO

CREATE TABLE PasswordResetTokens (
    PasswordResetTokenID BIGINT IDENTITY(1,1) PRIMARY KEY,
    UserID BIGINT NOT NULL,
    TokenHash VARCHAR(500) NOT NULL,
    ExpiresAt DATETIME2 NOT NULL,
    UsedAt DATETIME2 NULL,
    CreatedAt DATETIME2 NOT NULL CONSTRAINT DF_PasswordResetTokens_CreatedAt DEFAULT SYSUTCDATETIME(),

    CONSTRAINT FK_PasswordResetTokens_Users FOREIGN KEY (UserID)
        REFERENCES Users(UserID) ON DELETE CASCADE,
    CONSTRAINT UQ_PasswordResetTokens_TokenHash UNIQUE (TokenHash)
);
GO

CREATE INDEX IX_PasswordResetTokens_UserID ON PasswordResetTokens(UserID);
CREATE INDEX IX_PasswordResetTokens_ExpiresAt ON PasswordResetTokens(ExpiresAt);
GO

/* ============================================================================
   2. LOCATION & MASTER DATA
   ============================================================================ */

-- Viet Nam su dung mo hinh chinh quyen dia phuong 2 cap tu 01/07/2025:
--   Cap tinh/thanh pho truc thuoc trung uong -> cap xa/phuong/dac khu.
-- Khong con luu cap huyen trong du lieu dia chi cua Roomify.
-- Google Maps van su dung AddressLine + Latitude + Longitude de dinh vi chinh xac.
CREATE TABLE Provinces (
    ProvinceID INT IDENTITY(1,1) PRIMARY KEY,
    ProvinceCode VARCHAR(20) NULL,
    ProvinceName NVARCHAR(150) NOT NULL,
    ProvinceType VARCHAR(20) NOT NULL CONSTRAINT DF_Provinces_Type DEFAULT 'PROVINCE',
    IsActive BIT NOT NULL CONSTRAINT DF_Provinces_IsActive DEFAULT 1,

    CONSTRAINT UQ_Provinces_Name UNIQUE (ProvinceName),
    CONSTRAINT CK_Provinces_Type CHECK (ProvinceType IN ('PROVINCE','MUNICIPALITY'))
);
GO

CREATE UNIQUE INDEX UX_Provinces_Code_NotNull
ON Provinces(ProvinceCode)
WHERE ProvinceCode IS NOT NULL;
GO

-- Bang Wards dai dien chung cho don vi hanh chinh cap xa: xa, phuong, dac khu.
CREATE TABLE Wards (
    WardID INT IDENTITY(1,1) PRIMARY KEY,
    ProvinceID INT NOT NULL,
    WardCode VARCHAR(20) NULL,
    WardName NVARCHAR(150) NOT NULL,
    WardType VARCHAR(20) NOT NULL CONSTRAINT DF_Wards_Type DEFAULT 'WARD',
    IsActive BIT NOT NULL CONSTRAINT DF_Wards_IsActive DEFAULT 1,

    CONSTRAINT FK_Wards_Provinces FOREIGN KEY (ProvinceID)
        REFERENCES Provinces(ProvinceID),
    CONSTRAINT UQ_Wards_Province_Name UNIQUE (ProvinceID, WardName),
    -- Can composite unique key de cac bang dia chi tham chieu ca ProvinceID + WardID,
    -- tu do ngan Ward cua tinh A bi gan nham voi ProvinceID cua tinh B.
    CONSTRAINT UQ_Wards_Province_WardID UNIQUE (ProvinceID, WardID),
    CONSTRAINT CK_Wards_Type CHECK (WardType IN ('WARD','COMMUNE','SPECIAL_ZONE'))
);
GO

CREATE UNIQUE INDEX UX_Wards_Code_NotNull
ON Wards(WardCode)
WHERE WardCode IS NOT NULL;

CREATE INDEX IX_Wards_ProvinceID ON Wards(ProvinceID, WardName);
GO

CREATE TABLE Universities (
    UniversityID BIGINT IDENTITY(1,1) PRIMARY KEY,
    UniversityName NVARCHAR(255) NOT NULL,
    ProvinceID INT NULL,
    WardID INT NULL,
    AddressLine NVARCHAR(500) NULL,
    Latitude DECIMAL(10,7) NULL,
    Longitude DECIMAL(10,7) NULL,
    IsActive BIT NOT NULL CONSTRAINT DF_Universities_IsActive DEFAULT 1,

    CONSTRAINT FK_Universities_Provinces FOREIGN KEY (ProvinceID)
        REFERENCES Provinces(ProvinceID),
    CONSTRAINT FK_Universities_ProvinceWard FOREIGN KEY (ProvinceID, WardID)
        REFERENCES Wards(ProvinceID, WardID),

    CONSTRAINT CK_Universities_WardRequiresProvince CHECK (WardID IS NULL OR ProvinceID IS NOT NULL),
    CONSTRAINT CK_Universities_Latitude CHECK (Latitude IS NULL OR Latitude BETWEEN -90 AND 90),
    CONSTRAINT CK_Universities_Longitude CHECK (Longitude IS NULL OR Longitude BETWEEN -180 AND 180)
);
GO

CREATE INDEX IX_Universities_Name ON Universities(UniversityName);
CREATE INDEX IX_Universities_Location ON Universities(ProvinceID, WardID);
GO

CREATE TABLE PropertyTypes (
    PropertyTypeID INT IDENTITY(1,1) PRIMARY KEY,
    TypeName NVARCHAR(100) NOT NULL,
    IsActive BIT NOT NULL CONSTRAINT DF_PropertyTypes_IsActive DEFAULT 1,

    CONSTRAINT UQ_PropertyTypes_TypeName UNIQUE (TypeName)
);
GO

CREATE TABLE Amenities (
    AmenityID INT IDENTITY(1,1) PRIMARY KEY,
    AmenityName NVARCHAR(100) NOT NULL,
    IconURL NVARCHAR(500) NULL,
    IsActive BIT NOT NULL CONSTRAINT DF_Amenities_IsActive DEFAULT 1,

    CONSTRAINT UQ_Amenities_AmenityName UNIQUE (AmenityName)
);
GO

/* ============================================================================
   3. STUDENT & LANDLORD PROFILES
   ============================================================================ */

CREATE TABLE StudentProfiles (
    StudentID BIGINT IDENTITY(1,1) PRIMARY KEY,
    UserID BIGINT NOT NULL,
    UniversityID BIGINT NULL,
    StudentCode VARCHAR(50) NULL,
    StudentCardURL NVARCHAR(1000) NULL,
    IsVerified BIT NOT NULL CONSTRAINT DF_StudentProfiles_IsVerified DEFAULT 0,
    VerifiedByUserID BIGINT NULL,
    VerifiedAt DATETIME2 NULL,
    CreatedAt DATETIME2 NOT NULL CONSTRAINT DF_StudentProfiles_CreatedAt DEFAULT SYSUTCDATETIME(),
    UpdatedAt DATETIME2 NOT NULL CONSTRAINT DF_StudentProfiles_UpdatedAt DEFAULT SYSUTCDATETIME(),

    CONSTRAINT FK_StudentProfiles_Users FOREIGN KEY (UserID)
        REFERENCES Users(UserID) ON DELETE CASCADE,
    CONSTRAINT FK_StudentProfiles_Universities FOREIGN KEY (UniversityID)
        REFERENCES Universities(UniversityID),
    CONSTRAINT FK_StudentProfiles_VerifiedBy FOREIGN KEY (VerifiedByUserID)
        REFERENCES Users(UserID),

    CONSTRAINT UQ_StudentProfiles_UserID UNIQUE (UserID)
);
GO

-- Ma sinh vien chi can duy nhat trong cung mot truong khi ca hai gia tri deu co.
CREATE UNIQUE INDEX UX_StudentProfiles_University_StudentCode
ON StudentProfiles(UniversityID, StudentCode)
WHERE UniversityID IS NOT NULL AND StudentCode IS NOT NULL;
GO

CREATE INDEX IX_StudentProfiles_UniversityID ON StudentProfiles(UniversityID);
GO

CREATE TABLE LandlordProfiles (
    LandlordID BIGINT IDENTITY(1,1) PRIMARY KEY,
    UserID BIGINT NOT NULL,
    IdentityNumber VARCHAR(30) NULL,
    IdentityDocumentURL NVARCHAR(1000) NULL,
    IsVerified BIT NOT NULL CONSTRAINT DF_LandlordProfiles_IsVerified DEFAULT 0,
    VerifiedByUserID BIGINT NULL,
    VerifiedAt DATETIME2 NULL,
    CreatedAt DATETIME2 NOT NULL CONSTRAINT DF_LandlordProfiles_CreatedAt DEFAULT SYSUTCDATETIME(),
    UpdatedAt DATETIME2 NOT NULL CONSTRAINT DF_LandlordProfiles_UpdatedAt DEFAULT SYSUTCDATETIME(),

    CONSTRAINT FK_LandlordProfiles_Users FOREIGN KEY (UserID)
        REFERENCES Users(UserID) ON DELETE CASCADE,
    CONSTRAINT FK_LandlordProfiles_VerifiedBy FOREIGN KEY (VerifiedByUserID)
        REFERENCES Users(UserID),

    CONSTRAINT UQ_LandlordProfiles_UserID UNIQUE (UserID)
);
GO

CREATE UNIQUE INDEX UX_LandlordProfiles_IdentityNumber_NotNull
ON LandlordProfiles(IdentityNumber)
WHERE IdentityNumber IS NOT NULL;
GO

/* ============================================================================
   4. PROPERTIES, ROOMS & LISTINGS
   ============================================================================ */

CREATE TABLE Properties (
    PropertyID BIGINT IDENTITY(1,1) PRIMARY KEY,
    LandlordID BIGINT NOT NULL,
    PropertyTypeID INT NOT NULL,
    PropertyName NVARCHAR(255) NOT NULL,
    Description NVARCHAR(2000) NULL,

    -- Dia chi 2 cap: tinh/thanh pho -> xa/phuong/dac khu.
    ProvinceID INT NOT NULL,
    WardID INT NOT NULL,
    AddressLine NVARCHAR(500) NOT NULL,
    Latitude DECIMAL(10,7) NULL,
    Longitude DECIMAL(10,7) NULL,

    Status VARCHAR(20) NOT NULL CONSTRAINT DF_Properties_Status DEFAULT 'ACTIVE',
    CreatedAt DATETIME2 NOT NULL CONSTRAINT DF_Properties_CreatedAt DEFAULT SYSUTCDATETIME(),
    UpdatedAt DATETIME2 NOT NULL CONSTRAINT DF_Properties_UpdatedAt DEFAULT SYSUTCDATETIME(),

    CONSTRAINT FK_Properties_Landlords FOREIGN KEY (LandlordID)
        REFERENCES LandlordProfiles(LandlordID),
    CONSTRAINT FK_Properties_PropertyTypes FOREIGN KEY (PropertyTypeID)
        REFERENCES PropertyTypes(PropertyTypeID),
    CONSTRAINT FK_Properties_Provinces FOREIGN KEY (ProvinceID)
        REFERENCES Provinces(ProvinceID),
    -- Composite FK bao dam WardID thuc su thuoc ProvinceID dang luu.
    CONSTRAINT FK_Properties_ProvinceWard FOREIGN KEY (ProvinceID, WardID)
        REFERENCES Wards(ProvinceID, WardID),

    CONSTRAINT CK_Properties_Status CHECK (Status IN ('ACTIVE','INACTIVE')),
    CONSTRAINT CK_Properties_Latitude CHECK (Latitude IS NULL OR Latitude BETWEEN -90 AND 90),
    CONSTRAINT CK_Properties_Longitude CHECK (Longitude IS NULL OR Longitude BETWEEN -180 AND 180)
);
GO

CREATE INDEX IX_Properties_LandlordID ON Properties(LandlordID);
CREATE INDEX IX_Properties_Location ON Properties(ProvinceID, WardID);
CREATE INDEX IX_Properties_PropertyTypeID ON Properties(PropertyTypeID);
CREATE INDEX IX_Properties_Status ON Properties(Status);
GO

-- 1 Room = 1 tin dang. Khong tach Listing de giu mo hinh don gian cho do an.
CREATE TABLE Rooms (
    RoomID BIGINT IDENTITY(1,1) PRIMARY KEY,
    PropertyID BIGINT NOT NULL,
    RoomNumber NVARCHAR(50) NULL,
    Title NVARCHAR(255) NOT NULL,
    Description NVARCHAR(MAX) NULL,

    AreaM2 DECIMAL(8,2) NOT NULL,
    RentPrice DECIMAL(18,2) NOT NULL,
    DepositAmount DECIMAL(18,2) NULL,
    ElectricityPrice DECIMAL(18,2) NULL,
    WaterPrice DECIMAL(18,2) NULL,
    InternetPrice DECIMAL(18,2) NULL,
    ServiceFee DECIMAL(18,2) NULL,
    ParkingFee DECIMAL(18,2) NULL,

    MaxOccupants INT NOT NULL CONSTRAINT DF_Rooms_MaxOccupants DEFAULT 1,
    GenderPreference VARCHAR(20) NOT NULL CONSTRAINT DF_Rooms_GenderPreference DEFAULT 'ALL',
    AvailableFrom DATE NULL,

    -- Trang thai bai dang: DRAFT -> PENDING -> PUBLISHED / REJECTED.
    ListingStatus VARCHAR(30) NOT NULL CONSTRAINT DF_Rooms_ListingStatus DEFAULT 'DRAFT',

    -- Trang thai kha dung cua phong, tach rieng voi trang thai bai dang.
    AvailabilityStatus VARCHAR(20) NOT NULL CONSTRAINT DF_Rooms_AvailabilityStatus DEFAULT 'AVAILABLE',

    -- ViewCount la tong luot xem. RoomViewHistories ben duoi luu lich su cua tung Student.
    ViewCount INT NOT NULL CONSTRAINT DF_Rooms_ViewCount DEFAULT 0,

    ReviewedByUserID BIGINT NULL,
    ReviewedAt DATETIME2 NULL,
    RejectionReason NVARCHAR(1000) NULL,
    PublishedAt DATETIME2 NULL,

    CreatedAt DATETIME2 NOT NULL CONSTRAINT DF_Rooms_CreatedAt DEFAULT SYSUTCDATETIME(),
    UpdatedAt DATETIME2 NOT NULL CONSTRAINT DF_Rooms_UpdatedAt DEFAULT SYSUTCDATETIME(),

    CONSTRAINT FK_Rooms_Properties FOREIGN KEY (PropertyID)
        REFERENCES Properties(PropertyID),
    CONSTRAINT FK_Rooms_ReviewedByUser FOREIGN KEY (ReviewedByUserID)
        REFERENCES Users(UserID),

    CONSTRAINT CK_Rooms_Area CHECK (AreaM2 > 0),
    CONSTRAINT CK_Rooms_RentPrice CHECK (RentPrice >= 0),
    CONSTRAINT CK_Rooms_Deposit CHECK (DepositAmount IS NULL OR DepositAmount >= 0),
    CONSTRAINT CK_Rooms_ElectricityPrice CHECK (ElectricityPrice IS NULL OR ElectricityPrice >= 0),
    CONSTRAINT CK_Rooms_WaterPrice CHECK (WaterPrice IS NULL OR WaterPrice >= 0),
    CONSTRAINT CK_Rooms_InternetPrice CHECK (InternetPrice IS NULL OR InternetPrice >= 0),
    CONSTRAINT CK_Rooms_ServiceFee CHECK (ServiceFee IS NULL OR ServiceFee >= 0),
    CONSTRAINT CK_Rooms_ParkingFee CHECK (ParkingFee IS NULL OR ParkingFee >= 0),
    CONSTRAINT CK_Rooms_MaxOccupants CHECK (MaxOccupants > 0),
    CONSTRAINT CK_Rooms_GenderPreference CHECK (GenderPreference IN ('ALL','MALE','FEMALE')),
    CONSTRAINT CK_Rooms_ListingStatus CHECK (ListingStatus IN ('DRAFT','PENDING','PUBLISHED','REJECTED','HIDDEN')),
    CONSTRAINT CK_Rooms_AvailabilityStatus CHECK (AvailabilityStatus IN ('AVAILABLE','RESERVED','RENTED')),
    CONSTRAINT CK_Rooms_ViewCount CHECK (ViewCount >= 0)
);
GO

-- Neu chu tro co nhap so phong thi so phong phai duy nhat trong cung mot Property.
CREATE UNIQUE INDEX UX_Rooms_Property_RoomNumber
ON Rooms(PropertyID, RoomNumber)
WHERE RoomNumber IS NOT NULL;
GO

CREATE INDEX IX_Rooms_PropertyID ON Rooms(PropertyID);
CREATE INDEX IX_Rooms_Search ON Rooms(ListingStatus, AvailabilityStatus, RentPrice, AreaM2);
CREATE INDEX IX_Rooms_AvailableFrom ON Rooms(AvailableFrom);
CREATE INDEX IX_Rooms_CreatedAt ON Rooms(CreatedAt DESC);
CREATE INDEX IX_Rooms_ReviewStatus ON Rooms(ListingStatus, CreatedAt DESC);
GO

CREATE TABLE RoomImages (
    ImageID BIGINT IDENTITY(1,1) PRIMARY KEY,
    RoomID BIGINT NOT NULL,
    ImageURL NVARCHAR(1000) NOT NULL,
    PublicId NVARCHAR(500) NULL,
    IsPrimary BIT NOT NULL CONSTRAINT DF_RoomImages_IsPrimary DEFAULT 0,
    SortOrder INT NOT NULL CONSTRAINT DF_RoomImages_SortOrder DEFAULT 0,
    CreatedAt DATETIME2 NOT NULL CONSTRAINT DF_RoomImages_CreatedAt DEFAULT SYSUTCDATETIME(),

    CONSTRAINT FK_RoomImages_Rooms FOREIGN KEY (RoomID)
        REFERENCES Rooms(RoomID) ON DELETE CASCADE,
    CONSTRAINT CK_RoomImages_SortOrder CHECK (SortOrder >= 0)
);
GO

CREATE INDEX IX_RoomImages_RoomID ON RoomImages(RoomID, SortOrder);

-- Moi phong chi co toi da mot anh dai dien.
CREATE UNIQUE INDEX UX_RoomImages_OnePrimaryPerRoom
ON RoomImages(RoomID)
WHERE IsPrimary = 1;

-- PublicId cua Cloudinary neu co thi nen duy nhat de xoa/quan ly anh chinh xac.
CREATE UNIQUE INDEX UX_RoomImages_PublicId_NotNull
ON RoomImages(PublicId)
WHERE PublicId IS NOT NULL;
GO

CREATE TABLE RoomAmenities (
    RoomID BIGINT NOT NULL,
    AmenityID INT NOT NULL,

    CONSTRAINT PK_RoomAmenities PRIMARY KEY (RoomID, AmenityID),
    CONSTRAINT FK_RoomAmenities_Rooms FOREIGN KEY (RoomID)
        REFERENCES Rooms(RoomID) ON DELETE CASCADE,
    CONSTRAINT FK_RoomAmenities_Amenities FOREIGN KEY (AmenityID)
        REFERENCES Amenities(AmenityID)
);
GO

CREATE INDEX IX_RoomAmenities_AmenityID ON RoomAmenities(AmenityID, RoomID);
GO

/* ============================================================================
   5. STUDENT INTERACTIONS: FAVORITES, VIEW HISTORY & APPOINTMENTS
   ============================================================================ */

CREATE TABLE SavedRooms (
    StudentID BIGINT NOT NULL,
    RoomID BIGINT NOT NULL,
    SavedAt DATETIME2 NOT NULL CONSTRAINT DF_SavedRooms_SavedAt DEFAULT SYSUTCDATETIME(),

    CONSTRAINT PK_SavedRooms PRIMARY KEY (StudentID, RoomID),
    CONSTRAINT FK_SavedRooms_Students FOREIGN KEY (StudentID)
        REFERENCES StudentProfiles(StudentID),
    CONSTRAINT FK_SavedRooms_Rooms FOREIGN KEY (RoomID)
        REFERENCES Rooms(RoomID) ON DELETE CASCADE
);
GO

CREATE INDEX IX_SavedRooms_RoomID ON SavedRooms(RoomID);
CREATE INDEX IX_SavedRooms_Student_SavedAt ON SavedRooms(StudentID, SavedAt DESC);
GO

-- Luu lich su xem phong cua STUDENT.
-- Moi Student + Room chi co 1 dong; backend UPSERT moi khi mo chi tiet phong:
--   - lan dau: FirstViewedAt = LastViewedAt, ViewCount = 1
--   - lan sau: LastViewedAt = now, ViewCount = ViewCount + 1
-- Dong thoi backend tang Rooms.ViewCount de co tong luot xem cua phong.
CREATE TABLE RoomViewHistories (
    RoomViewHistoryID BIGINT IDENTITY(1,1) PRIMARY KEY,
    StudentID BIGINT NOT NULL,
    RoomID BIGINT NOT NULL,
    FirstViewedAt DATETIME2 NOT NULL CONSTRAINT DF_RoomViewHistories_FirstViewedAt DEFAULT SYSUTCDATETIME(),
    LastViewedAt DATETIME2 NOT NULL CONSTRAINT DF_RoomViewHistories_LastViewedAt DEFAULT SYSUTCDATETIME(),
    ViewCount INT NOT NULL CONSTRAINT DF_RoomViewHistories_ViewCount DEFAULT 1,

    CONSTRAINT FK_RoomViewHistories_Students FOREIGN KEY (StudentID)
        REFERENCES StudentProfiles(StudentID),
    CONSTRAINT FK_RoomViewHistories_Rooms FOREIGN KEY (RoomID)
        REFERENCES Rooms(RoomID) ON DELETE CASCADE,

    CONSTRAINT UQ_RoomViewHistories_Student_Room UNIQUE (StudentID, RoomID),
    CONSTRAINT CK_RoomViewHistories_ViewCount CHECK (ViewCount > 0),
    CONSTRAINT CK_RoomViewHistories_Times CHECK (LastViewedAt >= FirstViewedAt)
);
GO

CREATE INDEX IX_RoomViewHistories_Student_LastViewedAt
ON RoomViewHistories(StudentID, LastViewedAt DESC);
CREATE INDEX IX_RoomViewHistories_RoomID
ON RoomViewHistories(RoomID);
GO

CREATE TABLE ViewingAppointments (
    AppointmentID BIGINT IDENTITY(1,1) PRIMARY KEY,
    RoomID BIGINT NOT NULL,
    StudentID BIGINT NOT NULL,
    ScheduledAt DATETIME2 NOT NULL,
    Note NVARCHAR(1000) NULL,
    Status VARCHAR(20) NOT NULL CONSTRAINT DF_ViewingAppointments_Status DEFAULT 'PENDING',
    LandlordNote NVARCHAR(1000) NULL,
    RespondedAt DATETIME2 NULL,
    CreatedAt DATETIME2 NOT NULL CONSTRAINT DF_ViewingAppointments_CreatedAt DEFAULT SYSUTCDATETIME(),
    UpdatedAt DATETIME2 NOT NULL CONSTRAINT DF_ViewingAppointments_UpdatedAt DEFAULT SYSUTCDATETIME(),

    CONSTRAINT FK_ViewingAppointments_Rooms FOREIGN KEY (RoomID)
        REFERENCES Rooms(RoomID),
    CONSTRAINT FK_ViewingAppointments_Students FOREIGN KEY (StudentID)
        REFERENCES StudentProfiles(StudentID),

    CONSTRAINT CK_ViewingAppointments_Status CHECK (
        Status IN ('PENDING','CONFIRMED','COMPLETED','CANCELLED','REJECTED')
    )
);
GO

CREATE INDEX IX_ViewingAppointments_StudentID ON ViewingAppointments(StudentID, CreatedAt DESC);
CREATE INDEX IX_ViewingAppointments_RoomID_Status ON ViewingAppointments(RoomID, Status);
CREATE INDEX IX_ViewingAppointments_ScheduledAt ON ViewingAppointments(ScheduledAt);
GO

/* ============================================================================
   6. RENTAL REQUESTS, CONTRACTS & PAYMENTS
   ============================================================================ */

CREATE TABLE RentalRequests (
    RentalRequestID BIGINT IDENTITY(1,1) PRIMARY KEY,
    RoomID BIGINT NOT NULL,
    StudentID BIGINT NOT NULL,
    RequestedMoveInDate DATE NOT NULL,
    DurationMonths INT NULL,
    Note NVARCHAR(1500) NULL,
    Status VARCHAR(20) NOT NULL CONSTRAINT DF_RentalRequests_Status DEFAULT 'PENDING',
    LandlordNote NVARCHAR(1500) NULL,
    RespondedAt DATETIME2 NULL,
    CreatedAt DATETIME2 NOT NULL CONSTRAINT DF_RentalRequests_CreatedAt DEFAULT SYSUTCDATETIME(),
    UpdatedAt DATETIME2 NOT NULL CONSTRAINT DF_RentalRequests_UpdatedAt DEFAULT SYSUTCDATETIME(),

    CONSTRAINT FK_RentalRequests_Rooms FOREIGN KEY (RoomID)
        REFERENCES Rooms(RoomID),
    CONSTRAINT FK_RentalRequests_Students FOREIGN KEY (StudentID)
        REFERENCES StudentProfiles(StudentID),

    -- Composite key phuc vu FK cua RentalContracts.
    -- RentalRequestID da la PK; unique nay giup database rang buoc Request + Room la mot cap khong the sai lech.
    CONSTRAINT UQ_RentalRequests_Request_Room UNIQUE (RentalRequestID, RoomID),
    CONSTRAINT CK_RentalRequests_Duration CHECK (DurationMonths IS NULL OR DurationMonths > 0),
    CONSTRAINT CK_RentalRequests_Status CHECK (Status IN ('PENDING','APPROVED','REJECTED','CANCELLED'))
);
GO

-- Tranh Student bam gui nhieu lan tao nhieu request PENDING cho cung mot phong.
CREATE UNIQUE INDEX UX_RentalRequests_Student_Room_Pending
ON RentalRequests(StudentID, RoomID)
WHERE Status = 'PENDING';

CREATE INDEX IX_RentalRequests_StudentID ON RentalRequests(StudentID, CreatedAt DESC);
CREATE INDEX IX_RentalRequests_RoomID_Status ON RentalRequests(RoomID, Status);
CREATE INDEX IX_RentalRequests_Status_CreatedAt ON RentalRequests(Status, CreatedAt DESC);
GO

-- RentalContracts chi luu cac khoa can thiet.
-- StudentID duoc suy ra tu RentalRequests.StudentID.
-- LandlordID duoc suy ra theo Room -> Property -> Landlord.
-- Cach nay loai bo hoan toan nguy co hop dong luu Student/Landlord khac voi yeu cau thue.
-- RoomID duoc giu lai de tao unique filtered index cho hop dong ACTIVE,
-- nhung duoc bao ve bang composite FK (RentalRequestID, RoomID).
CREATE TABLE RentalContracts (
    ContractID BIGINT IDENTITY(1,1) PRIMARY KEY,
    RentalRequestID BIGINT NOT NULL,
    ContractCode VARCHAR(50) NOT NULL,
    RoomID BIGINT NOT NULL,

    StartDate DATE NOT NULL,
    EndDate DATE NULL,
    MonthlyRent DECIMAL(18,2) NOT NULL,
    DepositAmount DECIMAL(18,2) NOT NULL CONSTRAINT DF_RentalContracts_Deposit DEFAULT 0,
    Terms NVARCHAR(MAX) NULL,
    ContractFileURL NVARCHAR(1000) NULL,

    Status VARCHAR(20) NOT NULL CONSTRAINT DF_RentalContracts_Status DEFAULT 'PENDING',
    CreatedAt DATETIME2 NOT NULL CONSTRAINT DF_RentalContracts_CreatedAt DEFAULT SYSUTCDATETIME(),
    ActivatedAt DATETIME2 NULL,
    CompletedAt DATETIME2 NULL,
    CancelledAt DATETIME2 NULL,

    -- Database tu dong bao dam RoomID cua hop dong chinh la RoomID cua RentalRequest.
    CONSTRAINT FK_RentalContracts_RequestRoom FOREIGN KEY (RentalRequestID, RoomID)
        REFERENCES RentalRequests(RentalRequestID, RoomID),

    CONSTRAINT UQ_RentalContracts_RentalRequestID UNIQUE (RentalRequestID),
    CONSTRAINT UQ_RentalContracts_ContractCode UNIQUE (ContractCode),
    CONSTRAINT CK_RentalContracts_Dates CHECK (EndDate IS NULL OR EndDate >= StartDate),
    CONSTRAINT CK_RentalContracts_MonthlyRent CHECK (MonthlyRent >= 0),
    CONSTRAINT CK_RentalContracts_Deposit CHECK (DepositAmount >= 0),
    CONSTRAINT CK_RentalContracts_Status CHECK (Status IN ('PENDING','ACTIVE','COMPLETED','CANCELLED'))
);
GO

-- Mot phong chi duoc co toi da mot hop dong ACTIVE tai cung thoi diem.
CREATE UNIQUE INDEX UX_RentalContracts_OneActivePerRoom
ON RentalContracts(RoomID)
WHERE Status = 'ACTIVE';

CREATE INDEX IX_RentalContracts_RoomID ON RentalContracts(RoomID, Status);
CREATE INDEX IX_RentalContracts_Status_Dates ON RentalContracts(Status, StartDate, EndDate);
GO

-- View tien loi cho API/Admin: StudentID va LandlordID duoc suy ra thay vi luu lap trong hop dong.
CREATE VIEW vw_RentalContractDetails
AS
SELECT
    RC.ContractID,
    RC.RentalRequestID,
    RC.ContractCode,
    RC.RoomID,
    RR.StudentID,
    P.LandlordID,
    RC.StartDate,
    RC.EndDate,
    RC.MonthlyRent,
    RC.DepositAmount,
    RC.Terms,
    RC.ContractFileURL,
    RC.Status,
    RC.CreatedAt,
    RC.ActivatedAt,
    RC.CompletedAt,
    RC.CancelledAt
FROM RentalContracts RC
INNER JOIN RentalRequests RR ON RR.RentalRequestID = RC.RentalRequestID
                              AND RR.RoomID = RC.RoomID
INNER JOIN Rooms R ON R.RoomID = RC.RoomID
INNER JOIN Properties P ON P.PropertyID = R.PropertyID;
GO

-- Theo doi thanh toan; khong bat buoc tich hop cong thanh toan online.
CREATE TABLE Payments (
    PaymentID BIGINT IDENTITY(1,1) PRIMARY KEY,
    ContractID BIGINT NOT NULL,
    PaymentType VARCHAR(20) NOT NULL,
    Amount DECIMAL(18,2) NOT NULL,
    DueDate DATE NULL,
    PaymentMethod VARCHAR(30) NULL,
    Status VARCHAR(20) NOT NULL CONSTRAINT DF_Payments_Status DEFAULT 'PENDING',

    TransactionReference VARCHAR(150) NULL,
    ProofImageURL NVARCHAR(1000) NULL,
    Note NVARCHAR(1000) NULL,

    -- Landlord xac nhan minh chung/da nhan tien.
    ConfirmedByUserID BIGINT NULL,
    ConfirmedAt DATETIME2 NULL,
    PaidAt DATETIME2 NULL,

    CreatedAt DATETIME2 NOT NULL CONSTRAINT DF_Payments_CreatedAt DEFAULT SYSUTCDATETIME(),
    UpdatedAt DATETIME2 NOT NULL CONSTRAINT DF_Payments_UpdatedAt DEFAULT SYSUTCDATETIME(),

    CONSTRAINT FK_Payments_Contracts FOREIGN KEY (ContractID)
        REFERENCES RentalContracts(ContractID),
    CONSTRAINT FK_Payments_ConfirmedBy FOREIGN KEY (ConfirmedByUserID)
        REFERENCES Users(UserID),

    CONSTRAINT CK_Payments_Type CHECK (PaymentType IN ('DEPOSIT','RENT','UTILITY','OTHER')),
    CONSTRAINT CK_Payments_Amount CHECK (Amount > 0),
    CONSTRAINT CK_Payments_Method CHECK (
        PaymentMethod IS NULL OR PaymentMethod IN ('CASH','BANK_TRANSFER','OTHER')
    ),
    CONSTRAINT CK_Payments_Status CHECK (Status IN ('PENDING','PAID','CANCELLED','FAILED'))
);
GO

CREATE INDEX IX_Payments_ContractID ON Payments(ContractID, CreatedAt DESC);
CREATE INDEX IX_Payments_Status_DueDate ON Payments(Status, DueDate);
CREATE INDEX IX_Payments_PaidAt ON Payments(PaidAt DESC);
GO

/* ============================================================================
   7. CHAT
   ============================================================================ */

CREATE TABLE Conversations (
    ConversationID BIGINT IDENTITY(1,1) PRIMARY KEY,
    RoomID BIGINT NOT NULL,
    StudentID BIGINT NOT NULL,
    LastMessageAt DATETIME2 NULL,
    CreatedAt DATETIME2 NOT NULL CONSTRAINT DF_Conversations_CreatedAt DEFAULT SYSUTCDATETIME(),

    CONSTRAINT FK_Conversations_Rooms FOREIGN KEY (RoomID)
        REFERENCES Rooms(RoomID),
    CONSTRAINT FK_Conversations_Students FOREIGN KEY (StudentID)
        REFERENCES StudentProfiles(StudentID),

    -- Landlord cua cuoc tro chuyen luon la chu cua Room -> Property, khong luu lap LandlordID.
    CONSTRAINT UQ_Conversations_Room_Student UNIQUE (RoomID, StudentID)
);
GO

CREATE INDEX IX_Conversations_StudentID ON Conversations(StudentID, LastMessageAt DESC);
CREATE INDEX IX_Conversations_RoomID ON Conversations(RoomID, LastMessageAt DESC);
GO

CREATE TABLE Messages (
    MessageID BIGINT IDENTITY(1,1) PRIMARY KEY,
    ConversationID BIGINT NOT NULL,
    SenderUserID BIGINT NOT NULL,
    Content NVARCHAR(2000) NULL,
    ImageURL NVARCHAR(1000) NULL,
    MessageType VARCHAR(20) NOT NULL CONSTRAINT DF_Messages_MessageType DEFAULT 'TEXT',
    IsRead BIT NOT NULL CONSTRAINT DF_Messages_IsRead DEFAULT 0,
    ReadAt DATETIME2 NULL,
    SentAt DATETIME2 NOT NULL CONSTRAINT DF_Messages_SentAt DEFAULT SYSUTCDATETIME(),

    CONSTRAINT FK_Messages_Conversations FOREIGN KEY (ConversationID)
        REFERENCES Conversations(ConversationID) ON DELETE CASCADE,
    CONSTRAINT FK_Messages_Sender FOREIGN KEY (SenderUserID)
        REFERENCES Users(UserID),

    CONSTRAINT CK_Messages_Type CHECK (MessageType IN ('TEXT','IMAGE')),
    CONSTRAINT CK_Messages_Content CHECK (
        (MessageType = 'TEXT' AND Content IS NOT NULL)
        OR (MessageType = 'IMAGE' AND ImageURL IS NOT NULL)
    )
);
GO

CREATE INDEX IX_Messages_ConversationID_SentAt ON Messages(ConversationID, SentAt DESC);
CREATE INDEX IX_Messages_Unread ON Messages(ConversationID, IsRead, SentAt DESC);
GO

/* ============================================================================
   8. REVIEWS, REPORTS & ADMIN MODERATION
   ============================================================================ */

-- Service layer chi cho phep review neu Contract ACTIVE/COMPLETED.
CREATE TABLE Reviews (
    ReviewID BIGINT IDENTITY(1,1) PRIMARY KEY,
    ContractID BIGINT NOT NULL,
    Rating TINYINT NOT NULL,
    Comment NVARCHAR(2000) NULL,
    Status VARCHAR(20) NOT NULL CONSTRAINT DF_Reviews_Status DEFAULT 'VISIBLE',
    ModeratedByUserID BIGINT NULL,
    ModeratedAt DATETIME2 NULL,
    CreatedAt DATETIME2 NOT NULL CONSTRAINT DF_Reviews_CreatedAt DEFAULT SYSUTCDATETIME(),
    UpdatedAt DATETIME2 NOT NULL CONSTRAINT DF_Reviews_UpdatedAt DEFAULT SYSUTCDATETIME(),

    CONSTRAINT FK_Reviews_Contracts FOREIGN KEY (ContractID)
        REFERENCES RentalContracts(ContractID),
    CONSTRAINT FK_Reviews_ModeratedBy FOREIGN KEY (ModeratedByUserID)
        REFERENCES Users(UserID),

    CONSTRAINT UQ_Reviews_ContractID UNIQUE (ContractID),
    CONSTRAINT CK_Reviews_Rating CHECK (Rating BETWEEN 1 AND 5),
    CONSTRAINT CK_Reviews_Status CHECK (Status IN ('VISIBLE','HIDDEN'))
);
GO

CREATE INDEX IX_Reviews_Status_CreatedAt ON Reviews(Status, CreatedAt DESC);
CREATE INDEX IX_Reviews_Rating ON Reviews(Rating);
GO

CREATE TABLE Reports (
    ReportID BIGINT IDENTITY(1,1) PRIMARY KEY,
    ReporterUserID BIGINT NOT NULL,
    RoomID BIGINT NOT NULL,
    Reason NVARCHAR(255) NOT NULL,
    Description NVARCHAR(2000) NULL,
    Status VARCHAR(20) NOT NULL CONSTRAINT DF_Reports_Status DEFAULT 'OPEN',
    AdminNote NVARCHAR(2000) NULL,
    ResolvedByUserID BIGINT NULL,
    ResolvedAt DATETIME2 NULL,
    CreatedAt DATETIME2 NOT NULL CONSTRAINT DF_Reports_CreatedAt DEFAULT SYSUTCDATETIME(),

    CONSTRAINT FK_Reports_Reporter FOREIGN KEY (ReporterUserID)
        REFERENCES Users(UserID),
    CONSTRAINT FK_Reports_Rooms FOREIGN KEY (RoomID)
        REFERENCES Rooms(RoomID),
    CONSTRAINT FK_Reports_ResolvedBy FOREIGN KEY (ResolvedByUserID)
        REFERENCES Users(UserID),

    CONSTRAINT CK_Reports_Status CHECK (Status IN ('OPEN','RESOLVED','REJECTED'))
);
GO

CREATE INDEX IX_Reports_RoomID_Status ON Reports(RoomID, Status);
CREATE INDEX IX_Reports_Status_CreatedAt ON Reports(Status, CreatedAt DESC);
CREATE INDEX IX_Reports_ReporterUserID ON Reports(ReporterUserID, CreatedAt DESC);
GO

/* ============================================================================
   9. NOTIFICATIONS & FCM DEVICES
   ============================================================================ */

CREATE TABLE Notifications (
    NotificationID BIGINT IDENTITY(1,1) PRIMARY KEY,
    UserID BIGINT NOT NULL,
    NotificationType VARCHAR(50) NULL,
    Title NVARCHAR(255) NOT NULL,
    Message NVARCHAR(1000) NOT NULL,
    RelatedEntityType VARCHAR(50) NULL,
    RelatedEntityID BIGINT NULL,
    IsRead BIT NOT NULL CONSTRAINT DF_Notifications_IsRead DEFAULT 0,
    ReadAt DATETIME2 NULL,
    CreatedAt DATETIME2 NOT NULL CONSTRAINT DF_Notifications_CreatedAt DEFAULT SYSUTCDATETIME(),

    CONSTRAINT FK_Notifications_Users FOREIGN KEY (UserID)
        REFERENCES Users(UserID) ON DELETE CASCADE
);
GO

CREATE INDEX IX_Notifications_UserID_IsRead
ON Notifications(UserID, IsRead, CreatedAt DESC);
GO

CREATE TABLE UserDeviceTokens (
    DeviceTokenID BIGINT IDENTITY(1,1) PRIMARY KEY,
    UserID BIGINT NOT NULL,

    -- VARCHAR(512) tranh warning vuot gioi han 1700 bytes cua nonclustered index.
    DeviceToken VARCHAR(512) NOT NULL,
    Platform VARCHAR(20) NOT NULL CONSTRAINT DF_UserDeviceTokens_Platform DEFAULT 'ANDROID',
    IsActive BIT NOT NULL CONSTRAINT DF_UserDeviceTokens_IsActive DEFAULT 1,
    LastSeenAt DATETIME2 NOT NULL CONSTRAINT DF_UserDeviceTokens_LastSeenAt DEFAULT SYSUTCDATETIME(),
    CreatedAt DATETIME2 NOT NULL CONSTRAINT DF_UserDeviceTokens_CreatedAt DEFAULT SYSUTCDATETIME(),

    CONSTRAINT FK_UserDeviceTokens_Users FOREIGN KEY (UserID)
        REFERENCES Users(UserID) ON DELETE CASCADE,
    CONSTRAINT UQ_UserDeviceTokens_DeviceToken UNIQUE (DeviceToken),
    CONSTRAINT CK_UserDeviceTokens_Platform CHECK (Platform IN ('ANDROID','WEB'))
);
GO

CREATE INDEX IX_UserDeviceTokens_UserID ON UserDeviceTokens(UserID, IsActive);
GO

/* ============================================================================
   10. SEED DATA
   ============================================================================ */

INSERT INTO Roles(RoleCode, RoleName) VALUES
('STUDENT', N'Sinh viên'),
('LANDLORD', N'Chủ trọ'),
('ADMIN', N'Quản trị viên');
GO

INSERT INTO PropertyTypes(TypeName) VALUES
(N'Nhà trọ / Phòng trọ'),
(N'Căn hộ chung cư'),
(N'Chung cư mini'),
(N'Ký túc xá'),
(N'Nhà nguyên căn');
GO

INSERT INTO Amenities(AmenityName) VALUES
(N'Điều hòa'),
(N'Bình nóng lạnh'),
(N'Máy giặt'),
(N'Tủ lạnh'),
(N'Giường'),
(N'Tủ quần áo'),
(N'Chỗ để xe'),
(N'Bảo vệ 24/7'),
(N'Camera an ninh'),
(N'Wi-Fi'),
(N'Nhà vệ sinh riêng'),
(N'Bếp'),
(N'Ban công'),
(N'Thú cưng được phép'),
(N'Giờ giấc tự do');
GO

/* ============================================================================
   11. FINAL CHECK
   ============================================================================ */

PRINT N'RoomifyDB FINAL schema created successfully.';
PRINT N'Total tables expected: 27 (Districts removed for two-tier local government model).';
PRINT N'Address model: Province/Municipality -> Ward/Commune/Special zone.';
PRINT N'RentalContracts consistency strengthened with composite Request+Room FK; Student/Landlord are derived.';
PRINT N'RoomViewHistories is included for student room-view history.';
GO
