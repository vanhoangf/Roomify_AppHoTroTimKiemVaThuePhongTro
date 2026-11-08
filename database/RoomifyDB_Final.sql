-- ============================================================================
-- ROOMIFY - DATABASE SCHEMA DONG BO ROOMIFY.API DEN HET BUOC 21
-- De tai: Ung dung ho tro tim kiem va thue phong tro danh cho sinh vien
-- DBMS: Microsoft SQL Server 2019+
-- Backend: ASP.NET Core Web API + Entity Framework Core Database First + JWT
-- Mobile: Kotlin Android (STUDENT / LANDLORD)
-- Admin: Web rieng (ADMIN)
--
-- PHIEN BAN DONG BO:
--   - Backend Roomify.API sau khi hoan thanh Payments (Buoc 20)
--   - Backend Roomify.API sau khi hoan thanh Admin APIs (Buoc 21)
--   - ViewingAppointments da tich hop ProposedAt + RowVer + PROPOSED
--   - ViewingAppointmentHistories duoc tao truc tiep trong schema chinh
--
-- LUU Y:
--   @RecreateDatabase = 1 se XOA database RoomifyDB cu va tao lai tu dau.
--   File nay dung de cac thanh vien dung chung mot schema sach khi tich hop
--   Android Student, Android Landlord va Admin Web.
--   Neu database da co du lieu that, KHONG dung DROP DATABASE; hay dung script
--   migration/ALTER rieng.
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
ON RoomImages(RoomID, IsPrimary)
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

    -- Thoi gian chu tro de xuat lai khi can thuong luong lich xem phong.
    ProposedAt DATETIME2(7) NULL,

    Note NVARCHAR(1000) NULL,
    Status VARCHAR(20) NOT NULL CONSTRAINT DF_ViewingAppointments_Status DEFAULT 'PENDING',
    LandlordNote NVARCHAR(1000) NULL,
    RespondedAt DATETIME2 NULL,
    CreatedAt DATETIME2 NOT NULL CONSTRAINT DF_ViewingAppointments_CreatedAt DEFAULT SYSUTCDATETIME(),
    UpdatedAt DATETIME2 NOT NULL CONSTRAINT DF_ViewingAppointments_UpdatedAt DEFAULT SYSUTCDATETIME(),

    -- EF Core su dung IsRowVersion() de phat hien cap nhat dong thoi.
    RowVer ROWVERSION NOT NULL,

    CONSTRAINT FK_ViewingAppointments_Rooms FOREIGN KEY (RoomID)
        REFERENCES Rooms(RoomID),
    CONSTRAINT FK_ViewingAppointments_Students FOREIGN KEY (StudentID)
        REFERENCES StudentProfiles(StudentID),

    CONSTRAINT CK_ViewingAppointments_Status CHECK (
        Status IN ('PENDING','PROPOSED','CONFIRMED','COMPLETED','CANCELLED','REJECTED')
    ),

    -- Trang thai PROPOSED bat buoc co ProposedAt; cac trang thai khac khong luu ProposedAt.
    CONSTRAINT CK_ViewingAppointments_ProposedAt CHECK (
        (Status = 'PROPOSED' AND ProposedAt IS NOT NULL)
        OR
        (Status <> 'PROPOSED' AND ProposedAt IS NULL)
    )
);
GO

CREATE INDEX IX_ViewingAppointments_StudentID
ON ViewingAppointments(StudentID, CreatedAt DESC);

CREATE INDEX IX_ViewingAppointments_RoomID_Status
ON ViewingAppointments(RoomID, Status);

CREATE INDEX IX_ViewingAppointments_ScheduledAt
ON ViewingAppointments(ScheduledAt);
GO

-- Lich su xu ly lich hen. Backend truy cap bang _db.Set<ViewingAppointmentHistory>().
CREATE TABLE ViewingAppointmentHistories (
    AppointmentHistoryID BIGINT IDENTITY(1,1) PRIMARY KEY,
    AppointmentID BIGINT NOT NULL,
    ActorUserID BIGINT NOT NULL,

    Action VARCHAR(30) NOT NULL,

    OldStatus VARCHAR(20) NULL,
    NewStatus VARCHAR(20) NOT NULL,

    OldScheduledAt DATETIME2(7) NULL,
    NewScheduledAt DATETIME2(7) NULL,

    Note NVARCHAR(1000) NULL,

    CreatedAt DATETIME2(7) NOT NULL
        CONSTRAINT DF_ViewingAppointmentHistories_CreatedAt
        DEFAULT SYSUTCDATETIME(),

    CONSTRAINT FK_ViewingAppointmentHistories_Appointment
        FOREIGN KEY (AppointmentID)
        REFERENCES ViewingAppointments(AppointmentID),

    CONSTRAINT FK_ViewingAppointmentHistories_Actor
        FOREIGN KEY (ActorUserID)
        REFERENCES Users(UserID)
);
GO

CREATE INDEX IX_ViewingAppointmentHistories_Appointment
ON ViewingAppointmentHistories(AppointmentID, CreatedAt DESC);
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

-- Moi user chi duoc co toi da 1 bao cao OPEN cho cung 1 phong tai mot thoi diem.
-- Sau khi report cu da RESOLVED/REJECTED, user co the tao report moi cho phong do.
CREATE UNIQUE INDEX UX_Reports_Reporter_Room_Open
ON Reports(ReporterUserID, RoomID)
WHERE Status = 'OPEN';
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

