# 🏥 SGPA - Système de Gestion de Pharmacie

SGPA (Système de Gestion de Pharmacie) is a comprehensive JavaFX application designed to manage the daily operations of a pharmacy, including stock inventory, sales, supplier orders, and administrative statistics.

---

## 📂 Project Structure & Documentation

We have organized the project documentation and database resources in a specific folder for easy access:

### 📁 **`Others/`**
Inside this folder, you will find:
* **📄 `script.sql`**: The complete MySQL script to create the database and tables (`utilisateur`, `medicament`, `vente`, `logs`, etc.).
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
* **MySQL Server** (Running on port 3306)

### 2. Database Setup
1.  Open your MySQL client (phpMyAdmin, Workbench, or Terminal).
2.  Create a new database named `sgpa_db` (or as specified in the script).
3.  Import the SQL script located at:
    > **`Others/database_script.sql`**
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
* **truck: Supplier Orders:** Manage suppliers and create restock orders.
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
