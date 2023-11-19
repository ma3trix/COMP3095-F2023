#------------------------------
#Dockerfile for product-service
#------------------------------

#-----------
#BUILD stage
#-----------

#base image
FROM gradle:8-jdk17 AS builder

#Copy the application files from the host machine to the image filesystem
COPY --chown=gradle . /home/gradle/src

# set the directory within the image, for executing future commands
WORKDIR /home/gradle/src

# run gradle build within the container - NOT the host machine
RUN gradle build -x test

#------------------------
# Package Stage
#------------------------

from openjdk:17

RUN mkdir /app

COPY --from=builder /home/gradle/src/build/libs/*.jar /app/api-gateway.jar

EXPOSE 8181

ENTRYPOINT ["java", "-jar", "/app/api-gateway.jar"]

