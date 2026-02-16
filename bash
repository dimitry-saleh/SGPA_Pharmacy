# 1. Compile the code and resources into the JAR
mvn clean package

# 2. Setup the isolated workspace in /tmp
# (Isolating the build prevents 'FinderInfo' detritus from infecting the app)
rm -rf /tmp/sgpa_build
mkdir -p /tmp/sgpa_build/input
mkdir -p /tmp/sgpa_build/dist

# 3. Copy the fresh JAR and your Icon
cp target/SGPAManager.jar /tmp/sgpa_build/input/
cp src/main/resources/images/logo.icns /tmp/sgpa_build/logo.icns

# 4. Strip hidden macOS attributes from the build environment
xattr -cr /tmp/sgpa_build

# 5. Run the jpackage command
jpackage --input /tmp/sgpa_build/input/ \
  --name "SGPA-Pharma" \
  --main-jar SGPAManager.jar \
  --main-class com.pharmacy.sgpa.Launcher \
  --type app-image \
  --icon /tmp/sgpa_build/logo.icns \
  --dest /tmp/sgpa_build/dist/ \
  --mac-package-identifier com.pharmacy.sgpa \
  --verbose

# 6. Replace the old app with the new one
rm -rf SGPA-Pharma.app
mv /tmp/sgpa_build/dist/SGPA-Pharma.app ./

echo "----------------------------------------------------"
echo "SUCCESS! Your full-screen stable app is ready."
echo "New features: Red (Critical) and Yellow (Warning) stock rows."
echo "----------------------------------------------------"