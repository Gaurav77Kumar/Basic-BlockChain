# ⛓️ Java-Blockchain

<p align="center">
  <img src="https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=java&logoColor=white" />
  <img src="https://img.shields.io/badge/GSON-2.10.1-47A248?style=for-the-badge" />
  <img src="https://img.shields.io/badge/Blockchain-Security-blue?style=for-the-badge" />
</p>

---

A lightweight, secure implementation of a blockchain structure in Java. This project demonstrates how digital ledgers maintain integrity using cryptographic hashing and Proof of Work (Mining).

---

### 🌟 Features
* **SHA-256 Hashing:** Generates unique digital fingerprints for every block.
* **Proof of Work (Mining):** Implements a `nonce` system with adjustable difficulty to simulate real mining.
* **Tamper Protection:** Built-in validation logic to detect any unauthorized changes to the chain.
* **JSON Serialization:** Integration with the **GSON library** for beautiful, readable data output.

---

### 📋 Prerequisites
Before running this project, ensure you have:
* **Java Development Kit (JDK):** Version 8 or higher.
* **IntelliJ IDEA:** (Or any Java IDE).
* **GSON Library:** You must add the GSON `.jar` to your project dependencies to handle JSON formatting.

---

### ⚙️ How It Works
The security of this blockchain relies on three main pillars:

1.  **The Hash:** Each block creates a hash based on its `Data`, `Timestamp`, `Nonce`, and the `Previous Hash`.
2.  **The Chain:** Because Block B contains the hash of Block A, they are mathematically "linked." If Block A's data is changed, its hash changes, and the entire chain becomes invalid.
3.  **The Mine:** To add a block, the computer must solve a "puzzle"—finding a hash that starts with a specific number of `0`s (defined by the `difficulty` variable).



---

### 🚀 How to Run

1.  **Clone the Repository:**
    ```bash
    git clone https://github.com/Gaurav77Kumar/Basic-BlockChain
    ```
2.  **Import to IntelliJ:** Open the folder as a new project.
3.  **Add GSON:** * Go to `File` > `Project Structure` > `Libraries`.
    * Add the GSON library (Maven: `com.google.code.gson:gson:2.10.1`).
4.  **Execute:** Run the `Noob.java` file.

---

