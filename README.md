# SoftwareEngineeringLaserTag   
CSCE 35103-001 Laser Tag project. 

GitHub Names:    MattW005      |    martianbingus     |     BrightRupp    |    9R-R9               
Member Names:    Matt Wells    |    Marvin Barnett    |     Gene Rupp     |    Ridoy Roy           

INSTRUCTIONS TO RUN:                                                                                  
    Open VirtualBox                                                                               
    Clone our repository                                                                          
<<<<<<< HEAD
        git clone https://github.com/martianbingus/SoftwareEngineeringLaserTag.git                
=======
        git clone -b TestBranch https://github.com/martianbingus/SoftwareEngineeringLaserTag.git                
>>>>>>> TestBranch
    Change directory to our repo                                                                  
        cd SoftwareEngineeringLaserTag                                                            
    Run install script                                                                            
        chmod +x install.sh                                                                       
        ./install.sh                                                                              
    Compile the code                                                                              
<<<<<<< HEAD
        javac *.java                                                                              
    Launch the code                                                                               
        java -cp ".:postgresql-42.7.10.jar" Laser                                                 

        
=======
        javac -cp ".:junixsocket-common-2.10.1.jar:junixsocket-native-common-2.10.1.jar:postgresql-42.7.10.jar" *.java                                                                              
    Launch the code                                                                               
        java -cp ".:junixsocket-common-2.10.1.jar:junixsocket-native-common-2.10.1.jar:postgresql-42.7.10.jar" Laser
>>>>>>> TestBranch