PRINT N'RoomifyDB schema synced with Roomify.API through Step 21.';
PRINT N'Total tables expected: 28 (including ViewingAppointmentHistories).';
PRINT N'Address model: Province/Municipality -> Ward/Commune/Special zone.';
PRINT N'RentalContracts use composite Request+Room consistency; Student/Landlord are derived.';
PRINT N'ViewingAppointments includes PROPOSED workflow, ProposedAt and RowVer.';
PRINT N'RoomViewHistories and ViewingAppointmentHistories are included.';
GO

SET NOCOUNT ON;
SET XACT_ABORT ON;
GO

IF EXISTS (SELECT 1 FROM Users WHERE Email LIKE '%@roomify.demo')
    THROW 50001, 'Roomify demo data already exists. Do not run this seed twice.', 1;
GO

BEGIN TRY
BEGIN TRANSACTION;

-- ============================================================================
-- 1. MASTER DATA
-- ============================================================================

IF NOT EXISTS (SELECT 1 FROM Roles WHERE RoleCode='STUDENT')
    INSERT INTO Roles(RoleCode,RoleName) VALUES('STUDENT',N'Sinh viên');
IF NOT EXISTS (SELECT 1 FROM Roles WHERE RoleCode='LANDLORD')
    INSERT INTO Roles(RoleCode,RoleName) VALUES('LANDLORD',N'Chủ trọ');
IF NOT EXISTS (SELECT 1 FROM Roles WHERE RoleCode='ADMIN')
    INSERT INTO Roles(RoleCode,RoleName) VALUES('ADMIN',N'Quản trị viên');

DECLARE @Provinces TABLE(Code VARCHAR(20), Name NVARCHAR(150), Type VARCHAR(20));
INSERT INTO @Provinces VALUES
('DN',N'TP. Đà Nẵng','MUNICIPALITY'),
('HCM',N'TP. Hồ Chí Minh','MUNICIPALITY'),
('HN',N'TP. Hà Nội','MUNICIPALITY'),
('HP',N'TP. Hải Phòng','MUNICIPALITY'),
('HUE',N'TP. Huế','MUNICIPALITY'),
('CT',N'TP. Cần Thơ','MUNICIPALITY'),
('QN',N'Quảng Ninh','PROVINCE'),
('TH',N'Thanh Hóa','PROVINCE'),
('NA',N'Nghệ An','PROVINCE'),
('KH',N'Khánh Hòa','PROVINCE');

INSERT INTO Provinces(ProvinceCode,ProvinceName,ProvinceType,IsActive)
SELECT P.Code,P.Name,P.Type,1
FROM @Provinces P
WHERE NOT EXISTS (SELECT 1 FROM Provinces X WHERE X.ProvinceName=P.Name);

DECLARE @DaNangID INT=(SELECT ProvinceID FROM Provinces WHERE ProvinceName=N'TP. Đà Nẵng');

DECLARE @Wards TABLE(Code VARCHAR(20), Name NVARCHAR(150));
INSERT INTO @Wards VALUES
('DN01',N'Hòa Khánh'),('DN02',N'Hải Châu'),('DN03',N'An Hải'),
('DN04',N'Ngũ Hành Sơn'),('DN05',N'Hòa Cường'),('DN06',N'Thanh Khê'),
('DN07',N'Cẩm Lệ'),('DN08',N'Liên Chiểu'),('DN09',N'Sơn Trà'),('DN10',N'Hòa Xuân');

INSERT INTO Wards(ProvinceID,WardCode,WardName,WardType,IsActive)
SELECT @DaNangID,W.Code,W.Name,'WARD',1
FROM @Wards W
WHERE NOT EXISTS (
    SELECT 1 FROM Wards X WHERE X.ProvinceID=@DaNangID AND X.WardName=W.Name
);

DECLARE @Universities TABLE(No INT, Name NVARCHAR(255), WardName NVARCHAR(150), AddressLine NVARCHAR(500));
INSERT INTO @Universities VALUES
(1,N'Trường Đại học Bách khoa - Đại học Đà Nẵng',N'Hòa Khánh',N'54 Nguyễn Lương Bằng'),
(2,N'Trường Đại học Sư phạm Kỹ thuật - Đại học Đà Nẵng',N'Thanh Khê',N'48 Cao Thắng'),
(3,N'Trường Đại học Kinh tế - Đại học Đà Nẵng',N'Ngũ Hành Sơn',N'71 Ngũ Hành Sơn'),
(4,N'Trường Đại học Sư phạm - Đại học Đà Nẵng',N'Hòa Khánh',N'459 Tôn Đức Thắng'),
(5,N'Trường Đại học Ngoại ngữ - Đại học Đà Nẵng',N'Cẩm Lệ',N'131 Lương Nhữ Hộc'),
(6,N'Trường Đại học CNTT & Truyền thông Việt - Hàn',N'Ngũ Hành Sơn',N'470 Trần Đại Nghĩa'),
(7,N'Đại học Duy Tân',N'Hải Châu',N'254 Nguyễn Văn Linh'),
(8,N'Đại học Đông Á',N'Hòa Cường',N'33 Xô Viết Nghệ Tĩnh'),
(9,N'Đại học Kiến trúc Đà Nẵng',N'Hòa Cường',N'566 Núi Thành'),
(10,N'Đại học FPT Đà Nẵng',N'Hòa Xuân',N'Khu đô thị FPT City');

