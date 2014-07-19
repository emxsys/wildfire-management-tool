# README #
## Wildfire Management Tool##
### What is this repository for? ###
#### Quick summary ####
This is a NetBeans 8.0 mavenized module suite used to build the Wildfire Management Tool (WMT).

WMT is a software application based on the Campbell Prediction System (CPS) that estimates the potential behavior of a wildland fire so that firefighting strategies can ensure the safety and effectiveness of firefighters. The purpose of this tool is to provide an analysis of the potential fire behavior, and to visualize the areas of risk and/or opportunity. This project is an effort to enhance the training of wildland firefighters and fire behavior analysts, and to provide useful fire behavior estimates on active wildland fires and analyses of historical fires. 

This project uses the NASA WorldWind SDK for the virtual globe, which is provided by the [Emxsys - Terramenta](https://bitbucket.org/emxsys/emxsys-terramenta) project dependency, which is another NetBeans maven project.  

This project is licensed under the BSD 3-Clause license. 
#### Version ####
4.0

### How do I get set up? ###
#### Summary of set up 
1. Checkout the [Emxsys - Terramenta source code](https://bitbucket.org/emxsys/emxsys-terramenta/src) and build. 
1. Checkout the [Wildfire Managmement Tool source code](https://bitbucket.org/emxsys/wildfire-management-tool/src) and build. 

#### Configuration
* The source code is organized as a Maven project.
* Source code formatting uses the NetBeans defaults.

#### Dependencies
* NetBeans 8.0
* JDK 8
* [Emxsys - Terramenta Project](https://bitbucket.org/emxsys/emxsys-terramenta)
* NASA WorldWind 2.0 (included in the Emxsys - Terramenta project)

#### Database configuration 
* N/A.

#### How to run tests 
* JUnit is used for unit testing.
* Each module has its own Test Packages, which are executed during the build process.

#### Deployment instructions 
* Execute build goal to produce executable.
* Install4J has been selected as the installer.

### Contribution guidelines 
* Developers are welcome! Please contribute.
* Also, please make contributions to the root [Terramenta](/teamninjaneer/terramenta) project.
#### Beta Testing
* Beta testers needed
#### Internationalization
* Language translators needed 
#### Other guidelines 
* Please use the [WMT Issue Tracker](https://bitbucket.org/emxsys/wildfire-management-tool/issues) for bugs and feature requests.

### Who do I talk to? 
* Please contact Bruce Schubert, the project manager, for contribution access to the WMT source code. 
![OpenHub Profile](https://www.openhub.net/accounts/97968/widgets/account_tiny.gif)