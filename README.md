![Devoxx4Kids](http://www.devoxx4kids.de/wp-content/uploads/2015/07/cropped-header_hp.jpg)

[![Build Status](https://travis-ci.org/Devoxx4KidsDE/drone-controller.svg?branch=master)](https://travis-ci.org/Devoxx4KidsDE/drone-controller)
[![Coverage Status](https://coveralls.io/repos/github/Devoxx4KidsDE/drone-controller/badge.svg?branch=master)](https://coveralls.io/github/Devoxx4KidsDE/drone-controller?branch=master)

# Drone Controller

This library encapsulates the connection and the commands that can be send to the drone.

## Maven

add the jitpack repo : 

		<repositories>
			<repository>
				<id>jitpack.io</id>
				<url>https://jitpack.io</url>
			</repository>
		</repositories>
		
Then add the dependency : 

	<dependency>
            <groupId>com.github.Lastrik</groupId>
            <artifactId>drone-controller</artifactId>
            <version>0.3.0</version>
        </dependency>

		
## Release

        mvn versions:set -DnewVersion=${newVersion}

then

        mvn clean deploy -P release

then

        git tag ${newVersion}

and finally push it.