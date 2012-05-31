# Usage Notes
NARD (Not A Real Debugger, just a log printer) is a simple JAVA application for printing out the BlackBerry device System.out through a USB connection.

To run the application you can execute the `/run.sh` script

OR to execute it manually use: `java -Djava.library.path="lib" -d32 -jar target/NARD.jar`

Once the application is running simply connect your device and the System.out should start to print out. You can disconnect the device and reconnect without have to restart the NARD application.

# Build or Download
To build this you need to use Maven. You can also find a pre-built copy under the included [nard.zip](https://github.com/hardisonbrewing/nard/raw/master/nard.zip).

The JDWP is a required dependency. It is included with this project under the `/docs` folder.
To install the JAR to your local Maven repository you can execute the `/docs/install.sh` script

OR add the following remote repository to download the JAR automatically:

	<repositories>
		<repository>
			<id>hardisonbrewing-releases</id>
			<name>hardisonbrewing-releases</name>
			<url>http://repo.hardisonbrewing.org/nexus/content/repositories/releases/</url>
		</repository>
		<repository>
			<id>hardisonbrewing-snapshots</id>
			<name>hardisonbrewing-snapshots</name>
			<url>http://repo.hardisonbrewing.org/nexus/content/repositories/snapshots/</url>
		</repository>
	</repositories>

# YouTube Demo
[![BlackBerry Custom Debugger][kSsg1HFGY-k_img]][kSsg1HFGY-k_link]
[kSsg1HFGY-k_link]: http://www.youtube.com/watch?v=kSsg1HFGY-k  "BlackBerry Custom Debugger"
[kSsg1HFGY-k_img]: http://img.youtube.com/vi/kSsg1HFGY-k/2.jpg  "BlackBerry Custom Debugger"