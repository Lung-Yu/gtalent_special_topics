-- ========================================
-- Northwind Database Schema for MySQL
-- 北風資料庫綱要 - 用於學習 SQL 的完整範例資料庫
-- ========================================

USE northwind;

-- 設置字符集
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ========================================
-- Categories 表 - 產品類別
-- ========================================
DROP TABLE IF EXISTS Categories;
CREATE TABLE Categories (
    CategoryID INT NOT NULL AUTO_INCREMENT,
    CategoryName VARCHAR(25) NOT NULL,
    Description TEXT,
    PRIMARY KEY (CategoryID),
    KEY idx_CategoryName (CategoryName)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ========================================
-- Suppliers 表 - 供應商
-- ========================================
DROP TABLE IF EXISTS Suppliers;
CREATE TABLE Suppliers (
    SupplierID INT NOT NULL AUTO_INCREMENT,
    CompanyName VARCHAR(40) NOT NULL,
    ContactName VARCHAR(30),
    ContactTitle VARCHAR(30),
    Address VARCHAR(60),
    City VARCHAR(15),
    Region VARCHAR(15),
    PostalCode VARCHAR(10),
    Country VARCHAR(15),
    Phone VARCHAR(24),
    Fax VARCHAR(24),
    HomePage TEXT,
    PRIMARY KEY (SupplierID),
    KEY idx_CompanyName (CompanyName),
    KEY idx_PostalCode (PostalCode)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ========================================
-- Products 表 - 產品
-- ========================================
DROP TABLE IF EXISTS Products;
CREATE TABLE Products (
    ProductID INT NOT NULL AUTO_INCREMENT,
    ProductName VARCHAR(40) NOT NULL,
    SupplierID INT,
    CategoryID INT,
    QuantityPerUnit VARCHAR(20),
    UnitPrice DECIMAL(10,2) DEFAULT 0,
    UnitsInStock SMALLINT DEFAULT 0,
    UnitsOnOrder SMALLINT DEFAULT 0,
    ReorderLevel SMALLINT DEFAULT 0,
    Discontinued BOOLEAN NOT NULL DEFAULT 0,
    PRIMARY KEY (ProductID),
    KEY idx_ProductName (ProductName),
    KEY idx_CategoryID (CategoryID),
    KEY idx_SupplierID (SupplierID),
    CONSTRAINT fk_Products_Categories FOREIGN KEY (CategoryID) 
        REFERENCES Categories(CategoryID) ON DELETE SET NULL ON UPDATE CASCADE,
    CONSTRAINT fk_Products_Suppliers FOREIGN KEY (SupplierID) 
        REFERENCES Suppliers(SupplierID) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ========================================
-- Customers 表 - 客戶
-- ========================================
DROP TABLE IF EXISTS Customers;
CREATE TABLE Customers (
    CustomerID VARCHAR(5) NOT NULL,
    CompanyName VARCHAR(40) NOT NULL,
    ContactName VARCHAR(30),
    ContactTitle VARCHAR(30),
    Address VARCHAR(60),
    City VARCHAR(15),
    Region VARCHAR(15),
    PostalCode VARCHAR(10),
    Country VARCHAR(15),
    Phone VARCHAR(24),
    Fax VARCHAR(24),
    PRIMARY KEY (CustomerID),
    KEY idx_City (City),
    KEY idx_CompanyName (CompanyName),
    KEY idx_PostalCode (PostalCode),
    KEY idx_Region (Region)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ========================================
-- Shippers 表 - 物流公司
-- ========================================
DROP TABLE IF EXISTS Shippers;
CREATE TABLE Shippers (
    ShipperID INT NOT NULL AUTO_INCREMENT,
    CompanyName VARCHAR(40) NOT NULL,
    Phone VARCHAR(24),
    PRIMARY KEY (ShipperID)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ========================================
-- Employees 表 - 員工（包含自聯結層級關係）
-- ========================================
DROP TABLE IF EXISTS Employees;
CREATE TABLE Employees (
    EmployeeID INT NOT NULL AUTO_INCREMENT,
    LastName VARCHAR(20) NOT NULL,
    FirstName VARCHAR(10) NOT NULL,
    Title VARCHAR(30),
    TitleOfCourtesy VARCHAR(25),
    BirthDate DATE,
    HireDate DATE,
    Address VARCHAR(60),
    City VARCHAR(15),
    Region VARCHAR(15),
    PostalCode VARCHAR(10),
    Country VARCHAR(15),
    HomePhone VARCHAR(24),
    Extension VARCHAR(4),
    Photo BLOB,
    Notes TEXT,
    ReportsTo INT,
    PhotoPath VARCHAR(255),
    Salary DECIMAL(10,2),
    PRIMARY KEY (EmployeeID),
    KEY idx_LastName (LastName),
    KEY idx_PostalCode (PostalCode),
    KEY idx_ReportsTo (ReportsTo),
    CONSTRAINT fk_Employees_Employees FOREIGN KEY (ReportsTo) 
        REFERENCES Employees(EmployeeID) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ========================================
-- Orders 表 - 訂單
-- ========================================
DROP TABLE IF EXISTS Orders;
CREATE TABLE Orders (
    OrderID INT NOT NULL AUTO_INCREMENT,
    CustomerID VARCHAR(5),
    EmployeeID INT,
    OrderDate DATETIME,
    RequiredDate DATETIME,
    ShippedDate DATETIME,
    ShipVia INT,
    Freight DECIMAL(10,2) DEFAULT 0,
    ShipName VARCHAR(40),
    ShipAddress VARCHAR(60),
    ShipCity VARCHAR(15),
    ShipRegion VARCHAR(15),
    ShipPostalCode VARCHAR(10),
    ShipCountry VARCHAR(15),
    PRIMARY KEY (OrderID),
    KEY idx_OrderDate (OrderDate),
    KEY idx_ShippedDate (ShippedDate),
    KEY idx_ShipPostalCode (ShipPostalCode),
    KEY idx_CustomerID (CustomerID),
    KEY idx_EmployeeID (EmployeeID),
    KEY idx_ShipVia (ShipVia),
    CONSTRAINT fk_Orders_Customers FOREIGN KEY (CustomerID) 
        REFERENCES Customers(CustomerID) ON DELETE SET NULL ON UPDATE CASCADE,
    CONSTRAINT fk_Orders_Employees FOREIGN KEY (EmployeeID) 
        REFERENCES Employees(EmployeeID) ON DELETE SET NULL ON UPDATE CASCADE,
    CONSTRAINT fk_Orders_Shippers FOREIGN KEY (ShipVia) 
        REFERENCES Shippers(ShipperID) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ========================================
-- OrderDetails 表 - 訂單明細（多對多關聯表）
-- ========================================
DROP TABLE IF EXISTS OrderDetails;
CREATE TABLE OrderDetails (
    OrderID INT NOT NULL,
    ProductID INT NOT NULL,
    UnitPrice DECIMAL(10,2) NOT NULL DEFAULT 0,
    Quantity SMALLINT NOT NULL DEFAULT 1,
    Discount DECIMAL(4,2) NOT NULL DEFAULT 0,
    PRIMARY KEY (OrderID, ProductID),
    KEY idx_OrderID (OrderID),
    KEY idx_ProductID (ProductID),
    CONSTRAINT fk_OrderDetails_Orders FOREIGN KEY (OrderID) 
        REFERENCES Orders(OrderID) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_OrderDetails_Products FOREIGN KEY (ProductID) 
        REFERENCES Products(ProductID) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ========================================
-- Region 表 - 地區
-- ========================================
DROP TABLE IF EXISTS Region;
CREATE TABLE Region (
    RegionID INT NOT NULL,
    RegionDescription VARCHAR(50) NOT NULL,
    PRIMARY KEY (RegionID)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ========================================
-- Territories 表 - 銷售區域
-- ========================================
DROP TABLE IF EXISTS Territories;
CREATE TABLE Territories (
    TerritoryID VARCHAR(20) NOT NULL,
    TerritoryDescription VARCHAR(50) NOT NULL,
    RegionID INT NOT NULL,
    PRIMARY KEY (TerritoryID),
    KEY idx_RegionID (RegionID),
    CONSTRAINT fk_Territories_Region FOREIGN KEY (RegionID) 
        REFERENCES Region(RegionID) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ========================================
-- EmployeeTerritories 表 - 員工與銷售區域的多對多關聯
-- ========================================
DROP TABLE IF EXISTS EmployeeTerritories;
CREATE TABLE EmployeeTerritories (
    EmployeeID INT NOT NULL,
    TerritoryID VARCHAR(20) NOT NULL,
    PRIMARY KEY (EmployeeID, TerritoryID),
    KEY idx_TerritoryID (TerritoryID),
    CONSTRAINT fk_EmployeeTerritories_Employees FOREIGN KEY (EmployeeID) 
        REFERENCES Employees(EmployeeID) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_EmployeeTerritories_Territories FOREIGN KEY (TerritoryID) 
        REFERENCES Territories(TerritoryID) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ========================================
-- CustomerDemographics 表 - 客戶類型
-- ========================================
DROP TABLE IF EXISTS CustomerDemographics;
CREATE TABLE CustomerDemographics (
    CustomerTypeID VARCHAR(10) NOT NULL,
    CustomerDesc TEXT,
    PRIMARY KEY (CustomerTypeID)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ========================================
-- CustomerCustomerDemo 表 - 客戶與客戶類型的多對多關聯
-- ========================================
DROP TABLE IF EXISTS CustomerCustomerDemo;
CREATE TABLE CustomerCustomerDemo (
    CustomerID VARCHAR(5) NOT NULL,
    CustomerTypeID VARCHAR(10) NOT NULL,
    PRIMARY KEY (CustomerID, CustomerTypeID),
    KEY idx_CustomerTypeID (CustomerTypeID),
    CONSTRAINT fk_CustomerCustomerDemo_Customers FOREIGN KEY (CustomerID) 
        REFERENCES Customers(CustomerID) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_CustomerCustomerDemo_CustomerDemographics FOREIGN KEY (CustomerTypeID) 
        REFERENCES CustomerDemographics(CustomerTypeID) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ========================================
-- PerformanceTest 表 - 用於索引效能測試的專用表
-- ========================================
DROP TABLE IF EXISTS PerformanceTest;
CREATE TABLE PerformanceTest (
    id INT NOT NULL AUTO_INCREMENT,
    indexed_col INT,
    non_indexed_col INT,
    varchar_col VARCHAR(255),
    text_col TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_indexed_col (indexed_col)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 恢復外鍵檢查
SET FOREIGN_KEY_CHECKS = 1;

-- ========================================
-- 創建一些有用的視圖 (Views)
-- ========================================

-- 視圖：產品詳細資訊（包含類別和供應商）
CREATE OR REPLACE VIEW ProductDetails AS
SELECT 
    p.ProductID,
    p.ProductName,
    c.CategoryName,
    s.CompanyName AS SupplierName,
    p.UnitPrice,
    p.UnitsInStock,
    p.Discontinued
FROM Products p
LEFT JOIN Categories c ON p.CategoryID = c.CategoryID
LEFT JOIN Suppliers s ON p.SupplierID = s.SupplierID;

-- 視圖：訂單摘要（包含客戶和員工資訊）
CREATE OR REPLACE VIEW OrderSummary AS
SELECT 
    o.OrderID,
    o.OrderDate,
    c.CompanyName AS CustomerName,
    CONCAT(e.FirstName, ' ', e.LastName) AS EmployeeName,
    o.ShipCountry,
    o.Freight
FROM Orders o
LEFT JOIN Customers c ON o.CustomerID = c.CustomerID
LEFT JOIN Employees e ON o.EmployeeID = e.EmployeeID;

-- 視圖：銷售額統計
CREATE OR REPLACE VIEW SalesStatistics AS
SELECT 
    od.OrderID,
    SUM(od.Quantity * od.UnitPrice * (1 - od.Discount)) AS TotalAmount,
    SUM(od.Quantity) AS TotalQuantity,
    COUNT(od.ProductID) AS ProductCount
FROM OrderDetails od
GROUP BY od.OrderID;

DELIMITER //

-- 存儲過程：根據類別查詢產品
CREATE PROCEDURE GetProductsByCategory(IN catID INT)
BEGIN
    SELECT 
        p.ProductID,
        p.ProductName,
        p.UnitPrice,
        p.UnitsInStock
    FROM Products p
    WHERE p.CategoryID = catID
    ORDER BY p.ProductName;
END //

-- 存儲過程：查詢員工的銷售業績
CREATE PROCEDURE GetEmployeeSales(IN empID INT, IN startDate DATE, IN endDate DATE)
BEGIN
    SELECT 
        e.EmployeeID,
        CONCAT(e.FirstName, ' ', e.LastName) AS EmployeeName,
        COUNT(DISTINCT o.OrderID) AS TotalOrders,
        SUM(od.Quantity * od.UnitPrice * (1 - od.Discount)) AS TotalSales
    FROM Employees e
    LEFT JOIN Orders o ON e.EmployeeID = o.EmployeeID 
        AND o.OrderDate BETWEEN startDate AND endDate
    LEFT JOIN OrderDetails od ON o.OrderID = od.OrderID
    WHERE e.EmployeeID = empID
    GROUP BY e.EmployeeID, e.FirstName, e.LastName;
END //

DELIMITER ;

-- ========================================
-- Schema 創建完成
-- ========================================
SELECT 'Northwind Schema Created Successfully!' AS Status;
