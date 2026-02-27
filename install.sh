#!/bin/bash

echo "Starting installation..."
echo "Updating system package list..."
sudo apt update -y

#install java (JRE and JDK)
echo "Installing Java Runtime and Development Kit..."
sudo apt install -y default-jre default-jdk

#install py 3
echo "Ensuring Python 3 is installed..."
sudo apt install -y python3

sudo apt install -y postgresql

echo "Installation complete!"
echo "You can now compile the project using: javac ".:junixsocket-common-2.10.1.jar:junixsocket-native-common-2.10.1.jar:postgresql-42.7.10.jar" *.java"
echo "And run it using: java -cp ".:junixsocket-common-2.10.1.jar:junixsocket-native-common-2.10.1.jar:postgresql-42.7.10.jar" Laser"
