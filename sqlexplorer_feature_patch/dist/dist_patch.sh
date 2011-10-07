#!/bin/bash
# build SQL Explorer patch distribution

# prepare update site
echo "prepare update site"
rm -rf updates
mkdir updates
unzip -q plugin.zip -d updates
unzip -q xml.zip -d updates

echo "dist update site"
cp -rvp updates/* /home/project-web/eclipsesql/htdocs/updates
