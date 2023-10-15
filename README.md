# Getting Started

* 添加依赖
```
<dependency>
    <groupId>io.github.wing4123</groupId>
    <artifactId>http-spring-boot-starter</artifactId>
    <version>0.0.1-SNAPSHOT</version>
</dependency>
```
* get请求
```
Http.get(url).requestString();
```
* post请求发送json
```
String jsonStr = "{\"key\", \"value\"}";
http.post(url, jsonStr).requestString();
```
* form表单请求
```
Map<String, String> formData = new HashMap<>();
formData.put("param1", "value1");
http.form(url, formData).requestString();
```
* 文件上传
```
Map<String, byte[]> files = new HashMap<>();
formData.put("file1", Files.readAllBytes(Paths.get("d:/image.jpg")));
Map<String, String> params = new HashMap<>();
formData.put("param1", "value1");
http.upload(url, files, params).requestString();
```
或者
```
http.upload(url)
  .addFile("file1", "fileName", Files.readAllBytes(Paths.get("d:/image1.jpg")))
  .addFile("file2", "d:/image1.jpg")
  .addParam("key", "value")
  .requestString();
```
* 添加请求头
```
String result = http.get(url).headers("Authorization", "token").requestString();
```
* 返回数据类型
```
String result = http.get(url).requestString();
byte[] result = http.get(url).requestBytes();
JsonNode result = http.get(url).requestJson();
User result = http.get(url).requestObject(User.class);
```
* 异步调用
```
CompletableFuture<String> future = http.get(url).asyncRequestString();
```
