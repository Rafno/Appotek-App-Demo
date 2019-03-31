# Appotek-App-Demo
Demonstration of the code used in software project 2, with the API base url taken out until demonstration has finished.

# Appótekið
Project for Software Project 2 class in the University of Iceland.

## How do I run this ?
This project is created for Android SDK > 23. It uses Firebase (Url and API removed from project) to read barcode off medicine through the camera.

## What is this application showing ?
A hand-held android version of a previous project, this app has a built in search engine that, once connected to the proper API (removed until demo has finished) will allow you to search for any medicine sold in Iceland. It allows for a few medicine to be scanned via barcode using the vnr number placed in the barcode, however as this data was not available without scraping, it will only work for a selected few. A demo has been placed in the camera activity that will always return a specific drug once successfully scanning any barcode.

If the user creates an account, the information about that user is stored in a database and his password encrypted. The user can then login and add medicines to his account and be reminded of when to take said medicine via notifications.

A doctor account is available, with different functionalities. A doctor user can view all patients and add them to his care, at which point he can view their medicine cabinet.
