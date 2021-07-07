# Usage

~~~
mvn package
docker-compose up --build
curl -d '{"url": "long-url-here"}' -H "Content-Type: application/json" -H "Accept: application/json" -X POST localhost:8080/short 
curl -d '{"url": "short-url-here"}' -H "Content-Type: application/json" -H "Accept: application/json" -X POST localhost:8080/long 
~~~
### OR
manually in application.properties
