# VAC
## Overview
VAC (Video-Audio Converter) is a student project with learning as it's basic goal. It focuses on networking technologies and databases. Main functionality of VAC is (as full name suggests) converting audio to video. To accomplish this we have created three programs:
- Android client
- Flask server
- RTP streamer (Java)

Android client sends photos to Flask server in order to process them and then server sends resulting sound to streamer.
Our Flask server is equipped with swagger specification generation for ease of getting familiar with its methods.

## Tools
We used JetBrains programs (IntelliJ IDEA, Pycharm) to program server applications and Android Studio for client. For database setup we used Docker framework and MS SQL container.

## How to run
As our project implements primitive microservice architecture, it needs more than one program to run in order to achieve full functionality. In brief you have to run RTP streamer, database and Flask application to setup server. Then you only need to install client app and go through diferrent menus (you'll know what to do when you see them).

## Contributions
  - https://www.hackster.io/sam1902/encode-image-in-sound-with-python-f46a3f
  
## Credits
[Wojciech Lulek (Zerkles)](https://github.com/Zerkles)<br>
[Tomasz Kiljańczyk (Gunock)](https://github.com/Gunock)
