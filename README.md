# Voigt-Kampff Testing Application

The Voigt Kampff test  is a humanity detection test imagined by Philip K. Dick in his novel “Do Androids Dream of Electric Sheep?”.  
Reddit is the biggest forum on the Internet.  
This application aims to determine whether a Reddit user is human or a bot.    

There are two main components:
* VoigtKampfServer - an API/web server that does the actual detection
* VoigtKampfApp - the Android application

## Voigt-Kampf Server
Uvicorn python application that can run either as a Docker container or standalone.  
Functionality wise, it connects to Reddit, fetches all the data for a specified user and runs it through a pre-trained BERT based neural network that will classify the user as a bot with a specified probability 

### API
The main API that it exposes is /api/analyze that takes the username as a query parameter.   
It returns a json file with the below self-explanatory fields:
```json
  {
    "username": username,
    "bot_probability": probability,
    "confidence_score": activity_score,
  }
```

### How to run the server
There are two ways to run the server:
* as a Docker container
* as a standalone application

In both flavors it will run on port 5050. After starting the application point your browser to http://localhost:5050

### Files
The following files are provided:
* Dockerfile - contains the docker container definition
* run-webapp.sh - script that will build the docker container and start it
* run-webapp-mac.sh - script that will run the application standalone, assumes a python environment with the prerequisites installed. The prerequisites are listed in the Dockerfile
* src/reddit.py - library that wraps up the Reddit data fetching library
* vktapp.py - main application, implements the API server and a webapp
* src/templates/* - html templates for the webapp
* test_test_bots_data.json - **attention** this file is not submitted in github due to it's size. It contains the pre-trained model weights. Please contact me, icordos@gmail.com to provide it separately.

## Voigt-Kampf Application
JetPack Compose Android application that uses the aforementioned server to expose the Reddit detection functionality. [Watch the demo](media/screen2.mov)