INSERT INTO Universities(UniversityName,ProvinceID,WardID,AddressLine,Latitude,Longitude,IsActive)
SELECT U.Name,@DaNangID,W.WardID,U.AddressLine,
       CAST(16.0000000 + U.No*0.003 AS DECIMAL(10,7)),
       CAST(108.1400000 + U.No*0.008 AS DECIMAL(10,7)),1
FROM @Universities U
JOIN Wards W ON W.ProvinceID=@DaNangID AND W.WardName=U.WardName
WHERE NOT EXISTS (SELECT 1 FROM Universities X WHERE X.UniversityName=U.Name);

DECLARE @PropertyTypes TABLE(Name NVARCHAR(100));
INSERT INTO @PropertyTypes VALUES
(N'Nhà trọ / Phòng trọ'),(N'Căn hộ chung cư'),(N'Chung cư mini'),
(N'Ký túc xá'),(N'Nhà nguyên căn'),(N'Căn hộ dịch vụ'),
(N'Homestay sinh viên'),(N'Sleepbox'),(N'Nhà ở xã hội sinh viên'),
(N'Nhà nguyên căn chia phòng');

INSERT INTO PropertyTypes(TypeName,IsActive)
SELECT P.Name,1 FROM @PropertyTypes P
WHERE NOT EXISTS (SELECT 1 FROM PropertyTypes X WHERE X.TypeName=P.Name);

DECLARE @Amenities TABLE(Name NVARCHAR(100));
INSERT INTO @Amenities VALUES
(N'Điều hòa'),(N'Bình nóng lạnh'),(N'Máy giặt'),(N'Tủ lạnh'),(N'Giường'),
(N'Tủ quần áo'),(N'Chỗ để xe'),(N'Bảo vệ 24/7'),(N'Camera an ninh'),(N'Wi-Fi');

INSERT INTO Amenities(AmenityName,IsActive)
SELECT A.Name,1 FROM @Amenities A
WHERE NOT EXISTS (SELECT 1 FROM Amenities X WHERE X.AmenityName=A.Name);

-- ============================================================================
-- 2. USERS: 10 ADMIN + 10 STUDENT + 10 LANDLORD
-- ============================================================================

DECLARE @PasswordHash VARCHAR(500)='AQAAAAIAAYagAAAAEBhXXciCg7o7VScwX0urZslJYStStP3sA2DPdPJFwiJim1HICmqj1R5HwL1qkFSW6g==';
DECLARE @i INT=1;

WHILE @i<=10
BEGIN
    INSERT INTO Users(RoleID,Email,PasswordHash,FullName,Phone,AvatarURL,
                      Status,EmailVerified,LastLoginAt,CreatedAt,UpdatedAt)
    VALUES(
        (SELECT RoleID FROM Roles WHERE RoleCode='ADMIN'),
        CONCAT('admin',FORMAT(@i,'00'),'@roomify.demo'),
        @PasswordHash,
        CONCAT(N'Quản trị viên Demo ',FORMAT(@i,'00')),
        CONCAT('09000000',FORMAT(@i,'00')),
        CONCAT(N'https://cdn.roomify.demo/avatars/admin',FORMAT(@i,'00'),N'.jpg'),
        'ACTIVE',1,DATEADD(DAY,-@i,SYSUTCDATETIME()),
        DATEADD(DAY,-120-@i,SYSUTCDATETIME()),SYSUTCDATETIME()
    );
    SET @i+=1;
END;

SET @i=1;
WHILE @i<=10
BEGIN
    INSERT INTO Users(RoleID,Email,PasswordHash,FullName,Phone,AvatarURL,
                      Status,EmailVerified,LockedReason,LockedAt,LockedByUserID,
                      LastLoginAt,CreatedAt,UpdatedAt)
    VALUES(
        (SELECT RoleID FROM Roles WHERE RoleCode='STUDENT'),
        CONCAT('student',FORMAT(@i,'00'),'@roomify.demo'),
        @PasswordHash,
        CONCAT(N'Sinh viên Demo ',FORMAT(@i,'00')),
        CONCAT('09110000',FORMAT(@i,'00')),
        CONCAT(N'https://cdn.roomify.demo/avatars/student',FORMAT(@i,'00'),N'.jpg'),
        CASE WHEN @i=10 THEN 'LOCKED' ELSE 'ACTIVE' END,
        CASE WHEN @i<=8 THEN 1 ELSE 0 END,
        CASE WHEN @i=10 THEN N'Tài khoản demo bị khóa để kiểm thử Admin' ELSE NULL END,
        CASE WHEN @i=10 THEN DATEADD(DAY,-2,SYSUTCDATETIME()) ELSE NULL END,
        CASE WHEN @i=10 THEN (SELECT UserID FROM Users WHERE Email='admin01@roomify.demo') ELSE NULL END,
        DATEADD(DAY,-@i,SYSUTCDATETIME()),
        DATEADD(DAY,-60-@i,SYSUTCDATETIME()),SYSUTCDATETIME()
    );
    SET @i+=1;
END;

