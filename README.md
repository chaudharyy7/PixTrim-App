# 🚀 PixTrim – Smart Cleanup, More Memories

PixTrim is an intelligent Android application designed to help you declutter your gallery by identifying and removing similar or blurry photos. Using on-device AI-driven analysis, it ensures your best memories stay while duplicates and low-quality images are removed to save storage.

---

## ✨ Features

- 🔍 **Similar Photo Detection**  
  Detects visually similar or duplicate images using dHash (Difference Hash)

- 📸 **Blur Detection**  
  Identifies blurry or low-quality photos using Laplacian variance

- 💡 **Smart Suggestions**  
  Recommends which photos to keep based on clarity and resolution

- 🗑️ **Secure Cleanup (Scoped Storage)**  
  Safely deletes images using Android MediaStore API

- ⚡ **Fast Local Processing**  
  All processing happens on-device (no internet required)

- 📊 **Storage Insights**  
  Displays how much storage space can be freed

---

## 🛠️ Tech Stack

- **Language:** Kotlin  
- **UI:** Jetpack Compose  
- **Database:** Room  
- **Async:** Kotlin Coroutines & Flow  
- **Image Loading:** Coil  
- **Architecture:** MVVM  
- **Build System:** Gradle (Version Catalog)

---

## 🏗️ Architecture

- **UI Layer:** Compose screens + ViewModel  
- **Domain Layer:** Models (Photo, PhotoGroup)  
- **Data Layer:** Repository + Room + MediaStore  
- **Utils:** HashUtils for similarity & blur detection  

---

## 🚀 Getting Started

### 📌 Prerequisites
- Android Studio (latest version)
- Minimum SDK: 24  
- Target SDK: 36  

---

### ⚙️ Installation

```bash
git clone https://github.com/your-username/PixTrim.git
cd PixTrim

```

- Open in Android Studio  
- Sync Gradle  
- Run on emulator or physical device  

---

## 🛡️ Privacy

PixTrim processes all photos locally on your device.

- ❌ No data collection  
- ❌ No cloud upload  
- ✅ Fully private & secure  

---

## 👨‍💻 Developer

**Vimal Chaudhary**  
Android Developer  

- 📧 vimal07chaudhary@gmail.com  
- 🔗 https://github.com/chaudharyy7  

---

## ⭐ Support

If you like this project:

- ⭐ Star the repo  
- 🔁 Share it  
- 🛠️ Contribute  


---

# 🔥 Done — Just Copy & Paste into README.md

---

If you want next level 🚀  
I can:

👉 Add **badges (stars, downloads, tech icons)**  
👉 Add **screenshots section (very important for recruiters)**  
👉 Make it look like **top GitHub repo 💯**
