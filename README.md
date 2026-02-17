# 🏥 SGPA - Système de Gestion de Pharmacie

SGPA (Système de Gestion de Pharmacie) is a comprehensive JavaFX application designed to manage the daily operations of a pharmacy, including stock inventory, sales, supplier orders, and administrative statistics.

---

## 📂 Project Structure & Documentation

We have organized the project documentation and database resources in a specific folder for easy access:

### 📁 **`Others/`**
Inside this folder, you will find:
* **📄 SQL Scripts**: Two versions of the database creation script are provided to match your server version:
    * **`script8.sql`**: For MySQL 8.0.0 and newer (recommended).
    * **`script57.sql`**: Legacy script for MySQL 5.7.0.
* **📊 Diagrams**: All technical documentation including:
    * Class Diagrams (UML)
    * Use Case Diagrams (General & Specific)
    * Sequence Diagrams
    * Entity-Relationship Diagram (ERD)

---

## 🚀 How to Run the Project

### 1. Prerequisites
* **Java JDK 17** (or higher)
* **Maven**
* **MySQL Server** (MAMP, XAMPP, or standalone)

### 2. Database Setup
1.  Open your MySQL client (phpMyAdmin, Workbench, or Terminal).
2.  Create a new database named `sgpa_pharmacy`.
3.  **Import the correct SQL script** from the `Others/` folder:
    * Use **`script_mysql8.sql`** if you are running MySQL 8.0+.
    * Use **`script_mysql57.sql`** if you are running MySQL 5.7.
4.  **Important:** Check the `DatabaseConnection.java` file in the source code to ensure the username and password match your local MySQL configuration.

### 3. Launching the Application
To start the application, navigate to the source package and run the **Launcher** class.

**Path:**
`src/main/java/com/pharmacy/sgpa/Launcher.java`

* **From IDE (IntelliJ/Eclipse):**
    Right-click on `Launcher.java` and select **"Run 'Launcher.main()'"**.

* **From Terminal (Maven):**
    ```bash
    mvn clean javafx:run
    ```

---

## ✨ Key Features

* **🔐 Authentication:** Secure login for Pharmacists and Administrators.
* **📦 Stock Management:** Real-time inventory tracking with "Critical Stock" and "Expiry" alerts.
* **💊 Point of Sale (POS):** Process sales, print tickets, and handle prescriptions.
* **🚚 Supplier Orders:** Manage suppliers and create restock orders.
* **📈 Dashboard:** Visual KPI charts for sales, revenue, and pending orders.
* **🛡️ Administration:**
    * User Management (Create/Delete profiles).
    * **System Logs:** Track all user activities (Admin only).
    * PDF & Excel Exports.

---

## 🛠️ Technologies Used
* **JavaFX:** UI Framework.
* **MySQL:** Database.
* **JDBC:** Database Connectivity.
* **iText PDF:** Report Generation.
* **Apache POI:** Excel Export.
* **PlantUML:** Documentation Diagrams.

---

**Developed for the SGPA Project.**