SET @i=1;
WHILE @i<=10
BEGIN
    INSERT INTO Users(RoleID,Email,PasswordHash,FullName,Phone,AvatarURL,
                      Status,EmailVerified,LockedReason,LockedAt,LockedByUserID,
                      LastLoginAt,CreatedAt,UpdatedAt)
    VALUES(
        (SELECT RoleID FROM Roles WHERE RoleCode='LANDLORD'),
        CONCAT('landlord',FORMAT(@i,'00'),'@roomify.demo'),
        @PasswordHash,
        CONCAT(N'Chủ trọ Demo ',FORMAT(@i,'00')),
        CONCAT('09820000',FORMAT(@i,'00')),
        CONCAT(N'https://cdn.roomify.demo/avatars/landlord',FORMAT(@i,'00'),N'.jpg'),
        CASE WHEN @i=10 THEN 'LOCKED' ELSE 'ACTIVE' END,
        CASE WHEN @i<=9 THEN 1 ELSE 0 END,
        CASE WHEN @i=10 THEN N'Tài khoản demo bị khóa để kiểm thử Admin' ELSE NULL END,
        CASE WHEN @i=10 THEN DATEADD(DAY,-3,SYSUTCDATETIME()) ELSE NULL END,
        CASE WHEN @i=10 THEN (SELECT UserID FROM Users WHERE Email='admin02@roomify.demo') ELSE NULL END,
        DATEADD(DAY,-@i,SYSUTCDATETIME()),
        DATEADD(DAY,-90-@i,SYSUTCDATETIME()),SYSUTCDATETIME()
    );
    SET @i+=1;
END;

-- ============================================================================
-- 3. REFRESH TOKENS + PASSWORD RESET TOKENS: 10 EACH
-- ============================================================================

SET @i=1;
WHILE @i<=10
BEGIN
    INSERT INTO RefreshTokens(UserID,TokenHash,ExpiresAt,RevokedAt,CreatedAt)
    VALUES(
        (SELECT UserID FROM Users WHERE Email=CONCAT('student',FORMAT(@i,'00'),'@roomify.demo')),
        CONCAT('DEMO_REFRESH_HASH_',FORMAT(@i,'00')),
        DATEADD(DAY,30-@i,SYSUTCDATETIME()),
        CASE WHEN @i>=9 THEN DATEADD(DAY,-1,SYSUTCDATETIME()) ELSE NULL END,
        DATEADD(DAY,-@i,SYSUTCDATETIME())
    );

    INSERT INTO PasswordResetTokens(UserID,TokenHash,ExpiresAt,UsedAt,CreatedAt)
    VALUES(
        (SELECT UserID FROM Users WHERE Email=CONCAT('student',FORMAT(@i,'00'),'@roomify.demo')),
        CONCAT('DEMO_RESET_HASH_',FORMAT(@i,'00')),
        DATEADD(HOUR,24+@i,SYSUTCDATETIME()),
        CASE WHEN @i<=3 THEN DATEADD(HOUR,-1,SYSUTCDATETIME()) ELSE NULL END,
        DATEADD(HOUR,-@i,SYSUTCDATETIME())
    );
    SET @i+=1;
END;

-- ============================================================================
-- 4. STUDENT PROFILES + LANDLORD PROFILES: 10 EACH
-- ============================================================================

SET @i=1;
WHILE @i<=10
BEGIN
    DECLARE @UniversityID BIGINT=(
        SELECT UniversityID FROM (
            SELECT UniversityID,ROW_NUMBER() OVER(ORDER BY UniversityID) AS RN
            FROM Universities
        ) X WHERE RN=@i
    );

    INSERT INTO StudentProfiles(UserID,UniversityID,StudentCode,StudentCardURL,
                                IsVerified,VerifiedByUserID,VerifiedAt,CreatedAt,UpdatedAt)
    VALUES(
        (SELECT UserID FROM Users WHERE Email=CONCAT('student',FORMAT(@i,'00'),'@roomify.demo')),
        @UniversityID,
        CONCAT('SV26',FORMAT(@i,'0000')),
        CONCAT(N'https://cdn.roomify.demo/student-cards/student',FORMAT(@i,'00'),N'.jpg'),
        CASE WHEN @i<=7 THEN 1 ELSE 0 END,
        CASE WHEN @i<=7 THEN (SELECT UserID FROM Users WHERE Email='admin01@roomify.demo') ELSE NULL END,
        CASE WHEN @i<=7 THEN DATEADD(DAY,-20-@i,SYSUTCDATETIME()) ELSE NULL END,
        DATEADD(DAY,-50-@i,SYSUTCDATETIME()),SYSUTCDATETIME()
    );

    INSERT INTO LandlordProfiles(UserID,IdentityNumber,IdentityDocumentURL,
                                 IsVerified,VerifiedByUserID,VerifiedAt,CreatedAt,UpdatedAt)
    VALUES(
        (SELECT UserID FROM Users WHERE Email=CONCAT('landlord',FORMAT(@i,'00'),'@roomify.demo')),
        CONCAT('0482026',FORMAT(@i,'00000')),
        CONCAT(N'https://cdn.roomify.demo/identity/landlord',FORMAT(@i,'00'),N'.jpg'),
        CASE WHEN @i<=8 THEN 1 ELSE 0 END,
        CASE WHEN @i<=8 THEN (SELECT UserID FROM Users WHERE Email='admin02@roomify.demo') ELSE NULL END,
        CASE WHEN @i<=8 THEN DATEADD(DAY,-25-@i,SYSUTCDATETIME()) ELSE NULL END,
        DATEADD(DAY,-80-@i,SYSUTCDATETIME()),SYSUTCDATETIME()
    );

    SET @i+=1;
END;

