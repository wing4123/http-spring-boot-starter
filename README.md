# Getting Started

* 添加依赖
```
<dependency>
    <groupId>io.github.wing4123</groupId>
    <artifactId>http-spring-boot-starter</artifactId>
    <version>0.1.0</version>
</dependency>
```
* get请求
```
Http.get(url).send();
```
* post请求发送json
```
String jsonStr = "{\"key\", \"value\"}";
http.post(url, jsonStr).send();
```
* form表单请求
```
Map<String, String> formData = new HashMap<>();
formData.put("param1", "value1");
http.form(url, formData).send();
```
* 文件上传
```
http.upload(url)
  .addFile("file1", "fileName", Files.readAllBytes(Paths.get("d:/image1.jpg")))
  .addFile("file2", "d:/image1.jpg")
  .addParam("key", "value")
  .send();
```
* 添加请求头
```
http.get(url).addHeader("Authorization", "token").send();
```
* 返回数据类型
```
String result = http.get(url).send().string();
byte[] result = http.get(url).send().bytes();
JsonNode result = http.get(url).send().json();
User result = http.get(url).send().object(User.class);
```
* 异步调用
```
CompletableFuture<Response> future = http.get(url).sendAsync();
```
