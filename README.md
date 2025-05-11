# Capstone Project: Calendar Application

An interactive JavaFX calendar app with Firebase Firestore–backed scheduling and social event sharing.

## Table of Contents
- [Features](#features)
- [Tech Stack](#tech-stack)
- [Prerequisites](#prerequisites)
- [Installation & Setup](#installation--setup)
- [Running the App](#running-the-app)
- [Contributing](#contributing)
- [License](#license)

## Features
- **Personal Calendar View**  
  JavaFX-powered calendar grid and list showing your upcoming events.
- **Social Events**  
  Look up friends by username and view their next 5 scheduled events.
- **Event Management**  
  Create, edit & delete events (title, date, time) through a clean FXML form.
- **User Settings**  
  Change your display name or delete your account with built-in confirmation dialogs.

## Tech Stack
- **Java 23**  
- **JavaFX 23** (FXML layouts, Scene Builder)  
- **Firebase Admin SDK** for Google Cloud Firestore  
- **Maven** (with Maven Wrapper `mvnw` / `mvnw.cmd`)  
- **CSS** for UI styling
- ## How It Works

## Prerequisites
1. **JDK 17** or higher  
2. **Maven 3.6+** (or use the included Maven Wrapper)  
3. A **Firebase service account key** JSON file  
4. A **Firestore database** enabled in your Google Cloud project


## Installation & Setup
# 1. Clone the repo

git clone https://github.com/nelsonm04/Capstone_Project.git
cd Capstone_Project

# 2. Place your Firestore credentials
#    Download the JSON key and export its path:
export GOOGLE_APPLICATION_CREDENTIALS="/path/to/your-key.json"

# 3. Build dependencies
./mvnw clean install

## Running the App
User Flow
- **Register/Login → Calendar View & Event List → Event Management → Social View → Settings**
  ![image](https://github.com/user-attachments/assets/0b8882f5-0d1b-4de3-a9fd-3150cf650ed7)
  ![image](https://github.com/user-attachments/assets/482fe5e7-c02a-48c2-b47f-518570710cf0)


Load Events
![image](https://github.com/user-attachments/assets/17a7b444-a2ea-4b4d-ba4b-d9bf075e5589)

- In `EventController.initialize()`, the app:
  ```java
  ApiFuture<QuerySnapshot> future = db.collection("users")
      .document(currentUid)
      .collection("events")
      .whereGreaterThanOrEqualTo("date", today.toString())
      .orderBy("date")
      .get();
Event Management
- **Create & Update**
- ![image](https://github.com/user-attachments/assets/e5ac0f6c-3ab0-42de-9282-e23b1e73295e)
  - Clicking a date cell or the **New Event** button loads `event_view.fxml`.  
  - In `EventController.saveEvent()`, collects form data and writes to Firestore:
    ```java
    db.collection("users")
      .document(currentUid)
      .collection("events")
      .document(eventId)
      .set(eventData);
    ```
  - If `eventId` already exists, `.set()` overwrites the existing document (update).
  - ![image](https://github.com/user-attachments/assets/ca0880e0-39b8-4e58-a72a-becbac4b3909)
- **Delete**  
  - The **Delete** button shows a confirmation `Alert`, then calls:
  - ![image](https://github.com/user-attachments/assets/5936174e-c800-4b48-b18e-922f4206824b)
    ```java
    db.collection("users")
      .document(currentUid)
      .collection("events")
      .document(eventId)
      .delete();
    ```
  - After deletion, the controller refreshes both the calendar grid and the event list to reflect the removal.
View Friend Events
![image](https://github.com/user-attachments/assets/bc35ff82-402c-49df-9068-56f06c71f003)

- **Find Friend UID**
- ![image](https://github.com/user-attachments/assets/f141d59a-9727-47d3-b635-f55fcfedcab3)

  In `SocialController.showFriendEvents(String friendUsername)`, query Firestore for the friend’s UID:  
  ```java
  String friendUid = db.collection("users")
      .whereEqualTo("username", friendUsername)
      .get().get()
      .getDocuments().get(0)
      .getId();

- **Settings & Account Management**

- ![image](https://github.com/user-attachments/assets/bc26bc4f-ca26-401c-b3e3-80563b75d53f)

Change Username
- In `SettingsController.handleUsernameChange()`, read and trim the input, then update Firestore:
  ```java
  String newUsername = changeUserNameField.getText().trim();
  db.collection("users")
    .document(currentUid)
    .update("username", newUsername);

## Contributing
## License






