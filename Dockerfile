FROM java:8-alpine
COPY ./build/libs/rssparser-1.0-all.jar .
CMD ["java", "-jar", "rssparser-1.0-all.jar"]
