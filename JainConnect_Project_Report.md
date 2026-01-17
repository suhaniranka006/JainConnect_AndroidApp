# PROJECT REPORT: JainConnect
## A Comprehensive Digital Platform for the Jain Community

**Submitted by:** [Your Name / Group Members]
**Department:** Computer Science & Engineering
**Date:** January 2026

---

## TABLE OF CONTENTS

1.  **Abstract**
2.  **Chapter 1: Introduction**
    *   1.1 Background
    *   1.2 Problem Statement
    *   1.3 Objectives
    *   1.4 Project Scope
    *   1.5 Limitations
3.  **Chapter 2: Literature Review**
    *   2.1 Existing Systems
    *   2.2 Component Analysis
    *   2.3 Proposed Improvements
4.  **Chapter 3: System Analysis & Requirements**
    *   3.1 Software Requirement Specification (SRS)
    *   3.2 Hardware Requirements
    *   3.3 Functional Requirements
    *   3.4 Non-Functional Requirements
5.  **Chapter 4: System Design**
    *   4.1 System Architecture (MVVM)
    *   4.2 Database Design & Schema
    *   4.3 Data Flow Analysis
6.  **Chapter 5: Implementation Details**
    *   5.1 Technology Stack Selection
    *   5.2 Module Description
    *   5.3 Key Algorithms & Logic
7.  **Chapter 6: Testing & Validation**
    *   6.1 Testing Methodology
    *   6.2 Test Cases
8.  **Chapter 7: Conclusion & Future Scope**
9.  **References**

---

## ABSTRACT

In the contemporary digital landscape, niche communities often find themselves underserved by generic social networking platforms. "JainConnect" is a specialized mobile application developed to bridge the technological gap for the Jain community, integrating spiritual tradition with modern utility. The primary objective of this project is to centralize information regarding religious observances (Tithis), logistics (Bhojanshalas, Dharamshalas), and spiritual leadership (Maharaj Vihar tracking).

Developed using a robust Native Android (Kotlin) frontend and a scalable Node.js/MongoDB backend, the system addresses critical community needs such as finding nearby pilgrimage companions (Carpooling) and receiving accurate, location-based astro-religious timings. This report details the full software development lifecycle of JainConnect, from requirement analysis to deployment, highlighting the innovative use of Geospatial querying and Publish-Subscribe notification architectures to foster community cohesion.

---

## CHAPTER 1: INTRODUCTION

### 1.1 Background
The Jain community is characterized by its strict dietary laws, complex lunar calendar system, and the nomadic nature of its ascetics (Sadhus and Sadhvis) who travel on foot. Traditionally, information regarding the location of these ascetics, the timing of specific fasts (Pachkhan), or the organizing of group pilgrimages (Sangh Yatras) has been disseminated through fragmented channels—person-to-person word of mouth, printed newsletters, or disjointed WhatsApp groups. This analog approach often leads to misinformation, delays, and a lack of coordination.

### 1.2 Problem Statement
The current ecosystem lacks a "Super App" for the Jain community. Users currently need:
1.  One app for the Calendar (Panchang).
2.  Another app or website to find Dharmashalas (stay options).
3.  Social groups to find out where a Maharaj Saheb is currently located.
4.  Personal contacts to organize transport for Yatras.

This fragmentation results in a disjointed user experience and missed opportunities for community engagement. Furthermore, generic apps do not account for specific religious constraints (e.g., sunset timings for chauvihar).

### 1.3 Objectives
The primary objectives of this project are:
1.  **Centralization:** To unify all religious and logistical tools into a single, cohesive mobile interface.
2.  **Real-time Tracking:** To implement a crowdsourced, GPS-enabled system for tracking the Vihar (movement) of spiritual leaders.
3.  **Logistical Optimization:** To reduce travel costs and environmental impact by enabling a community-specific Carpooling system for Tirthyatras.
4.  **Accuracy:** To provide precise, location-based astronomical calculations for religious timings.

### 1.4 Project Scope
The project encompasses:
*   **User Module:** Registration, Profile Management, Community Directory.
*   **Religious Module:** Digital Panchang, Pachkhan Audio/Text, Tithi Notifications.
*   **Vihar Module:** Interactive Maps showing ascetic locations.
*   **Logistics Module:** Bhojanshala directory, Tirthyatra planning, Carpool matchmaking.
*   **Admin Panel:** A web-based dashboard for content moderation and master data management.

### 1.5 Limitations
*   **Network Dependency:** The app requires active internet connectivity for real-time updates (Vihar/Carpool).
*   **Crowdsourced Accuracy:** The accuracy of Maharaj locations depends on user inputs, requiring a verification mechanism (which is implemented via admin approval).

---

## CHAPTER 2: LITERATURE REVIEW

### 2.1 Existing Systems
We analyzed three categories of existing solutions:
1.  **WhatsApp/Telegram Groups:**
    *   *Pros:* High familiarity, ease of use.
    *   *Cons:* Unstructured data, limit on member count, important messages get buried, no searchability.
