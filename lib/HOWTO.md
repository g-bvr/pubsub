# HOWTO collect dependecies #

Comment out comment in org.jkube.git-beaver in pom.xml
Run maven plugin target dependency:copy-dependencies
Copy target/lib to lib
Commit and check in files in lib