# JainConnect: Full-Stack Community App

JainConnect is a complete full-stack system built to serve the Jain community. It features a native Android app (Kotlin/MVVM), a RESTful API backend (Node.js/Express/MongoDB), and a React.js admin panel. The application serves as a central hub for tithis, events, and Maharaj locations, with user authentication and push notifications.

JainConnect is a full-stack mobile application built to be the central digital hub for the Jain community. Its purpose is to solve the problem of scattered information by unifying three key pillars of community life into one modern, reliable, and easy-to-use platform.

Key Features
Connect to Faith: It provides a clear, searchable, and filterable calendar of all significant Tithis (religious dates). This ensures users can easily track and observe important spiritual days.

Engage with Community: It features a dynamic directory of community Events. Users can browse, search, and filter gatherings in their area. A new RSVP function allows users to register their interest, helping organizers and fostering a sense of community.

Follow Spiritual Guidance: It includes a Maharaj Locator to help devotees find the current locations and information of their spiritual leaders, bridging the gap between devotees and their gurus.

Core Purpose
Ultimately, JainConnect uses modern technology—like secure user accounts and instant push notifications—to strengthen community bonds, foster spiritual engagement, and make essential information accessible to everyone, anywhere.

---

## 🚀 Core Features

### 📱 Android App (Kotlin)
- **Secure Auth**: JWT-based Registration & Login (with SharedPreferences persistence).
- **Real-time Alerts**: Firebase Cloud Messaging (FCM) for push notifications.
- **Dynamic Lists**: View Tithis, Events, and Maharajs in RecyclerViews.
- **Interactive Filtering**: Client-side search and date-range filtering.
- **User Engagement**: RSVP button for events.
- **Profile Management**: View and update user profile, including image uploads to Cloudinary.

### 🧠 Backend (Node.js & Express)
- **Secure API**: RESTful API protected by JWT middleware (`authenticateToken`).
- **Password Security**: bcrypt.js for all password hashing.
- **Cloud Storage**: multer & Cloudinary for handling all image uploads.
- **Notification Service**: Firebase Admin SDK to send push notifications from the server.
- **Data Modeling**: Mongoose schemas for Users, Tithis, Events, and Maharajs.

### 👑 Admin Panel (React.js)
- **Full CRUD**: A secure dashboard to Create, Read, Update, and Delete all app content.
- **Centralized API**: Uses an Axios instance with interceptors to manage admin authentication.
- **Component-Based**: Built with React Hooks (`useState`, `useEffect`) for state management.

---

## 🛠️ Tech Stack

| Area        | Technology                                                                       |
|-------------|----------------------------------------------------------------------------------|
| Android     | Kotlin, MVVM, LiveData, Coroutines, Retrofit, OkHttp, Gson, Glide, SharedPreferences |
| Backend     | Node.js, Express.js, MongoDB, Mongoose, JWT, bcrypt.js, Multer, Cloudinary, Firebase Admin |
| Admin Panel | React.js, Axios, React Hooks                                                     |

---

## 🏗️ Architecture

This project follows a modern, decoupled architecture:
- **Android App**: Follows a strict MVVM (Model-View-ViewModel) pattern.
- **Backend**: A stateless RESTful API built with Express.js.
- **Admin Panel**: A Single-Page Application (SPA) built with React.

