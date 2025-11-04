#!/bin/bash

SRC_DIR="source"
BIN_DIR="bin"

mkdir -p "$BIN_DIR"

JAVA_FILES=$(find "$SRC_DIR" -name "*.java")

javac -d "$BIN_DIR" $JAVA_FILES
