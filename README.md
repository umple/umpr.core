# umpr.core - Umple Sample Importer 

[![Build Status](https://travis-ci.org/umple/umpr.core.svg?branch=master)](https://travis-ci.org/umple/umpr.core)

Umpr is repository tool for managing the automatic importing of different modelling languages into the 
[Umple](http://umple.org) language. This project seeks to create a single location for downloading and verifying the 
status of different import mechanisms and the status of the tools doing it. 

This project is part of the Umple collection of projects. 

## Building the Project

### Requirements

umpr.core requires the following components: 

1. [Apache Ant](http://ant.apache.org/)
1. Java 8 -- Oracle is tested, OpenJDK is not, but should work.
1. A pre-built version of Umple -- [Umple Cheatsheet](https://github.com/umple/umple/wiki/CheatSheet), the location of
the `dist/umple.jar` is used in building umpr.core.

### Building

1. Clone the repository `git clone https://github.com/umple-ucosp/umpr.core.git`
1. Enter the directory `cd umpr.core/`  
1. Build the project `ant -Dumple.core.jar=PATH/TO/dist/umple.jar`
1. (Optional) Run the tests `ant -Dumple.core.jar=PATH/TO/dist/umple.jar test`
1. Package the jars `ant -Dumple.core.jar=PATH/TO/dist/umple.jar package`

## Packages

There are three jars produced: 

* `umpr.core-lib-VERSION-COMMIT.jar`: Only includes the dependency libraries merged into one JAR
* `umpr.core-VERSION-COMMIT.jar`: Only includes the classes defined in `umpr.core/`
* `umpr.core-all-VERSION-COMMIT.jar`: Includes both previous JARS in a single entity

## Running umpr.core (a cook book!)

To see all options: 

    $ java -jar ./bin/target/umpr.core-VERSION-COMMIT.jar --help
    Usage: <main class> [options] [Repository1] [.. [RepositoryN]]
    Options:
      -h, -?, --help
         Print help message.
         Default: false
      --import, -i
         Folder to save import files to
         Default: Temporary folder
      -l, --limit
         Number of imports to download in total, there are no guarantees to which
         repositories are used or what order. (-1 implies no limit)
         Default: -1
    * -o, --output
         Output folder for generated .ump files
      -O, --override
         Force overriding of the output folders, i.e. remove output folder
         contents.
         Default: false
    
To run with default configurations: 

    $ java -jar ./bin/target/umpr.core-VERSION-COMMIT.jar -o /DESIRED/OUTPUT/PATH
    
To run and save the fetched files (e.g. ecore, scxml files):

    $ java -jar ./bin/target/umpr.core-VERSION-COMMIT.jar -o /DESIRED/OUTPUT/PATH -i /DESIRED/INPUT/ENTITIES
    
To run and override all output that is present: 

    $ java -jar ./bin/target/umpr.core-VERSION-COMMIT.jar -O -o /DESIRED/OUTPUT/PATH -i /DESIRED/INPUT/ENTITIES   
    
     
