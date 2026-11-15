JAVA_HOME := $(or $(shell unset JAVA_HOME; /usr/libexec/java_home -v 25 2>/dev/null),$(JAVA_HOME))
export JAVA_HOME

.PHONY: verify v
verify v:
	./mvnw verify
