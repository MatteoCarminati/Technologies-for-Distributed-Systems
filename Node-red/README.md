# Node-red Scenario
Implementation of a telegram chatbot, used to test the knowledge of Node-red.
The chatbot uses two additional extensions of Node-red which are:
- `node-red-node-openweathermap`, used for retrieving the temperature and other atmospherical data from cities all over the world
- `node-red-contrib-chatbot`, to set up correctly the bot on telegram, and to forward the messages asked to the bot to the Node-Red environment

In particular the chatbot can be used only if there is an active instance of the Node-red program.
It can be addressed from any device which have a connection to internet and an account on Telegram.

<p align="center">
  <img src="/Technologies-for-Distributed-Systems/main/Node-red/Images/IMG_6834.jpg" />
</p>

The bot is very simple, and it can answer only to 4 possible statements
- if you say 'My name is X' then the chatbot will answer with 'Hello X'
- if you ask 'What is the temperature in Milan/Rome', it will answer with the current temperature retrieved though the openweathermap extension
- if you ask anything else, then the chatbot will answer with 'I don't get what you asked...'