-- ============================================================================
-- 5. PROPERTIES + ROOMS + IMAGES + AMENITIES: 10 EACH
-- ============================================================================

SET @i=1;
WHILE @i<=10
BEGIN
    DECLARE @WardID INT=(
        SELECT WardID FROM (
            SELECT WardID,ROW_NUMBER() OVER(ORDER BY WardID) RN
            FROM Wards WHERE ProvinceID=@DaNangID
        ) X WHERE RN=@i
    );
    DECLARE @PropertyTypeID INT=(
        SELECT PropertyTypeID FROM (
            SELECT PropertyTypeID,ROW_NUMBER() OVER(ORDER BY PropertyTypeID) RN
            FROM PropertyTypes
        ) X WHERE RN=@i
    );

    INSERT INTO Properties(LandlordID,PropertyTypeID,PropertyName,Description,
                           ProvinceID,WardID,AddressLine,Latitude,Longitude,
                           Status,CreatedAt,UpdatedAt)
    VALUES(
        (SELECT LP.LandlordID FROM LandlordProfiles LP JOIN Users U ON U.UserID=LP.UserID
         WHERE U.Email=CONCAT('landlord',FORMAT(@i,'00'),'@roomify.demo')),
        @PropertyTypeID,
        CONCAT(N'Khu trọ Roomify Demo ',FORMAT(@i,'00')),
        N'Khu trọ mẫu dùng để kiểm thử ứng dụng Roomify.',
        @DaNangID,@WardID,
        CONCAT(@i,N' Đường Roomify Demo'),
        CAST(16.0100000+@i*0.003 AS DECIMAL(10,7)),
        CAST(108.1500000+@i*0.006 AS DECIMAL(10,7)),
        'ACTIVE',DATEADD(DAY,-70-@i,SYSUTCDATETIME()),SYSUTCDATETIME()
    );

    DECLARE @PropertyID BIGINT=SCOPE_IDENTITY();

    INSERT INTO Rooms(PropertyID,RoomNumber,Title,Description,AreaM2,RentPrice,
                      DepositAmount,ElectricityPrice,WaterPrice,InternetPrice,
                      ServiceFee,ParkingFee,MaxOccupants,GenderPreference,AvailableFrom,
                      ListingStatus,AvailabilityStatus,ViewCount,ReviewedByUserID,
                      ReviewedAt,PublishedAt,CreatedAt,UpdatedAt)
    VALUES(
        @PropertyID,CONCAT(N'P.',FORMAT(@i,'000')),
        CONCAT(N'Phòng Roomify Demo ',FORMAT(@i,'00')),
        N'Phòng mẫu đầy đủ dữ liệu phục vụ kiểm thử.',
        18+@i*2,1500000+@i*200000,1500000+@i*200000,
        3500,50000,100000,50000,50000,
        CASE WHEN @i=5 THEN 4 ELSE 2 END,
        CASE @i%3 WHEN 1 THEN 'ALL' WHEN 2 THEN 'MALE' ELSE 'FEMALE' END,
        DATEADD(DAY,@i,CAST(SYSUTCDATETIME() AS DATE)),
        'PUBLISHED',CASE WHEN @i<=5 THEN 'RENTED' ELSE 'AVAILABLE' END,
        100*@i+48,
        (SELECT UserID FROM Users WHERE Email='admin01@roomify.demo'),
        DATEADD(DAY,-15-@i,SYSUTCDATETIME()),
        DATEADD(DAY,-14-@i,SYSUTCDATETIME()),
        DATEADD(DAY,-30-@i,SYSUTCDATETIME()),SYSUTCDATETIME()
    );

    DECLARE @RoomID BIGINT=SCOPE_IDENTITY();

    INSERT INTO RoomImages(RoomID,ImageURL,PublicId,IsPrimary,SortOrder,CreatedAt)
    VALUES(@RoomID,
           CONCAT(N'https://cdn.roomify.demo/rooms/room',FORMAT(@i,'00'),N'-primary.jpg'),
           CONCAT(N'roomify/demo/room',FORMAT(@i,'00'),N'/primary'),
           1,0,DATEADD(DAY,-20-@i,SYSUTCDATETIME()));

    DECLARE @AmenityID INT=(
        SELECT AmenityID FROM (
            SELECT AmenityID,ROW_NUMBER() OVER(ORDER BY AmenityID) RN FROM Amenities
        ) X WHERE RN=@i
    );
    INSERT INTO RoomAmenities(RoomID,AmenityID) VALUES(@RoomID,@AmenityID);

    SET @i+=1;
END;

-- ============================================================================
-- 6. SAVED ROOMS / VIEW HISTORY / APPOINTMENTS: 10 EACH
-- ============================================================================

