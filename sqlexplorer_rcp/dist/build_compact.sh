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

# unzip se plugins
echo "unpacking zips"
rm -rf sqlexplorer
mkdir sqlexplorer
cd sqlexplorer
unzip -q ../sqlexplorer_rcp_plain.zip
unzip -q ../plugin.zip
unzip -q ../rcp.zip
cd ..
rm -f base.zip
zip -rq base.zip sqlexplorer
rm -rf sqlexplorer

# unzip delta
rm -rf delta
unzip -q delta.zip

# prepare dist
rm -rf dist
mkdir -p "dist/$PLUGIN_DIR"
mkdir -p "dist/$RCP_DIR"
mkdir -p "dist/$JRE_DIR"

# copy plugin zip
echo "build plugin"
cp plugin.zip "dist/$PLUGIN_DIR/sqlexplorer_plugin-$1.zip"
cp release.txt "dist/$PLUGIN_DIR"

# build aix
DELTA=aix.motif.ppc
echo $DELTA
cp base.zip test.zip
rm -rf delta/sqlexplorer
mkdir delta/sqlexplorer
cp -r delta/$DELTA/* delta/sqlexplorer
cd delta
zip -rq ../test.zip sqlexplorer
cd ..
mv test.zip "dist/$RCP_DIR/$PREFIX$1.$DELTA.zip"

# build hpux
DELTA=hpux.motif.ia64_32
echo $DELTA
cp base.zip test.zip
rm -rf delta/sqlexplorer
mkdir delta/sqlexplorer
cp -r delta/$DELTA/* delta/sqlexplorer
cd delta
zip -rq ../test.zip sqlexplorer
cd ..
mv test.zip "dist/$RCP_DIR/$PREFIX$1.$DELTA.zip"

# build linux ppc
DELTA=linux.gtk.ppc
echo $DELTA
cp base.zip test.zip
rm -rf delta/sqlexplorer
mkdir delta/sqlexplorer
cp -r delta/$DELTA/* delta/sqlexplorer
cd delta
zip -rq ../test.zip sqlexplorer
cd ..
mv test.zip "dist/$RCP_DIR/$PREFIX$1.$DELTA.zip"

# build linux x86
DELTA=linux.gtk.x86
echo $DELTA
cp base.zip test.zip
rm -rf delta/sqlexplorer
mkdir delta/sqlexplorer
cp -r delta/$DELTA/* delta/sqlexplorer
cd delta
zip -rq ../test.zip sqlexplorer
cd ..
mv test.zip "dist/$RCP_DIR/$PREFIX$1.$DELTA.zip"

# build linux x86 64
DELTA=linux.gtk.x86_64
echo $DELTA
cp base.zip test.zip
rm -rf delta/sqlexplorer
mkdir delta/sqlexplorer
cp -r delta/$DELTA/* delta/sqlexplorer
cd delta
zip -rq ../test.zip sqlexplorer
cd ..
mv test.zip "dist/$RCP_DIR/$PREFIX$1.$DELTA.zip"

# build mac ppc
DELTA=macosx.carbon.ppc
echo $DELTA
cp base.zip test.zip
rm -rf delta/sqlexplorer
mkdir delta/sqlexplorer
cp -r delta/$DELTA/* delta/sqlexplorer
cd delta
zip -rq ../test.zip sqlexplorer
cd ..
mv test.zip "dist/$RCP_DIR/$PREFIX$1.$DELTA.zip"

# build mac x86
DELTA=macosx.carbon.x86
echo $DELTA
cp base.zip test.zip
rm -rf delta/sqlexplorer
mkdir delta/sqlexplorer
cp -r delta/$DELTA/* delta/sqlexplorer
cd delta
zip -rq ../test.zip sqlexplorer
cd ..
mv test.zip "dist/$RCP_DIR/$PREFIX$1.$DELTA.zip"

# build solaris
DELTA=solaris.gtk.sparc
echo $DELTA
cp base.zip test.zip
rm -rf delta/sqlexplorer
mkdir delta/sqlexplorer
cp -r delta/$DELTA/* delta/sqlexplorer
cd delta
zip -rq ../test.zip sqlexplorer
cd ..
mv test.zip "dist/$RCP_DIR/$PREFIX$1.$DELTA.zip"

# build win32 64
DELTA=win32.win32.x86_64
echo $DELTA
cp base.zip test.zip
rm -rf delta/sqlexplorer
mkdir delta/sqlexplorer
cp -r delta/$DELTA/* delta/sqlexplorer
cd delta
zip -rq ../test.zip sqlexplorer
cd ..
mv test.zip "dist/$RCP_DIR/$PREFIX$1.$DELTA.zip"

# build win32
DELTA=win32.win32.x86
echo $DELTA
cp base.zip test.zip
rm -rf delta/sqlexplorer
mkdir delta/sqlexplorer
cp -r delta/$DELTA/* delta/sqlexplorer
cd delta
zip -rq ../test.zip sqlexplorer
cd ..
mv test.zip "dist/$RCP_DIR/$PREFIX$1.$DELTA.zip"


# build win32 JRE
DELTA=win32.win32.x86
echo "$DELTA JRE" 
cp base.zip test.zip
rm -rf delta/sqlexplorer
mkdir delta/sqlexplorer
cp -r delta/$DELTA/* delta/sqlexplorer
cd delta/sqlexplorer
unzip -q ../../jre.zip
cd ../..
cd delta
zip -rq ../test.zip sqlexplorer
cd ..
mv test.zip "dist/$JRE_DIR/$PREFIX$1.$DELTA-JRE.zip"

# cleanup
echo "cleanup"
rm -rf delta
rm -f base.zip

ls -AlRgGh dist

echo "Done"