2.  **Static Religious Apps (e.g., Jain Calendar apps):**
    *   *Pros:* Good for dates.
    *   *Cons:* No social interaction, no dynamic updates on events or locations.
3.  **Social Media (Facebook Groups):**
    *   *Pros:* Broad reach.
    *   *Cons:* Privacy concerns, algorithm-driven feeds hide relevant posts, lack of specific utility tools.

### 2.2 Comparison Matrix

| Feature | WhatsApp | Traditional Calendar Apps | **JainConnect** |
| :--- | :--- | :--- | :--- |
| **Pachkhan Timings** | Manual Text | Static Table | **Location-based Dynamic** |
| **Vihar Tracking** | Text Description | Not Available | **Live GPS Map** |
| **Carpooling** | Unorganized Chat | Not Available | **Structured Matching** |
| **Data Privacy** | Phone Number Exposed | Minimal Data | **Secure Auth & Masking** |

### 2.3 Proposed Improvements
JainConnect improves upon these systems by introducing **Geospatial Intelligence**. Unlike static apps, JainConnect knows where the user is. This allows it to suggest *nearby* events, sort carpools by *proximity*, and adjust religious timings based on the user's specific *longitude/latitude*.

---

## CHAPTER 3: SYSTEM ANALYSIS & REQUIREMENTS

### 3.1 Software Requirement Specification (SRS)
The system is designed as a client-server application.
*   **Client:** Android Application (min SDK 24).
*   **Server:** RESTful API hosted on AWS/Heroku/DigitalOcean.
*   **Database:** MongoDB Atlas (Cloud).

### 3.2 Hardware Requirements
*   **Development:** Processor i5 or higher, 16GB RAM (for Android Studio + Emulator).
*   **Client Device:** Android Smartphone with GPS and Internet capabilities.

### 3.3 Functional Requirements
1.  **Authentication:** Users must be able to sign up/login via Email or OTP.
2.  **Maharaj Tracking:** Users can add a "Spotting" of a Maharaj with a photo and location. This pin appears on the map for others.
3.  **Tithi Module:** System checks date daily and highlights important Jain festivals.
4.  **Carpool:** Users can "Offer Ride" (Driver) or "Request Seat" (Passenger). System matches them based on Source/Destination.

### 3.4 Non-Functional Requirements
1.  **Scalability:** The backend must handle simultaneous requests during peak festival times (Paryushan).
2.  **Reliability:** The app should crash-free (99.9% crash-free session target).
3.  **Security:** User passwords must be hashed (Bcrypt). API communication must use HTTPS.
4.  **Usability:** UI must be intuitive for all age groups, including elderly users who are frequent consumers of religious content.

---

## CHAPTER 4: SYSTEM DESIGN

### 4.1 System Architecture
The Android application follows the **MVVM (Model-View-ViewModel)** architectural pattern, recommended by Google for robust development.

*   **View (Activities/Fragments):** Handles UI rendering and user interactions. It observes data from the ViewModel.
*   **ViewModel:** Stores and manages UI-related data in a lifecycle-conscious way. It acts as a bridge between the Repo and UI.
*   **Repository:** The single source of truth. It decides whether to fetch data from the Local Database (Room - Cached) or the Remote Data Source (Retrofit API).
*   **Model:** Data classes representing the schema (e.g., `User`, `Event`, `Tithi`).

### 4.2 Database Design & Schema (MongoDB)
Since we use a NoSQL database, the data is stored in Collections of Documents.

**Collection: Users**
```json
{
  "_id": "ObjectId",
  "name": "String",
  "email": "String",
  "location": "GeoJSON Point",
  "is_verified": "Boolean"
}
```

**Collection: Tithis**
```json
{
  "date": "ISODate",
  "title": "String (e.g. Mahavir Janma Kalyanak)",
  "type": "Enum (Parv, Regular)",
  "description": "String"
}
```

**Collection: Carpools**
```json
{
  "owner_id": "ObjectId(User)",
  "source_coords": [Long, Lat],
  "dest_coords": [Long, Lat],
  "seats_available": "Integer",
  "date_time": "ISODate"
}
```

### 4.3 Data Flow Analysis (DFD Level 1: Carpool)
1.  **User** submits "Create Ride" request -> **Validation Logic** checks inputs.
2.  **Validation Logic** -> **Database** (Insert Record).
3.  **Search Process** queries **Database** using Geo-Index based on User Location.
4.  **Database** returns "Nearby Rides" -> **UI** displays List.

---

## CHAPTER 5: IMPLEMENTATION DETAILS

### 5.1 Technology Stack Selection
*   **Kotlin:** Chosen over Java for its null-safety features, coroutines support (for asynchronous background tasks), and modern syntax, reducing boilerplate code by ~40%.
*   **Node.js:** Chosen for the backend due to its non-blocking I/O model, making it ideal for handling multiple concurrent requests (e.g., real-time chat/location updates).
*   **Hilt (Dependency Injection):** Used to decouple classes, making the code modular and testable.

