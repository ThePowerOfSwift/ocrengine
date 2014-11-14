ocrengine
=========

OCR Engine.

This OCR Engine uses Tesseract-ocr, Leptonica and does the Character Recogonization.

This is a basic web app which gets the image as input and gives the extracted characters in JSON format along with the confidence value for each word.

====
Procedure followed to install Tesseract in your machine
====

Download the Tesseract engine (3.03). Source available at (Note am using Release Candidate version here)

https://tesseract-ocr.googlecode.com/archive/3.03-rc1.tar.gz

Download and install Leptonica 1.71 which is image processing and conversion tool used by Tesseract,

http://www.leptonica.com/source/leptonica-1.71.tar.gz


Incase if your Ubuntu machine doesn t have neccessary tools to run autogen and make commands, Install it using the commands below,

sudo apt-get install automake
sudo apt-get install build-essential libtool


Once you are done with this extract Leptonica and tesseract package using command,

tar -zxvf 3.03-rc1.tar.gz
tar -zxvf leptonica-1.71.tar.gz

Change the directory to leptonica and install it from the source using the commands,

sudo ./autogen.sh
mkdir ~/local
sudo ./configure --prefix=$HOME/local/
sudo make
sudo make install


Change the directory to tesseract-ocr-3.03-rc1 and execute the same set of commands given above.

Verify the installation using the command tesseract -v and it should give you the installed tesseract version number.

Copy the libtesseract.so, libtesseract.so.3, libtesseract.so.3.0.3 libraries to /usr/lib/. These shared objects are used by the Java Implementation while linking to native libraries. And yah these files will be available in the path,
tesseract-ocr-3.03-rc1/api/.libs

=============

To run the application,

mvn install

and the application will be started in 4565 port.
