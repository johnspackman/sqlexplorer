#!/bin/bash
# build SQL Explorer distribution

PREFIX=sqlexplorer_rcp-
PLUGIN_DIR="SQL Explorer Plugin/$1"

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

# copy plugin zip
echo "build plugin"
cp plugin.zip "dist/$PLUGIN_DIR/sqlexplorer_plugin-$1_$2.zip"
cp release.txt "dist/$PLUGIN_DIR/release-$1_$2.txt"

# cleanup
echo "cleanup"
rm -rf delta
rm -f base.zip

ls -AlRgGh updates
ls -AlRgGh dist

echo "Done"

