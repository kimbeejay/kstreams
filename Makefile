# See http://clarkgrubb.com/makefile-style-guide
MAKEFLAGS += --warn-undefined-variables
SHELL := bash
.SHELLFLAGS := -eu -o pipefail -c
.DEFAULT_GOAL := all
.DELETE_ON_ERROR:
.SUFFIXES:

.PHONY: all
all: test publish

.PHONY: test
test:
	-@echo "-> $@"
	-./gradlew test

.PHONY: publish
publish:
	-@echo "-> $@"
	-./gradlew publish