### 5.2 Key Algorithms

#### A. Geolocation Sorting (The "Nearby" Logic)
To implement the "Nearby Rides" feature, we utilized the Haversine formula abstraction provided by MongoDB's `$geoNear` operator.
*   *Input:* User's current (Lat, Long).
*   *Process:* The database calculates the spherical distance between the user and every active ride offering.
*   *Output:* A list of rides sorted by distance ascending (Nearest First).

#### B. Dynamic Tithi Notification
A Cron Job runs on the server at 00:00 UTC daily.
1.  Fetches today's date.
2.  Queries the `Tithi` collection for any matches.
3.  If a match is found, retrieves all valid FCM tokens from the `User` collection.
4.  Sends a high-priority data payload via Firebase Cloud Messaging.

### 5.3 Code Snippet (Android Repository Pattern)
```kotlin
// Example of clean architecture in JainConnect
class TithiRepository @Inject constructor(
    private val apiService: ApiService,
    private val tithiDao: TithiDao
) {
    suspend fun getTithis(): Resource<List<Tithi>> {
        return try {
            // Attempt network fetch
            val response = apiService.getTithis()
            if (response.isSuccessful) {
                // Cache data locally
                tithiDao.insertAll(response.body())
                Resource.Success(response.body())
            } else {
                Resource.Error("Server Error")
            }
        } catch (e: Exception) {
            // Fallback to local cache if offline
            val cached = tithiDao.getAllTithis()
            Resource.Success(cached)
        }
    }
}
```

---

## CHAPTER 6: TESTING & VALIDATION

### 6.1 Testing Methodology
*   **Unit Testing:** Performed on utility classes (e.g., ensuring the Date Formatter correctly parses API strings).
*   **Device Compatibility:** Tested on devices ranging from Android 8.0 (Oreo) to Android 14.
*   **Network Simulation:** Tested app behavior under 2G, 4G, and Offline conditions.

### 6.2 Test Cases (Sample)

| TC ID | Test Scenario | Steps | Expected Result | Status |
| :--- | :--- | :--- | :--- | :--- |
| TC_01 | **Login with Invalid Credentials** | Enter wrong password | Show "Invalid Credentials" Toast | **Pass** |
| TC_02 | **Create Carpool** | Fill details -> Click Submit | Redirect to "My Rides", Show Success | **Pass** |
| TC_03 | **Offline Mode (Tithi)** | Turn off Data -> Open Tithi | Load data from Room DB | **Pass** |
| TC_04 | **Play Store Security** | Upload Release Build | Warning if ProGuard is missing/crashing | **Fixed (v1.3)** |

### 6.3 Security Validation
We encountered a crash in the release build due to **ProGuard** obfuscating our Data Models. This was validated by analyzing `logcat` mappings.
*   *Fix:* Added `@Keep` annotations and updated `proguard-rules.pro` to preserve `com.mycompany.jainconnect.data.models.**`.
*   *Result:* The release build now functions correctly while maintaining code security.

---

## CHAPTER 7: RESULT AND DISCUSSION

The development of JainConnect has resulted in a stable, feature-rich application that meets the initial SRS.
*   **User Engagement:** Beta testing showed a high engagement rate with the "Tithi" feature, as users appreciated the clean interface over traditional cluttered calendars.
*   **Performance:** The app maintains a smooth 60fps frame rate for lists using `RecyclerView` and `DiffUtil`.
*   **Impact:** The Carpool feature has successfully facilitated 15+ mock trips during the testing phase, validating the algorithm's effectiveness.

The integration of Google Maps for "Maharaj Tracking" proved to be the most visually appealing feature, giving users an instant "birds-eye view" of spiritual activity around them.

---

## CHAPTER 8: CONCLUSION & FUTURE SCOPE

### 8.1 Conclusion
JainConnect successfully demonstrates how modern mobile technology can be tailored to serve specific cultural needs. By moving away from generic solutions and building custom modules for Vihar Tracking and Tirthyatra Logistics, the project provides tangible value to the community. The use of Scalable Vector Graphics (SVG/XML), Room Persistence Library, and Clean Architecture ensures the app is maintainable and future-proof.

### 8.2 Future Scope
1.  **iOS Version:** Developing a React Native or Flutter version to target iPhone users.
2.  **AI Chatbot:** Integrating a fine-tuned LLM to answer religious queries based on Jain Scriptures (Agams).
3.  **Live Streaming:** Adding a module for live telecast of Pravachans (sermons) from major temples.

---

## REFERENCES

1.  *Android Developers Documentation*. (2025). Guide to App Architecture. [https://developer.android.com/jetpack/guide](https://developer.android.com/jetpack/guide)
2.  *MongoDB Inc.* (2025). Geospatial Queries Protocol.
3.  *Martin, R. C.* (2018). *Clean Architecture: A Craftsman's Guide to Software Structure and Design*. Prentice Hall.
4.  *Jain Community Statistics & Needs Assessment Survey* (Internal Group Study, 2025).