SET @i=1;
WHILE @i<=10
BEGIN
    DECLARE @StudentID BIGINT=(
        SELECT SP.StudentID FROM StudentProfiles SP JOIN Users U ON U.UserID=SP.UserID
        WHERE U.Email=CONCAT('student',FORMAT(@i,'00'),'@roomify.demo')
    );
    DECLARE @RoomID2 BIGINT=(
        SELECT RoomID FROM Rooms WHERE Title=CONCAT(N'Phòng Roomify Demo ',FORMAT(@i,'00'))
    );

    INSERT INTO SavedRooms(StudentID,RoomID,SavedAt)
    VALUES(@StudentID,@RoomID2,DATEADD(DAY,-@i,SYSUTCDATETIME()));

    INSERT INTO RoomViewHistories(StudentID,RoomID,FirstViewedAt,LastViewedAt,ViewCount)
    VALUES(@StudentID,@RoomID2,DATEADD(DAY,-10-@i,SYSUTCDATETIME()),
           DATEADD(DAY,-@i,SYSUTCDATETIME()),@i+1);

    INSERT INTO ViewingAppointments(RoomID,StudentID,ScheduledAt,Note,Status,
                                    LandlordNote,RespondedAt,CreatedAt,UpdatedAt)
    VALUES(@RoomID2,@StudentID,DATEADD(DAY,@i+2,SYSUTCDATETIME()),
           N'Sinh viên muốn xem phòng.',
           CASE @i%5 WHEN 1 THEN 'PENDING' WHEN 2 THEN 'CONFIRMED'
                     WHEN 3 THEN 'COMPLETED' WHEN 4 THEN 'CANCELLED' ELSE 'REJECTED' END,
           CASE WHEN @i%5=1 THEN NULL ELSE N'Chủ trọ đã phản hồi lịch hẹn.' END,
           CASE WHEN @i%5=1 THEN NULL ELSE DATEADD(DAY,-@i,SYSUTCDATETIME()) END,
           DATEADD(DAY,-12-@i,SYSUTCDATETIME()),SYSUTCDATETIME());

    SET @i+=1;
END;

-- ============================================================================
-- 7. RENTAL REQUESTS + CONTRACTS + PAYMENTS: 10 EACH
-- ============================================================================

SET @i=1;
WHILE @i<=10
BEGIN
    DECLARE @StudentID2 BIGINT=(
        SELECT SP.StudentID FROM StudentProfiles SP JOIN Users U ON U.UserID=SP.UserID
        WHERE U.Email=CONCAT('student',FORMAT(@i,'00'),'@roomify.demo')
    );
    DECLARE @RoomID3 BIGINT=(
        SELECT RoomID FROM Rooms WHERE Title=CONCAT(N'Phòng Roomify Demo ',FORMAT(@i,'00'))
    );
    DECLARE @Rent DECIMAL(18,2)=(SELECT RentPrice FROM Rooms WHERE RoomID=@RoomID3);

    INSERT INTO RentalRequests(RoomID,StudentID,RequestedMoveInDate,DurationMonths,
                               Note,Status,LandlordNote,RespondedAt,CreatedAt,UpdatedAt)
    VALUES(@RoomID3,@StudentID2,DATEADD(DAY,-90+@i,CAST(SYSUTCDATETIME() AS DATE)),
           12,N'Yêu cầu thuê phòng mẫu.','APPROVED',N'Đã duyệt yêu cầu thuê.',
           DATEADD(DAY,-85+@i,SYSUTCDATETIME()),
           DATEADD(DAY,-95+@i,SYSUTCDATETIME()),SYSUTCDATETIME());

    DECLARE @RequestID BIGINT=SCOPE_IDENTITY();

    INSERT INTO RentalContracts(RentalRequestID,ContractCode,RoomID,StartDate,EndDate,
                                MonthlyRent,DepositAmount,Terms,ContractFileURL,Status,
                                CreatedAt,ActivatedAt,CompletedAt)
    VALUES(@RequestID,CONCAT('HD-DEMO-',FORMAT(@i,'0000')),@RoomID3,
           DATEADD(DAY,-80+@i,CAST(SYSUTCDATETIME() AS DATE)),
           CASE WHEN @i<=5
                THEN DATEADD(MONTH,12,DATEADD(DAY,-80+@i,CAST(SYSUTCDATETIME() AS DATE)))
                ELSE DATEADD(DAY,-10-@i,CAST(SYSUTCDATETIME() AS DATE)) END,
           @Rent,@Rent,N'Hợp đồng mẫu Roomify.',
           CONCAT(N'https://cdn.roomify.demo/contracts/HD-DEMO-',FORMAT(@i,'0000'),N'.pdf'),
           CASE WHEN @i<=5 THEN 'ACTIVE' ELSE 'COMPLETED' END,
           DATEADD(DAY,-82+@i,SYSUTCDATETIME()),
           DATEADD(DAY,-80+@i,SYSUTCDATETIME()),
           CASE WHEN @i<=5 THEN NULL ELSE DATEADD(DAY,-10-@i,SYSUTCDATETIME()) END);

    DECLARE @ContractID BIGINT=SCOPE_IDENTITY();
    DECLARE @LandlordUserID BIGINT=(
        SELECT U.UserID
        FROM Rooms R
        JOIN Properties P ON P.PropertyID=R.PropertyID
        JOIN LandlordProfiles LP ON LP.LandlordID=P.LandlordID
        JOIN Users U ON U.UserID=LP.UserID
        WHERE R.RoomID=@RoomID3
    );

    INSERT INTO Payments(ContractID,PaymentType,Amount,DueDate,PaymentMethod,Status,
                         TransactionReference,ProofImageURL,Note,ConfirmedByUserID,
                         ConfirmedAt,PaidAt,CreatedAt,UpdatedAt)
    VALUES(@ContractID,
           CASE @i%4 WHEN 1 THEN 'DEPOSIT' WHEN 2 THEN 'RENT'
                     WHEN 3 THEN 'UTILITY' ELSE 'OTHER' END,
           CASE WHEN @i%4 IN (1,2) THEN @Rent WHEN @i%4=3 THEN 350000 ELSE 200000 END,
           DATEADD(DAY,-@i,CAST(SYSUTCDATETIME() AS DATE)),
           CASE WHEN @i%3=0 THEN 'CASH' ELSE 'BANK_TRANSFER' END,
           'PAID',CONCAT('TX-DEMO-',FORMAT(@i,'000000')),
           CONCAT(N'https://cdn.roomify.demo/payment-proofs/payment',FORMAT(@i,'00'),N'.jpg'),
           N'Thanh toán mẫu.',@LandlordUserID,
           DATEADD(DAY,-@i,SYSUTCDATETIME()),
           DATEADD(DAY,-@i,SYSUTCDATETIME()),
           DATEADD(DAY,-@i-1,SYSUTCDATETIME()),SYSUTCDATETIME());

    SET @i+=1;
