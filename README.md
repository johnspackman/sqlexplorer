

# Eclipse SQL Explorer v3.7

## Code changes

This version of SQL Explorer has been migrated from [Sourceforge](https://sourceforge.net/projects/eclipsesql/) in 
October 2018; as well as fixing a few bugs, the code has been refactored to simplify development and deployment - 
where previously each database driver was implemented in a separate plugin and the plugins were combined for 
deployment via a feature, these have been removed and the drivers integrated into the one plugin.  

Older and more esoteric database support (eg Informix) has been dropped, although the code is still available in the 
master branch and would be straightforward to integrate.  The databases supported are:

* Oracle
* MySQL
* Microsoft SQL Server

## Releases
Currently, the only release is as a plugin for manual deployment.  This could change, depending on
whether there is demand (or someone wants to support it). 

To use this release:
1. Shut down Eclipse
2. In the Eclipse application directory 
a. find the `features` directory and delete all `net.sourceforge.sqlexplorer*` files & directories.
b. find the `plugins` directory and delete all `net.sourceforge.sqlexplorer*` files & directories.
3. Download the .jar from the Releases page at GitHub and place it in the `plugins` directory
4. Start Eclipse with the `-clean` command line argument
After the first time with `-clean` you can start Eclipse normally.

