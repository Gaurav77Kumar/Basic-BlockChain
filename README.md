# ⛓️ Java-Blockchain — Bitcoin-Style UTXO Engine

<p align="center">
  <img src="https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white" />
  <img src="https://img.shields.io/badge/BouncyCastle-ECDSA-orange?style=for-the-badge" />
  <img src="https://img.shields.io/badge/GSON-2.10.1-47A248?style=for-the-badge" />
  <img src="https://img.shields.io/badge/JUnit-5.10-25A162?style=for-the-badge&logo=junit5&logoColor=white" />
  <img src="https://img.shields.io/badge/Proof--of--Work-SHA--256-blue?style=for-the-badge" />
</p>

---

## 📌 Overview

A production-grade Bitcoin-style blockchain engine built from scratch in Java, implementing the complete UTXO transaction lifecycle — from coinbase issuance through mempool staging, PoW mining, and full chain validation. Replicates real-world consensus mechanics including dynamic difficulty retargeting, Merkle root integrity, and ECDSA cryptographic signing.

| Feature | Status |
|---|---|
| UTXO Transaction Model | ✅ |
| ECDSA Wallets (BouncyCastle) | ✅ |
| Proof-of-Work Mining | ✅ |
| Mempool (Transaction Staging) | ✅ |
| Coinbase Transaction + Mining Reward | ✅ |
| Dynamic Difficulty Retargeting | ✅ |
| Merkle Root Integrity | ✅ |
| Full Chain Validation | ✅ |
| Swing GUI Dashboard | ✅ |


---

## 🧠 Core Concepts

### 🔐 Wallets & Cryptography
- ECDSA key pair generation via BouncyCastle (`prime192v1` curve)
- Private key signing + public key verification per transaction
- Base64 key encoding via `StringUtil` crypto layer

### 💰 UTXO Model
- Full Bitcoin-style Unspent Transaction Output model
- Dynamic balance calculated by scanning UTXO map — no stored balance
- Double-spend prevention: spent inputs removed from global UTXO set immediately
- Input-output equality enforced on every `processTransaction()` call
- Change outputs automatically returned to sender

### ⛏️ Full Bitcoin Transaction Lifecycle
```
Coinbase TX (miner reward)
    ↓
Wallet.sendFunds() → Mempool (pending)
    ↓
mineNextBlock() → Block (coinbase first, then mempool TXs)
    ↓
PoW solved → Block appended to chain → UTXO set updated
```

### 🏦 Mempool
- Transactions submitted via `sendFunds()` enter a global pending pool
- Miner drains the full mempool into each new block
- Mempool cleared atomically after successful mine — no double-processing

### 🪙 Coinbase Transaction
- First transaction in every mined block — no inputs, creates coins
- Miner wallet receives `miningReward` coins per block
- Only legal mechanism for new coin issuance — prevents arbitrary coin creation
- Skips signature verification (no sender by design)

### 🎯 Dynamic Difficulty Retargeting
- Difficulty stored **per block** at mine time (not globally — fixes validation bug)
- Every `retargetInterval` blocks: compares actual vs target block time
- Increases difficulty if mining faster than target, decreases if slower
- Minimum difficulty floor of 1 enforced

### 🌳 Merkle Root
- Merkle root computed from all transaction IDs in a block
- Stored in block header and included in `calculateHash()`
- Updated on every `addTransaction()` call
- Any tampered transaction invalidates the root — detected by `isChainValid()`

### 🔎 Full Chain Validation
- Hash chain integrity: each block's `previousHash` must match prior block's `hash`
- Per-block hash recalculation with correct stored difficulty
- ECDSA signature verification on every non-coinbase transaction
- UTXO reference validation: inputs must exist and match stored values
- Coinbase transactions skipped correctly (index 0, null sender)

---

## 🏗️ Architecture

```
Wallet → sendFunds() → Mempool → mineNextBlock() → Block (Coinbase + TXs) → Chain → isChainValid()
```

### 6-Layer Design

```
             ┌────────────────────┐
             │    Dashboard GUI   │  Swing dashboard — live balances,
             └─────────▲──────────┘  mempool count, difficulty, block height
                       │
             ┌─────────┴──────────┐
             │      Noob.java     │  App layer — blockchain state, mempool,
             └─────────▲──────────┘  mineNextBlock(), isChainValid(), difficulty retarget
                       │
      ┌────────────────┼────────────────┐
      │                │                │
 ┌────┴────┐   ┌───────┴──────┐   ┌────┴────┐
 │ Wallet  │   │ Transaction  │   │  Block  │
 │         │   │  + Mempool   │   │+ Merkle │
 └────▲────┘   └──────▲───────┘   └────▲────┘
      │               │                │
      └───────────────┴────────────────┘
                       │
               ┌───────┴────────┐
               │   StringUtil   │
               │  SHA-256       │
               │  ECDSA Sign    │
               │  Base64 Encode │
               └────────────────┘
```

---

## 📦 Project Structure

```
src/
├── Noob.java              ← Main class: blockchain state, mempool, mining, validation
├── Block.java             ← Block: hash chain, nonce, merkle root, per-block difficulty
├── Transaction.java       ← UTXO TX: inputs, outputs, signing, merkle tree
├── TransactionInput.java  ← UTXO input reference
├── TransactionOutput.java ← UTXO output: recipient, value, ID
├── Wallet.java            ← ECDSA keypair, balance scan, sendFunds()
├── StringUtil.java        ← SHA-256, ECDSA sign/verify, Base64
└── Dashboard.java         ← Swing GUI: live stats, send, mine, validate
```

---


---

## ⚙️ Requirements

- Java JDK 17+
- GSON 2.10.1
- BouncyCastle `bcprov-jdk15on-1.70`
- JUnit Jupiter 5.10.0 (test scope)



---

## 🚀 How to Run

```bash
# 1. Clone
git clone https://github.com/Gaurav77Kumar/Basic-BlockChain
cd Basic-BlockChain

# 2. Add dependencies via Maven or IntelliJ Project Structure → Libraries

# 3. Run blockchain simulation
# Execute Noob.java — opens Dashboard GUI + prints full lifecycle to console

# 4. Run tests
mvn test
# or right-click BlockchainTest.java → Run in IntelliJ
```

---

## 📸 Output

<p align="center">
  <img src="assets/console-ouput.png" width="800" alt="Console output showing full blockchain lifecycle"/>
</p>
<p align="center">
  <img src="assets/output.png" width="800" alt="Swing dashboard with live wallet balances and controls"/>
</p>
