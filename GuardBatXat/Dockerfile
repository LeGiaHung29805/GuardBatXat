# --- GIAI ĐOẠN 1: MÔI TRƯỜNG BUILD ---
# Sử dụng Java 17
FROM eclipse-temurin:17-jdk-alpine AS builder
WORKDIR /app

# Copy các file cấu hình Maven vào trước
COPY .mvn/ .mvn
COPY mvnw pom.xml ./

# Cấp quyền thực thi cho file mvnw (rất quan trọng để tránh lỗi Permission Denied)
RUN chmod +x mvnw

# Tải trước các thư viện (giúp các lần build sau cực kỳ nhanh)
RUN ./mvnw dependency:go-offline

# Copy toàn bộ mã nguồn vào và tiến hành đóng gói (Bỏ qua chạy test để tiết kiệm thời gian)
COPY src ./src
RUN ./mvnw clean package -DskipTests

# --- GIAI ĐOẠN 2: MÔI TRƯỜNG CHẠY (RUNTIME) ---
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Chỉ copy file .jar đã hoàn thiện từ Giai đoạn 1 sang
COPY --from=builder /app/target/*.jar app.jar

# Mở cổng 8080 cho bên ngoài gọi vào
EXPOSE 8080

# Lệnh khởi động Spring Boot
ENTRYPOINT ["java", "-jar", "app.jar"]