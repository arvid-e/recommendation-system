plugins {
	java
	id("org.springframework.boot") version "4.1.2"
	id("io.spring.dependency-management") version "1.1.7"
}

group = "io.github.arvid-e"
version = "0.0.1-SNAPSHOT"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(21)
	}
}

repositories {
	mavenCentral()
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.springframework.boot:spring-boot-starter-flyway")
	runtimeOnly("org.flywaydb:flyway-database-postgresql")
	runtimeOnly("org.postgresql:postgresql")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.springframework.boot:spring-boot-starter-data-jpa-test")
	testRuntimeOnly("com.h2database:h2")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
	developmentOnly("me.paulschwarz:springboot4-dotenv:5.1.0")
}

tasks.withType<Test> {
	useJUnitPlatform()
}

tasks.withType<Test> {
    useJUnitPlatform()
    systemProperty("spring.profiles.active", "dev")
}
