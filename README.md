# 📱 Protocol Decoder – Android App
## Overview
**Protocol Decoder** is a beginner-friendly Android application built using **Jetpack Compose**.  
It allows users to input a **custom protocol hex frame**, decode it step by step, and validate it using **Checksum** and **CRC-16 (CCITT)**.  

This project is designed as an educational tool for students, developers, and embedded engineers who want to understand how protocol parsing and validation work inside Android apps.

---

## 🔑 Features
- Input hex frame manually or auto-fill with an **example frame**.
- Step-by-step decoding with **four simple actions**:
  1. **Paste Example** → Auto-fills the sample frame.  
  2. **Parse Header** → Extracts Device ID, Command, and Length.  
  3. **Show Payload** → Displays the payload data.  
  4. **Validate Frame** → Validates Checksum & CRC, shows VALID/INVALID.  
- Results are displayed in a **clean card-based UI**.  
- Implemented with **Material3 + Jetpack Compose** for a modern, user-friendly interface.  

---