END;

-- ============================================================================
-- 8. CONVERSATIONS + MESSAGES: 10 EACH
-- ============================================================================

SET @i=1;
WHILE @i<=10
BEGIN
    DECLARE @StudentID3 BIGINT=(
        SELECT SP.StudentID FROM StudentProfiles SP JOIN Users U ON U.UserID=SP.UserID
        WHERE U.Email=CONCAT('student',FORMAT(@i,'00'),'@roomify.demo')
    );
    DECLARE @RoomID4 BIGINT=(
        SELECT RoomID FROM Rooms WHERE Title=CONCAT(N'Phòng Roomify Demo ',FORMAT(@i,'00'))
    );

    INSERT INTO Conversations(RoomID,StudentID,LastMessageAt,CreatedAt)
    VALUES(@RoomID4,@StudentID3,DATEADD(HOUR,-@i,SYSUTCDATETIME()),
           DATEADD(DAY,-5-@i,SYSUTCDATETIME()));

    DECLARE @ConversationID BIGINT=SCOPE_IDENTITY();
    DECLARE @SenderUserID BIGINT;

    IF @i%2=1
        SET @SenderUserID=(SELECT UserID FROM Users WHERE Email=CONCAT('student',FORMAT(@i,'00'),'@roomify.demo'));
    ELSE
        SET @SenderUserID=(
            SELECT U.UserID
            FROM Rooms R JOIN Properties P ON P.PropertyID=R.PropertyID
            JOIN LandlordProfiles LP ON LP.LandlordID=P.LandlordID
            JOIN Users U ON U.UserID=LP.UserID
            WHERE R.RoomID=@RoomID4
        );

    INSERT INTO Messages(ConversationID,SenderUserID,Content,MessageType,IsRead,ReadAt,SentAt)
    VALUES(@ConversationID,@SenderUserID,
           CONCAT(N'Tin nhắn mẫu Roomify ',FORMAT(@i,'00')),
           'TEXT',CASE WHEN @i<=7 THEN 1 ELSE 0 END,
           CASE WHEN @i<=7 THEN DATEADD(MINUTE,-@i,SYSUTCDATETIME()) ELSE NULL END,
           DATEADD(HOUR,-@i,SYSUTCDATETIME()));

    SET @i+=1;
END;

-- ============================================================================
-- 9. REVIEWS + REPORTS: 10 EACH
-- ============================================================================

SET @i=1;
WHILE @i<=10
BEGIN
    DECLARE @ContractID2 BIGINT=(
        SELECT ContractID FROM RentalContracts WHERE ContractCode=CONCAT('HD-DEMO-',FORMAT(@i,'0000'))
    );
    DECLARE @RoomID5 BIGINT=(
        SELECT RoomID FROM Rooms WHERE Title=CONCAT(N'Phòng Roomify Demo ',FORMAT(@i,'00'))
    );

    INSERT INTO Reviews(ContractID,Rating,Comment,Status,ModeratedByUserID,
                        ModeratedAt,CreatedAt,UpdatedAt)
    VALUES(@ContractID2,1+((@i+3)%5),
           CONCAT(N'Đánh giá mẫu Roomify ',FORMAT(@i,'00')),
           CASE WHEN @i>=9 THEN 'HIDDEN' ELSE 'VISIBLE' END,
           CASE WHEN @i>=9 THEN (SELECT UserID FROM Users WHERE Email='admin03@roomify.demo') ELSE NULL END,
           CASE WHEN @i>=9 THEN DATEADD(DAY,-@i,SYSUTCDATETIME()) ELSE NULL END,
           DATEADD(DAY,-15-@i,SYSUTCDATETIME()),SYSUTCDATETIME());

    INSERT INTO Reports(ReporterUserID,RoomID,Reason,Description,Status,AdminNote,
                        ResolvedByUserID,ResolvedAt,CreatedAt)
    VALUES(
        (SELECT UserID FROM Users WHERE Email=CONCAT('student',FORMAT(@i,'00'),'@roomify.demo')),
        @RoomID5,
        CASE @i%5 WHEN 1 THEN N'Thông tin phòng chưa chính xác'
                  WHEN 2 THEN N'Giá phòng không đúng mô tả'
                  WHEN 3 THEN N'Hình ảnh không đúng thực tế'
                  WHEN 4 THEN N'Phòng đã thuê nhưng vẫn hiển thị'
                  ELSE N'Yêu cầu đặt cọc bất thường' END,
        CONCAT(N'Mô tả báo cáo mẫu ',FORMAT(@i,'00')),
        CASE WHEN @i<=4 THEN 'OPEN' WHEN @i<=8 THEN 'RESOLVED' ELSE 'REJECTED' END,
        CASE WHEN @i<=4 THEN NULL ELSE N'Admin đã kiểm tra báo cáo.' END,
        CASE WHEN @i<=4 THEN NULL ELSE (SELECT UserID FROM Users WHERE Email='admin04@roomify.demo') END,
        CASE WHEN @i<=4 THEN NULL ELSE DATEADD(DAY,-@i,SYSUTCDATETIME()) END,
        DATEADD(DAY,-20+@i,SYSUTCDATETIME())
    );

    SET @i+=1;
