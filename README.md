# HttpServerApp

Functionality:
* -p <port> command line option to specify port (default 80)
* -r <path> command line option to specify root directory (default .)
* /hello => welcome screen
* / | /\<dir path> => server “index.html” if it exists, else show listing of file in root directory
* /<file path> => serve files with appropriate mime type for extension (txt, png, jpg, gif, pdf) (sample files in repo)
* /guess => landing page for guessing game (7 chances to guess random number between 1 and 100. Computer lets you know if you’re too high or too low)
* /ping => sleep for 1 second, then respond with current time
