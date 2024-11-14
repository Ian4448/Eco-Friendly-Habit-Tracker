# Eco-Friendly Habit Tracker 🌱

An eco-conscious habit-tracking application designed to promote sustainable living through habit formation. This project helps users establish eco-friendly habits by providing a simple interface to track progress and view environmental impact reports. Built with a Java backend and minimal HTML/CSS frontend, this app demonstrates the potential of technology in fostering sustainable lifestyles.

## Table of Contents
- [Features](#features)
- [Future Enhancements](#future-enhancements)
- [Getting Started](#getting-started)
- [Installation](#installation)
- [Technologies Used](#technologies-used)
- [Contributing](#contributing)
- [Demo](#demo)
---

## Features
- **User Registration:** Users are able to create/register a new account which is stored with the in-memory h2 DB, and user's session id/auth token is stored for relevant login security.
- **Vehicle Registration:** Users are able to create and remove various vehicles with different attributes (name, make, model, mpg) which will be used in the future to calculate carbon emissions.
- **Extensive Testing:** Unit tests have been added to relevant logic to ensure functionality is working as intended, integration tests are on priority TODO. 

## Future Enhancements
- **Progress Reporting:** Visual summaries of your eco-habits’ impact over time. **[Currently Working On]**
- **Habit Tracking:** Track daily eco-friendly actions, set goals, and monitor your streaks.
- **Reminders and Notifications:** Encouragement to complete daily habits.
- **Eco-Fact of the Day:** Stay informed with daily eco-friendly tips and facts.
- **User Accounts:** Register and log in to save personal tracking data securely.
---

## Getting Started
To get a local copy of the project up and running, follow these simple steps.

### Prerequisites
- **Java 11 or later** installed
- **Maven** installed for managing dependencies
- **H2 Database** configured

### Installation
1. **Clone the repository**:
   Begin by cloning this repository to your local system:
   ```bash
   git clone https://github.com/Ian4448/Eco-Friendly-Habit-Tracker.git
   cd Eco-Friendly-Habit-Tracker```
2. **Install dependencies**:
   ```bash
   mvn install```
3. **Access the Application**:
   Open your web browser and navigate to http://localhost:8080 to access the Eco-Friendly Habit Tracker.
---   

## Technologies Used

### Languages
- **Java**: Backend language for business logic and core functionality.
- **HTML/CSS**: Used for the frontend layout and styling.
- **JavaScript**: Adds interactivity to the frontend.

### Frameworks and Libraries
- **Spring**: Provides a framework for building scaleable applications, handling dependency injection, RESTful APIs, and security.
- **Mockito**: Allows for unit testing by creating mock objects to simulate interactions.
- **JUnit**: Framework for testing Java applications, ensuring code reliability and performance.

### Databases and Persistence
- **H2 Database**: An in-memory database used for development and testing, providing easy and fast setup.
- **JPA (Java Persistence API)**: Manages relational data, simplifies database interactions, and maps Java objects to database tables.

### Security and Authentication
- **BCrypt**: Provides secure password hashing to protect user credentials.
- **JSoup**: Provides ability to parse HTML and sanitize input to only receive necessary text.

### API
- **REST API**: Used for client-server communication, allowing the frontend to interact with backend services seamlessly.
- **Google Maps API**: TODO
---  

### Contributing
Contributions are welcome! Follow these steps to contribute:
1. **Fork the project.**
2. **Create your feature branch (git checkout -b feature/AmazingFeature).**
3. **Commit your changes (git commit -m 'Add AmazingFeature').**
4. **Push to the branch (git push origin feature/AmazingFeature).**
5. **Open a pull request.**
For any significant changes, please open an issue first to discuss what you would like to change.

---
## Demo
<p align="center">
  <img src="https://github.com/user-attachments/assets/f859a6dc-3bb0-41ed-af9d-2afd0890c081" alt="Centered Image">
</p>

<div align="center">
  <img src="https://github.com/user-attachments/assets/2d8b7570-1ceb-481a-943b-2b75e3081731" height="450px" alt="First Image">
  <img src="https://github.com/user-attachments/assets/e903e0bd-e3a3-4efa-a9cf-7aaf70e5d60d" height="450px" alt="Second Image">
</div>

---
