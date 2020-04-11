#solar-ordering-tool
A tool for generating project specific material orders for residential solar projects. 

## Background:
This tool simplifies and and standardizes the material ordering process for a residential solar-pv construction company. For each solar installation, a material order is generated and shipped to the construction site using a third party supplier. This tool generates the material order as well as a cost estimate that is based on the most recent pricing obtained form the supplier. The cost estimate can be compared against the actual order cost to quickly spot discrepancies between what was asked for and what is being supplied.

## Prerequisites:
In order to use this tool you must have:
- The latest version of Java Runtime Environment (JRE)
- Numbers

## How To Use:
1. Open up the Numbers file "Solar Ordering Template.numbers" and enter all system info.
2. Export the Numbers file to a CSV located within the same directory.
3. Run either "GenerateOrderWCost.sh" or "GenerateOrder.sh".
4. The java program will print out the material order to the terminal in addition to creating a corresponding time stamped txt file in the directory "Order_History".

## Installing Java
Enter the following commands into your terminal: 
/usr/bin/ruby -e "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/master/install)"
brew cask install java
