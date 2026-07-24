#!/bin/bash
# KuiklyTable Maven 发布脚本
# 发布 kuikly-table 库到 Maven 仓库

set -e

echo "=== KuiklyTable Maven Publish ==="
echo "Version: 1.0.0-2.1.21"

# 发布库模块
./gradlew :kuikly-table:publishAllPublicationsToMavenRepository

echo "=== Publish Complete ==="
