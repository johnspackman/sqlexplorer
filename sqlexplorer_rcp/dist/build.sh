#!/bin/bash
# build SQL Explorer distribution

PREFIX=sqlexplorer_rcp-
PLUGIN_DIR="SQL Explorer Plugin/$1"
RCP_DIR="SQL Explorer RCP (exc JRE)/$1"
JRE_DIR="SQL Explorer RCP (inc JRE)/$1"

# prepare update site
echo "build update site"
rm -rf updates
mkdir updates
cd updates
unzip -q ../plugin.zip
unzip -q ../xml.zip
cd ..


# prepare dist
rm -rf dist
mkdir -p "dist/$PLUGIN_DIR"
mkdir -p "dist/$RCP_DIR"
mkdir -p "dist/$JRE_DIR"

# copy plugin zip
echo "build plugin dsitribution"
cp plugin.zip "dist/$PLUGIN_DIR/sqlexplorer_plugin-$1.zip"
cp release.txt "dist/$PLUGIN_DIR"

# build rcp
echo "build RCP dsitribution"
cd "dist/$RCP_DIR"
unzip -q ../../rcp.zip
cd ../..

# build win32 JRE
DELTA=win32.win32.x86
echo "build $DELTA with JRE" 
cp "dist/$RCP_DIR/$PREFIX$1.$DELTA.zip" test.zip
rm -rf delta
mkdir delta
mkdir delta/SQLExplorer
cd delta/SQLExplorer
unzip -q ../../jre.zip
cd ..
zip -rq ../test.zip SQLExplorer
cd ..
mv test.zip "dist/$JRE_DIR/$PREFIX$1.$DELTA-JRE.zip"
rm -rf delta


ls -AlRgGh updates
ls -AlRgGh dist

echo "Done"