END;

-- ============================================================================
-- 10. NOTIFICATIONS + DEVICE TOKENS: 10 EACH
-- ============================================================================

SET @i=1;
WHILE @i<=10
BEGIN
    DECLARE @UserID BIGINT=(
        SELECT UserID FROM Users WHERE Email=CONCAT('student',FORMAT(@i,'00'),'@roomify.demo')
    );
    DECLARE @RoomID6 BIGINT=(
        SELECT RoomID FROM Rooms WHERE Title=CONCAT(N'Phòng Roomify Demo ',FORMAT(@i,'00'))
    );

    INSERT INTO Notifications(UserID,NotificationType,Title,Message,RelatedEntityType,
                              RelatedEntityID,IsRead,ReadAt,CreatedAt)
    VALUES(@UserID,'RENTAL',CONCAT(N'Thông báo Roomify ',FORMAT(@i,'00')),
           N'Đây là thông báo mẫu để kiểm thử.',
           'ROOM',@RoomID6,
           CASE WHEN @i<=5 THEN 1 ELSE 0 END,
           CASE WHEN @i<=5 THEN DATEADD(HOUR,-@i,SYSUTCDATETIME()) ELSE NULL END,
           DATEADD(HOUR,-@i-2,SYSUTCDATETIME()));

    INSERT INTO UserDeviceTokens(UserID,DeviceToken,Platform,IsActive,LastSeenAt,CreatedAt)
    VALUES(@UserID,CONCAT('ROOMIFY_DEMO_ANDROID_TOKEN_',FORMAT(@i,'000')),
           'ANDROID',1,DATEADD(MINUTE,-@i*5,SYSUTCDATETIME()),
           DATEADD(DAY,-@i,SYSUTCDATETIME()));

    SET @i+=1;
END;

COMMIT TRANSACTION;

PRINT N'Roomify demo seed completed.';
PRINT N'Demo password for all seeded users: Roomify@123';

END TRY
BEGIN CATCH
    IF @@TRANCOUNT>0 ROLLBACK TRANSACTION;
    THROW;
END CATCH;
GO


/* ============================================================================
   12. BACKEND SYNC VERIFICATION (ROOMIFY.API THROUGH STEP 21)
   ============================================================================ */

-- Cac kiem tra nay giup thanh vien phat hien ngay neu schema local bi thieu
-- cac thanh phan backend hien tai dang su dung.

IF OBJECT_ID('dbo.ViewingAppointmentHistories', 'U') IS NULL
    THROW 51001, 'Missing table dbo.ViewingAppointmentHistories.', 1;

IF COL_LENGTH('dbo.ViewingAppointments', 'ProposedAt') IS NULL
    THROW 51002, 'Missing column ViewingAppointments.ProposedAt.', 1;

IF COL_LENGTH('dbo.ViewingAppointments', 'RowVer') IS NULL
    THROW 51003, 'Missing column ViewingAppointments.RowVer.', 1;

IF COL_LENGTH('dbo.Payments', 'ProofImageURL') IS NULL
    THROW 51004, 'Missing column Payments.ProofImageURL.', 1;

IF COL_LENGTH('dbo.Users', 'LockedByUserID') IS NULL
    THROW 51005, 'Missing Admin lock columns on Users.', 1;

IF COL_LENGTH('dbo.Rooms', 'ReviewedByUserID') IS NULL
    THROW 51006, 'Missing Admin moderation columns on Rooms.', 1;

IF COL_LENGTH('dbo.Reports', 'ResolvedByUserID') IS NULL
    THROW 51007, 'Missing Admin moderation columns on Reports.', 1;

IF NOT EXISTS (
    SELECT 1
    FROM sys.check_constraints
    WHERE name = 'CK_ViewingAppointments_Status'
      AND parent_object_id = OBJECT_ID('dbo.ViewingAppointments')
      AND definition LIKE '%PROPOSED%'
)
    THROW 51008, 'ViewingAppointments status constraint is not synced with PROPOSED workflow.', 1;

IF NOT EXISTS (
    SELECT 1
    FROM sys.indexes
    WHERE name = 'UX_RoomImages_OnePrimaryPerRoom'
      AND object_id = OBJECT_ID('dbo.RoomImages')
)
    THROW 51009, 'Missing UX_RoomImages_OnePrimaryPerRoom.', 1;

IF NOT EXISTS (
    SELECT 1
    FROM sys.indexes
    WHERE name = 'UX_Reports_Reporter_Room_Open'
      AND object_id = OBJECT_ID('dbo.Reports')
)
    THROW 51010, 'Missing UX_Reports_Reporter_Room_Open.', 1;

PRINT N'RoomifyDB sync verification PASSED.';
PRINT N'Demo password for seeded @roomify.demo accounts: Roomify@123';
PRINT N'Backend baseline: Roomify.API through Step 21 (Payments + Admin APIs).';
GO
