JAVA_HOME := $(or $(shell unset JAVA_HOME; /usr/libexec/java_home -v 25 2>/dev/null),$(JAVA_HOME))
export JAVA_HOME

.PHONY: verify v
verify v:
	./mvnw verify

# Interactive Spring Shell chat in the terminal (the 'cli' profile).
.PHONY: start
start:
	./mvnw -q spring-boot:run -Dspring-boot.run.arguments=--cli
