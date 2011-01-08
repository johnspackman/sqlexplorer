#!/bin/bash
# build SQL Explorer distribution

PREFIX=sqlexplorer_rcp-
PLUGIN_DIR="SQL Explorer Plugin/$1"

# prepare update site
echo "build update site"
rm -rf updates
mkdir updates
unzip -q plugin.zip -d updates
unzip -q xml.zip -d updates

# prepare dist
rm -rf dist
mkdir -p "dist/$PLUGIN_DIR"

# copy plugin zip
echo "build plugin"
cp plugin.zip "dist/$PLUGIN_DIR/sqlexplorer_plugin-$1_$2.zip"
cp release.txt "dist/$PLUGIN_DIR/readme_release-$1_$2.txt"

ls -AlRgGh updates
ls -AlRgGh dist

echo "Done"

