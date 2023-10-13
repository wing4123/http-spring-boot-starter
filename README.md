# Getting Started

* import dependency
```
<dependency>
    <groupId>io.github.wing4123</groupId>
	<artifactId>http-spring-boot-starter</artifactId>
	<version>0.0.1-SNAPSHOT</version>
</dependency>
```
* get request
```
Http.get(url).requestString();
```
* post json request
```
String jsonStr = "{\"key\", \"value\"}";
http.post(url, jsonStr).requestString();
```
* form request
```
Map<String, String> formData = new HashMap<>();
formData.put("param1", "value1");
http.form(url, formData).requestString();
```
* upload file request
```
Map<String, String> files = new HashMap<>();
formData.put("file1", Files.readAllBytes(Paths.get("d:/image0.jpg")));
Map<String, String> params = new HashMap<>();
formData.put("param1", "value1");
http.upload(url, files, params).requestString();
```
or
```
http.upload(url)
  .addFile("name", "fileName", Files.readAllBytes(Paths.get("d:/image.jpg")))
  .addParam("key", "value")
  .requestString();
```
* add headers
```
String result = http.get(url).headers("Authorization", "token").requestString();
```
* result type
```
String result = http.get(url).requestString();
byte[] result = http.get(url).requestBytes();
JsonNode result = http.get(url).requestJson();
User result = http.get(url).requestObject(User.class);
```
