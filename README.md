<div align="center">

<h1>📷 PixTrim</h1>
<p><strong>Smart Cleanup. More Memories.</strong></p>

![Android](https://img.shields.io/badge/Platform-Android-3DDC84?style=flat&logo=android&logoColor=white)
![Kotlin](https://img.shields.io/badge/Language-Kotlin-7F52FF?style=flat&logo=kotlin&logoColor=white)
![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack%20Compose-4285F4?style=flat&logo=jetpackcompose&logoColor=white)
![Min SDK](https://img.shields.io/badge/Min%20SDK-24-brightgreen?style=flat)
![License](https://img.shields.io/badge/License-MIT-blue?style=flat)
![Status](https://img.shields.io/badge/Status-Active-success?style=flat)

<p>An intelligent Android application that declutters your gallery using on-device AI — no cloud, no compromise.</p>

</div>

---

## ✨ Features

| Feature | Description |
|---|---|
| 🔍 **Similar Photo Detection** | Detects visually duplicate images using dHash (Difference Hash) algorithm |
| 📸 **Blur Detection** | Identifies blurry photos using Laplacian variance scoring |
| 💡 **Smart Suggestions** | Recommends the best photo per group based on clarity and resolution |
| 🗑️ **Secure Cleanup** | Safe deletion via Android MediaStore API with Scoped Storage compliance |
| ⚡ **On-Device Processing** | All analysis runs locally — zero internet required, zero data exposure |
| 📊 **Storage Insights** | Preview exactly how much storage can be freed before committing to deletion |

---

## 🛠️ Tech Stack

| Category | Technology |
|---|---|
| **Language** | Kotlin |
| **UI** | Jetpack Compose |
| **Architecture** | MVVM |
| **Async** | Kotlin Coroutines & Flow |
| **Database** | Room |
| **Image Loading** | Coil |
| **Image Analysis** | dHash · Laplacian Variance |
| **Storage API** | Android MediaStore (Scoped Storage) |
| **Build** | Gradle with Version Catalog |

---

## 🏗️ Architecture

```
com.pixtrim/
├── ui/
│   ├── screens/         # Jetpack Compose screens
│   └── viewmodel/       # ViewModels (state holders)
├── domain/
│   └── model/           # Photo, PhotoGroup (business models)
├── data/
│   ├── repository/      # Single source of truth
│   ├── local/           # Room database + DAOs
│   └── source/          # MediaStore queries
└── utils/
    └── HashUtils.kt     # dHash + Laplacian blur detection
```

**Data flow:** `MediaStore → Repository → ViewModel → Compose UI`

---

## 🚀 Getting Started

### Prerequisites

- Android Studio (latest stable)
- Min SDK: **24** · Target SDK: **36**
- Kotlin 1.9+

### Installation

```bash
git clone https://github.com/chaudharyy7/PixTrim.git
cd PixTrim
```

1. Open in **Android Studio**
2. Let Gradle sync complete
3. Run on emulator or physical device (API 24+)

> ⚠️ On Android 13+, grant `READ_MEDIA_IMAGES` permission when prompted. On Android 11–12, `READ_EXTERNAL_STORAGE` is required.

---

## 🛡️ Privacy

PixTrim is built privacy-first. Everything happens on your device.

- ❌ No data collection
- ❌ No cloud uploads
- ❌ No analytics or tracking
- ✅ Works fully offline
- ✅ Scoped Storage compliant

---

## 🗺️ Roadmap

- [ ] Video duplicate detection
- [ ] Screenshot-specific cleanup mode
- [ ] Batch undo support
- [ ] Widget for quick cleanup stats
- [ ] Dark mode refinements

---

## 🤝 Contributing

Contributions are welcome! Please:

1. Fork the repository
2. Create your branch: `git checkout -b feature/your-feature`
3. Commit your changes: `git commit -m 'Add some feature'`
4. Push to the branch: `git push origin feature/your-feature`
5. Open a Pull Request

---

## 👨‍💻 Developer

**Vimal Chaudhary** — Android Developer

[![Email](https://img.shields.io/badge/Email-vimal07chaudhary%40gmail.com-EA4335?style=flat&logo=gmail&logoColor=white)](mailto:vimal07chaudhary@gmail.com)
[![GitHub](https://img.shields.io/badge/GitHub-chaudharyy7-181717?style=flat&logo=github&logoColor=white)](https://github.com/chaudharyy7)

---

## ⭐ Support

If PixTrim helped you reclaim storage space, consider:

- ⭐ **Starring** this repo
- 🔁 **Sharing** with other Android developers
- 🐛 **Reporting** bugs via Issues
- 💡 **Suggesting** features via Discussions

---

<div align="center">
<sub>Built with ❤️ by <a href="https://github.com/chaudharyy7">Vimal Chaudhary</a> · All processing is local · Your photos never leave your device</sub>
</div>
