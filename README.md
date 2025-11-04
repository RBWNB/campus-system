Campus System
=============
Spring Boot (JDK21) 学工管理系统示例

Instructions:
1. Ensure MySQL is running and you have a database named `campus` (the application will create tables automatically).
   If you need to create: `CREATE DATABASE campus CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;`
2. Update DB credentials in src/main/resources/application.yml if needed (already set to your provided credentials).
3. Build and run:
   mvn clean package
   java -jar target/campus-system-0.0.1-SNAPSHOT.jar
4. Open http://localhost:8080/login.html to use the simple login page.
   Default admin: admin / admin123
   Default student: student / student123
